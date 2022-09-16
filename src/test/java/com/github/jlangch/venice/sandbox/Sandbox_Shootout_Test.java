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
package com.github.jlangch.venice.sandbox;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.SecurityException;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.javainterop.Interceptor;
import com.github.jlangch.venice.javainterop.SandboxRules;


public class Sandbox_Shootout_Test {

    @Test
    public void test_native_func() {
        final Interceptor interceptor = new SandboxRules()
                                             .rejectVeniceFunctions("+")
                                             .sandbox();

        final String[] expr = new String[] {

                    "(+ 1 1)",
                    "(core/+ 1 1)",

                    "(do (ns-alias 'c 'core) (c/+ 1 1))",
                    "(do (ns-alias 'c 'core) (reduce c/+ [1 2 3]))",

                    "(map #(+ %1 2) [1 2 3])",
                    "(reduce + [1 2 3])",
                    "(reduce core/+ [1 2 3])",
                    "(reduce #(+ %1 %2) 0 [1 2 3])",

                    "((juxt +) 1)",
                    "((juxt - +) 1)",

                    "((resolve '+) 2 3)",
                    "((resolve 'core/+) 2 3)",
                    "((resolve (symbol \"+\")) 2 3)",
                    "((resolve (symbol \"core/+\")) 2 3)",

                    "(eval (read-string \"(+ 2 3)\"))",
                    "(eval (read-string \"(core/+ 2 3)\"))",
                    "(eval '(+ 2 3))",
                    "(eval '(core/+ 2 3))",
                    "(eval (list + 2 3))",
                    "(eval (list core/+ 2 3))",

                    "(do                                 \n" +
                    "  (def add2 (partial + 2))          \n" +
                    "  (add2 3))                         ",

                    "(do                                 \n" +
                    "  (defmacro plus [x y] `(+ ~1 ~2))  \n" +
                    "  (plus 1 2))                       ",
                    "(do                                 \n" +
                    "  (defmacro plus [x y] (+ 1 2))     \n" +
                    "  (plus 4 5))                       ",

                    "(do                                 \n" +
                    "  (defn mul [x y]                   \n" +
                    "    { :pre [(pos? (+ x y))] }       \n" +
                    "    (* x y))                        \n" +
                    "  (mul 1 2))                        ",

               };

        final Venice venice = new Venice(interceptor);

        // all denied
        for(String e : expr) {
            assertThrows(SecurityException.class, () -> venice.eval(e));
        }

        // macro expansion
        venice.eval("test", "(defmacro plus [x y] (+ 1 2))", false, null);
        venice.eval("test", "(defmacro plus [x y] (+ 1 2))", true, null);  // just defined, nothing to expand yet

        assertThrows(SecurityException.class, () ->
                        venice.eval(
                            "test",
                            "(do                                 \n" +
                            "  (defmacro plus [x y] (+ 1 2))     \n" +
                            "  (plus 4 5))                       ",  // expands 'plus' at runtime
                            false, null));

        assertThrows(SecurityException.class, () ->
                        venice.eval(
                            "test",
                            "(do                                 \n" +
                            "  (defmacro plus [x y] (+ 1 2))     \n" +
                            "  (plus 4 5))                       ", // expands 'plus' at reader time
                            true, null));
    }

    @Test
    public void test_native_func_with_core_ns() {
        final Interceptor interceptor = new SandboxRules()
                                              .rejectVeniceFunctions("core/+")
                                              .sandbox();

        final Venice venice = new Venice(interceptor);

        // denied
        assertThrows(SecurityException.class, () -> venice.eval("(+ 1 1)"));
    }


    @Test
    public void test_native_func_concurrency() {
        final Interceptor interceptor = new SandboxRules()
                                             .rejectVeniceFunctions("+")
                                             .sandbox();

        final String[] expr = new String[] {

                    // pmap

                    "(pmap #(+ 1 %) [1 2 3])",

                    // pcalls

                    "(pcalls #(+ 1 2))",
                    "(pcalls #(- 1 2) #(+ 2 3))",


                    // thread

                    "@(thread #(+ 1 1))",
                    "@(thread (fn [] @(thread (fn [] (+ 1 1)))))",


                    // delay

                    "@(delay (+ 1 1))",


                    // future

                    "@(future #(+ 1 1))",


                    // promise

                    "@(promise #(+ 1 1))",

                    // promise -> then-apply
                    "(-> (promise (fn [] 5))      \n" +
                    "    (then-apply #(+ % 3))    \n" +
                    "    (deref))                 ",

                    // promise -> then-combine #1
                    "(-> (promise (fn [] (sleep 20) 5))                              \n" +
                    "    (then-apply (fn [x] (sleep 20) (* x 3)))                    \n" +
                    "    (then-combine (-> (promise (fn [] (sleep 20) 5))            \n" +
                    "                      (then-apply (fn [x] (sleep 20) (* x 2)))) \n" +
                    "                  #(+ %1 %2))                                   \n" +
                    "    (deref))",

                    // promise -> then-combine #2
                    "(-> (promise (fn [] (sleep 20) 5))                              \n" +
                    "    (then-apply (fn [x] (sleep 20) (* x 3)))                    \n" +
                    "    (then-combine (-> (promise (fn [] (sleep 20) 5))            \n" +
                    "                      (then-apply (fn [x] (sleep 20) (+ x 2)))) \n" +
                    "                  #(* %1 %2))                                   \n" +
                    "    (deref))",

                    // promise -> then-combine #3
                    "(-> (promise (fn [] (sleep 20) 5))                              \n" +
                    "    (then-apply (fn [x] (sleep 20) (* x 3)))                    \n" +
                    "    (then-combine (-> (promise (fn [] (sleep 20) (+ 1 1)))      \n" +
                    "                      (then-apply (fn [x] (sleep 20) (* x 2)))) \n" +
                    "                  #(* %1 %2))                                   \n" +
                    "    (deref))",

                    // promise -> then-accept
                    "(let [result (promise)                                          \n" +
                    "      p      (promise)]                                         \n" +
                    "  (thread #(deliver p 5))                                       \n" +
                    "  (then-accept p (fn [v]                                        \n" +
                    "                   (try                                         \n" +
                    "                     (deliver result (+ v 2))                   \n" +
                    "                     (catch :SecurityException ex               \n" +
                    "                       (deliver-ex result ex)))))               \n" +
                    "  @result))                                                     ",

                    // promise -> then-compose
                    "(-> (promise (fn [] 5))                                         \n" +
                    "    (then-apply (fn [x] (* x 2)))                               \n" +
                    "    (then-compose (fn [x] (-> (promise (fn [] 6))               \n" +
                    "                              (then-apply (fn [y] (* y 3)))     \n" +
                    "                              (then-apply (fn [y] (+ x y))))))  \n" +
                    "    (deref))                                                    ",

                    // promise -> when-complete
                    "(let [result (promise)                                          \n" +
                    "      p      (promise)]                                         \n" +
                    "  (thread #(deliver p 5))                                       \n" +
                    "  (let [q (then-apply p (fn [v] (* v 2)))]                      \n" +
                    "    (when-complete q (fn [v,e]                                  \n" +
                    "                       (try                                     \n" +
                    "                         (deliver result (+ v 2))               \n" +
                    "                         (catch :SecurityException ex           \n" +
                    "                           (deliver-ex result ex)))))           \n" +
                    "    @result))                                                   ",

                    // promise -> accept-either #1
            		"(let [result (promise)]                                         \n" +
                    "  (-> (promise (fn [] (sleep 200) 200))                         \n" +
                    "      (accept-either (promise (fn [] (sleep 100) 100))          \n" +
                    "                     (fn [v]                                    \n" +
                    "                       (try                                     \n" +
                    "                         (deliver result (+ v 2))               \n" +
                    "                         (catch :SecurityException ex           \n" +
                    "                           (deliver-ex result ex))))))          \n" +
                    "  @result)                                                      ",

                    // promise -> accept-either #2
            		"(let [result (promise)]                                         \n" +
                    "  (-> (promise (fn [] (sleep 100) 100))                         \n" +
                    "      (accept-either (promise (fn [] (sleep 200) 200))          \n" +
                    "                     (fn [v]                                    \n" +
                    "                       (try                                     \n" +
                    "                         (deliver result (+ v 2))               \n" +
                    "                         (catch :SecurityException ex           \n" +
                    "                           (deliver-ex result ex))))))          \n" +
                    "  @result)                                                      ",

                    // promise -> apply-to-either #1
                    "(-> (promise (fn [] (sleep 200) 200))                           \n" +
                    "    (apply-to-either (promise (fn [] (sleep 100) 100))          \n" +
                    "                     (fn [v] (+ v 1)))                          \n" +
                    "    (deref))                                                    ",

                    // promise -> apply-to-either #2
                    "(-> (promise (fn [] (sleep 100) 100))                           \n" +
                    "    (apply-to-either (promise (fn [] (sleep 200) 200))          \n" +
                    "                     (fn [v] (+ v 1)))                          \n" +
                    "    (deref))                                                    ",

                    // promise -> then-accept-both
            		"(let [result (promise)]                                         \n" +
                    "  (-> (promise (fn [] (sleep 200) 200))                         \n" +
                    "      (then-accept-both (promise (fn [] (sleep 100) 100))       \n" +
                    "                        (fn [u v]                               \n" +
                    "                          (try                                  \n" +
                    "                            (deliver result (+ u v))            \n" +
                    "                            4                                   \n" +
                    "                            (catch :SecurityException ex        \n" +
                    "                              (deliver-ex result ex))))))       \n" +
                    "  @result)                                                      ",


                    // volatile

                    "(let [c (volatile 100)]   \n" +
                    "  (swap! c #(+ % 1))      \n" +
                    "  @c)                     ",


                    // atom

                    "(let [c (atom 100)]   \n" +
                    "  (swap! c #(+ % 1))  \n" +
                    "  @c)                 ",

                    "(let [c (atom 100)]                 \n" +
                    "  (swap-vals! c #(+ %1 %2) 2)       \n" +
                    "  @c)                               ",

                    "(do                                 \n" +
                    "  (def p (promise))                 \n" +
                    "  (defn watcher [key ref old new]   \n" +
                    "    (try                            \n" +
                    "      (+ 1 1)                       \n" +
                    "      (catch :SecurityException ex  \n" +
                    "        (deliver-ex p ex))))        \n" +
                    "  (let [c (atom 100)]               \n" +
                    "    (add-watch c :test watcher)     \n" +
                    "    (swap! c inc)                   \n" +
                    "    (deref p 1000 :failed)))        ",


                    // agent

                    "(let [a (agent 100)] (send a + 5))",

                    "(let [a (agent 100)] (send-off a + 5))",

                    "(do                                 \n" +
                    "  (def p (promise))                 \n" +
                    "  (defn handler [ag ex]             \n" +
                    "    (try                            \n" +
                    "      (+ 1 1)                       \n" +
                    "      (catch :SecurityException ex  \n" +
                    "        (deliver-ex p ex))))        \n" +
                    "  (let [a (agent 100)]              \n" +
                    "    (set-error-handler! a handler)  \n" +
                    "    (send a (fn [n] (/ n 0))))      \n" +
                    "    (deref p 1000 :failed)))        ",

                    "(do                                 \n" +
                    "  (def p (promise))                 \n" +
                    "  (defn handler [ag ex]             \n" +
                    "    (try                            \n" +
                    "      (+ 1 1)                       \n" +
                    "      (catch :SecurityException ex  \n" +
                    "        (deliver-ex p ex))))        \n" +
                    "  (let [a (agent 100)]              \n" +
                    "    (set-error-handler! a handler)  \n" +
                    "    (send-off a (fn [n] (/ n 0))))  \n" +
                    "    (deref p 1000 :failed)))        ",


                    // scheduler

                    "(do                                 \n" +
                    "  (def p (promise))                 \n" +
                    "  (defn handler []                  \n" +
                    "    (try                            \n" +
                    "      (+ 1 1)                       \n" +
                    "      (catch :SecurityException ex  \n" +
                    "        (deliver-ex p ex))))        \n" +
                    "  (schedule-delay handler           \n" +
                    "                  100               \n" +
                    "                  :milliseconds)    \n" +
                    "  (deref p 1000 :failed))           ",

                    "(do                                                \n" +
                    "  (def p (promise))                                \n" +
                    "  (defn handler []                                 \n" +
                    "    (try                                           \n" +
                    "      (+ 1 1)                                      \n" +
                    "      (catch :SecurityException ex                 \n" +
                    "        (deliver-ex p ex))))                       \n" +
                    "  (let [s (schedule-at-fixed-rate handler          \n" +
                    "                                  10               \n" +
                    "                                  200              \n" +
                    "                                  :milliseconds)]  \n" +
                    "    (sleep 100)                                    \n" +
                    "    (cancel s)                                     \n" +
                    "    (deref p 1000 :failed)))                       ",


                    // shutdown-hook

                    // "(shutdown-hook #(+ 1 2))",  // this can not be tested in a unit test!
                    // Test it manually:
                    //  * start a REPL
                    //  * run `!sandbox customized`
                    //  * run `!sandbox add-rule blacklist:venice:func:+`
                    //  * run `(shutdown-hook (fn [] (try (+ 1 2) (catch :SecurityException ex (println ex) (sleep 3000)))))`
                    //  * exit the REPL with `!exit`

               };

        final Venice venice = new Venice(interceptor);

        // all denied
        for(String e : expr) {
            assertThrows(SecurityException.class, () -> venice.eval(e));
        }
    }
}
