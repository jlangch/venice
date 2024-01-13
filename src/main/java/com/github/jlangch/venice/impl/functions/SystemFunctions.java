/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
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
package com.github.jlangch.venice.impl.functions;

import static com.github.jlangch.venice.impl.types.Constants.Nil;
import static com.github.jlangch.venice.impl.types.VncBoolean.False;
import static com.github.jlangch.venice.impl.util.reflect.ReflectionAccessor.classForName;
import static com.github.jlangch.venice.impl.util.reflect.ReflectionAccessor.invokeInstanceMethod;
import static com.github.jlangch.venice.impl.util.reflect.ReflectionAccessor.invokeStaticMethod;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.javainterop.DynamicClassLoader2;
import com.github.jlangch.venice.impl.thread.ThreadBridge;
import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.threadpool.GlobalThreadFactory;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncOrderedMap;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;
import com.github.jlangch.venice.impl.util.callstack.CallFrame;
import com.github.jlangch.venice.impl.util.callstack.CallStack;
import com.github.jlangch.venice.javainterop.ReturnValue;
import com.github.jlangch.venice.util.OS;


public class SystemFunctions {

    ///////////////////////////////////////////////////////////////////////////
    // System
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction pid =
        new VncFunction(
                "pid",
                VncFunction
                    .meta()
                    .arglists("(pid)")
                    .doc("Returns the PID of this process.")
                    .examples("(pid)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                if (javaMajorVersion() <= 8) {
                    return new VncString(
                                ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
                }
                else {
                    // Java 9+  -> ProcessHandle.current().pid()
                    final ReturnValue procHandle = invokeStaticMethod(
                                                        classForName("java.lang.ProcessHandle"),
                                                        "current",
                                                        new Object[]{});

                    final ReturnValue ret = invokeInstanceMethod(
                                                procHandle.getValue(),
                                                procHandle.getFormalType(),
                                                "pid",
                                                new Object[]{});

                    return new VncLong((Long)ret.getValue());
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction byte_order =
        new VncFunction(
                "byte-order",
                VncFunction
                    .meta()
                    .arglists("(byte-order)")
                    .doc("Returns the CPU's byte order.")
                    .examples("(byte-order)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                final String order = ByteBuffer.allocate(2).order().toString();
                switch(order) {
                    case "BIG_ENDIAN": return new VncKeyword(":big-endian");
                    case "LITTLE_ENDIAN": return new VncKeyword(":little-endian");
                    default: return new VncKeyword(":little-endian");
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction host_name =
        new VncFunction(
                "host-name",
                VncFunction
                    .meta()
                    .arglists("(host-name)")
                    .doc("Returns this host's name.")
                    .examples("(host-name)")
                    .seeAlso("host-address")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                try {
                    return new VncString(
                            InetAddress.getLocalHost().getHostName());
                }
                catch(Exception ex) {
                    throw new VncException("(host-name) failed", ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction host_address =
        new VncFunction(
                "host-address",
                VncFunction
                    .meta()
                    .arglists("(host-address)")
                    .doc("Returns this host's ip address.")
                    .examples("(host-address)")
                    .seeAlso("host-name")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                try {
                    return new VncString(
                            InetAddress.getLocalHost().getHostAddress());
                }
                catch(Exception ex) {
                    throw new VncException("(host-address) failed", ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction user_name =
        new VncFunction(
                "user-name",
                VncFunction
                    .meta()
                    .arglists("(user-name)")
                    .doc("Returns the logged-in's user name.")
                    .examples("(user-name)")
                    .seeAlso("io/user-home-dir")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                try {
                    return new VncString(
                            System.getProperty("user.name"));
                }
                catch(Exception ex) {
                    throw new VncException("(user-name) failed", ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction ip_private_Q =
        new VncFunction(
                "ip-private?",
                VncFunction
                    .meta()
                    .arglists("(ip-private? addr)")
                    .doc(
                        "Returns true if the IP address is private. \n\n" +
                        "IPv4 addresses reserved for private networks:\n\n" +
                        " * 192.168.0.0 - 192.168.255.255\n" +
                        " * 172.16.0.0 - 172.31.255.255\n" +
                        " * 10.0.0.0 - 10.255.255.255\n")
                    .examples("(ip-private? \"192.168.170.181\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                try {
                    return VncBoolean.of(
                        InetAddress
                            .getByName(Coerce.toVncString(args.first()).getValue())
                            .isSiteLocalAddress());
                }
                catch(Exception ex) {
                    throw new VncException("function ip-private? failed", ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction gc =
        new VncFunction(
                "gc",
                VncFunction
                    .meta()
                    .arglists("(gc)")
                    .doc(
                        "Run the Java garbage collector. Runs the finalization methods " +
                        "of any objects pending finalization prior to the GC.")
                    .examples("(gc)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                sandboxFunctionCallValidation();

                Runtime.getRuntime().runFinalization();
                Runtime.getRuntime().gc();

                return Nil;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction cpus =
        new VncFunction(
                "cpus",
                VncFunction
                    .meta()
                    .arglists("(cpus)")
                    .doc(
                        "Returns the number of available processors or number of " +
                        "hyperthreads if the CPU supports hyperthreads.")
                    .examples("(cpus)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                return new VncLong(Runtime.getRuntime().availableProcessors());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction shutdown_hook =
        new VncFunction(
                "shutdown-hook",
                VncFunction
                    .meta()
                    .arglists("(shutdown-hook f)")
                    .doc(
                        "Registers the function f as a JVM shutdown hook.\n\n" +
                        "Shutdown hooks can be tested in a REPL:\n\n" +
                        "  * start a REPL\n" +
                        "  * run `(shutdown-hook (fn [] (println \"SHUTDOWN\") (sleep 3000)))`\n" +
                        "  * exit the REPL with `!exit`\n\n" +
                        "The sandbox is active within the shutdown hook:\n\n" +
                        "  * start a REPL\n" +
                        "  * run `!sandbox customized`\n" +
                        "  * run `!sandbox add-rule blacklist:venice:func:+`\n" +
                        "  * run `(shutdown-hook (fn [] (try (+ 1 2) (catch :SecurityException ex (println ex) (sleep 3000)))))`\n" +
                        "  * exit the REPL with `!exit`")
                    .examples(
                        "(shutdown-hook (fn [] (println \"shutdown\")))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                sandboxFunctionCallValidation();

                final VncFunction fn = Coerce.toVncFunction(args.first());
                fn.sandboxFunctionCallValidation();

                // Create a wrapper that inherits the Venice thread context
                // from the parent thread to the executer thread!
                final ThreadBridge threadBridge = ThreadBridge.create(
                                                    "shutdown-hook",
                                                    new CallFrame[] {
                                                        new CallFrame(this, args),
                                                        new CallFrame(fn)});
                final Runnable taskWrapper = threadBridge.bridgeRunnable(() -> fn.applyOf());

                final Thread hook = GlobalThreadFactory.newThread("shutdown-hook", taskWrapper);
                hook.setUncaughtExceptionHandler(ThreadBridge::handleUncaughtException);

                Runtime.getRuntime().addShutdownHook(hook);

                return Nil;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction callstack =
        new VncFunction(
                "callstack",
                VncFunction
                    .meta()
                    .arglists("(callstack)")
                    .doc("Returns the current callstack.")
                    .examples(
                        "(do                             \n" +
                        "   (defn f1 [x] (f2 x))         \n" +
                        "   (defn f2 [x] (f3 x))         \n" +
                        "   (defn f3 [x] (f4 x))         \n" +
                        "   (defn f4 [x] (callstack))    \n" +
                        "   (f1 100))                      ")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                final CallStack stack = ThreadContext.getCallStack();

                return VncVector.ofList(
                        stack
                            .callstack()
                            .stream()
                            .map(f -> VncOrderedMap.of(
                                            CALLSTACK_KEY_FN_NAME, f.getFnName() == null
                                                                        ? Constants.Nil
                                                                        : new VncString(f.getFnName()),
                                            CALLSTACK_KEY_FILE, new VncString(f.getFile()),
                                            CALLSTACK_KEY_LINE, new VncLong(f.getLine()),
                                            CALLSTACK_KEY_COL, new VncLong(f.getCol())))
                            .collect(Collectors.toList()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction os_type =
        new VncFunction(
                "os-type",
                VncFunction
                    .meta()
                    .arglists("(os-type)")
                    .doc("Returns the OS type. Type is one of `:windows`, `:mac-osx`, `:linux`, `:unix`, or `:unknown`")
                    .examples("(os-type)")
                    .seeAlso("os-type?", "os-arch", "os-name", "os-version")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                return new VncKeyword(osType());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction os_type_Q =
        new VncFunction(
                "os-type?",
                VncFunction
                    .meta()
                    .arglists("(os-type? type)")
                    .doc(
                        "Returns true if the OS id of the type otherwise false. Type is one " +
                        "of `:windows`, `:mac-osx`, `:linux`, or `:unix`")
                    .examples("(os-type? :mac-osx)", "(os-type? :windows)")
                    .seeAlso("os-type", "os-arch", "os-name", "os-version")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final String type = Coerce.toVncKeyword(args.first()).getValue();
                final String osName = System.getProperty("os.name");
                switch(type) {
                    case "windows": return VncBoolean.of(osName.startsWith("Windows"));
                    case "mac-osx": return VncBoolean.of(osName.startsWith("Mac OS X"));
                    case "linux":   return VncBoolean.of(osName.startsWith("Linux"));
                    default:        return False;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction os_arch =
        new VncFunction(
                "os-arch",
                VncFunction
                    .meta()
                    .arglists("(os-arch)")
                    .doc("Returns the OS architecture. E.g: \"x86_64\"")
                    .examples("(os-arch)")
                    .seeAlso("os-type", "os-type?", "os-name", "os-version")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                return new VncString(System.getProperty("os.arch"));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction os_name =
        new VncFunction(
                "os-name",
                VncFunction
                    .meta()
                    .arglists("(os-name)")
                    .doc("Returns the OS name. E.g.: \"Mac OS X\"")
                    .examples("(os-name)")
                    .seeAlso("os-type", "os-type?", "os-arch", "os-version")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                return new VncString(System.getProperty("os.name"));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction os_version =
            new VncFunction(
                    "os-version",
                    VncFunction
                        .meta()
                        .arglists("(os-version)")
                        .doc("Returns the OS version")
                        .examples("(os-version)")
                        .seeAlso("os-type", "os-type?", "os-arch", "os-name")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertArity(this, args, 0);

                    return new VncString(System.getProperty("os.version"));
                }

                private static final long serialVersionUID = -1848883965231344442L;
            };

    public static VncFunction system_prop =
        new VncFunction(
                "system-prop",
                VncFunction
                    .meta()
                    .arglists(
                        "(system-prop)",
                        "(system-prop name)",
                        "(system-prop name default-val)")
                    .doc(
                        "Returns the system property with the given name. Returns " +
                        "the default-val if the property does not exist or it's value is nil.\n\n" +
                        "Without arguments returns all system properties authorized by the " +
                        "configured sandbox.")
                    .examples(
                        "(system-prop :os.name)",
                        "(system-prop :foo.org \"abc\")",
                        "(system-prop \"os.name\")")
                    .seeAlso("system-env")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0, 1, 2);

                if (args.isEmpty()) {
                    final Set<String> names = System.getProperties().stringPropertyNames();
                    final Map<VncString,VncString> env = new HashMap<>();
                    for(String name : names) {
                        try {
                            // check sandbox, only use properties allowed by the sandbox!
                            final String val = ThreadContext.getInterceptor().onReadSystemProperty(name);
                            env.put(new VncString(name), new VncString(val));
                        }
                        catch(Exception ex) {
                            continue;
                        }
                    }
                    return new VncHashMap(env);
                }
                else {
                    final VncString key = Coerce.toVncString(
                                            CoreFunctions.name.apply(
                                                VncList.of(args.first())));
                    final VncVal defaultVal = args.size() == 2 ? args.second() : Nil;

                    final String val = ThreadContext.getInterceptor().onReadSystemProperty(key.getValue());

                    return val == null ? defaultVal : new VncString(val);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction system_env =
        new VncFunction(
                "system-env",
                VncFunction
                    .meta()
                    .arglists(
                        "(system-env)",
                        "(system-env name)",
                        "(system-env name default-val)")
                    .doc(
                        "Returns the system env variable with the given name. Returns " +
                        "the default-val if the variable does not exist or it's value is nil.\n\n" +
                        "Without arguments returns all system env variables authorized by the " +
                        "configured sandbox.")
                    .examples(
                        "(system-env :SHELL)",
                        "(system-env :FOO \"test\")",
                        "(system-env \"SHELL\")")
                    .seeAlso("system-prop")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0, 1, 2);

                if (args.isEmpty()) {
                    final Set<String> names = System.getenv().keySet();
                    final Map<VncString,VncString> env = new HashMap<>();
                    for(String name : names) {
                        try {
                            // check sandbox, only use env vars allowed by the sandbox!
                            final String val = ThreadContext.getInterceptor().onReadSystemEnv(name);
                            env.put(new VncString(name), new VncString(val));
                        }
                        catch(Exception ex) {
                            continue;
                        }
                    }
                    return new VncHashMap(env);
                }
                else {
                    final VncString key = Coerce.toVncString(
                                            CoreFunctions.name.apply(
                                                VncList.of(args.first())));
                    final VncVal defaultVal = args.size() == 2 ? args.second() : Nil;

                    final String val = ThreadContext.getInterceptor().onReadSystemEnv(key.getValue());

                    return val == null ? defaultVal : new VncString(val);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction system_exit_code =
        new VncFunction(
                "system-exit-code",
                VncFunction
                    .meta()
                    .arglists("(system-exit-code code)")
                    .doc(
                        "Defines the exit code that is used if the Java VM exits. " +
                        "Defaults to 0. \n\n" +
                        "Note:¶" +
                        "The exit code is only used when the Venice launcher has been " +
                        "used to run a script file, a command line script, a Venice app " +
                        "archive, or the REPL.")
                    .examples(
                        "(system-exit-code 0)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncLong code = Coerce.toVncLong(args.first());

                SYSTEM_EXIT_CODE.set(code.getIntValue());

                return Constants.Nil;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction java_version =
        new VncFunction(
                "java-version",
                VncFunction
                    .meta()
                    .arglists("(java-version)")
                    .doc("Returns the Java VM version (1.8.0_252, 11.0.7, ...)")
                    .examples("(java-version)")
                    .seeAlso("java-major-version", "java-version-info")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                final String version = System.getProperty("java.version");

                return new VncString(version);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction java_major_version =
        new VncFunction(
                "java-major-version",
                VncFunction
                    .meta()
                    .arglists("(java-major-version)")
                    .doc("Returns the Java major version (8, 9, 11, ...).")
                    .examples("(java-major-version)")
                    .seeAlso("java-version", "java-version-info")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                return new VncLong(javaMajorVersion());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction java_version_info =
        new VncFunction(
                "java-version-info",
                VncFunction
                    .meta()
                    .arglists("(java-version-info)")
                    .doc("Returns the Java VM version info.")
                    .examples("(java-version-info)")
                    .seeAlso("java-version", "java-major-version")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                return VncOrderedMap.of(
                        new VncKeyword("version"),
                        new VncString(System.getProperty("java.version")),
                        new VncKeyword("vendor"),
                        new VncString(System.getProperty("java.vendor")),
                        new VncKeyword("vm-version"),
                        new VncString(System.getProperty("java.vm.version")),
                        new VncKeyword("vm-name"),
                        new VncString(System.getProperty("java.vm.name")),
                        new VncKeyword("vm-vendor"),
                        new VncString(System.getProperty("java.vm.vendor")));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction java_source_location =
        new VncFunction(
                "java-source-location",
                VncFunction
                    .meta()
                    .arglists("(java-source-location class)")
                    .doc(
                        "Returns the path of the source location of a class (fully " +
                        "qualified class name).")
                    .examples(
                        "(java-source-location :com.github.jlangch.venice.Venice)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                try {
                    final String className = Coerce.toVncString(
                                                CoreFunctions.name.apply(VncList.of(args.first()))
                                            ).getValue();

                    final Class<?> clazz = Class.forName(className);

                    final URI uri = clazz.getProtectionDomain()
                                         .getCodeSource()
                                         .getLocation()
                                         .toURI();

                    return new VncString(new File(uri).getPath());
                }
                catch(Exception ex) {
                    return Nil;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction total_memory =
        new VncFunction(
                "total-memory",
                VncFunction
                    .meta()
                    .arglists("(total-memory)")
                    .doc("Returns the total amount of memory available to the Java VM.")
                    .examples("(total-memory)")
                    .seeAlso("used-memory")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                System.gc();
                final Runtime rt = Runtime.getRuntime();
                double totalMB = rt.totalMemory() / 1024.0D / 1024.0D;

                return new VncString(String.format("%.1fMB", totalMB));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction used_memory =
        new VncFunction(
                "used-memory",
                VncFunction
                    .meta()
                    .arglists("(used-memory)")
                    .doc("Returns the currently used memory by the Java VM.")
                    .examples("(used-memory)")
                    .seeAlso("total-memory")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                System.gc();
                final Runtime rt = Runtime.getRuntime();
                double usedMB = (rt.totalMemory() - rt.freeMemory()) / 1024.0D / 1024.0D;

                return new VncString(String.format("%.1fMB", usedMB));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction load_jar =
        new VncFunction(
                "load-jar",
                VncFunction
                    .meta()
                    .arglists("(load-jar url)")
                    .doc(
                        "Dynamically load a JAR into the classpath. \n\n" +
                        "Dynamically loading JARs is experimental and for security " +
                        "reasons available in the REPL only!")
                    .examples(
                        "(do                                                              \n" +
                        "  (load-module :maven)                                           \n" +
                        "  (let [uri (maven/uri \"org.knowm.xchart:xchart:3.6.5\" :jar)]  \n" +
                        "    (load-jar uri)))                                               ")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncString url = Coerce.toVncString(args.first());
                final ClassLoader cl = Thread.currentThread().getContextClassLoader();
                if (cl != null && cl instanceof DynamicClassLoader2) {
                    try {
                        ((DynamicClassLoader2)cl).addURL(new URL(url.getValue()));
                        return Nil;
                    }
                    catch (MalformedURLException ex) {
                        throw new VncException(
                                String.format("Malformed URL '%s'", url.getValue()),
                                ex);
                    }
                }
                else {
                    throw new VncException(
                            "There is no thread context ClassLoader available to " +
                            "dynamically load a JAR. For security reasons dynamically " +
                            "loading JARs is only available in the REPL!");
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction jansi_version =
        new VncFunction(
                "jansi-version",
                VncFunction
                    .meta()
                    .arglists("(jansi-version)")
                    .doc( "Returns the Jansi version or nil if not available.")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                final String version = getJansiVersion();
                return version == null ? Nil : new VncString(version);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };



    public static long javaMajorVersion() {
        String version = System.getProperty("java.version");

        if (version.startsWith("1.")) {
            version = version.substring(2);
        }

        return Long.parseLong(version.substring(0, version.indexOf(".")));
    }

    public static String osType() {
        switch(OS.type()) {
            case MacOSX:  return "mac-osx";
            case Linux:   return "linux";
            case Unix:    return "unix";
            case Windows: return "windows";
            default:      return "unknown";
        }
    }

    public static String getJansiVersion() {
        try {
            final Class<?> clazz = Class.forName("org.fusesource.jansi.AnsiMain");
            final Package p = clazz.getPackage();
            return p == null ? null : p.getImplementationVersion();
        }
        catch(Exception ex) {
            return null;
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // types_ns is namespace of type functions
    ///////////////////////////////////////////////////////////////////////////

    public static final Map<VncVal, VncVal> ns =
            new SymbolMapBuilder()
                    .add(pid)
                    .add(byte_order)
                    .add(host_name)
                    .add(host_address)
                    .add(user_name)
                    .add(ip_private_Q)
                    .add(gc)
                    .add(cpus)
                    .add(shutdown_hook)
                    .add(callstack)
                    .add(os_type)
                    .add(os_type_Q)
                    .add(os_arch)
                    .add(os_name)
                    .add(os_version)
                    .add(system_prop)
                    .add(system_env)
                    .add(system_exit_code)
                    .add(java_version)
                    .add(java_version_info)
                    .add(java_source_location)
                    .add(java_major_version)
                    .add(total_memory)
                    .add(used_memory)
                    .add(load_jar)
                    .add(jansi_version)
                    .toMap();



    public static final VncKeyword CALLSTACK_KEY_FN_NAME = new VncKeyword(":fn-name");
    public static final VncKeyword CALLSTACK_KEY_FILE = new VncKeyword(":file");
    public static final VncKeyword CALLSTACK_KEY_LINE = new VncKeyword(":line");
    public static final VncKeyword CALLSTACK_KEY_COL = new VncKeyword(":col");

    public static final AtomicInteger SYSTEM_EXIT_CODE = new AtomicInteger(0);
}
