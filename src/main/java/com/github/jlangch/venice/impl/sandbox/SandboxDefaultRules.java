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
package com.github.jlangch.venice.impl.sandbox;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.jlangch.venice.Venice;


public class SandboxDefaultRules {

    private static final String BASE = Venice.class.getPackage().getName();

	public static final List<String> SYSTEM_CLASS_RULES =
            Arrays.asList(
                "class:" + BASE + ".*Exception:*",
                "class:" + BASE + ".*Error:*",
                "class:" + BASE + ".IServiceDiscovery:*",

                "class:" + BASE + ".impl.repl.ReplPrintStream:*",
                "class:" + BASE + ".util.CapturingPrintStream:*",
                "class:" + BASE + ".util.CallbackPrintStream:*",
                "class:" + BASE + ".util.*XMLHandler*:*",

                "class:" + BASE + ".util.CapturingPrintStream:*",
                "class:" + BASE + ".util.ImmutableServiceDiscovery:*",

                "class:" + BASE + ".servlet.*:*",

                // Dynamic proxies based on venice' DynamicInvocationHandler
                "class:" + BASE + ".impl.javainterop.DynamicInvocationHandler*:*",

                "class:" + BASE + ".impl.VeniceInterpreter$1",
                "class:" + BASE + ".impl.types.collections.VncVector",

                "class:" + BASE + ".impl.types.concurrent.Delay:*",
                "class:" + BASE + ".impl.types.concurrent.Agent:*",

                // Excel adapter classes
                "class:" + BASE + ".util.excel.*:*",

                // Venice dynamic proxies
                "class:com.sun.proxy.$Proxy*:*",  // Java 8,11
                "class:jdk.proxy.$Proxy*:*",      // Java 17
                "class:jdk.proxy1.$Proxy*:*",     // Java 17
                "class:jdk.proxy2.$Proxy*:*",     // Java 17
                "class:jdk.proxy3.$Proxy*:*");    // Java 17

    public static final List<String> DEFAULT_CLASS_RULES =
            Arrays.asList(
                "class:java.lang.IllegalArgumentException:*",
                "class:java.lang.RuntimeException:*",
                "class:java.lang.Exception:*",
                "class:java.lang.SecurityException:*",
                "class:java.io.IOException:*",

                "class:java.io.BufferedReader:*",
                "class:java.io.BufferedWriter:*",
                "class:java.io.ByteArrayInputStream:*",
                "class:java.io.ByteArrayOutputStream:*",
                "class:java.io.InputStream:*",
                "class:java.io.InputStreamReader:*",
                "class:java.io.OutputStream:*",
                "class:java.io.OutputStreamWriter:*",
                "class:java.io.PrintStream:*",
                "class:java.io.PrintWriter:*",
                "class:java.io.Reader:*",
                "class:java.io.StringReader:*",
                "class:java.io.StringWriter:*",
                "class:java.io.Writer:*",

                "class:java.lang.Object",
                "class:java.lang.Object:class",

                "class:java.lang.Character",
                "class:java.lang.String",
                "class:java.lang.Boolean",
                "class:java.lang.Integer",
                "class:java.lang.Long",
                "class:java.lang.Float",
                "class:java.lang.Double",
                "class:java.lang.Byte",
                "class:java.lang.StringBuilder",

                "class:java.time.ZonedDateTime:*",
                "class:java.time.LocalDateTime:*",
                "class:java.time.LocalDate:*",

                "class:java.math.BigInteger",
                "class:java.math.BigDecimal",

                "class:java.nio.ByteBuffer",
                "class:java.nio.HeapByteBuffer:*",

                "class:java.util.ArrayList:*",
                "class:java.util.Collection:*",
                "class:java.util.Date:*",
                "class:java.util.HashMap:*",
                "class:java.util.HashSet:*",
                "class:java.util.LinkedHashMap:*",
                "class:java.util.List:*",
                "class:java.util.Locale:*",
                "class:java.util.Map:*",
                "class:java.util.Set:*",

                "class:java.util.concurrent.Semaphore:*",
                "class:java.util.concurrent.CountDownLatch:*",
                "class:java.util.concurrent.CyclicBarrier:*",
                "class:java.util.concurrent.locks.*:*");

    public static final Set<String> DEFAULT_SYSTEM_PROPERTIES =
            Collections.unmodifiableSet(
                new HashSet<>(
                    Arrays.asList(
                        "file.separator",
                        "java.home",
                        "java.vendor",
                        "java.vendor.url",
                        "java.version",
                        "line.separator",
                        "os.arch",
                        "os.name",
                        "os.version",
                        "path.separator",
                        "user.dir",
                        "user.home",
                        "user.name")));

    public static final Set<String> DEFAULT_SYSTEM_ENVS =
            Collections.unmodifiableSet(
                new HashSet<>());

    public static final Set<String> DEFAULT_WHITELISTED_MODULES =
            Collections.unmodifiableSet(
                new HashSet<>(
                    Arrays.asList(
                        "crypt",
                        "kira",
                        "mimetypes",
                        "parsifal",
                        "test",
                        "test-support",
                        "hexdump",
                        "xml")));

}
