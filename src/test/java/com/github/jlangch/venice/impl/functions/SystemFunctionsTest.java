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
package com.github.jlangch.venice.impl.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.util.junit.EnableOnLinux;
import com.github.jlangch.venice.impl.util.junit.EnableOnMac;
import com.github.jlangch.venice.impl.util.junit.EnableOnWindows;


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
    public void test_java_home() {
        final Venice venice = new Venice();

        assertNotNull(venice.eval("(java-home)"));
        assertEquals(System.getProperty("java.home"), venice.eval("(java-home)"));
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
    public void test_latest() {
        final Venice venice = new Venice();

        final String version = (String)venice.eval("(latest)");

        // internet connection is required!
        if (version != null) {
            assertTrue(version.matches("[0-9]+[.][0-9]+[.][0-9]+(-snapshot)*"));
        }
    }

    @Test
    public void test_parse_version_1() {
        final Venice venice = new Venice();

        @SuppressWarnings("unchecked")
        Map<String,Object> v1 = (Map<String,Object>)venice.eval("(parse-version \"2\")");
        assertEquals(2L, v1.get("major"));
        assertEquals(0L, v1.get("minor"));
        assertEquals(0L, v1.get("patch"));
        assertEquals("", v1.get("suffix"));

        @SuppressWarnings("unchecked")
        Map<String,Object> v2 = (Map<String,Object>)venice.eval("(parse-version \"456\")");
        assertEquals(456L, v2.get("major"));
        assertEquals(0L, v2.get("minor"));
        assertEquals(0L, v2.get("patch"));
        assertEquals("", v2.get("suffix"));
    }

    @Test
    public void test_parse_version_2() {
        final Venice venice = new Venice();

        @SuppressWarnings("unchecked")
        final Map<String,Object> v1 = (Map<String,Object>)venice.eval("(parse-version \"2.3\")");
        assertEquals(2L, v1.get("major"));
        assertEquals(3L, v1.get("minor"));
        assertEquals(0L, v1.get("patch"));
        assertEquals("", v1.get("suffix"));

        @SuppressWarnings("unchecked")
        final Map<String,Object> v2 = (Map<String,Object>)venice.eval("(parse-version \"23.114\")");
        assertEquals(23L, v2.get("major"));
        assertEquals(114L, v2.get("minor"));
        assertEquals(0L, v2.get("patch"));
        assertEquals("", v2.get("suffix"));
    }

    @Test
    public void test_parse_version_3() {
        final Venice venice = new Venice();

        @SuppressWarnings("unchecked")
        final Map<String,Object> v1 = (Map<String,Object>)venice.eval("(parse-version \"2.3.4\")");
        assertEquals(2L, v1.get("major"));
        assertEquals(3L, v1.get("minor"));
        assertEquals(4L, v1.get("patch"));
        assertEquals("", v1.get("suffix"));

        @SuppressWarnings("unchecked")
        final Map<String,Object> v2 = (Map<String,Object>)venice.eval("(parse-version \"23.114.9\")");
        assertEquals(23L, v2.get("major"));
        assertEquals(114L, v2.get("minor"));
        assertEquals(9L, v2.get("patch"));
        assertEquals("", v2.get("suffix"));
    }

    @Test
    public void test_parse_version_4() {
        final Venice venice = new Venice();

        @SuppressWarnings("unchecked")
        final Map<String,Object> v1 = (Map<String,Object>)venice.eval("(parse-version \"2.3.4-pre\")");
        assertEquals(2L, v1.get("major"));
        assertEquals(3L, v1.get("minor"));
        assertEquals(4L, v1.get("patch"));
        assertEquals("pre", v1.get("suffix"));

        @SuppressWarnings("unchecked")
        final Map<String,Object> v2 = (Map<String,Object>)venice.eval("(parse-version \"20.35.100-pre-1\")");
        assertEquals(20L, v2.get("major"));
        assertEquals(35L, v2.get("minor"));
        assertEquals(100L, v2.get("patch"));
        assertEquals("pre-1", v2.get("suffix"));

        @SuppressWarnings("unchecked")
        final Map<String,Object> v3 = (Map<String,Object>)venice.eval("(parse-version \"20.35.100-pre.1\")");
        assertEquals(20L, v3.get("major"));
        assertEquals(35L, v3.get("minor"));
        assertEquals(100L, v3.get("patch"));
        assertEquals("pre.1", v3.get("suffix"));
    }

    @Test
    public void test_newer_version_1() {
        final Venice venice = new Venice();

        assertFalse((Boolean)venice.eval("(newer-version? \"1\" \"2\")"));
        assertFalse((Boolean)venice.eval("(newer-version? \"2\" \"2\")"));
        assertTrue( (Boolean)venice.eval("(newer-version? \"3\" \"2\")"));

        assertFalse((Boolean)venice.eval("(newer-version? \"1.0\" \"2\")"));
        assertFalse((Boolean)venice.eval("(newer-version? \"2.0\" \"2\")"));
        assertTrue( (Boolean)venice.eval("(newer-version? \"2.1\" \"2\")"));
        assertTrue( (Boolean)venice.eval("(newer-version? \"3.1\" \"2\")"));

        assertFalse((Boolean)venice.eval("(newer-version? \"1.0.0\" \"2\")"));
        assertFalse((Boolean)venice.eval("(newer-version? \"2.0.0\" \"2\")"));
        assertTrue( (Boolean)venice.eval("(newer-version? \"2.0.1\" \"2\")"));
        assertTrue( (Boolean)venice.eval("(newer-version? \"3.1.8\" \"2\")"));

        assertFalse((Boolean)venice.eval("(newer-version? \"1.0.0-alpha\" \"2\")"));
        assertFalse((Boolean)venice.eval("(newer-version? \"2.0.0-alpha\" \"2\")"));
        assertTrue( (Boolean)venice.eval("(newer-version? \"2.0.1-alpha\" \"2\")"));
        assertTrue( (Boolean)venice.eval("(newer-version? \"3.1.8-alpha\" \"2\")"));
    }

    @Test
    public void test_newer_version_2() {
        final Venice venice = new Venice();

        assertFalse((Boolean)venice.eval("(newer-version? \"1\" \"2.0\")"));
        assertFalse((Boolean)venice.eval("(newer-version? \"2\" \"2.0\")"));
        assertTrue( (Boolean)venice.eval("(newer-version? \"3\" \"2.0\")"));

        assertFalse((Boolean)venice.eval("(newer-version? \"1.0\" \"2.0\")"));
        assertFalse((Boolean)venice.eval("(newer-version? \"2.0\" \"2.0\")"));
        assertTrue( (Boolean)venice.eval("(newer-version? \"2.1\" \"2.0\")"));
        assertFalse((Boolean)venice.eval("(newer-version? \"2.1\" \"2.1\")"));
        assertFalse((Boolean)venice.eval("(newer-version? \"2.1\" \"2.2\")"));
        assertTrue( (Boolean)venice.eval("(newer-version? \"3.1\" \"2.0\")"));

        assertFalse((Boolean)venice.eval("(newer-version? \"1.0.0\" \"2.0\")"));
        assertFalse((Boolean)venice.eval("(newer-version? \"2.0.0\" \"2.0\")"));
        assertTrue( (Boolean)venice.eval("(newer-version? \"2.0.1\" \"2.0\")"));
        assertTrue( (Boolean)venice.eval("(newer-version? \"2.1.2\" \"2.1\")"));
        assertFalse((Boolean)venice.eval("(newer-version? \"2.2.2\" \"2.3\")"));
        assertTrue( (Boolean)venice.eval("(newer-version? \"3.1.8\" \"2.0\")"));

        assertFalse((Boolean)venice.eval("(newer-version? \"1.0.0-alpha\" \"2.0\")"));
        assertFalse((Boolean)venice.eval("(newer-version? \"2.0.0-alpha\" \"2.0\")"));
        assertTrue( (Boolean)venice.eval("(newer-version? \"2.0.1-alpha\" \"2.0\")"));
        assertTrue( (Boolean)venice.eval("(newer-version? \"2.1.2-alpha\" \"2.1\")"));
        assertFalse((Boolean)venice.eval("(newer-version? \"2.2.2-alpha\" \"2.3\")"));
        assertTrue( (Boolean)venice.eval("(newer-version? \"3.1.8-alpha\" \"2.0\")"));
    }

    @Test
    public void test_newer_version_3() {
        final Venice venice = new Venice();

        assertFalse((Boolean)venice.eval("(newer-version? \"1\" \"2.0.1\")"));
        assertFalse((Boolean)venice.eval("(newer-version? \"2\" \"2.0.0\")"));
        assertFalse((Boolean)venice.eval("(newer-version? \"2\" \"2.0.1\")"));
        assertTrue( (Boolean)venice.eval("(newer-version? \"3\" \"2.0.1\")"));

        assertFalse((Boolean)venice.eval("(newer-version? \"1.0\" \"2.0.1\")"));
        assertFalse((Boolean)venice.eval("(newer-version? \"2.0\" \"2.0.0\")"));
        assertFalse((Boolean)venice.eval("(newer-version? \"2.0\" \"2.0.1\")"));
        assertTrue( (Boolean)venice.eval("(newer-version? \"2.1\" \"2.0.9\")"));
        assertFalse((Boolean)venice.eval("(newer-version? \"2.1\" \"2.1.0\")"));
        assertFalse((Boolean)venice.eval("(newer-version? \"2.1\" \"2.2.3\")"));
        assertTrue( (Boolean)venice.eval("(newer-version? \"3.1\" \"2.0.7\")"));

        assertFalse((Boolean)venice.eval("(newer-version? \"1.0.0\" \"2.0.1\")"));
        assertFalse((Boolean)venice.eval("(newer-version? \"2.0.0\" \"2.0.1\")"));
        assertFalse((Boolean)venice.eval("(newer-version? \"2.0.1\" \"2.0.1\")"));
        assertTrue( (Boolean)venice.eval("(newer-version? \"2.0.2\" \"2.0.1\")"));
        assertTrue( (Boolean)venice.eval("(newer-version? \"2.1.2\" \"2.1.1\")"));
        assertFalse((Boolean)venice.eval("(newer-version? \"2.2.2\" \"2.3.9\")"));
        assertTrue( (Boolean)venice.eval("(newer-version? \"3.1.8\" \"2.0.10\")"));

        assertFalse((Boolean)venice.eval("(newer-version? \"1.0.0-alpha\" \"2.0.1\")"));
        assertFalse((Boolean)venice.eval("(newer-version? \"2.0.0-alpha\" \"2.0.0\")"));
        assertTrue( (Boolean)venice.eval("(newer-version? \"2.0.1-alpha\" \"2.0.0\")"));
        assertTrue( (Boolean)venice.eval("(newer-version? \"2.1.2-alpha\" \"2.1.1\")"));
        assertFalse((Boolean)venice.eval("(newer-version? \"2.2.2-alpha\" \"2.3.1\")"));
        assertTrue( (Boolean)venice.eval("(newer-version? \"3.1.8-alpha\" \"2.0.9\")"));
    }

    @Test
    public void test_newer_version_4() {
        final Venice venice = new Venice();

        assertFalse((Boolean)venice.eval("(newer-version? \"1\" \"2.0.1-beta\")"));
        assertTrue((Boolean)venice.eval("(newer-version? \"2\" \"2.0.0-beta\")"));
        assertFalse((Boolean)venice.eval("(newer-version? \"2\" \"2.0.1-beta\")"));
        assertTrue( (Boolean)venice.eval("(newer-version? \"3\" \"2.0.1-beta\")"));

        assertFalse((Boolean)venice.eval("(newer-version? \"1.0\" \"2.0.1-beta\")"));
        assertTrue((Boolean)venice.eval("(newer-version? \"2.0\" \"2.0.0-beta\")"));
        assertFalse((Boolean)venice.eval("(newer-version? \"2.0\" \"2.0.1-beta\")"));
        assertTrue( (Boolean)venice.eval("(newer-version? \"2.1\" \"2.0.9-beta\")"));
        assertTrue((Boolean)venice.eval("(newer-version? \"2.1\" \"2.1.0-beta\")"));
        assertFalse((Boolean)venice.eval("(newer-version? \"2.1\" \"2.2.3-beta\")"));
        assertTrue( (Boolean)venice.eval("(newer-version? \"3.1\" \"2.0.7-beta\")"));

        assertFalse((Boolean)venice.eval("(newer-version? \"1.0.0\" \"2.0.1-beta\")"));
        assertFalse((Boolean)venice.eval("(newer-version? \"2.0.0\" \"2.0.1-beta\")"));
        assertTrue((Boolean)venice.eval("(newer-version? \"2.0.1\" \"2.0.1-beta\")"));
        assertTrue( (Boolean)venice.eval("(newer-version? \"2.0.2\" \"2.0.1-beta\")"));
        assertTrue( (Boolean)venice.eval("(newer-version? \"2.1.2\" \"2.1.1-beta\")"));
        assertFalse((Boolean)venice.eval("(newer-version? \"2.2.2\" \"2.3.9-beta\")"));
        assertTrue( (Boolean)venice.eval("(newer-version? \"3.1.8\" \"2.0.10-beta\")"));

        assertFalse((Boolean)venice.eval("(newer-version? \"1.0.0-alpha\" \"2.0.1-beta\")"));
        assertFalse((Boolean)venice.eval("(newer-version? \"2.0.0-a\" \"2.0.0-b\")"));
        assertFalse((Boolean)venice.eval("(newer-version? \"2.0.0-aaa\" \"2.0.0-b\")"));
        assertTrue((Boolean)venice.eval("(newer-version? \"2.0.0-b\" \"2.0.0-a\")"));
        assertTrue((Boolean)venice.eval("(newer-version? \"2.0.0-b\" \"2.0.0-aaa\")"));
        assertFalse((Boolean)venice.eval("(newer-version? \"2.0.0-rc-1\" \"2.0.0-rc-2\")"));
        assertTrue((Boolean)venice.eval("(newer-version? \"2.0.0-rc-2\" \"2.0.0-rc-1\")"));
        assertTrue( (Boolean)venice.eval("(newer-version? \"2.0.1-alpha\" \"2.0.0-beta\")"));
        assertTrue( (Boolean)venice.eval("(newer-version? \"2.1.2-alpha\" \"2.1.1-beta\")"));
        assertFalse((Boolean)venice.eval("(newer-version? \"2.2.2-alpha\" \"2.3.1-beta\")"));
        assertTrue( (Boolean)venice.eval("(newer-version? \"3.1.8-alpha\" \"2.0.9-beta\")"));
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

    @Test
    @EnableOnWindows
    public void test_os_type_windows() {
        final Venice venice = new Venice();

        assertTrue((Boolean)venice.eval("(os-type? :windows)"));

        assertFalse((Boolean)venice.eval("(os-type? :mac-osx)"));
        assertFalse((Boolean)venice.eval("(os-type? :mac-os)"));
        assertFalse((Boolean)venice.eval("(os-type? :linux)"));
    }

    @Test
    @EnableOnMac
    public void test_os_type_mac_osx() {
        final Venice venice = new Venice();

        assertTrue((Boolean)venice.eval("(os-type? :mac-osx)"));

        assertFalse((Boolean)venice.eval("(os-type? :windows)"));
        assertFalse((Boolean)venice.eval("(os-type? :linux)"));
    }

    @Test
    @EnableOnMac
    public void test_os_type_mac_os() {
        final Venice venice = new Venice();

        assertTrue((Boolean)venice.eval("(os-type? :mac-os)"));

        assertFalse((Boolean)venice.eval("(os-type? :windows)"));
        assertFalse((Boolean)venice.eval("(os-type? :linux)"));
    }

    @Test
    @EnableOnLinux
    public void test_os_type_linux() {
        final Venice venice = new Venice();

        assertTrue((Boolean)venice.eval("(os-type? :linux)"));

        assertFalse((Boolean)venice.eval("(os-type? :mac-osx)"));
        assertFalse((Boolean)venice.eval("(os-type? :mac-os)"));
        assertFalse((Boolean)venice.eval("(os-type? :windows)"));
    }
}
