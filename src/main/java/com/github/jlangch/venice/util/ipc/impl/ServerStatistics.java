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
package com.github.jlangch.venice.util.ipc.impl;

import java.util.concurrent.atomic.AtomicLong;


public class ServerStatistics {

    public ServerStatistics() {
        this.connectionCount = new AtomicLong(0L);
        this.messageCount = new AtomicLong(0L);
        this.publishCount = new AtomicLong(0L);
        this.discardedPublishCount = new AtomicLong(0L);
        this.discardedResponseCount = new AtomicLong(0L);
    }


    public void clear() {
        connectionCount.set(0L);
        messageCount.set(0L);
        publishCount.set(0L);
        discardedPublishCount.set(0L);
        discardedResponseCount.set(0L);
    }

    public void incrementConnectionCount() {
        connectionCount.incrementAndGet();
    }

    public void decrementConnectionCount() {
        connectionCount.decrementAndGet();
    }

    public void incrementMessageCount() {
        messageCount.incrementAndGet();
    }

    public void incrementPublishCount() {
        publishCount.incrementAndGet();
    }

    public void incrementDiscardedPublishCount() {
        discardedPublishCount.incrementAndGet();
    }

    public void incrementDiscardedResponseCount() {
        discardedResponseCount.incrementAndGet();
    }


    public long getConnectionCount() {
        return connectionCount.get();
    }

    public long getMessageCount() {
        return messageCount.get();
    }

    public long getPublishCount() {
        return publishCount.get();
    }

    public long getDiscardedPublishCount() {
        return discardedPublishCount.get();
    }

    public long getDiscardedResponseCount() {
        return discardedResponseCount.get();
    }


    private final AtomicLong connectionCount;
    private final AtomicLong messageCount;
    private final AtomicLong publishCount;
    private final AtomicLong discardedPublishCount;
    private final AtomicLong discardedResponseCount;
}
