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
package com.github.jlangch.venice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.util.CapturingPrintStream;


public class SpecialForms_TryTest {
	
	// ---------------------------------------------------------------
	// try - only
	// ---------------------------------------------------------------
	
	@Test
	public void test_try_empty() {
		final Venice venice = new Venice();

		final String lisp = "(try )";

		assertEquals(null, venice.eval(lisp));
	}

	@Test
	public void test_try_only_1() {
		final Venice venice = new Venice();

		final String lisp = "(try 200)";

		assertEquals(200L, venice.eval(lisp));
	}

	@Test
	public void test_try_only_2() {
		final Venice venice = new Venice();

		final String lisp = 
				"(with-out-str                \n" +
				"  (try                       \n" +
				"    (print 100)              \n" +
				"    (print 200)))              ";

		assertEquals("100200", venice.eval(lisp));
	}

	@Test
	public void test_try_only_3() {
		final Venice venice = new Venice();

		final String lisp = 
				"(with-out-str                \n" +
				"  (try                       \n" +
				"    (print 100)              \n" +
				"    (print 200)              \n" +
				"    (print 300)))              ";

		assertEquals("100200300", venice.eval(lisp));
	}

	
	// ---------------------------------------------------------------
	// try - throw
	// ---------------------------------------------------------------

	@Test
	public void test_try_throw_1() {
		final Venice venice = new Venice();

		final String lisp = 
				"(try           \n" +
				"  (throw ))      ";

		try {
			venice.eval(lisp);
		}
		catch(ValueException ex) {
			assertEquals(null, ex.getValue());
			return;
		}
		
		fail("Expected JavaValueException");
	}

	@Test
	public void test_try_throw_2() {
		final CapturingPrintStream ps = new CapturingPrintStream();

		final Venice venice = new Venice();

		final String lisp = 
				"(try               \n" +
				"  (print 100)      \n" +
				"  (print 200)      \n" +
				"  (throw ))        ";

		try {
			venice.eval(lisp, Parameters.of("*out*", ps));
		}
		catch(ValueException ex) {
			assertEquals(null, ex.getValue());
			assertEquals("100200", ps.getOutput());
			return;
		}

		fail("Expected JavaValueException");
	}

	@Test
	public void test_try_throw_3() {
		final Venice venice = new Venice();

		final String lisp = 
				"(try                          \n" +
				"  (throw \"test message\"))     ";

		try {
			venice.eval(lisp);
		}
		catch(ValueException ex) {
			assertEquals("test message", ex.getValue());
			return;
		}
		
		fail("Expected JavaValueException");
	}

	@Test
	public void test_try_throw_4() {
		final CapturingPrintStream ps = new CapturingPrintStream();

		final Venice venice = new Venice();

		final String lisp = 
				"(try                          \n" +
				"  (print 100)                 \n" +
				"  (print 200)                 \n" +
				"  (throw \"test message\"))     ";

		try {
			venice.eval(lisp, Parameters.of("*out*", ps));
		}
		catch(ValueException ex) {
			assertEquals("test message", ex.getValue());
			assertEquals("100200", ps.getOutput());
			return;
		}

		fail("Expected JavaValueException");
	}

	
	// ---------------------------------------------------------------
	// try - catch
	// ---------------------------------------------------------------

	@Test
	public void test_try_catch_1() {
		final Venice venice = new Venice();

		final String lisp = 
				"(try                                \n" +
				"  (+ 100 200)                       \n" +
				"  (catch :java.lang.Exception ex    \n" +
				"          -1))                        ";

		assertEquals(300L, venice.eval(lisp));
	}
	
	@Test
	public void test_try_catch_2() {
		final Venice venice = new Venice();

		final String lisp = 
				"(try                                \n" +
				"  200                               \n" +
				"  (+ 100 200)                       \n" +
				"  (catch :java.lang.Exception ex    \n" +
				"          -1))                     n  ";

		assertEquals(300L, venice.eval(lisp));
	}
	
	@Test
	public void test_try_catch_3() {
		final Venice venice = new Venice();

		final String lisp = 
				"(try                                \n" +
				"  200                               \n" +
				"  (+ 100 200)                       \n" +
				"  (catch :java.lang.Exception ex    \n" +
				"         (+ 1 2)                    \n" +
				"         -1))                        ";

		assertEquals(300L, venice.eval(lisp));
	}
	
	@Test
	public void test_try_catch_4() {
		final Venice venice = new Venice();

		final String lisp = 
				"(try                                \n" +
				"  (do (+ 100 200) )                 \n" +
				"  (catch :java.lang.Exception ex    \n" +
				"         (do                        \n" +
				"            (+ 1 2)                 \n" +
				"            (+ 3 4)                 \n" +
				"            -1)))                     ";

		assertEquals(300L, venice.eval(lisp));
	}

	
	// ---------------------------------------------------------------
	// try - throw - catch
	// ---------------------------------------------------------------

	@Test
	public void test_try_throw_catch_1() {
		final Venice venice = new Venice();

		// :ValueException is a :java.lang.Exception
		final String lisp = 
				"(try                                               \n" +
				"  (throw 100)                                      \n" +
				"  (catch :java.lang.Exception ex                   \n" +
				"         -1))                                        ";

		assertEquals(-1L, venice.eval(lisp));
	}
	
	@Test
	public void test_try_throw_catch_2() {
		final Venice venice = new Venice();

		// :ValueException is a :java.lang.RuntimeException
		final String lisp = 
				"(try                                               \n" +
				"  (throw 100)                                      \n" +
				"  (catch :java.lang.RuntimeException ex            \n" +
				"         -1))                                        ";

		assertEquals(-1L, venice.eval(lisp));
	}
	
	@Test
	public void test_try_throw_catch_3() {
		final Venice venice = new Venice();

		// :ValueException is a :VncException
		final String lisp = 
				"(try                                               \n" +
				"  (throw 100)                                      \n" +
				"  (catch :VncException ex                          \n" +
				"         -1))                                        ";

		assertEquals(-1L, venice.eval(lisp));
	}
	
	@Test
	public void test_try_throw_catch_4() {
		final Venice venice = new Venice();

		final String lisp = 
				"(try                                               \n" +
				"  (throw 100)                                      \n" +
				"  (catch :ValueException ex                        \n" +
				"         -1))                                        ";

		assertEquals(-1L, venice.eval(lisp));
	}
	
	@Test
	public void test_try_throw_catch_5() {
		final Venice venice = new Venice();

		// :ValueException is NOT a :java.lang.IllegalArgumentException
		final String lisp = 
				"(try                                               \n" +
				"  (throw 100)                                      \n" +
				"  (catch :java.lang.IllegalArgumentException ex    \n" +
				"         -1))                                      ";

		try {
			venice.eval(lisp);
			
			fail("Expected JavaValueException");
		}
		catch(ValueException ex) {
			assertEquals(Long.valueOf(100), ex.getValue());
		}
	}
	
	@Test
	public void test_try_throw_catch_6() {
		final Venice venice = new Venice();

		final String lisp = 
				"(try                                               \n" +
				"  (throw 100)                                      \n" +
				"  (catch :IllegalArgumentException ex              \n" +
				"         -1)                                       \n" +
				"  (catch :ValueException ex                        \n" +
				"         -2))                                        ";

		assertEquals(-2L, venice.eval(lisp));
	}
	
	@Test
	public void test_try_throw_catch_7() {
		final Venice venice = new Venice();

		final String lisp = 
				"(try                                               \n" +
				"  (throw 100)                                      \n" +
				"  (catch :IllegalArgumentException ex              \n" +
				"         -1)                                       \n" +
				"  (catch :RuntimeException ex                      \n" +
				"         -2)                                       \n" +
				"  (catch :ValueException ex                        \n" +
				"         -3))                                        ";

		assertEquals(-2L, venice.eval(lisp));
	}
	
	@Test
	public void test_try_throw_catch_8() {
		final Venice venice = new Venice();

		final String lisp = 
				"(try                                               \n" +
				"  (throw 100)                                      \n" +
				"  (catch :java.lang.IllegalArgumentException ex    \n" +
				"         -1)                                       \n" +
				"  (catch :ValueException ex                        \n" +
				"         -2)                                       \n" +
				"  (catch :java.lang.RuntimeException ex            \n" +
				"         -3))                                        ";

		assertEquals(-2L, venice.eval(lisp));
	}
	
	@Test
	public void test_try_throw_catch_9() {
		final Venice venice = new Venice();

		final String lisp = 
				"(try                                         \n" +
				"  (throw 100)                                \n" +
				"  (catch :java.lang.Exception ex             \n" +
				"         (do                                 \n" +
				"            (+ 1 2)                          \n" +
				"            (+ 3 4)                          \n" +
				"            (str (ordered-map :a 1 :b 2))))  \n" +
				")                                            \n";

		assertEquals("{:a 1 :b 2}", venice.eval(lisp));
	}
	
	
	@Test
	public void test_try_catch_10() {
		final Venice venice = new Venice();

		final String lisp = 
				"(do                                             \n" +
				"  (+ 10 (try                                    \n" +
				"          (throw 100)                           \n" +
				"          (catch :java.lang.Exception ex 30)))  \n" +
				")                                               ";

		assertEquals(Long.valueOf(40L), venice.eval(lisp));
	}
	
	@Test
	public void test_try_catch_11() {
		final Venice venice = new Venice();

		final String lisp = 
				"(do                                                     \n" +
				"  (import :java.lang.RuntimeException)                  \n" +
				"  (import :java.io.IOException)                         \n" +
				"  (try                                                  \n" +
				"     (throw (. :RuntimeException :new \"message\"))     \n" +
				"     (catch :IOException ex (. ex :getMessage))         \n" +
				"     (catch :RuntimeException ex (. ex :getMessage))))  \n" +
				")                                                       ";

		assertEquals("message", venice.eval(lisp));
	}
	
	@Test
	public void test_try_catch_11b() {
		final Venice venice = new Venice();

		final String lisp = 
				"(do                                                     \n" +
				"  (import :java.lang.RuntimeException)                  \n" +
				"  (import :java.io.IOException)                         \n" +
				"  (try                                                  \n" +
				"     (throw (. :RuntimeException :new \"message\"))     \n" +
				"     (catch :IOException ex (:message ex))              \n" +
				"     (catch :RuntimeException ex (:message ex))))       \n" +
				")                                                       ";

		assertEquals("message", venice.eval(lisp));
	}
	
	@Test
	public void test_try_catch_12() {
		final Venice venice = new Venice();

		final String lisp = 
				"(do                                                           \n" +
				"  (import :java.lang.RuntimeException)                        \n" +
				"  (try                                                        \n" +
				"     (throw [1 2 3])                                          \n" +
				"     (catch :ValueException ex (pr-str (. ex :getValue)))     \n" +
				"     (catch :RuntimeException ex \"???\")))                   \n" +
				")                                                             \n";

		assertEquals("[1 2 3]", venice.eval(lisp));
	}

	@Test
	public void test_try_catch_12b() {
		final Venice venice = new Venice();

		final String lisp = 
				"(do                                                        \n" +
				"  (import :java.lang.RuntimeException)                     \n" +
				"  (try                                                     \n" +
				"     (throw [1 2 3])                                       \n" +
				"     (catch :ValueException ex (pr-str (:value ex)))       \n" +
				"     (catch :RuntimeException ex \"???\")))                \n" +
				")                                                          ";

		assertEquals("[1 2 3]", venice.eval(lisp));
	}

	
	// ---------------------------------------------------------------
	// try - finally
	// ---------------------------------------------------------------

	@Test
	public void test_try_finally_1() {
		final Venice venice = new Venice();

		final String lisp = 
				"(try                 \n" +
				"  100                \n" +
				"  (finally 200))       ";
		
		assertEquals(100L, venice.eval(lisp));
	}
	
	@Test
	public void test_try_finally_2() {
		final CapturingPrintStream ps = new CapturingPrintStream();
		
		final Venice venice = new Venice();

		final String lisp = 
				"(try                 \n" +
				"  (print 100)        \n" +
				"  (print 101)        \n" +
				"  (print \"-\")      \n" +
				"  100                \n" +
				"  (finally           \n" +
				"     (print 200)     \n" +
				"     (print 201)))     ";
		
		assertEquals(100L, venice.eval(lisp, Parameters.of("*out*", ps)));
		assertEquals("100101-200201", ps.getOutput());
	}

	
	@Test
	public void test_try_finally_Java() {
		Supplier<Long> fx = () -> {
									try {
										return 100L;
									}
									catch(Exception ex) {
										return 200L;
									}
									finally {
										@SuppressWarnings("unused")
										final long a = 300L;
										// a return statement is not allowed
									} };
		
		assertEquals(100L, fx.get());
	}
	
	// ---------------------------------------------------------------
	// try - throw - finally
	// ---------------------------------------------------------------

	@Test
	public void test_try_throw_finally_1() {
		final CapturingPrintStream ps = new CapturingPrintStream();
		
		final Venice venice = new Venice();

		try {
			final String lisp = 
					"(try                         \n" +
					"  (throw 100)                \n" +
					"  200                        \n" +
					"  (finally (print 300)))       ";
			
			venice.eval(lisp, Parameters.of("*out*", ps));
			
			fail("Expected JavaValueException");
		}
		catch(ValueException ex) {
			assertEquals(Long.valueOf(100), ex.getValue());
			assertEquals("300", ps.getOutput());
		}
	}
	
	@Test
	public void test_try_throw_finally_2() {
		final CapturingPrintStream ps = new CapturingPrintStream();
		
		final Venice venice = new Venice();

		try {
			final String lisp = 
					"(try                        \n" +
					"  (throw 100)               \n" +
					"  200                       \n" +
					"  (finally                  \n" +
					"     (print 300)            \n" +
					"     (print 400)))            ";
			
			venice.eval(lisp, Parameters.of("*out*", ps));
			
			fail("Expected JavaValueException");
		}
		catch(ValueException ex) {
			assertEquals(Long.valueOf(100), ex.getValue());
			assertEquals("300400", ps.getOutput());
		}
	}

	
	// ---------------------------------------------------------------
	// try - throw - catch - finally
	// ---------------------------------------------------------------

	@Test
	public void test_try_catch_finally_1() {
		final CapturingPrintStream ps = new CapturingPrintStream();
		
		final Venice venice = new Venice();

		final String lisp = 
				"(try                                   \n" +
				"  (throw 100)                          \n" +
				"  (catch :java.lang.Exception ex       \n" +
				"            (+ 1 2)                    \n" +
				"            (+ 3 4)                    \n" +
				"            -1)                        \n" +
				"  (finally                             \n" +
				"     (print \"...finally\")))          \n" +
				")                                      ";

		assertEquals(-1L, venice.eval(lisp, Parameters.of("*out*", ps)));
		assertEquals("...finally", ps.getOutput());
	}

	@Test
	public void test_try_catch_finally_2() {
		final CapturingPrintStream ps = new CapturingPrintStream();
		
		final Venice venice = new Venice();

		final String lisp = 
				"(try                                   \n" +
				"  (print 100)                          \n" +
				"  (print 101)                          \n" +
				"  (throw 900)                          \n" +
				"  (print 102)                          \n" +
				"  (catch :java.lang.Exception ex       \n" +
				"            (print 200)                \n" +
				"            (print 201)                \n" +
				"            -1)                        \n" +
				"  (finally                             \n" +
				"     (print 300)                       \n" +
				"     (print 301)))                     \n" +
				")                                      ";

		assertEquals(-1L, venice.eval(lisp, Parameters.of("*out*", ps)));
		assertEquals("100101200201300301", ps.getOutput());
	}

}
