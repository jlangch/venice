/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2022 Venice
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


/**
 * Defines a {@code SAX Parser} content handler. Venice scripts can
 * dynamically proxify this handler to process SAX parser events.
 *
 * <pre>
 * (proxify :IXMLHandler
 *          { :startElement (fn [uri local-name q-name attributes] nil)
 *            :endElement (fn [uri local-name q-name] nil)
 *            :characters (fn [chars] nil)
 *            :setDocumentLocator (fn [locator] nil)
 *            :startDocument (fn [] nil)
 *            :endDocument (fn [] nil)
 *            :startPrefixMapping (fn [prefix uri] nil)
 *            :endPrefixMapping (fn [prefix] nil)
 *            :ignorableWhitespace (fn [chars] nil)
 *            :processingInstruction (fn [target data] nil)
 *            :skippedEntity (fn [name] nil) })
 * </pre>
 *
 * @see XMLHandler
 * @see XMLUtil
 */
public interface IXMLHandler {

    public void setDocumentLocator(Locator locator);

    public void startDocument() throws SAXException;

    public void endDocument() throws SAXException;

    public void startPrefixMapping(String prefix, String uri)
        throws SAXException;

    public void endPrefixMapping(String prefix)
        throws SAXException;

    public void startElement(String uri, String localName, String qName, Attributes atts)
        throws SAXException;

    public void endElement(String uri, String localName, String qName)
        throws SAXException;

    void characters(String chars)
        throws SAXException;

    void ignorableWhitespace(String chars)
        throws SAXException;

    void processingInstruction(String target, String data)
        throws SAXException;

    void skippedEntity(String name)
        throws SAXException;

}
