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
import static com.github.jlangch.venice.impl.types.Constants.Nil;

public class VncConstant extends VncVal {

	public VncConstant(final String name) { 
		value = name; 
	}

	@Override 
	public int compareTo(final VncVal o) {
		if (this == Nil) {
			return o == Nil ? 0 : -1;
		}
		else if (o == Nil) {
			return 1;
		}
		else if (Types.isVncBoolean(this) && Types.isVncBoolean(o)) {
			return Long.valueOf(this == False ? 0L : 1L).compareTo(o == False ? 0L : 1L);				
		}
		else {
			return 0;
		}
	}

	public VncConstant copy() { 
		final VncConstant v = new VncConstant(value); 
		v.setMeta(getMeta());
		return v;
	}

	public String getValue() { 
		return value; 
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
		VncConstant other = (VncConstant) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override 
	public String toString() { 
		return value; 
	}
   

    private static final long serialVersionUID = -1848883965231344442L;

	private final String value;
}