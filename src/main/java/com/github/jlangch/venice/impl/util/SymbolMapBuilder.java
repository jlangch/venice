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
package com.github.jlangch.venice.impl.util;

import java.util.HashMap;
import java.util.Map;

import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncSpecialForm;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;


public class SymbolMapBuilder {
	
	public SymbolMapBuilder() {
	}

	public SymbolMapBuilder add(final VncFunction fn) {
		if (fn.isPrivate()) {
			map.put(new VncSymbol(
							fn.getQualifiedName(), 
							VncHashMap.of(MetaUtil.PRIVATE, VncBoolean.True)),
					fn);
		}
		else {
			map.put(new VncSymbol(fn.getQualifiedName()), fn);
		}
		return this;
	}
	
	public SymbolMapBuilder add(final VncSpecialForm sf) {
		map.put(new VncSymbol(sf.getName()), sf);
		return this;
	}

	public SymbolMapBuilder put(final VncSymbol key, final VncVal val) {
		map.put(key, val);
		return this;
	}

	
	public VncHashMap build() {
		return new VncHashMap(map);
	}
	
	public Map<VncVal,VncVal> toMap() {
		return map;
	}
	
	
	private HashMap<VncVal,VncVal> map = new HashMap<>();
}
