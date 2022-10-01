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

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.util.io.ClassPathResource;


public class GrepModuleTest {

    @SuppressWarnings("unchecked")
	@Test
    public void test_grep() {
        final Venice venice = new Venice();

        final Map<Object,Object> result = (Map<Object,Object>)venice.eval(
        										"grep-test.venice",
        										loadScript("grep-test.venice"));

        assertEquals( 0, (long)result.get("fail"));
        assertEquals( 2, (long)result.get("test"));
        assertEquals( 2, (long)result.get("pass"));
        assertEquals(25, (long)result.get("assert"));
        assertEquals( 0, (long)result.get("error"));
    }


    private String loadScript(final String name) {
        return new ClassPathResource(getClass().getPackage(), name).getResourceAsString("UTF-8");
    }

}
