/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2023 Venice
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
                        "ansi",
                        "app",
                        "benchmark",
                        "cli",
                        "config",
                        "core",
                        "component",
                        "crypt",
                        "dag",
                        "docx",
                        "excel",
                        "excel-install",
                        "esr",
                        "fam",
                        "fonts",
                        "geoip",
                        "gradle",
                        "grep",
                        "hexdump",
                        "http",
                        "jackson",
                        "java",
                        "kira",
                        "math",
                        "maven",
                        "mercator",
                        "parsifal",
                        "pdf-install",
                        "qrbill",
                        "qrbill-install",
                        "repl-setup",
                        "ring",
                        "semver",
                        "shell",
                        "test",
                        "test-support",
                        "tomcat",
                        "tomcat-install",
                        "tomcat-util",
                        "tput",
                        "trace",
                        "webdav",
                        "xchart",
                        "xchart-install",
                        "xml")));

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
                        new VncKeyword("csv"))));
}
