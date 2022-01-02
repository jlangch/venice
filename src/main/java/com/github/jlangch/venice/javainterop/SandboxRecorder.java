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

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.github.jlangch.venice.SecurityException;


public class SandboxRecorder extends Interceptor {

	
	public SandboxRecorder() {
		this(null);
	}
	
	public SandboxRecorder(final ILoadPaths loadPaths) {
		super(loadPaths);
		this.writer = new PrintWriter(System.out);
	}

	public SandboxRecorder(
			final Writer writer,
			final ILoadPaths loadPaths
	) {
		super(loadPaths);
		this.writer = new PrintWriter(writer);
	}
	
	public SandboxRecorder(
			final OutputStream os,
			final ILoadPaths loadPaths
	) {
		super(loadPaths);
		this.writer = new PrintWriter(os);
	}
	


	@Override
	public ReturnValue onInvokeInstanceMethod(
			final IInvoker invoker, 
			final Object receiver, 
			final Class<?> receiverFormalType,
			final String method, 
			final Object... args
	) throws SecurityException {
		format("%s:%s(%s)", type(receiver), method, arguments(args));
		return super.onInvokeInstanceMethod(invoker, receiver, receiverFormalType, method, args);
	}

	@Override
	public ReturnValue onInvokeStaticMethod(
			final IInvoker invoker, 
			final Class<?> receiver, 
			final String method, 
			final Object... args
	) throws SecurityException {
		format("%s:%s(%s)", type(receiver), method, arguments(args));
		return super.onInvokeStaticMethod(invoker, receiver, method, args);
	}

	@Override
	public ReturnValue onInvokeConstructor(
			final IInvoker invoker, 
			final Class<?> receiver, 
			final Object... args
	) throws SecurityException {
		format("new %s(%s)", type(receiver), arguments(args));
		return super.onInvokeConstructor(invoker, receiver, args);
	}

	@Override
	public ReturnValue onGetBeanProperty(
			final IInvoker invoker, 
			final Object receiver, 
			final String property
	) throws SecurityException {
		format("%s.!%s", type(receiver), property);
		return super.onGetBeanProperty(invoker, receiver,property);
	}

	@Override
	public void onSetBeanProperty(
			final IInvoker invoker, 
			final Object receiver, 
			final String property, 
			final Object value
	) throws SecurityException {
		format("%s.!%s=%s", type(receiver), property, type(value));
		super.onSetBeanProperty(invoker, receiver, property, value);
	}

	@Override
	public ReturnValue onGetStaticField(
			final IInvoker invoker, 
			final Class<?> receiver, 
			final String fieldName
	) throws SecurityException {
		format("%s.@%s", type(receiver), fieldName);
		return super.onGetStaticField(invoker, receiver, fieldName);
	}

	@Override
	public ReturnValue onGetInstanceField(
			final IInvoker invoker, 
			final Object receiver, 
			final Class<?> receiverFormalType,
			final String fieldName
	) throws SecurityException {
		format("%s.%s", type(receiver), fieldName);
		return super.onGetInstanceField(invoker, receiver, receiverFormalType, fieldName);
	}

	@Override
	public byte[] onLoadClassPathResource(
			final String resourceName
	) throws SecurityException {
		format("classpath:%s", resourceName);
		return super.onLoadClassPathResource(resourceName);
	}

	@Override
	public String onReadSystemProperty(
			final String propertyName
	) throws SecurityException {
		format("system.property:%s", propertyName);
		return super.onReadSystemProperty(propertyName);
	}
	
	@Override
	public String onReadSystemEnv(
			final String name
	) throws SecurityException {
		format("system.env:%s", name);
		return super.onReadSystemEnv(name);
	}
	
	private void format(final String fmt, final Object ... args) {
		writer.println(String.format(fmt,args));
		writer.flush();
	}
	
	private String type(final Object obj) {
		return obj == null 
				 ? "null" 
				 : (isClass(obj) ? type((Class<?>)obj) : type(obj.getClass()));
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

	private boolean isClass(final Object obj) {
		return obj instanceof Class;
	}


	private final PrintWriter writer;
}
