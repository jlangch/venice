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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;


public class SandboxFunctionGroups {

    public static Set<String> groupFunctions(final String group) {
        if (group.equals("*io*")) {
            return RestrictedBlacklistedFunctions.getIoFunctions();
        }
        else if (group.equals("*print*")) {
            return RestrictedBlacklistedFunctions.getPrintFunctions();
        }
        else if (group.equals("*special-forms*")) {
            return RestrictedBlacklistedFunctions.getSpecialForms();
        }
        else if (group.equals("*concurrency*")) {
            return RestrictedBlacklistedFunctions.getConcurrencyFunctions();
        }
        else if (group.equals("*system*")) {
            return RestrictedBlacklistedFunctions.getSystemFunctions();
        }
        else if (group.equals("*java-interop*")) {
            return RestrictedBlacklistedFunctions.getJavaInteropFunctions();
        }
        else if (group.equals("*unsafe*")) {
            return RestrictedBlacklistedFunctions.getAllFunctions();
        }
        else {
            throw new VncException(String.format(
                        "Invalid sandbox function group '%s", group));
        }
    }

    public static List<String> groupFunctionsSorted(final String group) {
        return groupFunctions(group).stream().sorted().collect(Collectors.toList());
    }

    public static boolean isValidGroup(final String group) {
        return GROUPS.contains(group);
    }


    public static List<String> getGroups() {
        return GROUPS.stream().sorted().collect(Collectors.toList());
    }


    private static Set<String> GROUPS =
            new HashSet<>(
                Arrays.asList(
                        "*io*",
                        "*print*",
                        "*special-forms*",
                        "*concurrency*",
                        "*system*",
                        "*java-interop*",
                        "*unsafe*"));

}
