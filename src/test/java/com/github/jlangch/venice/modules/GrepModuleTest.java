/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2022 Venice
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


public class GrepModuleTest {

    @Test
    public void test_grep() {
        final Venice venice = new Venice();

        try {
            venice.eval(
                "(do\n"
                + "   (load-module :grep)\n"
                + "\n"
                + "   (def content1 \"\"\"\n"
                + "                line 1  BBB\n"
                + "                line 2  AAA\n"
                + "                line 3  BBB\n"
                + "                line 4  BBB\n"
                + "                line 5  BBB\n"
                + "                line 6  BBB\n"
                + "                line 7  BBB\n"
                + "                line 8  AAA\n"
                + "                \"\"\")\n"
                + "\n"
                + "  (def content2 \"\"\"\n"
                + "               line 1  BBB\n"
                + "               line 2  BBB\n"
                + "               line 3  BBB\n"
                + "               line 4  BBB\n"
                + "               line 5  BBB\n"
                + "               line 6  BBB\n"
                + "               line 7  BBB\n"
                + "               line 8  AAA\n"
                + "               \"\"\")\n"
                + "\n"
                + " (def content3 \"\"\"\n"
                + "              line 1  BBB\n"
                + "              line 2  BBB\n"
                + "              line 3  BBB\n"
                + "              line 4  BBB\n"
                + "              line 5  BBB\n"
                + "              line 6  BBB\n"
                + "              line 7  BBB\n"
                + "              line 8  BBB\n"
                + "              \"\"\")\n"
                + "                                                                                        \n"
                + "   (defn lines [s]                                                                      \n"
                + "     (io/slurp-lines (io/string-in-stream s)))                                          \n"
                + "                                                                                        \n"
                + "   (def dir (io/temp-dir \"grep-\"))                                                    \n"
                + "   (def f1  (io/file dir \"step.log\"))                                                 \n"
                + "   (def f2  (io/file dir \"step.log.2022-04-01\"))                                      \n"
                + "   (def f3  (io/file dir \"step.log.2022-05-01\"))                                      \n"
                + "   (def f4  (io/file dir \"step.log.2022-06-01\"))                                      \n"
                + "   (def zip (io/file dir \"step.2022-05.zip\"))                                         \n"
                + "                                                                                        \n"
                + "   (io/spit f1 content1)                                                                \n"
                + "   (io/spit f2 content1)                                                                \n"
                + "   (io/spit f3 content2)                                                                \n"
                + "   (io/spit f4 content3)                                                                \n"
                + "   (io/zip-file zip f2 f3 f4)                                                           \n"
                + "                                                                                        \n"
                + "   (io/delete-file-on-exit dir)                                                         \n"
                + "   (io/delete-file-on-exit f1)                                                          \n"
                + "   (io/delete-file-on-exit f2)                                                          \n"
                + "   (io/delete-file-on-exit f3)                                                          \n"
                + "   (io/delete-file-on-exit f4)                                                          \n"
                + "   (io/delete-file-on-exit zip)                                                         \n"
                + "                                                                                        \n"
                + "   ;; grep machine readable                                                             \n"
                + "   (def r1 (grep/grep dir \"step.log.*\" \".*AAA.*\" :print false))                     \n"
                + "   (assert (= 3 (count r1)))                                                            \n"
                + "   (assert (= \"step.log.2022-04-01::2::line 2  AAA\" (str/join \"::\" (first r1))))    \n"
                + "   (assert (= \"step.log.2022-04-01::8::line 8  AAA\" (str/join \"::\" (second r1))))   \n"
                + "   (assert (= \"step.log.2022-05-01::8::line 8  AAA\" (str/join \"::\" (third r1))))    \n"
                + "                                                                                        \n"
                + "   ;; grep human readable                                                               \n"
                + "   (def r1 (lines (with-out-str (grep/grep dir                                          \n"
                + "                                           \"step.log.*\"                               \n"
                + "                                           \".*AAA.*\"                                  \n"
                + "                                           :print true))))                              \n"
                + "   (assert (= \"step.log.2022-04-01:2:line 2  AAA\" (first r1)))                        \n"
                + "   (assert (= \"step.log.2022-04-01:8:line 8  AAA\" (second r1)))                       \n"
                + "   (assert (= \"step.log.2022-05-01:8:line 8  AAA\" (third r1)))                        \n"
                + ")                                                                                       ");
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
