/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2023 Venice
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

import com.github.jlangch.venice.impl.env.DynamicVar;
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
                    .doc(
                        "Returns a var's value.\n\n" +
                        "The var must exist (bound with a value) otherwise nil is returned.")
                    .examples(
                        "(var-get +)",
                        "(var-get '+)",
                        "(var-get (symbol \"+\"))",
                        "((var-get +) 1 2)",
                        "(do \n" +
                        "  (def x 10) \n" +
                        "  (var-get 'x))")
                    .seeAlso(
                        "var-sym", "var-name", "var-ns", "var-val-meta", "var-local?", "var-global?", "var-thread-local?")
                    .build()
        ) {
            @Override
            public VncVal apply(
                    final VncVal specialFormMeta,
                    final VncList args,
                    final Env env,
                    final SpecialFormsContext ctx
            ) {
                specialFormCallValidation(ctx, "var-get");
                assertArity("var-get", FnType.SpecialForm, args, 1);

                final VncSymbol sym = Types.isVncSymbol(args.first())
                                        ? (VncSymbol)args.first()
                                        : Coerce.toVncSymbol(
                                                ctx.getEvaluator().evaluate(args.first(), env,  false));

                final Var v = env.getVar(sym);
                return v == null ? Nil : v.getVal();
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncSpecialForm var_sym =
        new VncSpecialForm(
                "var-sym",
                VncSpecialForm
                    .meta()
                    .arglists("(var-sym v)")
                    .doc(
                        "Returns the var's symbol.\n\n" +
                        "The var must exist (bound with a value) otherwise nil is returned.")
                    .examples(
                        "(var-sym +)",
                        "(var-sym '+)",
                        "(var-sym (symbol \"+\"))",
                        "(do                 \n" +
                        "  (ns test)         \n" +
                        "  (defn x [] nil)   \n" +
                        "  (var-sym x))",
                        "(let [x 100] (var-sym x))",
                        "(binding [x 100] (var-sym x))",
                        "(do                          \n" +
                        "  (defn foo [x] (var-sym x)) \n" +
                        "  (foo nil))")
                    .seeAlso(
                        "var-get", "var-name", "var-ns", "var-sym-meta", "var-local?", "var-global?", "var-thread-local?")
                    .build()
        ) {
            @Override
            public VncVal apply(
                    final VncVal specialFormMeta,
                    final VncList args,
                    final Env env,
                    final SpecialFormsContext ctx
            ) {
                specialFormCallValidation(ctx, "var-sym");
                assertArity("var-sym", FnType.SpecialForm, args, 1);

                final VncSymbol sym = Types.isVncSymbol(args.first())
                                        ? (VncSymbol)args.first()
                                        : Coerce.toVncSymbol(
                                                ctx.getEvaluator().evaluate(args.first(), env, false));

                final Var v = env.getVar(sym);
                if (v == null) {
                    return Nil;
                }
                else {
                	final VncSymbol s = v.getName();
                    return s.hasNamespace()
                                ? s
                                : v.isGlobal() && !(v instanceof DynamicVar)
                                        ? s.withNamespace(Namespaces.NS_CORE.getName())
                                        : s;
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
                    .doc(
                        "Returns the unqualified name of the var's symbol.\n\n" +
                        "The var must exist (bound with a value) otherwise nil is returned.")
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
                        "name", "var-get", "var-sym", "var-ns", "var-sym-meta", "var-local?", "var-global?", "var-thread-local?")
                    .build()
        ) {
            @Override
            public VncVal apply(
                    final VncVal specialFormMeta,
                    final VncList args,
                    final Env env,
                    final SpecialFormsContext ctx
            ) {
                specialFormCallValidation(ctx, "var-name");
                assertArity("var-name", FnType.SpecialForm, args, 1);

                final VncSymbol sym = Types.isVncSymbol(args.first())
                                        ? (VncSymbol)args.first()
                                        : Coerce.toVncSymbol(
                                                ctx.getEvaluator().evaluate(args.first(), env, false));

                final Var v = env.getVar(sym);
                return v == null ? Nil : new VncString(v.getName().getSimpleName());
           }

            private static final long serialVersionUID = -1848883965231344442L;

        };

    public static VncSpecialForm var_ns =
        new VncSpecialForm(
                "var-ns",
                VncSpecialForm
                    .meta()
                    .arglists("(var-ns v)")
                    .doc(
                        "Returns the namespace of the var's symbol.\n\n" +
                        "The var must exist (bound with a value) otherwise nil is returned.")
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
                specialFormCallValidation(ctx, "var-ns");
                assertArity("var-ns", FnType.SpecialForm, args, 1);

                final VncSymbol sym = Types.isVncSymbol(args.first())
                                        ? (VncSymbol)args.first()
                                        : Coerce.toVncSymbol(
                                                ctx.getEvaluator().evaluate(args.first(), env, false));

                final Var v = env.getVar(sym);
                if (v == null) {
                    return Nil;
                }
                else {
                    return v.getName().hasNamespace()
                                ? new VncString(v.getName().getNamespace())
                                : v.isGlobal()
                                        ? new VncString(Namespaces.NS_CORE.getName())
                                        : Nil;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncSpecialForm var_sym_meta =
        new VncSpecialForm(
                "var-sym-meta",
                VncSpecialForm
                    .meta()
                    .arglists("(var-sym-meta v)")
                    .doc(
                       "Returns the var's symbol meta data.\n\n" +
                       "The var must exist (bound with a value) otherwise nil is returned.")
                    .examples(
                        "(do                           \n" +
                        "  (def ^{:foo 3} x 100)       \n" +
                        "  (:foo (var-sym-meta 'x)))   ",
                        "(do                           \n" +
                        "  (let [^{:foo 3} x 100]      \n" +
                        "    (:foo (var-sym-meta 'x))))",
                        "(do                           \n" +
                        "  (defn bar [^{:foo 3} x]     \n" +
                        "    (:foo (var-sym-meta 'x))) \n" +
                        "  (bar 100))                  ")
                    .seeAlso(
                        "var-val-meta", "var-get", "var-sym", "var-name", "bound?")
                    .build()
        ) {
            @Override
            public VncVal apply(
                    final VncVal specialFormMeta,
                    final VncList args,
                    final Env env,
                    final SpecialFormsContext ctx
            ) {
                specialFormCallValidation(ctx, "var-sym-meta");
                assertArity("var-sym-meta", FnType.SpecialForm, args, 1);

                final VncSymbol sym = Types.isVncSymbol(args.first())
                                        ? (VncSymbol)args.first()
                                        : Coerce.toVncSymbol(
                                                ctx.getEvaluator().evaluate(args.first(), env, false));
                final Var v = env.getVar(sym);
                return v == null ? Nil : v.getName().getMeta();
            }

            private static final long serialVersionUID = -1848883965231344442L;

        };

    public static VncSpecialForm var_val_meta =
        new VncSpecialForm(
                "var-val-meta",
                VncSpecialForm
                    .meta()
                    .arglists("(var-val-meta v)")
                    .doc(
                        "Returns the var's value meta data.\n\n" +
                        "The var must exist (bound with a value) otherwise nil is returned.")
                    .examples(
                        "(do                                    \n" +
                        "  (def x ^{:foo 4} 100)                \n" +
                        "  (:foo (var-val-meta 'x)))            ",
                        "(do                                    \n" +
                        "  (def x (vary-meta 100 assoc :foo 4)) \n" +
                        "  (:foo (var-val-meta 'x)))            ",
                        "(do                                    \n" +
                        "  (let [x ^{:foo 4} 100]               \n" +
                        "    (:foo (var-val-meta 'x))))         ",
                        "(do                                    \n" +
                        "  (defn bar [x]                        \n" +
                        "    (:foo (var-val-meta 'x)))          \n" +
                        "  (bar (vary-meta 100 assoc :foo 4)))")
                   .seeAlso(
                        "var-sym-meta", "var-get", "var-sym", "var-name", "bound?")
                    .build()
        ) {
            @Override
            public VncVal apply(
                    final VncVal specialFormMeta,
                    final VncList args,
                    final Env env,
                    final SpecialFormsContext ctx
            ) {
                specialFormCallValidation(ctx, "var-val-meta");
                assertArity("var-val-meta", FnType.SpecialForm, args, 1);

                final VncSymbol sym = Types.isVncSymbol(args.first())
                                        ? (VncSymbol)args.first()
                                        : Coerce.toVncSymbol(
                                                ctx.getEvaluator().evaluate(args.first(), env, false));

                final Var v = env.getVar(sym);
                return v == null ? Nil : v.getVal().getMeta();
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
                        "(let [x 10]       \n" +
                        "  (var-local? x))   ",
                        "(do               \n" +
                        "  (def x 10)      \n" +
                        "  (var-local? x))   ")
                    .seeAlso(
                        "var-get", "var-name", "var-ns", "var-global?", "var-thread-local?", "bound?")
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
                        "  (var-thread-local? x))")
                    .seeAlso(
                        "var-get", "var-name", "var-ns", "var-local?", "var-global?", "bound?")
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
                        "var-get", "var-name", "var-ns", "var-local?", "var-thread-local?", "bound?")
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

    public static final Map<VncVal, VncVal> ns =
            new SymbolMapBuilder()
                    .add(var_get)
                    .add(var_sym)
                    .add(var_name)
                    .add(var_sym_meta)
                    .add(var_val_meta)
                    .add(var_ns)
                    .add(var_globalQ)
                    .add(var_localQ)
                    .add(var_thread_localQ)
                    .toMap();
}
