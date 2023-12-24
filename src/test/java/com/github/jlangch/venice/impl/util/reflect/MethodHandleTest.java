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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.math.BigInteger;

import org.junit.jupiter.api.Test;


public class MethodHandleTest {

    @Test
    public void test_1() throws Throwable {
        final MethodHandles.Lookup caller = MethodHandles.lookup();

        final MethodHandle mh = caller.findConstructor(
                                    Long.class,
                                    MethodType.methodType(void.class, long.class));

        final Long l1 = (Long)mh.invoke(10L);

        assertEquals(10L, l1);
    }

    @Test
    public void test_2() throws Throwable {
        final MethodHandles.Lookup caller = MethodHandles.lookup();

        final MethodHandle mhValueOf = caller.findStatic(
                                            BigInteger.class,
                                            "valueOf",
                                            MethodType.methodType(BigInteger.class, long.class));

        final MethodHandle mhAdd = caller.findVirtual(
                                        BigInteger.class,
                                        "add",
                                        MethodType.methodType(BigInteger.class, BigInteger.class));


        final BigInteger i1 = (BigInteger)mhValueOf.invoke(10L);
        final BigInteger i2 = (BigInteger)mhValueOf.invoke(100L);
        final BigInteger sum = (BigInteger)mhAdd.invoke(i1, i2);

        assertEquals(110L, sum.longValue());
    }

}
