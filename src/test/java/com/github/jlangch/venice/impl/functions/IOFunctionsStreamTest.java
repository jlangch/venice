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

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class IOFunctionsStreamTest {

    @Test
    public void test_io_buffered_reader() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                \n" +
                "   (import :java.io.FileInputStream)                               \n" +
                "   (let [file (io/temp-file \"test-\", \".txt\")]                  \n" +
                "      (io/delete-file-on-exit file)                                \n" +
                "      (io/spit file \"100\n200\" :append false)                    \n" +
                "      (try-with [rd (io/buffered-reader file :encoding :utf-8)]    \n" +
                "         (pr-str [(read-line rd) (read-line rd)]))))               ";

        assertEquals("[\"100\" \"200\"]",venice.eval(script));
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
                "      (try-with [rd (io/buffered-reader file :encoding :utf-8)]    \n" +
                "         (pr-str [(read-line rd) (read-line rd)]))))               ";

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

}
