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
package com.github.jlangch.venice.impl;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;


public class Env {
	
	public Env() {
		this(null);
	}

	public Env(final Env outer) {
		this.outer = outer;
		this.level = outer == null ? 0 : outer.level() + 1;
		this.globalSymbols = outer == null ? new Symbols() : outer.globalSymbols;
	}
		
	public Env findEnv(final VncSymbol key) {
		if (globalSymbols.contains(key) || symbols.contains(key)) {
			return this;
		} 
		else if (outer != null) {
			return outer.findEnv(key);
		} 
		else {
			return null;
		}
	}

	public VncVal get(final VncSymbol key) {
		if (globalSymbols.contains(key)) {
			return globalSymbols.get(key);
		}
		else {		
			final Env e = findEnv(key);
			if (e == null) {
				throw new VncException("Symbol '" + key.getName() + "' not found");
			} 
			else {
				return e.symbols.get(key);
			}
		}
	}

	public int level() {
		return level;
	}

	public Env set(final VncSymbol key, final VncVal value) {
		symbols.set(key, value);
		return this;
	}

	public Env setGlobal(final VncSymbol key, final VncVal value) {
		globalSymbols.set(key, value);
		return this;
	}
	
	@Override
	public String toString() {
		return String.format("global: %s\n\nlevel %d: %s", globalSymbols, level, symbols);
	}
	
	
	private final Env outer;
	private final int level;
	private final Symbols globalSymbols;
	private final Symbols symbols = new Symbols();
}
