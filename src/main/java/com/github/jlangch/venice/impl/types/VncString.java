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

import java.util.ArrayList;
import java.util.List;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.custom.VncWrappingTypeDef;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.MetaUtil;
import com.github.jlangch.venice.impl.util.StringUtil;


public class VncString extends VncVal {
	
	public VncString(final String v) { 
		super(null, Constants.Nil);
		value = (v == null) ? "" : v; 
	}

	public VncString(final String v, final VncVal meta) { 
		super(null, meta);
		value = (v == null) ? "" : v; 
	}

	public VncString(
			final String v, 
			final VncWrappingTypeDef wrappingTypeDef, 
			final VncVal meta
	) { 
		super(wrappingTypeDef, meta);
		value = (v == null) ? "" : v; 
	}

	
	public String getValue() { 
		return value; 
	}
	
	@Override
	public VncString withMeta(final VncVal meta) {
		return new VncString(value, getWrappingTypeDef(), meta); 
	}
	
	@Override
	public VncString wrap(final VncWrappingTypeDef wrappingTypeDef, final VncVal meta) {
		return new VncString(value, wrappingTypeDef, meta); 
	}

	@Override
	public VncKeyword getType() {
		return isWrapped() ? new VncKeyword(
									getWrappingTypeDef().getType().getQualifiedName(),
									MetaUtil.typeMeta(
										new VncKeyword(VncString.TYPE), 
										new VncKeyword(VncVal.TYPE)))
						   : new VncKeyword(
									VncString.TYPE, 
									MetaUtil.typeMeta(
										new VncKeyword(VncVal.TYPE)));
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

		return new VncChar(value.charAt(idx));
	}

	public VncVal nthOrDefault(final int idx, final VncVal defaultVal) {
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
		final List<VncVal> list = new ArrayList<>();
		for(char c : value.toCharArray()) {
			list.add(new VncChar(c));
		}
		return VncList.ofList(list);
	}

	public VncSymbol toSymbol() {
		return new VncSymbol(getValue());
	}
	
	@Override 
	public TypeRank typeRank() {
		return TypeRank.STRING;
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
		else if (Types.isVncString(o)) {
			return getValue().compareTo(((VncString)o).getValue());
		}
		else if (Types.isVncChar(o)) {
			return getValue().compareTo(((VncChar)o).getValue().toString());
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
			return value.equals(((VncString)obj).value);
		}
	}

	@Override 
	public String toString() {
		return value;
	}
	
	public String toString(final boolean print_machine_readably) {
		if (print_machine_readably) {
			return StringUtil.quote(StringUtil.escape(value), '"');
		} 
		else {
			return value;
		}
	}
	
	public static VncString empty() {
		return EMPTY;
	}
	
    
    public static final VncString EMPTY = new VncString("");
    
    public static final String TYPE = ":core/string";

    
    private static final long serialVersionUID = -1848883965231344442L;

	private final String value;
}