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

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
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
import com.github.jlangch.venice.SecurityException;
import com.github.jlangch.venice.impl.util.Tuple2;
import com.github.jlangch.venice.impl.util.Tuple4;
import com.github.jlangch.venice.javainterop.ReturnValue;


public class ReflectionAccessor {

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

    public static ReturnValue invokeConstructor(final Class<?> clazz, final Object[] args) {
        try {
            final List<Constructor<?>> ctors = memoizedPublicConstructors(clazz, args.length);
            if (ctors.isEmpty()) {
                throw new JavaMethodInvocationException(noMatchingConstructorErrMsg(clazz, args));
            }
            else if (ctors.size() == 1) {
                final Constructor<?> ctor = ctors.get(0);
                final Object[] boxedArgs = Boxing.boxArgs(ctor.getParameterTypes(), args);
                return new ReturnValue(ctor.newInstance(boxedArgs));
            }
            else {
                // overloaded

                // try exact match first
                for(Constructor<?> ctor : ctors) {
                    final Class<?>[] params = ctor.getParameterTypes();

                    if (ArgTypeMatcher.isCongruent(params, args, true, ctor.isVarArgs())) {
                        final Object[] boxedArgs = Boxing.boxArgs(params, args);
                        return new ReturnValue(ctor.newInstance(boxedArgs));
                    }
                }

                // try widened match second
                for(Constructor<?> ctor : ctors) {
                    final Class<?>[] params = ctor.getParameterTypes();

                    if (ArgTypeMatcher.isCongruent(params, args, false, ctor.isVarArgs())) {
                        final Object[] boxedArgs = Boxing.boxArgs(params, args);
                        return new ReturnValue(ctor.newInstance(boxedArgs));
                    }
                }

                throw new JavaMethodInvocationException(noMatchingConstructorErrMsg(clazz, args));
            }
        }
        catch (JavaMethodInvocationException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new JavaMethodInvocationException(
            		String.format(
            				"Failed to invoke constructor %s(%s)",
            				clazz.getName(),
            				formatArgTypes(args)),
                    ex);
        }
    }

    public static ReturnValue invokeInstanceMethod(
            final Object target,
            final String methodName,
            final Object[] args
    ) {
        return invokeInstanceMethod(target, null, methodName, args);
    }

    public static ReturnValue invokeInstanceMethod(
            final Object target,
            final Class<?> targetFormalType,
            final String methodName,
            final Object[] args
    ) {
        if (target == null) {
            throw new JavaMethodInvocationException(
                    String.format(
                            "Failed to invoke instance method '%s' on <null> target",
                            methodName));
        }

        try {
            final Class<?> clazz = targetFormalType == null ? target.getClass() : targetFormalType;
            final List<Method> methods = memoizedInstanceMethod(clazz, methodName, args.length, true);
            return invokeMatchingMethod(methodName, methods, targetFormalType, target, args);
        }
        catch (JavaMethodInvocationException ex) {
            throw ex;
        }
        catch (Exception ex) {
            if (targetFormalType == null) {
                throw new JavaMethodInvocationException(
                        String.format(
                                "Failed to invoke instance method '%s' on target '%s'",
                                methodName,
                                target.getClass().getName()),
                        ex);
            }
            else {
                throw new JavaMethodInvocationException(
                        String.format(
                                "Failed to invoke instance method '%s' on target '%s' with formal type '%s'",
                                methodName,
                                target.getClass().getName(),
                                targetFormalType.getName()),
                        ex);
            }
        }
    }

    public static ReturnValue invokeStaticMethod(
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

                return invokeMatchingMethod(methodName, methods, null, null, args);
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

    public static ReturnValue getStaticField(final Class<?> clazz, String fieldName) {
        try {
            final MethodHandle mh = memoizedStaticFieldGet(clazz, fieldName);
            if (mh != null) {
                return new ReturnValue(mh.invoke());
            }
            else {
                throw new JavaMethodInvocationException(noMatchingFieldErrMsg(fieldName, clazz.getName()));
            }
        }
        catch (JavaMethodInvocationException ex) {
            throw ex;
        }
        catch (Throwable ex) {
            throw new JavaMethodInvocationException(
                    String.format(
                            "Failed to get static field '%s' on class '%s'",
                            fieldName,
                            clazz.getName()),
                    ex);
        }
    }

    public static ReturnValue getInstanceField(final Object target, Class<?> targetFormalType, final String fieldName) {
        try {
            final Class<?> clazz = targetFormalType == null ? target.getClass() : targetFormalType;
            final MethodHandle mh = memoizedInstanceField(clazz, fieldName);
            if (mh != null) {
                return new ReturnValue(mh.invoke(target));
            }
            else {
                throw new JavaMethodInvocationException(noMatchingFieldErrMsg(fieldName, clazz.getName()));
            }
        }
        catch (JavaMethodInvocationException ex) {
            throw ex;
        }
        catch (Throwable ex) {
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

    public static ReturnValue getBeanProperty(
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
                return new ReturnValue(method.invoke(target));
            }
            catch(Exception ex) {
                throw new JavaMethodInvocationException(
                        failedToGetBeanPropertyErrMsg(target, propertyName),
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
                        failedToSetBeanPropertyErrMsg(target, propertyName),
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
        if (target == null) {
            throw new JavaMethodInvocationException(
                    String.format(
                            "Failed to check for available instance method '%s' on <null> target",
                            methodName));
        }

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
                            target.getClass().getName()),
                    ex);
        }
    }

    public static boolean isStaticField(final Class<?> clazz, final String fieldName) {
        try {
            return memoizedStaticFieldGet(clazz, fieldName) != null;
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
        final Class<?> clazz = target.getClass();
        try {
            return memoizedInstanceField(clazz, fieldName) != null;
        }
        catch (JavaMethodInvocationException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new JavaMethodInvocationException(
                    String.format(
                            "Failed to check for available instance field '%s' on target '%s'",
                            fieldName,
                            clazz.getName()),
                    ex);
        }
    }

    private static ReturnValue invokeMatchingMethod(
            final String methodName,
            final List<Method> methods,
            final Class<?> targetFormalType,
            final Object target,
            final Object[] args
    ) {
        if (methods.size() == 1) {
            final Method m = methods.get(0);
            final Object[] boxedArgs = Boxing.boxArgs(m.getParameterTypes(), args);
            return invoke(m, target, boxedArgs);
        }
        else if (methods.size() > 1){
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

        if (target == null) {
            final String errMsg =
                    String.format(
                        "No matching public static method found: %s(%s) for target class '%s'",
                        methodName,
                        formatArgTypes(args),
                        methods.get(0).getDeclaringClass());

            throw new JavaMethodInvocationException(errMsg);
        }
        else {
            final String errMsg =
                    String.format(
                        "No matching public instance method found: %s(%s) for target object '%s'%s",
                        methodName,
                        formatArgTypes(args),
                        target.getClass().getName(),
                        targetFormalType == null
                            ? ""
                            : String.format(" as formal type '%s'", targetFormalType.getName()));

            throw new JavaMethodInvocationException(errMsg);
        }
    }

    private static ReturnValue invoke(final Method method, final Object target, final Object[] args) {
        try {
            if (method.getDeclaringClass().getName().equals("java.util.stream.ReferencePipeline")) {
                // ReferencePipeline is not a public class, hence its methods can not be invoked
                // by reflection.
                return new ReturnValue(
                            invokeStreamMethod(method.getName(), target, args),
                            method.getReturnType());
            }
            else {
                return new ReturnValue(
                            method.invoke(target, args),
                            method.getReturnType());
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


    private static String noMatchingConstructorErrMsg(final Class<?> clazz, final Object[] args) {
        return new StringBuilder()
                        .append("No matching public constructor found: ")
                        .append(clazz.getName())
                        .append("(")
                        .append(formatArgTypes(args))
                        .append(")")
                        .toString();
    }

    private static String failedToGetBeanPropertyErrMsg(final Object target, final String propertyName) {
        return String.format(
                "Failed to get bean property '%s' on target '%s'",
                propertyName,
                target.getClass().getName());
    }

    private static String failedToSetBeanPropertyErrMsg(final Object target, final String propertyName) {
        return String.format(
                "Failed to set bean property '%s' on target '%s'",
                propertyName,
                target.getClass().getName());
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

    private static MethodHandle memoizedStaticFieldGet(final Class<?> clazz, final String fieldName) {
        return staticFieldCache.computeIfAbsent(
                    new Tuple2<>(clazz,fieldName),
                    k ->  MethodHandleUtil.staticField_get(k._1, k._2));
    }

    private static MethodHandle memoizedInstanceField(final Class<?> clazz, final String fieldName) {
        return instanceFieldCache.computeIfAbsent(
                    new Tuple2<>(clazz,fieldName),
                    k ->  MethodHandleUtil.instanceField_get(k._1, k._2));
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
                .map(o -> o == null ? "null" : o.getClass().getName())
                .collect(Collectors.joining(", "));
    }


    private static String formatMethodArgTypes(final Class<?>[] args) {
        return Arrays
                .stream(args)
                .map(o -> o.getName())
                .collect(Collectors.joining(", "));
    }


    private static final Map<String,Class<?>> classCache = new ConcurrentHashMap<>();
    private static final Map<Class<?>,List<String>> getterPropertiesCache = new ConcurrentHashMap<>();
    private static final Map<Class<?>,List<String>> setterPropertiesCache = new ConcurrentHashMap<>();
    private static final Map<Tuple2<Class<?>,String>,Method> getterMethodCache = new ConcurrentHashMap<>();
    private static final Map<Tuple2<Class<?>,String>,Method> setterMethodCache = new ConcurrentHashMap<>();
    private static final Map<Tuple2<Class<?>,Integer>,List<Constructor<?>>> constructorCache = new ConcurrentHashMap<>();
    private static final Map<Tuple2<Class<?>,String>,MethodHandle> staticFieldCache = new ConcurrentHashMap<>();
    private static final Map<Tuple2<Class<?>,String>,MethodHandle> instanceFieldCache = new ConcurrentHashMap<>();
    private static final Map<Tuple4<Class<?>,String,Integer,Boolean>,List<Method>> staticMethodCache = new ConcurrentHashMap<>();
    private static final Map<Tuple4<Class<?>,String,Integer,Boolean>,List<Method>> instanceMethodCache = new ConcurrentHashMap<>();
}
