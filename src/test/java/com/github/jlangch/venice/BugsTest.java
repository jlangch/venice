/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2019 Venice
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
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.Env;
import com.github.jlangch.venice.impl.VeniceInterpreter;
import com.github.jlangch.venice.impl.functions.CoreFunctions;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;


public class BugsTest {

	@Test
	public void test_0_2_0_meta_for_vectors() {	
		// Create a VeniceInterpreter without 'core.venice' for simpler testing
		final VeniceInterpreter venice = new VeniceInterpreter();
		final Env env = new Env(null);
		
		// Add the 'meta' function
		env.set(new VncSymbol(CoreFunctions.meta.getName()), CoreFunctions.meta);
		
		// Test...
		final VncVal result = venice.RE("(meta [1 2 3])", null, env, null);		
		assertNotEquals(Constants.Nil, result);
	}

	@Test
	public void test_0_2_0_str_unicode() {
		// Create a VeniceInterpreter without 'core.venice' for simpler testing
		final VeniceInterpreter venice = new VeniceInterpreter();
		final Env env = new Env(null);
		
		// Add the 'meta' function
		env.set(new VncSymbol(CoreFunctions.str.getName()), CoreFunctions.str);
		
		// Test...
		final VncVal result = venice.RE("(str \"\\u0041\\u0042\\u0043\")", null, env, null);		
		assertEquals("ABC", ((VncString)result).getValue());
	}

}
