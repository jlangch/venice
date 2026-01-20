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
import java.net.URISyntaxException;
import java.util.Objects;

import com.github.jlangch.venice.impl.util.StringUtil;


public class ClientConfig {

    private ClientConfig(
            final URI connURI,
            final boolean encrypt,
            final int sndBufSize,
            final int rcvBufSize
    ) {
        this.connURI = connURI;
        this.encrypt = encrypt;
        this.sndBufSize = sndBufSize;
        this.rcvBufSize = rcvBufSize;

        this.ackMode = AcknowledgeMode.NO_ACKNOWLEDGE;
    }



    public URI getConnURI() {
        return connURI;
    }

    public boolean isEncrypting() {
        return encrypt;
    }

    public int getSndBufSize() {
        return sndBufSize;
    }

    public int getRcvBufSize() {
        return rcvBufSize;
    }

    public AcknowledgeMode getAckMode() {
        return ackMode;
    }

    public static ClientConfig of(final int port) {
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
                this.connURI = new URI(String.format(
                                            "af-inet://%s:%d",
                                            "127.0.0.1",
                                            port));
            }
            catch(URISyntaxException ex) {
                throw new IpcException("Invalid connection URI", ex);
            }
            return this;
        }

        /**
         * Client connecting to a server on the specified host / port.
         *
         * @param host a host
         * @param port a port
         * @return this builder
         */
        public Builder conn(final String host, final int port) {
            try {
                this.connURI = new URI(String.format(
                                            "af-inet://%s:%d",
                                            StringUtil.isBlank(host) ? "127.0.0.1" : host,
                                            port));
            }
            catch(URISyntaxException ex) {
                throw new IpcException("Invalid connection URI", ex);
            }
            return this;
        }

        /**
         * Create a new client for the specified connection URI.
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
         * Create a new client for the specified connection URI.
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
         * Set the encryption mode for this client-server connection.
         *
         * <p>The encryption is basically controlled by the server the client is attached to.
         *
         * <p>If the server requests encryption it cannot be weakened by the client.
         *
         * <pre>
         * ┌────────────────┬────────────────┬──────────────────────────┐
         * │ server encrypt │ client encrypt │ client-server connection │
         * ├────────────────┼────────────────┼──────────────────────────┤
         * │      false     │     false      │      not encrypted       │
         * │      false     │     true       │        encrypted         │
         * │      true      │     false      │        encrypted         │
         * │      true      │     true       │        encrypted         │
         * └────────────────┴────────────────┴──────────────────────────┘
         * </pre>
         *
         * @param encrypt if <code>true</code> encrypt the payload data at transport
         *                level communication between this client and the server.
         * @return this builder
         */
        public Builder encrypt(final boolean encrypt) {
            this.encrypt = encrypt;
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

        public ClientConfig build() {
            if (connURI == null) {
                throw new IpcException("A connection URI must not be null");
            }

            return new ClientConfig(
                    connURI,
                    encrypt,
                    sndBufSize,
                    rcvBufSize);
        }


        private URI connURI = null;
        private boolean encrypt = false;
        private int sndBufSize = -1;
        private int rcvBufSize = -1;
    }


    private final URI connURI;
    private final boolean encrypt;
    private final int sndBufSize;
    private final int rcvBufSize;

    private final AcknowledgeMode ackMode;
}
