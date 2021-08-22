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

import static com.github.jlangch.venice.impl.debug.breakpoint.BreakpointScope.FunctionEntry;
import static com.github.jlangch.venice.impl.util.CollectionUtil.drop;
import static com.github.jlangch.venice.impl.util.CollectionUtil.toList;
import static com.github.jlangch.venice.impl.util.CollectionUtil.toSet;
import static com.github.jlangch.venice.impl.util.StringUtil.trimToEmpty;
import static com.github.jlangch.venice.impl.util.StringUtil.trimToNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.types.util.QualifiedName;
import com.github.jlangch.venice.impl.util.StringUtil;


/**
 * Breakpoint parser. Parses function and file/line breakpoints
 */
public class BreakpointParser {

	/**
	 * Parse breakpoint from tokens
	 * 
	 * <p>Breakpoint variants:
	 * <ul>
	 *   <li>user/sum</li>
	 *   <li>user/sum + *</li>
	 *   <li>() user/sum</li>
	 *   <li>(!) user/sum + *</li>
	 * </ul>
	 * 
	 * @param tokens the tokens
	 * @return the parsed breakpoints
	 */
	public static List<IBreakpoint> parseBreakpoints(final List<String> tokens) {
		if (tokens.isEmpty()) {
			return new ArrayList<IBreakpoint>();
		}
		
		// First token: optional scopes
		final String scopes = trimToEmpty(tokens.get(0));
		final boolean hasScopes = isBreakpointScopes(scopes);
		final Set<BreakpointScope> scopeSet = parseBreakpointScopes(
													hasScopes ? scopes : null);

		final List<String> bpTokens = hasScopes ? drop(tokens,1) : tokens;
		
		final String selector = bpTokens.size() == 3 ? bpTokens.get(1) : ""; 
		
		switch (selector) {
			case ">": 
				final QualifiedName parent = QualifiedName.parse(bpTokens.get(0));
				return toList(new BreakpointFn(
									QualifiedName.parse(bpTokens.get(2)), 
									scopeSet));
				
			case "+":
				final QualifiedName anscestor = QualifiedName.parse(bpTokens.get(0));
				return toList(new BreakpointFn(
									QualifiedName.parse(bpTokens.get(2)), 
									scopeSet));
				
			default:
				return bpTokens
						.stream()
						.map(s -> StringUtil.trimToNull(s.replace(',', ' ')))
						.filter(s -> isBreakpointRefCandidate(s))
						.map(s -> parseBreakpoint(s, scopeSet))
						.filter(b -> b != null)
						.collect(Collectors.toList());
		}		
	}
	
	/**
	 * Parse a breakpoint given by a reference.
	 * 
	 * <p>Function breakpoints:
	 * <ul>
	 *   <li>filter</li>
	 *   <li>foo/count</li>
	 *   <li>foo/*</li>
	 *   <li>foo/count > reduce</li>
	 *   <li>foo/count + reduce</li>
	 * </ul>
	 * 
	 * Selector style function break points:
	 * <br/>
	 * <i>foo/*:</i>  break in any function defined in namespace foo
	 * <br/>
	 * <i>function1 > function2:</i>  break in function2 if function1 is its immediate caller
	 * <br/>
	 * <i>function1 + function2:</i>  break in function2 if function1 is in the caller hierarchy
	 * 
	 * @param ref a breakpoint reference
	 * @return A breakpoint or <code>null</code> if the passed reference
	 *         could not be parsed
	 */
	public static IBreakpoint parseBreakpoint(
			final String ref,
			final Set<BreakpointScope> scopes
	) {
		if (StringUtil.isBlank(ref)) {
			return null;
		}
		
		try {
			final String ref_ = ref.trim();
			
			if (isBreakpointFn(ref_)) {
				// function breakpoint
				return new BreakpointFn(
							QualifiedName.parse(ref_), 
							scopes == null ? DEFAULT_SCOPES : scopes);
			}
			else {
				return null;
			}
		}
		catch(RuntimeException ex) {
			return null;
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
	
	private static boolean isBreakpointFn(final String ref) {
		return ref.equals("/") || ref.matches("([^0-9/][^/]*|[^0-9/][^/]*/[^/]+)");
	}
	
	private static boolean isBreakpointRefCandidate(final String s) {
		return s != null && !isBreakpointScopes(s);
	}
	
	
	private static final Set<BreakpointScope> DEFAULT_SCOPES = toSet(FunctionEntry);

	private static final String BREAKPOINT_SCOPE_REGEX = "^[>(!)]+$";
}
