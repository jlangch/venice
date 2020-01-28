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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class JavaInterop_constructor_Test {

	@Test
	public void testDefaultConstructor() {
		final String clazz = TestObject.class.getName();
		
		final Venice venice = new Venice();

		TestObject obj;
		
		// default constructor
		obj = (TestObject)venice.eval("(. :" + clazz + " :new)");
		obj = (TestObject)venice.eval("(. :" + clazz + " :new)");
		assertNotNull(obj);
	}

	@Test
	public void testOneArgConstructor() {
		final String clazz = TestObject.class.getName();
		
		final Venice venice = new Venice();

		TestObject obj;
		
		// Long arg constructor
		obj = (TestObject)venice.eval("(. :" + clazz + " :new 100)");
		obj = (TestObject)venice.eval("(. :" + clazz + " :new 100)");
		assertEquals(100L, obj._long);
		assertNull(obj._string);
		assertNull(obj._double);
		
		// String arg constructor
		obj = (TestObject)venice.eval("(. :" + clazz + " :new \"abc\")");
		obj = (TestObject)venice.eval("(. :" + clazz + " :new \"abc\")");
		assertNull(obj._long);
		assertEquals("abc", obj._string);
		assertNull(obj._double);
		
		// Double arg constructor
		obj = (TestObject)venice.eval("(. :" + clazz + " :new 100.12)");
		obj = (TestObject)venice.eval("(. :" + clazz + " :new 100.12)");
		assertNull(obj._long);
		assertNull(obj._string);
		assertEquals(100.12D, obj._double, 0.01);
	}

	@Test
	public void testOneArgCoercedConstructor() {
		final String clazz = TestObject.class.getName();
		
		final Venice venice = new Venice();

		TestObject obj;
		
		// Coerce Integer to Long constructor
		obj = (TestObject)venice.eval("(. :" + clazz + " :new 100I)");
		obj = (TestObject)venice.eval("(. :" + clazz + " :new 100I)");
		assertEquals(100L, obj._long);
		assertNull(obj._string);
		assertNull(obj._double);
		
		// Coerce Keyword to String constructor
		obj = (TestObject)venice.eval("(. :" + clazz + " :new :abc)");
		obj = (TestObject)venice.eval("(. :" + clazz + " :new :abc)");
		assertNull(obj._long);
		assertEquals("abc", obj._string);
		assertNull(obj._double);
		
		// Coerce Symbol to String constructor
		obj = (TestObject)venice.eval("(. :" + clazz + " :new 'abc)");
		obj = (TestObject)venice.eval("(. :" + clazz + " :new 'abc)");
		assertNull(obj._long);
		assertEquals("abc", obj._string);
		assertNull(obj._double);
	}

	@Test
	public void testTwoArgConstructor() {
		final String clazz = TestObject.class.getName();
		
		final Venice venice = new Venice();

		TestObject obj;
		
		// Long,String arg constructor
		obj = (TestObject)venice.eval("(. :" + clazz + " :new 100 \"abc\")");
		obj = (TestObject)venice.eval("(. :" + clazz + " :new 100 \"abc\")");
		assertEquals(100L, obj._long);
		assertEquals("abc", obj._string);
		assertNull(obj._double);
	
		// Long,Double arg constructor
		obj = (TestObject)venice.eval("(. :" + clazz + " :new 100 100.12)");
		obj = (TestObject)venice.eval("(. :" + clazz + " :new 100 100.12)");
		assertEquals(100L, obj._long);
		assertNull(obj._string);
		assertEquals(100.12D, obj._double, 0.01);
	}

	@Test
	public void testTwoArgCoercedConstructor() {
		final String clazz = TestObject.class.getName();
		
		final Venice venice = new Venice();

		TestObject obj;
		
		// Long,String arg constructor
		obj = (TestObject)venice.eval("(. :" + clazz + " :new 100I :abc)");
		obj = (TestObject)venice.eval("(. :" + clazz + " :new 100I :abc)");
		assertEquals(100L, obj._long);
		assertEquals("abc", obj._string);
		assertNull(obj._double);
	
		// Long,Double arg constructor
		obj = (TestObject)venice.eval("(. :" + clazz + " :new 100I 100.12)");
		obj = (TestObject)venice.eval("(. :" + clazz + " :new 100I 100.12)");
		assertEquals(100L, obj._long);
		assertNull(obj._string);
		assertEquals(100.12D, obj._double, 0.01);
	}

	@Test
	public void testThreeArgConstructor() {
		final String clazz = TestObject.class.getName();
		
		final Venice venice = new Venice();

		TestObject obj;
		
		// Long,String,Double arg constructor
		obj = (TestObject)venice.eval("(. :" + clazz + " :new 100 \"abc\" 100.12)");
		obj = (TestObject)venice.eval("(. :" + clazz + " :new 100 \"abc\" 100.12)");
		assertEquals(100L, obj._long);
		assertEquals("abc", obj._string);
		assertEquals(100.12D, obj._double, 0.01);
	}

	@Test
	public void testThreeArgCoercedConstructor() {
		final String clazz = TestObject.class.getName();
		
		final Venice venice = new Venice();

		TestObject obj;
		
		// Long,String,Double arg constructor
		obj = (TestObject)venice.eval("(. :" + clazz + " :new 100I :abc 100.12)");
		obj = (TestObject)venice.eval("(. :" + clazz + " :new 100I :abc 100.12)");
		assertEquals(100L, obj._long);
		assertEquals("abc", obj._string);
		assertEquals(100.12D, obj._double, 0.01);
	}
	
	@Test
	public void testJavaObjArgConstructor() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                                      " +
				"   (import :com.github.jlangch.venice.support.User )                     " +
				"   (import :java.time.LocalDate)                                         " +
				"                                                                         " + 
				"   (def john (. :com.github.jlangch.venice.support.User :new             " +
				"                \"john\" 24 (. :java.time.LocalDate :of 2018 7 21)))     " +
				"   (str john)                                                            " + 
				")";
		
		assertEquals("john, 24, 2018-07-21", venice.eval(script));
	}
	
	
	public static class TestObject {
		public TestObject() {
			this._long = null;
			this._string = null;
			this._double = null;
		}
		
		public TestObject(final Long val) {
			this._long = val;
			this._string = null;
			this._double = null;
		}
		public TestObject(final String val) {
			this._long = null;
			this._string = val;
			this._double = null;
		}
		public TestObject(final Double val) {
			this._long = null;
			this._string = null;
			this._double = val;
		}
		
		public TestObject(final Long val1, final String val2) {
			this._long = val1;
			this._string = val2;
			this._double = null;
		}
		public TestObject(final Long val1, final Double val2) {
			this._long = val1;
			this._string = null;
			this._double = val2;
		}
		
		public TestObject(final Long val1, final String val2, final Double val3) {
			this._long = val1;
			this._string = val2;
			this._double = val3;
		}
		
		
		public final Long _long;
		public final String _string;
		public final Double _double;
	}
}
