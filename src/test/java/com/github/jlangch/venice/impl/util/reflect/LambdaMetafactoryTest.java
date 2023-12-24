/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


public class LambdaMetafactoryTest {

    @Test
    @Disabled  // not working on Java 11 (primitive type)
    public void test() throws Exception {
        Method mValueOf = BigInteger.class.getDeclaredMethod("valueOf", long.class);
        Method mAdd = BigInteger.class.getDeclaredMethod("add", BigInteger.class);

        Function<Long,BigInteger> fnValueOf = compileValueOf(mValueOf);
        BiFunction<BigInteger,BigInteger,BigInteger> fnAdd = compileAdd(mAdd);

        BigInteger i1 = fnValueOf.apply(10L);
        BigInteger i2 = fnValueOf.apply(100L);
        BigInteger sum = fnAdd.apply(i1, i2);

        assertEquals(110L, sum.longValue());
    }


    private Function<Long,BigInteger> compileValueOf(final Method method) {
        try {
            final MethodHandles.Lookup caller = MethodHandles.lookup();
            final MethodHandle handle = caller.unreflect(method);

            return (Function<Long,BigInteger>)LambdaMetafactory
                    .metafactory(
                        caller,
                        "apply",
                        MethodType.methodType(Function.class),
                        MethodType.methodType(Object.class, Object.class), // type erasure on SAM!
                        handle,
                        handle.type())
                    .getTarget()
                    .invoke();
        }
        catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    private BiFunction<BigInteger,BigInteger,BigInteger> compileAdd(final Method method) {
        try {
            final MethodHandles.Lookup caller = MethodHandles.lookup();
            final MethodHandle handle = caller.unreflect(method);

            return (BiFunction<BigInteger,BigInteger,BigInteger>)LambdaMetafactory
                    .metafactory(
                        caller,
                        "apply",
                        MethodType.methodType(BiFunction.class),
                        MethodType.methodType(Object.class, Object.class, Object.class), // type erasure on SAM!
                        handle,
                        handle.type())
                    .getTarget()
                    .invoke();
        }
        catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }
}
