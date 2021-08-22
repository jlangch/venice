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
package com.github.jlangch.venice.impl.debug.breakpoint;

import static com.github.jlangch.venice.impl.debug.breakpoint.BreakpointParser.parseBreakpoint;
import static com.github.jlangch.venice.impl.debug.breakpoint.BreakpointParser.parseBreakpointScopes;
import static com.github.jlangch.venice.impl.debug.breakpoint.FunctionScope.FunctionEntry;
import static com.github.jlangch.venice.impl.debug.breakpoint.FunctionScope.FunctionException;
import static com.github.jlangch.venice.impl.debug.breakpoint.FunctionScope.FunctionExit;
import static com.github.jlangch.venice.impl.util.CollectionUtil.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;


public class BreakpointParserTest {

	@Test
	public void test_parseBreakpointScopes() {
		final Set<FunctionScope> scopes1 = parseBreakpointScopes("");
		assertTrue(scopes1.isEmpty());
		
		final Set<FunctionScope> scopes2 = parseBreakpointScopes("(");
		assertEquals(1, scopes2.size());
		assertTrue(scopes2.contains(FunctionEntry));

		final Set<FunctionScope> scopes3 = parseBreakpointScopes("(!)");
		assertEquals(3, scopes3.size());
		assertTrue(scopes3.contains(FunctionEntry));
		assertTrue(scopes3.contains(FunctionException));
		assertTrue(scopes3.contains(FunctionExit));
	}

	@Test
	public void test_parseBreakpointScopes_withDefault() {
		final Set<FunctionScope> scopes1 = parseBreakpointScopes("", toSet(FunctionEntry));
		assertEquals(1, scopes1.size());
		assertTrue(scopes1.contains(FunctionEntry));
		
		final Set<FunctionScope> scopes2 = parseBreakpointScopes("", toSet(FunctionEntry, FunctionExit));
		assertEquals(2, scopes2.size());
		assertTrue(scopes2.contains(FunctionEntry));
		assertTrue(scopes2.contains(FunctionExit));
		
		final Set<FunctionScope> scopes3 = parseBreakpointScopes("(", toSet(FunctionEntry));
		assertEquals(1, scopes3.size());
		assertTrue(scopes3.contains(FunctionEntry));
		
		final Set<FunctionScope> scopes4 = parseBreakpointScopes("!", toSet(FunctionEntry, FunctionExit));
		assertEquals(1, scopes4.size());
		assertTrue(scopes4.contains(FunctionException));

		final Set<FunctionScope> scopes5 = parseBreakpointScopes("(!)", toSet(FunctionEntry, FunctionExit));
		assertEquals(3, scopes5.size());
		assertTrue(scopes5.contains(FunctionEntry));
		assertTrue(scopes5.contains(FunctionException));
		assertTrue(scopes5.contains(FunctionExit));
	}

	@Test
	public void test_parseBreakpoint_invalid() {
		assertNull(parseBreakpoint("", null));
		assertNull(parseBreakpoint("0", null));
		assertNull(parseBreakpoint("0.0", null));
		assertNull(parseBreakpoint("0M", null));
		assertNull(parseBreakpoint("test.venice/", null));
		assertNull(parseBreakpoint("/any", null));
 	}

	@Test
	public void test_parseBreakpoint_fn() {
		assertEquals(
				"/", 
				((BreakpointFn)parseBreakpoint("/", null)).getQualifiedFnName());

		assertEquals(
				"+", 
				((BreakpointFn)parseBreakpoint("+", null)).getQualifiedFnName());

		assertEquals(
				"user/sum", 
				((BreakpointFn)parseBreakpoint("user/sum", null)).getQualifiedFnName());

		assertEquals(
				"user/*", 
				((BreakpointFn)parseBreakpoint("user/*", null)).getQualifiedFnName());
	}

	@Test
	public void test_parseBreakpoint_fn_scopes() {
		assertEquals(
				"", 
				((BreakpointFn)parseBreakpoint("user/*", null)).getFormattedScopes());

		assertEquals(
				"", 
				((BreakpointFn)parseBreakpoint("user/*", toSet(FunctionEntry))).getFormattedScopes());

		assertEquals(
				"()", 
				((BreakpointFn)parseBreakpoint("user/*", toSet(FunctionEntry,FunctionExit))).getFormattedScopes());

		assertEquals(
				"(!)", 
				((BreakpointFn)parseBreakpoint("user/*", toSet(FunctionEntry,FunctionExit,FunctionException))).getFormattedScopes());
	}
	
}
