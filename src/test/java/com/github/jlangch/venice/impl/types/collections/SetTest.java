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
package com.github.jlangch.venice.impl.types.collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class SetTest {

	@Test
	public void test_SetAsFunction() {
		final Venice venice = new Venice();

		assertEquals(":a", venice.eval("(pr-str (#{:a :b :c} :a))"));
		assertEquals("nil", venice.eval("(pr-str (#{:a :b :c} :d))"));
		assertEquals("nil", venice.eval("(pr-str (#{:a :b :c} nil))"));
		assertEquals("nil", venice.eval("(pr-str (#{:a :b :c nil} nil))"));
		
		// defaults
		assertEquals(":a", venice.eval("(pr-str (#{:a :b :c} :a :e))"));
		assertEquals(":e", venice.eval("(pr-str (#{:a :b :c} :d :e))"));
		assertEquals(":e", venice.eval("(pr-str (#{:a :b :c} nil :e))"));
		assertEquals("nil", venice.eval("(pr-str (#{:a :b :c nil} nil :e))"));
	}

	@Test
	public void test_set_eval() {
		final Venice venice = new Venice();

		assertEquals("#{2 3}", venice.eval("(pr-str #{2 (+ 1 2) (+ 1 1)})"));
	}
}
