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

import static com.github.jlangch.venice.impl.types.Constants.Nil;
import static com.github.jlangch.venice.impl.util.ArityExceptions.assertArity;
import static com.github.jlangch.venice.impl.util.ArityExceptions.assertMinArity;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Destructuring;
import com.github.jlangch.venice.impl.Namespace;
import com.github.jlangch.venice.impl.Namespaces;
import com.github.jlangch.venice.impl.docgen.runtime.DocForm;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.env.GenSym;
import com.github.jlangch.venice.impl.env.ReservedSymbols;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.javainterop.JavaImports;
import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncMultiArityFunction;
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
import com.github.jlangch.venice.impl.util.CallFrame;
import com.github.jlangch.venice.impl.util.MetaUtil;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;
import com.github.jlangch.venice.impl.util.WithCallStack;


/**
 * The special form pesudo functions
 */
public class SpecialFormsFunctions {

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

	private static boolean isNonEmptySequence(final VncVal x) {
		return Types.isVncSequence(x) && !((VncSequence)x).isEmpty();
	}

	
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns =
			new SymbolMapBuilder()
					.add(defprotocol)
					.add(deftype)
					.add(extend_)
					.add(extendsQ_)
					.add(doc)
					.add(import_)
					.add(imports_)
					.add(var_get)
					.add(var_globalQ)
					.add(var_localQ)
					.add(var_name)
					.add(var_ns)
					.add(var_thread_localQ)
					.toMap();
}
