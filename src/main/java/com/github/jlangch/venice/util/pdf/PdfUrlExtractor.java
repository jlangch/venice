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
package com.github.jlangch.venice.util.pdf;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.io.IOStreamUtil;


public class PdfUrlExtractor {

    // https://svn.apache.org/repos/asf/pdfbox/trunk/examples/src/main/java/org/apache/pdfbox/examples/pdmodel/PrintURLs.java

    public static List<Url> extract(final File pdf) {
        try {
            final RandomAccessReadBufferedFile raf = new RandomAccessReadBufferedFile(pdf);
            try {
                return extract(new PDFParser(raf));
            }
            finally {
                raf.close();
            }
        }
        catch (Exception ex) {
            throw new RuntimeException("Failed to extract URLs from PDF file", ex);
        }
    }

    public static List<Url> extract(final InputStream is) {
        try {
            return extract(IOStreamUtil.copyIStoByteArray(is));
        }
        catch (Exception ex) {
            throw new RuntimeException("Failed to extract URLs from PDF input stream", ex);
        }
    }

    public static List<Url> extract(final byte[] pdf) {
        try {
            return extract(new PDFParser(new RandomAccessReadBuffer(pdf)));
        }
        catch (Exception ex) {
            throw new RuntimeException("Failed to extract URLs from PDF byte buffer", ex);
        }
    }

    private static List<Url> extract(final PDFParser pdfParser) throws Exception {
        try(final PDDocument pdDocument = pdfParser.parse()) {
            final ArrayList<Url> urls = new ArrayList<>();

            int pageNum = 0;
            for (PDPage page : pdDocument.getPages()) {
                pageNum++;

                final PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                final List<PDAnnotation> annotations = page.getAnnotations();

                //first setup text extraction regions
                for (int j=0; j<annotations.size(); j++ ) {
                    final PDAnnotation annot = annotations.get(j);

                    if (annot instanceof PDAnnotationLink) {
                        final PDRectangle rect = annot.getRectangle();
                        //need to reposition link rectangle to match text space
                        float x = rect.getLowerLeftX();
                        float y = rect.getUpperRightY();
                        float width = rect.getWidth();
                        float height = rect.getHeight();
                        int rotation = page.getRotation();
                        if (rotation == 0) {
                            final PDRectangle pageSize = page.getMediaBox();
                            // area stripper uses java coordinates, not PDF coordinates
                            y = pageSize.getHeight() - y;
                        }
                        else {
                            // do nothing
                        }

                        final Rectangle2D.Float awtRect = new Rectangle2D.Float(x, y, width, height);
                        stripper.addRegion(String.valueOf(j), awtRect);
                    }
                }

                stripper.extractRegions(page);

                for (int j=0; j<annotations.size(); j++ ) {
                    final PDAnnotation annot = annotations.get(j);

                    if (annot instanceof PDAnnotationLink) {
                        final PDAnnotationLink link = (PDAnnotationLink)annot;
                        final PDAction action = link.getAction();

                        if (action instanceof PDActionURI) {
                            final PDActionURI uri = (PDActionURI)action;
                            final String url = uri.getURI();
                            final String urlText = stripper.getTextForRegion(String.valueOf(j));

                            urls.add(new Url(url, StringUtil.trimToEmpty(urlText), pageNum));
                        }
                    }
                }
            }

            return urls;
        }
    }


    public static class Url {
        public Url(
                final String url,
                final String urlText,
                final int pageNum
        ) {
            this.url = url;
            this.urlText = urlText;
            this.pageNum = pageNum;
        }


        public String getUrl() {
            return url;
        }

        public String getUrlText() {
            return urlText;
        }

        public int getPageNum() {
            return pageNum;
        }


        private final String url;
        private final String urlText;
        private final int pageNum;
    }
}
