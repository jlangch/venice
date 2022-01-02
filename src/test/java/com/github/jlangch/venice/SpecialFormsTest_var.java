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
package com.github.jlangch.venice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;


public class SpecialFormsTest_var {
	
	@Test
	public void test_var_ns() {
		assertEquals("core", new Venice().eval("(var-ns +)"));					
		assertEquals("core", new Venice().eval("(var-ns core/+)"));					
		assertEquals("str", new Venice().eval("(var-ns str/split)"));					
		assertEquals(null, new Venice().eval("(let [x 100] (var-ns x))"));					
	}
	
	@Test
	public void test_var_name() {
		assertEquals("+", new Venice().eval("(var-name +)"));					
		assertEquals("+", new Venice().eval("(var-name core/+)"));					
		assertEquals("split", new Venice().eval("(var-name str/split)"));					
		assertEquals("x", new Venice().eval("(let [x 100] (var-name x))"));					
	}
}
