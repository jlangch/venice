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

import com.github.jlangch.venice.impl.types.custom.VncWrappingTypeDef;
import com.github.jlangch.venice.impl.types.util.Types;


public class VncBoolean extends VncVal {

	public VncBoolean(final Boolean v) { 
		this(v, null, Constants.Nil); 
	}

	public VncBoolean(final Boolean v, final VncVal meta) { 
		this(v, null, meta);
	}

	public VncBoolean(
			final Boolean v, 
			final VncWrappingTypeDef wrappingTypeDef, 
			final VncVal meta
	) { 
		super(wrappingTypeDef, meta);
		value = v; 
	}
	
	public static VncBoolean of(final boolean bool) {
		return bool ? True : False;
	}
	
	public static boolean isTrue(final VncVal val) {
		return (val instanceof VncBoolean) && (((VncBoolean)val).getValue() == Boolean.TRUE);
	}
	
	public static boolean isFalse(final VncVal val) {
		return (val instanceof VncBoolean) && (((VncBoolean)val).getValue() == Boolean.FALSE);
	}
	
	@Override
	public VncBoolean withMeta(final VncVal meta) {
		return new VncBoolean(value, getWrappingTypeDef(), meta);
	}
	
	@Override
	public VncBoolean wrap(final VncWrappingTypeDef wrappingTypeDef, final VncVal meta) {
		return new VncBoolean(value, wrappingTypeDef, meta); 
	}
	
	@Override
	public VncKeyword getType() {
		return isWrapped() ? getWrappingTypeDef().getType() : TYPE;
	}
	
	@Override
	public VncKeyword getSupertype() {
		return isWrapped() ? TYPE : VncVal.TYPE;
	}
	
	@Override
	public List<VncKeyword> getAllSupertypes() {
		return isWrapped() 
				? Arrays.asList(TYPE, VncVal.TYPE)
				: Arrays.asList(VncVal.TYPE);
	}

	public Boolean getValue() { 
		return value; 
	}
	
	@Override 
	public TypeRank typeRank() {
		return TypeRank.BOOLEAN;
	}
	
	@Override
	public Object convertToJavaObject() {
		return value;
	}

	@Override 
	public int compareTo(final VncVal o) {
		if (Types.isVncBoolean(o)) {
			return value.compareTo(((VncBoolean)o).getValue());
		}
		else if (o == Constants.Nil) {
			return 1;
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
		VncBoolean other = (VncBoolean) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override 
	public String toString() {
		return value.toString();
	}

	
	public static final VncBoolean True = new VncBoolean(true);
	public static final VncBoolean False = new VncBoolean(false);
    
    public static final VncKeyword TYPE = new VncKeyword(":core/boolean");

    private static final long serialVersionUID = -1848883965231344442L;

	private final Boolean value;
}