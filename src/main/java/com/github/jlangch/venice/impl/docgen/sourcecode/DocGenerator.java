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
package com.github.jlangch.venice.impl.docgen.sourcecode;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.compress.utils.Lists;

import com.github.jlangch.venice.impl.IVeniceInterpreter;
import com.github.jlangch.venice.impl.RunMode;
import com.github.jlangch.venice.impl.VeniceInterpreter;
import com.github.jlangch.venice.impl.docgen.util.CodeHighlighter;
import com.github.jlangch.venice.impl.docgen.util.ColorTheme;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.util.io.IOStreamUtil;
import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;
import com.lowagie.text.pdf.PdfReader;


public class DocGenerator {

    public DocGenerator() {

        final IVeniceInterpreter venice = new VeniceInterpreter(new AcceptAllInterceptor());

        final Env env = venice.createEnv(
                            Lists.newArrayList(),
                            false,
                            false,
                            RunMode.DOCGEN,
                            IOStreamUtil.nullPrintStream(),
                            IOStreamUtil.nullPrintStream(),
                            null);


        codeHighlighter = new CodeHighlighter(ColorTheme.getLightTheme());
    }

    public void run(final String version) {
        try {
            System.out.println("Creating source code doc");


            final Map<String,Object> data = new HashMap<>();
            data.put("meta-author", "Venice");
            data.put("version", version);
            data.put("code", "(+ 1 1)");

            // [1] create a HTML
            data.put("pdfmode", false);
            final String html = SourceCodeRenderer.renderXHTML(data);
            save(new File(getUserDir(), "source-code.html"), html);

            // [2] create a PDF
            data.put("pdfmode", true);
            final String xhtml = SourceCodeRenderer.renderXHTML(data);
            final ByteBuffer pdf = SourceCodeRenderer.renderPDF(xhtml);
            final byte[] pdfArr =  pdf.array();
            save(new File(getUserDir(), "source-code.pdf"), pdfArr);

            final PdfReader reader = new PdfReader(pdf.array());
            reader.close();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }


    private void save(final File file, final String text) throws Exception {
        save(file, text.getBytes("UTF-8"));
    }

    private void save(final File file, final byte[] data) throws Exception {
        try(FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data, 0, data.length);
            fos.flush();
        }
    }

    private File getUserDir() {
        return new File(System.getProperty("user.dir"));
    }

    private final CodeHighlighter codeHighlighter;
}
