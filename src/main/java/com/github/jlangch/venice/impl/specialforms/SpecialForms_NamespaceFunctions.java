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

import static com.github.jlangch.venice.impl.specialforms.SpecialFormsUtil.specialFormCallValidation;
import static com.github.jlangch.venice.impl.types.Constants.Nil;
import static com.github.jlangch.venice.impl.util.ArityExceptions.assertArity;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Namespaces;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.functions.CoreFunctions;
import com.github.jlangch.venice.impl.types.VncSpecialForm;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions.FnType;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;


/**
 * The special form pesudo functions
 */
public class SpecialForms_NamespaceFunctions {

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
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns =
			new SymbolMapBuilder()
					.add(ns_new)
					.add(ns_list)
					.add(ns_remove)
					.add(ns_unmap)
					.toMap();
}
