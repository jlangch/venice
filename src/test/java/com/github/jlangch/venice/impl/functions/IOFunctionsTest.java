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

import static com.github.jlangch.venice.impl.VeniceClasspath.getVeniceBasePath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;


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
    public void test_io_list_files() throws Exception{
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
    public void test_io_list_files_glob() throws Exception{
        final Venice venice = new Venice();

        final File file1 = File.createTempFile("spit-", "-1.txt");
        final File file2 = File.createTempFile("spit-", "-2.txt");
        final File file3 = File.createTempFile("spit-", "-2.xml");

        file1.deleteOnExit();
        file2.deleteOnExit();
        file3.deleteOnExit();

        try {
            venice.eval("(io/spit file \"123\" :append true)", Parameters.of("file", file1));
            venice.eval("(io/spit file \"123\" :append true)", Parameters.of("file", file2));
            venice.eval("(io/spit file \"123\" :append true)", Parameters.of("file", file3));

            final File dir = file1.getParentFile();

            final Map<String,Object> params = Parameters.of("dir", dir);

            assertEquals(3L, venice.eval("(count (io/list-files-glob dir \"spit-*.*\"))", params));
            assertEquals(2L, venice.eval("(count (io/list-files-glob dir \"spit-*.txt\"))", params));
            assertEquals(1L, venice.eval("(count (io/list-files-glob dir \"spit-*.?ml\"))", params));
            assertEquals(3L, venice.eval("(count (io/list-files-glob dir \"spit-*.{txt,xml}\"))", params));
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void test_io_tmp_dir() {
        final Venice venice = new Venice();

        assertTrue((Boolean)venice.eval("(not-empty? (io/tmp-dir))"));
        assertTrue((Boolean)venice.eval("(io/file? (io/tmp-dir))"));
    }

    @Test
    public void test_io_spit() {
        final Venice venice = new Venice();

        // with default encoding
        try {
            final File file = File.createTempFile("spit", ".txt");
            file.deleteOnExit();

            venice.eval(
                    "(io/spit file \"123456789\" :append true)",
                    Parameters.of("file", file.getAbsolutePath()));

            assertEquals(
                    "123456789",
                    venice.eval(
                            "(io/slurp file)",
                            Parameters.of("file", file.getAbsolutePath())));
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }

        // with UTF-8 encoding
        try {
            final File file = File.createTempFile("spit", ".txt");
            file.deleteOnExit();

            venice.eval(
                    "(io/spit file \"123456789\" :append true :encoding \"UTF-8\")",
                    Parameters.of("file", file.getAbsolutePath()));

            assertEquals(
                    "123456789",
                    venice.eval(
                            "(io/slurp file :encoding \"UTF-8\")",
                            Parameters.of("file", file.getAbsolutePath())));
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void test_io_slurp() {
        final Venice venice = new Venice();

        // with default encoding
        try {
            final File file = File.createTempFile("slurp", ".txt");
            file.deleteOnExit();

            Files.write(file.toPath(), "123456789".getBytes("UTF-8"), StandardOpenOption.APPEND);

            assertEquals(
                    "123456789",
                    venice.eval(
                            "(io/slurp file)",
                            Parameters.of("file", file.getAbsolutePath())));
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }

        // with UTF-8 encoding
        try {
            final File file = File.createTempFile("slurp", ".txt");
            file.deleteOnExit();

            Files.write(file.toPath(), "123456789".getBytes("UTF-8"), StandardOpenOption.APPEND);

            assertEquals(
                    "123456789",
                    venice.eval(
                            "(io/slurp file :encoding \"UTF-8\")",
                            Parameters.of("file", file.getAbsolutePath())));
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void test_io_slurp_stream() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                        " +
                "   (import :java.io.FileInputStream)                       " +
                "   (let [file (io/temp-file \"test-\", \".txt\")]          " +
                "        (io/spit file \"123456789\" :append true)          " +
                "        (io/delete-file-on-exit file)                      " +
                "        (try-with [is (. :FileInputStream :new file)]      " +
                "           (io/slurp-stream is :binary false)))            " +
                ")";

        assertEquals("123456789",venice.eval(script));
    }

    @Test
    public void test_io_slurp_lines_file() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                     " +
                "   (let [file (io/temp-file \"test-\", \".txt\")]       " +
                "      (io/spit file \"123\n456\n789\" :append true)     " +
                "      (io/delete-file-on-exit file)                     " +
                "      (pr-str (io/slurp-lines file))))                  " +
                ")";

        assertEquals("(\"123\" \"456\" \"789\")", venice.eval(script));
    }

    @Test
    public void test_io_slurp_lines_stream_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                      " +
                "   (import :java.io.FileInputStream)                     " +
                "   (let [file (io/temp-file \"test-\", \".txt\")]        " +
                "      (io/spit file \"123\n456\n789\" :append true)      " +
                "      (io/delete-file-on-exit file)                      " +
                "      (try-with [is (. :FileInputStream :new file)]      " +
                "         (pr-str (io/slurp-lines is))))                  " +
                ")";

        assertEquals("(\"123\" \"456\" \"789\")", venice.eval(script));
    }

    @Test
    public void test_io_slurp_lines_stream_2() {
        final Venice venice = new Venice();

        final String script =
                "(str (->> \"1\\n2\\n3\"         \n" +
                "          io/string-in-stream   \n" +
                "          io/slurp-lines))";

        assertEquals("(1 2 3)", venice.eval(script));
    }

    @Test
    public void test_io_slurp_lines_reader() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                      " +
                "   (import :java.io.FileReader)                          " +
                "   (let [file (io/temp-file \"test-\", \".txt\")]        " +
                "      (io/spit file \"123\n456\n789\" :append true)      " +
                "      (io/delete-file-on-exit file)                      " +
                "      (try-with [rd (. :FileReader :new file)]           " +
                "         (pr-str (io/slurp-lines rd))))                  " +
                ")";

        assertEquals("(\"123\" \"456\" \"789\")", venice.eval(script));
    }

    @Test
    public void test_io_spit_stream() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                           " +
                "   (import :java.io.FileOutputStream)                         " +
                "   (let [file (io/temp-file \"test-\", \".txt\")]             " +
                "        (io/delete-file-on-exit file)                         " +
                "        (try-with [is (. :FileOutputStream :new file)]        " +
                "           (io/spit-stream is \"123456789\" :flush true))     " +
                "        (io/slurp file :binary false))                         " +
                ")";

        assertEquals("123456789",venice.eval(script));
    }

    @Test
    public void test_io_user_dir() {
        final Venice venice = new Venice();

        assertTrue((Boolean)venice.eval("(not-empty? (io/user-dir))"));
        assertTrue((Boolean)venice.eval("(io/file? (io/user-dir))"));
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

        final String resource = getVeniceBasePath() + "test.venice";

        final String script =
                "(do                                                     \n" +
                "   (-<> (identity \"" + resource + "\")                 \n" +
                "        (io/load-classpath-resource <>)                 \n" +
                "        (bytebuf-to-string <> :UTF-8)                   \n" +
                "        (str/contains? <> \"(defn test/test-fn \"))))     ";

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

    @Test
    public void test_io_zip() throws Exception {
        final Venice venice = new Venice();

        assertEquals("abcdef", new String(
                                    ((ByteBuffer)venice.eval(
                                            "(-> (io/zip \"test\" (bytebuf-from-string \"abcdef\" :utf-8)) \n" +
                                            "    (io/unzip \"test\"))")).array(),
                                    "utf-8"));
    }

}
