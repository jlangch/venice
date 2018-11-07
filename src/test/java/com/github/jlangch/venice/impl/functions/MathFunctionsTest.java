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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;


public class MathFunctionsTest {

	@Test
	public void test_abs() {
		final Venice venice = new Venice();

		// Long
		assertEquals(Long.valueOf(3), venice.eval("(abs 3)"));
		assertEquals(Long.valueOf(3), venice.eval("(abs -3)"));

		// Double
		assertEquals(Double.valueOf(3.0D), venice.eval("(abs 3.0)"));
		assertEquals(Double.valueOf(3.0D), venice.eval("(abs -3.0)"));

		// Decimal
		assertEquals(new BigDecimal("3.333"), venice.eval("(abs 3.333M))"));
		assertEquals(new BigDecimal("3.333"), venice.eval("(abs -3.333M))"));
		assertEquals(new BigDecimal("3.0"), venice.eval("(abs 3.0M)"));
		assertEquals(new BigDecimal("3.0"), venice.eval("(abs -3.0M)"));
	}

	@Test
	public void test_add() {
		final Venice venice = new Venice();

		assertEquals(Long.valueOf(0), venice.eval("(+)"));

		// Long
		assertEquals(Long.valueOf(3), venice.eval("(+ 3)"));
		assertEquals(Long.valueOf(3), venice.eval("(+ 1 2)"));
		assertEquals(Long.valueOf(6), venice.eval("(+ 1 2 3)"));
		assertEquals(Double.valueOf(3.0D), venice.eval("(+ 1 2.0)"));
		assertEquals(new BigDecimal("3.0"), venice.eval("(+ 1 2.0M)"));

		// Double
		assertEquals(Double.valueOf(3.0D), venice.eval("(+ 3.0)"));
		assertEquals(Double.valueOf(3.0D), venice.eval("(+ 1.0 2.0)"));
		assertEquals(Double.valueOf(3.0D), venice.eval("(+ 1.0 2)"));
		assertEquals(new BigDecimal("3.0"), venice.eval("(+ 1.0 2.0M)"));

		// Decimal
		assertEquals(new BigDecimal("3.0"), venice.eval("(+ 3.0M)"));
		assertEquals(new BigDecimal("3.0"), venice.eval("(+ 1.0M 2.0M)"));
		assertEquals(new BigDecimal("3.0"), venice.eval("(+ 1.0M 2)"));
		assertEquals(new BigDecimal("3.0"), venice.eval("(+ 1.0M 2.0)"));
	}

	@Test
	public void test_dec() {
		final Venice venice = new Venice();

		assertEquals(Long.valueOf(1L), venice.eval("(dec 2)"));

		assertEquals(Double.valueOf(3.2D), (Double)venice.eval("(dec 4.2)"), 0.00001);

		assertEquals(new BigDecimal("4.01234"), venice.eval("(dec 5.01234M)"));
	}

	@Test
	public void test_dec_add() {
		final Venice venice = new Venice();

		assertEquals(new BigDecimal("6.323"), venice.eval("(dec/add 3.12345M 3.2M 3 :HALF_UP)"));
	}
	
	@Test
	public void test_dec_sub() {
		final Venice venice = new Venice();

		assertEquals(new BigDecimal("6.420"), venice.eval("(dec/sub 8.54321M 2.123M 3 :HALF_UP)"));
	}
	
	@Test
	public void test_dec_mul() {
		final Venice venice = new Venice();

		assertEquals(new BigDecimal("8.664"), venice.eval("(dec/mul 3.2345M 2.6787M 3 :HALF_UP)"));
	}
	
	@Test
	public void test_dec_div() {
		final Venice venice = new Venice();

		assertEquals(new BigDecimal("2.675"), venice.eval("(dec/div 6.567876M 2.45556M 3 :HALF_UP)"));
	}
	
	@Test
	public void test_dec_scale() {
		final Venice venice = new Venice();

		assertEquals(new BigDecimal("3"), venice.eval("(dec/scale 3.0M 0 :HALF_UP)"));
		assertEquals(new BigDecimal("3.0"), venice.eval("(dec/scale 3.0M 1 :HALF_UP)"));
		assertEquals(new BigDecimal("3.000"), venice.eval("(dec/scale 3.0M 3 :HALF_UP)"));
		assertEquals(new BigDecimal("3.457"), venice.eval("(dec/scale 3.456789M 3 :HALF_UP)"));
	}

	@Test
	public void test_div() {
		final Venice venice = new Venice();
		
		// Long
		assertEquals(Long.valueOf(0), venice.eval("(/ 12)"));
		assertEquals(Long.valueOf(6), venice.eval("(/ 12 2)"));
		assertEquals(Long.valueOf(2), venice.eval("(/ 12 2 3)"));
		assertEquals(Double.valueOf(6.0D), venice.eval("(/ 12 2.0)"));
		assertEquals(new BigDecimal("6.0000000000000000"), venice.eval("(/ 12 2.0M)"));

		// Double
		assertEquals(Double.valueOf(0.16666D), (Double)venice.eval("(/ 6.0)"), 0.001D);
		assertEquals(Double.valueOf(6.0D), venice.eval("(/ 12.0 2.0)"));
		assertEquals(Double.valueOf(2.0D), venice.eval("(/ 12.0 2.0 3.0)"));
		assertEquals(Double.valueOf(6.0D), venice.eval("(/ 12.0 2)"));
		assertEquals(new BigDecimal("6.0000000000000000"), venice.eval("(/ 12.0 2.0M)"));

		// Decimal
		assertEquals(new BigDecimal("0.1666666666666667"), venice.eval("(/ 6.0M)"));
		assertEquals(new BigDecimal("6.0000000000000000"), venice.eval("(/ 12.0M 2.0M)"));
		assertEquals(new BigDecimal("6.0000000000000000"), venice.eval("(/ 12.0M 2)"));
		assertEquals(new BigDecimal("6.0000000000000000"), venice.eval("(/ 12.0M 2.0)"));
	}

	@Test
	public void test_div_by_zero() {
		final Venice venice = new Venice();

		final String s = 
				"(do                        \n" +
				"   (defn f1 [x] (f2 x))    \n" +
				"   (defn f2 [x] (f3 x))    \n" +
				"   (defn f3 [x] (f4 x))    \n" +
				"   (defn f4 [x] (f5 x))    \n" +
				"   (defn f5 [x] (/ 1 x))   \n" +
				"   (f1 0))                    ";
	
		assertThrows(VncException.class, () -> {
			venice.eval("test", s);
		});
	}

	@Test
	public void test_even_Q() {
		final Venice venice = new Venice();

		assertTrue((Boolean)venice.eval("(even? 2)"));	
		assertTrue((Boolean)venice.eval("(even? 0)"));	
		assertFalse((Boolean)venice.eval("(even? 1)"));	
		
		try {
			venice.eval("(even? 1.0)");
			fail("Expected exception");
		}
		catch(RuntimeException ex) {
			assertTrue(true);
		}
	}

	@Test
	public void test_inc() {
		final Venice venice = new Venice();

		assertEquals(Long.valueOf(1L), venice.eval("(inc 0)"));

		assertEquals(Double.valueOf(3.2D), (Double)venice.eval("(inc 2.2)"), 0.00001);

		assertEquals(new BigDecimal("4.01234"), venice.eval("(inc 3.01234M)"));
	}
	
	@Test
	public void test_max() {
		final Venice venice = new Venice();

		// Long
		assertEquals(Long.valueOf(3), venice.eval("(max 3)"));
		assertEquals(Long.valueOf(3), venice.eval("(max 1 3)"));
		assertEquals(Long.valueOf(3), venice.eval("(max 1 2 3)"));
		assertEquals(Long.valueOf(3), venice.eval("(max 1 2 3 1.0)"));
		assertEquals(Long.valueOf(3), venice.eval("(max 1 2 3 1.0M)"));
		assertEquals(Double.valueOf(3.0D), venice.eval("(max 1 3.0)"));
		assertEquals(new BigDecimal("3.0"), venice.eval("(max 1 2.0 3.0M)"));

		// Double
		assertEquals(Double.valueOf(3.0D), venice.eval("(max 3.0)"));
		assertEquals(Double.valueOf(3.0D), venice.eval("(max 1.0 3.0)"));
		assertEquals(Double.valueOf(3.0D), venice.eval("(max 1.0 2.0 3.0)"));
		assertEquals(Double.valueOf(3.0D), venice.eval("(max 1.0 2.0 3.0 2)"));
		assertEquals(Double.valueOf(3.0D), venice.eval("(max 1.0 2.0 3.0 1.0M)"));
		assertEquals(Long.valueOf(3), venice.eval("(max 1.0 3)"));
		assertEquals(new BigDecimal("3.0"), venice.eval("(max 1.0 3.0M)"));

		// Decimal
		assertEquals(new BigDecimal("3.0"), venice.eval("(max 3.0M)"));
		assertEquals(new BigDecimal("3.0"), venice.eval("(max 1.0M 3.0M)"));
		assertEquals(new BigDecimal("3.0"), venice.eval("(max 1.0M 2.0M 3.0M)"));
		assertEquals(new BigDecimal("3.0"), venice.eval("(max 1.0M 2.0M 3.0M 2)"));
		assertEquals(new BigDecimal("3.0"), venice.eval("(max 1.0M 2.0M 3.0M 2.0)"));
		assertEquals(Long.valueOf(3), venice.eval("(max 1.0M 3)"));
		assertEquals(Double.valueOf(3.0D), venice.eval("(max 1.0M 3.0)"));
	}

	@Test
	public void test_min() {
		final Venice venice = new Venice();

		// Long
		assertEquals(Long.valueOf(3), venice.eval("(min 3)"));
		assertEquals(Long.valueOf(1), venice.eval("(min 1 3)"));
		assertEquals(Long.valueOf(1), venice.eval("(min 1 2 3)"));
		assertEquals(Long.valueOf(1), venice.eval("(min 1 2 3 2.0)"));
		assertEquals(Long.valueOf(1), venice.eval("(min 1 2 3 2.0M)"));
		assertEquals(Double.valueOf(3.0D), venice.eval("(min 4 3.0)"));
		assertEquals(new BigDecimal("3.0"), venice.eval("(min 4 5.0 3.0M)"));

		// Double
		assertEquals(Double.valueOf(3.0D), venice.eval("(min 3.0)"));
		assertEquals(Double.valueOf(1.0D), venice.eval("(min 1.0 3.0)"));
		assertEquals(Double.valueOf(1.0D), venice.eval("(min 1.0 2.0 3.0)"));
		assertEquals(Double.valueOf(1.0D), venice.eval("(min 1.0 2.0 3.0 2)"));
		assertEquals(Double.valueOf(1.0D), venice.eval("(min 1.0 2.0 3.0 2.0M)"));
		assertEquals(Long.valueOf(3), venice.eval("(min 4.0 3)"));
		assertEquals(new BigDecimal("3.0"), venice.eval("(min 4.0 3.0M)"));

		// Decimal
		assertEquals(new BigDecimal("3.0"), venice.eval("(min 3.0M)"));
		assertEquals(new BigDecimal("1.0"), venice.eval("(min 1.0M 3.0M)"));
		assertEquals(new BigDecimal("1.0"), venice.eval("(min 1.0M 2.0M 3.0M)"));
		assertEquals(new BigDecimal("1.0"), venice.eval("(min 1.0M 2.0M 3.0M 2)"));
		assertEquals(new BigDecimal("1.0"), venice.eval("(min 1.0M 2.0M 3.0M 2.0)"));
		assertEquals(Long.valueOf(3), venice.eval("(min 4.0M 3)"));
		assertEquals(Double.valueOf(3.0D), venice.eval("(min 4.0M 3.0)"));
	}

	@Test
	public void test_mod() {
		final Venice venice = new Venice();

		assertEquals(Long.valueOf(0), venice.eval("(mod 0 6)"));
		assertEquals(Long.valueOf(4), venice.eval("(mod 4 6)"));
		assertEquals(Long.valueOf(1), venice.eval("(mod 13 6)"));
	}

	@Test
	public void test_mul() {
		final Venice venice = new Venice();

		assertEquals(Long.valueOf(1), venice.eval("(*)"));

		// Long
		assertEquals(Long.valueOf(3), venice.eval("(* 3)"));
		assertEquals(Long.valueOf(2), venice.eval("(* 1 2)"));
		assertEquals(Long.valueOf(6), venice.eval("(* 1 2 3)"));
		assertEquals(Double.valueOf(2.0D), venice.eval("(* 1 2.0)"));
		assertEquals(new BigDecimal("2.0"), venice.eval("(* 1 2.0M)"));

		// Double
		assertEquals(Double.valueOf(3.0D), venice.eval("(* 3.0)"));
		assertEquals(Double.valueOf(2.0D), venice.eval("(* 1.0 2.0)"));
		assertEquals(Double.valueOf(2.0D), venice.eval("(* 1.0 2)"));
		assertEquals(new BigDecimal("2.0"), venice.eval("(* 1.0 2.0M)"));

		// Decimal
		assertEquals(new BigDecimal("3.0"), venice.eval("(* 3.0M)"));
		assertEquals(new BigDecimal("2.00"), venice.eval("(* 1.0M 2.0M)"));
		assertEquals(new BigDecimal("2.0"), venice.eval("(* 1.0M 2)"));
		assertEquals(new BigDecimal("2.0"), venice.eval("(* 1.0M 2.0)"));
	}

	@Test
	public void test_neg() {
		final Venice venice = new Venice();

		// Long
		assertTrue((Boolean)venice.eval("(neg? -3)"));
		assertFalse((Boolean)venice.eval("(neg? 0)"));
		assertFalse((Boolean)venice.eval("(neg? 3)"));
	
		// Double
		assertTrue((Boolean)venice.eval("(neg? -3.0)"));
		assertFalse((Boolean)venice.eval("(neg? 0.0)"));
		assertFalse((Boolean)venice.eval("(neg? 3.0)"));

		// Decimal
		assertTrue((Boolean)venice.eval("(neg? -3.0M)"));
		assertFalse((Boolean)venice.eval("(neg? 0.0M)"));
		assertFalse((Boolean)venice.eval("(neg? 3.0M)"));
	}
	
	@Test
	public void test_odd_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(odd? 2)"));	
		assertFalse((Boolean)venice.eval("(odd? 0)"));	
		assertTrue((Boolean)venice.eval("(odd? 1)"));	
		
		try {
			venice.eval("(odd? 1.0)");
			fail("Expected exception");
		}
		catch(RuntimeException ex) {
			assertTrue(true);
		}
	}
	
	@Test
	public void test_rand_long() {
		final Venice venice = new Venice();

		for(int ii=0; ii<1000; ii++) {
			final long x = ((Long)venice.eval("(rand-long )")).longValue();			
			assertTrue(x >= 0);
		}

		for(int ii=0; ii<1000; ii++) {
			final long x = ((Long)venice.eval("(rand-long 10)")).longValue();			
			assertTrue(x >= 0 && x < 10);
		}
	}

	@Test
	public void test_rand_double() {
		final Venice venice = new Venice();

		for(int ii=0; ii<1000; ii++) {
			final double x = ((Double)venice.eval("(rand-double )")).doubleValue();			
			assertTrue(x >= 0.0 && x <= 1.0);
		}

		for(int ii=0; ii<1000; ii++) {
			final double x = ((Double)venice.eval("(rand-double 2.0)")).doubleValue();			
			assertTrue(x >= 0.0 && x <= 2.0);
		}
	}
	
	@Test
	public void test_range() {
		final Venice venice = new Venice();

		// Long
		assertEquals("(1 2 3 4 5 6 7 8 9)", venice.eval("(str (range 1 10))"));
		assertEquals("(1 2 3 4 5 6 7 8 9)", venice.eval("(str (range 1 10 1))"));
		assertEquals("(-5 -4 -3 -2 -1 0 1 2 3 4)", venice.eval("(str (range -5 5))"));
		assertEquals("(-100 -90 -80 -70 -60 -50 -40 -30 -20 -10 0 10 20 30 40 50 60 70 80 90)", venice.eval("(str (range -100 100 10))"));
		assertEquals("(0 2)", venice.eval("(str (range 0 4 2))"));
		assertEquals("(0 2 4)", venice.eval("(str (range 0 5 2))"));
		assertEquals("(0 2 4)", venice.eval("(str (range 0 6 2))"));
		assertEquals("(0 2 4 6)", venice.eval("(str (range 0 7 2))"));
		assertEquals("(100 90 80 70 60 50 40 30 20 10)", venice.eval("(str (range 100 0 -10))"));
		assertEquals("(10 9 8 7 6 5 4 3 2 1 0 -1 -2 -3 -4 -5 -6 -7 -8 -9)", venice.eval("(str (range 10 -10 -1))"));
		
		// Double
		assertEquals("(1.0 2.0 3.0 4.0 5.0)", venice.eval("(str (range 1.0 6.0))"));
		assertEquals("(1.0 2.0 3.0 4.0 5.0)", venice.eval("(str (range 1.0 6.0 1.0))"));
		assertEquals("(1.0 1.5 2.0 2.5 3.0)", venice.eval("(str (range 1.0 3.1 0.5))"));
		
		// Decimal
		assertEquals("(1.0M 2.0M 3.0M 4.0M 5.0M)", venice.eval("(str (range 1.0M 6.0M))"));
		assertEquals("(1.0M 2.0M 3.0M 4.0M 5.0M)", venice.eval("(str (range 1.0M 6.0M 1.0M))"));
		assertEquals("(1.0M 1.5M 2.0M 2.5M 3.0M)", venice.eval("(str (range 1.0M 3.1M 0.5M))"));
		
		// Mixed
		assertEquals("(1 2 3 4 5)", venice.eval("(str (range 1 6.0))"));
		assertEquals("(1.0 2.0 3.0 4.0 5.0)", venice.eval("(str (range 1.0 6))"));
		assertEquals("(1 1.5 2.0 2.5 3.0)", venice.eval("(str (range 1 3.1 0.5))"));
		assertEquals("(1.0 1.5 2.0 2.5 3.0 3.5)", venice.eval("(str (range 1.0 4 0.5))"));
	}

	@Test
	public void test_zero_Q() {
		final Venice venice = new Venice();

		// Long
		assertFalse((Boolean)venice.eval("(zero? -3)"));
		assertTrue((Boolean)venice.eval("(zero? 0)"));
		assertFalse((Boolean)venice.eval("(zero? 3)"));
	
		// Double
		assertFalse((Boolean)venice.eval("(zero? -3.0)"));
		assertTrue((Boolean)venice.eval("(zero? 0.0)"));
		assertFalse((Boolean)venice.eval("(zero? 3.0)"));

		// Decimal
		assertFalse((Boolean)venice.eval("(zero? -3.0M)"));
		assertTrue((Boolean)venice.eval("(zero? 0.0M)"));
		assertFalse((Boolean)venice.eval("(zero? 3.0M)"));
	}	

}
