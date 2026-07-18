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

import java.io.File;
import java.io.OutputStream;
import java.util.Objects;

import org.apache.commons.io.output.ByteArrayOutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfWriter;


public class ImageToPdf {

    public static byte[] toPDF(final File img) {
        Objects.requireNonNull(img);

        try {
            final ByteArrayOutputStream os = new ByteArrayOutputStream();

            final Document document = new Document();

            final PdfWriter writer = PdfWriter.getInstance(document, os);
            writer.open();
            document.open();
            document.add(Image.getInstance(img.getAbsolutePath()));
            document.close();
            writer.flush();
            writer.close();

            return os.toByteArray();
        }
        catch (Exception ex) {
            throw new RuntimeException("Failed to convert the image to a PDF", ex);
        }
    }

    public static byte[] toPDF(final byte[] img) {
        Objects.requireNonNull(img);

        try {
            final ByteArrayOutputStream os = new ByteArrayOutputStream();

            final Document document = new Document();

            final PdfWriter writer = PdfWriter.getInstance(document, os);
            writer.open();
            document.open();
            document.add(Image.getInstance(img));
            document.close();
            writer.flush();
            writer.close();

            return os.toByteArray();
        }
        catch (Exception ex) {
            throw new RuntimeException("Failed to convert the image to a PDF", ex);
        }
    }

    public static void toPDF(final byte[] img, final OutputStream os) {
        Objects.requireNonNull(img);
        Objects.requireNonNull(os);

        try {
            final Document document = new Document();

            final PdfWriter writer = PdfWriter.getInstance(document, os);
            writer.open();
            document.open();
            document.add(Image.getInstance(img));
            document.close();
            writer.flush();
            writer.close();
        }
        catch (Exception ex) {
            throw new RuntimeException("Failed to convert the image to a PDF", ex);
        }
    }

    public static void toPDF(final File img, final OutputStream os) {
        Objects.requireNonNull(img);
        Objects.requireNonNull(os);

        try {
            final Document document = new Document();

            final PdfWriter writer = PdfWriter.getInstance(document, os);
            writer.open();
            document.open();
            document.add(Image.getInstance(img.getAbsolutePath()));
            document.close();
            writer.flush();
            writer.close();
        }
        catch (Exception ex) {
            throw new RuntimeException("Failed to convert the image to a PDF", ex);
        }
    }

}
