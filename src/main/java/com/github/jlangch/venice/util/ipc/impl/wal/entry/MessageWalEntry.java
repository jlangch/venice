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
package com.github.jlangch.venice.util.ipc.impl.wal.entry;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.channels.ByteChannel;
import java.util.UUID;

import com.github.jlangch.venice.util.ipc.WriteAheadLogException;
import com.github.jlangch.venice.util.ipc.impl.Message;
import com.github.jlangch.venice.util.ipc.impl.protocol.ByteArrayStreamChannel;
import com.github.jlangch.venice.util.ipc.impl.protocol.Protocol;
import com.github.jlangch.venice.util.ipc.impl.util.Compressor;
import com.github.jlangch.venice.util.ipc.impl.util.Encryptor;


/**
 * WalEntry serializer/deserializer for IPC message WAL data entries
 */
public class MessageWalEntry {

    public MessageWalEntry(final Message message) {
        if (message == null) {
            throw new IllegalArgumentException("message must not be null");
        }

        this.message = message;
    }


    public Message getMessage() {
        return message;
    }

    public WalEntry toWalEntry() {
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream();
             final ByteChannel ch = new ByteArrayStreamChannel(out)
        ) {
            Protocol.sendMessage(ch, message, Compressor.off(), Encryptor.off());

            final UUID uuid = message.getId();
            final byte[] payload = out.toByteArray();

            return new WalEntry(WalEntryType.DATA, uuid, message.getExpiresAt(), payload);
        }
        catch(Exception ex) {
            throw new WriteAheadLogException("Failed to serialize Message to WalEntry", ex);
        }
    }

    public static MessageWalEntry fromWalEntry(final WalEntry entry) {
        final byte[] payload = entry.getPayload();

        try (final ByteArrayInputStream in = new ByteArrayInputStream(payload);
             final ByteChannel ch = new ByteArrayStreamChannel(in)
        ) {
            final Message message = Protocol.receiveMessage(ch, Compressor.off(), Encryptor.off());
            return new MessageWalEntry(message);
        }
        catch(Exception ex) {
            throw new WriteAheadLogException("Failed to deserialize WalEntry to Message", ex);
        }
    }


    private final Message message;
}
