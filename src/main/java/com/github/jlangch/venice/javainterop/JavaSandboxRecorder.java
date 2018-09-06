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

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.stream.Collectors;


public class JavaSandboxRecorder extends JavaInterceptor {
	
	public JavaSandboxRecorder(final Writer writer) {
		this.writer = new PrintWriter(writer);
	}
	
	public JavaSandboxRecorder(final OutputStream os) {
		this.writer = new PrintWriter(os);
	}
	
	public JavaSandboxRecorder() {
		this.writer = new PrintWriter(System.out);
	}
	


	public Object onInvokeInstanceMethod(final IInvoker invoker, final Object receiver, final String method, final Object... args) {
		format("%s:%s(%s)", type(receiver), method, arguments(args));
		return super.onInvokeInstanceMethod(invoker, receiver, method, args);
	}

	public Object onInvokeStaticMethod(final IInvoker invoker, final Class<?> receiver, final String method, final Object... args) {
		format("%s:%s(%s)", type(receiver), method, arguments(args));
		return super.onInvokeStaticMethod(invoker, receiver, method, args);
	}

	public Object onInvokeConstructor(final IInvoker invoker, final Class<?> receiver, final Object... args) {
		format("new %s(%s)", type(receiver), arguments(args));
		return super.onInvokeConstructor(invoker, receiver, args);
	}

	public Object onGetBeanProperty(final IInvoker invoker, final Object receiver, final String property) {
		format("%s.!%s", type(receiver), property);
		return super.onGetBeanProperty(invoker, receiver,property);
	}

	public Object onSetBeanProperty(final IInvoker invoker, final Object receiver, final String property, final Object value) {
		format("%s.!%s=%s", type(receiver), property, type(value));
		return super.onSetBeanProperty(invoker, receiver, property, value);
	}

	public Object onGetStaticField(final IInvoker invoker, final Class<?> receiver, final String fieldName) {
		format("%s.@%s", type(receiver), fieldName);
		return super.onGetStaticField(invoker, receiver, fieldName);
	}

	public Object onGetInstanceField(final IInvoker invoker, final Object receiver, final String fieldName) {
		format("%s.%s", type(receiver), fieldName);
		return super.onGetInstanceField(invoker, receiver, fieldName);
	}

	public byte[] onLoadClassPathResource(final String resourceName) {
		format("classpath:%s", resourceName);
		return super.onLoadClassPathResource(resourceName);
	}

	public String onReadSystemProperty(final String propertyName) {
		format("system.property:propertyName", propertyName);
		return super.onReadSystemProperty(propertyName);
	}
	
	
	
	private void format(final String fmt, final Object ... args) {
		writer.println(String.format(fmt,args));
		writer.flush();
	}
	
	private String type(final Object o) {
		return o == null 
				 ? "null" 
				 : (isClass(o) ? type((Class<?>)o) : type(o.getClass()));
	}
	
	private String type(final Class<?> c) {
		if (c.isArray()) {
			return type(c.getComponentType()) + "[]";
		}
		else {
			final String className = c.getName();
			
			return className.startsWith("java.lang.")
					? className.substring("java.lang.".length())
					: className;
		}
	}
	
	private String arguments(final Object... args) {
		return Arrays.stream(args)
					 .map(a -> type(a))
					 .collect(Collectors.joining(","));
	}

	private boolean isClass(final Object o) {
		return o instanceof Class;
	}

 
	private final PrintWriter writer;

}
