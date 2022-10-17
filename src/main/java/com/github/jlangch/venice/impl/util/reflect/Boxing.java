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

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.jlangch.venice.JavaMethodInvocationException;

public class Boxing {

    public static Object[] boxArgs(final Class<?>[] params, final Object[] args) {
        if (params.length == 0) {
            return new Object[0];
        }

        final Object[] ret = new Object[params.length];
        for (int ii=0; ii<params.length; ii++) {
            ret[ii] = boxArg(params[ii], args[ii]);
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    public static Object boxArg(final Class<?> paramType, final Object arg) {
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
                        paramType.getName(),
                        arg.getClass().getName()));
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
        if (arg == null) {
            return null;
        }

        final Class<?> componentType = type.getComponentType();
        if (componentType == byte.class) {
            if (arg.getClass() == String.class) {
                return boxStringToByteArray((String)arg);
            }
            else if (arg.getClass() == byte[].class) {
                return arg;
            }
            else if (arg instanceof ByteBuffer) {
                return ((ByteBuffer)arg).array();
            }
        }
        else if (componentType == char.class) {
            if (arg.getClass() == String.class) {
                return ((String)arg).toCharArray();
            }
        }
        else {
            if (ReflectionTypes.isArrayType(arg.getClass())) {
                final Class<?> argComponentType = arg.getClass().getComponentType();

                if (componentType == argComponentType) {
                    return arg;
                }
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

}
