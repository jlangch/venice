/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2018 Venice
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.text.StringEscapeUtils;
import org.stringtemplate.v4.AttributeRenderer;

import com.github.jlangch.venice.impl.util.StringUtil;


/**
 * Provides a string renderer for the <code>StringTemplate</code> library.
 * 
 * <p>The renderer supports the formats:
 * <ul>
 *   <li>{@code html}: Encode for HTML</li>
 *   <li>{@code xml}: Encode for XML</li>
 *   <li>{@code js}: Encode for JavaScript</li>
 *   <li>{@code url}: Encode as UTF-8 URL</li>
 *   <li>{@code none}: No encoding</li>
 * </ul>
 * 
 * <p>Examples:
 * <pre>
 *    <span>$text; format="xml"$</span>
 * </pre>
 *  
 * @author juerg
 */
public class StringEscRenderer implements AttributeRenderer {
	
	public StringEscRenderer() {
	}


	@Override
	public String toString(
			final Object attribute, 
			final String format, 
			final Locale locale
	) {
		if (attribute == null) {
			return null;
		}

		final RendererFlags flags = RendererFlags.parse(format);

		if (flags.contains("html")) {
			return StringEscapeUtils.escapeHtml4(attribute.toString());
		}
		else if (flags.contains("js")) {
			return StringEscapeUtils.escapeEcmaScript(attribute.toString());
		}
		else if (flags.contains("xml")) {
			if (flags.contains("multiline")) {
				return multiline(StringEscapeUtils.escapeXml10(attribute.toString()));
			}
			else {
				return StringEscapeUtils.escapeXml10(attribute.toString());
			}
		}
		else if (flags.contains("url")) {
			return urlEncode(attribute.toString());
			
		}
		else if (flags.contains("none")) {
			return attribute.toString();
		}
		else {
			return attribute.toString();
		}
	}

	private static String urlEncode(final String text) {
		try {
			return URLEncoder.encode(text, "UTF-8");
		} 
		catch (UnsupportedEncodingException ex) {
			// UTF-8 is standard, should always be available
			throw new RuntimeException(ex);
		}
	}


	private static String multiline(final String text) {
			if (text == null) {
				return null;
			}
			else {
				return StringUtil
						.splitIntoLines(text)
						.stream()
						.collect(Collectors.joining("<br/>"));
			}
	}
}
