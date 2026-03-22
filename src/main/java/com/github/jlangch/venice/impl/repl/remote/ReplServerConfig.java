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
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.io.FileUtil;
import com.github.jlangch.venice.impl.util.io.IOStreamUtil;
import com.github.jlangch.venice.util.ipc.impl.util.Json;


public class ReplServerConfig {

    public ReplServerConfig(
            final int port,
            final String password,
            final boolean encrypt,
            final boolean compress,
            final int sessionTimeoutMinutes,
            final String serverPublicKeyFile,
            final String serverPrivateKeyFile,
            final String clientPublicKeyFile
    ) {
        this.port = port;
        this.password = password;
        this.encrypt = encrypt;
        this.compress = compress;
        this.sessionTimeoutMinutes = sessionTimeoutMinutes;
        this.serverPublicKeyFile = serverPublicKeyFile;
        this.serverPrivateKeyFile = serverPrivateKeyFile;
        this.clientPublicKeyFile = clientPublicKeyFile;
    }


    public int getPort() {
        return port;
    }

    public boolean isEncrypt() {
        return encrypt;
    }

    public boolean isCompress() {
        return compress;
    }

    public String getPassword() {
        return password;
    }

    public int getSessionTimeoutMinutes() {
        return sessionTimeoutMinutes;
    }

    public String getServerPublicKeyFile() {
        return serverPublicKeyFile;
    }

    public String getServerPrivateKeyFile() {
        return serverPrivateKeyFile;
    }

    public String getClientPublicKeyFile() {
        return clientPublicKeyFile;
    }


    public static ReplServerConfig of(final String jsonConfig) {
        Objects.requireNonNull(jsonConfig);

        final VncMap map = (VncMap)Json.readJson(jsonConfig, false);

        return new ReplServerConfig(
                toInt("port", map.get(new VncString("port")), 0),
                toString("password", map.get(new VncString("password"), Constants.Nil)),
                toBool("encrypt", map.get(new VncString("encrypt"), VncBoolean.True)),
                toBool("compress", map.get(new VncString("compress"), VncBoolean.False)),
                toInt("sessionTimeoutMinutes", map.get(new VncString("sessionTimeoutMinutes")), 30),
                toString("serverPublicKeyFile", map.get(new VncString("serverPublicKeyFile"))),
                toString("serverPrivateKeyFile", map.get(new VncString("serverPrivateKeyFile"))),
                toString("clientPublicKeyFile", map.get(new VncString("clientPublicKeyFile"))));
    }

    public static ReplServerConfig load(final File jsonConfig) {
        Objects.requireNonNull(jsonConfig);

        try {
            return ReplServerConfig.of(
                       new String(FileUtil.load(jsonConfig), StandardCharsets.UTF_8));
        }
        catch (Exception ex) {
            throw new RuntimeException("Failed to load REPL server config from file " + jsonConfig, ex);
        }
    }

    public static ReplServerConfig load(final InputStream is) {
        Objects.requireNonNull(is);

        try {
            return ReplServerConfig.of(
                    new String(IOStreamUtil.copyIStoByteArray(is), StandardCharsets.UTF_8));
        }
        catch (Exception ex) {
            throw new RuntimeException("Failed to load REPL server config from stream.", ex);
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
                    "The REPL server config field '" + field + "' must be a string value");
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
                    "The REPL server config field '" + field + "' must be a integer value");
        }
    }

    private static boolean toBool(final String field, final VncVal val) {
        if (val == Constants.Nil) {
            return false;
        }
        else if (val instanceof VncBoolean) {
            return ((VncBoolean)val).getValue();
        }
        else {
            throw new RuntimeException(
                    "The REPL server config field '" + field + "' must be a boolean value");
        }
    }


    private final int port;
    private final boolean encrypt;
    private final boolean compress;
    private final String password;
    private final int sessionTimeoutMinutes;
    private final String serverPublicKeyFile;
    private final String serverPrivateKeyFile;
    private final String clientPublicKeyFile;
}
