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

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.RunMode;
import com.github.jlangch.venice.impl.VeniceInterpreter;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.repl.ReplFunctions;
import com.github.jlangch.venice.impl.specialforms.SpecialFormsDoc;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.markdown.Markdown;
import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;
import com.github.jlangch.venice.util.CapturingPrintStream;
import com.lowagie.text.pdf.PdfReader;


public class DocGenerator {

	public DocGenerator() {
		preloadedModules
			.addAll(Arrays.asList(
						"app",    "xml",    "crypt",     "gradle", 
						"trace",  "ansi",   "maven",     "kira",
						"java",   "semver", "excel",     "hexdump",
						"shell",  "geoip",  "benchmark", "component",
						"config"));
		
		final Env docEnv = new VeniceInterpreter(new AcceptAllInterceptor())
							.createEnv(
								preloadedModules, 
								false, 
								false, 
								RunMode.DOCGEN)
							.setStdoutPrintStream(null)
							.setStderrPrintStream(null);
		
		// make REPL specific functions available (e.g: 'repl/info')
		env = ReplFunctions.register(docEnv, null, null);
		
		codeHighlighter = new DocHighlighter(DocColorTheme.getLightTheme());
	}
	
	public static List<DocSection> docInfo() {
		return new DocGenerator().includeExamples(false).buildDocInfo();
	}

	public DocGenerator includeExamples(final boolean runExamples) {
		this.runExamples = runExamples;
		return this;
	}

	public void run(final String version) {
		try {	
			System.out.println("Creating cheatsheet V" + version);
			
			final List<DocSection> left = getLeftSections();
			final List<DocSection> right = getRightSections();
			final List<DocSection> leftModules = getModulesLeftSections();
			final List<DocSection> rightModules = getModulesRightSections();

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
		
		final DocSection functions = new DocSection("Core\u00A0Functions", "functions");
		functions.addSection(new DocSection("Functions", "functions"));
		functions.addSection(new DocSection("Macros", "macros"));
		functions.addSection(new DocSection("Special\u00A0Forms", "specialforms"));
		functions.addSection(new DocSection("Transducers", "transducers"));
		functions.addSection(new DocSection("Namespaces", "namespace"));
		functions.addSection(new DocSection("Types", "types"));
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

		final DocSection embed = new DocSection("Embedding", "embedding");
		embed.addSection(new DocSection("Embedding in Java", "embedding"));
		content.add(embed);

		final DocSection extmod = new DocSection("Modules", "modules");
		extmod.addSection(new DocSection("Kira\u00A0Templates", "modules.kira"));
		extmod.addSection(new DocSection("Tracing", "modules.tracing"));
		extmod.addSection(new DocSection("XML", "modules.xml"));
		extmod.addSection(new DocSection("Cryptography", "modules.cryptography"));
		extmod.addSection(new DocSection("Gradle", "modules.gradle"));
		extmod.addSection(new DocSection("Maven", "modules.maven"));
		extmod.addSection(new DocSection("Java", "modules.java"));
		extmod.addSection(new DocSection("Semver", "modules.semver"));
		extmod.addSection(new DocSection("Hexdump", "modules.hexdump"));
		extmod.addSection(new DocSection("Shell", "modules.shell"));
		extmod.addSection(new DocSection("Geo IP", "modules.geoip"));
		extmod.addSection(new DocSection("Ansi", "modules.ansi"));
		extmod.addSection(new DocSection("Benchmark", "modules.benchmark"));
		extmod.addSection(new DocSection("Configuration", "modules.config"));
		extmod.addSection(new DocSection("Component", "modules.component"));
		extmod.addSection(new DocSection("App", "modules.app"));
		content.add(extmod);

		return content;
	}

	private List<DocSection> getLeftSections() {
		return Arrays.asList(
				getPrimitivesSection(),
				getByteBufSection(),
				getRegexSection(),
				getTransducersSection(),
				getFunctionsSection(),
				getMacrosSection(),
				getSpecialFormsSection(),
				getExceptionsSection(),
				getTypesSection(),
				getNamespaceSection(),
				getJavaInteropSection(),
				getReplSection(),
				getPdfSection(),
				getIOZipSection(),
				getAppSection(),
				getCsvSection());
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
				getCidrSection());
	}

	private List<DocSection> getModulesLeftSections() {
		return Arrays.asList(
				getModuleKiraSection(),
				getModuleCryptographySection(),
				getModuleXmlSection(),
				getModuleJavaSection(),
				getModuleGradleSection(),
				getModuleMavenSection(),
				getModuleTracingSection(),
				getModuleShellSection(),
				getModuleAnsiSection(),
				getModuleBenchmarkSection(),
				getModuleComponentSection());
	}
	
	private List<DocSection> getModulesRightSections() {
		return Arrays.asList(
				getModuleHexdumpSection(),
				getModuleSemverSection(),
				getModuleGeoipSection(),
				getModuleExcelSection(),
				getModuleConfigSection(),
				getModuleAppSection());
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
		lit.addLiteralIem("Nil",                  "nil",                                     id());
		lit.addLiteralIem("Boolean",              "true, false",                             id());
		lit.addLiteralIem("Integer",              "150I, 1_000_000I, 0x1FFI",                id());
		lit.addLiteralIem("Long",                 "1500, 1_000_000, 0x00A055FF",             id());
		lit.addLiteralIem("Double",               "3.569, 2.0E+10",                          id());
		lit.addLiteralIem("BigDecimal",           "6.897M, 2.345E+10M",                      id());
		lit.addLiteralIem("BigInteger",           "1000N, 1_000_000N",                       id());
		lit.addLiteralIem("String",               "\"abcd\", \"ab\\\"cd\", \"PI: \\u03C0\"", id());
		lit.addLiteralIem("",                     "\"\"\"{ \"age\": 42 }\"\"\"",             id());
		lit.addLiteralIem("String interpolation", "\"~{x}\", \"\"\"~{x}\"\"\"",              id());
		lit.addLiteralIem("",                     "\"~(inc x)\", \"\"\"~(inc x)\"\"\"",      id());

		
		final DocSection numbers = new DocSection("Numbers", "primitives.numbers");
		section.addSection(numbers);

		final DocSection arithmetic = new DocSection("Arithmetic", "primitives.arithmetic");
		numbers.addSection(arithmetic);
		arithmetic.addItem(getDocItem("+"));
		arithmetic.addItem(getDocItem("-"));
		arithmetic.addItem(getDocItem("*"));
		arithmetic.addItem(getDocItem("/"));
		arithmetic.addItem(getDocItem("mod"));
		arithmetic.addItem(getDocItem("inc"));
		arithmetic.addItem(getDocItem("dec"));
		arithmetic.addItem(getDocItem("min"));
		arithmetic.addItem(getDocItem("max"));
		arithmetic.addItem(getDocItem("abs"));
		arithmetic.addItem(getDocItem("sgn"));
		arithmetic.addItem(getDocItem("negate"));
		arithmetic.addItem(getDocItem("floor"));
		arithmetic.addItem(getDocItem("ceil"));
		arithmetic.addItem(getDocItem("sqrt"));
		arithmetic.addItem(getDocItem("square"));
		arithmetic.addItem(getDocItem("pow"));
		arithmetic.addItem(getDocItem("log"));
		arithmetic.addItem(getDocItem("log10"));
		arithmetic.addItem(getDocItem("digits"));

		final DocSection convert = new DocSection("Convert", "primitives.convert");
		numbers.addSection(convert);
		convert.addItem(getDocItem("int"));
		convert.addItem(getDocItem("long"));
		convert.addItem(getDocItem("double"));
		convert.addItem(getDocItem("decimal"));
		convert.addItem(getDocItem("bigint"));

		final DocSection compare = new DocSection("Compare", "primitives.compare");
		numbers.addSection(compare);
		compare.addItem(getDocItem("=="));
		compare.addItem(getDocItem("="));
		compare.addItem(getDocItem("<"));
		compare.addItem(getDocItem(">"));
		compare.addItem(getDocItem("<="));
		compare.addItem(getDocItem(">="));
		compare.addItem(getDocItem("compare"));

		final DocSection test = new DocSection("Test", "primitives.test");
		numbers.addSection(test);
		test.addItem(getDocItem("zero?"));
		test.addItem(getDocItem("pos?"));
		test.addItem(getDocItem("neg?"));
		test.addItem(getDocItem("even?"));
		test.addItem(getDocItem("odd?"));
		test.addItem(getDocItem("number?"));
		test.addItem(getDocItem("int?"));
		test.addItem(getDocItem("long?"));
		test.addItem(getDocItem("double?"));
		test.addItem(getDocItem("decimal?"));

		final DocSection random = new DocSection("Random", "primitives.random");
		numbers.addSection(random);
		random.addItem(getDocItem("rand-long"));
		random.addItem(getDocItem("rand-double"));
		random.addItem(getDocItem("rand-gaussian"));
		
		final DocSection trigonometry = new DocSection("Trigonometry", "primitives.trigonometry");
		numbers.addSection(trigonometry);
		trigonometry.addItem(getDocItem("to-radians"));
		trigonometry.addItem(getDocItem("to-degrees"));
		trigonometry.addItem(getDocItem("sin"));
		trigonometry.addItem(getDocItem("cos"));
		trigonometry.addItem(getDocItem("tan"));
		
		final DocSection statistics = new DocSection("Statistics", "primitives.statistics");
		numbers.addSection(statistics);
		statistics.addItem(getDocItem("mean"));
		statistics.addItem(getDocItem("median"));
		statistics.addItem(getDocItem("quartiles"));
		statistics.addItem(getDocItem("quantile"));
		statistics.addItem(getDocItem("standard-deviation"));

		final DocSection bigdecimal = new DocSection("BigDecimal", "primitives.bigdecimal");
		numbers.addSection(bigdecimal);
		bigdecimal.addItem(getDocItem("dec/add"));
		bigdecimal.addItem(getDocItem("dec/sub"));
		bigdecimal.addItem(getDocItem("dec/mul"));
		bigdecimal.addItem(getDocItem("dec/div"));
		bigdecimal.addItem(getDocItem("dec/scale"));

		
		final DocSection strings = new DocSection("Strings", "primitives.strings");
		section.addSection(strings);

		final DocSection create = new DocSection("Create", "primitives.strings.create");
		strings.addSection(create);
		create.addItem(getDocItem("str"));
		create.addItem(getDocItem("str/format"));
		create.addItem(getDocItem("str/quote"));
		create.addItem(getDocItem("str/double-quote"));
		create.addItem(getDocItem("str/double-unquote"));

		final DocSection use = new DocSection("Use", "primitives.strings.use");
		strings.addSection(use);
		use.addItem(getDocItem("count"));
		use.addItem(getDocItem("compare"));
		use.addItem(getDocItem("empty-to-nil"));
		use.addItem(getDocItem("first"));
		use.addItem(getDocItem("last"));
		use.addItem(getDocItem("nth"));
		use.addItem(getDocItem("nfirst"));
		use.addItem(getDocItem("nlast"));
		use.addItem(getDocItem("seq"));
		use.addItem(getDocItem("rest"));
		use.addItem(getDocItem("butlast"));
		use.addItem(getDocItem("reverse"));
		use.addItem(getDocItem("shuffle"));
		use.addItem(getDocItem("str/index-of"));
		use.addItem(getDocItem("str/last-index-of"));
		use.addItem(getDocItem("str/subs"));
		use.addItem(getDocItem("str/rest"));
		use.addItem(getDocItem("str/butlast"));
		use.addItem(getDocItem("str/chars"));
		use.addItem(getDocItem("str/pos"));
		use.addItem(getDocItem("str/repeat"));
		use.addItem(getDocItem("str/reverse"));
		use.addItem(getDocItem("str/truncate"));
		use.addItem(getDocItem("str/expand"));
		use.addItem(getDocItem("str/lorem-ipsum"));
		
		final DocSection split = new DocSection("Split/Join", "primitives.strings.splitjoin");
		strings.addSection(split);
		split.addItem(getDocItem("str/split"));
		split.addItem(getDocItem("str/split-lines"));
		split.addItem(getDocItem("str/join"));
		
		final DocSection replace = new DocSection("Replace", "primitives.strings.replace");
		strings.addSection(replace);
		replace.addItem(getDocItem("str/replace-first"));
		replace.addItem(getDocItem("str/replace-last"));
		replace.addItem(getDocItem("str/replace-all"));
		
		final DocSection strip = new DocSection("Strip", "primitives.strings.strip");
		strings.addSection(strip);
		strip.addItem(getDocItem("str/strip-start"));
		strip.addItem(getDocItem("str/strip-end"));
		strip.addItem(getDocItem("str/strip-indent"));
		strip.addItem(getDocItem("str/strip-margin"));
		
		final DocSection conv = new DocSection("Conversion", "primitives.strings.conversion");
		strings.addSection(conv);
		conv.addItem(getDocItem("str/lower-case"));
		conv.addItem(getDocItem("str/upper-case"));
		conv.addItem(getDocItem("str/cr-lf", false));
		
		final DocSection regex = new DocSection("Regex", "primitives.strings.regex");
		strings.addSection(regex);
		regex.addItem(getDocItem("match?"));
		regex.addItem(getDocItem("not-match?"));

		final DocSection trim = new DocSection("Trim", "primitives.strings.trim");
		strings.addSection(trim);
		trim.addItem(getDocItem("str/trim"));
		trim.addItem(getDocItem("str/trim-to-nil"));

		final DocSection hex = new DocSection("Hex", "primitives.strings.hex");
		strings.addSection(hex);
		hex.addItem(getDocItem("str/hex-to-bytebuf"));
		hex.addItem(getDocItem("str/bytebuf-to-hex"));
		hex.addItem(getDocItem("str/format-bytebuf"));

		final DocSection encode = new DocSection("Encode/Decode", "primitives.strings.encode");
		strings.addSection(encode);
		encode.addItem(getDocItem("str/encode-base64"));
		encode.addItem(getDocItem("str/decode-base64"));
		encode.addItem(getDocItem("str/encode-url"));
		encode.addItem(getDocItem("str/decode-url"));
		encode.addItem(getDocItem("str/escape-html"));
		encode.addItem(getDocItem("str/escape-xml"));


		final DocSection validation = new DocSection("Validation", "primitives.strings.validation");
		strings.addSection(validation);
		validation.addItem(getDocItem("str/valid-email-addr?"));

		final DocSection str_test = new DocSection("Test", "primitives.strings.test");
		strings.addSection(str_test);
		str_test.addItem(getDocItem("string?"));
		str_test.addItem(getDocItem("empty?"));
		str_test.addItem(getDocItem("not-empty?"));
		str_test.addItem(getDocItem("str/blank?"));
		str_test.addItem(getDocItem("str/starts-with?"));
		str_test.addItem(getDocItem("str/ends-with?"));
		str_test.addItem(getDocItem("str/contains?"));
		str_test.addItem(getDocItem("str/equals-ignore-case?"));
		str_test.addItem(getDocItem("str/quoted?"));
		str_test.addItem(getDocItem("str/double-quoted?"));

		final DocSection str_test_char = new DocSection("Test char", "primitives.strings.testchar");
		strings.addSection(str_test_char);
		str_test_char.addItem(getDocItem("str/char?"));
		str_test_char.addItem(getDocItem("str/digit?"));
		str_test_char.addItem(getDocItem("str/letter?"));
		str_test_char.addItem(getDocItem("str/whitespace?"));
		str_test_char.addItem(getDocItem("str/linefeed?"));
		str_test_char.addItem(getDocItem("str/lower-case?"));
		str_test_char.addItem(getDocItem("str/upper-case?"));

		final DocSection str_leven_char = new DocSection("Other", "primitives.strings.other");
		strings.addSection(str_leven_char);
		str_leven_char.addItem(getDocItem("str/levenshtein"));

		
		final DocSection chars = new DocSection("Chars", "primitives.chars");
		section.addSection(chars);

		final DocSection charuse = new DocSection("Use", id());
		chars.addSection(charuse);		
		charuse.addItem(getDocItem("char"));
		charuse.addItem(getDocItem("char?"));

		
		final DocSection other = new DocSection("Other", "primitives.other");
		section.addSection(other);

		final DocSection nil = new DocSection("Nil", id());
		other.addSection(nil);
		nil.addItem(getDocItem("nil?"));
		nil.addItem(getDocItem("some?"));
		

		final DocSection keywords = new DocSection("Keywords", "primitives.other.keywords");
		other.addSection(keywords);
		keywords.addItem(new DocItem(":a :blue", null));
		keywords.addItem(getDocItem("keyword?"));
		keywords.addItem(getDocItem("keyword"));

		final DocSection symbols = new DocSection("Symbols", "primitives.other.symbols");
		other.addSection(symbols);
		symbols.addItem(new DocItem("'a 'blue", null));
		symbols.addItem(getDocItem("symbol?"));
		symbols.addItem(getDocItem("symbol"));

		final DocSection just = new DocSection("Just", "primitives.other.just");
		other.addSection(just);
		just.addItem(getDocItem("just"));
		just.addItem(getDocItem("just?"));

		final DocSection boolean_ = new DocSection("Boolean", "primitives.other.boolean");
		other.addSection(boolean_);
		boolean_.addItem(getDocItem("boolean"));
		boolean_.addItem(getDocItem("not"));
		boolean_.addItem(getDocItem("boolean?"));
		boolean_.addItem(getDocItem("true?"));
		boolean_.addItem(getDocItem("false?"));

		return section;
	}

	private DocSection getCollectionsSection() {
		final DocSection section = new DocSection("Collections", "collections");


		final DocSection collections = new DocSection("Collections", "collections.collections");
		section.addSection(collections);
		
		final DocSection generic = new DocSection("Generic", "collections.collections.generic");
		collections.addSection(generic);
		generic.addItem(getDocItem("count"));
		generic.addItem(getDocItem("compare"));
		generic.addItem(getDocItem("empty-to-nil"));
		generic.addItem(getDocItem("empty"));
		generic.addItem(getDocItem("into"));
		generic.addItem(getDocItem("cons"));
		generic.addItem(getDocItem("conj"));
		generic.addItem(getDocItem("remove"));
		generic.addItem(getDocItem("repeat"));
		generic.addItem(getDocItem("repeatedly"));
		generic.addItem(getDocItem("cycle"));
		generic.addItem(getDocItem("replace"));
		generic.addItem(getDocItem("range"));
		generic.addItem(getDocItem("group-by"));
		generic.addItem(getDocItem("frequencies"));
		generic.addItem(getDocItem("get-in"));
		generic.addItem(getDocItem("seq"));
		generic.addItem(getDocItem("reverse"));
		generic.addItem(getDocItem("shuffle"));

		final DocSection coll_test = new DocSection("Tests", "collections.collections.tests");
		collections.addSection(coll_test);
		coll_test.addItem(getDocItem("empty?"));
		coll_test.addItem(getDocItem("not-empty?"));
		coll_test.addItem(getDocItem("coll?"));
		coll_test.addItem(getDocItem("list?"));
		coll_test.addItem(getDocItem("vector?"));
		coll_test.addItem(getDocItem("set?"));
		coll_test.addItem(getDocItem("sorted-set?"));
		coll_test.addItem(getDocItem("mutable-set?"));
		coll_test.addItem(getDocItem("map?"));
		coll_test.addItem(getDocItem("sequential?"));
		coll_test.addItem(getDocItem("hash-map?"));
		coll_test.addItem(getDocItem("ordered-map?"));
		coll_test.addItem(getDocItem("sorted-map?"));
		coll_test.addItem(getDocItem("mutable-map?"));
		coll_test.addItem(getDocItem("bytebuf?"));

		final DocSection coll_process = new DocSection("Process", "collections.collections.process");
		collections.addSection(coll_process);
		coll_process.addItem(getDocItem("map"));
		coll_process.addItem(getDocItem("map-indexed"));
		coll_process.addItem(getDocItem("filter"));
		coll_process.addItem(getDocItem("reduce"));
		coll_process.addItem(getDocItem("keep"));
		coll_process.addItem(getDocItem("docoll"));

		
		final DocSection lists = new DocSection("Lists", "collections.lists");
		section.addSection(lists);

		final DocSection list_create = new DocSection("Create", "collections.lists.create");
		lists.addSection(list_create);
		list_create.addItem(getDocItem("()"));
		list_create.addItem(getDocItem("list"));
		list_create.addItem(getDocItem("list*"));
		list_create.addItem(getDocItem("mutable-list"));

		final DocSection list_access = new DocSection("Access", "collections.lists.access");
		lists.addSection(list_access);
		list_access.addItem(getDocItem("first"));
		list_access.addItem(getDocItem("second"));
		list_access.addItem(getDocItem("third"));
		list_access.addItem(getDocItem("fourth"));
		list_access.addItem(getDocItem("nth"));
		list_access.addItem(getDocItem("last"));
		list_access.addItem(getDocItem("peek"));
		list_access.addItem(getDocItem("rest"));
		list_access.addItem(getDocItem("butlast"));
		list_access.addItem(getDocItem("nfirst"));
		list_access.addItem(getDocItem("nlast"));
		list_access.addItem(getDocItem("sublist"));
		list_access.addItem(getDocItem("some"));

		final DocSection list_modify = new DocSection("Modify", "collections.lists.modify");
		lists.addSection(list_modify);
		list_modify.addItem(getDocItem("cons"));
		list_modify.addItem(getDocItem("conj"));
		list_modify.addItem(getDocItem("rest"));
		list_modify.addItem(getDocItem("pop"));
		list_modify.addItem(getDocItem("into"));
		list_modify.addItem(getDocItem("concat"));
		list_modify.addItem(getDocItem("distinct"));
		list_modify.addItem(getDocItem("dedupe"));
		list_modify.addItem(getDocItem("partition"));
		list_modify.addItem(getDocItem("partition-by"));
		list_modify.addItem(getDocItem("interpose"));
		list_modify.addItem(getDocItem("interleave"));
		list_modify.addItem(getDocItem("cartesian-product"));
		list_modify.addItem(getDocItem("combinations"));
		list_modify.addItem(getDocItem("mapcat"));
		list_modify.addItem(getDocItem("flatten"));
		list_modify.addItem(getDocItem("sort"));
		list_modify.addItem(getDocItem("sort-by"));
		list_modify.addItem(getDocItem("take"));
		list_modify.addItem(getDocItem("take-while"));
		list_modify.addItem(getDocItem("take-last"));
		list_modify.addItem(getDocItem("drop"));
		list_modify.addItem(getDocItem("drop-while"));
		list_modify.addItem(getDocItem("drop-last"));
		list_modify.addItem(getDocItem("split-at"));
		list_modify.addItem(getDocItem("split-with"));
	
		final DocSection list_test = new DocSection("Test", "collections.lists.test");
		lists.addSection(list_test);
		list_test.addItem(getDocItem("list?"));
		list_test.addItem(getDocItem("mutable-list?"));
		list_test.addItem(getDocItem("every?"));
		list_test.addItem(getDocItem("not-every?"));
		list_test.addItem(getDocItem("any?"));
		list_test.addItem(getDocItem("not-any?"));
		
		
		final DocSection vectors = new DocSection("Vectors", "collections.vectors");
		section.addSection(vectors);

		final DocSection vec_create = new DocSection("Create", "collections.vectors.create");
		vectors.addSection(vec_create);
		vec_create.addItem(getDocItem("[]"));
		vec_create.addItem(getDocItem("vector"));
		vec_create.addItem(getDocItem("vector*"));
		vec_create.addItem(getDocItem("mutable-vector"));
		vec_create.addItem(getDocItem("mapv"));

		final DocSection vec_access = new DocSection("Access", "collections.vectors.access");
		vectors.addSection(vec_access);
		vec_access.addItem(getDocItem("first"));
		vec_access.addItem(getDocItem("second"));
		vec_access.addItem(getDocItem("third"));
		vec_access.addItem(getDocItem("nth"));
		vec_access.addItem(getDocItem("last"));
		vec_access.addItem(getDocItem("peek"));
		vec_access.addItem(getDocItem("butlast"));
		vec_access.addItem(getDocItem("rest"));
		vec_access.addItem(getDocItem("nfirst"));
		vec_access.addItem(getDocItem("nlast"));
		vec_access.addItem(getDocItem("subvec"));
		vec_access.addItem(getDocItem("some"));

		final DocSection vec_modify = new DocSection("Modify", "collections.vectors.modify");
		vectors.addSection(vec_modify);
		vec_modify.addItem(getDocItem("cons"));
		vec_modify.addItem(getDocItem("conj"));
		vec_modify.addItem(getDocItem("rest"));
		vec_modify.addItem(getDocItem("pop"));
		vec_modify.addItem(getDocItem("into"));
		vec_modify.addItem(getDocItem("concat"));
		vec_modify.addItem(getDocItem("distinct"));
		vec_modify.addItem(getDocItem("dedupe"));
		vec_modify.addItem(getDocItem("partition"));
		vec_modify.addItem(getDocItem("partition-by"));
		vec_modify.addItem(getDocItem("interpose"));
		vec_modify.addItem(getDocItem("interleave"));
		vec_modify.addItem(getDocItem("cartesian-product"));
		vec_modify.addItem(getDocItem("combinations"));
		vec_modify.addItem(getDocItem("mapcat"));
		vec_modify.addItem(getDocItem("flatten"));
		vec_modify.addItem(getDocItem("sort"));
		vec_modify.addItem(getDocItem("sort-by"));
		vec_modify.addItem(getDocItem("take"));
		vec_modify.addItem(getDocItem("take-while"));
		vec_modify.addItem(getDocItem("take-last"));
		vec_modify.addItem(getDocItem("drop"));
		vec_modify.addItem(getDocItem("drop-while"));
		vec_modify.addItem(getDocItem("drop-last"));
		vec_modify.addItem(getDocItem("update"));
		vec_modify.addItem(getDocItem("update!"));
		vec_modify.addItem(getDocItem("split-with"));
		
		final DocSection vec_nested = new DocSection("Nested", "collections.vectors.nested");
		vectors.addSection(vec_nested);
		vec_nested.addItem(getDocItem("get-in"));
		vec_nested.addItem(getDocItem("assoc-in"));
		vec_nested.addItem(getDocItem("update-in"));
		vec_nested.addItem(getDocItem("dissoc-in"));
			
		final DocSection vec_test = new DocSection("Test", "collections.vectors.test");
		vectors.addSection(vec_test);
		vec_test.addItem(getDocItem("vector?"));
		vec_test.addItem(getDocItem("mutable-vector?"));
		vec_test.addItem(getDocItem("contains?"));
		vec_test.addItem(getDocItem("not-contains?"));
		vec_test.addItem(getDocItem("every?"));
		vec_test.addItem(getDocItem("not-every?"));
		vec_test.addItem(getDocItem("any?"));
		vec_test.addItem(getDocItem("not-any?"));
	
		
		final DocSection sets = new DocSection("Sets", "collections.sets");
		section.addSection(sets);

		final DocSection set_create = new DocSection("Create", "collections.sets.create");
		sets.addSection(set_create);
		set_create.addItem(getDocItem("#{}"));
		set_create.addItem(getDocItem("set"));
		set_create.addItem(getDocItem("sorted-set"));
		set_create.addItem(getDocItem("mutable-set"));

		final DocSection set_modify = new DocSection("Modify", "collections.sets.modify");
		sets.addSection(set_modify);
		set_modify.addItem(getDocItem("cons"));
		set_modify.addItem(getDocItem("cons!"));
		set_modify.addItem(getDocItem("conj"));
		set_modify.addItem(getDocItem("conj!"));
		set_modify.addItem(getDocItem("disj"));

		final DocSection algebra = new DocSection("Algebra", "collections.sets.algebra");
		sets.addSection(algebra);
		algebra.addItem(getDocItem("difference"));
		algebra.addItem(getDocItem("union"));
		algebra.addItem(getDocItem("intersection"));
		algebra.addItem(getDocItem("subset?"));
		algebra.addItem(getDocItem("superset?"));

		final DocSection set_test = new DocSection("Test", "collections.sets.test");
		sets.addSection(set_test);
		set_test.addItem(getDocItem("set?"));
		set_test.addItem(getDocItem("sorted-set?"));
		set_test.addItem(getDocItem("mutable-set?"));
		set_test.addItem(getDocItem("contains?"));
		set_test.addItem(getDocItem("not-contains?"));
		set_test.addItem(getDocItem("every?"));
		set_test.addItem(getDocItem("not-every?"));
		set_test.addItem(getDocItem("any?"));
		set_test.addItem(getDocItem("not-any?"));

		
		final DocSection maps = new DocSection("Maps", "collections.maps");
		section.addSection(maps);

		final DocSection maps_create = new DocSection("Create", "collections.maps.create");
		maps.addSection(maps_create);
		maps_create.addItem(getDocItem("{}"));
		maps_create.addItem(getDocItem("hash-map"));
		maps_create.addItem(getDocItem("ordered-map"));
		maps_create.addItem(getDocItem("sorted-map"));
		maps_create.addItem(getDocItem("mutable-map"));
		maps_create.addItem(getDocItem("zipmap"));
		

		final DocSection map_access = new DocSection("Access", "collections.maps.access");
		maps.addSection(map_access);
		map_access.addItem(getDocItem("find"));
		map_access.addItem(getDocItem("get"));
		map_access.addItem(getDocItem("keys"));
		map_access.addItem(getDocItem("vals"));

		final DocSection map_modify = new DocSection("Modify", "collections.maps.modify");
		maps.addSection(map_modify);
		map_modify.addItem(getDocItem("cons"));
		map_modify.addItem(getDocItem("conj"));
		map_modify.addItem(getDocItem("assoc"));
		map_modify.addItem(getDocItem("assoc!"));
		map_modify.addItem(getDocItem("update"));
		map_modify.addItem(getDocItem("update!"));
		map_modify.addItem(getDocItem("dissoc"));
		map_modify.addItem(getDocItem("dissoc!"));
		map_modify.addItem(getDocItem("into"));
		map_modify.addItem(getDocItem("concat"));
		map_modify.addItem(getDocItem("flatten"));
		map_modify.addItem(getDocItem("filter-k"));
		map_modify.addItem(getDocItem("filter-kv"));
		map_modify.addItem(getDocItem("reduce-kv"));
		map_modify.addItem(getDocItem("merge"));
		map_modify.addItem(getDocItem("merge-with"));
		map_modify.addItem(getDocItem("merge-deep"));
		map_modify.addItem(getDocItem("map-invert"));
		map_modify.addItem(getDocItem("map-keys"));
		map_modify.addItem(getDocItem("map-vals"));

		final DocSection map_entries = new DocSection("Entries", "collections.maps.entries");
		maps.addSection(map_entries);
		map_entries.addItem(getDocItem("map-entry"));
		map_entries.addItem(getDocItem("key"));
		map_entries.addItem(getDocItem("val"));
		map_entries.addItem(getDocItem("entries"));
		map_entries.addItem(getDocItem("map-entry?"));

		final DocSection map_nested = new DocSection("Nested", "collections.maps.nested");
		maps.addSection(map_nested);
		map_nested.addItem(getDocItem("get-in"));
		map_nested.addItem(getDocItem("assoc-in"));
		map_nested.addItem(getDocItem("update-in"));
		map_nested.addItem(getDocItem("dissoc-in"));
		
		final DocSection map_test = new DocSection("Test", "collections.maps.test");
		maps.addSection(map_test);
		map_test.addItem(getDocItem("map?"));
		map_test.addItem(getDocItem("sequential?"));
		map_test.addItem(getDocItem("hash-map?"));
		map_test.addItem(getDocItem("ordered-map?"));
		map_test.addItem(getDocItem("sorted-map?"));
		map_test.addItem(getDocItem("mutable-map?"));
		map_test.addItem(getDocItem("contains?"));
		map_test.addItem(getDocItem("not-contains?"));

		
		final DocSection stacks = new DocSection("Stack", "collections.stack");
		section.addSection(stacks);

		final DocSection stacks_create = new DocSection("Create", "collections.stack.create");
		stacks.addSection(stacks_create);
		stacks_create.addItem(getDocItem("stack"));
		
		final DocSection stacks_access = new DocSection("Access", "collections.stack.access");
		stacks.addSection(stacks_access);
		stacks_access.addItem(getDocItem("peek"));
		stacks_access.addItem(getDocItem("pop!"));
		stacks_access.addItem(getDocItem("push!"));
		stacks_access.addItem(getDocItem("count"));
	
		final DocSection stacks_test = new DocSection("Test", "collections.stack.test");
		stacks.addSection(stacks_test);
		stacks_test.addItem(getDocItem("empty?"));
		stacks_test.addItem(getDocItem("stack?"));

		
		final DocSection queues = new DocSection("Queue", "collections.queue");
		section.addSection(queues);

		final DocSection queues_create = new DocSection("Create", "collections.queue.create");
		queues.addSection(queues_create);
		queues_create.addItem(getDocItem("queue"));
		

		final DocSection queues_access = new DocSection("Access", "collections.queue.access");
		queues.addSection(queues_access);
		queues_access.addItem(getDocItem("peek"));
		queues_access.addItem(getDocItem("poll!"));
		queues_access.addItem(getDocItem("offer!"));
		queues_access.addItem(getDocItem("count"));

		
		final DocSection queues_test = new DocSection("Test", "collections.queue.test");
		queues.addSection(queues_test);
		queues_test.addItem(getDocItem("empty?"));
		queues_test.addItem(getDocItem("queue?"));

		
		final DocSection dag = new DocSection("DAG", "directed acyclic graph", "collections.dag");
		section.addSection(dag);

		final DocSection dag_create = new DocSection("Create", "collections.dag.create");
		dag.addSection(dag_create);
		dag_create.addItem(getDocItem("dag/dag"));
		dag_create.addItem(getDocItem("dag/add-edges"));
		dag_create.addItem(getDocItem("dag/add-nodes"));
		
		final DocSection dag_access = new DocSection("Access", "collections.dag.access");
		dag.addSection(dag_access);
		dag_access.addItem(getDocItem("dag/nodes"));
		dag_access.addItem(getDocItem("dag/edges"));
		dag_access.addItem(getDocItem("dag/roots"));
		dag_access.addItem(getDocItem("count"));
		
		final DocSection dag_children = new DocSection("Children", "collections.dag.children");
		dag.addSection(dag_children);
		dag_children.addItem(getDocItem("dag/children"));
		dag_children.addItem(getDocItem("dag/direct-children"));
		
		final DocSection dag_parents = new DocSection("Parents", "collections.dag.parents");
		dag.addSection(dag_parents);
		dag_parents.addItem(getDocItem("dag/parents"));
		dag_parents.addItem(getDocItem("dag/direct-parents"));

		final DocSection dag_sort = new DocSection("Sort", "collections.dag.sort");
		dag.addSection(dag_sort);
		dag_sort.addItem(getDocItem("dag/topological-sort"));
		dag_sort.addItem(getDocItem("dag/compare-fn"));

		final DocSection dag_test = new DocSection("Test", "collections.dag.test");
		dag.addSection(dag_test);
		dag_test.addItem(getDocItem("dag/dag?"));
		dag_test.addItem(getDocItem("dag/node?"));
		dag_test.addItem(getDocItem("dag/parent-of?"));
		dag_test.addItem(getDocItem("dag/child-of?"));
		dag_test.addItem(getDocItem("empty?"));

		return section;
	}		

	private DocSection getLazySequencesSection() {
		final DocSection section = new DocSection("Lazy Sequences", "lazyseq");

		final DocSection all = new DocSection("", id());
		section.addSection(all);

		final DocSection create = new DocSection("Create", "lazyseq.create");
		all.addSection(create);
		create.addItem(getDocItem("lazy-seq"));

		final DocSection realize = new DocSection("Realize", "lazyseq.realize");
		all.addSection(realize);
		realize.addItem(getDocItem("doall"));

		final DocSection test = new DocSection("Test", "lazyseq.test");
		all.addSection(test);
		test.addItem(getDocItem("lazy-seq?"));

		return section;
	}

	private DocSection getArraysSection() {
		final DocSection section = new DocSection("Arrays", "arrays");

		final DocSection all = new DocSection("", id());
		section.addSection(all);

		final DocSection create = new DocSection("Create", "arrays.create");
		all.addSection(create);
		create.addItem(getDocItem("make-array"));
		create.addItem(getDocItem("object-array"));
		create.addItem(getDocItem("string-array"));
		create.addItem(getDocItem("int-array"));
		create.addItem(getDocItem("long-array"));
		create.addItem(getDocItem("float-array"));
		create.addItem(getDocItem("double-array"));

		final DocSection use = new DocSection("Use", "arrays.use");
		all.addSection(use);
		use.addItem(getDocItem("aget"));
		use.addItem(getDocItem("aset"));
		use.addItem(getDocItem("alength"));
		use.addItem(getDocItem("asub"));
		use.addItem(getDocItem("acopy"));
		use.addItem(getDocItem("amap"));

		return section;
	}

	private DocSection getRegexSection() {
		final DocSection section = new DocSection("Regex", "regex");

		final DocSection all = new DocSection("", id());
		section.addSection(all);

		final DocSection general = new DocSection("General", "regex.general");
		all.addSection(general);
		general.addItem(getDocItem("regex/pattern"));
		general.addItem(getDocItem("regex/matcher"));
		general.addItem(getDocItem("regex/reset"));
		general.addItem(getDocItem("regex/matches?"));
		general.addItem(getDocItem("regex/matches"));
		general.addItem(getDocItem("regex/group"));
		general.addItem(getDocItem("regex/count"));
		general.addItem(getDocItem("regex/find?"));
		general.addItem(getDocItem("regex/find"));
		general.addItem(getDocItem("regex/find-all"));
		general.addItem(getDocItem("regex/find+"));
		general.addItem(getDocItem("regex/find-all+"));

		return section;
	}
	
	private DocSection getFunctionsSection() {
		final DocSection section = new DocSection("Functions", "functions");

		final DocSection all = new DocSection("", id());
		section.addSection(all);

		final DocSection create = new DocSection("Create", "functions.create");
		all.addSection(create);
		create.addItem(getDocItem("fn"));
		create.addItem(getDocItem("defn"));
		create.addItem(getDocItem("defn-"));
		create.addItem(getDocItem("identity"));
		create.addItem(getDocItem("comp"));
		create.addItem(getDocItem("partial"));
		create.addItem(getDocItem("memoize"));
		create.addItem(getDocItem("juxt"));
		create.addItem(getDocItem("fnil"));
		create.addItem(getDocItem("trampoline"));
		create.addItem(getDocItem("complement"));
		create.addItem(getDocItem("constantly"));
		create.addItem(getDocItem("every-pred"));
		create.addItem(getDocItem("any-pred"));

		final DocSection call = new DocSection("Call", "functions.call");
		all.addSection(call);
		call.addItem(getDocItem("apply"));
		call.addItem(getDocItem("->"));
		call.addItem(getDocItem("->>"));

		final DocSection test = new DocSection("Test", "functions.test");
		all.addSection(test);
		test.addItem(getDocItem("fn?"));

		final DocSection misc = new DocSection("Misc", "functions.misc");
		all.addSection(misc);
		misc.addItem(getDocItem("nil?"));
		misc.addItem(getDocItem("some?"));
		misc.addItem(getDocItem("eval"));
		misc.addItem(getDocItem("name"));
		misc.addItem(getDocItem("callstack"));
		misc.addItem(getDocItem("coalesce"));
		misc.addItem(getDocItem("load-resource"));

		final DocSection env = new DocSection("Environment", "functions.environment");
		all.addSection(env);
		env.addItem(getDocItem("set!"));
		env.addItem(getDocItem("resolve"));
		env.addItem(getDocItem("bound?"));
		env.addItem(getDocItem("var-get"));
		env.addItem(getDocItem("var-name"));
		env.addItem(getDocItem("var-ns"));
		env.addItem(getDocItem("var-thread-local?"));
		env.addItem(getDocItem("var-local?"));
		env.addItem(getDocItem("var-global?"));
		env.addItem(getDocItem("name"));
		env.addItem(getDocItem("namespace"));
		
		final DocSection walk = new DocSection("Tree Walker", "functions.treewalker");
		all.addSection(walk);
		walk.addItem(getDocItem("prewalk"));
		walk.addItem(getDocItem("postwalk"));

		final DocSection meta = new DocSection("Meta", "functions.meta");
		all.addSection(meta);
		meta.addItem(getDocItem("meta"));
		meta.addItem(getDocItem("with-meta"));
		meta.addItem(getDocItem("vary-meta"));

		final DocSection doc = new DocSection("Documentation", "functions.doc");
		all.addSection(doc);
		doc.addItem(getDocItem("doc", false));
		doc.addItem(getDocItem("modules"));

		final DocSection def = new DocSection("Definiton", "functions.def");
		all.addSection(def);
		def.addItem(getDocItem("fn-body"));
		def.addItem(getDocItem("fn-pre-conditions"));

		final DocSection syntax = new DocSection("Syntax", "functions.syntax");
		all.addSection(syntax);
		syntax.addItem(getDocItem("highlight"));
		
		return section;
	}

	private DocSection getExceptionsSection() {
		final DocSection section = new DocSection("Exceptions", "exceptions");

		final DocSection all = new DocSection("", id());
		section.addSection(all);

		final DocSection block = new DocSection("Throw/Catch", "exceptions.block");
		all.addSection(block);
		block.addItem(getDocItem("try", true, true));
		block.addItem(getDocItem("try-with", true, true));
		block.addItem(getDocItem("throw", true, true));
		
		final DocSection create = new DocSection("Create", "exceptions.create");
		all.addSection(create);
		create.addItem(getDocItem("ex"));
		
		final DocSection test = new DocSection("Test", "exceptions.test");
		all.addSection(test);
		test.addItem(getDocItem("ex?"));
		test.addItem(getDocItem("ex-venice?"));

		final DocSection util = new DocSection("Util", "exceptions.util");
		all.addSection(util);
		util.addItem(getDocItem("ex-message"));
		util.addItem(getDocItem("ex-cause"));
		util.addItem(getDocItem("ex-value"));

		final DocSection stacktrace = new DocSection("Stacktrace", "exceptions.stacktrace");
		all.addSection(stacktrace);
		stacktrace.addItem(getDocItem("ex-venice-stacktrace"));
		stacktrace.addItem(getDocItem("ex-java-stacktrace", false, true));
		
		return section;
	}

	private DocSection getSystemSection() {
		final DocSection section = new DocSection("System", "system");

		final DocSection all = new DocSection("", id());
		section.addSection(all);

		final DocSection venice = new DocSection("Venice", "system.venice");
		all.addSection(venice);
		venice.addItem(getDocItem("version"));
		venice.addItem(getDocItem("sandboxed?"));
		venice.addItem(getDocItem("sandbox-type"));

		final DocSection system = new DocSection("System", "system.system");
		all.addSection(system);
		system.addItem(getDocItem("system-prop"));
		system.addItem(getDocItem("system-env"));
		system.addItem(getDocItem("system-exit-code"));
		system.addItem(getDocItem("charset-default-encoding"));
		
		final DocSection java = new DocSection("Java", "system.java");
		all.addSection(java);
		java.addItem(getDocItem("java-version"));
		java.addItem(getDocItem("java-version-info"));
		java.addItem(getDocItem("java-major-version"));
		java.addItem(getDocItem("java-source-location", false));		
		
		final DocSection javaVM = new DocSection("Java VM", "system.java-vm");
		all.addSection(javaVM);
		javaVM.addItem(getDocItem("pid"));
		javaVM.addItem(getDocItem("gc"));
		javaVM.addItem(getDocItem("total-memory"));
		javaVM.addItem(getDocItem("used-memory"));

		final DocSection os = new DocSection("OS", "system.os");
		all.addSection(os);
		os.addItem(getDocItem("os-type"));
		os.addItem(getDocItem("os-type?"));
		os.addItem(getDocItem("os-arch"));
		os.addItem(getDocItem("os-name"));
		os.addItem(getDocItem("os-version"));

		final DocSection time = new DocSection("Time", "system.time");
		all.addSection(time);
		time.addItem(getDocItem("current-time-millis"));
		time.addItem(getDocItem("nano-time"));
		time.addItem(getDocItem("format-nano-time"));
		time.addItem(getDocItem("format-micro-time"));
		time.addItem(getDocItem("format-milli-time"));

		final DocSection host = new DocSection("Host", "system.host");
		all.addSection(host);
		host.addItem(getDocItem("host-name"));
		host.addItem(getDocItem("host-address"));
		host.addItem(getDocItem("ip-private?"));
		host.addItem(getDocItem("cpus"));

		final DocSection user = new DocSection("User", "system.user");
		all.addSection(user);
		user.addItem(getDocItem("user-name"));
		user.addItem(getDocItem("io/user-home-dir"));

		final DocSection util = new DocSection("Util", "system.util");
		all.addSection(util);
		util.addItem(getDocItem("uuid"));
		util.addItem(getDocItem("sleep"));
		util.addItem(getDocItem("shutdown-hook"));

		final DocSection shell = new DocSection("Shell", "system.shell");
		all.addSection(shell);
		shell.addItem(getDocItem("sh", false));
		shell.addItem(getDocItem("with-sh-dir", false));
		shell.addItem(getDocItem("with-sh-env", false));
		shell.addItem(getDocItem("with-sh-throw", false));

		final DocSection tools = new DocSection("Shell Tools", "system.shell.tools");
		all.addSection(tools);
		tools.addItem(getDocItem("sh/open", false));
		tools.addItem(getDocItem("sh/pwd", false));
				
		return section;
	}

	private DocSection getMacrosSection() {
		final DocSection section = new DocSection("Macros", "macros");

		final DocSection all = new DocSection("", id());
		section.addSection(all);

		final DocSection create = new DocSection("Create", "macros.create");
		all.addSection(create);		
		create.addItem(getDocItem("defn"));
		create.addItem(getDocItem("defn-"));
		create.addItem(getDocItem("defmacro"));
		create.addItem(getDocItem("macroexpand"));
		create.addItem(getDocItem("macroexpand-all"));

		final DocSection quote = new DocSection("Quoting", "macros.quoting");
		all.addSection(quote);
		quote.addItem(getDocItem("quote"));
		quote.addItem(getDocItem("quasiquote"));

		final DocSection branch = new DocSection("Branch", "macros.branch");
		all.addSection(branch);
		branch.addItem(getDocItem("and"));
		branch.addItem(getDocItem("or"));
		branch.addItem(getDocItem("when"));
		branch.addItem(getDocItem("when-not"));
		branch.addItem(getDocItem("if-not"));
		branch.addItem(getDocItem("if-let"));
		branch.addItem(getDocItem("when-let"));

		final DocSection loop = new DocSection("Loop", "macros.loop");
		all.addSection(loop);
		loop.addItem(getDocItem("while"));
		loop.addItem(getDocItem("dotimes"));
		loop.addItem(getDocItem("list-comp"));
		loop.addItem(getDocItem("doseq"));

		final DocSection call = new DocSection("Call", "macros.call");
		all.addSection(call);
		call.addItem(getDocItem("doto"));
		call.addItem(getDocItem("->"));
		call.addItem(getDocItem("->>"));
		call.addItem(getDocItem("-<>"));
		call.addItem(getDocItem("as->"));
		call.addItem(getDocItem("cond->"));
		call.addItem(getDocItem("cond->>"));
		call.addItem(getDocItem("some->"));
		call.addItem(getDocItem("some->>"));

		final DocSection loading = new DocSection("Loading", "macros.loading");
		all.addSection(loading);
		loading.addItem(getDocItem("load-module"));
		loading.addItem(getDocItem("load-file", false));
		loading.addItem(getDocItem("load-classpath-file"));
		loading.addItem(getDocItem("load-string"));
		
		final DocSection test = new DocSection("Test", "macros.test");
		all.addSection(test);
		test.addItem(getDocItem("macro?"));
		test.addItem(getDocItem("cond"));
		test.addItem(getDocItem("condp"));
		test.addItem(getDocItem("case"));

		final DocSection assert_ = new DocSection("Assert", "macros.assert");
		all.addSection(assert_);
		assert_.addItem(getDocItem("assert", true, true));

		final DocSection util = new DocSection("Util", "macros.util");
		all.addSection(util);
		util.addItem(getDocItem("comment"));
		util.addItem(getDocItem("gensym"));
		util.addItem(getDocItem("time"));
		util.addItem(getDocItem("with-out-str"));
		util.addItem(getDocItem("with-err-str"));
		
		final DocSection profil = new DocSection("Profiling", "macros.profiling");
		all.addSection(profil);
		profil.addItem(getDocItem("time"));
		profil.addItem(getDocItem("perf", false));
		
		return section;
	}

	private DocSection getTypesSection() {
		final DocSection section = new DocSection("Types", "types");

		final DocSection all = new DocSection("", id());
		section.addSection(all);

		final DocSection util = new DocSection("Util", "types.util");
		all.addSection(util);		
		util.addItem(getDocItem("type"));
		util.addItem(getDocItem("supertype"));
		util.addItem(getDocItem("supertypes"));

		final DocSection test = new DocSection("Test", "types.test");
		all.addSection(test);		
		test.addItem(getDocItem("instance-of?"));
		test.addItem(getDocItem("deftype?"));

		final DocSection define = new DocSection("Define", "types.define");
		all.addSection(define);		
		define.addItem(getDocItem("deftype"));
		define.addItem(getDocItem("deftype-of"));
		define.addItem(getDocItem("deftype-or"));

		final DocSection create = new DocSection("Create", "types.create");
		all.addSection(create);
		create.addItem(getDocItem(".:"));

		final DocSection describe = new DocSection("Describe", "types.describe");
		all.addSection(describe);
		describe.addItem(getDocItem("deftype-describe"));
		
		return section;
	}

	private DocSection getTransducersSection() {
		final DocSection section = new DocSection("Transducers", "transducers");

		final DocSection all = new DocSection("", id());
		section.addSection(all);

		final DocSection run = new DocSection("Use", "transducers.use");
		all.addSection(run);		
		run.addItem(getDocItem("transduce"));

		final DocSection func = new DocSection("Functions", "transducers.functions");
		all.addSection(func);		
		func.addItem(getDocItem("map"));
		func.addItem(getDocItem("map-indexed"));
		func.addItem(getDocItem("filter"));
		func.addItem(getDocItem("drop"));
		func.addItem(getDocItem("drop-while"));
		func.addItem(getDocItem("drop-last"));
		func.addItem(getDocItem("take"));
		func.addItem(getDocItem("take-while"));
		func.addItem(getDocItem("take-last"));
		func.addItem(getDocItem("keep"));
		func.addItem(getDocItem("remove"));
		func.addItem(getDocItem("dedupe"));
		func.addItem(getDocItem("distinct"));
		func.addItem(getDocItem("sorted"));
		func.addItem(getDocItem("reverse"));
		func.addItem(getDocItem("flatten"));
		func.addItem(getDocItem("halt-when"));

		final DocSection red = new DocSection("Reductions", "transducers.reductions");
		all.addSection(red);		
		red.addItem(getDocItem("rf-first"));
		red.addItem(getDocItem("rf-last"));
		red.addItem(getDocItem("rf-every?"));
		red.addItem(getDocItem("rf-any?"));
		
		final DocSection early = new DocSection("Early", "transducers.early");
		all.addSection(early);		
		early.addItem(getDocItem("reduced"));
		early.addItem(getDocItem("reduced?"));
		early.addItem(getDocItem("deref"));
		early.addItem(getDocItem("deref?"));
		
		return section;
	}

	private DocSection getConcurrencySection() {
		final DocSection section = new DocSection("Concurrency", "concurrency");

		final DocSection all = new DocSection("", id());
		section.addSection(all);

		final DocSection atoms = new DocSection("Atoms", "concurrency.atoms");
		all.addSection(atoms);
		atoms.addItem(getDocItem("atom"));
		atoms.addItem(getDocItem("atom?"));
		atoms.addItem(getDocItem("deref"));
		atoms.addItem(getDocItem("deref?"));
		atoms.addItem(getDocItem("reset!"));
		atoms.addItem(getDocItem("swap!"));
		atoms.addItem(getDocItem("swap-vals!"));
		atoms.addItem(getDocItem("compare-and-set!"));
		atoms.addItem(getDocItem("add-watch"));
		atoms.addItem(getDocItem("remove-watch"));

		final DocSection futures = new DocSection("Futures", "concurrency.futures");
		all.addSection(futures);
		futures.addItem(getDocItem("future"));
		futures.addItem(getDocItem("future-task"));
		futures.addItem(getDocItem("future?"));
		futures.addItem(getDocItem("futures-fork"));
		futures.addItem(getDocItem("futures-wait"));
		futures.addItem(getDocItem("futures-thread-pool-info"));
		futures.addItem(getDocItem("done?"));
		futures.addItem(getDocItem("cancel"));
		futures.addItem(getDocItem("cancelled?"));
		futures.addItem(getDocItem("deref"));
		futures.addItem(getDocItem("deref?"));
		futures.addItem(getDocItem("realized?"));

		final DocSection promises = new DocSection("Promises", "concurrency.promises");
		all.addSection(promises);
		promises.addItem(getDocItem("promise"));
		promises.addItem(getDocItem("promise?"));
		promises.addItem(getDocItem("deliver"));
		promises.addItem(getDocItem("realized?"));
		promises.addItem(getDocItem("then-accept"));
		promises.addItem(getDocItem("then-accept-both"));
		promises.addItem(getDocItem("then-apply"));
		promises.addItem(getDocItem("then-combine"));
		promises.addItem(getDocItem("then-compose"));
		promises.addItem(getDocItem("when-complete"));
		promises.addItem(getDocItem("accept-either"));
		promises.addItem(getDocItem("apply-to-either"));
		promises.addItem(getDocItem("all-of"));
		promises.addItem(getDocItem("any-of"));
		promises.addItem(getDocItem("or-timeout", true, true));
		promises.addItem(getDocItem("complete-on-timeout", true, true));
		promises.addItem(getDocItem("timeout-after", true, true));
		promises.addItem(getDocItem("done?"));
		promises.addItem(getDocItem("cancel"));
		promises.addItem(getDocItem("cancelled?"));

		final DocSection delay = new DocSection("Delay", "concurrency.delay");
		all.addSection(delay);
		delay.addItem(getDocItem("delay"));
		delay.addItem(getDocItem("delay?"));
		delay.addItem(getDocItem("deref"));
		delay.addItem(getDocItem("deref?"));
		delay.addItem(getDocItem("force"));
		delay.addItem(getDocItem("realized?"));

		final DocSection agents = new DocSection("Agents", "concurrency.agents");
		all.addSection(agents);
		agents.addItem(getDocItem("agent"));
		agents.addItem(getDocItem("send"));
		agents.addItem(getDocItem("send-off"));
		agents.addItem(getDocItem("restart-agent"));
		agents.addItem(getDocItem("set-error-handler!"));
		agents.addItem(getDocItem("agent-error"));
		agents.addItem(getDocItem("await"));
		agents.addItem(getDocItem("await-for"));
		agents.addItem(getDocItem("shutdown-agents", false));
		agents.addItem(getDocItem("shutdown-agents?", false));
		agents.addItem(getDocItem("await-termination-agents", false));
		agents.addItem(getDocItem("await-termination-agents?", false));
		agents.addItem(getDocItem("agent-send-thread-pool-info"));
		agents.addItem(getDocItem("agent-send-off-thread-pool-info"));
		
		
		final DocSection sched = new DocSection("Scheduler", "concurrency.scheduler");
		all.addSection(sched);
		sched.addItem(getDocItem("schedule-delay", false));
		sched.addItem(getDocItem("schedule-at-fixed-rate", false));

		final DocSection locking = new DocSection("Locking", "concurrency.locking");
		all.addSection(locking);
		locking.addItem(getDocItem("locking"));

		final DocSection volatiles = new DocSection("Volatiles", "concurrency.volatiles");
		all.addSection(volatiles);
		volatiles.addItem(getDocItem("volatile"));
		volatiles.addItem(getDocItem("volatile?"));
		volatiles.addItem(getDocItem("deref"));
		volatiles.addItem(getDocItem("deref?"));
		volatiles.addItem(getDocItem("reset!"));
		volatiles.addItem(getDocItem("swap!"));
		
		final DocSection thlocal = new DocSection("ThreadLocal", "concurrency.threadlocal");
		all.addSection(thlocal);
		thlocal.addItem(getDocItem("thread-local"));
		thlocal.addItem(getDocItem("thread-local?"));
		thlocal.addItem(getDocItem("thread-local-clear"));
		thlocal.addItem(getDocItem("thread-local-map"));
		thlocal.addItem(getDocItem("assoc"));
		thlocal.addItem(getDocItem("dissoc"));
		thlocal.addItem(getDocItem("get"));
		thlocal.addItem(getDocItem("binding"));
		thlocal.addItem(getDocItem("def-dynamic"));

		final DocSection threads = new DocSection("Threads", "concurrency.threads");
		all.addSection(threads);
		threads.addItem(getDocItem("thread-id"));
		threads.addItem(getDocItem("thread-name"));
		threads.addItem(getDocItem("thread-daemon?"));
		threads.addItem(getDocItem("thread-interrupted?"));
		threads.addItem(getDocItem("thread-interrupted"));

		return section;
	}

	private DocSection getIOSection() {
		final DocSection section = new DocSection("I/O", "io.util");

		final DocSection all = new DocSection("", id());
		section.addSection(all);

		final DocSection to = new DocSection("to", "io.to");
		all.addSection(to);
		to.addItem(getDocItem("print"));
		to.addItem(getDocItem("println"));
		to.addItem(getDocItem("printf"));
		to.addItem(getDocItem("flush"));
		to.addItem(getDocItem("newline"));

		final DocSection to_str = new DocSection("to-str", "io.tostr");
		all.addSection(to_str);
		to_str.addItem(getDocItem("pr-str"));
		to_str.addItem(getDocItem("with-out-str"));

		final DocSection from = new DocSection("from", "io.from");
		all.addSection(from);
		from.addItem(getDocItem("read-line"));
		from.addItem(getDocItem("read-string"));

		final DocSection classpath = new DocSection("classpath", "io.classpath");
		all.addSection(classpath);
		classpath.addItem(getDocItem("io/load-classpath-resource", false));
		classpath.addItem(getDocItem("io/classpath-resource?", false));
		
		final DocSection stream = new DocSection("stream", "io.stream");
		all.addSection(stream);
		stream.addItem(getDocItem("io/copy-stream"));
		stream.addItem(getDocItem("io/slurp-stream"));
		stream.addItem(getDocItem("io/spit-stream"));
		stream.addItem(getDocItem("io/uri-stream", false));
		stream.addItem(getDocItem("io/file-in-stream", false));
		stream.addItem(getDocItem("io/string-in-stream", false));
		stream.addItem(getDocItem("io/bytebuf-in-stream", false));
		stream.addItem(getDocItem("io/wrap-os-with-buffered-writer"));
		stream.addItem(getDocItem("io/wrap-os-with-print-writer"));
		stream.addItem(getDocItem("io/wrap-is-with-buffered-reader"));

		final DocSection rd_wr = new DocSection("reader/writer", "io.readerwriter");
		all.addSection(rd_wr);
		rd_wr.addItem(getDocItem("io/buffered-reader"));
		rd_wr.addItem(getDocItem("io/buffered-writer"));

		final DocSection http = new DocSection("http", "io.http");
		all.addSection(http);
		http.addItem(getDocItem("io/download", false));
		http.addItem(getDocItem("io/internet-avail?", false));

		final DocSection other = new DocSection("other", "io.other");
		all.addSection(other);
		other.addItem(getDocItem("with-out-str"));
		other.addItem(getDocItem("io/mime-type"));
		other.addItem(getDocItem("io/default-charset"));

		return section;
	}

	private DocSection getIOFileSection() {
		final DocSection section = new DocSection("File I/O", "io.file");

		final DocSection all = new DocSection("", id());
		section.addSection(all);

		final DocSection file = new DocSection("file", "io.file_");
		all.addSection(file);
		file.addItem(getDocItem("io/file"));
		file.addItem(getDocItem("io/file-parent"));
		file.addItem(getDocItem("io/file-name"));
		file.addItem(getDocItem("io/file-path"));
		file.addItem(getDocItem("io/file-absolute-path"));
		file.addItem(getDocItem("io/file-canonical-path"));
		file.addItem(getDocItem("io/file-ext"));
		file.addItem(getDocItem("io/file-ext?"));
		file.addItem(getDocItem("io/file-size", false));
		file.addItem(getDocItem("io/file-last-modified", false));

		final DocSection file_dir = new DocSection("file dir", "io.filedir");
		all.addSection(file_dir);
		file_dir.addItem(getDocItem("io/mkdir"));
		file_dir.addItem(getDocItem("io/mkdirs"));

		final DocSection file_io = new DocSection("file i/o", "io.fileio");
		all.addSection(file_io);
		file_io.addItem(getDocItem("io/slurp"));
		file_io.addItem(getDocItem("io/slurp-lines"));
		file_io.addItem(getDocItem("io/spit"));
		file_io.addItem(getDocItem("io/copy-file"));
		file_io.addItem(getDocItem("io/move-file"));
		file_io.addItem(getDocItem("io/delete-file"));
		file_io.addItem(getDocItem("io/delete-file-on-exit"));
		file_io.addItem(getDocItem("io/delete-file-tree"));

		final DocSection file_list = new DocSection("file list", "io.filelist");
		all.addSection(file_list);
		file_list.addItem(getDocItem("io/list-files", false));
		file_list.addItem(getDocItem("io/list-files-glob", false));
		file_list.addItem(getDocItem("io/list-file-tree", false));

		final DocSection file_test = new DocSection("file test", "io.filetest");
		all.addSection(file_test);
		file_test.addItem(getDocItem("io/file?"));
		file_test.addItem(getDocItem("io/exists-file?"));
		file_test.addItem(getDocItem("io/exists-dir?"));
		file_test.addItem(getDocItem("io/file-can-read?", false));
		file_test.addItem(getDocItem("io/file-can-write?", false));
		file_test.addItem(getDocItem("io/file-can-execute?", false));
		file_test.addItem(getDocItem("io/file-hidden?", false));
		file_test.addItem(getDocItem("io/file-symbolic-link?", false));
		
		final DocSection file_uri = new DocSection("URL/URI", "io.url_uri");
		all.addSection(file_uri);
		file_uri.addItem(getDocItem("io/->url"));
		file_uri.addItem(getDocItem("io/->uri"));

		final DocSection file_watch = new DocSection("file watch", "io.filewatch");
		all.addSection(file_watch);
		file_watch.addItem(getDocItem("io/await-for", false));
		file_watch.addItem(getDocItem("io/watch-dir", false));
		file_watch.addItem(getDocItem("io/close-watcher", false));
		
		final DocSection file_other = new DocSection("file other", "io.fileother");
		all.addSection(file_other);
		file_other.addItem(getDocItem("io/temp-file"));
		file_other.addItem(getDocItem("io/tmp-dir"));
		file_other.addItem(getDocItem("io/user-dir"));
		file_other.addItem(getDocItem("io/user-home-dir"));

		return section;
	}

	private DocSection getIOZipSection() {
		final DocSection section = new DocSection("Zip/GZip", "io.zip");

		final DocSection all = new DocSection("", id());
		section.addSection(all);
		
		final DocSection zip = new DocSection("zip", "io.zip_");
		all.addSection(zip);
		zip.addItem(getDocItem("io/zip", false));
		zip.addItem(getDocItem("io/zip-file", false));
		zip.addItem(getDocItem("io/zip-list", false));
		zip.addItem(getDocItem("io/zip-list-entry-names", false));
		zip.addItem(getDocItem("io/zip-append", false));
		zip.addItem(getDocItem("io/zip-remove", false));
		zip.addItem(getDocItem("io/zip?"));
		zip.addItem(getDocItem("io/unzip"));
		zip.addItem(getDocItem("io/unzip-first"));
		zip.addItem(getDocItem("io/unzip-nth"));
		zip.addItem(getDocItem("io/unzip-all"));
		zip.addItem(getDocItem("io/unzip-to-dir", false));

		final DocSection gzip = new DocSection("gzip", "io.gzip");
		all.addSection(gzip);
		gzip.addItem(getDocItem("io/gzip", false));
		gzip.addItem(getDocItem("io/gzip-to-stream"));
		gzip.addItem(getDocItem("io/gzip?"));
		gzip.addItem(getDocItem("io/ungzip"));
		gzip.addItem(getDocItem("io/ungzip-to-stream"));

		return section;
	}

	private DocSection getAppSection() {
		final DocSection section = new DocSection("Application", "application");

		final DocSection all = new DocSection("", id());
		section.addSection(all);

		final DocSection mgmt = new DocSection("Management", "application.management");
		all.addSection(mgmt);
		mgmt.addItem(getDocItem("app/build"));
		mgmt.addItem(getDocItem("app/manifest"));

		return section;
	}

	private DocSection getNamespaceSection() {
		final DocSection section = new DocSection("Namespace", "namespace");

		final DocSection all = new DocSection("", id());
		section.addSection(all);

		final DocSection open = new DocSection("Open", "namespace.open");
		all.addSection(open);
		open.addItem(getDocItem("ns"));

		final DocSection curr = new DocSection("Current", "namespace.current");
		all.addSection(curr);
		curr.addItem(getDocItem("*ns*"));

		final DocSection remove = new DocSection("Remove", "namespace.remove");
		all.addSection(remove);
		remove.addItem(getDocItem("ns-unmap"));
		remove.addItem(getDocItem("ns-remove"));

		final DocSection util = new DocSection("Util", "namespace.util");
		all.addSection(util);
		util.addItem(getDocItem("ns-list"));
		util.addItem(getDocItem("namespace"));
	
		return section;
	}

	private DocSection getByteBufSection() {
		final DocSection section = new DocSection("Byte Buffer", "bytebuf");

		final DocSection all = new DocSection("", id());
		section.addSection(all);

		final DocSection bb_create = new DocSection("Create", "bytebuf.create");
		all.addSection(bb_create);
		bb_create.addItem(getDocItem("bytebuf"));
		bb_create.addItem(getDocItem("bytebuf-allocate"));
		bb_create.addItem(getDocItem("bytebuf-from-string"));
		
		final DocSection bb_test = new DocSection("Test", "bytebuf.test");
		all.addSection(bb_test);
		bb_test.addItem(getDocItem("empty?"));
		bb_test.addItem(getDocItem("not-empty?"));
		bb_test.addItem(getDocItem("bytebuf?"));

		final DocSection bb_use = new DocSection("Use", "bytebuf.use");
		all.addSection(bb_use);
		bb_use.addItem(getDocItem("count"));
		bb_use.addItem(getDocItem("bytebuf-capacity"));
		bb_use.addItem(getDocItem("bytebuf-limit"));
		bb_use.addItem(getDocItem("bytebuf-to-string"));
		bb_use.addItem(getDocItem("bytebuf-to-list"));
		bb_use.addItem(getDocItem("bytebuf-sub"));
		bb_use.addItem(getDocItem("bytebuf-pos"));
		bb_use.addItem(getDocItem("bytebuf-pos!"));

		final DocSection bb_read = new DocSection("Read", "bytebuf.read");
		all.addSection(bb_read);
		bb_read.addItem(getDocItem("bytebuf-get-byte"));
		bb_read.addItem(getDocItem("bytebuf-get-int"));
		bb_read.addItem(getDocItem("bytebuf-get-long"));
		bb_read.addItem(getDocItem("bytebuf-get-float"));
		bb_read.addItem(getDocItem("bytebuf-get-double"));

		final DocSection bb_write = new DocSection("Write", "bytebuf.write");
		all.addSection(bb_write);
		bb_write.addItem(getDocItem("bytebuf-put-byte!"));
		bb_write.addItem(getDocItem("bytebuf-put-int!"));
		bb_write.addItem(getDocItem("bytebuf-put-long!"));
		bb_write.addItem(getDocItem("bytebuf-put-float!"));
		bb_write.addItem(getDocItem("bytebuf-put-double!"));
		bb_write.addItem(getDocItem("bytebuf-put-buf!"));

		final DocSection encode = new DocSection("Base64", "bytebuf.base64");
		all.addSection(encode);
		encode.addItem(getDocItem("str/encode-base64"));
		encode.addItem(getDocItem("str/decode-base64"));

		final DocSection hex = new DocSection("Hex", "bytebuf.hex");
		all.addSection(hex);
		hex.addItem(getDocItem("str/hex-to-bytebuf"));
		hex.addItem(getDocItem("str/bytebuf-to-hex"));
		hex.addItem(getDocItem("str/format-bytebuf"));

		return section;
	}

	private DocSection getTimeSection() {
		final DocSection section = new DocSection("Time", "time");

		final DocSection all = new DocSection("", id());
		section.addSection(all);

		final DocSection date = new DocSection("Date", "time.date");
		all.addSection(date);
		date.addItem(getDocItem("time/date"));
		date.addItem(getDocItem("time/date?"));

		final DocSection local_date = new DocSection("Local Date", "time.localdate");
		all.addSection(local_date);
		local_date.addItem(getDocItem("time/local-date"));
		local_date.addItem(getDocItem("time/local-date?"));
		local_date.addItem(getDocItem("time/local-date-parse"));

		final DocSection local_date_time = new DocSection("Local Date Time", "time.localdatetime");
		all.addSection(local_date_time);
		local_date_time.addItem(getDocItem("time/local-date-time"));
		local_date_time.addItem(getDocItem("time/local-date-time?"));
		local_date_time.addItem(getDocItem("time/local-date-time-parse"));

		final DocSection zoned_date_time = new DocSection("Zoned Date Time", "time.zoneddatetime");
		all.addSection(zoned_date_time);
		zoned_date_time.addItem(getDocItem("time/zoned-date-time"));
		zoned_date_time.addItem(getDocItem("time/zoned-date-time?"));
		zoned_date_time.addItem(getDocItem("time/zoned-date-time-parse"));
		
		final DocSection fields = new DocSection("Fields", "time.fields");
		all.addSection(fields);
		fields.addItem(getDocItem("time/year"));
		fields.addItem(getDocItem("time/month"));
		fields.addItem(getDocItem("time/day-of-week"));
		fields.addItem(getDocItem("time/day-of-month"));
		fields.addItem(getDocItem("time/day-of-year"));
		fields.addItem(getDocItem("time/hour"));
		fields.addItem(getDocItem("time/minute"));
		fields.addItem(getDocItem("time/second"));

		final DocSection etc = new DocSection("Fields etc", "time.fieldsetc");
		all.addSection(etc);
		etc.addItem(getDocItem("time/length-of-year"));
		etc.addItem(getDocItem("time/length-of-month"));
		etc.addItem(getDocItem("time/first-day-of-month"));
		etc.addItem(getDocItem("time/last-day-of-month"));
		
		final DocSection zone = new DocSection("Zone", "time.zone");
		all.addSection(zone);
		zone.addItem(getDocItem("time/zone"));
		zone.addItem(getDocItem("time/zone-offset"));

		final DocSection format = new DocSection("Format", "time.format");
		all.addSection(format);
		format.addItem(getDocItem("time/formatter"));
		format.addItem(getDocItem("time/format"));
		
		final DocSection compare = new DocSection("Test", "time.test");
		all.addSection(compare);
		compare.addItem(getDocItem("time/after?"));
		compare.addItem(getDocItem("time/not-after?"));
		compare.addItem(getDocItem("time/before?"));
		compare.addItem(getDocItem("time/not-before?"));
		compare.addItem(getDocItem("time/within?"));
		compare.addItem(getDocItem("time/leap-year?"));
		
		final DocSection misc = new DocSection("Miscellaneous", "time.misc");
		all.addSection(misc);
		misc.addItem(getDocItem("time/with-time"));
		misc.addItem(getDocItem("time/plus"));
		misc.addItem(getDocItem("time/minus"));
		misc.addItem(getDocItem("time/period"));
		misc.addItem(getDocItem("time/earliest"));
		misc.addItem(getDocItem("time/latest"));

		final DocSection util = new DocSection("Util", "time.util");
		all.addSection(util);
		util.addItem(getDocItem("time/zone-ids"));
		util.addItem(getDocItem("time/to-millis"));

		return section;
	}

	private DocSection getSpecialFormsSection() {
		final DocSection section = new DocSection("Special Forms", "specialforms");

		final DocSection all = new DocSection("", id());
		section.addSection(all);

		final DocSection generic = new DocSection("Forms", "specialforms.forms");
		all.addSection(generic);

		generic.addItem(getDocItem("def"));
		generic.addItem(getDocItem("defonce"));
		generic.addItem(getDocItem("def-dynamic"));
		generic.addItem(getDocItem("if"));
		generic.addItem(getDocItem("do"));
		generic.addItem(getDocItem("let"));
		generic.addItem(getDocItem("binding"));
		generic.addItem(getDocItem("fn"));
		generic.addItem(getDocItem("set!"));

		final DocSection multi = new DocSection("Multi Methods", "specialforms.multimethod");
		all.addSection(multi);
		multi.addItem(getDocItem("defmulti"));
		multi.addItem(getDocItem("defmethod"));

		final DocSection proto = new DocSection("Protocols", "specialforms.protocol");
		all.addSection(proto);
		proto.addItem(getDocItem("defprotocol"));
		proto.addItem(getDocItem("extend"));
		proto.addItem(getDocItem("extends?"));

		final DocSection recur = new DocSection("Recursion", "specialforms.recursion");
		all.addSection(recur);
		recur.addItem(getDocItem("loop"));
		recur.addItem(getDocItem("recur"));
		recur.addItem(getDocItem("tail-pos", true, true));

		final DocSection ex = new DocSection("Exception", "specialforms.exception");
		all.addSection(ex);
		ex.addItem(getDocItem("throw", true, true));
		ex.addItem(getDocItem("try", true, true));
		ex.addItem(getDocItem("try-with", true, true));

		final DocSection profiling = new DocSection("Profiling", "specialforms.profiling");
		all.addSection(profiling);

		profiling.addItem(getDocItem("dobench"));
		profiling.addItem(getDocItem("dorun"));
		profiling.addItem(getDocItem("prof"));

		return section;
	}

	private DocSection getJavaInteropSection() {
		final DocSection section = new DocSection("Java Interoperability", "javainterop");

		final DocSection all = new DocSection("", id());
		section.addSection(all);
			
		final DocSection java = new DocSection("Java", "javainterop.java");
		all.addSection(java);	
		java.addItem(getDocItem("."));
		java.addItem(getDocItem("import"));
		java.addItem(getDocItem("java-iterator-to-list"));
		java.addItem(getDocItem("java-enumeration-to-list"));
		java.addItem(getDocItem("java-unwrap-optional"));
		java.addItem(getDocItem("cast"));
		java.addItem(getDocItem("class"));
		
		final DocSection proxy = new DocSection("Proxify", "javainterop.proxify");
		all.addSection(proxy);	
		proxy.addItem(getDocItem("proxify"));
		proxy.addItem(getDocItem("as-runnable"));
		proxy.addItem(getDocItem("as-callable"));
		proxy.addItem(getDocItem("as-predicate"));
		proxy.addItem(getDocItem("as-function"));
		proxy.addItem(getDocItem("as-consumer"));
		proxy.addItem(getDocItem("as-supplier"));
		proxy.addItem(getDocItem("as-bipredicate"));
		proxy.addItem(getDocItem("as-bifunction"));
		proxy.addItem(getDocItem("as-biconsumer"));
		proxy.addItem(getDocItem("as-binaryoperator"));

		final DocSection test = new DocSection("Test", "javainterop.test");
		all.addSection(test);	
		test.addItem(getDocItem("java-obj?"));
		test.addItem(getDocItem("exists-class?"));

		final DocSection support = new DocSection("Support", "javainterop.support");
		all.addSection(support);	
		support.addItem(getDocItem("imports"));
		support.addItem(getDocItem("supers"));
		support.addItem(getDocItem("bases"));
		support.addItem(getDocItem("formal-type"));
		support.addItem(getDocItem("stacktrace", false, false));

		final DocSection clazz = new DocSection("Classes", "javainterop.classes");
		all.addSection(clazz);	
		clazz.addItem(getDocItem("class"));
		clazz.addItem(getDocItem("class-of"));
		clazz.addItem(getDocItem("class-name"));
		clazz.addItem(getDocItem("class-version"));
		clazz.addItem(getDocItem("classloader"));
		clazz.addItem(getDocItem("classloader-of"));

		final DocSection jar = new DocSection("JARs", "javainterop.jar");
		all.addSection(jar);	
		jar.addItem(getDocItem("jar-maven-manifest-version"));
		jar.addItem(getDocItem("java-package-version"));
		
		final DocSection modules = new DocSection("Modules", "javainterop.modules");
		all.addSection(modules);	
		modules.addItem(getDocItem("module-name", false));
		
		return section;
	}

	private DocSection getReplSection() {
		final DocSection section = new DocSection("REPL", "repl");
	
		final DocSection all = new DocSection("", id());
		section.addSection(all);
			
		final DocSection repl = new DocSection("Info", "repl.info");
		all.addSection(repl);	
		repl.addItem(getDocItem("repl/info", false));
		
		final DocSection term = new DocSection("Terminal", "repl.terminal");
		all.addSection(term);	
		term.addItem(getDocItem("repl/term-rows", false));
		term.addItem(getDocItem("repl/term-cols", false));
		
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
		pdf.addItem(getDocItem("pdf/render", false));
		pdf.addItem(getDocItem("pdf/text-to-pdf", false));
		pdf.addItem(getDocItem("pdf/available?", false));
		pdf.addItem(getDocItem("pdf/check-required-libs", false));
		
		final DocSection pdf_tools = new DocSection("PDF Tools", "pdf.pdftools");
		all.addSection(pdf_tools);
		pdf_tools.addItem(getDocItem("pdf/merge", false));
		pdf_tools.addItem(getDocItem("pdf/copy", false));
		pdf_tools.addItem(getDocItem("pdf/pages"));
		pdf_tools.addItem(getDocItem("pdf/watermark", false));

		return section;
	}
	
	private DocSection getSystemVarSection() {
		final DocSection section = new DocSection("System Vars", "sysvars");
	
		final DocSection all = new DocSection("", id());
		section.addSection(all);

		final DocSection other = new DocSection("System Vars", "sysvars.var");
		all.addSection(other);
		other.addItem(getDocItem("*version*"));
		other.addItem(getDocItem("*newline*"));
		other.addItem(getDocItem("*loaded-modules*"));
		other.addItem(getDocItem("*loaded-files*"));
		other.addItem(getDocItem("*ns*"));
		other.addItem(getDocItem("*run-mode*"));
		other.addItem(getDocItem("*ansi-term*"));

		return section;
	}
	
	private DocSection getJsonSection() {
		final DocSection section = new DocSection("JSON", "json");

		
		final DocSection all = new DocSection("", id());
		section.addSection(all);
		
		final DocSection read = new DocSection("read", "json.read");
		all.addSection(read);
		read.addItem(getDocItem("json/read-str"));
		read.addItem(getDocItem("json/slurp"));
		
		final DocSection write = new DocSection("write", "json.write");
		all.addSection(write);
		write.addItem(getDocItem("json/write-str"));
		write.addItem(getDocItem("json/spit"));
		
		final DocSection prettify = new DocSection("prettify", "json.prettify");
		all.addSection(prettify);
		prettify.addItem(getDocItem("json/pretty-print"));

		return section;
	}
	
	private DocSection getCsvSection() {
		final DocSection section = new DocSection("CSV", "csv");
	
		final DocSection all = new DocSection("", id());
		section.addSection(all);

		final DocSection read = new DocSection("read", "csv.read");
		all.addSection(read);
		read.addItem(getDocItem("csv/read"));

		final DocSection write = new DocSection("write", "csv.write");
		all.addSection(write);
		write.addItem(getDocItem("csv/write", false));
		write.addItem(getDocItem("csv/write-str"));

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
		cidr.addItem(getDocItem("cidr/parse"));
		cidr.addItem(getDocItem("cidr/in-range?"));
		cidr.addItem(getDocItem("cidr/start-inet-addr"));
		cidr.addItem(getDocItem("cidr/end-inet-addr"));
		cidr.addItem(getDocItem("cidr/inet-addr"));
		cidr.addItem(getDocItem("cidr/inet-addr-to-bytes"));
		cidr.addItem(getDocItem("cidr/inet-addr-from-bytes"));
		
		final DocSection cidr_trie = new DocSection("CIDR Trie", "cidr.cidrtrie");
		all.addSection(cidr_trie);
		cidr_trie.addItem(getDocItem("cidr/trie"));
		cidr_trie.addItem(getDocItem("cidr/size"));
		cidr_trie.addItem(getDocItem("cidr/insert"));
		cidr_trie.addItem(getDocItem("cidr/lookup"));
		cidr_trie.addItem(getDocItem("cidr/lookup-reverse"));

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
		kira.addItem(getDocItem("kira/eval"));
		kira.addItem(getDocItem("kira/fn"));

		final DocSection escape = new DocSection("Escape", id());
		all.addSection(escape);
		escape.addItem(getDocItem("kira/escape-xml"));
		escape.addItem(getDocItem("kira/escape-html"));

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
		trace.addItem(getDocItem("trace/trace"));
		trace.addItem(getDocItem("trace/trace-var"));
		trace.addItem(getDocItem("trace/untrace-var"));

		final DocSection test = new DocSection("Test", id());
		all.addSection(test);
		test.addItem(getDocItem("trace/traced?"));
		test.addItem(getDocItem("trace/traceable?"));

		final DocSection util = new DocSection("Util", id());
		all.addSection(util);
		util.addItem(getDocItem("trace/trace-str-limit"));

		final DocSection tee = new DocSection("Tee", id());
		all.addSection(tee);
		tee.addItem(getDocItem("trace/tee->"));
		tee.addItem(getDocItem("trace/tee->>"));
		tee.addItem(getDocItem("trace/tee"));

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
		trace.addItem(getDocItem("shell/open", false));
		trace.addItem(getDocItem("shell/open-macos-app", false));

		final DocSection test = new DocSection("Process", id());
		all.addSection(test);
		test.addItem(getDocItem("shell/kill", false));
		test.addItem(getDocItem("shell/kill-forcibly", false));
		test.addItem(getDocItem("shell/wait-for-process-exit", false));
		test.addItem(getDocItem("shell/alive?", false));
		test.addItem(getDocItem("shell/pid", false));
		test.addItem(getDocItem("shell/process-handle", false));
		test.addItem(getDocItem("shell/process-handle?", false));
		test.addItem(getDocItem("shell/process-info", false));
		test.addItem(getDocItem("shell/processes", false));
		test.addItem(getDocItem("shell/processes-info", false));
		test.addItem(getDocItem("shell/descendant-processes", false));
		test.addItem(getDocItem("shell/parent-process", false));

		final DocSection util = new DocSection("Util", id());
		all.addSection(util);
		util.addItem(getDocItem("shell/diff", false));

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
		xml.addItem(getDocItem("xml/parse-str"));
		xml.addItem(getDocItem("xml/parse"));
		xml.addItem(getDocItem("xml/path->"));
		xml.addItem(getDocItem("xml/children"));
		xml.addItem(getDocItem("xml/text"));

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
		hashes.addItem(getDocItem("crypt/md5-hash"));
		hashes.addItem(getDocItem("crypt/sha1-hash"));
		hashes.addItem(getDocItem("crypt/sha512-hash"));
		hashes.addItem(getDocItem("crypt/pbkdf2-hash"));

		final DocSection crypt = new DocSection("Encrypt", id());
		all.addSection(crypt);
		crypt.addItem(getDocItem("crypt/encrypt"));
		crypt.addItem(getDocItem("crypt/decrypt"));

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
		gradle.addItem(getDocItem("gradle/with-home", false));
		gradle.addItem(getDocItem("gradle/version", false));
		gradle.addItem(getDocItem("gradle/task", false));

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
		maven.addItem(getDocItem("maven/download", false));
		maven.addItem(getDocItem("maven/get", false));
		maven.addItem(getDocItem("maven/uri", false));
		maven.addItem(getDocItem("maven/parse-artefact", false));
		
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
		java.addItem(getDocItem("java/javadoc", false));

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
		semver.addItem(getDocItem("semver/parse"));
		semver.addItem(getDocItem("semver/version"));

		final DocSection valid = new DocSection("Validation", id());
		all.addSection(valid);
		valid.addItem(getDocItem("semver/valid?"));
		valid.addItem(getDocItem("semver/valid-format?"));

		final DocSection test = new DocSection("Test", id());
		all.addSection(test);
		test.addItem(getDocItem("semver/newer?"));
		test.addItem(getDocItem("semver/older?"));
		test.addItem(getDocItem("semver/equal?"));
		test.addItem(getDocItem("semver/cmp"));

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
		geoip.addItem(getDocItem("geoip/ip-to-country-resolver", false));
		geoip.addItem(getDocItem("geoip/ip-to-country-loc-resolver", false));
		geoip.addItem(getDocItem("geoip/ip-to-city-loc-resolver", false));
		geoip.addItem(getDocItem("geoip/ip-to-city-loc-resolver-mem-optimized", false));
		
		final DocSection db = new DocSection("Databases", id());
		all.addSection(db);
		db.addItem(getDocItem("geoip/download-google-country-db-to-csvfile", false));
		db.addItem(getDocItem("geoip/download-maxmind-db-to-zipfile", false));
		db.addItem(getDocItem("geoip/download-maxmind-db", false));

		final DocSection dbBuild = new DocSection("DB Parser", id());
		all.addSection(dbBuild);
		dbBuild.addItem(getDocItem("geoip/parse-maxmind-country-ip-db", false));
		dbBuild.addItem(getDocItem("geoip/parse-maxmind-city-ip-db", false));
		dbBuild.addItem(getDocItem("geoip/parse-maxmind-country-db", false));
		dbBuild.addItem(getDocItem("geoip/parse-maxmind-city-db", false));

		final DocSection util = new DocSection("Util", id());
		all.addSection(util);
		util.addItem(getDocItem("geoip/build-maxmind-country-db-url"));
		util.addItem(getDocItem("geoip/build-maxmind-city-db-url"));
		util.addItem(getDocItem("geoip/map-location-to-numerics"));
		util.addItem(getDocItem("geoip/country-to-location-resolver", false));

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
		hexdump.addItem(getDocItem("hexdump/dump", false));

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
		colors.addItem(getDocItem("ansi/fg-color", false));
		colors.addItem(getDocItem("ansi/bg-color", false));

		final DocSection style = new DocSection("Styles", id());
		all.addSection(style);
		style.addItem(getDocItem("ansi/style", false));
		style.addItem(getDocItem("ansi/ansi", false));
		style.addItem(getDocItem("ansi/with-ansi", false));
		style.addItem(getDocItem("ansi/without-ansi", false));

		final DocSection cursor = new DocSection("Cursor", id());
		all.addSection(cursor);
		cursor.addItem(getDocItem("ansi/without-cursor", false));

		final DocSection progress = new DocSection("Progress", id());
		all.addSection(progress);
		progress.addItem(getDocItem("ansi/progress", false));
		progress.addItem(getDocItem("ansi/progress-bar", false));

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
		colors.addItem(getDocItem("bench/benchmark", false));

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
		build.addItem(getDocItem("config/build", false));

		final DocSection file = new DocSection("File", id());
		all.addSection(file);
		file.addItem(getDocItem("config/file", false));
		file.addItem(getDocItem("config/resource", false));
		
		final DocSection env = new DocSection("Env", id());
		all.addSection(env);
		env.addItem(getDocItem("config/env-var", true));
		env.addItem(getDocItem("config/env", false));

		final DocSection prop = new DocSection("Properties", id());
		all.addSection(prop);
		prop.addItem(getDocItem("config/property-var", true));
		prop.addItem(getDocItem("config/properties", false));

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
		system.addItem(getDocItem("component/system-map", false));
		system.addItem(getDocItem("component/system-using", false));

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
		build.addItem(getDocItem("app/build", false));

		final DocSection manifest = new DocSection("Manifest", id());
		all.addSection(manifest);
		manifest.addItem(getDocItem("app/manifest", false));

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
		wr.addItem(getDocItem("excel/writer", false));
		wr.addItem(getDocItem("excel/add-sheet", false));
		wr.addItem(getDocItem("excel/add-font", false));
		wr.addItem(getDocItem("excel/add-style", false));
		wr.addItem(getDocItem("excel/add-column", false));

		final DocSection wr_data = new DocSection("Writer Data", id());
		all.addSection(wr_data);
		wr_data.addItem(getDocItem("excel/write-data", false));
		wr_data.addItem(getDocItem("excel/write-items", false));
		wr_data.addItem(getDocItem("excel/write-item", false));
		wr_data.addItem(getDocItem("excel/write-value", false));

		final DocSection wr_io = new DocSection("Writer I/O", id());
		all.addSection(wr_io);
		wr_io.addItem(getDocItem("excel/write->file", false));
		wr_io.addItem(getDocItem("excel/write->stream", false));
		wr_io.addItem(getDocItem("excel/write->bytebuf", false));

		final DocSection wr_util = new DocSection("Writer Util", id());
		all.addSection(wr_util);
		wr_util.addItem(getDocItem("excel/cell-formula", false));
		wr_util.addItem(getDocItem("excel/sum-formula", false));
		wr_util.addItem(getDocItem("excel/cell-address", false));
		wr_util.addItem(getDocItem("excel/auto-size-columns", false));
		wr_util.addItem(getDocItem("excel/auto-size-column", false));
		wr_util.addItem(getDocItem("excel/row-height", false));
		wr_util.addItem(getDocItem("excel/evaluate-formulas", false));
		wr_util.addItem(getDocItem("excel/convert->reader", false));

		final DocSection rd = new DocSection("Reader", id());
		all.addSection(rd);
		rd.addItem(getDocItem("excel/open", false));
		rd.addItem(getDocItem("excel/sheet", false));
		rd.addItem(getDocItem("excel/read-string-val", false));
		rd.addItem(getDocItem("excel/read-boolean-val", false));
		rd.addItem(getDocItem("excel/read-long-val", false));
		rd.addItem(getDocItem("excel/read-double-val", false));
		rd.addItem(getDocItem("excel/read-date-val", false));

		final DocSection rd_util = new DocSection("Reader Util", id());
		all.addSection(rd_util);
		rd_util.addItem(getDocItem("excel/sheet-count", false));
		rd_util.addItem(getDocItem("excel/sheet-name", false));
		rd_util.addItem(getDocItem("excel/sheet-row-range", false));
		rd_util.addItem(getDocItem("excel/sheet-col-range", false));
		rd_util.addItem(getDocItem("excel/evaluate-formulas", false));
		rd_util.addItem(getDocItem("excel/cell-empty?", false));
		rd_util.addItem(getDocItem("excel/cell-type", false));

		return section;
	}

	private DocItem getDocItem(final String name) {
		return getDocItem(name, true, false);
	}

	private DocItem getDocItem(final String name, final boolean runExamples) {
		return getDocItem(name, runExamples, false);
	}

	private DocItem getDocItem(final String name, final boolean runExamples, final boolean catchEx) {
		final DocItem item = docItems.get(name);
		if (item != null) {
			return item;
		}
		else {
			final DocItem item_ = getDocItem_(name, runExamples, catchEx);
			if (item_ != null) {
				docItems.put(name, item_);
			}
			return item_;
		}
	}

	private DocItem getDocItem_(final String name, final boolean runExamples, final boolean catchEx) {
		final VncFunction fn = findFunction(name);

		if (fn != null) {
			final String fnDescr = fn.getDoc() == Constants.Nil 
										? "" 
										: ((VncString)fn.getDoc()).getValue();
			
			final String descr = MARKDOWN_FN_DESCR ? null : fnDescr;
			
			final String descrXmlStyled = MARKDOWN_FN_DESCR
											? Markdown.parse(fnDescr).renderToHtml()
											: null;
			
			return new DocItem(
					name, 
					toStringList(fn.getArgLists(), name, ":arglists"), 
					descr,
					descrXmlStyled,
					runExamples(
							name, 
							toStringList(fn.getExamples(), name, ":examples"), 
							runExamples, 
							catchEx),
					createCrossRefs(name, fn),
					id(name));
		}
		else {
			throw new RuntimeException(String.format("Unknown doc function %s", name));
		}
	}

	private List<ExampleOutput> runExamples(
			final String name, 
			final List<String> examples, 
			final boolean run,
			final boolean catchEx
	) {
		if (runExamples) {
			final Venice runner = new Venice();
	
			final AtomicLong exampleNr = new AtomicLong(0);
			try {
				return examples
							.stream()
							.filter(e -> !StringUtil.isEmpty(e))
							.map(e -> runExample(
										runner, 
										exampleNr.incrementAndGet(),
										name, 
										e, 
										run, 
										catchEx))
							.collect(Collectors.toList());
			}
			catch(RuntimeException ex) {
				throw new RuntimeException(
						String.format(
								"Failed to run examples #%d (of %d) for %s", 
								exampleNr.get(), examples.size(), name), 
						ex);
			}
		}
		else {
			return EMPTY_EXAMPLES;
		}
	}
	
	private ExampleOutput runExample(
			final Venice runner,
			final long exampleNr,
			final String name, 
			final String example, 
			final boolean run,
			final boolean catchEx
	) {
		final String exampleHighlighted = codeHighlighter.highlight(example);
				
		if (run) {
			final CapturingPrintStream ps_out = new CapturingPrintStream();
			final CapturingPrintStream ps_err = new CapturingPrintStream();

			try {
				final String modules = preloadedModules
											.stream()
											.map(m -> "  (load-module :" + m + ")")
											.collect(Collectors.joining("\n"));
				
				final String script = "(do \n" + modules + "\n\n  (pr-str " + example + "\n))";
				
				final String result = (String)runner.eval(
											"example",
											script,
											Parameters.of(
												"*out*", ps_out,
												"*err*", ps_err));
									
				return new ExampleOutput(
						name, example, exampleHighlighted, 
						ps_out.getOutput(), ps_err.getOutput(), result);
			}
			catch(RuntimeException ex) {
				if (catchEx) {							
					return new ExampleOutput(
							name, example, exampleHighlighted, 
							ps_out.getOutput(), ps_err.getOutput(), ex);
				}
				else {
					throw new RuntimeException(
							String.format("Failed to run example #%d for '%s'", exampleNr, name), 
							ex);
				}
			}
		}
		else {
			return new ExampleOutput(name, example, exampleHighlighted);
		}
	}
	
	private VncFunction findFunction(final String name) {
		// Special forms
		final VncFunction fn = (VncFunction)SpecialFormsDoc.ns.get(new VncSymbol(name));
		if (fn != null) {
			return fn;
		}

		// functions & macros
		return getFunction(name);
	}
	
	private List<CrossRef> createCrossRefs(final String parentName, final VncFunction fn) {
		final List<CrossRef> crossRefs = new ArrayList<>();
		
		final VncList seeAlso = fn.getSeeAlso();
		seeAlso.forEach(v -> {
			final String crossRefFnName = ((VncString)v).getValue();
			
			final VncFunction crossRefFn = findFunction(crossRefFnName);
			if (crossRefFn != null) {
				String doc = crossRefFn.getDoc() == Constants.Nil 
								? null 
								: ((VncString)crossRefFn.getDoc()).getValue();
				
				if (doc != null) {
					crossRefs.add(
						createCrossRef(crossRefFnName, getCrossRefDescr(doc)));
				}
			}
			else {
				throw new RuntimeException(String.format(
							"Missing cross reference function %s -> %s",
							parentName,
							crossRefFnName));
			}
		});

		return crossRefs;
	}

	private String getCrossRefDescr(final String descr) {
		String crossRefDescr = Markdown.parse(descr).renderToText(CROSSREF_MAX_LEN * 2);
		
		int posLF = crossRefDescr.indexOf('\n');
		
		// the crossref description text is built from the first line only
		String s = (posLF == -1) ? crossRefDescr.trim() : crossRefDescr.substring(0, posLF).trim();

		// limit to at most CROSSREF_MAX_LEN chars
		if (s.length() > CROSSREF_MAX_LEN) {
			// do not cut in the middle of a word, cut at the first space in the last 15 
			// characters of the description, if no space is found remove the last 5 chars
			// to get space for "..." marker
			final int spacePos = s.indexOf(' ', CROSSREF_MAX_LEN - 15); 
			s = (spacePos != -1)
				  ? s.substring(0, spacePos)
				  : s.substring(0, CROSSREF_MAX_LEN - 5).trim();
				  
			if (!s.endsWith(".")) {
				s = s + " ...";
			}
		}
		
		return s;
	}

	private CrossRef createCrossRef(final String name, final String descr) {
		return new CrossRef(name, id(name), descr);
	}

	private List<String> toStringList(final VncList list, final String name, final String helpType) {
		try {
			return list.stream()
					   .map(s -> ((VncString)s).getValue())
					   .collect(Collectors.toList());
		}
		catch(Exception ex) {
			throw new RuntimeException(String.format("Failed on item '%s' processing %s", name, helpType), ex);
		}
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

	private VncFunction getFunction(final String name) {
		final VncVal val = env.getOrNil(new VncSymbol(name));
		return Types.isVncFunction(val) ? (VncFunction)val : null;
	}

	private String id() {
		return String.valueOf(gen.getAndIncrement());
	}

	private String id(final String name) {
		return idMap.computeIfAbsent(name, n -> String.valueOf(gen.getAndIncrement()));
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
	
	
	private static final boolean MARKDOWN_FN_DESCR = true;

	private static final List<ExampleOutput> EMPTY_EXAMPLES = 
			Collections.unmodifiableList(new ArrayList<>());
	
	private static final int CROSSREF_MAX_LEN = 145;
	
	private final Map<String,String> idMap = new HashMap<>();
	
	private final AtomicLong gen = new AtomicLong(1000);
	
	private final List<String> preloadedModules = new ArrayList<>();

	private final Map<String, DocItem> docItems = new HashMap<>();
	private final Env env;
	private final DocHighlighter codeHighlighter;
	
	private boolean runExamples = false;
}
