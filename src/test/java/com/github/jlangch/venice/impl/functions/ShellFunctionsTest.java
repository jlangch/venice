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
package com.github.jlangch.venice.impl.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.ShellException;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.util.junit.EnableOnMacOrLinux;


public class ShellFunctionsTest {

	@Test
	@EnableOnMacOrLinux
	public void test_shell() {
		final Venice venice = new Venice();
		
		final Map<?,?> result = (Map<?,?>)venice.eval("(sh \"ls\" \"-l\")");
		
		assertEquals(0L, result.get("exit"));
	}

	@Test
	@EnableOnMacOrLinux
	public void test_shell_with_dir_1() {
		final Venice venice = new Venice();
		
		final Map<?,?> result = (Map<?,?>)venice.eval("(with-sh-dir \"/tmp\" (sh \"ls\"))");
		
		assertEquals(0L, result.get("exit"));
	}

	@Test
	@EnableOnMacOrLinux
	public void test_shell_with_dir_2() {
		final Venice venice = new Venice();
		
		final Map<?,?> result = (Map<?,?>)venice.eval("(sh \"ls\" :dir \"/tmp\")");
		
		assertEquals(0L, result.get("exit"));
	}

	@Test
	@EnableOnMacOrLinux
	public void test_shell_error_exit_code() {
		final Venice venice = new Venice();
		
		final Map<?,?> result = (Map<?,?>)venice.eval("(sh \"rm\" \"xxxxxxxxxxxxxxxxxxxxxxxxx.any\")");
		assertEquals(1L, result.get("exit"));
	}

	@Test
	@EnableOnMacOrLinux
	public void test_shell_error_throw_exception_1() {
		final Venice venice = new Venice();
		
		assertThrows(
	            ShellException.class, 
	            () -> venice.eval("(with-sh-throw (sh \"rm\" \"xxxxxxxxxxxxxxxxxxxxxxxxx.any\"))"));
	}

	@Test
	@EnableOnMacOrLinux
	public void test_shell_error_throw_exception_2() {
		final Venice venice = new Venice();
		
		assertThrows(
	            ShellException.class, 
	            () -> venice.eval("(sh \"rm\" \"xxxxxxxxxxxxxxxxxxxxxxxxx.any\" :throw-ex true)"));
	}
}
