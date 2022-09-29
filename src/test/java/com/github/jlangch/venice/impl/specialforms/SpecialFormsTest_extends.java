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

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class SpecialFormsTest_extends {

    @Test
    public void test_extends_deftype_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                 \n" +
                "  (ns test)                                         \n" +
                "  (defprotocol P (foo [x]) (bar [x]))               \n" +
                "  (deftype :person [name :string last :string]      \n" +
                "           P (foo [x] (:name x))                    \n" +
                "             (bar [x] (:last x)))                   \n" +
                "  (def p (person. \"joe\" \"smith\"))               \n" +
                "  (extends? (type p) P))";

        assertTrue((boolean)venice.eval(script));
    }

    @Test
    public void test_extends_deftype_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                 \n" +
                "  (ns test)                                         \n" +
                "  (defprotocol P (foo [x]) (bar [x]))               \n" +
                "  (deftype :person [name :string last :string])     \n" +
                "  (extend :test/person P                            \n" +
                "          (foo [x] (:name x))                       \n" +
                "          (bar [x] (:last x)))                      \n" +
                "  (def p (person. \"joe\" \"smith\"))               \n" +
                "  (extends? (type p) P))";

        assertTrue((boolean)venice.eval(script));
    }

}
