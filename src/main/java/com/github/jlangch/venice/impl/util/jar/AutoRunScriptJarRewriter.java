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

import static com.github.jlangch.venice.impl.util.StringUtil.trimToEmpty;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.jar.Manifest;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.AutoRunScriptLauncher;



public class AutoRunScriptJarRewriter {

    /**
     * Create an auto-run Venice JAR
     *
     * @param existingJar bytes of a .jar. Maybe <code>null</code> in which case
     *                    this JAR Venice will be used
     * @param scriptName the script name. E.g.: "avscan"
     * @param scriptVersion the script version. E.g.: "1.0"
     * @param script the script
     * @return the jar
     * @throws VncException on errors
     */
    public static byte[] createAutoRunVeniceJar(
            final byte[] existingVeniceJar,
            final String scriptName,
            final String scriptVersion,
            final String script
    ) {
        Objects.requireNonNull(script);

        if (!trimToEmpty(scriptName).matches("[a-zA-Z0-9-_]+")) {
            throw new VncException(
                    "A script name must only contain the characters a-z, A-Z, 0-9, '_', or '-'");
        }
        if (!trimToEmpty(scriptVersion).matches("[a-zA-Z0-9-_.]+")) {
            throw new VncException(
                    "A script name must only contain the characters a-z, A-Z, 0-9, '_', '-', or '.'");
        }

        final byte[] veniceJar = existingVeniceJar != null
                                    ? existingVeniceJar
                                    : loadThisVeniceJar();

        final String scriptMeta = String.format(
                                    "script-name=%s\nscript-version=%s\n",
                                    scriptName,
                                    scriptVersion);

        final Map<String, byte[]> additions = new HashMap<>();
        additions.put(AUTORUN_SCRIPT_PATH, AutoRunScript.obfuscate(script));
        additions.put(AUTORUN_META_PATH, scriptMeta.getBytes(StandardCharsets.UTF_8));

        try {
            final Manifest manifest = JarRewriter.manifest(
                                        scriptName,
                                        scriptVersion,
                                        AutoRunScriptLauncher.class.getName());
            return JarRewriter.addToJar(veniceJar, manifest, additions);
        }
        catch(Exception ex) {
            throw new VncException("Failed to create the JAR", ex);
        }
    }


    /**
     * Create an auto-run Venice JAR and save it to a directory.
     *
     * @param existingJar bytes of a .jar. Maybe <code>null</code> in which case
     *                    this JAR Venice will be used
     * @param scriptName the script name. E.g.: "avscan"
     * @param scriptVersion the script version. E.g.: "1.0"
     * @param script the script
     * @param saveTo the destination directory
     * @return the path of the saved JAR
     * @throws VncException on errors
     */
    public static Path createAndSaveAutoRunVeniceJar(
            final byte[] existingVeniceJar,
            final String scriptName,
            final String scriptVersion,
            final String script,
            final Path saveTo
    ) {
        Objects.requireNonNull(saveTo);
        if (!saveTo.toFile().isDirectory()) {
            throw new VncException(
                    "The destination directory '" + saveTo.toString() + "' does not exist!");
        }

        final byte[] veniceJar = existingVeniceJar != null
                                    ? existingVeniceJar
                                    : loadThisVeniceJar();

        return saveTo(
                createAutoRunVeniceJar(veniceJar, scriptName, scriptVersion, script),
                scriptName,
                saveTo).normalize().toAbsolutePath();
    }


    public static byte[] loadThisVeniceJar() {
        try {
            final URI uri = AutoRunScriptJarRewriter
                                 .class
                                 .getProtectionDomain()
                                 .getCodeSource()
                                 .getLocation()
                                 .toURI();

            return Files.readAllBytes(new File(uri).toPath());
        }
        catch(Exception ex) {
            throw new VncException("Failed to load Venice JAR!", ex);
        }
    }


    private static Path saveTo(
            final byte[] jar,
            final String scriptName,
            final Path saveTo
    ) {
        try {
            final Path dest = Paths.get(saveTo.toString(), scriptName + ".jar");
            Files.write(dest, jar);
            return dest;
        }
        catch(Exception ex) {
            throw new VncException("Failed to save new JAR!", ex);
        }
    }


    private static final String BASE_PATH = "com/github/jlangch/venice/auto/";

    public static final String AUTORUN_SCRIPT_PATH = BASE_PATH + "autorun.data";
    public static final String AUTORUN_META_PATH = BASE_PATH + "autorun.meta";
}
