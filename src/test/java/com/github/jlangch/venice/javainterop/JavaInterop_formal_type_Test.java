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
package com.github.jlangch.venice.javainterop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class JavaInterop_formal_type_Test {


    @Test
    public void test_Graphics2D() {
        final Venice venice = new Venice();

        // BufferedImage::createGraphics() returns effectively an object of type 'sun.java2d.SunGraphics2D'
        // The API defines the return type as 'java.awt.Graphics2D' (the formal type)

        // On Java 9+ :fillOval can not be called on 'sun.java2d.SunGraphics2D' without severe warnings
        // because 'sun.**' class are not on the public module path!!

        // WARNING: An illegal reflective access operation has occurred
        // WARNING: Illegal reflective access by com.github.jlangch.venice.impl.util.reflect.ReflectionAccessor (file:/Users/.../venice/build/classes/java/main/) to method sun.java2d.SunGraphics2D.fillOval(int,int,int,int)
        // WARNING: Please consider reporting this to the maintainers of com.github.jlangch.venice.impl.util.reflect.ReflectionAccessor
        // WARNING: Use --illegal-access=warn to enable warnings of further illegal reflective access operations
        // WARNING: All illegal access operations will be denied in a future release

        // Reflection must be done on the formal type! Venice handles implicitly with Version 1.7.17+

        final String script =
                "(do                                                  \n" +
                "   (import :java.awt.image.BufferedImage)            \n" +
                "   (import :java.awt.Graphics2D)                     \n" +
                "                                                     \n" +
                "   (let [img (. :BufferedImage :new 40 40 1)         \n" +
                "         g2d (. img :createGraphics)]                \n" +
                "     (. g2d :fillOval 10 20 5 5)                     \n" +
                "     img))                                             ";

        final BufferedImage img = (BufferedImage)venice.eval(script);
        assertEquals(40, img.getWidth());
    }

    @Test
    public void test_cast() {
        final Venice venice = new Venice();

        final String script =
                "(->> (. :java.awt.Point :new 0 0)  \n" +
                "     (cast :java.lang.Object)      \n" +
                "     (formal-type)                 \n" +
                "     (class))                      ";

        assertEquals(java.lang.Object.class, venice.eval(script));
    }

    @Test
    public void test_remove_formal_type() {
        final Venice venice = new Venice();

        final String script1 =
                "(->> (. :java.awt.Point :new 0 0)  \n" +
                "     (cast :java.lang.Object)      \n" +
                "     (formal-type)                 \n" +
                "     (class))                      ";

        assertEquals(java.lang.Object.class, venice.eval(script1));


        final String script2 =
                "(->> (. :java.awt.Point :new 0 0)  \n" +
                "     (cast :java.lang.Object)      \n" +
                "     (remove-formal-type)          \n" +
                "     (formal-type)                 \n" +
                "     (class))                      ";

        assertEquals(java.awt.Point.class, venice.eval(script2));
    }

}
