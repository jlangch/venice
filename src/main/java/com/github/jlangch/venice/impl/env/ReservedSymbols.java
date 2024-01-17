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
package com.github.jlangch.venice.impl.env;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class ReservedSymbols {

    public static boolean isReserved(final String name) {
        return RESERVED.contains(name);
    }

    private static final Set<String> RESERVED = new HashSet<>(
            Arrays.asList(
                ".",
                "proxify",
                "*in*",
                "*out*",
                "*err*",
                "*version*",
                "*newline*",
                "*ns*",
                "*loaded-modules*",
                "*loaded-files*",
                "*run-mode*",
                "*ansi-term*",
                "*app-name*",
                "*app-archive*",
                "*ARGV*",

                "*REPL*",
                "*repl-color-theme*"));
}
