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
package com.github.jlangch.venice.modules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class KiraModuleTest {

    // ------------------------------------------------------------------------
    // Utility functions
    // ------------------------------------------------------------------------


    @Test
    public void test_load() {
        assertEquals("[kira, loaded]", new Venice().eval("(load-module :kira)")
                                                   .toString());
    }

    @Test
    public void test_escape_xml() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                  \n" +
                "   (load-module :kira)                               \n" +
                "                                                     \n" +
                "   (kira/escape-xml \"\"\"a < > & ' \" b\"\"\")      \n" +
                ")";

        assertEquals("a &lt; &gt; &amp; &apos; &quot; b", venice.eval(script));
    }

    @Test
    public void test_escape_xml_transform() {
        final Venice venice = new Venice();

        final String script1 =
                "(do                                   \n" +
                "   (load-module :kira)                \n" +
                "                                      \n" +
                "   (kira/escape-xml                   \n" +
                "       \"a < > b\"                    \n" +
                "       str/upper-case)                \n" +
                ")";

        assertEquals("A &lt; &gt; B", venice.eval(script1));

        final String script2 =
                "(do                                   \n" +
                "   (load-module :kira)                \n" +
                "                                      \n" +
                "   (kira/escape-xml                   \n" +
                "       \"a < > b\"                    \n" +
                "       #(str/upper-case %))           \n" +
                ")";

        assertEquals("A &lt; &gt; B", venice.eval(script2));
    }

    @Test
    public void test_escape_xml_nested() {
        final Venice venice = new Venice();

        final String script1 =
                "(do                                                                       \n" +
                "   (load-module :kira)                                                    \n" +
                "                                                                          \n" +
                "   (kira/eval                                                             \n" +
                "      \"${ (doseq [x data] }$line: ${= (kira/escape-xml x) }$\n${ ) }$\"  \n" +
                "      [\"${\" \"}$\"]                                                     \n" +
                "      {:data [1 2]})                                                      \n" +
                ")";

        assertEquals("line: 1\nline: 2\n", venice.eval(script1));
    }



    // ------------------------------------------------------------------------
    // Evaluation
    // ------------------------------------------------------------------------

    @Test
    public void test_eval_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                     \n" +
                "   (load-module :kira)                  \n" +
                "                                        \n" +
                "   (kira/eval \"foo\")                  \n" +
                ")";

        assertEquals("foo", venice.eval(script));
    }

    @Test
    public void test_eval_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                     \n" +
                "   (load-module :kira)                  \n" +
                "                                        \n" +
                "   (kira/eval \"<%= 10 %>\")            \n" +
                ")";

        assertEquals("10", venice.eval(script));
    }

    @Test
    public void test_eval_2_delim_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                              \n" +
                "   (load-module :kira)                           \n" +
                "                                                 \n" +
                "   (kira/eval \"<%= 10 %>\" [\"<%\" \"%>\"] {})  \n" +
                ")";

        assertEquals("10", venice.eval(script));
    }

    @Test
    public void test_eval_2_delim_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                              \n" +
                "   (load-module :kira)                           \n" +
                "                                                 \n" +
                "   (kira/eval \"$= 10 $\" [\"$\" \"$\"] {})      \n" +
                ")";

        assertEquals("10", venice.eval(script));
    }

    @Test
    public void test_eval_3a() {
        final Venice venice = new Venice();

        final String script =
                "(do                                      \n" +
                "   (load-module :kira)                   \n" +
                "                                         \n" +
                "   (kira/eval \"<%= x %>\" {:x \"foo\"}) \n" +
                ")";

        assertEquals("foo", venice.eval(script));
    }

    @Test
    public void test_eval_3b() {
        final Venice venice = new Venice();

        final String script =
                "(do                                      \n" +
                "   (load-module :kira)                   \n" +
                "                                         \n" +
                "   (kira/eval \"<%=x%>\" {:x \"foo\"}) \n" +
                ")";

        assertEquals("foo", venice.eval(script));
    }

    @Test
    public void test_eval_3_delim_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                       \n" +
                "   (load-module :kira)                                    \n" +
                "                                                          \n" +
                "   (kira/eval \"<%= x %>\" [\"<%\" \"%>\"] {:x \"foo\"})  \n" +
                ")";

        assertEquals("foo", venice.eval(script));
    }

    @Test
    public void test_eval_3_delim_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                            \n" +
                "   (load-module :kira)         \n" +
                "                               \n" +
                "   (kira/eval \"$= x $\"       \n" +
                "              [\"$\" \"$\"]    \n" +
                "              {:x \"foo\"})    \n" +
                ")";

        assertEquals("foo", venice.eval(script));
    }

    @Test
    public void test_eval_4() {
        final Venice venice = new Venice();

        final String script =
                "(do                                               \n" +
                "   (load-module :kira)                            \n" +
                "                                                  \n" +
                "   (kira/eval \"<% (dotimes [x 3] %>foo<% ) %>\") \n" +
                ")";

        assertEquals("foofoofoo", venice.eval(script));
    }

    @Test
    public void test_eval_5() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                          \n" +
                "   (load-module :kira)                                       \n" +
                "                                                             \n" +
                "   (kira/eval \"<% (doseq [x xs] %>foo<%= x %> <% ) %>\"     \n" +
                "              {:xs [1 2 3]})                                 \n" +
                ")";

        assertEquals("foo1 foo2 foo3 ", venice.eval(script));
    }

    @Test
    public void test_eval_5_delim_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                          \n" +
                "   (load-module :kira)                                       \n" +
                "                                                             \n" +
                "   (kira/eval \"$ (doseq [x xs] $foo$= x $ $ ) $\"           \n" +
                "      [\"$\" \"$\"]                                          \n" +
                "      {:xs [1 2 3]})                                         \n" +
                ")";

        assertEquals("foo1 foo2 foo3 ", venice.eval(script));
    }

    @Test
    public void test_eval_if_true() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                 \n" +
                "   (load-module :kira)                                              \n" +
                "                                                                    \n" +
                "   (kira/eval \"<% (if mode (do %>123<% ) %><% (do %>456<% )) %>\"  \n" +
                "              {:mode true})                                         \n" +
                ")";

        assertEquals("123", venice.eval(script));
    }

    @Test
    public void test_eval_if_false() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                 \n" +
                "   (load-module :kira)                                              \n" +
                "                                                                    \n" +
                "   (kira/eval \"<% (if mode (do %>123<% ) %><% (do %>456<% )) %>\"  \n" +
                "              {:mode false})                                        \n" +
                ")";

        assertEquals("456", venice.eval(script));
    }

    @Test
    public void test_eval_if_true_simple() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                  \n" +
                "   (load-module :kira)                               \n" +
                "                                                     \n" +
                "   (kira/eval \"<% (if mode %>123<% %>456<% ) %>\"   \n" +
                "              {:mode true})                          \n" +
                ")";

        assertEquals("123", venice.eval(script));
    }

    @Test
    public void test_eval_if_false_simple() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                  \n" +
                "   (load-module :kira)                               \n" +
                "                                                     \n" +
                "   (kira/eval \"<% (if mode %>123<% %>456<% ) %>\"   \n" +
                "              {:mode false})                         \n" +
                ")";

        assertEquals("456", venice.eval(script));
    }

    @Test
    public void test_eval_doc() {
        final Venice venice = new Venice();

        final String script =
                  "(do\n"
                + "  (load-module :kira)\n"
                + "  (with-out-str\n"
                + "    (println (kira/eval \"Hello <%= name %>\" { :name \"Alice\" }))\n"
                + "    (println (kira/eval \"1 + 2 = <%= (+ 1 2) %>\"))\n"
                + "    (println (kira/eval \"1 + 2 = <% (print (+ 1 2)) %>\"))\n"
                + "    (println (kira/eval \"margin: <%= (if large \\\"1.5em\\\" \\\"0.5em\\\") %>\"\n"
                + "                        { :large false }))\n"
                + "    (println (kira/eval \"fruits: <% (doseq [f fruits] %><%= f %> <% ) %>\"\n"
                + "                        { :fruits '(\"apple\" \"peach\") }))\n"
                + "    (println (kira/eval \"fruits: <% (doseq [f fruits] %><%= f %> <% ) %>\"\n"
                + "                        { :fruits '(\"apple\" \"peach\") }))\n"
                + "    (println (kira/eval \"when: <% (when large %>is large<% ) %>\"\n"
                + "                        { :large true }))\n"
                + "    (println (kira/eval \"if: <% (if large (do %>100<% ) (do %>1<% )) %>\"\n"
                + "                        { :large true }))))";

        assertEquals(
                  "Hello Alice\n"
                + "1 + 2 = 3\n"
                + "1 + 2 = 3\n"
                + "margin: 0.5em\n"
                + "fruits: apple peach \n"
                + "fruits: apple peach \n"
                + "when: is large\n"
                + "if: 100\n",
                venice.eval(script));
    }


    // ------------------------------------------------------------------------
    // Formatter
    // ------------------------------------------------------------------------

    @Test
    public void test_kira_formatter_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                    \n" +
                "   (load-module :kira)                                                 \n" +
                "                                                                       \n" +
                "   (def template \"\"\"<%= (time/format ts \"yyyy-MM-dd\") %>\"\"\")   \n" +
                "                                                                       \n" +
                "   (def data { :ts (time/local-date 2000 8 1) })                       \n" +
                "                                                                       \n" +
                "   (kira/eval template data)                                           \n" +
                ")";

        assertEquals("2000-08-01", venice.eval(script));
    }

    @Test
    public void test_kira_formatter_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                       \n" +
                "   (ns test)                                              \n" +
                "                                                          \n" +
                "   (load-module :kira)                                    \n" +
                "                                                          \n" +
                "   (defn format-ts [t] (time/format t \"yyyy-MM-dd\"))    \n" +
                "                                                          \n" +
                "   (def template \"<%= (test/format-ts ts) %>\")          \n" +
                "                                                          \n" +
                "   (def data { :ts (time/local-date 2000 8 1) })          \n" +
                "                                                          \n" +
                "   (kira/eval template data)                              \n" +
                ")";

        assertEquals("2000-08-01", venice.eval(script));
    }

    @Test
    public void test_kira_esc_xml_formatter_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                                             \n" +
                "   (load-module :kira)                                                                          \n" +
                "                                                                                                \n" +
                "   (def template \"\"\"<%= (kira/escape-xml ts #(time/format %1 \"yyyy-MM-dd\")) %>\"\"\")      \n" +
                "                                                                                                \n" +
                "   (def data { :ts (time/local-date 2000 8 1) })                                                \n" +
                "                                                                                                \n" +
                "   (kira/eval template data)                                                                    \n" +
                ")";

        assertEquals("2000-08-01", venice.eval(script));
    }

    @Test
    public void test_kira_esc_xml_formatter_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                              \n" +
                "   (ns test)                                                     \n" +
                "                                                                 \n" +
                "   (load-module :kira)                                           \n" +
                "                                                                 \n" +
                "   (defn format-ts [t] (time/format t \"yyyy-MM-dd\"))           \n" +
                "                                                                 \n" +
                "   (def template \"<%= (kira/escape-xml ts test/format-ts) %>\") \n" +
                "                                                                 \n" +
                "   (def data { :ts (time/local-date 2000 8 1) })                 \n" +
                "                                                                 \n" +
                "   (kira/eval template data)                                     \n" +
                ")";

        assertEquals("2000-08-01", venice.eval(script));
    }


    // ------------------------------------------------------------------------
    // Compiled
    // ------------------------------------------------------------------------

    @Test
    public void test_compile() {
        final Venice venice = new Venice();

        // (let [func (kira/fn [x] "foo<%= x %>")] (func "bar"))

        final String script =
                "(do                                           \n" +
                "   (load-module :kira)                        \n" +
                "                                              \n" +
                "   (let [template \"foo<%= x %>\"             \n" +
                "         tf (kira/fn [x] \"foo<%= x %>\")]    \n" +
                "      (tf \"bar\"))                           \n" +
                ")";

        assertEquals("foobar", venice.eval(script));
    }

    @Test
    public void test_compile_multivars() {
        final Venice venice = new Venice();

        // (let [func (kira/fn [x] "formula: <%= x %> + <%= y %> + <%= z %>")] (func "a" "2b" "c"))

        final String script =
                "(do                                                                        \n" +
                "   (load-module :kira)                                                     \n" +
                "                                                                           \n" +
                "   (let [template \"formula: <%= x %> + <%= y %> + <%= z %>\"              \n" +
                "         tf (kira/fn [x y z] template)]                                    \n" +
                "      (tf \"a\" \"2b\" \"c\"))                                             \n" +
                ")";

        assertEquals("formula: a + 2b + c", venice.eval(script));
    }

    @Test
    public void test_compile_multivars_2() {
        final Venice venice = new Venice();

        // (let [func (kira/fn [x] <% (kira/emit x) %> + <% (kira/emit y) %>)] (func "a" "2b"))

        final String script =
                "(do                                                                        \n" +
                "   (load-module :kira)                                                     \n" +
                "                                                                           \n" +
                "   (let [template \"formula: <%= x %> + <%= y %>\"   \n" +
                "         tf (kira/fn [x y] template)]                                      \n" +
                "      (tf \"a\" \"2b\"))                                                   \n" +
                ")";

        assertEquals("formula: a + 2b", venice.eval(script));
    }

    @Test
    public void test_compile_delim() {
        final Venice venice = new Venice();

        // (let [func (kira/fn [x] "foo$= x $" ["$" "$"])] (func "bar"))

        final String script =
                "(do                                                     \n" +
                "   (load-module :kira)                                  \n" +
                "                                                        \n" +
                "   (let [template \"foo$= x $\"                         \n" +
                "         tf (kira/fn [x] template [\"$\" \"$\"])]       \n" +
                "     (tf \"bar\"))                                      \n" +
                ")";

        assertEquals("foobar", venice.eval(script));
    }

}
