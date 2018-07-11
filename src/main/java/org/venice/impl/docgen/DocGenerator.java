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
package org.venice.impl.docgen;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.venice.Venice;
import org.venice.Version;
import org.venice.impl.CoreFunctions;
import org.venice.impl.CoreMacroDefs;
import org.venice.impl.MacroDef;
import org.venice.impl.javainterop.JavaImports;
import org.venice.impl.javainterop.JavaInteropFn;
import org.venice.impl.types.VncFunction;
import org.venice.impl.types.VncString;
import org.venice.impl.types.VncSymbol;
import org.venice.impl.types.collections.VncList;
import org.venice.impl.util.ClassPathResource;
import org.venice.impl.util.StringUtil;


public class DocGenerator {

	public DocGenerator() {
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
			data.put("snippets", getCodeSnippet());
			
			final String html = HtmlRenderer.renderCheatSheet(data);
			
			final File file = new File(getUserDir(), "cheatsheet.html");
			save(file, html);
			
			System.out.println("Genereated Cheat Sheet: " + file.getPath());
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
				getSpecialFormsSection());
	}
	
	private List<DocSection> getRightSections() {
		return Arrays.asList(
				getCollectionsSection(),
				getAtomsSection(),
				getIOSection(),
				getJavaInteropSection());
	}

	private DocSection getPrimitivesSection() {
		final DocSection section = new DocSection("Primitives");

		final DocSection numbers = new DocSection("Numbers");
		section.addSection(numbers);
		
		final DocSection literals = new DocSection("Literals");
		numbers.addSection(literals);
		literals.addItem(new DocItem("Nil: nil"));
		literals.addItem(new DocItem("Long: 1500"));
		literals.addItem(new DocItem("Double: 3.569"));
		literals.addItem(new DocItem("Boolean: true, false"));
		literals.addItem(new DocItem("BigDecimal: 6.897M"));
		literals.addItem(new DocItem("String: \"abcde\""));

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
		use.addItem(getDocItem("str/lines"));

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
		str_test.addItem(getDocItem("str/starts-with?"));
		str_test.addItem(getDocItem("str/ends-with?"));
		str_test.addItem(getDocItem("str/contains?"));

		
		final DocSection other = new DocSection("Other");
		section.addSection(other);

		final DocSection keywords = new DocSection("Keywords");
		other.addSection(keywords);
		keywords.addItem(getDocItem("keyword?"));
		keywords.addItem(getDocItem("keyword"));
		keywords.addItem(new DocItem("literals: :a :xyz"));

		final DocSection symbols = new DocSection("Symbols");
		other.addSection(symbols);
		symbols.addItem(getDocItem("symbol?"));
		symbols.addItem(getDocItem("symbol"));

		final DocSection boolean_ = new DocSection("Boolean");
		other.addSection(boolean_);
		boolean_.addItem(getDocItem("boolean?"));
		boolean_.addItem(getDocItem("boolean"));
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
		generic.addItem(getDocItem("into"));
		generic.addItem(getDocItem("conj"));
		generic.addItem(getDocItem("remove"));
		generic.addItem(getDocItem("repeat"));
		generic.addItem(getDocItem("range"));
		generic.addItem(getDocItem("group-by"));

		final DocSection coll_test = new DocSection("Tests");
		collections.addSection(coll_test);
		coll_test.addItem(getDocItem("coll?"));
		coll_test.addItem(getDocItem("list?"));
		coll_test.addItem(getDocItem("vector?"));
		coll_test.addItem(getDocItem("set?"));
		coll_test.addItem(getDocItem("map?"));
		coll_test.addItem(getDocItem("seq?"));
		coll_test.addItem(getDocItem("hash-map?"));
		coll_test.addItem(getDocItem("ordered-map?"));
		coll_test.addItem(getDocItem("sorted-map?"));

		
		
		final DocSection lists = new DocSection("Lists");
		section.addSection(lists);

		final DocSection list_create = new DocSection("Create");
		lists.addSection(list_create);
		list_create.addItem(new DocItem("()"));
		list_create.addItem(getDocItem("list"));

		final DocSection list_access = new DocSection("Access");
		lists.addSection(list_access);
		list_access.addItem(getDocItem("first"));
		list_access.addItem(getDocItem("second"));
		list_access.addItem(getDocItem("nth"));
		list_access.addItem(getDocItem("last"));
		list_access.addItem(getDocItem("peek"));
		list_access.addItem(getDocItem("rest"));

		final DocSection list_modify = new DocSection("Modify");
		lists.addSection(list_modify);
		list_modify.addItem(getDocItem("cons"));
		list_modify.addItem(getDocItem("conj"));
		list_modify.addItem(getDocItem("rest"));
		list_modify.addItem(getDocItem("pop"));
		list_modify.addItem(getDocItem("into"));
		list_modify.addItem(getDocItem("concat"));
		list_modify.addItem(getDocItem("mpacat"));
		list_modify.addItem(getDocItem("flatten"));
		list_modify.addItem(getDocItem("reduce"));
		list_modify.addItem(getDocItem("reverse"));
		list_modify.addItem(getDocItem("sort"));
		list_modify.addItem(getDocItem("sort-by"));
		list_modify.addItem(getDocItem("take"));
		list_modify.addItem(getDocItem("take-while"));
		list_modify.addItem(getDocItem("drop"));
		list_modify.addItem(getDocItem("drop-while"));
	
		
		
		final DocSection vectors = new DocSection("Vectors");
		section.addSection(vectors);

		final DocSection vec_create = new DocSection("Create");
		vectors.addSection(vec_create);
		vec_create.addItem(new DocItem("[]"));
		vec_create.addItem(getDocItem("vector"));

		final DocSection vec_access = new DocSection("Access");
		vectors.addSection(vec_access);
		vec_access.addItem(getDocItem("first"));
		vec_access.addItem(getDocItem("second"));
		vec_access.addItem(getDocItem("nth"));
		vec_access.addItem(getDocItem("last"));
		vec_access.addItem(getDocItem("peek"));
		vec_access.addItem(getDocItem("rest"));

		final DocSection vec_modify = new DocSection("Modify");
		vectors.addSection(vec_modify);
		vec_modify.addItem(getDocItem("cons"));
		vec_modify.addItem(getDocItem("conj"));
		vec_modify.addItem(getDocItem("rest"));
		vec_modify.addItem(getDocItem("pop"));
		vec_modify.addItem(getDocItem("into"));
		vec_modify.addItem(getDocItem("concat"));
		vec_modify.addItem(getDocItem("mpacat"));
		vec_modify.addItem(getDocItem("flatten"));
		vec_modify.addItem(getDocItem("reduce"));
		vec_modify.addItem(getDocItem("reverse"));
		vec_modify.addItem(getDocItem("sort"));
		vec_modify.addItem(getDocItem("sort-by"));
		vec_modify.addItem(getDocItem("take"));
		vec_modify.addItem(getDocItem("take-while"));
		vec_modify.addItem(getDocItem("drop"));
		vec_modify.addItem(getDocItem("drop-while"));
		
		final DocSection vec_test = new DocSection("Test");
		vectors.addSection(vec_test);
		vec_test.addItem(getDocItem("contains?"));
	
		final DocSection sets = new DocSection("Sets");
		section.addSection(sets);

		final DocSection set_create = new DocSection("Create");
		sets.addSection(set_create);
		set_create.addItem(getDocItem("set"));
		
		final DocSection set_test = new DocSection("Test");
		sets.addSection(set_test);
		set_test.addItem(getDocItem("contains?"));

		
		final DocSection maps = new DocSection("Maps");
		section.addSection(maps);

		final DocSection maps_create = new DocSection("Create");
		maps.addSection(maps_create);
		maps_create.addItem(new DocItem("{}"));
		maps_create.addItem(getDocItem("hash-map"));
		maps_create.addItem(getDocItem("ordered-map"));
		maps_create.addItem(getDocItem("sorted-map"));

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
		map_modify.addItem(getDocItem("disassoc"));
		map_modify.addItem(getDocItem("into"));
		map_modify.addItem(getDocItem("concat"));
		map_modify.addItem(getDocItem("flatten"));
		map_modify.addItem(getDocItem("reduce-kv"));
		
		final DocSection map_test = new DocSection("Test");
		maps.addSection(map_test);
		map_test.addItem(getDocItem("contains?"));

		return section;
	}

	private DocSection getFunctionsSection() {
		final DocSection section = new DocSection("Functions");

		final DocSection all = new DocSection("");
		section.addSection(all);

		final DocSection create = new DocSection("Create");
		all.addSection(create);
		create.addItem(new DocItem("fn"));


		final DocSection call = new DocSection("Call");
		all.addSection(call);
		call.addItem(getDocItem("apply"));


		final DocSection test = new DocSection("Test");
		all.addSection(test);
		test.addItem(getDocItem("fn?"));


		final DocSection ex = new DocSection("Exception");
		all.addSection(ex);
		ex.addItem(getDocItem("throw"));


		final DocSection util = new DocSection("Other");
		all.addSection(util);
		util.addItem(getDocItem("uuid"));
		util.addItem(getDocItem("time-ms"));
		util.addItem(getDocItem("time-ns"));
		util.addItem(getDocItem("eval"));
		util.addItem(getDocItem("class"));


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
		create.addItem(new DocItem("defmacro"));


		final DocSection branch = new DocSection("Branch");
		all.addSection(branch);
		branch.addItem(getDocItem("and"));
		branch.addItem(getDocItem("or"));
		branch.addItem(getDocItem("not"));
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
		to.addItem(getDocItem("prn"));
		to.addItem(getDocItem("println"));

		final DocSection to_str = new DocSection("to-str");
		all.addSection(to_str);
		to_str.addItem(getDocItem("pr-str"));

		final DocSection from = new DocSection("from");
		all.addSection(from);
		from.addItem(getDocItem("readline"));
		from.addItem(getDocItem("read-string"));
		from.addItem(getDocItem("slurp"));

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
									"(def val 5)"))));
		
		generic.addItem(
				new DocItem(
					"if", 
					Arrays.asList("(if test true-expr false-expr)"), 
					"Evaluates test.",
					runExamples(
						"if", 
						Arrays.asList(
								"(if (< 10 20) \"yes\" \"no\")"))));
		
		generic.addItem(
				new DocItem(
						"do", 
						Arrays.asList("(do exprs)"), 
						"Evaluates the expressions in order and returns the value of the last.",
						runExamples(
							"do", 
							Arrays.asList(
									"(do (println \"Test...\") (+ 1 1))"))));
		
		generic.addItem(
				new DocItem(
						"let", 
						Arrays.asList("(let [bindings*] exprs*)"), 
						"Evaluates the expressions and binds the values to symbols to new local context",
						runExamples(
							"let", 
							Arrays.asList(
									"(let [x 1] x))"))));
		
		generic.addItem(
				new DocItem(
						"fn", 
						Arrays.asList("(fn [params*] exprs*)"), 
						"Evaluates test.",
						runExamples(
							"fn", 
							Arrays.asList(
									"(do (def sum (fn [x y] (+ x y))) (sum 2 3))",
									"(map (fn [x] (* 2 x)) (range 1 5))"))));
		
		generic.addItem(
				new DocItem(
						"loop", 
						Arrays.asList("(loop [bindings*] exprs*)"), 
						"Evaluates the exprs and binds the bindings. " + 
						"Creates a recursion point with the bindings.",
						runExamples(
							"loop", 
							Arrays.asList(
									"(if (< 10 20) \"yes\" \"no\")"))));
		
		generic.addItem(
				new DocItem(
						"recur", 
						Arrays.asList("(recur expr*)"), 
						"Evaluates the exprs and rebinds the bindings of " + 
						"the recursion point to the values of the exprs.",
						runExamples(
							"recur", 
							Arrays.asList(
									"(if (< 10 20) \"yes\" \"no\")"))));
		
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
							true)));

		return section;
	}

	private DocSection getJavaInteropSection() {
		final DocSection section = new DocSection("Java Interoperabilty");

		final DocSection all = new DocSection("");
		section.addSection(all);

		final JavaInteropFn fn = JavaInteropFn.create(new JavaImports());
		
		final DocSection general = new DocSection("General");
		all.addSection(general);
		general.addItem(
				new DocItem(
						fn.getName(), 
						toStringList(fn.getArgLists()), 
						((VncString)fn.getDescription()).getValue(),
						runExamples(fn.getName(), toStringList(fn.getExamples()))));
		general.addItem(new DocItem("Constructor: (. classname :new args)"));
		general.addItem(new DocItem("Method call: (. object method args)"));

		return section;
	}

	private DocItem getDocItem(final String name) {
		final VncFunction f = (VncFunction)CoreFunctions.ns.get(new VncSymbol(name));
		if (f != null) {
			return new DocItem(
					name, 
					toStringList(f.getArgLists()), 
					((VncString)f.getDescription()).getValue(),
					runExamples(name, toStringList(f.getExamples())));
		}
		
		final MacroDef m = CoreMacroDefs
							.getMacros()
							.stream()
							.filter(n -> n.getName().equals(name))
							.findFirst()
							.orElse(null);
		if (m != null) {
			return new DocItem(
					name, 
					m.getSignatures(), 
					m.getDescription(),
					runExamples(name, m.getExamples()));
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
				.forEach(e -> {
					try {
						final String result = (String)runner.eval("(str " + e + ")");
						
						if (sb.length() > 0) {
							sb.append("\n\n");
						}
						sb.append(e).append("\n").append("=> ").append(result);
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
		return list
				.getList()
				.stream()
				.map(s -> ((VncString)s).getValue())
				.collect(Collectors.toList());
	}

	private List<DocSection> concat(final List<DocSection> l1, final List<DocSection> l2) {
		final List<DocSection> list = new ArrayList<>();
		list.addAll(l1);
		list.addAll(l2);
		return list;
	}
	
	private void save(final File file, final String text) throws Exception {
		try(FileOutputStream fos = new FileOutputStream(file)) {
			final byte[] data = text.getBytes("UTF-8");
			fos.write(data, 0, data.length);					
			fos.flush();
		}
	}
	
	private File getUserDir() {
		return new File(System.getProperty("user.dir"));
	}
		
	private List<CodeSnippet> getCodeSnippet() {
		try {
			final List<String> lines = 
					StringUtil.splitIntoLines(
									new ClassPathResource(
											"org/venice/impl/docgen/cheatsheet.snippets")
							  .getResourceAsString("UTF-8"));
			
			final List<Integer> snippetStartLines = new ArrayList<>();			
			for(int ii=0; ii<lines.size(); ii++) {
				if (lines.get(ii).startsWith("**")) {
					snippetStartLines.add(ii);
				}
			}
			snippetStartLines.add(lines.size());
			
			final List<CodeSnippet> snippets = new ArrayList<>(); 
			for(int ii=0; ii<snippetStartLines.size()-1; ii++) {
				snippets.add(
						new CodeSnippet(
								lines.get(snippetStartLines.get(ii)).substring(2),
								lines
									.subList(snippetStartLines.get(ii)+1, snippetStartLines.get(ii+1))
									.stream()
									.collect(Collectors.joining("\n"))
									.trim()));
			}

			return snippets;
		}
		catch(Exception ex) {
			throw new RuntimeException("Failed to load code snippets", ex);
		}
	}
}
