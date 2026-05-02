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
package com.github.jlangch.venice.javainterop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.util.CollectionUtil;


public class JavaInterop_convert_Test {

    @Test
    public void test_convert_venice2java() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                  \n" +
                "  (let [val (venice->java '(1 2 3))]                 \n" +
                "    (assert (= :java.util.ArrayList (type val)))))   ";

        venice.eval(script);
    }

    @Test
    public void test_java2venice_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                               \n" +
                "  (let [val (->> (venice->java '(1 2 3))          \n" +
                "                 (java->venice))]                 \n" +
                "    (assert (= :core/list (type val)))            \n" +
                "    (assert (= :core/long (type (nth val 0))))))  ";

        venice.eval(script);
    }

    @Test
    public void test_java2venice_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                                          \n" +
                "  (->> (. :com.github.jlangch.venice.javainterop.JavaInterop_convert_Test$Factory :list)     \n" +
                "       (map java->venice)                                                                    \n" +
                "       (map keyword)                                                                         \n" +
                "       (pr-str)))                                                                            ";

        assertEquals("(:a :b :c)", venice.eval(script));
    }


    public static class Factory {

        public static List<String>  list() {
            return CollectionUtil.toList("a", "b", "c");
        }
    }
}
