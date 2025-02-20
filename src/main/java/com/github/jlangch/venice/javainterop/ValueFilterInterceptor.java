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
package com.github.jlangch.venice.javainterop;

import com.github.jlangch.venice.SecurityException;


public class ValueFilterInterceptor extends Interceptor {

    public ValueFilterInterceptor(final ILoadPaths loadPaths) {
        super(loadPaths);
    }

    @Override
    public ReturnValue onInvokeInstanceMethod(
            final IInvoker invoker,
            final Object receiver,
            final Class<?> receiverFormalType,
            final String method,
            final Object... args
    ) throws SecurityException {
        filterAccessor(receiver, method);
        return filterReturnValue(
                super.onInvokeInstanceMethod(
                        invoker, receiver, receiverFormalType, method, filterArguments(args)));
    }

    @Override
    public ReturnValue onInvokeStaticMethod(
            final IInvoker invoker,
            final Class<?> receiver,
            final String method,
            final Object... args
    ) throws SecurityException {
        filterAccessor(receiver, method);
        return filterReturnValue(
                super.onInvokeStaticMethod(
                        invoker, receiver, method, filterArguments(args)));
    }

    @Override
    public ReturnValue onInvokeConstructor(
            final IInvoker invoker,
            final Class<?> receiver,
            final Object... args
    ) throws SecurityException {
        filterAccessor(receiver, "new");
        return filterReturnValue(
                super.onInvokeConstructor(
                        invoker, receiver, filterArguments(args)));
    }

    @Override
    public ReturnValue onGetBeanProperty(
            final IInvoker invoker,
            final Object receiver,
            final String property
    ) throws SecurityException {
        filterAccessor(receiver, property);
        return filterReturnValue(
                super.onGetBeanProperty(
                        invoker, receiver, property));
    }

    @Override
    public void onSetBeanProperty(
            final IInvoker invoker,
            final Object receiver,
            final String property,
            final Object value
    ) throws SecurityException {
        filterAccessor(receiver, property);
        super.onSetBeanProperty(
                    invoker, receiver, property, filterArgument(value));
    }

    @Override
    public ReturnValue onGetStaticField(
            final IInvoker invoker,
            final Class<?> receiver,
            final String fieldName
    ) throws SecurityException {
        filterAccessor(receiver, fieldName);
        return filterReturnValue(
                super.onGetStaticField(
                        invoker, receiver, fieldName));
    }

    @Override
    public ReturnValue onGetInstanceField(
            final IInvoker invoker,
            final Object receiver,
            final Class<?> receiverFormalType,
            final String fieldName
    ) throws SecurityException {
        filterAccessor(receiver, fieldName);
        return filterReturnValue(
                super.onGetInstanceField(
                        invoker, receiver, receiverFormalType, fieldName));
    }


    protected ReturnValue filterReturnValue(final ReturnValue returnValue) {
        return returnValue;
    }

    protected Object filterArgument(final Object arg) {
        return filter(arg);
    }

    protected Object filter(final Object o) {
        return o;
    }

    protected Object filterAccessor(final Object o, final String accessor) {
        return o;
    }


    private Object[] filterArguments(final Object[] args) {
        for (int i=0; i<args.length; i++) {
            args[i] = filterArgument(args[i]);
        }
        return args;
    }

}
