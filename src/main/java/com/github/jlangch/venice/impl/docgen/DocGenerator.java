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
package com.github.jlangch.venice.impl.docgen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.Version;
import com.github.jlangch.venice.impl.Env;
import com.github.jlangch.venice.impl.VeniceInterpreter;
import com.github.jlangch.venice.impl.javainterop.JavaImports;
import com.github.jlangch.venice.impl.javainterop.JavaInteropFn;
import com.github.jlangch.venice.impl.javainterop.JavaInteropProxifyFn;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.Types;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.util.CapturingPrintStream;


public class DocGenerator {

	public DocGenerator() {
		this.env = new VeniceInterpreter().createEnv(new PrintStream(System.out));
	}

	public static void main(final String[] args) {
		new DocGenerator().run();
	}
	
	private void run() {
		try {			
			final List<DocSection> left = getLeftSections();
			final List<DocSection> right = getRightSections();
			
			final Map<String,Object> data = new HashMap<>();
			data.put("version", Version.VERSION);
			data.put("sections", concat(left, right));
			data.put("left", left);
			data.put("right", right);
			data.put("details", getDocItems(concat(left, right)));
			data.put("snippets", new CodeSnippetReader().readSnippets());
			
			// HTML
			data.put("pdfmode", false);
			final String html = HtmlRenderer.renderCheatSheet(data);
			save(new File(getUserDir(), "cheatsheet.html"), html);
			
			// PDF
			data.put("pdfmode", true);
			final String xhtml = HtmlRenderer.renderCheatSheet(data);
			final byte[] pdf = PdfRenderer.renderCheatSheet(xhtml);
			save(new File(getUserDir(), "cheatsheet.pdf"), pdf);
			
			System.out.println("Genereated Cheat Sheet at: " + getUserDir());
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
		
	private List<DocSection> getLeftSections() {
		return Arrays.asList(
				getPrimitivesSection(),
				getFunctionsSection(),
				getMacrosSection(),
				getIOSection());
	}
	
	private List<DocSection> getRightSections() {
		return Arrays.asList(
				getCollectionsSection(),
				getOtherTypesSection(),
				getAtomsSection(),
				getSpecialFormsSection(),
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
				.distinct()
				.filter(i -> !StringUtil.isBlank(i.getName()))
				.sorted(Comparator.comparing(DocItem::getName))
				.collect(Collectors.toList());
	}
	
	private DocSection getPrimitivesSection() {
		final DocSection section = new DocSection("Primitives");
		
		final DocSection lit = new DocSection("Literals");
		section.addSection(lit);
		
		final DocSection literals = new DocSection("Literals");
		lit.addSection(literals);

		literals.addItem(new DocItem("Nil: nil", idgen.id()));
		literals.addItem(new DocItem("Long: 1500", idgen.id()));
		literals.addItem(new DocItem("Double: 3.569", idgen.id()));
		literals.addItem(new DocItem("Boolean: true, false", idgen.id()));
		literals.addItem(new DocItem("BigDecimal: 6.897M", idgen.id()));
		literals.addItem(new DocItem("String: \"abcde\" 'abcde'", idgen.id()));

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

		final DocSection compare = new DocSection("Compare");
		numbers.addSection(compare);
		compare.addItem(getDocItem("=="));
		compare.addItem(getDocItem("!="));
		compare.addItem(getDocItem("<"));
		compare.addItem(getDocItem(">"));
		compare.addItem(getDocItem("<="));
		compare.addItem(getDocItem(">="));

		final DocSection test = new DocSection("Test");
		numbers.addSection(test);
		test.addItem(getDocItem("nil?"));
		test.addItem(getDocItem("some?"));
		test.addItem(getDocItem("zero?"));
		test.addItem(getDocItem("pos?"));
		test.addItem(getDocItem("neg?"));
		test.addItem(getDocItem("even?"));
		test.addItem(getDocItem("odd?"));
		test.addItem(getDocItem("number?"));
		test.addItem(getDocItem("long?"));
		test.addItem(getDocItem("double?"));
		test.addItem(getDocItem("decimal?"));

		final DocSection random = new DocSection("Random");
		numbers.addSection(random);
		random.addItem(getDocItem("rand-long"));
		random.addItem(getDocItem("rand-double"));

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

		final DocSection use = new DocSection("Use");
		strings.addSection(use);
		use.addItem(getDocItem("count"));
		use.addItem(getDocItem("empty-to-nil"));
		use.addItem(getDocItem("str/index-of"));
		use.addItem(getDocItem("str/last-index-of"));
		use.addItem(getDocItem("str/replace-first"));
		use.addItem(getDocItem("str/replace-last"));
		use.addItem(getDocItem("str/replace-all"));
		use.addItem(getDocItem("str/lower-case"));
		use.addItem(getDocItem("str/upper-case"));
		use.addItem(getDocItem("str/join"));
		use.addItem(getDocItem("str/subs"));
		use.addItem(getDocItem("str/split"));
		use.addItem(getDocItem("str/split-lines"));
		use.addItem(getDocItem("str/strip-start"));
		use.addItem(getDocItem("str/strip-end"));
		use.addItem(getDocItem("str/strip-indent"));
		use.addItem(getDocItem("str/strip-margin"));
		use.addItem(getDocItem("str/repeat"));
		use.addItem(getDocItem("str/truncate"));

		final DocSection regex = new DocSection("Regex");
		strings.addSection(regex);
		regex.addItem(getDocItem("match"));
		regex.addItem(getDocItem("match-not"));

		final DocSection trim = new DocSection("Trim");
		strings.addSection(trim);
		trim.addItem(getDocItem("str/trim"));
		trim.addItem(getDocItem("str/trim-to-nil"));

		final DocSection str_test = new DocSection("Test");
		strings.addSection(str_test);
		str_test.addItem(getDocItem("string?"));
		str_test.addItem(getDocItem("empty?"));
		str_test.addItem(getDocItem("str/blank?"));
		str_test.addItem(getDocItem("str/starts-with?"));
		str_test.addItem(getDocItem("str/ends-with?"));
		str_test.addItem(getDocItem("str/contains?"));

		
		final DocSection other = new DocSection("Other");
		section.addSection(other);

		final DocSection keywords = new DocSection("Keywords");
		other.addSection(keywords);
		keywords.addItem(getDocItem("keyword?"));
		keywords.addItem(getDocItem("keyword"));
		keywords.addItem(new DocItem("literals: :a :xyz", idgen.id()));

		final DocSection symbols = new DocSection("Symbols");
		other.addSection(symbols);
		symbols.addItem(getDocItem("symbol?"));
		symbols.addItem(getDocItem("symbol"));

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
		final DocSection section = new DocSection("Collections");


		final DocSection collections = new DocSection("Collections");
		section.addSection(collections);
		
		final DocSection generic = new DocSection("Generic");
		collections.addSection(generic);
		generic.addItem(getDocItem("count"));
		generic.addItem(getDocItem("empty?"));
		generic.addItem(getDocItem("not-empty?"));
		generic.addItem(getDocItem("empty-to-nil"));
		generic.addItem(getDocItem("empty"));
		generic.addItem(getDocItem("into"));
		generic.addItem(getDocItem("conj"));
		generic.addItem(getDocItem("remove"));
		generic.addItem(getDocItem("repeat"));
		generic.addItem(getDocItem("range"));
		generic.addItem(getDocItem("group-by"));
		generic.addItem(getDocItem("get-in"));

		final DocSection coll_test = new DocSection("Tests");
		collections.addSection(coll_test);
		coll_test.addItem(getDocItem("coll?"));
		coll_test.addItem(getDocItem("list?"));
		coll_test.addItem(getDocItem("vector?"));
		coll_test.addItem(getDocItem("set?"));
		coll_test.addItem(getDocItem("map?"));
		coll_test.addItem(getDocItem("sequential?"));
		coll_test.addItem(getDocItem("hash-map?"));
		coll_test.addItem(getDocItem("ordered-map?"));
		coll_test.addItem(getDocItem("sorted-map?"));
		coll_test.addItem(getDocItem("bytebuf?"));

		final DocSection coll_process = new DocSection("Process");
		collections.addSection(coll_process);
		coll_process.addItem(getDocItem("map"));
		coll_process.addItem(getDocItem("filter"));
		coll_process.addItem(getDocItem("docoll"));

		
		
		final DocSection lists = new DocSection("Lists");
		section.addSection(lists);

		final DocSection list_create = new DocSection("Create");
		lists.addSection(list_create);
		list_create.addItem(getDocItem("()"));
		list_create.addItem(getDocItem("list"));

		final DocSection list_access = new DocSection("Access");
		lists.addSection(list_access);
		list_access.addItem(getDocItem("first"));
		list_access.addItem(getDocItem("second"));
		list_access.addItem(getDocItem("nth"));
		list_access.addItem(getDocItem("last"));
		list_access.addItem(getDocItem("peek"));
		list_access.addItem(getDocItem("rest"));
		list_access.addItem(getDocItem("nfirst"));
		list_access.addItem(getDocItem("nlast"));

		final DocSection list_modify = new DocSection("Modify");
		lists.addSection(list_modify);
		list_modify.addItem(getDocItem("cons"));
		list_modify.addItem(getDocItem("conj"));
		list_modify.addItem(getDocItem("rest"));
		list_modify.addItem(getDocItem("pop"));
		list_modify.addItem(getDocItem("into"));
		list_modify.addItem(getDocItem("concat"));
		list_modify.addItem(getDocItem("interpose"));
		list_modify.addItem(getDocItem("interleave"));
		list_modify.addItem(getDocItem("mapcat"));
		list_modify.addItem(getDocItem("flatten"));
		list_modify.addItem(getDocItem("reduce"));
		list_modify.addItem(getDocItem("reverse"));
		list_modify.addItem(getDocItem("sort"));
		list_modify.addItem(getDocItem("sort-by"));
		list_modify.addItem(getDocItem("take"));
		list_modify.addItem(getDocItem("take-while"));
		list_modify.addItem(getDocItem("drop"));
		list_modify.addItem(getDocItem("drop-while"));
	
		final DocSection list_test = new DocSection("Test");
		lists.addSection(list_test);
		list_test.addItem(getDocItem("every?"));
		list_test.addItem(getDocItem("any?"));
		
		
		final DocSection vectors = new DocSection("Vectors");
		section.addSection(vectors);

		final DocSection vec_create = new DocSection("Create");
		vectors.addSection(vec_create);
		vec_create.addItem(getDocItem("[]"));
		vec_create.addItem(getDocItem("vector"));
		vec_create.addItem(getDocItem("vec"));
		vec_create.addItem(getDocItem("mapv"));

		final DocSection vec_access = new DocSection("Access");
		vectors.addSection(vec_access);
		vec_access.addItem(getDocItem("first"));
		vec_access.addItem(getDocItem("second"));
		vec_access.addItem(getDocItem("nth"));
		vec_access.addItem(getDocItem("last"));
		vec_access.addItem(getDocItem("peek"));
		vec_access.addItem(getDocItem("rest"));
		vec_access.addItem(getDocItem("nfirst"));
		vec_access.addItem(getDocItem("nlast"));
		vec_access.addItem(getDocItem("subvec"));

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
		vec_modify.addItem(getDocItem("interpose"));
		vec_modify.addItem(getDocItem("interleave"));
		vec_modify.addItem(getDocItem("mapcat"));
		vec_modify.addItem(getDocItem("flatten"));
		vec_modify.addItem(getDocItem("reduce"));
		vec_modify.addItem(getDocItem("reverse"));
		vec_modify.addItem(getDocItem("sort"));
		vec_modify.addItem(getDocItem("sort-by"));
		vec_modify.addItem(getDocItem("take"));
		vec_modify.addItem(getDocItem("take-while"));
		vec_modify.addItem(getDocItem("drop"));
		vec_modify.addItem(getDocItem("drop-while"));
		vec_modify.addItem(getDocItem("assoc-in"));
		vec_modify.addItem(getDocItem("get-in"));
		vec_modify.addItem(getDocItem("update"));
		vec_modify.addItem(getDocItem("update!"));
			
		final DocSection vec_test = new DocSection("Test");
		vectors.addSection(vec_test);
		vec_test.addItem(getDocItem("contains?"));
		vec_test.addItem(getDocItem("every?"));
		vec_test.addItem(getDocItem("any?"));
	
		
		final DocSection sets = new DocSection("Sets");
		section.addSection(sets);

		final DocSection set_create = new DocSection("Create");
		sets.addSection(set_create);
		set_create.addItem(getDocItem("set"));

		final DocSection set_modify = new DocSection("Modify");
		sets.addSection(set_modify);
		set_modify.addItem(getDocItem("difference"));

		final DocSection set_test = new DocSection("Test");
		sets.addSection(set_test);
		set_test.addItem(getDocItem("contains?"));

		
		final DocSection maps = new DocSection("Maps");
		section.addSection(maps);

		final DocSection maps_create = new DocSection("Create");
		maps.addSection(maps_create);
		maps_create.addItem(getDocItem("{}"));
		maps_create.addItem(getDocItem("hash-map"));
		maps_create.addItem(getDocItem("ordered-map"));
		maps_create.addItem(getDocItem("sorted-map"));
		maps_create.addItem(getDocItem("zipmap"));
		

		final DocSection map_access = new DocSection("Access");
		maps.addSection(map_access);
		map_access.addItem(getDocItem("find"));
		map_access.addItem(getDocItem("get"));
		map_access.addItem(getDocItem("keys"));
		map_access.addItem(getDocItem("vals"));
		map_access.addItem(getDocItem("key"));
		map_access.addItem(getDocItem("val"));

		final DocSection map_modify = new DocSection("Modify");
		maps.addSection(map_modify);
		map_modify.addItem(getDocItem("cons"));
		map_modify.addItem(getDocItem("conj"));
		map_modify.addItem(getDocItem("assoc"));
		map_modify.addItem(getDocItem("assoc-in"));
		map_modify.addItem(getDocItem("get-in"));
		map_modify.addItem(getDocItem("update"));
		map_modify.addItem(getDocItem("update!"));
		map_modify.addItem(getDocItem("dissoc"));
		map_modify.addItem(getDocItem("into"));
		map_modify.addItem(getDocItem("concat"));
		map_modify.addItem(getDocItem("flatten"));
		map_modify.addItem(getDocItem("reduce-kv"));
		map_modify.addItem(getDocItem("merge"));
		
		final DocSection map_test = new DocSection("Test");
		maps.addSection(map_test);
		map_test.addItem(getDocItem("contains?"));

		return section;
	}


	private DocSection getOtherTypesSection() {
		final DocSection section = new DocSection("Other Types");

		final DocSection bytebuf = new DocSection("ByteBuffer");
		section.addSection(bytebuf);

		final DocSection bytebuf_misc = new DocSection("Misc");
		bytebuf.addSection(bytebuf_misc);
		bytebuf_misc.addItem(getDocItem("count"));
		bytebuf_misc.addItem(getDocItem("empty?"));
		bytebuf_misc.addItem(getDocItem("not-empty?"));
		bytebuf_misc.addItem(getDocItem("bytebuf"));
		bytebuf_misc.addItem(getDocItem("bytebuf?"));
		bytebuf_misc.addItem(getDocItem("subbytebuf"));
		
		return section;
	}
		

	private DocSection getFunctionsSection() {
		final DocSection section = new DocSection("Functions");

		final DocSection all = new DocSection("");
		section.addSection(all);

		final DocSection create = new DocSection("Create");
		all.addSection(create);
		create.addItem(getDocItem("fn"));
		create.addItem(getDocItem("identity"));


		final DocSection call = new DocSection("Call");
		all.addSection(call);
		call.addItem(getDocItem("apply"));
		call.addItem(getDocItem("comp"));
		call.addItem(getDocItem("partial"));
		call.addItem(getDocItem("memoize"));


		final DocSection test = new DocSection("Test");
		all.addSection(test);
		test.addItem(getDocItem("fn?"));


		final DocSection ex = new DocSection("Exception");
		all.addSection(ex);
		ex.addItem(getDocItem("throw"));


		final DocSection misc = new DocSection("Misc");
		all.addSection(misc);
		misc.addItem(getDocItem("class"));
		misc.addItem(getDocItem("eval"));


		final DocSection util = new DocSection("Other");
		all.addSection(util);
		util.addItem(getDocItem("version"));
		util.addItem(getDocItem("uuid"));
		util.addItem(getDocItem("time-ms"));
		util.addItem(getDocItem("time-ns"));
		util.addItem(getDocItem("coalesce"));
		

		final DocSection meta = new DocSection("Meta");
		all.addSection(meta);
		meta.addItem(getDocItem("meta"));
		meta.addItem(getDocItem("with-meta"));
		meta.addItem(getDocItem("vary-meta"));
		
		return section;
	}

	private DocSection getMacrosSection() {
		final DocSection section = new DocSection("Macros");

		final DocSection all = new DocSection("");
		section.addSection(all);

		final DocSection create = new DocSection("Create");
		all.addSection(create);
		create.addItem(new DocItem("defmacro", idgen.id()));


		final DocSection branch = new DocSection("Branch");
		all.addSection(branch);
		branch.addItem(getDocItem("and"));
		branch.addItem(getDocItem("or"));
		branch.addItem(getDocItem("when"));
		branch.addItem(getDocItem("when-not"));
		branch.addItem(getDocItem("if-let"));

		final DocSection loop = new DocSection("Loop");
		all.addSection(loop);
		loop.addItem(getDocItem("list-comp"));
		loop.addItem(getDocItem("dotimes"));
		loop.addItem(getDocItem("while"));

		final DocSection call = new DocSection("Call");
		all.addSection(call);
		call.addItem(getDocItem("doto"));
		call.addItem(getDocItem("->"));
		call.addItem(getDocItem("->>"));

		final DocSection loading = new DocSection("Loading");
		all.addSection(loading);
		loading.addItem(getDocItem("load-string"));
		loading.addItem(getDocItem("load-file"));
		loading.addItem(getDocItem("load-module"));

		final DocSection test = new DocSection("Test");
		all.addSection(test);
		test.addItem(getDocItem("macro?"));
		test.addItem(getDocItem("cond"));

		final DocSection assert_ = new DocSection("Assert");
		all.addSection(assert_);
		assert_.addItem(getDocItem("assert"));

		final DocSection util = new DocSection("Util");
		all.addSection(util);
		util.addItem(getDocItem("comment"));
		util.addItem(getDocItem("gensym"));
		util.addItem(getDocItem("time"));
		
		return section;
	}

	private DocSection getAtomsSection() {
		final DocSection section = new DocSection("Atoms");

		final DocSection all = new DocSection("");
		section.addSection(all);

		final DocSection create = new DocSection("Create");
		all.addSection(create);
		create.addItem(getDocItem("atom"));

		final DocSection test = new DocSection("Test");
		all.addSection(test);
		test.addItem(getDocItem("atom?"));

		final DocSection access = new DocSection("Access");
		all.addSection(access);
		access.addItem(getDocItem("deref"));
		access.addItem(getDocItem("reset!"));
		access.addItem(getDocItem("swap!"));
		access.addItem(getDocItem("compare-and-set!"));

		return section;
	}

	private DocSection getIOSection() {
		final DocSection section = new DocSection("IO");

		final DocSection all = new DocSection("");
		section.addSection(all);

		final DocSection to = new DocSection("to");
		all.addSection(to);
		to.addItem(getDocItem("print"));
		to.addItem(getDocItem("println"));
		to.addItem(getDocItem("flush"));
		to.addItem(getDocItem("newline"));

		final DocSection to_str = new DocSection("to-str");
		all.addSection(to_str);
		to_str.addItem(getDocItem("pr-str"));

		final DocSection from = new DocSection("from");
		all.addSection(from);
		from.addItem(getDocItem("readline"));
		from.addItem(getDocItem("read-string"));

		final DocSection io = new DocSection("file-io");
		all.addSection(io);
		io.addItem(getDocItem("slurp"));
		io.addItem(getDocItem("spit"));
		io.addItem(getDocItem("io/file"));
		io.addItem(getDocItem("io/file?"));
		io.addItem(getDocItem("io/exists-file?"));
		io.addItem(getDocItem("io/exists-dir?"));
		io.addItem(getDocItem("io/list-files"));
		io.addItem(getDocItem("io/delete-file"));
		io.addItem(getDocItem("io/copy-file"));
		io.addItem(getDocItem("io/tmp-dir"));
		io.addItem(getDocItem("io/user-dir"));

		final DocSection load = new DocSection("load");
		all.addSection(load);
		load.addItem(getDocItem("load-file"));
		load.addItem(getDocItem("load-string"));

		return section;
	}

	private DocSection getSpecialFormsSection() {
		final DocSection section = new DocSection("Special Forms");

		final DocSection all = new DocSection("");
		section.addSection(all);

		final DocSection generic = new DocSection("Forms");
		all.addSection(generic);
		
		generic.addItem(
				new DocItem(
						"def", 
						Arrays.asList("(def name expr)"), 
						"Creates a global variable.",
						runExamples(
							"def", 
							Arrays.asList(
									"(def val 5)")),
						idgen.id()));
		
		generic.addItem(
				new DocItem(
					"if", 
					Arrays.asList("(if test true-expr false-expr)"), 
					"Evaluates test.",
					runExamples(
						"if", 
						Arrays.asList(
								"(if (< 10 20) \"yes\" \"no\")")),
					idgen.id()));
		
		generic.addItem(
				new DocItem(
						"do", 
						Arrays.asList("(do exprs)"), 
						"Evaluates the expressions in order and returns the value of the last.",
						runExamples(
							"do", 
							Arrays.asList(
									"(do (println \"Test...\") (+ 1 1))")),
						idgen.id()));
		
		generic.addItem(
				new DocItem(
						"let", 
						Arrays.asList("(let [bindings*] exprs*)"), 
						"Evaluates the expressions and binds the values to symbols to new local context",
						runExamples(
							"let", 
							Arrays.asList(
									"(let [x 1] x))")),
						idgen.id()));
		
		generic.addItem(
				new DocItem(
						"fn", 
						Arrays.asList("(fn [params*] exprs*)"), 
						"Evaluates test.",
						runExamples(
							"fn", 
							Arrays.asList(
									"(do (def sum (fn [x y] (+ x y))) (sum 2 3))",
									"(map (fn [x] (* 2 x)) (range 1 5))")),
						idgen.id()));
		
		generic.addItem(
				new DocItem(
						"loop", 
						Arrays.asList("(loop [bindings*] exprs*)"), 
						"Evaluates the exprs and binds the bindings. " + 
						"Creates a recursion point with the bindings.",
						runExamples(
							"loop", 
							Arrays.asList(
									"(loop [x 10]\n" + 
									"  (when (> x 1)\n" + 
									"    (println x)\n" + 
									"    (recur (- x 2))))")),
						idgen.id()));
		
		generic.addItem(
				new DocItem(
						"defmacro", 
						Arrays.asList("(macroexpand form)"), 
						"If form represents a macro form, returns its expansion, " + 
						"else returns form",
						runExamples(
							"defmacro", 
							Arrays.asList(
									"(macroexpand '(-> c (+ 3) (* 2)))")),
						idgen.id()));
	    
		generic.addItem(
				new DocItem(
						"recur", 
						Arrays.asList("(recur expr*)"), 
						"Evaluates the exprs and rebinds the bindings of " + 
						"the recursion point to the values of the exprs.",
						runExamples("recur", Arrays.asList()),
						idgen.id()));
		
		generic.addItem(
				new DocItem(
						"try", 
						Arrays.asList(
								"(try (throw))",
								"(try (throw expr))",
								"(try (throw expr) (catch expr))",
								"(try (throw expr) (catch expr) (finally expr))"),
						"Exception handling: try - catch -finally ",
						runExamples(
							"try", 
							Arrays.asList(
									"(try (throw))",
									"(try (throw \"test message\"))",
									"(try (throw 100) (catch (do (+ 1 2) -1)))",
									"(try (throw 100) (finally -2))",
									"(try (throw 100) (catch (do (+ 1 2) -1)) (finally -2))"),
							true),
						idgen.id()));

		return section;
	}

	private DocSection getJavaInteropSection() {
		final DocSection section = new DocSection("Java Interoperability");

		final DocSection all = new DocSection("");
		section.addSection(all);
		
		
		final JavaImports javaImports = new JavaImports();
		final VncFunction javaDot = JavaInteropFn.create(javaImports);
		final VncFunction javaProxify = new JavaInteropProxifyFn(javaImports);
		
		final DocSection general = new DocSection("General");
		all.addSection(general);
		general.addItem(
				new DocItem(
						javaDot.getName(), 
						toStringList(javaDot.getArgLists()), 
						((VncString)javaDot.getDoc()).getValue(),
						runExamples(javaDot.getName(), toStringList(javaDot.getExamples())),
						idgen.id()));
		general.addItem(
				new DocItem(
						javaProxify.getName(), 
						toStringList(javaProxify.getArgLists()), 
						((VncString)javaProxify.getDoc()).getValue(),
						runExamples(javaProxify.getName(), toStringList(javaProxify.getExamples())),
						idgen.id()));
		general.addItem(new DocItem("Constructor: (. classname :new args)", idgen.id()));
		general.addItem(new DocItem("Method call: (. object method args)", idgen.id()));
		general.addItem(new DocItem("Field access: (. object field)", idgen.id()));

		return section;
	}

	private DocSection getMiscellaneousSection() {
		final DocSection section = new DocSection("Miscellaneous");

		final DocSection all = new DocSection("");
		section.addSection(all);
		
		final DocSection general = new DocSection("JSON");
		all.addSection(general);
		general.addItem(new DocItem("", idgen.id()));
		general.addItem(new DocItem("json/pretty-print", Arrays.asList("(json/pretty-print json)"), "Pretty print a JSON string", "", idgen.id()));
		general.addItem(new DocItem("json/to-json", Arrays.asList("(json/to-json val)"), "Convert the value to JSON", "", idgen.id()));
		general.addItem(new DocItem("json/to-pretty-json", Arrays.asList("(json/to-pretty-json val)"), "Convert the value to pretty-printed JSON", "", idgen.id()));
		general.addItem(new DocItem("json/parse", Arrays.asList("(json/parse json)"), "Parse a JSON string", "", idgen.id()));
		general.addItem(new DocItem(" ", idgen.id()));
		general.addItem(new DocItem("json/avail?", Arrays.asList("(json/avail?)"), "Checks if the Jackson JSON libs are on the classpath", "", idgen.id()));
		general.addItem(new DocItem("json/avail-jdk8-module?", Arrays.asList("(json/avail-jdk8-module?)"), "Checks if the Jackson JSON JDK8 module is on the classpath", "", idgen.id()));
		general.addItem(new DocItem(" ", idgen.id()));
		general.addItem(new DocItem("Available if Jackson libs are on runtime classpath", idgen.id()));

		return section;
	}

	private DocItem getDocItem(final String name) {
		if ("()".equals(name)) {
			return new DocItem(
					name, 
					Arrays.asList(""), 
					"Creates a list.",
					runExamples(
							name, 
							Arrays.asList(
							 "'(10 20 30)")),
					idgen.id());
		}

		if ("[]".equals(name)) {
			return new DocItem(
					name, 
					Arrays.asList(""), 
					"Creates a vector",
					runExamples(
							name, 
							Arrays.asList(
							 "[10 20]")),
					idgen.id());
		}

		if ("{}".equals(name)) {
			return new DocItem(
					name, 
					Arrays.asList(""), 
					"Creates a hash map.",
					runExamples(
							name, 
							Arrays.asList(
							 "{:a 10 b: 20}")),
					idgen.id());
		}

		if ("fn".equals(name)) {
			return new DocItem(
					name, 
					Arrays.asList("(fn [params*] expr)"), 
					"Creates a function.",
					runExamples(
							name, 
							Arrays.asList(
							 "(fn [x y] (+ x y))",
							 "(def sum (fn [x y] (+ x y)))")),
					idgen.id());
		}

		if ("eval".equals(name)) {
			return new DocItem(
					name, 
					Arrays.asList("(eval form)"), 
					"Evaluates the form data structure (not text!) and returns the result.",
					runExamples(
							name, 
							Arrays.asList(
							 "(eval '(let [a 10] (+ 3 4 a)))",
							 "(eval (list + 1 2 3))")),
					idgen.id());
		}

		final VncFunction f = getFunction(name);
		if (f != null) {
			return new DocItem(
					name, 
					toStringList(f.getArgLists()), 
					f.getDoc() == Constants.Nil ? "" : ((VncString)f.getDoc()).getValue(),
					runExamples(name, toStringList(f.getExamples())),
					idgen.id());
		}
	
		return null;
	}

	private String runExamples(final String name, final List<String> examples) {
		return runExamples(name, examples, false);
	}

	private String runExamples(final String name, final List<String> examples, final boolean catchEx) {
		final Venice runner = new Venice();

		final StringBuilder sb = new StringBuilder();
		
		try {
			examples
				.stream()
				.filter(e -> !StringUtil.isEmpty(e))
				.map(e -> StringUtil.stripMargin(e, '|'))
				.forEach(e -> {
					try {
						final CapturingPrintStream ps = CapturingPrintStream.create();
						
						final String result = (String)runner.eval(
													"(str " + e + ")",
													Parameters.of("*out*", ps));
						
						if (sb.length() > 0) {
							sb.append("\n\n");
						}
						sb.append(e).append("\n");
						if (!ps.isEmpty()) {
							final String out = ps.getOutput();
							sb.append(out);
							if (!out.endsWith("\n")) sb.append("\n");
						}
						sb.append("=> ").append(result);
					}
					catch(Exception ex) {
						if (catchEx) {
							
							if (sb.length() > 0) {
								sb.append("\n\n");
							}
							sb.append(e)
							  .append("\n")
							  .append("=> ")
							  .append(ex.getClass().getSimpleName())
							  .append(": ")
							  .append(ex.getMessage());
						}
						else {
							throw ex;
						}
					}
				});
			
			return sb.length() == 0 ? null : sb.toString();	
		}
		catch(RuntimeException ex) {
			throw new RuntimeException(String.format("Failed to run examples for %s", name), ex);
		}
	}

	private List<String> toStringList(final VncList list) {
		try {
			return list
					.getList()
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
	


	private final Env env;
	private final IdGen idgen = new IdGen();
}
