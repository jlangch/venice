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
package com.github.jlangch.venice.impl.specialforms.util;

import java.util.ArrayList;
import java.util.List;

import com.github.jlangch.venice.impl.IFormEvaluator;
import com.github.jlangch.venice.impl.InterruptChecker;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJust;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;


public class Benchmark {

    public static VncList benchmark(
            final long warmUpIterations,
            final long gcRuns,
            final long iterations,
            final VncVal expr,
            final VncFunction statusFn,
            final Env env,
            final IFormEvaluator evaluator
    ) {
        try {
            // warmup
            statusFn.applyOf(new VncString("Warmup..."));
            final VncList warmupSamples = samples(warmUpIterations, expr, env, evaluator);
            storeToHole(warmupSamples);

            long batchSize = 1;

//            final VncList sortedUp = (VncList)CoreFunctions.sort.applyOf(warmupSamples);
//            final VncList samples = (VncList)TransducerFunctions.take.applyOf(
//                                                new VncLong(Math.min(100, sortedUp.size())),
//                                                sortedUp);
//            final long warmupElapsed = ((VncNumber)MathFunctions.mean.apply(samples)).toJavaLong();
//            if (warmupElapsed < 1000) batchSize = 1000;
//            else if (warmupElapsed < 10000) batchSize = 100;

            // GC
            statusFn.applyOf(new VncString("GC..."));
            runGCs(gcRuns);
            sleep(1000L);

            // measure overhead
            final long overheadPerSample = measureSampleOverhead();
            runGCs(1);

            // sampling
            statusFn.applyOf(new VncString("Sampling..."));
            final VncList iterationSamples = samples(iterations, batchSize, overheadPerSample, expr, env, evaluator);
            return iterationSamples;
        }
        finally {
            ThreadContext.removeValue(benchVal);
        }
    }

    private static VncList samples(
            final long iterations,
            final VncVal expr,
            final Env env,
            final IFormEvaluator evaluator
    ) {
        return samples(iterations, 1L, 0L, expr, env, evaluator);
    }

    private static VncList samples(
            final long iterations,
            final long batchSize,
            final long overheadPerSample,
            final VncVal expr,
            final Env env,
            final IFormEvaluator evaluator
    ) {
        if (batchSize == 1) {
            final List<VncVal> elapsed = new ArrayList<>();
            for(int ii=0; ii<iterations; ii++) {
                final long elapsed_ = sample(1, expr, env, evaluator);

                elapsed.add(new VncLong(Math.max(0, elapsed_ - overheadPerSample)));

                InterruptChecker.checkInterrupted(Thread.currentThread(), "dobench");
            }

            return VncList.ofList(elapsed);
        }
        else {
            final List<VncVal> elapsed = new ArrayList<>();
            final long batchedIterations = iterations/batchSize + 1L;
            for(int ii=0; ii<batchedIterations; ii++) {
                final long elapsed_ = sample(batchSize, expr, env, evaluator);

                final long e = Math.max(1, (elapsed_ - overheadPerSample) / batchSize);
                for(int jj=0; jj<batchSize; jj++) {
                    elapsed.add(new VncLong(e));
                }

                InterruptChecker.checkInterrupted(Thread.currentThread(), "dobench");
            }

            return VncList.ofList(elapsed);
        }
    }

    private static long sample(
            final long n,
            final VncVal expr,
            final Env env,
            final IFormEvaluator evaluator
    ) {
        final long start = System.nanoTime();

        VncVal result = Constants.Nil;

        if (n == 1) {
            result = evaluator.evaluate(expr, env, false);
        }
        else {
            for(int ii=0; ii<n; ii++) {
                result = evaluator.evaluate(expr, env, false);
            }
        }

        final long elapsed = System.nanoTime() - start;

        // Store value to a mutable place to prevent JIT from optimizing
        // too much. Wrap the result so a VncStack can be used as result
        // too (VncStack is a special value in ThreadLocalMap)
        storeToHole(new VncJust(result));

        return elapsed;
    }

    private static long measureSampleOverhead() {
        long elapsedTotal = 0L;

        for(int ii=0; ii<10_000; ii++) {
            final long start_ = System.nanoTime();

            final VncLong result = (VncLong)dummyFn.apply(VncList.empty());

            final long elapsed = System.nanoTime() - start_;

            // Store value to a mutable place to prevent JIT from optimizing
            // too much. Wrap the result so a VncStack can be used as result
            // too (VncStack is a special value in ThreadLocalMap)
            storeToHole(new VncLong(elapsed - start_ + result.toJavaLong()));

            elapsedTotal += elapsed;
        }

        return elapsedTotal / 10_000;
    }

    private static void sleep(final long millis) {
        try { Thread.sleep(millis); } catch(Exception ex) {}
    }

    private static void runGCs(final long count) {
        for(int ii=0; ii<count; ii++) {
            System.runFinalization();
            System.gc();
        }
    }

    private static void storeToHole(final VncVal v) {
        // Store value to a mutable place to prevent JIT from optimizing
        // too much. Wrap the result so a VncStack can be used as result
        // too (VncStack is a special value in ThreadLocalMap)
        ThreadContext.setValue(benchVal, v);
    }

    private static VncFunction dummyFn =
        new VncFunction("dummy___") {
            @Override
            public VncVal apply(final VncList args) { return zero; }
            private static final long serialVersionUID = -1L;
        };


    private static final VncLong zero = new VncLong(0);
    private static final VncKeyword benchVal = new VncKeyword("*benchmark-val*");
}
