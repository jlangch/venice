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

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class ExcelModuleTest {

    @Test
    public void test_reader() {
        final Venice venice = new Venice();

        final String script =
                "(do\n"
                + "  (ns test)\n"
                + "\n"
                + "  (load-module :excel)\n"
                + "  \n"
                + "  (defn create-excel []\n"
                + "    (let [wbook (excel/create :xlsx)\n"
                + "          sheet (excel/add-sheet wbook \"Data\")]\n"
                + "      (excel/write-data sheet [[\"foo\" \n"
                + "                                false \n"
                + "                                100 \n"
                + "                                100.123\n"
                + "                                (time/local-date 2021 1 1)\n"
                + "                                (time/local-date-time 2021 1 1 15 30 45)\n"
                + "                                {:formula \"SUM(C1,D1)\"}\n"
                + "                                \"\" \n"
                + "                                nil]])\n"
                + "      (excel/write->bytebuf wbook)))\n"
                + "\n"
                + "  (let [wbook (excel/open (create-excel))\n"
                + "        sheet (excel/sheet wbook \"Data\")]\n"
                + "    (excel/evaluate-formulas wbook) ;; evaluate the formulas!\n"
                + "    (println \"Cell (1,1): ~(pr-str (excel/read-string-val sheet 1 1))\")\n"
                + "    (println \"Cell (1,2): ~(pr-str (excel/read-boolean-val sheet 1 2))\")\n"
                + "    (println \"Cell (1,3): ~(pr-str (excel/read-long-val sheet 1 3))\")\n"
                + "    (println \"Cell (1,4): ~(pr-str (excel/read-double-val sheet 1 4))\")\n"
                + "    (println \"Cell (1,5): ~(pr-str (excel/read-date-val sheet 1 5))\")\n"
                + "    (println \"Cell (1,6): ~(pr-str (excel/read-datetime-val sheet 1 6))\")\n"
                + "    (println \"Cell (1,7): ~(pr-str (excel/read-double-val sheet 1 7))\")\n"
                + "    (println \"Cell (1,8): ~(pr-str (excel/read-string-val sheet 1 8))\")\n"
                + "    (println \"Cell (1,9): ~(pr-str (excel/read-string-val sheet 1 9))\")))\n"
                + "";

        venice.eval(script);

        assertTrue(true);
    }

}
