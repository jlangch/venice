/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2014-2018 Venice
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
package com.github.jlangch.venice.impl.util.xml;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class XMLHandler extends DefaultHandler {

	public XMLHandler(final ContentHandler h) {
		this.h = h;
	}


	@Override
	public void setDocumentLocator(final Locator locator) {
		h.setDocumentLocator(locator);
	}

	@Override
	public void startDocument() throws SAXException {
		h.startDocument();
	}

	@Override
	public void endDocument() throws SAXException {
		h.endDocument();
	}

	@Override
	public void startPrefixMapping(
			final String prefix, 
			final String uri
	) throws SAXException {
		h.startPrefixMapping(prefix, uri);
	}

	@Override
	public void endPrefixMapping(
			final String prefix
	) throws SAXException {
		h.endPrefixMapping(prefix);
	}

	@Override
	public void startElement(
			final String uri, 
			final String localName, 
			final String qName, 
			final Attributes atts
	) throws SAXException {
		h.startElement(uri, localName, qName, atts);
	}

	@Override
	public void endElement(
			final String uri, 
			final String localName, 
			final String qName
	) throws SAXException {
		h.endElement(uri, localName, qName);
	}

	@Override
	public void characters(
			final char ch[], 
			final int start, 
			final int length
	) throws SAXException {
		h.characters(ch, start, length);
	}

	@Override
	public void ignorableWhitespace(
			final char ch[], 
			final int start, 
			final int length
	) throws SAXException {
		h.ignorableWhitespace(ch, start, length);
	}

	@Override
	public void processingInstruction(
			final String target, 
			final String data
	) throws SAXException {
		h.processingInstruction(target, data);
	}

	@Override
	public void skippedEntity(
			final String name
	) throws SAXException {
		h.skippedEntity(name);
	}


	private final ContentHandler h;
}