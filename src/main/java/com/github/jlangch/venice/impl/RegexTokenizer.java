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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.jlangch.venice.EofException;
import com.github.jlangch.venice.ParseError;
import com.github.jlangch.venice.impl.util.ErrorMessage;


public class RegexTokenizer {

	private RegexTokenizer(final String text, final String fileName) {
		this(text, fileName, true);
	}

	private RegexTokenizer(
			final String text, 
			final String fileName,
			final boolean errorOnUnbalancedStringQuotes
	) {
		this.text = text;
		this.fileName = fileName;
		this.errorOnUnbalancedStringQuotes = errorOnUnbalancedStringQuotes;
	}
	
	
	public static List<Token> tokenize(final String text, final String fileName) {
		return new RegexTokenizer(text, fileName, true).tokenize();
	}

	public static List<Token> tokenize(
			final String text, 
			final String fileName,
			final boolean errorOnUnbalancedStringQuotes
	) {
		return new RegexTokenizer(
						text, 
						fileName, 
						errorOnUnbalancedStringQuotes
					).tokenize();
	}
	
	private List<Token> tokenize() {
		tokens.clear();

		final char[] strArr = text.toCharArray();
		final Matcher matcher = tokenize_pattern.matcher(text);

		int[] lastPos = {1,1};
		int lastStartPos = 0;
		
		final ArrayList<Token> tokens = new ArrayList<>();
		while (matcher.find()) {
			final String token = matcher.group(1);
			if (token == null || token.equals("")) {
				continue;
			}
			else if (token.startsWith("\"\"\"") && !token.endsWith("\"\"\"") && errorOnUnbalancedStringQuotes) {
				// EOF in triple quoted string
				final int tokenStartPos = matcher.start(1);			
				final int[] pos = getTextPosition(strArr, tokenStartPos, lastStartPos, lastPos[0], lastPos[1]);				
				throw new EofException(formatParseError(
							new Token(token, fileName, tokenStartPos, pos[0], pos[1]), 
							"Expected closing \"\"\" for triple quoted string but got EOF"));
			}
			else if (token.startsWith("\"") && !token.endsWith("\"") && errorOnUnbalancedStringQuotes) {
				// EOL in single quoted string
				final int tokenStartPos = matcher.start(1);			
				final int[] pos = getTextPosition(strArr, tokenStartPos, lastStartPos, lastPos[0], lastPos[1]);				
				throw new ParseError(formatParseError(
							new Token(token, fileName, tokenStartPos, pos[0], pos[1]), 
							"Expected closing \" for single quoted string but got EOL"));
			}
			else if (token.charAt(0) != ';') {
				// not a comment
				final int tokenStartPos = matcher.start(1);
				
				final int[] pos = getTextPosition(strArr, tokenStartPos, lastStartPos, lastPos[0], lastPos[1]);
				
				tokens.add(new Token(token, fileName, tokenStartPos, pos[0], pos[1]));
				
				lastStartPos = tokenStartPos;
				lastPos = pos;
			}
		}

		
		return tokens;
	}
	
	private static int[] getTextPosition(
			final char[] text, 
			final int pos, 
			final int startPos, 
			final int startRow, 
			final int startCol
	) {
		int row = startRow;
		int col = startCol;
		
		for(int ii=startPos; ii<pos; ii++) {
			switch (text[ii]) {
			case '\n': row++; col=1; break;
			case '\r': break;
			case '\t': col+=4; break;
			default:   col++; break;
			}
		}
		
		return new int[] {row,col};
	}
	
	private static String formatParseError(final Token token, final String format, final Object... args) {
		return String.format(format, args) + ". " + ErrorMessage.buildErrLocation(token);
	}
	
	
	// (?:X)      non capturing group
	// [\\s\\S]*? zero or more characters, linefeed included, reluctant not greedy
	// \\s        whitespace
	//
	// tokens:
	//    unquote splicing => ~@
	//    chars            => [\\[\\]{}()'`~@]
	//    string           => \"{3}(?:[\\s\\S]*?)\"{3}
	//    string           => \"{3}(?:[\\s\\S]*)           (-> EOF in triple quoted string)
	//    string           => \"(?:[\\\\].|[^\\\\\"])*\"
	//    string           => \"(?:[\\\\].|[^\\\\\"])*     (-> EOL in single quoted string)
	//    comment          => ;.*
	//    else             => [^\\s \\[\\]{}()'\"`~@,;]
	private static final Pattern tokenize_pattern = Pattern.compile(
														"[\\s ,]*("
														+ "~@"
														+ "|\\^"
														+ "|[\\[\\]{}()'`~@]"
														+ "|\"{3}(?:[\\s\\S]*?)\"{3}"
														+ "|\"{3}(?:[\\s\\S]*)"
														+ "|\"(?:[\\\\].|[^\\\\\"])*\""
														+ "|\"(?:[\\\\].|[^\\\\\"])*"
														+ "|;.*"
														+ "|[^\\s \\[\\]{}()'\"`~@,;]*"
														+ ")");
	
	
	private final String text;
	private final String fileName;
	private final boolean errorOnUnbalancedStringQuotes;
	private final List<Token> tokens = new ArrayList<>();
}
