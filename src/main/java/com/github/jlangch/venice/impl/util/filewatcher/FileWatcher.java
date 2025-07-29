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
package com.github.jlangch.venice.impl.util.filewatcher;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.Closeable;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;



public class FileWatcher implements Closeable {

    public FileWatcher(
            final Path mainDir,
            final boolean registerAllSubDirs,
            final Consumer<FileWatchFileEvent> eventListener,
            final Consumer<FileWatchRegisterEvent> registerListener,
            final Consumer<FileWatchErrorEvent> errorListener,
            final Consumer<FileWatchTerminationEvent> terminationListener
    ) {
        if (mainDir == null) {
            throw new IllegalArgumentException("The mainDir must not be null!");
        }
        if (!mainDir.toFile().isDirectory()) {
            throw new RuntimeException("The main dir " + mainDir + " does not exist or is not a directory");
        }

        this.mainDir = mainDir.toAbsolutePath().normalize();
        this.eventListener = eventListener;
        this.registerListener = registerListener;
        this.errorListener = errorListener;
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

    public void start() {
        try {
            startService();
        }
        catch(Exception ex) {
            throw new RuntimeException("Failed to start FileWatcher");
        }
    }

    public void register(final Path dir) {
        if (dir == null) {
            throw new IllegalArgumentException("A dir must not be null!");
        }
        if (!dir.toFile().isDirectory()) {
            throw new IllegalArgumentException("Folder " + dir + " is not a directory");
        }

        try {
            final Path normalizedDir = dir.toAbsolutePath().normalize();

            final WatchKey dirKey = normalizedDir.register(
                                        ws,
                                        ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

            keys.put(dirKey, normalizedDir);

            if (registerListener != null) {
                safeRun(() -> registerListener.accept(new FileWatchRegisterEvent(normalizedDir)));
            }
        }
        catch(Exception e) {
            if (errorListener != null) {
                safeRun(() -> errorListener.accept(new FileWatchErrorEvent(dir, e)));
            }
        }
    }

    public List<Path> getRegisteredPaths() {
        return keys.values().stream().sorted().collect(Collectors.toList());
    }

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
                safeRun(() -> terminationListener.accept(
                                  new FileWatchTerminationEvent(mainDir)));
            }
        }
    }

    private void startService() {
        final Runnable runnable = () -> {
            while (!closed.get()) {
                try {
                    final WatchKey key = ws.poll(1L, TimeUnit.SECONDS);
                    if (key == null) {
                        continue;
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

                           if (absPath.toFile().isDirectory()
                               && e.kind() == ENTRY_CREATE
                           ) {
                               // register the new subdir
                               register(absPath);
                           }

                           final FileWatchFileEventType type = convertEvent(e.kind());
                           if (type != null) {
                               safeRun(() -> eventListener.accept(
                                                new FileWatchFileEvent(absPath, type)));
                           }
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
                        safeRun(() -> errorListener.accept(
                                          new FileWatchErrorEvent(mainDir, ex)));
                    }
                    // continue watching
                }
            }

            close();
        };

        final Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.setName("vebice-filewatcher-" + threadCounter.getAndIncrement());
        thread.start();
    }

    private FileWatchFileEventType convertEvent(final WatchEvent.Kind<?> kind) {
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


    private void safeRun(final Runnable r) {
        try {
            r.run();
        }
        catch(Exception e) { }
    }


    private static final AtomicLong threadCounter = new AtomicLong(1L);

    private final AtomicBoolean closed = new AtomicBoolean(false);

    private final Path mainDir;
    private final WatchService ws;
    private final Map<WatchKey,Path> keys = new HashMap<>();
    private final Consumer<FileWatchFileEvent> eventListener;
    private final Consumer<FileWatchRegisterEvent> registerListener;
    private final Consumer<FileWatchErrorEvent> errorListener;
    private final Consumer<FileWatchTerminationEvent> terminationListener;
}
