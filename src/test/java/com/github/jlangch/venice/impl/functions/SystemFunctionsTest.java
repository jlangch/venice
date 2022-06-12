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
package com.github.jlangch.venice.impl.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class SystemFunctionsTest {

    @Test
    public void test_current_time_millis() {
        final Venice venice = new Venice();

        assertTrue((Long)venice.eval("(current-time-millis)") > 0);
    }

    @Test
    public void test_nano_time() {
        final Venice venice = new Venice();

        assertTrue((Long)venice.eval("(nano-time)") > 0);
    }

    @Test
    public void test_sleep() {
        final Venice venice = new Venice();

        assertNull(venice.eval("(sleep 30)"));
    }

    @Test
    public void test_sandboxed() {
        final Venice venice = new Venice();

        assertFalse((boolean)venice.eval("(sandboxed?)"));
    }

    @Test
    public void test_system_prop() {
        final Venice venice = new Venice();

        assertNotNull(venice.eval("(system-prop :os.name)"));
        assertNull(venice.eval("(system-prop :foo.org)"));
        assertEquals("abc", venice.eval("(system-prop :foo.org \"abc\")"));
    }

    @Test
    public void test_java_version() {
        final Venice venice = new Venice();

        assertNotNull(venice.eval("(java-version)"));
        assertEquals(System.getProperty("java.version"), venice.eval("(java-version)"));
    }

    @Test
    public void test_java_major_version() {
        final Venice venice = new Venice();

        final Long version = (Long)venice.eval("(java-major-version)");
        assertTrue(version > 0L);
    }

    @Test
    public void test_java_version_info() {
        final Venice venice = new Venice();

        assertNotNull(venice.eval("(java-version-info)"));
    }

    @Test
    public void test_uuid() {
        final Venice venice = new Venice();

        assertNotNull(venice.eval("(uuid)"));
    }

    @Test
    public void test_version() {
        final Venice venice = new Venice();

        final String version = (String)venice.eval("(version)");

        assertTrue(version.matches("[0-9]+[.][0-9]+[.][0-9]+(-snapshot)*"));
    }

    @Test
    public void test_callstack() {
        final Venice venice = new Venice();

        final String s =
                "(do                                                     \n" +
                "   (def cs (atom nil))                                  \n" +
                "   (defn f1 [x] (f2 x))                                 \n" +
                "   (defn f2 [x] (f3 x))                                 \n" +
                "   (defn f3 [x] (f4 x))                                 \n" +
                "   (defn f4 [x] (f5 x))                                 \n" +
                "   (defn f5 [x] (do (reset! cs (callstack)) x))         \n" +
                "   (f1 1)                                               \n" +
                "   @cs)                                                   ";

        @SuppressWarnings("unchecked")
        final List<String> callstack = (List<String>)venice.eval("test", s);

        assertEquals(6, callstack.size());
    }
}
