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
package com.github.jlangch.venice.javainterop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class UserTest {

	@Test
	public void test_constructor() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                                   " +
				"   (def john (. :com.github.jlangch.venice.support.User :new          " +
				"                \"john\" 24 (. :java.time.LocalDate :of 2018 7 21)))  " +
				"   (str john)                                                         " + 
				")";
		
		assertEquals("john, 24, 2018-07-21", venice.eval(script));
	}

	@Test
	public void test_constructor_imports() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                                      " +
				"   (import :com.github.jlangch.venice.support.User :java.time.LocalDate) " +
				"   (def john (. :User :new \"john\" 24 (. :LocalDate :of 2018 7 21)))    " +
				"   (str john)                                                            " + 
				")";
		
		assertEquals("john, 24, 2018-07-21", venice.eval(script));
	}

	@Test
	public void test_convert_to_map() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                                      " +
				"   (import :com.github.jlangch.venice.support.User :java.time.LocalDate) " +
				"   (def john (. :User :new \"john\" 24 (. :LocalDate :of 2018 7 21)))    " +
				"   (hash-map john)                                                       " + 
				")";
		
		@SuppressWarnings("unchecked")
		final Map<Object,Object> map = (Map<Object,Object>)venice.eval(script);
		
		assertEquals("john", map.get("firstname"));
		assertEquals(Integer.valueOf(24), map.get("age"));
		assertEquals(LocalDate.of(2018, 07, 21), map.get("birthday"));
		assertEquals("com.github.jlangch.venice.support.User", map.get("class"));
	}

	@Test
	public void test_collection_filter() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                                       " +
				"   (import :com.github.jlangch.venice.support.User :java.time.LocalDate)  " +
				"   (def users [                                                           " +
				"        (. :User :new \"john\" 24 (. :LocalDate :of 2018 7 21))           " +
				"        (. :User :new \"pete\" 48 (. :LocalDate :of 1970 1 12)) ])        " +
				"   (str (filter (fn [u] (> (get u :age) 30)) users))                      " + 
				")";
		
		assertEquals("[pete, 48, 1970-01-12]", venice.eval(script));
	}

}
