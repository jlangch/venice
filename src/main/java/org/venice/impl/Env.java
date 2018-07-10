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
package org.venice.impl;

import java.util.HashMap;
import java.util.Map;

import org.venice.VncException;
import org.venice.impl.types.VncSymbol;
import org.venice.impl.types.VncVal;


public class Env {
	
	public Env() {
		this.outer = null;
		this.level = 0;
	}

	public Env(final Env outer) {
		this.outer = outer;
		this.level = outer == null ? 0 : outer.level() + 1;
	}
		
	public Env find(final VncSymbol key) {
		if (data.containsKey(key.getName())) {
			return this;
		} 
		else if (outer != null) {
			return outer.find(key);
		} 
		else {
			return null;
		}
	}

	public VncVal get(final VncSymbol key) {
		final Env e = find(key);
		if (e == null) {
			throw new VncException("Symbol '" + key.getName() + "' not found");
		} 
		else {
			return e.data.get(key.getName());
		}
	}

	public int level() {
		return level;
	}

	public Env set(final VncSymbol key, final VncVal value) {
		data.put(key.getName(), value);
		return this;
	}
	
	@Override
	public String toString() {
		return String.format("%d: %s", level, data);
	}
	
	
	private final Env outer;
	private final int level;
	private final Map<String,VncVal> data = new HashMap<>();
}
