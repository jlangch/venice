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


public class Benchmark {

    public Benchmark(
            final String sConnURI,
            final long msgSize,
            final long msgCount,
            final long duration,

            final boolean print,
            final boolean encrypt,
            final boolean oneway,
            final int sndBufSize,
            final int rcvBufSize,
            final int rampUpMsgCount,
            final int rampUpDuration
    ) {
        this.sConnURI = sConnURI;
        this.msgSize = msgSize;
        this.msgCount = msgCount;
        this.duration = duration;

        this.print = print;
        this.encrypt = encrypt;
        this.oneway = oneway;
        this.sndBufSize = sndBufSize;
        this.rcvBufSize = rcvBufSize;
        this.rampUpMsgCount = rampUpMsgCount;
        this.rampUpDuration = rampUpDuration;

        this.connURI = parseConnectionURI(sConnURI);
        this.hostAddr = getConnectionURIHost(connURI);
    }

    public VncHashMap run() {
        return hostAddr.isLoopbackAddress()
                ? runLocal()
                : runRemote();
    }

    private VncHashMap runLocal() {
        try(TcpServer server = TcpServer.of(connURI);
            TcpClient client = TcpClient.of(connURI)
        ) {
            server.setMaxMessageSize(Messages.MESSAGE_LIMIT_MAX);
            server.setEncryption(encrypt);
            server.setSndRcvBufferSize(sndBufSize, rcvBufSize);
            server.setMaxParallelConnections(2);
            server.start();

            IO.sleep(300);

            client.setSndRcvBufferSize(sndBufSize, rcvBufSize);
            client.open();

            // Payload data (random)
            final byte[] payload = new byte[(int)msgSize];
            final Random random = new Random();
            random.nextBytes(payload);

            // Ramp-Up phase
            if (rampUpMsgCount > 0 && rampUpDuration > 0) {
                   if (print) System.out.println("Ramp up...");

                   rampUp(client, payload);

                   if (print) {
                       System.out.println("Ramp up done.");
                       System.out.println("Benchmark...\n\n");
                   }
            }

            // Benchmark
            final VncHashMap result = benchmark(client, payload);

            if (print) {
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


    private VncHashMap runRemote() {
        try(TcpClient client = TcpClient.of(connURI)) {
            client.setSndRcvBufferSize(sndBufSize, rcvBufSize);
            client.open();

            // Test
            final byte[] payload = new byte[(int)msgSize];

            // Ramp-Up phase
            if (rampUpMsgCount > 0 && rampUpDuration > 0) {
                   if (print) System.out.println("Ramp up...");

                   rampUp(client, payload);

                   if (print) {
                       System.out.println("Ramp up done.");
                       System.out.println("Benchmark...\n\n");
                   }
            }

            // Benchmark
            final VncHashMap result = benchmark(client, payload);

            if (print) {
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
            if (now > end || count >= msgCount) {
                elapsed = now - start;
                break;
            }
        }

        // Statistics

        final double elapsedSec     = elapsed / 1000.0;
        final long transferredBytes = count * msgSize;
        final double transferredMB  = transferredBytes / (double)MB;
        final double throughputMsgs = count / elapsedSec;
        final double throughputMB   = transferredMB / elapsedSec;
        final boolean compress      = client.isCompressing();

        final String sThroughputMsgs = throughputMsgs < 10.0
                                        ? String.format("%.1f", throughputMsgs)
                                        : String.format("%.0f", throughputMsgs);

        final String sThroughputMB = throughputMB < 100.0
                                        ? String.format("%.1f", throughputMB)
                                        : String.format("%.0f", throughputMB);

        final StringBuilder summary = new StringBuilder();
        summary.append(String.format("Messages:         %d\n", count));
        summary.append(String.format("Payload size:     %d KB\n", msgSize / KB));
        summary.append(String.format("Encryption:       %s\n", encrypt ? "on" : "off"));
        summary.append(String.format("Compression:      %s\n", compress ? "on" : "off"));
        summary.append("------------------------------\n");
        summary.append(String.format("Duration:         %.1f s\n", elapsedSec));
        summary.append(String.format("Total bytes:      %.1f MB\n", transferredMB));
        summary.append(String.format("Throughput msgs:  %s msg/s\n", sThroughputMsgs));
        summary.append(String.format("Throughput bytes: %s MB/s\n", sThroughputMB));

        return VncHashMap.of(
                new VncKeyword("params"), VncHashMap.of(
                                            new VncKeyword("connection-uri"),      new VncString(sConnURI),
                                            new VncKeyword("msg-size"),            new VncLong(msgSize),
                                            new VncKeyword("msg-count"),           new VncLong(msgCount),
                                            new VncKeyword("duration"),            new VncLong(duration),
                                            new VncKeyword("socket-snd-buf-size"), new VncLong(sndBufSize),
                                            new VncKeyword("socket-rcv-buf-size"), new VncLong(rcvBufSize),
                                            new VncKeyword("ramp-up-msg-count"),   new VncLong(rampUpMsgCount),
                                            new VncKeyword("ramp-up-duration"),    new VncLong(rampUpDuration)),

                new VncKeyword("message-count"),    new VncLong(count),
                new VncKeyword("message-size"),     new VncLong(msgSize),
                new VncKeyword("duration-millis"),  new VncLong(elapsed),
                new VncKeyword("total-bytes-sent"), new VncLong(transferredBytes),
                new VncKeyword("throughput-msgs"),  new VncDouble(throughputMsgs),
                new VncKeyword("throughput-MB"),    new VncDouble(throughputMB),
                new VncKeyword("encrypt"),          VncBoolean.of(encrypt),
                new VncKeyword("compress"),         VncBoolean.of(compress),
                new VncKeyword("summary"),          new VncString(summary.toString()));
    }

    private void rampUp(final TcpClient client, final byte[] payload) {
        final long end = System.currentTimeMillis() + rampUpDuration * 1000L;
        int count = 0;

        while(true) {
            final IMessage m = client.test(payload, oneway);
            if (ResponseStatus.OK != m.getResponseStatus()) {
               throw new RuntimeException("Bad response");
            }
            count++;
            if (System.currentTimeMillis() > end || count >= rampUpMsgCount) {
                break;
            }
        }
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


    private final String sConnURI;
    private final long msgSize;
    private final long msgCount;
    private final long duration;

    private final boolean print;
    private final boolean encrypt;
    private final boolean oneway;
    private final int sndBufSize;
    private final int rcvBufSize;
    private final int rampUpMsgCount;
    private final int rampUpDuration;

    private final URI connURI;
    private final InetAddress hostAddr;
}

