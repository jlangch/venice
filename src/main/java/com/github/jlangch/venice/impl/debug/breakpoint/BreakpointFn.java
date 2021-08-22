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

import static com.github.jlangch.venice.impl.debug.breakpoint.BreakpointScope.FunctionCall;
import static com.github.jlangch.venice.impl.debug.breakpoint.BreakpointScope.FunctionEntry;
import static com.github.jlangch.venice.impl.debug.breakpoint.BreakpointScope.FunctionException;
import static com.github.jlangch.venice.impl.debug.breakpoint.BreakpointScope.FunctionExit;
import static com.github.jlangch.venice.impl.util.CollectionUtil.toSet;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.types.util.QualifiedName;
import com.github.jlangch.venice.impl.util.StringUtil;


/**
 * Defines a breakpoint given by qualified function name
 */
public class BreakpointFn implements IBreakpoint {

	public BreakpointFn(
			final QualifiedName qualifiedName
	) {
		if (qualifiedName == null) {
			throw new IllegalArgumentException("A qualifiedName must not be null");
		}

		this.qn = qualifiedName;
		this.scopes = DEFAULT_SCOPES;
	}

	public BreakpointFn(
			final QualifiedName qualifiedName,
			final Set<BreakpointScope> scopes
	) {
		if (qualifiedName == null) {
			throw new IllegalArgumentException("A qualifiedName must not be null");
		}

		this.qn = qualifiedName;
		this.scopes = scopes == null || scopes.isEmpty() 
						? DEFAULT_SCOPES
						: new HashSet<>(scopes);
	}

	
	public BreakpointFn withScopes(final Set<BreakpointScope> scopes) {
		return new BreakpointFn(qn, scopes);
	}

	public String getQualifiedFnName() {
		return qn.getQualifiedName();
	}

	public String getNamespace() {
		return qn.getNamespace();
	}

	public String getSimpleFnName() {
		return qn.getSimpleName();
	}
	
	public boolean hasScope(final BreakpointScope scope) {
		return scope == null ? false : scopes.contains(scope);
	}
	
	public String getFormattedScopes() {
		return format(scopes, false);
	}

	@Override
	public IBreakpointRef getBreakpointRef() {
		return new BreakpointFnRef(qn.getQualifiedName());
	}
	
	@Override
	public String format() {
		final String sScopes = format(scopes, false);
		
		return StringUtil.isBlank(sScopes)
				? qn.getQualifiedName()
				: String.format("%s at level %s", qn.getQualifiedName(), sScopes);
	}
	
	@Override
	public String formatEx() {
		final String sScopes = format(scopes, true);
		
		return StringUtil.isBlank(sScopes)
				? qn.getQualifiedName()
				: String.format("%s at level %s", qn.getQualifiedName(), sScopes);
	}
	
	@Override
	public String toString() {
		return String.format("Function breakpoint: %s", format());
	}
	
	@Override
	public int hashCode() {
		return qn.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BreakpointFn other = (BreakpointFn) obj;
		return (qn.equals(other.qn));
	}

	@Override
	public int compareTo(final IBreakpoint o) {
		if (o instanceof BreakpointFn) {
			return comp.compare(this, (BreakpointFn)o);
		}
		else {
			return -1;
		}
	}
	
	
	private String format(final Set<BreakpointScope> scopes, final boolean extended) {
		// predefined order of breakpoint scopes
		if (scopes.contains(FunctionException) || scopes.contains(FunctionExit)) {
			final String delimiter = extended ? ", " : "";
			return Arrays.asList(
							FunctionCall, 
							FunctionEntry, 
							FunctionException, 
							FunctionExit)
						 .stream()
						 .filter(t -> scopes.contains(t))
						 .map(t -> extended ? t.description() : t.symbol())
						 .collect(Collectors.joining(delimiter));
		}
		else {
			return "";
		}
	}
	
	
	private static Comparator<BreakpointFn> comp = 
			Comparator.comparing(BreakpointFn::getQualifiedFnName);
	
	private static final Set<BreakpointScope> DEFAULT_SCOPES = toSet(FunctionEntry);
	
	private final QualifiedName qn;
	private final Set<BreakpointScope> scopes;
}
