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
 * 
 * @author juerg
 */
public class PdfMetaDataCreationListener extends DefaultPDFCreationListener {

	public PdfMetaDataCreationListener() {
	}

	public void parseMetaTags(final Document sourceXHTML) {
		Element headTag = (Element)sourceXHTML.getDocumentElement()
										.getElementsByTagName("head")
										.item(0);
		NodeList metaTags = headTag.getElementsByTagName("meta");

		for (int ii=0; ii<metaTags.getLength(); ++ii) {
			Element tag = (Element)metaTags.item(ii);
			String name = tag.getAttribute("name");
			String content = tag.getAttribute("content");
			if (!name.isEmpty() && !content.isEmpty()) {
				this.headMetaTags.setProperty(name, content);
			}
		}

		// No title meta tag given --> take it from title tag
		if (this.headMetaTags.getProperty("title") == null) {
			Element titleTag = (Element)headTag.getElementsByTagName("title").item(0);
			this.headMetaTags.setProperty("title", titleTag.getTextContent());
		}
	}

	@Override
	public void preOpen(final ITextRenderer iTextRenderer) {
		Enumeration<?> e = this.headMetaTags.propertyNames();
        
		while (e.hasMoreElements()) {
			String key = (String)e.nextElement();
			PdfString val = new PdfString(this.headMetaTags.getProperty(key), PdfObject.TEXT_UNICODE);
			iTextRenderer.getWriter().setViewerPreferences(PdfWriter.DisplayDocTitle);

			if ("title".equals(key)) {
				iTextRenderer.getWriter().getInfo().put(PdfName.TITLE, val);
			} 
			else if ("author".equals(key)) {
				iTextRenderer.getWriter().getInfo().put(PdfName.AUTHOR, val);
			} 
			else if ("subject".equals(key)) {
				iTextRenderer.getWriter().getInfo().put(PdfName.SUBJECT, val);
			} 
			else if ("creator".equals(key)) {
				iTextRenderer.getWriter().getInfo().put(PdfName.CREATOR, val);
			} 
			else if ("description".equals(key)) {
				iTextRenderer.getWriter().getInfo().put(PdfName.DESC, val);
			} 
			else if ("keywords".equals(key)) {
				iTextRenderer.getWriter().getInfo().put(PdfName.KEYWORDS, val);
			} 
			else {
				/* This line allows for arbitrary meta tags. */
				iTextRenderer.getWriter().getInfo().put(new PdfName(key), val);
			}
		}
	}


	final Properties headMetaTags = new Properties();
}
