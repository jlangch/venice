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
package com.github.jlangch.venice.util.ipc.impl.protocol;

import java.nio.ByteBuffer;

import com.github.jlangch.venice.util.ipc.IpcException;


public class Header {

    public Header(
            final int version,
            final boolean compressed,
            final boolean encrypted,
            final int payloadMetaSize,
            final int payloadDataSize

    ) {
        this.version = version;
        this.compressed = compressed;
        this.encrypted = encrypted;
        this.payloadMetaSize = payloadMetaSize;
        this.payloadDataSize = payloadDataSize;
    }


    public static void write(final Header header, final ByteBuffer buf) {
        buf.put(MAGIC_1);
        buf.put(MAGIC_2);
        buf.putInt(header.version);
        buf.put(toByte(header.compressed));
        buf.put(toByte(header.encrypted));
        buf.putInt(header.payloadMetaSize);
        buf.putInt(header.payloadDataSize);
    }

    public static Header read(final ByteBuffer buf) {
        // parse header (magic bytes)
        final byte magic1 = buf.get();
        final byte magic2 = buf.get();

        if (magic1 != MAGIC_1 || magic2 != MAGIC_2) {
            throw new IpcException(
                    "Received unknown message (bad magic bytes)!");
        }

        // parse header
        return new Header(
                buf.getInt(),         // version
                toBool(buf.get()),    // compressed
                toBool(buf.get()),    // encrypted
                buf.getInt(),         // payloadMetaSize
                buf.getInt());        // payloadDataSize
    }

    public static byte[] aadData(final Header header) {
        final byte[] addData = new byte[2];
        addData[0] = toByte(header.isCompressed());
        addData[1] = toByte(header.isEncrypted());
        return addData;
    }

    public static byte[] aadData(
            final boolean compressed,
            final boolean encrypted
    ) {
        final byte[] addData = new byte[2];
        addData[0] = toByte(compressed);
        addData[1] = toByte(encrypted);
        return addData;
    }



    public int getVersion() {
        return version;
    }

    public boolean isCompressed() {
        return compressed;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public int getPayloadMetaSize() {
        return payloadMetaSize;
    }

    public int getPayloadDataSize() {
        return payloadDataSize;
    }


    private static boolean toBool(final byte n) {
        if (n == 0) return false;
        else if (n == 1) return true;
        else throw new IpcException("Illegal IPC message boolean value");
    }

    private static byte toByte(final boolean b) {
        return b ? (byte)1 : (byte)0;
    }


    public static int SIZE = 16;

    private static byte MAGIC_1 = (byte)'v';
    private static byte MAGIC_2 = (byte)'n';

    private final int version;
    private final boolean compressed;
    private final boolean encrypted;
    private final int payloadMetaSize;
    private final int payloadDataSize;
}
