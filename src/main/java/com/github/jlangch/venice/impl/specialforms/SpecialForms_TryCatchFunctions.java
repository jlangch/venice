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
import static com.github.jlangch.venice.impl.specialforms.util.SpecialFormsUtil.resolveClassName;
import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.github.jlangch.venice.ValueException;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.debug.agent.DebugAgent;
import com.github.jlangch.venice.impl.debug.breakpoint.BreakpointFnRef;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.specialforms.util.CatchBlock;
import com.github.jlangch.venice.impl.specialforms.util.FinallyBlock;
import com.github.jlangch.venice.impl.specialforms.util.SpecialFormsContext;
import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncSpecialForm;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.CallStack;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;
import com.github.jlangch.venice.impl.util.reflect.ReflectionAccessor;


/**
 * The special form pseudo functions
 *
 * Special forms have evaluation rules that differ from standard Venice
 * evaluation rules and are understood directly by the Venice interpreter.
 */
public class SpecialForms_TryCatchFunctions {

    public static VncSpecialForm try_ =
        new VncSpecialForm(
                "try",
                VncSpecialForm
                    .meta()
                    .arglists(
                            "(try expr*)",
                            "(try expr* (catch selector ex-sym expr*)*)",
                            "(try expr* (catch selector ex-sym expr*)* (finally expr*))")
                    .doc(
                        "Exception handling: try - catch - finally \n\n" +
                        "`(try)` without any expression returns `nil`.\n\n" +
                        "The exception types \n\n" +
                        "  * :java.lang.Exception \n" +
                        "  * :java.lang.RuntimeException \n" +
                        "  * :com.github.jlangch.venice.VncException \n" +
                        "  * :com.github.jlangch.venice.ValueException \n\n" +
                        "are imported implicitly so its alias :Exception, :RuntimeException, " +
                        ":VncException, and :ValueException can be used as selector without " +
                        "an import of the class.\n\n" +
                        "**Selectors**\n\n" +
                        "  * a class: (e.g., :RuntimeException, :java.text.ParseException), " +
                        "    matches any instance of that class\n" +
                        "  * a key-values vector: (e.g., [key val & kvs]), matches any instance " +
                        "    of :ValueException where the exception's value meets the expression " +
                        "    `(and (= (get ex-value key) val) ...)`\n" +
                        "  * a predicate: (a function of one argument like map?, set?), matches " +
                        "    any instance of :ValueException where the predicate applied to the " +
                        "    exception's value returns true\n\n" +
                        "**Notes:**\n\n" +
                        "The finally block is just for side effects, like closing resources. " +
                        "It never returns a value!\n\n" +
                        "All exceptions in Venice are *unchecked*. If *checked* exceptions are thrown " +
                        "in Venice they are immediately wrapped in a :RuntimeException before being " +
                        "thrown! If Venice catches a *checked* exception from a Java interop call " +
                        "it wraps it in a :RuntimeException before handling it by the catch block " +
                        "selectors.")
                    .examples(
                        "(try                                      \n" +
                        "   (throw \"test\")                       \n" +
                        "   (catch :ValueException e               \n" +
                        "          \"caught ~(ex-value e)\"))        ",

                        "(try                                       \n" +
                        "   (throw 100)                             \n" +
                        "   (catch :Exception e -100))                ",

                        "(try                                       \n" +
                        "   (throw 100)                             \n" +
                        "   (catch :ValueException e (ex-value e))  \n" +
                        "   (finally (println \"...finally\")))       ",

                        "(try                                              \n" +
                        "   (throw (ex :RuntimeException \"message\"))     \n" +
                        "   (catch :RuntimeException e (ex-message e)))     ",

                        ";; exception type selector:                       \n" +
                        "(try                                              \n" +
                        "   (throw [1 2 3])                                \n" +
                        "   (catch :ValueException e (ex-value e))         \n" +
                        "   (catch :RuntimeException e \"runtime ex\")     \n" +
                        "   (finally (println \"...finally\")))             ",

                        ";; key-value selector:                                      \n" +
                        "(try                                                        \n" +
                        "   (throw {:a 100, :b 200})                                 \n" +
                        "   (catch [:a 100] e                                        \n" +
                        "      (println \"ValueException, value: ~(ex-value e)\"))   \n" +
                        "   (catch [:a 100, :b 200] e                                \n" +
                        "      (println \"ValueException, value: ~(ex-value e)\")))   ",

                        ";; key-value selector (exception cause):                           \n" +
                        "(try                                                               \n" +
                        "   (throw (ex :java.io.IOException \"failure\"))                   \n" +
                        "   (catch [:cause-type :java.io.IOException] e                     \n" +
                        "      (println \"IOException, msg: ~(ex-message (ex-cause e))\"))  \n" +
                        "   (catch :RuntimeException e                                      \n" +
                        "      (println \"RuntimeException, msg: ~(ex-message e)\")))         ",

                        ";; predicate selector:                                      \n" +
                        "(try                                                        \n" +
                        "   (throw {:a 100, :b 200})                                 \n" +
                        "   (catch long? e                                           \n" +
                        "      (println \"ValueException, value: ~(ex-value e)\"))   \n" +
                        "   (catch map? e                                            \n" +
                        "      (println \"ValueException, value: ~(ex-value e)\"))   \n" +
                        "   (catch #(and (map? %) (= 100 (:a %))) e                  \n" +
                        "      (println \"ValueException, value: ~(ex-value e)\"))))   ",

                        ";; predicate selector with custom types:                       \n" +
                        "(do                                                            \n" +
                        "   (deftype :my-exception1 [message :string, position :long])  \n" +
                        "   (deftype :my-exception2 [message :string])                  \n" +
                        "                                                               \n" +
                        "   (try                                                        \n" +
                        "      (throw (my-exception1. \"error\" 100))                   \n" +
                        "      (catch my-exception1? e                                  \n" +
                        "         (println (:value e)))                                 \n" +
                        "      (catch my-exception2? e                                  \n" +
                        "         (println (:value e)))))                                 ")
                    .seeAlso("try-with", "throw", "ex")
                    .build()
        ) {
            @Override
            public VncVal apply(
                    final VncVal specialFormMeta,
                    final VncList args,
                    final Env env,
                    final SpecialFormsContext ctx
            ) {
                return handleTryCatchFinally(
                        "try",
                        args,
                        ctx,
                        env,
                        specialFormMeta,
                        new ArrayList<Var>());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncSpecialForm try_with =
        new VncSpecialForm(
                "try-with",
                VncSpecialForm
                    .meta()
                    .arglists(
                            "(try-with [bindings*] expr*)",
                            "(try-with [bindings*] expr* (catch selector ex-sym expr*)*)",
                            "(try-with [bindings*] expr* (catch selector ex-sym expr*)* (finally expr))")
                    .doc(
                        "*try-with-resources* allows the declaration of resources to be used in a try block " +
                        "with the assurance that the resources will be closed after execution " +
                        "of that block. The resources declared must implement the Closeable or " +
                        "AutoCloseable interface.")
                    .examples(
                        "(do                                                   \n" +
                        "   (import :java.io.FileInputStream)                  \n" +
                        "   (let [file (io/temp-file \"test-\", \".txt\")]     \n" +
                        "        (io/spit file \"123456789\" :append true)     \n" +
                        "        (try-with [is (. :FileInputStream :new file)] \n" +
                        "           (io/slurp-stream is :binary false))))        ")
                    .seeAlso("try", "throw", "ex")
                    .build()
        ) {
            @Override
            public VncVal apply(
                    final VncVal specialFormMeta,
                    final VncList args,
                    final Env env,
                    final SpecialFormsContext ctx
            ) {
                final Env localEnv = new Env(env);
                final VncSequence bindings = Coerce.toVncSequence(args.first());
                final List<Var> boundResources = new ArrayList<>();

                for(int i=0; i<bindings.size(); i+=2) {
                    final VncVal sym = bindings.nth(i);
                    final VncVal val = ctx.getEvaluator().evaluate(bindings.nth(i+1), localEnv, false);

                    if (Types.isVncSymbol(sym)) {
                        final Var binding = new Var((VncSymbol)sym, val);
                        localEnv.setLocal(binding);
                        boundResources.add(binding);
                    }
                    else {
                        throw new VncException(
                                String.format(
                                        "Invalid 'try-with' destructuring symbol "
                                        + "value type %s. Expected symbol.",
                                        Types.getType(sym)));
                    }
                }

                try {
                    return handleTryCatchFinally(
                                "try-with",
                                args.rest(),
                                ctx,
                                localEnv,
                                specialFormMeta,
                                boundResources);
                }
                finally {
                    // close resources in reverse order
                    Collections.reverse(boundResources);
                    boundResources.stream().forEach(b -> {
                        final VncVal resource = b.getVal();
                        if (Types.isVncJavaObject(resource)) {
                            final Object r = ((VncJavaObject)resource).getDelegate();
                            if (r instanceof AutoCloseable) {
                                try {
                                    ((AutoCloseable)r).close();
                                }
                                catch(Exception ex) {
                                    throw new VncException(
                                            String.format(
                                                    "'try-with' failed to close resource %s.",
                                                    b.getName()));
                                }
                            }
                        }
                    });
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };





    ///////////////////////////////////////////////////////////////////////////
    // helpers
    ///////////////////////////////////////////////////////////////////////////

    private static VncVal handleTryCatchFinally(
            final String specialForm,
            final VncList args,
            final SpecialFormsContext ctx,
            final Env env,
            final VncVal meta,
            final List<Var> bindings
    ) {
        final ThreadContext threadCtx = ThreadContext.get();
        final DebugAgent debugAgent = threadCtx.getDebugAgent_();

        if (debugAgent != null && debugAgent.hasBreakpointFor(new BreakpointFnRef(specialForm))) {
            final CallStack callStack = threadCtx.getCallStack_();
            debugAgent.onBreakSpecialForm(
                    specialForm, FunctionEntry, bindings, meta, env, callStack);
        }

        try {
            final Env bodyEnv = new Env(env);
            return evaluateBody(getTryBody(args), ctx, bodyEnv, true);
        }
        catch (Exception ex) {
            final RuntimeException wrappedEx = ex instanceof RuntimeException
                                                    ? (RuntimeException)ex
                                                    : new RuntimeException(ex);

            final CatchBlock catchBlock = findCatchBlockMatchingThrowable(ctx, env, args, ex);
            if (catchBlock == null) {
                throw wrappedEx;
            }
            else {
                final Env catchEnv = new Env(env);
                catchEnv.setLocal(new Var(catchBlock.getExSym(), new VncJavaObject(wrappedEx)));
                catchBlockDebug(threadCtx, debugAgent, catchBlock.getMeta(), catchEnv, catchBlock.getExSym(), wrappedEx);
                return evaluateBody(catchBlock.getBody(), ctx, catchEnv, false);
            }
        }
        finally {
            final FinallyBlock finallyBlock = findFirstFinallyBlock(args);
            if (finallyBlock != null) {
                final Env finallyEnv = new Env(env);
                finallyBlockDebug(threadCtx, debugAgent, finallyBlock.getMeta(), finallyEnv);
                evaluateBody(finallyBlock.getBody(), ctx, finallyEnv, false);
            }
        }
    }

    private static VncList getTryBody(final VncList args) {
        final List<VncVal> body = new ArrayList<>();
         for(VncVal e : args) {
            if (Types.isVncList(e)) {
                final VncVal first = ((VncList)e).first();
                if (Types.isVncSymbol(first)) {
                    final String symName = ((VncSymbol)first).getName();
                    if (symName.equals("catch") || symName.equals("finally")) {
                        break;
                    }
                }
            }
            body.add(e);
        }

        return VncList.ofList(body);
    }

    private static CatchBlock findCatchBlockMatchingThrowable(
            final SpecialFormsContext ctx,
            final Env env,
            final VncList blocks,
            final Throwable th
    ) {
        // (catch ex-class ex-sym expr*)

        for(VncVal b : blocks) {
            if (Types.isVncList(b)) {
                final VncList block = ((VncList)b);
                final VncVal catchSym = block.first();
                if (Types.isVncSymbol(catchSym) && ((VncSymbol)catchSym).getName().equals("catch")) {
                    if (isCatchBlockMatchingThrowable(ctx, env, block, th)) {
                        return new CatchBlock(
                                    Coerce.toVncSymbol(block.third()),
                                    block.slice(3),
                                    catchSym.getMeta());
                    }
                }
            }
        }

        return null;
    }

    private static boolean isCatchBlockMatchingThrowable(
            final SpecialFormsContext ctx,
            final Env env,
            final VncList block,
            final Throwable th
    ) {
        final VncVal selector = ctx.getEvaluator().evaluate(block.second(), env, false);

        // Selector: exception class => (catch :RuntimeExceptiom e (..))
        if (Types.isVncString(selector)) {
            final String className = resolveClassName(((VncString)selector).getValue());
            final Class<?> targetClass = ReflectionAccessor.classForName(className);

            return targetClass.isAssignableFrom(th.getClass());
        }

        // Selector: predicate => (catch predicate-fn e (..))
        else if (Types.isVncFunction(selector)) {
            final VncFunction predicate = (VncFunction)selector;

            if (th instanceof ValueException) {
                final VncVal exVal = getValueExceptionValue((ValueException)th);
                final VncVal result = predicate.apply(VncList.of(exVal));
                return VncBoolean.isTrue(result);
            }
            else {
                final VncVal result = predicate.apply(VncList.of(Nil));
                return VncBoolean.isTrue(result);
            }
        }

        // Selector: list => (catch [key1 value1, ...] e (..))
        else if (Types.isVncSequence(selector)) {
            VncSequence seq = (VncSequence)selector;

            // (catch [:cause :IOException, ...] e (..))
            if (seq.first().equals(CAUSE_TYPE_SELECTOR_KEY) && Types.isVncKeyword(seq.second())) {
                final Throwable cause = th.getCause();
                if (cause != null) {
                    final VncKeyword classRef = (VncKeyword)seq.second();
                    final String className = resolveClassName(classRef.getSimpleName());
                    final Class<?> targetClass = ReflectionAccessor.classForName(className);

                    if (!targetClass.isAssignableFrom(cause.getClass())) {
                        return false;
                    }

                    if (seq.size() == 2) {
                        return true; // no more key/val pairs
                    }
                }
                seq = seq.drop(2);
            }

            // (catch [key1 value1, ...] e (..))
            if (th instanceof ValueException) {
                final VncVal exVal = getValueExceptionValue((ValueException)th);
                if (Types.isVncMap(exVal)) {
                    final VncMap exValMap = (VncMap)exVal;

                    while (!seq.isEmpty()) {
                        final VncVal key = seq.first();
                        final VncVal val = seq.second();

                        if (!Types._equal_strict_Q(val, exValMap.get(key))) {
                            return false;
                        }

                        seq = seq.drop(2);
                    }

                    return true;
                }
            }

            return false;
        }

        else {
            return false;
        }
    }

    private static FinallyBlock findFirstFinallyBlock(final VncList blocks) {
        for(VncVal b : blocks) {
            if (Types.isVncList(b)) {
                final VncList block = ((VncList)b);
                final VncVal first = block.first();
                if (Types.isVncSymbol(first) && ((VncSymbol)first).getName().equals("finally")) {
                    return new FinallyBlock(block.rest(), first.getMeta());
                }
            }
        }
        return null;
    }

    private static void catchBlockDebug(
            final ThreadContext threadCtx,
            final DebugAgent debugAgent,
            final VncVal meta,
            final Env env,
            final VncSymbol exSymbol,
            final RuntimeException ex
    ) {
        if (debugAgent != null && debugAgent.hasBreakpointFor(new BreakpointFnRef("catch"))) {
            debugAgent.onBreakSpecialForm(
                    "catch",
                    FunctionEntry,
                    VncVector.of(exSymbol),
                    VncList.of(new VncJavaObject(ex)),
                    meta,
                    env,
                    threadCtx.getCallStack_());
        }
    }

    private static void finallyBlockDebug(
            final ThreadContext threadCtx,
            final DebugAgent debugAgent,
            final VncVal meta,
            final Env env
    ) {
        if (debugAgent != null && debugAgent.hasBreakpointFor(new BreakpointFnRef("finally"))) {
            debugAgent.onBreakSpecialForm(
                    "finally",
                    FunctionEntry,
                    new ArrayList<Var>(),
                    meta,
                    env,
                    threadCtx.getCallStack_());
        }
    }

    private static VncVal getValueExceptionValue(final ValueException ex) {
        final Object val = ex.getValue();

        return val == null
                ? Nil
                : val instanceof VncVal
                    ? (VncVal)val
                    : new VncJavaObject(val);
    }



    private static final VncKeyword CAUSE_TYPE_SELECTOR_KEY = new VncKeyword(":cause-type");




    ///////////////////////////////////////////////////////////////////////////
    // types_ns is namespace of type functions
    ///////////////////////////////////////////////////////////////////////////

    public static Map<VncVal, VncVal> ns =
            new SymbolMapBuilder()
                    .add(try_)
                    .add(try_with)
                    .toMap();
}
