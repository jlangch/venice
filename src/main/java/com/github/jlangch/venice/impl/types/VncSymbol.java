/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2020 Venice
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
package com.github.jlangch.venice.impl.types;

import java.util.Arrays;
import java.util.List;

import com.github.jlangch.venice.impl.Namespaces;
import com.github.jlangch.venice.impl.types.util.Types;

public class VncSymbol extends VncVal {

	public VncSymbol(final String v) { 
		super(Constants.Nil);
		value = v; 
		ns = Namespaces.getNamespace(v);
	}

	public VncSymbol(final String v, final VncVal meta) { 
		super(meta);
		value = v; 
		ns = Namespaces.getNamespace(v);
	}
	
	@Override
	public VncSymbol withMeta(final VncVal meta) {
		return new VncSymbol(value, meta);
	}
	
	@Override
	public VncKeyword getType() {
		return TYPE;
	}

	@Override
	public VncKeyword getSupertype() {
		return VncVal.TYPE;
	}

	@Override
	public List<VncKeyword> getAllSupertypes() {
		return Arrays.asList(VncVal.TYPE);
	}

	public String getName() { 
		return value; 
	}

	public String getValue() { 
		return value; 
	}
	
	public String getNamespace() {
		return ns;
	}
	
	public boolean hasNamespace() {
		return ns != null;
	}
	
	@Override 
	public TypeRank typeRank() {
		return TypeRank.SYMBOL;
	}
	
	@Override
	public Object convertToJavaObject() {
		return value;
	}

	@Override 
	public int compareTo(final VncVal o) {
		if (o == Constants.Nil) {
			return 1;
		}
		else if (Types.isVncSymbol(o)) {
			return getName().compareTo(((VncSymbol)o).getName());
		}

		return super.compareTo(o);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		VncSymbol other = (VncSymbol) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override 
	public String toString() {
		return value;
	}
	

    public static final VncKeyword TYPE = new VncKeyword(":core/symbol");

    private static final long serialVersionUID = -1848883965231344442L;

	private final String value;
	private final String ns;
}