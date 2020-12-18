/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2021 Venice
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

import java.util.Enumeration;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.pdf.DefaultPDFCreationListener;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfString;
import com.lowagie.text.pdf.PdfWriter;


/**
 * PDF meta data creation listener
 */
public class PdfMetaDataCreationListener extends DefaultPDFCreationListener {

	public PdfMetaDataCreationListener() {
	}

	public PdfMetaDataCreationListener parseMetaTags(final Document sourceXHTML) {
		final Element headTag = (Element)sourceXHTML.getDocumentElement()
													.getElementsByTagName("head")
													.item(0);
		if (headTag == null) {
			return this;
		}
		
		final NodeList metaTags = headTag.getElementsByTagName("meta");
		if (metaTags == null) {
			return this;
		}

		for (int ii=0; ii<metaTags.getLength(); ++ii) {
			final Element tag = (Element)metaTags.item(ii);
			final String name = tag.getAttribute("name");
			final String content = tag.getAttribute("content");
			if (!name.isEmpty() && !content.isEmpty()) {
				headMetaTags.setProperty(name, content);
			}
		}

		// No title meta tag given --> take it from title tag
		if (headMetaTags.getProperty("title") == null) {
			final Element titleTag = (Element)headTag.getElementsByTagName("title").item(0);
			if (titleTag != null) {
				headMetaTags.setProperty("title", titleTag.getTextContent());
			}
		}
		
		return this;
	}

	@Override
	public void preOpen(final ITextRenderer iTextRenderer) {
		final Enumeration<?> e = headMetaTags.propertyNames();
        
		while (e.hasMoreElements()) {
			final String key = (String)e.nextElement();
			final PdfString val = new PdfString(headMetaTags.getProperty(key), PdfObject.TEXT_UNICODE);
			iTextRenderer.getWriter().setViewerPreferences(PdfWriter.DisplayDocTitle);

			switch(key) {
				case "title":
					iTextRenderer.getWriter().getInfo().put(PdfName.TITLE, val);
					break;
				case "author":
					iTextRenderer.getWriter().getInfo().put(PdfName.AUTHOR, val);
					break;
				case "subject":
					iTextRenderer.getWriter().getInfo().put(PdfName.SUBJECT, val);
					break;
				case "creator":
					iTextRenderer.getWriter().getInfo().put(PdfName.CREATOR, val);
					break;
				case "description":
					iTextRenderer.getWriter().getInfo().put(PdfName.DESC, val);
					break;
				case "keywords":
					iTextRenderer.getWriter().getInfo().put(PdfName.KEYWORDS, val);
					break;
				default:
					/* This allows for arbitrary meta tags. */
					iTextRenderer.getWriter().getInfo().put(new PdfName(key), val);
					break;
			}
		}
	}


	final Properties headMetaTags = new Properties();
}
