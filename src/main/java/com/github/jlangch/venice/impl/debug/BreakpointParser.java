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

	public static IBreakpoint parseBreakpoint(final String ref) {
		if (StringUtil.isBlank(ref)) {
			return null;
		}
		
		final String ref_ = ref.trim();
		
		final int pos = ref_.indexOf('/');
		if (pos < 1 || pos == (ref_.length()-1)) {
			return null;
		}
		else {
			final String s1 = ref_.substring(0, pos).trim();
			final String s2 = ref_.substring(pos+1).trim();
	
			if (isInteger(s2)) {
				// line breakpoint
				final int lineNr = parseInteger(s2);
				return lineNr < 1
						? null
						: new BreakpointLine(s1, lineNr);			
			}
			else {
				// function breakpoint
				return new BreakpointFn(ref_);
			}
		}
	}

	public static Set<BreakpointScope> parseBreakpointScopes(
			final String scopes
	) {
		return parseBreakpointScopes(scopes, new HashSet<>());
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

	
	private static boolean isInteger(final String s) {
		return s.matches("[0-9]+");
	}

	private static int parseInteger(final String s) {
		try {
			return Integer.parseInt(s);
		}
		catch(Exception ex) {
			return -1;
		}
	}

	
	
	// build regex: "^[(!)]+$"
	private static final String BREAKPOINT_SCOPE_REGEX = "^[(!)]+$";
}
