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
package com.github.jlangch.venice.impl.specialforms;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 * Special forms have evaluation rules that differ from standard Venice
 * evaluation rules and are understood directly by the Venice interpreter.
 */
public class SpecialForms {

    public static boolean isSpecialForm(final String name) {
        return FORMS.contains(name);
    }

    public static final Set<String> FORMS = new HashSet<>(
            Arrays.asList(
                    // VeniceInterpreter
                    "do",
                    "if",
                    "let",
                    "loop",
                    "recur",
                    "quasiquote",
                    "macroexpand",
                    "macroexpand-all*",
                    "tail-pos",

                    // VncSpecialForm   (Functions::main(..) helps producing this)
                    ".:",
                    "binding",
                    "bound?",
                    "def",
                    "def-dynamic",
                    "defmacro",
                    "defmethod",
                    "defmulti",
                    "defonce",
                    "defprotocol",
                    "deftype",
                    "deftype-describe",
                    "deftype-of",
                    "deftype-or",
                    "deftype?",
                    "dobench",
                    "doc",
                    "dorun",
                    "eval",
                    "extend",
                    "extends?",
                    "fn",
                    "import",
                    "imports",
                    "inspect",
                    "load-classpath-file",
                    "load-file",
                    "load-module",
                    "load-string",
                    "locking",
                    "macroexpand-on-load?",
                    "modules",
                    "ns",
                    "ns-list",
                    "ns-remove",
                    "ns-unmap",
                    "print-highlight",
                    "prof",
                    "quasiquote",
                    "quote",
                    "resolve",
                    "set!",
                    "try",
                    "try-with",
                    "var-get",
                    "var-global?",
                    "var-local?",
                    "var-name",
                    "var-ns",
                    "var-thread-local?",

                    // implicite
                    "catch",
                    "finally"));

}
