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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;


public class IOFunctionsSpitSlurpTest {

    @Test
    public void test_io_spit() {
        final Venice venice = new Venice();

        // with default encoding
        try {
            final File file = Files.createTempFile("spit", ".txt").normalize().toFile();
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
            final File file = Files.createTempFile("spit", ".txt").normalize().toFile();
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
            final File file = Files.createTempFile("slurp", ".txt").normalize().toFile();
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
            final File file = Files.createTempFile("slurp", ".txt").normalize().toFile();
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

}
