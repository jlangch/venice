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

import static com.github.jlangch.venice.impl.types.Constants.Nil;
import static com.github.jlangch.venice.impl.util.ArityExceptions.formatArityExMsg;
import static com.github.jlangch.venice.impl.util.ArityExceptions.formatVariadicArityExMsg;

import java.io.Serializable;

import com.github.jlangch.venice.ArityException;
import com.github.jlangch.venice.AssertionException;
import com.github.jlangch.venice.impl.debug.agent.DebugAgent;
import com.github.jlangch.venice.impl.debug.breakpoint.BreakpointFnRef;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.namespaces.Namespace;
import com.github.jlangch.venice.impl.namespaces.Namespaces;
import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions.FnType;
import com.github.jlangch.venice.impl.util.CallFrame;
import com.github.jlangch.venice.impl.util.CallFrameFnData;
import com.github.jlangch.venice.impl.util.CallStack;
import com.github.jlangch.venice.impl.util.WithCallStack;


public class FunctionBuilder implements Serializable {

    public FunctionBuilder(
            final IFormEvaluator evaluator,
            final boolean optimized
    ) {
        this.evaluator = evaluator;
        this.optimized = optimized;
    }


    public VncFunction buildFunction(
            final String name,
            final VncVector params,
            final VncList body,
            final VncVector preConditions,
            final boolean macro,
            final VncVal meta,
            final Env env
    ) {
        // the namespace the function/macro is defined for
        final Namespace functionNS = Namespaces.getCurrentNamespace();

        // Note: Do not switch to the functions own namespace for the function
        //       "core/macroexpand-all". Handle "macroexpand-all" like a special
        //       form. This allows expanding locally defined macros from the REPL
        //       without the need of qualifying them:
        //          > (defmacro bench [expr] ...)
        //          > (macroexpand-all '(bench (+ 1 2))
        //       instead of:
        //          > (macroexpand-all '(user/bench (+ 1 2))
        final boolean switchToFunctionNamespaceAtRuntime = !macro && !name.equals("macroexpand-all");

        // Destructuring optimization for function parameters
        final boolean plainSymbolParams = Destructuring.isFnParamsWithoutDestructuring(params);

        // PreCondition optimization
        final boolean hasPreConditions = preConditions != null && !preConditions.isEmpty();

        // Body evaluation optimizations
        final VncVal[] bodyExprs = body.isEmpty()
                                        ? new VncVal[] {Nil}
                                        : body.getJavaList().toArray(new  VncVal[] {});

        // Param access optimizations
        final VncVal[] paramArr = params.getJavaList().toArray(new  VncVal[] {});

        return new VncFunction(name, params, macro, preConditions, meta) {
            @Override
            public VncVal apply(final VncList args) {
                final ThreadContext threadCtx = ThreadContext.get();

                final CallFrameFnData callFrameFnData = threadCtx.getAndClearCallFrameFnData_();

                if (hasVariadicArgs()) {
                    if (args.size() < getFixedArgsCount()) {
                        throwVariadicArityException(this, args, callFrameFnData);
                    }
                }
                else if (args.size() != getFixedArgsCount()) {
                    throwFixedArityException(this, args, callFrameFnData);
                }

                final Env localEnv = new Env(env);

                addFnArgsToEnv(args, localEnv);

                if (switchToFunctionNamespaceAtRuntime) {
                    final CallStack callStack = threadCtx.getCallStack_();
                    final DebugAgent debugAgent = threadCtx.getDebugAgent_();
                    final Namespace curr_ns = threadCtx.getCurrNS_();
                    final String fnName = getQualifiedName();

                    final boolean pushCallstack = !optimized
                                                    && callFrameFnData != null
                                                    && callFrameFnData.matchesFnName(fnName);
                    if (pushCallstack) {
                        callStack.push(new CallFrame(fnName, args, callFrameFnData.getFnMeta(), localEnv));
                    }

                    try {
                        threadCtx.setCurrNS_(functionNS);

                        if (debugAgent != null && debugAgent.hasBreakpointFor(new BreakpointFnRef(fnName))) {
                            final CallStack cs = threadCtx.getCallStack_();
                            try {
                                debugAgent.onBreakFnEnter(fnName, this, args, localEnv, cs);
                                if (hasPreConditions) {
                                    validateFnPreconditions(localEnv);
                                }
                                final VncVal retVal = evaluateBody(bodyExprs, localEnv, true);
                                debugAgent.onBreakFnExit(fnName, this, args, retVal, localEnv, cs);
                                return retVal;
                            }
                            catch(Exception ex) {
                                debugAgent.onBreakFnException(fnName, this, args, ex, localEnv, cs);
                                throw ex;
                            }
                        }
                        else {
                            if (hasPreConditions) {
                                validateFnPreconditions(localEnv);
                            }
                            return evaluateBody(bodyExprs, localEnv, true);
                        }
                    }
                    finally {
                        if (pushCallstack) {
                            callStack.pop();
                        }

                        // switch always back to current namespace, just in case
                        // the namespace was changed within the function body!
                        threadCtx.setCurrNS_(curr_ns);
                    }
                }
                else {
                    if (hasPreConditions) {
                        validateFnPreconditions(localEnv);
                    }
                    return evaluateBody(bodyExprs, localEnv, false);
                }
            }

            @Override
            public boolean isNative() {
                return false;
            }

            @Override
            public VncVal getBody() {
                return body;
            }

            private void addFnArgsToEnv(final VncList args, final Env env) {
                // destructuring fn params -> args
                if (plainSymbolParams) {
                    for(int ii=0; ii<paramArr.length; ii++) {
                        env.setLocal(
                            new Var(
                                (VncSymbol)paramArr[ii],
                                args.nthOrDefault(ii, Nil)));
                    }
                }
                else {
                    env.addLocalVars(Destructuring.destructure(params, args));
                }
            }

            private void validateFnPreconditions(final Env env) {
                final Env local = new Env(env);
                for(VncVal v : preConditions) {
                    if (!isFnConditionTrue(evaluator.evaluate(v, local, false))) {
                        final CallFrame cf = new CallFrame(name, v.getMeta());
                        try (WithCallStack cs = new WithCallStack(cf)) {
                            throw new AssertionException(String.format(
                                    "pre-condition assert failed: %s",
                                    v.toString(true)));
                        }
                    }
                }
            }

            private static final long serialVersionUID = -1L;
        };
    }


    private void throwVariadicArityException(
            final VncFunction fn,
            final VncList args,
            final CallFrameFnData callFrameFnData
    ) {
        final VncVal meta = callFrameFnData == null ? null : callFrameFnData.getFnMeta();
        final CallFrame cf = new CallFrame(fn.getQualifiedName(), meta);
        try (WithCallStack cs = new WithCallStack(cf)) {
            throw new ArityException(
                    formatVariadicArityExMsg(
                        fn.getQualifiedName(),
                        fn.isMacro() ? FnType.Macro : FnType.Function,
                        args.size(),
                        fn.getFixedArgsCount(),
                        fn.getArgLists()));
        }
    }

    private void throwFixedArityException(
            final VncFunction fn,
            final VncList args,
            final CallFrameFnData callFrameFnData
    ) {
        final VncVal meta = callFrameFnData == null ? null : callFrameFnData.getFnMeta();
        final CallFrame cf = new CallFrame(fn.getQualifiedName(), meta);
        try (WithCallStack cs = new WithCallStack(cf)) {
            throw new ArityException(
                    formatArityExMsg(
                        fn.getQualifiedName(),
                        fn.isMacro() ? FnType.Macro : FnType.Function,
                        args.size(),
                        fn.getFixedArgsCount(),
                        fn.getArgLists()));
        }
    }

    private boolean isFnConditionTrue(final VncVal result) {
        return Types.isVncSequence(result)
                ? VncBoolean.isTrue(((VncSequence)result).first())
                : VncBoolean.isTrue(result);
    }

    private VncVal evaluateBody(
            final VncVal[] body,
            final Env env,
            final boolean withTailPosition
    ) {
        if (body.length == 1) {
            return evaluator.evaluate(body[0], env, withTailPosition);
        }
        else {
            for(int ii=0; ii<body.length-1; ii++) {
                evaluator.evaluate(body[ii], env, false);
            }
            return evaluator.evaluate(body[body.length-1], env, withTailPosition);
        }
    }


    private static final long serialVersionUID = -7383567149949961233L;

    private final IFormEvaluator evaluator;
    private final boolean optimized;
}

