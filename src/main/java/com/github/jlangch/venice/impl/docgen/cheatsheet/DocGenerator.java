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
package com.github.jlangch.venice.impl.docgen.cheatsheet;

import static com.github.jlangch.venice.impl.VeniceClasspath.getVeniceBasePath;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.RunMode;
import com.github.jlangch.venice.impl.VeniceInterpreter;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.repl.ReplFunctions;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.io.ClassPathResource;
import com.github.jlangch.venice.impl.util.markdown.Markdown;
import com.github.jlangch.venice.impl.util.markdown.renderer.html.HtmlRenderer;
import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;
import com.lowagie.text.pdf.PdfReader;


public class DocGenerator {

    public DocGenerator(final boolean runExamples) {
        final List<String> preloadedModules = new ArrayList<>();

        preloadedModules
            .addAll(Arrays.asList(
                        "app",    "xml",      "crypt",     "gradle",
                        "trace",  "ansi",     "maven",     "kira",
                        "java",   "semver",   "excel",     "hexdump",
                        "shell",  "geoip",    "benchmark", "component",
                        "config", "parsifal", "grep"));

        final Env docEnv = new VeniceInterpreter(new AcceptAllInterceptor())
                            .createEnv(
                                preloadedModules,
                                false,
                                false,
                                RunMode.DOCGEN)
                            .setStdoutPrintStream(null)
                            .setStderrPrintStream(null);

        // make REPL specific functions available (e.g: 'repl/info')
        final Env env = ReplFunctions.register(docEnv, null, null);

        this.diBuilder = new DocItemBuilder(
                                env,
                                new DocHighlighter(DocColorTheme.getLightTheme()),
                                preloadedModules,
                                runExamples);
    }

    public static List<DocSection> docInfo() {
        return new DocGenerator(false).buildDocInfo();
    }

    public void run(final String version) {
        try {
            System.out.println("Creating cheatsheet V" + version);

            final List<DocSection> left = getLeftSections();
            final List<DocSection> right = getRightSections();
            final List<DocSection> leftModules = getModulesLeftSections();
            final List<DocSection> rightModules = getModulesRightSections();
            final List<MarkdownDoc> topics = getTopics();

            validateUniqueSectionsId(left, right);

            final Map<String,Object> data = new HashMap<>();
            data.put("meta-author", "Venice");
            data.put("version", version);
            data.put("toc", getTOC());
            data.put("left", left);
            data.put("right", right);
            data.put("left-modules", leftModules);
            data.put("right-modules", rightModules);
            data.put("details", getDocItems(concat(left, right, leftModules, rightModules)));
            data.put("snippets", new CodeSnippetReader().readSnippets());
            data.put("topics", topics);

            // [1] create a HTML
            data.put("pdfmode", false);
            final String html = CheatsheetRenderer.renderXHTML(data);
            save(new File(getUserDir(), "cheatsheet.html"), html);

            // [2] create a PDF
            data.put("pdfmode", true);
            final String xhtml = CheatsheetRenderer.renderXHTML(data);
            final ByteBuffer pdf = CheatsheetRenderer.renderPDF(xhtml);
            final byte[] pdfArr =  pdf.array();
            save(new File(getUserDir(), "cheatsheet.pdf"), pdfArr);

            final PdfReader reader = new PdfReader(pdf.array());
            final int pages = reader.getNumberOfPages();
            reader.close();

            System.out.println(String.format(
                    "Generated Cheat Sheet at: %s. XHTML: %dKB, PDF: %dKB / %d pages",
                    getUserDir(),
                    xhtml.length() / 1024,
                    pdfArr.length / 1024,
                    pages));
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private List<DocSection> buildDocInfo() {
        final List<DocSection> sections = new ArrayList<>();
        sections.addAll(getLeftSections());
        sections.addAll(getRightSections());
        return sections;
    }

    private List<DocSection> getTOC() {
        final List<DocSection> content = new ArrayList<>();

        final DocSection primitives = new DocSection("Primitives", "primitives");
        primitives.addSection(new DocSection("Literals", "primitives.literals"));
        primitives.addSection(new DocSection("Numbers", "primitives.numbers"));
        primitives.addSection(new DocSection("Strings", "primitives.strings"));
        primitives.addSection(new DocSection("Chars", "primitives.chars"));
        primitives.addSection(new DocSection("Other", "primitives.other"));
        content.add(primitives);

        final DocSection collections = new DocSection("Collections", "collections");
        collections.addSection(new DocSection("List", "collections.lists"));
        collections.addSection(new DocSection("Vector", "collections.vectors"));
        collections.addSection(new DocSection("Set", "collections.sets"));
        collections.addSection(new DocSection("Map", "collections.maps"));
        collections.addSection(new DocSection("LazySeq", "lazyseq"));
        collections.addSection(new DocSection("Stack", "collections.stack"));
        collections.addSection(new DocSection("Queue", "collections.queue"));
        collections.addSection(new DocSection("DAG", "collections.dag"));
        collections.addSection(new DocSection("Array", "arrays"));
        collections.addSection(new DocSection("ByteBuf", "bytebuf"));
        content.add(collections);

        final DocSection datatypes = new DocSection("Custom\u00A0Types", "datatypes");
        datatypes.addSection(new DocSection("Types", "types"));
        datatypes.addSection(new DocSection("Protocols", "protocols"));
        content.add(datatypes);

        final DocSection functions = new DocSection("Core\u00A0Functions", "functions");
        functions.addSection(new DocSection("Functions", "functions"));
        functions.addSection(new DocSection("Macros", "macros"));
        functions.addSection(new DocSection("Special\u00A0Forms", "specialforms"));
        functions.addSection(new DocSection("Transducers", "transducers"));
        functions.addSection(new DocSection("Namespaces", "namespace"));
        functions.addSection(new DocSection("Exceptions", "exceptions"));
        content.add(functions);

        final DocSection concurrency = new DocSection("Concurrency", "concurrency");
        concurrency.addSection(new DocSection("Atoms", "concurrency.atoms"));
        concurrency.addSection(new DocSection("Futures", "concurrency.futures"));
        concurrency.addSection(new DocSection("Promises", "concurrency.promises"));
        concurrency.addSection(new DocSection("Delay", "concurrency.delay"));
        concurrency.addSection(new DocSection("Agents", "concurrency.agents"));
        concurrency.addSection(new DocSection("Scheduler", "concurrency.scheduler"));
        concurrency.addSection(new DocSection("Locking", "concurrency.locking"));
        concurrency.addSection(new DocSection("Volatiles", "concurrency.volatiles"));
        concurrency.addSection(new DocSection("Parallel", "concurrency.parallel"));
        content.add(concurrency);

        final DocSection threads = new DocSection("Threads", "concurrency.threads");
        threads.addSection(new DocSection("ThreadLocal", "concurrency.threadlocal"));
        threads.addSection(new DocSection("Threads", "concurrency.threads"));
        content.add(threads);

        final DocSection system = new DocSection("System\u00A0&\u00A0Java", "system");
        system.addSection(new DocSection("System", "system"));
        system.addSection(new DocSection("System\u00A0Vars", "sysvars"));
        system.addSection(new DocSection("Java\u00A0Interop", "javainterop"));
        system.addSection(new DocSection("REPL", "repl"));
        content.add(system);

        final DocSection util = new DocSection("Util", "util");
        util.addSection(new DocSection("Math", "math"));
        util.addSection(new DocSection("Time", "time"));
        util.addSection(new DocSection("Regex", "regex"));
        util.addSection(new DocSection("CIDR", "cidr"));

        content.add(util);

        final DocSection io = new DocSection("I/O", "io");
        io.addSection(new DocSection("I/O", "io.util"));
        io.addSection(new DocSection("File", "io.file"));
        io.addSection(new DocSection("Zip/GZip", "io.zip"));
        content.add(io);

        final DocSection documents = new DocSection("Documents", "miscellaneous");
        documents.addSection(new DocSection("JSON", "json"));
        documents.addSection(new DocSection("PDF", "pdf"));
        documents.addSection(new DocSection("PDF Tools", "pdf.pdftools"));
        documents.addSection(new DocSection("CSV", "csv"));
        documents.addSection(new DocSection("XML", "modules.xml"));
        documents.addSection(new DocSection("Excel", "modules.excel"));
        content.add(documents);

        final DocSection extmod = new DocSection("Modules", "modules");
        extmod.addSection(new DocSection("Kira\u00A0Templates", "modules.kira"));
        extmod.addSection(new DocSection("Parsifal", "modules.parsifal"));
        extmod.addSection(new DocSection("Configuration", "modules.config"));
        extmod.addSection(new DocSection("Component", "modules.component"));
        extmod.addSection(new DocSection("XML", "modules.xml"));
        extmod.addSection(new DocSection("Grep", "modules.grep"));
        extmod.addSection(new DocSection("Cryptography", "modules.cryptography"));
        extmod.addSection(new DocSection("Java", "modules.java"));
        extmod.addSection(new DocSection("Semver", "modules.semver"));
        extmod.addSection(new DocSection("Hexdump", "modules.hexdump"));
        extmod.addSection(new DocSection("Shell", "modules.shell"));
        extmod.addSection(new DocSection("Geo IP", "modules.geoip"));
        extmod.addSection(new DocSection("Ansi", "modules.ansi"));
        extmod.addSection(new DocSection("Gradle", "modules.gradle"));
        extmod.addSection(new DocSection("Maven", "modules.maven"));
        extmod.addSection(new DocSection("Tracing", "modules.tracing"));
        extmod.addSection(new DocSection("Benchmark", "modules.benchmark"));
        extmod.addSection(new DocSection("App", "modules.app"));
        content.add(extmod);

        final DocSection others = new DocSection("Others", "others");
        others.addSection(new DocSection("Embedding in Java", "embedding"));
        others.addSection(new DocSection("Venice Doc", "venicedoc"));
        others.addSection(new DocSection("Markdown", "markdown"));
        content.add(others);

        return content;
    }

    private List<MarkdownDoc> getTopics() {
        final List<MarkdownDoc> topics = new ArrayList<>();

        topics.add(new MarkdownDoc(
                        "VeniceDoc",
                        new HtmlRenderer().render(loadVeniceDocMarkdown()),
                        "venicedoc"));

        topics.add(new MarkdownDoc(
                        "Markdown",
                        new HtmlRenderer().render(loadMarkdownDoc()),
                        "markdown"));

        return topics;
    }

    private List<DocSection> getLeftSections() {
        return Arrays.asList(
                getPrimitivesSection(),
                getByteBufSection(),
                getRegexSection(),
                getMathSection(),
                getTransducersSection(),
                getFunctionsSection(),
                getMacrosSection(),
                getSpecialFormsSection(),
                getExceptionsSection(),
                getTypesSection(),
                getProtocolsSection(),
                getNamespaceSection(),
                getJavaInteropSection(),
                getReplSection(),
                getPdfSection(),
                getIOZipSection());
    }

    private List<DocSection> getRightSections() {
        return Arrays.asList(
                getCollectionsSection(),
                getLazySequencesSection(),
                getArraysSection(),
                getConcurrencySection(),
                getSystemSection(),
                getSystemVarSection(),
                getTimeSection(),
                getIOSection(),
                getIOFileSection(),
                getJsonSection(),
                getCidrSection(),
                getAppSection(),
                getCsvSection());
    }

    private List<DocSection> getModulesLeftSections() {
        return Arrays.asList(
                getModuleKiraSection(),
                getModuleCryptographySection(),
                getModuleXmlSection(),
                getModuleJavaSection(),
                getModuleParsifalSection(),
                getModuleGradleSection(),
                getModuleMavenSection(),
                getModuleTracingSection(),
                getModuleShellSection(),
                getModuleAnsiSection(),
                getModuleGrepSection());
    }

    private List<DocSection> getModulesRightSections() {
        return Arrays.asList(
                getModuleHexdumpSection(),
                getModuleSemverSection(),
                getModuleGeoipSection(),
                getModuleExcelSection(),
                getModuleConfigSection(),
                getModuleComponentSection(),
                getModuleAppSection(),
                getModuleBenchmarkSection());
    }

    private List<DocItem> getDocItems(final List<DocSection> sections) {
        return sections
                .stream()
                .map(s -> s.getSections())
                .flatMap(List::stream)
                .map(s -> s.getSections())
                .flatMap(List::stream)
                .map(s -> s.getItems())
                .flatMap(List::stream)
                .filter(i -> !StringUtil.isBlank(i.getName()))
                .distinct()
                .sorted(Comparator.comparing(DocItem::getName))
                .collect(Collectors.toList());
    }

    private DocSection getPrimitivesSection() {
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
        create.addItem(diBuilder.getDocItem("str/format"));
        create.addItem(diBuilder.getDocItem("str/quote"));
        create.addItem(diBuilder.getDocItem("str/double-quote"));
        create.addItem(diBuilder.getDocItem("str/double-unquote"));

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
        use.addItem(diBuilder.getDocItem("str/index-of"));
        use.addItem(diBuilder.getDocItem("str/last-index-of"));
        use.addItem(diBuilder.getDocItem("str/subs"));
        use.addItem(diBuilder.getDocItem("str/nfirst"));
        use.addItem(diBuilder.getDocItem("str/nlast"));
        use.addItem(diBuilder.getDocItem("str/rest"));
        use.addItem(diBuilder.getDocItem("str/butlast"));
        use.addItem(diBuilder.getDocItem("str/chars"));
        use.addItem(diBuilder.getDocItem("str/pos"));
        use.addItem(diBuilder.getDocItem("str/repeat"));
        use.addItem(diBuilder.getDocItem("str/reverse"));
        use.addItem(diBuilder.getDocItem("str/truncate"));
        use.addItem(diBuilder.getDocItem("str/expand"));
        use.addItem(diBuilder.getDocItem("str/lorem-ipsum"));

        final DocSection split = new DocSection("Split/Join", "primitives.strings.splitjoin");
        strings.addSection(split);
        split.addItem(diBuilder.getDocItem("str/split"));
        split.addItem(diBuilder.getDocItem("str/split-lines"));
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


        final DocSection validation = new DocSection("Validation", "primitives.strings.validation");
        strings.addSection(validation);
        validation.addItem(diBuilder.getDocItem("str/valid-email-addr?"));

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

        final DocSection str_leven_char = new DocSection("Other", "primitives.strings.other");
        strings.addSection(str_leven_char);
        str_leven_char.addItem(diBuilder.getDocItem("str/levenshtein"));


        final DocSection chars = new DocSection("Chars", "primitives.chars");
        section.addSection(chars);

        final DocSection charuse = new DocSection("Use", id());
        chars.addSection(charuse);
        charuse.addItem(diBuilder.getDocItem("char"));
        charuse.addItem(diBuilder.getDocItem("char?"));
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


        final DocSection other = new DocSection("Other", "primitives.other");
        section.addSection(other);

        final DocSection nil = new DocSection("Nil", id());
        other.addSection(nil);
        nil.addItem(diBuilder.getDocItem("nil?"));
        nil.addItem(diBuilder.getDocItem("some?"));


        final DocSection keywords = new DocSection("Keywords", "primitives.other.keywords");
        other.addSection(keywords);
        keywords.addItem(new DocItem(":a :blue", null));
        keywords.addItem(diBuilder.getDocItem("keyword?"));
        keywords.addItem(diBuilder.getDocItem("keyword"));

        final DocSection symbols = new DocSection("Symbols", "primitives.other.symbols");
        other.addSection(symbols);
        symbols.addItem(new DocItem("'a 'blue", null));
        symbols.addItem(diBuilder.getDocItem("symbol?"));
        symbols.addItem(diBuilder.getDocItem("symbol"));

        final DocSection just = new DocSection("Just", "primitives.other.just");
        other.addSection(just);
        just.addItem(diBuilder.getDocItem("just"));
        just.addItem(diBuilder.getDocItem("just?"));

        final DocSection boolean_ = new DocSection("Boolean", "primitives.other.boolean");
        other.addSection(boolean_);
        boolean_.addItem(diBuilder.getDocItem("boolean"));
        boolean_.addItem(diBuilder.getDocItem("not"));
        boolean_.addItem(diBuilder.getDocItem("boolean?"));
        boolean_.addItem(diBuilder.getDocItem("true?"));
        boolean_.addItem(diBuilder.getDocItem("false?"));

        return section;
    }

    private DocSection getCollectionsSection() {
        final DocSection section = new DocSection("Collections", "collections");


        final DocSection collections = new DocSection("Collections", "collections.collections");
        section.addSection(collections);

        final DocSection generic = new DocSection("Generic", "collections.collections.generic");
        collections.addSection(generic);
        generic.addItem(diBuilder.getDocItem("count"));
        generic.addItem(diBuilder.getDocItem("compare"));
        generic.addItem(diBuilder.getDocItem("empty-to-nil"));
        generic.addItem(diBuilder.getDocItem("empty"));
        generic.addItem(diBuilder.getDocItem("into"));
        generic.addItem(diBuilder.getDocItem("cons"));
        generic.addItem(diBuilder.getDocItem("conj"));
        generic.addItem(diBuilder.getDocItem("remove"));
        generic.addItem(diBuilder.getDocItem("repeat"));
        generic.addItem(diBuilder.getDocItem("repeatedly"));
        generic.addItem(diBuilder.getDocItem("cycle"));
        generic.addItem(diBuilder.getDocItem("replace"));
        generic.addItem(diBuilder.getDocItem("range"));
        generic.addItem(diBuilder.getDocItem("group-by"));
        generic.addItem(diBuilder.getDocItem("frequencies"));
        generic.addItem(diBuilder.getDocItem("get-in"));
        generic.addItem(diBuilder.getDocItem("seq"));
        generic.addItem(diBuilder.getDocItem("reverse"));
        generic.addItem(diBuilder.getDocItem("shuffle"));

        final DocSection coll_test = new DocSection("Tests", "collections.collections.tests");
        collections.addSection(coll_test);
        coll_test.addItem(diBuilder.getDocItem("empty?"));
        coll_test.addItem(diBuilder.getDocItem("not-empty?"));
        coll_test.addItem(diBuilder.getDocItem("coll?"));
        coll_test.addItem(diBuilder.getDocItem("list?"));
        coll_test.addItem(diBuilder.getDocItem("vector?"));
        coll_test.addItem(diBuilder.getDocItem("set?"));
        coll_test.addItem(diBuilder.getDocItem("sorted-set?"));
        coll_test.addItem(diBuilder.getDocItem("mutable-set?"));
        coll_test.addItem(diBuilder.getDocItem("map?"));
        coll_test.addItem(diBuilder.getDocItem("sequential?"));
        coll_test.addItem(diBuilder.getDocItem("hash-map?"));
        coll_test.addItem(diBuilder.getDocItem("ordered-map?"));
        coll_test.addItem(diBuilder.getDocItem("sorted-map?"));
        coll_test.addItem(diBuilder.getDocItem("mutable-map?"));
        coll_test.addItem(diBuilder.getDocItem("bytebuf?"));

        final DocSection coll_process = new DocSection("Process", "collections.collections.process");
        collections.addSection(coll_process);
        coll_process.addItem(diBuilder.getDocItem("map"));
        coll_process.addItem(diBuilder.getDocItem("map-indexed"));
        coll_process.addItem(diBuilder.getDocItem("filter"));
        coll_process.addItem(diBuilder.getDocItem("reduce"));
        coll_process.addItem(diBuilder.getDocItem("keep"));
        coll_process.addItem(diBuilder.getDocItem("docoll"));


        final DocSection lists = new DocSection("Lists", "collections.lists");
        section.addSection(lists);

        final DocSection list_create = new DocSection("Create", "collections.lists.create");
        lists.addSection(list_create);
        list_create.addItem(diBuilder.getDocItem("()"));
        list_create.addItem(diBuilder.getDocItem("list"));
        list_create.addItem(diBuilder.getDocItem("list*"));
        list_create.addItem(diBuilder.getDocItem("mutable-list"));

        final DocSection list_access = new DocSection("Access", "collections.lists.access");
        lists.addSection(list_access);
        list_access.addItem(diBuilder.getDocItem("first"));
        list_access.addItem(diBuilder.getDocItem("second"));
        list_access.addItem(diBuilder.getDocItem("third"));
        list_access.addItem(diBuilder.getDocItem("fourth"));
        list_access.addItem(diBuilder.getDocItem("nth"));
        list_access.addItem(diBuilder.getDocItem("last"));
        list_access.addItem(diBuilder.getDocItem("peek"));
        list_access.addItem(diBuilder.getDocItem("rest"));
        list_access.addItem(diBuilder.getDocItem("butlast"));
        list_access.addItem(diBuilder.getDocItem("nfirst"));
        list_access.addItem(diBuilder.getDocItem("nlast"));
        list_access.addItem(diBuilder.getDocItem("sublist"));
        list_access.addItem(diBuilder.getDocItem("some"));

        final DocSection list_modify = new DocSection("Modify", "collections.lists.modify");
        lists.addSection(list_modify);
        list_modify.addItem(diBuilder.getDocItem("cons"));
        list_modify.addItem(diBuilder.getDocItem("conj"));
        list_modify.addItem(diBuilder.getDocItem("rest"));
        list_modify.addItem(diBuilder.getDocItem("pop"));
        list_modify.addItem(diBuilder.getDocItem("into"));
        list_modify.addItem(diBuilder.getDocItem("concat"));
        list_modify.addItem(diBuilder.getDocItem("distinct"));
        list_modify.addItem(diBuilder.getDocItem("dedupe"));
        list_modify.addItem(diBuilder.getDocItem("partition"));
        list_modify.addItem(diBuilder.getDocItem("partition-by"));
        list_modify.addItem(diBuilder.getDocItem("interpose"));
        list_modify.addItem(diBuilder.getDocItem("interleave"));
        list_modify.addItem(diBuilder.getDocItem("cartesian-product"));
        list_modify.addItem(diBuilder.getDocItem("combinations"));
        list_modify.addItem(diBuilder.getDocItem("mapcat"));
        list_modify.addItem(diBuilder.getDocItem("flatten"));
        list_modify.addItem(diBuilder.getDocItem("sort"));
        list_modify.addItem(diBuilder.getDocItem("sort-by"));
        list_modify.addItem(diBuilder.getDocItem("take"));
        list_modify.addItem(diBuilder.getDocItem("take-while"));
        list_modify.addItem(diBuilder.getDocItem("take-last"));
        list_modify.addItem(diBuilder.getDocItem("drop"));
        list_modify.addItem(diBuilder.getDocItem("drop-while"));
        list_modify.addItem(diBuilder.getDocItem("drop-last"));
        list_modify.addItem(diBuilder.getDocItem("split-at"));
        list_modify.addItem(diBuilder.getDocItem("split-with"));

        final DocSection list_test = new DocSection("Test", "collections.lists.test");
        lists.addSection(list_test);
        list_test.addItem(diBuilder.getDocItem("list?"));
        list_test.addItem(diBuilder.getDocItem("mutable-list?"));
        list_test.addItem(diBuilder.getDocItem("every?"));
        list_test.addItem(diBuilder.getDocItem("not-every?"));
        list_test.addItem(diBuilder.getDocItem("any?"));
        list_test.addItem(diBuilder.getDocItem("not-any?"));


        final DocSection vectors = new DocSection("Vectors", "collections.vectors");
        section.addSection(vectors);

        final DocSection vec_create = new DocSection("Create", "collections.vectors.create");
        vectors.addSection(vec_create);
        vec_create.addItem(diBuilder.getDocItem("[]"));
        vec_create.addItem(diBuilder.getDocItem("vector"));
        vec_create.addItem(diBuilder.getDocItem("vector*"));
        vec_create.addItem(diBuilder.getDocItem("mutable-vector"));
        vec_create.addItem(diBuilder.getDocItem("mapv"));

        final DocSection vec_access = new DocSection("Access", "collections.vectors.access");
        vectors.addSection(vec_access);
        vec_access.addItem(diBuilder.getDocItem("first"));
        vec_access.addItem(diBuilder.getDocItem("second"));
        vec_access.addItem(diBuilder.getDocItem("third"));
        vec_access.addItem(diBuilder.getDocItem("nth"));
        vec_access.addItem(diBuilder.getDocItem("last"));
        vec_access.addItem(diBuilder.getDocItem("peek"));
        vec_access.addItem(diBuilder.getDocItem("butlast"));
        vec_access.addItem(diBuilder.getDocItem("rest"));
        vec_access.addItem(diBuilder.getDocItem("nfirst"));
        vec_access.addItem(diBuilder.getDocItem("nlast"));
        vec_access.addItem(diBuilder.getDocItem("subvec"));
        vec_access.addItem(diBuilder.getDocItem("some"));

        final DocSection vec_modify = new DocSection("Modify", "collections.vectors.modify");
        vectors.addSection(vec_modify);
        vec_modify.addItem(diBuilder.getDocItem("cons"));
        vec_modify.addItem(diBuilder.getDocItem("conj"));
        vec_modify.addItem(diBuilder.getDocItem("rest"));
        vec_modify.addItem(diBuilder.getDocItem("pop"));
        vec_modify.addItem(diBuilder.getDocItem("into"));
        vec_modify.addItem(diBuilder.getDocItem("concat"));
        vec_modify.addItem(diBuilder.getDocItem("distinct"));
        vec_modify.addItem(diBuilder.getDocItem("dedupe"));
        vec_modify.addItem(diBuilder.getDocItem("partition"));
        vec_modify.addItem(diBuilder.getDocItem("partition-by"));
        vec_modify.addItem(diBuilder.getDocItem("interpose"));
        vec_modify.addItem(diBuilder.getDocItem("interleave"));
        vec_modify.addItem(diBuilder.getDocItem("cartesian-product"));
        vec_modify.addItem(diBuilder.getDocItem("combinations"));
        vec_modify.addItem(diBuilder.getDocItem("mapcat"));
        vec_modify.addItem(diBuilder.getDocItem("flatten"));
        vec_modify.addItem(diBuilder.getDocItem("sort"));
        vec_modify.addItem(diBuilder.getDocItem("sort-by"));
        vec_modify.addItem(diBuilder.getDocItem("take"));
        vec_modify.addItem(diBuilder.getDocItem("take-while"));
        vec_modify.addItem(diBuilder.getDocItem("take-last"));
        vec_modify.addItem(diBuilder.getDocItem("drop"));
        vec_modify.addItem(diBuilder.getDocItem("drop-while"));
        vec_modify.addItem(diBuilder.getDocItem("drop-last"));
        vec_modify.addItem(diBuilder.getDocItem("update"));
        vec_modify.addItem(diBuilder.getDocItem("update!"));
        vec_modify.addItem(diBuilder.getDocItem("assoc"));
        vec_modify.addItem(diBuilder.getDocItem("assoc!"));
        vec_modify.addItem(diBuilder.getDocItem("split-with"));

        final DocSection vec_nested = new DocSection("Nested", "collections.vectors.nested");
        vectors.addSection(vec_nested);
        vec_nested.addItem(diBuilder.getDocItem("get-in"));
        vec_nested.addItem(diBuilder.getDocItem("assoc-in"));
        vec_nested.addItem(diBuilder.getDocItem("update-in"));
        vec_nested.addItem(diBuilder.getDocItem("dissoc-in"));

        final DocSection vec_test = new DocSection("Test", "collections.vectors.test");
        vectors.addSection(vec_test);
        vec_test.addItem(diBuilder.getDocItem("vector?"));
        vec_test.addItem(diBuilder.getDocItem("mutable-vector?"));
        vec_test.addItem(diBuilder.getDocItem("contains?"));
        vec_test.addItem(diBuilder.getDocItem("not-contains?"));
        vec_test.addItem(diBuilder.getDocItem("every?"));
        vec_test.addItem(diBuilder.getDocItem("not-every?"));
        vec_test.addItem(diBuilder.getDocItem("any?"));
        vec_test.addItem(diBuilder.getDocItem("not-any?"));


        final DocSection sets = new DocSection("Sets", "collections.sets");
        section.addSection(sets);

        final DocSection set_create = new DocSection("Create", "collections.sets.create");
        sets.addSection(set_create);
        set_create.addItem(diBuilder.getDocItem("#{}"));
        set_create.addItem(diBuilder.getDocItem("set"));
        set_create.addItem(diBuilder.getDocItem("sorted-set"));
        set_create.addItem(diBuilder.getDocItem("mutable-set"));

        final DocSection set_modify = new DocSection("Modify", "collections.sets.modify");
        sets.addSection(set_modify);
        set_modify.addItem(diBuilder.getDocItem("cons"));
        set_modify.addItem(diBuilder.getDocItem("cons!"));
        set_modify.addItem(diBuilder.getDocItem("conj"));
        set_modify.addItem(diBuilder.getDocItem("conj!"));
        set_modify.addItem(diBuilder.getDocItem("disj"));

        final DocSection algebra = new DocSection("Algebra", "collections.sets.algebra");
        sets.addSection(algebra);
        algebra.addItem(diBuilder.getDocItem("difference"));
        algebra.addItem(diBuilder.getDocItem("union"));
        algebra.addItem(diBuilder.getDocItem("intersection"));
        algebra.addItem(diBuilder.getDocItem("subset?"));
        algebra.addItem(diBuilder.getDocItem("superset?"));

        final DocSection set_test = new DocSection("Test", "collections.sets.test");
        sets.addSection(set_test);
        set_test.addItem(diBuilder.getDocItem("set?"));
        set_test.addItem(diBuilder.getDocItem("sorted-set?"));
        set_test.addItem(diBuilder.getDocItem("mutable-set?"));
        set_test.addItem(diBuilder.getDocItem("contains?"));
        set_test.addItem(diBuilder.getDocItem("not-contains?"));
        set_test.addItem(diBuilder.getDocItem("every?"));
        set_test.addItem(diBuilder.getDocItem("not-every?"));
        set_test.addItem(diBuilder.getDocItem("any?"));
        set_test.addItem(diBuilder.getDocItem("not-any?"));


        final DocSection maps = new DocSection("Maps", "collections.maps");
        section.addSection(maps);

        final DocSection maps_create = new DocSection("Create", "collections.maps.create");
        maps.addSection(maps_create);
        maps_create.addItem(diBuilder.getDocItem("{}"));
        maps_create.addItem(diBuilder.getDocItem("hash-map"));
        maps_create.addItem(diBuilder.getDocItem("ordered-map"));
        maps_create.addItem(diBuilder.getDocItem("sorted-map"));
        maps_create.addItem(diBuilder.getDocItem("mutable-map"));
        maps_create.addItem(diBuilder.getDocItem("zipmap"));


        final DocSection map_access = new DocSection("Access", "collections.maps.access");
        maps.addSection(map_access);
        map_access.addItem(diBuilder.getDocItem("find"));
        map_access.addItem(diBuilder.getDocItem("get"));
        map_access.addItem(diBuilder.getDocItem("keys"));
        map_access.addItem(diBuilder.getDocItem("vals"));

        final DocSection map_modify = new DocSection("Modify", "collections.maps.modify");
        maps.addSection(map_modify);
        map_modify.addItem(diBuilder.getDocItem("cons"));
        map_modify.addItem(diBuilder.getDocItem("conj"));
        map_modify.addItem(diBuilder.getDocItem("assoc"));
        map_modify.addItem(diBuilder.getDocItem("assoc!"));
        map_modify.addItem(diBuilder.getDocItem("update"));
        map_modify.addItem(diBuilder.getDocItem("update!"));
        map_modify.addItem(diBuilder.getDocItem("dissoc"));
        map_modify.addItem(diBuilder.getDocItem("dissoc!"));
        map_modify.addItem(diBuilder.getDocItem("into"));
        map_modify.addItem(diBuilder.getDocItem("concat"));
        map_modify.addItem(diBuilder.getDocItem("flatten"));
        map_modify.addItem(diBuilder.getDocItem("filter-k"));
        map_modify.addItem(diBuilder.getDocItem("filter-kv"));
        map_modify.addItem(diBuilder.getDocItem("reduce-kv"));
        map_modify.addItem(diBuilder.getDocItem("merge"));
        map_modify.addItem(diBuilder.getDocItem("merge-with"));
        map_modify.addItem(diBuilder.getDocItem("merge-deep"));
        map_modify.addItem(diBuilder.getDocItem("map-invert"));
        map_modify.addItem(diBuilder.getDocItem("map-keys"));
        map_modify.addItem(diBuilder.getDocItem("map-vals"));
        map_modify.addItem(diBuilder.getDocItem("select-keys"));

        final DocSection map_entries = new DocSection("Entries", "collections.maps.entries");
        maps.addSection(map_entries);
        map_entries.addItem(diBuilder.getDocItem("map-entry"));
        map_entries.addItem(diBuilder.getDocItem("key"));
        map_entries.addItem(diBuilder.getDocItem("val"));
        map_entries.addItem(diBuilder.getDocItem("entries"));
        map_entries.addItem(diBuilder.getDocItem("map-entry?"));

        final DocSection map_nested = new DocSection("Nested", "collections.maps.nested");
        maps.addSection(map_nested);
        map_nested.addItem(diBuilder.getDocItem("get-in"));
        map_nested.addItem(diBuilder.getDocItem("assoc-in"));
        map_nested.addItem(diBuilder.getDocItem("update-in"));
        map_nested.addItem(diBuilder.getDocItem("dissoc-in"));

        final DocSection map_test = new DocSection("Test", "collections.maps.test");
        maps.addSection(map_test);
        map_test.addItem(diBuilder.getDocItem("map?"));
        map_test.addItem(diBuilder.getDocItem("sequential?"));
        map_test.addItem(diBuilder.getDocItem("hash-map?"));
        map_test.addItem(diBuilder.getDocItem("ordered-map?"));
        map_test.addItem(diBuilder.getDocItem("sorted-map?"));
        map_test.addItem(diBuilder.getDocItem("mutable-map?"));
        map_test.addItem(diBuilder.getDocItem("contains?"));
        map_test.addItem(diBuilder.getDocItem("not-contains?"));


        final DocSection stacks = new DocSection("Stack", "collections.stack");
        section.addSection(stacks);

        final DocSection stacks_create = new DocSection("Create", "collections.stack.create");
        stacks.addSection(stacks_create);
        stacks_create.addItem(diBuilder.getDocItem("stack"));

        final DocSection stacks_access = new DocSection("Access", "collections.stack.access");
        stacks.addSection(stacks_access);
        stacks_access.addItem(diBuilder.getDocItem("peek"));
        stacks_access.addItem(diBuilder.getDocItem("pop!"));
        stacks_access.addItem(diBuilder.getDocItem("push!"));
        stacks_access.addItem(diBuilder.getDocItem("count"));

        final DocSection stacks_test = new DocSection("Test", "collections.stack.test");
        stacks.addSection(stacks_test);
        stacks_test.addItem(diBuilder.getDocItem("empty?"));
        stacks_test.addItem(diBuilder.getDocItem("stack?"));


        final DocSection queues = new DocSection("Queue", "collections.queue");
        section.addSection(queues);

        final DocSection queues_create = new DocSection("Create", "collections.queue.create");
        queues.addSection(queues_create);
        queues_create.addItem(diBuilder.getDocItem("queue"));


        final DocSection queues_access = new DocSection("Access", "collections.queue.access");
        queues.addSection(queues_access);
        queues_access.addItem(diBuilder.getDocItem("peek"));
        queues_access.addItem(diBuilder.getDocItem("poll!"));
        queues_access.addItem(diBuilder.getDocItem("offer!"));
        queues_access.addItem(diBuilder.getDocItem("count"));


        final DocSection queues_test = new DocSection("Test", "collections.queue.test");
        queues.addSection(queues_test);
        queues_test.addItem(diBuilder.getDocItem("empty?"));
        queues_test.addItem(diBuilder.getDocItem("queue?"));


        final DocSection dag = new DocSection("DAG", "directed acyclic graph", "collections.dag");
        section.addSection(dag);

        final DocSection dag_create = new DocSection("Create", "collections.dag.create");
        dag.addSection(dag_create);
        dag_create.addItem(diBuilder.getDocItem("dag/dag"));
        dag_create.addItem(diBuilder.getDocItem("dag/add-edges"));
        dag_create.addItem(diBuilder.getDocItem("dag/add-nodes"));

        final DocSection dag_access = new DocSection("Access", "collections.dag.access");
        dag.addSection(dag_access);
        dag_access.addItem(diBuilder.getDocItem("dag/nodes"));
        dag_access.addItem(diBuilder.getDocItem("dag/edges"));
        dag_access.addItem(diBuilder.getDocItem("dag/roots"));
        dag_access.addItem(diBuilder.getDocItem("count"));

        final DocSection dag_children = new DocSection("Children", "collections.dag.children");
        dag.addSection(dag_children);
        dag_children.addItem(diBuilder.getDocItem("dag/children"));
        dag_children.addItem(diBuilder.getDocItem("dag/direct-children"));

        final DocSection dag_parents = new DocSection("Parents", "collections.dag.parents");
        dag.addSection(dag_parents);
        dag_parents.addItem(diBuilder.getDocItem("dag/parents"));
        dag_parents.addItem(diBuilder.getDocItem("dag/direct-parents"));

        final DocSection dag_sort = new DocSection("Sort", "collections.dag.sort");
        dag.addSection(dag_sort);
        dag_sort.addItem(diBuilder.getDocItem("dag/topological-sort"));
        dag_sort.addItem(diBuilder.getDocItem("dag/compare-fn"));

        final DocSection dag_test = new DocSection("Test", "collections.dag.test");
        dag.addSection(dag_test);
        dag_test.addItem(diBuilder.getDocItem("dag/dag?"));
        dag_test.addItem(diBuilder.getDocItem("dag/node?"));
        dag_test.addItem(diBuilder.getDocItem("dag/edge?"));
        dag_test.addItem(diBuilder.getDocItem("dag/parent-of?"));
        dag_test.addItem(diBuilder.getDocItem("dag/child-of?"));
        dag_test.addItem(diBuilder.getDocItem("empty?"));

        return section;
    }

    private DocSection getLazySequencesSection() {
        final DocSection section = new DocSection("Lazy Sequences", "lazyseq");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection create = new DocSection("Create", "lazyseq.create");
        all.addSection(create);
        create.addItem(diBuilder.getDocItem("lazy-seq"));

        final DocSection realize = new DocSection("Realize", "lazyseq.realize");
        all.addSection(realize);
        realize.addItem(diBuilder.getDocItem("doall"));

        final DocSection test = new DocSection("Test", "lazyseq.test");
        all.addSection(test);
        test.addItem(diBuilder.getDocItem("lazy-seq?"));

        return section;
    }

    private DocSection getArraysSection() {
        final DocSection section = new DocSection("Arrays", "arrays");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection create = new DocSection("Create", "arrays.create");
        all.addSection(create);
        create.addItem(diBuilder.getDocItem("make-array"));
        create.addItem(diBuilder.getDocItem("object-array"));
        create.addItem(diBuilder.getDocItem("string-array"));
        create.addItem(diBuilder.getDocItem("int-array"));
        create.addItem(diBuilder.getDocItem("long-array"));
        create.addItem(diBuilder.getDocItem("float-array"));
        create.addItem(diBuilder.getDocItem("double-array"));

        final DocSection use = new DocSection("Use", "arrays.use");
        all.addSection(use);
        use.addItem(diBuilder.getDocItem("aget"));
        use.addItem(diBuilder.getDocItem("aset"));
        use.addItem(diBuilder.getDocItem("alength"));
        use.addItem(diBuilder.getDocItem("asub"));
        use.addItem(diBuilder.getDocItem("acopy"));
        use.addItem(diBuilder.getDocItem("amap"));

        return section;
    }

    private DocSection getRegexSection() {
        final DocSection section = new DocSection("Regex", "regex");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection general = new DocSection("General", "regex.general");
        all.addSection(general);
        general.addItem(diBuilder.getDocItem("regex/pattern"));
        general.addItem(diBuilder.getDocItem("regex/matcher"));
        general.addItem(diBuilder.getDocItem("regex/reset"));
        general.addItem(diBuilder.getDocItem("regex/matches?"));
        general.addItem(diBuilder.getDocItem("regex/matches"));
        general.addItem(diBuilder.getDocItem("regex/group"));
        general.addItem(diBuilder.getDocItem("regex/count"));
        general.addItem(diBuilder.getDocItem("regex/find?"));
        general.addItem(diBuilder.getDocItem("regex/find"));
        general.addItem(diBuilder.getDocItem("regex/find-all"));
        general.addItem(diBuilder.getDocItem("regex/find+"));
        general.addItem(diBuilder.getDocItem("regex/find-all+"));

        return section;
    }

    private DocSection getMathSection() {
        final DocSection section = new DocSection("Math", "math");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection constants = new DocSection("Constants", "math.constants");
        section.addSection(constants);
        constants.addLiteralItem("E",  "math/E",  id());
        constants.addLiteralItem("PI", "math/PI", id());

        final DocSection arithmetic = new DocSection("Arithmetic", "math.arithmetic");
        all.addSection(arithmetic);
        arithmetic.addItem(diBuilder.getDocItem("mod"));
        arithmetic.addItem(diBuilder.getDocItem("inc"));
        arithmetic.addItem(diBuilder.getDocItem("dec"));
        arithmetic.addItem(diBuilder.getDocItem("min"));
        arithmetic.addItem(diBuilder.getDocItem("max"));
        arithmetic.addItem(diBuilder.getDocItem("abs"));
        arithmetic.addItem(diBuilder.getDocItem("sgn"));
        arithmetic.addItem(diBuilder.getDocItem("negate"));
        arithmetic.addItem(diBuilder.getDocItem("floor"));
        arithmetic.addItem(diBuilder.getDocItem("ceil"));
        arithmetic.addItem(diBuilder.getDocItem("sqrt"));
        arithmetic.addItem(diBuilder.getDocItem("square"));
        arithmetic.addItem(diBuilder.getDocItem("pow"));
        arithmetic.addItem(diBuilder.getDocItem("exp"));
        arithmetic.addItem(diBuilder.getDocItem("log"));
        arithmetic.addItem(diBuilder.getDocItem("log10"));

        final DocSection util = new DocSection("Util", "math.util");
        all.addSection(util);
        util.addItem(diBuilder.getDocItem("digits"));

        final DocSection random = new DocSection("Random", "math.random");
        all.addSection(random);
        random.addItem(diBuilder.getDocItem("rand-long"));
        random.addItem(diBuilder.getDocItem("rand-double"));
        random.addItem(diBuilder.getDocItem("rand-gaussian"));

        final DocSection trigonometry = new DocSection("Trigonometry", "math.trigonometry");
        all.addSection(trigonometry);
        trigonometry.addItem(diBuilder.getDocItem("math/to-radians"));
        trigonometry.addItem(diBuilder.getDocItem("math/to-degrees"));
        trigonometry.addItem(diBuilder.getDocItem("math/sin"));
        trigonometry.addItem(diBuilder.getDocItem("math/cos"));
        trigonometry.addItem(diBuilder.getDocItem("math/tan"));
        trigonometry.addItem(diBuilder.getDocItem("math/asin"));
        trigonometry.addItem(diBuilder.getDocItem("math/acos"));
        trigonometry.addItem(diBuilder.getDocItem("math/atan"));

        final DocSection statistics = new DocSection("Statistics", "math.statistics");
        all.addSection(statistics);
        statistics.addItem(diBuilder.getDocItem("math/mean"));
        statistics.addItem(diBuilder.getDocItem("math/median"));
        statistics.addItem(diBuilder.getDocItem("math/quartiles"));
        statistics.addItem(diBuilder.getDocItem("math/quantile"));
        statistics.addItem(diBuilder.getDocItem("math/standard-deviation"));

        final DocSection algo = new DocSection("Algorithms", "math.algo");
        all.addSection(algo);
        algo.addItem(diBuilder.getDocItem("math/softmax"));

        return section;
    }

    private DocSection getFunctionsSection() {
        final DocSection section = new DocSection("Functions", "functions");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection create = new DocSection("Create", "functions.create");
        all.addSection(create);
        create.addItem(diBuilder.getDocItem("fn"));
        create.addItem(diBuilder.getDocItem("defn"));
        create.addItem(diBuilder.getDocItem("defn-"));
        create.addItem(diBuilder.getDocItem("identity"));
        create.addItem(diBuilder.getDocItem("comp"));
        create.addItem(diBuilder.getDocItem("partial"));
        create.addItem(diBuilder.getDocItem("memoize"));
        create.addItem(diBuilder.getDocItem("juxt"));
        create.addItem(diBuilder.getDocItem("fnil"));
        create.addItem(diBuilder.getDocItem("trampoline"));
        create.addItem(diBuilder.getDocItem("complement"));
        create.addItem(diBuilder.getDocItem("constantly"));
        create.addItem(diBuilder.getDocItem("every-pred"));
        create.addItem(diBuilder.getDocItem("any-pred"));

        final DocSection call = new DocSection("Call", "functions.call");
        all.addSection(call);
        call.addItem(diBuilder.getDocItem("apply"));
        call.addItem(diBuilder.getDocItem("->"));
        call.addItem(diBuilder.getDocItem("->>"));

        final DocSection test = new DocSection("Test", "functions.test");
        all.addSection(test);
        test.addItem(diBuilder.getDocItem("fn?"));

        final DocSection misc = new DocSection("Misc", "functions.misc");
        all.addSection(misc);
        misc.addItem(diBuilder.getDocItem("nil?"));
        misc.addItem(diBuilder.getDocItem("some?"));
        misc.addItem(diBuilder.getDocItem("eval"));
        misc.addItem(diBuilder.getDocItem("read-string"));
        misc.addItem(diBuilder.getDocItem("name"));
        misc.addItem(diBuilder.getDocItem("namespace"));
        misc.addItem(diBuilder.getDocItem("fn-name"));
        misc.addItem(diBuilder.getDocItem("callstack"));
        misc.addItem(diBuilder.getDocItem("coalesce"));
        misc.addItem(diBuilder.getDocItem("load-resource"));

        final DocSection env = new DocSection("Environment", "functions.environment");
        all.addSection(env);
        env.addItem(diBuilder.getDocItem("set!"));
        env.addItem(diBuilder.getDocItem("resolve"));
        env.addItem(diBuilder.getDocItem("bound?"));
        env.addItem(diBuilder.getDocItem("var-get"));
        env.addItem(diBuilder.getDocItem("var-name"));
        env.addItem(diBuilder.getDocItem("var-ns"));
        env.addItem(diBuilder.getDocItem("var-thread-local?"));
        env.addItem(diBuilder.getDocItem("var-local?"));
        env.addItem(diBuilder.getDocItem("var-global?"));
        env.addItem(diBuilder.getDocItem("name"));
        env.addItem(diBuilder.getDocItem("namespace"));

        final DocSection walk = new DocSection("Tree Walker", "functions.treewalker");
        all.addSection(walk);
        walk.addItem(diBuilder.getDocItem("prewalk"));
        walk.addItem(diBuilder.getDocItem("postwalk"));
        walk.addItem(diBuilder.getDocItem("prewalk-replace"));
        walk.addItem(diBuilder.getDocItem("postwalk-replace"));

        final DocSection meta = new DocSection("Meta", "functions.meta");
        all.addSection(meta);
        meta.addItem(diBuilder.getDocItem("meta"));
        meta.addItem(diBuilder.getDocItem("with-meta"));
        meta.addItem(diBuilder.getDocItem("vary-meta"));

        final DocSection doc = new DocSection("Documentation", "functions.doc");
        all.addSection(doc);
        doc.addItem(diBuilder.getDocItem("doc", false));
        doc.addItem(diBuilder.getDocItem("modules"));

        final DocSection def = new DocSection("Definiton", "functions.def");
        all.addSection(def);
        def.addItem(diBuilder.getDocItem("fn-body"));
        def.addItem(diBuilder.getDocItem("fn-pre-conditions"));

        final DocSection syntax = new DocSection("Syntax", "functions.syntax");
        all.addSection(syntax);
        syntax.addItem(diBuilder.getDocItem("highlight"));

        return section;
    }
    private DocSection getMacrosSection() {
        final DocSection section = new DocSection("Macros", "macros");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection create = new DocSection("Create", "macros.create");
        all.addSection(create);
        create.addItem(diBuilder.getDocItem("def-", false));
        create.addItem(diBuilder.getDocItem("defn"));
        create.addItem(diBuilder.getDocItem("defn-"));
        create.addItem(diBuilder.getDocItem("defmacro"));
        create.addItem(diBuilder.getDocItem("macroexpand"));
        create.addItem(diBuilder.getDocItem("macroexpand-all"));
        create.addItem(diBuilder.getDocItem("macro?"));

        final DocSection test = new DocSection("Test", "macros.test");
        all.addSection(test);
        test.addItem(diBuilder.getDocItem("macro?"));
        test.addItem(diBuilder.getDocItem("macroexpand-on-load?"));


        final DocSection quote = new DocSection("Quoting", "macros.quoting");
        all.addSection(quote);
        quote.addItem(diBuilder.getDocItem("quote"));
        quote.addItem(diBuilder.getDocItem("quasiquote"));

        final DocSection branch = new DocSection("Branch", "macros.branch");
        all.addSection(branch);
        branch.addItem(diBuilder.getDocItem("and"));
        branch.addItem(diBuilder.getDocItem("or"));
        branch.addItem(diBuilder.getDocItem("when"));
        branch.addItem(diBuilder.getDocItem("when-not"));
        branch.addItem(diBuilder.getDocItem("if-not"));
        branch.addItem(diBuilder.getDocItem("if-let"));
        branch.addItem(diBuilder.getDocItem("when-let"));
        branch.addItem(diBuilder.getDocItem("letfn"));

        final DocSection cond = new DocSection("Conditions", "macros.cond");
        all.addSection(cond);
        cond.addItem(diBuilder.getDocItem("cond"));
        cond.addItem(diBuilder.getDocItem("condp"));
        cond.addItem(diBuilder.getDocItem("case"));

        final DocSection loop = new DocSection("Loop", "macros.loop");
        all.addSection(loop);
        loop.addItem(diBuilder.getDocItem("while"));
        loop.addItem(diBuilder.getDocItem("dotimes"));
        loop.addItem(diBuilder.getDocItem("list-comp"));
        loop.addItem(diBuilder.getDocItem("doseq"));

        final DocSection call = new DocSection("Call", "macros.call");
        all.addSection(call);
        call.addItem(diBuilder.getDocItem("doto"));
        call.addItem(diBuilder.getDocItem("->"));
        call.addItem(diBuilder.getDocItem("->>"));
        call.addItem(diBuilder.getDocItem("-<>"));
        call.addItem(diBuilder.getDocItem("as->"));
        call.addItem(diBuilder.getDocItem("cond->"));
        call.addItem(diBuilder.getDocItem("cond->>"));
        call.addItem(diBuilder.getDocItem("some->"));
        call.addItem(diBuilder.getDocItem("some->>"));

        final DocSection loading = new DocSection("Loading", "macros.loading");
        all.addSection(loading);
        loading.addItem(diBuilder.getDocItem("load-module"));
        loading.addItem(diBuilder.getDocItem("load-file", false));
        loading.addItem(diBuilder.getDocItem("load-classpath-file"));
        loading.addItem(diBuilder.getDocItem("load-string"));

        final DocSection assert_ = new DocSection("Assert", "macros.assert");
        all.addSection(assert_);
        assert_.addItem(diBuilder.getDocItem("assert", true, true));

        final DocSection util = new DocSection("Util", "macros.util");
        all.addSection(util);
        util.addItem(diBuilder.getDocItem("comment"));
        util.addItem(diBuilder.getDocItem("gensym"));
        util.addItem(diBuilder.getDocItem("time"));
        util.addItem(diBuilder.getDocItem("with-out-str"));
        util.addItem(diBuilder.getDocItem("with-err-str"));

        final DocSection profil = new DocSection("Profiling", "macros.profiling");
        all.addSection(profil);
        profil.addItem(diBuilder.getDocItem("time"));
        profil.addItem(diBuilder.getDocItem("perf", false));

        return section;
    }

    private DocSection getSpecialFormsSection() {
        final DocSection section = new DocSection("Special Forms", "specialforms");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection generic = new DocSection("Forms", "specialforms.forms");
        all.addSection(generic);

        generic.addItem(diBuilder.getDocItem("def"));
        generic.addItem(diBuilder.getDocItem("defonce"));
        generic.addItem(diBuilder.getDocItem("def-dynamic"));
        generic.addItem(diBuilder.getDocItem("if"));
        generic.addItem(diBuilder.getDocItem("do"));
        generic.addItem(diBuilder.getDocItem("let"));
        generic.addItem(diBuilder.getDocItem("binding"));
        generic.addItem(diBuilder.getDocItem("fn"));
        generic.addItem(diBuilder.getDocItem("set!"));

        final DocSection multi = new DocSection("Multi Methods", "specialforms.multimethod");
        all.addSection(multi);
        multi.addItem(diBuilder.getDocItem("defmulti"));
        multi.addItem(diBuilder.getDocItem("defmethod"));

        final DocSection proto = new DocSection("Protocols", "specialforms.protocol");
        all.addSection(proto);
        proto.addItem(diBuilder.getDocItem("defprotocol"));
        proto.addItem(diBuilder.getDocItem("extend"));
        proto.addItem(diBuilder.getDocItem("extends?"));

        final DocSection recur = new DocSection("Recursion", "specialforms.recursion");
        all.addSection(recur);
        recur.addItem(diBuilder.getDocItem("loop"));
        recur.addItem(diBuilder.getDocItem("recur"));
        recur.addItem(diBuilder.getDocItem("tail-pos", true, true));

        final DocSection ex = new DocSection("Exception", "specialforms.exception");
        all.addSection(ex);
        ex.addItem(diBuilder.getDocItem("throw", true, true));
        ex.addItem(diBuilder.getDocItem("try", true, true));
        ex.addItem(diBuilder.getDocItem("try-with", true, true));

        final DocSection profiling = new DocSection("Profiling", "specialforms.profiling");
        all.addSection(profiling);

        profiling.addItem(diBuilder.getDocItem("dobench"));
        profiling.addItem(diBuilder.getDocItem("dorun"));
        profiling.addItem(diBuilder.getDocItem("prof"));

        return section;
    }

    private DocSection getExceptionsSection() {
        final DocSection section = new DocSection("Exceptions", "exceptions");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection block = new DocSection("Throw/Catch", "exceptions.block");
        all.addSection(block);
        block.addItem(diBuilder.getDocItem("try", true, true));
        block.addItem(diBuilder.getDocItem("try-with", true, true));
        block.addItem(diBuilder.getDocItem("throw", true, true));

        final DocSection create = new DocSection("Create", "exceptions.create");
        all.addSection(create);
        create.addItem(diBuilder.getDocItem("ex"));

        final DocSection test = new DocSection("Test", "exceptions.test");
        all.addSection(test);
        test.addItem(diBuilder.getDocItem("ex?"));
        test.addItem(diBuilder.getDocItem("ex-venice?"));

        final DocSection util = new DocSection("Util", "exceptions.util");
        all.addSection(util);
        util.addItem(diBuilder.getDocItem("ex-message"));
        util.addItem(diBuilder.getDocItem("ex-cause"));
        util.addItem(diBuilder.getDocItem("ex-value"));

        final DocSection stacktrace = new DocSection("Stacktrace", "exceptions.stacktrace");
        all.addSection(stacktrace);
        stacktrace.addItem(diBuilder.getDocItem("ex-venice-stacktrace"));
        stacktrace.addItem(diBuilder.getDocItem("ex-java-stacktrace", false, true));

        return section;
    }

    private DocSection getSystemSection() {
        final DocSection section = new DocSection("System", "system");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection venice = new DocSection("Venice", "system.venice");
        all.addSection(venice);
        venice.addItem(diBuilder.getDocItem("version"));

        final DocSection sandbox = new DocSection("Sandbox", "system.sandbox");
        all.addSection(sandbox);
        sandbox.addItem(diBuilder.getDocItem("sandboxed?"));
        sandbox.addItem(diBuilder.getDocItem("sandbox-type"));

        final DocSection system = new DocSection("System", "system.system");
        all.addSection(system);
        system.addItem(diBuilder.getDocItem("system-prop"));
        system.addItem(diBuilder.getDocItem("system-env"));
        system.addItem(diBuilder.getDocItem("system-exit-code"));
        system.addItem(diBuilder.getDocItem("charset-default-encoding"));

        final DocSection java = new DocSection("Java", "system.java");
        all.addSection(java);
        java.addItem(diBuilder.getDocItem("java-version"));
        java.addItem(diBuilder.getDocItem("java-version-info"));
        java.addItem(diBuilder.getDocItem("java-major-version"));
        java.addItem(diBuilder.getDocItem("java-source-location", false));

        final DocSection javaVM = new DocSection("Java VM", "system.java-vm");
        all.addSection(javaVM);
        javaVM.addItem(diBuilder.getDocItem("pid"));
        javaVM.addItem(diBuilder.getDocItem("gc"));
        javaVM.addItem(diBuilder.getDocItem("total-memory"));
        javaVM.addItem(diBuilder.getDocItem("used-memory"));

        final DocSection os = new DocSection("OS", "system.os");
        all.addSection(os);
        os.addItem(diBuilder.getDocItem("os-type"));
        os.addItem(diBuilder.getDocItem("os-type?"));
        os.addItem(diBuilder.getDocItem("os-arch"));
        os.addItem(diBuilder.getDocItem("os-name"));
        os.addItem(diBuilder.getDocItem("os-version"));

        final DocSection time = new DocSection("Time", "system.time");
        all.addSection(time);
        time.addItem(diBuilder.getDocItem("current-time-millis"));
        time.addItem(diBuilder.getDocItem("nano-time"));
        time.addItem(diBuilder.getDocItem("format-nano-time"));
        time.addItem(diBuilder.getDocItem("format-micro-time"));
        time.addItem(diBuilder.getDocItem("format-milli-time"));

        final DocSection host = new DocSection("Host", "system.host");
        all.addSection(host);
        host.addItem(diBuilder.getDocItem("host-name"));
        host.addItem(diBuilder.getDocItem("host-address"));
        host.addItem(diBuilder.getDocItem("ip-private?"));
        host.addItem(diBuilder.getDocItem("cpus"));

        final DocSection user = new DocSection("User", "system.user");
        all.addSection(user);
        user.addItem(diBuilder.getDocItem("user-name"));
        user.addItem(diBuilder.getDocItem("io/user-home-dir"));

        final DocSection util = new DocSection("Util", "system.util");
        all.addSection(util);
        util.addItem(diBuilder.getDocItem("uuid"));
        util.addItem(diBuilder.getDocItem("sleep"));
        util.addItem(diBuilder.getDocItem("shutdown-hook"));

        final DocSection shell = new DocSection("Shell", "system.shell");
        all.addSection(shell);
        shell.addItem(diBuilder.getDocItem("sh", false));
        shell.addItem(diBuilder.getDocItem("with-sh-dir", false));
        shell.addItem(diBuilder.getDocItem("with-sh-env", false));
        shell.addItem(diBuilder.getDocItem("with-sh-throw", false));

        final DocSection tools = new DocSection("Shell Tools", "system.shell.tools");
        all.addSection(tools);
        tools.addItem(diBuilder.getDocItem("sh/open", false));
        tools.addItem(diBuilder.getDocItem("sh/pwd", false));

        return section;
    }

    private DocSection getTypesSection() {
        final DocSection section = new DocSection("Types", "types");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection util = new DocSection("Util", "types.util");
        all.addSection(util);
        util.addItem(diBuilder.getDocItem("type"));
        util.addItem(diBuilder.getDocItem("supertype"));
        util.addItem(diBuilder.getDocItem("supertypes"));

        final DocSection test = new DocSection("Test", "types.test");
        all.addSection(test);
        test.addItem(diBuilder.getDocItem("instance-of?"));
        test.addItem(diBuilder.getDocItem("deftype?"));

        final DocSection define = new DocSection("Define", "types.define");
        all.addSection(define);
        define.addItem(diBuilder.getDocItem("deftype"));
        define.addItem(diBuilder.getDocItem("deftype-of"));
        define.addItem(diBuilder.getDocItem("deftype-or"));

        final DocSection create = new DocSection("Create", "types.create");
        all.addSection(create);
        create.addItem(diBuilder.getDocItem(".:"));

        final DocSection describe = new DocSection("Describe", "types.describe");
        all.addSection(describe);
        describe.addItem(diBuilder.getDocItem("deftype-describe"));

        return section;
    }

    private DocSection getProtocolsSection() {
        final DocSection section = new DocSection("Protocols", "protocols");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection core = new DocSection("Core", "protocols.core");
        all.addSection(core);
        core.addItem(diBuilder.getDocItem("Object"));

        return section;
    }

    private DocSection getTransducersSection() {
        final DocSection section = new DocSection("Transducers", "transducers");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection run = new DocSection("Use", "transducers.use");
        all.addSection(run);
        run.addItem(diBuilder.getDocItem("transduce"));

        final DocSection func = new DocSection("Functions", "transducers.functions");
        all.addSection(func);
        func.addItem(diBuilder.getDocItem("map"));
        func.addItem(diBuilder.getDocItem("map-indexed"));
        func.addItem(diBuilder.getDocItem("filter"));
        func.addItem(diBuilder.getDocItem("drop"));
        func.addItem(diBuilder.getDocItem("drop-while"));
        func.addItem(diBuilder.getDocItem("drop-last"));
        func.addItem(diBuilder.getDocItem("take"));
        func.addItem(diBuilder.getDocItem("take-while"));
        func.addItem(diBuilder.getDocItem("take-last"));
        func.addItem(diBuilder.getDocItem("keep"));
        func.addItem(diBuilder.getDocItem("remove"));
        func.addItem(diBuilder.getDocItem("dedupe"));
        func.addItem(diBuilder.getDocItem("distinct"));
        func.addItem(diBuilder.getDocItem("sorted"));
        func.addItem(diBuilder.getDocItem("reverse"));
        func.addItem(diBuilder.getDocItem("flatten"));
        func.addItem(diBuilder.getDocItem("halt-when"));

        final DocSection red = new DocSection("Reductions", "transducers.reductions");
        all.addSection(red);
        red.addItem(diBuilder.getDocItem("rf-first"));
        red.addItem(diBuilder.getDocItem("rf-last"));
        red.addItem(diBuilder.getDocItem("rf-every?"));
        red.addItem(diBuilder.getDocItem("rf-any?"));

        final DocSection early = new DocSection("Early", "transducers.early");
        all.addSection(early);
        early.addItem(diBuilder.getDocItem("reduced"));
        early.addItem(diBuilder.getDocItem("reduced?"));
        early.addItem(diBuilder.getDocItem("deref"));
        early.addItem(diBuilder.getDocItem("deref?"));

        return section;
    }

    private DocSection getConcurrencySection() {
        final DocSection section = new DocSection("Concurrency", "concurrency");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection atoms = new DocSection("Atoms", "concurrency.atoms");
        all.addSection(atoms);
        atoms.addItem(diBuilder.getDocItem("atom"));
        atoms.addItem(diBuilder.getDocItem("atom?"));
        atoms.addItem(diBuilder.getDocItem("deref"));
        atoms.addItem(diBuilder.getDocItem("deref?"));
        atoms.addItem(diBuilder.getDocItem("reset!"));
        atoms.addItem(diBuilder.getDocItem("swap!"));
        atoms.addItem(diBuilder.getDocItem("swap-vals!"));
        atoms.addItem(diBuilder.getDocItem("compare-and-set!"));
        atoms.addItem(diBuilder.getDocItem("add-watch"));
        atoms.addItem(diBuilder.getDocItem("remove-watch"));

        final DocSection futures = new DocSection("Futures", "concurrency.futures");
        all.addSection(futures);
        futures.addItem(diBuilder.getDocItem("future"));
        futures.addItem(diBuilder.getDocItem("future-task"));
        futures.addItem(diBuilder.getDocItem("future?"));
        futures.addItem(diBuilder.getDocItem("futures-fork"));
        futures.addItem(diBuilder.getDocItem("futures-wait"));
        futures.addItem(diBuilder.getDocItem("futures-thread-pool-info"));
        futures.addItem(diBuilder.getDocItem("done?"));
        futures.addItem(diBuilder.getDocItem("cancel"));
        futures.addItem(diBuilder.getDocItem("cancelled?"));
        futures.addItem(diBuilder.getDocItem("deref"));
        futures.addItem(diBuilder.getDocItem("deref?"));
        futures.addItem(diBuilder.getDocItem("realized?"));

        final DocSection promises = new DocSection("Promises", "concurrency.promises");
        all.addSection(promises);
        promises.addItem(diBuilder.getDocItem("promise"));
        promises.addItem(diBuilder.getDocItem("promise?"));
        promises.addItem(diBuilder.getDocItem("deliver"));
        promises.addItem(diBuilder.getDocItem("deliver-ex"));
        promises.addItem(diBuilder.getDocItem("realized?"));
        promises.addItem(diBuilder.getDocItem("then-accept"));
        promises.addItem(diBuilder.getDocItem("then-accept-both"));
        promises.addItem(diBuilder.getDocItem("then-apply"));
        promises.addItem(diBuilder.getDocItem("then-combine"));
        promises.addItem(diBuilder.getDocItem("then-compose"));
        promises.addItem(diBuilder.getDocItem("when-complete"));
        promises.addItem(diBuilder.getDocItem("accept-either"));
        promises.addItem(diBuilder.getDocItem("apply-to-either"));
        promises.addItem(diBuilder.getDocItem("all-of"));
        promises.addItem(diBuilder.getDocItem("any-of"));
        promises.addItem(diBuilder.getDocItem("or-timeout", true, true));
        promises.addItem(diBuilder.getDocItem("complete-on-timeout", true, true));
        promises.addItem(diBuilder.getDocItem("timeout-after", true, true));
        promises.addItem(diBuilder.getDocItem("done?"));
        promises.addItem(diBuilder.getDocItem("cancel"));
        promises.addItem(diBuilder.getDocItem("cancelled?"));

        final DocSection delay = new DocSection("Delay", "concurrency.delay");
        all.addSection(delay);
        delay.addItem(diBuilder.getDocItem("delay"));
        delay.addItem(diBuilder.getDocItem("delay?"));
        delay.addItem(diBuilder.getDocItem("deref"));
        delay.addItem(diBuilder.getDocItem("deref?"));
        delay.addItem(diBuilder.getDocItem("force"));
        delay.addItem(diBuilder.getDocItem("realized?"));

        final DocSection agents = new DocSection("Agents", "concurrency.agents");
        all.addSection(agents);
        agents.addItem(diBuilder.getDocItem("agent"));
        agents.addItem(diBuilder.getDocItem("send"));
        agents.addItem(diBuilder.getDocItem("send-off"));
        agents.addItem(diBuilder.getDocItem("restart-agent"));
        agents.addItem(diBuilder.getDocItem("set-error-handler!"));
        agents.addItem(diBuilder.getDocItem("agent-error"));
        agents.addItem(diBuilder.getDocItem("await"));
        agents.addItem(diBuilder.getDocItem("await-for"));
        agents.addItem(diBuilder.getDocItem("shutdown-agents", false));
        agents.addItem(diBuilder.getDocItem("shutdown-agents?", false));
        agents.addItem(diBuilder.getDocItem("await-termination-agents", false));
        agents.addItem(diBuilder.getDocItem("await-termination-agents?", false));
        agents.addItem(diBuilder.getDocItem("agent-send-thread-pool-info"));
        agents.addItem(diBuilder.getDocItem("agent-send-off-thread-pool-info"));


        final DocSection sched = new DocSection("Scheduler", "concurrency.scheduler");
        all.addSection(sched);
        sched.addItem(diBuilder.getDocItem("schedule-delay", false));
        sched.addItem(diBuilder.getDocItem("schedule-at-fixed-rate", false));

        final DocSection locking = new DocSection("Locking", "concurrency.locking");
        all.addSection(locking);
        locking.addItem(diBuilder.getDocItem("locking"));

        final DocSection volatiles = new DocSection("Volatiles", "concurrency.volatiles");
        all.addSection(volatiles);
        volatiles.addItem(diBuilder.getDocItem("volatile"));
        volatiles.addItem(diBuilder.getDocItem("volatile?"));
        volatiles.addItem(diBuilder.getDocItem("deref"));
        volatiles.addItem(diBuilder.getDocItem("deref?"));
        volatiles.addItem(diBuilder.getDocItem("reset!"));
        volatiles.addItem(diBuilder.getDocItem("swap!"));

        final DocSection thlocal = new DocSection("ThreadLocal", "concurrency.threadlocal");
        all.addSection(thlocal);
        thlocal.addItem(diBuilder.getDocItem("thread-local"));
        thlocal.addItem(diBuilder.getDocItem("thread-local?"));
        thlocal.addItem(diBuilder.getDocItem("thread-local-clear"));
        thlocal.addItem(diBuilder.getDocItem("thread-local-map"));
        thlocal.addItem(diBuilder.getDocItem("assoc"));
        thlocal.addItem(diBuilder.getDocItem("dissoc"));
        thlocal.addItem(diBuilder.getDocItem("get"));
        thlocal.addItem(diBuilder.getDocItem("binding"));
        thlocal.addItem(diBuilder.getDocItem("def-dynamic"));

        final DocSection threads = new DocSection("Threads", "concurrency.threads");
        all.addSection(threads);
        threads.addItem(diBuilder.getDocItem("thread-id"));
        threads.addItem(diBuilder.getDocItem("thread-name"));
        threads.addItem(diBuilder.getDocItem("thread-daemon?"));
        threads.addItem(diBuilder.getDocItem("thread-interrupted?"));
        threads.addItem(diBuilder.getDocItem("thread-interrupted"));

        final DocSection parallel = new DocSection("Parallel", "concurrency.parallel");
        all.addSection(parallel);
        parallel.addItem(diBuilder.getDocItem("pmap"));
        parallel.addItem(diBuilder.getDocItem("pcalls"));

        return section;
    }

    private DocSection getIOSection() {
        final DocSection section = new DocSection("I/O", "io.util");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection to = new DocSection("to", "io.to");
        all.addSection(to);
        to.addItem(diBuilder.getDocItem("print"));
        to.addItem(diBuilder.getDocItem("println"));
        to.addItem(diBuilder.getDocItem("printf"));
        to.addItem(diBuilder.getDocItem("flush"));
        to.addItem(diBuilder.getDocItem("newline"));
        to.addItem(diBuilder.getDocItem("pr"));
        to.addItem(diBuilder.getDocItem("prn"));

        final DocSection to_str = new DocSection("to-str", "io.tostr");
        all.addSection(to_str);
        to_str.addItem(diBuilder.getDocItem("pr-str"));
        to_str.addItem(diBuilder.getDocItem("with-out-str"));

        final DocSection from = new DocSection("from", "io.from");
        all.addSection(from);
        from.addItem(diBuilder.getDocItem("read-line"));
        from.addItem(diBuilder.getDocItem("read-char"));

        final DocSection classpath = new DocSection("classpath", "io.classpath");
        all.addSection(classpath);
        classpath.addItem(diBuilder.getDocItem("io/load-classpath-resource"));
        classpath.addItem(diBuilder.getDocItem("io/classpath-resource?"));

        final DocSection stream = new DocSection("stream", "io.stream");
        all.addSection(stream);
        stream.addItem(diBuilder.getDocItem("io/slurp"));
        stream.addItem(diBuilder.getDocItem("io/slurp-lines"));
        stream.addItem(diBuilder.getDocItem("io/copy-stream"));
        stream.addItem(diBuilder.getDocItem("io/slurp-stream"));
        stream.addItem(diBuilder.getDocItem("io/spit-stream"));
        stream.addItem(diBuilder.getDocItem("io/uri-stream", false));
        stream.addItem(diBuilder.getDocItem("io/file-in-stream", false));
        stream.addItem(diBuilder.getDocItem("io/string-in-stream", false));
        stream.addItem(diBuilder.getDocItem("io/bytebuf-in-stream", false));
        stream.addItem(diBuilder.getDocItem("io/wrap-os-with-buffered-writer"));
        stream.addItem(diBuilder.getDocItem("io/wrap-os-with-print-writer"));
        stream.addItem(diBuilder.getDocItem("io/wrap-is-with-buffered-reader"));

        final DocSection rd_wr = new DocSection("reader/writer", "io.readerwriter");
        all.addSection(rd_wr);
        rd_wr.addItem(diBuilder.getDocItem("io/buffered-reader"));
        rd_wr.addItem(diBuilder.getDocItem("io/buffered-writer"));

        final DocSection http = new DocSection("http", "io.http");
        all.addSection(http);
        http.addItem(diBuilder.getDocItem("io/download", false));
        http.addItem(diBuilder.getDocItem("io/internet-avail?", false));

        final DocSection other = new DocSection("other", "io.other");
        all.addSection(other);
        other.addItem(diBuilder.getDocItem("with-out-str"));
        other.addItem(diBuilder.getDocItem("with-err-str"));
        other.addItem(diBuilder.getDocItem("io/mime-type"));
        other.addItem(diBuilder.getDocItem("io/default-charset"));

        final DocSection vars = new DocSection("vars", "io.vars");
        all.addSection(vars);
        vars.addItem(diBuilder.getDocItem("*out*"));
        vars.addItem(diBuilder.getDocItem("*err*"));
        vars.addItem(diBuilder.getDocItem("*in*"));

        return section;
    }

    private DocSection getIOFileSection() {
        final DocSection section = new DocSection("File I/O", "io.file");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection file = new DocSection("file", "io.file_");
        all.addSection(file);
        file.addItem(diBuilder.getDocItem("io/file"));
        file.addItem(diBuilder.getDocItem("io/file-parent"));
        file.addItem(diBuilder.getDocItem("io/file-name"));
        file.addItem(diBuilder.getDocItem("io/file-path"));
        file.addItem(diBuilder.getDocItem("io/file-absolute-path"));
        file.addItem(diBuilder.getDocItem("io/file-canonical-path"));
        file.addItem(diBuilder.getDocItem("io/file-ext"));
        file.addItem(diBuilder.getDocItem("io/file-ext?"));
        file.addItem(diBuilder.getDocItem("io/file-size", false));
        file.addItem(diBuilder.getDocItem("io/file-last-modified", false));

        final DocSection file_dir = new DocSection("file dir", "io.filedir");
        all.addSection(file_dir);
        file_dir.addItem(diBuilder.getDocItem("io/mkdir"));
        file_dir.addItem(diBuilder.getDocItem("io/mkdirs"));

        final DocSection file_io = new DocSection("file i/o", "io.fileio");
        all.addSection(file_io);
        file_io.addItem(diBuilder.getDocItem("io/slurp"));
        file_io.addItem(diBuilder.getDocItem("io/slurp-lines"));
        file_io.addItem(diBuilder.getDocItem("io/spit"));
        file_io.addItem(diBuilder.getDocItem("io/copy-file"));
        file_io.addItem(diBuilder.getDocItem("io/move-file"));
        file_io.addItem(diBuilder.getDocItem("io/delete-file"));
        file_io.addItem(diBuilder.getDocItem("io/delete-file-on-exit"));
        file_io.addItem(diBuilder.getDocItem("io/delete-file-tree"));

        final DocSection file_list = new DocSection("file list", "io.filelist");
        all.addSection(file_list);
        file_list.addItem(diBuilder.getDocItem("io/list-files", false));
        file_list.addItem(diBuilder.getDocItem("io/list-files-glob", false));
        file_list.addItem(diBuilder.getDocItem("io/list-file-tree", false));

        final DocSection file_test = new DocSection("file test", "io.filetest");
        all.addSection(file_test);
        file_test.addItem(diBuilder.getDocItem("io/file?"));
        file_test.addItem(diBuilder.getDocItem("io/exists-file?"));
        file_test.addItem(diBuilder.getDocItem("io/exists-dir?"));
        file_test.addItem(diBuilder.getDocItem("io/file-can-read?", false));
        file_test.addItem(diBuilder.getDocItem("io/file-can-write?", false));
        file_test.addItem(diBuilder.getDocItem("io/file-can-execute?", false));
        file_test.addItem(diBuilder.getDocItem("io/file-hidden?", false));
        file_test.addItem(diBuilder.getDocItem("io/file-symbolic-link?", false));

        final DocSection file_glob = new DocSection("file glob", "io.fileglob");
        all.addSection(file_glob);
        file_glob.addItem(diBuilder.getDocItem("io/glob-path-matcher"));
        file_glob.addItem(diBuilder.getDocItem("io/file-matches-glob?"));
        file_glob.addItem(diBuilder.getDocItem("io/list-files-glob", false));

        final DocSection file_uri = new DocSection("URL/URI", "io.url_uri");
        all.addSection(file_uri);
        file_uri.addItem(diBuilder.getDocItem("io/->url"));
        file_uri.addItem(diBuilder.getDocItem("io/->uri"));

        final DocSection file_watch = new DocSection("file watch", "io.filewatch");
        all.addSection(file_watch);
        file_watch.addItem(diBuilder.getDocItem("io/await-for", false));
        file_watch.addItem(diBuilder.getDocItem("io/watch-dir", false));
        file_watch.addItem(diBuilder.getDocItem("io/close-watcher", false));

        final DocSection file_other = new DocSection("file other", "io.fileother");
        all.addSection(file_other);
        file_other.addItem(diBuilder.getDocItem("io/temp-file"));
        file_other.addItem(diBuilder.getDocItem("io/tmp-dir"));
        file_other.addItem(diBuilder.getDocItem("io/user-dir"));
        file_other.addItem(diBuilder.getDocItem("io/user-home-dir"));

        return section;
    }

    private DocSection getIOZipSection() {
        final DocSection section = new DocSection("Zip/GZip", "io.zip");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection zip = new DocSection("zip", "io.zip_");
        all.addSection(zip);
        zip.addItem(diBuilder.getDocItem("io/zip", false));
        zip.addItem(diBuilder.getDocItem("io/zip-file", false));
        zip.addItem(diBuilder.getDocItem("io/zip-list", false));
        zip.addItem(diBuilder.getDocItem("io/zip-list-entry-names", false));
        zip.addItem(diBuilder.getDocItem("io/zip-append", false));
        zip.addItem(diBuilder.getDocItem("io/zip-remove", false));
        zip.addItem(diBuilder.getDocItem("io/zip?"));
        zip.addItem(diBuilder.getDocItem("io/unzip"));
        zip.addItem(diBuilder.getDocItem("io/unzip-first"));
        zip.addItem(diBuilder.getDocItem("io/unzip-nth"));
        zip.addItem(diBuilder.getDocItem("io/unzip-all"));
        zip.addItem(diBuilder.getDocItem("io/unzip-to-dir", false));

        final DocSection gzip = new DocSection("gzip", "io.gzip");
        all.addSection(gzip);
        gzip.addItem(diBuilder.getDocItem("io/gzip", false));
        gzip.addItem(diBuilder.getDocItem("io/gzip-to-stream"));
        gzip.addItem(diBuilder.getDocItem("io/gzip?"));
        gzip.addItem(diBuilder.getDocItem("io/ungzip"));
        gzip.addItem(diBuilder.getDocItem("io/ungzip-to-stream"));

        return section;
    }

    private DocSection getAppSection() {
        final DocSection section = new DocSection("Application", "application");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection mgmt = new DocSection("Management", "application.management");
        all.addSection(mgmt);
        mgmt.addItem(diBuilder.getDocItem("app/build"));
        mgmt.addItem(diBuilder.getDocItem("app/manifest"));

        return section;
    }

    private DocSection getNamespaceSection() {
        final DocSection section = new DocSection("Namespace", "namespace");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection open = new DocSection("Open", "namespace.open");
        all.addSection(open);
        open.addItem(diBuilder.getDocItem("ns"));

        final DocSection curr = new DocSection("Current", "namespace.current");
        all.addSection(curr);
        curr.addItem(diBuilder.getDocItem("*ns*"));

        final DocSection remove = new DocSection("Remove", "namespace.remove");
        all.addSection(remove);
        remove.addItem(diBuilder.getDocItem("ns-unmap"));
        remove.addItem(diBuilder.getDocItem("ns-remove"));

        final DocSection util = new DocSection("Util", "namespace.util");
        all.addSection(util);
        util.addItem(diBuilder.getDocItem("ns-list"));
        util.addItem(diBuilder.getDocItem("namespace"));

        final DocSection alias = new DocSection("Alias", "namespace.alias");
        all.addSection(alias);
        alias.addItem(diBuilder.getDocItem("ns-alias"));
        alias.addItem(diBuilder.getDocItem("ns-aliases"));
        alias.addItem(diBuilder.getDocItem("ns-unalias"));

        return section;
    }

    private DocSection getByteBufSection() {
        final DocSection section = new DocSection("Byte Buffer", "bytebuf");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection bb_create = new DocSection("Create", "bytebuf.create");
        all.addSection(bb_create);
        bb_create.addItem(diBuilder.getDocItem("bytebuf"));
        bb_create.addItem(diBuilder.getDocItem("bytebuf-allocate"));
        bb_create.addItem(diBuilder.getDocItem("bytebuf-from-string"));

        final DocSection bb_test = new DocSection("Test", "bytebuf.test");
        all.addSection(bb_test);
        bb_test.addItem(diBuilder.getDocItem("empty?"));
        bb_test.addItem(diBuilder.getDocItem("not-empty?"));
        bb_test.addItem(diBuilder.getDocItem("bytebuf?"));

        final DocSection bb_use = new DocSection("Use", "bytebuf.use");
        all.addSection(bb_use);
        bb_use.addItem(diBuilder.getDocItem("count"));
        bb_use.addItem(diBuilder.getDocItem("bytebuf-capacity"));
        bb_use.addItem(diBuilder.getDocItem("bytebuf-limit"));
        bb_use.addItem(diBuilder.getDocItem("bytebuf-to-string"));
        bb_use.addItem(diBuilder.getDocItem("bytebuf-to-list"));
        bb_use.addItem(diBuilder.getDocItem("bytebuf-sub"));
        bb_use.addItem(diBuilder.getDocItem("bytebuf-pos"));
        bb_use.addItem(diBuilder.getDocItem("bytebuf-pos!"));

        final DocSection bb_read = new DocSection("Read", "bytebuf.read");
        all.addSection(bb_read);
        bb_read.addItem(diBuilder.getDocItem("bytebuf-get-byte"));
        bb_read.addItem(diBuilder.getDocItem("bytebuf-get-int"));
        bb_read.addItem(diBuilder.getDocItem("bytebuf-get-long"));
        bb_read.addItem(diBuilder.getDocItem("bytebuf-get-float"));
        bb_read.addItem(diBuilder.getDocItem("bytebuf-get-double"));

        final DocSection bb_write = new DocSection("Write", "bytebuf.write");
        all.addSection(bb_write);
        bb_write.addItem(diBuilder.getDocItem("bytebuf-put-byte!"));
        bb_write.addItem(diBuilder.getDocItem("bytebuf-put-int!"));
        bb_write.addItem(diBuilder.getDocItem("bytebuf-put-long!"));
        bb_write.addItem(diBuilder.getDocItem("bytebuf-put-float!"));
        bb_write.addItem(diBuilder.getDocItem("bytebuf-put-double!"));
        bb_write.addItem(diBuilder.getDocItem("bytebuf-put-buf!"));

        final DocSection encode = new DocSection("Base64", "bytebuf.base64");
        all.addSection(encode);
        encode.addItem(diBuilder.getDocItem("str/encode-base64"));
        encode.addItem(diBuilder.getDocItem("str/decode-base64"));

        final DocSection hex = new DocSection("Hex", "bytebuf.hex");
        all.addSection(hex);
        hex.addItem(diBuilder.getDocItem("str/hex-to-bytebuf"));
        hex.addItem(diBuilder.getDocItem("str/bytebuf-to-hex"));
        hex.addItem(diBuilder.getDocItem("str/format-bytebuf"));

        return section;
    }

    private DocSection getTimeSection() {
        final DocSection section = new DocSection("Time", "time");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection date = new DocSection("Date", "time.date");
        all.addSection(date);
        date.addItem(diBuilder.getDocItem("time/date"));
        date.addItem(diBuilder.getDocItem("time/date?"));

        final DocSection local_date = new DocSection("Local Date", "time.localdate");
        all.addSection(local_date);
        local_date.addItem(diBuilder.getDocItem("time/local-date"));
        local_date.addItem(diBuilder.getDocItem("time/local-date?"));
        local_date.addItem(diBuilder.getDocItem("time/local-date-parse"));

        final DocSection local_date_time = new DocSection("Local Date Time", "time.localdatetime");
        all.addSection(local_date_time);
        local_date_time.addItem(diBuilder.getDocItem("time/local-date-time"));
        local_date_time.addItem(diBuilder.getDocItem("time/local-date-time?"));
        local_date_time.addItem(diBuilder.getDocItem("time/local-date-time-parse"));

        final DocSection zoned_date_time = new DocSection("Zoned Date Time", "time.zoneddatetime");
        all.addSection(zoned_date_time);
        zoned_date_time.addItem(diBuilder.getDocItem("time/zoned-date-time"));
        zoned_date_time.addItem(diBuilder.getDocItem("time/zoned-date-time?"));
        zoned_date_time.addItem(diBuilder.getDocItem("time/zoned-date-time-parse"));

        final DocSection fields = new DocSection("Fields", "time.fields");
        all.addSection(fields);
        fields.addItem(diBuilder.getDocItem("time/year"));
        fields.addItem(diBuilder.getDocItem("time/month"));
        fields.addItem(diBuilder.getDocItem("time/day-of-week"));
        fields.addItem(diBuilder.getDocItem("time/day-of-month"));
        fields.addItem(diBuilder.getDocItem("time/day-of-year"));
        fields.addItem(diBuilder.getDocItem("time/hour"));
        fields.addItem(diBuilder.getDocItem("time/minute"));
        fields.addItem(diBuilder.getDocItem("time/second"));

        final DocSection etc = new DocSection("Fields etc", "time.fieldsetc");
        all.addSection(etc);
        etc.addItem(diBuilder.getDocItem("time/length-of-year"));
        etc.addItem(diBuilder.getDocItem("time/length-of-month"));
        etc.addItem(diBuilder.getDocItem("time/first-day-of-month"));
        etc.addItem(diBuilder.getDocItem("time/last-day-of-month"));

        final DocSection zone = new DocSection("Zone", "time.zone");
        all.addSection(zone);
        zone.addItem(diBuilder.getDocItem("time/zone"));
        zone.addItem(diBuilder.getDocItem("time/zone-offset"));

        final DocSection format = new DocSection("Format", "time.format");
        all.addSection(format);
        format.addItem(diBuilder.getDocItem("time/formatter", false, false));
        format.addItem(diBuilder.getDocItem("time/format"));

        final DocSection compare = new DocSection("Test", "time.test");
        all.addSection(compare);
        compare.addItem(diBuilder.getDocItem("time/after?"));
        compare.addItem(diBuilder.getDocItem("time/not-after?"));
        compare.addItem(diBuilder.getDocItem("time/before?"));
        compare.addItem(diBuilder.getDocItem("time/not-before?"));
        compare.addItem(diBuilder.getDocItem("time/within?"));
        compare.addItem(diBuilder.getDocItem("time/leap-year?"));

        final DocSection misc = new DocSection("Miscellaneous", "time.misc");
        all.addSection(misc);
        misc.addItem(diBuilder.getDocItem("time/with-time"));
        misc.addItem(diBuilder.getDocItem("time/plus"));
        misc.addItem(diBuilder.getDocItem("time/minus"));
        misc.addItem(diBuilder.getDocItem("time/period"));
        misc.addItem(diBuilder.getDocItem("time/earliest"));
        misc.addItem(diBuilder.getDocItem("time/latest"));

        final DocSection util = new DocSection("Util", "time.util");
        all.addSection(util);
        util.addItem(diBuilder.getDocItem("time/zone-ids"));
        util.addItem(diBuilder.getDocItem("time/to-millis"));

        return section;
    }

    private DocSection getJavaInteropSection() {
        final DocSection section = new DocSection("Java Interoperability", "javainterop");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection java = new DocSection("Java", "javainterop.java");
        all.addSection(java);
        java.addItem(diBuilder.getDocItem("."));
        java.addItem(diBuilder.getDocItem("import"));
        java.addItem(diBuilder.getDocItem("java-iterator-to-list"));
        java.addItem(diBuilder.getDocItem("java-enumeration-to-list"));
        java.addItem(diBuilder.getDocItem("java-unwrap-optional"));
        java.addItem(diBuilder.getDocItem("cast"));
        java.addItem(diBuilder.getDocItem("class"));

        final DocSection proxy = new DocSection("Proxify", "javainterop.proxify");
        all.addSection(proxy);
        proxy.addItem(diBuilder.getDocItem("proxify"));
        proxy.addItem(diBuilder.getDocItem("as-runnable"));
        proxy.addItem(diBuilder.getDocItem("as-callable"));
        proxy.addItem(diBuilder.getDocItem("as-predicate"));
        proxy.addItem(diBuilder.getDocItem("as-function"));
        proxy.addItem(diBuilder.getDocItem("as-consumer"));
        proxy.addItem(diBuilder.getDocItem("as-supplier"));
        proxy.addItem(diBuilder.getDocItem("as-bipredicate"));
        proxy.addItem(diBuilder.getDocItem("as-bifunction"));
        proxy.addItem(diBuilder.getDocItem("as-biconsumer"));
        proxy.addItem(diBuilder.getDocItem("as-binaryoperator"));

        final DocSection test = new DocSection("Test", "javainterop.test");
        all.addSection(test);
        test.addItem(diBuilder.getDocItem("java-obj?"));
        test.addItem(diBuilder.getDocItem("exists-class?"));

        final DocSection support = new DocSection("Support", "javainterop.support");
        all.addSection(support);
        support.addItem(diBuilder.getDocItem("imports"));
        support.addItem(diBuilder.getDocItem("supers"));
        support.addItem(diBuilder.getDocItem("bases"));
        support.addItem(diBuilder.getDocItem("formal-type"));
        support.addItem(diBuilder.getDocItem("stacktrace", false, false));

        final DocSection clazz = new DocSection("Classes", "javainterop.classes");
        all.addSection(clazz);
        clazz.addItem(diBuilder.getDocItem("class"));
        clazz.addItem(diBuilder.getDocItem("class-of"));
        clazz.addItem(diBuilder.getDocItem("class-name"));
        clazz.addItem(diBuilder.getDocItem("class-version"));
        clazz.addItem(diBuilder.getDocItem("classloader"));
        clazz.addItem(diBuilder.getDocItem("classloader-of"));

        final DocSection jar = new DocSection("JARs", "javainterop.jar");
        all.addSection(jar);
        jar.addItem(diBuilder.getDocItem("jar-maven-manifest-version"));
        jar.addItem(diBuilder.getDocItem("java-package-version"));

        final DocSection modules = new DocSection("Modules", "javainterop.modules");
        all.addSection(modules);
        modules.addItem(diBuilder.getDocItem("module-name", false));

        return section;
    }

    private DocSection getReplSection() {
        final DocSection section = new DocSection("REPL", "repl");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection repl = new DocSection("Info", "repl.info");
        all.addSection(repl);
        repl.addItem(diBuilder.getDocItem("repl/info", false));

        final DocSection term = new DocSection("Terminal", "repl.terminal");
        all.addSection(term);
        term.addItem(diBuilder.getDocItem("repl/term-rows", false));
        term.addItem(diBuilder.getDocItem("repl/term-cols", false));

        return section;
    }

    private DocSection getPdfSection() {
        final String footer = "Required 3rd party libraries:\n\n" +
                              "* org.xhtmlrenderer:flying-saucer-core:9.1.22\n" +
                              "* org.xhtmlrenderer:flying-saucer-pdf-openpdf:9.1.22\n" +
                              "* com.github.librepdf:openpdf:1.3.26\n" +
                              "* com.github.librepdf:pdf-toolbox:1.3.26\n";

        final DocSection section = new DocSection("PDF", null, "pdf", null, footer);


        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection pdf = new DocSection("PDF", "pdf.pdf");
        all.addSection(pdf);
        pdf.addItem(diBuilder.getDocItem("pdf/render", false));
        pdf.addItem(diBuilder.getDocItem("pdf/text-to-pdf", false));
        pdf.addItem(diBuilder.getDocItem("pdf/available?", false));
        pdf.addItem(diBuilder.getDocItem("pdf/check-required-libs", false));

        final DocSection pdf_tools = new DocSection("PDF Tools", "pdf.pdftools");
        all.addSection(pdf_tools);
        pdf_tools.addItem(diBuilder.getDocItem("pdf/merge", false));
        pdf_tools.addItem(diBuilder.getDocItem("pdf/copy", false));
        pdf_tools.addItem(diBuilder.getDocItem("pdf/pages"));
        pdf_tools.addItem(diBuilder.getDocItem("pdf/watermark", false));

        return section;
    }

    private DocSection getSystemVarSection() {
        final DocSection section = new DocSection("System Vars", "sysvars");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection other = new DocSection("System Vars", "sysvars.var");
        all.addSection(other);
        other.addItem(diBuilder.getDocItem("*version*"));
        other.addItem(diBuilder.getDocItem("*newline*"));
        other.addItem(diBuilder.getDocItem("*loaded-modules*"));
        other.addItem(diBuilder.getDocItem("*loaded-files*"));
        other.addItem(diBuilder.getDocItem("*ns*"));
        other.addItem(diBuilder.getDocItem("*run-mode*"));
        other.addItem(diBuilder.getDocItem("*ansi-term*"));
        other.addItem(diBuilder.getDocItem("*ARGV*"));
        other.addItem(diBuilder.getDocItem("*out*"));
        other.addItem(diBuilder.getDocItem("*err*"));
        other.addItem(diBuilder.getDocItem("*in*"));

        return section;
    }

    private DocSection getJsonSection() {
        final DocSection section = new DocSection("JSON", "json");


        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection read = new DocSection("read", "json.read");
        all.addSection(read);
        read.addItem(diBuilder.getDocItem("json/read-str"));
        read.addItem(diBuilder.getDocItem("json/slurp"));

        final DocSection write = new DocSection("write", "json.write");
        all.addSection(write);
        write.addItem(diBuilder.getDocItem("json/write-str"));
        write.addItem(diBuilder.getDocItem("json/spit"));

        final DocSection prettify = new DocSection("prettify", "json.prettify");
        all.addSection(prettify);
        prettify.addItem(diBuilder.getDocItem("json/pretty-print"));

        return section;
    }

    private DocSection getCsvSection() {
        final DocSection section = new DocSection("CSV", "csv");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection read = new DocSection("read", "csv.read");
        all.addSection(read);
        read.addItem(diBuilder.getDocItem("csv/read"));

        final DocSection write = new DocSection("write", "csv.write");
        all.addSection(write);
        write.addItem(diBuilder.getDocItem("csv/write", false));
        write.addItem(diBuilder.getDocItem("csv/write-str"));

        return section;
    }

    private DocSection getCidrSection() {
        final DocSection section = new DocSection(
                                        "CIDR",
                                        "classless inter-domain routing",
                                        "cidr");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection cidr = new DocSection("CIDR", "cidr.cidr");
        all.addSection(cidr);
        cidr.addItem(diBuilder.getDocItem("cidr/parse"));
        cidr.addItem(diBuilder.getDocItem("cidr/in-range?"));
        cidr.addItem(diBuilder.getDocItem("cidr/start-inet-addr"));
        cidr.addItem(diBuilder.getDocItem("cidr/end-inet-addr"));
        cidr.addItem(diBuilder.getDocItem("cidr/inet-addr"));
        cidr.addItem(diBuilder.getDocItem("cidr/inet-addr-to-bytes"));
        cidr.addItem(diBuilder.getDocItem("cidr/inet-addr-from-bytes"));

        final DocSection cidr_trie = new DocSection("CIDR Trie", "cidr.cidrtrie");
        all.addSection(cidr_trie);
        cidr_trie.addItem(diBuilder.getDocItem("cidr/trie"));
        cidr_trie.addItem(diBuilder.getDocItem("cidr/size"));
        cidr_trie.addItem(diBuilder.getDocItem("cidr/insert"));
        cidr_trie.addItem(diBuilder.getDocItem("cidr/lookup"));
        cidr_trie.addItem(diBuilder.getDocItem("cidr/lookup-reverse"));

        return section;
    }

    private DocSection getModuleKiraSection() {
        final DocSection section = new DocSection(
                                        "Kira",
                                        "Templating system",
                                        "modules.kira");

        final DocSection all = new DocSection("(load-module :kira)", id());
        section.addSection(all);

        final DocSection kira = new DocSection("Kira", id());
        all.addSection(kira);
        kira.addItem(diBuilder.getDocItem("kira/eval"));
        kira.addItem(diBuilder.getDocItem("kira/fn"));

        final DocSection escape = new DocSection("Escape", id());
        all.addSection(escape);
        escape.addItem(diBuilder.getDocItem("kira/escape-xml"));
        escape.addItem(diBuilder.getDocItem("kira/escape-html"));

        return section;
    }

    private DocSection getModuleTracingSection() {
        final DocSection section = new DocSection(
                                        "Tracing",
                                        "Tracing functions",
                                        "modules.tracing");

        final DocSection all = new DocSection("(load-module :trace)", id());
        section.addSection(all);

        final DocSection trace = new DocSection("Tracing", id());
        all.addSection(trace);
        trace.addItem(diBuilder.getDocItem("trace/trace"));
        trace.addItem(diBuilder.getDocItem("trace/trace-var"));
        trace.addItem(diBuilder.getDocItem("trace/untrace-var"));

        final DocSection test = new DocSection("Test", id());
        all.addSection(test);
        test.addItem(diBuilder.getDocItem("trace/traced?"));
        test.addItem(diBuilder.getDocItem("trace/traceable?"));

        final DocSection util = new DocSection("Util", id());
        all.addSection(util);
        util.addItem(diBuilder.getDocItem("trace/trace-str-limit"));

        final DocSection tee = new DocSection("Tee", id());
        all.addSection(tee);
        tee.addItem(diBuilder.getDocItem("trace/tee->"));
        tee.addItem(diBuilder.getDocItem("trace/tee->>"));
        tee.addItem(diBuilder.getDocItem("trace/tee"));

        return section;
    }

    private DocSection getModuleShellSection() {
        final DocSection section = new DocSection(
                                        "Shell",
                                        "Functions to deal with the operating system",
                                        "modules.shell");

        final DocSection all = new DocSection("(load-module :shell)", id());
        section.addSection(all);

        final DocSection trace = new DocSection("Open", id());
        all.addSection(trace);
        trace.addItem(diBuilder.getDocItem("shell/open", false));
        trace.addItem(diBuilder.getDocItem("shell/open-macos-app", false));

        final DocSection test = new DocSection("Process", id());
        all.addSection(test);
        test.addItem(diBuilder.getDocItem("shell/kill", false));
        test.addItem(diBuilder.getDocItem("shell/kill-forcibly", false));
        test.addItem(diBuilder.getDocItem("shell/wait-for-process-exit", false));
        test.addItem(diBuilder.getDocItem("shell/alive?", false));
        test.addItem(diBuilder.getDocItem("shell/pid", false));
        test.addItem(diBuilder.getDocItem("shell/process-handle", false));
        test.addItem(diBuilder.getDocItem("shell/process-handle?", false));
        test.addItem(diBuilder.getDocItem("shell/process-info", false));
        test.addItem(diBuilder.getDocItem("shell/processes", false));
        test.addItem(diBuilder.getDocItem("shell/processes-info", false));
        test.addItem(diBuilder.getDocItem("shell/descendant-processes", false));
        test.addItem(diBuilder.getDocItem("shell/parent-process", false));

        final DocSection util = new DocSection("Util", id());
        all.addSection(util);
        util.addItem(diBuilder.getDocItem("shell/diff", false));

        return section;
    }

    private DocSection getModuleXmlSection() {
        final DocSection section = new DocSection(
                                        "XML",
                                        "modules.xml");

        final DocSection all = new DocSection("(load-module :xml)", id());
        section.addSection(all);

        final DocSection xml = new DocSection("XML", id());
        all.addSection(xml);
        xml.addItem(diBuilder.getDocItem("xml/parse-str"));
        xml.addItem(diBuilder.getDocItem("xml/parse"));
        xml.addItem(diBuilder.getDocItem("xml/path->"));
        xml.addItem(diBuilder.getDocItem("xml/children"));
        xml.addItem(diBuilder.getDocItem("xml/text"));

        return section;
    }

    private DocSection getModuleCryptographySection() {
        final DocSection section = new DocSection(
                                        "Cryptography",
                                        "modules.cryptography");

        final DocSection all = new DocSection("(load-module :crypt)", id());
        section.addSection(all);

        final DocSection hashes = new DocSection("Hashes", id());
        all.addSection(hashes);
        hashes.addItem(diBuilder.getDocItem("crypt/md5-hash"));
        hashes.addItem(diBuilder.getDocItem("crypt/sha1-hash"));
        hashes.addItem(diBuilder.getDocItem("crypt/sha512-hash"));
        hashes.addItem(diBuilder.getDocItem("crypt/pbkdf2-hash"));

        final DocSection crypt = new DocSection("Encrypt", id());
        all.addSection(crypt);
        crypt.addItem(diBuilder.getDocItem("crypt/encrypt"));
        crypt.addItem(diBuilder.getDocItem("crypt/decrypt"));

        return section;
    }

    private DocSection getModuleGradleSection() {
        final DocSection section = new DocSection(
                                        "Gradle",
                                        "modules.gradle");

        final DocSection all = new DocSection("(load-module :gradle)", id());
        section.addSection(all);

        final DocSection gradle = new DocSection("Gradle", id());
        all.addSection(gradle);
        gradle.addItem(diBuilder.getDocItem("gradle/with-home", false));
        gradle.addItem(diBuilder.getDocItem("gradle/version", false));
        gradle.addItem(diBuilder.getDocItem("gradle/task", false));

        return section;
    }

    private DocSection getModuleMavenSection() {
        final DocSection section = new DocSection(
                                        "Maven",
                                        "modules.maven");

        final DocSection all = new DocSection("(load-module :maven)", id());
        section.addSection(all);

        final DocSection maven = new DocSection("Maven", id());
        all.addSection(maven);
        maven.addItem(diBuilder.getDocItem("maven/download", false));
        maven.addItem(diBuilder.getDocItem("maven/get", false));
        maven.addItem(diBuilder.getDocItem("maven/uri", false));
        maven.addItem(diBuilder.getDocItem("maven/parse-artefact", false));

        return section;
    }

    private DocSection getModuleJavaSection() {
        final DocSection section = new DocSection(
                                        "Java",
                                        "modules.java");

        final DocSection all = new DocSection("(load-module :java)", id());
        section.addSection(all);

        final DocSection java = new DocSection("Java", id());
        all.addSection(java);
        java.addItem(diBuilder.getDocItem("java/javadoc", false));

        return section;
    }

    private DocSection getModuleSemverSection() {
        final DocSection section = new DocSection(
                                        "Semver",
                                        "Semantic versioning",
                                        "modules.semver");

        final DocSection all = new DocSection("(load-module :semver)", id());
        section.addSection(all);

        final DocSection semver = new DocSection("Semver", id());
        all.addSection(semver);
        semver.addItem(diBuilder.getDocItem("semver/parse"));
        semver.addItem(diBuilder.getDocItem("semver/version"));

        final DocSection valid = new DocSection("Validation", id());
        all.addSection(valid);
        valid.addItem(diBuilder.getDocItem("semver/valid?"));
        valid.addItem(diBuilder.getDocItem("semver/valid-format?"));

        final DocSection test = new DocSection("Test", id());
        all.addSection(test);
        test.addItem(diBuilder.getDocItem("semver/newer?"));
        test.addItem(diBuilder.getDocItem("semver/older?"));
        test.addItem(diBuilder.getDocItem("semver/equal?"));
        test.addItem(diBuilder.getDocItem("semver/cmp"));

        return section;
    }

    private DocSection getModuleGeoipSection() {
        final DocSection section = new DocSection(
                                        "Geo IP",
                                        "Geolocation mapping for IP adresses",
                                        "modules.geoip");

        final DocSection all = new DocSection("(load-module :geoip)", id());
        section.addSection(all);

        final DocSection geoip = new DocSection("Lookup", id());
        all.addSection(geoip);
        geoip.addItem(diBuilder.getDocItem("geoip/ip-to-country-resolver", false));
        geoip.addItem(diBuilder.getDocItem("geoip/ip-to-country-loc-resolver", false));
        geoip.addItem(diBuilder.getDocItem("geoip/ip-to-city-loc-resolver", false));
        geoip.addItem(diBuilder.getDocItem("geoip/ip-to-city-loc-resolver-mem-optimized", false));

        final DocSection db = new DocSection("Databases", id());
        all.addSection(db);
        db.addItem(diBuilder.getDocItem("geoip/download-google-country-db-to-csvfile", false));
        db.addItem(diBuilder.getDocItem("geoip/download-maxmind-db-to-zipfile", false));
        db.addItem(diBuilder.getDocItem("geoip/download-maxmind-db", false));

        final DocSection dbBuild = new DocSection("DB Parser", id());
        all.addSection(dbBuild);
        dbBuild.addItem(diBuilder.getDocItem("geoip/parse-maxmind-country-ip-db", false));
        dbBuild.addItem(diBuilder.getDocItem("geoip/parse-maxmind-city-ip-db", false));
        dbBuild.addItem(diBuilder.getDocItem("geoip/parse-maxmind-country-db", false));
        dbBuild.addItem(diBuilder.getDocItem("geoip/parse-maxmind-city-db", false));

        final DocSection util = new DocSection("Util", id());
        all.addSection(util);
        util.addItem(diBuilder.getDocItem("geoip/build-maxmind-country-db-url"));
        util.addItem(diBuilder.getDocItem("geoip/build-maxmind-city-db-url"));
        util.addItem(diBuilder.getDocItem("geoip/map-location-to-numerics"));
        util.addItem(diBuilder.getDocItem("geoip/country-to-location-resolver", false));

        return section;
    }

    private DocSection getModuleHexdumpSection() {
        final DocSection section = new DocSection(
                                        "Hexdump",
                                        "modules.hexdump");

        final DocSection all = new DocSection("(load-module :hexdump)", id());
        section.addSection(all);

        final DocSection hexdump = new DocSection("Hexdump", id());
        all.addSection(hexdump);
        hexdump.addItem(diBuilder.getDocItem("hexdump/dump", false));

        return section;
    }

    private DocSection getModuleAnsiSection() {
        final DocSection section = new DocSection(
                                        "Ansi",
                                        "ANSI codes, styles, and colorization helper functions",
                                        "modules.ansi");

        final DocSection all = new DocSection("(load-module :ansi)", id());
        section.addSection(all);

        final DocSection colors = new DocSection("Colors", id());
        all.addSection(colors);
        colors.addItem(diBuilder.getDocItem("ansi/fg-color", false));
        colors.addItem(diBuilder.getDocItem("ansi/bg-color", false));

        final DocSection style = new DocSection("Styles", id());
        all.addSection(style);
        style.addItem(diBuilder.getDocItem("ansi/style", false));
        style.addItem(diBuilder.getDocItem("ansi/ansi", false));
        style.addItem(diBuilder.getDocItem("ansi/with-ansi", false));
        style.addItem(diBuilder.getDocItem("ansi/without-ansi", false));

        final DocSection cursor = new DocSection("Cursor", id());
        all.addSection(cursor);
        cursor.addItem(diBuilder.getDocItem("ansi/without-cursor", false));

        final DocSection progress = new DocSection("Progress", id());
        all.addSection(progress);
        progress.addItem(diBuilder.getDocItem("ansi/progress", false));
        progress.addItem(diBuilder.getDocItem("ansi/progress-bar", false));

        return section;
    }

    private DocSection getModuleGrepSection() {
        final DocSection section = new DocSection(
                                        "Grep",
                                        "Grep like search tool",
                                        "modules.grep");

        final DocSection all = new DocSection("(load-module :grep)", id());
        section.addSection(all);

        final DocSection grep = new DocSection("Grep", id());
        all.addSection(grep);
        grep.addItem(diBuilder.getDocItem("grep/grep", false));
        grep.addItem(diBuilder.getDocItem("grep/grep-zip", false));

        return section;
    }

    private DocSection getModuleParsifalSection() {
        final DocSection section = new DocSection(
                                        "Parsifal",
                                        "A parser combinator",
                                        "modules.parsifal",
                                        "*Parsifal* is a port of Nate Young's Parsatron Clojure " +
                                        "[parser combinators](https://github.com/youngnh/parsatron) "+
                                        "project.",
                                        null);

        final DocSection all = new DocSection("(load-module :parsifal)", id());
        section.addSection(all);

        final DocSection run = new DocSection("Run", id());
        all.addSection(run);
        run.addItem(diBuilder.getDocItem("parsifal/run", false));

        final DocSection define = new DocSection("Define", id());
        all.addSection(define);
        define.addItem(diBuilder.getDocItem("parsifal/defparser", false));

        final DocSection parsers = new DocSection("Parsers", id());
        all.addSection(parsers);
        parsers.addItem(diBuilder.getDocItem("parsifal/any", false));
        parsers.addItem(diBuilder.getDocItem("parsifal/many", false));
        parsers.addItem(diBuilder.getDocItem("parsifal/many1", false));
        parsers.addItem(diBuilder.getDocItem("parsifal/times", false));
        parsers.addItem(diBuilder.getDocItem("parsifal/either", false));
        parsers.addItem(diBuilder.getDocItem("parsifal/choice", false));
        parsers.addItem(diBuilder.getDocItem("parsifal/between", false));
        parsers.addItem(diBuilder.getDocItem("parsifal/>>", false));

        final DocSection special = new DocSection("Special Parsers", id());
        all.addSection(special);
        special.addItem(diBuilder.getDocItem("parsifal/eof", false));
        special.addItem(diBuilder.getDocItem("parsifal/never", false));
        special.addItem(diBuilder.getDocItem("parsifal/always", false));
        special.addItem(diBuilder.getDocItem("parsifal/lookahead", false));
        special.addItem(diBuilder.getDocItem("parsifal/attempt", false));

        final DocSection binding = new DocSection("Binding", id());
        all.addSection(binding);
        binding.addItem(diBuilder.getDocItem("parsifal/let->>", false));

        final DocSection ch = new DocSection("Char Parsers", id());
        all.addSection(ch);
        ch.addItem(diBuilder.getDocItem("parsifal/char", false));
        ch.addItem(diBuilder.getDocItem("parsifal/not-char", false));
        ch.addItem(diBuilder.getDocItem("parsifal/any-char", false));
        ch.addItem(diBuilder.getDocItem("parsifal/digit", false));
        ch.addItem(diBuilder.getDocItem("parsifal/hexdigit", false));
        ch.addItem(diBuilder.getDocItem("parsifal/letter", false));
        ch.addItem(diBuilder.getDocItem("parsifal/letter-or-digit", false));
        ch.addItem(diBuilder.getDocItem("parsifal/any-char-of", false));
        ch.addItem(diBuilder.getDocItem("parsifal/none-char-of", false));
        ch.addItem(diBuilder.getDocItem("parsifal/string", false));

        final DocSection tok = new DocSection("Token Parsers", id());
        all.addSection(tok);
        tok.addItem(diBuilder.getDocItem("parsifal/token", false));

        final DocSection proto = new DocSection("Protocols", id());
        all.addSection(proto);
        proto.addItem(diBuilder.getDocItem("parsifal/SourcePosition", false));

        final DocSection line = new DocSection("Line Info", id());
        all.addSection(line);
        line.addItem(diBuilder.getDocItem("parsifal/lineno", false));
        line.addItem(diBuilder.getDocItem("parsifal/pos", false));

        return section;
    }

    private DocSection getModuleBenchmarkSection() {
        final DocSection section = new DocSection(
                                        "Benchmark",
                                        "modules.benchmark");

        final DocSection all = new DocSection("(load-module :benchmark)", id());
        section.addSection(all);

        final DocSection colors = new DocSection("Utils", id());
        all.addSection(colors);
        colors.addItem(diBuilder.getDocItem("benchmark/benchmark", false));

        return section;
    }

    private DocSection getModuleConfigSection() {
        final DocSection section = new DocSection(
                                        "Configuration",
                                        "Manages configurations with system property & env var support",
                                        "modules.config");

        final DocSection all = new DocSection("(load-module :config)", id());
        section.addSection(all);

        final DocSection build = new DocSection("Build", id());
        all.addSection(build);
        build.addItem(diBuilder.getDocItem("config/build", false));

        final DocSection file = new DocSection("File", id());
        all.addSection(file);
        file.addItem(diBuilder.getDocItem("config/file", false));
        file.addItem(diBuilder.getDocItem("config/resource", true));

        final DocSection env = new DocSection("Env", id());
        all.addSection(env);
        env.addItem(diBuilder.getDocItem("config/env-var", true));
        env.addItem(diBuilder.getDocItem("config/env", false));

        final DocSection prop = new DocSection("Properties", id());
        all.addSection(prop);
        prop.addItem(diBuilder.getDocItem("config/property-var", true));
        prop.addItem(diBuilder.getDocItem("config/properties", false));

        return section;
    }

    private DocSection getModuleComponentSection() {
        final DocSection section = new DocSection(
                                    "Component",
                                    "Managing lifecycle and dependencies of components",
                                    "modules.component");

        final DocSection all = new DocSection("(load-module :component)", id());
        section.addSection(all);

        final DocSection system = new DocSection("Build", id());
        all.addSection(system);
        system.addItem(diBuilder.getDocItem("component/system-map", false));
        system.addItem(diBuilder.getDocItem("component/system-using"));

        final DocSection protocol = new DocSection("Protocol", id());
        all.addSection(protocol);
        protocol.addItem(diBuilder.getDocItem("component/Component", false));


        final DocSection util = new DocSection("Util", id());
        all.addSection(util);
        util.addItem(diBuilder.getDocItem("component/deps"));
        util.addItem(diBuilder.getDocItem("component/dep"));
        util.addItem(diBuilder.getDocItem("component/id"));

        return section;
    }

    private DocSection getModuleAppSection() {
        final DocSection section = new DocSection(
                                        "App",
                                        "Venice application archive",
                                        "modules.app");

        final DocSection all = new DocSection("(load-module :app)", id());
        section.addSection(all);

        final DocSection build = new DocSection("Build", id());
        all.addSection(build);
        build.addItem(diBuilder.getDocItem("app/build", false));

        final DocSection manifest = new DocSection("Manifest", id());
        all.addSection(manifest);
        manifest.addItem(diBuilder.getDocItem("app/manifest", false));

        return section;
    }

    private DocSection getModuleExcelSection() {
        final String footer = "Required 3rd party libraries:\n\n" +
                              "* org.apache.poi:poi:4.1.2\n" +
                              "* org.apache.poi:ooxml:4.1.2\n" +
                              "* org.apache.poi:ooxml-schemas:4.1.2\n" +
                              "* commons-codec:commons-codec:1.15\n" +
                              "* org.apache.commons:commons-collections:4.4.4\n" +
                              "* org.apache.commons:commons-compress:1.20\n" +
                              "* org.apache.commons:commons-math3:3.6.1\n" +
                              "* org.apache.xmlbeans:xmlbeans:3.1.0\n";

        final DocSection section = new DocSection("Excel", "Read/Write Excel files", "modules.excel", null, footer);

        final DocSection all = new DocSection("(load-module :excel)", id());
        section.addSection(all);

        final DocSection wr = new DocSection("Writer", id());
        all.addSection(wr);
        wr.addItem(diBuilder.getDocItem("excel/writer", false));
        wr.addItem(diBuilder.getDocItem("excel/add-sheet", false));
        wr.addItem(diBuilder.getDocItem("excel/add-font", false));
        wr.addItem(diBuilder.getDocItem("excel/add-style", false));
        wr.addItem(diBuilder.getDocItem("excel/add-column", false));

        final DocSection wr_data = new DocSection("Writer Data", id());
        all.addSection(wr_data);
        wr_data.addItem(diBuilder.getDocItem("excel/write-data", false));
        wr_data.addItem(diBuilder.getDocItem("excel/write-items", false));
        wr_data.addItem(diBuilder.getDocItem("excel/write-item", false));
        wr_data.addItem(diBuilder.getDocItem("excel/write-value", false));

        final DocSection wr_io = new DocSection("Writer I/O", id());
        all.addSection(wr_io);
        wr_io.addItem(diBuilder.getDocItem("excel/write->file", false));
        wr_io.addItem(diBuilder.getDocItem("excel/write->stream", false));
        wr_io.addItem(diBuilder.getDocItem("excel/write->bytebuf", false));

        final DocSection wr_util = new DocSection("Writer Util", id());
        all.addSection(wr_util);
        wr_util.addItem(diBuilder.getDocItem("excel/cell-formula", false));
        wr_util.addItem(diBuilder.getDocItem("excel/sum-formula", false));
        wr_util.addItem(diBuilder.getDocItem("excel/cell-address", false));
        wr_util.addItem(diBuilder.getDocItem("excel/auto-size-columns", false));
        wr_util.addItem(diBuilder.getDocItem("excel/auto-size-column", false));
        wr_util.addItem(diBuilder.getDocItem("excel/row-height", false));
        wr_util.addItem(diBuilder.getDocItem("excel/evaluate-formulas", false));
        wr_util.addItem(diBuilder.getDocItem("excel/convert->reader", false));

        final DocSection rd = new DocSection("Reader", id());
        all.addSection(rd);
        rd.addItem(diBuilder.getDocItem("excel/open", false));
        rd.addItem(diBuilder.getDocItem("excel/sheet", false));
        rd.addItem(diBuilder.getDocItem("excel/read-string-val", false));
        rd.addItem(diBuilder.getDocItem("excel/read-boolean-val", false));
        rd.addItem(diBuilder.getDocItem("excel/read-long-val", false));
        rd.addItem(diBuilder.getDocItem("excel/read-double-val", false));
        rd.addItem(diBuilder.getDocItem("excel/read-date-val", false));

        final DocSection rd_util = new DocSection("Reader Util", id());
        all.addSection(rd_util);
        rd_util.addItem(diBuilder.getDocItem("excel/sheet-count", false));
        rd_util.addItem(diBuilder.getDocItem("excel/sheet-name", false));
        rd_util.addItem(diBuilder.getDocItem("excel/sheet-row-range", false));
        rd_util.addItem(diBuilder.getDocItem("excel/sheet-col-range", false));
        rd_util.addItem(diBuilder.getDocItem("excel/evaluate-formulas", false));
        rd_util.addItem(diBuilder.getDocItem("excel/cell-empty?", false));
        rd_util.addItem(diBuilder.getDocItem("excel/cell-type", false));

        return section;
    }

    private List<DocSection> concat(
            final List<DocSection> s1,
            final List<DocSection> s2,
            final List<DocSection> s3,
            final List<DocSection> s4
    ) {
        final List<DocSection> list = new ArrayList<>();
        list.addAll(s1);
        list.addAll(s2);
        list.addAll(s3);
        list.addAll(s4);
        return list;
    }

    private void save(final File file, final String text) throws Exception {
        save(file, text.getBytes("UTF-8"));
    }

    private void save(final File file, final byte[] data) throws Exception {
        try(FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data, 0, data.length);
            fos.flush();
        }
    }

    private File getUserDir() {
        return new File(System.getProperty("user.dir"));
    }

    private Markdown loadVeniceDocMarkdown() {
        try {
            return Markdown.parse(
                        new ClassPathResource(getVeniceBasePath() + "docgen/venice-doc.md")
                            .getResourceAsString("UTF-8"));
        }
        catch(RuntimeException ex) {
            throw new RuntimeException("Failed to read 'venice-doc.md!", ex);
        }
    }

    private Markdown loadMarkdownDoc() {
        try {
            return Markdown.parse(
                        new ClassPathResource(getVeniceBasePath() + "docgen/markdown-doc.md")
                            .getResourceAsString("UTF-8"));
        }
        catch(RuntimeException ex) {
            throw new RuntimeException("Failed to read 'markdown-doc.md!", ex);
        }
    }

    private final void validateUniqueSectionsId(
            final List<DocSection> left,
            final List<DocSection> right
    ) {
        final Set<String> ids = new HashSet<>();

        left.forEach(s -> validateUniqueSectionId(s, ids));
        right.forEach(s -> validateUniqueSectionId(s, ids));
    }

    private final void validateUniqueSectionId(
            final DocSection section,
            final Set<String> ids
    ) {
        final String id = section.getId();
        if (id != null) {
            if (ids.contains(section.getId())) {
                throw new RuntimeException(
                        String.format(
                                "Non unique section id %s on section %s",
                                id, section.getTitle()));
            }
            ids.add(id);
        }

        // recursively validate children
        section.getSections().forEach(s -> validateUniqueSectionId(s, ids));
    }

    private String id() {
        return diBuilder.id();
    }


    private final DocItemBuilder diBuilder;
}
