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

import static com.github.jlangch.venice.impl.thread.ThreadBridge.Options.DEACTIVATE_DEBUG_AGENT;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.debug.agent.DebugAgent;
import com.github.jlangch.venice.impl.util.CallFrame;
import com.github.jlangch.venice.impl.util.CallStack;
import com.github.jlangch.venice.impl.util.CollectionUtil;
import com.github.jlangch.venice.impl.util.StringUtil;


/**
 * The <code>ThreadBridge</code> properly runs functions in clients threads
 * inheriting the correct environment from the calling parent function.
 *
 * <p>Functions that are run in futures, agents, or schedulers are managed
 * by this bridge.
 */
public class ThreadBridge {

    private ThreadBridge(
            final String name,
            final ThreadContextSnapshot parentThreadSnapshot,
            final boolean deactivateDebugAgent,
            final CallFrame[] callFrames
    ) {
        this.parentThreadSnapshot = parentThreadSnapshot;
        this.deactivateDebugAgent = deactivateDebugAgent;
        this.callFrames = callFrames;
    }


    public static ThreadBridge create(
            final String name
    ) {
        return create(name, new CallFrame[0], new Options[0]);
    }

    public static ThreadBridge create(
            final String name,
            final CallFrame callFrame,
            final Options... options
    ) {
        return create(name, new CallFrame[]{callFrame}, options);
    }

    public static ThreadBridge create(
            final String name,
            final CallFrame[] callFrames,
            final Options... options
    ) {
        final Set<Options> opts = new HashSet<>(CollectionUtil.toList(options));

        final boolean deactivateDebugAgent = opts.contains(DEACTIVATE_DEBUG_AGENT);

        validateName(name);

        return new ThreadBridge(
                        name,
                        ThreadContext.snapshot(),
                        deactivateDebugAgent,
                        callFrames);
    }


    public <T> Callable<T> bridgeCallable(final Callable<T> callable) {
        final Callable<T> wrapper = () -> {
            try {
                // inherit thread local values to the child thread
                ThreadContext.inheritFrom(parentThreadSnapshot);

                if (callFrames != null) {
                    final CallStack cs = ThreadContext.getCallStack();
                    for(CallFrame cf : callFrames) {
                        cs.push(cf);
                    }
                }

                if (deactivateDebugAgent) {
                    DebugAgent.unregister();
                }

                return callable.call();
            }
            finally {
                // clean up
                ThreadContext.remove();
            }};

        return wrapper;
    }

    public Runnable bridgeRunnable(final Runnable runnable) {
        final Runnable wrapper = () -> {
            try {
                // inherit thread local values to the child thread
                ThreadContext.inheritFrom(parentThreadSnapshot);

                if (deactivateDebugAgent) {
                    DebugAgent.unregister();
                }

                runnable.run();
            }
            finally {
                // clean up
                ThreadContext.remove();
            }};

        return wrapper;
    }

    public <T> Consumer<T> bridgeConsumer(final Consumer<T> consumer) {
        final Consumer<T> wrapper = (T t) -> {
            try {
                // inherit thread local values to the child thread
                ThreadContext.inheritFrom(parentThreadSnapshot);

                if (callFrames != null) {
                    final CallStack cs = ThreadContext.getCallStack();
                    for(CallFrame cf : callFrames) {
                        cs.push(cf);
                    }
                }

                if (deactivateDebugAgent) {
                    DebugAgent.unregister();
                }

                consumer.accept(t);
            }
            finally {
                // clean up
                ThreadContext.remove();
            }};

        return wrapper;
    }

    public <T,U> BiConsumer<T,U> bridgeBiConsumer(final BiConsumer<T,U> consumer) {
        final BiConsumer<T,U> wrapper = (T t, U u) -> {
            try {
                // inherit thread local values to the child thread
                ThreadContext.inheritFrom(parentThreadSnapshot);

                if (callFrames != null) {
                    final CallStack cs = ThreadContext.getCallStack();
                    for(CallFrame cf : callFrames) {
                        cs.push(cf);
                    }
                }

                if (deactivateDebugAgent) {
                    DebugAgent.unregister();
                }

                consumer.accept(t,u);
            }
            finally {
                // clean up
                ThreadContext.remove();
            }};

        return wrapper;
    }

    public <T> Supplier<T> bridgeSupplier(final Supplier<T> supplier) {
        final Supplier<T> wrapper = () -> {
            try {
                // inherit thread local values to the child thread
                ThreadContext.inheritFrom(parentThreadSnapshot);

                if (callFrames != null) {
                    final CallStack cs = ThreadContext.getCallStack();
                    for(CallFrame cf : callFrames) {
                        cs.push(cf);
                    }
                }

                if (deactivateDebugAgent) {
                    DebugAgent.unregister();
                }

                return supplier.get();
            }
            finally {
                // clean up
                ThreadContext.remove();
            }};

        return wrapper;
    }

    public <T,R> Function<T,R> bridgeFunction(final Function<T,R> func) {
        final Function<T,R> wrapper = (T t) -> {
            try {
                // inherit thread local values to the child thread
                ThreadContext.inheritFrom(parentThreadSnapshot);

                if (callFrames != null) {
                    final CallStack cs = ThreadContext.getCallStack();
                    for(CallFrame cf : callFrames) {
                        cs.push(cf);
                    }
                }

                if (deactivateDebugAgent) {
                    DebugAgent.unregister();
                }

                return func.apply(t);
            }
            finally {
                // clean up
                ThreadContext.remove();
            }};

        return wrapper;
    }

    public <T,U,R> BiFunction<T,U,R> bridgeBiFunction(final BiFunction<T,U,R> func) {
        final BiFunction<T,U,R> wrapper = (T t, U u) -> {
            try {
                // inherit thread local values to the child thread
                ThreadContext.inheritFrom(parentThreadSnapshot);

                if (callFrames != null) {
                    final CallStack cs = ThreadContext.getCallStack();
                    for(CallFrame cf : callFrames) {
                        cs.push(cf);
                    }
                }

                if (deactivateDebugAgent) {
                    DebugAgent.unregister();
                }

                return func.apply(t, u);
            }
            finally {
                // clean up
                ThreadContext.remove();
            }};

        return wrapper;
    }

    public boolean isSameAsCurrentThread() {
        return parentThreadSnapshot.isSameAsCurrentThread();
    }

    public static void handleUncaughtException(final Thread t, final Throwable e) {
        if (e instanceof VncException) {
            ((VncException)e).printVeniceStackTrace(System.err);
        }
        else {
            e.printStackTrace(System.err);
        }
    }


    private static void validateName(final String name) {
        if (StringUtil.isBlank(name)) {
            throw new VncException("A ThreadBridge name must not be blank!");
        }
    }


    public static enum Options {
        DEACTIVATE_DEBUG_AGENT
    };


    private final ThreadContextSnapshot parentThreadSnapshot;
    private final boolean deactivateDebugAgent;
    private final CallFrame[] callFrames;
}
