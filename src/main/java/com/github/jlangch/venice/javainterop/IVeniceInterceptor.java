/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2014-2018 Venice
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

import com.github.jlangch.venice.impl.types.collections.VncList;


/**
 * Defines a Venice interceptor
 */
public interface IVeniceInterceptor {
 
	/**
	 * Invokes an instance method
	 * 
	 * @param invoker	the invoker
	 * @param receiver	an object
	 * @param method	a method
	 * @param args		a list of arguments
	 * @return the return value
	 * @throws SecurityException if the instance method is not whitelisted
	 */
	Object onInvokeInstanceMethod(
			IInvoker invoker, 
			Object receiver, 
			String method, 
			Object... args
	) throws SecurityException;

	/**
	 * Invokes a static method
	 * 
	 * @param invoker	the invoker
	 * @param receiver	a class
	 * @param method	a method
	 * @param args		a list of arguments
	 * @return the return value
	 * @throws SecurityException if the static method is not whitelisted
	 */
	Object onInvokeStaticMethod(
			IInvoker invoker, 
			Class<?> receiver, 
			String method, 
			Object... args
	) throws SecurityException;

	/**
	 * Invokes a constructor
	 * 
	 * @param invoker	the invoker
	 * @param receiver	a class
	 * @param args		a list of arguments
	 * @return the create object
	 * @throws SecurityException if the constructor is not whitelisted
	 */
	Object onInvokeConstructor(
			IInvoker invoker, 
			Class<?> receiver, 
			Object... args
	) throws SecurityException;

	/**
	 * Gets a <tt>Java Bean</tt> property
	 * 
	 * @param invoker	the invoker
	 * @param receiver	an object
	 * @param property	a property name
	 * @return the property's value
	 * @throws SecurityException if the bean property (instance method) is not whitelisted
	 */
	Object onGetBeanProperty(
			IInvoker invoker, 
			Object receiver, 
			String property
	) throws SecurityException;

	/**
	 * Sets a <tt>Java Bean</tt> property
	 * 
	 * @param invoker	the invoker
	 * @param receiver	an object
	 * @param property	a property name
	 * @param value		a property value
	 * @throws SecurityException if the bean property (instance method) is not whitelisted
	 */
	void onSetBeanProperty(
			IInvoker invoker, 
			Object receiver, 
			String property, 
			Object value
	) throws SecurityException;

	/**
	 * Get a static field's value
	 * 
	 * @param invoker	the invoker
	 * @param receiver	a class
	 * @param fieldName	a field name
	 * @return the field's value
	 * @throws SecurityException if the static field is not whitelisted
	 */
	Object onGetStaticField(
			IInvoker invoker, 
			Class<?> receiver, 
			String fieldName
	) throws SecurityException;

	/**
	 * Get an instance field's value
	 * 
	 * @param invoker	the invoker
	 * @param receiver	an object
	 * @param fieldName	a field name 
	 * @return the field's value
	 * @throws SecurityException if the instance field is not whitelisted
	 */
	Object onGetInstanceField(
			IInvoker invoker, 
			Object receiver, 
			String fieldName
	) throws SecurityException;

	/**
	 * Loads a classpath resource
	 * 
	 * @param resourceName a resource name (e.g.: /foo/org/image.png)
	 * @return the resource data
	 * @throws SecurityException if the classpath resource is not whitelisted
	 */
	byte[] onLoadClassPathResource(String resourceName) throws SecurityException;

	/**
	 * Reads a Java system property
	 * 
	 * @param propertyName a proprty name (e.g: user.home)
	 * @return the property's value
	 * @throws SecurityException if the property is not whitelisted
	 */
	String onReadSystemProperty(String propertyName) throws SecurityException;
	
	/**
	 * Validates the invocation of a Venice function with args.
	 * 
	 * @param funcName A venice function name
	 * @param args A list of arguments
	 * 
	 * @throws SecurityException if the function is blacklisted and not allowed to be invoked.
	 */
	void validateBlackListedVeniceFunction(
			String funcName, 
			VncList args
	) throws SecurityException;

}
