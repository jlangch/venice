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
import java.util.List;
import java.util.UUID;
import java.util.zip.CRC32;


/**
 * Write-Ahead-Log
 *
 * <p>WAL record:
 *
 * <pre>
 * +-----------+-----------+-----------+------------+-------------+------------+
 * |   MAGIC   |    LSN    |   TYPE    |    UUID    | PAYLOAD_LEN |  CHECKSUM  |
 * |  4 bytes  |  8 bytes  |  4 bytes  |  16 bytes  |   4 bytes   |  4 bytes   |
 * +-----------+-----------+-----------+------------+-------------+------------+
 * |                                PAYLOAD                                    |
 * |                                n bytes                                    |
 * +---------------------------------------------------------------------------+
 *
 * •  MAGIC        – int constant 0xCAFEBABE
 * •  LSN          – long, log sequence number starts from 1 and increments per append.
 * •  TYPE         – int, record type
 * •  UUID         – 16 bytes, record UUID
 * •  PAYLOAD_LEN  – int, number of bytes in payload.
 * •  CHECKSUM     – int, CRC32 of payload bytes.
 *
 * HEADER_SIZE = 4 + 8 + 4 + 16 + 4 + 4 = 40 bytes.
 * </pre>
 */
public final class WriteAheadLog implements Closeable {

     public WriteAheadLog(final File file) throws IOException {
        this.file = file;
        this.raf = new RandomAccessFile(file, "rw");
        this.channel = raf.getChannel();

        // Recover state if file already exists / has content
        recover();
    }

     /**
      * Append an entry to the WAL and fsync it.
      *
      * @param entry WAL entry
      * @return LSN assigned to this record starts (from 1 and increments per append)
      */
     public synchronized long append(
             final WalEntry entry
     ) throws IOException {
         if (entry == null) {
             throw new IllegalArgumentException("entry must not be null");
         }

         return append(entry.getType(), entry.getUUID(), entry.getPayload());
     }

    /**
     * Append a payload to the WAL and fsync it.
     *
     * @param type type of this log record
     * @param uuid uuid of this log record
     * @param payload bytes of this log record
     * @return LSN assigned to this record starts (from 1 and increments per append)
     */
    public synchronized long append(
            final int type,
            final UUID uuid,
            final byte[] payload
    ) throws IOException {
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

        final int payloadLength = payload.length;
        final CRC32 crc32 = new CRC32();
        crc32.update(payload);
        final int checksum = (int) crc32.getValue();

        // Prepare header + payload buffer
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE + payloadLength);
        buffer.putInt(MAGIC);
        buffer.putLong(lsn);
        buffer.putInt(type);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        buffer.putInt(payloadLength);
        buffer.putInt(checksum);
        buffer.put(payload);
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

    /**
     * Read all valid entries from the WAL (from beginning to last good record).
     */
    public synchronized List<WalEntry> readAll() throws IOException {
        final List<WalEntry> result = new ArrayList<>();
        channel.position(0);

        long position = 0L;
        while (position < validEndPosition) {
            WalEntry entry = readOneAtCurrentPosition();
            if (entry == null) {
                break;
            }
            result.add(entry);
            position = channel.position();
        }
        return result;
    }

    /**
     * Recover WAL state: scan from beginning, validate records,
     * stop at first corruption/partial record, and optionally truncate.
     */
    private void recover() throws IOException {
        channel.position(0);
        final long fileSize = channel.size();
        long position = 0L;
        long lastGoodLsn = 0L;
        long lastGoodEnd = 0L;

        while (position < fileSize) {
            try {
                WalEntry entry = readOneAtCurrentPosition();
                if (entry == null) {
                    break;
                }
                lastGoodLsn = entry.getLsn();
                lastGoodEnd = channel.position();
                position = lastGoodEnd;
            }
            catch (EOFException e) {
                // Partial header or payload at the end: stop, treat as corruption
                break;
            }
            catch (CorruptedRecordException e) {
                // Data corruption: stop scanning here
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
    }

    /**
     * Read a single entry at the current channel position.
     * On EOF/partial data, throws EOFException.
     * On corruption (bad magic or checksum), throws CorruptedRecordException.
     */
    private WalEntry readOneAtCurrentPosition() throws IOException {
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
        final byte[] payload = new byte[length];
        payloadBuf.get(payload);

        // Validate checksum
        final CRC32 crc32 = new CRC32();
        crc32.update(payload);

        final int actualChecksum = (int) crc32.getValue();
        if (actualChecksum != checksum) {
            throw new CorruptedRecordException(
                    "Checksum mismatch. expected=" + checksum + " actual=" + actualChecksum);
        }

        return new WalEntry(lsn, type, uuid, payload);
    }

    /**
     * Read into buffer until it's full or EOF is reached.
     *
     * @return total bytes read, or -1 if EOF encountered and no bytes read.
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
        channel.close();
        raf.close();
    }

    public long getLastLsn() {
        return lastLsn;
    }

    public File getFile() {
        return file;
    }


    private static final int MAGIC = 0xCAFEBABE;

    // header: magic + lsn + type + uuid + length + checksum
    private static final int HEADER_SIZE = 4 + 8 + 4 + 16 + 4 + 4;


    private final File file;
    private final RandomAccessFile raf;
    private final FileChannel channel;

    // last written LSN
    private long lastLsn = 0L;

    // position up to which the log is considered valid (last good record end)
    private long validEndPosition = 0L;

}