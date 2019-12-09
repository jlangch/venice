/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2020 Venice
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
package com.github.jlangch.venice.impl.javainterop;

import com.github.jlangch.venice.impl.util.reflect.ReflectionAccessor;
import com.github.jlangch.venice.javainterop.IInvoker;


public class Invoker implements IInvoker{

	@Override
	public Object callInstanceMethod(Object receiver, String method, Object... args) {
		return ReflectionAccessor.invokeInstanceMethod(receiver, method, args);
	}

	@Override
	public Object callStaticMethod(Class<?> receiver, String method, Object... args) {
		return ReflectionAccessor.invokeStaticMethod(receiver, method, args);
	}

	@Override
	public Object callConstructor(Class<?> receiver, Object... args) {
		return ReflectionAccessor.invokeConstructor(receiver, args);
	}

	@Override
	public Object getBeanProperty(Object receiver, String property) {
		return ReflectionAccessor.getBeanProperty(receiver, property);
	}

	@Override
	public Object setBeanProperty(Object receiver, String property, Object value) {
		ReflectionAccessor.setBeanProperty(receiver, property, value);
		return null;
	}

	@Override
	public Object getStaticField(Class<?> receiver, String fieldName) {
		return ReflectionAccessor.getStaticField(receiver, fieldName);
	}

	@Override
	public Object getInstanceField(Object receiver, String fieldName) {
		return ReflectionAccessor.getInstanceField(receiver, fieldName);
	}

}
