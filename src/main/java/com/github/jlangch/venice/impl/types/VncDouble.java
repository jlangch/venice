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

import java.math.BigDecimal;


public class VncDouble extends VncVal {

	public VncDouble(final Double v) { 
		value = v; 
	}
	
	public VncDouble(final Float v) { 
		value = v.doubleValue(); 
	}
	
	public VncDouble copy() { 
		final VncDouble v = new VncDouble(value);
		v.setMeta(getMeta());
		return v;
	}

	public Double getValue() { 
		return value; 
	}
	
	public VncDouble inc() {
		return new VncDouble(value + 1.D);
	}
	
	public VncDouble dec() {
		return new VncDouble(value - 1D);
	}

	public VncLong toLong() {
		return new VncLong(Long.valueOf(value.longValue()));
	}

	public VncBigDecimal toDecimal() {
		return new VncBigDecimal(new BigDecimal(value.doubleValue()));
	}

	@Override 
	public int compareTo(final VncVal o) {
		if (o == Constants.Nil) {
			return 1;
		}
		else if (o instanceof VncLong) {
			return value.compareTo(new Double(((VncLong)o).getValue()));
		}
		else if (o instanceof VncDouble) {
			return value.compareTo(((VncDouble)o).getValue());
		}
		else if (o instanceof VncBigDecimal) {
			return new BigDecimal(value.doubleValue()).compareTo(((VncBigDecimal)o).getValue());
		}
		else {
			return 0;
		}
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


	private final Double value;
}