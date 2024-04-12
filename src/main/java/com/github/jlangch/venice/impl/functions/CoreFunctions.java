/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
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

import static com.github.jlangch.venice.impl.functions.FunctionsUtil.removeNilValues;
import static com.github.jlangch.venice.impl.types.Constants.Nil;
import static com.github.jlangch.venice.impl.types.VncBoolean.False;
import static com.github.jlangch.venice.impl.types.VncBoolean.True;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.jlangch.venice.ContinueException;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.env.GenSym;
import com.github.jlangch.venice.impl.namespaces.Namespaces;
import com.github.jlangch.venice.impl.reader.HighlightClass;
import com.github.jlangch.venice.impl.reader.HighlightItem;
import com.github.jlangch.venice.impl.reader.HighlightParser;
import com.github.jlangch.venice.impl.reader.Reader;
import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.IVncFunction;
import com.github.jlangch.venice.impl.types.VncBigDecimal;
import com.github.jlangch.venice.impl.types.VncBigInteger;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncByteBuffer;
import com.github.jlangch.venice.impl.types.VncChar;
import com.github.jlangch.venice.impl.types.VncDouble;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncInteger;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncJust;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncMultiArityFunction;
import com.github.jlangch.venice.impl.types.VncMultiFunction;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncThreadLocal;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncCollection;
import com.github.jlangch.venice.impl.types.collections.VncDelayQueue;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncHashSet;
import com.github.jlangch.venice.impl.types.collections.VncJavaList;
import com.github.jlangch.venice.impl.types.collections.VncJavaSet;
import com.github.jlangch.venice.impl.types.collections.VncLazySeq;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncMapEntry;
import com.github.jlangch.venice.impl.types.collections.VncMutable;
import com.github.jlangch.venice.impl.types.collections.VncMutableList;
import com.github.jlangch.venice.impl.types.collections.VncMutableMap;
import com.github.jlangch.venice.impl.types.collections.VncMutableSet;
import com.github.jlangch.venice.impl.types.collections.VncMutableVector;
import com.github.jlangch.venice.impl.types.collections.VncOrderedMap;
import com.github.jlangch.venice.impl.types.collections.VncQueue;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.collections.VncSet;
import com.github.jlangch.venice.impl.types.collections.VncSortedMap;
import com.github.jlangch.venice.impl.types.collections.VncSortedSet;
import com.github.jlangch.venice.impl.types.collections.VncStack;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.custom.VncCustomType;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.MeterRegistry;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;
import com.github.jlangch.venice.impl.util.callstack.CallFrame;
import com.github.jlangch.venice.impl.util.callstack.CallStack;
import com.github.jlangch.venice.impl.util.transducer.Reducer;


public class CoreFunctions {


    ///////////////////////////////////////////////////////////////////////////
    // Scalar functions
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction nil_Q =
        new VncFunction(
                "nil?",
                VncFunction
                    .meta()
                    .arglists("(nil? x)")
                    .doc("Returns true if x is nil, false otherwise")
                    .examples(
                        "(nil? nil)",
                        "(nil? 0)",
                        "(nil? false)")
                    .seeAlso("some?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(args.first() == Nil);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction some_Q =
        new VncFunction(
                "some?",
                VncFunction
                    .meta()
                    .arglists("(some? x)")
                    .doc("Returns true if x is not nil, false otherwise")
                    .examples(
                        "(some? nil)",
                        "(some? 0)",
                        "(some? 4.0)",
                        "(some? false)",
                        "(some? [])",
                        "(some? {})")
                    .seeAlso("nil?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(args.first() != Nil);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction true_Q =
        new VncFunction(
                "true?",
                VncFunction
                    .meta()
                    .arglists("(true? x)")
                    .doc("Returns true if x is true, false otherwise")
                    .examples(
                        "(true? true)",
                        "(true? false)",
                        "(true? nil)",
                        "(true? 0)",
                        "(true? (== 1 1))")
                    .seeAlso("false?", "not")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(VncBoolean.isTrue(args.first()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction false_Q =
        new VncFunction(
                "false?",
                VncFunction
                    .meta()
                    .arglists("(false? x)")
                    .doc("Returns true if x is false, false otherwise")
                    .examples(
                        "(false? true)",
                        "(false? false)",
                        "(false? nil)",
                        "(false? 0)",
                        "(false? (== 1 2))")
                    .seeAlso("true?", "not")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(VncBoolean.isFalse(args.first()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction boolean_Q =
        new VncFunction(
                "boolean?",
                VncFunction
                    .meta()
                    .arglists("(boolean? n)")
                    .doc("Returns true if n is a boolean")
                    .examples(
                        "(boolean? true)",
                        "(boolean? false)",
                        "(boolean? nil)",
                        "(boolean? 0)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(Types.isVncBoolean(args.first()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction int_Q =
        new VncFunction(
                "int?",
                VncFunction
                    .meta()
                    .arglists("(int? n)")
                    .doc("Returns true if n is an int")
                    .examples(
                        "(int? 4I)",
                        "(int? 4)",
                        "(int? 3.1)",
                        "(int? true)",
                        "(int? nil)",
                        "(int? {})")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(Types.isVncInteger(args.first()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction long_Q =
        new VncFunction(
                "long?",
                VncFunction
                    .meta()
                    .arglists("(long? n)")
                    .doc("Returns true if n is a long")
                    .examples(
                        "(long? 4)",
                        "(long? 4I)",
                        "(long? 3.1)",
                        "(long? true)",
                        "(long? nil)",
                        "(long? {})")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(Types.isVncLong(args.first()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction double_Q =
        new VncFunction(
                "double?",
                VncFunction
                    .meta()
                    .arglists("(double? n)")
                    .doc("Returns true if n is a double")
                    .examples(
                        "(double? 4.0)",
                        "(double? 3)",
                        "(double? 3I)",
                        "(double? 3.0M)",
                        "(double? true)",
                        "(double? nil)",
                        "(double? {})")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(Types.isVncDouble(args.first()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction decimal_Q =
        new VncFunction(
                "decimal?",
                VncFunction
                    .meta()
                    .arglists("(decimal? n)")
                    .doc("Returns true if n is a decimal")
                    .examples(
                        "(decimal? 4.0M)",
                        "(decimal? 4.0)",
                        "(decimal? 3)",
                        "(decimal? 3I)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(Types.isVncBigDecimal(args.first()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction bigint_Q =
        new VncFunction(
                "bigint?",
                VncFunction
                    .meta()
                    .arglists("(bigint? n)")
                    .doc("Returns true if n is a big integer")
                    .examples(
                        "(bigint? 4.0N)",
                        "(bigint? 4.0)",
                        "(bigint? 3)",
                        "(bigint? 3I)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(Types.isVncBigInteger(args.first()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction number_Q =
        new VncFunction(
                "number?",
                VncFunction
                    .meta()
                    .arglists("(number? n)")
                    .doc("Returns true if n is a number (int, long, double, or decimal)")
                    .examples(
                        "(number? 4I))",
                        "(number? 4)",
                        "(number? 4.0M)",
                        "(number? 4.0)",
                        "(number? true)",
                        "(number? \"a\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(
                            Types.isVncLong(args.first())
                                || Types.isVncInteger(args.first())
                                || Types.isVncDouble(args.first())
                                || Types.isVncBigDecimal(args.first()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction string_Q =
        new VncFunction(
                "string?",
                VncFunction
                    .meta()
                    .arglists("(string? x)")
                    .doc("Returns true if x is a string")
                    .examples(
                        "(string? \"abc\")",
                        "(string? 1)",
                        "(string? nil)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                if (args.first() instanceof VncKeyword) {
                    return False;
                }
                else if (args.first() instanceof VncString) {
                    return True;
                }
                else {
                    return False;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction char_cast =
        new VncFunction(
                "char",
                VncFunction
                    .meta()
                    .arglists("(char c)")
                    .doc("Converts a number or s single char string to a char.")
                    .examples(
                        "(char 65)",
                        "(char \"A\")",
                        "(long (char \"A\"))",
                        "(str/join (map char [65 66 67 68]))",
                        "(map #(- (long %) (long (char \"0\"))) (str/chars \"123456\"))")
                    .seeAlso("char?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal c = args.first();

                if (c == Nil) {
                    return Nil;
                }
                else if (Types.isVncChar(c)) {
                    return c;
                }
                else if (Types.isVncString(c)) {
                    final String s = ((VncString)c).getValue();
                    if (s.length() == 1) {
                        return new VncChar(s.charAt(0));
                    }
                    else {
                        throw new VncException(
                                "Function 'char' expects a string type argument of length 1.");
                    }
                }
                else if (Types.isVncInteger(c)) {
                    return new VncChar((char)((VncInteger)c).getValue().intValue());
                }
                else if (Types.isVncLong(c)) {
                    return new VncChar((char)((VncLong)c).getValue().intValue());
                }
                else {
                    throw new VncException(String.format(
                            "Function 'char' does not allow %s argument.",
                            Types.getType(c)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction char_Q =
        new VncFunction(
                "char?",
                VncFunction
                    .meta()
                    .arglists("(char? s)")
                    .doc("Returns true if s is a char.")
                    .examples("(char? #\\a)")
                    .seeAlso("char")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(Types.isVncChar(args.first()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction char_literals =
        new VncFunction(
                "char-literals",
                VncFunction
                    .meta()
                    .arglists("(char-literals)")
                    .doc(
                        "Returns all defined char literals. \n\n" +
                        renderCharLiteralsMarkdownTable())
                    .examples("(char-literals)")
                    .seeAlso("char", "char?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                return VncList.ofList(
                    VncChar
                        .symbols()
                        .entrySet()
                        .stream()
                        .map(e -> VncList.of(
                                    new VncString(e.getKey()),
                                    new VncString(e.getValue().toUnicode()),
                                    new VncString("'" + e.getValue().getValue() + "'")))
                        .collect(Collectors.toList()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction char_escaped =
        new VncFunction(
                "char-escaped",
                VncFunction
                    .meta()
                    .arglists("(char-escaped c)")
                    .doc(
                        "Returns the ASCII escaped character for c. \n\n" +
                        "  -  `\\'` single quote¶\n" +
                        "  -  `\\\"` double quote¶\n" +
                        "  -  `\\\\` backslash¶\n" +
                        "  -  `\\n` new line¶\n" +
                        "  -  `\\r` carriage return¶\n" +
                        "  -  `\\t` tab¶\n" +
                        "  -  `\\b` backspace¶\n" +
                        "  -  `\\f` form feed¶\n" +
                        "  -  `\\0` null character¶\n" +
                        "  -  in all other cases returns the character c")
                    .examples(
                        "(char-escaped #\\n)",
                        "(char-escaped #\\a)")
                    .seeAlso("char", "char?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final char ch = Coerce.toVncChar(args.first()).getValue();
                switch (ch) {
                    case '\'':  return new VncChar('\'');
                    case '\"':  return new VncChar('"');
                    case '\\':  return new VncChar('\\');
                    case 'n':   return new VncChar('\n');
                    case 'r':   return new VncChar('\r');
                    case 't':   return new VncChar('\t');
                    case 'b':   return new VncChar('\b');
                    case 'f':   return new VncChar('\f');
                    case '0':   return new VncChar('\0');
                    default:    return new VncChar(ch);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction symbol =
        new VncFunction(
                "symbol",
                VncFunction
                    .meta()
                    .arglists(
                        "(symbol name)",
                        "(symbol ns name)")
                    .doc("Returns a symbol from the given name")
                    .examples(
                        "(symbol \"a\")",
                        "(symbol \"foo\" \"a\")",
                        "(symbol *ns* \"a\")",
                        "(symbol 'a)",
                    	"((resolve (symbol \"core\" \"+\")) 1 2)",
                        "(name str/reverse)",
                        "(namespace str/reverse)")
                    .seeAlso("resolve", "name", "namespace")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1,2);

                if (args.size() == 1) {
                    if (Types.isVncSymbol(args.first())) {
                        return args.first();
                    }
                    else if (Types.isVncString(args.first())) {
                        return new VncSymbol(((VncString)args.first()).getValue());
                    }
                    else {
                        throw new VncException(String.format(
                                "Function 'symbol' does not allow %s name.",
                                Types.getType(args.first())));
                    }
                }
                else {
                    if (Types.isVncSymbol(args.first())) {
                        return new VncSymbol(
                                Coerce.toVncSymbol(args.first()).getName(),
                                Coerce.toVncString(args.second()).getValue(),
                                Nil);
                    }
                    else {
                        return new VncSymbol(
                                Coerce.toVncString(args.first()).getValue(),
                                Coerce.toVncString(args.second()).getValue(),
                                Nil);
                    }
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction symbol_Q =
        new VncFunction(
                "symbol?",
                VncFunction
                    .meta()
                    .arglists("(symbol? x)")
                    .doc("Returns true if x is a symbol")
                    .examples(
                        "(symbol? 'a)",
                        "(symbol? (symbol \"a\"))",
                        "(symbol? nil)",
                        "(symbol? :a)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(Types.isVncSymbol(args.first()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction qualified_symbol_Q =
        new VncFunction(
                "qualified-symbol?",
                VncFunction
                    .meta()
                    .arglists("(qualified-symbol? x)")
                    .doc("Returns true if x is a qualified symbol")
                    .examples(
                        "(qualified-symbol? 'foo/a)",
                        "(qualified-symbol? (symbol \"foo/a\"))",
                        "(qualified-symbol? 'a)",
                        "(qualified-symbol? nil)",
                        "(qualified-symbol? :a)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal s = args.first();
                if (Types.isVncSymbol(s)) {
                    return VncBoolean.of(((VncSymbol)s).hasNamespace());
                }
                else {
                    return VncBoolean.False;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction keyword =
        new VncFunction(
                "keyword",
                VncFunction
                    .meta()
                    .arglists(
                    	"(keyword name)",
                    	"(keyword ns name)")
                    .doc("Returns a keyword from the given name")
                    .examples(
                        "(keyword \"a\")",
                        "(keyword :a)",
                        "(keyword :foo/a)",
                        "(keyword \"foo\" \"a\")",
                        "(keyword (. :java.time.Month :JANUARY)) ;; java enum to keyword",
                        "(name :foo/a)",
                        "(namespace :foo/a)")
                    .seeAlso("name", "namespace")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                if (args.size() == 1) {
	                if (Types.isVncKeyword(args.first())) {
	                    return args.first();
	                }
	                else if (Types.isVncString(args.first())) {
	                    return new VncKeyword(((VncString)args.first()).getValue());
	                }
	                else if (Types.isVncJavaObject(args.first()) && ((VncJavaObject)args.first()).isEnum()) {
	                	final Enum<?> e = (Enum<?>)((VncJavaObject)args.first()).getDelegate();
	                    return new VncKeyword(e.name());
	                }
	                else {
	                    throw new VncException(String.format(
	                            "Function 'keyword' does not allow %s name",
	                            Types.getType(args.first())));
	                }
                }
                else {
                    if (Types.isVncSymbol(args.first())) {
                        return new VncKeyword(
                                Coerce.toVncSymbol(args.first()).getValue(),
                                Coerce.toVncString(args.second()).getValue(),
                                Nil);
                    }
                    else {
                        return new VncKeyword(
                                Coerce.toVncString(args.first()).getValue(),
                                Coerce.toVncString(args.second()).getValue(),
                                Nil);
                    }
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction keyword_Q =
        new VncFunction(
                "keyword?",
                VncFunction
                    .meta()
                    .arglists("(keyword? x)")
                    .doc("Returns true if x is a keyword")
                    .examples(
                        "(keyword? (keyword \"a\"))",
                        "(keyword? :a)",
                        "(keyword? nil)",
                        "(keyword? 'a)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(Types.isVncKeyword(args.first()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction fn_Q =
        new VncFunction(
                "fn?",
                VncFunction
                    .meta()
                    .arglists("(fn? x)")
                    .doc("Returns true if x is a function")
                    .examples("(do \n   (def sum (fn [x] (+ 1 x)))\n   (fn? sum))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                if (!Types.isVncFunction(args.first())) {
                    return False;
                }
                return VncBoolean.of(!((VncFunction)args.first()).isMacro());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction macro_Q =
        new VncFunction(
                "macro?",
                VncFunction
                    .meta()
                    .arglists("(macro? x)")
                    .doc("Returns true if x is a macro")
                    .examples("(macro? and)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(Types.isVncMacro(args.first()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction fn_name =
        new VncFunction(
                "fn-name",
                VncFunction
                    .meta()
                    .arglists("(fn-name f)")
                    .doc("Returns the qualified name of a function or macro")
                    .examples(
                        "(fn-name (fn sum [x y] (+ x y)))",
                        "(let [f str/digit?]  \n" +
                        "  (fn-name f))       ")
                    .seeAlso(
                        "name", "namespace",
                        "fn-about", "fn-body", "fn-pre-conditions")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal arg = args.first();

                if (arg == Nil) {
                    return Nil;
                }
                else if (Types.isVncFunction(arg) || Types.isVncMacro(arg)) {
                    final VncFunction fn = (VncFunction)arg;
                    fn.sandboxFunctionCallValidation();

                    return new VncString(fn.getQualifiedName());
                }
                else {
                    throw new VncException(String.format(
                            "Function 'fn-name' does not allow %s as parameter",
                            Types.getType(arg)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction fn_about =
        new VncFunction(
                "fn-about",
                VncFunction
                    .meta()
                    .arglists("(fn-about f)")
                    .doc("Returns the meta information about a function")
                    .examples(
                        "(fn-about and)",
                        "(fn-about println)",
                        "(fn-about +)")
                    .seeAlso("fn-name", "fn-body", "fn-pre-conditions")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal arg = args.first();

                if (arg == Nil) {
                    return Nil;
                }
                else if (Types.isVncFunction(arg) || Types.isVncMacro(arg)) {
                    final VncFunction fn = (VncFunction)arg;
                    fn.sandboxFunctionCallValidation();

                    return fn.about();
                }
                else {
                    throw new VncException(String.format(
                            "Function 'fn-about' does not allow %s as parameter",
                            Types.getType(arg)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction fn_body =
        new VncFunction(
                "fn-body",
                VncFunction
                    .meta()
                    .arglists(
                        "(fn-body fn)",
                        "(fn-body fn arity)")
                    .doc(
                        "Returns the body (a list of forms) of a function.\n\n" +
                        "Returns `nil` if fn is not a function or if fn is a " +
                        "native function.")
                    .examples(
                        "(do                         \n" +
                        "  (defn calc [& x]          \n" +
                        "    (->> x                  \n" +
                        "         (filter even?)     \n" +
                        "         (map #(* % 10))))  \n" +
                        "  (fn-body (var-get calc))) ")
                    .seeAlso(
                        "fn-name", "fn-about", "fn-pre-conditions")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                if (!Types.isVncFunction(args.first())) {
                    return Nil;
                }

                final VncFunction fn = (VncFunction)args.first();
                fn.sandboxFunctionCallValidation();

                if (fn instanceof VncMultiArityFunction) {
                    final VncMultiArityFunction mafn = (VncMultiArityFunction)fn;
                    if (args.size() == 1) {
                        throw new VncException(
                                "The passed function is a multi-arity function. Please " +
                                "pass the arity as second argument!");
                    }
                    else if (args.size() == 2) {
                        final int arity = Coerce.toVncLong(args.second()).getIntValue();
                        return mafn.getFunctionForArity(arity).getBody();
                    }
                    else {
                        return Nil;
                    }
                }
                else if (fn instanceof VncMultiFunction) {
                    return Nil;  // not supported
                }
                else {
                    return fn.getBody();
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction fn_pre_conditions =
        new VncFunction(
                "fn-pre-conditions",
                VncFunction
                    .meta()
                    .arglists(
                        "(fn-pre-conditions fn)",
                        "(fn-pre-conditions fn arity)")
                    .doc(
                        "Returns the pre-conditions (a vector of forms) of a " +
                        "function.\n\n" +
                        "Returns `nil` if fn is not a function.")
                    .examples(
                        "(do                                   \n" +
                        "  (defn sum [x y]                     \n" +
                        "     { :pre [(> x 0) (> y 0)] }       \n" +
                        "     (+ x y))                         \n" +
                        "  (fn-pre-conditions (var-get sum)))  ")
                    .seeAlso(
                            "fn-name", "fn-about", "fn-body")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                if (!Types.isVncFunction(args.first())) {
                    return Nil;
                }

                final VncFunction fn = (VncFunction)args.first();

                fn.sandboxFunctionCallValidation();

                if (fn instanceof VncMultiArityFunction) {
                    final VncMultiArityFunction mafn = (VncMultiArityFunction)fn;
                    if (args.size() == 1) {
                        return ((VncFunction)mafn.getFunctions().first()).getPreConditions();
                    }
                    else if (args.size() == 2) {
                        final int arity = Coerce.toVncLong(args.second()).getIntValue();
                        return mafn.getFunctionForArity(arity).getPreConditions();
                    }
                    else {
                        return Nil;
                    }
                }
                else if (fn instanceof VncMultiFunction) {
                    return Nil;
                }
                else {
                    return fn.getPreConditions();
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };



    ///////////////////////////////////////////////////////////////////////////
    // String functions
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction pr_str =
        new VncFunction(
                "pr-str",
                VncFunction
                    .meta()
                    .arglists("(pr-str & xs)")
                    .doc(
                        "With no args, returns the empty string. With one arg x, returns " +
                        "x.toString(). With more than one arg, returns the concatenation " +
                        "of the str values of the args with delimiter ' '.")
                    .examples(
                        "(pr-str)",
                        "(pr-str 1 2 3)")
                    .seeAlso("str")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                return args.isEmpty()
                        ? VncString.empty()
                        : new VncString(
                                args.stream()
                                    .map(v -> Printer.pr_str(v, true))
                                    .collect(Collectors.joining(" ")));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction str =
        new VncFunction(
                "str",
                VncFunction
                    .meta()
                    .arglists("(str & xs)")
                    .doc(
                        "With no args, returns the empty string. With one arg x, returns " +
                        "x.toString(). (str nil) returns the empty string. With more than " +
                        "one arg, returns the concatenation of the str values of the args.")
                    .examples(
                        "(str)",
                        "(str 1 2 3)",
                        "(str +)",
                        "(str [1 2 3])",
                        "(str \"total \" 100)",
                        "(str #\\h #\\i)")
                    .seeAlso("pr-str")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                final StringBuilder sb = new StringBuilder();
                for(VncVal v : args) {
                    if (v != Nil) {
                        sb.append(Printer.pr_str(v, false));
                    }
                }
                return new VncString(sb.toString());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction to_str =
        new VncFunction(
                "to-str",
                VncFunction
                    .meta()
                    .arglists(
                        "(to-str x)",
                        "(to-str print-machine-readably x)")
                    .doc(
                        "Returns the string representation of x.")
                    .examples(
                        "(to-str [1 2 3])",
                        "(to-str false [1 2 3])",
                        "(to-str true [1 2 3])")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                if (args.size() == 1) {
                    return new VncString(args.first().toString());
                }
                else {
                    final boolean print_machine_readably = Coerce.toVncBoolean(args.first()).getValue();
                    final VncVal v = args.second();

                    return new VncString(v.toString(print_machine_readably));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction read_string =
        new VncFunction(
                "read-string",
                VncFunction
                    .meta()
                    .arglists(
                        "(read-string s)",
                        "(read-string s origin)")
                    .doc(
                        "Reads Venice source from a string and transforms its " +
                        "content into a Venice data structure, following the " +
                        "rules of the Venice syntax.")
                    .examples(
                        "(do                                             \n" +
                        "  (eval (read-string \"(def x 100)\" \"test\")) \n" +
                        "  x)                                              ")
                    .seeAlso("eval")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                try {
                    ArityExceptions.assertArity(this, args, 1, 2);

                    String origin = null;

                    if (args.size() == 2) {
                        origin = Coerce.toVncString(args.second()).getValue();
                        origin = origin.substring(origin.lastIndexOf('/') + 1);
                    }

                    origin = StringUtil.isBlank(origin) ? "unknown" : origin;

                    return Reader.read_str(Coerce.toVncString(args.first()).getValue(), origin);
                }
                catch (ContinueException c) {
                    return Nil;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };



    ///////////////////////////////////////////////////////////////////////////
    // Just functions
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction just =
        new VncFunction(
                "just",
                VncFunction
                    .meta()
                    .arglists("(just x)")
                    .doc("Creates a wrapped x, that is dereferenceable")
                    .examples(
                        "(just 10)",
                        "(just \"10\")",
                        "(deref (just 10))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return new VncJust(args.first());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction just_Q =
        new VncFunction(
                "just?",
                VncFunction
                    .meta()
                    .arglists("(just? x)")
                    .doc("Returns true if x is of type just")
                    .examples("(just? (just 1))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(Types.isVncJust(args.first()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };



    ///////////////////////////////////////////////////////////////////////////
    // Equal / match functions
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction equal_strict_Q =
        new VncFunction(
                "=",
                VncFunction
                    .meta()
                    .arglists(
                        "(= x)", "(= x y)", "(= x y & more)")
                    .doc(
                        "Returns true if both operands have equivalent type and value")
                    .examples(
                        "(= \"abc\" \"abc\")",
                        "(= 0 0)",
                        "(= 0 1)",
                        "(= 0 0.0)",
                        "(= 0 0.0M)",
                        "(= \"0\" 0)",
                        "(= 4)",
                        "(= 4 4 4)")
                    .seeAlso(
                        "==", "not=")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                if (args.size() == 2) {
                    return VncBoolean.of(Types._equal_strict_Q(args.first(), args.second()));
                }
                else if (args.size() == 1) {
                    return True;
                }
                else {
                    final VncVal first = args.first();
                    for(VncVal v : args.rest()) {
                        if (!Types._equal_strict_Q(first, v)) return False;
                    }
                    return True;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction equal_Q =
        new VncFunction(
                "==",
                VncFunction
                    .meta()
                    .arglists(
                        "(== x)", "(== x y)", "(== x y & more)")
                    .doc(
                        "Returns true if both operands have equivalent value. \n\n" +
                        "Numbers of different types can be checked for value equality.")
                    .examples(
                        "(== \"abc\" \"abc\")",
                        "(== 0 0)",
                        "(== 0 1)",
                        "(== 0 0.0)",
                        "(== 0 0.0M)",
                        "(== \"0\" 0)",
                        "(== 4)",
                        "(== 4I 4 4.0 4.0M 4N)")
                    .seeAlso("=", "not=")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                if (args.size() == 2) {
                    return VncBoolean.of(Types._equal_Q(args.first(), args.second()));
                }
                else if (args.size() == 1) {
                    return True;
                }
                else {
                    final VncVal first = args.first();
                    for(VncVal v : args.rest()) {
                        if (!Types._equal_Q(first, v)) return False;
                    }
                    return True;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction not_equal_strict_Q =
        new VncFunction(
                "not=",
                VncFunction
                    .meta()
                    .arglists(
                        "(not= x)", "(not= x y)", "(not= x y & more)")
                    .doc(
                        "Same as (not (= x y))")
                    .examples(
                        "(not= \"abc\" \"abc\")",
                        "(not= 0 0)",
                        "(not= 0 1)",
                        "(not= 0 0.0)",
                        "(not= 0 0.0M)",
                        "(not= \"0\" 0)",
                        "(not= 4)",
                        "(not= 1 2 3)")
                    .seeAlso("=", "==")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                if (args.size() == 2) {
                    return VncBoolean.of(!Types._equal_strict_Q(args.first(), args.second()));
                }
                else if (args.size() == 1) {
                    return False;
                }
                else {
                    return ((VncBoolean)equal_strict_Q.apply(args)).not();
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction match_Q =
        new VncFunction(
                "match?",
                VncFunction
                    .meta()
                    .arglists("(match? s regex)")
                    .doc(
                        "Returns true if the string s matches the regular expression " +
                        "regex.\n\n" +
                        "The argument 'regex' may be a string representing a regular " +
                        "expression or a :java.util.regex.Pattern. \n\n" +
                        "See the functions in the 'regex' namespace if more than a " +
                        "simple regex match is required! E.g. `regex/matches?` performs " +
                        "much better on matching many strings against the same pattern: \n\n" +
                        "```                                                          \n" +
                        "(let [m (regex/matcher #\"[0-9]+\" \"\")]                    \n" +
                        "  (filter #(regex/matches? m %) [\"100\" \"1a1\" \"200\"]))  \n" +
                        "```")
                    .examples(
                        "(match? \"1234\" \"[0-9]+\")",
                        "(match? \"1234ss\" \"[0-9]+\")",
                        "(match? \"1234\" #\"[0-9]+\")")
                    .seeAlso(
                        "not-match?", "regex/matches?", "regex/matches-not?",
                        "regex/pattern", "regex/matcher", "regex/matches",
                        "regex/find", "regex/find-all")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                if (!Types.isVncString(args.first())) {
                    throw new VncException(String.format(
                            "Invalid first argument type %s while calling function 'match?'",
                            Types.getType(args.first())));
                }

                final String s = ((VncString)args.first()).getValue();

                if (Types.isVncString(args.second())) {
                    final String regex = ((VncString)args.second()).getValue();
                    return VncBoolean.of(s.matches(regex));
                }
                else if (Types.isVncJavaObject(args.second(), Pattern.class)) {
                    final Pattern p = Coerce.toVncJavaObject(args.second(), Pattern.class);
                    return VncBoolean.of(p.matcher(s).matches());
                }
                else if (Types.isVncJavaObject(args.second(), Matcher.class)) {
                    final Matcher m = Coerce.toVncJavaObject(args.second(), Matcher.class);
                    return VncBoolean.of(m.reset(s).matches());
                }
                else {
                    throw new VncException(String.format(
                            "Invalid second argument type %s while calling function 'match?'",
                            Types.getType(args.second())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction not_match_Q =
        new VncFunction(
                "not-match?",
                VncFunction
                    .meta()
                    .arglists("(not-match? s regex)")
                    .doc(
                        "Returns true if the string s does not match the regular " +
                        "expression regex.\n\n" +
                        "The argument 'regex' may be a string representing a regular " +
                        "expression or a :java.util.regex.Pattern.\n\n" +
                        "See the functions in the 'regex' namespace if more than a " +
                        "simple regex match is required! E.g. `regex/matches-not?` performs " +
                        "much better on matching many strings against the same pattern: \n\n" +
                        "```                                                              \n" +
                        "(let [m (regex/matcher #\"[0-9]+\" \"\")]                        \n" +
                        "  (filter #(regex/matches-not? m %) [\"100\" \"1a1\" \"200\"]))  \n" +
                        "```")
                    .examples(
                        "(not-match? \"S1000\" \"[0-9]+\")",
                        "(not-match? \"S1000\" #\"[0-9]+\")",
                        "(not-match? \"1000\" \"[0-9]+\")")
                    .seeAlso(
                        "match?", "regex/matches-not?", "regex/matches?",
                        "regex/pattern", "regex/matcher", "regex/matches",
                        "regex/find", "regex/find-all")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                if (!Types.isVncString(args.first())) {
                    throw new VncException(String.format(
                            "Invalid first argument type %s while calling function 'not-match?'",
                            Types.getType(args.first())));
                }

                final String s = ((VncString)args.first()).getValue();

                if (Types.isVncString(args.second())) {
                    final String regex = ((VncString)args.second()).getValue();
                    return VncBoolean.of(!s.matches(regex));
                }
                else if (Types.isVncJavaObject(args.second(), Pattern.class)) {
                    final Pattern p = Coerce.toVncJavaObject(args.second(), Pattern.class);
                    return VncBoolean.of(!p.matcher(s).matches());
                }
                else if (Types.isVncJavaObject(args.second(), Matcher.class)) {
                    final Matcher m = Coerce.toVncJavaObject(args.second(), Matcher.class);
                    return VncBoolean.of(!m.reset(s).matches());
                }
                else {
                    throw new VncException(String.format(
                            "Invalid second argument type %s while calling function 'match?'",
                            Types.getType(args.second())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };



    ///////////////////////////////////////////////////////////////////////////
    // Number functions
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction lt =
        new VncFunction(
                "<",
                VncFunction
                    .meta()
                    .arglists("(< x y)", "(< x y & more)")
                    .doc("Returns true if the numbers are in monotonically increasing order, otherwise false.")
                    .examples(
                        "(< 2 3)",
                        "(< 2 3.0)",
                        "(< 2 3.0M)",
                        "(< 2 3 4 5 6 7)",
                        "(let [x 10] \n" +
                        "  (< 0 x 100)) ")
                    .seeAlso("<=", ">", ">=")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 2);

                VncVal op1 = args.first();
                if (!Types.isVncNumber(op1)) {
                    throw new VncException("Function '<' supports numbers only.");
                }

                if (args.size() == 2) {
                    final VncVal op2 = args.second();
                    if (!Types.isVncNumber(op2)) {
                        throw new VncException("Function '<' supports numbers only.");
                    }
                    return VncBoolean.of(op1.compareTo(op2) < 0);
                }
                else {
                    VncList vals = args.rest();
                    while(!vals.isEmpty()) {
                        VncVal op2 = vals.first();
                        if (!Types.isVncNumber(op2)) {
                            throw new VncException("Function '<' supports numbers only.");
                        }

                        if (op1.compareTo(op2) < 0) {
                            op1 = op2;
                            vals = vals.rest();
                        }
                        else {
                            return VncBoolean.False;
                        }
                    }

                    return VncBoolean.True;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction lte =
        new VncFunction(
                "<=",
                VncFunction
                    .meta()
                    .arglists("(<= x y)", "(<= x y & more)")
                    .doc("Returns true if the numbers are in monotonically non-decreasing order, otherwise false.")
                    .examples(
                        "(<= 2 3)",
                        "(<= 3 3)",
                        "(<= 2 3.0)",
                        "(<= 2 3.0M)",
                        "(<= 2 3 4 5 6 7)",
                        "(let [x 10] \n" +
                        "  (<= 0 x 100)) ")
                    .seeAlso("<", ">", ">=")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 2);

                VncVal op1 = args.first();
                if (!Types.isVncNumber(op1)) {
                    throw new VncException("Function '<=' supports numbers only.");
                }

                if (args.size() == 2) {
                    final VncVal op2 = args.second();
                    if (!Types.isVncNumber(op2)) {
                        throw new VncException("Function '<=' supports numbers only.");
                    }
                    return VncBoolean.of(op1.compareTo(op2) <= 0);
                }
                else {
                    VncList vals = args.rest();
                    while(!vals.isEmpty()) {
                        VncVal op2 = vals.first();
                        if (!Types.isVncNumber(op2)) {
                            throw new VncException("Function '<=' supports numbers only.");
                        }

                        if (op1.compareTo(op2) <= 0) {
                            op1 = op2;
                            vals = vals.rest();
                        }
                        else {
                            return VncBoolean.False;
                        }
                    }

                    return VncBoolean.True;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction gt =
        new VncFunction(
                ">",
                VncFunction
                    .meta()
                    .arglists("(> x y)", "(> x y & more)")
                    .doc("Returns true if the numbers are in monotonically decreasing order, otherwise false.")
                    .examples(
                        "(> 3 2)",
                        "(> 3 3)",
                        "(> 3.0 2)",
                        "(> 3.0M 2)",
                        "(> 7 6 5 4 3 2)")
                    .seeAlso("<", "<=", ">=")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 2);

                VncVal op1 = args.first();
                if (!Types.isVncNumber(op1)) {
                    throw new VncException("Function '>' supports numbers only.");
                }

                if (args.size() == 2) {
                    final VncVal op2 = args.second();
                    if (!Types.isVncNumber(op2)) {
                        throw new VncException("Function '>' supports numbers only.");
                    }
                    return VncBoolean.of(op1.compareTo(op2) > 0);
                }
                else {
                    VncList vals = args.rest();
                    while(!vals.isEmpty()) {
                        VncVal op2 = vals.first();
                        if (!Types.isVncNumber(op2)) {
                            throw new VncException("Function '>' supports numbers only.");
                        }

                        if (op1.compareTo(op2) > 0) {
                            op1 = op2;
                            vals = vals.rest();
                        }
                        else {
                            return VncBoolean.False;
                        }
                    }

                    return VncBoolean.True;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction gte =
        new VncFunction(
                ">=",
                VncFunction
                    .meta()
                    .arglists("(>= x y)", "(>= x y & more)")
                    .doc("Returns true if the numbers are in monotonically non-increasing order, otherwise false.")
                    .examples(
                        "(>= 3 2)",
                        "(>= 3 3)",
                        "(>= 3.0 2)",
                        "(>= 3.0M 2)",
                        "(>= 7 6 5 4 3 2)")
                    .seeAlso("<", "<=", ">")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 2);

                VncVal op1 = args.first();
                if (!Types.isVncNumber(op1)) {
                    throw new VncException("Function '>=' supports numbers only.");
                }

                if (args.size() == 2) {
                    final VncVal op2 = args.second();
                    if (!Types.isVncNumber(op2)) {
                        throw new VncException("Function '>=' supports numbers only.");
                    }
                    return VncBoolean.of(op1.compareTo(op2) >= 0);
                }
                else {
                    VncList vals = args.rest();
                    while(!vals.isEmpty()) {
                        VncVal op2 = vals.first();
                        if (!Types.isVncNumber(op2)) {
                            throw new VncException("Function '>=' supports numbers only.");
                        }

                        if (op1.compareTo(op2) >= 0) {
                            op1 = op2;
                            vals = vals.rest();
                        }
                        else {
                            return VncBoolean.False;
                        }
                    }

                    return VncBoolean.True;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };




    ///////////////////////////////////////////////////////////////////////////
    // Casts
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction boolean_cast =
        new VncFunction(
                "boolean",
                VncFunction
                    .meta()
                    .arglists("(boolean x)")
                    .doc("Converts to boolean. Everything except 'false' and 'nil' is true in boolean context.")
                    .examples(
                        "(boolean false)",
                        "(boolean true)",
                        "(boolean nil)",
                        "(boolean 100)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal arg = args.first();
                if (arg == Nil) {
                    return False;
                }
                else if (VncBoolean.isFalse(arg)) {
                    return False;
                }
                else {
                    return True;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction long_cast =
        new VncFunction(
                "long",
                VncFunction
                    .meta()
                    .arglists("(long x)")
                    .doc("Converts to long")
                    .examples(
                        "(long 1)",
                        "(long nil)",
                        "(long false)",
                        "(long true)",
                        "(long 1.2)",
                        "(long 1.2M)",
                        "(long \"1\")",
                        "(long (char \"A\"))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal arg = args.first();

                if (arg == Nil) {
                    return new VncLong(0L);
                }
                else if (VncBoolean.isFalse(arg)) {
                    return new VncLong(0L);
                }
                else if (VncBoolean.isTrue(arg)) {
                    return new VncLong(1L);
                }
                else if (Types.isVncLong(arg)) {
                    return arg;
                }
                else if (Types.isVncInteger(arg)) {
                    return new VncLong(((VncInteger)arg).toJavaLong());
                }
                else if (Types.isVncDouble(arg)) {
                    return new VncLong(((VncDouble)arg).toJavaLong());
                }
                else if (Types.isVncBigDecimal(arg)) {
                    return new VncLong(((VncBigDecimal)arg).toJavaLong());
                }
                else if (Types.isVncBigInteger(arg)) {
                    return new VncLong(((VncBigInteger)arg).toJavaLong());
                }
                else if (Types.isVncChar(arg)) {
                    return new VncLong(((VncChar)arg).getValue().charValue());
                }
                else if (Types.isVncString(arg)) {
                    final String s = ((VncString)arg).getValue();
                    try {
                        return new VncLong(Long.parseLong(s));
                    }
                    catch(Exception ex) {
                        throw new VncException(String.format(
                                "Function 'long': the string %s can not be converted to a long",
                                s));
                    }
                }
                else if (Types.isVncJavaObject(arg, Long.class)) {
                    return new VncLong((Long)((VncJavaObject)arg).getDelegate());
                }
                else {
                    throw new VncException(String.format(
                                            "Function 'long' does not allow %s as operand 1",
                                            Types.getType(arg)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction int_cast =
    new VncFunction(
            "int",
            VncFunction
                .meta()
                .arglists("(int x)")
                .doc("Converts to int")
                .examples(
                    "(int 1)",
                    "(int nil)",
                    "(int false)",
                    "(int true)",
                    "(int 1.2)",
                    "(int 1.2M)",
                    "(int \"1\")",
                    "(int (char \"A\"))")
                .build()
    ) {
        @Override
        public VncVal apply(final VncList args) {
            ArityExceptions.assertArity(this, args, 1);

            final VncVal arg = args.first();

            if (arg == Nil) {
                return new VncInteger(0);
            }
            else if (VncBoolean.isFalse(arg)) {
                return new VncInteger(0);
            }
            else if (VncBoolean.isTrue(arg)) {
                return new VncInteger(1);
            }
            else if (Types.isVncInteger(arg)) {
                return arg;
            }
            else if (Types.isVncLong(arg)) {
                return new VncInteger(((VncLong)arg).toJavaInteger());
            }
            else if (Types.isVncDouble(arg)) {
                return new VncInteger(((VncDouble)arg).toJavaInteger());
            }
            else if (Types.isVncBigDecimal(arg)) {
                return new VncInteger(((VncBigDecimal)arg).toJavaInteger());
            }
            else if (Types.isVncBigInteger(arg)) {
                return new VncInteger(((VncBigInteger)arg).toJavaInteger());
            }
            else if (Types.isVncChar(arg)) {
                return new VncInteger(((VncChar)arg).getValue().charValue());
            }
            else if (Types.isVncString(arg)) {
                final String s = ((VncString)arg).getValue();
                try {
                    return new VncInteger(Integer.parseInt(s));
                }
                catch(Exception ex) {
                    throw new VncException(String.format(
                            "Function 'int': the string %s can not be converted to an int",
                            s));
                }
            }
            else if (Types.isVncJavaObject(arg, Integer.class)) {
                return new VncInteger((Integer)((VncJavaObject)arg).getDelegate());
            }
            else {
                throw new VncException(String.format(
                                        "Function 'int' does not allow %s as operand 1",
                                        Types.getType(arg)));
            }
        }

        private static final long serialVersionUID = -1848883965231344442L;
    };

    public static VncFunction double_cast =
        new VncFunction(
                "double",
                VncFunction
                    .meta()
                    .arglists("(double x)")
                    .doc("Converts to double")
                    .examples(
                        "(double 1)",
                        "(double nil)",
                        "(double false)",
                        "(double true)",
                        "(double 1.2)",
                        "(double 1.2M)",
                        "(double \"1.2\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal arg = args.first();

                if (arg == Nil) {
                    return new VncDouble(0.0D);
                }
                else if (VncBoolean.isFalse(arg)) {
                    return new VncDouble(0.0D);
                }
                else if (VncBoolean.isTrue(arg)) {
                    return new VncDouble(1.0D);
                }
                else if (Types.isVncInteger(arg)) {
                    return new VncDouble(((VncInteger)arg).toJavaDouble());
                }
                else if (Types.isVncLong(arg)) {
                    return new VncDouble(((VncLong)arg).toJavaDouble());
                }
                else if (Types.isVncDouble(arg)) {
                    return arg;
                }
                else if (Types.isVncBigDecimal(arg)) {
                    return new VncDouble(((VncBigDecimal)arg).toJavaDouble());
                }
                else if (Types.isVncBigInteger(arg)) {
                    return new VncDouble(((VncBigInteger)arg).toJavaDouble());
                }
                else if (Types.isVncString(arg)) {
                    final String s = ((VncString)arg).getValue();
                    try {
                        return new VncDouble(Double.parseDouble(s));
                    }
                    catch(Exception ex) {
                        throw new VncException(String.format(
                                "Function 'double': the string %s can not be converted to a double",
                                s));
                    }
                }
                else if (Types.isVncJavaObject(arg, Double.class)) {
                    return new VncDouble((Double)((VncJavaObject)arg).getDelegate());
                }
                else if (Types.isVncJavaObject(arg, Float.class)) {
                    return new VncDouble((Float)((VncJavaObject)arg).getDelegate());
                }
                else {
                    throw new VncException(String.format(
                                "Function 'double' does not allow %s as operand 1",
                                Types.getType(arg)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction decimal_cast =
        new VncFunction(
                "decimal",
                VncFunction
                    .meta()
                    .arglists("(decimal x) (decimal x scale rounding-mode)")
                    .doc(
                        "Converts to decimal. rounding-mode is one of (:CEILING, :DOWN, " +
                        ":FLOOR, :HALF_DOWN, :HALF_EVEN, :HALF_UP, :UNNECESSARY, :UP)")
                    .examples(
                        "(decimal 2)",
                        "(decimal 2 3 :HALF_UP)",
                        "(decimal 2.5787 3 :HALF_UP)",
                        "(decimal 2.5787M 3 :HALF_UP)",
                        "(decimal \"2.5787\" 3 :HALF_UP)",
                        "(decimal nil)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 3);

                if (args.isEmpty()) {
                    return new VncBigDecimal(BigDecimal.ZERO);
                }
                else {
                    final VncVal arg = args.first();
                    final boolean scaling = args.size() == 3;
                    final int scale = scaling ? Coerce.toVncLong(args.second()).getIntValue() : 0;
                    final RoundingMode roundingMode = args.size() < 3 ? null : VncBigDecimal.toRoundingMode((VncString)args.nth(2));

                    if (arg == Nil) {
                        final BigDecimal dec = BigDecimal.ZERO;
                        return new VncBigDecimal(!scaling ? dec : dec.setScale(scale, roundingMode));
                    }
                    else if (VncBoolean.isFalse(arg)) {
                        final BigDecimal dec = BigDecimal.ZERO;
                        return new VncBigDecimal(!scaling ? dec : dec.setScale(scale, roundingMode));
                    }
                    else if (VncBoolean.isTrue(arg)) {
                        final BigDecimal dec = BigDecimal.ONE;
                        return new VncBigDecimal(!scaling ? dec : dec.setScale(scale, roundingMode));
                    }
                    else if (Types.isVncString(arg)) {
                        final BigDecimal dec = new BigDecimal(((VncString)arg).getValue());
                        return new VncBigDecimal(!scaling ? dec : dec.setScale(scale, roundingMode));
                    }
                    else if (Types.isVncNumber(arg)) {
                        final BigDecimal dec = VncBigDecimal.of(arg).getValue();
                        return new VncBigDecimal(!scaling ? dec : dec.setScale(scale, roundingMode));
                    }
                    else if (Types.isVncJavaObject(arg, BigDecimal.class)) {
                        return new VncBigDecimal((BigDecimal)((VncJavaObject)arg).getDelegate());
                    }
                    else {
                        throw new VncException(String.format(
                                "Function 'decimal' does not allow %s as operand 1",
                                Types.getType(arg)));
                    }
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction bigint_cast =
        new VncFunction(
                "bigint",
                VncFunction
                    .meta()
                    .arglists("(bigint x)")
                    .doc(
                        "Converts to big integer.")
                    .examples(
                        "(bigint 2000)",
                        "(bigint 34897.65)",
                        "(bigint \"5676000000000\")",
                        "(bigint nil)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                if (args.isEmpty()) {
                    return new VncBigDecimal(BigDecimal.ZERO);
                }
                else {
                    final VncVal arg = args.first();

                    if (arg == Nil) {
                        return new VncBigInteger(BigInteger.ZERO);
                    }
                    else if (VncBoolean.isFalse(arg)) {
                        return new VncBigInteger(BigInteger.ZERO);
                    }
                    else if (VncBoolean.isTrue(arg)) {
                        return new VncBigInteger(BigInteger.ONE);
                    }
                    else if (Types.isVncString(arg)) {
                        return new VncBigInteger(new BigInteger(((VncString)arg).getValue()));
                    }
                    else if (Types.isVncNumber(arg)) {
                        return VncBigInteger.of(arg);
                    }
                    else if (Types.isVncJavaObject(arg, BigInteger.class)) {
                        return new VncBigInteger((BigInteger)((VncJavaObject)arg).getDelegate());
                    }
                    else {
                        throw new VncException(String.format(
                                "Function 'decimal' does not allow %s as operand 1",
                                Types.getType(arg)));
                    }
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    ///////////////////////////////////////////////////////////////////////////
    // List functions
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction new_list =
        new VncFunction(
                "list",
                VncFunction
                    .meta()
                    .arglists("(list & items)")
                    .doc("Creates a new list containing the items.")
                    .examples("(list)", "(list 1 2 3)", "(list 1 2 3 [:a :b])")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                return args;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction new_list_ASTERISK =
        new VncFunction(
                "list*",
                VncFunction
                    .meta()
                    .arglists(
                        "(list* args)",
                        "(list* a args)",
                        "(list* a b args)",
                        "(list* a b c args)",
                        "(list* a b c d & more)")
                    .doc(
                        "Creates a new list containing the items prepended to the rest, the " +
                        "last of which will be treated as a collection.")
                    .examples(
                        "(list* 1 '(2 3))",
                        "(list* 1 2 3 [4])",
                        "(list* 1 2 3 '(4 5))",
                        "(list* '(1 2) 3 [4])",
                        "(list* nil)",
                        "(list* nil [2 3])",
                        "(list* 1 2 nil)")
                    .seeAlso("cons", "conj", "concat", "vector*")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                if (args.size() == 1 && args.first() == Nil) {
                    return Nil;
                }
                else if (args.last() == Nil) {
                    return args.butlast();
                }
                else if (!Types.isVncSequence(args.last())) {
                    throw new VncException(String.format(
                            "Function 'list*' does not allow %s as last argument",
                            Types.getType(args.last())));
                }
                else {
                    return VncList.empty()
                                .addAllAtEnd(args.butlast())
                                .addAllAtEnd((VncSequence)args.last());
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction list_Q =
        new VncFunction(
                "list?",
                VncFunction
                    .meta()
                    .arglists("(list? obj)")
                    .doc("Returns true if obj is a list")
                    .examples("(list? (list 1 2))", "(list? '(1 2))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(Types.isVncList(args.first()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    ///////////////////////////////////////////////////////////////////////////
    // Mutable List functions
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction new_mutable_list =
        new VncFunction(
                "mutable-list",
                VncFunction
                    .meta()
                    .arglists("(mutable-list & items)")
                    .doc(
                        "Creates a new mutable list containing the items.\n\n" +
                        "The list is backed by `java.util.ArrayList` and is not thread-safe.")
                    .examples(
                        "(mutable-list)",
                        "(mutable-list 1 2 3)",
                        "(mutable-list 1 2 3 [:a :b])")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                return VncMutableList.ofAll(args, Nil);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction mutable_list_Q =
            new VncFunction(
                    "mutable-list?",
                    VncFunction
                        .meta()
                        .arglists("(mutable-list? obj)")
                        .doc("Returns true if obj is a mutable list")
                        .examples("(mutable-list? (mutable-list 1 2))")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertArity(this, args, 1);

                    return VncBoolean.of(Types.isVncMutableList(args.first()));
                }

                private static final long serialVersionUID = -1848883965231344442L;
            };


    ///////////////////////////////////////////////////////////////////////////
    // Vector functions
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction new_vector =
        new VncFunction(
                "vector",
                VncFunction
                    .meta()
                    .arglists("(vector & items)")
                    .doc(
                        "Creates a new vector containing the items.")
                    .examples(
                        "(vector)",
                        "(vector 1 2 3)",
                        "(vector 1 2 3 [:a :b])",
                        "(vector \"abc\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                return args.toVncVector();
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction new_vector_ASTERISK =
        new VncFunction(
                "vector*",
                VncFunction
                    .meta()
                    .arglists(
                        "(vector* args)",
                        "(vector* a args)",
                        "(vector* a b args)",
                        "(vector* a b c args)",
                        "(vector* a b c d & more)")
                    .doc(
                        "Creates a new vector containing the items prepended to the rest, the " +
                        "last of which will be treated as a collection.")
                    .examples(
                        "(vector* 1 [2 3])",
                        "(vector* 1 2 3 [4])",
                        "(vector* 1 2 3 '(4 5))",
                        "(vector* '[1 2] 3 [4])",
                        "(vector* nil)",
                        "(vector* nil [2 3])",
                        "(vector* 1 2 nil)")
                    .seeAlso("cons", "conj", "concat", "list*")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                if (args.size() == 1 && args.first() == Nil) {
                    return Nil;
                }
                else if (args.last() == Nil) {
                    return args.butlast();
                }
                else if (!Types.isVncSequence(args.last())) {
                    throw new VncException(String.format(
                            "Function 'vector*' does not allow %s as last argument",
                            Types.getType(args.last())));
                }
                else {
                    return VncVector.empty()
                                .addAllAtEnd(args.butlast())
                                .addAllAtEnd((VncSequence)args.last());
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static boolean vector_Q(VncVal mv) {
        return Types.isVncVector(mv);
    }

    public static VncFunction vector_Q =
        new VncFunction(
                "vector?",
                VncFunction
                    .meta()
                    .arglists("(vector? obj)")
                    .doc("Returns true if obj is a vector")
                    .examples(
                        "(vector? (vector 1 2))",
                        "(vector? [1 2])")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(vector_Q(args.first()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction subvec =
        new VncFunction(
                "subvec",
                VncFunction
                    .meta()
                    .arglists("(subvec v start) (subvec v start end)")
                    .doc(
                        "Returns a vector of the items in vector from start (inclusive) "+
                        "to end (exclusive). If end is not supplied, defaults to " +
                        "(count vector)")
                    .examples(
                        "(subvec [1 2 3 4 5 6] 2)",
                        "(subvec [1 2 3 4 5 6] 2 3)")
                    .seeAlso("sublist")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2, 3);

                final VncSequence seq = Coerce.toVncSequence(args.first());

                if (seq instanceof VncLazySeq) {
                    throw new VncException(
                            "Function 'subvec' requires a vector but got a lazy-seq!");
                }
                else {
                    final VncVector vec = Coerce.toVncVector(seq);
                    final int from = Coerce.toVncLong(args.second()).getIntValue();
                    final Integer to = args.size() > 2 ? Coerce.toVncLong(args.nth(2)).getIntValue() : null;

                    if (from >= vec.size()) {
                        return VncVector.empty();
                    }
                    else {
                        return to == null ? vec.slice(from) : vec.slice(from, to);
                    }
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction sublist =
        new VncFunction(
                "sublist",
                VncFunction
                    .meta()
                    .arglists("(sublist l start) (sublist l start end)")
                    .doc(
                        "Returns a list of the items in list from start (inclusive) "+
                        "to end (exclusive). If end is not supplied, defaults to " +
                        "(count list).\n\n" +
                        "`sublist` accepts a lazy-seq if both start and end is given.")
                    .examples(
                        "(sublist '(1 2 3 4 5 6) 2)",
                        "(sublist '(1 2 3 4 5 6) 2 3)",
                        "(doall (sublist (lazy-seq 1 inc) 3 7))")
                    .seeAlso("subvec")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2, 3);

                final VncSequence seq = Coerce.toVncSequence(args.first());

                final int from = Coerce.toVncLong(args.second()).getIntValue();
                final Integer to = args.size() > 2 ? Coerce.toVncLong(args.nth(2)).getIntValue() : null;

                if (seq instanceof VncLazySeq) {
                    final VncLazySeq lazySeq = (VncLazySeq)seq;
                    if (to == null) {
                        throw new VncException(
                                "Function 'sublist' requires start and end if a lazy-seq is passed!");
                    }
                    else {
                        return lazySeq.slice(from, to);
                    }
                }
                else {
                    final VncList list = Coerce.toVncList(seq);

                    if (from >= list.size()) {
                        return VncList.empty();
                    }
                    else {
                        return to == null ? list.slice(from) : list.slice(from, to);
                    }
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction replace =
        new VncFunction(
                "replace",
                VncFunction
                    .meta()
                    .arglists("(replace smap coll)")
                    .doc(
                        "Given a map of replacement pairs and a collection, returns a " +
                        "collection with any elements that are a key in smap replaced with the " +
                        "corresponding value in smap.")
                    .examples(
                            "(replace {2 :two, 4 :four} [4 2 3 4 5 6 2])",
                            "(replace {2 :two, 4 :four} #{1 2 3 4 5})",
                            "(replace {[:a 10] [:c 30]} {:a 10 :b 20})")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final VncMap map = Coerce.toVncMap(args.first());
                final VncVal coll = args.second();

                if (Types.isVncSequence(coll)) {
                    final VncSequence seq = (VncSequence)coll;

                    final List<VncVal> vals = new ArrayList<>();

                    for(VncVal v : seq) {
                        final VncVal r = map.get(v);
                        vals.add(r == Nil ? v : r);
                    }

                    return seq.withValues(vals, coll.getMeta());
                }
                else if (Types.isVncSet(coll)) {
                    final VncSet set = (VncSet)coll;

                    final List<VncVal> vals = new ArrayList<>();

                    for(VncVal v : set) {
                        final VncVal r = map.get(v);
                        vals.add(r == Nil ? v : r);
                    }

                    return set.withValues(vals, coll.getMeta());
                }
                else if (Types.isVncMap(coll)) {
                    VncMap mapc = (VncMap)coll;

                    for(VncMapEntry e : map.entries()) {
                        final VncVal k = Coerce.toVncVector(e.getKey()).first();
                        final VncVal v = Coerce.toVncVector(e.getKey()).second();

                        if (v.equals(mapc.get(k))) {
                            mapc = mapc.dissoc(k);
                            mapc = mapc.assoc(
                                        Coerce.toVncVector(e.getValue()).first(),
                                        Coerce.toVncVector(e.getValue()).second());
                        }
                    }

                    return mapc;
                }
                else {
                    throw new VncException(
                            "Function 'repeat' requires a list, vector, set, or map as coll argument");
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction shuffle =
        new VncFunction(
                "shuffle",
                VncFunction
                    .meta()
                    .arglists("(shuffle coll)")
                    .doc(
                        "Returns a collection of the items in coll in random order.")
                    .examples(
                        "(shuffle '(1 2 3 4 5 6))",
                        "(shuffle [1 2 3 4 5 6])",
                        "(shuffle \"abcdef\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal coll = args.first();

                if (coll == Nil) {
                    return Nil;
                }
                else if (Types.isVncSequence(coll)) {
                    return ((VncSequence)coll).shuffle();
                }
                else if (Types.isVncString(coll)) {
                    return ((VncString)coll).toVncList().shuffle();
                }
                else {
                    throw new VncException(
                            "Function 'shuffle' requires a list, vector, or string as coll argument.");
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };



    ///////////////////////////////////////////////////////////////////////////
    // Mutable Vector functions
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction new_mutable_vector =
        new VncFunction(
                "mutable-vector",
                VncFunction
                    .meta()
                    .arglists("(mutable-vector & items)")
                    .doc("Creates a new mutable threadsafe vector containing the items.")
                    .examples(
                        "(mutable-vector)",
                        "(mutable-vector 1 2 3)",
                        "(mutable-vector 1 2 3 [:a :b])")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                return VncMutableVector.ofAll(args);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction mutable_vector_Q =
            new VncFunction(
                    "mutable-vector?",
                    VncFunction
                        .meta()
                        .arglists("(mutable-vector? obj)")
                        .doc("Returns true if obj is a mutable vector")
                        .examples("(mutable-vector? (mutable-vector 1 2))")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertArity(this, args, 1);

                    return VncBoolean.of(Types.isVncMutableVector(args.first()));
                }

                private static final long serialVersionUID = -1848883965231344442L;
            };

    public static VncFunction mutable_Q =
        new VncFunction(
                "mutable?",
                VncFunction
                    .meta()
                    .arglists("(mutable? val)")
                    .doc("Returns true if val is mutable")
                    .examples("(mutable? (queue))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(args.first() instanceof VncMutable);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    ///////////////////////////////////////////////////////////////////////////
    // LazySeq functions
    //
    // Functions that return lazy sequences when their input is a lazy
    // sequence:
    //    - cons
    //    - map
    //    - filter
    //    - remove
    //    - take
    //    - take-while
    //    - drop
    //    - drop-while
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction new_lazy_seq =
        new VncFunction(
                "lazy-seq",
                VncFunction
                    .meta()
                    .arglists(
                        "(lazy-seq)",
                        "(lazy-seq f)",
                        "(lazy-seq seed f)",
                        "(lazy-seq head tail-lazy-seq)")
                    .doc(
                        "Creates a new lazy sequence.\n\n" +
                        "`(lazy-seq)`¶" +
                        "empty lazy sequence\n\n" +
                        "`(lazy-seq f)`¶" +
                        "(theoretically) infinitely lazy sequence using a repeatedly " +
                        "invoked supplier function for each next value. The supplier " +
                        "function f is a no arg function. The sequence ends if the " +
                        "supplier function returns nil. \n\n" +
                        "`(lazy-seq seed f)`¶\n" +
                        "(theoretically) infinitely lazy sequence with a seed value " +
                        "and a supplier function to calculate the next value based on " +
                        "the previous. f is a single arg function. The sequence ends if " +
                        "the supplier function returns nil.\n\n" +
                        "`(lazy-seq head tail-lazy-seq)`¶" +
                        "Constructs a lazy sequence of a head element and a lazy " +
                        "sequence tail supplier.")
                    .examples(
                        "; empty lazy sequence  \n" +
                        "(->> (lazy-seq)        \n" +
                        "     (doall))",
                        "; lazy sequence with a supplier function producing random longs \n" +
                        "(->> (lazy-seq rand-long)                                       \n" +
                        "     (take 4)                                                   \n" +
                        "     (doall))",
                        "; lazy sequence with a constant value                           \n" +
                        "(->> (lazy-seq (constantly 5))                                  \n" +
                        "     (take 4)                                                   \n" +
                        "     (doall))",
                        "; lazy sequence with a seed value and a supplier function \n" +
                        "; producing of all positive numbers (1, 2, 3, 4, ...)     \n" +
                        "(->> (lazy-seq 1 inc)                                     \n" +
                        "     (take 10)                                            \n" +
                        "     (doall))",
                        "; producing of all positive even numbers (2, 4, 6, ...)   \n" +
                        "(->> (lazy-seq 2 #(+ % 2))                                \n" +
                        "     (take 10)                                            \n" +
                        "     (doall))",
                        "; lazy sequence as value producing function               \n" +
                        "(interleave [:a :b :c] (lazy-seq 1 inc))",
                        "; lazy sequence with a mapping                         \n" +
                        "(->> (lazy-seq 1 (fn [x] (do (println \"realized\" x)  \n" +
                        "                             (inc x))))                \n" +
                        "     (take 10)                                         \n" +
                        "     (map #(* 10 %))                                   \n" +
                        "     (take 2)                                          \n" +
                        "     (doall))",
                        "; finite lazy sequence from a vector  \n" +
                        "(->> (lazy-seq [1 2 3 4])             \n" +
                        "     (doall))",
                        "; finite lazy sequence with a supplier function that   \n" +
                        "; returns nil to terminate the sequence                \n" +
                        "(do                                                    \n" +
                        "   (def counter (atom 5))                              \n" +
                        "   (defn generate []                                   \n" +
                        "      (swap! counter dec)                              \n" +
                        "      (if (pos? @counter) @counter nil))               \n" +
                        "   (doall (lazy-seq generate)))",
                        "; lazy sequence from a head element and a tail lazy    \n" +
                        "; sequence                                             \n" +
                        "(->> (cons -1 (lazy-seq 0 #(+ % 1)))                   \n" +
                        "     (take 5)                                          \n" +
                        "     (doall))",
                        "; lazy sequence from a head element and a tail lazy    \n" +
                        "; sequence                                             \n" +
                        "(->> (lazy-seq -1 (lazy-seq 0 #(+ % 1)))               \n" +
                        "     (take 5)                                          \n" +
                        "     (doall))",
                        "; lazy sequence show its power to generate the Fibonacci sequence   \n" +
                        "(do                                                                 \n" +
                        "  (def fib (map first (lazy-seq [0N 1N] (fn [[a b]] [b (+ a b)])))) \n" +
                        "  (doall (take 10 fib)))")
                    .seeAlso("doall", "lazy-seq?", "cons", "cycle", "repeat")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0, 1, 2);

                if (args.size() == 0) {
                    // empty lazy sequence
                    return new VncLazySeq(Nil);
                }
                else if (args.size() == 1) {
                    if (Types.isVncFunction(args.first())) {
                        // finite/infinite lazy sequence with a supplier function
                        final VncFunction fn = (VncFunction)args.first();
                        fn.sandboxFunctionCallValidation();

                        return VncLazySeq.iterate(fn, Nil);
                    }
                    else if (Types.isVncList(args.first())) {
                        // finite lazy sequence from list
                        return VncLazySeq.ofAll(Coerce.toVncList(args.first()), Nil);
                    }
                    else if (Types.isVncVector(args.first())) {
                        // finite lazy sequence from vector
                        return VncLazySeq.ofAll(Coerce.toVncVector(args.first()), Nil);
                    }
                    else {
                        throw new VncException(
                                "Function 'lazy-seq' requires for the first arg either "
                                    + "nil, a function, or a sequence.");
                    }
                }
                else if (args.second() == Nil) {
                    // finite/infinite lazy sequence with a supplier function
                    final VncFunction fn = Coerce.toVncFunction(args.first());
                    fn.sandboxFunctionCallValidation();

                    return VncLazySeq.iterate(fn, Nil);
                }
                else if (Types.isVncFunction(args.second())) {
                    // infinite lazy sequence with a seed value and a function to compute the next value
                    final VncFunction fn = (VncFunction)args.second();
                    fn.sandboxFunctionCallValidation();

                    return VncLazySeq.iterate(args.first(), fn, Nil);
                }
                else if (Types.isVncLazySeq(args.second())) {
                    // infinite lazy sequence from a value and lazy sequence building the tail
                    return VncLazySeq.cons(args.first(), (VncLazySeq)args.second(), Nil);
                }
                else {
                    throw new VncException(
                            "Function 'lazy-seq' requires for the second arg either "
                                + "nil, a function, or a lazy sequence.");
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction lazyseq_Q =
        new VncFunction(
                "lazy-seq?",
                VncFunction
                    .meta()
                    .arglists("(lazy-seq? obj)")
                    .doc("Returns true if obj is a lazyseq")
                    .examples("(lazy-seq? (lazy-seq rand-long))")
                    .seeAlso("lazy-seq")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(Types.isVncLazySeq(args.first()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };



    ///////////////////////////////////////////////////////////////////////////
    // Set functions
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction new_set =
        new VncFunction(
                "set",
                VncFunction
                    .meta()
                    .arglists("(set & items)")
                    .doc("Creates a new set containing the items.")
                    .examples("(set)", "(set nil)", "(set 1)", "(set 1 2 3)", "(set [1 2] 3)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                return VncHashSet.ofAll(args);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction new_sorted_set =
        new VncFunction(
                "sorted-set",
                VncFunction
                    .meta()
                    .arglists("(sorted-set & items)")
                    .doc("Creates a new sorted-set containing the items.")
                    .examples("(sorted-set)", "(sorted-set nil)", "(sorted-set 1)", "(sorted-set 6 2 4)", "(str (sorted-set [2 3] [1 2]))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                return VncSortedSet.ofAll(args);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction new_mutable_set =
            new VncFunction(
                    "mutable-set",
                    VncFunction
                        .meta()
                        .arglists("(mutable-set & items)")
                        .doc("Creates a new mutable set containing the items.")
                        .examples(
                            "(mutable-set)",
                            "(mutable-set nil)",
                            "(mutable-set 1)",
                            "(mutable-set 1 2 3)",
                            "(mutable-set [1 2] 3)")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    return VncMutableSet.ofAll(args, Nil);
                }

                private static final long serialVersionUID = -1848883965231344442L;
            };

    public static VncFunction set_Q =
        new VncFunction(
                "set?",
                VncFunction
                    .meta()
                    .arglists("(set? obj)")
                    .doc("Returns true if obj is a set")
                    .examples("(set? (set 1))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(Types.isVncHashSet(args.first()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction sorted_set_Q =
        new VncFunction(
                "sorted-set?",
                VncFunction
                    .meta()
                    .arglists("(sorted-set? obj)")
                    .doc("Returns true if obj is a sorted-set")
                    .examples("(sorted-set? (sorted-set 1))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(Types.isVncSortedSet(args.first()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction mutable_set_Q =
        new VncFunction(
                "mutable-set?",
                VncFunction
                    .meta()
                    .arglists("(mutable-set? obj)")
                    .doc("Returns true if obj is a mutable-set")
                    .examples("(mutable-set? (mutable-set 1))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(Types.isVncMutableSet(args.first()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction distinct_Q =
        new VncFunction(
                "distinct?",
                VncFunction
                    .meta()
                    .arglists("(distinct? x) (distinct? x y) (distinct? x y & more)")
                    .doc("Returns true if no two of the arguments are equal")
                    .examples(
                        "(distinct? 1 2 3)",
                        "(distinct? 1 2 3 3)",
                        "(distinct? 1 2 3 1)")
                    .seeAlso("distinct")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                VncList vals = args;
                Set<VncVal> visited = new HashSet<>();

                while(!vals.isEmpty()) {
                    final VncVal v = vals.first();
                    vals = vals.rest();

                    if (visited.contains(v)) {
                        return VncBoolean.False;
                    }

                    visited.add(v);
                }

                return VncBoolean.True;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction difference =
        new VncFunction(
                "difference",
                VncFunction
                    .meta()
                    .arglists("(difference s1)", "(difference s1 s2)", "(difference s1 s2 & sets)")
                    .doc("Return a set that is the first set without elements of the remaining sets")
                    .examples(
                        "(difference (set 1 2 3))",
                        "(difference (set 1 2) (set 2 3))",
                        "(difference (set 1 2) (set 1) (set 1 4) (set 3))")
                    .seeAlso("union", "intersection", "cons", "conj", "disj")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                Set<VncVal> set = new HashSet<>(Coerce.toVncSet(args.first()).getJavaSet());

                for(int ii=1; ii<args.size(); ii++) {
                    set.removeAll(Coerce.toVncSet(args.nth(ii)).getJavaSet());
                }

                return VncHashSet.ofAll(set);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction union =
        new VncFunction(
                "union",
                VncFunction
                    .meta()
                    .arglists("(union s1)", "(union s1 s2)", "(union s1 s2 & sets)")
                    .doc("Return a set that is the union of the input sets")
                    .examples(
                        "(union (set 1 2 3))",
                        "(union (set 1 2) (set 2 3))",
                        "(union (set 1 2 3) (set 1 2) (set 1 4) (set 3))")
                    .seeAlso("difference", "intersection", "cons", "conj", "disj")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                final Set<VncVal> set = new HashSet<>(Coerce.toVncSet(args.first()).getJavaSet());

                for(int ii=1; ii<args.size(); ii++) {
                    set.addAll(Coerce.toVncSet(args.nth(ii)).getJavaSet());
                }

                return VncHashSet.ofAll(set);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction intersection =
        new VncFunction(
                "intersection",
                VncFunction
                    .meta()
                    .arglists("(intersection s1)", "(intersection s1 s2)", "(intersection s1 s2 & sets)")
                    .doc("Return a set that is the intersection of the input sets")
                    .examples(
                        "(intersection (set 1))",
                        "(intersection (set 1 2) (set 2 3))",
                        "(intersection (set 1 2) (set 3 4))")
                    .seeAlso("union", "difference", "cons", "conj", "disj")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                final Set<VncVal> intersection = new HashSet<>();

                final Set<VncVal> first = Coerce.toVncSet(args.first()).getJavaSet();

                first.forEach(v -> {
                    boolean intersect = true;

                    for(int ii=1; ii<args.size(); ii++) {
                        if (!Coerce.toVncSet(args.nth(ii)).getJavaSet().contains(v)) {
                            intersect = false;
                            break;
                        }
                    }

                    if (intersect) {
                        intersection.add(v);
                    }
                });

                return VncHashSet.ofAll(intersection);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction subset_Q =
        new VncFunction(
                "subset?",
                VncFunction
                    .meta()
                    .arglists("(subset? set1 set2)")
                    .doc("Return true if set1 is a subset of set2")
                    .examples(
                        "(subset? #{2 3} #{1 2 3 4})",
                        "(subset? #{2 5} #{1 2 3 4})")
                    .seeAlso("set", "superset?", "union", "difference", "intersection")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final VncSet set1 = Coerce.toVncSet(args.first());
                final VncSet set2 = Coerce.toVncSet(args.second());

                return VncBoolean.of(set1.stream().allMatch(v -> set2.contains(v)));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction superset_Q =
        new VncFunction(
                "superset?",
                VncFunction
                    .meta()
                    .arglists("(superset? set1 set2)")
                    .doc("Return true if set1 is a superset of set2")
                    .examples(
                        "(superset? #{1 2 3 4} #{2 3} )",
                        "(superset? #{1 2 3 4} #{2 5})")
                    .seeAlso("set", "subset?", "union", "difference", "intersection")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final VncSet set1 = Coerce.toVncSet(args.first());
                final VncSet set2 = Coerce.toVncSet(args.second());

                return VncBoolean.of(set2.stream().allMatch(v -> set1.contains(v)));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction juxt =
        new VncFunction(
                "juxt",
                VncFunction
                    .meta()
                    .arglists(
                        "(juxt f)",
                        "(juxt f g)",
                        "(juxt f g h)",
                        "(juxt f g h & fs)")
                    .doc(
                        "Takes a set of functions and returns a fn that is the juxtaposition " +
                        "of those fns. The returned fn takes a variable number of args, and " +
                        "returns a vector containing the result of applying each fn to the " +
                        "args (left-to-right).\n\n" +
                        "`((juxt a b c) x) => [(a x) (b x) (c x)]`")
                    .examples(
                        "((juxt first last) '(1 2 3 4))",

                        "(do                                                   \n" +
                        "  (defn index-by [coll key-fn]                        \n" +
                        "     (into {} (map (juxt key-fn identity) coll)))     \n" +
                        "                                                      \n" +
                        "  (index-by [{:id 1 :name \"foo\"}                    \n" +
                        "             {:id 2 :name \"bar\"}                    \n" +
                        "             {:id 3 :name \"baz\"}]                   \n" +
                        "            :id))                                       ")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                final List<IVncFunction> functions = new ArrayList<>();
                for(VncVal a : args) {
                    final IVncFunction fn = Coerce.toIVncFunction(a);
                    fn.sandboxFunctionCallValidation();
                    functions.add(fn);
                }

                return new VncFunction(createAnonymousFuncName("juxt:wrapped")) {
                    @Override
                    public VncVal apply(final VncList args) {
                        final List<VncVal> values = new ArrayList<>();
                        functions
                            .stream()
                            .forEach(f -> values.add(f.apply(args)));

                        return VncVector.ofList(values);
                    }

                    private static final long serialVersionUID = -1848883965231344442L;
                };
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction fnil =
        new VncFunction(
                "fnil",
                VncFunction
                    .meta()
                    .arglists(
                        "(fnil f x)",
                        "(fnil f x y)",
                        "(fnil f x y z)")
                    .doc(
                        "Takes a function f, and returns a function that calls f, replacing " +
                        "a nil first argument to f with the supplied value x. Higher arity " +
                        "versions can replace arguments in the second and third " +
                        "positions (y, z). Note that the function f can take any number of " +
                        "arguments, not just the one(s) being nil-patched.")
                    .examples(
                        ";; e.g.: change the `str/lower-case` handling of nil arguments by \n" +
                        ";; returning an empty string instead of nil.                      \n" +
                        "((fnil str/lower-case \"\") nil)                                  ",
                        "((fnil + 10) nil)",
                        "((fnil + 10) nil 1)",
                        "((fnil + 10) nil 1 2)",
                        "((fnil + 10) 20 1 2)",
                        "((fnil + 10) nil 1 2 3 4)",
                        "((fnil + 1000 100) nil nil)",
                        "((fnil + 1000 100) 2000 nil 1)",
                        "((fnil + 1000 100) nil 200 1 2)",
                        "((fnil + 1000 100) nil nil 1 2 3 4)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2, 3, 4);

                final MeterRegistry meterRegistry = ThreadContext.getMeterRegistry();

                final List<VncFunction> functions = new ArrayList<>();

                final IVncFunction fn = Coerce.toIVncFunction(args.first());
                fn.sandboxFunctionCallValidation();

                if (args.size() == 2) {
                    final VncVal x = args.second();

                    functions.add(
                        new VncFunction(
                                createAnonymousFuncName("fnil:wrapped"),
                                VncVector.of(new VncSymbol("&"), new VncString("args"))
                        ) {
                            @Override
                            public VncVal apply(final VncList args) {
                                if (args.isEmpty()) {
                                    throw new VncException("fnil: the passed fn function requires at least one arg");
                                }
                                else {
                                    final VncVal a = args.first();
                                    return VncFunction.applyWithMeter(
                                                fn,
                                                args.rest().addAtStart(a == Nil ? x : a),
                                                meterRegistry);
                                }
                            }

                            private static final long serialVersionUID = -1848883965231344442L;
                        });
                }
                else if (args.size() == 3) {
                    final VncVal x = args.second();
                    final VncVal y = args.third();

                    functions.add(
                    new VncFunction(
                                createAnonymousFuncName("fnil:wrapped"),
                                VncVector.of(new VncSymbol("&"), new VncString("args"))
                        ) {
                            @Override
                            public VncVal apply(final VncList args) {
                                if (args.size() < 2) {
                                    throw new VncException("fnil: the passed fn function requires at least two args");
                                }
                                else {
                                    final VncVal a = args.first();
                                    final VncVal b = args.second();
                                    return VncFunction.applyWithMeter(
                                                fn,
                                                args.rest()
                                                    .rest()
                                                    .addAtStart(b == Nil ? y : b)
                                                    .addAtStart(a == Nil ? x : a),
                                                meterRegistry);
                                }
                            }

                            private static final long serialVersionUID = -1848883965231344442L;
                        });
                }
                else if (args.size() == 4) {
                    final VncVal x = args.second();
                    final VncVal y = args.third();
                    final VncVal z = args.fourth();

                    functions.add(
                        new VncFunction(
                                createAnonymousFuncName("fnil:wrapped"),
                                VncVector.of(new VncSymbol("&"), new VncString("args"))
                        ) {
                            @Override
                            public VncVal apply(final VncList args) {
                                if (args.size() < 3) {
                                    throw new VncException("fnil: the passed fn function requires at least three args");
                                }
                                else {
                                    final VncVal a = args.first();
                                    final VncVal b = args.second();
                                    final VncVal c = args.third();
                                    return VncFunction.applyWithMeter(
                                                fn,
                                                args.rest()
                                                    .rest()
                                                    .rest()
                                                    .addAtStart(c == Nil ? z : c)
                                                    .addAtStart(b == Nil ? y : b)
                                                    .addAtStart(a == Nil ? x : a),
                                                meterRegistry);
                                }
                            }

                            private static final long serialVersionUID = -1848883965231344442L;
                        });
                }
                else {
                    return Nil; // we never get here, handled by arity check
                }

                return new VncMultiArityFunction(
                                createAnonymousFuncName("fnil"),
                                functions,
                                false,
                                Nil);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    ///////////////////////////////////////////////////////////////////////////
    // HashMap functions
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction new_hash_map =
        new VncFunction(
                "hash-map",
                VncFunction
                    .meta()
                    .arglists("(hash-map & keyvals)", "(hash-map map)")
                    .doc("Creates a new hash map containing the items.")
                    .examples(
                        "(hash-map :a 1 :b 2)",
                        "(hash-map (sorted-map :a 1 :b 2))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                if (args.size() == 1 && Types.isVncMap(args.first())) {
                    final VncMap map = (VncMap)args.first();
                    return map instanceof VncHashMap ? map : new VncHashMap(map.getJavaMap());
                }
                else if (args.size() == 1 && Types.isVncJavaObject(args.first())) {
                    return ((VncJavaObject)args.first()).toVncMap();
                }
                else {
                    return VncHashMap.ofAll(args);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction new_ordered_map =
        new VncFunction(
                "ordered-map",
                VncFunction
                    .meta()
                    .arglists("(ordered-map & keyvals)", "(ordered-map map)")
                    .doc("Creates a new ordered map containing the items.")
                    .examples(
                        "(ordered-map :a 1 :b 2)",
                        "(ordered-map (hash-map :a 1 :b 2))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                if (args.size() == 1 && Types.isVncMap(args.first())) {
                    final VncMap map = (VncMap)args.first();
                    return map instanceof VncOrderedMap ? map : new VncOrderedMap(map.getJavaMap());
                }
                else {
                    return VncOrderedMap.ofAll(args);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction new_sorted_map =
        new VncFunction(
                "sorted-map",
                VncFunction
                    .meta()
                    .arglists("(sorted-map & keyvals)", "(sorted-map map)")
                    .doc("Creates a new sorted map containing the items.")
                    .examples(
                        "(sorted-map :a 1 :b 2)",
                        "(sorted-map (hash-map :a 1 :b 2))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                if (args.size() == 1 && Types.isVncMap(args.first())) {
                    final VncMap map = (VncMap)args.first();
                    return map instanceof VncSortedMap ? map : new VncSortedMap(map.getJavaMap());
                }
                else {
                    return VncSortedMap.ofAll(args);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction new_mutable_map =
        new VncFunction(
                "mutable-map",
                VncFunction
                    .meta()
                    .arglists("(mutable-map & keyvals)", "(mutable-map map)")
                    .doc("Creates a new mutable threadsafe map containing the items.")
                    .examples(
                        "(mutable-map :a 1 :b 2)",
                        "(mutable-map {:a 1 :b 2})")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                return args.size() == 1 && Types.isVncMap(args.first())
                        ? new VncMutableMap(((VncMap)args.first()).getJavaMap())
                        : VncMutableMap.ofAll(args);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction new_stack =
        new VncFunction(
                "stack",
                VncFunction
                    .meta()
                    .arglists("(stack)")
                    .doc("Creates a new mutable threadsafe stack.")
                    .examples(
                        "(let [s (stack)]   \n" +
                        "   (push! s 1)     \n" +
                        "   (push! s 2)     \n" +
                        "   (push! s 3))      ")
                    .seeAlso(
                        "peek", "pop!", "push!",
                        "empty", "empty?", "count",
                        "into!", "conj!", "stack?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                return new VncStack();
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction new_queue =
        new VncFunction(
                "queue",
                VncFunction
                    .meta()
                    .arglists("(queue)", "(queue capacity)")
                    .doc(
                        "Creates a new mutable threadsafe bounded or unbounded queue.\n\n" +
                        "The queue can be turned into a synchronous queue when using " +
                        "the functions `put!` and `take!`. `put!` waits until the value " +
                        "be added and `take! waits until a value is available from " +
                        "queue thus synchronizing the producer and consumer.")
                    .examples(
                        "; unbounded queue  \n" +
                        "(let [q (queue)]   \n" +
                        "  (offer! q 1)     \n" +
                        "  (offer! q 2)     \n" +
                        "  (offer! q 3)     \n" +
                        "  (poll! q)        \n" +
                        "  q)                ",

                        "; bounded queue       \n" +
                        "(let [q (queue 10)]   \n" +
                        "  (offer! q 1000 1)   \n" +
                        "  (offer! q 1000 2)   \n" +
                        "  (offer! q 1000 3)   \n" +
                        "  (poll! q 1000)      \n" +
                        "  q)                   ",

                        "; synchronous unbounded queue  \n" +
                        "(let [q (queue)]               \n" +
                        "  (put! q 1)                   \n" +
                        "  (put! q 2)                   \n" +
                        "  (put! q 3)                   \n" +
                        "  (take! q)                    \n" +
                        "  q)                            ",

                        "; synchronous bounded queue  \n" +
                        "(let [q (queue 10)]          \n" +
                        "  (put! q 1)                 \n" +
                        "  (put! q 2)                 \n" +
                        "  (put! q 3)                 \n" +
                        "  (take! q)                  \n" +
                        "  q)                          ")
                    .seeAlso(
                        "peek", "put!", "take!", "offer!", "poll!",
                        "empty", "empty?", "count", "queue?",
                        "reduce", "transduce", "docoll",
                        "into!", "conj!")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0, 1);

                return args.isEmpty()
                        ? new VncQueue()
                        : new VncQueue(Coerce.toVncLong(args.first()).getIntValue());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction new_delay_queue =
        new VncFunction(
                "delay-queue",
                VncFunction
                    .meta()
                    .arglists("(delay-queue)")
                    .doc(
                        "Creates a new delay queue.\n\n" +
                        "A delay-queue is an unbounded blocking queue of delayed elements, " +
                        "in which an element can only be taken when its delay has expired. " +
                        "The head of the queue is that delayed element whose delay expired " +
                        "furthest in the past. If no delay has expired there is no head and " +
                        "`poll!` will return nil. Unexpired elements cannot be removed using " +
                        "`take!` or `poll!`, they are otherwise treated as normal elements. " +
                        "For example, the `count` method returns the count of both expired and " +
                        "unexpired elements. This queue does not permit `nil` elements.\n\n" +
                        "Example rate limiter:\n\n" +
                        "```\n" +
                        "(do                                                                \n" +
                        "  (defprotocol RateLimiter (init [x]) (aquire [x]))                \n" +
                        "                                                                   \n" +
                        "  (deftype :rate-limiter [queue                :core/delay-queue   \n" +
                        "                          limit-for-period     :long               \n" +
                        "                          limit-refresh-period :long]              \n" +
                        "           RateLimiter                                             \n" +
                        "             (init [this]   (let [q (:queue this)                  \n" +
                        "                                  n (:limit-for-period this)]      \n" +
                        "                              (empty q)                            \n" +
                        "                              (repeatedly n #(put! q :token 0))    \n" +
                        "                              this))                               \n" +
                        "             (aquire [this] (let [q (:queue this)                  \n" +
                        "                                  p (:limit-refresh-period this)]  \n" +
                        "                              (take! q)                            \n" +
                        "                              (put! q :token p))))                 \n" +
                        "                                                                   \n" +
                        "  ;; create a limiter with a limit of 5 actions within a 2s period \n" +
                        "  (def limiter (init (rate-limiter. (delay-queue) 5 2000)))        \n" +
                        "                                                                   \n" +
                        "  ;; test the limiter                                              \n" +
                        "  (doseq [x (range 1 26)]                                          \n" +
                        "    (aquire limiter)                                               \n" +
                        "    (printf \"%s: run %2d%n\" (time/local-date-time) x)))          \n" +
                        "```")
                    .examples(
                        "(let [q (delay-queue)]  \n" +
                        "  (put! q 1 100)        \n" +
                        "  (put! q 1 200)        \n" +
                        "  (take! q))              ")
                    .seeAlso(
                        "peek", "put!", "take!", "poll!", "empty", "empty?", "count", "delay-queue?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                return new VncDelayQueue(null);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction new_map_entry =
        new VncFunction(
                "map-entry",
                VncFunction
                    .meta()
                    .arglists("(map-entry key val)")
                    .doc("Creates a new map entry")
                    .examples(
                        "(map-entry :a 1)",
                        "(key (map-entry :a 1))",
                        "(val (map-entry :a 1))",
                        "(entries {:a 1 :b 2 :c 3})")
                    .seeAlso("map-entry?", "entries", "map", "key", "val")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                return new VncMapEntry(args.first(), args.second());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction map_Q =
        new VncFunction(
                "map?",
                VncFunction
                    .meta()
                    .arglists("(map? obj)")
                    .doc("Returns true if obj is a map")
                    .examples("(map? {:a 1 :b 2})")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(Types.isVncMap(args.first()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction hash_map_Q =
        new VncFunction(
                "hash-map?",
                VncFunction
                    .meta()
                    .arglists("(hash-map? obj)")
                    .doc("Returns true if obj is a hash map")
                    .examples("(hash-map? (hash-map :a 1 :b 2))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(Types.isVncHashMap(args.first()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction ordered_map_Q =
        new VncFunction(
                "ordered-map?",
                VncFunction
                    .meta()
                    .arglists("(ordered-map? obj)")
                    .doc("Returns true if obj is an ordered map")
                    .examples("(ordered-map? (ordered-map :a 1 :b 2))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(Types.isVncOrderedMap(args.first()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction sorted_map_Q =
        new VncFunction(
                "sorted-map?",
                VncFunction
                    .meta()
                    .arglists("(sorted-map? obj)")
                    .doc("Returns true if obj is a sorted map")
                    .examples("(sorted-map? (sorted-map :a 1 :b 2))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(Types.isVncSortedMap(args.first()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction mutable_map_Q =
        new VncFunction(
                "mutable-map?",
                VncFunction
                    .meta()
                    .arglists("(mutable-map? obj)")
                    .doc("Returns true if obj is a mutable map")
                    .examples("(mutable-map? (mutable-map :a 1 :b 2))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(Types.isVncMutableMap(args.first()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction map_entry_Q =
        new VncFunction(
                "map-entry?",
                VncFunction
                    .meta()
                    .arglists("(map-entry? m)")
                    .doc("Returns true if m is a map entry")
                    .examples(
                        "(map-entry? (map-entry :a 1))",
                        "(map-entry? (first (entries {:a 1 :b 2})))")
                    .seeAlso("map-entry", "entries", "map")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(Types.isVncMapEntry(args.first()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction stack_Q =
            new VncFunction(
                    "stack?",
                    VncFunction
                        .meta()
                        .arglists("(stack? coll)")
                        .doc("Returns true if coll is a stack")
                        .examples("(stack? (stack))")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertArity(this, args, 1);

                    return VncBoolean.of(Types.isVncStack(args.first()));
                }

                private static final long serialVersionUID = -1848883965231344442L;
            };

    public static VncFunction queue_Q =
        new VncFunction(
                "queue?",
                VncFunction
                    .meta()
                    .arglists("(queue? coll)")
                    .doc("Returns true if coll is a queue")
                    .examples("(queue? (queue))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(Types.isVncQueue(args.first()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction delay_queue_Q =
        new VncFunction(
                "delay-queue?",
                VncFunction
                    .meta()
                    .arglists("(delay-queue? coll)")
                    .doc("Returns true if coll is a delay-queue")
                    .examples("(delay-queue? (delay-queue))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(Types.isVncDelayQueue(args.first()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction contains_Q =
        new VncFunction(
                "contains?",
                VncFunction
                    .meta()
                    .arglists("(contains? coll key)")
                    .doc(
                        "Returns true if key is present in the given collection, otherwise " +
                        "returns false.\n\n" +
                        "Note: To test if a value is in a vector or list use `any?` ")
                    .examples(
                        "(contains? #{:a :b} :a)",
                        "(contains? {:a 1 :b 2} :a)",
                        "(contains? [10 11 12] 1)",
                        "(contains? [10 11 12] 5)",
                        "(contains? \"abc\" 1)",
                        "(contains? \"abc\" 5)")
                    .seeAlso("not-contains?", "any?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final VncVal coll = args.first();
                final VncVal key = args.second();

                if (Types.isVncMap(coll)) {
                    return ((VncMap)coll).containsKey(key);
                }
                else if (Types.isVncVector(coll)) {
                    final VncVector v = (VncVector)coll;
                    final VncLong k = Coerce.toVncLong(key);
                    return VncBoolean.of(v.size() > k.getValue().intValue());
                }
                else if (Types.isVncSet(coll)) {
                    return VncBoolean.of(((VncSet)coll).contains(key));
                }
                else if (Types.isVncString(coll)) {
                    final VncString s = (VncString)coll;
                    final VncLong k = Coerce.toVncLong(key);
                    return VncBoolean.of(s.getValue().length() > k.getValue().intValue());
                }
                else {
                    throw new VncException(String.format(
                            "Function 'contains?' does not allow %s as coll. ",
                            Types.getType(coll)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction not_contains_Q =
        new VncFunction(
                "not-contains?",
                VncFunction
                    .meta()
                    .arglists("(not-contains? coll key)")
                    .doc(
                        "Returns true if key is not present in the given collection, otherwise " +
                        "returns false.")
                    .examples(
                        "(not-contains? #{:a :b} :c)",
                        "(not-contains? {:a 1 :b 2} :c)",
                        "(not-contains? [10 11 12] 1)",
                        "(not-contains? [10 11 12] 5)",
                        "(not-contains? \"abc\" 1)",
                        "(not-contains? \"abc\" 5)")
                    .seeAlso(
                        "contains?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                return VncBoolean.of(VncBoolean.isFalse(contains_Q.apply(args)));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction assoc =
        new VncFunction(
                "assoc",
                VncFunction
                    .meta()
                    .arglists("(assoc coll key val)", "(assoc coll key val & kvs)")
                    .doc(
                        "When applied to a map, returns a new map of the same type, that " +
                        "contains the mapping of key(s) to val(s). " +
                        "When applied to a vector, returns a new vector that contains val " +
                        "at index. Note - index must be <= (count vector). " +
                        "When applied to a custom type, returns a new custom type with " +
                        "passed fields changed.")
                    .examples(
                        "(assoc {} :a 1 :b 2)",
                        "(assoc nil :a 1 :b 2)",
                        "(assoc [1 2 3] 0 10)",
                        "(assoc [1 2 3] 3 10)",
                        "(assoc [1 2 3] 6 10)",
                        "(do                                                 \n" +
                        "  (deftype :complex [real :long, imaginary :long])  \n" +
                        "  (def x (complex. 100 200))                        \n" +
                        "  (def y (assoc x :real 110))                       \n" +
                        "  (pr-str y))                                         ")
                    .seeAlso("dissoc", "update")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                final VncVal coll = args.first();
                if (coll == Nil) {
                    return new VncHashMap().assoc(args.rest());
                }
                else if (Types.isVncMutableMap(coll)) {
                    throw new VncException(
                            "Function 'assoc' can not be used with mutable maps use assoc!");
                }
                else if (Types.isVncMutableVector(coll) || Types.isVncMutableList(coll)) {
                    throw new VncException(
                            "Function 'assoc' can not be used with mutable vectors use assoc!");
                }
                else if (Types.isVncCustomType(coll)) {
                    return ((VncCustomType)coll).assoc(args.rest());
                }
                else if (Types.isVncMap(coll)) {
                    return ((VncMap)coll).assoc(args.rest());
                }
                else if (Types.isVncVector(coll) || Types.isVncList(coll)) {
                    VncSequence seq = ((VncSequence)coll);

                    final VncList keyvals = args.rest();
                    for(int ii=0; ii<keyvals.size(); ii+=2) {
                        final VncLong key = Coerce.toVncLong(keyvals.nth(ii));
                        final VncVal val = keyvals.nth(ii+1);
                        if (seq.size() > key.getValue().intValue()) {
                            seq = seq.setAt(key.getValue().intValue(), val);
                        }
                        else {
                            seq = seq.addAtEnd(val);
                        }
                    }
                    return seq;
                }
                else if (Types.isVncString(coll)) {
                    String s = ((VncString)coll).getValue();
                    final VncList keyvals = args.rest();
                    for(int ii=0; ii<keyvals.size(); ii+=2) {
                        final VncLong key = Coerce.toVncLong(keyvals.nth(ii));
                        final VncString val = Coerce.toVncString(keyvals.nth(ii+1));
                        final int idx = key.getValue().intValue();
                        if (s.length() > idx) {
                            if (idx == 0) {
                                s = "" + val.getValue().charAt(0) + s.substring(1);
                            }
                            else if (idx == s.length()-1) {
                                s = s.substring(0, idx)  + val.getValue().charAt(0);
                            }
                            else {
                                s = s.substring(0, idx) + val.getValue().charAt(0) + s.substring(idx+1);
                            }
                        }
                        else {
                            s = s + val.getValue().charAt(0);
                        }
                    }
                    return new VncString(s);
                }
                else {
                    throw new VncException(String.format(
                            "Function 'assoc' does not allow %s as collection",
                            Types.getType(coll)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction assoc_BANG =
        new VncFunction(
                "assoc!",
                VncFunction
                    .meta()
                    .arglists("(assoc! coll key val)", "(assoc! coll key val & kvs)")
                    .doc("Associates key/vals with a mutable map, returns the map")
                    .examples(
                        "(assoc! nil :a 1 :b 2)",
                        "(assoc! (mutable-map) :a 1 :b 2)",
                        "(assoc! (mutable-vector 1 2 3) 0 10)",
                        "(assoc! (mutable-vector 1 2 3) 3 10)",
                        "(assoc! (mutable-vector 1 2 3) 6 10)")
                    .seeAlso("dissoc!", "update!")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                final VncVal coll = args.first();
                if (coll == Nil) {
                    return new VncMutableMap().assoc(args.rest());
                }
                else if (Types.isVncMutableMap(coll) || Types.isVncJavaMap(coll)) {
                    return ((VncMap)coll).assoc(args.rest());
                }
                else if (Types.isVncMutableVector(coll) || Types.isVncMutableList(coll) || Types.isVncJavaList(coll)) {
                    VncSequence seq = ((VncSequence)coll);

                    final VncList keyvals = args.rest();
                    for(int ii=0; ii<keyvals.size(); ii+=2) {
                        final VncLong key = Coerce.toVncLong(keyvals.nth(ii));
                        final VncVal val = keyvals.nth(ii+1);
                        if (seq.size() > key.getValue().intValue()) {
                            seq = seq.setAt(key.getValue().intValue(), val);
                        }
                        else {
                            seq = seq.addAtEnd(val);
                        }
                    }
                    return seq;
                }
                else if (Types.isVncThreadLocal(coll)) {
                    final VncThreadLocal th = (VncThreadLocal)coll;

                    return th.assoc(args.rest());
                }
                else {
                    throw new VncException(String.format(
                            "Function 'assoc!' does not allow %s as collection. It works with mutable maps only.",
                            Types.getType(coll)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction assoc_in =
        new VncFunction(
                "assoc-in",
                VncFunction
                    .meta()
                    .arglists("(assoc-in m ks v)")
                    .doc(
                        "Associates a value in a nested associative structure, where ks is a " +
                        "sequence of keys and v is the new value and returns a new nested structure. " +
                        "If any levels do not exist, hash-maps or vectors will be created.")
                    .examples(
                        "(do                                               \n" +
                        "  (def users [ {:name \"James\" :age 26}          \n" +
                        "               {:name \"John\" :age 43}])         \n" +
                        "  (assoc-in users [1 :age] 44))                   ",
                        "(do                                               \n" +
                        "  (def users [ {:name \"James\" :age 26}          \n" +
                        "               {:name \"John\" :age 43}])         \n" +
                        "  (assoc-in users [2] {:name \"Jack\" :age 19}))  ",
                        "(assoc-in [[1 2] [3 4]] [0 0] 9)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 3);

                final VncVal coll = args.first(); // may be Nil
                final VncSequence keys = Coerce.toVncSequence(args.second());
                final VncVal val = args.third();

                final VncVal key = keys.first();
                final VncSequence keyRest = keys.rest();

                if (keyRest.isEmpty()) {
                    return assoc.applyOf(coll, key, val);
                }
                else {
                    final VncVal childColl = get.applyOf(coll, key);
                    if (childColl == Nil
                            || childColl instanceof VncSequence
                            || childColl instanceof VncMap
                    ) {
                        return assoc.applyOf(
                                    coll,
                                    key,
                                    assoc_in.applyOf(childColl, keyRest, val));
                    }
                    else {
                        return assoc.applyOf(
                                coll,
                                key,
                                assoc_in.applyOf(VncHashMap.empty(), keyRest, val));
                    }
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction dissoc_in =
        new VncFunction(
                "dissoc-in",
                VncFunction
                    .meta()
                    .arglists("(dissoc-in m ks)")
                    .doc(
                        "Dissociates an entrye in a nested associative structure, where ks is a " +
                        "sequence of keys and returns a new nested structure.")
                    .examples(
                        "(do                                               \n" +
                        "  (def users [ {:name \"James\" :age 26}          \n" +
                        "               {:name \"John\" :age 43} ])        \n" +
                        "  (dissoc-in users [1]))                            ",
                        "(do                                               \n" +
                        "  (def users [ {:name \"James\" :age 26}          \n" +
                        "               {:name \"John\" :age 43} ])        \n" +
                        "  (dissoc-in users [1 :age]))                       ")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final VncVal coll = args.first();
                final VncSequence keys = Coerce.toVncSequence(args.second());

                if (keys.size() == 0) {
                    return coll;
                }
                else if (keys.size() == 1) {
                    return dissoc.applyOf(coll, keys.first());
                }
                else {
                    return update_in.applyOf(coll, keys.butlast(), dissoc, keys.last());
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction update_in =
        new VncFunction(
                "update-in",
                VncFunction
                    .meta()
                    .arglists("(update-in [m ks f & fargs])")
                    .doc(
                        "Updates' a value in a nested associative structure, where ks is a " +
                        "sequence of keys and f is a function that will take the old value " +
                        "and any supplied fargs and return the new value, and returns a new " +
                        "nested structure. \n\n" +
                        "If any levels do not exist, hash-maps will be reated.")
                    .examples(
                        "(do                                               \n" +
                        "  (def users [ {:name \"James\" :age 26}          \n" +
                        "               {:name \"John\" :age 43} ])        \n" +
                        "  (update-in users [1 :age] inc))                   ",
                        "(update-in {:a 12} [:a] * 4)",
                        "(update-in {:a 12} [:a] + 3 4)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 3);

                final VncFunction up = new VncFunction("up", this.getMeta()) {
                    @Override
                    public VncVal apply(final VncList args) {
                        final VncVal m_ = args.first();
                        final VncVal k_ = Coerce.toVncSequence(args.second()).first();
                        final VncSequence ks_ = Coerce.toVncSequence(args.second()).rest();
                        final VncFunction f_ = Coerce.toVncFunction(args.third());
                        final VncVal args_ = args.slice(3);

                        f_.sandboxFunctionCallValidation();

                        if (!ks_.isEmpty()) {
                            return assoc.applyOf(
                                    m_,
                                    k_,
                                    apply.applyOf(
                                        this,
                                        get.applyOf(m_, k_),
                                        ks_,
                                        f_,
                                        args_));
                        }
                        else {
                            return assoc.applyOf(
                                    m_,
                                    k_,
                                    apply.applyOf(
                                        f_,
                                        get.applyOf(m_, k_),
                                        args_));
                        }
                    }

                    private static final long serialVersionUID = -1L;
                };

                return up.apply(args);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction get_in =
        new VncFunction(
                "get-in",
                VncFunction
                    .meta()
                    .arglists("(get-in m ks)", "(get-in m ks not-found)")
                    .doc(
                        "Returns the value in a nested associative structure, " +
                        "where ks is a sequence of keys. Returns nil if the key " +
                        "is not present, or the not-found value if supplied.")
                    .examples(
                        "(get-in {:a 1 :b {:c 2 :d 3}} [:b :c])",
                        "(get-in [:a :b :c] [0])",
                        "(get-in [:a :b [:c :d :e]] [2 1])",
                        "(get-in {:a 1 :b {:c [4 5 6]}} [:b :c 1])")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2, 3);

                if (args.first() == Constants.Nil) {
                    return Constants.Nil;
                }

                VncCollection coll = Coerce.toVncCollection(args.first());
                VncSequence keys = Coerce.toVncSequence(args.second());
                VncVal key_not_found = (args.size() == 3) ? args.nth(2) : Nil;

                while(!keys.isEmpty()) {
                    final VncVal key = keys.first();
                    keys = keys.rest();

                    if (Types.isVncMap(coll)) {
                        final VncVal val = ((VncMap)coll).get(key);
                        if (val == Nil) {
                            return key_not_found;
                        }
                        else if (keys.isEmpty()) {
                            return val;
                        }
                        else if (Types.isVncCollection(val)) {
                            coll = (VncCollection)val;
                        }
                        else {
                            return key_not_found;
                        }
                    }
                    else {
                        if (Types.isVncLong(key)) {
                            final int index = ((VncLong)key).getValue().intValue();
                            final VncVal val = ((VncSequence)coll).nthOrDefault(index, Nil);
                            if (val == Nil) {
                                return key_not_found;
                            }
                            else if (keys.isEmpty()) {
                                return val;
                            }
                            else if (Types.isVncCollection(val)) {
                                coll = (VncCollection)val;
                            }
                            else {
                                return key_not_found;
                            }
                        }
                        else {
                            return key_not_found;
                        }
                    }
                }

                return key_not_found;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction dissoc =
        new VncFunction(
                "dissoc",
                VncFunction
                    .meta()
                    .arglists("(dissoc coll key)", "(dissoc coll key & ks)")
                    .doc(
                        "Returns a new coll of the same type, " +
                        "that does not contain a mapping for key(s)")
                    .examples(
                        "(dissoc {:a 1 :b 2 :c 3} :b)",
                        "(dissoc {:a 1 :b 2 :c 3} :c :b)",
                        "(dissoc [1 2 3] 0)",
                        "(do                                                 \n" +
                        "  (deftype :complex [real :long, imaginary :long])  \n" +
                        "  (def x (complex. 100 200))                        \n" +
                        "  (def y (dissoc x :real))                          \n" +
                        "  (pr-str y))                                         ")
                    .seeAlso("assoc", "update")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                final VncVal coll = args.first();
                if (coll == Nil) {
                    return Nil;
                }
                else if (Types.isVncMutableMap(coll)) {
                    throw new VncException(
                            "Function 'dissoc' can not be used with mutable maps use dissoc!");
                }
                else if (Types.isVncMap(coll)) {
                    return ((VncMap)args.first()).dissoc(args.rest());
                }
                else if (Types.isVncVector(coll)) {
                    VncVector vec = ((VncVector)coll);
                    final VncList keyvals = args.rest();
                    for(int ii=0; ii<keyvals.size(); ii++) {
                        final VncLong key = Coerce.toVncLong(keyvals.nth(ii));
                        if (vec.size() > key.getValue().intValue()) {
                            vec = vec.removeAt(key.getValue().intValue());
                        }
                    }
                    return vec;
                }
                else if (Types.isVncString(coll)) {
                    String s = ((VncString)coll).getValue();
                    final VncList keyvals = args.rest();
                    for(int ii=0; ii<keyvals.size(); ii++) {
                        final VncLong key = Coerce.toVncLong(keyvals.nth(ii));
                        final int idx = key.getValue().intValue();
                        if (s.length() > idx) {
                            if (idx == 0) {
                                s = s.substring(1);
                            }
                            else if (idx == s.length()-1) {
                                s = s.substring(0, idx);
                            }
                            else {
                                s = s.substring(0, idx) + s.substring(idx+1);
                            }
                        }
                    }
                    return new VncString(s);
                }
                else {
                    throw new VncException(String.format(
                            "Function 'dissoc' does not allow %s as coll",
                            Types.getType(coll)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction dissoc_BANG =
        new VncFunction(
                "dissoc!",
                VncFunction
                    .meta()
                    .arglists("(dissoc! coll key)", "(dissoc! coll key & ks)")
                    .doc("Dissociates keys from a mutable map, returns the map")
                    .examples(
                        "(dissoc! (mutable-map :a 1 :b 2 :c 3) :b)",
                        "(dissoc! (mutable-map :a 1 :b 2 :c 3) :c :b)",
                        "(dissoc! (mutable-vector 1 2 3) 0)")
                    .seeAlso("assoc!", "update!")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                final VncVal coll = args.first();
                if (coll == Nil) {
                    return Nil;
                }
                else if (Types.isVncMutableMap(coll)) {
                    return ((VncMap)coll).dissoc(args.rest());
                }
                else if (Types.isVncMutableVector(coll) || Types.isVncMutableList(coll) || Types.isVncJavaList(coll)) {
                    VncSequence seq = ((VncSequence)coll);

                    final VncList keyvals = args.rest();
                    for(int ii=0; ii<keyvals.size(); ii++) {
                        final VncLong key = Coerce.toVncLong(keyvals.nth(ii));
                        if (seq.size() > key.getValue().intValue()) {
                            seq = seq.removeAt(key.getValue().intValue());
                        }
                    }
                    return seq;
                }
                else if (Types.isVncThreadLocal(coll)) {
                    final VncThreadLocal th = (VncThreadLocal)coll;

                    return th.dissoc(args.rest());
                }
                else {
                    throw new VncException(String.format(
                            "Function 'dissoc!' does not allow %s as coll. It works with "
                                + "mutable maps and vectors only.",
                            Types.getType(coll)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction get =
        new VncFunction(
                "get",
                VncFunction
                    .meta()
                    .arglists("(get map key)", "(get map key not-found)")
                    .doc(
                        "Returns the value mapped to key, not-found or nil if key not " +
                        "present.\n\n" +
                        "Note: `(get :x foo)` is almost twice as fast as `(:x foo)`")
                    .examples(
                        "(get {:a 1 :b 2} :b)",
                        "(get [0 1 2 3] 1)",
                        ";; keywords act like functions on maps \n" +
                        "(:b {:a 1 :b 2})")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2, 3);

                final VncVal coll = args.first();

                if (coll == Nil) {
                    final VncVal key_not_found = (args.size() == 3) ? args.third() : Nil;
                    return key_not_found;
                }
                else if (Types.isVncMap(coll)) {
                    final VncMap map = (VncMap)coll;
                    final VncVal key = args.second();

                    final VncVal value = map.get(key);
                    return value != Nil
                                ? value
                                : args.size() == 3 ? args.third() : Nil;
                }
                else if (Types.isVncVector(coll)) {
                    final VncVector vec = (VncVector)coll;
                    final int idx = Coerce.toVncLong(args.second()).getIntValue();
                    final VncVal key_not_found = (args.size() == 3) ? args.third() : Nil;

                    return vec.nthOrDefault(idx, key_not_found);
                }
                else if (Types.isVncThreadLocal(coll)) {
                    final VncThreadLocal th = (VncThreadLocal)coll;
                    final VncKeyword key = Coerce.toVncKeyword(args.second());
                    final VncVal value = th.get(key);

                    return value != Nil
                                ? value
                                : args.size() == 3 ? args.third() : Nil;
                }
                else if (Types.isVncSet(coll)) {
                    final VncSet set = (VncSet)coll;
                    final VncVal val = args.second();

                    return set.contains(val)
                                ? val
                                : args.size() == 3 ? args.third() : Nil;
                }
                else {
                    throw new VncException(String.format(
                            "Function 'get' does not allow %s as collection",
                            Types.getType(coll)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction find =
        new VncFunction(
                "find",
                VncFunction
                    .meta()
                    .arglists("(find map key)")
                    .doc("Returns the map entry for key, or nil if key not present.")
                    .examples(
                        "(find {:a 1 :b 2} :b)",
                        "(find {:a 1 :b 2} :z)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                if (args.first() == Nil) {
                    return Nil;
                }
                else {
                    final VncMap mhm = Coerce.toVncMap(args.first());
                    final VncVal key = args.second();

                    final VncVal value = mhm.get(key);
                    return value == Nil ? Nil : VncVector.of(key, value);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction key =
        new VncFunction(
                "key",
                VncFunction
                    .meta()
                    .arglists("(key e)")
                    .doc("Returns the key of the map entry.")
                    .examples(
                        "(key (find {:a 1 :b 2} :b))",
                        "(key (first (entries {:a 1 :b 2 :c 3})))")
                    .seeAlso("map", "entries", "val", "keys")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal first = args.first();
                if (Types.isVncMapEntry(first)) {
                    return ((VncMapEntry)first).getKey();
                }
                else {
                    return Coerce.toVncSequence(first).first();
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction keys =
        new VncFunction(
                "keys",
                VncFunction
                    .meta()
                    .arglists("(keys map)")
                    .doc(
                        "Returns a collection of the map's keys.\n\n" +
                        "Please note that the functions 'keys' and 'vals' applied " +
                        "to the same map are not guaranteed not return the keys and " +
                        "vals in the same order! \n\n" +
                        "To achieve this, keys and vals can calculated based on the " +
                        "map's entry list: \n\n" +
                        "```venice\n" +
                        "(let [e (entries {:a 1 :b 2 :c 3})]\n" +
                        "  (println (map key e))\n" +
                        "  (println (map val e)))\n" +
                        "```")
                    .examples("(keys {:a 1 :b 2 :c 3})")
                    .seeAlso("vals", "entries", "map")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return Coerce.toVncMap(args.first()).keys();
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction val =
        new VncFunction(
                "val",
                VncFunction
                    .meta()
                    .arglists("(val e)")
                    .doc("Returns the val of the map entry.")
                    .examples(
                        "(val (find {:a 1 :b 2} :b))",
                        "(val (first (entries {:a 1 :b 2 :c 3})))")
                    .seeAlso("map", "entries", "key", "vals")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal first = args.first();
                if (Types.isVncMapEntry(first)) {
                    return ((VncMapEntry)first).getValue();
                }
                else {
                    return Coerce.toVncSequence(first).second();
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction vals =
        new VncFunction(
                "vals",
                VncFunction
                    .meta()
                    .arglists("(vals map)")
                    .doc(
                        "Returns a collection of the map's values.\n\n" +
                        "Please note that the functions 'keys' and 'vals' applied " +
                        "to the same map are not guaranteed not return the keys and " +
                        "vals in the same order! \n\n" +
                        "To achieve this, keys and vals can calculated based on the " +
                        "map's entry list: \n\n" +
                        "```venice\n" +
                        "(let [e (entries {:a 1 :b 2 :c 3})]\n" +
                        "  (println (map key e))\n" +
                        "  (println (map val e)))\n" +
                        "```")
                    .examples("(vals {:a 1 :b 2 :c 3})")
                    .seeAlso("keys", "entries", "map")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncMap mhm = Coerce.toVncMap(args.first());
                return VncList.ofColl(mhm.getJavaMap().values());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction select_keys =
        new VncFunction(
                "select-keys",
                VncFunction
                    .meta()
                    .arglists("(select-keys map keyseq)")
                    .doc(
                        "Returns a map containing only those entries in map whose key is in keys")
                    .examples(
                        "(select-keys {:a 1 :b 2} [:a])",
                        "(select-keys {:a 1 :b 2} [:a :c])",
                        "(select-keys {:a 1 :b 2 :c 3} [:a :c])")
                    .seeAlso("keys", "entries", "map")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                if (args.first() == Nil) {
                    return new VncHashMap();
                }

                final VncMap map = Coerce.toVncMap(args.first());

                if (args.second() == Nil) {
                    return map.emptyWithMeta();
                }

                final VncSequence keyseq = Coerce.toVncSequence(args.second());

                VncMap newMap = map.emptyWithMeta();
                for(VncVal k : keyseq.getJavaList()) {
                    if (VncBoolean.isTrue(map.containsKey(k))) {
                        newMap = newMap.assoc(k, map.get(k));
                    }
                }

                return newMap;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction entries =
        new VncFunction(
                "entries",
                VncFunction
                    .meta()
                    .arglists("(entries m)")
                    .doc("Returns a collection of the map's entries.")
                    .examples(
                        "(entries {:a 1 :b 2 :c 3})",
                        "(let [e (entries {:a 1 :b 2 :c 3})]\n" +
                        "  (println (map key e))\n" +
                        "  (println (map val e)))",
                        ";; compare to 'into' \n" +
                        "(let [e (into [] {:a 1 :b 2 :c 3})]\n" +
                        "  (println (map first e))\n" +
                        "  (println (map second e)))")
                    .seeAlso("map", "key", "val", "keys", "vals", "map-entry")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncMap mhm = Coerce.toVncMap(args.first());
                return VncList.ofList(mhm.entries());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction update =
        new VncFunction(
                "update",
                VncFunction
                    .meta()
                    .arglists(
                        "(update m k f)",
                        "(update m k f & fargs)")
                    .doc(
                        "Updates a value in an associative structure, where k is a " +
                        "key and f is a function that will take the old value " +
                        "and any supplied fargs and return the new value. " +
                        "Returns a new structure. \n\n" +
                        "If the key does not exist, `nil` is passed as the old value. " +
                        "The optional fargs are passed to the function f as " +
                        "`(f old-value (f old-value arg1 arg2 ...) ...)`.")
                    .examples(
                        "(update [] 0 (fn [x] 5))",
                        "(update [0 1 2] 0 (fn [x] 5))",
                        "(update [0 1 2] 1 (fn [x] (+ x 3)))",
                        "(update {} :a (fn [x] 5))",
                        "(update {:a 0} :b (fn [x] 5))",
                        "(update {:a 0 :b 1} :a (fn [x] (+ x 5)))",
                        "(update [0 1 2] 1 + 3)",
                        "(update {:a 0 :b 1} :b * 4)")
                    .seeAlso(
                        "assoc", "dissoc")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 3);

                final MeterRegistry meterRegistry = ThreadContext.getMeterRegistry();

                final VncVal m = args.first();
                final VncVal k = args.second();
                final VncVal f = args.third();
                final VncList supplementalArgs = args.slice(3);

                final IVncFunction fn = Coerce.toIVncFunction(f);
                fn.sandboxFunctionCallValidation();

                if (Types.isVncSequence(m)) {
                    final VncSequence list = (VncSequence)m;
                    final int idx = Coerce.toVncLong(k).getValue().intValue();

                    if (idx < 0 || idx > list.size()) {
                        throw new VncException(String.format(
                                "Function 'update' index %d out of bounds",
                                idx));
                    }
                    else if (idx < list.size()) {
                        final VncList fnArgs = VncList.of(list.nth(idx))
                                                      .addAllAtEnd(supplementalArgs);
                        return list.setAt(idx, VncFunction.applyWithMeter(fn, fnArgs, meterRegistry));
                    }
                    else {
                        final VncList fnArgs = VncList.of(Nil)
                                                      .addAllAtEnd(supplementalArgs);
                        return list.addAtEnd(VncFunction.applyWithMeter(fn, fnArgs, meterRegistry));
                    }
                }
                else if (Types.isVncMap(m)) {
                    final VncMap map = (VncMap)m;
                    final VncList fnArgs = VncList.of(map.get(k))
                                                  .addAllAtEnd(supplementalArgs);
                    return map.assoc(k, VncFunction.applyWithMeter(fn, fnArgs, meterRegistry));
                }
                else {
                    throw new VncException(String.format(
                            "'update' does not allow %s as associative structure",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction update_BANG =
        new VncFunction(
                "update!",
                VncFunction
                    .meta()
                    .arglists("(update! m k f & fargs)")
                    .doc(
                        "Updates a value in a mutable associative structure, where k is a " +
                        "key and f is a function that will take the old value " +
                        "and any supplied fargs and return the new value. " +
                        "Returns a new structure. \n\n" +
                        "If the key does not exist, `nil` is passed as the old value. " +
                        "The optional fargs are passed to the function f as " +
                        "`(f old-value arg1 arg2 ...)`.")
                    .examples(
                        "(update! (mutable-vector) 0 (fn [x] 5))",
                        "(update! (mutable-vector 0 1 2) 0 (fn [x] 5))",
                        "(update! (mutable-vector 0 1 2) 0 (fn [x] (+ x 1)))",
                        "(update! (mutable-map) :a (fn [x] 5))",
                        "(update! (mutable-map :a 0) :b (fn [x] 5))",
                        "(update! (mutable-map :a 0 :b 1) :a (fn [x] 5))",
                        "(update! (mutable-vector 0 1 2) 0 + 4)",
                        "(update! (mutable-map :a 0 :b 1) :b * 4)")
                    .seeAlso("assoc!", "dissoc!")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 3);

                final MeterRegistry meterRegistry = ThreadContext.getMeterRegistry();

                final VncVal m = args.first();
                final VncVal k = args.second();
                final VncVal f = args.third();
                final VncList supplementalArgs = args.slice(3);

                final IVncFunction fn = Coerce.toIVncFunction(f);
                fn.sandboxFunctionCallValidation();

                if (Types.isVncMutableMap(m)) {
                    final VncMutableMap map = (VncMutableMap)m;
                    final VncList fnArgs = VncList.of(map.get(k))
                                                  .addAllAtEnd(supplementalArgs);
                    return map.assoc(k, VncFunction.applyWithMeter(fn, fnArgs, meterRegistry));
                }
                else if (Types.isVncMutableVector(m) || Types.isVncMutableList(m) || Types.isVncJavaList(m)) {
                    final VncSequence seq = ((VncSequence)m);
                    final int idx =  Coerce.toVncLong(args.second()).getValue().intValue();

                    if (idx < 0 || idx > seq.size()) {
                        throw new VncException(String.format(
                                "Function 'update' index %d out of bounds",
                                idx));
                    }
                    else if (idx < seq.size()) {
                        final VncList fnArgs = VncList.of(seq.nth(idx))
                                                      .addAllAtEnd(supplementalArgs);
                        return seq.setAt(idx, VncFunction.applyWithMeter(fn, fnArgs, meterRegistry));
                    }
                    else {
                        final VncList fnArgs = VncList.of(Nil)
                                                      .addAllAtEnd(supplementalArgs);
                        return seq.addAtEnd(VncFunction.applyWithMeter(fn, fnArgs, meterRegistry));
                    }
                }
                else {
                    throw new VncException(String.format(
                            "'update!' does not allow %s as map. It works with mutable maps only.",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    ///////////////////////////////////////////////////////////////////////////
    // Sequence functions
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction split_at =
        new VncFunction(
                "split-at",
                VncFunction
                    .meta()
                    .arglists("(split-at n coll)")
                    .doc("Returns a vector of [(take n coll) (drop n coll)]")
                    .examples(
                        "(split-at 2 [1 2 3 4 5])",
                        "(split-at 3 [1 2])")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                if (args.second() == Nil) {
                    return VncVector.of(VncList.empty(), VncList.empty());
                }

                final VncSequence seq = Coerce.toVncSequence(args.second());
                final int n = Math.min(
                                seq.size(),
                                Math.max(
                                    0,
                                    Coerce.toVncLong(args.first()).getValue().intValue()));

                return VncVector.of(seq.slice(0, n).toVncList(), seq.slice(n).toVncList());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction split_with =
        new VncFunction(
                "split-with",
                VncFunction
                    .meta()
                    .arglists("(split-with pred coll)")
                    .doc("Splits the collection at the first false/nil predicate result in a vector with two lists")
                    .examples(
                        "(split-with odd? [1 3 5 6 7 9])",
                        "(split-with odd? [1 3 5])",
                        "(split-with odd? [2 4 6])")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                if (args.second() == Nil) {
                    return VncVector.of(VncList.empty(), VncList.empty());
                }

                final IVncFunction pred = Coerce.toIVncFunction(args.first());
                final VncSequence coll = Coerce.toVncSequence(args.second());

                pred.sandboxFunctionCallValidation();

                int splitPos = coll.size();

                // find splitPos
                int pos = 0;
                for(VncVal v : coll) {
                    final VncVal match = pred.apply(VncList.of(v));
                    if (VncBoolean.isFalse(match) || match == Nil) {
                        splitPos = pos;
                        break;
                    }
                    pos++;
                }

                if (splitPos == 0) {
                    return VncVector.of(
                            VncList.empty(),
                            coll.toVncList());
                }
                else if (splitPos < coll.size()) {
                    return VncVector.of(
                            coll.slice(0, splitPos).toVncList(),
                            coll.slice(splitPos).toVncList());
                }
                else {
                    return VncVector.of(
                            coll.toVncList(),
                            VncList.empty());
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction into =
        new VncFunction(
                "into",
                VncFunction
                    .meta()
                    .arglists(
                        "(into)",
                        "(into to)",
                        "(into to from)")
                    .doc(
                        "Returns a new coll consisting of to coll with all of the items of " +
                        "from coll conjoined.")
                    .examples(
                        "(into (sorted-map) [ [:a 1] [:c 3] [:b 2] ])",
                        "(into (sorted-map) [ {:a 1} {:c 3} {:b 2} ])",
                        "(into (sorted-map) [(map-entry :b 2) (map-entry :c 3) (map-entry :a 1)])",
                        "(into (sorted-map) {:b 2 :c 3 :a 1})",
                        "(into [] {:a 1, :b 2})",
                        "(into [] '(1 2 3))",
                        "(into '() '(1 2 3))",
                        "(into [1 2 3] '(4 5 6))",
                        "(into '(1 2 3) '(4 5 6))",
                        "(into [] (bytebuf [0 1 2]))",
                        "(into '() (bytebuf [0 1 2]))",
                        "(into [] \"abc\")",
                        "(into '() \"abc\")")
                    .seeAlso("concat", "merge")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0, 1, 2);

                if (args.size() == 0) {
                    return VncList.empty();
                }
                else if (args.size() == 1) {
                    return args.first();
                }

                if (args.second() == Nil) {
                    return args.first();
                }

                final VncCollection to = Coerce.toVncCollection(args.first());

                if (to instanceof VncMutable) {
                    throw new VncException(String.format(
                            "Function 'into' does not allow the mutable collection %s as to-coll. " +
                            "Please use 'into!' instead!",
                            Types.getType(args.first())));
                }

                if (Types.isVncByteBuffer(args.second())) {
                    final VncList byteList = ((VncByteBuffer)args.second()).toVncList();

                    if (Types.isVncSequence(to)) {
                        return ((VncSequence)to).addAllAtEnd(byteList);
                    }
                    else {
                        throw new VncException(String.format(
                                "Function 'into' does only allow list and vector as to-coll if from-coll " +
                                "is a bytebuf"));
                    }
                }
                else if (Types.isVncString(args.second())) {
                    final VncList charList = ((VncString)args.second()).toVncList();

                    if (Types.isVncSequence(to)) {
                        return ((VncSequence)to).addAllAtEnd(charList);
                    }
                    else if (Types.isVncSet(to)) {
                        return ((VncSet)to).addAll(charList);
                    }
                    else {
                        throw new VncException(String.format(
                                "Function 'into' does only allow list, vector, and set as to-coll if from-coll " +
                                "is a string"));
                    }
                }


                final VncCollection from = Coerce.toVncCollection(args.second());

                if (Types.isVncVector(to)) {
                    return ((VncVector)to).addAllAtEnd(from.toVncList());
                }
                else if (Types.isVncList(to)) {
                    // add reversed as defined by Clojure
                    return ((VncList)to).addAllAtStart(from.toVncList(), true);
                }
                else if (Types.isVncSet(to)) {
                    return ((VncSet)to).addAll(from.toVncList());
                }
                else if (Types.isVncMap(to)) {
                    if (Types.isVncSequence(from)) {
                        VncMap toMap = (VncMap)to;
                        for(VncVal it : ((VncSequence)from)) {
                            if (Types.isVncSequence(it)) {
                                toMap = toMap.assoc(((VncSequence)it).toVncList());
                            }
                            else if (Types.isVncMapEntry(it)) {
                                final VncMapEntry entry = (VncMapEntry)it;
                                toMap = toMap.assoc(entry.getKey(), entry.getValue());
                            }
                            else if (Types.isVncMap(it)) {
                                toMap = toMap.putAll((VncMap)it);
                            }
                        }

                        return toMap;
                    }
                    else if (Types.isVncMap(from)) {
                        return ((VncMap)to).putAll((VncMap)from);
                    }
                    else {
                        throw new VncException(String.format(
                                "Function 'into' does not allow %s as from-coll into a map",
                                Types.getType(from)));
                    }
                }
                else {
                    throw new VncException(String.format(
                            "Function 'into' does not allow %s as to-coll",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction into_BANG =
        new VncFunction(
                "into!",
                VncFunction
                    .meta()
                    .arglists(
                        "(into!)",
                        "(into! to)",
                        "(into! to from)")
                    .doc(
                        "Adds all of the items of 'from' conjoined to the mutable 'to' collection")
                    .examples(
                        "(into! (queue) [1 2 3 4])",
                        "(into! (stack) [1 2 3 4])",
                        "(do\n" +
                        "   (into! (. :java.util.concurrent.CopyOnWriteArrayList :new)\n" +
                        "          (doto (. :java.util.ArrayList :new)\n" +
                        "                (. :add 3)\n" +
                        "                (. :add 4))))",
                        "(do\n" +
                        "   (into! (. :java.util.concurrent.CopyOnWriteArrayList :new)\n" +
                        "          '(3 4)))")
                    .seeAlso("concat", "merge")
                    .build()
        ) {
            @Override
            @SuppressWarnings("unchecked")
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0, 1, 2);

                if (args.size() == 0) {
                    return VncList.empty();
                }
                else if (args.size() == 1) {
                    return args.first();
                }

                if (args.second() == Nil) {
                    return args.first();
                }

                final VncCollection to = Coerce.toVncCollection(args.first());


                if (!(to instanceof VncMutable)) {
                    throw new VncException(
                            "Function 'into!' does not allow persistent collections as to-coll. " +
                            "Please use 'into' instead!");
                }

                if (Types.isVncByteBuffer(args.second())) {
                    final VncList byteList = ((VncByteBuffer)args.second()).toVncList();

                    if (Types.isVncSequence(to)) {
                        return ((VncSequence)to).addAllAtEnd(byteList);
                    }
                    else {
                        throw new VncException(String.format(
                                "Function 'into!' does only allow list and vector as to-coll if from-coll " +
                                "is a bytebuf"));
                    }
                }
                else if (Types.isVncString(args.second())) {
                    final VncList charList = ((VncString)args.second()).toVncList();

                    if (Types.isVncSequence(to)) {
                        return ((VncSequence)to).addAllAtEnd(charList);
                    }
                    else if (Types.isVncSet(to)) {
                        return ((VncSet)to).addAll(charList);
                    }
                    else {
                        throw new VncException(String.format(
                                "Function 'into!' does only allow list, vector, and set as to-coll if from-coll " +
                                "is a string"));
                    }
                }

                final VncCollection from = Coerce.toVncCollection(args.second());

                if (Types.isVncMutableList(to)) {
                    // add reversed as defined by Clojure
                    return ((VncMutableList)to).addAllAtStart(from.toVncList(), true);
                }
                else if (Types.isVncMutableVector(to)) {
                    return ((VncMutableVector)to).addAllAtEnd(from.toVncList());
                }
                else if (Types.isVncQueue(to)) {
                    if (Types.isVncSequence(from)) {
                        VncQueue queue = (VncQueue)to;
                        for(VncVal it : ((VncSequence)from)) {
                            queue.put(it);
                        }

                        return queue;
                    }
                    else {
                        throw new VncException(String.format(
                                "Function 'into!' does not allow %s as from-coll into a queue",
                                Types.getType(from)));
                    }
                }
                else if (Types.isVncStack(to)) {
                    if (Types.isVncSequence(from)) {
                        VncStack stack = (VncStack)to;
                        for(VncVal it : ((VncSequence)from)) {
                            stack.push(it);
                        }

                        return stack;
                    }
                    else {
                        throw new VncException(String.format(
                                "Function 'into!' does not allow %s as from-coll into a queue",
                                Types.getType(from)));
                    }
                }
                else if (Types.isVncSet(to)) {
                    return ((VncSet)to).addAll(from.toVncList());
                }
                else if (Types.isVncMap(to)) {
                    if (Types.isVncSequence(from)) {
                        VncMap toMap = (VncMap)to;
                        for(VncVal it : ((VncSequence)from)) {
                            if (Types.isVncSequence(it)) {
                                toMap = toMap.assoc(((VncSequence)it).toVncList());
                            }
                            else if (Types.isVncMapEntry(it)) {
                                final VncMapEntry entry = (VncMapEntry)it;
                                toMap = toMap.assoc(entry.getKey(), entry.getValue());
                            }
                            else if (Types.isVncMap(it)) {
                                toMap = toMap.putAll((VncMap)it);
                            }
                        }

                        return toMap;
                    }
                    else if (Types.isVncMap(from)) {
                        return ((VncMap)to).putAll((VncMap)from);
                    }
                    else {
                        throw new VncException(String.format(
                                "Function 'into!' does not allow %s as from-coll into a map",
                                Types.getType(from)));
                    }
                }
                else if (Types.isVncJavaList(to)) {
                    if (Types.isVncJavaList(from)) {
                        List<Object> to_ = (List<Object>)((VncJavaList)to).getDelegate();
                        List<Object> from_ = (List<Object>)((VncJavaList)from).getDelegate();
                        to_.addAll(from_);
                        return to;
                    }
                    if (Types.isVncJavaSet(from)) {
                        List<Object> to_ = (List<Object>)((VncJavaList)to).getDelegate();
                        Set<Object> from_ = (Set<Object>)((VncJavaSet)from).getDelegate();
                        to_.addAll(from_);
                        return to;
                    }
                    else {
                        return ((VncJavaList)to).addAllAtEnd(from.toVncList());
                    }
                }
                else if (Types.isVncJavaSet(to)) {
                    if (Types.isVncJavaSet(from)) {
                        Set<Object> to_ = (Set<Object>)((VncJavaSet)to).getDelegate();
                        Set<Object> from_ = (Set<Object>)((VncJavaSet)from).getDelegate();
                        to_.addAll(from_);
                        return to;
                    }
                    if (Types.isVncJavaList(from)) {
                        Set<Object> to_ = (Set<Object>)((VncJavaSet)to).getDelegate();
                        List<Object> from_ = (List<Object>)((VncJavaList)to).getDelegate();
                        to_.addAll(from_);
                        return to;
                    }
                    else {
                        return ((VncJavaSet)to).addAll(from.toVncList());
                    }
                }
                else {
                    throw new VncException(String.format(
                            "Function 'into!' does not allow %s as to-coll",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction sequential_Q =
        new VncFunction(
                "sequential?",
                VncFunction
                    .meta()
                    .arglists("(sequential? coll)")
                    .doc("Returns true if coll is a sequential collection")
                    .examples(
                        "(sequential? '(1))",
                        "(sequential? [1])",
                        "(sequential? {:a 1})",
                        "(sequential? nil)",
                        "(sequential? \"abc\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(Types.isVncSequence(args.first()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction coll_Q =
        new VncFunction(
                "coll?",
                VncFunction
                    .meta()
                    .arglists("(coll? coll)")
                    .doc("Returns true if coll is a collection")
                    .examples("(coll? {:a 1})", "(coll? [1 2])")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(Types.isVncCollection(args.first()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction every_Q =
        new VncFunction(
                "every?",
                VncFunction
                    .meta()
                    .arglists("(every? pred coll)")
                    .doc(
                        "Returns true if coll is a collection and the predicate is " +
                        "true for all collection items, false otherwise.")
                    .examples(
                        "(every? number? nil)",
                        "(every? number? [])",
                        "(every? number? [1 2 3 4])",
                        "(every? number? [1 2 3 :a])",
                        "(every? #(>= % 10) [10 11 12])")
                    .seeAlso(
                        "any?",
                        "not-any?",
                        "not-every?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                if (args.second() == Nil) {
                    return False;
                }
                else {
                    final IVncFunction pred = Coerce.toIVncFunction(args.first());
                    final VncCollection coll = Coerce.toVncCollection(args.second());

                    pred.sandboxFunctionCallValidation();

                    if (coll.isEmpty()) {
                        return True;
                    }

                    return VncBoolean.of(
                                coll.toVncList()
                                    .stream()
                                    .allMatch(v -> {
                                       final VncVal r = pred.apply(VncList.of(v));
                                       return r != Nil && !VncBoolean.isFalse(r); }));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction not_every_Q =
        new VncFunction(
                "not-every?",
                VncFunction
                    .meta()
                    .arglists("(not-every? pred coll)")
                    .doc(
	                    "Returns true if coll is a collection and the predicate is " +
	                    "not true for all collection items, false otherwise.")
                    .examples(
                        "(not-every? number? nil)",
                        "(not-every? number? [])",
                        "(not-every? number? [1 2 3 4])",
                        "(not-every? number? [1 2 3 :a])",
                        "(not-every? #(>= % 10) [10 11 12])")
                    .seeAlso(
                        "every?",
                        "any?",
                        "not-any?")
                   .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                if (args.second() == Nil) {
                    return False;
                }
                else {
                    final IVncFunction pred = Coerce.toIVncFunction(args.first());
                    final VncCollection coll = Coerce.toVncCollection(args.second());

                    pred.sandboxFunctionCallValidation();

                    if (coll.isEmpty()) {
                        return True;
                    }

                    return VncBoolean.of(
                                coll.toVncList()
                                    .stream()
                                    .anyMatch(v -> {
                                       final VncVal r = pred.apply(VncList.of(v));
                                       return r == Nil || VncBoolean.isFalse(r); }));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction any_Q =
        new VncFunction(
                "any?",
                VncFunction
                    .meta()
                    .arglists("(any? pred coll)")
                    .doc(
                        "Returns true if the predicate is true for at least one collection item, " +
                        "false otherwise.")
                    .examples(
                        "(any? number? nil)",
                        "(any? number? [])",
                        "(any? number? [1 :a :b])",
                        "(any? number? [1 2 3])",
                        "(any? #(== % 10) [10 20 30])",
                        "(any? #(>= % 10) [1 5 10])")
                    .seeAlso(
                        "every?",
                        "not-any?",
                        "not-every?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                if (args.second() == Nil) {
                    return False;
                }
                else {
                    final IVncFunction pred = Coerce.toIVncFunction(args.first());
                    final VncCollection coll = Coerce.toVncCollection(args.second());

                    pred.sandboxFunctionCallValidation();

                    if (coll.isEmpty()) {
                        return False;
                    }

                    return VncBoolean.of(
                                coll.toVncList()
                                    .stream()
                                    .anyMatch(v -> {
                                       final VncVal r = pred.apply(VncList.of(v));
                                       return r != Nil && !VncBoolean.isFalse(r); }));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction not_any_Q =
        new VncFunction(
                "not-any?",
                VncFunction
                    .meta()
                    .arglists("(not-any? pred coll)")
                    .doc(
                        "Returns false if the predicate is true for at least one collection item, " +
                        "true otherwise")
                    .examples(
                        "(not-any? number? nil)",
                        "(not-any? number? [])",
                        "(not-any? number? [1 :a :b])",
                        "(not-any? number? [1 2 3])",
                        "(not-any? #(>= % 10) [1 5 10])")
                    .seeAlso(
                        "any?",
                        "every?",
                        "not-every?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                return VncBoolean.of(VncBoolean.isFalse(any_Q.apply(args)));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction some =
        new VncFunction(
                "some",
                VncFunction
                    .meta()
                    .arglists("(some pred coll)")
                    .doc(
                        "Returns the first logical true value of (pred x) for any x in coll, " +
                        "else nil. \n\n" +
                        "Stops processing the collection if the first value is found that meets " +
                        "the predicate.")
                    .examples(
                        "(some even? '(1 2 3 4))",
                        "(some even? '(1 3 5 7))",
                        "(some #{5} [1 2 3 4 5])",
                        "(some #(== 5 %) [1 2 3 4 5])",
                        "(some #(if (even? %) %) [1 2 3 4])")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                if (args.second() == Nil) {
                    return Nil;
                }
                else {
                    final IVncFunction pred = Coerce.toIVncFunction(args.first());
                    final VncCollection coll = Coerce.toVncCollection(args.second());

                    pred.sandboxFunctionCallValidation();

                    if (coll.isEmpty()) {
                        return Nil;
                    }

                    Iterable<VncVal> items;
                    if (coll instanceof VncSequence) {
                        items = ((VncSequence)coll);
                    }
                    else if (coll instanceof VncSet) {
                        items = ((VncSet)coll);
                    }
                    else {
                        items = coll.toVncList();
                    }

                    for(VncVal v : items) {
                        final VncVal r = pred.apply(VncList.of(v));
                        if (!VncBoolean.isFalse(r) && r != Nil) {
                            return r;
                        }
                    }

                    return Nil;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction every_pred =
        new VncFunction(
                "every-pred",
                VncFunction
                    .meta()
                    .arglists("(every-pred p1 & p)")
                    .doc(
                        "Takes a set of predicates and returns a function f that returns true " +
                        "if all of its composing predicates return a logical true value against " +
                        "all of its arguments, else it returns false. Note that f is short-circuiting " +
                        "in that it will stop execution on the first argument that triggers a logical " +
                        "false result against the original predicates.")
                    .examples(
                        "((every-pred number?) 1)",
                        "((every-pred number?) 1 2)",
                        "((every-pred number? even?) 2 4 6)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                final List<VncFunction> predicates = new ArrayList<>();
                args.forEach(p -> {
                    final VncFunction fn = Coerce.toVncFunction(p);
                    fn.sandboxFunctionCallValidation();
                    predicates.add(fn);
                });

                return new VncFunction(createAnonymousFuncName("every-pred:wrapped")) {
                    @Override
                    public VncVal apply(final VncList args) {
                        for(VncVal arg : args) {
                            for(VncFunction pred : predicates) {
                                final VncVal res = pred.apply(VncList.of(arg));
                                if (VncBoolean.isFalse(res)) {
                                    return False;
                                }
                                else if (!VncBoolean.isTrue(res)) {
                                    throw new VncException(String.format(
                                            "every-pred: The predicate function %s did not return a boolean value",
                                            pred.getQualifiedName()));
                                }
                            }
                        }

                        return True;
                    }

                    private static final long serialVersionUID = -1L;
                };
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction any_pred =
        new VncFunction(
                "any-pred",
                VncFunction
                    .meta()
                    .arglists("(any-pred p1 & p)")
                    .doc(
                        "Takes a set of predicates and returns a function f that returns the first " +
                        "logical true value returned by one of its composing predicates against any " +
                        "of its arguments, else it returns logical false. Note that f is short-circuiting " +
                        "in that it will stop execution on the first argument that triggers a logical " +
                        "true result against the original predicates.")
                    .examples(
                        "((any-pred number?) 1)",
                        "((any-pred number?) 1 \"a\")",
                        "((any-pred number? string?) 2 \"a\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                final List<VncFunction> predicates = new ArrayList<>();
                args.forEach(p -> {
                    final VncFunction fn = Coerce.toVncFunction(p);
                    fn.sandboxFunctionCallValidation();
                    predicates.add(fn);
                });

                return new VncFunction(createAnonymousFuncName("any-pred:wrapped")) {
                    @Override
                    public VncVal apply(final VncList args) {
                        for(VncVal arg : args) {
                            for(VncFunction pred : predicates) {
                                final VncVal res = pred.apply(VncList.of(arg));
                                if (VncBoolean.isTrue(res)) {
                                    return True;
                                }
                                else if (!VncBoolean.isFalse(res)) {
                                    throw new VncException(String.format(
                                            "any-pred: The predicate function %s did not return a boolean value",
                                            pred.getQualifiedName()));
                                }
                            }
                        }

                        return False;
                    }

                    private static final long serialVersionUID = -1L;
                };
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction count =
        new VncFunction(
                "count",
                VncFunction
                    .meta()
                    .arglists("(count coll)")
                    .doc(
                        "Returns the number of items in the collection. `(count nil)` returns " +
                        "0. Also works on strings, and Java Collections")
                    .examples(
                        "(count {:a 1 :b 2})",
                        "(count [1 2])",
                        "(count \"abc\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal arg = args.first();
                if (arg == Nil) {
                    return new VncLong(0L);
                }
                else if (Types.isVncString(arg)) {
                    return new VncLong(((VncString)arg).getValue().length());
                }
                else if (Types.isVncByteBuffer(arg)) {
                    return new VncLong(((VncByteBuffer)arg).size());
                }
                else if (Types.isVncCollection(arg)) {
                    return new VncLong(((VncCollection)arg).size());
                }
                else {
                    throw new VncException(String.format(
                            "Invalid argument type %s while calling function 'count'",
                            Types.getType(arg)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction empty =
        new VncFunction(
                "empty",
                VncFunction
                    .meta()
                    .arglists("(empty coll)")
                    .doc(
                        "Returns an empty collection of the same category as coll, or nil if coll " +
                        "is nil. If the collection is mutable clears the collection and returns " +
                        "the emptied collection.")
                    .examples("(empty {:a 1})", "(empty [1 2])", "(empty '(1 2))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal coll = args.first();
                if (coll == Nil) {
                    return Nil;
                }
                else if (coll instanceof VncMutable) {
                    ((VncMutable)coll).clear();
                    return coll;
                }
                else if (Types.isVncCollection(coll)) {
                    return ((VncCollection)coll).emptyWithMeta();
                }
                else {
                    throw new VncException(String.format(
                            "Invalid argument type %s while calling function 'empty'",
                            Types.getType(coll)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction empty_Q =
        new VncFunction(
                "empty?",
                VncFunction
                    .meta()
                    .arglists("(empty? x)")
                    .doc("Returns true if x is empty.  Accepts strings, collections and bytebufs.")
                    .examples(
                        "(empty? {})",
                        "(empty? [])",
                        "(empty? '())",
                        "(empty? nil)",
                        "(empty? \"\")")
                    .seeAlso(
                        "not-empty?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal val = args.first();
                if (val == Nil) {
                    return True;
                }
                else if (Types.isVncString(val)) {
                    return VncBoolean.of(((VncString)val).getValue().isEmpty());
                }
                else if (Types.isVncCollection(val)) {
                    return VncBoolean.of(((VncCollection)val).isEmpty());
                }
                else if (Types.isVncByteBuffer(val)) {
                    return VncBoolean.of(((VncByteBuffer)val).size() == 0);
                }
                else {
                    return False;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction not_empty_Q =
        new VncFunction(
                "not-empty?",
                VncFunction
                    .meta()
                    .arglists("(not-empty? x)")
                    .doc("Returns true if x is not empty. Accepts strings, collections and bytebufs.")
                    .examples(
                        "(not-empty? {:a 1})",
                        "(not-empty? [1 2])",
                        "(not-empty? '(1 2))",
                        "(not-empty? \"abc\")",
                        "(not-empty? nil)",
                        "(not-empty? \"\")")
                    .seeAlso(
                        "empty?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal val = args.first();
                if (val == Nil) {
                    return False;
                }
                else if (Types.isVncString(val)) {
                    return VncBoolean.of(!((VncString)val).getValue().isEmpty());
                }
                else if (Types.isVncCollection(val)) {
                    return VncBoolean.of(!((VncCollection)val).isEmpty());
                }
                else if (Types.isVncByteBuffer(val)) {
                    return VncBoolean.of(((VncByteBuffer)val).size() > 0);
                }
                else {
                    return True;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction cons =
        new VncFunction(
                "cons",
                VncFunction
                    .meta()
                    .arglists("(cons x coll)")
                    .doc(
                        "Returns a new collection where x is the first element and coll is " +
                        "the rest. \n\n" +
                        "For ordered collections like list, vectors and ordered sets/maps the value " +
                        "is added at the beginning. For all other collections the position is " +
                        "undefined.")
                    .examples(
                        "(cons 1 '(2 3 4 5 6))",
                        "(cons 1 nil)",
                        "(cons [1 2] [4 5 6])",
                        "(cons 3 (set 1 2))",
                        "(cons {:c 3} {:a 1 :b 2})",
                        "(cons (map-entry :c 3) {:a 1 :b 2})",
                        "; cons a value to a lazy sequence    \n" +
                        "(->> (cons -1 (lazy-seq 0 #(+ % 1))) \n" +
                        "     (take 5)                        \n" +
                        "     (doall))",
                        "; recursive lazy sequence (fibonacci example)    \n" +
                        "(do                                              \n" +
                        "  (defn fib                                      \n" +
                        "    ([]    (fib 1 1))                            \n" +
                        "    ([a b] (cons a (fn [] (fib b (+ a b))))))    \n" +
                        "                                                 \n" +
                        "    (doall (take 6 (fib))))                        ")
                    .seeAlso("conj", "list*", "vector*")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final VncVal x = args.first();
                final VncVal coll = args.second();

                if (coll == Nil) {
                    return VncList.of(x);
                }


                if (coll instanceof VncMutable) {
                    throw new VncException(String.format(
                            "Function 'cons' does not allow the mutable collection %s as coll. " +
                            "Please use 'cons!' instead!",
                            Types.getType(args.first())));
                }

                if (Types.isVncVector(coll)) {
                    return ((VncVector)coll).addAtStart(x);
                }
                else if (Types.isVncList(coll)) {
                    return ((VncList)coll).addAtStart(x);
                }
                else if (Types.isVncLazySeq(coll)) {
                    return VncLazySeq.cons(x, (VncLazySeq)coll, Nil);
                }
                else if (Types.isVncHashSet(coll)) {
                    return ((VncHashSet)coll).add(x);
                }
                else if (Types.isVncSortedSet(coll)) {
                    return ((VncSortedSet)coll).add(x);
                }
                else if (Types.isVncMap(coll)) {
                    if (Types.isVncMapEntry(x)) {
                        final VncMapEntry entry = (VncMapEntry)x;
                        return ((VncMap)coll).assoc(entry.getKey(), entry.getValue());
                    }
                    else if (Types.isVncMap(x)) {
                        return ((VncMap)coll).putAll((VncMap)x);
                    }
                    else {
                        throw new VncException(String.format(
                                "Invalid argument type %s for element while calling function 'cons' on map",
                                Types.getType(x)));
                    }
                }
                else if (Types.isVncFunction(coll)) {
                    // recursive lazy sequence

                    // (do
                    //   (defn fib
                    //     ([]    (fib 1 1))
                    //     ([a b] (cons a (fn [] (fib b (+ a b))))))
                    //
                    //   (pr-str (doall (take 6 (fib)))))   ; -> (1 1 2 3 5 8)

                    final VncFunction fn = (VncFunction)args.second();
                    fn.sandboxFunctionCallValidation();

                    return VncLazySeq.cons(args.first(), fn, Nil);
                }
                else {
                    throw new VncException(String.format(
                            "Invalid argument type %s while calling function 'cons'",
                            Types.getType(coll)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction conj =
        new VncFunction(
                "conj",
                VncFunction
                    .meta()
                    .arglists(
                        "(conj)",
                        "(conj x)",
                        "(conj coll x)",
                        "(conj coll x & xs)")
                    .doc(
                        "Returns a new collection with the x, xs 'added'. `(conj nil item)` " +
                        "returns `(item)` and `(conj item)` returns `item`.\n\n" +
                        "For ordered collections like list, vectors and ordered sets/maps the value " +
                        "is added at the end. For all other collections the position is undefined.")
                    .examples(
                        "(conj [1 2 3] 4)",
                        "(conj [1 2 3] 4 5)",
                        "(conj [1 2 3] [4 5])",
                        "(conj '(1 2 3) 4)",
                        "(conj '(1 2 3) 4 5)",
                        "(conj '(1 2 3) '(4 5))",
                        "(conj (set 1 2 3) 4)",
                        "(conj {:a 1 :b 2} [:c 3])",
                        "(conj {:a 1 :b 2} {:c 3})",
                        "(conj {:a 1 :b 2} (map-entry :c 3))",
                        "(conj)",
                        "(conj 4)")
                    .seeAlso("cons", "into", "concat", "list*", "vector*")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                if (args.isEmpty()) {
                    return VncVector.empty();
                }
                else if (args.size() == 1) {
                    return args.first();
                }
                else {
                    VncVal coll = args.first();
                    if (coll == Nil) {
                        coll = VncList.empty();
                    }

                    if (coll instanceof VncMutable) {
                        throw new VncException(String.format(
                                "Function 'conj' does not allow the mutable collection %s as coll. " +
                                "Please use 'conj!' instead!",
                                Types.getType(args.first())));
                    }

                    if (Types.isVncVector(coll)) {
                        return ((VncVector)coll).addAllAtEnd(args.rest());
                    }
                    else if (Types.isVncList(coll)) {
                        return ((VncList)coll).addAllAtEnd(args.rest());
                    }
                    else if (Types.isVncSet(coll)) {
                        return ((VncSet)coll).addAll(args.rest());
                    }
                    else if (Types.isVncMap(coll)) {
                        VncMap map = (VncMap)coll;
                        for(VncVal v : args.rest()) {
                            if (Types.isVncSequence(v) && ((VncSequence)v).size() == 2) {
                                map = map.assoc(
                                            VncList.of(
                                                ((VncSequence)v).first(),
                                                ((VncSequence)v).second()));
                            }
                            else if (Types.isVncMapEntry(v)) {
                                final VncMapEntry entry = (VncMapEntry)v;
                                map = map.assoc(entry.getKey(), entry.getValue());
                            }
                            else if (Types.isVncMap(v)) {
                                map = map.putAll((VncMap)v);
                            }
                            else {
                                throw new VncException(String.format(
                                        "Invalid x %s while calling function 'conj'",
                                        Types.getType(v)));
                            }
                        }
                        return map;
                    }
                    else {
                        throw new VncException(String.format(
                                "Invalid coll %s while calling function 'conj'",
                                Types.getType(coll)));
                    }
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction cons_BANG =
        new VncFunction(
                "cons!",
                VncFunction
                    .meta()
                    .arglists("(cons! x coll)")
                    .doc(
                        "Adds x to the mutable collection coll. \n\n" +
                        "For mutable ordered collections like lists the value " +
                        "is added at the beginning. For all other mutable collections the " +
                        "position is undefined.")
                    .examples(
                        "(cons! 1 (mutable-list 2 3))",
                        "(cons! 3 (mutable-set 1 2))",
                        "(cons! {:c 3} (mutable-map :a 1 :b 2))",
                        "(cons! (map-entry :c 3) (mutable-map :a 1 :b 2))",
                        "(cons! 1 (stack))",
                        "(cons! 1 (queue))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final VncVal coll = args.second();

                if (!(coll instanceof VncMutable)) {
                    throw new VncException(
                            "Function 'cons!' does not allow persistent collections as coll. " +
                            "Please use 'cons' instead!");
                }

                if (Types.isVncMutableList(coll)) {
                    return ((VncMutableList)coll).addAtStart(args.first());
                }
                else if (Types.isVncMutableSet(coll)) {
                    return ((VncMutableSet)coll).add(args.first());
                }
                else if (Types.isVncMutableMap(coll)) {
                    if (Types.isVncMapEntry(args.first())) {
                        final VncMapEntry entry = (VncMapEntry)args.first();
                        return ((VncMutableMap)coll).assoc(entry.getKey(), entry.getValue());
                    }
                    else if (Types.isVncMap(args.first())) {
                        return ((VncMutableMap)coll).putAll((VncMap)args.first());
                    }
                    else {
                        throw new VncException(String.format(
                                "Invalid argument type %s for element while calling function 'cons' on mutable map",
                                Types.getType(args.first())));
                    }
                }
                else if (Types.isVncQueue(coll)) {
                    final VncQueue queue = (VncQueue)coll;
                    queue.put(args.first());
                    return queue;
                }
                else if (Types.isVncStack(coll)) {
                    final VncStack stack = (VncStack)coll;
                    stack.push(args.first());
                    return stack;
                }
                else {
                    throw new VncException(String.format(
                            "Invalid argument type %s while calling function 'cons!'",
                            Types.getType(coll)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction conj_BANG =
        new VncFunction(
                "conj!",
                VncFunction
                    .meta()
                    .arglists(
                        "(conj!)",
                        "(conj! x)",
                        "(conj! coll x)",
                        "(conj! coll x & xs)")
                    .doc(
                        "Returns a new mutable collection with the x, xs 'added'. " +
                        "`(conj! nil item)` returns `(item)` and `(conj! item)` returns `item`.\n\n" +
                        "For mutable ordered collections like lists the value is added at the end. " +
                        "For all other mutable collections the position is undefined.")
                    .examples(
                        "(conj! (mutable-list 1 2 3) 4)",
                        "(conj! (mutable-list 1 2 3) 4 5)",
                        "(conj! (mutable-list 1 2 3) '(4 5))",
                        "(conj! (mutable-set 1 2 3) 4)",
                        "(conj! (mutable-map :a 1 :b 2) [:c 3])",
                        "(conj! (mutable-map :a 1 :b 2) {:c 3})",
                        "(conj! (mutable-map :a 1 :b 2) (map-entry :c 3))",
                        "(conj! (stack) 1 2 3)",
                        "(conj! (queue) 1 2 3)",
                        "(conj!)",
                        "(conj! 4)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                if (args.isEmpty()) {
                    return new VncMutableList();
                }
                else if (args.size() == 1) {
                	return args.first();
                }
                else {
                    VncVal coll = args.first();
                    if (coll == Nil) {
                        coll = new VncMutableList();
                    }

                    if (!(coll instanceof VncMutable)) {
                        throw new VncException(
                                "Function 'conj!' does not allow persistent collections as coll. " +
                                "Please use 'conj' instead!");
                    }

                    if (Types.isVncMutableList(coll)) {
                        return ((VncMutableList)coll).addAllAtEnd(args.rest());
                    }
                    else if (Types.isVncMutableSet(coll)) {
                        return ((VncMutableSet)coll).addAll(args.rest());
                    }
                    else if (Types.isVncMutableMap(coll)) {
                        VncMutableMap map = (VncMutableMap)coll;
                        for(VncVal v : args.rest()) {
                            if (Types.isVncSequence(v) && ((VncSequence)v).size() == 2) {
                                map = map.assoc(
                                            VncList.of(
                                                ((VncSequence)v).first(),
                                                ((VncSequence)v).second()));
                            }
                            else if (Types.isVncMapEntry(v)) {
                                final VncMapEntry entry = (VncMapEntry)v;
                                map = map.assoc(entry.getKey(), entry.getValue());
                            }
                            else if (Types.isVncMap(v)) {
                                map = map.putAll((VncMap)v);
                            }
                            else {
                                throw new VncException(String.format(
                                        "Invalid x %s while calling function 'conj!'",
                                        Types.getType(v)));
                            }
                        }
                        return map;
                    }
                    else if (Types.isVncQueue(coll)) {
                        VncQueue queue = (VncQueue)coll;
                        for(VncVal it : args.rest()) {
                            queue.put(it);
                        }
                        return queue;
                    }
                    else if (Types.isVncStack(coll)) {
                        VncStack stack = (VncStack)coll;
                        for(VncVal it : args.rest()) {
                            stack.push(it);
                        }
                        return stack;
                    }
                    else {
                        throw new VncException(String.format(
                                "Invalid coll %s while calling function 'conj!'",
                                Types.getType(coll)));
                    }
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction concat =
        new VncFunction(
                "concat",
                VncFunction
                    .meta()
                    .arglists("(concat coll)", "(concat coll & colls)")
                    .doc( "Returns a list of the concatenation of the elements " +
                          "in the supplied collections.")
                    .examples(
                        "(concat [1 2])",
                        "(concat [1 2] [4 5 6])",
                        "(concat '(1 2))",
                        "(concat '(1 2) [4 5 6])",
                        "(concat {:a 1})",
                        "(concat {:a 1} {:b 2 :c 3})",
                        "(concat \"abc\")",
                        "(concat \"abc\" \"def\")")
                    .seeAlso("concatv", "into", "merge")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                VncList result = VncList.empty();

                for(VncVal val : args) {
                    if (val == Nil) {
                        // skip
                    }
                    else if (Types.isVncString(val)) {
                        result = result.addAllAtEnd(((VncString)val).toVncList());
                    }
                    else if (Types.isVncCollection(val)) {
                        result = result.addAllAtEnd(((VncCollection)val).toVncList());
                    }
                    else {
                        throw new VncException(String.format(
                                "Invalid argument type %s while calling function 'concat'",
                                Types.getType(val)));
                    }
                }

                return result;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction concatv =
        new VncFunction(
                "concatv",
                VncFunction
                    .meta()
                    .arglists("(concatv coll)", "(concatv coll & colls)")
                    .doc( "Returns a vector of the concatenation of the elements " +
                          "in the supplied collections.")
                    .examples(
                        "(concatv [1 2])",
                        "(concatv [1 2] [4 5 6])",
                        "(concatv '(1 2))",
                        "(concatv '(1 2) [4 5 6])",
                        "(concatv {:a 1})",
                        "(concatv {:a 1} {:b 2 :c 3})",
                        "(concatv \"abc\")",
                        "(concatv \"abc\" \"def\")")
                    .seeAlso("concat", "into", "merge")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                VncVector result = VncVector.empty();

                for(VncVal val : args) {
                    if (val == Nil) {
                        // skip
                    }
                    else if (Types.isVncString(val)) {
                        result = result.addAllAtEnd(((VncString)val).toVncList());
                    }
                    else if (Types.isVncCollection(val)) {
                        result = result.addAllAtEnd(((VncCollection)val).toVncList());
                    }
                    else {
                        throw new VncException(String.format(
                                "Invalid argument type %s while calling function 'concatv'",
                                Types.getType(val)));
                    }
                }

                return result;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction interleave =
        new VncFunction(
                "interleave",
                VncFunction
                    .meta()
                    .arglists(
                        "(interleave c1 c2)",
                        "(interleave c1 c2 & colls)")
                    .doc(
                        "Returns a collection of the first item in each coll, then the " +
                        "second etc. \n\n" +
                        "Supports lazy sequences as long at least one collection " +
                        "is not a lazy sequence.")
                    .examples(
                        "(interleave [:a :b :c] [1 2])",
                        "(interleave [:a :b :c] (lazy-seq 1 inc))",
                        "(interleave (lazy-seq (constantly :v)) [1 2 3])")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 2);

                final int numColl = args.size();

                final VncSequence[] seqs = new VncSequence[numColl];
                for(int ii=0; ii<numColl; ii++) {
                    seqs[ii] = Coerce.toVncSequence(args.nth(ii));
                }

                final List<VncVal> result = new ArrayList<>();

                final VncVal[] tuple = new VncVal[numColl];
                while(true) {
                    for(int ii=0; ii<numColl; ii++) {
                        if (seqs[ii].isEmpty()) {
                            return VncList.ofList(result);
                        }
                        else {
                            tuple[ii] = seqs[ii].first();
                            seqs[ii] = seqs[ii].rest();
                        }
                    }

                    for(int ii=0; ii<numColl; ii++) result.add(tuple[ii]);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction interpose =
        new VncFunction(
                "interpose",
                VncFunction
                    .meta()
                    .arglists("(interpose sep coll)")
                    .doc("Returns a collection of the elements of coll separated by sep.")
                    .examples("(interpose \", \" [1 2 3])", "(apply str (interpose \", \" [1 2 3]))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final VncVal sep = args.first();
                final VncSequence coll = Coerce.toVncSequence(args.second());

                final List<VncVal> result = new ArrayList<>();

                if (!coll.isEmpty()) {
                    result.add(coll.first());
                    for (VncVal p : coll.rest()) {
                        result.add(sep);
                        result.add(p);
                    }
                }

                return VncList.ofList(result);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction cartesian_product =
        new VncFunction(
                "cartesian-product",
                VncFunction
                    .meta()
                    .arglists("(cartesian-product coll1 coll2 coll*)")
                    .doc(
                        "Returns the cartesian product of two or more collections.\n\n" +
                        "Removes all duplicates items in the collections before computing " +
                        "the cartesian product.")
                    .examples(
                        "(cartesian-product [1 2 3] [1 2 3])",
                        "(cartesian-product [0 1] [0 1] [0 1])")
                    .seeAlso("combinations")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 2);

                VncList result = VncList.empty();

                VncSequence coll = (VncSequence)sort(
                                        "combinations",
                                        Coerce.toVncSequence(args.first()).distinct());
                for(VncVal c : coll) {
                    result = result.addAtEnd(VncList.of(c));
                }

                VncList restColls = args.rest();
                while (!restColls.isEmpty()) {
                    coll = (VncSequence)sort(
                                "combinations",
                                Coerce.toVncSequence(restColls.first()).distinct());
                    restColls = restColls.rest();

                    VncList resultTmp = VncList.empty();

                    for(VncVal tuple : result) {
                        for(VncVal c : coll) {
                            resultTmp = resultTmp.addAtEnd(((VncList)tuple).addAtEnd(c));
                        }
                    }

                    result = resultTmp;
                }

                return result;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction combinations =
        new VncFunction(
                "combinations",
                VncFunction
                    .meta()
                    .arglists("(combinations coll n)")
                    .doc(
                        "All the unique ways of taking n different elements from the items " +
                        "in the collection")
                    .examples(
                        "(combinations [0 1 2 3] 1)",
                        "(combinations [0 1 2 3] 2)",
                        "(combinations [0 1 2 3] 3)",
                        "(combinations [0 1 2 3] 4)")
                    .seeAlso("cartesian-product")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                VncSequence coll = (VncSequence)sort(
                                                "combinations",
                                                Coerce.toVncSequence(args.first()).distinct());
                final int n = Coerce.toVncLong(args.second()).getIntValue();

                if (n == 1) {
                    VncList result = VncList.empty();
                    while(!coll.isEmpty()) {
                        result = result.addAtEnd(coll.slice(0, 1));
                        coll = coll.drop(1);
                    }
                    return result;
                }
                else if (n > 1 && n < coll.size()) {
                    VncList result = VncList.empty();

                    while(!coll.isEmpty()) {
                        VncSequence head = coll.take(n-1);
                        VncSequence rest = coll.drop(n-1);

                        for(VncVal v : rest) {
                            result = result.addAtEnd(head.addAtEnd(v));
                        }

                        coll = coll.drop(1);
                    }

                    return result;
                }
                else if (n == coll.size()) {
                    return VncList.of(coll);
                }
                else {
                    // no combinations possible
                    return VncList.empty();
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction first =
        new VncFunction(
                "first",
                VncFunction
                    .meta()
                    .arglists("(first coll)")
                    .doc(
                        "Returns the first element of coll or nil if coll is nil or empty.")
                    .examples(
                        "(first nil)",
                        "(first [])",
                        "(first [1 2 3])",
                        "(first '())",
                        "(first '(1 2 3))",
                        "(first \"abc\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0, 1);

                final VncVal coll = args.first();
                if (coll == Nil) {
                    return Nil;
                }
                else if (Types.isVncSequence(coll)) {
                    return ((VncSequence)coll).first();
                }
                else if (Types.isVncString(coll)) {
                    return ((VncString)coll).first();
                }
                else {
                    throw new VncException(String.format(
                            "Invalid argument type %s while calling function 'first'",
                            Types.getType(coll)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction second =
        new VncFunction(
                "second",
                VncFunction
                    .meta()
                    .arglists("(second coll)")
                    .doc("Returns the second element of coll.")
                    .examples(
                        "(second nil)",
                        "(second [])",
                        "(second [1 2 3])",
                        "(second '())",
                        "(second '(1 2 3))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);


                final VncVal coll = args.first();
                if (coll == Nil) {
                    return Nil;
                }

                if (Types.isVncSequence(coll)) {
                    return ((VncSequence)coll).second();
                }
                else if (Types.isVncString(coll)) {
                    return ((VncString)coll).second();
                }
                else {
                    throw new VncException(String.format(
                            "Invalid argument type %s while calling function 'second'",
                            Types.getType(coll)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction third =
        new VncFunction(
                "third",
                VncFunction
                    .meta()
                    .arglists("(third coll)")
                    .doc("Returns the third element of coll.")
                    .examples(
                        "(third nil)",
                        "(third [])",
                        "(third [1 2 3])",
                        "(third '())",
                        "(third '(1 2 3))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);


                final VncVal coll = args.first();
                if (coll == Nil) {
                    return Nil;
                }

                if (Types.isVncSequence(coll)) {
                    return ((VncSequence)coll).third();
                }
                else if (Types.isVncString(coll)) {
                    return ((VncString)coll).nth(2);
                }
                else {
                    throw new VncException(String.format(
                            "Invalid argument type %s while calling function 'third'",
                            Types.getType(coll)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction fourth =
        new VncFunction(
                "fourth",
                VncFunction
                    .meta()
                    .arglists("(fourth coll)")
                    .doc("Returns the fourth element of coll.")
                    .examples(
                        "(fourth nil)",
                        "(fourth [])",
                        "(fourth [1 2 3 4 5])",
                        "(fourth '())",
                        "(fourth '(1 2 3 4 5))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);


                final VncVal coll = args.first();
                if (coll == Nil) {
                    return Nil;
                }

                if (Types.isVncSequence(coll)) {
                    return ((VncSequence)coll).fourth();
                }
                else if (Types.isVncString(coll)) {
                    return ((VncString)coll).nth(3);
                }
                else {
                    throw new VncException(String.format(
                            "Invalid argument type %s while calling function 'fourth'",
                            Types.getType(coll)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction nth =
        new VncFunction(
                "nth",
                VncFunction
                    .meta()
                    .arglists(
                    	"(nth coll idx)",
                    	"(nth coll idx defaultVal)")
                    .doc(
                    	"Returns the nth element of coll. \n\n" +
                    	"Throws an exception if the index does not exist and there " +
                    	"is no default value passed else returns the default value.")
                    .examples(
                        "(nth nil 1)",
                        "(nth [1 2 3] 1)",
                        "(nth '(1 2 3) 1)",
                        "(nth \"abc\" 2)",
                        "(nth nil 1 9)",
                        "(nth [1 2 3] 6 9)",
                        "(nth '(1 2 3) 6 9)",
                        "(nth \"abc\" 6 9)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2, 3);

                final int idx = Coerce.toVncLong(args.second()).getValue().intValue();

                final VncVal coll = args.first();
                if (coll == Nil) {
                    return Nil;
                }

                if (args.size() == 2) {
	                if (Types.isVncSequence(coll)) {
	                    return ((VncSequence)coll).nth(idx);
	                }
	                else if (Types.isVncString(coll)) {
	                    return ((VncString)coll).nth(idx);
	                }
	                else {
	                    throw new VncException(String.format(
	                            "Invalid argument type %s while calling function 'nth'",
	                            Types.getType(coll)));
	                }
                }
                else {
	                if (Types.isVncSequence(coll)) {
	                    return ((VncSequence)coll).nthOrDefault(idx, args.third());
	                }
	                else if (Types.isVncString(coll)) {
	                    return ((VncString)coll).nthOrDefault(idx, args.third());
	                }
	                else {
	                    throw new VncException(String.format(
	                            "Invalid argument type %s while calling function 'nth'",
	                            Types.getType(coll)));
	                }
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction last =
        new VncFunction(
                "last",
                VncFunction
                    .meta()
                    .arglists("(last coll)")
                    .doc("Returns the last element of coll.")
                    .examples(
                        "(last nil)",
                        "(last [])",
                        "(last [1 2 3])",
                        "(last '())",
                        "(last '(1 2 3))",
                        "(last \"abc\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal val = args.first();
                if (val == Nil) {
                    return Nil;
                }

                if (Types.isVncSequence(val)) {
                    return ((VncSequence)val).last();
                }
                else if (Types.isVncString(val)) {
                    return ((VncString)val).last();
                }
                else {
                    throw new VncException(String.format(
                            "Invalid argument type %s while calling function 'last'",
                            Types.getType(val)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction rest =
        new VncFunction(
                "rest",
                VncFunction
                    .meta()
                    .arglists("(rest coll)")
                    .doc("Returns a possibly empty collection of the items after the first.")
                    .examples(
                        "(rest nil)",
                        "(rest [])",
                        "(rest [1])",
                        "(rest [1 2 3])",
                        "(rest '())",
                        "(rest '(1))",
                        "(rest '(1 2 3))",
                        "(rest \"1234\")")
                    .seeAlso("str/rest")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal coll = args.first();
                if (coll == Nil) {
                    return Nil;
                }
                else if (Types.isVncSequence(coll)) {
                    return ((VncSequence)coll).rest();
                }
                else if (Types.isVncString(coll)) {
                    final String s = ((VncString)coll).getValue();
                    if (s.length() > 1) {
                        final List<VncVal> lst = new ArrayList<VncVal>();
                        for (char c : s.toCharArray()) {
                            lst.add(new VncChar(c));
                        }
                        return VncList.ofList(lst).rest();
                    }
                    else {
                        return VncList.empty();
                    }
                }
                else {
                    throw new VncException(String.format(
                            "Invalid argument type %s while calling function 'rest'",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction butlast =
        new VncFunction(
                "butlast",
                VncFunction
                    .meta()
                    .arglists("(butlast coll)")
                    .doc("Returns a collection with all but the last list element")
                    .examples(
                        "(butlast nil)",
                        "(butlast [])",
                        "(butlast [1])",
                        "(butlast [1 2 3])",
                        "(butlast '())",
                        "(butlast '(1))",
                        "(butlast '(1 2 3))",
                        "(butlast \"1234\")")
                    .seeAlso("str/butlast")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal coll = args.first();
                if (coll == Nil) {
                    return Nil;
                }
                else if (Types.isVncSequence(coll)) {
                    return ((VncSequence)coll).dropRight(1);
                }
                else if (Types.isVncString(coll)) {
                    final String s = ((VncString)coll).getValue();
                    if (s.length() > 1) {
                        final List<VncVal> lst = new ArrayList<VncVal>();
                        for (char c : s.toCharArray()) {
                            lst.add(new VncChar(c));
                        }
                        return VncList.ofList(lst).slice(0, s.length()-1);
                    }
                    else {
                        return VncList.empty();
                    }
                }
                else {
                    throw new VncException(String.format(
                            "Invalid argument type %s while calling function 'butlast'",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction nfirst =
        new VncFunction(
                "nfirst",
                VncFunction
                    .meta()
                    .arglists("(nfirst coll n)")
                    .doc("Returns a collection of the first n items")
                    .examples(
                        "(nfirst nil 2)",
                        "(nfirst [] 2)",
                        "(nfirst [1] 2)",
                        "(nfirst [1 2 3] 2)",
                        "(nfirst '() 2)",
                        "(nfirst '(1) 2)",
                        "(nfirst '(1 2 3) 2)",
                        "(nfirst \"abcdef\" 2)",
                        "(nfirst (lazy-seq 1 #(+ % 1)) 4)")
                    .seeAlso("str/nfirst")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final VncVal coll = args.first();
                int n = Coerce.toVncLong(args.second()).getValue().intValue();

                if (coll == Nil) {
                    return VncList.empty();
                }
                else if (Types.isVncVector(coll)) {
                    final VncVector vec = (VncVector)coll;
                    n = Math.max(0, Math.min(vec.size(), n));
                    return vec.slice(0, n);
                }
                else if (Types.isVncList(coll) || Types.isVncJavaList(coll)) {
                    final VncSequence list = (VncSequence)args.first();
                    n = Math.max(0, Math.min(list.size(), n));
                    return list.slice(0, n);
                }
                else if (Types.isVncLazySeq(coll)) {
                    final VncLazySeq list = (VncLazySeq)args.first();
                    return list.slice(0, n);
                }
                else if (Types.isVncMutableList(coll)) {
                    final VncMutableList list = (VncMutableList)args.first();
                    n = Math.max(0, Math.min(list.size(), n));
                    return list.slice(0, n);
                }
                else if (Types.isVncString(coll)) {
                    final VncSequence list = ((VncString)coll).toVncList();
                    n = Math.max(0, Math.min(list.size(), n));
                    return list.slice(0, n);
                }
                else {
                    throw new VncException(String.format(
                            "nfirst: type %s not supported",
                            Types.getType(coll)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction nlast =
        new VncFunction(
                "nlast",
                VncFunction
                    .meta()
                    .arglists("(nlast coll n)")
                    .doc("Returns a collection of the last n items")
                    .examples(
                        "(nlast nil 2)",
                        "(nlast [] 2)",
                        "(nlast [1] 2)",
                        "(nlast [1 2 3] 2)",
                        "(nlast '() 2)",
                        "(nlast '(1) 2)",
                        "(nlast '(1 2 3) 2)",
                        "(nlast \"abcdef\" 2)")
                    .seeAlso("str/nlast")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final VncVal coll = args.first();
                int n = Coerce.toVncLong(args.second()).getValue().intValue();

                if (args.first() == Nil) {
                    return VncList.empty();
                }
                else if (Types.isVncVector(coll)) {
                    final VncVector vec = (VncVector)coll;
                    n = Math.max(0, Math.min(vec.size(), n));
                    return vec.slice(vec.size()-n);
                }
                else if (Types.isVncList(coll) || Types.isVncJavaList(coll)) {
                    final VncSequence list = (VncSequence)args.first();
                    n = Math.max(0, Math.min(list.size(),n));
                    return list.slice(list.size()-n);
                }
                else if (Types.isVncMutableList(coll)) {
                    final VncMutableList list = (VncMutableList)args.first();
                    n = Math.max(0, Math.min(list.size(),n));
                    return list.slice(list.size()-n);
                }
                else if (Types.isVncString(coll)) {
                    final VncSequence list = ((VncString)coll).toVncList();
                    n = Math.max(0, Math.min(list.size(),n));
                    return list.slice(list.size()-n);
                }
                else {
                    throw new VncException(String.format(
                            "nlast: type %s not supported",
                            Types.getType(coll)));
                }
            }

        private static final long serialVersionUID = -1848883965231344442L;
    };

    public static VncFunction partition =
        new VncFunction(
                "partition",
                VncFunction
                    .meta()
                    .arglists("(partition n coll)", "(partition n step coll)", "(partition n step padcoll coll)")
                    .doc(
                        "Returns a collection of lists of n items each, at offsets step " +
                        "apart. If step is not supplied, defaults to n, i.e. the partitions " +
                        "do not overlap. If a padcoll collection is supplied, use its elements as " +
                        "necessary to complete last partition upto n items. In case there are " +
                        "not enough padding elements, return a partition with less than n items. " +
                        "padcoll may be a lazy sequence")
                    .examples(
                        "(partition 3 [0 1 2 3 4 5 6])",
                        "(partition 3 3 (repeat 99) [0 1 2 3 4 5 6])",
                        "(partition 3 3 [] [0 1 2 3 4 5 6])",
                        "(partition 2 3 [0 1 2 3 4 5 6])",
                        "(partition 3 1 [0 1 2 3 4 5 6])",
                        "(partition 3 6 [\"a\"] (range 20))",
                        "(partition 4 6 [\"a\" \"b\" \"c\" \"d\"] (range 20))")
                    .seeAlso("partition-all", "partition-by")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2, 3, 4);

                final int n = Coerce.toVncLong(args.first()).getValue().intValue();
                final int step = args.size() > 2 ? Coerce.toVncLong(args.second()).getValue().intValue() : n;
                final VncSequence padseq = args.size() == 4
                                            ? args.third() == Nil
                                                    ? VncList.empty()
                                                    : Coerce.toVncSequence(args.third())
                                            : null;
                VncSequence seq = args.last() == Nil ? VncList.empty() : Coerce.toVncSequence(args.last());

                if (n <= 0) {
                    throw new VncException("partition: n must be greater than 0");
                }
                if (step <= 0) {
                    throw new VncException("partition: step must be greater than 0");
                }

                VncList result = VncList.empty();

                while (!seq.isEmpty()) {
                    VncSequence part = seq.take(n);
                    if (Types.isVncLazySeq(part)) {
                        part = ((VncLazySeq)part).realize();
                    }

                    if (padseq != null) {
                        final VncSequence partPadded = part.addAllAtEnd(padseq.take(n-part.size()));
                        result = result.addAtEnd(partPadded);
                    }
                    else {
                        if (part.size() == n) {
                            result = result.addAtEnd(part);
                        }
                    }

                    if (part.size() < n) {
                        break;
                    }

                    seq = seq.drop(step);
                }

                return result;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction partition_all =
        new VncFunction(
                "partition-all",
                VncFunction
                    .meta()
                    .arglists("(partition-all n coll)", "(partition-all n step coll)")
                    .doc(
                        "Returns a collection of lists of n items each, at offsets step " +
                        "apart. If step is not supplied, defaults to n, i.e. the partitions " +
                        "do not overlap. May include partitions with fewer than n items " +
                        "at the end.")
                    .examples(
                        "(partition-all 3 [0 1 2 3 4 5 6])",
                        "(partition-all 2 3 [0 1 2 3 4 5 6])",
                        "(partition-all 3 1 [0 1 2 3 4 5 6])",
                        "(partition-all 3 6 [\"a\"])",
                        "(partition-all 2 2 [\"a\" \"b\" \"c\" \"d\"])")
                    .seeAlso("partition", "partition-by")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2, 3);

                final int n = Coerce.toVncLong(args.first()).getValue().intValue();
                final int step = args.size() > 2 ? Coerce.toVncLong(args.second()).getValue().intValue() : n;
                VncSequence seq = args.last() == Nil ? VncList.empty() : Coerce.toVncSequence(args.last());

                if (n <= 0) {
                    throw new VncException("partition-all: n must be greater than 0");
                }
                if (step <= 0) {
                    throw new VncException("partition-all: step must be greater than 0");
                }

                VncList result = VncList.empty();

                while (!seq.isEmpty()) {
                    VncSequence part = seq.take(n);
                    if (Types.isVncLazySeq(part)) {
                        part = ((VncLazySeq)part).realize();
                    }

                    result = result.addAtEnd(part);

                    if (part.size() < n) {
                        break;
                    }

                    seq = seq.drop(step);
                }

                return result;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction partition_by =
        new VncFunction(
                "partition-by",
                VncFunction
                    .meta()
                    .arglists("(partition-by f coll)")
                    .doc(
                        "Applies f to each value in coll, splitting it each time f returns " +
                        "a new value.")
                    .examples(
                        "(partition-by even? [1 2 4 3 5 6])",
                        "(partition-by identity (seq \"ABBA\"))",
                        "(partition-by identity [1 1 1 1 2 2 3])")
                    .seeAlso("partition", "partition-all")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final MeterRegistry meterRegistry = ThreadContext.getMeterRegistry();

                final IVncFunction f = Coerce.toIVncFunction(args.first());
                VncSequence seq = Coerce.toVncSequence(args.second());

                f.sandboxFunctionCallValidation();

                if (seq.isEmpty()) {
                    return VncList.empty();
                }

                VncList result = VncList.empty();
                VncList part = VncList.empty();

                // first element
                VncVal v = seq.first();
                seq = seq.rest();
                part = part.addAtEnd(v);

                VncVal splitValLast = VncFunction.applyWithMeter(
                                            f,
                                            VncList.of(v),
                                            meterRegistry);

                while (!seq.isEmpty()) {
                    v = seq.first();
                    seq = seq.rest();

                    VncVal splitVal = VncFunction.applyWithMeter(
                                            f,
                                            VncList.of(v),
                                            meterRegistry);

                    VncBoolean equal = (VncBoolean)VncFunction.applyWithMeter(
                                            CoreFunctions.equal_Q,
                                            VncList.of(splitValLast, splitVal),
                                            meterRegistry);

                    if (!VncBoolean.isTrue(equal)) {
                        splitValLast = splitVal;
                        result = result.addAtEnd(part);
                        part = VncList.empty();
                    }
                    part = part.addAtEnd(v);
                }

                if (!part.isEmpty()) {
                    result = result.addAtEnd(part);
                }

                return result;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction emptyToNil =
        new VncFunction(
                "empty-to-nil",
                VncFunction
                    .meta()
                    .arglists("(empty-to-nil x)")
                    .doc("Returns nil if x is empty")
                    .examples(
                        "(empty-to-nil \"\")",
                        "(empty-to-nil [])",
                        "(empty-to-nil '())",
                        "(empty-to-nil {})")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal arg = args.first();
                if (Types.isVncString(arg)) {
                    return ((VncString)arg).getValue().isEmpty() ? Nil : arg;
                }
                else if (Types.isVncSequence(arg)) {
                    return ((VncSequence)arg).isEmpty() ? Nil : arg;
                }
                else if (Types.isVncSet(arg)) {
                    return ((VncSet)arg).isEmpty() ? Nil : arg;
                }
                else if (Types.isVncMap(arg)) {
                    return ((VncMap)arg).isEmpty() ? Nil : arg;
                }
                else {
                    return arg;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction instance_of_Q =
            new VncFunction(
                    "instance-of?",
                    VncFunction
                        .meta()
                        .arglists("(instance-of? type x)")
                        .doc("Returns true if x is an instance of the given type")
                        .examples(
                            "(instance-of? :long 500)",
                            "(instance-of? :java.math.BigInteger 500)")
                        .seeAlso("type", "supertype", "supertypes")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertArity(this, args, 2);

                    final VncKeyword type = Coerce.toVncKeyword(args.first());
                    if (type.hasNamespace()) {
                        // Qualified Venice or Java type
                        return VncBoolean.of(Types.isInstanceOf(type, args.second()));
                    }
                    else {
                        // Unqualified type

                        // Try Venice core type first: long -> :core/long
                        final VncKeyword qualifiedType = type.withNamespace(Namespaces.NS_CORE);
                        if (Types.isInstanceOf(qualifiedType, args.second())) {
                            return VncBoolean.True;
                        }

                        // Try unqualified Java type:  :ArrayList
                        return VncBoolean.of(Types.isInstanceOf(type, args.second()));
                    }
                }

                private static final long serialVersionUID = -1848883965231344442L;
            };

    public static VncFunction pop =
        new VncFunction(
                "pop",
                VncFunction
                    .meta()
                    .arglists("(pop coll)")
                    .doc(
                        "For a list, returns a new list without the first item, " +
                        "for a vector, returns a new vector without the last item.")
                    .examples(
                        "(pop '(1 2 3 4))",
                        "(pop [1 2 3 4])")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal val = args.first();
                if (val == Nil) {
                    return Nil;
                }

                if (Types.isVncVector(val)) {
                    final VncVector vec = (VncVector)val;
                    return vec.size() < 2 ? VncVector.empty() : vec.slice(0, vec.size()-1);
                }
                else if (Types.isVncSequence(val)) {
                    final VncSequence seq = (VncSequence)val;
                    return seq.isEmpty() ? VncList.empty() : seq.rest();
                }
                else {
                    throw new VncException(String.format(
                            "pop: type %s not supported",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction pop_BANG =
        new VncFunction(
                "pop!",
                VncFunction
                    .meta()
                    .arglists("(pop! stack)")
                    .doc("Pops an item from a stack.")
                    .examples(
                        "(let [s (stack)]   \n" +
                        "   (push! s 1)     \n" +
                        "   (push! s 2)     \n" +
                        "   (push! s 3)     \n" +
                        "   (pop! s))")
                    .seeAlso(
                        "stack", "peek", "push!", "empty?", "count")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal val = args.first();
                if (val == Nil) {
                    return Nil;
                }

                if (Types.isVncStack(val)) {
                    return ((VncStack)val).pop();
                }
                else {
                    throw new VncException(String.format(
                            "pop!: type %s not supported",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction push_BANG =
        new VncFunction(
                "push!",
                VncFunction
                    .meta()
                    .arglists("(push! stack v)")
                    .doc("Pushes an item to a stack.")
                    .examples(
                        "(let [s (stack)]   \n" +
                        "   (push! s 1)     \n" +
                        "   (push! s 2)     \n" +
                        "   (push! s 3)     \n" +
                        "   (pop! s))")
                    .seeAlso(
                        "stack", "peek", "pop!", "empty?", "count")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final VncVal val = args.first();
                if (val == Nil) {
                    return Nil;
                }

                if (Types.isVncStack(val)) {
                    return ((VncStack)val).push(args.second());
                }
                else {
                    throw new VncException(String.format(
                            "push!: type %s not supported",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction offer_BANG =
        new VncFunction(
                "offer!",
                VncFunction
                    .meta()
                    .arglists(
                        "(offer! queue v)",
                        "(offer! queue timeout v)")
                    .doc(
                        "Offers an item to a queue with an optional timeout in milliseconds. " +
                        "If a timeout is given waits up to the specified wait time if necessary " +
                        "for space to become available. For an indefinite timeout pass the timeout " +
                        "value :indefinite. " +
                        "If no timeout is given returns immediately false if the queue does not " +
                        "have any more capacity. " +
                        "Returns true if the element was added to this queue, else false.")
                    .examples(
                        "(let [q (queue)]           \n" +
                        "  (offer! q 1)             \n" +
                        "  (offer! q 1000 2)        \n" +
                        "  (offer! q :indefinite 3) \n" +
                        "  (offer! q 3)             \n" +
                        "  (poll! q)                \n" +
                        "  q)")
                    .seeAlso("queue", "put!", "take!", "poll!", "peek", "empty?", "count")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2, 3);

                final VncVal val = args.first();
                if (val == Nil) {
                    return Nil;
                }

                if (Types.isVncQueue(val)) {
                    if (args.size() == 2) {
                        return ((VncQueue)val).offer(args.second());
                    }
                    else {
                        final VncVal option = args.second();
                        if (Types.isVncKeyword(option)) {
                            if (((VncKeyword)option).hasValue("indefinite")) {
                                ((VncQueue)val).put(args.third());
                                return VncBoolean.True;
                            }
                            else {
                                throw new VncException(String.format(
                                        "offer!: timeout value '%s' not supported",
                                        option.toString()));
                            }
                        }
                        else {
                            final long timeout = Coerce.toVncLong(option).getValue();
                            return ((VncQueue)val).offer(args.third(), timeout);
                        }
                    }
                }
                else {
                    throw new VncException(String.format(
                            "offer!: type %s not supported",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction put_BANG =
        new VncFunction(
                "put!",
                VncFunction
                    .meta()
                    .arglists(
                        "(put! queue val)",
                        "(put! queue val delay)")
                    .doc(
                        "Puts an item to a queue. The operation is synchronous, it waits indefinitely " +
                        "until the value can be placed on the queue. Returns always nil.\n\n" +
                        "*queue:* `(put! queue val)`¶\n" +
                        "Puts the value 'val' to the tail of the queue.\n\n" +
                        "*delay-queue:* `(put! queue val delay)`¶\n" +
                        "Puts the value 'val' with a delay of 'delay' milliseconds to a delay-queue")
                    .examples(
                        "(let [q (queue)]   \n" +
                        "  (put! q 1)       \n" +
                        "  (poll! q)        \n" +
                        "  q)",
                        "(let [q (delay-queue)]   \n" +
                        "  (put! q 1 100)         \n" +
                        "  (take! q))             ")
                    .seeAlso("queue", "take!", "offer!", "poll!", "peek", "empty?", "count")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2, 3);

                final VncVal coll = args.first();
                if (coll == Nil) {
                    return Nil;
                }

                if (Types.isVncQueue(coll)) {
                    if (args.size() == 2) {
                        ((VncQueue)coll).put(args.second());
                        return Nil;
                    }
                    else {
                        throw new VncException("put! for a queue requires two args (put! queue val)");
                    }
                }
                else if (Types.isVncDelayQueue(coll)) {
                    if (args.size() == 3) {
                        final VncVal val = args.second();
                        if (val == Nil) {
                            throw new VncException("put! A delay-queue does not permit nil elements");
                        }
                        else {
                            ((VncDelayQueue)coll).put(
                                    val,
                                    Coerce.toVncLong(args.third()).getValue(),
                                    TimeUnit.MILLISECONDS);
                            return Nil;
                        }
                    }
                    else {
                        throw new VncException(
                                "put! for a delay-queue requires three args (put! queue val delay)");
                    }
                }
                else {
                    throw new VncException(String.format(
                            "put!: type %s not supported",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction poll_BANG =
        new VncFunction(
                "poll!",
                VncFunction
                    .meta()
                    .arglists(
                        "(poll! queue)",
                        "(poll! queue timeout)")
                    .doc(
                        "Polls an item from a queue with an optional timeout in milliseconds. " +
                        "For an indefinite timeout pass the timeout value :indefinite. " +
                        "If no timeout is given returns the item if one is available else " +
                        "returns nil. With a timeout returns the item if one is available within " +
                        "the given timeout else returns nil.")
                    .examples(
                        "(let [q (conj! (queue) 1 2 3 4)]   \n" +
                        "  (poll! q)                        \n" +
                        "  (poll! q 1000)                   \n" +
                       "  q)")
                    .seeAlso("queue", "put!", "take!", "offer!", "peek", "empty?", "count")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                final VncVal val = args.first();
                if (val == Nil) {
                    return Nil;
                }

                if (Types.isVncQueue(val)) {
                    if (args.size() == 1) {
                        return ((VncQueue)val).poll();
                    }
                    else {
                        final VncVal option = args.second();
                        if (Types.isVncKeyword(option)) {
                            if (((VncKeyword)option).hasValue("indefinite")) {
                                return ((VncQueue)val).take();
                            }
                            else {
                                throw new VncException(String.format(
                                        "poll!: timeout value '%s' not supported",
                                        option.toString()));
                            }
                        }
                        else {
                            final long timeout = Coerce.toVncLong(option).getValue();
                            return ((VncQueue)val).poll(timeout);
                        }
                    }
                }
                else if (Types.isVncDelayQueue(val)) {
                    if (args.size() == 1) {
                        return ((VncDelayQueue)val).poll();
                    }
                    else {
                        final VncVal option = args.second();
                        if (Types.isVncKeyword(option)) {
                            if (((VncKeyword)option).hasValue("indefinite")) {
                                return ((VncDelayQueue)val).take();
                            }
                            else {
                                throw new VncException(String.format(
                                        "poll!: timeout value '%s' not supported",
                                        option.toString()));
                            }
                        }
                        else {
                            final long timeout = Coerce.toVncLong(option).getValue();
                            return ((VncDelayQueue)val).poll(timeout);
                        }
                    }
                }
                else {
                    throw new VncException(String.format(
                            "poll!: type %s not supported",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction take_BANG =
        new VncFunction(
                "take!",
                VncFunction
                    .meta()
                    .arglists(
                        "(take! queue)")
                    .doc(
                        "Retrieves and removes the head value of the queue, waiting if " +
                        "necessary until a value becomes available.")
                    .examples(
                        "(let [q (queue)]   \n" +
                        "  (put! q 1)       \n" +
                        "  (take! q)        \n" +
                        "  q)")
                    .seeAlso("queue", "put!", "offer!", "poll!", "peek", "empty?", "count")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal queue = args.first();
                if (queue == Nil) {
                    return Nil;
                }

                if (Types.isVncQueue(queue)) {
                   return ((VncQueue)queue).take();
                }
                if (Types.isVncDelayQueue(queue)) {
                    return ((VncDelayQueue)queue).take();
                 }
               else {
                    throw new VncException(String.format(
                            "take!: type %s not supported",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction peek =
        new VncFunction(
                "peek",
                VncFunction
                    .meta()
                    .arglists("(peek coll)")
                    .doc(
                        "For a list, same as first, for a vector, same as last, " +
                        "for a stack the top element (or nil if the stack is empty), " +
                        "for a queue the head element (or nil if the queue is empty).")
                    .examples(
                        "(peek '(1 2 3 4))",
                        "(peek [1 2 3 4])",
                        "(let [s (conj! (stack) 1 2 3 4)] \n" +
                        "   (peek s))                      ",
                        "(let [q (conj! (queue) 1 2 3 4)] \n" +
                        "   (peek q))                      ")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal val = args.first();
                if (val == Nil) {
                    return Nil;
                }

                if (Types.isVncVector(val)) {
                    final VncVector vec = (VncVector)val;
                    return vec.isEmpty() ? Nil : vec.nth(vec.size()-1);
                }
                else if (Types.isVncSequence(val)) {
                    final VncSequence seq = (VncSequence)val;
                    return seq.isEmpty() ? Nil : seq.first();
                }
                else if (Types.isVncStack(val)) {
                    return ((VncStack)val).peek();
                }
                else if (Types.isVncQueue(val)) {
                    return ((VncQueue)val).peek();
                }
                else if (Types.isVncDelayQueue(val)) {
                    return ((VncDelayQueue)val).peek();
                }
                else {
                    throw new VncException(String.format(
                            "peek: type %s not supported",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction sort =
        new VncFunction(
                "sort",
                VncFunction
                    .meta()
                    .arglists("(sort coll)", "(sort comparefn coll)")
                    .doc(
                        "Returns a sorted list of the items in coll. If no compare function " +
                        "comparefn is supplied, uses the natural compare. The compare function " +
                        "takes two arguments and returns -1, 0, or 1")
                    .examples(
                        "(sort [3 2 5 4 1 6])",
                        "(sort compare [3 2 5 4 1 6])",
                        "; reversed\n" +
                        "(sort (comp - compare) [3 2 5 4 1 6])",
                        "(sort {:c 3 :a 1 :b 2})")
                    .seeAlso("sort-by")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                final IVncFunction compfn = args.size() == 1
                                            ? compare // -> sort by natural order
                                            : Coerce.toIVncFunction(args.first());

                compfn.sandboxFunctionCallValidation();

                final VncVal coll = args.last();

                return sort(
                        "sort",
                        coll,
                        (x,y) -> Coerce
                                    .toVncLong(compfn.apply(VncList.of(x,y)))
                                    .getIntValue());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction sort_by =
        new VncFunction(
                "sort-by",
                VncFunction
                    .meta()
                    .arglists("(sort-by keyfn coll)", "(sort-by keyfn compfn coll)")
                    .doc(
                        "Returns a sorted sequence of the items in coll, where the sort " +
                        "order is determined by comparing (keyfn item).  If no comparator is " +
                        "supplied, uses compare. \n\n" +
                        "To sort by multiple values use `juxt`, see the examples below.")
                    .examples(
                        "(sort-by :id [{:id 2 :name \"Smith\"} {:id 1 :name \"Jones\"} ])",

                        "(sort-by count [\"aaa\" \"bb\" \"c\"])",

                        "; reversed                                                            \n" +
                        "(sort-by count (comp - compare) [\"aaa\" \"bb\" \"c\"])               ",

                        "(sort-by first [[1 2] [3 4] [2 3]])                                   ",

                        "; sort tuples by first value, and where first value is equal,         \n" +
                        "; sort by second value                                                \n" +
                        "(sort-by (juxt first second) [[3 2] [1 3] [3 1] [1 2]])                ",

                        "; reversed                                                            \n" +
                        "(sort-by first (comp - compare) [[1 2] [3 4] [2 3]])                  ",
                        "(sort-by :rank [{:rank 2} {:rank 3} {:rank 1}])                       ",

                        "; reversed                                                            \n" +
                        "(sort-by :rank (comp - compare) [{:rank 2} {:rank 3} {:rank 1}])      ",

                        ";sort entries in a map by value                                       \n" +
                        "(sort-by val {:foo 7, :bar 3, :baz 5})                                ",

                        "; sort by :foo, and where :foo is equal, sort by :bar                 \n" +
                        "(do                                                                   \n" +
                        "  (def x [ {:foo 2 :bar 11}                                           \n" +
                        "           {:foo 1 :bar 99}                                           \n" +
                        "           {:foo 2 :bar 55}                                           \n" +
                        "           {:foo 1 :bar 77} ])                                        \n" +
                        "  (sort-by (juxt :foo :bar) x))                                       ",

                        "; sort by a given key order                                           \n" +
                        "(do                                                                   \n" +
                        "  (def x [ {:foo 2 :bar 11}                                           \n" +
                        "           {:foo 1 :bar 99}                                           \n" +
                        "           {:foo 2 :bar 55}                                           \n" +
                        "           {:foo 1 :bar 77} ])                                        \n" +
                        "  (def order [55 77 99 11])                                           \n" +
                        "  (sort-by #((into {} (map-indexed (fn [i e] [e i]) order)) (:bar %)) \n" +
                        "           x))                                                         ")
                    .seeAlso("sort")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2, 3);

                final IVncFunction keyfn = Coerce.toIVncFunction(args.first());
                final IVncFunction compfn = args.size() == 2
                                                ? compare
                                                : Coerce.toIVncFunction(args.second());

                keyfn.sandboxFunctionCallValidation();
                compfn.sandboxFunctionCallValidation();

                return sort(
                        "sort-by",
                        args.last(),
                        (x,y) -> Coerce
                                    .toVncLong(
                                        compfn.apply(
                                                VncList.of(
                                                    keyfn.apply(VncList.of(x)),
                                                    keyfn.apply(VncList.of(y)))))
                                    .getIntValue());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction group_by =
        new VncFunction(
                "group-by",
                VncFunction
                    .meta()
                    .arglists("(group-by f coll)")
                    .doc(
                        "Returns a map of the elements of coll keyed by the result of " +
                        "f on each element. The value at each key will be a vector of the " +
                        "corresponding elements, in the order they appeared in coll.")
                    .examples(
                        "(group-by count [\"a\" \"as\" \"asd\" \"aa\" \"asdf\" \"qwer\"])",
                        "(group-by odd? (range 10))",
                        "(group-by identity (seq \"abracadabra\"))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final MeterRegistry meterRegistry = ThreadContext.getMeterRegistry();

                final IVncFunction fn = Coerce.toIVncFunction(args.first());
                final VncSequence coll = Coerce.toVncSequence(args.second());

                fn.sandboxFunctionCallValidation();

                VncMap map = new VncOrderedMap();

                for(VncVal v : coll) {
                    final VncVal key = VncFunction.applyWithMeter(fn, VncList.of(v), meterRegistry);
                    final Object val = map.getJavaMap().get(key);
                    if (val == null) {
                        map = map.assoc(key, VncVector.of(v));
                    }
                    else {
                        final VncSequence seq = Coerce.toVncSequence((VncVal)val);
                        map = map.assoc(key, seq.addAtEnd(v));
                    }
                }

                return map;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction frequencies =
        new VncFunction(
                "frequencies",
                VncFunction
                    .meta()
                    .arglists("(frequencies coll)")
                    .doc(
                        "Returns a map from distinct items in coll to the number of times " +
                        "they appear.")
                    .examples(
                        "(frequencies [:a :b :a :a])",
                        ";; Turn a frequency map back into a coll.\n" +
                        "(mapcat (fn [[x n]] (repeat n x)) {:a 2 :b 1 :c 3})")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncSequence coll = Coerce.toVncSequence(args.first());

                final Map<VncVal,VncLong> map = new HashMap<>();

                for(VncVal v : coll) {
                    VncLong count = map.get(v);
                    if (count == null) {
                        map.put(v, new VncLong(1L));
                    }
                    else {
                        map.put(v, new VncLong(count.getValue()+1));
                    }
                }

                return new VncHashMap(map);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    // General sequence functions
    public static VncFunction apply =
        new VncFunction(
                "apply",
                VncFunction
                    .meta()
                    .arglists("(apply f args* coll)")
                    .doc("Applies f to all arguments composed of args and coll")
                    .examples(
                        "(apply + [1 2 3])",
                        "(apply + 1 2 [3 4 5])",
                        "(apply str [1 2 3 4 5])",
                        "(apply inc [1])")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                final IVncFunction fn = Coerce.toIVncFunction(args.first());
                final VncList fn_args = args.slice(1,args.size()-1);

                fn.sandboxFunctionCallValidation();

                final VncVal coll = args.last();
                if (coll == Nil) {
                    return fn.apply(fn_args);
                }

                final VncSequence seq = Coerce.toVncSequence(coll);
                return seq.isEmpty() ? fn.apply(fn_args)
                                     : fn.apply(fn_args.addAllAtEnd(seq));
             }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction comp =
        new VncFunction(
                "comp",
                VncFunction
                    .meta()
                    .arglists("(comp f*)")
                    .doc(
                        "Takes a set of functions and returns a fn that is the composition " +
                        "of those fns. The returned fn takes a variable number of args, " +
                        "applies the rightmost of fns to the args, the next " +
                        "fn (right-to-left) to the result, etc. ")
                    .examples(
                        "((comp str +) 8 8 8)",
                        "(map (comp - (partial + 3) (partial * 2)) [1 2 3 4])",
                        "((reduce comp [(partial + 1) (partial * 2) (partial + 3)]) 100)",
                        "(filter (comp not zero?) [0 1 0 2 0 3 0 4])",
                        "(do \n" +
                        "   (def fifth (comp first rest rest rest rest)) \n" +
                        "   (fifth [1 2 3 4 5]))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 0);

                final MeterRegistry meterRegistry = ThreadContext.getMeterRegistry();

                final int len = args.size();

                final IVncFunction[] fns = new IVncFunction[len == 0 ? 1 : len];

                if (len == 0) {
                    fns[0] = identity;
                }
                else {
                    // the functions are applied right to left
                    for(int ii=0; ii<len; ii++) {
                        final IVncFunction fn = Coerce.toIVncFunction(args.nth(ii));
                        fn.sandboxFunctionCallValidation();

                        fns[len-1-ii] = fn;
                    }
                }

                return new VncFunction(createAnonymousFuncName("comp")) {
                    @Override
                    public VncVal apply(final VncList args) {
                        VncList args_ = args;
                        for(int ii=0; ii<fns.length-1; ii++) {
                            VncVal result = VncFunction.applyWithMeter(
                                                fns[ii],
                                                args_,
                                                meterRegistry);
                            args_ = VncList.of(result);
                        }

                        return VncFunction.applyWithMeter(fns[fns.length-1], args_, meterRegistry);
                    }

                    private static final long serialVersionUID = -1L;
                };
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction compare =
        new VncFunction(
                "compare",
                VncFunction
                    .meta()
                    .arglists("(compare x y)")
                    .doc(
                        "Comparator. Returns -1, 0, or 1 when x is logically 'less than', " +
                        "'equal to', or 'greater than' y. For list and vectors the longer " +
                        "sequence is always 'greater' regardless of its contents. " +
                        "For sets and maps only the size of the collection is compared.")
                    .examples(
                        "(compare nil 0)",
                        "(compare 0 nil)",
                        "(compare 1 0)",
                        "(compare 1 1)",
                        "(compare 1M 2M)",
                        "(compare 1 nil)",
                        "(compare nil 1)",
                        "(compare \"aaa\" \"bbb\")",
                        "(compare [0 1 2] [0 1 2])",
                        "(compare [0 1 2] [0 9 2])",
                        "(compare [0 9 2] [0 1 2])",
                        "(compare [1 2 3] [0 1 2 3])",
                        "(compare [0 1 2] [3 4])")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                return new VncLong(args.first().compareTo(args.second()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction partial =
        new VncFunction(
                "partial",
                VncFunction
                    .meta()
                    .arglists("(partial f args*)")
                    .doc(
                        "Takes a function f and fewer than the normal arguments to f, and " +
                        "returns a fn that takes a variable number of additional args. When " +
                        "called, the returned function calls f with args + additional args.")
                    .examples(
                        "((partial * 2) 3)",
                        "(map (partial * 2) [1 2 3 4])",
                        "(map (partial reduce +) [[1 2 3 4] [5 6 7 8]])",
                        "(do \n" +
                        "   (def hundred-times (partial * 100)) \n" +
                        "   (hundred-times 5))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                final VncVal first = args.first();
                final IVncFunction fn = Coerce.toIVncFunction(first);
                final VncList fnArgs = args.rest();

                fn.sandboxFunctionCallValidation();

                return new VncFunction(createAnonymousFuncName("partial")) {
                    @Override
                    public VncVal apply(final VncList args) {
                        final CallStack cs = ThreadContext.getCallStack();
                        try {
                            final VncList fnArgsAll = fnArgs.addAllAtEnd(args);

                            cs.push(fn instanceof VncFunction
                                        ? new CallFrame(
                                                (VncFunction)fn,
                                                fnArgsAll)
                                        : new CallFrame(
                                                Types.getType(first).getQualifiedName(),
                                                fnArgsAll,
                                                first.getMeta()));
                            return fn.apply(fnArgsAll);
                        }
                        finally {
                            cs.pop();
                        }
                    }

                    private static final long serialVersionUID = -1L;
                };
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

     public static VncFunction run_BANG =
        new VncFunction(
                "run!",
                VncFunction
                    .meta()
                    .arglists(
                        "(run! f coll)")
                    .doc(
                        "Runs the supplied function, for purposes of side " +
                        "effects, on successive items in the collection. Returns `nil`")
                    .examples(
                        "(run! prn [1 2 3 4])")
                    .seeAlso(
                        "docoll", "mapv")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                // It's an alias for 'docoll' -> clojure compatibility
                return docoll.apply(args);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction mapv =
        new VncFunction(
                "mapv",
                VncFunction
                    .meta()
                    .arglists(
                        "(mapv f coll colls*)")
                    .doc(
                        "Returns a vector consisting of the result of applying f " +
                        "to the set of first items of each coll, followed by applying " +
                        "f to the set of second items in each coll, until any one of the colls " +
                        "is exhausted. Any remaining items in other colls are ignored. ")
                    .examples(
                        "(mapv inc [1 2 3 4])",
                        "(mapv + [1 2 3 4] [10 20 30 40])",
                        "(mapv vector [1 2 3 4] [10 20 30 40])")
                    .seeAlso(
                        "docoll")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 2);

                final MeterRegistry meterRegistry = ThreadContext.getMeterRegistry();

                final IVncFunction fn = Coerce.toIVncFunction(args.first());
                final VncList lists = removeNilValues(args.rest());
                final List<VncVal> result = new ArrayList<>();

                fn.sandboxFunctionCallValidation();

                if (lists.isEmpty()) {
                    return Nil;
                }

                int index = 0;
                boolean hasMore = true;
                while(hasMore) {
                    final List<VncVal> fnArgs = new ArrayList<>();

                    for(int ii=0; ii<lists.size(); ii++) {
                        final VncSequence nthList = Coerce.toVncSequence(lists.nth(ii));
                        if (nthList.size() > index) {
                            fnArgs.add(nthList.nth(index));
                        }
                        else {
                            hasMore = false;
                            break;
                        }
                    }

                    if (hasMore) {
                        result.add(VncFunction.applyWithMeter(
                                        fn,
                                        VncList.ofList(fnArgs),
                                        meterRegistry));
                        index += 1;
                    }
                }

                return VncVector.ofList(result);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction map_keys =
        new VncFunction(
                "map-keys",
                VncFunction
                    .meta()
                    .arglists("(map-keys f m)")
                    .doc(
                        "Applys function f to the keys of the map m.")
                    .examples(
                        "(map-keys name {:a 1 :b 2 :c 3})")
                    .seeAlso("map-vals", "map-invert")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 2);

                final MeterRegistry meterRegistry = ThreadContext.getMeterRegistry();

                final IVncFunction fn = Coerce.toIVncFunction(args.first());
                final VncMap map = Coerce.toVncMap(args.second());

                fn.sandboxFunctionCallValidation();

                VncMap newMap = map.emptyWithMeta();

                for(VncMapEntry e : map.entries()) {
                    newMap = newMap.assoc(
                                VncList.of(
                                    VncFunction.applyWithMeter(
                                            fn,
                                            VncList.of(e.getKey()),
                                            meterRegistry),
                                    e.getValue()));
                }

                return newMap;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction map_vals =
        new VncFunction(
                "map-vals",
                VncFunction
                    .meta()
                    .arglists("(map-vals f m)")
                    .doc(
                        "Applys function f to the values of the map m.")
                    .examples(
                        "(map-vals inc {:a 1 :b 2 :c 3})",
                        "(map-vals :len {:a {:col 1 :len 10} :b {:col 2 :len 20} :c {:col 3 :len 30}})")
                    .seeAlso("map-keys", "map-invert")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 2);

                final MeterRegistry meterRegistry = ThreadContext.getMeterRegistry();

                final IVncFunction fn = Coerce.toIVncFunction(args.first());
                final VncMap map = Coerce.toVncMap(args.second());

                fn.sandboxFunctionCallValidation();

                VncMap newMap = map.emptyWithMeta();

                for(VncMapEntry e : map.entries()) {
                    newMap = newMap.assoc(
                                VncList.of(
                                    e.getKey(),
                                    VncFunction.applyWithMeter(
                                            fn,
                                            VncList.of(e.getValue()),
                                            meterRegistry)));
                }

                return newMap;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction docoll =
        new VncFunction(
                "docoll",
                VncFunction
                    .meta()
                    .arglists("(docoll f coll)")
                    .doc(
                        "Applies f to the items of the collection presumably for side effects. " +
                        "Returns nil.\n\n" +
                        "If coll is a lazy sequence, `docoll` iterates over the lazy sequence " +
                        "and realizes value by value while calling function f on the realized " +
                        "values.")
                    .examples(
                        "(docoll #(println %) [1 2 3 4])",
                        "(docoll (fn [[k v]] (println (pr-str k v)))  \n" +
                        "        {:a 1 :b 2 :c 3 :d 4})               ",
                        ";; docoll all elements of a queue. calls (take! queue) to get the     \n" +
                        ";; elements of the queue.                                             \n" +
                        ";; note: use nil to mark the end of the queue otherwise docoll will   \n" +
                        ";;       block forever!                                               \n" +
                        "(let [q (conj! (queue) 1 2 3 nil)]       \n" +
                        "  (docoll println q))                    ",
                        ";; lazy sequence                         \n" +
                        "(let [q (conj! (queue) 1 2 3 nil)]       \n" +
                        "  (defn f []                             \n" +
                        "    (let [v (poll! q)]                   \n" +
                        "      (println \"Producing \" v)         \n" +
                        "      v))                                \n" +
                        "  (docoll #(println \"Collecting\" %)    \n" +
                        "          (lazy-seq f)))                 ")
                    .seeAlso("mapv")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final MeterRegistry meterRegistry = ThreadContext.getMeterRegistry();

                final IVncFunction fn = Coerce.toIVncFunction(args.first());
                final VncVal coll = args.second();

                fn.sandboxFunctionCallValidation();

                if (coll == Nil) {
                    // ok do nothing
                }
                else if (Types.isVncSequence(coll)) {
                    for (VncVal p : (VncSequence)coll) {
                        VncFunction.applyWithMeter(
                                fn,
                                VncList.of(p),
                                meterRegistry);
                    }
                }
                else if (Types.isVncMap(coll)) {
                    ((VncMap)coll).entries().forEach(v -> VncFunction.applyWithMeter(
                                                                fn,
                                                                VncList.of(VncVector.of(v.getKey(), v.getValue())),
                                                                meterRegistry));
                }
                else if (Types.isVncQueue(coll)) {
                    final VncQueue queue = (VncQueue)coll;

                    while(true) {
                        final VncVal v = queue.take();
                        if (v == Nil) break;  // queue has been closed

                        VncFunction.applyWithMeter(
                                fn,
                                VncList.of(v),
                                meterRegistry);
                    }
                }
                else {
                    throw new VncException(String.format(
                            "docoll: collection type %s not supported",
                            Types.getType(coll)));
                }

                return Nil;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction doall =
        new VncFunction(
                "doall",
                VncFunction
                    .meta()
                    .arglists(
                        "(doall coll)",
                        "(doall n coll)")
                    .doc(
                        "When lazy sequences are produced `doall` can be used to force " +
                        "any effects and realize the lazy sequence.\n" +
                        "Returns the relaized items in a list!")
                    .examples(
                        "(->> (lazy-seq #(rand-long 100))  \n" +
                        "     (take 4)                     \n" +
                        "     (doall))",
                        "(->> (lazy-seq #(rand-long 100))  \n" +
                        "     (doall 4))")
                    .seeAlso("lazy-seq", "cycle", "repeat", "cons")
                    .build()
       ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                if (args.size() == 1) {
                    // (doall coll)
                    if (Types.isVncLazySeq(args.first())) {
                        final VncLazySeq seq = (VncLazySeq)args.first();
                        return seq.realize();
                    }
                    else if (Types.isVncCollection(args.first())) {
                        return args.first();
                    }
                    else {
                        throw new VncException(String.format(
                                "doall: type %s not supported as first argument",
                                Types.getType(args.first())));
                    }
                }
                else if (args.size() == 2) {
                    if (Types.isVncNumber(args.first())) {
                        // (doall n coll)
                        final int n = Coerce.toVncLong(args.first()).getIntValue();
                        if (Types.isVncLazySeq(args.second())) {
                            final VncLazySeq seq = (VncLazySeq)args.second();
                            return seq.realize(n);
                        }
                        else if (Types.isVncCollection(args.second())) {
                            final VncCollection coll = (VncCollection)args.second();
                            return coll.toVncList().slice(0, n);
                        }
                        else {
                            throw new VncException(String.format(
                                    "doall: type %s not supported as first argument",
                                    Types.getType(args.second())));
                        }
                    }
                    else {
                        throw new VncException(String.format(
                                "doall: type %s not supported as second argument",
                                Types.getType(args.second())));
                    }
                }
                else {
                    throw new VncException(String.format(
                            "doall: invalid number of arguments %d",
                            args.size()));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction mapcat =
        new VncFunction(
                "mapcat",
                VncFunction
                    .meta()
                    .arglists("(mapcat fn & colls)")
                    .doc(
                        "Returns the result of applying concat to the result of applying map " +
                        "to fn and colls. Thus function fn should return a collection.")
                    .examples(
                        "(mapcat identity [[1 2 3] [4 5 6] [7 8 9]])",
                        "(mapcat identity [[1 2 [3 4]] [5 6 [7 8]]])",
                        "(mapcat reverse [[3 2 1 ] [6 5 4] [9 8 7]])",
                        "(mapcat list [:a :b :c] [1 2 3])",
                        "(mapcat #(remove even? %) [[1 2] [2 2] [2 3]])",
                        "(mapcat #(repeat 2 %) [1 2])",
                        "(mapcat (juxt inc dec)  [1 2 3 4])",
                        ";; Turn a frequency map back into a coll.\n" +
                        "(mapcat (fn [[x n]] (repeat n x)) {:a 2 :b 1 :c 3})")
                    .seeAlso("map", "flatten")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                return concat.apply(Coerce.toVncList(TransducerFunctions.map.apply(args)));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction map_invert =
        new VncFunction(
                "map-invert",
                VncFunction
                    .meta()
                    .arglists("(map-invert m)")
                    .doc(
                        "Returns the map with the vals mapped to the keys.")
                    .examples(
                        "(map-invert {:a 1 :b 2 :c 3})")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncMap m = Coerce.toVncMap(args.first());

                final Map<VncVal,VncVal> inverted = new HashMap<>();
                for(VncMapEntry e : m.entries()) {
                    inverted.put(e.getValue(), e.getKey());
                }
                return m.withValues(inverted, m.getMeta());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction filter_k =
        new VncFunction(
                "filter-k",
                VncFunction
                    .meta()
                    .arglists("(filter-k f map)")
                    .doc(
                        "Returns a map with entries for which the predicate (f key) returns " +
                        "logical true. f is a function with one arguments.")
                    .examples(
                        "(filter-k #(= % :a) {:a 1 :b 2 :c 3})")
                    .seeAlso("filter-kv")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final IVncFunction filterFn = Coerce.toIVncFunction(args.first());
                VncMap map = Coerce.toVncMap(args.second());

                filterFn.sandboxFunctionCallValidation();

                if (map.isEmpty()) {
                    return map;
                }
                else {
                    for(VncVal key : map.keys()) {
                        final VncVal r = filterFn.apply(VncList.of(key));
                        if (r == Nil || VncBoolean.isFalse(r)) {
                            map = map.dissoc(key);
                        }
                    }

                    return map;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction filter_kv =
        new VncFunction(
                "filter-kv",
                VncFunction
                    .meta()
                    .arglists("(filter-kv f map)")
                    .doc(
                        "Returns a map with entries for which the predicate `(f key value)` " +
                        "returns logical true. f is a function with two arguments.")
                    .examples(
                        "(filter-kv (fn [k v] (= k :a)) {:a 1 :b 2 :c 3})",
                        "(filter-kv (fn [k v] (= v 2)) {:a 1 :b 2 :c 3})")
                    .seeAlso("filter-k")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final IVncFunction filterFn = Coerce.toIVncFunction(args.first());
                final VncMap map = Coerce.toVncMap(args.second());

                filterFn.sandboxFunctionCallValidation();

                if (map.isEmpty()) {
                    return map;
                }
                else {
                    final HashMap<VncVal, VncVal> filtered = new HashMap<>();
                    for(VncMapEntry entry : map.entries()) {
                        final VncVal key = entry.getKey();
                        final VncVal val = entry.getValue();

                        final VncVal r = filterFn.apply(VncList.of(key, val));
                        if (r != Nil && !VncBoolean.isFalse(r)) {
                            filtered.put(key, val);
                        }
                    }

                    return new VncHashMap(filtered, map.getMeta());
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction reduce =
        new VncFunction(
                "reduce",
                VncFunction
                    .meta()
                    .arglists("(reduce f coll)", "(reduce f val coll)")
                    .doc(
                        "f should be a function of 2 arguments. If val is not supplied, " +
                        "returns the result of applying f to the first 2 items in coll, then " +
                        "applying f to that result and the 3rd item, etc. If coll contains no " +
                        "items, f must accept no arguments as well, and reduce returns the " +
                        "result of calling f with no arguments. If coll has only 1 item, it " +
                        "is returned and f is not called.  If val is supplied, returns the " +
                        "result of applying f to val and the first item in coll, then " +
                        "applying f to that result and the 2nd item, etc. If coll contains no " +
                        "items, returns val and f is not called.\n\n" +
                        "`reduce` can work with queues as collection, given that the " +
                        "end of the queue is marked by addding a `nil` element. Otherwise " +
                        "the reducer does not not when to stop reading elements from " +
                        "the queue.")
                    .examples(
                        "(reduce + [1 2 3 4 5 6 7])",
                        "(reduce + 10 [1 2 3 4 5 6 7])",
                        "(reduce (fn [x y] (+ x y 10)) [1 2 3 4 5 6 7])",
                        "(reduce (fn [x y] (+ x y 10)) 10 [1 2 3 4 5 6 7])",
                        "((reduce comp [(partial + 1) (partial * 2) (partial + 3)]) 100)",
                        "(reduce (fn [m [k v]] (assoc m k v)) {} [[:a 1] [:b 2] [:c 3]])",
                        "(reduce (fn [m [k v]] (assoc m v k)) {} {:b 2 :a 1 :c 3})",
                        "(reduce (fn [m c] (assoc m (first c) c)) {} [[:a 1] [:b 2] [:c 3]])",
                        ";; sliding window (width 3) average\n" +
                        "(->> (partition 3 1 (repeatedly 10 #(rand-long 30)))\n" +
                        "     (map (fn [window] (/ (reduce + window) (count window)))))",
                        ";; reduce all elements of a queue.                         \n" +
                        ";; calls (take! queue) to get the elements of the queue.   \n" +
                        ";; note: use nil to mark the end of the queue otherwise    \n" +
                        ";;       reduce will block forever!                        \n" +
                        "(let [q (conj! (queue) 1 2 3 4 5 6 7 nil)]                 \n" +
                        "  (reduce + q))                                            ",
                        ";; reduce data supplied by a finit lazy seq \n" +
                        "(do                                         \n" +
                        "  (def counter (atom 5))                    \n" +
                        "  (defn generate []                         \n" +
                        "    (swap! counter dec)                     \n" +
                        "    (if (pos? @counter) @counter nil))      \n" +
                        "  (reduce + 100 (lazy-seq generate)))       ")
                    .seeAlso("reduce-kv", "map", "filter")
                    .build()
        ) {
            @Override
            @SuppressWarnings("unchecked")
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2, 3);

                final boolean noInitValue = args.size() < 3;

                final IVncFunction reduceFn = Coerce.toIVncFunction(args.first());
                final VncVal init = noInitValue ? null : args.second();
                final VncVal coll = noInitValue ? args.second() : args.third();

                reduceFn.sandboxFunctionCallValidation();

                if (Types.isVncSequence(coll)) {
                    return reduce_sequence((VncSequence)coll, reduceFn, init);
                }
                else if (Types.isVncMap(coll)) {
                    return reduce_sequence(((VncMap)coll).toVncList(), reduceFn, init);
                }
                else if (Types.isVncQueue(coll)) {
                    return reduce_queue((VncQueue)coll, reduceFn, init);
                }
                else if (Types.isIVncFunction(coll)) {
                    return reduce_supplyFn((IVncFunction)coll, reduceFn, init);
                }
                else if (coll instanceof Iterable<?>) {
                   return reduce_iterable((Iterable<VncVal>)coll, reduceFn, init);
                }
                else if (coll == Nil) {
                    return reduce_sequence(VncList.empty(), reduceFn, init);
                }
                else {
                    throw new VncException(String.format(
                            "reduce: collection type %s not supported",
                            Types.getType(args.last())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    private static VncVal reduce_sequence(
            final VncSequence seq,
            final IVncFunction reduceFn,
            final VncVal init
    ) {
        final MeterRegistry meterRegistry = ThreadContext.getMeterRegistry();

        if (init == null) {
            return seq.isEmpty()
                    ? reduceFn.apply(VncList.empty())
                    : Reducer.reduce(reduceFn, seq.first(), seq.rest(), meterRegistry);
        }
        else {
            return Reducer.reduce(reduceFn, init, seq, meterRegistry);
        }
    }

    private static VncVal reduce_queue(
            final VncQueue queue,
            final IVncFunction reduceFn,
            final VncVal init
    ) {
        final MeterRegistry meterRegistry = ThreadContext.getMeterRegistry();

        if (init == null) {
            final VncVal init_ = queue.take();
            return init_ == Nil  // queue has been closed -> empty
                    ? reduceFn.apply(VncList.empty())
                    : Reducer.reduce(reduceFn, init_, queue, meterRegistry);
        }
        else {
            return Reducer.reduce(reduceFn, init, queue, meterRegistry);
        }
    }

    private static VncVal reduce_supplyFn(
            final IVncFunction supplyFn,
            final IVncFunction reduceFn,
            final VncVal init
    ) {
        final MeterRegistry meterRegistry = ThreadContext.getMeterRegistry();

        if (init == null) {
            final VncVal init_ = supplyFn.apply(VncList.empty());
            return init_ == Nil  // queue has been closed -> empty
                    ? reduceFn.apply(VncList.empty())
                    : Reducer.reduce(reduceFn, init_, supplyFn, meterRegistry);
        }
        else {
            return Reducer.reduce(reduceFn, init, supplyFn, meterRegistry);
        }
    }

    private static VncVal reduce_iterable(
            final Iterable<VncVal> iterable,
            final IVncFunction reduceFn,
            final VncVal init
    ) {
        final MeterRegistry meterRegistry = ThreadContext.getMeterRegistry();

        final Iterator<VncVal> iter = iterable.iterator();

        if (init == null) {
            final VncVal init_ = iter.hasNext() ? iter.next() : Nil;
            return init_ == Nil
                    ? reduceFn.apply(VncList.empty())
                    : Reducer.reduce(reduceFn, init_, iter, meterRegistry);
        }
        else {
            return Reducer.reduce(reduceFn, init, iter, meterRegistry);
        }
    }

    public static VncFunction reduce_kv =
        new VncFunction(
                "reduce-kv",
                VncFunction
                    .meta()
                    .arglists("(reduce-kv f init coll)")
                    .doc(
                        "Reduces an associative collection. f should be a function of 3 " +
                        "arguments. Returns the result of applying f to init, the first key " +
                        "and the first value in coll, then applying f to that result and the " +
                        "2nd key and value, etc. If coll contains no entries, returns init " +
                        "and f is not called. Note that reduce-kv is supported on vectors, " +
                        "where the keys will be the ordinals.")
                    .examples(
                        "(reduce-kv (fn [m k v] (assoc m v k)) \n" +
                        "           {}                         \n" +
                        "           {:a 1 :b 2 :c 3})",
                        "(reduce-kv (fn [m k v] (assoc m k (:col v))) \n" +
                        "           {}                                \n" +
                        "           {:a {:col :red   :len 10}         \n" +
                        "            :b {:col :green :len 20}         \n" +
                        "            :c {:col :blue  :len 30} })")
                    .seeAlso("reduce", "map", "filter")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 3);

                final IVncFunction reduceFn = Coerce.toIVncFunction(args.first());
                final List<VncMapEntry> values = Coerce.toVncMap(args.third()).entries();

                reduceFn.sandboxFunctionCallValidation();

                VncVal value = args.second();

                if (values.isEmpty()) {
                    return value;
                }
                else {
                    for(VncMapEntry entry : values) {
                        final VncVal key = entry.getKey();
                        final VncVal val = entry.getValue();
                        value = reduceFn.apply(VncList.of(value, key, val));
                    }

                    return value;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction merge =
        new VncFunction(
                "merge",
                VncFunction
                    .meta()
                    .arglists("(merge & maps)")
                    .doc(
                        "Returns a map that consists of the rest of the maps conj-ed onto " +
                        "the first.  If a key occurs in more than one map, the mapping from " +
                        "the latter (left-to-right) will be the mapping in the result.")
                    .examples(
                        "(merge {:a 1 :b 2 :c 3} {:b 9 :d 4})",
                        "(merge {:a 1} nil)",
                        "(merge nil {:a 1})",
                        "(merge nil nil)")
                    .seeAlso("merge-with", "merge-deep", "into", "concat")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                // remove Nil
                final List<VncVal> maps = args.stream()
                                              .filter(v -> v != Nil)
                                              .collect(Collectors.toList());

                if (maps.isEmpty()) {
                    return Nil;
                }
                else {
                    final Map<VncVal,VncVal> map = new HashMap<>();
                    maps.stream().forEach(v -> map.putAll(Coerce.toVncMap(v).getJavaMap()));
                    return new VncHashMap(map);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction merge_with =
        new VncFunction(
                "merge-with",
                VncFunction
                    .meta()
                    .arglists("(merge-with f & maps)")
                    .doc(
                        "Returns a map that consists of the rest of the maps conj-ed onto " +
                        "the first. If a key occurs in more than one map, the mapping(s) " +
                        "from the latter (left-to-right) will be combined with the mapping in " +
                        "the result by calling (f val-in-result val-in-latter).")
                    .examples(
                        "(merge-with + {:a 1 :b 2} {:a 9 :b 98 :c 0})",
                        "(merge-with into {:a [1] :b [2]} {:b [3 4] :c [5 6]})")
                    .seeAlso("merge", "merge-deep")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                final List<VncMap> rest = args.rest()
                                              .stream()
                                              .filter(v -> v != Nil)
                                              .map(v -> Coerce.toVncMap(v))
                                              .collect(Collectors.toList());

                if (rest.isEmpty()) {
                    return new VncHashMap();
                }
                else if (rest.size() == 1) {
                    return rest.get(0);
                }

                final VncFunction fn = Coerce.toVncFunction(args.first());
                fn.sandboxFunctionCallValidation();

                final Map<VncVal,VncVal> map = new HashMap<>();

                for(VncMap m : rest) {
                    for(VncMapEntry e : m.entries()) {
                        final VncVal key = e.getKey();
                        final VncVal val1 = map.get(key);
                        final VncVal val2 = e.getValue();

                        if (val1 == null) {
                            map.put(key, fn.apply(VncList.of(val2)));
                        }
                        else if (val2 == null) {
                            map.put(key, fn.apply(VncList.of(val1)));
                        }
                        else {
                            map.put(key, fn.apply(VncList.of(val1, val2)));
                        }
                    }
                }

                return new VncHashMap(map);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction merge_deep =
        new VncFunction(
                "merge-deep",
                VncFunction
                    .meta()
                    .arglists(
                        "(merge-deep values)",
                        "(merge-deep strategy & values)")
                    .doc(
                        "Recursively merges maps.\n\n" +
                        "If the first parameter is a keyword it defines the strategy to\n" +
                        "use when merging non-map collections. Options are:\n\n" +
                        "1. *:replace*, the default, the last value is used\n" +
                        "2. *:into*, if the value in every map is a collection they are\n" +
                        "   concatenated using `into`. Thus the type of (first) value is\n" +
                        "   maintained.")
                    .examples(
                        "(merge-deep {:a {:c 2}} {:a {:b 1}})",
                        "(merge-deep :replace {:a [1]} {:a [2]})",
                        "(merge-deep :into {:a [1]} {:a [2]})",
                        "(merge-deep {:a 1} nil)")
                    .seeAlso("merge", "merge-with")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                final boolean hasStrategy = Types.isVncKeyword(args.first());

                final VncList values = hasStrategy ? args.rest() : args;

                final VncKeyword strategy = hasStrategy
                                                ? (VncKeyword)args.first()
                                                : new VncKeyword(":replace");

                final boolean strategyInto = new VncKeyword(":into").equals(strategy);

                if (VncBoolean.isTrue(every_Q.applyOf(map_Q, values))) {
                    return apply.applyOf(
                                    merge_with,
                                    partial.applyOf(merge_deep, strategy),
                                    values);
                }
                else if (strategyInto && VncBoolean.isTrue(every_Q.applyOf(coll_Q, values))) {
                    return reduce.applyOf(into, values);
                }
                else {
                    return values.last();
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction disj =
        new VncFunction(
                "disj",
                VncFunction
                    .meta()
                    .arglists("(disj set x)", "(disj set x & xs)")
                    .doc( "Returns a new set with the x, xs removed.")
                    .examples("(disj (set 1 2 3) 3)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 2);

                if (args.first() instanceof VncSet) {
                    return ((VncSet)args.first()).removeAll(args.rest());
                }
                else {
                    throw new VncException(String.format(
                            "Invalid coll %s while calling function 'disj'",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction seq =
        new VncFunction(
                "seq",
                VncFunction
                    .meta()
                    .arglists("(seq coll)")
                    .doc(
                        "Returns a seq on the collection. If the collection is " +
                        "empty, returns nil. `(seq nil)` returns nil. seq also works on " +
                        "Strings and converts Java streams to lists.")
                    .examples(
                        "(seq nil)",
                        "(seq [])",
                        "(seq [1 2 3])",
                        "(seq '(1 2 3))",
                        "(seq {:a 1 :b 2})",
                        "(seq \"abcd\")",
                        "(flatten (seq {:a 1 :b 2}))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal val = args.first();
                if (Types.isVncJavaObject(val, java.util.stream.Stream.class)) {
                    // convert to venice list
                    // TODO: handle the formal type
                    @SuppressWarnings("unchecked")
                    java.util.stream.Stream<Object> stream = (java.util.stream.Stream<Object>)((VncJavaObject)val).getDelegate();
                    return VncList.ofList(
                            stream.map(o -> new VncJavaObject(o))
                                  .collect(Collectors.toList()));
                }
                else if (Types.isVncMap(val)) {
                    if (((VncMap)val).isEmpty()) {
                        return Nil;
                    }
                    return VncList.ofList(
                            ((VncMap)val)
                                .entries()
                                .stream()
                                .map(e -> VncVector.of(e.getKey(), e.getValue()))
                                .collect(Collectors.toList()));
                }
                else if (Types.isVncLazySeq(val)) {
                    return ((VncLazySeq)val).isEmpty() ? Nil : val;
                }
                else if (Types.isVncSequence(val)) {
                    return ((VncSequence)val).isEmpty() ? Nil : ((VncSequence)val).toVncList();
                }
                else if (Types.isVncString(val)) {
                    final VncString s = (VncString)val;
                    return s.isEmpty() ? Nil : s.toVncList();
                }
                else if (val == Nil) {
                    return Nil;
                }
                else {
                    throw new VncException("seq: called on non-sequence");
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction repeat =
        new VncFunction(
                "repeat",
                VncFunction
                    .meta()
                    .arglists(
                        "(repeat x)",
                        "(repeat n x)")
                    .doc(
                        "Returns a lazy sequence of x values or a collection with " +
                        "the value x repeated n times.")
                    .examples(
                        "(repeat 3 \"hello\")",
                        "(repeat 5 [1 2])",
                        "(repeat \":\")",
                        "(interleave [:a :b :c] (repeat 100))")
                    .seeAlso("repeatedly", "dotimes", "constantly")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                if (args.size() == 1) {
                    final VncVal val = args.first();
                    return VncLazySeq.continually(
                            new VncFunction(createAnonymousFuncName("repeat")) {
                                @Override
                                public VncVal apply(final VncList args) {
                                    return val;
                                }
                                private static final long serialVersionUID = 1L;
                            },
                            Nil);
                }
                else {
                    final long repeat = Coerce.toVncLong(args.first()).getValue();
                    if (repeat < 0) {
                        throw new VncException("repeat: a count n must be grater or equal to 0");
                    }

                    final VncVal val = args.second();
                    final List<VncVal> values = new ArrayList<>();
                    for(int ii=0; ii<repeat; ii++) {
                        values.add(val);
                    }
                    return VncList.ofList(values);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction repeatedly =
        new VncFunction(
                "repeatedly",
                VncFunction
                    .meta()
                    .arglists("(repeatedly n fn)")
                    .doc(
                        "Takes a function of no args, presumably with side effects, and " +
                        "returns a collection of n calls to it")
                    .examples(
                        "(repeatedly 5 #(rand-long 11))",
                        ";; compare with repeat, which only calls the 'rand-long'\n" +
                        ";; function once, repeating the value five times. \n" +
                        "(repeat 5 (rand-long 11))")
                    .seeAlso("repeat", "dotimes", "constantly")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final MeterRegistry meterRegistry = ThreadContext.getMeterRegistry();

                final long repeat = Coerce.toVncLong(args.first()).getValue();
                final IVncFunction fn = Coerce.toIVncFunction(args.second());

                fn.sandboxFunctionCallValidation();

                if (repeat < 0) {
                    throw new VncException("repeatedly: a count n must be grater or equal to 0");
                }

                final List<VncVal> values = new ArrayList<>();
                for(int ii=0; ii<repeat; ii++) {
                    values.add(VncFunction.applyWithMeter(fn, VncList.empty(), meterRegistry));
                }
                return VncList.ofList(values);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction cycle =
        new VncFunction(
                "cycle",
                VncFunction
                    .meta()
                    .arglists(
                        "(cycle coll)")
                    .doc(
                        "Returns a lazy (infinite!) sequence of repetitions of the items in coll.")
                    .examples(
                        "(doall (take 5 (cycle [1 2])))")
                    .seeAlso("repeat", "repeatedly", "dotimes", "constantly")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncSequence seq = Coerce.toVncSequence(args.first());

                if (seq.isEmpty()) {
                    throw new VncException("cycle: the cycle collection must not be empty!");
                }

                final VncFunction f = new VncFunction(createAnonymousFuncName("cycle")) {
                    private int idx = -1;

                    @Override
                    public VncVal apply(final VncList args) {
                        idx = (idx + 1) % seq.size();
                        return seq.nth(idx);
                    }
                    private static final long serialVersionUID = -1;
                };

                return VncLazySeq.iterate(f, Nil);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction identity =
        new VncFunction(
                "identity",
                VncFunction
                    .meta()
                    .arglists("(identity x)")
                    .doc("Returns its argument.")
                    .examples(
                        "(identity 4)",
                        "(filter identity [1 2 3 nil 4 false true 1234])")
                    .build()
        ) {
            @Override
            public VncVector getParams() {
                return VncVector.of(new VncSymbol("x"));
            }

            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return args.first();
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };




    ///////////////////////////////////////////////////////////////////////////
    // Meta functions
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction meta =
        new VncFunction(
                "meta",
                VncFunction
                    .meta()
                    .arglists("(meta obj)")
                    .doc("Returns the metadata of obj, returns nil if there is no metadata.")
                    .examples("(meta (vary-meta [1 2] assoc :foo 3))")
                    .seeAlso("vary-meta", "with-meta", "var-val-meta", "var-sym-meta")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return args.first().getMeta();
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction with_meta =
        new VncFunction(
                "with-meta",
                VncFunction
                    .meta()
                    .arglists("(with-meta obj m)")
                    .doc("Returns a copy of the object obj, with a map m as its metadata.")
                    .examples("(meta (with-meta [1 2] {:foo 3}))")
                    .seeAlso("meta", "vary-meta", "var-val-meta", "var-sym-meta")
                   .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                return args.first().withMeta(Coerce.toVncMap(args.second()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction vary_meta =
        new VncFunction(
                "vary-meta",
                VncFunction
                    .meta()
                    .arglists("(vary-meta obj f & args)")
                    .doc("Returns a copy of the object obj, with (apply f (meta obj) args) as its metadata.")
                    .examples("(meta (vary-meta [1 2] assoc :foo 3))")
                    .seeAlso("meta", "with-meta", "var-val-meta", "var-sym-meta")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 2);

                final VncVal obj = args.first();
                final VncVal meta = obj.getMeta();
                final IVncFunction fn = Coerce.toIVncFunction(args.second());
                final VncList fnArgs = args.slice(2).addAtStart(meta == Nil ? new VncHashMap() : meta);

                return obj.withMeta(fn.apply(fnArgs));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction alter_meta_BANG =
        new VncFunction(
                "alter-meta!",
                VncFunction
                    .meta()
                    .arglists(
                        "(alter-meta! obj f & args)")
                    .doc(
                        "Atomically sets the metadata for a namespace/agent/atom/volatile \n" +
                        "to be:                                                           \n\n" +
                        "  `(apply f its-current-meta args)`                              \n\n" +
                        "f must be free of side-effects.")
                    .examples(
                            "(do                                   \n" +
                            "  (def counter (atom 0))              \n" +
                            "  (alter-meta! counter assoc :a 1))   ",
                            "(do                                   \n" +
                            "  (def counter (atom 0))              \n" +
                            "  (alter-meta! counter assoc :a 1)    \n" +
                            "  (meta counter))                     ")
                    .seeAlso("meta", "reset-meta!")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 2);

                // TODO: implement

                throw new VncException("The function alter-meta! is not yet implemented!");
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction reset_meta_BANG =
        new VncFunction(
                "reset-meta!",
                VncFunction
                    .meta()
                    .arglists(
                        "(reset-meta! obj metadata-map)")
                    .doc(
                        "Atomically resets the metadata for a namespace/agent/atom/volatile")
                    .examples(
                        "(do                                   \n" +
                        "  (def counter (atom 0))              \n" +
                        "  (alter-meta! counter assoc :a 1)    \n" +
                        "  (reset-meta! counter {}))           ",
                        "(do                                   \n" +
                        "  (def counter (atom 0))              \n" +
                        "  (alter-meta! counter assoc :a 1)    \n" +
                        "  (reset-meta! counter {})            \n" +
                        "  (meta counter))                     ")
                    .seeAlso("meta", "alter-meta!")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                // TODO: implement

                throw new VncException("The function reset-meta! is not yet implemented!");
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };




    ///////////////////////////////////////////////////////////////////////////
    // Namespace
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction ns_alias =
        new VncFunction(
                "ns-alias",
                VncFunction
                    .meta()
                    .arglists("(ns-alias alias namespace-sym)")
                    .doc(
                        "Add an alias in the current namespace to another namespace. " +
                        "Arguments are two symbols: the alias to be used, and the " +
                        "symbolic name of the target namespace.")
                    .examples(
                        "(ns-alias 'p 'parsatron)",
                        "(do                      \n" +
                        "  (load-module :hexdump) \n" +
                        "  (ns-alias 'h 'hexdump) \n" +
                        "  (h/dump [0 1 2 3]))    ")
                    .seeAlso(
                        "ns-unalias", "ns-aliases", "*ns*", "ns")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final VncSymbol alias = Coerce.toVncSymbol(args.first());
                final VncSymbol ns = Coerce.toVncSymbol(args.second());

                Namespaces.getCurrentNamespace().addAlias(alias.getName(), ns.getQualifiedName());

                return Nil;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction ns_aliases =
        new VncFunction(
                "ns-aliases",
                VncFunction
                    .meta()
                    .arglists("(ns-aliases)")
                    .doc("Returns a map of the aliases defined in the current namespace.")
                    .examples(
                        "(ns-aliases)",
                        "(do                        \n" +
                        "  (ns-alias 'h 'hexdump)   \n" +
                        "  (ns-alias 'p 'parsatron) \n" +
                        "  (ns-aliases))            ")
                    .seeAlso(
                        "ns-alias", "ns-unalias", "*ns*", "ns")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                return Namespaces.getCurrentNamespace().listAliases();
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction ns_unalias =
        new VncFunction(
                "ns-unalias",
                VncFunction
                    .meta()
                    .arglists("(ns-unalias alias)")
                    .doc("Removes a namespace alias in the current namespace." )
                    .examples(
                        "(do                      \n" +
                        "  (ns-alias 'h 'hexdump) \n" +
                        "  (ns-unalias 'h))         ")
                    .seeAlso(
                        "ns-alias", "ns-aliases", "*ns*", "ns")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncSymbol alias = Coerce.toVncSymbol(args.first());

                Namespaces.getCurrentNamespace().removeAlias(alias.getName());

                return Nil;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    ///////////////////////////////////////////////////////////////////////////
    // Utilities
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction gensym =
        new VncFunction(
                "gensym",
                VncFunction
                    .meta()
                    .arglists("(gensym)", "(gensym prefix)")
                    .doc("Generates a symbol.")
                    .examples("(gensym)", "(gensym \"prefix_\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0, 1);

                return args.isEmpty()
                        ? GenSym.generate()
                        : GenSym.generate(
                            Types.isVncSymbol(args.first())
                                ? Coerce.toVncSymbol(args.first()).getName()
                                : Coerce.toVncString(args.first()).getValue());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction name =
        new VncFunction(
                "name",
                VncFunction
                    .meta()
                    .arglists("(name x)")
                    .doc("Returns the name string of a string, symbol, keyword, or function. "
                            + "If applied to a string it returns the string itself.")
                    .examples(
                        "(name 'foo)  ;; symbol ",
                        "(name 'user/foo)  ;; symbol ",
                        "(name (symbol \"user/foo\"))  ;; symbol ",
                        "(name :foo)  ;; keyword",
                        "(name :user/foo)  ;; keyword",
                        "(name +)  ;; function",
                        "(name str/digit?)  ;; function",
                        "(name \"ab/text\")  ;; string",
                        "(name (. :java.time.Month :JANUARY))  ;; java enum")
                    .seeAlso("qualified-name", "namespace", "fn-name")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal arg = args.first();

                if (arg == Nil) {
                    return Nil;
                }
                else if (Types.isVncKeyword(arg)) {
                    return new VncString(((VncKeyword)arg).getSimpleName());
                }
                else if (Types.isVncSymbol(arg)) {
                    return new VncString(((VncSymbol)arg).getSimpleName());
                }
                else if (Types.isVncString(arg)) {
                    return arg;
                }
                else if (Types.isVncFunction(arg) || Types.isVncMacro(arg)) {
                    return new VncString(((VncFunction)arg).getSimpleName());
                }
                else if (Types.isVncJavaObject(args.first()) && ((VncJavaObject)args.first()).isEnum()) {
                	final Enum<?> e = (Enum<?>)((VncJavaObject)args.first()).getDelegate();
                    return new VncString(e.name());
                }
                else {
                    throw new VncException(String.format(
                            "Function 'name' does not allow %s as parameter",
                            Types.getType(arg)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction qualified_name =
        new VncFunction(
                "qualified-name",
                VncFunction
                    .meta()
                    .arglists("(name x)")
                    .doc("Returns the qualified name String of a string, symbol, keyword, or function")
                    .examples(
                        "(qualified-name :user/x)",
                        "(qualified-name 'x)",
                        "(qualified-name \"x\")",
                        "(qualified-name str/digit?)")
                    .seeAlso("name", "namespace", "fn-name")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal arg = args.first();

                if (arg == Nil) {
                    return Nil;
                }
                else if (Types.isVncKeyword(arg)) {
                    return new VncString(((VncKeyword)arg).getQualifiedName());
                }
                else if (Types.isVncSymbol(arg)) {
                    return new VncString(((VncSymbol)arg).getQualifiedName());
                }
                else if (Types.isVncString(arg)) {
                    return arg;
                }
                else if (Types.isVncFunction(arg) || Types.isVncMacro(arg)) {
                    return new VncString(((VncFunction)arg).getQualifiedName());
                }
                else {
                    throw new VncException(String.format(
                            "Function 'qualified-name' does not allow %s as parameter",
                            Types.getType(arg)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction type =
        new VncFunction(
                "type",
                VncFunction
                    .meta()
                    .arglists("(type x)")
                    .doc("Returns the type of x.")
                    .examples(
                        "(type 5)",
                        "(type [1 2])",
                        "(type (. :java.math.BigInteger :valueOf 100))")
                    .seeAlso("supertype", "supertypes", "instance-of?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);
                return Types.getType(args.first());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction supertype =
        new VncFunction(
                "supertype",
                VncFunction
                    .meta()
                    .arglists("(supertype x)")
                    .doc("Returns the super type of x.")
                    .examples(
                        "(supertype 5)",
                        "(supertype [1 2])",
                        "(supertype (. :java.math.BigInteger :valueOf 100))")
                    .seeAlso("type", "supertypes", "instance-of?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);
                return Types.getSupertype(args.first());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction supertypes =
        new VncFunction(
                "supertypes",
                VncFunction
                    .meta()
                    .arglists("(supertypes x)")
                    .doc("Returns the super types of x.")
                    .examples(
                        "(supertypes 5)",
                        "(supertypes [1 2])",
                        "(supertypes (. :java.math.BigInteger :valueOf 100))")
                    .seeAlso("type", "supertype", "instance-of?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);
                return Types.getSupertypes(args.first());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction highlight =
        new VncFunction(
                "highlight",
                VncFunction
                    .meta()
                    .arglists("(highlight form)")
                    .doc(
                        "Syntax highlighting. Reads the form and returns a list of " +
                        "(token, token-class) tuples. \n\n" +
                        "Token classes: \n\n" +
                        "```\n"+
                        "   :comment                 ; .... \n" +
                        "   :whitespaces             \"  \", \"\\n\", \"  \\n\"  \n" +
                        "\n" +
                        "   :string                  \"lorem\", \"\"\"lorem\"\"\"  \n" +
                        "   :number                  100, 100I, 100.0, 100.23M  \n" +
                        "   :constant                nil, true, false  \n" +
                        "   :keyword                 :alpha  \n" +
                        "   :symbol                  alpha  \n" +
                        "   :symbol-special-form     def, loop, ...  \n" +
                        "   :symbol-function-name    +, println, ...  \n" +
                        "\n" +
                        "   :quote                   '  \n" +
                        "   :quasi-quote             `  \n" +
                        "   :unquote                 ~  \n" +
                        "   :unquote-splicing        ~@  \n" +
                        "\n" +
                        "   :meta                    ^private, ^{:arglist '() :doc \"....\"}\n" +
                        "   :at                      @  \n" +
                        "   :hash                    #  \n" +

                        "   :brace-begin             {  \n" +
                        "   :brace-end               {  \n" +
                        "   :bracket-begin           [  \n" +
                        "   :bracket-end             ]  \n" +
                        "   :parenthesis-begin       (  \n" +
                        "   :parenthesis-end         )  \n" +
                        "\n" +
                        "   :unknown                 anything that could not be classified\n" +
                        "```")
                    .examples(
                        "(highlight \"(+ 10 20)\")",
                        "(highlight \"(if (= 1 2) true false)\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final String form = Coerce.toVncString(args.first()).getValue();

                final Map<HighlightClass,VncKeyword> classMap =
                        Arrays.stream(HighlightClass.values())
                              .collect(Collectors.toMap(
                                            p -> p,
                                            p -> new VncKeyword(p.name().toLowerCase().replace('_', '-'))));

                VncList list = VncList.empty();
                for(HighlightItem it : HighlightParser.parse(form)) {
                    list = list.addAtEnd(
                                VncList.of(
                                    new VncString(it.getForm()),
                                    classMap.get(it.getClazz())));
                }

                return list;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };




    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    private static VncVal sort(
            final String fnName,
            final VncVal coll
        ) {
          return sort(
              fnName,
              coll,
              (x,y) -> Coerce
                    .toVncLong(compare.apply(VncList.of(x,y)))
                    .getIntValue());
        }

    private static VncVal sort(
            final String fnName,
            final VncVal coll,
            final Comparator<VncVal> c
    ) {
        if (Types.isVncVector(coll)) {
            return VncVector.ofList(
                    ((VncVector)coll)
                        .stream()
                        .sorted(c)
                        .collect(Collectors.toList()));
        }
        else if (Types.isVncSequence(coll)) {
            return VncList.ofList(
                    ((VncSequence)coll)
                        .stream()
                        .sorted(c)
                        .collect(Collectors.toList()));
        }
        else if (Types.isVncSet(coll)) {
            return VncList.ofList(
                    ((VncSet)coll)
                        .stream()
                        .sorted(c)
                        .collect(Collectors.toList()));
        }
        else if (Types.isVncMap(coll)) {
            return VncList.ofList(
                     ((VncMap)coll)
                         .toVncList()
                        .stream()
                        .sorted(c)
                        .collect(Collectors.toList()));
        }
        else {
            throw new VncException(String.format(
                    "%s: collection type %s not supported",
                    fnName, Types.getType(coll)));
        }
    }

    private static String renderCharLiteralsMarkdownTable() {
        // note: set an explicit column width to prevent line breaks
        //       in the first column!
        return "| Char Literal                      | Unicode | Char | \n" +
               "| [![width: 25%; text-align: left]] | :------ | :--- | \n" +
                VncChar
                    .symbols()
                    .entrySet()
                    .stream()
                    .map(e -> String.format(
                                    "| %s | %s | %s |",
                                    e.getKey(), //.replace("-", "\u2013"),  // en-dash
                                    e.getValue().toUnicode(),
                                    (e.getValue().getValue() == '¶')
                                        ? "#\\\\¶"
                                        : e.getValue().toString(true)))
                    .collect(Collectors.joining("\n"));
    }



    ///////////////////////////////////////////////////////////////////////////
    // types_ns is namespace of type functions
    ///////////////////////////////////////////////////////////////////////////

    public static final Map<VncVal, VncVal> ns =
            new SymbolMapBuilder()

                .add(nil_Q)
                .add(some_Q)
                .add(true_Q)
                .add(false_Q)
                .add(boolean_Q)
                .add(int_Q)
                .add(long_Q)
                .add(double_Q)
                .add(decimal_Q)
                .add(bigint_Q)
                .add(number_Q)
                .add(string_Q)
                .add(char_Q)
                .add(symbol)
                .add(symbol_Q)
                .add(qualified_symbol_Q)
                .add(keyword)
                .add(keyword_Q)

                .add(fn_Q)
                .add(macro_Q)
                .add(fn_name)
                .add(fn_about)
                .add(fn_body)
                .add(fn_pre_conditions)

                .add(just)
                .add(just_Q)

                .add(pr_str)
                .add(str)
                .add(to_str)
                .add(read_string)

                .add(lt)
                .add(lte)
                .add(gt)
                .add(gte)

                .add(equal_Q)
                .add(equal_strict_Q)
                .add(not_equal_strict_Q)
                .add(match_Q)
                .add(not_match_Q)

                // deprecated (just for compatibility)
                .add(match_Q)
                .add(not_match_Q)

                .add(boolean_cast)
                .add(char_cast)
                .add(int_cast)
                .add(long_cast)
                .add(double_cast)
                .add(decimal_cast)
                .add(bigint_cast)

                .add(char_escaped)
                .add(char_literals)

                .add(mutable_Q)

                .add(new_list)
                .add(new_list_ASTERISK)
                .add(list_Q)
                .add(new_mutable_list)
                .add(mutable_list_Q)
                .add(new_vector)
                .add(new_vector_ASTERISK)
                .add(vector_Q)
                .add(new_mutable_vector)
                .add(mutable_vector_Q)
                .add(new_lazy_seq)
                .add(lazyseq_Q)
                .add(map_Q)
                .add(map_entry_Q)
                .add(hash_map_Q)
                .add(ordered_map_Q)
                .add(sorted_map_Q)
                .add(mutable_map_Q)
                .add(stack_Q)
                .add(queue_Q)
                .add(delay_queue_Q)
                .add(new_hash_map)
                .add(new_ordered_map)
                .add(new_sorted_map)
                .add(new_mutable_map)
                .add(new_map_entry)
                .add(new_stack)
                .add(new_queue)
                .add(new_delay_queue)
                .add(assoc)
                .add(assoc_BANG)
                .add(assoc_in)
                .add(dissoc)
                .add(dissoc_BANG)
                .add(dissoc_in)
                .add(contains_Q)
                .add(not_contains_Q)
                .add(find)
                .add(get)
                .add(get_in)
                .add(key)
                .add(keys)
                .add(val)
                .add(vals)
                .add(entries)
                .add(select_keys)
                .add(update)
                .add(update_BANG)
                .add(update_in)
                .add(subvec)
                .add(sublist)
                .add(empty)

                .add(set_Q)
                .add(sorted_set_Q)
                .add(mutable_set_Q)
                .add(new_set)
                .add(new_sorted_set)
                .add(new_mutable_set)
                .add(distinct_Q)
                .add(difference)
                .add(union)
                .add(intersection)
                .add(subset_Q)
                .add(superset_Q)
                .add(juxt)
                .add(fnil)
                .add(shuffle)

                .add(split_at)
                .add(split_with)
                .add(into)
                .add(into_BANG)
                .add(sequential_Q)
                .add(coll_Q)
                .add(cons)
                .add(cons_BANG)
                .add(conj)
                .add(conj_BANG)
                .add(concat)
                .add(concatv)
                .add(interpose)
                .add(interleave)
                .add(cartesian_product)
                .add(combinations)
                .add(mapcat)
                .add(map_invert)
                .add(docoll)
                .add(doall)
                .add(nth)
                .add(first)
                .add(second)
                .add(third)
                .add(fourth)
                .add(last)
                .add(rest)
                .add(butlast)
                .add(nfirst)
                .add(nlast)
                .add(emptyToNil)
                .add(pop)
                .add(put_BANG)
                .add(take_BANG)
                .add(pop_BANG)
                .add(push_BANG)
                .add(poll_BANG)
                .add(offer_BANG)
                .add(peek)
                .add(empty_Q)
                .add(not_empty_Q)
                .add(every_Q)
                .add(not_every_Q)
                .add(any_Q)
                .add(not_any_Q)
                .add(every_pred)
                .add(any_pred)
                .add(count)
                .add(compare)
                .add(apply)
                .add(comp)
                .add(partial)
                .add(mapv)
                .add(partition)
                .add(partition_all)
                .add(partition_by)
                .add(filter_k)
                .add(filter_kv)
                .add(reduce)
                .add(reduce_kv)
                .add(replace)
                .add(group_by)
                .add(frequencies)
                .add(sort)
                .add(sort_by)
                .add(some)
                .add(map_keys)
                .add(map_vals)
                .add(run_BANG)

                .add(merge)
                .add(merge_with)
                .add(merge_deep)
                .add(disj)
                .add(seq)
                .add(repeat)
                .add(repeatedly)
                .add(cycle)

                .add(meta)
                .add(with_meta)
                .add(vary_meta)
                .add(alter_meta_BANG)
                .add(reset_meta_BANG)

                .add(identity)
                .add(gensym)
                .add(name)
                .add(qualified_name)
                .add(type)
                .add(supertype)
                .add(supertypes)
                .add(instance_of_Q)
                .add(highlight)

                .add(ns_alias)
                .add(ns_aliases)
                .add(ns_unalias)

                .toMap();
}
