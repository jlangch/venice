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

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.thread.ThreadBridge;
import com.github.jlangch.venice.impl.threadpool.GlobalThreadFactory;
import com.github.jlangch.venice.impl.util.callstack.CallFrame;
import com.github.jlangch.venice.impl.util.filewatcher.FileWatchFileEventType;


public class FileWatcher_Linux implements IFileWatcher {

    public FileWatcher_Linux(
            final Path mainDir,
            final boolean registerAllSubDirs,
            final BiConsumer<Path,FileWatchFileEventType> eventListener,
            final BiConsumer<Path,Exception> errorListener,
            final Consumer<Path> terminationListener,
            final Consumer<Path> registerListener
    ) throws IOException {
        if (mainDir == null) {
            throw new IllegalArgumentException("The mainDir must not be null!");
        }
        if (!Files.isDirectory(mainDir)) {
            throw new RuntimeException("The main dir " + mainDir + " does not exist or is not a directory");
        }

        this.mainDir = mainDir.toAbsolutePath().normalize();
        this.eventListener = eventListener;
        this.errorListener = errorListener;
        this.registerListener = registerListener;
        this.terminationListener = terminationListener;

        try {
            this.ws = mainDir.getFileSystem().newWatchService();

            if (registerAllSubDirs) {
                Files.walk(mainDir)
                     .filter(Files::isDirectory)
                     .forEach(this::register);
            }
            else {
                register(mainDir);
            }
        }
        catch(Exception ex) {
            throw new RuntimeException("Failed to create FileWatcher!", ex);
        }
    }

    @Override
    public Path getMainDir() {
        return mainDir;
    }

    @Override
    public void start(final CallFrame[] callFrame) {
        if (callFrame == null) {
            throw new IllegalArgumentException("The callFrame array must not be null!");
        }

        try {
            startService(callFrame);
        }
        catch(Exception ex) {
            throw new RuntimeException("Failed to start FileWatcher");
        }
    }

    @Override
   public void register(final Path dir) {
        if (!Files.isDirectory(dir)) {
            throw new RuntimeException("The path " + dir + " does not exist or is not a directory");
        }

        final Path normalizedDir = dir.toAbsolutePath().normalize();

        register(normalizedDir, false);
    }

    @Override
    public List<Path> getRegisteredPaths() {
        return keys.values().stream().sorted().collect(Collectors.toList());
    }

    @Override
    public boolean isRunning() {
        return !closed.get();
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            try {
                ws.close();
            }
            catch(Exception ex) {
                throw new RuntimeException("Failed to close FileWatcher!", ex);
            }

            if (terminationListener != null) {
                safeRun(() -> terminationListener.accept(mainDir));
            }
        }
    }


    private void register(final Path dir, final boolean sendEvent) {
        try {
            final WatchKey dirKey = dir.register(
                                      ws,
                                      ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

            keys.put(dirKey, dir);

            if (sendEvent && registerListener != null) {
                safeRun(() -> registerListener.accept(dir));
            }
        }
        catch(Exception e) {
            if (errorListener != null) {
                safeRun(() -> errorListener.accept(dir, e));
            }
        }
    }

    private void startService(final CallFrame[] callFrame) {
        // Create a wrapper that inherits the Venice thread context
        // from the parent thread to the executer thread!
        final ThreadBridge threadBridge = ThreadBridge.create("thread", callFrame);

        final Runnable runnable = threadBridge.bridgeRunnable(
            () -> {
                while (!closed.get()) {
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
                           .filter(e -> e.kind() != OVERFLOW)
                           .forEach(e -> {
                               @SuppressWarnings("unchecked")
                               final Path p = ((WatchEvent<Path>)e).context();
                               final Path absPath = dirPath.resolve(p);
                               if (absPath.toFile().isDirectory() && e.kind() == ENTRY_CREATE) {
                                   // register the new subdir
                                   register(absPath, true);
                               }
                               safeRun(() -> eventListener.accept(
                                                 absPath,
                                                 convertToEventType(e.kind())));
                             });

                        key.reset();
                    }
                    catch(ClosedWatchServiceException ex) {
                        break; // stop watching
                    }
                    catch(InterruptedException ex) {
                        break; // stop watching
                    }
                    catch(Exception ex) {
                        if (errorListener != null) {
                            safeRun(() -> errorListener.accept(mainDir, ex));
                        }
                        // continue
                    }
                }

                close();
            });

        final Thread th = GlobalThreadFactory.newThread("venice-watch-dir", runnable);
        th.setUncaughtExceptionHandler(ThreadBridge::handleUncaughtException);
        th.start();
    }

    private static void safeRun(final Runnable r) {
        try {
            r.run();
        }
        catch(Exception e) { }
    }

    private FileWatchFileEventType convertToEventType(final WatchEvent.Kind<?> kind) {
        if (kind == null) {
            return null;
        }
        else {
            switch(kind.name()) {
                case "ENTRY_CREATE": return FileWatchFileEventType.CREATED;
                case "ENTRY_DELETE": return FileWatchFileEventType.DELETED;
                case "ENTRY_MODIFY": return FileWatchFileEventType.MODIFIED;
                case "OVERFLOW":     return FileWatchFileEventType.OVERFLOW;
                default:             return null;
            }
        }
    }


    private final AtomicBoolean closed = new AtomicBoolean(false);

    private final Path mainDir;
    private final WatchService ws;
    private final Map<WatchKey,Path> keys = new HashMap<>();
    private final BiConsumer<Path,FileWatchFileEventType> eventListener;
    private final BiConsumer<Path,Exception> errorListener;
    private final Consumer<Path> registerListener;
    private final Consumer<Path> terminationListener;
}
