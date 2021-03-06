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
package com.github.jlangch.venice.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.PrintStream;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.javainterop.JavaInterop;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.concurrent.ThreadLocalMap;
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

		assertEquals("alpha\nbeta\nalpha\n", venice.eval(script));
	}

	@Test
	public void test_namespace_symbol() {
		final Venice venice = new Venice();

		assertEquals("", venice.eval("(namespace 'foo)"));
		assertEquals("xxx", venice.eval("(namespace 'xxx/foo)"));
	}

	@Test
	public void test_namespace_keyword() {
		final Venice venice = new Venice();

		assertEquals("", venice.eval("(namespace :alpha)"));
		assertEquals("user", venice.eval("(namespace :user/alpha)"));
	}

	@Test
	public void test_namespace_function() {
		final Venice venice = new Venice();

		final String script =
				"(do                         \n" +
				"   (ns xxx)                 \n" +
				"   (defn f1 [x] (+ x 1))    \n" +
				"   (namespace f1))            ";

		assertEquals("xxx", venice.eval(script));
	}

	@Test
	public void test_namespace_anonymous_function() {
		final Venice venice = new Venice();

		final String script =
				"(do                               \n" +
				"   (ns xxx)                       \n" +
				"   (defn f1 [f] (namespace f))    \n" +
				"   (f1 #(+ 1)))                     ";

		assertEquals("xxx", venice.eval(script));
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

		assertEquals("alpha\nbeta\nbeta\nalpha\nbeta\n", venice.eval(script));
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

		assertEquals("gamma\n100\ndelta\n100\n", venice.eval(script));
	}

	@Test
	public void test_namespace_in_macro_evaluation_runtime_2() {
		// Macros are evaluated in the namespace they are called from!

		final IInterceptor interceptor = new AcceptAllInterceptor();
		ThreadLocalMap.remove(); // clean thread locals			
		JavaInterop.register(interceptor);

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

		assertEquals("gamma\n100\ndelta\n100\n", result2.toString());
	}

	@Test
	public void test_namespace_in_macro_evaluation_runtime_3() {
		// Macros are evaluated in the namespace they are called from!

		final IInterceptor interceptor = new AcceptAllInterceptor();
		ThreadLocalMap.remove(); // clean thread locals			
		JavaInterop.register(interceptor);

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
		assertEquals("gamma\ndelta\n", result2.toString());
	}

	@Test
	public void test_namespace_in_macro_evaluation_upfront_1() {
		// Macros are evaluated in the namespace they are called from!

		final IInterceptor interceptor = new AcceptAllInterceptor();
		ThreadLocalMap.remove(); // clean thread locals			
		JavaInterop.register(interceptor);

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

		final IInterceptor interceptor = new AcceptAllInterceptor();
		ThreadLocalMap.remove(); // clean thread locals			
		JavaInterop.register(interceptor);

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
		venice.setMacroExpandOnLoad(true, env);
		
		final VncVal result2 = venice.RE(script, "test", env);

		// stdout:  "gamma\ndelta"   -> macro expansion takes place before (with-out-str ...) 
		//                              has been applied, so stdout redirection is not yet in
		//                              place the the time the macro 'alpha/whenn' is run.
		// result:  "100\n100\n"     -> OK
		assertEquals("100\n100\n", result2.toString());  
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
				"	(defmulti math-op (fn [s] (:op s)))                          \n" +
				"                                                                \n" +
				"	(defmethod math-op \"add\" [s] (+ (:op1 s) (:op2 s)))        \n" +
				"	(defmethod math-op \"subtract\" [s] (- (:op1 s) (:op2 s)))   \n" +
				"	(defmethod math-op :default [s] 0)                           \n" +
				"                                                                \n" +
				"   (ns B)                                                       \n" +
				"                                                                \n" +
				"   (str                                                         \n" +
				"	   [ (A/math-op {:op \"add\"      :op1 1 :op2 5})            \n" +
				"	     (A/math-op {:op \"subtract\" :op1 1 :op2 5})            \n" +
				"	     (A/math-op {:op \"bogus\"    :op1 1 :op2 5}) ] ))       \n" +
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
