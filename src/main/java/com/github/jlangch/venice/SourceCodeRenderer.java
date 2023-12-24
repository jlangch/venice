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
package com.github.jlangch.venice;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.docgen.util.CodeHighlighter;
import com.github.jlangch.venice.impl.docgen.util.ColorTheme;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.io.ClassPathResource;
import com.github.jlangch.venice.impl.util.kira.KiraTemplateEvaluator;
import com.github.jlangch.venice.util.pdf.PdfRenderer;
import com.lowagie.text.pdf.PdfReader;


public class SourceCodeRenderer {

    private SourceCodeRenderer() {
        this.codeHighlighter = new CodeHighlighter(ColorTheme.getLightTheme());
    }

    public static void render(
            final String sourceFile,
            final String destDir,
            final String fontDir,
            final boolean lineNumbering,
            final boolean syntaxHighlighted
    ) {
        render(new File(sourceFile), new File(destDir), new File(fontDir), lineNumbering, syntaxHighlighted);
    }

    public static void render(
            final File sourceFile,
            final File destDir,
            final File fontDir,
            final boolean lineNumbering,
            final boolean syntaxHighlighted
    ) {
        try {
            if (sourceFile == null) {
                throw new IllegalArgumentException("A 'sourceFile' must not be null!");
            }
            if (fontDir == null) {
                throw new IllegalArgumentException("A 'fontDir' must not be null!");
            }

            final File dir = destDir == null ? getUserDir() : destDir;
            final String name = sourceFile.getName();

            if (!sourceFile.canRead()) {
                throw new FileException(
                        "The file '" + sourceFile.getPath() + "' cannot be read!");
            }
            if (!dir.isDirectory()) {
                throw new FileException(
                        "The destination dir '" + dir.getPath() + "' is not a directory!");
            }
            if (!fontDir.isDirectory()) {
                throw new FileException(
                        "The font dir '" + dir.getPath() + "' is not a directory!");
            }

            if (!areFontsAvailable(fontDir)) {
                return;
            }

            new SourceCodeRenderer().renderSourceCode(
                    read(sourceFile),
                    sourceFile,
                    new File(dir, name + ".html"),
                    new File(dir, name + ".pdf"),
                    fontDir,
                    lineNumbering,
                    syntaxHighlighted);
        }
        catch(Exception ex) {
            throw new RuntimeException("Failed to Venice source file as HTML/PDF", ex);
        }
    }

    private void renderSourceCode(
            final String source,
            final File srcFile,
            final File htmlFile,
            final File pdfFile,
            final File fontDir,
            final boolean lineNumbering,
            final boolean syntaxHighlighted
    ) throws Exception {
        System.out.println("Processing Venice file:        " + srcFile.getAbsolutePath());
        System.out.println("Rendering HTML source code to: " + htmlFile.getAbsolutePath());
        System.out.println("Rendering PDF source code to:  " + pdfFile.getAbsolutePath());
        System.out.println("Using font dir:                " + fontDir.getAbsolutePath());

        String wrapped = "(do\n" + source + "\n)";

        String codeHighlighted = syntaxHighlighted ? codeHighlighter.highlight(wrapped) : wrapped;

        List<String> lines = StringUtil.splitIntoLines(codeHighlighted);
        lines = lines.subList(1, lines.size()-1); // unwrap

        if (lineNumbering) {
        	final String lineNrFormat = syntaxHighlighted
        									? "<span style=\"color: #808080\">%04d   </span>%s"
        									: "%04d   ";

            final AtomicLong line = new AtomicLong(1);
            lines = lines.stream()
                         .map(s -> String.format(lineNrFormat, line.getAndIncrement(), s))
                         .collect(Collectors.toList());
        }

        codeHighlighted = String.join("\n", lines);

        // final String baseURL = "classpath:/fonts/";
        final String baseURL = fontDir.toURI().toURL().toString();

        final Map<String,Object> data = new HashMap<>();
        data.put("meta-author", "Venice");
        data.put("version", Venice.getVersion());
        data.put("filename", srcFile.getName());
        data.put("code", codeHighlighted);

        // [1] create a HTML
        data.put("pdfmode", false);
        final String html = renderXHTML(data);
        save(htmlFile, html);

        // [2] create a PDF
        data.put("pdfmode", true);
        final String xhtml = renderXHTML(data);
        final ByteBuffer pdf = renderPDF(xhtml, baseURL);
        final byte[] pdfArr =  pdf.array();
        save(pdfFile, pdfArr);

        final PdfReader reader = new PdfReader(pdf.array());
        reader.close();
    }

    private String renderXHTML(final Map<String,Object> data) {
        try {
            final String template = loadCheatSheetTemplate();

            // Need to run the template evaluation in its own thread because
            // it runs a Venice interpreter, that must not conflict with this
            // Venice interpreter.
            final KiraTemplateEvaluator evaluator = new KiraTemplateEvaluator();
            final String xhtml = evaluator.runAsync(() -> evaluator.evaluateKiraTemplate(template, data));

            return xhtml;
        }
        catch(VncException ex) {
            throw new RuntimeException(
                        "Failed to render source code XHTML. \n" +
                        "Venice Callstack: \n" + ex.getCallStackAsString("   "),
                        ex);
        }
        catch(Exception ex) {
            throw new RuntimeException("Failed to render source code XHTML", ex);
        }
    }

    private ByteBuffer renderPDF(final String xhtml, final String baseURL) {
        return PdfRenderer.render(xhtml, baseURL);
    }


    private String loadCheatSheetTemplate() {
        return new ClassPathResource(Venice.class.getPackage(), "docgen/source-code.html")
                        .getResourceAsString("UTF-8");
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

    private static boolean areFontsAvailable(final File fontDir) {
        boolean ok = true;

        File font = new File (fontDir, fontOpenSans);
        if (!font.canRead()) {
            System.out.println("Error: Font " + font.getAbsolutePath() + " is not available!");
            System.out.println("    -> Download from " + fontOpenSansDownload);
            ok = false;
        }

        font = new File (fontDir, fontSourceCodePro);
        if (!font.canRead()) {
            System.out.println("Error: Font " + font.getAbsolutePath() + " is not available!");
            System.out.println("    -> Download from " + fontSourceCodeProDownload);
            ok = false;
        }

        return ok;
    }

    private static String read(final File file) throws Exception {
        return new String(
                Files.readAllBytes(file.toPath()),
                Charset.forName("UTF-8"));
    }

    private static File getUserDir() {
        return new File(System.getProperty("user.dir"));
    }


    private static final String fontOpenSansDownload = "https://fonts.google.com/specimen/Open+Sans";
    private static final String fontSourceCodeProDownload = "https://fonts.google.com/specimen/Source+Sans+Pro";

    private static final String fontOpenSans = "OpenSans-Regular.ttf";
    private static final String fontSourceCodePro = "SourceCodePro-Regular.ttf";

    private final CodeHighlighter codeHighlighter;
}
