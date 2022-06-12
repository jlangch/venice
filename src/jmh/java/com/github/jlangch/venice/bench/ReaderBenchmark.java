/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2021 Venice
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
package com.github.jlangch.venice.bench;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

import com.github.jlangch.venice.impl.ModuleLoader;
import com.github.jlangch.venice.impl.reader.Reader;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.util.StringUtil;


@Warmup(iterations=3, time=3, timeUnit=TimeUnit.SECONDS)
@Measurement(iterations=3, time=10, timeUnit=TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode (Mode.AverageTime)
@OutputTimeUnit (TimeUnit.MILLISECONDS)
@State (Scope.Benchmark)
@Threads (1)
public class ReaderBenchmark {

    public ReaderBenchmark() {
        System.out.print("(Core lines: " + StringUtil.splitIntoLines(core).size() + ") ");
    }

    /* ------------------------------------------------------------------------
     *  Venice 1.8.5 (08.06.2020)
     *
     *     Test system:            2017 MacBook Pro (Mac OSX, Core i7 2.8 GHz)
     *                             Java 8 server VM
     *
     *     Benchmark               Mode  Cnt  Score   Error  Units
     *     ReaderBenchmark.reader  avgt    3  1.278 ± 0.069  ms/op
     *
     *
     *
     *  Venice 1.8.4 (05.06.2020)
     *
     *     Test system:            2017 MacBook Pro (Mac OSX, Core i7 2.8 GHz)
     *                             Java 8 server VM
     *
     *     Benchmark               Mode  Cnt  Score   Error  Units
     *     ReaderBenchmark.reader  avgt    3  1.685 ± 0.411  ms/op
     *
     *
     *
     *  Venice 1.8.0 (05.06.2020)
     *
     *     Test system:            2017 MacBook Pro (Mac OSX, Core i7 2.8 GHz)
     *                             Java 8 server VM
     *
     *     Benchmark               Mode  Cnt  Score   Error  Units
     *     ReaderBenchmark.reader  avgt    3  2.961 ± 1.019  ms/op
     *
     * ------------------------------------------------------------------------ */
    @Benchmark
    public VncVal reader() {
        return Reader.read_str(core, "core");
    }


    private final String core = "(do\n" + ModuleLoader.loadModule("core") + "\n)";
}
