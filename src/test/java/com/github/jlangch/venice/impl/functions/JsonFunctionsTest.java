/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2021 Venice
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
package com.github.jlangch.venice.impl.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;


public class JsonFunctionsTest {

	@Test
	public void test_write_str_basic() {
		final Venice venice = new Venice();

		assertEquals(null, venice.eval("(json/write-str nil)"));

		assertEquals("true", venice.eval("(json/write-str true)"));
		assertEquals("false", venice.eval("(json/write-str false)"));
	
		assertEquals("1", venice.eval("(json/write-str (atom 1))"));

		assertEquals("1", venice.eval("(json/write-str 1)"));
		assertEquals("1.0", venice.eval("(json/write-str 1.0)"));
		assertEquals("\"1.33\"", venice.eval("(json/write-str 1.33M)"));
		assertEquals("\"a\"", venice.eval("(json/write-str (keyword \"a\"))"));
		assertEquals("\"a\"", venice.eval("(json/write-str (symbol \"a\"))"));
	}

	@Test
	public void test_write_str_collections() {
		final Venice venice = new Venice();

		assertEquals("[1,2]", venice.eval("(json/write-str '(1 2)))"));
		assertEquals("[1,2]", venice.eval("(json/write-str [1 2]))"));
		
		assertEquals("[1,2]", venice.eval("(json/write-str #{1 2}))"));		
		assertEquals("[1,2]", venice.eval("(json/write-str (sorted-set 1 2)))"));
		assertEquals("{\"a\":1}", venice.eval("(json/write-str {:a 1}))"));
		
		assertEquals("{\"a\":1}", venice.eval("(json/write-str (hash-map :a 1)))"));
		assertEquals("{\"a\":1}", venice.eval("(json/write-str (sorted-map :a 1)))"));
		assertEquals("{\"a\":1}", venice.eval("(json/write-str (ordered-map :a 1)))"));
		assertEquals("{\"a\":1}", venice.eval("(json/write-str (mutable-map :a 1)))"));
		
		assertEquals(
				"[1,2]", 
				venice.eval(
						"(json/write-str                        \n" +
						"  (doto (. :java.util.ArrayList :new)  \n" +
						"        (. :add 1)                     \n" +
						"        (. :add 2)))                     "));
		
		assertEquals(
				"[1,2]", 
				venice.eval(
						"(json/write-str                        \n" +
						"  (doto (. :java.util.HashSet :new)    \n" +
						"        (. :add 1)                     \n" +
						"        (. :add 2)))                     "));
		
		assertEquals(
				"{\"a\":1}", 
				venice.eval(
						"(json/write-str                        \n" +
						"  (doto (. :java.util.HashMap :new)    \n" +
						"        (. :put \"a\" 1)))             "));
	}

	@Test
	public void test_write_str_binary() {
		final Venice venice = new Venice();

		assertEquals(
				"{\"a\":\"YWJjZGVmZ2g=\"}", 
				venice.eval("(json/write-str {:a (bytebuf-from-string \"abcdefgh\" :utf-8)})"));
	}

	@Test
	public void test_write_str_time() {
		final Venice venice = new Venice();

		assertEquals(
				"{\"a\":\"2018-08-01\"}", 
				venice.eval("(json/write-str {:a (time/local-date 2018 8 1)})"));

		assertEquals(
				"{\"a\":\"2018-08-01T14:20:10.2\"}", 
				venice.eval("(json/write-str {:a (time/local-date-time \"2018-08-01T14:20:10.200\")})"));

		assertEquals(
				"{\"a\":\"2018-08-01T14:20:10.2+01:00\"}", 
				venice.eval("(json/write-str {:a (time/zoned-date-time \"2018-08-01T14:20:10.200+01:00\")})"));
	}

	@Test
	public void test_write_nested() {
		final Venice venice = new Venice();

		assertEquals(
				"{\"a\":100,\"b\":100,\"c\":[10,20,30]}", 
				venice.eval("(json/write-str {:a 100 :b 100 :c [10 20 30]})"));

		assertEquals(
				"{\"a\":100,\"b\":null,\"c\":[10,20,null]}", 
				venice.eval("(json/write-str {:a 100 :b nil :c [10 20 nil]})"));
		
		assertEquals(
				"{\"a\":100,\"b\":100,\"c\":[10,20,{\"d\":100,\"e\":200}]}", 
				venice.eval("(json/write-str {:a 100 :b 100 :c [10 20 {:d 100 :e 200}]})"));
	}

	@Test
	public void test_spit() {
		final Venice venice = new Venice();

		final String script =
				"(let [out (. :java.io.ByteArrayOutputStream :new)]           \n" +
				"  (json/spit out {:a 100 :b 100 :c [10 20 30]})              \n" +
				"  (. out :flush)                                             \n" +
				"  (. :java.lang.String :new (. out :toByteArray) \"utf-8\"))   ";

		assertEquals(
				"{\"a\":100,\"b\":100,\"c\":[10,20,30]}", 
				venice.eval(script));
	}

	@Test
	public void test_to_pretty_json() {
		final Venice venice = new Venice();

		final String script =
				"(json/write-str [{:a 100 :b 100}, {:a 200 :b 200}] :pretty true)";

		assertEquals(
			"[{\n" + 
			"  \"a\": 100,\n" + 
			"  \"b\": 100\n" + 
			"},{\n" + 
			"  \"a\": 200,\n" + 
			"  \"b\": 200\n" + 
			"}]", 
			venice.eval(script));
	}

	@Test
	public void test_pretty_print() {
		final Venice venice = new Venice();

		final String script =
				"(json/pretty-print (json/write-str {:a 100 :b 100}))";

		assertEquals(
			"{\n" + 
			"  \"a\": 100,\n" + 
			"  \"b\": 100\n" + 
			"}", 
			venice.eval(script));
	}
	
	@Test
	public void test_json_read_str_decimal() {
		final Venice venice = new Venice();
		
		// map object keys to keywords
		assertEquals(
				"{:a 100 :b 100.33M}", 
				venice.eval("(str (json/read-str \"\"\"{\"a\": 100, \"b\": 100.33}\"\"\" :key-fn keyword :decimal true))"));
	}
	
	@Test
	public void test_json_read_str_key_fn() {
		final Venice venice = new Venice();
		
		// map object keys to keywords
		assertEquals(
				"{:a 100 :b 100}", 
				venice.eval("(str (json/read-str \"\"\"{\"a\": 100, \"b\": 100}\"\"\" :key-fn keyword))"));
	}
	
	@Test
	public void test_json_read_str_value_fn() {
		final Venice venice = new Venice();
		
		// map value identity
		assertEquals(
				"{:a 100 :b 100}", 
				venice.eval("(str (json/read-str \"\"\"{\"a\": 100, \"b\": 100}\"\"\" :key-fn keyword :value-fn (fn [k v] v)))"));
		
		// map value
		assertEquals(
				"{:a 101 :b 101}", 
				venice.eval("(str (json/read-str \"\"\"{\"a\": 100, \"b\": 100}\"\"\" :key-fn keyword :value-fn (fn [k v] (inc v))))"));
	}
	
	@Test
	public void test_json_read_str() {
		final Venice venice = new Venice();

		assertEquals(
			"{a 100 b 100}", 
			venice.eval("(str (json/read-str \"\"\"{\"a\": 100, \"b\": 100}\"\"\"))"));

		assertEquals(
			"{a 100 b 100 c (10 20 30)}", 
			venice.eval("(str (json/read-str \"\"\"{\"a\": 100, \"b\": 100, \"c\": [10,20,30]}\"\"\"))"));

		assertEquals(
			"({a 100 b 100} {a 200 b 200})", 
			venice.eval("(str (json/read-str \"\"\"[{\"a\": 100,\"b\": 100}, {\"a\": 200, \"b\": 200}]\"\"\"))"));

		assertEquals(
			"({a 100 b 100} {a 200 b 200})", 
			venice.eval("(str (json/read-str \"\"\"[ { \"a\" : 100, \n\"b\" : 100 \n}, { \"a\" : 200, \n\"b\" : 200 } ]\"\"\"))"));
	}
	
	@Test
	public void test_json_read_str_empty_array() {
		final Venice venice = new Venice();

		assertEquals(
			"{a ()}", 
			venice.eval("(str (json/read-str \"\"\"{\"a\" : [ ]}\"\"\"))"));

		assertEquals(
			"{a () b 12}", 
			venice.eval("(str (json/read-str \"\"\"{\"a\" : [ ], \"b\" : 12}\"\"\"))"));
	}

	@Test
	public void test_slurp() {
		final Venice venice = new Venice();

		final String script =
				"(let [json (json/write-str {:a 100 :b 100})             \n" +
				"      data (bytebuf-from-string json :utf-8)            \n" +
				"      in (. :java.io.ByteArrayInputStream :new data)]   \n" +
				"  (str (json/slurp in)))                                  ";

		assertEquals("{a 100 b 100}", venice.eval(script));
	}
	
	@Test
	public void test_json_read_write_file() {
		final Venice venice = new Venice();

		try {
			final File file = File.createTempFile("from__", ".json");
			file.deleteOnExit();
			final String fileName = file.getAbsolutePath();
			
			// write
			venice.eval(
					"(json/spit (io/file file-name) {:a 100 :b 100})", 
					Parameters.of("file-name", fileName));

			// read
			assertEquals(
					"{a 100 b 100}", 
					venice.eval("(str (json/slurp (io/file file-name)))", 
							Parameters.of("file-name", fileName)));
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
}
