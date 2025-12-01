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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.util.CollectionUtil;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.util.ipc.IMessage;
import com.github.jlangch.venice.util.ipc.impl.Message;
import com.github.jlangch.venice.util.ipc.impl.queue.BoundedQueue;
import com.github.jlangch.venice.util.ipc.impl.queue.CircularBuffer;
import com.github.jlangch.venice.util.ipc.impl.queue.IpcQueue;


/**
 * Write-Ahead-Log queue manager
 *
 * <p>thread-safe
 */
public class WalQueueManager {

    public WalQueueManager() {
    }

    public void activate(final File walDir) {
        this.walDir.set(walDir);
    }

    public boolean isEnabled() {
        return walDir.get() != null;
    }

    public File getWalDir() {
        return walDir.get();
    }

    public Map<String, IpcQueue<Message>> preloadQueues()
    throws IOException, InterruptedException {
        if (!isEnabled()) {
            throw new VncException("Write-Ahead-Log is not active");
        }

        final Map<String, IpcQueue<Message>> queues = new HashMap<>();

        for(File logFile : listLogFiles()) {
            final String queueName = WalQueueManager.toQueueName(logFile);


            // load all Write-Ahead-Log entries and compact the entries
            final List<WalEntry> entries = WriteAheadLog.compact(
                                                WriteAheadLog.loadAll(logFile),
                                                true); // discard expired entries

            // read the configuration WAL entry to get the queue type
            // and its capacity
            final WalEntry firstEntry = CollectionUtil.first(entries);
            final ConfigWalEntry config = firstEntry.getType() == WalEntryType.CONFIG
                                             ? ConfigWalEntry.fromWalEntry(firstEntry)
                                             : null;

            final IpcQueue<Message> queue = toQueue(queueName, config);

            // load the write-ahead-log entries into the queue, take care for
            // the queue capacity
            final int gap = Math.min(entries.size(), queue.capacity() - queue.size());
            if (gap > 0) {
                for(WalEntry e : entries.subList(entries.size() - gap, entries.size())) {
                    if (WalEntryType.DATA == e.getType()) {
                        final Message m = MessageWalEntry.fromWalEntry(e).getMessage();
                        queue.offer(m, 0, TimeUnit.MILLISECONDS);
                    }
                }
            }

            queues.put(queueName, new WalBasedQueue(queue, walDir.get()));
        };

        return queues;
    }

    public List<IMessage> loadWalQueueMessages(
        final String queueName
    ) throws IOException {
        Objects.requireNonNull(queueName);

        if (!isEnabled()) {
            throw new VncException("Write-Ahead-Log is not active");
        }

        final File logFile = new File(walDir.get(), toFileName(queueName));
        return loadWalQueueMessages(logFile);
    }

    public List<IMessage> loadWalQueueMessages(
        final File logFile
    ) throws IOException {
        Objects.requireNonNull(logFile);

        if (!isEnabled()) {
            throw new VncException("Write-Ahead-Log is not active");
        }

        if (logFile.isFile()) {
            final List<IMessage> messages = new ArrayList<>();

            // load all Write-Ahead-Log entries and compact the entries
            final List<WalEntry> entries = WriteAheadLog.compact(
                                                WriteAheadLog.loadAll(logFile),
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

    public void close(final Collection<IpcQueue<Message>> queues)
    throws IOException, InterruptedException {
        for(IpcQueue<Message> q : queues) {
            if (q instanceof WalBasedQueue) {
               try {
                   ((WalBasedQueue)q).close();
               }
               catch(Exception ignore) { }
            }
        }
    }

    public List<File> listLogFiles() {
        if (!isEnabled()) {
            throw new VncException("Write-Ahead-Log is not active");
        }

        return Arrays
                .stream(walDir.get().listFiles())
                .filter(f -> f.getName().endsWith(".wal"))
                .collect(Collectors.toList());
    }

    public List<String> listQueueNames() {
        if (!isEnabled()) {
            throw new VncException("Write-Ahead-Log is not active");
        }

        return listLogFiles()
                .stream()
                .map(f -> toQueueName(f))
                .collect(Collectors.toList());
    }


    public static String toFileName(final String queueName) {
        return queueName.replace('/', '$') + ".wal";
    }

    public static String toQueueName(final File file) {
        return StringUtil.removeEnd(file.getName().replace('$', '/'), ".wal");
    }


    private static IpcQueue<Message> toQueue(
            final String queueName,
            final ConfigWalEntry config
    ) {
        if (config == null) {
            return new BoundedQueue<Message>(queueName, 200, false, true);
        }
        else if (config.isBoundedQueue()) {
            return new BoundedQueue<Message>(queueName, config.getQueueCapacity(), false, true);
        }
        else {
            return new CircularBuffer<Message>(queueName, config.getQueueCapacity(), false, true);
        }
    }


    private final AtomicReference<File> walDir = new AtomicReference<>();
}
