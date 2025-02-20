/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2025 Venice
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
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.util.TestException;


public class ExceptionTest {

    @SuppressWarnings({ "unchecked" })
    @Test
    public void test_ValueException() {
        final Venice venice = new Venice();

        try {
            venice.eval("(throw '(1 2))");
        }
        catch(ValueException ex) {
            final Object val = ex.getValue();
            if (val instanceof List) {
                final List<Object> list = (List<Object>)val;
                assertEquals(2, list.size());
                assertEquals(1L, list.get(0));
                assertEquals(2L, list.get(1));
            }
            else {
                fail("Expected java.util.List value");
            }
        }
        catch(RuntimeException ex) {
            fail("Expected ValueException");
        }
    }


    @Test
    public void test_JavaException() {
        final Venice venice = new Venice();

        try {
            venice.eval(
                    "(do                                                          \n" +
                    "   (import :com.github.jlangch.venice.util.TestException)    \n" +
                    "                                                             \n" +
                    "   (ex :TestException \"hello\"))                            ");
        }
        catch(TestException ex) {
            assertEquals("hello", ex.getMessage());
        }
        catch(Exception ex) {
        	ex.printStackTrace();
            fail("Unexpected Exception " + ex.getClass().getName());
        }
    }
}
