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
package com.github.jlangch.venice.impl.types;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.custom.VncWrappingTypeDef;
import com.github.jlangch.venice.impl.types.util.Types;


public class VncInteger extends VncNumber {

	public VncInteger(final Integer v) { 
		super(null, Constants.Nil);
		value = v; 
	}
	
	public VncInteger(final Long v) { 
		super(null, Constants.Nil);
		value = v.intValue(); 
	}
	
	public VncInteger(final Integer v, final VncVal meta) { 
		super(null, meta);
		value = v; 
	}

	public VncInteger(
			final Integer v, 
			final VncWrappingTypeDef wrappingTypeDef, 
			final VncVal meta
	) { 
		super(wrappingTypeDef, meta);
		value = v; 
	}
	
	
	public static VncInteger of(final VncVal v) { 
		if (Types.isVncNumber(v)) {
			return new VncInteger(((VncNumber)v).toJavaInteger());
		}
		else {
			throw new VncException(String.format(
					"Cannot convert value of type %s to integer", 
					Types.getType(v)));
		}
	}
	
	
	@Override
	public VncInteger withMeta(final VncVal meta) {
		return new VncInteger(value, getWrappingTypeDef(), meta);
	}
	
	@Override
	public VncInteger wrap(final VncWrappingTypeDef wrappingTypeDef, final VncVal meta) {
		return new VncInteger(value, wrappingTypeDef, meta); 
	}
	
	@Override
	public VncKeyword getType() {
		return isWrapped() ? getWrappingTypeDef().getType() : TYPE;
	}
	
	@Override
	public VncKeyword getSupertype() {
		return isWrapped() ? TYPE : super.getType();
	}
	
	@Override
	public List<VncKeyword> getAllSupertypes() {
		return isWrapped() 
				? Arrays.asList(TYPE, VncVal.TYPE)
				: super.getAllSupertypes();
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
	public Integer toJavaInteger() {
		return value; 
	}
	
	@Override
	public Long toJavaLong() {
		return value.longValue(); 
	}
	
	@Override
	public Double toJavaDouble() {
		return value.doubleValue();
	}
	
	@Override
	public BigInteger toJavaBigInteger() {
		return BigInteger.valueOf(value);
	}
	
	@Override
	public BigDecimal toJavaBigDecimal() {
		return new BigDecimal(value);
	}
	
	@Override
	public BigDecimal toJavaBigDecimal(final int scale) {
		return new BigDecimal(value).setScale(scale);
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
			return value.compareTo(((VncDouble)o).getValue().intValue());
		}
		else if (Types.isVncBigDecimal(o)) {
			return value.compareTo(((VncBigDecimal)o).toJavaInteger());
		}
		else if (Types.isVncBigInteger(o)) {
			return value.compareTo(((VncBigInteger)o).toJavaInteger());
		}
		else if (o == Constants.Nil) {
			return 1;
		}

		return super.compareTo(o);
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		else if (getClass() != obj.getClass()) {
			return false;
		}
		else {
			return value.equals(((VncInteger)obj).value);
		}
	}

	@Override 
	public String toString() {
		return value.toString() + "I";
	}

    
    public static final VncKeyword TYPE = new VncKeyword(":core/integer");

    private static final long serialVersionUID = -1848883965231344442L;

	private final Integer value;
}