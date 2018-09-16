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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
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
		assertEquals("one", venice.eval("(do (. jobj :setJavaEnum \"JavaEnum.one\") (. jobj :getJavaEnum))", symbols()));
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
	public void testStringByteArrStringAccessor() {
		final Venice venice = new Venice();

		assertEquals("null,null,null", venice.eval("(. jobj :_StringByteArrString nil nil nil)", symbols()));
		assertEquals("a,null,null", venice.eval("(. jobj :_StringByteArrString \"a\" nil nil)", symbols()));
		assertEquals("a,2,null", venice.eval("(. jobj :_StringByteArrString \"a\" (bytebuf '(1 2)) nil)", symbols()));
		assertEquals("a,2,c", venice.eval("(. jobj :_StringByteArrString \"a\"(bytebuf '(1 2)) \"c\")", symbols()));
	}

	@Test
	public void testByteArray() {
		final Venice venice = new Venice();

		assertEquals(null, venice.eval("(. jobj :getByteArray)", symbols()));
		assertArrayEquals(new byte[] {1,2,3}, ((ByteBuffer)venice.eval("(do (. jobj :setByteArray (bytebuf '(1 2 3))) (. jobj :getByteArray))", symbols())).array());
		assertArrayEquals(new byte[] {}, ((ByteBuffer)venice.eval("(do (. jobj :setByteArray (bytebuf '())) (. jobj :getByteArray))", symbols())).array());
		assertArrayEquals(new byte[] {1}, ((ByteBuffer)venice.eval("(do (. jobj :setByteArray 1) (. jobj :getByteArray))", symbols())).array());
	}

	@Test
	public void testIntArray() {
		final Venice venice = new Venice();

		assertEquals(null, venice.eval("(. jobj :getIntArray)", symbols()));
		assertEquals("[1 2 3]", venice.eval("(str (do (. jobj :setIntArray '(1 2 3)) (. jobj :getIntArray)))", symbols()));
		assertEquals("[]", venice.eval("(str (do (. jobj :setIntArray '()) (. jobj :getIntArray)))", symbols()));
		assertEquals("[1]", venice.eval("(str (do (. jobj :setIntArray 1) (. jobj :getIntArray)))", symbols()));
	}

	@Test
	public void testIntegerArray() {
		final Venice venice = new Venice();

		assertEquals(null, venice.eval("(. jobj :getIntegerArray)", symbols()));
		assertEquals("[1 2 3]", venice.eval("(str (do (. jobj :setIntegerArray '(1 2 3)) (. jobj :getIntegerArray)))", symbols()));
		assertEquals("[]", venice.eval("(str (do (. jobj :setIntegerArray '()) (. jobj :getIntegerArray)))", symbols()));
		assertEquals("[1]", venice.eval("(str (do (. jobj :setIntegerArray 1) (. jobj :getIntegerArray)))", symbols()));
	}

	@Test
	public void testStringArray() {
		final Venice venice = new Venice();

		assertEquals(null, venice.eval("(. jobj :getStringArray)", symbols()));
		assertEquals("[a b c]", venice.eval("(str (do (. jobj :setStringArray '(\"a\" \"b\" \"c\")) (. jobj :getStringArray)))", symbols()));
		assertEquals("[]", venice.eval("(str (do (. jobj :setStringArray '()) (. jobj :getStringArray)))", symbols()));
		assertEquals("[a]", venice.eval("(str (do (. jobj :setStringArray \"a\") (. jobj :getStringArray)))", symbols()));
		
		assertEquals("[a b c]", venice.eval("(str (do (. jobj :setStringArray '(\"a\" \"b\" \"c\")) (. jobj :getStringArray)))", symbols()));
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
		
		assertEquals(0L, venice.eval("(:red (. :java.awt.Color :BLUE))"));		
		assertEquals(0L, venice.eval("(:green (. :java.awt.Color :BLUE))"));		
		assertEquals(255L, venice.eval("(:blue (. :java.awt.Color :BLUE))"));		
	}
	
	@Test
	public void testLocalDate() {
		final Venice venice = new Venice();
		
		final LocalDate today = LocalDate.now();
				
		assertEquals(today, venice.eval("(. :java.time.LocalDate :now)"));
		assertEquals(today.plusDays(5), venice.eval("(. (. :java.time.LocalDate :now) :plusDays 5)"));

	}

	@Test
	public void testVarargs() {
		final Venice venice = new Venice();

		assertEquals("abc: 100", venice.eval("(. :java.lang.String :format \"%s: %d\" '(\"abc\" 100))", symbols()));
	}
	
	@Test
	public void testStaticVoid() {
		final Venice venice = new Venice();

		assertEquals("123", venice.eval("(. :com.github.jlangch.venice.support.JavaObject :staticVoid)"));
	}

	@Test
	@Ignore
	public void testJavaSandboxRecorder() {
		final Venice venice = new Venice(new SandboxRecorder());
		
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

	@Test
	public void test_proxy_FilenameFilter() {
		final Venice venice = new Venice();

		final String script =
				"(do                                           " +
				"  (def filter (fn [dir name] true))           " +
				"  (def dir (. :java.io.File :new \"/tmp\"))   " +
				"  (. dir :list                                " +
				"         (proxify                             " +
				"             :java.io.FilenameFilter          " +
				"             {:accept filter}))               " +
				") ";

		venice.eval(script);
	}

	@Test
	public void test_proxy_Predicate() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                              " +
				"    (import :com.github.jlangch.venice.support.Functions)        " +
				"    (import :java.util.function.Predicate)                       " +
			    "                                                                 " +
			    "    (def pred-fn (fn[x] (== x \"abc\")))                         " +
			    "                                                                 " +
			    "    (def pred-fn-proxy (proxify :Predicate { :test pred-fn }))   " +
			    "                                                                 " +
				"    (let [functions (. :Functions :new)]                         " +
			    "         (. functions :evalPredicate                             " +
			    "                      pred-fn-proxy                              " +
			    "                      \"abc\" ))                                 " +
				") ";

		assertEquals(true, (Boolean)venice.eval(script));
	}
	
	@Test
	@Ignore
	public void test_proxy_SwingInvoker() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                                              " +
				"   (import :java.lang.Runnable)                                                  " +
				"   (import :javax.swing.JPanel)                                                  " +
				"   (import :javax.swing.JFrame)                                                  " +
				"   (import :javax.swing.JLabel)                                                  " +
				"   (import :javax.swing.SwingUtilities)                                          " +
				"                                                                                 " +
				"   (def swing-open-window                                                        " +
				"        (fn [title]                                                              " +
				"            (let [frame (. :JFrame :new title)                                   " +
				"                  label (. :JLabel :new \"Hello World\")                         " +
				"                  closeOP (. :JFrame :EXIT_ON_CLOSE)]                            " +
				"                 (. frame :setDefaultCloseOperation closeOP)                     " +
				"                 (. frame :add label)                                            " +
				"                 (. frame :setSize 200 200)                                      " +
				"                 (. frame :setVisible true))))                                   " +
				"                                                                                 " +
				"   (def swing-view                                                               " +
				"        (fn [title]                                                              " +
				"            (. :SwingUtilities :invokeLater                                      " +
				"               (proxify :Runnable { :run (fn [] (swing-open-window title))}))))  " +
				"                                                                                 " +
				"   (swing-view \"test\")                                                         " +
				"   (sleep 20000)                                                                 " +
				") ";

		venice.eval(script);
	}
			
	@Test
	public void test_proxy_Streams_Filter() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                              " +
				"    (import :java.util.ArrayList)                                " +
				"    (import :java.util.function.Predicate)                       " +
				"    (import :java.util.stream.Collectors)                        " +
			    "                                                                 " +
			    "    (def pred-fn (fn[x] (> x 2)))                                " +
			    "                                                                 " +
				"    (let [ data (doto (. :ArrayList :new)                        " +
			    "                      (. :add 1)                                 " +
			    "                      (. :add 2)                                 " +
			    "                      (. :add 3)                                 " +
			    "                      (. :add 4)) ]                              " +
			    "                                                                 " +
				"         (-> (. data :stream)                                    " +
			    "             (. :filter (proxify :Predicate { :test pred-fn }))  " +
			    "             (. :collect (. :Collectors :toList))))              " +
				") ";

		assertEquals("[3, 4]", venice.eval(script).toString());
	}

	
	private Map<String, Object> symbols() {
		return Parameters.of("jobj", new JavaObject());
	}

}
