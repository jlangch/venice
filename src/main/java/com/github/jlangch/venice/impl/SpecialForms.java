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
package com.github.jlangch.venice.impl;

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.util.Map;

import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;


/**
 * The special form pseudo functions just serve for the cheat-sheet generation 
 * and the 'doc' function!
 */
public class SpecialForms {

	public static VncFunction doc = new SpecialFormsDocFunction("doc") {
		{
			setArgLists("(doc name)");
			setDoc("Prints documentation for a var or special form given its name");
			setExamples("(doc +)");
		}
	};
	
	public static VncFunction list = new SpecialFormsDocFunction("()") {
		{
			setArgLists("");
			setDoc("Creates a list.");
			setExamples("'(10 20 30)");
		}
	};

	public static VncFunction vector = new SpecialFormsDocFunction("[]") {
		{
			setArgLists("");
			setDoc("Creates a vector.");
			setExamples("[10 20 30]");
		}
	};

	public static VncFunction set = new SpecialFormsDocFunction("#{}") {
		{
			setArgLists("");
			setDoc("Creates a set.");
			setExamples("#{10 20 30}");
		}
	};

	public static VncFunction map = new SpecialFormsDocFunction("{}") {
		{
			setArgLists("");
			setDoc("Creates a hash map.");
			setExamples("{:a 10 b: 20}");
		}
	};

	public static VncFunction fn = new SpecialFormsDocFunction("fn") {
		{
			setArgLists("(fn name? [params*] condition-map? expr*)");
			setDoc("Defines an anonymous function.");
			setExamples(
					"(do (def sum (fn [x y] (+ x y))) (sum 2 3))",
					
					"(map (fn double [x] (* 2 x)) (range 1 5))",
					
					"(map #(* 2 %) (range 1 5))",
					
					"(map #(* 2 %1) (range 1 5))",
					
					";; anonymous function with two params, the second is destructured\n" + 
					"(reduce (fn [m [k v]] (assoc m v k)) {} {:b 2 :a 1 :c 3})",
					
					";; defining a pre-condition                 \n" + 
					"(do                                         \n" +
					"   (def sqrt                                \n" +
					"        (fn [x]                             \n" +
					"            { :pre [(>= x 0)] }             \n" +
					"            (. :java.lang.Math :sqrt x)))   \n" +
					"   (sqrt 4))                                  ",
					
					";; higher-order function                                           \n" + 
					"(do                                                                \n" +
					"   (def discount                                                   \n" +
					"        (fn [percentage]                                           \n" +
					"            { :pre [(and (>= percentage 0) (<= percentage 100))] } \n" +
					"            (fn [price] (- price (* price percentage 0.01)))))     \n" +
					"   ((discount 50) 300))                                              ");
		}
	};	

	public static VncFunction eval = new SpecialFormsDocFunction("eval") {
		{
			setArgLists("(eval form)");			
			setDoc("Evaluates the form data structure (not text!) and returns the result.");
			setExamples(
					 "(eval '(let [a 10] (+ 3 4 a)))",
					 "(eval (list + 1 2 3))");
		}
	};

	public static VncFunction def = new SpecialFormsDocFunction("def") {
		{
			setArgLists("(def name expr)");
			setDoc("Creates a global variable.");
			setExamples(
					 "(def val 5)",
					 "(def sum (fn [x y] (+ x y)))");
		}
	};

	public static VncFunction do_ = new SpecialFormsDocFunction("do") {
		{
			setArgLists("(do exprs)");
			setDoc("Evaluates the expressions in order and returns the value of the last.");
			setExamples("(do (println \"Test...\") (+ 1 1))");
		}
	};

	public static VncFunction if_ = new SpecialFormsDocFunction("if") {
		{
			setArgLists("(if test true-expr false-expr)");
			setDoc("Evaluates test.");
			setExamples("(if (< 10 20) \"yes\" \"no\")");
		}
	};

	public static VncFunction let = new SpecialFormsDocFunction("let") {
		{
			setArgLists("(let [bindings*] exprs*)");
			setDoc("Evaluates the expressions and binds the values to symbols to new local context");
			setExamples(
					"(let [x 1] x))",
					
					";; destructured map                     \n" +
					"(let [{:keys [width height title ]      \n" +
					"       :or {width 640 height 500}       \n" +
					"       :as styles}                      \n" +
					"      {:width 1000 :title \"Title\"}]   \n" +
					"     (println \"width: \" width)        \n" +
					"     (println \"height: \" height)      \n" +
					"     (println \"title: \" title)        \n" +
					"     (println \"styles: \" styles))       ");
		}
	};

	public static VncFunction loop = new SpecialFormsDocFunction("loop") {
		{
			setArgLists("(loop [bindings*] exprs*)");
			setDoc( "Evaluates the exprs and binds the bindings. " + 
					"Creates a recursion point with the bindings.");
			setExamples(
					";; tail recursion                                   \n" +
					"(loop [x 10]                                        \n" +
					"   (when (> x 1)                                    \n" +
					"      (println x)                                   \n" +
					"      (recur (- x 2))))                               ",
			
					";; tail recursion                                   \n" +
					"(do                                                 \n" +
					"   (defn sum [n]                                    \n" +
					"         (loop [cnt n acc 0]                        \n" +
					"            (if (zero? cnt)                         \n" +
					"                acc                                 \n" +
					"                (recur (dec cnt) (+ acc cnt)))))    \n" +
					"   (sum 10000))                                       ");
		}
	};

	public static VncFunction recur = new SpecialFormsDocFunction("recur") {
		{
			setArgLists("(recur expr*)");
			setDoc( "Evaluates the exprs and rebinds the bindings of the recursion " + 
					"point to the values of the exprs. The recur expression must be " +
					"at the tail position. The tail position is a postion which an " +
					"expression would return a value from.");
			setExamples(
					";; tail recursion                                   \n" +
					"(loop [x 10]                                        \n" +
					"   (when (> x 1)                                    \n" +
					"      (println x)                                   \n" +
					"      (recur (- x 2))))                               ",
			
					";; tail recursion                                   \n" +
					"(do                                                 \n" +
					"   (defn sum [n]                                    \n" +
					"         (loop [cnt n acc 0]                        \n" +
					"            (if (zero? cnt)                         \n" +
					"                acc                                 \n" +
					"                (recur (dec cnt) (+ acc cnt)))))    \n" +
					"   (sum 10000))                                       ");
		}
	};

	public static VncFunction try_ = new SpecialFormsDocFunction("try") {
		{
			setArgLists(
					"(try expr)",
					"(try expr (catch exClass exSym expr))",
					"(try expr (catch exClass exSym expr) (finally expr))");
			setDoc( "Exception handling: try - catch -finally ");
			setExamples(
					"(try (throw))",
					
					"(try                                      \n" +
					"   (throw \"test message\"))                ",
					
					"(try                                       \n" +
					"   (throw 100)                             \n" +
					"   (catch :java.lang.Exception ex -100))    ",
					
					"(try                                       \n" +
					"   (throw 100)                             \n" +
					"   (finally (println \"...finally\")))       ",
					
					"(try                                       \n" +
					"   (throw 100)                             \n" +
					"   (catch :java.lang.Exception ex -100)    \n" +
					"   (finally (println \"...finally\")))       ",
					
					"(do                                                  \n" +
					"   (import :java.lang.RuntimeException)              \n" +
					"   (try                                              \n" +
					"      (throw (. :RuntimeException :new \"message\")) \n" +
					"      (catch :RuntimeException ex (:message ex))))   \n",
					
					"(do                                                   \n" +
					"   (import :com.github.jlangch.venice.ValueException) \n" +
					"   (try                                               \n" +
					"      (throw [1 2 3])                                 \n" +
					"      (catch :ValueException ex (str (:value ex)))    \n" +
					"      (catch :RuntimeException ex \"runtime ex\")     \n" +
					"      (finally (println \"...finally\"))))             ");
		}
	};

	public static VncFunction try_with = new SpecialFormsDocFunction("try-with") {
		{
			setArgLists(
					"(try-with [bindings*] expr)",
					"(try-with [bindings*] expr (catch :java.lang.Exception ex expr))",
					"(try-with [bindings*] expr (catch :java.lang.Exception ex expr) (finally expr))");
			setDoc( "try-with resources allows the declaration of resources to be used in a try block "
						+ "with the assurance that the resources will be closed after execution "
						+ "of that block. The resources declared must implement the Closeable or ");
			setExamples(
					"(do                                                   \n" +
					"   (import :java.io.FileInputStream)                  \n" +
					"   (let [file (io/temp-file \"test-\", \".txt\")]     \n" +
					"        (io/spit file \"123456789\" :append true)     \n" +
					"        (try-with [is (. :FileInputStream :new file)] \n" +
					"           (io/slurp-stream is :binary false))))        ");
		}
	};

	public static VncFunction defmacro = new SpecialFormsDocFunction("defmacro") {
		{
			setArgLists("(defmacro name [params*] body)");
			setDoc( "Macro definition");
			setExamples(
					"(defmacro unless [pred a b]         \n" + 
							"  `(if (not ~pred) ~a ~b))    ");
		}
	};

	public static VncFunction macroexpand = new SpecialFormsDocFunction("macroexpand") {
		{
			setArgLists("(macroexpand form)");
			setDoc("If form represents a macro form, returns its expansion, else returns form");
			setExamples("(macroexpand (-> c (+ 3) (* 2)))");
		}
	};	
	
	
	private static class SpecialFormsDocFunction extends VncFunction {
		public SpecialFormsDocFunction(final String name) {
			super(name);
		}
		
		public VncVal apply(final VncList args) {
			return Nil;
		}
	};
	
	
	public static Map<VncVal, VncVal> ns = 
			new VncHashMap.Builder()
					.put("doc", 		doc)
					.put("()", 			list)
					.put("[]", 			vector)
					.put("#{}",			set)
					.put("{}", 			map)
					.put("fn", 			fn)
					.put("eval",		eval)
					.put("def",			def)
					.put("do",			do_)
					.put("if",			if_)
					.put("let",			let)
					.put("loop",		loop)
					.put("recur",		recur)
					.put("try",			try_)
					.put("try-with",	try_with)
					.put("defmacro",	defmacro)
					.put("macroexpand",	macroexpand)
					.toMap();
}
