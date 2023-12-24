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
package com.github.jlangch.venice.impl.namespaces;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.util.MetaUtil;


public class Namespaces {

    public static boolean isCoreNS(final VncSymbol nsSym) {
        return Namespaces.NS_CORE.equals(nsSym);
    }

    public static boolean isCoreNS(final String nsName) {
        return "core".equals(nsName);
    }

    public static boolean isCurrentNSSymbol(final VncSymbol nsSym) {
        return NS_CURRENT_NAME.equals(nsSym.getName());
    }

    public static boolean isSystemNS(final String nsName) {
        return SYSTEM_NAMESPACES.contains(nsName);
    }

    public static boolean isCurrentNSSymbol(final String nsName) {
        return NS_CURRENT_NAME.equals(nsName);
    }

    public static VncSymbol getCurrentNS() {
        return ThreadContext.getCurrNS().getNS();
    }

    public static Namespace getCurrentNamespace() {
        return ThreadContext.getCurrNS();
    }

    public static void setCurrentNamespace(final Namespace ns) {
        ThreadContext.setCurrNS(ns);
    }

    public static VncSymbol qualifySymbolWithCurrNS(final VncSymbol sym) {
        if (sym == null) {
            return null;
        }
        else if (sym.hasNamespace()) {
            return new VncSymbol(
                        sym.getName(),
                        MetaUtil.setNamespace(sym.getMeta(), sym.getNamespace()));
        }
        else {
            final VncSymbol ns = Namespaces.getCurrentNS();
            final VncVal newMeta = MetaUtil.setNamespace(sym.getMeta(), ns.getName());

            return Namespaces.isCoreNS(ns) || sym.isSpecialFormName()
                    ? new VncSymbol(sym.getName(), newMeta)
                    : new VncSymbol(ns.getName(), sym.getName(), newMeta);
        }
    }



    public static final String NS_CURRENT_NAME = "*ns*";
    public static final VncSymbol NS_CURRENT_SYMBOL = new VncSymbol("*ns*");

    public static final VncSymbol NS_USER = new VncSymbol("user");
    public static final VncSymbol NS_CORE = new VncSymbol("core");

    public static final Set<String> SYSTEM_NAMESPACES =
            Collections.unmodifiableSet(
                toSet(
                    Arrays.asList(
                        "core", "cidr", "csv",
                        "dag",
                        "inet", "io",
                        "json",
                        "math",
                        "sandbox", "sh", "str",
                        "regex", "repl",
                        "time")));

    public static final Set<String> RESERVED_NAMESPACES =
            Collections.unmodifiableSet(
                toSet(
                    SYSTEM_NAMESPACES,
                    Arrays.asList(
                        "bench",
                        "component", "crypt",
                        "docker", "docx",
                        "excel",
                        "http",
                        "jackson",
                        "kira",
                        "local",
                        "maven",
                        "parsifal", "pdf",
                        "ring",
                        "shell",
                        "tc", "test",
                        "xchart", "xml",
                        "webdav")));

    private static Set<String> toSet(
            final Collection<String> l1
    ) {
        return new HashSet<>(l1);
    }

    private static Set<String> toSet(
            final Collection<String> l1,
            final Collection<String> l2
    ) {
        final HashSet<String> set = new HashSet<>(l1);
        set.addAll(l2);
        return set;
    }
}
