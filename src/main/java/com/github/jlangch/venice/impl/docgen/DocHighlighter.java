/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2020 Venice
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
package com.github.jlangch.venice.impl.docgen;

import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.functions.StringFunctions;
import com.github.jlangch.venice.impl.reader.HighlightClass;
import com.github.jlangch.venice.impl.reader.HighlightParser;
import com.github.jlangch.venice.impl.types.VncString;


public class DocHighlighter {
	
	public DocHighlighter(final DocColorTheme theme) {
		this.theme = theme;
	}
	
	public String highlight(final String form) {
		return HighlightParser
				.parse(form)
				.stream()
				.map(it -> style(it.getForm(), it.getClazz()))
				.collect(Collectors.joining());
	}

	private String style(final String text, final HighlightClass clazz) {
		final String color = clazz == null ? null : theme.getColor(clazz);
		
		return color == null 
				? escapeXml(text) 
				: String.format(TEMPLATE, color, escapeXml(text));
	}
	
	private String escapeXml(final String str) {
		return ((VncString)StringFunctions
					.str_escape_html
					.applyOf(new VncString(str))).getValue();
	}
	
	
	private final DocColorTheme theme;
	private final String TEMPLATE = "<span style=\"color: %s\">%s</span>";
}
