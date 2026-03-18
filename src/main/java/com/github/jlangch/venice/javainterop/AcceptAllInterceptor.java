/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2026 Venice
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

/**
 * Defines a Venice sandbox without any restrictions.
 */
public class AcceptAllInterceptor extends Interceptor {

    public AcceptAllInterceptor(final ILoadPaths loadPaths) {
        super(loadPaths == null ? LoadPathsFactory.acceptAll() : loadPaths);
    }

    public AcceptAllInterceptor() {
        super(LoadPathsFactory.acceptAll());
    }


    @Override
    public ReturnValue onInvokeInstanceMethod(
            final IInvoker invoker,
            final Object receiver,
            final Class<?> receiverFormalType,
            final String method,
            final Object... args
    ) throws com.github.jlangch.venice.SecurityException {
        return super.onInvokeInstanceMethod(invoker, receiver, receiverFormalType, method, args);
    }

    @Override
    public ReturnValue onInvokeStaticMethod(
            final IInvoker invoker,
            final Class<?> receiver,
            final String method,
            final Object... args
    ) throws com.github.jlangch.venice.SecurityException {
        return super.onInvokeStaticMethod(invoker, receiver, method, args);
    }

    @Override
    public ReturnValue onInvokeConstructor(
            final IInvoker invoker,
            final Class<?> receiver,
            final Object... args
    ) throws com.github.jlangch.venice.SecurityException {
        return super.onInvokeConstructor(invoker, receiver, args);
    }

    @Override
    public ReturnValue onGetBeanProperty(
            final IInvoker invoker,
            final Object receiver,
            final String property
    ) throws com.github.jlangch.venice.SecurityException {
        return super.onGetBeanProperty(invoker, receiver, property);
    }

    @Override
    public void onSetBeanProperty(
            final IInvoker invoker,
            final Object receiver,
            final String property,
            final Object value
    ) throws com.github.jlangch.venice.SecurityException {
        super.onSetBeanProperty(invoker, receiver, property, value);
    }

    @Override
    public ReturnValue onGetStaticField(
            final IInvoker invoker,
            final Class<?> receiver,
            final String fieldName
    ) throws com.github.jlangch.venice.SecurityException {
        return super.onGetStaticField(invoker, receiver, fieldName);
    }

    @Override
    public ReturnValue onGetInstanceField(
            final IInvoker invoker,
            final Object receiver,
            final Class<?> receiverFormalType,
            final String fieldName
    ) throws com.github.jlangch.venice.SecurityException {
        return super.onGetInstanceField(invoker, receiver, receiverFormalType, fieldName);
    }

    @Override
    public byte[] onLoadClassPathResource(
            final String resourceName
    ) throws com.github.jlangch.venice.SecurityException {
        return super.onLoadClassPathResource(resourceName);
    }

    @Override
    public String onReadSystemProperty(
            final String propertyName
    ) throws com.github.jlangch.venice.SecurityException {
        return super.onReadSystemProperty(propertyName);
    }

    @Override
    public String onReadSystemEnv(
            final String name
    ) throws com.github.jlangch.venice.SecurityException {
        return super.onReadSystemEnv(name);
    }

    @Override
    public IInterceptor validateVeniceFunction(
            final String funcName
    ) throws com.github.jlangch.venice.SecurityException {
        // ok
        return this;
    }

    @Override
    public IInterceptor validateLoadModule(
            final String moduleName
    ) throws com.github.jlangch.venice.SecurityException {
        // ok
        return this;
    }
}
