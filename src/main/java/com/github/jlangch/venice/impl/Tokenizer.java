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
package com.github.jlangch.venice.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import com.github.jlangch.venice.EofException;
import com.github.jlangch.venice.ParseError;
import com.github.jlangch.venice.impl.util.ErrorMessage;
import com.github.jlangch.venice.impl.util.LineNumberingPushbackReader;


public class Tokenizer {

	private Tokenizer(final String text, final String fileName) {
		this(text, fileName, true);
	}

	private Tokenizer(
			final String text, 
			final String fileName,
			final boolean errorOnUnbalancedStringQuotes
	) {
		this(
			new LineNumberingPushbackReader(new StringReader(text)), 
			fileName, 
			errorOnUnbalancedStringQuotes);
	}

	private Tokenizer(
			final Reader reader,
			final String fileName,
			final boolean errorOnUnbalancedStringQuotes
	) {
		this.reader = reader instanceof LineNumberingPushbackReader
						? (LineNumberingPushbackReader)reader
						: new LineNumberingPushbackReader(reader);
		this.fileName = fileName;
		this.errorOnUnbalancedStringQuotes = errorOnUnbalancedStringQuotes;
	}
	
	public static List<Token> tokenize(final String text, final String fileName) {
		return new Tokenizer(text, fileName, true).tokenize();
	}

	public static List<Token> tokenize(
			final String text, 
			final String fileName,
			final boolean errorOnUnbalancedStringQuotes
	) {
		return new Tokenizer(
						new LineNumberingPushbackReader(new StringReader(text)), 
						fileName, 
						errorOnUnbalancedStringQuotes
					).tokenize();
	}
	
	private List<Token> tokenize() {
		tokens.clear();

		try {
			while(true) {
				int line = reader.getLineNumber() + 1;
				int col = reader.getColumnNumber() + 1;
				
				char ch = (char)reader.read();
				
				if (ch == -1) {
					break;
				}
				else if (ch == ',') {  // comma -> like a whitespace
					continue;
				}
				else if (Character.isWhitespace(ch)) {
					ch = (char)reader.read();
					while(Character.isWhitespace(ch)) {		
						ch = (char)reader.read();
					}
					reader.unread(ch);
				}
				else if (ch == '~') {  // unquote splicing
					final char chNext = (char)reader.read();
					if (chNext == '@') {
						addToken("~@", line, col);	
					}
					else if (chNext == LF) {
						addToken("~", line, col);						
					}
					else {
						reader.unread(chNext);
						reader.unread(ch);
					}
				}
				else if (ch == ';') {  // comment - read to EOL
					ch = (char)reader.read();
					while(ch != LF) {		
						ch = (char)reader.read();
					}
				}
				else if (isSpecialChar(ch)) {  // special:  ()[]{}^\`~#@
					addToken(ch, line, col);	
				}
				else if (ch == '"') {  // string
					final char chNext = (char)reader.read();
					if (chNext == LF) {
						throwSingleQuotedStringEolError("\"", line, col);
					}
					else {
						final char chNextNext = (char)reader.read();
						if (chNextNext == LF) {
							if (chNext == '"') {
								addToken("\"\"", line, col);	
							}
							else {
								throwSingleQuotedStringEolError("\"" + chNext, line, col);
							}
						}
						else if (chNext == '"' && chNextNext == '"') {
							addToken(readTripleQuotedString(line, col), line, col);					
						}
						else {
							reader.unread(chNextNext);
							reader.unread(chNext);
							addToken(readSingleQuotedString(line, col), line, col);					
						}
					}
				}
				else {
					final StringBuilder sb = new StringBuilder();
					sb.append(ch);
					
					ch = (char)reader.read();				
					while(ch != -1 && ch != ',' && ch != ';'  && ch != '"' && !isSpecialChar(ch)) { 		
						sb.append(ch);
						ch = (char)reader.read();				
					}
					
					if (ch != LF) {
						reader.unread(ch);
					}
					
					addToken(sb.toString(), line, col);	
				}
			}
		}
		catch(Exception ex) {
			throw new ParseError("Parse error (tokenizer phase) while reading from input", ex);
		}
		
		return tokens;
	}
	
	private String readSingleQuotedString(
			final int lineStart, 
			final int colStart
	) throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append('"');

		int line = reader.getLineNumber() + 1;
		int col = reader.getColumnNumber() + 1;
		char ch = (char)reader.read();

		while(true) {
			if (ch == LF) {
				throwSingleQuotedStringEolError(sb.toString(), lineStart, colStart);
			}
			else if (ch == -1) {
				throwSingleQuotedStringEofError(sb.toString(), lineStart, colStart);
			}
			else if (ch == '"') {
				sb.append(ch);
				break;
			}
			else if (ch == '\\') {
				sb.append(ch);
				sb.append(readStringEscapeChar(line, col));
			}
			else {
				sb.append(ch);
			}		
			
			line = reader.getLineNumber() + 1;
			col = reader.getColumnNumber() + 1;
			ch = (char)reader.read();
		}
		
		return sb.toString();
	}

	
	private String readTripleQuotedString(
			final int lineStart, 
			final int colStart
	) throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append("\"\"\"");

		int line = reader.getLineNumber() + 1;
		int col = reader.getColumnNumber() + 1;
		char ch = (char)reader.read();

		while(true) {
			if (ch == -1) {
				throwTripleQuotedStringEofError(sb.toString(), lineStart, colStart);
			}
			else if (ch == LF) {
				sb.append(ch);
			}
			else if (ch == '"') {
				final char chNext = (char)reader.read();
				if (chNext == '"') {
					final char chNextNext = (char)reader.read();
					if (chNextNext == '"') {
						sb.append("\"\"\"");
						break;
					}
					else {
						sb.append(ch);
						sb.append(chNext);
						sb.append(chNextNext);
					}
				}
				else {
					sb.append(ch);
					sb.append(chNext);
				}
			}
			else if (ch == '\\') {
				sb.append(ch);
				sb.append(readStringEscapeChar(line, col));
			}
			else {
				sb.append(ch);
			}
					
			line = reader.getLineNumber() + 1;
			col = reader.getColumnNumber() + 1;
			ch = (char)reader.read();
		}
		
		return sb.toString();
	}
		
	private boolean isSpecialChar(final char ch) {
		return ch == '(' 
				|| ch == ')' 
				|| ch == '[' 
				|| ch == ']'
				|| ch == '{' 
				|| ch == '}'
				|| ch == '^' 
				|| ch == '\'' 
				|| ch == '`' 
				|| ch == '~' 
				|| ch == '#'
				|| ch == '@';
	}


	private Token createToken(char token, final int line, final int col) {
		return new Token(String.valueOf(token), fileName, 0, line, col);	
	}

	private Token createToken(final String token, final int line, final int col) {
		return new Token(token, fileName, 0, line, col);	
	}

	private void addToken(final char token, final int line, final int col) {
		tokens.add(createToken(token, line, col));	
	}

	private void addToken(final String token, final int line, final int col) {
		tokens.add(createToken(token, line, col));	
	}
	
	private char readStringEscapeChar(final int line, final int col) throws IOException {
		char ch = (char)reader.read();
		if (ch == LF) {
			throw new ParseError(formatParseError(
					createToken("\\", line, col), 
					"Expected escape char a string but got EOL"));
		}
		else if (ch == -1) {
			throw new EofException(formatParseError(
					createToken("\\", line, col), 
					"Expected escape char astring but got EOF"));
		}
		
		return ch;
	}

	private String formatParseError(
			final Token token, 
			final String format, 
			final Object... args
	) {
		return String.format(format, args) + ". " + ErrorMessage.buildErrLocation(token);
	}
	
	private void throwSingleQuotedStringEolError(final String s, final int line, final int col) {
		throw new ParseError(formatParseError(
				createToken(s, line, col), 
				"Expected closing \" for single quoted string but got EOL"));
	}
	
	private void throwSingleQuotedStringEofError(final String s, final int line, final int col) {
		throw new ParseError(formatParseError(
				createToken(s, line, col), 
				"Expected closing \" for single quoted string but got EOF"));
	}
	
	private void throwTripleQuotedStringEofError(final String s, final int line, final int col) {
		throw new ParseError(formatParseError(
				createToken(s, line, col), 
				"Expected closing \" for triple quoted string but got EOF"));
	}
	
	
	
	private static final int LF = (int)'\n';

	private final LineNumberingPushbackReader reader;
	private final String fileName;
	private final boolean errorOnUnbalancedStringQuotes;
	private final List<Token> tokens = new ArrayList<>();
}
