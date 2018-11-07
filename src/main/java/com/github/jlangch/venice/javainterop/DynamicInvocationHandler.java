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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import com.github.jlangch.venice.impl.javainterop.JavaInterop;
import com.github.jlangch.venice.impl.javainterop.JavaInteropUtil;
import com.github.jlangch.venice.impl.types.Coerce;
import com.github.jlangch.venice.impl.types.Types;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;


/**
 * DynamicInvocationHandler
 * 
 * <pre>
 * Map proxyInstance = (Map)Proxy.newProxyInstance(
 *                             DynamicProxyTest.class.getClassLoader(), 
 *                             new Class[] { Map.class }, 
 *                             new DynamicInvocationHandler());
 *                             
 * proxyInstance.put("hello", "world");
 * </pre>
 */
public class DynamicInvocationHandler implements InvocationHandler {
	
	public DynamicInvocationHandler(final Map<String, VncFunction> methods) {
		this.methods = methods;
		this.parentInterceptor = JavaInterop.getInterceptor();
	}
		 
	@Override
	public Object invoke(
			final Object proxy, 
			final Method method, 
			final Object[] args
	) throws Throwable { 
		final VncFunction fn = methods.get(method.getName());
		if (fn != null) {
			final VncList vncArgs = new VncList();
			if (args != null) {
				for(Object arg : args) {
					vncArgs.addAtEnd(JavaInteropUtil.convertToVncVal(arg));
				}
			}
				
			// [SECURITY]
			//
			// Ensure that the Venice callback is running in the Venice's 
			// sandbox. The Java callback parent could actually fork a thread
			// to run this Venice proxy callback!
			
			final IInterceptor proxyInterceptor = JavaInterop.getInterceptor();
			if (proxyInterceptor == parentInterceptor) {
				// we run in the same thread
				return JavaInteropUtil.convertToJavaObject(fn.apply(vncArgs));
			}
			else {
				// the call back run's in another thread, 
				try {
					JavaInterop.register(parentInterceptor);
					
					return JavaInteropUtil.convertToJavaObject(fn.apply(vncArgs));
				}
				finally {
					JavaInterop.unregister();
				}
			}
		}
		else {
			throw new UnsupportedOperationException(
					String.format("ProxyMethod %s", method.getName()));
		}
	}
	
	public static Object proxify(
			final Class<?> clazz, 
			final Map<String, VncFunction> handlers
	) {
		return Proxy.newProxyInstance(
				DynamicInvocationHandler.class.getClassLoader(), 
				new Class[] { clazz }, 
				new DynamicInvocationHandler(handlers));
	}
	
	public static Object proxify(
			final Class<?> clazz, 
			final VncMap handlers
	) {
		final Map<String, VncFunction> handlerMap = new HashMap<>();
		for(Map.Entry<VncVal,VncVal> entry : handlers.entries()) {
			handlerMap.put(
					Types.isVncKeyword(entry.getKey())
						? Coerce.toVncKeyword(entry.getKey()).getValue()
						: Coerce.toVncString(entry.getKey()).getValue(), 
					Coerce.toVncFunction(entry.getValue()));
		}
		
		return Proxy.newProxyInstance(
				DynamicInvocationHandler.class.getClassLoader(), 
				new Class[] { clazz }, 
				new DynamicInvocationHandler(handlerMap));
	}
	
	
	final Map<String, VncFunction> methods;
	final IInterceptor parentInterceptor;
}
