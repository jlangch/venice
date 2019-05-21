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
package com.github.jlangch.venice.util;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.github.jlangch.venice.VncException;


public class XMLHandler extends DefaultHandler {

	public XMLHandler(final IXMLHandler h) {
		this.h = h;
	}


	@Override
	public void setDocumentLocator(final Locator locator) {
		h.setDocumentLocator(locator);
	}

	@Override
	public void startDocument() throws SAXException {
		try {
			h.startDocument();
		}
		catch(VncException ex) {
			throw new SAXException(ex.printVeniceStackTraceToString(),ex);
		}
	}

	@Override
	public void endDocument() throws SAXException {
		try {
			h.endDocument();
		}
		catch(VncException ex) {
			throw new SAXException(ex.printVeniceStackTraceToString(),ex);
		}
	}

	@Override
	public void startPrefixMapping(
			final String prefix, 
			final String uri
	) throws SAXException {
		try {
			h.startPrefixMapping(prefix, uri);
		}
		catch(VncException ex) {
			throw new SAXException(ex.printVeniceStackTraceToString(),ex);
		}
	}

	@Override
	public void endPrefixMapping(
			final String prefix
	) throws SAXException {
		try {
			h.endPrefixMapping(prefix);
		}
		catch(VncException ex) {
			throw new SAXException(ex.printVeniceStackTraceToString(),ex);
		}
	}

	@Override
	public void startElement(
			final String uri, 
			final String localName, 
			final String qName, 
			final Attributes atts
	) throws SAXException {
		try {
			h.startElement(uri, localName, qName, atts);
		}
		catch(VncException ex) {
			throw new SAXException(ex.printVeniceStackTraceToString(),ex);
		}
	}

	@Override
	public void endElement(
			final String uri, 
			final String localName, 
			final String qName
	) throws SAXException {
		try {
			h.endElement(uri, localName, qName);
		}
		catch(VncException ex) {
			throw new SAXException(ex.printVeniceStackTraceToString(),ex);
		}
	}

	@Override
	public void characters(
			final char ch[], 
			final int start, 
			final int length
	) throws SAXException {
		try {
			h.characters(new String(ch).substring(start, start + length));
		}
		catch(VncException ex) {
			throw new SAXException(ex.printVeniceStackTraceToString(),ex);
		}
	}

	@Override
	public void ignorableWhitespace(
			final char ch[], 
			final int start, 
			final int length
	) throws SAXException {
		try {
			h.ignorableWhitespace(new String(ch).substring(start, start + length));
		}
		catch(VncException ex) {
			throw new SAXException(ex.printVeniceStackTraceToString(),ex);
		}
	}

	@Override
	public void processingInstruction(
			final String target, 
			final String data
	) throws SAXException {
		try {
			h.processingInstruction(target, data);
		}
		catch(VncException ex) {
			throw new SAXException(ex.printVeniceStackTraceToString(),ex);
		}
	}

	@Override
	public void skippedEntity(
			final String name
	) throws SAXException {
		try {
			h.skippedEntity(name);
		}
		catch(VncException ex) {
			throw new SAXException(ex.printVeniceStackTraceToString(),ex);
		}
	}
	
	
	private final IXMLHandler h;
}