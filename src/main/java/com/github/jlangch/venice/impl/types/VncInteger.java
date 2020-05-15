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

import com.github.jlangch.venice.impl.functions.Numeric;
import com.github.jlangch.venice.impl.types.util.Types;


public class VncInteger extends VncVal {

	public VncInteger(final Integer v) { 
		this(v, Constants.Nil); 
	}
	
	public VncInteger(final Long v) { 
		this(v.intValue(), Constants.Nil); 
	}

	public VncInteger(final Integer v, final VncVal meta) { 
		super(meta);
		value = v; 
	}
	
	
	@Override
	public VncInteger withMeta(final VncVal meta) {
		return new VncInteger(value, meta);
	}
	
	@Override
	public VncKeyword getType() {
		return new VncKeyword(":core/integer");
	}
	
	public VncInteger negate() { 
		return new VncInteger(value * -1); 
	}

	public Integer getValue() { 
		return value; 
	}
	
	public Long getLongValue() { 
		return value.longValue(); 
	}
	
	@Override 
	public TypeRank typeRank() {
		return TypeRank.INTEGER;
	}
	
	@Override
	public Object convertToJavaObject() {
		return value;
	}

	@Override 
	public int compareTo(final VncVal o) {
		if (Types.isVncInteger(o)) {
			return value.compareTo(((VncInteger)o).getValue());
		}
		else if (Types.isVncLong(o)) {
			return value.compareTo(((VncLong)o).getIntValue());
		}
		else if (Types.isVncDouble(o)) {
			return value.compareTo(Numeric.doubleToInt((VncDouble)o).getValue());
		}
		else if (Types.isVncBigDecimal(o)) {
			return value.compareTo(Numeric.decimalToInt((VncBigDecimal)o).getValue());
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
		VncInteger other = (VncInteger) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override 
	public String toString() {
		return value.toString() + "I";
	}


    private static final long serialVersionUID = -1848883965231344442L;

	private final Integer value;
}