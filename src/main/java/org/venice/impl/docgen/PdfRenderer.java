/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2018 Venice
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
package org.venice.impl.docgen;

import java.io.ByteArrayOutputStream;

import org.xhtmlrenderer.pdf.ITextRenderer;


public class PdfRenderer {

	public byte[] renderPDF(
			final String documentName,
			final String xhtml
	) {
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			
			final ITextRenderer renderer = new ITextRenderer(DOTS_PER_POINT, DOTS_PER_PIXEL);

			renderer.setDocumentFromString(xhtml, "classpath:///");
//			final org.w3c.dom.Document doc = parseXHTML(xhtml);
//			renderer.setDocument(doc, "classpath:///");
			renderer.layout();
			renderer.createPDF(os);
			os.flush();	

			return os.toByteArray();
		}
		catch(Exception ex) {
			throw new RuntimeException(
					"Failed to render PDF report '" + documentName + "'.", ex);
		}		
	}


//	private org.w3c.dom.Document parseXHTML(final String xhtml) throws Exception {
//		try {
//			final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//			return builder.parse(new ByteArrayInputStream(xhtml.getBytes("UTF-8")));
//		}
//		catch(SAXParseException ex) {
//			// Get an extract of the failure to simplify error analysis (+/- 10 lines)
//			throw new RuntimeException(
//					String.format(
//							"Invalid XHTML template regarding XML validation.\nXML parser error: %s",
//							ex.getMessage()), 
//					ex);
//		}		
//		catch(Exception ex) {
//			throw new RuntimeException("Failed to parse XHTML template", ex);
//		}
//	}

	// These two defaults combine to produce an effective resolution of 96 px to
    // the inch
    private static final int DOTS_PER_PIXEL = 20;
    private static final float DOTS_PER_POINT = (float)DOTS_PER_PIXEL * 96f / 72f;
}
