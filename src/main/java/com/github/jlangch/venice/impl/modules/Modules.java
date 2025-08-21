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
package com.github.jlangch.venice.impl.modules;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.github.jlangch.venice.impl.types.VncKeyword;


public class Modules {

    public static boolean isValidModule(final String module) {
        return VALID_MODULES.contains(module);
    }

    public static boolean isValidModule(final VncKeyword module) {
        return isValidModule(module.getValue());
    }


    public static final Set<String> VALID_MODULES =
            Collections.unmodifiableSet(
                new HashSet<>(
                    Arrays.asList(
                        "installer",

                        "bouncycastle-install",
                        "docx4j-8-install",
                        "excel-install",
                        "ivy-install",
                        "jansi-install",
                        "jtokkit-install",
                        "langchain-install",
                        "pdf-install",
                        "postgresql-jdbc-install",
                        "qdrant-client-install",
                        "qrbill-install",
                        "qrcode-install",
                        "tomcat-install",
                        "xchart-install",
                        "yaml-install",

                        "ansi",
                        "app",
                        "ascii-table",
                        "ascii-canvas",
                        "ascii-charts",
                        "aviron",
                        "aviron-limiter",
                        "aviron-queue",
                        "aviron-cycler",
                        "aviron-demo-filestore",
                        "benchmark",
                        "cargo",
                        "cargo-arangodb",
                        "cargo-qdrant",
                        "cargo-mysql",
                        "cargo-postgresql",
                        "chinook-postgresql",
                        "cli",
                        "clipboard",
                        "config",
                        "core",
                        "component",
                        "crypt",
                        "dag",
                        "docker",
                        "docx",
                        "docx4j-8",
                        "excel",
                        "esr",
                        "fam",
                        "fonts",
                        "geoip",
                        "gradle",
                        "gradlew",
                        "grep",
                        "hexdump",
                        "http-client",
                        "http-client-j8",
                        "images",
                        "ivy",
                        "jdbc",
                        "jdbc-core",
                        "jdbc-postgresql",
                        "jackson",
                        "java",
                        "jetty",
                        "jsonl",
                        "jtokkit",
                        "keystores",
                        "kira",
                        "langchain",
                        "logger",
                        "math",
                        "matrix",
                        "maven",
                        "mercator",
                        "mimetypes",
                        "multipart",
                        "openai",
                        "openai-demo",
                        "parsifal",
                        "postgresql-jdbc",
                        "pretty-print",
                        "qrbill",
                        "qrref",
                        "qrcode",
                        "repl-setup",
                        "ring",
                        "ring-mw",
                        "ring-multipart",
                        "ring-session",
                        "ring-util",
                        "semver",
                        "server-side-events",
                        "shell",
                        "stopwatch",
                        "test",
                        "test-support",
                        "timing",
                        "tomcat",
                        "tomcat-util",
                        "tput",
                        "trace",
                        "utf8",
                        "webdav",
                        "xchart",
                        "xml",
                        "yaml",
                        "zipvault")));

    public static final Set<VncKeyword> NATIVE_MODULES =
            Collections.unmodifiableSet(
                new HashSet<>(
                    Arrays.asList(
                        new VncKeyword("sandbox"),
                        new VncKeyword("math"),
                        new VncKeyword("str"),
                        new VncKeyword("regex"),
                        new VncKeyword("time"),
                        new VncKeyword("io"),
                        new VncKeyword("json"),
                        new VncKeyword("pdf"),
                        new VncKeyword("inet"),
                        new VncKeyword("cidr"),
                        new VncKeyword("csv"),
                        new VncKeyword("cron"),
                        new VncKeyword("mbean"))));
}
