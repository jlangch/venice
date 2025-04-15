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
package com.github.jlangch.venice.impl.util.io;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.github.jlangch.venice.impl.thread.ThreadBridge;
import com.github.jlangch.venice.impl.threadpool.GlobalThreadFactory;


public class FileWatcher implements Closeable {

    public FileWatcher(
            final Path dir,
            final BiConsumer<Path,WatchEvent.Kind<?>> eventListener,
            final BiConsumer<Path,Exception> errorListener,
            final Consumer<Path> terminationListener
    ) throws IOException {
        ws = dir.getFileSystem().newWatchService();

        // https://stackoverflow.com/questions/18701242/how-to-watch-a-folder-and-subfolders-for-changes

        // com.sun.nio.file.ExtendedWatchEventModifier.FILE_TREE

        final WatchKey dirKey = dir.register(
                                    ws,
                                    StandardWatchEventKinds.ENTRY_CREATE,
                                    StandardWatchEventKinds.ENTRY_DELETE,
                                    StandardWatchEventKinds.ENTRY_MODIFY);

        keys.put(dirKey, dir);

        final Runnable runnable =
            () -> {
                while (true) {
                    try {
                        final WatchKey key = ws.take();
                        if (key == null) {
                            break;
                        }

                        final Path p = keys.get(key);
                        if (p != null) {
                            for (WatchEvent<?> event: key.pollEvents()) {
                                if (event.context() instanceof Path) {
                                    final Path e = Paths.get(p.toString(), ((Path)event.context()).toString());
                                    safeRun(() -> eventListener.accept(e, event.kind()));
                                }
                            }
                        }

                        key.reset();
                    }
                    catch(ClosedWatchServiceException ex) {
                        break;
                    }
                    catch(InterruptedException ex) {
                        // continue
                    }
                    catch(Exception ex) {
                        if (errorListener != null) {
                            safeRun(() -> errorListener.accept(dir, ex));
                        }
                        // continue
                    }
                }

                try { ws.close(); } catch(Exception e) {}

                if (terminationListener != null) {
                    safeRun(() -> terminationListener.accept(dir));
                }
            };

        final Thread th = GlobalThreadFactory.newThread("venice-watch-dir", runnable);
        th.setUncaughtExceptionHandler(ThreadBridge::handleUncaughtException);
        th.start();
    }

    public void register(final Path dir) throws IOException {
        final WatchKey dirKey = dir.register(
                                        ws,
                                        StandardWatchEventKinds.ENTRY_CREATE,
                                        StandardWatchEventKinds.ENTRY_DELETE,
                                        StandardWatchEventKinds.ENTRY_MODIFY);

        keys.put(dirKey, dir);
    }


    @Override
    public void close() throws IOException {
        ws.close();
    }


    private static void safeRun(final Runnable r) {
        try {
            if (r != null) {
                r.run();
            }
        }
        catch(Exception e) { e.printStackTrace(); }
    }


    private final WatchService ws;
    private final Map<WatchKey,Path> keys = new HashMap<>();
}
