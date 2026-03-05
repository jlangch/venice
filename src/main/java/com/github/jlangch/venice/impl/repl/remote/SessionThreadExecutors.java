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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


public class SessionThreadExecutors {

    public SessionThreadExecutors(final long timeoutMinutes) {
        this.timeoutMillils = TimeUnit.MINUTES.toMillis(timeoutMinutes);

        // Garbage collect timeouted sessions
        this.gcThread = new Thread(() -> { while (!stop) { gc(); sleep(INTERVAL); } },
                                   "venice-repl-server-gc");
        this.gcThread.setDaemon(true);
        this.gcThread.start();
    }

    public SessionThreadExecutor getForSession(
            final String sessionId,
            final Runnable onInitSession
    ) {
        if (isInvalidatedSession(sessionId)) {
            throw new RuntimeException("The remote REPL session has been invalidated!");
        }
        else {
            final SessionThreadExecutor e = executors.computeIfAbsent(
                                                sessionId,
                                                id -> new SessionThreadExecutor(onInitSession));

            if (e.isRunning()) {
               return e;
            }
            else {
                throw new RuntimeException("The remote REPL session has been invalidated!");
            }
        }
    }

    public void shutdown() {
        if (!stop) {
            stop = true;

            for(SessionThreadExecutor e : executors.values()) {
                if (e.isRunning()) {
                     try { e.shutdown(); } catch(Exception ignore) {}
                }
            }

            executors.clear();
        }
    }

    public int getSessionCount() {
        return executors.size();
    }

    private void gc() {
        final List<String> removableSessions = new ArrayList<>();
        for(String sessionId : executors.keySet()) {
            final SessionThreadExecutor e = executors.get(sessionId);
            if (e == null) {
                continue;
            }
            else if (e.isRunning()) {
                if (isTimeout(e)) {
                    removableSessions.add(sessionId);
                    try { e.shutdown(); } catch(Exception ignore) {}
               }
            }
            else {
                removableSessions.add(sessionId);
            }
        }

        removableSessions.forEach(id -> invalidateSession(id));
    }

    private boolean isInvalidatedSession(final String sessionId) {
        return invalidatedSessions.containsKey(sessionId);
    }

    private void invalidateSession(final String sessionId) {
        invalidatedSessions.put(sessionId, "");
        executors.remove(sessionId);
    }

    private boolean isTimeout(final SessionThreadExecutor e) {
        return e.lastUsedTime() + timeoutMillils < System.currentTimeMillis();
    }

    private static void sleep(final long millis) {
        try { Thread.sleep(millis); } catch(Exception ignore) {};
    }


    private static long INTERVAL = TimeUnit.SECONDS.toMillis(60);

    private volatile boolean stop = false;

    private final long timeoutMillils;
    private final Thread gcThread;
    private final ConcurrentHashMap<String, SessionThreadExecutor> executors = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> invalidatedSessions = new ConcurrentHashMap<>();
}
