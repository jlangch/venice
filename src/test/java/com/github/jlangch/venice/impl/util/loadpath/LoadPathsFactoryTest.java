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
package com.github.jlangch.venice.impl.util.loadpath;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.util.io.FileUtil;
import com.github.jlangch.venice.impl.util.junit.EnableOnLinux;
import com.github.jlangch.venice.impl.util.junit.EnableOnMac;
import com.github.jlangch.venice.util.OS.OsType;


public class LoadPathsFactoryTest {

    // ========================================================================
    // LINUX
    // ========================================================================

    @Test
    public void test_linux_parse_null() {
        final List<File> items = LoadPathsFactory.parseDelimitedLoadPathRaw(null, OsType.Linux);
        assertEquals(0, items.size());
    }

    @Test
    public void test_linux_parse_empty() {
        final List<File> items = LoadPathsFactory.parseDelimitedLoadPathRaw("", OsType.Linux);
        assertEquals(0, items.size());
    }

    @Test
    public void test_linux_parse_single() {
        final List<File> items = LoadPathsFactory.parseDelimitedLoadPathRaw("a", OsType.Linux);
        assertEquals(1, items.size());
        assertTrue(contains("a", items));
    }

    // ------------------------------------------------------------------------
    // colon
    // ------------------------------------------------------------------------

    @Test
    public void test_linux_parse_colon_only() {
        final List<File> items = LoadPathsFactory.parseDelimitedLoadPathRaw(":", OsType.Linux);
        assertEquals(0, items.size());
    }

    @Test
    public void test_linux_parse_2_colon() {
        final List<File> items = LoadPathsFactory.parseDelimitedLoadPathRaw("::", OsType.Linux);
        assertEquals(0, items.size());
    }

    @Test
    public void test_linux_parse_2_colon_mixed() {
        final List<File> items = LoadPathsFactory.parseDelimitedLoadPathRaw("a::b", OsType.Linux);
        assertEquals(2, items.size());
        assertTrue(contains("a", items));
        assertTrue(contains("b", items));
    }

    @Test
    public void test_linux_parse_colon() {
        final List<File> items = LoadPathsFactory.parseDelimitedLoadPathRaw("a:b", OsType.Linux);
        assertEquals(2, items.size());
        assertTrue(contains("a", items));
        assertTrue(contains("b", items));
    }

    // ------------------------------------------------------------------------
    // semi-colon
    // ------------------------------------------------------------------------

    @Test
    public void test_linux_parse_semicolon_only() {
        final List<File> items = LoadPathsFactory.parseDelimitedLoadPathRaw(";", OsType.Linux);
        assertEquals(0, items.size());
    }

    @Test
    public void test_linux_parse_2_semi_colon() {
        final List<File> items = LoadPathsFactory.parseDelimitedLoadPathRaw(";;", OsType.Linux);
        assertEquals(0, items.size());
    }

    @Test
    public void test_linux_parse_2_semi_colon_mixed() {
        final List<File> items = LoadPathsFactory.parseDelimitedLoadPathRaw("a;;b", OsType.Linux);
        assertEquals(2, items.size());
        assertTrue(contains("a", items));
        assertTrue(contains("b", items));
    }

    @Test
    public void test_linux_parse_semi_colon() {
        final List<File> items = LoadPathsFactory.parseDelimitedLoadPathRaw("a;b", OsType.Linux);
        assertEquals(2, items.size());
        assertTrue(contains("a", items));
        assertTrue(contains("b", items));
    }

    @Test
    public void test_linux_parse_mixed() {
        final List<File> items = LoadPathsFactory.parseDelimitedLoadPathRaw("a;b:c", OsType.Linux);
        assertEquals(3, items.size());
        assertTrue(contains("a", items));
        assertTrue(contains("b", items));
        assertTrue(contains("c", items));
    }

    // ------------------------------------------------------------------------
    // real paths
    // ------------------------------------------------------------------------

    @Test
    @EnableOnLinux
    public void test_linux_real_paths() throws IOException {
        final File dir = Files.createTempDirectory("test").toFile();

        final File dirA = new File(dir, "a");
        final File dirB = new File(dir, "b");
        FileUtil.mkdir(dirA);
        FileUtil.mkdir(dirB);

        try {
            final ILoadPaths loadPaths = LoadPathsFactory.parseDelimitedLoadPath(
                                                 dirA.getPath() + ":" + dirB.getPath());
            assertEquals(2, loadPaths.getPaths().size());
        }
        finally {
            FileUtil.rmdir(dir);
        }

        assertFalse(dir.exists());
    }

    @Test
    @EnableOnMac
    public void test_linux_real_paths_2() throws IOException {
        // quirk (2 times current working directory)
        final ILoadPaths loadPaths = LoadPathsFactory.parseDelimitedLoadPath(".:.");
        assertEquals(2, loadPaths.getPaths().size());
    }


    // ========================================================================
    // MACOS
    // ========================================================================

    @Test
    public void test_macos_parse_null() {
        final List<File> items = LoadPathsFactory.parseDelimitedLoadPathRaw(null, OsType.MacOSX);
        assertEquals(0, items.size());
    }

    @Test
    public void test_macos_parse_empty() {
        final List<File> items = LoadPathsFactory.parseDelimitedLoadPathRaw("", OsType.MacOSX);
        assertEquals(0, items.size());
    }

    @Test
    public void test_macos_parse_single() {
        final List<File> items = LoadPathsFactory.parseDelimitedLoadPathRaw("a", OsType.MacOSX);
        assertEquals(1, items.size());
        assertTrue(contains("a", items));
    }

    // ------------------------------------------------------------------------
    // colon
    // ------------------------------------------------------------------------

    @Test
    public void test_macos_parse_colon_only() {
        final List<File> items = LoadPathsFactory.parseDelimitedLoadPathRaw(":", OsType.MacOSX);
        assertEquals(0, items.size());
    }

    @Test
    public void test_macos_parse_2_colon() {
        final List<File> items = LoadPathsFactory.parseDelimitedLoadPathRaw("::", OsType.MacOSX);
        assertEquals(0, items.size());
    }

    @Test
    public void test_macos_parse_2_colon_mixed() {
        final List<File> items = LoadPathsFactory.parseDelimitedLoadPathRaw("a::b", OsType.MacOSX);
        assertEquals(2, items.size());
        assertTrue(contains("a", items));
        assertTrue(contains("b", items));
    }

    @Test
    public void test_macos_parse_colon() {
        final List<File> items = LoadPathsFactory.parseDelimitedLoadPathRaw("a:b", OsType.MacOSX);
        assertEquals(2, items.size());
        assertTrue(contains("a", items));
        assertTrue(contains("b", items));
    }

    // ------------------------------------------------------------------------
    // semi-colon
    // ------------------------------------------------------------------------

    @Test
    public void test_macos_parse_semicolon_only() {
        final List<File> items = LoadPathsFactory.parseDelimitedLoadPathRaw(";", OsType.MacOSX);
        assertEquals(0, items.size());
    }

    @Test
    public void test_macos_parse_2_semi_colon() {
        final List<File> items = LoadPathsFactory.parseDelimitedLoadPathRaw(";;", OsType.MacOSX);
        assertEquals(0, items.size());
    }

    @Test
    public void test_macos_parse_2_semi_colon_mixed() {
        final List<File> items = LoadPathsFactory.parseDelimitedLoadPathRaw("a;;b", OsType.MacOSX);
        assertEquals(2, items.size());
        assertTrue(contains("a", items));
        assertTrue(contains("b", items));
    }

    @Test
    public void test_macos_parse_semi_colon() {
        final List<File> items = LoadPathsFactory.parseDelimitedLoadPathRaw("a;b", OsType.MacOSX);
        assertEquals(2, items.size());
        assertTrue(contains("a", items));
        assertTrue(contains("b", items));
    }

    @Test
    public void test_macos_parse_mixed() {
        final List<File> items = LoadPathsFactory.parseDelimitedLoadPathRaw("a;b:c", OsType.MacOSX);
        assertEquals(3, items.size());
        assertTrue(contains("a", items));
        assertTrue(contains("b", items));
        assertTrue(contains("c", items));
    }

    // ------------------------------------------------------------------------
    // real paths
    // ------------------------------------------------------------------------

    @Test
    @EnableOnMac
    public void test_macos_real_paths_1() throws IOException {
        final File dir = Files.createTempDirectory("test").toFile();

        final File dirA = new File(dir, "a");
        final File dirB = new File(dir, "b");
        FileUtil.mkdir(dirA);
        FileUtil.mkdir(dirB);

        try {
            final ILoadPaths loadPaths = LoadPathsFactory.parseDelimitedLoadPath(
                                                 dirA.getPath() + ":" + dirB.getPath());
            assertEquals(2, loadPaths.getPaths().size());
        }
        finally {
            FileUtil.rmdir(dir);
        }

        assertFalse(dir.exists());
    }

    @Test
    @EnableOnMac
    public void test_macos_real_paths_2() throws IOException {
        // quirk (2 times current working directory)
        final ILoadPaths loadPaths = LoadPathsFactory.parseDelimitedLoadPath(".:.");
        assertEquals(2, loadPaths.getPaths().size());
    }



    // ========================================================================
    // WINDOWS
    // ========================================================================

    @Test
    public void test_windows_parse_null() {
        final List<File> items = LoadPathsFactory.parseDelimitedLoadPathRaw(null, OsType.Windows);
        assertEquals(0, items.size());
    }

    @Test
    public void test_windows_parse_empty() {
        final List<File> items = LoadPathsFactory.parseDelimitedLoadPathRaw("", OsType.Windows);
        assertEquals(0, items.size());
    }

    @Test
    public void test_windows_parse_single() {
        final List<File> items = LoadPathsFactory.parseDelimitedLoadPathRaw("a", OsType.Windows);
        assertEquals(1, items.size());
        assertTrue(contains("a", items));
    }

    @Test
    public void test_windows_parse_single_2() {
        final List<File> items = LoadPathsFactory.parseDelimitedLoadPathRaw("C:\\a", OsType.Windows);
        assertEquals(1, items.size());
        assertTrue(contains("C:\\a", items));
    }

    @Test
    public void test_windows_parse_semicolon_only() {
        final List<File> items = LoadPathsFactory.parseDelimitedLoadPathRaw(";", OsType.Windows);
        assertEquals(0, items.size());
    }

    @Test
    public void test_windows_parse_2_semi_colon() {
        final List<File> items = LoadPathsFactory.parseDelimitedLoadPathRaw(";;", OsType.Windows);
        assertEquals(0, items.size());
    }

    @Test
    public void test_windows_parse_2_semi_colon_mixed() {
        final List<File> items = LoadPathsFactory.parseDelimitedLoadPathRaw("a;;b", OsType.Windows);
        assertEquals(2, items.size());
        assertTrue(contains("a", items));
        assertTrue(contains("b", items));
    }

    @Test
    public void test_windows_parse_2_semi_colon_mixed_2() {
        final List<File> items = LoadPathsFactory.parseDelimitedLoadPathRaw("C:\\a;;C:\\b", OsType.Windows);
        assertEquals(2, items.size());
        assertTrue(contains("C:\\a", items));
        assertTrue(contains("C:\\b", items));
    }

    @Test
    public void test_windows_parse_semi_colon() {
        final List<File> items = LoadPathsFactory.parseDelimitedLoadPathRaw("a;b", OsType.Windows);
        assertEquals(2, items.size());
        assertTrue(contains("a", items));
        assertTrue(contains("b", items));
    }

    @Test
    public void test_windows_parse_semi_colon_2() {
        final List<File> items = LoadPathsFactory.parseDelimitedLoadPathRaw("C:\\a;C:\\b", OsType.Windows);
        assertEquals(2, items.size());
        assertTrue(contains("C:\\a", items));
        assertTrue(contains("C:\\b", items));
    }




    // ========================================================================
    // utils
    // ========================================================================

    private static boolean contains(final String path, final List<File> files) {
        return files.contains(new File(path));
    }
}

