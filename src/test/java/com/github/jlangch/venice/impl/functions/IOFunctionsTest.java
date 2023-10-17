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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.Charset;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.util.io.ClassPathResource;


public class IOFunctionsTest {

    @Test
    public void test_io_copy_file() {
        final Venice venice = new Venice();

        try {
            final File from = File.createTempFile("from__", ".txt");
            from.deleteOnExit();
            venice.eval(
                    "(io/spit file \"123456789\" :append true)",
                    Parameters.of("file", from));

            final File to = File.createTempFile("to__", ".txt");
            to.delete();
            to.deleteOnExit();

            assertTrue((Boolean)venice.eval("(io/exists-file? f))", Parameters.of("f", from)));
            assertFalse((Boolean)venice.eval("(io/exists-file? f))", Parameters.of("f", to)));

            venice.eval("(io/copy-file from to))", Parameters.of("from", from, "to", to));

            assertTrue((Boolean)venice.eval("(io/exists-file? f))", Parameters.of("f", from)));
            assertTrue((Boolean)venice.eval("(io/exists-file? f))", Parameters.of("f", to)));

            venice.eval("(io/delete-file f))", Parameters.of("f", from));
            venice.eval("(io/delete-file f))", Parameters.of("f", to));

            assertFalse((Boolean)venice.eval("(io/exists-file? f))", Parameters.of("f", from)));
            assertFalse((Boolean)venice.eval("(io/exists-file? f))", Parameters.of("f", to)));
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void test_io_delete_file() {
        final Venice venice = new Venice();

        try {
            final File file1 = File.createTempFile("spit", ".txt");
            final File file2 = File.createTempFile("spit", ".txt");
            final File file3 = File.createTempFile("spit", ".txt");

            file1.deleteOnExit();
            file2.deleteOnExit();
            file3.deleteOnExit();

            venice.eval(
                    "(io/spit file \"123456789\" :append true)",
                    Parameters.of("file", file1));
            venice.eval(
                    "(io/spit file \"123456789\" :append true)",
                    Parameters.of("file", file2));
            venice.eval(
                    "(io/spit file \"123456789\" :append true)",
                    Parameters.of("file", file3));

            assertTrue((Boolean)venice.eval("(io/exists-file? f))", Parameters.of("f", file1)));
            assertTrue((Boolean)venice.eval("(io/exists-file? f))", Parameters.of("f", file2)));
            assertTrue((Boolean)venice.eval("(io/exists-file? f))", Parameters.of("f", file3)));

            venice.eval("(io/delete-file f))", Parameters.of("f", file1));

            venice.eval("(io/delete-file f1 f2))", Parameters.of("f1", file2, "f2", file3));

            assertFalse((Boolean)venice.eval("(io/exists-file? f))", Parameters.of("f", file1)));
            assertFalse((Boolean)venice.eval("(io/exists-file? f))", Parameters.of("f", file2)));
            assertFalse((Boolean)venice.eval("(io/exists-file? f))", Parameters.of("f", file3)));
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void test_io_exists_dir_Q() {
        final Venice venice = new Venice();

        assertTrue((Boolean)venice.eval("(io/exists-dir? (io/user-dir))"));
    }

    @Test
    public void test_io_file() {
        final Venice venice = new Venice();

        assertTrue((Boolean)venice.eval("(io/file? (io/file \"/tmp\"))"));
        assertTrue((Boolean)venice.eval("(io/file? (io/file \"/tmp\" \"a.txt\"))"));
        assertTrue((Boolean)venice.eval("(io/file? (io/file (io/file \"/tmp\") \"a.txt\"))"));
    }

    @Test
    public void test_io_file_parent() {
        final Venice venice = new Venice();

        assertEquals("/tmp/test", venice.eval("(io/file-path (io/file-parent (io/file \"/tmp/test/x.txt\")))"));
    }

    @Test
    public void test_io_file_path() {
        final Venice venice = new Venice();

        assertEquals("/tmp/test/x.txt", venice.eval("(io/file-path (io/file \"/tmp/test/x.txt\"))"));
    }

    @Test
    public void test_io_file_name() {
        final Venice venice = new Venice();

        assertEquals("x.txt", venice.eval("(io/file-name (io/file \"/tmp/test/x.txt\"))"));
    }

    @Test
    public void test_io_file_Q() {
        final Venice venice = new Venice();

        assertTrue((Boolean)venice.eval("(io/file? (io/file \"/tmp\"))"));
    }

    @Test
    public void test_io_file_ext_Q() {
        final Venice venice = new Venice();

        assertTrue((Boolean)venice.eval("(io/file-ext? \"some.png\" \"png\")"));
        assertTrue((Boolean)venice.eval("(io/file-ext? \"some.png\" \".png\")"));

        assertTrue((Boolean)venice.eval("(io/file-ext? \"/tmp/some.png\" \"png\")"));
        assertTrue((Boolean)venice.eval("(io/file-ext? \"/tmp/some.png\" \".png\")"));

        assertTrue((Boolean)venice.eval("(io/file-ext? (io/file \"some.png\") \"png\")"));
        assertTrue((Boolean)venice.eval("(io/file-ext? (io/file \"some.png\") \".png\")"));

        assertTrue((Boolean)venice.eval("(io/file-ext? (io/file \"/tmp/some.png\") \"png\")"));
        assertTrue((Boolean)venice.eval("(io/file-ext? (io/file \"/tmp/some.png\") \".png\")"));
    }

    @Test
    public void test_io_file_ext() {
        final Venice venice = new Venice();

        assertEquals("png", venice.eval("(io/file-ext \"some.png\"))"));
        assertEquals(null, venice.eval("(io/file-ext \"some\"))"));

        assertEquals("png", venice.eval("(io/file-ext \"/tmp/some.png\"))"));
        assertEquals(null, venice.eval("(io/file-ext \"/tmp/some\"))"));

        assertEquals("png", venice.eval("(io/file-ext (io/file \"some.png\"))"));
        assertEquals(null, venice.eval("(io/file-ext (io/file \"some\"))"));

        assertEquals("png", venice.eval("(io/file-ext (io/file \"/tmp/some.png\"))"));
        assertEquals(null, venice.eval("(io/file-ext (io/file \"/tmp/some\"))"));
    }

    @Test
    public void test_io_file_within_dir_Q() {
        final Venice venice = new Venice();

        assertTrue((Boolean)venice.eval("(io/file-within-dir? (io/file \"/temp/foo\")          \n" +
                                        "                     (io/file \"/temp/foo/img.png\")) "));

        assertTrue((Boolean)venice.eval("(io/file-within-dir? (io/file \"/temp/foo\")              \n" +
                                        "                     (io/file \"/temp/foo/bar/img.png\")) "));

        assertTrue((Boolean)venice.eval("(io/file-within-dir? (io/file \"/temp/foo\")                 \n" +
                                        "                     (io/file \"/temp/foo/../foo/img.png\")) "));

        assertTrue((Boolean)venice.eval("(io/file-within-dir? (io/file \"/temp/foo\")                     \n" +
                                        "                     (io/file \"/temp/foo/../foo/bar/img.png\")) "));



        assertFalse((Boolean)venice.eval("(io/file-within-dir? (io/file \"/temp/foo\")          \n" +
                                         "                     (io/file \"/temp/zoo/img.png\")) "));

        assertFalse((Boolean)venice.eval("(io/file-within-dir? (io/file \"/temp/foo\")              \n" +
                                         "                     (io/file \"/temp/zoo/bar/img.png\")) "));

        assertFalse((Boolean)venice.eval("(io/file-within-dir? (io/file \"/temp/foo\")                 \n" +
                                         "                     (io/file \"/temp/foo/../bar/img.png\")) "));

        assertFalse((Boolean)venice.eval("(io/file-within-dir? (io/file \"/temp/foo\")                     \n" +
                                         "                     (io/file \"/temp/foo/../zoo/bar/img.png\")) "));



        assertThrows(VncException.class, () -> venice.eval("(io/file-within-dir? (io/file \"foo\")                \n" +
                                                           "                     (io/file \"/temp/foo/img.png\")) "));

        assertThrows(VncException.class, () -> venice.eval("(io/file-within-dir? (io/file \"/temp/foo\")          \n" +
                                                           "                     (io/file \"foo/img.png\"))       "));
    }

    @Test
    public void test_io_glob_path_matcher() {
        final Venice venice = new Venice();

        assertEquals(
                "com.github.jlangch.venice.impl.util.VncPathMatcher",
                venice.eval("(type (io/glob-path-matcher \"*.log\"))"));

        assertEquals(
                "com.github.jlangch.venice.impl.util.VncPathMatcher",
                venice.eval("(type (io/glob-path-matcher \"**/*.log\"))"));
    }

    @Test
    public void test_io_file_matches_globQ() {
        final Venice venice = new Venice();

        assertTrue((Boolean)venice.eval("(io/file-matches-glob? \"*.log\" \"file1.log\")"));
        assertFalse((Boolean)venice.eval("(io/file-matches-glob? \"*.log\" \"dir/file1.log\")"));
        assertTrue((Boolean)venice.eval("(io/file-matches-glob? \"**/*.log\" \"dir/file1.log\")"));
        assertTrue((Boolean)venice.eval("(io/file-matches-glob? \"**/*.log\" \"dir1/dir2/file1.log\")"));

        assertTrue((Boolean)venice.eval("(io/file-matches-glob? \"*.log\" (io/file \"file1.log\"))"));
        assertFalse((Boolean)venice.eval("(io/file-matches-glob? \"*.log\" (io/file \"dir/file1.log\"))"));
        assertTrue((Boolean)venice.eval("(io/file-matches-glob? \"**/*.log\" (io/file \"dir/file1.log\"))"));
        assertTrue((Boolean)venice.eval("(io/file-matches-glob? \"**/*.log\" (io/file \"dir1/dir2/file1.log\"))"));

        assertTrue((Boolean)venice.eval("(io/file-matches-glob? (io/glob-path-matcher\"*.log\") \"file1.log\")"));
        assertFalse((Boolean)venice.eval("(io/file-matches-glob? (io/glob-path-matcher\"*.log\") \"dir/file1.log\")"));
        assertTrue((Boolean)venice.eval("(io/file-matches-glob? (io/glob-path-matcher\"**/*.log\") \"dir/file1.log\")"));
        assertTrue((Boolean)venice.eval("(io/file-matches-glob? (io/glob-path-matcher\"**/*.log\") \"dir1/dir2/file1.log\")"));

        assertTrue((Boolean)venice.eval("(io/file-matches-glob? (io/glob-path-matcher\"*.log\") (io/file \"file1.log\"))"));
        assertFalse((Boolean)venice.eval("(io/file-matches-glob? (io/glob-path-matcher\"*.log\") (io/file \"dir/file1.log\"))"));
        assertTrue((Boolean)venice.eval("(io/file-matches-glob? (io/glob-path-matcher\"**/*.log\") (io/file \"dir/file1.log\"))"));
        assertTrue((Boolean)venice.eval("(io/file-matches-glob? (io/glob-path-matcher\"**/*.log\") (io/file \"dir1/dir2/file1.log\"))"));
   }

    @Test
    public void test_io_list_files_1() throws Exception{
        final Venice venice = new Venice();

        final File file1 = File.createTempFile("spit-list", "-1.txt");
        final File file2 = File.createTempFile("spit-list", "-2.txt");

        file1.deleteOnExit();
        file2.deleteOnExit();

        try {
            venice.eval("(io/spit file \"123\" :append true)", Parameters.of("file", file1));
            venice.eval("(io/spit file \"123\" :append true)", Parameters.of("file", file2));

            final File dir = file1.getParentFile();

            assertTrue(
                    ((Long)venice.eval(
                            "(count (io/list-files dir))",
                            Parameters.of("dir", dir))
                    ).longValue() > 2);

            assertEquals(Long.valueOf(2),
                    venice.eval(
                            "(count " +
                            "  (io/list-files " +
                            "         dir " +
                            "         (fn [f] (match? (get f :name) \"spit-list.*[.]txt\"))))",
                            Parameters.of("dir", dir)));
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void test_io_list_files_2() {
        final Venice venice = new Venice();

        try {
            venice.eval(
                "(let [dir (io/file (io/temp-dir \"test-\"))         \n" +
                "      a1  (io/touch-file (io/file dir \"a1.txt\"))  \n" +
                "      a2  (io/touch-file (io/file dir \"a2.txt\"))  \n" +
                "      a3  (io/touch-file (io/file dir \"a3.txt\"))] \n" +
                "                                                    \n" +
                "  (assert (== 3 (count (io/list-files dir))))       \n" +
                "  (io/delete-file-tree dir)                         \n" +
                ")");
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void test_io_list_files_glob() throws Exception {
        final Venice venice = new Venice();

        try {
            venice.eval(
                "(let [dir (io/file (io/temp-dir \"test-\"))                           \n" +
                "      a1  (io/touch-file (io/file dir \"s-a1.txt\"))                  \n" +
                "      a2  (io/touch-file (io/file dir \"s-a2.txt\"))                  \n" +
                "      a3  (io/touch-file (io/file dir \"s-a3.xml\"))]                 \n" +
                "                                                                      \n" +
                "  (assert (== 3, (count (io/list-files-glob dir \"s-*.*\"))))         \n" +
                "  (assert (== 2, (count (io/list-files-glob dir \"s-*.txt\"))))       \n" +
                "  (assert (== 1, (count (io/list-files-glob dir \"s-*.?ml\"))))       \n" +
                "  (assert (== 3, (count (io/list-files-glob dir \"s-*.{txt,xml}\")))) \n" +
                "  (io/delete-file-tree dir)                                           \n" +
                ")");
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void test_io_delete_file_tree() {
        final Venice venice = new Venice();

        try {
            venice.eval(
                    "(let [dir (io/file (io/temp-dir \"test-\"))          \n" +
                    "      a1  (io/touch-file (io/file dir \"a1.txt\"))   \n" +
                    "      a2  (io/touch-file (io/file dir \"a2.txt\"))   \n" +
                    "      a3  (io/touch-file (io/file dir \"a3.txt\"))]  \n" +
                    "                                                     \n" +
                    "  (io/delete-file-tree dir)                          \n" +
                    "  (assert (== false (io/exists-dir? dir)))           \n" +
                    ")");
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void test_io_delete_files_glob() {
        final Venice venice = new Venice();

        try {
            venice.eval(
                    "(let [dir (io/file (io/temp-dir \"test-\"))          \n" +
                    "      a1  (io/touch-file (io/file dir \"a1.txt\"))   \n" +
                    "      a2  (io/touch-file (io/file dir \"a2.txt\"))   \n" +
                    "      a3  (io/touch-file (io/file dir \"a3.txt\"))]  \n" +
                    "                                                     \n" +
                    "  (assert (== 3 (count (io/list-files dir))))        \n" +
                    "  (io/delete-files-glob dir \"*.txt\")               \n" +
                    "  (assert (== 0 (count (io/list-files dir))))        \n" +
                    "  (assert (io/exists-dir? dir))                      \n" +
                    "  (io/delete-file dir)                               \n" +
                    ")");
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void test_io_temp_dir() {
        final Venice venice = new Venice();

        final String script =
                "(do                                     \n" +
                "  (def dir (io/temp-dir \"test-\"))     \n" +
                "  (assert (io/exists-dir? dir))         \n" +
                "  (io/delete-file-on-exit dir))         ";

        venice.eval(script);
    }

    @Test
    public void test_io_temp_file() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                         \n" +
                "  (def f (io/temp-file \"test-\" \".txt\"))                 \n" +
                "  (assert (io/exists-file? f))                              \n" +
                "  (assert (io/file-absolute f))                             \n" +
                "  (io/spit f \"123456789\" :append true)                    \n" +
                "  (io/delete-file-on-exit f)                                \n" +
                "  (assert (io/exists-file? f))                              \n" +
                "  (assert (== \"123456789\"                                 \n" +
                "              (io/slurp f :binary false :encoding :utf-8))) \n" +
                "  (assert (io/exists-file? f)))                             ";

        venice.eval(script);
    }

    @Test
    public void test_io_tmp_dir() {
        final Venice venice = new Venice();

        assertTrue((Boolean)venice.eval("(not-empty? (io/tmp-dir))"));
        assertTrue((Boolean)venice.eval("(io/file? (io/tmp-dir))"));
    }

    @Test
    public void test_io_user_dir() {
        final Venice venice = new Venice();

        assertTrue((Boolean)venice.eval("(not-empty? (io/user-dir))"));
        assertTrue((Boolean)venice.eval("(io/file? (io/user-dir))"));
    }

    @Test
    public void test_io_filesystem_usable_space() {
        final Venice venice = new Venice();

        assertTrue(venice.eval("(io/filesystem-usable-space)") instanceof Long);
        assertTrue(venice.eval("(io/filesystem-usable-space (io/file \".\"))") instanceof Long);
    }

    @Test
    public void test_io_filesystem_total_space() {
        final Venice venice = new Venice();

        assertTrue(venice.eval("(io/filesystem-total-space)") instanceof Long);
        assertTrue(venice.eval("(io/filesystem-total-space (io/file \".\"))") instanceof Long);
    }

    @Test
    public void test_io_make_venice_filename() {
        final Venice venice = new Venice();

        assertEquals(".venice", venice.eval("(io/make-venice-filename \"\")"));
        assertEquals("foo.venice", venice.eval("(io/make-venice-filename \"foo.venice\")"));
        assertEquals("foo.venice", venice.eval("(io/make-venice-filename \"foo\")"));
        assertEquals("bar/foo.venice", venice.eval("(io/make-venice-filename \"bar/foo.venice\")"));
        assertEquals("bar/foo.venice", venice.eval("(io/make-venice-filename \"bar/foo\")"));

        assertEquals(new File("foo.venice"), venice.eval("(io/make-venice-filename (io/file \"foo.venice\"))"));
        assertEquals(new File("foo.venice"), venice.eval("(io/make-venice-filename (io/file \"foo\"))"));
        assertEquals(new File("bar/foo.venice"), venice.eval("(io/make-venice-filename (io/file \"bar/foo.venice\"))"));
        assertEquals(new File("bar/foo.venice"), venice.eval("(io/make-venice-filename (io/file \"bar/foo\"))"));
    }


    @Test
    public void test_io_mime_type() {
        final Venice venice = new Venice();

        assertEquals("application/pdf",venice.eval("(io/mime-type \"document.pdf\")"));
        assertEquals("application/pdf",venice.eval("(io/mime-type (io/file \"document.pdf\"))"));
    }

    @Test
    public void test_io_load_classpath_resource() {
        final Venice venice = new Venice();

        final String resource = ClassPathResource.toPath(Venice.class.getPackage(), "test-support.venice");

        final String script =
                "(do                                                               \n" +
                "   (-<> (identity \"" + resource + "\")                           \n" +
                "        (io/load-classpath-resource <>)                           \n" +
                "        (bytebuf-to-string <> :UTF-8)                             \n" +
                "        (str/contains? <> \"(defn test-support/test-fn \"))))     ";

        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void test_io_default_charset() {
        final Venice venice = new Venice();

        final String charset = Charset.defaultCharset().name();
        assertEquals(charset, venice.eval("(io/default-charset)"));
    }

    @Test
    public void test_shell_with_out_str() {
        final Venice venice = new Venice();

        assertEquals("s: \n", venice.eval("(str \"s: \" (with-out-str (newline)))"));
        assertEquals("s: hello", venice.eval("(str \"s: \" (with-out-str (print \"hello\")))"));
        assertEquals("s: hello\n", venice.eval("(str \"s: \" (with-out-str (println \"hello\")))"));

        assertEquals("s: hello", venice.eval("(str \"s: \" (with-out-str *out* (print \"hello\")))"));
        assertEquals("s: hello\n", venice.eval("(str \"s: \" (with-out-str *out* (println \"hello\")))"));

        assertEquals("s: abc: 100", venice.eval("(str \"s: \" (with-out-str (printf \"%s: %d\" \"abc\" 100)))"));
        assertEquals("s: abc: 100", venice.eval("(str \"s: \" (with-out-str (printf *out* \"%s: %d\" \"abc\" 100)))"));
    }

    @Test
    public void test_shell_with_err_str() {
        final Venice venice = new Venice();

        assertEquals("s: \n", venice.eval("(str \"s: \" (with-err-str (newline *err*)))"));

        assertEquals("s: hello", venice.eval("(str \"s: \" (with-err-str (print *err* \"hello\")))"));

        assertEquals("s: hello\n", venice.eval("(str \"s: \" (with-err-str (println *err* \"hello\")))"));

        assertEquals("s: abc: 100", venice.eval("(str \"s: \" (with-err-str (printf *err* \"%s: %d\" \"abc\" 100)))"));
    }

}
