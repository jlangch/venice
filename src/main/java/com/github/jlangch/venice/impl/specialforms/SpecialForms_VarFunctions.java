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

import static com.github.jlangch.venice.impl.specialforms.util.SpecialFormsUtil.specialFormCallValidation;
import static com.github.jlangch.venice.impl.types.Constants.Nil;
import static com.github.jlangch.venice.impl.util.ArityExceptions.assertArity;

import java.util.Map;

import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.namespaces.Namespaces;
import com.github.jlangch.venice.impl.specialforms.util.SpecialFormsContext;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncSpecialForm;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions.FnType;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;


/**
 * The special form pseudo functions
 *
 * Special forms have evaluation rules that differ from standard Venice
 * evaluation rules and are understood directly by the Venice interpreter.
 */
public class SpecialForms_VarFunctions {

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
            @Override
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
            @Override
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
            @Override
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
            @Override
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
            @Override
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
            @Override
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
    // types_ns is namespace of type functions
    ///////////////////////////////////////////////////////////////////////////

    public static Map<VncVal, VncVal> ns =
            new SymbolMapBuilder()
                    .add(var_get)
                    .add(var_globalQ)
                    .add(var_localQ)
                    .add(var_name)
                    .add(var_ns)
                    .add(var_thread_localQ)
                    .toMap();
}
