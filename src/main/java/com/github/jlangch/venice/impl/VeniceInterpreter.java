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
package com.github.jlangch.venice.impl;

import static com.github.jlangch.venice.impl.debug.breakpoint.FunctionScope.FunctionEntry;
import static com.github.jlangch.venice.impl.types.Constants.Nil;
import static com.github.jlangch.venice.impl.types.VncFunction.createAnonymousFuncName;
import static com.github.jlangch.venice.impl.util.ArityExceptions.assertArity;
import static com.github.jlangch.venice.impl.util.ArityExceptions.assertMinArity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.jlangch.venice.NotInTailPositionException;
import com.github.jlangch.venice.SecurityException;
import com.github.jlangch.venice.Version;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.debug.agent.DebugAgent;
import com.github.jlangch.venice.impl.debug.breakpoint.BreakpointFnRef;
import com.github.jlangch.venice.impl.env.ComputedVar;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.functions.CoreFunctions;
import com.github.jlangch.venice.impl.functions.Functions;
import com.github.jlangch.venice.impl.functions.TransducerFunctions;
import com.github.jlangch.venice.impl.modules.Modules;
import com.github.jlangch.venice.impl.namespaces.Namespace;
import com.github.jlangch.venice.impl.namespaces.NamespaceRegistry;
import com.github.jlangch.venice.impl.namespaces.Namespaces;
import com.github.jlangch.venice.impl.reader.Reader;
import com.github.jlangch.venice.impl.specialforms.SpecialForms_LoadCodeMacros;
import com.github.jlangch.venice.impl.specialforms.SpecialForms_OtherFunctions;
import com.github.jlangch.venice.impl.specialforms.util.SpecialFormsContext;
import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.types.IVncFunction;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncMultiArityFunction;
import com.github.jlangch.venice.impl.types.VncScalar;
import com.github.jlangch.venice.impl.types.VncSpecialForm;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncLazySeq;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncMapEntry;
import com.github.jlangch.venice.impl.types.collections.VncMutableSet;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.collections.VncSet;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions.FnType;
import com.github.jlangch.venice.impl.util.CallFrame;
import com.github.jlangch.venice.impl.util.CallFrameFnData;
import com.github.jlangch.venice.impl.util.CallStack;
import com.github.jlangch.venice.impl.util.CollectionUtil;
import com.github.jlangch.venice.impl.util.MeterRegistry;
import com.github.jlangch.venice.impl.util.WithCallStack;
import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;
import com.github.jlangch.venice.javainterop.IInterceptor;

/**
 * The Venice interpreter runs the scripts and handles the special forms
 *
 *
 * <p>Tail recursion (loop-recur):
 * <pre>
 *   +----------------+-------------------------------------------+---------------+
 *   | Form           | Tail Position                             | recur target? |
 *   +----------------+-------------------------------------------+---------------+
 *   | fn, defn       | (fn [args] expressions tail)              | No            |
 *   | loop           | (loop [bindings] expressions tail)        | Yes           |
 *   | let            | (let [bindings] expressions tail)         | No            |
 *   | do             | (do expressions tail)                     | No            |
 *   | if, if-not     | (if test then tail else tail)             | No            |
 *   | when, when-not | (when test expressions tail)              | No            |
 *   | cond           | (cond test tail ... :else else tail)      | No            |
 *   | case           | (case const tail ... default tail)        | No            |
 *   | or, and        | (or test test ... tail)                   | No            |
 *   +----------------+-------------------------------------------+---------------+
 * </pre>
 */
public class VeniceInterpreter implements IVeniceInterpreter, Serializable  {

    public VeniceInterpreter(
            final IInterceptor interceptor
    ) {
        this(interceptor, null);
    }

    public VeniceInterpreter(
            final IInterceptor interceptor,
            final MeterRegistry meterRegistry
    ) {
        if (interceptor == null) {
            throw new SecurityException("VeniceInterpreter can not run without an interceptor");
        }

        final MeterRegistry mr = meterRegistry == null
                                    ? new MeterRegistry(false)
                                    : meterRegistry;

        this.interceptor = interceptor;
        this.meterRegistry = mr;
        this.nsRegistry = new NamespaceRegistry();
        this.sealedSystemNS = new AtomicBoolean(false);

        // performance optimization
        this.checkSandbox = !(interceptor instanceof AcceptAllInterceptor);
        this.optimized = false;  // no callstack, no auto TCO, meter, checks, ...

        this.functionBuilder = new FunctionBuilder(this::evaluate, this.optimized);

        this.specialFormsContext= new SpecialFormsContext(
                                        this,
                                        this::evaluate,
                                        this::evaluate_values,
                                        this::evaluate_sequence_values,
                                        this.functionBuilder,
                                        this.interceptor,
                                        this.nsRegistry,
                                        this.meterRegistry,
                                        this.sealedSystemNS);

        ThreadContext.setInterceptor(interceptor);
        ThreadContext.setMeterRegistry(mr);

        if (optimized && checkSandbox) {
            // invalid combination: prevent security problems with partially deactivated sandboxes
            throw new VncException("Venice interpreter supports optimized mode only with AcceptAllInterceptor!");
        }
    }


    @Override
    public void initNS() {
        nsRegistry.clear();
        Namespaces.setCurrentNamespace(nsRegistry.computeIfAbsent(Namespaces.NS_USER));
    }

    @Override
    public void presetNS(final NamespaceRegistry nsRegistry) {
        final VncSymbol ns = Namespaces.getCurrentNS();

        if (nsRegistry != null) {
            this.nsRegistry.add(nsRegistry);

            Namespaces.setCurrentNamespace(nsRegistry.computeIfAbsent(ns));
        }
    }

    @Override
    public void sealSystemNS() {
        sealedSystemNS.set(true);
    }

    @Override
    public void setMacroExpandOnLoad(final boolean macroExpandOnLoad) {
        // Dynamically turn on/off macroexpand-on-load. The REPL makes use of this.
        this.macroExpandOnLoad = macroExpandOnLoad;
    }

    @Override
    public boolean isMacroExpandOnLoad() {
        return macroExpandOnLoad;
    }

    @Override
    public boolean isEvaluateDynamicallyLoadedCode() {
        return true;
    }

    @Override
    public VncVal READ(final String script, final String filename) {
        if (meterRegistry.enabled) {
            final long nanos = System.nanoTime();
            final VncVal val = Reader.read_str(script, filename);
            meterRegistry.record("venice.read", System.nanoTime() - nanos);
            return val;
        }
        else {
            return Reader.read_str(script, filename);
        }
    }

    @Override
    public VncVal EVAL(final VncVal ast, final Env env) {
        if (meterRegistry.enabled) {
            final long nanos = System.nanoTime();
            final VncVal val = evaluate(ast, env, false);
            meterRegistry.record("venice.eval", System.nanoTime() - nanos);
            return val;
        }
        else {
            return evaluate(ast, env, false);
        }
    }

    @Override
    public VncVal MACROEXPAND(final VncVal ast, final Env env) {
        return macroexpand_all(
                    new CallFrame("macroexpand-all", ast.getMeta()),
                    ast,
                    env);
    }

    @Override
    public VncVal RE(
            final String script,
            final String name,
            final Env env
    ) {
        VncVal ast = READ(script, name);
        if (macroExpandOnLoad) {
            ast = MACROEXPAND(ast, env);
        }
        return EVAL(ast, env);
    }

    @Override
    public String PRINT(final VncVal exp) {
        return Printer.pr_str(exp, true);
    }

    @Override
    public Env createEnv(
            final boolean macroexpandOnLoad,
            final boolean ansiTerminal,
            final RunMode runMode
    ) {
        return createEnv(null, macroexpandOnLoad, ansiTerminal, runMode);
    }

    @Override
    public Env createEnv(
            final List<String> preloadedExtensionModules,
            final boolean macroExpandOnLoad,
            final boolean ansiTerminal,
            final RunMode runMode
    ) {
        final CodeLoader codeLoader = new CodeLoader();

        sealedSystemNS.set(false);

        final Env env = new Env(null);

        for(Map.Entry<VncVal,VncVal> e: Functions.functions.entrySet()) {
            final VncSymbol sym = (VncSymbol)e.getKey();
            final VncVal val = e.getValue();
            if (val instanceof VncFunction) {
                final VncFunction fn = (VncFunction)e.getValue();
                env.setGlobal(new Var(sym, fn, fn.isRedefinable()));
            }
            else if (val instanceof VncSpecialForm) {
                env.setGlobal(new Var(sym, val, false)); // not redefinable
            }
            else {
                env.setGlobal(new Var(sym, val, true));
            }
        }

        // set Venice version
        env.setGlobal(new Var(new VncSymbol("*version*"), new VncString(Version.VERSION), false));

        // set current namespace
        env.setGlobal(new ComputedVar(new VncSymbol("*ns*"), () -> Namespaces.getCurrentNS(), false));

        // set system newline
        env.setGlobal(new Var(new VncSymbol("*newline*"), new VncString(System.lineSeparator()), false));

        // ansi terminal (set when run from a REPL in an ANSI terminal)
        env.setGlobal(new Var(new VncSymbol("*ansi-term*"), VncBoolean.of(ansiTerminal), false));

        // set the run mode
        env.setGlobal(new Var(new VncSymbol("*run-mode*"), runMode == null ? Nil : runMode.mode, false));

        // command line args (default nil)
        env.setGlobal(new Var(new VncSymbol("*ARGV*"), Nil, true));

        // loaded modules & files
        final VncMutableSet loadedModules = new VncMutableSet();
        env.setGlobal(new Var(new VncSymbol("*loaded-modules*"), loadedModules, true));
        env.setGlobal(new Var(new VncSymbol("*loaded-files*"), new VncMutableSet(), true));

        // init namespaces
        initNS();

        // Activates macroexpand on load
        setMacroExpandOnLoad(macroExpandOnLoad);

        // add all native modules (implicitly preloaded)
        loadedModules.addAll(VncMutableSet.ofAll(Modules.NATIVE_MODULES));

        // load core modules
        codeLoader.loadModule(new VncKeyword("core"), this, null, env, false, null);

        // security: seal system namespaces (Namespaces::SYSTEM_NAMESPACES) - no further changes allowed!
        sealedSystemNS.set(true);

        // load other modules requested for preload
        CollectionUtil.toEmpty(preloadedExtensionModules)
                      .forEach(m -> codeLoader.loadModule(new VncKeyword(m), this, null, env, false, null));

        // set current namespace to 'user' after loading modules
        Namespaces.setCurrentNamespace(nsRegistry.computeIfAbsent(Namespaces.NS_USER));

        return env;
    }

    @Override
    public List<String> getAvailableModules() {
        final List<String> modules = new ArrayList<>(Modules.VALID_MODULES);
        modules.removeAll(Arrays.asList("core", "test", "http", "jackson"));
        Collections.sort(modules);
        return modules;
    }

    @Override
    public IInterceptor getInterceptor() {
        return interceptor;
    }

    @Override
    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }

    @Override
    public NamespaceRegistry getNamespaceRegistry() {
        return nsRegistry;
    }

    private VncVal evaluate(
            final VncVal ast_,
            final Env env_,
            final boolean inTailPosition
    ) {
        VncVal orig_ast = ast_;
        Env env = env_;

        RecursionPoint recursionPoint = null;
        boolean tailPosition = inTailPosition;

        while (true) {
            // System.out.println("EVAL:            " + Types.getType(orig_ast) + " > " + orig_ast.toString(true));

            if (ast_ == Nil) return Nil;

            if (!(orig_ast instanceof VncList)) {
                // not an s-expr
                return evaluate_values(orig_ast, env);
            }

            final VncList ast = (VncList)orig_ast;
            if (ast.isEmpty()) {
                return ast;
            }

            final VncVal a0 = ast.first();
            final VncVal a0meta = a0.getMeta();
            final String a0sym = (a0 instanceof VncSymbol) ? ((VncSymbol)a0).getName() : "__<*fn*>__";
            final VncList args = ast.rest();

            // special form / function dispatcher
            switch (a0sym) {
                case "do": { // (do expr*)
                        final VncList expressions = args;
                        evaluate_sequence_values(expressions.butlast(), env);
                        orig_ast = expressions.last();
                        tailPosition = true;
                    }
                    break;

                case "if": { // (if cond expr-true expr-false*)
                        final int numArgs = args.size();
                        if (numArgs == 2 || numArgs == 3) {
                            final VncVal cond = evaluate(args.first(), env, false);

                            if (!optimized) {
                                final ThreadContext threadCtx = ThreadContext.get();
                                final DebugAgent debugAgent = threadCtx.getDebugAgent_();

                                if (debugAgent != null && debugAgent.hasBreakpointFor(BreakpointFnRef.IF)) {
                                    debugAgent.onBreakSpecialForm(
                                            "if",
                                            FunctionEntry,
                                            VncVector.of(new VncString("cond")),
                                            VncList.of(cond),
                                            a0meta,
                                            env,
                                            threadCtx.getCallStack_());
                                }
                            }

                            orig_ast = VncBoolean.isFalseOrNil(cond)
                                            ? args.third()   // eval false slot form (nil if not available)
                                            : args.second(); // eval true slot form
                            tailPosition = true;
                        }
                        else {
                            // only create callstack when needed!
                            final CallFrame cf = new CallFrame("if", args, a0meta);
                            try (WithCallStack cs = new WithCallStack(cf)) {
                                assertArity("if", FnType.SpecialForm, args, 2, 3);
                            }
                        }
                    }
                    break;

                case "let": { // (let [bindings*] exprs*)
                        if (args.isEmpty()) {
                            // only create callstack when needed!
                            final CallFrame cf = new CallFrame("let", args, a0meta);
                            try (WithCallStack cs = new WithCallStack(cf)) {
                                assertMinArity("let", FnType.SpecialForm, args, 1);
                            }
                        }
                        env = new Env(env);  // let introduces a new environment

                        final ThreadContext threadCtx = ThreadContext.get();
                        final DebugAgent debugAgent = threadCtx.getDebugAgent_();

                        final VncVector bindings = Coerce.toVncVector(args.first());
                        final VncList expressions = args.rest();

                        if (bindings.size() % 2 != 0) {
                            final CallFrame cf = new CallFrame("let", args, a0meta);
                            try (WithCallStack cs = new WithCallStack(cf)) {
                                throw new VncException("let requires an even number of forms in the binding vector!");
                            }
                        }

                        final Iterator<VncVal> bindingsIter = bindings.iterator();
                        final List<Var> vars = new ArrayList<>();
                        while(bindingsIter.hasNext()) {
                            final VncVal sym = bindingsIter.next();
                            final boolean symIsSymbol = sym instanceof VncSymbol;

                            if (symIsSymbol && ((VncSymbol)sym).hasNamespace()) {
                                final VncSymbol s = (VncSymbol)sym;
                                final CallFrame cf = new CallFrame(s.getQualifiedName(), args, s.getMeta());
                                try (WithCallStack cs = new WithCallStack(cf)) {
                                    throw new VncException("Can't use qualified symbols with let!");
                                }
                            }

                            final VncVal val = evaluate(bindingsIter.next(), env, false);
                            if (symIsSymbol) {
                                // optimized with plain symbol when destructuring is not used
                                env.setLocal(new Var((VncSymbol)sym, val));
                                if (debugAgent != null) {
                                    vars.add(new Var((VncSymbol)sym, val));
                                }
                            }
                            else {
                                final List<Var> varTmp = Destructuring.destructure(sym, val);
                                env.addLocalVars(varTmp);

                                if (debugAgent != null) {
                                    vars.addAll(varTmp);
                                }
                            }
                        }

                        if (debugAgent != null && debugAgent.hasBreakpointFor(BreakpointFnRef.LET)) {
                            final CallStack callStack = threadCtx.getCallStack_();
                            debugAgent.onBreakSpecialForm(
                                    "let", FunctionEntry, vars, a0meta, env, callStack);
                        }

                        if (expressions.size() == 1) {
                            orig_ast = expressions.first();
                        }
                        else {
                            evaluate_sequence_values(expressions.butlast(), env);
                            orig_ast = expressions.last();
                        }
                        tailPosition = true;
                    }
                    break;

                case "loop": { // (loop [bindings*] exprs*)
                        recursionPoint = null;
                        if (args.size() < 2) {
                            // only create callstack when needed!
                            final CallFrame cf = new CallFrame("loop", args, a0meta);
                            try (WithCallStack cs = new WithCallStack(cf)) {
                                assertMinArity("loop", FnType.SpecialForm, args, 2);
                            }
                        }
                        env = new Env(env);

                        final VncVector bindings = Coerce.toVncVector(args.first());
                        final VncList expressions = args.rest();

                        if (bindings.size() % 2 != 0) {
                            final CallFrame cf = new CallFrame("loop", args, a0meta);
                            try (WithCallStack cs = new WithCallStack(cf)) {
                                throw new VncException("loop requires an even number of forms in the binding vector!");
                            }
                        }

                        final List<VncVal> bindingNames = new ArrayList<>(bindings.size() / 2);
                        final Iterator<VncVal> bindingsIter = bindings.iterator();
                        while(bindingsIter.hasNext()) {
                            final VncVal symVal = bindingsIter.next();
                            final VncVal bindVal = evaluate(bindingsIter.next(), env, false);
                            bindingNames.add(symVal);
                            RecursionPoint.addToLocalEnv(symVal, bindVal, env);
                        }

                        final ThreadContext threadCtx = ThreadContext.get();
                        final DebugAgent debugAgent = threadCtx.getDebugAgent_();

                        recursionPoint = new RecursionPoint(
                                                bindingNames,
                                                expressions,
                                                env,
                                                a0meta,
                                                debugAgent);

                        if (debugAgent != null && debugAgent.hasBreakpointFor(BreakpointFnRef.LOOP)) {
                            final CallStack cs = threadCtx.getCallStack_();
                            debugAgent.onBreakLoop(FunctionEntry, recursionPoint, env, cs);
                        }

                        if (expressions.size() == 1) {
                            orig_ast = expressions.first();
                        }
                        else {
                            evaluate_sequence_values(expressions.butlast(), env);
                            orig_ast = expressions.last();
                        }
                        tailPosition = true;
                    }
                    break;

                case "recur":  { // (recur exprs*)
                        // Note: (recur) is valid, it's used by the while macro
                        if (recursionPoint == null) {
                            final CallFrame cf = new CallFrame("recur", args, a0meta);
                            try (WithCallStack cs = new WithCallStack(cf)) {
                                throw new NotInTailPositionException(
                                        "The recur expression is not in tail position!");
                            }
                        }
                        if (args.size() != recursionPoint.getLoopBindingNamesCount()) {
                            final CallFrame cf = new CallFrame("recur", args, a0meta);
                            try (WithCallStack cs = new WithCallStack(cf)) {
                                throw new VncException(String.format(
                                        "The recur args (%d) do not match the loop args (%d) !",
                                        args.size(),
                                        recursionPoint.getLoopBindingNamesCount()));
                            }
                        }

                        env = buildRecursionEnv(args, env, recursionPoint);

                        // for performance reasons the DebugAgent is stored in the
                        // RecursionPoint. This saves repeated ThreadLocal access!
                        if (recursionPoint.isDebuggingActive()) {
                            final DebugAgent debugAgent = recursionPoint.getDebugAgent();
                            if (debugAgent.hasBreakpointFor(BreakpointFnRef.LOOP)) {
                                final CallStack cs = ThreadContext.getCallStack();
                                debugAgent.onBreakLoop(FunctionEntry, recursionPoint, env, cs);
                            }
                        }

                        final VncList expressions = recursionPoint.getLoopExpressions();
                        if (expressions.size() == 1) {
                            orig_ast = expressions.first();
                        }
                        else {
                            evaluate_sequence_values(expressions.butlast(), env);
                            orig_ast = expressions.last();
                        }
                        tailPosition = true;
                    }
                    break;

                case "quasiquote": // (quasiquote form)
                    orig_ast = SpecialForms_OtherFunctions.quasiquote.apply(a0meta, args, env, specialFormsContext);
                    break;

                case "macroexpand": // (macroexpand form)
                    return macroexpand(args, env, a0meta);

                case "macroexpand-all*":  // (macroexpand-all* form)
                    // Note: This special form is exposed through the public Venice
                    //       function 'core/macroexpand-all' in the 'core' module.
                    return macroexpand_all(
                            new CallFrame("macroexpand-all*", args, a0meta),
                            evaluate(args.first(), env, false),
                            env);

                case "tail-pos":
                    return tail_pos_check_(tailPosition, args, env, a0meta);

                default: { // functions, macros, collections/keywords as functions
                    final VncVal fn0 = a0 instanceof VncSymbol
                                            ? env.get((VncSymbol)a0)
                                            : evaluate(a0, env, false); // ((resolve '+) 1 2)


                    if (fn0 instanceof VncSpecialForm) {
                        final VncSpecialForm sf = (VncSpecialForm)fn0;
                        if (sf.addCallFrame()) {
                            final CallFrame callframe = new CallFrame(sf.getName(), args, a0meta);
                            try (WithCallStack cs = new WithCallStack(callframe)) {
                                return sf.apply(a0meta, args, env, specialFormsContext);
                            }
                        }
                        else {
                            return sf.apply(a0meta, args, env, specialFormsContext);
                        }
                    }
                    else if (fn0 instanceof VncFunction) {
                        final VncFunction fn = (VncFunction)fn0;

                        if (fn.isMacro()) {
                            // macro
                            final VncVal expandedAst = doMacroexpand(ast, env);
                            if (expandedAst instanceof VncList) {
                                orig_ast = expandedAst;
                                continue;
                            }
                            else {
                                return evaluate_values(expandedAst, env); // not an s-expr
                            }
                        }
                        else {
                            final String fnName = fn.getQualifiedName();

                            if (optimized) {
                                // evaluate function args
                                final VncList fnArgs = (VncList)evaluate_sequence_values(args, env);
                                return fn.apply(fnArgs);
                            }
                            else {
                                final ThreadContext threadCtx = ThreadContext.get();
                                final CallStack callStack = threadCtx.getCallStack_();
                                final DebugAgent debugAgent = threadCtx.getDebugAgent_();

                                if (debugAgent != null && debugAgent.hasBreakpointFor(new BreakpointFnRef(fnName))) {
                                    debugAgent.onBreakFnCall(fnName, fn, args, env, callStack);
                                }

                                // evaluate function args
                                final VncList fnArgs = (VncList)evaluate_sequence_values(args, env);

                                final long nanos = meterRegistry.enabled ? System.nanoTime() : 0L;

                                // validate function call allowed by sandbox
                                if (checkSandbox) {
                                    final CallFrame cf = new CallFrame(fnName, fnArgs, a0meta, env);
                                    try (WithCallStack cs = new WithCallStack(cf)) {
                                        interceptor.validateVeniceFunction(fnName);
                                    }
                                    interceptor.validateMaxExecutionTime();
                                }

                                final Thread currThread = Thread.currentThread();

                                InterruptChecker.checkInterrupted(currThread, fn);


                                // Automatic TCO (tail call optimization)
                                if (tailPosition
                                        && !fn.isNative()  // native functions do not have an AST body
                                        && !callStack.isEmpty()
                                        && fnName.equals(callStack.peek().getFnName())
                                ) {
                                    // fn may be a normal function, a multi-arity, or a multi-method function
                                    final VncFunction effFn = fn.getFunctionForArgs(fnArgs);
                                    env.addLocalVars(Destructuring.destructure(effFn.getParams(), fnArgs));

                                    if (debugAgent != null && debugAgent.hasBreakpointFor(new BreakpointFnRef(fnName))) {
                                        debugAgent.onBreakFnEnter(fnName, effFn, fnArgs, env, callStack);
                                    }

                                    final VncList body = (VncList)effFn.getBody();
                                    evaluate_sequence_values(body.butlast(), env);
                                    orig_ast = body.last();
                                }
                                else {
                                    // invoke function with a new call frame
                                    try {
                                        if (fn.isNative()) {
                                            callStack.push(new CallFrame(fnName, fnArgs, a0meta, env));

                                            if (debugAgent != null && debugAgent.hasBreakpointFor(new BreakpointFnRef(fnName))) {
                                                // Debugging handled for native functions only.
                                                env.setLocal(new Var(new VncSymbol("debug::fn-args"), fnArgs));
                                                try {
                                                    debugAgent.onBreakFnEnter(fnName, fn, fnArgs, env, callStack);
                                                    final VncVal retVal = fn.apply(fnArgs);
                                                    debugAgent.onBreakFnExit(fnName, fn, fnArgs, retVal, env, callStack);
                                                    return retVal;
                                                }
                                                catch(Exception ex) {
                                                    debugAgent.onBreakFnException(fnName, fn, fnArgs, ex, env, callStack);
                                                    throw ex;
                                                }
                                            }
                                            else {
                                                return fn.apply(fnArgs);
                                            }
                                        }
                                        else {
                                            // Debugging for non native functions is handled in the
                                            // implementation of VncFunction::apply. See the builder
                                            // FunctionBuilder::buildFunction(..)
                                            threadCtx.setCallFrameFnData_(new CallFrameFnData(fnName, a0meta));
                                            return fn.apply(fnArgs);
                                        }
                                    }
                                    finally {
                                        threadCtx.setCallFrameFnData_(null);
                                        if (fn.isNative()) {
                                            callStack.pop();
                                        }

                                        InterruptChecker.checkInterrupted(currThread, fn);
                                        if (checkSandbox) {
                                            interceptor.validateMaxExecutionTime();
                                        }
                                        if (meterRegistry.enabled) {
                                            final long elapsed = System.nanoTime() - nanos;
                                            if (fn instanceof VncMultiArityFunction) {
                                                final VncFunction f = fn.getFunctionForArgs(fnArgs);
                                                meterRegistry.record(fnName, f.getParams().size(), elapsed);
                                            }
                                            else {
                                                meterRegistry.record(fnName, elapsed);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else if (fn0 instanceof IVncFunction) {
                        // collection/keyword as function
                        final CallFrame cf = new CallFrame(fn0.getType().toString(), args, a0meta, env);
                        try (WithCallStack cs = new WithCallStack(cf)) {
                            final IVncFunction fn = (IVncFunction)fn0;
                            final VncList fnArgs = (VncList)evaluate_sequence_values(args, env);
                            return fn.apply(fnArgs);
                        }
                    }
                    else {
                        final CallFrame cf = new CallFrame("unknown", args, a0meta);
                        try (WithCallStack cs = new WithCallStack(cf)) {
                            throw new VncException(String.format(
                                    "Expected a function or keyword/set/map/vector as "
                                        + "s-expression symbol value but got a value "
                                        + "of type '%s'!",
                                    Types.getType(fn0)));
                        }
                    }
                }
                break;
            }
        }
    }

    private VncVal evaluate_values(final VncVal ast, final Env env) {
        // System.out.println("EVAL VALUES:     " + Types.getType(ast) + " > " + ast.toString(true));

        if (ast == Nil) {
            return Nil;
        }
        else if (ast instanceof VncSymbol) {
            return env.get((VncSymbol)ast);
        }
        else if (ast instanceof VncSequence) {
            return evaluate_sequence_values((VncSequence)ast, env);
        }
        else if (ast instanceof VncScalar) {
            return ast;
        }
        else if (ast instanceof VncJavaObject) {
            return ast;
        }
        else if (ast instanceof VncMap) {
            final VncMap map = (VncMap)ast;

            final Map<VncVal,VncVal> vals = new HashMap<>(map.size());
            for(Entry<VncVal,VncVal> e: map.getJavaMap().entrySet()) {
                vals.put(
                    evaluate(e.getKey(), env, false),
                    evaluate(e.getValue(), env, false));
            }
            return map.withValues(vals);
        }
        else if (ast instanceof VncSet) {
            final VncSet set = (VncSet)ast;

            final List<VncVal> vals = new ArrayList<>(set.size());
            for(VncVal v: set) {
                vals.add(evaluate(v, env, false));
            }
            return set.withValues(vals);
        }
        else {
            return ast;
        }
    }

    private VncSequence evaluate_sequence_values(final VncSequence seq, final Env env) {
        if (seq instanceof VncLazySeq) {
            return seq;
        }
        else {
            // System.out.println("EVAL SEQ VALUES: " + Types.getType(seq) + " > " + seq.toString(true));

            switch(seq.size()) {
                case 0:
                    return seq;

                case 1: {
                    final VncVal v1 = seq.first();
                    return v1 instanceof VncScalar
                            ? seq
                            : seq.withVariadicValues(
                                evaluate(v1, env, false));
                }

                case 2: {
                    final VncVal v1 = seq.first();
                    final VncVal v2 = seq.second();
                    return v1 instanceof VncScalar && v2 instanceof VncScalar
                            ? seq
                            : seq.withVariadicValues(
                                evaluate(v1, env, false),
                                evaluate(v2, env, false));
                }

                case 3: {
                    final VncVal v1 = seq.first();
                    final VncVal v2 = seq.second();
                    final VncVal v3 = seq.third();
                    return v1 instanceof VncScalar && v2 instanceof VncScalar && v3 instanceof VncScalar
                            ? seq
                            : seq.withVariadicValues(
                                evaluate(v1, env, false),
                                evaluate(v2, env, false),
                                evaluate(v3, env, false));
                }

                case 4: {
                    return seq.withVariadicValues(
                                evaluate(seq.first(), env, false),
                                evaluate(seq.second(), env, false),
                                evaluate(seq.third(), env, false),
                                evaluate(seq.fourth(), env, false));
                }

                default:
                    return seq.map(v -> evaluate(v, env, false));
            }
        }
    }

    /**
     * Recursively expands a macro. Inside the loop, the first element
     * of the ast list (a symbol), is looked up in the environment to get
     * the macro function. This macro function is then called/applied with
     * the rest of the ast elements (2nd through the last) as arguments.
     * The return value of the macro call becomes the new value of ast.
     * When the loop completes because ast no longer represents a macro call,
     * the current value of ast is returned.
     *
     * <p>Macro check:
     * An ast is a macro if the ast is a list that contains a symbol as the
     * first element and that symbol refers to a function in the env environment
     * and that function has the macro attribute set to true.
     *
     * @param ast ast
     * @param env env
     * @return the expanded macro
     */
    private VncVal doMacroexpand(
            final VncVal ast,
            final Env env
    ) {
        final long nanos = meterRegistry.enabled ? System.nanoTime() : 0L;

        VncVal ast_ = ast;
        int expandedMacros = 0;

        while (ast_ instanceof VncList) {
            final VncVal a0 = ((VncList)ast_).first();
            if (!(a0 instanceof VncSymbol)) break;

            final VncSymbol a0Sym = (VncSymbol)a0;
            final String a0SymName = a0Sym.getName();
            final VncList macroArgs = ((VncList)ast_).rest();

            if ("load-module".equals(a0SymName)) {
                return SpecialForms_LoadCodeMacros.load_module.apply(a0Sym, macroArgs, env, specialFormsContext);
            }
            else if ("load-file".equals(a0SymName)) {
                return SpecialForms_LoadCodeMacros.load_file.apply(a0Sym, macroArgs, env, specialFormsContext);
            }
            else if ("load-classpath-file".equals(a0SymName)) {
                return SpecialForms_LoadCodeMacros.load_classpath_file.apply(a0Sym, macroArgs, env, specialFormsContext);
            }
            else if ("load-string".equals(a0SymName)) {
                return SpecialForms_LoadCodeMacros.load_string.apply(a0Sym, macroArgs, env, specialFormsContext);
            }

            final VncVal fn = env.getGlobalOrNull(a0Sym);
            if (!(fn != null && fn instanceof VncFunction && ((VncFunction)fn).isMacro())) break;

            final VncFunction macro = (VncFunction)fn;


            // validate that the macro is allowed by the sandbox
            if (checkSandbox) {
                interceptor.validateVeniceFunction(macro.getQualifiedName());
            }

            expandedMacros++;

            final CallFrame cf = new CallFrame(macro.getQualifiedName(), macroArgs, a0.getMeta());
            try (WithCallStack cs = new WithCallStack(cf)) {
                if (meterRegistry.enabled) {
                    final long nanosRun = System.nanoTime();

                    ast_ = macro.apply(macroArgs);

                    meterRegistry.record(macro.getQualifiedName() + "[m]", System.nanoTime() - nanosRun);
                }
                else {
                    ast_ = macro.apply(macroArgs);
                }
            }
        }

        if (expandedMacros > 0) {
            if (meterRegistry.enabled) {
                meterRegistry.record("macroexpand", System.nanoTime() - nanos);
            }
        }

        return ast_;
    }

    private VncVal macroexpand(
            final VncList args,
            final Env env,
            final VncVal meta
    ) {
        final CallFrame callframe = new CallFrame("macroexpand", args, meta);
        try (WithCallStack cs = new WithCallStack(callframe)) {
            assertArity("macroexpand", FnType.SpecialForm, args, 1);
            final VncVal ast = evaluate(args.first(), env, false);
            return doMacroexpand(ast, env);
        }
    }

    /**
     * Expands recursively all macros in the form.
     *
     * <p>An approach with <code>core/prewalk</code> does not work, because
     * this function does not apply namespaces definitions (ns x).
     * Remember that macros are always executed in the namespace of the caller
     * as opposed to functions that are executed in the namespace they are
     * defined in.
     *
     * <p>With <code>core/prewalk</code> we cannot execute <code>(ns x)</code>
     * because the functions involved like <code>core/walk</code> and
     * <code>core/partial</code> will reset the changed namespace upon leaving
     * its body.
     *
     * <pre>
     *     (core/prewalk (fn [x] (if (list? x) (macroexpand x) x)) form)
     * </pre>
     *
     * <p>Note: only macros that have already been parsed in another parse unit
     *          can be expanded!
     *          'macroexpand-all' is not an interpreter thus it cannot not
     *          run macro definitions and put them to the symbol table for later
     *          reference!
     *
     * @param callframe a callframe
     * @param form the form to expand
     * @param env the env
     * @return the expanded form
     */
    @Override
    public VncVal macroexpand_all(
            final CallFrame callframe,
            final VncVal form,
            final Env env
    ) {
        try (WithCallStack cs = new WithCallStack(callframe)) {
            final VncFunction handler = new VncFunction(createAnonymousFuncName("macroexpand-all-handler")) {
                @Override
                public VncVal apply(final VncList args) {
                    final VncVal form = args.first();

                    if (Types.isVncList(form)) {
                        final VncList list = (VncList)form;
                        final VncVal first = list.first();
                        if (Types.isVncSymbol(first)) {
                            final VncVal second = list.second();
                            // check if the expression is of the form (ns x)
                            if (list.size() == 2
                                    && "ns".equals(((VncSymbol)first).getName())
                                    && second instanceof VncSymbol
                            ) {
                                // we've encountered a '(ns x)' symbolic expression -> apply it
                                Namespaces.setCurrentNamespace(nsRegistry.computeIfAbsent((VncSymbol)second));
                            }
                            else {
                                // try to expand
                                return doMacroexpand(list, env);
                            }
                        }
                    }

                    return form;
                }
                private static final long serialVersionUID = -1L;
            };

            final VncFunction walk = new VncFunction(createAnonymousFuncName("macroexpand-all-walk")) {
                // Java implementation of 'core/walk' from 'core' module with
                // the optimization for 'outer' function as 'identity' for 'core/prewalk'
                @Override
                public VncVal apply(final VncList args) {
                    final VncFunction inner = (VncFunction)args.first();
                    final VncVal form = args.second();

                    if (Types.isVncList(form)) {
                        // (outer (apply list (map inner form)))
                        return TransducerFunctions.map.applyOf(inner, form);
                    }
                    else if (Types.isVncMapEntry(form)) {
                        // (outer (map-entry (inner (key form)) (inner (val form))))
                        return CoreFunctions.new_map_entry.applyOf(
                                    inner.applyOf(((VncMapEntry)form).getKey()),
                                    inner.applyOf(((VncMapEntry)form).getValue()));
                    }
                    else if (Types.isVncCollection(form)) {
                        // (outer (into (empty form) (map inner form)))
                        return CoreFunctions.into.applyOf(
                                    CoreFunctions.empty.applyOf(form),
                                    TransducerFunctions.map.applyOf(inner, form));
                    }
                    else {
                        // (outer form)
                        return form;
                    }
                }
                private static final long serialVersionUID = -1L;
            };

            final VncFunction prewalk = new VncFunction(createAnonymousFuncName("macroexpand-all-prewalk")) {
                // Java implementation of 'core/prewalk' from 'core' module
                @Override
                public VncVal apply(final VncList args) {
                    final VncFunction f = (VncFunction)args.first();
                    final VncVal form = args.second();

                    return walk.applyOf(
                                CoreFunctions.partial.applyOf(this, f),
                                f.applyOf(form));
                }
                private static final long serialVersionUID = -1L;
            };


            // remember the original namespace
            final Namespace original_ns = Namespaces.getCurrentNamespace();
            try {
                return prewalk.applyOf(handler, form);
            }
            finally {
                // set the original namespace back
                Namespaces.setCurrentNamespace(original_ns);
            }
        }
    }

    private VncVal tail_pos_check_(
            final boolean inTailPosition,
            final VncList args,
            final Env env,
            final VncVal meta
    ) {
        if (!inTailPosition) {
            final CallFrame callframe = new CallFrame("tail-pos", args, meta);
            final VncString name = Coerce.toVncString(args.nthOrDefault(0, VncString.empty()));
            try (WithCallStack cs = new WithCallStack(callframe)) {
                throw new NotInTailPositionException(
                        name.isEmpty()
                            ? "Not in tail position"
                            : String.format(
                                "The tail-pos expression '%s' is not in tail position",
                                name.getValue()));
            }
        }
        else {
            return Nil;
        }
    }

    private Env buildRecursionEnv(
            final VncList args,
            final Env env,
            final RecursionPoint recursionPoint
    ) {
        final Env recur_env = recursionPoint.getLoopEnv();

        final int argCount = args.size();

        // denormalize for best performance (short loops are performance critical)
        switch(argCount) {
            case 0:
                break;

            case 1:
                // [1][2] calculate and bind the single new value
                final VncVal symVal0 = recursionPoint.getLoopBindingName(0);
                final VncVal v0 = evaluate(args.first(), env, false);
                RecursionPoint.addToLocalEnv(symVal0, v0, recur_env);
                break;

            case 2:
                final VncVal symVal1 = recursionPoint.getLoopBindingName(0);
                final VncVal symVal2 = recursionPoint.getLoopBindingName(1);

                // [1] calculate the new values
                final VncVal v1 = evaluate(args.first(), env, false);
                final VncVal v2 = evaluate(args.second(), env, false);

                // [2] bind the new values
                RecursionPoint.addToLocalEnv(symVal1, v1, recur_env);
                RecursionPoint.addToLocalEnv(symVal2, v2, recur_env);
                break;

            default:
                // [1] calculate new values
                final VncVal[] newValues = new VncVal[argCount];
                for(int ii=0; ii<argCount; ii++) {
                    newValues[ii] = evaluate(args.nth(ii), env, false);
                }

                // [2] bind the new values
                for(int ii=0; ii<argCount; ii++) {
                    final VncVal symVal = recursionPoint.getLoopBindingName(ii);
                    RecursionPoint.addToLocalEnv(symVal, newValues[ii], recur_env);
                }
                break;
        }

        return recur_env;
    }



    private static final long serialVersionUID = -8130740279914790685L;


    private final IInterceptor interceptor;
    private final boolean checkSandbox;
    private final MeterRegistry meterRegistry;
    private final NamespaceRegistry nsRegistry;

    private final SpecialFormsContext specialFormsContext;
    private final FunctionBuilder functionBuilder;

    private final AtomicBoolean sealedSystemNS;

    private final boolean optimized;

    private volatile boolean macroExpandOnLoad = false;
}
