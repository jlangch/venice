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
package com.github.jlangch.venice.impl.docgen;

import java.io.File;
import java.io.FileOutputStream;
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
import com.github.jlangch.venice.impl.SpecialForms;
import com.github.jlangch.venice.impl.VeniceInterpreter;
import com.github.jlangch.venice.impl.javainterop.JavaImports;
import com.github.jlangch.venice.impl.javainterop.JavaInteropFn;
import com.github.jlangch.venice.impl.javainterop.JavaInteropProxifyFn;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.util.CapturingPrintStream;


public class DocGenerator {

	public DocGenerator() {
		this.env = new VeniceInterpreter()
							.createEnv(Arrays.asList("json"))
							.setStdoutPrintStream(null);
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
			
			System.out.println("Generated Cheat Sheet at: " + getUserDir());
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
				getFunctionsSection(),
				getMacrosSection(),
				getSpecialFormsSection());
	}
	
	private List<DocSection> getRightSections() {
		return Arrays.asList(
				getCollectionsSection(),
				getArraysSection(),
				getConcurrencySection(),
				getSystemSection(),
				getJavaInteropSection(),
				getIOSection(),
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

		literals.addItem(new DocItem("Nil: nil", null));
		literals.addItem(new DocItem("Integer: 150I", null));
		literals.addItem(new DocItem("Long: 1500", null));
		literals.addItem(new DocItem("Double: 3.569", null));
		literals.addItem(new DocItem("Boolean: true, false", null));
		literals.addItem(new DocItem("BigDecimal: 6.897M", null));
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
		arithmetic.addItem(getDocItem("negate"));
		arithmetic.addItem(getDocItem("sqrt"));

		final DocSection convert = new DocSection("Convert");
		numbers.addSection(convert);
		convert.addItem(getDocItem("int"));
		convert.addItem(getDocItem("long"));
		convert.addItem(getDocItem("double"));
		convert.addItem(getDocItem("decimal"));

		final DocSection compare = new DocSection("Compare");
		numbers.addSection(compare);
		compare.addItem(getDocItem("=="));
		compare.addItem(getDocItem("!="));
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
		use.addItem(getDocItem("compare"));
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
		use.addItem(getDocItem("str/char"));

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
		keywords.addItem(new DocItem(":a :blue", null));
		keywords.addItem(getDocItem("keyword?"));
		keywords.addItem(getDocItem("keyword"));

		final DocSection symbols = new DocSection("Symbols");
		other.addSection(symbols);
		symbols.addItem(new DocItem("'a 'blue", null));
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
		generic.addItem(getDocItem("compare"));
		generic.addItem(getDocItem("empty-to-nil"));
		generic.addItem(getDocItem("empty"));
		generic.addItem(getDocItem("into"));
		generic.addItem(getDocItem("conj"));
		generic.addItem(getDocItem("remove"));
		generic.addItem(getDocItem("repeat"));
		generic.addItem(getDocItem("repeatedly"));
		generic.addItem(getDocItem("replace"));
		generic.addItem(getDocItem("range"));
		generic.addItem(getDocItem("group-by"));
		generic.addItem(getDocItem("get-in"));
		generic.addItem(getDocItem("seq"));

		final DocSection coll_test = new DocSection("Tests");
		collections.addSection(coll_test);
		coll_test.addItem(getDocItem("empty?"));
		coll_test.addItem(getDocItem("not-empty?"));
		coll_test.addItem(getDocItem("coll?"));
		coll_test.addItem(getDocItem("list?"));
		coll_test.addItem(getDocItem("vector?"));
		coll_test.addItem(getDocItem("set?"));
		coll_test.addItem(getDocItem("sorted-set?"));
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

		final DocSection list_access = new DocSection("Access");
		lists.addSection(list_access);
		list_access.addItem(getDocItem("first"));
		list_access.addItem(getDocItem("second"));
		list_access.addItem(getDocItem("nth"));
		list_access.addItem(getDocItem("last"));
		list_access.addItem(getDocItem("peek"));
		list_access.addItem(getDocItem("rest"));
		list_access.addItem(getDocItem("butlast"));
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
		list_modify.addItem(getDocItem("reverse"));
		list_modify.addItem(getDocItem("sort"));
		list_modify.addItem(getDocItem("sort-by"));
		list_modify.addItem(getDocItem("take"));
		list_modify.addItem(getDocItem("take-while"));
		list_modify.addItem(getDocItem("drop"));
		list_modify.addItem(getDocItem("drop-while"));
		list_modify.addItem(getDocItem("split-with"));
	
		final DocSection list_test = new DocSection("Test");
		lists.addSection(list_test);
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
		vec_access.addItem(getDocItem("nth"));
		vec_access.addItem(getDocItem("last"));
		vec_access.addItem(getDocItem("peek"));
		vec_access.addItem(getDocItem("butlast"));
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
		vec_modify.addItem(getDocItem("split-with"));
			
		final DocSection vec_test = new DocSection("Test");
		vectors.addSection(vec_test);
		vec_test.addItem(getDocItem("contains?"));
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

		final DocSection set_modify = new DocSection("Modify");
		sets.addSection(set_modify);
		set_modify.addItem(getDocItem("cons"));
		set_modify.addItem(getDocItem("conj"));
		set_modify.addItem(getDocItem("disj"));
		set_modify.addItem(getDocItem("difference"));
		set_modify.addItem(getDocItem("union"));
		set_modify.addItem(getDocItem("intersection"));

		final DocSection set_test = new DocSection("Test");
		sets.addSection(set_test);
		set_test.addItem(getDocItem("contains?"));
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

		final DocSection map_modify = new DocSection("Modify");
		maps.addSection(map_modify);
		map_modify.addItem(getDocItem("cons"));
		map_modify.addItem(getDocItem("conj"));
		map_modify.addItem(getDocItem("assoc"));
		map_modify.addItem(getDocItem("assoc!"));
		map_modify.addItem(getDocItem("assoc-in"));
		map_modify.addItem(getDocItem("get-in"));
		map_modify.addItem(getDocItem("update"));
		map_modify.addItem(getDocItem("update!"));
		map_modify.addItem(getDocItem("dissoc"));
		map_modify.addItem(getDocItem("dissoc!"));
		map_modify.addItem(getDocItem("into"));
		map_modify.addItem(getDocItem("concat"));
		map_modify.addItem(getDocItem("flatten"));
		map_modify.addItem(getDocItem("reduce-kv"));
		map_modify.addItem(getDocItem("merge"));
		map_modify.addItem(getDocItem("map-invert"));
		
		final DocSection map_test = new DocSection("Test");
		maps.addSection(map_test);
		map_test.addItem(getDocItem("contains?"));

		return section;
	}		

	private DocSection getArraysSection() {
		final DocSection section = new DocSection("Arrays");

		final DocSection all = new DocSection("");
		section.addSection(all);

		final DocSection create = new DocSection("Create");
		all.addSection(create);
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

		return section;
	}
	
	private DocSection getFunctionsSection() {
		final DocSection section = new DocSection("Functions");

		final DocSection all = new DocSection("");
		section.addSection(all);

		final DocSection create = new DocSection("Create");
		all.addSection(create);
		create.addItem(getDocItem("fn"));
		create.addItem(getDocItem("defn"));
		create.addItem(getDocItem("identity"));
		create.addItem(getDocItem("comp"));
		create.addItem(getDocItem("partial"));
		create.addItem(getDocItem("memoize"));

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
		misc.addItem(getDocItem("doc"));
		misc.addItem(getDocItem("type"));
		misc.addItem(getDocItem("instance?"));
		misc.addItem(getDocItem("eval"));
		misc.addItem(getDocItem("name"));
		misc.addItem(getDocItem("callstack"));


		final DocSection meta = new DocSection("Meta");
		all.addSection(meta);
		meta.addItem(getDocItem("meta"));
		meta.addItem(getDocItem("with-meta"));
		meta.addItem(getDocItem("vary-meta"));
		
		return section;
	}


	private DocSection getSystemSection() {
		final DocSection section = new DocSection("System");

		final DocSection all = new DocSection("");
		section.addSection(all);

		final DocSection test = new DocSection("Test");
		all.addSection(test);
		test.addItem(getDocItem("sandboxed?"));
		test.addItem(getDocItem("os-type?"));

		final DocSection util = new DocSection("Other");
		all.addSection(util);
		util.addItem(getDocItem("version"));
		util.addItem(getDocItem("os-type"));
		util.addItem(getDocItem("system-prop"));
		util.addItem(getDocItem("java-version"));
		util.addItem(getDocItem("uuid"));
		util.addItem(getDocItem("sleep"));
		util.addItem(getDocItem("gc"));
		util.addItem(getDocItem("current-time-millis"));
		util.addItem(getDocItem("nano-time"));
		util.addItem(getDocItem("coalesce"));

		final DocSection shell = new DocSection("Shell");
		all.addSection(shell);
		shell.addItem(getDocItem("sh", false, false));
		shell.addItem(getDocItem("with-sh-dir", false, false));
		shell.addItem(getDocItem("with-sh-env", false, false));
		shell.addItem(getDocItem("with-sh-throw", false, false));
				
		return section;
	}

	private DocSection getMacrosSection() {
		final DocSection section = new DocSection("Macros");

		final DocSection all = new DocSection("");
		section.addSection(all);

		final DocSection create = new DocSection("Create");
		all.addSection(create);		
		create.addItem(getDocItem("defn", false, false));
		create.addItem(getDocItem("defmacro"));
		create.addItem(getDocItem("macroexpand"));

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
		call.addItem(getDocItem("-<>"));
		call.addItem(getDocItem("as->"));

		final DocSection loading = new DocSection("Loading");
		all.addSection(loading);
		loading.addItem(getDocItem("load-string"));
		loading.addItem(getDocItem("load-file"));
		loading.addItem(getDocItem("load-module"));
		loading.addItem(getDocItem("load-classpath-file"));
		
		final DocSection test = new DocSection("Test");
		all.addSection(test);
		test.addItem(getDocItem("macro?"));
		test.addItem(getDocItem("cond"));
		test.addItem(getDocItem("case"));

		final DocSection assert_ = new DocSection("Assert");
		all.addSection(assert_);
		assert_.addItem(getDocItem("assert"));

		final DocSection util = new DocSection("Util");
		all.addSection(util);
		util.addItem(getDocItem("comment"));
		util.addItem(getDocItem("gensym"));
		util.addItem(getDocItem("time"));

		final DocSection profil = new DocSection("Profiling");
		all.addSection(profil);
		profil.addItem(getDocItem("time"));
		profil.addItem(getDocItem("perf", false, false));
		
		return section;
	}

	private DocSection getConcurrencySection() {
		final DocSection section = new DocSection("Concurrency");

		final DocSection all = new DocSection("");
		section.addSection(all);

		final DocSection atoms = new DocSection("Atoms");
		all.addSection(atoms);
		atoms.addItem(getDocItem("atom"));
		atoms.addItem(getDocItem("atom?"));
		atoms.addItem(getDocItem("deref"));
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
		futures.addItem(getDocItem("deref"));
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
		agents.addItem(getDocItem("shutdown-agents", false, false));
		agents.addItem(getDocItem("shutdown-agents?", false, false));
		agents.addItem(getDocItem("await-termination-agents", false, false));
		agents.addItem(getDocItem("await-termination-agents?", false, false));
		
		final DocSection thlocal = new DocSection("ThreadLocal");
		all.addSection(thlocal);
		thlocal.addItem(getDocItem("thread-local"));
		thlocal.addItem(getDocItem("thread-local?"));
		thlocal.addItem(getDocItem("thread-local-clear"));
		thlocal.addItem(getDocItem("assoc"));
		thlocal.addItem(getDocItem("dissoc"));
		thlocal.addItem(getDocItem("get"));

		final DocSection util = new DocSection("Util");
		all.addSection(util);
		util.addItem(getDocItem("thread-id"));
		util.addItem(getDocItem("thread-name"));

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
		to.addItem(getDocItem("printf"));
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
		io.addItem(getDocItem("io/file"));
		io.addItem(getDocItem("io/file?"));
		io.addItem(getDocItem("io/file-parent"));
		io.addItem(getDocItem("io/file-name"));
		io.addItem(getDocItem("io/file-path"));
		io.addItem(getDocItem("io/exists-file?"));
		io.addItem(getDocItem("io/exists-dir?"));
		io.addItem(getDocItem("io/list-files"));
		io.addItem(getDocItem("io/file-size"));
		io.addItem(getDocItem("io/delete-file"));
		io.addItem(getDocItem("io/delete-file-on-exit"));
		io.addItem(getDocItem("io/copy-file"));
		io.addItem(getDocItem("io/move-file"));
		io.addItem(getDocItem("io/mkdir"));
		io.addItem(getDocItem("io/mkdirs"));
		io.addItem(getDocItem("io/slurp"));
		io.addItem(getDocItem("io/slurp-lines"));
		io.addItem(getDocItem("io/spit"));
		io.addItem(getDocItem("io/tmp-dir"));
		io.addItem(getDocItem("io/user-dir"));

		final DocSection stream = new DocSection("stream-io");
		all.addSection(stream);
		stream.addItem(getDocItem("io/slurp-stream"));
		stream.addItem(getDocItem("io/spit-stream"));

		final DocSection io_tmp = new DocSection("file-io temp");
		all.addSection(io_tmp);
		io_tmp.addItem(getDocItem("io/temp-file"));
		io_tmp.addItem(getDocItem("io/slurp-temp-file"));
		io_tmp.addItem(getDocItem("io/spit-temp-file"));

		final DocSection other = new DocSection("other");
		all.addSection(other);
		other.addItem(getDocItem("io/load-classpath-resource"));
		other.addItem(getDocItem("io/mime-type"));
		other.addItem(getDocItem("io/default-charset"));
		other.addItem(getDocItem("with-out-str"));

		return section;
	}
	
	private DocSection getByteBufSection() {
		final DocSection section = new DocSection("Byte Buffer");

		final DocSection all = new DocSection("");
		section.addSection(all);

		final DocSection bb_create = new DocSection("Create");
		all.addSection(bb_create);
		bb_create.addItem(getDocItem("bytebuf"));
		bb_create.addItem(getDocItem("bytebuf-from-string"));
		
		final DocSection bb_test = new DocSection("Test");
		all.addSection(bb_test);
		bb_test.addItem(getDocItem("empty?"));
		bb_test.addItem(getDocItem("not-empty?"));
		bb_test.addItem(getDocItem("bytebuf?"));

		final DocSection bb_use = new DocSection("Use");
		all.addSection(bb_use);
		bb_use.addItem(getDocItem("count"));
		bb_use.addItem(getDocItem("bytebuf-to-string"));
		bb_use.addItem(getDocItem("bytebuf-sub"));

		return section;
	}

	private DocSection getTimeSection() {
		final DocSection section = new DocSection("Time");

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
		fields.addItem(getDocItem("time/hour"));
		fields.addItem(getDocItem("time/minute"));
		fields.addItem(getDocItem("time/second"));
		fields.addItem(getDocItem("time/length-of-year"));
		fields.addItem(getDocItem("time/length-of-month"));
		fields.addItem(getDocItem("time/day-of-week"));
		fields.addItem(getDocItem("time/day-of-month"));
		fields.addItem(getDocItem("time/day-of-year"));
		fields.addItem(getDocItem("time/zone"));
		fields.addItem(getDocItem("time/zone-offset"));

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
		misc.addItem(getDocItem("time/first-day-of-month"));
		misc.addItem(getDocItem("time/last-day-of-month"));
		misc.addItem(getDocItem("time/earliest"));
		misc.addItem(getDocItem("time/latest"));

		final DocSection util = new DocSection("Util");
		all.addSection(util);
		util.addItem(getDocItem("time/zone-ids"));
		util.addItem(getDocItem("time/to-millis"));

		return section;
	}

	private DocSection getSpecialFormsSection() {
		final DocSection section = new DocSection("Special Forms");

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
		generic.addItem(getDocItem("loop"));
		generic.addItem(getDocItem("recur"));
		generic.addItem(getDocItem("try", true, true));   // FIXME: try
		generic.addItem(getDocItem("try-with", true, true));

		final DocSection profiling = new DocSection("Profiling");
		all.addSection(profiling);

		profiling.addItem(getDocItem("dorun"));
		profiling.addItem(getDocItem("prof"));

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
						runExamples(javaDot.getName(), toStringList(javaDot.getExamples()), true, false),
						idgen.id()));
		general.addItem(
				new DocItem(
						javaProxify.getName(), 
						toStringList(javaProxify.getArgLists()), 
						((VncString)javaProxify.getDoc()).getValue(),
						runExamples(javaProxify.getName(), toStringList(javaProxify.getExamples()), true, false),
						idgen.id()));
		general.addItem(new DocItem(" ", null));
		general.addItem(new DocItem("Invoke constructors", null));
		general.addItem(new DocItem("Invoke static or instance methods", null));
		general.addItem(new DocItem("Access static or instance fields", null));

		return section;
	}

	private DocSection getMiscellaneousSection() {
		final DocSection section = new DocSection("Miscellaneous");

		final DocSection all = new DocSection("");
		section.addSection(all);
		
		final DocSection general = new DocSection("JSON");
		all.addSection(general);
		general.addItem(getDocItem("json/pretty-print", false, false));
		general.addItem(getDocItem("json/to-json", false, false));
		general.addItem(getDocItem("json/to-pretty-json", false, false));
		general.addItem(getDocItem("json/parse", false, false));
		general.addItem(new DocItem(" ", null));
		general.addItem(getDocItem("json/avail?", false, false));
		general.addItem(getDocItem("json/avail-jdk8-module?", false, false));
		general.addItem(new DocItem(" ", null));
		general.addItem(new DocItem("Available if Jackson libs are on runtime classpath", null));

		return section;
	}

	private DocItem getDocItem(final String name) {
		return getDocItem(name, true, false);
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
		// Special forms
		VncFunction fn = (VncFunction)SpecialForms.ns.get(new VncSymbol(name));
		if (fn == null) {
			// functions & macros
			fn = getFunction(name);
		}
		
		if (fn != null) {
			return new DocItem(
					name, 
					toStringList(fn.getArgLists()), 
					fn.getDoc() == Constants.Nil ? "" : ((VncString)fn.getDoc()).getValue(),
					runExamples(name, toStringList(fn.getExamples()), runExamples, catchEx),
					idgen.id());
		}
	
		return null;
	}

	private String runExamples(
			final String name, 
			final List<String> examples, 
			final boolean run,
			final boolean catchEx
	) {
		final Venice runner = new Venice();

		final StringBuilder sb = new StringBuilder();
		
		try {
			examples
				.stream()
				.filter(e -> !StringUtil.isEmpty(e))
				.forEach(e -> {
					if (run) {
						final CapturingPrintStream ps = CapturingPrintStream.create();
						
						try {
							final String result = (String)runner.eval(
														"example",
														"(pr-str " + e + ")",
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
								sb.append(e);
								sb.append("\n");
								if (!ps.isEmpty()) {
									final String out = ps.getOutput();
									sb.append(out);
									if (!out.endsWith("\n")) sb.append("\n");
								}
								sb.append("=> ")
								  .append(ex.getClass().getSimpleName())
								  .append(": ")
								  .append(ex.getMessage());
							}
							else {
								throw ex;
							}
						}
					}
					else {
						if (sb.length() > 0) {
							sb.append("\n\n");
						}
						sb.append(e);
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
	


	private final Map<String, DocItem> docItems = new HashMap<>();
	private final Env env;
	private final IdGen idgen = new IdGen();
}
