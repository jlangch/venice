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
package com.github.jlangch.venice.impl.thread;

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.io.PrintStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.debug.agent.DebugAgent;
import com.github.jlangch.venice.impl.namespaces.Namespace;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncStack;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.util.CallFrameFnData;
import com.github.jlangch.venice.impl.util.CallStack;
import com.github.jlangch.venice.impl.util.MeterRegistry;
import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;
import com.github.jlangch.venice.javainterop.IInterceptor;
import com.github.jlangch.venice.javainterop.RejectAllInterceptor;


/**
 * The <code>ThreadContext</code> holds all thread local data that is used
 * by Venice.
 *
 * <ul>
 *   <li>Current namespace</li>
 *   <li>Thread local vars</li>
 *   <li>Callstack</li>
 *   <li>Debug agent (mirrored across threads)</li>
 *   <li>Sandbox interceptor (mirrored across threads)</li>
 *   <li>Meter registry (mirrored across threads)</li>
 * </ul>
 */
public class ThreadContext {

    public ThreadContext() {
        initNS();
    }


    public Namespace getCurrNS_() {
        return ns;
    }

    public void setCurrNS_(final Namespace ns) {
        this.ns = ns;
    }

    public DebugAgent getDebugAgent_() {
        return debugAgent;
    }

    public CallStack getCallStack_() {
        return callStack;
    }

    public CallFrameFnData getAndClearCallFrameFnData_() {
        final CallFrameFnData data = callFrameFnData;
        callFrameFnData = null;
        return data;
    }
    public CallFrameFnData getCallFrameFnData_() {
        return callFrameFnData;
    }

    public void setCallFrameFnData_(final CallFrameFnData data) {
        callFrameFnData = data;
    }

    public static VncVal getValue(final VncKeyword key) {
        return getValue(key, Nil);
    }

    public static VncVal getValue(final VncKeyword key, final VncVal defaultValue) {
        if (key == null) {
            return Nil;
        }
        else {
            final VncVal v = get().values.get(key);
            if (v instanceof VncStack) {
                final VncVal thVal = ((VncStack)v).peek();
                return thVal == Nil ? defaultValue : thVal;
            }
            else {
                return v == null ? defaultValue : v;
            }
        }
    }

    public static void setValue(final VncKeyword key, final VncVal val) {
        if (key != null) {
            final ThreadContext ctx = get();
            final VncVal v = ctx.values.get(key);
            if (v == null) {
                ctx.values.put(key, val == null ? Nil : val);
            }
            else if (v instanceof VncStack) {
                ((VncStack) v).clear();
                ((VncStack)v).push(val == null ? Nil : val);
            }
            else {
                ctx.values.put(key, val);
            }
        }
    }

    public static void removeValue(final VncKeyword key) {
        if (key != null) {
            get().values.remove(key);
        }
    }

    public static boolean containsKey(final VncKeyword key) {
        return key == null ? false : get().values.containsKey(key);
    }

    public static void pushValue(final VncKeyword key, final VncVal val) {
        if (key != null) {
            final ThreadContext ctx = get();
            if (ctx.values.containsKey(key)) {
                final VncVal v = ctx.values.get(key);
                if (v instanceof VncStack) {
                    ((VncStack)v).push(val == null ? Nil : val);
                }
                else {
                    throw new VncException(String.format(
                            "The var %s is not defined as dynamic on the "
                            + "thread-local context",
                            key.getValue()));
                }
            }
            else {
                final VncStack stack = new VncStack();
                stack.push(val == null ? Nil : val);
                ctx.values.put(key, stack);
            }
        }
    }

    public static VncVal popValue(final VncKeyword key) {
        if (key != null) {
            final ThreadContext ctx = get();
            if (ctx.values.containsKey(key)) {
                final VncVal v = ctx.values.get(key);
                if (v instanceof VncStack) {
                    return ((VncStack)v).pop();
                }
                else {
                    throw new VncException(String.format(
                            "The var %s is not defined as dynamic on the "
                            + "thread-local context",
                            key.getValue()));
                }
            }
        }

        return Nil;
    }

    public static VncVal peekValue(final VncKeyword key) {
        if (key != null) {
            final ThreadContext ctx = get();
            if (ctx.values.containsKey(key)) {
                final VncVal v = ctx.values.get(key);
                if (v instanceof VncStack) {
                    return ((VncStack)v).peek();
                }
                else {
                    throw new VncException(String.format(
                            "The var %s is not defined as dynamic on the "
                            + "thread-local context",
                            key.getValue()));
                }
            }
        }

        return Nil;
    }

    public static Map<VncKeyword,VncVal> getValues() {
        final Map<VncKeyword,VncVal> copy = new HashMap<>();

        copyValues(get().values, copy);

        return copy;  // return a copy of the values
    }

    public static void clearCallStack() {
        get().callStack.clear();
    }

    public static CallStack getCallStack() {
        return  get().callStack;
    }

    public static Namespace getCurrNS() {
        return get().ns;
    }

    public static void setCurrNS(final Namespace ns) {
        get().ns = ns;
    }

    public static DebugAgent getDebugAgent() {
        return get().debugAgent;
    }

    public static void setDebugAgent(final DebugAgent agent) {
        get().debugAgent = agent;
    }

    public static MeterRegistry getMeterRegistry() {
        return get().meterRegistry;
    }

    public static void setMeterRegistry(final MeterRegistry registry) {
        get().meterRegistry = registry == null
                                ? new MeterRegistry(false)
                                : registry;
    }

    public static IInterceptor getInterceptor() {
        return get().interceptor;
    }

    public static void setInterceptor(final IInterceptor interceptor) {
        get().interceptor = interceptor == null
                                ? REJECT_ALL_INTERCEPTOR
                                : interceptor;
    }

    public static boolean isSandboxed() {
        return !(get().interceptor instanceof AcceptAllInterceptor);
    }

    public static void clearValues(final boolean preserveSystemValues) {
        try {
            if (preserveSystemValues) {
                final Map<VncKeyword, VncVal> values = get().values;

                // save
                final VncVal stdIn = values.get(STD_IN);
                final VncVal stdOut = values.get(STD_OUT);
                final VncVal stdErr = values.get(STD_ERR);

                values.clear();

                // restore
                values.put(STD_IN, stdIn);
                values.put(STD_OUT, stdOut);
                values.put(STD_ERR, stdErr);
            }
            else {
                get().values.clear();
            }
        }
        catch(Exception ex) {
            // do not care
        }
    }

    public static void clear() {
        try {
            final ThreadContext ctx = ThreadContext.get();

            ctx.interceptor = REJECT_ALL_INTERCEPTOR;
            ctx.debugAgent = null;
            ctx.values.clear();
            ctx.callStack.clear();
            ctx.meterRegistry = new MeterRegistry(false);
            ctx.initNS();
        }
        catch(Exception ex) {
            // do not care
        }
    }

    public static void remove() {
        try {
            clear();

            ThreadContext.context.set(null);
            ThreadContext.context.remove();
        }
        catch(Exception ex) {
            // do not care
        }
    }

    public static ThreadContext get() {
        return ThreadContext.context.get();
    }

    public static PrintStream getStdOut() {
        return Coerce.toVncJavaObject(peekValue(STD_OUT), PrintStream.class);
    }

    public static PrintStream getStdErr() {
        return Coerce.toVncJavaObject(peekValue(STD_ERR), PrintStream.class);
    }

    public static Reader getStdIn() {
        return Coerce.toVncJavaObject(peekValue(STD_IN), Reader.class);
    }

    public static ThreadContextSnapshot snapshot() {
        final ThreadContext ctx = get();

        final Map<VncKeyword,VncVal> vals = new HashMap<>();

        copyValues(ctx.values, vals);

        return new ThreadContextSnapshot(
                        Thread.currentThread().getId(),
                        ctx.ns,
                        vals,
                        ctx.debugAgent,
                        ctx.interceptor,
                        ctx.meterRegistry);
    }

    public static void inheritFrom(final ThreadContextSnapshot snapshot) {
        final ThreadContext ctx = get();

        copyValues(snapshot.getValues(), ctx.values);

        ctx.ns = snapshot.getNamespace();
        ctx.debugAgent = snapshot.getAgent();
        ctx.meterRegistry = snapshot.getMeterRegistry();
        ctx.interceptor = snapshot.getInterceptor();
    }


    private static void copyValues(final Map<VncKeyword,VncVal> from, final Map<VncKeyword,VncVal> to) {
        to.clear();

        for(Map.Entry<VncKeyword,VncVal> e : from.entrySet()) {
            final VncVal val = e.getValue();
            if (val instanceof VncStack) {
                final VncStack copyStack = new VncStack();
                if (!((VncStack)val).isEmpty()) {
                    copyStack.push(((VncStack)val).peek());
                }
                to.put(e.getKey(), copyStack);
            }
            else {
                to.put(e.getKey(), val);
            }
        }
    }

    private void initNS() {
        ns = new Namespace(new VncSymbol("user"));
    }



    private final Map<VncKeyword,VncVal> values = new HashMap<>();
    private final CallStack callStack = new CallStack();
    private Namespace ns = null;
    private DebugAgent debugAgent = null;
    private IInterceptor interceptor = REJECT_ALL_INTERCEPTOR;
    private MeterRegistry meterRegistry = new MeterRegistry(false);
    private CallFrameFnData callFrameFnData = null;

    private static final IInterceptor REJECT_ALL_INTERCEPTOR = new RejectAllInterceptor();
    private static final VncKeyword STD_IN = new VncKeyword("*in*");
    private static final VncKeyword STD_OUT = new VncKeyword("*out*");
    private static final VncKeyword STD_ERR = new VncKeyword("*err*");


    // Note: Do NOT use InheritableThreadLocal with ExecutorServices. It's not guaranteed
    //       to work in all cases!
    private static ThreadLocal<ThreadContext> context =
            ThreadLocal.withInitial(() -> new ThreadContext());
}
