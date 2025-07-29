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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.github.jlangch.venice.impl.thread.ThreadBridge;
import com.github.jlangch.venice.impl.threadpool.GlobalThreadFactory;
import com.github.jlangch.venice.impl.util.callstack.CallFrame;
import com.github.jlangch.venice.impl.util.filewatcher.FileWatchFileEventType;


public class FileWatcher_MacOS implements IFileWatcher {

    public FileWatcher_MacOS(
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
        if (!mainDir.toFile().isDirectory()) {
            throw new RuntimeException("The main dir " + mainDir + " does not exist or is not a directory");
        }

        this.mainDir = mainDir.toAbsolutePath().normalize();
        this.eventListener = eventListener;
        this.errorListener = errorListener;
        this.registerListener = registerListener;
        this.terminationListener = terminationListener;

        try {
            // TODO: implement

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
        if (!dir.toFile().exists() || !dir.toFile().isDirectory()) {
            throw new RuntimeException("Folder " + dir + " does not exist or is not a directory");
        }

        final Path normalizedDir = dir.toAbsolutePath().normalize();

        register(normalizedDir, false);
    }

    @Override
    public List<Path> getRegisteredPaths() {
        // TODO: implement
        return new ArrayList<>();
    }

    @Override
    public boolean isRunning() {
        return !closed.get();
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            try {
                // TODO: implement
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
                        // TODO: implement
                        Thread.sleep(2000);
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


    private final AtomicBoolean closed = new AtomicBoolean(false);

    private final Path mainDir;
    private final BiConsumer<Path,FileWatchFileEventType> eventListener;
    private final BiConsumer<Path,Exception> errorListener;
    private final Consumer<Path> registerListener;
    private final Consumer<Path> terminationListener;
}
