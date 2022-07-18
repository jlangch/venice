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
import static com.github.jlangch.venice.impl.specialforms.util.SpecialFormsUtil.evaluateBody;
import static com.github.jlangch.venice.impl.specialforms.util.SpecialFormsUtil.specialFormCallValidation;
import static com.github.jlangch.venice.impl.types.Constants.Nil;
import static com.github.jlangch.venice.impl.util.ArityExceptions.assertArity;
import static com.github.jlangch.venice.impl.util.ArityExceptions.assertMinArity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Destructuring;
import com.github.jlangch.venice.impl.IFormEvaluator;
import com.github.jlangch.venice.impl.ISequenceValuesEvaluator;
import com.github.jlangch.venice.impl.InterruptChecker;
import com.github.jlangch.venice.impl.Modules;
import com.github.jlangch.venice.impl.debug.agent.DebugAgent;
import com.github.jlangch.venice.impl.debug.breakpoint.BreakpointFnRef;
import com.github.jlangch.venice.impl.docgen.runtime.DocForm;
import com.github.jlangch.venice.impl.env.DynamicVar;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.namespaces.Namespace;
import com.github.jlangch.venice.impl.namespaces.Namespaces;
import com.github.jlangch.venice.impl.specialforms.util.Benchmark;
import com.github.jlangch.venice.impl.specialforms.util.SpecialFormsContext;
import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJust;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncSpecialForm;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions.FnType;
import com.github.jlangch.venice.impl.util.CallFrame;
import com.github.jlangch.venice.impl.util.CallStack;
import com.github.jlangch.venice.impl.util.Inspector;
import com.github.jlangch.venice.impl.util.MeterRegistry;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;
import com.github.jlangch.venice.impl.util.WithCallStack;


/**
 * The special form pseudo functions
 *
 * Special forms have evaluation rules that differ from standard Venice
 * evaluation rules and are understood directly by the Venice interpreter.
 */
public class SpecialForms_OtherFunctions {

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
                    .seeAlso("ns-list", "modules")
                    .build()
        ) {
            @Override
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
    // Utility functions
    ///////////////////////////////////////////////////////////////////////////

    public static VncSpecialForm macroexpand_on_loadQ =
        new VncSpecialForm(
                "macroexpand-on-load?",
                VncSpecialForm
                    .meta()
                    .arglists("(macroexpand-on-load?)")
                    .doc(
                        "Returns true if `macroexpand-on-load` feature is enabled else false.\n\n" +
                        "The activation of `macroexpand-on-load` (upfront macro expansion) " +
                        "results in 3x to 15x better performance. Upfront macro expansion " +
                        "can be activated through the `!macroexpand` command in the REPL.")
                    .examples(
                        "(macroexpand-on-load?)")
                    .build()
        ) {
            @Override
            public VncVal apply(
                    final VncVal specialFormMeta,
                    final VncList args,
                    final Env env,
                    final SpecialFormsContext ctx
            ) {
                assertMinArity("macroexpand-on-read?", FnType.SpecialForm, args, 0);

                return VncBoolean.of(ctx.getInterpreter().isMacroExpandOnLoad());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncSpecialForm eval =
        new VncSpecialForm(
                "eval",
                VncSpecialForm
                    .meta()
                    .arglists("(eval form)")
                    .doc("Evaluates the form data structure (not text!) and returns the result.")
                    .examples(
                        "(eval '(let [a 10] (+ 3 4 a)))",
                        "(eval (list + 1 2 3))",
                         "(let [s \"(+ 2 x)\" x 10]     \n" +
                         "   (eval (read-string s))))     ")
                    .seeAlso("read-string")
                    .build()
        ) {
            @Override
            public VncVal apply(
                    final VncVal specialFormMeta,
                    final VncList args,
                    final Env env,
                    final SpecialFormsContext ctx
            ) {
                specialFormCallValidation("eval");
                assertMinArity("eval", FnType.SpecialForm, args, 0);

                final Namespace ns = Namespaces.getCurrentNamespace();

                try {
                    final IFormEvaluator formEvaluator = ctx.getEvaluator();
                    final ISequenceValuesEvaluator seqEvaluator = ctx.getSequenceValuesEvaluator();

                    return formEvaluator.evaluate(
                            Coerce.toVncSequence(seqEvaluator.evaluate_sequence_values(args, env)).last(),
                            env,
                            false);
                }
                finally {
                    Namespaces.setCurrentNamespace(ns);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncSpecialForm binding =
        new VncSpecialForm(
                "binding",
                VncSpecialForm
                    .meta()
                    .arglists("(binding [bindings*] exprs*)")
                    .doc("Evaluates the expressions and binds the values to dynamic (thread-local) symbols")
                    .examples(
                        "(do                      \n" +
                        "   (binding [x 100]      \n" +
                        "      (println x)        \n" +
                        "      (binding [x 200]   \n" +
                        "         (println x))    \n" +
                        "      (println x)))        ",
                        ";; binding-introduced bindings are thread-locally mutable: \n" +
                        "(binding [x 1]                                             \n" +
                        "  (set! x 2)                                               \n" +
                        "  x)                                                         ",
                        ";; binding can use qualified names : \n" +
                        "(binding [user/x 1]                  \n" +
                        "  user/x)                              ")
                    .seeAlso("def-dynamic", "let")
                    .build()
        ) {
            @Override
            public VncVal apply(
                    final VncVal specialFormMeta,
                    final VncList args,
                    final Env env,
                    final SpecialFormsContext ctx
            ) {
                final Env bind_env = new Env(env);

                final VncSequence bindings = Coerce.toVncSequence(args.first());
                final VncList expressions = args.rest();

                if (bindings.size() % 2 != 0) {
                    try (WithCallStack cs = new WithCallStack(new CallFrame("bindings", args, specialFormMeta))) {
                        throw new VncException(
                                "bindings requires an even number of forms in the "
                                + "binding vector!");
                    }
                }

                final IFormEvaluator formEvaluator = ctx.getEvaluator();
                final ISequenceValuesEvaluator seqEvaluator = ctx.getSequenceValuesEvaluator();

                final List<Var> bindingVars = new ArrayList<>();
                try {
                    for(int i=0; i<bindings.size(); i+=2) {
                        final VncVal sym = bindings.nth(i);
                        final VncVal val = formEvaluator.evaluate(bindings.nth(i+1), bind_env, false);

                        if (sym instanceof VncSymbol) {
                            // optimization if not destructuring
                            bind_env.pushGlobalDynamic((VncSymbol)sym, val);
                            bindingVars.add(new Var((VncSymbol)sym, val));
                        }
                        else {
                            final List<Var> vars = Destructuring.destructure(sym, val);
                            vars.forEach(v -> bind_env.pushGlobalDynamic(v.getName(), v.getVal()));
                            bindingVars.addAll(vars);
                        }
                    }

                    final ThreadContext threadCtx = ThreadContext.get();
                    final DebugAgent debugAgent = threadCtx.getDebugAgent_();

                    if (debugAgent != null && debugAgent.hasBreakpointFor(BreakpointFnRef.BINDINGS)) {
                        final CallStack callStack = threadCtx.getCallStack_();
                        debugAgent.onBreakSpecialForm(
                                "bindings", FunctionEntry, bindingVars, specialFormMeta, bind_env, callStack);
                    }

                    seqEvaluator.evaluate_sequence_values(expressions.butlast(), bind_env);
                    return formEvaluator.evaluate(expressions.last(), bind_env, false);
                }
                finally {
                    bindingVars.forEach(v -> bind_env.popGlobalDynamic(v.getName()));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

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
            @Override
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
            @Override
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
                    .doc("Lists the available Venice modules")
                    .seeAlso("doc", "ns-list")
                    .build()
        ) {
            @Override
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
            @Override
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
            @Override
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
            @Override
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
            @Override
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
                        "`quote` or with `'`. They prevent the quoted form from being " +
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
            @Override
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

            @Override
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
            @Override
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

            @Override
            public boolean addCallFrame() {
                return false;
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
                    .arglists(
                        "(dobench iterations expr)",
                        "(dobench warm-up-iterations gc-runs iterations expr)")
                    .doc(
                        "Runs the expr count times in the most effective way and returns a list of " +
                        "elapsed nanoseconds for each invocation. It's main purpose is supporting " +
                        "benchmark tests.\n\n" +
                        "*Note:* For best performance enable `macroexpand-on-load`!")
                    .examples(
                         "(dobench 100 (+ 1 1))",
                         "(dobench 1000 2 100 (+ 1 1))")
                    .build()
        ) {
            @Override
            public VncVal apply(
                    final VncVal specialFormMeta,
                    final VncList args,
                    final Env env,
                    final SpecialFormsContext ctx
            ) {
                specialFormCallValidation("dobench");
                assertArity("dobench", FnType.SpecialForm, args, 2, 4, 5);

                if (args.size() == 2) {
                    return dobench.apply(
                            specialFormMeta,
                            VncList.of(
                                new VncLong(0),
                                new VncLong(0),
                                args.first(),
                                args.second(),
                                nilStatusFn),
                            env,
                            ctx);
                }
                else if (args.size() == 4) {
                    return dobench.apply(
                            specialFormMeta,
                            VncList.of(
                                args.first(),
                                args.second(),
                                args.third(),
                                args.fourth(),
                                nilStatusFn),
                            env,
                            ctx);
                }
                else {
                    final long warmUpIterations = Coerce.toVncLong(args.first()).getValue();
                    final long gcRuns = Coerce.toVncLong(args.second()).getValue();
                    final long iterations = Coerce.toVncLong(args.third()).getValue();
                    final VncVal expr = args.fourth();
                    final VncFunction statusFn = Coerce.toVncFunction(args.nth(4));

                    return Benchmark.benchmark(
                                warmUpIterations,
                                gcRuns,
                                iterations,
                                expr,
                                statusFn,
                                env,
                                ctx.getEvaluator());
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
                        "For best performance enable `macroexpand-on-load`! The expression is evaluated " +
                        "for every run. Alternatively a zero or one arg function referenced by a symbol " +
                        "can be passed:\n\n" +
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
            @Override
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
            @Override
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

    private static VncFunction nilStatusFn =
            new VncFunction("nil-status-fn") {
                @Override
                public VncVal apply(final VncList args) { return Nil; }
                private static final long serialVersionUID = -1L;
            };



    ///////////////////////////////////////////////////////////////////////////
    // types_ns is namespace of type functions
    ///////////////////////////////////////////////////////////////////////////

    public static Map<VncVal, VncVal> ns =
            new SymbolMapBuilder()
                    .add(binding)
                    .add(boundQ)
                    .add(dobench)
                    .add(doc)
                    .add(dorun)
                    .add(macroexpand_on_loadQ)
                    .add(eval)
                    .add(inspect)
                    .add(locking)
                    .add(modules)
                    .add(print_highlight)
                    .add(prof)
                    .add(quote)
                    .add(quasiquote)
                    .add(resolve)
                    .add(setBANG)
                    .toMap();
}
