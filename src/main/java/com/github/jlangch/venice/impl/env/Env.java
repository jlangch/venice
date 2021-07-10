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
package com.github.jlangch.venice.impl.env;

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

import com.github.jlangch.venice.SymbolNotFoundException;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Namespaces;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.concurrent.ThreadLocalMap;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.CallFrame;
import com.github.jlangch.venice.impl.util.CallStack;
import com.github.jlangch.venice.impl.util.WithCallStack;
import com.github.jlangch.venice.util.NullInputStream;
import com.github.jlangch.venice.util.NullOutputStream;


public class Env implements Serializable {
	
	public Env() {
		this((Env)null);
	}

	public Env(final Env outer) {
		if (outer == null) {
			this.outer = null;
			this.level = 0;
			this.precompiledGlobalSymbols = null; 
			this.globalSymbols = new ConcurrentHashMap<>(1024);
			this.localSymbols = new ConcurrentHashMap<>(64);
		}
		else {
			this.outer = outer;
			this.level = outer.level() + 1;
			this.precompiledGlobalSymbols = outer.precompiledGlobalSymbols;
			this.globalSymbols = outer.globalSymbols;
			this.localSymbols = new ConcurrentHashMap<>(64);
		}
	}
	
	private Env(final Map<VncSymbol,Var> precompiledGlobalSymbols) {
		this.outer = null;
		this.level = 0;
		this.precompiledGlobalSymbols = precompiledGlobalSymbols;
		this.globalSymbols = new ConcurrentHashMap<>(256);
		this.localSymbols = new ConcurrentHashMap<>(64);
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

		try (WithCallStack cs = new WithCallStack(new CallFrame(sym.getQualifiedName(), sym.getMeta()))) {
			throw new SymbolNotFoundException(
					String.format("Symbol '%s' not found.", sym.getQualifiedName()),
					sym.getQualifiedName()); 
		}
	}

	/**
	 * Checks if a symbol is global
	 *
	 * @param sym a symbol
	 * @return returns true if a symbol is global else false
	 */
	public boolean isGlobal(final VncSymbol sym) {
		final Var dv = getGlobalVar(sym);
		return dv != null && !(dv instanceof DynamicVar);
	}

	/**
	 * Checks if a symbol is thread local
	 *
	 * @param sym a symbol
	 * @return returns true if a symbol is thread local else false
	 */
	public boolean isThreadLocal(final VncSymbol sym) {
		final Var dv = getGlobalVar(sym);
		return dv != null && dv instanceof DynamicVar;
	}

	/**
	 * Checks if a symbol is local
	 *
	 * @param sym a symbol
	 * @return returns true if a symbol is local else false
	 */
	public boolean isLocal(final VncSymbol sym) {
		return sym.hasNamespace() ? false : findLocalVar(sym) != null;
	}

	/**
	 * Checks if a symbol is bound to a value
	 *
	 * @param sym a symbol
	 * @return returns true if a symbol is bound to a value else false
	 */
	public boolean isBound(final VncSymbol sym) {
		if (sym.hasNamespace()) {
			// if we got a namespace it must be a global var
			return getGlobalVar(sym) != null;
		}
		else {
			final Var v = findLocalVar(sym);
			return v != null ? true : getGlobalVar(sym) != null;
		}
	}

	/**
	 * Returns the symbol's namespace or null if the symbol is local
	 *
	 * @param sym a symbol
	 * @return returns the symbol'snamespace
	 */
	public String getNamespace(final VncSymbol sym) {
		if (sym.hasNamespace()) {
			return sym.getNamespace();
		}
		else {
			if (findLocalVar(sym) != null) {
				return null;
			}
			else {
				final String name = sym.getName();
				final VncSymbol ns = Namespaces.getCurrentNS();
				
				if (!Namespaces.isCoreNS(ns)) {
					final VncSymbol qualifiedKey = new VncSymbol(ns.getName(), name, Constants.Nil);
					final Var v = getGlobalVarRaw(qualifiedKey);
					if (v != null) {
						return Namespaces.getCurrentNS().getName();
					}
				}

				return Namespaces.NS_CORE.getName();
			}
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
	
	public Env setLocal(final Var localVar) {
		final VncSymbol sym = localVar.getName();
		
		if (sym.getName().equals(Namespaces.NS_CURRENT_NAME)) {
			throw new VncException(String.format("Internal error setting var %s", sym.getName()));
		}

		if (failOnShadowingGlobalVars) {
			final Var globVar = getGlobalVar(sym);
	
			// check shadowing of a global non function var by a local var
			//
			// e.g.:   (do (defonce x 1) (let [x 10 y 20] (+ x y)))
			//         (let [+ 10] (core/+ + 20))
			if (globVar != null && !globVar.isOverwritable() && Types.isVncFunction(globVar.getVal())) {
				try (WithCallStack cs = new WithCallStack(new CallFrame(sym.getQualifiedName(), sym.getMeta()))) {
					throw new VncException(String.format(
								"The global var '%s' must not be shadowed by a local var!", 
								sym.getQualifiedName()));
				}
			}
		}
		
		setLocalVar(sym, localVar);
		
		return this;
	}

	public Env setGlobal(final Var val) {
		final VncSymbol sym = val.getName();
		
		if (sym.equals(Namespaces.NS_CURRENT_SYMBOL)) {
			throw new VncException(String.format("Internal error setting var %s", sym.getName()));
		}

		if (ReservedSymbols.isSpecialForm(sym.getName())) {
			throw new VncException(String.format("Internal error setting var %s with name of a special form", sym.getName()));
		}

		final Var v = getGlobalVar(sym);
		if (v != null && !v.isOverwritable()) {
			try (WithCallStack cs = new WithCallStack(new CallFrame(sym.getQualifiedName(), sym.getMeta()))) {
				throw new VncException(String.format(
							"The existing global var '%s' must not be overwritten!", 
							sym.getQualifiedName()));
			}
		}
		
		setGlobalVar(sym, val);

		return this;
	}

	public Env addGlobalVars(final List<Var> vars) {
		vars.forEach(v -> setGlobal(v));
		return this;
	}

	public void addLocalVars(final List<Var> vars) {
		for(Var b : vars) setLocal(b);
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
		// Do not care about precompiledGlobalSymbols.
		// Only system namespaces like core, time, ... are part of the pre-compiled 
		// global symbols, and these namespaces are sealed anyway!
		// 
		// The calling VeniceInterpreter is preventing the removal of global
		// system namespace symbols!

		globalSymbols.remove(sym);
	}
	
	public void removeGlobalSymbolsByNS(final VncSymbol ns) {
		// Do not care about precompiledGlobalSymbols.
		// Only system namespaces like core, time, ... are part of the pre-compiled 
		// global symbols, and these namespaces are sealed anyway!
		// 
		// The calling VeniceInterpreter is preventing the removal of global
		// system namespaces!

		final String nsName = ns.getName();
		
		globalSymbols
			.keySet()
			.stream()
			.filter(s -> nsName.equals(s.getNamespace()))
			.forEach(s -> globalSymbols.remove(s));
	}

	public Env getEnvAtLevel(final int level) {
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
	
	public Env setMacroExpandOnLoad(final VncBoolean macroexpandOnLoad) {
		setGlobal(new Var(new VncSymbol("*macroexpand-on-load*"), 
						  macroexpandOnLoad, 
						  true));
		return this;
	}

	public Env setStdinReader(final Reader rd) {
		if (rd == null) {
			replaceGlobalDynamic(
					new VncSymbol("*in*"), 
					VncJavaObject.from(nullBufferedReader(), Reader.class));
		}
		else if (rd instanceof BufferedReader) {
			replaceGlobalDynamic(
					new VncSymbol("*in*"), 
					VncJavaObject.from(rd, Reader.class));
		}
		else {
			replaceGlobalDynamic(
					new VncSymbol("*in*"),
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
			throw new VncException(String.format(
						"%s can not be used as a dynamic var", 
						sym.getQualifiedName()));
		}

		final Var dv = getGlobalVar(sym);
		if (dv != null) {
			if (dv instanceof DynamicVar) {
				return (DynamicVar)dv;
			}
			else {
				try (WithCallStack cs = new WithCallStack(new CallFrame(sym.getQualifiedName(), sym.getMeta()))) {
					throw new VncException(String.format(
								"The var '%s' is not defined as dynamic", 
								sym.getQualifiedName()));
				}
			}
		}
		return null;
	}
	
	private VncVal getOrElse(final VncSymbol sym, final VncVal defaultVal) {
		if (sym.hasNamespace()) {
			// if we got a namespace it must be a global var
			final Var glob = getGlobalVar(sym);
			return glob == null ? defaultVal : glob.getVal();
		}
		else {
			final Var local = findLocalVar(sym);
		
			if (globalVarLookupOptimization) {
				if (local != null) {
					if (local instanceof GlobalRefVar) {
						final Var glob = getGlobalVar(sym);
						return glob == null ? defaultVal : glob.getVal();
					}
					else {
						return local.getVal();
					}
				}
				else {
					final Var glob = getGlobalVar(sym);
					if (glob != null) {
						localSymbols.put(sym, new GlobalRefVar(sym));
						return glob.getVal();
					}
					else {
						return defaultVal;
					}
				}
			}
			else {
				if (local != null) {
					return local.getVal();
				}
				else {
					final Var glob = getGlobalVar(sym);
					return glob == null ? defaultVal : glob.getVal();
				}
			}
		}
	}
	
	private Var findLocalVar(final VncSymbol sym) {
		Var v = this.localSymbols.get(sym);
		if (v != null) return v;

		// descend through the env levels
		Env env = this.outer;		
		while(env != null) {
			v = env.localSymbols.get(sym);
			if (v != null) return v;
			env = env.outer;
		}
		
		return null;
	}
	
	private Var getGlobalVar(final VncSymbol sym) {
		final String name = sym.getName();

		if (name.equals(Namespaces.NS_CURRENT_NAME)) {
			return new Var(Namespaces.NS_CURRENT_SYMBOL, Namespaces.getCurrentNS());
		}

		Var v = null;

		final boolean qualified = sym.hasNamespace();
		if (qualified && "core".equals(sym.getNamespace())) {
			// core/test
			v = getGlobalVarRaw(new VncSymbol(name.substring(5)));
		}
		else {
			if (!qualified) {
				final VncSymbol ns = Namespaces.getCurrentNS();
				if (!Namespaces.isCoreNS(ns)) {
					final VncSymbol qualifiedKey = new VncSymbol(ns.getName(), name, Constants.Nil);
					// curr-ns/test
					v = getGlobalVarRaw(qualifiedKey);
				}
			}
	
			if (v == null) {
				// test
				v = getGlobalVarRaw(sym);
			}
		}
		
		if (v == null) {
			return null;
		}
		else {
			if (failOnPrivateSymbolAccess) {
				rejectPrivateSymbolAccess(sym, v);
			}
			return v;
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

	private void rejectPrivateSymbolAccess(final VncSymbol sym, final Var envVar) {
		final VncSymbol envSym = envVar.getName();
		if (envSym.isPrivate()) {
			// note: global symbols without namespace belong to the "core" namespace
			final String currNS = Namespaces.getCurrentNS().getName();
			final String symNS = envSym.hasNamespace() ? envSym.getNamespace() : "core";
			if (!currNS.equals(symNS)) {
				final CallStack callStack = ThreadLocalMap.getCallStack();
				
				try (WithCallStack cs = new WithCallStack(new CallFrame("symbol", sym.getMeta()))) {
					throw new VncException(String.format(
							"Illegal access of private symbol '%s/%s' "
								+ "accessed from namespace '%s'.\n%s", 
							symNS,
							envSym.getSimpleName(),
							currNS,
							callStack.toString()));
				}
			}
		}	
	}
	
	
	
	private static final long serialVersionUID = 9002640180394221858L;
	
	// Note: Clojure allows shadowing global vars by local vars
	private final boolean failOnShadowingGlobalVars = false; 

	private final boolean failOnPrivateSymbolAccess = true; 

	private final boolean globalVarLookupOptimization = true; 

	private final Env outer;
	private final int level;
	private final Map<VncSymbol,Var> precompiledGlobalSymbols;
	private final Map<VncSymbol,Var> globalSymbols;
	private final Map<VncSymbol,Var> localSymbols;
}
