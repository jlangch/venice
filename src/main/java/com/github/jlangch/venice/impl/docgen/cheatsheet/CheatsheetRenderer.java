/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
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
package com.github.jlangch.venice.impl.docgen.cheatsheet;

import java.nio.ByteBuffer;
import java.util.Map;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.util.io.ClassPathResource;
import com.github.jlangch.venice.util.pdf.PdfRenderer;


public class CheatsheetRenderer {

    public static String parseXhtmlTemplate() {
        try {
            final String template = loadCheatSheetXhtmlTemplate();

            final String script = "(do                                              \n" +
                                  "   (load-module :kira)                           \n" +
                                  "   (kira/parse-string template [\"${\" \"}$\"]))   ";

            // apply the template
            return (String)new Venice().eval(
                            script,
                            Parameters.of("template", template));
        }
        catch(VncException ex) {
            throw new RuntimeException(
                        "Failed to parse cheatsheet template. \n" +
                        "Venice Callstack: \n" + ex.getCallStackAsString("   "),
                        ex);
        }
        catch(Exception ex) {
            throw new RuntimeException("Failed to parse cheatsheet template", ex);
        }
    }

    public static String renderXHTML(final Map<String,Object> data) {
        try {
            final String template = loadCheatSheetXhtmlTemplate();

            final String script = "(do                                           \n" +
                                  "   (load-module :kira)                        \n" +
                                  "   (kira/eval template [\"${\" \"}$\"] data))   ";

            // apply the template
            return (String)new Venice().eval(
                            script,
                            Parameters.of("template", template, "data", data));
        }
        catch(VncException ex) {
            throw new RuntimeException(
                        "Failed to render cheatsheet XHTML. \n" +
                        "Venice Callstack: \n" + ex.getCallStackAsString("   "),
                        ex);
        }
        catch(Exception ex) {
            throw new RuntimeException("Failed to render cheatsheet XHTML", ex);
        }
    }

    public static ByteBuffer renderPDF(final String xhtml) {
        return PdfRenderer.render(
                xhtml,
                "classpath:com/github/jlangch/venice/fonts/");
    }


    public static String renderASCII(final Map<String,Object> data) {
        try {
            final String template = loadCheatSheetAsciiTemplate();

            final String script = "(do                                           \n" +
                                  "   (load-module :kira)                        \n" +
                                  "   (kira/eval template [\"${\" \"}$\"] data))   ";

            // apply the template
            return (String)new Venice().eval(
                            script,
                            Parameters.of("template", template, "data", data));
        }
        catch(VncException ex) {
            throw new RuntimeException(
                        "Failed to render cheatsheet ASCII. \n" +
                        "Venice Callstack: \n" + ex.getCallStackAsString("   "),
                        ex);
        }
        catch(Exception ex) {
            throw new RuntimeException("Failed to render cheatsheet ASCII", ex);
        }
    }

    private static String loadCheatSheetXhtmlTemplate() {
        return new ClassPathResource(Venice.class.getPackage(), "docgen/cheatsheet2.html")
                        .getResourceAsString("UTF-8");
    }

    private static String loadCheatSheetAsciiTemplate() {
        return new ClassPathResource(Venice.class.getPackage(), "docgen/cheatsheet2.ascii")
                        .getResourceAsString("UTF-8");
    }
}
