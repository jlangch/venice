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
package com.github.jlangch.venice.util.ipc.impl.wal;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class OfferPollTest {

    @Test
    public void testOfferPollDurableQueue_with_wal_compact() throws Exception {
        final Venice venice = new Venice();

        try {
            venice.eval(
                "(let [wal-dir (io/file (io/temp-dir \"wal-\"))]                                           \n" +
                "  (try                                                                                    \n" +
                "    (try-with [server (ipc/server 33333                                                   \n" +
                "                                  :write-ahead-log-dir wal-dir                            \n" +
                "                                  :write-ahead-log-compress true                          \n" +
                "                                  :write-ahead-log-compact true)                          \n" +
                "               client (ipc/client 33333)]                                                 \n" +
                "                                                                                          \n" +
                "      (sleep 100)                                                                         \n" +
                "                                                                                          \n" +
                "      ;; create the durable queue :testq                                                  \n" +
                "      (ipc/create-queue server :testq 100 :bounded true)                                  \n" +
                "                                                                                          \n" +
                "      ;; offer 3 durable and 1 nondurable message                                         \n" +
                "      (ipc/offer client :testq 300 (ipc/plain-text-message \"1\" :test \"hello 1\" true)) \n" +
                "      (ipc/offer client :testq 300 (ipc/plain-text-message \"2\" :test \"hello 2\" true)) \n" +
                "      (ipc/offer client :testq 300 (ipc/plain-text-message \"3\" :test \"hello 3\" false))\n" +
                "      (ipc/offer client :testq 300 (ipc/plain-text-message \"4\" :test \"hello 4\" true)) \n" +
                "                                                                                          \n" +
                "      ;; poll message #1                                                                  \n" +
                "      (let [m (ipc/poll client :testq 300)]                                               \n" +
                "        (assert (ipc/response-ok? m))                                                     \n" +
                "        (assert (== \"hello 1\" (ipc/message-field m :payload-text)))))                   \n" +
                "                                                                                          \n" +
                "    (sleep 100)                                                                           \n" +
                "                                                                                          \n" +
                "    ;; restart client/server to test Write-Ahead-Logs                                     \n" +
                "    ;; the new server will read the Write-Ahead-Logs and populate the queue :testq        \n" +
                "    (try-with [server (ipc/server 33333                                                   \n" +
                "                                  :write-ahead-log-dir wal-dir                            \n" +
                "                                  :write-ahead-log-compress true                          \n" +
                "                                  :write-ahead-log-compact true)                          \n" +
                "               client (ipc/client 33333)]                                                 \n" +
                "                                                                                          \n" +
                "      (sleep 100)                                                                         \n" +
                "                                                                                          \n" +
                "      ;; create the durable queue :testq                                                  \n" +
                "      ;; if the queue already exists due to the WAL recovery process, this                \n" +
                "      ;; queue create request will just be skipped!                                       \n" +
                "      (ipc/create-queue server :testq 100 :bounded true)                                  \n" +
                "                                                                                          \n" +
                "      ;; poll message #2                                                                  \n" +
                "      (let [m (ipc/poll client :testq 300)]                                               \n" +
                "        (assert (ipc/response-ok? m))                                                     \n" +
                "        (assert (== \"hello 2\" (ipc/message-field m :payload-text))))                    \n" +
                "                                                                                          \n" +
                "      ;; message #3 is nondurable and therefore lost after server shutdown                \n" +
                "                                                                                          \n" +
                "      ;; poll message #4                                                                  \n" +
                "      (let [m (ipc/poll client :testq 300)]                                               \n" +
                "        (assert (ipc/response-ok? m))                                                     \n" +
                "        (assert (== \"hello 4\" (ipc/message-field m :payload-text)))))                   \n" +
                "                                                                                          \n" +
                "    (sleep 100)                                                                           \n" +
                "                                                                                          \n" +
                "    (finally (io/delete-file-tree wal-dir))))                                             ");
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void testOfferPollDurableQueue_without_wal_compact() throws Exception {
        final Venice venice = new Venice();

        try {
            venice.eval(
                "(let [wal-dir (io/file (io/temp-dir \"wal-\"))]                                           \n" +
                "  (try                                                                                    \n" +
                "    (try-with [server (ipc/server 33333                                                   \n" +
                "                                  :write-ahead-log-dir wal-dir                            \n" +
                "                                  :write-ahead-log-compress true                          \n" +
                "                                  :write-ahead-log-compact false)                         \n" +
                "               client (ipc/client 33333)]                                                 \n" +
                "                                                                                          \n" +
                "      (sleep 100)                                                                         \n" +
                "                                                                                          \n" +
                "      ;; create the durable queue :testq                                                  \n" +
                "      (ipc/create-queue server :testq 100 :bounded true)                                  \n" +
                "                                                                                          \n" +
                "      ;; offer 3 durable and 1 nondurable message                                         \n" +
                "      (ipc/offer client :testq 300 (ipc/plain-text-message \"1\" :test \"hello 1\" true)) \n" +
                "      (ipc/offer client :testq 300 (ipc/plain-text-message \"2\" :test \"hello 2\" true)) \n" +
                "      (ipc/offer client :testq 300 (ipc/plain-text-message \"3\" :test \"hello 3\" false))\n" +
                "      (ipc/offer client :testq 300 (ipc/plain-text-message \"4\" :test \"hello 4\" true)) \n" +
                "                                                                                          \n" +
                "      ;; poll message #1                                                                  \n" +
                "      (let [m (ipc/poll client :testq 300)]                                               \n" +
                "        (assert (ipc/response-ok? m))                                                     \n" +
                "        (assert (== \"hello 1\" (ipc/message-field m :payload-text)))))                   \n" +
                "                                                                                          \n" +
                "    (sleep 100)                                                                           \n" +
                "                                                                                          \n" +
                "    ;; restart client/server to test Write-Ahead-Logs                                     \n" +
                "    ;; the new server will read the Write-Ahead-Logs and populate the queue :testq        \n" +
                "    (try-with [server (ipc/server 33333                                                   \n" +
                "                                  :write-ahead-log-dir wal-dir                            \n" +
                "                                  :write-ahead-log-compress true                          \n" +
                "                                  :write-ahead-log-compact false)                         \n" +
                "               client (ipc/client 33333)]                                                 \n" +
                "                                                                                          \n" +
                "      (sleep 100)                                                                         \n" +
                "                                                                                          \n" +
                "      ;; create the durable queue :testq                                                  \n" +
                "      ;; if the queue already exists due to the WAL recovery process, this                \n" +
                "      ;; queue create request will just be skipped!                                       \n" +
                "      (ipc/create-queue server :testq 100 :bounded true)                                  \n" +
                "                                                                                          \n" +
                "      ;; poll message #2                                                                  \n" +
                "      (let [m (ipc/poll client :testq 300)]                                               \n" +
                "        (assert (ipc/response-ok? m))                                                     \n" +
                "        (assert (== \"hello 2\" (ipc/message-field m :payload-text))))                    \n" +
                "                                                                                          \n" +
                "      ;; message #3 is nondurable and therefore lost after server shutdown                \n" +
                "                                                                                          \n" +
                "      ;; poll message #4                                                                  \n" +
                "      (let [m (ipc/poll client :testq 300)]                                               \n" +
                "        (assert (ipc/response-ok? m))                                                     \n" +
                "        (assert (== \"hello 4\" (ipc/message-field m :payload-text)))))                   \n" +
                "                                                                                          \n" +
                "    (sleep 100)                                                                           \n" +
                "                                                                                          \n" +
                "    (finally (io/delete-file-tree wal-dir))))                                             ");
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
