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

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.github.jlangch.venice.impl.thread.ThreadBridge;
import com.github.jlangch.venice.impl.threadpool.GlobalThreadFactory;
import com.github.jlangch.venice.impl.types.VncKeyword;


public class FileWatcher implements Closeable {

    public FileWatcher(
            final Path dir,
            final BiConsumer<Path,WatchEvent.Kind<?>> eventListener,
            final BiConsumer<Path,Exception> errorListener,
            final Consumer<Path> terminationListener,
            final Consumer<Path> registerListener
    ) throws IOException {
        if (!dir.toFile().exists() || !dir.toFile().isDirectory()) {
            throw new RuntimeException("Folder " + dir + " does not exist or is not a directory");
        }

        this.ws = dir.getFileSystem().newWatchService();
        this.errorListener = errorListener;
        this.registerListener = registerListener;

        // https://stackoverflow.com/questions/18701242/how-to-watch-a-folder-and-subfolders-for-changes

        // com.sun.nio.file.ExtendedWatchEventModifier.FILE_TREE

        register(dir);

        final Runnable runnable =
            () -> {
                while (true) {
                    try {
                        final WatchKey key = ws.take();
                        if (key == null) {
                            break;
                        }

                        final Path dirPath = keys.get(key);
                        if (dirPath == null) {
                            continue;
                        }

                        key.pollEvents()
                           .stream()
                           .filter(e -> (e.kind() != OVERFLOW))
                           .forEach(e -> {
                               @SuppressWarnings("unchecked")
                               final Path p = ((WatchEvent<Path>)e).context();
                               final Path absPath = dirPath.resolve(p);
                               if (absPath.toFile().isDirectory() && e.kind() == ENTRY_CREATE) {
                                   // register the new subdir
                                   register(ws, keys, errorListener, registerListener, dir);
                               }
                               safeRun(() -> eventListener.accept(absPath, e.kind()));
                             });

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
        if (!dir.toFile().exists() || !dir.toFile().isDirectory()) {
            throw new RuntimeException("Folder " + dir + " does not exist or is not a directory");
        }

        register(ws, keys, errorListener, registerListener, dir);
     }


    @Override
    public void close() throws IOException {
        ws.close();
    }

    public static VncKeyword convertEvent(final WatchEvent.Kind<?> kind) {
        if (kind == null) {
            return new VncKeyword("unknown");
        }
        else {
            switch(kind.name()) {
                case "ENTRY_CREATE": return new VncKeyword("created");
                case "ENTRY_DELETE": return new VncKeyword("deleted");
                case "ENTRY_MODIFY": return new VncKeyword("modified");
                case "OVERFLOW":     return new VncKeyword("overflow");
                default:             return new VncKeyword("unknown");
            }
        }
    }

    private static void register(
            final WatchService ws,
            final Map<WatchKey,Path> keys,
            final BiConsumer<Path,Exception> errorListener,
            final Consumer<Path> registerListener,
            final Path dir
    ) {
        try {
            final WatchKey dirKey = dir.register(
                    ws,
                    new WatchEvent.Kind[] { ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY },
                    new WatchEvent.Modifier[0]);

            keys.put(dirKey, dir);

            registerListener.accept(dir);
        }
        catch(Exception e) {
            if (errorListener != null) {
                safeRun(() -> errorListener.accept(dir, e));
            }
        }
    }

    private static void safeRun(final Runnable r) {
        try {
            if (r != null) r.run();
        }
        catch(Exception e) { }
    }


    private final WatchService ws;
    private final Map<WatchKey,Path> keys = new HashMap<>();
    private final BiConsumer<Path,Exception> errorListener;
    private final Consumer<Path> registerListener;
}
