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
package com.github.jlangch.venice.impl.types.collections;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.TypeRank;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;


public class VncMapEntry extends VncVal {

	public VncMapEntry(final VncVal key, final VncVal val) {
		super(Constants.Nil);
		this.key = key;
		this.val = val;
	}
	
	public VncMapEntry(final Map.Entry<VncVal, VncVal> entry) {
		super(Constants.Nil);
		this.key = entry.getKey();
		this.val = entry.getValue();
	}
	
	public VncVal getKey() {
		return key;
	}

	public VncVal getValue() {
		return val;
	}

	public VncVector toVector() {
		return VncVector.of(key, val);
	}

	@Override
	public VncMapEntry withMeta(final VncVal meta) {
		return this;
	}
	
	@Override
	public VncKeyword getType() {
		return TYPE;
	}

	@Override
	public List<VncKeyword> getAllSupertypes() {
		return Arrays.asList(VncVal.TYPE);
	}
	
	@Override 
	public TypeRank typeRank() {
		return TypeRank.MAPENTRY;
	}

	@Override
	public Object convertToJavaObject() {
		return Arrays.asList(
				key.convertToJavaObject(),
				val.convertToJavaObject());
	}


	@Override 
	public String toString() {
		return toString(true);
	}
	
	@Override
	public String toString(final boolean print_readably) {
		return String.format(
				"[%s %s]", 
				key.toString(print_readably),
				val.toString(print_readably));
	}

	
    public static final VncKeyword TYPE = new VncKeyword(":core/map-entry");

    private static final long serialVersionUID = 7943559441888855596L;
	
	private final VncVal key;
	private final VncVal val;
}
