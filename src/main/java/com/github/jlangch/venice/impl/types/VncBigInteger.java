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

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.custom.VncWrappingTypeDef;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.MetaUtil;


public class VncBigInteger extends VncNumber {

	public VncBigInteger(final BigInteger v) { 
		this(v, null, Constants.Nil); 
	}

	public VncBigInteger(final double v) { 
		this(BigInteger.valueOf((long)v), null, Constants.Nil); 
	}

	public VncBigInteger(final long v) { 
		this(BigInteger.valueOf(v), null, Constants.Nil); 
	}
	
	public VncBigInteger(final BigInteger v, final VncVal meta) { 
		this(v, null, meta);
	}

	public VncBigInteger(
			final BigInteger v, 
			final VncWrappingTypeDef wrappingTypeDef, 
			final VncVal meta
	) {
		super(wrappingTypeDef, meta);
		value = v; 
	}
	
	
	public static VncBigInteger of(final VncVal v) { 
		if (Types.isVncNumber(v)) {
			return new VncBigInteger(((VncNumber)v).toJavaBigInteger());
		}
		else {
			throw new VncException(String.format(
					"Cannot convert value of type %s to big integer", 
					Types.getType(v)));
		}
	}
	
	
	@Override
	public VncBigInteger withMeta(final VncVal meta) {
		return new VncBigInteger(value, getWrappingTypeDef(), meta);
	}
	
	@Override
	public VncBigInteger wrap(final VncWrappingTypeDef wrappingTypeDef, final VncVal meta) {
		return new VncBigInteger(value, wrappingTypeDef, meta); 
	}
	
	@Override
	public VncKeyword getType() {
		return isWrapped() ? new VncKeyword(
									getWrappingTypeDef().getType().getQualifiedName(),
									MetaUtil.typeMeta(
										new VncKeyword(VncBigInteger.TYPE), 
										new VncKeyword(VncNumber.TYPE), 
										new VncKeyword(VncVal.TYPE)))
						   : new VncKeyword(
									VncBigInteger.TYPE, 
									MetaUtil.typeMeta(
										new VncKeyword(VncNumber.TYPE), 
										new VncKeyword(VncVal.TYPE)));
	}
	
	public VncBigInteger negate() { 
		return new VncBigInteger(value.negate()); 
	}

	public BigInteger getValue() { 
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
		return value;
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
		if (Types.isVncBigInteger(o)) {
			return value.compareTo(((VncBigInteger)o).getValue());
		}
		else if (Types.isVncBigDecimal(o)) {
			return value.compareTo(((VncBigDecimal)o).toJavaBigInteger());
		}
		else if (Types.isVncInteger(o)) {
			return value.compareTo(((VncInteger)o).toJavaBigInteger());
		}
		else if (Types.isVncDouble(o)) {
			return value.compareTo(((VncDouble)o).toJavaBigInteger());
		}
		else if (Types.isVncLong(o)) {
			return value.compareTo(((VncLong)o).toJavaBigInteger());
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
			return value.equals(((VncBigInteger)obj).value);
		}
	}

	@Override 
	public String toString() {
		return value.toString() + "N";
	}

	
    public static final String TYPE = ":core/bigint";

    private static final long serialVersionUID = -1848883965231344442L;

	private final BigInteger value;
}