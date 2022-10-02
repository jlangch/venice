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

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.util.io.ClassPathResource;


public class TestModuleTest {

    @Test
    public void test_1() {
        final Map<String,Long> result = run("test-module-test-1.venice");

        assertEquals( 4L, result.get("test"));

        assertEquals( 2L, result.get("pass"));
        assertEquals( 2L, result.get("fail"));
        assertEquals( 0L, result.get("error"));

        assertEquals( 4L, result.get("assert"));
    }

    @Test
    public void test_2() {
        final Map<String,Long> result = run("test-module-test-2.venice");

        assertEquals( 4L, result.get("test"));

        assertEquals( 2L, result.get("pass"));
        assertEquals( 2L, result.get("fail"));
        assertEquals( 0L, result.get("error"));

        assertEquals( 4L, result.get("assert"));
    }

    @Test
    public void test_3() {
        final Map<String,Long> result = run("test-module-test-3.venice");

        assertEquals( 1L, result.get("test"));

        assertEquals( 0L, result.get("pass"));
        assertEquals( 1L, result.get("fail"));
        assertEquals( 0L, result.get("error"));

        assertEquals( 1L, result.get("assert"));
    }

    @Test
    public void test_4() {
        final Map<String,Long> result = run("test-module-test-4.venice");

        assertEquals( 1L, result.get("test"));

        assertEquals( 1L, result.get("pass"));
        assertEquals( 0L, result.get("fail"));
        assertEquals( 0L, result.get("error"));

        assertEquals( 1L, result.get("assert"));
    }

    @Test
    public void test_5() {
        final Map<String,Long> result = run("test-module-test-5.venice");

        assertEquals( 1L, result.get("test"));

        assertEquals( 1L, result.get("pass"));
        assertEquals( 0L, result.get("fail"));
        assertEquals( 0L, result.get("error"));

        assertEquals( 2L, result.get("assert"));
    }

    @Test
    public void test_6() {
        final Map<String,Long> result = run("test-module-test-6.venice");

        assertEquals( 1L, result.get("test"));

        assertEquals( 1L, result.get("pass"));
        assertEquals( 0L, result.get("fail"));
        assertEquals( 0L, result.get("error"));

        assertEquals( 2L, result.get("assert"));
    }

    @Test
    public void test_7() {
        final Map<String,Long> result = run("test-module-test-7.venice");

        assertEquals( 1L, result.get("test"));

        assertEquals( 1L, result.get("pass"));
        assertEquals( 0L, result.get("fail"));
        assertEquals( 0L, result.get("error"));

        assertEquals( 2L, result.get("assert"));
    }



    @SuppressWarnings("unchecked")
    private Map<String,Long> run(final String file) {
        final Map<Object,Object> tmp = (Map<Object,Object>)new Venice().eval(file, loadScript(file));

        final Map<String,Long> result = new HashMap<>();
        result.put("test",   (Long)tmp.get("test"));
        result.put("pass",   (Long)tmp.get("pass"));
        result.put("fail",   (Long)tmp.get("fail"));
        result.put("error",  (Long)tmp.get("error"));
        result.put("assert", (Long)tmp.get("assert"));
        return result;
    }

    private String loadScript(final String name) {
        return new ClassPathResource(getClass().getPackage(), name).getResourceAsString("UTF-8");
    }
}
