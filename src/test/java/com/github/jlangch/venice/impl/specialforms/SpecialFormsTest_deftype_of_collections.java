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
package com.github.jlangch.venice.impl.specialforms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.AssertionException;
import com.github.jlangch.venice.Venice;


public class SpecialFormsTest_deftype_of_collections {

    @Test
    public void test_deftype_of_vector_large() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                      \n" +
                "  (deftype-of :colors :vector)                           \n" +
                "  (first (colors. [:red :green :blue :white :yellow])))  ";

        assertEquals("red", venice.eval(script));
    }

    @Test
    public void test_deftype_of_vector_large_validation_OK() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                      \n" +
                "  (deftype-of :colors :vector #(every? keyword? %))      \n" +
                "  (first (colors. [:red :green :blue :white :yellow])))  ";

        assertEquals("red", venice.eval(script));
    }

    @Test
    public void test_deftype_of_vector_large_validation_FAIL() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                      \n" +
                "  (deftype-of :colors :vector #(every? keyword? %))      \n" +
                "  (first (colors. [:red :green :blue 1000 :white :yellow])))  ";

        assertThrows(AssertionException.class, () -> venice.eval(script));
    }

    @Test
    public void test_deftype_of_vector_tiny() {
        final Venice venice = new Venice();

        final String script =
                "(do                                       \n" +
                "  (deftype-of :colors :vector)            \n" +
                "  (first (colors. [:red :green :blue])))  ";

        assertEquals("red", venice.eval(script));
    }

    @Test
    public void test_deftype_of_vector_tiny_validation_OK() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                 \n" +
                "  (deftype-of :colors :vector #(every? keyword? %)) \n" +
                "  (first (colors. [:red :green :blue])))            ";

        assertEquals("red", venice.eval(script));
    }

    @Test
    public void test_deftype_of_vector_tiny_validation_FAIL() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                 \n" +
                "  (deftype-of :colors :vector #(every? keyword? %)) \n" +
                "  (first (colors. [:red 1000 :blue])))              ";

        assertThrows(AssertionException.class, () -> venice.eval(script));
    }



    @Test
    public void test_deftype_of_list_large() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                       \n" +
                "  (deftype-of :colors :list)                              \n" +
                "  (first (colors. '(:red :green :blue :white :yellow))))  ";

        assertEquals("red", venice.eval(script));
    }

    @Test
    public void test_deftype_of_list_large_validation_OK() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                      \n" +
                "  (deftype-of :colors :list #(every? keyword? %))        \n" +
                "  (first (colors. '(:red :green :blue :white :yellow)))) ";

        assertEquals("red", venice.eval(script));
    }

    @Test
    public void test_deftype_of_list_large_validation_FAIL() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                      \n" +
                "  (deftype-of :colors :list #(every? keyword? %))        \n" +
                "  (first (colors. '(:red 1000 :blue :white :yellow))))   ";

        assertThrows(AssertionException.class, () -> venice.eval(script));
    }

    @Test
    public void test_deftype_of_list_tiny() {
        final Venice venice = new Venice();

        final String script =
                "(do                                       \n" +
                "  (deftype-of :colors :list)              \n" +
                "  (first (colors. '(:red :green :blue))))  ";

        assertEquals("red", venice.eval(script));
    }

    @Test
    public void test_deftype_of_list_tiny_validation_OK() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                 \n" +
                "  (deftype-of :colors :list #(every? keyword? %))   \n" +
                "  (first (colors. '(:red :green :blue))))           ";

        assertEquals("red", venice.eval(script));
    }

    @Test
    public void test_deftype_of_list_tiny_validation_FAIL() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                 \n" +
                "  (deftype-of :colors :list #(every? keyword? %))   \n" +
                "  (first (colors. '(:red 1000 :blue))))             ";

        assertThrows(AssertionException.class, () -> venice.eval(script));
    }


    @Test
    public void test_deftype_of_hash_set() {
        final Venice venice = new Venice();

        final String script =
                "(do                                           \n" +
                "  (deftype-of :units :hash-set)               \n" +
                "  (contains? (units. #{:mm :cm \"m\"}) :mm))  ";

        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void test_deftype_of_hash_set_validation_OK() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                     \n" +
                "  (deftype-of :units :hash-set #(every? keyword? %))    \n" +
                "  (contains? (units. #{:mm :cm :m}) :mm))               ";

        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void test_deftype_of_hash_set_validation_FAIL() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                     \n" +
                "  (deftype-of :units :hash-set #(every? keyword? %))    \n" +
                "  (contains? (units. #{:mm :cm \"m\"}) :mm))            ";

        assertThrows(AssertionException.class, () -> venice.eval(script));
    }

}
