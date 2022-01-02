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
package com.github.jlangch.venice.impl.docgen.cheatsheet;

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


public class DocColorTheme {

	public DocColorTheme(
			final String name,
			final Map<HighlightClass,String> colors
	) {
		this.name = name;
		this.colors.putAll(colors);
	}
	
	public String getName() {
		return name;
	}
	
	public String getColor(final HighlightClass clazz) {
		return colors.get(clazz);
	}

	
	public static DocColorTheme getLightTheme() {
		final Map<HighlightClass,String> colors = new HashMap<>();
	
		colors.put(COMMENT, 				GREY_LIGHT);
		colors.put(WHITESPACES, 			null);
		
		colors.put(STRING, 					GREEN);
		colors.put(NUMBER, 					ORANGE);
		colors.put(CONSTANT,				ORANGE);
		colors.put(KEYWORD, 				ORANGE);
		colors.put(SYMBOL, 					GREY);
		colors.put(SYMBOL_SPECIAL_FORM, 	PURPLE);
		colors.put(SYMBOL_FUNCTION_NAME,	BLUE);
		colors.put(SYMBOL_MACRO_NAME,		BLUE);
		colors.put(SYMBOL_EAR_MUFFS,		BLUE);
		
		colors.put(QUOTE,					GREY);	
		colors.put(QUASI_QUOTE,				GREY);	
		colors.put(UNQUOTE,					GREY);		
		colors.put(UNQUOTE_SPLICING,		GREY);

		colors.put(META,					GREY_LIGHT);
		colors.put(AT,						GREY);
		colors.put(HASH,					GREY);
		
		colors.put(BRACE_BEGIN,				GREY);
		colors.put(BRACE_END,				GREY);
		colors.put(BRACKET_BEGIN,			GREY);
		colors.put(BRACKET_END,				GREY);
		colors.put(PARENTHESIS_BEGIN,		GREY);
		colors.put(PARENTHESIS_END,			GREY);

		colors.put(UNKNOWN,					GREY);

		colors.put(UNPROCESSED,				RED);

		return new DocColorTheme("light", colors);
	}

	
	private static String PURPLE      = "#9932CC";
	private static String GREY        = "#404040";
	private static String GREY_LIGHT  = "#808080";
	private static String BLUE        = "#4169E1";
	private static String GREEN       = "#2E8B57";
	private static String ORANGE      = "#E9967A";
	private static String RED         = "#C71585";

	
	private final String name;
	private final Map<HighlightClass,String> colors = new HashMap<>();
}
