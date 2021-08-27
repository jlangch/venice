/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2021 Venice
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
package com.github.jlangch.venice.impl.javainterop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.concurrent.ThreadContext;
import com.github.jlangch.venice.impl.types.concurrent.ThreadContextSnapshot;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.CallFrame;
import com.github.jlangch.venice.impl.util.CallStack;


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
	
	private DynamicInvocationHandler(
			final CallFrame callFrameProxy,
			final Map<String, VncFunction> methods
	) {
		this.callFrameProxy = callFrameProxy;
		this.methods = methods;
		
		this.parentThreadLocalSnapshot = new AtomicReference<>(ThreadContext.snapshot());

	}
		 
	@Override
	public Object invoke(
			final Object proxy, 
			final Method method, 
			final Object[] args
	) throws Throwable { 
		final VncFunction fn = methods.get(method.getName());
		if (fn != null) {
			final VncList fnArgs = toVncArgs(args);
				
			final CallStack callStack = ThreadContext.getCallStack();
			final CallFrame callFrameMethod = new CallFrame(
													"proxy(:" + method.getName() + ")->" + fn.getQualifiedName(),
													fn.getMeta());
			
			// [SECURITY]
			//
			// Ensure that the Venice callback function is running in the Venice's 
			// sandbox. The Java callback parent could have forked a thread
			// to run this Venice proxy callback!
			
			if (parentThreadLocalSnapshot.get().isSameAsCurrentThread()) {
				// we run in the same security context (thread)
				try {
					callStack.push(callFrameProxy);
					callStack.push(callFrameMethod);
					
					return fn.apply(fnArgs).convertToJavaObject();
				}
				finally {
					callStack.pop();
					callStack.pop();
				}
			}
			else {
				// The callback function runs in another thread.	
				// Inherit sandbox and thread local vars
				try {
					ThreadContext.clear();
					ThreadContext.inheritFrom(parentThreadLocalSnapshot.get());
					
					callStack.push(callFrameProxy);
					callStack.push(callFrameMethod);
					
					return fn.apply(fnArgs).convertToJavaObject();
				}
				finally {
					callStack.pop();
					callStack.pop();
					
					ThreadContext.remove();
				}
			}
		}
		else {
			throw new UnsupportedOperationException(
					String.format("ProxyMethod %s", method.getName()));
		}
	}
		
	public static Object proxify(
			final CallFrame callFrame,
			final Class<?> clazz, 
			final VncMap handlers
	) {
		return Proxy.newProxyInstance(
				DynamicInvocationHandler.class.getClassLoader(), 
				new Class[] { clazz }, 
				new DynamicInvocationHandler(callFrame, handlerMap(handlers)));
	}
	
	private static String name(final VncVal val) {
		if (Types.isVncKeyword(val)) {
			return ((VncKeyword)val).getValue();
		}
		else if (Types.isVncString(val)) {
			return ((VncString)val).getValue();
		}
		else {
			throw new VncException("A proxy handler map key must be of type VncKeyword or VncString");
		}
	}

	private static Map<String, VncFunction> handlerMap(final VncMap handlers) {
		return handlers
					.entries()
					.stream()
					.collect(Collectors.toMap(
						e -> name(e.getKey()), 
						e -> Coerce.toVncFunction(e.getValue())));
	}
	
	private static VncList toVncArgs(final Object[] args) {
		if (args == null || args.length == 0) {
			return VncList.empty();
		}
		else {
			final VncVal[] vncArgs = new VncVal[args.length];		
			for(int ii=0; ii<args.length; ii++) {
				vncArgs[ii] = JavaInteropUtil.convertToVncVal(args[ii]);
			}
			return VncList.of(vncArgs);
		}
	}

	
	private final CallFrame callFrameProxy;
	private final Map<String, VncFunction> methods;
	private final AtomicReference<ThreadContextSnapshot> parentThreadLocalSnapshot;
}
