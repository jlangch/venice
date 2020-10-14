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
import static com.github.jlangch.venice.impl.types.VncBoolean.True;
import static com.github.jlangch.venice.impl.types.VncFunction.createAnonymousFuncName;

import java.io.Closeable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.github.jlangch.venice.ArityException;
import com.github.jlangch.venice.AssertionException;
import com.github.jlangch.venice.Version;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.env.DynamicVar;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.env.ReservedSymbols;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.functions.CoreFunctions;
import com.github.jlangch.venice.impl.functions.Functions;
import com.github.jlangch.venice.impl.functions.TransducerFunctions;
import com.github.jlangch.venice.impl.javainterop.JavaInterop;
import com.github.jlangch.venice.impl.reader.Reader;
import com.github.jlangch.venice.impl.specialforms.CatchBlock;
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
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncMapEntry;
import com.github.jlangch.venice.impl.types.collections.VncMutableSet;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.collections.VncSet;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.concurrent.ThreadLocalMap;
import com.github.jlangch.venice.impl.types.custom.CustomWrappableTypes;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.CallFrame;
import com.github.jlangch.venice.impl.util.CallStack;
import com.github.jlangch.venice.impl.util.Inspector;
import com.github.jlangch.venice.impl.util.MeterRegistry;
import com.github.jlangch.venice.impl.util.WithCallStack;
import com.github.jlangch.venice.impl.util.reflect.ReflectionAccessor;
import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;
import com.github.jlangch.venice.javainterop.IInterceptor;

/**
 * The Venice interpreter runs the scripts and handles the special forms
 * 
 * 	
 * <p>Tail recursion (loop-recur):		
 * <pre>		
 *   +----------------+-------------------------------------------+---------------+
 *   | Form           | Tail Position                             | recur target? |
 *   +----------------+-------------------------------------------+---------------+
 *   | fn, defn       | (fn [args] expressions tail)              | No            |
 *   | loop           | (loop [bindings] expressions tail)        | Yes           |
 *   | let            | (let [bindings] expressions tail)         | No            |
 *   | do             | (do expressions tail)                     | No            |
 *   | if, if-not     | (if test then tail else tail)             | No            |
 *   | when, when-not | (when test expressions tail)              | No            |
 *   | cond           | (cond test tail ... :else else tail)      | No            |
 *   | case           | (case const tail ... default tail)        | No            |
 *   | or, and        | (or test test ... tail)                   | No            |
 *   +----------------+-------------------------------------------+---------------+
 * </pre>
 */
public class VeniceInterpreter implements Serializable  {

	public VeniceInterpreter(final IInterceptor interceptor) {
		if (interceptor == null) {
			throw new SecurityException("VeniceInterpreter can not run without an interceptor");
		}
		
		this.interceptor = interceptor;
		this.meterRegistry = this.interceptor.getMeterRegistry();
		
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
	
	public void setMacroexpandOnLoad(final boolean macroexpandOnLoad, final Env env) {
		// Dynamically turn on/off macroexpand-on-load. The REPL makes use of this.
		env.setMacroexpandOnLoad(VncBoolean.of(macroexpandOnLoad));		
		this.macroexpand = macroexpandOnLoad;
	}
	
	public boolean isMacroexpandOnLoad() {
		return macroexpand;
	}
	
	// read
	public VncVal READ(final String script, final String filename) {
		if (meterRegistry.enabled) {
			final long nanos = System.nanoTime();
			final VncVal val = Reader.read_str(script, filename);
			meterRegistry.record("venice.read", System.nanoTime() - nanos);
			return val;
		}
		else {
			return Reader.read_str(script, filename);
		}
	}

	public VncVal EVAL(final VncVal ast, final Env env) {
		if (meterRegistry.enabled) {
			final long nanos = System.nanoTime();
			final VncVal val = evaluate(ast, env);
			meterRegistry.record("venice.eval", System.nanoTime() - nanos);			
			return val;
		}
		else {
			return evaluate(ast, env);
		}
	}

	public VncVal MACROEXPAND(final VncVal ast, final Env env) {
		return macroexpand_all(new CallFrame("macroexpand-all", ast.getMeta()), ast, env);
	}

	public VncVal RE(
			final String script, 
			final String name, 
			final Env env
	) {
		VncVal ast = READ(script, name);			
		if (macroexpand) {
			ast = MACROEXPAND(ast, env);			
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
			final RunMode runMode
	) {  
		return createEnv(null, macroexpandOnLoad, ansiTerminal, runMode);
	}

	public Env createEnv(
			final List<String> preloadedExtensionModules,
			final boolean macroexpandOnLoad, 
			final boolean ansiTerminal,
			final RunMode runMode
	) {
		sealedSystemNS.set(false);

		final Env env = new Env(null);
			
		// loaded modules: preset with implicitly preloaded modules
		final VncMutableSet loadedModules = new VncMutableSet(Modules.PRELOADED_MODULES);
		
		for(Map.Entry<VncVal,VncVal> e: Functions.functions.entrySet()) {
			final VncSymbol sym = (VncSymbol)e.getKey();
			final VncFunction fn = (VncFunction)e.getValue();			
			env.setGlobal(new Var(sym, fn, fn.isRedefinable()));
		}

		// set Venice version
		env.setGlobal(new Var(new VncSymbol("*version*"), new VncString(Version.VERSION), false));

		// set system newline
		env.setGlobal( new Var(new VncSymbol("*newline*"), new VncString(System.lineSeparator()), false));

		// ansi terminal (set when run from a REPL in an ASNI terminal)
		env.setGlobal(new Var(new VncSymbol("*ansi-term*"), VncBoolean.of(ansiTerminal), false));
		
		// set the run mode
		env.setGlobal(new Var(new VncSymbol("*run-mode*"), runMode == null ? Constants.Nil : runMode.mode, false));

		// loaded modules & files
		env.setGlobal(new Var(new VncSymbol("*loaded-modules*"), loadedModules, true));
		env.setGlobal(new Var(new VncSymbol("*loaded-files*"), new VncMutableSet(), true));

		// init namespaces
		initNS();

		// Activates macroexpand on load
		//
		// expands macros on behalf of the 'core' functions:   
		//     core/load-string
		//     core/load-file
		//     core/load-classpath-file
		//     core/load-module
		//     VeniceInterpreter::loadModule(..)
		setMacroexpandOnLoad(macroexpandOnLoad, env);

		// load core module
		loadModule("core", env, loadedModules);
		
		// security: seal system namespaces (Namespaces::SYSTEM_NAMESPACES) - no further changes allowed!
		sealedSystemNS.set(true);

		// load other modules requested for preload
		toEmpty(preloadedExtensionModules).forEach(m -> loadModule(m, env, loadedModules));

		return env;
	}
	
	public List<String> getAvailableModules() {
		final List<String> modules = new ArrayList<>(Modules.VALID_MODULES);
		modules.removeAll(Arrays.asList("core", "test", "http", "jackson"));
		Collections.sort(modules);
		return modules;
	}
	
	private void loadModule(
			final String module, 
			final Env env, 
			final VncMutableSet loadedModules
	) {
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
		if (!(ast_ instanceof VncList)) {
			// not an s-expr
			return evaluate_values(ast_, env_);
		}

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
			final VncVal expanded = macroexpand(orig_ast, env, null);
			if (!(expanded instanceof VncList)) {
				// not an s-expr
				return evaluate_values(expanded, env);
			}
			
			final VncList ast = (VncList)expanded;
			if (ast.isEmpty()) { 
				return ast; 
			}
			
			final VncVal a0 = ast.first();		
			final String a0sym = (a0 instanceof VncSymbol) ? ((VncSymbol)a0).getName() : "__<*fn*>__";
			
			switch (a0sym) {		
				case "do": {
						final VncList expressions = ast.rest();
						evaluate_values(expressions.butlast(), env);
						orig_ast = expressions.last();
					}
					break;
					
				case "if": 
					// (if cond expr-true expr-false*)
					final VncVal cond = evaluate(ast.second(), env);
					orig_ast = (VncBoolean.isFalse(cond) || cond == Nil) 
									? ast.fourth()   // eval false slot form (nil if not available)
									: ast.third();   // eval true slot form
					break;
					
				case "let":  { // (let [bindings*] exprs*)
					env = new Env(env);  // let introduces a new environment
	
					final VncVector bindings = Coerce.toVncVector(ast.second());
					final VncList expressions = ast.slice(2);
				
					for(int i=0; i<bindings.size(); i+=2) {
						final VncVal sym = bindings.nth(i);
						final VncVal val = evaluate(bindings.nth(i+1), env);
						env.addLocalVars(Destructuring.destructure(sym, val));
					}
						
					evaluate_values(expressions.butlast(), env);
					orig_ast = expressions.last();
					break;
				}
					
				case "fn":
					// (fn name? [params*] condition-map? expr*)
					return fn_(ast, env);
					
				case "eval":
					return eval_(new CallFrame("eval", a0.getMeta()), ast, env);
					
				case "def":  // (def name value)
					return def_(new CallFrame("def", a0.getMeta()), ast, env);
				
				case "defonce": // (defonce name value)
					return defonce_(new CallFrame("defonce", a0.getMeta()), ast, env);
				
				case "def-dynamic": // (def-dynamic name value)
					return def_dynamic_(new CallFrame("def-dynamic", a0.getMeta()), ast, env);

				case "defmacro":
					return defmacro_(new CallFrame("defmacro", a0.getMeta()), ast, env);
				
				case "deftype": // (deftype type fields validationFn*)
					return deftype_(new CallFrame("deftype", a0.getMeta()), ast, env);
				
				case "deftype?": // (deftype? type)
					return deftypeQ_(new CallFrame("deftype?", a0.getMeta()), ast, env);
				
				case "deftype-of": // (deftype-of type base-type validationFn*)
					return deftype_of_(new CallFrame("deftype-of", a0.getMeta()), ast, env);
				
				case "deftype-or":  // (deftype-or type vals*)
					return deftype_or_(new CallFrame("deftype-or", a0.getMeta()), ast, env);

				case ".:": // (.: type args*)
					return deftype_create_(new CallFrame(".:", a0.getMeta()), ast, env);

				case "defmulti":  // (defmulti name dispatch-fn)
					return defmulti_(new CallFrame("defmulti", a0.getMeta()), ast, env);
				
				case "defmethod": // (defmethod multifn-name dispatch-val & fn-tail)
					return defmethod_(new CallFrame("defmethod", a0.getMeta()), ast, env);
				
				case "ns": // (ns alpha)
					return ns_(new CallFrame("ns", a0.getMeta()), ast, env);
				
				case "ns-remove": // (ns-remove ns)
					return ns_remove_(new CallFrame("ns-remove", a0.getMeta()), ast, env);
				
				case "ns-unmap": // (ns-unmap ns sym)
					return ns_unmap_(new CallFrame("ns-unmap", a0.getMeta()), ast, env);
				
				case "import":
					return import_(new CallFrame("import", a0.getMeta()), ast, env);
					
				case "imports":
					return imports_(new CallFrame("imports", a0.getMeta()), ast, env);

				case "namespace": // (namespace x)
					return namespace_(new CallFrame("namespace", a0.getMeta()), ast, env);
							 	
				case "resolve": // (resolve sym)
					return resolve_(new CallFrame("resolve", a0.getMeta()), ast, env);
				
				case "var-get": // (var-get v)
					return var_get_(new CallFrame("var-get", a0.getMeta()), ast, env);

				case "var-ns": // (var-ns v)
					return var_ns_(new CallFrame("var-ns", a0.getMeta()), ast, env);

				case "var-name": // (var-name v)
					return var_name_(new CallFrame("var-name", a0.getMeta()), ast, env);

				case "var-local?": // (var-local? v)
					return var_localQ_(new CallFrame("var-local?", a0.getMeta()), ast, env);

				case "var-thread-local?": // (var-thread-local? v)
					return var_thread_localQ_(new CallFrame("var-thread-local?", a0.getMeta()), ast, env);

				case "var-global?": // (var-global? v)
					return var_globalQ_(new CallFrame("var-global?", a0.getMeta()), ast, env);

				case "set!": // (set! name expr)
					return setBANG_(new CallFrame("set!", a0.getMeta()), ast, env);
				
				case "inspect": // (inspect sym)
					return inspect_(new CallFrame("inspect", a0.getMeta()), ast, env);

				case "macroexpand": 
					return macroexpand(
							new CallFrame("macroexpand", a0.getMeta()), 
							evaluate(ast.second(), env), 
							env, 
							null);

				case "macroexpand-all*": 
					// Note: This special form is exposed through the public Venice function 
					//       'core/macroexpand-all' in the 'core' module.
					//       The VeniceInterpreter::MACROEXPAND function makes use of it.
					return macroexpand_all(
								new CallFrame("macroexpand-all*", a0.getMeta()), 
								evaluate(ast.second(), env), 
								env);
					
				case "macroexpand-info": 
					return macroexpand_info_(new CallFrame("macroexpand-info", a0.getMeta()), ast, env);
					
				case "quote":
					return ast.second();
					
				case "quasiquote":
					orig_ast = quasiquote(ast.second());
					break;
					
				case "doc": // (doc sym)
					return doc_(new CallFrame("doc", a0.getMeta()), ast, env);
					
				case "modules": // (modules )
					return modules_(new CallFrame("modules", a0.getMeta()), ast, env);
				
				case "binding":  // (binding [bindings*] exprs*)
					return binding_(ast, new Env(env));
					
				case "bound?": // (bound? sym)
					return VncBoolean.of(env.isBound(Coerce.toVncSymbol(evaluate(ast.second(), env))));
				
				case "global-vars-count": // (global-vars-count)
					return new VncLong(env.globalsCount());
					
				case "loop": { // (loop [bindings*] exprs*)
					recursionPoint = null;
					env = new Env(env);
	
					final VncVector bindings = Coerce.toVncVector(ast.second());
					final VncList expressions = ast.slice(2);
					
					final List<VncSymbol> bindingNames = new ArrayList<>(bindings.size() / 2);
					for(int i=0; i<bindings.size(); i+=2) {
						final VncSymbol sym = Coerce.toVncSymbol(bindings.nth(i));
						final VncVal val = evaluate(bindings.nth(i+1), env);
	
						env.setLocal(new Var(sym, val));
						bindingNames.add(sym);
					}
					
					recursionPoint = new RecursionPoint(bindingNames, expressions, env);
					
					// optimization for small loops
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
					if (recursionPoint == null) {
						try (WithCallStack cs = new WithCallStack(new CallFrame("recur", a0.getMeta()))) {
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
							recur_env.setLocal(new Var(recursionPoint.getLoopBindingName(0), evaluate(ast.second(), env)));
							break;
						case 3:
							// [1] calculate the new values
							final VncVal v1 = evaluate(ast.second(), env);
							final VncVal v2 = evaluate(ast.third(), env);
							// [2] bind the new values
							recur_env.setLocal(new Var(recursionPoint.getLoopBindingName(0), v1));
							recur_env.setLocal(new Var(recursionPoint.getLoopBindingName(1), v2));
							break;
						case 4:
							// [1] calculate the new values
							final VncVal v1_ = evaluate(ast.second(), env);
							final VncVal v2_ = evaluate(ast.third(), env);
							final VncVal v3_ = evaluate(ast.fourth(), env);
							// [2] bind the new values
							recur_env.setLocal(new Var(recursionPoint.getLoopBindingName(0), v1_));
							recur_env.setLocal(new Var(recursionPoint.getLoopBindingName(1), v2_));
							recur_env.setLocal(new Var(recursionPoint.getLoopBindingName(2), v3_));
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
								recur_env.setLocal(new Var(recursionPoint.getLoopBindingName(ii), newValues[ii]));
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
					
				case "try": // (try expr (catch :Exception e expr) (finally expr))
					return try_(new CallFrame("try", a0.getMeta()), ast, new Env(env));
					
				case "try-with": // (try-with [bindings*] expr (catch :Exception e expr) (finally expr))
					return try_with_(new CallFrame("try-with", a0.getMeta()), ast, new Env(env));
					
				case "locking":
					return locking_(new CallFrame("locking", a0.getMeta()), ast, env);
					
				case "dorun":
					specialFormCallValidation("dorun");
					return dorun_(new CallFrame("dorun", a0.getMeta()), ast, env);
				
				case "dobench":
					specialFormCallValidation("dobench");
					return dobench_(new CallFrame("dobench", a0.getMeta()), ast, env);
										
				case "prof":
					specialFormCallValidation("prof");
					return prof_(new CallFrame("prof", a0.getMeta()), ast, env);

				default:				
					final VncList el = (VncList)evaluate_values(ast, env);
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
						
						// Automatic TCO (tail call optimization)
						if (isAutoTCO() 
								&& !fn.emptyBody()  // no VncMultiArityFunction, VncMultiFunction
								&& !callStack.isEmpty() 
								&& fnName.equals(callStack.peek().getFnName())
						) {
							// currently there is no tail position check
							env.addLocalVars(Destructuring.destructure(fn.getParams(), elArgs));
							final VncList body = (VncList)fn.getBody();
							evaluate_values(body.butlast(), env);
							orig_ast = body.last();
						}
						else {
							// invoke function with a new call frame
							// Note: the overhead with callstack and interrupt check is ~150ns
							try {
								callStack.push(new CallFrame(fn.getQualifiedName(), a0.getMeta()));								
								return fn.apply(elArgs);
							}
							finally {
								callStack.pop();
								checkInterrupted(fnName);
								if (checkSandbox) {
									interceptor.validateMaxExecutionTime();
								}
								if (meterRegistry.enabled) {
									final long elapsed = System.nanoTime() - nanos;
									meterRegistry.record(fn.getQualifiedName(), elapsed);
								}
							}
						}
					}
					else if (elFirst instanceof IVncFunction) {
						// 1)  keyword as function to access maps: (:a {:a 100})
						// 2)  a map as function to deliver its value for a key: ({:a 100} :a)
						return ((IVncFunction)elFirst).apply(elArgs);
					}
					else {
						try (WithCallStack cs = new WithCallStack(new CallFrame(a0sym, a0.getMeta()))) {
							throw new VncException(String.format(
									"Expected a function or keyword/set/map/vector as "
										+ "s-expression  symbol value but got a value "
										+ "of type '%s'!", 
									Types.getType(elFirst)));
						}
					}
					break;
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
					final List<VncVal> vals = new ArrayList<>(seq.size());
					for(int ii=0; ii<seq.size(); ii++) {
						vals.add(evaluate(seq.nth(ii), env));
			 		}
					return seq.withValues(vals);
			}
		}
		else if (ast instanceof VncMap) {
			final VncMap map = (VncMap)ast;
			
			final Map<VncVal,VncVal> vals = new HashMap<>(map.size());
			for(Entry<VncVal,VncVal> e: map.getMap().entrySet()) {
				vals.put(
					evaluate(e.getKey(), env), 
					evaluate(e.getValue(), env));
			}
			return map.withValues(vals);
		} 
		else if (ast instanceof VncSet) {
			final VncSet set = (VncSet)ast;
			
			final List<VncVal> vals = new ArrayList<>(set.size());
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
	private VncVal macroexpand(
			final VncVal ast, 
			final Env env,
			final AtomicInteger expandedMacrosCounter
	) {
		final long nanos = meterRegistry.enabled ? System.nanoTime() : 0L;
		
		VncVal ast_ = ast;
		int expandedMacros = 0;
		
		while (ast_ instanceof VncList) {
			final VncVal a0 = ((VncList)ast_).first();
			if (!(a0 instanceof VncSymbol)) break;
			
			final VncVal fn = env.getGlobalOrNull((VncSymbol)a0);
			if (!(fn != null && fn instanceof VncFunction && ((VncFunction)fn).isMacro())) break;
			
			final VncFunction macro = (VncFunction)fn;
			
			final VncList macroArgs = ((VncList)ast_).rest();

			// validate that the macro is allowed by the sandbox
			if (checkSandbox) {
				interceptor.validateVeniceFunction(macro.getQualifiedName());					
			}
			
			expandedMacros++; 

			if (meterRegistry.enabled) {
				final long nanosRun = System.nanoTime();
				
				ast_ = macro.apply(macroArgs);
				
				meterRegistry.record(macro.getQualifiedName() + "[m]", System.nanoTime() - nanosRun);
			}
			else {
				ast_ = macro.apply(macroArgs);
			}
		}
	 
		if (expandedMacros > 0) {
			macroExpandCount.addAndGet(expandedMacros);
			if (meterRegistry.enabled) {
				meterRegistry.record("macroexpand", System.nanoTime() - nanos);
			}
		}
		
		if (expandedMacrosCounter != null) {
			expandedMacrosCounter.addAndGet(expandedMacros);
		}

		return ast_;
	}

	private VncVal macroexpand(
			final CallFrame callframe, 
			final VncVal ast, 
			final Env env,
			final AtomicInteger expandedMacrosCounter
	) {
		try (WithCallStack cs = new WithCallStack(callframe)) {
			return macroexpand(ast, env, expandedMacrosCounter);
		}		
	}

	/**
	 * Expands recursively all macros in the form.
	 * 
	 * <p>An approach with <code>core/prewalk</code> does not work, because
	 * this function does not apply namespaces (remember macros are always 
	 * executed in the namespace of the caller as opposed to functions that
	 * are executed in the namespace they are defined in).
	 * 
	 * <p>With <code>core/prewalk</code> we cannot execute <code>(ns x)</code>
	 * because the functions involved like <code>core/walk</code> and 
	 * <code>core/partial</code> will reset the changed namespace upon leaving 
	 * its body.
	 * 
	 * <pre>
	 *     (core/prewalk (fn [x] (if (list? x) (macroexpand x) x)) form)
	 * </pre>
	 * 
	 * <p>Note: only macros that have already been parsed in another parse unit 
	 *          can be expanded! 
	 *          'macroexpand-all' is not an interpreter thus it cannot not 
	 *          run macro definitions and put them to the symbol table for later
	 *          reference!
	 * 
	 * @param form the form to expand
	 * @param env the env
	 * @return the expanded form
	 */
	private VncVal macroexpand_all(
			final CallFrame callframe, 
			final VncVal form, 
			final Env env
	) {
		try (WithCallStack cs = new WithCallStack(callframe)) {
			final AtomicInteger expandedMacroCounter = new AtomicInteger(0);
	
			final VncFunction handler = new VncFunction(createAnonymousFuncName("macroexpand-all-handler")) {
				public VncVal apply(final VncList args) {
					final VncVal form = args.first();
					
					if (Types.isVncList(form)) {
						final VncList list = (VncList)form;
						final VncVal first = list.first();
						if (Types.isVncSymbol(first)) {
							final VncVal second = list.second();
							// check if the expression is of the form (ns x)
							if (list.size() == 2 
									&& "ns".equals(((VncSymbol)first).getName()) 
									&& second instanceof VncSymbol
							) {
								// we've encountered a '(ns x)' symbolic expression -> apply it
								Namespaces.setCurrentNamespace(nsRegistry.computeIfAbsent((VncSymbol)second)); 
							}
							else {
								// try to expand
								return macroexpand(list, env, expandedMacroCounter);
							}
						}
					}
	
					return form;
				}
				private static final long serialVersionUID = -1L;
			};
	
			final VncFunction walk = new VncFunction(createAnonymousFuncName("macroexpand-all-walk")) {
				// Java implementation of 'core/walk' from 'core' module with
				// the optimization for 'outer' function as 'identity' for 'core/prewalk'
				public VncVal apply(final VncList args) {
					final VncFunction inner = (VncFunction)args.first();
					final VncVal form = args.second();
					
					if (Types.isVncList(form)) {
						// (outer (apply list (map inner form)))
						return TransducerFunctions.map.applyOf(inner, form);
					}
					else if (Types.isVncMapEntry(form)) {
						// (outer (map-entry (inner (key form)) (inner (val form))))
						return CoreFunctions.new_map_entry.applyOf(
									inner.applyOf(((VncMapEntry)form).getKey()), 
									inner.applyOf(((VncMapEntry)form).getValue()));
					}
					else if (Types.isVncCollection(form)) {
						// (outer (into (empty form) (map inner form)))
						return CoreFunctions.into.applyOf(
									CoreFunctions.empty.applyOf(form), 
									TransducerFunctions.map.applyOf(inner, form));
					}
					else {
						// (outer form)
						return form;
					}
				}
				private static final long serialVersionUID = -1L;
			};
	
			final VncFunction prewalk = new VncFunction(createAnonymousFuncName("macroexpand-all-prewalk")) {
				// Java implementation of 'core/prewalk' from 'core' module
				public VncVal apply(final VncList args) {
					final VncFunction f = (VncFunction)args.first();
					final VncVal form = args.second();
	
					return walk.applyOf(
								CoreFunctions.partial.applyOf(this, f),
								f.applyOf(form));
				}
				private static final long serialVersionUID = -1L;
			};
	
			
			final Namespace original_ns = Namespaces.getCurrentNamespace();
			try {
				final VncVal expanded = prewalk.applyOf(handler, form);
			
				// the number of expanded macros in this 'macroexpand-all' run
				final int count = expandedMacroCounter.get(); 
				if (count == 0) {
					macroExpandAllCount.incrementAndGet();
				}
				else {
					macroExpandAllCount.incrementAndGet();
					macroExpandAllCountEffective.incrementAndGet();
				}
				return expanded;
			}
			finally {
				// set the original namespace back
				Namespaces.setCurrentNamespace(original_ns);
			}
		}
	}
	
	private static boolean isNonEmptySequence(final VncVal x) {
		return Types.isVncSequence(x) && !((VncSequence)x).isEmpty();
	}

	private static VncVal quasiquote(final VncVal ast) {
		if (isNonEmptySequence(ast)) {
			final VncVal a0 = Coerce.toVncSequence(ast).first();
			if (Types.isVncSymbol(a0) && ((VncSymbol)a0).getName().equals("unquote")) {
				return ((VncSequence)ast).second();
			} 
			else if (isNonEmptySequence(a0)) {
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
		else {
			return VncList.of(new VncSymbol("quote"), ast);
		}
	}
	
	private VncFunction defmacro_(final VncList ast, final Env env) {
		int argPos = 1;
		
		final VncSymbol macroName = qualifySymbolWithCurrNS(
										evaluateSymbolMetaData(ast.nth(argPos++), env));
		VncVal meta = macroName.getMeta();
		
		if (MetaUtil.isPrivate(meta)) {
			throw new VncException(String.format(
					"The macro '%s' must not be defined as private! "
						+ "Venice does not support private macros.",
					macroName.getName()));
		}
		
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

			final VncList body = ast.slice(argPos);
	
			final VncFunction macroFn = buildFunction(
											macroName_.getName(), 
											params, 
											body, 
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
							true,
							env));
			});

			final VncFunction macroFn = new VncMultiArityFunction(macroName_.getName(), fns, true).withMeta(meta);
			
			env.setGlobal(new Var(macroName_, macroFn, false));

			return macroFn;
		}
	}

	private VncVal def_(final CallFrame callframe, final VncList ast, final Env env) {
		try (WithCallStack cs = new WithCallStack(callframe)) {
			assertArity("def", ast.rest(), 1, 2);
			final VncSymbol name = validateSymbolWithCurrNS(
										qualifySymbolWithCurrNS(
												evaluateSymbolMetaData(ast.second(), env)),
										"def");
			
			final VncVal val = ast.third();
			
			final VncVal res = evaluate(val, env).withMeta(name.getMeta());
			env.setGlobal(new Var(name, res, true));
			return name;
		}				
	}

	private VncVal defonce_(final CallFrame callframe, final VncList ast, final Env env) {
		try (WithCallStack cs = new WithCallStack(callframe)) {
			assertArity("defonce", ast.rest(), 1, 2);
			final VncSymbol name = validateSymbolWithCurrNS(
										qualifySymbolWithCurrNS(
												evaluateSymbolMetaData(ast.second(), env)),
										"defonce");
							
			final VncVal val = ast.third();

			final VncVal res = evaluate(val, env).withMeta(name.getMeta());
			env.setGlobal(new Var(name, res, false));
			return name;
		}
	}

	private VncVal def_dynamic_(final CallFrame callframe, final VncList ast, final Env env) {
		try (WithCallStack cs = new WithCallStack(callframe)) {
			assertArity("def-dynamic", ast.rest(), 1, 2);
			final VncSymbol name = validateSymbolWithCurrNS(
										qualifySymbolWithCurrNS(
												evaluateSymbolMetaData(ast.second(), env)),
										"def-dynamic");
			
			final VncVal val = ast.third();
			
			final VncVal res = evaluate(val, env).withMeta(name.getMeta());
			env.setGlobalDynamic(name, res);
			return name;
		}
	}

	private VncVal defmacro_(final CallFrame callframe, final VncList ast, final Env env) {
		try (WithCallStack cs = new WithCallStack(callframe)) {
			assertMinArity("defmacro", ast.rest(), 2);
			return defmacro_(ast, env);
		}
	}

	private VncVal deftype_(final CallFrame callframe, final VncList ast, final Env env) {
		try (WithCallStack cs = new WithCallStack(callframe)) {
			assertArity("deftype", ast.rest(), 2, 3);
			final VncKeyword type = Coerce.toVncKeyword(evaluate(ast.second(), env));
			final VncVector fields = Coerce.toVncVector(ast.third());
			final VncFunction validationFn = ast.size() == 4
												? Coerce.toVncFunction(evaluate(ast.fourth(), env))
												: null;

			return DefTypeForm.defineCustomType(type, fields, validationFn, this::RE, env);
		}
	}

	private VncVal deftypeQ_(final CallFrame callframe, final VncList ast, final Env env) {
		try (WithCallStack cs = new WithCallStack(callframe)) {
			assertArity("deftype?", ast.rest(), 1);
			final VncVal type = evaluate(ast.second(), env);
			return VncBoolean.of(DefTypeForm.isCustomType(type, env));
		}
	}

	private VncVal deftype_of_(final CallFrame callframe, final VncList ast, final Env env) {
		try (WithCallStack cs = new WithCallStack(callframe)) {
			assertMinArity("deftype-of", ast.rest(), 2);
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

	private VncVal deftype_or_(final CallFrame callframe, final VncList ast, final Env env) {
		try (WithCallStack cs = new WithCallStack(callframe)) {
			assertMinArity("deftype-or", ast.rest(), 2);
			final VncKeyword type = Coerce.toVncKeyword(evaluate(ast.second(), env));
			final VncList choiceVals = ast.slice(2);

			return DefTypeForm.defineCustomChoiceType(type, choiceVals, this::RE, env);
		}
	}

	private VncVal deftype_create_(final CallFrame callframe, final VncList ast, final Env env) {
		try (WithCallStack cs = new WithCallStack(callframe)) {
			assertMinArity(".:", ast.rest(), 1);
			final List<VncVal> args = new ArrayList<>();
	 		final Iterator<VncVal> iter = ast.rest().iterator();
	 		while (iter.hasNext()) {
	 			final VncVal v = iter.next();
				args.add(evaluate(v, env));
			}
			return DefTypeForm.createType(args, env);
		}
	}

	private VncVal defmulti_(final CallFrame callframe, final VncList ast, final Env env) {
		try (WithCallStack cs = new WithCallStack(callframe)) {
			assertArity("defmulti", ast.rest(), 2);
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
			env.setGlobal(new Var(name, multiFn, true));
			return multiFn;
		}
	}

	private VncVal defmethod_(final CallFrame callframe, final VncList ast, final Env env) {
		try (WithCallStack cs = new WithCallStack(callframe)) {
			final VncSymbol multiFnName = qualifySymbolWithCurrNS(
											Coerce.toVncSymbol(ast.second()));
			final VncVal multiFnVal = env.getGlobalOrNull(multiFnName);
			if (multiFnVal == null) {
				throw new VncException(String.format(
							"No multifunction '%s' defined for the method definition", 
							multiFnName.getName())); 
			}
			final VncMultiFunction multiFn = Coerce.toVncMultiFunction(multiFnVal);
			final VncVal dispatchVal = ast.third();
			
			final VncVector params = Coerce.toVncVector(ast.fourth());
			if (params.size() != multiFn.getParams().size()) {
				throw new VncException(String.format(
						"A method definition for the multifunction '%s' must have %d parameters", 
						multiFnName.getName(),
						multiFn.getParams().size()));
			}
			final VncVector preConditions = getFnPreconditions(ast.nth(4), env);
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
	}

	private VncVal ns_(final CallFrame callframe, final VncList ast, final Env env) {
		try (WithCallStack cs = new WithCallStack(callframe)) {
			specialFormCallValidation("ns");

			final VncVal name = ast.second();
			final VncSymbol ns = Types.isVncSymbol(name)
									? (VncSymbol)name
									: (VncSymbol)CoreFunctions.symbol.apply(VncList.of(evaluate(name, env)));
			
			if (ns.hasNamespace()) {
				throw new VncException(String.format(
						"A namespace '%s' must not have itself a namespace! However you can use '%s'.",
						ns.getQualifiedName(),
						ns.getNamespace() + "." + ns.getSimpleName()));
			}
			else {
				if (Namespaces.isSystemNS(ns.getName()) && sealedSystemNS.get()) {
					// prevent Venice's system namespaces from being altered
					throw new VncException("Namespace '" + ns.getName() + "' cannot be reopened!");
				}
				Namespaces.setCurrentNamespace(nsRegistry.computeIfAbsent(ns));
				return ns;
			}
		}
	}
	
	private VncVal ns_remove_(final CallFrame callframe, final VncList ast, final Env env) {
		try (WithCallStack cs = new WithCallStack(callframe)) {
			specialFormCallValidation("ns-remove");

			final VncSymbol ns = Namespaces.lookupNS(ast.second(), env);
			if (Namespaces.isSystemNS(ns.getName()) && sealedSystemNS.get()) {
				// prevent Venice's system namespaces from being altered
				throw new VncException("Namespace '" + ns.getName() + "' cannot be removed!");
			}
			else {
				env.removeGlobalSymbolsByNS(ns);
				nsRegistry.remove(ns);
				return Nil;
			}
		}
	}
	
	private VncVal ns_unmap_(final CallFrame callframe, final VncList ast, final Env env) {
		try (WithCallStack cs = new WithCallStack(callframe)) {
			specialFormCallValidation("ns-unmap");

			final VncSymbol ns = Namespaces.lookupNS(ast.second(), env);
			if (Namespaces.isSystemNS(ns.getName()) && sealedSystemNS.get()) {
				// prevent Venice's system namespaces from being altered
				throw new VncException("Cannot remove a symbol from namespace '" + ns.getName() + "'!");
			}
			else {
				final VncSymbol sym = Coerce.toVncSymbol(ast.third()).withNamespace(ns);
				env.removeGlobalSymbol(sym);
				return Nil;
			}
		}
	}

	private VncVal import_(final CallFrame callframe, final VncList ast, final Env env) {
		try (WithCallStack cs = new WithCallStack(callframe)) {
			ast.rest().forEach(i -> Namespaces
										.getCurrentNamespace()
										.getJavaImports()
										.add(Coerce.toVncString(i).getValue()));
			return Nil;
		}
	}

	private VncVal imports_(final CallFrame callframe, final VncList ast, final Env env) {
		try (WithCallStack cs = new WithCallStack(callframe)) {
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
					throw new VncException(String.format(
						"The namespace '%s' does not exist", ns.toString()));
				}
			}
		}
	}

	private VncVal namespace_(final CallFrame callframe, final VncList ast, final Env env) {
		final VncVal val = evaluate(ast.second(), env);
		if (val instanceof INamespaceAware) {
			return new VncString(((INamespaceAware)val).getNamespace());
		}
		else {
			try (WithCallStack cs = new WithCallStack(callframe)) {
				throw new VncException(String.format(
						"The type '%s' does not support namespaces!",
						Types.getType(val)));
			}
		}
	}

	private VncVal resolve_(final CallFrame callframe, final VncList ast, final Env env) {
		specialFormCallValidation("resolve");
		return env.getOrNil(Coerce.toVncSymbol(evaluate(ast.second(), env)));
	}

	private VncVal var_get_(final CallFrame callframe, final VncList ast, final Env env) {
		specialFormCallValidation("var-get");
		final VncSymbol sym = Types.isVncSymbol(ast.second())
								? (VncSymbol)ast.second()
								: Coerce.toVncSymbol(evaluate(ast.second(), env));
		return env.getOrNil(sym);
	}

	private VncVal var_ns_(final CallFrame callframe, final VncList ast, final Env env) {
		specialFormCallValidation("var-ns");
		final VncSymbol sym = Types.isVncSymbol(ast.second())
								? (VncSymbol)ast.second()
								: Coerce.toVncSymbol(evaluate(ast.second(), env));
		final String ns = env.getNamespace(sym);
		return ns == null ? Nil : new VncString(ns);
	}

	private VncVal var_name_(final CallFrame callframe, final VncList ast, final Env env) {
		specialFormCallValidation("var-name");
		final VncSymbol sym = Types.isVncSymbol(ast.second())
								? (VncSymbol)ast.second()
								: Coerce.toVncSymbol(evaluate(ast.second(), env));
		return new VncString(sym.getName());
	}

	private VncVal var_localQ_(final CallFrame callframe, final VncList ast, final Env env) {
		final VncSymbol sym = Types.isVncSymbol(ast.second())
								? (VncSymbol)ast.second()
								: Coerce.toVncSymbol(evaluate(ast.second(), env));
		return VncBoolean.of(env.isLocal(sym));
	}

	private VncVal var_thread_localQ_(final CallFrame callframe, final VncList ast, final Env env) {
		final VncSymbol sym = Types.isVncSymbol(ast.second())
								? (VncSymbol)ast.second()
								: Coerce.toVncSymbol(evaluate(ast.second(), env));
		return VncBoolean.of(env.isThreadLocal(sym));
	}

	private VncVal var_globalQ_(final CallFrame callframe, final VncList ast, final Env env) {
		final VncSymbol sym = Types.isVncSymbol(ast.second())
								? (VncSymbol)ast.second()
								: Coerce.toVncSymbol(evaluate(ast.second(), env));
		return VncBoolean.of(env.isGlobal(sym));
	}

	private VncVal setBANG_(final CallFrame callframe, final VncList ast, final Env env) {
		specialFormCallValidation("set!");

		final VncSymbol sym = Types.isVncSymbol(ast.second())
								? (VncSymbol)ast.second()
								: Coerce.toVncSymbol(evaluate(ast.second(), env));
		final Var globVar = env.getGlobalVarOrNull(sym);
		if (globVar != null) {
			final VncVal expr = ast.third();
			final VncVal val = evaluate(expr, env);
			
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
			try (WithCallStack cs = new WithCallStack(callframe)) {
				throw new VncException(String.format(
							"The global or thread-local var '%s' does not exist!", 
							sym.getName()));
			}
		}
	}

	private VncVal inspect_(final CallFrame callframe, final VncList ast, final Env env) {
		specialFormCallValidation("inspect");
		final VncSymbol sym = Coerce.toVncSymbol(evaluate(ast.second(), env));
		return Inspector.inspect(env.get(sym));
	}

	private VncVal macroexpand_info_(final CallFrame callframe, final VncList ast, final Env env) {
		return VncHashMap.of(
				new VncKeyword("macroexpand-count"),
				new VncLong(macroExpandCount.get()),
				new VncKeyword("macroexpand-all-count"),
				new VncLong(macroExpandAllCount.get()),
				new VncKeyword("macroexpand-all-count-effective"),
				new VncLong(macroExpandAllCountEffective.get()));
	}
	
	private VncVal doc_(final CallFrame callframe, final VncList ast, final Env env) {
		try (WithCallStack cs = new WithCallStack(callframe)) {
			final VncString doc = DocForm.doc(ast.second(), env);
			evaluate(VncList.of(new VncSymbol("println"), doc), env);
			return Nil;
		}
	}

	private VncVal modules_(final CallFrame callframe, final VncList ast, final Env env) {
		try (WithCallStack cs = new WithCallStack(callframe)) {
			return VncList.ofList(
						Modules
							.VALID_MODULES
							.stream()
							.filter(s ->!s.equals("core"))  // skip core module
							.sorted()
							.map(s -> new VncKeyword(s))
							.collect(Collectors.toList()));
		}
	}

	private VncVal eval_(final CallFrame callframe, final VncList ast, final Env env) {
		specialFormCallValidation("eval");
		final Namespace ns = Namespaces.getCurrentNamespace();
		try {
			return evaluate(Coerce.toVncSequence(evaluate_values(ast.rest(), env)).last(), env);
		}
		finally {
			Namespaces.setCurrentNamespace(ns);
		}
	}
	
	private VncVal dorun_(final CallFrame callframe, final VncList ast, final Env env) {
		try (WithCallStack cs = new WithCallStack(callframe)) {
			if (ast.size() != 3) {
				throw new VncException("dorun requires two arguments a count and an expression to run");
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
	}

	private VncVal dobench_(final CallFrame callframe, final VncList ast, final Env env) {
		try (WithCallStack cs = new WithCallStack(callframe)) {
			if (ast.size() != 3) {
				throw new VncException("dobench requires two arguments a count and an expression to run");
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
	}

	private VncVal locking_(final CallFrame callframe, final VncList ast, final Env env) {
		try (WithCallStack cs = new WithCallStack(callframe)) {
			if (ast.size() < 3) {
				throw new VncException("locking requires a lockee and one or more expressions to run");
			}
			
			final VncVal mutex = evaluate(ast.second(), env);
	
			synchronized(mutex) {
				return evaluateBody(ast.slice(2), env);
			}
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
			
			final VncVector preCon = getFnPreconditions(ast.nth(argPos), env);
			if (preCon != null) argPos++;
			
			final VncList body = ast.slice(argPos);
			
//			if (macroexpand) {
//				return buildFunction(
//						fnName.getName(), 
//						params, 
//						(VncList)macroexpand_all(body, env), 
//						preCon == null ? null : (VncVector)macroexpand_all(preCon, env), 
//						false, 
//						env);
//			}
//			else {
				return buildFunction(fnName.getName(), params, body, preCon, false, env);
//			}		
		}
		else {
			// multi arity:

			final List<VncFunction> fns = new ArrayList<>();
			
			ast.slice(argPos).forEach(s -> {
				int pos = 0;
				
				final VncList sig = Coerce.toVncList(s);
				
				final VncVector params = Coerce.toVncVector(sig.nth(pos++));
				
				final VncVector preCon = getFnPreconditions(sig.nth(pos), env);
				if (preCon != null) pos++;
				
				final VncList body = sig.slice(pos);
				
//				if (macroexpand) {
//					fns.add(buildFunction(
//							fnName.getName(), 
//							params, 
//							(VncList)macroexpand_all(body, env), 
//							preCon == null ? null : (VncVector)macroexpand_all(preCon, env), 
//							false, 
//							env));
//				}
//				else {
					fns.add(buildFunction(fnName.getName(), params, body, preCon, false, env));
//				}
			});
			
			return new VncMultiArityFunction(fnName.getName(), fns, false);
		}
	}

	private VncVal prof_(final CallFrame callframe, final VncList ast, final Env env) {
		try (WithCallStack cs = new WithCallStack(callframe)) {
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
						final VncVal opt1 = ast.third();
						final VncVal opt2 = ast.fourth();
						
						String title = "Metrics";
						if (Types.isVncString(opt1) && !Types.isVncKeyword(opt1)) title = ((VncString)opt1).getValue();
						if (Types.isVncString(opt2) && !Types.isVncKeyword(opt2)) title = ((VncString)opt2).getValue();
	
						boolean anonFn = false;
						if (Types.isVncKeyword(opt1)) anonFn = anonFn || ((VncKeyword)opt1).hasValue("anon-fn");
						if (Types.isVncKeyword(opt2)) anonFn = anonFn || ((VncKeyword)opt2).hasValue("anon-fn");
	
						return new VncString(meterRegistry.getTimerDataFormatted(title, anonFn));
				}
			}
	
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
	
			vars.addAll(Destructuring.destructure(sym, val));
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

	private VncVal try_(final CallFrame callframe, final VncList ast, final Env env) {
		try (WithCallStack cs = new WithCallStack(callframe)) {
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
					env.setLocal(new Var(catchBlock.getExSym(), new VncJavaObject(th)));			
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
	}

	private VncVal try_with_(final CallFrame callframe, final VncList ast, final Env env) {
		try (WithCallStack cs = new WithCallStack(callframe)) {
			final VncSequence bindings = Coerce.toVncSequence(ast.second());
			final List<Var> boundResources = new ArrayList<>();
			
			for(int i=0; i<bindings.size(); i+=2) {
				final VncVal sym = bindings.nth(i);
				final VncVal val = evaluate(bindings.nth(i+1), env);
	
				if (Types.isVncSymbol(sym)) {
					final Var binding = new Var((VncSymbol)sym, val);
					env.setLocal(binding);
					boundResources.add(binding);
				}
				else {
					throw new VncException(
							String.format(
									"Invalid 'try-with' destructuring symbol value type %s. Expected symbol.",
									Types.getType(sym)));
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
						env.setLocal(new Var(catchBlock.getExSym(), new VncJavaObject(th)));
					
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
					final VncVal resource = b.getVal();
					if (Types.isVncJavaObject(resource)) {
						final Object r = ((VncJavaObject)resource).getDelegate();
						if (r instanceof AutoCloseable) {
							try {
								((AutoCloseable)r).close();
							}
							catch(Exception ex) {
								throw new VncException(
										String.format(
												"'try-with' failed to close resource %s.",
												b.getName()));
							}
						}
						else if (r instanceof Closeable) {
							try {
								((Closeable)r).close();
							}
							catch(Exception ex) {
								throw new VncException(
										String.format(
												"'try-with' failed to close resource %s.",
												b.getName()));
							}
						}
					}
				});
			}
			
			return result;
		}
	}

	private VncList getTryBody(final VncList ast) {
		final List<VncVal> body = new ArrayList<>();
 		final Iterator<VncVal> iter = ast.rest().iterator();
 		while (iter.hasNext()) {
 			final VncVal e = iter.next();
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
 		final Iterator<VncVal> iter = blocks.iterator();
 		while (iter.hasNext()) {
 			final VncVal b = iter.next();
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
 		final Iterator<VncVal> iter = blocks.iterator();
 		while (iter.hasNext()) {
 			final VncVal b = iter.next();
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
		// the namespace the function/macro is defined for
		final Namespace ns = Namespaces.getCurrentNamespace();
		
		// Note: Do not switch to the functions own namespace for the function 
		//       "core/macroexpand-all". Handle "macroexpand-all" like a special 
		//       form.This allows expanding locally defined macros from the REPL 
		//       without the need of qualifying them:
		//          > (defmacro bench [expr] ...)
		//          > (macroexpand-all '(bench (+ 1 2))
		//       instead of:
		//          > (macroexpand-all '(user/bench (+ 1 2))
		final boolean switchToFunctionNamespaceAtRuntime = !macro && !name.equals("macroexpand-all");

		// Destructuring optimization for function parameters
		final boolean plainSymbolParams = Destructuring.isFnParamsWithoutDestructuring(params);

		// PreCondition optimization
		final boolean hasPreConditions = preConditions != null && !preConditions.isEmpty();

		return new VncFunction(name, params, macro) {
			@Override
			public VncVal apply(final VncList args) {
				final Env localEnv = new Env(env);

				addFnArgsToEnv(args, localEnv);

				if (switchToFunctionNamespaceAtRuntime) {
					final ThreadLocalMap threadLocalMap = ThreadLocalMap.get();
					
					final Namespace curr_ns = threadLocalMap.getCurrentNS();
					try {
						threadLocalMap.setCurrentNS(ns);
						
						if (hasPreConditions) {
							validateFnPreconditions(localEnv);
						}
						return evaluateBody(body, localEnv);
					}
					finally {
						// switch always back to curr namespace, just in case (ns xyz)
						// was executed within the function body!
						threadLocalMap.setCurrentNS(curr_ns);
					}
				}
				else {
					if (hasPreConditions) {
						validateFnPreconditions(localEnv);
					}
					return evaluateBody(body, localEnv);
				}
			}
			
			@Override
			public VncVal getBody() {
				return body;
			}

			private void addFnArgsToEnv(final VncList args, final Env env) {
				// destructuring fn params -> args
				if (plainSymbolParams) {
					for(int ii=0; ii<params.size(); ii++) {
						env.setLocal(
							new Var((VncSymbol)params.nth(ii), args.nthOrDefault(ii, Nil)));
					}
				}
				else {
					env.addLocalVars(Destructuring.destructure(params, args));	
				}
			}

			private void validateFnPreconditions(final Env env) {
				if (preConditions != null && !preConditions.isEmpty()) {
			 		final Env local = new Env(env);	
			 		final Iterator<VncVal> iter = preConditions.iterator();
			 		while (iter.hasNext()) {
			 			final VncVal v = iter.next();
						if (!isFnConditionTrue(evaluate(v, local))) {
							try (WithCallStack cs = new WithCallStack(new CallFrame(name, v.getMeta()))) {
								throw new AssertionException(String.format(
										"pre-condition assert failed: %s",
										((VncString)CoreFunctions.str.apply(VncList.of(v))).getValue()));
							}
						}
		 			}
				}
			}

			private static final long serialVersionUID = -1L;
		};
	}

	private VncVector getFnPreconditions(final VncVal prePostConditions, final Env env) {
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
	
	private VncVal evaluateBody(final VncList body, final Env env) {
		if (body.isEmpty()) {
			return Constants.Nil;
		}
		else if (body.size() == 1) {
			return evaluate(body.first(), env);
		}
		else if (body.size() == 2) {
			evaluate(body.first(), env);
			return evaluate(body.second(), env);
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
						MetaUtil.setNamespace(sym.getMeta(), sym.getNamespace()));
		}
		else {
			final VncSymbol ns = Namespaces.getCurrentNS();			
			final VncVal newMeta = MetaUtil.setNamespace(sym.getMeta(), ns.getName());
			
			return Namespaces.isCoreNS(ns)
					? new VncSymbol(sym.getName(), newMeta)
					: new VncSymbol(ns.getName(), sym.getName(), newMeta);
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
				try (WithCallStack cs = new WithCallStack(new CallFrame(specialFormName, sym.getMeta()))) {
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
	
	private void assertArity(final String name, final VncList args, final int... expectedArities) {
		final int arity = args.size();
		for (int ii=0; ii<expectedArities.length; ii++) {
			if (expectedArities[ii] == arity) return;
		}		
		throw new ArityException(arity, name);
	}
	
	private void assertMinArity(final String name, final VncList args, final int minArity) {
		final int arity = args.size();
		if (arity < minArity) {
			throw new ArityException(arity, name);
		}
	}
	
	private void specialFormCallValidation(final String name) {
		JavaInterop.getInterceptor().validateVeniceFunction(name);
	}

	private boolean isAutoTCO() {
		// Currently I don't see a major advantage of automated tail call optimization 
		// compared to self-recursion using loop - recur. The code of the latter even
		// looks cleaner
		return false;
	}

	
	private static final long serialVersionUID = -8130740279914790685L;

	private static final VncKeyword PRE_CONDITION_KEY = new VncKeyword(":pre");
		
	private final IInterceptor interceptor;	
	private final boolean checkSandbox;
	private final MeterRegistry meterRegistry;
	private final NamespaceRegistry nsRegistry = new NamespaceRegistry();
	private final CustomWrappableTypes wrappableTypes = new CustomWrappableTypes();
	
	private final AtomicBoolean sealedSystemNS = new AtomicBoolean(false);
	private final AtomicLong macroExpandAllCountEffective = new AtomicLong(0L);
	private final AtomicLong macroExpandAllCount = new AtomicLong(0L);
	private final AtomicLong macroExpandCount = new AtomicLong(0L);
	
	private volatile boolean macroexpand = false;
}
