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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Random;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.util.junit.EnableOnMac;
import com.github.jlangch.venice.util.ipc.impl.Message;
import com.github.jlangch.venice.util.ipc.impl.Messages;
import com.github.jlangch.venice.util.ipc.impl.util.Compressor;
import com.github.jlangch.venice.util.ipc.impl.util.Encryptor;


public class ProtocolTest {

    @Test
    @EnableOnMac
    public void test_snd_rcv_small() throws Exception{
        final Protocol p = new Protocol();

        final int MSG_SIZE_LIMIT = 10 * MB;

        // Payload
        final byte[] payload = new byte[] { 1, 2, 3, 4, 5 };

        final Message m = Messages.testMessage(payload, false);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream(10 * KB);
        final ByteArrayStreamChannel chSnd = new ByteArrayStreamChannel(baos);

        p.sendMessage(chSnd, m, Compressor.off(), Encryptor.off(), MSG_SIZE_LIMIT);

        chSnd.close();

        final byte[] serialisedData = baos.toByteArray();

        final ByteArrayInputStream bais = new ByteArrayInputStream(serialisedData);
        final ByteArrayStreamChannel chRcv = new ByteArrayStreamChannel(bais);


        final Message mr = p.receiveMessage(chRcv, Compressor.off(), Encryptor.off());

        assertArrayEquals(payload, mr.getData());
    }


    @Test
    @EnableOnMac
    public void test_snd_rcv_large() throws Exception{
        final Protocol p = new Protocol();

        final int MSG_SIZE_LIMIT = 10 * MB;

        // Payload
        final byte[] payload = createRandomPayload(1 * MB);

        final Message m = Messages.testMessage(payload, false);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream(10 * MB);
        final ByteArrayStreamChannel chSnd = new ByteArrayStreamChannel(baos);

        p.sendMessage(chSnd, m, Compressor.off(), Encryptor.off(), MSG_SIZE_LIMIT);

        chSnd.close();

        final byte[] serialisedData = baos.toByteArray();

        final ByteArrayInputStream bais = new ByteArrayInputStream(serialisedData);
        final ByteArrayStreamChannel chRcv = new ByteArrayStreamChannel(bais);


        final Message mr = p.receiveMessage(chRcv, Compressor.off(), Encryptor.off());

        assertArrayEquals(payload, mr.getData());
    }


    private byte[] createRandomPayload(final int bufSize) {
        final byte[] payload = new byte[bufSize];
        final Random random = new Random();
        random.nextBytes(payload);
        return payload;
    }


    private static int KB = 1024;
    private static int MB = KB * KB;
}
