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

	public static byte[] renderCheatSheet(final String xhtml) {
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {			
			final ITextRenderer renderer = new ITextRenderer(DOTS_PER_POINT, DOTS_PER_PIXEL);

			renderer.setDocumentFromString(xhtml, "classpath:///");
			renderer.layout();
			renderer.createPDF(os);
			os.flush();	

			return os.toByteArray();
		}
		catch(Exception ex) {
			throw new RuntimeException("Failed to render PDF cheatsheet.", ex);
		}		
	}

    private static final int DOTS_PER_PIXEL = 20;
    private static final float DOTS_PER_POINT = (float)DOTS_PER_PIXEL * 96f / 72f;
}
