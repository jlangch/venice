/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2026 Venice
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
package com.github.jlangch.venice.impl.interop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class JavaInteropFunctionsTest {

    @Test
    public void test_java_string_list() {
        final Venice venice = new Venice();

        assertEquals("java.util.ArrayList",             venice.eval("(type (java-string-list '(\"ab\")))"));

        assertEquals("[]",                              venice.eval("(java-string-list '())").toString());
        assertEquals("[ab]",                            venice.eval("(java-string-list '(\"ab\"))").toString());
        assertEquals("[ab, 1, 2, 3.0, 4.0, 5, null]",   venice.eval("(java-string-list '(\"ab\" 1 2I 3.0 4.0M 5 nil))").toString());
    }

    @Test
    public void test_java_int_list() {
        final Venice venice = new Venice();

        assertEquals("java.util.ArrayList",        venice.eval("(type (java-int-list '(1I)))"));

        assertEquals("[]",                         venice.eval("(java-int-list '())").toString());
        assertEquals("[1]",                        venice.eval("(java-int-list '(1I))").toString());
        assertEquals("[1, 2, 3, 4, 5, null]",      venice.eval("(java-int-list '(1 2I 3.0 4.0M 5 nil))").toString());
    }

    @Test
    public void test_java_long_list() {
        final Venice venice = new Venice();

        assertEquals("java.util.ArrayList",        venice.eval("(type (java-long-list '(1)))"));

        assertEquals("[]",                         venice.eval("(java-long-list '())").toString());
        assertEquals("[1]",                        venice.eval("(java-long-list '(1))").toString());
        assertEquals("[1, 2, 3, 4, 5, null]",      venice.eval("(java-long-list '(1 2I 3.0 4.0M 5 nil))").toString());
    }

    @Test
    public void test_java_float_list() {
        final Venice venice = new Venice();

        assertEquals("java.util.ArrayList",             venice.eval("(type (java-float-list '(1.0)))"));

        assertEquals("[]",                              venice.eval("(java-float-list '())").toString());
        assertEquals("[1.0]",                           venice.eval("(java-float-list '(1.0F))").toString());
        assertEquals("[1.0, 2.0, 3.0, 4.0, 5.0, null]", venice.eval("(java-float-list '(1 2I 3.0 4.0M 5 nil))").toString());
    }

    @Test
    public void test_java_double_list() {
        final Venice venice = new Venice();

        assertEquals("java.util.ArrayList",             venice.eval("(type (java-double-list '(1.0)))"));

        assertEquals("[]",                              venice.eval("(java-double-list '())").toString());
        assertEquals("[1.0]",                           venice.eval("(java-double-list '(1.0))").toString());
        assertEquals("[1.0, 2.0, 3.0, 4.0, 5.0, null]", venice.eval("(java-double-list '(1 2I 3.0 4.0M 5 nil))").toString());
    }
}
