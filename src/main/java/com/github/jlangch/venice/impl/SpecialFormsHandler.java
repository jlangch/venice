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
import static com.github.jlangch.venice.impl.util.ArityExceptions.assertArity;
import static com.github.jlangch.venice.impl.util.ArityExceptions.assertMinArity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.github.jlangch.venice.NotInTailPositionException;
import com.github.jlangch.venice.ValueException;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.debug.agent.DebugAgent;
import com.github.jlangch.venice.impl.debug.breakpoint.BreakpointFnRef;
import com.github.jlangch.venice.impl.docgen.runtime.DocForm;
import com.github.jlangch.venice.impl.env.DynamicVar;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.env.GenSym;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.functions.CoreFunctions;
import com.github.jlangch.venice.impl.specialforms.CatchBlock;
import com.github.jlangch.venice.impl.specialforms.DefTypeForm;
import com.github.jlangch.venice.impl.specialforms.FinallyBlock;
import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.types.INamespaceAware;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncJust;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncMultiArityFunction;
import com.github.jlangch.venice.impl.types.VncProtocolFunction;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.custom.CustomWrappableTypes;
import com.github.jlangch.venice.impl.types.custom.VncProtocol;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions.FnType;
import com.github.jlangch.venice.impl.util.CallFrame;
import com.github.jlangch.venice.impl.util.CallStack;
import com.github.jlangch.venice.impl.util.Inspector;
import com.github.jlangch.venice.impl.util.MetaUtil;
import com.github.jlangch.venice.impl.util.MeterRegistry;
import com.github.jlangch.venice.impl.util.WithCallStack;
import com.github.jlangch.venice.impl.util.reflect.ReflectionAccessor;


public class SpecialFormsHandler {

	public SpecialFormsHandler(
			final IVeniceInterpreter interpreter,
			final IFormEvaluator evaluator,
			final IValuesEvaluator valuesEvaluator,
			final NamespaceRegistry nsRegistry,
			final MeterRegistry meterRegistry,
			final AtomicBoolean sealedSystemNS
	) {
		this.interpreter = interpreter;
		this.evaluator = evaluator;
		this.valuesEvaluator = valuesEvaluator;
		this.nsRegistry = nsRegistry;
		this.meterRegistry = meterRegistry;
		this.sealedSystemNS = sealedSystemNS;
	}


	public VncVal quote_(
			final VncList args, 
			final Env env,
			final VncVal meta
	) {
		if (args.size() != 1) {
			// only create callstack when needed!
			final CallFrame callframe = new CallFrame("quote", args, meta);
			try (WithCallStack cs = new WithCallStack(callframe)) {
				assertArity("quote", FnType.SpecialForm, args, 1);
			}
		}
		return args.first();
	}

	public VncVal quasiquote_(
			final VncList args, 
			final Env env,
			final VncVal meta
	) {
		if (args.size() != 1) {
			// only create callstack when needed!
			final CallFrame callframe = new CallFrame("quasiquote", args, meta);
			try (WithCallStack cs = new WithCallStack(callframe)) {
				assertArity("quasiquote", FnType.SpecialForm, args, 1);
			}
		}
		return quasiquote(args.first());
	}

	public VncVal import_(
			final VncList args, 
			final Env env,
			final VncVal meta
	) {
		final CallFrame callframe = new CallFrame("import", args, meta);
		try (WithCallStack cs = new WithCallStack(callframe)) {
			assertMinArity("import", FnType.SpecialForm, args, 0);
			args.forEach(i -> Namespaces
								.getCurrentNamespace()
								.getJavaImports()
								.add(Coerce.toVncString(i).getValue()));
			return Nil;
		}
	}

	public VncVal imports_(
			final VncList args, 
			final Env env,
			final VncVal meta
	) {
		final CallFrame callframe = new CallFrame("imports", args, meta);
		try (WithCallStack cs = new WithCallStack(callframe)) {
			if (args.isEmpty()) {
				return Namespaces.getCurrentNamespace().getJavaImportsAsVncList();
			}
			else {
				final VncSymbol ns = Coerce.toVncSymbol(args.first());
				final Namespace namespace = nsRegistry.get(ns);
				if (namespace != null) {
					return namespace.getJavaImportsAsVncList();
				}
				else {
					throw new VncException(String.format(
						"The namespace '%s' does not exist", 
						ns.toString()));
				}
			}
		}
	}


	public VncVal ns_(
			final VncList args, 
			final Env env,
			final VncVal meta
	) {
		final CallFrame callframe = new CallFrame("ns", args, meta);
		try (WithCallStack cs = new WithCallStack(callframe)) {
			specialFormCallValidation("ns");
			assertArity("ns", FnType.SpecialForm, args, 1);

			final VncVal name = args.first();
			final VncSymbol ns = Types.isVncSymbol(name)
									? (VncSymbol)name
									: (VncSymbol)CoreFunctions.symbol.apply(VncList.of(evaluator.evaluate(name, env, false)));
			
			if (ns.hasNamespace() && !"core".equals(ns.getNamespace())) {
				throw new VncException(String.format(
						"A namespace '%s' must not have itself a namespace! However you can use '%s'.",
						ns.getQualifiedName(),
						ns.getNamespace() + "." + ns.getSimpleName()));
			}
			else {
				// clean
				final VncSymbol ns_ = new VncSymbol(ns.getSimpleName());
				
				if (Namespaces.isSystemNS(ns_.getSimpleName()) && sealedSystemNS.get()) {
					// prevent Venice's system namespaces from being altered
					throw new VncException("Namespace '" + ns_.getName() + "' cannot be reopened!");
				}
				Namespaces.setCurrentNamespace(nsRegistry.computeIfAbsent(ns_));
				return ns_;
			}
		}
	}
	
	public VncVal ns_remove_(
			final VncList args, 
			final Env env,
			final VncVal meta
	) {
		final CallFrame callframe = new CallFrame("ns-remove", args, meta);
		try (WithCallStack cs = new WithCallStack(callframe)) {
			specialFormCallValidation("ns-remove");
			assertArity("ns-remove", FnType.SpecialForm, args, 1);

			final VncSymbol ns = Namespaces.lookupNS(args.first(), env);
			final VncSymbol nsCurr = Namespaces.getCurrentNS();
			if (Namespaces.isSystemNS(ns.getName()) && sealedSystemNS.get()) {
				// prevent Venice's system namespaces from being altered
				throw new VncException("Namespace '" + ns.getName() + "' cannot be removed!");
			}
			else if (ns.equals(nsCurr)) {
				// prevent removing the current namespace
				throw new VncException("The current samespace '" + nsCurr.getName() + "' cannot be removed!");
			}
			else {
				env.removeGlobalSymbolsByNS(ns);
				nsRegistry.remove(ns);
				return Nil;
			}
		}
	}
	
	public VncVal ns_unmap_(
			final VncList args, 
			final Env env,
			final VncVal meta
	) {
		final CallFrame callframe = new CallFrame("ns-unmap", args, meta);
		try (WithCallStack cs = new WithCallStack(callframe)) {
			specialFormCallValidation("ns-unmap");
			assertArity("ns-unmap", FnType.SpecialForm, args, 2);

			final VncSymbol ns = Namespaces.lookupNS(args.first(), env);
			if (Namespaces.isSystemNS(ns.getName()) && sealedSystemNS.get()) {
				// prevent Venice's system namespaces from being altered
				throw new VncException("Cannot remove a symbol from namespace '" + ns.getName() + "'!");
			}
			else {
				final VncSymbol sym = Coerce.toVncSymbol(args.second()).withNamespace(ns);
				env.removeGlobalSymbol(sym);
				return Nil;
			}
		}
	}
	
	public VncVal ns_list_(
			final VncList args, 
			final Env env,
			final VncVal meta
	) {
		final CallFrame callframe = new CallFrame("ns-list", args, meta);
		try (WithCallStack cs = new WithCallStack(callframe)) {
			specialFormCallValidation("ns-list");
			assertArity("ns-list", FnType.SpecialForm, args, 1);

			final VncSymbol ns = Namespaces.lookupNS(args.first(), env);

			final String nsCore = Namespaces.NS_CORE.getName();
			final String nsName = nsCore.equals(ns.getName()) ? null : ns.getName();
						
			return VncList.ofList(
						env.getAllGlobalSymbols()
							.keySet()
							.stream()
							.filter(s -> Objects.equals(nsName, s.getNamespace()))
							.sorted()
							.collect(Collectors.toList()));
		}
	}

	public VncVal locking_(
			final VncList args, 
			final Env env,
			final VncVal meta
	) {
		final CallFrame callframe = new CallFrame("locking", args, meta);
		try (WithCallStack cs = new WithCallStack(callframe)) {
			assertMinArity("locking", FnType.SpecialForm, args, 2);
			
			final VncVal mutex = evaluator.evaluate(args.first(), env, false);
	
			synchronized(mutex) {
				return evaluateBody(args.rest(), env, true);
			}
		}
	}

	public VncVal setBANG_(
			final VncList args, 
			final Env env,
			final VncVal meta
	) {
		final CallFrame callframe = new CallFrame("set!", args, meta);
		try (WithCallStack cs = new WithCallStack(callframe)) {
			specialFormCallValidation("set!");
			assertArity("set!", FnType.SpecialForm, args, 2);
	
			final VncSymbol sym = Types.isVncSymbol(args.first())
									? (VncSymbol)args.first()
									: Coerce.toVncSymbol(evaluator.evaluate(args.first(), env, false));
			final Var globVar = env.getGlobalVarOrNull(sym);
			if (globVar != null) {
				final VncVal expr = args.second();
				final VncVal val = evaluator.evaluate(expr, env, false);
				
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
				throw new VncException(String.format(
							"The global or thread-local var '%s' does not exist!", 
							sym.getName()));
			}
		}
	}

	public VncVal modules_(
			final VncList args, 
			final Env env,
			final VncVal meta
	) {
		final CallFrame callframe = new CallFrame("modules", args, meta);
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
	
	public VncVal namespace_(
			final VncList args, 
			final Env env,
			final VncVal meta
	) {
		final CallFrame callframe = new CallFrame("namespace", args, meta);
		try (WithCallStack cs = new WithCallStack(callframe)) {
			assertArity("namespace", FnType.SpecialForm, args, 1);
			final VncVal val = evaluator.evaluate(args.first(), env, false);
			if (val instanceof INamespaceAware) {
				return new VncString(((INamespaceAware)val).getNamespace());
			}
			else {
				throw new VncException(String.format(
						"The type '%s' does not support namespaces!",
						Types.getType(val)));
			}
		}
	}

	public VncVal resolve_(
			final VncList args, 
			final Env env,
			final VncVal meta
	) {
		final CallFrame callframe = new CallFrame("resolve", args, meta);
		try (WithCallStack cs = new WithCallStack(callframe)) {
			specialFormCallValidation("resolve");
			assertArity("resolve", FnType.SpecialForm, args, 1);
			return env.getOrNil(Coerce.toVncSymbol(evaluator.evaluate(args.first(), env, false)));
		}
	}

	public VncVal var_get_(
			final VncList args, 
			final Env env,
			final VncVal meta
	) {
		final CallFrame callframe = new CallFrame("var-get", args, meta);
		try (WithCallStack cs = new WithCallStack(callframe)) {
			specialFormCallValidation("var-get");
			assertArity("var-get", FnType.SpecialForm, args, 1);
			final VncSymbol sym = Types.isVncSymbol(args.first())
									? (VncSymbol)args.first()
									: Coerce.toVncSymbol(evaluator.evaluate(args.first(), env, false));
			return env.getOrNil(sym);
		}
	}

	public VncVal var_ns_(
			final VncList args, 
			final Env env,
			final VncVal meta
	) {
		final CallFrame callframe = new CallFrame("var-ns", args, meta);
		try (WithCallStack cs = new WithCallStack(callframe)) {
			specialFormCallValidation("var-ns");
			assertArity("var-ns", FnType.SpecialForm, args, 1);
			
			final VncSymbol sym = Types.isVncSymbol(args.first())
									? (VncSymbol)args.first()
									: Coerce.toVncSymbol(evaluator.evaluate(args.first(), env, false));
			
			if (sym.hasNamespace()) {
				return new VncString(sym.getNamespace());
			}
			else if (env.isLocal(sym)) {
				return Nil;
			}
			else {
				final Var v = env.getGlobalVarOrNull(sym);
				return v == null
						? Nil
						: new VncString(
									v.getName().hasNamespace()
										? v.getName().getNamespace()
										: Namespaces.NS_CORE.getName());
			}
		}
	}

	public VncVal var_name_(
			final VncList args, 
			final Env env,
			final VncVal meta
	) {
		final CallFrame callframe = new CallFrame("var-name", args, meta);
		try (WithCallStack cs = new WithCallStack(callframe)) {
			specialFormCallValidation("var-name");
			assertArity("var-name", FnType.SpecialForm, args, 1);
			final VncSymbol sym = Types.isVncSymbol(args.first())
									? (VncSymbol)args.first()
									: Coerce.toVncSymbol(evaluator.evaluate(args.first(), env, false));
			return new VncString(sym.getSimpleName());
		}
	}

	public VncVal var_localQ_(
			final VncList args, 
			final Env env,
			final VncVal meta
	) {
		final CallFrame callframe = new CallFrame("var-local?", args, meta);
		try (WithCallStack cs = new WithCallStack(callframe)) {
			assertArity("var-local?", FnType.SpecialForm, args, 1);
			final VncSymbol sym = Types.isVncSymbol(args.first())
									? (VncSymbol)args.first()
									: Coerce.toVncSymbol(evaluator.evaluate(args.first(), env, false));
			return VncBoolean.of(env.isLocal(sym));
		}
	}

	public VncVal var_thread_localQ_(
			final VncList args, 
			final Env env,
			final VncVal meta
	) {
		final CallFrame callframe = new CallFrame("var-thread-local?", args, meta);
		try (WithCallStack cs = new WithCallStack(callframe)) {
			assertArity("var-thread-local?", FnType.SpecialForm, args, 1);
			final VncSymbol sym = Types.isVncSymbol(args.first())
									? (VncSymbol)args.first()
									: Coerce.toVncSymbol(evaluator.evaluate(args.first(), env, false));
			return VncBoolean.of(env.isDynamic(sym));
		}
	}

	public VncVal var_globalQ_(
			final VncList args, 
			final Env env,
			final VncVal meta
	) {
		final CallFrame callframe = new CallFrame("var-global?", args, meta);
		try (WithCallStack cs = new WithCallStack(callframe)) {
			assertArity("var-global?", FnType.SpecialForm, args, 1);
			final VncSymbol sym = Types.isVncSymbol(args.first())
									? (VncSymbol)args.first()
									: Coerce.toVncSymbol(evaluator.evaluate(args.first(), env, false));
			return VncBoolean.of(env.isGlobal(sym));
		}
	}
	
	public VncVal defprotocol_(
			final IVeniceInterpreter interpreter, 
			final VncSymbol name,
			final VncList args, 
			final Env env,
			final VncVal meta
	) {
		final CallFrame callframe = new CallFrame("defprotocol", args, meta);
		try (WithCallStack cs = new WithCallStack(callframe)) {
			// (defprotocol P
			//	  (foo [x])
			//	  (bar [x] [x y])
			//	  (zoo [x] "default val")
			//	  (yar [x] [x y] "default val"))
			
			validateDefProtocol(args);
						
			VncMap protocolFns = VncHashMap.empty();
			for(VncVal s : args.rest()) {
				final VncMultiArityFunction fn = parseProtocolFnSpec(s, env);
				
				final VncSymbol fnName = new VncSymbol(
												name.getNamespace(), 
												fn.getSimpleName(),
												meta);
				
				final VncProtocolFunction fnProtocol = new VncProtocolFunction(
																fnName.getQualifiedName(), 
																name,
																fn, 
																fn.getMeta());

				final VncVal p = env.getGlobalOrNull(fnName);
				if (p instanceof VncProtocolFunction) {
					if (!((VncProtocolFunction)p).getProtocolName().equals(name)) {
						// collision of protocol function name with another protocol
						throw new VncException(String.format(
									"The protocol function '%s' of protocol '%s' collides "
											+ "with the same function in protocol '%s' in "
											+ "the same namespace!",
									fn.getSimpleName(),
									((VncProtocolFunction)p).getProtocolName(),
									name));
					}
				}
				
				env.setGlobal(new Var(fnName, fnProtocol));

				protocolFns = protocolFns.assoc(new VncString(fn.getSimpleName()), fn);
			}
	
			final VncProtocol protocol = new VncProtocol(name, protocolFns, name.getMeta());
			
			env.setGlobal(new Var(name, protocol));

			return protocol;
		}
	}
	
	public VncVal extend_(
			final VncVal typeRef,
			final VncSymbol protocolSym,
			final VncList args, 
			final Env env
	) {
		if (!(typeRef instanceof VncKeyword)) {
			throw new VncException(String.format(
					"The type '%s' must be a keyword like :core/long!",
					typeRef.getType()));
		}

		// Lookup protocol from the ENV
		final VncVal p = env.getGlobalOrNull(protocolSym);
		if (!(p instanceof VncProtocol)) {
			throw new VncException(String.format(
					"The protocol '%s' is not defined!",
					protocolSym.getQualifiedName()));
		}
		
		final VncKeyword type = (VncKeyword)typeRef;
		
		if (!type.hasNamespace()) {
			throw new VncException(String.format(
					"The type '%s' must be qualified!",
					type.getQualifiedName()));
		}
		
		final VncProtocol protocol = (VncProtocol)p;		
		final VncList fnSpecList = args.slice(2);


		for(VncVal fnSpec : fnSpecList.getJavaList()) {
			if (!Types.isVncList(fnSpec)) {
				throw new VncException(String.format(
						"Invalid extend for protocol '%s' with type '%s' . "
						+ "Expected a function spec like '(foo [x] nil)'!",
						protocolSym.getQualifiedName(),
						typeRef.getType()));
			}
			
			// (foo [x] nil)
			extendFnSpec(type, (VncList)fnSpec, protocol, env);
		}
		
		protocol.register(type);
		
		return Nil;
	}
	
	public VncVal extendsQ_(
			final VncVal typeRef,
			final VncSymbol protocolSym,
			final Env env
	) {
		final VncVal typeRefEval = evaluator.evaluate(typeRef, env, false);
		
		if (!(typeRefEval instanceof VncKeyword)) {
			throw new VncException(String.format(
					"The type '%s' must be a keyword like :core/long!",
					typeRefEval.getType()));
		}

		// Lookup protocol from the ENV
		final VncVal p = env.getGlobalOrNull(protocolSym);
		if (!(p instanceof VncProtocol)) {
			throw new VncException(String.format(
					"The protocol '%s' is not defined!",
					protocolSym.getQualifiedName()));
		}
		
		final VncKeyword type = (VncKeyword)typeRefEval;
		
		if (!type.hasNamespace()) {
			throw new VncException(String.format(
					"The type '%s' must be qualified!",
					type.getQualifiedName()));
		}
		
		final VncProtocol protocol = (VncProtocol)p;
				
		return VncBoolean.of(protocol.isRegistered(type));
	}

	public VncVal deftype_(
			final VncList args, 
			final Env env,
			final VncVal meta
	) {
		// (deftype :complex [real :long, imaginary :long])
		//
		// (deftype :complex [real :long, imaginary :long]
		//          P (foo [x])
		final CallFrame callframe = new CallFrame("deftype", args, meta);
		try (WithCallStack cs = new WithCallStack(callframe)) {
			assertMinArity("deftype", FnType.SpecialForm, args, 2);
			
			final VncList deftypeArgs = args.takeWhile(e -> !Types.isVncSymbol(e));
			VncList extendArgs = args.drop(deftypeArgs.size());
			
			// [1] parse deftype args
			final VncKeyword type = Coerce.toVncKeyword(
										evaluator.evaluate(deftypeArgs.first(), env, false));

			final VncVector fields = Coerce.toVncVector(deftypeArgs.second());

			final VncFunction validationFn = deftypeArgs.size() == 3
												? Coerce.toVncFunction(
														evaluator.evaluate(
																args.third(), env, false))
												: null;
			
			// custom type is a namespace qualified keyword
			final VncVal customType = DefTypeForm.defineCustomType(
											type, fields, validationFn, interpreter, env);
	
			// [2] parse extend protocol definitions
			while(!extendArgs.isEmpty()) {
				final VncVal protocolSym = extendArgs.first();
				final VncList extDefs = extendArgs.drop(1).takeWhile(e -> Types.isVncList(e));
				if (Types.isVncSymbol(protocolSym)) {
					extendArgs = extendArgs.drop(extDefs.size()+1);
					
					// process the extend definition
					extend_(customType,
							(VncSymbol)protocolSym,
							extDefs.addAtStart(protocolSym).addAtStart(type), 
							env);						
				}
				else {
					throw new VncException(String.format(
							"Invalid extend protocol definitions for custom type '%s'",
							type.toString()));
				}
			}	
			
			return customType;
		}
	}

	public VncVal deftypeQ_(
			final VncList args, 
			final Env env,
			final VncVal meta
	) {
		final CallFrame callframe = new CallFrame("deftype?", args, meta);
		try (WithCallStack cs = new WithCallStack(callframe)) {
			assertArity("deftype?", FnType.SpecialForm, args, 1);
			final VncVal type = evaluator.evaluate(args.first(), env, false);
			return VncBoolean.of(DefTypeForm.isCustomType(type, env));
		}
	}

	public VncVal deftype_of_(
			final VncList args, 
			final Env env,
			final VncVal meta
	) {
		final CallFrame callframe = new CallFrame("deftype-of", args, meta);
		try (WithCallStack cs = new WithCallStack(callframe)) {
			assertMinArity("deftype-of", FnType.SpecialForm, args, 2);
			final VncKeyword type = Coerce.toVncKeyword(evaluator.evaluate(args.first(), env, false));
			final VncKeyword baseType = Coerce.toVncKeyword(evaluator.evaluate(args.second(), env, false));
			final VncFunction validationFn = args.size() == 3
												? Coerce.toVncFunction(evaluator.evaluate(args.third(), env, false))
												: null;
			return DefTypeForm.defineCustomWrapperType(
						type, 
						baseType, 
						validationFn, 
						interpreter, 
						env,
						wrappableTypes);
		}
	}

	public VncVal deftype_or_(
			final VncList args, 
			final Env env,
			final VncVal meta
	) {
		final CallFrame callframe = new CallFrame("deftype-or", args, meta);
		try (WithCallStack cs = new WithCallStack(callframe)) {
			assertMinArity("deftype-or", FnType.SpecialForm, args, 2);
			final VncKeyword type = Coerce.toVncKeyword(evaluator.evaluate(args.first(), env, false));
			final VncList choiceVals = args.rest();

			return DefTypeForm.defineCustomChoiceType(type, choiceVals, interpreter, env);
		}
	}

	public VncVal deftype_describe_(
			final VncList args, 
			final Env env,
			final VncVal meta
	) {
		final CallFrame callframe = new CallFrame(".:", args, meta);
		try (WithCallStack cs = new WithCallStack(callframe)) {
			assertArity("deftype-describe", FnType.SpecialForm, args, 1);
			final VncVal evaluatedArg = evaluator.evaluate(args.first(), env, false);
			return DefTypeForm.describeType(evaluatedArg, env);
		}
	}

	public VncVal deftype_create_(
			final VncList args, 
			final Env env,
			final VncVal meta
	) {
		final CallFrame callframe = new CallFrame(".:", args, meta);
		try (WithCallStack cs = new WithCallStack(callframe)) {
			assertMinArity(".:", FnType.SpecialForm, args, 1);
			final List<VncVal> evaluatedArgs = new ArrayList<>();
			for(VncVal v : args) {
				evaluatedArgs.add(evaluator.evaluate(v, env, false));
			}
			return DefTypeForm.createType(evaluatedArgs, env);
		}
	}

	public VncVal inspect_(
			final VncList args, 
			final Env env,
			final VncVal meta
	) {
		final CallFrame callframe = new CallFrame("inspect", args, meta);
		try (WithCallStack cs = new WithCallStack(callframe)) {
			specialFormCallValidation("inspect");
			assertArity("inspect", FnType.SpecialForm, args, 1);
			final VncSymbol sym = Coerce.toVncSymbol(evaluator.evaluate(args.first(), env, false));
			return Inspector.inspect(env.get(sym));
		}
	}
	
	public VncVal doc_(
			final VncList args, 
			final Env env,
			final VncVal meta
	) {
		final CallFrame callframe = new CallFrame("doc", args, meta);
		try (WithCallStack cs = new WithCallStack(callframe)) {
			assertArity("doc", FnType.SpecialForm, args, 1);
			final VncString doc = DocForm.doc(args.first(), env);
			evaluator.evaluate(VncList.of(new VncSymbol("println"), doc), env, false);
			return Nil;
		}
	}
	
	public VncVal print_highlight_(
			final VncList args, 
			final Env env,
			final VncVal meta
	) {
		final CallFrame callframe = new CallFrame("print-highlight", args, meta);
		try (WithCallStack cs = new WithCallStack(callframe)) {
			assertArity("print-highlight", FnType.SpecialForm, args, 1);
			final VncString form = DocForm.highlight(Coerce.toVncString(args.first()), env);
			evaluator.evaluate(VncList.of(new VncSymbol("println"), form), env, false);
			return Nil;
		}
	}
	
	public VncVal dobench_(
			final VncList args, 
			final Env env,
			final VncVal meta
	) {
		final CallFrame callframe = new CallFrame("dobench", args, meta);
		try (WithCallStack cs = new WithCallStack(callframe)) {
			specialFormCallValidation("dobench");
			assertArity("dobench", FnType.SpecialForm, args, 2);
			
			try {
				final long count = Coerce.toVncLong(args.first()).getValue();
				final VncVal expr = args.second();
				
				final List<VncVal> elapsed = new ArrayList<>();
				for(int ii=0; ii<count; ii++) {
					final long start = System.nanoTime();
					
					final VncVal result = evaluator.evaluate(expr, env, false);
					
					final long end = System.nanoTime();
					elapsed.add(new VncLong(end-start));
	
					InterruptChecker.checkInterrupted(Thread.currentThread(), "dobench");
	
					// Store value to a mutable place to prevent JIT from optimizing 
					// too much. Wrap the result so a VncStack can be used as result
					// too (VncStack is a special value in ThreadLocalMap)
					ThreadContext.setValue(
							new VncKeyword("*benchmark-val*"), 
							new VncJust(result));
				}
				
				return VncList.ofList(elapsed);
			}
			finally {
				ThreadContext.removeValue(new VncKeyword("*benchmark-val*"));
			}
		}
	}
	
	public VncVal dorun_(
			final VncList args, 
			final Env env,
			final VncVal meta
	) {
		final CallFrame callframe = new CallFrame("dorun", args, meta);
		
		try (WithCallStack cs = new WithCallStack(callframe)) {
			specialFormCallValidation("dorun");
			assertArity("dorun", FnType.SpecialForm, args, 2);
			
			
			final VncVal vCount = evaluator.evaluate(args.first(), env, false);				
			final long count = Coerce.toVncLong(vCount).getValue();
			if (count <= 0) return Nil;
			
			final VncVal expr = args.second();
	
			if (Types.isVncSymbol(expr)) {
				final VncVal v = env.getOrNil((VncSymbol)expr);
				
				if (Types.isVncFunction(v)) {
					// run the function
					final VncFunction fn = (VncFunction)v;
					
					if (fn.getFixedArgsCount() == 1) {
						// one arg function: pass the counter value
						for(int ii=0; ii<count-1; ii++) {
							fn.apply(VncList.of(new VncLong(ii)));
						}
						return fn.apply(VncList.of(new VncLong(count-1)));
					}
					else {
						// call as zero arg function
						final VncList fnArgs = VncList.empty();
						for(int ii=0; ii<count-1; ii++) {
							fn.apply(fnArgs);
						}
						return fn.apply(fnArgs);
					}
				}
			}

			try {
				final VncVal first = evaluator.evaluate(expr, env, false);
				
				for(int ii=1; ii<count; ii++) {
					final VncVal result = evaluator.evaluate(expr, env, false);
	
					InterruptChecker.checkInterrupted(Thread.currentThread(), "dorun");
	
					// Store value to a mutable place to prevent JIT from optimizing 
					// too much. Wrap the result so a VncStack can be used as result
					// too (VncStack is a special value in ThreadLocalMap)
					ThreadContext.setValue(
							new VncKeyword("*benchmark-val*"), 
							new VncJust(result));
				}
				
				return first;
			}
			finally {
				ThreadContext.removeValue(new VncKeyword("*benchmark-val*"));
			}
		}
	}
	
	public VncVal prof_( 
			final VncList args, 
			final Env env,
			final VncVal meta
	) {
		// Note on profiling recursive functions: 
		// For recursive functions the profiler reports the 'time with children
		// for the particular recursive function resulting in much higher measured 
		// elapsed times.
		// Profiling TCO based recursive functions report correct times.
		//
		// See:  - https://smartbear.com/learn/code-profiling/fundamentals-of-performance-profiling/
		//       - https://support.smartbear.com/aqtime/docs/profiling-with/profile-various-apps/recursive-routines.html
		final CallFrame callframe = new CallFrame("prof", args, meta); 
		try (WithCallStack cs = new WithCallStack(callframe)) {
			specialFormCallValidation("prof");
			assertArity("prof", FnType.SpecialForm, args, 1, 2, 3);

			if (Types.isVncKeyword(args.first())) {
				final VncKeyword cmd = (VncKeyword)args.first();
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
						meterRegistry.resetAllBut(Coerce.toVncSequence(args.second())); 
						return new VncKeyword(meterRegistry.isEnabled() ? "on" : "off");
						
					case "data":
						return meterRegistry.getVncTimerData();
						
					case "data-formatted":
						final VncVal opt1 = args.second();
						final VncVal opt2 = args.third();
						
						String title = "Metrics";
						if (Types.isVncString(opt1) && !Types.isVncKeyword(opt1)) {
							title = ((VncString)opt1).getValue();
						}
						if (Types.isVncString(opt2) && !Types.isVncKeyword(opt2)) {
							title = ((VncString)opt2).getValue();
						}
	
						boolean anonFn = false;
						if (Types.isVncKeyword(opt1)) {
							anonFn = anonFn || ((VncKeyword)opt1).hasValue("anon-fn");
						}
						if (Types.isVncKeyword(opt2)) {
							anonFn = anonFn || ((VncKeyword)opt2).hasValue("anon-fn");
						}
	
						return new VncString(meterRegistry.getTimerDataFormatted(title, anonFn));
				}
			}
	
			throw new VncException(
					"Function 'prof' expects a single keyword argument: " +
					":on, :off, :status, :clear, :clear-all-but, :data, " +
					"or :data-formatted");
		}
	}

	public VncVal try_(
			final VncList args, 
			final Env env, 
			final VncVal meta
	) {
		final CallFrame callframe = new CallFrame("try", args, meta); 
		try (WithCallStack cs = new WithCallStack(callframe)) {
			return handleTryCatchFinally(
					"try",
					args,
					env,
					meta,
					new ArrayList<Var>());
		}
	}

	public VncVal try_with_(
			final VncList args, 
			final Env env, 
			final VncVal meta
	) {
		final CallFrame callframe = new CallFrame("try-with", args, meta); 
		try (WithCallStack cs = new WithCallStack(callframe)) {
			final Env localEnv = new Env(env);
			final VncSequence bindings = Coerce.toVncSequence(args.first());
			final List<Var> boundResources = new ArrayList<>();
			
			for(int i=0; i<bindings.size(); i+=2) {
				final VncVal sym = bindings.nth(i);
				final VncVal val = evaluator.evaluate(bindings.nth(i+1), localEnv, false);
	
				if (Types.isVncSymbol(sym)) {
					final Var binding = new Var((VncSymbol)sym, val);
					localEnv.setLocal(binding);
					boundResources.add(binding);
				}
				else {
					throw new VncException(
							String.format(
									"Invalid 'try-with' destructuring symbol "
									+ "value type %s. Expected symbol.",
									Types.getType(sym)));
				}
			}
	
			try {
				return handleTryCatchFinally(
							"try-with",
							args.rest(),
							localEnv,
							meta,
							boundResources);
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
					}
				});
			}
		}
	}
	
	public VncVal tail_pos_check(
			final boolean inTailPosition, 
			final VncList args, 
			final Env env,
			final VncVal meta
	) {
		if (!inTailPosition) {
			final CallFrame callframe = new CallFrame("tail-pos", args, meta);
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

	private VncVal handleTryCatchFinally(
			final String specialForm,
			final VncList args,
			final Env env, 
			final VncVal meta,
			final List<Var> bindings
	) {
		final ThreadContext threadCtx = ThreadContext.get();
		final DebugAgent debugAgent = threadCtx.getDebugAgent_();

		if (debugAgent != null && debugAgent.hasBreakpointFor(new BreakpointFnRef(specialForm))) {
			final CallStack callStack = threadCtx.getCallStack_();
			debugAgent.onBreakSpecialForm(
					specialForm, FunctionEntry, bindings, meta, env, callStack);
		}
		
		try {
			final Env bodyEnv = new Env(env);
			return evaluateBody(getTryBody(args), bodyEnv, true);
		} 
		catch (Exception ex) {
			final RuntimeException wrappedEx = ex instanceof RuntimeException 
													? (RuntimeException)ex 
													: new RuntimeException(ex);
			
			final CatchBlock catchBlock = findCatchBlockMatchingThrowable(env, args, ex);
			if (catchBlock == null) {
				throw wrappedEx;
			}
			else {
				final Env catchEnv = new Env(env);
				catchEnv.setLocal(new Var(catchBlock.getExSym(), new VncJavaObject(wrappedEx)));			
				catchBlockDebug(threadCtx, debugAgent, catchBlock.getMeta(), catchEnv, catchBlock.getExSym(), wrappedEx);
				return evaluateBody(catchBlock.getBody(), catchEnv, false);
			}
		}
		finally {
			final FinallyBlock finallyBlock = findFirstFinallyBlock(args);
			if (finallyBlock != null) {
				final Env finallyEnv = new Env(env);
				finallyBlockDebug(threadCtx, debugAgent, finallyBlock.getMeta(), finallyEnv);
				evaluateBody(finallyBlock.getBody(), finallyEnv, false);
			}
		}
	}
	
	private VncList getTryBody(final VncList args) {
		final List<VncVal> body = new ArrayList<>();
 		for(VncVal e : args) {
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
			final Env env,
			final VncList blocks, 
			final Throwable th
	) {
		// (catch ex-class ex-sym expr*)
		
		for(VncVal b : blocks) {
			if (Types.isVncList(b)) {
				final VncList block = ((VncList)b);
				final VncVal catchSym = block.first();
				if (Types.isVncSymbol(catchSym) && ((VncSymbol)catchSym).getName().equals("catch")) {
					if (isCatchBlockMatchingThrowable(env, block, th)) {
						return new CatchBlock(
									Coerce.toVncSymbol(block.third()), 
									block.slice(3),
									catchSym.getMeta());
					}
				}
			}
		}
		
		return null;
	}
	
	private boolean isCatchBlockMatchingThrowable(
			final Env env,
			final VncList block, 
			final Throwable th
	) {
		final VncVal selector = evaluator.evaluate(block.second(), env, false);

		// Selector: exception class => (catch :RuntimeExceptiom e (..))
		if (Types.isVncString(selector)) {
			final String className = resolveClassName(((VncString)selector).getValue());
			final Class<?> targetClass = ReflectionAccessor.classForName(className);
			
			return targetClass.isAssignableFrom(th.getClass());
		}

		// Selector: predicate => (catch predicate-fn e (..))
		else if (Types.isVncFunction(selector)) {
			final VncFunction predicate = (VncFunction)selector;
			
			if (th instanceof ValueException) {
				final VncVal exVal = getValueExceptionValue((ValueException)th);				
				final VncVal result = predicate.apply(VncList.of(exVal));
				return VncBoolean.isTrue(result);
			}
			else {
				final VncVal result = predicate.apply(VncList.of(Nil));
				return VncBoolean.isTrue(result);
			}
		}
		
		// Selector: list => (catch [key1 value1, ...] e (..))
		else if (Types.isVncSequence(selector)) {
			VncSequence seq = (VncSequence)selector;
			
			// (catch [:cause :IOException, ...] e (..))
			if (seq.first().equals(CAUSE_TYPE_SELECTOR_KEY) && Types.isVncKeyword(seq.second())) {
				final Throwable cause = th.getCause();
				if (cause != null) {
					final VncKeyword classRef = (VncKeyword)seq.second();
					final String className = resolveClassName(classRef.getSimpleName());
					final Class<?> targetClass = ReflectionAccessor.classForName(className);
					
					if (!targetClass.isAssignableFrom(cause.getClass())) {
						return false;
					}
					
					if (seq.size() == 2) {
						return true; // no more key/val pairs
					}
				}
				seq = seq.drop(2);
			}
			
			// (catch [key1 value1, ...] e (..))
			if (th instanceof ValueException) {				
				final VncVal exVal = getValueExceptionValue((ValueException)th);
				if (Types.isVncMap(exVal)) {
					final VncMap exValMap = (VncMap)exVal;
					
					while (!seq.isEmpty()) {
						final VncVal key = seq.first();
						final VncVal val = seq.second();
						
						if (!Types._equal_strict_Q(val, exValMap.get(key))) {
							return false;
						}
						
						seq = seq.drop(2);
					}
					
					return true;
				}
			}
			
			return false;
		}

		else {
			return false;
		}
	}
	
	private FinallyBlock findFirstFinallyBlock(final VncList blocks) {
		for(VncVal b : blocks) {
			if (Types.isVncList(b)) {
				final VncList block = ((VncList)b);
				final VncVal first = block.first();
				if (Types.isVncSymbol(first) && ((VncSymbol)first).getName().equals("finally")) {
					return new FinallyBlock(block.rest(), first.getMeta());
				}
			}
		}
		return null;
	}
	
	private void catchBlockDebug(
			final ThreadContext threadCtx,
			final DebugAgent debugAgent,
			final VncVal meta,
			final Env env,
			final VncSymbol exSymbol,
			final RuntimeException ex
	) {
		if (debugAgent != null && debugAgent.hasBreakpointFor(new BreakpointFnRef("catch"))) {
			debugAgent.onBreakSpecialForm(
					"catch", 
					FunctionEntry, 
					VncVector.of(exSymbol), 
					VncList.of(new VncJavaObject(ex)), 
					meta, 
					env, 
					threadCtx.getCallStack_());
		}
	}
	
	private void finallyBlockDebug(
			final ThreadContext threadCtx,
			final DebugAgent debugAgent,
			final VncVal meta,
			final Env env
	) {
		if (debugAgent != null && debugAgent.hasBreakpointFor(new BreakpointFnRef("finally"))) {
			debugAgent.onBreakSpecialForm(
					"finally", 
					FunctionEntry, 
					new ArrayList<Var>(), 
					meta, 
					env, 
					threadCtx.getCallStack_());
		}
	}

	private VncVal getValueExceptionValue(final ValueException ex) {
		final Object val = ex.getValue();
		
		return val == null 
				? Nil
				: val instanceof VncVal 
					? (VncVal)val 
					: new VncJavaObject(val);
	}
	
	private VncVal evaluateBody(
			final VncList body, 
			final Env env, final 
			boolean withTailPosition
	) {
		valuesEvaluator.evaluate_values(body.butlast(), env);
		return evaluator.evaluate(body.last(), env, withTailPosition);
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

	private void specialFormCallValidation(final String name) {
		ThreadContext.getInterceptor().validateVeniceFunction(name);
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

	private void validateDefProtocol(final VncList args) {
		// (defprotocol P
		//	  (foo [x])
		//	  (bar [x] [x y])
		//	  (goo [x] "default val")
		//	  (dar [x] [x y] "default val"))

		if (!Types.isVncSymbol(args.first())) {
			throw new VncException(
					"A protocol definition must have a symbol as its name!\n" +
					"E.g.: as 'P' in (defprotocol P (foo [x]))");
		}
		
		for(VncVal spec : args.rest()) {
			if (!Types.isVncList(spec)) {
				throw new VncException(
						"A protocol definition must have a list with function " +
						"specifications!\n" +
						"E.g.: as '(foo [x])' (defprotocol P (foo [x]) (bar [x]))");
			}

			final VncList specList = (VncList)spec;

			final VncSymbol fnName = (VncSymbol)specList.first();
			final VncList specs = specList.rest();
			final VncList paramSpecs = specs.takeWhile(s -> Types.isVncVector(s));

			if (!Types.isVncSymbol(fnName)) {
				throw new VncException(
						"A protocol function specification must have a symbol as " +
						"its name!\n" +
						"E.g.: as 'foo' in (defprotocol P (foo [x]))");
			}

			if (paramSpecs.isEmpty()) {
				throw new VncException(String.format(
						"The protocol function specification '%s' must have at least one " +
						"parameter specification!\n" +
						"E.g.: as '[x]' in (defprotocol P (foo [x]))",
						fnName));
			}
			
			final Set<Integer> aritySet = new HashSet<>();			
			for(VncVal ps : paramSpecs) {
				if (!Types.isVncVector(ps)) {
					throw new VncException(String.format(
							"The protocol function specification '%s' must have one or multiple " +
							"vectors of param symbols followed by an optional return value of any " +
							"type but vector!\n" +
							"E.g.: (defprotocol P (foo [x] [x y] nil))",
							fnName));
				}
				
				// validate for non duplicate arities
				final int arity = ((VncVector)ps).size();
				if (aritySet.contains(arity)) {
					throw new VncException(String.format(
							"The protocol function specification '%s' has multiple parameter " +
							"definitions for the arity %d!\n" +
							"E.g.: as '[x y]' in (defprotocol P (foo [x] [x y] [x y]))",
							fnName,
							arity));
				}
				aritySet.add(arity);

				for(VncVal p : (VncVector)ps) {
					if (!Types.isVncSymbol(p)) {
						throw new VncException(String.format(
								"The protocol function specification '%s' must have vector of param " +
								"symbols!\n" +
								"E.g.: as '[x y]' in (defprotocol P (foo [x y]))",
								fnName));
					}
				}
			}
		}
	}

	private VncMultiArityFunction parseProtocolFnSpec(
			final VncVal spec,
			final Env env
	) {
		// spec:  (bar [x] [x y] nil)
		
		final VncList specList = (VncList)spec;
		
		final VncSymbol fnName = (VncSymbol)specList.first();
		final VncList specs = specList.rest();
		final VncList paramSpecs = specs.takeWhile(s -> Types.isVncVector(s));
		
		final VncList body = specs.slice(paramSpecs.size());

		// the namespace the function is defined for
		final Namespace ns = Namespaces.getCurrentNamespace();

		// the arg list for the function's meta data
		final List<VncString> argList =
			paramSpecs
				.stream()
				.map(spc -> String.format("(%s %s)", fnName.getName(), spc.toString()))
				.map(s -> new VncString(s))
				.collect(Collectors.toList());
		
		final List<VncFunction> functions =
			paramSpecs
				.getJavaList()
				.stream()
				.map(p -> new VncFunction(
								fnName.getQualifiedName(), 
								(VncVector)p,
								fnName.getMeta()
						  ) {
							public VncVal apply(final VncList args) {
								final ThreadContext threadCtx = ThreadContext.get();

								final Env localEnv = new Env(env);
								localEnv.addLocalVars(Destructuring.destructure(p, args));

								final Namespace curr_ns = threadCtx.getCurrNS_();

								try {
									threadCtx.setCurrNS_(ns);
									valuesEvaluator.evaluate_values(body.butlast(), localEnv);
									return evaluator.evaluate(body.last(), localEnv, false);
								}
								finally {
									// switch always back to current namespace, just in case
									// the namespace was changed within the function body!
									threadCtx.setCurrNS_(curr_ns);
								}
							}
							
							private static final long serialVersionUID = -1L;
						  })
				.collect(Collectors.toList());

		return new VncMultiArityFunction(
						fnName.getQualifiedName(),
						functions,
						false,
						MetaUtil.mergeMeta(
								VncHashMap.of(MetaUtil.ARGLIST, VncList.ofColl(argList)),
								fnName.getMeta()));
	}

	private void extendFnSpec(
			final VncKeyword type,
			final VncList fnSpec,
			final VncProtocol protocol,
			final Env env
	) {
		// (foo [x] nil)                 -> (defn foo [x] nil)
		// (foo ([x] nil) ([x y] nil))   -> (defn foo ([x] nil) ([x y] nil))
		
		final String name = ((VncSymbol)fnSpec.first()).getName();
		final VncSymbol fnProtoSym = new VncSymbol(
										protocol.getName().getNamespace(), 
										name, 
										fnSpec.first().getMeta());
		
		// the created extended function must be in the current namespace
		final VncSymbol fnSym = new VncSymbol(
										GenSym.generateAutoSym(name).getName(), 
										fnSpec.first().getMeta());
		
		// Lookup protocol function from the ENV
		final VncVal protocolFn = env.getGlobalOrNull(fnProtoSym);
		if (!(protocolFn instanceof VncProtocolFunction)) {
			throw new VncException(String.format(
						"The protocol function '%s' does not exist!",
						fnProtoSym.getQualifiedName()));
		}
		
		// Create the protocol function by evaluating (defn ...)
		final VncList fnDef = VncList
								.of(new VncSymbol("defn"), fnSym)
								.addAllAtEnd(fnSpec.rest());
		evaluator.evaluate(fnDef, env, false);
		final VncFunction fn = (VncFunction)env.getGlobalOrNull(fnSym);
		env.removeGlobalSymbol(fnSym);
		
		// Register the function for the type on the protocol
		((VncProtocolFunction)protocolFn).register(type, fn);
	}

	private static boolean isNonEmptySequence(final VncVal x) {
		return Types.isVncSequence(x) && !((VncSequence)x).isEmpty();
	}

	
	
	private static final VncKeyword CAUSE_TYPE_SELECTOR_KEY = new VncKeyword(":cause-type");

	private final CustomWrappableTypes wrappableTypes = new CustomWrappableTypes();
	private final NamespaceRegistry nsRegistry;
	private final MeterRegistry meterRegistry;
	private final AtomicBoolean sealedSystemNS;

	private final IVeniceInterpreter interpreter;
	private final IFormEvaluator evaluator;
	private final IValuesEvaluator valuesEvaluator;
}
