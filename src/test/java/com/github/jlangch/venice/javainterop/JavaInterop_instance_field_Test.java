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
package com.github.jlangch.venice.javainterop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;


public class JavaInterop_instance_field_Test {

	@Test
	public void test1() {
		final Map<String,Object> params = Parameters.of("obj", new TestObject());
		
		final Venice venice = new Venice();

		assertEquals(100L, venice.eval("(. obj :LONG_VAL)", params));		
		assertEquals(100L, venice.eval("(. obj :LONG_VAL)", params));		
				
		assertEquals(Double.valueOf(3.14159265), (Double)venice.eval("(. obj :DOUBLE_VAL)", params), 0.0000001D);
		assertEquals(Double.valueOf(3.14159265), (Double)venice.eval("(. obj :DOUBLE_VAL)", params), 0.0000001D);

		assertEquals("alpha", venice.eval("(. obj :STRING_VAL)", params));		
		assertEquals("alpha", venice.eval("(. obj :STRING_VAL)", params));		

		assertEquals("BLUE", venice.eval("(. obj :ENUM_VAL)", params));		
		assertEquals("BLUE", venice.eval("(. obj :ENUM_VAL)", params));		
	}


	
	public static enum TestEnum {
		RED, GREEN, BLUE;
	}
	
	public static class TestObject {
		public final long LONG_VAL = 100L;
		public final double DOUBLE_VAL = 3.14159265D;
		public final String STRING_VAL = "alpha";
		public final TestEnum ENUM_VAL = TestEnum.BLUE;
	}
}
