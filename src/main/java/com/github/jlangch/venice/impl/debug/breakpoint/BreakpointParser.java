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

import static com.github.jlangch.venice.impl.debug.breakpoint.FunctionScope.FunctionCall;
import static com.github.jlangch.venice.impl.debug.breakpoint.FunctionScope.FunctionException;
import static com.github.jlangch.venice.impl.debug.breakpoint.FunctionScope.FunctionExit;
import static com.github.jlangch.venice.impl.util.CollectionUtil.drop;
import static com.github.jlangch.venice.impl.util.CollectionUtil.toList;
import static com.github.jlangch.venice.impl.util.StringUtil.trimToEmpty;
import static com.github.jlangch.venice.impl.util.StringUtil.trimToNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.jlangch.venice.ParseError;
import com.github.jlangch.venice.impl.SpecialForms;
import com.github.jlangch.venice.impl.types.util.QualifiedName;
import com.github.jlangch.venice.impl.util.StringUtil;


/**
 * Breakpoint parser. Parses function and file/line breakpoints
 */
public class BreakpointParser {

	/**
	 * Parse breakpoint
	 * 
	 * <p>Breakpoint variants:
	 * <ul>
	 *   <li>filter</li>
	 *   <li>foo/count</li>
	 *   <li>foo/count filter map</li>
	 *   <li>foo/count > reduce</li>
	 *   <li>foo/count + reduce</li>
	 *   <li>() user/sum</li>
	 *   <li>(!) user/sum + *</li>
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
	 * @param definition the breakpoint definition
	 * @return the parsed breakpoints
	 */
	public static List<BreakpointFn> parseBreakpoints(final String definition) {
		return parseBreakpoints(Arrays.asList(definition.trim().split(" +")));
	}

	public static List<BreakpointFn> parseBreakpoints(final List<String> tokens) {
		if (tokens.isEmpty()) {
			return new ArrayList<BreakpointFn>();
		}
		
		// First token: optional scopes
		final String scopes = trimToEmpty(tokens.get(0));
		final boolean hasScopes = isBreakpointScopes(scopes);
		final Set<FunctionScope> scopeSet = parseBreakpointScopes(
													hasScopes ? scopes : null);

		final List<String> bpTokens = hasScopes ? drop(tokens,1) : tokens;
		
		final String selector = bpTokens.size() == 3 ? bpTokens.get(1) : ""; 
		
		switch (selector) {
			case ">": 
				return toList(
						validate(
							new BreakpointFn(
									QualifiedName.parse(bpTokens.get(2)),
									new Selector(
										scopeSet,
										new AncestorSelector(
												QualifiedName.parse(bpTokens.get(0)),
												AncestorType.Nearest)))));
				
			case "+":
				return toList(
						validate(
							new BreakpointFn(
									QualifiedName.parse(bpTokens.get(2)),
									new Selector(
										scopeSet,
										new AncestorSelector(
												QualifiedName.parse(bpTokens.get(0)),
												AncestorType.Any)))));
				
			default:
				return bpTokens
						.stream()
						.map(s -> StringUtil.trimToNull(s.replace(',', ' ')))
						.filter(s -> isBreakpointRefCandidate(s))
						.map(s -> parseBreakpoint(s, scopeSet))
						.filter(b -> b != null)
						.map(s -> validate(s))
						.collect(Collectors.toList());
		}		
	}
	
	/**
	 * Parse a breakpoint given by a reference.
	 * 
	 * @param ref a breakpoint reference
	 * @return A breakpoint or <code>null</code> if the passed reference
	 *         could not be parsed
	 */
	private static BreakpointFn parseBreakpoint(
			final String ref,
			final Set<FunctionScope> scopes
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
							new Selector(scopes));
			}
			else {
				return null;
			}
		}
		catch(RuntimeException ex) {
			return null;
		}
	}

	public static Set<FunctionScope> parseBreakpointScopes(
			final String scopes
	) {
		return parseBreakpointScopes(scopes, new HashSet<>());
	}

	public static Set<FunctionScope> parseBreakpointScopes(
			final String scopes,
			final Set<FunctionScope> defaultScopes
	) {
		if (trimToNull(scopes) == null) {
			return defaultScopes;
		}
		else {
			final Set<FunctionScope> tset = 
					Arrays.asList(FunctionScope.values())
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
	
	private static BreakpointFn validate(final BreakpointFn bp) {
		final String fnName = bp.getQualifiedFnName();
		
		if (SpecialForms.isSpecialForm(fnName)) {		
			if (!isSpecialFormSupportedForDebugging(fnName)) {
				throw new ParseError(
						String.format(
							"The special form '%s' is not supported for debugging! "
								+ "Only the forms %s are supported yet.",
							fnName,
							SUPPORTED_SPECIAL_FORMS
								.stream()
								.sorted()
								.map(s -> "'" + s + "'")
								.collect(Collectors.joining(", "))));
			}

			bp.getSelectors().forEach(s -> {
				if (s.hasScope(FunctionCall) 
						|| s.hasScope(FunctionExit) 
						|| s.hasScope(FunctionException)
				) {
					throw new ParseError(
							String.format(
								"Breakpoints on special forms like '%s' do not "
									+ "support level %s, %s, or %s!",
								fnName,
								FunctionCall.description(),
								FunctionExit.description(),
								FunctionException.description()));
				}
			});
		}
		
		return bp;
	}
	
	
	private static boolean isSpecialFormSupportedForDebugging(final String name) {
		return SUPPORTED_SPECIAL_FORMS.contains(name);
	}

	public static Set<String> SUPPORTED_SPECIAL_FORMS = new HashSet<>(
			Arrays.asList("if", "let", "bindings", "loop", "try-with"));

	private static final String BREAKPOINT_SCOPE_REGEX = "^[>(!)]+$";
}
