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
import java.math.RoundingMode;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.custom.VncWrappingTypeDef;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.MetaUtil;


public class VncBigDecimal extends VncNumber {

	public VncBigDecimal(final BigDecimal v) { 
		this(v, null, Constants.Nil); 
	}

	public VncBigDecimal(final double v) { 
		this(BigDecimal.valueOf(v), null, Constants.Nil); 
	}

	public VncBigDecimal(final long v) { 
		this(BigDecimal.valueOf(v), null, Constants.Nil); 
	}
	
	public VncBigDecimal(final BigDecimal v, final VncVal meta) { 
		this(v, null, meta);
	}

	public VncBigDecimal(
			final BigDecimal v, 
			final VncWrappingTypeDef wrappingTypeDef, 
			final VncVal meta
	) {
		super(wrappingTypeDef, meta);
		value = v; 
	}
	
	
	public static VncBigDecimal of(final VncVal v) { 
		if (Types.isVncNumber(v)) {
			return new VncBigDecimal(((VncNumber)v).toJavaBigDecimal());
		}
		else {
			throw new VncException(String.format(
					"Cannot convert value of type %s to decimal", 
					Types.getType(v)));
		}
	}
	@Override
	public VncBigDecimal withMeta(final VncVal meta) {
		return new VncBigDecimal(value, getWrappingTypeDef(), meta);
	}
	
	@Override
	public VncBigDecimal wrap(final VncWrappingTypeDef wrappingTypeDef, final VncVal meta) {
		return new VncBigDecimal(value, wrappingTypeDef, meta); 
	}
	
	@Override
	public VncKeyword getType() {
		return isWrapped() ? new VncKeyword(
								getWrappingTypeDef().getType().getQualifiedName(),
								MetaUtil.typeMeta(
											new VncKeyword(VncBigDecimal.TYPE), 
											new VncKeyword(VncNumber.TYPE), 
											new VncKeyword(VncVal.TYPE)))
						   : new VncKeyword(
									VncBigDecimal.TYPE, 
									MetaUtil.typeMeta(
										new VncKeyword(VncNumber.TYPE), 
										new VncKeyword(VncVal.TYPE)));
	}
	
	public VncBigDecimal negate() { 
		return new VncBigDecimal(value.negate()); 
	}

	public BigDecimal getValue() { 
		return value; 
	}
	
	@Override 
	public TypeRank typeRank() {
		return TypeRank.BIGDECIMAL;
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
		return value.doubleValue();
	}
	
	@Override
	public BigInteger toJavaBigInteger() {
		return value.toBigInteger();
	}
	
	@Override
	public BigDecimal toJavaBigDecimal() {
		return value;
	}
	
	@Override
	public BigDecimal toJavaBigDecimal(final int scale) {
		return value.setScale(scale);
	}

	@Override 
	public int compareTo(final VncVal o) {
		if (Types.isVncBigDecimal(o)) {
			return value.compareTo(((VncBigDecimal)o).getValue());
		}
		else if (Types.isVncInteger(o)) {
			return value.compareTo(((VncInteger)o).toJavaBigDecimal());
		}
		else if (Types.isVncDouble(o)) {
			return value.compareTo(((VncDouble)o).toJavaBigDecimal());
		}
		else if (Types.isVncLong(o)) {
			return value.compareTo(((VncLong)o).toJavaBigDecimal());
		}
		else if (Types.isVncBigInteger(o)) {
			return value.compareTo(((VncBigInteger)o).toJavaBigDecimal());
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
			return value.equals(((VncBigDecimal)obj).value);
		}
	}

	@Override 
	public String toString() {
		return value.toString() + "M";
	}

	public static RoundingMode toRoundingMode(final VncString val) {
		return RoundingMode.valueOf(RoundingMode.class, val.getValue());
	}

	
    public static final String TYPE = ":core/decimal";

    private static final long serialVersionUID = -1848883965231344442L;

	private final BigDecimal value;
}