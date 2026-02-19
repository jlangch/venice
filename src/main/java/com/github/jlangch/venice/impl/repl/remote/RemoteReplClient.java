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

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.util.ipc.Client;
import com.github.jlangch.venice.util.ipc.ClientConfig;
import com.github.jlangch.venice.util.ipc.IMessage;


public class RemoteReplClient implements AutoCloseable  {

    public RemoteReplClient(
            final String host,
            final int port,
            final String principal,
            final String password
    ) {
        this.ipcClient = createIpcClient(host, port, principal, password);
    }


    public FormResult eval(final String form) {
        final IMessage m = null;

        final IMessage result = ipcClient.sendMessage(m, "func/repl");

        return new FormResult(0, null, null, null, null, null, 0);
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
                    "Failed to start Venice REPL server. The password must not be empty!");
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


    private final Client ipcClient;
    private final AtomicBoolean stop = new AtomicBoolean(false);
}
