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


public class VncDouble extends VncNumber {

	public VncDouble(final Double v) { 
		super(null, Constants.Nil);
		value = v; 
	}
	
	public VncDouble(final Float v) { 
		super(null, Constants.Nil);
		value = v.doubleValue(); 
	}

	public VncDouble(final Long v) { 
		super(null, Constants.Nil);
		value = v.doubleValue(); 
	}

	public VncDouble(final Integer v) { 
		super(null, Constants.Nil);
		value = v.doubleValue(); 
	}
	
	public VncDouble(final Double v, final VncVal meta) { 
		super(null, meta);
		value = v; 
	}

	public VncDouble(
			final Double v, 
			final VncWrappingTypeDef wrappingTypeDef, 
			final VncVal meta
	) { 
		super(wrappingTypeDef, meta);
		value = v; 
	}
	
	
	public static VncDouble of(final VncVal v) { 
		if (Types.isVncNumber(v)) {
			return new VncDouble(((VncNumber)v).toJavaDouble());
		}
		else {
			throw new VncException(String.format(
					"Cannot convert value of type %s to double", 
					Types.getType(v)));
		}
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
		return isWrapped() ? TYPE : super.getType();
	}
	
	@Override
	public List<VncKeyword> getAllSupertypes() {
		return isWrapped() 
				? Arrays.asList(TYPE, VncVal.TYPE)
				: super.getAllSupertypes();
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
	public Integer toJavaInteger() {
		return value.intValue(); 
	}
	
	@Override
	public Long toJavaLong() {
		return value.longValue(); 
	}
	
	@Override
	public Double toJavaDouble() {
		return value;
	}
	
	@Override
	public BigInteger toJavaBigInteger() {
		return BigInteger.valueOf(value.longValue());
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
		if (Types.isVncDouble(o)) {
			return value.compareTo(((VncDouble)o).toJavaDouble());
		}
		else if (Types.isVncInteger(o)) {
			return value.compareTo(((VncInteger)o).toJavaDouble());
		}
		else if (Types.isVncLong(o)) {
			return value.compareTo(((VncLong)o).toJavaDouble());
		}
		else if (Types.isVncBigDecimal(o)) {
			return value.compareTo(((VncBigDecimal)o).toJavaDouble());
		}
		else if (Types.isVncBigInteger(o)) {
			return value.compareTo(((VncBigInteger)o).toJavaDouble());
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
			return value.equals(((VncDouble)obj).value);
		}
	}

	@Override 
	public String toString() {
		return value.toString();
	}


    public static final VncKeyword TYPE = new VncKeyword(":core/double");

    private static final long serialVersionUID = -1848883965231344442L;

	private final Double value;
}