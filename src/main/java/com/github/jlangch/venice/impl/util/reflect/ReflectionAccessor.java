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
package com.github.jlangch.venice.impl.util.reflect;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.jlangch.venice.JavaMethodInvocationException;
import com.github.jlangch.venice.impl.util.Tuple2;
import com.github.jlangch.venice.impl.util.Tuple4;


public class ReflectionAccessor {

	public static void enableCache(final boolean enable) {
		clearCache();
		cachingEnabled.set(enable);
	}

	public static boolean isCacheEnabled() {
		return cachingEnabled.get();
	}

	public static void clearCache() {
		classCache.clear();
		getterMethodCache.clear();
		setterMethodCache.clear();
		constructorCache.clear();
		staticFieldCache.clear();
		instanceFieldCache.clear();
		instanceMethodCache.clear();
		staticMethodCache.clear();
	}

	public static Class<?> classForName(final String className) {
		try {
			return memoizedClassForName(className);
		}
		catch (JavaMethodInvocationException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new JavaMethodInvocationException(
					String.format("Failed to get class '%s'", className),
					ex);
		}
	}

	public static Class<?> classExists(final String className) {
		try {
			return memoizedClassForName(className);
		}
		catch (Exception ex) {
			return null;
		}
	}

	public static Object invokeConstructor(final Class<?> clazz, final Object[] args) {
		try {
			final List<Constructor<?>> ctors = memoizedPublicConstructors(clazz, args.length);
			if (ctors.isEmpty()) {
				throw new JavaMethodInvocationException(noMatchingMConstructorErrMsg(clazz));
			} 
			else if (ctors.size() == 1) {
				final Constructor<?> ctor = (Constructor<?>)ctors.get(0);
				return ctor.newInstance(boxArgs(ctor.getParameterTypes(), args));
			} 
			else {
				// overloaded
							
				// try exact match first
				for(Constructor<?> ctor : ctors) {
					final Class<?>[] params = ctor.getParameterTypes();
					
					if (isCongruent(params, args, true, ctor.isVarArgs())) {
						Object[] boxedArgs = boxArgs(params, args);
						return ctor.newInstance(boxedArgs);
					}
				}

				// try widened match second
				for(Constructor<?> ctor : ctors) {
					final Class<?>[] params = ctor.getParameterTypes();
										
					if (isCongruent(params, args, false, ctor.isVarArgs())) {
						Object[] boxedArgs = boxArgs(params, args);
						return ctor.newInstance(boxedArgs);
					}
				}

				throw new JavaMethodInvocationException(noMatchingMConstructorErrMsg(clazz));
			}
		} 
		catch (JavaMethodInvocationException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new JavaMethodInvocationException(
					String.format("Failed to invoke constructor '%s'", clazz.getName()),
					ex);
		}
	}

	public static Object invokeInstanceMethod(
			final Object target,
			final String methodName, 
			final Object[] args
	) {
		try {
			final Class<?> clazz = target.getClass();
			final List<Method> methods = memoizedInstanceMethod(clazz, methodName, args.length, true);
			return invokeMatchingMethod(methodName, methods, target, args);
		}
		catch (JavaMethodInvocationException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new JavaMethodInvocationException(
					String.format(
							"Failed to invoke instance method '%s' on target '%s'",
							methodName,
							target == null ? "<null>" : target.getClass().getName()),
					ex);
		}
	}

	public static Object invokeStaticMethod(
			final String className, 
			final String methodName, 
			final Object[] args
	) {
		try {
			final Class<?> clazz = classForName(className);
			return invokeStaticMethod(clazz, methodName, args);
		}
		catch (JavaMethodInvocationException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new JavaMethodInvocationException(
					String.format(
							"Failed to invoke static method '%s' on class '%s'",
							methodName,
							className),
					ex);
		}
	}

	public static Object invokeStaticMethod(final Class<?> clazz, final String methodName, final Object[] args) {
		if (methodName.equals("new")) {
			return invokeConstructor(clazz, args);
		}
		else {
			try {
				final List<Method> methods = memoizedStaticMethod(clazz, methodName, args.length, true);
	
				return invokeMatchingMethod(methodName, methods, null, args);
			}
			catch (JavaMethodInvocationException ex) {
				throw ex;
			}
			catch (Exception ex) {
				throw new JavaMethodInvocationException(
						String.format(
								"Failed to invoke static method '%s' on class '%s'",
								methodName,
								clazz.getName()),
						ex);
			}
		}
	}

	public static Object getStaticField(final String className, final String fieldName) {
		try {
			final Class<?> clazz = classForName(className);
			return getStaticField(clazz, fieldName);
		}
		catch (JavaMethodInvocationException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new JavaMethodInvocationException(
					String.format(
							"Failed to get static field '%s' on class '%s'",
							fieldName,
							className),
					ex);
		}
	}

	public static Object getStaticField(final Class<?> clazz, String fieldName) {
		try {
			final Field f = memoizedStaticField(clazz, fieldName);
			if (f != null) {
				return f.get(null);
			}
			else {
				throw new JavaMethodInvocationException(noMatchingFieldErrMsg(fieldName, clazz.getName()));
			}
		}
		catch (JavaMethodInvocationException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new JavaMethodInvocationException(
					String.format(
							"Failed to get static field '%s' on class '%s'",
							fieldName,
							clazz.getName()),
					ex);
		}
	}

	public static Object getInstanceField(final Object target, final String fieldName) {
		try {
			final Class<?> clazz = target.getClass();
			final Field f = memoizedInstanceField(clazz, fieldName);
			if (f != null) {
				return f.get(target);
			}
			else {
				throw new JavaMethodInvocationException(noMatchingFieldErrMsg(fieldName, clazz.getName()));
			}
		}
		catch (JavaMethodInvocationException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new JavaMethodInvocationException(
					String.format(
							"Failed to get instance field '%s' on target '%s'",
							fieldName,
							target.getClass().getName()),
					ex);
		}
	}

	public static List<String> getBeanGetterProperties(final Object target) {
		return memoizedBeanGetterProperties(target.getClass());
	}

	public static List<String> getBeanSetterProperties(final Object target) {
		return memoizedBeanSetterProperties(target.getClass());
	}
			
	public static Object getBeanProperty(
			final Object target, 
			final String propertyName
	) {	
		final Method method = memoizedBeanGetterMethod(target.getClass(), propertyName);

		if (method == null) {
			throw new JavaMethodInvocationException(
					String.format(
							"No bean get property '%s' on target '%s'",
							propertyName,
							target.getClass().getName()));
		}
		else {
			try {
				return method.invoke(target);
			}
			catch(Exception ex) {
				throw new JavaMethodInvocationException(
						String.format(
								"Failed to get bean property '%s' on target '%s'",
								propertyName,
								target.getClass().getName()),
						ex);
			}
		}
	}

	public static void setBeanProperty(
			final Object target, 
			final String propertyName, 
			final Object value
	) {
		final Method method = memoizedBeanSetterMethod(target.getClass(), propertyName);

		if (method == null) {
			throw new JavaMethodInvocationException(
					String.format(
							"No bean set property '%s' on target '%s'",
							propertyName,
							target.getClass().getName()));
		}
		else {
			try {
				final Class<?> type = method.getParameterTypes()[0];
				method.invoke(target, new Object[] { boxArg(type, value) });
			}
			catch(Exception ex) {
				throw new JavaMethodInvocationException(
						String.format(
								"Failed to set bean property '%s' on target '%s'",
								propertyName,
								target.getClass().getName()),
						ex);
			}
		}
	}


	public static boolean isStaticMethod(
			final Class<?> clazz,
			final String methodName, 
			final Object[] args
	) {
		try {
			final List<Method> methods = memoizedStaticMethod(clazz, methodName, args.length, true);
			return !methods.isEmpty();
		}
		catch (JavaMethodInvocationException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new JavaMethodInvocationException(
					String.format(
							"Failed check for available static method '%s' on class '%s'",
							methodName,
							clazz.getName()),
					ex);
		}
	}

	public static boolean isInstanceMethod(
			final Object target,
			final String methodName, 
			final Object[] args
	) {
		try {
			final Class<?> clazz = target.getClass();
			final List<Method> methods = memoizedInstanceMethod(clazz, methodName, args.length, true);
			return !methods.isEmpty();
		}
		catch (JavaMethodInvocationException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new JavaMethodInvocationException(
					String.format(
							"Failed to check for available instance method '%s' on target '%s'",
							methodName,
							target == null ? "<null>" : target.getClass().getName()),
					ex);
		}
	}

	public static boolean isStaticField(final Class<?> clazz, final String fieldName) {
		try {
			final Field f = memoizedStaticField(clazz, fieldName);
			return f != null;
		}
		catch (JavaMethodInvocationException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new JavaMethodInvocationException(
					String.format(
							"Failed to check for available static field '%s' on target '%s'",
							fieldName,
							clazz.getName()),
					ex);
		}
	}

	public static boolean isInstanceField(final Object target, final String fieldName) {
		try {
			final Class<?> clazz = target.getClass();
			final Field f = memoizedInstanceField(clazz, fieldName);
			return f != null;
		}
		catch (JavaMethodInvocationException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new JavaMethodInvocationException(
					String.format(
							"Failed to check for available instance field '%s' on target '%s'",
							fieldName,
							target.getClass().getName()),
					ex);
		}
	}

	private static Object invokeMatchingMethod(
			final String methodName, 
			final List<Method> methods, 
			final Object target,
			final Object[] args
	) {
		if (methods.isEmpty()) {
			throw new JavaMethodInvocationException(noMatchingMethodErrMsg(methodName, target, args));
		} 
		else if (methods.size() == 1) {
			final Method m = (Method)methods.get(0);
			final Object[] boxedArgs = boxArgs(m.getParameterTypes(), args);
			return invoke(m, target, boxedArgs);
		} 
		else { 
			// overloaded
			
			// try exact match first
			for (Method m : methods) {
				final Class<?>[] params = m.getParameterTypes();
				
				if (isCongruent(params, args, true, m.isVarArgs())) {
					final Object[] boxedArgs = boxArgs(params, args);
					return invoke(m, target, boxedArgs);
				}
			}

			// try widened match second
			for (Method m : methods) {
				final Class<?>[] params = m.getParameterTypes();
				
				if (isCongruent(params, args, false, m.isVarArgs())) {
					final Object[] boxedArgs = boxArgs(params, args);
					return invoke(m, target, boxedArgs);
				}
			}
		}
		
		throw new JavaMethodInvocationException(noMatchingMethodErrMsg(methodName, target, args));
	}
	
	private static Object invoke(final Method method, final Object target, final Object[] args) {
		try {
			if (method.getDeclaringClass().getName().equals("java.util.stream.ReferencePipeline")) {
				// ReferencePipeline is not a public class, hence its methods can not be invoked
				// by reflection.
				return invokeStreamMethod(method.getName(), target, args);
			}
			else {
				return method.invoke(target, args);
			}
		} 
		catch (Exception ex) {
			throw new JavaMethodInvocationException(
					target == null
						? String.format(
							"Failed to invoke static method %s",
							method.getName())
						: String.format(
							"Failed to invoke method %s on target %s",
							method.getName(),
							target.getClass().getName()),
					ex);
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Object invokeStreamMethod(final String methodName, final Object target, final Object[] args) {
		final Stream<Object> stream = (Stream<Object>)target;
		
		switch(methodName) {
			case "allMatch":
				return stream.allMatch((Predicate<Object>)args[0]);
			case "anyMatch":
				return stream.anyMatch((Predicate<Object>)args[0]);
			case "collect":
				switch(args.length) {
					case 1:
						return stream.collect((Collector<Object,Object,Object>)args[0]);
					case 3:
						return stream.collect(
										(Supplier<Object>)args[0],
										(BiConsumer<Object,Object>)args[1],
										(BiConsumer<Object,Object>)args[2]);
					default:
						throw new JavaMethodInvocationException(
									"Unsupported stream method 'collect' with " + args.length + " parameters");
				}
			case "distinct":
				return stream.distinct();
			case "filter":
				return stream.filter((Predicate<Object>)args[0]);
			case "findAny":
				return stream.findAny();
			case "findFirst":
				return stream.findFirst();
			case "limit":
				return stream.limit((Long)args[0]);
			case "map":
				return stream.map((Function<Object,Object>)args[0]);
			case "mapToDouble":
				return stream.mapToDouble((ToDoubleFunction<Object>)args[0]);
			case "mapToInt":
				return stream.mapToInt((ToIntFunction<Object>)args[0]);
			case "mapToLong":
				return stream.mapToLong((ToLongFunction<Object>)args[0]);
			case "max":
				return stream.max((Comparator<Object>)args[0]);
			case "min":
				return stream.min((Comparator<Object>)args[0]);
			case "noneMatch":
				return stream.noneMatch((Predicate<Object>)args[0]);
			case "reduce":
				switch(args.length) {
					case 1:
						return stream.reduce((BinaryOperator)args[0]);
					case 2:
						return stream.reduce(args[0], (BinaryOperator)args[1]);
					case 3:
						return stream.reduce(
										args[0],
										(BiFunction<Object,Object,Object>)args[1],
										(BinaryOperator<Object>)args[2]);
					default:
						throw new JavaMethodInvocationException(
									"Unsupported stream method 'reduce' with " + args.length + " parameters");
				}
			case "sorted":
				return args.length == 0
						? stream.sorted()
						: stream.sorted((Comparator<Object>)args[0]);
			case "skip":
				return stream.skip((Long)args[0]);
			default:
				throw new JavaMethodInvocationException(
							"Unsupported stream method '" + methodName + "'");
		}
	}

	private static boolean isCongruent(
			final Class<?>[] params, 
			final Object[] args,
			final boolean exactMatch,
			final boolean varargs
	) {
		if (args == null) {
			return params.length == 0;
		}
		else if (params.length == args.length) {
			for (int ii=0; ii<params.length; ii++) {
				final Object arg = args[ii];
				final Class<?> argType = (arg == null) ? null : arg.getClass();
				final Class<?> paramType = params[ii];
				
				if (ReflectionTypes.isEnumType(paramType)) {
					if (arg != null) {
						if (arg instanceof String) {
							final ScopedEnumValue scopedEnum = new ScopedEnumValue((String)arg);
							if (scopedEnum.isScoped()) {
								if (!scopedEnum.isCompatible(paramType)) {
									return false; // enum type not matching
								}
							}
							else {
								// non scoped enum name -> test compatibility while boxing
							}
						}
						else {
							// an arg other than string can not be converted to an enum value
							return false;
						}
					}
				}
				else {
					final boolean match = exactMatch 
											? paramArgTypeMatchExact(paramType, argType)
											: paramArgTypeMatch(paramType, argType);
					if (!match) {
						return false;
					}
				}
			}
			return true;
		}
		else {
			return false;
		}
	}

	private static boolean paramArgTypeMatchExact(final Class<?> paramType, final Class<?> argType) {
		if (argType == null) {
			// an arg of value <null> can be assigned to any object param type
			return !paramType.isPrimitive(); 
		}
		
		if (paramType == argType || paramType.isAssignableFrom(argType)) {
			return true;
		}
		
		if (paramType == byte.class 
				|| argType == Byte.class 
				|| paramType == short.class 
				|| argType == Short.class 
				|| paramType == int.class 
				|| argType == Integer.class 
				|| paramType == long.class
				|| paramType == Long.class
		) {
			return argType == Byte.class 
					|| argType == Short.class 
					|| argType == Integer.class 
					|| argType == Long.class;
		}
		if (paramType == float.class 
				|| paramType == Float.class
				|| paramType == double.class
				|| paramType == Double.class
		) {
			return argType == Float.class || argType == Double.class;
		}
		else if (paramType == char.class || paramType == Character.class) {
			return argType == Character.class;
		}
		else if (paramType == boolean.class || paramType == Boolean.class) {
			return argType == Boolean.class;
		}
		
		return false;
	}

	private static boolean paramArgTypeMatch(final Class<?> paramType, final Class<?> argType) {
		if (argType == null) {
			// an arg of value <null> can be assigned to any object param type
			return !paramType.isPrimitive();
		}
		
		if (paramType == argType || paramType.isAssignableFrom(argType)) {
			return true;
		}
		
		if (paramType == byte.class 
				|| paramType == Byte.class 
				|| paramType == short.class 
				|| paramType == Short.class 
				|| paramType == int.class 
				|| paramType == Integer.class 
				|| paramType == long.class 
				|| paramType == Long.class 
				|| paramType == float.class 
				|| paramType == Float.class 
				|| paramType == double.class
				|| paramType == Double.class 
		) {
			return argType == Byte.class 
					|| argType == Short.class 
					|| argType == Integer.class 
					|| argType == Long.class 
					|| argType == Float.class 
					|| argType == Double.class;
		}
		else if (paramType == char.class || paramType == Character.class) {
			return argType == Character.class;
		}
		else if (paramType == boolean.class || paramType == Boolean.class) {
			return argType == Boolean.class;
		}
		else if (ReflectionTypes.isArrayType(paramType)) {
			final Class<?> paramComponentType = paramType.getComponentType();					
			if (paramComponentType == byte.class) {
				if (argType == String.class) {
					return true;
				}
				else if (ByteBuffer.class.isAssignableFrom(argType)) {
					return true;
				}
			}
			return false;
		}
		
		return false;
	}

	private static Object[] boxArgs(final Class<?>[] params, final Object[] args) {
		if (params.length == 0) {
			return null;
		}
		
		final Object[] ret = new Object[params.length];
		for (int ii=0; ii<params.length; ii++) {
			ret[ii] = boxArg(params[ii], args[ii]);
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	private static Object boxArg(final Class<?> paramType, final Object arg) {
		if (!paramType.isPrimitive()) {
			if (ReflectionTypes.isArrayType(paramType)) {
				return boxArrayArg(paramType, arg);		
			}
			else if (arg instanceof Number) {
				final Object boxed = boxNumberArg(paramType, (Number)arg);		
				if (boxed != null) {
					return boxed;
				}
			}
			else if(ReflectionTypes.isEnumType(paramType)) {
				return boxEnumArg((Class<? extends Enum<?>>)paramType, arg);
			}
		
			return paramType.cast(arg); // try to cast
		}
		else if (paramType == boolean.class) {
			return Boolean.class.cast(arg);
		}
		else if (paramType == char.class) {
			return Character.class.cast(arg);
		}
		else if (arg instanceof Number) {
			final Object boxed = boxNumberArg(paramType, (Number)arg);		
			if (boxed != null) {
				return boxed;
			}
		}
		
		throw new JavaMethodInvocationException(
				String.format(
						"Unexpected param type, expected: %s, given: %s",
						paramType.getName(), arg.getClass().getName()));
	}

	private static Object boxNumberArg(final Class<?> paramType, final Number arg) {
		if (paramType == byte.class || paramType == Byte.class) {
			return arg.byteValue();
		}
		else if (paramType == short.class || paramType == Short.class) {
			return arg.shortValue();
		}
		else if (paramType == int.class || paramType == Integer.class) {
			return arg.intValue();
		}
		else if (paramType == long.class || paramType == Long.class) {
			return arg.longValue();
		}
		else if (paramType == float.class || paramType == Float.class) {
			return arg.floatValue();
		}
		else if (paramType == double.class || paramType == Double.class) {
			return arg.doubleValue();
		}
		else {
			return null;
		}
	}
	
	private static Enum<?> boxEnumArg(final Class<? extends Enum<?>> enumType, final Object arg) {
		if (arg instanceof String) {
			final ScopedEnumValue scopedEnum = new ScopedEnumValue((String)arg);
			if (scopedEnum.isScoped()) {
				if (scopedEnum.isCompatible(enumType)) {
					final Enum<?> e = scopedEnum.getEnum(enumType);
					if (e != null) {
						return e;
					}
					else {
						throw new JavaMethodInvocationException(String.format(
								"Enum %s does not define value %s",
								enumType.getName(),
								scopedEnum.getEnumValue()));
					}
				}
				else {
					throw new JavaMethodInvocationException(String.format(
							"Enum %s is not compatible with %s",
							scopedEnum.getScopedEnumValue(),
							enumType.getName()));
				}
			}
			else {
				final Enum<?> e = scopedEnum.getEnum(enumType);
				if (e != null) {
					return e;
				}
				else {
					throw new JavaMethodInvocationException(String.format(
							"Enum %s does not define value %s",
							enumType.getName(),
							scopedEnum.getEnumValue()));
				}
			}
		}
		else {
			throw new JavaMethodInvocationException(String.format(
					"Cannot convert type %s to enum %s",
					arg.getClass().getName(),
					enumType.getName()));	
		}
	}

	private static Object boxArrayArg(Class<?> type, final Object arg) {
		final Class<?> componentType = type.getComponentType();					
		if (componentType == byte.class) {
			if (arg == null) {
				return boxStringToByteArray((String)arg);
			}
			else if (arg.getClass() == String.class) {
				return boxStringToByteArray((String)arg);
			}
			else if (arg.getClass() == byte[].class) {
				return (byte[])arg;
			}
			else if (arg instanceof ByteBuffer) {
				return ((ByteBuffer)arg).array();
			}
		}
		
		if (ReflectionTypes.isListOrSet(arg.getClass())) {
			final int size = ((Collection<?>)arg).size();
			final Object arr = Array.newInstance(componentType, size);
			
			final AtomicInteger idx = new AtomicInteger(0);
			((Collection<?>)arg).forEach(v -> {
				Array.set(arr, idx.getAndIncrement(), boxArg(componentType, v));
			});
			
			return arr;
		}
		else if (ReflectionTypes.isMap(arg.getClass())) {
			throw new JavaMethodInvocationException("Cannot box map to array");	
		}
		else {
			// scalar type
			final Object arr = Array.newInstance(componentType, 1);			
			Array.set(arr, 0, boxArg(componentType, arg));
			return arr;
		}
	}
	
	private static byte[] boxStringToByteArray(final String str) {
		try {
			return str == null ? null : str.getBytes("UTF-8");
		}
		catch(Exception ex) {
			throw new JavaMethodInvocationException(
					"Failed to box arg of type String to byte[]", ex);
		}
	}

	private static String noMatchingFieldErrMsg(final String fieldName, final Object target) {
		return String.format(
				"No matching public field found: '%s' for target '%s'",
				fieldName,
				target == null ? "<null>" : target.getClass().getName());
	}

	private static String noMatchingMethodErrMsg(final String methodName, final Object target, final Object... methodArgs) {
		return String.format(
				"No matching public method found: %s(%s) for target '%s'",
				methodName,
				formatArgTypes(methodArgs),
				target == null ? "<null>" : target.getClass().getName());
	}

	private static String noMatchingMConstructorErrMsg(final Class<?> clazz) {
		return String.format(
				"No matching public constructor found: '%s'",
				clazz.getName());
	}

	private static Class<?> memoizedClassForName(final String className) {
		return isCacheEnabled()
				? classCache.computeIfAbsent(className, k -> ReflectionUtil.classForName(k))
				: ReflectionUtil.classForName(className);
	}

	private static List<String> memoizedBeanGetterProperties(final Class<?> clazz) {
		return isCacheEnabled()
				? getterPropertiesCache.computeIfAbsent(
					clazz, 
					k -> ReflectionUtil.getBeanGetterProperties(k))
				: ReflectionUtil.getBeanGetterProperties(clazz);
	}

	private static List<String> memoizedBeanSetterProperties(final Class<?> clazz) {
		return isCacheEnabled()
				? setterPropertiesCache.computeIfAbsent(
					clazz, 
					k -> ReflectionUtil.getBeanSetterProperties(k))
				: ReflectionUtil.getBeanSetterProperties(clazz);
	}

	private static Method memoizedBeanGetterMethod(final Class<?> clazz, final String propertyName) {
		return isCacheEnabled()
				? getterMethodCache.computeIfAbsent(
					new Tuple2<>(clazz,propertyName), 
					k -> ReflectionUtil.getBeanGetterMethod(k._1, k._2))
				: ReflectionUtil.getBeanGetterMethod(clazz, propertyName);
	}

	private static Method memoizedBeanSetterMethod(final Class<?> clazz, final String propertyName) {
		return isCacheEnabled()
				? setterMethodCache.computeIfAbsent(
					new Tuple2<>(clazz,propertyName), 
					k -> ReflectionUtil.getBeanSetterMethod(k._1, k._2))
				: ReflectionUtil.getBeanSetterMethod(clazz, propertyName);
	}

	private static List<Constructor<?>> memoizedPublicConstructors(final Class<?> clazz, final int args) {
		return isCacheEnabled()
				? constructorCache.computeIfAbsent(
					new Tuple2<>(clazz,args), 
					k -> ReflectionUtil.getPublicConstructors(k._1, k._2))
				: ReflectionUtil.getPublicConstructors(clazz, args);
	}
	
	private static Field memoizedStaticField(final Class<?> clazz, final String fieldName) {
		return isCacheEnabled()
				? staticFieldCache.computeIfAbsent(
					new Tuple2<>(clazz,fieldName), 
					k ->  ReflectionUtil.getPublicStaticField(k._1, k._2))
				: ReflectionUtil.getPublicStaticField(clazz, fieldName);
	}
	
	private static Field memoizedInstanceField(final Class<?> clazz, final String fieldName) {
		return isCacheEnabled()
				? instanceFieldCache.computeIfAbsent(
					new Tuple2<>(clazz,fieldName), 
					k ->  ReflectionUtil.getPublicInstanceField(k._1, k._2))
				: ReflectionUtil.getPublicInstanceField(clazz, fieldName);
	}
	
	private static List<Method> memoizedStaticMethod(
			final Class<?> clazz, 
			final String methodName, 
			final Integer arity, 
			final boolean includeInheritedClasses
	) {
		return isCacheEnabled()
				? staticMethodCache.computeIfAbsent(
					new Tuple4<>(clazz,methodName,arity,includeInheritedClasses), 
					k ->  ReflectionUtil.getAllPublicStaticMethods(k._1, k._2, k._3, k._4))
				: ReflectionUtil.getAllPublicStaticMethods(clazz,methodName,arity,includeInheritedClasses);
	}
	
	private static List<Method> memoizedInstanceMethod(
			final Class<?> clazz, 
			final String methodName, 
			final Integer arity, 
			final boolean includeInheritedClasses
	) {
		return isCacheEnabled()
				? instanceMethodCache.computeIfAbsent(
					new Tuple4<>(clazz,methodName,arity,includeInheritedClasses), 
					k ->  ReflectionUtil.getAllPublicInstanceMethods(k._1, k._2, k._3, k._4))
				: ReflectionUtil.getAllPublicInstanceMethods(clazz,methodName,arity,includeInheritedClasses);
	}
	
	private static String formatArgTypes(final Object[] args) {
		return Arrays
				.stream(args)
				.map(o -> o.getClass())
				.map(c -> c.getSimpleName())
				.collect(Collectors.joining(", "));
	}

	
	private static final AtomicBoolean cachingEnabled = new AtomicBoolean(true);
	
	private static final HashMap<String,Class<?>> classCache = new HashMap<>();
	private static final HashMap<Class<?>,List<String>> getterPropertiesCache = new HashMap<>();
	private static final HashMap<Class<?>,List<String>> setterPropertiesCache = new HashMap<>();
	private static final HashMap<Tuple2<Class<?>,String>,Method> getterMethodCache = new HashMap<>();
	private static final HashMap<Tuple2<Class<?>,String>,Method> setterMethodCache = new HashMap<>();
	private static final HashMap<Tuple2<Class<?>,Integer>,List<Constructor<?>>> constructorCache = new HashMap<>();
	private static final HashMap<Tuple2<Class<?>,String>,Field> staticFieldCache = new HashMap<>();
	private static final HashMap<Tuple2<Class<?>,String>,Field> instanceFieldCache = new HashMap<>();
	private static final HashMap<Tuple4<Class<?>,String,Integer,Boolean>,List<Method>> staticMethodCache = new HashMap<>();
	private static final HashMap<Tuple4<Class<?>,String,Integer,Boolean>,List<Method>> instanceMethodCache = new HashMap<>();
}
