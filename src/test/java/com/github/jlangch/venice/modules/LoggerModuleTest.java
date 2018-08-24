/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2018 Venice
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
package com.github.jlangch.venice.modules;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.util.CapturingPrintStream;


public class LoggerModuleTest {

	@Test
	public void test_logger() {
		final Venice venice = new Venice();

		final String script =
				"(do                                          " +
				"   (load-module :logger)                   " +
				"                                             " +
				"   (logger/open)                           " + 
				"   (logger/log :INFO \"test 1\")           " + 
				"   (logger/log :INFO \"test 2\")           " + 
				"   (logger/to-string)                      " + 
				") ";

		final String logger = (String)venice.eval("(str " + script + ")");
		assertEquals(2, StringUtil.splitIntoLines(logger).size());
	}

	@Test
	public void test_logger_stdout() {
		final CapturingPrintStream ps = CapturingPrintStream.create();

		final Venice venice = new Venice();

		final String script =
				"(do                                          " +
				"   (load-module :logger)                   " +
				"                                             " +
				"   (logger/open)                           " + 
				"   (logger/attach-os *out*)                " + 
				"   (logger/log :INFO \"test 1\")           " + 
				"   (logger/log :INFO \"test 2\")           " + 
				"   (logger/to-string)                      " + 
				") ";

		final String logger = (String)venice.eval(
											"(str " + script + ")",
											Parameters.of("*out*", ps));
		
		assertEquals(2, StringUtil.splitIntoLines(logger).size());
		assertEquals(2, StringUtil.splitIntoLines(ps.getOutput()).size());
	}

}
