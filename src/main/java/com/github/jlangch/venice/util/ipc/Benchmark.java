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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.util.CollectionUtil;
import com.github.jlangch.venice.util.ipc.impl.Messages;
import com.github.jlangch.venice.util.ipc.impl.util.IO;
import com.github.jlangch.venice.util.ipc.impl.util.URIHelper;


/**

<pre>

   Benchmark: MacBook Air M2, 24GB, MacOS 26, Venice 1.12.74
   --------------------------------------------------------------------------------------------------

   AF_INET tcp/ip sockets
   Java 8, single connection, single thread

   +-------------------------------------------------------------------------------------------------+
   | Payload bytes    | 5 KB        | 50 KB       | 500 KB      | 5 MB       | 50 MB     | 200 MB    |
   +-------------------------------------------------------------------------------------------------+
   | Throughput msgs  | 30578 msg/s | 23317 msg/s | 9498 msg/s  | 1552 msg/s | 122 msg/s | 30 msg/s  |
   | Throughput bytes | 149 MB/s    | 1139 MB/s   | 4638 MB/s   | 7760 MB/s  | 6104 MB/s | 5953 MB/s |
   +-------------------------------------------------------------------------------------------------+

   Java 8, multiple connections, 1 thread per connection

   +---------------------------------------------------------------------------------------------------------+
   | Payload bytes 5KB | 1 conn      | 2 conn      | 3 conn      | 4 conn      | 10 conn      | 100 conn     |
   +---------------------------------------------------------------------------------------------------------+
   | Throughput msgs   | 30578 msg/s | 57340 msg/s | 80033 msg/s | 93818 msg/s | 112331 msg/s | 119498 msg/s |
   | Throughput bytes  | 149 MB/s    | 280 MB/s    | 391 MB/s    | 458 MB/s    | 548 MB/s     | 583 MB/s     |
   +---------------------------------------------------------------------------------------------------------+

   Java 21, single connection, single thread

   +-------------------------------------------------------------------------------------------------+
   | Payload bytes    | 5 KB        | 50 KB       | 500 KB      | 5 MB       | 50 MB     | 200 MB    |
   +-------------------------------------------------------------------------------------------------+
   | Throughput msgs  | 34782 msg/s | 24099 msg/s | 9548 msg/s  | 1545 msg/s | 114 msg/s | 29 msg/s  |
   | Throughput bytes | 170 MB/s    | 1177 MB/s   | 4662 MB/s   | 7724 MB/s  | 5691 MB/s | 5797 MB/s |
   +-------------------------------------------------------------------------------------------------+

   Java 21, multiple connections, 1 thread per connection

   +---------------------------------------------------------------------------------------------------------+
   | Payload bytes 5KB | 1 conn      | 2 conn      | 3 conn      | 4 conn      | 10 conn      | 100 conn     |
   +---------------------------------------------------------------------------------------------------------+
   | Throughput msgs   | 34782 msg/s | 60245 msg/s | 83154 msg/s | 97530 msg/s | 120214 msg/s | 119482 msg/s |
   | Throughput bytes  | 170 MB/s    | 294 MB/s    | 406 MB/s    | 476 MB/s    | 587 MB/s     | 583 MB/s     |
   +---------------------------------------------------------------------------------------------------------+


   AF_UNIX Unix domain sockets
   Java 8, default socket snd/rcv buffer size, single connection, single thread

   +------------------------------------------------------------------------------------------------+
   | Payload bytes    | 5 KB        | 50 KB       | 500 KB      | 5 MB      | 50 MB     | 200 MB    |
   +------------------------------------------------------------------------------------------------+
   | Throughput msgs  | 49122 msg/s | 20003 msg/s | 3591 msg/s  | 7 msg/s   | - msg/s   | - msg/s   |
   | Throughput bytes | 240 MB/s    | 977 MB/s    | 1754 MB/s   | 34 MB/s   | - MB/s    | - MB/s    |
   +------------------------------------------------------------------------------------------------+


   AF_UNIX Unix domain sockets
   Java 8, 1MB socket snd/rcv buffer size, single connection, single thread

   +------------------------------------------------------------------------------------------------+
   | Payload bytes    | 5 KB        | 50 KB       | 500 KB      | 5 MB      | 50 MB     | 200 MB    |
   +------------------------------------------------------------------------------------------------+
   | Throughput msgs  | 49013 msg/s | 38756 msg/s | 13616 msg/s | 414 msg/s | 7.3 msg/s | 0.5 msg/s |
   | Throughput bytes | 239 MB/s    | 1892 MB/s   | 6649 MB/s   | 2072 MB/s | 365 MB/s  | 96 MB/s   |
   +------------------------------------------------------------------------------------------------+

   Java 8, 1MB socket snd/rcv buffer size, multiple connections, 1 thread per connection (limit 53 connections!)

   +-----------------------------------------------------------------------------------------------------------+
   | Payload bytes 5KB | 1 conn      | 2 conn      | 3 conn       | 4 conn       | 10 conn      | 50 conn      |
   +-----------------------------------------------------------------------------------------------------------+
   | Throughput msgs   | 49013 msg/s | 88937 msg/s | 120589 msg/s | 130190 msg/s | 137519 msg/s | 137386 msg/s |
   | Throughput bytes  | 239 MB/s    | 434 MB/s    | 589 MB/s     | 636 MB/s     | 671 MB/s     | 871 MB/s     |
   +-----------------------------------------------------------------------------------------------------------+



   Benchmark: VMWare, Intel(R) Xeon(R) Silver 4214 CPU @ 2.20GHz, 2 cores with 1 thread per core,
              12GB, AlmaLinux 9, Venice 1.12.74
   --------------------------------------------------------------------------------------------------

   AF_INET tcp/ip sockets
   Java 8, default socket snd/rcv buffer size, single connection, single thread

   +------------------------------------------------------------------------------------------------+
   | Payload bytes    | 5 KB         | 50 KB       | 500 KB     | 5 MB      | 50 MB     | 200 MB    |
   +------------------------------------------------------------------------------------------------+
   | Throughput msgs  | 26012 msg/s  | 16065 msg/s | 3419 msg/s | 345 msg/s | 34 msg/s  | 8.5 msg/s |
   | Throughput bytes | 127 MB/s     | 784 MB/s    | 1669 MB/s  | 1727 MB/s | 1681 MB/s | 1708 MB/s |
   +------------------------------------------------------------------------------------------------+

   Java 8, multiple connections, 1 thread per connection

   +-------------------------------------------------------------------------------------------------------+
   | Payload bytes 5KB | 1 conn      | 2 conn      | 3 conn      | 4 conn      | 10 conn     | 100 conn    |
   +-------------------------------------------------------------------------------------------------------+
   | Throughput msgs   | 26012 msg/s | 47891 msg/s | 50588 msg/s | 48312 msg/s | 48265 msg/s | 39386 msg/s |
   | Throughput bytes  | 127 MB/s    | 234 MB/s    | 247 MB/s    | 234 MB/s    | 236 MB/s    | 192 MB/s    |
   +-------------------------------------------------------------------------------------------------------+

</pre>
*/
public class Benchmark {

    public Benchmark(
            final String sConnURI,
            final long msgSize,
            final long duration,
            final int connections,
            final boolean print,
            final boolean encrypt,
            final boolean oneway,
            final int sndBufSize,
            final int rcvBufSize,
            final int rampUpDuration
    ) {
        this.msgSize = msgSize;
        this.duration = duration;
        this.connections = Math.min(100, connections);

        this.print = print;
        this.encrypt = encrypt;
        this.oneway = oneway;
        this.sndBufSize = sndBufSize;
        this.rcvBufSize = rcvBufSize;
        this.rampUpDuration = rampUpDuration;

        this.connURI = URIHelper.parseConnectionURI(sConnURI);
        this.hostAddr = URIHelper.getConnectionURIHost(connURI);
    }

    public Map<String,Object> run() {
        return hostAddr.isLoopbackAddress()
                ? benchmarkWithLocalServer()
                : benchmark();
    }

    public void runServer() {
        try(Server server = Server.of(ServerConfig.builder()
                                        .connURI(connURI)
                                        .maxMessageSize(Messages.MESSAGE_LIMIT_MAX)
                                        .encrypt(encrypt)
                                        .sendBufferSize(sndBufSize)
                                        .receiveBufferSize(rcvBufSize)
                                        .maxParallelConnections(connections + 1)
                                        .build())
        ) {
            server.start();

            if (print) System.out.println("Benchmark server started!");

            while(true) {
                Thread.sleep(10_000);
            }
        }
        catch(InterruptedException ex) {
            if (print) System.out.println("Benchmark server stopped!");
        }
        catch(Exception ex) {
            throw new VncException("Benchmark server failed!", ex);
        }
    }

    public Map<String,Object> runClient() {
        return benchmark();
    }

    private Map<String,Object> benchmarkWithLocalServer() {
        try(Server server = Server.of(ServerConfig.builder()
                                        .connURI(connURI)
                                        .maxMessageSize(Messages.MESSAGE_LIMIT_MAX)
                                        .encrypt(encrypt)
                                        .sendBufferSize(sndBufSize)
                                        .receiveBufferSize(rcvBufSize)
                                        .maxParallelConnections(connections + 1)
                                        .build())
        ) {
            server.start();

            IO.sleep(300);

            return benchmark();
        }
        catch(Exception ex) {
            throw new VncException("Benchmark failed!", ex);
        }
    }

    private Map<String,Object> benchmark() {
        // Payload data
        final byte[] payload = createRandomPayload();

        final ClientConfig config = ClientConfig
                                        .builder()
                                        .connURI(connURI)
                                        .sendBufferSize(sndBufSize)
                                        .receiveBufferSize(rcvBufSize)
                                        .build();

        try(Client client = Client.of(config)) {
            client.open();

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
            final Map<String,Object> result = connections > 1
                                        ? benchmark(client, connections, payload)
                                        : benchmark(client, payload);

            if (print) {
                System.out.println();
                System.out.println(result.get("summary"));
                return null;
            }
            else {
                return result;
            }
        }
        catch(Exception ex) {
            throw new IpcException("Benchmark failed!", ex);
        }
    }

    private Map<String,Object> benchmark(final Client client, final byte[] payload) {
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

        return computeStatistics(client, 1, count, msgSize,  elapsed);
    }

    private Map<String,Object> benchmark(final Client clientBase, final int clientCount, final byte[] payload) {
        System.out.println("Connections: " + clientCount);

        final ThreadPoolExecutor es = (ThreadPoolExecutor)Executors.newCachedThreadPool();
        es.setMaximumPoolSize(clientCount);

        final AtomicBoolean stop = new AtomicBoolean(false);
        final CyclicBarrier startBarrier = new CyclicBarrier(clientCount + 1);

        final List<Future<Map<String,Object>>> futures = new ArrayList<>();

        for(int cc=1; cc<=clientCount; cc++) {
            // start workers
            futures.add(es.submit(() -> {
                final Client client = clientBase.copy();
                int count = 0;

                try {
                    client.open();

                    // wait for all works to be ready to start
                    startBarrier.await();

                    final long start = System.currentTimeMillis();

                    while(!stop.get()) {
                        final IMessage m = client.test(payload, oneway);
                        if (ResponseStatus.OK != m.getResponseStatus()) {
                           throw new RuntimeException("Bad response");
                        }
                        count++;
                    }

                    final long elapsed = System.currentTimeMillis()-start;

                    return computeStatistics(
                            clientBase,
                            1,
                            count,
                            payload.length,
                            elapsed);
                }
                catch(Exception ex) {
                    return new HashMap<>();
                }
                finally {
                    try { client.close(); } catch (Exception ignore) {}
                }

            }));
        }

        try {
            // Wait for all benchmark worker threads to be ready
            startBarrier.await();

            IO.sleep(duration * 1000L);  // test window

            stop.set(true);  // stop request for workers

            System.out.println("Aggregating results...");

            // Wait for all workers to be finished
            final List<Map<String,Object>> results = new ArrayList<>();
            futures.forEach(f ->  { try { results.add(f.get()); } catch (Exception ignore) {}});

            final long msgCount = results
                                    .stream()
                                    .map(e -> e.get("message-count"))
                                    .mapToLong(e -> ((Long)e).longValue())
                                    .sum();

            // Aggregate statistics
            return computeStatistics(
                    clientBase,
                    clientCount,
                    msgCount,
                    payload.length,
                    duration * 1000L);
        }
        catch(Exception ex) {
            throw new IpcException("Benchmark failed!", ex);
        }
    }

    private void rampUp(final Client client, final byte[] payload) {
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

    private Map<String,Object> computeStatistics(
            final Client client,
            final int connections,
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

        final String summary = String.join(
                "\n",
                CollectionUtil.toList(
                    String.format("Messages:         %d", msgCount),
                    String.format("Payload size:     %d KB", msgSize / KB),
                    String.format("Encryption:       %s", encrypt ? "on" : "off"),
                    String.format("Compression:      %s", compress ? "on" : "off"),
                    String.format("Connections:      %d", connections),
                    "------------------------------",
                    String.format("Duration:         %.1f s", elapsedSec),
                    String.format("Total bytes:      %.1f MB", transferredMB),
                    String.format("Throughput msgs:  %s msg/s", sThroughputMsgs),
                    String.format("Throughput bytes: %s MB/s", sThroughputMB)));

        final HashMap<String,Object> info = new HashMap<>();
        info.put("message-count",    msgCount);
        info.put("message-size",     msgSize);
        info.put("duration-millis",  elapsedMillis);
        info.put("total-bytes-sent", transferredBytes);
        info.put("throughput-msgs",  throughputMsgs);
        info.put("throughput-MB",    throughputMB);
        info.put("encrypt",          encrypt);
        info.put("compress",         compress);
        info.put("connections",      connections);
        info.put("summary",          summary);
        return info;
    }



    private static int KB = 1024;
    private static int MB = KB * KB;


    private final long msgSize;
    private final long duration;
    private final int connections;

    private final boolean print;
    private final boolean encrypt;
    private final boolean oneway;
    private final int sndBufSize;
    private final int rcvBufSize;
    private final int rampUpDuration;

    private final URI connURI;
    private final InetAddress hostAddr;
}

