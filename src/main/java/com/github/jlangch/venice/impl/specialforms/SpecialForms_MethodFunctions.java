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
import static com.github.jlangch.venice.impl.util.ArityExceptions.assertArity;
import static com.github.jlangch.venice.impl.util.ArityExceptions.assertMinArity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.FunctionBuilder;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.namespaces.Namespaces;
import com.github.jlangch.venice.impl.specialforms.util.SpecialFormsContext;
import com.github.jlangch.venice.impl.types.IVncFunction;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncMultiArityFunction;
import com.github.jlangch.venice.impl.types.VncMultiFunction;
import com.github.jlangch.venice.impl.types.VncSpecialForm;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.collections.VncVector;
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
public class SpecialForms_MethodFunctions {

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
            @Override
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
            @Override
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
            @Override
            public VncVal apply(
                    final VncVal specialFormMeta,
                    final VncList args,
                    final Env env,
                    final SpecialFormsContext ctx
            ) {
                // single arity:  (fn name? [params*] condition-map? expr*)
                // multi arity:   (fn name? ([params*] condition-map? expr*)+ )

                assertMinArity("fn", FnType.SpecialForm, args, 1);

                final FunctionBuilder functionBuilder = ctx.getFunctionBuilder();

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
    // helpers
    ///////////////////////////////////////////////////////////////////////////

    private static VncVector getFnPreconditions(final VncVal prePostConditions) {
        if (Types.isVncMap(prePostConditions)) {
            final VncVal val = ((VncMap)prePostConditions).get(PRE_CONDITION_KEY);
            if (Types.isVncVector(val)) {
                return (VncVector)val;
            }
        }

        return null;
    }


    private static final VncKeyword PRE_CONDITION_KEY = new VncKeyword(":pre");




    ///////////////////////////////////////////////////////////////////////////
    // types_ns is namespace of type functions
    ///////////////////////////////////////////////////////////////////////////

    public static Map<VncVal, VncVal> ns =
            new SymbolMapBuilder()
                    .add(defmethod)
                    .add(defmulti)
                    .add(fn)
                    .toMap();
}
