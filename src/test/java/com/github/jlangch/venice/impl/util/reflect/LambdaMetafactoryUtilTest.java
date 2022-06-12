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
package com.github.jlangch.venice.impl.util.reflect;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.util.reflect.LambdaMetafactoryUtil.Consumer1;
import com.github.jlangch.venice.impl.util.reflect.LambdaMetafactoryUtil.Consumer2;
import com.github.jlangch.venice.impl.util.reflect.LambdaMetafactoryUtil.Consumer3;
import com.github.jlangch.venice.impl.util.reflect.LambdaMetafactoryUtil.Function0;
import com.github.jlangch.venice.impl.util.reflect.LambdaMetafactoryUtil.Function1;
import com.github.jlangch.venice.impl.util.reflect.LambdaMetafactoryUtil.Function2;
import com.github.jlangch.venice.impl.util.reflect.LambdaMetafactoryUtil.Function3;


public class LambdaMetafactoryUtilTest {

    @Test
    public void test_instanceMethod_0_args() throws Exception {
        final TestObject to = new TestObject();

        final Method m = TestObject.class.getDeclaredMethod("fn_string_void");

        final Function1<Object,Object> fn = LambdaMetafactoryUtil.instanceMethod_0_args(m);

        assertEquals("-", fn.apply(to));
    }

    @Test
    public void test_instanceMethod_1_args() throws Exception {
        final TestObject to = new TestObject();

        final Method m = TestObject.class.getDeclaredMethod("fn_string_string", String.class);

        final Function2<Object,Object,Object> fn = LambdaMetafactoryUtil.instanceMethod_1_args(m);
        assertEquals("arg1", fn.apply(to, "arg1"));
        assertEquals("null", fn.apply(to, null));
    }

    @Test
    public void test_instanceMethod_2_args() throws Exception {
        final TestObject to = new TestObject();

        final Method m = TestObject.class.getDeclaredMethod("fn_string_string_string", String.class, String.class);

        final Function3<Object,Object,Object,Object> fn = LambdaMetafactoryUtil.instanceMethod_2_args(m);

        assertEquals("arg1-arg2", fn.apply(to, "arg1", "arg2"));
        assertEquals("null-arg2", fn.apply(to, null, "arg2"));
        assertEquals("null-null", fn.apply(to, null, null));
    }


    @Test
    public void test_instanceMethodVoid_0_args() throws Exception {
        final TestObject to = new TestObject();

        final Method m = TestObject.class.getDeclaredMethod("fn_void_void");

        final Consumer1<Object> fn = LambdaMetafactoryUtil.instanceMethodVoid_0_args(m);
        fn.accept(to);

        assertEquals("void", TestObject.last());
    }

    @Test
    public void test_instanceMethodVoid_1_args() throws Exception {
        final TestObject to = new TestObject();

        final Method m = TestObject.class.getDeclaredMethod("fn_void_string", String.class);

        final Consumer2<Object,Object> fn = LambdaMetafactoryUtil.instanceMethodVoid_1_args(m);

        fn.accept(to, "arg1");
        assertEquals("arg1", TestObject.last());

        fn.accept(to, null);
        assertEquals("null", TestObject.last());
    }

    @Test
    public void test_instanceMethodVoid_2_args() throws Exception {
        final TestObject to = new TestObject();

        final Method m = TestObject.class.getDeclaredMethod("fn_void_string_string", String.class, String.class);

        final Consumer3<Object,Object,Object> fn = LambdaMetafactoryUtil.instanceMethodVoid_2_args(m);

        fn.accept(to, "arg1", "arg2");
        assertEquals("arg1-arg2", TestObject.last());

        fn.accept(to, null, "arg2");
        assertEquals("null-arg2", TestObject.last());

        fn.accept(to, null, null);
        assertEquals("null-null", TestObject.last());
    }


    @Test
    public void test_instanceMethod_1_args_long() throws Exception {
        final TestObject to = new TestObject();

        final Method m = TestObject.class.getDeclaredMethod("fn_long_long", Long.class);

        final Function2<Object,Object,Object> fn = LambdaMetafactoryUtil.instanceMethod_1_args(m);
        assertEquals(100L, fn.apply(to, Long.valueOf(100L)));
        assertEquals(100L, fn.apply(to, 100L));
        assertEquals(null, fn.apply(to, null));
    }

    @Test
    @Disabled  // not working on Java 11 (primitive type)
    public void test_instanceMethod_1_args_long_primitive() throws Exception {
        final TestObject to = new TestObject();

        final Method m = TestObject.class.getDeclaredMethod("fn_long_long_primitive", long.class);

        final Function2<Object,Object,Object> fn = LambdaMetafactoryUtil.instanceMethod_1_args(m);
        assertEquals(100L, fn.apply(to, Long.valueOf(100L)));
        assertEquals(100L, fn.apply(to, 100L));

        assertEquals(100L, fn.apply(to, Integer.valueOf(100)));
        assertEquals(100L, fn.apply(to, 100));
        assertEquals(100L, fn.apply(to, Float.valueOf(100.0F)));
        assertEquals(100L, fn.apply(to, 100.0));
        assertEquals(100L, fn.apply(to, Double.valueOf(100.0D)));
        assertEquals(100L, fn.apply(to, 100.0D));
    }

    @Test
    public void test_constructor_0_args_string() throws Exception {
        final Constructor<?> c = String.class.getConstructor();

        final Function0<Object> fn = LambdaMetafactoryUtil.constructor_0_args(c);
        assertEquals("", fn.apply());

        assertEquals("", LambdaMetafactoryUtil.invoke_constructor(new Object[0], fn));
    }

    @Test
    @Disabled  // not working on Java 11 (primitive type)
    public void test_constructor_1_args_long() throws Exception {
        final Constructor<?> c = Long.class.getConstructor(long.class);

        final Function1<Object,Object> fn = LambdaMetafactoryUtil.constructor_1_args(c);
        assertEquals(100L, fn.apply(Long.valueOf(100L)));
        assertEquals(100L, fn.apply(100L));

        assertEquals(100L, LambdaMetafactoryUtil.invoke_constructor(new Object[] {Long.valueOf(100L)}, fn));
    }

    @Test
    public void test_staticMethod_1_args() throws Exception {
        final Method m = TestObject.class.getDeclaredMethod("fn_static_string_string", String.class);

        final Function1<Object,Object> fn = LambdaMetafactoryUtil.staticMethod_1_args(m);
        assertEquals("arg1", fn.apply("arg1"));
        assertEquals(null, fn.apply(null));
    }

    @Test
    public void test_staticMethodVoid_1_args() throws Exception {
        final Method m = TestObject.class.getDeclaredMethod("fn_static_void_string", String.class);

        final Consumer1<Object> fn = LambdaMetafactoryUtil.staticMethodVoid_1_args(m);

        fn.accept("arg1");
        assertEquals("arg1", TestObject.last());


        fn.accept(null);
        assertEquals(null, TestObject.last());
    }


//    @Test
//    public void test_Graphics2D_LambdaMetafactory_1() throws Throwable {
//        BufferedImage img = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
//
//        final Method m_getGraphics = BufferedImage.class.getDeclaredMethod("getGraphics");
//        final Method m_fillOval = Graphics2D.class.getMethod("fillOval", int.class, int.class, int.class, int.class);
//
//        final Function1<Object,Object> fn_getGraphics = LambdaMetafactoryUtil.instanceMethod_0_args(m_getGraphics);
//        final Consumer5<Object,Object,Object,Object,Object> fn_fillOval = LambdaMetafactoryUtil.instanceMethodVoid_4_args(m_fillOval);
//
//        Object g2d = fn_getGraphics.apply(img);
//        fn_fillOval.accept(g2d, 10, 20, 5, 5);
//    }
//
//    @Test
//    public void test_Graphics2D_LambdaMetafactory_2() throws Throwable {
//        BufferedImage img = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
//
//        final Method m_getGraphics = BufferedImage.class.getDeclaredMethod("getGraphics");
//        final Function1<Object,Object> fn_getGraphics = LambdaMetafactoryUtil.instanceMethod_0_args(m_getGraphics);
//        Object g2d = fn_getGraphics.apply(img);
//
//        final Method m_fillOval = g2d.getClass().getMethod("fillOval", int.class, int.class, int.class, int.class);
//        final Consumer5<Object,Object,Object,Object,Object> fn_fillOval = LambdaMetafactoryUtil.instanceMethodVoid_4_args(m_fillOval);
//        fn_fillOval.accept(g2d, 10, 20, 5, 5);
//    }
//
//    @Test
//    public void test_Graphics2D_Reflection_1() throws Throwable {
//        BufferedImage img = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
//
//        final Method m_getGraphics = BufferedImage.class.getDeclaredMethod("getGraphics");
//        final Method m_fillOval = Graphics2D.class.getMethod("fillOval", int.class, int.class, int.class, int.class);
//
//        Object g2d = m_getGraphics.invoke(img);
//        m_fillOval.invoke(g2d, 10, 20, 5, 5);
//    }
//
//    @Test
//    public void test_Graphics2D_Reflection_2() throws Throwable {
//        BufferedImage img = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
//
//        final Method m_getGraphics = BufferedImage.class.getDeclaredMethod("getGraphics");
//        Object g2d = m_getGraphics.invoke(img);  // returns a 'sun.java2d.SunGraphics2D'
//
//        final Method m_fillOval = g2d.getClass().getMethod("fillOval", int.class, int.class, int.class, int.class);
//        m_fillOval.invoke(g2d, 10, 20, 5, 5);
//    }


    @SuppressWarnings("unused")
    private static class TestObject {

        public TestObject() {
        }


        // void ------------------------------------------------------------

        public void fn_void_void() {
            last = "void";
        }


        // strings ------------------------------------------------------------

        public void fn_void_string(final String s1) {
            last = "" + s1;
        }

        public void fn_void_string_string(final String s1, final String s2) {
            last = "" + s1 + "-" + s2;
        }

        public String fn_string_void() {
            last = "-";
            return (String)last;
        }

        public String fn_string_string(final String s1) {
            last = "" + s1;
            return (String)last;
        }

        public String fn_string_string_string(final String s1, final String s2) {
            last = "" + s1 + "-" + s2;
            return (String)last;
        }


        // Long ------------------------------------------------------------

        public Long fn_long_long(final Long s1) {
            last = s1;
            return (Long)last;
        }

        public long fn_long_long_primitive(final long s1) {
            last = s1;
            return (Long)last;
        }



        // static ------------------------------------------------------------

        public static String fn_static_string_string(final String s1) {
            last = s1;
            return (String)last;
        }

        public static void fn_static_void_string(final String s1) {
            last = s1;
        }

        public static Object last() {
            return last;
        }


        private static Object last = "init";
    }


}
