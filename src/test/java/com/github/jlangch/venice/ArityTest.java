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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;


public class ArityTest {

	@Test
	public void test_special_form_arity_ex() {
		final Venice venice = new Venice();

		try {
			venice.eval("(locking)");
			
			fail("Expected ArityException");
		}
		catch(ArityException ex) {
			final String msg = ex.getMessage();
			assertTrue(msg.startsWith("Wrong number of args (0) passed to special form locking."));
		}
	}

	@Test
	public void test_macro_form_arity_ex() {
		final Venice venice = new Venice();

		try {
			venice.eval("(time)");
			
			fail("Expected ArityException");
		}
		catch(ArityException ex) {
			final String msg = ex.getMessage();
			assertTrue(msg.startsWith("Wrong number of args (0) passed to macro time."));
		}
	}

	@Test
	public void test_macro_form_arity_ex_2() {
		final Venice venice = new Venice();

		try {
			venice.eval("(while)");
			
			fail("Expected ArityException");
		}
		catch(ArityException ex) {
			final String msg = ex.getMessage();
			assertTrue(msg.startsWith("Wrong number of args (0) passed to the variadic macro while that requires at least 1 arg."));
		}
	}

	@Test
	public void test_function_form_arity_ex() {
		final Venice venice = new Venice();

		try {
			venice.eval("(count)");
			
			fail("Expected ArityException");
		}
		catch(ArityException ex) {
			final String msg = ex.getMessage();
			assertTrue(msg.startsWith("Wrong number of args (0) passed to function count."));
		}
	}
}
