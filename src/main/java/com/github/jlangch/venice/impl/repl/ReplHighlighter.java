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

import com.github.jlangch.venice.impl.reader.HighlightClass;
import com.github.jlangch.venice.impl.reader.HighlightItem;
import com.github.jlangch.venice.impl.reader.HighlightParser;
import com.github.jlangch.venice.impl.repl.ReplConfig.ColorMode;


public class ReplHighlighter implements Highlighter {

	public ReplHighlighter(final ReplConfig config) {
		this.colorMode = config.getColorMode();
	}
	
	public void enable(final boolean val) {
		enabled = val;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	@Override
	public AttributedString highlight(
			final LineReader reader, 
			final String buffer
	) {
		final AttributedStringBuilder sb = new AttributedStringBuilder();
		
		if (enabled && !isReplCommand(buffer)) {
			HighlightParser
				.parse(buffer)
				.forEach(it -> sb.ansiAppend(highlight(it)));		
		}
		else {
			sb.append(buffer);
		}
		
		return sb.toAttributedString();
	}

	@Override
	public void setErrorPattern(final Pattern errorPattern) {
	}

	@Override
	public void setErrorIndex(final int errorIndex) {
	}

	private String highlight(final HighlightItem item) {
		final String style = getStyle(item);
								
		return style == null 
				? item.getForm() 
				: style + item.getForm() + ANSI_RESET;
	}
	
	private String getStyle(final HighlightItem item) {
		switch(colorMode) {
			case Light: return getLightModeStyle(item.getClazz());
			case Dark:  return getDarkModeStyle(item.getClazz());
			case None:  return getNoneModeStyle(item.getClazz());
			default:    return getNoneModeStyle(item.getClazz());
		}
	}

	private String getLightModeStyle(final HighlightClass clazz) {
		switch(clazz) {
			case COMMENT:				return LIGHT_GREY;
			case WHITESPACES:			return null;
				
			case STRING:				return LIGHT_GREEN;
			case NUMBER:				return LIGHT_ORANGE;
			case CONSTANT:				return LIGHT_ORANGE;
			case KEYWORD:				return LIGHT_ORANGE;
			case SYMBOL:				return LIGHT_GREY;
			case SYMBOL_SPECIAL_FORM:	return LIGHT_PURPLE;
			case SYMBOL_FUNCTION_NAME:	return LIGHT_BLUE;
				
			case QUOTE:					return LIGHT_GREY;	
			case QUASI_QUOTE:			return LIGHT_GREY;	
			case UNQUOTE:				return LIGHT_GREY;		
			case UNQUOTE_SPLICING:		return LIGHT_GREY;
		
			case META:					return LIGHT_GREY;
			case AT:					return LIGHT_GREY;
			case HASH:					return LIGHT_GREY;
				
			case BRACE_BEGIN:			return LIGHT_GREY;
			case BRACE_END:				return LIGHT_GREY;
			case BRACKET_BEGIN:			return LIGHT_GREY;
			case BRACKET_END:			return LIGHT_GREY;
			case PARENTHESIS_BEGIN:		return LIGHT_GREY;
			case PARENTHESIS_END:		return LIGHT_GREY;
		
			case UNKNOWN:				return LIGHT_GREY;
			default:					return LIGHT_GREY;
		}
	}
	
	private String getDarkModeStyle(final HighlightClass clazz) {
		switch(clazz) {
			case COMMENT:				return DARK_GREY;
			case WHITESPACES:			return null;
				
			case STRING:				return DARK_GREEN;
			case NUMBER:				return DARK_ORANGE;
			case CONSTANT:				return DARK_ORANGE;
			case KEYWORD:				return DARK_ORANGE;
			case SYMBOL:				return DARK_GREY;
			case SYMBOL_SPECIAL_FORM:	return DARK_PURPLE;
			case SYMBOL_FUNCTION_NAME:	return DARK_BLUE;
				
			case QUOTE:					return DARK_GREY;	
			case QUASI_QUOTE:			return DARK_GREY;	
			case UNQUOTE:				return DARK_GREY;		
			case UNQUOTE_SPLICING:		return DARK_GREY;
		
			case META:					return DARK_GREY;
			case AT:					return DARK_GREY;
			case HASH:					return DARK_GREY;
				
			case BRACE_BEGIN:			return DARK_GREY;
			case BRACE_END:				return DARK_GREY;
			case BRACKET_BEGIN:			return DARK_GREY;
			case BRACKET_END:			return DARK_GREY;
			case PARENTHESIS_BEGIN:		return DARK_GREY;
			case PARENTHESIS_END:		return DARK_GREY;
		
			case UNKNOWN:				return DARK_GREY;
			default:					return DARK_GREY;
		}
	}
	
	private String getNoneModeStyle(final HighlightClass clazz) {
		return null;
	}
	
	private boolean isReplCommand(final String buffer) {
		return buffer.startsWith("!");
	}

	
	// light mode
	private static String LIGHT_PURPLE  = "\u001b[38;5;128m";
	private static String LIGHT_GREY    = "\u001b[38;5;235m";
	private static String LIGHT_BLUE    = "\u001b[38;5;20m";
	private static String LIGHT_GREEN   = "\u001b[38;5;28m";
	private static String LIGHT_ORANGE  = "\u001b[38;5;208m";

	// dark mode
	private static String DARK_PURPLE   = "\u001b[38;5;164m";
	private static String DARK_GREY     = "\u001b[38;5;252m";
	private static String DARK_BLUE     = "\u001b[38;5;39m";
	private static String DARK_GREEN    = "\u001b[38;5;41m";
	private static String DARK_ORANGE   = "\u001b[38;5;208m";
	
	private static String ANSI_RESET = "\u001b[0m";
	
	private final ColorMode colorMode;
	private boolean enabled = true;
}
