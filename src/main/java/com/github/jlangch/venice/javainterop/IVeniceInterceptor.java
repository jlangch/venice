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


public interface IVeniceInterceptor {
 
	Object onInvokeInstanceMethod(
			IInvoker invoker, 
			Object receiver, 
			String method, 
			Object... args);

	Object onInvokeStaticMethod(
			IInvoker invoker, 
			Class<?> receiver, 
			String method, 
			Object... args);

	Object onInvokeConstructor(
			IInvoker invoker, 
			Class<?> receiver, 
			Object... args);

	Object onGetBeanProperty(
			IInvoker invoker, 
			Object receiver, 
			String property);

	Object onSetBeanProperty(
			IInvoker invoker, 
			Object receiver, 
			String property, 
			Object value);

	Object onGetStaticField(
			IInvoker invoker, 
			Class<?> receiver, 
			String fieldName);

	Object onGetInstanceField(
			IInvoker invoker, 
			Object receiver, 
			String fieldName);

	byte[] onLoadClassPathResource(String resourceName);

	String onReadSystemProperty(String propertyName);
	
	void checkBlackListedVeniceFunction(
			String funcName, 
			VncList args);

}
