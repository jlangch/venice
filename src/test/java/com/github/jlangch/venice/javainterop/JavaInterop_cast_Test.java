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
package com.github.jlangch.venice.javainterop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.JavaMethodInvocationException;
import com.github.jlangch.venice.Venice;


public class JavaInterop_cast_Test {

    @Test
    public void test_cast_1() {
        final Venice venice = new Venice();

        final String script =
                "(->> (. :java.awt.Point :new 0 0)  \n" +
                "     (cast :java.lang.Object)      \n" +
                "     (formal-type)                 \n" +
                "     (class))                      ";

        assertEquals(java.lang.Object.class, venice.eval(script));
    }

    @Test
    public void test_cast_2a() {
        final Venice venice = new Venice();

        final String script =
                "(->> (. :java.awt.Point :new 0 0)  \n" +
                "     (cast :java.lang.Object)      \n" +
                "     (cast :java.awt.Point)        \n" +
                "     (formal-type)                 \n" +
                "     (class))                      ";

        assertEquals(java.awt.Point.class, venice.eval(script));
    }

    @Test
    public void test_cast_2b() {
        final Venice venice = new Venice();

        final String script =
                "(-<> (. :java.awt.Point :new 0 0)  \n" +
                "     (cast :java.lang.Object <>)   \n" +
                "     (. <> :getX))                 ";

        assertThrows(JavaMethodInvocationException.class, () -> venice.eval(script));
    }

    @Test
    public void test_cast_2c() {
        final Venice venice = new Venice();

        // java.awt.geom.Point2D defines:  abstract double getX()
        final String script =
                "(-<> (. :java.awt.Point :new 0 0)         \n" +
                "     (cast :java.awt.geom.Point2D <>)     \n" +
                "     (. <> :getX))                        ";

        assertEquals(0.0, venice.eval(script));
    }

    @Test
    public void test_cast_2d() {
        final Venice venice = new Venice();

        final String script =
                "(-<> (. :java.awt.Point :new 0 0)  \n" +
                "     (cast :java.lang.Object <>)   \n" +
                "     (cast :java.awt.Point <>)     \n" +
                "     (. <> :getX))                 ";

        assertEquals(0.0, venice.eval(script));
    }
}
