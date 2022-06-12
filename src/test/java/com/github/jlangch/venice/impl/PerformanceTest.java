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
package com.github.jlangch.venice.impl;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class PerformanceTest {

    @BeforeAll
    public static void test() {
        System.out.println("Performance tests:");
    }

    @Test
    public void test1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                         \n" +
                "   (defn format                                             \n" +
                "           [name profession born]                           \n" +
                "           (str \"The person named \"                       \n" +
                "                name                                        \n" +
                "                \" works as a \"                            \n" +
                "                profession                                  \n" +
                "                \" and was born in \"                       \n" +
                "                born))                                      \n" +
                "   (dotimes                                                 \n" +
                "      [n 2000]                                              \n" +
                "      (format \"John\" \"farmer\" \"Lucerne\"))             \n" +
                "   (gc) (gc)                                                \n" +
                "                                                            \n" +
                "   (time (format \"John\" \"farmer\" \"Lucerne\")))           ";

        venice.eval(script);
    }

    @Test
    public void test2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                         \n" +
                "   (defn format                                             \n" +
                "           [name profession born]                           \n" +
                "           (str/format \"The person named %s works as a %s and was born in %s\" \n" +
                "                       name profession born))               \n" +
                "   (dotimes                                                 \n" +
                "      [n 2000]                                              \n" +
                "      (format \"John\" \"farmer\" \"Lucerne\"))             \n" +
                "   (gc) (gc)                                                \n" +
                "                                                            \n" +
                "   (time (format \"John\" \"farmer\" \"Lucerne\")))           ";

        venice.eval(script);
    }

    @Test
    public void test3() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                         \n" +
                "   (defn format                                             \n" +
                "           [name profession born]                           \n" +
                "           \"\"\"The person named ~{name} works as a ~{profession} and was born in ~{born}\"\"\") \n" +
                "   (dotimes                                                 \n" +
                "      [n 2000]                                              \n" +
                "      (format \"John\" \"farmer\" \"Lucerne\"))             \n" +
                "   (gc) (gc)                                                \n" +
                "                                                            \n" +
                "   (time (format \"John\" \"farmer\" \"Lucerne\")))           ";

        venice.eval(script);
    }

    @Test
    public void test4() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                     \n" +
                "   (defn sum [n]                                        \n" +
                "         (loop [i 0]                                    \n" +
                "            (if (< i n)                                 \n" +
                "               (recur (inc i))                          \n" +
                "               i)))                                     \n" +
                "                                                        \n" +
                "   (sum 100000) (gc) (gc)                               \n" +
                "                                                        \n" +
                "   (time (sum 100000)))                                   ";

        venice.eval(script);
    }

    @Test
    public void test5() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                     \n" +
                "   (defn sum [n]                                        \n" +
                "         (loop [cnt n, acc 0]                           \n" +
                "            (if (zero? cnt)                             \n" +
                "               acc                                      \n" +
                "               (recur (dec cnt) (+ acc cnt)))))         \n" +
                "                                                        \n" +
                "   (sum 100000) (gc) (gc)                               \n" +
                "                                                        \n" +
                "   (time (sum 100000)))                                   ";

        venice.eval(script);
    }

    @Test
    public void test_profile_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                           \n" +
                "   (perf (* (+ 1 2) 3) 12000 1000)                            \n" +
                "   (println (prof :data-formatted \"Metrics (* (+ 1 2) 3)\")))  ";

        venice.eval(script);
    }

    @Test
    public void test_profile_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                     \n" +
                "   (defn sum [n]                                        \n" +
                "         (loop [cnt n, acc 0]                           \n" +
                "            (if (zero? cnt)                             \n" +
                "               acc                                      \n" +
                "               (recur (dec cnt) (+ acc cnt)))))         \n" +
                "   (perf (sum 300) 2000 1000)                           \n" +
                "   (println (prof :data-formatted \"Metrics loop\")))     ";

        venice.eval(script);
    }

    @Test
    public void test_profile_3() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                     \n" +
                "   (defn test [] (and true true true false))            \n" +
                "   (perf (test) 20000 1000)                             \n" +
                "   (println (prof :data-formatted \"Metrics and\")))     ";

        venice.eval(script);
    }

}
