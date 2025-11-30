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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.util.CollectionUtil;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.util.ipc.impl.Message;
import com.github.jlangch.venice.util.ipc.impl.queue.BoundedQueue;
import com.github.jlangch.venice.util.ipc.impl.queue.CircularBuffer;
import com.github.jlangch.venice.util.ipc.impl.queue.IpcQueue;


public class WalQueueManager {

    public WalQueueManager(final File walDir) {
        this.walDir = walDir;
    }

    public void preloadQueues(
        final Map<String, IpcQueue<Message>> p2pQueues
    ) throws IOException, InterruptedException {
        for(File logFile :listLogFiles()) {
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

            p2pQueues.put(queueName, new WalBasedQueue(queue, walDir));
        };
    }

    public List<File> listLogFiles() {
        return Arrays
                .stream(walDir.listFiles())
                .filter(f -> f.getName().endsWith(".wal"))
                .collect(Collectors.toList());
    }

    public List<String> listQueueNames() {
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


    private final File walDir;
}
