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
package com.github.jlangch.venice.impl.functions;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import com.github.jlangch.venice.impl.thread.ThreadBridge;
import com.github.jlangch.venice.impl.threadpool.ManagedScheduledThreadPoolExecutor;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;
import com.github.jlangch.venice.impl.util.callstack.CallFrame;
import com.github.jlangch.venice.impl.util.time.TimeUnitUtil;


public class ScheduleFunctions {

    public static VncFunction schedule_delay =
        new VncFunction(
                "schedule-delay",
                VncFunction
                    .meta()
                    .arglists("(schedule-delay fn delay time-unit)")
                    .doc(
                        "Creates and executes a one-shot action that becomes enabled " +
                        "after the given delay.¶" +
                        "Returns a future. `(deref f)`, `(future? f)`, `(cancel f)`, " +
                        "and `(done? f)` will work on the returned future.¶" +
                        "Time unit is one of :milliseconds, :seconds, :minutes, :hours, or :days. ")
                    .examples(
                        "(schedule-delay (fn[] (println \"test\")) 1 :seconds)",
                        "(deref (schedule-delay (fn [] 100) 2 :seconds))")
                    .seeAlso("schedule-at-fixed-rate")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 3);

                sandboxFunctionCallValidation();

                final VncFunction fn = Coerce.toVncFunction(args.first());
                final VncLong delay = Coerce.toVncLong(args.second());
                final VncKeyword unit = Coerce.toVncKeyword(args.third());

                fn.sandboxFunctionCallValidation();

                // Create a wrapper that inherits the Venice thread context
                // from the parent thread to the executer thread!
                final ThreadBridge threadBridge = ThreadBridge.create(
                                                    "schedule-delay",
                                                    new CallFrame[] {
                                                        new CallFrame(this, args),
                                                        new CallFrame(fn)});
                final Callable<VncVal> taskWrapper = threadBridge.bridgeCallable(() -> fn.applyOf());

                final ScheduledFuture<VncVal> future = getScheduledExecutorService()
                                                        .schedule(
                                                            taskWrapper,
                                                            delay.getValue(),
                                                            TimeUnitUtil.toTimeUnit(unit));

                return new VncJavaObject(future);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction schedule_at_fixed_rate =
        new VncFunction(
                "schedule-at-fixed-rate",
                VncFunction
                    .meta()
                    .arglists("(schedule-at-fixed-rate fn initial-delay period time-unit)")
                    .doc(
                        "Creates and executes a periodic action that becomes enabled first " +
                        "after the given initial delay, and subsequently with the given " +
                        "period.¶" +
                        "Returns a future. `(future? f)`, `(cancel f)`, and `(done? f)` " +
                        "will work on the returned future.¶" +
                        "Time unit is one of :milliseconds, :seconds, :minutes, :hours, or :days. ")
                    .examples(
                        "(schedule-at-fixed-rate #(println \"test\") 1 2 :seconds)",

                        "(let [s (schedule-at-fixed-rate #(println \"test\") 1 2 :seconds)] \n" +
                        "   (sleep 16 :seconds) \n" +
                        "   (cancel s))")
                    .seeAlso("schedule-delay")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 4);

                sandboxFunctionCallValidation();

                final VncFunction fn = Coerce.toVncFunction(args.first());
                final VncLong delay = Coerce.toVncLong(args.second());
                final VncLong period = Coerce.toVncLong(args.third());
                final VncKeyword unit = Coerce.toVncKeyword(args.fourth());

                fn.sandboxFunctionCallValidation();

                // Create a wrapper that inherits the Venice thread context
                // from the parent thread to the executer thread!
                final ThreadBridge threadBridge = ThreadBridge.create(
                                                    "schedule-at-fixed-rate",
                                                    new CallFrame[] {
                                                        new CallFrame(this, args),
                                                        new CallFrame(fn)});
                final Runnable taskWrapper = threadBridge.bridgeRunnable(() -> fn.applyOf());

                final ScheduledFuture<?> future = getScheduledExecutorService()
                                                    .scheduleAtFixedRate(
                                                        taskWrapper,
                                                        delay.getValue(),
                                                        period.getValue(),
                                                        TimeUnitUtil.toTimeUnit(unit));

                return new VncJavaObject(future);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    ///////////////////////////////////////////////////////////////////////////
    // Utils
    ///////////////////////////////////////////////////////////////////////////

    public static void shutdown() {
        mngdExecutor.shutdown();
    }

    public static ScheduledExecutorService getScheduledExecutorService() {
        return mngdExecutor.getExecutor();
    }


    ///////////////////////////////////////////////////////////////////////////
    // types_ns is namespace of type functions
    ///////////////////////////////////////////////////////////////////////////

    public static final Map<VncVal, VncVal> ns =
            new SymbolMapBuilder()
                    .add(schedule_delay)
                    .add(schedule_at_fixed_rate)
                    .toMap();


    private static ManagedScheduledThreadPoolExecutor mngdExecutor =
        new ManagedScheduledThreadPoolExecutor("venice-scheduler-pool", 4);
}
