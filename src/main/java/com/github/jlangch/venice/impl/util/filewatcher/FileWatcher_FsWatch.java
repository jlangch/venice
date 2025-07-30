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

import static com.github.jlangch.venice.impl.util.filewatcher.events.FileWatchFileEventType.CREATED;
import static com.github.jlangch.venice.impl.util.filewatcher.events.FileWatchFileEventType.DELETED;
import static com.github.jlangch.venice.impl.util.filewatcher.events.FileWatchFileEventType.MODIFIED;
import static com.github.jlangch.venice.impl.util.filewatcher.events.FileWatchFileEventType.OVERFLOW;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.thread.ThreadBridge;
import com.github.jlangch.venice.impl.threadpool.GlobalThreadFactory;
import com.github.jlangch.venice.impl.util.CollectionUtil;
import com.github.jlangch.venice.impl.util.callstack.CallFrame;
import com.github.jlangch.venice.impl.util.filewatcher.events.FileWatchErrorEvent;
import com.github.jlangch.venice.impl.util.filewatcher.events.FileWatchFileEvent;
import com.github.jlangch.venice.impl.util.filewatcher.events.FileWatchFileEventType;
import com.github.jlangch.venice.impl.util.filewatcher.events.FileWatchRegisterEvent;
import com.github.jlangch.venice.impl.util.filewatcher.events.FileWatchTerminationEvent;


/**
 * FileWatcher implementation based on top of the <i>fswatch</i> tool.
 *
 * <p>The Java WatchService does not work properly on MacOS. This FileWatcher
 * solves the problem on MacOS.
 *
 * <p><i>fswatch</i> is installed via Homebrew:
 *
 * <pre>
 *   brew install fswatch
 * </pre>
 *
 * @see <a href="https://github.com/emcrisostomo/fswatch/">fswatch Github</a>
 * @see <a href="https://emcrisostomo.github.io/fswatch/doc/1.17.1/fswatch.html/">fswatch Manual</a>
 * @see <a href="https://formulae.brew.sh/formula/fswatch">fswatch Installation</a>
 */
public class FileWatcher_FsWatch implements IFileWatcher {

    public FileWatcher_FsWatch(
            final Path mainDir,
            final boolean recursive,
            final Consumer<FileWatchFileEvent> eventListener,
            final Consumer<FileWatchErrorEvent> errorListener,
            final Consumer<FileWatchTerminationEvent> terminationListener,
            final Consumer<FileWatchRegisterEvent> registerListener,
            final String fswatchProgram
    ) throws IOException {
        if (mainDir == null) {
            throw new IllegalArgumentException("The mainDir must not be null!");
        }
        if (!Files.isDirectory(mainDir)) {
            throw new RuntimeException("The main dir " + mainDir + " does not exist or is not a directory");
        }
        if (fswatchProgram != null && !Files.isExecutable(Paths.get(fswatchProgram))) {
            throw new IllegalArgumentException("The fswatchProgram does not exist or is not executable!");
        }

        this.mainDir = mainDir.toAbsolutePath().normalize();
        this.recursive = recursive;
        this.eventListener = eventListener;
        this.registerListener = registerListener;
        this.errorListener = errorListener;
        this.terminationListener = terminationListener;
        this.fswatchProgram = fswatchProgram == null ? "fswatch" : fswatchProgram;
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
        throw new RuntimeException(
                "Registering additional FileWatcher directories is not support on MacOS!");
    }

    @Override
    public List<Path> getRegisteredPaths() {
        return CollectionUtil.toList(mainDir);
    }

    @Override
    public boolean isRunning() {
        return !closed.get();
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            try {
                final Process process = fswatchProcess.get();
                if (process != null && process.isAlive()) {
                    process.destroyForcibly();
                }
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


    private void startService(final CallFrame[] callFrame) {
        try {
            // fswatch options:
            // -0                  null-separated output
            // -r                  recursive
            // --fire-idle-event   fire idle events
            // --allow-overflow    allow a monitor to overflow and report it as a change event
            // --event-flags       include event flags like Created, Updated, etc

            final String formatOpt = "--format=%p" + SEPARATOR + "%f";

            final ProcessBuilder pb = recursive
                                        ? new ProcessBuilder(
                                                fswatchProgram, formatOpt, "-r", mainDir.toString())
                                        : new ProcessBuilder(
                                                fswatchProgram, formatOpt, mainDir.toString());
            pb.redirectErrorStream(true);

            fswatchProcess.set(pb.start());
        }
        catch(Exception ex) {
            throw new RuntimeException("Failed to start FileWatcher!", ex);
        }

        // Create a wrapper that inherits the Venice thread context
        // from the parent thread to the executer thread!
        final ThreadBridge threadBridge = ThreadBridge.create("thread", callFrame);

        final Runnable runnable = threadBridge.bridgeRunnable(
            () -> {
                try {
                    final Process process = fswatchProcess.get();

                    try (BufferedReader reader = new BufferedReader(
                         new InputStreamReader(process.getInputStream()))) {

                        final StringBuilder buffer = new StringBuilder();

                        int ch;
                        while ((ch = reader.read()) != -1 && !closed.get()) {
                            if (ch == '\n') { // event terminator
                                final String line = buffer.toString();
                                buffer.setLength(0);

                                if (isIdleEvent(line)) {
                                   continue;
                                }

                                // Example line: /path/to/file.txt|#|Updated Created
                                int separatorIdx = line.indexOf(SEPARATOR);
                                if (separatorIdx != -1) {
                                    final String filePath = line.substring(0, separatorIdx);
                                    final String flags = line.substring(separatorIdx + SEPARATOR.length());
                                    final Set<FileWatchFileEventType> types =  mapToEventTypes(flags);

                                    final Path path = Paths.get(filePath);

                                    final boolean isDir = flags.contains("IsDir");
                                    final boolean isFile = flags.contains("IsFile");

                                    if (isDir) {
                                        if (types.contains(CREATED)) {
                                            safeRun(() -> registerListener.accept(
                                                                new FileWatchRegisterEvent(path)));
                                        }
                                    }

                                    if (isFile || isDir) {
                                        if (types.contains(CREATED)) {
                                           safeRun(() -> eventListener.accept(
                                                            new FileWatchFileEvent(path, false, CREATED)));
                                        }
                                        else if (types.contains(MODIFIED) && isFile) {
                                            safeRun(() -> eventListener.accept(
                                                            new FileWatchFileEvent(path, false, MODIFIED)));
                                        }
                                        else if (types.contains(DELETED)) {
                                            safeRun(() -> eventListener.accept(
                                                            new FileWatchFileEvent(path, false, DELETED)));
                                        }
                                    }
                                }
                                else {
                                    // fallback in case of no flags
                                    final Path path = Paths.get(line);
                                    if (Files.isDirectory(path)) {
                                        safeRun(() -> eventListener.accept(
                                                new FileWatchFileEvent(path, true, MODIFIED)));
                                    }
                                    else if (Files.isRegularFile(path)) {
                                        safeRun(() -> eventListener.accept(
                                                new FileWatchFileEvent(path, false, MODIFIED)));
                                    }
                                }
                            }
                            else {
                                buffer.append((char) ch);
                            }
                        }
                    }
                }
                catch (Exception ex) {
                    if (errorListener != null) {
                        safeRun(() -> errorListener.accept(
                                        new FileWatchErrorEvent(mainDir, ex)));
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

    private boolean isIdleEvent(final String line) {
        return line.matches(" *NoOp *");
    }

    private Set<FileWatchFileEventType> mapToEventTypes(final String flags) {
       return Arrays.stream(flags.split(" "))
                    .map(s -> mapToEventType(s))
                    .filter(e -> e != null)
                    .collect(Collectors.toSet());
    }

    private FileWatchFileEventType mapToEventType(final String flag) {
        switch(flag) {
            // This event maps a platform-specific event that has no corresponding flag.
            case "PlatformSpecific": return null;

            // The object has been created
            case "Created": return CREATED;

            // The object has been updated. The kind of update is monitor-dependent
            case "Updated": return MODIFIED;

            // The object has been removed
            case "Removed": return DELETED;

            // The object has been renamed
            case "Renamed": return null;

            // The object’s owner has changed
            case "OwnerModified": return null;

            // An object’s attribute has changed
            case "AttributeModified": return null;

            // The object has moved from this location to a new location of the same file system
            case "MovedFrom": return null;

            // The object has moved from another location in the same file system into this location
            case "MovedTo": return null;

            // The object is a regular file
            case "IsFile": return null;

            // The object is a directory
            case "IsDir": return null;

            // The object is a symbolic link
            case "IsSymLink": return null;

            // The object link count has changed
            case "Link": return null;

            // The monitor has overflowed
            case "Overflow": return OVERFLOW;

            default: return null;
        }
    }


    private static final String SEPARATOR = "|";

    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final AtomicReference<Process> fswatchProcess = new AtomicReference<>();

    private final Path mainDir;
    private final boolean recursive;
    private final Consumer<FileWatchFileEvent> eventListener;
    private final Consumer<FileWatchRegisterEvent> registerListener;
    private final Consumer<FileWatchErrorEvent> errorListener;
    private final Consumer<FileWatchTerminationEvent> terminationListener;
    private final String fswatchProgram;
}
