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
package com.github.jlangch.venice.util;

import org.xml.sax.Attributes;

/**
 * Wraps {@link org.xml.sax.Attributes} to allow reflective access
 * without "illegal reflective access operations" warnings on Java 9+
 */
public class XmlAttributesWrapper implements Attributes {

    public XmlAttributesWrapper(final Attributes attrs) {
        this.attrs = attrs;
    }

    @Override
    public int getLength() {
        return attrs.getLength();
    }

    @Override
    public String getURI(int index) {
        return attrs.getURI(index);
    }

    @Override
    public String getLocalName(int index) {
        return attrs.getLocalName(index);
    }

    @Override
    public String getQName(int index) {
        return attrs.getQName(index);
    }

    @Override
    public String getType(int index) {
        return attrs.getValue(index);
    }

    @Override
    public String getValue(int index) {
        return attrs.getValue(index);
    }

    @Override
    public int getIndex(String uri, String localName) {
        return attrs.getIndex(uri, localName);
    }

    @Override
    public int getIndex(String qName) {
        return attrs.getIndex(qName);
    }

    @Override
    public String getType(String uri, String localName) {
        return attrs.getType(uri, localName);
    }

    @Override
    public String getType(String qName) {
        return attrs.getType(qName);
    }

    @Override
    public String getValue(String uri, String localName) {
        return attrs.getValue(uri, localName);
    }

    @Override
    public String getValue(String qName) {
        return attrs.getValue(qName);
    }


    private final Attributes attrs;
}
