/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2022 Venice
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
import java.util.regex.PatternSyntaxException;

import com.github.jlangch.venice.VncException;
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
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;


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
                            "Patterns are immutable and are safe for use by multiple " +
                            "concurrent threads! \n\n" +
                            "Alternatively regex pattern literals can be used to " +
                            "define a pattern: `#\"[0-9+]\"`\n\n" +
                            "```                                                   \n" +
                            "\"\\\\d\" ;; regex string to match one digit          \n" +
                            "```                                                   \n" +
                            "Notice that you have to escape the backslash to get a " +
                            "literal backslash in the string. However, regex       " +
                            "pattern literals are smart. They don't need to double escape: \n\n" +
                            "```                                                   \n" +
                            "#\"\\d\" ;; regex pattern literal to match one digit  \n" +
                            "```                                                   \n" +
                            "JavaDoc: [Pattern](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html)")
                        .examples(
                            "(regex/pattern \"[0-9]+\")",
                            "(regex/pattern \"\\\\d+\")",
                            "#\"[0-9]+\"",
                            "#\"\\d+\"")
                        .seeAlso(
                            "regex/matcher", "regex/matches", "regex/find", "regex/find-all")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertArity(this, args, 1);

                    try {
                        // "[Regex Pattern](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html)"
                        return new VncJavaObject(
                                Pattern.compile(
                                        Coerce.toVncString(args.first()).getValue()));
                    }
                    catch (PatternSyntaxException ex) {
                        throw new VncException("Illegal regex pattern: " + ex.getMessage(), ex);
                    }
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
                        "Matchers are mutable and are not safe for use by multiple " +
                        "concurrent threads! \n\n" +
                        "JavaDoc: [Pattern](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html)")
                    .examples(
                        "(regex/matcher #\"[0-9]+\" \"100\")",
                        "(regex/matcher (regex/pattern\"[0-9]+\") \"100\")",
                        "(regex/matcher \"[0-9]+\" \"100\")")
                    .seeAlso(
                        "regex/pattern", "regex/matches?", "regex/find?", "regex/reset",
                        "regex/matches", "regex/find", "regex/find-all")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final VncVal pattern = args.first();
                final Pattern p = Types.isVncString(pattern)
                                    ? Pattern.compile(((VncString)pattern).getValue())
                                    : Coerce.toVncJavaObject(args.first(), Pattern.class);
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
                    .arglists(
                        "(regex/matches? matcher)",
                        "(regex/matches? matcher str)")
                    .doc(
                        "Attempts to match the entire region against the pattern. " +
                        "If the match succeeds then more information can be obtained " +
                        "via the regex/group function")
                    .examples(
                        "(let [m (regex/matcher \"[0-9]+\" \"100\")]         \n" +
                        "  (regex/matches? m))                               ",
                        "(let [m (regex/matcher \"[0-9]+\" \"value: 100\")]  \n" +
                        "  (regex/matches? m))                               ",
                        "(let [m (regex/matcher \"[0-9]+\" \"\")]                     \n" +
                        "  (filter #(regex/matches? m %) [\"100\" \"1a1\" \"200\"]))  ")
                    .seeAlso("regex/matcher", "regex/matches")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                if (args.size() == 1) {
                    final Matcher m = (Matcher)Coerce.toVncJavaObject(args.first()).getDelegate();
                    return VncBoolean.of(m.matches());
                }
                else {
                    final Matcher m = Coerce.toVncJavaObject(args.first(), Matcher.class);
                    final String s = Coerce.toVncString(args.second()).getValue();
                    return VncBoolean.of(m.reset(s).matches());
                }
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
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final VncVal pattern = args.first();
                final Pattern p = Types.isVncString(pattern)
                                    ? Pattern.compile(((VncString)pattern).getValue())
                                    : Coerce.toVncJavaObject(args.first(), Pattern.class);
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
                        "(let [m (regex/matcher #\"[0-9]+\" \"100\")] \n" +
                        "  (regex/find? m))",

                        "(let [m (regex/matcher #\"[0-9]+\" \"xxx: 100\")] \n" +
                        "  (regex/find? m))",

                        "(let [m (regex/matcher #\"[0-9]+\" \"xxx: 100 200\")] \n" +
                        "  (when (regex/find? m) \n" +
                        "    (println (regex/group m 0))) \n" +
                        "  (when (regex/find? m) \n" +
                        "    (println (regex/group m 0))) \n" +
                        "  (when (regex/find? m) \n" +
                        "    (println (regex/group m 0))))")
                    .seeAlso("regex/group", "regex/matches?")
                    .build()
        ) {
            @Override
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
                    .arglists(
                        "(regex/find matcher)",
                        "(regex/find pattern s)")
                    .doc(
                        "Returns the next regex match or nil if there is no further match. " +
                        "Returns `nil` if there is no match. \n\n" +
                        "To get the positional data for the matched group use `(regex/find+ matcher)`.")
                    .examples(
                        "(regex/find #\"[0-9]+\" \"672-345-456-3212\")",
                        "(let [m (regex/matcher #\"[0-9]+\" \"672-345-456-3212\")]  \n" +
                        "  (println (regex/find m))  \n" +
                        "  (println (regex/find m))  \n" +
                        "  (println (regex/find m))  \n" +
                        "  (println (regex/find m))  \n" +
                        "  (println (regex/find m)))")
                    .seeAlso(
                        "regex/find-all", "regex/find+", "regex/matcher")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                Matcher m;

                if (args.size() == 1) {
                    m = Coerce.toVncJavaObject(args.first(), Matcher.class);
                }
                else {
                    final String s = Coerce.toVncString(args.second()).getValue();
                    final Pattern p = Coerce.toVncJavaObject(args.first(), Pattern.class);
                    m = p.matcher(s);
                }

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
                    .arglists(
                        "(regex/find+ matcher)",
                        "(regex/find+ pattern s)")
                    .doc(
                        "Returns the next regex match and returns the group with " +
                        "its positional data. Returns `nil` if there is no match.")
                    .examples(
                        "(regex/find+ #\"[0-9]+\" \"672-345-456-3212\")",
                        "(let [m (regex/matcher #\"[0-9]+\" \"672-345-456-3212\")]  \n" +
                        "   (println (regex/find+ m))  \n" +
                        "   (println (regex/find+ m))  \n" +
                        "   (println (regex/find+ m))  \n" +
                        "   (println (regex/find+ m))  \n" +
                        "   (println (regex/find+ m))) \n")
                    .seeAlso(
                        "regex/find-all+", "regex/find", "regex/matcher")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                Matcher m;

                if (args.size() == 1) {
                    m = Coerce.toVncJavaObject(args.first(), Matcher.class);
                }
                else {
                    final String s = Coerce.toVncString(args.second()).getValue();
                    final Pattern p = Coerce.toVncJavaObject(args.first(), Pattern.class);
                    m = p.matcher(s);
                }

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
                    .arglists(
                        "(regex/find-all matcher)",
                        "(regex/find-all pattern s)")
                    .doc(
                        "Returns all regex matches as list or an empty list if there " +
                        "are no matches.\n\n" +
                        "To get the positional data for the matched groups use 'regex/find-all+'.")
                    .examples(
                        "(regex/find-all #\"\\d+\" \"672-345-456-3212\")",
                        "(->> (regex/matcher #\"\\d+\" \"672-345-456-3212\") \n" +
                        "     (regex/find-all))                                 ",
                        "(->> (regex/matcher \"([^\\\"]\\\\S*|\\\".+?\\\")\\\\s*\" \"1 2 \\\"3 4\\\" 5\") \n" +
                        "     (regex/find-all))                                 ")
                    .seeAlso(
                        "regex/find", "regex/find-all+", "regex/matcher")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                Matcher m;

                if (args.size() == 1) {
                    m = Coerce.toVncJavaObject(args.first(), Matcher.class);
                }
                else {
                    final String s = Coerce.toVncString(args.second()).getValue();
                    final Pattern p = Coerce.toVncJavaObject(args.first(), Pattern.class);
                    m = p.matcher(s);
                }

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
                    .arglists(
                        "(regex/find-all+ matcher)",
                        "(regex/find-all+ pattern s)")
                    .doc(
                        "Returns the all regex matches and returns the groups " +
                        "with its positional data. Returns an empty list if there " +
                        "are no matches.")
                    .examples(
                        "(regex/find-all+ #\"[0-9]+\" \"672-345-456-3212\")",
                        "(let [m (regex/matcher #\"[0-9]+\" \"672-345-456-3212\")] \n" +
                        "  (regex/find-all+ m))  \n")
                    .seeAlso(
                        "regex/find+", "regex/find-all", "regex/matcher")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                Matcher m;

                if (args.size() == 1) {
                    m = Coerce.toVncJavaObject(args.first(), Matcher.class);
                }
                else {
                    final String s = Coerce.toVncString(args.second()).getValue();
                    final Pattern p = Coerce.toVncJavaObject(args.first(), Pattern.class);
                    m = p.matcher(s);
                }

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
                        "  (let [m (regex/matcher #\"[0-9]+\" \"100\")] \n" +
                        "    (println (regex/find m))                   \n" +
                        "    (let [m (regex/reset m \"200\")]           \n" +
                        "      (println (regex/find m)))))" )
                    .seeAlso("regex/matcher")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final Matcher m = Coerce.toVncJavaObject(args.first(), Matcher.class);
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
                        "previous match operation.\n\n" +
                        "Note: Do not forget to call the `regex/matches?` function!")
                    .examples(
                        "(let [m (regex/matcher #\"([0-9]+)(.*)\" \"100abc\")] \n" +
                        "   (if (regex/matches? m)                             \n" +
                        "      [(regex/group m 1) (regex/group m 2)]           \n" +
                        "      []))                                            ")
                    .seeAlso("regex/groups", "regex/matcher", "regex/matches?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final Matcher m = Coerce.toVncJavaObject(args.first(), Matcher.class);
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

    public static VncFunction groups =
            new VncFunction(
                    "regex/groups",
                    VncFunction
                        .meta()
                        .arglists("(regex/groups matcher)")
                        .doc(
                            "Attempts to match the entire region against the pattern and returns all matched groups.")
                        .examples(
                            "(let [m (regex/matcher #\"([0-9]+)(.*)\" \"100abc\")] \n" +
                            "   (regex/groups m))                                  ")
                        .seeAlso("regex/group", "regex/matcher", "regex/matches?")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertArity(this, args, 1);

                    VncList list = VncList.empty();

                    final Matcher m = Coerce.toVncJavaObject(args.first(), Matcher.class);
                    if (m.matches()) {
                        for(int ii=0; ii<=m.groupCount(); ii++) {
                            final String group = m.group(ii);
                            list = list.addAtEnd(group == null ? Nil : new VncString(group));
                        }
                    }

                    return list;
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
                        "(let [m (regex/matcher #\"([0-9]+)(.*)\" \"100abc\")]\n" +
                        "   (regex/count m))  ")
                    .seeAlso(
                        "regex/matcher")
                    .build()
        ) {
            @Override
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

    public static final Map<VncVal, VncVal> ns =
            new SymbolMapBuilder()
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
                    .add(groups)
                    .add(count)
                    .toMap();
}
