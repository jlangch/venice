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
package com.github.jlangch.venice.impl;

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.util.Map;

import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncSymbol;
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
					.arglists("(doc x)")
					.doc(
						"Prints documentation for a var or special form given x as its name. " +
						"Prints the definition of custom types. \n\n" +
						"Displays the source of a module if x is a module: (doc :ansi)")
					.examples(
						"(doc +)",
						"(doc def)",
						"(do \n" +
						"   (deftype :complex [real :long, imaginary :long]) \n" +
						"   (doc :complex))")
					.build()
		) {
			private static final long serialVersionUID = -1;
		};
	
	public static VncFunction modules = 
		new SpecialFormsDocFunction(
				"modules",
				VncFunction
					.meta()
					.arglists("(modules )")
					.doc("Lists the available modules")
					.build()
		) {
			private static final long serialVersionUID = -1;
		};
			
	public static VncFunction list = 
		new SpecialFormsDocFunction(
				"()",
				VncFunction
					.meta()
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
					.arglists("(resolve symbol)")
					.doc("Resolves a symbol.")
					.examples(
						"(resolve '+)", 
						"(resolve 'y)", 
						"(resolve (symbol \"+\"))",
						"((-> \"first\" symbol resolve) [1 2 3])")
					.build()
		) {
		   private static final long serialVersionUID = -1;
		};

	public static VncFunction var_get = 
		new SpecialFormsDocFunction(
				"var-get",
				VncFunction
					.meta()
					.arglists("(var-get sym)")
					.doc("Returns the var associated with the symbol")
					.examples(
						"(var-get '+)")
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

	public static VncFunction set_BANG = 
		new SpecialFormsDocFunction(
				"set!",
				VncFunction
					.meta()
					.arglists("(set! var-symbol expr)")
					.doc("Sets a global or thread-local variable to the value of the expression.")
					.examples(
						"(do                             \n" +
						"  (def x 10)                    \n" +
						"  (set! x 20)                   \n" +
						"  x)                              ",
						 
						"(do                             \n" +
						"   (def-dynamic x 100)          \n" +
						"   (set! x 200)                 \n" +
						"   x)                             ",
						
						"(do                             \n" +
						"   (def-dynamic x 100)          \n" +
						"   (with-out-str                \n" +
						"      (print x)                 \n" +
						"      (binding [x 200]          \n" +
						"        (print (str \"-\" x))   \n" +
						"        (set! x (inc x))        \n" +
						"        (print (str \"-\" x)))  \n" +
						"      (print (str \"-\" x))))     ")
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

	public static VncFunction deftype = 
		new SpecialFormsDocFunction(
				"deftype",
				VncFunction
					.meta()
					.arglists(
						"(deftype name fields)",
						"(deftype name fields validator)")
					.doc(
						"Defines a new custom type for the name with the fields.")
					.examples(
						"(do                                                      \n" +
						"  (ns foo)                                               \n" +
						"  (deftype :complex [real :long, imaginary :long])       \n" +
						"  ; explicitly creating a custom type value              \n" +
						"  (def x (.: :complex 100 200))                          \n" +
						"  ; Venice implicitly creates a builder function         \n" +
						"  ; suffixed with a '.'                                  \n" +
						"  (def y (complex. 200 300))                             \n" +
						"  ; ... and a type check function                        \n" +
						"  (complex? y)                                           \n" +
						"  y)                                                       ",
						"(do                                                      \n" +
						"  (ns foo)                                               \n" +
						"  (deftype :complex [real :long, imaginary :long])       \n" +
						"  (def x (complex. 100 200))                             \n" +
						"  (type x))                                                ",
						"(do                                                      \n" +
						"  (ns foo)                                               \n" +
						"  (deftype :complex                                                           \n" +
						"           [real :long, imaginary :long]                                      \n" +
						"           (fn [t]                                                            \n" +
						"              (assert (pos? (:real t)) \"real must be positive\")             \n" +
						"              (assert (pos? (:imaginary t)) \"imaginary must be positive\"))) \n" +
						"  (def x (complex. 100 200))                                                  \n" +
						"  [(:real x) (:imaginary x)])                                                   ",
						"(do                                                      \n" +
						"  (ns foo)                                               \n" +
						"  (deftype :named [name :string, value :any])            \n" +
						"  (def x (named. \"count\" 200))                         \n" +
						"  (def y (named. \"seq\" [1 2]))                         \n" +
						"  [x y])                                                   ")
					.build()
		) {
			private static final long serialVersionUID = -1;
		};

	public static VncFunction deftypeQ = 
		new SpecialFormsDocFunction(
				"deftype?",
				VncFunction
					.meta()
					.arglists(
						"(deftype? type)")
					.doc(
						"Returns true if type is a custom type else false.")
					.examples(
						"(do                                                 \n" +
						"  (ns foo)                                          \n" +
						"  (deftype :complex [real :long, imaginary :long])  \n" +
						"  (deftype? :complex))                                ",
						"(do                                                 \n" +
						"  (ns foo)                                          \n" +
						"  (deftype-of :email-address :string)               \n" +
						"  (deftype? :email-address))                          ",
						"(do                                                 \n" +
						"  (ns foo)                                          \n" +
						"  (deftype :complex [real :long, imaginary :long])  \n" +
						"  (def x (complex. 100 200))                        \n" +
						"  (deftype? (type x)))                                ")
					.build()
		) {
			private static final long serialVersionUID = -1;
		};

	public static VncFunction deftype_of = 
		new SpecialFormsDocFunction(
				"deftype-of",
				VncFunction
					.meta()
					.arglists(
						"(deftype-of name base-type)",
						"(deftype-of name base-type validator)")
					.doc(
						"Defines a new custom type wrapper based on a base type.")
					.examples(
						"(do                                                           \n" +
						"  (ns foo)                                                    \n" +
						"  (deftype-of :email-address :string)                         \n" +
						"  ; explicitly creating a wrapper type value                  \n" +
						"  (def x (.: :email-address \"foo@foo.org\"))                 \n" +
						"  ; Venice implicitly creates a builder function              \n" +
						"  ; suffixed with a '.'                                       \n" +
						"  (def y (email-address. \"foo@foo.org\"))                    \n" +
						"  ; ... and a type check function                             \n" +
						"  (email-address? y)                                          \n" +
						"  y)                                                            ",
						"(do                                                           \n" +
						"  (ns foo)                                                    \n" +
						"  (deftype-of :email-address :string)                         \n" +
						"  (str \"Email: \" (email-address. \"foo@foo.org\")))           ",
						"(do                                                           \n" +
						"  (ns foo)                                                    \n" +
						"  (deftype-of :email-address :string)                         \n" +
						"  (def x (email-address. \"foo@foo.org\"))                    \n" +
						"  [(type x) (supertype x)])                                     ",
						"(do                                                           \n" +
						"  (ns foo)                                                    \n" +
						"  (deftype-of :email-address                                  \n" +
						"              :string                                         \n" +
						"              str/valid-email-addr?)                          \n" +
						"  (email-address. \"foo@foo.org\"))                             ",
						"(do                                                           \n" +
						"  (ns foo)                                                    \n" +
						"  (deftype-of :contract-id :long)                             \n" +
						"  (contract-id. 100000))                                        ",
						"(do                                                           \n" +
						"  (ns foo)                                                    \n" +
						"  (deftype-of :my-long :long)                                 \n" +
						"  (+ 10 (my-long. 100000)))                                     ")
					.build()
		) {
			private static final long serialVersionUID = -1;
		};

	public static VncFunction deftype_or = 
		new SpecialFormsDocFunction(
				"deftype-or",
				VncFunction
					.meta()
					.arglists(
						"(deftype-or name val*)")
					.doc(
						"Defines a new custom or type.")
					.examples(
						"(do                                                           \n" +
						"  (ns foo)                                                    \n" +
						"  (deftype-or :color :red :green :blue)                       \n" +
						"  ; explicitly creating a wrapper type value                  \n" +
						"  (def x (.: :color :red))                                    \n" +
						"  ; Venice implicitly creates a builder function              \n" +
						"  ; suffixed with a '.'                                       \n" +
						"  (def y (color. :red))                                       \n" +
						"  ; ... and a type check function                             \n" +
						"  (color? y)                                                  \n" +
						"  y)                                                            ",
						"(do                                                           \n" +
						"  (ns foo)                                                    \n" +
						"  (deftype-or :digit 0 1 2 3 4 5 6 7 8 9)                     \n" +
						"  (digit. 1))                                                   ",
						"(do                                                           \n" +
						"  (ns foo)                                                    \n" +
						"  (deftype-or :long-or-double :long :double)                  \n" +
						"  (long-or-double. 1000))                                       ")
				.build()
		) {
			private static final long serialVersionUID = -1;
		};

	public static VncFunction deftype_new = 
		new SpecialFormsDocFunction(
				".:",
				VncFunction
					.meta()
					.arglists("(.: type-name args*)")
					.doc("Instantiates a custom type.")
					.examples(
						"(do                                                      \n" +
						"  (ns foo)                                               \n" +
						"  (deftype :complex [real :long, imaginary :long])       \n" +
						"  (def x (.: :complex 100 200))                          \n" +
						"  [(:real x) (:imaginary x)])                              ")
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

	public static VncFunction bound_Q = 
		new SpecialFormsDocFunction(
				"bound?",
				VncFunction
					.meta()
					.arglists("(bound? s)")
					.doc("Returns true if the symbol is bound to a value else false")
					.examples(
						"(bound? 'test)",
						"(let [test 100] (bound? 'test))" )
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
					.doc(
						"Runs the expr count times in the most effective way. It's main purpose is " +
						"supporting benchmark test. Returns the expression result of the first " +
						"invocation.")
					.examples("(dorun 10 (+ 1 1))")
					.build()
		) {
			private static final long serialVersionUID = -1;
		};

	public static VncFunction dobench = 
		new SpecialFormsDocFunction(
				"dobench",
				VncFunction
					.meta()
					.arglists("(dobench count expr)")
					.doc(
						"Runs the expr count times in the most effective way and returns a list of " +
						"elapsed nanoseconds for each invocation. It's main purpose is supporting " +
						"benchmark test.")
					.examples("(dobench 10 (+ 1 1))")
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
					.doc(
						"Evaluates the expressions and binds the values to symbols in " +
						"the new local context.")
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


	public static VncFunction locking = 
		new SpecialFormsDocFunction(
				"locking",
				VncFunction
					.meta()
					.arglists("(locking x & exprs)")
					.doc(
						"Executes exprs in an implicit do, while holding the monitor of x. \n" + 
						"Will release the monitor of x in all circumstances. \n" +
						"Locking operates like the synchronized keyword in Java.")
					.examples(
						"(do                        \n" +
						"   (def x 1)               \n" +
						"   (locking x              \n" +
						"      (println 100)        \n" +
						"      (println 200)))        ",
						";; Locks are reentrant     \n" +
						"(do                        \n" +
						"   (def x 1)               \n" +
						"   (locking x              \n" +
						"      (locking x           \n" +
						"         (println \"in\")) \n" +
						"      (println \"out\")))    ")
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
					.doc(
						"If form represents a macro form, returns its expansion, else " + 
						"returns form.\n\n" +
						"To recursively expand all macros in a form use (macroexpand-all form).")
					.examples("(macroexpand '(-> c (+ 3) (* 2)))")
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
					.doc(
						"Imports a Java class. Imports are bound to the current namespace.")
					.examples(
						"(do                        \n" +
						"  (import :java.lang.Math) \n" +
						"  (. :Math :max 2 10))      ",
						"(do                                                                \n" +
						"  (ns alpha)                                                       \n" +
						"  (import :java.lang.Math)                                         \n" +
						"  (println \"alpha:\" (any? #(== % :java.lang.Math) (imports)))    \n" +
						"                                                                   \n" +
						"  (ns beta)                                                        \n" +
						"  (println \"beta:\" (any? #(== % :java.lang.Math) (imports)))     \n" +
						"                                                                   \n" +
						"  (ns alpha)                                                       \n" +
						"  (println \"alpha:\" (any? #(== % :java.lang.Math) (imports)))    \n" +
						")")
					.build()
		) {
			private static final long serialVersionUID = -1;
		};

	public static VncFunction imports_ = 
		new SpecialFormsDocFunction(
				"imports",
				VncFunction
					.meta()
					.arglists("(imports)")
					.doc("List the registered imports for the current namespace.")
					.examples(
						"(do                        \n" +
						"  (import :java.lang.Math) \n" +
						"  (imports))                 ")
					.build()
		) {
			private static final long serialVersionUID = -1;
		};

	public static VncFunction ns_new = 
		new SpecialFormsDocFunction(
				"ns",
				VncFunction
					.meta()
					.arglists("(ns sym)")
					.doc("Opens a namespace.")
					.examples(
						"(do                               \n" + 
						"  (ns xxx)                        \n" + 
						"  (def foo 1)                     \n" + 
						"  (ns yyy)                        \n" + 
						"  (def foo 5)                     \n" + 
						"  (println xxx/foo foo yyy/foo))    ")
					.build()
		) {
			private static final long serialVersionUID = -1;
		};

	public static VncFunction ns_unmap = 
		new SpecialFormsDocFunction(
				"ns-unmap",
				VncFunction
					.meta()
					.arglists("(ns-unmap ns sym)")
					.doc("Removes the mappings for the symbol from the namespace.")
					.examples(
						"(do                    \n" + 
						"  (ns xxx)             \n" + 
						"  (def foo 1)          \n" + 
						"  (ns-unmap xxx foo)   \n" + 
						"  (ns-unmap *ns* foo))   ")
					.build()
		) {
			private static final long serialVersionUID = -1;
		};

	public static VncFunction ns_remove = 
		new SpecialFormsDocFunction(
				"ns-remove",
				VncFunction
					.meta()
					.arglists("(ns-remove ns)")
					.doc("Removes the mappings for all symbols from the namespace.")
					.examples(
						"(do                     \n" + 
						"  (ns xxx)              \n" + 
						"  (def foo 1)           \n" + 
						"  (def goo 1)           \n" + 
						"  (ns-remove xxx foo)   \n" + 
						"  (ns-remove *ns* foo))   ")
					.build()
		) {
			private static final long serialVersionUID = -1;
		};

	public static VncFunction namespace = 
		new SpecialFormsDocFunction(
				"namespace",
				VncFunction
					.meta()
					.arglists("(namespace x)")
					.doc("Returns the namespace String of a symbol or keyword, or nil if not present.")
					.examples(
						"(namespace 'user/foo)",
						"(namespace :user/foo)")
					.build()
		) {
			private static final long serialVersionUID = -1;
		};

	public static VncFunction var_version = 
		new SpecialFormsDocFunction(
				"*version*",
				VncFunction
					.meta()
					.doc("The Venice version")
					.examples("*version*")
					.build()
		) {
			private static final long serialVersionUID = -1;
		};

	public static VncFunction var_newline = 
		new SpecialFormsDocFunction(
				"*newline*",
				VncFunction
					.meta()
					.doc("The system newline")
					.examples("*newline*")
					.build()
		) {
			private static final long serialVersionUID = -1;
		};

	public static VncFunction var_loaded_modules = 
		new SpecialFormsDocFunction(
				"*loaded-modules*",
				VncFunction
					.meta()
					.doc("The loaded modules")
					.examples("*loaded-modules*")
					.build()
		) {
			private static final long serialVersionUID = -1;
		};

	public static VncFunction var_loaded_files = 
		new SpecialFormsDocFunction(
				"*loaded-files*",
				VncFunction
					.meta()
					.doc("The loaded files")
					.examples("*loaded-files*")
					.build()
		) {
			private static final long serialVersionUID = -1;
		};

	public static VncFunction var_ns = 
		new SpecialFormsDocFunction(
				"*ns*",
				VncFunction
					.meta()
					.doc("The current namespace")
					.examples(
						"*ns*", 
						"(do \n" +
						"  (ns test) \n" +
						"  *ns*)")
					.build()
		) {
			private static final long serialVersionUID = -1;
		};

	public static VncFunction var_run_mode = 
		new SpecialFormsDocFunction(
				"*run-mode*",
				VncFunction
					.meta()
					.doc("The current run-mode one of (:repl, :script, :app)")
					.examples(
						"*run-mode*")
					.build()
		) {
			private static final long serialVersionUID = -1;
		};
	

	public static VncFunction var_ansi_term = 
		new SpecialFormsDocFunction(
				"*ansi-term*",
				VncFunction
					.meta()
					.doc("True if Venice runs in an ANSI terminal, otherwise false")
					.examples(
						"*ansi-term*")
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
			new VncHashMap
					.Builder()
					.put(new VncSymbol("doc"), 				doc)
					.put(new VncSymbol("modules"), 			modules)
					.put(new VncSymbol("()"), 				list)
					.put(new VncSymbol("[]"), 				vector)
					.put(new VncSymbol("#{}"),				set)
					.put(new VncSymbol("{}"), 				map)
					.put(new VncSymbol("fn"), 				fn)
					.put(new VncSymbol("eval"),				eval)
					.put(new VncSymbol("resolve"),			resolve)
					.put(new VncSymbol("var-get"),			var_get)
					.put(new VncSymbol("def"),				def)
					.put(new VncSymbol("defonce"),			defonce)
					.put(new VncSymbol("defmulti"),			defmulti)
					.put(new VncSymbol("defmethod"),		defmethod)
					.put(new VncSymbol("deftype"),			deftype)
					.put(new VncSymbol("deftype?"),			deftypeQ)
					.put(new VncSymbol("deftype-of"),		deftype_of)
					.put(new VncSymbol("deftype-or"),		deftype_or)
					.put(new VncSymbol(".:"),				deftype_new)
					.put(new VncSymbol("def-dynamic"),		def_dynamic)
					.put(new VncSymbol("binding"),			binding)
					.put(new VncSymbol("bound?"),			bound_Q)
					.put(new VncSymbol("set!"),				set_BANG)
					.put(new VncSymbol("do"),				do_)
					.put(new VncSymbol("if"),				if_)
					.put(new VncSymbol("let"),				let)
					.put(new VncSymbol("loop"),				loop)
					.put(new VncSymbol("recur"),			recur)
					.put(new VncSymbol("try"),				try_)
					.put(new VncSymbol("try-with"),			try_with)
					.put(new VncSymbol("locking"),			locking)
					.put(new VncSymbol("defmacro"),			defmacro)
					.put(new VncSymbol("macroexpand"),		macroexpand)
					.put(new VncSymbol("ns"),				ns_new)
					.put(new VncSymbol("ns-unmap"),			ns_unmap)
					.put(new VncSymbol("ns-remove"),		ns_remove)
					.put(new VncSymbol("namespace"),		namespace)
					.put(new VncSymbol("import"),			import_)
					.put(new VncSymbol("imports"),			imports_)
					.put(new VncSymbol("dobench"),			dobench)
					.put(new VncSymbol("dorun"),			dorun)
					.put(new VncSymbol("prof"),				prof)
					.put(new VncSymbol("*version*"),		var_version)
					.put(new VncSymbol("*newline*"),		var_newline)
					.put(new VncSymbol("*loaded-modules*"),var_loaded_modules)
					.put(new VncSymbol("*loaded-files*"),  var_loaded_files)
					.put(new VncSymbol("*ns*"),				var_ns)
					.put(new VncSymbol("*run-mode*"),		var_run_mode)
					.put(new VncSymbol("*ansi-term*"),		var_ansi_term)
					.toMap();
}
