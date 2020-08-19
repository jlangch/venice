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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.jlangch.venice.ContinueException;
import com.github.jlangch.venice.EofException;
import com.github.jlangch.venice.ParseError;
import com.github.jlangch.venice.impl.AnonymousFnArgs;
import com.github.jlangch.venice.impl.AutoGenSym;
import com.github.jlangch.venice.impl.MetaUtil;
import com.github.jlangch.venice.impl.functions.CoreFunctions;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncBigDecimal;
import com.github.jlangch.venice.impl.types.VncBigInteger;
import com.github.jlangch.venice.impl.types.VncBoolean;
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
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ErrorMessage;
import com.github.jlangch.venice.impl.util.StringUtil;


/**
 * The reader reads Venice forms/expressions from a string and returns a VncVal
 * that can be evaluated.
 * 
 * <pre>
 *                                   READER
 *           +-------------------------------------------------------+
 *           |                                                       |
 *           |  +-----------+      +-----------+       +----------+  |
 *   string ---»| Character |-----»| Tokenizer |------»|  Reader  |----» VncVal
 *           |  |  Reader   | char |           | token |          |  |
 *           |  +-----------+      +-----------+       +----------+  |
 *           |                                                       |
 *           +-------------------------------------------------------+
 * </pre>
 * 
 * @author juerg
 */
public class Reader {
	
	private Reader(
			final String filename, 
			final String form, 
			final List<Token> formTokens
	) {
		this.filename = filename;
		this.form = form;
		this.tokens = formTokens;
		this.position = 0;
	}

	public static VncVal read_str(final String str, final String filename) {
		return read_form(reader(str, filename));
	}
	
	private static Reader reader(final String str, final String filename) {
		// Modules.validateFileName(filename);		
		return new Reader(filename, str, tokenize(str, filename));
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
		if (position >= tokens.size()) {
			throw new ContinueException();
		}
		return tokens.get(position++);
	}
	

	public static List<Token> tokenize(final String str, final String filename) {
		return tokenize(str, filename, true, true); 
	}

	public static List<Token> tokenize(
			final String str, 
			final String filename, 
			final boolean errorOnUnbalancedStringQuotes,
			final boolean errorOnIncompleteEscapeChars
	) {
		return Tokenizer.tokenize(str, filename, true, errorOnUnbalancedStringQuotes, errorOnIncompleteEscapeChars);
	}

	private static VncVal read_atom(final Reader rdr) {
		final Token token = rdr.next();
		
		final String sToken = token.getToken();
		
		switch(getAtomType(token)) {
			case NIL:
				return Constants.Nil;
				
			case TRUE:
				return VncBoolean.True;
				
			case FALSE:
				return VncBoolean.False;
				
			case INTEGER: {
				final boolean hex = isHexNumberLiteral(sToken);
				return new VncInteger(
							hex ? Integer.parseInt(sToken.substring(2, sToken.length()-1), 16)
								: Integer.parseInt(butlast(sToken)), 
							MetaUtil.toMeta(token));
			}
				
			case LONG: {
				final boolean hex = isHexNumberLiteral(sToken);
				return new VncLong(
							hex ? Long.parseLong(sToken.substring(2), 16)
								: Long.parseLong(sToken),
							MetaUtil.toMeta(token));
			}
				
			case DOUBLE:
				return new VncDouble(
							Double.parseDouble(sToken), 
							MetaUtil.toMeta(token));
				
			case DECIMAL:
				return new VncBigDecimal(
							new BigDecimal(butlast(sToken)), 
							MetaUtil.toMeta(token));
				
			case BIGINT:
				return new VncBigInteger(
							new BigInteger(butlast(sToken)), 
							MetaUtil.toMeta(token));
				
			case STRING: {
					final String s = unescapeAndDecodeUnicode(
										StringUtil.removeEnd(
												sToken.substring(1), 
												"\""));			
					return interpolate(s, rdr.filename, token.getLine(), token.getColumn())
							.withMeta(MetaUtil.toMeta(token));
				}
			
			case STRING_BLOCK: {
					final String s = unescapeAndDecodeUnicode(
										StringUtil.stripIndentIfFirstLineEmpty(
											StringUtil.removeEnd(
														sToken.substring(3), 
														"\"\"\"")));			
					return interpolate(s, rdr.filename, token.getLine(), token.getColumn())
								.withMeta(MetaUtil.toMeta(token));
				}
			
			case KEYWORD:
				return new VncKeyword(sToken, MetaUtil.toMeta(token));
				
			case SYMBOL: {
					final VncSymbol sym = new VncSymbol(sToken);
					if (rdr.autoGenSym.isWithinSyntaxQuote() && rdr.autoGenSym.isAutoGenSymbol(sym)) {
						// auto gen symbols within syntax quote
						return rdr.autoGenSym.lookup(sym);
					}
					else {
						rdr.anonymousFnArgs.addSymbol(sym);
						return sym.withMeta(MetaUtil.toMeta(token));
					}
				}
			
			case UNKNOWN:
			default:
				throw new ParseError(formatParseError(
						token, 
						"Unrecognized token '%s'", 
						token.getToken()));
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
		
		Token token;
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
		
		final VncSequence lst = read_list(rdr, VncList.empty(), '{', '}');
		return VncHashMap.ofAll(lst).withMeta(MetaUtil.toMeta(refToken));
	}

	private static VncVal read_form(final Reader rdr) {
		final Token token = rdr.peek();
		if (token == null) { 
			throw new ContinueException(); 
		}
		
		switch (token.charAt(0)) {
			case '\'': 
				rdr.next();
				return VncList.of(new VncSymbol("quote"), read_form(rdr))
							  .withMeta(MetaUtil.toMeta(token));
			
			case '`': 
				rdr.next();
				try {
					// Note: auto gen symbols can not be used across nested syntax quotes.
					//       Use gensym in these cases.
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
					meta = VncHashMap.of(meta, VncBoolean.True);
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
					return VncHashSet.ofAll(read_list(rdr, VncList.empty(), '{' , '}')); 
				}
				else if (t.charAt(0) == '(') {
					final VncVal meta = MetaUtil.toMeta(t);
					// anonymous function literal #(> % 2)
					if (rdr.anonymousFnArgs.isCapturing()) {
						throw new ParseError(formatParseError(t, " #() forms cannot be nested"));						
					}
					rdr.anonymousFnArgs.startCapture();
					final VncVal body = read_list(rdr, VncList.empty(), '(' , ')').withMeta(meta);
					final VncVal argsDef = rdr.anonymousFnArgs.buildArgDef().withMeta(meta);
					final VncVal s_expr = VncList.of(new VncSymbol("fn", meta), argsDef, body);
					rdr.anonymousFnArgs.stopCapture();
					return s_expr;
				}
				else {
					throw new ParseError(formatParseError(t, "Expected '#{' or '#('"));
				}
			
			case '(': 
				return read_list(rdr, VncList.empty(), '(' , ')'); 
			
			case ')': 
				rdr.next();
				throw new ParseError(formatParseError(token, "Unexpected ')'"));
			
			case '[': 
				return read_list(rdr, VncVector.empty(), '[' , ']'); 
			
			case ']': 
				rdr.next();
				throw new ParseError(formatParseError(token, "Unexpected ']'"));
				
			case '{': 
				return read_hash_map(rdr); 
				
			case '}': 
				rdr.next();
				throw new ParseError(formatParseError(token, "Unexpected '}'"));
				
			default:  
				return read_atom(rdr);
		}
	}
	
	public static AtomType getAtomType(final Token token) {
		switch(token.getType()) {
			case STRING: 
				return AtomType.STRING;
				
			case STRING_BLOCK: 
				return AtomType.STRING_BLOCK;
				
			case ANY: {
					final String sToken = token.getToken();
					final char first = sToken.charAt(0);
					final char second = sToken.length() > 1 ? sToken.charAt(1) : ' ';
					if (first == ':') {
						return AtomType.KEYWORD;
					}
					else if (first == '0' && (second == 'x' || second == 'X')) {
						// hex: 0x00EF56AA
						final char lastCh = sToken.charAt(sToken.length()-1);
						return lastCh == 'I' ? AtomType.INTEGER : AtomType.LONG; 
					}
					else if (Character.isDigit(first) || (first == '-' && Character.isDigit(second))) {
						final char lastCh = sToken.charAt(sToken.length()-1);
						if (lastCh == 'I') {
							return AtomType.INTEGER;
						}
						else if (lastCh == 'M') {
							return AtomType.DECIMAL;
						}
						else if (lastCh == 'N') {
							return AtomType.BIGINT;
						}
						else {
							return sToken.indexOf('.') > 0 
										? AtomType.DOUBLE 
										: AtomType.LONG;
						}
					}
					else {
						switch(sToken) {
							case "nil":   return AtomType.NIL;
							case "true":  return AtomType.TRUE;
							case "false": return AtomType.FALSE;
							default:      return AtomType.SYMBOL;
						}
					}
				}
			
			default: 
				return AtomType.UNKNOWN;
		}
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
					final Reader rdr = new Reader(filename, s_, tokenize(s_, filename, false, false));
					list.add(read_list(rdr, VncList.empty(), '(' , ')'));
					
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
					return VncList.ofList(list);
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
	
	private static String formatParseError(final Token token, final String format, final Object... args) {
		return String.format(format, args) + ". " + ErrorMessage.buildErrLocation(token);
	}
	
	private static String formatParseError(final String filename, final int line, final int column, final String format, final Object... args) {
		return String.format(format, args) + ". " + ErrorMessage.buildErrLocation(filename, line, column);
	}
	
	private static boolean isHexNumberLiteral(final String s) {
		return s.startsWith("0x") || s.startsWith("0X");
	}
	
	private static String butlast(final String s) {
		return s.substring(0, s.length()-1);
	}
	
	
	
	private final String filename;
	private final String form;
	private final List<Token> tokens;
	private int position;
	private final AnonymousFnArgs anonymousFnArgs = new AnonymousFnArgs();
	
	private final AutoGenSym autoGenSym = new AutoGenSym();
}
