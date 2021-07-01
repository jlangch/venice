/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2021 Venice
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
import static com.github.jlangch.venice.impl.reader.HighlightClass.SYMBOL_EAR_MUFFS;
import static com.github.jlangch.venice.impl.reader.HighlightClass.SYMBOL_FUNCTION_NAME;
import static com.github.jlangch.venice.impl.reader.HighlightClass.SYMBOL_MACRO_NAME;
import static com.github.jlangch.venice.impl.reader.HighlightClass.SYMBOL_SPECIAL_FORM;
import static com.github.jlangch.venice.impl.reader.HighlightClass.UNKNOWN;
import static com.github.jlangch.venice.impl.reader.HighlightClass.UNPROCESSED;
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
		switch(StringUtil.trimToEmpty(name).toLowerCase()) {
			case "light": return getLightTheme();
			case "dark":  return getDarkTheme();
			default:      return null;
		}
	}
		
	public static AnsiColorTheme getLightTheme() {
		final Map<VncKeyword,String> colors = new HashMap<>();
	
		colors.put(map(COMMENT), 				LIC_GREY_LIGHT);
		colors.put(map(WHITESPACES), 			null);
		
		colors.put(map(STRING), 				LIC_GREEN);
		colors.put(map(NUMBER), 				LIC_ORANGE);
		colors.put(map(CONSTANT),				LIC_ORANGE);
		colors.put(map(KEYWORD), 				LIC_ORANGE);
		colors.put(map(SYMBOL), 				LIC_GREY);
		colors.put(map(SYMBOL_SPECIAL_FORM), 	LIC_PURPLE);
		colors.put(map(SYMBOL_FUNCTION_NAME),	LIC_BLUE);
		colors.put(map(SYMBOL_MACRO_NAME),		LIC_BLUE);
		colors.put(map(SYMBOL_EAR_MUFFS),		LIC_BLUE);
		
		colors.put(map(QUOTE),					LIC_GREY);	
		colors.put(map(QUASI_QUOTE),			LIC_GREY);	
		colors.put(map(UNQUOTE),				LIC_GREY);		
		colors.put(map(UNQUOTE_SPLICING),		LIC_GREY);

		colors.put(map(META),					LIC_GREY_LIGHT);
		colors.put(map(AT),						LIC_GREY);
		colors.put(map(HASH),					LIC_GREY);
		
		colors.put(map(BRACE_BEGIN),			LIC_GREY);
		colors.put(map(BRACE_END),				LIC_GREY);
		colors.put(map(BRACKET_BEGIN),			LIC_GREY);
		colors.put(map(BRACKET_END),			LIC_GREY);
		colors.put(map(PARENTHESIS_BEGIN),		LIC_GREY);
		colors.put(map(PARENTHESIS_END),		LIC_GREY);

		colors.put(map(UNKNOWN),				LIC_GREY);

		colors.put(map(UNPROCESSED),			LIC_RED);

		return new AnsiColorTheme("light", colors);
	}
	
	public static AnsiColorTheme getDarkTheme() {
		final Map<VncKeyword,String> colors = new HashMap<>();
	
		colors.put(map(COMMENT), 				DAC_GREY_DARK);
		colors.put(map(WHITESPACES), 			null);
		
		colors.put(map(STRING), 				DAC_GREEN);
		colors.put(map(NUMBER), 				DAC_ORANGE);
		colors.put(map(CONSTANT),				DAC_ORANGE);
		colors.put(map(KEYWORD), 				DAC_ORANGE);
		colors.put(map(SYMBOL), 				DAC_GREY);
		colors.put(map(SYMBOL_SPECIAL_FORM), 	DAC_PURPLE);
		colors.put(map(SYMBOL_FUNCTION_NAME),	DAC_BLUE);
		colors.put(map(SYMBOL_MACRO_NAME),		DAC_BLUE);
		colors.put(map(SYMBOL_EAR_MUFFS),		DAC_BLUE);
		
		colors.put(map(QUOTE),					DAC_GREY);	
		colors.put(map(QUASI_QUOTE),			DAC_GREY);	
		colors.put(map(UNQUOTE),				DAC_GREY);		
		colors.put(map(UNQUOTE_SPLICING),		DAC_GREY);

		colors.put(map(META),					DAC_GREY_DARK);
		colors.put(map(AT),						DAC_GREY);
		colors.put(map(HASH),					DAC_GREY);
		
		colors.put(map(BRACE_BEGIN),			DAC_GREY);
		colors.put(map(BRACE_END),				DAC_GREY);
		colors.put(map(BRACKET_BEGIN),			DAC_GREY);
		colors.put(map(BRACKET_END),			DAC_GREY);
		colors.put(map(PARENTHESIS_BEGIN),		DAC_GREY);
		colors.put(map(PARENTHESIS_END),		DAC_GREY);

		colors.put(map(UNKNOWN),				DAC_GREY);

		colors.put(map(UNPROCESSED),			DAC_RED);

		return new AnsiColorTheme("dark", colors);
	}

	private static VncKeyword map(final HighlightClass clazz) {
		return new VncKeyword(clazz.name().toLowerCase().replace('_', '-'));
	}

	
	// light mode
	private static String LIC_PURPLE      = "\u001b[38;5;128m";
	private static String LIC_GREY        = "\u001b[38;5;235m";
	private static String LIC_GREY_LIGHT  = "\u001b[38;5;245m";
	private static String LIC_BLUE        = "\u001b[38;5;20m";
	private static String LIC_GREEN       = "\u001b[38;5;28m";
	private static String LIC_ORANGE      = "\u001b[38;5;130m";
	private static String LIC_RED         = "\u001b[38;5;9m";

	// dark mode
	private static String DAC_PURPLE     = "\u001b[38;5;140m";
	private static String DAC_GREY       = "\u001b[38;5;252m";
	private static String DAC_GREY_DARK  = "\u001b[38;5;244m";
	private static String DAC_BLUE       = "\u001b[38;5;111m";
	private static String DAC_GREEN      = "\u001b[38;5;157m";
	private static String DAC_ORANGE     = "\u001b[38;5;216m";
	private static String DAC_RED        = "\u001b[38;5;196m";
}
