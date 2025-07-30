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

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.util.junit.EnableOnMac;


public class IOFunctionsFileWatcherTest {

    // Note: The Github CI test containers do not like file watchers!
    //       Github simply aborts the CI action!

    @Test
    @EnableOnMac
    public void test_io_watch_dir() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                     \n" +
                "  (def event-count (atom 0))                                            \n" +
                "                                                                        \n" +
                "  (defn event [path mode]                                               \n" +
                "    (swap! event-count inc)                                             \n" +
                "    (log \"Event:      \" path mode))                                   \n" +
                "                                                                        \n" +
                "  (defn log [& s]                                                       \n" +
                "    (locking event-count (apply println s)))                            \n" +
                "                                                                        \n" +
                "  (def dir (io/temp-dir \"watchdir-\"))                                 \n" +
                "  (io/delete-file-on-exit dir)                                          \n" +
                "                                                                        \n" +
                "  (try-with [w (io/watch-dir dir                                        \n" +
                "                             #(event %1 %2)                             \n" +
                "                             #(log \"Failure:    \" (:message %2))      \n" +
                "                             #(log \"Terminated: \" %1)                 \n" +
                "                             #(log \"Registered: \" %1))]               \n" +
                "    (log \"Watching:  \" dir)                                           \n" +
                "                                                                        \n" +
                "    (sleep 1 :seconds)                                                  \n" +
                "                                                                        \n" +
                "    (let [f (io/file dir \"test1.txt\")]                                \n" +
                "      (io/spit f \"123456789\")                                         \n" +
                "      (io/delete-file-on-exit f)                                        \n" +
                "      (log \"Added File: \" f))                                         \n" +
                "                                                                        \n" +
                "    (sleep 3 :seconds))                                                 \n" +
                "                                                                        \n" +
                "    (sleep 1 :seconds)                                                  \n" +
                "    (log \"\n#Events:    \" @event-count)                               \n" +
                "                                                                        \n" +
                "    @event-count)                                                       ";

        assertEquals(1L, venice.eval(script));
    }

}
