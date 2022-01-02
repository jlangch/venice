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
package com.github.jlangch.venice.impl.debug.breakpoint;

import static com.github.jlangch.venice.impl.debug.breakpoint.BreakpointParser.parseBreakpointScopes;
import static com.github.jlangch.venice.impl.debug.breakpoint.BreakpointParser.parseBreakpoints;
import static com.github.jlangch.venice.impl.debug.breakpoint.FunctionScope.FunctionCall;
import static com.github.jlangch.venice.impl.debug.breakpoint.FunctionScope.FunctionEntry;
import static com.github.jlangch.venice.impl.debug.breakpoint.FunctionScope.FunctionException;
import static com.github.jlangch.venice.impl.debug.breakpoint.FunctionScope.FunctionExit;
import static com.github.jlangch.venice.impl.util.CollectionUtil.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
		
		final Set<FunctionScope> scopes4 = parseBreakpointScopes(">");
		assertEquals(1, scopes4.size());
		assertTrue(scopes4.contains(FunctionCall));

		final Set<FunctionScope> scopes5 = parseBreakpointScopes(">(!)");
		assertEquals(4, scopes5.size());
		assertTrue(scopes5.contains(FunctionEntry));
		assertTrue(scopes5.contains(FunctionException));
		assertTrue(scopes5.contains(FunctionExit));
		assertTrue(scopes5.contains(FunctionCall));
	}
	
	@Test
	public void test_parseBreakpointScopes_Format() {
		assertEquals(
				"", 
				parseBreakpoints("foo").get(0).getSelectors().get(0).getFormattedScopes());
		
		assertEquals(
				"", 
				parseBreakpoints("( foo").get(0).getSelectors().get(0).getFormattedScopes());
		
		assertEquals(
				")", 
				parseBreakpoints(") foo").get(0).getSelectors().get(0).getFormattedScopes());
		
		assertEquals(
				"!", 
				parseBreakpoints("! foo").get(0).getSelectors().get(0).getFormattedScopes());
		
		assertEquals(
				">", 
				parseBreakpoints("> foo").get(0).getSelectors().get(0).getFormattedScopes());
		
		assertEquals(
				"()", 
				parseBreakpoints("() foo").get(0).getSelectors().get(0).getFormattedScopes());
		
		assertEquals(
				">(!)", 
				parseBreakpoints(">(!) foo").get(0).getSelectors().get(0).getFormattedScopes());
		
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
		assertTrue(parseBreakpoints("").isEmpty());
		assertTrue(parseBreakpoints("0").isEmpty());
		assertTrue(parseBreakpoints("0.0").isEmpty());
		assertTrue(parseBreakpoints("0M").isEmpty());
		assertTrue(parseBreakpoints("test.venice/").isEmpty());
		assertTrue(parseBreakpoints("/any").isEmpty());
 	}

	@Test
	public void test_parseBreakpoint_fn() {
		assertEquals(
				"/", 
				parseBreakpoints("/").get(0).getQualifiedFnName());

		assertEquals(
				"+", 
				parseBreakpoints("+").get(0).getQualifiedFnName());

		assertEquals(
				"user/sum", 
				parseBreakpoints("user/sum").get(0).getQualifiedFnName());

		assertEquals(
				"user/*", 
				parseBreakpoints("user/*").get(0).getQualifiedFnName());
	}

	@Test
	public void test_parseBreakpoint_fn_scopes() {
		assertEquals(
				"", 
				parseBreakpoints("user/*")
					.get(0)
					.getSelectors()
					.get(0)
					.getFormattedScopes());

		assertEquals(
				"", 
				parseBreakpoints("( user/*")
					.get(0)
					.getSelectors()
					.get(0)
					.getFormattedScopes());

		assertEquals(
				"()", 
				parseBreakpoints("() user/*")
					.get(0)
					.getSelectors()
					.get(0)
					.getFormattedScopes());

		assertEquals(
				"(!)", 
				parseBreakpoints("(!) user/*")
					.get(0)
					.getSelectors()
					.get(0)
					.getFormattedScopes());
	}	

	@Test
	public void test_parseBreakpoint_fn_ancestor_nearest() {
		assertEquals(
				"*", 
				parseBreakpoints("foo/test > *")
					.get(0)
					.getQualifiedFnName());

		assertEquals(
				"foo/test", 
				parseBreakpoints("foo/test > *")
					.get(0)
					.getSelectors()
					.get(0)
					.getAncestorSelector()
					.getAncestor()
					.getQualifiedName());

		assertEquals(
				AncestorType.Nearest, 
				parseBreakpoints("foo/test > *")
					.get(0)
					.getSelectors()
					.get(0)
					.getAncestorSelector()
					.getType());
	}	

	@Test
	public void test_parseBreakpoint_fn_ancestor_nearest_with_scopes() {
		assertEquals(
				"*", 
				parseBreakpoints("(!) foo/test > *")
					.get(0)
					.getQualifiedFnName());

		assertEquals(
				"foo/test", 
				parseBreakpoints("(!) foo/test > *")
					.get(0)
					.getSelectors()
					.get(0)
					.getAncestorSelector()
					.getAncestor()
					.getQualifiedName());

		assertEquals(
				AncestorType.Nearest, 
				parseBreakpoints("(!) foo/test > *")
					.get(0)
					.getSelectors()
					.get(0)
					.getAncestorSelector()
					.getType());

		assertEquals(
				"(!)", 
				parseBreakpoints("(!) foo/test > *")
					.get(0)
					.getSelectors()
					.get(0)
					.getFormattedScopes());
	}	

	@Test
	public void test_parseBreakpoint_fn_ancestor_any() {
		assertEquals(
				"*", 
				parseBreakpoints("foo/test + *")
					.get(0)
					.getQualifiedFnName());

		assertEquals(
				"foo/test", 
				parseBreakpoints("foo/test + *")
					.get(0)
					.getSelectors()
					.get(0)
					.getAncestorSelector()
					.getAncestor()
					.getQualifiedName());

		assertEquals(
				AncestorType.Any, 
				parseBreakpoints("foo/test + *")
					.get(0)
					.getSelectors()
					.get(0)
					.getAncestorSelector()
					.getType());
	}	

	@Test
	public void test_parseBreakpoint_fn_ancestor_any_with_scopes() {
		assertEquals(
				"*", 
				parseBreakpoints("(!) foo/test + *")
					.get(0)
					.getQualifiedFnName());

		assertEquals(
				"foo/test", 
				parseBreakpoints("(!) foo/test + *")
					.get(0)
					.getSelectors()
					.get(0)
					.getAncestorSelector()
					.getAncestor()
					.getQualifiedName());

		assertEquals(
				AncestorType.Any, 
				parseBreakpoints("(!) foo/test + *")
					.get(0)
					.getSelectors()
					.get(0)
					.getAncestorSelector()
					.getType());

		assertEquals(
				"(!)", 
				parseBreakpoints("(!) foo/test + *")
					.get(0)
					.getSelectors()
					.get(0)
					.getFormattedScopes());
	}	
}
