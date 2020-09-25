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
package com.github.jlangch.venice.impl.functions;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;


public class ModuleFunctionsTest {
	
	@Test
	public void test_access_private_load_module_STAR() {
		final Venice venice = new Venice();

		final String s = 
				"(do                         \n" +
				"   (ns alpha)               \n" +
				"   (load-module* :semver)   \n" +
				")                             ";
	
		assertThrows(VncException.class, () -> venice.eval(s));
	}
	
	@Test
	public void test_access_private_load_file_STAR() {
		final Venice venice = new Venice();

		final String s = 
				"(do                         \n" +
				"   (ns alpha)               \n" +
				"   (load-file* :a.txt '())  \n" +
				")                             ";
	
		assertThrows(VncException.class, () -> venice.eval(s));
	}
	
	@Test
	public void test_access_private_load_resource_STAR() {
		final Venice venice = new Venice();

		final String s = 
				"(do                             \n" +
				"   (ns alpha)                   \n" +
				"   (load-resource* :a.txt '())  \n" +
				")                                 ";
	
		assertThrows(VncException.class, () -> venice.eval(s));
	}
	
	@Test
	public void test_access_private_load_classpath_file_STAR() {
		final Venice venice = new Venice();

		final String s = 
				"(do                               \n" +
				"   (ns alpha)                     \n" +
				"   (load-classpath-file* :a.txt)  \n" +
				")                                   ";
	
		assertThrows(VncException.class, () -> venice.eval(s));
	}

}
