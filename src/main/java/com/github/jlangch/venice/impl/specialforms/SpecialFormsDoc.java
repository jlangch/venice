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
package com.github.jlangch.venice.impl.specialforms;

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.util.Map;

import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;


/**
 * Provides the documentation for the cheat-sheet and the 'doc' function
 * for global vars and special forms that are implemented in the Reader 
 * or in the VeniceInterpreter without having a VncSpecialForm 
 * implementation!
 * 
 * Special forms have evaluation rules that differ from standard Venice 
 * evaluation rules and are understood directly by the Venice reader or
 * interpreter.
 */
public class SpecialFormsDoc {

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

	public static VncFunction if_ = 
		new SpecialFormsDocFunction(
				"if",
				VncFunction
					.meta()
					.arglists("(if test then else)", "(if test then)")
					.doc("Evaluates test. If logical true, evaluates and returns then expression, " +
						 "otherwise else expression, if supplied, else nil.")
					.examples(
						"(if (< 10 20) \"yes\" \"no\")",
						"(if true \"yes\")",
						"(if false \"yes\")")
					.seeAlso("if-let", "if-not", "when", "when-not", "when-let")
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
						"(let [x 1] x)",
						
						"(let [x 1   \n" +
						"      y 2]  \n" +
						"  (+ x y))    ",
						
						";; Destructured list                    \n" +
						"(let [[x y] '(1 2)]                     \n" +
						"  (printf \"x: %d, y: %d%n\" x y))        ",
						
						";; Destructured map                     \n" +
						"(let [{:keys [width height title ]      \n" +
						"       :or {width 640 height 500}       \n" +
						"       :as styles}                      \n" +
						"      {:width 1000 :title \"Title\"}]   \n" +
						"     (println \"width: \" width)        \n" +
						"     (println \"height: \" height)      \n" +
						"     (println \"title: \" title)        \n" +
						"     (println \"styles: \" styles))       ")
					.seeAlso("letfn", "if-let", "when-let", "binding")
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
					.seeAlso("recur")
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
					.seeAlso("loop")
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
						"To recursively expand all macros in a form use " +
						"`(macroexpand-all form)`.")
					.examples("(macroexpand '(-> c (+ 3) (* 2)))")
					.seeAlso("defmacro", "macroexpand-all")
					.build()
		) {
			private static final long serialVersionUID = -1;
		};
		
	public static VncFunction tail_pos = 
		new SpecialFormsDocFunction(
				"tail-pos",
				VncFunction
					.meta()
					.arglists("(tail-pos)", "(tail-pos name)")
					.doc(
						"Throws a NotInTailPositionException if the expr is not in " +
						"tail position otherwise returns nil. \n\n" +
						"Definition:¶\n" +
						"The tail position is a position which an expression would " +
						"return a value from. There are no more forms evaluated after " +
						"the form in the tail position is evaluated. ")
					.examples(
						";; in tail position \n" +
						"(do 1 (tail-pos))",
						";; not in tail position \n" +
						"(do (tail-pos) 1)")
					.build()
		) {
			private static final long serialVersionUID = -1;
		};

	public static VncFunction global_var_version = 
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

	public static VncFunction global_var_newline = 
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

	public static VncFunction global_var_loaded_modules = 
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

	public static VncFunction global_var_loaded_files = 
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

	public static VncFunction global_var_ns = 
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

	public static VncFunction global_var_run_mode = 
		new SpecialFormsDocFunction(
				"*run-mode*",
				VncFunction
					.meta()
					.doc("The current run-mode one of `:repl`, `:script`, `:app`")
					.examples(
						"*run-mode*")
					.build()
		) {
			private static final long serialVersionUID = -1;
		};
	
	public static VncFunction global_var_ansi_term = 
		new SpecialFormsDocFunction(
				"*ansi-term*",
				VncFunction
					.meta()
					.doc("`true` if Venice runs in an ANSI terminal, otherwise `false`")
					.examples(
						"*ansi-term*")
					.build()
		) {
			private static final long serialVersionUID = -1;
		};
		
	public static VncFunction global_var_argv = 
		new SpecialFormsDocFunction(
				"*ARGV*",
				VncFunction
					.meta()
					.doc(
						"A list of the supplied command line arguments, or nil if " +
						"instantiator of the Venice instance decided not to make " +
						"the command line arguments available.")
					.examples(
						"*ARGV*")
					.build()
		) {
			private static final long serialVersionUID = -1;
		};
		
	public static VncFunction global_var_out = 
		new SpecialFormsDocFunction(
				"*out*",
				VncFunction
					.meta()
					.doc(
						"A `:java.io.PrintStream` object representing standard output " +
						"for print operations.\n\n" +
						"Defaults to System.out, wrapped in an PrintStream.\n\n" +
						"`*out*` is a dynamic var. Any `:java.io.PrintStream` can be " +
						"dynamically bound to it:\n\n" +
						"```                           \n" +
						"(binding [*out* print-stream] \n" +
						"  (println \"text\"))         \n" +
						"```")
					.seeAlso("with-out-str", "*err*", "*in*")
					.build()
		) {
			private static final long serialVersionUID = -1;
		};
		
	public static VncFunction global_var_err = 
		new SpecialFormsDocFunction(
				"*err*",
				VncFunction
					.meta()
					.doc(
						"A `:java.io.PrintStream` object representing standard error " +
						"for print operations.\n\n" +
						"Defaults to System.err, wrapped in an PrintStream.\n\n" +
						"`*err*` is a dynamic var. Any `:java.io.PrintStream` can be " +
						"dynamically bound to it:\n\n" +
						"```                           \n" +
						"(binding [*err* print-stream] \n" +
						"  (println \"text\"))         \n" +
						"```")
					.seeAlso("with-err-str", "*out*", "*in*")
					.build()
		) {
			private static final long serialVersionUID = -1;
		};
		
	public static VncFunction global_var_in = 
		new SpecialFormsDocFunction(
				"*in*",
				VncFunction
					.meta()
					.doc(
						"A `:java.io.Reader` object representing standard input " +
						"for read operations.\n\n" +
						"Defaults to System.in, wrapped in an InputStreamReader.\n\n" +
						"`*in*` is a dynamic var. Any `:java.io.Reader` can be " +
						"dynamically bound to it:\n\n" +
						"```                           \n" +
						"(binding [*in* reader]        \n" +
						"  (read-line))                \n" +
						"```")
					.seeAlso("read-line", "read-char", "*out*", "*err*")
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
			new SymbolMapBuilder()
					.put(new VncSymbol("do"),				do_)
					.put(new VncSymbol("if"),				if_)
					.put(new VncSymbol("let"),				let)
					.put(new VncSymbol("loop"),				loop)
					.put(new VncSymbol("recur"),			recur)
					.put(new VncSymbol("macroexpand"),		macroexpand)
					.put(new VncSymbol("tail-pos"),			tail_pos)					

					.put(new VncSymbol("()"),				list)
					.put(new VncSymbol("[]"),				vector)
					.put(new VncSymbol("#{}"),				set)
					.put(new VncSymbol("{}"),				map)

					.put(new VncSymbol("*version*"),		global_var_version)
					.put(new VncSymbol("*newline*"),		global_var_newline)
					.put(new VncSymbol("*loaded-modules*"),	global_var_loaded_modules)
					.put(new VncSymbol("*loaded-files*"),	global_var_loaded_files)
					.put(new VncSymbol("*ns*"),				global_var_ns)
					.put(new VncSymbol("*run-mode*"),		global_var_run_mode)
					.put(new VncSymbol("*ansi-term*"),		global_var_ansi_term)
					.put(new VncSymbol("*ARGV*"),	     	global_var_argv)
					
					.put(new VncSymbol("*out*"),		    global_var_out)
					.put(new VncSymbol("*err*"),		    global_var_err)
					.put(new VncSymbol("*in*"),		        global_var_in)
					.toMap();
}
