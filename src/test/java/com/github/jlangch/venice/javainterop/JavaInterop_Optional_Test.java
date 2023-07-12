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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.JavaMethodInvocationException;
import com.github.jlangch.venice.Venice;


public class JavaInterop_Optional_Test {

    @Test
    public void test_optional_string() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                                            \n" +
                "  (import :com.github.jlangch.venice.javainterop.JavaInterop_Optional_Test$OptionalFunctions)  \n" +
                "                                                                                               \n" +
                "  (-> (. :JavaInterop_Optional_Test$OptionalFunctions :new)                                    \n" +
                "      (. :optionalString)                                                                      \n" +
                "      (java-unwrap-optional)))                                                                 \n";

        assertEquals("hello", venice.eval(script));
    }

    @Test
    public void test_optional_string_null() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                                            \n" +
                "  (import :com.github.jlangch.venice.javainterop.JavaInterop_Optional_Test$OptionalFunctions)  \n" +
                "                                                                                               \n" +
                "  (-> (. :JavaInterop_Optional_Test$OptionalFunctions :new)                                    \n" +
                "      (. :optionalStringNull)                                                                  \n" +
                "      (java-unwrap-optional)))                                                                 \n";

        assertEquals(null, venice.eval(script));
    }

    @Test
    public void test_optional_circle() {
        final Venice venice = new Venice();

        final String script1 =
                "(do                                                                                            \n" +
                "  (import :com.github.jlangch.venice.javainterop.JavaInterop_Optional_Test$OptionalFunctions)  \n" +
                "                                                                                               \n" +
                "  (-> (. :JavaInterop_Optional_Test$OptionalFunctions :new)                                    \n" +
                "      (. :optionalCircle)                                                                      \n" +
                "      (java-unwrap-optional)                                                                   \n" +
                "      (. :area)))                                                                              \n";

        final String script2 =
                "(do                                                                                            \n" +
                "  (import :com.github.jlangch.venice.javainterop.JavaInterop_Optional_Test$OptionalFunctions)  \n" +
                "                                                                                               \n" +
                "  (-> (. :JavaInterop_Optional_Test$OptionalFunctions :new)                                    \n" +
                "      (. :optionalCircle)                                                                      \n" +
                "      (java-unwrap-optional)                                                                   \n" +
                "      (. :radius)))                                                                              \n";

        assertEquals(7.06858D, (Double)venice.eval(script1), 0.00001D);
        assertEquals(1.5D,     (Double)venice.eval(script2), 0.00001D);
    }

    @Test
    public void test_optional_shape() {
        final Venice venice = new Venice();

        final String script1 =
                "(do                                                                                            \n" +
                "  (import :com.github.jlangch.venice.javainterop.JavaInterop_Optional_Test$OptionalFunctions)  \n" +
                "                                                                                               \n" +
                "  (-> (. :JavaInterop_Optional_Test$OptionalFunctions :new)                                    \n" +
                "      (. :optionalShape)                                                                       \n" +
                "      (java-unwrap-optional)                                                                   \n" +
                "      (. :area)))                                                                              \n";

        final String script2 =
                "(do                                                                                            \n" +
                "  (import :com.github.jlangch.venice.javainterop.JavaInterop_Optional_Test$OptionalFunctions)  \n" +
                "                                                                                               \n" +
                "  (-> (. :JavaInterop_Optional_Test$OptionalFunctions :new)                                    \n" +
                "      (. :optionalShape)                                                                       \n" +
                "      (java-unwrap-optional)                                                                   \n" +
                "      (. :radius)))                                                                              \n";

        assertEquals(7.06858D, (Double)venice.eval(script1), 0.00001D);

        // The unwrapped object of the formal type 'Shape' does not support the 'radius' method!
        assertThrows(JavaMethodInvocationException.class, () -> venice.eval(script2));
    }

//    @Test
//    public void test_optional_parameterized_type() throws Exception {
//        final Optional<Circle> ret1 = new OptionalFunctions().optionalCircle();
//
//        final List<Method> methods = ReflectionUtil.getAllPublicInstanceMethods(OptionalFunctions.class, false);
//
//        final Method method = methods.stream().filter(m -> m.getName().equals("optionalShape")).findFirst().get();
//
//        final Type type = method.getGenericReturnType();
//
//        for(Type t : ReflectionUtil.getTypeArguments(type)) {
//            System.out.println(t.getTypeName());
//            System.out.println(Class.forName(t.getTypeName()));
//        }
//
//        // see: JavaInteropUtil.convertToVncVal(..)
//
//    }

    public static class OptionalFunctions {

        public Optional<String> optionalString() {
            return Optional.of("hello");
        }

        public Optional<String> optionalStringNull() {
            return Optional.empty();
        }

        public Optional<Circle> optionalCircle() {
            return Optional.of(new Circle(1.5));
        }

        public Optional<Circle> optionalCircleNull() {
            return Optional.empty();
        }

        public Optional<Shape> optionalShape() {
            return Optional.of(new Circle(1.5));
        }

        public Optional<Shape> optionalShapeNull() {
            return Optional.empty();
        }
    }


    public static interface Shape {
        double area();
    }


    public static class Circle implements Shape {
        public Circle(double radius) {
            this.radius = radius;
        }

        @Override
        public double area() {
            return Math.PI * radius * radius;
        }

        public double radius() {
            return radius;
        }

        private final double radius;
    }
}
