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

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;


public class JavaInterop_polymorph_Test {

    @Test
    public void test_geom_circle() {
        final Venice venice = new Venice();

        final Map<String, Object> symbols = Parameters.of("geom", new Geometry());

        final String script =
                "(do                                                                                     \n" +
                "   (import :com.github.jlangch.venice.javainterop.JavaInterop_polymorph_Test$Circle)    \n" +
                "                                                                                        \n" +
                "   (def circle (. :JavaInterop_polymorph_Test$Circle :new 3.0))                         \n" +
                "                                                                                        \n" +
                "   (. geom :area circle)                                                                \n" +
                ")";

        assertEquals(28.274D, (Double)venice.eval(script, symbols), 0.001D);
    }

    @Test
    public void test_geom_triangle() {
        final Venice venice = new Venice();

        final Map<String, Object> symbols = Parameters.of("geom", new Geometry());

        final String script =
                "(do                                                                                     \n" +
                "   (import :com.github.jlangch.venice.javainterop.JavaInterop_polymorph_Test$Triangle)  \n" +
                "                                                                                        \n" +
                "   (def triangle (. :JavaInterop_polymorph_Test$Triangle :new 3.0 4.0))                 \n" +
                "                                                                                        \n" +
                "   (. geom :area triangle)                                                              \n" +
                ")";

        assertEquals(6.0D, venice.eval(script, symbols));
    }

    @Test
    public void test_geom_rectangle() {
        final Venice venice = new Venice();

        final Map<String, Object> symbols = Parameters.of("geom", new Geometry());

        final String script =
                "(do                                                                                     \n" +
                "   (import :com.github.jlangch.venice.javainterop.JavaInterop_polymorph_Test$Rectangle) \n" +
                "                                                                                        \n" +
                "   (def rectangle (. :JavaInterop_polymorph_Test$Rectangle :new 3.0 4.0))               \n" +
                "                                                                                        \n" +
                "   (. geom :area rectangle)                                                             \n" +
                ")";

        assertEquals(12.0D, venice.eval(script, symbols));
    }

    @Test
    public void test_circle() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                                     \n" +
                "   (import :com.github.jlangch.venice.javainterop.JavaInterop_polymorph_Test$Circle)    \n" +
                "                                                                                        \n" +
                "   (def circle (. :JavaInterop_polymorph_Test$Circle :new 3.0))                         \n" +
                "                                                                                        \n" +
                "   (. circle :area )                                                                    \n" +
                ")";

        assertEquals(28.274D, (Double)venice.eval(script), 0.001D);
    }

    @Test
    public void test_triangle() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                                     \n" +
                "   (import :com.github.jlangch.venice.javainterop.JavaInterop_polymorph_Test$Triangle)  \n" +
                "                                                                                        \n" +
                "   (def triangle (. :JavaInterop_polymorph_Test$Triangle :new 3.0 4.0))                 \n" +
                "                                                                                        \n" +
                "   (. triangle :area)                                                                   \n" +
                ")";

        assertEquals(6.0D, venice.eval(script));
    }

    @Test
    public void test_rectangle() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                                     \n" +
                "   (import :com.github.jlangch.venice.javainterop.JavaInterop_polymorph_Test$Rectangle) \n" +
                "                                                                                        \n" +
                "   (def rectangle (. :JavaInterop_polymorph_Test$Rectangle :new 3.0 4.0))               \n" +
                "                                                                                        \n" +
                "   (. rectangle :area)                                                                  \n" +
                ")";

        assertEquals(12.0D, venice.eval(script));
    }



    public static class Geometry {
        public Geometry() {
        }

        public double area(final IShape shape) {
            return shape.area();
        }
    }

    public static interface IValidate {
        public void validate();
    }

    public static interface IScalable {
        public IScalable scale(double factor);
    }

    public static interface IShape extends IScalable, IValidate  {
        public double area();
    }

    public static abstract class Shape implements IShape {
        public Shape(final double width, final double height) {
            this.width = width;
            this.height = height;
        }

        public double getWidth() {
            return width;
        }

        public double getHeight() {
            return height;
        }

        private final double width;
        private final double height;
    }

    public static class Circle extends Shape {
        public Circle(final double radius) {
            super(radius, radius);
        }

        @Override
        public double area() {
            return getWidth() * getWidth() * Math.PI;
        }

        @Override
        public IScalable scale(final double factor) {
            return new Circle(getWidth() * factor);
        }

        @Override
        public void validate() {
        }
    }

    public static class Triangle extends Shape {
        public Triangle(final double width, final double height) {
            super(width, height);
        }

        @Override
        public double area() {
            return getWidth() * getHeight() / 2.0;
        }

        @Override
        public IScalable scale(final double factor) {
            return new Triangle(getWidth() * factor, getHeight() * factor);
        }

        @Override
        public void validate() {
        }
    }

    public static class Rectangle extends Shape {
        public Rectangle(final double width, final double height) {
            super(width, height);
        }

        @Override
        public double area() {
            return getWidth() * getHeight();
        }

        @Override
        public IScalable scale(final double factor) {
            return new Rectangle(getWidth() * factor, getHeight() * factor);
        }

        @Override
        public void validate() {
        }
    }

}
