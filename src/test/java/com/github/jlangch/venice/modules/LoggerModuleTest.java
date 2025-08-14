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
    public void test() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                   \n" +
                "  (load-module :logger)                               \n" +
                "                                                      \n" +
                "  (def dir (io/temp-dir \"logger-\"))                 \n" +
                "  (io/delete-file-on-exit dir)                        \n" +
                "                                                      \n" +
                "  (let [t (io/file dir \"test.log\")]                 \n" +
                "    (io/touch-file t)                                 \n" +
                "    (io/delete-file-on-exit t)                        \n" +
                "                                                      \n" +
                "    (logger/log t :info :base \"test message 1\")     \n" +
                "    (logger/log t :info :base \"test message 2\")     \n" +
                "                                                      \n" +
                "    (let [lines (io/slurp-lines t)]                   \n" +
                "      lines)))                                       ";

        final List<String> lines = (List<String>)venice.eval(script);

        assertEquals(2, lines.size());
        assertTrue(lines.get(0).matches("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}[.][0-9]{3}[|]INFO[|]base[|]test message 1"));
        assertTrue(lines.get(1).matches("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}[.][0-9]{3}[|]INFO[|]base[|]test message 2"));
    }

}
