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


import java.net.URI;

import com.github.jlangch.venice.util.ipc.impl.util.IO;


/**
 *
 *
 * <p>MacBook Air M2, 24GB, MacOS 26
 *
 * <pre>
 * AF_INET
 *
 * +-----------------------------------------------------------------------------------------------+
 * | Payload bytes    | 5 KB        | 50 KB       | 500 KB     | 5 MB      | 50 MB     | 200 MB    |
 * +-----------------------------------------------------------------------------------------------+
 * | Throughput msgs  | 14970 msg/s | 13793 msg/s | 6435 msg/s | 804 msg/s | 46 msg/s  | 11 msg/s  |
 * | Throughput bytes | 73 MB/s     | 674 MB/s    | 3142 MB/s  | 3926 MB/s | 2291 MB/s | 2219 MB/s |
 * +-----------------------------------------------------------------------------------------------+
 * </pre>
 *
 * <pre>
 * AF_UNIX
 *
 * +-----------------------------------------------------------------------------------------------+
 * | Payload bytes    | 5 KB        | 50 KB       | 500 KB     | 5 MB      | 50 MB     | 200 MB    |
 * +-----------------------------------------------------------------------------------------------+
 * | Throughput msgs  | 29034 msg/s | 14479 msg/s | 3540 msg/s | 6 msg/s   | - msg/s   | - msg/s   |
 * | Throughput bytes | 142 MB/s    | 707 MB/s    | 1728 MB/s  | 27 MB/s   | - MB/s    | - MB/s    |
 * +-----------------------------------------------------------------------------------------------+
 * </pre>
 */
public class Benchmark {

    public static void main(String[] args) throws Exception {
        final int rounds = 300000;
        final int payloadSize = 5 * KB;
        final int maxDurationSeconds = 5;

        final URI connURI_1 = new URI("af-inet://localhost:33333");
        final URI connURI_2 = new URI("af-unix:///Users/juerg/Desktop/venice/tmp/test.sock");

        run(connURI_2, rounds, payloadSize, maxDurationSeconds);
    }

    public static void run(
            final URI connURI,
            final int rounds,
            final int payloadSize,
            final int maxDurationSeconds
    ) {
        try(TcpServer server = TcpServer.of(connURI);
            TcpClient client = TcpClient.of(connURI)
        ) {
            server.setMaxMessageSize(200 * MB);
            server.start();

            IO.sleep(300);

            client.open();

            final Stats stats = run(client, rounds, payloadSize, maxDurationSeconds);

            final double elapsedSec = stats.elapsedMillis / 1000.0;
            final long transferred = (long)stats.messages * (long)payloadSize;

            System.out.println(String.format("Messages:         %d", stats.messages));
            System.out.println(String.format("Payload size:     %d KB", payloadSize / KB));
            System.out.println("------------------------------");
            System.out.println(String.format("Duration:         %.1fs", elapsedSec));
            System.out.println(String.format("Total bytes:      %.1f MB", (double)transferred/ (double)MB));

            System.out.println(String.format(
                    "Throughput msgs:  %d msg/s",
                    (int)(stats.messages / elapsedSec + 0.5)));

            System.out.println(String.format(
                    "Throughput bytes: %.0f MB/s",
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
