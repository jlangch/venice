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
package com.github.jlangch.venice.impl;

import static com.github.jlangch.venice.impl.debug.breakpoint.FunctionScope.FunctionEntry;
import static com.github.jlangch.venice.impl.types.Constants.Nil;
import static com.github.jlangch.venice.impl.types.VncBoolean.True;
import static com.github.jlangch.venice.impl.types.VncFunction.createAnonymousFuncName;
import static com.github.jlangch.venice.impl.util.ArityExceptions.assertArity;
import static com.github.jlangch.venice.impl.util.ArityExceptions.assertMinArity;
import static com.github.jlangch.venice.impl.util.ArityExceptions.formatArityExMsg;
import static com.github.jlangch.venice.impl.util.ArityExceptions.formatVariadicArityExMsg;

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

import com.github.jlangch.venice.ArityException;
import com.github.jlangch.venice.AssertionException;
import com.github.jlangch.venice.NotInTailPositionException;
import com.github.jlangch.venice.SecurityException;
import com.github.jlangch.venice.Version;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.debug.agent.DebugAgent;
import com.github.jlangch.venice.impl.debug.breakpoint.BreakpointFnRef;
import com.github.jlangch.venice.impl.env.ComputedVar;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.env.ReservedSymbols;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.functions.CoreFunctions;
import com.github.jlangch.venice.impl.functions.Functions;
import com.github.jlangch.venice.impl.functions.TransducerFunctions;
import com.github.jlangch.venice.impl.reader.Reader;
import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.IVncFunction;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncMultiArityFunction;
import com.github.jlangch.venice.impl.types.VncMultiFunction;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncLazySeq;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncMapEntry;
import com.github.jlangch.venice.impl.types.collections.VncMutableSet;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.collections.VncSet;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions.FnType;
import com.github.jlangch.venice.impl.util.CallFrame;
import com.github.jlangch.venice.impl.util.CallFrameFnData;
import com.github.jlangch.venice.impl.util.CallStack;
import com.github.jlangch.venice.impl.util.MeterRegistry;
import com.github.jlangch.venice.impl.util.WithCallStack;
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
public class VeniceInterpreter implements IVeniceInterpreter, Serializable  {

	public VeniceInterpreter(
			final IInterceptor interceptor
	) {
		this(interceptor, null);
	}

	public VeniceInterpreter(
			final IInterceptor interceptor,
			final MeterRegistry meterRegistry
	) {
		if (interceptor == null) {
			throw new SecurityException("VeniceInterpreter can not run without an interceptor");
		}
		
		final MeterRegistry mr = meterRegistry == null 
									? new MeterRegistry(false)
									: meterRegistry;
		
		this.interceptor = interceptor;
		this.meterRegistry = mr;
		this.nsRegistry = new NamespaceRegistry();
		this.sealedSystemNS = new AtomicBoolean(false);

		// performance optimization
		this.checkSandbox = !(interceptor instanceof AcceptAllInterceptor);
		
		this.specialFormHandler = new SpecialFormsHandler(
									this::evaluate,
									this::evaluate_values,
									this.nsRegistry,
									this.meterRegistry,
									this.sealedSystemNS);

		ThreadContext.setInterceptor(interceptor);	
		ThreadContext.setMeterRegistry(mr);
	}

	
	@Override
	public void initNS() {
		nsRegistry.clear();
		Namespaces.setCurrentNamespace(nsRegistry.computeIfAbsent(Namespaces.NS_USER));
	}
		
	@Override
	public void sealSystemNS() {
		sealedSystemNS.set(true);
	}
	
	@Override
	public void setMacroExpandOnLoad(final boolean macroExpandOnLoad, final Env env) {
		// Dynamically turn on/off macroexpand-on-load. The REPL makes use of this.
		env.setMacroExpandOnLoad(VncBoolean.of(macroExpandOnLoad));		
		this.macroexpand = macroExpandOnLoad;
	}
	
	@Override
	public boolean isMacroExpandOnLoad() {
		return macroexpand;
	}
	
	@Override
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

	@Override
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

	@Override
	public VncVal MACROEXPAND(final VncVal ast, final Env env) {
		return macroexpand_all(
					new CallFrame("macroexpand-all", ast.getMeta()), 
					ast, 
					env);
	}

	@Override
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

	@Override
	public String PRINT(final VncVal exp) {
		return Printer.pr_str(exp, true);
	}
		
	@Override
	public Env createEnv(
			final boolean macroexpandOnLoad, 
			final boolean ansiTerminal, 
			final RunMode runMode
	) {  
		return createEnv(null, macroexpandOnLoad, ansiTerminal, runMode);
	}

	@Override
	public Env createEnv(
			final List<String> preloadedExtensionModules,
			final boolean macroExpandOnLoad, 
			final boolean ansiTerminal,
			final RunMode runMode
	) {
		sealedSystemNS.set(false);

		final Env env = new Env(null);
			
		// loaded modules: preset with implicitly preloaded modules
		final VncMutableSet loadedModules = VncMutableSet.ofAll(Modules.PRELOADED_MODULES);
		
		for(Map.Entry<VncVal,VncVal> e: Functions.functions.entrySet()) {
			final VncSymbol sym = (VncSymbol)e.getKey();
			final VncFunction fn = (VncFunction)e.getValue();			
			env.setGlobal(new Var(sym, fn, fn.isRedefinable()));
		}

		// set Venice version
		env.setGlobal(new Var(new VncSymbol("*version*"), new VncString(Version.VERSION), false));

		// set current namespace
		env.setGlobal(new ComputedVar(new VncSymbol("*ns*"), () -> Namespaces.getCurrentNS(), false));
		
		// set system newline
		env.setGlobal(new Var(new VncSymbol("*newline*"), new VncString(System.lineSeparator()), false));

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
		setMacroExpandOnLoad(macroExpandOnLoad, env);

		// load core module
		loadModule("core", env, loadedModules);
		
		// security: seal system namespaces (Namespaces::SYSTEM_NAMESPACES) - no further changes allowed!
		sealedSystemNS.set(true);

		// load other modules requested for preload
		toEmpty(preloadedExtensionModules).forEach(m -> loadModule(m, env, loadedModules));

		return env;
	}
	
	@Override
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
	private VncVal evaluate(
			final VncVal ast_, 
			final Env env_
	) {
		return evaluate(ast_, env_, false);
	}
	
	private VncVal evaluate(
			final VncVal ast_, 
			final Env env_, 
			final boolean inTailPosition
	) {
		VncVal orig_ast = ast_;
		Env env = env_;

		RecursionPoint recursionPoint = null;
		boolean tailPosition = inTailPosition;

		while (true) {
			//System.out.println("EVAL: " + printer._pr_str(orig_ast, true));
			if (!(orig_ast instanceof VncList)) {
				// not an s-expr
				return evaluate_values(orig_ast, env);
			}

			final VncList ast = (VncList)orig_ast;
			if (ast.isEmpty()) { 
				return ast; 
			}

			final VncVal a0 = ast.first();		
			final String a0sym = (a0 instanceof VncSymbol) ? ((VncSymbol)a0).getName() : "__<*fn*>__";
			final VncList args = ast.rest();		

			// special form / function dispatcher
			switch (a0sym) {
				case "do": { // (do expr*)
						final VncList expressions = args;
						evaluate_sequence_values(expressions.butlast(), env);
						orig_ast = expressions.last();
						tailPosition = true;
					}
					break;

				case "if": { // (if cond expr-true expr-false*)
						final int numArgs = args.size();
						if (numArgs == 2 || numArgs == 3) {
							final VncVal cond = evaluate(args.first(), env);

							final ThreadContext threadCtx = ThreadContext.get();
							final DebugAgent debugAgent = threadCtx.getDebugAgent_();

							if (debugAgent != null && debugAgent.hasBreakpointFor(BREAKPOINT_REF_IF)) {
								debugAgent.onBreakSpecialForm(
										"if", 
										FunctionEntry, 
										VncVector.of(new VncString("cond")), 
										VncList.of(cond), 
										a0.getMeta(), 
										env, 
										threadCtx.getCallStack_());
							}

							orig_ast = VncBoolean.isFalseOrNil(cond) 
											? args.third()   // eval false slot form (nil if not available)
											: args.second(); // eval true slot form
							tailPosition = true;
						}
						else {
							// only create callstack when needed!
							try (WithCallStack cs = new WithCallStack(new CallFrame("if", args, a0.getMeta()))) {
								assertArity("if", FnType.SpecialForm, args, 2, 3);
							}
						}
					}
					break;

				case "let": { // (let [bindings*] exprs*)
						if (args.isEmpty()) {
							// only create callstack when needed!
							try (WithCallStack cs = new WithCallStack(new CallFrame("let", args, a0.getMeta()))) {
								assertMinArity("let", FnType.SpecialForm, args, 1);					
							}
						}
						env = new Env(env);  // let introduces a new environment

						final ThreadContext threadCtx = ThreadContext.get();
						final DebugAgent debugAgent = threadCtx.getDebugAgent_();

						final VncVector bindings = Coerce.toVncVector(args.first());
						final VncList expressions = args.rest();

						final Iterator<VncVal> bindingsIter = bindings.iterator();
						final List<Var> vars = new ArrayList<>();
						while(bindingsIter.hasNext()) {
							final VncVal sym = bindingsIter.next();
							if (!bindingsIter.hasNext()) {
								try (WithCallStack cs = new WithCallStack(new CallFrame("let", args, a0.getMeta()))) {
									throw new VncException("let requires an even number of forms in the binding vector!");					
								}
							}
							if (sym instanceof VncSymbol && ((VncSymbol)sym).hasNamespace()) {
								final VncSymbol s = (VncSymbol)sym;
								final CallFrame cf = new CallFrame(s.getQualifiedName(), args, s.getMeta());
								try (WithCallStack cs = new WithCallStack(cf)) {
									throw new VncException("Can't use qualified symbols with let!");					
								}
							}
							
							final VncVal val = evaluate(bindingsIter.next(), env);
							final List<Var> varTmp = Destructuring.destructure(sym, val);
							env.addLocalVars(varTmp);
							
							if (debugAgent != null) {
								vars.addAll(varTmp);
							}
						}
						
						if (debugAgent != null && debugAgent.hasBreakpointFor(BREAKPOINT_REF_LET)) {
							final CallStack callStack = threadCtx.getCallStack_();
							debugAgent.onBreakSpecialForm(
									"let", FunctionEntry, vars, a0.getMeta(), env, callStack);
						}
						
						if (expressions.size() == 1) {
							orig_ast = expressions.first();
						}
						else {
							evaluate_sequence_values(expressions.butlast(), env);
							orig_ast = expressions.last();
						}
						tailPosition = true;
					}
					break;

				case "quasiquote": // (quasiquote form)
					if (args.size() != 1) {
						// only create callstack when needed!
						try (WithCallStack cs = new WithCallStack(new CallFrame("quasiquote", args, a0.getMeta()))) {
							assertArity("quasiquote", FnType.SpecialForm, args, 1);
						}
					}
					orig_ast = quasiquote(args.first());
					break;

				case "quote": // (quote form)
					if (args.size() != 1) {
						// only create callstack when needed!
						try (WithCallStack cs = new WithCallStack(new CallFrame("quote", args, a0.getMeta()))) {
							assertArity("quote", FnType.SpecialForm, args, 1);
						}
					}
					return args.first();

				case "loop": { // (loop [bindings*] exprs*)
						recursionPoint = null;
						if (args.size() < 2) {
							// only create callstack when needed!
							try (WithCallStack cs = new WithCallStack(new CallFrame("loop", args, a0.getMeta()))) {
								assertMinArity("loop", FnType.SpecialForm, args, 2);
							}
						}
						env = new Env(env);

						final VncVector bindings = Coerce.toVncVector(args.first());
						final VncList expressions = args.rest();
						final VncVal meta = a0.getMeta();
						
						if (bindings.size() % 2 != 0) {
							try (WithCallStack cs = new WithCallStack(new CallFrame("loop", args, meta))) {
								throw new VncException("loop requires an even number of forms in the binding vector!");					
							}
						}

						final List<VncSymbol> bindingNames = new ArrayList<>(bindings.size() / 2);
						final Iterator<VncVal> bindingsIter = bindings.iterator();
						while(bindingsIter.hasNext()) {
							final VncSymbol sym = Coerce.toVncSymbol(bindingsIter.next());
							final VncVal val = evaluate(bindingsIter.next(), env);

							env.setLocal(new Var(sym, val));
							bindingNames.add(sym);
						}

						final ThreadContext threadCtx = ThreadContext.get();
						final DebugAgent debugAgent = threadCtx.getDebugAgent_();

						recursionPoint = new RecursionPoint(
												bindingNames,
												expressions,
												env,
												meta,
												debugAgent);

						if (debugAgent != null && debugAgent.hasBreakpointFor(BREAKPOINT_REF_LOOP)) {
							debugAgent.onBreakLoop(
								FunctionEntry,
								recursionPoint.getLoopBindingNames(), 
								recursionPoint.getMeta(),
								env,
								threadCtx.getCallStack_());
						}

						if (expressions.size() == 1) {
							orig_ast = expressions.first();
						}
						else {
							evaluate_sequence_values(expressions.butlast(), env);
							orig_ast = expressions.last();
						}
						tailPosition = true;
					}
					break;

				case "recur":  { // (recur exprs*)
						// Note: (recur) is valid, it's used by the while macro
						if (recursionPoint == null) {
							try (WithCallStack cs = new WithCallStack(new CallFrame("recur", args, a0.getMeta()))) {
								throw new NotInTailPositionException(
										"The recur expression is not in tail position!");
							}
						}
						if (args.size() != recursionPoint.getLoopBindingNamesCount()) {
							try (WithCallStack cs = new WithCallStack(new CallFrame("recur", args, a0.getMeta()))) {
								throw new VncException(String.format(
										"The recur args (%d) do not match the loop args (%d) !",
										args.size(), 
										recursionPoint.getLoopBindingNamesCount()));
							}
						}
				
						env = buildRecursionEnv(args, env, recursionPoint);
	
						// for performance reasons the DebugAgent is stored in the 
						// RecursionPoint. Saves repeated ThreadLocal access!
						final ThreadContext threadCtx = ThreadContext.get();
						final DebugAgent debugAgent = threadCtx.getDebugAgent_();

						if (debugAgent != null && debugAgent.hasBreakpointFor(BREAKPOINT_REF_LOOP)) {
							debugAgent.onBreakLoop(
									FunctionEntry,
									recursionPoint.getLoopBindingNames(), 
									recursionPoint.getMeta(),
									env,
									threadCtx.getCallStack_());
						}

						final VncList expressions = recursionPoint.getLoopExpressions();
						if (expressions.size() == 1) {
							orig_ast = expressions.first();
						}
						else {
							evaluate_sequence_values(expressions.butlast(), env);
							orig_ast = expressions.last();
						}
						tailPosition = true;
					}
					break;

				case "fn": // (fn name? [params*] condition-map? expr*)
					return fn_(new CallFrame("fn", args, a0.getMeta()), args, env);

				case "eval": // (eval expr*)
					return eval_(new CallFrame("eval", args, a0.getMeta()), args, env);

				case "def":  // (def name value)
					return def_(new CallFrame("def", args, a0.getMeta()), args, env);

				case "defonce": // (defonce name value)
					return defonce_(new CallFrame("defonce", args, a0.getMeta()), args, env);

				case "def-dynamic": // (def-dynamic name value)
					return def_dynamic_(new CallFrame("def-dynamic", args, a0.getMeta()), args, env);

				case "defmacro":
					return defmacro_(new CallFrame("defmacro", args, a0.getMeta()), args, env);

				case "deftype": // (deftype type fields validationFn*)
					return specialFormHandler.deftype_(this, new CallFrame("deftype", args, a0.getMeta()), args, env);

				case "deftype?": // (deftype? type)
					return specialFormHandler.deftypeQ_(new CallFrame("deftype?", args, a0.getMeta()), args, env);

				case "deftype-of": // (deftype-of type base-type validationFn*)
					return specialFormHandler.deftype_of_(this, new CallFrame("deftype-of", args, a0.getMeta()), args, env);

				case "deftype-or":  // (deftype-or type vals*)
					return specialFormHandler.deftype_or_(this, new CallFrame("deftype-or", args, a0.getMeta()), args, env);

				case ".:": // (.: type args*)
					return specialFormHandler.deftype_create_(this, new CallFrame(".:", args, a0.getMeta()), args, env);

				case "defmulti":  // (defmulti name dispatch-fn)
					return defmulti_(new CallFrame("defmulti", args, a0.getMeta()), args, env, a0.getMeta());

				case "defmethod": // (defmethod multifn-name dispatch-val & fn-tail)
					return defmethod_(new CallFrame("defmethod", args, a0.getMeta()), args, env, a0.getMeta());

				case "ns": // (ns alpha)
					return specialFormHandler.ns_(new CallFrame("ns", args, a0.getMeta()), args, env);

				case "ns-remove": // (ns-remove ns)
					return specialFormHandler.ns_remove_(new CallFrame("ns-remove", args, a0.getMeta()), args, env);

				case "ns-unmap": // (ns-unmap ns sym)
					return specialFormHandler.ns_unmap_(new CallFrame("ns-unmap", args, a0.getMeta()), args, env);

				case "ns-list": // (ns-list ns)
					return specialFormHandler.ns_list_(new CallFrame("ns-list", args, a0.getMeta()), args, env);

				case "import":
					return specialFormHandler.import_(new CallFrame("import", args, a0.getMeta()), args, env);

				case "imports":
					return specialFormHandler.imports_(new CallFrame("imports", args, a0.getMeta()), args, env);

				case "namespace": // (namespace x)
					return specialFormHandler.namespace_(new CallFrame("namespace", args, a0.getMeta()), args, env);

				case "resolve": // (resolve sym)
					return specialFormHandler.resolve_(new CallFrame("resolve", args, a0.getMeta()), args, env);
				
				case "var-get": // (var-get v)
					return specialFormHandler.var_get_(new CallFrame("var-get", args, a0.getMeta()), args, env);

				case "var-ns": // (var-ns v)
					return specialFormHandler.var_ns_(new CallFrame("var-ns", args, a0.getMeta()), args, env);

				case "var-name": // (var-name v)
					return specialFormHandler.var_name_(new CallFrame("var-name", args, a0.getMeta()), args, env);

				case "var-local?": // (var-local? v)
					return specialFormHandler.var_localQ_(new CallFrame("var-local?", args, a0.getMeta()), args, env);

				case "var-thread-local?": // (var-thread-local? v)
					return specialFormHandler.var_thread_localQ_(new CallFrame("var-thread-local?", args, a0.getMeta()), args, env);

				case "var-global?": // (var-global? v)
					return specialFormHandler.var_globalQ_(new CallFrame("var-global?", args, a0.getMeta()), args, env);

				case "set!": // (set! name expr)
					return specialFormHandler.setBANG_(new CallFrame("set!", args, a0.getMeta()), args, env);
				
				case "inspect": // (inspect sym)
					return specialFormHandler.inspect_(new CallFrame("inspect", args, a0.getMeta()), args, env);

				case "macroexpand": // (macroexpand form)
					return macroexpand(
							new CallFrame("macroexpand", args, a0.getMeta()), args, env);

				case "macroexpand-all*":  // (macroexpand-all* form)
					// Note: This special form is exposed through the public Venice function 
					//       'core/macroexpand-all' in the 'core' module.
					//       The VeniceInterpreter::MACROEXPAND function makes use of it.
					return macroexpand_all(
								new CallFrame("macroexpand-all*", args, a0.getMeta()), 
								evaluate(args.first(), env), 
								env);

				case "doc": // (doc sym)
					return specialFormHandler.doc_(new CallFrame("doc", args, a0.getMeta()), args, env);

				case "print-highlight": // (print-highlight form)
					return specialFormHandler.print_highlight_(new CallFrame("print-highlight", args, a0.getMeta()), args, env);

				case "modules": // (modules )
					return specialFormHandler.modules_(new CallFrame("modules", args, a0.getMeta()), args, env);

				case "binding":  // (binding [bindings*] exprs*)
					return binding_(args, new Env(env), a0.getMeta());

				case "bound?": // (bound? sym)
					return VncBoolean.of(env.isBound(Coerce.toVncSymbol(evaluate(args.first(), env))));

				case "try": // (try exprs* (catch :Exception e exprs*) (finally exprs*))
					return specialFormHandler.try_(new CallFrame("try", args, a0.getMeta()), args, env, a0.getMeta());

				case "try-with": // (try-with [bindings*] exprs* (catch :Exception e exprs*) (finally exprs*))
					return specialFormHandler.try_with_(new CallFrame("try-with", args, a0.getMeta()), args, env, a0.getMeta());

				case "locking":
					return specialFormHandler.locking_(new CallFrame("locking", args, a0.getMeta()), args, env);

				case "dorun":
					return specialFormHandler.dorun_(new CallFrame("dorun", args, a0.getMeta()), args, env);

				case "dobench":
					return specialFormHandler.dobench_(new CallFrame("dobench", args, a0.getMeta()), args, env);

				case "prof":
					return specialFormHandler.prof_(new CallFrame("prof", args, a0.getMeta()), args, env);
				
				case "tail-pos": 
					return tail_pos_check(
								tailPosition, 
								new CallFrame("tail-pos", args, a0.getMeta()), args, env);

				default: { // functions, macros, collections/keywords as functions
					final VncVal fn0 = a0 instanceof VncSymbol
											? env.get((VncSymbol)a0)  // (+ 1 2)
											: evaluate(a0, env);      // ((resolve '+) 1 2)
										
					if (fn0 instanceof VncFunction) { 
						final VncFunction fn = (VncFunction)fn0;

						if (fn.isMacro()) { 
							// macro
							final VncVal expandedAst = macroexpand(ast, env);
							if (expandedAst instanceof VncList) {					
								orig_ast = expandedAst;
								continue;
							}
							else {
								return evaluate_values(expandedAst, env); // not an s-expr
							}
						} 
						else { 
							final String fnName = fn.getQualifiedName();

							final ThreadContext threadCtx = ThreadContext.get();
							final CallStack callStack = threadCtx.getCallStack_();						
							final DebugAgent debugAgent = threadCtx.getDebugAgent_();
							
							if (debugAgent != null && debugAgent.hasBreakpointFor(new BreakpointFnRef(fnName))) {
								debugAgent.onBreakFnCall(fnName, fn, args, env, callStack);
							}
		
							// evaluate function args
							final VncList fnArgs = (VncList)evaluate_sequence_values(args, env);
							
							final long nanos = meterRegistry.enabled ? System.nanoTime() : 0L;
	
							// validate function call allowed by sandbox
							if (checkSandbox) {
								final CallFrame cf = new CallFrame(fnName, fnArgs, a0.getMeta(), env);
								try (WithCallStack cs = new WithCallStack(cf)) {
									interceptor.validateVeniceFunction(fnName);	
								}
								interceptor.validateMaxExecutionTime();
							}
							
							final Thread currThread = Thread.currentThread();
	
							InterruptChecker.checkInterrupted(currThread, fn);
							

							// Automatic TCO (tail call optimization)
							if (tailPosition
									&& !fn.isNative()  // native functions do not have an AST body
									&& !callStack.isEmpty() 
									&& fnName.equals(callStack.peek().getFnName())
							) {
								// fn may be a normal function, a multi-arity, or a multi-method function							
								final VncFunction f = fn.getFunctionForArgs(fnArgs);
								env.addLocalVars(Destructuring.destructure(f.getParams(), fnArgs));
								
								if (debugAgent != null && debugAgent.hasBreakpointFor(new BreakpointFnRef(fnName))) {
									debugAgent.onBreakFnEnter(fnName, f, fnArgs, env, callStack);
								}
								
								final VncList body = (VncList)f.getBody();
								evaluate_sequence_values(body.butlast(), env);
								orig_ast = body.last();
							}
							else {
								// invoke function with a new call frame
								try {
									if (fn.isNative()) {
										callStack.push(new CallFrame(fnName, fnArgs, a0.getMeta(), env));

										if (debugAgent != null && debugAgent.hasBreakpointFor(new BreakpointFnRef(fnName))) {
											// Debugging handled for native functions only.
											env.setLocal(new Var(new VncSymbol("debug::fn-args"), fnArgs));
											try {
												debugAgent.onBreakFnEnter(fnName, fn, fnArgs, env, callStack);
												final VncVal retVal = fn.apply(fnArgs);
												debugAgent.onBreakFnExit(fnName, fn, fnArgs, retVal, env, callStack);
												return retVal;
											}
											catch(Exception ex) {
												debugAgent.onBreakFnException(fnName, fn, fnArgs, ex, env, callStack);
												throw ex;
											}
										}
										else {
											return fn.apply(fnArgs);
										}
									}
									else {
										// Debugging for non native functions is handled in 
										// VncFunction::apply. See the builder
										// VeniceInterpreter::buildFunction(..)
										threadCtx.setCallFrameFnData_(new CallFrameFnData(fnName, a0.getMeta()));
										return fn.apply(fnArgs);
									}
								}
								finally {
									threadCtx.setCallFrameFnData_(null);
									if (fn.isNative()) {
										callStack.pop();
									}
									
									InterruptChecker.checkInterrupted(currThread, fn);
									if (checkSandbox) {
										interceptor.validateMaxExecutionTime();
									}
									if (meterRegistry.enabled) {
										final long elapsed = System.nanoTime() - nanos;
										if (fn instanceof VncMultiArityFunction) {
											final VncFunction f = fn.getFunctionForArgs(fnArgs);
											meterRegistry.record(fnName + "[" + f.getParams().size() + "]", elapsed);
										}
										else {
											meterRegistry.record(fnName, elapsed);
										}
									}
								}	
							}
						}
					}
					else if (fn0 instanceof IVncFunction) {
						// collection/keyword as function
						final CallFrame cf = new CallFrame(fn0.getType().toString(), args, a0.getMeta(), env);
						try (WithCallStack cs = new WithCallStack(cf)) {
							final IVncFunction fn = (IVncFunction)fn0;
							final VncList fnArgs = (VncList)evaluate_sequence_values(args, env);
							return fn.apply(fnArgs);
						}
					}
					else {
						final CallFrame cf = new CallFrame(a0sym, args, a0.getMeta());
						try (WithCallStack cs = new WithCallStack(cf)) {
							throw new VncException(String.format(
									"Expected a function or keyword/set/map/vector as "
										+ "s-expression symbol value but got a value "
										+ "of type '%s'!", 
									Types.getType(fn0)));
						}
					}
				}
				break;
			}
		}
	}
	
	private VncVal evaluate_values(final VncVal ast, final Env env) {
		if (ast == Nil) {
			return Nil;
		}
		else if (ast instanceof VncSymbol) {
			return env.get((VncSymbol)ast);
		}
		else if (ast instanceof VncSequence) {
			return evaluate_sequence_values((VncSequence)ast, env);
		}
		else if (ast instanceof VncMap) {
			final VncMap map = (VncMap)ast;
			
			final Map<VncVal,VncVal> vals = new HashMap<>(map.size());
			for(Entry<VncVal,VncVal> e: map.getJavaMap().entrySet()) {
				vals.put(
					evaluate(e.getKey(), env), 
					evaluate(e.getValue(), env));
			}
			return map.withValues(vals);
		} 
		else if (ast instanceof VncSet) {
			final VncSet set = (VncSet)ast;
			
			final List<VncVal> vals = new ArrayList<>(set.size());
			for(VncVal v: set) {
				vals.add(evaluate(v, env));
			}
			return set.withValues(vals);
		} 
		else {
			return ast;
		}
	}

	private VncSequence evaluate_sequence_values(final VncSequence seq, final Env env) {
		if (seq instanceof VncLazySeq) {
			return seq;
		}
		
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
				return seq.map(v -> evaluate(v, env));
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
			final Env env
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

			final CallFrame cf = new CallFrame(macro.getQualifiedName(), macroArgs, a0.getMeta());
			try (WithCallStack cs = new WithCallStack(cf)) {
				if (meterRegistry.enabled) {
					final long nanosRun = System.nanoTime();
					
					ast_ = macro.apply(macroArgs);
					
					meterRegistry.record(macro.getQualifiedName() + "[m]", System.nanoTime() - nanosRun);
				}
				else {
					ast_ = macro.apply(macroArgs);
				}
			}
		}
	 
		if (expandedMacros > 0) {
			if (meterRegistry.enabled) {
				meterRegistry.record("macroexpand", System.nanoTime() - nanos);
			}
		}

		return ast_;
	}

	private VncVal macroexpand(
			final CallFrame callframe, 
			final VncList args, 
			final Env env
	) {
		try (WithCallStack cs = new WithCallStack(callframe)) {
			assertArity("macroexpand", FnType.SpecialForm, args, 1);
			final VncVal ast = evaluate(args.first(), env);
			return macroexpand(ast, env);
		}		
	}

	/**
	 * Expands recursively all macros in the form.
	 * 
	 * <p>An approach with <code>core/prewalk</code> does not work, because
	 * this function does not apply namespaces definitions (ns x).
	 * Remember that macros are always executed in the namespace of the caller 
	 * as opposed to functions that are executed in the namespace they are 
	 * defined in.
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
								return macroexpand(list, env);
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
	
			
			// remember the original namespace
			final Namespace original_ns = Namespaces.getCurrentNamespace();
			try {
				return prewalk.applyOf(handler, form);
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

	private VncVal defmacro_(final CallFrame callframe, final VncList args, final Env env) {
		try (WithCallStack cs = new WithCallStack(callframe)) {
			assertMinArity("defmacro", FnType.SpecialForm, args, 2);
			return defmacro_(args, env);
		}
	}
	
	private VncFunction defmacro_(final VncList args, final Env env) {
		int argPos = 0;
		
		final VncSymbol macroName = qualifySymbolWithCurrNS(
										evaluateSymbolMetaData(args.nth(argPos++), env));
		VncVal meta = macroName.getMeta();
		
		if (MetaUtil.isPrivate(meta)) {
			throw new VncException(String.format(
					"The macro '%s' must not be defined as private! "
						+ "Venice does not support private macros.",
					macroName.getName()));
		}
		
		final VncSequence paramsOrSig = Coerce.toVncSequence(args.nth(argPos));
					
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

			final VncList body = args.slice(argPos);
	
			final VncFunction macroFn = buildFunction(
											macroName_.getName(), 
											params, 
											body, 
											null, 
											true,
											meta,
											env);
	
			env.setGlobal(new Var(macroName_, macroFn.withMeta(meta), false));

			return macroFn;
		}
		else {
			// multi arity:

			final List<VncFunction> fns = new ArrayList<>();
			
			final VncVal meta_ = meta;

			args.slice(argPos).forEach(s -> {
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
							meta_,
							env));
			});

			final VncFunction macroFn = new VncMultiArityFunction(
												macroName_.getName(), 
												fns, 
												true, 
												meta);
			
			env.setGlobal(new Var(macroName_, macroFn, false));

			return macroFn;
		}
	}

	private VncVal def_(final CallFrame callframe, final VncList args, final Env env) {
		try (WithCallStack cs = new WithCallStack(callframe)) {
			assertArity("def", FnType.SpecialForm, args, 1, 2);
			final VncSymbol name = validateSymbolWithCurrNS(
										qualifySymbolWithCurrNS(
												evaluateSymbolMetaData(args.first(), env)),
										"def");
			
			final VncVal val = args.second();
			
			final VncVal res = evaluate(val, env).withMeta(name.getMeta());
			env.setGlobal(new Var(name, res, true));
			return name;
		}				
	}

	private VncVal defonce_(final CallFrame callframe, final VncList args, final Env env) {
		try (WithCallStack cs = new WithCallStack(callframe)) {
			assertArity("defonce", FnType.SpecialForm, args, 1, 2);
			final VncSymbol name = validateSymbolWithCurrNS(
										qualifySymbolWithCurrNS(
												evaluateSymbolMetaData(args.first(), env)),
										"defonce");
							
			final VncVal val = args.second();

			final VncVal res = evaluate(val, env).withMeta(name.getMeta());
			env.setGlobal(new Var(name, res, false));
			return name;
		}
	}

	private VncVal def_dynamic_(final CallFrame callframe, final VncList args, final Env env) {
		try (WithCallStack cs = new WithCallStack(callframe)) {
			assertArity("def-dynamic", FnType.SpecialForm, args, 1, 2);
			final VncSymbol name = validateSymbolWithCurrNS(
										qualifySymbolWithCurrNS(
												evaluateSymbolMetaData(args.first(), env)),
										"def-dynamic");
			
			final VncVal val = args.second();
			
			final VncVal res = evaluate(val, env).withMeta(name.getMeta());
			env.setGlobalDynamic(name, res);
			return name;
		}
	}


	private VncVal defmulti_(final CallFrame callframe, final VncList args, final Env env, final VncVal meta) {
		try (WithCallStack cs = new WithCallStack(callframe)) {
			assertArity("defmulti", FnType.SpecialForm, args, 2);
			final VncSymbol name =  validateSymbolWithCurrNS(
										qualifySymbolWithCurrNS(
												evaluateSymbolMetaData(args.first(), env)),
										"defmulti");
			
			IVncFunction dispatchFn;
			
			if (Types.isVncKeyword(args.second())) {
				dispatchFn = (VncKeyword)args.second();
			}
			else if (Types.isVncSymbol(args.second())) {
				dispatchFn = Coerce.toVncFunction(env.get((VncSymbol)args.second()));
			}
			else {
				final VncList fnAst = Coerce.toVncList(args.second());
				dispatchFn = fn_(callframe, fnAst.rest(), env);
			}

			final VncMultiFunction multiFn = new VncMultiFunction(name.getName(), dispatchFn)
														.withMeta(name.getMeta());
			env.setGlobal(new Var(name, multiFn, true));
			return multiFn;
		}
	}

	private VncVal defmethod_(final CallFrame callframe, final VncList args, final Env env, final VncVal meta) {
		try (WithCallStack cs = new WithCallStack(callframe)) {
			assertMinArity("defmethod", FnType.SpecialForm, args, 2);
			final VncSymbol multiFnName = qualifySymbolWithCurrNS(
											Coerce.toVncSymbol(args.first()));
			final VncVal multiFnVal = env.getGlobalOrNull(multiFnName);
			if (multiFnVal == null) {
				throw new VncException(String.format(
							"No multifunction '%s' defined for the method definition", 
							multiFnName.getName())); 
			}
			final VncMultiFunction multiFn = Coerce.toVncMultiFunction(multiFnVal);
			final VncVal dispatchVal = args.second();
			
			final VncVector params = Coerce.toVncVector(args.third());
			if (params.size() != multiFn.getParams().size()) {
				throw new VncException(String.format(
						"A method definition for the multifunction '%s' must have %d parameters", 
						multiFnName.getName(),
						multiFn.getParams().size()));
			}
			final VncVector preConditions = getFnPreconditions(args.fourth(), env);
			final VncList body = args.slice(preConditions == null ? 3 : 4);
			final VncFunction fn = buildFunction(
										multiFnName.getName(),
										params,
										body,
										preConditions,
										false,
										meta,
										env);

			return multiFn.addFn(dispatchVal, fn.withMeta(meta));
		}
	}


	private VncVal eval_(
			final CallFrame callframe, 
			final VncList args, 
			final Env env
	) {
		try (WithCallStack cs = new WithCallStack(callframe)) {
			assertMinArity("eval", FnType.SpecialForm, args, 0);
			specialFormCallValidation("eval");
			final Namespace ns = Namespaces.getCurrentNamespace();
			try {
				return evaluate(Coerce.toVncSequence(evaluate_sequence_values(args, env)).last(), env);
			}
			finally {
				Namespaces.setCurrentNamespace(ns);
			}
		}
	}
	
	private VncVal tail_pos_check(
			final boolean inTailPosition, 
			final CallFrame callframe, 
			final VncList args, 
			final Env env
	) {
		if (!inTailPosition) {
			final VncString name = Coerce.toVncString(args.nthOrDefault(0, VncString.empty()));
			try (WithCallStack cs = new WithCallStack(callframe)) {
				throw new NotInTailPositionException(
						name.isEmpty() 
							? "Not in tail position"
							: String.format("Not '%s' in tail position", name.getValue()));
			}
		}
		else {
			return Nil;
		}
	}

	private VncFunction fn_(
			final CallFrame callframe, 
			final VncList args, 
			final Env env
	) {
		// single arity:  (fn name? [params*] condition-map? expr*)
		// multi arity:   (fn name? ([params*] condition-map? expr*)+ )

		try (WithCallStack cs = new WithCallStack(callframe)) {
			assertMinArity("fn", FnType.SpecialForm, args, 1);
	
			VncSymbol name;
			VncVal meta;
			int argPos;
			
			if (Types.isVncSymbol(args.first())) {
				argPos = 1;
				name = (VncSymbol)args.first();
				meta = name.getMeta();
			}
			else {
				argPos = 0;
				name = new VncSymbol(VncFunction.createAnonymousFuncName());
				meta = args.second().getMeta();
			}
	
			final VncSymbol fnName = qualifySymbolWithCurrNS(name);
			ReservedSymbols.validateNotReservedSymbol(fnName);
	
			final VncSequence paramsOrSig = Coerce.toVncSequence(args.nth(argPos));
			if (Types.isVncVector(paramsOrSig)) {
				// single arity:
				
				argPos++;
				final VncVector params = (VncVector)paramsOrSig;
				
				final VncVector preCon = getFnPreconditions(args.nthOrDefault(argPos, null), env);
				if (preCon != null) argPos++;
				
				final VncList body = args.slice(argPos);
				
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
					return buildFunction(
								fnName.getName(), params, body, preCon, 
								false, meta, env);
	//			}		
			}
			else {
				// multi arity:
	
				final List<VncFunction> fns = new ArrayList<>();
				
				args.slice(argPos).forEach(s -> {
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
						fns.add(
							buildFunction(
								fnName.getName(), params, body, preCon, false, meta, env));
	//				}
				});
				
				return new VncMultiArityFunction(fnName.getName(), fns, false, meta);
			}
		}
	}

	private VncVal binding_(final VncList args, final Env env, final VncVal meta) {
		final VncSequence bindings = Coerce.toVncSequence(args.first());
		final VncList expressions = args.rest();

		if (bindings.size() % 2 != 0) {
			try (WithCallStack cs = new WithCallStack(new CallFrame("bindings", args, meta))) {
				throw new VncException(
						"bindings requires an even number of forms in the "
						+ "binding vector!");					
			}
		}

		final List<Var> bindingVars = new ArrayList<>();
		try {
			for(int i=0; i<bindings.size(); i+=2) {
				final VncVal sym = bindings.nth(i);
				final VncVal val = evaluate(bindings.nth(i+1), env);
		
				final List<Var> vars = Destructuring.destructure(sym, val);
				vars.forEach(v -> env.pushGlobalDynamic(v.getName(), v.getVal()));
				
				bindingVars.addAll(vars);
			}

			final ThreadContext threadCtx = ThreadContext.get();
			final DebugAgent debugAgent = threadCtx.getDebugAgent_();

			if (debugAgent != null && debugAgent.hasBreakpointFor(BREAKPOINT_REF_BINDINGS)) {
				final CallStack callStack = threadCtx.getCallStack_();
				debugAgent.onBreakSpecialForm(
						"bindings", FunctionEntry, bindingVars, meta, env, callStack);
			}

			evaluate_sequence_values(expressions.butlast(), env);
			return evaluate(expressions.last(), env);
		}
		finally {
			bindingVars.forEach(v -> env.popGlobalDynamic(v.getName()));
		}
	}
	
	private VncFunction buildFunction(
			final String name, 
			final VncVector params, 
			final VncList body, 
			final VncVector preConditions, 
			final boolean macro,
			final VncVal meta,
			final Env env
	) {
		// the namespace the function/macro is defined for
		final Namespace ns = Namespaces.getCurrentNamespace();
		
		// Note: Do not switch to the functions own namespace for the function 
		//       "core/macroexpand-all". Handle "macroexpand-all" like a special 
		//       form. This allows expanding locally defined macros from the REPL 
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

		return new VncFunction(name, params, macro, preConditions, meta) {
			@Override
			public VncVal apply(final VncList args) {
				if (hasVariadicArgs()) {
					if (args.size() < getFixedArgsCount()) {
							throw new ArityException(
									formatVariadicArityExMsg(
										getQualifiedName(), 
										macro ? FnType.Macro : FnType.Function,
										args.size(), 
										getFixedArgsCount(),
										getArgLists()));
					}
				}
				else if (args.size() != getFixedArgsCount()) {
					throw new ArityException(
							formatArityExMsg(
								getQualifiedName(), 
								macro ? FnType.Macro : FnType.Function,
								args.size(), 
								getFixedArgsCount(),
								getArgLists()));
				}

				final ThreadContext threadCtx = ThreadContext.get();
				
				final CallFrameFnData callFrameFnData = threadCtx.getCallFrameFnData_();
				threadCtx.setCallFrameFnData_(null); // we've got it
								
				final Env localEnv = new Env(env);

				addFnArgsToEnv(args, localEnv);

				if (switchToFunctionNamespaceAtRuntime) {	
					final CallStack callStack = threadCtx.getCallStack_();						
					final DebugAgent debugAgent = threadCtx.getDebugAgent_();
					final Namespace curr_ns = threadCtx.getCurrNS_();
					final String fnName = getQualifiedName();
					
					final boolean pushCallstack = callFrameFnData != null 
													&& callFrameFnData.matchesFnName(fnName);
					if (pushCallstack) {
						callStack.push(new CallFrame(fnName, args, callFrameFnData.getFnMeta(), localEnv));
					}
					
					try {
						threadCtx.setCurrNS_(ns);

						if (debugAgent != null && debugAgent.hasBreakpointFor(new BreakpointFnRef(fnName))) {
							final CallStack cs = threadCtx.getCallStack_();
							try {
								debugAgent.onBreakFnEnter(fnName, this, args, localEnv, cs);
								if (hasPreConditions) {
									validateFnPreconditions(localEnv);
								}
								final VncVal retVal = evaluateBody(body, localEnv, true);
								debugAgent.onBreakFnExit(fnName, this, args, retVal, localEnv, cs);
								return retVal;
							}
							catch(Exception ex) {
								debugAgent.onBreakFnException(fnName, this, args, ex, localEnv, cs);
								throw ex;
							}
						}
						else {
							if (hasPreConditions) {
								validateFnPreconditions(localEnv);
							}
							return evaluateBody(body, localEnv, true);
						}
					}
					finally {
						if (pushCallstack) {
							callStack.pop();
						}

						// switch always back to current namespace, just in case
						// the namespace was changed within the function body!
						threadCtx.setCurrNS_(curr_ns);
					}
				}
				else {
					if (hasPreConditions) {
						validateFnPreconditions(localEnv);
					}
					return evaluateBody(body, localEnv, false);
				}
			}
			
			@Override
			public boolean isNative() { 
				return false;
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
			 		for(VncVal v : preConditions) {
						if (!isFnConditionTrue(evaluate(v, local))) {
							final CallFrame cf = new CallFrame(name, v.getMeta());
							try (WithCallStack cs = new WithCallStack(cf)) {
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
	
	private Env buildRecursionEnv(
			final VncList args, 
			final Env env, 
			final RecursionPoint recursionPoint
	) {
		final Env recur_env = recursionPoint.getLoopEnv();
		
		final int argCount = args.size();
		
		// denormalize for best performance (short loops are performance critical)
		switch(argCount) {
			case 0:
				break;
				
			case 1:
				// [1][2] calculate and bind the single new value
				recur_env.setLocal(
					new Var(
						recursionPoint.getLoopBindingName(0), 
						evaluate(args.first(), env, false)));
				break;
				
			case 2:
				// [1] calculate the new values
				final VncVal v1 = evaluate(args.first(), env, false);
				final VncVal v2 = evaluate(args.second(), env, false);
				
				// [2] bind the new values
				recur_env.setLocal(new Var(recursionPoint.getLoopBindingName(0), v1));
				recur_env.setLocal(new Var(recursionPoint.getLoopBindingName(1), v2));
				break;
				
			default:
				// [1] calculate new values
				final VncVal[] newValues = new VncVal[argCount];
				for(int ii=0; ii<argCount; ii++) {
					newValues[ii] = evaluate(args.nth(ii), env, false);
				}
				
				// [2] bind the new values
				for(int ii=0; ii<argCount; ii++) {
					recur_env.setLocal(new Var(recursionPoint.getLoopBindingName(ii), newValues[ii]));
				}
				break;
		}
		
		return recur_env;
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
	
	private VncVal evaluateBody(final VncList body, final Env env, final boolean withTailPosition) {
		evaluate_values(body.butlast(), env);
		return evaluate(body.last(), env, withTailPosition);
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
				try (WithCallStack cs = new WithCallStack(
												new CallFrame(
														specialFormName, 
														sym.getMeta()))
				) {
					throw new VncException(String.format(
							"Special form '%s': Invalid use of namespace. "
								+ "The symbol '%s' can only be defined for the "
								+ "current namespace '%s'.",
							specialFormName,
							sym.getSimpleName(),
							Namespaces.getCurrentNS().toString()));
				}
			}
		}
		
		return sym;
	}
	
	
	private void specialFormCallValidation(final String name) {
		ThreadContext.getInterceptor().validateVeniceFunction(name);
	}

	
	private static final long serialVersionUID = -8130740279914790685L;

	private static final VncKeyword PRE_CONDITION_KEY = new VncKeyword(":pre");
	
	private static final BreakpointFnRef BREAKPOINT_REF_IF = new BreakpointFnRef("if");
	private static final BreakpointFnRef BREAKPOINT_REF_LET = new BreakpointFnRef("let");
	private static final BreakpointFnRef BREAKPOINT_REF_BINDINGS = new BreakpointFnRef("bindings");
	private static final BreakpointFnRef BREAKPOINT_REF_LOOP = new BreakpointFnRef("loop");
		
	private final IInterceptor interceptor;	
	private final boolean checkSandbox;
	private final MeterRegistry meterRegistry;
	private final NamespaceRegistry nsRegistry;
	
	private final SpecialFormsHandler specialFormHandler;
	
	private final AtomicBoolean sealedSystemNS;
	
	private volatile boolean macroexpand = false;
}
