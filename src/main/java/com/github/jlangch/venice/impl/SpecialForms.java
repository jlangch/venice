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

	public static VncFunction doc = 
		new SpecialFormsDocFunction(
				"doc",
				VncFunction
					.meta()
					.arglists("(doc name)")		
					.doc("Prints documentation for a var or special form given its name")
					.examples("(doc +)")
					.build()
	) {
	    private static final long serialVersionUID = -1;
	};
	
	public static VncFunction list = 
		new SpecialFormsDocFunction(
				"()",
				VncFunction
				.meta()
				.arglists("")		
				.doc("Creates a list.")
				.examples("'(10 20 30)")
				.build()
	) {
	    private static final long serialVersionUID = -1;
	};

	public static VncFunction vector = 
		new SpecialFormsDocFunction(
				"[]",
				VncFunction
				.meta()
				.arglists("")		
				.doc("Creates a vector.")
				.examples("[10 20 30]")
				.build()
	) {
	    private static final long serialVersionUID = -1;
	};

	public static VncFunction set = 
		new SpecialFormsDocFunction(
				"#{}",
				VncFunction
				.meta()
				.arglists("")		
				.doc("Creates a set.")
				.examples("#{10 20 30}")
				.build()
	) {
	    private static final long serialVersionUID = -1;
	};

	public static VncFunction map = 
		new SpecialFormsDocFunction(
				"{}",
				VncFunction
				.meta()
				.arglists("")		
				.doc("Creates a hash map.")
				.examples("{:a 10 :b 20}")
				.build()
	) {
	    private static final long serialVersionUID = -1;
	};

	public static VncFunction resolve = 
		new SpecialFormsDocFunction(
				"resolve",
				VncFunction
				.meta()
				.arglists("")		
				.doc("Resolves a symbiol.")
				.examples("(resolve '+)", "(resolve (symbol \"+\"))")
				.build()
	) {
	    private static final long serialVersionUID = -1;
	};

	public static VncFunction fn = 
		new SpecialFormsDocFunction(
				"fn",
				VncFunction
				.meta()
				.arglists("(fn name? [params*] condition-map? expr*)")		
				.doc("Defines an anonymous function.")
				.examples(
					"(do (def sum (fn [x y] (+ x y))) (sum 2 3))",
					
					"(map (fn double [x] (* 2 x)) (range 1 5))",
					
					"(map #(* 2 %) (range 1 5))",
					
					"(map #(* 2 %1) (range 1 5))",
					
					";; anonymous function with two params, the second is destructured\n" + 
					"(reduce (fn [m [k v]] (assoc m v k)) {} {:b 2 :a 1 :c 3})",
					
					";; defining a pre-condition                 \n" + 
					"(do                                         \n" +
					"   (def square-root                         \n" +
					"        (fn [x]                             \n" +
					"            { :pre [(>= x 0)] }             \n" +
					"            (. :java.lang.Math :sqrt x)))   \n" +
					"   (square-root 4))                           ",
					
					";; higher-order function                                           \n" + 
					"(do                                                                \n" +
					"   (def discount                                                   \n" +
					"        (fn [percentage]                                           \n" +
					"            { :pre [(and (>= percentage 0) (<= percentage 100))] } \n" +
					"            (fn [price] (- price (* price percentage 0.01)))))     \n" +
					"   ((discount 50) 300))                                              ")
				.build()
	) {
	    private static final long serialVersionUID = -1;
	};

	public static VncFunction eval = 
		new SpecialFormsDocFunction(
				"eval",
				VncFunction
				.meta()
				.arglists("(eval form)")	
				.doc("Evaluates the form data structure (not text!) and returns the result.")
				.examples(
					"(eval '(let [a 10] (+ 3 4 a)))",
					"(eval (list + 1 2 3))",
				 	"(let [s \"(+ 2 x)\" x 10]     \n" +
				 	"   (eval (read-string s))))     ")
				.build()
	) {
	    private static final long serialVersionUID = -1;
	};

	public static VncFunction def = 
		new SpecialFormsDocFunction(
				"def",
				VncFunction
				.meta()
				.arglists("(def name expr)")		
				.doc("Creates a global variable.")
				.examples(
					 "(def x 5)",
					 "(def sum (fn [x y] (+ x y)))")
				.build()
	) {
	    private static final long serialVersionUID = -1;
	};

	public static VncFunction defonce = 
		new SpecialFormsDocFunction(
				"defonce",
				VncFunction
				.meta()
				.arglists("(defonce name expr)")		
				.doc("Creates a global variable that can not be overwritten")
				.examples("(defonce x 5)")
				.build()
	) {
	    private static final long serialVersionUID = -1;
	};

	public static VncFunction defmulti = 
		new SpecialFormsDocFunction(
				"defmulti",
				VncFunction
				.meta()
				.arglists("(defmulti name dispatch-fn)")		
				.doc("Creates a new multimethod with the associated dispatch function.")
				.examples(
					"(do                                                                       \n" +
					"   ;;defmulti with dispatch function                                      \n" +
					"   (defmulti salary (fn[amount] (amount :t)))                             \n" +
					"                                                                          \n" +
					"   ;;defmethod provides a function implementation for a particular value  \n" +
					"   (defmethod salary \"com\" [amount] (+ (:b amount) (/ (:b amount) 2)))  \n" +
					"   (defmethod salary \"bon\" [amount] (+ (:b amount) 99))                 \n" +
					"   (defmethod salary :default  [amount] (:b amount))                      \n" +
					"                                                                          \n" +
					"   [(salary {:t \"com\" :b 1000})                                         \n" +
					"    (salary {:t \"bon\" :b 1000})                                         \n" +
					"    (salary {:t \"xxx\" :b 1000})]                                        \n" +
					")                                                                           ")
				.build()
	) {
	    private static final long serialVersionUID = -1;
	};

	public static VncFunction defmethod = 
		new SpecialFormsDocFunction(
				"defmethod",
				VncFunction
				.meta()
				.arglists("(defmethod multifn-name dispatch-val & fn-tail)")		
				.doc("Creates a new method for a multimethod associated with a dispatch-value.")
				.examples(
						"(do                                                                       \n" +
						"   ;;defmulti with dispatch function                                      \n" +
						"   (defmulti salary (fn[amount] (amount :t)))                             \n" +
						"                                                                          \n" +
						"   ;;defmethod provides a function implementation for a particular value  \n" +
						"   (defmethod salary \"com\" [amount] (+ (:b amount) (/ (:b amount) 2)))  \n" +
						"   (defmethod salary \"bon\" [amount] (+ (:b amount) 99))                 \n" +
						"   (defmethod salary :default  [amount] (:b amount))                      \n" +
						"                                                                          \n" +
						"   [(salary {:t \"com\" :b 1000})                                         \n" +
						"    (salary {:t \"bon\" :b 1000})                                         \n" +
						"    (salary {:t \"xxx\" :b 1000})]                                        \n" +
						")                                                                           ")
				.build()
	) {
	    private static final long serialVersionUID = -1;
	};

	public static VncFunction def_dynamic = 
		new SpecialFormsDocFunction(
				"def-dynamic",
				VncFunction
				.meta()
				.arglists("(def-dynamic name expr)")		
				.doc(
					"Creates a dynamic variable that starts off as a global variable " +
					"and can be bound with 'binding' to a new value on the local thread.")
				.examples(
					"(do                      \n" +
					"   (def-dynamic x 100)   \n" +
					"   (println x)           \n" +
					"   (binding [x 200]      \n" +
					"      (println x))       \n" +
					"   (println x)))           ")
				.build()
	) {
	    private static final long serialVersionUID = -1;
	};

	public static VncFunction binding = 
		new SpecialFormsDocFunction(
				"binding",
				VncFunction
				.meta()
				.arglists("(binding [bindings*] exprs*)")		
				.doc("Evaluates the expressions and binds the values to dynamic (thread-local) symbols")
				.examples(
					"(do                      \n" +
					"   (binding [x 100]      \n" +
					"      (println x)        \n" +
					"      (binding [x 200]   \n" +
					"         (println x))    \n" +
					"      (println x)))        ")
				.build()
	) {
	    private static final long serialVersionUID = -1;
	};

	public static VncFunction do_ = 
		new SpecialFormsDocFunction(
				"do",
				VncFunction
				.meta()
				.arglists("(do exprs)")		
				.doc("Evaluates the expressions in order and returns the value of the last.")
				.examples("(do (println \"Test...\") (+ 1 1))")
				.build()
	) {
	    private static final long serialVersionUID = -1;
	};

	public static VncFunction dorun = 
		new SpecialFormsDocFunction(
				"dorun",
				VncFunction
				.meta()
				.arglists("(dorun count expr)")		
				.doc("Runs the expr count times in the most effective way. It's main purpose is supporting performance test.")
				.examples("(dorun 10 (+ 1 1))")
				.build()
	) {
	    private static final long serialVersionUID = -1;
	};

	public static VncFunction prof = 
		new SpecialFormsDocFunction(
				"prof",
				VncFunction
				.meta()
				.arglists("(prof opts)")		
				.doc(
					"Controls the code profiling. See the companion functions/macros 'dorun' and 'perf'. " +
					"The perf macro is built on prof and dorun and provides all to do simple Venice profiling.")
				.examples(
					"(do  \n" +
					"  (prof :on)   ; turn profiler on  \n" +
					"  (prof :off)   ; turn profiler off  \n" +
					"  (prof :status)   ; returns the profiler on/off staus  \n" +
					"  (prof :clear)   ; clear profiler data captured so far  \n" +
					"  (prof :data)   ; returns the profiler data as map  \n" +
					"  (prof :data-formatted)   ; returns the profiler data as formatted text  \n" +
					"  (prof :data-formatted \"Metrics test\")   ; returns the profiler data as formatted text with a title  \n" +
					"  nil)  ")
				.build()
	) {
	    private static final long serialVersionUID = -1;
	};

	public static VncFunction if_ = 
		new SpecialFormsDocFunction(
				"if",
				VncFunction
				.meta()
				.arglists("(if test true-expr false-expr)")		
				.doc("Evaluates test.")
				.examples("(if (< 10 20) \"yes\" \"no\")")
				.build()
	) {
	    private static final long serialVersionUID = -1;
	};

	public static VncFunction let = 
		new SpecialFormsDocFunction(
				"let",
				VncFunction
				.meta()
				.arglists("(let [bindings*] exprs*)")		
				.doc("Evaluates the expressions and binds the values to symbols to new local context")
				.examples(
					"(let [x 1] x))",
					
					";; destructured map                     \n" +
					"(let [{:keys [width height title ]      \n" +
					"       :or {width 640 height 500}       \n" +
					"       :as styles}                      \n" +
					"      {:width 1000 :title \"Title\"}]   \n" +
					"     (println \"width: \" width)        \n" +
					"     (println \"height: \" height)      \n" +
					"     (println \"title: \" title)        \n" +
					"     (println \"styles: \" styles))       ")
				.build()
	) {
	    private static final long serialVersionUID = -1;
	};

	public static VncFunction loop = 
		new SpecialFormsDocFunction(
				"loop",
				VncFunction
				.meta()
				.arglists("(loop [bindings*] exprs*)")		
				.doc(
					"Evaluates the exprs and binds the bindings. " + 
					"Creates a recursion point with the bindings.")
				.examples(
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
					"   (sum 10000))                                       ")
				.build()
	) {
	    private static final long serialVersionUID = -1;
	};

	public static VncFunction recur = 
		new SpecialFormsDocFunction(
				"recur",
				VncFunction
				.meta()
				.arglists("(recur expr*)")		
				.doc(
					"Evaluates the exprs and rebinds the bindings of the recursion " + 
					"point to the values of the exprs. The recur expression must be " +
					"at the tail position. The tail position is a postion which an " +
					"expression would return a value from.")
				.examples(
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
					"   (sum 10000))                                       ")
				.build()
	) {
	    private static final long serialVersionUID = -1;
	};

	public static VncFunction try_ = 
		new SpecialFormsDocFunction(
				"try",
				VncFunction
				.meta()
				.arglists(
					"(try expr)",
					"(try expr (catch exClass exSym expr))",
					"(try expr (catch exClass exSym expr) (finally expr))")		
				.doc("Exception handling: try - catch -finally ")
				.examples(
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
					"   (try                                               \n" +
					"      (throw [1 2 3])                                 \n" +
					"      (catch :ValueException ex (str (:value ex)))    \n" +
					"      (catch :RuntimeException ex \"runtime ex\")     \n" +
					"      (finally (println \"...finally\"))))             ")
				.build()
	) {
	    private static final long serialVersionUID = -1;
	};

	public static VncFunction try_with = 
		new SpecialFormsDocFunction(
				"try-with",
				VncFunction
				.meta()
				.arglists(
					"(try-with [bindings*] expr)",
					"(try-with [bindings*] expr (catch :java.lang.Exception ex expr))",
					"(try-with [bindings*] expr (catch :java.lang.Exception ex expr) (finally expr))")		
				.doc("try-with resources allows the declaration of resources to be used in a try block "
						+ "with the assurance that the resources will be closed after execution "
						+ "of that block. The resources declared must implement the Closeable or ")
				.examples(
					"(do                                                   \n" +
					"   (import :java.io.FileInputStream)                  \n" +
					"   (let [file (io/temp-file \"test-\", \".txt\")]     \n" +
					"        (io/spit file \"123456789\" :append true)     \n" +
					"        (try-with [is (. :FileInputStream :new file)] \n" +
					"           (io/slurp-stream is :binary false))))        ")
				.build()
	) {
	    private static final long serialVersionUID = -1;
	};

	public static VncFunction defmacro = 
		new SpecialFormsDocFunction(
				"defmacro",
				VncFunction
				.meta()
				.arglists("(defmacro name [params*] body)")		
				.doc("Macro definition")
				.examples(
					"(defmacro unless [pred a b]   \n" + 
					"  `(if (not ~pred) ~a ~b))      ")
				.build()
	) {
	    private static final long serialVersionUID = -1;
	};

	public static VncFunction macroexpand = 
		new SpecialFormsDocFunction(
				"macroexpand",
				VncFunction
				.meta()
				.arglists("(macroexpand form)")		
				.doc("If form represents a macro form, returns its expansion, else returns form")
				.examples("(macroexpand (-> c (+ 3) (* 2)))")
				.build()
	) {
	    private static final long serialVersionUID = -1;
	};

	public static VncFunction import_ = 
		new SpecialFormsDocFunction(
				"import",
				VncFunction
				.meta()
				.arglists("(import class)")		
				.doc("Imports a Java class")
				.examples("(do\n   (import :java.lang.Long)\n   (. :Long :new 10))")
				.build()
	) {
	    private static final long serialVersionUID = -1;
	};
	
	
	private static class SpecialFormsDocFunction extends VncFunction {
		public SpecialFormsDocFunction(final String name, final VncVal meta) {
			super(name, meta);
		}
		
		public VncVal apply(final VncList args) {
			return Nil;
		}
		
	    private static final long serialVersionUID = -1;
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
					.put("resolve",		resolve)				
					.put("def",			def)
					.put("defonce",		defonce)
					.put("defmulti",	defmulti)
					.put("defmethod",	defmethod)
					.put("def-dynamic",	def_dynamic)
					.put("binding",		binding)
					.put("do",			do_)
					.put("if",			if_)
					.put("let",			let)
					.put("loop",		loop)
					.put("recur",		recur)
					.put("try",			try_)
					.put("try-with",	try_with)
					.put("defmacro",	defmacro)
					.put("macroexpand",	macroexpand)
					.put("import",		import_)
					.put("dorun",		dorun)
					.put("prof",		prof)
					.toMap();
}
