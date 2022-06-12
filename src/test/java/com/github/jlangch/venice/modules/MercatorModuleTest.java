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
package com.github.jlangch.venice.modules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class MercatorModuleTest {

    @Test
    public void test_spherical() {
        final Venice venice = new Venice();

        final String script =
                "(do                                      " +
                "   (load-module :mercator)               " +
                "                                         " +
                "   [ (mercator/spherical-x-axis 22)      " +
                "     (mercator/spherical-y-axis 44) ] )  ";

        @SuppressWarnings("unchecked")
        final List<Object> map = (List<Object>)venice.eval(script);

        assertEquals(
                2449028.7974520186D,
                (Double)map.get(0),
                0.0000000001D);

        assertEquals(
                5465442.183322753D,
                (Double)map.get(1),
                0.000000001D);
    }

    @Test
    public void test_elliptical() {
        final Venice venice = new Venice();

        final String script =
                "(do                                      " +
                "   (load-module :mercator)               " +
                "                                         " +
                "   [ (mercator/elliptical-x-axis 22)     " +
                "     (mercator/elliptical-y-axis 44) ] ) ";

        @SuppressWarnings("unchecked")
        final List<Object> map = (List<Object>)venice.eval(script);

        assertEquals(
                2449028.7974520186D,
                (Double)map.get(0),
                0.0000000001D);

        assertEquals(
                5435749.887511954D,
                (Double)map.get(1),
                0.000000001D);
    }

}
