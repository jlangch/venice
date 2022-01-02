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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.JavaMethodInvocationException;
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
	public void testOneArgConstructorBoxing() {
		final String clazz = TestObject_Boxing.class.getName();
		
		final Venice venice = new Venice();

		TestObject_Boxing obj;
		
		// Long arg constructor
		obj = (TestObject_Boxing)venice.eval("(. :" + clazz + " :new 100)");
		obj = (TestObject_Boxing)venice.eval("(. :" + clazz + " :new 100)");
		assertEquals(100L, obj._long);
		assertNull(obj._double);
		
		// Double arg constructor
		obj = (TestObject_Boxing)venice.eval("(. :" + clazz + " :new 100.12)");
		obj = (TestObject_Boxing)venice.eval("(. :" + clazz + " :new 100.12)");
		assertNull(obj._long);
		assertEquals(100.12D, obj._double, 0.01);
	}

	@Test
	public void testOneArgCoercedConstructorBoxing() {
		final String clazz = TestObject_Boxing.class.getName();
		
		final Venice venice = new Venice();

		TestObject_Boxing obj;
		
		// Coerce Integer to Long constructor
		obj = (TestObject_Boxing)venice.eval("(. :" + clazz + " :new 100I)");
		obj = (TestObject_Boxing)venice.eval("(. :" + clazz + " :new 100I)");
		assertEquals(100L, obj._long);
		assertNull(obj._double);
	}

	@Test
	public void testTwoArgConstructorBoxing() {
		final String clazz = TestObject_Boxing.class.getName();
		
		final Venice venice = new Venice();

		TestObject_Boxing obj;
		
		// Long,Double arg constructor
		obj = (TestObject_Boxing)venice.eval("(. :" + clazz + " :new 100 100.12)");
		obj = (TestObject_Boxing)venice.eval("(. :" + clazz + " :new 100 100.12)");
		assertEquals(100L, obj._long);
		assertEquals(100.12D, obj._double, 0.01);
		
		// Long,Double arg constructor
		obj = (TestObject_Boxing)venice.eval("(. :" + clazz + " :new 100I 100.12)");
		obj = (TestObject_Boxing)venice.eval("(. :" + clazz + " :new 100I 100.12)");
		assertEquals(100L, obj._long);
		assertEquals(100.12D, obj._double, 0.01);
	}

	@Test
	public void testTwoArgCoercedConstructorBoxing() {
		final String clazz = TestObject_Boxing.class.getName();
		
		final Venice venice = new Venice();

		TestObject_Boxing obj;
		
		// Integer,Long arg constructor
		obj = (TestObject_Boxing)venice.eval("(. :" + clazz + " :new 100I 100)");
		obj = (TestObject_Boxing)venice.eval("(. :" + clazz + " :new 100I 100)");
		assertEquals(100L, obj._long);
		assertEquals(100.0D, obj._double);
	
		// Integer,Double arg constructor
		obj = (TestObject_Boxing)venice.eval("(. :" + clazz + " :new 100I 100.12)");
		obj = (TestObject_Boxing)venice.eval("(. :" + clazz + " :new 100I 100.12)");
		assertEquals(100L, obj._long);
		assertEquals(100.12D, obj._double, 0.01);
	}

	@Test
	public void testOneArgListConstructor() {
		final String clazz = TestObject_List.class.getName();
		
		final Venice venice = new Venice();

		TestObject_List obj;
		
		obj = (TestObject_List)venice.eval("(. :" + clazz + " :new [1 2 3])");
		obj = (TestObject_List)venice.eval("(. :" + clazz + " :new [1 2 3])");
		assertEquals(3L, obj._len);
		assertEquals(6L, obj._sum);

		obj = (TestObject_List)venice.eval("(. :" + clazz + " :new '(1 2 3))");
		obj = (TestObject_List)venice.eval("(. :" + clazz + " :new '(1 2 3))");
		assertEquals(3L, obj._len);
		assertEquals(6L, obj._sum);

		obj = (TestObject_List)venice.eval("(. :" + clazz + " :new [1 2 3])");
		assertEquals(3L, obj._len);
		assertEquals(6L, obj._sum);
	}

	@Test
	public void testOneArgListConstructor_Failed() {
		final String clazz = TestObject_List.class.getName();
		
		final Venice venice = new Venice();
		
		// Java exception: cannot cast Integer to Long
		assertThrows(JavaMethodInvocationException.class, () -> 
			venice.eval("(. :" + clazz + " :new [1I 2 3])"));
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
	
	@Test
	public void testVarArg1Constructor_1() {
		final String clazz = TestObject_VarArg_1.class.getName();
		
		final Venice venice = new Venice();

		TestObject_VarArg_1 obj;
		
		obj = (TestObject_VarArg_1)venice.eval("(. :" + clazz + " :new '())");
		obj = (TestObject_VarArg_1)venice.eval("(. :" + clazz + " :new '())");
		assertEquals(0L, obj._long);
		assertEquals("", obj._string);
		assertNull(obj._double);
	}
	
	@Test
	public void testVarArg1Constructor_2() {
		final String clazz = TestObject_VarArg_1.class.getName();
		
		final Venice venice = new Venice();

		TestObject_VarArg_1 obj;
		
		obj = (TestObject_VarArg_1)venice.eval("(. :" + clazz + " :new '(\"c\"))");
		obj = (TestObject_VarArg_1)venice.eval("(. :" + clazz + " :new '(\"c\"))");
		assertEquals(1L, obj._long);
		assertEquals("c", obj._string);
		assertNull(obj._double);
	}
	
	@Test
	public void testVarArg1Constructor_3() {
		final String clazz = TestObject_VarArg_1.class.getName();
		
		final Venice venice = new Venice();

		TestObject_VarArg_1 obj;
		
		obj = (TestObject_VarArg_1)venice.eval("(. :" + clazz + " :new '(\"c\" \"d\"))");
		obj = (TestObject_VarArg_1)venice.eval("(. :" + clazz + " :new '(\"c\" \"d\"))");
		assertEquals(2L, obj._long);
		assertEquals("c:d", obj._string);
		assertNull(obj._double);
	}
	
	@Test
	public void testVarArg1Constructor_4() {
		final String clazz = TestObject_VarArg_1.class.getName();
		
		final Venice venice = new Venice();

		TestObject_VarArg_1 obj;
		
		obj = (TestObject_VarArg_1)venice.eval("(. :" + clazz + " :new '(:c \"d\"))");
		obj = (TestObject_VarArg_1)venice.eval("(. :" + clazz + " :new '(:c \"d\"))");
		assertEquals(2L, obj._long);
		assertEquals("c:d", obj._string);
		assertNull(obj._double);
	}
	
	@Test
	public void testVarArg2Constructor_1() {
		final String clazz = TestObject_VarArg_2.class.getName();
		
		final Venice venice = new Venice();

		TestObject_VarArg_2 obj;
		
		obj = (TestObject_VarArg_2)venice.eval("(. :" + clazz + " :new \"a\" \"b\" '())");
		obj = (TestObject_VarArg_2)venice.eval("(. :" + clazz + " :new \"a\" \"b\" '())");
		assertEquals(0L, obj._long);
		assertEquals("a:b:", obj._string);
		assertNull(obj._double);
	}
	
	@Test
	public void testVarArg2Constructor_2() {
		final String clazz = TestObject_VarArg_2.class.getName();
		
		final Venice venice = new Venice();

		TestObject_VarArg_2 obj;
		
		obj = (TestObject_VarArg_2)venice.eval("(. :" + clazz + " :new \"a\" \"b\" '(\"c\"))");
		obj = (TestObject_VarArg_2)venice.eval("(. :" + clazz + " :new \"a\" \"b\" '(\"c\"))");
		assertEquals(1L, obj._long);
		assertEquals("a:b:c", obj._string);
		assertNull(obj._double);
	}
	
	@Test
	public void testVarArg2Constructor_3() {
		final String clazz = TestObject_VarArg_2.class.getName();
		
		final Venice venice = new Venice();

		TestObject_VarArg_2 obj;
		
		obj = (TestObject_VarArg_2)venice.eval("(. :" + clazz + " :new \"a\" \"b\" '(\"c\" \"d\"))");
		obj = (TestObject_VarArg_2)venice.eval("(. :" + clazz + " :new \"a\" \"b\" '(\"c\" \"d\"))");
		assertEquals(2L, obj._long);
		assertEquals("a:b:c:d", obj._string);
		assertNull(obj._double);
	}
	
	@Test
	public void testVarArg2Constructor_4() {
		final String clazz = TestObject_VarArg_2.class.getName();
		
		final Venice venice = new Venice();

		TestObject_VarArg_2 obj;
		
		obj = (TestObject_VarArg_2)venice.eval("(. :" + clazz + " :new :a \"b\" '(:c \"d\"))");
		obj = (TestObject_VarArg_2)venice.eval("(. :" + clazz + " :new :a \"b\" '(:c \"d\"))");
		assertEquals(2L, obj._long);
		assertEquals("a:b:c:d", obj._string);
		assertNull(obj._double);
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
		
		public TestObject(final String val1, final String val2, final String... vals) {
			this._long = Long.valueOf(vals.length);
			this._string = val1 + ":" + val2 + ":" + String.join(":", vals);
			this._double = null;
		}
		
		public final Long _long;
		public final String _string;
		public final Double _double;
	}

	public static class TestObject_Boxing {
		public TestObject_Boxing() {
			this._long = null;
			this._double = null;
		}
		
		public TestObject_Boxing(final long val) {
			this._long = val;
			this._double = null;
		}
		public TestObject_Boxing(final double val) {
			this._long = null;
			this._double = val;
		}
		public TestObject_Boxing(final long val1, final double val2) {
			this._long = val1;
			this._double = val2;
		}
		
		public final Long _long;
		public final Double _double;
	}

	public static class TestObject_List {
		public TestObject_List(final List<Long> vals) {
			this._sum = vals.stream().mapToLong(v -> v.longValue()).sum();
			this._len = vals.size();
		}
		
		public final Long _sum;
		public final long _len;
	}

	public static class TestObject_VarArg_1 {
		public TestObject_VarArg_1(final String... vals) {
			this._long = Long.valueOf(vals.length);
			this._string = String.join(":", vals);
			this._double = null;
		}
		
		public final Long _long;
		public final String _string;
		public final Double _double;
	}	
	
	public static class TestObject_VarArg_2 {
		public TestObject_VarArg_2(final String val1, final String val2, final String... vals) {
			this._long = Long.valueOf(vals.length);
			this._string = val1 + ":" + val2 + ":" + String.join(":", vals);
			this._double = null;
		}
		
		public final Long _long;
		public final String _string;
		public final Double _double;
	}	
}
