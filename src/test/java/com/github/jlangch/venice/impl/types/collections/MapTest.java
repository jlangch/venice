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


public class MapTest {

	@Test
	public void test_MapAsFunction() {
		final Venice venice = new Venice();

		assertEquals("2", venice.eval("(pr-str ({:a 2 :b 3} :a))"));
		assertEquals("nil", venice.eval("(pr-str ({:a 2 :b 3} :c))"));
		assertEquals("nil", venice.eval("(pr-str ({:a 2 :b 3} nil))"));
		
		// defaults
		assertEquals("2", venice.eval("(pr-str ({:a 2 :b 3} :a 9))"));
		assertEquals("9", venice.eval("(pr-str ({:a 2 :b 3} :c 9))"));
		assertEquals("9", venice.eval("(pr-str ({:a 2 :b 3} nil 9))"));
	}

	@Test
	public void test_map_eval() {
		final Venice venice = new Venice();

		assertEquals("{:a 2 :b 3}", venice.eval("(pr-str {:a 2 :b (+ 1 2)})"));
	}
}
