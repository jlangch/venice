/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2025 Venice
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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.thread.ThreadBridge;
import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.callstack.CallFrame;
import com.github.jlangch.venice.impl.util.callstack.CallStack;
import com.github.jlangch.venice.impl.util.reflect.Boxing;
import com.github.jlangch.venice.impl.util.reflect.ReflectionUtil;


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
            final Class<?> clazz,
            final Map<String, VncFunction> methods,
            final CallFrame callFrameProxy
    ) {
        this.clazz = clazz;
        this.methods = methods;
        this.threadBridge = ThreadBridge.create("proxy");
        this.callFrameProxy = callFrameProxy;
        this.java8 = "1.8".equals(System.getProperty("java.specification.version"));
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

            final CallFrame callFrameMethod = new CallFrame(
                                                    "proxy(:" + method.getName() + ")->" + fn.getQualifiedName(),
                                                    fn.getMeta());

            final Callable<Object> impl = () -> {
                final CallStack callStack = ThreadContext.getCallStack();
                callStack.push(callFrameProxy);
                callStack.push(callFrameMethod);
                try {
                    return fn.apply(fnArgs).convertToJavaObject();
                }
                finally {
                    callStack.pop();
                    callStack.pop();
                }};


            if (threadBridge.isSameAsCurrentThread()) {
                // we're running in the same thread as the caller (parent)
                return impl.call();
            }
            else {
                // we're running in an other thread
                return threadBridge.bridgeCallable(() -> impl.call())
                                   .call();
            }
        }
        else if (method.isDefault()) {
            return invokeDefaultMethod(proxy, method, args);
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
                new DynamicInvocationHandler(clazz, handlerMap(handlers), callFrame));
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
        final Map<String, VncFunction> map = new HashMap<>();

        handlers.entries().forEach(e -> {
            final VncFunction fn = Coerce.toVncFunction(e.getValue());
            fn.sandboxFunctionCallValidation();
            map.put(name(e.getKey()), fn);
        });

        return map;
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

    private Object invokeDefaultMethod(
            final Object proxy,
            final Method method,
            final Object[] args
    ) throws Throwable {
        final List<Method> methods = ReflectionUtil.getAllPublicDefaultMethods(
                                        clazz,
                                        method.getName(),
                                        args == null ? 0 : args.length,
                                        false);

        if (methods.size() == 0) {
            throw new UnsupportedOperationException(
                    String.format("ProxyMethod %s", method.getName()));
        }

        final Method m = methods.get(0);

        final Object[] boxedArgs = Boxing.boxArgs(
                                    m.getParameterTypes(),
                                    args);

        if (java8) {
            // Java 8
            final Constructor<Lookup> ctor = Lookup.class
                                                   .getDeclaredConstructor(Class.class);
            ctor.setAccessible(true);

            return ctor.newInstance(clazz)
                       .in(clazz)
                       .unreflectSpecial(m, clazz)
                       .bindTo(proxy)
                       .invokeWithArguments(boxedArgs);
        }
        else {
            // Java 9, 10, 11, ....
            return MethodHandles
                        .lookup()
                        .findSpecial(
                             clazz,
                             m.getName(),
                             MethodType.methodType(
                                 m.getReturnType(),
                                 m.getParameterTypes()),
                             clazz)
                        .bindTo(proxy)
                        .invokeWithArguments(boxedArgs);
        }
    }


    private final Class<?> clazz;
    private final Map<String, VncFunction> methods;
    private final ThreadBridge threadBridge;
    private final CallFrame callFrameProxy;
    private final boolean java8;
}
