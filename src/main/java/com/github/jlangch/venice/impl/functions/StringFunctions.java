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
package com.github.jlangch.venice.impl.functions;

import static com.github.jlangch.venice.impl.functions.FunctionsUtil.assertArity;
import static com.github.jlangch.venice.impl.types.Constants.False;
import static com.github.jlangch.venice.impl.types.Constants.Nil;
import static com.github.jlangch.venice.impl.types.Constants.True;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.VncByteBuffer;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.Tuple2;


public class StringFunctions {

	///////////////////////////////////////////////////////////////////////////
	// String
	///////////////////////////////////////////////////////////////////////////
	
	public static VncFunction str_blank_Q = 
		new VncFunction(
				"str/blank?", 
				VncFunction
					.meta()
					.module("str")
					.arglists("(str/blank? s)")		
					.doc("True if s is blank.")
					.examples(
						"(str/blank? nil)", 
						"(str/blank? \"\")", 
						"(str/blank? \"  \")", 
						"(str/blank? \"abc\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("str/blank?", args, 1);
	
				if (args.first() == Nil) {
					return True;
				}
				
				final String s = Coerce.toVncString(args.first()).getValue();		
	
				return StringUtil.isBlank(s) ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction str_char = 
		new VncFunction(
				"str/char", 
				VncFunction
					.meta()
					.module("str")
					.arglists("(str/char n)")		
					.doc("Converts a number to a single char string.")
					.examples("(str/char 65)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("str/char", args, 1);
	
				if (args.first() == Nil) {
					return Nil;
				}
				
				final long n = Coerce.toVncLong(args.first()).getValue();		
	
				return new VncString(String.valueOf((char)n));
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction str_starts_with_Q = 
		new VncFunction(
				"str/starts-with?", 
				VncFunction
					.meta()
					.module("str")
					.arglists("(str/starts-with? s substr)")		
					.doc("True if s starts with substr.")
					.examples("(str/starts-with? \"abc\"  \"ab\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("str/starts-with?", args, 2);
	
				if (args.first() == Nil || args.second() == Nil) {
					return False;
				}
				
				final VncString string = Coerce.toVncString(args.first());		
				final VncString prefix = Coerce.toVncString(args.second());		
				
				return string.getValue().startsWith(prefix.getValue()) ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction str_ends_with_Q = 
		new VncFunction(
				"str/ends-with?", 
				VncFunction
					.meta()
					.module("str")
					.arglists("(str/ends-with? s substr)")		
					.doc("True if s ends with substr.")
					.examples("(str/starts-with? \"abc\"  \"bc\")")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("str/ends-with?", args, 2);
	
				if (args.first() == Nil || args.second() == Nil) {
					return False;
				}
	
				final VncString string = Coerce.toVncString(args.first());
				final VncString suffix = Coerce.toVncString(args.second());
				
				return string.getValue().endsWith(suffix.getValue()) ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction str_contains_Q = 
		new VncFunction(
				"str/contains?", 
				VncFunction
					.meta()
					.module("str")
					.arglists("(str/contains? s substr)")		
					.doc("True if s contains with substr.")
					.examples("(str/contains? \"abc\"  \"ab\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("str/contains?", args, 2);
	
				if (args.first() == Nil || args.second() == Nil) {
					return False;
				}
	
				final VncString string = Coerce.toVncString(args.first());
				final VncString text = Coerce.toVncString(args.second());
				
				return string.getValue().contains(text.getValue()) ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncFunction str_equals_ignore_case_Q = 
		new VncFunction(
				"str/equals-ignore-case?", 
				VncFunction
					.meta()
					.module("str")
					.arglists("(str/equals-ignore-case? s1 s2)")		
					.doc("Compares two strings ignoring case.  True if both are equal.")
					.examples("(str/equals-ignore-case? \"abc\"  \"abC\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("str/equals-ignore-case?", args, 2);
	
				final VncVal v1 = args.first();
				final VncVal v2 = args.second();
			
				if (v1 == Nil || v2 == Nil) {
					return True;
				}
				else if (v1 != Nil || v2 != Nil) {
					final String s1 = Coerce.toVncString(args.first()).getValue();
					final String s2 = Coerce.toVncString(args.second()).getValue();
					
					return s1.equalsIgnoreCase(s2) ? True : False;
				}
				else {
					return False;
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction str_trim = 
		new VncFunction(
				"str/trim", 
				VncFunction
					.meta()
					.module("str")
					.arglists("(str/trim s substr)")		
					.doc("Trims leading and trailing spaces from s.")
					.examples("(str/trim \" abc  \")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("str/trim", args, 1);
	
				if (args.first() == Nil) {
					return Nil;
				}
	
				return new VncString(Coerce.toVncString(args.first()).getValue().trim());
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction str_trim_to_nil = 
		new VncFunction(
				"str/trim-to-nil", 
				VncFunction
					.meta()
					.module("str")
					.arglists("(str/trim-to-nil s substr)")		
					.doc(
						"Trims leading and trailing spaces from s. " +
						"Returns nil if the rewsulting string is empry")
					.examples(
						"(str/trim \"\")",
						"(str/trim \"    \")",
						"(str/trim nil)",
						"(str/trim \" abc   \")")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("str/trim-to-nil", args, 1);
	
				if (args.first() == Nil) {
					return Nil;
				}
	
				final String str = Coerce.toVncString(args.first()).getValue().trim();
				return str.isEmpty() ? Nil : new VncString(str);
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction str_index_of = 
		new VncFunction(
				"str/index-of", 
				VncFunction
					.meta()
					.module("str")
					.arglists("(str/index-of s value)", "(str/index-of s value from-index)")		
					.doc(
						"Return index of value (string or char) in s, optionally searching " + 
						"forward from from-index. Return nil if value not found.")
					.examples(
						"(str/index-of \"abcdefabc\" \"ab\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("str/index-of", args, 2, 3);
	
				final String text = Coerce.toVncString(args.first()).getValue();	
				final String searchString = Coerce.toVncString(args.second()).getValue();		
				
				if (args.size() == 3) {
					final int startPos = Coerce.toVncLong(args.nth(2)).getValue().intValue();
					final int pos = text.indexOf(searchString, startPos);
					return pos < 0 ? Nil : new VncLong(pos);
				}
				else {
					final int pos = text.indexOf(searchString);
					return pos < 0 ? Nil : new VncLong(pos);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction str_last_index_of = 
		new VncFunction(
				"str/last-index-of", 
				VncFunction
					.meta()
					.module("str")
					.arglists("(str/last-index-of s value)", "(str/last-index-of s value from-index)")		
					.doc(
						"Return last index of value (string or char) in s, optionally\n" + 
						"searching backward from from-index. Return nil if value not found.")
					.examples(
						"(str/last-index-of \"abcdefabc\" \"ab\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("str/last-index-of", args, 2, 3);
	
				if (args.first() == Nil) {
					return Nil;
				}
	
				final String text = Coerce.toVncString(args.first()).getValue();	
				final String searchString = Coerce.toVncString(args.second()).getValue();		
				
				if (args.size() > 2) {
					final int startPos = Coerce.toVncLong(args.nth(2)).getValue().intValue();
					final int pos = text.lastIndexOf(searchString, startPos);
					return pos < 0 ? Nil : new VncLong(pos);
				}
				else {
					final int pos = text.lastIndexOf(searchString);
					return pos < 0 ? Nil : new VncLong(pos);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction str_replace_first = 
		new VncFunction(
				"str/replace-first", 
				VncFunction
					.meta()
					.module("str")
					.arglists("(str/replace-first s search replacement)")		
					.doc(
						"Replaces the first occurrance of search in s. " +
						"The search arg may be a string or a regex pattern")
					.examples(
						"(str/replace-first \"abcdefabc\" \"ab\" \"XYZ\")",
						"(str/replace-first \"a0b01c012d\" (regex/pattern \"[0-9]+\") \"_\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("str/replace-first", args, 3);
	
				if (args.first() == Nil) {
					return Nil;
				}
	
				final String text = Coerce.toVncString(args.first()).getValue();	
				final VncVal search = args.second();		
				final String replacement = Coerce.toVncString(args.nth(2)).getValue();

				if (Types.isVncString(search)) {
					final String searchString = Coerce.toVncString(args.second()).getValue();		

					if (StringUtil.isEmpty(text) || StringUtil.isEmpty(searchString) || replacement == null) {
						return args.first();
					}
		
					int pos = text.indexOf(searchString);
					return pos >= 0
						? new VncString(
								text.substring(0, pos) + 
								replacement + 
								text.substring(pos + replacement.length()))
					 	: args.first();
				}
				else if (Types.isVncJavaObject(search, Pattern.class)) {
					final Pattern p = (Pattern)((VncJavaObject)search).getDelegate();
					
					final Matcher m = p.matcher(text);
					return new VncString(m.replaceFirst(replacement));
				}
				else {
					throw new VncException(String.format(
							"Function 'str/replace-first' does not allow %s as search argument.", 
							Types.getType(search)));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction str_replace_last = 
		new VncFunction(
				"str/replace-last", 
				VncFunction
					.meta()
					.module("str")
					.arglists("(str/replace-last s search replacement)")		
					.doc("Replaces the last occurrance of search in s")
					.examples("(str/replace-last \"abcdefabc\" \"ab\" \"XYZ\")")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("str/replace-last", args, 3);
	
				if (args.first() == Nil) {
					return Nil;
				}
	
				final String text = Coerce.toVncString(args.first()).getValue();	
				final String searchString = Coerce.toVncString(args.second()).getValue();
				final String replacement = Coerce.toVncString(args.nth(2)).getValue();
	
				if (StringUtil.isEmpty(text) || StringUtil.isEmpty(searchString) || replacement == null) {
					return args.first();
				}
	
				int pos = text.lastIndexOf(searchString);
				return pos >= 0
					? new VncString(
							text.substring(0, pos) + 
							replacement + 
							text.substring(pos + replacement.length()))
				 	: args.first();
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction str_replace_all = 
		new VncFunction(
				"str/replace-all", 
				VncFunction
					.meta()
					.module("str")
					.arglists("(str/replace-all s search replacement)")		
					.doc(
						"Replaces the all occurrances of search in s. " +
						"The search arg may be a string or a regex pattern")
					.examples(
						"(str/replace-all \"abcdefabc\" \"ab\" \"XYZ\")",
						"(str/replace-all \"a0b01c012d\" (regex/pattern \"[0-9]+\") \"_\")")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("str/replace-all", args, 3);
	
				if (args.first() == Nil) {
					return Nil;
				}
	
				final String text = Coerce.toVncString(args.first()).getValue();	
				final VncVal search = args.second();		
				final String replacement = Coerce.toVncString(args.nth(2)).getValue();		

				if (Types.isVncString(search)) {
					final String searchString = Coerce.toVncString(args.second()).getValue();		

					if (StringUtil.isEmpty(text) || StringUtil.isEmpty(searchString) || replacement == null) {
						return args.first();
					}

					int start = 0;
					int end = text.indexOf(searchString, start);
					if (end == -1) {
						return args.first();
					}
					final int replLength = searchString.length();
					final StringBuilder buf = new StringBuilder();
					while (end != -1) {
						buf.append(text, start, end).append(replacement);
						start = end + replLength;
						end = text.indexOf(searchString, start);
					}
					buf.append(text, start, text.length());
					return new VncString(buf.toString());
				}
				else if (Types.isVncJavaObject(search, Pattern.class)) {
					final Pattern p = (Pattern)((VncJavaObject)search).getDelegate();
					
					final Matcher m = p.matcher(text);
					return new VncString(m.replaceAll(replacement));
				}
				else {
					throw new VncException(String.format(
							"Function 'str/replace-all' does not allow %s as search argument.", 
							Types.getType(search)));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction str_lower_case = 
		new VncFunction(
				"str/lower-case", 
				VncFunction
					.meta()
					.module("str")
					.arglists("(str/lower-case s)")		
					.doc("Converts s to lowercase")
					.examples("(str/lower-case \"aBcDeF\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("str/lower-case", args, 1);
	
				if (args.first() == Nil) {
					return Nil;
				}
	
				final VncString string = Coerce.toVncString(args.first());
				
				return new VncString(string.getValue().toLowerCase());
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction str_upper_case = 
		new VncFunction(
				"str/upper-case", 
				VncFunction
					.meta()
					.module("str")
					.arglists("(str/upper-case s)")		
					.doc("Converts s to uppercase")
					.examples("(str/upper-case \"aBcDeF\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("str/upper-case", args, 1);
	
				if (args.first() == Nil) {
					return Nil;
				}
	
				final VncString string = Coerce.toVncString(args.first());
				
				return new VncString(string.getValue().toUpperCase());
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction str_join = 
		new VncFunction(
				"str/join", 
				VncFunction
					.meta()
					.module("str")
					.arglists("(str/join coll)", "(str/join separator coll)")		
					.doc("Joins all elements in coll separated by an optional separator.")
					.examples(
						"(str/join [1 2 3])",
						"(str/join \"-\" [1 2 3])")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("str/join", args, 1, 2);
	
				final VncSequence coll = Coerce.toVncSequence(args.last());		
				final VncString delim = args.size() == 2 ? Coerce.toVncString(args.first()) : new VncString("");
				
				return new VncString(
							coll.size() > 0
								? coll
									.getList()
									.stream()
									.map(v -> Types.isVncString(v) ? ((VncString)v).getValue() : v.toString())
									.collect(Collectors.joining(delim.getValue()))
								: "");
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction str_subs = 
		new VncFunction(
				"str/subs", 
				VncFunction
					.meta()
					.module("str")
					.arglists("(str/subs s start)", "(str/subs s start end)")		
					.doc(
						"Returns the substring of s beginning at start inclusive, and ending " + 
						"at end (defaults to length of string), exclusive.")
					.examples(
						"(str/subs \"abcdef\" 2)",
						"(str/subs \"abcdef\" 2 5)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("str/subs", args, 2, 3);
	
				final VncString string = Coerce.toVncString(args.first());		
				final VncLong from = Coerce.toVncLong(args.second());
				final VncLong to = args.size() > 2 ? (VncLong)args.nth(2) : null;
				
				return new VncString(
								to == null
									? string.getValue().substring(from.getValue().intValue())
									: string.getValue().substring(from.getValue().intValue(), to.getValue().intValue()));
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction str_split = 
		new VncFunction(
				"str/split", 
				VncFunction
					.meta()
					.module("str")
					.arglists("(str/split s regex)")		
					.doc("Splits string on a regular expression.")
					.examples(
						"(str/split \"abc,def,ghi\" \",\")",
						"(str/split \"abc , def , ghi\" \"[ *],[ *]\")",
						"(str/split \"abc,def,ghi\" \"((?<=,)|(?=,))\")")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("str/split", args, 2);
	
				final VncString string = Coerce.toVncString(args.first());
				final VncString regex = Coerce.toVncString(args.second());
				
				return new VncList(
						Arrays
							.asList(string.getValue().split(regex.getValue()))
							.stream()
							.map(s -> new VncString(s))
							.collect(Collectors.toList()));			
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction str_split_lines = 
		new VncFunction(
				"str/split-lines", 
				VncFunction
					.meta()
					.module("str")
					.arglists("(str/split-lines s)")		
					.doc("Splits s into lines.")
					.examples("(str/split-lines \"line1\nline2\nline3\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("str/split-lines", args, 1);
	
				return args.first() == Nil
						? new VncList()
						: new VncList(
								StringUtil
									.splitIntoLines(Coerce.toVncString(args.first()).getValue())
									.stream()
									.map(s -> new VncString(s))
									.collect(Collectors.toList()));
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction str_format = 
		new VncFunction(
				"str/format", 
				VncFunction
					.meta()
					.module("str")
					.arglists(
						"(str/format format args*)",
						"(str/format locale format args*)")		
					.doc("Returns a formatted string using the specified format string and arguments.")
					.examples(
						"(str/format \"value: %.4f\" 1.45)",
						"(str/format (. :java.util.Locale :new \"de\" \"DE\") \"value: %.4f\" 1.45)",
						"(str/format (. :java.util.Locale :GERMANY) \"value: %.4f\" 1.45)",
						"(str/format [ \"de\"] \"value: %.4f\" 1.45)",
						"(str/format [ \"de\" \"DE\"] \"value: %.4f\" 1.45)")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				if (Types.isVncJavaObject(args.first(), Locale.class)) {
					final Locale locale = (Locale)((VncJavaObject)args.first()).getDelegate();
					final VncString fmt = (VncString)args.second();
					final VncList fmtArgs = args.slice(2);
					return new VncString(String.format(locale, fmt.getValue(), toJavaObjects(fmtArgs).toArray()));
				}
				else if (Types.isVncSequence(args.first())) {
					final VncSequence localeSeq = (VncSequence)args.first();
					final String fmt = Coerce.toVncString(args.second()).getValue();
					final Object[] fmtArgs = toJavaObjects(args.slice(2)).toArray();
					switch (localeSeq.size()) {
						case 0:
							return new VncString(String.format(fmt, fmtArgs));
						case 1:
							// language
							final Locale locale1 = new Locale(
														Coerce.toVncString(localeSeq.first()).getValue());
							return new VncString(String.format(locale1, fmt, fmtArgs));
						case 2:
							// language, country
							final Locale locale2 = new Locale(
														Coerce.toVncString(localeSeq.first()).getValue(),
														Coerce.toVncString(localeSeq.second()).getValue());
							return new VncString(String.format(locale2, fmt, fmtArgs));
						default:
							// language, country, variant
							final Locale locale3 = new Locale(
														Coerce.toVncString(localeSeq.first()).getValue(),
														Coerce.toVncString(localeSeq.second()).getValue(),
														Coerce.toVncString(localeSeq.third()).getValue());
							return new VncString(String.format(locale3, fmt, fmtArgs));
					}
				}
				else {
					final VncString fmt = (VncString)args.first();
					final VncList fmtArgs = args.rest();
					return new VncString(String.format(fmt.getValue(), toJavaObjects(fmtArgs).toArray()));
				}			
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction str_quote = 
		new VncFunction(
				"str/quote", 
				VncFunction
					.meta()
					.module("str")
					.arglists("(str/quote str q)", "(str/quote str start end)")		
					.doc("Quotes a string.")
					.examples(
						"(str/quote \"abc\" \"-\")",
						"(str/quote \"abc\" \"<\" \">\")")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("str/quote", args, 2, 3);
	
				final String s = Coerce.toVncString(args.first()).getValue();
				final String start = Coerce.toVncString(args.second()).getValue();
				final String end = (args.size() == 2) 
										? start 
										: Coerce.toVncString(args.nth(2)).getValue();
	
				return new VncString(start + s + end);
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction str_double_quote = 
			new VncFunction(
					"str/double-quote", 
					VncFunction
						.meta()
						.module("str")
						.arglists("(str/double-quote str)")		
						.doc("Double quotes a string.")
						.examples(
							"(str/double-quote \"abc\")",
							"(str/double-quote \"\")")
						.build()
			) {	
				public VncVal apply(final VncList args) {
					assertArity("str/double-quote", args, 1);
		
					final String s = Coerce.toVncString(args.first()).getValue();
		
					return new VncString("\"" + s + "\"");
				}
		
			    private static final long serialVersionUID = -1848883965231344442L;
			};

	public static VncFunction str_truncate = 
		new VncFunction(
				"str/truncate", 
				VncFunction
					.meta()
					.module("str")
					.arglists("(str/truncate s maxlen marker)")		
					.doc(
						"Truncates a string to the max lenght maxlen and adds the " +
						"marker to the end if the string needs to be truncated")
					.examples(
						"(str/truncate \"abcdefghij\" 20 \"...\")",
						"(str/truncate \"abcdefghij\" 9 \"...\")",
						"(str/truncate \"abcdefghij\" 4 \"...\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("str/truncate", args, 3);
				
				if (args.first() == Nil) {
					return Nil;
				}
				
				return new VncString(
							StringUtil.truncate(
								Coerce.toVncString(args.first()).getValue(), 
								Coerce.toVncLong(args.second()).getValue().intValue(),
								Coerce.toVncString(args.nth(2)).getValue()));		
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction str_strip_start = 
		new VncFunction(
				"str/strip-start", 
				VncFunction
					.meta()
					.module("str")
					.arglists("(str/strip-start s substr)")		
					.doc("Removes a substr only if it is at the beginning of a s, otherwise returns s.")
					.examples(
						"(str/strip-start \"abcdef\" \"abc\")",
						"(str/strip-start \"abcdef\" \"def\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("str/strip-start", args, 2);
				
				if (args.first() == Nil) {
					return Nil;
				}
				
				final String s = Coerce.toVncString(args.first()).getValue();
				final String substr = Coerce.toVncString(args.second()).getValue();
				
				return new VncString(s.startsWith(substr) ? s.substring(substr.length()) : s);
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction str_strip_end = 
		new VncFunction(
				"str/strip-end", 
				VncFunction
					.meta()
					.module("str")
					.arglists("(str/strip-end s substr)")		
					.doc("Removes a substr only if it is at the end of a s, otherwise returns s.")
					.examples(
						"(str/strip-end \"abcdef\" \"def\")",
						"(str/strip-end \"abcdef\" \"abc\")")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("str/strip-end", args, 2);
				
				if (args.first() == Nil) {
					return Nil;
				}
				
				final String s = Coerce.toVncString(args.first()).getValue();
				final String substr = Coerce.toVncString(args.second()).getValue();
				
				return new VncString(s.endsWith(substr) ? s.substring(0, s.length() - substr.length()) : s);
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction str_strip_indent = 
		new VncFunction(
				"str/strip-indent", 
				VncFunction
					.meta()
					.module("str")
					.arglists("(str/strip-indent s)")		
					.doc("Strip the indent of a multi-line string. The first line's leading whitespaces define the indent.")
					.examples("(str/strip-indent \"  line1\n    line2\n    line3\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("str/strip-indent", args, 1);
				
				if (args.first() == Nil) {
					return Nil;
				}
				
				return new VncString(StringUtil.stripIndent(Coerce.toVncString(args.first()).getValue()));
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction str_strip_margin = 
		new VncFunction(
				"str/strip-margin", 
				VncFunction
					.meta()
					.module("str")
					.arglists("(str/strip-margin s)")		
					.doc(
						"Strips leading whitespaces upto and including the margin '|' " +
						"from each line in a multi-line string.")
					.examples(
						"(str/strip-margin \"line1\n  |  line2\n  |  line3\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("str/strip-margin", args, 1);
				
				if (args.first() == Nil) {
					return Nil;
				}
				
				return new VncString(StringUtil.stripMargin(Coerce.toVncString(args.first()).getValue(), '|'));
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction str_repeat = 
		new VncFunction(
				"str/repeat", 
				VncFunction
					.meta()
					.module("str")
					.arglists("(str/repeat s n)", "(str/repeat s n sep)")		
					.doc("Repeats s n times with an optional separator.")
					.examples(
						"(str/repeat \"abc\" 0)",
						"(str/repeat \"abc\" 3)",
						"(str/repeat \"abc\" 3 \"-\")")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("str/repeat", args, 2, 3);
				
				if (args.first() == Nil) {
					return Nil;
				}
				
				final String s = Coerce.toVncString(args.first()).getValue();
				final int times = Coerce.toVncLong(args.second()).getValue().intValue();
				final String sep = args.size() == 3 ? Coerce.toVncString(args.nth(2)).getValue() : "";
				
				final StringBuilder sb = new StringBuilder();
				for(int ii=0; ii<times; ii++) {
					if (ii>0)sb.append(sep);
					sb.append(s);
				}			
				return new VncString(sb.toString());
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction str_digit_Q = 
		new VncFunction(
				"str/digit?", 
				VncFunction
					.meta()
					.module("str")
					.arglists("(str/digit? s)")		
					.doc(
						"True if s is a single char string and the char is a digit. " +
						"Defined by Java Character.isDigit(ch).")
					.examples("(str/digit? \"8\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("str/digit?", args, 1);
	
				final String str = Coerce.toVncString(args.first()).getValue();
				if (str.length() != 1) {
					throw new VncException(String.format(
							"Function 'str/digit?' expects a single char string",
							Types.getType(args.first())));
				}
				return Character.isDigit(str.charAt(0)) ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction str_letter_Q = 
		new VncFunction(
				"str/letter?", 
				VncFunction
					.meta()
					.module("str")
					.arglists("(str/letter? s)")		
					.doc(
						"True if s is a single char string and the char is a letter. " + 
						"Defined by Java Character.isLetter(ch).")
					.examples("(str/letter? \"x\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("str/letter?", args, 1);
				
				final String str = Coerce.toVncString(args.first()).getValue();
				if (str.length() != 1) {
					throw new VncException(String.format(
							"Function 'str/letter?' expects a single char string",
							Types.getType(args.first())));
				}
				return Character.isLetter(str.charAt(0)) ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

		
	public static VncFunction str_linefeed_Q = 
		new VncFunction(
				"str/linefeed?", 
				VncFunction
					.meta()
					.module("str")
					.arglists("(str/linefeed? s)")		
					.doc("True if s is a single char string and the char is a linefeed.")
					.examples("(str/linefeed? \"\n\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("str/linefeed?", args, 1);
				
				final String str = Coerce.toVncString(args.first()).getValue();
				if (str.length() != 1) {
					throw new VncException(String.format(
							"Function 'str/linefeed?' expects a single char string",
							Types.getType(args.first())));
				}
				return str.charAt(0) == '\n' ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction str_whitespace_Q = 
		new VncFunction(
				"str/whitespace?", 
				VncFunction
					.meta()
					.module("str")
					.arglists("(str/whitespace? s)")		
					.doc(
						"True if s is a single char string and the char is a whitespace. " +
						"Defined by Java Character.isWhitespace(ch).")
					.examples("(str/whitespace? \" \")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("str/whitespace?", args, 1);
					
				final String str = Coerce.toVncString(args.first()).getValue();
				if (str.length() != 1) {
					throw new VncException(String.format(
							"Function 'str/whitespace?' expects a single char string",
							Types.getType(args.first())));
				}
				return Character.isWhitespace(str.charAt(0)) ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction str_encode_base64 = 
		new VncFunction(
				"str/encode-base64", 
				VncFunction
					.meta()
					.module("str")
					.arglists("(str/encode-base64 data)")		
					.doc("Base64 encode.")
					.examples("(str/encode-base64 (bytebuf [0 1 2 3 4 5 6]))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("str/encode-base64", args, 1);
				
				final byte[] buf = Coerce.toVncByteBuffer(args.first()).getValue().array();		
				return new VncString(Base64.getEncoder().encodeToString(buf));
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction str_decode_base64 = 
		new VncFunction(
				"str/decode-base64", 
				VncFunction
					.meta()
					.module("str")
					.arglists("(str/decode-base64 s)")		
					.doc("Base64 decode.")
					.examples("(str/decode-base64 (str/encode-base64 (bytebuf [0 1 2 3 4 5 6])))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("str/decode-base64", args, 1);
				
				final String base64 = Coerce.toVncString(args.first()).getValue();		
				return new VncByteBuffer(Base64.getDecoder().decode(base64));
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction str_encode_url = 
		new VncFunction(
				"str/encode-url", 
				VncFunction
					.meta()
					.module("str")
					.arglists("(str/encode-url s)")		
					.doc("URL encode.")
					.examples("(str/encode-url \"The string ü@foo-bar\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("str/encode-url", args, 1);
				
				try {
					final String s = Coerce.toVncString(args.first()).getValue();		
					return new VncString(URLEncoder.encode(s, "UTF-8"));
				}
				catch(UnsupportedEncodingException ex) {
					throw new RuntimeException("Unsupported encoding", ex);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction str_decode_url = 
		new VncFunction(
				"str/decode-url", 
				VncFunction
					.meta()
					.module("str")
					.arglists("(str/decode-url s)")		
					.doc("URL decode.")
					.examples("(str/decode-url \"The+string+%C3%BC%40foo-bar\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("str/decode-url", args, 1);
				
				try {
					final String s = Coerce.toVncString(args.first()).getValue();		
					return new VncString(URLDecoder.decode(s, "UTF-8"));
				}
				catch(UnsupportedEncodingException ex) {
					throw new RuntimeException("Unsupported encoding", ex);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction str_escape_html = 
		new VncFunction(
				"str/escape-html", 
				VncFunction
					.meta()
					.module("str")
					.arglists("(str/escape-html s)")		
					.doc("HTML escape. Escapes &<>\"' and the non blocking space U+00A0")
					.examples("(str/escape-html \"1 2 3 & < > \\\" '\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("str/escape-html", args, 1);
				
				final String s = Coerce.toVncString(args.first()).getValue();
				return new VncString(replace(s, HTML_ESCAPES));
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction str_escape_xml = 
		new VncFunction(
				"str/escape-xml", 
				VncFunction
					.meta()
					.module("str")
					.arglists("(str/escape-xml s)")		
					.doc("XML escape. Escapes &<>\"'")
					.examples("(str/escape-xml \"1 2 3 & < > \\\" '\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("str/escape-xml", args, 1);
				
				final String s = Coerce.toVncString(args.first()).getValue();
				return new VncString(replace(s, XML_ESCAPES));
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};


	private static List<Object> toJavaObjects(final VncList list) {
		return list
				.getList()
				.stream()
				.map(v -> v.convertToJavaObject())
				.collect(Collectors.toList());
	}
	
	private static String replace(final String str, final List<Tuple2<String,String>> replacements) {
		if (str == null || str.isEmpty()) {
			return str;
		}
		
		String s = str;
		for(Tuple2<String,String> r : replacements) {
			s = s.replace(r._1, r._2);
		}
		return s;
	}
	
	private static final List<Tuple2<String,String>> XML_ESCAPES =
			Arrays.asList(
					Tuple2.of("&", "&amp;"),
					Tuple2.of("<", "&lt;"),
					Tuple2.of(">", "&gt;"),
					Tuple2.of("\"", "&quot;"),
					Tuple2.of("'", "&apos;"));

	private static final List<Tuple2<String,String>> HTML_ESCAPES =
			Arrays.asList(
					Tuple2.of("&", "&amp;"),
					Tuple2.of("<", "&lt;"),
					Tuple2.of(">", "&gt;"),
					Tuple2.of("\"", "&quot;"),
					Tuple2.of("'", "&apos;"),
					Tuple2.of("\u00A0", "&nbsp;"));

	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap.Builder()
					.put("str/blank?",				str_blank_Q)
					.put("str/starts-with?",		str_starts_with_Q)
					.put("str/ends-with?",			str_ends_with_Q)
					.put("str/contains?",			str_contains_Q)
					.put("str/equals-ignore-case?",	str_equals_ignore_case_Q)			
					.put("str/digit?",				str_digit_Q)
					.put("str/letter?",				str_letter_Q)
					.put("str/linefeed?",			str_linefeed_Q)
					.put("str/whitespace?",			str_whitespace_Q)
					.put("str/trim",				str_trim)
					.put("str/trim-to-nil",			str_trim_to_nil)
					.put("str/index-of",			str_index_of)
					.put("str/last-index-of",		str_last_index_of)
					.put("str/replace-first",		str_replace_first)
					.put("str/replace-last",		str_replace_last)
					.put("str/replace-all",			str_replace_all)
					.put("str/lower-case",			str_lower_case)
					.put("str/upper-case",			str_upper_case)
					.put("str/join",				str_join)
					.put("str/subs",				str_subs)
					.put("str/split",				str_split)
					.put("str/split-lines",			str_split_lines)
					.put("str/format",				str_format)
					.put("str/quote",				str_quote)
					.put("str/double-quote",		str_double_quote)
					.put("str/truncate",			str_truncate)
					.put("str/strip-start",			str_strip_start)
					.put("str/strip-end",			str_strip_end)
					.put("str/strip-indent",		str_strip_indent)
					.put("str/strip-margin",		str_strip_margin)
					.put("str/repeat",				str_repeat)
					.put("str/char",				str_char)
					.put("str/encode-base64",		str_encode_base64)
					.put("str/decode-base64",		str_decode_base64)
					.put("str/encode-url",			str_encode_url)
					.put("str/decode-url",			str_decode_url)
					.put("str/escape-html",			str_escape_html)
					.put("str/escape-xml",			str_escape_xml)
					.toMap();	
}
