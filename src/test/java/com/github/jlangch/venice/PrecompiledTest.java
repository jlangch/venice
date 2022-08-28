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
package com.github.jlangch.venice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.util.StopWatch;
import com.github.jlangch.venice.util.CapturingPrintStream;


public class PrecompiledTest {

    @Test
    public void test_simple() {
        final Venice venice = new Venice();

        final PreCompiled precomp = venice.precompile("test", "(do (nil? 1) (+ 1 3))");

        assertEquals(4L, venice.eval(precomp));
    }

    @Test
    public void test_simple2() throws Exception {
        final Venice venice = new Venice();

        final String script = "(do (defn sum [a b] (+ a b z)) (sum x y))";

        final PreCompiled precomp = venice.precompile("test", script);
        assertEquals(103L, venice.eval(precomp, Parameters.of("x", 100L, "y", 1L, "z", 2L)));

//      assertEquals(103L, venice.eval(script, Parameters.of("x", 100L, "y", 1L, "z", 2L)));
    }

    @Test
    public void test_ns() throws Exception {
        final Venice venice = new Venice();

        final PreCompiled precomp = venice.precompile("test", "(do (defn x [] *ns*) (x))");

        assertEquals("user", venice.eval(precomp));
    }

    @Test
    public void test_simple_with_params() {
        final Venice venice = new Venice();

        final PreCompiled precomp = venice.precompile("test", "(do (+ x y))");

        assertEquals(300, venice.eval(precomp, Parameters.of("x", 100, "y", 200)));
        assertEquals(300L, venice.eval(precomp, Parameters.of("x", 100L, "y", 200L)));
    }

    @Test
    public void test_simple_serialize() {
        final Venice venice = new Venice();

        final PreCompiled precomp = venice.precompile("test", "(do (nil? 1) (+ 1 3))");

        final byte[] data = precomp.serialize();
        System.out.println("PreCompiled (simple) size: " + data.length);
        assertEquals(4L, venice.eval(PreCompiled.deserialize(data)));
    }

    @Test
    public void test_version() {
        final Venice venice = new Venice();

        final PreCompiled precomp = venice.precompile("test", "*version*");

        assertEquals(Venice.getVersion(), venice.eval(precomp));
    }

    @Test
    public void test_loaded_modules() {
        final Venice venice = new Venice();

        final PreCompiled precomp = venice.precompile("test", "(count (sort *loaded-modules*))");

        assertEquals(10L, venice.eval(precomp));
    }

    @Test
    public void test_stdout() {
        final Venice venice = new Venice();

        final PreCompiled precomp = venice.precompile("test", "(print 23)");

        final CapturingPrintStream ps = new CapturingPrintStream();

        venice.eval(precomp, Parameters.of("*out*", ps));

        assertEquals("23", ps.getOutput());
    }

    @Test
    public void test_with_stdout_str() {
        final Venice venice = new Venice();

        final PreCompiled precomp = venice.precompile("test", "(with-out-str (print 23))");

        final CapturingPrintStream ps = new CapturingPrintStream();

        assertEquals("23", venice.eval(precomp, Parameters.of("*out*", ps)));
    }

    @Test
    public void test_with_fn() {
        final Venice venice = new Venice();

        final PreCompiled precomp = venice.precompile("test", "(do (defn sum [x y] (+ x y)) (sum 1 3))");

        assertEquals(4L, venice.eval(precomp));
    }

    @Test
    public void test_with_fn_serialize() {
        final Venice venice = new Venice();

        final PreCompiled precomp = venice.precompile("test", "(do (defn sum [x y] (+ x y)) (sum 1 3))");

        final byte[] data = precomp.serialize();
        System.out.println("PreCompiled (defn) size: " + data.length);
        assertEquals(4L, venice.eval(PreCompiled.deserialize(data)));
    }

    @Test
    public void test_with_java_import() {
        final Venice venice = new Venice();

        final String script =
                "(do                                     \n" +
                "   (ns test)                            \n" +
                "   (import :java.awt.Point)             \n" +
                "   (. (. :Point :new 10 20) :toString))   ";

        final PreCompiled precomp = venice.precompile("test", script);


        // Note: Venice::eval will rebuild the namespace 'test' with the imports
        //       for the namespace registry while evaluating the s-expression
        //       from the precompiled AST.
        assertEquals("java.awt.Point[x=10,y=20]", venice.eval(precomp));
    }

    @Test
    public void test_with_java_import_serialize() {
        final Venice venice = new Venice();

        final String script =
                "(do                                     \n" +
                "   (ns test)                            \n" +
                "   (import :java.awt.Point)             \n" +
                "   (. (. :Point :new 10 20) :toString))   ";

        final PreCompiled precomp = venice.precompile("test", script);

        final byte[] data = precomp.serialize();

        // Note: Venice::eval will rebuild the namespace 'test' with the imports
        //       for the namespace registry while evaluating the s-expression
        //       from the precompiled AST.
        assertEquals("java.awt.Point[x=10,y=20]", venice.eval(PreCompiled.deserialize(data)));
    }

    @Test
    public void test_elapsed() {
        final Venice venice = new Venice();

        final PreCompiled precomp = venice.precompile("test", "(do (nil? 1) (+ 1 3))");

        // warmup
        for(int ii=0; ii<40_000; ii++) {
            venice.eval(precomp);
        }

        System.gc();
        final StopWatch sw = StopWatch.nanos();
        for(int ii=0; ii<10_000; ii++) {
            venice.eval(precomp);
        }
        sw.stop();

        System.out.println("Elapsed (pre-compiled, 10'000 calls): " + sw.toString());
    }

    @Test
    public void test_elapsed_with_params() {
        final Venice venice = new Venice();

        final PreCompiled precomp = venice.precompile("test", "(do (nil? 1) (+ x y))");

        // warmup
        for(int ii=0; ii<40_000; ii++) {
            venice.eval(precomp, Parameters.of("x", 100, "y", 200));
        }

        System.gc();
        final StopWatch sw = StopWatch.nanos();
        for(int ii=0; ii<10_000; ii++) {
            venice.eval(precomp, Parameters.of("x", 100, "y", 200));
        }
        sw.stop();

        System.out.println("Elapsed (pre-compiled, params, 10'000 calls): " + sw.toString());
    }

    @Test
    public void test_multi_threaded() throws Exception {
        final ExecutorService es = Executors.newFixedThreadPool(10);

        final Venice venice = new Venice();

        final PreCompiled precomp = venice.precompile(
                                        "test",
                                        "(do                          " +
                                        "  (defn sum [a b] (+ a b z)) " +
                                        "  (sleep (rand-long 50))     " +
                                        "  (sum x y))                 ");

        final List<Callable<Object>> tasks = new ArrayList<>();
        for(long ii=0; ii<2000; ii++) {
            final long count = ii;
            tasks.add(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    return venice.eval(precomp, Parameters.of("x", 100L, "y", 0L, "z", count));
                }
            });
        }

        final List<Future<Object>> results = es.invokeAll(tasks);

        assertEquals(2000, results.size());

        long resVal = 100L;
        for(Future<Object> result : results) {
            assertEquals(resVal++, result.get());
        }

        es.shutdown();
    }

    @Test
    public void test_multi_threaded_2() throws Exception {
        final ExecutorService es = Executors.newFixedThreadPool(10);

        final Venice venice = new Venice();

        final PreCompiled precomp = venice.precompile(
                                        "test",
                                        "(do                                 " +
                                        "  (defn sum [a b] (+ a b z))        " +
                                        "  (long (with-out-str               " +
                                        "           (do                      " +
                                        "             (sleep (rand-long 50)) " +
                                        "             (print (sum x y))))))  ");

        final List<Callable<Object>> tasks = new ArrayList<>();
        for(long ii=0; ii<2000; ii++) {
            final long count = ii;
            tasks.add(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    return venice.eval(precomp, Parameters.of("x", 100L, "y", 0L, "z", count));
                }
            });
        }

        final List<Future<Object>> results = es.invokeAll(tasks);

        assertEquals(2000, results.size());

        long resVal = 100L;
        for(Future<Object> result : results) {
            assertEquals(resVal++, result.get());
        }

        es.shutdown();
    }

    @Test
    public void test_expand_macros() {
        final Venice venice = new Venice();

        final PreCompiled precomp = venice.precompile("test", "(if (and true (= 1 1)) 4 0)", true);

        assertEquals(4L, venice.eval(precomp));
    }

    @Test
    public void test_remove_global_symbol_ok_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                 \n" +
                "  (ns foo)          \n" +
                "  (def x 100)       \n" +
                "  (ns goo)          \n" +
                "  (ns-unmap 'foo 'x))";

        // removing foo/x is okay, it's not part of the pre-compiled system symbols
        final PreCompiled precomp = venice.precompile("test", script, true);

        assertNull(venice.eval(precomp));
    }

    @Test
    public void test_remove_global_symbol_ok_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                 \n" +
                "  (ns foo)          \n" +
                "  (def x 100)       \n" +
                "  (ns goo)          \n" +
                "  (ns-remove 'foo))";

        // removing foo/x is okay, it's not part of the pre-compiled system symbols
        final PreCompiled precomp = venice.precompile("test", script, true);

        assertNull(venice.eval(precomp));
    }

    @Test
    public void test_remove_global_symbol_fail() {
        final Venice venice = new Venice();

        // core/+ is a sealed namespace symbol and thus cannot be removed
        final String script =
                "(do                 \n" +
                "  (ns foo)          \n" +
                "  (def x 100)       \n" +
                "  (ns-unmap 'core '+))";

        final PreCompiled precomp = venice.precompile("test", script, true);

        assertThrows(VncException.class, () -> venice.eval(precomp));
    }

    @Test
    public void test_ns_alias_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                   \n" +
                "  (ns foo)            \n" +
                "  (def x 100)         \n" +
                "  (ns bar)            \n" +
                "  (ns-alias 'f 'foo)  \n" +
                "  f/x)                ";

        final PreCompiled precomp = venice.precompile("test", script, true);

        assertEquals(100L, venice.eval(precomp));
    }

    @Test
    public void test_ns_alias_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                         \n" +
                "  (load-module :hexdump ['hexdump :as 'h])  \n" +
                "  (with-out-str (h/dump [0 1 2 3])))       ";

        // removing foo/x is okay, it's not part of the pre-compiled system symbols
        final PreCompiled precomp = venice.precompile("test", script, true);

        final Object result = venice.eval(precomp);

        assertEquals(
                "00000000: 0001 0203                                ....            \n\n",
                result);
    }
}
