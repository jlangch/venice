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
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

import com.github.jlangch.venice.JavaMethodInvocationException;


public class MethodHandleUtil {

    // ------------------------------------------------------------------------
    // instance Fields
    // ------------------------------------------------------------------------

    public static MethodHandle instanceField_get(final Field field) {
        try {
            // fields are not supported by LambdaMetafactory
            final MethodHandles.Lookup caller = MethodHandles.lookup();
            return caller.unreflectGetter(field);
        }
        catch (Throwable ex) {
            throw new JavaMethodInvocationException(
                        "Could not generate the function to access the instance field " + field.getName()
                            + " on " + field.getDeclaringClass().getName(),
                        ex);
        }
    }

    public static MethodHandle instanceField_get(final Class<?> clazz, final String fieldName) {
        try {
            // fields are not supported by LambdaMetafactory
            final MethodHandles.Lookup caller = MethodHandles.lookup();
            return caller.unreflectGetter(clazz.getField(fieldName));
        }
        catch (Throwable ex) {
            throw new JavaMethodInvocationException(
                        "Could not generate the function to access the instance field " + fieldName
                            + " on " + clazz.getName(),
                        ex);
        }
    }

    public static MethodHandle instanceField_set(final Field field) {
        try {
            // fields are not supported by LambdaMetafactory
            final MethodHandles.Lookup caller = MethodHandles.lookup();
            return caller.unreflectSetter(field);
        }
        catch (Throwable ex) {
            throw new JavaMethodInvocationException(
                        "Could not generate the function to access the instance field "
                            + field.getName() + " on " + field.getDeclaringClass().getName(),
                        ex);
        }
    }

    public static MethodHandle instanceField_set(final Class<?> clazz, final String fieldName) {
        try {
            // fields are not supported by LambdaMetafactory
            final MethodHandles.Lookup caller = MethodHandles.lookup();
            return caller.unreflectSetter(clazz.getField(fieldName));
        }
        catch (Throwable ex) {
            throw new JavaMethodInvocationException(
                        "Could not generate the function to access the instance field "
                            + fieldName + " on " + clazz.getName(),
                        ex);
        }
    }



    // ------------------------------------------------------------------------
    // static Fields
    // ------------------------------------------------------------------------

    public static MethodHandle staticField_get(final Field field) {
        try {
            // fields are not supported by LambdaMetafactory
            final MethodHandles.Lookup caller = MethodHandles.lookup();
            return caller.unreflectGetter(field);
        }
        catch (Throwable ex) {
            throw new JavaMethodInvocationException(
                        "Could not generate the function to access the static field " + field.getName()
                            + " on " + field.getDeclaringClass().getName(),
                        ex);
        }
    }

    public static MethodHandle staticField_get(final Class<?> clazz, final String fieldName) {
        try {
            // fields are not supported by LambdaMetafactory
            final MethodHandles.Lookup caller = MethodHandles.lookup();
            return caller.unreflectGetter(clazz.getField(fieldName));
        }
        catch (Throwable ex) {
            throw new JavaMethodInvocationException(
                        "Could not generate the function to access the static field " + fieldName
                            + " on " + clazz.getName(),
                        ex);
        }
    }

    public static MethodHandle staticField_set(final Field field) {
        try {
            // fields are not supported by LambdaMetafactory
            final MethodHandles.Lookup caller = MethodHandles.lookup();
            return caller.unreflectSetter(field);
        }
        catch (Throwable ex) {
            throw new JavaMethodInvocationException(
                        "Could not generate the function to access the static field " + field.getName()
                            + " on " + field.getDeclaringClass().getName(),
                        ex);
        }
    }

    public static MethodHandle staticField_set(final Class<?> clazz, final String fieldName) {
        try {
            // fields are not supported by LambdaMetafactory
            final MethodHandles.Lookup caller = MethodHandles.lookup();
            return caller.unreflectSetter(clazz.getField(fieldName));
        }
        catch (Throwable ex) {
            throw new JavaMethodInvocationException(
                        "Could not generate the function to access the static field " + fieldName
                            + " on " + clazz.getName(),
                        ex);
        }
    }

}
