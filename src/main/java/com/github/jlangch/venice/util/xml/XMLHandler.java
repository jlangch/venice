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
package com.github.jlangch.venice.util.xml;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.github.jlangch.venice.VncException;


/**
 * The {@code XMLHandler} serves as an adapater to hook into Java XML processing.
 *
 * <p>Venice can not extend Java classes. The {@code XMLHandler} allows Venice to
 * pass a dynamic proxy for the interface {@code IXMLHandler}.
 *
 * @see IXMLHandler
 * @see XMLUtil
 */
public class XMLHandler extends DefaultHandler {

    public XMLHandler(final IXMLHandler handler) {
        this.handler = handler;
    }


    @Override
    public void setDocumentLocator(final Locator locator) {
        handler.setDocumentLocator(locator);
    }

    @Override
    public void startDocument() throws SAXException {
        try {
            handler.startDocument();
        }
        catch(VncException ex) {
            throw new SAXException(ex.printVeniceStackTraceToString(),ex);
        }
    }

    @Override
    public void endDocument() throws SAXException {
        try {
            handler.endDocument();
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
            handler.startPrefixMapping(prefix, uri);
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
            handler.endPrefixMapping(prefix);
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
            final Attributes attrs
    ) throws SAXException {
        try {
            // wrap org.xml.sax.Attributes to allow reflective access
            // without "illegal reflective access operations" warnings on Java 9+
            handler.startElement(uri, localName, qName, new XmlAttributesWrapper(attrs));
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
            handler.endElement(uri, localName, qName);
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
            handler.characters(new String(ch).substring(start, start + length));
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
            handler.ignorableWhitespace(new String(ch).substring(start, start + length));
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
            handler.processingInstruction(target, data);
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
            handler.skippedEntity(name);
        }
        catch(VncException ex) {
            throw new SAXException(ex.printVeniceStackTraceToString(),ex);
        }
    }


    private final IXMLHandler handler;
}
