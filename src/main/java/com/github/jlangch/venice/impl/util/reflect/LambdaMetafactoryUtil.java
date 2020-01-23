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

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.github.jlangch.venice.JavaMethodInvocationException;


public class LambdaMetafactoryUtil {

	public static Function<Object,Object> getter(final Method method) {
		try {
			final MethodHandles.Lookup caller = MethodHandles.lookup();
			final MethodHandle handle = caller.unreflect(method);
			
			return (Function<Object,Object>)LambdaMetafactory
					.metafactory(
						caller,
						"apply",
						MethodType.methodType(Function.class),
						MethodType.methodType(Object.class, Object.class),
						handle,
						handle.type())
					.getTarget()
					.invoke();
		} 
		catch (Throwable ex) {
			throw new JavaMethodInvocationException(
						"Could not generate the function to access the getter " + method.getName(), 
						ex);
		}
	}

	public static BiConsumer<Object,Object> setter(Method method) {
		try {
			final MethodHandles.Lookup caller = MethodHandles.lookup();
			final MethodHandle handle = caller.unreflect(method);
			
			return (BiConsumer<Object,Object>)LambdaMetafactory
					.metafactory(
						caller,
						"accept",
						MethodType.methodType(BiConsumer.class),
						MethodType.methodType(Void.TYPE, Object.class, Object.class),
						handle,
						handle.type())
					.getTarget()
					.invoke();
		} 
		catch (Throwable ex) {
			throw new JavaMethodInvocationException(
					"Could not generate the function to access the setter " + method.getName(), 
					ex);
		}
	}
	
	public static Function2<Object,Object,Object> function2(final Method method) {
		try {
			final MethodHandles.Lookup caller = MethodHandles.lookup();
			final MethodHandle handle = caller.unreflect(method);
			
			return (Function2<Object,Object,Object>)LambdaMetafactory
					.metafactory(
						caller,
						"apply",
						MethodType.methodType(Function2.class),
						MethodType.methodType(Object.class, Object.class, Object.class),
						handle,
						handle.type())
					.getTarget()
					.invoke();
		} 
		catch (Throwable ex) {
			throw new JavaMethodInvocationException(
						"Could not generate the function to access a 1 arg method " + method.getName(), 
						ex);
		}
	}
	
	public static Function3<Object,Object,Object,Object> function3(final Method method) {
		try {
			final MethodHandles.Lookup caller = MethodHandles.lookup();
			final MethodHandle handle = caller.unreflect(method);
			
			return (Function3<Object,Object,Object,Object>)LambdaMetafactory
					.metafactory(
						caller,
						"apply",
						MethodType.methodType(Function3.class),
						MethodType.methodType(Object.class, Object.class, Object.class, Object.class),
						handle,
						handle.type())
					.getTarget()
					.invoke();
		} 
		catch (Throwable ex) {
			throw new JavaMethodInvocationException(
						"Could not generate the function to access a 2 arg method " + method.getName(), 
						ex);
		}
	}

	public static Consumer2<Object,Object> consumer2(final Method method) {
		try {
			final MethodHandles.Lookup caller = MethodHandles.lookup();
			final MethodHandle handle = caller.unreflect(method);
			
			return (Consumer2<Object,Object>)LambdaMetafactory
					.metafactory(
						caller,
						"accept",
						MethodType.methodType(Consumer2.class),
						MethodType.methodType(Void.TYPE, Object.class, Object.class),
						handle,
						handle.type())
					.getTarget()
					.invoke();
		} 
		catch (Throwable ex) {
			throw new JavaMethodInvocationException(
						"Could not generate the function to access a 1 arg void method " + method.getName(), 
						ex);
		}
	}

	public static Consumer3<Object,Object,Object> consumer3(final Method method) {
		try {
			final MethodHandles.Lookup caller = MethodHandles.lookup();
			final MethodHandle handle = caller.unreflect(method);
			
			return (Consumer3<Object,Object,Object>)LambdaMetafactory
					.metafactory(
						caller,
						"accept",
						MethodType.methodType(Consumer2.class),
						MethodType.methodType(Void.TYPE, Object.class, Object.class, Object.class),
						handle,
						handle.type())
					.getTarget()
					.invoke();
		} 
		catch (Throwable ex) {
			throw new JavaMethodInvocationException(
						"Could not generate the function to access a 2 arg void method " + method.getName(), 
						ex);
		}
	}

	@FunctionalInterface
	public static interface Function2<A, B, R> {
		R apply(A a, B b);
	}
	
	@FunctionalInterface
	public static interface Function3<A, B, C, R> {
		R apply(A a, B b, C c);
	}
	
	@FunctionalInterface
	public static interface Function4<A, B, C, D, R> {
		R apply(A a, B b, C c, D d);
	}
	
	@FunctionalInterface
	public static interface Consumer2<A, B> {
		void accept(A a, B b);
	}
	
	@FunctionalInterface
	public static interface Consumer3<A, B, C> {
		void accept(A a, B b, C c);
	}
	
	@FunctionalInterface
	public static interface Consumer4<A, B, C, D> {
		void accept(A a, B b, C c, D d);
	}

}
