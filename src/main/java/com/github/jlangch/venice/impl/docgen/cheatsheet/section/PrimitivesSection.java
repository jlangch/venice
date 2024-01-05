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
package com.github.jlangch.venice.impl.docgen.cheatsheet.section;

import com.github.jlangch.venice.impl.docgen.cheatsheet.DocItem;
import com.github.jlangch.venice.impl.docgen.cheatsheet.DocItemBuilder;
import com.github.jlangch.venice.impl.docgen.cheatsheet.DocSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.ISectionBuilder;


public class PrimitivesSection implements ISectionBuilder {

    public PrimitivesSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection("Primitives", "primitives");

        final DocSection lit = new DocSection("Literals", "primitives.literals");
        section.addSection(lit);
        lit.addLiteralItem("Nil",                  "nil",                                     id());
        lit.addLiteralItem("Boolean",              "true, false",                             id());
        lit.addLiteralItem("Integer",              "150I, 1_000_000I, 0x1FFI",                id());
        lit.addLiteralItem("Long",                 "1500, 1_000_000, 0x00A055FF",             id());
        lit.addLiteralItem("Double",               "3.569, 2.0E+10",                          id());
        lit.addLiteralItem("BigDecimal",           "6.897M, 2.345E+10M",                      id());
        lit.addLiteralItem("BigInteger",           "1000N, 1_000_000N",                       id());
        lit.addLiteralItem("Char",                 "#\\A, #\\π, #\\u03C0",                    id());
        lit.addLiteralItem("",                     "#\\space, #\\newline, #\\return, " +
                                                   "#\\tab, #\\formfeed, #\\backspace, " +
                                                   "#\\lparen, #\\rparen, #\\quote",          id());
        lit.addLiteralItem("String",               "\"abcd\", \"ab\\\"cd\", \"PI: \\u03C0\"", id());
        lit.addLiteralItem("",                     "\"\"\"{ \"age\": 42 }\"\"\"",             id());
        lit.addLiteralItem("String interpolation", "\"~{x}\", \"\"\"~{x}\"\"\"",              id());
        lit.addLiteralItem("",                     "\"~(inc x)\", \"\"\"~(inc x)\"\"\"",      id());


        final DocSection numbers = new DocSection("Numbers", "primitives.numbers");
        section.addSection(numbers);

        final DocSection arithmetic = new DocSection("Arithmetic", "primitives.arithmetic");
        numbers.addSection(arithmetic);
        arithmetic.addItem(diBuilder.getDocItem("+"));
        arithmetic.addItem(diBuilder.getDocItem("-"));
        arithmetic.addItem(diBuilder.getDocItem("*"));
        arithmetic.addItem(diBuilder.getDocItem("/"));

        final DocSection convert = new DocSection("Convert", "primitives.convert");
        numbers.addSection(convert);
        convert.addItem(diBuilder.getDocItem("int"));
        convert.addItem(diBuilder.getDocItem("long"));
        convert.addItem(diBuilder.getDocItem("double"));
        convert.addItem(diBuilder.getDocItem("decimal"));
        convert.addItem(diBuilder.getDocItem("bigint"));

        final DocSection compare = new DocSection("Compare", "primitives.compare");
        numbers.addSection(compare);
        compare.addItem(diBuilder.getDocItem("=="));
        compare.addItem(diBuilder.getDocItem("="));
        compare.addItem(diBuilder.getDocItem("not="));
        compare.addItem(diBuilder.getDocItem("<"));
        compare.addItem(diBuilder.getDocItem(">"));
        compare.addItem(diBuilder.getDocItem("<="));
        compare.addItem(diBuilder.getDocItem(">="));
        compare.addItem(diBuilder.getDocItem("compare"));

        final DocSection test = new DocSection("Test", "primitives.test");
        numbers.addSection(test);
        test.addItem(diBuilder.getDocItem("zero?"));
        test.addItem(diBuilder.getDocItem("pos?"));
        test.addItem(diBuilder.getDocItem("neg?"));
        test.addItem(diBuilder.getDocItem("even?"));
        test.addItem(diBuilder.getDocItem("odd?"));
        test.addItem(diBuilder.getDocItem("number?"));
        test.addItem(diBuilder.getDocItem("int?"));
        test.addItem(diBuilder.getDocItem("long?"));
        test.addItem(diBuilder.getDocItem("double?"));
        test.addItem(diBuilder.getDocItem("decimal?"));

        final DocSection nan = new DocSection("NaN/Infinite", "primitives.nan");
        numbers.addSection(nan);
        nan.addItem(diBuilder.getDocItem("nan?"));
        nan.addItem(diBuilder.getDocItem("infinite?"));

        final DocSection bigdecimal = new DocSection("BigDecimal", "primitives.bigdecimal");
        numbers.addSection(bigdecimal);
        bigdecimal.addItem(diBuilder.getDocItem("dec/add"));
        bigdecimal.addItem(diBuilder.getDocItem("dec/sub"));
        bigdecimal.addItem(diBuilder.getDocItem("dec/mul"));
        bigdecimal.addItem(diBuilder.getDocItem("dec/div"));
        bigdecimal.addItem(diBuilder.getDocItem("dec/scale"));



        final DocSection strings = new DocSection("Strings", "primitives.strings");
        section.addSection(strings);

        final DocSection create = new DocSection("Create", "primitives.strings.create");
        strings.addSection(create);
        create.addItem(diBuilder.getDocItem("str"));

        final DocSection use = new DocSection("Use", "primitives.strings.use");
        strings.addSection(use);
        use.addItem(diBuilder.getDocItem("count"));
        use.addItem(diBuilder.getDocItem("compare"));
        use.addItem(diBuilder.getDocItem("empty-to-nil"));
        use.addItem(diBuilder.getDocItem("first"));
        use.addItem(diBuilder.getDocItem("last"));
        use.addItem(diBuilder.getDocItem("nth"));
        use.addItem(diBuilder.getDocItem("nfirst"));
        use.addItem(diBuilder.getDocItem("nlast"));
        use.addItem(diBuilder.getDocItem("seq"));
        use.addItem(diBuilder.getDocItem("rest"));
        use.addItem(diBuilder.getDocItem("butlast"));
        use.addItem(diBuilder.getDocItem("reverse"));
        use.addItem(diBuilder.getDocItem("shuffle"));
        use.addItem(diBuilder.getDocItem("str/subs"));
        use.addItem(diBuilder.getDocItem("str/nfirst"));
        use.addItem(diBuilder.getDocItem("str/nlast"));
        use.addItem(diBuilder.getDocItem("str/rest"));
        use.addItem(diBuilder.getDocItem("str/nrest"));
        use.addItem(diBuilder.getDocItem("str/butlast"));
        use.addItem(diBuilder.getDocItem("str/butnlast"));
        use.addItem(diBuilder.getDocItem("str/chars"));
        use.addItem(diBuilder.getDocItem("str/pos"));
        use.addItem(diBuilder.getDocItem("str/repeat"));
        use.addItem(diBuilder.getDocItem("str/reverse"));
        use.addItem(diBuilder.getDocItem("str/lorem-ipsum"));

        final DocSection index = new DocSection("Index", "primitives.strings.index");
        strings.addSection(index);
        index.addItem(diBuilder.getDocItem("str/index-of"));
        index.addItem(diBuilder.getDocItem("str/index-one-char-of"));
        index.addItem(diBuilder.getDocItem("str/index-one-char-not-of"));
        index.addItem(diBuilder.getDocItem("str/last-index-of"));

        final DocSection split = new DocSection("Split/Join", "primitives.strings.splitjoin");
        strings.addSection(split);
        split.addItem(diBuilder.getDocItem("str/split"));
        split.addItem(diBuilder.getDocItem("str/split-at"));
        split.addItem(diBuilder.getDocItem("str/split-lines"));
        split.addItem(diBuilder.getDocItem("str/split-columns"));
        split.addItem(diBuilder.getDocItem("str/join"));

        final DocSection replace = new DocSection("Replace", "primitives.strings.replace");
        strings.addSection(replace);
        replace.addItem(diBuilder.getDocItem("str/replace-first"));
        replace.addItem(diBuilder.getDocItem("str/replace-last"));
        replace.addItem(diBuilder.getDocItem("str/replace-all"));

        final DocSection strip = new DocSection("Strip", "primitives.strings.strip");
        strings.addSection(strip);
        strip.addItem(diBuilder.getDocItem("str/strip-start"));
        strip.addItem(diBuilder.getDocItem("str/strip-end"));
        strip.addItem(diBuilder.getDocItem("str/strip-indent"));
        strip.addItem(diBuilder.getDocItem("str/strip-margin"));

        final DocSection conv = new DocSection("Conversion", "primitives.strings.conversion");
        strings.addSection(conv);
        conv.addItem(diBuilder.getDocItem("str/lower-case"));
        conv.addItem(diBuilder.getDocItem("str/upper-case"));
        conv.addItem(diBuilder.getDocItem("str/cr-lf", false));

        final DocSection regex = new DocSection("Regex", "primitives.strings.regex");
        strings.addSection(regex);
        regex.addItem(diBuilder.getDocItem("match?"));
        regex.addItem(diBuilder.getDocItem("not-match?"));

        final DocSection trim = new DocSection("Trim", "primitives.strings.trim");
        strings.addSection(trim);
        trim.addItem(diBuilder.getDocItem("str/trim"));
        trim.addItem(diBuilder.getDocItem("str/trim-to-nil"));
        trim.addItem(diBuilder.getDocItem("str/trim-left"));
        trim.addItem(diBuilder.getDocItem("str/trim-right"));

        final DocSection format = new DocSection("Format", "primitives.strings.format");
        strings.addSection(format);
        format.addItem(diBuilder.getDocItem("str/format"));
        format.addItem(diBuilder.getDocItem("str/quote"));
        format.addItem(diBuilder.getDocItem("str/double-quote"));
        format.addItem(diBuilder.getDocItem("str/double-unquote"));
        format.addItem(diBuilder.getDocItem("str/align"));
        format.addItem(diBuilder.getDocItem("str/wrap"));
        format.addItem(diBuilder.getDocItem("str/expand"));
        format.addItem(diBuilder.getDocItem("str/truncate"));

        final DocSection hex = new DocSection("Hex", "primitives.strings.hex");
        strings.addSection(hex);
        hex.addItem(diBuilder.getDocItem("str/hex-to-bytebuf"));
        hex.addItem(diBuilder.getDocItem("str/bytebuf-to-hex"));
        hex.addItem(diBuilder.getDocItem("str/format-bytebuf"));

        final DocSection encode = new DocSection("Encode/Decode", "primitives.strings.encode");
        strings.addSection(encode);
        encode.addItem(diBuilder.getDocItem("str/encode-base64"));
        encode.addItem(diBuilder.getDocItem("str/decode-base64"));
        encode.addItem(diBuilder.getDocItem("str/encode-url"));
        encode.addItem(diBuilder.getDocItem("str/decode-url"));
        encode.addItem(diBuilder.getDocItem("str/escape-html"));
        encode.addItem(diBuilder.getDocItem("str/escape-xml"));

        final DocSection str_test = new DocSection("Test", "primitives.strings.test");
        strings.addSection(str_test);
        str_test.addItem(diBuilder.getDocItem("string?"));
        str_test.addItem(diBuilder.getDocItem("empty?"));
        str_test.addItem(diBuilder.getDocItem("not-empty?"));
        str_test.addItem(diBuilder.getDocItem("str/blank?"));
        str_test.addItem(diBuilder.getDocItem("str/not-blank?"));
        str_test.addItem(diBuilder.getDocItem("str/starts-with?"));
        str_test.addItem(diBuilder.getDocItem("str/ends-with?"));
        str_test.addItem(diBuilder.getDocItem("str/contains?"));
        str_test.addItem(diBuilder.getDocItem("str/equals-ignore-case?"));
        str_test.addItem(diBuilder.getDocItem("str/quoted?"));
        str_test.addItem(diBuilder.getDocItem("str/double-quoted?"));

        final DocSection str_test_char = new DocSection("Test char", "primitives.strings.testchar");
        strings.addSection(str_test_char);
        str_test_char.addItem(diBuilder.getDocItem("str/char?"));
        str_test_char.addItem(diBuilder.getDocItem("str/digit?"));
        str_test_char.addItem(diBuilder.getDocItem("str/hexdigit?"));
        str_test_char.addItem(diBuilder.getDocItem("str/letter?"));
        str_test_char.addItem(diBuilder.getDocItem("str/whitespace?"));
        str_test_char.addItem(diBuilder.getDocItem("str/linefeed?"));
        str_test_char.addItem(diBuilder.getDocItem("str/lower-case?"));
        str_test_char.addItem(diBuilder.getDocItem("str/upper-case?"));

        final DocSection validation = new DocSection("Validation", "primitives.strings.validation");
        strings.addSection(validation);
        validation.addItem(diBuilder.getDocItem("str/valid-email-addr?"));

        final DocSection str_leven_char = new DocSection("Other", "primitives.strings.other");
        strings.addSection(str_leven_char);
        str_leven_char.addItem(diBuilder.getDocItem("str/levenshtein"));


        final DocSection chars = new DocSection("Chars", "primitives.chars");
        section.addSection(chars);

        final DocSection charuse = new DocSection("Use", id());
        chars.addSection(charuse);
        charuse.addItem(diBuilder.getDocItem("char"));
        charuse.addItem(diBuilder.getDocItem("char?"));
        charuse.addItem(diBuilder.getDocItem("char-escaped"));
        charuse.addItem(diBuilder.getDocItem("char-literals", false));

        final DocSection charconv = new DocSection("Conversion", "primitives.chars.conversion");
        chars.addSection(charconv);
        charconv.addItem(diBuilder.getDocItem("str"));
        charconv.addItem(diBuilder.getDocItem("str/lower-case"));
        charconv.addItem(diBuilder.getDocItem("str/upper-case"));

        final DocSection chartest = new DocSection("Test char", "primitives.chars.test");
        chars.addSection(chartest);
        chartest.addItem(diBuilder.getDocItem("str/char?"));
        chartest.addItem(diBuilder.getDocItem("str/digit?"));
        chartest.addItem(diBuilder.getDocItem("str/letter?"));
        chartest.addItem(diBuilder.getDocItem("str/whitespace?"));
        chartest.addItem(diBuilder.getDocItem("str/linefeed?"));
        chartest.addItem(diBuilder.getDocItem("str/lower-case?"));
        chartest.addItem(diBuilder.getDocItem("str/upper-case?"));


        final DocSection bool_ = new DocSection("Booleans", "primitives.booleans");
        section.addSection(bool_);
        final DocSection bool = new DocSection("Boolean", id());
        bool_.addSection(bool);
        bool.addItem(new DocItem("true false", null));
        bool.addItem(diBuilder.getDocItem("boolean"));
        bool.addItem(diBuilder.getDocItem("not"));
        bool.addItem(diBuilder.getDocItem("boolean?"));
        bool.addItem(diBuilder.getDocItem("true?"));
        bool.addItem(diBuilder.getDocItem("false?"));


        final DocSection keywords_ = new DocSection("Keywords", "primitives.keywords");
        section.addSection(keywords_);
        final DocSection keywords = new DocSection("Keyword", id());
        keywords_.addSection(keywords);
        keywords.addItem(new DocItem(":a :blue", null));
        keywords.addItem(diBuilder.getDocItem("keyword?"));
        keywords.addItem(diBuilder.getDocItem("keyword"));


        final DocSection symbols_ = new DocSection("Symbols", "primitives.symbols");
        section.addSection(symbols_);
        final DocSection symbols = new DocSection("Symbol", id());
        symbols_.addSection(symbols);
        symbols.addItem(new DocItem("'a 'blue", null));
        symbols.addItem(diBuilder.getDocItem("symbol?"));
        symbols.addItem(diBuilder.getDocItem("qualified-symbol?"));
        symbols.addItem(diBuilder.getDocItem("symbol"));


        final DocSection nil_ = new DocSection("Nil", "primitives.nil");
        section.addSection(nil_);
        final DocSection nil = new DocSection("Nil", id());
        nil_.addSection(nil);
        nil.addItem(new DocItem("nil", null));
        nil.addItem(diBuilder.getDocItem("nil?"));
        nil.addItem(diBuilder.getDocItem("some?"));


        final DocSection just_ = new DocSection("Just", "primitives.just");
        section.addSection(just_);
        final DocSection just = new DocSection("Just", id());
        just_.addSection(just);
        just.addItem(diBuilder.getDocItem("just"));
        just.addItem(diBuilder.getDocItem("just?"));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
