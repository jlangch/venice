/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2022 Venice
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


public class RestrictedBlacklistedFunctions {

    public static Set<String> getIoFunctions() {
        return IO;
    }

    public static Set<String> getSpecialForms() {
        return SPECIAL_FORMS;
    }

    public static Set<String> getAll() {
        return ALL;
    }

    public static boolean contains(final String funcName) {
        return ALL.contains(funcName);
    }

    public static boolean isIoAsteriskFunction(final String funcName) {
        String fnName = funcName;
        if (fnName.startsWith("core/")) {
            fnName.substring("core/".length());
        }

        if (fnName.endsWith("**")) {
            return false;  // ends with more than one asterisk
        }

        return SPECIAL_FORM_ASTERISKS.contains(fnName);
    }


    private static Set<String> SPECIAL_FORM_ASTERISKS =
        new HashSet<>(
            Arrays.asList(
                // load
                "load-module",
                "load-module*",
                "load-file",
                "load-file*",
                "load-classpath-file",
                "load-classpath-file*",
                "load-resource",
                "load-resource*"));

    private static Set<String> IO =
        new HashSet<>(
            Arrays.asList(
                // miscellaneous
                "fn-body",
                "fn-pre-conditions",

                // print
                "print",
                "printf",
                "println",
                "newline",
                "pr",
                "prn",

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

                // Concurrency:
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
                "atom",
                "atom?",
                "await",
                "await-for",
                "await-termination-agents",
                "await-termination-agents?",
                "cancel",
                "cancelled?",
                "compare-and-set!",
                "complete-on-timeout",
                "delay*",
                "delay?",
                "deliver",
                "deliver-ex",
                "deref",
                "deref?",
                "done?",
                "force",
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
                "reset!",
                "restart-agent",
                "send",
                "send-off",
                "set-error-handler!",
                "shutdown-agents",
                "shutdown-agents?",
                "swap!",
                "swap-vals!",
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
                "volatile",
                "volatile?",
                "when-complete",

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
                "supers",

                // Scheduler
                "schedule-at-fixed-rate",
                "schedule-delay",

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
                "sandbox-type",
                "sandboxed?",
                "shutdown-hook",
                "system-env",
                "system-exit-code",
                "system-prop",
                "total-memory",
                "used-memory",
                "user-name",

                // Shell
                "sh",
                "sh/open",
                "sh/pwd"
    ));

    private static Set<String> SPECIAL_FORMS =
            mergeToSet(
                SPECIAL_FORM_ASTERISKS,

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



    private static Set<String> ALL = mergeToSet(IO, SPECIAL_FORMS);

    private static Set<String> mergeToSet(final Collection<String> s1, Collection<String> s2) {
        final HashSet<String> set = new HashSet<>(s1);
        set.addAll(s2);
        return set;
    }
}
