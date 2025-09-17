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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class IpcFunctionsTest {

    @Test
    public void test_thread_based_eval() {
        final Venice venice = new Venice();


        final String script =
                "(do                                                               \n" +
                "  (defn handler [m]                                               \n" +
                "    (let [cmd    (. m :getText)                                   \n" +
                "          result (str (eval (read-string cmd)))]                  \n" +
                "      (ipc/plain-text-message (. m :getTopic)                     \n" +
                "                              result)))                           \n" +
                "                                                                  \n" +
                "  (try-with [server (ipc/server 33333 handler)                    \n" +
                "             client (ipc/client \"localhost\" 33333)]             \n" +
                "    (-<> (ipc/plain-text-message \"exec\" \"(+ 1 2)\")            \n" +
                "         (ipc/send client <>)                                     \n" +
                "         (. <> :getText))))";

        assertEquals("3", venice.eval(script));
    }


    @Test
    public void test_map2json_a() {
        final Venice venice = new Venice();


        final String script =
                "(do                                             \n" +
                "  (->> (ipc/text-message \"test\"               \n" +
                "                         \"text/plain\"         \n" +
                "                         :UTF-8                 \n" +
                "                         \"hello\")             \n" +
                "       (ipc/message->json false)))              ";

        assertNotNull(venice.eval(script));
    }


    @Test
    public void test_map2json_b() {
        final Venice venice = new Venice();


        final String script =
                "(do                                             \n" +
                "  (->> (ipc/text-message \"test\"               \n" +
                "                         \"text/plain\"         \n" +
                "                         :UTF-8                 \n" +
                "                         \"hello\")             \n" +
                "       (ipc/message->json true)))               ";

        assertNotNull(venice.eval(script));
    }

}
