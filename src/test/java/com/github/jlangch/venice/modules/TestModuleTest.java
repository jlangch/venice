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
package com.github.jlangch.venice.modules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class TestModuleTest {

    @Test
    public void test_load() {
        assertEquals("[test, loaded]", new Venice().eval("(load-module :test)")
                                                   .toString());

        assertEquals("[test, already-loaded]", new Venice().eval("(do (load-module :test) (load-module :test))")
                                                           .toString());
    }

    @Test
    public void test_eval() {
        final String script =
            "(ns xxx)                            \n" +
            "                                    \n" +
            "(defmacro xxx/fn ([x] `(+ 1 ~x)))   \n" +
            "                                    \n" +
            "(defn xxx/test-fn [x]               \n" +
            "  (str \"test: \" x))                 ";

        assertEquals(
            "xxx/test-fn",
            new Venice().eval("(load-string \"\"\"" + script + "\"\"\")"));
    }

}
