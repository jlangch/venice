/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2019 Venice
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

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.util.CallFrameBuilder;
import com.github.jlangch.venice.impl.util.ThreadLocalMap;


public class Env implements Serializable {
	
	public Env() {
		this(null);
	}

	public Env(final Env outer) {
		this.outer = outer;
		this.level = outer == null ? 0 : outer.level() + 1;
		this.globalSymbols = outer == null ? new ConcurrentHashMap<>() : outer.globalSymbols;
		this.symbols = new ConcurrentHashMap<>();
	}

	public VncVal get(final VncSymbol key) {
		final VncVal val = getOrNull(key);
		if (val != null) {
			return val;
		}
		
		try {
			ThreadLocalMap.getCallStack().push(CallFrameBuilder.fromVal(key));
			throw new VncException(String.format("Symbol '%s' not found.",  key.getName()));
		}
		finally {
			ThreadLocalMap.getCallStack().pop();
		}
	}

	public VncVal getOrNil(final VncSymbol key) {
		final VncVal val = getOrNull(key);
		return val == null ? Nil : val;
	}

	public VncVal getGlobalOrNil(final VncSymbol key) {
		final Var glob = globalSymbols.get(key);
		return glob == null ? Nil : glob.getVal();
	}

	public int level() {
		return level;
	}

	public Env set(final VncSymbol name, final VncVal val) {
		symbols.put(name, new Var(name, val));
		return this;
	}
	
	public Env addAll(final List<Binding> bindings) {
		for(Binding b : bindings) {
			symbols.put(b.sym, new Var(b.sym, b.val));
		}
		return this;
	}

	public Env setGlobal(final Var val) {
		final Var v = globalSymbols.get(val.getName());
		if (v != null && !v.isOverwritable()) {
			try {
				ThreadLocalMap.getCallStack().push(CallFrameBuilder.fromVal(val.getName()));
				throw new VncException(String.format(
						"The existing global var %s must not be overwritten!",
						val.getName()));
			}
			finally {
				ThreadLocalMap.getCallStack().pop();
			}
		}
		
		globalSymbols.put(val.getName(), val);
		return this;
	}

	public Env pushGlobalDynamic(final Var val) {
		final Var dv = globalSymbols.get(val.getName());
		if (dv != null) {
			if (dv instanceof DynamicVar) {
				((DynamicVar)dv).pushVal(val.getVal());
			}
			else {
				try {
					ThreadLocalMap.getCallStack().push(CallFrameBuilder.fromVal(val.getName()));
					throw new VncException(String.format(
							"The var %s is not defined as dynamic",
							val.getName()));
				}
				finally {
					ThreadLocalMap.getCallStack().pop();
				}
			}
		}
		else {
			final DynamicVar nv = new DynamicVar(val.getName(), Nil);
			globalSymbols.put(val.getName(), nv);
			nv.pushVal(val.getVal());
		}
		return this;
	}

	public VncVal popGlobalDynamic(final VncSymbol sym) {
		final Var dv = globalSymbols.get(sym);
		if (dv != null) {
			if (dv instanceof DynamicVar) {
				return ((DynamicVar)dv).popVal();
			}
			else {
				try {
					ThreadLocalMap.getCallStack().push(CallFrameBuilder.fromVal(sym));
					throw new VncException(String.format(
							"The var %s is not defined as dynamic",
							sym.getName()));
				}
				finally {
					ThreadLocalMap.getCallStack().pop();
				}
			}
		}
		else {
			return Nil;
		}
	}

	public VncVal peekGlobalDynamic(final VncSymbol sym) {
		final Var dv = globalSymbols.get(sym);
		if (dv != null) {
			if (dv instanceof DynamicVar) {
				return ((DynamicVar)dv).peekVal();
			}
			else {
				try {
					ThreadLocalMap.getCallStack().push(CallFrameBuilder.fromVal(sym));
					throw new VncException(String.format(
							"The var %s is not defined as dynamic",
							sym.getName()));
				}
				finally {
					ThreadLocalMap.getCallStack().pop();
				}
			}
		}
		else {
			return Nil;
		}
	}

	public boolean hasGlobalSymbol(final VncSymbol key) {
		return globalSymbols.containsKey(key);
	}

	public Env getRootEnv() {
		Env env = this;
		while(env.outer != null) env = env.outer;
		return env;
	}
	
	@Override
	public String toString() {
		return String.format("level %d: %s\n\nglobal: %s", level, symbols, globalSymbols);
	}
	

	private VncVal getOrNull(final VncSymbol key) {
		final Env e = findEnv(key);
		if (e == null) {
			final Var glob = globalSymbols.get(key);
			if (glob != null) {
				return glob.getVal();
			}
		}
		else {
			final Var loc = e.symbols.get(key);
			if (loc != null) {
				return loc.getVal();
			}
		}
		
		return null;
	}
	
	private Env findEnv(final VncSymbol key) {		
		if (symbols.containsKey(key)) {
			return this;
		} 
		else if (outer != null) {
			return outer.findEnv(key);
		} 
		else {
			return null;
		}
	}
	
	
	private static final long serialVersionUID = 9002640180394221858L;

	private final Env outer;
	private final int level;
	private final Map<VncSymbol,Var> globalSymbols;
	private final Map<VncSymbol,Var> symbols;
}

