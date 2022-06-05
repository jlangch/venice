/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2022 Venice
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
package com.github.jlangch.venice.impl.specialforms;

import static com.github.jlangch.venice.impl.debug.breakpoint.FunctionScope.FunctionEntry;
import static com.github.jlangch.venice.impl.types.Constants.Nil;
import static com.github.jlangch.venice.impl.util.ArityExceptions.assertArity;
import static com.github.jlangch.venice.impl.util.ArityExceptions.assertMinArity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.jlangch.venice.ValueException;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Destructuring;
import com.github.jlangch.venice.impl.FunctionBuilder;
import com.github.jlangch.venice.impl.IFormEvaluator;
import com.github.jlangch.venice.impl.InterruptChecker;
import com.github.jlangch.venice.impl.Modules;
import com.github.jlangch.venice.impl.Namespace;
import com.github.jlangch.venice.impl.Namespaces;
import com.github.jlangch.venice.impl.debug.agent.DebugAgent;
import com.github.jlangch.venice.impl.debug.breakpoint.BreakpointFnRef;
import com.github.jlangch.venice.impl.docgen.runtime.DocForm;
import com.github.jlangch.venice.impl.env.DynamicVar;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.env.GenSym;
import com.github.jlangch.venice.impl.env.ReservedSymbols;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.functions.CoreFunctions;
import com.github.jlangch.venice.impl.javainterop.JavaImports;
import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.types.IVncFunction;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncJust;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncMultiArityFunction;
import com.github.jlangch.venice.impl.types.VncMultiFunction;
import com.github.jlangch.venice.impl.types.VncProtocolFunction;
import com.github.jlangch.venice.impl.types.VncSpecialForm;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.custom.VncCustomBaseTypeDef;
import com.github.jlangch.venice.impl.types.custom.VncProtocol;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions.FnType;
import com.github.jlangch.venice.impl.util.reflect.ReflectionAccessor;
import com.github.jlangch.venice.impl.util.CallFrame;
import com.github.jlangch.venice.impl.util.CallStack;
import com.github.jlangch.venice.impl.util.Inspector;
import com.github.jlangch.venice.impl.util.MetaUtil;
import com.github.jlangch.venice.impl.util.MeterRegistry;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;
import com.github.jlangch.venice.impl.util.WithCallStack;


/**
 * The special form pesudo functions
 */
public class SpecialFormsFunctions {

	
	///////////////////////////////////////////////////////////////////////////
	// def functions
	///////////////////////////////////////////////////////////////////////////

	public static VncSpecialForm def =
		new VncSpecialForm(
				"def",
				VncSpecialForm
					.meta()
					.arglists("(def name expr)")
					.doc("Creates a global variable.")
					.examples(
						 "(def x 5)",
						 "(def sum (fn [x y] (+ x y)))",
						 "(def ^{:private true} x 100)")
					.seeAlso("def", "def-", "defonce", "def-dynamic", "set!")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				assertArity("def", FnType.SpecialForm, args, 1, 2);
				final VncSymbol name = validateSymbolWithCurrNS(
											Namespaces.qualifySymbolWithCurrNS(
													evaluateSymbolMetaData(args.first(), env, ctx)),
											"def");
				
				final VncVal val = args.second();
				
				VncVal res = ctx.getEvaluator().evaluate(val, env, false);
				
				// we want source location from name and this to work:
				//      (def y (vary-meta 1 assoc :a 100))
				//      (get (meta y) :a)  ; -> 100

				res = res.withMeta(MetaUtil.mergeMeta(res.getMeta(), name.getMeta()));
				
				env.setGlobal(new Var(name, res, true));
				return name;
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncSpecialForm defonce =
		new VncSpecialForm(
				"defonce",
				VncSpecialForm
					.meta()
					.arglists("(defonce name expr)")
					.doc("Creates a global variable that can not be overwritten")
					.examples(
						"(defonce x 5)",
						"(defonce ^{:private true} x 5)")
					.seeAlso("def", "def-dynamic")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				assertArity("defonce", FnType.SpecialForm, args, 1, 2);
				final VncSymbol name = validateSymbolWithCurrNS(
											Namespaces.qualifySymbolWithCurrNS(
													evaluateSymbolMetaData(args.first(), env, ctx)),
											"defonce");
								
				final VncVal val = args.second();

				final VncVal res = ctx.getEvaluator().evaluate(val, env, false).withMeta(name.getMeta());
				env.setGlobal(new Var(name, res, false));
				return name;
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncSpecialForm def_dynamic =
		new VncSpecialForm(
				"def-dynamic",
				VncSpecialForm
					.meta()
					.arglists("(def-dynamic name expr)")
					.doc(
						"Creates a dynamic variable that starts off as a global variable " +
						"and can be bound with 'binding' to a new value on the local thread.")
					.examples(
						"(do                      \n" +
						"   (def-dynamic x 100)   \n" +
						"   (println x)           \n" +
						"   (binding [x 200]      \n" +
						"      (println x))       \n" +
						"   (println x)))           ",
						"(def-dynamic ^{:private true} x 100)")
					.seeAlso("binding", "def", "defonce", "set!")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				assertArity("def-dynamic", FnType.SpecialForm, args, 1, 2);
				final VncSymbol name = validateSymbolWithCurrNS(
											Namespaces.qualifySymbolWithCurrNS(
													evaluateSymbolMetaData(args.first(), env, ctx)),
											"def-dynamic");
				
				final VncVal val = args.second();
				
				final VncVal res = ctx.getEvaluator().evaluate(val, env, false).withMeta(name.getMeta());
				env.setGlobalDynamic(name, res);
				return name;
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

		
		
	///////////////////////////////////////////////////////////////////////////
	// doc functions
	///////////////////////////////////////////////////////////////////////////

	public static VncSpecialForm doc =
		new VncSpecialForm(
				"doc",
				VncSpecialForm
					.meta()
					.arglists("(doc x)")
					.doc(
						"Prints documentation for a var or special form given `x` as its name. " +
						"Prints the definition of custom types. \n\n" +
						"Displays the source of a module if `x` is a module: `(doc :ansi)`\n\n" +
						"If the var could not be found, searches for a similiar var with " +
						"the **Levenshtein distance** 1.¶" +
						"E.g: \n\n" +
						"```                     \n" +
						"> (doc dac)             \n" +
						"Symbol 'dac' not found! \n" +
						"                        \n" +
						"Did you mean?           \n" +
						"   dag/dag              \n" +
						"   dec                  \n" +
						"```")
					.examples(
						"(doc +)",
						"(doc def)",
						"(do \n" +
						"   (deftype :complex [real :long, imaginary :long]) \n" +
						"   (doc :complex))")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				assertArity("doc", FnType.SpecialForm, args, 1);
				final VncString doc = DocForm.doc(args.first(), env);
				ctx.getEvaluator().evaluate(
						VncList.of(new VncSymbol("println"), doc), 
						env, 
						false);
				return Nil;
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};



	///////////////////////////////////////////////////////////////////////////
	// import functions
	///////////////////////////////////////////////////////////////////////////

	public static VncSpecialForm import_ =
		new VncSpecialForm(
				"import",
				VncSpecialForm
					.meta()
					.arglists(
						"(import class & classes)",
						"(import class :as alias)")
					.doc(
						"Imports one or multiple Java classes. Imports are bound to the current " +
						"namespace.\n\n" +
						"Aliases are helpful if Java classes have the same name but different " +
						"packages like `java.util.Date` and `java.sql.Date`:\n\n" +
						"```                                                  \n" +
						"(do                                                  \n" +
						"  (import :java.util.Date)                           \n" +
						"  (import :java.sql.Date :as :sql.Date)              \n" +
						"                                                     \n" +
						"  (println (. :Date :new))                           \n" +
						"  (println (. :sql.Date :valueOf \"2022-06-24\")))   \n" +
						"```")
					.examples(
						"(do                        \n" +
						"  (import :java.lang.Math) \n" +
						"  (. :Math :max 2 10))      ",
						"(do                        \n" +
						"  (import :java.awt.Point  \n" +
						"          :java.lang.Math) \n" +
						"  (. :Math :max 2 10))      ",
						"(do                        \n" +
						"  (import :java.awt.Color :as :AwtColor)    \n" +
						"  (. :AwtColor :new 200I 230I 255I 180I))   ",
						"(do                                                                            \n" +
						"  (ns util)                                                                    \n" +
						"  (defn import? [clazz ns_]                                                    \n" +
						"    (any? #(== % clazz) (map first (imports ns_))))                            \n" +
						"                                                                               \n" +
						"  (ns alpha)                                                                   \n" +
						"  (import :java.lang.Math)                                                     \n" +
						"  (println \"alpha:\" (util/import? :java.lang.Math 'alpha))                   \n" +
						"                                                                               \n" +
						"  (ns beta)                                                                    \n" +
						"  (println \"beta:\" (util/import? :java.lang.Math 'beta))                     \n" +
						"                                                                               \n" +
						"  (ns alpha)                                                                   \n" +
						"  (println \"alpha:\" (util/import? :java.lang.Math 'alpha))                   \n" +
						")")
					.seeAlso("imports")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				// (import :java.lang.Math)
				// (import :java.lang.Math :java.awt.Color)
				// (import :java.lang.Math :as :Math)
				// (import :java.lang.Math :as :Math :java.awt.Color :as :Color)
				assertMinArity("import", FnType.SpecialForm, args, 0);
				
				final JavaImports jImports = Namespaces
												.getCurrentNamespace()
												.getJavaImports();
				
				VncList args_ = args;
				while(!args_.isEmpty()) {
					final VncVal def = args_.first();
					final VncVal as = args_.second();
					
					if (Types.isVncKeyword(as) && "as".equals(((VncKeyword)as).getValue())) {
						final VncVal alias = args_.third();
						if (alias != Nil) {
							jImports.add(Coerce.toVncString(def).getValue(),
									 	 Coerce.toVncString(alias).getValue());
							
							args_ = args_.drop(3);
						}
						else {
							throw new VncException("Invalid Java import definition!");
						}
					}
					else {
						jImports.add(Coerce.toVncString(def).getValue());
						args_ = args_.drop(1);
					}			
				}
				
				return Nil;
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncSpecialForm imports_ =
		new VncSpecialForm(
				"imports",
				VncSpecialForm
					.meta()
					.arglists(
						"(imports & options)",
						"(imports ns & options)")
					.doc(
						"Without namespace arg returns a list with the registered imports " +
						"for the current namespace. With namespace arg returns a list with " +
						"the registered imports for the given namespace. \n\n" +
						"Options:  \n\n" +
						"| :print | print the import list to the current value of `*out*` | ")
					.examples(
						"(do                           \n" +
						"  (import :java.lang.Math)    \n" +
						"  (imports))                  ",
						"(do                           \n" +
						"  (import :java.lang.Math)    \n" +
						"  (imports :print))           ",
						"(do                           \n" +
						"  (ns foo)                    \n" +
						"  (import :java.lang.Math)    \n" +
						"  (ns bar)                    \n" +
						"  (imports 'foo))              ")
					.seeAlso("import")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				// (imports)
				// (imports user)
				// (imports :print)
				// (imports user :print)
				assertArity("imports", FnType.SpecialForm, args, 0, 1, 2);

				final boolean print = Types.isVncKeyword(args.last()) 
										&& "print".equals(((VncKeyword)args.last()).getValue());
				
				
				final VncList args_ = print ? args.butlast() : args;
				
				Namespace namespace = Namespaces.getCurrentNamespace();
				 
				if (!args_.isEmpty()) {
					// we got a ns argument
					final VncSymbol ns = Coerce.toVncSymbol(
											ctx.getEvaluator().evaluate(args.first(), env, false));
					namespace = ctx.getNsRegistry().get(ns);
					if (namespace == null) {
						throw new VncException(String.format(
								"The namespace '%s' does not exist", 
								ns.toString()));
					}
				}
				
				final VncList importList = namespace.getJavaImportsAsVncList();
				
				if (print) {
					final VncFunction printFn = (VncFunction)env.get(new VncSymbol("println"));
					importList.forEach(i -> printFn.applyOf(
												((VncVector)i).first(), 
												new VncKeyword("as"),
												((VncVector)i).second()));
					return Nil;
				}
				else {
					return importList;
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};


	
	///////////////////////////////////////////////////////////////////////////
	// protocol functions
	///////////////////////////////////////////////////////////////////////////
	
	public static VncSpecialForm defprotocol =
			new VncSpecialForm(
					"defprotocol",
					VncSpecialForm
						.meta()
						.arglists("(defprotocol protocol fn-spec*)")
						.doc(
							"Defines a new protocol with the supplied function specs.  \n\n" +
							"Formats:                                                  \n\n" +
							" * `(defprotocol P (foo [x]))`                            \n" +
							" * `(defprotocol P (foo [x] [x y]))`                      \n" +
							" * `(defprotocol P (foo [x] [x y] nil))`                  \n" +
							" * `(defprotocol P (foo [x] [x y] 100))`                  \n" +
							" * `(defprotocol P (foo [x]) (bar [x] [x y]))`              ")
						.examples(
							"(do                                                       \n" +
							"   (ns foo)                                               \n" +
							"   (deftype :complex [re :long, im :long])                \n" +
							"   (defprotocol XMath (+ [x y])                           \n" +
							"                      (- [x y]))                          \n" +
							"   (extend :foo/complex XMath                             \n" +
							"           (+ [x y] (complex. (core/+ (:re x) (:re y))    \n" +
							"                              (core/+ (:im x) (:im y))))  \n" +
							"           (- [x y] (complex. (core/- (:re x) (:re y))    \n" +
							"                              (core/- (:im x) (:im y))))) \n" +
							"   (extend :core/long XMath                               \n" +
							"           (+ [x y] (core/+ x y))                         \n" +
							"           (- [x y] (core/- x y)))                        \n" +
							"   (foo/+ (complex. 1 1)  (complex. 4 5)))                  ",
							
							"(do                                                                  \n" +
							"   (ns foo)                                                          \n" +
							"   (defprotocol Lifecycle (start [c]) (stop [c]))                    \n" +
							"   (deftype :component [name :string]                                \n" +
							"            Lifecycle (start [c] (println \"'~(:name c)' started\")) \n" +
							"                      (stop [c] (println \"'~(:name c)' stopped\"))) \n" +
							"   (let [c          (component. \"test\")                            \n" +
							"         lifecycle? (extends? (type c) Lifecycle)]                   \n" +
							"     (println \"'~(:name c)' extends Lifecycle protocol: ~{lifecycle?}\") \n" +
							"     (start c)                                                       \n" +
							"     (stop c)))                                                        ")
						.seeAlso("extend", "extends?", "defmulti")
						.build()
			) {
				public VncVal apply(
						final VncVal specialFormMeta,
						final VncList args, 
						final Env env, 
						final SpecialFormsContext ctx
				) {
					// (defprotocol P
					//	  (foo [x])
					//	  (bar [x] [x y])
					//	  (zoo [x] "default val")
					//	  (yar [x] [x y] "default val"))

					final CallFrame callframe = new CallFrame("defprotocol", args, specialFormMeta);
					try (WithCallStack cs = new WithCallStack(callframe)) {
						assertMinArity("defprotocol", FnType.SpecialForm, args, 2);
					}

					final VncSymbol protocolName = Namespaces.qualifySymbolWithCurrNS(
														evaluateSymbolMetaData(args.first(), env, ctx));

					validateDefProtocol(args);
								
					VncMap protocolFns = VncHashMap.empty();
					for(VncVal s : args.rest()) {
						final VncMultiArityFunction fn = parseProtocolFnSpec(s, env, ctx);
						
						final VncSymbol fnName = new VncSymbol(
														protocolName.getNamespace(), 
														fn.getSimpleName(),
														specialFormMeta);
						
						final VncProtocolFunction fnProtocol = new VncProtocolFunction(
																		fnName.getQualifiedName(), 
																		protocolName,
																		fn, 
																		fn.getMeta());

						final VncVal p = env.getGlobalOrNull(fnName);
						if (p instanceof VncProtocolFunction) {
							if (!((VncProtocolFunction)p).getProtocolName().equals(protocolName)) {
								// collision of protocol function name with another protocol
								throw new VncException(String.format(
											"The protocol function '%s' of protocol '%s' collides "
													+ "with the same function in protocol '%s' in "
													+ "the same namespace!",
											fn.getSimpleName(),
											((VncProtocolFunction)p).getProtocolName(),
											protocolName));
							}
						}
						
						env.setGlobal(new Var(fnName, fnProtocol));

						protocolFns = protocolFns.assoc(new VncString(fn.getSimpleName()), fn);
					}
			
					final VncProtocol protocol = new VncProtocol(protocolName, protocolFns, protocolName.getMeta());
					
					env.setGlobal(new Var(protocolName, protocol));

					return protocol;
				}
				
				public boolean addCallFrame() { 
					return false; 
				}


				private static final long serialVersionUID = -1848883965231344442L;
			};

	public static VncSpecialForm extend_ =
		new VncSpecialForm(
				"extend",
				VncSpecialForm
					.meta()
					.arglists("(extend type protocol fns*)")		
					.doc(
						"Extends protocol for type with the supplied functions.    \n\n" +
						"Formats:                                                  \n\n" +
						" * `(extend :core/long P (foo [x] x))`                    \n" +
						" * `(extend :core/long P (foo [x] x) (foo [x y] x))`      \n" +
						" * `(extend :core/long P (foo [x] x) (bar [x] x))`          ")
					.examples(
						"(do                                                       \n" +
						"   (ns foo)                                               \n" +
						"   (deftype :complex [re :long, im :long])                \n" +
						"   (defprotocol XMath (+ [x y])                           \n" +
						"                      (- [x y]))                          \n" +
						"   (extend :foo/complex XMath                             \n" +
						"           (+ [x y] (complex. (core/+ (:re x) (:re y))    \n" +
						"                              (core/+ (:im x) (:im y))))  \n" +
						"           (- [x y] (complex. (core/- (:re x) (:re y))    \n" +
						"                              (core/- (:im x) (:im y))))) \n" +
						"   (extend :core/long XMath                               \n" +
						"           (+ [x y] (core/+ x y))                         \n" +
						"           (- [x y] (core/- x y)))                        \n" +
						"   (foo/+ (complex. 1 1)  (complex. 4 5)))                  ")
					.seeAlso("defprotocol", "extends?")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				final VncVal typeRef = args.first();
				final VncVal protocolRef = args.second();
				
				final VncSymbol protocolSym = Namespaces.qualifySymbolWithCurrNS(
												evaluateSymbolMetaData(protocolRef, env, ctx));

				return extendType(typeRef, protocolSym, args.drop(2), env, ctx);
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncSpecialForm extendsQ_ =
		new VncSpecialForm(
				"extends?",
				VncSpecialForm
					.meta()
					.arglists("(extends? type protocol)")		
					.doc("Returns true if the type extends the protocol.")
					.examples(
						"(do                                                       \n" +
						"   (ns foo)                                               \n" +
						"   (deftype :complex [re :long, im :long])                \n" +
						"   (defprotocol XMath (+ [x y])                           \n" +
						"                      (- [x y]))                          \n" +
						"   (extend :foo/complex XMath                             \n" +
						"           (+ [x y] (complex. (core/+ (:re x) (:re y))    \n" +
						"                              (core/+ (:im x) (:im y))))  \n" +
						"           (- [x y] (complex. (core/- (:re x) (:re y))    \n" +
						"                              (core/- (:im x) (:im y))))) \n" +
						"   (extend :core/long XMath                               \n" +
						"           (+ [x y] (core/+ x y))                         \n" +
						"           (- [x y] (core/- x y)))                        \n" +
						"   (extends? :foo/complex XMath))                           ")
					.seeAlso("defprotocol", "extend")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				final VncVal typeRef = args.first();
				
				final VncSymbol protocolSym = Namespaces.qualifySymbolWithCurrNS(
												evaluateSymbolMetaData(args.second(), env, ctx));

				final VncVal typeRefEval = ctx.getEvaluator().evaluate(typeRef, env, false);
				
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
	
			private static final long serialVersionUID = -1848883965231344442L;
		};

	
		
	///////////////////////////////////////////////////////////////////////////
	// deftype functions
	///////////////////////////////////////////////////////////////////////////

	public static VncSpecialForm deftype =
		new VncSpecialForm(
				"deftype",
				VncSpecialForm
					.meta()
					.arglists(
						"(deftype name fields)",
						"(deftype name fields validator)")
					.doc(
						"Defines a new custom *record* type for the name with " +
						"the fields. \n\n" +
						"Venice implicitly creates a builder and a type check " +
						"function suffixed with a dot and a question mark:\n\n" +
						"```venice                                        \n" +
						"(deftype :point [x :long, y :long])              \n" +
						"                                                 \n" +
						"(point. 200 300)           ; builder             \n" +
						"(point? (point. 200 300))  ; type check          \n" +
						"```                                              \n\n" +
						"The builder accepts values of any subtype of the \n" +
						"field's type.")
					.examples(
						"(do                                                      \n" +
						"  (ns foo)                                               \n" +
						"  (deftype :point [x :long, y :long])                    \n" +
						"  ; explicitly creating a custom type value              \n" +
						"  (def x (.: :point 100 200))                            \n" +
						"  ; Venice implicitly creates a builder function         \n" +
						"  ; suffixed with a '.'                                  \n" +
						"  (def y (point. 200 300))                               \n" +
						"  ; ... and a type check function                        \n" +
						"  (point? y)                                             \n" +
						"  y)                                                       ",
						"(do                                                      \n" +
						"  (ns foo)                                               \n" +
						"  (deftype :point [x :long, y :long])                    \n" +
						"  (def x (point. 100 200))                               \n" +
						"  (type x))                                                ",
						"(do                                                      \n" +
						"  (ns foo)                                               \n" +
						"  (deftype :point [x :long, y :long]                     \n" +
						"     (fn [p]                                             \n" +
						"       (assert (pos? (:x p)) \"x must be positive\")     \n" +
						"       (assert (pos? (:y p)) \"y must be positive\")))   \n" +
						"  (def p (point. 100 200))                               \n" +
						"  [(:x p) (:y p)])                                        ",
						"(do                                                 \n" +
						"  (ns foo)                                          \n" +
						"  (deftype :named [name :string, value :any])       \n" +
						"  (def x (named. \"count\" 200))                    \n" +
						"  (def y (named. \"seq\" [1 2]))                    \n" +
						"  [x y])                                              ",
						";; modifying a custom type field                    \n" +
						"(do                                                 \n" +
						"  (deftype :point [x :long, y :long])               \n" +
						"  (def p (point. 0 0))                              \n" +
						"  (def q (assoc p :x 1 :y 2)) ; q is a 'point'      \n" +
						"  (pr-str q))                                         ",
						";; removing a custom type field                     \n" +
						"(do                                                 \n" +
						"  (deftype :point [x :long, y :long])               \n" +
						"  (def p (point. 100 200))                          \n" +
						"  (def q (dissoc p :x)) ; q is just a map now       \n" +
						"  (pr-str q))                                         ")
					.seeAlso(
						"deftype?", "deftype-of", "deftype-or", ".:", 
						"deftype-describe", "Object", "assoc", "dissoc")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				// (deftype :complex [real :long, imaginary :long])
				//
				// (deftype :complex [real :long, imaginary :long]
				//          P (foo [x])
				assertMinArity("deftype", FnType.SpecialForm, args, 2);
				
				final VncList deftypeArgs = args.takeWhile(e -> !Types.isVncSymbol(e));
				VncList extendArgs = args.drop(deftypeArgs.size());
				
				// [1] parse deftype args
				final VncKeyword type = Coerce.toVncKeyword(
											ctx.getEvaluator().evaluate(
													deftypeArgs.first(), env, false));

				final VncVector fields = Coerce.toVncVector(deftypeArgs.second());

				final VncFunction validationFn = deftypeArgs.size() == 3
													? Coerce.toVncFunction(
															ctx.getEvaluator().evaluate(
																	args.third(), env, false))
													: null;
				
				// custom type is a namespace qualified keyword
				final VncVal customType = DefTypeForm.defineCustomType(
												type, fields, validationFn, ctx.getInterpreter(), env);
		
				// [2] parse extend protocol definitions
				while(!extendArgs.isEmpty()) {
					final VncVal protocolSym = extendArgs.first();
					final VncList extDefs = extendArgs.drop(1).takeWhile(e -> Types.isVncList(e));
					if (Types.isVncSymbol(protocolSym)) {
						extendArgs = extendArgs.drop(extDefs.size()+1);
						
						// process the extend definition
						extendType(customType,
								(VncSymbol)protocolSym,
								extDefs, 
								env,
								ctx);						
					}
					else {
						throw new VncException(String.format(
								"Invalid extend protocol definitions for custom type '%s'",
								type.toString()));
					}
				}	
				
				return customType;
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncSpecialForm deftypeQ =
		new VncSpecialForm(
				"deftype?",
				VncSpecialForm
					.meta()
					.arglists(
						"(deftype? type)")
					.doc(
						"Returns true if `type` is a custom type else false.")
					.examples(
						"(do                                                 \n" +
						"  (ns foo)                                          \n" +
						"  (deftype :complex [real :long, imaginary :long])  \n" +
						"  (deftype? :complex))                                ",
						"(do                                                 \n" +
						"  (ns foo)                                          \n" +
						"  (deftype-of :email-address :string)               \n" +
						"  (deftype? :email-address))                          ",
						"(do                                                 \n" +
						"  (ns foo)                                          \n" +
						"  (deftype :complex [real :long, imaginary :long])  \n" +
						"  (def x (complex. 100 200))                        \n" +
						"  (deftype? (type x)))                                ")
					.seeAlso(
						"deftype", "deftype-of", "deftype-or", ".:", 
						"deftype-describe")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				assertArity("deftype?", FnType.SpecialForm, args, 1);
				final VncVal type = ctx.getEvaluator().evaluate(args.first(), env, false);
				return VncBoolean.of(DefTypeForm.isCustomType(type, env));
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncSpecialForm deftype_describe =
		new VncSpecialForm(
				"deftype-describe",
				VncSpecialForm
					.meta()
					.arglists(
						"(deftype-describe type)")
					.doc(
						"Describes a custom type.")
					.examples(
						"(do                                                           \n" +
						"  (ns foo)                                                    \n" +
						"  (deftype :complex [real :long, imaginary :long])		       \n" +
						"  (deftype-describe :complex))	                               \n",
						"(do                                                           \n" +
						"  (ns foo)                                                    \n" +
						"  (deftype-of :port :long)		                               \n" +
						"  (deftype-describe :port))	                               \n",
						"(do                                                           \n" +
						"  (ns foo)                                                    \n" +
						"  (deftype-or :digit 0 1 2 3 4 5 6 7 8 9)                     \n" +
						"  (deftype-describe :digit))                                    ")
					.seeAlso(
						"deftype", "deftype?", "deftype-or", "deftype-of", ".:")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				assertArity("deftype-describe", FnType.SpecialForm, args, 1);
				final VncVal evaluatedArg = ctx.getEvaluator().evaluate(args.first(), env, false);
				return DefTypeForm.describeType(evaluatedArg, env);
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncSpecialForm deftype_new =
		new VncSpecialForm(
				".:",
				VncSpecialForm
					.meta()
					.arglists("(.: type-name args*)")
					.doc(
						"Instantiates a custom type.                           \n\n" +
						"Note: Venice implicitly creates a builder function    \n" +
						"suffixed with a dot:                                  \n\n" +
						"```venice                                             \n" +
						"(deftype :complex [real :long, imaginary :long])      \n" +
						"(complex. 200 300)                                    \n" +
						"```                                                   \n\n" +
						"For readability prefer `(complex. 200 300)` over      \n" +
						"`(.: :complex 100 200)`.                                ")
					.examples(
						"(do                                                      \n" +
						"  (ns foo)                                               \n" +
						"  (deftype :complex [real :long, imaginary :long])       \n" +
						"  (def x (.: :complex 100 200))                          \n" +
						"  [(:real x) (:imaginary x)])                              ")
					.seeAlso(
						"deftype", "deftype?", "deftype-of", "deftype-or", 
						"deftype-describe")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				assertMinArity(".:", FnType.SpecialForm, args, 1);
				final List<VncVal> evaluatedArgs = new ArrayList<>();
				for(VncVal v : args) {
					evaluatedArgs.add(ctx.getEvaluator().evaluate(v, env, false));
				}
				return DefTypeForm.createType(evaluatedArgs, env);
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};

		public static VncSpecialForm deftype_of =
			new VncSpecialForm(
					"deftype-of",
					VncSpecialForm
						.meta()
						.arglists(
							"(deftype-of name base-type)",
							"(deftype-of name base-type validator)")
						.doc(
							"Defines a new custom *wrapper* type based on a base type. \n\n" +
							"Venice implicitly creates a builder and a type check " +
							"function suffixed with a dot and a question mark:\n\n" +
							"```venice                           \n" +
							"(deftype-of :port :long)            \n" +
							"                                    \n" +
							"(port. 8080)          ; builder     \n" +
							"(port? (port. 8080))  ; type check  \n" +
							"```")
						.examples(
							"(do                                                           \n" +
							"  (ns foo)                                                    \n" +
							"  (deftype-of :email-address :string)                         \n" +
							"  ; explicitly creating a wrapper type value                  \n" +
							"  (def x (.: :email-address \"foo@foo.org\"))                 \n" +
							"  ; Venice implicitly creates a builder function              \n" +
							"  ; suffixed with a '.'                                       \n" +
							"  (def y (email-address. \"foo@foo.org\"))                    \n" +
							"  ; ... and a type check function                             \n" +
							"  (email-address? y)                                          \n" +
							"  y)                                                            ",
							"(do                                                           \n" +
							"  (ns foo)                                                    \n" +
							"  (deftype-of :email-address :string)                         \n" +
							"  (str \"Email: \" (email-address. \"foo@foo.org\")))           ",
							"(do                                                           \n" +
							"  (ns foo)                                                    \n" +
							"  (deftype-of :email-address :string)                         \n" +
							"  (def x (email-address. \"foo@foo.org\"))                    \n" +
							"  [(type x) (supertype x)])                                     ",
							"(do                                                           \n" +
							"  (ns foo)                                                    \n" +
							"  (deftype-of :email-address                                  \n" +
							"              :string                                         \n" +
							"              str/valid-email-addr?)                          \n" +
							"  (email-address. \"foo@foo.org\"))                             ",
							"(do                                                           \n" +
							"  (ns foo)                                                    \n" +
							"  (deftype-of :contract-id :long)                             \n" +
							"  (contract-id. 100000))                                        ",
							"(do                                                           \n" +
							"  (ns foo)                                                    \n" +
							"  (deftype-of :my-long :long)                                 \n" +
							"  (+ 10 (my-long. 100000)))                                     ")
						.seeAlso(
							"deftype", "deftype?", "deftype-or", ".:", "deftype-describe")
						.build()
			) {
				public VncVal apply(
						final VncVal specialFormMeta,
						final VncList args, 
						final Env env, 
						final SpecialFormsContext ctx
				) {
					assertMinArity("deftype-of", FnType.SpecialForm, args, 2);
					final VncKeyword type = Coerce.toVncKeyword(ctx.getEvaluator().evaluate(args.first(), env, false));
					final VncKeyword baseType = Coerce.toVncKeyword(ctx.getEvaluator().evaluate(args.second(), env, false));
					final VncFunction validationFn = args.size() == 3
														? Coerce.toVncFunction(ctx.getEvaluator().evaluate(args.third(), env, false))
														: null;
					return DefTypeForm.defineCustomWrapperType(
								type, 
								baseType, 
								validationFn, 
								ctx.getInterpreter(), 
								env,
								ctx.getWrappableTypes());
				}
		
				private static final long serialVersionUID = -1848883965231344442L;
			};

		public static VncSpecialForm deftype_or =
			new VncSpecialForm(
					"deftype-or",
					VncSpecialForm
						.meta()
						.arglists(
							"(deftype-or name val*)")
						.doc(
							"Defines a new custom *choice* type. \n\n" +
							"Venice implicitly creates a builder and a type check " +
							"function suffixed with a dot and a question mark:\n\n" +
							"```venice                                 \n" +
							"(deftype-or :color :red :green :blue)     \n" +
							"                                          \n" +
							"(color. :blue)           ; builder        \n" +
							"(color? (color. :blue))  ; type check     \n" +
							"```")
						.examples(
							"(do                                                           \n" +
							"  (ns foo)                                                    \n" +
							"  (deftype-or :color :red :green :blue)                       \n" +
							"  ; explicitly creating a wrapper type value                  \n" +
							"  (def x (.: :color :red))                                    \n" +
							"  ; Venice implicitly creates a builder function              \n" +
							"  ; suffixed with a '.'                                       \n" +
							"  (def y (color. :blue))                                       \n" +
							"  ; ... and a type check function                             \n" +
							"  (color? y)                                                  \n" +
							"  y)                                                            ",
							"(do                                                           \n" +
							"  (ns foo)                                                    \n" +
							"  (deftype-or :digit 0 1 2 3 4 5 6 7 8 9)                     \n" +
							"  (digit. 1))                                                   ",
							"(do                                                           \n" +
							"  (ns foo)                                                    \n" +
							"  (deftype-or :long-or-double :long :double)                  \n" +
							"  (long-or-double. 1000))                                       ")
						.seeAlso(
							"deftype", "deftype?", "deftype-of", ".:", "deftype-describe")
						.build()
			) {
				public VncVal apply(
						final VncVal specialFormMeta,
						final VncList args, 
						final Env env, 
						final SpecialFormsContext ctx
				) {
					assertMinArity("deftype-or", FnType.SpecialForm, args, 2);
					final VncKeyword type = Coerce.toVncKeyword(ctx.getEvaluator().evaluate(args.first(), env, false));
					final VncList choiceVals = args.rest();

					return DefTypeForm.defineCustomChoiceType(type, choiceVals, ctx.getInterpreter(), env);
				}
		
				private static final long serialVersionUID = -1848883965231344442L;
			};
	
			
			
					
	///////////////////////////////////////////////////////////////////////////
	// multimethod functions
	///////////////////////////////////////////////////////////////////////////
			
	public static VncSpecialForm defmethod =
		new VncSpecialForm(
				"defmethod",
				VncSpecialForm
					.meta()
					.arglists("(defmethod multifn-name dispatch-val & fn-tail)")		
					.doc("Creates a new method for a multimethod associated with a dispatch-value.")
					.examples(
							"(do                                                                       \n" +
							"   ;;defmulti with dispatch function                                      \n" +
							"   (defmulti salary (fn [amount] (amount :t)))                            \n" +
							"                                                                          \n" +
							"   ;;defmethod provides a function implementation for a particular value  \n" +
							"   (defmethod salary \"com\" [amount] (+ (:b amount) (/ (:b amount) 2)))  \n" +
							"   (defmethod salary \"bon\" [amount] (+ (:b amount) 99))                 \n" +
							"   (defmethod salary :default  [amount] (:b amount))                      \n" +
							"                                                                          \n" +
							"   [(salary {:t \"com\" :b 1000})                                         \n" +
							"    (salary {:t \"bon\" :b 1000})                                         \n" +
							"    (salary {:t \"xxx\" :b 1000})]                                        \n" +
							")                                                                           ")
					.seeAlso("defmulti")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				assertMinArity("defmethod", FnType.SpecialForm, args, 2);
				final VncSymbol multiFnName = Namespaces.qualifySymbolWithCurrNS(
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
				final VncVector preConditions = getFnPreconditions(args.fourth());
				final VncList body = args.slice(preConditions == null ? 3 : 4);
				final VncFunction fn = ctx.getFunctionBuilder().buildFunction(
											multiFnName.getName(),
											params,
											body,
											preConditions,
											false,
											specialFormMeta,
											env);

				return multiFn.addFn(dispatchVal, fn.withMeta(specialFormMeta));
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncSpecialForm defmulti =
		new VncSpecialForm(
				"defmulti",
				VncSpecialForm
					.meta()
					.arglists("(defmulti name dispatch-fn)")
					.doc("Creates a new multimethod with the associated dispatch function.")
					.examples(
						"(do                                                                         \n" +
						"   ;;defmulti with dispatch function                                        \n" +
						"   (defmulti salary (fn [amount] (amount :t)))                              \n" +
						"                                                                            \n" +
						"   ;;defmethod provides a function implementation for a particular value    \n" +
						"   (defmethod salary \"com\"   [amount] (+ (:b amount) (/ (:b amount) 2)))  \n" +
						"   (defmethod salary \"bon\"   [amount] (+ (:b amount) 99))                 \n" +
						"   (defmethod salary :default  [amount] (:b amount))                        \n" +
						"                                                                            \n" +
						"   [(salary {:t \"com\" :b 1000})                                           \n" +
						"    (salary {:t \"bon\" :b 1000})                                           \n" +
						"    (salary {:t \"xxx\" :b 1000})]                                          \n" +
						")                                                                             ",
						"(do                                                \n" +
						"   ;;dispatch on type                              \n" +
						"   (defmulti test (fn [x] (type x)))               \n" +
						"                                                   \n" +
						"   (defmethod test :core/number  [x] [x :number])  \n" +
						"   (defmethod test :core/string  [x] [x :string])  \n" +
						"   (defmethod test :core/boolean [x] [x :boolean]) \n" +
						"   (defmethod test :default      [x] [x :default]) \n" +
						"                                                   \n" +
						"   [(test 1)                                       \n" +
						"    (test 1.0)                                     \n" +
						"    (test 1.0M)                                    \n" +
						"    (test \"abc\")                                 \n" +
						"    (test [1])]                                    \n" +
						")                                                    ")
					.seeAlso("defmethod")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				assertArity("defmulti", FnType.SpecialForm, args, 2);
				final VncSymbol name =  validateSymbolWithCurrNS(
											Namespaces.qualifySymbolWithCurrNS(
													evaluateSymbolMetaData(args.first(), env, ctx)),
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
					dispatchFn = (IVncFunction)fn.apply(specialFormMeta, fnAst.rest(), env, ctx);
				}

				final VncMultiFunction multiFn = new VncMultiFunction(name.getName(), dispatchFn)
															.withMeta(name.getMeta());
				env.setGlobal(new Var(name, multiFn, true));
				return multiFn;
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};
		
		
		
	///////////////////////////////////////////////////////////////////////////
	// create functions
	///////////////////////////////////////////////////////////////////////////
			
	public static VncSpecialForm fn =
		new VncSpecialForm(
				"fn",
				VncSpecialForm
					.meta()
					.arglists("(fn name? [params*] condition-map? expr*)")
					.doc("Defines an anonymous function.")
					.examples(
						"(do                             \n" +
						"  (def sum (fn [x y] (+ x y)))  \n" +
						"  (sum 2 3))                    ",
						
						";; multi-arity anonymous function      \n" +
						"(let [f (fn ([x] x) ([x y] (+ x y)))]  \n" +
						"   [(f 1) (f 4 6)])",
							
						"(map (fn double [x] (* 2 x)) (range 1 5))",
						
						"(map #(* 2 %) (range 1 5))",
						
						"(map #(* 2 %1) (range 1 5))",
						
						";; anonymous function with two params, the second is destructured\n" + 
						"(reduce (fn [m [k v]] (assoc m v k)) {} {:b 2 :a 1 :c 3})",
						
						";; defining a pre-condition                 \n" + 
						"(do                                         \n" +
						"   (def square-root                         \n" +
						"        (fn [x]                             \n" +
						"            { :pre [(>= x 0)] }             \n" +
						"            (. :java.lang.Math :sqrt x)))   \n" +
						"   (square-root 4))                           ",
						
						";; higher-order function                                           \n" + 
						"(do                                                                \n" +
						"   (def discount                                                   \n" +
						"        (fn [percentage]                                           \n" +
						"            { :pre [(and (>= percentage 0) (<= percentage 100))] } \n" +
						"            (fn [price] (- price (* price percentage 0.01)))))     \n" +
						"   ((discount 50) 300))                                              ")
					.seeAlso("defn", "defn-", "def")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				// single arity:  (fn name? [params*] condition-map? expr*)
				// multi arity:   (fn name? ([params*] condition-map? expr*)+ )

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
		
				final VncSymbol fnName = Namespaces.qualifySymbolWithCurrNS(name);
				ReservedSymbols.validateNotReservedSymbol(fnName);
			
				final FunctionBuilder functionBuilder = ctx.getFunctionBuilder();

				final VncSequence paramsOrSig = Coerce.toVncSequence(args.nth(argPos));
				if (Types.isVncVector(paramsOrSig)) {
					// single arity:
					
					argPos++;
					final VncVector params = (VncVector)paramsOrSig;				
					final VncVector preCon = getFnPreconditions(args.nthOrDefault(argPos, null));
					if (preCon != null) argPos++;
					
					final VncList body = args.slice(argPos);
					
					return functionBuilder.buildFunction(
								fnName.getName(), params, body, preCon, 
								false, meta, env);
				}
				else {
					// multi arity:
		
					final List<VncFunction> fns = new ArrayList<>();
					
					args.slice(argPos).forEach(s -> {
						int pos = 0;
						
						final VncList sig = Coerce.toVncList(s);					
						final VncVector params = Coerce.toVncVector(sig.nth(pos++));					
						final VncVector preCon = getFnPreconditions(sig.nth(pos));
						if (preCon != null) pos++;
						
						final VncList body = sig.slice(pos);
						
						fns.add(
							functionBuilder.buildFunction(
								fnName.getName(), params, body, preCon, false, meta, env));
					});
					
					return new VncMultiArityFunction(fnName.getName(), fns, false, meta);
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};


		
	///////////////////////////////////////////////////////////////////////////
	// namespace functions
	///////////////////////////////////////////////////////////////////////////

	public static VncSpecialForm ns_new =
		new VncSpecialForm(
				"ns",
				VncSpecialForm
					.meta()
					.arglists("(ns sym)")
					.doc("Opens a namespace.")
					.examples(
						"(do                               \n" + 
						"  (ns xxx)                        \n" + 
						"  (def foo 1)                     \n" + 
						"  (ns yyy)                        \n" + 
						"  (def foo 5)                     \n" + 
						"  (println xxx/foo foo yyy/foo))    ")
					.seeAlso(
						"*ns*", "ns-unmap", "ns-remove", 
						"ns-list", "ns-alias", "namespace", "var-ns")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				specialFormCallValidation("ns");
				assertArity("ns", FnType.SpecialForm, args, 1);

				final VncVal name = args.first();
				final VncSymbol ns = Types.isVncSymbol(name)
										? (VncSymbol)name
										: (VncSymbol)CoreFunctions.symbol.apply(
														VncList.of(ctx.getEvaluator().evaluate(name, env, false)));
				
				if (ns.hasNamespace() && !"core".equals(ns.getNamespace())) {
					throw new VncException(String.format(
							"A namespace '%s' must not have itself a namespace! However you can use '%s'.",
							ns.getQualifiedName(),
							ns.getNamespace() + "." + ns.getSimpleName()));
				}
				else {
					// clean
					final VncSymbol ns_ = new VncSymbol(ns.getSimpleName());
					
					if (Namespaces.isSystemNS(ns_.getSimpleName()) && ctx.getSealedSystemNS().get()) {
						// prevent Venice's system namespaces from being altered
						throw new VncException("Namespace '" + ns_.getName() + "' cannot be reopened!");
					}
					Namespaces.setCurrentNamespace(ctx.getNsRegistry().computeIfAbsent(ns_));
					return ns_;
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};


	public static VncSpecialForm ns_unmap =
		new VncSpecialForm(
				"ns-unmap",
				VncSpecialForm
					.meta()
					.arglists("(ns-unmap ns sym)")
					.doc("Removes the mappings for the symbol from the namespace.")
					.examples(
						"(do                    \n" + 
						"  (ns foo)             \n" + 
						"  (def x 1)            \n" + 
						"  (ns-unmap 'foo 'x)   \n" + 
						"  (ns-unmap *ns* 'x))   ")
					.seeAlso("ns", "*ns*", "ns-remove", "ns-list", "namespace", "var-ns")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				specialFormCallValidation("ns-unmap");
				assertArity("ns-unmap", FnType.SpecialForm, args, 2);

				final VncSymbol ns = Coerce.toVncSymbol(
										ctx.getEvaluator().evaluate(args.first(), env, false));

				if (Namespaces.isSystemNS(ns.getName()) && ctx.getSealedSystemNS().get()) {
					// prevent Venice's system namespaces from being altered
					throw new VncException("Cannot remove a symbol from namespace '" + ns.getName() + "'!");
				}
				else {
					final VncSymbol sym = Coerce.toVncSymbol(
											ctx.getEvaluator().evaluate(args.second(), env, false));
					env.removeGlobalSymbol(sym.withNamespace(ns));
					return Nil;
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncSpecialForm ns_remove =
		new VncSpecialForm(
				"ns-remove",
				VncSpecialForm
					.meta()
					.arglists("(ns-remove ns)")
					.doc("Removes the mappings for all symbols from the namespace.")
					.examples(
						"(do                                     \n" + 
						"  (ns foo)                              \n" + 
						"  (def x 1)                             \n" + 
						"  (ns bar)                              \n" + 
						"  (def y 1)                             \n" + 
						"  (ns-remove 'foo)                      \n" + 
						"  (println \"ns foo:\" (ns-list 'foo))  \n" + 
						"  (println \"ns bar:\" (ns-list 'bar)))   ")
					.seeAlso("ns", "ns-unmap", "ns-list", "namespace", "var-ns")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				specialFormCallValidation("ns-remove");
				assertArity("ns-remove", FnType.SpecialForm, args, 1);

				final VncSymbol ns = Coerce.toVncSymbol(
										ctx.getEvaluator().evaluate(args.first(), env, false));
				final VncSymbol nsCurr = Namespaces.getCurrentNS();
				if (Namespaces.isSystemNS(ns.getName()) && ctx.getSealedSystemNS().get()) {
					// prevent Venice's system namespaces from being altered
					throw new VncException("Namespace '" + ns.getName() + "' cannot be removed!");
				}
				else if (ns.equals(nsCurr)) {
					// prevent removing the current namespace
					throw new VncException("The current samespace '" + nsCurr.getName() + "' cannot be removed!");
				}
				else {
					env.removeGlobalSymbolsByNS(ns);
					ctx.getNsRegistry().remove(ns);
					return Nil;
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncSpecialForm ns_list =
		new VncSpecialForm(
				"ns-list",
				VncSpecialForm
					.meta()
					.arglists("(ns-list ns)")
					.doc("Lists all the symbols in the namespace ns.")
					.examples("(ns-list 'regex)")
					.seeAlso("ns", "*ns*", "ns-unmap", "ns-remove", "namespace", "var-ns")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				specialFormCallValidation("ns-list");
				assertArity("ns-list", FnType.SpecialForm, args, 1);

				final VncSymbol ns = Coerce.toVncSymbol(
										ctx.getEvaluator().evaluate(args.first(), env, false));

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

			private static final long serialVersionUID = -1848883965231344442L;
		};
		


	///////////////////////////////////////////////////////////////////////////
	// Utility functions
	///////////////////////////////////////////////////////////////////////////
		
	public static VncSpecialForm locking =
		new VncSpecialForm(
				"locking",
				VncSpecialForm
					.meta()
					.arglists("(locking x & exprs)")
					.doc(
						"Executes 'exprs' in an implicit do, while holding the " + 
						"monitor of 'x'. Will release the monitor of 'x' in all " +
						"circumstances. Locking operates like the synchronized " +
						"keyword in Java.")
					.examples(
						"(do                        \n" +
						"   (def x 1)               \n" +
						"   (locking x              \n" +
						"      (println 100)        \n" +
						"      (println 200)))",
						";; Locks are reentrant     \n" +
						"(do                        \n" +
						"   (def x 1)               \n" +
						"   (locking x              \n" +
						"      (locking x           \n" +
						"         (println \"in\")) \n" +
						"      (println \"out\"))) ",
						"(do                                             \n" +
					    "  (defn log [msg] (locking log (println msg)))  \n" +
						"  (log \"message\"))")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				assertMinArity("locking", FnType.SpecialForm, args, 2);
				
				final VncVal mutex = ctx.getEvaluator().evaluate(args.first(), env, false);
		
				synchronized(mutex) {
					return evaluateBody(args.rest(), ctx, env, true);
				}
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncSpecialForm print_highlight =
		new VncSpecialForm(
				"print-highlight",
				VncSpecialForm
					.meta()
					.arglists("(print-highlight form)")
					.doc(
						"Prints the form highlighted to *out*")
					.examples(
						"(print-highlight \"(+ 1 2)\")")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				assertArity("print-highlight", FnType.SpecialForm, args, 1);
				final VncString form = DocForm.highlight(Coerce.toVncString(args.first()), env);
				ctx.getEvaluator().evaluate(VncList.of(new VncSymbol("println"), form), env, false);
				return Nil;
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncSpecialForm modules =
		new VncSpecialForm(
				"modules",
				VncSpecialForm
					.meta()
					.arglists("(modules)")
					.doc("Lists the available modules")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				return VncList.ofList(
						Modules
							.VALID_MODULES
							.stream()
							.filter(s ->!s.equals("core"))  // skip core module
							.sorted()
							.map(s -> new VncKeyword(s))
							.collect(Collectors.toList()));
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncSpecialForm inspect =
		new VncSpecialForm(
				"inspect",
				VncSpecialForm
					.meta()
					.arglists("(inspect val)")
					.doc("Inspect a value")
					.examples("(inspect '+)")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				specialFormCallValidation("inspect");
				assertArity("inspect", FnType.SpecialForm, args, 1);
				final VncSymbol sym = Coerce.toVncSymbol(ctx.getEvaluator().evaluate(args.first(), env, false));
				return Inspector.inspect(env.get(sym));
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncSpecialForm resolve =
		new VncSpecialForm(
				"resolve",
				VncSpecialForm
					.meta()
					.arglists("(resolve symbol)")
					.doc("Resolves a symbol.")
					.examples(
						"(resolve '+)", 
						"(resolve 'y)", 
						"(resolve (symbol \"+\"))",
						"((-> \"first\" symbol resolve) [1 2 3])")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				specialFormCallValidation("resolve");
				assertArity("resolve", FnType.SpecialForm, args, 1);
				return env.getOrNil(Coerce.toVncSymbol(
										ctx.getEvaluator().evaluate(args.first(), env, false)));
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncSpecialForm boundQ =
		new VncSpecialForm(
				"bound?",
				VncSpecialForm
					.meta()
					.arglists("(bound? s)")
					.doc("Returns true if the symbol is bound to a value else false")
					.examples(
						"(bound? 'test)",
						"(let [test 100]   \n" +
						"  (bound? 'test))   ",
						"(do               \n" +
						"  (def a 100)     \n" +
						"  (bound? 'a))      ")
					.seeAlso("let", "def", "defonce")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				return VncBoolean.of(
							env.isBound(
								Coerce.toVncSymbol(
									ctx.getEvaluator().evaluate(args.first(), env, false))));
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncSpecialForm setBANG =
		new VncSpecialForm(
				"set!",
				VncSpecialForm
					.meta()
					.arglists("(set! var-symbol expr)")
					.doc("Sets a global or thread-local variable to the value of the expression.")
					.examples(
						"(do                             \n" +
						"  (def x 10)                    \n" +
						"  (set! x 20)                   \n" +
						"  x)                              ",
						 
						"(do                             \n" +
						"   (def-dynamic x 100)          \n" +
						"   (set! x 200)                 \n" +
						"   x)                             ",
						
						"(do                             \n" +
						"   (def-dynamic x 100)          \n" +
						"   (with-out-str                \n" +
						"      (print x)                 \n" +
						"      (binding [x 200]          \n" +
						"        (print (str \"-\" x))   \n" +
						"        (set! x (inc x))        \n" +
						"        (print (str \"-\" x)))  \n" +
						"      (print (str \"-\" x))))     ")
					.seeAlso("def", "def-dynamic")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				specialFormCallValidation("set!");
				assertArity("set!", FnType.SpecialForm, args, 2);
		
				final VncSymbol sym = Types.isVncSymbol(args.first())
										? (VncSymbol)args.first()
										: Coerce.toVncSymbol(ctx.getEvaluator().evaluate(args.first(), env, false));
				final Var globVar = env.getGlobalVarOrNull(sym);
				if (globVar != null) {
					final VncVal expr = args.second();
					final VncVal val = ctx.getEvaluator().evaluate(expr, env, false);
					
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
	
			private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncSpecialForm quote =
		new VncSpecialForm(
				"quote",
				VncSpecialForm
					.meta()
					.arglists("(quote form)")
					.doc(
						"There are two equivalent ways to quote a form either with " +
						"quote or with '. They prevent the quoted form from being " +
						"evaluated.\n\n" +
						"Regular quotes work recursively with any kind of forms and " +
						"types: strings, maps, lists, vectors...")
					.examples(
						"(quote (1 2 3))",
						"(quote (+ 1 2))",
						"'(1 2 3)",
						"'(+ 1 2)",
						"'(a (b (c d (+ 1 2))))")
					.seeAlso("quasiquote")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				if (args.size() != 1) {
					// only create callstack when needed!
					final CallFrame callframe = new CallFrame("quote", args, specialFormMeta);
					try (WithCallStack cs = new WithCallStack(callframe)) {
						assertArity("quote", FnType.SpecialForm, args, 1);
					}
				}
				return args.first();
			}
			
			public boolean addCallFrame() { 
				return false; 
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncSpecialForm quasiquote =
		new VncSpecialForm(
				"quasiquote",
				VncSpecialForm
					.meta()
					.arglists("(quasiquote form)")
					.doc(
						"Quasi quotes also called syntax quotes (a backquote) supress " +
						"evaluation of the form that follows it and all the nested forms." +
						"\n\n" +
						"unquote:¶\n" +
						"It is possible to unquote part of the form that is quoted with `~`. " +
						"Unquoting allows you to evaluate parts of the syntax quoted expression." +
						"\n\n" +
						"unquote-splicing:¶\n" +
						"Unquote evaluates to a collection of values and inserts the " +
						"collection into the quoted form. But sometimes you want to " +
						"unquote a list and insert its elements (not the list) inside " +
						"the quoted form. This is where `~@` (unquote-splicing) comes " +
						"to rescue.")
					.examples(
						"(quasiquote (16 17 (inc 17)))",
						"`(16 17 (inc 17))",
						"`(16 17 ~(inc 17))",
						"`(16 17 ~(map inc [16 17]))",
						"`(16 17 ~@(map inc [16 17]))",
						"`(1 2 ~@#{1 2 3})",
						"`(1 2 ~@{:a 1 :b 2 :c 3})")
					.seeAlso("quote")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				if (args.size() != 1) {
					// only create callstack when needed!
					final CallFrame callframe = new CallFrame("quasiquote", args, specialFormMeta);
					try (WithCallStack cs = new WithCallStack(callframe)) {
						assertArity("quasiquote", FnType.SpecialForm, args, 1);
					}
				}
				return quasiquote(args.first());
			}
			
			public boolean addCallFrame() { 
				return false; 
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};
			
			
			
	///////////////////////////////////////////////////////////////////////////
	// try-catch functions
	///////////////////////////////////////////////////////////////////////////
		
	public static VncSpecialForm try_ =
		new VncSpecialForm(
				"try",
				VncSpecialForm
					.meta()
					.arglists(
							"(try expr*)",
							"(try expr* (catch selector ex-sym expr*)*)",
							"(try expr* (catch selector ex-sym expr*)* (finally expr*))")
					.doc(
						"Exception handling: try - catch - finally \n\n" +
						"`(try)` without any expression returns `nil`.\n\n" +
						"The exception types \n\n" +
						"  * :java.lang.Exception \n" +
						"  * :java.lang.RuntimeException \n" +
						"  * :com.github.jlangch.venice.VncException \n" +
						"  * :com.github.jlangch.venice.ValueException \n\n" +
						"are imported implicitly so its alias :Exception, :RuntimeException, " +
						":VncException, and :ValueException can be used as selector without " +
						"an import of the class.\n\n" +
						"**Selectors**\n\n" +
						"  * a class: (e.g., :RuntimeException, :java.text.ParseException), " +
						"    matches any instance of that class\n" +
						"  * a key-values vector: (e.g., [key val & kvs]), matches any instance " +
						"    of :ValueException where the exception's value meets the expression " +
						"    `(and (= (get ex-value key) val) ...)`\n" +
						"  * a predicate: (a function of one argument like map?, set?), matches " +
						"    any instance of :ValueException where the predicate applied to the " +
						"    exception's value returns true\n\n" +
						"**Notes:**\n\n" +
						"The finally block is just for side effects, like closing resources. " +
						"It never returns a value!\n\n" +
						"All exceptions in Venice are *unchecked*. If *checked* exceptions are thrown " +
						"in Venice they are immediately wrapped in a :RuntimeException before being " +
						"thrown! If Venice catches a *checked* exception from a Java interop call " +
						"it wraps it in a :RuntimeException before handling it by the catch block " +
						"selectors.")
					.examples(
						"(try                                      \n" +
						"   (throw \"test\")                       \n" +
						"   (catch :ValueException e               \n" +
						"          \"caught ~(ex-value e)\"))        ",
						
						"(try                                       \n" +
						"   (throw 100)                             \n" +
						"   (catch :Exception e -100))                ",
												
						"(try                                       \n" +
						"   (throw 100)                             \n" +
						"   (catch :ValueException e (ex-value e))  \n" +
						"   (finally (println \"...finally\")))       ",
						
						"(try                                              \n" +
						"   (throw (ex :RuntimeException \"message\"))     \n" +
						"   (catch :RuntimeException e (ex-message e)))     ",
						
						";; exception type selector:                       \n" +
						"(try                                              \n" +
						"   (throw [1 2 3])                                \n" +
						"   (catch :ValueException e (ex-value e))         \n" +
						"   (catch :RuntimeException e \"runtime ex\")     \n" +
						"   (finally (println \"...finally\")))             ",
						
						";; key-value selector:                                      \n" +
						"(try                                                        \n" +
						"   (throw {:a 100, :b 200})                                 \n" +
						"   (catch [:a 100] e                                        \n" +
						"      (println \"ValueException, value: ~(ex-value e)\"))   \n" +
						"   (catch [:a 100, :b 200] e                                \n" +
						"      (println \"ValueException, value: ~(ex-value e)\")))   ",
						
						";; key-value selector (exception cause):                           \n" +
						"(try                                                               \n" +
						"   (throw (ex :java.io.IOException \"failure\"))                   \n" +
						"   (catch [:cause-type :java.io.IOException] e                     \n" +
						"      (println \"IOException, msg: ~(ex-message (ex-cause e))\"))  \n" +
						"   (catch :RuntimeException e                                      \n" +
						"      (println \"RuntimeException, msg: ~(ex-message e)\")))         ",
					
						";; predicate selector:                                      \n" +
						"(try                                                        \n" +
						"   (throw {:a 100, :b 200})                                 \n" +
						"   (catch long? e                                           \n" +
						"      (println \"ValueException, value: ~(ex-value e)\"))   \n" +
						"   (catch map? e                                            \n" +
						"      (println \"ValueException, value: ~(ex-value e)\"))   \n" +
						"   (catch #(and (map? %) (= 100 (:a %))) e                  \n" +
						"      (println \"ValueException, value: ~(ex-value e)\"))))   ",
					
						";; predicate selector with custom types:                       \n" +
						"(do                                                            \n" +
						"   (deftype :my-exception1 [message :string, position :long])  \n" +
						"   (deftype :my-exception2 [message :string])                  \n" +
						"                                                               \n" +
						"   (try                                                        \n" +
						"      (throw (my-exception1. \"error\" 100))                   \n" +
						"      (catch my-exception1? e                                  \n" +
						"         (println (:value e)))                                 \n" +
						"      (catch my-exception2? e                                  \n" +
						"         (println (:value e)))))                                 ")
					.seeAlso("try-with", "throw", "ex")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				return handleTryCatchFinally(
						"try",
						args,
						ctx,
						env,
						specialFormMeta,
						new ArrayList<Var>());
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncSpecialForm try_with =
		new VncSpecialForm(
				"try-with",
				VncSpecialForm
					.meta()
					.arglists(
							"(try-with [bindings*] expr*)",
							"(try-with [bindings*] expr* (catch selector ex-sym expr*)*)",
							"(try-with [bindings*] expr* (catch selector ex-sym expr*)* (finally expr))")		
					.doc(
						"*try-with-resources* allows the declaration of resources to be used in a try block " +
						"with the assurance that the resources will be closed after execution " +
						"of that block. The resources declared must implement the Closeable or " +
						"AutoCloseable interface.")
					.examples(
						"(do                                                   \n" +
						"   (import :java.io.FileInputStream)                  \n" +
						"   (let [file (io/temp-file \"test-\", \".txt\")]     \n" +
						"        (io/spit file \"123456789\" :append true)     \n" +
						"        (try-with [is (. :FileInputStream :new file)] \n" +
						"           (io/slurp-stream is :binary false))))        ")
					.seeAlso("try", "throw", "ex")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				final Env localEnv = new Env(env);
				final VncSequence bindings = Coerce.toVncSequence(args.first());
				final List<Var> boundResources = new ArrayList<>();
				
				for(int i=0; i<bindings.size(); i+=2) {
					final VncVal sym = bindings.nth(i);
					final VncVal val = ctx.getEvaluator().evaluate(bindings.nth(i+1), localEnv, false);
		
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
								ctx,
								localEnv,
								specialFormMeta,
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
	
			private static final long serialVersionUID = -1848883965231344442L;
		};

		
		
	///////////////////////////////////////////////////////////////////////////
	// var functions
	///////////////////////////////////////////////////////////////////////////

	public static VncSpecialForm var_get =
		new VncSpecialForm(
				"var-get",
				VncSpecialForm
					.meta()
					.arglists("(var-get v)")
					.doc("Returns a var's value.")
					.examples(
						"(var-get +)",
						"(var-get '+)",
						"(var-get (symbol \"+\"))",
						"((var-get +) 1 2)",
						"(do \n" +
						"  (def x 10) \n" +
						"  (var-get 'x))")
					.seeAlso(
						"var-ns", "var-name", "var-local?", "var-global?", "var-thread-local?")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				specialFormCallValidation("var-get");
				assertArity("var-get", FnType.SpecialForm, args, 1);
				final VncSymbol sym = Types.isVncSymbol(args.first())
										? (VncSymbol)args.first()
										: Coerce.toVncSymbol(
												ctx.getEvaluator().evaluate(args.first(), env,  false));
				return env.getOrNil(sym);
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncSpecialForm var_ns =
		new VncSpecialForm(
				"var-ns",
				VncSpecialForm
					.meta()
					.arglists("(var-ns v)")
					.doc("Returns the namespace of the var's symbol")
					.examples(
						"(var-ns +)",
						"(var-ns '+)",
						"(var-ns (symbol \"+\"))",
						";; aliased function \n" +
						"(do \n" +
						"  (ns foo) \n" +
						"  (def add +)\n" +
						"  (var-ns add))",
						"(do  \n" +
						"  (def x 10) \n" +
						"  (var-ns x))",
						"(let [x 10]\n" +
						"  (var-ns x))",
						";; compare with namespace \n" +
						"(do \n" +
						"  (ns foo) \n" +
						"  (def add +)\n" +
						"  (namespace add))",
						";; compare aliased function with namespace \n" +
						"(do \n" +
						"  (ns foo) \n" +
						"  (def add +)\n" +
						"  (namespace add))")
					.seeAlso(
						"namespace", "var-get", "var-name", "var-local?", "var-global?", "var-thread-local?")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				specialFormCallValidation("var-ns");
				assertArity("var-ns", FnType.SpecialForm, args, 1);
				
				final VncSymbol sym = Types.isVncSymbol(args.first())
										? (VncSymbol)args.first()
										: Coerce.toVncSymbol(
												ctx.getEvaluator().evaluate(args.first(), env, false));
				
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

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncSpecialForm var_name =
		new VncSpecialForm(
				"var-name",
				VncSpecialForm
					.meta()
					.arglists("(var-name v)")
					.doc("Returns the name of the var's symbol")
					.examples(
						"(var-name +)",
						"(var-name '+)",
						"(var-name (symbol \"+\"))",
						";; aliased function \n" +
						"(do \n" +
						"  (ns foo) \n" +
						"  (def add +)\n" +
						"  (var-name add))",
						"(do \n" +
						"  (def x 10) \n" +
						"  (var-name x))",
						"(let [x 10] \n" +
						"  (var-name x))",
						";; compare with name \n" +
						"(do \n" +
						"  (ns foo) \n" +
						"  (def add +)\n" +
						"  (name add))",
						";; compare aliased function with name \n" +
						"(do \n" +
						"  (ns foo) \n" +
						"  (def add +)\n" +
						"  (name add))")
					.seeAlso(
						"name", "var-get", "var-ns", "var-local?", "var-global?", "var-thread-local?")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				specialFormCallValidation("var-name");
				assertArity("var-name", FnType.SpecialForm, args, 1);
				final VncSymbol sym = Types.isVncSymbol(args.first())
										? (VncSymbol)args.first()
										: Coerce.toVncSymbol(
												ctx.getEvaluator().evaluate(args.first(), env, false));
				return new VncString(sym.getSimpleName());
			}
				
			private static final long serialVersionUID = -1848883965231344442L;
			
		};

	public static VncSpecialForm var_localQ =
		new VncSpecialForm(
				"var-local?",
				VncSpecialForm
					.meta()
					.arglists("(var-local? v)")
					.doc("Returns true if the var is local else false")
					.examples(
						"(var-local? +)",
						"(var-local? '+)",
						"(var-local? (symbol \"+\"))",
						"(do               \n" +
						"  (def x 10)      \n" +
						"  (var-local? x))   ",
						"(let [x 10]       \n" +
						"  (var-local? x))   ")
					.seeAlso(
						"var-get", "var-ns", "var-name", "var-global?", "var-thread-local?")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				assertArity("var-local?", FnType.SpecialForm, args, 1);
				final VncSymbol sym = Types.isVncSymbol(args.first())
										? (VncSymbol)args.first()
										: Coerce.toVncSymbol(
												ctx.getEvaluator().evaluate(args.first(), env, false));
				return VncBoolean.of(env.isLocal(sym));
			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncSpecialForm var_thread_localQ =
		new VncSpecialForm(
				"var-thread-local?",
				VncSpecialForm
					.meta()
					.arglists("(var-thread-local? v)")
					.doc("Returns true if the var is thread-local else false")
					.examples(
						"(binding [x 100] \n" +
						"  (var-local? x))")
					.seeAlso(
						"var-get", "var-ns", "var-name", "var-local?", "var-global?")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				assertArity("var-thread-local?", FnType.SpecialForm, args, 1);
				final VncSymbol sym = Types.isVncSymbol(args.first())
										? (VncSymbol)args.first()
										: Coerce.toVncSymbol(
												ctx.getEvaluator().evaluate(args.first(), env, false));
				return VncBoolean.of(env.isDynamic(sym));
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncSpecialForm var_globalQ =
		new VncSpecialForm(
				"var-global?",
				VncSpecialForm
					.meta()
					.arglists("(var-global? v)")
					.doc("Returns true if the var is global else false")
					.examples(
						"(var-global? +)",
						"(var-global? '+)",
						"(var-global? (symbol \"+\"))",
						"(do                \n" +
						"  (def x 10)       \n" +
						"  (var-global? x))   ",
						"(let [x 10]        \n" +
						"  (var-global? x))   ")
					.seeAlso(
						"var-get", "var-ns", "var-name", "var-local?", "var-thread-local?")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				assertArity("var-global?", FnType.SpecialForm, args, 1);
				final VncSymbol sym = Types.isVncSymbol(args.first())
										? (VncSymbol)args.first()
										: Coerce.toVncSymbol(
												ctx.getEvaluator().evaluate(args.first(), env, false));
				return VncBoolean.of(env.isGlobal(sym));
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};
		

	
	///////////////////////////////////////////////////////////////////////////
	// benchmark functions
	///////////////////////////////////////////////////////////////////////////

	public static VncSpecialForm dobench =
		new VncSpecialForm(
				"dobench",
				VncSpecialForm
					.meta()
					.arglists("(dobench count expr)")
					.doc(
						"Runs the expr count times in the most effective way and returns a list of " +
						"elapsed nanoseconds for each invocation. It's main purpose is supporting " +
						"benchmark test.")
					.examples("(dobench 10 (+ 1 1))")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				specialFormCallValidation("dobench");
				assertArity("dobench", FnType.SpecialForm, args, 2);
				
				try {
					final long count = Coerce.toVncLong(args.first()).getValue();
					final VncVal expr = args.second();
					
					final List<VncVal> elapsed = new ArrayList<>();
					for(int ii=0; ii<count; ii++) {
						final long start = System.nanoTime();
						
						final VncVal result = ctx.getEvaluator().evaluate(expr, env, false);
						
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

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncSpecialForm dorun =
		new VncSpecialForm(
				"dorun",
				VncSpecialForm
					.meta()
					.arglists("(dorun count expr)")
					.doc(
						"Runs the expr count times in the most effective way. It's main purpose is " +
						"supporting benchmark tests. Returns the expression result of the last " +
						"invocation.\n\n" +
						"*Note:*¶" +
						"The expression is evaluated for every run. " +
						"Alternatively a zero or one arg function referenced by a symbol can be " +
						"passed:\n\n" +
						"```                      \n" +
						"(let [f (fn [] (+ 1 1))] \n" +
						"  (dorun 10 f))          \n" +
						"```                      \n\n" +
						"When passing a one arg function `dorun` passes the incrementing counter " +
						"value (0..N) to the function:\n\n" +
						"```                       \n" +
						"(let [f (fn [x] (+ x 1))] \n" +
						"  (dorun 10 f))           \n" +
						"```                         ")
					.examples("(dorun 10 (+ 1 1))")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				final IFormEvaluator evaluator = ctx.getEvaluator();
				
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

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncSpecialForm prof =
		new VncSpecialForm(
				"prof",
				VncSpecialForm
					.meta()
					.arglists("(prof opts)")
					.doc(
						"Controls the code profiling. See the companion functions/macros " +
						"'dorun' and 'perf'. The perf macro is built on prof and dorun and " +
						"provides all for simple Venice profiling.\n\n" +
						"The profiler reports a function's elapsed time as \"time with children\"! \n\n" +
						"Profiling recursive functions:¶\n" +
						"Because the profiler reports \"time with children\" and accumulates the " +
						"elapsed time across all recursive calls the resulting time for a " +
						"particular recursive function is higher than the effective time.")
					.examples(
						"(do  \n" +
						"  (prof :on)   ; turn profiler on  \n" +
						"  (prof :off)   ; turn profiler off  \n" +
						"  (prof :status)   ; returns the profiler on/off staus  \n" +
						"  (prof :clear)   ; clear profiler data captured so far  \n" +
						"  (prof :data)   ; returns the profiler data as map  \n" +
						"  (prof :data-formatted)   ; returns the profiler data as formatted text  \n" +
						"  (prof :data-formatted \"Metrics test\")   ; returns the profiler data as formatted text with a title  \n" +
						"  nil)  ")
					.seeAlso("perf", "time")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				// Note on profiling recursive functions: 
				// For recursive functions the profiler reports the 'time with children
				// for the particular recursive function resulting in much higher measured 
				// elapsed times.
				// Profiling TCO based recursive functions report correct times.
				//
				// See:  - https://smartbear.com/learn/code-profiling/fundamentals-of-performance-profiling/
				//       - https://support.smartbear.com/aqtime/docs/profiling-with/profile-various-apps/recursive-routines.html

				specialFormCallValidation("prof");
				assertArity("prof", FnType.SpecialForm, args, 1, 2, 3);

				final MeterRegistry meterRegistry = ctx.getMeterRegistry();
				
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

			private static final long serialVersionUID = -1848883965231344442L;
		};
		
		
		
		
	///////////////////////////////////////////////////////////////////////////
	// helpers
	///////////////////////////////////////////////////////////////////////////

	private static void specialFormCallValidation(final String name) {
		ThreadContext.getInterceptor().validateVeniceFunction(name);
	}

	private static VncSymbol evaluateSymbolMetaData(
			final VncVal symVal, 
			final Env env,
			final SpecialFormsContext ctx
	) {
		final VncSymbol sym = Coerce.toVncSymbol(symVal);
		ReservedSymbols.validateNotReservedSymbol(sym);
		return sym.withMeta(ctx.getEvaluator().evaluate(sym.getMeta(), env, false));
	}

	private static void validateDefProtocol(final VncList args) {
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

	private static VncMultiArityFunction parseProtocolFnSpec(
			final VncVal spec,
			final Env env,
			final SpecialFormsContext ctx
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
									ctx.getValuesEvaluator().evaluate_values(body.butlast(), localEnv);
									return ctx.getEvaluator().evaluate(body.last(), localEnv, false);
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

	private static VncVal extendType(
			final VncVal typeRef,
			final VncSymbol protocolSym,
			final VncList fnSpecList, 
			final Env env,
			final SpecialFormsContext ctx
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
		final boolean isObjectProtocol = protocol.getName().equals(new VncSymbol("Object"));

		for(VncVal fnSpec : fnSpecList.getJavaList()) {
			if (!Types.isVncList(fnSpec)) {
				throw new VncException(String.format(
						"Invalid extend for protocol '%s' with type '%s' . "
						+ "Expected a function spec like '(foo [x] nil)'!",
						protocolSym.getQualifiedName(),
						typeRef.getType()));
			}
			
			// (foo [x] nil)
			VncFunction fn = extendFnSpec(type, (VncList)fnSpec, protocol, env, ctx);
			
			// Handle 'Object' protocol 'toString' function for custom types
			if (isObjectProtocol) {
				VncVal fnName = ((VncList)fnSpec).first();
				if (fnName instanceof VncSymbol) {
					if (((VncSymbol)fnName).getSimpleName().equals("toString")) {
						final VncKeyword qualifiedType = type.hasNamespace() 
															? type 
															: type.withNamespace(Namespaces.getCurrentNS());
				
						final VncVal typeDef = env.getGlobalOrNull(qualifiedType.toSymbol());
						if (typeDef instanceof VncCustomBaseTypeDef) {
							final VncCustomBaseTypeDef customBaseTypeDef = (VncCustomBaseTypeDef)typeDef;
		
							// register custom 'toString' function with the custom type definition
							customBaseTypeDef.setCustomToStringFn(fn);
						}
					}
					else if (((VncSymbol)fnName).getSimpleName().equals("compareTo")) {
						final VncKeyword qualifiedType = type.hasNamespace() 
															? type 
															: type.withNamespace(Namespaces.getCurrentNS());
				
						final VncVal typeDef = env.getGlobalOrNull(qualifiedType.toSymbol());
						if (typeDef instanceof VncCustomBaseTypeDef) {
							final VncCustomBaseTypeDef customBaseTypeDef = (VncCustomBaseTypeDef)typeDef;
		
							// register custom 'compareTo' function with the custom type definition
							customBaseTypeDef.setCustomCompareToFn(fn);
						}
					}
				}
			}
		}
		
		protocol.register(type);
		
		return Nil;
	}

	private static VncFunction extendFnSpec(
			final VncKeyword type,
			final VncList fnSpec,
			final VncProtocol protocol,
			final Env env,
			final SpecialFormsContext ctx
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
		ctx.getEvaluator().evaluate(fnDef, env, false);
		final VncFunction fn = (VncFunction)env.getGlobalOrNull(fnSym);
		env.removeGlobalSymbol(fnSym);
		
		// Register the function for the type on the protocol
		((VncProtocolFunction)protocolFn).register(type, fn);
		
		return fn;
	}

	private static VncVector getFnPreconditions(final VncVal prePostConditions) {
		if (Types.isVncMap(prePostConditions)) {
			final VncVal val = ((VncMap)prePostConditions).get(PRE_CONDITION_KEY);
			if (Types.isVncVector(val)) {
				return (VncVector)val;
			}
		}
		
		return null;
	}	

	private static VncVal handleTryCatchFinally(
			final String specialForm,
			final VncList args,
			final SpecialFormsContext ctx,
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
			return evaluateBody(getTryBody(args), ctx, bodyEnv, true);
		} 
		catch (Exception ex) {
			final RuntimeException wrappedEx = ex instanceof RuntimeException 
													? (RuntimeException)ex 
													: new RuntimeException(ex);
			
			final CatchBlock catchBlock = findCatchBlockMatchingThrowable(ctx, env, args, ex);
			if (catchBlock == null) {
				throw wrappedEx;
			}
			else {
				final Env catchEnv = new Env(env);
				catchEnv.setLocal(new Var(catchBlock.getExSym(), new VncJavaObject(wrappedEx)));			
				catchBlockDebug(threadCtx, debugAgent, catchBlock.getMeta(), catchEnv, catchBlock.getExSym(), wrappedEx);
				return evaluateBody(catchBlock.getBody(), ctx, catchEnv, false);
			}
		}
		finally {
			final FinallyBlock finallyBlock = findFirstFinallyBlock(args);
			if (finallyBlock != null) {
				final Env finallyEnv = new Env(env);
				finallyBlockDebug(threadCtx, debugAgent, finallyBlock.getMeta(), finallyEnv);
				evaluateBody(finallyBlock.getBody(), ctx, finallyEnv, false);
			}
		}
	}
	
	private static VncList getTryBody(final VncList args) {
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
	
	private static CatchBlock findCatchBlockMatchingThrowable(
			final SpecialFormsContext ctx,
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
					if (isCatchBlockMatchingThrowable(ctx, env, block, th)) {
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
	
	private static boolean isCatchBlockMatchingThrowable(
			final SpecialFormsContext ctx,
			final Env env,
			final VncList block, 
			final Throwable th
	) {
		final VncVal selector = ctx.getEvaluator().evaluate(block.second(), env, false);

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
	
	private static FinallyBlock findFirstFinallyBlock(final VncList blocks) {
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
	
	private static void catchBlockDebug(
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
	
	private static void finallyBlockDebug(
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

	private static VncVal getValueExceptionValue(final ValueException ex) {
		final Object val = ex.getValue();
		
		return val == null 
				? Nil
				: val instanceof VncVal 
					? (VncVal)val 
					: new VncJavaObject(val);
	}
	
	private static VncVal evaluateBody(
			final VncList body, 
			final SpecialFormsContext ctx,
			final Env env, 
			final boolean withTailPosition
	) {
		ctx.getValuesEvaluator().evaluate_values(body.butlast(), env);
		return ctx.getEvaluator().evaluate(body.last(), env, withTailPosition);
	}

	/**
	 * Resolves a class name.
	 * 
	 * @param className A simple class name like 'Math' or a class name
	 *                  'java.lang.Math'
	 * @return the mapped class 'Math' -&gt; 'java.lang.Math' or the passed 
	 *         value if a mapping does nor exist 
	 */
	private static String resolveClassName(final String className) {
		return Namespaces
					.getCurrentNamespace()
					.getJavaImports()
					.resolveClassName(className);
	}

	private static VncSymbol validateSymbolWithCurrNS(
			final VncSymbol sym,
			final String specialFormName
	) {
		if (sym != null) {
			// do not allow to hijack another namespace
			final String ns = sym.getNamespace();
			if (ns != null && !ns.equals(Namespaces.getCurrentNS().getName())) {
				final CallFrame cf = new CallFrame(specialFormName, sym.getMeta());
				try (WithCallStack cs = new WithCallStack(cf)) {
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

	private static boolean isNonEmptySequence(final VncVal x) {
		return Types.isVncSequence(x) && !((VncSequence)x).isEmpty();
	}
	

	private static final VncKeyword PRE_CONDITION_KEY = new VncKeyword(":pre");
	private static final VncKeyword CAUSE_TYPE_SELECTOR_KEY = new VncKeyword(":cause-type");

	
	
	
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns =
			new SymbolMapBuilder()
					.add(boundQ)
					.add(def)
					.add(defonce)
					.add(def_dynamic)
					.add(defmethod)
					.add(defmulti)
					.add(defprotocol)
					.add(deftype)
					.add(deftype_describe)
					.add(deftype_new)
					.add(deftype_of)
					.add(deftype_or)
					.add(deftypeQ)
					.add(dobench)
					.add(doc)
					.add(dorun)
					.add(extend_)
					.add(extendsQ_)
					.add(fn)
					.add(import_)
					.add(imports_)
					.add(inspect)
					.add(locking)
					.add(modules)
					.add(ns_new)
					.add(ns_list)
					.add(ns_remove)
					.add(ns_unmap)
					.add(print_highlight)
					.add(prof)
					.add(quote)
					.add(quasiquote)
					.add(resolve)
					.add(setBANG)
					.add(try_)
					.add(try_with)
					.add(var_get)
					.add(var_globalQ)
					.add(var_localQ)
					.add(var_name)
					.add(var_ns)
					.add(var_thread_localQ)
					.toMap();
}
