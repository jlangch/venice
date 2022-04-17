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
package com.github.jlangch.venice.impl.reader;

import static com.github.jlangch.venice.impl.reader.TokenType.ANY;
import static com.github.jlangch.venice.impl.reader.TokenType.COMMENT;
import static com.github.jlangch.venice.impl.reader.TokenType.SPECIAL_CHAR;
import static com.github.jlangch.venice.impl.reader.TokenType.STRING;
import static com.github.jlangch.venice.impl.reader.TokenType.STRING_BLOCK;
import static com.github.jlangch.venice.impl.reader.TokenType.UNQUOTE_SPLICE;
import static com.github.jlangch.venice.impl.reader.TokenType.WHITESPACES;

import java.util.ArrayList;
import java.util.List;

import com.github.jlangch.venice.EofException;
import com.github.jlangch.venice.ParseError;
import com.github.jlangch.venice.impl.util.ErrorMessage;


public class Tokenizer {

	private Tokenizer(
			final String text, 
			final String fileName
	) {
		this(text, fileName, true, true, true);
	}

	private Tokenizer(
			final String text, 
			final String fileName,
			final boolean skipWhitespaces,
			final boolean errorOnUnbalancedStringQuotes,
			final boolean errorOnIncompleteEscapeChars
	) {
		this.reader = new CharacterReader(text);
		this.fileName = fileName;
		this.skipWhitespaces = skipWhitespaces;
		this.errorOnUnbalancedStringQuotes = errorOnUnbalancedStringQuotes;
		this.errorOnIncompleteEscapeChars = errorOnIncompleteEscapeChars;
	}

	
	public static List<Token> tokenize(final String text, final String fileName) {
		return new Tokenizer(text, fileName, true, true, true).tokenize();
	}

	public static List<Token> tokenize(
			final String text, 
			final String fileName,
			final boolean skipWhitespaces,
			final boolean errorOnUnbalancedStringQuotes,
			final boolean errorOnIncompleteEscapeChars
	) {
		return new Tokenizer(
					text, 
					fileName, 
					skipWhitespaces,
					errorOnUnbalancedStringQuotes, 
					errorOnIncompleteEscapeChars
				).tokenize();
	}
	
	
	private List<Token> tokenize() {
		tokens.clear();

		try {
			while(true) {
				ReaderPos pos = reader.getPos();
				
				int ch = reader.peek();
				
				if (ch == EOF) {
					break;
				}

				else if (ch == LF) {
					addToken(WHITESPACES, "\n", pos);
					reader.consume();
				}
				
				// - reader macro ---------------------------------------------
				else if (ch == (int)'#') { 
					reader.consume();
					
					final int chNext = reader.peek();
					if (chNext == (int)'\\') {
						// char reader macro. E.g.: #\A, #\\u03C0", #\space
						reader.consume();
						processCharReaderMacro(pos);  
					}
					else {
						addToken(ANY, String.valueOf('#'), pos);
						// leave the reader macro processing to the Reader
					}
				}
				
				// - whitespaces ----------------------------------------------
				else if (isWhitespace((char)ch)) {
					final StringBuilder sb = new StringBuilder();
					sb.append((char)ch);
					reader.consume();

					while(isWhitespace((char)reader.peek())) {		
						sb.append((char)reader.peek());
						reader.consume();
					}
					
					addToken(WHITESPACES, sb.toString(), pos);	
				}
				
				// - unquote splicing: ~@ -------------------------------------
				else if (ch == (int)'~') { 
					reader.consume();
					
					final int chNext = reader.peek();
					if (chNext == (int)'@') {
						addToken(UNQUOTE_SPLICE, "~@", pos);	
						reader.consume();
					}
					else {
						addToken(SPECIAL_CHAR, "~", pos);
					}
				}
				
				// - special chars:  ()[]{}^'`~@ ------------------------------
				else if (isSpecialChar((char)ch)) {
					reader.consume();
					addToken(SPECIAL_CHAR, String.valueOf((char)ch), pos);
				}
				
				// - string:  "xx" or """xx""" --------------------------------
				else if (ch == (int)'"') {
					readString(pos);
				}
				
				// - comment:  ; ....  read to EOL ----------------------------
				else if (ch == (int)';') {
					readComment(pos);
				}

				// - comma: , (treated like a whitespace) ---------------------
				else if (ch == (int)',') {  
					reader.consume();
					addToken(WHITESPACES, ",", pos);	
				}
				
				// - anything else --------------------------------------------
				else {
					readAny(ch, pos);
				}
			}
		}
		catch(RuntimeException ex) {
			throw new ParseError("Parse error (tokenizer phase) while reading from input", ex);
		}
		
		return tokens;
	}

	private void readAny(final int firstChar, final ReaderPos pos) {
		reader.consume();
		
		final StringBuilder sb = new StringBuilder();
		sb.append((char)firstChar);
		
		int ch = reader.peek();
		while(ch != EOF
				&& !isWhitespace((char)ch) 
				&& ch != (int)',' 
				&& ch != (int)';'  
				&& ch != (int)'"' 
				&& !isSpecialChar((char)ch)
		) { 		
			sb.append((char)ch);
			reader.consume();
			
			ch = reader.peek();
		}

		addToken(ANY, sb.toString(), pos);	
	}

	private void readComment(final ReaderPos pos) {
		reader.consume();
		final StringBuilder sb = new StringBuilder();
		sb.append(';');

		while(LF != reader.peek() && EOF != reader.peek()) {		
			sb.append((char)reader.peek());
			reader.consume();
		}

		addToken(COMMENT, sb.toString(), pos);				
	}

	private void processCharReaderMacro(final ReaderPos startPos) {
		final StringBuilder sb = new StringBuilder("#\\");

		int ch = reader.peek();
		if (ch == (int)'u') {  
			// unicode char:  #\\u03C0
			reader.consume();
			sb.append('u');
			
			while(true) {
				ch = reader.peek();
				if (isHexChar((char)ch)) {
					sb.append((char)ch);
					reader.consume();
				}
				else {
					break;
				}
			}
		}
		else if (ch > 32) {
			// #\\A, #\\space, #\\newline, ...
			reader.consume();
			sb.append((char)ch);
			
			while(true) {
				ch = reader.peek();
				if (isAsciiLetter((char)ch) || ch == '-') {
					sb.append((char)ch);
					reader.consume();
				}
				else {
					break;
				}
			}
		}
		
		addToken(ANY, sb.toString(), startPos);
	}

	private void readString(final ReaderPos pos) {
		reader.consume();
		
		final int chNext = reader.peek();
		if (chNext != (int)'"'){
			final String s = readSingleQuotedString(pos);
			addToken(STRING, s, pos);
		}
		else {
			reader.consume();
			
			final int chNextNext = reader.peek();
			if (chNextNext != (int)'"') {
				addToken(STRING, "\"\"", pos);	
			}
			else {
				reader.consume();
				addToken(STRING_BLOCK, readTripleQuotedString(pos), pos);
			}
		}
	}

	private String readSingleQuotedString(final ReaderPos posStart) {
		final StringBuilder sb = new StringBuilder("\"");

		while(true) {
			final int ch = reader.peek();
			
			if (ch == EOF) {
				if (errorOnUnbalancedStringQuotes) {
					throw new ParseError(formatParseError(
							new Token(STRING, sb.toString(), fileName, posStart), 
							"Expected closing \" for single quoted string but got EOF"));
				}
				break;
			}
			else if (ch == (int)'"') {
				reader.consume();
				sb.append((char)ch);
				break;
			}
			else if (ch == (int)'\\') {
				final ReaderPos pos = reader.getPos();
				reader.consume();
				sb.append((char)ch);
				readStringEscapeChar(STRING, pos, sb);
			}
			else {
				reader.consume();
				sb.append((char)ch);
			}		
		}
		
		return sb.toString();
	}

	
	private String readTripleQuotedString(final ReaderPos posStart) {
		final StringBuilder sb = new StringBuilder("\"\"\"");

		while(true) {
			final int ch = reader.peek();
			
			if (ch == EOF) {
				if (errorOnUnbalancedStringQuotes) {
					throw new ParseError(formatParseError(
							new Token(STRING_BLOCK, sb.toString(), fileName, posStart), 
							"Expected closing \" for triple quoted string but got EOF"));
				}
				break;
			}
			else if (ch == (int)'"') {
				reader.consume();
				sb.append('"');

				final int chNext = reader.peek();
				if (chNext == (int)'"') {
					reader.consume();
					sb.append('"');
									
					final int chNextNext = reader.peek();
					if (chNextNext == (int)'"') {
						reader.consume();
						sb.append('"');
						break;
					}
				}
			}
			else if (ch == (int)'\\') {
				final ReaderPos pos = reader.getPos();
				reader.consume();
				
				final int chNext = reader.peek();
				if (chNext == LF || chNext == CR) {
					reader.consume();
					sb.append((char)ch);
					sb.append((char)chNext);
				}
				else {
					sb.append((char)ch);
					readStringEscapeChar(STRING, pos, sb);
				}
			}
			else {
				reader.consume();
				sb.append((char)ch);
			}
		}
		
		return sb.toString();
	}
		
	private boolean isSpecialChar(final char ch) {
		switch(ch) {
			case '(':
			case ')': 
			case '[': 
			case ']':
			case '{': 
			case '}':
			case '^':
			case '\'': 
			case '`':
			case '~':
			case '@':
				return true;
				
			default:
				return false;
		}
	}
	
	private boolean isWhitespace(final char ch) {
		switch(ch) {
			case '\r': 
			case '\n': 
			case '\f': 
			case '\t':
			case ' ': 
				return true;
				
			default:
				return false;
		}
	}

	private void addToken(
			final TokenType type,
			final String token, 
			final ReaderPos pos
	) { 
		if (!skipWhitespaces || (type != WHITESPACES && type != COMMENT)) {
			tokens.add(new Token(type, token, fileName, pos));	
		}
	}
	
	private void readStringEscapeChar(
			final TokenType type,
			final ReaderPos pos,
			final StringBuilder sb
	) {
		final int ch = reader.peek();
	
		reader.consume();			

		if (ch == LF || ch == CR) {
			if (errorOnIncompleteEscapeChars) {
				throw new ParseError(formatParseError(
						new Token(type, "\\", fileName, pos), 
						"Expected escaped char in a string but got EOL"));
			}
		}
		else if (ch == EOF) {
			if (errorOnIncompleteEscapeChars) {
				throw new EofException(formatParseError(
						new Token(type, "\\", fileName, pos), 
						"Expected escaped char in a string but got EOF"));
			}
		}
		else {
			sb.append((char)ch);
		}
	}
	
	private boolean isHexChar(final char ch) {
		return (ch >= '0' && ch <= '9') 
				|| (ch >= 'A' && ch <= 'F')
				|| (ch >= 'a' && ch <= 'f');
	}
	
	private boolean isAsciiLetter(final char ch) {
		return (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z');
	}
	
	private String formatParseError(
			final Token token, 
			final String format, 
			final Object... args
	) {
		return String.format(format, args) 
				+ ". " 
				+ ErrorMessage.buildErrLocation(token);
	}
	
	
	private static final int LF = (int)'\n';
	private static final int CR = (int)'\r';
	private static final int EOF = -1;

	private final CharacterReader reader;
	private final String fileName;
	private final boolean skipWhitespaces;
	private final boolean errorOnUnbalancedStringQuotes;
	private final boolean errorOnIncompleteEscapeChars;
	private final List<Token> tokens = new ArrayList<>();
}
