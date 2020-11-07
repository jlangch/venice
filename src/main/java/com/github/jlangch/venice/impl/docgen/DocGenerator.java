/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2020 Venice
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
package com.github.jlangch.venice.impl.docgen;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.RunMode;
import com.github.jlangch.venice.impl.VeniceInterpreter;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.specialforms.SpecialFormsDoc;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;
import com.github.jlangch.venice.util.CapturingPrintStream;
import com.lowagie.text.pdf.PdfReader;


public class DocGenerator {

	public DocGenerator() {
		this.preloadedModules
			.addAll(Arrays.asList(
						"app",    "xml",   "crypt",  "gradle", 
						"trace",  "ansi",  "maven",  "kira"));
		
		this.env = new VeniceInterpreter(new AcceptAllInterceptor())
							.createEnv(
								preloadedModules, 
								false, 
								false, 
								RunMode.DOCGEN)
							.setStdoutPrintStream(null);
	}

	public static void main(final String[] args) {
		final String version = args.length > 0 ? args[0] : "0.0.0";
		new DocGenerator().run(version);

//		System.out.println(
//			CheatsheetRenderer.parseTemplate().replace("\\n", "\n"));
	}
	
	private void run(final String version) {
		try {	
			System.out.println("Creating cheatsheet V" + version);
			
			final List<DocSection> left = getLeftSections();
			final List<DocSection> right = getRightSections();
			
			final Map<String,Object> data = new HashMap<>();
			data.put("meta-author", "Venice");
			data.put("version", version);
			data.put("sections", concat(left, right));
			data.put("left", left);
			data.put("right", right);
			data.put("details", getDocItems(concat(left, right)));
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
		
	private List<DocSection> getLeftSections() {
		return Arrays.asList(
				getPrimitivesSection(),
				getByteBufSection(),
				getTimeSection(),
				getRegexSection(),
				getTransducersSection(),
				getFunctionsSection(),
				getMacrosSection(),
				getSpecialFormsSection(),
				getTypesSection(),
				getNamespaceSection(),
				getAppSection(),
				getModulesSection());
	}
	
	private List<DocSection> getRightSections() {
		return Arrays.asList(
				getCollectionsSection(),
				getLazySequencesSection(),
				getArraysSection(),
				getConcurrencySection(),
				getSystemSection(),
				getIOSection(),
				getJavaInteropSection(),
				getMiscellaneousSection());
	}

	private List<DocItem> getDocItems(List<DocSection> sections) {
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
		final DocSection section = new DocSection("Primitives", id());
		
		final DocSection lit = new DocSection("Literals");
		section.addSection(lit);
		
		final DocSection literals = new DocSection("Literals");
		lit.addSection(literals);

		literals.addItem(new DocItem("Nil: nil", null));
		literals.addItem(new DocItem("Boolean: true, false", null));
		literals.addItem(new DocItem("Integer: 150I, 1_000_000I, 0x1FFI", null));
		literals.addItem(new DocItem("Long: 1500, 1_000_000, 0x00A055FF", null));
		literals.addItem(new DocItem("Double: 3.569, 2.0E+10", null));
		literals.addItem(new DocItem("BigDecimal: 6.897M, 2.345E+10M", null));
		literals.addItem(new DocItem("BigInteger: 1000N, 1_000_000N", null));
		literals.addItem(new DocItem("String: \"abcd\", \"ab\\\"cd\", \"PI: \\u03C0\"", null) );
		literals.addItem(new DocItem("String: \"\"\"{ \"age\": 42 }\"\"\"", null) );

		final DocSection numbers = new DocSection("Numbers");
		section.addSection(numbers);

		final DocSection arithmetic = new DocSection("Arithmetic");
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

		final DocSection convert = new DocSection("Convert");
		numbers.addSection(convert);
		convert.addItem(getDocItem("int"));
		convert.addItem(getDocItem("long"));
		convert.addItem(getDocItem("double"));
		convert.addItem(getDocItem("decimal"));
		convert.addItem(getDocItem("bigint"));

		final DocSection compare = new DocSection("Compare");
		numbers.addSection(compare);
		compare.addItem(getDocItem("=="));
		compare.addItem(getDocItem("="));
		compare.addItem(getDocItem("<"));
		compare.addItem(getDocItem(">"));
		compare.addItem(getDocItem("<="));
		compare.addItem(getDocItem(">="));
		compare.addItem(getDocItem("compare"));

		final DocSection test = new DocSection("Test");
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

		final DocSection random = new DocSection("Random");
		numbers.addSection(random);
		random.addItem(getDocItem("rand-long"));
		random.addItem(getDocItem("rand-double"));
		random.addItem(getDocItem("rand-gaussian"));
		
		final DocSection trigonometry = new DocSection("Trigonometry");
		numbers.addSection(trigonometry);
		trigonometry.addItem(getDocItem("to-radians"));
		trigonometry.addItem(getDocItem("to-degrees"));
		trigonometry.addItem(getDocItem("sin"));
		trigonometry.addItem(getDocItem("cos"));
		trigonometry.addItem(getDocItem("tan"));
		
		final DocSection statistics = new DocSection("Statistics");
		numbers.addSection(statistics);
		statistics.addItem(getDocItem("mean"));
		statistics.addItem(getDocItem("median"));
		statistics.addItem(getDocItem("quartiles"));
		statistics.addItem(getDocItem("quantile"));
		statistics.addItem(getDocItem("standard-deviation"));

		final DocSection bigdecimal = new DocSection("BigDecimal");
		numbers.addSection(bigdecimal);
		bigdecimal.addItem(getDocItem("dec/add"));
		bigdecimal.addItem(getDocItem("dec/sub"));
		bigdecimal.addItem(getDocItem("dec/mul"));
		bigdecimal.addItem(getDocItem("dec/div"));
		bigdecimal.addItem(getDocItem("dec/scale"));

		
		final DocSection strings = new DocSection("Strings");
		section.addSection(strings);

		final DocSection create = new DocSection("Create");
		strings.addSection(create);
		create.addItem(getDocItem("str"));
		create.addItem(getDocItem("str/format"));
		create.addItem(getDocItem("str/quote"));
		create.addItem(getDocItem("str/double-quote"));
		create.addItem(getDocItem("str/double-unquote"));

		final DocSection use = new DocSection("Use");
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
		
		final DocSection split = new DocSection("Split/Join");
		strings.addSection(split);
		split.addItem(getDocItem("str/split"));
		split.addItem(getDocItem("str/split-lines"));
		split.addItem(getDocItem("str/join"));
		
		final DocSection replace = new DocSection("Replace");
		strings.addSection(replace);
		replace.addItem(getDocItem("str/replace-first"));
		replace.addItem(getDocItem("str/replace-last"));
		replace.addItem(getDocItem("str/replace-all"));
		
		final DocSection strip = new DocSection("Strip");
		strings.addSection(strip);
		strip.addItem(getDocItem("str/strip-start"));
		strip.addItem(getDocItem("str/strip-end"));
		strip.addItem(getDocItem("str/strip-indent"));
		strip.addItem(getDocItem("str/strip-margin"));
		
		final DocSection conv = new DocSection("Conversion");
		strings.addSection(conv);
		conv.addItem(getDocItem("str/lower-case"));
		conv.addItem(getDocItem("str/upper-case"));
		conv.addItem(getDocItem("str/cr-lf", false));
		
		final DocSection regex = new DocSection("Regex");
		strings.addSection(regex);
		regex.addItem(getDocItem("match?"));
		regex.addItem(getDocItem("not-match?"));

		final DocSection trim = new DocSection("Trim");
		strings.addSection(trim);
		trim.addItem(getDocItem("str/trim"));
		trim.addItem(getDocItem("str/trim-to-nil"));

		final DocSection hex = new DocSection("Hex");
		strings.addSection(hex);
		hex.addItem(getDocItem("str/hex-to-bytebuf"));
		hex.addItem(getDocItem("str/bytebuf-to-hex"));
		hex.addItem(getDocItem("str/format-bytebuf"));

		final DocSection encode = new DocSection("Encode/Decode");
		strings.addSection(encode);
		encode.addItem(getDocItem("str/encode-base64"));
		encode.addItem(getDocItem("str/decode-base64"));
		encode.addItem(getDocItem("str/encode-url"));
		encode.addItem(getDocItem("str/decode-url"));
		encode.addItem(getDocItem("str/escape-html"));
		encode.addItem(getDocItem("str/escape-xml"));


		final DocSection validation = new DocSection("Validation");
		strings.addSection(validation);
		validation.addItem(getDocItem("str/valid-email-addr?"));

		final DocSection str_test = new DocSection("Test");
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

		final DocSection str_test_char = new DocSection("Test char");
		strings.addSection(str_test_char);
		str_test_char.addItem(getDocItem("str/char?"));
		str_test_char.addItem(getDocItem("str/digit?"));
		str_test_char.addItem(getDocItem("str/letter?"));
		str_test_char.addItem(getDocItem("str/whitespace?"));
		str_test_char.addItem(getDocItem("str/linefeed?"));
		str_test_char.addItem(getDocItem("str/lower-case?"));
		str_test_char.addItem(getDocItem("str/upper-case?"));

		
		final DocSection chars = new DocSection("Chars");
		section.addSection(chars);

		final DocSection charuse = new DocSection("Use");
		chars.addSection(charuse);		
		charuse.addItem(getDocItem("char"));
		charuse.addItem(getDocItem("char?"));

		
		final DocSection other = new DocSection("Other");
		section.addSection(other);

		final DocSection keywords = new DocSection("Keywords");
		other.addSection(keywords);
		keywords.addItem(new DocItem(":a :blue", null));
		keywords.addItem(getDocItem("keyword?"));
		keywords.addItem(getDocItem("keyword"));

		final DocSection symbols = new DocSection("Symbols");
		other.addSection(symbols);
		symbols.addItem(new DocItem("'a 'blue", null));
		symbols.addItem(getDocItem("symbol?"));
		symbols.addItem(getDocItem("symbol"));

		final DocSection just = new DocSection("Just");
		other.addSection(just);
		just.addItem(getDocItem("just"));
		just.addItem(getDocItem("just?"));

		final DocSection boolean_ = new DocSection("Boolean");
		other.addSection(boolean_);
		boolean_.addItem(getDocItem("boolean"));
		boolean_.addItem(getDocItem("not"));
		boolean_.addItem(getDocItem("boolean?"));
		boolean_.addItem(getDocItem("true?"));
		boolean_.addItem(getDocItem("false?"));

		return section;
	}

	private DocSection getCollectionsSection() {
		final DocSection section = new DocSection("Collections", id());


		final DocSection collections = new DocSection("Collections");
		section.addSection(collections);
		
		final DocSection generic = new DocSection("Generic");
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
		generic.addItem(getDocItem("replace"));
		generic.addItem(getDocItem("range"));
		generic.addItem(getDocItem("group-by"));
		generic.addItem(getDocItem("frequencies"));
		generic.addItem(getDocItem("get-in"));
		generic.addItem(getDocItem("seq"));
		generic.addItem(getDocItem("reverse"));
		generic.addItem(getDocItem("shuffle"));

		final DocSection coll_test = new DocSection("Tests");
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

		final DocSection coll_process = new DocSection("Process");
		collections.addSection(coll_process);
		coll_process.addItem(getDocItem("map"));
		coll_process.addItem(getDocItem("map-indexed"));
		coll_process.addItem(getDocItem("filter"));
		coll_process.addItem(getDocItem("reduce"));
		coll_process.addItem(getDocItem("keep"));
		coll_process.addItem(getDocItem("docoll"));

		
		final DocSection lists = new DocSection("Lists");
		section.addSection(lists);

		final DocSection list_create = new DocSection("Create");
		lists.addSection(list_create);
		list_create.addItem(getDocItem("()"));
		list_create.addItem(getDocItem("list"));
		list_create.addItem(getDocItem("list*"));
		list_create.addItem(getDocItem("mutable-list"));

		final DocSection list_access = new DocSection("Access");
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
		list_access.addItem(getDocItem("some"));

		final DocSection list_modify = new DocSection("Modify");
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
		list_modify.addItem(getDocItem("mapcat"));
		list_modify.addItem(getDocItem("flatten"));
		list_modify.addItem(getDocItem("sort"));
		list_modify.addItem(getDocItem("sort-by"));
		list_modify.addItem(getDocItem("take"));
		list_modify.addItem(getDocItem("take-while"));
		list_modify.addItem(getDocItem("drop"));
		list_modify.addItem(getDocItem("drop-while"));
		list_modify.addItem(getDocItem("split-at"));
		list_modify.addItem(getDocItem("split-with"));
	
		final DocSection list_test = new DocSection("Test");
		lists.addSection(list_test);
		list_test.addItem(getDocItem("list?"));
		list_test.addItem(getDocItem("mutable-list?"));
		list_test.addItem(getDocItem("every?"));
		list_test.addItem(getDocItem("not-every?"));
		list_test.addItem(getDocItem("any?"));
		list_test.addItem(getDocItem("not-any?"));
		
		
		final DocSection vectors = new DocSection("Vectors");
		section.addSection(vectors);

		final DocSection vec_create = new DocSection("Create");
		vectors.addSection(vec_create);
		vec_create.addItem(getDocItem("[]"));
		vec_create.addItem(getDocItem("vector"));
		vec_create.addItem(getDocItem("mapv"));

		final DocSection vec_access = new DocSection("Access");
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

		final DocSection vec_modify = new DocSection("Modify");
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
		vec_modify.addItem(getDocItem("mapcat"));
		vec_modify.addItem(getDocItem("flatten"));
		vec_modify.addItem(getDocItem("sort"));
		vec_modify.addItem(getDocItem("sort-by"));
		vec_modify.addItem(getDocItem("take"));
		vec_modify.addItem(getDocItem("take-while"));
		vec_modify.addItem(getDocItem("drop"));
		vec_modify.addItem(getDocItem("drop-while"));
		vec_modify.addItem(getDocItem("update"));
		vec_modify.addItem(getDocItem("update!"));
		vec_modify.addItem(getDocItem("split-with"));
		
		final DocSection vec_nested = new DocSection("Nested");
		vectors.addSection(vec_nested);
		vec_nested.addItem(getDocItem("get-in"));
		vec_nested.addItem(getDocItem("assoc-in"));
		vec_nested.addItem(getDocItem("update-in"));
		vec_nested.addItem(getDocItem("dissoc-in"));
			
		final DocSection vec_test = new DocSection("Test");
		vectors.addSection(vec_test);
		vec_test.addItem(getDocItem("vector?"));
		vec_test.addItem(getDocItem("contains?"));
		vec_test.addItem(getDocItem("not-contains?"));
		vec_test.addItem(getDocItem("every?"));
		vec_test.addItem(getDocItem("not-every?"));
		vec_test.addItem(getDocItem("any?"));
		vec_test.addItem(getDocItem("not-any?"));
	
		
		final DocSection sets = new DocSection("Sets");
		section.addSection(sets);

		final DocSection set_create = new DocSection("Create");
		sets.addSection(set_create);
		set_create.addItem(getDocItem("#{}"));
		set_create.addItem(getDocItem("set"));
		set_create.addItem(getDocItem("sorted-set"));
		set_create.addItem(getDocItem("mutable-set"));

		final DocSection set_modify = new DocSection("Modify");
		sets.addSection(set_modify);
		set_modify.addItem(getDocItem("cons"));
		set_modify.addItem(getDocItem("cons!"));
		set_modify.addItem(getDocItem("conj"));
		set_modify.addItem(getDocItem("conj!"));
		set_modify.addItem(getDocItem("disj"));
		set_modify.addItem(getDocItem("difference"));
		set_modify.addItem(getDocItem("union"));
		set_modify.addItem(getDocItem("intersection"));

		final DocSection set_test = new DocSection("Test");
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

		
		final DocSection maps = new DocSection("Maps");
		section.addSection(maps);

		final DocSection maps_create = new DocSection("Create");
		maps.addSection(maps_create);
		maps_create.addItem(getDocItem("{}"));
		maps_create.addItem(getDocItem("hash-map"));
		maps_create.addItem(getDocItem("ordered-map"));
		maps_create.addItem(getDocItem("sorted-map"));
		maps_create.addItem(getDocItem("mutable-map"));
		maps_create.addItem(getDocItem("zipmap"));
		

		final DocSection map_access = new DocSection("Access");
		maps.addSection(map_access);
		map_access.addItem(getDocItem("find"));
		map_access.addItem(getDocItem("get"));
		map_access.addItem(getDocItem("keys"));
		map_access.addItem(getDocItem("vals"));
		map_access.addItem(getDocItem("key"));
		map_access.addItem(getDocItem("val"));
		map_access.addItem(getDocItem("entries"));

		final DocSection map_modify = new DocSection("Modify");
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
		map_modify.addItem(getDocItem("map-invert"));
		map_modify.addItem(getDocItem("map-keys"));
		map_modify.addItem(getDocItem("map-vals"));
		
		final DocSection map_nested = new DocSection("Nested");
		maps.addSection(map_nested);
		map_nested.addItem(getDocItem("get-in"));
		map_nested.addItem(getDocItem("assoc-in"));
		map_nested.addItem(getDocItem("update-in"));
		map_nested.addItem(getDocItem("dissoc-in"));
		
		final DocSection map_test = new DocSection("Test");
		maps.addSection(map_test);
		map_test.addItem(getDocItem("map?"));
		map_test.addItem(getDocItem("sequential?"));
		map_test.addItem(getDocItem("hash-map?"));
		map_test.addItem(getDocItem("ordered-map?"));
		map_test.addItem(getDocItem("sorted-map?"));
		map_test.addItem(getDocItem("mutable-map?"));
		map_test.addItem(getDocItem("map-entry?"));
		map_test.addItem(getDocItem("contains?"));
		map_test.addItem(getDocItem("not-contains?"));

		
		final DocSection stacks = new DocSection("Stack");
		section.addSection(stacks);

		final DocSection stacks_create = new DocSection("Create");
		stacks.addSection(stacks_create);
		stacks_create.addItem(getDocItem("stack"));
		

		final DocSection stacks_access = new DocSection("Access");
		stacks.addSection(stacks_access);
		stacks_access.addItem(getDocItem("peek"));
		stacks_access.addItem(getDocItem("pop!"));
		stacks_access.addItem(getDocItem("push!"));
		stacks_access.addItem(getDocItem("count"));

		
		final DocSection stacks_test = new DocSection("Test");
		stacks.addSection(stacks_test);
		stacks_test.addItem(getDocItem("empty?"));
		stacks_test.addItem(getDocItem("stack?"));

		
		final DocSection queues = new DocSection("Queue");
		section.addSection(queues);

		final DocSection queues_create = new DocSection("Create");
		queues.addSection(queues_create);
		queues_create.addItem(getDocItem("queue"));
		

		final DocSection queues_access = new DocSection("Access");
		queues.addSection(queues_access);
		queues_access.addItem(getDocItem("peek"));
		queues_access.addItem(getDocItem("poll!"));
		queues_access.addItem(getDocItem("offer!"));
		queues_access.addItem(getDocItem("count"));

		
		final DocSection queues_test = new DocSection("Test");
		queues.addSection(queues_test);
		queues_test.addItem(getDocItem("empty?"));
		queues_test.addItem(getDocItem("queue?"));

		return section;
	}		

	private DocSection getLazySequencesSection() {
		final DocSection section = new DocSection("Lazy Sequences", id());

		final DocSection all = new DocSection("");
		section.addSection(all);

		final DocSection create = new DocSection("Create");
		all.addSection(create);
		create.addItem(getDocItem("lazy-seq"));

		final DocSection realize = new DocSection("Realize");
		all.addSection(realize);
		realize.addItem(getDocItem("doall"));

		final DocSection test = new DocSection("Test");
		all.addSection(test);
		test.addItem(getDocItem("lazy-seq?"));

		return section;
	}

	private DocSection getArraysSection() {
		final DocSection section = new DocSection("Arrays", id());

		final DocSection all = new DocSection("");
		section.addSection(all);

		final DocSection create = new DocSection("Create");
		all.addSection(create);
		create.addItem(getDocItem("make-array"));
		create.addItem(getDocItem("object-array"));
		create.addItem(getDocItem("string-array"));
		create.addItem(getDocItem("int-array"));
		create.addItem(getDocItem("long-array"));
		create.addItem(getDocItem("float-array"));
		create.addItem(getDocItem("double-array"));

		final DocSection use = new DocSection("Use");
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
		final DocSection section = new DocSection("Regex", id());

		final DocSection all = new DocSection("");
		section.addSection(all);

		final DocSection general = new DocSection("General");
		all.addSection(general);
		general.addItem(getDocItem("regex/pattern"));
		general.addItem(getDocItem("regex/matcher"));
		general.addItem(getDocItem("regex/find"));
		general.addItem(getDocItem("regex/find-all"));
		general.addItem(getDocItem("regex/find-group"));
		general.addItem(getDocItem("regex/find-all-groups"));
		general.addItem(getDocItem("regex/reset"));
		general.addItem(getDocItem("regex/find?"));
		general.addItem(getDocItem("regex/matches"));
		general.addItem(getDocItem("regex/matches?"));
		general.addItem(getDocItem("regex/group"));
		general.addItem(getDocItem("regex/groupcount"));

		return section;
	}
	
	private DocSection getFunctionsSection() {
		final DocSection section = new DocSection("Functions", id());

		final DocSection all = new DocSection("");
		section.addSection(all);

		final DocSection create = new DocSection("Create");
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

		final DocSection call = new DocSection("Call");
		all.addSection(call);
		call.addItem(getDocItem("apply"));
		call.addItem(getDocItem("->"));
		call.addItem(getDocItem("->>"));

		final DocSection test = new DocSection("Test");
		all.addSection(test);
		test.addItem(getDocItem("fn?"));

		final DocSection ex = new DocSection("Exception");
		all.addSection(ex);
		ex.addItem(getDocItem("throw"));

		final DocSection misc = new DocSection("Misc");
		all.addSection(misc);
		misc.addItem(getDocItem("nil?"));
		misc.addItem(getDocItem("some?"));
		misc.addItem(getDocItem("eval"));
		misc.addItem(getDocItem("name"));
		misc.addItem(getDocItem("callstack"));
		misc.addItem(getDocItem("coalesce"));
		misc.addItem(getDocItem("load-resource"));

		final DocSection env = new DocSection("Environment");
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
		
		final DocSection walk = new DocSection("Tree Walker");
		all.addSection(walk);
		walk.addItem(getDocItem("prewalk"));
		walk.addItem(getDocItem("postwalk"));

		final DocSection meta = new DocSection("Meta");
		all.addSection(meta);
		meta.addItem(getDocItem("meta"));
		meta.addItem(getDocItem("with-meta"));
		meta.addItem(getDocItem("vary-meta"));

		final DocSection doc = new DocSection("Documentation");
		all.addSection(doc);
		doc.addItem(getDocItem("doc", false));
		doc.addItem(getDocItem("modules"));

		final DocSection syntax = new DocSection("Syntax");
		all.addSection(syntax);
		syntax.addItem(getDocItem("highlight"));
		
		return section;
	}


	private DocSection getSystemSection() {
		final DocSection section = new DocSection("System", id());

		final DocSection all = new DocSection("");
		section.addSection(all);

		final DocSection venice = new DocSection("Venice");
		all.addSection(venice);
		venice.addItem(getDocItem("version"));
		venice.addItem(getDocItem("sandboxed?"));

		final DocSection system = new DocSection("System");
		all.addSection(system);
		system.addItem(getDocItem("system-prop"));
		system.addItem(getDocItem("system-env"));
		system.addItem(getDocItem("system-exit-code"));
		system.addItem(getDocItem("charset-default-encoding"));
		
		final DocSection java = new DocSection("Java");
		all.addSection(java);
		java.addItem(getDocItem("java-version"));
		java.addItem(getDocItem("java-version-info"));
		java.addItem(getDocItem("java-major-version"));

		final DocSection os = new DocSection("OS");
		all.addSection(os);
		os.addItem(getDocItem("os-type"));
		os.addItem(getDocItem("os-type?"));
		os.addItem(getDocItem("os-arch"));
		os.addItem(getDocItem("os-name"));
		os.addItem(getDocItem("os-version"));

		final DocSection time = new DocSection("Time");
		all.addSection(time);
		time.addItem(getDocItem("current-time-millis"));
		time.addItem(getDocItem("nano-time"));
		time.addItem(getDocItem("format-nano-time"));

		final DocSection util = new DocSection("Other");
		all.addSection(util);
		util.addItem(getDocItem("uuid"));
		util.addItem(getDocItem("sleep"));
		util.addItem(getDocItem("host-name"));
		util.addItem(getDocItem("host-address"));
		util.addItem(getDocItem("gc"));
		util.addItem(getDocItem("cpus"));
		util.addItem(getDocItem("pid"));
		util.addItem(getDocItem("shutdown-hook"));

		final DocSection shell = new DocSection("Shell");
		all.addSection(shell);
		shell.addItem(getDocItem("sh", false));
		shell.addItem(getDocItem("with-sh-dir", false));
		shell.addItem(getDocItem("with-sh-env", false));
		shell.addItem(getDocItem("with-sh-throw", false));
				
		return section;
	}

	private DocSection getMacrosSection() {
		final DocSection section = new DocSection("Macros", id());

		final DocSection all = new DocSection("");
		section.addSection(all);

		final DocSection create = new DocSection("Create");
		all.addSection(create);		
		create.addItem(getDocItem("defn"));
		create.addItem(getDocItem("defn-"));
		create.addItem(getDocItem("defmacro"));
		create.addItem(getDocItem("macroexpand"));
		create.addItem(getDocItem("macroexpand-all"));

		final DocSection branch = new DocSection("Branch");
		all.addSection(branch);
		branch.addItem(getDocItem("and"));
		branch.addItem(getDocItem("or"));
		branch.addItem(getDocItem("when"));
		branch.addItem(getDocItem("when-not"));
		branch.addItem(getDocItem("if-not"));
		branch.addItem(getDocItem("if-let"));
		branch.addItem(getDocItem("when-let"));

		final DocSection loop = new DocSection("Loop");
		all.addSection(loop);
		loop.addItem(getDocItem("while"));
		loop.addItem(getDocItem("dotimes"));
		loop.addItem(getDocItem("list-comp"));
		loop.addItem(getDocItem("doseq"));

		final DocSection call = new DocSection("Call");
		all.addSection(call);
		call.addItem(getDocItem("doto"));
		call.addItem(getDocItem("->"));
		call.addItem(getDocItem("->>"));
		call.addItem(getDocItem("-<>"));
		call.addItem(getDocItem("as->"));
		call.addItem(getDocItem("cond->"));
		call.addItem(getDocItem("cond->>"));

		final DocSection loading = new DocSection("Loading");
		all.addSection(loading);
		loading.addItem(getDocItem("load-module"));
		loading.addItem(getDocItem("load-file", false));
		loading.addItem(getDocItem("load-classpath-file"));
		loading.addItem(getDocItem("load-string"));
		
		final DocSection test = new DocSection("Test");
		all.addSection(test);
		test.addItem(getDocItem("macro?"));
		test.addItem(getDocItem("cond"));
		test.addItem(getDocItem("condp"));
		test.addItem(getDocItem("case"));

		final DocSection assert_ = new DocSection("Assert");
		all.addSection(assert_);
		assert_.addItem(getDocItem("assert"));

		final DocSection util = new DocSection("Util");
		all.addSection(util);
		util.addItem(getDocItem("comment"));
		util.addItem(getDocItem("gensym"));
		util.addItem(getDocItem("time"));
		util.addItem(getDocItem("with-out-str"));
		util.addItem(getDocItem("with-err-str"));
		
		final DocSection profil = new DocSection("Profiling");
		all.addSection(profil);
		profil.addItem(getDocItem("time"));
		profil.addItem(getDocItem("perf", false));
		
		return section;
	}

	private DocSection getTypesSection() {
		final DocSection section = new DocSection("Types", id());

		final DocSection all = new DocSection("");
		section.addSection(all);

		final DocSection test = new DocSection("Test");
		all.addSection(test);		
		test.addItem(getDocItem("type"));
		test.addItem(getDocItem("supertype"));
		test.addItem(getDocItem("instance?"));
		test.addItem(getDocItem("deftype?"));

		final DocSection define = new DocSection("Define");
		all.addSection(define);		
		define.addItem(getDocItem("deftype"));
		define.addItem(getDocItem("deftype-of"));
		define.addItem(getDocItem("deftype-or"));

		final DocSection create = new DocSection("Create");
		all.addSection(create);
		create.addItem(getDocItem(".:"));
		
		return section;
	}

	private DocSection getTransducersSection() {
		final DocSection section = new DocSection("Transducers", id());

		final DocSection all = new DocSection("");
		section.addSection(all);

		final DocSection run = new DocSection("Use");
		all.addSection(run);		
		run.addItem(getDocItem("transduce"));

		final DocSection func = new DocSection("Functions");
		all.addSection(func);		
		func.addItem(getDocItem("map"));
		func.addItem(getDocItem("map-indexed"));
		func.addItem(getDocItem("filter"));
		func.addItem(getDocItem("drop"));
		func.addItem(getDocItem("drop-while"));
		func.addItem(getDocItem("take"));
		func.addItem(getDocItem("take-while"));
		func.addItem(getDocItem("keep"));
		func.addItem(getDocItem("remove"));
		func.addItem(getDocItem("dedupe"));
		func.addItem(getDocItem("distinct"));
		func.addItem(getDocItem("sorted"));
		func.addItem(getDocItem("reverse"));
		func.addItem(getDocItem("flatten"));
		func.addItem(getDocItem("halt-when"));

		final DocSection red = new DocSection("Reductions");
		all.addSection(red);		
		red.addItem(getDocItem("rf-first"));
		red.addItem(getDocItem("rf-last"));
		red.addItem(getDocItem("rf-every?"));
		red.addItem(getDocItem("rf-any?"));
		
		final DocSection early = new DocSection("Early");
		all.addSection(early);		
		early.addItem(getDocItem("reduced"));
		early.addItem(getDocItem("reduced?"));
		early.addItem(getDocItem("deref"));
		early.addItem(getDocItem("deref?"));
		
		return section;
	}

	private DocSection getConcurrencySection() {
		final DocSection section = new DocSection("Concurrency", id());

		final DocSection all = new DocSection("");
		section.addSection(all);

		final DocSection atoms = new DocSection("Atoms");
		all.addSection(atoms);
		atoms.addItem(getDocItem("atom"));
		atoms.addItem(getDocItem("atom?"));
		atoms.addItem(getDocItem("deref"));
		atoms.addItem(getDocItem("deref?"));
		atoms.addItem(getDocItem("reset!"));
		atoms.addItem(getDocItem("swap!"));
		atoms.addItem(getDocItem("compare-and-set!"));
		atoms.addItem(getDocItem("add-watch"));
		atoms.addItem(getDocItem("remove-watch"));

		final DocSection futures = new DocSection("Futures");
		all.addSection(futures);
		futures.addItem(getDocItem("future"));
		futures.addItem(getDocItem("future?"));
		futures.addItem(getDocItem("future-done?"));
		futures.addItem(getDocItem("future-cancel"));
		futures.addItem(getDocItem("future-cancelled?"));
		futures.addItem(getDocItem("futures-fork"));
		futures.addItem(getDocItem("futures-wait"));
		futures.addItem(getDocItem("deref"));
		futures.addItem(getDocItem("deref?"));
		futures.addItem(getDocItem("realized?"));

		final DocSection promises = new DocSection("Promises");
		all.addSection(promises);
		promises.addItem(getDocItem("promise"));
		promises.addItem(getDocItem("promise?"));
		promises.addItem(getDocItem("deliver"));
		promises.addItem(getDocItem("realized?"));

		final DocSection delay = new DocSection("Delay");
		all.addSection(delay);
		delay.addItem(getDocItem("delay"));
		delay.addItem(getDocItem("delay?"));
		delay.addItem(getDocItem("deref"));
		delay.addItem(getDocItem("deref?"));
		delay.addItem(getDocItem("force"));
		delay.addItem(getDocItem("realized?"));

		final DocSection agents = new DocSection("Agents");
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
		
		final DocSection sched = new DocSection("Scheduler");
		all.addSection(sched);
		sched.addItem(getDocItem("schedule-delay", false));
		sched.addItem(getDocItem("schedule-at-fixed-rate", false));

		final DocSection locking = new DocSection("Locking");
		all.addSection(locking);
		locking.addItem(getDocItem("locking"));

		final DocSection volatiles = new DocSection("Volatiles");
		all.addSection(volatiles);
		volatiles.addItem(getDocItem("volatile"));
		volatiles.addItem(getDocItem("volatile?"));
		volatiles.addItem(getDocItem("deref"));
		volatiles.addItem(getDocItem("deref?"));
		volatiles.addItem(getDocItem("reset!"));
		volatiles.addItem(getDocItem("swap!"));
		
		final DocSection thlocal = new DocSection("ThreadLocal");
		all.addSection(thlocal);
		thlocal.addItem(getDocItem("thread-local"));
		thlocal.addItem(getDocItem("thread-local?"));
		thlocal.addItem(getDocItem("thread-local-clear"));
		thlocal.addItem(getDocItem("assoc"));
		thlocal.addItem(getDocItem("dissoc"));
		thlocal.addItem(getDocItem("get"));

		final DocSection threads = new DocSection("Threads");
		all.addSection(threads);
		threads.addItem(getDocItem("thread-id"));
		threads.addItem(getDocItem("thread-name"));
		threads.addItem(getDocItem("thread-interrupted?"));
		threads.addItem(getDocItem("thread-interrupted"));

		return section;
	}

	private DocSection getIOSection() {
		final DocSection section = new DocSection("IO", id());

		final DocSection all = new DocSection("");
		section.addSection(all);

		final DocSection to = new DocSection("to");
		all.addSection(to);
		to.addItem(getDocItem("print"));
		to.addItem(getDocItem("println"));
		to.addItem(getDocItem("printf"));
		to.addItem(getDocItem("flush"));
		to.addItem(getDocItem("newline"));

		final DocSection to_str = new DocSection("to-str");
		all.addSection(to_str);
		to_str.addItem(getDocItem("pr-str"));
		to_str.addItem(getDocItem("with-out-str"));

		final DocSection from = new DocSection("from");
		all.addSection(from);
		from.addItem(getDocItem("read-line"));
		from.addItem(getDocItem("read-string"));

		final DocSection file = new DocSection("file");
		all.addSection(file);
		file.addItem(getDocItem("io/file"));
		file.addItem(getDocItem("io/file-parent"));
		file.addItem(getDocItem("io/file-name"));
		file.addItem(getDocItem("io/file-path"));
		file.addItem(getDocItem("io/file-absolute-path"));
		file.addItem(getDocItem("io/file-canonical-path"));
		file.addItem(getDocItem("io/file-ext?"));
		file.addItem(getDocItem("io/file-size"));

		final DocSection file_dir = new DocSection("file dir");
		all.addSection(file_dir);
		file_dir.addItem(getDocItem("io/mkdir"));
		file_dir.addItem(getDocItem("io/mkdirs"));

		final DocSection file_io = new DocSection("file i/o");
		all.addSection(file_io);
		file_io.addItem(getDocItem("io/slurp"));
		file_io.addItem(getDocItem("io/slurp-lines"));
		file_io.addItem(getDocItem("io/spit"));
		file_io.addItem(getDocItem("io/copy-file"));
		file_io.addItem(getDocItem("io/move-file"));
		file_io.addItem(getDocItem("io/delete-file"));
		file_io.addItem(getDocItem("io/delete-file-on-exit"));
		file_io.addItem(getDocItem("io/delete-file-tree"));

		final DocSection file_list = new DocSection("file list");
		all.addSection(file_list);
		file_list.addItem(getDocItem("io/list-files"));
		file_list.addItem(getDocItem("io/list-files-glob"));
		file_list.addItem(getDocItem("io/list-file-tree"));

		final DocSection file_test = new DocSection("file test");
		all.addSection(file_test);
		file_test.addItem(getDocItem("io/file?"));
		file_test.addItem(getDocItem("io/exists-file?"));
		file_test.addItem(getDocItem("io/exists-dir?"));
		file_test.addItem(getDocItem("io/file-can-read?", false));
		file_test.addItem(getDocItem("io/file-can-write?", false));
		file_test.addItem(getDocItem("io/file-can-execute?", false));
		file_test.addItem(getDocItem("io/file-hidden?", false));

		final DocSection file_watch = new DocSection("file watch");
		all.addSection(file_watch);
		file_watch.addItem(getDocItem("io/await-for", false));
		file_watch.addItem(getDocItem("io/watch-dir", false));
		file_watch.addItem(getDocItem("io/close-watcher", false));
		
		final DocSection file_other = new DocSection("file other");
		all.addSection(file_other);
		file_other.addItem(getDocItem("io/temp-file"));
		file_other.addItem(getDocItem("io/tmp-dir"));
		file_other.addItem(getDocItem("io/user-dir"));
		file_other.addItem(getDocItem("io/user-home-dir"));

		final DocSection classpath = new DocSection("classpath");
		all.addSection(classpath);
		classpath.addItem(getDocItem("io/load-classpath-resource", false));
		classpath.addItem(getDocItem("io/classpath-resource?", false));
		
		final DocSection stream = new DocSection("stream");
		all.addSection(stream);
		stream.addItem(getDocItem("io/copy-stream"));
		stream.addItem(getDocItem("io/slurp-stream"));
		stream.addItem(getDocItem("io/spit-stream"));
		stream.addItem(getDocItem("io/uri-stream", false));
		stream.addItem(getDocItem("io/wrap-os-with-buffered-writer"));
		stream.addItem(getDocItem("io/wrap-os-with-print-writer"));
		stream.addItem(getDocItem("io/wrap-is-with-buffered-reader"));

		final DocSection rd_wr = new DocSection("reader/writer");
		all.addSection(rd_wr);
		rd_wr.addItem(getDocItem("io/buffered-reader"));
		rd_wr.addItem(getDocItem("io/buffered-writer"));

		final DocSection http = new DocSection("http");
		all.addSection(http);
		http.addItem(getDocItem("io/download", false));
		http.addItem(getDocItem("io/internet-avail?", false));
		
		final DocSection zip = new DocSection("zip");
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

		final DocSection gzip = new DocSection("gzip");
		all.addSection(gzip);
		gzip.addItem(getDocItem("io/gzip", false));
		gzip.addItem(getDocItem("io/gzip-to-stream"));
		gzip.addItem(getDocItem("io/gzip?"));
		gzip.addItem(getDocItem("io/ungzip"));
		gzip.addItem(getDocItem("io/ungzip-to-stream"));

		final DocSection other = new DocSection("other");
		all.addSection(other);
		other.addItem(getDocItem("with-out-str"));
		other.addItem(getDocItem("io/mime-type"));
		other.addItem(getDocItem("io/default-charset"));

		return section;
	}

	private DocSection getAppSection() {
		final DocSection section = new DocSection("Application", id());

		final DocSection all = new DocSection("");
		section.addSection(all);

		final DocSection mgmt = new DocSection("Management");
		all.addSection(mgmt);
		mgmt.addItem(getDocItem("app/build"));
		mgmt.addItem(getDocItem("app/manifest"));

		return section;
	}

	private DocSection getNamespaceSection() {
		final DocSection section = new DocSection("Namespace", id());

		final DocSection all = new DocSection("");
		section.addSection(all);

		final DocSection open = new DocSection("Open");
		all.addSection(open);
		open.addItem(getDocItem("ns"));

		final DocSection curr = new DocSection("Current");
		all.addSection(curr);
		curr.addItem(getDocItem("*ns*"));

		final DocSection remove = new DocSection("Remove");
		all.addSection(remove);
		remove.addItem(getDocItem("ns-unmap"));
		remove.addItem(getDocItem("ns-remove"));

		final DocSection util = new DocSection("Util");
		all.addSection(util);
		util.addItem(getDocItem("ns-list"));
		util.addItem(getDocItem("namespace"));
	
		return section;
	}

	private DocSection getByteBufSection() {
		final DocSection section = new DocSection("Byte Buffer", id());

		final DocSection all = new DocSection("");
		section.addSection(all);

		final DocSection bb_create = new DocSection("Create");
		all.addSection(bb_create);
		bb_create.addItem(getDocItem("bytebuf"));
		bb_create.addItem(getDocItem("bytebuf-allocate"));
		bb_create.addItem(getDocItem("bytebuf-from-string"));
		
		final DocSection bb_test = new DocSection("Test");
		all.addSection(bb_test);
		bb_test.addItem(getDocItem("empty?"));
		bb_test.addItem(getDocItem("not-empty?"));
		bb_test.addItem(getDocItem("bytebuf?"));

		final DocSection bb_use = new DocSection("Use");
		all.addSection(bb_use);
		bb_use.addItem(getDocItem("count"));
		bb_use.addItem(getDocItem("bytebuf-capacity"));
		bb_use.addItem(getDocItem("bytebuf-limit"));
		bb_use.addItem(getDocItem("bytebuf-to-string"));
		bb_use.addItem(getDocItem("bytebuf-to-list"));
		bb_use.addItem(getDocItem("bytebuf-sub"));
		bb_use.addItem(getDocItem("bytebuf-pos"));
		bb_use.addItem(getDocItem("bytebuf-pos!"));

		final DocSection bb_read = new DocSection("Read");
		all.addSection(bb_read);
		bb_read.addItem(getDocItem("bytebuf-get-byte"));
		bb_read.addItem(getDocItem("bytebuf-get-int"));
		bb_read.addItem(getDocItem("bytebuf-get-long"));
		bb_read.addItem(getDocItem("bytebuf-get-float"));
		bb_read.addItem(getDocItem("bytebuf-get-double"));

		final DocSection bb_write = new DocSection("Write");
		all.addSection(bb_write);
		bb_write.addItem(getDocItem("bytebuf-put-byte!"));
		bb_write.addItem(getDocItem("bytebuf-put-int!"));
		bb_write.addItem(getDocItem("bytebuf-put-long!"));
		bb_write.addItem(getDocItem("bytebuf-put-float!"));
		bb_write.addItem(getDocItem("bytebuf-put-double!"));
		bb_write.addItem(getDocItem("bytebuf-put-buf!"));

		final DocSection encode = new DocSection("Base64");
		all.addSection(encode);
		encode.addItem(getDocItem("str/encode-base64"));
		encode.addItem(getDocItem("str/decode-base64"));

		final DocSection hex = new DocSection("Hex");
		all.addSection(hex);
		hex.addItem(getDocItem("str/hex-to-bytebuf"));
		hex.addItem(getDocItem("str/bytebuf-to-hex"));
		hex.addItem(getDocItem("str/format-bytebuf"));

		return section;
	}

	private DocSection getTimeSection() {
		final DocSection section = new DocSection("Time", id());

		final DocSection all = new DocSection("");
		section.addSection(all);

		final DocSection date = new DocSection("Date");
		all.addSection(date);
		date.addItem(getDocItem("time/date"));
		date.addItem(getDocItem("time/date?"));

		final DocSection local_date = new DocSection("Local Date");
		all.addSection(local_date);
		local_date.addItem(getDocItem("time/local-date"));
		local_date.addItem(getDocItem("time/local-date?"));
		local_date.addItem(getDocItem("time/local-date-parse"));

		final DocSection local_date_time = new DocSection("Local Date Time");
		all.addSection(local_date_time);
		local_date_time.addItem(getDocItem("time/local-date-time"));
		local_date_time.addItem(getDocItem("time/local-date-time?"));
		local_date_time.addItem(getDocItem("time/local-date-time-parse"));

		final DocSection zoned_date_time = new DocSection("Zoned Date Time");
		all.addSection(zoned_date_time);
		zoned_date_time.addItem(getDocItem("time/zoned-date-time"));
		zoned_date_time.addItem(getDocItem("time/zoned-date-time?"));
		zoned_date_time.addItem(getDocItem("time/zoned-date-time-parse"));
		
		final DocSection fields = new DocSection("Fields");
		all.addSection(fields);
		fields.addItem(getDocItem("time/year"));
		fields.addItem(getDocItem("time/month"));
		fields.addItem(getDocItem("time/day-of-week"));
		fields.addItem(getDocItem("time/day-of-month"));
		fields.addItem(getDocItem("time/day-of-year"));
		fields.addItem(getDocItem("time/hour"));
		fields.addItem(getDocItem("time/minute"));
		fields.addItem(getDocItem("time/second"));

		final DocSection etc = new DocSection("Fields etc");
		all.addSection(etc);
		etc.addItem(getDocItem("time/length-of-year"));
		etc.addItem(getDocItem("time/length-of-month"));
		etc.addItem(getDocItem("time/first-day-of-month"));
		etc.addItem(getDocItem("time/last-day-of-month"));
		
		final DocSection zone = new DocSection("Zone");
		all.addSection(zone);
		zone.addItem(getDocItem("time/zone"));
		zone.addItem(getDocItem("time/zone-offset"));

		final DocSection format = new DocSection("Format");
		all.addSection(format);
		format.addItem(getDocItem("time/formatter"));
		format.addItem(getDocItem("time/format"));
		
		final DocSection compare = new DocSection("Test");
		all.addSection(compare);
		compare.addItem(getDocItem("time/after?"));
		compare.addItem(getDocItem("time/not-after?"));
		compare.addItem(getDocItem("time/before?"));
		compare.addItem(getDocItem("time/not-before?"));
		compare.addItem(getDocItem("time/within?"));
		compare.addItem(getDocItem("time/leap-year?"));
		
		final DocSection misc = new DocSection("Miscellaneous");
		all.addSection(misc);
		misc.addItem(getDocItem("time/with-time"));
		misc.addItem(getDocItem("time/plus"));
		misc.addItem(getDocItem("time/minus"));
		misc.addItem(getDocItem("time/period"));
		misc.addItem(getDocItem("time/earliest"));
		misc.addItem(getDocItem("time/latest"));

		final DocSection util = new DocSection("Util");
		all.addSection(util);
		util.addItem(getDocItem("time/zone-ids"));
		util.addItem(getDocItem("time/to-millis"));

		return section;
	}

	private DocSection getSpecialFormsSection() {
		final DocSection section = new DocSection("Special Forms", id());

		final DocSection all = new DocSection("");
		section.addSection(all);

		final DocSection generic = new DocSection("Forms");
		all.addSection(generic);

		generic.addItem(getDocItem("def"));
		generic.addItem(getDocItem("defonce"));
		generic.addItem(getDocItem("def-dynamic"));
		generic.addItem(getDocItem("defmulti"));
		generic.addItem(getDocItem("defmethod"));
		generic.addItem(getDocItem("if"));
		generic.addItem(getDocItem("do"));
		generic.addItem(getDocItem("let"));
		generic.addItem(getDocItem("binding"));
		generic.addItem(getDocItem("fn"));
		generic.addItem(getDocItem("set!"));

		final DocSection recur = new DocSection("Recursion");
		all.addSection(recur);
		recur.addItem(getDocItem("loop"));
		recur.addItem(getDocItem("recur"));
		recur.addItem(getDocItem("tail-pos", true, true));

		final DocSection ex = new DocSection("Exception");
		all.addSection(ex);
		ex.addItem(getDocItem("throw", true, true));
		ex.addItem(getDocItem("try", true, true));
		ex.addItem(getDocItem("try-with", true, true));

		final DocSection profiling = new DocSection("Profiling");
		all.addSection(profiling);

		profiling.addItem(getDocItem("dobench"));
		profiling.addItem(getDocItem("dorun"));
		profiling.addItem(getDocItem("prof"));

		return section;
	}

	private DocSection getJavaInteropSection() {
		final DocSection section = new DocSection("Java Interoperability", id());

		final DocSection all = new DocSection("");
		section.addSection(all);
			
		final DocSection java = new DocSection("Java");
		all.addSection(java);	
		java.addItem(getDocItem("."));
		java.addItem(getDocItem("import"));
		java.addItem(getDocItem("java-iterator-to-list"));
		java.addItem(getDocItem("java-enumeration-to-list"));
		java.addItem(getDocItem("java-unwrap-optional"));
		java.addItem(getDocItem("cast"));
		java.addItem(getDocItem("class"));
		
		final DocSection proxy = new DocSection("Proxify");
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

		final DocSection test = new DocSection("Test");
		all.addSection(test);	
		test.addItem(getDocItem("java-obj?"));
		test.addItem(getDocItem("exists-class?"));

		final DocSection support = new DocSection("Support");
		all.addSection(support);	
		support.addItem(getDocItem("imports"));
		support.addItem(getDocItem("supers"));
		support.addItem(getDocItem("bases"));
		support.addItem(getDocItem("formal-type"));
		support.addItem(getDocItem("stacktrace", false, false));

		final DocSection clazz = new DocSection("Class");
		all.addSection(clazz);	
		clazz.addItem(getDocItem("class"));
		clazz.addItem(getDocItem("class-of"));
		clazz.addItem(getDocItem("class-name"));
		clazz.addItem(getDocItem("class-version"));
		clazz.addItem(getDocItem("classloader"));
		clazz.addItem(getDocItem("classloader-of"));
		
		return section;
	}

	private DocSection getMiscellaneousSection() {
		final DocSection section = new DocSection("Miscellaneous", id());

		final DocSection all = new DocSection("");
		section.addSection(all);
		
		final DocSection json = new DocSection("JSON");
		all.addSection(json);
		json.addItem(getDocItem("json/write-str"));
		json.addItem(getDocItem("json/read-str"));
		json.addItem(getDocItem("json/spit"));
		json.addItem(getDocItem("json/slurp"));
		json.addItem(getDocItem("json/pretty-print"));
		
		final DocSection pdf = new DocSection("PDF");
		all.addSection(pdf);
		pdf.addItem(getDocItem("pdf/render", false));
		pdf.addItem(getDocItem("pdf/text-to-pdf", false));
		pdf.addItem(getDocItem("pdf/available?", false));
		pdf.addItem(getDocItem("pdf/check-required-libs", false));
		
		final DocSection pdf_tools = new DocSection("PDF Tools");
		all.addSection(pdf_tools);
		pdf_tools.addItem(getDocItem("pdf/merge", false));
		pdf_tools.addItem(getDocItem("pdf/copy", false));
		pdf_tools.addItem(getDocItem("pdf/pages"));
		pdf_tools.addItem(getDocItem("pdf/watermark", false));

		final DocSection csv = new DocSection("CSV");
		all.addSection(csv);
		csv.addItem(getDocItem("csv/read"));
		csv.addItem(getDocItem("csv/write", false));
		csv.addItem(getDocItem("csv/write-str"));
		
		final DocSection cidr = new DocSection("CIDR");
		all.addSection(cidr);
		cidr.addItem(getDocItem("cidr/parse"));
		cidr.addItem(getDocItem("cidr/in-range?"));
		cidr.addItem(getDocItem("cidr/start-inet-addr"));
		cidr.addItem(getDocItem("cidr/end-inet-addr"));
		cidr.addItem(getDocItem("cidr/inet-addr"));
		cidr.addItem(getDocItem("cidr/inet-addr-to-bytes"));
		cidr.addItem(getDocItem("cidr/inet-addr-from-bytes"));
		
		final DocSection cidr_trie = new DocSection("CIDR Trie");
		all.addSection(cidr_trie);
		cidr_trie.addItem(getDocItem("cidr/trie"));
		cidr_trie.addItem(getDocItem("cidr/size"));
		cidr_trie.addItem(getDocItem("cidr/insert"));
		cidr_trie.addItem(getDocItem("cidr/lookup"));
		cidr_trie.addItem(getDocItem("cidr/lookup-reverse"));
		
		final DocSection other = new DocSection("Other");
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

	private DocSection getModulesSection() {
		final DocSection section = new DocSection("Extension Modules (selection)", id());

		final DocSection all = new DocSection("");
		section.addSection(all);

		final DocSection kira = new DocSection("Kira");
		all.addSection(kira);
		kira.addItem(new DocItem("(load-module :kira)", null));
		kira.addItem(getDocItem("kira/eval"));
		kira.addItem(getDocItem("kira/fn"));
		kira.addItem(getDocItem("kira/escape-xml"));
		kira.addItem(getDocItem("kira/escape-html"));


		final DocSection trace = new DocSection("Tracing");
		all.addSection(trace);
		trace.addItem(new DocItem("(load-module :trace)", null));
		trace.addItem(getDocItem("trace/trace"));
		trace.addItem(getDocItem("trace/traced?"));
		trace.addItem(getDocItem("trace/traceable?"));
		trace.addItem(getDocItem("trace/trace-var"));
		trace.addItem(getDocItem("trace/untrace-var"));
		
		final DocSection xml = new DocSection("XML");
		all.addSection(xml);
		xml.addItem(new DocItem("(load-module :xml)", null));
		xml.addItem(getDocItem("xml/parse-str"));
		xml.addItem(getDocItem("xml/parse"));
		xml.addItem(getDocItem("xml/path->"));
		xml.addItem(getDocItem("xml/children"));
		xml.addItem(getDocItem("xml/text"));
		
		final DocSection crypt = new DocSection("Cryptography");
		all.addSection(crypt);
		crypt.addItem(new DocItem("(load-module :crypt)", null));
		crypt.addItem(getDocItem("crypt/md5-hash"));
		crypt.addItem(getDocItem("crypt/sha1-hash"));
		crypt.addItem(getDocItem("crypt/sha512-hash"));
		crypt.addItem(getDocItem("crypt/pbkdf2-hash"));
		crypt.addItem(getDocItem("crypt/encrypt"));
		crypt.addItem(getDocItem("crypt/decrypt"));
		
		final DocSection gradle = new DocSection("Gradle");
		all.addSection(gradle);
		gradle.addItem(new DocItem("(load-module :gradle)", null));
		gradle.addItem(getDocItem("gradle/with-home", false));
		gradle.addItem(getDocItem("gradle/version", false));
		gradle.addItem(getDocItem("gradle/task", false));
		
		final DocSection maven = new DocSection("Maven");
		all.addSection(maven);
		maven.addItem(new DocItem("(load-module :maven)", null));
		maven.addItem(getDocItem("maven/download", false));
		maven.addItem(getDocItem("maven/get", false));
		maven.addItem(getDocItem("maven/uri", false));

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
			return new DocItem(
					name, 
					toStringList(fn.getArgLists()), 
					fn.getDoc() == Constants.Nil ? "" : ((VncString)fn.getDoc()).getValue(),
					runExamples(name, toStringList(fn.getExamples()), runExamples, catchEx),
					createCrossRefs(name, fn),
					id(name));
		}
		else {
			throw new RuntimeException(String.format("Unknown function %s", name));
		}
	}

	private List<ExampleOutput> runExamples(
			final String name, 
			final List<String> examples, 
			final boolean run,
			final boolean catchEx
	) {
		final Venice runner = new Venice();

		try {
			final AtomicLong idx = new AtomicLong(0L);
			
			return examples
						.stream()
						.filter(e -> !StringUtil.isEmpty(e))
						.map(e -> runExample(
									runner, 
									idx.getAndIncrement(), 
									name, 
									e, 
									run, 
									catchEx))
						.collect(Collectors.toList());
		}
		catch(RuntimeException ex) {
			throw new RuntimeException(String.format(
					"Failed to run examples for %s", name), 
					ex);
		}
	}
	
	private ExampleOutput runExample(
			final Venice runner,
			final long id,
			final String name, 
			final String example, 
			final boolean run,
			final boolean catchEx
	) {
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
						id, name, example, ps_out.getOutput(), ps_err.getOutput(), result);
			}
			catch(RuntimeException ex) {
				if (catchEx) {							
					return new ExampleOutput(
							id, name, example, ps_out.getOutput(), ps_err.getOutput(), ex);
				}
				else {
					throw ex;
				}
			}
		}
		else {
			return new ExampleOutput(id, name, example);
		}
	}
	
	private VncFunction findFunction(final String name) {
		// Special forms
		VncFunction fn = (VncFunction)SpecialFormsDoc.ns.get(new VncSymbol(name));
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
		final int posLF = descr.indexOf('\n');
		
		String s = (posLF == -1) ? descr.trim() : descr.substring(0, posLF).trim();

		if (s.length() > 145) {
			// do not cut in the middle of a word
			final int spacePos = s.indexOf(' ', 135); 
			s = (spacePos != -1)
				  ? s.substring(0, spacePos)
				  : s.substring(0, 140).trim();
				  
			if (!s.endsWith(".")) {
				s = s + " ...";
			}
		}
		
		return s;
	}

	private CrossRef createCrossRef(final String name, final String descr) {
		return new CrossRef(name, id(name), descr);
	}

	private List<String> toStringList(final VncList list) {
		try {
			return list.getList()
					   .stream()
					   .map(s -> ((VncString)s).getValue())
					   .collect(Collectors.toList());
		}
		catch(Exception ex) {
			throw ex;
		}
	}

	private List<DocSection> concat(final List<DocSection> l1, final List<DocSection> l2) {
		final List<DocSection> list = new ArrayList<>();
		list.addAll(l1);
		list.addAll(l2);
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
		final VncVal val = env.get(new VncSymbol(name));
		return Types.isVncFunction(val) ? (VncFunction)val : null;
	}

	private String id() {
		return String.valueOf(gen.getAndIncrement());
	}

	private String id(final String name) {
		return idMap.computeIfAbsent(name, n -> String.valueOf(gen.getAndIncrement()));
	}
	
	
	
	private final Map<String,String> idMap = new HashMap<>();
	
	private final AtomicLong gen = new AtomicLong(1000);
	
	private final List<String> preloadedModules = new ArrayList<>();

	private final Map<String, DocItem> docItems = new HashMap<>();
	private final Env env;
}
