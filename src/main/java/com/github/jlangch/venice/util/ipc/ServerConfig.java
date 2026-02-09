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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import com.github.jlangch.venice.util.ipc.impl.Messages;
import com.github.jlangch.venice.util.ipc.impl.ServerQueueManager;


public class ServerConfig {

    private ServerConfig(
            final URI connURI,
            final boolean encrypt,
            final int compressCutoffSize,
            final long maxMessageSize,
            final int maxQueues,
            final int maxTempQueuesPerConnection,
            final int maxTopics,
            final int maxFunctions,
            final int deadLetterQueueSize,
            final int sndBufSize,
            final int rcvBufSize,
            final int maxConnections,
            final Authenticator authenticator,
            final int heartbeatIntervalSeconds,
            final File walDir,
            final boolean walCompress,
            final boolean walCompactAtStart,
            final File logDir
    ) {
        this.connURI = connURI;
        this.encrypt = encrypt;
        this.compressCutoffSize = compressCutoffSize;
        this.maxMessageSize = maxMessageSize;
        this.maxQueues = maxQueues;
        this.maxTopics = maxTopics;
        this.maxFunctions = maxFunctions;
        this.maxTempQueuesPerConnection = maxTempQueuesPerConnection;
        this.deadLetterQueueSize = deadLetterQueueSize;
        this.sndBufSize = sndBufSize;
        this.rcvBufSize = rcvBufSize;
        this.maxConnections = maxConnections;
        this.authenticator = authenticator;
        this.heartbeatIntervalSeconds = heartbeatIntervalSeconds;
        this.walDir = walDir;
        this.walCompress = walCompress;
        this.walCompactAtStart = walCompactAtStart;
        this.logDir = logDir;
    }



    public URI getConnURI() {
        return connURI;
    }

    public boolean isEncrypting() {
        return encrypt;
    }

    public int getCompressCutoffSize() {
        return compressCutoffSize;
    }

    public int getSndBufSize() {
        return sndBufSize;
    }

    public int getRcvBufSize() {
        return rcvBufSize;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public long getMaxMessageSize() {
        return maxMessageSize;
    }

    public int getMaxQueues() {
        return maxQueues;
    }

    public int getMaxTempQueuesPerConnection() {
        return maxTempQueuesPerConnection;
    }

    public int getMaxTopics() {
        return maxTopics;
    }

    public int getMaxFunctions() {
        return maxFunctions;
    }

    public int getDeadLetterQueueSize() {
        return deadLetterQueueSize;
    }

    public Authenticator getAuthenticator() {
        return authenticator;
    }

    public int getHeartbeatIntervalSeconds() {
        return heartbeatIntervalSeconds;
    }

    public File getWalDir() {
        return walDir;
    }

    public boolean isWalCompress() {
        return walCompress;
    }

    public boolean isWalCompactAtStart() {
        return walCompactAtStart;
    }

    public File getLogDir() {
        return logDir;
    }



    public static ServerConfig of(final int port) {
        return new Builder().conn(port).build();
    }

    public static Builder builder() {
        return new Builder();
    }


    public static class Builder {

        /**
         * Client connecting to a server on the specified port on the local host.
         *
         * @param port a port
         * @return this builder
         */
        public Builder conn(final int port) {
            try {
                this.connURI = new URI(String.format("af-inet://127.0.0.1:%d", port));
            }
            catch(URISyntaxException ex) {
                throw new IpcException("Invalid connection URI", ex);
            }
            return this;
        }

        /**
         * Create a new server for the specified connection URI.
         *
         * <p>Supported socket types:
         * <ul>
         *    <li>AF_INET sockets (TCP/IP sockets)</li>
         *    <li>AF_UNIX domain sockets (Unix sockets, requires junixsocket libraries)</li>
         * </ul>
         *
         * <p>AF_INET
         * af-inet://localhost:33333
         *
         * <p>AF_UNIX
         * af-unix:///path/to/your/socket.sock
         *
         * @param connURI a connection URI
         * @return this builder
         */
        public Builder connURI(final String connURI) {
            Objects.requireNonNull(connURI);

            try {
                this.connURI = new URI(connURI);
            }
            catch(URISyntaxException ex) {
                throw new IpcException("Invalid connection URI", ex);
            }
            return this;
        }

        /**
         * Create a new server for the specified connection URI.
         *
         * <p>Supported socket types:
         * <ul>
         *    <li>AF_INET sockets (TCP/IP sockets)</li>
         *    <li>AF_UNIX domain sockets (Unix sockets, requires junixsocket libraries)</li>
         * </ul>
         *
         * <p>AF_INET
         * af-inet://localhost:33333
         *
         * <p>AF_UNIX
         * af-unix:///path/to/your/socket.sock
         *
         * @param connURI a connection URI
         * @return this builder
         */
        public Builder connURI(final URI connURI) {
            Objects.requireNonNull(connURI);

            this.connURI = connURI;
            return this;
        }

        /**
         * Set the encryption mode for all connections with this server.
         *
         * @param encrypt if <code>true</code> encrypt the payload data at transport
         *                level communication.
         * @return this builder
         */
        public Builder encrypt(final boolean encrypt) {
            this.encrypt = encrypt;
            return this;
        }

        /**
         * Set the compression cutoff size for payload messages.
         *
         * <p>With a negative cutoff size payload messages will not be compressed.
         * If the payload message size is greater than the cutoff size it will be
         * compressed.
         *
         * <p>Defaults to -1 (no compression)
         *
         * @param cutoffSize the compress cutoff size in bytes
         * @return this builder
         */
        public Builder compressCutoffSize(final int cutoffSize) {
            this.compressCutoffSize = cutoffSize < 0 ? -1 : cutoffSize;
            return this;
        }

        /**
         * Set the maximum message size.
         *
         * <p>Defaults to 20 MB
         *
         * @param maxSize the max message size in bytes. Must be in the range 2KB ... 250MB.
         * @return this builder
         */
        public Builder maxMessageSize(final long maxSize) {
            if (maxSize < Messages.MESSAGE_LIMIT_MIN || maxSize > Messages.MESSAGE_LIMIT_MAX) {
                throw new IllegalArgumentException(
                        "The maximum message size is limited to "
                        + Messages.MESSAGE_LIMIT_MIN + " .. " + Messages.MESSAGE_LIMIT_MAX
                        + " bytes!");
            }

            this.maxMessageSize = maxSize;
            return this;
        }

        /**
         * Set the max number of queues.
         *
         * <p>Defaults to 20
         *
         * @param maxQueues the max number of queues.
         * @return this builder
         */
        public Builder maxQueues(final int maxQueues) {
            if (maxQueues < Server.QUEUES_MIN || maxQueues > Server.QUEUES_MAX) {
                throw new IllegalArgumentException(
                        "The max number of queues is limited to "
                        + Server.QUEUES_MIN + " .. " + Server.QUEUES_MAX + "!");
            }

            this.maxQueues = maxQueues;
            return this;
        }

        /**
         * Set the max number of temporary queues per connection.
         *
         * <p>Defaults to 20
         *
         * @param maxQueues the max number of queues.
         * @return this builder
         */
        public Builder maxTempQueuesPerConnection(final int maxQueues) {
            if (maxQueues < Server.QUEUES_MIN || maxQueues > Server.QUEUES_MAX) {
                throw new IllegalArgumentException(
                        "The max number of temporary queues  per connection is limited to "
                        + Server.QUEUES_MIN + " .. " + Server.QUEUES_MAX + "!");
            }

            this.maxTempQueuesPerConnection = maxQueues;
            return this;
        }

        /**
         * Set the max number of topics.
         *
         * <p>Defaults to 20
         *
         * @param maxTopics the max number of topics.
         * @return this builder
         */
        public Builder maxTopics(final int maxTopics) {
            if (maxTopics < Server.TOPICS_MIN || maxTopics > Server.TOPICS_MAX) {
                throw new IllegalArgumentException(
                        "The max number of topics is limited to "
                        + Server.TOPICS_MIN + " .. " + Server.TOPICS_MAX + "!");
            }

            this.maxTopics = maxTopics;
            return this;
        }

        /**
         * Set the max number of functions.
         *
         * <p>Defaults to 20
         *
         * @param maxFunctions the max number of functions.
         * @return this builder
         */
        public Builder maxFunctions(final int maxFunctions) {
            if (maxFunctions < Server.FUNCTIONS_MIN || maxFunctions > Server.FUNCTIONS_MAX) {
                throw new IllegalArgumentException(
                        "The max number of functions is limited to "
                        + Server.FUNCTIONS_MIN + " .. " + Server.FUNCTIONS_MAX + "!");
            }

            this.maxFunctions = maxFunctions;
            return this;
        }

        /**
         * Set the size of the dead letter queue.
         *
         * <p>Defaults to 100
         *
         * @param size the dead letter queue's size. A size of 0 deactivates
         *             the dead letter queue
         * @return this builder
         */
        public Builder deadLetterQueueSize(final int size) {
            this.deadLetterQueueSize = Math.min(10_000, Math.max(0, size));
            return this;
        }

        /**
         * Set the socket's send buffer size. -1 keeps the default.
         *
         * @param bufSize a send buffer size
         * @return this builder
         */
        public Builder sendBufferSize(final int bufSize) {
            this.sndBufSize = bufSize <= 0 ? -1 : bufSize;
            return this;
        }

        /**
         * Set the socket's receive buffer size. -1 keeps the default.
         *
         * @param bufSize a receive buffer size
         * @return this builder
         */
        public Builder receiveBufferSize(final int bufSize) {
            this.rcvBufSize = bufSize <= 0 ? -1 : bufSize;
            return this;
        }

        /**
         * Set maximum parallel connections the server can handle.
         *
         * <p>Defaults to 20
         *
         * @param count the max parallel connections
         * @return this builder
         */
        public Builder maxParallelConnections(final int count) {
            this.maxConnections = Math.max(1, count);  // need at least one
            return this;
        }

        /**
         * Set an authenticator
         *
         * <p>Note: For security reasons enforce encryption to securely send the
         * user credentials from a client to the server!
         *
         * @param authenticator a client authenticator.
         * @return this builder
         */
        public Builder authenticator(final Authenticator authenticator) {
            if (authenticator != null) {
                this.authenticator = authenticator;
            }
            return this;
        }

        /**
         * Set a heartbeat interval in seconds. A value equal or lower to zero  will
         * turnoff the hearbeat.
         *
         * <p>Defaults to <code>0</code> (hearbeat turned off)
         *
         * @param seconds the heartbeat interval in seconds
         * @return this server
         */
        public Builder hearbeatIntervalSeconds(final int seconds) {
            this.heartbeatIntervalSeconds = Math.max(0, seconds);
            return this;
        }

        /**
         * Enable  Write-Ahead-Logs
         *
         * @param walDir the Write-Ahead-Logs directory
         * @param compress enable/disable Write-Ahead-Log entry compression
         * @param compactAtStart if true compact the Write-Ahead-Log at startup
         * @return this builder
         */
        public Builder enableWriteAheadLog(
                final File walDir,
                final boolean compress,
                final boolean compactAtStart
         ) {
            Objects.requireNonNull(walDir);

            if (!walDir.isDirectory()) {
                throw new IpcException(
                        "The WAL directory '" + walDir.getAbsolutePath() + "' does not exist!");
            }

            this.walDir = walDir;
            this.walCompress = compress;
            this.walCompactAtStart = compactAtStart;
            return this;
        }

        /**
         * Enable the server logger within the specified log directory
         *
         * @param logDir a log directory
         * @return this builder
         */
        public Builder enableLogger(final File logDir) {
            Objects.requireNonNull(logDir);

            if (!logDir.isDirectory()) {
                throw new IpcException(
                        "The server log directory '" + logDir.getAbsolutePath() + "' does not exist!");
            }

            this.logDir = logDir;
            return this;
        }



        public ServerConfig build() {
            if (connURI == null) {
                throw new IpcException("A connection URI must not be null");
            }

            return new ServerConfig(
                    connURI,
                    encrypt,
                    compressCutoffSize,
                    maxMessageSize,
                    maxQueues,
                    maxTempQueuesPerConnection,
                    maxTopics,
                    maxFunctions,
                    deadLetterQueueSize,
                    sndBufSize,
                    rcvBufSize,
                    maxConnections,
                    authenticator,
                    heartbeatIntervalSeconds,
                    walDir,
                    walCompress,
                    walCompactAtStart,
                    logDir);
        }


        private URI connURI = null;
        private boolean encrypt = false;
        private int compressCutoffSize = -1;
        private long maxMessageSize = Messages.MESSAGE_LIMIT_DEFAULT;
        private int maxQueues = Server.QUEUES_MAX_DEFAULT;
        private int maxTempQueuesPerConnection = Server.QUEUES_MAX;
        private int maxTopics = Server.TOPICS_MAX_DEFAULT;
        private int maxFunctions = Server.FUNCTIONS_MAX_DEFAULT;
        private int deadLetterQueueSize = ServerQueueManager.DEAD_LETTER_QUEUE_SIZE;
        private int sndBufSize = -1;
        private int rcvBufSize = -1;
        private int maxConnections = Server.MAX_CONNECTIONS_DEFAULT;
        private Authenticator authenticator = new Authenticator(false);
        private int heartbeatIntervalSeconds = 0;
        private File walDir;
        private boolean walCompress;
        private boolean walCompactAtStart;
        private File logDir;
    }


    private final URI connURI;
    private final boolean encrypt;
    private final int compressCutoffSize;
    private final long maxMessageSize;
    private final int maxQueues;
    private final int maxTempQueuesPerConnection;
    private final int maxTopics;
    private final int maxFunctions;
    private final int deadLetterQueueSize;
    private final int sndBufSize;
    private final int rcvBufSize;
    private final int maxConnections;
    private final Authenticator authenticator;
    private final int heartbeatIntervalSeconds;
    private final File walDir;
    private final boolean walCompress;
    private final boolean walCompactAtStart;
    private final File logDir;
}
