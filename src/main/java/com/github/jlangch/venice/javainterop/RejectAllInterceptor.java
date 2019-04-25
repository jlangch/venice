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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.github.jlangch.venice.impl.functions.IOFnBlacklisted;


public class RejectAllInterceptor extends Interceptor {

	public List<String> getBlacklistedVeniceFunctions() {
		final List<String> list = new ArrayList<>(blacklistedVeniceFunctions);
		Collections.sort(list);
		return list;
	}
	
	@Override
	public Object onInvokeInstanceMethod(
			final IInvoker invoker, 
			final Object receiver, 
			final String method, 
			final Object... args
	) throws SecurityException {
		throw new SecurityException(String.format(
					"Access denied to target %s", 
					receiver.getClass().getName()));
	}

	@Override
	public Object onInvokeStaticMethod(
			final IInvoker invoker, 
			final Class<?> receiver, 
			final String method, 
			final Object... args
	) throws SecurityException {
		throw new SecurityException(String.format(
				"Access denied to target %s", 
				receiver.getName()));
	}

	@Override
	public Object onInvokeConstructor(
			final IInvoker invoker, 
			final Class<?> receiver,
			final Object... args
	) throws SecurityException {
		throw new SecurityException(String.format(
				"Access denied to target %s", 
				receiver.getName()));
	}

	@Override
	public Object onGetBeanProperty(
			final IInvoker invoker, 
			final Object receiver, 
			final String property
	) throws SecurityException {
		throw new SecurityException(String.format(
				"Access denied to target %s", 
				receiver.getClass().getName()));
	}

	@Override
	public void onSetBeanProperty(
			final IInvoker invoker, 
			final Object receiver, 
			final String property, 
			final Object value
	) throws SecurityException {
		throw new SecurityException(String.format(
				"Access denied to target %s", 
				receiver.getClass().getName()));
	}

	@Override
	public Object onGetStaticField(
			final IInvoker invoker, 
			final Class<?> receiver, 
			final String fieldName
	) throws SecurityException {
		throw new SecurityException(String.format(
				"Access denied to target %s", 
				receiver.getName()));
	}

	@Override
	public Object onGetInstanceField(
			final IInvoker invoker, 
			final Object receiver, 
			final String fieldName
	) throws SecurityException {
		throw new SecurityException(String.format(
				"Access denied to target %s", 
				receiver.getClass().getName()));
	}

	@Override
	public byte[] onLoadClassPathResource(
			final String resourceName
	) throws SecurityException {
		throw new SecurityException(String.format(
				"Venice Sandbox: Access denied to classpath resource '%s'", 
				resourceName));
	}

	@Override
	public String onReadSystemProperty(
			final String propertyName
	) throws SecurityException {
		throw new SecurityException(String.format(
				"Venice Sandbox: Access denied to system property '%s'", 
				propertyName));
	}

	@Override
	public void validateVeniceFunction(
			final String funcName
	) throws SecurityException {
		if (blacklistedVeniceFunctions.contains(funcName)) {
			throw new SecurityException(String.format(
					"Access denied to Venice function %s. (reject-all sandbox)", funcName));
		}
	}

	
	private final Set<String> blacklistedVeniceFunctions = IOFnBlacklisted.getAllIoFunctions();
}
