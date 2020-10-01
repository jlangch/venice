/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2020 Venice
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
package com.github.jlangch.venice.javainterop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class JavaInterop_static_field_Test {

	@Test
	public void test1() {
		final String clazz = TestObject.class.getName();
		
		final Venice venice = new Venice();

		assertEquals(100L, venice.eval("(. :" + clazz + " :LONG_VAL)"));		
		assertEquals(100L, venice.eval("(. :" + clazz + " :LONG_VAL)"));		
				
		assertEquals(Double.valueOf(3.14159265), (Double)venice.eval("(. :" + clazz + " :DOUBLE_VAL)"), 0.0000001D);
		assertEquals(Double.valueOf(3.14159265), (Double)venice.eval("(. :" + clazz + " :DOUBLE_VAL)"), 0.0000001D);

		assertEquals("alpha", venice.eval("(. :" + clazz + " :STRING_VAL)"));		
		assertEquals("alpha", venice.eval("(. :" + clazz + " :STRING_VAL)"));		

		assertEquals("BLUE", venice.eval("(. :" + clazz + " :ENUM_VAL)"));		
		assertEquals("BLUE", venice.eval("(. :" + clazz + " :ENUM_VAL)"));		
	}

	@Test
	public void test2() {
		final Venice venice = new Venice();
				
		assertEquals(Double.valueOf(3.14159265), (Double)venice.eval("(. :java.lang.Math :PI)"), 0.0000001D);
		
		assertEquals(0, venice.eval("(:red (. :java.awt.Color :BLUE))"));		
		assertEquals(0, venice.eval("(:green (. :java.awt.Color :BLUE))"));		
		assertEquals(255, venice.eval("(:blue (. :java.awt.Color :BLUE))"));		
		
		assertEquals(true, venice.eval("(:empty (. :java.awt.Rectangle :new))"));		
	}

	
	public static enum TestEnum {
		RED, GREEN, BLUE;
	}
	
	public static class TestObject {
		public static final long LONG_VAL = 100L;
		public static final double DOUBLE_VAL = 3.14159265D;
		public static final String STRING_VAL = "alpha";
		public static final TestEnum ENUM_VAL = TestEnum.BLUE;
	}
}
