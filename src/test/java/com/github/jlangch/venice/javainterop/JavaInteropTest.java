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
package com.github.jlangch.venice.javainterop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.javainterop.JavaInteropUtil;
import com.github.jlangch.venice.impl.types.Types;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncJavaObject;
import com.github.jlangch.venice.support.AuditEvent;
import com.github.jlangch.venice.support.AuditEventType;
import com.github.jlangch.venice.support.JavaObject;


public class JavaInteropTest {

	@Test
	public void convertTest() {
		final AuditEvent event = new AuditEvent(
										"su",
										2000L,
										AuditEventType.ALERT,
										"superuser",
										"webapp.started",
										"text");
		
		final VncVal val = JavaInteropUtil.convertToVncVal(event);
		assertTrue(Types.isVncJavaObject(val));
		
		final VncJavaObject javaObj = (VncJavaObject)val;
		assertEquals(7, javaObj.size());
		assertEquals("su", ((VncString)javaObj.get(new VncKeyword("principal"))).getValue());
		assertEquals(2000L, ((VncLong)javaObj.get(new VncKeyword("elapsedTimeMillis"))).getValue().longValue());
		assertEquals("ALERT", ((VncString)javaObj.get(new VncKeyword("eventType"))).getValue());
		assertEquals("superuser", ((VncString)javaObj.get(new VncKeyword("eventKey"))).getValue());
		assertEquals("webapp.started", ((VncString)javaObj.get(new VncKeyword("eventName"))).getValue());
		assertEquals("text", ((VncString)javaObj.get(new VncKeyword("eventMessage"))).getValue());
		assertEquals(AuditEvent.class.getName(), ((VncString)javaObj.get(new VncKeyword("class"))).getValue());
		
		final Object obj = JavaInteropUtil.convertToJavaObject(val);
		assertTrue(obj instanceof AuditEvent);
	}
	
	@Test
	public void testVoidAccessor() {
		final Venice venice = new Venice();

		assertEquals(null, venice.eval("(. jobj :_void)", symbols()));
	}

	@Test
	public void testStringAccessor() {
		final Venice venice = new Venice();

		assertEquals(null, venice.eval("(. jobj :getString)", symbols()));
		assertEquals("abc", venice.eval("(do (. jobj :setString \"abc\") (. jobj :getString))", symbols()));
	}
	
	@Test
	public void testBooleanAccessor() {
		final Venice venice = new Venice();

		assertEquals(null, venice.eval("(. jobj :getBoolean)", symbols()));
		assertEquals(true, venice.eval("(do (. jobj :setBoolean true) (. jobj :getBoolean))", symbols()));

		assertEquals(false, venice.eval("(. jobj :isPrimitiveBoolean)", symbols()));
		assertEquals(true, venice.eval("(do (. jobj :setPrimitiveBoolean true) (. jobj :isPrimitiveBoolean))", symbols()));
	}
	
	@Test
	public void testIntegerAccessor() {
		final Venice venice = new Venice();

		assertEquals(null, venice.eval("(. jobj :getInteger)", symbols()));
		assertEquals(100L, venice.eval("(do (. jobj :setInteger 100) (. jobj :getInteger))", symbols()));

		assertEquals(0L, venice.eval("(. jobj :getPrimitiveInt)", symbols()));
		assertEquals(100L, venice.eval("(do (. jobj :setPrimitiveInt 100) (. jobj :getPrimitiveInt))", symbols()));
	}
	
	@Test
	public void testLongAccessor() {
		final Venice venice = new Venice();

		assertEquals(null, venice.eval("(. jobj :getLong)", symbols()));
		assertEquals(100L, venice.eval("(do (. jobj :setLong 100) (. jobj :getLong))", symbols()));

		assertEquals(0L, venice.eval("(. jobj :getPrimitiveLong)", symbols()));
		assertEquals(100L, venice.eval("(do (. jobj :setPrimitiveLong 100) (. jobj :getPrimitiveLong))", symbols()));
	}
	
	@Test
	public void testFloatAccessor() {
		final Venice venice = new Venice();

		assertEquals(null, venice.eval("(. jobj :getFloat)", symbols()));
		assertEquals(100.0D, venice.eval("(do (. jobj :setFloat 100.0) (. jobj :getFloat))", symbols()));

		assertEquals(0.0D, venice.eval("(. jobj :getPrimitiveFloat)", symbols()));
		assertEquals(100.0D, venice.eval("(do (. jobj :setPrimitiveFloat 100.0) (. jobj :getPrimitiveFloat))", symbols()));
	}
	
	@Test
	public void testDoubleAccessor() {
		final Venice venice = new Venice();

		assertEquals(null, venice.eval("(. jobj :getDouble)", symbols()));
		assertEquals(100.0D, venice.eval("(do (. jobj :setDouble 100.0) (. jobj :getDouble))", symbols()));

		assertEquals(0.0D, venice.eval("(. jobj :getPrimitiveDouble)", symbols()));
		assertEquals(100.0D, venice.eval("(do (. jobj :setPrimitiveDouble 100.0) (. jobj :getPrimitiveDouble))", symbols()));
	}
	
	@Test
	public void testBigDecimalAccessor() {
		final Venice venice = new Venice();

		assertEquals(null, venice.eval("(. jobj :getBigDecimal)", symbols()));
		assertEquals(new BigDecimal("100.0"), venice.eval("(do (. jobj :setBigDecimal (decimal \"100.0\")) (. jobj :getBigDecimal))", symbols()));
	}
	
	@Test
	public void testEnumAccessor() {
		final Venice venice = new Venice();

		assertEquals(null, venice.eval("(. jobj :getJavaEnum)", symbols()));
		assertEquals("one", venice.eval("(do (. jobj :setJavaEnum \"one\") (. jobj :getJavaEnum))", symbols()));
	}

	
	@Test
	public void testScopedEnumAccessor() {
		final Venice venice = new Venice();

		assertEquals(null, venice.eval("(. jobj :getJavaEnum)", symbols()));
		assertEquals("one", venice.eval("(do (. jobj :setJavaEnum \"com.github.jlangch.venice.support.JavaObject.JavaEnum.one\") (. jobj :getJavaEnum))", symbols()));
	}

	@Test
	public void testStringStringStringAccessor() {
		final Venice venice = new Venice();

		assertEquals("null,null,null", venice.eval("(. jobj :_StringStringString nil nil nil)", symbols()));
		assertEquals("a,null,null", venice.eval("(. jobj :_StringStringString \"a\" nil nil)", symbols()));
		assertEquals("a,b,null", venice.eval("(. jobj :_StringStringString \"a\" \"b\" nil)", symbols()));
		assertEquals("a,b,c", venice.eval("(. jobj :_StringStringString \"a\" \"b\" \"c\")", symbols()));
	}
	
	@Test
	public void testConstructorAccessor() {
		final Venice venice = new Venice();

		final Object obj1 = venice.eval("(. :com.github.jlangch.venice.support.JavaObject :new)", symbols());
		assertTrue(obj1 instanceof JavaObject);

		final Object obj2 = venice.eval("(. :com.github.jlangch.venice.support.JavaObject :new 100)", symbols());
		assertTrue(obj2 instanceof JavaObject);
	}

	@Test
	public void testOverloadedMethod() {
		final Venice venice = new Venice();

		assertEquals(null, venice.eval("(. jobj :getOverloaded)", symbols()));
		assertEquals(100L, venice.eval("(do (. jobj :setOverloaded 100) (. jobj :getOverloaded))", symbols()));
		assertEquals("abc", venice.eval("(do (. jobj :setOverloaded \"abc\") (. jobj :getOverloaded))", symbols()));
	}

	@Test
	public void testStaticMethod() {
		final Venice venice = new Venice();
				
		assertEquals(Long.valueOf(20L), venice.eval("(. :java.lang.Math :min 20 30)"));
	}
	
	@Test
	public void testStaticField() {
		final Venice venice = new Venice();
				
		assertEquals(Double.valueOf(3.14159265), (Double)venice.eval("(. :java.lang.Math :PI)"), 0.0000001D);
	}
	
	@Test
	public void testLocalDate() {
		final Venice venice = new Venice();
		
		final LocalDate today = LocalDate.now();
				
		assertEquals(today, venice.eval("(. :java.time.LocalDate :now)"));
		assertEquals(today.plusDays(5), venice.eval("(. (. :java.time.LocalDate :now) :plusDays 5)"));

	}
	
	@Test
	@Ignore
	public void testJavaSandboxRecorder() {
		final Venice venice = new Venice(new JavaSandboxRecorder());
		
		venice.eval("(. :java.lang.Math :min 20 30)");
		venice.eval("(. (. :java.time.ZonedDateTime :now) :plusDays 5)");
				
		final String lisp = 
				"(do                                                  " +
				"   (def fmt (. :java.time.format.DateTimeFormatter   " +
				"               :ofPattern                            " +
				"               \"YYYY-MM-dd'T'HH:mm:ss.SSS\"))       " +
				"                                                     " +
				"   (let [now (. :java.time.ZonedDateTime :now)]      " +
				"        (. fmt :parse (. fmt :format now))           " +
				"        (. fmt :format now))                         " +
				")                                                    ";

		System.out.println(venice.eval(lisp));
	}
	
	@Test
	public void test_doto() {
		final Venice venice = new Venice();

		//new JavaSandboxRecorder().register();
		
		// let [x (. :java.util.LinkedHashMap :new)] (. x :put :a 1), (. x :put :b 2) x)
		
		final String map =
				"(do                                  " +
				"  (doto (. :java.util.HashMap :new)  " +
				"	     (. :put :a 1)                " +
				"	     (. :put :b 2))               " +
				") ";

		assertEquals("{a=1, b=2}", venice.eval(map).toString());
		
		
		final String list =
				"(do                                    " +
				"  (doto (. :java.util.ArrayList :new)  " +
				"	     (. :add 1)                     " +
				"	     (. :add 2))                    " +
				") ";

		assertEquals("[1, 2]", venice.eval(list).toString());
	}

	@Test
	public void test_convert_to_VncHashMap() {
		final Venice venice = new Venice();

		final String map1 =
				"(do                                           " +
				"  (hash-map                                   " +
				"     (doto (. :java.util.LinkedHashMap :new)  " +
				"	        (. :put :a 1)                      " +
				"	        (. :put :b 2)))                    " +
				") ";

		assertEquals("{a=1, b=2}", venice.eval(map1).toString());

		final String map2 =
				"(class                                        " +
				"  (hash-map                                   " +
				"     (doto (. :java.util.LinkedHashMap :new)  " +
				"	        (. :put :a 1)                      " +
				"	        (. :put :b 2)))                    " +
				") ";

		assertEquals("venice.HashMap", venice.eval(map2).toString());
	}

	@Test
	public void test_convert_to_VncLinkedMap() {
		final Venice venice = new Venice();

		final String map1 =
				"(do                                           " +
				"  (ordered-map                                " +
				"     (doto (. :java.util.LinkedHashMap :new)  " +
				"	        (. :put :a 1)                      " +
				"	        (. :put :b 2)))                    " +
				") ";

		assertEquals("{a=1, b=2}", venice.eval(map1).toString());

		final String map2 =
				"(class                                        " +
				"  (ordered-map                                " +
				"     (doto (. :java.util.LinkedHashMap :new)  " +
				"	        (. :put :a 1)                      " +
				"	        (. :put :b 2)))                    " +
				") ";

		assertEquals("venice.OrderedMap", venice.eval(map2).toString());
	}

	@Test
	public void test_convert_to_VncSortedMap() {
		final Venice venice = new Venice();

		final String map1 =
				"(do                                           " +
				"  (sorted-map                                 " +
				"     (doto (. :java.util.LinkedHashMap :new)  " +
				"	        (. :put :a 1)                      " +
				"	        (. :put :b 2)))                    " +
				") ";

		assertEquals("{a=1, b=2}", venice.eval(map1).toString());

		final String map2 =
				"(class                                        " +
				"  (sorted-map                                 " +
				"     (doto (. :java.util.LinkedHashMap :new)  " +
				"	        (. :put :a 1)                      " +
				"	        (. :put :b 2)))                    " +
				") ";

		assertEquals("venice.SortedMap", venice.eval(map2).toString());
	}

	
	private Map<String, Object> symbols() {
		return Parameters.of("jobj", new JavaObject());
	}

}
