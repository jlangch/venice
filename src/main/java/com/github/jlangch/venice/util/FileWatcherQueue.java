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
package com.github.jlangch.venice.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.io.FileUtil;


public class FileWatcherQueue implements Closeable {

    private FileWatcherQueue(final File walFileDir) {
        this.walFile = walFileDir == null ? null : new File(walFileDir, "filewatcher.wal");
    }

    public static FileWatcherQueue create() {
        return create(null);
    }

    public static FileWatcherQueue create(final File walFileDir) {
        if (walFileDir != null && !walFileDir.isDirectory()) {
            throw new RuntimeException(
                    "WAL dir " + walFileDir + " is not a directory");
        }

        // initialize
        final FileWatcherQueue queue = new FileWatcherQueue(walFileDir);
        queue.init();
        return queue;
    }

    public File getWalFile() {
        return walFile;
    }

    public void removeWalFile() {
        if (walFile != null && walFile.isFile()) {
            walFile.delete();
        }
    }

    public void clearWalFile() {
        if (walFile != null) {
            try {
                new FileWriter(walFile, false).close();
            }
            catch(IOException ex) {
                throw new RuntimeException(
                        "Failed to initialize FileWatcher WAL file",
                        ex);
            }
        }
    }

    public int size() {
        synchronized(queue) {
            return queue.size();
        }
    }

    public boolean isEmpty() {
        synchronized(queue) {
            return queue.isEmpty();
        }
    }

    public void clear() {
        synchronized(queue) {
            queue.clear();
        }
    }

    public void push(final File file) {
        synchronized(queue) {
            addToWalFile(WalAction.PUSH, file);
            queue.removeIf(it -> it.equals(file));
            queue.add(file);
        }
    }

    public File pop() {
        synchronized(queue) {
            if (queue.isEmpty()) {
                return null;
            }
            else {
                final File file = queue.removeFirst();
                addToWalFile(WalAction.POP, file);
                return file;
            }
        }
    }

    public List<File> pop(final int n) {
        synchronized(queue) {
            final List<File> files = new ArrayList<>();
            for(int ii=0; ii<n && !queue.isEmpty(); ii++) {
                final File file = queue.removeFirst();
                addToWalFile(WalAction.POP, file);
                files.add(file);
            }
            return files;
        }
    }

    public void load() {
        if (walFile == null) {
            return;
        }

        synchronized(queue) {
            queue.clear();

            if (!walFile.isFile()) {
            	return;
            }

            StringUtil.splitIntoLines(
                            new String(
                                    FileUtil.load(walFile),
                                    Charset.forName("UTF-8")))
                      .stream()
                      .filter(s -> StringUtil.isNotBlank(s))
                      .map(s -> s.split("[|]"))
                      .filter(e -> e.length == 2)
                      .forEach(e -> {
                          final File f = new File(e[1]);
                          if (e[0].equals(WalAction.PUSH.name())) {
                              queue.removeIf(it -> it.equals(f));
                              queue.add(f);
                          }
                          else if (e[0].equals(WalAction.POP.name())) {
                              queue.removeIf(it -> it.equals(f));
                          }
                      });
        }
    }

    public void save() {
        if (walFile == null) {
            return;
        }

        synchronized(queue) {
            try (FileWriter fw = new FileWriter(walFile, false)) {
                queue.forEach(f -> {
                    try {
                        fw.write(walEntry(WalAction.PUSH, f));
                    }
                    catch(IOException ex) {
                        throw new RuntimeException(
                                "Failed to write FileWatcher WAL entry",
                                ex);
                    }});
            }
            catch(IOException ex) {
                throw new RuntimeException(
                        "Failed to save FileWatcher WAL entries",
                        ex);
            }
        }
    }

    @Override
    public void close() {
        save();
    }

    private void init() {
        synchronized(queue) {
            if (walFile == null) {
                return;
            }

            if (this.walFile.isFile()) {
                try {
                    load();
                }
                catch(Exception ex) {
                    throw new RuntimeException(
                            "Failed to initially load the FileWatcherQueue from the WAL file",
                            ex);
                }
            }
            else {
                try {
                    new FileWriter(walFile, false).close();
                }
                catch(IOException ex) {
                    throw new RuntimeException(
                            "Failed to initialize FileWatcher WAL file",
                            ex);
                }
            }
        }
    }

    private void addToWalFile(final WalAction action, final File file) {
        if (walFile == null) {
            return;
        }

        try (FileWriter fw = new FileWriter(walFile, true)) {
            fw.write(walEntry(action, file));
        }
        catch(IOException ex) {
            throw new RuntimeException("Failed to write FileWatcher WAL entry", ex);
        }
    }

    private String walEntry(final WalAction action, final File file) {
        return action.name() + "|" + file.getAbsolutePath() + "\n";
    }


    private static enum WalAction { PUSH, POP };

    private final File walFile;
    private final LinkedList<File> queue = new LinkedList<>();
}
