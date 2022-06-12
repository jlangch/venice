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
package com.github.jlangch.venice.pdf;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class PdfRendererTest {

    @Test
    public void testRenderer() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                \n" +
                "     (ns test)                                                     \n" +
                "                                                                   \n" +
                "     (import :com.github.jlangch.venice.util.pdf.PdfRenderer)      \n" +
                "                                                                   \n" +
                "     (load-module :kira)                                           \n" +
                "                                                                   \n" +
                "     (defn format-ts [t] (time/format t \"yyyy-MM-dd\"))           \n" +
                "                                                                   \n" +
                "     ; define the template                                         \n" +
                "     (def template (str/strip-indent                               \n" +
                "        \"\"\"<?xml version=\"1.0\" encoding=\"UTF-8\"?>           \n" +
                "        <html lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\">  \n" +
                "          <body>                                                   \n" +
                "            <div>${ (kira/escape-xml title) }$</div>               \n" +
                "            <div>${ (kira/escape-xml ts test/format-ts) }$</div>   \n" +
                "          </body>                                                  \n" +
                "        </html>                                                    \n" +
                "        \"\"\"))                                                   \n" +
                "                                                                   \n" +
                "     (def data { :title \"Hello, world\"                           \n" +
                "                 :ts (time/local-date 2000 8 1) })                 \n" +
                "                                                                   \n" +
                "     (def xhtml (kira/eval template [\"${\" \"}$\"] data))         \n" +
                "                                                                   \n" +
                "     (. :PdfRenderer :render xhtml)                                \n" +
                "   )                                                               \n";

        venice.eval(script);
    }

}
