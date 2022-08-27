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

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.RunMode;
import com.github.jlangch.venice.impl.VeniceInterpreter;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;


public class MacroExpandTest {

    @Test
    public void test_macroexpand_1() {
        final Venice venice = new Venice();

        final String s1 =
                "(do                                        " +
                "   (defmacro hello [x] '(+ 1 2))           " +
                "                                           " +
                "   (macroexpand '(hello 0))                " +
                ")                                          ";


        final String s2 =
                "(do                                        " +
                "   (defmacro hello [x] (eval '(+ 1 2)))    " +
                "                                           " +
                "   (macroexpand '(hello 0))                " +
                ")                                          ";

        assertEquals("(+ 1 2)", venice.eval("(str " + s1 + ")"));
        assertEquals(3L, venice.eval(s2));
    }

    @Test
    public void test_macroexpand_2() {
        final Venice venice = new Venice();

        final String s1 =
                "(do                                        " +
                "   (defmacro when1 [expr form]             " +
                "      (list 'if expr nil form))            " +
                "                                           " +
                "   (macroexpand '(when1 true (+ 1 1)))     " +
                ")                                          ";


        final String s2 =
                "(do                                        " +
                "   (defmacro when1 [expr form]             " +
                "      (list 'if expr nil form))            " +
                "                                           " +
                "   (macroexpand '(when1 false (+ 1 1)))    " +
                ")                                          ";

        assertEquals("(if true nil (+ 1 1))", venice.eval("(str " + s1 + ")"));
        assertEquals("(if false nil (+ 1 1))", venice.eval("(str " + s2 + ")"));
    }

    @Test
    public void test_macroexpand_all() {
        final Venice venice = new Venice();

        final String s1 = "(macroexpand-all '(when true 1))";

        final String s2 = "(macroexpand-all '[(when true 1) (when true 2) (when true 3)])";

        final String s3 = "(macroexpand-all '(do (when true 1) (when true 2) (when true 3)))";

        final String s4 = "(macroexpand-all '(when true (when true (when true 3))))";

        assertEquals("(if true (do 1))", venice.eval("(str " + s1 + ")"));
        assertEquals("[(if true (do 1)) (if true (do 2)) (if true (do 3))]", venice.eval("(str " + s2 + ")"));
        assertEquals("(do (if true (do 1)) (if true (do 2)) (if true (do 3)))", venice.eval("(str " + s3 + ")"));
        assertEquals("(if true (do (if true (do (if true (do 3))))))", venice.eval("(str " + s4 + ")"));
    }

    @Test
    public void test_macroexpand_READ() {
        final VeniceInterpreter venice = new VeniceInterpreter(new AcceptAllInterceptor());

        final Env env = venice.createEnv(true, false, RunMode.SCRIPT);

        assertEquals("(if true (do 1))",
                     venice.MACROEXPAND(
                        venice.READ("(when true 1)", "test"),
                        env).toString(true));

        assertEquals("[(if true (do 1)) (if true (do 2)) (if true (do 3))]",
                     venice.MACROEXPAND(
                        venice.READ("[(when true 1) (when true 2) (when true 3)]", "test"),
                        env).toString(true));


        assertEquals("(do (if true (do 1)) (if true (do 2)) (if true (do 3)))",
                     venice.MACROEXPAND(
                        venice.READ("(do (when true 1) (when true 2) (when true 3))", "test"),
                        env).toString(true));


        assertEquals("(if true (do (if true (do (if true (do 3))))))",
                     venice.MACROEXPAND(
                        venice.READ("(when true (when true (when true 3)))", "test"),
                        env).toString(true));
    }

    @Test
    public void test_eval_macroexpand_flag() {
        final Venice venice = new Venice(new AcceptAllInterceptor());

        final String script1 = "(do                      \n" +
        		               "  (defn tt []            \n" +
        		               "    (when true 1))       \n" +
        		               "  (pr-str (fn-body tt))) ";

        assertEquals(
        		"((if true (do 1)))",
        		venice.eval("test", script1, true, null));


        final String script2 = "(do                                               \n" +
        		               "  (defn tt []                                     \n" +
        		               "    [(when true 1) (when true 2) (when true 3)])  \n" +
        		               "  (pr-str (fn-body tt)))                          ";

        assertEquals(
        		"([(if true (do 1)) (if true (do 2)) (if true (do 3))])",
        		venice.eval("test", script2, true, null));


        final String script3 = "(do                                               \n" +
        		               "  (defn tt []                                     \n" +
        		               "    (when true 1) (when true 2) (when true 3))    \n" +
        		               "  (pr-str (fn-body tt)))                          ";

        assertEquals(
        		"((if true (do 1)) (if true (do 2)) (if true (do 3)))",
        		venice.eval("test", script3, true, null));


        final String script4 = "(do                                               \n" +
        		               "  (defn tt []                                     \n" +
        		               "    (when true (when true (when true 3))))        \n" +
        		               "  (pr-str (fn-body tt)))                          ";

        assertEquals(
        		"((if true (do (if true (do (if true (do 3)))))))",
        		venice.eval("test", script4, true, null));
    }

    @Test
    public void test_load_module_with_macroexpand() {
        final Venice venice = new Venice(new AcceptAllInterceptor());

        final String script1 = "(do                                             \n" +
        		               "  (load-module :test)                           \n" +
        		               "                                                \n" +
        		               "  (pr-str (fn-body test/test-body-with-macro))) ";

        assertEquals(
        		"((when true 1))",
        		venice.eval("test", script1, false, null));

        assertEquals(
        		"((if true (do 1)))",
        		venice.eval("test", script1, true, null));
    }

}
