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
package com.github.jlangch.venice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;


public class SpecialFormsTest_let {
	
	@Test
	public void test_let() {
		final Venice venice = new Venice();

		assertEquals(10L, venice.eval("(let [x 10] x)"));
		assertEquals(21L, venice.eval("(let [x 10 y 11] (+ x y))"));
		assertEquals(60L, venice.eval("(let [x 10 y (* x 5)] (+ x y))"));

		assertEquals(21L, venice.eval("(let [[x y] [10 11]] (+ x y))"));
		assertEquals(71L, venice.eval("(let [[x y] [10 11] z (* x 5)] (+ x y z))"));
	}
}
