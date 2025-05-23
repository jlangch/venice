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
package com.github.jlangch.venice.modules;

import static com.github.jlangch.venice.impl.util.StringUtil.to_lf;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class JsonlModuleTest {

    // ------------------------------------------------------------------------
    // String write
    // ------------------------------------------------------------------------

    @Test
    public void test_write_str_basic() {
        final Venice venice = new Venice();

        assertEquals(
                null,
                venice.eval("(do                       \n" +
                            "  (load-module :jsonl)    \n" +
                            "  (jsonl/write-str nil))  "));

        assertEquals(
                "true",
                venice.eval("(do                       \n" +
                            "  (load-module :jsonl)    \n" +
                            "  (jsonl/write-str true))  "));

        assertEquals(
                "10",
                venice.eval("(do                       \n" +
                            "  (load-module :jsonl)    \n" +
                            "  (jsonl/write-str 10))  "));

        assertEquals(
                "10.2",
                venice.eval("(do                       \n" +
                            "  (load-module :jsonl)    \n" +
                            "  (jsonl/write-str 10.2)) "));

        assertEquals(
                "{\"a\":1}",
                venice.eval("(do                         \n" +
                            "  (load-module :jsonl)      \n" +
                            "  (jsonl/write-str {:a 1})) "));
    }

    @Test
    public void test_write_str_collection_single_value() {
        final Venice venice = new Venice();

        assertEquals(
                "{\"a\":1}",
                venice.eval("(do                                   \n" +
                            "  (load-module :jsonl)                \n" +
                            "  (jsonl/write-str [{:a 1}]))  "));

        assertEquals(
                "{\"a\":1,\"b\":2}",
                venice.eval("(do                                   \n" +
                            "  (load-module :jsonl)                \n" +
                            "  (jsonl/write-str [{:a 1 :b 2}]))  "));
    }

    @Test
    public void test_write_str_collection_multi_value() {
        final Venice venice = new Venice();

        assertEquals(
                "{\"a\":1}\n{\"b\":2}",
                to_lf(
                    venice.eval("(do                                   \n" +
                                "  (load-module :jsonl)                \n" +
                                "  (jsonl/write-str [{:a 1} {:b 2}]))  ")));

        assertEquals(
                "{\"a\":1}\n{\"b\":2}\n{\"c\":3}",
                to_lf(
                    venice.eval("(do                                          \n" +
                                "  (load-module :jsonl)                       \n" +
                                "  (jsonl/write-str [{:a 1} {:b 2} {:c 3}]))  ")));

        assertEquals(
                "{\"a\":1,\"b\":2}\n{\"c\":3,\"d\":4}",
                to_lf(
                    venice.eval("(do                                             \n" +
                                "  (load-module :jsonl)                          \n" +
                                "  (jsonl/write-str [{:a 1 :b 2} {:c 3 :d 4}]))  ")));

        assertEquals(
                "{\"a\":1,\"d\":1}\n{\"b\":2,\"d\":2}\n{\"c\":3,\"d\":3}",
                to_lf(
                    venice.eval("(do                                                         \n" +
                                "  (load-module :jsonl)                                      \n" +
                                "  (jsonl/write-str [{:a 1 :d 1} {:b 2 :d 2} {:c 3 :d 3}]))  ")));
    }


    // ------------------------------------------------------------------------
    // String read
    // ------------------------------------------------------------------------

    @Test
    public void test_read_str_basic() {
        final Venice venice = new Venice();

        assertEquals(
                "()",
                venice.eval("(do                                \n" +
                            "  (load-module :jsonl)             \n" +
                            "  (pr-str (jsonl/read-str nil)))   "));

        assertEquals(
                "(true)",
                venice.eval("(do                                    \n" +
                            "  (load-module :jsonl)                 \n" +
                            "  (pr-str (jsonl/read-str \"true\")))  "));

        assertEquals(
                "(10)",
                venice.eval("(do                                  \n" +
                            "  (load-module :jsonl)               \n" +
                            "  (pr-str (jsonl/read-str \"10\")))  "));

        assertEquals(
                "(10.2)",
                venice.eval("(do                                   \n" +
                            "  (load-module :jsonl)                \n" +
                            "  (pr-str (jsonl/read-str \"10.2\"))) "));
    }

    @Test
    public void test_read_str_collection_single_value() {
        final Venice venice = new Venice();

        assertEquals(
                "(1 2 3)",
                venice.eval("(do                                        \n" +
                            "  (load-module :jsonl)                     \n" +
                            "  (pr-str (jsonl/read-str \"1\n2\n3\")))   "));

        assertEquals(
                "(1 2 3)",
                venice.eval("(do                                              \n" +
                            "  (load-module :jsonl)                           \n" +
                            "  (pr-str (jsonl/read-str \"1\n\n2\n3\n\n\")))   "));
    }

    @Test
    public void test_read_str_collection_multi_value() {
        final Venice venice = new Venice();

        assertEquals(
                "({:a 100 :b 200} {:a 101 :b 201} {:a 102 :b 202})",
                venice.eval("(do                                                      \n" +
                            "  (load-module :jsonl)                                   \n" +
                            "  (try-with [sw (io/string-writer)]                      \n" +
                            "     (println sw (json/write-str {:a 100 :b 200}))       \n" +
                            "     (println sw (json/write-str {:a 101 :b 201}))       \n" +
                            "     (println sw (json/write-str {:a 102 :b 202}))       \n" +
                            "     (flush sw)                                          \n" +
                            "     (let [json @sw]                                     \n" +
                            "        (pr-str (jsonl/read-str json :key-fn keyword)))))"));
    }


    // ------------------------------------------------------------------------
    // Spit
    // ------------------------------------------------------------------------

    @Test
    public void test_spit() {
        final Venice venice = new Venice();

        final String script1 =  "(do                                                      \n" +
                                "  (load-module :jsonl)                                   \n" +
                                "  (try-with [sw (io/string-writer)]                      \n" +
                                "     (jsonl/spit sw {:a 100 :b 200})                     \n" +
                                "     (flush sw)                                          \n" +
                                "     @sw))                                               ";

        final String script2 =  "(do                                                      \n" +
                                "  (load-module :jsonl)                                   \n" +
                                "  (try-with [sw (io/string-writer)]                      \n" +
                                "     (jsonl/spit sw [{:a 100 :b 200}                     \n" +
                                "                     {:a 101 :b 201}])                   \n" +
                                "     (flush sw)                                          \n" +
                                "     @sw))                                               ";

        final String script3 =  "(do                                                      \n" +
                                "  (load-module :jsonl)                                   \n" +
                                "  (try-with [sw (io/string-writer)]                      \n" +
                                "     (jsonl/spit sw [{:a 100 :b 200}                     \n" +
                                "                     {:a 101 :b 201}                     \n" +
                                "                     {:a 102 :b 202}])                   \n" +
                                "     (flush sw)                                          \n" +
                                "     @sw))                                               ";

        final String script4 =  "(do                                                      \n" +
                                "  (load-module :jsonl)                                   \n" +
                                "  (try-with [sw (io/string-writer)]                      \n" +
                                "     (jsonl/spit sw [{:a 100 :b 200}                     \n" +
                                "                     {:a 101 :b 201}                     \n" +
                                "                     {:a 102 :b 202}])                   \n" +
                                "     (flush sw)                                          \n" +
                                "     (let [json @sw]                                     \n" +
                                "        (pr-str (jsonl/read-str json :key-fn keyword)))))";


        assertEquals(
                "{\"a\":100,\"b\":200}",
                venice.eval(script1));

        assertEquals(
                "{\"a\":100,\"b\":200}\n{\"a\":101,\"b\":201}",
                to_lf(venice.eval(script2)));

        assertEquals(
                "{\"a\":100,\"b\":200}\n{\"a\":101,\"b\":201}\n{\"a\":102,\"b\":202}",
                to_lf(venice.eval(script3)));

        assertEquals(
                "({:a 100 :b 200} {:a 101 :b 201} {:a 102 :b 202})",
                venice.eval(script4));
    }

    @Test
    public void test_spit_line_by_line() {
        final Venice venice = new Venice();

        final String script1 =  "(do                                                      \n" +
                                "  (load-module :jsonl)                                   \n" +
                                "  (try-with [sw (io/string-writer)]                      \n" +
                                "     (jsonl/spit sw {:a 100 :b 200})                     \n" +
                                "     (println sw)                                        \n" +
                                "     (jsonl/spit sw {:a 101 :b 201})                     \n" +
                                "     (println sw)                                        \n" +
                                "     (jsonl/spit sw {:a 102 :b 202})                     \n" +
                                "     (flush sw)                                          \n" +
                                "     (let [json @sw]                                     \n" +
                                "        (pr-str (jsonl/read-str json :key-fn keyword)))))";

        assertEquals(
                "({:a 100 :b 200} {:a 101 :b 201} {:a 102 :b 202})",
                venice.eval(script1));
    }

    @Test
    public void test_spitln() {
        final Venice venice = new Venice();

        final String script1 =  "(do                                                      \n" +
                                "  (load-module :jsonl)                                   \n" +
                                "  (try-with [sw (io/string-writer)]                      \n" +
                                "     (jsonl/spitln sw {:a 100 :b 200})                   \n" +
                                "     (flush sw)                                          \n" +
                                "     @sw))                                               ";

        final String script2 =  "(do                                                      \n" +
                                "  (load-module :jsonl)                                   \n" +
                                "  (try-with [sw (io/string-writer)]                      \n" +
                                "     (jsonl/spitln sw [{:a 100 :b 200}                   \n" +
                                "                       {:a 101 :b 201}])                 \n" +
                                "     (flush sw)                                          \n" +
                                "     @sw))                                               ";

        final String script3 =  "(do                                                      \n" +
                                "  (load-module :jsonl)                                   \n" +
                                "  (try-with [sw (io/string-writer)]                      \n" +
                                "     (jsonl/spitln sw [{:a 100 :b 200}                   \n" +
                                "                       {:a 101 :b 201}                   \n" +
                                "                       {:a 102 :b 202}])                 \n" +
                                "     (flush sw)                                          \n" +
                                "     @sw))                                               ";

        final String script4 =  "(do                                                      \n" +
                                "  (load-module :jsonl)                                   \n" +
                                "  (try-with [sw (io/string-writer)]                      \n" +
                                "     (jsonl/spitln sw {:a 100 :b 200})                   \n" +
                                "     (jsonl/spitln sw {:a 101 :b 201})                   \n" +
                                "     (jsonl/spit sw {:a 102 :b 202})                     \n" +
                                "     (flush sw)                                          \n" +
                                "     @sw))                                               ";

        final String script5 =  "(do                                                      \n" +
                                "  (load-module :jsonl)                                   \n" +
                                "  (try-with [sw (io/string-writer)]                      \n" +
                                "     (jsonl/spitln sw [{:a 100 :b 200}                   \n" +
                                "                       {:a 101 :b 201}                   \n" +
                                "                       {:a 102 :b 202}])                 \n" +
                                "     (flush sw)                                          \n" +
                                "     (let [json @sw]                                     \n" +
                                "        (pr-str (jsonl/read-str json :key-fn keyword)))))";


        assertEquals(
                "{\"a\":100,\"b\":200}\n",
                to_lf(venice.eval(script1)));

        assertEquals(
                "{\"a\":100,\"b\":200}\n{\"a\":101,\"b\":201}\n",
                to_lf(venice.eval(script2)));

        assertEquals(
                "{\"a\":100,\"b\":200}\n{\"a\":101,\"b\":201}\n{\"a\":102,\"b\":202}\n",
                to_lf(venice.eval(script3)));

        assertEquals(
                "{\"a\":100,\"b\":200}\n{\"a\":101,\"b\":201}\n{\"a\":102,\"b\":202}",
                to_lf(venice.eval(script4)));

        assertEquals(
                "({:a 100 :b 200} {:a 101 :b 201} {:a 102 :b 202})",
                venice.eval(script5));
    }


    // ------------------------------------------------------------------------
    // Slurp
    // ------------------------------------------------------------------------

    @Test
    public void test_slurp() {
        final Venice venice = new Venice();

        final String script1 =  "(do                                                      \n" +
                                "  (load-module :jsonl)                                   \n" +
                                "  (defn test-data []                                     \n" +
                                "    (try-with [sw (io/string-writer)]                    \n" +
                                "      (println sw (json/write-str {:a 100 :b 200}))      \n" +
                                "      (println sw (json/write-str {:a 101 :b 201}))      \n" +
                                "      (println sw (json/write-str {:a 102 :b 202}))      \n" +
                                "      (flush sw)                                         \n" +
                                "      @sw))                                              \n" +
                                "  (let [json (test-data)]                                \n" +
                                "    (try-with [rd (io/buffered-reader json)]             \n" +
                                "      (pr-str (jsonl/slurp rd :key-fn keyword)))))       ";

        assertEquals(
                "({:a 100 :b 200} {:a 101 :b 201} {:a 102 :b 202})",
                venice.eval(script1));
    }

    @Test
    public void test_slurp_filter() {
        final Venice venice = new Venice();

        final String script1 =  "(do                                                        \n" +
                                "  (load-module :jsonl)                                     \n" +
                                "  (defn test-data []                                       \n" +
                                "    (try-with [sw (io/string-writer)]                      \n" +
                                "      (println sw (json/write-str {:a 100 :b 200}))        \n" +
                                "      (println sw (json/write-str {:a 101 :b 201}))        \n" +
                                "      (println sw (json/write-str {:a 100 :b 202}))        \n" +
                                "      (flush sw)                                           \n" +
                                "      @sw))                                                \n" +
                                "  (let [json (test-data)]                                  \n" +
                                "    (try-with [rd (io/buffered-reader json)]               \n" +
                                "      (pr-str (jsonl/slurp rd                              \n" +
                                "                           :key-fn keyword                 \n" +
                                "                           :filter-fn #(= 100 (:a %))))))) ";

        assertEquals(
                "({:a 100 :b 200} {:a 100 :b 202})",
                venice.eval(script1));
    }

    @Test
    public void test_slurp_laze_sequence() {
        final Venice venice = new Venice();

        final String script1 =  "(do                                                              \n" +
                                "  (load-module :jsonl)                                           \n" +
                                "  (defn test-data []                                             \n" +
                                "    (try-with [sw (io/string-writer)]                            \n" +
                                "      (println sw (json/write-str {:a 100 :b 200}))              \n" +
                                "      (println sw (json/write-str {:a 101 :b 201}))              \n" +
                                "      (println sw (json/write-str {:a 102 :b 202}))              \n" +
                                "      (flush sw)                                                 \n" +
                                "      @sw))                                                      \n" +
                                "  (let [json (test-data)]                                        \n" +
                                "    (try-with [rd (io/buffered-reader json)]                     \n" +
                                "      (let [slurper (jsonl/lazy-seq-slurper rd :key-fn keyword)] \n" +
                                "        ;; realize the lazy sequence                             \n" +
                                "        (pr-str (doall slurper))))))                             ";

        assertEquals(
                "({:a 100 :b 200} {:a 101 :b 201} {:a 102 :b 202})",
                venice.eval(script1));
    }

    @Test
    public void test_slurp_with_transducer() {
        final Venice venice = new Venice();

        final String script1 =  "(do                                                                \n" +
                                "  (load-module :jsonl)                                             \n" +
                                "                                                                   \n" +
                                "  (defn test-data []                                               \n" +
                                "    (try-with [sw (io/string-writer)]                              \n" +
                                "      (println sw (json/write-str {:a 100 :b 200 :c 300}))         \n" +
                                "      (println sw (json/write-str {:a 101 :b 201 :c 301}))         \n" +
                                "      (println sw (json/write-str {:a 100 :b 202 :c 302}))         \n" +
                                "      (flush sw)                                                   \n" +
                                "      @sw))                                                        \n" +
                                "                                                                   \n" +
                                "  (def xform (comp (map #(dissoc % :c))                            \n" +
                                "                   (map #(update % :b (fn [x] (+ x 5))))           \n" +
                                "                   (filter #(= 100 (:a %)))))                      \n" +
                                "                                                                   \n" +
                                "  (let [json (test-data)]                                          \n" +
                                "    (try-with [rd (io/buffered-reader json)]                       \n" +
                                "      (let [slurper (jsonl/lazy-seq-slurper rd :key-fn keyword)]   \n" +
                                "        ;; transduce the lazy sequence                             \n" +
                                "        (pr-str (transduce xform conj slurper))))))                ";

        assertEquals(
                "[{:a 100 :b 205} {:a 100 :b 207}]",
                venice.eval(script1));
    }
}

