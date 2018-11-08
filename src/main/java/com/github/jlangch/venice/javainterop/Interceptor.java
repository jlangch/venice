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

import com.github.jlangch.venice.impl.util.ClassPathResource;
import com.github.jlangch.venice.impl.util.StringUtil;


public abstract class Interceptor implements IInterceptor {
 
	@Override
	public Object onInvokeInstanceMethod(
			final IInvoker invoker, 
			final Object receiver, 
			final String method, 
			final Object... args
	) throws SecurityException {
		return invoker.callInstanceMethod(receiver, method, args);
	}

	@Override
	public Object onInvokeStaticMethod(
			final IInvoker invoker, 
			final Class<?> receiver, 
			final String method, 
			final Object... args
	) throws SecurityException {
		return invoker.callStaticMethod(receiver, method, args);
	}

	@Override
	public Object onInvokeConstructor(
			final IInvoker invoker, 
			final Class<?> receiver, 
			final Object... args
	) throws SecurityException {
		return invoker.callConstructor(receiver, args);
	}

	@Override
	public Object onGetBeanProperty(
			final IInvoker invoker, 
			final Object receiver, 
			final String property
	) throws SecurityException {
		return invoker.getBeanProperty(receiver, property);
	}

	@Override
	public void onSetBeanProperty(
			final IInvoker invoker, 
			final Object receiver, 
			final String property, 
			final Object value
	) throws SecurityException {
		invoker.setBeanProperty(receiver, property, value);
	}

	@Override
	public Object onGetStaticField(
			final IInvoker invoker, 
			final Class<?> receiver, 
			final String fieldName
	) throws SecurityException {
		return invoker.getStaticField(receiver, fieldName);
	}

	@Override
	public Object onGetInstanceField(
			final IInvoker invoker, 
			final Object receiver, 
			final String fieldName
	) throws SecurityException {
		return invoker.getInstanceField(receiver, fieldName);
	}

	@Override
	public byte[] onLoadClassPathResource(
			final String resourceName
	) throws SecurityException {
		return StringUtil.isBlank(resourceName) 
					? null
					: new ClassPathResource(resourceName).getResourceAsBinary();
	}

	@Override
	public String onReadSystemProperty(
			final String propertyName
	) throws SecurityException {
		return StringUtil.isBlank(propertyName) 
				? null
				: System.getProperty(propertyName);
	}
	
	@Override
	public void validateBlackListedVeniceFunction(
			final String funcName
	) throws SecurityException {
		// ok,  no black listed Venice functions
	}

}
