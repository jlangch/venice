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
package com.github.jlangch.venice.javainterop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class JavaInterop_java_types_Test {


    @Test
    public void test_java_list() {
        final Venice venice = new Venice();

        final String list1 =
                "(do                                      " +
                "  (type                                  " +
                "    (doto (. :java.util.ArrayList :new)  " +
                "          (. :add 1)                     " +
                "          (. :add (+ 1 2))))             " +
                ") ";

        assertEquals("java.util.ArrayList", venice.eval(list1));


        final String list2 =
                "(do                                    " +
                "  (doto (. :java.util.ArrayList :new)  " +
                "        (. :add 1)                     " +
                "        (. :add (+ 1 2)))              " +
                ") ";

        assertEquals("java.util.ArrayList", venice.eval(list2).getClass().getName());
        assertEquals("[1, 3]", venice.eval(list2).toString());


        final String list3 =
                "(do                                                          " +
                "  (doto (. :java.util.concurrent.CopyOnWriteArrayList :new)  " +
                "        (. :add 1)                                           " +
                "        (. :add (+ 1 2)))                                    " +
                ") ";

        assertEquals("java.util.concurrent.CopyOnWriteArrayList", venice.eval(list3).getClass().getName());
        assertEquals("[1, 3]", venice.eval(list3).toString());


        final String list4 =
                "(do                                           " +
                "  (first (doto (. :java.util.ArrayList :new)  " +
                "               (. :add 1)                     " +
                "               (. :add (+ 1 2))))             " +
                ") ";

        assertEquals(1L, venice.eval(list4));


        final String list5 =
                "(do                                          " +
                "  (rest (doto (. :java.util.ArrayList :new)  " +
                "              (. :add 1)                     " +
                "              (. :add (+ 1 2))))             " +
                ") ";

        assertEquals("[3]", venice.eval(list5).toString());


        final String list6 =
                "(do                                          " +
                "  (nth (doto (. :java.util.ArrayList :new)   " +
                "              (. :add 1)                     " +
                "              (. :add (+ 1 2)))              " +
                "       1)                                    " +
                ") ";

        assertEquals(3L, venice.eval(list6));
    }

    @Test
    public void test_java_list_conversion() {
        final Venice venice = new Venice();

        final String list1 =
                "(do                                               " +
                "   (str                                           " +
                "      (into (list)                                " +
                "            (doto (. :java.util.ArrayList :new)   " +
                "                  (. :add 1)                      " +
                "                  (. :add (+ 1 2)))))             " +
                ") ";

        assertEquals("(3 1)", venice.eval(list1));

        final String list2 =
                "(do                                               " +
                "   (str                                           " +
                "      (list*                                      " +
                "            (doto (. :java.util.ArrayList :new)   " +
                "                  (. :add 1)                      " +
                "                  (. :add (+ 1 2)))))             " +
                ") ";

        assertEquals("(1 3)", venice.eval(list2));

        final String list3 =
                "(do                                               " +
                "   (str                                           " +
                "      (into (vector)                              " +
                "            (doto (. :java.util.ArrayList :new)   " +
                "                  (. :add 1)                      " +
                "                  (. :add (+ 1 2)))))             " +
                ") ";

        assertEquals("[1 3]", venice.eval(list3));
    }

    @Test
    public void test_java_list_into() {
        final Venice venice = new Venice();

        final String list1 =
                "(do                                                                      " +
                "   (str                                                                  " +
                "      (into! (doto (. :java.util.concurrent.CopyOnWriteArrayList :new)   " +
                "                   (. :add 1)                                            " +
                "                   (. :add 2))                                           " +
                "             (doto (. :java.util.ArrayList :new)                         " +
                "                   (. :add 3)                                            " +
                "                   (. :add 4))))                                         " +
                ") ";

        assertEquals("(1 2 3 4)", venice.eval(list1));

        final String list2 =
                "(do                                                " +
                "   (str                                            " +
                "      (into! (doto (. :java.util.ArrayList :new)   " +
                "                   (. :add 1)                      " +
                "                   (. :add 2))                     " +
                "             (doto (. :java.util.ArrayList :new)   " +
                "                   (. :add 3)                      " +
                "                   (. :add 4))))                   " +
                ") ";

        assertEquals("(1 2 3 4)", venice.eval(list2));

        final String list3 =
                "(do                                                " +
                "   (str                                            " +
                "      (into! (doto (. :java.util.ArrayList :new)   " +
                "                   (. :add 1)                      " +
                "                   (. :add 2))                     " +
                "             (doto (. :java.util.HashSet :new)     " +
                "                   (. :add 3))))                   " +
                ") ";

        assertEquals("(1 2 3)", venice.eval(list3));

        final String list4 =
                "(do                                                " +
                "   (str                                            " +
                "      (into! (doto (. :java.util.ArrayList :new)   " +
                "                   (. :add 1)                      " +
                "                   (. :add 2))                     " +
                "             '(3 4)))                              " +
                ") ";

        assertEquals("(1 2 3 4)", venice.eval(list4));
    }

    @Test
    public void test_java_set() {
        final Venice venice = new Venice();

        final String set =
                "(do                                  " +
                "  (doto (. :java.util.HashSet :new)  " +
                "        (. :add :a)                  " +
                "        (. :add :b))                 " +
                ") ";

        assertEquals("java.util.HashSet", venice.eval(set).getClass().getName());
        assertEquals("[a, b]", venice.eval(set).toString());
    }

    @Test
    public void test_java_set_conversion() {
        final Venice venice = new Venice();

        final String set =
                "(do                                          " +
                "  (str                                       " +
                "    (into (set)                              " +
                "          (doto (. :java.util.HashSet :new)  " +
                "                (. :add :a)                  " +
                "                (. :add :b))))               " +
                ") ";

        assertEquals("#{a b}", venice.eval(set).toString());
    }

    @Test
    public void test_java_map() {
        final Venice venice = new Venice();

        final String map =
                "(do                                  " +
                "  (doto (. :java.util.HashMap :new)  " +
                "        (. :put :a 1)                " +
                "        (. :put :b (+ 1 2)))         " +
                ") ";

        assertEquals("java.util.HashMap", venice.eval(map).getClass().getName());
        assertEquals("{a=1, b=3}", venice.eval(map).toString());
    }

    @Test
    public void test_convert_to_VncHashMap() {
        final Venice venice = new Venice();

        final String map1 =
                "(do                                           " +
                "  (hash-map                                   " +
                "     (doto (. :java.util.LinkedHashMap :new)  " +
                "           (. :put :a 1)                      " +
                "           (. :put :b 2)))                    " +
                ") ";

        assertEquals("{a=1, b=2}", venice.eval(map1).toString());

        final String map2 =
                "(type                                         " +
                "  (hash-map                                   " +
                "     (doto (. :java.util.LinkedHashMap :new)  " +
                "           (. :put :a 1)                      " +
                "           (. :put :b 2)))                    " +
                ") ";

        assertEquals("core/hash-map", venice.eval(map2).toString());
    }

    @Test
    public void test_convert_to_VncLinkedMap() {
        final Venice venice = new Venice();

        final String map1 =
                "(do                                           " +
                "  (ordered-map                                " +
                "     (doto (. :java.util.LinkedHashMap :new)  " +
                "           (. :put :a 1)                      " +
                "           (. :put :b 2)))                    " +
                ") ";

        assertEquals("{a=1, b=2}", venice.eval(map1).toString());

        final String map2 =
                "(type                                         " +
                "  (ordered-map                                " +
                "     (doto (. :java.util.LinkedHashMap :new)  " +
                "           (. :put :a 1)                      " +
                "           (. :put :b 2)))                    " +
                ") ";

        assertEquals("core/ordered-map", venice.eval(map2).toString());
    }

    @Test
    public void test_convert_to_VncSortedMap() {
        final Venice venice = new Venice();

        final String map1 =
                "(do                                           " +
                "  (sorted-map                                 " +
                "     (doto (. :java.util.LinkedHashMap :new)  " +
                "           (. :put :a 1)                      " +
                "           (. :put :b 2)))                    " +
                ") ";

        assertEquals("{a=1, b=2}", venice.eval(map1).toString());

        final String map2 =
                "(type                                         " +
                "  (sorted-map                                 " +
                "     (doto (. :java.util.LinkedHashMap :new)  " +
                "           (. :put :a 1)                      " +
                "           (. :put :b 2)))                    " +
                ") ";

        assertEquals("core/sorted-map", venice.eval(map2).toString());
    }

}
