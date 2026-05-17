/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2026 Venice
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
package com.github.jlangch.venice.util.geo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.util.junit.EnableOnMac;
import com.github.jlangch.venice.util.geo.SwissCities.City;


@Disabled
public class SwissCitiesTest {

    @Test
    @EnableOnMac
    public void test_load() {
        final SwissCities data = SwissCities.downloadFromSwissTopo(true);

        final City maur =  data.findByOrtschaft("Maur").get(0);
        final City dubi =  data.findByOrtschaft("Dübendorf").get(0);

        final double dist1 = data.distance(maur, dubi);
        final double dist2 = data.distanceByPlz(maur.getPlz(), dubi.getPlz());

        System.out.println((long)dist1 + " km");
        System.out.println((long)dist2 + " km");
    }
}
