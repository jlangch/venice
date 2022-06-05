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

import static com.github.jlangch.venice.impl.specialforms.util.SpecialFormsUtil.evaluateSymbolMetaData;
import static com.github.jlangch.venice.impl.specialforms.util.SpecialFormsUtil.validateSymbolWithCurrNS;
import static com.github.jlangch.venice.impl.types.VncBoolean.True;
import static com.github.jlangch.venice.impl.util.ArityExceptions.assertArity;
import static com.github.jlangch.venice.impl.util.ArityExceptions.assertMinArity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.FunctionBuilder;
import com.github.jlangch.venice.impl.Namespaces;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.specialforms.util.SpecialFormsContext;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncMultiArityFunction;
import com.github.jlangch.venice.impl.types.VncSpecialForm;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions.FnType;
import com.github.jlangch.venice.impl.util.MetaUtil;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;


/**
 * The special form pesudo functions
 */
public class SpecialForms_DefFunctions {

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

	public static VncSpecialForm defmacro =
		new VncSpecialForm(
				"defmacro",
				VncSpecialForm
					.meta()
					.arglists("(defmacro name [params*] body)")
					.doc("Macro definition")
					.examples(
						"(defmacro unless [pred a b]   \n" + 
						"  `(if (not ~pred) ~a ~b))      ")
					.seeAlso("macroexpand", "macroexpand-all")
					.build()
		) {
			public VncVal apply(
					final VncVal specialFormMeta,
					final VncList args, 
					final Env env, 
					final SpecialFormsContext ctx
			) {
				assertMinArity("defmacro", FnType.SpecialForm, args, 2);

				final FunctionBuilder functionBuilder = ctx.getFunctionBuilder();

				int argPos = 0;
				
				final VncSymbol macroName = Namespaces.qualifySymbolWithCurrNS(
												evaluateSymbolMetaData(args.nth(argPos++), env, ctx));
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
					final VncFunction macroFn = functionBuilder.buildFunction(
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
						
						fns.add(functionBuilder.buildFunction(
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

			private static final long serialVersionUID = -1848883965231344442L;
		};
		
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns =
			new SymbolMapBuilder()
					.add(def)
					.add(defonce)
					.add(def_dynamic)
					.add(defmacro)
					.toMap();
}
