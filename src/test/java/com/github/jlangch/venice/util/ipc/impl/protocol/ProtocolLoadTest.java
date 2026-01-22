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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.util.ipc.impl.Message;
import com.github.jlangch.venice.util.ipc.impl.Messages;
import com.github.jlangch.venice.util.ipc.impl.util.Compressor;
import com.github.jlangch.venice.util.ipc.impl.util.Encryptor;


public class ProtocolLoadTest {

    @Test
    public void test_send() throws Exception{
        final Protocol p = new Protocol(false);

        // A test message with 10 byte payload
        final Message m = Messages.testMessage(new byte[10], false);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream(100 * MB);

        final ByteArrayStreamChannel ch = new ByteArrayStreamChannel(baos);

        // warmup
        for(int ii=0; ii<50_000; ii++) {
            p.sendMessage(ch, m, Compressor.off(), Encryptor.off(), KB);
        }

        final int count = 500_000;

        final long start = System.currentTimeMillis();

        for(int ii=0; ii<count; ii++) {
            p.sendMessage(ch, m, Compressor.off(), Encryptor.off(), KB);
        }

        ch.close();
        assertNotNull(baos.toByteArray());

        final long elapsedMicros = (System.currentTimeMillis() - start) * 1000L;

        System.out.println(String.format(
                "IPC Protocol: Sent %d messages: %.2f us / msg",
                count,
                ((double)elapsedMicros / (double)count)));
    }


    @Test
    public void test_receive() throws Exception{
        final Protocol p = new Protocol(false);

        // A test message with 10 byte payload
        final Message m = Messages.testMessage(new byte[10], false);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream(100 * MB);
        final ByteArrayStreamChannel chSnd = new ByteArrayStreamChannel(baos);

        final int countWarmup = 50_000;
        final int count = 500_000;

        for(int ii=0; ii<countWarmup + count; ii++) {
            p.sendMessage(chSnd, m, Compressor.off(), Encryptor.off(), KB);
        }

        chSnd.close();

        final byte[] serialisedData = baos.toByteArray();

        final ByteArrayInputStream bais = new ByteArrayInputStream(serialisedData);
        final ByteArrayStreamChannel chRcv = new ByteArrayStreamChannel(bais);

        // warmup
        for(int ii=0; ii<countWarmup; ii++) {
            final Message mr = p.receiveMessage(chRcv, Compressor.off(), Encryptor.off());
            assertNotNull(mr);
        }

        final long start = System.currentTimeMillis();

        for(int ii=0; ii<count; ii++) {
            final Message mr = p.receiveMessage(chRcv, Compressor.off(), Encryptor.off());
            assertNotNull(mr);
        }

        final long elapsedMicros = (System.currentTimeMillis() - start) * 1000L;

        System.out.println(String.format(
                "IPC Protocol: Received %d messages: %.2f us / msg",
                count,
                ((double)elapsedMicros / (double)count)));
    }

    private static int KB = 1024;
    private static int MB = KB * KB;
}
