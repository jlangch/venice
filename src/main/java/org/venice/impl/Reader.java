/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2018 Venice
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
package org.venice.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.venice.ContinueException;
import org.venice.ParseError;
import org.venice.impl.types.Constants;
import org.venice.impl.types.VncBigDecimal;
import org.venice.impl.types.VncDouble;
import org.venice.impl.types.VncLong;
import org.venice.impl.types.VncString;
import org.venice.impl.types.VncSymbol;
import org.venice.impl.types.VncVal;
import org.venice.impl.types.collections.VncHashMap;
import org.venice.impl.types.collections.VncList;
import org.venice.impl.types.collections.VncVector;
import org.venice.impl.util.StringUtil;


public class Reader {
	
	public Reader(final ArrayList<Token> tokens) {
		this.tokens = tokens;
		this.position = 0;
	}

	public Token peek() {
		if (position >= tokens.size()) {
			return null;
		} 
		else {
			return tokens.get(position);
		}
	}
   
	public Token next() {
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

	public static VncVal read_atom(final Reader rdr) {
		final Token token = rdr.next();
		final Matcher matcher = atom_pattern.matcher(token.getToken());
		
		if (!matcher.find()) {
			throw new ParseError(String.format(
					"%s: unrecognized token '%s'",
					ErrorMessage.buildErrLocation(token),
					token.getToken()));
		}
		
		if (matcher.group(1) != null) {
			return withTokenPos(
					new VncLong(Long.parseLong(matcher.group(1))), 
					token);
		} 
		else if (matcher.group(2) != null) {
			return withTokenPos(
					new VncDouble(Double.parseDouble(matcher.group(2))), 
					token);
		} 
		else if (matcher.group(3) != null) {
			String dec = matcher.group(3);
			dec = dec.substring(0, dec.length()-1);
			return withTokenPos(
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
			return withTokenPos(
					new VncString(
							StringUtil.unescape(
									StringUtil.decodeUnicode(
											matcher.group(7)))), 
					token);
		} 
		else if (matcher.group(8) != null) {
			return withTokenPos(
					VncString.keyword(matcher.group(8)), 
					token);
		} 
		else if (matcher.group(9) != null) {
			return withTokenPos(
					new VncSymbol(matcher.group(9)), 
					token);
		} 
		else {
			throw new ParseError(String.format(
					"%s: Unrecognized '%s'",
					ErrorMessage.buildErrLocation(token),
					matcher.group(0)));
		}
	}

	public static VncVal read_list(
			final Reader rdr, 
			final VncList lst, 
			final char start, 
			final char end
	) {
		Token token = rdr.next();
		if (token.charAt(0) != start) {
			throw new ParseError(String.format(
					"%s: Expected '%s'",
					ErrorMessage.buildErrLocation(token),
					start));
		}

		while ((token = rdr.peek()) != null && token.charAt(0) != end) {
			lst.addAtEnd(read_form(rdr));
		}

		if (token == null) {
			throw new ParseError("expected '" + end + "', got EOF");
		}
		rdr.next();

		return lst;
	}

	public static VncVal read_hash_map(final Reader rdr) {
		final VncList lst = (VncList)read_list(rdr, (VncList)withTokenPos(new VncList(), rdr.peek()), '{', '}');
		return withTokenPos(new VncHashMap(lst), rdr.peek());
	}

	public static VncVal read_form(final Reader rdr) {
		final Token token = rdr.peek();
		if (token == null) { 
			throw new ContinueException(); 
		}
		
		VncVal form;

		switch (token.charAt(0)) {
			case '\'': 
				rdr.next();
				return withTokenPos(
						new VncList(new VncSymbol("quote"), read_form(rdr)), 
						token);
			
			case '`': 
				rdr.next();
				return withTokenPos(
						new VncList(new VncSymbol("quasiquote"), read_form(rdr)), 
						token);
			
			case '~':
				if (token.equals("~")) {
					rdr.next();
					return withTokenPos(
							new VncList(new VncSymbol("unquote"), read_form(rdr)), 
							token);
				} 
				else {
					rdr.next();
					return withTokenPos(
							new VncList(new VncSymbol("splice-unquote"), read_form(rdr)), 
							token);
				}
			
			case '^': 
				rdr.next();
				final VncVal meta = read_form(rdr);
				return withTokenPos(
						new VncList(new VncSymbol("with-meta"), read_form(rdr), meta), 
						token);
			
			case '@': 
				rdr.next();
				return withTokenPos(
						new VncList(new VncSymbol("deref"), read_form(rdr)), 
						token);
			
			case '(': 
				form = read_list(rdr, (VncList)withTokenPos(new VncList(), token), '(' , ')'); 
				break;
			
			case ')': 
				throw new ParseError(String.format(
						"%s: Unexpected ')'",
						ErrorMessage.buildErrLocation(token)));
			
			case '[': 
				form = read_list(rdr, (VncVector)withTokenPos(new VncVector(), token), '[' , ']'); 
				break;
			
			case ']': 
				throw new ParseError(String.format(
						"%s: Unexpected ']'",
						ErrorMessage.buildErrLocation(token)));
				
			case '{': 
				form = read_hash_map(rdr); break;
				
			case '}': 
				throw new ParseError(String.format(
						"%s: Unexpected '}'",
						ErrorMessage.buildErrLocation(token)));
				
			default:  
				form = read_atom(rdr);
				break;
		}
		
		return form;
	}

	public static VncVal read_str(final String str, final String filename) {
		return read_form(new Reader(tokenize(str, filename)));
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
		
		return new int[]{row,col};
	}
	
	private static VncVal withTokenPos(final VncVal val, final Token token) {
		val.setMetaVal(new VncSymbol(":file"), new VncString(token.getFile()));
		val.setMetaVal(new VncSymbol(":line"), new VncLong(token.getLine()));
		val.setMetaVal(new VncSymbol(":column"), new VncLong(token.getColumn()));
		return val;
	}
	
	// group 1: integer = "(^-?[0-9]+$)";
	// group 2: decimal = "(^-?[0-9]+[.][0-9]*$)";
	// group 3: bigdecimal = "(^-?[0-9]+[.][0-9]*M$)";
	// group 4: nil = "(^nil$)";
	// group 5: true = "(^true$)";
	// group 6: false = "(^false$)";
	// group 7: string_escaped = "^\"(.*)\"$";
	// group 8: string = ":(.*)";
	// group 9: symbol = "(^[^\"]*$)";	
	private static final Pattern atom_pattern = Pattern.compile("(?s)(^-?[0-9]+$)|(^-?[0-9][0-9.]*$)|(^-?[0-9][0-9.]*M$)|(^nil$)|(^true$)|(^false$)|^\"(.*)\"$|:(.*)|(^[^\"]*$)");
	
	private static final Pattern tokenize_pattern = Pattern.compile("[\\s ,]*(~@|[\\[\\]{}()'`~@]|\"(?:[\\\\].|[^\\\\\"])*\"|;.*|[^\\s \\[\\]{}()'\"`~@,;]*)");

	private ArrayList<Token> tokens;
	private int position;
}
