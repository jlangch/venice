/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2019 Venice
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

import com.github.jlangch.venice.impl.javainterop.CompiledSandboxRules;
import com.github.jlangch.venice.impl.util.StringUtil;


public class SandboxInterceptor extends ValueFilterInterceptor {
	
	public SandboxInterceptor(final SandboxRules rules) {
		this.sandboxRulesOrg = rules;
		this.sandboxRules = CompiledSandboxRules.compile(rules);
	}
	
	public SandboxRules getRules() {
		return sandboxRulesOrg;
	}

	@Override
	public Object onInvokeInstanceMethod(
			final IInvoker invoker, 
			final Object receiver, 
			final String method, 
			final Object... args
	) throws SecurityException {
		validateAccessor(receiver, method);
	
		return super.onInvokeInstanceMethod(invoker, receiver, method, args);
	}

	@Override
	public Object onInvokeStaticMethod(
			final IInvoker invoker, 
			final Class<?> receiver, 
			final String method, 
			final Object... args
	) throws SecurityException {
		validateAccessor(receiver, method);

		return super.onInvokeStaticMethod(invoker, receiver, method, args);
	}

	@Override
	public Object onInvokeConstructor(
			final IInvoker invoker, 
			final Class<?> receiver,
			final Object... args
	) throws SecurityException {
		return super.onInvokeConstructor(invoker, receiver, args);
	}

	@Override
	public Object onGetBeanProperty(
			final IInvoker invoker, 
			final Object receiver, 
			final String property
	) throws SecurityException {
		validateAccessor(receiver, property);
		
		return super.onGetBeanProperty(invoker, receiver, property);
	}

	@Override
	public void onSetBeanProperty(
			final IInvoker invoker, 
			final Object receiver, 
			final String property, 
			final Object value
	) throws SecurityException {
		validateAccessor(receiver, property);
		
		super.onSetBeanProperty(invoker, receiver, property, value);
	}

	@Override
	public Object onGetStaticField(
			final IInvoker invoker, 
			final Class<?> receiver, 
			final String fieldName
	) throws SecurityException {
		validateAccessor(receiver, fieldName);
		
		return super.onGetStaticField(invoker, receiver, fieldName);
	}

	@Override
	public Object onGetInstanceField(
			final IInvoker invoker, 
			final Object receiver, 
			final String fieldName
	) throws SecurityException {
		validateAccessor(receiver, fieldName);
		
		return super.onGetInstanceField(invoker, receiver, fieldName);
	}

	@Override
	public byte[] onLoadClassPathResource(
			final String resourceName
	) throws SecurityException {
		validateClasspathResource(resourceName);
		
		return super.onLoadClassPathResource(resourceName);
	}

	@Override
	public String onReadSystemProperty(
			final String propertyName
	) throws SecurityException {
		validateSystemProperty(propertyName);
		
		return super.onReadSystemProperty(propertyName);
	}

	@Override
	public String onReadSystemEnv(
			final String name
	) throws SecurityException {
		validateSystemEnv(name);
		
		return super.onReadSystemEnv(name);
	}

	@Override
	public void validateVeniceFunction(
			final String funcName
	) throws SecurityException {
		if (sandboxRules.isBlackListedVeniceFunction(funcName)) {
			throw new SecurityException(String.format(
					"Venice Sandbox: Access denied to function %s", 
					funcName));
		}
	}

	@Override
	public Integer getMaxExecutionTimeSeconds() {
		return sandboxRules.getMaxExecTimeSeconds();
	}

	
	@Override
	protected Object filter(final Object obj) {
		validateClass(obj);
		return obj;
	}

	@Override
	protected Object filterAccessor(final Object o, final String accessor) {
		validateAccessor(o, accessor);
		return o;
	}

	
	private void validateClass(final Object obj) {
		if (obj != null) {
			final Class<?> clazz= getClass(obj);

			if (!sandboxRules.isWhiteListed(clazz)) {
				throw new SecurityException(String.format(
						"Venice Sandbox: Access denied to class %s", 
						clazz.getName()));
			}
		}
	}

	private void validateAccessor(final Object receiver, final String accessor) {
		if (receiver != null) {
			final Class<?> clazz= getClass(receiver);
			if (!sandboxRules.isWhiteListed(clazz, accessor)) {
				throw new SecurityException(String.format(
						"Venice Sandbox: Access denied to accessor %s::%s", 
						clazz.getName(), accessor));
			}
		}
	}

	private void validateClasspathResource(final String resourceName) {
		if (!StringUtil.isBlank(resourceName)) {
			if (!sandboxRules.isWhiteListedClasspathResource(resourceName)) {
				throw new SecurityException(String.format(
						"Venice Sandbox: Access denied to classpath resource '%s'", 
						resourceName));
			}
		}
	}

	private void validateSystemProperty(final String propertyName) {
		if (!StringUtil.isBlank(propertyName)) {
			if (!sandboxRules.isWhiteListedSystemProperty(propertyName)) {
				throw new SecurityException(String.format(
						"Venice Sandbox: Access denied to system property '%s'", 
						propertyName));
			}
		}
	}

	private void validateSystemEnv(final String name) {
		if (!StringUtil.isBlank(name)) {
			if (!sandboxRules.isWhiteListedSystemEnv(name)) {
				throw new SecurityException(String.format(
						"Venice Sandbox: Access denied to system environment variable '%s'", 
						name));
			}
		}
	}
	
	private Class<?> getClass(final Object obj) {
		if (obj != null) {
			return obj instanceof Class<?> ? (Class<?>)obj : obj.getClass();
		}
		else {
			return null;
		}
	}
	
	
	private final SandboxRules sandboxRulesOrg;
	private final CompiledSandboxRules sandboxRules;
}
