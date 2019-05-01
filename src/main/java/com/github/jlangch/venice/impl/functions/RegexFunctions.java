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

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;


public class RegexFunctions {

	///////////////////////////////////////////////////////////////////////////
	// Regex
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction pattern = 
			new VncFunction(
					"regex/pattern", 
					VncFunction
						.meta()
						.module("regex")
						.arglists("(regex/pattern s)")		
						.doc("Returns an instance of java.util.regex.Pattern.")
						.examples("(regex/pattern \"[0-9]+\")")
						.build()
			) {		
				public VncVal apply(final VncList args) {
					assertArity("regex/pattern", args, 1);
		
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
					.module("regex")
					.arglists("(regex/matcher pattern str)")		
					.doc(
						"Returns an instance of java.util.regex.Matcher. The pattern can be " +
						"either a string or a pattern created by (regex/pattern s)")
					.examples(
						"(regex/matcher \"[0-9]+\" \"100\")",
						"(let [p (regex/pattern \"[0-9]+\")] \n" +
						"   (regex/matcher p \"100\"))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("regex/matcher", args, 2);

				final VncVal pattern = args.first();
				final Pattern p = Types.isVncString(pattern)
									? Pattern.compile(((VncString)pattern).getValue())	
									: (Pattern)Coerce.toVncJavaObject(args.first()).getDelegate();		
				final String s = Coerce.toVncString(args.second()).getValue();	
				return new VncJavaObject(p.matcher(s));
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction find_Q = 
		new VncFunction(
				"regex/find?", 
				VncFunction
					.meta()
					.module("regex")
					.arglists("(regex/find matcher)")		
					.doc(
						"Attempts to find the next subsequence that matches the pattern. " +
						"If the match succeeds then more information can be obtained via " +
						"the regex/group function")
					.examples(
						"(let [p (regex/pattern \"[0-9]+\")  \n" +
						"      m (regex/matcher p \"100\")]  \n" +
						"   (regex/find? m))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("regex/find", args, 1);
	
				final Matcher m = (Matcher)Coerce.toVncJavaObject(args.first()).getDelegate();		
	
				return m.find() ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction matches_Q = 
		new VncFunction(
				"regex/matches?", 
				VncFunction
					.meta()
					.module("regex")
					.arglists("(regex/matches matcher)")		
					.doc(
						"Attempts to match the entire region against the pattern. " +
						"If the match succeeds then more information can be obtained " +
						"via the regex/group function")
					.examples(
						"(let [p (regex/pattern \"[0-9]+\")  \n" +
						"      m (regex/matcher p \"100\")]  \n" +
						"   (regex/matches? m))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("regex/matches", args, 1);
	
				final Matcher m = (Matcher)Coerce.toVncJavaObject(args.first()).getDelegate();		
	
				return m.matches() ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction find = 
			new VncFunction(
					"regex/find", 
					VncFunction
						.meta()
						.module("regex")
						.arglists("(regex/find matcher)")		
						.doc("Returns the next regex match")
						.examples(
							"(let [m (regex/matcher \"[0-9]+\" \"672-345-456-3212\")]  \n" +
							"   (println (regex/find m))                               \n" +
							"   (println (regex/find m))                               \n" +
							"   (println (regex/find m))                               \n" +
							"   (println (regex/find m))                               \n" +
							"   (println (regex/find m)))                              \n")
						.build()
			) {		
				public VncVal apply(final VncList args) {
					assertArity("regex/find", args, 1);
		
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

	public static VncFunction reset = 
		new VncFunction(
				"regex/reset", 
				VncFunction
					.meta()
					.module("regex")
					.arglists("(regex/reset matcher str)")		
					.doc(
						"Resets the matcher with a new string")
					.examples(
						"(let [p (regex/pattern \"[0-9]+\")  \n" +
						"      m1 (regex/matcher p \"100\")  \n" +
						"      m2 (regex/reset m1 \"200\")]  \n" +
						"   (regex/find? m2))                  ")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("regex/reset", args, 2);
	
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
					.module("regex")
					.arglists("(regex/group matcher group)")		
					.doc(
						"Returns the input subsequence captured by the given group during the" + 
						"previous match operation.")
					.examples(
						"(let [p (regex/pattern \"([0-9]+)(.*)\")      \n" +
						"      m (regex/matcher p \"100abc\")]         \n" +
						"   (if (regex/matches? m)                     \n" +
						"      [(regex/group m 1) (regex/group m 2)]   \n" +
						"      []))                                      ")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("regex/group", args, 2);
	
				final Matcher m = (Matcher)Coerce.toVncJavaObject(args.first()).getDelegate();		
				final int g = Coerce.toVncLong(args.second()).getValue().intValue();		
					
				return g >= 0 && g <= m.groupCount() ? new VncString(m.group(g)) : Nil;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncFunction groupcount = 
		new VncFunction(
				"regex/groupcount", 
				VncFunction
					.meta()
					.module("regex")
					.arglists("(regex/groupcount matcher)")		
					.doc(
						"Returns the matcher's group count.")
					.examples(
						"(let [p (regex/pattern \"([0-9]+)(.*)\")  \n" +
						"      m (regex/matcher p \"100abc\")]     \n" +
						"   (regex/groupcount m))                    ")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("regex/groupcount", args, 1);
	
				final Matcher m = (Matcher)Coerce.toVncJavaObject(args.first()).getDelegate();		
					
				return new VncLong(m.groupCount());
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

			
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap.Builder()
					.put("regex/pattern",		pattern)
					.put("regex/matcher",		matcher)
					.put("regex/find",			find)
					.put("regex/reset",			reset)
					.put("regex/find?",			find_Q)
					.put("regex/matches?",		matches_Q)
					.put("regex/group",			group)
					.put("regex/groupcount",	groupcount)
					.toMap();	
}
