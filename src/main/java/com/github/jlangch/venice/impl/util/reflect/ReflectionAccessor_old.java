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
package com.github.jlangch.venice.impl.util.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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


public class ReflectionAccessor_old {

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

	public static boolean classExists(final String className) {
		try {
			return memoizedClassForName(className) != null;
		}
		catch (Exception ex) {
			return false;
		}
	}

	public static Object invokeConstructor(final Class<?> clazz, final Object[] args) {
		try {
			final List<Constructor<?>> ctors = memoizedPublicConstructors(clazz, args.length);
			if (ctors.isEmpty()) {
				throw new JavaMethodInvocationException(noMatchingConstructorErrMsg(clazz));
			} 
			else if (ctors.size() == 1) {
				final Constructor<?> ctor = (Constructor<?>)ctors.get(0);
				return ctor.newInstance(Boxing.boxArgs(ctor.getParameterTypes(), args));
			} 
			else {
				// overloaded
							
				// try exact match first
				for(Constructor<?> ctor : ctors) {
					final Class<?>[] params = ctor.getParameterTypes();
					
					if (ArgTypeMatcher.isCongruent(params, args, true, ctor.isVarArgs())) {
						Object[] boxedArgs = Boxing.boxArgs(params, args);
						return ctor.newInstance(boxedArgs);
					}
				}

				// try widened match second
				for(Constructor<?> ctor : ctors) {
					final Class<?>[] params = ctor.getParameterTypes();
										
					if (ArgTypeMatcher.isCongruent(params, args, false, ctor.isVarArgs())) {
						Object[] boxedArgs = Boxing.boxArgs(params, args);
						return ctor.newInstance(boxedArgs);
					}
				}

				throw new JavaMethodInvocationException(noMatchingConstructorErrMsg(clazz));
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
			final Class<?> clazz, 
			final String methodName, 
			final Object[] args
	) {
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
				method.invoke(target, new Object[] { Boxing.boxArg(type, value) });
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
			final Object[] boxedArgs = Boxing.boxArgs(m.getParameterTypes(), args);
			return invoke(m, target, boxedArgs);
		} 
		else { 
			// overloaded
			
			// try exact match first
			for (Method m : methods) {
				final Class<?>[] params = m.getParameterTypes();
				
				if (ArgTypeMatcher.isCongruent(params, args, true, m.isVarArgs())) {
					final Object[] boxedArgs = Boxing.boxArgs(params, args);
					return invoke(m, target, boxedArgs);
				}
			}

			// try widened match second
			for (Method m : methods) {
				final Class<?>[] params = m.getParameterTypes();
				
				if (ArgTypeMatcher.isCongruent(params, args, false, m.isVarArgs())) {
					final Object[] boxedArgs = Boxing.boxArgs(params, args);
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
		catch (SecurityException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new JavaMethodInvocationException(
					target == null
						? String.format(
							"Failed to invoke static method %s(%s) on class %s",
							method.getName(),
							formatMethodArgTypes(method.getParameterTypes()),
							method.getDeclaringClass().getName())
						: String.format(
							"Failed to invoke instance method %s(%s) on target %s",
							method.getName(),
							formatMethodArgTypes(method.getParameterTypes()),
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

	private static String noMatchingConstructorErrMsg(final Class<?> clazz) {
		return String.format(
				"No matching public constructor found: '%s'",
				clazz.getName());
	}

	private static Class<?> memoizedClassForName(final String className) {
		return classCache.computeIfAbsent(
					className, 
					k -> ReflectionUtil.classForName(k));
	}

	private static List<String> memoizedBeanGetterProperties(final Class<?> clazz) {
		return getterPropertiesCache.computeIfAbsent(
					clazz, 
					k -> ReflectionUtil.getBeanGetterProperties(k));
	}

	private static List<String> memoizedBeanSetterProperties(final Class<?> clazz) {
		return setterPropertiesCache.computeIfAbsent(
					clazz, 
					k -> ReflectionUtil.getBeanSetterProperties(k));
	}

	private static Method memoizedBeanGetterMethod(final Class<?> clazz, final String propertyName) {
		return getterMethodCache.computeIfAbsent(
					new Tuple2<>(clazz,propertyName), 
					k -> ReflectionUtil.getBeanGetterMethod(k._1, k._2));
	}

	private static Method memoizedBeanSetterMethod(final Class<?> clazz, final String propertyName) {
		return setterMethodCache.computeIfAbsent(
					new Tuple2<>(clazz,propertyName), 
					k -> ReflectionUtil.getBeanSetterMethod(k._1, k._2));
	}

	private static List<Constructor<?>> memoizedPublicConstructors(final Class<?> clazz, final int args) {
		return constructorCache.computeIfAbsent(
					new Tuple2<>(clazz,args), 
					k -> ReflectionUtil.getPublicConstructors(k._1, k._2));
	}
	
	private static Field memoizedStaticField(final Class<?> clazz, final String fieldName) {
		return staticFieldCache.computeIfAbsent(
					new Tuple2<>(clazz,fieldName), 
					k ->  ReflectionUtil.getPublicStaticField(k._1, k._2));
	}
	
	private static Field memoizedInstanceField(final Class<?> clazz, final String fieldName) {
		return instanceFieldCache.computeIfAbsent(
					new Tuple2<>(clazz,fieldName), 
					k ->  ReflectionUtil.getPublicInstanceField(k._1, k._2));
	}
	
	private static List<Method> memoizedStaticMethod(
			final Class<?> clazz, 
			final String methodName, 
			final Integer arity, 
			final boolean includeInheritedClasses
	) {
		return staticMethodCache.computeIfAbsent(
					new Tuple4<>(clazz,methodName,arity,includeInheritedClasses), 
					k ->  ReflectionUtil.getAllPublicStaticMethods(k._1, k._2, k._3, k._4));
	}
	
	private static List<Method> memoizedInstanceMethod(
			final Class<?> clazz, 
			final String methodName, 
			final Integer arity, 
			final boolean includeInheritedClasses
	) {
		return instanceMethodCache.computeIfAbsent(
					new Tuple4<>(clazz,methodName,arity,includeInheritedClasses), 
					k ->  ReflectionUtil.getAllPublicInstanceMethods(k._1, k._2, k._3, k._4));
	}
	
	private static String formatArgTypes(final Object[] args) {
		return Arrays
				.stream(args)
				.map(o -> o.getClass().getSimpleName())
				.collect(Collectors.joining(", "));
	}
	
	
	private static String formatMethodArgTypes(final Class<?>[] args) {
		return Arrays
				.stream(args)
				.map(o -> o.getSimpleName())
				.collect(Collectors.joining(", "));
	}

	
	private static final Map<String,Class<?>> classCache = new ConcurrentHashMap<>();
	private static final Map<Class<?>,List<String>> getterPropertiesCache = new ConcurrentHashMap<>();
	private static final Map<Class<?>,List<String>> setterPropertiesCache = new ConcurrentHashMap<>();
	private static final Map<Tuple2<Class<?>,String>,Method> getterMethodCache = new ConcurrentHashMap<>();
	private static final Map<Tuple2<Class<?>,String>,Method> setterMethodCache = new ConcurrentHashMap<>();
	private static final Map<Tuple2<Class<?>,Integer>,List<Constructor<?>>> constructorCache = new ConcurrentHashMap<>();
	private static final Map<Tuple2<Class<?>,String>,Field> staticFieldCache = new ConcurrentHashMap<>();
	private static final Map<Tuple2<Class<?>,String>,Field> instanceFieldCache = new ConcurrentHashMap<>();
	private static final Map<Tuple4<Class<?>,String,Integer,Boolean>,List<Method>> staticMethodCache = new ConcurrentHashMap<>();
	private static final Map<Tuple4<Class<?>,String,Integer,Boolean>,List<Method>> instanceMethodCache = new ConcurrentHashMap<>();
}
