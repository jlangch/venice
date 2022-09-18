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
package com.github.jlangch.venice.impl.repl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.SymbolNotFoundException;
import com.github.jlangch.venice.Venice;


public class ReplFunctionsTest {

    @Test
    public void test_1() {
        assertFalse((Boolean)new Venice().eval("(repl?)"));
    }

    @Test
    public void test_info() {
        // ensure that REPL functions like 'repl/info are not available
        // if not run within a REPL!
        assertThrows(
                SymbolNotFoundException.class,
                () -> new Venice().eval("(repl/info)"));
    }

    @Test
    public void test_term_rows() {
        // ensure that REPL functions like 'repl/term-rows are not available
        // if not run within a REPL!
        assertThrows(
                SymbolNotFoundException.class,
                () -> new Venice().eval("(repl/term-rows)"));
    }

    @Test
    public void test_term_cols() {
        // ensure that REPL functions like 'repl/term-cols are not available
        // if not run within a REPL!
        assertThrows(
                SymbolNotFoundException.class,
                () -> new Venice().eval("(repl/term-cols)"));
    }
}
