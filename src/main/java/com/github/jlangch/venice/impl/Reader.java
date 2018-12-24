/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2019 Venice
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.jlangch.venice.ContinueException;
import com.github.jlangch.venice.ParseError;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncBigDecimal;
import com.github.jlangch.venice.impl.types.VncDouble;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncSet;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.util.ErrorMessage;
import com.github.jlangch.venice.impl.util.StringUtil;


public class Reader {
	
	public Reader(final ArrayList<Token> tokens) {
		this.tokens = tokens;
		this.position = 0;
	}

	public static VncVal read_str(final String str, final String filename) {
		return read_form(new Reader(tokenize(str, filename)));
	}

	@Override
	public String toString() {
		return tokens
				.stream()
				.map(t -> String.format("%-8s %s",  String.format("%d,%d:", t.getLine(), t.getColumn()), t.getToken()))
				.collect(Collectors.joining("\n"));
	}

	private Token peek() {
		return position >= tokens.size() ? null : tokens.get(position);
	}
   
	private Token next() {
		return tokens.get(position++);
	}
	
	public static ArrayList<Token> tokenize(final String str, final String filename) {
		final char[] strArr = str.toCharArray();
		final Matcher matcher = tokenize_pattern.matcher(str);

		int[] lastPos = {1,1};
		int lastStartPos = 0;
		
		final ArrayList<Token> tokens = new ArrayList<>();
		while (matcher.find()) {
			final String token = matcher.group(1);
			
			if (token != null && !token.equals("") && !(token.charAt(0) == ';')) {
				final int tokenStartPos = matcher.start(1);
				
				final int[] pos = getTextPosition(strArr, tokenStartPos, lastStartPos, lastPos[0], lastPos[1]);
				
				tokens.add(new Token(token, filename, pos[0], pos[1]));
				
				lastStartPos = tokenStartPos;
				lastPos = pos;
			}
		}
		return tokens;
	}

	private static VncVal read_atom(final Reader rdr) {
		final Token token = rdr.next();
		final Matcher matcher = atom_pattern.matcher(token.getToken());
		
		if (!matcher.find()) {
			throw new ParseError(String.format(
					"Unrecognized token '%s'. %s",
					token.getToken(),
					ErrorMessage.buildErrLocation(token)));
		}
		
		if (matcher.group(1) != null) {
			return MetaUtil.withTokenPos(
					new VncLong(Long.parseLong(matcher.group(1))), 
					token);
		} 
		else if (matcher.group(2) != null) {
			return MetaUtil.withTokenPos(
					new VncDouble(Double.parseDouble(matcher.group(2))), 
					token);
		} 
		else if (matcher.group(3) != null) {
			String dec = matcher.group(3);
			dec = dec.substring(0, dec.length()-1);
			return MetaUtil.withTokenPos(
					new VncBigDecimal(new BigDecimal(dec)), 
					token);
		} 
		else if (matcher.group(4) != null) {
			return Constants.Nil;
		} 
		else if (matcher.group(5) != null) {
			return Constants.True;
		} 
		else if (matcher.group(6) != null) {
			return Constants.False;
		} 
		else if (matcher.group(7) != null) {
			return MetaUtil.withTokenPos(
					new VncString(
							StringUtil.unescape(
									StringUtil.decodeUnicode(
											matcher.group(7)))), 
					token);
		} 
		else if (matcher.group(8) != null) {
			return MetaUtil.withTokenPos(
					new VncKeyword(matcher.group(8)), 
					token);
		} 
		else if (matcher.group(9) != null) {
			final VncSymbol sym = new VncSymbol(matcher.group(9));
			rdr.anonymousFnArgs.addSymbol(sym);
			return MetaUtil.withTokenPos(sym, token);
		} 
		else {
			throw new ParseError(String.format(
					"Unrecognized '%s'. %s",
					matcher.group(0),
					ErrorMessage.buildErrLocation(token)));
		}
	}

	private static VncList read_list(
			final Reader rdr, 
			final VncList lst, 
			final char start, 
			final char end
	) {
		final Token lstToken = rdr.next();
		MetaUtil.withTokenPos(lst, lstToken);

		if (lstToken.charAt(0) != start) {
			throw new ParseError(String.format(
					"Expected '%s'. %s",
					start,
					ErrorMessage.buildErrLocation(lstToken)));
		}

		Token token = lstToken;
		while ((token = rdr.peek()) != null && token.charAt(0) != end) {
			lst.addAtEnd(read_form(rdr));
		}

		if (token == null) {
			throw new ParseError(String.format(
					"Expected '" + end + "', got EOF. %s",
					ErrorMessage.buildErrLocation(lstToken)));
		}
		rdr.next();

		return lst;
	}

	private static VncHashMap read_hash_map(final Reader rdr) {
		final Token refToken = rdr.peek();
		
		final VncList lst = read_list(rdr, new VncList(), '{', '}');
		return (VncHashMap)MetaUtil.withTokenPos(new VncHashMap(lst), refToken);
	}

	private static VncVal read_form(final Reader rdr) {
		final Token token = rdr.peek();
		if (token == null) { 
			throw new ContinueException(); 
		}
		
		VncVal form;

		switch (token.charAt(0)) {
			case '\'': 
				rdr.next();
				return MetaUtil.withTokenPos(
						new VncList(new VncSymbol("quote"), read_form(rdr)), 
						token);
			
			case '`': 
				rdr.next();
				return MetaUtil.withTokenPos(
						new VncList(new VncSymbol("quasiquote"), read_form(rdr)), 
						token);
			
			case '~':
				if (token.equals("~")) {
					rdr.next();
					return MetaUtil.withTokenPos(
							new VncList(new VncSymbol("unquote"), read_form(rdr)), 
							token);
				} 
				else {
					rdr.next();
					return MetaUtil.withTokenPos(
							new VncList(new VncSymbol("splice-unquote"), read_form(rdr)), 
							token);
				}
			
			case '^': 
				rdr.next();
				final VncVal meta = read_form(rdr);
				return MetaUtil.withTokenPos(
						new VncList(new VncSymbol("with-meta"), read_form(rdr), meta), 
						token);
			
			case '@': 
				rdr.next();
				return MetaUtil.withTokenPos(
						new VncList(new VncSymbol("deref"), read_form(rdr)), 
						token);
				
			case '#': 
				rdr.next();
				Token t = rdr.peek();
				if (t.charAt(0) == '{') {
					// set literal #{1 2}
					form = new VncSet(read_list(rdr, new VncList(), '{' , '}')); 
				}
				else if (t.charAt(0) == '(') {
					// anonymous function literal #(> % 2)
					if (rdr.anonymousFnArgs.isCapturing()) {
						throw new ParseError(String.format(
								" #() forms cannot be nested. %s",
								ErrorMessage.buildErrLocation(t)));						
					}
					rdr.anonymousFnArgs.startCapture();
					final VncVal body = read_list(rdr, new VncList(), '(' , ')');
					final VncVal argsDef = rdr.anonymousFnArgs.buildArgDef();
					form = new VncList(new VncSymbol("fn"), argsDef, body);
					rdr.anonymousFnArgs.stopCapture();
				}
				else {
					throw new ParseError(String.format(
							"Expected '{' or '('. %s",
							ErrorMessage.buildErrLocation(t)));
				}
				break;
			
			case '(': 
				form = read_list(rdr, new VncList(), '(' , ')'); 
				break;
			
			case ')': 
				throw new ParseError(String.format(
						"Unexpected ')'. %s",
						ErrorMessage.buildErrLocation(token)));
			
			case '[': 
				form = read_list(rdr, new VncVector(), '[' , ']'); 
				break;
			
			case ']': 
				throw new ParseError(String.format(
						"Unexpected ']'. %s",
						ErrorMessage.buildErrLocation(token)));
				
			case '{': 
				form = read_hash_map(rdr); 
				break;
				
			case '}': 
				throw new ParseError(String.format(
						"Unexpected '}'. %s",
						ErrorMessage.buildErrLocation(token)));
				
			default:  
				form = read_atom(rdr);
				break;
		}
		
		return form;
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

	// (?s) makes the dot match all characters, including line breaks.
	// groups:
	//    1: long => (^-?[0-9]+$)
	//    2: double => (^-?[0-9]+[.][0-9]*$)
	//    3: bigdecimal => (^-?[0-9]+[.][0-9]*M$)
	//    4: nil => (^nil$)
	//    5: true => (^true$)
	//    6: false => (^false$)
	//    7: string => ^"(.*)"$
	//    8: keyword => :(.*)
	//    9: symbol => (^[^"]*$)
	private static final Pattern atom_pattern = Pattern.compile(
													"(?s)"  
													+ "(^-?[0-9]+$)"
													+ "|(^-?[0-9][0-9.]*$)"
													+ "|(^-?[0-9][0-9.]*M$)"
													+ "|(^nil$)"
													+ "|(^true$)"
													+ "|(^false$)"
													+ "|^\"(.*)\"$"
													+ "|:(.*)"
													+ "|(^[^\"]*$)");
	
	// (?:X) non capturing group
	// tokens:
	//    unquote splicing => ~@
	//    chars            => [\\[\\]{}()'`~@]
	//    string           => \"(?:[\\\\].|[^\\\\\"])*\"
	//    comment          => ;.*
	//    else             => [^\\s \\[\\]{}()'\"`~@,;]
	private static final Pattern tokenize_pattern = Pattern.compile(
														"[\\s ,]*("
														+ "~@"
														+ "|[\\[\\]{}()'`~@]"
//														+ "|\"{3}.*\"{3}"
														+ "|\"(?:[\\\\].|[^\\\\\"])*\""
														+ "|;.*"
														+ "|[^\\s \\[\\]{}()'\"`~@,;]*"
														+ ")");

	private ArrayList<Token> tokens;
	private int position;
	private final AnonymousFnArgs anonymousFnArgs = new AnonymousFnArgs();
}
