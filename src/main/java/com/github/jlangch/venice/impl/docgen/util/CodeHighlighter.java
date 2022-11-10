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
package com.github.jlangch.venice.impl.docgen.util;

import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.reader.HighlightClass;
import com.github.jlangch.venice.impl.reader.HighlightParser;
import com.github.jlangch.venice.impl.util.StringEscapeUtil;


public class CodeHighlighter {

    public CodeHighlighter(final ColorTheme theme) {
        this.theme = theme;
    }

    public String highlight(final String form) {
        return HighlightParser
                .parse(form)
                .stream()
                .map(it -> style(it.getForm(), it.getClazz()))
                .collect(Collectors.joining());
    }

    public static String style(final String text, final String htmlColor) {
        return htmlColor == null
                ? StringEscapeUtil.escapeXml(text)
                : String.format(TEMPLATE, htmlColor, StringEscapeUtil.escapeXml(text));
    }


    private String style(final String text, final HighlightClass clazz) {
        return style(text, theme.getColor(clazz));
    }


    private final ColorTheme theme;
    private final static String TEMPLATE = "<span style=\"color: %s\">%s</span>";
}
