/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2020 Venice
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
package com.github.jlangch.venice.impl;

import java.util.List;

import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;


public class Binding {

	public Binding(final VncSymbol sym, final VncVal val) {
		this.sym = sym;
		this.val = val;
	}

	
	public static Binding findBinding(final VncSymbol sym, final List<Binding> bindings) {
		final int idx = getBindingIndex(sym, bindings);
		return idx < 0 ? null : bindings.get(idx);
	}
	
	public static int getBindingIndex(final VncSymbol sym, final List<Binding> bindings) {
		for(int ii=0; ii<bindings.size(); ii++) {
			final Binding b = bindings.get(ii);
			if (b.sym.equals(sym)) {
				return ii;
			}
		}
		return -1;
	}
	
	@Override
	public String toString() {
		return sym.toString(true) + " -> " + val.toString(true);
	}


	public final VncSymbol sym;
	public final VncVal val;
}
