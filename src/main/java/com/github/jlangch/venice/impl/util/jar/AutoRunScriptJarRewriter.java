/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2025 Venice
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
package com.github.jlangch.venice.impl.util.jar;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Manifest;

import com.github.jlangch.venice.Launcher;

import joptsimple.internal.Objects;

// https://stackoverflow.com/questions/62313791/replacing-the-manifest-mf-file-in-a-jar-programmatically

public class AutoRunScriptJarRewriter {

    /**
     * Add or replace entries in an existing JAR
     *
     * @param existingJar bytes of a .jar file (may be null or empty to behave like create)
     * @param additions map of "path/inside.jar" -> bytes; paths use forward slashes.
     * @return the modified jar
     * @throws IOException
     */
    public static byte[] makeAutoRunVeniceJar(
            final byte[] existingVeniceJar,
            final String scriptName,
            final String scriptVersion,
            final String script
    ) throws IOException {
        Objects.ensureNotNull(existingVeniceJar);
        Objects.ensureNotNull(scriptName);
        Objects.ensureNotNull(scriptVersion);
        Objects.ensureNotNull(script);

        final Map<String, byte[]> additions = new HashMap<>();
        additions.put("auto/" + scriptName + ".venice", script.getBytes(StandardCharsets.UTF_8));

        final Manifest manifest = JarRewriter.manifest(scriptName, scriptVersion, Launcher.class.getName());

        return JarRewriter.addToJar(existingVeniceJar, manifest, additions);
    }

}
