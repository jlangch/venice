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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.util.StringUtil;


public class IpcFunctionsTest {

    @Test
    public void test_plain_text_message_expiry_1() {
        final Venice venice = new Venice();

        final String script =
                "(ipc/message-expired?                                       \n" +
                "  (ipc/plain-text-message \"1\" \"test\" \"hello 3\" false nil))  ";

        assertFalse((Boolean)venice.eval(script));
    }

    @Test
    public void test_plain_text_message_expiry_2() {
        final Venice venice = new Venice();

        final String script =
                "(ipc/message-expired?                                       \n" +
                "  (ipc/plain-text-message \"1\" \"test\" \"hello 3\" false (+ (current-time-millis) 3_600_000)))  ";

        assertFalse((Boolean)venice.eval(script));
    }

    @Test
    public void test_plain_text_message_expiry_3() {
        final Venice venice = new Venice();

        final String script =
                "(ipc/message-expired?                                       \n" +
                "  (ipc/plain-text-message \"1\" \"test\" \"hello 3\" false (- (current-time-millis) 3_600_000)))  ";

        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void test_plain_text_message_expiry_4() {
        final Venice venice = new Venice();

        final String script =
                "(ipc/message-expired?                                              \n" +
                "  (ipc/plain-text-message \"1\" \"test\" \"hello 3\" false 2 :minutes))  ";

        assertFalse((Boolean)venice.eval(script));
    }

    @Test
    public void test_plain_text_message_expiry_5() {
        final Venice venice = new Venice();

        final String script =
                "(ipc/message-expired?                                               \n" +
                "  (ipc/plain-text-message \"1\" \"test\" \"hello 3\" false -2 :minutes))  ";

        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void test_text_message_expiry_1() {
        final Venice venice = new Venice();

        final String script =
                "(ipc/message-expired?                                                      \n" +
                "  (ipc/text-message \"1\" \"test\" \"text/plain\" :UTF-8 \"hello 3\" false nil)) ";

        assertFalse((Boolean)venice.eval(script));
    }

    @Test
    public void test_text_message_expiry_2() {
        final Venice venice = new Venice();

        final String script =
                "(ipc/message-expired?                                                      \n" +
                "  (ipc/text-message \"1\" \"test\" \"text/plain\" :UTF-8 \"hello 3\" false (+ (current-time-millis) 3_600_000))) ";

        assertFalse((Boolean)venice.eval(script));
    }

    @Test
    public void test_text_message_expiry_3() {
        final Venice venice = new Venice();

        final String script =
                "(ipc/message-expired?                                                      \n" +
                "  (ipc/text-message \"1\" \"test\" \"text/plain\" :UTF-8 \"hello 3\" false (- (current-time-millis) 3_600_000))) ";

        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void test_text_message_expiry_4() {
        final Venice venice = new Venice();

        final String script =
                "(ipc/message-expired?                                                      \n" +
                "  (ipc/text-message \"1\" \"test\" \"text/plain\" :UTF-8 \"hello 3\" false 2 :minutes)) ";

        assertFalse((Boolean)venice.eval(script));
    }

    @Test
    public void test_text_message_expiry_5() {
        final Venice venice = new Venice();

        final String script =
                "(ipc/message-expired?                                                      \n" +
                "  (ipc/text-message \"1\" \"test\" \"text/plain\" :UTF-8 \"hello 3\" false -2 :minutes)) ";

        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void test_binary_message_expiry_1() {
        final Venice venice = new Venice();

        final String script =
                "(ipc/message-expired?                                                                     \n" +
                "  (ipc/binary-message \"1\" \"test\" \"application/octet-stream\" (bytebuf [0 1 2]) false nil)) ";

        assertFalse((Boolean)venice.eval(script));
    }

    @Test
    public void test_binary_message_expiry_2() {
        final Venice venice = new Venice();

        final String script =
                "(ipc/message-expired?                                                                     \n" +
                "  (ipc/binary-message \"1\" \"test\" \"application/octet-stream\" (bytebuf [0 1 2]) false (+ (current-time-millis) 3_600_000))) ";

        assertFalse((Boolean)venice.eval(script));
    }

    @Test
    public void test_binary_message_expiry_3() {
        final Venice venice = new Venice();

        final String script =
                "(ipc/message-expired?                                                                     \n" +
                "  (ipc/binary-message \"1\" \"test\" \"application/octet-stream\" (bytebuf [0 1 2]) false (- (current-time-millis) 3_600_000))) ";

        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void test_binary_message_expiry_4() {
        final Venice venice = new Venice();

        final String script =
                "(ipc/message-expired?                                                                     \n" +
                "  (ipc/binary-message \"1\" \"test\" \"application/octet-stream\" (bytebuf [0 1 2]) false 2 :minutes)) ";

        assertFalse((Boolean)venice.eval(script));
    }

    @Test
    public void test_binary_message_expiry_5() {
        final Venice venice = new Venice();

        final String script =
                "(ipc/message-expired?                                                                     \n" +
                "  (ipc/binary-message \"1\" \"test\" \"application/octet-stream\" (bytebuf [0 1 2]) false -2 :minutes)) ";

        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void test_venice_message_expiry_1() {
        final Venice venice = new Venice();

        final String script =
                "(ipc/message-expired?                                        \n" +
                "  (ipc/venice-message \"1\" \"test\" {:a 100, :b 200} false nil))  ";

        assertFalse((Boolean)venice.eval(script));
    }

    @Test
    public void test_venice_message_expiry_2() {
        final Venice venice = new Venice();

        final String script =
                "(ipc/message-expired?                                        \n" +
                "  (ipc/venice-message \"1\" \"test\" {:a 100, :b 200} false (+ (current-time-millis) 3_600_000)))  ";

        assertFalse((Boolean)venice.eval(script));
    }

    @Test
    public void test_venice_message_expiry_3() {
        final Venice venice = new Venice();

        final String script =
                "(ipc/message-expired?                                        \n" +
                "  (ipc/venice-message \"1\" \"test\" {:a 100, :b 200} false (- (current-time-millis) 3_600_000)))  ";

        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void test_venice_message_expiry_4() {
        final Venice venice = new Venice();

        final String script =
                "(ipc/message-expired?                                        \n" +
                "  (ipc/venice-message \"1\" \"test\" {:a 100, :b 200} false 2 :minutes))  ";

        assertFalse((Boolean)venice.eval(script));
    }

    @Test
    public void test_venice_message_expiry_5() {
        final Venice venice = new Venice();

        final String script =
                "(ipc/message-expired?                                        \n" +
                "  (ipc/venice-message \"1\" \"test\" {:a 100, :b 200} false -2 :minutes))  ";

        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void test_client_clone() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                             \n" +
                "  (def counter (atom 0))                                                        \n" +
                "                                                                                \n" +
                "  (defn echo-handler [m]                                                        \n" +
                "    (swap! counter inc)                                                         \n" +
                "    m)                                                                          \n" +
                "                                                                                \n" +
                "  (try-with [server   (ipc/server 33333 echo-handler)                           \n" +
                "             client-1 (ipc/client \"localhost\" 33333 :encrypted true)          \n" +
                "             client-2 (ipc/clone client-1)                                      \n" +
                "             client-3 (ipc/clone client-1)]                                     \n" +
                "    (ipc/send client-1 (ipc/plain-text-message \"1\" \"test\" \"hello 1\"))     \n" +
                "    (ipc/send client-2 (ipc/plain-text-message \"2\" \"test\" \"hello 2\"))     \n" +
                "    (ipc/send client-3 (ipc/plain-text-message \"3\" \"test\" \"hello 3\"))     \n" +
                "    (sleep 100))                                                                \n" +
                "                                                                                \n" +
                "  (deref counter))";

        assertEquals(3L, venice.eval(script));
    }

    @Test
    public void test_send_receive() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                         \n" +
                "  (defn echo-handler [m] m)                                 \n" +
                "                                                            \n" +
                "  (try-with [server (ipc/server 33333 echo-handler)         \n" +
                "             client (ipc/client \"localhost\" 33333)]       \n" +
                "    (->> (ipc/plain-text-message \"1\" \"test\" \"hello\")  \n" +
                "         (ipc/send client)                                  \n" +
                "         (ipc/message->map)                                 \n" +
                "         (:text ))))                                        ";

        assertEquals("hello", venice.eval(script));
    }

    @Test
    public void test_send_receive_async() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                         \n" +
                "  (defn echo-handler [m] m)                                 \n" +
                "                                                            \n" +
                "  (try-with [server (ipc/server 33333 echo-handler)         \n" +
                "             client (ipc/client \"localhost\" 33333)]       \n" +
                "    (->> (ipc/plain-text-message \"1\" \"test\" \"hello\")  \n" +
                "         (ipc/send-async client)                            \n" +
                "         (deref)                                            \n" +
                "         (ipc/message->map)                                 \n" +
                "         (:text ))))                                        ";

        assertEquals("hello", venice.eval(script));
    }

    @Test
    public void test_send_oneway() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                         \n" +
                "  (defn echo-handler [m] nil)                               \n" +
                "                                                            \n" +
                "  (try-with [server (ipc/server 33333 echo-handler)         \n" +
                "             client (ipc/client \"localhost\" 33333)]       \n" +
                "    (->> (ipc/plain-text-message \"1\" \"test\" \"hello\")  \n" +
                "         (ipc/send-oneway client))))                        ";

        assertEquals(null, venice.eval(script));
    }

    @Test
    public void test_send_receive_no_compress() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                           \n" +
                "  (defn echo-handler [m] m)                                                   \n" +
                "                                                                              \n" +
                "  (try-with [server (ipc/server 33333 echo-handler                            \n" +
                "                                :compress-cutoff-size -1)                     \n" +
                "             client (ipc/client \"localhost\" 33333                           \n" +
                "                                :compress-cutoff-size -1)]                    \n" +
                "    (->> (ipc/plain-text-message \"1\" \"test\" (str/repeat \"hello\" 1_000)) \n" +
                "         (ipc/send client)                                                    \n" +
                "         (ipc/message->map)                                                   \n" +
                "         (:text ))))                                                          ";

        assertEquals(StringUtil.repeat("hello", 1_000), venice.eval(script));
    }

    @Test
    public void test_send_receive_always_compress() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                           \n" +
                "  (defn echo-handler [m] m)                                                   \n" +
                "                                                                              \n" +
                "  (try-with [server (ipc/server 33333 echo-handler                            \n" +
                "                                :compress-cutoff-size 0)                      \n" +
                "             client (ipc/client \"localhost\" 33333                           \n" +
                "                                :compress-cutoff-size 0)]                     \n" +
                "    (->> (ipc/plain-text-message \"1\" \"test\" (str/repeat \"hello\" 1_000)) \n" +
                "         (ipc/send client)                                                    \n" +
                "         (ipc/message->map)                                                   \n" +
                "         (:text ))))                                                          ";

        assertEquals(StringUtil.repeat("hello", 1_000), venice.eval(script));
    }

    @Test
    public void test_send_receive_size_compress() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                           \n" +
                "  (defn echo-handler [m] m)                                                   \n" +
                "                                                                              \n" +
                "  (try-with [server (ipc/server 33333 echo-handler                            \n" +
                "                                :compress-cutoff-size :2KB)                   \n" +
                "             client (ipc/client \"localhost\" 33333                           \n" +
                "                                :compress-cutoff-size :2KB)]                  \n" +
                "    (->> (ipc/plain-text-message \"1\" \"test\" (str/repeat \"hello\" 1_000)) \n" +
                "         (ipc/send client)                                                    \n" +
                "         (ipc/message->map)                                                   \n" +
                "         (:text ))))                                                          ";

        assertEquals(StringUtil.repeat("hello", 1_000), venice.eval(script));
    }

    @Test
    public void test_thread_based_eval() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                           \n" +
                "  (defn handler [m]                                           \n" +
                "    (let [cmd    (. m :getText)                               \n" +
                "          result (str (eval (read-string cmd)))]              \n" +
                "      (ipc/plain-text-message (. m :getRequestId)             \n" +
                "                              (. m :getTopic)                 \n" +
                "                              result)))                       \n" +
                "                                                              \n" +
                "  (try-with [server (ipc/server 33333 handler)                \n" +
                "             client (ipc/client \"localhost\" 33333)]         \n" +
                "    (-<> (ipc/plain-text-message \"1\" \"exec\" \"(+ 1 2)\")  \n" +
                "         (ipc/send client <>)                                 \n" +
                "         (. <> :getText))))                                   ";

        assertEquals("3", venice.eval(script));
    }

    @Test
    public void test_queue_status() {
        final Venice venice = new Venice();

        final String script =
                "(do                                             \n" +
                "  (try-with [server (ipc/server 33333)          \n" +
                "             client (ipc/client 33333)]         \n" +
                "     (ipc/create-queue server :orders 100)      \n" +
                "     ;; ...                                     \n" +
                "     (ipc/queue-status client :orders)))        ";

        @SuppressWarnings("unchecked")
        final Map<String,Object> s = (Map<String,Object>)venice.eval(script);
        assertNotNull(s);

        assertEquals("orders",  s.get("name"));
        assertEquals(true,      s.get("exists"));
        assertEquals("BOUNDED", s.get("type"));
        assertEquals(false,     s.get("temporary"));
        assertEquals(100L,      s.get("capacity"));
        assertEquals(0L,        s.get("size"));
    }

    @Test
    public void test_queue_create_permission_1() {
        final Venice venice = new Venice();

        final String script =
                "(try-with [server (ipc/server 33333)       \n" +
                "           client (ipc/client 33333)]      \n" +
                "  (ipc/create-queue client :orders 100))   ";

        try {
          venice.eval(script);
          assertTrue(true);
        }
        catch(VncException ex) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void test_queue_create_permission_2() {
        final Venice venice = new Venice();

        final String script =
                "(try-with [server (ipc/server 33333 :permit-client-queue-mgmt true)  \n" +
                "           client (ipc/client 33333)]      \n" +
                "  (ipc/create-queue client :orders 100))   ";

        try {
          venice.eval(script);
          assertTrue(true);
        }
        catch(VncException ex) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void test_queue_create_permission_3() {
        final Venice venice = new Venice();

        final String script =
                "(try-with [server (ipc/server 33333:permit-client-queue-mgmt false)  \n" +
                "           client (ipc/client 33333)]      \n" +
                "  (ipc/create-queue client :orders 100))   ";

        try {
          venice.eval(script);
          fail("Expected exception");
        }
        catch(VncException ex) {
            assertTrue(true);
        }
    }

    @Test
    public void test_queue_remove_permission_1() {
        final Venice venice = new Venice();

        final String script =
                "(try-with [server (ipc/server 33333)       \n" +
                "           client (ipc/client 33333)]      \n" +
                "  (ipc/create-queue server :orders 100)    \n" +
                "  (ipc/remove-queue client :orders))   ";

        try {
          venice.eval(script);
          assertTrue(true);
        }
        catch(VncException ex) {
            System.err.println(ex.getMessage());
            fail("Unexpected exception");
        }
    }

    @Test
    public void test_queue_remove_permission_2() {
        final Venice venice = new Venice();

        final String script =
                "(try-with [server (ipc/server 33333 :permit-client-queue-mgmt true)  \n" +
                "           client (ipc/client 33333)]      \n" +
                "  (ipc/create-queue server :orders 100)    \n" +
                "  (ipc/remove-queue client :orders))   ";

        try {
          venice.eval(script);
          assertTrue(true);
        }
        catch(VncException ex) {
            System.err.println(ex.getMessage());
            fail("Unexpected exception");
        }
    }

    @Test
    public void test_queue_remove_permission_3() {
        final Venice venice = new Venice();

        final String script =
                "(try-with [server (ipc/server 33333:permit-client-queue-mgmt false)  \n" +
                "           client (ipc/client 33333)]      \n" +
                "  (ipc/create-queue server :orders 100)    \n" +
                "  (ipc/remove-queue client :orders))   ";

        try {
          venice.eval(script);
          fail("Expected exception");
        }
        catch(VncException ex) {
            System.err.println(ex.getMessage());
            assertTrue(true);
        }
    }

    @Test
    public void test_map2json_a() {
        final Venice venice = new Venice();

        final String script =
                "(do                                             \n" +
                "  (->> (ipc/text-message \"1\"                  \n" +
                "                         \"test\"              \n" +
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
                "  (->> (ipc/text-message \"1\"                  \n" +
                "                         \"test\"              \n" +
                "                         \"text/plain\"         \n" +
                "                         :UTF-8                 \n" +
                "                         \"hello\")             \n" +
                "       (ipc/message->json true)))               ";

        assertNotNull(venice.eval(script));
    }

    @Test
    public void test_handler_exception() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                              \n" +
                "  (ns junit)                                                     \n" +
                "                                                                 \n" +
                "  (defn handler [m]                                              \n" +
                "    (let [cmd (. m :getText)]                                    \n" +
                "      (throw :VncException \"TEST\")))                           \n" +
                "                                                                 \n" +
                "  (try-with [server (ipc/server 33333 handler)                   \n" +
                "             client (ipc/client \"localhost\" 33333)]            \n" +
                "    (let [m (ipc/plain-text-message \"1\" \"exec\" \"(+ 1 2)\")  \n" +
                "          r        (ipc/send client m)                           \n" +
                "          status   (ipc/message-field r :response-status)        \n" +
                "          topic    (ipc/message-field r :topic)                  \n" +
                "          mimetype (ipc/message-field r :payload-mimetype)       \n" +
                "          text     (ipc/message-field r :payload-text)]          \n" +
                "      (assert (= :HANDLER_ERROR status))                         \n" +
                "      (assert (= \"exec\" topic))                                \n" +
                "      (assert (= \"text/plain\" mimetype))                       \n" +
                "      text)))                                                    ";

        assertEquals(
                "Failed to handle request of type REQUEST!\n"
                + "throw (test-script: line 6, col 8)\n"
                + "junit/handler (test-script: line 4, col 9)\n"
                + "ipc/server (unknown: line -1, col -1)",
                venice.eval("test-script", script));
    }

}
