/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        final IPreCompiled precomp = new Venice().precompile("test", "(do (nil? 1) (+ 1 3))");

        assertEquals(4L, new Venice().eval(precomp));
    }

    @Test
    public void test_simple2() throws Exception {
        final String script = "(do (defn sum [a b] (+ a b z)) (sum x y))";

        final IPreCompiled precomp = new Venice().precompile("test", script);

        assertEquals(103L, new Venice().eval(precomp, Parameters.of("x", 100L, "y", 1L, "z", 2L)));
    }


    @Test
    public void test_run_mode() {
        final IPreCompiled precomp = new Venice().precompile("test", "*run-mode*");

        assertEquals("script", new Venice().eval(precomp));
    }

    @Test
    public void test_ns() throws Exception {
        final IPreCompiled precomp = new Venice().precompile("test", "(do (defn x [] *ns*) (x))");

        assertEquals("user", new Venice().eval(precomp));
    }

    @Test
    public void test_simple_with_params() {
        final IPreCompiled precomp = new Venice().precompile("test", "(do (+ x y))");

        assertEquals(300, new Venice().eval(precomp, Parameters.of("x", 100, "y", 200)));
        assertEquals(300L, new Venice().eval(precomp, Parameters.of("x", 100L, "y", 200L)));
    }

    @Test
    public void test_version() {
        final IPreCompiled precomp = new Venice().precompile("test", "*version*");

        assertEquals(Venice.getVersion(), new Venice().eval(precomp));
    }

    @Test
    public void test_loaded_modules() {
        final IPreCompiled precomp = new Venice().precompile("test", "(count (sort *loaded-modules*))");

        assertEquals(12L, new Venice().eval(precomp));
    }

    @Test
    public void test_stdout() {
        final IPreCompiled precomp = new Venice().precompile("test", "(print 23)");

        final CapturingPrintStream ps = new CapturingPrintStream();

        new Venice().eval(precomp, Parameters.of("*out*", ps));

        assertEquals("23", ps.getOutput());
    }

    @Test
    public void test_with_stdout_str() {
        final IPreCompiled precomp = new Venice().precompile("test", "(with-out-str (print 23))");

        final CapturingPrintStream ps = new CapturingPrintStream();

        assertEquals("23", new Venice().eval(precomp, Parameters.of("*out*", ps)));
    }

    @Test
    public void test_with_fn() {
        final IPreCompiled precomp = new Venice().precompile("test", "(do (defn sum [x y] (+ x y)) (sum 1 3))");

        assertEquals(4L, new Venice().eval(precomp));
    }

    @Test
    public void test_with_java_import() {
        final String script =
                "(do                                     \n" +
                "   (ns test)                            \n" +
                "   (import :java.awt.Point)             \n" +
                "   (. (. :Point :new 10 20) :toString))   ";

        final IPreCompiled precomp = new Venice().precompile("test", script);


        // Note: Venice::eval will rebuild the namespace 'test' with the imports
        //       for the namespace registry while evaluating the s-expression
        //       from the precompiled AST.
        assertEquals("java.awt.Point[x=10,y=20]", new Venice().eval(precomp));
    }

    @Test
    public void test_elapsed_no_precompiled() {
        final Venice venice = new Venice();

        // warmup
        for(int ii=0; ii<2_000; ii++) {
            venice.eval("(do (nil? 1) (+ 1 3))");
        }

        System.gc();
        final StopWatch sw = StopWatch.nanos();
        for(int ii=0; ii<1_000; ii++) {
            venice.eval("(do (nil? 1) (+ 1 3))");
        }
        sw.stop();

        System.out.println("Elapsed (not pre-compiled, 1'000 calls): " + sw.toString());
    }

    @Test
    public void test_elapsed_precompiled() {
        final IPreCompiled precomp = new Venice().precompile("test", "(do (nil? 1) (+ 1 3))", true);

        Venice venice = new Venice();

        // warmup
        for(int ii=0; ii<2_000; ii++) {
            venice.eval(precomp);
        }

        venice = new Venice();

        System.gc();
        final StopWatch sw = StopWatch.nanos();
        for(int ii=0; ii<1_000; ii++) {
            venice.eval(precomp);
        }
        sw.stop();

        System.out.println("Elapsed (pre-compiled, 1'000 calls): " + sw.toString());
    }

    @Test
    public void test_elapsed_precompiled_with_params() {
        final IPreCompiled precomp = new Venice().precompile("test", "(do (nil? 1) (+ x y))", true);

        Venice venice = new Venice();

        // warmup
        for(int ii=0; ii<2_000; ii++) {
            venice.eval(precomp, Parameters.of("x", 100, "y", 200));
        }

        venice = new Venice();

        System.gc();
        final StopWatch sw = StopWatch.nanos();
        for(int ii=0; ii<1_000; ii++) {
            venice.eval(precomp, Parameters.of("x", 100, "y", 200));
        }
        sw.stop();

        System.out.println("Elapsed (pre-compiled, params, 1'000 calls): " + sw.toString());
    }

    @Test
    public void test_multi_threaded() throws Exception {
        final ExecutorService es = Executors.newFixedThreadPool(10);

        final IPreCompiled precomp = new Venice().precompile(
                                        "test",
                                        "(do                          " +
                                        "  (defn sum [a b] (+ a b z)) " +
                                        "  (sleep (rand-long 50))     " +
                                        "  (sum x y))                 ",
                                        true);

        final Venice venice = new Venice();

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

        final IPreCompiled precomp = new Venice().precompile(
                                        "test",
                                        "(do                                 " +
                                        "  (defn sum [a b] (+ a b z))        " +
                                        "  (long (with-out-str               " +
                                        "           (do                      " +
                                        "             (sleep (rand-long 50)) " +
                                        "             (print (sum x y))))))  ",
                                        true);

        final Venice venice = new Venice();

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
        final IPreCompiled precomp = new Venice().precompile("test", "(if (and true (= 1 1)) 4 0)", true);

        assertEquals(4L, new Venice().eval(precomp));
    }

    @Test
    public void test_expand_macros_flag() {
        final IPreCompiled precomp1 = new Venice().precompile("test", "(macroexpand-on-load?)", false);
        final IPreCompiled precomp2 = new Venice().precompile("test", "(macroexpand-on-load?)", true);

        assertFalse((Boolean)new Venice().eval(precomp1));
        assertTrue((Boolean)new Venice().eval(precomp2));
    }

    @Test
    public void test_expand_macros_in_loaded_module_function_1a() {
        final String script =
                "(do                                    \n" +
                "  (load-module :test-support)          \n" +
                "  (test-support/test-body-with-macro)) ";

        // removing foo/x is okay, it's not part of the pre-compiled system symbols
        final IPreCompiled precomp = new Venice().precompile("test", script, true);

        assertEquals(1L, new Venice().eval(precomp));
    }

    @Test
    public void test_expand_macros_in_loaded_module_function_1b() {
        final String script =
                "(do                                                     \n" +
                "  (load-module :test-support)                           \n" +
                "  (test-support/test-body-with-macro)                   \n" +
                "  (pr-str (fn-body test-support/test-body-with-macro))) ";

        // removing foo/x is okay, it's not part of the pre-compiled system symbols
        final IPreCompiled precomp = new Venice().precompile("test", script, true);

        assertEquals("((if true (do 1)))", new Venice().eval(precomp));
    }

    @Test
    public void test_expand_macros_in_loaded_module_macro_1a() {
        final String script =
                "(do                                         \n" +
                "  (load-module :test-support)               \n" +
                "  (defn plus [x y] (test-support/sum x y))  \n" +
                "  (plus 1 2))                               ";

        // removing foo/x is okay, it's not part of the pre-compiled system symbols
        final IPreCompiled precomp = new Venice().precompile("test", script, true);

        assertEquals(3L, new Venice().eval(precomp));
    }

    @Test
    public void test_expand_macros_in_loaded_module_macro_1b() {
        final String script =
                "(do                                         \n" +
                "  (load-module :test-support)               \n" +
                "  (defn plus [x y] (test-support/sum x y))  \n" +
                "  (pr-str (fn-body plus))))                  ";

        // removing foo/x is okay, it's not part of the pre-compiled system symbols
        final IPreCompiled precomp = new Venice().precompile("test", script, true);

        assertEquals("((+ x y))", new Venice().eval(precomp));
    }

    @Test
    public void test_remove_global_symbol_ok_1() {
        final String script =
                "(do                 \n" +
                "  (ns foo)          \n" +
                "  (def x 100)       \n" +
                "  (ns goo)          \n" +
                "  (ns-unmap 'foo 'x))";

        // removing foo/x is okay, it's not part of the pre-compiled system symbols
        final IPreCompiled precomp = new Venice().precompile("test", script, true);

        assertNull(new Venice().eval(precomp));
    }

    @Test
    public void test_remove_global_symbol_ok_2() {
        final String script =
                "(do                 \n" +
                "  (ns foo)          \n" +
                "  (def x 100)       \n" +
                "  (ns goo)          \n" +
                "  (ns-remove 'foo))";

        // removing foo/x is okay, it's not part of the pre-compiled system symbols
        final IPreCompiled precomp = new Venice().precompile("test", script, true);

        assertNull(new Venice().eval(precomp));
    }

    @Test
    public void test_remove_global_symbol_fail() {
        // core/+ is a sealed namespace symbol and thus cannot be removed
        final String script =
                "(do                 \n" +
                "  (ns foo)          \n" +
                "  (def x 100)       \n" +
                "  (ns-unmap 'core '+))";

        final IPreCompiled precomp = new Venice().precompile("test", script, true);

        assertThrows(VncException.class, () -> new Venice().eval(precomp));
    }

    @Test
    public void test_ns_alias_1() {
        final String script =
                "(do                   \n" +
                "  (ns foo)            \n" +
                "  (def x 100)         \n" +
                "  (ns bar)            \n" +
                "  (ns-alias 'f 'foo)  \n" +
                "  f/x)                ";

        final IPreCompiled precomp = new Venice().precompile("test", script, true);

        assertEquals(100L, new Venice().eval(precomp));
    }

    @Test
    public void test_ns_alias_2a() {
        final String script =
                "(do                                         \n" +
                "  (load-module :hexdump)                    \n" +
                "  (with-out-str (hexdump/dump [0 1 2 3])))       ";

        final IPreCompiled precomp = new Venice().precompile("test", script, false);

        final Object result = new Venice().eval(precomp);

        assertEquals(
                "00000000: 0001 0203                                ....            \n\n",
                result);
    }

    @Test
    public void test_ns_alias_2b() {
        final String script =
                "(do                                         \n" +
                "  (load-module :hexdump ['hexdump :as 'h])  \n" +
                "  (with-out-str (h/dump [0 1 2 3])))       ";

        // Note
        final IPreCompiled precomp = new Venice().precompile("test", script, true);

        final Object result = new Venice().eval(precomp);

        assertEquals(
                "00000000: 0001 0203                                ....            \n\n",
                result);
    }

    @Test
    public void test_ns_alias_2c() {
        final String script =
                "(do                                         \n" +
                "  (ns foo)                                  \n" +
                "  (load-module :hexdump ['hexdump :as 'h])  \n" +
                "  *ns*)       ";

        // Note
        final IPreCompiled precomp = new Venice().precompile("test", script, true);

        final Object result = new Venice().eval(precomp);

        assertEquals("foo", result);
    }

    @Test
    public void test_ns_alias_2d() {
        final String script =
                "(do                                         \n" +
                "  (ns foo)                                  \n" +
                "  (load-module :hexdump ['hexdump :as 'h])  \n" +
                "  (pr-str (ns-aliases)))                    ";

        // Note
        final IPreCompiled precomp = new Venice().precompile("test", script, true);

        final Object result = new Venice().eval(precomp);

        assertEquals("{h hexdump}", result);
    }

    @Test
    public void test_ns_alias_3b() {
        final String script =
                "(do                                           \n" +
                "  (load-module :test-support ['test :as 't])  \n" +
                "  (test-support/add 1 2))                     ";

        final IPreCompiled precomp = new Venice().precompile("test", script, false);

        final Object result = new Venice().eval(precomp);

        assertEquals(3L, result);
    }
}
