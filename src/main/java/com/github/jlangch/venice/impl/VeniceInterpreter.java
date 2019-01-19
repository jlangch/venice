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
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import com.github.jlangch.venice.AssertionException;
import com.github.jlangch.venice.Version;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.functions.CoreFunctions;
import com.github.jlangch.venice.impl.functions.Functions;
import com.github.jlangch.venice.impl.javainterop.JavaImports;
import com.github.jlangch.venice.impl.javainterop.JavaInteropFn;
import com.github.jlangch.venice.impl.javainterop.JavaInteropProxifyFn;
import com.github.jlangch.venice.impl.javainterop.SandboxMaxExecutionTimeChecker;
import com.github.jlangch.venice.impl.types.Coerce;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.Types;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncMultiArityFunction;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.util.CallFrameBuilder;
import com.github.jlangch.venice.impl.util.CatchBlock;
import com.github.jlangch.venice.impl.util.Doc;
import com.github.jlangch.venice.impl.util.ThreadLocalMap;
import com.github.jlangch.venice.impl.util.reflect.ReflectionAccessor;
import com.github.jlangch.venice.util.CallFrame;
import com.github.jlangch.venice.util.ScriptElapsedTime;


public class VeniceInterpreter implements Serializable  {

	public VeniceInterpreter() {
		this.sandboxMaxExecutionTimeChecker = new SandboxMaxExecutionTimeChecker();
	}
	
	
	// read
	public VncVal READ(final String script, final String filename) {
		return Reader.read_str(script, filename);
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

	/**
	 * Returns true if ast is a list that contains a symbol as the first element 
	 * and that symbol refers to a function in the env environment and that 
	 * function has the is_macro attribute set to true. 
	 * Otherwise, it returns false.
	 * 
	 * @param ast ast
	 * @param env env
	 * @return true if the ast starts with a macro
	 */
	private boolean is_macro_call(final VncVal ast, final Env env) {
		if (Types.isVncList(ast) && !((VncList)ast).isEmpty()) {
			final VncVal a0 = Coerce.toVncSequence(ast).first();
			if (Types.isVncSymbol(a0)) {
				final VncSymbol macroName = (VncSymbol)a0;
				if (env.findEnv(macroName) != null) {
					final VncVal fn = env.get(macroName);
					if (Types.isVncMacro(fn)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Recursively expands a macro. It calls is_macro_call with ast and env and 
	 * loops while that condition is true. Inside the loop, the first element 
	 * of the ast list (a symbol), is looked up in the environment to get 
	 * the macro function. This macro function is then called/applied with 
	 * the rest of the ast elements (2nd through the last) as arguments. 
	 * The return value of the macro call becomes the new value of ast. 
	 * When the loop completes because ast no longer represents a macro call, 
	 * the current value of ast is returned.
	 * 
	 * @param ast ast
	 * @param env env
	 * @return the expanded macro
	 */
	private VncVal macroexpand(VncVal ast, final Env env) {
		while (is_macro_call(ast, env)) {
			final VncSymbol macroName = Coerce.toVncSymbol(Coerce.toVncSequence(ast).first());
			final VncFunction macroFn = Coerce.toVncFunction(env.get(macroName));
			final VncList macroFnArgs = Coerce.toVncList(ast).rest();
			ast = macroFn.apply(macroFnArgs);
		}
		return ast;
	}

	private VncVal eval_ast(final VncVal ast, final Env env) {
		if (Types.isVncSymbol(ast)) {
			return env.get((VncSymbol)ast);
		} 
		else if (Types.isVncSequence(ast)) {
			final VncSequence seq = (VncSequence)ast;		
			
			return seq.withValues(				
						seq.getList()
						   .stream()
						   .map(v -> EVAL(v, env))
						   .collect(Collectors.toList()));
		}
		else if (Types.isVncMap(ast)) {
			final VncMap map = (VncMap)ast;
			
			return map.withValues(
						map.entries()
						   .stream()
						   .collect(Collectors.toMap(
						            	e -> e.getKey(),
						            	e -> EVAL(e.getValue(), env))));
		} 
		else {
			return ast;
		}
	}

	public VncVal EVAL(VncVal orig_ast, Env env) {
		RecursionPoint recursionPoint = null;
		
		while (true) {
			//System.out.println("EVAL: " + printer._pr_str(orig_ast, true));
			if (!Types.isVncList(orig_ast)) {
				return eval_ast(orig_ast, env);
			}
	
			// apply list
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
				case "def": { // (def meta-data? name value)
					final boolean hasMeta = ast.size() > 3;
					final VncMap defMeta = hasMeta ? (VncHashMap)EVAL(ast.second(), env) : new VncHashMap();
					final VncSymbol defName = Coerce.toVncSymbol(ast.nth(hasMeta ? 2 : 1));
					final VncVal defVal = ast.nth(hasMeta ? 3 : 2);
					final VncVal res = EVAL(defVal, env);
					env.setGlobal(new Var(defName, MetaUtil.addDefMeta(res, defMeta), true));
					return res;
				}
				
				case "defonce": { // (defonce meta-data? name value)
					final boolean hasMeta = ast.size() > 3;
					final VncMap defMeta = hasMeta ? (VncHashMap)EVAL(ast.second(), env) : new VncHashMap();
					final VncSymbol defName = Coerce.toVncSymbol(ast.nth(hasMeta ? 2 : 1));
					final VncVal defVal = ast.nth(hasMeta ? 3 : 2);
					final VncVal res = EVAL(defVal, env);
					env.setGlobal(new Var(defName, MetaUtil.addDefMeta(res, defMeta), false));
					return res;
				}
				
				case "def-dynamic": { // (def-dynamic meta-data? name value)
					final boolean hasMeta = ast.size() > 3;
					final VncMap defMeta = hasMeta ? (VncHashMap)EVAL(ast.second(), env) : new VncHashMap();
					final VncSymbol defName = Coerce.toVncSymbol(ast.nth(hasMeta ? 2 : 1));
					final VncVal defVal = ast.nth(hasMeta ? 3 : 2);
					final VncVal res = EVAL(defVal, env);
					env.setGlobal(new DynamicVar(defName, MetaUtil.addDefMeta(res, defMeta)));
					return res;
				}

				case "doc":
					final String name = ((VncString)CoreFunctions.name.apply(ast.slice(1))).getValue();
					VncVal docVal = SpecialForms.ns.get(new VncSymbol(name));
					if (docVal == null) {
						docVal = env.get(new VncSymbol(name));
					}
					orig_ast = VncList.of(new VncSymbol("println"), Doc.getDoc(docVal));
					break;

				case "eval":
					orig_ast = Coerce.toVncSequence(eval_ast(ast.slice(1), env)).last();
					break;
					
				case "let":  { // (let [bindings*] exprs*)
					env = new Env(env);

					final VncVector bindings = Coerce.toVncVector(ast.second());
					final VncList expressions = ast.slice(2);
				
					for(int i=0; i<bindings.size(); i+=2) {
						final VncVal sym = bindings.nth(i);
						final VncVal val = EVAL(bindings.nth(i+1), env);

						final Env _env = env;
						Destructuring
							.destructure(sym, val)
							.forEach(b -> _env.set(b.sym, b.val));
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
					
					final List<VncVal> bindingNames = new ArrayList<>();
					for(int i=0; i<bindings.size(); i+=2) {
						final VncVal sym = bindings.nth(i);
						final VncVal val = EVAL(bindings.nth(i+1), env);

						final Env _env = env;
						Destructuring
							.destructure(sym, val)
							.forEach(b -> { 
								_env.set(b.sym, b.val); 
								bindingNames.add(b.sym);
							 });
					}
					
					recursionPoint = new RecursionPoint(new VncList(bindingNames), expressions, env);
					orig_ast = expressions;
					break;
				}

				case "recur":  // (recur exprs*)
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

					// [1] calculate new values
					final List<VncVal> recur_values = new ArrayList<>();
					for(int i=1; i<ast.size(); i++) {
						recur_values.add(EVAL(ast.nth(i), env));
					}
					// [2] bind the values
					final VncList recur_bindingNames = recursionPoint.getLoopBindingNames();					
					final Env recur_env = recursionPoint.getLoopEnv();
					for(int i=0; i<recur_bindingNames.size(); i++) {
						final VncSymbol key = Coerce.toVncSymbol(recur_bindingNames.nth(i));
						recur_env.set(key, recur_values.get(i));
					}
					// [3] continue on the loop with the new parameters
					orig_ast = recursionPoint.getLoopExpressions();
					env = recur_env;
					break;
					
				case "quote":
					return ast.second();
					
				case "quasiquote":
					orig_ast = quasiquote(ast.second());
					break;
	
				case "defmacro":
					return runWithCallStack("defmacro", ast, env, (a,e) -> defmacro_(a, e));

				case "macroexpand": 
					return runWithCallStack("macroexpand", ast, env, (a,e) -> macroexpand(a.second(), e));
					
				case "try":  // (try expr (catch :Exception e expr) (finally expr))
					return runWithCallStack("try", ast, env, (a,e) -> try_(a, new Env(e)));
					
				case "try-with": // (try-with [bindings*] expr (catch :Exception e expr) (finally expr))
					return runWithCallStack("try-with", ast, env, (a,e) -> try_with_(a, new Env(e)));

				case "import":
					return runWithCallStack(
								"import", ast, env, 
								(a,e) -> {
									a.slice(1)
									 .stream()
									 .map(clazzName -> Coerce.toVncString(clazzName).getValue())
									 .forEach(clazzName -> javaImports.add(clazzName));
									return Nil;
								});
					
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
					
				case "if": 
					final VncVal condArg = ast.second();
					final VncVal cond = EVAL(condArg, env);
					if (cond == Nil || cond == False) {
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

				default:
					final VncList el = Coerce.toVncList(eval_ast(ast, env));
					final VncVal elFirst = el.first();
					if (Types.isVncFunction(elFirst)) {
						sandboxMaxExecutionTimeChecker.check();
						
						// invoke function
						final VncFunction f = (VncFunction)elFirst;
						final VncList fnArgs = el.rest().withMeta(el.getMeta());
						final CallFrame frame = CallFrameBuilder.fromFunction(f, ast.first());
						
						try {
							ThreadLocalMap.getCallStack().push(frame);
							return f.apply(fnArgs);
						}
						finally {
							ThreadLocalMap.getCallStack().pop();
							sandboxMaxExecutionTimeChecker.check();
						}
					}
					else if (Types.isVncKeyword(elFirst)) {
						// keyword as function to access map: (:a {:a 100})
						final VncKeyword k = (VncKeyword)elFirst;
						final VncList fnArgs = el.rest().withMeta(el.getMeta());
						return k.apply(fnArgs);
					}
					else {
						ThreadLocalMap.getCallStack().push(CallFrameBuilder.fromVal(ast));
						throw new VncException(String.format(
										"Not a function or keyword: '%s'", 
										PRINT(elFirst)));
					}
			}
		}
	}

	// print
	public String PRINT(final VncVal exp) {
		return Printer._pr_str(exp, true);
	}
	
	public VncVal RE(
			final String script, 
			final String filename, 
			final Env env,
			final ScriptElapsedTime elapsedTime
	) {
		final VncVal ast = READ(script, filename);	
		if (elapsedTime != null) elapsedTime.readDone();	
		
		final VncVal result = EVAL(ast, env);
		if (elapsedTime != null) elapsedTime.evalDone();	
		
		return result;
	}
	
	public Env createEnv() {
		return createEnv(null);
	}

	public Env createEnv(final List<String> preloadedExtensionModules) {
		final Env env = new Env(null);
	
		// core functions defined in Java
		Functions.functions
				 .keySet()
				 .forEach(key -> env.set((VncSymbol)key, Functions.functions.get(key)));

		// core functions Java interoperability
		env.set(new VncSymbol("."), JavaInteropFn.create(javaImports)); 
		env.set(new VncSymbol("proxify"), new JavaInteropProxifyFn(javaImports)); 

		// set Venice version
		env.setGlobal(new Var(new VncSymbol("*version*"), new VncString(Version.VERSION)));

		// set system newline
		env.setGlobal(new Var(new VncSymbol("*newline*"), new VncString(System.lineSeparator())));

		// set system stdout (dynamic)
		env.setGlobal(new DynamicVar(new VncSymbol("*out*"), new VncJavaObject(new PrintStream(System.out, true))));

		// core module: core.venice 
		RE("(eval " + ModuleLoader.load("core") + ")", "core.venice", env, null);

		if (preloadedExtensionModules != null) {
			preloadedExtensionModules.forEach(
				m -> RE("(eval " + ModuleLoader.load(m) + ")", m + ".venice", env, null));
		}
		
		return env;
	}

	
	private VncFunction defmacro_(final VncList ast, final Env env) {
		int argPos = 1;

		final boolean hasMeta = Types.isVncMap(ast.nth(argPos));
		
		final VncMap defMeta = hasMeta ? (VncMap)EVAL(ast.nth(argPos++), env) : new VncHashMap();
		final VncVal macroName = ast.nth(argPos++);
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
			env.set((VncSymbol)macroName, MetaUtil.addDefMeta(macroFn, defMeta));
			
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

			final VncFunction macro = new VncMultiArityFunction(sMacroName, fns);
			
			macro.setMacro();
			env.set((VncSymbol)macroName, MetaUtil.addDefMeta(macro, defMeta));

			return macro;
		}
	}
	
	private VncFunction fn_(final VncList ast, final Env env) {
		// single arity:  (fn name? [params*] condition-map? expr*)
		// multi arity:   (fn name? ([params*] condition-map? expr*)+ )

		int argPos = 1;
		
		final String name = getFnName(ast.nth(argPos));
		if (name != null) argPos++;

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

	private VncVal binding_(final VncList ast, final Env env) {
		final VncSequence bindings = Coerce.toVncSequence(ast.second());
		final VncList expressions = ast.slice(2);
	
		final List<Var> vars = new ArrayList<>();
		for(int i=0; i<bindings.size(); i+=2) {
			final VncVal sym = bindings.nth(i);
			final VncVal val = EVAL(bindings.nth(i+1), env);
	
			Destructuring
				.destructure(sym, val)
				.forEach(b -> vars.add(new DynamicVar(b.sym, b.val)));
		}
			
		try {
			vars.forEach(v -> env.pushGlobalDynamic(v));
			
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
			result = EVAL(ast.second(), env);
		} 
		catch (Throwable th) {
			CatchBlock catchBlock = null;
			if (ast.size() > 2) {
				catchBlock = findCatchBlockMatchingThrowable(ast.slice(2), th);
				if (catchBlock != null) {
					env.set(catchBlock.getExSym(), new VncJavaObject(th));
					
					final VncVal blocks = eval_ast(catchBlock.getBody(), env);
					result = Coerce.toVncSequence(blocks).first();
				}
			}
			
			if (catchBlock == null) {
				throw th;
			}
		}
		finally {
			if (ast.size() > 2) {
				final VncList finallyBlock = findFirstFinallyBlock(ast.slice(2));
				if (finallyBlock != null) {
					eval_ast(finallyBlock.slice(1), env);
				}
			}
		}
		
		return result;
	}

	private VncVal try_with_(final VncList ast, final Env env) {
		final VncSequence bindings = Coerce.toVncSequence(ast.second());
		final List<Binding> boundResources = new ArrayList<>();
		
		for(int i=0; i<bindings.size(); i+=2) {
			final VncVal sym = bindings.nth(i);
			final VncVal val = EVAL(bindings.nth(i+1), env);

			if (Types.isVncSymbol(sym)) {
				env.set((VncSymbol)sym, val);
				boundResources.add(new Binding((VncSymbol)sym, val));
			}
			else {
				throw new VncException(
						String.format(
								"Invalid 'try-with' destructuring symbol value type %s. Expected symbol.",
								Types.getClassName(sym)));
			}
		}

		
		VncVal result = Nil;
		try {
			try {
				result = EVAL(ast.nth(2), env);
			} 
			catch (Throwable th) {
				CatchBlock catchBlock = null;
				if (ast.size() > 3) {
					catchBlock = findCatchBlockMatchingThrowable(ast.slice(3), th);
					if (catchBlock != null) {
						env.set(catchBlock.getExSym(), new VncJavaObject(th));

						final VncVal blocks = eval_ast(catchBlock.getBody(), env);
						result = Coerce.toVncSequence(blocks).first();
					}
				}
						
				if (catchBlock == null) {
					throw th;
				}
			}
			finally {
				// finally is only for side effects
				if (ast.size() > 3) {
					final VncList finallyBlock = findFirstFinallyBlock(ast.slice(3));
					if (finallyBlock != null) {
						eval_ast(finallyBlock.slice(1), env);
					}
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

	private CatchBlock findCatchBlockMatchingThrowable(
			final VncList blocks, 
			final Throwable th
	) {
		final VncList block = blocks
								.stream()
								.map(b -> (VncList)b)
								.filter(b -> ((VncSymbol)b.first()).getName().equals("catch"))
								.filter(b -> isCatchBlockMatchingThrowable(b, th))
								.findFirst()
								.orElse(null);
		
		return block == null
				? null
				: new CatchBlock(
						Coerce.toVncSymbol(block.nth(2)), 
						block.slice(3));
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
		for(int ii=0; ii<blocks.size(); ii++) {
			final VncList block = Coerce.toVncList(blocks.nth(ii));
			
			final VncSymbol sym = Coerce.toVncSymbol(block.first());
			if (sym.getName().equals("finally")) {
				return block;
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
				Destructuring
					.destructure(params, args)
					.forEach(b -> localEnv.set(b.sym, b.val));
				
				validateFnPreconditions(name, preConditions, localEnv);
				
				if (body.isEmpty()) {
					return Constants.Nil;
				}
				else if (body.size() == 1) {
					return EVAL(body.first(), localEnv);
				}
				else {
					eval_ast(body.slice(0, body.size()-1), localEnv);
					return EVAL(body.last(), localEnv);
				}
			}
			
		    private static final long serialVersionUID = -1L;
		};
	}

	private String getFnName(final VncVal name) {
		return name == Nil
				? null
				: Types.isVncSymbol(name) ? ((VncSymbol)name).getName() : null;
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
		if (preConditions != null) {
	 		final Env local = new Env(env);	
	 		preConditions.forEach(v -> {
				if (!isFnConditionTrue(EVAL(v, local))) {
					ThreadLocalMap.getCallStack().push(CallFrameBuilder.fromVal(fnName, v));
					throw new AssertionException(
							String.format(
									"pre-condition assert failed: %s",
									((VncString)CoreFunctions.str.apply(VncList.of(v))).getValue()));		
				}
 			});
		}
	}

	private VncVal runWithCallStack(
			final String fnName, 
			final VncList ast, 
			final Env env, 
			final BiFunction<VncList,Env,VncVal> fn
	) {
		try {
			ThreadLocalMap.getCallStack().push(CallFrameBuilder.fromVal(fnName, ast));
			return fn.apply(ast, env);
		}
		finally {
			ThreadLocalMap.getCallStack().pop();
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
		return javaImports.resolveClassName(className);
	}
	
	
	
	private static final long serialVersionUID = -8130740279914790685L;

	private static final VncKeyword PRE_CONDITION_KEY = new VncKeyword(":pre");
	
	private final JavaImports javaImports = new JavaImports();
	
	private final SandboxMaxExecutionTimeChecker sandboxMaxExecutionTimeChecker;
}
