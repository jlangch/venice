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

import java.io.PrintStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.CallFrame;
import com.github.jlangch.venice.impl.util.WithCallStack;
import com.github.jlangch.venice.util.NullOutputStream;


public class Env implements Serializable {
	
	public Env() {
		this((Env)null);
	}

	public Env(final Env outer) {
		this.outer = outer;
		this.level = outer == null ? 0 : outer.level() + 1;
		this.coreGlobalSymbols = outer == null ? null : outer.coreGlobalSymbols; 
		this.globalSymbols = outer == null ? new ConcurrentHashMap<>() : outer.globalSymbols;
		this.localSymbols = new ConcurrentHashMap<>();
	}
	
	private Env(final Map<VncSymbol,Var> coreGlobalSymbols) {
		this.outer = null;
		this.level = 0;
		this.coreGlobalSymbols = coreGlobalSymbols; 
		this.globalSymbols = new ConcurrentHashMap<>();
		this.localSymbols = new ConcurrentHashMap<>();
	}

	public Env makeCoreOnlyGlobalEnv() {
		// Used for precompiled scripts. 
		// Move the global symbols to core global symbols so they remain untouched
		// while running the precompiled script and thus can be reused by subsequent
		// precompiled script invocations
		return new Env(this.globalSymbols);
	}

	/**
	 * Look up a local or global symbol's value
	 * 
	 * <p>Unqualified symbol resolution:
	 * <ol>
	 *  <li>try to resolve the symbol from the local namespace</li>
	 *  <li>try to resolve the symbol from the global current namespace defined by *ns*</li>
	 *  <li>try to resolve the symbol from the global 'core' namespace</li>
	 * </ol>
	 * 
	 * <p>Qualified symbol resolution:
	 * <ol>
	 *  <li>qualified symbols are resolved exclusively from the global symbols</li>
	 * </ol>
	 * 
	 * @param key a symbol
	 * @return the value
	 * @throws VncException if the symbol does not exist.
	 */
	public VncVal get(final VncSymbol key) {
		final VncVal val = getOrNull(key);
		if (val != null) return val;

		try (WithCallStack cs = new WithCallStack(CallFrame.fromVal(key))) {
			throw new VncException(String.format("Symbol '%s' not found.", key.getName())); 
		}
	}

	/**
	 * Look up a local or global symbol's value
	 * 
	 * <p>Unqualified symbol resolution:
	 * <ol>
	 *  <li>try to resolve the symbol from the local namespace</li>
	 *  <li>try to resolve the symbol from the global current namespace defined by *ns*</li>
	 *  <li>try to resolve the symbol from the global 'core' namespace</li>
	 * </ol>
	 * 
	 * <p>Qualified symbol resolution:
	 * <ol>
	 *  <li>qualified symbols are resolved exclusively from the global symbols</li>
	 * </ol>
	 * 
	 * @param key a symbol
	 * @return the value or <code>Nil</code> if not found
	 */
	public VncVal getOrNil(final VncSymbol key) {
		final VncVal val = getOrNull(key);
		return val != null ? val : Nil;
	}

	/**
	 * Look up a global symbol' value
	 * 
	 * <p>Unqualified symbol resolution:
	 * <ol>
	 *  <li>try to resolve the symbol from the global current namespace defined by *ns*</li>
	 *  <li>try to resolve the symbol from the global 'core' namespace</li>
	 * </ol>
	 * 
	 * <p>Qualified symbol resolution:
	 * <ol>
	 *  <li>try to resolve the symbol from the global namespace</li>
	 * </ol>
	 * 
	 * @param key a symbol
	 * @return the value or <code>Nil</code> if not found
	 */
	public VncVal getGlobalOrNil(final VncSymbol key) {
		final Var v = getGlobalVar(key);
		return v != null ? v.getVal() : Nil;
	}

	/**
	 * Look up a global symbol's value
	 * 
	 * <p>Unqualified symbol resolution:
	 * <ol>
	 *  <li>try to resolve the symbol from the global current namespace defined by *ns*</li>
	 *  <li>try to resolve the symbol from the global 'core' namespace</li>
	 * </ol>
	 * 
	 * <p>Qualified symbol resolution:
	 * <ol>
	 *  <li>try to resolve the symbol from the global namespace</li>
	 * </ol>
	 * 
	 * @param key a symbol
	 * @return the value or <code>null</code> if not found
	 */
	public VncVal getGlobalOrNull(final VncSymbol key) {
		final Var v = getGlobalVar(key);
		return v != null ? v.getVal() : null;
	}

	/**
	 * Look up a global symbol's var
	 * 
	 * <p>Unqualified symbol resolution:
	 * <ol>
	 *  <li>try to resolve the symbol from the global current namespace defined by *ns*</li>
	 *  <li>try to resolve the symbol from the global 'core' namespace</li>
	 * </ol>
	 * 
	 * <p>Qualified symbol resolution:
	 * <ol>
	 *  <li>try to resolve the symbol from the global namespace</li>
	 * </ol>
	 * 
	 * @param key a symbol
	 * @return the value or <code>null</code> if not found
	 */
	public Var getGlobalVarOrNull(final VncSymbol key) {
		return getGlobalVar(key);
	}

	public int level() {
		return level;
	}

	public Env set(final VncSymbol name, final VncVal val) {
		if (name.equals(Namespace.NS_SYMBOL_CURRENT)) {
			throw new VncException(String.format("Internal error setting var %s", name.getName()));
		}

		final Var v = getGlobalVar(name);

		// allow shadowing of a global non function var by a local var
		// e.g.:   (do (defonce x 1) (defonce y 3) (let [x 10 y 20] (+ x y)))
		if (v != null && !v.isOverwritable() && Types.isVncFunction(v.getVal())) {
			try (WithCallStack cs = new WithCallStack(CallFrame.fromVal(name))) {
				throw new VncException(String.format(
							"The global function '%s' must not be shadowed by a local var!", 
							name));
			}
		}

		setLocalVar(name, new Var(name, val));
		return this;
	}
	
	public Env addAll(final List<Binding> bindings) {
		for(Binding b : bindings) {
			set(b.sym, b.val);
		}
		return this;
	}

	public Env setGlobal(final Var val) {
		if (val.getName().equals(Namespace.NS_SYMBOL_CURRENT)) {
			throw new VncException(String.format("Internal error setting var %s", val.getName().getName()));
		}

		final Var v = getGlobalVar(val.getName());
		if (v != null && !v.isOverwritable()) {
			try (WithCallStack cs = new WithCallStack(CallFrame.fromVal(val.getName()))) {
				throw new VncException(String.format(
							"The existing global var '%s' must not be overwritten!", 
							val.getName()));
			}
		}
		
		setGlobalVar(val.getName(), val);

		return this;
	}

	public Env pushGlobalDynamic(final VncSymbol sym, final VncVal val) {
		final Var dv = getGlobalVar(sym);
		if (dv != null) {
			if (dv instanceof DynamicVar) {
				((DynamicVar)dv).pushVal(val);
			}
			else {
				try (WithCallStack cs = new WithCallStack(CallFrame.fromVal(sym))) {
					throw new VncException(String.format("The var '%s' is not defined as dynamic", sym));
				}
			}
		}
		else {
			final DynamicVar nv = new DynamicVar(sym, Nil);
			setGlobalVar(sym, nv);
			nv.pushVal(val);
		}
		return this;
	}

	public VncVal popGlobalDynamic(final VncSymbol sym) {
		final Var dv = getGlobalVar(sym);
		if (dv != null) {
			if (dv instanceof DynamicVar) {
				return ((DynamicVar)dv).popVal();
			}
			else {
				try (WithCallStack cs = new WithCallStack(CallFrame.fromVal(sym))) {
					throw new VncException(String.format("The var '%s' is not defined as dynamic", sym.getName()));
				}
			}
		}
		
		return Nil;
	}

	public VncVal peekGlobalDynamic(final VncSymbol sym) {
		final Var dv = getGlobalVar(sym);
		if (dv != null) {
			if (dv instanceof DynamicVar) {
				return ((DynamicVar)dv).peekVal();
			}
			else {
				try (WithCallStack cs = new WithCallStack(CallFrame.fromVal(sym))) {
					throw new VncException(String.format("The var '%s' is not defined as dynamic", sym.getName()));
				}
			}
		}
		return Nil;
	}

	public Env setGlobalDynamic(final VncSymbol sym, final VncVal val) {
		if (sym.equals(Namespace.NS_SYMBOL_CURRENT)) {
			throw new VncException(String.format("Internal error setting var %s", sym.getName()));
		}

		final Var dv = getGlobalVar(sym);
		if (dv != null) {
			if (dv instanceof DynamicVar) {
				((DynamicVar)dv).setVal(val);
			}
			else {
				try (WithCallStack cs = new WithCallStack(CallFrame.fromVal(sym))) {
					throw new VncException(String.format("The var '%s' is not defined as dynamic", sym));
				}
			}
		}
		else {
			final DynamicVar nv = new DynamicVar(sym, Nil);
			setGlobalVar(sym, nv);
			nv.pushVal(val);
		}
		return this;
	}

	public void removeGlobalSymbol(final VncSymbol key) {
		coreGlobalSymbols.remove(key);
		globalSymbols.remove(key);
	}

	public Env getLevelEnv(final int level) {
		Env env = this;
		if (env.level == level) {
			return env;
		}
		else {
			while(env.outer != null) {
				env = env.outer;
				if (env.level == level) {
					return env;
				}
			}
		}
		
		throw new VncException(String.format("No env level %d", level));
	}
	
	@Override
	public String toString() {
		return new StringBuilder()
					.append("level ").append(level).append(":")
					.append("\n   [local]\n").append(toString(localSymbols, "      ", null))
					.append("\n   [global]\n").append(toString(getAllGlobalSymbols(), "      ", null))
					.toString();
	}
	
	public String localsToString() {
		return new StringBuilder()
					.append("level ").append(level).append(":")
					.append("\n   [local]\n").append(toString(localSymbols, "      ", null))
					.toString();
	}
	
	public String globalsToString() {
		return new StringBuilder()
					.append("[global]\n").append(toString(getAllGlobalSymbols(), "   ", null))
					.toString();
	}
	
	public String globalsToString(final String regexFilter) {
		return new StringBuilder()
					.append("[global]\n").append(toString(getAllGlobalSymbols(), "   ", regexFilter))
					.toString();
	}
	
	public Env setStdoutPrintStream(final PrintStream ps) {
		final VncVal psVal = new VncJavaObject(ps != null ? ps : nullPrintStream());
		final DynamicVar var = new DynamicVar(new VncSymbol("*out*"), psVal);
		
		setGlobal(var);
		
		// push it to the current thread so running precompiled scripts see it
		var.pushVal(psVal);  
		
		return this;
	}
	
	private String toString(
			final Map<VncSymbol,Var> vars, 
			final String indent, final 
			String regexFilter
	) {
		return vars.values()
				   .stream()
				   .sorted((a,b) -> a.getName().getName().compareTo(b.getName().getName()))
				   .filter(v -> regexFilter == null ? true : v.getName().getName().matches(regexFilter))
				   .map(v -> String.format(
								"%s%s: %s", 
								indent,
								v.getName().getName(), 
								Printer.pr_str(v.getVal(), true)))
				   .collect(Collectors.joining("\n"));
	}
	
	private VncVal getOrNull(final VncSymbol key) {
		final Env e = findEnv(key);
		if (e == null) {
			final Var glob = getGlobalVar(key);
			if (glob != null) {
				return glob.getVal();
			}
		}
		else {
			final Var loc = e.getLocalVar(key);
			if (loc != null) {
				return loc.getVal();
			}
		}
		
		return null;
	}
	
	private Env findEnv(final VncSymbol key) {		
		if (hasLocalVar(key)) {
			return this;
		} 
		else if (outer != null) {
			return outer.findEnv(key);
		} 
		else {
			return null;
		}
	}
	
	private Var getGlobalVar(final VncSymbol key) {
		if (key.equals(Namespace.NS_SYMBOL_CURRENT)) {
			return new Var(Namespace.NS_SYMBOL_CURRENT, Namespace.getCurrentNS());
		}
		
		final Var v = getGlobalVarRaw(key);
		if (v != null) return v;
		
		if (Namespace.on() && !Namespace.isQualified(key)) {
			final VncSymbol ns = Namespace.getCurrentNS();
			if (!Namespace.NS_CORE.equals(ns)) {
				final VncSymbol qualifiedKey = new VncSymbol(ns.getName() + "/" + key.getName());
				return getGlobalVarRaw(qualifiedKey);
			}
		}
		
		return null;
	}

	private Var getGlobalVarRaw(final VncSymbol key) {
		if (coreGlobalSymbols != null) {
			final Var v = coreGlobalSymbols.get(key);
			if (v != null) return v;
		}
		
		return globalSymbols.get(key);
	}

	private void setGlobalVar(final VncSymbol key, final Var value) {
		globalSymbols.put(key, value);
	}

	private Var getLocalVar(final VncSymbol key) {
		return localSymbols.get(key);
	}

	private void setLocalVar(final VncSymbol key, final Var value) {
		localSymbols.put(key, value);
	}

	private boolean hasLocalVar(final VncSymbol key) {
		return localSymbols.containsKey(key);
	}
	
	private Map<VncSymbol,Var> getAllGlobalSymbols() {
		final Map<VncSymbol,Var> all = new HashMap<>();
		if (coreGlobalSymbols != null) {
			all.putAll(coreGlobalSymbols);
		}
		all.putAll(globalSymbols);
		all.put(Namespace.NS_SYMBOL_CURRENT, new Var(Namespace.NS_SYMBOL_CURRENT, Namespace.getCurrentNS()));
		return all;
	}

	public List<VncSymbol> getAllGlobalFunctionSymbols() {
		return getAllGlobalSymbols()
				.entrySet()
				.stream()
				.filter(e -> e.getValue().getVal() instanceof VncFunction)
				.map(e -> {
					final VncFunction fn = (VncFunction)e.getValue().getVal();
					return e.getKey()
							.withMeta(VncHashMap.of(
								new VncKeyword("group"), new VncString(fn.getNamespace()),
								new VncKeyword("arglists"), fn.getArgLists(),
								new VncKeyword("doc"), fn.getDoc()));
				 })
				.collect(Collectors.toList());
	}

	
	private PrintStream nullPrintStream() {
		return new PrintStream(new NullOutputStream(), true);
	}

	
	
	private static final long serialVersionUID = 9002640180394221858L;

	private final Env outer;
	private final int level;
	private final Map<VncSymbol,Var> coreGlobalSymbols;
	private final Map<VncSymbol,Var> globalSymbols;
	private final Map<VncSymbol,Var> localSymbols;
}
