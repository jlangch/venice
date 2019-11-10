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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.jlangch.venice.ContinueException;
import com.github.jlangch.venice.EofException;
import com.github.jlangch.venice.ParseError;
import com.github.jlangch.venice.impl.functions.CoreFunctions;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncBigDecimal;
import com.github.jlangch.venice.impl.types.VncDouble;
import com.github.jlangch.venice.impl.types.VncInteger;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncHashSet;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.collections.VncTinyList;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ErrorMessage;
import com.github.jlangch.venice.impl.util.StringUtil;


public class Reader {
	
	private Reader(
			final String filename, 
			final String form, 
			final ArrayList<Token> formTokens
	) {
		this.filename = filename;
		this.form = form;
		this.tokens = formTokens;
		this.position = 0;
	}
	
	public static Reader reader(final String str, final String filename) {
		// Modules.validateFileName(filename);		
		return new Reader(filename, str, tokenize(str, filename));
	}

	public static VncVal read_str(final String str, final String filename) {
		return read_form(reader(str, filename));
	}

	public String unprocessedRest() {
		return lastReadPos() < 0 ? form : form.substring(lastReadPos());
	}

	public int lastReadPos() {
		return position == 0 ? -1 : tokens.get(position-1).getFileEndPos();
	}
	
	@Override
	public String toString() {
		return tokens
				.stream()
				.map(t -> String.format(
							"%-8s %s", 
							String.format("%d,%d:", t.getLine(), t.getColumn()), 
							t.getToken()))
				.collect(Collectors.joining("\n"));
	}

	private Token peek() {
		return position >= tokens.size() ? null : tokens.get(position);
	}
   
	private Token next() {
		return tokens.get(position++);
	}
	

	public static ArrayList<Token> tokenize(final String str, final String filename) {
		return tokenize(str, filename, true); 
	}

	public static ArrayList<Token> tokenize(
			final String str, 
			final String filename, 
			final boolean errorOnUnbalancedStringQuotes
	) {
		final char[] strArr = str.toCharArray();
		final Matcher matcher = tokenize_pattern.matcher(str);

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
							new Token(token, filename, tokenStartPos, pos[0], pos[1]), 
							"Expected closing \"\"\" for triple quoted string but got EOF"));
			}
			else if (token.startsWith("\"") && !token.endsWith("\"") && errorOnUnbalancedStringQuotes) {
				// EOL in single quoted string
				final int tokenStartPos = matcher.start(1);			
				final int[] pos = getTextPosition(strArr, tokenStartPos, lastStartPos, lastPos[0], lastPos[1]);				
				throw new ParseError(formatParseError(
							new Token(token, filename, tokenStartPos, pos[0], pos[1]), 
							"Expected closing \" for single quoted string but got EOL"));
			}
			else if (token.charAt(0) != ';') {
				// not a comment
				final int tokenStartPos = matcher.start(1);
				
				final int[] pos = getTextPosition(strArr, tokenStartPos, lastStartPos, lastPos[0], lastPos[1]);
				
				tokens.add(new Token(token, filename, tokenStartPos, pos[0], pos[1]));
				
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
			throw new ParseError(formatParseError(token, "Unrecognized token '%s'", token.getToken()));
		}
		
		if (matcher.group(1) != null) {
			// 1: long
			return new VncLong(Long.parseLong(matcher.group(1)), MetaUtil.toMeta(token));
		} 
		if (matcher.group(2) != null) {
			// 2: int
			String intVal = matcher.group(2);
			intVal = intVal.substring(0, intVal.length()-1);
			return new VncInteger(Integer.parseInt(intVal), MetaUtil.toMeta(token));
		} 
		else if (matcher.group(3) != null) {
			// 3: double
			return new VncDouble(Double.parseDouble(matcher.group(3)), MetaUtil.toMeta(token));
		} 
		else if (matcher.group(4) != null) {
			// 4: bigdecimal
			String dec = matcher.group(4);
			dec = dec.substring(0, dec.length()-1);
			return new VncBigDecimal(new BigDecimal(dec), MetaUtil.toMeta(token));
		} 
		else if (matcher.group(5) != null) {
			// 5: nil
			return Constants.Nil;
		} 
		else if (matcher.group(6) != null) {
			// 6: true
			return Constants.True;
		} 
		else if (matcher.group(7) != null) {
			// 7: false
			return Constants.False;
		} 
		else if (matcher.group(8) != null) {
			// 8: string """
			String s = StringUtil.stripIndentIfFirstLineEmpty(
							unescapeAndDecodeUnicode(matcher.group(8)));
			
			return interpolate(s, rdr.filename, token.getLine(), token.getColumn())
						.withMeta(MetaUtil.toMeta(token));
		} 
		else if (matcher.group(9) != null) {
			// 9: string "
			final String s = unescapeAndDecodeUnicode(matcher.group(9));			
			return interpolate(s, rdr.filename, token.getLine(), token.getColumn())
					.withMeta(MetaUtil.toMeta(token));
		} 
		else if (matcher.group(10) != null) {
			// 10: keyword
			return new VncKeyword(matcher.group(10), MetaUtil.toMeta(token));
		} 
		else if (matcher.group(11) != null) {
			// 11: symbol
			final VncSymbol sym = new VncSymbol(matcher.group(11));
			if (rdr.autoGenSym.isWithinSyntaxQuote() && rdr.autoGenSym.isAutoGenSymbol(sym)) {
				// auto gen symbols within syntax quote
				return rdr.autoGenSym.lookup(sym);
			}
			else {
				rdr.anonymousFnArgs.addSymbol(sym);
				return sym.withMeta(MetaUtil.toMeta(token));
			}
		} 
		else {
			throw new ParseError(formatParseError(token, "Unrecognized '%s'", matcher.group(0)));
		}
	}

	private static VncSequence read_list(
			final Reader rdr, 
			final VncSequence lst, 
			final char start, 
			final char end
	) {
		final Token lstToken = rdr.next();

		if (lstToken.charAt(0) != start) {
			throw new ParseError(formatParseError(lstToken, "Expected '%c'", start));
		}

		final ArrayList<VncVal> items = new ArrayList<>();
		
		Token token = lstToken;
		while ((token = rdr.peek()) != null && token.charAt(0) != end) {
			items.add(read_form(rdr));
		}

		if (token == null) {
			throw new EofException(formatParseError(token, "Expected '%c', got EOF", end));
		}
		rdr.next();

		return lst.withValues(items, MetaUtil.toMeta(lstToken));
	}

	private static VncHashMap read_hash_map(final Reader rdr) {
		final Token refToken = rdr.peek();
		
		final VncSequence lst = read_list(rdr, new VncList(), '{', '}');
		return VncHashMap.ofAll(lst).withMeta(MetaUtil.toMeta(refToken));
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
				return VncList.of(new VncSymbol("quote"), read_form(rdr))
							  .withMeta(MetaUtil.toMeta(token));
			
			case '`': 
				rdr.next();
				try {
					rdr.autoGenSym.enterSyntaxQuote();
					
					return VncList.of(new VncSymbol("quasiquote"), read_form(rdr))
								  .withMeta(MetaUtil.toMeta(token));
				}
				finally {
					rdr.autoGenSym.leaveSyntaxQuote();
				}
			
			case '~':
				if (token.equals("~")) {
					rdr.next();
					return VncList.of(new VncSymbol("unquote"), read_form(rdr))
								   .withMeta(MetaUtil.toMeta(token));
				} 
				else {
					rdr.next();
					return VncList.of(new VncSymbol("splice-unquote"), read_form(rdr))
								  .withMeta(MetaUtil.toMeta(token));
				}
			
			case '^': {
				rdr.next();
				final Token metaToken = rdr.peek();
				VncVal meta = read_form(rdr);
				if (Types.isVncKeyword(meta)) {
					// allow ^:private is equivalent to ^{:private true}
					meta = VncHashMap.of(meta, Constants.True);
				}
				if (Types.isVncMap(meta)) {
					final Token symToken = rdr.peek();
					return read_form(rdr).withMeta(MetaUtil.mergeMeta(meta, MetaUtil.toMeta(symToken)));
				}
				else {
					throw new ParseError(formatParseError(
							metaToken, "Invalid meta data type %s", Types.getType(meta)));						
				}
			}
			
			case '@': 
				rdr.next();
				return VncList.of(new VncSymbol("deref"), read_form(rdr))
							  .withMeta(MetaUtil.toMeta(token));
				
			case '#': 
				rdr.next();
				Token t = rdr.peek();
				if (t.charAt(0) == '{') {
					// set literal #{1 2}
					form = VncHashSet.ofAll(read_list(rdr, new VncList(), '{' , '}')); 
				}
				else if (t.charAt(0) == '(') {
					final VncVal meta = MetaUtil.toMeta(t);
					// anonymous function literal #(> % 2)
					if (rdr.anonymousFnArgs.isCapturing()) {
						throw new ParseError(formatParseError(t, " #() forms cannot be nested"));						
					}
					rdr.anonymousFnArgs.startCapture();
					final VncVal body = read_list(rdr, new VncList(), '(' , ')').withMeta(meta);
					final VncVal argsDef = rdr.anonymousFnArgs.buildArgDef().withMeta(meta);
					form = VncList.of(new VncSymbol("fn", meta), argsDef, body);
					rdr.anonymousFnArgs.stopCapture();
				}
				else {
					throw new ParseError(formatParseError(t, "Expected '{' or '('"));
				}
				break;
			
			case '(': 
				form = read_list(rdr, new VncTinyList(), '(' , ')'); 
				break;
			
			case ')': 
				throw new ParseError(formatParseError(token, "Unexpected ')'"));
			
			case '[': 
				form = read_list(rdr, new VncVector(), '[' , ']'); 
				break;
			
			case ']': 
				throw new ParseError(formatParseError(token, "Unexpected ']'"));
				
			case '{': 
				form = read_hash_map(rdr); 
				break;
				
			case '}': 
				throw new ParseError(formatParseError(token, "Unexpected '}'"));
				
			default:  
				form = read_atom(rdr);
				break;
		}
		
		return form;
	}
	
	public static VncVal interpolate(final String s, final String filename, final int line, final int column) {
		// this is a reader macro implemented in Java
		
		int pos = getFirstInterpolationFormStartPos(s);
		if (pos < 0) {
			return new VncString(s);
		}
		else {
			final List<VncVal> list = new ArrayList<>();
			list.add(CoreFunctions.str);
			
			String str = s;
			while (true) {
				if (pos > 0) {
					list.add(new VncString(str.substring(0, pos)));
				}
				
				String tail;
				
				final String rest = str.substring(pos);
				if (rest.startsWith("~(")) {
					final String s_ = rest.substring(1);
					final Reader rdr = new Reader(filename, s_, tokenize(s_, filename, false));
					list.add(read_list(rdr, new VncTinyList(), '(' , ')'));
					
					tail = rdr.unprocessedRest().substring(1);
				}
				else if (rest.startsWith("~{")) {
					final int endPos = rest.indexOf('}');
					if (endPos > 2) {
						final String expr = rest.substring(2, endPos);
						final Reader rdr = reader(expr, filename);
						list.add(read_form(rdr));
						tail = rest.substring(endPos+1);
					}
					else {
						throw new ParseError(formatParseError(
								filename, line, column, 
								"Invalid value interpolation expression\"~{..}\" "));
					}
				}
				else {
					throw new ParseError(formatParseError(
							filename, line, column,
							"Interpolation error. Expected \"~(\" or \"~{\""));
				}
				
				pos = getFirstInterpolationFormStartPos(tail);
				if (pos < 0) {
					if (!tail.isEmpty()) {
						list.add(new VncString(tail));
					}
					return new VncList(list);
				}
				
				str = tail;
			}						
		}
	}

	public static VncVal interpolate_(final String s, final String filename) {
		// this is a reader macro implemented in Java
		
		int pos = getFirstInterpolationFormStartPos(s);
		if (pos < 0) {
			return new VncString(s);
		}
		else {
			final List<VncVal> list = new ArrayList<>();
			list.add(CoreFunctions.str);
			
			String str = s;
			while (true) {
				if (pos > 0) {
					list.add(new VncString(str.substring(0, pos)));
				}
				
				final String rest = str.substring(pos);
				final int offset = rest.startsWith("~{") ? 2 : 1;
				
				final Reader rdr = reader(rest.substring(offset), filename);
				list.add(read_form(rdr));
				
				final String tail = rdr.unprocessedRest().substring(offset);
				
				pos = getFirstInterpolationFormStartPos(tail);
				if (pos < 0) {
					if (!tail.isEmpty()) {
						list.add(new VncString(tail));
					}
					return new VncList(list);
				}
				
				str = tail;
			}						
		}
	}

	private static int getFirstInterpolationFormStartPos(final String s) {
		final int p1 = s.indexOf("~{");
		final int p2 = s.indexOf("~(");
		
		return (p1 < 0 || p2 < 0) ? Math.max(p1, p2) : Math.min(p1, p2);
	}
	
	private static String unescapeAndDecodeUnicode(final String s) {
		return unescape(StringUtil.decodeUnicode(s));	
	}

	private static String unescape(final String text) {
		if (text == null) {
			return text;
		}
				
		final StringBuilder sb = new StringBuilder();
		
		final char[] chars = text.toCharArray();
		final int len = chars.length;
		int ii = 0;
		while(ii<len) {
			final char c = chars[ii++];
			if (c == '\\' && ii<len) {
				switch(chars[ii++]) {
					case 'n': sb.append('\n'); break;
					case 'r': sb.append('\r'); break;
					case 't': sb.append('\t'); break;
					case '"': sb.append('"'); break;
					case '\'': sb.append('\''); break;
					case '\\': sb.append('\\'); break;
					
					// line escape
					case '\r': if (ii<len && chars[ii] == '\n') ii++; else sb.append("\\\r"); break;
					case '\n': break;
					
					default: break;
				}
			}
			else {
				sb.append(c);
			}
		}
		
		return sb.toString();
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
	
	private static String formatParseError(final String filename, final int line, final int column, final String format, final Object... args) {
		return String.format(format, args) + ". " + ErrorMessage.buildErrLocation(filename, line, column);
	}

	// (?s) makes the dot match all characters, including line breaks.
	//
	// groups:
	//     1: long => (^-?[0-9]+$)
	//     2: int => (^-?[0-9]+I$)
	//     3: double => (^-?[0-9]+[.][0-9]*$)
	//     4: bigdecimal => (^-?[0-9]+[.][0-9]*M$)
	//     5: nil => (^nil$)
	//     6: true => (^true$)
	//     7: false => (^false$)
	//     8: string => ^"""(.*)"""$
	//     9: string => ^"(.*)"$
	//    10: keyword => :(.*)
	//    11: symbol => (^[^"]*$)
	private static final Pattern atom_pattern = Pattern.compile(
													"(?s)"  
													+ "(^-?[0-9]+$)"
													+ "|(^-?[0-9]+I$)"
													+ "|(^-?[0-9][0-9.]*$)"
													+ "|(^-?[0-9][0-9.]*M$)"
													+ "|(^nil$)"
													+ "|(^true$)"
													+ "|(^false$)"
													+ "|^\"{3}(.*)\"{3}$"
													+ "|^\"(.*)\"$"
													+ "|:(.*)"
													+ "|(^[^\"]*$)");
	
	// (?:X)      non capturing group
	// [\\s\\S]*? zero or more characters, linefeed included, reluctant not greedy
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

	private final String filename;
	private final String form;
	private ArrayList<Token> tokens;
	private int position;
	private final AnonymousFnArgs anonymousFnArgs = new AnonymousFnArgs();
	
	private final AutoGenSym autoGenSym = new AutoGenSym();
}
