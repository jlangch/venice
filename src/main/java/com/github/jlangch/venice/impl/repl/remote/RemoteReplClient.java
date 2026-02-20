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

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.util.ipc.Client;
import com.github.jlangch.venice.util.ipc.ClientConfig;
import com.github.jlangch.venice.util.ipc.IMessage;
import com.github.jlangch.venice.util.ipc.MessageFactory;

import joptsimple.internal.Objects;


public class RemoteReplClient implements AutoCloseable  {

    public RemoteReplClient(
            final String host,
            final int port,
            final String password
    ) {
        this.ipcClient = createIpcClient(host, port, RemoteRepl.PRINCIPAL, password);
    }


    public FormResult eval(final String form) {
        Objects.ensureNotNull(form);

        final IMessage m = MessageFactory.venice(
                                String.valueOf(requestId.incrementAndGet()),
                                "form",
                                createDataMap(form));

        final IMessage result = ipcClient.sendMessage(m, RemoteRepl.FUNCTION);

        final VncMap resultData = (VncMap)result.getVeniceData();
        return FormResult.of(resultData);
    }

    public boolean isRunning() {
        return ipcClient != null && ipcClient.isRunning() && !isStop();
    }

    @Override
    public void close() throws IOException {
        if (stop.compareAndSet(false, true)) {
            if (ipcClient != null) {
                ipcClient.close();
            }
        }
    }


    private boolean isStop() {
        return stop.get();
    }

    private Client createIpcClient(
            final String host,
            final int port,
            final String principal,
            final String password
    ) {
        if (port <= 0 || port > 65536) {
            throw new VncException(
                    "Invalid Venice REPL server port. The port (" + port + ") "
                    + "must be in the range [0..65536]!");
        }
        if (StringUtil.isEmpty(principal)) {
            throw new VncException(
                    "Failed to start Venice REPL server. The principal must not be empty!");
        }
        if (StringUtil.isEmpty(password)) {
            throw new VncException(
                    "Failed to start Venice REPL server. The password must not be empty! "
                    + "Please set environment var 'REPL_SERVER_PASSWORD' in the 'repl.env' "
                    + "file!");
        }

        try {
            final ClientConfig config = ClientConfig
                                            .builder()
                                            .conn(host, port)
                                            .encrypt(true)
                                            .build();

            final Client client = Client.of(config);

            client.open(principal, password);

            stop.set(false);

            return client;
        }
        catch(Exception ex) {
            throw new VncException("Failed to start Venice REPL client", ex);
        }
    }

    private VncHashMap createDataMap(
            final String form
    ) {
        return VncHashMap.of(
                new VncString("form"), new VncString(form));
    }


    private final Client ipcClient;
    private final AtomicBoolean stop = new AtomicBoolean(false);
    private final AtomicLong requestId = new AtomicLong(0L);
}
