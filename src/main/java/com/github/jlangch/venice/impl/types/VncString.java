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

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.util.StringUtil;


public class VncString extends VncVal {
	
	public VncString(final String v) { 
		value = v; 
	}

	public String getValue() { 
		return value; 
	}
	
	public VncString copy() { 
		final VncString v = new VncString(value); 
		v.setMeta(getMeta());
		return v;
	}

	public int size() {
		return value.length();
	}
	
	public boolean isEmpty() {
		return value.isEmpty();
	}

	public VncVal nth(final int idx) {
		if (idx < 0 || idx >= value.length()) {
			throw new VncException(String.format(
					"nth: index %d out of range for a string of length %d",
					idx,
					value.length()));
		}

		return new VncString(String.valueOf(value.charAt(idx)));
	}

	public VncVal nthOrDefault(final int idx, final VncString defaultVal) {
		return (idx < 0 || idx >= value.length()) ? defaultVal : nth(idx);
	}

	public VncVal first() {
		return isEmpty() ? Constants.Nil : nth(0);
	}

	public VncVal second() {
		return size() < 2 ? Constants.Nil : nth(1);
	}

	public VncVal last() {
		return isEmpty() ? Constants.Nil : nth(value.length()-1);
	}
	
	public VncList toVncList() {
		final VncList list = new VncList();
		for(char c : value.toCharArray()) {
			list.addAtEnd(new VncString(String.valueOf((char)c)));
		}
		return list;
	}

	public VncSymbol toSymbol() {
		return new VncSymbol(getValue());
	}

	@Override 
	public int compareTo(final VncVal o) {
		if (o == Constants.Nil) {
			return 1;
		}
		else if (Types.isVncString(o)) {
			return getValue().compareTo(((VncString)o).getValue());
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
		VncString other = (VncString) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override 
	public String toString() {
		return "\"" + value + "\"";
	}
	
	public String toString(final boolean print_readably) {
		if (print_readably) {
			return "\"" + StringUtil.escape(value) + "\"";
		} 
		else {
			return value;
		}
	}
	

    private static final long serialVersionUID = -1848883965231344442L;

	private final String value;
}