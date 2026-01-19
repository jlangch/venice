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

import java.net.InetAddress;
import java.net.URI;
import java.util.Objects;
import java.util.Random;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncDouble;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.util.ipc.impl.Messages;
import com.github.jlangch.venice.util.ipc.impl.util.IO;


// Benchmark: MacBook Air M2, 24GB, MacOS 26
// --------------------------------------------------------------------------------------------------
//
// AF_INET tcp/ip sockets
//
// +-------------------------------------------------------------------------------------------------+
// | Payload bytes    | 5 KB        | 50 KB       | 500 KB      | 5 MB       | 50 MB     | 200 MB    |
// +-------------------------------------------------------------------------------------------------+
// | Throughput msgs  | 22624 msg/s | 16178 msg/s | 6901 msg/s  | 1082 msg/s | 95 msg/s  | 22 msg/s  |
// | Throughput bytes | 110 MB/s    | 790 MB/s    | 3370 MB/s   | 5411 MB/s  | 4728 MB/s | 4359 MB/s |
// +-------------------------------------------------------------------------------------------------+
//
//
// AF_UNIX Unix domain sockets: default socket snd/rcv buffer size
//
// +------------------------------------------------------------------------------------------------+
// | Payload bytes    | 5 KB        | 50 KB       | 500 KB      | 5 MB      | 50 MB     | 200 MB    |
// +------------------------------------------------------------------------------------------------+
// | Throughput msgs  | 33597 msg/s | 18618 msg/s | 3577 msg/s  | 6 msg/s   | - msg/s   | - msg/s   |
// | Throughput bytes | 164 MB/s    | 909 MB/s    | 1747 MB/s   | 31 MB/s   | - MB/s    | - MB/s    |
// +------------------------------------------------------------------------------------------------+
//
//
// AF_UNIX Unix domain sockets: 1MB socket snd/rcv buffer size
//
// +------------------------------------------------------------------------------------------------+
// | Payload bytes    | 5 KB        | 50 KB       | 500 KB      | 5 MB      | 50 MB     | 200 MB    |
// +------------------------------------------------------------------------------------------------+
// | Throughput msgs  | 34014 msg/s | 28196 msg/s | 10192 msg/s | 373 msg/s | 6 msg/s   | 0.4 msg/s |
// | Throughput bytes | 166 MB/s    | 1377 MB/s   | 4977 MB/s   | 1863 MB/s | 285 MB/s  | 78 MB/s   |
// +------------------------------------------------------------------------------------------------+
//
//
//
// Benchmark: VMWare, Intel(R) Xeon(R) Silver 4214 CPU @ 2.20GHz, 2 cores with 1 thread per core,
//            12GB, AlmaLinux 9
// --------------------------------------------------------------------------------------------------
//
// AF_INET tcp/ip sockets
//
// +-----------------------------------------------------------------------------------------------+
// | Payload bytes    | 5 KB        | 50 KB       | 500 KB     | 5 MB      | 50 MB     | 200 MB    |
// +-----------------------------------------------------------------------------------------------+
// | Throughput msgs  | 11776 msg/s | 8515 msg/s  | 2068 msg/s | 202 msg/s | 19 msg/s  | 3.7 msg/s |
// | Throughput bytes | 57.5 MB/s   | 416 MB/s    | 1010 MB/s  | 1009 MB/s | 929 MB/s  | 737 MB/s  |
// +-----------------------------------------------------------------------------------------------+
//
//
public class Benchmark {

    public Benchmark(
            final String sConnURI,
            final long msgSize,
            final long duration,
            final long connections,
            final boolean print,
            final boolean encrypt,
            final boolean oneway,
            final int sndBufSize,
            final int rcvBufSize,
            final int rampUpDuration
    ) {
        this.msgSize = msgSize;
        this.duration = duration;
        this.connections = connections;

        this.print = print;
        this.encrypt = encrypt;
        this.oneway = oneway;
        this.sndBufSize = sndBufSize;
        this.rcvBufSize = rcvBufSize;
        this.rampUpDuration = rampUpDuration;

        this.connURI = parseConnectionURI(sConnURI);
        this.hostAddr = getConnectionURIHost(connURI);
    }

    public VncHashMap run() {
        return hostAddr.isLoopbackAddress()
                ? benchmarkWithLocalServer()
                : benchmarkSingleConnection();
    }

    private VncHashMap benchmarkWithLocalServer() {
        try(TcpServer server = TcpServer.of(connURI);) {
            server.setMaxMessageSize(Messages.MESSAGE_LIMIT_MAX);
            server.setEncryption(encrypt);
            server.setSndRcvBufferSize(sndBufSize, rcvBufSize);
            server.setMaxParallelConnections(2);
            server.start();

            IO.sleep(300);

            return benchmarkSingleConnection();
        }
        catch(Exception ex) {
            throw new VncException("Benchmark failed!", ex);
        }
    }

    private VncHashMap benchmarkSingleConnection() {
        try(TcpClient client = TcpClient.of(connURI)) {
            client.setSndRcvBufferSize(sndBufSize, rcvBufSize);
            client.open();

            // Payload data
            final byte[] payload = createRandomPayload();

            // Ramp-Up phase
            if (rampUpDuration > 0) {
                   if (print) System.out.println("Ramp up...");

                   rampUp(client, payload);

                   if (print) {
                       System.out.println("Ramp up done.");
                       System.out.println("Benchmark...");
                   }
            }

            // Benchmark
            final VncHashMap result = benchmark(client, payload);

            if (print) {
                System.out.println();
                System.out.println(result.get(new VncKeyword("summary")).toString());
                return null;
            }
            else {
                return result;
            }
        }
        catch(Exception ex) {
            throw new VncException("Benchmark failed!", ex);
        }
    }

    private VncHashMap benchmark(final TcpClient client, final byte[] payload) {
        final long start = System.currentTimeMillis();
        final long end = start + duration * 1000L;

        int count = 0;
        long elapsed = 0;

        while(true) {
            final IMessage m = client.test(payload, oneway);
            if (ResponseStatus.OK != m.getResponseStatus()) {
               throw new RuntimeException("Bad response");
            }
            count++;

            final long now = System.currentTimeMillis();
            if (now > end) {
                elapsed = now - start;
                break;
            }
        }

        return computeStatistics(client, count, msgSize,  elapsed);
    }

    private void rampUp(final byte[] payload) {
        try(TcpClient client = TcpClient.of(connURI)) {
            client.setSndRcvBufferSize(sndBufSize, rcvBufSize);
            client.open();

            rampUp(client, payload);
        }
    }

    private void rampUp(final TcpClient client, final byte[] payload) {
        final long end = System.currentTimeMillis() + rampUpDuration * 1000L;

        while(true) {
            final IMessage m = client.test(payload, oneway);
            if (ResponseStatus.OK != m.getResponseStatus()) {
               throw new RuntimeException("Bad response");
            }
            if (System.currentTimeMillis() > end) {
                break;
            }
        }
    }

    private byte[] createRandomPayload() {
        final byte[] payload = new byte[(int)msgSize];
        final Random random = new Random();
        random.nextBytes(payload);
        return payload;
    }

    private VncHashMap computeStatistics(
            final TcpClient client,
            final long msgCount,
            final long payloadSize,
            final long elapsedMillis
    ) {
        final double elapsedSec     = elapsedMillis / 1000.0;
        final long transferredBytes = msgCount * msgSize;
        final double transferredMB  = transferredBytes / (double)MB;
        final double throughputMsgs = msgCount / elapsedSec;
        final double throughputMB   = transferredMB / elapsedSec;
        final boolean compress      = client.isCompressing();

        final String sThroughputMsgs = throughputMsgs < 10.0
                                        ? String.format("%.1f", throughputMsgs)
                                        : String.format("%.0f", throughputMsgs);

        final String sThroughputMB = throughputMB < 100.0
                                        ? String.format("%.1f", throughputMB)
                                        : String.format("%.0f", throughputMB);

        final StringBuilder summary = new StringBuilder();
        summary.append(String.format("Messages:         %d\n", msgCount));
        summary.append(String.format("Payload size:     %d KB\n", msgSize / KB));
        summary.append(String.format("Encryption:       %s\n", encrypt ? "on" : "off"));
        summary.append(String.format("Compression:      %s\n", compress ? "on" : "off"));
        summary.append("------------------------------\n");
        summary.append(String.format("Duration:         %.1f s\n", elapsedSec));
        summary.append(String.format("Total bytes:      %.1f MB\n", transferredMB));
        summary.append(String.format("Throughput msgs:  %s msg/s\n", sThroughputMsgs));
        summary.append(String.format("Throughput bytes: %s MB/s\n", sThroughputMB));

        return VncHashMap.of(
                new VncKeyword("message-count"),    new VncLong(msgCount),
                new VncKeyword("message-size"),     new VncLong(msgSize),
                new VncKeyword("duration-millis"),  new VncLong(elapsedMillis),
                new VncKeyword("total-bytes-sent"), new VncLong(transferredBytes),
                new VncKeyword("throughput-msgs"),  new VncDouble(throughputMsgs),
                new VncKeyword("throughput-MB"),    new VncDouble(throughputMB),
                new VncKeyword("encrypt"),          VncBoolean.of(encrypt),
                new VncKeyword("compress"),         VncBoolean.of(compress),
                new VncKeyword("summary"),          new VncString(summary.toString()));
    }

    private URI parseConnectionURI(final String uri) {
        try {
            Objects.requireNonNull(uri);
            return new URI(uri);
        }
        catch(Exception ex) {
            throw new IpcException("Invalid connection URI", ex);
        }
    }

    private InetAddress getConnectionURIHost(final URI uri) {
        try {
            Objects.requireNonNull(uri);
            return InetAddress.getByName(uri.getHost());
        }
        catch(Exception ex) {
            throw new IpcException("Invalid connection URI host", ex);
        }
    }


    private static int KB = 1024;
    private static int MB = KB * KB;


    private final long msgSize;
    private final long duration;
    private final long connections;

    private final boolean print;
    private final boolean encrypt;
    private final boolean oneway;
    private final int sndBufSize;
    private final int rcvBufSize;
    private final int rampUpDuration;

    private final URI connURI;
    private final InetAddress hostAddr;
}

