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

import com.github.jlangch.venice.impl.functions.Numeric;
import com.github.jlangch.venice.impl.types.custom.VncWrappingTypeDef;
import com.github.jlangch.venice.impl.types.util.Types;


public class VncDouble extends VncNumber {

	public VncDouble(final Double v) { 
		this(v, null, Constants.Nil); 
	}
	
	public VncDouble(final Float v) { 
		this(v.doubleValue(), null, Constants.Nil); 
	}

	public VncDouble(final Long v) { 
		this(v.doubleValue(), null, Constants.Nil); 
	}

	public VncDouble(final Integer v) { 
		this(v.doubleValue(), null, Constants.Nil); 
	}
	
	public VncDouble(final Double v, final VncVal meta) { 
		this(v, null, meta);
	}

	public VncDouble(
			final Double v, 
			final VncWrappingTypeDef wrappingTypeDef, 
			final VncVal meta
	) { 
		super(wrappingTypeDef, meta);
		value = v; 
	}

	
	@Override
	public VncDouble withMeta(final VncVal meta) {
		return new VncDouble(value, getWrappingTypeDef(), meta);
	}
	
	@Override
	public VncDouble wrap(final VncWrappingTypeDef wrappingTypeDef, final VncVal meta) {
		return new VncDouble(value, wrappingTypeDef, meta); 
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
	
	public VncDouble negate() { 
		return new VncDouble(value * -1.0D); 
	}

	public Double getValue() { 
		return value; 
	}
	
	public Float getFloatValue() { 
		return value.floatValue(); 
	}

	@Override 
	public TypeRank typeRank() {
		return TypeRank.DOUBLE;
	}
	
	@Override
	public Object convertToJavaObject() {
		return value;
	}

	@Override 
	public int compareTo(final VncVal o) {
		if (Types.isVncDouble(o)) {
			return value.compareTo(((VncDouble)o).getValue());
		}
		else if (Types.isVncInteger(o)) {
			return value.compareTo(Numeric.intToDouble((VncInteger)o).getValue());
		}
		else if (Types.isVncLong(o)) {
			return value.compareTo(Numeric.longToDouble((VncLong)o).getValue());
		}
		else if (Types.isVncBigDecimal(o)) {
			return value.compareTo(Numeric.decimalToDouble((VncBigDecimal)o).getValue());
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
		VncDouble other = (VncDouble) obj;
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


    public static final VncKeyword TYPE = new VncKeyword(":core/double");

    private static final long serialVersionUID = -1848883965231344442L;

	private final Double value;
}