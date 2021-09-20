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

import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.custom.VncWrappingTypeDef;


public abstract class VncCollection extends VncVal {
	
	public VncCollection(VncVal meta) {
		super(meta);
	}
	
	public VncCollection(
			final VncWrappingTypeDef wrappingTypeDef,
			final VncVal meta
	) {
		super(wrappingTypeDef, meta);
	}


	@Override
	public abstract VncCollection withMeta(VncVal meta);
	
	@Override
	public VncKeyword getType() {
		return TYPE;
	}
	
	@Override
	public List<VncKeyword> getSupertypes() {
		return Arrays.asList(VncVal.TYPE);
	}
	
	
	public abstract VncCollection emptyWithMeta();
		
	public abstract VncList toVncList();
	
	public abstract VncVector toVncVector();

	public abstract int size();
	
	public abstract boolean isEmpty();
	
	
	public static final VncKeyword TYPE = new VncKeyword(":core/collection");

    private static final long serialVersionUID = -1848883965231344442L;
}