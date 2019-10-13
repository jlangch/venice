/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2019 Venice
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

import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.StringUtil;


public class VncChar extends VncVal {
	
	public VncChar(final Character v) { 
		this(v, Constants.Nil); 
	}
	
	public VncChar(final char v) { 
		this(v, Constants.Nil); 
	}

	public VncChar(final Character v, final VncVal meta) { 
		super(meta);
		value = v; 
	}

	public VncChar(final char v, final VncVal meta) { 
		super(meta);
		value = v; 
	}

	public Character getValue() { 
		return value; 
	}
	
	
	@Override
	public VncChar withMeta(final VncVal meta) {
		return new VncChar(value, meta); 
	}
	
	@Override 
	public int typeRank() {
		return 5;
	}
	
	@Override
	public Object convertToJavaObject() {
		return value;
	}

	@Override 
	public int compareTo(final VncVal o) {
		if (o == Constants.Nil) {
			return 1;
		}
		else if (Types.isVncChar(o)) {
			return getValue().compareTo(((VncChar)o).getValue());
		}
		else if (Types.isVncString(o)) {
			return getValue().toString().compareTo(((VncString)o).getValue());
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
		VncChar other = (VncChar) obj;
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
	
	public String toString(final boolean print_readably) {
		if (print_readably) {
			return "\"" + StringUtil.escape(value.toString()) + "\"";
		} 
		else {
			return value.toString();
		}
	}
	

    private static final long serialVersionUID = -1848883965231344442L;

	private final Character value;
}