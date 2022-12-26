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

import static com.github.jlangch.venice.impl.specialforms.util.SpecialFormsUtil.evaluateSymbolMetaData;
import static com.github.jlangch.venice.impl.types.Constants.Nil;
import static com.github.jlangch.venice.impl.util.ArityExceptions.assertArity;
import static com.github.jlangch.venice.impl.util.ArityExceptions.assertMinArity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Destructuring;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.env.GenSym;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.namespaces.Namespace;
import com.github.jlangch.venice.impl.namespaces.Namespaces;
import com.github.jlangch.venice.impl.specialforms.util.DefTypeForm;
import com.github.jlangch.venice.impl.specialforms.util.SpecialFormsContext;
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
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.custom.VncCustomBaseTypeDef;
import com.github.jlangch.venice.impl.types.custom.VncProtocol;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions.FnType;
import com.github.jlangch.venice.impl.util.callstack.CallFrame;
import com.github.jlangch.venice.impl.util.callstack.WithCallStack;
import com.github.jlangch.venice.impl.util.MetaUtil;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;


/**
 * The special form pseudo functions
 *
 * Special forms have evaluation rules that differ from standard Venice
 * evaluation rules and are understood directly by the Venice interpreter.
 */
public class SpecialForms_TypeFunctions {

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
                @Override
                public VncVal apply(
                        final VncVal specialFormMeta,
                        final VncList args,
                        final Env env,
                        final SpecialFormsContext ctx
                ) {
                    // (defprotocol P
                    //    (foo [x])
                    //    (bar [x] [x y])
                    //    (zoo [x] "default val")
                    //    (yar [x] [x y] "default val"))

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

                @Override
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
            @Override
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
            @Override
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
            @Override
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

                if (validationFn != null) validationFn.sandboxFunctionCallValidation();

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
            @Override
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
                        "  (deftype :complex [real :long, imaginary :long])            \n" +
                        "  (deftype-describe :complex))                                \n",
                        "(do                                                           \n" +
                        "  (ns foo)                                                    \n" +
                        "  (deftype-of :port :long)                                    \n" +
                        "  (deftype-describe :port))                                   \n",
                        "(do                                                           \n" +
                        "  (ns foo)                                                    \n" +
                        "  (deftype-or :digit 0 1 2 3 4 5 6 7 8 9)                     \n" +
                        "  (deftype-describe :digit))                                    ")
                    .seeAlso(
                        "deftype", "deftype?", "deftype-or", "deftype-of", ".:")
                    .build()
        ) {
            @Override
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
            @Override
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
                @Override
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

                    if (validationFn != null) validationFn.sandboxFunctionCallValidation();

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
                @Override
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
    // helpers
    ///////////////////////////////////////////////////////////////////////////

    private static void validateDefProtocol(final VncList args) {
        // (defprotocol P
        //    (foo [x])
        //    (bar [x] [x y])
        //    (goo [x] "default val")
        //    (dar [x] [x y] "default val"))

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
                            @Override
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



    ///////////////////////////////////////////////////////////////////////////
    // types_ns is namespace of type functions
    ///////////////////////////////////////////////////////////////////////////

    public static final Map<VncVal, VncVal> ns =
            new SymbolMapBuilder()
                    .add(defprotocol)
                    .add(deftype)
                    .add(deftype_describe)
                    .add(deftype_new)
                    .add(deftype_of)
                    .add(deftype_or)
                    .add(deftypeQ)
                    .add(extend_)
                    .add(extendsQ_)
                    .toMap();
}
