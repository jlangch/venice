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

import java.util.function.Function;

import com.github.jlangch.venice.impl.CoreFunctions;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;


public class VncKeyword extends VncString implements Function<VncList, VncVal> {
	
	public VncKeyword(final String v) { 
		super(v.startsWith(":") ? v.substring(1): v); 
	}

	public VncVal apply(final VncList args) {
		CoreFunctions.assertArity("keyword", args, 1);
		
		final VncMap map = Coerce.toVncMap(args.nth(0));
		return map.get(this);
	}
	
	public VncKeyword copy() { 
		final VncKeyword v = new VncKeyword(getValue()); 
		v.setMeta(getMeta());
		return v;
	}

	public VncSymbol toSymbol() {
		return new VncSymbol(getValue());
	}

	@Override 
	public int compareTo(final VncVal o) {
		return Types.isVncKeyword(o) ? getValue().compareTo(((VncKeyword)o).getValue()) : 0;
	}

	@Override 
	public String toString() {
		return ":" + getValue();
	}
	
	public String toString(final boolean print_readably) {
		return toString();
	}
}