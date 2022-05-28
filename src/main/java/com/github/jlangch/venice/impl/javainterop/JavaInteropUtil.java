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
package com.github.jlangch.venice.impl.javainterop;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.jlangch.venice.JavaMethodInvocationException;
import com.github.jlangch.venice.SecurityException;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.IVncJavaObject;
import com.github.jlangch.venice.impl.types.VncBigDecimal;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncByteBuffer;
import com.github.jlangch.venice.impl.types.VncChar;
import com.github.jlangch.venice.impl.types.VncDouble;
import com.github.jlangch.venice.impl.types.VncInteger;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncHashSet;
import com.github.jlangch.venice.impl.types.collections.VncJavaList;
import com.github.jlangch.venice.impl.types.collections.VncJavaMap;
import com.github.jlangch.venice.impl.types.collections.VncJavaSet;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.concurrent.Agent;
import com.github.jlangch.venice.impl.types.concurrent.Delay;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ErrorMessage;
import com.github.jlangch.venice.impl.util.reflect.ReflectionAccessor;
import com.github.jlangch.venice.impl.util.reflect.ReflectionTypes;
import com.github.jlangch.venice.impl.util.reflect.ReflectionUtil;
import com.github.jlangch.venice.javainterop.IInterceptor;
import com.github.jlangch.venice.javainterop.ReturnValue;


public class JavaInteropUtil {

	public static VncVal applyJavaAccess(final VncList args, final JavaImports javaImports) {
		try {
			final VncVal arg0 = args.first();		
			final VncString method = (VncString)args.second();
			final VncList params = args.slice(2);
			
			final String methodName = method.getValue();
			
			
			if ("new".equals(methodName)) {			
				// call constructor (. :java.util.String :new \"abc\")
				final Class<?> targetClass = toClass(arg0, javaImports);
				final String className = targetClass.getName();
				
				// Delay & Agents exceptionally get the original Venice data types passed!
				final Object[] methodArgs = isDelayOrAgentClass(className) 
												? copyToJavaMethodArgs(params)
												: convertToJavaMethodArgs(params);
							
				if (Exception.class.isAssignableFrom(targetClass)) {
					// bypass sandbox for instantiating exceptions, always allowed
					return JavaInteropUtil.convertToVncVal(
							ReflectionAccessor.invokeConstructor(targetClass, methodArgs));
				}
				else {
					final IInterceptor interceptor = ThreadContext.getInterceptor();

					return JavaInteropUtil.convertToVncVal(
							interceptor.onInvokeConstructor(new Invoker(), targetClass, methodArgs));
				}
			}
			else if ("class".equals(methodName)) {			
				// get class (. :java.util.String :class)
				if (Types.isVncString(arg0)) {
					try {
						return new VncJavaObject(toClass(arg0, javaImports));
					}
					catch(Exception ex) {
						return Constants.Nil;
					}
				}
				else if (Types.isVncJavaObject(arg0, Class.class)) {
					return  new VncJavaObject((Class<?>)((VncJavaObject)arg0).getDelegate());
				}
				else if (Types.isVncJavaObject(arg0)) {
					return new VncJavaObject(((VncJavaObject)arg0).getDelegate().getClass());
				}
				else {
					return new VncJavaObject(arg0.getClass());
				}
			}
			else {
				final IInterceptor interceptor = ThreadContext.getInterceptor();
				
				if (Types.isVncKeyword(arg0) || (Types.isVncJavaObject(arg0, Class.class))) {
					// static method / field:   (. :org.foo.Foo :getLastName)
					final Class<?> targetClass = toClass(arg0, javaImports);
					final String className = targetClass.getName();

					
					// Delay & Agents exceptionally get the original Venice data types passed!
					final Object[] methodArgs = isDelayOrAgentClass(className) 
													? copyToJavaMethodArgs(params)
													: convertToJavaMethodArgs(params);

					if (methodArgs.length > 0 || ReflectionAccessor.isStaticMethod(targetClass, methodName, methodArgs)) {
						// static method
						return JavaInteropUtil.convertToVncVal(
								interceptor.onInvokeStaticMethod(new Invoker(), targetClass, methodName, methodArgs));
					}
					else if (ReflectionAccessor.isStaticField(targetClass, methodName)) {
						// static field
						return JavaInteropUtil.convertToVncVal(
								interceptor.onGetStaticField(new Invoker(), targetClass, methodName));
					}
					else {
						throw new JavaMethodInvocationException(String.format(
								"No matching public static method or field found: '%s' for target '%s'",
								methodName,
								targetClass));
					}
				}
				else {
					// instance method/field:   (. person :getLastName)
					//	                        (. person :setLastName \"john\")
					Object target = null;
					Class<?> targetFormalType = null;
					if (arg0 instanceof IVncJavaObject) {
						target = ((IVncJavaObject)arg0).getDelegate();
						targetFormalType = ((IVncJavaObject)arg0).getDelegateFormalType();
					}
					else {
						target = arg0.convertToJavaObject();
					}
											
					// Delay & Agents exceptionally get the original Venice data types passed!
					final Object[] methodArgs = isDelayOrAgentClass(target) 
													? copyToJavaMethodArgs(params)
													: convertToJavaMethodArgs(params);
	
					boolean isInstanceMethod = 
							methodArgs.length > 0 
								|| (targetFormalType != null && ReflectionAccessor.isInstanceMethod(targetFormalType, methodName, methodArgs))
								|| ReflectionAccessor.isInstanceMethod(target, methodName, methodArgs);
					if (isInstanceMethod) {
						// instance method
						return JavaInteropUtil.convertToVncVal(
								interceptor.onInvokeInstanceMethod(new Invoker(), target, targetFormalType, methodName, methodArgs));
					}
					else if (ReflectionAccessor.isInstanceField(target, methodName)) {
						// instance field
						return JavaInteropUtil.convertToVncVal(
								interceptor.onGetInstanceField(new Invoker(), target, targetFormalType, methodName));
					}
					else {
						throw new JavaMethodInvocationException(String.format(
								"No matching public instance method or field found: '%s' for target '%s'",
								methodName,
								target.getClass()));
					}
				}
			}
		}
		catch(JavaMethodInvocationException ex) {
			Throwable cause = ex.getCause();
			if (cause != null) {
				if (cause instanceof SecurityException) {
					throw new SecurityException(String.format(
							"%s. %s", 
							cause.getMessage(),
							ErrorMessage.buildErrLocation(args)));
				}
				else if (cause instanceof InvocationTargetException) {
					cause = cause.getCause();
					if (cause != null) {
						if (cause instanceof SecurityException) {
							throw new SecurityException(String.format(
									"%s. %s", 
									cause.getMessage(),
									ErrorMessage.buildErrLocation(args)));
						}

						throw new JavaMethodInvocationException(
								String.format(
									"%s.", 
									ex.getMessage()),
								cause);
					}
				}
			}
			
			// else
			throw new JavaMethodInvocationException(
					String.format("%s.", ex.getMessage()));
		}
		catch(SecurityException ex) {
			throw new SecurityException(String.format(
					"%s. %s", 
					ex.getMessage(),
					ErrorMessage.buildErrLocation(args)));
		}
		catch(RuntimeException ex) {
			throw new JavaMethodInvocationException(
						String.format(
							"JavaInterop failure."),
						ex);
		}
	}
	
	public static Class<?> toClass(final VncVal val, final JavaImports javaImports) {
		try {
			if (Types.isVncJavaObject(val, Class.class)) {
				return (Class<?>)((VncJavaObject)val).getDelegate();
			}
			else if (Types.isVncJavaObject(val)) {
				return ((VncJavaObject)val).getDelegate().getClass();
			}
			else {
				final String className = Types.isVncKeyword(val)
											? Coerce.toVncKeyword(val).getValue()
											: Coerce.toVncString(val).getValue();
		
				return ReflectionUtil.classForName(javaImports.resolveClassName(className));
			}
		}
		catch(RuntimeException ex) {
			throw new VncException(ex.getMessage(), ex);
		}
	}
	
	public static VncKeyword toVncKeyword(final Class<?> clazz) {
		return new VncKeyword(clazz.getName());
	}
	
	public static List<VncKeyword> toVncKeywords(final List<Class<?>> classes) {
		return classes.stream().map(c -> toVncKeyword(c)).collect(Collectors.toList());
	}
	
	private static Object[] convertToJavaMethodArgs(final VncList params) {
		final Object[] methodArgs = new Object[params.size()];
		for(int ii=0; ii<params.size(); ii++) {
			methodArgs[ii] = params.nth(ii).convertToJavaObject();
		}
		return methodArgs;
	}

	private static Object[] copyToJavaMethodArgs(final VncList params) {
		final Object[] methodArgs = new Object[params.size()];
		for(int ii=0; ii<params.size(); ii++) {
			methodArgs[ii] = params.nth(ii);
		}
		return methodArgs;
	}
	
	public static VncVal convertToVncVal(final Object value) {
		return convertToVncVal(value, null, false);
	}

	@SuppressWarnings("unchecked")
	private static VncVal convertToVncVal(
			final Object value, 
			final Class<?> formalType,
			final boolean recursive
	) {
		if (value == null) {
			return Constants.Nil;
		}
		else if (value instanceof ReturnValue) {
			return convertToVncVal(
						((ReturnValue)value).getValue(),
						((ReturnValue)value).getFormalType(),
						recursive);
		}
		else if (value instanceof VncVal) {
			return (VncVal)value;
		}
		else if (value instanceof String) {
			return new VncString((String)value);
		}
		else if (value instanceof Character) {
			return new VncChar((Character)value);
		}
		else if (value instanceof Number) {
			if (value instanceof Integer) {
				return new VncInteger((Integer)value);
			}
			else if (value instanceof Long) {
				return new VncLong((Long)value);
			}
			else if (value instanceof Float) {
				return new VncDouble((Float)value);
			}
			else if (value instanceof Double) {
				return new VncDouble((Double)value);
			}
			else if (value instanceof BigDecimal) {
				return new VncBigDecimal((BigDecimal)value);
			}
			else if (value instanceof Byte) {
				return new VncLong((((Byte)value).byteValue() & 0xFF));
			}
			else if (value instanceof Short) {
				return new VncLong(((Short)value).longValue());
			}
			else { 
				return new VncJavaObject(value);
			}
		}
		else if (value instanceof Boolean) {
			return VncBoolean.of(((Boolean)value).booleanValue());
		}
		else if (ReflectionTypes.isEnumType(value.getClass())) {
			return new VncString(value.toString());
		}
		else if (value instanceof List) {
			if (recursive) {
				final List<VncVal> list = new ArrayList<>();
				for(Object o : (List<Object>)value) {
					list.add(convertToVncVal(o, null, recursive));
				}
				return VncList.ofList(list);
			}
			else {
				return new VncJavaList((List<Object>)value);
			}
		}
		else if (value instanceof Set) {
			if (recursive) {
				final Set<VncVal> set = new HashSet<>();
				for(Object o : (Set<Object>)value) {
					set.add(convertToVncVal(o, null, recursive));
				}
				return VncHashSet.ofAll(set);
			}
			else {
				return new VncJavaSet((Set<Object>)value);
			}
		}
		else if (value instanceof Map) {
			if (recursive) {
				final HashMap<VncVal,VncVal> map = new HashMap<>();
				for(Map.Entry<Object, Object> o : ((Map<Object,Object>)value).entrySet()) {
					map.put(
						convertToVncVal(o.getKey(), null, recursive),
						convertToVncVal(o.getValue(), null, recursive));
				}
				return new VncHashMap(map);
			}
			else {
				return new VncJavaMap((Map<Object,Object>)value);
			}
		}
		else if (value instanceof ByteBuffer) {
			return new VncByteBuffer((ByteBuffer)value);
		}
		else if (ReflectionTypes.isArrayType(value.getClass())) {
			final Class<?> componentType = value.getClass().getComponentType();					
			if (componentType == byte.class) {
				return new VncByteBuffer(ByteBuffer.wrap((byte[])value));
			}
			else {
				final List<VncVal> vec = new ArrayList<>();
				for(int ii=0; ii<Array.getLength(value); ii++) {
					vec.add(convertToVncVal(Array.get(value, ii)));
				}
				return VncVector.ofList(vec);
			}
		}
		else if (value instanceof Class) {
			return new VncString(((Class<?>)value).getName());
		}
		else { 
			return formalType == null
						? new VncJavaObject(value)
						: VncJavaObject.from(value, formalType);
		}
	}

	private static boolean isDelayOrAgentClass(final String className) {
		return (className.equals(Delay.class.getName()) || className.equals(Agent.class.getName()));
	}
	
	private static boolean isDelayOrAgentClass(final Object target) {
		return target instanceof Delay || target instanceof Agent;
	}
}
