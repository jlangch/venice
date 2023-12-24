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
package com.github.jlangch.venice.impl.types.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.VncException;


public class QualifiedNameTest {

    @Test
    public void testParser_EdgeCases() {
        assertThrows(VncException.class, () -> QualifiedName.parse(null));
        assertThrows(VncException.class, () -> QualifiedName.parse(""));
    }

    @Test
    public void testParser_Invalid() {
        assertThrows(VncException.class, () -> QualifiedName.parse("//"));
        assertThrows(VncException.class, () -> QualifiedName.parse("///b"));
        assertThrows(VncException.class, () -> QualifiedName.parse("/a/b"));
        assertThrows(VncException.class, () -> QualifiedName.parse("/a//b"));
        assertThrows(VncException.class, () -> QualifiedName.parse("a/a/b"));
   }

    @Test
    public void testParser_SpecialCases() {
        // special case:  core function "/" (division)
        final QualifiedName qn1 = QualifiedName.parse("/");
        assertEquals(null, qn1.getNamespace());
        assertEquals("/",  qn1.getQualifiedName());
        assertEquals("/",  qn1.getSimpleName());
        assertFalse(qn1.isQualified());


        // special case:  qualified core function "/" (division)
        final QualifiedName qn2 = QualifiedName.parse("core//");
        assertEquals("core",   qn2.getNamespace());
        assertEquals("core//", qn2.getQualifiedName());
        assertEquals("/",      qn2.getSimpleName());
        assertTrue(qn2.isQualified());
    }

    @Test
    public void testParser() {
        // unqualified core function "+"
        final QualifiedName qn1 = QualifiedName.parse("+");
        assertEquals(null, qn1.getNamespace());
        assertEquals("+",  qn1.getQualifiedName());
        assertEquals("+",  qn1.getSimpleName());
        assertFalse(qn1.isQualified());

        // qualified core function "+"
        final QualifiedName qn2 = QualifiedName.parse("core/+");
        assertEquals("core",   qn2.getNamespace());
        assertEquals("core/+", qn2.getQualifiedName());
        assertEquals("+",      qn2.getSimpleName());
        assertTrue(qn2.isQualified());

        // unqualified symbol "foo"
        final QualifiedName qn3 = QualifiedName.parse("foo");
        assertEquals(null,  qn3.getNamespace());
        assertEquals("foo", qn3.getQualifiedName());
        assertEquals("foo", qn3.getSimpleName());
        assertFalse(qn3.isQualified());

        // qualified core function "v/foo"
        final QualifiedName qn4 = QualifiedName.parse("v/foo");
        assertEquals("v",     qn4.getNamespace());
        assertEquals("v/foo", qn4.getQualifiedName());
        assertEquals("foo",   qn4.getSimpleName());
        assertTrue(qn4.isQualified());
   }

}
