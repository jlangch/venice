/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2021 Venice
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

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions;


public class RegexFunctions {

	///////////////////////////////////////////////////////////////////////////
	// Regex
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction pattern = 
			new VncFunction(
					"regex/pattern", 
					VncFunction
						.meta()
						.arglists("(regex/pattern s)")		
						.doc(
							"Returns an instance of `java.util.regex.Pattern`.\n\n"  +
							"JavaDoc: [Pattern](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html)")
						.examples("(regex/pattern \"[0-9]+\")")
						.seeAlso("regex/matcher", "regex/matches", "regex/find", "regex/find-all")
						.build()
			) {		
				public VncVal apply(final VncList args) {
					ArityExceptions.assertArity(this, args, 1);
		
					// "[Regex Pattern](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html)"
					return new VncJavaObject(
							Pattern.compile(
									Coerce.toVncString(args.first()).getValue()));
				}
		
				private static final long serialVersionUID = -1848883965231344442L;
			};

	public static VncFunction matcher = 
		new VncFunction(
				"regex/matcher", 
				VncFunction
					.meta()
					.arglists("(regex/matcher pattern str)")		
					.doc(
						"Returns an instance of `java.util.regex.Matcher`.¶" +
						"The pattern can be either a string or a pattern created by `(regex/pattern s)`.\n\n" +
						"JavaDoc: [Pattern](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html)")
					.examples(
						"(regex/matcher \"[0-9]+\" \"100\")",
						"(let [p (regex/pattern \"[0-9]+\")] \n" +
						"   (regex/matcher p \"100\"))")
					.seeAlso(
						"regex/pattern", "regex/matches?", "regex/find?", "regex/reset", 
						"regex/matches", "regex/find", "regex/find-all")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 2);

				final VncVal pattern = args.first();
				final Pattern p = Types.isVncString(pattern)
									? Pattern.compile(((VncString)pattern).getValue())	
									: (Pattern)Coerce.toVncJavaObject(args.first()).getDelegate();		
				final String s = Coerce.toVncString(args.second()).getValue();	
				return new VncJavaObject(p.matcher(s));
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction matches_Q = 
		new VncFunction(
				"regex/matches?", 
				VncFunction
					.meta()
					.arglists("(regex/matches? matcher)")		
					.doc(
						"Attempts to match the entire region against the pattern. " +
						"If the match succeeds then more information can be obtained " +
						"via the regex/group function")
					.examples(
						"(let [m (regex/matcher \"[0-9]+\" \"100\")]  \n" +
						"  (regex/matches? m))",
						"(let [m (regex/matcher \"[0-9]+\" \"value: 100\")]  \n" +
						"  (regex/matches? m))")
					.seeAlso("regex/matcher", "regex/matches")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);
	
				final Matcher m = (Matcher)Coerce.toVncJavaObject(args.first()).getDelegate();		
	
				return VncBoolean.of(m.matches());
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction matches = 
		new VncFunction(
				"regex/matches", 
				VncFunction
					.meta()
					.arglists("(regex/matches pattern str)")		
					.doc(
						"Returns the matches, if any, for the matcher with the pattern of a " + 
						"string, using `java.util.regex.Matcher.matches()`.¶" +
						"If the matcher's pattern matches the entire region sequence returns a " +
						"list with the entire region sequence and the matched groups otherwise " +
						"returns an empty list. \n\n" +
						"Returns matching info as meta data on the region and the groups. \n\n" +
						"Region meta data: \n\n" +
						"| :start       | start pos of the overall group        |\n" +
						"| :end         | end pos of the overall group          |\n" +
						"| :group-count | the number of matched elements groups |\n\n" +
						"Group meta data: \n\n" +
						"| :start | start pos of the element group |\n" +
						"| :end   | end pos of the element group   |\n\n" +
						"JavaDoc: [Pattern](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html)")
					.examples(
						";; Entire region sequence matched \n" +
						"(regex/matches \"hello, (.*)\" \"hello, world\")",

						";; Entire region sequence not matched \n" +
						"(regex/matches \"HEllo, (.*)\" \"hello, world\")",

						";; Matching multiple groups\n" +
						"(regex/matches \"([0-9]+)-([0-9]+)-([0-9]+)-([0-9]+)\" \"672-345-456-212\")",
						
						";; Matching multiple groups\n" +
						"(let [p (regex/pattern \"([0-9]+)-([0-9]+)\")]\n" +
						"  (regex/matches p \"672-345\"))",
					
						";; Access matcher's region meta info \n" +
						"(let [pattern \"([0-9]+)-([0-9]+)-([0-9]+)-([0-9]+)\" \n" +
						"      matches (regex/matches pattern \"672-345-456-212\")] \n" +
						"   (println \"meta info:\" (pr-str (meta matches))) \n" +
						"   (println \"matches:  \" (pr-str matches)))",
						
						";; Access matcher's region meta info and the meta info of each group \n" +
						"(let [pattern \"([0-9]+)-([0-9]+)-([0-9]+)-([0-9]+)\" \n" +
						"      matches (regex/matches pattern \"672-345-456-212\")] \n" +
						"  (println \"region info:   \" (pr-str (meta matches))) \n" +
						"  (println \"group count:   \" (count matches) \"(region included)\") \n" +
						"  (println \"group matches: \" (pr-str (nth matches 0)) (meta (nth matches 0))) \n" +
						"  (println \"               \" (pr-str (nth matches 1)) (meta (nth matches 1))) \n" +
						"  (println \"               \" (pr-str (nth matches 2)) (meta (nth matches 2))) \n" +
						"  (println \"               \" (pr-str (nth matches 3)) (meta (nth matches 3))) \n" +
						"  (println \"               \" (pr-str (nth matches 4)) (meta (nth matches 4))))")
					.seeAlso("regex/pattern")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 2);
	
				final VncVal pattern = args.first();
				final Pattern p = Types.isVncString(pattern)
									? Pattern.compile(((VncString)pattern).getValue())	
									: (Pattern)Coerce.toVncJavaObject(args.first()).getDelegate();		
				final String s = Coerce.toVncString(args.second()).getValue();	
				final Matcher m = p.matcher(s);

				if (m.matches()) {
					final List<VncVal> groups = new ArrayList<>();

					final VncMap metaGroup = VncHashMap.of(
												new VncKeyword("start"),       new VncLong(m.start()),
												new VncKeyword("end"),         new VncLong(m.end()),
												new VncKeyword("group-count"), new VncLong(m.groupCount()));

					for(int ii=0; ii<=m.groupCount(); ii++) {
						final VncMap metaItem = VncHashMap.of(
												new VncKeyword("start"), new VncLong(m.start(ii)),
												new VncKeyword("end"),   new VncLong(m.end(ii)));
						
						final String group = m.group(ii);
						groups.add(group == null ? Nil : new VncString(group, metaItem));
					}
					
					return VncList.ofList(groups, metaGroup);
				}
				else {
					return VncList.empty();
				}
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction find_Q = 
		new VncFunction(
				"regex/find?", 
				VncFunction
					.meta()
					.arglists("(regex/find? matcher)")		
					.doc(
						"Attempts to find the next subsequence that matches the pattern. " +
						"If the match succeeds then more information can be obtained via " +
						"the `regex/group` function")
					.examples(
						"(let [m (regex/matcher \"[0-9]+\" \"100\")] \n" +
						"  (regex/find? m))",
						
						"(let [m (regex/matcher \"[0-9]+\" \"xxx: 100\")] \n" +
						"  (regex/find? m))",
						
						"(let [m (regex/matcher \"[0-9]+\" \"xxx: 100 200\")] \n" +
						"  (when (regex/find? m) \n" +
						"    (println (regex/group m 0))) \n" +
						"  (when (regex/find? m) \n" +
						"    (println (regex/group m 0))) \n" +
						"  (when (regex/find? m) \n" +
						"    (println (regex/group m 0))))")
					.seeAlso("regex/group", "regex/matches?")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);
	
				final Matcher m = (Matcher)Coerce.toVncJavaObject(args.first()).getDelegate();		
	
				return VncBoolean.of(m.find());
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction find = 
		new VncFunction(
				"regex/find", 
				VncFunction
					.meta()
					.arglists("(regex/find matcher)")		
					.doc(
						"Returns the next regex match or nil if there is no further match. \n\n" +
						"To get the positional data for the matched group use `(regex/find+ matcher)`.")
					.examples(
						"(let [m (regex/matcher \"[0-9]+\" \"672-345-456-3212\")]  \n" +
						"  (println (regex/find m))  \n" +
						"  (println (regex/find m))  \n" +
						"  (println (regex/find m))  \n" +
						"  (println (regex/find m))  \n" +
						"  (println (regex/find m)))")
					.seeAlso("regex/find-all", "regex/find+", "regex/matcher")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);
	
				final Matcher m = (Matcher)Coerce.toVncJavaObject(args.first()).getDelegate();		
				if (m.find()) {
					return new VncString(m.group());
				}
				else {
					return Nil;
				}
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction find_plus = 
		new VncFunction(
				"regex/find+", 
				VncFunction
					.meta()
					.arglists("(regex/find+ matcher)")		
					.doc(
						"Returns the next regex match and returns the group with " +
						"its positional data.")
					.examples(
						"(let [m (regex/matcher \"[0-9]+\" \"672-345-456-3212\")]  \n" +
						"   (println (regex/find+ m))  \n" +
						"   (println (regex/find+ m))  \n" +
						"   (println (regex/find+ m))  \n" +
						"   (println (regex/find+ m))  \n" +
						"   (println (regex/find+ m))) \n")
					.seeAlso("regex/find-all+", "regex/find", "regex/matcher")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);
	
				final Matcher m = (Matcher)Coerce.toVncJavaObject(args.first()).getDelegate();		
				if (m.find()) {
					return VncHashMap.of(
							new VncKeyword("start"), new VncLong(m.start()),
							new VncKeyword("end"),  new VncLong(m.end()),
							new VncKeyword("group"), new VncString(m.group()));
				}
				else {
					return Nil;
				}
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction find_all = 
		new VncFunction(
				"regex/find-all", 
				VncFunction
					.meta()
					.arglists("(regex/find-all matcher)")		
					.doc(
						"Returns all regex matches.\n\n" +
						"To get the positional data for the matched groups use 'regex/find-all+'.")
					.examples(
						"(->> (regex/matcher \"\\\\d+\" \"672-345-456-3212\") \n" +
						"     (regex/find-all))                                 ",
						"(->> (regex/matcher \"([^\\\"]\\\\S*|\\\".+?\\\")\\\\s*\" \"1 2 \\\"3 4\\\" 5\") \n" +
						"     (regex/find-all))                                 ")
					.seeAlso("regex/find", "regex/find-all+", "regex/matcher")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);
	
				final Matcher m = (Matcher)Coerce.toVncJavaObject(args.first()).getDelegate();		
				final List<VncVal> matches = new ArrayList<>();
				while (m.find()) {
					matches.add(new VncString(m.group()));
				}
				return VncList.ofList(matches);
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};


	public static VncFunction find_all_plus = 
		new VncFunction(
				"regex/find-all+", 
				VncFunction
					.meta()
					.arglists("(regex/find-all+ matcher)")		
					.doc(
						"Returns the all regex matches and returns the groups " +
						"with its positional data")
					.examples(
						"(let [m (regex/matcher \"[0-9]+\" \"672-345-456-3212\")]  \n" +
						"  (regex/find-all+ m))  \n")
					.seeAlso("regex/find+", "regex/find-all", "regex/matcher")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);
	
				final Matcher m = (Matcher)Coerce.toVncJavaObject(args.first()).getDelegate();
				final List<VncVal> groups = new ArrayList<>();
				while (m.find()) {
					groups.add(
							VncHashMap.of(
								new VncKeyword("start"), new VncLong(m.start()),
								new VncKeyword("end"),  new VncLong(m.end()),
								new VncKeyword("group"), new VncString(m.group())));
				}
				return VncList.ofList(groups);
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction reset = 
		new VncFunction(
				"regex/reset", 
				VncFunction
					.meta()
					.arglists("(regex/reset matcher str)")		
					.doc(
						"Resets the matcher with a new string")
					.examples(
						"(do  \n" +
						"  (let [m (regex/matcher \"[0-9]+\" \"100\")]  \n" +
						"    (println (regex/find m))                   \n" +
						"    (let [m (regex/reset m \"200\")]           \n" +
						"      (println (regex/find m)))))" )
					.seeAlso("regex/matcher")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 2);
	
				final Matcher m = (Matcher)Coerce.toVncJavaObject(args.first()).getDelegate();		
				final String s = Coerce.toVncString(args.second()).getValue();		
	
				return new VncJavaObject(m.reset(s));
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction group = 
		new VncFunction(
				"regex/group", 
				VncFunction
					.meta()
					.arglists("(regex/group matcher group)")		
					.doc(
						"Returns the input subsequence captured by the given group during the " + 
						"previous match operation.")
					.examples(
						"(let [p (regex/pattern \"([0-9]+)(.*)\")      \n" +
						"      m (regex/matcher p \"100abc\")]         \n" +
						"   (if (regex/matches? m)                     \n" +
						"      [(regex/group m 1) (regex/group m 2)]   \n" +
						"      []))                                      ")
					.seeAlso("regex/matcher", "regex/matches?")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 2);
	
				final Matcher m = (Matcher)Coerce.toVncJavaObject(args.first()).getDelegate();		
				final int g = Coerce.toVncLong(args.second()).getValue().intValue();		
					
				if (g >= 0 && g <= m.groupCount()) {
					final String group = m.group(g);
					return group == null ? Nil : new VncString(group);
				}
				else {
					return Nil;
				}
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncFunction count = 
		new VncFunction(
				"regex/count", 
				VncFunction
					.meta()
					.arglists("(regex/count matcher)")		
					.doc(
						"Returns the matcher's group count.")
					.examples(
						"(let [p (regex/pattern \"([0-9]+)(.*)\")  \n" +
						"      m (regex/matcher p \"100abc\")]     \n" +
						"   (regex/count m))  ")
					.seeAlso("regex/matcher")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);
	
				final Matcher m = (Matcher)Coerce.toVncJavaObject(args.first()).getDelegate();		
					
				return new VncLong(m.groupCount());
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};

			
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap
					.Builder()
					.add(pattern)
					.add(matcher)
					.add(find)
					.add(find_all)
					.add(find_plus)
					.add(find_all_plus)
					.add(reset)
					.add(find_Q)
					.add(matches)
					.add(matches_Q)
					.add(group)
					.add(count)
					.toMap();	
}
