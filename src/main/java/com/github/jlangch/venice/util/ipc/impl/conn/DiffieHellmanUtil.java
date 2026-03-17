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

import java.nio.charset.Charset;
import java.security.PrivateKey;

import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.util.crypt.RSA;
import com.github.jlangch.venice.util.ipc.IpcException;
import com.github.jlangch.venice.util.ipc.MessageType;
import com.github.jlangch.venice.util.ipc.ResponseStatus;
import com.github.jlangch.venice.util.ipc.impl.Message;
import com.github.jlangch.venice.util.ipc.impl.Messages;
import com.github.jlangch.venice.util.ipc.impl.util.JsonBuilder;


public abstract class DiffieHellmanUtil {

    public static String getExchangeKey(final Message m) {
        return StringUtil.trimToNull(
                getString((VncMap)m.getVeniceData(), "key"));
    }

    public static String getSignature(final Message m) {
        return StringUtil.trimToNull(
                getString((VncMap)m.getVeniceData(), "signature"));
    }

    public static Message createDiffieHellmanRequestMessage(
            final String key,
            final PrivateKey rsaSigningKey
    ) throws Exception {
        return new Message(
                null,
                MessageType.DIFFIE_HELLMAN_KEY_REQUEST,
                ResponseStatus.NULL,
                false,
                false,
                false,
                Messages.EXPIRES_NEVER,
                "",
                "application/json",
                "UTF-8",
                toBytes(buildSignedKeyJsonPayload(key, rsaSigningKey), "UTF-8"));
    }

    public static Message createDiffieHellmanResponseMessage(
            final Message request,
            final String key,
            final PrivateKey rsaSigningKey
    ) throws Exception {
        return new Message(
                request.getRequestId(),
                MessageType.DIFFIE_HELLMAN_KEY_REQUEST,
                ResponseStatus.DIFFIE_HELLMAN_ACK,
                false,
                false,
                false,
                Messages.EXPIRES_NEVER,
                "",
                "application/json",
                "UTF-8",
                toBytes(buildSignedKeyJsonPayload(key, rsaSigningKey), "UTF-8"));
    }

    public static Message createDiffieHellmanResponseErrorMessage(
            final Message request,
            final String errMsg
    ) {
        return new Message(
                request.getRequestId(),
                MessageType.DIFFIE_HELLMAN_KEY_REQUEST,
                ResponseStatus.DIFFIE_HELLMAN_NAK,
                false,
                false,
                false,
                Messages.EXPIRES_NEVER,
                "",
                "text/plain",
                "UTF-8",
                toBytes(errMsg, "UTF-8"));
    }

    private static String buildSignedKeyJsonPayload(
            final String key,
            final PrivateKey rsaSigningKey
    ) {
        return new JsonBuilder()
                .add("key", key)
                .add("signature", sign(key, rsaSigningKey))
                .toJson(false);
    }

    private static String sign(
            final String key,
            final PrivateKey rsaSigningKey
    ) {
        if (rsaSigningKey == null) {
            return "";
        }
        else {
            try {
                return RSA.sign(key, rsaSigningKey);
            }
            catch(Exception ex) {
                throw new IpcException("Failed to sign Diffie-Hellman key", ex);
            }
        }
    }

    private static String getString(final VncMap map, final String entryName) {
        final VncVal v =  map.get(new VncKeyword(entryName));
        return v == Constants.Nil ? null : Coerce.toVncString(v).getValue();
    }

    private static byte[] toBytes(final String s, final String charset) {
        return s.getBytes(Charset.forName(charset));
    }
}
