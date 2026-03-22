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

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.io.IOException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.util.ipc.Client;
import com.github.jlangch.venice.util.ipc.ClientConfig;
import com.github.jlangch.venice.util.ipc.IMessage;
import com.github.jlangch.venice.util.ipc.MessageFactory;
import com.github.jlangch.venice.util.ipc.ResponseStatus;


public class RemoteReplClient implements AutoCloseable  {

    public RemoteReplClient(
            final ReplClientConfig replConfig
    ) {
        final String uuid = UUID.randomUUID().toString();

        this.sessionId = uuid;
        this.ipcClient = createIpcClient(
                            replConfig,
                            RemoteRepl.PRINCIPAL, uuid);
    }


    public FormResult eval(final String form) {
        Objects.requireNonNull(form);

        final IMessage m = MessageFactory.venice(
                                sessionId,
                                // String.valueOf(requestId.incrementAndGet()),
                                "eval",
                                createDataMap("form", form));

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
                try { sendSessionClose(ipcClient, sessionId); } catch(Exception ignore) {}
                ipcClient.close();
            }
        }
    }


    private boolean isStop() {
        return stop.get();
    }

    private VncHashMap createDataMap(
            final String key,
            final String value
    ) {
        Objects.requireNonNull(key);

        return VncHashMap.of(
                new VncKeyword(key),
                value == null ? Nil : new VncString(value));
    }

    private VncHashMap createDataMap(
            final String key1,
            final String val1,
            final String key2,
            final String val2
    ) {
        Objects.requireNonNull(key1);
        Objects.requireNonNull(key2);

        return VncHashMap.of(
                new VncKeyword(key1),
                val1 == null ? Nil : new VncString(val1),
                new VncKeyword(key2),
                val2 == null ? Nil : new VncString(val2));
    }


    private static Client createIpcClient(
            final ReplClientConfig replConfig,
            final String principal,
            final String sessionId
    ) {
        if (replConfig.getPort() <= 0 || replConfig.getPort() > 65536) {
            throw new VncException(
                "Failed to start Venice REPL client. "
                + "The port (" + replConfig.getPort() + ") must be in the range [0..65536]! ");
        }
        if (StringUtil.isEmpty(principal)) {
            throw new VncException(
                    "Failed to start Venice REPL client. The principal must not be empty!");
        }
        if (StringUtil.isEmpty(replConfig.getPassword())) {
            throw new VncException(
                    "Failed to start Venice REPL client. The password must not be empty!");
        }

        // Load the keys from the PEM files. The keys may be null!
        final KeyPair keyPair = RsaKeyUtil.createKeyPair(
                                    RsaKeyUtil.loadPublicKey(replConfig.getClientPublicKeyFile()),
                                    RsaKeyUtil.loadPrivateKey(replConfig.getClientPrivateKeyFile()));
        final PublicKey serverPublicKey = RsaKeyUtil.loadPublicKey(replConfig.getServerPublicKeyFile());

        if ((keyPair == null && serverPublicKey != null)
                || (keyPair != null && serverPublicKey == null)
        ) {
            throw new VncException(
                    "Failed to start Venice REPL server. "
                    + "Either pass both a key pair with private/public key and a "
                    + "server public key or none of them");
        }


        try {
            final ClientConfig config = ClientConfig
                                            .builder()
                                            .conn(replConfig.getHost(), replConfig.getPort())
                                            .encrypt(true)
                                            .dhRsaSigningClientKeyPair(keyPair)
                                            .dhRsaSigningServerPublicKey(serverPublicKey)
                                            .build();

            final Client client = Client.of(config);

            client.open(principal, replConfig.getPassword());

            sendSessionInit(client, sessionId);

            return client;
        }
        catch(Exception ex) {
            throw new VncException("Failed to start Venice REPL client", ex);
        }
    }

    private static void sendSessionInit(final Client client, final String sessionId) {
        final IMessage m = MessageFactory.text(sessionId, "session-init", "text/plain", "UTF-8", "");

        final IMessage r = client.sendMessage(m, RemoteRepl.FUNCTION);
        if (r.getResponseStatus() != ResponseStatus.OK) {
            throw new VncException("REPL remote session initialization failure");
        }
    }

    private static void sendSessionClose(final Client client, final String sessionId) {
        final IMessage m = MessageFactory.text(sessionId, "session-close", "text/plain", "UTF-8", "");

        final IMessage r = client.sendMessage(m, RemoteRepl.FUNCTION);
        if (r.getResponseStatus() != ResponseStatus.OK) {
            throw new VncException("REPL remote session close failure");
        }
    }


    private final String sessionId;
    private final Client ipcClient;
    private final AtomicBoolean stop = new AtomicBoolean(false);
}
