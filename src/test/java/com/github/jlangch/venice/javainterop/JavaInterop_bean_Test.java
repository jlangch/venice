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


public class JavaInterop_bean_Test {


	@Test
	public void test_convert_bean_to_map() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                                      " +
				"   (import :com.github.jlangch.venice.support.User )                     " +
				"   (import :java.time.LocalDate)                                         " +
				"                                                                         " + 
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
	public void test_bean_map_accessor() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                                      " +
				"   (import :com.github.jlangch.venice.support.User )                     " +
				"   (import :java.time.LocalDate)                                         " +
				"                                                                         " + 
				"   (def john (. :User :new \"john\" 24 (. :LocalDate :of 2018 7 21)))    " +
				"   (:age john)                                                           " + 
				")";
		
		assertEquals(24, venice.eval(script));
	}

}