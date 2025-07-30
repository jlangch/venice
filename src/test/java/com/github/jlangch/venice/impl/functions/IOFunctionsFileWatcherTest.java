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

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.util.junit.EnableOnMac;


public class IOFunctionsFileWatcherTest {

    // Note: The Github CI test containers do not like file watchers!
    //       Github simply aborts the CI action!
    //
    //       ==> Therefore the io/watch-dir unit tests are only run the
    //           local MacOS.
    //           Github CI actions will run on Linux and Windows

    @Test
    @EnableOnMac
    public void test_io_watch_dir() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                     \n" +
                "  (def lock 0)                                                          \n" +
                "                                                                        \n" +
                "  (def file-event-count        (atom 0))                                \n" +
                "  (def register-event-count    (atom 0))                                \n" +
                "  (def error-event-count       (atom 0))                                \n" +
                "  (def termination-event-count (atom 0))                                \n" +
                "                                                                        \n" +
                "  (defn event [path mode]                                               \n" +
                "    (swap! file-event-count inc)                                        \n" +
                "    (log \"Event:      \" path mode))                                   \n" +
                "                                                                        \n" +
                "  (defn register [path]                                                 \n" +
                "    (swap! register-event-count inc)                                    \n" +
                "    (log \"Registered: \" path))                                        \n" +
                "                                                                        \n" +
                "  (defn error [path e]                                                  \n" +
                "    (swap! error-event-count inc)                                       \n" +
                "    (log \"Failure:    \" (:message e)))                                \n" +
                "                                                                        \n" +
                "  (defn termination [path]                                              \n" +
                "    (swap! termination-event-count inc)                                 \n" +
                "    (log \"Terminated: \" path))                                        \n" +
                "                                                                        \n" +
                "  (defn log [& s]                                                       \n" +
                "    (locking lock (apply println s)))                                   \n" +
                "                                                                        \n" +
                "  (def dir (io/temp-dir \"watchdir-\"))                                 \n" +
                "  (io/delete-file-on-exit dir)                                          \n" +
                "                                                                        \n" +
                "  (try-with [w (io/watch-dir                                            \n" +
                "                   dir                                                  \n" +
                "                   :include-all-subdirs true                            \n" +
                "                   :event-fn            #(event %1 %2)                  \n" +
                "                   :error-fn            #(error %1 %2)                  \n" +
                "                   :termination-fn      #(termination %1)               \n" +
                "                   :register-fn         #(register %1)                  \n" +
                "                   :fswatch-binary      \"/opt/homebrew/bin/fswatch\")] \n" +
                "    (log \"Watching:   \" dir)                                          \n" +
                "                                                                        \n" +
                "    (let [f (io/file dir \"test1.txt\")]                                \n" +
                "      (io/touch-file f)                   ;; created                    \n" +
                "      (io/delete-file-on-exit f)                                        \n" +
                "      (log \"Test File:  \" f)                                          \n" +
                "      (sleep 1000)                                                      \n" +
                "      (io/spit f \"AAA\" :append true)    ;; modifed                    \n" +
                "      (sleep 1000)                                                      \n" +
                "      (io/delete-file f))                 ;; deleted                    \n" +
                "                                                                        \n" +
                "    (sleep 1 :seconds)                                                  \n" +
                "                                                                        \n" +
                "    (let [f (io/file dir \"test2.txt\")]                                \n" +
                "      (io/spit f \"123\")                 ;; modifed                    \n" +
                "      (io/delete-file-on-exit f)                                        \n" +
                "      (log \"Test File:  \" f)                                          \n" +
                "      (sleep 1000)                                                      \n" +
                "      (io/spit f \"AAA\" :append true)    ;; modifed                    \n" +
                "      (sleep 1000)                                                      \n" +
                "      (io/delete-file f))                 ;; deleted                    \n" +
                "                                                                        \n" +
                "    ;; wait for all events to be processed before closing the watcher   \n" +
                "    (sleep 3 :seconds))                                                 \n" +
                "                                                                        \n" +
                "    ;; wait to receive the termination event                            \n" +
                "    (sleep 1 :seconds)                                                  \n" +
                "                                                                        \n" +
                "    (log \"\")                                                          \n" +
                "    (log \"File Events:        \" @file-event-count)                    \n" +
                "    (log \"Register Events:    \" @register-event-count)                \n" +
                "    (log \"Error Events:       \" @error-event-count)                   \n" +
                "    (log \"Termination Events: \" @termination-event-count)             \n" +
                "                                                                        \n" +
                "    [ @file-event-count                                                 \n" +
                "      @register-event-count                                             \n" +
                "      @error-event-count                                                \n" +
                "      @termination-event-count ])                                       ";

        @SuppressWarnings("unchecked")
        final List<Long> events = (List<Long>)venice.eval(script);

        assertEquals(6L, events.get(0));  // file events
        assertEquals(0L, events.get(2));  // error events
        assertEquals(1L, events.get(3));  // termination events
    }

}
