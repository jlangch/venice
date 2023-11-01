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
package com.github.jlangch.venice.impl.util.loadpath;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.util.io.CharsetUtil;
import com.github.jlangch.venice.impl.util.io.FileUtil;
import com.github.jlangch.venice.impl.util.io.zip.Zipper;


public class LoadPaths2Test {

    @Test
    public void test_getPaths() throws IOException {
        final File zip = Files.createTempFile("loadpath_", ".zip").normalize().toFile().getCanonicalFile();
        final File dir1 = Files.createTempDirectory("loadpath_").toFile().getCanonicalFile();
        final File dir2 = Files.createTempDirectory("loadpath_").toFile().getCanonicalFile();
        zip.deleteOnExit();
        dir1.deleteOnExit();
        dir2.deleteOnExit();

        FileUtil.save(zip("a", "1234", "b", "5678"), zip, true);


        // null path
        assertTrue(LoadPaths.of(null, true).getPaths().isEmpty());
        assertTrue(LoadPaths.of(null, false).getPaths().isEmpty());

        // empty path
        final List<File> empty = new ArrayList<File>();
        assertTrue(LoadPaths.of(empty, true).getPaths().isEmpty());
        assertTrue(LoadPaths.of(empty, false).getPaths().isEmpty());

        // single dir path
        final List<File> paths1 = new ArrayList<File>();
        paths1.add(dir1);
        assertEquals(1, LoadPaths.of(paths1, true).getPaths().size());
        assertEquals(1, LoadPaths.of(paths1, false).getPaths().size());
        assertEquals(dir1, LoadPaths.of(paths1, true).getPaths().get(0));
        assertEquals(dir1, LoadPaths.of(paths1, false).getPaths().get(0));

        // path with zip and 2 dirs
        final List<File> paths2 = new ArrayList<File>();
        paths2.add(zip);
        paths2.add(dir1);
        paths2.add(dir2);
        assertEquals(3, LoadPaths.of(paths2, true).getPaths().size());
        assertEquals(3, LoadPaths.of(paths2, false).getPaths().size());
        assertEquals(zip, LoadPaths.of(paths2, true).getPaths().get(0));
        assertEquals(zip, LoadPaths.of(paths2, false).getPaths().get(0));
        assertEquals(dir1, LoadPaths.of(paths2, true).getPaths().get(1));
        assertEquals(dir1, LoadPaths.of(paths2, false).getPaths().get(1));
        assertEquals(dir2, LoadPaths.of(paths2, true).getPaths().get(2));
        assertEquals(dir2, LoadPaths.of(paths2, false).getPaths().get(2));
    }

    @Test
    public void test_isUnlimitedAccess() throws IOException {
        final File dir1 = Files.createTempDirectory("loadpath_").toFile().getCanonicalFile();
        dir1.deleteOnExit();


        assertTrue(LoadPaths.of(null, true).isUnlimitedAccess());
        assertFalse(LoadPaths.of(null, false).isUnlimitedAccess());

        final List<File> empty = new ArrayList<File>();
        assertTrue(LoadPaths.of(empty, true).isUnlimitedAccess());
        assertFalse(LoadPaths.of(empty, false).isUnlimitedAccess());

        final List<File> paths = new ArrayList<File>();
        paths.add(dir1);
        assertTrue(LoadPaths.of(paths, true).isUnlimitedAccess());
        assertFalse(LoadPaths.of(paths, false).isUnlimitedAccess());
    }

    @Test
    public void test_loadBinaryResource_unlimited_false() throws IOException {
        final File dir1 = Files.createTempDirectory("loadpath_").toFile().getCanonicalFile();
        final File dir2 = Files.createTempDirectory("loadpath_").toFile().getCanonicalFile();
        final File bin1 = new File(dir1, "data1.bin");
        FileUtil.save("1234".getBytes(CharsetUtil.DEFAULT_CHARSET), bin1, true);
        final File bin2 = new File(dir2, "data2.bin");
        FileUtil.save("5678".getBytes(CharsetUtil.DEFAULT_CHARSET), bin2, true);
        final File zip = Files.createTempFile("loadpath_", ".zip").normalize().toFile().getCanonicalFile();
        FileUtil.save(zip("a", "1234", "b", "5678"), zip, true);
        dir1.deleteOnExit();
        dir2.deleteOnExit();
        bin1.deleteOnExit();
        bin2.deleteOnExit();
        zip.deleteOnExit();

        final List<File> paths = new ArrayList<File>();
        paths.add(dir1);
        paths.add(zip);
        LoadPaths lp = LoadPaths.of(paths, false);


        // relative file -> ok
        ByteBuffer data = lp.loadBinaryResource(new File(bin1.getName()));
        assertEquals("1234", new String(data.array(), CharsetUtil.DEFAULT_CHARSET));

        // relative file -> fail
        data = lp.loadBinaryResource(new File(bin2.getName()));
        assertNull(data);

        // absolute file -> ok
        data = lp.loadBinaryResource(bin1);
        assertEquals("1234", new String(data.array(), CharsetUtil.DEFAULT_CHARSET));

        // absolute file -> fail
        data = lp.loadBinaryResource(bin2);
        assertNull(data);

        // ZIP
        data = lp.loadBinaryResource(new File("a"));
        assertEquals("1234", new String(data.array(), CharsetUtil.DEFAULT_CHARSET));
        data = lp.loadBinaryResource(new File("b"));
        assertEquals("5678", new String(data.array(), CharsetUtil.DEFAULT_CHARSET));
        data = lp.loadBinaryResource(new File("c"));
        assertNull(data);
    }

    @Test
    public void test_loadBinaryResource_unlimited_true() throws IOException {
        final File dir1 = Files.createTempDirectory("loadpath_").toFile().getCanonicalFile();
        final File dir2 = Files.createTempDirectory("loadpath_").toFile().getCanonicalFile();
        final File bin1 = new File(dir1, "data1.bin");
        FileUtil.save("1234".getBytes(CharsetUtil.DEFAULT_CHARSET), bin1, true);
        final File bin2 = new File(dir2, "data2.bin");
        FileUtil.save("5678".getBytes(CharsetUtil.DEFAULT_CHARSET), bin2, true);
        final File zip = Files.createTempFile("loadpath_", ".zip").normalize().toFile().getCanonicalFile();
        FileUtil.save(zip("a", "1234", "b", "5678"), zip, true);
        dir1.deleteOnExit();
        dir2.deleteOnExit();
        bin1.deleteOnExit();
        bin2.deleteOnExit();
        zip.deleteOnExit();

        final List<File> paths = new ArrayList<File>();
        paths.add(dir1);
        paths.add(zip);
        LoadPaths lp = LoadPaths.of(paths, true);


        // relative file -> ok
        ByteBuffer data = lp.loadBinaryResource(new File(bin1.getName()));
        assertEquals("1234", new String(data.array(), CharsetUtil.DEFAULT_CHARSET));

        // relative file -> fail
        data = lp.loadBinaryResource(new File(bin2.getName()));
        assertNull(data);

        // absolute file -> ok
        data = lp.loadBinaryResource(bin1);
        assertEquals("1234", new String(data.array(), CharsetUtil.DEFAULT_CHARSET));

        // absolute file -> ok
        data = lp.loadBinaryResource(bin2);
        assertEquals("5678", new String(data.array(), CharsetUtil.DEFAULT_CHARSET));

        // ZIP
        data = lp.loadBinaryResource(new File("a"));
        assertEquals("1234", new String(data.array(), CharsetUtil.DEFAULT_CHARSET));
        data = lp.loadBinaryResource(new File("b"));
        assertEquals("5678", new String(data.array(), CharsetUtil.DEFAULT_CHARSET));
        data = lp.loadBinaryResource(new File("c"));
        assertNull(data);
    }

    @Test
    public void test_loadTextResource_unlimited_false() throws IOException {
        final File dir1 = Files.createTempDirectory("loadpath_").toFile().getCanonicalFile();
        final File dir2 = Files.createTempDirectory("loadpath_").toFile().getCanonicalFile();
        final File bin1 = new File(dir1, "data1.bin");
        FileUtil.save("1234".getBytes(CharsetUtil.DEFAULT_CHARSET), bin1, true);
        final File bin2 = new File(dir2, "data2.bin");
        FileUtil.save("5678".getBytes(CharsetUtil.DEFAULT_CHARSET), bin2, true);
        final File zip = Files.createTempFile("loadpath_", ".zip").normalize().toFile().getCanonicalFile();
        FileUtil.save(zip("a", "1234", "b", "5678"), zip, true);
        dir1.deleteOnExit();
        dir2.deleteOnExit();
        bin1.deleteOnExit();
        bin2.deleteOnExit();
        zip.deleteOnExit();

        final List<File> paths = new ArrayList<File>();
        paths.add(dir1);
        paths.add(zip);
        LoadPaths lp = LoadPaths.of(paths, false);


        // relative file -> ok
        String data = lp.loadTextResource(new File(bin1.getName()), CharsetUtil.DEFAULT_CHARSET);
        assertEquals("1234", data);

        // relative file -> fail
        data = lp.loadTextResource(new File(bin2.getName()), CharsetUtil.DEFAULT_CHARSET);
        assertNull(data);

        // absolute file -> ok
        data = lp.loadTextResource(bin1, CharsetUtil.DEFAULT_CHARSET);
        assertEquals("1234", data);

        // absolute file -> fail
        data = lp.loadTextResource(bin2, CharsetUtil.DEFAULT_CHARSET);
        assertNull(data);

        // ZIP
        data = lp.loadTextResource(new File("a"), CharsetUtil.DEFAULT_CHARSET);
        assertEquals("1234", data);
        data = lp.loadTextResource(new File("b"), CharsetUtil.DEFAULT_CHARSET);
        assertEquals("5678", data);
        data = lp.loadTextResource(new File("c"), CharsetUtil.DEFAULT_CHARSET);
        assertNull(data);
    }

    @Test
    public void test_loadTextResource_unlimited_true() throws IOException {
        final File dir1 = Files.createTempDirectory("loadpath_").toFile().getCanonicalFile();
        final File dir2 = Files.createTempDirectory("loadpath_").toFile().getCanonicalFile();
        final File bin1 = new File(dir1, "data1.bin");
        FileUtil.save("1234".getBytes(CharsetUtil.DEFAULT_CHARSET), bin1, true);
        final File bin2 = new File(dir2, "data2.bin");
        FileUtil.save("5678".getBytes(CharsetUtil.DEFAULT_CHARSET), bin2, true);
        final File zip = Files.createTempFile("loadpath_", ".zip").normalize().toFile().getCanonicalFile();
        FileUtil.save(zip("a", "1234", "b", "5678"), zip, true);
        dir1.deleteOnExit();
        dir2.deleteOnExit();
        bin1.deleteOnExit();
        bin2.deleteOnExit();
        zip.deleteOnExit();

        final List<File> paths = new ArrayList<File>();
        paths.add(dir1);
        paths.add(zip);
        LoadPaths lp = LoadPaths.of(paths, true);


        // relative file -> ok
        String data = lp.loadTextResource(new File(bin1.getName()), CharsetUtil.DEFAULT_CHARSET);
        assertEquals("1234", data);

        // relative file -> fail
        data = lp.loadTextResource(new File(bin2.getName()), CharsetUtil.DEFAULT_CHARSET);
        assertNull(data);

        // absolute file -> ok
        data = lp.loadTextResource(bin1, CharsetUtil.DEFAULT_CHARSET);
        assertEquals("1234", data);

        // absolute file -> ok
        data = lp.loadTextResource(bin2, CharsetUtil.DEFAULT_CHARSET);
        assertEquals("5678", data);

        // ZIP
        data = lp.loadTextResource(new File("a"), CharsetUtil.DEFAULT_CHARSET);
        assertEquals("1234", data);
        data = lp.loadTextResource(new File("b"), CharsetUtil.DEFAULT_CHARSET);
        assertEquals("5678", data);
        data = lp.loadTextResource(new File("c"), CharsetUtil.DEFAULT_CHARSET);
        assertNull(data);
    }

    @Test
    public void test_loadVeniceFile_unlimited_false() throws IOException {
        final File dir1 = Files.createTempDirectory("loadpath_").toFile().getCanonicalFile();
        final File dir2 = Files.createTempDirectory("loadpath_").toFile().getCanonicalFile();
        final File bin1 = new File(dir1, "data1.venice");
        FileUtil.save("(def x 1)".getBytes(CharsetUtil.DEFAULT_CHARSET), bin1, true);
        final File bin2 = new File(dir2, "data2.venice");
        FileUtil.save("(def x 2)".getBytes(CharsetUtil.DEFAULT_CHARSET), bin2, true);
        final File zip = Files.createTempFile("loadpath_", ".zip").normalize().toFile().getCanonicalFile();
        FileUtil.save(zip("a.venice", "(def x :a)", "b.venice", "(def x :b)"), zip, true);
        dir1.deleteOnExit();
        dir2.deleteOnExit();
        bin1.deleteOnExit();
        bin2.deleteOnExit();
        zip.deleteOnExit();

        final List<File> paths = new ArrayList<File>();
        paths.add(dir1);
        paths.add(zip);
        LoadPaths lp = LoadPaths.of(paths, false);


        // relative file -> ok
        String data = lp.loadVeniceFile(new File("data1"));
        assertEquals("(def x 1)", data);
        data = lp.loadVeniceFile(new File("data1.venice"));
        assertEquals("(def x 1)", data);

        // relative file -> fail
        data = lp.loadVeniceFile(new File("data2"));
        assertNull(data);
        data = lp.loadVeniceFile(new File("data2.venice"));
        assertNull(data);

        // absolute file -> ok
        data = lp.loadVeniceFile(bin1);
        assertEquals("(def x 1)", data);

        // absolute file -> fail
        data = lp.loadVeniceFile(bin2);
        assertNull(data);

        // ZIP
        data = lp.loadVeniceFile(new File("a"));
        assertEquals("(def x :a)", data);
        data = lp.loadVeniceFile(new File("b"));
        assertEquals("(def x :b)", data);
        data = lp.loadVeniceFile(new File("c"));
        assertNull(data);
        data = lp.loadVeniceFile(new File("a.venice"));
        assertEquals("(def x :a)", data);
        data = lp.loadVeniceFile(new File("b.venice"));
        assertEquals("(def x :b)", data);
        data = lp.loadVeniceFile(new File("c.venice"));
        assertNull(data);
    }

    @Test
    public void test_loadVeniceFile_unlimited_true() throws IOException {
        final File dir1 = Files.createTempDirectory("loadpath_").toFile().getCanonicalFile();
        final File dir2 = Files.createTempDirectory("loadpath_").toFile().getCanonicalFile();
        final File bin1 = new File(dir1, "data1.venice");
        FileUtil.save("(def x 1)".getBytes(CharsetUtil.DEFAULT_CHARSET), bin1, true);
        final File bin2 = new File(dir2, "data2.venice");
        FileUtil.save("(def x 2)".getBytes(CharsetUtil.DEFAULT_CHARSET), bin2, true);
        final File zip = Files.createTempFile("loadpath_", ".zip").normalize().toFile().getCanonicalFile();
        FileUtil.save(zip("a.venice", "(def x :a)", "b.venice", "(def x :b)"), zip, true);
        dir1.deleteOnExit();
        dir2.deleteOnExit();
        bin1.deleteOnExit();
        bin2.deleteOnExit();
        zip.deleteOnExit();

        final List<File> paths = new ArrayList<File>();
        paths.add(dir1);
        paths.add(zip);
        LoadPaths lp = LoadPaths.of(paths, true);


        // relative file -> ok
        String data = lp.loadVeniceFile(new File("data1"));
        assertEquals("(def x 1)", data);
        data = lp.loadVeniceFile(new File("data1.venice"));
        assertEquals("(def x 1)", data);

        // relative file -> fail
        data = lp.loadVeniceFile(new File("data2"));
        assertNull(data);
        data = lp.loadVeniceFile(new File("data2.venice"));
        assertNull(data);

        // absolute file -> ok
        data = lp.loadVeniceFile(bin1);
        assertEquals("(def x 1)", data);

        // absolute file -> ok
        data = lp.loadVeniceFile(bin2);
        assertEquals("(def x 2)", data);

        // ZIP
        data = lp.loadVeniceFile(new File("a"));
        assertEquals("(def x :a)", data);
        data = lp.loadVeniceFile(new File("b"));
        assertEquals("(def x :b)", data);
        data = lp.loadVeniceFile(new File("c"));
        assertNull(data);
        data = lp.loadVeniceFile(new File("a.venice"));
        assertEquals("(def x :a)", data);
        data = lp.loadVeniceFile(new File("b.venice"));
        assertEquals("(def x :b)", data);
        data = lp.loadVeniceFile(new File("c.venice"));
        assertNull(data);
    }

    @Test
    public void test_Partial_Path_Traversal_Vulnerability() throws IOException {
        final File dir1 = Files.createTempDirectory("loadpath_test__").toFile().getCanonicalFile();
        final File dir2 = Files.createTempDirectory("loadpath_test_alt__").toFile().getCanonicalFile();
        final File bin1 = new File(dir1, "data1.venice");
        FileUtil.save("(def x 1)".getBytes(CharsetUtil.DEFAULT_CHARSET), bin1, true);
        final File bin2 = new File(dir2, "data2.venice");
        FileUtil.save("(def x 2)".getBytes(CharsetUtil.DEFAULT_CHARSET), bin2, true);
        dir1.deleteOnExit();
        dir2.deleteOnExit();
        bin1.deleteOnExit();
        bin2.deleteOnExit();

        final List<File> paths = new ArrayList<File>();
        paths.add(dir1);
        LoadPaths lp = LoadPaths.of(paths, false);


        // relative file -> ok
        String data = lp.loadVeniceFile(new File("data1"));
        assertEquals("(def x 1)", data);
        data = lp.loadVeniceFile(new File("data1.venice"));
        assertEquals("(def x 1)", data);

        // relative file, access outside the load path!
        data = lp.loadVeniceFile(new File("../" + dir2.getName() + "/data2.venice"));
        assertNull(data);

        // absolute file -> ok
        data = lp.loadVeniceFile(bin1);
        assertEquals("(def x 1)", data);

        // absolute file, access outside the load path!
        data = lp.loadVeniceFile(bin2);
        assertNull(data);
    }


    private static byte[] zip(
            final String entry1, final String value1,
            final String entry2, final String value2
    ) throws UnsupportedEncodingException {
        return zip(entry1, value1.getBytes(CharsetUtil.DEFAULT_CHARSET),
                   entry2, value2.getBytes(CharsetUtil.DEFAULT_CHARSET));
    }

    private static byte[] zip(
            final String entry1, final byte[] value1,
            final String entry2, final byte[] value2
    ) throws UnsupportedEncodingException {
        final  Map<String, Object> entries = new HashMap<>();

        entries.put(entry1, value1);
        entries.put(entry2, value2);

        return Zipper.zip(entries);
    }

}
