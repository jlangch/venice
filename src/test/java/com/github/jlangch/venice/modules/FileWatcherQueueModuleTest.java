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

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class FileWatcherQueueModuleTest {

    @Test
    public void test_empty() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                   \n" +
                "   (load-module :file-watcher-queue ['file-watcher-queue :as 'wq])    \n" +
                "   (let [q (wq/create)]                                               \n" +
                "      (assert (wq/empty? q))                                          \n" +
                "      (assert (== 0 (wq/size q)))                                     \n" +
                "      (assert (nil? (wq/wal-file q)))))                               ";

        venice.eval(script);
    }

    @Test
    public void test_push() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                   \n" +
                "   (load-module :file-watcher-queue ['file-watcher-queue :as 'wq])    \n" +
                "   (let [q (wq/create)]                                               \n" +
                "      (wq/push q (io/file \"a\"))                                     \n" +
                "      (assert (not (wq/empty? q)))                                    \n" +
                "      (assert (== 1 (wq/size q)))                                     \n" +
                "      (assert (nil? (wq/wal-file q)))))                               ";

        venice.eval(script);
    }

    @Test
    public void test_push2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                   \n" +
                "   (load-module :file-watcher-queue ['file-watcher-queue :as 'wq])    \n" +
                "   (let [q (wq/create)]                                               \n" +
                "      (wq/push q (io/file \"a\"))                                     \n" +
                "      (wq/push q (io/file \"b\"))                                     \n" +
                "      (wq/push q (io/file \"c\"))                                     \n" +
                "      (assert (not (wq/empty? q)))                                    \n" +
                "      (assert (== 3 (wq/size q)))                                     \n" +
                "      (assert (nil? (wq/wal-file q)))))                               ";

        venice.eval(script);
    }

    @Test
    public void test_push3() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                   \n" +
                "   (load-module :file-watcher-queue ['file-watcher-queue :as 'wq])    \n" +
                "   (let [q (wq/create)]                                               \n" +
                "      (wq/push q (io/file \"a\"))                                     \n" +
                "      (wq/push q (io/file \"b\"))                                     \n" +
                "      (wq/push q (io/file \"c\"))                                     \n" +
                "      (wq/push q (io/file \"c\"))                                     \n" +
                "      (wq/push q (io/file \"c\"))                                     \n" +
                "      (assert (not (wq/empty? q)))                                    \n" +
                "      (assert (== 3 (wq/size q)))                                     \n" +
                "      (assert (nil? (wq/wal-file q)))))                               ";

        venice.eval(script);
    }

    @Test
    public void test_pop() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                   \n" +
                "   (load-module :file-watcher-queue ['file-watcher-queue :as 'wq])    \n" +
                "   (let [q (wq/create)]                                               \n" +
                "      (wq/push q (io/file \"a\"))                                     \n" +
                "      (assert (= \"a\" (io/file-path (wq/pop q))))                    \n" +
                "      (assert (wq/empty? q))                                          \n" +
                "      (assert (== 0 (wq/size q)))                                     \n" +
                "      (assert (nil? (wq/wal-file q)))))                               ";

        venice.eval(script);
   }

    @Test
    public void test_pop2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                   \n" +
                "   (load-module :file-watcher-queue ['file-watcher-queue :as 'wq])    \n" +
                "   (let [q (wq/create)]                                               \n" +
                "      (wq/push q (io/file \"a\"))                                     \n" +
                "      (wq/push q (io/file \"b\"))                                     \n" +
                "      (wq/push q (io/file \"c\"))                                     \n" +
                "      (assert (= \"a\" (io/file-path (wq/pop q))))                    \n" +
                "      (assert (= \"b\" (io/file-path (wq/pop q))))                    \n" +
                "      (assert (= \"c\" (io/file-path (wq/pop q))))                    \n" +
                "      (assert (wq/empty? q))                                          \n" +
                "      (assert (== 0 (wq/size q)))                                     \n" +
                "      (assert (nil? (wq/wal-file q)))))                               ";

        venice.eval(script);
    }

    @Test
    public void test_pop3() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                   \n" +
                "   (load-module :file-watcher-queue ['file-watcher-queue :as 'wq])    \n" +
                "   (let [q (wq/create)]                                               \n" +
                "      (wq/push q (io/file \"a\"))                                     \n" +
                "      (wq/push q (io/file \"b\"))                                     \n" +
                "      (wq/push q (io/file \"c\"))                                     \n" +
                "      (let [l1 (wq/pop q 2)                                           \n" +
                "            l2 (wq/pop q 2)                                           \n" +
                "            l3 (wq/pop q 2)]                                          \n" +
                "        (assert (= 2 (count l1)))                                     \n" +
                "        (assert (= 1 (count l2)))                                     \n" +
                "        (assert (= 0 (count l3)))                                     \n" +
                "        (assert (= \"a\" (io/file-path (first l1))))                  \n" +
                "        (assert (= \"b\" (io/file-path (second l1))))                 \n" +
                "        (assert (= \"c\" (io/file-path (first l2))))                  \n" +
                "        (assert (wq/empty? q))                                        \n" +
                "        (assert (== 0 (wq/size q)))                                   \n" +
                "        (assert (nil? (wq/wal-file q))))))                            ";

        venice.eval(script);
    }

    @Test
    public void test_pop4() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                   \n" +
                "   (load-module :file-watcher-queue ['file-watcher-queue :as 'wq])    \n" +
                "   (let [q (wq/create)]                                               \n" +
                "      (wq/push q (io/file \"a\"))                                     \n" +
                "      (wq/push q (io/file \"b\"))                                     \n" +
                "      (wq/push q (io/file \"c\"))                                     \n" +
                "      (let [l1 (wq/pop q 20)]                                         \n" +
                "        (assert (= 3 (count l1)))                                     \n" +
                "        (assert (= \"a\" (io/file-path (first l1))))                  \n" +
                "        (assert (= \"b\" (io/file-path (second l1))))                 \n" +
                "        (assert (= \"c\" (io/file-path (third l1))))                  \n" +
                "        (assert (wq/empty? q))                                        \n" +
                "        (assert (== 0 (wq/size q)))                                   \n" +
                "        (assert (nil? (wq/wal-file q))))))                            ";

        venice.eval(script);
    }

    @Test
    public void test_clear() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                   \n" +
                "   (load-module :file-watcher-queue ['file-watcher-queue :as 'wq])    \n" +
                "   (let [q (wq/create)]                                               \n" +
                "      (wq/push q (io/file \"a\"))                                     \n" +
                "      (wq/push q (io/file \"b\"))                                     \n" +
                "      (wq/push q (io/file \"c\"))                                     \n" +
                "      (assert (not (wq/empty? q)))                                    \n" +
                "      (assert (== 3 (wq/size q)))                                     \n" +
                "      (wq/clear q)                                                    \n" +
                "      (assert (wq/empty? q))                                          \n" +
                "      (assert (== 0 (wq/size q)))                                     \n" +
                "      (assert (nil? (wq/wal-file q)))))                               ";

        venice.eval(script);
    }

}
