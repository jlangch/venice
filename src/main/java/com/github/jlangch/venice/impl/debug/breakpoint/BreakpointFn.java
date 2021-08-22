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

import java.util.Comparator;

import com.github.jlangch.venice.impl.types.util.QualifiedName;


/**
 * Defines a breakpoint given by qualified function name
 */
public class BreakpointFn implements Comparable<BreakpointFn> {

	public BreakpointFn(
			final QualifiedName qualifiedName
	) {
		this(qualifiedName, null);
	}

	public BreakpointFn(
			final QualifiedName qualifiedName,
			final Selector selector
	) {
		if (qualifiedName == null) {
			throw new IllegalArgumentException("A qualifiedName must not be null");
		}

		this.qn = qualifiedName;
		this.selector = selector == null ? new Selector() : selector;
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

	public Selector getSelector() {
		return selector;
	}

	public BreakpointFnRef getBreakpointRef() {
		return new BreakpointFnRef(qn.getQualifiedName());
	}
	
	public String format(boolean useDescriptiveScopeNames) {
		return selector.formatForBaseFn(
				qn.getQualifiedName(), 
				useDescriptiveScopeNames);
	}
	
	@Override
	public String toString() {
		return String.format("Function breakpoint: %s", format(false));
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
	public int compareTo(final BreakpointFn o) {
		return comp.compare(this, (BreakpointFn)o);
	}
	
	
	private static Comparator<BreakpointFn> comp = 
			Comparator.comparing(BreakpointFn::getQualifiedFnName);
	
	private final QualifiedName qn;
	private final Selector selector;
}
