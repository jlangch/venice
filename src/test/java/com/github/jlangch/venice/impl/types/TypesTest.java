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
package com.github.jlangch.venice.impl.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.types.util.Types;


public class TypesTest {

    @Test
    public void test_isVncJavaObject() {
        final VncJavaObject javaObj = new VncJavaObject(Long.valueOf(100));

        assertTrue(Types.isVncJavaObject(javaObj));
        assertTrue(Types.isVncJavaObject(javaObj, Long.class));
        assertTrue(Types.isVncJavaObject(javaObj, Number.class));
        assertTrue(Types.isVncJavaObject(javaObj, Object.class));
        assertFalse(Types.isVncJavaObject(javaObj, Integer.class));
    }

    @Test
    public void test_Type() {
        assertEquals(":core/nil", Types.getType(Constants.Nil).toString(true));

        assertEquals(":core/boolean", Types.getType(VncBoolean.True).toString(true));
        assertEquals(":core/boolean", Types.getType(VncBoolean.False).toString(true));

        assertEquals(":core/integer", Types.getType(new VncInteger(1)).toString(true));
        assertEquals(":core/long", Types.getType(new VncLong(1L)).toString(true));
        assertEquals(":core/double", Types.getType(new VncDouble(1D)).toString(true));
        assertEquals(":core/decimal", Types.getType(new VncBigDecimal(1L)).toString(true));
        assertEquals(":core/bigint", Types.getType(new VncBigInteger(1L)).toString(true));

        assertEquals(":core/char", Types.getType(new VncChar('a')).toString(true));
        assertEquals(":core/string", Types.getType(new VncString("abc")).toString(true));

        assertEquals(":core/keyword", Types.getType(new VncKeyword("x")).toString(true));
        assertEquals(":core/symbol", Types.getType(new VncSymbol("x")).toString(true));
    }

    @Test
    public void test_Supertype() {
        assertEquals(":core/val", Types.getSupertype(Constants.Nil).toString(true));

        assertEquals(":core/number", Types.getSupertype(new VncLong(1L)).toString(true));
    }

    @Test
    public void test_Supertypes() {
        assertEquals(":core/val", Types.getSupertypes(Constants.Nil).first().toString(true));
        assertEquals(1L, Types.getSupertypes(Constants.Nil).size());

        assertEquals(":core/number", Types.getSupertypes(new VncLong(1L)).first().toString(true));
        assertEquals(":core/val", Types.getSupertypes(new VncLong(1L)).second().toString(true));
        assertEquals(2L, Types.getSupertypes(new VncLong(1L)).size());
    }
}
