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
package com.github.jlangch.venice.impl.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.TimeoutException;
import com.github.jlangch.venice.ValueException;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.util.CapturingPrintStream;


public class ConcurrencyFunctionsTest {

    @Test
    public void test_atom() {
        final Venice venice = new Venice();

        final String script =
                "(do                          \n" +
                "   (def x (atom 100))        \n" +
                "   @x)                        ";

        final Object result = venice.eval(script);

        assertEquals(100L, result);
    }

    @Test
    public void test_atom_reset() {
        final Venice venice = new Venice();

        final String script =
                "(do                          \n" +
                "   (def x (atom 100))        \n" +
                "   (reset! x 200)            \n" +
                "   @x)                        ";

        final Object result = venice.eval(script);

        assertEquals(200L, result);
    }

    @Test
    public void test_atom_swap() {
        final Venice venice = new Venice();

        final String script =
                "(do                          \n" +
                "   (def x (atom 100))        \n" +
                "   (swap! x inc)             \n" +
                "   @x)                        ";

        final Object result = venice.eval(script);

        assertEquals(101L, result);
    }

    @Test
    public void test_atom_swap_vals() {
        final Venice venice = new Venice();

        final String script =
                "(do                                  \n" +
                "   (def queue (atom '(1 2 3)))       \n" +
                "   (pr-str (swap-vals! queue pop)))    ";

        final String result = (String)venice.eval(script);

        assertEquals("[(1 2 3) (2 3)]", result);
    }

    @Test
    public void test_atom_dereferenceable() {
        final Venice venice = new Venice();

        final String script = "(deref? (atom 100))";

        final Object result = venice.eval(script);

        assertTrue((Boolean)result);
    }

    @Test
    public void test_atom_compareAndSet_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                    \n" +
                "   (def x (atom 2))                    \n" +
                "   (compare-and-set! x 2 4)  ; true    \n" +
                "   (compare-and-set! x 3 5)  ; false   \n" +
                "   @x)                                   ";

        final Object result = venice.eval(script);

        assertEquals(4L, result);
    }

    @Test
    public void test_atom_compareAndSet_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                               \n" +
                "   (def x (atom 2))               \n" +
                "   (compare-and-set! x 2 4))        ";

        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void test_atom_compareAndSet_3() {
        final Venice venice = new Venice();

        final String script =
                "(do                               \n" +
                "   (def x (atom 2))               \n" +
                "   (compare-and-set! x 3 4))        ";

        assertFalse((Boolean)venice.eval(script));
    }

    @Test
    public void test_atom_with_meta() {
        final Venice venice = new Venice();

        final String script =
                "(do                                    \n" +
                "   (def x (atom 100 :meta {:a 1000}))  \n" +
                "   (swap! x inc)                       \n" +
                "   (:a (meta x)))                        ";

        final Object result = venice.eval(script);

        assertEquals(1000L, result);
    }

    @Test
    public void test_atom_reset_with_validator_OK() {
        final Venice venice = new Venice();

        final String script =
                "(do                                    \n" +
                "   (def x (atom 100 :validator pos?))  \n" +
                "   (reset! x 200)                      \n" +
                "   @x)                                   ";

        assertEquals(200L, venice.eval(script));
    }

    @Test
    public void test_atom_reset_with_validator_FAIL() {
        final Venice venice = new Venice();

        final String script =
                "(do                                    \n" +
                "   (def x (atom 100 :validator pos?))  \n" +
                "   (reset! x -200)                     \n" +
                "   @x)                                   ";

        assertThrows(VncException.class, () -> venice.eval(script));
    }

    @Test
    public void test_atom_swap_with_validator_OK() {
        final Venice venice = new Venice();

        final String script =
                "(do                                    \n" +
                "   (def x (atom 1 :validator pos?))    \n" +
                "   (swap! x inc)                       \n" +
                "   @x)                                   ";

        final Object result = venice.eval(script);

        assertEquals(2L, result);
    }

    @Test
    public void test_atom_swap_with_validator_FAIL() {
        final Venice venice = new Venice();

        final String script =
                "(do                                    \n" +
                "   (def x (atom 1 :validator pos?))    \n" +
                "   (swap! x dec)                       \n" +
                "   @x)                                   ";

        assertThrows(VncException.class, () -> venice.eval(script));
    }

    @Test
    public void test_atom_compareAndSet_with_validator_OK_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                    \n" +
                "   (def x (atom 1 :validator pos?))    \n" +
                "   (compare-and-set! x 1 4)            \n" +
                "   @x)                                   ";

        final Object result = venice.eval(script);

        assertEquals(4L, result);
    }

    @Test
    public void test_atom_compareAndSet_with_validator_OK_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                    \n" +
                "   (def x (atom 1 :validator pos?))    \n" +
                "   (compare-and-set! x 2 4)            \n" +
                "   @x)                                   ";

        final Object result = venice.eval(script);

        assertEquals(1L, result);
    }

    @Test
    public void test_atom_compareAndSet_with_validator_FAIL_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                    \n" +
                "   (def x (atom 1 :validator pos?))    \n" +
                "   (compare-and-set! x 1 -4)           \n" +
                "   @x)                                   ";

        assertThrows(VncException.class, () -> venice.eval(script));
    }

    @Test
    public void test_atom_compareAndSet_with_validator_FAIL_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                    \n" +
                "   (def x (atom 1 :validator pos?))    \n" +
                "   (compare-and-set! x 2 -4)           \n" +
                "   @x)                                   ";

        assertThrows(VncException.class, () -> venice.eval(script));
    }

    @Test
    public void test_volatile() {
        final Venice venice = new Venice();

        final String script =
                "(do                          \n" +
                "   (def x (volatile 100))    \n" +
                "   @x)                        ";

        final Object result = venice.eval(script);

        assertEquals(100L, result);
    }

    @Test
    public void test_volatile_reset() {
        final Venice venice = new Venice();

        final String script =
                "(do                          \n" +
                "   (def x (volatile 100))    \n" +
                "   (reset! x 200)            \n" +
                "   @x)                        ";

        final Object result = venice.eval(script);

        assertEquals(200L, result);
    }

    @Test
    public void test_volatile_swap() {
        final Venice venice = new Venice();

        final String script =
                "(do                          \n" +
                "   (def x (volatile 100))    \n" +
                "   (swap! x inc)             \n" +
                "   @x)                        ";

        final Object result = venice.eval(script);

        assertEquals(101L, result);
    }

    @Test
    public void test_volatile_dereferenceable() {
        final Venice venice = new Venice();

        final String script = "(deref? (volatile 100))";

        final Object result = venice.eval(script);

        assertTrue((Boolean)result);
    }

    @Test
    public void test_agent() {
        final Venice venice = new Venice();

        final String script =
                "(do                          \n" +
                "   (def x (agent 100))       \n" +
                "   (deref x))                  ";

        final Object result = venice.eval(script);

        assertEquals(Long.valueOf(100), result);
    }

    @Test
    public void test_agent_dereferenceable() {
        final Venice venice = new Venice();

        final String script = "(deref? (agent 100))";

        final Object result = venice.eval(script);

        assertTrue((Boolean)result);
    }

    @Test
    public void test_agent_restart() {
        final Venice venice = new Venice();

        final String script =
                "(do                                 \n" +
                "   (def x (agent 100))              \n" +
                "   (restart-agent x 200)            \n" +
                "   (deref x))                         ";

        final Object result = venice.eval(script);

        assertEquals(Long.valueOf(200), result);
    }

    @Test
    public void test_agent_send() {
        final Venice venice = new Venice();

        final String script =
                "(do                         \n" +
                "   (def x (agent 100))      \n" +
                "   (send x + 5)             \n" +
                "   (sleep 200)              \n" +
                "   (deref x))                 ";

        final Object result = venice.eval(script);

        assertEquals(Long.valueOf(105), result);
    }

    @Test
    public void test_agent_send_order_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                               \n" +
                "   (def a1 (agent 2))                             \n" +
                "   (def a2 (agent 3))                             \n" +
                "   (send a1 (fn [x] (do (sleep 500) (+ x 10))))   \n" +
                "   (send a2 (fn [x] (do (sleep 400) (+ x 10))))   \n" +
                "   (send a1 (fn [x] (do (sleep 100) (* x 2))))    \n" +
                "   (send a2 (fn [x] (do (sleep 100) (* x 2))))    \n" +
                "   (sleep 800)                                    \n" +
                "   (str [@a1 @a2]))                                 ";

        assertEquals("[24 26]", venice.eval(script));
    }

    @Test
    public void test_agent_send_order_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                               \n" +
                "   (def a1 (agent 2))                             \n" +
                "   (def a2 (agent 3))                             \n" +
                "   (send a1 (fn [x] (do (sleep 500) (+ x 10))))   \n" +
                "   (send a2 (fn [x] (do (sleep 400) (+ x 10))))   \n" +
                "   (send a1 (fn [x] (do (sleep 100) (* x 2))))    \n" +
                "   (send a2 (fn [x] (do (sleep 100) (* x 2))))    \n" +
                "   (await-for 1000 a1 a2)                         \n" +
                "   (str [@a1 @a2]))                                 ";

        assertEquals("[24 26]", venice.eval(script));
    }

    @Test
    public void test_agent_send_off() {
        final Venice venice = new Venice();

        final String script =
                "(do                         \n" +
                "   (def x (agent 100))      \n" +
                "   (send-off x + 5)         \n" +
                "   (sleep 100)              \n" +
                "   (deref x))                 ";

        final Object result = venice.eval(script);

        assertEquals(Long.valueOf(105), result);
    }

    @Test
    public void test_agent_watch() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                     \n" +
                "   (defn watcher [key ref old new]                                      \n" +
                "         (println \"watcher: \" key \", old:\" old \", new:\" new ))    \n" +
                "   (def x (agent 100))                                                  \n" +
                "   (add-watch x :test watcher)                                          \n" +
                "   (send x + 5)                                                         \n" +
                "   (sleep 100)                                                          \n" +
                "   (deref x))                                                             ";

        final Object result = venice.eval(script);

        assertEquals(Long.valueOf(105), result);
    }

    @Test
    public void test_agent_error_mode_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                              \n" +
                "   (def x (agent 100))           \n" +
                "   (str (agent-error-mode x)))     ";

        assertEquals(":continue", venice.eval(script));
    }

    @Test
    public void test_agent_error_mode_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                              \n" +
                "   (def x (agent 100 :error-mode :continue))     \n" +
                "   (str (agent-error-mode x)))                     ";

        assertEquals(":continue", venice.eval(script));
    }

    @Test
    public void test_agent_error_mode_3() {
        final Venice venice = new Venice();

        final String script =
                "(do                                              \n" +
                "   (def x (agent 100 :error-mode :fail))         \n" +
                "   (str (agent-error-mode x)))                     ";

        assertEquals(":fail", venice.eval(script));
    }

    @Test
    public void test_agent_error_1() {
        final Venice venice = new Venice();

        // Agents as message relay

        final String script =
                "(do                                              \n" +
                "   (def logger (agent (list)))                   \n" +
                "                                                 \n" +
                "   (defn log* [msg]                              \n" +
                "      (send logger #(cons %2 %1) msg))           \n" +
                "                                                 \n" +
                "   (def a (agent 100))                           \n" +
                "                                                 \n" +
                "   (defn err-handler-fn [ag ex]                  \n" +
                "      (println (stacktrace ex))                  \n" +
                "      (log* (str \"error occured: \"             \n" +
                "                 (type ex)                       \n" +
                "                 \"> \"                          \n" +
                "                 (:message ex)                   \n" +
                "                 \" and we still have value \"   \n" +
                "                 @ag)))                          \n" +
                "                                                 \n" +
                "   (set-error-handler! a err-handler-fn)         \n" +
                "   (send a (fn [x] (/ x 0)))                     \n" +
                "   (sleep 500)                                   \n" +
                "   (with-out-str (print @logger)))                 ";

        assertEquals(
                "(error occured: :com.github.jlangch.venice.VncException> / by zero and we still have value 100)",
                venice.eval(script));
    }

    @Test
    public void test_agent_error_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                              \n" +
                "   (def a (agent 100 :error-mode :fail))         \n" +
                "                                                 \n" +
                "   (send a (fn [x] (/ x 0)))                     \n" +
                "   (sleep 500)                                   \n" +
                "   (:message (agent-error a)))                     ";

        assertEquals("/ by zero", venice.eval(script));
    }

    @Test
    public void test_agent_await_for() {
        final Venice venice = new Venice();

        final String script =
                "(do                            \n" +
                "   (def x1 (agent 100))        \n" +
                "   (def x2 (agent 100))        \n" +
                "   (await-for 500 x1 x2))        ";

        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void test_agent_relay() {
        final Venice venice = new Venice();

        // Agents as message relay

        final String script =
                "(do                                                                         \n" +
                "   (def logger (agent (list)))                                              \n" +
                "                                                                            \n" +
                "   (defn log* [msg]                                                         \n" +
                "      (send logger #(cons %2 %1) msg))                                      \n" +
                "                                                                            \n" +
                "   (defn create-relay [n]                                                   \n" +
                "      (reduce (fn [prev _] (agent prev)) nil (range 0 n)))                  \n" +
                "                                                                            \n" +
                "   (defn process [relay msg]                                                \n" +
                "      (let [relay-fn (fn [next-actor hop msg]                               \n" +
                "                         (if next-actor                                     \n" +
                "                            (do                                             \n" +
                "                               (log* (list hop msg))                        \n" +
                "                               (send next-actor relay-fn (inc hop) msg)     \n" +
                "                               @next-actor)                                 \n" +
                "                            (log* \"finished relay\") ))]                   \n" +
                "         (send relay relay-fn 0 msg)))                                      \n" +
                "                                                                            \n" +
                "   (process (create-relay 5) \"hello\")                                     \n" +
                "   (sleep 500)                                                              \n" +
                "   (with-out-str (print @logger)))                                            ";

        assertEquals(
                "(finished relay (3 hello) (2 hello) (1 hello) (0 hello))",
                venice.eval(script));
    }

    @Test
    public void test_agent_logger() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                         \n" +
                "   (import :java.io.PrintWriter)                                            \n" +
                "   (import :java.io.BufferedWriter)                                         \n" +
                "                                                                            \n" +
                "   (let [pwtr (. :PrintWriter :new *out* true)                              \n" +
                "         wtr (agent (. :BufferedWriter :new pwtr))]                         \n" +
                "      (defn log* [msg]                                                      \n" +
                "         (let [write (fn [out msg] (do (. out :write msg) out))]            \n" +
                "            (send wtr write msg)))                                          \n" +
                "      (defn log-close []                                                    \n" +
                "            (do                                                             \n" +
                "               (send wtr (fn [out] (do (. out :flush) (. out :close) out))) \n" +
                "               (await-for 2000 wtr))))                                      \n" +
                "                                                                            \n" +
                "   (log* \"test\n\")                                                        \n" +
                "   (log* \"another line\n\")                                                \n" +
                "   (log-close)                                                              \n" +
                "   (println \"DONE.\"))                                                 ";

        venice.eval(script);
    }

    @Test
    public void test_agent_thread_local() {
        final Venice venice = new Venice();

        final String script =
                "(do                            \n" +
                "   (defn add [a b] (+ a b z))  \n" +
                "   (def x (agent 100))         \n" +
                "   (binding [z 10]             \n" +
                "     (send x add 5)            \n" +
                "     (sleep 200)               \n" +
                "     (deref x)))                 ";

        final Object result = venice.eval(script);

        assertEquals(Long.valueOf(115), result);
    }

    @Test
    public void test_delay() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                \n" +
                "   (def x (delay (println \"working...\") 100))    \n" +
                "   (println \"start\")                             \n" +
                "   (deref x)                                       \n" +
                "   (deref x)                                       \n" +
                "   (deref x)                                       \n" +
                "   (println \"end\")                               \n" +
                "   (deref x))                                     ";

        final CapturingPrintStream ps = new CapturingPrintStream();

        final Object result = venice.eval(script, Parameters.of("*out*", ps));

        assertEquals(Long.valueOf(100), result);
        assertEquals("start\nworking...\nend\n", ps.getOutput());
    }

    @Test
    public void test_delay_realized_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                       \n" +
                "   (def x (delay 100))    \n" +
                "   (realized? x))           ";

        assertFalse((Boolean)venice.eval(script));
    }

    @Test
    public void test_delay_realized_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                       \n" +
                "   (def x (delay 100))    \n" +
                "   @x                     \n" +
                "   (realized? x))           ";

        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void test_delay_dereferenceable() {
        final Venice venice = new Venice();

        final String script = "(deref? (delay 100))";

        final Object result = venice.eval(script);

        assertTrue((Boolean)result);
    }

    @Test
    public void test_pmap() {
        final Venice venice = new Venice();

        assertEquals("()", venice.eval("(pr-str (pmap inc nil))"));
        assertEquals("()", venice.eval("(pr-str (pmap inc []))"));
        assertEquals("(2 3 4 5 6 7)", venice.eval("(pr-str (pmap inc [1 2 3 4 5 6]))"));

        assertEquals("(9 11)", venice.eval("(pr-str (pmap + [1 2] [8 9]))"));
        assertEquals("(9 11)", venice.eval("(pr-str (pmap + [1 2 3 4 5] [8 9]))"));
        assertEquals("(9 11)", venice.eval("(pr-str (pmap + [1 2 3 4 5] [8 9]))"));
        assertEquals("(9 11)", venice.eval("(pr-str (pmap + [1 2] [8 9 10 11]))"));
    }

    @Test
    public void test_pcalls() {
        final Venice venice = new Venice();

        assertEquals("(3)", venice.eval("(pr-str (pcalls #(+ 1 2)))"));
        assertEquals("(3 5 7)", venice.eval("(pr-str (pcalls #(+ 1 2) #(+ 2 3) #(+ 3 4)))"));
    }

    @Test
    public void test_promise_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                          " +
                "   (def p (promise))         " +
                "   (defn task []             " +
                "      (sleep 500)            " +
                "      (deliver p 123))       " +
                "                             " +
                "   (future task)             " +
                "   (deref p))                " +
                ") ";

        assertEquals(123L, venice.eval(script));
    }

    @Test
    public void test_promise_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                             " +
                "   (def p (promise (fn [] 10))) " +
                "   (deref p))                   " +
                ") ";

        assertEquals(10L, venice.eval(script));
    }

    @Test
    public void test_promise_3() {
        final Venice venice = new Venice();

        final String script =
                "(do                          " +
                "   (defn task []             " +
                "      (sleep 500)            " +
                "      (deliver p 123))       " +
                "                             " +
                "   (def p (promise task))    " +
                "   (deref p))                " +
                ") ";

        assertEquals(123L, venice.eval(script));
    }

    @Test
    public void test_promise_future() {
        final Venice venice = new Venice();

        final String script =
                "(do                                 \n" +
                "   (def p (promise))                \n" +
                "   (future #(deliver p 100))        \n" +
                "   @p)                              ";

        assertEquals(100L, venice.eval(script));
    }

    @Test
    public void test_promise_deliver_ex_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                             \n" +
                "   (def p (promise))                            \n" +
                "   (deliver-ex p (ex :VncException \"error\"))  \n" +
                "   (deliver p 20)                               \n" +
                "   @p)                                          ";

        assertThrows(VncException.class, () -> venice.eval(script));
    }

    @Test
    public void test_promise_deliver_ex_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                             \n" +
                "   (def p (promise))                            \n" +
                "   (deliver-ex p 100)                           \n" +
                "   (deliver p 20)                               \n" +
                "   @p)                                          ";

        assertThrows(ValueException.class, () -> venice.eval(script));
    }

    @Test
    public void test_promise_dereferenceable() {
        final Venice venice = new Venice();

        final String script = "(deref? (promise))";

        final Object result = venice.eval(script);

        assertTrue((Boolean)result);
    }

    @Test
    public void test_promise_all_of() {
        final Venice venice = new Venice();

        final String script =
                "(-> (all-of (promise (fn [] (sleep 100) 1))  \n" +
                "            (promise (fn [] (sleep 100) 2))  \n" +
                "            (promise (fn [] (sleep 500) 3))) \n" +
                "    (deref))";

        assertEquals(null, venice.eval(script));
    }

    @Test
    public void test_promise_any_of() {
        final Venice venice = new Venice();

        final String script =
                "(-> (any-of (promise (fn [] (sleep 300) 1))  \n" +
                "            (promise (fn [] (sleep 100) 2))  \n" +
                "            (promise (fn [] (sleep 500) 3))) \n" +
                "    (deref))";

        assertEquals(2L, venice.eval(script));
    }

    @Test
    public void test_promise_then_apply_1a() {
        final Venice venice = new Venice();

        final String script =
                "(-> (promise (fn [] 5))      \n" +
                "    (then-apply #(+ % 3))    \n" +
                "    (then-apply #(* % 2))    \n" +
                "    (deref))";

        assertEquals(16L, venice.eval(script));
    }

    @Test
    public void test_promise_then_apply_2() {
        final Venice venice = new Venice();

        final String script =
                "(-> (promise (fn [] 5))                       \n" +
                "    (then-apply #(do (sleep 100) (+ % 3)))    \n" +
                "    (then-apply #(do (sleep 2) (* % 2)))      \n" +
                "    (deref))";

        assertEquals(16L, venice.eval(script));
    }

    @Test
    public void test_promise_then_apply_3() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                 \n" +
                "  (def x (atom 0))                                  \n" +
                "  (def p (-> (promise (fn [] (swap! x inc)))        \n" +
                "             (then-apply (fn [_] (swap! x inc)))    \n" +
                "             (then-apply (fn [_] (swap! x inc)))))  \n" +
                "  @p                                                \n" +
                "  @x)";

        assertEquals(3L, venice.eval(script));
    }

    @Test
    public void test_promise_then_apply_exception_1a() {
        final Venice venice = new Venice();

        final String script =
                "(-> (promise (fn [] (/ 1 0)))               \n" +
                "    (then-apply #(do (sleep 100) (+ % 3)))  \n" +
                "    (then-apply #(do (sleep 2) (* % 2)))    \n" +
                "    (deref))";

        try {
            venice.eval(script);
            fail("Expected VncException");
        }
        catch(VncException ex) {
            assertEquals("/ by zero", ex.getMessage());
        }
    }

    @Test
    public void test_promise_then_apply_exception_1b() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                  \n" +
                "  (def x (atom 0))                                   \n" +
                "  (def p (-> (promise #(/ 1 0))                      \n" +
                "             (then-apply (fn [_] (swap! x inc)))     \n" +
                "             (then-apply (fn [_] (swap! x inc)))))   \n" +
                "  (try                                               \n" +
                "    (deref p)                                        \n" +
                "    -1                                               \n" +
                "    (catch :VncException e @x)))                       ";

        assertEquals(0L, venice.eval(script));
    }

    @Test
    public void test_promise_then_apply_exception_2a() {
        final Venice venice = new Venice();

        final String script =
                "(-> (promise (fn [] 5))                       \n" +
                "    (then-apply #(do (sleep 100) (/ % 0)))    \n" +
                "    (then-apply #(do (sleep 2) (* % 2)))      \n" +
                "    (deref))";

        try {
            venice.eval(script);
            fail("Expected VncException");
        }
        catch(VncException ex) {
            assertEquals("/ by zero", ex.getMessage());
        }
    }

    @Test
    public void test_promise_then_apply_exception_2b() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                 \n" +
                "  (def x (atom 0))                                  \n" +
                "  (def p (-> (promise (fn [] (swap! x inc)))        \n" +
                "             (then-apply #(/ % 0))                  \n" +
                "             (then-apply (fn [_] (swap! x inc)))))  \n" +
                "  (try                                              \n" +
                "    (deref p)                                       \n" +
                "    -1                                              \n" +
                "    (catch :VncException e @x)))                      ";

        assertEquals(1L, venice.eval(script));
    }

    @Test
    public void test_promise_then_apply_exception_3a() {
        final Venice venice = new Venice();

        final String script =
                "(-> (promise (fn [] 5))                       \n" +
                "    (then-apply #(do (sleep 100) (+ % 3)))    \n" +
                "    (then-apply #(do (sleep 2) (/ % 0)))      \n" +
                "    (deref))";

        try {
            venice.eval(script);
            fail("Expected VncException");
        }
        catch(VncException ex) {
            assertEquals("/ by zero", ex.getMessage());
        }
    }

    @Test
    public void test_promise_then_apply_exception_3b() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                \n" +
                "  (def x (atom 0))                                 \n" +
                "  (def p (-> (promise (fn [] (swap! x inc)))       \n" +
                "             (then-apply (fn [_] (swap! x inc)))   \n" +
                "             (then-apply #(/ % 0))))               \n" +
                "  (try                                             \n" +
                "    (deref p)                                      \n" +
                "    -1                                             \n" +
                "    (catch :VncException e @x)))                     ";

        assertEquals(2L, venice.eval(script));
    }

    @Test
    public void test_promise_then_combine_1() {
        final Venice venice = new Venice();

        final String script =
                "(-> (promise (fn [] (sleep 20) 5))                              \n" +
                "    (then-apply (fn [x] (sleep 20) (+ x 3)))                    \n" +
                "    (then-combine (-> (promise (fn [] (sleep 20) 5))            \n" +
                "                      (then-apply (fn [x] (sleep 20) (* x 2)))) \n" +
                "                  #(+ %1 %2))                                   \n" +
                "    (deref))";

        assertEquals(18L, venice.eval(script));
    }

    @Test
    public void test_promise_then_compose_1() {
        final Venice venice = new Venice();

        final String script =
                "(-> (promise (fn [] 5))                                           \n" +
                "    (then-apply (fn [x] (* x 2)))                                 \n" +
                "    (then-compose (fn [x] (-> (promise (fn [] 6))                 \n" +
                "                              (then-apply (fn [y] (* y 3)))       \n" +
                "                              (then-apply (fn [y] (+ x y))))))    \n" +
                "    (deref))";

        assertEquals(28L, venice.eval(script));  // 5 * 2 + 6 * 3
    }

    @Test
    public void test_promise_when_complete_1() {
        final Venice venice = new Venice();

        final String script =
                "(let [result (promise)                                     \n" +
                "      p      (promise)]                                    \n" +
                "  (thread #(deliver p 5))                                  \n" +
                "  (let [q (then-apply p (fn [v] (* v 2)))]                 \n" +
                "    (when-complete q (fn [v,e] (deliver result (+ v 1))))  \n" +
                "    @result))                                               ";

        assertEquals(11L, venice.eval(script));  // 5 * 2 + 1
    }

    @Test
    public void test_promise_then_accept_1() {
        final Venice venice = new Venice();

        final String script =
                "(let [result (promise)                                \n" +
                "      p      (promise)]                               \n" +
                "  (thread #(deliver p 5))                             \n" +
                "  (then-accept p (fn [v] (deliver result (+ v 2))))   \n" +
                "  @result))                                           ";

        assertEquals(7L, venice.eval(script));
    }

    @Test
    public void test_promise_then_accept_2() {
        final Venice venice = new Venice();

        final String script =
                "(let [result (promise)                                \n" +
                "      p      (promise)]                               \n" +
                "  (thread #(deliver p 5))                             \n" +
                "  (then-accept p (fn [v] (deliver result (+ v 2))))   \n" +
                "  @p))                                           ";

        assertEquals(5L, venice.eval(script));
    }

    @Test
    public void test_promise_or_timeout_1() {
        final Venice venice = new Venice();

        final String script =
                "(-> (promise (fn [] (sleep 50) 100))  \n" +
                "    (or-timeout 100 :milliseconds)    \n" +
                "    (deref))";

        assertEquals(100L, venice.eval(script));
    }

    @Test
    public void test_promise_or_timeout_2() {
        final Venice venice = new Venice();

        final String script =
                "(-> (promise (fn [] (sleep 150) 100))  \n" +
                "    (or-timeout 100 :milliseconds)     \n" +
                "    (deref))";

        assertThrows(TimeoutException.class, () -> venice.eval(script));
    }

    @Test
    public void test_promise_or_timeout_3() {
        final Venice venice = new Venice();

        final String script =
                "(-> (promise (fn [] (sleep 50) 100))         \n" +
                "    (then-apply #(do (sleep 50) (* % 3)))    \n" +
                "    (or-timeout 200 :milliseconds)           \n" +
                "    (deref))";

        assertEquals(300L, venice.eval(script));
    }

    @Test
    public void test_promise_or_timeout_4() {
        final Venice venice = new Venice();

        final String script =
                "(-> (promise (fn [] (sleep 100) 100))         \n" +
                "    (or-timeout 150 :milliseconds)            \n" +
                "    (then-apply #(do (sleep 100) (* % 3)))    \n" +
                "    (or-timeout 150 :milliseconds)            \n" +
                "    (deref))";

        assertThrows(TimeoutException.class, () -> venice.eval(script));
    }

    @Test
    public void test_promise_or_timeout_5() {
        final Venice venice = new Venice();

        final String script =
                "(-> (promise (fn [] (sleep 50) 100))          \n" +
                "    (or-timeout 100 :milliseconds)            \n" +
                "    (then-apply #(do (sleep 200) (* % 3)))    \n" +
                "    (or-timeout 300 :milliseconds)            \n" +
                "    (deref))";

        assertEquals(300L, venice.eval(script));
    }

    @Test
    public void test_promise_or_timeout_6() {
        final Venice venice = new Venice();

        final String script =
                "(-> (promise (fn [] (sleep 50) 100))          \n" +
                "    (or-timeout 100 :milliseconds)            \n" +
                "    (then-apply #(do (sleep 200) (* % 3)))    \n" +
                "    (or-timeout 220 :milliseconds)   ; fires! \n" +
                "    (deref))";

        assertThrows(TimeoutException.class, () -> venice.eval(script));
    }

    @Test
    public void test_promise_complete_on_timeout_1() {
        final Venice venice = new Venice();

        final String script =
                "(-> (promise (fn [] (sleep 50) 100))               \n" +
                "    (complete-on-timeout 999 100 :milliseconds)    \n" +
                "    (deref))";

        assertEquals(100L, venice.eval(script));
    }

    @Test
    public void test_promise_complete_on_timeout_2() {
        final Venice venice = new Venice();

        final String script =
                "(-> (promise (fn [] (sleep 150) 100))               \n" +
                "    (complete-on-timeout 999 100 :milliseconds)     \n" +
                "    (deref))";

        assertEquals(999L, venice.eval(script));
    }

    @Test
    public void test_promise_complete_on_timeout_3() {
        final Venice venice = new Venice();

        final String script =
                "(-> (promise (fn [] (sleep 50) 100))              \n" +
                "    (then-apply #(do (sleep 50) (* % 3)))         \n" +
                "    (complete-on-timeout 999 200 :milliseconds)   \n" +
                "    (deref))";

        assertEquals(300L, venice.eval(script));
    }

    @Test
    public void test_promise_complete_on_timeout_4() {
        final Venice venice = new Venice();

        final String script =
                "(-> (promise (fn [] (sleep 50) 100))               \n" +
                "    (complete-on-timeout 888 100 :milliseconds)    \n" +
                "    (then-apply #(do (sleep 200) (* % 3)))         \n" +
                "    (complete-on-timeout 999 300 :milliseconds)    \n" +
                "    (deref))";

        assertEquals(300L, venice.eval(script));
    }

    @Test
    public void test_promise_complete_on_timeout_5() {
        final Venice venice = new Venice();

        final String script =
                "(-> (promise (fn [] (sleep 50) 100))             \n" +
                "    (complete-on-timeout 888 100 :milliseconds)  \n" +
                "    (then-apply #(do (sleep 200) (* % 3)))       \n" +
                "    (complete-on-timeout 999 220 :milliseconds)  \n" +
                "    (deref))";

        assertEquals(999L, venice.eval(script));
    }

    @Test
    public void test_promise_complete_on_timeout_6() {
        final Venice venice = new Venice();

        final String script =
                "(-> (promise (fn [] (sleep 50) 100))             \n" +
                "    (complete-on-timeout 888 30 :milliseconds)  \n" +
                "    (then-apply #(do (sleep 200) (* % 3)))       \n" +
                "    (complete-on-timeout 999 400 :milliseconds)  \n" +
                "    (deref))";

        assertEquals(2664L, venice.eval(script));
    }

    @Test
    public void test_promise_complete_on_timeout_7() {
        final Venice venice = new Venice();

        final String script =
                "(-> (promise (fn [] (sleep 50) 100))             \n" +
                "    (complete-on-timeout 888 100 :milliseconds)  \n" +
                "    (then-apply #(do (sleep 200) (* % 3)))       \n" +
                "    (complete-on-timeout 999 400 :milliseconds)  \n" +
                "    (deref))";

        assertEquals(300L, venice.eval(script));
    }

    @Test
    public void test_promise_accept_either_1a() {
        final Venice venice = new Venice();

        final String script =
        		"(let [result (promise)]                                  \n" +
                "  (-> (promise (fn [] (sleep 200) 200))                  \n" +
                "      (accept-either (promise (fn [] (sleep 100) 100))   \n" +
                "                     (fn [v] (deliver result v))))       \n" +
                "  @result)";

        assertEquals(100L, venice.eval(script));
    }

    @Test
    public void test_promise_accept_either_1b() {
        final Venice venice = new Venice();

        final String script =
        		"(let [result (promise)]                                  \n" +
                "  (-> (promise (fn [] (sleep 200) 200))                  \n" +
                "      (accept-either (promise (fn [] (sleep 100) 100))   \n" +
                "                     (fn [v] (deliver result v) v))      \n" +
                "      (deref)))";

        assertEquals(null, venice.eval(script));
    }

    @Test
    public void test_promise_accept_either_2() {
        final Venice venice = new Venice();

        final String script =
        		"(let [result (promise)]                                  \n" +
                "  (-> (promise (fn [] (sleep 100) 100))                  \n" +
                "      (accept-either (promise (fn [] (sleep 200) 200))   \n" +
                "                     (fn [v] (deliver result v))))       \n" +
                "  @result)";

        assertEquals(100L, venice.eval(script));
    }

    @Test
    public void test_promise_apply_to_either_1() {
        final Venice venice = new Venice();

        final String script =
                "(-> (promise (fn [] (sleep 200) 200))                  \n" +
                "    (apply-to-either (promise (fn [] (sleep 100) 100)) \n" +
                "                     (fn [v] (+ v 1)))                 \n" +
                "    (deref))";

        assertEquals(101L, venice.eval(script));
    }

    @Test
    public void test_promise_apply_to_either_2() {
        final Venice venice = new Venice();

        final String script =
                "(-> (promise (fn [] (sleep 100) 100))                  \n" +
                "    (apply-to-either (promise (fn [] (sleep 200) 200)) \n" +
                "                     (fn [v] (+ v 1)))                 \n" +
                "    (deref))";

        assertEquals(101L, venice.eval(script));
    }

    @Test
    public void test_promise_then_accep_both_1() {
        final Venice venice = new Venice();

        final String script =
        		"(let [result (promise)]                                          \n" +
                "  (-> (promise (fn [] (sleep 200) 200))                          \n" +
                "      (then-accept-both (promise (fn [] (sleep 100) 100))        \n" +
                "                        (fn [u v] (deliver result (+ u v)) 4)))  \n" +
                "  @result)";

        assertEquals(300L, venice.eval(script));
    }

    @Test
    public void test_promise_then_accep_both_2() {
        final Venice venice = new Venice();

        final String script =
        		"(let [result (promise)]                                          \n" +
                "  (-> (promise (fn [] (sleep 200) 200))                          \n" +
                "      (then-accept-both (promise (fn [] (sleep 100) 100))        \n" +
                "                        (fn [u v] (deliver result (+ u v)) 4))   \n" +
                "      (deref)))";

        assertEquals(null, venice.eval(script));
    }

    @Test
    public void test_future_deref_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                             " +
                "   (let [f (future (fn [] {:a 100}))]           " +
                "      @f)) ";

        assertEquals("{:a 100}", venice.eval("(str " + script + ")"));
    }

    @Test
    public void test_future_deref_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                             " +
                "   (let [f (future (fn [] {:a 100}))]           " +
                "      (conj @f {:c 3}))                         " +
                ") ";

        assertEquals("{:a 100 :c 3}", venice.eval("(str " + script + ")"));
    }

    @Test
    public void test_future_deref_3() {
        final Venice venice = new Venice();

        final String script =
                "(do                                   " +
                "   (let [a 100                        " +
                "      f (future (fn [] (+ a 20)))]    " +
                "    @f)                               " +
                ") ";

        assertEquals(120L, venice.eval(script));
    }

    @Test
    public void test_future_deref_4() {
        final Venice venice = new Venice();

        final String script =
                "(do                                   " +
                "  (defn sum [x y] (+ x y))            " +
                "  (let [f (future (partial sum 3 4))] " +
                "    @f)) ";

        assertEquals(7L, venice.eval(script));
    }

    @Test
    public void test_future_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                             " +
                "   (defn wait [] (sleep 500) {:a 100})          " +
                "                                                " +
                "   (let [f (future wait)]                       " +
                "      (deref f))                                " +
                ") ";

        assertEquals("{:a 100}", venice.eval("(str " + script + ")"));
    }

    @Test
    public void test_future_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                        " +
                "   (defn wait [] (sleep 500) 100)          " +
                "                                           " +
                "   (let [f (future wait)]                  " +
                "      (deref f 700 :timeout))              " +
                ") ";

        assertEquals(Long.valueOf(100), venice.eval(script));
    }

    @Test
    public void test_future_dereferenceable() {
        final Venice venice = new Venice();

        final String script = "(deref? (future (fn [] 10)))";

        final Object result = venice.eval(script);

        assertTrue((Boolean)result);
    }

    @Test
    public void test_future_exception() {
        final Venice venice = new Venice();

        final String script =
                "(do                                              \n" +
                "   (def wait (fn [] (do (sleep 500) (throw 1)))) \n" +
                "                                                 \n" +
                "   (let [f (future wait)]                        \n" +
                "      (deref f))                                 \n" +
                ") ";

        assertThrows(ValueException.class, () -> venice.eval(script));
    }

    @Test
    public void test_future_error() {
        final Venice venice = new Venice();

        // Note: the "Symbol 'xxx' not found" exception does not bubble up to the
        //       caller. Without dereferencing the future the exception is not
        //       propagated! 'p' will never get a value delivered!
        final String script =
                "(do                                 \n" +
                "   (def p (promise))                \n" +
                "   (future #(deliver p xxx))        \n" +  // Symbol 'xxx' not found!
                "   (deref p 300 :timeout))            ";

        assertEquals("timeout", venice.eval(script));
    }

    @Test
    public void test_future_timeout() {
        final Venice venice = new Venice();

        final String script =
                "(do                                        " +
                "   (defn wait [] (sleep 500) 100)          " +
                "                                           " +
                "   (let [f (future wait)]                  " +
                "      (deref f 300 :timeout))              " +
                ") ";

        assertEquals("timeout", venice.eval(script));
    }

    @Test
    public void test_future_done() {
        final Venice venice = new Venice();

        final String script =
                "(do                                 \n" +
                "   (defn wait [] (sleep 200) 100)   \n" +
                "                                    \n" +
                "   (def f (future wait))            \n" +
                "   (deref f)                        \n" +
                "   (done? f))                         ";

        assertEquals(true, venice.eval(script));
    }

    @Test
    public void test_future_cancel_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                        \n" +
                "   (defn worker []                         \n" +
                "     (sleep 500)                           \n" +
                "     100)                                  \n" +
                "                                           \n" +
                "   (def f (future worker))                 \n" +
                "   (sleep 100)                             \n" +
                "   (cancel f)                              \n" +
                "   (cancelled? f))                           ";

        assertEquals(true, venice.eval(script));
    }

    @Test
    public void test_future_cancel_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                        \n" +
                "   (defn worker []                         \n" +
                "     (sleep 500)                           \n" +
                "     100)                                  \n" +
                "                                           \n" +
                "   (def f (future worker))                 \n" +
                "   (sleep 100)                             \n" +
                "   (cancel f)                              \n" +
                "   (done? f))                                ";

        assertEquals(true, venice.eval(script));
    }

    @Test
    public void test_future_cancel_3() {
        final Venice venice = new Venice();

        final String script =
                "(do                                        \n" +
                "   (def progress (atom nil))               \n" +
                "   (defn worker []                         \n" +
                "     (reset! progress :started)            \n" +
                "     (sleep 500)                           \n" +
                "     (reset! progress :end)                \n" +
                "     100)                                  \n" +
                "                                           \n" +
                "   (def f (future worker))                 \n" +
                "   (sleep 100)                             \n" +
                "   (cancel f)                              \n" +
                "   (sleep 500)                             \n" +
                "   @progress)                                ";

        assertEquals("started", venice.eval(script));
    }

    @Test
    public void test_future_thread_local_inherited() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                      \n" +
                "   (assoc! (thread-local) :a 10 :b 20)                   \n" +
                "   (assoc! (thread-local) :a 11)                         \n" +
                "   (let [f (future (fn [] (get (thread-local) :a)))]     \n" +
                "        @f)                                              \n" +
                ") ";

        assertEquals(11L, venice.eval(script));
    }

    @Test
    public void test_future_thread_local_parent_untouched() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                        \n" +
                "   (assoc! (thread-local) :a 10 :b 20)                     \n" +
                "   (assoc! (thread-local) :a 11)                           \n" +
                "   [ (let [f (future (fn []                                \n" +
                "                         (assoc! (thread-local) :a 90)     \n" +
                "                         (get (thread-local) :a)))]        \n" +
                "        @f)                                                \n" +
                "     (get (thread-local) :a) ]                             \n" +
                ") ";

        assertEquals("[90 11]", venice.eval("(str " + script + ")"));
    }

    @Test
    public void test_future_task_1a() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                   \n" +
                "   (def q (queue))                                    \n" +
                "   (defn wait [s v] (sleep s) v)                      \n" +
                "   (future-task (partial wait 200 2) #(offer! q @%))  \n" +
                "   (future-task (partial wait 300 3) #(offer! q @%))  \n" +
                "   (future-task (partial wait 100 1) #(offer! q @%))  \n" +
                "   (pr-str [ (poll! q :indefinite)                    \n" +
                "             (poll! q :indefinite)                    \n" +
                "             (poll! q :indefinite) ] ))                ";

        assertEquals("[1 2 3]", venice.eval(script));
    }

    @Test
    public void test_future_task_1b() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                \n" +
                "   (def q (queue))                                                 \n" +
                "   (defn wait [s v] (sleep s) v)                                   \n" +
                "   (future-task (partial wait 200 2) #(offer! q %) #(offer! q %))  \n" +
                "   (future-task (partial wait 300 3) #(offer! q %) #(offer! q %))  \n" +
                "   (future-task (partial wait 100 1) #(offer! q %) #(offer! q %))  \n" +
                "   (pr-str [ (poll! q :indefinite)                                 \n" +
                "             (poll! q :indefinite)                                 \n" +
                "             (poll! q :indefinite) ] ))                              ";

        assertEquals("[1 2 3]", venice.eval(script));
    }

    @Test
    public void test_future_task_2a() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                         \n" +
                "   (def q (queue))                                          \n" +
                "   (future-task #(/ 1 0)                                    \n" +
                "                #(try                                       \n" +
                "                   @%                                       \n" +
                "                   (catch :VncException e (offer! q e))))   \n" +
                "   (poll! q :indefinite))                                     ";

        final Object ret = venice.eval(script);
        assertTrue(ret instanceof VncException);
        assertTrue(((VncException)ret).getMessage().contains("/ by zero"));
    }

    @Test
    public void test_future_task_2b() {
        final Venice venice = new Venice();

        final String script =
                "(do                               \n" +
                "   (def q (queue))                \n" +
                "   (future-task #(/ 1 0)          \n" +
                "                #(offer! q %)     \n" +
                "                #(offer! q %))    \n" +
                "   (poll! q :indefinite))           ";

        final Object ret = venice.eval(script);
        assertTrue(ret instanceof VncException);
        assertTrue(((VncException)ret).getMessage().contains("/ by zero"));
    }

    @Test
    public void test_locking() {
        final Venice venice = new Venice();

        final String script =
                "(do                                         \n" +
                "   (def x 1)                                \n" +
                "                                            \n" +
                "   (with-out-str                            \n" +
                "      (future (fn []                        \n" +
                "                  (locking x                \n" +
                "                     (sleep 2000)           \n" +
                "                     (println \"done1\")))) \n" +
                "                                            \n" +
                "      (sleep 200)                           \n" +
                "                                            \n" +
                "      (locking x                            \n" +
                "         (sleep 1000)                       \n" +
                "         (println \"done2\")))              \n" +
                ") ";

        assertEquals("done1\ndone2\n", venice.eval(script));
    }

    @Test
    public void test_thread_1a() {
        final Venice venice = new Venice();

        final String script =
                "(let [p (thread #(thread-name)  \"AAA\")]   \n" +
                "  @p)                                       ";

        assertTrue(((String)venice.eval(script)).matches("AAA-[0-9]+"));
    }

    @Test
    public void test_thread_1b() {
        final Venice venice = new Venice();

        final String script =
                "(let [p (thread #(thread-name))]   \n" +
                "  @p)                               ";

        assertTrue(((String)venice.eval(script)).matches("venice-thread-[0-9]+"));
    }

    @Test
    public void test_thread_2() {
        final Venice venice = new Venice();

        final String script =
                "(let [p (thread #(thread-daemon?))]  \n" +
                "  @p)                                ";

        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void test_thread_3() {
        final Venice venice = new Venice();

        final String script =
                "(let [p (thread #(do (sleep 100) 30))]  \n" +
                "  @p)                                        ";

        assertEquals(30L, venice.eval(script));
    }

    @Test
    public void test_thread_4() {
        final Venice venice = new Venice();

        final String script =
                "(let [p1 (thread #(do (sleep 300) 30))   \n" +
                "      p2 (thread #(do (sleep 100) 10))]  \n" +
                "  (str @p1 \":\" @p2))                    ";

        assertEquals("30:10", venice.eval(script));
    }

    @Test
    public void test_thread_5() {
        final Venice venice = new Venice();

        final String script =
                "(let [q (queue)]                                \n" +
                "  (-> (thread #(do (sleep 100) 30))             \n" +
                "      (when-complete (fn [v,e] (offer! q v))))  \n" +
                "  (poll! q 200))                                     ";

        assertEquals(30L, venice.eval(script));
    }

    @Test
    public void test_thread_id() {
        final Venice venice = new Venice();

        assertNotNull(venice.eval("(thread-id)"));
    }

    @Test
    public void test_thread_name() {
        final Venice venice = new Venice();

        assertNotNull(venice.eval("(thread-name)"));
    }

    @Test
    public void test_thread_local_clear() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                        \n" +
                " (thread-local :a 1 :b 2)                                  \n" +
                " (thread-local-clear)                                      \n" +
                " (let [m (thread-local-map)]                               \n" +
                "   (assert (= :unknown (get (thread-local) :a :unknown)))  \n" +
                "   (assert (= :unknown (get (thread-local) :b :unknown)))  \n" +
                "   (assert (some? (get (thread-local) :*in*)))             \n" +
                "   (assert (some? (get (thread-local) :*out*)))            \n" +
                "   (assert (some? (get (thread-local) :*err*)))))          \n";

        venice.eval(script);
    }

}
