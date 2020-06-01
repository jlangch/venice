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

import static com.github.jlangch.venice.impl.reader.TokenType.*;

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

	private Tokenizer(
			final String text, 
			final String fileName
	) {
		this(text, fileName, true, true);
	}

	private Tokenizer(
			final String text, 
			final String fileName,
			final boolean skipWhitespaces,
			final boolean errorOnUnbalancedStringQuotes
	) {
		this(
			new StringReader(text), 
			fileName, 
			skipWhitespaces,
			errorOnUnbalancedStringQuotes);
	}

	private Tokenizer(
			final Reader reader,
			final String fileName,
			final boolean skipWhitespaces,
			final boolean errorOnUnbalancedStringQuotes
	) {
		this.reader = reader instanceof LineNumberingPushbackReader
						? (LineNumberingPushbackReader)reader
						: new LineNumberingPushbackReader(reader, 10);
		this.fileName = fileName;
		this.skipWhitespaces = skipWhitespaces;
		this.errorOnUnbalancedStringQuotes = errorOnUnbalancedStringQuotes;
	}
	
	public static List<Token> tokenize(final String text, final String fileName) {
		return new Tokenizer(text, fileName, true, true).tokenize();
	}

	public static List<Token> tokenize(
			final String text, 
			final String fileName,
			final boolean skipWhitespaces,
			final boolean errorOnUnbalancedStringQuotes
	) {
		return new Tokenizer(text, fileName, skipWhitespaces, errorOnUnbalancedStringQuotes).tokenize();
	}
	
	private List<Token> tokenize() {
		tokens.clear();

		try {
			while(true) {
				int filePos = reader.getPos();
				int line = reader.getLineNumber();
				int col = reader.getColumnNumber();
				
				int ch = reader.read();
				
				if (ch == EOF) {
					break;
				}

				else if (ch == LF) {
					addLinefeedToken(filePos, line, col);	
					continue;
				}

				// - comma: , (treated like a whitespace) ---------------------
				else if (ch == (int)',') {  
					addToken(WHITESPACES, ",", filePos, line, col);	
					continue;
				}
				
				// - whitespaces ----------------------------------------------
				else if (Character.isWhitespace(ch)) {
					final StringBuilder sb = new StringBuilder();
					sb.append((char)ch);

					ch = reader.read();
					while(Character.isWhitespace(ch)) {		
						sb.append((char)ch);
						ch = reader.read();
					}
					
					addToken(WHITESPACES, sb.toString(), filePos, line, col);	
					reader.unread(ch);
				}
				
				// - unquote splicing: ~@ -------------------------------------
				else if (ch == (int)'~') {   
					final int chNext = reader.read();
					if (chNext == (int)'@') {
						addToken(UNQUOTE_SPLICE, "~@", filePos, line, col);	
					}
					else if (chNext == LF) {
						addToken(SPECIAL_CHAR, "~", filePos, line, col);
						addLinefeedToken(filePos, line, col+1);
					}
					else {
						reader.unread(chNext);
						addToken(SPECIAL_CHAR, String.valueOf((char)ch), filePos, line, col);	
					}
				}
				
				// - comment:  ; ....  read to EOL ----------------------------
				else if (ch == (int)';') {  // 
					final StringBuilder sb = new StringBuilder();
					sb.append((char)ch);

					ch = reader.read();
					while(ch != LF && ch != EOF) {		
						sb.append((char)ch);
						ch = reader.read();
					}

					addToken(COMMENT, sb.toString(), filePos, line, col);				
					if (ch == LF) {
						addLinefeedToken(filePos, line, col + sb.length());	
					}
				}
				
				// - special chars:  ()[]{}^'`~@ ------------------------------
				else if (isSpecialChar((char)ch)) {  
					addToken(SPECIAL_CHAR, String.valueOf((char)ch), filePos, line, col);	
				}
				
				// - string:  "xx" or """xx""" --------------------------------
				else if (ch == (int)'"') {  
					final int chNext = reader.read();
					if (chNext == LF) {
						final String s = readSingleQuotedString("\"" + (char)LF, filePos, line, col);
						addToken(STRING, s, filePos, line, col);
					}
					else if (chNext == EOF) {
						if (errorOnUnbalancedStringQuotes) {
							throwSingleQuotedStringEofError("\"", filePos, line, col);
						}
					}
					else if (chNext == (int)'"') {
						final int chNextNext = reader.read();
						if (chNextNext == EOF) {
							addToken(STRING, "\"\"", filePos, line, col);	
						}
						else if (chNextNext == (int)'"') {
							addToken(BLOCK_STRING, readTripleQuotedString(filePos, line, col), filePos, line, col);
						}
						else {
							reader.unread(chNextNext);
							addToken(STRING, "\"\"", filePos, line, col);	
						}
					}
					else {
						reader.unread(chNext);
						final String s = readSingleQuotedString("\"", filePos, line, col);
						addToken(STRING, s, filePos, line, col);
					}
				}
				
				// - anything else --------------------------------------------
				else {
					final StringBuilder sb = new StringBuilder();
					sb.append((char)ch);
					
					ch = reader.read();
					while(ch != EOF 
							&& ch != (int)',' 
							&& ch != (int)';'  
							&& ch != (int)'"' 
							&& !Character.isWhitespace(ch) 
							&& !isSpecialChar((char)ch)
					) { 		
						sb.append((char)ch);
						ch = reader.read();
					}
					
					if (ch == LF) {
						addLinefeedToken(filePos, line, col);	
					}
					else {
						reader.unread(ch);
					}
					
					addToken(ANY, sb.toString(), filePos, line, col);	
				}
			}
		}
		catch(Exception ex) {
			throw new ParseError("Parse error (tokenizer phase) while reading from input", ex);
		}
		
		return tokens;
	}
	
	private String readSingleQuotedString(
			final String lead,
			final int filePosStart, 
			final int lineStart, 
			final int colStart
	) throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append(lead);

		int filePos = reader.getPos();
		int line = reader.getLineNumber();
		int col = reader.getColumnNumber();
		int ch = reader.read();

		while(true) {
			if (ch == EOF) {
				if (errorOnUnbalancedStringQuotes) {
					throwSingleQuotedStringEofError(sb.toString(), filePosStart, lineStart, colStart);
				}
				break;
			}
			else if (ch == (int)'"') {
				sb.append((char)ch);
				break;
			}
			else if (ch == (int)'\\') {
				sb.append((char)ch);
				sb.append(readStringEscapeChar(STRING, filePos, line, col));
			}
			else {
				sb.append((char)ch);
			}		
			
			filePos = reader.getPos();
			line = reader.getLineNumber();
			col = reader.getColumnNumber();
			ch = reader.read();
		}
		
		return sb.toString();
	}

	
	private String readTripleQuotedString(
			final int filePosStart, 
			final int lineStart, 
			final int colStart
	) throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append("\"\"\"");

		int filePos = reader.getPos();
		int line = reader.getLineNumber();
		int col = reader.getColumnNumber();
		int ch = reader.read();

		while(true) {
			if (ch == EOF) {
				if (errorOnUnbalancedStringQuotes) {
					throwTripleQuotedStringEofError(sb.toString(), filePosStart, lineStart, colStart);
				}
				break;
			}
			else if (ch == LF) {
				sb.append((char)ch);
			}
			else if (ch == (int)'"') {
				final int chNext = reader.read();
				if (chNext == (int)'"') {
					final int chNextNext = reader.read();
					if (chNextNext == (int)'"') {
						sb.append("\"\"\"");
						break;
					}
					else {
						sb.append((char)ch);
						sb.append((char)chNext);
						sb.append((char)chNextNext);
					}
				}
				else {
					sb.append((char)ch);
					sb.append((char)chNext);
				}
			}
			else if (ch == (int)'\\') {
				sb.append((char)ch);
				sb.append(readStringEscapeChar(BLOCK_STRING, filePos, line, col));
			}
			else {
				sb.append((char)ch);
			}
					
			filePos = reader.getPos();
			line = reader.getLineNumber();
			col = reader.getColumnNumber();
			ch = reader.read();
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
				|| ch == '@';
	}

	private void addLinefeedToken(final int filePos, final int line, final int col) { 
		addToken(WHITESPACES, "\n", filePos, line, col);
	}

	private void addToken(
			final TokenType type,
			final String token, 
			final int filePos, 
			final int line, 
			final int col
	) { 
		if (skipWhitespaces) {
			if (type != WHITESPACES && type != COMMENT) {
				tokens.add(new Token(type, token, fileName, filePos, line, col));	
			}
		}
		else {
			tokens.add(new Token(type, token, fileName, filePos, line, col));	
		}
	}
	
	private char readStringEscapeChar(
			final TokenType type,
			final int filePos, 
			final int line, 
			final int col
	) throws IOException {
		final int ch = reader.read();
		
		if (ch == LF) {
			throw new ParseError(formatParseError(
					new Token(type, "\\", fileName, filePos, line, col), 
					"Expected escaped char in a string but got EOL"));
		}
		else if (ch == -1) {
			throw new EofException(formatParseError(
					new Token(type, "\\", fileName, filePos, line, col), 
					"Expected escaped char in a string but got EOF"));
		}
		
		return (char)ch;
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
	
	private void throwSingleQuotedStringEofError(
			final String s, 
			final int filePos, 
			final int line, 
			final int col
	) {
		throw new ParseError(formatParseError(
				new Token(STRING, s, fileName, filePos, line, col), 
				"Expected closing \" for single quoted string but got EOF"));
	}
	
	private void throwTripleQuotedStringEofError(
			final String s, 
			final int filePos, 
			final int line, 
			final int col
	) {
		throw new ParseError(formatParseError(
				new Token(BLOCK_STRING, s, fileName, filePos, line, col), 
				"Expected closing \" for triple quoted string but got EOF"));
	}
	
	
	
	private static final int LF = (int)'\n';
	private static final int EOF = -1;

	private final LineNumberingPushbackReader reader;
	private final String fileName;
	private final boolean skipWhitespaces;
	private final boolean errorOnUnbalancedStringQuotes;
	private final List<Token> tokens = new ArrayList<>();
}
