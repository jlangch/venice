/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2019 Venice
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
		
		// (kira/eval """<% (docoll #(print (str %>foo<% % " ")) xs)%>""" {:xs [1 2 3]})
		
		final String script =
				"(do                                                                  \n" +
				"   (load-module :kira)                                               \n" +
				"                                                                     \n" +
				"   (kira/eval                                                        \n" + 
				"       \"\"\"<% (docoll #(print (str %>foo<% % \" \")) xs)%>\"\"\"   \n" + 
				"      {:xs [1 2 3]})                                                 \n" + 
				")";

		assertEquals("foo1 foo2 foo3 ", venice.eval(script));
	}

	@Test
	public void test_kira_5_delim_2() {
		final Venice venice = new Venice();

		// (let [xs [1 2 3 4]] (docoll #(print (str "foo" % " ")) xs))
		
		// (kira/eval """$ (docoll #(print (str $foo$ % " ")) xs)$""" ["$" "$"] {:xs [1 2 3]})
		
		final String script =
				"(do                                                                  \n" +
				"   (load-module :kira)                                               \n" +
				"                                                                     \n" +
				"   (kira/eval                                                        \n" + 
				"       \"\"\"$ (docoll #(print (str $foo$ % \" \")) xs)$\"\"\"       \n" + 
				"      [\"$\" \"$\"]                                                  \n" + 
				"      {:xs [1 2 3]})                                                 \n" + 
				")";

		assertEquals("foo1 foo2 foo3 ", venice.eval(script));
	}

	@Test
	public void test_kira_6() {
		final Venice venice = new Venice();

		// (let [func (kira/fn [x] "foo<%= x %>")] (func "bar"))
		
		final String script =
				"(do                                                                  \n" +
				"   (load-module :kira)                                               \n" +
				"                                                                     \n" +
				"   (let [tf (kira/fn [x] \"foo<%= x %>\")] (tf \"bar\"))               " +
				")";

		assertEquals("foobar", venice.eval(script));
	}

	@Test
	public void test_kira_6_delim_2() {
		final Venice venice = new Venice();

		// (let [func (kira/fn [x] "foo$= x $" ["$" "$"])] (func "bar"))
		
		final String script =
				"(do                                                                  \n" +
				"   (load-module :kira)                                               \n" +
				"                                                                     \n" +
				"   (let [tf (kira/fn [x] \"foo$= x $\" [\"$\" \"$\"])] (tf \"bar\"))  " +
				")";

		assertEquals("foobar", venice.eval(script));
	}

}
