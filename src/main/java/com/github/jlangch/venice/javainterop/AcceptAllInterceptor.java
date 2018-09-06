/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2018 Venice
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

public class AcceptAllInterceptor extends JavaValueFilterInterceptor {
	
	public AcceptAllInterceptor() {
	}
	

	@Override
	public Object onInvokeInstanceMethod(
			final IInvoker invoker, 
			final Object receiver, 
			final String method, 
			final Object... args
	) {
		return super.onInvokeInstanceMethod(invoker, receiver, method, args);
	}

	@Override
	public Object onInvokeStaticMethod(
			final IInvoker invoker, 
			final Class<?> receiver, 
			final String method, 
			final Object... args
	) {
		return super.onInvokeStaticMethod(invoker, receiver, method, args);
	}

	@Override
	public Object onInvokeConstructor(
			final IInvoker invoker, 
			final Class<?> receiver,
			final Object... args
	) {
		return super.onInvokeConstructor(invoker, receiver, args);
	}

	@Override
	public Object onGetBeanProperty(
			final IInvoker invoker, 
			final Object receiver, 
			final String property
	) {
		return super.onGetBeanProperty(invoker, receiver, property);
	}

	@Override
	public void onSetBeanProperty(
			final IInvoker invoker, 
			final Object receiver, 
			final String property, 
			final Object value
	) {
		super.onSetBeanProperty(invoker, receiver, property, value);
	}

	@Override
	public Object onGetStaticField(
			final IInvoker invoker, 
			final Class<?> receiver, 
			final String fieldName
	) {
		return super.onGetStaticField(invoker, receiver, fieldName);
	}

	@Override
	public Object onGetInstanceField(
			final IInvoker invoker, 
			final Object receiver, 
			final String fieldName
	) {
		return super.onGetInstanceField(invoker, receiver, fieldName);
	}

	@Override
	public byte[] onLoadClassPathResource(final String resourceName) {
		return super.onLoadClassPathResource(resourceName);
	}

	@Override
	public String onReadSystemProperty(final String propertyName) {
		return super.onReadSystemProperty(propertyName);
	}

	@Override
	public void validateBlackListedVeniceFunction(
			final String funcName, 
			final VncList args
	) {
		super.validateBlackListedVeniceFunction(funcName, args);
	}
}
