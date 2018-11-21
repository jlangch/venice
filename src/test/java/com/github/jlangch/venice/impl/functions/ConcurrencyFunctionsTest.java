/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2018 Venice
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

import java.io.File;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.javainterop.Interceptor;
import com.github.jlangch.venice.javainterop.SandboxInterceptor;
import com.github.jlangch.venice.javainterop.SandboxRules;
import com.github.jlangch.venice.util.CapturingPrintStream;


public class ConcurrencyFunctionsTest {

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
	public void test_agent_send() {
		final Venice venice = new Venice();

		final String script = 
				"(do                                 \n" +
				"   (defn increment [c n] (+ c n))   \n" +
				"   (def x (agent 100))              \n" +
				"   (send x increment 5)             \n" +
				"   (sleep 100)                      \n" +
				"   (deref x))                         ";

		final Object result = venice.eval(script);
		
		assertEquals(Long.valueOf(105), result);
	}

	@Test
	public void test_agent_send_off() {
		final Venice venice = new Venice();

		final String script = 
				"(do                                 \n" +
				"   (defn increment [c n] (+ c n))   \n" +
				"   (def x (agent 100))              \n" +
				"   (send-off x increment 5)         \n" +
				"   (sleep 100)                      \n" +
				"   (deref x))                         ";

		final Object result = venice.eval(script);
		
		assertEquals(Long.valueOf(105), result);
	}

	@Test
	public void test_agent_watch() {
		final Venice venice = new Venice();

		final String script = 
				"(do                                                                     \n" +
				"   (defn increment [c n] (+ c n))                                       \n" +
				"   (defn watcher [key ref old new]                                      \n" +
				"         (println \"watcher: \" key \", old:\" old \", new:\" new ))    \n" +
				"   (def x (agent 100))                                                  \n" +
				"   (add-watch x :test watcher)                                          \n" +
				"   (send x increment 5)                                                 \n" +
				"   (sleep 100)                                                          \n" +
				"   (deref x))                                                             ";

		final Object result = venice.eval(script);
		
		assertEquals(Long.valueOf(105), result);
	}

	@Test
	@Disabled
	public void test_agent_relay() {
		final Venice venice = new Venice();

		// Agents as message relay
		
		final String script = 
				"(do                                                                         \n" +
				"   (def logger (agent (list)))                                              \n" +
				"                                                                            \n" +
				"   (defn log [msg]                                                          \n" +
				"      (send logger #(cons %2 %1) msg))                                      \n" +
				"                                                                            \n" +
				"   (defn create-relay [n]                                                   \n" +
				"      (reduce (fn [prev _] (agent prev)) nil (range 0 n)))                  \n" +
				"                                                                            \n" +
				"   (defn process [relay msg]                                                \n" +
				"      (let [relay-fn (fn [next-actor hop msg]                               \n" +
				"                         (if next-actor                                     \n" +
				"                            (do                                             \n" +
				"                               (log (list hop msg))                         \n" +
				"                               (send next-actor relay-fn (inc hop) msg)     \n" +
				"                               @next-actor)                                 \n" +
				"                            (log \"finished relay\") ))]                    \n" +
				"         (send relay relay-fn 0 msg)))                                      \n" +
				"                                                                            \n" +
				"   (process (create-relay 10) \"hello\")                                    \n" +
				"   (sleep 500)                                                              \n" +
				"   (println @logger))                                                         ";

		venice.eval(script);
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

		final CapturingPrintStream ps = CapturingPrintStream.create();

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
	public void test_promise() {
		final Venice venice = new Venice();

		final String script = 
				"(do                                        " +
				"   (def p (promise))                       " +
				"   (def task (fn []                        " +
				"                 (do                       " +
				"                    (sleep 500)            " +
				"                    (deliver p 123))))     " +
				"                                           " +
				"   (future task)                           " +
				"   (deref p))                              " +
				") ";

		assertEquals(Long.valueOf(123), venice.eval(script));
	}

	@Test
	public void test_future_deref() {
		final Venice venice = new Venice();

		final String script = 
				"(do                                             " +
				"   (let [f (future (fn [] {:a 100}))]           " +
				"        @f)                                     " +
				") ";

		assertEquals("{:a 100}", venice.eval("(str " + script + ")"));
	}

	@Test
	public void test_future_1() {
		final Venice venice = new Venice();

		final String script = 
				"(do                                             " +
				"   (def wait (fn [] (do (sleep 500) {:a 100}))) " +
				"                                                " +
				"   (let [f (future wait)]                       " +
				"        (deref f))                              " +
				") ";

		assertEquals("{:a 100}", venice.eval("(str " + script + ")"));
	}

	@Test
	public void test_future_2() {
		final Venice venice = new Venice();

		final String script = 
				"(do                                        " +
				"   (def wait (fn [] (do (sleep 500) 100))) " +
				"                                           " +
				"   (let [f (future wait)]                  " +
				"        (deref f 700 :timeout))            " +
				") ";

		assertEquals(Long.valueOf(100), venice.eval(script));
	}

	@Test
	public void test_future_timeout() {
		final Venice venice = new Venice();

		final String script = 
				"(do                                        " +
				"   (def wait (fn [] (do (sleep 500) 100))) " +
				"                                           " +
				"   (let [f (future wait)]                  " +
				"        (deref f 300 :timeout))            " +
				") ";

		assertEquals("timeout", venice.eval(script));
	}
	
	@Test
	public void test_future_not_sandboxed() {
			final Venice venice = new Venice();
	
			final String script = 
					"(do                                        " +
					"   (def wait (fn [] (sandboxed?)))         " +
					"                                           " +
					"   (let [f (future wait)]                  " +
					"        (deref f))                         " +
					") ";

			assertFalse((Boolean)venice.eval(script));
	}
	
	@Test
	public void test_future_sandboxed() {
		// all venice 'file' function blacklisted
		final Interceptor interceptor = 
				new SandboxInterceptor(new SandboxRules().withBlacklistedVeniceFn("io/file"));

		final Venice venice = new Venice(interceptor);

		final String script = 
				"(do                                        " +
				"   (def wait (fn [] (sandboxed?)))         " +
				"                                           " +
				"   (let [f (future wait)]                  " +
				"        (deref f))                         " +
				") ";

		assertTrue((Boolean)venice.eval(script));
	}
	
	@Test
	public void test_future_sandbox_violation() {
		// all venice 'file' function blacklisted
		final Interceptor interceptor = 
				new SandboxInterceptor(new SandboxRules().withBlacklistedVeniceFn("io/file"));

		final Venice venice = new Venice(interceptor);

		// 'io/file' is black listed, thus a call to 'io/file' must 
		// throw a SecurityException!
		final String script = 
				"(do                                        " +
				"   (def wait (fn [] (io/file \"a.txt\")))  " +
				"                                           " +
				"   (let [f (future wait)]                  " +
				"        (deref f))                         " +
				") ";

		assertThrows(SecurityException.class, () -> venice.eval(script));

	}
	
	@Test
	public void test_future_sandbox_ok() {
		// all venice 'file' function blacklisted
		final Interceptor interceptor = 
				new SandboxInterceptor(new SandboxRules().withBlacklistedVeniceFn("io/slurp"));

		final Venice venice = new Venice(interceptor);

		final String script = 
				"(do                                        " +
				"   (def wait (fn [] (io/file \"a.txt\")))  " +
				"                                           " +
				"   (let [f (future wait)]                  " +
				"        (deref f))                         " +
				") ";

		final File file = (File)venice.eval(script);
		assertEquals("a.txt", file.getName());
	}
	
	@Test
	public void test_thread_id() {
		final Venice venice = new Venice();

		assertNotNull((Long)venice.eval("(thread-id)"));
	}
	
	@Test
	public void test_thread_name() {
		final Venice venice = new Venice();

		assertNotNull((String)venice.eval("(thread-name)"));
	}

}
