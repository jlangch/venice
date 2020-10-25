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
package com.github.jlangch.venice;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.VeniceInterpreter;


public class Recursion_TailPos_Check_Test {

	@Test
	public void test_recursion_multi_arity() {
		if (!VeniceInterpreter.supportsAutoTCO()) {
			return;
		}
		
		final Venice venice = new Venice();

		// do
		assertNull(venice.eval("(do (tail-pos))"));
		assertNull(venice.eval("(do 1 (tail-pos))"));
		assertThrows(NotInTailPositionException.class, () -> venice.eval("(do (tail-pos) 1)"));
		assertThrows(NotInTailPositionException.class, () -> venice.eval("(do 1 (tail-pos) 1)"));

		// if
		assertNull(venice.eval("(if true (tail-pos) 2)"));
		assertNull(venice.eval("(if false 2 (tail-pos))"));		
		assertThrows(NotInTailPositionException.class, () -> venice.eval("(if true (+ 1 (tail-pos)) 2)"));
	}

}
