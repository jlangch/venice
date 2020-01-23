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
package com.github.jlangch.venice.impl.util.reflect;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.util.reflect.LambdaMetafactoryUtil.Consumer2;
import com.github.jlangch.venice.impl.util.reflect.LambdaMetafactoryUtil.Function2;
import com.github.jlangch.venice.impl.util.reflect.LambdaMetafactoryUtil.Function3;
import com.github.jlangch.venice.support.JavaObject;


public class LambdaMetafactoryUtilTest {

	@Test
	public void testGetter() throws Exception {
		final JavaObject jo = new JavaObject();
		jo.setString("hello");
		
		final Method m = JavaObject.class.getDeclaredMethod("getString");

		final Function<Object,Object> fn = LambdaMetafactoryUtil.getter(m);

		assertEquals("hello", fn.apply(jo));
	}

	@Test
	public void testSetter() throws Exception {
		final JavaObject jo = new JavaObject();
		
		final Method m = JavaObject.class.getDeclaredMethod("setString", String.class);

		final BiConsumer<Object,Object> fn = LambdaMetafactoryUtil.setter(m);
		fn.accept(jo, "hello");
		
		assertEquals("hello", jo.getString());
	}

	@Test
	public void test1ArgFunction() throws Exception {
		final JavaObject jo = new JavaObject();
		
		final Method m = JavaObject.class.getDeclaredMethod("_String", String.class);

		final Function2<Object,Object,Object> fn = LambdaMetafactoryUtil.function2(m);
		assertEquals("hello", fn.apply(jo, "hello"));
	}

	@Test
	public void test1ArgVoidFunction() throws Exception {
		final JavaObject jo = new JavaObject();
		
		final Method m = JavaObject.class.getDeclaredMethod("setString", String.class);

		final Consumer2<Object,Object> fn = LambdaMetafactoryUtil.consumer2(m);
		fn.accept(jo, "hello");
		
		assertEquals("hello", jo.getString());
	}

	@Test
	public void test2ArgFunction() throws Exception {
		final JavaObject jo = new JavaObject();
		
		final Method m = JavaObject.class.getDeclaredMethod("_StringString", String.class, String.class);

		final Function3<Object,Object,Object,Object> fn = LambdaMetafactoryUtil.function3(m);

		assertEquals("hello,world", fn.apply(jo, "hello", "world"));
	}
	
}
