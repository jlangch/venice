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
package com.github.jlangch.venice.util.ipc;


import com.github.jlangch.venice.util.ipc.impl.util.IO;


public class BenchmarkTest {

    public static void main(String[] args) {
        try(TcpServer server = new TcpServer(33333);
            TcpClient client = new TcpClient(33333)
        ) {
            server.setMaxMessageSize(200 * MB);
            server.start();

            IO.sleep(300);

            client.open();

            final int rounds = 3000;
            final int payloadSize = 5 * MB;

            final Stats stats = run(client, rounds, payloadSize, 5);

            final double elapsedSec = stats.elapsedMillis / 1000.0;
            final long transferred = (long)stats.messages * (long)payloadSize;

            System.out.println(String.format("Messages:         %d", stats.messages));
            System.out.println(String.format("Payload size:     %d KB", payloadSize / KB));
            System.out.println("------------------------------");
            System.out.println(String.format("Duration:         %.2fs", elapsedSec));
            System.out.println(String.format("Total bytes:      %.2f MB", (double)transferred/ (double)MB));

            System.out.println(String.format(
                    "Throughput msgs:  %d msg/s",
                    (int)(stats.messages / elapsedSec + 0.5)));

            System.out.println(String.format(
                    "Throughput bytes: %.2f MB/s",
                    ((double)transferred / (double)MB) / elapsedSec));
        }
    }

    private static Stats run(
            final TcpClient client,
            final int messages,
            final int payloadBytes,
            final int maxDurationSec
    ) {
        final byte[] payload = new byte[payloadBytes];

        final long start = System.currentTimeMillis();
        final long end = start + 1000L * maxDurationSec;

        int msgCount = 0;

        for(int ii=0; ii<messages; ii++) {
            final IMessage m = client.test(payload);
            if (ResponseStatus.OK != m.getResponseStatus()) {
               throw new RuntimeException("Bad response");
            }
            msgCount++;
            if (System.currentTimeMillis() > end) {
                break;
            }
        }

        return new Stats(msgCount, payloadBytes, System.currentTimeMillis() - start);
    }


    private static class Stats {
        public Stats(
            final int messages,
            final int payloadBytes,
            final long elapsedMillis
        ) {
            this.messages = messages;
            this.payloadBytes = payloadBytes;
            this.elapsedMillis = elapsedMillis;
        }

        final int messages;
        final int payloadBytes;
        final long elapsedMillis;
    }


    private static int KB = 1024;
    private static int MB = KB * KB;
}
