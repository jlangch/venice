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

import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.thread.ThreadBridge;
import com.github.jlangch.venice.impl.threadpool.GlobalThreadFactory;
import com.github.jlangch.venice.impl.util.callstack.CallFrame;
import com.github.jlangch.venice.impl.util.filewatcher.events.FileWatchErrorEvent;
import com.github.jlangch.venice.impl.util.filewatcher.events.FileWatchFileEvent;
import com.github.jlangch.venice.impl.util.filewatcher.events.FileWatchFileEventType;
import com.github.jlangch.venice.impl.util.filewatcher.events.FileWatchTerminationEvent;


/**
 * A FileWatcher based on the Java WatchService
 */
public class FileWatcher_JavaWatchService implements IFileWatcher {

    public FileWatcher_JavaWatchService(
            final Path mainDir,
            final boolean registerAllSubDirs,
            final Consumer<FileWatchFileEvent> fileListener,
            final Consumer<FileWatchErrorEvent> errorListener,
            final Consumer<FileWatchTerminationEvent> terminationListener
    ) {
        if (mainDir == null) {
            throw new IllegalArgumentException("The mainDir must not be null!");
        }
        if (!Files.isDirectory(mainDir)) {
            throw new RuntimeException("The main dir " + mainDir + " does not exist or is not a directory");
        }

        this.mainDir = mainDir.toAbsolutePath().normalize();
        this.fileListener = fileListener;
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

    @Override
    public Path getMainDir() {
        return mainDir;
    }

    @Override
    public void start(final CallFrame[] callFrame) {
        if (callFrame == null) {
            throw new IllegalArgumentException("The callFrame array must not be null!");
        }

        if (status.compareAndSet(
                FileWatcherStatus.CREATED,
                FileWatcherStatus.INITIALISING)
        ) {
            try {
                startService(callFrame);
            }
            catch(Exception ex) {
                throw new RuntimeException(
                        "Rejected to start the FileWatcher in status " + status.get());
            }
        }
    }

    @Override
    public List<Path> getRegisteredPaths() {
        return keys.values().stream().sorted().collect(Collectors.toList());
    }

    @Override
    public FileWatcherStatus getStatus() {
        return status.get();
    }

    @Override
    public void close() {
        if (status.compareAndSet(
                FileWatcherStatus.RUNNING,
                FileWatcherStatus.CLOSED)
        ) {
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
        else {
            throw new RuntimeException(
                    "Rejected to close the FileWatcher in status " + status.get());
        }
    }


    private void register(final Path dir) {
        try {
            final WatchKey dirKey = dir.register(
                                      ws,
                                      ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

            keys.put(dirKey, dir);
        }
        catch(Exception e) {
            if (errorListener != null) {
                safeRun(() -> errorListener.accept(new FileWatchErrorEvent(dir, e)));
            }
        }
    }

    private void startService(final CallFrame[] callFrame) {
        try {
            // Create a wrapper that inherits the Venice thread context
            // from the parent thread to the executer thread!
            final ThreadBridge threadBridge = ThreadBridge.create("thread", callFrame);

            final Runnable runnable = threadBridge.bridgeRunnable(createWorker());

            final Thread th = GlobalThreadFactory.newThread("venice-watch-dir", runnable);
            th.setUncaughtExceptionHandler(ThreadBridge::handleUncaughtException);
            th.start();

            // spin wait max 5s for service to be ready or closed
            final long ts = System.currentTimeMillis();
            while(System.currentTimeMillis() < ts + 5_000) {
                if (status.get() == FileWatcherStatus.RUNNING) break;
                if (status.get() == FileWatcherStatus.CLOSED) break;
                try { Thread.sleep(100); } catch(Exception ex) {}
            }
        }
        catch(Exception ex) {
            status.set(FileWatcherStatus.CLOSED);
            throw new RuntimeException("Failed to start FileWatcher!", ex);
        }
    }

    private Runnable createWorker() {
        return () -> {
            status.set(FileWatcherStatus.RUNNING);

            while (status.get() == FileWatcherStatus.RUNNING) {
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
                           final FileWatchFileEventType eventType = convertToEventType(e.kind());
                           if (Files.isDirectory(absPath)) {
                               if (eventType == FileWatchFileEventType.CREATED) {
                                   register(absPath);  // register the new subdir
                               }

                               if (eventType != FileWatchFileEventType.MODIFIED) {
                                   safeRun(() -> fileListener.accept(
                                                   new FileWatchFileEvent(absPath, true, false, eventType)));
                               }
                           }
                           else if (Files.isRegularFile(absPath)) {
                               safeRun(() -> fileListener.accept(
                                               new FileWatchFileEvent(absPath, false, true, eventType)));
                           }
                           else {
                               // if the file has been deleted its type cannot be checked
                               safeRun(() -> fileListener.accept(
                                               new FileWatchFileEvent(absPath, false, false, eventType)));
                           }});

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
                    // continue
                }
            }

            if (status.get() != FileWatcherStatus.CLOSED) {
                close();
            }
        };
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


    private final AtomicReference<FileWatcherStatus> status = new AtomicReference<>(FileWatcherStatus.CREATED);

    private final Path mainDir;
    private final WatchService ws;
    private final Map<WatchKey,Path> keys = new HashMap<>();
    private final Consumer<FileWatchFileEvent> fileListener;
    private final Consumer<FileWatchErrorEvent> errorListener;
    private final Consumer<FileWatchTerminationEvent> terminationListener;
}
