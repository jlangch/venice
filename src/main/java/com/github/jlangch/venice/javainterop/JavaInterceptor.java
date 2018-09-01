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


public abstract class JavaInterceptor {
 
	public Object onInvokeInstanceMethod(
			final IInvoker invoker, 
			final Object receiver, 
			final String method, 
			final Object... args
	) {
		return invoker.callInstanceMethod(receiver, method, args);
	}

	public Object onInvokeStaticMethod(
			final IInvoker invoker, 
			final Class<?> receiver, 
			final String method, 
			final Object... args
	) {
		return invoker.callStaticMethod(receiver, method, args);
	}

	public Object onInvokeConstructor(
			final IInvoker invoker, 
			final Class<?> receiver, 
			final Object... args
	) {
		return invoker.callConstructor(receiver, args);
	}

	public Object onGetBeanProperty(
			final IInvoker invoker, 
			final Object receiver, 
			final String property
	) {
		return invoker.getBeanProperty(receiver, property);
	}

	public Object onSetBeanProperty(
			final IInvoker invoker, 
			final Object receiver, 
			final String property, 
			final Object value
	) {
		return invoker.setBeanProperty(receiver, property, value);
	}

	public Object onGetStaticField(
			final IInvoker invoker, 
			final Class<?> receiver, 
			final String fieldName
	) {
		return invoker.getStaticField(receiver, fieldName);
	}

	public Object onGetInstanceField(
			final IInvoker invoker, 
			final Object receiver, 
			final String fieldName
	) {
		return invoker.getInstanceField(receiver, fieldName);
	}
	

	public void checkBlackListedVeniceFunction(
			final String funcName, 
			final VncList args
	) {
		// ok,  no black listed Venice functions
	}
	
	public void checkWhiteListedSystemProperty(final String property) {
		// ok, all system properties white-listed
	}

}
