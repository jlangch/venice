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

import static com.github.jlangch.venice.impl.debug.BreakpointScope.FunctionEntry;
import static com.github.jlangch.venice.impl.debug.BreakpointScope.FunctionException;
import static com.github.jlangch.venice.impl.debug.BreakpointScope.FunctionExit;
import static com.github.jlangch.venice.impl.util.CollectionUtil.toSet;
import static com.github.jlangch.venice.impl.util.StringUtil.isBlank;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


public class BreakpointFn {

	public BreakpointFn(
			final String qualifiedFnName,
			final Set<BreakpointScope> scopes
	) {
		if (isBlank(qualifiedFnName)) {
			throw new IllegalArgumentException("A qualifiedFnName must not be blank");
		}

		this.qualifiedFnName = qualifiedFnName;
		this.scopes = scopes == null || scopes.isEmpty() 
						? new HashSet<>(toSet(FunctionEntry)) 
						: new HashSet<>(scopes);
	}
	
	public String getQualifiedFnName() {
		return qualifiedFnName;
	}
	
	public boolean hasScope(final BreakpointScope scope) {
		return scope == null ? false : scopes.contains(scope);
	}
	
	public String getFormattedScopes() {
		return format(scopes);
	}
	
	
	private String format(final Set<BreakpointScope> scopes) {
		// predefined order of breakpoint scopes
		if (scopes.contains(FunctionException) || scopes.contains(FunctionExit)) {
			return Arrays.asList(FunctionEntry, FunctionException, FunctionExit)
						 .stream()
						 .filter(t -> scopes.contains(t))
						 .map(t -> t.symbol())
						 .collect(Collectors.joining());
		}
		else {
			return "";
		}
	}
	
	@Override
	public String toString() {
		return String.format("%s %s", qualifiedFnName, format(scopes));
	}
	
	
	
	private final String qualifiedFnName;
	private final Set<BreakpointScope> scopes;
}
