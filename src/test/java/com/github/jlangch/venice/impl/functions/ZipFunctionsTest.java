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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.ByteBuffer;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;


public class ZipFunctionsTest {

    @Test
    public void test_io_zip() throws Exception {
        final Venice venice = new Venice();

        assertEquals("abcdef", new String(
                                    ((ByteBuffer)venice.eval(
                                            "(-> (io/zip \"test\" (bytebuf-from-string \"abcdef\" :utf-8)) \n" +
                                            "    (io/unzip \"test\"))")).array(),
                                    "utf-8"));
    }

    @Test
    public void test_io_zip_file_add_file() {
        final Venice venice = new Venice();

        try {
            venice.eval(
                    "(let [base-dir (io/file (io/temp-dir \"zip-test-\"))    \n" +
                    "      zip-dir  (io/file base-dir \"test\")              \n" +
                    "      a1       (io/file zip-dir \"a1.txt\")             \n" +
                    "      zip      (io/file base-dir \"a.zip\")]            \n" +
                    "                                                        \n" +
                    "  (io/mkdir zip-dir)                                    \n" +
                    "  (io/spit a1 \"a1\")                                   \n" +
                    "                                                        \n" +
                    "  (io/zip-file zip a1)                                  \n" +
                    "  (assert (io/exists-file? zip))                        \n" +
                    "  (assert (== 1 (io/zip-size zip)))                     \n" +
                    "                                                        \n" +
                    "  (io/delete-file a1)                                   \n" +
                    "  (assert (== 0 (count (io/list-files zip-dir))))       \n" +
                    "                                                        \n" +
                    "  (io/unzip-to-dir zip zip-dir)                         \n" +
                    "                                                        \n" +
                    "  (assert (== 1 (count (io/list-files zip-dir))))       \n" +
                    "  (io/delete-file a1)                                   \n" +
                    "  (assert (== 0 (count (io/list-files zip-dir))))       \n" +
                    "                                                        \n" +
                    "  (io/delete-file a1 zip-dir)                           \n" +
                    "  (io/delete-file zip)                                  \n" +
                    "  (assert (== 0 (count (io/list-files base-dir))))      \n" +
                    "  (io/delete-file base-dir)                             \n" +
                    ")");
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void test_io_zip_file_add_dir() {
        final Venice venice = new Venice();

        try {
            venice.eval(
                    "(let [base-dir (io/file (io/temp-dir \"zip-test-\"))    \n" +
                    "      zip-dir  (io/file base-dir \"test\")              \n" +
                    "      a1       (io/file zip-dir \"a1.txt\")             \n" +
                    "      a2       (io/file zip-dir \"a2.txt\")             \n" +
                    "      a3       (io/file zip-dir \"a3.txt\")             \n" +
                    "      zip      (io/file base-dir \"a.zip\")]            \n" +
                    "                                                        \n" +
                    "  (io/mkdir zip-dir)                                    \n" +
                    "  (io/spit a1 \"a1\")                                   \n" +
                    "  (io/spit a2 \"a2\")                                   \n" +
                    "  (io/spit a3 \"a3\")                                   \n" +
                    "  (assert (== 3 (count (io/list-files zip-dir))))       \n" +
                    "                                                        \n" +
                    "  (io/zip-file zip zip-dir)                             \n" +
                    "  (assert (io/exists-file? zip))                        \n" +
                    "  (assert (== 4 (io/zip-size zip)))                     \n" +
                    "                                                        \n" +
                    "  (io/delete-file a1 a2 a3)                             \n" +
                    "  (assert (== 0 (count (io/list-files zip-dir))))       \n" +
                    "  (io/delete-file zip-dir)                              \n" +
                    "                                                        \n" +
                    "  (io/unzip-to-dir zip base-dir)                        \n" +
                    "                                                        \n" +
                    "  (assert (== 3 (count (io/list-files zip-dir))))       \n" +
                    "  (io/delete-file a1 a2 a3)                             \n" +
                    "  (assert (== 0 (count (io/list-files zip-dir))))       \n" +
                    "                                                        \n" +
                    "  (io/delete-file a1 a2 a3 zip-dir)                     \n" +
                    "  (io/delete-file zip)                                  \n" +
                    "  (assert (== 0 (count (io/list-files base-dir))))      \n" +
                    "  (io/delete-file base-dir)                             \n" +
                    ")");
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void test_io_zip_file_add_dir_filter() {
        final Venice venice = new Venice();

        try {
            venice.eval(
                    "(let [base-dir (io/file (io/temp-dir \"zip-test-\"))    \n" +
                    "      zip-dir  (io/file base-dir \"test\")              \n" +
                    "      a1       (io/file zip-dir \"a1.txt\")             \n" +
                    "      a2       (io/file zip-dir \"a2.png\")             \n" +
                    "      a3       (io/file zip-dir \"a3.txt\")             \n" +
                    "      zip      (io/file base-dir \"a.zip\")]            \n" +
                    "                                                        \n" +
                    "  (io/mkdir zip-dir)                                    \n" +
                    "  (io/spit a1 \"a1\")                                   \n" +
                    "  (io/spit a2 \"a2\")                                   \n" +
                    "  (io/spit a3 \"a3\")                                   \n" +
                    "  (assert (== 3 (count (io/list-files zip-dir))))       \n" +
                    "                                                        \n" +
                    "  (io/zip-file :filter-fn (fn [d n] (str/ends-with? n \".txt\"))  \n" +
                    "               zip                                      \n" +
                    "               zip-dir)                                 \n" +
                    "  (assert (io/exists-file? zip))                        \n" +
                    "  (assert (== 3 (io/zip-size zip)))                     \n" +
                    "                                                        \n" +
                    "  (io/delete-file a1 a2 a3)                             \n" +
                    "  (assert (== 0 (count (io/list-files zip-dir))))       \n" +
                    "  (io/delete-file zip-dir)                              \n" +
                    "                                                        \n" +
                    "  (io/unzip-to-dir zip base-dir)                        \n" +
                    "                                                        \n" +
                    "  (assert (== 2 (count (io/list-files zip-dir))))       \n" +
                    "  (io/delete-file a1 a2 a3)                             \n" +
                    "  (assert (== 0 (count (io/list-files zip-dir))))       \n" +
                    "                                                        \n" +
                    "  (io/delete-file a1 a2 a3 zip-dir)                     \n" +
                    "  (io/delete-file zip)                                  \n" +
                    "  (assert (== 0 (count (io/list-files base-dir))))      \n" +
                    "  (io/delete-file base-dir)                             \n" +
                    ")");
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void test_io_zip_append() {
        final Venice venice = new Venice();

        try {
            venice.eval(
                    "(let [base-dir (io/file (io/temp-dir \"zip-test-\"))    \n" +
                    "      zip      (io/file base-dir \"x.zip\")             \n" +
                    "      data1     (bytebuf-from-string \"abc\" :utf-8)    \n" +
                    "      data2     (bytebuf-from-string \"def\" :utf-8)]   \n" +
                    "                                                        \n" +
                    "  ; create the initial zip                              \n" +
                    "  (io/spit zip (io/zip \"a.txt\" data1))                \n" +
                    "                                                        \n" +
                    "  ; append files                                        \n" +
                    "  (io/zip-append zip \"b.txt\" data1)                   \n" +
                    "  (io/zip-append zip \"c.txt\" data1)                   \n" +
                    "  (io/zip-append zip                                    \n" +
                    "                 \"d.txt\" data1                        \n" +
                    "                 \"e.txt\" data1                        \n" +
                    "                 \"f.txt\" data1)                       \n" +
                    "  (assert (== 6 (io/zip-size zip)))                     \n" +
                    "                                                        \n" +
                    "  ; append files with overwrite                         \n" +
                    "  (io/zip-append zip \"e.txt\" data2)                   \n" +
                    "  (io/zip-append zip                                    \n" +
                    "                 \"f.txt\" data2                        \n" +
                    "                 \"g.txt\" data2                        \n" +
                    "                 \"h.txt\" data2)                       \n" +
                    "  (assert (== 8 (io/zip-size zip)))                     \n" +
                    "                                                        \n" +
                    "  ; add empty dir                                       \n" +
                    "  (io/zip-append zip \"x/\" nil)                        \n" +
                    "  (assert (== 9 (io/zip-size zip)))                     \n" +
                    "                                                        \n" +
                    "  ; append files with dir                               \n" +
                    "  (io/zip-append zip \"y/a.txt\" data1)                 \n" +
                    "  (io/zip-append zip \"y/b.txt\" data1)                 \n" +
                    "  (io/zip-append zip                                    \n" +
                    "                 \"y/c.txt\" data1                      \n" +
                    "                 \"z/d.txt\" data1                      \n" +
                    "                 \"z/e.txt\" data1)                     \n" +
                    "  (assert (== 16 (io/zip-size zip)))                    \n" +
                    "                                                        \n" +
                    "  ; test a.txt                                          \n" +
                    "  (-> (io/unzip zip \"a.txt\")                          \n" +
                    "      (bytebuf-to-string :utf-8)                        \n" +
                    "      (== \"abc\"))                                     \n" +
                    "                                                        \n" +
                    "  ; test e.txt                                          \n" +
                    "  (-> (io/unzip zip \"e.txt\")                          \n" +
                    "      (bytebuf-to-string :utf-8)                        \n" +
                    "      (== \"def\"))                                     \n" +
                    "                                                        \n" +
                    "  ; test z/e.txt                                        \n" +
                    "  (-> (io/unzip zip \"z/e.txt\")                        \n" +
                    "      (bytebuf-to-string :utf-8)                        \n" +
                    "      (== \"abc\"))                                     \n" +
                    "                                                        \n" +
                    "  (io/delete-file zip)                                  \n" +
                    "  (io/delete-file base-dir)                             \n" +
                    ")");
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void test_io_zip_Q() throws Exception {
        final Venice venice = new Venice();

        assertTrue((Boolean)venice.eval(
                                "(-> (io/zip \"test\" (bytebuf-from-string \"abcdef\" :utf-8)) \n" +
                                "    (io/zip? ))"));

        assertFalse((Boolean)venice.eval(
                                "(-> (io/gzip (bytebuf-from-string \"abcdef\" :utf-8)) \n" +
                                "    (io/zip? ))"));

        assertFalse((Boolean)venice.eval("(io/zip? (bytebuf [1 2 3 4]))"));
    }

    @Test
    public void test_io_zip_size() throws Exception {
        final Venice venice = new Venice();

        assertEquals(1L, venice.eval(
                                "(-> (io/zip \"a\" (bytebuf-from-string \"abc\" :utf-8)) \n" +
                                "    (io/zip-size))"));

        assertEquals(2L, venice.eval(
                                "(-> (io/zip \"a\" (bytebuf-from-string \"abc\" :utf-8)  \n" +
                                "            \"b\" (bytebuf-from-string \"def\" :utf-8)) \n" +
                                "    (io/zip-size))"));
    }

    @Test
    public void test_io_unzip_first() throws Exception {
        final Venice venice = new Venice();

        assertEquals("abcdef", new String(
                                    ((ByteBuffer)venice.eval(
                                            "(-> (io/zip \"test\" (bytebuf-from-string \"abcdef\" :utf-8)) \n" +
                                            "    (io/unzip-first))")).array(),
                                    "utf-8"));
    }

    @Test
    public void test_io_unzip_first_maliciuos_entry() throws Exception {
        final Venice venice = new Venice();

        final String script = "(->> (io/zip \"../../a\" (bytebuf-from-string \"abc\" :utf-8)) \n" +
                              "     (io/unzip-first))                                           ";

        try {
            venice.eval(script);

            fail("Expected a VncException");
        }
        catch(VncException ex) {
            assertEquals("ZIP entry slips ../../a a potential target dir!", ex.getMessage());
        }
    }

    @Test
    public void test_io_unzip_nth() throws Exception {
        final Venice venice = new Venice();

        assertEquals("abc", new String(
                                    ((ByteBuffer)venice.eval(
                                            "(-> (io/zip \"a\" (bytebuf-from-string \"abc\" :utf-8)) \n" +
                                            "    (io/unzip-nth 0))")).array(),
                                    "utf-8"));

        assertEquals("def", new String(
                                    ((ByteBuffer)venice.eval(
                                            "(-> (io/zip \"a\" (bytebuf-from-string \"abc\" :utf-8)  \n" +
                                            "            \"b\" (bytebuf-from-string \"def\" :utf-8)) \n" +
                                            "    (io/unzip-nth 1))")).array(),
                                    "utf-8"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test_io_unzip_all() throws Exception {
        final Venice venice = new Venice();

        Map<String,ByteBuffer> data = (Map<String,ByteBuffer>)venice.eval(
                                        "(->> (io/zip \"a\" (bytebuf-from-string \"abc\" :utf-8)  \n" +
                                        "             \"b\" (bytebuf-from-string \"def\" :utf-8)  \n" +
                                        "             \"c\" (bytebuf-from-string \"ghi\" :utf-8)) \n" +
                                        "     (io/unzip-all))");

        assertEquals(3, data.size());
        assertEquals("abc", new String(data.get("a").array(), "utf-8"));
        assertEquals("def", new String(data.get("b").array(), "utf-8"));
        assertEquals("ghi", new String(data.get("c").array(), "utf-8"));
    }

    @Test
    public void test_io_unzip_all_maliciuos_entry() throws Exception {
        final Venice venice = new Venice();

        final String script = "(->> (io/zip \"../../a\" (bytebuf-from-string \"abc\" :utf-8)) \n" +
                              "     (io/unzip-all))                                           ";

        try {
            venice.eval(script);

            fail("Expected a VncException");
        }
        catch(VncException ex) {
            assertEquals("ZIP entry slips ../../a a potential target dir!", ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test_io_unzip_all_glob() throws Exception {
        final Venice venice = new Venice();

        Map<String,ByteBuffer> data = (Map<String,ByteBuffer>)venice.eval(
                                        "(->> (io/zip \"a.txt\" (bytebuf-from-string \"abc\" :utf-8)  \n" +
                                        "             \"b.txt\" (bytebuf-from-string \"def\" :utf-8)  \n" +
                                        "             \"c.log\" (bytebuf-from-string \"ghi\" :utf-8)) \n" +
                                        "     (io/unzip-all \"*.txt\"))");

        assertEquals(2, data.size());
        assertEquals("abc", new String(data.get("a.txt").array(), "utf-8"));
        assertEquals("def", new String(data.get("b.txt").array(), "utf-8"));
    }

    @Test
    public void test_io_unzip_all_glob_maliciuos_entry() throws Exception {
        final Venice venice = new Venice();

        final String script = "(->> (io/zip \"../../a\" (bytebuf-from-string \"abc\" :utf-8)) \n" +
                              "     (io/unzip-all \"*.txt\"))                                 ";

        try {
            venice.eval(script);

            fail("Expected a VncException");
        }
        catch(VncException ex) {
            assertEquals("ZIP entry slips ../../a a potential target dir!", ex.getMessage());
        }
    }

    @Test
    public void test_io_gzip() throws Exception {
        final Venice venice = new Venice();

        assertEquals(null, venice.eval("(io/ungzip (io/gzip nil))"));
        assertEquals("abcdef", new String(
                                    ((ByteBuffer)venice.eval("(io/ungzip (io/gzip (bytebuf-from-string \"abcdef\" :utf-8)))")).array(),
                                    "utf-8"));
    }

    @Test
    public void test_io_gzip_Q() throws Exception {
        final Venice venice = new Venice();

        assertTrue((Boolean)venice.eval(
                                "(-> (io/gzip (bytebuf-from-string \"abcdef\" :utf-8)) \n" +
                                "    (io/gzip? ))"));

        assertFalse((Boolean)venice.eval(
                                "(-> (io/zip \"test\" (bytebuf-from-string \"abcdef\" :utf-8)) \n" +
                                "    (io/gzip? ))"));

        assertFalse((Boolean)venice.eval("(io/gzip? (bytebuf [1 2 3 4]))"));
    }

    @Test
    public void test_io_gzip_to_stream() throws Exception {
        final Venice venice = new Venice();

        assertEquals("abcdef",
                venice.eval(
                        "(do                                                 \n" +
                        "  (import :java.io.ByteArrayOutputStream)           \n" +
                        "  (try-with [os (. :ByteArrayOutputStream :new)]    \n" +
                        "    (do                                             \n" +
                        "      (-> (bytebuf-from-string \"abcdef\" :utf-8)   \n" +
                        "          (io/gzip-to-stream os))                   \n" +
                        "      (-> (. os :toByteArray)                       \n" +
                        "          (io/ungzip)                               \n" +
                        "          (bytebuf-to-string :utf-8)))))              "));
    }

    @Test
    public void test_io_ungzip_to_stream() throws Exception {
        final Venice venice = new Venice();

        assertEquals("abcdef",
                venice.eval(
                    "(-> (bytebuf-from-string \"abcdef\" :utf-8) \n" +
                    "    (io/gzip) \n" +
                    "    (io/ungzip-to-stream) \n" +
                    "    (io/slurp-stream :binary false :encoding :utf-8))"));
    }

}
