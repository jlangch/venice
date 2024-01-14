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
package com.github.jlangch.venice.impl;

import static com.github.jlangch.venice.impl.util.StringUtil.to_lf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.PrintStream;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;
import com.github.jlangch.venice.javainterop.IInterceptor;


public class NamespaceTest {

    @Test
    public void test_ns_1() {
        final Venice venice = new Venice();

        assertEquals("user", venice.eval("*ns*"));
    }

    @Test
    public void test_ns_2() {
        final Venice venice = new Venice();

        assertEquals("A", venice.eval("(ns A)"));
    }

    @Test
    public void test_ns_3() {
        final Venice venice = new Venice();

        assertEquals("A", venice.eval("(do (ns (symbol \"A\")) *ns*)"));
        assertEquals("B", venice.eval("(do (ns (symbol :B)) *ns*)"));
    }

    @Test
    public void test_ns_4() {
        final Venice venice = new Venice();

        assertEquals("B", venice.eval("(do (ns A) (ns B) *ns*)"));
    }

    @Test
    public void test_ns_5() {
        final Venice venice = new Venice();

        final String script =
                "(do                                      \n" +
                "   (with-out-str                         \n" +
                "     (ns alpha)                          \n" +
                "     (println *ns*)                      \n" +
                "     (let [temp-ns (name *ns*)]          \n" +
                "       (ns beta)                         \n" +
                "       (println *ns*)                    \n" +
                "       (ns (symbol temp-ns))             \n" +
                "       (println *ns*))))                   ";

        assertEquals("alpha\nbeta\nalpha\n", to_lf(venice.eval(script)));
    }

    @Test
    public void test_ns_6a() {
        final Venice venice = new Venice();

        final String script =
                "(do                                       \n" +
                "  (ns foo)                                \n" +
                "  (defn foo/*  [x y] (core/* x y 2))      \n" +
                "  (foo/* 3 4))                              ";

        assertEquals(24L, venice.eval(script));
    }

    @Test
    public void test_ns_6b() {
        final Venice venice = new Venice();

        final String script =
                "(do                                       \n" +
                "  (ns foo)                                \n" +
                "  (defn foo/*  [x y] (core/* x y 2))      \n" +
                "  (* 3 4))  ;; calling foo/* !!             ";

        assertEquals(24L, venice.eval(script));
    }

    @Test
    public void test_ns_6c() {
        final Venice venice = new Venice();

        final String script =
                "(do                                       \n" +
                "  (ns foo)                                \n" +
                "  (defn foo/*  [x y] (core/* x y 2))      \n" +
                "  (ns test)                               \n" +
                "  (foo/* 3 4))                              ";

        assertEquals(24L, venice.eval(script));
    }

    @Test
    public void test_ns_6d() {
        final Venice venice = new Venice();

        final String script =
                "(do                                       \n" +
                "  (ns foo)                                \n" +
                "  (defn foo/*  [x y] (ns xxx) (* x y 2))  \n" +
                "  (foo/* 3 4))                              ";

        assertEquals(24L, venice.eval(script));
    }

    @Test
    public void test_namespace_in_function_evaluation() {
        // Functions are evaluated in the namespace they are defined!

        final Venice venice = new Venice();

        final String script =
                "(do                                      \n" +
                "   (with-out-str                         \n" +
                "     (ns alpha)                          \n" +
                "     (defn x-alpha [] (println *ns*))    \n" +
                "                                         \n" +
                "     (ns beta)                           \n" +
                "     (defn x-beta [] (println *ns*))     \n" +
                "                                         \n" +
                "     (alpha/x-alpha)                     \n" +
                "     (x-beta)                            \n" +
                "     (beta/x-beta)                       \n" +
                "                                         \n" +
                "     (ns gamma)                          \n" +
                "     (alpha/x-alpha)                     \n" +
                "     (beta/x-beta)))                       ";

        assertEquals("alpha\nbeta\nbeta\nalpha\nbeta\n", to_lf(venice.eval(script)));
    }

    @Test
    public void test_namespace_in_macro_evaluation_runtime_1() {
        // Macros are evaluated in the namespace they are called from!

        final Venice venice = new Venice();

        final String script =
                "(do                                        \n" +
                "   (with-out-str                           \n" +
                "     (ns alpha)                            \n" +
                "                                           \n" +
                "     (defmacro whenn [test form]           \n" +
                "       (do                                 \n" +
                "         (println *ns*)                    \n" +
                "         `(if ~test ~form nil)))           \n" +
                "                                           \n" +
                "     (ns beta)                             \n" +
                "                                           \n" +
                "     (do                                   \n" +
                "       (ns gamma)                          \n" +
                "       (alpha/whenn true (println 100))    \n" +
                "       (ns delta)                          \n" +
                "       (alpha/whenn true (println 100)))))   ";

        assertEquals("gamma\n100\ndelta\n100\n", to_lf(venice.eval(script)));
    }

    @Test
    public void test_namespace_in_macro_evaluation_runtime_2() {
        // Macros are evaluated in the namespace they are called from!

        ThreadContext.remove(); // clean thread locals

        final IInterceptor interceptor = new AcceptAllInterceptor();
        final VeniceInterpreter venice = new VeniceInterpreter(interceptor);

        final boolean macroexpandOnLoad = false;

        final Env env = venice.createEnv(macroexpandOnLoad, false, RunMode.SCRIPT)
                              .setStdoutPrintStream(new PrintStream(System.out, true));

        final String macros =
                "(do                                             \n" +
                "  (ns alpha)                                    \n" +
                "                                                \n" +
                "  (defmacro whenn [test form]                   \n" +
                "    (do                                         \n" +
                "      (println *ns*)                            \n" +
                "      `(if ~test ~form nil))))                    ";

        venice.RE(macros, "test", env);


        // [2]
        final String ns = "(ns beta)";
        venice.RE(ns, "test", env);


        // [3]
        final String script =
                "(do                                             \n" +
                "   (with-out-str                                \n" +
                "     (do                                        \n" +
                "       (ns gamma)                               \n" +
                "       (alpha/whenn true (println 100))         \n" +
                "       (ns delta)                               \n" +
                "       (alpha/whenn true (println 100))))))       ";

        final VncVal result2 = venice.RE(script, "test", env);

        assertEquals("gamma\n100\ndelta\n100\n", to_lf(result2.toString()));
    }

    @Test
    public void test_namespace_in_macro_evaluation_runtime_3() {
        // Macros are evaluated in the namespace they are called from!

        ThreadContext.remove(); // clean thread locals

        final IInterceptor interceptor = new AcceptAllInterceptor();
        final VeniceInterpreter venice = new VeniceInterpreter(interceptor);

        final boolean macroexpandOnLoad = false;

        final Env env = venice.createEnv(macroexpandOnLoad, false, RunMode.SCRIPT)
                              .setStdoutPrintStream(new PrintStream(System.out, true));

        // [1]
        final String macros =
                "(do                                             \n" +
                "  (ns alpha)                                    \n" +
                "                                                \n" +
                "  (defmacro whenn [test form]                   \n" +
                "    (do                                         \n" +
                "      (println *ns*)                            \n" +
                "      `(if ~test ~form nil))))                    ";

        venice.RE(macros, "test", env);


        // [2]
        final String ns = "(ns beta)";
        venice.RE(ns, "test", env);


        // [3]
        final String script =
                "(do                                             \n" +
                "   (with-out-str                                \n" +
                "     (macroexpand-all                           \n" +
                "       '(do                                     \n" +
                "          (ns gamma)                            \n" +
                "          (alpha/whenn true (println 100))      \n" +
                "          (ns delta)                            \n" +
                "          (alpha/whenn true (println 100))))))   ";

        final VncVal result2 = venice.RE(script, "test", env);

        // Note: the output from script [3] "100\n100\n" is not captured
        //       because 'macroexpand-all' just expands the macros but
        //       does not execute the expanded code!
        assertEquals("gamma\ndelta\n", to_lf(result2.toString()));
    }

    @Test
    public void test_namespace_in_macro_evaluation_upfront_1() {
        // Macros are evaluated in the namespace they are called from!

        ThreadContext.remove(); // clean thread locals

        final IInterceptor interceptor = new AcceptAllInterceptor();
        final VeniceInterpreter venice = new VeniceInterpreter(interceptor);

        final boolean macroexpandOnLoad = true;

        final Env env = venice.createEnv(macroexpandOnLoad, false, RunMode.SCRIPT)
                              .setStdoutPrintStream(new PrintStream(System.out, true));

        // [1]
        final String ns1 = "(ns alpha)";
        venice.RE(ns1, "test", env);


        // [2]
        final String macros =
                "(defmacro whenn [test form]                   \n" +
                "  (do                                         \n" +
                "    (println *ns*)                            \n" +
                "    `(if ~test ~form nil)))                    ";

        venice.RE(macros, "test", env);


        // [3]
        final String ns2 = "(ns beta)";
        venice.RE(ns2, "test", env);


        // [4]
        final String script =
                "(do                                             \n" +
                "   (with-out-str                                \n" +
                "     (macroexpand-all                           \n" +
                "       '(do                                     \n" +
                "          (ns gamma)                            \n" +
                "          (alpha/whenn true (println 100))      \n" +
                "          (ns delta)                            \n" +
                "          (alpha/whenn true (println 100))))))   ";

        final VncVal result2 = venice.RE(script, "test", env);

        // stdout:  "gamma\ndelta"   -> macro expansion takes place before (with-out-str ...)
        //                              has been applied, so stdout redirectin is not yet in
        //                              place the when the macro 'alpha/whenn' is run.
        // result:  ""               -> 'macroexpand-all' only expands but does not execute
        //                              the code
        assertEquals("", result2.toString());
    }

    @Test
    public void test_namespace_in_macro_evaluation_upfront_2() {
        // Macros are evaluated in the namespace they are called from!

        ThreadContext.remove(); // clean thread locals

        final IInterceptor interceptor = new AcceptAllInterceptor();
        final VeniceInterpreter venice = new VeniceInterpreter(interceptor);

        // Start off with macroexpand = false
        final Env env = venice.createEnv(false, false, RunMode.SCRIPT)
                              .setStdoutPrintStream(new PrintStream(System.out, true));

        // [1]
        final String macros =
                "(do                                           \n" +
                "  (ns alpha)                                  \n" +
                "                                              \n" +
                "  (defmacro whenn [test form]                 \n" +
                "    (do                                       \n" +
                "      (println *ns*)                          \n" +
                "      `(if ~test ~form nil))))                  ";

        venice.RE(macros, "test", env);


        // [2]
        final String ns = "(ns beta)";
        venice.RE(ns, "test", env);

        // [3]
        final String script =
                "(do                                           \n" +
                "  (with-out-str                               \n" +
                "    (ns gamma)                                \n" +
                "    (alpha/whenn true (println 100))          \n" +
                "    (ns delta)                                \n" +
                "    (alpha/whenn true (println 100)))))        ";

        // Switch to macroexpand = true
        venice.setMacroExpandOnLoad(true);

        final VncVal result2 = venice.RE(script, "test", env);

        // stdout:  "gamma\ndelta"   -> macro expansion takes place before (with-out-str ...)
        //                              has been applied, so stdout redirection is not yet in
        //                              place the the time the macro 'alpha/whenn' is run.
        // result:  "100\n100\n"     -> OK
        assertEquals("100\n100\n", to_lf(result2.toString()));
    }

    @Test
    public void test_ns_alias_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                       \n" +
                "   (ns AAA)               \n" +
                "   (ns-alias 'a 'AAA)     \n" +
                "   (def x 100)            \n" +
                "   a/x)                   ";

        assertEquals(100L, venice.eval(script));
    }

    @Test
    public void test_ns_alias_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                       \n" +
                "   (ns AAA)               \n" +
                "   (def x 100)            \n" +
                "   (ns BBB)               \n" +
                "   (ns-alias 'a 'AAA)     \n" +
                "   a/x)                   ";

        assertEquals(100L, venice.eval(script));
    }

    @Test
    public void test_ns_alias_3() {
        final Venice venice = new Venice();

        final String script =
                "(do                       \n" +
                "   (ns AAA)               \n" +
                "   (def x 100)            \n" +
                "   (ns BBB)               \n" +
                "   (ns-alias 'a 'AAA)     \n" +
                "   (ns-alias 'b 'AAA)     \n" +
                "   (pr-str [a/x b/x]))      ";

        assertEquals("[100 100]", venice.eval(script));
    }

    @Test
    public void test_ns_alias_4() {
        final Venice venice = new Venice();

        final String script =
                "(do                       \n" +
                "   (ns AAA)               \n" +
                "   (def x 100)            \n" +
                "   (ns-alias 'a 'AAA)     \n" +
                "   (ns BBB)               \n" +
                "   a/x)                   ";

        assertThrows(VncException.class, () -> venice.eval(script));
    }

    @Test
    public void test_ns_unalias_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                       \n" +
                "   (ns AAA)               \n" +
                "   (def x 100)            \n" +
                "   (ns-alias 'a 'AAA)     \n" +
                "   (ns-unalias 'a)        \n" +
                "   a/x)                   ";

        assertThrows(VncException.class, () -> venice.eval(script));
    }

    @Test
    public void test_ns_unalias_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                       \n" +
                "   (ns AAA)               \n" +
                "   (def x 100)            \n" +
                "   (ns BBB)               \n" +
                "   (ns-alias 'a 'AAA)     \n" +
                "   (ns-unalias 'a)        \n" +
                "   a/x)                   ";

        assertThrows(VncException.class, () -> venice.eval(script));
    }

    @Test
    public void test_ns_aliases_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                       \n" +
                "   (ns AAA)               \n" +
                "   (def x 100)            \n" +
                "   (pr-str (ns-aliases))) ";

        assertEquals("{}", venice.eval(script));
    }

    @Test
    public void test_ns_aliases_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                       \n" +
                "   (ns AAA)               \n" +
                "   (ns-alias 'a 'AAA)     \n" +
                "   (def x 100)            \n" +
                "   (pr-str (ns-aliases))) ";

        assertEquals("{a AAA}", venice.eval(script));
    }

    @Test
    public void test_ns_aliases_3() {
        final Venice venice = new Venice();

        final String script =
                "(do                       \n" +
                "   (ns AAA)               \n" +
                "   (ns-alias 'a 'AAA)     \n" +
                "   (ns-alias 'b 'AAA)     \n" +
                "   (def x 100)            \n" +
                "   (pr-str (ns-aliases))) ";

        assertEquals("{a AAA b AAA}", venice.eval(script));
    }

    @Test
    public void test_ns_aliases_4() {
        final Venice venice = new Venice();

        final String script =
                "(do                       \n" +
                "   (ns AAA)               \n" +
                "   (ns-alias 'a 'AAA)     \n" +
                "   (ns-alias 'b 'AAA)     \n" +
                "   (def x 100)            \n" +
                "   (ns-unalias 'b)        \n" +
                "   (pr-str (ns-aliases))) ";

        assertEquals("{a AAA}", venice.eval(script));
    }

    @Test
    public void test_ns_aliases_on_str_ns() {
        final Venice venice = new Venice();

        final String script =
                "(do                            \n" +
                "   (ns-alias 's 'str)          \n" +
                "   (s/join [\"ab\" \"cd\"]))    ";

        assertEquals("abcd", venice.eval(script));
    }

    @Test
    public void test_def() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                   \n" +
                "   (ns A)                                             \n" +
                "                                                      \n" +
                "   (def s1 1)                                         \n" +
                "   (def s2 s1)                                        \n" +
                "   (defn f1 [x] (+ x s1 s2))                          \n" +
                "   (defn f2 [x] (+ x (f1 x)))                         \n" +
                "   (defn f3 [x] (+ x ((resolve (symbol \"f1\")) x)))  \n" +
                "                                                      \n" +
                "   (ns B)                                             \n" +
                "                                                      \n" +
                "   (str [(A/f1 100) (A/f2 100) (A/f3 100)])           \n" +
                ")";

        assertEquals("[102 202 202]", venice.eval(script));
    }

    @Test
    public void test_defmulti() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                             \n" +
                "   (ns A)                                                       \n" +
                "                                                                \n" +
                "   (defmulti math-op (fn [s] (:op s)))                          \n" +
                "                                                                \n" +
                "   (defmethod math-op \"add\" [s] (+ (:op1 s) (:op2 s)))        \n" +
                "   (defmethod math-op \"subtract\" [s] (- (:op1 s) (:op2 s)))   \n" +
                "   (defmethod math-op :default [s] 0)                           \n" +
                "                                                                \n" +
                "   (ns B)                                                       \n" +
                "                                                                \n" +
                "   (str                                                         \n" +
                "      [ (A/math-op {:op \"add\"      :op1 1 :op2 5})            \n" +
                "        (A/math-op {:op \"subtract\" :op1 1 :op2 5})            \n" +
                "        (A/math-op {:op \"bogus\"    :op1 1 :op2 5}) ] ))       \n" +
                ")                                                                 ";

        assertEquals("[6 -4 0]", venice.eval(script));
    }

    @Test
    public void test_import() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                   \n" +
                "   (ns A)                                             \n" +
                "                                                      \n" +
                "   (import :java.lang.Long)                           \n" +
                "                                                      \n" +
                "   (defn f1 [x] (. :Long :new x))                     \n" +
                "   (defn f2 [x] (+ x (f1 x)))                         \n" +
                "                                                      \n" +
                "   (ns B)                                             \n" +
                "                                                      \n" +
                "   (str [(A/f1 100) (A/f2 100)])                      \n" +
                ")";

        assertEquals("[100 200]", venice.eval(script));
    }
}
