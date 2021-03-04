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
package com.github.jlangch.venice.javainterop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class JavaInterop_filter_Test {

	@Test
	public void test_filter_java_objects() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                                      " +
				"   (import :com.github.jlangch.venice.support.User )                     " +
				"   (import :java.time.LocalDate)                                         " +
				"                                                                         " + 
				"   (def users [                                                          " +
				"        (. :User :new \"john\" 24 (. :LocalDate :of 2018 7 21))          " +
				"        (. :User :new \"pete\" 48 (. :LocalDate :of 1970 1 12)) ])       " +
				"   (str (filter (fn [u] (> (get u :age) 30)) users))                     " + 
				")";
		
		assertEquals("(pete, 48, 1970-01-12)", venice.eval(script));
	}
}