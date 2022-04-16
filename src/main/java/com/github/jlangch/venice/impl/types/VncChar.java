/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2022 Venice
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

import com.github.jlangch.venice.impl.types.custom.VncWrappingTypeDef;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.MetaUtil;


public class VncChar extends VncVal {
	
	public VncChar(final char v) { 
		this(Character.valueOf(v), null); 
	}

	public VncChar(final Character v) { 
		this(v, null); 
	}

	public VncChar(
			final Character v, 
			final VncWrappingTypeDef wrappingTypeDef
	) { 
		super(wrappingTypeDef, Constants.Nil);
		value = v; 
	}

	public Character getValue() { 
		return value; 
	}
	
	
	@Override
	public VncChar withMeta(final VncVal meta) {
		return this; 
	}
	
	@Override
	public VncChar wrap(final VncWrappingTypeDef wrappingTypeDef, final VncVal meta) {
		return new VncChar(value, wrappingTypeDef); 
	}
	
	@Override
	public VncKeyword getType() {
		return isWrapped() ? new VncKeyword(
									getWrappingTypeDef().getType().getQualifiedName(),
									MetaUtil.typeMeta(
										new VncKeyword(VncChar.TYPE), 
										new VncKeyword(VncVal.TYPE)))
						   : new VncKeyword(
									VncChar.TYPE, 
									MetaUtil.typeMeta(
										new VncKeyword(VncVal.TYPE)));
	}
	
	@Override 
	public TypeRank typeRank() {
		return TypeRank.CHAR;
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
			return value.equals(((VncChar)obj).value);
		}
	}

	@Override 
	public String toString() {
		return value.toString();
	}
	
	public String toString(final boolean print_readably) {
		if (print_readably) {
			final char ch = value.charValue();
			if (ch > 32 && ch < 256) {
				return "#\\" + ch;
			}
			else {
				return String.format("#\\u%04x", (int)ch);
			}
		}
		else {
			return value.toString();
		}
	}

	
    public static final String TYPE = ":core/char";

    private static final long serialVersionUID = -1848883965231344442L;

	private final Character value;
}