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

import java.util.Map;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Namespace;
import com.github.jlangch.venice.impl.Namespaces;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.javainterop.JavaImports;
import com.github.jlangch.venice.impl.specialforms.util.SpecialFormsContext;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncSpecialForm;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions.FnType;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;


/**
 * The special form pesudo functions
 */
public class SpecialForms_ImportFunctions {

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
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns =
			new SymbolMapBuilder()
					.add(import_)
					.add(imports_)
					.toMap();
}
