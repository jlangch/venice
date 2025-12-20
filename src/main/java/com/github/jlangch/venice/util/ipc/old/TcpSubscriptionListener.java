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
package com.github.jlangch.venice.util.ipc.old;

import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.github.jlangch.venice.util.ipc.IMessage;
import com.github.jlangch.venice.util.ipc.impl.Message;
import com.github.jlangch.venice.util.ipc.impl.protocol.Protocol;
import com.github.jlangch.venice.util.ipc.impl.util.Compressor;
import com.github.jlangch.venice.util.ipc.impl.util.Encryptor;


public class TcpSubscriptionListener implements Runnable {

    public TcpSubscriptionListener(
            final SocketChannel ch,
            final Consumer<IMessage> handler,
            final Compressor compressor,
            final Encryptor encryptor
    ) {
        Objects.requireNonNull(ch);
        Objects.requireNonNull(handler);
        Objects.requireNonNull(compressor);
        Objects.requireNonNull(encryptor);

        this.ch = ch;
        this.handler = handler;
        this.compressor = compressor;
        this.encryptor = encryptor;
    }

    public boolean isRunning() {
        return running.get();
    }

    @Override
    public void run() {
        try {
            running.set(true);

            while(true) {
                final Message msg = Protocol.receiveMessage(ch, compressor, encryptor);
                if (msg != null) {
                   try {
                       handler.accept(msg);
                   }
                   catch(Exception ignore) { }
                }
            }
        }
        catch(Exception ex) {
            // -> quit this subscription listener thread
        }
        finally {
            running.set(false);
        }
    }


    private final SocketChannel ch;
    private final Consumer<IMessage> handler;
    private final Compressor compressor;
    private final Encryptor encryptor;
    private final AtomicBoolean running = new AtomicBoolean(false);
}
