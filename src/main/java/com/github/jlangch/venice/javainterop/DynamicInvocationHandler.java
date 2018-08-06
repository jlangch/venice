package com.github.jlangch.venice.javainterop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

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
			return JavaInteropUtil.convertToJavaObject(fn.apply(vncArgs));
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
}
