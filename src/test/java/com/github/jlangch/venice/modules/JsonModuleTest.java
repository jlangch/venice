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


public class JsonModuleTest {

	@Test
	public void test_to_json() {
		final Venice venice = new Venice();

		final String script =
				"(json/to-json {:a 100 :b 100 :c [10 20 30]})";

		assertEquals(
				"{\"a\":100,\"b\":100,\"c\":[10,20,30]}", 
				venice.eval(script));
	}

	@Test
	public void test_to_pretty_json() {
		final Venice venice = new Venice();

		final String script =
				"(json/to-pretty-json [{:a 100 :b 100}, {:a 200 :b 200}])";

		assertEquals(
			"[{\n" + 
			"  \"a\":100,\n" + 
			"  \"b\":100\n" + 
			"},{\n" + 
			"  \"a\":200,\n" + 
			"  \"b\":200\n" + 
			"}]", 
			venice.eval(script));
	}

	@Test
	public void test_pretty_print() {
		final Venice venice = new Venice();

		final String script =
				"(json/pretty-print (json/to-json {:a 100 :b 100}))";

		assertEquals(
			"{\n" + 
			"  \"a\":100,\n" + 
			"  \"b\":100\n" + 
			"}", 
			venice.eval(script));
	}
	
	@Test
	public void test_json_parse_1() {
		final Venice venice = new Venice();

		final String script =
				"(json/parse \"\"\"{\"a\": 100, \"b\": 100, \"c\": [10,20,30]}\"\"\")";

		assertEquals(
			"{a 100 b 100 c (10 20 30)}", 
			venice.eval("(str " + script + ")"));
	}
	
	@Test
	public void test_json_parse_2() {
		final Venice venice = new Venice();

		final String script =
				"(json/parse \"\"\"[{\"a\": 100,\"b\": 100}, {\"a\": 200, \"b\": 200}]\"\"\") ";

		assertEquals(
			"({a 100 b 100} {a 200 b 200})", 
			venice.eval("(str " + script + ")"));
	}
}
