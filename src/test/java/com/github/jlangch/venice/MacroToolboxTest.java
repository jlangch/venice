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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;


public class MacroToolboxTest {

    @Test
    public void testQuoting() {
        final Venice venice = new Venice();

        assertEquals("(1 2 3)",                    venice.eval("(str '(1 2 3))"));
        assertEquals("(1 2 (list 4 5))",           venice.eval("(str '(1 2 (list 4 5)))"));
        assertEquals("(1 2 (unquote (list 4 5)))", venice.eval("(str '(1 2 ~(list 4 5)))"));

        assertEquals("(1 2 3)",                    venice.eval("(str `(1 2 3))"));
        assertEquals("(1 2 (list 4 5))",           venice.eval("(str `(1 2 (list 4 5)))"));
        assertEquals("(1 2 (4 5))",                venice.eval("(str `(1 2 ~(list 4 5)))"));

        assertEquals("(1 2 (list 4 5))",           venice.eval("(str `(1 2 (list 4 5)))"));
        assertEquals("(1 2 (4 5))",                venice.eval("(str `(1 2 ~(list 4 5)))"));
        assertEquals("(1 2 4 5)",                  venice.eval("(str `(1 2 ~@(list 4 5)))"));

        assertEquals("(1 2 (list 4 (+ 3 2)))",     venice.eval("(str `(1 2 (list 4 (+ 3 2))))"));
        assertEquals("(1 2 (4 5))",                venice.eval("(str `(1 2 ~(list 4 (+ 3 2))))"));
        assertEquals("(1 2 4 5)",                  venice.eval("(str `(1 2 ~@(list 4 (+ 3 2))))"));

        assertEquals("(1 2 (4 (+ 3 2)))",          venice.eval("(str `(1 2 ~(list 4 `(+ 3 2))))"));
        assertEquals("(1 2 4 (+ 3 2))",            venice.eval("(str `(1 2 ~@(list 4 `(+ 3 2))))"));
    }

    @Test
    public void testGensym() {
        final Venice venice = new Venice();

        final Set<String> symbols = new HashSet<>();

        for(int ii=0; ii<1000; ii++) {
            symbols.add((String)venice.eval("(gensym)"));
        }
        assertEquals(1000, symbols.size());

        for(int ii=0; ii<1000; ii++) {
            symbols.add((String)venice.eval("(gensym 'hello)"));
        }
        assertEquals(2000, symbols.size());
    }

    @Test
    public void testAutoGensym_1() {
        final Venice venice = new Venice();

        assertEquals(100L,  venice.eval("(let [a# 100] a#)"));
    }

    @Test
    public void testAutoGensym_2() {
        final Venice venice = new Venice();

        // (let [a# 100] `(+ 10 ~(dec a#)))
        //    expands to
        // (let [a# 100] (quasiquote (+ 10 (unquote (dec a__44__auto)))))

        try {
            venice.eval("(let [a# 100] `(+ 10 ~(dec a#)))");

            // SymbolNotFoundException: Symbol 'a__42__auto' not found.
            fail("Expected SymbolNotFoundException");
        }
        catch(SymbolNotFoundException ex) {
            assertTrue(true);
        }
    }

    @Test
    public void testAutoGensym_3() {
        final Venice venice = new Venice();

        // `(let [a# 100] ~(dec a#))
        //    expands to
        // (quasiquote (let [a__50__auto 100] (unquote (dec a__50__auto))))

        try {
            venice.eval("`(let [a# 100] ~(dec a#))");

            // SymbolNotFoundException: Symbol 'a__42__auto' not found.
            fail("Expected SymbolNotFoundException");


            // The reason for  this behavior is that `(dec a#)` is executed before
            // the symbol a# is created and assigned the value 100 in `(let [a# 100] ...`
        }
        catch(SymbolNotFoundException ex) {
            assertTrue(true);
        }
    }

}
