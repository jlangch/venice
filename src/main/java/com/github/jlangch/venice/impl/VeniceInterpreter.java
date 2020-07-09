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
import static com.github.jlangch.venice.impl.types.VncBoolean.False;
import static com.github.jlangch.venice.impl.types.VncBoolean.True;

import java.io.Closeable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.github.jlangch.venice.AssertionException;
import com.github.jlangch.venice.Version;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.functions.CoreFunctions;
import com.github.jlangch.venice.impl.functions.Functions;
import com.github.jlangch.venice.impl.reader.Reader;
import com.github.jlangch.venice.impl.specialforms.DefTypeForm;
import com.github.jlangch.venice.impl.specialforms.DocForm;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.INamespaceAware;
import com.github.jlangch.venice.impl.types.IVncFunction;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncMultiArityFunction;
import com.github.jlangch.venice.impl.types.VncMultiFunction;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncMutableSet;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.collections.VncSet;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.concurrent.ThreadLocalMap;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.CallFrame;
import com.github.jlangch.venice.impl.util.CallStack;
import com.github.jlangch.venice.impl.util.CatchBlock;
import com.github.jlangch.venice.impl.util.Inspector;
import com.github.jlangch.venice.impl.util.MeterRegistry;
import com.github.jlangch.venice.impl.util.WithCallStack;
import com.github.jlangch.venice.impl.util.reflect.ReflectionAccessor;
import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;
import com.github.jlangch.venice.javainterop.IInterceptor;


public class VeniceInterpreter implements Serializable  {

	public VeniceInterpreter() {
		this(new MeterRegistry(false), new AcceptAllInterceptor(), null);
	}

	public VeniceInterpreter(
			final IInterceptor interceptor, 
			final List<String> loadPaths
	) {
		this(new MeterRegistry(false), interceptor, loadPaths);
	}

	public VeniceInterpreter(
			final MeterRegistry perfmeter, 
			final IInterceptor interceptor, 
			final List<String> loadPaths
	) {
		this.meterRegistry = perfmeter;
		this.interceptor = interceptor;
		this.loadPaths = loadPaths;
		
		// performance optimization
		this.checkSandbox = !(interceptor instanceof AcceptAllInterceptor);
	}
	
	public void initNS() {
		nsRegistry.clear();
		Namespaces.setCurrentNamespace(nsRegistry.computeIfAbsent(Namespaces.NS_USER));
	}
	
	public void sealSystemNS() {
		sealedSystemNS.set(true);
	}
	
	// read
	public VncVal READ(final String script, final String filename) {
		final long nanos = System.nanoTime();

		final VncVal val = Reader.read_str(script, filename);

		if (meterRegistry.enabled) {
			meterRegistry.record("venice.read", System.nanoTime() - nanos);
		}

		return val;
	}

	public VncVal EVAL(final VncVal ast, final Env env) {
		final long nanos = System.nanoTime();

		final VncVal val = evaluate(ast, env);
		
		if (meterRegistry.enabled) {
			meterRegistry.record("venice.eval", System.nanoTime() - nanos);
		}

		return val;
	}

	public VncVal MACROEXPAND(
			final VncVal ast, 
			final Env env, 
			final boolean macroexpand
	) {
		if (macroexpand) {
			final VncFunction macroexpandFn = (VncFunction)env.getGlobalOrNull(
													new VncSymbol("core/macroexpand-all"));
			if (macroexpandFn != null) {
				return macroexpandFn.apply(VncList.of(ast));
			}
		}
		
		return ast;		
	}
	
	public VncVal RE(
			final String script, 
			final String name, 
			final Env env
	) {
		return EVAL(READ(script, name), env);		
	}
	
	public VncVal RE(
			final String script, 
			final String name, 
			final Env env,
			final boolean macroexpand
	) {
		VncVal ast = READ(script, name);			
		if (macroexpand) {
			ast = MACROEXPAND(ast, env, macroexpand);			
		}
		return EVAL(ast, env);
	}

	// print
	public String PRINT(final VncVal exp) {
		return Printer.pr_str(exp, true);
	}
		
	public Env createEnv(
			final boolean macroexpandOnLoad, 
			final boolean ansiTerminal, 
			final VncKeyword runMode
	) {  
		return createEnv(null, macroexpandOnLoad, ansiTerminal, runMode);
	}

	public Env createEnv(
			final List<String> preloadExtensionModules,
			final boolean macroexpandOnLoad, 
			final boolean ansiTerminal,
			final VncKeyword runMode
	) {
		sealedSystemNS.set(false);

		final Env env = new Env(null);
			
		// loaded modules: preset with implicitly preloaded modules
		final VncMutableSet loadedModules = new VncMutableSet(ModuleLoader.PRELOADED_MODULES);
		
		for(Map.Entry<VncVal,VncVal> e: Functions.functions.entrySet()) {
			final VncSymbol sym = (VncSymbol)e.getKey();
			final VncFunction fn = (VncFunction)e.getValue();			
			env.setGlobal(new Var(sym, fn, fn.isRedefinable()));
		}

		// set Venice version
		env.setGlobal(VERSION_VAR);

		// set system newline
		env.setGlobal(NEWLINE_VAR);

		// ansi terminal
		env.setGlobal(new Var(ANSI_TERM_SYMBOL, VncBoolean.of(ansiTerminal), false));

		// set the load path
		env.setGlobal(new Var(LOAD_PATH_SYMBOL, LoadPath.toVncList(loadPaths), false));
		
		// set the run mode
		env.setGlobal(new Var(RUN_MODE_SYMBOL, runMode == null ? Constants.Nil : runMode, false));

		// start off with disabled macroexpand-on-load
		env.setGlobal(DISABLED_MACRO_EXPAND_ON_LOAD_SYMBOL_VAR);
		
		// loaded modules & files
		env.setGlobal(new Var(LOADED_MODULES_SYMBOL, loadedModules, true));
		env.setGlobal(new Var(LOADED_FILES_SYMBOL, new VncMutableSet(), true));

		// init namespaces
		initNS();

		// load core module (take care that macro expansion is not active!)
		loadModule("core", env, loadedModules);
		
		// set macroexpand on load
		if (macroexpandOnLoad) {
			env.setGlobal(new Var(MACRO_EXPAND_ON_LOAD_SYMBOL, True, true));
		}

		sealedSystemNS.set(true);

		// load other modules requested for preload
		toEmpty(preloadExtensionModules).forEach(m -> loadModule(m, env, loadedModules));

		return env;
	}
	
	public List<String> getAvailableModules() {
		final List<String> modules = new ArrayList<>(ModuleLoader.VALID_MODULES);
		modules.removeAll(Arrays.asList("core", "test", "http", "jackson"));
		Collections.sort(modules);
		return modules;
	}
	
	private void loadModule(final String module, final Env env, final VncMutableSet loadedModules) {
		final long nanos = System.nanoTime();
		
		RE("(eval " + ModuleLoader.loadModule(module) + ")", module, env);

		if (meterRegistry.enabled) {
			meterRegistry.record("venice.module." + module + ".load", System.nanoTime() - nanos);
		}
		
		// remember the loaded module
		loadedModules.add(new VncKeyword(module));
	}
	
	/**
	 * Evaluate the passed ast as s-expr, simple value, or collection of values.
	 * 
	 * <p>An s-expr is a list with at least one value and the first value being a 
	 * symbol.
	 * 
	 * @param ast_ an ast
	 * @param env_ the env
	 * @return the result
	 */
	private VncVal evaluate(final VncVal ast_, final Env env_) {
		RecursionPoint recursionPoint = null;
		
		VncVal orig_ast = ast_;
		Env env = env_;
		
		while (true) {
			//System.out.println("EVAL: " + printer._pr_str(orig_ast, true));
			if (!(orig_ast instanceof VncList)) {
				// not an s-expr
				return evaluate_values(orig_ast, env);
			}
	
			// expand macros
			final VncVal expanded = macroexpand(orig_ast, env);
			if (!(expanded instanceof VncList)) {
				// not an s-expr
				return evaluate_values(expanded, env);
			}
			
			final VncList ast = (VncList)expanded;
			if (ast.isEmpty()) { 
				return ast; 
			}
			
			final VncVal a0 = ast.first();		
			final String a0sym = Types.isVncSymbol(a0) ? ((VncSymbol)a0).getName() : "__<*fn*>__";
			
			switch (a0sym) {		
				case "do": {
						final VncList expressions = ast.rest();						
						evaluate_values(expressions.butlast(), env);
						orig_ast = expressions.last();
					}
					break;
					
				case "def": { // (def name value)
					final VncSymbol name = validateSymbolWithCurrNS(
												qualifySymbolWithCurrNS(
														evaluateSymbolMetaData(ast.second(), env)),
												"def");
					
					final VncVal val = ast.third();
					
					final VncVal res = evaluate(val, env).withMeta(name.getMeta());
					env.setGlobal(new Var(name, res, true));
					return name;
				}
				
				case "defonce": { // (defonce name value)
					final VncSymbol name = validateSymbolWithCurrNS(
												qualifySymbolWithCurrNS(
														evaluateSymbolMetaData(ast.second(), env)),
												"defonce");
									
					final VncVal val = ast.third();

					final VncVal res = evaluate(val, env).withMeta(name.getMeta());
					env.setGlobal(new Var(name, res, false));
					return name;
				}
				
				case "def-dynamic": { // (def-dynamic name value)
					final VncSymbol name = validateSymbolWithCurrNS(
												qualifySymbolWithCurrNS(
														evaluateSymbolMetaData(ast.second(), env)),
												"def-dynamic");
					
					final VncVal val = ast.third();
					
					final VncVal res = evaluate(val, env).withMeta(name.getMeta());
					env.setGlobalDynamic(name, res);
					return name;
				}

				case "defmacro":
					try (WithCallStack cs = new WithCallStack(CallFrame.fromVal("defmacro", ast))) {
						return defmacro_(ast, env);
					}
				
				case "deftype": { // (deftype type fields validationFn*)
					try (WithCallStack cs = new WithCallStack(CallFrame.fromVal("deftype", ast))) {
						final VncKeyword type = Coerce.toVncKeyword(evaluate(ast.second(), env));
						final VncVector fields = Coerce.toVncVector(ast.third());
						final VncFunction validationFn = ast.size() == 4
															? Coerce.toVncFunction(evaluate(ast.fourth(), env))
															: null;
	
						return DefTypeForm.defineCustomType(type, fields, validationFn, this::RE, env);
					}
				}
				
				case "deftype?": { // (deftype? type)
					try (WithCallStack cs = new WithCallStack(CallFrame.fromVal("deftype?", ast))) {
						final VncVal type = evaluate(ast.second(), env);

						return VncBoolean.of(DefTypeForm.isCustomType(type, env));
					}
				}
				
				case "deftype-of": { // (deftype-of type base-type validationFn*)
					try (WithCallStack cs = new WithCallStack(CallFrame.fromVal("deftype-of", ast))) {
						final VncKeyword type = Coerce.toVncKeyword(evaluate(ast.second(), env));
						final VncKeyword baseType = Coerce.toVncKeyword(evaluate(ast.third(), env));
						final VncFunction validationFn = ast.size() == 4
															? Coerce.toVncFunction(evaluate(ast.fourth(), env))
															: null;
						return DefTypeForm.defineCustomWrapperType(
									type, 
									baseType, 
									validationFn, 
									this::RE, 
									env,
									wrappableTypes);
					}
				}
				
				case "deftype-or": { // (deftype-of type base-type*)
					try (WithCallStack cs = new WithCallStack(CallFrame.fromVal("deftype-or", ast))) {
						final VncKeyword type = Coerce.toVncKeyword(evaluate(ast.second(), env));
						final VncList choiceVals = ast.slice(2);

						return DefTypeForm.defineCustomChoiceType(type, choiceVals, this::RE, env);
					}
				}

				case ".:": { // (.: type args*)
					final List<VncVal> args = new ArrayList<>();
					for(VncVal v : ast.rest().getList()) {
						args.add(evaluate(v, env));
					}
					return DefTypeForm.createType(args, env);
				}

				case "set!": { // (set! name expr)
					final VncSymbol name = qualifySymbolWithCurrNS(
												evaluateSymbolMetaData(ast.second(), env));
					final Var globVar = env.getGlobalVarOrNull(name);
					if (globVar != null) {
						final VncVal expr = ast.third();
						final VncVal val = evaluate(expr, env).withMeta(name.getMeta());
						
						if (globVar instanceof DynamicVar) {
							env.popGlobalDynamic(globVar.getName());
							env.pushGlobalDynamic(globVar.getName(), val);
						}
						else {
							env.setGlobal(new Var(globVar.getName(), val, globVar.isOverwritable()));
						}
						return val;
					}
					else {
						try (WithCallStack cs = new WithCallStack(CallFrame.fromVal(name))) {
							throw new VncException(String.format(
										"The global var or thread-local '%s' does not exist!", 
										name.getName()));
						}
					}
				}
				
				case "defmulti": { // (defmulti name dispatch-fn)
					final VncSymbol name =  validateSymbolWithCurrNS(
												qualifySymbolWithCurrNS(
														evaluateSymbolMetaData(ast.second(), env)),
												"defmulti");
					
					IVncFunction dispatchFn;
					
					if (Types.isVncKeyword(ast.third())) {
						dispatchFn = (VncKeyword)ast.third();
					}
					else if (Types.isVncSymbol(ast.third())) {
						dispatchFn = Coerce.toVncFunction(env.get((VncSymbol)ast.third()));
					}
					else {
						dispatchFn = fn_(Coerce.toVncList(ast.third()), env);
					}

					final VncMultiFunction multiFn = new VncMultiFunction(name.getName(), dispatchFn)
																.withMeta(name.getMeta());
					env.setGlobal(new Var(name, multiFn, false));
					return multiFn;
				}
				
				case "defmethod": { // (defmethod multifn-name dispatch-val & fn-tail)
					final VncSymbol multiFnName = qualifySymbolWithCurrNS(
													Coerce.toVncSymbol(ast.second()));
					final VncVal multiFnVal = env.getGlobalOrNull(multiFnName);
					if (multiFnVal == null) {
						try (WithCallStack cs = new WithCallStack(CallFrame.fromVal(ast))) {
							throw new VncException(String.format(
										"No multifunction '%s' defined for the method definition", 
										multiFnName.getName())); 
						}
					}
					final VncMultiFunction multiFn = Coerce.toVncMultiFunction(multiFnVal);
					final VncVal dispatchVal = ast.third();
					
					final VncVector params = Coerce.toVncVector(ast.fourth());
					if (params.size() != multiFn.getParams().size()) {
						try (WithCallStack cs = new WithCallStack(CallFrame.fromVal(ast))) {
							throw new VncException(String.format(
									"A method definition for the multifunction '%s' must have %d parameters", 
									multiFnName.getName(),
									multiFn.getParams().size()));
						}
					}
					final VncVector preConditions = getFnPreconditions(ast.nth(4));
					final VncList body = ast.slice(preConditions == null ? 4 : 5);
					final VncFunction fn = buildFunction(
												multiFnName.getName(),
												params,
												body,
												preConditions,
												false,
												env);

					return multiFn.addFn(dispatchVal, fn.withMeta(ast.getMeta()));
				}
				
				case "ns": { // (ns alpha)
					final VncSymbol ns = Coerce.toVncSymbol(ast.second());
					if (ns.hasNamespace()) {
						try (WithCallStack cs = new WithCallStack(CallFrame.fromVal("ns", ast))) {
							throw new VncException(String.format(
									"A namespace '%s' must not have itself a namespace! However you can use '%s'.",
									ns.getQualifiedName(),
									ns.getNamespace() + "." + ns.getSimpleName()));
						}
					}
					else {
						if (Namespaces.isSystemNS(ns.getName()) && sealedSystemNS.get()) {
							// prevent Venice's system namespaces from being altered
							try (WithCallStack cs = new WithCallStack(CallFrame.fromVal("ns", ast))) {
								throw new VncException("Namespace '" + ns.getName() + "' cannot be reopened!");
							}
						}
						Namespaces.setCurrentNamespace(nsRegistry.computeIfAbsent(ns));
						return ns;
					}
				}
				
				case "ns-remove": { // (ns-remove ns)
					final VncSymbol ns = Namespaces.lookupNS(ast.second(), env);
					if (Namespaces.isSystemNS(ns.getName()) && sealedSystemNS.get()) {
						// prevent Venice's system namespaces from being altered
						try (WithCallStack cs = new WithCallStack(CallFrame.fromVal("ns-remove", ast))) {
							throw new VncException("Namespace '" + ns.getName() + "' cannot be removed!");
						}
					}
					else {
						env.removeGlobalSymbolsByNS(ns);
						nsRegistry.remove(ns);
						return Nil;
					}
				}
				
				case "ns-unmap": { // (ns-unmap ns sym)
					final VncSymbol ns = Namespaces.lookupNS(ast.second(), env);
					if (Namespaces.isSystemNS(ns.getName()) && sealedSystemNS.get()) {
						// prevent Venice's system namespaces from being altered
						try (WithCallStack cs = new WithCallStack(CallFrame.fromVal("ns-unmap", ast))) {
							throw new VncException("Cannot remove a symbol from namespace '" + ns.getName() + "'!");
						}
					}
					else {
						final VncSymbol sym = Coerce.toVncSymbol(ast.third()).withNamespace(ns);
						env.removeGlobalSymbol(sym);
						return Nil;
					}
				}

				case "namespace": { // (namespace x)
					final VncVal val = evaluate(ast.second(), env);
					if (val instanceof INamespaceAware) {
						return new VncString(((INamespaceAware)val).getNamespace());
					}
					else {
						try (WithCallStack cs = new WithCallStack(CallFrame.fromVal("namespace", ast))) {
							throw new VncException(String.format(
									"The type '%s' does not support namespaces!",
									Types.getType(val)));
						}
					}
				}
				
				case "import":
					try (WithCallStack cs = new WithCallStack(CallFrame.fromVal("import", ast))) {
						ast.rest().forEach(i -> Namespaces
													.getCurrentNamespace()
													.getJavaImports()
													.add(Coerce.toVncString(i).getValue()));
						return Nil;
					}
					
				case "imports": {
					if (ast.size() == 1) {
						return Namespaces.getCurrentNamespace().getJavaImportsAsVncList();
					}
					else {
						final VncSymbol ns = Coerce.toVncSymbol(ast.second());
						final Namespace namespace = nsRegistry.get(ns);
						if (namespace != null) {
							return namespace.getJavaImportsAsVncList();
						}
						else {
							try (WithCallStack cs = new WithCallStack(CallFrame.fromVal("imports", ast))) {
								throw new VncException(String.format(
									"The namespace '%s' does not exist", ns.toString()));
							}
						}
					}
				}
							 	
				case "resolve": { // (resolve sym)
					final VncSymbol sym = Coerce.toVncSymbol(evaluate(ast.second(), env));
					return env.getOrNil(sym);
				}
				
				case "var-get": { // (var-get sym)
					final VncSymbol sym = Coerce.toVncSymbol(evaluate(ast.second(), env));
					return env.getOrNil(sym);
				}
				
				case "inspect": { // (inspect sym)
					final VncSymbol sym = Coerce.toVncSymbol(evaluate(ast.second(), env));
					return Inspector.inspect(env.get(sym));
				}

				case "macroexpand": 
					try (WithCallStack cs = new WithCallStack(CallFrame.fromVal("macroexpand", ast))) {
						return macroexpand(evaluate(ast.second(), env), env);
					}
					
				case "quote":
					return ast.second();
					
				case "quasiquote":
					orig_ast = quasiquote(ast.second());
					break;
					
				case "doc": // (doc conj)
					try (WithCallStack cs = new WithCallStack(CallFrame.fromVal("doc", ast))) {
						final VncString doc = DocForm.doc(ast.second(), env);
						orig_ast = VncList.of(new VncSymbol("println"), doc);
					}
					break;
					
				case "modules": // (modules )
					try (WithCallStack cs = new WithCallStack(CallFrame.fromVal("modules", ast))) {
						return VncList.ofList(
									ModuleLoader
										.VALID_MODULES
										.stream()
										.filter(s ->!s.equals("core"))  // skip core module
										.sorted()
										.map(s -> new VncKeyword(s))
										.collect(Collectors.toList()));
					}
					
				case "eval": {
					final Namespace ns = Namespaces.getCurrentNamespace();
					try {
						return evaluate(Coerce.toVncSequence(evaluate_values(ast.rest(), env)).last(), env);
					}
					finally {
						Namespaces.setCurrentNamespace(ns);
					}
				}
					
				case "let":  { // (let [bindings*] exprs*)
					env = new Env(env);
	
					final VncVector bindings = Coerce.toVncVector(ast.second());
					final VncList expressions = ast.slice(2);
				
					for(int i=0; i<bindings.size(); i+=2) {
						final VncVal sym = bindings.nth(i);
						final VncVal val = evaluate(bindings.nth(i+1), env);

						env.addLocalBindings(Destructuring.destructure(sym, val));
					}
						
					if (expressions.isEmpty()) {
						orig_ast = Constants.Nil;
					}
					else {
						evaluate_values(expressions.butlast(), env);
						orig_ast = expressions.last();
					}
					break;
				}
				
				case "binding":  // (binding [bindings*] exprs*)
					return binding_(ast, new Env(env));
					
				case "bound?": { // (bound? sym)
					final VncSymbol sym = Coerce.toVncSymbol(evaluate(ast.second(), env));
					return VncBoolean.of(env.isBound(sym));
				}
				
				case "global-vars-count": { // (global-vars-count)
					return new VncLong(env.globalsCount());
				}
					
				case "loop": { // (loop [bindings*] exprs*)
					recursionPoint = null;
					env = new Env(env);
	
					final VncVector bindings = Coerce.toVncVector(ast.second());
					final VncList expressions = ast.slice(2);
					
					final List<VncSymbol> bindingNames = new ArrayList<>(bindings.size() / 2);
					for(int i=0; i<bindings.size(); i+=2) {
						final VncSymbol sym = Coerce.toVncSymbol(bindings.nth(i));
						final VncVal val = evaluate(bindings.nth(i+1), env);
	
						env.setLocal(sym, val);
						bindingNames.add(sym);
					}
					
					recursionPoint = new RecursionPoint(bindingNames, expressions, env);
					if (expressions.size() == 1) {
						orig_ast = expressions.first();
					}
					else {
						evaluate_values(expressions.butlast(), env);
						orig_ast = expressions.last();						
					}
				}
				break;
	
				case "recur":  { // (recur exprs*)
					// +----------------+-------------------------------------------+---------------+
					// | Form           | Tail Position                             | recur target? |
					// +----------------+-------------------------------------------+---------------+
					// | fn, defn       | (fn [args] expressions tail)              | No            |
					// | loop           | (loop [bindings] expressions tail)        | Yes           |
					// | let            | (let [bindings] expressions tail)         | No            |
					// | do             | (do expressions tail)                     | No            |
					// | if, if-not     | (if test then tail else tail)             | No            |
					// | when, when-not | (when test expressions tail)              | No            |
					// | cond           | (cond test test tail ... :else else tail) | No            |
					// | case           | (case const const tail ... default tail)  | No            |
					// | or, and        | (or test test ... tail)                   | No            |
					// +----------------+-------------------------------------------+---------------+

					if (recursionPoint == null) {
						try (WithCallStack cs = new WithCallStack(CallFrame.fromVal("recur", ast))) {
							throw new VncException("The recur expression is not in tail position!");
						}
					}

					final Env recur_env = recursionPoint.getLoopEnv();
	
					// denormalize for best performance (short loops are performance critical)
					switch(ast.size()) {
						case 1:
							break;
						case 2:
							// [1][2] calculate and bind the single new value
							recur_env.setLocal(recursionPoint.getLoopBindingName(0), evaluate(ast.second(), env));
							break;
						case 3:
							// [1] calculate the new values
							final VncVal v1 = evaluate(ast.second(), env);
							final VncVal v2 = evaluate(ast.third(), env);
							// [2] bind the new values
							recur_env.setLocal(recursionPoint.getLoopBindingName(0), v1);
							recur_env.setLocal(recursionPoint.getLoopBindingName(1), v2);
							break;
						case 4:
							// [1] calculate the new values
							final VncVal v1_ = evaluate(ast.second(), env);
							final VncVal v2_ = evaluate(ast.third(), env);
							final VncVal v3_ = evaluate(ast.fourth(), env);
							// [2] bind the new values
							recur_env.setLocal(recursionPoint.getLoopBindingName(0), v1_);
							recur_env.setLocal(recursionPoint.getLoopBindingName(1), v2_);
							recur_env.setLocal(recursionPoint.getLoopBindingName(2), v3_);
							break;
						default:
							// [1] calculate new values
							final VncList values = ast.rest();
							final VncVal[] newValues = new VncVal[values.size()];
							for(int kk=0; kk<values.size(); kk++) {
								newValues[kk++] = evaluate(values.nth(kk), env);
							}
							
							// [2] bind the new values
							for(int ii=0; ii<recursionPoint.getLoopBindingNamesCount(); ii++) {
								recur_env.setLocal(recursionPoint.getLoopBindingName(ii), newValues[ii]);
							}
							break;
					}
					
					// [3] continue on the loop with the new bindings
					final VncList expressions = recursionPoint.getLoopExpressions();
					
					env = recur_env;
					if (expressions.size() > 1) {
						evaluate_values(expressions.butlast(), env);
					}
					orig_ast = expressions.last();						
				}
				break;
					
				case "try":  // (try expr (catch :Exception e expr) (finally expr))
					try (WithCallStack cs = new WithCallStack(CallFrame.fromVal("try", ast))) {
						return try_(ast, new Env(env));
					}
					
				case "try-with": // (try-with [bindings*] expr (catch :Exception e expr) (finally expr))
					try (WithCallStack cs = new WithCallStack(CallFrame.fromVal("try-with", ast))) {
						return try_with_(ast, new Env(env));
					}
					
				case "dorun":
					return dorun_(ast, env);
				
				case "dobench":
					return dobench_(ast, env);
					
				case "if": 
					final VncVal cond = evaluate(ast.second(), env);
					if (VncBoolean.isFalse(cond) || cond == Nil) {
						// eval false slot form
						if (ast.size() > 3) {
							orig_ast = ast.nth(3);
						} 
						else {
							return Nil;
						}
					} 
					else {
						// eval true slot form
						orig_ast = ast.nth(2);
					}
					break;
					
				case "fn":
					// (fn name? [params*] condition-map? expr*)
					return fn_(ast, env);
					
				case "prof":
					return prof_(ast, env);
					
				case "locking":
					return locking_(ast, env);
	
				default:					
					final VncList el = (VncList)evaluate_values((VncList)ast, env);
					final VncVal elFirst = el.first();
					final VncList elArgs = el.rest();
					if (elFirst instanceof VncFunction) {
						final VncFunction fn = (VncFunction)elFirst;
						
						final String fnName = fn.getQualifiedName();

						final long nanos = meterRegistry.enabled ? System.nanoTime() : 0L;
						
						// validate function call allowed by sandbox
						if (checkSandbox) {
							interceptor.validateVeniceFunction(fnName);	
							interceptor.validateMaxExecutionTime();
						}
						
						checkInterrupted(fnName);

						final CallStack callStack = ThreadLocalMap.getCallStack();
						
						// invoke function with call frame
						final CallFrame callFrame = CallFrame.fromFunction(fn, a0);
						try {
							callStack.push(callFrame);
							return fn.apply(elArgs);
						}
						finally {
							callStack.pop();
							checkInterrupted(fnName);
							if (checkSandbox) {
								interceptor.validateMaxExecutionTime();
							}
							if (meterRegistry.enabled) {
								meterRegistry.record(fn.getQualifiedName(), System.nanoTime() - nanos);
							}
						}
					}
					else if (elFirst instanceof IVncFunction) {
						// 1)  keyword as function to access maps: (:a {:a 100})
						// 2)  a map as function to deliver its value for a key: ({:a 100} :a)
						return ((IVncFunction)elFirst).apply(elArgs);
					}
					else {
						try (WithCallStack cs = new WithCallStack(CallFrame.fromVal(ast))) {
							throw new VncException(String.format(
									"Not a function or keyword/map used as function: '%s'", 
									PRINT(elFirst)));
						}
					}
			}
		}
	}

	private VncVal evaluate_values(final VncVal ast, final Env env) {
		if (ast instanceof VncSymbol) {
			return env.get((VncSymbol)ast);
		}
		else if (ast instanceof VncSequence) {
			final VncSequence seq = (VncSequence)ast;
			
			switch(seq.size()) {
				case 0: 
					return seq;
				case 1:
					return seq.withVariadicValues(
								evaluate(seq.first(), env));
				case 2: 
					return seq.withVariadicValues(
								evaluate(seq.first(), env), 
								evaluate(seq.second(), env));
				case 3: 
					return seq.withVariadicValues(
								evaluate(seq.first(), env), 
								evaluate(seq.second(), env),
								evaluate(seq.third(), env));
				case 4: 
					return seq.withVariadicValues(
								evaluate(seq.first(), env), 
								evaluate(seq.second(), env),
								evaluate(seq.third(), env),
								evaluate(seq.fourth(), env));
				default: 
					final List<VncVal> vals = new ArrayList<>();
					for(VncVal v : seq.getList()) {
						vals.add(evaluate(v, env));
					}
					return seq.withValues(vals);
			}
		}
		else if (ast instanceof VncMap) {
			final VncMap map = (VncMap)ast;
			
			final Map<VncVal,VncVal> vals = new HashMap<>();
			for(Entry<VncVal,VncVal> e: map.getMap().entrySet()) {
				vals.put(
					evaluate(e.getKey(), env), 
					evaluate(e.getValue(), env));
			}
			return map.withValues(vals);
		} 
		else if (ast instanceof VncSet) {
			final VncSet set = (VncSet)ast;
			
			final List<VncVal> vals = new ArrayList<>();
			for(VncVal v: set.getList()) {
				vals.add(evaluate(v, env));
			}
			return set.withValues(vals);
		} 
		else {
			return ast;
		}
	}

	/**
	 * Recursively expands a macro. Inside the loop, the first element 
	 * of the ast list (a symbol), is looked up in the environment to get 
	 * the macro function. This macro function is then called/applied with 
	 * the rest of the ast elements (2nd through the last) as arguments. 
	 * The return value of the macro call becomes the new value of ast. 
	 * When the loop completes because ast no longer represents a macro call, 
	 * the current value of ast is returned.
	 * 
	 * <p>Macro check:
	 * An ast is a macro if the ast is a list that contains a symbol as the 
	 * first element and that symbol refers to a function in the env environment 
	 * and that function has the macro attribute set to true. 
	 * 
	 * @param ast ast
	 * @param env env
	 * @return the expanded macro
	 */
	private VncVal macroexpand(final VncVal ast, final Env env) {
		final long nanos = meterRegistry.enabled ? System.nanoTime() : 0L;
		
		VncVal ast_ = ast;
		boolean expanded = false;
		
		while (ast_ instanceof VncList) {
			final VncVal a0 = ((VncList)ast_).first();
			if (!(a0 instanceof VncSymbol)) break;
			
			final VncVal fn = env.getGlobalOrNull((VncSymbol)a0);
			if (!(fn != null && fn instanceof VncFunction && ((VncFunction)fn).isMacro())) break;
			
			final VncFunction macro = (VncFunction)fn;

			// validate that the macro is allowed by the sandbox
			if (checkSandbox) {
				interceptor.validateVeniceFunction(macro.getQualifiedName());					
			}
			
			expanded = true; 

			ast_ = macro.apply(((VncList)ast_).rest());
		}
	
		if (expanded && meterRegistry.enabled) {
			meterRegistry.record("macroexpand", System.nanoTime() - nanos);
		}

		return ast_;
	}

	private static boolean is_pair(final VncVal x) {
		return Types.isVncSequence(x) && !((VncSequence)x).isEmpty();
	}

	private static VncVal quasiquote(final VncVal ast) {
		if (!is_pair(ast)) {
			return VncList.of(new VncSymbol("quote"), ast);
		} 
		else {
			final VncVal a0 = Coerce.toVncSequence(ast).first();
			if (Types.isVncSymbol(a0) && ((VncSymbol)a0).getName().equals("unquote")) {
				return ((VncSequence)ast).second();
			} 
			else if (is_pair(a0)) {
				final VncVal a00 = Coerce.toVncSequence(a0).first();
				if (Types.isVncSymbol(a00) && ((VncSymbol)a00).getName().equals("splice-unquote")) {
					return VncList.of(
								new VncSymbol("concat"),
								Coerce.toVncSequence(a0).second(),
								quasiquote(((VncSequence)ast).rest()));
				}
			}
			return VncList.of(
						new VncSymbol("cons"),
						quasiquote(a0),
						quasiquote(((VncSequence)ast).rest()));
		}
	}
	
	private VncFunction defmacro_(final VncList ast, final Env env) {
		int argPos = 1;
		
		final VncSymbol macroName = qualifySymbolWithCurrNS(
										evaluateSymbolMetaData(ast.nth(argPos++), env));
		VncVal meta = macroName.getMeta();
		
		final VncSequence paramsOrSig = Coerce.toVncSequence(ast.nth(argPos));
					
		String name = macroName.getName();
		String ns = macroName.getNamespace();

		if (ns == null) {
			ns = Namespaces.getCurrentNS().getName();
			if (!Namespaces.isCoreNS(ns)) {
				name = ns + "/" + name;
			}
		}

		meta = MetaUtil.addMetaVal(
							meta,
							MetaUtil.NS, new VncString(ns),
							MetaUtil.MACRO, True);

		final VncSymbol macroName_ = new VncSymbol(name, meta);

		if (Types.isVncVector(paramsOrSig)) {
			// single arity:
			
			argPos++;
			final VncVector params = (VncVector)paramsOrSig;

			final VncVal body = ast.nth(argPos++);
	
	
			final VncFunction macroFn = buildFunction(
											macroName_.getName(), 
											params, 
											VncList.of(body), 
											null, 
											true,
											env);
	
			env.setGlobal(new Var(macroName_, macroFn.withMeta(meta), false));

			return macroFn;
		}
		else {
			// multi arity:

			final List<VncFunction> fns = new ArrayList<>();
			
			ast.slice(argPos).forEach(s -> {
				int pos = 0;
				
				final VncList fnSig = Coerce.toVncList(s);
				
				final VncVector fnParams = Coerce.toVncVector(fnSig.nth(pos++));
				
				final VncList fnBody = fnSig.slice(pos);
				
				fns.add(buildFunction(
							macroName_.getName() + "-arity-" + fnParams.size(),
							fnParams, 
							fnBody, 
							null,
							false,
							env));
			});

			final VncFunction macroFn = new VncMultiArityFunction(macroName_.getName(), fns, true).withMeta(meta);
			
			env.setGlobal(new Var(macroName_, macroFn, false));

			return macroFn;
		}
	}

	private VncVal dorun_(final VncList ast, final Env env) {
		if (ast.size() != 3) {
			try (WithCallStack cs = new WithCallStack(CallFrame.fromVal("dorun", ast))) {
				throw new VncException("dorun requires two arguments a count and an expression to run");
			}
		}
		
		final long count = Coerce.toVncLong(ast.second()).getValue();
		if (count <= 0) return Nil;
		
		final VncVal expr = ast.third();

		try {
			final VncVal first = evaluate(expr, env);
			
			for(int ii=1; ii<count; ii++) {
				final VncVal result = evaluate(expr, env);

				checkInterrupted("dorun");

				// store value to a mutable place to prevent JIT from optimizing too much
				ThreadLocalMap.set(new VncKeyword("*benchmark-val*"), result);
			}
			
			return first;
		}
		finally {
			ThreadLocalMap.remove(new VncKeyword("*benchmark-val*"));
		}
	}

	private VncVal dobench_(final VncList ast, final Env env) {
		if (ast.size() != 3) {
			try (WithCallStack cs = new WithCallStack(CallFrame.fromVal("dobench", ast))) {
				throw new VncException("dobench requires two arguments a count and an expression to run");
			}
		}
		
		try {
			final long count = Coerce.toVncLong(ast.second()).getValue();
			final VncVal expr = ast.third();
			
			final List<VncVal> elapsed = new ArrayList<>();
			for(int ii=0; ii<count; ii++) {
				final long start = System.nanoTime();
				
				final VncVal result = evaluate(expr, env);
				
				final long end = System.nanoTime();
				elapsed.add(new VncLong(end-start));

				checkInterrupted("dobench");

				// store value to a mutable place to prevent JIT from optimizing too much
				ThreadLocalMap.set(new VncKeyword("*benchmark-val*"), result);
			}
			
			return VncList.ofList(elapsed);
		}
		finally {
			ThreadLocalMap.remove(new VncKeyword("*benchmark-val*"));
		}
	}

	private VncVal locking_(final VncList ast, final Env env) {
		if (ast.size() < 3) {
			try (WithCallStack cs = new WithCallStack(CallFrame.fromVal("locking", ast))) {
				throw new VncException("locking requires a lockee and one or more expressions to run");
			}
		}
		
		final VncVal mutex = evaluate(ast.second(), env);

		synchronized(mutex) {
			return evaluateBody(ast.slice(2), env);
		}
	}

	private VncFunction fn_(final VncList ast, final Env env) {
		// single arity:  (fn name? [params*] condition-map? expr*)
		// multi arity:   (fn name? ([params*] condition-map? expr*)+ )

		VncSymbol name;
		int argPos;
		
		if (Types.isVncSymbol(ast.second())) {
			argPos = 2;
			name = (VncSymbol)ast.second();
		}
		else {
			argPos = 1;
			name = new VncSymbol(VncFunction.createAnonymousFuncName());
		}

		final VncSymbol fnName = qualifySymbolWithCurrNS(name);
		ReservedSymbols.validateNotReservedSymbol(fnName);

		final VncSequence paramsOrSig = Coerce.toVncSequence(ast.nth(argPos));
		if (Types.isVncVector(paramsOrSig)) {
			// single arity:
			
			argPos++;
			final VncVector params = (VncVector)paramsOrSig;
			
			final VncVector preConditions = getFnPreconditions(ast.nth(argPos));
			if (preConditions != null) argPos++;
			
			final VncList body = ast.slice(argPos);
			
			return buildFunction(fnName.getName(), params, body, preConditions, false, env);
		}
		else {
			// multi arity:

			final List<VncFunction> fns = new ArrayList<>();
			
			ast.slice(argPos).forEach(s -> {
				int pos = 0;
				
				final VncList sig = Coerce.toVncList(s);
				
				final VncVector params = Coerce.toVncVector(sig.nth(pos++));
				
				final VncVector preConditions = getFnPreconditions(sig.nth(pos));
				if (preConditions != null) pos++;
				
				final VncList body = sig.slice(pos);
				
				fns.add(buildFunction(fnName.getName(), params, body, preConditions, false, env));
			});
			
			return new VncMultiArityFunction(fnName.getName(), fns, false);
		}
	}

	private VncVal prof_(final VncList ast, final Env env) {
		if (Types.isVncKeyword(ast.second())) {
			final VncKeyword cmd = (VncKeyword)ast.second();
			switch(cmd.getValue()) {
				case "on":
				case "enable":
					meterRegistry.enable(); 
					return new VncKeyword("on");
				case "off":
				case "disable":
					meterRegistry.disable(); 
					return new VncKeyword("off");
				case "status":
					return new VncKeyword(meterRegistry.isEnabled() ? "on" : "off");
				case "clear":
					meterRegistry.reset(); 
					return new VncKeyword(meterRegistry.isEnabled() ? "on" : "off");
				case "clear-all-but":
					meterRegistry.resetAllBut(Coerce.toVncSequence(ast.third())); 
					return new VncKeyword(meterRegistry.isEnabled() ? "on" : "off");
				case "data":
					return meterRegistry.getVncTimerData();
				case "data-formatted":
					final String title = ast.size() == 3 ? Coerce.toVncString(ast.third()).getValue() : "Metrics";
					return new VncString(meterRegistry.getTimerDataFormatted(title));
			}
		}

		try (WithCallStack cs = new WithCallStack(CallFrame.fromVal("prof", ast))) {
			throw new VncException(
					"Function 'prof' expects a single keyword argument: " +
					":on, :off, :status, :clear, :clear-all-but, :data, " +
					"or :data-formatted");
		}
	}

	private VncVal binding_(final VncList ast, final Env env) {
		final VncSequence bindings = Coerce.toVncSequence(ast.second());
		final VncList expressions = ast.slice(2);
	
		final List<Var> vars = new ArrayList<>();
		for(int i=0; i<bindings.size(); i+=2) {
			final VncVal sym = bindings.nth(i);
			final VncVal val = evaluate(bindings.nth(i+1), env);
	
			for(Binding b : Destructuring.destructure(sym, val)) {
				vars.add(new Var(b.sym, b.val));
			}
		}
			
		try {
			vars.forEach(v -> env.pushGlobalDynamic(v.getName(), v.getVal()));
			
			evaluate_values(expressions.butlast(), env);
			return evaluate(expressions.last(), env);
		}
		finally {
			vars.forEach(v -> env.popGlobalDynamic(v.getName()));
		}
	}

	private VncVal try_(final VncList ast, final Env env) {
		VncVal result = Nil;

		try {
			result = evaluateBody(getTryBody(ast), env);
		} 
		catch (Throwable th) {
			final CatchBlock catchBlock = findCatchBlockMatchingThrowable(ast, th);
			if (catchBlock == null) {
				throw th;
			}
			else {
				env.setLocal(catchBlock.getExSym(), new VncJavaObject(th));			
				return evaluateBody(catchBlock.getBody(), env);
			}
		}
		finally {
			final VncList finallyBlock = findFirstFinallyBlock(ast);
			if (finallyBlock != null) {
				evaluate_values(finallyBlock.rest(), env);
			}
		}
		
		return result;
	}

	private VncVal try_with_(final VncList ast, final Env env) {
		final VncSequence bindings = Coerce.toVncSequence(ast.second());
		final List<Binding> boundResources = new ArrayList<>();
		
		for(int i=0; i<bindings.size(); i+=2) {
			final VncVal sym = bindings.nth(i);
			final VncVal val = evaluate(bindings.nth(i+1), env);

			if (Types.isVncSymbol(sym)) {
				env.setLocal((VncSymbol)sym, val);
				boundResources.add(new Binding((VncSymbol)sym, val));
			}
			else {
				try (WithCallStack cs = new WithCallStack(CallFrame.fromVal("try-with", ast))) {
					throw new VncException(
							String.format(
									"Invalid 'try-with' destructuring symbol value type %s. Expected symbol.",
									Types.getType(sym)));
				}
			}
		}

		
		VncVal result = Nil;
		try {
			try {
				result = evaluateBody(getTryBody(ast), env);
			} 
			catch (Throwable th) {
				final CatchBlock catchBlock = findCatchBlockMatchingThrowable(ast, th);
				if (catchBlock == null) {
					throw th;
				}
				else {
					env.setLocal(catchBlock.getExSym(), new VncJavaObject(th));
				
					return evaluateBody(catchBlock.getBody(), env);
				}
			}
			finally {
				// finally is only for side effects
				final VncList finallyBlock = findFirstFinallyBlock(ast);
				if (finallyBlock != null) {
					evaluate_values(finallyBlock.rest(), env);
				}
			}
		}
		finally {
			// close resources in reverse order
			Collections.reverse(boundResources);
			boundResources.stream().forEach(b -> {
				final VncVal resource = b.val;
				if (Types.isVncJavaObject(resource)) {
					final Object r = ((VncJavaObject)resource).getDelegate();
					if (r instanceof AutoCloseable) {
						try {
							((AutoCloseable)r).close();
						}
						catch(Exception ex) {
							try (WithCallStack cs = new WithCallStack(CallFrame.fromVal("try-with", ast))) {
								throw new VncException(
										String.format(
												"'try-with' failed to close resource %s.",
												b.sym.getName()));
							}
						}
					}
					else if (r instanceof Closeable) {
						try {
							((Closeable)r).close();
						}
						catch(Exception ex) {
							try (WithCallStack cs = new WithCallStack(CallFrame.fromVal("try-with", ast))) {
								throw new VncException(
										String.format(
												"'try-with' failed to close resource %s.",
												b.sym.getName()));
							}
						}
					}
				}
			});
		}
		
		return result;
	}

	private VncList getTryBody(final VncList ast) {
		final List<VncVal> body = new ArrayList<>();
		for(VncVal e : ast.rest().getList()) {
			if (Types.isVncList(e)) {
				final VncVal first = ((VncList)e).first();
				if (Types.isVncSymbol(first)) {
					final String symName = ((VncSymbol)first).getName();
					if (symName.equals("catch") || symName.equals("finally")) {
						break;
					}
				}
			}
			body.add(e);
		}
		
		return VncList.ofList(body);
	}
	
	private CatchBlock findCatchBlockMatchingThrowable(
			final VncList blocks, 
			final Throwable th
	) {
		for(VncVal b : blocks.getList()) {
			if (Types.isVncList(b)) {
				final VncList block = ((VncList)b);
				final VncVal first = block.first();
				if (Types.isVncSymbol(first) && ((VncSymbol)first).getName().equals("catch")) {
					if (isCatchBlockMatchingThrowable(block, th)) {
						return new CatchBlock(
									Coerce.toVncSymbol(block.nth(2)), 
									block.slice(3));
					}
				}
			}
		}
		
		return null;
	}
	
	private boolean isCatchBlockMatchingThrowable(
		final VncList block, 
		final Throwable th
	) {
		final String className = resolveClassName(((VncString)block.second()).getValue());
		final Class<?> targetClass = ReflectionAccessor.classForName(className);
		
		return targetClass.isAssignableFrom(th.getClass());
	}
	
	private VncList findFirstFinallyBlock(final VncList blocks) {
		for(VncVal b : blocks.getList()) {
			if (Types.isVncList(b)) {
				final VncList block = ((VncList)b);
				final VncVal first = block.first();
				if (Types.isVncSymbol(first) && ((VncSymbol)first).getName().equals("finally")) {
					return block;
				}
			}
		}
		return null;
	}
	
	private VncFunction buildFunction(
			final String name, 
			final VncVector params, 
			final VncList body, 
			final VncVector preConditions, 
			final boolean macro,
			final Env env
	) {
		final Namespace ns = Namespaces.getCurrentNamespace();
		
		return new VncFunction(name, params, macro) {
			@Override
			public VncVal apply(final VncList args) {
				final Env localEnv = new Env(env);

				final Namespace curr_ns = Namespaces.getCurrentNamespace();
				try {
					if (!name.equals("macroexpand-all")) {
						// Note:
						// do not switch to the functions own namespace for the function 
						// "core/macroexpand-all". Handle "macroexpand-all" like a special form. 
						// This allows expanding locally defined macros from the REPL without 
						// the need of qualifying them:
						//    > (defmacro bench [expr] ...)
						//    > (macroexpand-all '(bench (+ 1 2))
						// instead of:
						//    > (macroexpand-all '(user/bench (+ 1 2))
						
						Namespaces.setCurrentNamespace(ns);
					}

					// destructuring fn params -> args
					localEnv.addLocalBindings(Destructuring.destructure(params, args));
	
					validateFnPreconditions(name, preConditions, localEnv);
	
					return evaluateBody(body, localEnv);
				}
				finally {
					Namespaces.setCurrentNamespace(curr_ns);
				}
			}
			
			@Override
			public VncVal getBody() {
				return body;
			}
			
			private static final long serialVersionUID = -1L;
		};
	}

	private VncVector getFnPreconditions(final VncVal prePostConditions) {
		if (Types.isVncMap(prePostConditions)) {
			final VncVal val = ((VncMap)prePostConditions).get(PRE_CONDITION_KEY);
			if (Types.isVncVector(val)) {
				return (VncVector)val;
			}
		}
		
		return null;
	}
	
	private boolean isFnConditionTrue(final VncVal result) {
		return Types.isVncSequence(result) 
				? VncBoolean.isTrue(((VncSequence)result).first()) 
				: VncBoolean.isTrue(result);
	}

	private void validateFnPreconditions(
			final String fnName, 
			final VncVector preConditions, 
			final Env env
	) {
		if (preConditions != null && !preConditions.isEmpty()) {
	 		final Env local = new Env(env);	
	 		for(VncVal v : preConditions.getList()) {
				if (!isFnConditionTrue(evaluate(v, local))) {
					try (WithCallStack cs = new WithCallStack(CallFrame.fromVal(fnName, v))) {
						throw new AssertionException(String.format(
								"pre-condition assert failed: %s",
								((VncString)CoreFunctions.str.apply(VncList.of(v))).getValue()));
					}
				}
 			}
		}
	}
	
	private VncVal evaluateBody(final VncList body, final Env env) {
		if (body.isEmpty()) {
			return Constants.Nil;
		}
		else if (body.size() == 1) {
			return evaluate(body.first(), env);
		}
		else if (body.size() == 2) {
			evaluate(body.first(), env);
			return evaluate(body.last(), env);
		}
		else {
			evaluate_values(body.butlast(), env);
			return evaluate(body.last(), env);
		}
	}
	

	/**
	 * Resolves a class name.
	 * 
	 * @param className A simple class name like 'Math' or a class name
	 *                  'java.lang.Math'
	 * @return the mapped class 'Math' -&gt; 'java.lang.Math' or the passed 
	 *         value if a mapping does nor exist 
	 */
	private String resolveClassName(final String className) {
		return Namespaces
					.getCurrentNamespace()
					.getJavaImports()
					.resolveClassName(className);
	}
	
	private void checkInterrupted(final String fnName) {
		if (Thread.currentThread().isInterrupted()) {
			throw new com.github.jlangch.venice.InterruptedException(
						"Interrupted while processing function " + fnName);
		}
	}
	
	private VncSymbol evaluateSymbolMetaData(final VncVal symVal, final Env env) {
		final VncSymbol sym = Coerce.toVncSymbol(symVal);
		ReservedSymbols.validateNotReservedSymbol(sym);
		return sym.withMeta(evaluate(sym.getMeta(), env));
	}

	private static <T> List<T> toEmpty(final List<T> list) {
		return list == null ? new ArrayList<T>() : list;
	}
	
	private VncSymbol qualifySymbolWithCurrNS(final VncSymbol sym) {
		if (sym == null) {
			return null;
		}	
		else if (sym.hasNamespace()) {
			return new VncSymbol(
						sym.getName(),
						MetaUtil.setNamespace(
							sym.getMeta(),
							sym.getNamespace()));
		}
		else {
			final VncSymbol ns = Namespaces.getCurrentNS();
			
			return new VncSymbol(
					Namespaces.isCoreNS(ns)
						? sym.getName()
						: ns.getName() + "/" + sym.getName(), 
					MetaUtil.setNamespace(sym.getMeta(), ns.getName()));
		}
	}
	
	private VncSymbol validateSymbolWithCurrNS(
			final VncSymbol sym,
			final String specialFormName
	) {
		if (sym != null) {
			// do not allow to hijack another namespace
			final String ns = sym.getNamespace();
			if (ns != null && !ns.equals(Namespaces.getCurrentNS().getName())) {
				try (WithCallStack cs = new WithCallStack(CallFrame.fromVal(specialFormName, sym))) {
					throw new VncException(String.format(
							"Special form '%s': Invalid use of namespace. "
								+ "The symbol '%s' can only be defined for the current namespace '%s'.",
								specialFormName,
							sym.getSimpleName(),
							Namespaces.getCurrentNS().toString()));
				}
			}
		}
		
		return sym;
	}
	
	
	private static final long serialVersionUID = -8130740279914790685L;

	private static final VncKeyword PRE_CONDITION_KEY = new VncKeyword(":pre");
	private static final VncSymbol LOADED_MODULES_SYMBOL = new VncSymbol("*loaded-modules*");
	private static final VncSymbol LOADED_FILES_SYMBOL = new VncSymbol("*loaded-files*");
	private static final VncSymbol VERSION_SYMBOL = new VncSymbol("*version*");
	private static final VncSymbol NEWLINE_SYMBOL = new VncSymbol("*newline*");
	private static final VncSymbol LOAD_PATH_SYMBOL = new VncSymbol("*load-path*");
	private static final VncSymbol RUN_MODE_SYMBOL = new VncSymbol("*run-mode*");
	private static final VncSymbol ANSI_TERM_SYMBOL = new VncSymbol("*ansi-term*");
	private static final VncSymbol MACRO_EXPAND_ON_LOAD_SYMBOL = new VncSymbol("*macroexpand-on-load*");

	private static final VncString VERSION = new VncString(Version.VERSION);
	private static final VncString NEWLINE = new VncString(System.lineSeparator());

	private static final Var VERSION_VAR = new Var(VERSION_SYMBOL, VERSION, false);
	private static final Var NEWLINE_VAR = new Var(NEWLINE_SYMBOL, NEWLINE, false);
	private static final Var DISABLED_MACRO_EXPAND_ON_LOAD_SYMBOL_VAR = new Var(MACRO_EXPAND_ON_LOAD_SYMBOL, False, true);
	
	private final IInterceptor interceptor;	
	private final boolean checkSandbox;
	private final List<String> loadPaths;
	private final MeterRegistry meterRegistry;
	private final NamespaceRegistry nsRegistry = new NamespaceRegistry();
	private final CustomWrappableTypes wrappableTypes = new CustomWrappableTypes();
	
	private final AtomicBoolean sealedSystemNS = new AtomicBoolean(false);
}
