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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

// https://stackoverflow.com/questions/62313791/replacing-the-manifest-mf-file-in-a-jar-programmatically

public class JarRewriter {

    /**
     * Add or replace entries in an existing JAR
     *
     * @param existingJar bytes of a .jar file (may be null or empty to behave like create)
     * @param manifest an optional new manifest if <code>null</code> the existing will be copied.
     * @param additions map of "path/inside.jar" -> bytes; paths use forward slashes.
     * @return the modified jar
     * @throws IOException
     */
    public static byte[] addToJar(
            final byte[] existingJar,
            final Manifest manifest,
            final Map<String, byte[]> additions
    ) throws IOException {
        if (existingJar == null || existingJar.length == 0) {
            return createJar(additions, null);
        }

        // Collect additions with normalized names for quick lookup
        final Map<String, byte[]> toAdd = new HashMap<>();
        for (Map.Entry<String, byte[]> e : additions.entrySet()) {
            toAdd.put(normalize(e.getKey()), e.getValue());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream(existingJar.length + 16 * 1024);
        try (JarInputStream jis = new JarInputStream(new ByteArrayInputStream(existingJar));
             JarOutputStream jos = manifest == null
                                    ? copyManifestFirstIfPresent(jis, out)
                                    : replaceManifestFirst(manifest, jis, out)) {

            long fixedTime = 0L;
            jos.setLevel(Deflater.DEFAULT_COMPRESSION);

            // Copy over existing entries except those being replaced
            JarEntry in;
            final Set<String> written = new HashSet<>();
            while ((in = jis.getNextJarEntry()) != null) {
                String name = normalize(in.getName());

                // Skip directories (we'll recreate as needed) and anything we plan to replace
                if (in.isDirectory() || toAdd.containsKey(name)) {
                    continue;
                }

                final JarEntry outEntry = new JarEntry(name);
                outEntry.setTime(fixedTime);
                // You could copy extra fields if needed:
                // outEntry.setComment(in.getComment()); etc.
                jos.putNextEntry(outEntry);
                copy(jis, jos);
                jos.closeEntry();

                written.add(name);
            }

            // Ensure directory entries for new files
            final Set<String> dirs = new HashSet<>();
            for (String path : toAdd.keySet()) {
                int slash = path.lastIndexOf('/');
                while (slash > 0) {
                    final String dir = path.substring(0, slash + 1);
                    if (dirs.add(dir) && !written.contains(dir)) {
                        final JarEntry de = new JarEntry(dir);
                        de.setTime(fixedTime);
                        jos.putNextEntry(de);
                        jos.closeEntry();
                    }
                    slash = path.lastIndexOf('/', slash - 1);
                }
            }

            // Write additions
            for (Map.Entry<String, byte[]> e : toAdd.entrySet()) {
                final String path = e.getKey();
                final JarEntry je = new JarEntry(path);
                je.setTime(fixedTime);
                jos.putNextEntry(je);
                jos.write(e.getValue());
                jos.closeEntry();
            }
        }
        return out.toByteArray();
    }


    /**
     * Create a new JAR in memory
     *
     * @param entries the jar entries
     * @param manifest an optional manifest
     * @return the created jar
     * @throws IOException
     */
    public static byte[] createJar(
            final Map<String, byte[]> entries,
            final Manifest manifest
    ) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream(64 * 1024);

        final JarOutputStream jos = manifest != null
                                        ? new JarOutputStream(out, manifest)
                                        : new JarOutputStream(out);

        // Optional: make output deterministic
        jos.setLevel(Deflater.DEFAULT_COMPRESSION);
        long fixedTime = 0L; // 1970-01-01; use an Instant epoch millis if you want reproducible builds

        // Ensure directory entries exist (JAR/ZIP readers generally don't require them,
        // but some tools like to see them).
        final Set<String> dirs = new HashSet<>();
        for (String path : entries.keySet()) {
            int slash = path.lastIndexOf('/');
            while (slash > 0) {
                final String dir = path.substring(0, slash + 1);
                if (dirs.add(dir)) {
                    final JarEntry de = new JarEntry(dir);
                    de.setTime(fixedTime);
                    jos.putNextEntry(de);
                    jos.closeEntry();
                }
                slash = path.lastIndexOf('/', slash - 1);
            }
        }

        for (Map.Entry<String, byte[]> e : entries.entrySet()) {
            final String path = normalize(e.getKey());
            final JarEntry je = new JarEntry(path);
            je.setTime(fixedTime);
            jos.putNextEntry(je);
            jos.write(e.getValue());
            jos.closeEntry();
        }

        jos.close();
        return out.toByteArray();
    }

    public static Manifest manifest(
            final String appName,
            final String version,
            final String mainClass
    ) {
        final String now = LocalDateTime
                            .now()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

        final Manifest mf = new Manifest();
        mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        mf.getMainAttributes().put(Attributes.Name.SPECIFICATION_TITLE, appName);
        mf.getMainAttributes().put(Attributes.Name.SPECIFICATION_VERSION, version);
        mf.getMainAttributes().put(Attributes.Name.MAIN_CLASS, mainClass);
        mf.getMainAttributes().put("Application-Name", appName);
        mf.getMainAttributes().put("Build-Date", now);
        return mf;
    }


    public static byte[] demo() throws IOException {
        // 1) Create a fresh JAR with a manifest and a single class/resource
        Manifest mf = new Manifest();
        mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        mf.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "example.App");

        Map<String, byte[]> initial = new HashMap<>();
        initial.put("example/App.class", new byte[]{(byte)0xCA, (byte)0xFE, (byte)0xBA, (byte)0xBE}); // placeholder bytes
        initial.put("config/app.properties", "env=dev\n".getBytes(StandardCharsets.UTF_8));

        byte[] jar = createJar(initial, mf);

        // 2) Add (or replace) a file entirely in memory
        Map<String, byte[]> additions = new HashMap<>();
        additions.put("README.txt", "Hello JAR!\n".getBytes(StandardCharsets.UTF_8));
        additions.put("config/app.properties", "env=prod\n".getBytes(StandardCharsets.UTF_8));

        byte[] updatedJar = addToJar(jar, null, additions);

        // Optional: quick sanity check — list entries
        try (ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(updatedJar))) {
            System.out.println("JAR entries:");
            ZipEntry ze;
            while ((ze = zin.getNextEntry()) != null) {
                System.out.println(" - " + ze.getName());
            }
        }

        return updatedJar;
    }


    private static String normalize(final String path) {
        String p = path.replace('\\', '/');
        if (p.startsWith("/")) p = p.substring(1);
        return p;
    }

    private static void copy(
            final InputStream in,
            final OutputStream out
    ) throws IOException {
        final byte[] buf = new byte[8192];
        int r;
        while ((r = in.read(buf)) != -1) {
            out.write(buf, 0, r);
        }
    }

    private static JarOutputStream copyManifestFirstIfPresent(
            final JarInputStream jis,
            final ByteArrayOutputStream out
    ) throws IOException {
        final Manifest mf = jis.getManifest();
        return mf != null
                ? new JarOutputStream(out, mf)
                : new JarOutputStream(out);
    }

    private static JarOutputStream replaceManifestFirst(
            final Manifest manifest,
            final JarInputStream jis,
            final ByteArrayOutputStream out
    ) throws IOException {
        return manifest != null
                ? new JarOutputStream(out, manifest)
                : new JarOutputStream(out);
    }


}
