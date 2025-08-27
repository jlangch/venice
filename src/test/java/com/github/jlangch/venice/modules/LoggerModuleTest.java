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
package com.github.jlangch.venice.modules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;


public class LoggerModuleTest {

    @Test
    @SuppressWarnings("unchecked")
    public void logTest() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                     \n" +
                "  (load-module :logger)                                 \n" +
                "                                                        \n" +
                "  (def dir (io/temp-dir \"logger-\"))                   \n" +
                "                                                        \n" +
                "  (try                                                  \n" +
                "    (let [file    (io/file dir \"test.log\")            \n" +
                "          _       (logger/file-logger :test file)       \n" +
                "          log     (logger/logger :test)]                \n" +
                "      (log :info :base \"test message 1\")              \n" +
                "      (log :info :base \"test message 2\")              \n" +
                "      (io/slurp-lines file))                            \n" +
                "    (finally                                            \n" +
                "      (io/delete-file-tree dir))))                      ";

        final List<String> lines = (List<String>)venice.eval(script);

        assertEquals(2, lines.size());
        assertTrue(lines.get(0).matches("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}[.][0-9]{3}[|]INFO[|]base[|]test message 1"));
        assertTrue(lines.get(1).matches("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}[.][0-9]{3}[|]INFO[|]base[|]test message 2"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void logTruncateTest() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                         \n" +
                "  (load-module :logger)                                     \n" +
                "                                                            \n" +
                "  (def dir (io/temp-dir \"logger-\"))                       \n" +
                "                                                            \n" +
                "  (try                                                      \n" +
                "    (let [file    (io/file dir \"test.log\")                \n" +
                "          _       (logger/file-logger :test file            \n" +
                "                                            :max-size 120)  \n" +
                "          log     (logger/logger :test)]                    \n" +
                "      (log :info :base \"test message 1\")                  \n" +
                "      (log :info :base \"test message 2\")                  \n" +
                "      (log :info :base \"test message 3\")                  \n" +
                "      (log :info :base \"test message 4\")                  \n" +
                "      (log :info :base \"test message 5\")                  \n" +
                "      (log :info :base \"test message 6\")                  \n" +
                "                                                            \n" +
                "      (io/slurp-lines file))                                \n" +
                "    (finally                                                \n" +
                "      (io/delete-file-tree dir))))                          ";

        final List<String> lines = (List<String>)venice.eval(script);

        assertEquals(2, lines.size());
        assertEquals(48, lines.get(0).length());
        assertEquals(48, lines.get(1).length());
        assertTrue(lines.get(0).matches("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}[.][0-9]{3}[|]INFO[|]base[|]test message 5"));
        assertTrue(lines.get(1).matches("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}[.][0-9]{3}[|]INFO[|]base[|]test message 6"));
    }

    @Test
    public void logRotateDayTest_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                         \n" +
                "  (load-module :logger)                                                     \n" +
                "                                                                            \n" +
                "  (def dir (io/temp-dir \"logger-\"))                                       \n" +
                "                                                                            \n" +
                "  (try                                                                      \n" +
                "    (def archive-dir (io/file dir \"archive\"))                             \n" +
                "    (io/mkdir archive-dir)                                                  \n" +
                "                                                                            \n" +
                "    ;; disable for unit tests, rotation is started explicitly               \n" +
                "    (logger/enable-auto-start-rotation-scheduler false)                     \n" +
                "                                                                            \n" +
                "    (let [file    (io/file dir \"test.log\")                                \n" +
                "          _       (logger/file-logger :test file                            \n" +
                "                                            :rotate-mode :daily             \n" +
                "                                            :rotate-dir archive-dir)        \n" +
                "          log     (logger/logger :test)]                                    \n" +
                "      (log :info :base \"test message 1\")                                  \n" +
                "      (log :info :base \"test message 2\")                                  \n" +
                "                                                                            \n" +
                "      (and (logger/rotate :test)                                            \n" +
                "           (io/exists-file? file)                                           \n" +
                "           (== 2 (count (io/list-files dir)))))                             \n" +
                "    (finally                                                                \n" +
                "      (io/delete-file-tree dir))))                                          ";
        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void logRotateDayTest_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                         \n" +
                "  (load-module :logger)                                                     \n" +
                "                                                                            \n" +
                "  (def dir (io/temp-dir \"logger-\"))                                       \n" +
                "                                                                            \n" +
                "  (try                                                                      \n" +
                "    (def archive-dir (io/file dir \"archive\"))                             \n" +
                "    (io/mkdir archive-dir)                                                  \n" +
                "                                                                            \n" +
                "    ;; disable for unit tests, rotation is started explicitly               \n" +
                "    (logger/enable-auto-start-rotation-scheduler false)                     \n" +
                "                                                                            \n" +
                "    (let [file    (io/file dir \"test.log\")                                \n" +
                "          _       (logger/file-logger :test file                            \n" +
                "                                            :rotate-mode :daily             \n" +
                "                                            :rotate-dir archive-dir)        \n" +
                "          log     (logger/logger :test)]                                    \n" +
                "      (log :info :base \"test message 1\")                                  \n" +
                "      (log :info :base \"test message 2\")                                  \n" +
                "                                                                            \n" +
                "      (logger/rotate :test)                                                 \n" +
                "      (io/slurp-lines file))                                                \n" +
                "    (finally                                                                \n" +
                "      (io/delete-file-tree dir))))                                          ";
        final List<String> lines = (List<String>)venice.eval(script);

        assertEquals(1, lines.size());
        assertTrue(lines.get(0).matches("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}[.][0-9]{3}[|]INFO[|]system[|]Rotated log file"));
    }

    @Test
    public void logRequiresRotationTest_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                         \n" +
                "  (load-module :logger)                                                     \n" +
                "                                                                            \n" +
                "  (logger/requires-rotation?))                                              ";
        assertFalse((Boolean)venice.eval(script));
    }

    @Test
    public void logRequiresRotationTest_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                         \n" +
                "  (load-module :logger)                                                     \n" +
                "                                                                            \n" +
                "  (def dir (io/temp-dir \"logger-\"))                                       \n" +
                "                                                                            \n" +
                "  (try                                                                      \n" +
                "    (def archive-dir (io/file dir \"archive\"))                             \n" +
                "    (io/mkdir archive-dir)                                                  \n" +
                "                                                                            \n" +
                "    ;; disable for unit tests, rotation is started explicitly               \n" +
                "    (logger/enable-auto-start-rotation-scheduler false)                     \n" +
                "                                                                            \n" +
                "    (logger/console-logger nil)                                             \n" +
                "                                                                            \n" +
                "    (logger/requires-rotation?)                                             \n" +
                "    (finally                                                                \n" +
                "      (io/delete-file-tree dir))))                                          ";
        assertFalse((Boolean)venice.eval(script));
    }

    @Test
    public void logRequiresRotationTest_3() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                         \n" +
                "  (load-module :logger)                                                     \n" +
                "                                                                            \n" +
                "  (def dir (io/temp-dir \"logger-\"))                                       \n" +
                "                                                                            \n" +
                "  (try                                                                      \n" +
                "    (def archive-dir (io/file dir \"archive\"))                             \n" +
                "    (io/mkdir archive-dir)                                                  \n" +
                "                                                                            \n" +
                "    ;; disable for unit tests, rotation is started explicitly               \n" +
                "    (logger/enable-auto-start-rotation-scheduler false)                     \n" +
                "                                                                            \n" +
                "    (logger/console-logger nil)                                             \n" +
                "    (logger/file-logger :test (io/file dir \"test.log\"))                   \n" +
                "                                                                            \n" +
                "    (logger/requires-rotation?)                                             \n" +
                "    (finally                                                                \n" +
                "      (io/delete-file-tree dir))))                                          ";
        assertFalse((Boolean)venice.eval(script));
    }

    @Test
    public void logRequiresRotationTest_4() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                         \n" +
                "  (load-module :logger)                                                     \n" +
                "                                                                            \n" +
                "  (def dir (io/temp-dir \"logger-\"))                                       \n" +
                "                                                                            \n" +
                "  (try                                                                      \n" +
                "    (def archive-dir (io/file dir \"archive\"))                             \n" +
                "    (io/mkdir archive-dir)                                                  \n" +
                "                                                                            \n" +
                "    ;; disable for unit tests, rotation is started explicitly               \n" +
                "    (logger/enable-auto-start-rotation-scheduler false)                     \n" +
                "                                                                            \n" +
                "    (logger/console-logger nil)                                             \n" +
                "    (logger/file-logger :test (io/file dir \"test.log\")                    \n" +
                "                              :rotate-mode :daily                           \n" +
                "                              :rotate-dir archive-dir)                      \n" +
                "                                                                            \n" +
                "    (logger/requires-rotation?)                                             \n" +
                "    (finally                                                                \n" +
                "      (io/delete-file-tree dir))))                                          ";
        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void logRequiresRotationTest_5() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                         \n" +
                "  (load-module :logger)                                                     \n" +
                "                                                                            \n" +
                "  (def dir (io/temp-dir \"logger-\"))                                       \n" +
                "                                                                            \n" +
                "  (try                                                                      \n" +
                "    (def archive-dir (io/file dir \"archive\"))                             \n" +
                "    (io/mkdir archive-dir)                                                  \n" +
                "                                                                            \n" +
                "    ;; disable for unit tests, rotation is started explicitly               \n" +
                "    (logger/enable-auto-start-rotation-scheduler false)                     \n" +
                "                                                                            \n" +
                "    (logger/console-logger nil)                                             \n" +
                "    (logger/file-logger :test (io/file dir \"test.log\")                    \n" +
                "                              :rotate-mode :monthly                         \n" +
                "                              :rotate-dir archive-dir)                      \n" +
                "                                                                            \n" +
                "    (logger/requires-rotation?)                                             \n" +
                "    (finally                                                                \n" +
                "      (io/delete-file-tree dir))))                                          ";
        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void logRequiresRotationTest_6() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                         \n" +
                "  (load-module :logger)                                                     \n" +
                "                                                                            \n" +
                "  (def dir (io/temp-dir \"logger-\"))                                       \n" +
                "                                                                            \n" +
                "  (try                                                                      \n" +
                "    (def archive-dir (io/file dir \"archive\"))                             \n" +
                "    (io/mkdir archive-dir)                                                  \n" +
                "                                                                            \n" +
                "    ;; disable for unit tests, rotation is started explicitly               \n" +
                "    (logger/enable-auto-start-rotation-scheduler false)                     \n" +
                "                                                                            \n" +
                "    (logger/console-logger nil)                                             \n" +
                "    (logger/file-logger :test (io/file dir \"test.log\"))                   \n" +
                "    (logger/file-logger :test (io/file dir \"test.log\")                    \n" +
                "                              :rotate-mode :daily                           \n" +
                "                              :rotate-dir archive-dir)                      \n" +
                "    (logger/file-logger :test (io/file dir \"test.log\")                    \n" +
                "                              :rotate-mode :monthly                         \n" +
                "                              :rotate-dir archive-dir)                      \n" +
                "                                                                            \n" +
                "    (logger/requires-rotation?)                                             \n" +
                "    (finally                                                                \n" +
                "      (io/delete-file-tree dir))))                                          ";
        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void logConsoleLevelSetTest_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                   \n" +
                "  (load-module :logger)               \n" +
                "                                      \n" +
                "  (logger/console-logger nil)         \n" +
                "                                      \n" +
                "  (logger/level :console))            ";
         assertEquals("debug", venice.eval(script));
    }

    @Test
    public void logConsoleLevelSetTest_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                   \n" +
                "  (load-module :logger)               \n" +
                "                                      \n" +
                "  (logger/console-logger nil)         \n" +
                "                                      \n" +
                "  (logger/level :console :warn)       \n" +
                "  (logger/level :console))            ";
        assertEquals("warn", venice.eval(script));
    }

    @Test
    public void logConsoleLevelAllSetTest() {
        final Venice venice = new Venice();

        final String script =
                "(do                                   \n" +
                "  (load-module :logger)               \n" +
                "                                      \n" +
                "  (logger/console-logger nil)         \n" +
                "                                      \n" +
                "  (logger/level-all :warn)            \n" +
                "  (logger/level :console))            ";
        assertEquals("warn", venice.eval(script));
    }

    @Test
    public void logFileLevelSetTest_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                         \n" +
                "  (load-module :logger)                                                     \n" +
                "                                                                            \n" +
                "  (def dir (io/temp-dir \"logger-\"))                                       \n" +
                "                                                                            \n" +
                "  (try                                                                      \n" +
                "    (def archive-dir (io/file dir \"archive\"))                             \n" +
                "    (io/mkdir archive-dir)                                                  \n" +
                "                                                                            \n" +
                "    ;; disable for unit tests, rotation is started explicitly               \n" +
                "    (logger/enable-auto-start-rotation-scheduler false)                     \n" +
                "                                                                            \n" +
                "    (logger/console-logger nil)                                             \n" +
                "    (logger/file-logger :test (io/file dir \"test.log\"))                   \n" +
                "                                                                            \n" +
                "    (logger/level :test)                                                    \n" +
                "    (finally                                                                \n" +
                "      (io/delete-file-tree dir))))                                          ";
        assertEquals("debug", venice.eval(script));
    }

    @Test
    public void logFileLevelSetTest_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                         \n" +
                "  (load-module :logger)                                                     \n" +
                "                                                                            \n" +
                "  (def dir (io/temp-dir \"logger-\"))                                       \n" +
                "                                                                            \n" +
                "  (try                                                                      \n" +
                "    (def archive-dir (io/file dir \"archive\"))                             \n" +
                "    (io/mkdir archive-dir)                                                  \n" +
                "                                                                            \n" +
                "    ;; disable for unit tests, rotation is started explicitly               \n" +
                "    (logger/enable-auto-start-rotation-scheduler false)                     \n" +
                "                                                                            \n" +
                "    (logger/console-logger nil)                                             \n" +
                "    (logger/file-logger :test (io/file dir \"test.log\"))                   \n" +
                "                                                                            \n" +
                "    (logger/level :test :warn)                                              \n" +
                "    (logger/level :test)                                                    \n" +
                "    (finally                                                                \n" +
                "      (io/delete-file-tree dir))))                                          ";
        assertEquals("warn", venice.eval(script));
    }

    @Test
    public void logFileLevelAllSetTest() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                         \n" +
                "  (load-module :logger)                                                     \n" +
                "                                                                            \n" +
                "  (def dir (io/temp-dir \"logger-\"))                                       \n" +
                "                                                                            \n" +
                "  (try                                                                      \n" +
                "    (def archive-dir (io/file dir \"archive\"))                             \n" +
                "    (io/mkdir archive-dir)                                                  \n" +
                "                                                                            \n" +
                "    ;; disable for unit tests, rotation is started explicitly               \n" +
                "    (logger/enable-auto-start-rotation-scheduler false)                     \n" +
                "                                                                            \n" +
                "    (logger/console-logger nil)                                             \n" +
                "    (logger/file-logger :test (io/file dir \"test.log\"))                   \n" +
                "                                                                            \n" +
                "    (logger/level-all :warn)                                                \n" +
                "    (logger/level :test)                                                    \n" +
                "    (finally                                                                \n" +
                "      (io/delete-file-tree dir))))                                          ";
        assertEquals("warn", venice.eval(script));
    }

    @Test
    public void convertToBytesTest() {
        final Venice venice = new Venice();

        assertEquals(-1L, venice.eval("(do (load-module :logger) (logger/convert-to-bytes -1))"));

        assertEquals(100L, venice.eval("(do (load-module :logger) (logger/convert-to-bytes 100))"));

        assertEquals(10L * 1024, venice.eval("(do (load-module :logger) (logger/convert-to-bytes :10KB))"));

        assertEquals(10L * 1024 * 1024, venice.eval("(do (load-module :logger) (logger/convert-to-bytes :10MB))"));

        assertEquals(10L * 1024 * 1024 * 1024, venice.eval("(do (load-module :logger) (logger/convert-to-bytes :10GB))"));

        assertThrows(VncException.class, () ->  venice.eval("(do (load-module :logger) (logger/convert-to-bytes :x))"));
        assertThrows(VncException.class, () ->  venice.eval("(do (load-module :logger) (logger/convert-to-bytes :10))"));
        assertThrows(VncException.class, () ->  venice.eval("(do (load-module :logger) (logger/convert-to-bytes :10MBx))"));
        assertThrows(VncException.class, () ->  venice.eval("(do (load-module :logger) (logger/convert-to-bytes :10PB))"));
    }

}
