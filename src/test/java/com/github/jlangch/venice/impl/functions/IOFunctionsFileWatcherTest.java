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
    //       ==> Therefore the io/watch-dir unit tests are only run
    //           on the local MacOS.

    @Test
    @EnableOnMac
    public void test_io_watch_dir() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                     \n" +
                "  (def lock 0)                                                          \n" +
                "                                                                        \n" +
                "  (defn log [format & args]                                             \n" +
                "    (locking lock (println (apply str/format format args))))            \n" +
                "                                                                        \n" +
                "  (def file-event-count        (atom 0))                                \n" +
                "  (def error-event-count       (atom 0))                                \n" +
                "  (def termination-event-count (atom 0))                                \n" +
                "                                                                        \n" +
                "  (defn file-event [path dir? file? action]                             \n" +
                "    (swap! file-event-count inc)                                        \n" +
                "    (log \"File:       %s %-9s, dir: %b, file: %b\"                     \n" +
                "         path action dir? file?))                                       \n" +
                "                                                                        \n" +
                "  (defn error-event [path e]                                            \n" +
                "    (swap! error-event-count inc)                                       \n" +
                "    (log \"Failure:    %s %s\" path (:message e)))                      \n" +
                "                                                                        \n" +
                "  (defn termination-event [path]                                        \n" +
                "    (swap! termination-event-count inc)                                 \n" +
                "    (log \"Terminated: %s\" path))                                      \n" +
                "                                                                        \n" +
                "  (def dir (io/temp-dir \"watchdir-\"))                                 \n" +
                "  (io/delete-file-on-exit dir)                                          \n" +
                "                                                                        \n" +
                "  (sleep 1 :seconds)                                                    \n" +
                "                                                                        \n" +
                "  (try-with [w (io/watch-dir                                            \n" +
                "                   dir                                                  \n" +
                "                   :include-all-subdirs true                            \n" +
                "                   :file-fn             file-event                      \n" +
                "                   :error-fn            error-event                     \n" +
                "                   :termination-fn      termination-event               \n" +
                "                   :fswatch-monitor     nil                             \n" +
                "                   :fswatch-program     \"/opt/homebrew/bin/fswatch\")] \n" +
                "                                                                        \n" +
                "    (sleep 1 :seconds)                                                  \n" +
                "                                                                        \n" +
                "    (log \"Watching:   %s\" dir)                                        \n" +
                "                                                                        \n" +
                "    (let [f (io/file dir \"test1.txt\")]                                \n" +
                "      (io/touch-file f)                   ;; created                    \n" +
                "      (io/delete-file-on-exit f)                                        \n" +
                "      (log \"Test File:  %s\" f)                                        \n" +
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
                "      (log \"Test File:  %s\" f)                                        \n" +
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
                "    (log \"File Events:        %s\" @file-event-count)                  \n" +
                "    (log \"Error Events:       %s\" @error-event-count)                 \n" +
                "    (log \"Termination Events: %s\" @termination-event-count)           \n" +
                "                                                                        \n" +
                "    [ @file-event-count                                                 \n" +
                "      @error-event-count                                                \n" +
                "      @termination-event-count ])                                       ";

        @SuppressWarnings("unchecked")
        final List<Long> events = (List<Long>)venice.eval(script);

        assertEquals(6L, events.get(0));  // file events
        assertEquals(0L, events.get(1));  // error events
        assertEquals(1L, events.get(2));  // termination events
    }

}
