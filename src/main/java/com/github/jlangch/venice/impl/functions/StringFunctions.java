/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2023 Venice
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
import static com.github.jlangch.venice.impl.types.VncBoolean.False;
import static com.github.jlangch.venice.impl.types.VncBoolean.True;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Base64;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jline.utils.Levenshtein;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncByteBuffer;
import com.github.jlangch.venice.impl.types.VncChar;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncNumber;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.HexFormatter;
import com.github.jlangch.venice.impl.util.HexUtil;
import com.github.jlangch.venice.impl.util.LoremIpsum;
import com.github.jlangch.venice.impl.util.StringEscapeUtil;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;
import com.github.jlangch.venice.impl.util.markdown.renderer.text.LineWrap;


public class StringFunctions {

    ///////////////////////////////////////////////////////////////////////////
    // String
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction str_blank_Q =
        new VncFunction(
                "str/blank?",
                VncFunction
                    .meta()
                    .arglists("(str/blank? s)")
                    .doc("True if s is nil, empty, or contains only whitespace.")
                    .examples(
                        "(str/blank? nil)",
                        "(str/blank? \"\")",
                        "(str/blank? \"  \")",
                        "(str/blank? \"abc\")")
                    .seeAlso("str/not-blank?", "empty?", "not-empty?", "nil?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                if (args.first() == Nil) {
                    return True;
                }

                final String s = Coerce.toVncString(args.first()).getValue();

                return VncBoolean.of(StringUtil.isBlank(s));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_not_blank_Q =
            new VncFunction(
                    "str/not-blank?",
                    VncFunction
                        .meta()
                        .arglists("(str/not-blank? s)")
                        .doc("True if s contains at least one non whitespace char.")
                        .examples(
                            "(str/not-blank? \"abc\")",
                            "(str/not-blank? \" a \")",
                            "(str/not-blank? nil)",
                            "(str/not-blank? \"\")",
                            "(str/not-blank? \"  \")")
                        .seeAlso("str/blank?", "empty?", "not-empty?", "nil?")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertArity(this, args, 1);

                    if (args.first() == Nil) {
                        return False;
                    }

                    final String s = Coerce.toVncString(args.first()).getValue();

                    return VncBoolean.of(StringUtil.isNotBlank(s));
                }

                private static final long serialVersionUID = -1848883965231344442L;
            };

    public static VncFunction str_char_Q =
        new VncFunction(
                "str/char?",
                VncFunction
                    .meta()
                    .arglists("(str/char? s)")
                    .doc("Returns true if s is a char or a single char string.")
                    .examples(
                        "(str/char? \"x\")",
                        "(str/char? #\\x)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal s = args.first();


                if (s == Nil) {
                    return Nil;
                }
                else if (Types.isVncChar(s)) {
                    return True;
                }
                else {
                    return VncBoolean.of(Types.isVncString(s) && ((VncString)s).size() == 1);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_starts_with_Q =
        new VncFunction(
                "str/starts-with?",
                VncFunction
                    .meta()
                    .arglists("(str/starts-with? s substr)")
                    .doc("True if s starts with substr.")
                    .examples("(str/starts-with? \"abc\"  \"ab\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                if (args.first() == Nil || args.second() == Nil) {
                    return False;
                }

                final VncString string = Coerce.toVncString(args.first());
                final VncString prefix = Coerce.toVncString(args.second());

                return VncBoolean.of(string.getValue().startsWith(prefix.getValue()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_ends_with_Q =
        new VncFunction(
                "str/ends-with?",
                VncFunction
                    .meta()
                    .arglists("(str/ends-with? s substr)")
                    .doc("True if s ends with substr.")
                    .examples("(str/ends-with? \"abc\"  \"bc\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                if (args.first() == Nil || args.second() == Nil) {
                    return False;
                }

                final VncString string = Coerce.toVncString(args.first());
                final VncString suffix = Coerce.toVncString(args.second());

                return VncBoolean.of(string.getValue().endsWith(suffix.getValue()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_contains_Q =
        new VncFunction(
                "str/contains?",
                VncFunction
                    .meta()
                    .arglists("(str/contains? s substr)")
                    .doc("True if s contains with substr.")
                    .examples(
                        "(str/contains? \"abc\" \"ab\")",
                        "(str/contains? \"abc\" #\\b)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                if (args.first() == Nil || args.second() == Nil) {
                    return False;
                }

                final VncString string = Coerce.toVncString(args.first());

                final VncVal vSubstr = args.second();

                if (Types.isVncString(vSubstr)) {
                    final String text = Coerce.toVncString(args.second()).getValue();

                    if (text.isEmpty()) {
                        return VncBoolean.False;
                    }

                    return VncBoolean.of(string.getValue().contains(text));
                }
                else if (Types.isVncChar(vSubstr)) {
                    final Character ch = Coerce.toVncChar(args.second()).getValue();
                    final String text = String.valueOf(ch);
                    return VncBoolean.of(string.getValue().contains(text));
                }
                else {
                    return VncBoolean.False;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_equals_ignore_case_Q =
        new VncFunction(
                "str/equals-ignore-case?",
                VncFunction
                    .meta()
                    .arglists("(str/equals-ignore-case? s1 s2)")
                    .doc("Compares two strings ignoring case. True if both are equal.")
                    .examples("(str/equals-ignore-case? \"abc\"  \"abC\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final VncVal v1 = args.first();
                final VncVal v2 = args.second();

                if (v1 == Nil || v2 == Nil) {
                    return True;
                }
                else if (v1 != Nil || v2 != Nil) {
                    final String s1 = Coerce.toVncString(args.first()).getValue();
                    final String s2 = Coerce.toVncString(args.second()).getValue();

                    return VncBoolean.of(s1.equalsIgnoreCase(s2));
                }
                else {
                    return False;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_align =
        new VncFunction(
                "str/align",
                VncFunction
                    .meta()
                    .arglists(
                        "(str/align width align overflow text)")
                    .doc(
                        "Aligns a text within a string of width characters.\n\n" +
                        "align: :left, :center, :right\n\n" +
                        "overflow: :newline :clip-left, :clip-right, :ellipsis-left, :ellipsis-right")
                    .examples(
                        "(str/align 6 :left :clip-right \"abc\")",
                        "(str/align 6 :center :clip-right \"abc\")",
                        "(str/align 6 :right :clip-right \"abc\")",
                        "(str/align 6 :left :clip-left \"abcdefgh\")",
                        "(str/align 6 :left :ellipsis-left \"abcdefgh\")",
                        "(str/align 6 :left :ellipsis-right \"abcdefgh\")")
                    .seeAlso(
                        "str/trim-to-nil", "str/trim-left", "str/trim-right")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 4);

                final int width = Coerce.toVncLong(args.first()).toJavaInteger();
                final String align = Coerce.toVncKeyword(args.second()).getSimpleName();
                final String overflow = Coerce.toVncKeyword(args.third()).getSimpleName();
                final String text = Coerce.toVncString(args.fourth())
                                          .getValue()
                                          .trim()
                                          .replace('\t', ' ');

                final Function<String,List<String>> clip = s -> {
                    final int len = s.length();
                    switch(overflow) {
                        case "newline":        return LineWrap.softWrap(s, width);
                        case "clip-left":      return Arrays.asList(len > width ? s.substring(len-width, len) : s);
                        case "clip-right":     return Arrays.asList(len > width ? s.substring(0, width) : s);
                        case "ellipsis-left":  return Arrays.asList(len > width ? "…" + s.substring(len-width+1, len) : s);
                        case "ellipsis-right": return Arrays.asList(len > width ? s.substring(0, width-1) + "…" : s);
                        default:               throw new VncException(String.format(
                                                            "Function 'str/align' got undefined overflow :%s.",
                                                            overflow));
                    }};

                final Function<String,String> justify = s -> {
                    if (s.length() < width) {
                         switch(align) {
                             case "left":   return StringUtil.padRight(s, width);
                             case "right":  return StringUtil.padLeft(s, width);
                             case "center": return StringUtil.padCenter(s, width);
                             default:       throw new VncException(String.format(
                                                 "Function 'str/align' got undefined align :%s.",
                                                 align));
                         }
                     }
                     else {
                         return s;
                     }};

                 return new VncString(
                        text.isEmpty()
                            ? StringUtil.repeat(' ', width)
                            : StringUtil
                                .splitIntoLines(text)
                                .stream()
                                .map(s -> s.trim())
                                .map(s -> clip.apply(s))
                                .flatMap(list -> list.stream())
                                .map(s -> justify.apply(s))
                                .collect(Collectors.joining("\n")));
             }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_trim =
        new VncFunction(
                "str/trim",
                VncFunction
                    .meta()
                    .arglists("(str/trim s)")
                    .doc("Trims leading and trailing whitespaces from s.")
                    .examples("(str/trim \" abc  \")")
                    .seeAlso("str/trim-to-nil", "str/trim-left", "str/trim-right")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                if (args.first() == Nil) {
                    return Nil;
                }

                return new VncString(Coerce.toVncString(args.first()).getValue().trim());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_trim_left =
        new VncFunction(
                "str/trim-left",
                VncFunction
                    .meta()
                    .arglists("(str/trim-left s)")
                    .doc("Trims leading whitespaces from s.")
                    .examples("(str/trim-left \" abc \")")
                    .seeAlso("str/trim-right", "str/trim", "str/trim-to-nil")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                if (args.first() == Nil) {
                    return Nil;
                }

                return new VncString(
                            StringUtil.trimLeft(
                                Coerce.toVncString(args.first()).getValue()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_trim_right =
        new VncFunction(
                "str/trim-right",
                VncFunction
                    .meta()
                    .arglists("(str/trim-right s)")
                    .doc("Trims trailing whitespaces from s.")
                    .examples("(str/trim-right \" abc \")")
                    .seeAlso("str/trim-left", "str/trim", "str/trim-to-nil")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                if (args.first() == Nil) {
                    return Nil;
                }

                return new VncString(
                            StringUtil.trimRight(
                                Coerce.toVncString(args.first()).getValue()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_trim_to_nil =
        new VncFunction(
                "str/trim-to-nil",
                VncFunction
                    .meta()
                    .arglists("(str/trim-to-nil s)")
                    .doc(
                        "Trims leading and trailing whitespaces from s. " +
                        "Returns nil if the resulting string is empty")
                    .examples(
                        "(str/trim-to-nil \"\")",
                        "(str/trim-to-nil \"    \")",
                        "(str/trim-to-nil nil)",
                        "(str/trim-to-nil \" abc   \")")
                    .seeAlso("str/trim", "str/trim-left", "str/trim-right")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

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
                    .arglists("(str/index-of s value)", "(str/index-of s value from-index)")
                    .doc(
                        "Return index of value (string or char) in s, optionally searching " +
                        "forward from from-index. Return nil if value not found.")
                    .examples(
                        "(str/index-of \"abcdefabc\" \"ab\")")
                    .seeAlso("str/last-index-of")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2, 3);

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
                    .arglists("(str/last-index-of s value)", "(str/last-index-of s value from-index)")
                    .doc(
                        "Return last index of value (string or char) in s, optionally " +
                        "searching backward from from-index. Return nil if value not found.")
                    .examples(
                        "(str/last-index-of \"abcdefabc\" \"ab\")")
                    .seeAlso("str/index-of")
                   .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2, 3);

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

    public static VncFunction str_replace_all =
        new VncFunction(
                "str/replace-all",
                VncFunction
                    .meta()
                    .arglists("(str/replace-all s search replacement)")
                    .doc(
                        "Replaces the all occurrances of search in s. " +
                        "The search arg may be a string or a regex pattern")
                    .examples(
                        "(str/replace-all \"abcdefabc\" \"ab\" \"__\")",
                        "(str/replace-all \"a0b01c012d\" (regex/pattern \"[0-9]+\") \"_\")",
                        "(str/replace-all \"a0b01c012d\" #\"[0-9]+\" \"_\")")
                    .seeAlso(
                        "str/replace-first", "str/replace-last")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 3);

                if (args.first() == Nil) {
                    return Nil;
                }

                final String text = Coerce.toVncString(args.first()).getValue();
                final VncVal search = args.second();
                final String replacement = Coerce.toVncString(args.third()).getValue();

                final VncHashMap options = VncHashMap.ofAll(args.slice(3));
                final boolean ignoreCase = VncBoolean.isTrue(options.get(new VncKeyword("ignore-case"), False));

                if (Types.isVncString(search)) {
                    final String searchString = Coerce.toVncString(args.second()).getValue();

                    return new VncString(StringUtil.replace(text, searchString, replacement, 1000000, ignoreCase));
                }
                else if (Types.isVncJavaObject(search, Pattern.class)) {
                    final Pattern p = Coerce.toVncJavaObject(search, Pattern.class);

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

    public static VncFunction str_replace_first =
        new VncFunction(
                "str/replace-first",
                VncFunction
                    .meta()
                    .arglists("(str/replace-first s search replacement & options)")
                    .doc(
                        "Replaces the first occurrance of search in s. The search arg may be a" +
                        "string or a regex pattern. If the search arg is of type string the " +
                        "options :ignore-case and :nfirst are supported.\n\n" +
                        "Options: \n\n" +
                        "| :ignore-case b | if true ignores case, defaults to false |\n" +
                        "| :nfirst n      | e.g :nfirst 2, defaults to 1 |\n")
                    .examples(
                        "(str/replace-first \"ab-cd-ef-ab-cd\" \"ab\" \"XYZ\")",
                        "(str/replace-first \"AB-CD-EF-AB-CD\" \"ab\" \"XYZ\" :ignore-case true)",
                        "(str/replace-first \"ab-ab-cd-ab-ef-ab-cd\" \"ab\" \"XYZ\" :nfirst 3)",
                        "(str/replace-first \"a0b01c012d\" (regex/pattern \"[0-9]+\") \"_\")",
                        "(str/replace-first \"a0b01c012d\" #\"[0-9]+\" \"_\")")
                    .seeAlso(
                        "str/replace-last", "str/replace-all")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 3);

                if (args.first() == Nil || args.second() == Nil || args.third() == Nil) {
                    return args.first();
                }

                final String text = Coerce.toVncString(args.first()).getValue();
                final VncVal search = args.second();
                final String replacement = Coerce.toVncString(args.third()).getValue();

                final VncHashMap options = VncHashMap.ofAll(args.slice(3));
                final boolean ignoreCase = VncBoolean.isTrue(options.get(new VncKeyword("ignore-case"), False));
                final long nFirst = Coerce.toVncLong(options.get(new VncKeyword("nfirst"), new VncLong(1))).getValue();

                if (Types.isVncString(search)) {
                    final String searchString = Coerce.toVncString(args.second()).getValue();

                    return new VncString(StringUtil.replace(text, searchString, replacement, (int)nFirst, ignoreCase));
                }
                else if (Types.isVncJavaObject(search, Pattern.class)) {
                    final Pattern p = Coerce.toVncJavaObject(search, Pattern.class);

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
                    .arglists("(str/replace-last s search replacement & options)")
                    .doc(
                        "Replaces the last occurrance of search in s.\n\n" +
                        "Options: \n\n" +
                        "| :ignore-case b | if true ignores case, defaults to false |\n")
                    .examples(
                        "(str/replace-last \"abcdefabc\" \"ab\" \"XYZ\")",
                        "(str/replace-last \"foo.JPG\" \".jpg\" \".png\" :ignore-case true)")
                    .seeAlso(
                        "str/replace-first", "str/replace-all")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 3);


                if (args.first() == Nil || args.second() == Nil || args.third() == Nil) {
                    return args.first();
                }

                final String text = Coerce.toVncString(args.first()).getValue();
                final String searchString = Coerce.toVncString(args.second()).getValue();
                final String replacement = Coerce.toVncString(args.third()).getValue();

                final VncHashMap options = VncHashMap.ofAll(args.slice(3));
                final boolean ignoreCase = VncBoolean.isTrue(options.get(new VncKeyword("ignore-case"), False));

                return new VncString(StringUtil.replaceLast(text, searchString, replacement, ignoreCase));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_reverse =
        new VncFunction(
                "str/reverse",
                VncFunction
                    .meta()
                    .arglists("(str/reverse s)")
                    .doc("Reverses a string")
                    .examples("(str/reverse \"abcdef\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                if (args.first() == Nil) {
                    return Nil;
                }

                return new VncString(
                    new StringBuilder(Coerce.toVncString(args.first()).getValue())
                            .reverse()
                            .toString());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction str_lower_case =
        new VncFunction(
                "str/lower-case",
                VncFunction
                    .meta()
                    .arglists(
                        "(str/lower-case s)",
                        "(str/lower-case locale s)")
                    .doc(
                        "Converts s to lowercase.\n\n" +
                        "Since case mappings are not always 1:1 character mappings when a locale is given, " +
                        "the resulting string may be a different length than the original!")
                    .examples(
                        "(str/lower-case \"aBcDeF\")",
                        "(str/lower-case #\\A)",
                        "(str/lower-case (. :java.util.Locale :new \"de\" \"DE\") \"aBcDeF\")",
                        "(str/lower-case (. :java.util.Locale :GERMANY) \"aBcDeF\")",
                        "(str/lower-case (. :java.util.Locale :new \"de\" \"CH\") \"aBcDeF\")",
                        "(str/lower-case [ \"de\"] \"aBcDeF\")",
                        "(str/lower-case [ \"de\" \"DE\"] \"aBcDeF\")",
                        "(str/lower-case [ \"de\" \"DE\"] \"aBcDeF\")")
                    .seeAlso("str/upper-case")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                if (args.size() == 1) {
                    final VncVal v = args.first();

                    if (v == Nil) {
                        return Nil;
                    }
                    else if (v instanceof VncChar) {
                        return new VncChar(Character.toLowerCase(((VncChar)v).getValue()));
                    }
                    else {
                        return new VncString(Coerce
                                                .toVncString(v)
                                                .getValue()
                                                .toLowerCase());
                    }
                }
                else  {
                    final VncVal v = args.second();

                    final Locale locale = toLocale(args.first());
                    if (locale == null) {
                        throw new VncException(String.format(
                                    "str/lower-case: the first arg is not a locale. Got a '%s'.",
                                    Types.getType(args.first())));
                    }
                    else if (v == Nil) {
                        return Nil;
                    }
                    else if (v instanceof VncChar) {
                        throw new VncException(
                                "str/lower-case: Cannot convert a char to lowercase if a locale is given " +
                                "since case mappings are not always 1:1 character mappings when a locale " +
                                "is given, the resulting string may be a different length than one!");
                    }
                    else {
                        return new VncString(Coerce
                                                .toVncString(v)
                                                .getValue()
                                                .toLowerCase(locale));
                    }
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_upper_case =
        new VncFunction(
                "str/upper-case",
                VncFunction
                    .meta()
                    .arglists(
                        "(str/upper-case s)",
                        "(str/upper-case locale s)")
                    .doc(
                        "Converts s to uppercase.\n\n" +
                        "Since case mappings are not always 1:1 character mappings when a locale is given, " +
                        "the resulting string may be a different length than the original!")
                    .examples(
                        "(str/upper-case \"aBcDeF\")",
                        "(str/upper-case #\\a)",
                        "(str/upper-case (. :java.util.Locale :new \"de\" \"DE\") \"aBcDeF\")",
                        "(str/upper-case (. :java.util.Locale :GERMANY) \"aBcDeF\")",
                        "(str/upper-case (. :java.util.Locale :new \"de\" \"CH\") \"aBcDeF\")",
                        "(str/upper-case [ \"de\"] \"aBcDeF\")",
                        "(str/upper-case [ \"de\" \"DE\"] \"aBcDeF\")",
                        "(str/upper-case [ \"de\" \"DE\"] \"aBcDeF\")")
                    .seeAlso("str/lower-case")
                   .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                if (args.size() == 1) {
                    final VncVal v = args.first();

                    if (v == Nil) {
                        return Nil;
                    }
                    else if (v instanceof VncChar) {
                        return new VncChar(Character.toUpperCase(((VncChar)v).getValue()));
                    }
                    else {
                        return new VncString(Coerce
                                                .toVncString(v)
                                                .getValue()
                                                .toUpperCase());
                    }
                }
                else {
                    final VncVal v = args.second();

                    final Locale locale = toLocale(args.first());
                    if (locale == null) {
                        throw new VncException(String.format(
                                    "str/upper-case: the first arg is not a locale. Got a '%s'.",
                                    Types.getType(args.first())));
                    }
                    else if (v == Nil) {
                        return Nil;
                    }
                    else if (v instanceof VncChar) {
                        throw new VncException(
                                "str/upper-case: Cannot convert a char to uppercase if a locale is given " +
                                "since case mappings are not always 1:1 character mappings when a locale is " +
                                "given, the resulting string may be a different length than one!");
                    }
                    else {
                        return new VncString(Coerce
                                                .toVncString(v)
                                                .getValue()
                                                .toUpperCase(locale));
                    }
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_join =
        new VncFunction(
                "str/join",
                VncFunction
                    .meta()
                    .arglists(
                        "(str/join coll)",
                        "(str/join separator coll)")
                    .doc(
                        "Joins all elements in coll separated by an optional separator.")
                    .examples(
                        "(str/join [1 2 3])",
                        "(str/join \"-\" [1 2 3])",
                        "(str/join \"-\" [(char \"a\") 1 \"xyz\" 2.56M])")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                final VncVal last = args.last();
                if (last == Constants.Nil) {
                    return VncString.EMPTY;
                }

                final VncSequence coll = Coerce.toVncSequence(last);
                if (coll.isEmpty()) {
                    return VncString.EMPTY;
                }


                final String delim = args.size() == 1 ? "" : Coerce.toVncString(args.first()).getValue();

                return new VncString(
                            coll.stream()
                                .map(v -> Types.isVncString(v) ? ((VncString)v).getValue() : v.toString())
                                .collect(Collectors.joining(delim)));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_subs =
        new VncFunction(
                "str/subs",
                VncFunction
                    .meta()
                    .arglists("(str/subs s start)", "(str/subs s start end)")
                    .doc(
                        "Returns the substring of s beginning at start inclusive, and ending " +
                        "at end (defaults to length of string), exclusive.")
                    .examples(
                        "(str/subs \"abcdef\" 2)",
                        "(str/subs \"abcdef\" 2 5)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2, 3);

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

    public static VncFunction str_pos =
        new VncFunction(
                "str/pos",
                VncFunction
                    .meta()
                    .arglists("(str/pos s pos)")
                    .doc(
                        "Returns the 0 based row/column position within a string based on " +
                        "absolute character position. Returns a map with the keys " +
                        "'row' and 'col'.\n\n" +
                        "Note: CR & LF count together as one each regarding the absolute position.")
                    .examples(
                        "(str/pos \"abcdefghij\" 4)",
                        "(str/pos \"ab\ncdefghij\" 6)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final String string = Coerce.toVncString(args.first()).getValue();
                final int pos = Coerce.toVncLong(args.second()).getIntValue();

                final char[] chars = string.toCharArray();
                int row=0;
                int col=0;
                for(int ii=0; ii<chars.length; ii++) {
                    if (ii == pos) {
                        return VncHashMap.of(
                                new VncKeyword("row"), new VncLong(row),
                                new VncKeyword("col"), new VncLong(col));
                    }
                    switch(chars[ii]) {
                        case '\r': break;
                        case '\n': row++; col=0; break;
                        default:   col++; break;
                    }
                }

                return VncHashMap.of(
                        new VncKeyword("row"), new VncLong(-1L),
                        new VncKeyword("col"), new VncLong(-1L));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_chars =
            new VncFunction(
                    "str/chars",
                    VncFunction
                        .meta()
                        .arglists("(str/chars s)")
                        .doc("Converts a string to a char list.")
                        .examples(
                            "(str/chars \"abcdef\")",
                            "(str/join (str/chars \"abcdef\"))")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertArity(this, args, 1);

                    if (args.first() == Nil) {
                        return VncList.empty();
                    }
                    else {
                        final String s = Coerce.toVncString(args.first()).getValue();

                        return VncList.ofList(
                                    s.chars()
                                     .mapToObj(c -> new VncChar((char)c))
                                     .collect(Collectors.toList()));
                    }
                }

                private static final long serialVersionUID = -1848883965231344442L;
            };

    public static VncFunction str_split =
        new VncFunction(
                "str/split",
                VncFunction
                    .meta()
                    .arglists(
                        "(str/split s regex)",
                        "(str/split s regex limit)")
                    .doc(
                        "Splits string on a regular expression. Optional argument limit is "+
                        "the maximum number of splits. Returns a list of the splits.")
                    .examples(
                        "(str/split \"abc,def,ghi\" \",\")",
                        "(str/split \"James Peter Robert\" \" \" 2)",
                        "(str/split \"abc , def ,  ghi\" \" *, *\")",
                        "(str/split \"abc,def,ghi\" \"((?<=,)|(?=,))\")",
                        "(str/split \"q1w2e3r4t5y6u7i8o9p0\" #\"\\d+\")",
                        "(str/split \"q1w2e3r4t5y6u7i8o9p0\" #\"\\d+\" 5)",
                        "(str/split \"1234567890\" #\"(?<=\\G.{4})\")",
                        "(str/split \"1234567890\" #\"(?=(.{4})+$)\")",
                        "(str/split \" q1w2 \" #\"\")",
                        "(str/split nil \",\")")
                    .seeAlso("str/split-lines")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2, 3);

                if (args.first() == Nil) {
                    return VncList.empty();
                }
                else {
                    final String str = Coerce.toVncString(args.first()).getValue();

                    final boolean limited = args.size() == 3;
                    final long limit = limited ? Coerce.toVncLong(args.third()).getValue() : -1;

                    if (Types.isVncString(args.second())) {
                        final VncString regex = Coerce.toVncString(args.second());

                        final String[] matches = limited
                                                    ? str.split(regex.getValue(), (int)limit)
                                                    : str.split(regex.getValue());

                        return VncList.ofList(
                                Arrays
                                    .asList(matches)
                                    .stream()
                                    .map(s -> new VncString(s))
                                    .collect(Collectors.toList()));
                    }
                    else if (Types.isVncJavaObject(args.second(), Pattern.class)) {
                        final Pattern pattern = Coerce.toVncJavaObject(args.second(), Pattern.class);

                        final String[] matches = limited
                                                    ? pattern.split(str, (int)limit)
                                                    : pattern.split(str);

                        return VncList.ofList(
                                Arrays
                                    .asList(matches)
                                    .stream()
                                    .map(s -> new VncString(s))
                                    .collect(Collectors.toList()));
                    }
                    else {
                        throw new VncException(String.format(
                                "Function 'str/split' does not allow %s as regex pattern. " +
                                "Expected a string or a java.util.regex.Pattern",
                                Types.getType(args.second())));
                    }
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_split_at =
        new VncFunction(
                "str/split-at",
                VncFunction
                    .meta()
                    .arglists(
                        "(str/split-at s pos)")
                    .doc(
                        "Splits string at the given position. Returns a list of the splits.")
                    .examples(
                        "(str/split-at nil 1)",
                        "(str/split-at \"\" 1)",
                        "(str/split-at \"abc\" 0)",
                        "(str/split-at \"abc\" 1)",
                        "(str/split-at \"abc\" 2)",
                        "(str/split-at \"abc\" 3)")
                    .seeAlso("str/split-lines")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                if (args.first() == Nil) {
                    return VncList.of(new VncString(""), new VncString(""));
                }
                else {
                    final String str = Coerce.toVncString(args.first()).getValue();
                    final long pos = Coerce.toVncLong(args.second()).getValue();

                    if (pos <= 0) {
                        return VncList.of(new VncString(""), args.first());
                    }
                    else if (pos >= str.length()) {
                        return VncList.of(args.first(), new VncString(""));
                    }
                    else {
                        return VncList.of(
                                    new VncString(str.substring(0, (int)pos)),
                                    new VncString(str.substring((int)pos)));
                    }
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_split_lines =
        new VncFunction(
                "str/split-lines",
                VncFunction
                    .meta()
                    .arglists("(str/split-lines s)")
                    .doc("Splits s into lines.")
                    .examples("(str/split-lines \"line1\nline2\nline3\")")
                    .seeAlso("str/split", "io/slurp-lines")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return args.first() == Nil
                        ? VncList.empty()
                        : VncList.ofList(
                                StringUtil
                                    .splitIntoLines(Coerce.toVncString(args.first()).getValue())
                                    .stream()
                                    .map(s -> new VncString(s))
                                    .collect(Collectors.toList()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_split_columns =
        new VncFunction(
                "str/split-columns",
                VncFunction
                    .meta()
                    .arglists("(str/split-columns s cols)")
                    .doc("Splits a string into columns. The columns are given by their start positions.")
                    .examples("(str/split-columns \"1abc  2d    3gh\" [0 6 12])")
                    .seeAlso("str/split")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final String text = Coerce.toVncString(args.first()).getValue();
                final List<VncVal> colList =  Coerce.toVncSequence(args.second()).getJavaList();

                final VncVal[] colStartPos_ = colList.toArray(new VncVal[0]);
                final int[] colStartPos = new int[colList.size()];

                for(int ii=0; ii<colList.size(); ii++) {
                    colStartPos[ii] = Coerce.toVncLong(colStartPos_[ii]).toJavaInteger();
                }

                final List<String> cols = StringUtil.splitColumns(text, colStartPos);

                return VncList.ofColl(
                        cols.stream()
                            .map(s -> new VncString(s))
                            .collect(Collectors.toList()));
             }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_cr_lf =
        new VncFunction(
                "str/cr-lf",
                VncFunction
                    .meta()
                    .arglists("(str/cr-lf s mode)")
                    .doc("Convert a text to use LF or CR-LF.")
                    .examples(
                        "(str/cr-lf \"line1\nline2\nline3\" :cr-lf)",
                        "(str/cr-lf \"line1\nline2\nline3\" :lf)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                if (args.first() == Nil) {
                    return Nil;
                }
                else {
                    final String text = Coerce.toVncString(args.first()).getValue();
                    final String mode = Coerce.toVncKeyword(args.second()).getValue();
                    final String ending = "cr-lf".equals(mode) ? "\r\n" : "\n";

                    return new VncString(
                            StringUtil
                                .splitIntoLines(text)
                                .stream()
                                .collect(Collectors.joining(ending)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_butlast =
        new VncFunction(
                "str/butlast",
                VncFunction
                    .meta()
                    .arglists("(str/butlast s)")
                    .doc("Returns a possibly empty string of the characters without the last.")
                    .examples("(str/butlast \"abcdef\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                if (args.first() == Nil) {
                    return Nil;
                }
                else {
                    final String s = Coerce.toVncString(args.first()).getValue();
                    return new VncString(s.length() <= 1 ? "" : s.substring(0, s.length()-1));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_butnlast =
        new VncFunction(
                "str/butnlast",
                VncFunction
                    .meta()
                    .arglists("(str/butnlast s n)")
                    .doc("Returns a possibly empty string of the characters without the n last characters.")
                    .examples("(str/butnlast \"abcdef\" 3)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                if (args.first() == Nil) {
                    return Nil;
                }
                else {
                    final String s = Coerce.toVncString(args.first()).getValue();
                    final long n = Coerce.toVncLong(args.second()).getValue();
                   return new VncString(s.length() <= n ? "" : s.substring(0, s.length()-(int)n));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_rest =
        new VncFunction(
                "str/rest",
                VncFunction
                    .meta()
                    .arglists("(str/rest s)")
                    .doc("Returns a possibly empty string of the characters after the first.")
                    .examples("(str/rest \"abcdef\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                if (args.first() == Nil) {
                    return Nil;
                }
                else {
                    final String s = Coerce.toVncString(args.first()).getValue();
                    return new VncString(s.length() < 2 ? "" : s.substring(1));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_nrest =
        new VncFunction(
                "str/nrest",
                VncFunction
                    .meta()
                    .arglists("(str/nrest s n)")
                    .doc("Returns a possibly empty string of the characters after the n first characters.")
                    .examples("(str/nrest \"abcdef\" 3)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                if (args.first() == Nil) {
                    return Nil;
                }
                else {
                    final String s = Coerce.toVncString(args.first()).getValue();
                    final long n = Coerce.toVncLong(args.second()).getValue();
                    return new VncString(s.length() < n ? "" : s.substring((int)n));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_nfirst =
        new VncFunction(
                "str/nfirst",
                VncFunction
                    .meta()
                    .arglists("(str/nfirst s n)")
                    .doc("Returns a string of the n first characters of s.")
                    .examples(
                        "(str/nfirst \"abcdef\" 2)",
                        "(str/nfirst \"abcdef\" 10)",
                        "(str/nfirst \"abcdef\" 0)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                if (args.first() == Nil) {
                    return Nil;
                }
                else {
                    int n = Coerce.toVncLong(args.second()).getValue().intValue();

                    final String s = Coerce.toVncString(args.first()).getValue();
                    n = Math.max(0, Math.min(s.length(), n));
                    return s.isEmpty()
                            ? VncString.empty()
                            : new VncString(s.substring(0, n));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_nlast =
        new VncFunction(
                "str/nlast",
                VncFunction
                    .meta()
                    .arglists("(str/nlast s n)")
                    .doc("Returns a string of the n last characters of s.")
                    .examples(
                        "(str/nlast \"abcdef\" 2)",
                        "(str/nlast \"abcdef\" 10)",
                        "(str/nlast \"abcdef\" 0)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                if (args.first() == Nil) {
                    return Nil;
                }
                else {
                    int n = Coerce.toVncLong(args.second()).getValue().intValue();

                    final String s = Coerce.toVncString(args.first()).getValue();
                    n = Math.max(0, Math.min(s.length(), n));
                    return s.isEmpty()
                            ? VncString.empty()
                            : new VncString(s.substring(s.length()-n, s.length()));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_format =
        new VncFunction(
                "str/format",
                VncFunction
                    .meta()
                    .arglists(
                        "(str/format format args*)",
                        "(str/format locale format args*)")
                    .doc(
                        "Returns a formatted string using the specified format string and arguments.¶" +
                        "Venice uses the Java format syntax.\n\n" +
                        "JavaDoc: [Format Syntax](https://docs.oracle.com/javase/8/docs/api/java/util/Formatter.html#syntax)")
                    .examples(
                        "(str/format \"value: %.4f\" 1.45)",
                        "(str/format (. :java.util.Locale :new \"de\" \"DE\") \"value: %.4f\" 1.45)",
                        "(str/format (. :java.util.Locale :GERMANY) \"value: %.4f\" 1.45)",
                        "(str/format (. :java.util.Locale :new \"de\" \"CH\") \"value: %,d\" 2345000)",
                        "(str/format [ \"de\" ] \"value: %,.2f\" 100000.45)",
                        "(str/format [ \"de\" \"DE\" ] \"value: %,.2f\" 100000.45)",
                        "(str/format [ \"de\" \"CH\" ] \"value: %,.2f\" 100000.45)",
                        "(str/format [ \"en\" \"US\" ] \"value: %,.2f\" 100000.45)",
                        "(str/format [ \"de\" \"DE\" ] \"value: %,d\" 2345000)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args_) {
                final Locale locale = Types.isVncString(args_.first()) ? null : toLocale(args_.first());
                final VncList args = locale == null ? args_ : args_.rest();

                final VncString fmt = Coerce.toVncString(args.first());

                final List<Object> params = args.rest()
                                                .stream()
                                                .map(v -> v instanceof VncNumber || v instanceof VncBoolean
                                                            ? v.convertToJavaObject()
                                                            : v.toString(false))
                                                .collect(Collectors.toList());

                try {
                    return new VncString(String.format(
                                            locale == null ? Locale.getDefault() : locale,
                                            fmt.getValue(),
                                            params.toArray()));
                }
                catch(IllegalFormatException ex) {
                    throw new VncException(ex.getMessage());
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_quote =
        new VncFunction(
                "str/quote",
                VncFunction
                    .meta()
                    .arglists("(str/quote str q)", "(str/quote str start end)")
                    .doc("Quotes a string.")
                    .examples(
                        "(str/quote \"abc\" \"-\")",
                        "(str/quote \"abc\" \"<\" \">\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2, 3);

                final String s = Coerce.toVncString(args.first()).getValue();
                final String start = Coerce.toVncString(args.second()).getValue();
                final String end = (args.size() == 2)
                                        ? start
                                        : Coerce.toVncString(args.nth(2)).getValue();

                return new VncString(start + s + end);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_quoted_Q =
        new VncFunction(
                "str/quoted?",
                VncFunction
                    .meta()
                    .arglists("(str/quoted? str q)", "(str/quoted? str start end)")
                    .doc("Returns true if the string is quoted.")
                    .examples(
                        "(str/quoted? \"-abc-\" \"-\")",
                        "(str/quoted? \"<abc>\" \"<\" \">\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2, 3);

                final String s = Coerce.toVncString(args.first()).getValue();
                final String start = Coerce.toVncString(args.second()).getValue();
                final String end = (args.size() == 2)
                                        ? start
                                        : Coerce.toVncString(args.nth(2)).getValue();

                return VncBoolean.of(s.startsWith(start) && s.endsWith(end));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_double_quote =
        new VncFunction(
                "str/double-quote",
                VncFunction
                    .meta()
                    .arglists("(str/double-quote str)")
                    .doc("Double quotes a string.")
                    .examples(
                        "(str/double-quote \"abc\")",
                        "(str/double-quote \"\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final String s = Coerce.toVncString(args.first()).getValue();

                return new VncString("\"" + s + "\"");
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_double_unquote =
        new VncFunction(
                "str/double-unquote",
                VncFunction
                    .meta()
                    .arglists("(str/double-unquote str)")
                    .doc("Unquotes a double quoted string.")
                    .examples(
                        "(str/double-unquote \"\\\"abc\\\"\")",
                        "(str/double-unquote \"\\\"\\\"\")",
                        "(str/double-unquote nil)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                if (args.first() == Nil) {
                    return Nil;
                }
                else {
                    final String s = Coerce.toVncString(args.first()).getValue();

                    if (s.startsWith("\"") && s.endsWith("\"")) {
                        return new VncString(s.length() == 2 ? "" : s.substring(1, s.length()-1));
                    }
                    else {
                        return args.first();
                    }
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_double_quoted_Q =
        new VncFunction(
                "str/double-quoted?",
                VncFunction
                    .meta()
                    .arglists("(str/double-quoteed? str)")
                    .doc("Returns true if the string is double quoted.")
                    .examples("(str/double-quoted? \"\\\"abc\\\"\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                if (args.first() == Nil) {
                    return False;
                }
                else {
                    final String s = Coerce.toVncString(args.first()).getValue();

                    return VncBoolean.of(s.startsWith("\"") && s.endsWith("\""));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_truncate =
        new VncFunction(
                "str/truncate",
                VncFunction
                    .meta()
                    .arglists("(str/truncate s maxlen marker mode*)")
                    .doc(
                        "Truncates a string to the max lenght maxlen and adds the " +
                        "marker if the string needs to be truncated. The marker is " +
                        "added to the start, middle, or end of the string depending " +
                        "on the mode :start, :middle, :end. The mode defaults to :end")
                    .examples(
                        "(str/truncate \"abcdefghij\" 20 \"...\")",
                        "(str/truncate \"abcdefghij\" 9 \"...\")",
                        "(str/truncate \"abcdefghij\" 4 \"...\")",
                        "(str/truncate \"abcdefghij\" 7 \"...\" :start)",
                        "(str/truncate \"abcdefghij\" 7 \"...\" :middle)",
                        "(str/truncate \"abcdefghij\" 7 \"...\" :end)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 3, 4);

                if (args.first() == Nil) {
                    return Nil;
                }

                final String text = Coerce.toVncString(args.first()).getValue();
                final int maxLen = Coerce.toVncLong(args.second()).getValue().intValue();
                final String marker = Coerce.toVncString(args.nth(2)).getValue();
                final String mode = Coerce.toVncKeyword(args.nthOrDefault(3, new VncKeyword(":end")))
                                          .getValue();

                int lenMarker = marker.length();

                if (maxLen <= lenMarker){
                    throw new VncException("A maxLen must greater than the length of the truncation marker");
                }

                if (text == null || text.length() <= maxLen) {
                    return args.first();
                }


                switch(mode) {
                    case "start": {
                        final int lenTail = maxLen - lenMarker;
                        return new VncString(marker + text.substring(text.length() - lenTail));
                    }
                    case "middle": {
                        final int lenStart = maxLen / 2 - lenMarker / 2;
                        final int lenTail = maxLen - lenStart - lenMarker;
                        return new VncString(
                                    text.substring(0, lenStart)
                                        + marker
                                        + text.substring(text.length() - lenTail));
                    }
                    case "end": {
                        final int lenStart = maxLen - lenMarker;
                        return new VncString(text.substring(0, lenStart) + marker);
                    }
                }

                throw new VncException("Invalid truncation mode ':" + mode + "'");
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_expand =
        new VncFunction(
                "str/expand",
                VncFunction
                    .meta()
                    .arglists("(str/expand s len fill mode*)")
                    .doc(
                        "Expands a string to the max lenght len. Fills up with the fill" +
                        "string if the string needs to be expanded. The fill string is " +
                        "added to the start or end of the string depending on the mode " +
                        ":start, :end. The mode defaults to :end")
                    .examples(
                        "(str/expand \"abcdefghij\" 8 \".\")",
                        "(str/expand \"abcdefghij\" 20 \".\")",
                        "(str/expand \"abcdefghij\" 20 \".\" :start)",
                        "(str/expand \"abcdefghij\" 20 \".\" :end)",
                        "(str/expand \"abcdefghij\" 30 \"1234\" :start)",
                        "(str/expand \"abcdefghij\" 30 \"1234\" :end)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 3, 4);

                final String text = args.first() == Nil
                                        ? ""
                                        : Coerce.toVncString(args.first()).getValue();
                final int len = Coerce.toVncLong(args.second()).getValue().intValue();
                final String fill = Coerce.toVncString(args.nth(2)).getValue();
                final String mode = Coerce.toVncKeyword(args.nthOrDefault(3, new VncKeyword(":end")))
                                          .getValue();

                if (fill.isEmpty()){
                    throw new VncException("A fill string must not be empty");
                }

                if (text.length() >= len) {
                    return args.first();
                }

                final int gap = len - text.length();

                final StringBuilder filling = new StringBuilder();
                while(filling.length() < gap) {
                    final int delta = gap - filling.length();
                    filling.append(delta >= fill.length()
                                    ? fill
                                    : fill.substring(0, delta));
                }

                switch(mode) {
                    case "start": return new VncString(filling + text);
                    case "end": return new VncString(text + filling);
                }

                throw new VncException("Invalid truncation mode ':" + mode + "'");
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_strip_start =
        new VncFunction(
                "str/strip-start",
                VncFunction
                    .meta()
                    .arglists("(str/strip-start s substr)")
                    .doc("Removes a substr only if it is at the beginning of a s, otherwise returns s.")
                    .examples(
                        "(str/strip-start \"abcdef\" \"abc\")",
                        "(str/strip-start \"abcdef\" \"def\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

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
                    .arglists("(str/strip-end s substr)")
                    .doc("Removes a substr only if it is at the end of a s, otherwise returns s.")
                    .examples(
                        "(str/strip-end \"abcdef\" \"def\")",
                        "(str/strip-end \"abcdef\" \"abc\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

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
                    .arglists("(str/strip-indent s)")
                    .doc("Strip the indent of a multi-line string. The first line's leading whitespaces define the indent.")
                    .examples("(str/strip-indent \"  line1\n    line2\n    line3\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

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
                    .arglists("(str/strip-margin s)")
                    .doc(
                        "Strips leading whitespaces upto and including the margin '|' " +
                        "from each line in a multi-line string.")
                    .examples(
                        "(str/strip-margin \"line1\n  |  line2\n  |  line3\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

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
                    .arglists("(str/repeat s n)", "(str/repeat s n sep)")
                    .doc("Repeats s n times with an optional separator.")
                    .examples(
                        "(str/repeat \"abc\" 0)",
                        "(str/repeat \"abc\" 3)",
                        "(str/repeat \"abc\" 3 \"-\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2, 3);

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
                    .arglists("(str/digit? s)")
                    .doc(
                        "True if s is a char and the char is a digit. \n\n" +
                        "Defined by Java Character.isDigit(ch).")
                    .examples(
                        "(str/digit? #\\8)",
                        "(str/digit? \"8\")")
                    .seeAlso("str/letter?", "str/hexdigit?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal v = args.first();

                if (Types.isVncChar(v)) {
                    return VncBoolean.of(Character.isDigit(((VncChar)v).getValue()));
                }
                else {
                    return False;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_hexdigit_Q =
        new VncFunction(
                "str/hexdigit?",
                VncFunction
                    .meta()
                    .arglists("(str/hexdigit? s)")
                    .doc(
                        "True if s is a char and the char is a hex digit.")
                    .examples(
                        "(str/hexdigit? #\\8)",
                        "(str/hexdigit? #\\a)",
                        "(str/hexdigit? #\\A)",
                        "(str/hexdigit? #\\Y)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal v = args.first();

                if (Types.isVncChar(v)) {
                    final int ch = ((VncChar)v).getValue().charValue();
                    return VncBoolean.of(
                                (ch >= '0' && ch <= '9')
                                || (ch >= 'A' && ch <= 'F')
                                || (ch >= 'a' && ch <= 'f'));
                }
                else {
                    return False;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_letter_Q =
        new VncFunction(
                "str/letter?",
                VncFunction
                    .meta()
                    .arglists("(str/letter? s)")
                    .doc(
                        "True if s is a char and the char is a letter. \n\n" +
                        "Defined by Java Character.isLetter(ch).")
                    .examples(
                        "(str/letter? #\\x)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal v = args.first();

                if (Types.isVncChar(v)) {
                    return VncBoolean.of(Character.isLetter(((VncChar)v).getValue()));
                }
                else if (Types.isVncString(v)) {
                    final String str = Coerce.toVncString(v).getValue();
                    if (str.length() != 1) {
                        throw new VncException(String.format(
                                "Function 'str/letter?' expects a single char string. Got a '%s'.",
                                Types.getType(v)));
                    }
                    return VncBoolean.of(Character.isLetter(str.charAt(0)));
                }
                else {
                    return False;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_letter_or_digit_Q =
        new VncFunction(
                "str/letter-or-digit?",
                VncFunction
                    .meta()
                    .arglists("(str/letter-or-digit? s)")
                    .doc(
                        "True if s is a char the char is a letter or a digit. \n\n" +
                        "Defined by Java Character.isLetterOrDigit(ch).")
                    .examples(
                        "(str/letter-or-digit? #\\x)",
                        "(str/letter-or-digit? #\\X)",
                        "(str/letter-or-digit? #\\!)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal v = args.first();

                if (Types.isVncChar(v)) {
                    return VncBoolean.of(Character.isLetterOrDigit(((VncChar)v).getValue()));
                }
                else {
                    return False;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_lower_case_Q =
        new VncFunction(
                "str/lower-case?",
                VncFunction
                    .meta()
                    .arglists("(str/lower-case? s)")
                    .doc(
                        "True if s is a char and the char is a lower case char. \n\n" +
                        "Defined by Java Character.isLowerCase(ch).")
                    .examples(
                        "(str/lower-case? #\\x)",
                        "(str/lower-case? #\\X)",
                        "(str/lower-case? #\\8)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal v = args.first();

                if (Types.isVncChar(v)) {
                    return VncBoolean.of(Character.isLowerCase(((VncChar)v).getValue()));
                }
                else {
                    return False;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_upper_case_Q =
        new VncFunction(
                "str/upper-case?",
                VncFunction
                    .meta()
                    .arglists("(str/upper-case? s)")
                    .doc(
                        "True if s is a char and the char is an upper case char. \n\n" +
                        "Defined by Java Character.isUpperCase(ch).")
                    .examples(
                        "(str/upper-case? #\\x)",
                        "(str/upper-case? #\\X)",
                        "(str/upper-case? #\\8)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal v = args.first();

                if (Types.isVncChar(v)) {
                    return VncBoolean.of(Character.isUpperCase(((VncChar)v).getValue()));
                }
                else {
                    return False;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_linefeed_Q =
        new VncFunction(
                "str/linefeed?",
                VncFunction
                    .meta()
                    .arglists("(str/linefeed? s)")
                    .doc("True if s is a char and the char is a linefeed.")
                    .examples(
                        "(str/linefeed? #\\newline)",
                        "(str/linefeed? (first \"\n\"))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal v = args.first();

                if (Types.isVncChar(v)) {
                    return VncBoolean.of(((VncChar)v).getValue() == '\n');
                }
                else {
                    return False;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_whitespace_Q =
        new VncFunction(
                "str/whitespace?",
                VncFunction
                    .meta()
                    .arglists("(str/whitespace? s)")
                    .doc(
                        "True if s is char and the char is a whitespace. \n\n" +
                        "Defined by Java Character.isWhitespace(ch).")
                    .examples(
                        "(str/whitespace? #\\space)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal v = args.first();

                if (Types.isVncChar(v)) {
                    return VncBoolean.of(Character.isWhitespace(((VncChar)v).getValue()));
                }
                else {
                    return False;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_lorem_ipsum =
            new VncFunction(
                    "str/lorem-ipsum",
                    VncFunction
                        .meta()
                        .arglists("(str/lorem-ipsum & options)")
                        .doc(
                            "Creates an arbitrary length Lorem Ipsum text. \n\n" +
                            "Options: \n\n" +
                            "| :chars n      | returns n characters (limited to " + LoremIpsum.getMaxChars() + ") |\n" +
                            "| :paragraphs n | returns n paragraphs (limited to " + LoremIpsum.getMaxParagraphs() + ") |\n")
                        .examples
                            ("(str/lorem-ipsum :chars 250)",
                             "(str/lorem-ipsum :paragraphs 1)")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertMinArity(this, args, 0);

                    final VncHashMap options = VncHashMap.ofAll(args);

                    final VncVal chars = options.get(new VncKeyword("chars"));
                    if (Types.isVncLong(chars)) {
                        return new VncString(
                                LoremIpsum.loremIpsum_Chars(
                                        Coerce.toVncLong(chars).getValue().intValue()));
                    }

                    final VncVal paragraphs = options.get(new VncKeyword("paragraphs"));
                    if (Types.isVncLong(paragraphs)) {
                        return new VncString(
                                LoremIpsum.loremIpsum_Paragraphs(
                                        Coerce.toVncLong(paragraphs).getValue().intValue()));
                    }

                    throw new VncException("Function 'str/lorem-ipsum' invalid options");
                }

                private static final long serialVersionUID = -1848883965231344442L;
            };

    public static VncFunction str_wrap =
        new VncFunction(
                "str/wrap",
                VncFunction
                    .meta()
                    .arglists("(str/wrap text & options)")
                    .doc(
                        "Wraps ascii text to lines with a length of maxlen characters . \n\n" +
                        "Options: \n\n" +
                        "| :maxlen n                           | the max len of line (default 80) |\n" +
                        "| :line-wrap {:anywhere, :break-word} | controls the line wrap |\n")
                     .examples
                        ("(-> (str/lorem-ipsum :paragraphs 1)               \n" +
                         "    (str/wrap :maxlen 80 :line-wrap :break-word)) ")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                final VncString text = Coerce.toVncString(args.first());
                final VncHashMap options = VncHashMap.ofAll(args.rest());

                long maxlen = 80;
                boolean breakword = true;

                final VncVal maxlen_ = options.get(new VncKeyword(":maxlen"));
                if (Types.isVncLong(maxlen_)) {
                    maxlen = ((VncLong)maxlen_).toJavaLong();
                    maxlen = Math.max(2, maxlen);
                }

                final VncVal linewrap_ = options.get(new VncKeyword(":line-wrap"));
                if (Types.isVncKeyword(linewrap_)) {
                    breakword = "break-word".equals(((VncKeyword)linewrap_).getValue());
                }

                final String s = text.getValue();
                final List<String> lines = breakword
                                                ? LineWrap.softWrap(s, (int)maxlen)
                                                : LineWrap.hardWrap(s, (int) maxlen);

                return new VncString(String.join("\n", lines));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_bytebuf_to_hex =
        new VncFunction(
                "str/bytebuf-to-hex",
                VncFunction
                    .meta()
                    .arglists(
                        "(str/bytebuf-to-hex data)",
                        "(str/bytebuf-to-hex data :upper)")
                    .doc(
                        "Converts byte data to a hex string using the hexadecimal digits: " +
                        "`0123456789abcdef`. ¶" +
                        "If the :upper options is passed the hex digits `0123456789ABCDEF` " +
                        "are used.")
                    .examples(
                        "(str/bytebuf-to-hex (bytebuf [0 1 2 3 4 5 6]))",
                        "(str/bytebuf-to-hex (bytebuf [202 254]) :upper)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                if (args.first() == Nil) {
                    return Nil;
                }

                final VncByteBuffer data = Coerce.toVncByteBuffer(args.first());

                if (args.size() == 1) {
                    return new VncString(HexUtil.toString(data.getBytes()));
                }
                else {
                    final VncKeyword opt = Coerce.toVncKeyword(args.second());
                    if (opt.getValue().equalsIgnoreCase("upper")) {
                        return new VncString(HexUtil.toStringUpperCase(data.getBytes()));
                    }
                    else {
                        throw new VncException("Function 'str/bytebuf-to-hex' expects the option :upper");
                    }
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_hex_to_bytebuf =
        new VncFunction(
                "str/hex-to-bytebuf",
                VncFunction
                    .meta()
                    .arglists("(str/hex-to-bytebuf hex)")
                    .doc("Converts a hex string to a bytebuf")
                    .examples(
                        "(str/hex-to-bytebuf \"005E4AFF\")",
                        "(str/hex-to-bytebuf \"005e4aff\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                if (args.first() == Nil) {
                    return Nil;
                }

                return new VncByteBuffer(HexUtil.toBytes(Coerce.toVncString(args.first()).getValue()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_format_bytebuf =
        new VncFunction(
                "str/format-bytebuf",
                VncFunction
                    .meta()
                    .arglists(
                        "(str/format-bytebuf data delimiter & options)")
                    .doc(
                        "Formats a bytebuffer. \n\n" +
                        "Options \n\n" +
                        "| :prefix0x | prefix with 0x |")
                    .examples(
                        "(str/format-bytebuf (bytebuf [0 34 67 -30 -1]) nil)",
                        "(str/format-bytebuf (bytebuf [0 34 67 -30 -1]) \"\")",
                        "(str/format-bytebuf (bytebuf [0 34 67 -30 -1]) \", \")",
                        "(str/format-bytebuf (bytebuf [0 34 67 -30 -1]) \", \" :prefix0x)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2, 3);

                if (args.first() == Nil) {
                    return Nil;
                }

                final VncByteBuffer data = Coerce.toVncByteBuffer(args.first());
                final String delimiter = args.second() == Nil ? "" : Coerce.toVncString(args.second()).getValue();

                if (args.size() == 2) {
                    return new VncString(HexFormatter.toHex(data.getBytes(), delimiter, false));
                }
                else {
                    final VncKeyword opt = Coerce.toVncKeyword(args.third());
                    if (opt.getValue().equalsIgnoreCase("prefix0x")) {
                        return new VncString(HexFormatter.toHex(data.getBytes(), delimiter, true));
                    }
                    else {
                        throw new VncException("Function 'str/format-bytebuf' expects the option :prefix0x");
                    }
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_levenshtein =
        new VncFunction(
                "str/levenshtein",
                VncFunction
                    .meta()
                    .arglists("(str/levenshtein s1 s2)")
                    .doc(
                        "Returns the *Levenshtein* distance of two strings.\n\n" +
                        "The *Damerau-Levenshtein* algorithm is an extension to the *Levenshtein* " +
                        "algorithm which solves the edit distance problem between a source string and " +
                        "a target string with the following operations:\n\n" +
                        "  * Character Insertion\n" +
                        "  * Character Deletion\n" +
                        "  * Character Replacement\n" +
                        "  * Adjacent Character Swap\n\n" +
                        "Note that the adjacent character swap operation is an edit that may be " +
                        "applied when two adjacent characters in the source string match two adjacent " +
                        "characters in the target string, but in reverse order, rather than a general " +
                        "allowance for adjacent character swaps.\n\n" +
                        "This implementation allows the client to specify the costs of the various " +
                        "edit operations with the restriction that the cost of two swap operations " +
                        "must not be less than the cost of a delete operation followed by an insert " +
                        "operation. This restriction is required to preclude two swaps involving the " +
                        "same character being required for optimality which, in turn, enables a fast " +
                        "dynamic programming solution.\n\n" +
                        "The cost of the *Damerau-Levenshtein* algorithm is `O(n*m)` where `n` is " +
                        "the length of the source string and `m is the length of the target string. " +
                        "This implementation consumes `O(n*m)` space.")
                    .examples(
                        "(str/levenshtein \"Tier\" \"Tor\")",
                        "(str/levenshtein \"Tier\" \"tor\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                return new VncLong(
                            Levenshtein.distance(
                                Coerce.toVncString(args.first()).getValue(),
                                Coerce.toVncString(args.second()).getValue()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_encode_base64 =
        new VncFunction(
                "str/encode-base64",
                VncFunction
                    .meta()
                    .arglists("(str/encode-base64 data)")
                    .doc("Base64 encode.")
                    .examples("(str/encode-base64 (bytebuf [0 1 2 3 4 5 6]))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal arg = args.first();
                if (arg == Nil) {
                    return Nil;
                }
                else {
                    final byte[] buf = Coerce.toVncByteBuffer(arg).getBytes();
                    return new VncString(Base64.getEncoder().encodeToString(buf));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_decode_base64 =
        new VncFunction(
                "str/decode-base64",
                VncFunction
                    .meta()
                    .arglists("(str/decode-base64 s)")
                    .doc("Base64 decode.")
                    .examples("(str/decode-base64 (str/encode-base64 (bytebuf [0 1 2 3 4 5 6])))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal arg = args.first();
                if (arg == Nil) {
                    return Nil;
                }
                else {
                    final String base64 = Coerce.toVncString(arg).getValue();
                    return new VncByteBuffer(Base64.getDecoder().decode(base64));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_encode_url =
        new VncFunction(
                "str/encode-url",
                VncFunction
                    .meta()
                    .arglists("(str/encode-url s)")
                    .doc("URL encode.")
                    .examples("(str/encode-url \"The string ü@foo-bar\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                try {
                    final VncVal arg = args.first();
                    if (arg == Nil) {
                        return Nil;
                    }
                    else {
                        final String s = Coerce.toVncString(arg).getValue();
                        return new VncString(URLEncoder.encode(s, "UTF-8"));
                    }
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
                    .arglists("(str/decode-url s)")
                    .doc("URL decode.")
                    .examples("(str/decode-url \"The+string+%C3%BC%40foo-bar\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                try {
                    final VncVal arg = args.first();
                    if (arg == Nil) {
                        return Nil;
                    }
                    else {
                        final String s = Coerce.toVncString(arg).getValue();
                        return new VncString(URLDecoder.decode(s, "UTF-8"));
                    }
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
                    .arglists("(str/escape-html s)")
                    .doc("HTML escape. Escapes `&`, `<`, `>`, `\"`, `'`, and the non blocking space `U+00A0`")
                    .examples("(str/escape-html \"1 2 3 & < > \\\" ' \\u00A0\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal arg = args.first();
                if (arg == Nil) {
                    return Nil;
                }
                else {
                    final String s = Coerce.toVncString(arg).getValue();
                    return new VncString(StringEscapeUtil.escapeHtml(s));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_escape_xml =
        new VncFunction(
                "str/escape-xml",
                VncFunction
                    .meta()
                    .arglists("(str/escape-xml s)")
                    .doc("XML escape. Escapes `&`, `<`, `>`, `\"`, `'`")
                    .examples("(str/escape-xml \"1 2 3 & < > \\\" '\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal arg = args.first();
                if (arg == Nil) {
                    return Nil;
                }
                else {
                    final String s = Coerce.toVncString(arg).getValue();
                    return new VncString(StringEscapeUtil.escapeXml(s));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str_valid_email_addr_Q =
        new VncFunction(
                "str/valid-email-addr?",
                VncFunction
                    .meta()
                    .arglists("(str/valid-email-addr? e)")
                    .doc("Returns true if e is a valid email address according to RFC5322, else returns false")
                    .examples(
                        "(str/valid-email-addr? \"user@domain.com\")",
                        "(str/valid-email-addr? \"user@domain.co.in\")",
                        "(str/valid-email-addr? \"user.name@domain.com\")",
                        "(str/valid-email-addr? \"user_name@domain.com\")",
                        "(str/valid-email-addr? \"username@yahoo.corporate.in\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal arg = args.first();
                if (arg == Nil) {
                    return False;
                }
                else {
                    final String s = Coerce.toVncString(arg).getValue();
                    return VncBoolean.of(s.matches(EMAIL_REGEX));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    private static Locale toLocale(final VncVal locale) {
        if (Types.isVncJavaObject(locale, Locale.class)) {
            return (Locale)((VncJavaObject)locale).getDelegate();
        }
        else if (Types.isVncSequence(locale)) {
            final VncSequence localeSeq = (VncSequence)locale;
            switch (localeSeq.size()) {
                case 0:
                    return Locale.getDefault();
                case 1:
                    // language
                    return new Locale(Coerce.toVncString(localeSeq.first()).getValue());
                case 2:
                    // language, country
                    return new Locale(
                                Coerce.toVncString(localeSeq.first()).getValue(),
                                Coerce.toVncString(localeSeq.second()).getValue());
                default:
                    // language, country, variant
                    return new Locale(
                                Coerce.toVncString(localeSeq.first()).getValue(),
                                Coerce.toVncString(localeSeq.second()).getValue(),
                                Coerce.toVncString(localeSeq.third()).getValue());
            }
        }
        else {
            return null;
        }
    }


    // see: https://howtodoinjava.com/regex/java-regex-validate-email-address/
    private static final String EMAIL_REGEX =
            "^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";



    ///////////////////////////////////////////////////////////////////////////
    // types_ns is namespace of type functions
    ///////////////////////////////////////////////////////////////////////////

    public static final Map<VncVal, VncVal> ns =
            new SymbolMapBuilder()
                    .add(str_blank_Q)
                    .add(str_not_blank_Q)
                    .add(str_starts_with_Q)
                    .add(str_ends_with_Q)
                    .add(str_contains_Q)
                    .add(str_equals_ignore_case_Q)
                    .add(str_char_Q)
                    .add(str_digit_Q)
                    .add(str_hexdigit_Q)
                    .add(str_letter_Q)
                    .add(str_letter_or_digit_Q)
                    .add(str_linefeed_Q)
                    .add(str_whitespace_Q)
                    .add(str_upper_case_Q)
                    .add(str_lower_case_Q)
                    .add(str_trim)
                    .add(str_trim_left)
                    .add(str_trim_right)
                    .add(str_trim_to_nil)
                    .add(str_align)
                    .add(str_index_of)
                    .add(str_last_index_of)
                    .add(str_replace_first)
                    .add(str_replace_last)
                    .add(str_replace_all)
                    .add(str_reverse)
                    .add(str_lower_case)
                    .add(str_upper_case)
                    .add(str_join)
                    .add(str_subs)
                    .add(str_pos)
                    .add(str_chars)
                    .add(str_split)
                    .add(str_split_at)
                    .add(str_split_lines)
                    .add(str_split_columns)
                    .add(str_cr_lf)
                    .add(str_format)
                    .add(str_rest)
                    .add(str_nrest)
                    .add(str_nfirst)
                    .add(str_nlast)
                    .add(str_butlast)
                    .add(str_butnlast)
                    .add(str_quote)
                    .add(str_double_quote)
                    .add(str_double_unquote)
                    .add(str_quoted_Q)
                    .add(str_double_quoted_Q)
                    .add(str_truncate)
                    .add(str_expand)
                    .add(str_strip_start)
                    .add(str_strip_end)
                    .add(str_strip_indent)
                    .add(str_strip_margin)
                    .add(str_repeat)
                    .add(str_lorem_ipsum)
                    .add(str_wrap)
                    .add(str_hex_to_bytebuf)
                    .add(str_bytebuf_to_hex)
                    .add(str_format_bytebuf)
                    .add(str_encode_base64)
                    .add(str_decode_base64)
                    .add(str_encode_url)
                    .add(str_decode_url)
                    .add(str_escape_html)
                    .add(str_escape_xml)
                    .add(str_valid_email_addr_Q)
                    .add(str_levenshtein)
                    .toMap();
}
