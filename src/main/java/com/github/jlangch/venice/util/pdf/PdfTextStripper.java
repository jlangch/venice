/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2023 Venice
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
import java.io.InputStream;

import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import com.github.jlangch.venice.impl.util.io.IOStreamUtil;

import io.github.jonathanlink.PDFLayoutTextStripper;


public class PdfTextStripper {

	public static String text(final File pdf) {
		try {
			final PDFParser pdfParser = new PDFParser(new RandomAccessFile(pdf, "r"));
			pdfParser.parse();
			final PDDocument pdDocument = new PDDocument(pdfParser.getDocument());
			final PDFTextStripper pdfTextStripper = new PDFLayoutTextStripper();
			return pdfTextStripper.getText(pdDocument);
		}
		catch (Exception ex) {
			throw new RuntimeException("Failed to strip text from PDF file", ex);
		}
	}

	public static String text(final InputStream is) {
		try {
			return text(IOStreamUtil.copyIStoByteArray(is));
		}
		catch (Exception ex) {
			throw new RuntimeException("Failed to strip text from PDF input stream", ex);
		}
	}

	public static String text(final byte[] pdf) {
		try {
			final PDFParser pdfParser = new PDFParser(new RandomAccessBuffer(pdf));
			pdfParser.parse();
			final PDDocument pdDocument = new PDDocument(pdfParser.getDocument());
			final PDFTextStripper pdfTextStripper = new PDFLayoutTextStripper();
			return pdfTextStripper.getText(pdDocument);
		}
		catch (Exception ex) {
			throw new RuntimeException("Failed to strip text from PDF byte buffer", ex);
		}
	}
}