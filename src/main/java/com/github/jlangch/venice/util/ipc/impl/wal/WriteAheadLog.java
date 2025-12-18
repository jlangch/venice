/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2025 Venice
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jlangch.venice.util.ipc.impl.wal;

import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.CRC32;

import com.github.jlangch.venice.util.ipc.WriteAheadLogException;
import com.github.jlangch.venice.util.ipc.impl.util.Compressor;
import com.github.jlangch.venice.util.ipc.impl.wal.entry.AckWalEntry;
import com.github.jlangch.venice.util.ipc.impl.wal.entry.WalEntry;
import com.github.jlangch.venice.util.ipc.impl.wal.entry.WalEntryType;


/**
 * Write-Ahead-Log
 *
 * <p>WAL record:
 *
 * <pre>
 * +-----------+------------+-------------+------------+
 * |   MAGIC   |    LSN     |   TYPE      |    UUID    |
 * |  4 bytes  |  8 bytes   |  4 bytes    |  16 bytes  |
 * +-----------+------------+-------------+------------+
 * |   EXPIRE  | COMPRESSED | PAYLOAD_LEN |  CHECKSUM  |
 * |  8 bytes  |  4 bytes   |   4 bytes   |  4 bytes   |
 * +===========+============+=============+============+
 * |                     PAYLOAD                       |
 * |                     n bytes                       |
 * +---------------------------------------------------+
 *
 * •  MAGIC        – int constant 0xCAFEBABE
 * •  LSN          – long, log sequence number starts from 1 and increments per append.
 * •  TYPE         – int, record type
 * •  UUID         – 16 bytes, record UUID
 * •  EXPIRE       – 8 bytes, expiry timestamp (millis since epoch or -1 if never expires)
 * •  COMPRESSED   – 4 bytes, 0=not compressed, 1=compressed
 * •  PAYLOAD_LEN  – int, number of bytes in payload.
 * •  CHECKSUM     – int, CRC32 of payload bytes.
 *
 * HEADER_SIZE = 4 + 8 + 4 + 16 + 8 + 4 + 4 + 4 = 52 bytes.
 * </pre>
 */
public final class WriteAheadLog implements Closeable {

    public WriteAheadLog(
            final File file,
            final WalLogger logger
    ) throws WriteAheadLogException {
        this(file, false, logger);
    }

    public WriteAheadLog(
            final File file,
            final boolean compress,
            final WalLogger logger
    ) throws WriteAheadLogException {
        if (file == null) {
            throw new IllegalArgumentException("file must not be null");
        }

        try {
            this.file = file;
            this.raf = new RandomAccessFile(file, "rw");
            this.channel = raf.getChannel();
            this.compressor = compress ? new Compressor(COMPRESSION_CUTOFF) : Compressor.off();

            this.logger = logger;

            logger.info(file, "WAL opening...");

            // Recover state if Write-Ahead-Log exists / has content
            recover();

            logger.info(file, "WAL opened");
        }
        catch(Exception ex) {
            throw new WriteAheadLogException(
                    String.format(
                        "Failed to open and recover Write-Ahead-Log \"%s\"!",
                        file.getAbsolutePath()),
                    ex);
        }
    }

    /**
     * Checks if this Write-Ahead-Log is compressing data
     *
     * @return <code>true</code> if this Write-Ahead-Log is compressing the WAL entries
     *         else <code>false</code>
     */
    public boolean isCompressing() {
        return compressor.isActive();
    }

    /**
     * Append an entry to the WAL and fsync it.
     *
     * @param entry WAL entry
     * @return LSN assigned to this record starts (from 1 and increments per append)
     * @throws IOException on I/O failure
     */
    public synchronized long append(final WalEntry entry) throws IOException {
        if (entry == null) {
            throw new IllegalArgumentException("entry must not be null");
        }

        return append(
                entry.getType(),
                entry.getUUID(),
                entry.getExpiry(),
                entry.getPayload());
    }

    /**
     * Append a payload to the WAL and fsync it.
     *
     * @param type type of this log record
     * @param uuid uuid of this log record
     * @param expiry expiration of this log record (milliseconds since epoch or -1 if nor expiration)
     * @param payload bytes of this log record
     * @return LSN assigned to this record starts (from 1 and increments per append)
     * @throws WriteAheadLogException on any failure appending the record to the Write-Ahead-Log
     */
    public synchronized long append(
            final WalEntryType type,
            final UUID uuid,
            final long expiry,
            final byte[] payload
    ) throws WriteAheadLogException {
        try {
            if (uuid == null) {
                throw new IllegalArgumentException("uuid must not be null");
            }
            if (payload == null) {
                throw new IllegalArgumentException("payload must not be null");
            }
            if (payload.length < 0) {
                throw new IllegalArgumentException("payload length invalid");
            }

            final long lsn = ++lastLsn;

            final boolean compress = compressor.isActive()
                                        && type == WalEntryType.DATA
                                        && compressor.needsCompression(payload);

            final byte[] payloadCompressed = compressor.compress(payload, compress);

            final int payloadLength = payloadCompressed.length;
            final CRC32 crc32 = new CRC32();
            crc32.update(payloadCompressed);
            final int checksum = (int) crc32.getValue();

            // Prepare header + payload buffer
            ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE + payloadLength);
            buffer.putInt(MAGIC);
            buffer.putLong(lsn);
            buffer.putInt(type.getValue());
            buffer.putLong(uuid.getMostSignificantBits());
            buffer.putLong(uuid.getLeastSignificantBits());
            buffer.putLong(expiry);
            buffer.putInt(compress ? 1 : 0);
            buffer.putInt(payloadLength);
            buffer.putInt(checksum);
            buffer.put(payloadCompressed);
            buffer.flip();

            // Position at end of file
            channel.position(channel.size());

            // Write fully
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }

            // Force write to disk (data only, not metadata)
            channel.force(false);

            // Update valid end position
            validEndPosition = channel.position();

            return lsn;
        }
        catch(Exception ex) {
            logger.error(file, "Failed to append WAL record (type=" + type +")", ex);
            throw new WriteAheadLogException("Failed to append WAL record (type=" + type +")");
        }
    }

    /**
     * Read all valid entries from the WAL
     *
     * @param avoidDecompression if true do not decompress compressed entry payloads
     * @return a list of the read entries
     * @throws WriteAheadLogException if the Write-Ahead-Log contains a corrupted record or
     *         for any I/O error while reading the Write-Ahead-Log
     */
    public synchronized List<WalEntry> readAll(
        final boolean avoidDecompression
    ) throws WriteAheadLogException {
        return loadAll(file, avoidDecompression);
    }

    /**
     * Load all valid entries from the WAL
     *
     * @param file the log file
     * @param avoidDecompression if true do not decompress compressed entry payloads
     * @return a list of the read entries
     * @throws WriteAheadLogException if the Write-Ahead-Log contains a corrupted record or
     *         for any I/O error while reading the Write-Ahead-Log
     */
    public static List<WalEntry> loadAll(
            final File file,
            final boolean avoidDecompression
    ) throws WriteAheadLogException {
        if (file == null) {
            throw new IllegalArgumentException("file must not be null");
        }

        try (RandomAccessFile raf = new RandomAccessFile(file, "r");
             FileChannel channel = raf.getChannel()
        ) {
            final Compressor compressor = avoidDecompression
                                            ? Compressor.off()
                                            : new Compressor(0);

            final long fileSize = channel.size();

            final List<WalEntry> result = new ArrayList<>();
            channel.position(0);

            long position = 0L;
            while (position < fileSize) {
                WalEntry entry = readOneAtCurrentPosition(channel, compressor);
                if (entry == null) {
                    break;
                }
                result.add(entry);
                position = channel.position();
            }
            return result;
        }
        catch(CorruptedRecordException ex) {
            throw new WriteAheadLogException(
                    String.format(
                        "Corrupted Write-Ahead-Log \"%s\"!",
                        file.getAbsolutePath()),
                    ex);
        }
        catch(Exception ex) {
            throw new WriteAheadLogException(
                    String.format(
                        "Failed to read the entries from the Write-Ahead-Log \"%s\"!",
                        file.getAbsolutePath()),
                        ex);
        }
    }

    /**
     * Compact the given log file in-place.
     *
     * @param logFile the existing write-ahead-log
     * @param removeBackupLogFile if true remove the created backup write-ahead-log
     * @param discardExpiredEntries if true discard expired entries
     * @return all valid entries from the compacted WAL
     * @throws WriteAheadLogException if the Write-Ahead-Log contains a corrupted record or
     *         for any I/O error while reading the Write-Ahead-Log
     */
    public static List<WalEntry> compact(
        final File logFile,
        final boolean discardExpiredEntries,
        final boolean removeBackupLogFile
    ) throws WriteAheadLogException {
        if (logFile == null) {
            throw new IllegalArgumentException("file must not be null");
        }
        if (!logFile.exists()) {
            // nothing to do
            return new ArrayList<>();
        }

        File dir = logFile.getAbsoluteFile().getParentFile();
        String baseName = logFile.getName();

        File tmpFile = new File(dir, baseName + ".compact");
        File backupFile = new File(dir, baseName + ".bak");

        final WalLogger logger = WalLogger.withinDir(logFile.getParentFile());
        logger.info(logFile, "WAL compacting...");

        final List<WalEntry> pending = new ArrayList<>();

        // Note: for performance reason the WAL entries are not decompressed
        //       while reading the entries and not compressed agin on writing
        //       the compacted entries.
        //       This avoids a completely unnecessary decompress/compress cycle!

        // Phase 1: xxx.log -> [compact to] -> xxx.log.compact
        //          xxx.log -> [rename to]  -> xxx.log.bak
        try {
            // [1] read the entries of the old log
            try (WriteAheadLog oldLog = new WriteAheadLog(logFile, false, logger)) {
                pending.addAll(compact(oldLog.readAll(true), discardExpiredEntries));
            }

            // [2] Write a brand-new tmpLog with only pending messages
            if (tmpFile.exists() && !tmpFile.delete()) {
                logger.warn(logFile, "WAL compacting: Deleting stale tmp WAL " + tmpFile.getName() + " failed.");
                throw new IOException("Could not delete stale tmp file: " + tmpFile);
            }
            try (WriteAheadLog tmpLog = new WriteAheadLog(tmpFile, false, logger)) {
                for (WalEntry e : pending) {
                    tmpLog.append(e);
                }
            }

            // [3] Atomically-ish swap files: old -> .bak, tmp -> original
            //     (Simple approach; fsync directory etc. if stricter guarantees are required.)

            // remove old backup if exists
            if (backupFile.exists() && !backupFile.delete()) {
                logger.warn(logFile, "WAL compacting: Deleting old backup WAL " + backupFile.getName() + " failed.");
                throw new IOException("Could not delete old backup file: " + backupFile);
            }

            if (!logFile.renameTo(backupFile)) {
                // failed before touching tmpFile => safe
                throw new IOException("Failed to rename original log to backup: " +
                                      logFile + " -> " + backupFile);
            }
        }
        catch(IOException ex) {
        	final String msg = "WAL compacting failed due to I/O error! Leaving Write-Ahead-Log unchanged!";
            logger.warn(logFile, msg, ex);
            throw new WriteAheadLogException(msg, ex);
        }
        catch(CorruptedRecordException ex) {
        	final String msg = "WAL compacting failed due to corrupted data! Leaving Write-Ahead-Log unchanged!";
            logger.warn(logFile, msg, ex);
            throw new WriteAheadLogException(msg, ex);
        }
        catch(RuntimeException ex) {
        	final String msg = "WAL compacting failed! Leaving Write-Ahead-Log unchanged!";
            logger.warn(logFile, msg, ex);
            throw new WriteAheadLogException(msg, ex);
        }


        // Phase 2: xxx.log.compact -> [rename to] -> xxx.log
        //          xxx.log.bak     -> [remove]

        // [4] Rename compacted logfile to logfile
        if (!tmpFile.renameTo(logFile)) {
            final String failedRenameAction = "Rename compacted " + tmpFile.getName() + " -> " + logFile.getName()
                                                + " failed!";
            // try to restore original
            logger.warn(logFile, "WAL compacting failed: " + failedRenameAction);

            if (backupFile.renameTo(logFile)) {
                // old, uncompacted logfile successfully restored
                logger.warn(logFile, "Successfully restored uncompacted Write-Ahead-Log! No data lost!");
                throw new WriteAheadLogException(
                        "WAL compacting failed: " + failedRenameAction +
                        " Successfully restored uncompacted Write-Ahead-Log! No data lost!");
            }
            else {
                // FATAL error
                logger.error(logFile, "Fatal failure: Compensation rename backup WAL to old WAL failed! DATA LOST!");
                throw new WriteAheadLogException(
                        "WAL compacting failed: " + failedRenameAction +
                        " Fatal failure: Compensation rename backup WAL to old WAL failed! DATA LOST!");
            }
        }

        // [5] Optionally: delete the created backup file
        if (removeBackupLogFile) {
            if (!backupFile.delete()) {
                logger.warn(logFile, "WAL compacting: Deleting backup WAL " + backupFile.getName() + " failed.");
            }
        }

        logger.info(logFile, "WAL compacting done.");

        return pending;
    }

    /**
     * Compact a list of entries.
     *
     * @param entries a list of entries
     * @param discardExpiredEntries if true discard expired entries
     * @return the compacted list of entries
     */
    public static List<WalEntry> compact(
            final List<WalEntry> entries,
            final boolean discardExpiredEntries
    ) {
        final Map<UUID, WalEntry> pending = new LinkedHashMap<>();

        entries.forEach(e -> {
            if (WalEntryType.CONFIG == e.getType()) {
                pending.put(e.getUUID(), e);  // keep
            }
            else if (WalEntryType.ACK == e.getType()) {
                // acknowledge data entry
                AckWalEntry ackEntry = AckWalEntry.fromWalEntry(e);
                pending.remove(ackEntry.getAckedEntryUUID());  // remove by uuid
            }
            else if (WalEntryType.DATA == e.getType()) {
               // data entry, discard expired entries
               if (!discardExpiredEntries || !e.hasExpired()) {
                   pending.put(e.getUUID(), e);  // keep
               }
            }
        });

        return new ArrayList<>(pending.values());
    }


    /**
     * Read a single entry at the current channel position.
     * On EOF/partial data, throws EOFException.
     * On corruption (bad magic or checksum), throws CorruptedRecordException.
     *
     * @param channel the channel to read the entry from
     * @param compressor a compressor
     * @return the read WAL entry
     * @throws IOException on I/O failure
     * @throws CorruptedRecordException if the read entry is corrupted
     */
    private static WalEntry readOneAtCurrentPosition(
            final FileChannel channel,
            final Compressor compressor
    ) throws IOException, CorruptedRecordException {
        if (channel == null) {
            throw new IllegalArgumentException("channel must not be null");
        }
        if (compressor == null) {
            throw new IllegalArgumentException("compressor must not be null");
        }

        final ByteBuffer headerBuf = ByteBuffer.allocate(HEADER_SIZE);

        int bytesRead = readFully(channel, headerBuf);
        if (bytesRead == -1) {
            // EOF at header start
            return null;
        }
        if (bytesRead < HEADER_SIZE) {
            throw new EOFException("Partial header encountered");
        }

        headerBuf.flip();
        final int magic = headerBuf.getInt();
        if (magic != MAGIC) {
            throw new CorruptedRecordException("Bad magic: " + Integer.toHexString(magic));
        }

        final long lsn = headerBuf.getLong();
        final int type = headerBuf.getInt();
        final long uuidMostSigBits = headerBuf.getLong();
        final long uuidLeastSigBits = headerBuf.getLong();
        final long expiry = headerBuf.getLong();
        final boolean compressed = headerBuf.getInt() > 0;
        final int length = headerBuf.getInt();
        final int checksum = headerBuf.getInt();

        final UUID uuid = new UUID(uuidMostSigBits, uuidLeastSigBits);

        if (length < 0) {
            throw new CorruptedRecordException("Negative payload length: " + length);
        }

        final ByteBuffer payloadBuf = ByteBuffer.allocate(length);
        bytesRead = readFully(channel, payloadBuf);
        if (bytesRead < length) {
            throw new EOFException("Partial payload encountered");
        }

        payloadBuf.flip();
        final byte[] payloadCompressed = new byte[length];
        payloadBuf.get(payloadCompressed);

        final byte[] payload = compressor.decompress(payloadCompressed, compressed);

        // Validate checksum
        final CRC32 crc32 = new CRC32();
        crc32.update(payloadCompressed);

        final int actualChecksum = (int) crc32.getValue();
        if (actualChecksum != checksum) {
            throw new CorruptedRecordException(
                    "Checksum mismatch. expected=" + checksum + " actual=" + actualChecksum);
        }

        return new WalEntry(lsn, WalEntryType.fromCode(type), uuid, expiry, payload);
    }

    /**
     * Recover WAL state: scan from beginning, validate records,
     * stop at first corruption/partial record, and optionally truncate.
     *
     * @throws IOException on I/O failure
     */
    private boolean recover() throws IOException {
        channel.position(0);
        final long fileSize = channel.size();
        long position = 0L;
        long lastGoodLsn = 0L;
        long lastGoodEnd = 0L;

        boolean recoveredFromCorruption = true;

        while (position < fileSize) {
            try {
                WalEntry entry = readOneAtCurrentPosition(channel, compressor);
                if (entry == null) {
                    break;
                }
                lastGoodLsn = entry.getLsn();
                lastGoodEnd = channel.position();
                position = lastGoodEnd;
            }
            catch (EOFException e) {
                // Partial header or payload at the end: stop, treat as corruption
                logger.warn(file, "WAL recovery faced early EOF.");
                recoveredFromCorruption = false;
                break;
            }
            catch (CorruptedRecordException e) {
                // Data corruption: stop scanning here
                logger.warn(file, "WAL recovery faced corrupted recorder.");
                recoveredFromCorruption = false;
                break;
            }
        }

        // Truncate any trailing garbage/partial record
        if (lastGoodEnd < fileSize) {
            channel.truncate(lastGoodEnd);
        }

        this.lastLsn = lastGoodLsn;
        this.validEndPosition = lastGoodEnd;
        channel.position(validEndPosition);

        if (recoveredFromCorruption) {
            logger.info(file, "WAL recovered successfully.");
        }
        else {
            logger.warn(file, String.format(
                                "WAL truncated at lastGoodFilePos=%d, fileSize=%d",
                                lastGoodEnd,
                                 fileSize));
            logger.warn(file, "WAL recovered from corrupted file.");
        }

        return recoveredFromCorruption;
    }

    /**
     * Read into buffer until it's full or EOF is reached.
     *
     * @param channel a file channel
     * @param buffer a buffer to read
     * @return total bytes read, or -1 if EOF encountered and no bytes read.
     * @throws IOException on I/O failure
     */
    private static int readFully(
            final FileChannel channel,
            final ByteBuffer buffer
    ) throws IOException {
        int totalRead = 0;
        while (buffer.hasRemaining()) {
            final int read = channel.read(buffer);
            if (read == -1) {
                // EOF
                return totalRead == 0 ? -1 : totalRead;
            }
            totalRead += read;
        }
        return totalRead;
    }


    @Override
    public synchronized void close() throws IOException {
        try {
            raf.close();
            logger.info(file, "WAL closed");
        }
        catch(Exception ex) {
            logger.error(file, "WAL close error", ex);
        }
    }

    public long getLastLsn() {
        return lastLsn;
    }

    public File getFile() {
        return file;
    }


    private static final int MAGIC = 0xCAFEBABE;

    // header: magic + lsn + type + uuid + expire + length + checksum
    private static final int HEADER_SIZE = 4 + 8 + 4 + 16 + 8 + 4 + 4 + 4;

    // the record payload size at which the payload is compressed
    private static final int COMPRESSION_CUTOFF = 300;

    private final File file;
    private final RandomAccessFile raf;
    private final FileChannel channel;
    private final Compressor compressor;

    private final WalLogger logger;

    // last written LSN
    private long lastLsn = 0L;

    // position up to which the log is considered valid (last good record end)
    private long validEndPosition = 0L;
}