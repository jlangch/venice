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
package com.github.jlangch.venice.impl.sandbox;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class RestrictedBlacklistedFunctions {

    public static Set<String> getIoFunctions() {
        return cache.computeIfAbsent("io", k -> io());
    }

    public static Set<String> getPrintFunctions() {
        return cache.computeIfAbsent("print", k -> print());
    }

    public static Set<String> getConcurrencyFunctions() {
        return cache.computeIfAbsent("concurrency", k -> concurrency());
    }

    public static Set<String> getJavaInteropFunctions() {
        return cache.computeIfAbsent("java-interop", k -> java_interop());
    }

    public static Set<String> getSystemFunctions() {
        return cache.computeIfAbsent("system", k -> system());
    }

    public static Set<String> getSpecialForms() {
        return cache.computeIfAbsent("special-forms", k -> special_forms());
    }

    public static Set<String> getAllFunctions() {
        return cache.computeIfAbsent("all", k -> mergeToSet(
        											getIoFunctions(),
        											getPrintFunctions(),
        											getConcurrencyFunctions(),
        											getJavaInteropFunctions(),
        											getSystemFunctions(),
        											getSpecialForms()));
    }


    private static Set<String> print() {
        return new HashSet<>(
            Arrays.asList(
                // print
                "print",
                "printf",
                "println",
                "newline",
                "pr",
                "prn",
                "flush",
                "io/print",
                "io/flush"));
    }


    private static Set<String> io() {
        return new HashSet<>(
            // ************************************************************
            // * Functions::main() helps with build this list
            // ************************************************************

            Arrays.asList(
                // miscellaneous
                "fn-body",              // from core functions
                "fn-pre-conditions",    // from core functions

                // print
                "flush",


                // print
                "print",
                "printf",
                "println",
                "newline",
                "pr",
                "prn",
                "flush",
                "io/print",
                "io/flush",


                // read
                "read-line",
                "read-char",


                // I/O:
                "io/->uri",
                "io/->url",
                "io/await-for",
                "io/buffered-reader",
                "io/buffered-writer",
                "io/bytebuf-in-stream",
                "io/bytebuf-out-stream",
                "io/capturing-print-stream",
                "io/classpath-resource?",
                "io/close",
                "io/close-watcher",
                "io/copy-file",
                "io/copy-stream",
                "io/default-charset",
                "io/delete-file",
                "io/delete-file-on-exit",
                "io/delete-file-tree",
                "io/delete-files-glob",
                "io/download",
                "io/exists-dir?",
                "io/exists-file?",
                "io/file",
                "io/file-absolute-path",
                "io/file-can-execute?",
                "io/file-can-read?",
                "io/file-can-write?",
                "io/file-canonical-path",
                "io/file-ext",
                "io/file-ext?",
                "io/file-hidden?",
                "io/file-in-stream",
                "io/file-last-modified",
                "io/file-matches-glob?",
                "io/file-name",
                "io/file-out-stream",
                "io/file-parent",
                "io/file-path",
                "io/file-size",
                "io/file-symbolic-link?",
                "io/file?",
                "io/flush",
                "io/glob-path-matcher",
                "io/gzip",
                "io/gzip-to-stream",
                "io/gzip?",
                "io/internet-avail?",
                "io/list-file-tree",
                "io/list-files",
                "io/list-files-glob",
                "io/load-classpath-resource",
                "io/make-venice-filename",
                "io/mime-type",
                "io/mkdir",
                "io/mkdirs",
                "io/move-file",
                "io/print",
                "io/print-line",
                "io/read-char",
                "io/read-line",
                "io/slurp",
                "io/slurp-lines",
                "io/slurp-reader",
                "io/slurp-stream",
                "io/spit",
                "io/spit-stream",
                "io/spit-writer",
                "io/string-in-stream",
                "io/string-reader",
                "io/string-writer",
                "io/temp-dir",
                "io/temp-file",
                "io/tmp-dir",
                "io/touch-file",
                "io/ungzip",
                "io/ungzip-to-stream",
                "io/unzip",
                "io/unzip-all",
                "io/unzip-first",
                "io/unzip-nth",
                "io/unzip-to-dir",
                "io/uri-stream",
                "io/user-dir",
                "io/user-home-dir",
                "io/watch-dir",
                "io/wrap-is-with-buffered-reader",
                "io/wrap-os-with-buffered-writer",
                "io/wrap-os-with-print-writer",
                "io/zip",
                "io/zip-append",
                "io/zip-file",
                "io/zip-list",
                "io/zip-list-entry-names",
                "io/zip-remove",
                "io/zip-size",
                "io/zip?",

                // Shell
                "sh",
                "sh/open",
                "sh/pwd",

                // Fonts
                "fonts/download-font-family",

                // Installer
                "installer/install-module",
                "installer/install-libs",
                "installer/install-demo",
                "installer/install-demo-fonts",

                // Maven
                "maven/dependencies",
                "maven/download",
                "maven/get",
                "maven/mvn",
                "maven/version",

                // Gradle
                "gradle/task",
                "gradle/version",

                // Docker
                "docker/version",
                "docker/cmd",
                "docker/debug",
                "docker/images",
                "docker/image-pull",
                "docker/rmi",
                "docker/image-rm",
                "docker/image-prune",
                "docker/ps",
                "docker/stop",
                "docker/run",
                "docker/start",
                "docker/exec",
                "docker/rm",
                "docker/diff",
                "docker/unpause",
                "docker/wait",
                "docker/logs",
                "docker/exec&",
                "docker/prune",
                "docker/cp",
                "docker/pause",
                "docker/volume-list",
                "docker/volume-create",
                "docker/volume-inspect",
                "docker/volume-rm",
                "docker/volume-prune",
                "docker/volume-exists?",
                "docker/images-query-by-repo",
                "docker/image-ready?",
                "docker/container-find-by-name",
                "docker/container-exists-with-name?",
                "docker/container-running-with-name?",
                "docker/container-start-by-name",
                "docker/container-stop-by-name",
                "docker/container-remove-by-name",
                "docker/container-status-by-name",
                "docker/container-exec-by-name",
                "docker/container-exec-by-name&",
                "docker/container-logs",
                "docker/container-purge-by-name",
                "docker/container-image-info-by-name"
            ));
    }


    private static Set<String> system() {
        return new HashSet<>(

            // ************************************************************
            // * Functions::main() helps with build this list
            // ************************************************************

            Arrays.asList(

                // System
                "callstack",
                "cpus",
                "gc",
                "host-address",
                "host-name",
                "ip-private?",
                "java-major-version",
                "java-source-location",
                "java-version",
                "java-version-info",
                "load-jar",
                "os-arch",
                "os-name",
                "os-type",
                "os-type?",
                "os-version",
                "pid",
                "shutdown-hook",
                "system-env",
                "system-exit-code",
                "system-prop",
                "total-memory",
                "used-memory",
                "user-name"
           ));
    }

    private static Set<String> concurrency() {
        return new HashSet<>(

            // ************************************************************
            // * Functions::main() helps with build this list
            // ************************************************************

            Arrays.asList(

                // Concurrency: agents, futures, promises, thread, thread local, watches
                //              parallel processing
                "accept-either",
                "add-watch",
                "agent",
                "agent-error",
                "agent-error-mode",
                "agent-send-off-thread-pool-info",
                "agent-send-thread-pool-info",
                "all-of",
                "any-of",
                "apply-to-either",
                "await",
                "await-for",
                "await-termination-agents",
                "await-termination-agents?",
                "cancel",
                "cancelled?",
                "complete-on-timeout",
                "deliver",
                "deliver-ex",
                "done?",
                "future",
                "future-task",
                "future?",
                "futures-fork",
                "futures-thread-pool-info",
                "futures-wait",
                "or-timeout",
                "pcalls",
                "pmap",
                "promise",
                "promise?",
                "realized?",
                "remove-watch",
                "restart-agent",
                "send",
                "send-off",
                "set-error-handler!",
                "shutdown-agents",
                "shutdown-agents?",
                "then-accept",
                "then-accept-both",
                "then-apply",
                "then-combine",
                "then-compose",
                "thread",
                "thread-daemon?",
                "thread-id",
                "thread-interrupted",
                "thread-interrupted?",
                "thread-local",
                "thread-local-clear",
                "thread-local-map",
                "thread-local?",
                "thread-name",
                "timeout-after",
                "when-complete",

                // Scheduler
                "schedule-at-fixed-rate",
                "schedule-delay"
        ));
    }


    private static Set<String> java_interop() {
        return new HashSet<>(

            // ************************************************************
            // * Functions::main() helps with build this list
            // ************************************************************

            Arrays.asList(

                // Java Interop
                ".",
                "bases",
                "cast",
                "class",
                "class-name",
                "class-of",
                "class-version",
                "classloader",
                "classloader-of",
                "describe-class",
                "exists-class?",
                "formal-type",
                "jar-maven-manifest-version",
                "java-enumeration-to-list",
                "java-iterator-to-list",
                "java-obj?",
                "java-package-version",
                "java-unwrap",
                "java-unwrap-optional",
                "java-wrap",
                "module-name",
                "proxify",
                "stacktrace",
                "supers"
        ));
    }

    private static Set<String> special_forms() {
        return new HashSet<>(
                Arrays.asList(
                    "dobench",
                    "dorun",
                    "inspect",
                    "load-classpath-file",
                    "load-file",
                    "load-module",
                    "load-string",
                    "macroexpand-on-load?",
                    "ns-list",
                    "ns-remove",
                    "ns-unmap",
                    "print-highlight",
                    "prof",
                    "set!",
                    "var-get",
                    "var-global?",
                    "var-local?",
                    "var-name",
                    "var-ns",
                    "var-thread-local?"));
    }


    private static Set<String> mergeToSet(
            final Collection<String> s1,
            final Collection<String> s2,
            final Collection<String> s3,
            final Collection<String> s4,
            final Collection<String> s5,
            final Collection<String> s6
    ) {
        final HashSet<String> set = new HashSet<>(s1);
        set.addAll(s2);
        set.addAll(s3);
        set.addAll(s4);
        set.addAll(s5);
        set.addAll(s6);
        return set;
    }


    private static ConcurrentHashMap<String, Set<String>> cache = new ConcurrentHashMap<>();
}
