/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2025 Venice
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

import static com.github.jlangch.venice.impl.util.StringUtil.to_lf;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.util.io.IOStreamUtil;


public class IOFunctionsStreamTest {

    @Test
    public void test_io_file_out_stream() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                \n" +
                "   (import :java.io.FileInputStream)                               \n" +
                "   (let [file (io/temp-file \"test-\", \".txt\")]                  \n" +
                "     (io/delete-file-on-exit file)                                 \n" +
                "      (try-with [fos (io/file-out-stream file :append false)       \n" +
                "                 wr (io/wrap-os-with-buffered-writer fos :utf-8)]  \n" +
                "        (println wr \"100\")                                       \n" +
                "        (println wr \"200\"))                                      \n" +
                "     (try-with [rd (io/buffered-reader file :encoding :utf-8)]     \n" +
                "        (pr-str [(read-line rd) (read-line rd)]))))               ";

        assertEquals("[\"100\" \"200\"]",venice.eval(script));
    }

    @Test
    public void test_io_buffered_reader() {
        final Venice venice = new Venice();

        final String script1 =
                "(do                                                                \n" +
                "   (import :java.io.FileInputStream)                               \n" +
                "   (let [file (io/temp-file \"test-\", \".txt\")]                  \n" +
                "      (io/delete-file-on-exit file)                                \n" +
                "      (io/spit file \"100\n200\" :append false)                    \n" +
                "      (try-with [rd (io/buffered-reader file :encoding :utf-8)]    \n" +
                "         (pr-str [(read-line rd) (read-line rd)]))))               ";

        final String script2 =
                "(try-with [rd (io/buffered-reader \"100\n200\")]   \n" +
                "   (pr-str [(read-line rd) (read-line rd)]))        ";

        assertEquals("[\"100\" \"200\"]",venice.eval(script1));

        assertEquals("[\"100\" \"200\"]",venice.eval(script2));
    }

    @Test
    public void test_io_buffered_writer() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                \n" +
                "   (import :java.io.FileInputStream)                               \n" +
                "   (let [file (io/temp-file \"test-\", \".txt\")]                  \n" +
                "     (io/delete-file-on-exit file)                                 \n" +
               "      (try-with [wr (io/buffered-writer file)]                      \n" +
                "        (println wr \"100\")                                       \n" +
                "        (println wr \"200\"))                                      \n" +
                "     (try-with [rd (io/buffered-reader file :encoding :utf-8)]     \n" +
                "        (pr-str [(read-line rd) (read-line rd)]))))               ";

        assertEquals("[\"100\" \"200\"]",venice.eval(script));
    }

    @Test
    public void test_io_string_reader() {
        final Venice venice = new Venice();

        final String script1 =
                "(try-with [rd (io/string-reader \"1234\")]       \n" +
                "  (pr-str [ (read-char rd)                       \n" +
                "            (read-char rd)                       \n" +
                "            (read-char rd) ]))                   ";

        final String script2 =
                "(let [rd (io/string-reader \"1\\n2\\n3\\n4\")]   \n" +
                "  (try-with [br (io/buffered-reader rd)]         \n" +
                "    (pr-str [ (read-line br)                     \n" +
                "              (read-line br)                     \n" +
                "              (read-line br) ])))                ";

        assertEquals("[#\\1 #\\2 #\\3]",venice.eval(script1));
        assertEquals("[\"1\" \"2\" \"3\"]",venice.eval(script2));
    }

    @Test
    public void test_io_string_writer() {
        final Venice venice = new Venice();

        final String script =
                "(try-with [sw (io/string-writer)]     \n" +
                "  (print sw 100)                      \n" +
                "  (print sw \"-\")                    \n" +
                "  (print sw 200)                      \n" +
                "  (flush sw)                          \n" +
                "  @sw)                                ";

        assertEquals("100-200",venice.eval(script));
    }

    @Test
    public void test_io_bytebuf_out_stream() {
        final Venice venice = new Venice();

        final String script =
                "(try-with [os (io/bytebuf-out-stream)                        \n" +
                "           wr (io/wrap-os-with-buffered-writer os :utf-8)]   \n" +
                "  (println wr \"100\")                                       \n" +
                "  (println wr \"200\")                                       \n" +
                "  (flush wr)                                                 \n" +
                "  (bytebuf-to-string @os :utf-8))                            ";

        assertEquals("100\n200\n", to_lf(venice.eval(script)));
    }

    @Test
    public void test_io_bytebuf_in_stream() {
        final Venice venice = new Venice();

        final String script =
                "(let [data (bytebuf [108 105 110 101 32 49 10 108 105 110 101 32 50])]    \n" +
                "  (try-with [is (io/bytebuf-in-stream data)                               \n" +
                "             rd (io/wrap-is-with-buffered-reader is :utf-8)]              \n" +
                "    (pr-str [(read-line rd) (read-line rd)])))                            ";

        assertEquals("[\"line 1\" \"line 2\"]",venice.eval(script));
    }

    @Test
    public void test_io_wrap_os_with_buffered_writer() {
        final Venice venice = new Venice();

        final String script =
                "(try-with [os (io/bytebuf-out-stream)                        \n" +
                "           wr (io/wrap-os-with-buffered-writer os :utf-8)]   \n" +
                "  (println wr \"100\")                                       \n" +
                "  (println wr \"200\")                                       \n" +
                "  (flush wr)                                                 \n" +
                "  (bytebuf-to-string @os :utf-8))                            ";

        assertEquals("100\n200\n", to_lf(venice.eval(script)));
    }

    @Test
    public void test_io_wrap_is_with_buffered_reader() {
        final Venice venice = new Venice();

        final String script =
                "(let [data (bytebuf [108 105 110 101 32 49 10 108 105 110 101 32 50])]    \n" +
                "  (try-with [is (io/bytebuf-in-stream data)                               \n" +
                "             rd (io/wrap-is-with-buffered-reader is :utf-8)]              \n" +
                "    (pr-str [(read-line rd) (read-line rd)])))                            ";

        assertEquals("[\"line 1\" \"line 2\"]",venice.eval(script));
    }

    @Test
    public void test_io_wrap_is_with_gzip_input_stream() {
        final Venice venice = new Venice();

        final String script =
                "(let [text      \"hello, hello, hello\"                        \n" +
                "      gzip-buf  (io/gzip (bytebuf-from-string text :utf-8))]   \n" +
                "  (try-with [is (-> (io/bytebuf-in-stream gzip-buf)            \n" +
                "                    (io/wrap-is-with-gzip-input-stream))]      \n" +
                "    (-> (io/slurp is :binary true)                             \n" +
                "        (bytebuf-to-string :utf-8))))                          ";

        assertEquals("hello, hello, hello",venice.eval(script));
    }

    @Test
    public void test_io_wrap_os_with_gzip_output_stream() {
        final Venice venice = new Venice();

        final String script =
                "(let [text \"hello, hello, hello\"                          \n" +
                "      bos (io/bytebuf-out-stream)]                          \n" +
                "  (try-with [gos (io/wrap-os-with-gzip-output-stream bos)]  \n" +
                "    (io/spit gos text :encoding :utf-8)                     \n" +
                "    (io/flush gos)                                          \n" +
                "    (io/close gos)                                          \n" +
                "    (-> (io/ungzip @bos)                                    \n" +
                "        (bytebuf-to-string :utf-8))))                       ";

        assertEquals("hello, hello, hello",venice.eval(script));
    }

    @Test
    public void test_io_wrap_is_with_deflate_input_stream() {
        final Venice venice = new Venice();

        final String script =
                "(let [text      \"hello, hello, hello\"                        \n" +
                "      zlib-buf  (-> (bytebuf-from-string text :utf-8)          \n" +
                "                    (io/deflate))]                             \n" +
                "  (try-with [is (-> (io/bytebuf-in-stream zlib-buf)            \n" +
                "                    (io/wrap-is-with-inflater-input-stream))]  \n" +
                "    (-> (io/slurp is :binary true)                             \n" +
                "        (bytebuf-to-string :utf-8))))                          ";

        assertEquals("hello, hello, hello",venice.eval(script));
    }

    @Test
    public void test_io_wrap_os_with_deflate_output_stream() {
        final Venice venice = new Venice();

        final String script =
                "(let [text  \"hello, hello, hello\"                             \n" +
                "      bos   (io/bytebuf-out-stream)]                            \n" +
                "  (try-with [gos (io/wrap-os-with-deflater-output-stream bos)]  \n" +
                "    (io/spit gos text :encoding :utf-8)                         \n" +
                "    (io/flush gos)                                              \n" +
                "    (io/close gos)                                              \n" +
                "    (-> (io/inflate @bos)                                       \n" +
                "        (bytebuf-to-string :utf-8))))                           ";

        assertEquals("hello, hello, hello", venice.eval(script));
    }

    @Test
    public void test_ZLIB_compression() throws Exception {
        final String text = "hello, hello, hello";

        // OK
        final byte[] zlib1 = ZipFunctions.compress(text.getBytes("utf-8"));
        assertEquals("hello, hello, hello", new String(ZipFunctions.decompress(zlib1), "utf-8"));

        // OK (compress with DeflaterOutputStream)
        final ByteArrayOutputStream bo = new ByteArrayOutputStream();
        try (OutputStream os = new DeflaterOutputStream(bo)) {
            os.write(text.getBytes("utf-8"));
            os.flush();
        }
        final byte[] zlib2 = bo.toByteArray();
        assertEquals("hello, hello, hello", new String(ZipFunctions.decompress(zlib2), "utf-8"));


        // OK (decompress with InflaterInputStream)
        try (InputStream is = new InflaterInputStream(new ByteArrayInputStream(zlib1))) {
            final byte[] buffer = IOStreamUtil.copyIStoByteArray(is);
             assertEquals("hello, hello, hello", new String(buffer, "utf-8"));
        }
    }

    @Test
    public void test_io_read_char() {
        final Venice venice = new Venice();

        final String script =
                "(try-with [rd (io/string-reader \"1234\")]       \n" +
                "  (pr-str [ (io/read-char rd)                    \n" +
                "            (io/read-char rd)                    \n" +
                "            (io/read-char rd) ]))                   ";

        assertEquals("[#\\1 #\\2 #\\3]",venice.eval(script));
    }

    @Test
    public void test_io_read_char_to_eof() {
        final Venice venice = new Venice();

        final String script =
                "(try-with [rd (io/string-reader \"12\")]       \n" +
                "  (pr-str [ (io/read-char rd)                    \n" +
                "            (io/read-char rd)                    \n" +
                "            (io/read-char rd) ]))                   ";

        assertEquals("[#\\1 #\\2 nil]",venice.eval(script));
    }

    @Test
    public void test_io_read_line() {
        final Venice venice = new Venice();

        final String script =
                "(let [rd (io/string-reader \"1\\n2\\n3\\n4\")]   \n" +
                "  (try-with [br (io/buffered-reader rd)]         \n" +
                "    (pr-str [ (io/read-line br)                  \n" +
                "              (io/read-line br)                  \n" +
                "              (io/read-line br) ])))             ";

        assertEquals("[\"1\" \"2\" \"3\"]",venice.eval(script));
    }

    @Test
    public void test_io_read_line_to_eof() {
        final Venice venice = new Venice();

        final String script =
                "(let [rd (io/string-reader \"1\\n2\")]   \n" +
                "  (try-with [br (io/buffered-reader rd)]         \n" +
                "    (pr-str [ (io/read-line br)                  \n" +
                "              (io/read-line br)                  \n" +
                "              (io/read-line br) ])))             ";

        assertEquals("[\"1\" \"2\" nil]",venice.eval(script));
    }

    @Test
    public void test_io_printl_line() {
        final Venice venice = new Venice();

        final String script =
                "(try-with [sw (io/string-writer)]     \n" +
                "  (io/print-line sw 100)              \n" +
                "  (io/print-line sw)                  \n" +
                "  (io/print-line sw 200)              \n" +
                "  (flush sw)                          \n" +
                "  @sw)                                ";

        assertEquals("100\n\n200\n", to_lf(venice.eval(script)));
    }

}
