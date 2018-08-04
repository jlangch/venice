/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2018 Venice
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

import static com.github.jlangch.venice.impl.types.Constants.False;
import static com.github.jlangch.venice.impl.types.Constants.True;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.github.jlangch.venice.VncException;


public class VncBigDecimal extends VncVal {

	public VncBigDecimal(final BigDecimal v) { 
		value = v; 
	}

	public VncBigDecimal copy() { 
		final VncBigDecimal v = new VncBigDecimal(value);
		v.setMeta(getMeta());
		return v;
	}

	public BigDecimal getValue() { 
		return value; 
	}
		
	public VncBigDecimal inc() {
		return new VncBigDecimal(value.add(new BigDecimal(1)));
	}
	
	public VncBigDecimal dec() {
		return new VncBigDecimal(value.subtract(new BigDecimal(1)));
	}
	
	public VncConstant lt(final VncVal other) {
		if (other instanceof VncLong) {
			return value.compareTo(toDecimal((VncLong)other).getValue()) < 0 ? True : False;
		}
		else if (other instanceof VncDouble) {
			return value.compareTo(toDecimal((VncDouble)other).getValue()) < 0 ? True : False;
		}
		else if (other instanceof VncBigDecimal) {
			return value.compareTo(((VncBigDecimal)other).getValue()) < 0 ? True : False;
		}
		else {
			throw new VncException(String.format(
									"Function '<' with operand 1 of type %s does not allow %s as operand 2", 
									this.getClass().getSimpleName(),
									other.getClass().getSimpleName()));
		}
	}
	
	public VncConstant lte(final VncVal other) {
		if (other instanceof VncLong) {
			return value.compareTo(toDecimal((VncLong)other).getValue()) <= 0 ? True : False;
		}
		else if (other instanceof VncDouble) {
			return value.compareTo(toDecimal((VncDouble)other).getValue()) <= 0 ? True : False;
		}
		else if (other instanceof VncBigDecimal) {
			return value.compareTo(((VncBigDecimal)other).getValue()) <= 0 ? True : False;
		}
		else {
			throw new VncException(String.format(
									"Function '<=' with operand 1 of type %s does not allow %s as operand 2", 
									this.getClass().getSimpleName(),
									other.getClass().getSimpleName()));
		}
	}
	
	public VncConstant gt(final VncVal other) {
		if (other instanceof VncLong) {
			return value.compareTo(toDecimal((VncLong)other).getValue()) > 0 ? True : False;
		}
		else if (other instanceof VncDouble) {
			return value.compareTo(toDecimal((VncDouble)other).getValue()) > 0 ? True : False;
		}
		else if (other instanceof VncBigDecimal) {
			return value.compareTo(((VncBigDecimal)other).getValue()) > 0 ? True : False;
		}
		else {
			throw new VncException(String.format(
									"Function '>' with operand 1 of type %s does not allow %s as operand 2", 
									this.getClass().getSimpleName(),
									other.getClass().getSimpleName()));
		}
	}
	
	public VncConstant gte(final VncVal other) {
		if (other instanceof VncLong) {
			return value.compareTo(toDecimal((VncLong)other).getValue()) >= 0 ? True : False;
		}
		else if (other instanceof VncDouble) {
			return value.compareTo(toDecimal((VncDouble)other).getValue()) >= 0 ? True : False;
		}
		else if (other instanceof VncBigDecimal) {
			return value.compareTo(((VncBigDecimal)other).getValue()) >= 0 ? True : False;
		}
		else {
			throw new VncException(String.format(
									"Function '>=' with operand 1 of type %s does not allow %s as operand 2", 
									this.getClass().getSimpleName(),
									other.getClass().getSimpleName()));
		}
	}

	public VncLong toLong() {
		return new VncLong(Long.valueOf(value.longValue()));
	}

	public VncDouble toDouble() {
		return new VncDouble(Double.valueOf(value.doubleValue()));
	}

	public static VncBigDecimal toDecimal(final VncLong val) {
		return new VncBigDecimal(new BigDecimal(((VncLong)val).getValue()));
	}

	public static VncBigDecimal toDecimal(final VncDouble val) {
		return new VncBigDecimal(new BigDecimal(((VncDouble)val).getValue()));
	}

	public static RoundingMode toRoundingMode(final VncString val) {
		return RoundingMode.valueOf(RoundingMode.class, val.getValue());
	}

	@Override 
	public int compareTo(final VncVal o) {
		return Types.isVncDouble(o) ? getValue().compareTo(((VncBigDecimal)o).getValue()) : 0;
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
		VncBigDecimal other = (VncBigDecimal) obj;
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


	private final BigDecimal value;
}