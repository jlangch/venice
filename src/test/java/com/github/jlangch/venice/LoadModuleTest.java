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
package com.github.jlangch.venice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;


public class LoadModuleTest {

    @Test
    public void test_loadmodule() {
        final String script =
                "(do                               \n" +
                "  (ns foo)                        \n" +
                "  (load-module :test-support)     \n" +
                "  (test-support/test-fn \"xxx\")) ";

        assertEquals("test: xxx", new Venice().eval(script));
    }

    @Test
    public void test_loadmodule_alias() {
        final String script =
                "(do                                                      \n" +
                "  (ns foo)                                               \n" +
                "  (load-module :test-support ['test-support :as 't])     \n" +
                "  (t/test-fn \"xxx\"))                                   ";

        assertEquals("test: xxx", new Venice().eval(script));
    }

    @Test
    public void test_loadmodule_ns() {
        // verify that the namespace is not changed by 'load-module'
        final String script =
                "(do                               \n" +
                "  (ns foo)                        \n" +
                "  (load-module :test-support)     \n" +
                "  *ns*)                   ";

        assertEquals("foo", new Venice().eval(script));
    }

}
