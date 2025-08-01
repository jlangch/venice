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
package com.github.jlangch.venice.impl.util.filewatcher.aviron;

import java.nio.file.Path;
import java.util.function.Consumer;

import com.github.jlangch.aviron.filewatcher.events.FileWatchErrorEvent;
import com.github.jlangch.aviron.filewatcher.events.FileWatchFileEvent;
import com.github.jlangch.aviron.filewatcher.events.FileWatchTerminationEvent;
import com.github.jlangch.venice.impl.thread.ThreadBridge;
import com.github.jlangch.venice.impl.threadpool.GlobalThreadFactory;
import com.github.jlangch.venice.impl.util.callstack.CallFrame;


public class Aviron_FileWatcher_JavaWatchService
extends com.github.jlangch.aviron.filewatcher.FileWatcher_JavaWatchService {

    public Aviron_FileWatcher_JavaWatchService(
            final CallFrame[] veniceCallFrame,
            final Path mainDir,
            final boolean recursive,
            final Consumer<FileWatchFileEvent> fileListener,
            final Consumer<FileWatchErrorEvent> errorListener,
            final Consumer<FileWatchTerminationEvent> terminationListener,
            final CallFrame[] callFrame
    ) {
       super(mainDir,
              recursive,
              fileListener,
              errorListener,
              terminationListener);

        this.callFrame = veniceCallFrame;
    }

    @Override
    public void startServiceThread(final Runnable runnable) {
        // Create a wrapper that inherits the Venice thread context
        // from the parent thread to the executer thread!
        final ThreadBridge threadBridge = ThreadBridge.create("thread", callFrame);

        final Runnable bridgedRunnable = threadBridge.bridgeRunnable(runnable);

        final Thread th = GlobalThreadFactory.newThread("venice-watch-dir", bridgedRunnable);
        th.setUncaughtExceptionHandler(ThreadBridge::handleUncaughtException);
        th.start();
    }


    private final CallFrame[] callFrame;
}
