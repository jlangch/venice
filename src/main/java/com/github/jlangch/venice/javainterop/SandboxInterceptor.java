/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2021 Venice
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


import java.io.File;

import com.github.jlangch.venice.SecurityException;
import com.github.jlangch.venice.impl.sandbox.CompiledSandboxRules;
import com.github.jlangch.venice.impl.util.StringUtil;


public class SandboxInterceptor extends ValueFilterInterceptor {

	public SandboxInterceptor(
			final SandboxRules rules
	) {
		this(rules, LoadPathsFactory.rejectAll());
	}

	public SandboxInterceptor(
			final SandboxRules rules,
			final ILoadPaths loadPaths
	) {
		super(loadPaths);
		
		this.sandboxRulesOrg = rules;
		this.sandboxRules = CompiledSandboxRules.compile(rules);

		this.executionTimeDeadline = getExecutionTimeDeadlineTime();
	}
	
	public SandboxRules getRules() {
		return sandboxRulesOrg;
	}

	@Override
	public ReturnValue onInvokeInstanceMethod(
			final IInvoker invoker, 
			final Object receiver, 
			final Class<?> receiverFormalType,
			final String method, 
			final Object... args
	) throws SecurityException {
		validateClassAccessor(receiverFormalType, method);
		validateObjAccessor(receiver, method);
	
		return super.onInvokeInstanceMethod(invoker, receiver, receiverFormalType, method, args);
	}

	@Override
	public ReturnValue onInvokeStaticMethod(
			final IInvoker invoker, 
			final Class<?> receiver, 
			final String method, 
			final Object... args
	) throws SecurityException {
		validateClassAccessor(receiver, method);

		return super.onInvokeStaticMethod(invoker, receiver, method, args);
	}

	@Override
	public ReturnValue onInvokeConstructor(
			final IInvoker invoker, 
			final Class<?> receiver,
			final Object... args
	) throws SecurityException {
		return super.onInvokeConstructor(invoker, receiver, args);
	}

	@Override
	public ReturnValue onGetBeanProperty(
			final IInvoker invoker, 
			final Object receiver, 
			final String property
	) throws SecurityException {
		validateObjAccessor(receiver, property);
		
		return super.onGetBeanProperty(invoker, receiver, property);
	}

	@Override
	public void onSetBeanProperty(
			final IInvoker invoker, 
			final Object receiver, 
			final String property, 
			final Object value
	) throws SecurityException {
		validateObjAccessor(receiver, property);
		
		super.onSetBeanProperty(invoker, receiver, property, value);
	}

	@Override
	public ReturnValue onGetStaticField(
			final IInvoker invoker, 
			final Class<?> receiver, 
			final String fieldName
	) throws SecurityException {
		validateClassAccessor(receiver, fieldName);
		
		return super.onGetStaticField(invoker, receiver, fieldName);
	}

	@Override
	public ReturnValue onGetInstanceField(
			final IInvoker invoker, 
			final Object receiver, 
			final Class<?> receiverFormalType,
			final String fieldName
	) throws SecurityException {
		validateObjAccessor(receiver, fieldName);
		
		return super.onGetInstanceField(invoker, receiver, receiverFormalType, fieldName);
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
					"%s: Access denied to function %s", 
					PREFIX,
					funcName));
		}
	}

	@Override
	public void validateLoadModule(
			final String moduleName
	) throws SecurityException {
		if (!sandboxRules.isWhiteListedVeniceModule(moduleName)) {
			throw new SecurityException(String.format(
					"%s: Access denied to module %s", 
					PREFIX,
					moduleName));
		}
	}

	@Override
	public void validateMaxExecutionTime() throws SecurityException {
		if (executionTimeDeadline > 0 && System.currentTimeMillis() > executionTimeDeadline) {
			throw new SecurityException(
					"Venice Sandbox: The sandbox exceeded the max execution time");
		}
	}
	
	@Override
	public void validateFileRead(final File file) throws SecurityException {
		if (!getLoadPaths().isOnLoadPath(file)) {
			throw new SecurityException(
					"Venice Sandbox: The sandbox denied reading the file: " + file +
					"! The file is not on the sandbox' load paths.");
		}
	}
	
	@Override
	public void validateFileWrite(final File file) throws SecurityException {
		if (!getLoadPaths().isOnLoadPath(file)) {
			throw new SecurityException(
					"Venice Sandbox: The sandbox denied writing the file: " + file +
					"! The file is not on the sandbox' load paths.");
		}
	}

	@Override
	public Integer getMaxExecutionTimeSeconds() {
		return sandboxRules.getMaxExecTimeSeconds();
	}

	@Override
	public Integer getMaxFutureThreadPoolSize() {
		return sandboxRules.getMaxFutureThreadPoolSize();
	}

	@Override
	protected ReturnValue filterReturnValue(final ReturnValue returnValue) {
		validateClass(returnValue.getFormalType());
		validateObj(returnValue.getValue());
		return returnValue;
	}

	@Override
	protected Object filter(final Object obj) {
		validateObj(obj);
		return obj;
	}

	@Override
	protected Object filterAccessor(final Object o, final String accessor) {
		validateObjAccessor(o, accessor);
		return o;
	}

	
	private void validateClass(final Class<?> clazz) {
		if (clazz != null) {
			if (!sandboxRules.isWhiteListed(clazz)) {
				throw new SecurityException(String.format(
						"%s: Access denied to class %s", 
						PREFIX,
						clazz.getName()));
			}
		}
	}
	
	private void validateObj(final Object obj) {
		if (obj != null) {
			validateClass(getClass(obj));
		}
	}

	private void validateClassAccessor(final Class<?> clazz, final String accessor) {
		if (clazz != null) {
			if (!sandboxRules.isWhiteListed(clazz, accessor)) {
				throw new SecurityException(String.format(
						"%s: Access denied to accessor %s::%s", 
						PREFIX,
						clazz.getName(), 
						accessor));
			}
		}
	}

	private void validateObjAccessor(final Object receiver, final String accessor) {
		if (receiver != null) {
			validateClassAccessor(getClass(receiver), accessor);
		}
	}

	private void validateClasspathResource(final String resourceName) {
		if (!StringUtil.isBlank(resourceName)) {
			if (!sandboxRules.isWhiteListedClasspathResource(resourceName)) {
				throw new SecurityException(String.format(
						"%s: Access denied to classpath resource '%s'", 
						PREFIX,
						resourceName));
			}
		}
	}

	private void validateSystemProperty(final String propertyName) {
		if (!StringUtil.isBlank(propertyName)) {
			if (!sandboxRules.isWhiteListedSystemProperty(propertyName)) {
				throw new SecurityException(String.format(
						"%s: Access denied to system property '%s'", 
						PREFIX,
						propertyName));
			}
		}
	}

	private void validateSystemEnv(final String name) {
		if (!StringUtil.isBlank(name)) {
			if (!sandboxRules.isWhiteListedSystemEnv(name)) {
				throw new SecurityException(String.format(
						"%s: Access denied to system environment variable '%s'",
						PREFIX,
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

	private long getExecutionTimeDeadlineTime() {
		final Integer maxExecTimeSeconds = getMaxExecutionTimeSeconds();
		return maxExecTimeSeconds == null 
					? -1L
					: System.currentTimeMillis() + 1000L * maxExecTimeSeconds.longValue();
	}

	
	private static final String PREFIX = "Venice Sandbox";
	
	private final SandboxRules sandboxRulesOrg;
	private final CompiledSandboxRules sandboxRules;
	
	private final long executionTimeDeadline;
}
