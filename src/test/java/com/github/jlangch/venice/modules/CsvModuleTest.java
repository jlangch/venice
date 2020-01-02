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

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class CsvModuleTest {

	@Test
	@SuppressWarnings("unchecked")
	public void test_1() {
		final Venice venice = new Venice();

		final String script =
				"(do                                  \n" +
				"   (load-module :csv)                \n" +
				"                                     \n" +
				"   (csv/read \"1,2,3\"))             ";

		final List<Object> parsed = (List<Object>)venice.eval(script);
		final List<Object> values = (List<Object>)((List<Object>)parsed).get(0);
		
		assertEquals("1", (String)values.get(0));
		assertEquals("2", (String)values.get(1));
		assertEquals("3", (String)values.get(2));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void test_2() {
		final Venice venice = new Venice();

		final String script =
				"(do                                  \n" +
				"   (load-module :csv)                \n" +
				"                                     \n" +
				"   (csv/read \"1,,3\"))             ";

		final List<Object> parsed = (List<Object>)venice.eval(script);
		final List<Object> values = (List<Object>)((List<Object>)parsed).get(0);
		
		assertEquals("1", (String)values.get(0));
		assertEquals(null, (String)values.get(1));
		assertEquals("3", (String)values.get(2));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void test_3() {
		final Venice venice = new Venice();

		final String script =
				"(do                                  \n" +
				"   (load-module :csv)                \n" +
				"                                     \n" +
				"   (csv/read \",,,\"))                 ";

		final List<Object> parsed = (List<Object>)venice.eval(script);
		final List<Object> values = (List<Object>)((List<Object>)parsed).get(0);
		
		assertEquals(null, (String)values.get(0));
		assertEquals(null, (String)values.get(1));
		assertEquals(null, (String)values.get(2));
	}
}
