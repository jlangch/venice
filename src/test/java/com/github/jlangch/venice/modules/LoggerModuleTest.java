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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


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
                "    (let [f       (io/file dir \"test.log\")            \n" +
                "          handler (logger/handler f)                    \n" +
                "          log     (partial logger/log handler)]         \n" +
                "      (log :info :base \"test message 1\")              \n" +
                "      (log :info :base \"test message 2\")              \n" +
                "                                                        \n" +
                "      (io/slurp-lines f))                               \n" +
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
                "    (let [f       (io/file dir \"test.log\")                \n" +
                "          handler (logger/handler f 120)                    \n" +
                "          log     (partial logger/log handler)]             \n" +
                "      (log :info :base \"test message 1\")                  \n" +
                "      (log :info :base \"test message 2\")                  \n" +
                "      (log :info :base \"test message 3\")                  \n" +
                "      (log :info :base \"test message 4\")                  \n" +
                "      (log :info :base \"test message 5\")                  \n" +
                "      (log :info :base \"test message 6\")                  \n" +
                "                                                            \n" +
                "      (io/slurp-lines f))                                   \n" +
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
    public void logRotateTest() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                     \n" +
                "  (load-module :logger)                                 \n" +
                "                                                        \n" +
                "  (def dir (io/temp-dir \"logger-\"))                   \n" +
                "                                                        \n" +
                "  (try                                                  \n" +
                "    (def archive-dir (io/file dir \"archive\"))         \n" +
                "    (io/mkdir archive-dir)                              \n" +
                "                                                        \n" +
                "    (let [f       (io/file dir \"test.log\")            \n" +
                "          handler (logger/handler f)                    \n" +
                "          log     (partial logger/log handler)]         \n" +
                "      (log :info :base \"test message 1\")              \n" +
                "      (log :info :base \"test message 2\")              \n" +
                "                                                        \n" +
                "      (logger/rotate-log-file-by-month f archive-dir)   \n" +
                "      (and (not (io/exists-file? f))                    \n" +
                "           (== 1 (count (io/list-files dir)))))         \n" +
                "    (finally                                            \n" +
                "      (io/delete-file-tree dir))))                      ";
        assertTrue((Boolean)venice.eval(script));
    }

}
