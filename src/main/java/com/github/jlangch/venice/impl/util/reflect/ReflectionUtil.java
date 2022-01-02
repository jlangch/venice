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
package com.github.jlangch.venice.impl.util.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.util.StringUtil;


/**
 * Java reflection utilities
 */
public class ReflectionUtil {

	
	/**
	 * Load a class
	 * 
	 * @param name the class's name
	 * @return the class
	 */
	static public Class<?> classForName(final String name) {
		final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		if (contextClassLoader != null) {
			try {
				return Class.forName(name, true, contextClassLoader);
			}
			catch(Throwable ex) {
				// try next with current class loader
			}
		}
		
		// current class loader
		try {
			return Class.forName(name);
		}
		catch(Throwable ex) {
			throw new RuntimeException(String.format("Failed to load class '%s'", name));
		}
	}	
	
	/**
	 * Checks if the class for the given name exists. 
	 * 
	 * @param name the class's name
	 * @return true if the class exists else false
	 */
	static public boolean isClassAvailable(final String name) {
		try {
			return classForName(name) != null;
		}
		catch(Throwable ex) {
			return false;
		}
	}	

	/**
	 * Checks if a method is a getter method
	 * 
	 * @param method the method to check
	 * @return true if the method is a getter, otherwise false
	 */
	public static boolean isGetter(final Method method) {
		final String methodName = method.getName();
		return (isGetAccessor(methodName) || isIsAccessor(methodName))
					&& !method.isBridge()
					&& arity(method) == 0
					&& !void.class.equals(method.getReturnType());
	}
	
	/**
	 * Checks if a method is a setter method
	 * 
	 * @param method the method to check
	 * @return true if the method is a setter, otherwise false
	 */
	public static boolean isSetter(final Method method) {
		final String methodName = method.getName();
		return methodName.startsWith("set")
				&& !method.isBridge()
				&& methodName.length() > 3
				&& StringUtil.isAsciiAlphaUpper(methodName.charAt(3))
				&& arity(method) == 1
				&& void.class.equals(method.getReturnType());
	}
	
	/**
	 * Checks if a method returns void
	 * 
	 * @param method the method to check
	 * @return true if the method returns void, otherwise false
	 */
	public static boolean isReturnVoid(final Method method) {
		return void.class.equals(method.getReturnType());
	}
	
	public static String getAttributeNameByGetter(final Method getter) {
		if (!isGetter(getter)) {
			throw new IllegalArgumentException("Passed method is not a getter!");
		}
		
		final String name = getter.getName();
		if (name.startsWith("get")) {
			final String p = name.substring(3);
			return String.valueOf(Character.toLowerCase(p.charAt(0))) + p.substring(1);
		}
		if (name.startsWith("is")) {
			final String p = name.substring(2);
			return String.valueOf(Character.toLowerCase(p.charAt(0)))  + p.substring(1);
		}
		else {
			return null;
		}
	}
	
	/**
	 * Returns the type arguments of a parameterized type
	 * if the given type is not parameterized, null is returned
	 * 
	 * @param type a type
	 * @return an array of types or null
	 */
	public static Type[] getTypeArguments(final Type type) {
		if (type instanceof ParameterizedType) {
			return ((ParameterizedType)type).getActualTypeArguments();
		}
		return null;
	}
	
	/**
	 * Finds the nearest super class of two classes
	 * 
	 * @param c1 a class
	 * @param c2 another class
	 * @return their nearest super class
	 */
	public static Class<?> nearestSuperClass(
			final Class<?> c1,
			final Class<?> c2
	) {
	    Class<?> s = c1;
	    while (!s.isAssignableFrom(c2)) s = s.getSuperclass();
	    return s;
	}
	
	/**
	 * Finds the nearest super class of a collection of classes
	 * 
	 * @param classes a collection of classes
	 * @return their nearest super class or null if the collection is empty
	 */
	public static Class<?> nearestSuperClass(final Collection<Class<?>> classes) {
	    return classes.stream().reduce(ReflectionUtil::nearestSuperClass).orElse(null);
	}

	/**
	 * Gets the type of a generic parameter of a class that has a generic super class.
	 * 
	 * @param clazz a class having a generic super class 
	 * @param idx number of the parameter
	 * @return the type of the parameter
	 */
	public static Class<?> getGenericType(final Class<?> clazz, final int idx) {
		if (clazz.getGenericSuperclass() instanceof ParameterizedType) {
			final ParameterizedType parameterizedType = (ParameterizedType)clazz.getGenericSuperclass();
			if (parameterizedType.getActualTypeArguments().length > idx) {
				final Type type = parameterizedType.getActualTypeArguments()[idx];
				if (type instanceof Class) {
					return (Class<?>)type;
				}
				if (type instanceof ParameterizedType) {
					final Type rawType = ((ParameterizedType)type).getRawType();
					if (rawType instanceof Class) {
						return (Class<?>)rawType; 
					}
				}
			}
		}
		return null;
	}

	public static Class<?> getSuperclass(final Class<?> type) {
		final Class<?> parent = type.getSuperclass();
		if (parent == null || parent == Object.class) {
			return null;
		}
		else {
			return parent;
		}
	}

	public static List<Class<?>> getAllDirectInterfaces(final Class<?> type) {
		return Arrays.asList(type.getInterfaces());
	}

	public static List<Class<?>> getAllSuperclasses(final Class<?> clazz) {
		final List<Class<?>> superclasses = new ArrayList<>();
		getAllSuperclasses(ReflectionUtil.getSuperclass(clazz), superclasses);
		return superclasses;
	}

	private static void getAllSuperclasses(final Class<?> clazz, final List<Class<?>> superclasses) {
		if (clazz != null) {
			superclasses.add(clazz);
			getAllSuperclasses(ReflectionUtil.getSuperclass(clazz), superclasses);
		}
	}

	public static List<Class<?>> getAllInterfaces(final List<Class<?>> classes) {
		final List<Class<?>> interfaces = new ArrayList<>();
		classes.forEach(c -> getAllInterfaces(c, interfaces));
		return interfaces;
	}
	
	private static void getAllInterfaces(final Class<?> clazz, final List<Class<?>> interfaces) {
		ReflectionUtil
			.getAllDirectInterfaces(clazz)
			.forEach(c -> {
				interfaces.add(c);
				getAllInterfaces(c, interfaces);
			});
	}

	public static List<Class<?>> distinct(final List<Class<?>> classes) {
		final Set<Class<?>> visited = new HashSet<>();
		
		final List<Class<?>> distinct = new ArrayList<>();
		classes.forEach(c -> {
			if (!visited.contains(c)) {
				visited.add(c);
				distinct.add(c);
			}
		});
		
		return distinct;
	}
	
	
	
	
	
	
	public static boolean hasDefaultConstructor(final Class<?> type) {
		try {
			return type.getConstructor() != null;
		}
		catch(NoSuchMethodException ex) {
			return false;
		}
	}

	public static Constructor<?> getDefaultConstructor(final Class<?> type) {
		try {
			return type.getConstructor();
		}
		catch(NoSuchMethodException ex) {
			return null;
		}
	}

	public static List<Constructor<?>> getPublicConstructors(
			final Class<?> type
	) {
		final List<Constructor<?>> constructors = new ArrayList<>();
		
		for(Constructor<?> c : type.getDeclaredConstructors()) {
			if (Modifier.isPublic(c.getModifiers())) {
				constructors.add(c);
			}
		}
		
		return constructors;
	}

	public static List<Constructor<?>> getPublicConstructors(
			final Class<?> type, final int numArgs
	) {
		final List<Constructor<?>> constructors = new ArrayList<>();
		
		for(Constructor<?> c : type.getDeclaredConstructors()) {
			if (Modifier.isPublic(c.getModifiers())) {
				if (c.getParameterTypes().length == numArgs) {
					constructors.add(c);
				}
			}
		}
		
		return constructors;
	}

	public static List<Method> getBeanGetterMethods(final Class<?> type) {
		return getAllPublicInstanceMethods(type, true)
				.stream()
				.filter(m -> isBeanGetterMethod(m))
				.collect(Collectors.toList());
	}

	public static List<String> getBeanGetterProperties(final Class<?> type) {
		return getBeanGetterMethods(type)
				.stream()
				.map(m -> getBeanPropertyName(m))
				.collect(Collectors.toList());
	}

	public static boolean isBeanGetterMethod(final Method method) {
		if (!isPublic(method)) {
			return false;
		}
		else if (isStatic(method)) {
			return false;
		}
		else if (isTransient(method)) {
			return false;
		}
		else if (arity(method) != 0) {
			return false;
		}
		
		final String name = method.getName();
		return (name.startsWith("get") || name.startsWith("is"));
	}
	
	public static List<Method> getBeanSetterMethods(final Class<?> type) {
		return getAllPublicInstanceMethods(type, true)
				.stream()
				.filter(m -> isBeanSetterMethod(m))
				.collect(Collectors.toList());
	}

	public static List<String> getBeanSetterProperties(final Class<?> type) {
		return getBeanSetterMethods(type)
				.stream()
				.map(m -> getBeanPropertyName(m))
				.collect(Collectors.toList());
	}

	public static boolean isBeanSetterMethod(final Method method) {
		if (!isPublic(method)) {
			return false;
		}
		else if (isStatic(method)) {
			return false;
		}
		else if (isTransient(method)) {
			return false;
		}
		else if (arity(method) != 1) {
			return false;
		}
		
		final String name = method.getName();
		return (name.startsWith("set"));
	}

	public static String getBeanPropertyName(final Method method) {
		final String name = method.getName();

		if (isBeanGetterMethod(method)) {
			if (name.startsWith("get")) {
				final String p = name.substring(3);
				return String.valueOf(Character.toLowerCase(p.charAt(0))) + p.substring(1);
			}
			else if (name.startsWith("is")) {
				final String p = name.substring(2);
				return String.valueOf(Character.toLowerCase(p.charAt(0)))  + p.substring(1);
			}
		}
		else if (isBeanSetterMethod(method)) {
			if (name.startsWith("set")) {
				final String p = name.substring(3);
				return String.valueOf(Character.toLowerCase(p.charAt(0))) + p.substring(1);
			}
		}
		
		throw new IllegalArgumentException(String.format(
				"The method '%s'is not a bean property accessor of class '%s'",
				name,
				method.getDeclaringClass().getName()));
	}	
	
	public static Method getBeanGetterMethod(final Class<?> clazz, final String propertyName) {
		return ReflectionUtil
				.getBeanGetterMethods(clazz)
				.stream()
				.filter(m -> ReflectionUtil.getBeanPropertyName(m).equals(propertyName))
				.findFirst()
				.orElse(null);
	}
	
	public static Method getBeanSetterMethod(final Class<?> clazz, final String propertyName) {
		return ReflectionUtil
				.getBeanSetterMethods(clazz)
				.stream()
				.filter(m -> ReflectionUtil.getBeanPropertyName(m).equals(propertyName))
				.findFirst()
				.orElse(null);
	}

	public static List<Field> getPublicStaticFields(final Class<?> clazz) {
		return Arrays
				.stream(clazz.getFields())
				.filter(f -> isPublic(f))
				.filter(f -> isStatic(f))
				.collect(Collectors.toList());
	}

	public static Field getPublicStaticField(final Class<?> clazz, String name) {
		return Arrays
				.stream(clazz.getFields())
				.filter(f -> f.getName().equals(name))
				.filter(f -> isPublic(f))
				.filter(f -> isStatic(f))
				.findFirst()
				.orElse(null);
	}

	public static List<Field> getPublicInstanceFields(final Class<?> clazz) {
		return Arrays
				.stream(clazz.getFields())
				.filter(f -> isPublic(f))
				.filter(f -> !isStatic(f))
				.collect(Collectors.toList());
	}

	public static Field getPublicInstanceField(final Class<?> clazz, String name) {
		return Arrays
				.stream(clazz.getFields())
				.filter(f -> f.getName().equals(name))
				.filter(f -> isPublic(f))
				.filter(f -> !isStatic(f))
				.findFirst()
				.orElse(null);
	}

	public static int arity(final Constructor<?> constructor) {
		return constructor.getParameters().length;
	}

	public static int arity(final Method method) {
		return method.getParameters().length;
	}

	public static boolean hasVarArgs(final Method method) {
		return method.isVarArgs();
	}

	public static boolean isStatic(final Method method) {
		return Modifier.isStatic(method.getModifiers());
	}

	public static boolean isPublic(final Method method) {
		return Modifier.isPublic(method.getModifiers());
	}

	public static boolean isDeprecated(final Method method) {
		return method.isAnnotationPresent(Deprecated.class);
	}

	public static boolean isTransient(final Method method) {
		return method.isAnnotationPresent(java.beans.Transient.class);
	}

	public static boolean isStatic(final Field field) {
		return Modifier.isStatic(field.getModifiers());
	}

	public static boolean isPublic(final Field field) {
		return Modifier.isPublic(field.getModifiers());
	}
	
	public static List<Method> getAllPublicInstanceMethods(
			final Class<?> clazz, 
			final boolean includeInheritedClasses
	) {
		return getAllPublicMethods(clazz, null, null, includeInheritedClasses, true, false, true, true);
	}
	
	public static List<Method> getAllPublicInstanceMethods(
			final Class<?> clazz, 
			final String methodName,
			final Integer arity,
			final boolean includeInheritedClasses
	) {
		return getAllPublicMethods(clazz, methodName, arity, includeInheritedClasses, true, false, true, true);
	}
	
	public static List<Method> getAllPublicStaticMethods(
			final Class<?> clazz, 
			final boolean includeInheritedClasses
	) {
		return getAllPublicMethods(clazz, null, null, includeInheritedClasses, false, true, true, true);
	}
	
	public static List<Method> getAllPublicStaticMethods(
			final Class<?> clazz, 
			final String methodName,
			final Integer arity,
			final boolean includeInheritedClasses
	) {
		return getAllPublicMethods(clazz, methodName, arity, includeInheritedClasses, false, true, true, true);
	}

	public static List<Method> getAllPublicMethods(
			final Class<?> clazz, 
			final String methodName,
			final Integer arity,
			final boolean includeInheritedClasses,
			final boolean addInstanceMethods, 
			final boolean addStaticMethods, 
			final boolean addTransientMethods, 
			final boolean addDeprecatedMethods
	) {
		final Method[] methods = includeInheritedClasses ? clazz.getMethods() : clazz.getDeclaredMethods();
		
		return Arrays
				.stream(methods)
				.filter(m -> methodName == null || methodName.equals(m.getName()))
				.filter(m -> isPublic(m))
				.filter(m -> !m.isBridge())
				.filter(m -> (addInstanceMethods && addStaticMethods)
								|| (addInstanceMethods && !isStatic(m))
								|| (addStaticMethods && isStatic(m)))
				.filter(m -> addTransientMethods || !isTransient(m))
				.filter(m -> addDeprecatedMethods || !isDeprecated(m))
				.filter(m -> arity == null || arity == arity(m))
				//.filter(m -> arity == null  || arity == arity(m) || (m.isVarArgs() && arity >= (arity(m) - 1)))
				.collect(Collectors.toList());
	}


	private static boolean isGetAccessor(final String methodName) {
		return methodName.startsWith("get") 
				&& methodName.length() > 3 
				&& StringUtil.isAsciiAlphaUpper(methodName.charAt(3));
	}

	private static boolean isIsAccessor(final String methodName) {
		return methodName.startsWith("is")
				&& methodName.length() > 2 
				&& StringUtil.isAsciiAlphaUpper(methodName.charAt(2));
	}
}
