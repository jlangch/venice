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

import static com.github.jlangch.venice.impl.types.Constants.False;
import static com.github.jlangch.venice.impl.types.Constants.Nil;
import static com.github.jlangch.venice.impl.types.Constants.True;

import java.io.Closeable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.jlangch.venice.AssertionException;
import com.github.jlangch.venice.Version;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.functions.CoreFunctions;
import com.github.jlangch.venice.impl.functions.Functions;
import com.github.jlangch.venice.impl.javainterop.JavaImports;
import com.github.jlangch.venice.impl.javainterop.SandboxMaxExecutionTimeChecker;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.IVncFunction;
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
import com.github.jlangch.venice.impl.types.collections.VncMutableMap;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.concurrent.ThreadLocalMap;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.CallFrame;
import com.github.jlangch.venice.impl.util.CallStack;
import com.github.jlangch.venice.impl.util.CatchBlock;
import com.github.jlangch.venice.impl.util.Doc;
import com.github.jlangch.venice.impl.util.MeterRegistry;
import com.github.jlangch.venice.impl.util.WithCallStack;
import com.github.jlangch.venice.impl.util.reflect.ReflectionAccessor;
import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;
import com.github.jlangch.venice.javainterop.IInterceptor;


public class VeniceInterpreter implements Serializable  {

	public VeniceInterpreter() {
		this(new MeterRegistry(false), new AcceptAllInterceptor());
	}

	public VeniceInterpreter(final IInterceptor interceptor) {
		this(new MeterRegistry(false), interceptor);
	}

	public VeniceInterpreter(
			final MeterRegistry perfmeter, 
			final IInterceptor interceptor
	) {
		this.sandboxMaxExecutionTimeChecker = new SandboxMaxExecutionTimeChecker();
		this.meterRegistry = perfmeter;
		this.interceptor = interceptor;
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

	// print
	public String PRINT(final VncVal exp) {
		return Printer.pr_str(exp, true);
	}
	
	public VncVal RE(
			final String script, 
			final String filename, 
			final Env env
	) {
		final VncVal ast = READ(script, filename);			
		final VncVal result = EVAL(ast, env);		
		return result;
	}
	
	public Env createEnv() {  
		return createEnv(null);
	}

	public Env createEnv(final List<String> preloadExtensionModules) {
		final Env env = new Env(null);
	
		final VncMutableMap loadedModules = new VncMutableMap();
		
		Functions
			.create(javaImports)
			.entrySet()
			.forEach(e -> env.setGlobal(
							new Var(
								(VncSymbol)e.getKey(), 
								e.getValue(), 
								((VncFunction)e.getValue()).isRedefinable())));

		// set Venice version
		env.setGlobal(new Var(new VncSymbol("*version*"), new VncString(Version.VERSION), false));

		// set system newline
		env.setGlobal(new Var(new VncSymbol("*newline*"), new VncString(System.lineSeparator()), false));
		
		// loaded modules
		env.setGlobal(new Var(new VncSymbol("*loaded-modules*"), loadedModules, false));

		// current namespace
		env.pushGlobalDynamic(new VncSymbol("*ns*"), new VncSymbol("user"));

		// load modules
		final List<String> modules = new ArrayList<>();
		modules.add("core");
		modules.addAll(toEmpty(preloadExtensionModules));
		
		modules.forEach(m -> {
			final long nanos = System.nanoTime();
			RE("(eval " + ModuleLoader.load(m) + ")", m, env);
			meterRegistry.record("venice.module." + m + ".load", System.nanoTime() - nanos);
			loadedModules.assoc(new VncKeyword(m), new VncLong(System.currentTimeMillis()));
		});
		
		return env;
	}
	
	public List<String> getAvailableModules() {
		final List<String> modules = new ArrayList<>(ModuleLoader.VALID_MODULES);
		modules.removeAll(Arrays.asList("core", "test", "http", "jackson", "logger"));
		Collections.sort(modules);
		return modules;
	}


	private VncVal evaluate(VncVal orig_ast, Env env) {
		RecursionPoint recursionPoint = null;
		
		while (true) {
			//System.out.println("EVAL: " + printer._pr_str(orig_ast, true));
			if (!Types.isVncList(orig_ast)) {
				return eval_ast(orig_ast, env);
			}
	
			// expand macros
			final VncVal expanded = macroexpand(orig_ast, env);
			if (!Types.isVncList(expanded)) {
				return eval_ast(expanded, env);
			}
			
			final VncList ast = (VncList)expanded;
			if (ast.isEmpty()) { 
				return ast; 
			}
			
			final VncVal a0 = ast.first();		
			final String a0sym = Types.isVncSymbol(a0) ? ((VncSymbol)a0).getName() : "__<*fn*>__";
			
			switch (a0sym) {		
				case "do":
					if (ast.size() < 2) {
						orig_ast = Constants.Nil;
					}
					else {
						final VncList head_exprs = ast.slice(1, ast.size()-1);
						eval_ast(head_exprs, env);
						orig_ast = ast.last();
					}
					break;
					
				case "def": { // (def name value)
					VncSymbol defName = Coerce.toVncSymbol(ast.second());
					defName = defName.withMeta(evaluate(defName.getMeta(), env));
					ReservedSymbols.validate(defName);
					final VncVal defVal = ast.third();
					final VncVal res = evaluate(defVal, env).withMeta(defName.getMeta());
					env.setGlobal(new Var(defName, res, true));
					return res;
				}
				
				case "defonce": { // (defonce name value)
					VncSymbol defName = Coerce.toVncSymbol(ast.second());
					defName = defName.withMeta(evaluate(defName.getMeta(), env));
					ReservedSymbols.validate(defName);
					final VncVal defVal = ast.third();
					final VncVal res = evaluate(defVal, env).withMeta(defName.getMeta());
					env.setGlobal(new Var(defName, res, false));
					return res;
				}
				
				case "defmulti": { // (defmulti name dispatch-fn)
					VncSymbol multiFnName = Coerce.toVncSymbol(ast.second());
					multiFnName = multiFnName.withMeta(evaluate(multiFnName.getMeta(), env));
					ReservedSymbols.validate(multiFnName);
					final VncFunction dispatchFn = fn_(Coerce.toVncList(ast.third()), env);
					final VncMultiFunction multiFn = new VncMultiFunction(multiFnName.getName(), dispatchFn);
					env.setGlobal(new Var(multiFnName, multiFn, false));
					return multiFn;
				}
				
				case "defmethod": { // (defmethod multifn-name dispatch-val & fn-tail)
					final VncSymbol multiFnName = Coerce.toVncSymbol(ast.nth(1));
					final VncVal multiFnVal = env.getGlobalOrNull(multiFnName);
					if (multiFnVal == null) {
						try (WithCallStack cs = new WithCallStack(CallFrame.fromVal(ast))) {
							throw new VncException(String.format(
										"No multifunction '%s' defined for the method definition", 
										multiFnName.getName())); 
						}
					}
					final VncMultiFunction multiFn = Coerce.toVncMultiFunction(multiFnVal);
					final VncVal dispatchVal = ast.nth(2);
					
					final VncVector params = Coerce.toVncVector(ast.nth(3));
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
					final VncFunction fn = buildFunction(multiFnName.getName(), params, body, preConditions, env);

					return multiFn.addFn(dispatchVal, fn);
				}
								
				case "def-dynamic": { // (def-dynamic name value)
					VncSymbol defName = Coerce.toVncSymbol(ast.second());
					defName = defName.withMeta(evaluate(defName.getMeta(), env));
					ReservedSymbols.validate(defName);
					final VncVal defVal = ast.third();
					final VncVal res = evaluate(defVal, env).withMeta(defName.getMeta());
					env.setGlobal(new DynamicVar(defName, res));
					return res;
				}
				
				case "resolve": { // (resolve sym)
					final VncSymbol sym = Coerce.toVncSymbol(evaluate(ast.second(), env));
					return env.getOrNil(sym);
				}

				case "defmacro":
					try (WithCallStack cs = new WithCallStack(CallFrame.fromVal("defmacro", ast))) {
						return defmacro_(ast, env);
					}
	
				case "macroexpand": 
					try (WithCallStack cs = new WithCallStack(CallFrame.fromVal("macroexpand", ast))) {
						return macroexpand(ast.second(), env);
					}
					
				case "quote":
					return ast.second();
					
				case "quasiquote":
					orig_ast = quasiquote(ast.second());
					break;
	
				case "doc":
					final String name = ((VncString)CoreFunctions.name.apply(ast.rest())).getValue();
					VncVal docVal = SpecialForms.ns.get(new VncSymbol(name));
					if (docVal == null) {
						docVal = env.get(new VncSymbol(name));
					}
					orig_ast = VncList.of(new VncSymbol("println"), Doc.getDoc(docVal));
					break;
	
				case "ns": { // (ns alpha)
					final VncSymbol ns = (VncSymbol)ast.second();
					env.setGlobalDynamic(new VncSymbol("*ns*"), ns);
					return Nil;
				}
					
				case "eval": 
					orig_ast = Coerce.toVncSequence(eval_ast(ast.rest(), env)).last();
					break;
					
				case "let":  { // (let [bindings*] exprs*)
					env = new Env(env);
	
					final VncVector bindings = Coerce.toVncVector(ast.second());
					final VncList expressions = ast.slice(2);
				
					for(int i=0; i<bindings.size(); i+=2) {
						final VncVal sym = bindings.nth(i);
						final VncVal val = evaluate(bindings.nth(i+1), env);

						for(Binding b : Destructuring.destructure(sym, val)) {
							env.set(b.sym, b.val);
						}
					}
						
					if (expressions.isEmpty()) {
						orig_ast = Constants.Nil;
					}
					else {
						eval_ast(expressions.slice(0, expressions.size()-1), env);
						orig_ast = expressions.last();
					}
					break;
				}
				
				case "binding":  // (binding [bindings*] exprs*)
					return binding_(ast, new Env(env));
					
				case "loop": { // (loop [bindings*] exprs*)
					env = new Env(env);					
	
					final VncVector bindings = Coerce.toVncVector(ast.second());
					final VncVal expressions = ast.nth(2);
					
					final List<VncSymbol> bindingNames = new ArrayList<>();
					for(int i=0; i<bindings.size(); i+=2) {
						final VncVal sym = bindings.nth(i);
						final VncVal val = evaluate(bindings.nth(i+1), env);
	
						env.set((VncSymbol)sym, val);
						bindingNames.add((VncSymbol)sym);
	
						//for(Binding b : Destructuring.destructure(sym, val)) {
						//	env.set(b.sym, b.val);
						//	bindingNames.add(b.sym);
						//}
					}
					
					recursionPoint = new RecursionPoint(bindingNames, expressions, env);
					orig_ast = expressions;
					break;
				}
	
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
	
					final List<VncSymbol> bindingNames = recursionPoint.getLoopBindingNames();
					final Env recur_env = recursionPoint.getLoopEnv();
	
					if (ast.size() == 2) {
						// [1][2] calculate and bind the single new value
						recur_env.set(bindingNames.get(0), evaluate(ast.second(), env));
					}
					else if (ast.size() == 3) {
						// [1][2] calculate and bind the new values
						final VncVal v1 = evaluate(ast.second(), env);
						final VncVal v2 = evaluate(ast.third(), env);
						recur_env.set(bindingNames.get(0), v1);
						recur_env.set(bindingNames.get(1), v2);
					}
					else {
						// [1] calculate new values
						final VncList values = ast.rest();
						final VncVal[] newValues = new VncVal[values.size()];
						int kk=0;
						for(VncVal v : values.getList()) {
							newValues[kk++] = evaluate(v, env);
						}
						
						// [2] bind the new values
						for(int ii=0; ii<bindingNames.size(); ii++) {
							recur_env.set(bindingNames.get(ii), newValues[ii]);
						}
					}
					
					// [3] continue on the loop with the new parameters
					orig_ast = recursionPoint.getLoopExpressions();
					env = recur_env;
					break;
				}
				
				case "set!": { // (set! var-symbol expr)
					VncSymbol sym = Coerce.toVncSymbol(ast.second());
					sym = sym.withMeta(evaluate(sym.getMeta(), env));
					final Var globVar = env.getGlobalVarOrNull(sym);
					if (globVar != null) {
						final VncVal expr = ast.third();
						final VncVal res = evaluate(expr, env).withMeta(sym.getMeta());
						
						if (globVar instanceof DynamicVar) {
							env.popGlobalDynamic(sym);
							env.pushGlobalDynamic(sym, res);
						}
						else {
							env.setGlobal(new Var(sym, res));
						}
						return res;
					}
					else {
						try (WithCallStack cs = new WithCallStack(CallFrame.fromVal(sym))) {
							throw new VncException(String.format(
										"The global var or thread-local '%s' does not exist!", 
										sym.getName()));
						}
					}
				}
					
				case "try":  // (try expr (catch :Exception e expr) (finally expr))
					try (WithCallStack cs = new WithCallStack(CallFrame.fromVal("try", ast))) {
						return try_(ast, new Env(env));
					}
					
				case "try-with": // (try-with [bindings*] expr (catch :Exception e expr) (finally expr))
					try (WithCallStack cs = new WithCallStack(CallFrame.fromVal("try-with", ast))) {
						return try_with_(ast, new Env(env));
					}
	
				case "import":
					try (WithCallStack cs = new WithCallStack(CallFrame.fromVal("import", ast))) {
						ast.rest().forEach(i -> javaImports.add(Coerce.toVncString(i).getValue()));
						return Nil;
					}
					
				case "dorun":
					return dorun_(ast, env);
				
				case "dobench":
					return dobench_(ast, env);
					
				case "if": 
					final VncVal cond = evaluate(ast.second(), env);
					if (cond == False || cond == Nil) {
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
	
				default:
					final long nanos = System.nanoTime();
					
					final VncList el = (VncList)eval_ast((VncList)ast, env);
					final VncVal elFirst = el.first();
					final VncList elArgs = el.rest();
					if (Types.isVncFunction(elFirst)) {
						final VncFunction fn = (VncFunction)elFirst;
						
						// validate function call allowed by sandbox
						interceptor.validateVeniceFunction(fn.getName());	
	
						final CallStack callStack = ThreadLocalMap.getCallStack();
						
//						// private functions may be called from the same module only
//						if (fn.isPrivate()) {
//							validatePrivateFnCall(fn, a0, callStack);
//						}
						
						sandboxMaxExecutionTimeChecker.check();
						checkInterrupted();
	
						// invoke function with call frame
						try {
							callStack.push(CallFrame.fromFunction(fn, a0));

							final VncVal val = fn.apply(elArgs);
							
							if (meterRegistry.enabled) {
								meterRegistry.record(fn.getName(), System.nanoTime() - nanos);
							}
							
							return val;
						}
						finally {
							callStack.pop();
							checkInterrupted();
							sandboxMaxExecutionTimeChecker.check();
						}
					}
					else if (Types.isIVncFunction(elFirst)) {
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

	private VncVal eval_ast(final VncVal ast, final Env env) {
		if (Types.isVncSymbol(ast)) {
			return env.get((VncSymbol)ast);
		} 
		else if (Types.isVncSequence(ast)) {
			final VncSequence seq = (VncSequence)ast;
			
			switch(seq.size()) {
				case 0: 
					return seq;
				case 1: 
					return seq.withVariadicValues(evaluate(seq.first(), env));
				case 2: 
					return seq.withVariadicValues(evaluate(seq.first(), env), evaluate(seq.second(), env));
				default: 
					final List<VncVal> vals = new ArrayList<>();
					for(VncVal v : seq.getList()) {
						vals.add(evaluate(v, env));
					}
					return seq.withValues(vals);
			}
		}
		else if (Types.isVncMap(ast)) {
			final VncMap map = (VncMap)ast;
			
			final Map<VncVal,VncVal> vals = new HashMap<>();
			for(Entry<VncVal,VncVal> e: map.getMap().entrySet()) {
				vals.put(
					evaluate(e.getKey(), env), 
					evaluate(e.getValue(), env));
			}
			return map.withValues(vals);
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
		final long nanos = System.nanoTime();
		
		VncVal ast_ = ast;
		boolean expanded = false;
		
		while(Types.isVncList(ast_)) {
			final VncVal a0 = ((VncList)ast_).first();
			if (!Types.isVncSymbol(a0)) break;
			
			final VncVal fn = env.getGlobalOrNull((VncSymbol)a0);
			if (!Types.isVncMacro(fn)) break;
			
			// validate that the macro allowed by the sandbox
			interceptor.validateVeniceFunction(((VncFunction)fn).getName());					

			expanded = true;
			ast_ = ((VncFunction)fn).apply(((VncList)ast_).rest());
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

		VncVal macroName = ast.nth(argPos++);
		macroName = macroName.withMeta(evaluate(macroName.getMeta(), env));
		final VncSequence paramsOrSig = Coerce.toVncSequence(ast.nth(argPos));

		final String sMacroName = Types.isVncSymbol(macroName) 
									? ((VncSymbol)macroName).getName() 
									: ((VncString)macroName).getValue();

		if (Types.isVncVector(paramsOrSig)) {
			// single arity:
			
			argPos++;
			final VncVector macroParams = (VncVector)paramsOrSig;

			final VncVal body = ast.nth(argPos++);
	
	
			final VncFunction macroFn = buildFunction(
											sMacroName, 
											macroParams, 
											VncList.of(body), 
											null, 
											env);
	
			macroFn.setMacro();
			env.setGlobal(new Var((VncSymbol)macroName, macroFn.withMeta(macroName.getMeta()), false));
			
			return macroFn;
		}
		else {
			// multi arity:

			final List<VncFunction> fns = new ArrayList<>();
			
			ast.slice(argPos).forEach(s -> {
				int pos = 0;
				
				final VncList sig = Coerce.toVncList(s);
				
				final VncVector params = Coerce.toVncVector(sig.nth(pos++));
				
				final VncList body = sig.slice(pos);
				
				fns.add(buildFunction(sMacroName, params, body, null, env));
			});

			final VncFunction macro = new VncMultiArityFunction(sMacroName, fns).withMeta(macroName.getMeta());
			
			macro.setMacro();
			env.setGlobal(new Var((VncSymbol)macroName, macro, false));

			return macro;
		}
	}

	private VncVal dorun_(final VncList ast, final Env env) {
		if (ast.size() != 3) {
			try (WithCallStack cs = new WithCallStack(CallFrame.fromVal("dorun", ast))) {
				throw new VncException("dorun requires two arguments a count and an expression to run");
			}
		}
		
		final long count = Coerce.toVncLong(ast.second()).getValue();
		final VncList expr = VncList.of(ast.third());

		try {
			final VncVal first = ((VncList)eval_ast(expr, env)).first();
			
			for(int ii=1; ii<count; ii++) {
				final VncVal result = eval_ast(expr, env);
				
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
			final VncList expr = VncList.of(ast.third());
			
			final List<VncVal> elapsed = new ArrayList<>();
			for(int ii=0; ii<count; ii++) {
				final long start = System.nanoTime();
				final VncVal result = eval_ast(expr, env);
				final long end = System.nanoTime();
				elapsed.add(new VncLong(end-start));
				
				// store value to a mutable place to prevent JIT from optimizing too much
				ThreadLocalMap.set(new VncKeyword("*benchmark-val*"), result);
			}
			
			return new VncList(elapsed);
		}
		finally {
			ThreadLocalMap.remove(new VncKeyword("*benchmark-val*"));
		}
	}

	private VncFunction fn_(final VncList ast, final Env env) {
		// single arity:  (fn name? [params*] condition-map? expr*)
		// multi arity:   (fn name? ([params*] condition-map? expr*)+ )

		int argPos = 1;
		
		final VncSymbol sName = getFnName(ast.nth(argPos));
		ReservedSymbols.validate(sName);
		final String name = sName == null ? null : sName.getName();
		if (name != null) {
			argPos++;
		}

		final VncSequence paramsOrSig = Coerce.toVncSequence(ast.nth(argPos));
		if (Types.isVncVector(paramsOrSig)) {
			// single arity:
			
			argPos++;
			final VncVector params = (VncVector)paramsOrSig;
			
			final VncVector preConditions = getFnPreconditions(ast.nth(argPos));
			if (preConditions != null) argPos++;
			
			final VncList body = ast.slice(argPos);
			
			return buildFunction(name, params, body, preConditions, env);
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
				
				fns.add(buildFunction(name, params, body, preConditions, env));
			});
			
			return new VncMultiArityFunction(name, fns);
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
			
			if (expressions.isEmpty()) {
				return Constants.Nil;
			}
			else {
				eval_ast(expressions.slice(0, expressions.size()-1), env);
				return ((VncList)eval_ast(VncList.of(expressions.last()), env)).first();
			}
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
				env.set(catchBlock.getExSym(), new VncJavaObject(th));
				
				return evaluateBody(catchBlock.getBody(), env);
			}
		}
		finally {
			final VncList finallyBlock = findFirstFinallyBlock(ast);
			if (finallyBlock != null) {
				eval_ast(finallyBlock.rest(), env);
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
				env.set((VncSymbol)sym, val);
				boundResources.add(new Binding((VncSymbol)sym, val));
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
					env.set(catchBlock.getExSym(), new VncJavaObject(th));
				
					return evaluateBody(catchBlock.getBody(), env);
				}
			}
			finally {
				// finally is only for side effects
				final VncList finallyBlock = findFirstFinallyBlock(ast);
				if (finallyBlock != null) {
					eval_ast(finallyBlock.rest(), env);
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
							throw new VncException(
									String.format(
											"'try-with' failed to close resource %s.",
											b.sym.getName()));
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
											b.sym.getName()));
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
		
		return new VncList(body);
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
			final Env env
	) {
		return new VncFunction(name, body, env, params) {
			public VncVal apply(final VncList args) {
				final Env localEnv = new Env(env);

				// destructuring fn params -> args
				localEnv.addAll(Destructuring.destructure(params, args));

				validateFnPreconditions(name, preConditions, localEnv);
				
				return evaluateBody(body, localEnv);
			}
			
			private static final long serialVersionUID = -1L;
		};
	}

	private VncSymbol getFnName(final VncVal name) {
		return name == Nil
				? null
				: Types.isVncSymbol(name) ? (VncSymbol)name : null;
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
				? ((VncSequence)result).first() == True 
				: result == True;
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
			eval_ast(body.slice(0, body.size()-1), env);
			return evaluate(body.last(), env);
		}
	}
	
//	private void validatePrivateFnCall(
//			final VncFunction fn, 
//			final VncVal fnAst, 
//			final CallStack callStack
//	) {
//		final String callerModule = callStack.peekModule();
//		if (callerModule == null || !callerModule.equals(fn.getModule())) {
//			final CallFrame callFrame = callStack.peek();
//			final String callerFnName = callFrame == null ? null : callFrame.getFnName();								
//			try (WithCallStack cs = new WithCallStack(CallFrame.fromFunction(fn, fnAst))) {
//				throw new VncException(String.format(
//						"Illegal call of private function %s (module %s). Called by %s (module %s).\n%s", 
//						fn.getName(),
//						fn.getModule(),
//						callerFnName,
//						callerModule,
//						callStack.toString()));
//			}
//		}
//	}

	/**
	 * Resolves a class name.
	 * 
	 * @param className A simple class name like 'Math' or a class name
	 *                  'java.lang.Math'
	 * @return the mapped class 'Math' -&gt; 'java.lang.Math' or the passed 
	 *         value if a mapping does nor exist 
	 */
	private String resolveClassName(final String className) {
		return javaImports.resolveClassName(className);
	}
	
	private void checkInterrupted() {
		if (Thread.currentThread().isInterrupted()) {
			throw new com.github.jlangch.venice.InterruptedException("interrupted");
		}
	}

	private static <T> List<T> toEmpty(final List<T> list) {
		return list == null ? new ArrayList<T>() : list;
	}
	
	private static final long serialVersionUID = -8130740279914790685L;

	private static final VncKeyword PRE_CONDITION_KEY = new VncKeyword(":pre");
	
	private final JavaImports javaImports = new JavaImports();	
	private final IInterceptor interceptor;	
	private final SandboxMaxExecutionTimeChecker sandboxMaxExecutionTimeChecker;	
	private final MeterRegistry meterRegistry;
}
