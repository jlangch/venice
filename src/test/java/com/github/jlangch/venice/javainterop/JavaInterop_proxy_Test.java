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
package com.github.jlangch.venice.javainterop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class JavaInterop_proxy_Test {

    @Test
    public void test_proxy_FilenameFilter() {
        final Venice venice = new Venice();

        final String script =
                "(do                                           " +
                "  (def filter-fn (fn [path name] true))       " +
                "  (def dir (. :java.io.File :new \"/tmp\"))   " +
                "  (. dir :list                                " +
                "         (proxify                             " +
                "             :java.io.FilenameFilter          " +
                "             {:accept filter-fn}))            " +
                ") ";

        System.out.println(venice.eval(script));
    }

    @Test
    public void test_proxy_Predicate() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                              " +
                "    (import :com.github.jlangch.venice.support.Functions)        " +
                "    (import :java.util.function.Predicate)                       " +
                "                                                                 " +
                "    (def pred-fn (fn[x] (== x \"abc\")))                         " +
                "                                                                 " +
                "    (def pred-fn-proxy (proxify :Predicate { :test pred-fn }))   " +
                "                                                                 " +
                "    (let [functions (. :Functions :new)]                         " +
                "         (. functions :evalPredicate                             " +
                "                      pred-fn-proxy                              " +
                "                      \"abc\" ))                                 " +
                ") ";

        assertEquals(true, venice.eval(script));
    }

    @Test
    public void test_proxy_Predicate_class() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                              " +
                "    (import :com.github.jlangch.venice.support.Functions)        " +
                "    (import :java.util.function.Predicate)                       " +
                "                                                                 " +
                "    (def pred-fn (fn[x] (== x \"abc\")))                         " +
                "                                                                 " +
                "    (def pred-fn-proxy                                           " +
                "         (proxify (class :Predicate) { :test pred-fn }))         " +
                "                                                                 " +
                "    (let [functions (. :Functions :new)]                         " +
                "         (. functions :evalPredicate                             " +
                "                      pred-fn-proxy                              " +
                "                      \"abc\" ))                                 " +
                ") ";

        assertEquals(true, venice.eval(script));
    }

    @Test
    @Disabled
    public void test_proxy_SwingInvoker() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                        " +
                "   (import :java.lang.Runnable)                                            " +
                "   (import :javax.swing.JPanel)                                            " +
                "   (import :javax.swing.JFrame)                                            " +
                "   (import :javax.swing.JLabel)                                            " +
                "   (import :javax.swing.SwingUtilities)                                    " +
                "                                                                           " +
                "   (def swing-open-window                                                  " +
                "        (fn [title]                                                        " +
                "            (let [frame (. :JFrame :new title)                             " +
                "                  label (. :JLabel :new \"Hello World\")                   " +
                "                  closeOP (. :JFrame :EXIT_ON_CLOSE)]                      " +
                "                 (. frame :setDefaultCloseOperation closeOP)               " +
                "                 (. frame :add label)                                      " +
                "                 (. frame :setSize 200 200)                                " +
                "                 (. frame :setVisible true))))                             " +
                "                                                                           " +
                "   (def swing-view                                                         " +
                "        (fn [title]                                                        " +
                "            (. :SwingUtilities :invokeLater                                " +
                "               (proxify :Runnable { :run #(swing-open-window title) }))))  " +
                "                                                                           " +
                "   (swing-view \"test\")                                                   " +
                "   (sleep 20000)                                                           " +
                ") ";

        venice.eval(script);
    }

    @Test
    public void test_proxy_Streams_filter() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                              " +
                "    (import :java.util.function.Predicate)                       " +
                "    (import :java.util.stream.Collectors)                        " +
                "                                                                 " +
                "    (-> (. [1 2 3 4] :stream)                                    " +
                "        (. :filter (proxify :Predicate { :test #(> % 2) }))      " +
                "        (. :collect (. :Collectors :toList)))                    " +
                ") ";

        assertEquals("[3, 4]", venice.eval(script).toString());
    }

    @Test
    public void test_proxy_Streams_filter2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                              " +
                "    (import :java.util.stream.Collectors)                        " +
                "    (defn pred [x] (> x 2))                                      " +
                "                                                                 " +
                "    (-> (. [1 2 3 4] :stream)                                    " +
                "        (. :filter (as-predicate pred))                          " +
                "        (. :collect (. :Collectors :toList)))                    " +
                ") ";

        assertEquals("[3, 4]", venice.eval(script).toString());
    }

    @Test
    public void test_proxy_Streams_filter3() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                              " +
                "    (import :java.util.stream.Collectors)                        " +
                "                                                                 " +
                "    (-> (. [1 2 3 4] :stream)                                    " +
                "        (. :filter (as-predicate #(> % 2)))                      " +
                "        (. :collect (. :Collectors :toList)))                    " +
                ") ";

        assertEquals("[3, 4]", venice.eval(script).toString());
    }

    @Test
    public void test_proxy_Streams_filter_parallel() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                              " +
                "    (import :java.util.stream.Collectors)                        " +
                "                                                                 " +
                "    (-> (. [1 2 3 4 5 6 7 8 9 10 11 12 13 14] :parallelStream)   " +
                "        (. :filter (as-predicate #(> % 2)))                      " +
                "        (. :collect (. :Collectors :toList)))                    " +
                ") ";

        assertEquals(
                "[3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14]",
                venice.eval(script).toString());
    }

    @Test
    public void test_proxy_Streams_filter_map() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                              " +
                "    (import :java.util.function.Predicate)                       " +
                "    (import :java.util.function.Function)                        " +
                "    (import :java.util.stream.Collectors)                        " +
                "                                                                 " +
                "    (-> (. [1 2 3 4] :stream)                                    " +
                "        (. :filter (proxify :Predicate { :test #(> % 2) }))      " +
                "        (. :map (proxify :Function { :apply #(* % 10) }))        " +
                "        (. :collect (. :Collectors :toList)))                    " +
                ") ";

        assertEquals("[30, 40]", venice.eval(script).toString());
    }

    @Test
    public void test_proxy_Streams_filter_map2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                              " +
                "    (import :java.util.stream.Collectors)                        " +
                "    (defn pred [x] (> x 2))                                      " +
                "    (defn mul [x] (* x 10))                                      " +
                "                                                                 " +
                "    (-> (. [1 2 3 4] :stream)                                    " +
                "        (. :filter (as-predicate pred))                          " +
                "        (. :map (as-function mul))                               " +
                "        (. :collect (. :Collectors :toList)))                    " +
                ") ";

        assertEquals("[30, 40]", venice.eval(script).toString());
    }

    @Test
    public void test_proxy_Streams_filter_map3() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                              " +
                "    (import :java.util.stream.Collectors)                        " +
                "                                                                 " +
                "    (-> (. [1 2 3 4 5 6 7 8] :stream)                            " +
                "        (. :filter (as-predicate #(> % 2)))                      " +
                "        (. :map (as-function #(* % 10)))                         " +
                "        (. :collect (. :Collectors :toList)))                    " +
                ") ";

        assertEquals("[30, 40, 50, 60, 70, 80]", venice.eval(script).toString());
    }

    @Test
    public void test_proxy_Streams_filter_map_parallel() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                              " +
                "    (import :java.util.stream.Collectors)                        " +
                "                                                                 " +
                "    (-> (. [1 2 3 4 5 6 7 8] :parallelStream)                    " +
                "        (. :filter (as-predicate #(> % 2)))                      " +
                "        (. :map (as-function #(* % 10)))                         " +
                "        (. :collect (. :Collectors :toList)))                    " +
                ") ";

        assertEquals("[30, 40, 50, 60, 70, 80]", venice.eval(script).toString());
    }

    @Test
    public void test_proxy_Streams_reduce() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                      " +
                "    (import :java.util.function.BinaryOperator)                          " +
                "                                                                         " +
                "    (-> (. [1 2 3 4] :stream)                                            " +
                "        (. :reduce 0 (proxify :BinaryOperator { :apply #(+ %1 %2) })))   " +
                ") ";

        assertEquals("10", venice.eval(script).toString());
    }

    @Test
    public void test_proxy_Streams_reduce2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                     " +
                "    (-> (. [1 2 3 4] :stream)                           " +
                "        (. :reduce 0 (as-binaryoperator #(+ %1 %2))))   " +
                ") ";

        assertEquals("10", venice.eval(script).toString());
    }

    @Test
    public void test_proxy_Streams_reduce_parallel() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                     " +
                "    (-> (. [1 2 3 4] :parallelStream)                   " +
                "        (. :reduce 0 (as-binaryoperator #(+ %1 %2))))   " +
                ") ";

        assertEquals("10", venice.eval(script).toString());
    }

    @Test
    public void test_proxy_Streams_reduce_optional() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                      " +
                "    (import :java.util.function.BinaryOperator)                          " +
                "                                                                         " +
                "    (-> (. [1 2 3 4] :stream)                                            " +
                "        (. :reduce (proxify :BinaryOperator { :apply #(+ %1 %2) }))      " +
                "        (. :orElse 0))                                                   " +
                ") ";

        assertEquals("10", venice.eval(script).toString());
    }

    @Test
    public void test_proxy_Streams_reduce_optional2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                   " +
                "    (-> (. [1 2 3 4] :stream)                         " +
                "        (. :reduce (as-binaryoperator #(+ %1 %2)))    " +
                "        (. :orElse 0))                                " +
                ") ";

        assertEquals("10", venice.eval(script).toString());
    }

}
