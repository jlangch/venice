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
package com.github.jlangch.venice.impl.repl.remote;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.io.FileUtil;
import com.github.jlangch.venice.impl.util.io.IOStreamUtil;
import com.github.jlangch.venice.util.ipc.impl.util.Json;


public class ReplClientConfig {

    public ReplClientConfig(
            final String host,
            final int port,
            final String password
    ) {
        this.host = host;
        this.port = port;
        this.password = password;
        this.clientPublicKeyFile = null;
        this.clientPrivateKeyFile = null;
        this.serverPublicKeyFile = null;
    }

    public ReplClientConfig(
            final String host,
            final int port,
            final String password,
            final String clientPublicKeyFile,
            final String clientPrivateKeyFile,
            final String serverPublicKeyFile
    ) {
        this.host = host;
        this.port = port;
        this.password = password;
        this.clientPublicKeyFile = clientPublicKeyFile;
        this.clientPrivateKeyFile = clientPrivateKeyFile;
        this.serverPublicKeyFile = serverPublicKeyFile;
    }


    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getPassword() {
        return password;
    }

    public String getClientPublicKeyFile() {
        return clientPublicKeyFile;
    }

    public String getClientPrivateKeyFile() {
        return clientPrivateKeyFile;
    }

    public String getServerPublicKeyFile() {
        return serverPublicKeyFile;
    }


    public static ReplClientConfig of(final String jsonConfig) {
        Objects.requireNonNull(jsonConfig);

        final VncMap map = (VncMap)Json.readJson(jsonConfig, false);

        return new ReplClientConfig(
                toString("host", map.get(new VncString("host"), new VncString("localhost"))),
                toInt("port", map.get(new VncString("port")), 0),
                toString("password", map.get(new VncString("password"), Constants.Nil)),
                toString("clientPublicKeyFile", map.get(new VncString("clientPublicKeyFile"))),
                toString("clientPrivateKeyFile", map.get(new VncString("clientPrivateKeyFile"))),
                toString("serverPublicKeyFile", map.get(new VncString("serverPublicKeyFile"))));
    }

    public static ReplClientConfig load(final File jsonConfig) {
        Objects.requireNonNull(jsonConfig);

        try {
            return ReplClientConfig.of(
                       new String(FileUtil.load(jsonConfig), StandardCharsets.UTF_8));
        }
        catch (Exception ex) {
            throw new RuntimeException("Failed to load REPL client config from file " + jsonConfig, ex);
        }
    }

    public static ReplClientConfig load(final InputStream is) {
        Objects.requireNonNull(is);

        try {
            return ReplClientConfig.of(
                    new String(IOStreamUtil.copyIStoByteArray(is), StandardCharsets.UTF_8));
        }
        catch (Exception ex) {
            throw new RuntimeException("Failed to load REPL client config from stream.", ex);
        }
    }


    private static String toString(final String field, final VncVal val) {
        if (val == Constants.Nil) {
            return null;
        }
        else if (val instanceof VncString) {
            return StringUtil.trimToNull(((VncString)val).getValue());
        }
        else {
            throw new RuntimeException(
                    "The REPL client config field '" + field + "' must be a string value");
        }
    }

    private static int toInt(final String field, final VncVal val, final int defaultValue) {
        if (val == Constants.Nil) {
            return defaultValue;
        }
        else if (val instanceof VncLong) {
            return ((VncLong)val).getIntValue();
        }
        else {
            throw new RuntimeException(
                    "The REPL client config field '" + field + "' must be a integer value");
        }
    }


    private final String host;
    private final int port;
    private final String password;
    private final String clientPublicKeyFile;
    private final String clientPrivateKeyFile;
    private final String serverPublicKeyFile;
}
