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


public class ReplRemotingConfig {

    public ReplRemotingConfig(
            final String host,
            final int port,
            final String password
    ) {
        this.host = host;
        this.port = port;
        this.password = password;
        this.encrypt = true;
        this.compress = false;
        this.sessionTimeoutMinutes = 30;
        this.signKeys = false;
        this.serverPublicKeyFile = null;
        this.serverPrivateKeyFile = null;
        this.clientPublicKeyFile = null;
        this.clientPrivateKeyFile = null;
    }

    public ReplRemotingConfig(
            final String host,
            final int port,
            final String password,
            final boolean encrypt,
            final boolean compress,
            final int sessionTimeoutMinutes,
            final boolean signKeys,
            final String serverPublicKeyFile,
            final String serverPrivateKeyFile,
            final String clientPublicKeyFile,
            final String clientPrivateKeyFile
    ) {
        this.host = host;
        this.port = port;
        this.password = password;
        this.encrypt = encrypt;
        this.compress = compress;
        this.sessionTimeoutMinutes = sessionTimeoutMinutes;
        this.signKeys = signKeys;
        this.serverPublicKeyFile = serverPublicKeyFile;
        this.serverPrivateKeyFile = serverPrivateKeyFile;
        this.clientPublicKeyFile = clientPublicKeyFile;
        this.clientPrivateKeyFile = clientPrivateKeyFile;
    }



    public String getHost() {
        return host;
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

    public boolean isSignKeys() {
        return signKeys;
    }

    public String getServerPublicKeyFile() {
        return signKeys ? serverPublicKeyFile : null;
    }

    public String getServerPrivateKeyFile() {
        return signKeys ? serverPrivateKeyFile : null;
    }

    public String getClientPublicKeyFile() {
        return signKeys ? clientPublicKeyFile : null;
    }

    public String getClientPrivateKeyFile() {
        return signKeys ? clientPrivateKeyFile : null;
    }


    @Override
    public String toString() {
        return "ReplRemotingConfig [\n"
                + "host=" + host + ", \n"
                + "port=" + port + ", \n"
                + "encrypt=" + encrypt + ", \n"
                + "compress=" + compress + ", \n"
                + "password=" + password + ", \n"
                + "sessionTimeoutMinutes=" + sessionTimeoutMinutes + ", \n"
                + "signKeys=" + signKeys + ", \n"
                + "serverPublicKeyFile=" + serverPublicKeyFile + ", \n"
                + "serverPrivateKeyFile=" + serverPrivateKeyFile + ", \n"
                + "clientPublicKeyFile=" + clientPublicKeyFile + ", \n"
                + "clientPrivateKeyFile=" + clientPrivateKeyFile + "\n"
                + "]";
    }

    public ReplRemotingConfig with(
            final String host,
            final int port,
            final String password
    ) {
        return new ReplRemotingConfig(
                host,
                port,
                password,
                this.encrypt,
                this.compress,
                this.sessionTimeoutMinutes,
                this.signKeys,
                this.serverPublicKeyFile,
                this.serverPrivateKeyFile,
                this.clientPublicKeyFile,
                this.clientPrivateKeyFile);
    }

    public static ReplRemotingConfig of(final String jsonConfig) {
        Objects.requireNonNull(jsonConfig);

        final VncMap map = (VncMap)Json.readJson(jsonConfig, false);

        return new ReplRemotingConfig(
                toString("host", map.get(new VncString("host"), new VncString("localhost"))),
                toInt("port", map.get(new VncString("port")), 0),
                toString("password", map.get(new VncString("password"), Constants.Nil)),
                toBool("encrypt", map.get(new VncString("encrypt"), VncBoolean.True)),
                toBool("compress", map.get(new VncString("compress"), VncBoolean.False)),
                toInt("sessionTimeoutMinutes", map.get(new VncString("sessionTimeoutMinutes")), 30),
                toBool("signKeys", map.get(new VncString("signKeys"), VncBoolean.False)),
                toString("serverPublicKeyFile", map.get(new VncString("serverPublicKeyFile"))),
                toString("serverPrivateKeyFile", map.get(new VncString("serverPrivateKeyFile"))),
                toString("clientPublicKeyFile", map.get(new VncString("clientPublicKeyFile"))),
                toString("clientPrivateKeyFile", map.get(new VncString("clientPrivateKeyFile"))));
    }

    public static ReplRemotingConfig load(final File jsonConfig) {
        Objects.requireNonNull(jsonConfig);

        try {
            return ReplRemotingConfig.of(
                       new String(FileUtil.load(jsonConfig), StandardCharsets.UTF_8));
        }
        catch (Exception ex) {
            throw new RuntimeException("Failed to load REPL remoting config from file " + jsonConfig, ex);
        }
    }

    public static ReplRemotingConfig load(final InputStream is) {
        Objects.requireNonNull(is);

        try {
            return ReplRemotingConfig.of(
                    new String(IOStreamUtil.copyIStoByteArray(is), StandardCharsets.UTF_8));
        }
        catch (Exception ex) {
            throw new RuntimeException("Failed to load REPL remoting config from stream.", ex);
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
                    "The REPL remoting config field '" + field + "' must be a string value");
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
                    "The REPL remoting config field '" + field + "' must be a integer value");
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
                    "The REPL remoting config field '" + field + "' must be a boolean value");
        }
    }


    private final String host;
    private final int port;
    private final boolean encrypt;
    private final boolean compress;
    private final String password;
    private final int sessionTimeoutMinutes;
    private final boolean signKeys;
    private final String serverPublicKeyFile;
    private final String serverPrivateKeyFile;
    private final String clientPublicKeyFile;
    private final String clientPrivateKeyFile;
}
