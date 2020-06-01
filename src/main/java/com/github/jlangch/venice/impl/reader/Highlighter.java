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
package com.github.jlangch.venice.impl.reader;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import com.github.jlangch.venice.EofException;
import com.github.jlangch.venice.ParseError;
import com.github.jlangch.venice.impl.util.ErrorMessage;


public class Highlighter {
	
	private Highlighter(
			final String form, 
			final List<Token> formTokens
	) {
		this.tokens = formTokens;
		this.position = 0;
	}

	public static List<HighlightItem> highlight(final String str) {
		final Highlighter hl = new Highlighter(
										str,
										Tokenizer.tokenize(
											str, "highlighter", false, false));

		try {
			hl.process_form();		
			return hl.items();
		}
		catch(EofException ex) {
			// return what we've got so far
			return hl.items(); 
		}
	}

	private List<HighlightItem> items() {
		return items;
	}
	
	private void add(final HighlightItem item) {
		items.add(item);
	}

	private Token peek() {
		if (position >= tokens.size()) {
			throw new EofException("Unexpected EOF");
		}
		
		Token tok = tokens.get(position);
		while(tok.isWhitespacesOrComment()) {
			if (tok.isWhitespaces()) {
				add(new HighlightItem(tok.getToken(), HighlightClass.WHITESPACES));
			}
			else if (tok.isComment()) {
				add(new HighlightItem(tok.getToken(), HighlightClass.COMMENT));
			}
			
			position++;
			if (position >= tokens.size()) {
				throw new EofException("Unexpected EOF");
			}
			tok = tokens.get(position);
		}
		
		return tok;
	}
   
	private Token next() {
		final Token t = peek();
		position++;
		return t;
	}
	
	private void process_atom() {
		final Token token = next();

		final Matcher matcher = Reader.ATOM_PATTERN.matcher(token.getToken());
		
		if (!matcher.find()) {
			add(new HighlightItem(token.getToken(), HighlightClass.UNKNOWN));
		}
		
		if (matcher.group(1) != null) {
			// 1: long
			add(new HighlightItem(token.getToken(), HighlightClass.NUMBER));
		} 
		if (matcher.group(2) != null) {
			// 2: int
			add(new HighlightItem(token.getToken(), HighlightClass.NUMBER));
		} 
		else if (matcher.group(3) != null) {
			// 3: double
			add(new HighlightItem(token.getToken(), HighlightClass.NUMBER));
		} 
		else if (matcher.group(4) != null) {
			// 4: bigdecimal
			add(new HighlightItem(token.getToken(), HighlightClass.NUMBER));
		} 
		else if (matcher.group(5) != null) {
			// 5: nil
			add(new HighlightItem(token.getToken(), HighlightClass.CONSTANT));
		} 
		else if (matcher.group(6) != null) {
			// 6: true
			add(new HighlightItem(token.getToken(), HighlightClass.CONSTANT));
		} 
		else if (matcher.group(7) != null) {
			// 7: false
			add(new HighlightItem(token.getToken(), HighlightClass.CONSTANT));
		} 
		else if (matcher.group(8) != null) {
			// 8: string """
			add(new HighlightItem(token.getToken(), HighlightClass.STRING));
		} 
		else if (matcher.group(9) != null) {
			// 9: string "
			add(new HighlightItem(token.getToken(), HighlightClass.STRING));
		} 
		else if (matcher.group(10) != null) {
			// 10: keyword
			add(new HighlightItem(token.getToken(), HighlightClass.KEYWORD));
		} 
		else if (matcher.group(11) != null) {
			// 11: symbol
			add(new HighlightItem(token.getToken(), HighlightClass.SYMBOL));
		} 
		else {
			add(new HighlightItem(token.getToken(), HighlightClass.UNKNOWN));
		}
	}

	private void process_list(
			final char start, 
			final char end
	) {
		final Token startTok = next();
		add(new HighlightItem(startTok.getToken(), HighlightClass.BRACE_BEGIN));
		
		Token token = peek();
		while (token.charAt(0) != end) {
			process_form();
			token = peek();
		}
		
		final Token endTok = next();
		add(new HighlightItem(endTok.getToken(), HighlightClass.BRACE_END));
	}

	private void process_form() {
		final Token token = peek();
		
		switch (token.charAt(0)) {
			case '\'': 
				next();
				add(new HighlightItem('\'', HighlightClass.QUOTE));
				process_form();
				break;
			
			case '`': 
				next();
				add(new HighlightItem('`', HighlightClass.QUASI_QUOTE));
				process_form();
				break;
			
			case '~':
				if (token.equals("~")) {
					next();
					add(new HighlightItem('~', HighlightClass.UNQUOTE));
					process_form();
				} 
				else {
					next();
					add(new HighlightItem("~@", HighlightClass.UNQUOTE_SPLICING));
					process_form();
				}
				break;
			
			case '^':
				next();
				add(new HighlightItem('^', HighlightClass.META));			
				process_form();
				break;
			
			
			case '@': 
				next();
				add(new HighlightItem("@", HighlightClass.AT));
				process_form();
				break;
				
			case '#': 
				next();
				add(new HighlightItem("#", HighlightClass.HASH));

				Token t = peek();
				if (t.charAt(0) == '{') { // set literal #{1 2}
					process_list('{' , '}'); 
				}
				else if (t.charAt(0) == '(') { // anonymous function literal #(> % 2)
					process_list('(' , ')');
				}
				break;
			
			case '(': 
				process_list('(' , ')'); 
				break;
			
			case ')': 
				throw new ParseError(formatParseError(token, "Unexpected ')'"));
			
			case '[': 
				process_list('[' , ']'); 
				break;
			
			case ']': 
				throw new ParseError(formatParseError(token, "Unexpected ']'"));
				
			case '{': 
				process_list('{', '}');
				break;
				
			case '}': 
				throw new ParseError(formatParseError(token, "Unexpected '}'"));
				
			default:  
				process_atom();
				break;
		}
	}
	
	
	private static String formatParseError(final Token token, final String format, final Object... args) {
		return String.format(format, args) + ". " + ErrorMessage.buildErrLocation(token);
	}

	
	private final List<HighlightItem> items= new ArrayList<>();
	private final List<Token> tokens;
	private int position;
}
