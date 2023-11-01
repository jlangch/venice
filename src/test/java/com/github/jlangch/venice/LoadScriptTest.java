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
package com.github.jlangch.venice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import org.junit.jupiter.api.Test;


public class LoadScriptTest {

    @Test
    public void test_load_classpath_file() {
        final String script =
                "(do                                                                        \n" +
                "  (load-classpath-file \"com/github/jlangch/venice/test-support.venice\")  \n" +
                "  (test-support/test-fn \"hello\"))                                        ";

        assertEquals("test: hello", new Venice().eval(script));
    }

    @Test
    public void test_load_classpath_file_with_ns() {
        final String script =
                "(do                                                                                               \n" +
                "  (load-classpath-file \"com/github/jlangch/venice/test-support.venice\" ['test-support :as 't])  \n" +
                "  (t/test-fn \"hello\"))                                                                          ";

        assertEquals("test: hello", new Venice().eval(script));
    }

    @Test
    public void test_load_file() {
        final String script_1 =
                "(ns xxx)                        \n" +
                "                                \n" +
                "(defn sum [x y] (+ x y 11))       ";

        try {
            final File file = Files.createTempFile("test", ".venice").normalize().toFile();
            Files.write(file.toPath(), script_1.getBytes("UTF-8"), StandardOpenOption.TRUNCATE_EXISTING);

            final String script_2 =
                    String.format(
                        "(do                   \n" +
                        "  (load-file \"%s\")  \n" +
                        "  (xxx/sum 1 2))        ",
                    file.getPath());

            try {
                assertEquals(14L, new Venice().eval(script_2));
            }
            finally {
                file.delete();
            }
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void test_load_file_with_ns() {
        final String script_1 =
                "(ns xxx)                        \n" +
                "                                \n" +
                "(defn sum [x y] (+ x y 11))     ";

        try {
            final File file = Files.createTempFile("test", ".venice").normalize().toFile();
            Files.write(file.toPath(), script_1.getBytes("UTF-8"), StandardOpenOption.TRUNCATE_EXISTING);

            final String script_2 =
                    String.format(
                        "(do                                 \n" +
                        "  (load-file \"%s\" ['xxx :as 'x])  \n" +
                        "  (x/sum 1 2))                      ",
                    file.getPath());

            try {
                assertEquals(14L, new Venice().eval(script_2));
            }
            finally {
                file.delete();
            }
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void test_load_file_param() {
        final String script_1 =
                "(ns xxx)                        \n" +
                "                                \n" +
                "(defn sum [x y] (+ x y 11))       ";

        try {
            File file = Files.createTempFile("test", ".venice").normalize().toFile();
            Files.write(file.toPath(), script_1.getBytes("UTF-8"), StandardOpenOption.TRUNCATE_EXISTING);

            final String script_2 =
                    "(do                   \n" +
                    "  (load-file f)       \n" +
                    "  (xxx/sum 1 2)))       ";

            try {
                assertEquals(14L, new Venice().eval(script_2, Parameters.of("f", file.getAbsolutePath())));
            }
            finally {
                file.delete();
            }


            file = Files.createTempFile("test", ".venice").normalize().toFile();
            Files.write(file.toPath(), script_1.getBytes("UTF-8"), StandardOpenOption.TRUNCATE_EXISTING);

            final String script_3 =
                    "(do                    \n" +
                    "  (load-file f true)   \n" +
                    "  (xxx/sum 1 2)))       ";

            try {
                assertEquals(14L, new Venice().eval(script_3, Parameters.of("f", file.getAbsolutePath())));
            }
            finally {
                file.delete();
            }
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void test_load_file_force() {
        final String script_1 =
                "(ns xxx)                        \n" +
                "                                \n" +
                "(defn sum [x y] (+ x y 11))       ";

        try {
            final File file = Files.createTempFile("test", ".venice").normalize().toFile();
            Files.write(file.toPath(), script_1.getBytes("UTF-8"), StandardOpenOption.TRUNCATE_EXISTING);

            final String script_2 =
                    String.format(
                        "(do                         \n" +
                        "  (load-file \"%s\" true)   \n" +
                        "  (xxx/sum 1 2))              ",
                    file.getPath());

            try {
                assertEquals(14L, new Venice().eval(script_2));
            }
            finally {
                file.delete();
            }
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void test_load_file_force_with_ns() {
        final String script_1 =
                "(ns xxx)                        \n" +
                "                                \n" +
                "(defn sum [x y] (+ x y 11))       ";

        try {
            final File file = Files.createTempFile("test", ".venice").normalize().toFile();
            Files.write(file.toPath(), script_1.getBytes("UTF-8"), StandardOpenOption.TRUNCATE_EXISTING);

            final String script_2 =
                    String.format(
                        "(do                                      \n" +
                        "  (load-file \"%s\" true ['xxx :as 'x])  \n" +
                        "  (x/sum 1 2))                             ",
                    file.getPath());

            try {
                assertEquals(14L, new Venice().eval(script_2));
            }
            finally {
                file.delete();
            }
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
