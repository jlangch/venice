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
import static com.github.jlangch.venice.impl.debug.breakpoint.FunctionScope.FunctionEntry;
import static com.github.jlangch.venice.impl.debug.breakpoint.FunctionScope.FunctionException;
import static com.github.jlangch.venice.impl.debug.breakpoint.FunctionScope.FunctionExit;
import static com.github.jlangch.venice.impl.util.CollectionUtil.toSet;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.util.StringUtil;


public class Selector {

	public Selector() {
		this(null, null);
	}

	public Selector(final Set<FunctionScope> scopes) {
		this(scopes, null);
	}

	public Selector(
			final Set<FunctionScope> scopes, 
			final AncestorSelector ancestorSelector
	) {
		this.scopes = scopes == null || scopes.isEmpty() 
						? DEFAULT_SCOPES
						: new HashSet<>(scopes);
		this.ancestorSelector = ancestorSelector;
	}
	
	
	public boolean hasScope(final FunctionScope scope) {
		return scope == null ? false : scopes.contains(scope);
	}
	
	public AncestorSelector getAncestorSelector() {
		return ancestorSelector;
	}

	public String formatForBaseFn(
			final String fnName, 
			final boolean useDescriptiveScopeNames
	) {
		final String sScopes = format(scopes, useDescriptiveScopeNames);
		
		if (ancestorSelector != null) {
			return StringUtil.isBlank(sScopes)
					? ancestorSelector.formatForBaseFn(fnName)
					: String.format(
							"%s at level %s", 
							ancestorSelector.formatForBaseFn(fnName), 
							sScopes);
		}
		else {
			return StringUtil.isBlank(sScopes)
					? fnName
					: String.format(
							"%s at level %s", 
							fnName, 
							sScopes);
		}
	}

	public String getFormattedScopes() {
		return format(scopes, false);
	}

	private String format(
			final Set<FunctionScope> scopes, 
			final boolean useDescriptiveScopeNames
	) {
		// predefined order of breakpoint scopes
		if (scopes.contains(FunctionException) || scopes.contains(FunctionExit)) {
			final String delimiter = useDescriptiveScopeNames ? ", " : "";
			return Arrays.asList(
							FunctionCall, 
							FunctionEntry, 
							FunctionException, 
							FunctionExit)
						 .stream()
						 .filter(t -> scopes.contains(t))
						 .map(t -> useDescriptiveScopeNames 
								 	? t.description() 
								 	: t.symbol())
						 .collect(Collectors.joining(delimiter));
		}
		else {
			return "";
		}
	}

	
	private static final Set<FunctionScope> DEFAULT_SCOPES = toSet(FunctionEntry);

	private final Set<FunctionScope> scopes;
	private final AncestorSelector ancestorSelector;
}
