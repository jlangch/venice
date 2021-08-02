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
package com.github.jlangch.venice.impl.debug;

import static com.github.jlangch.venice.impl.util.StringUtil.trimToNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.util.StringUtil;


public class BreakpointParser {

	public static IBreakpoint parseBreakpoint(
			final String ref,
			final String scopes
	) {	
		return BreakpointParser
					.parseBreakpointFn(ref)
					.withScopes(parseBreakpointScopes(scopes));
	}

	public static IBreakpoint parseBreakpoint(final String ref) {
		final IBreakpoint bp = parseBreakpointFn(ref);
		return bp != null ? bp : parseBreakpointLine(ref);
	}

	public static Set<BreakpointScope> parseBreakpointScopes(
			final String scopes
	) {
		return parseBreakpointScopes(scopes, new HashSet<>());
	}
	
	public static BreakpointFn parseBreakpointFn(
			final String ref
	) {
		// format:  {namespace}/{name}  
		//          e.g.: user/sum
		
		if (StringUtil.isBlank(ref)) {
			return null;
		}
		
		return new BreakpointFn(ref);
	}

	public static BreakpointLine parseBreakpointLine(final String ref) {
		// format:  {file}/{lineNr}  
		//          e.g.: statistics.venice/300
		
		if (StringUtil.isBlank(ref)) {
			return null;
		}
		
		final int pos = ref.lastIndexOf('/');
		if (pos < 1) {
			return null;
		}
		
		final String file = ref.substring(0, pos);
		final int lineNr = parseInt(ref.substring(pos));
		return (lineNr < 1) ? null : new BreakpointLine(file, lineNr);
	}

	public static Set<BreakpointScope> parseBreakpointScopes(
			final String scopes,
			final Set<BreakpointScope> defaultScopes
	) {
		if (trimToNull(scopes) == null) {
			return defaultScopes;
		}
		else {
			final Set<BreakpointScope> tset = 
					Arrays.asList(BreakpointScope.values())
						  .stream()
						  .filter(t -> scopes.contains(t.symbol()))
						  .collect(Collectors.toSet());
			
			return tset.isEmpty() ? defaultScopes : tset;
		}
	}

	public static boolean isBreakpointScopes(final String scopes) {
		return scopes.matches(BREAKPOINT_SCOPE_REGEX);
	}
	
	private static int parseInt(final String s) {
		try {
			return Integer.parseInt(s);
		}
		catch(Exception ex) {
			return -1;
		}
	}

	
	private static String getBreakpointScopeSymbolList() {
		// return "(!)"
		return BreakpointScope
					.all()
					.stream()
					.map(t -> t.symbol())
					.collect(Collectors.joining());
	}

	
	
	// build regex: "^[(!)]+$"
	private static final String BREAKPOINT_SCOPE_REGEX = 
			"^[" + getBreakpointScopeSymbolList() + "]+$";
}
