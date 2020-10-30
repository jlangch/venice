/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2020 Venice
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
		assertEquals("kira", new Venice().eval("(load-module :kira)"));
	}
	
	@Test
	public void test_emit() {
		final Venice venice = new Venice();

		final String script1 =
				"(do                                                  \n" +
				"   (load-module :kira)                               \n" +
				"                                                     \n" +
				"   (with-out-str                                     \n" +
				"      (kira/emit \"abc def\"))                       \n" + 
				")";

		assertEquals("abc def", venice.eval(script1));
	}
	
	@Test
	public void test_escape_xml() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                  \n" +
				"   (load-module :kira)                               \n" +
				"                                                     \n" +
				"   (with-out-str                                     \n" +
				"      (kira/escape-xml \"\"\"a < > & ' \" b\"\"\"))  \n" + 
				")";

		assertEquals("a &lt; &gt; &amp; &apos; &quot; b", venice.eval(script));
	}
	
	@Test
	public void test_escape_xml_transform() {
		final Venice venice = new Venice();

		final String script1 =
				"(do                                      \n" +
				"   (load-module :kira)                   \n" +
				"                                         \n" +
				"   (with-out-str                         \n" +
				"      (kira/escape-xml                   \n" +
				"          \"a < > b\"                    \n" + 
				"          str/upper-case))               \n" +
				")";

		assertEquals("A &lt; &gt; B", venice.eval(script1));

		final String script2 =
				"(do                                      \n" +
				"   (load-module :kira)                   \n" +
				"                                         \n" +
				"   (with-out-str                         \n" +
				"      (kira/escape-xml                   \n" +
				"          \"a < > b\"                    \n" + 
				"          #(str/upper-case %)))          \n" +
				")";

		assertEquals("A &lt; &gt; B", venice.eval(script2));
	}
	
	@Test
	public void test_escape_xml_multiline() {
		final Venice venice = new Venice();

		final String script1 =
				"(do                                            \n" +
				"   (load-module :kira)                         \n" +
				"                                               \n" +
				"   (with-out-str                               \n" +
				"      (kira/escape-xml-multiline               \n" +
				"          \"line <1>\nline <2>\nline <3>\"     \n" +
				"          #(str % \"<br/>\")))                 \n" + 
				")";

		assertEquals(
				"line &lt;1&gt;<br/>line &lt;2&gt;<br/>line &lt;3&gt;<br/>", 
				venice.eval(script1));
	}
	
	@Test
	public void test_escape_xml_docoll() {
		final Venice venice = new Venice();

		final String script1 =
				"(do                                               \n" +
				"   (load-module :kira)                            \n" +
				"                                                  \n" +
				"   (with-out-str                                  \n" +
				"      (kira/foreach [1 2 3]                       \n" +
				"                   (fn [x] (kira/emit (+ x 1))))) \n" +
				")";

		assertEquals("234", venice.eval(script1));

		final String script2 =
				"(do                                               \n" +
				"  (load-module :kira)                             \n" +
				"                                                  \n" +
				"  (with-out-str                                   \n" +
				"     (kira/foreach []                             \n" +
				"                   (fn [x] (kira/emit (+ x 1))))) \n" +
				")";

		assertEquals("", venice.eval(script2));
	}

	@Test
	public void test_escape_xml_nested_raw() {
		final Venice venice = new Venice();

		final String script1 =
				"(do                                                       \n" +
				"  (load-module :kira)                                     \n" +
				"                                                          \n" +
				"  (with-out-str                                           \n" +
				"    (kira/foreach [1 2 3]                                 \n" +
				"                  (fn [x]                                 \n" +
				"                    (kira/emit (kira/escape-xml x))))))     ";

		assertEquals("123", venice.eval(script1));
	}

	@Test
	public void test_escape_xml_nested() {
		final Venice venice = new Venice();

		final String script1 =
				"(do                                                   \n" +
				"   (load-module :kira)                                \n" +
				"                                                      \n" +
				"   (kira/eval                                         \n" + 
				"      \"${ (kira/foreach data (fn [x] (kira/emit }$line: ${ (kira/escape-xml x) }$\n${ ))) }$\"  \n" + 
				"      [\"${\" \"}$\"]                                 \n" + 
				"      {:data [1 2]})                                    \n" + 
				")";

		assertEquals("line: 1\nline: 2\n", venice.eval(script1));
	}
	
	
	
	// ------------------------------------------------------------------------
	// Evaluation
	// ------------------------------------------------------------------------
	
	@Test
	public void test_kira_1() {
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
	public void test_kira_2() {
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
	public void test_kira_2_delim_1() {
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
	public void test_kira_2_delim_2() {
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
	public void test_kira_3() {
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
	public void test_kira_3_delim_1() {
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
	public void test_kira_3_delim_2() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                       \n" +
				"   (load-module :kira)                                    \n" +
				"                                                          \n" +
				"   (kira/eval \"$= x $\" [\"$\" \"$\"] {:x \"foo\"})      \n" + 
				")";

		assertEquals("foo", venice.eval(script));
	}

	@Test
	public void test_kira_4() {
		final Venice venice = new Venice();

		final String script =
				"(do                                      \n" +
				"   (load-module :kira)                   \n" +
				"                                         \n" +
				"   (kira/eval \"<%=x%>\" {:x \"foo\"})   \n" + 
				")";

		assertEquals("foo", venice.eval(script));
	}

	@Test
	public void test_kira_4_delim_1() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                     \n" +
				"   (load-module :kira)                                  \n" +
				"                                                        \n" +
				"   (kira/eval \"<%=x%>\" [\"<%\" \"%>\"] {:x \"foo\"})  \n" + 
				")";

		assertEquals("foo", venice.eval(script));
	}

	@Test
	public void test_kira_4_delim_2() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                     \n" +
				"   (load-module :kira)                                  \n" +
				"                                                        \n" +
				"   (kira/eval \"$=x$\" [\"$\" \"$\"] {:x \"foo\"})      \n" + 
				")";

		assertEquals("foo", venice.eval(script));
	}

	@Test
	public void test_kira_5() {
		final Venice venice = new Venice();

		// (let [xs [1 2 3 4]] (docoll #(print (str "foo" % " ")) xs))
		
		// (kira/eval """<% (kira/foreach xs #(print (str %>foo<% % " ")))%>""" {:xs [1 2 3]})
		
		final String script =
				"(do                                                                       \n" +
				"   (load-module :kira)                                                    \n" +
				"                                                                          \n" +
				"   (kira/eval                                                             \n" + 
				"       \"\"\"<% (kira/foreach xs #(print (str %>foo<% % \" \")))%>\"\"\"  \n" + 
				"      {:xs [1 2 3]})                                                      \n" + 
				")";

		assertEquals("foo1 foo2 foo3 ", venice.eval(script));
	}

	@Test
	public void test_kira_5_delim_2() {
		final Venice venice = new Venice();

		// (let [xs [1 2 3 4]] (docoll #(print (str "foo" % " ")) xs))
		
		// (kira/eval """$ (kira/foreach xs #(print (str $foo$ % " ")))$""" ["$" "$"] {:xs [1 2 3]})
		
		final String script =
				"(do                                                                       \n" +
				"   (load-module :kira)                                                    \n" +
				"                                                                          \n" +
				"   (kira/eval                                                             \n" + 
				"       \"\"\"$ (kira/foreach xs #(print (str $foo$ % \" \")))$\"\"\"      \n" + 
				"      [\"$\" \"$\"]                                                       \n" + 
				"      {:xs [1 2 3]})                                                      \n" + 
				")";

		assertEquals("foo1 foo2 foo3 ", venice.eval(script));
	}
	
	
	// ------------------------------------------------------------------------
	// Formatter
	// ------------------------------------------------------------------------
	
	@Test
	public void test_kira_formatter_1() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                                           \n" +
				"	(load-module :kira)                                                        \n" +
				"	                                                                           \n" +
				"	(def template \"\"\"<% (print (time/format ts \"yyyy-MM-dd\")) %>\"\"\")   \n" +
				"	                                                                           \n" +
				"	(def data { :ts (time/local-date 2000 8 1) })                              \n" +
				"	                                                                           \n" +
				"   (kira/eval template data)                                                  \n" + 
				")";

		assertEquals("2000-08-01", venice.eval(script));
	}
	
	@Test
	public void test_kira_formatter_2() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                              \n" +
				"   (ns test)                                                     \n" +
				"	                                                              \n" +
				"	(load-module :kira)                                           \n" +
				"	                                                              \n" +
				"	(defn format-ts [t] (time/format t \"yyyy-MM-dd\"))           \n" +
				"	                                                              \n" +
				"	(def template \"<% (print (test/format-ts ts)) %>\")          \n" +
				"	                                                              \n" +
				"	(def data { :ts (time/local-date 2000 8 1) })                 \n" +
				"	                                                              \n" +
				"   (kira/eval template data)                                     \n" + 
				")";

		assertEquals("2000-08-01", venice.eval(script));
	}
	
	@Test
	public void test_kira_esc_xml_formatter_1() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                                                             \n" +
				"	(load-module :kira)                                                                          \n" +
				"	                                                                                             \n" +
				"	(def template \"\"\"<% (kira/escape-xml ts #(time/format %1 \"yyyy-MM-dd\")) %>\"\"\")       \n" +
				"	                                                                                             \n" +
				"	(def data { :ts (time/local-date 2000 8 1) })                                                \n" +
				"	                                                                                             \n" +
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
				"	                                                              \n" +
				"	(load-module :kira)                                           \n" +
				"	                                                              \n" +
				"	(defn format-ts [t] (time/format t \"yyyy-MM-dd\"))           \n" +
				"	                                                              \n" +
				"	(def template \"<% (kira/escape-xml ts test/format-ts) %>\")  \n" +
				"	                                                              \n" +
				"	(def data { :ts (time/local-date 2000 8 1) })                 \n" +
				"	                                                              \n" +
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
				"   (let [template \"formula: <% (kira/emit x) %> + <% (kira/emit y) %>\"   \n" +
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
