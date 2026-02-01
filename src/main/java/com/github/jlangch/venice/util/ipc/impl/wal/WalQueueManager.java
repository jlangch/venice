/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2026 Venice
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.util.ipc.IMessage;
import com.github.jlangch.venice.util.ipc.WriteAheadLogException;
import com.github.jlangch.venice.util.ipc.impl.Message;
import com.github.jlangch.venice.util.ipc.impl.dest.queue.IpcQueue;
import com.github.jlangch.venice.util.ipc.impl.wal.entry.MessageWalEntry;
import com.github.jlangch.venice.util.ipc.impl.wal.entry.WalEntry;
import com.github.jlangch.venice.util.ipc.impl.wal.entry.WalEntryType;


/**
 * Write-Ahead-Log queue manager
 *
 * <p>thread-safe
 */
public class WalQueueManager {

    public WalQueueManager() {
    }

    public void activate(
            final File walDir,
            final boolean compress,
            final boolean compactAtStart
    ) {
        if (!walDir.isDirectory()) {
            throw new WriteAheadLogException(
                    "The WAL directory '" + walDir.getAbsolutePath() + "' does not exist!");
        }

        this.walDir = walDir;
        this.logger = WalLogger.withinDir(walDir);
        this.compress = compress;
        this.compactAtStart = compactAtStart;
    }

    public boolean isEnabled() {
        return walDir != null;
    }

    public boolean isCompressed() {
        return compress;
    }

    public boolean isCompactAtStart() {
        return compactAtStart;
    }

    public File getWalDir() {
        return walDir;
    }

    public WalLogger getLogger() {
        return logger;
    }

    public List<IpcQueue<Message>> preloadQueues()
    throws WriteAheadLogException, InterruptedException {
        if (!isEnabled()) {
            throw new WriteAheadLogException("Write-Ahead-Log is not active");
        }

        final List<IpcQueue<Message>> queues = new ArrayList<>();

        for(File logFile : listLogFiles()) {
            if (compactAtStart) {
                logger.info(logFile, "WalQueueManager@preloadQueues: compacting WAL");
                WriteAheadLog.compact(logFile, logger, true, true);
            }

            logger.info(logFile, "WalQueueManager@preloadQueues: create queue from WAL");

            final IpcQueue<Message> queue = DurableBoundedQueue.createFromWal(logFile, logger);
            queues.add(queue);
        };

        return queues;
    }

    public List<IMessage> loadWalQueueMessages(
        final String queueName
    ) throws WriteAheadLogException {
        Objects.requireNonNull(queueName);

        if (!isEnabled()) {
            throw new WriteAheadLogException("Write-Ahead-Log is not active");
        }

        final File logFile = new File(walDir, toFileName(queueName));
        if (logFile.isFile()) {
            return loadWalQueueMessages(logFile);
        }
        else {
            throw new WriteAheadLogException(
                    "The Write-Ahead-Log for the queue '" + queueName + "' does not exist!");
        }
    }

    public void close(final Collection<IpcQueue<Message>> queues)
    throws IOException, InterruptedException {
        for(IpcQueue<Message> q : queues) {
            if (q instanceof DurableBoundedQueue) {
               try {
                   ((DurableBoundedQueue)q).close();
               }
               catch(Exception ignore) { }
            }
        }
    }

    public List<File> listLogFiles() {
        if (!isEnabled()) {
            throw new WriteAheadLogException("Write-Ahead-Log is not active");
        }

        return Arrays
                .stream(walDir.listFiles())
                .filter(f -> f.getName().endsWith(".wal"))
                .collect(Collectors.toList());
    }

    public List<String> listQueueNames() {
        if (!isEnabled()) {
            throw new WriteAheadLogException("Write-Ahead-Log is not active");
        }

        return listLogFiles()
                .stream()
                .map(f -> toQueueName(f))
                .collect(Collectors.toList());
    }

    public int countLogFiles() {
        if (!isEnabled()) {
            throw new WriteAheadLogException("Write-Ahead-Log is not active");
        }

        return listQueueNames().size();
    }

    public static String toFileName(final String queueName) {
        return queueName.replace('/', '$') + ".wal";
    }

    public static String toQueueName(final File file) {
        return StringUtil.removeEnd(file.getName().replace('$', '/'), ".wal");
    }


    private List<IMessage> loadWalQueueMessages(
        final File logFile
    ) throws WriteAheadLogException {
        Objects.requireNonNull(logFile);

        if (!isEnabled()) {
            throw new WriteAheadLogException("Write-Ahead-Log is not active");
        }

        if (logFile.isFile()) {
            final List<IMessage> messages = new ArrayList<>();

            // load all Write-Ahead-Log entries and compact the entries
            final List<WalEntry> entries = WriteAheadLog.compact(
                                                WriteAheadLog.loadAll(logFile, false),
                                                true); // discard expired entries

            for(WalEntry e : entries) {
                if (WalEntryType.DATA == e.getType()) {
                    final Message m = MessageWalEntry.fromWalEntry(e).getMessage();
                    messages.add(m);
                }
            }

            return messages;
        }
        else {
            return new ArrayList<>();
        }
    }


    private volatile File walDir;
    private volatile WalLogger logger;
    private volatile boolean compress;
    private volatile boolean compactAtStart;
}
