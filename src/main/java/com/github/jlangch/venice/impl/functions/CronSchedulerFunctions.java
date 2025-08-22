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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.github.jlangch.venice.impl.thread.ThreadBridge;
import com.github.jlangch.venice.impl.threadpool.ThreadPoolUtil;
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

import io.timeandspace.cronscheduler.CronScheduler;
import io.timeandspace.cronscheduler.CronTask;


public class CronSchedulerFunctions {

    public static VncFunction schedule_at =
            new VncFunction(
                    "cron/schedule-at",
                    VncFunction
                        .meta()
                        .arglists(
                            "(schedule-at-round-times-in-day fn sync-period schedule-at)")
                        .doc(
                            "Creates and executes a one-shot scheduled task that becomes enabled " +
                            "at a given time.\n\n" +
                            "This scheduled task is not prone to clock shifts.\n\n" +
                            "Returns a future. `(deref f)`, `(future? f)`, `(cancel f)`, " +
                            "and `(done? f)` will work on the returned future.¶" +
                            "\n\n" +
                            "This function is built on the [CronScheduler](https://github.com/TimeAndSpaceIO/CronScheduler) " +
                            "project.")
                        .examples(
                            "(let [sync-period     (. :java.time.Duration :ofMinutes 10)                  \n" +
                            "      at              (time/plus (time/local-date-time) :seconds 2)          \n" +
                            "      task            (fn [] (println \"Task:\" (time/local-date-time)) 100) \n" +
                            "      sched           (cron/schedule-at task sync-period at)]                \n" +
                            "   (deref sched))                                                            ")
                        .seeAlso(
                            "cron/schedule-at-fixed-rate",
                            "cron/schedule-at-round-times-in-day")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertArity(this, args, 3);

                    sandboxFunctionCallValidation();

                    final VncFunction fn = Coerce.toVncFunction(args.first());
                    final Duration syncPeriod = Coerce.toVncJavaObject(args.second(), Duration.class);
                    final LocalDateTime scheduleAt = Coerce.toVncJavaObject(args.third(), LocalDateTime.class);

                    fn.sandboxFunctionCallValidation();

                    // Create a wrapper that inherits the Venice thread context
                    // from the parent thread to the executer thread!
                    final ThreadBridge threadBridge = ThreadBridge.create(
                                                        "cron/schedule-at",
                                                        new CallFrame[] {
                                                            new CallFrame(this, args),
                                                            new CallFrame(fn)});
                    final Callable<VncVal> taskWrapper = threadBridge.bridgeCallable(() -> fn.applyOf());

                    final CronScheduler scheduler = createCronScheduler(syncPeriod);

                    final ZoneId zoneId = ZoneId.systemDefault();

                    final Instant instant = scheduleAt.atZone(zoneId).toInstant();

                    final Future<VncVal> future = scheduler.scheduleAt(instant, taskWrapper);
                    return new VncJavaObject(future);
                }

                private static final long serialVersionUID = -1848883965231344442L;
            };


    public static VncFunction schedule_at_round_times_in_day =
        new VncFunction(
                "cron/schedule-at-round-times-in-day",
                VncFunction
                    .meta()
                    .arglists(
                        "(schedule-at-round-times-in-day fn sync-period schedule-period)",
                        "(schedule-at-round-times-in-day fn sync-period schedule-period skipping-to-latest)")
                    .doc(
                        "Submits a periodic task that becomes enabled at round clock times within " +
                        "a day, with the given period.                                            " +
                        "\n\n" +
                        "This scheduled task is not prone to clock shifts.                        " +
                        "\n\n" +
                        "Returns a future. `(deref f)`, `(future? f)`, `(cancel f)`,              " +
                        "and `(done? f)` will work on the returned future.                        " +
                        "\n\n" +
                        "This function is built on the [CronScheduler](https://github.com/TimeAndSpaceIO/CronScheduler) " +
                        "project.")
                    .examples(
                        "(let [sync-period     (. :java.time.Duration :ofMinutes 10)                                   \n" +
                        "      schedule-period (. :java.time.Duration :ofHours 4)                                      \n" +
                        "      task            (fn [] (println \"Task:\" (time/local-date-time)))                      \n" +
                        "      sched           (cron/schedule-at-round-times-in-day task sync-period schedule-period)] \n" +
                        "   (sleep 24 :hours)                                                                          \n" +
                        "   (cancel sched))                                                                            ")
                    .seeAlso(
                        "cron/schedule-at-fixed-rate",
                        "cron/schedule-at")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 3, 4);

                sandboxFunctionCallValidation();

                final VncFunction fn = Coerce.toVncFunction(args.first());
                final Duration syncPeriod = Coerce.toVncJavaObject(args.second(), Duration.class);
                final Duration schedulePeriod = Coerce.toVncJavaObject(args.third(), Duration.class);
                final boolean skipToLatest = args.size() > 3 ? Coerce.toVncBoolean(args.fourth()).getValue() : true;

                fn.sandboxFunctionCallValidation();

                // Create a wrapper that inherits the Venice thread context
                // from the parent thread to the executer thread!
                final ThreadBridge threadBridge = ThreadBridge.create(
                                                    "cron/schedule-at-round-times-in-day",
                                                    new CallFrame[] {
                                                        new CallFrame(this, args),
                                                        new CallFrame(fn)});
                final Runnable taskWrapper = threadBridge.bridgeRunnable(
                                                    () -> { try { fn.applyOf(); }
                                                            catch(Exception ignore) { };
                                                          } );

                final CronTask task = (millis) -> taskWrapper.run();

                final CronScheduler scheduler = createCronScheduler(syncPeriod);

                final Future<?> future = skipToLatest
                                            ? scheduler.scheduleAtRoundTimesInDaySkippingToLatest(
                                                schedulePeriod,
                                                ZoneId.systemDefault(),
                                                task)
                                            : scheduler.scheduleAtRoundTimesInDay(
                                                schedulePeriod,
                                                ZoneId.systemDefault(),
                                                task);
                return new VncJavaObject(future);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction schedule_at_fixed_rate =
        new VncFunction(
                "cron/schedule-at-fixed-rate",
                VncFunction
                    .meta()
                    .arglists(
                        "(schedule-at-fixed-rate fn sync-period initial-delay period time-unit)",
                        "(schedule-at-fixed-rate fn sync-period initial-delay period time-unit skipping-to-latest)")
                    .doc(
                        "Creates and executes a periodic action that becomes enabled first " +
                        "after the given initial delay, and subsequently with the given " +
                        "period." +
                        "\n\n" +
                        "This scheduled task is not prone to clock shifts. " +
                        "\n\n" +
                        "Returns a future. `(deref f)`, `(future? f)`, `(cancel f)`, " +
                        "and `(done? f)` will work on the returned future." +
                        "\n\n" +
                        "This function is built on the " +
                        "[CronScheduler](https://github.com/TimeAndSpaceIO/CronScheduler) " +
                        "project.")
                    .examples(
                        "(let [sync-period (. :java.time.Duration :ofMinutes 10)                        \n" +
                        "      task        (fn [] (println \"Task:\" (time/local-date-time)))           \n" +
                        "      sched       (cron/schedule-at-fixed-rate task sync-period 1 2 :seconds)] \n" +
                        "   (sleep 16 :seconds)                                                         \n" +
                        "   (cancel sched))                                                             ")
                    .seeAlso(
                        "cron/schedule-at-round-times-in-day",
                        "cron/schedule-at")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 5, 6);

                sandboxFunctionCallValidation();

                final VncFunction fn = Coerce.toVncFunction(args.nth(0));
                final Duration syncPeriod = Coerce.toVncJavaObject(args.nth(1), Duration.class);
                final VncLong delay = Coerce.toVncLong(args.nth(2));
                final VncLong period = Coerce.toVncLong(args.nth(3));
                final VncKeyword unit = Coerce.toVncKeyword(args.nth(4));
                final boolean skipToLatest = args.size() > 5 ? Coerce.toVncBoolean(args.nth(5)).getValue() : true;

                fn.sandboxFunctionCallValidation();

                // Create a wrapper that inherits the Venice thread context
                // from the parent thread to the executer thread!
                final ThreadBridge threadBridge = ThreadBridge.create(
                                                    "cron/schedule-at-fixed-rate",
                                                    new CallFrame[] {
                                                        new CallFrame(this, args),
                                                        new CallFrame(fn)});
                final Runnable taskWrapper = threadBridge.bridgeRunnable(
                                                    () -> { try { fn.applyOf(); }
                                                            catch(Exception ignore) { };
                                                          } );

                final CronTask task = (millis) -> taskWrapper.run();

                final CronScheduler scheduler = createCronScheduler(syncPeriod);

                final Future<?> future = skipToLatest
                                            ? scheduler.scheduleAtFixedRateSkippingToLatest(
                                                delay.getValue(),
                                                period.getValue(),
                                                TimeUnitUtil.toTimeUnit(unit),
                                                task)
                                            : scheduler.scheduleAtFixedRate(
                                                delay.getValue(),
                                                period.getValue(),
                                                TimeUnitUtil.toTimeUnit(unit),
                                                task);
                return new VncJavaObject(future);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    private static CronScheduler createCronScheduler(final Duration syncPeriod) {
        return CronScheduler
                    .newBuilder(syncPeriod)
                    .setThreadFactory(
                           ThreadPoolUtil.createCountedThreadFactory(
                                   "cron-scheduler",
                                   true))
                    .build();
    }


    ///////////////////////////////////////////////////////////////////////////
    // types_ns is namespace of type functions
    ///////////////////////////////////////////////////////////////////////////

    public static final Map<VncVal, VncVal> ns =
            new SymbolMapBuilder()
                    .add(schedule_at)
                    .add(schedule_at_round_times_in_day)
                    .add(schedule_at_fixed_rate)
                    .toMap();
}
