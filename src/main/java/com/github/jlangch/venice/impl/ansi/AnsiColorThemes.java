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
package com.github.jlangch.venice.impl.ansi;

import static com.github.jlangch.venice.impl.reader.HighlightClass.AT;
import static com.github.jlangch.venice.impl.reader.HighlightClass.BRACE_BEGIN;
import static com.github.jlangch.venice.impl.reader.HighlightClass.BRACE_END;
import static com.github.jlangch.venice.impl.reader.HighlightClass.BRACKET_BEGIN;
import static com.github.jlangch.venice.impl.reader.HighlightClass.BRACKET_END;
import static com.github.jlangch.venice.impl.reader.HighlightClass.COMMENT;
import static com.github.jlangch.venice.impl.reader.HighlightClass.CONSTANT;
import static com.github.jlangch.venice.impl.reader.HighlightClass.HASH;
import static com.github.jlangch.venice.impl.reader.HighlightClass.KEYWORD;
import static com.github.jlangch.venice.impl.reader.HighlightClass.META;
import static com.github.jlangch.venice.impl.reader.HighlightClass.NUMBER;
import static com.github.jlangch.venice.impl.reader.HighlightClass.PARENTHESIS_BEGIN;
import static com.github.jlangch.venice.impl.reader.HighlightClass.PARENTHESIS_END;
import static com.github.jlangch.venice.impl.reader.HighlightClass.QUASI_QUOTE;
import static com.github.jlangch.venice.impl.reader.HighlightClass.QUOTE;
import static com.github.jlangch.venice.impl.reader.HighlightClass.STRING;
import static com.github.jlangch.venice.impl.reader.HighlightClass.SYMBOL;
import static com.github.jlangch.venice.impl.reader.HighlightClass.SYMBOL_FUNCTION_NAME;
import static com.github.jlangch.venice.impl.reader.HighlightClass.SYMBOL_SPECIAL_FORM;
import static com.github.jlangch.venice.impl.reader.HighlightClass.UNKNOWN;
import static com.github.jlangch.venice.impl.reader.HighlightClass.UNQUOTE;
import static com.github.jlangch.venice.impl.reader.HighlightClass.UNQUOTE_SPLICING;
import static com.github.jlangch.venice.impl.reader.HighlightClass.WHITESPACES;

import java.util.HashMap;
import java.util.Map;

import com.github.jlangch.venice.impl.reader.HighlightClass;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.util.StringUtil;


public class AnsiColorThemes {

	public AnsiColorThemes() {
	}
	
	public static AnsiColorTheme getTheme(final String name) {
		switch(StringUtil.trimToEmpty(name)) {
			case "light": return getLightTheme();
			case "dark":  return getDarkTheme();
			default:      return null;
		}
	}
		
	public static AnsiColorTheme getLightTheme() {
		final Map<VncKeyword,String> colors = new HashMap<>();
	
		colors.put(map(COMMENT), 				LIGHT_GREY_LIGHT);
		colors.put(map(WHITESPACES), 			null);
		
		colors.put(map(STRING), 				LIGHT_GREEN);
		colors.put(map(NUMBER), 				LIGHT_ORANGE);
		colors.put(map(CONSTANT),				LIGHT_ORANGE);
		colors.put(map(KEYWORD), 				LIGHT_ORANGE);
		colors.put(map(SYMBOL), 				LIGHT_GREY);
		colors.put(map(SYMBOL_SPECIAL_FORM), 	LIGHT_PURPLE);
		colors.put(map(SYMBOL_FUNCTION_NAME),	LIGHT_BLUE);
		
		colors.put(map(QUOTE),					LIGHT_GREY);	
		colors.put(map(QUASI_QUOTE),			LIGHT_GREY);	
		colors.put(map(UNQUOTE),				LIGHT_GREY);		
		colors.put(map(UNQUOTE_SPLICING),		LIGHT_GREY);

		colors.put(map(META),					LIGHT_GREY_LIGHT);
		colors.put(map(AT),						LIGHT_GREY);
		colors.put(map(HASH),					LIGHT_GREY);
		
		colors.put(map(BRACE_BEGIN),			LIGHT_GREY);
		colors.put(map(BRACE_END),				LIGHT_GREY);
		colors.put(map(BRACKET_BEGIN),			LIGHT_GREY);
		colors.put(map(BRACKET_END),			LIGHT_GREY);
		colors.put(map(PARENTHESIS_BEGIN),		LIGHT_GREY);
		colors.put(map(PARENTHESIS_END),		LIGHT_GREY);

		colors.put(map(UNKNOWN),				LIGHT_GREY);

		return new AnsiColorTheme("light", colors);
	}
	
	public static AnsiColorTheme getDarkTheme() {
		final Map<VncKeyword,String> colors = new HashMap<>();
	
		colors.put(map(COMMENT), 				DARK_GREY_DARK);
		colors.put(map(WHITESPACES), 			null);
		
		colors.put(map(STRING), 				DARK_GREEN);
		colors.put(map(NUMBER), 				DARK_ORANGE);
		colors.put(map(CONSTANT),				DARK_ORANGE);
		colors.put(map(KEYWORD), 				DARK_ORANGE);
		colors.put(map(SYMBOL), 				DARK_GREY);
		colors.put(map(SYMBOL_SPECIAL_FORM), 	DARK_PURPLE);
		colors.put(map(SYMBOL_FUNCTION_NAME),	DARK_BLUE);
		
		colors.put(map(QUOTE),					DARK_GREY);	
		colors.put(map(QUASI_QUOTE),			DARK_GREY);	
		colors.put(map(UNQUOTE),				DARK_GREY);		
		colors.put(map(UNQUOTE_SPLICING),		DARK_GREY);

		colors.put(map(META),					DARK_GREY_DARK);
		colors.put(map(AT),						DARK_GREY);
		colors.put(map(HASH),					DARK_GREY);
		
		colors.put(map(BRACE_BEGIN),			DARK_GREY);
		colors.put(map(BRACE_END),				DARK_GREY);
		colors.put(map(BRACKET_BEGIN),			DARK_GREY);
		colors.put(map(BRACKET_END),			DARK_GREY);
		colors.put(map(PARENTHESIS_BEGIN),		DARK_GREY);
		colors.put(map(PARENTHESIS_END),		DARK_GREY);

		colors.put(map(UNKNOWN),				DARK_GREY);

		return new AnsiColorTheme("dark", colors);
	}

	private static VncKeyword map(final HighlightClass clazz) {
		return new VncKeyword(clazz.name().toLowerCase().replace('_', '-'));
	}

	
	// light mode
	private static String LIGHT_PURPLE      = "\u001b[38;5;128m";
	private static String LIGHT_GREY        = "\u001b[38;5;235m";
	private static String LIGHT_GREY_LIGHT  = "\u001b[38;5;249m";
	private static String LIGHT_BLUE        = "\u001b[38;5;20m";
	private static String LIGHT_GREEN       = "\u001b[38;5;28m";
	private static String LIGHT_ORANGE      = "\u001b[38;5;208m";

	// dark mode
	private static String DARK_PURPLE     = "\u001b[38;5;164m";
	private static String DARK_GREY       = "\u001b[38;5;252m";
	private static String DARK_GREY_DARK  = "\u001b[38;5;244m";
	private static String DARK_BLUE       = "\u001b[38;5;39m";
	private static String DARK_GREEN      = "\u001b[38;5;41m";
	private static String DARK_ORANGE     = "\u001b[38;5;208m";
}
