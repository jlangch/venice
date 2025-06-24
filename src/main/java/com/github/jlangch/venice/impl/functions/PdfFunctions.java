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

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncByteBuffer;
import com.github.jlangch.venice.impl.types.VncDouble;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncInteger;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncMapEntry;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;
import com.github.jlangch.venice.impl.util.io.ClassPathResource;
import com.github.jlangch.venice.impl.util.kira.KiraTemplateEvaluator;
import com.github.jlangch.venice.impl.util.reflect.ReflectionAccessor;
import com.github.jlangch.venice.javainterop.ILoadPaths;
import com.github.jlangch.venice.util.pdf.HtmlColor;
import com.github.jlangch.venice.util.pdf.PdfRenderer;
import com.github.jlangch.venice.util.pdf.PdfTextStripper;
import com.github.jlangch.venice.util.pdf.PdfUrlExtractor;
import com.github.jlangch.venice.util.pdf.PdfUrlExtractor.Url;
import com.github.jlangch.venice.util.pdf.PdfWatermark;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfReader;


public class PdfFunctions {

    private static final String BLACK = "#000000";


    ///////////////////////////////////////////////////////////////////////////
    // PDF
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction pdf_render =
        new VncFunction(
                "pdf/render",
                VncFunction
                    .meta()
                    .arglists(
                        "(pdf/render xhtml & options)")
                    .doc(
                        "Renders a PDF.\n\n" +
                        "Options: \n\n" +
                        "| :base-url url     | a base url for resources . E.g.: \"classpath:/\"  |\n" +
                        "| :resources resmap | a resource map for dynamic resources              |\n")
                    .examples(
                        "(do                                                              \n" +
                        "   (load-module :kira)                                           \n" +
                        "                                                                 \n" +
                        "   (defn format-ts [t] (time/format t \"yyyy-MM-dd\"))           \n" +
                        "                                                                 \n" +
                        "   ;; define the template                                        \n" +
                        "   (def template (str/strip-indent                               \n" +
                        "      \"\"\"<?xml version=\"1.0\" encoding=\"UTF-8\"?>           \n" +
                        "      <html lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\">  \n" +
                        "        <body>                                                   \n" +
                        "          <div>${ (kira/escape-xml title) }$</div>               \n" +
                        "          <div>${ (kira/escape-xml ts test/format-ts) }$</div>   \n" +
                        "        </body>                                                  \n" +
                        "      </html>                                                    \n" +
                        "      \"\"\"))                                                   \n" +
                        "                                                                 \n" +
                        "   (def data { :title \"Hello, world\"                           \n" +
                        "               :ts (time/local-date 2000 8 1) })                 \n" +
                        "                                                                 \n" +
                        "   (def xhtml (kira/eval template [\"${\" \"}$\"] data))         \n" +
                        "                                                                 \n" +
                        "   (pdf/render xhtml))                                           ",
                        "(pdf/render xhtml :base-url \"classpath:/\")",
                        "(pdf/render xhtml \n" +
                        "            :base-url \"classpath:/\"\n" +
                        "            :resources {\"/chart_1.png\" (chart-create :2018) \n" +
                        "                        \"/chart_2.png\" (chart-create :2019) })")
                    .seeAlso("pdf/text-to-pdf")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                sandboxFunctionCallValidation();

                final VncString xhtml = Coerce.toVncString(args.first());

                final VncMap options = VncHashMap.ofAll(args.slice(1));
                final VncVal baseUrl = options.get(new VncKeyword("base-url"));
                final VncVal resources = options.get(new VncKeyword("resources"));

                // undocumented options
                // be careful with these options, know what you are doing!
                final int dotsPerPixel = getIntOption("dots-per-pixel", options, PdfRenderer.DOTS_PER_PIXEL);
                final float dotsPerPoint = getFloatOption("dots-per-point", options, PdfRenderer.DOTS_PER_POINT);

                return new VncByteBuffer(
                        PdfRenderer.render(
                                xhtml.getValue(),
                                baseUrl == Nil ? null : Coerce.toVncString(baseUrl).getValue(),
                                resources == Nil ? null : mapResources(Coerce.toVncMap(resources)),
                                dotsPerPixel,
                                dotsPerPoint));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction pdf_watermark =
        new VncFunction(
                "pdf/watermark",
                VncFunction
                    .meta()
                    .arglists(
                        "(pdf/watermark pdf options-map)",
                        "(pdf/watermark pdf & options)")
                    .doc(
                        "Adds a watermark text to the pages of a PDF. The passed PDF pdf is " +
                        "a bytebuf. Returns the new PDF as a bytebuf.\n\n" +
                        "Options: \n\n" +
                        "| :text s              | watermark text (string), defaults to \"WATERMARK\" |\n" +
                        "| :font-size n         | font size in pt (double), defaults to 24.0 |\n" +
                        "| :font-char-spacing n | font character spacing (double), defaults to 0.0 |\n" +
                        "| :color s             | font color (HTML color string), defaults to " + BLACK + " |\n" +
                        "| :opacity n           | opacity 0.0 ... 1.0 (double), defaults to 0.4 |\n" +
                        "| :outline-color s     | font outline color (HTML color string), defaults to " + BLACK + " |\n" +
                        "| :outline-opacity n   | outline opacity 0.0 ... 1.0 (double), defaults to 0.8 |\n" +
                        "| :outline-witdh n     | outline width  0.0 ... 10.0 (double), defaults to 0.5 |\n" +
                        "| :angle n             | angle 0.0 ... 360.0 (double), defaults to 45.0 |\n" +
                        "| :over-content b      | print text over the content (boolean), defaults to true |\n" +
                        "| :skip-top-pages n    | the number of top pages to skip (long), defaults to 0 |\n" +
                        "| :skip-bottom-pages n | the number of bottom pages to skip (long), defaults to 0 |\n")
                    .examples(
                        "(pdf/watermark pdf :text \"CONFIDENTIAL\" :font-size 64 :font-char-spacing 10.0)",
                        "(let [watermark { :text \"CONFIDENTIAL\"      \n" +
                        "                  :font-size 64               \n" +
                        "                  :font-char-spacing 10.0 } ] \n" +
                        "   (pdf/watermark pdf watermark))                ")
                    .seeAlso("pdf/merge", "pdf/copy", "pdf/pages")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 2);

                sandboxFunctionCallValidation();

                final VncVal pdf = args.first();

                final VncMap options = Types.isVncMap(args.second())
                                        ? Coerce.toVncMap(args.second())
                                        : VncHashMap.ofAll(args.slice(1));

                final String text           = getStringOption("text", options, "WATERMARK");
                final float fontSize        = getFloatOption("font-size", options, 24.0);
                final float fontCharSpacing = getFloatOption("font-char-spacing", options, 0.0);
                final Color color           = HtmlColor.getColor(getStringOption("color", options, BLACK));
                final float opacity         = getFloatOption("opacity", options, 0.4);
                final Color outlineColor    = HtmlColor.getColor(getStringOption("outline-color", options, BLACK));
                final float outlineOpacity  = getFloatOption("outline-opacity", options, 0.8);
                final float outlineWidth    = getFloatOption("outline-width", options, 0.5);
                final float angle           = getFloatOption("angle", options, 45.0);
                final boolean overContent   = getBooleanOption("over-content", options, true);
                final int skipTopPages      = getIntOption("skip-top-pages", options, 0);
                final int skipBottomPages   = getIntOption("skip-bottom-pages", options, 0);

                if (StringUtil.isBlank(text)) {
                    return pdf;
                }

                return new VncByteBuffer(
                        new PdfWatermark()
                                .addWatermarkText(
                                    Coerce.toVncByteBuffer(pdf).getValue(),
                                    text,
                                    fontSize,
                                    fontCharSpacing,
                                    color,
                                    opacity,
                                    outlineColor,
                                    outlineOpacity,
                                    outlineWidth,
                                    angle,
                                    overContent,
                                    skipTopPages,
                                    skipBottomPages));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction pdf_check_required_libs =
        new VncFunction(
                "pdf/check-required-libs",
                VncFunction
                    .meta()
                    .arglists(
                        "(pdf/check-required-libs)")
                    .doc(
                        "Checks if the 3rd party libraries required for generating PDFs " +
                        "are available. Throws an exception if not.")
                    .examples(
                        "(pdf/check-required-libs)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                // com.github.librepdf:openpdf:xxx
                if (ReflectionAccessor.classExists("com.lowagie.text.Anchor")
                        // com.github.librepdf:pdf-toolbox:xxx
                        && ReflectionAccessor.classExists("com.lowagie.text.pdf.PdfCopy")
                        // org.xhtmlrenderer:flying-saucer-core:xxx
                        && ReflectionAccessor.classExists("org.xhtmlrenderer.DefaultCSSMarker")
                        // org.xhtmlrenderer:flying-saucer-pdf-openpdf:xxx
                        &&ReflectionAccessor.classExists("org.xhtmlrenderer.pdf.AbstractFormField")
                ) {
                    return Nil;
                }

                throw new VncException(
                        "The PDF libraries are not on the classpath! \n" +
                        "\n" +
                        "(do \n" +
                        "  (load-module :maven) \n" +
                        "  (maven/download \"org.xhtmlrenderer:flying-saucer-core:9.3.2\") \n" +
                        "  (maven/download \"org.xhtmlrenderer:lying-saucer-pdf-openpdf:9.3.2\") \n" +
                        "  (maven/download \"com.github.librepdf:openpdf:1.3.35\") \n" +
                        "  (maven/download \"com.github.librepdf:pdf-toolbox:1.3.35\") \n" +
                        "  (maven/download \"com.github.librepdf:openpdf-fonts-extra:1.3.35\")) \n");
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction pdf_available_Q =
        new VncFunction(
                "pdf/available?",
                VncFunction
                    .meta()
                    .arglists("(pdf/available?)")
                    .doc("Checks if the 3rd party libraries required for generating PDFs are available.")
                    .examples("(pdf/available?)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                try {
                    pdf_check_required_libs.apply(VncList.empty());
                    return VncBoolean.True;
                }
                catch(Exception ex) {
                    return VncBoolean.False;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction pdf_merge =
        new VncFunction(
                "pdf/merge",
                VncFunction
                    .meta()
                    .arglists("(pdf/merge pdfs)")
                    .doc(
                        "Merge multiple PDFs into a single PDF. The PDFs are passed " +
                        "as bytebuf. Returns the new PDF as a bytebuf.")
                    .examples(
                        "(pdf/merge pdf1 pdf2)",
                        "(pdf/merge pdf1 pdf2 pdf3)")
                    .seeAlso("pdf/copy", "pdf/pages", "pdf/watermark")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                sandboxFunctionCallValidation();

                if (args.isEmpty()) {
                    throw new VncException("pdf/merge: A PDF list must not be empty");
                }
                else if (args.size() == 1) {
                    return args.first();
                }
                else {
                    try {
                        final ByteArrayOutputStream os = new ByteArrayOutputStream();
                        final Document document = new Document();
                        final PdfCopy copy = new PdfCopy(document, os);

                        document.open();

                         for (VncVal val : args) {
                            if (val != Nil) {
                                final ByteBuffer pdf = Coerce.toVncByteBuffer(val).getValue();

                                final PdfReader reader = new PdfReader(pdf.array());
                                for (int ii=1; ii<=reader.getNumberOfPages(); ii++){
                                    copy.addPage(copy.getImportedPage(reader, ii));
                                }
                                copy.freeReader(reader);
                                reader.close();
                            }
                        }
                        document.close();
                        copy.close();

                        return new VncByteBuffer(os.toByteArray());
                    }
                    catch(Exception ex) {
                        throw new VncException(
                                String.format("pdf/merge: Failed to merge %d PDFs", args.size()),
                                ex);
                    }
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction pdf_copy =
        new VncFunction(
                "pdf/copy",
                VncFunction
                    .meta()
                    .arglists("(pdf/copy pdf & page-nr)")
                    .doc(
                        "Copies pages from a PDF to a new PDF. The PDF is passed " +
                        "as bytebuf. Returns the new PDF as a bytebuf.")
                    .examples(
                        "; copy the first and second page \n" +
                        "(pdf/copy pdf :1 :2)",

                        "; copy the last and second last page \n" +
                        "(pdf/copy pdf :-1 :-2)",

                        "; copy the pages 1, 2, 6-10, and 12 \n" +
                        "(pdf/copy pdf :1 :2 :6-10 :12)")
                    .seeAlso("pdf/merge", "pdf/pages", "pdf/watermark")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                sandboxFunctionCallValidation();

                final ByteBuffer pdf = Coerce.toVncByteBuffer(args.first()).getValue();

                final List<List<Integer>> pages = new ArrayList<>();

                 for (VncVal p : args.rest()) {
                    final String spec = Coerce.toVncKeyword(p).getValue();
                    if (spec.matches("^[0-9]+$")) {
                        pages.add(Arrays.asList(Integer.parseInt(spec)));
                    }
                    else if (spec.matches("^-[0-9]+$")) {
                        pages.add(Arrays.asList(Integer.parseInt(spec)));
                    }
                    else if (spec.matches("^[0-9]+-[0-9]+$")) {
                        final String[] range = spec.split("-");
                        final int start = Integer.parseInt(range[0]);
                        final int end = Integer.parseInt(range[1]);
                        final List<Integer> specs = new ArrayList<>();
                        for(int ii=start; ii<=end; ii++) {
                            specs.add(ii);
                        }
                        pages.add(specs);
                    }
                    else {
                        throw new VncException("pdf/copy: Invalid page specifier " + spec);
                    }
                }

                try {
                    final ByteArrayOutputStream os = new ByteArrayOutputStream();
                    final Document document = new Document();
                    final PdfCopy copy = new PdfCopy(document, os);

                    document.open();

                    final PdfReader reader = new PdfReader(pdf.array());
                    final int numPages = reader.getNumberOfPages();

                    for(List<Integer> specs : pages) {
                        for(int p : specs) {
                            int page = (p < 0) ? numPages + p + 1 : p;
                            if (page > 0 && page <= numPages) {
                                copy.addPage(copy.getImportedPage(reader, page));
                            }
                        }
                    }

                    copy.freeReader(reader);
                    reader.close();

                    document.close();
                    copy.close();

                    return new VncByteBuffer(os.toByteArray());
                }
                catch(Exception ex) {
                    throw new VncException("pdf/copy: Failed to copy PDFs", ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction pdf_pages =
        new VncFunction(
                "pdf/pages",
                VncFunction
                    .meta()
                    .arglists("(pdf/pages pdf)")
                    .doc(
                        "Returns the number of pages of a PDF. The PDF is passed as bytebuf.\n\n" +
                        "Uses the openPDF libraries.")
                    .examples(
                        "(->> (str/lorem-ipsum :paragraphs 30)  \n" +
                        "     (pdf/text-to-pdf)                 \n" +
                        "     (pdf/pages))                        ")
                    .seeAlso("pdf/merge", "pdf/copy", "pdf/watermark")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                sandboxFunctionCallValidation();

                final ByteBuffer pdf = Coerce.toVncByteBuffer(args.first()).getValue();

                try {
                    final PdfReader reader = new PdfReader(pdf.array());
                    final int pages = reader.getNumberOfPages();
                    reader.close();

                    return new VncLong(pages);
                }
                catch(Exception ex) {
                    throw new VncException("pdf/pages: Failed to count the PDF's pages", ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction pdf_text_to_pdf =
        new VncFunction(
                "pdf/text-to-pdf",
                VncFunction
                    .meta()
                    .arglists("(pdf/text-to-pdf text & options)")
                    .doc(
                        "Creates a PDF from simple text. The tool process line-feeds '\\n' " +
                        "and form-feeds. To start a new page just insert a form-feed " +
                        "marker \"<form-feed>\".\n\n" +
                        "Options: \n\n" +
                        "| :font-size n      | font size in pt (double), defaults to 9.0 |\n" +
                        "| :font-weight n    | font weight (0...1000) (long), defaults to 200 |\n" +
                        "| :font-monospace b | if true use monospaced font, defaults to false |\n")
                    .examples(
                        "(->> (pdf/text-to-pdf \"Lorem Ipsum...\")   \n" +
                        "     (io/spit \"text.pdf\"))                  ")
                    .seeAlso("pdf/render", "pdf/to-text")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                sandboxFunctionCallValidation();

                try {
                    final String text = Coerce.toVncString(args.first()).getValue();

                    final VncMap options = VncHashMap.ofAll(args.slice(1));
                    final float fontSize = getFloatOption("font-size", options, 9.0);
                    final int fontWeight = getIntOption("font-weight", options, 200);
                    final boolean fontMonoSpace = getBooleanOption("font-monospace", options, false);

                    final List<List<String>> pages = splitIntoPages(text)
                                                        .stream()
                                                        .map(p -> splitIntoLines(p))
                                                        .collect(Collectors.toList());

                    final Map<String,Object> data = new HashMap<>();
                    data.put("pages", pages);
                    data.put("fontSize", fontSize);
                    data.put("fontWeight", fontWeight);
                    data.put("fontFamiliy", fontMonoSpace ? "Courier" : "Helvetica, Sans-Serif");

                    final String template = loadText2PdfTemplate();

                    // Need to run the template evaluation in its own thread because
                    // it runs a Venice interpreter, that must not conflict with this
                    // Venice interpreter.
                    final KiraTemplateEvaluator evaluator = new KiraTemplateEvaluator();
                    final String xhtml = evaluator.runAsync(() -> evaluator.evaluateKiraTemplate(template, data));

                    return new VncByteBuffer(PdfRenderer.render(xhtml));
                }
                catch(VncException ex) {
                    throw ex;
                }
                catch(Exception ex) {
                    throw new VncException("Failed to render text PDF", ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction pdf_to_text =
        new VncFunction(
                "pdf/to-text",
                VncFunction
                    .meta()
                    .arglists("(pdf/to-text pdf)")
                    .doc(
                        "Extracts the text from a PDF.                           \n\n" +
                        "pdf may be a:                                           \n\n" +
                        " * string file path, e.g: \"/temp/foo.pdf\"             \n" +
                        " * bytebuffer                                           \n" +
                        " * `java.io.File`, e.g: `(io/file \"/temp/foo.pdf\")`   \n" +
                        " * `java.io.InputStream`                                ")
                    .examples(
                        "(-> (pdf/text-to-pdf \"Lorem Ipsum...\")   \n" +
                        "    (pdf/to-text)                          \n" +
                        "    (println))                             ")
                    .seeAlso("pdf/text-to-pdf", "pdf/render")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                sandboxFunctionCallValidation();

                final ILoadPaths loadpaths = ThreadContext.getInterceptor().getLoadPaths();

                final VncVal arg = args.first();

                try {
                    final File file = convertToFile(arg);
                    if (file != null) {
                        final ByteBuffer data = loadpaths.loadBinaryResource(file);
                        final String text = PdfTextStripper.text(data.array());
                        return new VncString(text);
                    }
                    else if (Types.isVncByteBuffer(arg)) {
                         final VncByteBuffer data = (VncByteBuffer)arg;
                            final String text = PdfTextStripper.text(data.getBytes());
                            return new VncString(text);
                    }
                    else if (Types.isVncJavaObject(arg, InputStream.class)) {
                        final InputStream is = Coerce.toVncJavaObject(args.first(), InputStream.class);
                        final String text = PdfTextStripper.text(is);
                        return new VncString(text);
                    }
                    else {
                        throw new VncException(String.format(
                                "Function 'pdf/to-tex' does not allow %s as pdf input",
                                Types.getType(args.first())));
                    }
                }
                catch(VncException ex) {
                    throw ex;
                }
                catch(Exception ex) {
                    throw new VncException("Failed to extract text from PDF", ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction pdf_extract_urls =
        new VncFunction(
                "pdf/extract-urls",
                VncFunction
                    .meta()
                    .arglists("(pdf/extract-urls pdf)")
                    .doc(
                        "Extracts the URLs from a PDF.                           \n\n" +
                        "pdf may be a:                                           \n\n" +
                        " * string file path, e.g: \"/temp/foo.pdf\"             \n" +
                        " * bytebuffer                                           \n" +
                        " * `java.io.File`, e.g: `(io/file \"/temp/foo.pdf\")`   \n" +
                        " * `java.io.InputStream`                                \n\n" +
                        "Returns a list of URLs given as maps with the keys:     \n" +
                        "`:url`, `:url-text`, and `:page-num`")
                    .examples(
                        "(do                                                                      \n" +
                        "   (def xhtml \"\"\"                                                     \n" +
                        "              <?xml version=\"1.0\" encoding=\"UTF-8\"?>                 \n" +
                        "              <html lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\">  \n" +
                        "                <body>                                                   \n" +
                        "                   <a href=\"https://github.com/\">GitHub</a>            \n" +
                        "                   <a href=\"https://duckduckgo.com/\">DuckDuckGo</a>    \n" +
                        "                </body>                                                  \n" +
                        "              </html>                                                    \n" +
                        "              \"\"\")                                                    \n" +
                        "                                                                         \n" +
                        "     (pdf/extract-urls (pdf/render xhtml)))                              ")
                    .seeAlso("pdf/render")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                sandboxFunctionCallValidation();

                final ILoadPaths loadpaths = ThreadContext.getInterceptor().getLoadPaths();

                final VncVal arg = args.first();

                try {
                    final File file = convertToFile(arg);
                    if (file != null) {
                        final ByteBuffer data = loadpaths.loadBinaryResource(file);
                        return mapPdfUrls(PdfUrlExtractor.extract(data.array()));
                   }
                    else if (Types.isVncByteBuffer(arg)) {
                         final VncByteBuffer data = (VncByteBuffer)arg;
                         return mapPdfUrls(PdfUrlExtractor.extract(data.getBytes()));
                   }
                    else if (Types.isVncJavaObject(arg, InputStream.class)) {
                        final InputStream is = Coerce.toVncJavaObject(args.first(), InputStream.class);
                        return mapPdfUrls(PdfUrlExtractor.extract(is));
                    }
                    else {
                        throw new VncException(String.format(
                                "Function 'pdf/to-tex' does not allow %s as pdf input",
                                Types.getType(args.first())));
                    }
                }
                catch(VncException ex) {
                    throw ex;
                }
                catch(Exception ex) {
                    throw new VncException("Failed to extract text from PDF", ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction pdf_page_to_image =
        new VncFunction(
                "pdf/page-to-image",
                VncFunction
                    .meta()
                    .arglists(
                        "(pdf/page-to-image pdf page-nr)",
                        "(pdf/page-to-image pdf page-nr dpi)")
                    .doc(
                        "Converts a page from the PDF to an image buffer. \n\n" +
                        "The passed PDF pdf is a bytebuf. Returns the image buffer as a" +
                        ":java.awt.image.BufferedImage that can be further " +
                        "processed or saved with the  `:images` module. ")
                    .seeAlso("pdf/render", "pdf/to-text")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2, 3);

                sandboxFunctionCallValidation();

                final ByteBuffer pdf = Coerce.toVncByteBuffer(args.first()).getValue();
                final long pageNr = Coerce.toVncLong(args.second()).toJavaLong();

                Double dpi = null;
                if (args.size() == 3) {
                    if (Types.isVncLong(args.third())) {
                        dpi = Coerce.toVncLong(args.third()).toJavaDouble();
                    }
                    else if (Types.isVncDouble(args.third())) {
                        dpi = Coerce.toVncDouble(args.third()).toJavaDouble();
                    }
                    else {
                        throw new VncException(String.format(
                                "Function 'page-to-image' does not allow %s as dpi value",
                                Types.getType(args.third())));
                    }
                }

                try(PDDocument doc = Loader.loadPDF(pdf.array())) {
                    final PDFRenderer pdfRenderer = new PDFRenderer(doc);

                    final BufferedImage img = dpi != null
                                                ? pdfRenderer.renderImage((int)pageNr, dpi.floatValue())
                                                : pdfRenderer.renderImage((int)pageNr);

                    return new VncJavaObject(img);
                }
                catch(VncException ex) {
                    throw ex;
                }
                catch(Exception ex) {
                    throw new VncException("Failed to convert PDF page to an image", ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

//    public static VncFunction pdf_extract_images =
//        new VncFunction(
//                "pdf/extract-images",
//                VncFunction
//                    .meta()
//                    .arglists("(pdf/extract-images pdf page-nr)")
//                    .doc(
//                        "Extracts the images from the PDF page. \n\n" +
//                        "The passed PDF pdf is a bytebuf. Returns the images as a" +
//                        "list of :java.awt.image.BufferedImage that can be further " +
//                        "processed or saved with the `:images` module. ")
//                    .seeAlso("pdf/render", "pdf/to-text")
//                    .build()
//        ) {
//            @Override
//            public VncVal apply(final VncList args) {
//                ArityExceptions.assertArity(this, args, 2);
//
//                sandboxFunctionCallValidation();
//
//                final ByteBuffer pdf = Coerce.toVncByteBuffer(args.first()).getValue();
//                final long pageNr = Coerce.toVncLong(args.second()).toJavaLong();
//
//                try(PDDocument doc = Loader.loadPDF(pdf.array())) {
//                    final PDPage page = doc.getPage((int)pageNr);
//
//                    final List<VncVal> images = new ArrayList<>();
//
//                    final PDFStreamEngine engine = new PDFStreamEngine() {
//                        @Override
//                        protected void processOperator(Operator operator, List<COSBase> operands) throws IOException{
//                            String operation = operator.getName();
//
//                            if ("Do".equals(operation)) {
//                                COSName objectName = (COSName)operands.get(0);
//                                PDXObject pdxObject = getResources().getXObject(objectName);
//
//                                if (pdxObject instanceof PDImageXObject) {
//                                    // Image
//                                    PDImageXObject img = (PDImageXObject) pdxObject;
//                                    BufferedImage bImg = img.getImage();
//
//                                    images.add(new VncJavaObject(bImg));
//
//                                }
//                                else if (pdxObject instanceof PDFormXObject) {
//                                    PDFormXObject form = (PDFormXObject) pdxObject;
//                                    showForm(form);
//                                }
//                            }
//                            else {
//                                super.processOperator(operator, operands);
//                            }
//                        }
//                    };
//
//                    engine.processPage(page);
//
//                    return VncList.ofColl(images);
//                }
//                catch(VncException ex) {
//                    throw ex;
//                }
//                catch(Exception ex) {
//                    throw new VncException("Failed to extract images from PDF", ex);
//                }
//            }
//
//            private static final long serialVersionUID = -1848883965231344442L;
//        };

    public static VncFunction pdf_page_count =
        new VncFunction(
                "pdf/page-count",
                VncFunction
                    .meta()
                    .arglists("(pdf/page-count pdf)")
                    .doc(
                       "Returns the number of pages in a PDF.\n\n" +
                       "Uses the pdfbox libraries.")
                    .seeAlso("pdf/render", "pdf/to-text")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                sandboxFunctionCallValidation();

                final ByteBuffer pdf = Coerce.toVncByteBuffer(args.first()).getValue();

                try(PDDocument doc = Loader.loadPDF(pdf.array())) {
                    return new VncLong(doc.getNumberOfPages());
                }
                catch(VncException ex) {
                    throw ex;
                }
                catch(Exception ex) {
                    throw new VncException("Failed to get the number of PDF pages", ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    private static Map<String,ByteBuffer> mapResources(final VncMap resourceMap) {
        final Map<String,ByteBuffer> resources = new HashMap<>();
        for (VncMapEntry entry : resourceMap.entries()) {
            resources.put(
                Coerce.toVncString(entry.getKey()).getValue(),
                Coerce.toVncByteBuffer(entry.getValue()).getValue());
        }
        return resources;
    }

    private static String loadText2PdfTemplate() {
        return new ClassPathResource("com/github/jlangch/venice/templates/text-2-pdf.kira")
                        .getResourceAsString();
    }

    private static List<String> splitIntoPages(final String text) {
        final List<String> pages = new ArrayList<>();

        if (StringUtil.isNotEmpty(text)) {
            int lastPos = 0;
            while(lastPos < text.length()) {
                int pos = text.indexOf("\n<form-feed>\n", lastPos);
                if (pos >= 0) {
                    pages.add(text.substring(lastPos, pos));
                    lastPos = pos + "\n<form-feed>\n".length();
                }
                else {
                    pos = text.indexOf("<form-feed>", lastPos);
                    if (pos >= 0) {
                        pages.add(text.substring(lastPos, pos));
                        lastPos = pos + "<form-feed>".length();
                    }
                    else {
                        pages.add(text.substring(lastPos));
                        break;
                    }
                }
            }
        }

        return pages;
    }

    private static List<String> splitIntoLines(final String text) {
        return StringUtil
                    .splitIntoLines(text)
                    .stream()
                    .map(s -> StringUtil.isBlank(s) ? "\u2002" : s)
                    .map(s -> StringUtil.replaceLeadingSpaces(s, '\u00A0'))
                    .collect(Collectors.toList());
    }

    private static String getStringOption(
            final String optName,
            final VncMap options,
            final String defaultVal
    ) {
        final VncVal val = options.get(new VncKeyword(optName), new VncString(defaultVal));
        if (Types.isVncString(val)) {
            return ((VncString)val).getValue();
        }
        else {
            throw new VncException(
                    "Invalid '" + optName + "' option type " + Types.getType(val)
                        + ". Expected a string!");
        }
    }

    private static float getFloatOption(
            final String optName,
            final VncMap options,
            final double defaultVal
    ) {
        final VncVal val = options.get(new VncKeyword(optName), new VncDouble(defaultVal));
        if (Types.isVncLong(val)) {
            return ((VncLong)val).getValue().floatValue();
        }
        else if (Types.isVncInteger(val)) {
            return ((VncInteger)val).getValue().floatValue();
        }
        else if (Types.isVncDouble(val)) {
            return ((VncDouble)val).toJavaFloat();
        }
        else {
            throw new VncException(
                    "Invalid '" + optName + "' option type " + Types.getType(val)
                        + ". Expected a double!");
        }
    }

    private static int getIntOption(
            final String optName,
            final VncMap options,
            final long defaultVal
    ) {
        final VncVal val = options.get(new VncKeyword(optName), new VncLong(defaultVal));
        if (Types.isVncLong(val)) {
            return ((VncLong)val).getIntValue();
        }
        else if (Types.isVncInteger(val)) {
            return ((VncInteger)val).getValue();
        }
        else if (Types.isVncDouble(val)) {
            return ((VncDouble)val).getValue().intValue();
        }
        else {
            throw new VncException(
                    "Invalid '" + optName + "' option type " + Types.getType(val)
                        + ". Expected a long!");
        }
    }

    private static boolean getBooleanOption(
            final String optName,
            final VncMap options,
            final boolean defaultVal
    ) {
        final VncVal val = options.get(new VncKeyword(optName), VncBoolean.of(defaultVal));
        if (Types.isVncBoolean(val)) {
            return ((VncBoolean)val).getValue();
        }
        else {
            throw new VncException(
                    "Invalid '" + optName + "' option type " + Types.getType(val)
                        + ". Expected a boolean!");
        }
    }

    private static File convertToFile(final VncVal f) {
        if (Types.isVncString(f)) {
            return new File(((VncString)f).getValue());
        }
        else if (Types.isVncJavaObject(f, File.class)) {
            return Coerce.toVncJavaObject(f, File.class);
        }
        else if (Types.isVncJavaObject(f, Path.class)) {
            return Coerce.toVncJavaObject(f, Path.class).toFile();
        }
        else {
            return null;
        }
    }

    private static VncList mapPdfUrls(final List<Url> urls) {
        return VncList.ofList(
                urls.stream()
                    .map(u -> VncHashMap.of(
                                new VncKeyword("url"),      new VncString(u.getUrl()),
                                new VncKeyword("url-text"), new VncString(u.getUrlText()),
                                new VncKeyword("page-num"), new VncLong(u.getPageNum())))
                    .collect(Collectors.toList()));
    }


    ///////////////////////////////////////////////////////////////////////////
    // types_ns is namespace of type functions
    ///////////////////////////////////////////////////////////////////////////

    public static final Map<VncVal, VncVal> ns =
            new SymbolMapBuilder()
                    .add(pdf_check_required_libs)
                    .add(pdf_available_Q)
                    .add(pdf_render)
                    .add(pdf_watermark)
                    .add(pdf_merge)
                    .add(pdf_copy)
                    .add(pdf_pages)
                    .add(pdf_text_to_pdf)
                    .add(pdf_to_text)
                    .add(pdf_extract_urls)
                    .add(pdf_page_to_image)
//                    .add(pdf_extract_images)
                    .add(pdf_page_count)
                    .toMap();

}
