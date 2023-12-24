/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
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
package com.github.jlangch.venice.impl.docgen.cheatsheet;

import java.util.ArrayList;
import java.util.List;

import com.github.jlangch.venice.impl.util.markdown.Markdown;


public class DocSection {

    public DocSection(final String title, final String id) {
        this(title, null, id, null, null);
    }

    public DocSection(
            final String title,
            final String subtitle,
            final String id
    ) {
        this(title, subtitle, id, null, null);
    }

    public DocSection(
            final String title,
            final String subtitle,
            final String id,
            final String header,
            final String footer
    ) {
        this.title = title;
        this.subtitle = subtitle;
        this.id = id;
        this.headerXmlStyled = style(header);
        this.footerXmlStyled = style(footer);
    }


    public String getTitle() {
        return title;
    }

    public String getFormattedTitle() {
        return subtitle == null ? title : title + "\u00A0\u00A0(" + subtitle + ")";
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getId() {
        return id;
    }

    public String getHeaderXmlStyled() {
        return headerXmlStyled;
    }

    public String getFooterXmlStyled() {
        return footerXmlStyled;
    }

    public void addSection(final DocSection section) {
        if (section != null) {
            sections.add(section);
        }
    }

    public void addLiteralItem(final String name, final String text, final String id) {
        final DocSection s = new DocSection(name, id);
        addSection(s);
        s.addItem(new DocItem(text, null));
    }

    public List<DocSection> getSections() {
        return sections;
    }

    public boolean isSectionsEmpty() {
        return sections.isEmpty();
    }

    public void addItem(final DocItem item) {
        if (item != null) {
            items.add(item);
        }
    }

    public List<DocItem> getItems() {
        return items;
    }

    public boolean isItemsEmpty() {
        return items.isEmpty();
    }

    @Override
    public String toString() {
        return getFormattedTitle() + ", id=" + id;
    }

    private static String style(final String markdown) {
        return markdown == null
                ? null
                : Markdown.parse(markdown).renderToHtml();
    }


    private final String title;
    private final String subtitle;
    private final String id;
    private final String headerXmlStyled;
    private final String footerXmlStyled;

    private final List<DocSection> sections = new ArrayList<>();
    private final List<DocItem> items = new ArrayList<>();
}
