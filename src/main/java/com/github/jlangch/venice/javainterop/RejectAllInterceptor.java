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
package com.github.jlangch.venice.javainterop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.jlangch.venice.SecurityException;
import com.github.jlangch.venice.impl.sandbox.RestrictedBlacklistedFunctions;
import com.github.jlangch.venice.impl.sandbox.SandboxDefaultRules;


/**
 * Defines a safe Venice sandbox rejecting all I/O functions and Java interoperability.
 */
public class RejectAllInterceptor extends Interceptor {

    public RejectAllInterceptor() {
        super(LoadPathsFactory.rejectAll());

        this.executionTimeDeadline = calcExecutionTimeDeadline(MAX_EXECUTION_TIME_SECONDS);
    }

    @Override
    public ReturnValue onInvokeInstanceMethod(
            final IInvoker invoker,
            final Object receiver,
            final Class<?> receiverFormalType,
            final String method,
            final Object... args
    ) throws SecurityException {
        if (receiver == null) {
            throw new SecurityException(String.format(
                    "%s: Access to any java object denied",
                    PREFIX));
        }
        else {
            throw new SecurityException(String.format(
                        "%s: Access denied to target %s",
                        PREFIX,
                        receiver.getClass().getName()));
        }
    }

    @Override
    public ReturnValue onInvokeStaticMethod(
            final IInvoker invoker,
            final Class<?> receiver,
            final String method,
            final Object... args
    ) throws SecurityException {
        throw new SecurityException(String.format(
                "%s: Access denied to target %s",
                PREFIX,
                receiver.getName()));
    }

    @Override
    public ReturnValue onInvokeConstructor(
            final IInvoker invoker,
            final Class<?> receiver,
            final Object... args
    ) throws SecurityException {
        throw new SecurityException(String.format(
                "%s: Access denied to target %s",
                PREFIX,
                receiver.getName()));
    }

    @Override
    public ReturnValue onGetBeanProperty(
            final IInvoker invoker,
            final Object receiver,
            final String property
    ) throws SecurityException {
        if (receiver == null) {
            throw new SecurityException(String.format(
                    "%s: Access to any java object denied",
                    PREFIX));
        }
        else {
            throw new SecurityException(String.format(
                    "%s: Access denied to target %s",
                    PREFIX,
                    receiver.getClass().getName()));
        }
    }

    @Override
    public void onSetBeanProperty(
            final IInvoker invoker,
            final Object receiver,
            final String property,
            final Object value
    ) throws SecurityException {
        if (receiver == null) {
            throw new SecurityException(String.format(
                    "%s: Access to any java object denied",
                    PREFIX));
        }
        else {
            throw new SecurityException(String.format(
                    "%s: Access denied to target %s",
                    PREFIX,
                    receiver.getClass().getName()));
        }
    }

    @Override
    public ReturnValue onGetStaticField(
            final IInvoker invoker,
            final Class<?> receiver,
            final String fieldName
    ) throws SecurityException {
        throw new SecurityException(String.format(
                "%s: Access denied to target %s",
                PREFIX,
                receiver.getName()));
    }

    @Override
    public ReturnValue onGetInstanceField(
            final IInvoker invoker,
            final Object receiver,
            final Class<?> receiverFormalType,
            final String fieldName
    ) throws SecurityException {
        if (receiver == null) {
            throw new SecurityException(String.format(
                    "%s: Access to any java object denied",
                    PREFIX));
        }
        else {
            throw new SecurityException(String.format(
                    "%s: Access denied to target %s",
                    PREFIX,
                    receiver.getClass().getName()));
        }
    }

    @Override
    public byte[] onLoadClassPathResource(
            final String resourceName
    ) throws SecurityException {
        throw new SecurityException(String.format(
                "%s: Access denied to classpath resource '%s'",
                PREFIX,
                resourceName));
    }

    @Override
    public String onReadSystemProperty(
            final String propertyName
    ) throws SecurityException {
        throw new SecurityException(String.format(
                "%s: Access denied to system property '%s'",
                PREFIX,
                propertyName));
    }

    @Override
    public String onReadSystemEnv(
            final String name
    ) throws SecurityException {
        throw new SecurityException(String.format(
                "%s: Access denied to system environment variable '%s'",
                PREFIX,
                name));
    }

    @Override
    public IInterceptor validateVeniceFunction(
            final String funcName
    ) throws SecurityException {
        if (RestrictedBlacklistedFunctions.getAllFunctions().contains(funcName)) {
            throw new SecurityException(String.format(
                    "%s: Access denied to Venice function '%s'!",
                    PREFIX,
                    funcName));
        }
        return this;
    }

    @Override
    public IInterceptor validateLoadModule(
            final String moduleName
    ) throws SecurityException {
        if (!SandboxDefaultRules.DEFAULT_WHITELISTED_MODULES.contains(moduleName)) {
            throw new SecurityException(String.format(
                    "%s: Access denied to Venice module :%s!",
                    PREFIX,
                    moduleName));
        }
        return this;
    }

    @Override
    public IInterceptor validateMaxExecutionTime() throws SecurityException {
        if (executionTimeDeadline > 0 && System.currentTimeMillis() > executionTimeDeadline) {
            throw new SecurityException(
                    "Venice Sandbox: The sandbox exceeded the max execution time");
        }
        return this;
    }

    @Override
    public Integer getMaxExecutionTimeSeconds() {
        return MAX_EXECUTION_TIME_SECONDS;
    }

    @Override
    public Integer getMaxFutureThreadPoolSize() {
        return MAX_FUTURE_THREAD_POOL_SIZE;
    }


    public List<String> getBlacklistedVeniceFunctions() {
        final List<String> list = new ArrayList<>(RestrictedBlacklistedFunctions.getAllFunctions());
        Collections.sort(list);
        return list;
    }

    public List<String> getWhitelistedVeniceModules() {
        final List<String> list = new ArrayList<>(SandboxDefaultRules.DEFAULT_WHITELISTED_MODULES);
        Collections.sort(list);
        return list;
    }


    private static long calcExecutionTimeDeadline(final Integer maxExecutionTimeSeconds) {
        return maxExecutionTimeSeconds == null
                ? -1L
                : System.currentTimeMillis() + maxExecutionTimeSeconds * 1000L;
    }



    private static final String PREFIX = "Venice Sandbox (RejectAllInterceptor)";

    private static final Integer MAX_EXECUTION_TIME_SECONDS = null; // null is unlimited

    private static final Integer MAX_FUTURE_THREAD_POOL_SIZE = 5;

    private final long executionTimeDeadline;
}
