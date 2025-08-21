/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2025 Venice
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

import java.time.Duration;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.Future;

import com.github.jlangch.venice.impl.thread.ThreadBridge;
import com.github.jlangch.venice.impl.threadpool.ThreadPoolUtil;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;
import com.github.jlangch.venice.impl.util.callstack.CallFrame;

import io.timeandspace.cronscheduler.CronScheduler;
import io.timeandspace.cronscheduler.CronTask;


public class CronSchedulerFunctions {

    public static VncFunction schedule_at_round_times_in_day =
        new VncFunction(
                "cron/schedule-at-round-times-in-day",
                VncFunction
                    .meta()
                    .arglists("(schedule-at-round-times-in-day fn sync-period schedule-period)")
                    .doc(
                        "Submits a periodic task that becomes enabled at round clock times within " +
                        "a day, with the given period.                                            " +
                        "\n\n" +
                        "Returns a future. `(deref f)`, `(future? f)`, `(cancel f)`,              " +
                        "and `(done? f)` will work on the returned future.                        ")
                    .examples(
                        "(let [sync-period     (. :java.time.Duration :ofMinutes 10)                  \n" +
                        "      schedule-period (. :java.time.Duration :ofSeconds 5)                   \n" +
                        "      f               (fn [] (println (time/local-date-time)))               \n" +
                        "      s (cron/schedule-at-round-times-in-day f sync-period schedule-period)] \n" +
                        "   (sleep 16 :seconds) \n" +
                        "   (cancel s))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 3);

                sandboxFunctionCallValidation();

                final VncFunction fn = Coerce.toVncFunction(args.first());
                final Duration syncPeriod = Coerce.toVncJavaObject(args.second(), Duration.class);
                final Duration schedulePeriod = Coerce.toVncJavaObject(args.third(), Duration.class);

                fn.sandboxFunctionCallValidation();

                // Create a wrapper that inherits the Venice thread context
                // from the parent thread to the executer thread!
                final ThreadBridge threadBridge = ThreadBridge.create(
                                                    "schedule-delay",
                                                    new CallFrame[] {
                                                        new CallFrame(this, args),
                                                        new CallFrame(fn)});
                final Runnable taskWrapper = threadBridge.bridgeRunnable(() -> fn.applyOf());

                final CronTask task = (millis) -> taskWrapper.run();

                CronScheduler scheduler = CronScheduler.newBuilder(syncPeriod)
                                                       .setThreadFactory(
                                                               ThreadPoolUtil.createCountedThreadFactory(
                                                                       "cron",
                                                                       true))
                                                       .build();

                final Future<?> future = scheduler.scheduleAtRoundTimesInDay(
                                                schedulePeriod,
                                                ZoneId.systemDefault(),
                                                task);

                return new VncJavaObject(future);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };



    ///////////////////////////////////////////////////////////////////////////
    // types_ns is namespace of type functions
    ///////////////////////////////////////////////////////////////////////////

    public static final Map<VncVal, VncVal> ns =
            new SymbolMapBuilder()
                    .add(schedule_at_round_times_in_day)
                    .toMap();
}
