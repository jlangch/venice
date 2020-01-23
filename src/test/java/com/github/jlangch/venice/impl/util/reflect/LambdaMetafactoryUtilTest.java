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

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.util.reflect.LambdaMetafactoryUtil.Consumer1;
import com.github.jlangch.venice.impl.util.reflect.LambdaMetafactoryUtil.Consumer2;
import com.github.jlangch.venice.impl.util.reflect.LambdaMetafactoryUtil.Consumer3;
import com.github.jlangch.venice.impl.util.reflect.LambdaMetafactoryUtil.Function1;
import com.github.jlangch.venice.impl.util.reflect.LambdaMetafactoryUtil.Function2;
import com.github.jlangch.venice.impl.util.reflect.LambdaMetafactoryUtil.Function3;


public class LambdaMetafactoryUtilTest {

	@Test
	public void test0ArgFunction() throws Exception {
		final TestObject to = new TestObject();
		
		final Method m = TestObject.class.getDeclaredMethod("fn_string_void");

		final Function1<Object,Object> fn = LambdaMetafactoryUtil.function0Args(m);

		assertEquals("-", fn.apply(to));
	}

	@Test
	public void test1ArgFunction() throws Exception {
		final TestObject to = new TestObject();
		
		final Method m = TestObject.class.getDeclaredMethod("fn_string_string", String.class);

		final Function2<Object,Object,Object> fn = LambdaMetafactoryUtil.function1Args(m);
		assertEquals("hello", fn.apply(to, "hello"));
	}

	@Test
	public void test2ArgFunction() throws Exception {
		final TestObject to = new TestObject();
		
		final Method m = TestObject.class.getDeclaredMethod("fn_string_string_string", String.class, String.class);

		final Function3<Object,Object,Object,Object> fn = LambdaMetafactoryUtil.function2Args(m);

		assertEquals("hello-world", fn.apply(to, "hello", "world"));
	}

	
	@Test
	public void test0ArgVoidFunction() throws Exception {
		final TestObject to = new TestObject();
		
		final Method m = TestObject.class.getDeclaredMethod("fn_void_void");

		final Consumer1<Object> fn = LambdaMetafactoryUtil.consumer0Args(m);
		fn.accept(to);
		
		assertEquals("void", to.last());
	}
	
	@Test
	public void test1ArgVoidFunction() throws Exception {
		final TestObject to = new TestObject();
		
		final Method m = TestObject.class.getDeclaredMethod("fn_void_string", String.class);

		final Consumer2<Object,Object> fn = LambdaMetafactoryUtil.consumer1Args(m);
		fn.accept(to, "hello");
		
		assertEquals("hello", to.last());
	}
	
	@Test
	public void test2ArgVoidFunction() throws Exception {
		final TestObject to = new TestObject();
		
		final Method m = TestObject.class.getDeclaredMethod("fn_void_string_string", String.class, String.class);

		final Consumer3<Object,Object,Object> fn = LambdaMetafactoryUtil.consumer2Args(m);
		fn.accept(to, "hello", "world");
		
		assertEquals("hello-world", to.last());
	}
	
	
	
	@SuppressWarnings("unused")
	private static class TestObject {

		public TestObject() {
		}
		
		public void fn_void_void() {
			last = "void";
		}
		
		public void fn_void_string(final String s1) {
			last = s1;
		}
		
		public void fn_void_string_string(final String s1, final String s2) {
			last = s1 + "-" + s2;
		}
		
		public String fn_string_void() {
			last = "-";
			return last;
		}
		
		public String fn_string_string(final String s1) {
			last = s1;
			return last;
		}
		
		public String fn_string_string_string(final String s1, final String s2) {
			last = s1 + "-" + s2;
			return last;
		}
		
		
		public String last() {
			return last;
		}
		
		
		private String last = "init";
	}

}
