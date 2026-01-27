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
package com.github.jlangch.venice.util.ipc.impl.conn;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.util.ipc.Authenticator;
import com.github.jlangch.venice.util.ipc.IMessage;
import com.github.jlangch.venice.util.ipc.impl.Message;
import com.github.jlangch.venice.util.ipc.impl.ServerStatistics;
import com.github.jlangch.venice.util.ipc.impl.queue.IpcQueue;
import com.github.jlangch.venice.util.ipc.impl.util.Compressor;
import com.github.jlangch.venice.util.ipc.impl.util.ServerLogger;
import com.github.jlangch.venice.util.ipc.impl.wal.WalQueueManager;


public class ServerContext {

    public ServerContext(
            final Authenticator authenticator,
            final ServerLogger logger,
            final Function<IMessage,IMessage> handler,
            final Compressor compressor,
            final WalQueueManager wal,
            final Subscriptions subscriptions,
            final int publishQueueCapacity,
            final Map<String, IpcQueue<Message>> p2pQueues,
            final ServerStatistics statistics,
            final Supplier<VncMap> serverThreadPoolStatistics
    ) {
        this.authenticator = authenticator;
        this.logger = logger;
        this.handler = handler;
        this.compressor = compressor;
        this.wal = wal;
        this.subscriptions = subscriptions;
        this.publishQueueCapacity = publishQueueCapacity;
        this.statistics = statistics;
        this.serverThreadPoolStatistics = serverThreadPoolStatistics;
        this.p2pQueues = p2pQueues;
    }


    public final Authenticator authenticator;
    public final ServerLogger logger;
    public final Function<IMessage,IMessage> handler;
    public final Compressor compressor;
    public final WalQueueManager wal;
    public final Subscriptions subscriptions;
    public final int publishQueueCapacity;
    public final Map<String, IpcQueue<Message>> p2pQueues;
    public final ServerStatistics statistics;
    public final Supplier<VncMap> serverThreadPoolStatistics;
}
