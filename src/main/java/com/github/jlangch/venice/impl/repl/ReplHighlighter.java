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
package com.github.jlangch.venice.impl.repl;

import java.util.regex.Pattern;

import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import com.github.jlangch.venice.impl.reader.HighlightClass;
import com.github.jlangch.venice.impl.reader.HighlightItem;
import com.github.jlangch.venice.impl.reader.HighlightParser;


public class ReplHighlighter implements Highlighter {

	@Override
	public AttributedString highlight(final LineReader reader, final String buffer) {
		final AttributedStringBuilder sb = new AttributedStringBuilder();
	       	
		// -------------------------------------------------
		//        W O R K   I N    P R O G R E S S
		// -------------------------------------------------
		
		HighlightParser
			.parse(buffer)
			.forEach(it -> sb.ansiAppend(highlight(it)));
				
        if (errorPattern != null) {
            sb.styleMatches(errorPattern, AttributedStyle.INVERSE);
        }
		
		return sb.toAttributedString();
	}

	@Override
	public void setErrorPattern(final Pattern errorPattern) {
		this.errorPattern = errorPattern;
	}

	@Override
	public void setErrorIndex(final int errorIndex) {
		this.errorIndex = errorIndex;
	}

	private String highlight(final HighlightItem item) {
		return getColor(item.getClazz()) + item.getForm() +"\u001b[0m";
	}
	
	private String getColor(final HighlightClass clazz) {
		switch(clazz) {
		case COMMENT:			return "\u001b[38;5;243m";
		case WHITESPACES:		return "\u001b[38;5;243m";
			
		case STRING:			return "\u001b[38;5;243m";
		case NUMBER:			return "\u001b[38;5;243m";
		case CONSTANT:			return "\u001b[38;5;243m";
		case KEYWORD:			return "\u001b[38;5;243m";
		case SYMBOL:			return "\u001b[38;5;243m";
			
		case QUOTE:				return "\u001b[38;5;243m";
		case QUASI_QUOTE:		return "\u001b[38;5;243m";
		case UNQUOTE:			return "\u001b[38;5;243m";
		case UNQUOTE_SPLICING:	return "\u001b[38;5;243m";
	
		case META:				return "\u001b[38;5;243m";
		case AT:				return "\u001b[38;5;243m";
		case HASH:				return "\u001b[38;5;243m";
			
		case BRACE_BEGIN:		return "\u001b[38;5;243m";
		case BRACE_END:			return "\u001b[38;5;243m";
		case BRACKET_BEGIN:		return "\u001b[38;5;243m";
		case BRACKET_END:		return "\u001b[38;5;243m";
		case PARENTHESIS_BEGIN:	return "\u001b[38;5;243m";
		case PARENTHESIS_END:	return "\u001b[38;5;243m";
	
		case UNKNOWN:	
		default:				return "\u001b[38;5;243m";
	}
	}
	
	
	private Pattern errorPattern;
	private int errorIndex = -1;
}
