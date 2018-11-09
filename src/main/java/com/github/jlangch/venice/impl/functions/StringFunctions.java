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
package com.github.jlangch.venice.impl.functions;

import static com.github.jlangch.venice.impl.functions.FunctionsUtil.assertArity;
import static com.github.jlangch.venice.impl.types.Constants.False;
import static com.github.jlangch.venice.impl.types.Constants.Nil;
import static com.github.jlangch.venice.impl.types.Constants.True;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.javainterop.JavaInteropUtil;
import com.github.jlangch.venice.impl.types.Coerce;
import com.github.jlangch.venice.impl.types.Types;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.util.StringUtil;


public class StringFunctions {

	///////////////////////////////////////////////////////////////////////////
	// String
	///////////////////////////////////////////////////////////////////////////
	
	public static VncFunction str_blank = new VncFunction("str/blank?") {
		{
			setArgLists("(str/blank? s)");
			
			setDoc("True if s is blank.");
			
			setExamples(
					"(str/blank? nil)", 
					"(str/blank? \"\")", 
					"(str/blank? \"  \")", 
					"(str/blank? \"abc\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/blank?", args, 1);

			if (args.nth(0) == Nil) {
				return True;
			}
			
			final String s = Coerce.toVncString(args.nth(0)).getValue();		

			return StringUtil.isBlank(s) ? True : False;
		}

	    private static final long serialVersionUID = -1848883965231344442L;
	};

	public static VncFunction str_char = new VncFunction("str/char") {
		{
			setArgLists("(str/char n)");
			
			setDoc("Converts a number to a single char string.");
			
			setExamples("(str/char 65)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/char", args, 1);

			if (args.nth(0) == Nil) {
				return Nil;
			}
			
			final long n = Coerce.toVncLong(args.nth(0)).getValue();		

			return new VncString(String.valueOf((char)n));
		}

	    private static final long serialVersionUID = -1848883965231344442L;
	};

	public static VncFunction str_starts_with = new VncFunction("str/starts-with?") {
		{
			setArgLists("(str/starts-with? s substr)");
			
			setDoc("True if s starts with substr.");
			
			setExamples(
					"(str/starts-with? \"abc\"  \"ab\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/starts-with?", args, 2);

			if (args.nth(0) == Nil || args.nth(1) == Nil) {
				return False;
			}
			
			final VncString string = Coerce.toVncString(args.nth(0));		
			final VncString prefix = Coerce.toVncString(args.nth(1));		
			
			return string.getValue().startsWith(prefix.getValue()) ? True : False;
		}

	    private static final long serialVersionUID = -1848883965231344442L;
	};
	
	public static VncFunction str_ends_with = new VncFunction("str/ends-with?") {
		{
			setArgLists("(str/ends-with? s substr)");
			
			setDoc("True if s ends with substr.");
			
			setExamples(
					"(str/starts-with? \"abc\"  \"bc\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/ends-with?", args, 2);

			if (args.nth(0) == Nil || args.nth(1) == Nil) {
				return False;
			}

			final VncString string = Coerce.toVncString(args.nth(0));
			final VncString suffix = Coerce.toVncString(args.nth(1));
			
			return string.getValue().endsWith(suffix.getValue()) ? True : False;
		}

	    private static final long serialVersionUID = -1848883965231344442L;
	};
	
	public static VncFunction str_contains = new VncFunction("str/contains?") {
		{
			setArgLists("(str/contains? s substr)");
			
			setDoc("True if s contains with substr.");
			
			setExamples(
					"(str/contains? \"abc\"  \"ab\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/contains?", args, 2);

			if (args.nth(0) == Nil || args.nth(1) == Nil) {
				return False;
			}

			final VncString string = Coerce.toVncString(args.nth(0));
			final VncString text = Coerce.toVncString(args.nth(1));
			
			return string.getValue().contains(text.getValue()) ? True : False;
		}

	    private static final long serialVersionUID = -1848883965231344442L;
	};
	
	public static VncFunction str_trim = new VncFunction("str/trim") {
		{
			setArgLists("(str/trim s substr)");
			
			setDoc("Trims leading and trailing spaces from s.");
			
			setExamples("(str/trim \" abc  \")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/trim", args, 1);

			if (args.nth(0) == Nil) {
				return Nil;
			}

			return new VncString(Coerce.toVncString(args.nth(0)).getValue().trim());
		}

	    private static final long serialVersionUID = -1848883965231344442L;
	};
	
	public static VncFunction str_trim_to_nil = new VncFunction("str/trim-to-nil") {
		{
			setArgLists("(str/trim-to-nil s substr)");
			
			setDoc( "Trims leading and trailing spaces from s. " +
					"Returns nil if the rewsulting string is empry");
			
			setExamples(
					"(str/trim \"\")",
					"(str/trim \"    \")",
					"(str/trim nil)",
					"(str/trim \" abc   \")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/trim-to-nil", args, 1);

			if (args.nth(0) == Nil) {
				return Nil;
			}

			final String str = Coerce.toVncString(args.nth(0)).getValue().trim();
			return str.isEmpty() ? Nil : new VncString(str);
		}

	    private static final long serialVersionUID = -1848883965231344442L;
	};
	
	public static VncFunction str_index_of = new VncFunction("str/index-of") {
		{
			setArgLists("(str/index-of s value)", "(str/index-of s value from-index)");
			
			setDoc( "Return index of value (string or char) in s, optionally searching " + 
					"forward from from-index. Return nil if value not found.");
			
			setExamples(
					"(str/index-of \"abcdefabc\" \"ab\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/index-of", args, 2, 3);

			final String text = Coerce.toVncString(args.nth(0)).getValue();	
			final String searchString = Coerce.toVncString(args.nth(1)).getValue();		
			
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
	
	public static VncFunction str_last_index_of = new VncFunction("str/last-index-of") {
		{
			setArgLists("(str/last-index-of s value)", "(str/last-index-of s value from-index)");
			
			setDoc( "Return last index of value (string or char) in s, optionally\n" + 
					"searching backward from from-index. Return nil if value not found.");
			
			setExamples(
					"(str/last-index-of \"abcdefabc\" \"ab\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/last-index-of", args, 2, 3);

			if (args.nth(0) == Nil) {
				return Nil;
			}

			final String text = Coerce.toVncString(args.nth(0)).getValue();	
			final String searchString = Coerce.toVncString(args.nth(1)).getValue();		
			
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
	
	public static VncFunction str_replace_first = new VncFunction("str/replace-first") {
		{
			setArgLists("(str/replace-first s search replacement)");
			
			setDoc("Replaces the first occurrance of search in s");
			
			setExamples(
					"(str/replace-first \"abcdefabc\" \"ab\" \"XYZ\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/replace-first", args, 3);

			if (args.nth(0) == Nil) {
				return Nil;
			}

			final String text = Coerce.toVncString(args.nth(0)).getValue();	
			final String searchString = Coerce.toVncString(args.nth(1)).getValue();
			final String replacement = Coerce.toVncString(args.nth(2)).getValue();

			if (StringUtil.isEmpty(text) || StringUtil.isEmpty(searchString) || replacement == null) {
				return args.nth(0);
			}

			int pos = text.indexOf(searchString);
			return pos >= 0
				? new VncString(
						text.substring(0, pos) + 
						replacement + 
						text.substring(pos + replacement.length()))
			 	: args.nth(0);
		}

	    private static final long serialVersionUID = -1848883965231344442L;
	};
	
	public static VncFunction str_replace_last = new VncFunction("str/replace-last") {
		{
			setArgLists("(str/replace-last s search replacement)");
			
			setDoc("Replaces the last occurrance of search in s");
			
			setExamples(
					"(str/replace-last \"abcdefabc\" \"ab\" \"XYZ\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/replace-last", args, 3);

			if (args.nth(0) == Nil) {
				return Nil;
			}

			final String text = Coerce.toVncString(args.nth(0)).getValue();	
			final String searchString = Coerce.toVncString(args.nth(1)).getValue();
			final String replacement = Coerce.toVncString(args.nth(2)).getValue();

			if (StringUtil.isEmpty(text) || StringUtil.isEmpty(searchString) || replacement == null) {
				return args.nth(0);
			}

			int pos = text.lastIndexOf(searchString);
			return pos >= 0
				? new VncString(
						text.substring(0, pos) + 
						replacement + 
						text.substring(pos + replacement.length()))
			 	: args.nth(0);
		}

	    private static final long serialVersionUID = -1848883965231344442L;
	};
	
	public static VncFunction str_replace_all = new VncFunction("str/replace-all") {
		{
			setArgLists("(str/replace-all s search replacement)");
			
			setDoc("Replaces the all occurrances of search in s");
			
			setExamples(
					"(str/replace-all \"abcdefabc\" \"ab\" \"XYZ\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/replace-all", args, 3);

			if (args.nth(0) == Nil) {
				return Nil;
			}

			final String text = Coerce.toVncString(args.nth(0)).getValue();	
			final String searchString = Coerce.toVncString(args.nth(1)).getValue();		
			final String replacement = Coerce.toVncString(args.nth(2)).getValue();		
			
			if (StringUtil.isEmpty(text) || StringUtil.isEmpty(searchString) || replacement == null) {
				return args.nth(0);
			}

			int start = 0;
			int end = text.indexOf(searchString, start);
			if (end == -1) {
				return args.nth(0);
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

	    private static final long serialVersionUID = -1848883965231344442L;
	};
	
	public static VncFunction str_lower_case = new VncFunction("str/lower-case") {
		{
			setArgLists("(str/lower-case s)");
			
			setDoc("Converts s to lowercase");
			
			setExamples(
					"(str/lower-case \"aBcDeF\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/lower-case", args, 1);

			if (args.nth(0) == Nil) {
				return Nil;
			}

			final VncString string = Coerce.toVncString(args.nth(0));
			
			return new VncString(string.getValue().toLowerCase());
		}

	    private static final long serialVersionUID = -1848883965231344442L;
	};
	
	public static VncFunction str_upper_case = new VncFunction("str/upper-case") {
		{
			setArgLists("(str/upper-case s)");
			
			setDoc("Converts s to uppercase");
			
			setExamples(
					"(str/upper-case \"aBcDeF\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/upper-case", args, 1);

			if (args.nth(0) == Nil) {
				return Nil;
			}

			final VncString string = Coerce.toVncString(args.nth(0));
			
			return new VncString(string.getValue().toUpperCase());
		}

	    private static final long serialVersionUID = -1848883965231344442L;
	};
	
	public static VncFunction str_join = new VncFunction("str/join") {
		{
			setArgLists("(str/join coll)", "(str/join separator coll)");
			
			setDoc("Joins all elements in coll separated by an optional separator.");
			
			setExamples(
					"(str/join [1 2 3])",
					"(str/join \"-\" [1 2 3])");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/join", args, 1, 2);

			final VncList coll = Coerce.toVncList(args.last());		
			final VncString delim = args.size() == 2 ? Coerce.toVncString(args.nth(0)) : new VncString("");
			
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
	
	public static VncFunction str_subs = new VncFunction("str/subs") {
		{
			setArgLists("(str/subs s start)", "(str/subs s start end)");
			
			setDoc( "Returns the substring of s beginning at start inclusive, and ending " + 
					"at end (defaults to length of string), exclusive.");
			
			setExamples(
					"(str/subs \"abcdef\" 2)",
					"(str/subs \"abcdef\" 2 5)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/subs", args, 2, 3);

			final VncString string = Coerce.toVncString(args.nth(0));		
			final VncLong from = Coerce.toVncLong(args.nth(1));
			final VncLong to = args.size() > 2 ? (VncLong)args.nth(2) : null;
			
			return new VncString(
							to == null
								? string.getValue().substring(from.getValue().intValue())
								: string.getValue().substring(from.getValue().intValue(), to.getValue().intValue()));
		}

	    private static final long serialVersionUID = -1848883965231344442L;
	};
	
	public static VncFunction str_split = new VncFunction("str/split") {
		{
			setArgLists("(str/split s regex)");
			
			setDoc("Splits string on a regular expression.");
			
			setExamples("(str/split \"abc , def , ghi\" \"[ *],[ *]\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/split", args, 2);

			final VncString string = Coerce.toVncString(args.nth(0));
			final VncString regex = Coerce.toVncString(args.nth(1));
			
			return new VncList(
					Arrays
						.asList(string.getValue().split(regex.getValue()))
						.stream()
						.map(s -> new VncString(s))
						.collect(Collectors.toList()));			
		}

	    private static final long serialVersionUID = -1848883965231344442L;
	};
	
	public static VncFunction str_split_lines = new VncFunction("str/split-lines") {
		{
			setArgLists("(str/split-lines s)");
			
			setDoc("Splits s into lines.");
			
			setExamples("(str/split-lines \"line1\nline2\nline3\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/split-lines", args, 1);

			return args.nth(0) == Nil
					? new VncList()
					: new VncList(
							StringUtil
								.splitIntoLines(Coerce.toVncString(args.nth(0)).getValue())
								.stream()
								.map(s -> new VncString(s))
								.collect(Collectors.toList()));
		}

	    private static final long serialVersionUID = -1848883965231344442L;
	};
	
	public static VncFunction str_format = new VncFunction("str/format") {
		{
			setArgLists("(str/format format args*)");
			
			setDoc("Returns a formatted string using the specified format string and arguments.");
			
			setExamples("(str/format \"%s: %d\" \"abc\" 100)");
		}
		
		public VncVal apply(final VncList args) {
			final VncString fmt = (VncString)args.nth(0);
			final List<Object> fmtArgs = args
										.slice(1)
										.getList()
										.stream()
										.map(v -> JavaInteropUtil.convertToJavaObject(v))
										.collect(Collectors.toList());
			
			return new VncString(String.format(fmt.getValue(), fmtArgs.toArray()));
		}

	    private static final long serialVersionUID = -1848883965231344442L;
	};
	
	public static VncFunction str_quote = new VncFunction("str/quote") {
		{
			setArgLists("(str/quote str q)", "(str/quote str start end)");
			
			setDoc("Quotes a string.");
			
			setExamples(
					"(str/quote \"abc\" \"-\")",
					"(str/quote \"abc\" \"<\" \">\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/quote", args, 2, 3);

			final String s = Coerce.toVncString(args.nth(0)).getValue();
			final String start = Coerce.toVncString(args.nth(1)).getValue();
			final String end = (args.size() == 2) 
									? start 
									: Coerce.toVncString(args.nth(2)).getValue();

			return new VncString(start + s + end);
		}

	    private static final long serialVersionUID = -1848883965231344442L;
	};
	
	public static VncFunction str_truncate = new VncFunction("str/truncate") {
		{
			setArgLists("(str/truncate s maxlen marker)");
			
			setDoc( "Truncates a string to the max lenght maxlen and adds the " +
					"marker to the end if the string needs to be truncated");
			
			setExamples(
					"(str/truncate \"abcdefghij\" 20 \"...\")",
					"(str/truncate \"abcdefghij\" 9 \"...\")",
					"(str/truncate \"abcdefghij\" 4 \"...\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/truncate", args, 3);
			
			if (args.nth(0) == Nil) {
				return Nil;
			}
			
			return new VncString(
						StringUtil.truncate(
							Coerce.toVncString(args.nth(0)).getValue(), 
							Coerce.toVncLong(args.nth(1)).getValue().intValue(),
							Coerce.toVncString(args.nth(2)).getValue()));		
		}

	    private static final long serialVersionUID = -1848883965231344442L;
	};

	public static VncFunction str_strip_start = new VncFunction("str/strip-start") {
		{
			setArgLists("(str/strip-start s substr)");
			
			setDoc("Removes a substr only if it is at the beginning of a s, otherwise returns s.");
			
			setExamples(
					"(str/strip-start \"abcdef\" \"abc\")",
					"(str/strip-start \"abcdef\" \"def\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/strip-start", args, 2);
			
			if (args.nth(0) == Nil) {
				return Nil;
			}
			
			final String s = Coerce.toVncString(args.nth(0)).getValue();
			final String substr = Coerce.toVncString(args.nth(1)).getValue();
			
			return new VncString(s.startsWith(substr) ? s.substring(substr.length()) : s);
		}

	    private static final long serialVersionUID = -1848883965231344442L;
	};

	public static VncFunction str_strip_end = new VncFunction("str/strip-end") {
		{
			setArgLists("(str/strip-end s substr)");
			
			setDoc("Removes a substr only if it is at the end of a s, otherwise returns s.");
			
			setExamples(
					"(str/strip-end \"abcdef\" \"def\")",
					"(str/strip-end \"abcdef\" \"abc\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/strip-end", args, 2);
			
			if (args.nth(0) == Nil) {
				return Nil;
			}
			
			final String s = Coerce.toVncString(args.nth(0)).getValue();
			final String substr = Coerce.toVncString(args.nth(1)).getValue();
			
			return new VncString(s.endsWith(substr) ? s.substring(0, s.length() - substr.length()) : s);
		}

	    private static final long serialVersionUID = -1848883965231344442L;
	};

	public static VncFunction str_strip_indent = new VncFunction("str/strip-indent") {
		{
			setArgLists("(str/strip-indent s)");
			
			setDoc("Strip the indent of a multi-line string. The first line's leading whitespaces define the indent.");
			
			setExamples(
					"(str/strip-indent \"  line1\n    line2\n    line3\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/strip-indent", args, 1);
			
			if (args.nth(0) == Nil) {
				return Nil;
			}
			
			return new VncString(StringUtil.stripIndent(Coerce.toVncString(args.first()).getValue()));
		}

	    private static final long serialVersionUID = -1848883965231344442L;
	};

	public static VncFunction str_strip_margin = new VncFunction("str/strip-margin") {
		{
			setArgLists("(str/strip-margin s)");
			
			setDoc("Strips leading whitespaces upto and including the margin '|' " +
					"from each line in a multi-line string.");
			
			setExamples(
					"(str/strip-margin \"line1\n  |  line2\n  |  line3\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/strip-margin", args, 1);
			
			if (args.nth(0) == Nil) {
				return Nil;
			}
			
			return new VncString(StringUtil.stripMargin(Coerce.toVncString(args.first()).getValue(), '|'));
		}

	    private static final long serialVersionUID = -1848883965231344442L;
	};

	public static VncFunction str_repeat = new VncFunction("str/repeat") {
		{
			setArgLists("(str/repeat s n)", "(str/repeat s n sep)");
			
			setDoc("Repeats s n times with an optional separator.");
			
			setExamples(
					"(str/repeat \"abc\" 0)",
					"(str/repeat \"abc\" 3)",
					"(str/repeat \"abc\" 3 \"-\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/repeat", args, 2, 3);
			
			if (args.nth(0) == Nil) {
				return Nil;
			}
			
			final String s = Coerce.toVncString(args.nth(0)).getValue();
			final int times = Coerce.toVncLong(args.nth(1)).getValue().intValue();
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

	
	
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap.Builder()
					.put("str/blank?",			str_blank)
					.put("str/starts-with?",	str_starts_with)
					.put("str/ends-with?",		str_ends_with)
					.put("str/contains?",		str_contains)
					.put("str/trim",			str_trim)
					.put("str/trim-to-nil",		str_trim_to_nil)
					.put("str/index-of",		str_index_of)
					.put("str/last-index-of",	str_last_index_of)
					.put("str/replace-first",	str_replace_first)
					.put("str/replace-last",	str_replace_last)
					.put("str/replace-all",		str_replace_all)
					.put("str/lower-case",		str_lower_case)
					.put("str/upper-case",		str_upper_case)
					.put("str/join",			str_join)
					.put("str/subs",			str_subs)
					.put("str/split",			str_split)
					.put("str/split-lines",		str_split_lines)
					.put("str/format",			str_format)
					.put("str/quote",			str_quote)
					.put("str/truncate",		str_truncate)
					.put("str/strip-start",		str_strip_start)
					.put("str/strip-end",		str_strip_end)
					.put("str/strip-indent",	str_strip_indent)
					.put("str/strip-margin",	str_strip_margin)
					.put("str/repeat",			str_repeat)
					.put("str/char",			str_char)
					.toMap();	
}
