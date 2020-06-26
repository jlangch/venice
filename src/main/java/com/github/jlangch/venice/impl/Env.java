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

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
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
import com.github.jlangch.venice.util.NullInputStream;
import com.github.jlangch.venice.util.NullOutputStream;


public class Env implements Serializable {
	
	public Env() {
		this((Env)null);
	}

	public Env(final Env outer) {
		this.outer = outer;
		this.level = outer == null ? 0 : outer.level() + 1;
		this.precompiledGlobalSymbols = outer == null ? null : outer.precompiledGlobalSymbols; 
		this.globalSymbols = outer == null ? new ConcurrentHashMap<>() : outer.globalSymbols;
		this.localSymbols = new ConcurrentHashMap<>();
	}
	
	private Env(final Map<VncSymbol,Var> precompiledGlobalSymbols) {
		this.outer = null;
		this.level = 0;
		this.precompiledGlobalSymbols = precompiledGlobalSymbols; 
		this.globalSymbols = new ConcurrentHashMap<>();
		this.localSymbols = new ConcurrentHashMap<>();
	}

	public Env copyGlobalToPrecompiledSymbols() {
		// Used for precompiled scripts. 
		// Move the global symbols to core global symbols so they remain untouched
		// while running the precompiled script and thus can be reused by subsequent
		// precompiled script invocations
		return new Env(globalSymbols);
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
	 * @param sym a symbol
	 * @return the value
	 * @throws VncException if the symbol does not exist.
	 */
	public VncVal get(final VncSymbol sym) {
		final VncVal val = getOrElse(sym, null);
		if (val != null) return val;

		try (WithCallStack cs = new WithCallStack(CallFrame.fromVal(sym))) {
			throw new VncException(String.format("Symbol '%s' not found.", sym.getName())); 
		}
	}

	/**
	 * Checks if a symbol is bound to a value
	 *
	 * @param sym a symbol
	 * @return returns true if a symbol is bound to a value else false
	 */
	public boolean isBound(final VncSymbol sym) {
		final String ns = Namespaces.getNamespace(sym.getName());
		if (ns != null) {
			// if we got a namespace it must be a global var
			return getGlobalVar(sym) != null;
		}
		else {
			final VncVal v = findLocalVar(sym);
			return v != null ? true : getGlobalVar(sym) != null;
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
	 * @param sym a symbol
	 * @return the value or <code>Nil</code> if not found
	 */
	public VncVal getOrNil(final VncSymbol sym) {
		return getOrElse(sym, Nil);
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
	 * @param sym a symbol
	 * @return the value or <code>Nil</code> if not found
	 */
	public VncVal getGlobalOrNil(final VncSymbol sym) {
		final Var v = getGlobalVar(sym);
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
	 * @param sym a symbol
	 * @return the value or <code>null</code> if not found
	 */
	public VncVal getGlobalOrNull(final VncSymbol sym) {
		final Var v = getGlobalVar(sym);
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
	 * @param sym a symbol
	 * @return the value or <code>null</code> if not found
	 */
	public Var getGlobalVarOrNull(final VncSymbol sym) {
		return getGlobalVar(sym);
	}

	public int level() {
		return level;
	}

	public Env setLocal(final VncSymbol sym, final VncVal val) {
		if (sym.getName().equals(Namespaces.NS_CURRENT_NAME)) {
			throw new VncException(String.format("Internal error setting var %s", sym.getName()));
		}

		if (failOnShadowingGlobalVars) {
			final Var v = getGlobalVar(sym);
	
			// check shadowing of a global non function var by a local var
			//
			// e.g.:   (do (defonce x 1) (let [x 10 y 20] (+ x y)))
			//         (let [+ 10] (core/+ + 20))
			if (v != null && !v.isOverwritable() && Types.isVncFunction(v.getVal())) {
				try (WithCallStack cs = new WithCallStack(CallFrame.fromVal(sym))) {
					throw new VncException(String.format(
								"The global var '%s' must not be shadowed by a local var!", 
								sym));
				}
			}
		}
		
		setLocalVar(sym, new Var(sym, val));
		
		return this;
	}

	public Env setGlobal(final Var val) {
		if (val.getName().equals(Namespaces.NS_CURRENT_SYMBOL)) {
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

	public Env addGlobalVars(final List<Var> vars) {
		if (vars != null) {
			vars.forEach(v -> setGlobal(v));
		}

		return this;
	}

	public void addLocalBindings(final List<Binding> bindings) {
		for(Binding b : bindings) {
			setLocal(b.sym, b.val);
		}
	}

	public void pushGlobalDynamic(final VncSymbol sym, final VncVal val) {
		final DynamicVar dv = findGlobalDynamicVar(sym);
		if (dv != null) {
			dv.pushVal(val);
		}
		else {
			final DynamicVar nv = new DynamicVar(sym, Nil);
			setGlobalVar(sym, nv);
			nv.pushVal(val);
		}
	}

	public VncVal popGlobalDynamic(final VncSymbol sym) {
		final DynamicVar dv = findGlobalDynamicVar(sym);
		return dv != null ? dv.popVal() : Nil;
	}

	public VncVal peekGlobalDynamic(final VncSymbol sym) {
		final DynamicVar dv = findGlobalDynamicVar(sym);
		return dv != null ? dv.peekVal() : Nil;
	}

	public void setGlobalDynamic(final VncSymbol sym, final VncVal val) {
		final DynamicVar dv = findGlobalDynamicVar(sym);
		if (dv != null) {
			dv.setVal(val);
		}
		else {
			final DynamicVar nv = new DynamicVar(sym, Nil);
			setGlobalVar(sym, nv);
			nv.pushVal(val);
		}
	}

	public void replaceGlobalDynamic(final VncSymbol sym, final VncVal val) {
		final DynamicVar nv = new DynamicVar(sym, Nil);
		setGlobalVar(sym, nv);
		nv.pushVal(val);
	}

	public void removeGlobalSymbol(final VncSymbol sym) {
		if (precompiledGlobalSymbols != null) {
			precompiledGlobalSymbols.remove(sym);
		}		
		globalSymbols.remove(sym);
	}
	
	public void removeGlobalSymbolsByNS(final VncSymbol ns) {
		final String nsName = ns.getName();
		
		if (Namespaces.isCoreNS(nsName)) {
			return;
		}
		
		if (precompiledGlobalSymbols != null) {
			precompiledGlobalSymbols
				.keySet()
				.stream()
				.filter(s -> nsName.equals(Namespaces.getNamespace(s.getName())))
				.forEach(s -> precompiledGlobalSymbols.remove(s));
		}	

		globalSymbols
			.keySet()
			.stream()
			.filter(s -> nsName.equals(Namespaces.getNamespace(s.getName())))
			.forEach(s -> globalSymbols.remove(s));
	}

	public Env getLevelEnv(final int level) {
		Env env = this;
		if (env.level == level) {
			return env;
		}
		else {
			while (env.outer != null) {
				env = env.outer;
				if (env.level == level) {
					return env;
				}
			}
		}
		
		throw new VncException(String.format("No env level %d", level));
	}
	
	public int globalsCount() {
		if (precompiledGlobalSymbols != null) {
			return precompiledGlobalSymbols.size();
		}
		else {
			return globalSymbols.size();
		}
	}
	
	@Override
	public String toString() {
		return new StringBuilder()
					.append("level ").append(level).append(":")
					.append("\n   [local]\n").append(toString(localSymbols, "      "))
					.append("\n   [global]\n").append(toString(getAllGlobalSymbols(), "      "))
					.toString();
	}
		
	public Env setStdoutPrintStream(final PrintStream ps) {
		replaceGlobalDynamic(
				new VncSymbol("*out*"), 
				VncJavaObject.from(
						ps != null ? ps : nullPrintStream(),
						PrintStream.class));
		
		return this;
	}
	
	public Env setStderrPrintStream(final PrintStream ps) {
		replaceGlobalDynamic(
				new VncSymbol("*err*"), 
				VncJavaObject.from(
						ps != null ? ps : nullPrintStream(),
						PrintStream.class));
		
		return this;
	}

	public Env setStdinReader(final java.io.Reader rd) {
		final VncSymbol sym = new VncSymbol("*in*");
		
		if (rd == null) {
			replaceGlobalDynamic(
					sym, 
					VncJavaObject.from(nullBufferedReader(), Reader.class));
		}
		else if (rd instanceof BufferedReader) {
			replaceGlobalDynamic(
					sym, 
					VncJavaObject.from(rd, Reader.class));
		}
		else {
			replaceGlobalDynamic(
					sym,
					VncJavaObject.from(new BufferedReader(rd), Reader.class));
		}
		
		return this;
	}

	private String toString(
			final Map<VncSymbol,Var> vars, 
			final String indent
	) {
		return vars.values()
				   .stream()
				   .sorted((a,b) -> a.getName().getName().compareTo(b.getName().getName()))
				   .map(v -> String.format(
								"%s%s (:%s)", 
								indent,
								v.getName().getName(),
								Types.getType(v.getVal()).getValue()))
				   .collect(Collectors.joining("\n"));
	}
	
	private DynamicVar findGlobalDynamicVar(final VncSymbol sym) {
		if (sym.equals(Namespaces.NS_CURRENT_SYMBOL)) {
			throw new VncException(String.format("%s can not be used as a dynamic var", sym.getName()));
		}

		final Var dv = getGlobalVar(sym);
		if (dv != null) {
			if (dv instanceof DynamicVar) {
				return (DynamicVar)dv;
			}
			else {
				try (WithCallStack cs = new WithCallStack(CallFrame.fromVal(sym))) {
					throw new VncException(String.format("The var '%s' is not defined as dynamic", sym.getName()));
				}
			}
		}
		return null;
	}
	
	private VncVal getOrElse(final VncSymbol sym, final VncVal defaultVal) {
		final String ns = sym.getNamespace();
		if (ns != null) {
			// if we got a namespace it must be a global var
			final Var glob = getGlobalVar(sym);
			return glob == null ? defaultVal : glob.getVal();
		}
		else {
			final VncVal local = findLocalVar(sym);
			if (local != null) {
				return local;
			}
			else {
				final Var glob = getGlobalVar(sym);
				return glob == null ? defaultVal : glob.getVal();
			}
		}
	}
	
	private VncVal findLocalVar(final VncSymbol sym) {
		Env env = this;	
		while(env != null) {
			final Var v = env.localSymbols.get(sym);
			if (v != null) return v.getVal();
			env = env.outer;
		}
		
		return null;
	}
	
	private Var getGlobalVar(final VncSymbol sym) {
		final String name = sym.getName();

		if (name.equals(Namespaces.NS_CURRENT_NAME)) {
			return new Var(Namespaces.NS_CURRENT_SYMBOL, Namespaces.getCurrentNS());
		}

		if (ReservedSymbols.isSpecialForm(name)) {
			return null;
		}
		
		// validatePrivateSymbolAccess(sym);
		
		final boolean qualified = Namespaces.isQualified(name);
		
		if (qualified && name.startsWith("core/")) {
			return getGlobalVarRaw(new VncSymbol(name.substring(5)));
		}
		else {
			if (!qualified) {
				final VncSymbol ns = Namespaces.getCurrentNS();
				if (!Namespaces.isCoreNS(ns)) {
					final VncSymbol qualifiedKey = new VncSymbol(ns.getName() + "/" + name);
					final Var v = getGlobalVarRaw(qualifiedKey);
					if (v != null) return v;
				}
			}
	
			// symbol with namespace
			return getGlobalVarRaw(sym);
		}
	}

	private Var getGlobalVarRaw(final VncSymbol sym) {
		if (precompiledGlobalSymbols != null) {
			final Var v = precompiledGlobalSymbols.get(sym);
			if (v != null) return v;
		}
		
		return globalSymbols.get(sym);
	}

	private void setGlobalVar(final VncSymbol sym, final Var value) {
		globalSymbols.put(sym, value);
	}

	private void setLocalVar(final VncSymbol sym, final Var value) {
		localSymbols.put(sym, value);
	}
	
	public Map<VncSymbol,Var> getAllGlobalSymbols() {
		final Map<VncSymbol,Var> all = new HashMap<>();
		
		if (precompiledGlobalSymbols != null) {
			all.putAll(precompiledGlobalSymbols);
		}
		
		all.putAll(globalSymbols);
		
		all.put(
			Namespaces.NS_CURRENT_SYMBOL, 
			new Var(Namespaces.NS_CURRENT_SYMBOL, Namespaces.getCurrentNS()));
		
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

	private BufferedReader nullBufferedReader() {
		return new BufferedReader(new InputStreamReader(new NullInputStream()));
	}

//	private void validatePrivateSymbolAccess(final VncSymbol sym) {
//		if (sym.isPrivate()) {
//			final VncSymbol currNS = Namespaces.getCurrentNS();
//			final String symNS = Namespaces.getNamespace(sym.getName());
//			if (!Namespaces.getNamespace(currNS.getName()).equals(symNS)) {
//				final CallStack callStack = ThreadLocalMap.getCallStack();
//				final CallFrame callFrame = callStack.peek();
//				
//				try (WithCallStack cs = new WithCallStack(callFrame)) {
//					throw new VncException(String.format(
//							"Illegal access of private symbol %s. Called by %s.\n%s", 
//							sym.getName(),
//							callFrame.getFnName(),
//							callStack.toString()));
//				}				
//			}
//		}	
//	}
	
	
	private static final long serialVersionUID = 9002640180394221858L;
	
	// Note: Clojure allows shadowing global vars by local vars
	private boolean failOnShadowingGlobalVars = false; 

	private final Env outer;
	private final int level;
	private final Map<VncSymbol,Var> precompiledGlobalSymbols;
	private final Map<VncSymbol,Var> globalSymbols;
	private final Map<VncSymbol,Var> localSymbols;
}
