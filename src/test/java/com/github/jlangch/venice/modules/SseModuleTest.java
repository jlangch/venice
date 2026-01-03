/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2026 Venice
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;


public class SseModuleTest {

    @Test
    public void test_validation_1a() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                \n" +
                "  (load-module :server-side-events ['server-side-events :as 'sse]) \n" +
                "  (sse/render { :id \"100\"                                        \n" +
                "                :event \"scores\"                                  \n" +
                "                :data [\"100\"] } ))                               ";

        venice.eval(script);
    }

    @Test
    public void test_validation_1b() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                \n" +
                "  (load-module :server-side-events ['server-side-events :as 'sse]) \n" +
                "  (sse/render { :id nil                                            \n" +
                "                :event nil                                         \n" +
                "                :data nil } ))                                     ";

        venice.eval(script);
    }

    @Test
    public void test_validation_2a() {
        final Venice venice = new Venice();

        // illegal \n in :id
        final String script =
                "(do                                                                \n" +
                "  (load-module :server-side-events ['server-side-events :as 'sse]) \n" +
                "  (sse/render { :id \"10\n0\"                                      \n" +
                "                :event \"scores\"                                  \n" +
                "                :data [\"100\"] } ))                               ";

        assertThrows(VncException.class, () -> venice.eval(script));
    }

    @Test
    public void test_validation_2b() {
        final Venice venice = new Venice();

        // illegal \r in :id
        final String script =
                "(do                                                                \n" +
                "  (load-module :server-side-events ['server-side-events :as 'sse]) \n" +
                "  (sse/render { :id \"10\r0\"                                      \n" +
                "                :event \"scores\"                                  \n" +
                "                :data [\"100\"] } ))                               ";

        assertThrows(VncException.class, () -> venice.eval(script));
    }

    @Test
    public void test_validation_3() {
        final Venice venice = new Venice();

        // illegal \n in :event
        final String script =
                "(do                                                                \n" +
                "  (load-module :server-side-events ['server-side-events :as 'sse]) \n" +
                "  (sse/render { :id \"100\"                                        \n" +
                "                :event \"sco\nres\"                                \n" +
                "                :data [\"100\"] } ))                               ";

        assertThrows(VncException.class, () -> venice.eval(script));
    }

    @Test
    public void test_validation_4() {
        final Venice venice = new Venice();

        // illegal \n in :data
        final String script =
                "(do                                                                \n" +
                "  (load-module :server-side-events ['server-side-events :as 'sse]) \n" +
                "  (sse/render { :id \"100\"                                        \n" +
                "                :event \"scores\"                                  \n" +
                "                :data [\"10\n0\"] } ))                             ";

        assertThrows(VncException.class, () -> venice.eval(script));
    }

    @Test
    public void test_validation_5() {
        final Venice venice = new Venice();

        // illegal data type :id
        final String script =
                "(do                                                                \n" +
                "  (load-module :server-side-events ['server-side-events :as 'sse]) \n" +
                "  (sse/render { :id 100                                            \n" +
                "                :event \"scores\"                                  \n" +
                "                :data [\"100\"] } ))                               ";

        assertThrows(VncException.class, () -> venice.eval(script));
    }

    @Test
    public void test_validation_6() {
        final Venice venice = new Venice();

        // illegal data type :event
        final String script =
                "(do                                                                \n" +
                "  (load-module :server-side-events ['server-side-events :as 'sse]) \n" +
                "  (sse/render { :id \"100\"                                        \n" +
                "                :event :scores                                     \n" +
                "                :data [\"100\"] } ))                               ";

        assertThrows(VncException.class, () -> venice.eval(script));
    }

    @Test
    public void test_validation_7() {
        final Venice venice = new Venice();

        // illegal data type :data
        final String script =
                "(do                                                                \n" +
                "  (load-module :server-side-events ['server-side-events :as 'sse]) \n" +
                "  (sse/render { :id 100                                            \n" +
                "                :event scores                                      \n" +
                "                :data [100] } ))                                   ";

        assertThrows(VncException.class, () -> venice.eval(script));
    }

    @Test
    public void test_render_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                \n" +
                "  (load-module :server-side-events ['server-side-events :as 'sse]) \n" +
                "  (sse/render { :id \"100\"                                        \n" +
                "                :event \"scores\"                                  \n" +
                "                :data [\"100\"] } ))                               ";

        assertEquals("id: 100\r\nevent: scores\r\ndata: 100\r\n\r\n", venice.eval(script));
    }

    @Test
    public void test_render_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                \n" +
                "  (load-module :server-side-events ['server-side-events :as 'sse]) \n" +
                "  (sse/render { :id \"100\"                                        \n" +
                "                :event \"scores\"                                  \n" +
                "                :data [\"100\" \"200\"] } ))                       ";

        assertEquals("id: 100\r\nevent: scores\r\ndata: 100\r\ndata: 200\r\n\r\n", venice.eval(script));
    }

    @Test
    public void test_render_3() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                \n" +
                "  (load-module :server-side-events ['server-side-events :as 'sse]) \n" +
                "  (sse/render { :id \"100\"                                        \n" +
                "                :data [\"100\"] } ))                               ";

        assertEquals("id: 100\r\ndata: 100\r\n\r\n", venice.eval(script));
    }

    @Test
    public void test_render_4() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                \n" +
                "  (load-module :server-side-events ['server-side-events :as 'sse]) \n" +
                "  (sse/render { :data [\"100\"] } ))                               ";

        assertEquals("data: 100\r\n\r\n", venice.eval(script));
    }

    @Test
    public void test_render_5() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                \n" +
                "  (load-module :server-side-events ['server-side-events :as 'sse]) \n" +
                "  (sse/render { :data [\"100\" \"200\"] } ))                       ";

        assertEquals("data: 100\r\ndata: 200\r\n\r\n", venice.eval(script));
    }

    @Test
    public void test_parse_0a() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                \n" +
                "  (load-module :server-side-events ['server-side-events :as 'sse]) \n" +
                "  (-> \"id: 100\r\nevent: scores\r\ndata: 100\"                    \n" +
                "      (sse/parse)                                                  \n" +
                "      (pr-str)))";

        assertEquals("{:data [\"100\"] :event \"scores\" :id \"100\"}", venice.eval(script));
    }

    @Test
    public void test_parse_0b() {
        final Venice venice = new Venice();

        // two data elements
        final String script =
                "(do                                                                \n" +
                "  (load-module :server-side-events ['server-side-events :as 'sse]) \n" +
                "  (-> \"id: 100\r\nevent: scores\r\ndata: 100\r\ndata: 200\"       \n" +
                "      (sse/parse)                                                  \n" +
                "      (pr-str)))";

        assertEquals("{:data [\"100\" \"200\"] :event \"scores\" :id \"100\"}", venice.eval(script));
    }

    @Test
    public void test_parse_0c() {
        final Venice venice = new Venice();

        // with comment
        final String script =
                "(do                                                                        \n" +
                "  (load-module :server-side-events ['server-side-events :as 'sse])         \n" +
                "  (-> \"id: 100\r\nevent: scores\r\n: comment\r\ndata: 100\r\ndata: 200\"  \n" +
                "      (sse/parse)                                                          \n" +
                "      (pr-str)))";

        assertEquals("{:data [\"100\" \"200\"] :event \"scores\" :id \"100\"}", venice.eval(script));
    }

    @Test
    public void test_parse_0d() {
        final Venice venice = new Venice();

        // with comment
        final String script =
                "(do                                                                        \n" +
                "  (load-module :server-side-events ['server-side-events :as 'sse])         \n" +
                "  (-> \": comment\r\nid: 100\r\nevent: scores\r\ndata: 100\r\ndata: 200\"  \n" +
                "      (sse/parse)                                                          \n" +
                "      (pr-str)))";

        assertEquals("{:data [\"100\" \"200\"] :event \"scores\" :id \"100\"}", venice.eval(script));
    }

    @Test
    public void test_parse_0e() {
        final Venice venice = new Venice();

        // with comment
        final String script =
                "(do                                                                        \n" +
                "  (load-module :server-side-events ['server-side-events :as 'sse])         \n" +
                "  (-> \"id: 100\r\nevent: scores\r\ndata: 100\r\ndata: 200\r\n: comment\"  \n" +
                "      (sse/parse)                                                          \n" +
                "      (pr-str)))";

        assertEquals("{:data [\"100\" \"200\"] :event \"scores\" :id \"100\"}", venice.eval(script));
    }

    @Test
    public void test_parse_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                \n" +
                "  (load-module :server-side-events ['server-side-events :as 'sse]) \n" +
                "  (-> (sse/render { :id \"100\"                                    \n" +
                "                    :event \"scores\"                              \n" +
                "                    :data [\"100\"] } )                            \n" +
                "      (sse/parse)                                                  \n" +
                "      (pr-str)))";

        assertEquals("{:data [\"100\"] :event \"scores\" :id \"100\"}", venice.eval(script));
    }

    @Test
    public void test_parse_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                \n" +
                "  (load-module :server-side-events ['server-side-events :as 'sse]) \n" +
                "  (-> (sse/render { :id \"100\"                                    \n" +
                "                    :event \"scores\"                              \n" +
                "                    :data [\"100\" \"200\"] } )                    \n" +
                "      (sse/parse)                                                  \n" +
                "      (pr-str)))";

        assertEquals("{:data [\"100\" \"200\"] :event \"scores\" :id \"100\"}", venice.eval(script));
    }

    @Test
    public void test_parse_3() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                \n" +
                "  (load-module :server-side-events ['server-side-events :as 'sse]) \n" +
                "  (-> (sse/render { :id \"100\"                                    \n" +
                "                    :data [\"100\"] } )                            \n" +
                "      (sse/parse)                                                  \n" +
                "      (pr-str)))";

        assertEquals("{:data [\"100\"] :id \"100\"}", venice.eval(script));
    }

    @Test
    public void test_parse_4() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                \n" +
                "  (load-module :server-side-events ['server-side-events :as 'sse]) \n" +
                "  (-> (sse/render { :data [\"100\"] } )                            \n" +
                "      (sse/parse)                                                  \n" +
                "      (pr-str)))";

        assertEquals("{:data [\"100\"]}", venice.eval(script));
    }

    @Test
    public void test_parse_5() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                \n" +
                "  (load-module :server-side-events ['server-side-events :as 'sse]) \n" +
                "  (-> (sse/render { :data [\"100\" \"200\"] } )                    \n" +
                "      (sse/parse)                                                  \n" +
                "      (pr-str)))";

        assertEquals("{:data [\"100\" \"200\"]}", venice.eval(script));
    }


    @Test
    public void test_read_event_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                            \n" +
                "  (load-module :server-side-events ['server-side-events :as 'sse])             \n" +
                "                                                                               \n" +
                "  (defn sample-events []                                                       \n" +
                "    (str (sse/render { :id \"100\"  :event \"scores\"   :data [\"100\"] } )    \n" +
                "         (sse/render { :id \"101\"  :event \"scores\"   :data [\"101\"] } )    \n" +
                "         (sse/render { :id \"102\"  :event \"scores\"   :data [\"102\"] } )    \n" +
                "         (sse/render { :id \"103\"  :event \"scores\"   :data [\"103\"] } )))  \n" +
                "                                                                               \n" +
                "  (try-with [is (io/string-in-stream (sample-events))                          \n" +
                "             rd (io/wrap-is-with-buffered-reader is :utf-8)]                   \n" +
                "    (pr-str (sse/read-event rd))))                                             ";

        assertEquals("{:data [\"100\"] :event \"scores\" :id \"100\"}", venice.eval(script));
    }

    @Test
    public void test_read_event_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                            \n" +
                "  (load-module :server-side-events ['server-side-events :as 'sse])             \n" +
                "                                                                               \n" +
                "  (defn sample-events []                                                       \n" +
                "    (str (sse/render { :id \"100\"  :event \"scores\"   :data [\"100\"] } )))  \n" +
                "                                                                               \n" +
                "  (try-with [is (io/string-in-stream (sample-events))                          \n" +
                "             rd (io/wrap-is-with-buffered-reader is :utf-8)]                   \n" +
                "    (pr-str (sse/read-event rd))))                                             ";

        assertEquals("{:data [\"100\"] :event \"scores\" :id \"100\"}", venice.eval(script));
    }

    @Test
    public void test_read_event_3() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                            \n" +
                "  (load-module :server-side-events ['server-side-events :as 'sse])             \n" +
                "                                                                               \n" +
                "  (defn sample-events [] \"\")                                                 \n" +
                "                                                                               \n" +
                "  (try-with [is (io/string-in-stream (sample-events))                          \n" +
                "             rd (io/wrap-is-with-buffered-reader is :utf-8)]                   \n" +
                "    (sse/read-event rd)))                                                      ";

        assertNull(venice.eval(script));
    }


    @Test
    public void test_read_events_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                            \n" +
                "  (load-module :server-side-events ['server-side-events :as 'sse])             \n" +
                "                                                                               \n" +
                "  (defn sample-events []                                                       \n" +
                "    (str (sse/render { :id \"100\"  :event \"scores\"   :data [\"100\"] } )    \n" +
                "         (sse/render { :id \"101\"  :event \"scores\"   :data [\"101\"] } )    \n" +
                "         (sse/render { :id \"102\"  :event \"scores\"   :data [\"102\"] } )    \n" +
                "         (sse/render { :id \"103\"  :event \"scores\"   :data [\"103\"] } )))  \n" +
                "                                                                               \n" +
                "  (try-with [is (io/string-in-stream (sample-events))                          \n" +
                "             rd (io/wrap-is-with-buffered-reader is :utf-8)]                   \n" +
                "    (pr-str (sse/read-events rd 1))))                                             ";

        assertEquals("[{:data [\"100\"] :event \"scores\" :id \"100\"}]", venice.eval(script));
    }

    @Test
    public void test_read_events_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                            \n" +
                "  (load-module :server-side-events ['server-side-events :as 'sse])             \n" +
                "                                                                               \n" +
                "  (defn sample-events []                                                       \n" +
                "    (str (sse/render { :id \"100\"  :event \"scores\"   :data [\"100\"] } )    \n" +
                "         (sse/render { :id \"101\"  :event \"scores\"   :data [\"101\"] } )    \n" +
                "         (sse/render { :id \"102\"  :event \"scores\"   :data [\"102\"] } )    \n" +
                "         (sse/render { :id \"103\"  :event \"scores\"   :data [\"103\"] } )))  \n" +
                "                                                                               \n" +
                "  (try-with [is (io/string-in-stream (sample-events))                          \n" +
                "             rd (io/wrap-is-with-buffered-reader is :utf-8)]                   \n" +
                "    (pr-str (sse/read-events rd 2))))                                             ";

        assertEquals("[" +
                     "{:data [\"100\"] :event \"scores\" :id \"100\"} " +
                     "{:data [\"101\"] :event \"scores\" :id \"101\"}" +
                     "]", venice.eval(script));
    }

    @Test
    public void test_read_events_3() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                            \n" +
                "  (load-module :server-side-events ['server-side-events :as 'sse])             \n" +
                "                                                                               \n" +
                "  (defn sample-events []                                                       \n" +
                "    (str (sse/render { :id \"100\"  :event \"scores\"   :data [\"100\"] } )    \n" +
                "         (sse/render { :id \"101\"  :event \"scores\"   :data [\"101\"] } )    \n" +
                "         (sse/render { :id \"102\"  :event \"scores\"   :data [\"102\"] } )    \n" +
                "         (sse/render { :id \"103\"  :event \"scores\"   :data [\"103\"] } )))  \n" +
                "                                                                               \n" +
                "  (try-with [is (io/string-in-stream (sample-events))                          \n" +
                "             rd (io/wrap-is-with-buffered-reader is :utf-8)]                   \n" +
                "    (pr-str (sse/read-events rd 3))))                                             ";

        assertEquals("[" +
                     "{:data [\"100\"] :event \"scores\" :id \"100\"} " +
                     "{:data [\"101\"] :event \"scores\" :id \"101\"} " +
                     "{:data [\"102\"] :event \"scores\" :id \"102\"}" +
                     "]", venice.eval(script));
    }

    @Test
    public void test_read_events_4() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                            \n" +
                "  (load-module :server-side-events ['server-side-events :as 'sse])             \n" +
                "                                                                               \n" +
                "  (defn sample-events []                                                       \n" +
                "    (str (sse/render { :id \"100\"  :event \"scores\"   :data [\"100\"] } )    \n" +
                "         (sse/render { :id \"101\"  :event \"scores\"   :data [\"101\"] } )    \n" +
                "         (sse/render { :id \"102\"  :event \"scores\"   :data [\"102\"] } )    \n" +
                "         (sse/render { :id \"103\"  :event \"scores\"   :data [\"103\"] } )))  \n" +
                "                                                                               \n" +
                "  (try-with [is (io/string-in-stream (sample-events))                          \n" +
                "             rd (io/wrap-is-with-buffered-reader is :utf-8)]                   \n" +
                "    (pr-str (sse/read-events rd 4))))                                             ";

        assertEquals("[" +
                        "{:data [\"100\"] :event \"scores\" :id \"100\"} " +
                        "{:data [\"101\"] :event \"scores\" :id \"101\"} " +
                        "{:data [\"102\"] :event \"scores\" :id \"102\"} " +
                        "{:data [\"103\"] :event \"scores\" :id \"103\"}" +
                     "]", venice.eval(script));
    }

    @Test
    public void test_read_events_5() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                            \n" +
                "  (load-module :server-side-events ['server-side-events :as 'sse])             \n" +
                "                                                                               \n" +
                "  (defn sample-events []                                                       \n" +
                "    (str (sse/render { :id \"100\"  :event \"scores\"   :data [\"100\"] } )    \n" +
                "         (sse/render { :id \"101\"  :event \"scores\"   :data [\"101\"] } )    \n" +
                "         (sse/render { :id \"102\"  :event \"scores\"   :data [\"102\"] } )    \n" +
                "         (sse/render { :id \"103\"  :event \"scores\"   :data [\"103\"] } )))  \n" +
                "                                                                               \n" +
                "  (try-with [is (io/string-in-stream (sample-events))                          \n" +
                "             rd (io/wrap-is-with-buffered-reader is :utf-8)]                   \n" +
                "    (pr-str (sse/read-events rd 5))))                                             ";

        assertEquals("[" +
                        "{:data [\"100\"] :event \"scores\" :id \"100\"} " +
                        "{:data [\"101\"] :event \"scores\" :id \"101\"} " +
                        "{:data [\"102\"] :event \"scores\" :id \"102\"} " +
                        "{:data [\"103\"] :event \"scores\" :id \"103\"}" +
                     "]", venice.eval(script));
    }

}
