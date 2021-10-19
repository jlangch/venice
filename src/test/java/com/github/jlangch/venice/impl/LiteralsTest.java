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

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class LiteralsTest {

	@Test
	public void test_nil() {
		final Venice venice = new Venice();

		assertEquals(null, venice.eval("nil"));
	}

	@Test
	public void test_string() {
		final Venice venice = new Venice();

		assertEquals("", venice.eval("\"\""));
		assertEquals("a", venice.eval("\"a\""));
		assertEquals("abc", venice.eval("\"abc\""));

		assertEquals("", venice.eval("\"\""));
		assertEquals("a", venice.eval("\"\"\"a\"\"\""));
		assertEquals("abc", venice.eval("\"\"\"abc\"\"\""));
	}

	@Test
	public void test_boolean() {
		final Venice venice = new Venice();

		assertEquals(false, (Boolean)venice.eval("false"));
		assertEquals(true, (Boolean)venice.eval("true"));
	}

	@Test
	public void test_integer() {
		final Venice venice = new Venice();

		assertEquals(0, (Integer)venice.eval("0I"));
		assertEquals(-10, (Integer)venice.eval("-10I"));
		assertEquals(10, (Integer)venice.eval("10I"));
		
		assertEquals(10000, (Integer)venice.eval("10_000I"));
		assertEquals(10000222, (Integer)venice.eval("10_000_222I"));
		assertEquals(-10000, (Integer)venice.eval("-10_000I"));
		assertEquals(-10000222, (Integer)venice.eval("-10_000_222I"));
		
		assertEquals(65535, (Integer)venice.eval("0xFFFFI"));
	}

	@Test
	public void test_long() {
		final Venice venice = new Venice();

		assertEquals(0L, (Long)venice.eval("0"));
		assertEquals(-10L, (Long)venice.eval("-10"));
		assertEquals(10L, (Long)venice.eval("10"));
		
		assertEquals(10000L, (Long)venice.eval("10_000"));
		assertEquals(10000222L, (Long)venice.eval("10_000_222"));
		assertEquals(-10000L, (Long)venice.eval("-10_000"));
		assertEquals(-10000222L, (Long)venice.eval("-10_000_222"));
		
		assertEquals(65535L, (Long)venice.eval("0xFFFF"));
	}
	
	@Test
	public void test_double() {
		final Venice venice = new Venice();

		assertEquals(0D, (Double)venice.eval("0.0"));
		assertEquals(-10D, (Double)venice.eval("-10.0"));
		assertEquals(10D, (Double)venice.eval("10.0"));


		assertEquals(10D, (Double)venice.eval("10.0e0"));
		assertEquals(10D, (Double)venice.eval("10.0E0"));
		assertEquals(-10D, (Double)venice.eval("-10.0e0"));
		assertEquals(-10D, (Double)venice.eval("-10.0E0"));

		assertEquals(10D, (Double)venice.eval("10.0e00"));
		assertEquals(10D, (Double)venice.eval("10.0E00"));
		assertEquals(-10D, (Double)venice.eval("-10.0e00"));
		assertEquals(-10D, (Double)venice.eval("-10.0E00"));

		assertEquals(10D, (Double)venice.eval("10.0e+0"));
		assertEquals(10D, (Double)venice.eval("10.0E+0"));
		assertEquals(-10D, (Double)venice.eval("-10.0e+0"));
		assertEquals(-10D, (Double)venice.eval("-10.0E+0"));

		assertEquals(10D, (Double)venice.eval("10.0e+00"));
		assertEquals(10D, (Double)venice.eval("10.0E+00"));
		assertEquals(-10D, (Double)venice.eval("-10.0e+00"));
		assertEquals(-10D, (Double)venice.eval("-10.0E+00"));

		assertEquals(10D, (Double)venice.eval("10.0e-0"));
		assertEquals(10D, (Double)venice.eval("10.0E-0"));
		assertEquals(-10D, (Double)venice.eval("-10.0e-0"));
		assertEquals(-10D, (Double)venice.eval("-10.0E-0"));

		assertEquals(10D, (Double)venice.eval("10.0e-00"));
		assertEquals(10D, (Double)venice.eval("10.0E-00"));
		assertEquals(-10D, (Double)venice.eval("-10.0e-00"));
		assertEquals(-10D, (Double)venice.eval("-10.0E-00"));

		assertEquals(1000000D, (Double)venice.eval("10.0e5"));
		assertEquals(1000000D, (Double)venice.eval("10.0E5"));
		assertEquals(-1000000D, (Double)venice.eval("-10.0e5"));
		assertEquals(-1000000D, (Double)venice.eval("-10.0E5"));
		
		assertEquals(1000000D, (Double)venice.eval("10.0e05"));
		assertEquals(1000000D, (Double)venice.eval("10.0E05"));
		assertEquals(-1000000D, (Double)venice.eval("-10.0e05"));
		assertEquals(-1000000D, (Double)venice.eval("-10.0E05"));

		assertEquals(1000000D, (Double)venice.eval("10.0e+5"));
		assertEquals(1000000D, (Double)venice.eval("10.0E+5"));
		assertEquals(-1000000D, (Double)venice.eval("-10.0e+5"));
		assertEquals(-1000000D, (Double)venice.eval("-10.0E+5"));
		
		assertEquals(1000000D, (Double)venice.eval("10.0e+05"));
		assertEquals(1000000D, (Double)venice.eval("10.0E+05"));
		assertEquals(-1000000D, (Double)venice.eval("-10.0e+05"));
		assertEquals(-1000000D, (Double)venice.eval("-10.0E+05"));
		
		assertEquals(1.0E-4D, (Double)venice.eval("10.0e-5"));
		assertEquals(1.0E-4D, (Double)venice.eval("10.0E-5"));
		assertEquals(-1.0E-4D, (Double)venice.eval("-10.0e-5"));
		assertEquals(-1.0E-4D, (Double)venice.eval("-10.0E-5"));
		
		assertEquals(1.0E-4D, (Double)venice.eval("10.0e-05"));
		assertEquals(1.0E-4D, (Double)venice.eval("10.0E-05"));
		assertEquals(-1.0E-4D, (Double)venice.eval("-10.0e-05"));
		assertEquals(-1.0E-4D, (Double)venice.eval("-10.0E-05"));
	}
	
	@Test
	public void test_bigdecimal() {
		final Venice venice = new Venice();

		assertEquals(new BigDecimal("0"), (BigDecimal)venice.eval("0M"));
		assertEquals(new BigDecimal("-10"), (BigDecimal)venice.eval("-10M"));
		assertEquals(new BigDecimal("10"), (BigDecimal)venice.eval("10M"));
		
		assertEquals(new BigDecimal("10.123"), (BigDecimal)venice.eval("10.123M"));
		assertEquals(new BigDecimal("-10.123"), (BigDecimal)venice.eval("-10.123M"));
		
		assertEquals(new BigDecimal("1.0123E+6"), (BigDecimal)venice.eval("10.123e5M"));
		assertEquals(new BigDecimal("-1.0123E+6"), (BigDecimal)venice.eval("-10.123e5M"));
		
		assertEquals(new BigDecimal("1.0123E-4"), (BigDecimal)venice.eval("10.123e-5M"));
		assertEquals(new BigDecimal("-1.0123E-4"), (BigDecimal)venice.eval("-10.123e-5M"));
	}

	@Test
	public void test_bigint() {
		final Venice venice = new Venice();

		assertEquals(new BigInteger("0"), (BigInteger)venice.eval("0N"));
		assertEquals(new BigInteger("-10"), (BigInteger)venice.eval("-10N"));
		assertEquals(new BigInteger("10"), (BigInteger)venice.eval("10N"));
		
		assertEquals(new BigInteger("10000"), (BigInteger)venice.eval("10_000N"));
		assertEquals(new BigInteger("10000222"), (BigInteger)venice.eval("10_000_222N"));	
		assertEquals(new BigInteger("-10000"), (BigInteger)venice.eval("-10_000N"));
		assertEquals(new BigInteger("-10000222"), (BigInteger)venice.eval("-10_000_222N"));
		
		assertEquals(new BigInteger("65535"), (BigInteger)venice.eval("0xFFFFN"));
	}
}
