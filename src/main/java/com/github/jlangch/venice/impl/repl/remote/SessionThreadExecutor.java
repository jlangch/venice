/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2026 Venice
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
package com.github.jlangch.venice.impl.repl.remote;

import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;


public class SessionThreadExecutor {

    public SessionThreadExecutor(final Runnable onInit) {
        this.lastUsedTime = System.currentTimeMillis();
        this.worker = new Thread(() -> worker(onInit), "venice-repl-server-worker");
        worker.start();
    }

    public <T> Future<T> submit(final Callable<T> task) {
        if (task == null) throw new NullPointerException();
        final CompletableFuture<T> future = new CompletableFuture<>();
        tasks.add(new Job<T>(future, task));
        return future;
    }

    public boolean isRunning() {
        return !stop;
    }

    public void shutdown() {
        if (!stop) {
            stop = true;
            try { worker.interrupt(); } catch(Exception ignore) {};
        }
    }

    public long lastUsedTime() {
        return lastUsedTime;
    }


    private void worker(final Runnable onInit) {
        try {
            if (onInit != null) {
                onInit.run();  // run the session initializer
            }

            while (!stop) {
                final Runnable job = tasks.poll();
                if (job != null) {
                    lastUsedTime = System.currentTimeMillis();
                    job.run();
                }
                else {
                    sleep(5);
                }
            }
        }
        finally {
            stop = true;
        }
    }


    private static void sleep(final long millis) {
        try { Thread.sleep(millis); } catch(Exception ignore) {};
    }

    private static class Job<T> implements Runnable {
        public Job(
                final CompletableFuture<T> future,
                final Callable<T> task
        ) {
            this.future = future;
            this.task = task;
        }

        @Override
        public void run() {
            try {
                future.complete(task.call());
            }
            catch(Exception ex) {
                future.completeExceptionally(ex);
            }
        }

        final CompletableFuture<T> future;
        final Callable<T> task;
    }


    private volatile boolean stop = false;
    private volatile long lastUsedTime;

    private final Thread worker;
    private final Queue<Job<?>> tasks = new LinkedBlockingDeque<>();
}
