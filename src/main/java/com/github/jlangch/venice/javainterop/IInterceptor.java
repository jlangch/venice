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

import com.github.jlangch.venice.SecurityException;


/**
 * Defines a Venice interceptor
 */
public interface IInterceptor {

    /**
     * Invokes an instance method
     *
     * @param invoker               the invoker
     * @param receiver              an object
     * @param receiverFormalType    the formal type of the receiver (e.g a superclass)
     * @param method                a method
     * @param args                  a list of arguments
     * @return the return value
     * @throws SecurityException if the instance method is not whitelisted
     */
    ReturnValue onInvokeInstanceMethod(
            IInvoker invoker,
            Object receiver,
            Class<?> receiverFormalType,
            String method,
            Object... args
    ) throws com.github.jlangch.venice.SecurityException;

    /**
     * Invokes a static method
     *
     * @param invoker   the invoker
     * @param receiver  a class
     * @param method    a method
     * @param args      a list of arguments
     * @return the return value
     * @throws SecurityException if the static method is not whitelisted
     */
    ReturnValue onInvokeStaticMethod(
            IInvoker invoker,
            Class<?> receiver,
            String method,
            Object... args
    ) throws com.github.jlangch.venice.SecurityException;

    /**
     * Invokes a constructor
     *
     * @param invoker   the invoker
     * @param receiver  a class
     * @param args  a list of arguments
     * @return the create object
     * @throws com.github.jlangch.venice.SecurityException if the constructor is not whitelisted
     */
    ReturnValue onInvokeConstructor(
            IInvoker invoker,
            Class<?> receiver,
            Object... args
    ) throws com.github.jlangch.venice.SecurityException;

    /**
     * Gets a <i>Java Bean</i> property
     *
     * @param invoker   the invoker
     * @param receiver  an object
     * @param property  a property name
     * @return the property's value
     * @throws SecurityException if the bean property (instance method) is not whitelisted
     */
    ReturnValue onGetBeanProperty(
            IInvoker invoker,
            Object receiver,
            String property
    ) throws com.github.jlangch.venice.SecurityException;

    /**
     * Sets a <i>Java Bean</i> property
     *
     * @param invoker   the invoker
     * @param receiver  an object
     * @param property  a property name
     * @param value     a property value
     * @throws SecurityException if the bean property (instance method) is not whitelisted
     */
    void onSetBeanProperty(
            IInvoker invoker,
            Object receiver,
            String property,
            Object value
    ) throws com.github.jlangch.venice.SecurityException;

    /**
     * Get a static field's value
     *
     * @param invoker   the invoker
     * @param receiver  a class
     * @param fieldName a field name
     * @return the field's value
     * @throws com.github.jlangch.venice.SecurityException if the static field is not whitelisted
     */
    ReturnValue onGetStaticField(
            IInvoker invoker,
            Class<?> receiver,
            String fieldName
    ) throws com.github.jlangch.venice.SecurityException;

    /**
     * Get an instance field's value
     *
     * @param invoker   the invoker
     * @param receiver  an object
     * @param receiverFormalType    the formal type of the receiver (e.g a superclass)
     * @param fieldName a field name
     * @return the field's value
     * @throws SecurityException if the instance field is not whitelisted
     */
    ReturnValue onGetInstanceField(
            IInvoker invoker,
            Object receiver,
            Class<?> receiverFormalType,
            String fieldName
    ) throws com.github.jlangch.venice.SecurityException;

    /**
     * Loads a classpath resource
     *
     * @param resourceName a resource name (e.g.: foo/org/image.png)
     * @return the resource data
     * @throws SecurityException if the classpath resource is not whitelisted
     */
    byte[] onLoadClassPathResource(String resourceName) throws com.github.jlangch.venice.SecurityException;

    /**
     * Reads a Java system property
     *
     * @param propertyName a property name (e.g: user.home)
     * @return the property's value
     * @throws SecurityException if the property is not whitelisted
     */
    String onReadSystemProperty(String propertyName) throws com.github.jlangch.venice.SecurityException;

    /**
     * Reads a Java environment variable
     *
     * @param name a variable name (e.g: USER)
     * @return the variable value
     * @throws SecurityException if the variable is not whitelisted
     */
    String onReadSystemEnv(String name) throws com.github.jlangch.venice.SecurityException;

    /**
     * Validates the load of a module
     *
     * @param moduleName the module name
     * @return this interceptor, for chaining validation
     * @throws SecurityException if the module is blacklisted
     */
    IInterceptor validateLoadModule(String moduleName) throws com.github.jlangch.venice.SecurityException;

    /**
     * Validates the invocation of a Venice function.
     *
     * @param funcName A venice function name
     * @return this interceptor, for chaining validation
     * @throws SecurityException if the function is blacklisted and not
     *                           allowed to be invoked.
     */
    IInterceptor validateVeniceFunction(String funcName) throws com.github.jlangch.venice.SecurityException;

    /**
     * Validates the execution time
     *
     * @return this interceptor, for chaining validation
     * @throws SecurityException if the execution time exceeds the configured limit.
     */
    IInterceptor validateMaxExecutionTime() throws com.github.jlangch.venice.SecurityException;

    /**
     * @return the load paths for loading Venice files and resources
     */
    ILoadPaths getLoadPaths();

    /**
     * @return the max execution time in seconds a Venice script under this
     * <code>Sandbox</code> is allowed to run.
     */
    Integer getMaxExecutionTimeSeconds();

    /**
     * @return the max future thread pool size a Venice script under this
     * {@code Sandbox} is allowed to use.
     */
    Integer getMaxFutureThreadPoolSize();

}
