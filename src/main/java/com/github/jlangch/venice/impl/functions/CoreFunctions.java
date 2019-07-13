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
package com.github.jlangch.venice.impl.functions;

import static com.github.jlangch.venice.impl.functions.FunctionsUtil.assertArity;
import static com.github.jlangch.venice.impl.functions.FunctionsUtil.assertMinArity;
import static com.github.jlangch.venice.impl.functions.FunctionsUtil.removeNilValues;
import static com.github.jlangch.venice.impl.types.Constants.False;
import static com.github.jlangch.venice.impl.types.Constants.Nil;
import static com.github.jlangch.venice.impl.types.Constants.True;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.github.jlangch.venice.ContinueException;
import com.github.jlangch.venice.EofException;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.Reader;
import com.github.jlangch.venice.impl.Readline;
import com.github.jlangch.venice.impl.ValueException;
import com.github.jlangch.venice.impl.javainterop.JavaInteropUtil;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncBigDecimal;
import com.github.jlangch.venice.impl.types.VncByteBuffer;
import com.github.jlangch.venice.impl.types.VncDouble;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncInteger;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncThreadLocal;
import com.github.jlangch.venice.impl.types.VncTunnelAsJavaObject;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncCollection;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncHashSet;
import com.github.jlangch.venice.impl.types.collections.VncJavaList;
import com.github.jlangch.venice.impl.types.collections.VncJavaSet;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncMapEntry;
import com.github.jlangch.venice.impl.types.collections.VncMutableMap;
import com.github.jlangch.venice.impl.types.collections.VncOrderedMap;
import com.github.jlangch.venice.impl.types.collections.VncQueue;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.collections.VncSet;
import com.github.jlangch.venice.impl.types.collections.VncSortedMap;
import com.github.jlangch.venice.impl.types.collections.VncSortedSet;
import com.github.jlangch.venice.impl.types.collections.VncStack;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.CallFrame;
import com.github.jlangch.venice.impl.util.StreamUtil;
import com.github.jlangch.venice.impl.util.WithCallStack;
import com.github.jlangch.venice.impl.util.transducer.Reduced;


public class CoreFunctions {
	
	///////////////////////////////////////////////////////////////////////////
	// Errors/Exceptions
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction throw_ex = 
		new VncFunction(
				"throw", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(throw)", "(throw x)")		
					.doc("Throws exception with passed value x")
					.examples(
						"(do                                                     \n" +
						"   (try                                                 \n" +
						"     (+ 100 200)                                        \n" +
						"     (catch :Exception ex (:message ex))))                ",
						
						"(do                                                     \n" +
						"   (try                                                 \n" +
						"     (throw 100)                                        \n" +
						"     (catch :ValueException ex (:value ex))))             ",
						
						"(do                                                     \n" +
						"   (try                                                 \n" +
						"      (throw [100 {:a 3}])                              \n" +
						"      (catch :ValueException ex (:value ex))            \n" +
						"      (finally (println \"#finally\"))))                  ",
						
						"(do                                                     \n" +
						"   (import :java.lang.RuntimeException)                 \n" +
						"   (try                                                 \n" +
						"      (throw (. :RuntimeException :new \"#test\"))      \n" +  
						"      (catch :RuntimeException ex (:message ex))))        ",
						
						";; Venice wraps thrown checked exceptions with a RuntimeException! \n" +
						"(do                                                                \n" +
						"   (import :java.lang.RuntimeException)                            \n" +
						"   (import :java.io.IOException)                                   \n" +
						"   (try                                                            \n" +
						"      (throw (. :IOException :new \"#test\"))                      \n" +  
						"      (catch :RuntimeException ex (:message (:cause ex)))))          ")
					.build()
		) {
			public VncVal apply(final VncList args) {
				if (args.isEmpty()) {
					throw new ValueException("throw", Constants.Nil);
				}
				else if (Types.isVncJavaObject(args.first())) {
					final Object obj = ((VncJavaObject)args.first()).getDelegate();
					if (obj instanceof RuntimeException) {
						throw (RuntimeException)obj;
					}
					else if (obj instanceof Exception) {
						throw new RuntimeException((Exception)obj);
					}
					else {
						throw new RuntimeException(obj.toString());
					}
				}
				else {
					throw new ValueException("throw", args.first());
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};


	///////////////////////////////////////////////////////////////////////////
	// Scalar functions
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction nil_Q = 
		new VncFunction(
				"nil?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(nil? x)")		
					.doc("Returns true if x is nil, false otherwise")
					.examples(
						"(nil? nil)",
						"(nil? 0)",
						"(nil? false)")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("nil?", args, 1);
				
				return args.first() == Nil ? True : False;
			}
		
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction some_Q = 
		new VncFunction(
				"some?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(some? x)")		
					.doc("Returns true if x is not nil, false otherwise")
					.examples(
						"(some? nil)",
						"(some? 0)",
						"(some? 4.0)",
						"(some? false)",
						"(some? [])",
						"(some? {})")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("some?", args, 1);
				
				return args.first() == Nil ? False : True;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction true_Q = 
		new VncFunction(
				"true?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(true? x)")		
					.doc("Returns true if x is true, false otherwise")
					.examples(
						"(true? true)",
						"(true? false)",
						"(true? nil)",
						"(true? 0)",
						"(true? (== 1 1))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("true?", args, 1);
				
				return args.first() == True ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction false_Q = 
		new VncFunction(
				"false?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(false? x)")		
					.doc("Returns true if x is false, false otherwise")
					.examples(
						"(false? true)",
						"(false? false)",
						"(false? nil)",
						"(false? 0)",
						"(false? (== 1 2))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("false?", args, 1);
				
				return args.first() == False ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction boolean_Q = 
		new VncFunction(
				"boolean?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(boolean? n)")		
					.doc("Returns true if n is a boolean")
					.examples(
						"(boolean? true)",
						"(boolean? false)",
						"(boolean? nil)",
						"(boolean? 0)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("boolean?", args, 1);
				
				return args.first() == True || args.first() == False ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction int_Q = 
		new VncFunction(
				"int?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(int? n)")		
					.doc("Returns true if n is an int")
					.examples(
						"(int? 4I)",
						"(int? 4)",
						"(int? 3.1)",
						"(int? true)",
						"(int? nil)",
						"(int? {})")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("int?", args, 1);
	
				return Types.isVncInteger(args.first()) ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction long_Q = 
		new VncFunction(
				"long?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(long? n)")		
					.doc("Returns true if n is a long")
					.examples(
						"(long? 4)",
						"(long? 4I)",
						"(long? 3.1)",
						"(long? true)",
						"(long? nil)",
						"(long? {})")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("long?", args, 1);
	
				return Types.isVncLong(args.first()) ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction double_Q = 
		new VncFunction(
				"double?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(double? n)")		
					.doc("Returns true if n is a double")
					.examples(
						"(double? 4.0)",
						"(double? 3)",
						"(double? 3I)",
						"(double? 3.0M)",
						"(double? true)",
						"(double? nil)",
						"(double? {})")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("double?", args, 1);
				
				return Types.isVncDouble(args.first()) ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction decimal_Q = 
		new VncFunction(
				"decimal?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(decimal? n)")		
					.doc("Returns true if n is a decimal")
					.examples(
						"(decimal? 4.0M)",
						"(decimal? 4.0)",
						"(decimal? 3)",
						"(decimal? 3I)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("decimal?", args, 1);
				
				return Types.isVncBigDecimal(args.first()) ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction number_Q = 
		new VncFunction(
				"number?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(number? n)")		
					.doc("Returns true if n is a number (int, long, double, or decimal)")
					.examples(
						"(number? 4I))",
						"(number? 4)",
						"(number? 4.0M)",
						"(number? 4.0)",
						"(number? true)",
						"(number? \"a\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("number?", args, 1);
				
				return Types.isVncLong(args.first()) 
						|| Types.isVncInteger(args.first())
						|| Types.isVncDouble(args.first())
						|| Types.isVncBigDecimal(args.first())? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction string_Q = 
		new VncFunction(
				"string?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(string? x)")		
					.doc("Returns true if x is a string")
					.examples(
						"(string? \"abc\")",
						"(string? 1)",
						"(string? nil)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("string?", args, 1);
				
				if (args.first() instanceof VncKeyword) { 
					return False; 
				}
				else if (args.first() instanceof VncString) { 
					return True; 
				}
				else {
					return False;
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction symbol = 
		new VncFunction(
				"symbol", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(symbol name)")		
					.doc("Returns a symbol from the given name")
					.examples(
						"(symbol \"a\")",
						"(symbol 'a)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("symbol", args, 1);
	
				if (Types.isVncSymbol(args.first())) {
					return args.first();
				} 
				else if (Types.isVncString(args.first())) {
					return new VncSymbol((VncString)args.first());
				} 
				else {
					throw new VncException(String.format(
							"Function 'symbol' does not allow %s name.",
							Types.getType(args.first())));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction symbol_Q = 
		new VncFunction(
				"symbol?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(symbol? x)")		
					.doc("Returns true if x is a symbol")
					.examples(
						"(symbol? (symbol \"a\"))",
						"(symbol? 'a)",
						"(symbol? nil)",
						"(symbol? :a)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("symbol?", args, 1);
				
				return Types.isVncSymbol(args.first()) ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction keyword = 
		new VncFunction(
				"keyword", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(keyword name)")		
					.doc("Returns a keyword from the given name")
					.examples(
						"(keyword \"a\")",
						"(keyword :a)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("keyword", args, 1);
				
				if (Types.isVncKeyword(args.first())) {
					return args.first();
				} 
				else if (Types.isVncString(args.first())) {
					return new VncKeyword(((VncString)args.first()).getValue());
				} 
				else {
					throw new VncException(String.format(
							"Function 'keyword' does not allow %s name",
							Types.getType(args.first())));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction keyword_Q = 
		new VncFunction(
				"keyword?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(keyword? x)")		
					.doc("Returns true if x is a keyword")
					.examples(
						"(keyword? (keyword \"a\"))",
						"(keyword? :a)",
						"(keyword? nil)",
						"(keyword? 'a)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("keyword?", args, 1);
				
				return Types.isVncKeyword(args.first()) ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction fn_Q = 
		new VncFunction(
				"fn?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(fn? x)")		
					.doc("Returns true if x is a function")
					.examples("(do \n   (def sum (fn [x] (+ 1 x)))\n   (fn? sum))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("fn?", args, 1);
				
				if (!Types.isVncFunction(args.first())) { 
					return False; 
				}
				return ((VncFunction)args.first()).isMacro() ? False : True;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncFunction macro_Q = 
		new VncFunction(
				"macro?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(macro? x)")		
					.doc("Returns true if x is a macro")
					.examples("(macro? and)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("macro?", args, 1);
				
				if (!Types.isVncFunction(args.first())) { 
					return False; 
				}
				return ((VncFunction)args.first()).isMacro() ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};


	///////////////////////////////////////////////////////////////////////////
	// String functions
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction pr_str = 
		new VncFunction(
				"pr-str", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(pr-str & xs)")		
					.doc(
						"With no args, returns the empty string. With one arg x, returns " + 
						"x.toString(). With more than one arg, returns the concatenation " +
						"of the str values of the args with delimiter ' '.")
					.examples("(pr-str )", "(pr-str 1 2 3)")
					.build()
		) {
			public VncVal apply(final VncList args) {
				return args.isEmpty()
						? new VncString("")
						: new VncString(
								args.getList()
									.stream()
									.map(v -> Printer.pr_str(v, true))
									.collect(Collectors.joining(" ")));
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction str = 
		new VncFunction(
				"str", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(str & xs)")		
					.doc(
						"With no args, returns the empty string. With one arg x, returns " + 
						"x.toString(). (str nil) returns the empty string. With more than " + 
						"one arg, returns the concatenation of the str values of the args.")
					.examples(
						"(str )", 
						"(str 1 2 3)", 
						"(str +)", 
						"(str [1 2 3])")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				final StringBuilder sb = new StringBuilder();
				for(VncVal v : args.getList()) {
					if (v != Nil) {
						sb.append(Printer.pr_str(v, false));
					}
				}		
				return new VncString(sb.toString());
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction readline = 
		new VncFunction(
				"readline", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(readline prompt)")		
					.doc("Reads the next line from stdin. The function is sandboxed")
					.build()
		) {
			public VncVal apply(final VncList args) {			
				final String prompt = Coerce.toVncString(args.first()).getValue();
				try {
					return new VncString(Readline.readline(prompt));
				} 
				catch (IOException ex) {
					try (WithCallStack cs = new WithCallStack(CallFrame.fromVal("readline", args))) {
						throw new ValueException(new VncString(ex.getMessage()), ex);
					}
				} 
				catch (EofException e) {
					return Nil;
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction read_string = 
		new VncFunction(
				"read-string", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(read-string x)")		
					.doc("Reads from x")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				try {
					assertArity("read-string", args, 1, 2);
	
					return Reader.read_str(
							Coerce.toVncString(args.first()).getValue(), 
							args.size() == 2 ? Coerce.toVncString(args.second()).getValue() : "user");
				} 
				catch (ContinueException c) {
					return Nil;
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	
	///////////////////////////////////////////////////////////////////////////
	// Number functions
	///////////////////////////////////////////////////////////////////////////	

	public static VncFunction equal_Q = 
		new VncFunction(
				"==", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(== x y)")		
					.doc("Returns true if both operands have the equivalent type")
					.examples("(== 0 0)", "(== 0 1)", "(== 0 0.0)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("==", args, 2);
				
				return Types._equal_Q(args.first(), args.second()) ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction not_equal_Q = 
		new VncFunction(
				"!=", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(!= x y)")		
					.doc("Returns true if both operands do not have the equivalent type")
					.examples("(!= 0 1)", "(!= 0 0)", "(!= 0 0.0)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("!=", args, 2);
				
				return Types._equal_Q(args.first(), args.second()) ? False : True;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction match_Q = 
		new VncFunction(
				"match?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(match? s regex)")		
					.doc("Returns true if the string s matches the regular expression regex")
					.examples(
							"(match? \"1234\" \"[0-9]+\")", 
							"(match? \"1234ss\" \"[0-9]+\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("match?", args, 2);
				
				if (!Types.isVncString(args.first())) {
					throw new VncException(String.format(
							"Invalid first argument type %s while calling function 'match?'",
							Types.getType(args.first())));
				}
				if (!Types.isVncString(args.second())) {
					throw new VncException(String.format(
							"Invalid second argument type %s while calling function 'match?'",
							Types.getType(args.second())));
				}
	
				return Types._match_Q(args.first(), args.second()) ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction not_match_Q = 
		new VncFunction(
				"not-match?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(not-match? s regex)")		
					.doc("Returns true if the string s does not match the regular expression regex")
					.examples(
						"(not-match? \"1234\" \"[0-9]+\")", 
						"(not-match? \"1234ss\" \"[0-9]+\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("not-match?", args, 2);
				
				if (!Types.isVncString(args.first())) {
					throw new VncException(String.format(
							"Invalid first argument type %s while calling function 'not-match?'",
							Types.getType(args.first())));
				}
				if (!Types.isVncString(args.second())) {
					throw new VncException(String.format(
							"Invalid second argument type %s while calling function 'not-match?'",
							Types.getType(args.second())));
				}
				
				return Types._match_Q(args.first(), args.second()) ? False : True;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction lt = 
		new VncFunction(
				"<", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(< x y)")		
					.doc("Returns true if x is smaller than y")
					.examples("(< 2 3)", "(< 2 3.0)", "(< 2 3.0M)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("<", args, 2);
	
				final VncVal op1 = args.first();
				final VncVal op2 = args.second();
				
				if (Types.isVncNumber(op1)) {
					return op1.compareTo(op2) < 0 ? True : False;
				}
				else if (Types.isVncString(op1)) {
					if (!Types.isVncString(op2)) {
						throw new VncException(String.format(
								"Function '<' with operand 1 of type %s does not allow %s as operand 2", 
								Types.getType(op1),
								Types.getType(op2)));
					}
	
					final String s1 = ((VncString)op1).getValue();
					final String s2 = ((VncString)op2).getValue();
					return s1.compareTo(s2) < 0 ? True : False;
				}
				else {
					throw new VncException(String.format(
											"Function '<' does not allow %s as operand 1", 
											Types.getType(op1)));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction lte = 
		new VncFunction(
				"<=", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(<= x y)")		
					.doc("Returns true if x is smaller or equal to y")
					.examples("(<= 2 3)", "(<= 3 3)", "(<= 2 3.0)", "(<= 2 3.0M)")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("<=", args, 2);
	
				final VncVal op1 = args.first();
				final VncVal op2 = args.second();
				
				if (Types.isVncNumber(op1)) {
					return op1.compareTo(op2) <= 0 ? True : False;
				}
				else if (Types.isVncString(op1)) {
					if (!Types.isVncString(op2)) {
						throw new VncException(String.format(
								"Function '<=' with operand 1 of type %s does not allow %s as operand 2", 
								Types.getType(op1),
								Types.getType(op2)));
					}
	
					final String s1 = ((VncString)op1).getValue();
					final String s2 = ((VncString)op2).getValue();
					return s1.compareTo(s2) <= 0 ? True : False;
				}
				else {
					throw new VncException(String.format(
											"Function '<=' does not allow %s as operand 1", 
											Types.getType(op1)));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction gt = 
		new VncFunction(
				">", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(> x y)")		
					.doc("Returns true if x is greater than y")
					.examples("(> 3 2)", "(> 3 3)", "(> 3.0 2)", "(> 3.0M 2)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity(">", args, 2);
	
				final VncVal op1 = args.first();
				final VncVal op2 = args.second();
				
				if (Types.isVncNumber(op1)) {
					return op1.compareTo(op2) > 0 ? True : False;
				}
				else if (Types.isVncString(op1)) {
					if (!Types.isVncString(op2)) {
						throw new VncException(String.format(
								"Function '>' with operand 1 of type %s does not allow %s as operand 2", 
								Types.getType(op1),
								Types.getType(op2)));
					}
	
					final String s1 = ((VncString)op1).getValue();
					final String s2 = ((VncString)op2).getValue();
					return s1.compareTo(s2) > 0 ? True : False;
				}
				else {
					throw new VncException(String.format(
											"Function '>' does not allow %s as operand 1", 
											Types.getType(op1)));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction gte = 
		new VncFunction(
				">=", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(>= x y)")		
					.doc("Returns true if x is greater or equal to y")
					.examples("(>= 3 2)", "(>= 3 3)", "(>= 3.0 2)", "(>= 3.0M 2)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity(">=", args, 2);
	
				final VncVal op1 = args.first();
				final VncVal op2 = args.second();
				
				if (Types.isVncNumber(op1)) {
					return op1.compareTo(op2) >= 0 ? True : False;
				}
				else if (Types.isVncString(op1)) {
					if (!Types.isVncString(op2)) {
						throw new VncException(String.format(
								"Function '>=' with operand 1 of type %s does not allow %s as operand 2", 
								Types.getType(op1),
								Types.getType(op2)));
					}
	
					final String s1 = ((VncString)op1).getValue();
					final String s2 = ((VncString)op2).getValue();
					return s1.compareTo(s2) >= 0 ? True : False;
				}
				else {
					throw new VncException(String.format(
											"Function '>=' does not allow %s as operand 1", 
											Types.getType(op1)));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	


	
	///////////////////////////////////////////////////////////////////////////
	// Casts
	///////////////////////////////////////////////////////////////////////////
 	
	public static VncFunction boolean_cast = 
		new VncFunction(
				"boolean", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(boolean x)")		
					.doc("Converts to boolean. Everything except 'false' and 'nil' is true in boolean context.")
					.examples(
						"(boolean false)",
						"(boolean true)",
						"(boolean nil)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("boolean", args, 1);
	
				final VncVal arg = args.first();
				if (arg == Nil) {
					return False;
				}
				else if (arg == False) {
					return False;
				}
				else {
					return True;
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
 	
	public static VncFunction long_cast = 
		new VncFunction(
				"long", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(long x)")		
					.doc("Converts to long")
					.examples(
						"(long 1)",
						"(long nil)",
						"(long false)",
						"(long true)",
						"(long 1.2)",
						"(long 1.2M)",
						"(long \"1\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("long", args, 1);
	
				final VncVal op1 = args.first();
				if (op1 == Nil) {
					return new VncLong(0);
				}
				else if (op1 == False) {
					return new VncLong(0);
				}
				else if (op1 == True) {
					return new VncLong(1);
				}
				else if (Types.isVncLong(op1)) {
					return op1;
				}
				else if (Types.isVncInteger(op1)) {
					return Numeric.intToLong((VncInteger)op1);
				}
				else if (Types.isVncDouble(op1)) {
					return Numeric.doubleToLong((VncDouble)op1);
				}
				else if (Types.isVncBigDecimal(op1)) {
					return Numeric.decimalToLong((VncBigDecimal)op1);
				}
				else if (Types.isVncString(op1)) {
					final String s = ((VncString)op1).getValue();
					try {
						return new VncLong(Long.parseLong(s));
					}
					catch(Exception ex) {
						throw new VncException(String.format(
								"Function 'long': the string %s can not be converted to a long", 
								s));
					}
				}
				else {
					throw new VncException(String.format(
											"Function 'long' does not allow %s as operand 1", 
											Types.getType(op1)));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction int_cast = 
	new VncFunction(
			"int", 
			VncFunction
				.meta()
				.arglists("(int x)")		
				.doc("Converts to int")
				.examples(
					"(int 1)",
					"(int nil)",
					"(int false)",
					"(int true)",
					"(int 1.2)",
					"(int 1.2M)",
					"(int \"1\")")
				.build()
	) {		
		public VncVal apply(final VncList args) {
			assertArity("int", args, 1);

			final VncVal op1 = args.first();
			if (op1 == Nil) {
				return new VncInteger(0);
			}
			else if (op1 == False) {
				return new VncInteger(0);
			}
			else if (op1 == True) {
				return new VncInteger(1);
			}
			else if (Types.isVncInteger(op1)) {
				return op1;
			}
			else if (Types.isVncLong(op1)) {
				return Numeric.longToInt((VncLong)op1);
			}
			else if (Types.isVncDouble(op1)) {
				return Numeric.doubleToInt((VncDouble)op1);
			}
			else if (Types.isVncBigDecimal(op1)) {
				return Numeric.decimalToInt((VncBigDecimal)op1);
			}
			else if (Types.isVncString(op1)) {
				final String s = ((VncString)op1).getValue();
				try {
					return new VncInteger(Integer.parseInt(s));
				}
				catch(Exception ex) {
					throw new VncException(String.format(
							"Function 'int': the string %s can not be converted to an int", 
							s));
				}
			}
			else {
				throw new VncException(String.format(
										"Function 'int' does not allow %s as operand 1", 
										Types.getType(op1)));
			}
		}

	    private static final long serialVersionUID = -1848883965231344442L;
	};

	public static VncFunction double_cast = 
		new VncFunction(
				"double", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(double x)")		
					.doc("Converts to double")
					.examples(
						"(double 1)",
						"(double nil)",
						"(double false)",
						"(double true)",
						"(double 1.2)",
						"(double 1.2M)",
						"(double \"1.2\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("double", args, 1);
	
				final VncVal op1 = args.first();
				if (op1 == Nil) {
					return new VncDouble(0.0);
				}
				else if (op1 == False) {
					return new VncDouble(0.0);
				}
				else if (op1 == True) {
					return new VncDouble(1.0);
				}
				else if (Types.isVncInteger(op1)) {
					return Numeric.intToDouble((VncInteger)op1);
				}
				else if (Types.isVncLong(op1)) {
					return Numeric.longToDouble((VncLong)op1);
				}
				else if (Types.isVncDouble(op1)) {
					return op1;
				}
				else if (Types.isVncBigDecimal(op1)) {
					return Numeric.decimalToDouble((VncBigDecimal)op1);
				}
				else if (Types.isVncString(op1)) {
					final String s = ((VncString)op1).getValue();
					try {
						return new VncDouble(Double.parseDouble(s));
					}
					catch(Exception ex) {
						throw new VncException(String.format(
								"Function 'double': the string %s can not be converted to a double", 
								s));
					}
				}
				else {
					throw new VncException(String.format(
								"Function 'double' does not allow %s as operand 1", 
								Types.getType(op1)));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction decimal_cast = 
		new VncFunction(
				"decimal", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(decimal x) (decimal x scale rounding-mode)")		
					.doc(
						"Converts to decimal. rounding-mode is one of (:CEILING, :DOWN, " +
						":FLOOR, :HALF_DOWN, :HALF_EVEN, :HALF_UP, :UNNECESSARY, :UP)")
					.examples(
						"(decimal 2)", 
						"(decimal 2 3 :HALF_UP)", 
						"(decimal 2.5787 3 :HALF_UP)",
						"(decimal 2.5787M 3 :HALF_UP)",
						"(decimal \"2.5787\" 3 :HALF_UP)",
						"(decimal nil)")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("decimal", args, 1, 3);
	
				if (args.isEmpty()) {
					return new VncBigDecimal(BigDecimal.ZERO);
				}
				else {				
					final VncVal arg = args.first();
					final VncLong scale = args.size() < 3 ? null : Coerce.toVncLong(args.second());
					final RoundingMode roundingMode = args.size() < 3 ? null : VncBigDecimal.toRoundingMode((VncString)args.nth(2));
	
					if (arg == Constants.Nil) {
						final BigDecimal dec = BigDecimal.ZERO;
						return new VncBigDecimal(args.size() < 3 ? dec : dec.setScale(scale.getValue().intValue(), roundingMode));
					}
					else if (arg == Constants.False) {
						final BigDecimal dec = BigDecimal.ZERO;
						return new VncBigDecimal(args.size() < 3 ? dec : dec.setScale(scale.getValue().intValue(), roundingMode));
					}
					else if (arg == Constants.True) {
						final BigDecimal dec = BigDecimal.ONE;
						return new VncBigDecimal(args.size() < 3 ? dec : dec.setScale(scale.getValue().intValue(), roundingMode));
					}
					else if (Types.isVncString(arg)) {
						final BigDecimal dec = new BigDecimal(((VncString)arg).getValue());
						return new VncBigDecimal(args.size() < 3 ? dec : dec.setScale(scale.getValue().intValue(), roundingMode));
					}
					else if (Types.isVncNumber(arg)) {
						final BigDecimal dec = Numeric.toDecimal(arg).getValue();
						return new VncBigDecimal(args.size() < 3 ? dec : dec.setScale(scale.getValue().intValue(), roundingMode));
					}
					else {
						throw new VncException(String.format(
								"Function 'decimal' does not allow %s as operand 1", 
								Types.getType(arg)));
					}
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	
	///////////////////////////////////////////////////////////////////////////
	// List functions
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction new_list = 
		new VncFunction(
				"list", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(list & items)")		
					.doc("Creates a new list containing the items.")
					.examples("(list )", "(list 1 2 3)", "(list 1 2 3 [:a :b])")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				return new VncList(args.getList());
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction new_list_ASTERISK = 
		new VncFunction(
				"list*", 
				VncFunction
					.meta()
					.module("core")
					.arglists(	
				    	"(list* args)",
				    	"(list* a args)",
				    	"(list* a b args)",
				    	"(list* a b c args)",
				    	"(list* a b c d & more)")
					.doc(
						"Creates a new list containing the items prepended to the rest, the\n" + 
						"last of which will be treated as a collection.")
					.examples(
						"(list* 1 [2 3])", 
						"(list* 1 2 3 [4])", 
						"(list* '(1 2) 3 [4])", 
						"(list* nil)",
						"(list* nil [2 3])",
						"(list* 1 2 nil)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertMinArity("list*", args, 1);
				
				if (args.size() == 1 && args.first() == Nil) {
					return Nil;
				}			
				else if (args.last() == Nil) {
					return new VncList(args.slice(0, args.size()-1).getList());
				}
				else if (!Types.isVncSequence(args.last())) {
					throw new VncException(String.format(
							"Function 'list*' does not allow %s as last argument", 
							Types.getType(args.last())));
				}
				else {			
					return new VncList()
								.addAllAtEnd(args.slice(0, args.size()-1))
								.addAllAtEnd((VncSequence)args.last());
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	static public boolean list_Q(VncVal mv) {
		return Types.isVncList(mv);
	}
	
	public static VncFunction list_Q = 
		new VncFunction(
				"list?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(list? obj)")		
					.doc("Returns true if obj is a list")
					.examples("(list? (list 1 2))", "(list? '(1 2))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("list?", args, 1);
				
				return list_Q(args.first()) ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	
	///////////////////////////////////////////////////////////////////////////
	// Vector functions
	///////////////////////////////////////////////////////////////////////////
	
	public static VncFunction new_vector = 
		new VncFunction(
				"vector", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(vector & items)")		
					.doc("Creates a new vector containing the items.")
					.examples(
						"(vector )", 
						"(vector 1 2 3)", 
						"(vector 1 2 3 [:a :b])")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				return new VncVector(args.getList());
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	static public boolean vector_Q(VncVal mv) {
		return Types.isVncVector(mv);
	}
	
	public static VncFunction vector_Q = 
		new VncFunction(
				"vector?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(vector? obj)")		
					.doc("Returns true if obj is a vector")
					.examples(
						"(vector? (vector 1 2))", 
						"(vector? [1 2])")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("vector?", args, 1);
				
				return vector_Q(args.first()) ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncFunction subvec = 
		new VncFunction(
				"subvec", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(subvec v start) (subvec v start end)")		
					.doc(
						"Returns a vector of the items in vector from start (inclusive) "+
						"to end (exclusive). If end is not supplied, defaults to " + 
						"(count vector)")
					.examples(
						"(subvec [1 2 3 4 5 6] 2)", 
						"(subvec [1 2 3 4 5 6] 4)")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("subvec", args, 2, 3);
	
				final VncVector vec = Coerce.toVncVector(args.first());		
				final VncLong from = Coerce.toVncLong(args.second());
				final VncLong to = args.size() > 2 ? Coerce.toVncLong(args.nth(2)) : null;
				
				return new VncVector(
								to == null
									? vec.getList().subList(from.getValue().intValue(), vec.size())
									: vec.getList().subList(from.getValue().intValue(), to.getValue().intValue()));
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncFunction replace = 
		new VncFunction(
				"replace", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(replace smap coll)")		
					.doc(
						"Given a map of replacement pairs and a collection, returns a\n" + 
						"collection with any elements that are a key in smap replaced with the\n" + 
						"corresponding value in smap.")
					.examples(
							"(replace {2 :two, 4 :four} [4 2 3 4 5 6 2])",
							"(replace {2 :two, 4 :four} #{1 2 3 4 5})",
							"(replace {[:a 10] [:c 30]} {:a 10 :b 20})")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("replace", args, 2);
	
				final VncMap map = Coerce.toVncMap(args.first());
				final VncVal coll = args.second();
				
				if (Types.isVncSequence(coll)) {
					final VncSequence seq = (VncSequence)coll;
					
					final List<VncVal> vals = new ArrayList<>();
							
					for(VncVal v : seq.getList()) {
						final VncVal r = map.get(v);
						vals.add(r == Nil ? v : r);
					}
	
					return seq.withValues(vals, coll.getMeta());
				}
				else if (Types.isVncSet(coll)) {
					final VncSet set = (VncSet)coll;
					
					final List<VncVal> vals = new ArrayList<>();
							
					for(VncVal v : set.getList()) {
						final VncVal r = map.get(v);
						vals.add(r == Nil ? v : r);
					}
	
					return set.withValues(vals, coll.getMeta());
				}
				else if (Types.isVncMap(coll)) {
					VncMap mapc = (VncMap)coll;
					
					for(VncMapEntry e : map.entries()) {
						final VncVal k = Coerce.toVncVector(e.getKey()).first();
						final VncVal v = Coerce.toVncVector(e.getKey()).second();
						
						if (v.equals(mapc.get(k))) {
							mapc = mapc.dissoc(k);
							mapc = mapc.assoc(
										Coerce.toVncVector(e.getValue()).first(),
										Coerce.toVncVector(e.getValue()).second());
						}
					}
					
					return mapc;
				}
				else {
					throw new VncException(
							"Function 'repeat' requires a list, vector, set, or map as coll argument");
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	
	///////////////////////////////////////////////////////////////////////////
	// ByteBuf functions
	///////////////////////////////////////////////////////////////////////////
	
	public static VncFunction bytebuf_Q = 
		new VncFunction(
				"bytebuf?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(bytebuf? x)")		
					.doc("Returns true if x is a bytebuf")
					.examples(
						"(bytebuf? (bytebuf [1 2]))",
						"(bytebuf? [1 2])",
						"(bytebuf? nil)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("bytebuf?", args, 1);
				
				return Types.isVncByteBuffer(args.first()) ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction bytebuf_cast = 
		new VncFunction(
				"bytebuf", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(bytebuf x)")		
					.doc( "Converts to bytebuf. x can be a bytebuf, a list/vector of longs, or a string")
					.examples("(bytebuf [0 1 2])", "(bytebuf '(0 1 2))", "(bytebuf \"abc\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("bytebuf", args, 0, 1);
	
				if (args.isEmpty()) {
					return new VncByteBuffer(ByteBuffer.wrap(new byte[0]));
				}
				
				final VncVal arg = args.first();
	
				if (Types.isVncString(arg)) {
					try {
						return new VncByteBuffer(
										ByteBuffer.wrap(
											((VncString)arg).getValue().getBytes("UTF-8")));
					}
					catch(Exception ex) {
						throw new VncException(
								"Failed to coerce string to bytebuf", ex);
					}
				}
				else if (Types.isVncJavaObject(arg)) {
					final Object delegate = ((VncJavaObject)arg).getDelegate();
					if (delegate.getClass() == byte[].class) {
						return new VncByteBuffer(ByteBuffer.wrap((byte[])delegate));
					}
					else if (delegate instanceof ByteBuffer) {
						return new VncByteBuffer((ByteBuffer)delegate);
					}
				}
				else if (Types.isVncByteBuffer(arg)) {
					return arg;
				}
				else if (Types.isVncSequence(arg)) {
					if (!((VncSequence)arg).getList().stream().allMatch(v -> Types.isVncLong(v))) {
						throw new VncException(String.format(
								"Function 'bytebuf' a list as argument must contains long values"));
					}
					
					final List<VncVal> list = ((VncSequence)arg).getList();
					
					final byte[] buf = new byte[list.size()];
					for(int ii=0; ii<list.size(); ii++) {
						buf[ii] = (byte)((VncLong)list.get(ii)).getValue().longValue();
					}
					
					return new VncByteBuffer(ByteBuffer.wrap(buf));
				}
	
				throw new VncException(String.format(
							"Function 'bytebuf' does not allow %s as argument", 
							Types.getType(arg)));
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction bytebuf_from_string = 
		new VncFunction(
				"bytebuf-from-string", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(bytebuf-from-string s encoding)")		
					.doc( "Converts a string to a bytebuf using an optional encoding. The encoding defaults to :UTF-8")
					.examples("(bytebuf-from-string \"abcdef\" :UTF-8)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("bytebuf-from-string", args, 1, 2);
	
				final String s = Coerce.toVncString(args.first()).getValue();
	
				final VncVal encVal = args.size() == 2 ? args.second() : Nil;
				final String encoding = encoding(encVal);
				
				try {
					return new VncByteBuffer(ByteBuffer.wrap(s.getBytes(encoding)));				
				}
				catch(Exception ex) {
					throw new VncException(String.format(
							"Failed to convert string to bytebuffer"));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction bytebuf_to_string = 
		new VncFunction(
				"bytebuf-to-string", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(bytebuf-to-string buf encoding)")		
					.doc( "Converts a bytebuf to a string using an optional encoding. The encoding defaults to :UTF-8")
					.examples("(bytebuf-to-string (bytebuf [97 98 99]) :UTF-8)")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("bytebuf-to-string", args, 1, 2);
	
				final ByteBuffer buf = Coerce.toVncByteBuffer(args.first()).getValue();
	
				final VncVal encVal = args.size() == 2 ? args.second() : Nil;
				final String encoding = encoding(encVal);
				
				try {
					return new VncString(new String(buf.array(), encoding));				
				}
				catch(Exception ex) {
					throw new VncException(String.format(
							"Failed to convert bytebuf to string"));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction bytebuf_sub = 
		new VncFunction(
				"bytebuf-sub", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(bytebuf-sub x start) (bytebuf-sub x start end)")		
					.doc(
						"Returns a byte buffer of the items in buffer from start (inclusive) "+
						"to end (exclusive). If end is not supplied, defaults to " + 
						"(count bytebuffer)")
					.examples(
						"(bytebuf-sub (bytebuf [1 2 3 4 5 6]) 2)", 
						"(bytebuf-sub (bytebuf [1 2 3 4 5 6]) 4)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("bytebuf-sub", args, 2, 3);
	
				final byte[] buf = Coerce.toVncByteBuffer(args.first()).getValue().array();		
				final VncLong from = Coerce.toVncLong(args.second());
				final VncLong to = args.size() > 2 ? Coerce.toVncLong(args.nth(2)) : null;
				
				
				return new VncByteBuffer(
								to == null
									? ByteBuffer.wrap(
											Arrays.copyOfRange(
													buf, 
													from.getValue().intValue(), 
													buf.length))
									:  ByteBuffer.wrap(
											Arrays.copyOfRange(
													buf, 
													from.getValue().intValue(), 
													to.getValue().intValue())));
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	
	///////////////////////////////////////////////////////////////////////////
	// Set functions
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction new_set = 
		new VncFunction(
				"set", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(set & items)")		
					.doc("Creates a new set containing the items.")
					.examples("(set )", "(set nil)", "(set 1)", "(set 1 2 3)", "(set [1 2] 3)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				return VncHashSet.ofAll(args);
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction new_sorted_set = 
		new VncFunction(
				"sorted-set", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(sorted-set & items)")		
					.doc("Creates a new sorted-set containing the items.")
					.examples("(sorted-set )", "(sorted-set nil)", "(sorted-set 1)", "(sorted-set 6 2 4)", "(str (sorted-set [2 3] [1 2]))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				return VncSortedSet.ofAll(args);
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction set_Q = 
		new VncFunction(
				"set?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(set? obj)")		
					.doc("Returns true if obj is a set")
					.examples("(set? (set 1))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("set?", args, 1);
				
				return Types.isVncHashSet(args.first()) ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction sorted_set_Q = 
		new VncFunction(
				"sorted-set?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(sorted-set? obj)")		
					.doc("Returns true if obj is a sorted-set")
					.examples("(sorted-set? (set 1))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("sorted-set?", args, 1);
				
				return Types.isVncSortedSet(args.first()) ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction difference = 
		new VncFunction(
				"difference", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(difference s1)", "(difference s1 s2)", "(difference s1 s2 & sets)")		
					.doc("Return a set that is the first set without elements of the remaining sets")
					.examples(
						"(difference (set 1 2 3))",
						"(difference (set 1 2) (set 2 3))",
						"(difference (set 1 2) (set 1) (set 1 4) (set 3))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertMinArity("difference", args, 1);
				
				Set<VncVal> set = new HashSet<>(Coerce.toVncSet(args.first()).getSet());
				
				for(int ii=1; ii<args.size(); ii++) {
					set.removeAll(Coerce.toVncSet(args.nth(ii)).getSet());
				}
				
				return VncHashSet.ofAll(set);
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction union = 
		new VncFunction(
				"union", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(union s1)", "(union s1 s2)", "(union s1 s2 & sets)")		
					.doc("Return a set that is the union of the input sets")
					.examples(
						"(union (set 1 2 3))",
						"(union (set 1 2) (set 2 3))",
						"(union (set 1 2 3) (set 1 2) (set 1 4) (set 3))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertMinArity("union", args, 1);
				
				final Set<VncVal> set = new HashSet<>(Coerce.toVncSet(args.first()).getSet());
				
				for(int ii=1; ii<args.size(); ii++) {
					set.addAll(Coerce.toVncSet(args.nth(ii)).getSet());
				}
				
				return VncHashSet.ofAll(set);
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction intersection = 
		new VncFunction(
				"intersection", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(intersection s1)", "(intersection s1 s2)", "(intersection s1 s2 & sets)")		
					.doc("Return a set that is the intersection of the input sets")
					.examples(
						"(intersection (set 1))",
						"(intersection (set 1 2) (set 2 3))",
						"(intersection (set 1 2) (set 3 4))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertMinArity("intersection", args, 1);
				
				final Set<VncVal> intersection = new HashSet<>();
			
				final Set<VncVal> first = Coerce.toVncSet(args.first()).getSet();
				
				first.forEach(v -> {
					boolean intersect = true;
					
					for(int ii=1; ii<args.size(); ii++) {
						if (!Coerce.toVncSet(args.nth(ii)).getSet().contains(v)) {
							intersect = false;
							break;
						}
					}
				
					if (intersect) {
						intersection.add(v);
					}	
				});
				
				return VncHashSet.ofAll(intersection);
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	
	///////////////////////////////////////////////////////////////////////////
	// HashMap functions
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction new_hash_map = 
		new VncFunction(
				"hash-map", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(hash-map & keyvals)", "(hash-map map)")		
					.doc("Creates a new hash map containing the items.")
					.examples(
						"(hash-map :a 1 :b 2)", 
						"(hash-map (sorted-map :a 1 :b 2))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				if (args.size() == 1 && Types.isVncMap(args.first())) {
					return new VncHashMap(((VncMap)args.first()).getMap());
				}
				else if (args.size() == 1 && Types.isVncJavaObject(args.first())) {
					return ((VncJavaObject)args.first()).toVncMap();
				}
				else {
					return VncHashMap.ofAll(args);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction new_ordered_map = 
		new VncFunction(
				"ordered-map", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(ordered-map & keyvals)", "(ordered-map map)")		
					.doc("Creates a new ordered map containing the items.")
					.examples(
						"(ordered-map :a 1 :b 2)", 
						"(ordered-map (hash-map :a 1 :b 2))")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				if (args.size() == 1 && Types.isVncMap(args.first())) {
					return new VncOrderedMap(((VncMap)args.first()).getMap());
				}
				else {
					return VncOrderedMap.ofAll(args);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction new_sorted_map = 
		new VncFunction(
				"sorted-map", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(sorted-map & keyvals)", "(sorted-map map)")		
					.doc("Creates a new sorted map containing the items.")
					.examples(
						"(sorted-map :a 1 :b 2)", 
						"(sorted-map (hash-map :a 1 :b 2))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				if (args.size() == 1 && Types.isVncMap(args.first())) {
					return new VncSortedMap(((VncMap)args.first()).getMap());
				}
				else {
					return VncSortedMap.ofAll(args);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction new_mutable_map = 
		new VncFunction(
				"mutable-map", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(mutable-map & keyvals)", "(mutable-map map)")		
					.doc("Creates a new mutable threadsafe map containing the items.")
					.examples(
						"(mutable-map :a 1 :b 2)", 
						"(mutable-map (hash-map :a 1 :b 2))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				return args.size() == 1 && Types.isVncMap(args.first())
						? new VncMutableMap(((VncMap)args.first()).getMap())
						: VncMutableMap.ofAll(args);
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction new_stack = 
		new VncFunction(
				"stack", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(stack )")		
					.doc("Creates a new mutable threadsafe stack.")
					.examples("(let [s (stack)]\n   (push! s 4)\n   (push! s 3)\n   (pop! s)\n   s)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("stack", args, 0);
				
				return new VncStack();
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction new_queue = 
		new VncFunction(
				"queue", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(queue )", "(queue 100)")		
					.doc("Creates a new mutable threadsafe bounded or unbounded queue.")
					.examples(
						";unbounded queue   \n" +
						"(let [q (queue)]   \n" +
						"  (offer! q 1)     \n" +
						"  (offer! q 2)     \n" +
						"  (offer! q 3)     \n" +
						"  (poll! q)        \n" +
						"   q)                ",
						";bounded queue        \n" +
						"(let [q (queue 10)]   \n" +
						"  (offer! q 1000 1)   \n" +
						"  (offer! q 1000 2)   \n" +
						"  (offer! q 1000 3)   \n" +
						"  (poll! q 1000)      \n" +
						"   q)                   ")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("queue", args, 0, 1);
				
				return args.isEmpty() ? new VncQueue() : new VncQueue(Coerce.toVncLong(args.first()).getIntValue());
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction map_Q = 
		new VncFunction(
				"map?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(map? obj)")		
					.doc("Returns true if obj is a map")
					.examples("(map? {:a 1 :b 2})")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("map?", args, 1);
				
				return Types.isVncMap(args.first()) ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction hash_map_Q = 
		new VncFunction(
				"hash-map?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(hash-map? obj)")		
					.doc("Returns true if obj is a hash map")
					.examples("(hash-map? (hash-map :a 1 :b 2))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("hash-map?", args, 1);
				
				return Types.isVncHashMap(args.first()) ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction ordered_map_Q = 
		new VncFunction(
				"ordered-map?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(ordered-map? obj)")		
					.doc("Returns true if obj is an ordered map")
					.examples("(ordered-map? (ordered-map :a 1 :b 2))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("ordered-map?", args, 1);
				
				return Types.isVncOrderedMap(args.first()) ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction sorted_map_Q = 
		new VncFunction(
				"sorted-map?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(sorted-map? obj)")		
					.doc("Returns true if obj is a sorted map")
					.examples("(sorted-map? (sorted-map :a 1 :b 2))")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("sorted-map?", args, 1);
				
				return Types.isVncSortedMap(args.first()) ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction mutable_map_Q = 
		new VncFunction(
				"mutable-map?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(mutable-map? obj)")		
					.doc("Returns true if obj is a mutable map")
					.examples("(mutable-map? (mutable-map :a 1 :b 2))")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("mutable-map?", args, 1);
				
				return Types.isVncMutableMap(args.first()) ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction stack_Q = 
			new VncFunction(
					"stack?", 
					VncFunction
						.meta()
						.module("core")
						.arglists("(stack? obj)")		
						.doc("Returns true if obj is a stack")
						.examples("(stack? (stack))")
						.build()
			) {	
				public VncVal apply(final VncList args) {
					assertArity("stack?", args, 1);
					
					return Types.isVncStack(args.first()) ? True : False;
				}
		
			    private static final long serialVersionUID = -1848883965231344442L;
			};

	public static VncFunction queue_Q = 
		new VncFunction(
				"queue?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(queue? obj)")		
					.doc("Returns true if obj is a queue")
					.examples("(queue? (queue))")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("queue?", args, 1);
				
				return Types.isVncQueue(args.first()) ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction contains_Q = 
		new VncFunction(
				"contains?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(contains? coll key)")		
					.doc(
						"Returns true if key is present in the given collection, otherwise " + 
						"returns false.")
					.examples(
						"(contains? {:a 1 :b 2} :a)",
						"(contains? [10 11 12] 1)",
						"(contains? [10 11 12] 5)",
						"(contains? \"abc\" 1)",
						"(contains? \"abc\" 5)")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("contains?", args, 2);
				
				final VncVal coll = args.first();
				final VncVal key = args.second();
				
				if (Types.isVncMap(coll)) {
					return ((VncMap)coll).containsKey(key);
				}
				else if (Types.isVncVector(coll)) {
					final VncVector v = (VncVector)coll;
					final VncLong k = (VncLong)key;
					return v.size() > k.getValue().intValue() ? True : False;
				}
				else if (Types.isVncHashSet(coll)) {
					final VncHashSet s = (VncHashSet)coll;
					return s.getSet().contains(key) ? True : False;
				}
				else if (Types.isVncString(coll)) {
					final VncString s = (VncString)coll;
					final VncLong k = (VncLong)key;
					return s.getValue().length() > k.getValue().intValue() ? True : False;			
				}
				else {
					throw new VncException(String.format(
							"Function 'contains?' does not allow %s as coll", 
							Types.getType(coll)));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction assoc = 
		new VncFunction(
				"assoc", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(assoc coll key val)", "(assoc coll key val & kvs)")		
					.doc(
						"When applied to a map, returns a new map of the " + 
						"same type, that contains the mapping of key(s) to " + 
						"val(s). When applied to a vector, returns a new vector that " + 
						"contains val at index. Note - index must be <= (count vector).")
					.examples(
						"(assoc {} :a 1 :b 2)",
						"(assoc nil :a 1 :b 2)",
						"(assoc [1 2 3] 0 10)",
						"(assoc [1 2 3] 3 10)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				final VncVal coll = args.first();
				if (coll == Nil) {
					return new VncHashMap().assoc((VncList)args.rest());
				}
				else if (Types.isVncMutableMap(coll)) {
					throw new VncException(String.format(
							"Function 'assoc' can not be used with mutable maps use assoc!", 
							Types.getType(coll)));
				}
				else if (Types.isVncMap(coll)) {
					return ((VncMap)coll).assoc((VncList)args.rest());
				}
				else if (Types.isVncVector(coll)) {
					VncVector vec = ((VncVector)coll);
					
					final VncList keyvals = args.rest();
					for(int ii=0; ii<keyvals.size(); ii+=2) {
						final VncLong key = Coerce.toVncLong(keyvals.nth(ii));
						final VncVal val = keyvals.nth(ii+1);
						if (vec.size() > key.getValue().intValue()) {
							vec = (VncVector)vec.setAt(key.getValue().intValue(), val);
						}
						else {
							vec = (VncVector)vec.addAtEnd(val);
						}
					}
					return vec;
				}
				else if (Types.isVncString(coll)) {
					String s = ((VncString)coll).getValue();
					final VncList keyvals = (VncList)args.rest();
					for(int ii=0; ii<keyvals.size(); ii+=2) {
						final VncLong key = Coerce.toVncLong(keyvals.nth(ii));
						final VncString val = Coerce.toVncString(keyvals.nth(ii+1));
						final int idx = key.getValue().intValue();
						if (s.length() > idx) {
							if (idx == 0) {
								s = "" + val.getValue().charAt(0) + s.substring(1);
							}
							else if (idx == s.length()-1) {
								s = s.substring(0, idx)  + val.getValue().charAt(0);
							}
							else {
								s = s.substring(0, idx) + val.getValue().charAt(0) + s.substring(idx+1);
							}
						}
						else {
							s = s + val.getValue().charAt(0);
						}
					}
					return new VncString(s);
				}
				else if (Types.isVncThreadLocal(coll)) {
					final VncThreadLocal th = (VncThreadLocal)coll;
					
					return th.assoc((VncList)args.rest());
				}
				else {
					throw new VncException(String.format(
							"Function 'assoc' does not allow %s as collection", 
							Types.getType(coll)));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction assoc_BANG = 
		new VncFunction(
				"assoc!", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(assoc! coll key val)", "(assoc! coll key val & kvs)")		
					.doc("Associates key/vals with a mutable map, returns the map")
					.examples(
						"(assoc! (mutable-map ) :a 1 :b 2)",
						"(assoc! nil :a 1 :b 2)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				final VncVal coll = args.first();
				if (coll == Nil) {
					return new VncMutableMap().assoc((VncList)args.rest());
				}
				else if (Types.isVncMutableMap(coll)) {
					return ((VncMutableMap)coll).assoc((VncList)args.rest());
				}
				else {
					throw new VncException(String.format(
							"Function 'assoc!' does not allow %s as collection. It works with mutable maps only.", 
							Types.getType(coll)));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction assoc_in = 
		new VncFunction(
				"assoc-in", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(assoc-in m ks v)")		
					.doc(
						"Associates a value in a nested associative structure, where ks is a " + 
						"sequence of keys and v is the new value and returns a new nested structure. " + 
						"If any levels do not exist, hash-maps or vectors will be created.")
					.examples(
						"(do\n   (def users [{:name \"James\" :age 26}  {:name \"John\" :age 43}])\n   (assoc-in users [1 :age] 44))",
						"(do\n   (def users [{:name \"James\" :age 26}  {:name \"John\" :age 43}])\n   (assoc-in users [2] {:name \"Jack\" :age 19}) )")
					.build()			
		) {		
			public VncVal apply(final VncList args) {
				assertArity("assoc-in", args, 3);
							
				final VncVal coll = args.first(); // may be Nil
				final VncSequence keys = Coerce.toVncSequence(args.second());
				final VncVal val = args.nth(2);
				
				final VncVal key = keys.first();
				final VncSequence keyRest = keys.rest();
				
				if (keyRest.isEmpty()) {
					return assoc.apply(VncList.of(coll, key, val));
				}
				else {
					final VncVal childColl = get.apply(VncList.of(coll, key));
					return assoc.apply(
							VncList.of(
									coll, 
									key, 
									assoc_in.apply(VncList.of(childColl, keyRest, val))));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction dissoc = 
		new VncFunction(
				"dissoc", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(dissoc coll key)", "(dissoc coll key & ks)")		
					.doc(
						"Returns a new coll of the same type, " + 
						"that does not contain a mapping for key(s)")
					.examples(
						"(dissoc {:a 1 :b 2 :c 3} :b)",
						"(dissoc {:a 1 :b 2 :c 3} :c :b)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				final VncVal coll = args.first();
				if (coll == Nil) {
					return Nil;
				}
				else if (Types.isVncMutableMap(coll)) {
					throw new VncException(String.format(
							"Function 'dissoc' can not be used with mutable maps use dissoc!", 
							Types.getType(coll)));
				}
				else if (Types.isVncMap(coll)) {
					return ((VncMap)args.first()).dissoc(args.rest());
				}
				else if (Types.isVncVector(coll)) {
					VncVector vec = ((VncVector)coll);
					final VncList keyvals = (VncList)args.rest();
					for(int ii=0; ii<keyvals.size(); ii++) {
						final VncLong key = Coerce.toVncLong(keyvals.nth(ii));
						if (vec.size() > key.getValue().intValue()) {
							vec = (VncVector)vec.removeAt(key.getValue().intValue());
						}
					}
					return vec;
				}
				else if (Types.isVncString(coll)) {
					String s = ((VncString)coll).getValue();
					final VncList keyvals = (VncList)args.rest();
					for(int ii=0; ii<keyvals.size(); ii++) {
						final VncLong key = Coerce.toVncLong(keyvals.nth(ii));
						final int idx = key.getValue().intValue();
						if (s.length() > idx) {
							if (idx == 0) {
								s = s.substring(1);
							}
							else if (idx == s.length()-1) {
								s = s.substring(0, idx);
							}
							else {
								s = s.substring(0, idx) + s.substring(idx+1);
							}
						}
					}
					return new VncString(s);
				}
				else if (Types.isVncThreadLocal(coll)) {
					final VncThreadLocal th = (VncThreadLocal)coll;
					
					return th.dissoc((VncList)args.rest());
				}
				else {
					throw new VncException(String.format(
							"Function 'dissoc' does not allow %s as coll", 
							Types.getType(coll)));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction dissoc_BANG = 
		new VncFunction(
				"dissoc!", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(dissoc! coll key)", "(dissoc! coll key & ks)")		
					.doc("Dissociates keys from a mutable map, returns the map")
					.examples(
						"(dissoc! (mutable-map :a 1 :b 2 :c 3) :b)",
						"(dissoc! (mutable-map :a 1 :b 2 :c 3) :c :b)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				final VncVal coll = args.first();
				if (coll == Nil) {
					return Nil;
				}
				else if (Types.isVncMap(coll)) {
					return ((VncMap)coll).dissoc(args.rest());
				}
				else {
					throw new VncException(String.format(
							"Function 'dissoc!' does not allow %s as coll. It works with mutable maps only.", 
							Types.getType(coll)));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction get = 
		new VncFunction(
				"get", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(get map key)", "(get map key not-found)")		
					.doc("Returns the value mapped to key, not-found or nil if key not present.")
					.examples(
						"(get {:a 1 :b 2} :b)",
						";; keywords act like functions on maps \n(:b {:a 1 :b 2})")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("get", args, 2, 3);
				
				if (args.first() == Nil) {
					final VncVal key_not_found = (args.size() == 3) ? args.nth(2) : Nil;
					return key_not_found;
				} 
				else if (Types.isVncMap(args.first())) {
					final VncMap mhm = Coerce.toVncMap(args.first());
					final VncVal key = args.second();
					final VncVal key_not_found = (args.size() == 3) ? args.nth(2) : Nil;
					
					final VncVal value = mhm.get(key);
					return value != Nil ? value : key_not_found;
				}
				else if (Types.isVncVector(args.first())) {
					final VncVector vec = Coerce.toVncVector(args.first());
					final int idx = Coerce.toVncLong(args.second()).getIntValue();
					final VncVal key_not_found = (args.size() == 3) ? args.nth(2) : Nil;
					
					return vec.nthOrDefault(idx, key_not_found);
				}
				else if (Types.isVncThreadLocal(args.first())) {
					final VncThreadLocal th = Coerce.toVncThreadLocal(args.first());
					
					final VncKeyword key = Coerce.toVncKeyword(args.second());
					final VncVal key_not_found = (args.size() == 3) ? args.nth(2) : Nil;
					
					final VncVal value = th.get(key);
					return value != Nil ? value : key_not_found;
				}
				else {
					throw new VncException(String.format(
							"Function 'get' does not allow %s as collection", 
							Types.getType(args.first())));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction get_in = 
		new VncFunction(
				"get-in", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(get-in m ks)", "(get-in m ks not-found)")		
					.doc(
						"Returns the value in a nested associative structure, " + 
						"where ks is a sequence of keys. Returns nil if the key " + 
						"is not present, or the not-found value if supplied.")
					.examples(
						"(get-in {:a 1 :b {:c 2 :d 3}} [:b :c])",
						"(get-in [:a :b :c] [0])",
						"(get-in [:a :b [:c :d :e]] [2 1])",
						"(get-in {:a 1 :b {:c [4 5 6]}} [:b :c 1])")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("get-in", args, 2, 3);
				
				VncCollection coll = Coerce.toVncCollection(args.first());
				VncSequence keys = Coerce.toVncSequence(args.second());
				VncVal key_not_found = (args.size() == 3) ? args.nth(2) : Nil;
				
				while(!keys.isEmpty()) {
					final VncVal key = keys.first();
					keys = keys.rest();
						
					if (Types.isVncMap(coll)) {
						final VncVal val = ((VncMap)coll).get(key);
						if (val == Nil) {
							return key_not_found;
						}
						else if (keys.isEmpty()) {
							return val;
						}
						else if (Types.isVncCollection(val)) {
							coll = (VncCollection)val;
						}
						else {
							return key_not_found;
						}
					}
					else {
						if (Types.isVncLong(key)) {
							final int index = ((VncLong)key).getValue().intValue();
							final VncVal val = ((VncSequence)coll).nthOrDefault(index, Nil);
							if (val == Nil) {
								return key_not_found;
							}
							else if (keys.isEmpty()) {
								return val;
							}
							else if (Types.isVncCollection(val)) {
								coll = (VncCollection)val;
							}
							else {
								return key_not_found;
							}
						}
						else {
							return key_not_found;
						}
					}
				}
				
				return key_not_found;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction find = 
		new VncFunction(
				"find", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(find map key)")		
					.doc("Returns the map entry for key, or nil if key not present.")
					.examples("(find {:a 1 :b 2} :b)", "(find {:a 1 :b 2} :z)")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("find", args, 2);
				
				if (args.first() == Nil) {
					return Nil;
				} 
				else {
					final VncMap mhm = Coerce.toVncMap(args.first());
					final VncVal key = args.second();
					
					final VncVal value = mhm.get(key);
					return value == Nil ? Nil : VncVector.of(key, value);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction key = 
		new VncFunction(
				"key", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(key e)")		
					.doc("Returns the key of the map entry.")
					.examples("(key (find {:a 1 :b 2} :b))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("key", args, 1);
				
				final VncSequence entry = Coerce.toVncSequence(args.first());
				return entry.first();
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction keys = 
		new VncFunction(
				"keys", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(keys map)")		
					.doc("Returns a collection of the map's keys.")
					.examples("(keys {:a 1 :b 2 :c 3})")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("keys", args, 1);
				
				return Coerce.toVncMap(args.first()).keys();
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction val = 
		new VncFunction(
				"val", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(val e)")		
					.doc("Returns the val of the map entry.")
					.examples("(val (find {:a 1 :b 2} :b))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("val", args, 1);
				
				final VncSequence entry = Coerce.toVncSequence(args.first());
				return entry.second();
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction vals = 
		new VncFunction(
				"vals", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(vals map)")		
					.doc("Returns a collection of the map's values.")
					.examples("(vals {:a 1 :b 2 :c 3})")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("vals", args, 1);
				
				final VncMap mhm = Coerce.toVncMap(args.first());
				return new VncList(mhm.getMap().values());
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction update = 
		new VncFunction(
				"update", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(update m k f)")		
					.doc(
						"Updates a value in an associative structure, where k is a " + 
						"key and f is a function that will take the old value " + 
						"return the new value. Returns a new structure.")
					.examples(
						"(update [] 0 (fn [x] 5))",
						"(update [0 1 2] 0 (fn [x] 5))",
						"(update [0 1 2] 0 (fn [x] (+ x 1)))",
						"(update {} :a (fn [x] 5))",
						"(update {:a 0} :b (fn [x] 5))",
						"(update {:a 0 :b 1} :a (fn [x] 5))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("update", args, 3);
				
				if (Types.isVncSequence(args.first())) {
					final VncSequence list = ((VncSequence)args.first());
					final int idx = Coerce.toVncLong(args.second()).getValue().intValue();
					final VncFunction fn = Coerce.toVncFunction(args.nth(2));
							
					if (idx < 0 || idx > list.size()) {
						throw new VncException(String.format(
								"Function 'update' index %d out of bounds",
								idx));
					}
					else if (idx < list.size()) {
						return list.setAt(idx, fn.apply(VncList.of(list.nth(idx))));
					}
					else {
						return list.addAtEnd(fn.apply(VncList.of(Nil)));
					}			
				}
				else if (Types.isVncMap(args.first())) {
					final VncMap map = ((VncMap)args.first());
					final VncVal key = args.second();
					final VncFunction fn = Coerce.toVncFunction(args.nth(2));
					return map.assoc(key, fn.apply(VncList.of(map.get(key))));
				}
				else {
					throw new VncException(String.format(
							"'update' does not allow %s as associative structure", 
							Types.getType(args.first())));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction update_BANG = 
		new VncFunction(
				"update!", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(update! m k f)")		
					.doc(
						"Updates a value in a mutable map, where k is a " + 
						"key and f is a function that will take the old value " + 
						"return the new value.")
					.examples(
						"(update! (mutable-map) :a (fn [x] 5))",
						"(update! (mutable-map :a 0) :b (fn [x] 5))",
						"(update! (mutable-map :a 0 :b 1) :a (fn [x] 5))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("update!", args, 3);
				
				if (Types.isVncMutableMap(args.first())) {
					final VncMutableMap map = (VncMutableMap)args.first();
					final VncVal key = args.second();
					final VncFunction fn = Coerce.toVncFunction(args.nth(2));
					return map.assoc(key, fn.apply(VncList.of(map.get(key))));
				}
				else {
					throw new VncException(String.format(
							"'update!' does not allow %s as map. It works with mutable maps only.", 
							Types.getType(args.first())));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	
	///////////////////////////////////////////////////////////////////////////
	// Sequence functions
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction split_at = 
		new VncFunction(
				"split-at", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(split-at n coll)")		
					.doc("Returns a vector of [(take n coll) (drop n coll)]")
					.examples(
						"(split-at 2 [1 2 3 4 5])",
						"(split-at 3 [1 2])")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("split-at", args, 2);
				
				if (args.second() == Nil) {
					return VncVector.of(new VncList(), new VncList());
				}
				
				final List<VncVal> items = Coerce.toVncSequence(args.second()).getList();
				final int n = Math.min(
								items.size(), 
								Math.max(
									0, 
									Coerce.toVncLong(args.first()).getValue().intValue()));
				
				return VncVector.of(
						new VncList(items.subList(0, n)), 
						new VncList(items.subList(n, items.size())));
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction split_with = 
		new VncFunction(
				"split-with", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(split-with pred coll)")		
					.doc("Splits the collection at the first false/nil predicate result in a vector with two lists")
					.examples(
						"(split-with odd? [1 3 5 6 7 9])",
						"(split-with odd? [1 3 5])",
						"(split-with odd? [2 4 6])")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("split-with", args, 2);
				
				if (args.second() == Nil) {
					return VncVector.of(new VncList(), new VncList());
				}
				
				final VncFunction pred = Coerce.toVncFunction(args.first());
				final VncSequence coll = Coerce.toVncSequence(args.second());
				
				final List<VncVal> items = coll.getList();
				int splitPos = items.size();
				
				// find splitPos
				for(int ii=0; ii<items.size(); ii++) {
					final VncVal val = coll.nth(ii);
					final VncVal match = pred.apply(VncList.of(val));
					if (match == False || match == Nil) {
						splitPos = ii;
						break;
					}				
				}
				
				if (splitPos == 0) {
					return VncVector.of(new VncList(), new VncList(items));
				}
				else if (splitPos < items.size()) {
					return VncVector.of(
								new VncList(items.subList(0, splitPos)), 
								new VncList(items.subList(splitPos, items.size())));
				}
				else {
					return VncVector.of(new VncList(items), new VncList());
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction into = 
		new VncFunction(
				"into", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(into to-coll from-coll)")		
					.doc(
						"Returns a new coll consisting of to-coll with all of the items of" + 
						"from-coll conjoined.")
					.examples(
						"(into (sorted-map) [ [:a 1] [:c 3] [:b 2] ] )",
						"(into (sorted-map) [ {:a 1} {:c 3} {:b 2} ] )",
						"(into [] {1 2, 3 4})",
						"(into '() '(1 2 3))",
						"(into [1 2 3] '(4 5 6))",
						"(into '() (bytebuf [0 1 2]))",
						"(into [] (bytebuf [0 1 2]))",
						"(into '() \"abc\")",
						"(into [] \"abc\")",
						"(into (sorted-map) {:b 2 :c 3 :a 1})",
						"(do\n" +
						"   (into (. :java.util.concurrent.CopyOnWriteArrayList :new)\n" +
						"         (doto (. :java.util.ArrayList :new)\n" +
						"               (. :add 3)\n" +
						"               (. :add 4))))\n",
						"(do\n" +
						"   (into (. :java.util.concurrent.CopyOnWriteArrayList :new)\n" +
						"         '(3 4)))\n")
					.build()
		) {		
			@SuppressWarnings("unchecked")
			public VncVal apply(final VncList args) {
				assertArity("into", args, 2);
				
				if (args.second() == Nil) {
					return args.first();
				}
		
				final VncCollection to = Coerce.toVncCollection(args.first());
	
				if (Types.isVncByteBuffer(args.second())) {
					final VncList byteList = ((VncByteBuffer)args.second()).toVncList();
				
					if (Types.isVncSequence(to)) {
						return ((VncSequence)to).addAllAtEnd(byteList);
					}
					else {
						throw new VncException(String.format(
								"Function 'into' does only allow list and vector as to-coll if from-coll " +
								"is a bytebuf"));
					}
				}
				else if (Types.isVncString(args.second())) {
					final VncList charList = ((VncString)args.second()).toVncList();
								
					if (Types.isVncSequence(to)) {
						return ((VncSequence)to).addAllAtEnd(charList);
					}
					else if (Types.isVncSet(to)) {
						return ((VncSet)to).addAll(charList);
					}
					else {
						throw new VncException(String.format(
								"Function 'into' does only allow list, vector, and set as to-coll if from-coll " +
								"is a string"));
					}
				}
	
	
				final VncCollection from = Coerce.toVncCollection(args.second());
				
				if (Types.isVncVector(to)) {
					return ((VncVector)to).addAllAtEnd(from.toVncList());
				}
				else if (Types.isVncList(to)) {
					return ((VncList)to).addAllAtStart(from.toVncList());
				}
				else if (Types.isVncHashSet(to) || Types.isVncSortedSet(to)) {
					return ((VncHashSet)to).addAll(from.toVncList());
				}
				else if (Types.isVncMap(to)) {
					if (Types.isVncSequence(from)) {
						VncMap toMap = (VncMap)to;					
						for(VncVal it : ((VncSequence)from).getList()) {
							if (Types.isVncSequence(it)) {
								toMap = ((VncMap)toMap).assoc(((VncSequence)it).toVncList());
							}
							else if (Types.isVncMap(it)) {
								toMap = ((VncMap)toMap).putAll((VncMap)it);
							}
						}
						
						return toMap;
					}
					else if (Types.isVncMap(from)) {
						return ((VncMap)to).putAll((VncMap)from);
					}
					else {
						throw new VncException(String.format(
								"Function 'into' does not allow %s as from-coll into a map", 
								Types.getType(from)));
					}
				}
				else if (Types.isVncJavaList(to)) {
					if (Types.isVncJavaList(from)) {
						List<Object> to_ = (List<Object>)((VncJavaList)to).getDelegate();
						List<Object> from_ = (List<Object>)((VncJavaList)from).getDelegate();
						to_.addAll(from_);
						return to;
					}
					if (Types.isVncJavaSet(from)) {
						List<Object> to_ = (List<Object>)((VncJavaList)to).getDelegate();
						Set<Object> from_ = (Set<Object>)((VncJavaSet)from).getDelegate();
						to_.addAll(from_);
						return to;
					}
					else {
						return ((VncJavaList)to).addAllAtEnd(from.toVncList());					
					}
				}
				else if (Types.isVncJavaSet(to)) {
					if (Types.isVncJavaSet(from)) {
						Set<Object> to_ = (Set<Object>)((VncJavaSet)to).getDelegate();
						Set<Object> from_ = (Set<Object>)((VncJavaSet)from).getDelegate();
						to_.addAll(from_);
						return to;
					}
					if (Types.isVncJavaList(from)) {
						Set<Object> to_ = (Set<Object>)((VncJavaSet)to).getDelegate();
						List<Object> from_ = (List<Object>)((VncJavaList)to).getDelegate();
						to_.addAll(from_);
						return to;
					}
					else {
						return ((VncJavaSet)to).addAll(from.toVncList());					
					}
				}
				else {
					throw new VncException(String.format(
							"Function 'into' does not allow %s as to-coll", 
							Types.getType(args.first())));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction sequential_Q = 
		new VncFunction(
				"sequential?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(sequential? obj)")		
					.doc("Returns true if obj is a sequential collection")
					.examples("(sequential? '(1))", 
						"(sequential? [1])", 
						"(sequential? {:a 1})", 
						"(sequential? nil)", 
						"(sequential? \"abc\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("sequential?", args, 1);
				
				return Types.isVncSequence(args.first()) ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction coll_Q = 
		new VncFunction(
				"coll?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(coll? obj)")		
					.doc("Returns true if obj is a collection")
					.examples("(coll? {:a 1})", "(coll? [1 2])")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("coll?", args, 1);
				
				return Types.isVncCollection(args.first()) ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction every_Q = 
		new VncFunction(
				"every?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(every? pred coll)")		
					.doc(
						"Returns true if the predicate is true for all collection items, " +
						"false otherwise")
					.examples(
						"(every? number? nil)",
						"(every? number? [])",
						"(every? number? [1 2 3 4])",
						"(every? number? [1 2 3 :a])",
						"(every? #(>= % 10) [10 11 12])")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("every?", args, 2);
				
				if (args.second() == Nil) {
					return False;
				}
				else {				
					final VncFunction pred = Coerce.toVncFunction(args.first());
					final VncCollection coll = Coerce.toVncCollection(args.second());
	
					if (coll.isEmpty()) {
						return False;
					}
					
					return coll.toVncList()
							   .getList()
							   .stream()
							   .allMatch(v -> pred.apply(VncList.of(v)) == True) ? True : False;
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction not_every_Q = 
		new VncFunction(
				"not-every?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(not-every? pred coll)")		
					.doc(
						"Returns false if the predicate is true for all collection items, " +
						"true otherwise")
					.examples(
						"(not-every? number? nil)",
						"(not-every? number? [])",
						"(not-every? number? [1 2 3 4])",
						"(not-every? number? [1 2 3 :a])",
						"(not-every? #(>= % 10) [10 11 12])")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("not-every?", args, 2);
					
				return every_Q.apply(args) == True ? False : True;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction any_Q = 
		new VncFunction(
				"any?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(any? pred coll)")		
					.doc(
						"Returns true if the predicate is true for at least one collection item, " +
						"false otherwise")
					.examples(
						"(any? number? nil)",
						"(any? number? [])",
						"(any? number? [1 :a :b])",
						"(any? number? [1 2 3])",
						"(any? #(>= % 10) [1 5 10])")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("any?", args, 2);
				
				if (args.second() == Nil) {
					return False;
				}
				else {
					final VncFunction pred = Coerce.toVncFunction(args.first());
					final VncCollection coll = Coerce.toVncCollection(args.second());
					
					if (coll.isEmpty()) {
						return False;
					}
									
					return coll.toVncList()
							   .getList()
							   .stream()
							   .anyMatch(v -> pred.apply(VncList.of(v)) == True) ? True : False;
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction not_any_Q = 
		new VncFunction(
				"not-any?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(not-any? pred coll)")		
					.doc(
						"Returns false if the predicate is true for at least one collection item, " +
						"true otherwise")
					.examples(
						"(not-any? number? nil)",
						"(not-any? number? [])",
						"(not-any? number? [1 :a :b])",
						"(not-any? number? [1 2 3])",
						"(not-any? #(>= % 10) [1 5 10])")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("not-any?", args, 2);
				
				return any_Q.apply(args) == True ? False : True;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction some = 
		new VncFunction(
				"some", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(some? pred coll)")		
					.doc(
						"Returns the first logical true value of (pred x) for any x in coll, " + 
						"else nil.")
					.examples(
						"(some even? '(1 2 3 4))",
						"(some even? '(1 3 5 7))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("some?", args, 2);
				
				if (args.second() == Nil) {
					return Nil;
				}
				else {
					final VncFunction pred = Coerce.toVncFunction(args.first());
					final VncCollection coll = Coerce.toVncCollection(args.second());
					
					if (coll.isEmpty()) {
						return Nil;
					}
									
					return coll.toVncList()
							   .getList()
							   .stream()
							   .anyMatch(v -> pred.apply(VncList.of(v)) == True) ? True : Nil;
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction count = 
		new VncFunction(
				"count", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(count coll)")		
					.doc(
						"Returns the number of items in the collection. (count nil) returns " + 
						"0. Also works on strings, and Java Collections")
					.examples(
						"(count {:a 1 :b 2})", 
						"(count [1 2])", 
						"(count \"abc\")")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("count", args, 1);
				
				final VncVal arg = args.first();
				if (arg == Nil) {
					return new VncLong(0L);
				} 
				else if (Types.isVncString(arg)) {
					return new VncLong(((VncString)arg).getValue().length());
				}
				else if (Types.isVncByteBuffer(arg)) {
					return new VncLong(((VncByteBuffer)arg).size());
				}
				else if (Types.isVncCollection(arg)) {
					return new VncLong(((VncCollection)arg).size());
				}
				else {
					throw new VncException(String.format(
							"Invalid argument type %s while calling function 'count'",
							Types.getType(arg)));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction empty = 
		new VncFunction(
				"empty", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(empty coll)")		
					.doc("Returns an empty collection of the same category as coll, or nil")
					.examples("(empty {:a 1})", "(empty [1 2])", "(empty '(1 2))")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("empty", args, 1);
	
				final VncVal coll = args.first();
				if (coll == Nil) {
					return Nil;
				} 
				else if (Types.isVncSequence(coll)) {
					return ((VncSequence)coll).empty();
				} 
				else if (Types.isVncMap(coll)) {
					return ((VncMap)coll).empty();
				} 
				else {
					throw new VncException(String.format(
							"Invalid argument type %s while calling function 'empty'",
							Types.getType(coll)));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction empty_Q = 
		new VncFunction(
				"empty?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(empty? x)")		
					.doc("Returns true if x is empty")
					.examples("(empty? {})", "(empty? [])", "(empty? '())", "(empty? \"\")")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("empty?", args, 1);
	
				final VncVal val = args.first();
				if (val == Nil) {
					return True;
				} 
				else if (Types.isVncString(val)) {
					return ((VncString)val).getValue().isEmpty() ? True : False;
				} 
				else if (Types.isVncCollection(val)) {
					return ((VncCollection)val).isEmpty() ? True : False;
				} 
				else if (Types.isVncByteBuffer(val)) {
					return ((VncByteBuffer)val).size() == 0 ? True : False;
				} 
				else {
					return False;
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction not_empty_Q = 
		new VncFunction(
				"not-empty?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(not-empty? x)")		
					.doc("Returns true if x is not empty")
					.examples("(not-empty? {:a 1})", "(not-empty? [1 2])", "(not-empty? '(1 2))")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("not-empty?", args, 1);
	
				final VncVal val = args.first();
				if (val == Nil) {
					return False;
				} 
				else if (Types.isVncString(val)) {
					return ((VncString)val).getValue().isEmpty() ? False : True;
				} 
				else if (Types.isVncCollection(val)) {
					return ((VncCollection)val).isEmpty() ? False : True;
				} 
				else if (Types.isVncByteBuffer(val)) {
					return ((VncByteBuffer)val).size() == 0 ? False : True;
				} 
				else {
					return True;
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction cons = 
		new VncFunction(
				"cons", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(cons x coll)")		
					.doc(
						"Returns a new collection where x is the first element and coll is " + 
						"the rest")
					.examples(
						"(cons 1 '(2 3 4 5 6))",
						"(cons [1 2] [4 5 6])",
						"(cons 3 (set 1 2))")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("cons", args, 2);

				final VncVal coll = args.second();

				if (Types.isVncVector(coll)) {
					return ((VncVector)coll).addAtStart(args.first());
				}
				if (Types.isVncList(coll)) {
					return ((VncList)coll).addAtStart(args.first());
				}
				else if (Types.isVncHashSet(coll)) {
					return ((VncHashSet)coll).add(args.first());
				}
				else if (Types.isVncMap(coll) && Types.isVncMap(args.first())) {
					return ((VncMap)coll).putAll((VncMap)args.first());
				}
				else {
					throw new VncException(String.format(
							"Invalid argument type %s while calling function 'cons'",
							Types.getType(coll)));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction concat = 
		new VncFunction(
				"concat", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(concat coll)", "(concat coll & colls)")		
					.doc( "Returns a collection of the concatenation of the elements " +
					"in the supplied colls.")
					.examples(
					"(concat [1 2])",
					"(concat [1 2] [4 5 6])",
					"(concat '(1 2))",
					"(concat '(1 2) [4 5 6])",
					"(concat {:a 1})",
					"(concat {:a 1} {:b 2 :c 3})",
					"(concat \"abc\")",
					"(concat \"abc\" \"def\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				final List<VncVal> result = new ArrayList<>();
				
				args.getList().forEach(val -> {
					if (val == Nil) {
						// skip
					}
					else if (Types.isVncString(val)) {
						final String str = ((VncString)val).getValue();
						for(char ch : str.toCharArray()) {
							result.add(new VncString(String.valueOf(ch)));
						}
					}
					else if (Types.isVncSequence(val)) {
						result.addAll(((VncSequence)val).getList());
					}
					else if (Types.isVncSet(val)) {
						result.addAll(((VncSet)val).getList());
					}
					else if (Types.isVncMap(val)) {
						result.addAll(((VncMap)val).toVncList().getList());
					}
					else {
						throw new VncException(String.format(
								"Invalid argument type %s while calling function 'concat'",
								Types.getType(val)));
					}
				});
				
				return new VncList(result);
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction interleave = 
		new VncFunction(
				"interleave", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(interleave c1 c2)", "(interleave c1 c2 & colls)")		
					.doc("Returns a collection of the first item in each coll, then the second etc.")
					.examples("(interleave [:a :b :c] [1 2])")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertMinArity("interleave", args, 2);
	
				int len = Coerce.toVncSequence(args.first()).size();
				final List<VncSequence> lists = new ArrayList<>();
				for(int ii=0; ii<args.size(); ii++) {
					final VncSequence l = Coerce.toVncSequence(args.nth(ii));
					lists.add(l);
					len = Math.min(len, l.size());				
				}
	
				final List<VncVal> result = new ArrayList<>();
				
				for(int nn=0; nn<len; nn++) {
					for(int ii=0; ii<lists.size(); ii++) {
						result.add(lists.get(ii).nth(nn));
					}
				}
						
				return new VncList(result);
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction interpose = 
		new VncFunction(
				"interpose", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(interpose sep coll)")		
					.doc("Returns a collection of the elements of coll separated by sep.")						
					.examples("(interpose \", \" [1 2 3])", "(apply str (interpose \", \" [1 2 3]))")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("interpose", args, 2);
	
				final VncVal sep = args.first();
				final VncSequence coll = Coerce.toVncSequence(args.second());
				
				final List<VncVal> result = new ArrayList<>();
		
				if (!coll.isEmpty()) {
					result.add(coll.first());
					coll.rest().forEach(v -> {
						result.add(sep);
						result.add(v);
					});
				}
							
				return new VncList(result);
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction first = 
		new VncFunction(
				"first", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(first coll)")		
					.doc("Returns the first element of coll.")
					.examples(
						"(first nil)",
						"(first [])",
						"(first [1 2 3])",
						"(first '())",
						"(first '(1 2 3))",
						"(first \"abc\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("first", args, 1);
	
				final VncVal val = args.first();
				if (val == Nil) {
					return Nil;
				}
				
				if (Types.isVncSequence(val)) {
					return ((VncSequence)val).first();
				}
				else if (Types.isVncString(val)) {
					return ((VncString)val).first();
				}
				else {
					throw new VncException(String.format(
							"Invalid argument type %s while calling function 'first'",
							Types.getType(val)));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction second = 
		new VncFunction(
				"second", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(second coll)")		
					.doc("Returns the second element of coll.")
					.examples(
						"(second nil)",
						"(second [])",
						"(second [1 2 3])",
						"(second '())",
						"(second '(1 2 3))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("second", args, 1);
	
	
				final VncVal val = args.first();
				if (val == Nil) {
					return Nil;
				}
				
				if (Types.isVncSequence(val)) {
					return ((VncSequence)val).second();
				}
				else if (Types.isVncString(val)) {
					return ((VncString)val).second();
				}
				else {
					throw new VncException(String.format(
							"Invalid argument type %s while calling function 'second'",
							Types.getType(val)));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction third = 
		new VncFunction(
				"third", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(third coll)")		
					.doc("Returns the third element of coll.")
					.examples(
						"(third nil)",
						"(third [])",
						"(second [1 2 3])",
						"(third '())",
						"(third '(1 2 3))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("third", args, 1);
	
	
				final VncVal val = args.first();
				if (val == Nil) {
					return Nil;
				}
				
				if (Types.isVncSequence(val)) {
					return ((VncSequence)val).third();
				}
				else if (Types.isVncString(val)) {
					return ((VncString)val).nth(2);
				}
				else {
					throw new VncException(String.format(
							"Invalid argument type %s while calling function 'third'",
							Types.getType(val)));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction nth = 
		new VncFunction(
				"nth", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(nth coll idx)")		
					.doc("Returns the nth element of coll.")
					.examples(
						"(nth nil 1)",
						"(nth [1 2 3] 1)",
						"(nth '(1 2 3) 1)",
						"(nth \"abc\" 2)")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("nth", args, 2);
	
				final int idx = Coerce.toVncLong(args.second()).getValue().intValue();
	
				final VncVal val = args.first();
				if (val == Nil) {
					return Nil;
				}
				
				if (Types.isVncSequence(val)) {
					return ((VncSequence)val).nth(idx);
				}
				else if (Types.isVncString(val)) {
					return ((VncString)val).nth(idx);
				}
				else {
					throw new VncException(String.format(
							"Invalid argument type %s while calling function 'nth'",
							Types.getType(val)));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction last = 
		new VncFunction(
				"last", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(last coll)")		
					.doc("Returns the last element of coll.")
					.examples(
						"(last nil)",
						"(last [])",
						"(last [1 2 3])",
						"(last '())",
						"(last '(1 2 3))",
						"(last \"abc\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("last", args, 1);
	
				final VncVal val = args.first();
				if (val == Nil) {
					return Nil;
				}
				
				if (Types.isVncSequence(val)) {
					return ((VncSequence)val).last();
				}
				else if (Types.isVncString(val)) {
					return ((VncString)val).last();
				}
				else {
					throw new VncException(String.format(
							"Invalid argument type %s while calling function 'last'",
							Types.getType(val)));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction rest = 
		new VncFunction(
				"rest", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(rest coll)")		
					.doc("Returns a collection with second to list element")
					.examples(
						"(rest nil)",
						"(rest [])",
						"(rest [1])",
						"(rest [1 2 3])",
						"(rest '())",
						"(rest '(1))",
						"(rest '(1 2 3))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("rest", args, 1);
	
				final VncVal coll = args.first();
				if (coll == Nil) {
					return Nil;
				}
				else if (Types.isVncVector(coll)) {
					return ((VncVector)coll).rest();
				}
				else if (Types.isVncList(coll)) {
					return ((VncList)coll).rest();
				}
				else if (Types.isVncJavaList(coll)) {
					return ((VncJavaList)coll).rest();
				}
				else {
					throw new VncException(String.format(
							"Invalid argument type %s while calling function 'rest'",
							Types.getType(args.first())));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction butlast = 
		new VncFunction(
				"butlast", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(butlast coll)")		
					.doc("Returns a collection with all but the last list element")
					.examples(
						"(butlast nil)",
						"(butlast [])",
						"(butlast [1])",
						"(butlast [1 2 3])",
						"(butlast '())",
						"(butlast '(1))",
						"(butlast '(1 2 3))")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("butlast", args, 1);
	
				final VncVal coll = args.first();
				if (coll == Nil) {
					return Nil;
				}
				else if (Types.isVncVector(coll)) {
					final VncVector vec = (VncVector)coll;
					return vec.size() > 1 ? vec.slice(0, vec.size()-1) : new VncVector(); 
				}
				else if (Types.isVncList(coll)) {
					final VncList list = (VncList)coll;
					return list.size() > 1 ? list.slice(0, list.size()-1) : new VncList(); 
				}
				else if (Types.isVncJavaList(coll)) {
					final VncList list = ((VncJavaList)coll).toVncList();
					return list.size() > 1 ? list.slice(0, list.size()-1) : new VncList(); 
				}
				else {
					throw new VncException(String.format(
							"Invalid argument type %s while calling function 'butlast'",
							Types.getType(args.first())));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction nfirst = 
		new VncFunction(
				"nfirst", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(nfirst coll n)")		
					.doc("Returns a collection of the first n items")
					.examples(
						"(nfirst nil 2)",
						"(nfirst [] 2)",
						"(nfirst [1] 2)",
						"(nfirst [1 2 3] 2)",
						"(nfirst '() 2)",
						"(nfirst '(1) 2)",
						"(nfirst '(1 2 3) 2)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("nfirst", args, 2);
	
				if (args.first() == Nil) {
					return new VncList();
				}
				else if (Types.isVncVector(args.first())) {
					final VncVector vec = Coerce.toVncVector(args.first());		
					final int n = Math.max(0, Math.min(vec.size(), Coerce.toVncLong(args.second()).getValue().intValue()));				
					return vec.isEmpty() 
							? new VncVector() 
							: new VncVector(vec.getList().subList(0, n));
				}
				else if (Types.isVncList(args.first()) || Types.isVncJavaList(args.first())) {
					final VncSequence list = Coerce.toVncSequence(args.first());		
					final int n = Math.max(0, Math.min(list.size(), Coerce.toVncLong(args.second()).getValue().intValue()));				
					return list.isEmpty() 
							? new VncList() 
							: new VncList(list.getList().subList(0, n));
				}
				else {
					throw new VncException(String.format(
							"nfirst: type %s not supported",
							Types.getType(args.first())));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction nlast = 
		new VncFunction(
				"nlast", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(nlast coll n)")		
					.doc("Returns a collection of the last n items")
					.examples(
						"(nlast nil 2)",
						"(nlast [] 2)",
						"(nlast [1] 2)",
						"(nlast [1 2 3] 2)",
						"(nlast '() 2)",
						"(nlast '(1) 2)",
						"(nlast '(1 2 3) 2)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("nlast", args, 2);
	
				if (args.first() == Nil) {
					return new VncList();
				}
				else if (Types.isVncVector(args.first())) {
					final VncVector vec = Coerce.toVncVector(args.first());		
					final int n = Math.max(0, Math.min(vec.size(), Coerce.toVncLong(args.second()).getValue().intValue()));				
					return vec.isEmpty() 
							? new VncVector() 
							: new VncVector(vec.getList().subList(vec.size()-n, vec.size()));
				}
				else if (Types.isVncList(args.first()) || Types.isVncJavaList(args.first())) {
					final VncList list = Coerce.toVncList(args.first());		
					final int n = Math.max(0, Math.min(list.size(), Coerce.toVncLong(args.second()).getValue().intValue()));				
					return list.isEmpty() 
							? new VncList() 
							: new VncList(list.getList().subList(list.size()-n, list.size()));
				}
				else {
					throw new VncException(String.format(
							"nlast: type %s not supported",
							Types.getType(args.first())));
				}
			}

	    private static final long serialVersionUID = -1848883965231344442L;
	};

	public static VncFunction distinct = 
		new VncFunction(
				"distinct", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(distinct coll)")		
					.doc("Returns a collection with all duplicates removed")
					.examples(
						"(distinct [1 2 3 4 2 3 4])",
						"(distinct '(1 2 3 4 2 3 4))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("distinct", args, 1);
	
				if (args.first() == Nil) {
					return new VncList();
				}
				
				return ((VncSequence)args.first()).withValues(
													Coerce
														.toVncSequence(args.first())
														.getList()
														.stream()
														.distinct()
														.collect(Collectors.toList()));
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction dedupe = 
		new VncFunction(
				"dedupe", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(dedupe coll)")		
					.doc("Returns a collection with all consecutive duplicates removed")
					.examples(
						"(dedupe [1 2 2 2 3 4 4 2 3])",
						"(dedupe '(1 2 2 2 3 4 4 2 3))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("dedupe", args, 1);
	
				if (args.first() == Nil) {
					return new VncList();
				}
				
				VncVal seen = null;
	
				final List<VncVal> items = new ArrayList<>();
	
				for(VncVal val : Coerce.toVncSequence(args.first()).getList()) {
					if (seen == null || !val.equals(seen)) {
						items.add(val);
						seen = val;
					}
				}
				
				return ((VncSequence)args.first()).withValues(items);
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction partition = 
		new VncFunction(
				"partition", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(partition n coll)", "(partition n step coll)", "(partition n step padcoll coll)")		
					.doc(
						"Returns a collection of lists of n items each, at offsets step " + 
						"apart. If step is not supplied, defaults to n, i.e. the partitions " + 
						"do not overlap. If a padcoll collection is supplied, use its elements as " + 
						"necessary to complete last partition upto n items. In case there are " + 
						"not enough padding elements, return a partition with less than n items.")
					.examples(
						"(partition 4 (range 20))",
						"(partition 4 6 (range 20))",
						"(partition 3 6 [\"a\"] (range 20))",
						"(partition 4 6 [\"a\" \"b\" \"c\" \"d\"] (range 20))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("partition", args, 2, 3 ,4);
	
				final int n = Coerce.toVncLong(args.first()).getValue().intValue();
				final int step = args.size() > 2 ? Coerce.toVncLong(args.second()).getValue().intValue() : n;
				final List<VncVal> padcoll = args.size() > 3 ? Coerce.toVncSequence(args.nth(2)).getList() : new ArrayList<>();
				final List<VncVal> coll = Coerce.toVncSequence(args.nth(args.size()-1)).getList();
				
				if (n <= 0) {
					throw new VncException(String.format(
							"partition: n must be a positive number"));
				}
				if (step <= 0) {
					throw new VncException(String.format(
							"partition: step must be a positive number"));
				}
				
				// split at 'step'
				final List<List<VncVal>> splits = new ArrayList<>();
				for (int ii=0; ii<coll.size(); ii += step) {			
					splits.add(coll.subList(ii, Math.min(ii + step, coll.size())));
				}
				
				VncList result = new VncList();
				for(List<VncVal> split : splits) {
					if (n == split.size()) {
						result = result.addAtEnd(new VncList(split));
					}
					else if (n < split.size()) {
						result = result.addAtEnd(new VncList(split.subList(0, n)));
					}
					else {
						final List<VncVal> split_ = new ArrayList<>(split);
						for(int ii=0; ii<(n-split.size()) && ii<padcoll.size(); ii++) {
							split_.add(padcoll.get(ii));
						}
						result = result.addAtEnd(new VncList(split_));
					}
				}
				return result;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction coalesce = 
		new VncFunction(
				"coalesce", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(coalesce args*)")		
					.doc("Returns the first non nil arg")
					.examples(
						"(coalesce )",
						"(coalesce 1 2)",
						"(coalesce nil)",
						"(coalesce nil 1 2)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				return args.stream()
						   .filter(v -> v != Nil)
						   .findFirst()
						   .orElse(Nil);
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction emptyToNil = 
		new VncFunction(
				"empty-to-nil", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(empty-to-nil x)")		
					.doc("Returns nil if x is empty")
					.examples(
						"(empty-to-nil \"\")",
						"(empty-to-nil [])",
						"(empty-to-nil '())",
						"(empty-to-nil {})")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("empty-to-nil", args, 1);
				
				final VncVal arg = args.first();
				if (Types.isVncString(arg)) {
					return ((VncString)arg).getValue().isEmpty() ? Nil : arg;
				}
				else if (Types.isVncVector(arg)) {
					return ((VncVector)arg).isEmpty() ? Nil : arg;
				}
				else if (Types.isVncList(arg)) {
					return ((VncList)arg).isEmpty() ? Nil : arg;
				}
				else if (Types.isVncMap(arg)) {
					return ((VncMap)arg).isEmpty() ? Nil : arg;
				}
				else {
					return arg;
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncFunction instance_Q = 
			new VncFunction(
					"instance?", 
					VncFunction
						.meta()
					.module("core")
						.arglists("(instance? type x)")		
						.doc("Returns true if x is an instance of the given type")
						.examples(
							"(instance? :venice.Long 500)",
							"(instance? :java.math.BigInteger 500)")
						.build()
			) {		
				public VncVal apply(final VncList args) {
					assertArity("instance?", args, 2);
					
					final VncKeyword type = Coerce.toVncKeyword(args.first());
					final VncVal x = args.second();
					
					return Types.isInstanceOf(type, x) ? True : False;
				}
		
			    private static final long serialVersionUID = -1848883965231344442L;
			};

	public static VncFunction pop = 
		new VncFunction(
				"pop", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(pop coll)")		
					.doc(
						"For a list, returns a new list without the first item, " + 
						"for a vector, returns a new vector without the last item.")
					.examples(
						"(pop '(1 2 3 4))",
						"(pop [1 2 3 4])")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("pop", args, 1);

				final VncVal val = args.first();
				if (val == Nil) {
					return Nil;
				}
	
				if (Types.isVncVector(val)) {
					final VncVector vec = (VncVector)val;
					return vec.size() < 2 ? new VncVector() : vec.slice(0, vec.size()-1);
				}
				else if (Types.isVncSequence(val)) {
					final VncSequence seq = (VncSequence)val;
					return seq.isEmpty() ? new VncList() : seq.rest();
				}
				else {
					throw new VncException(String.format(
							"pop: type %s not supported",
							Types.getType(args.first())));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction pop_BANG = 
		new VncFunction(
				"pop!", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(pop! stack)")		
					.doc("Pops an item from a stack.")
					.examples("(let [s (stack)]\n   (push! s 4)\n   (push! s 3)\n   (pop! s)\n   s)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("pop!", args, 1);

				final VncVal val = args.first();
				if (val == Nil) {
					return Nil;
				}
	
				if (Types.isVncStack(val)) {
					return ((VncStack)val).pop();
				}
				else {
					throw new VncException(String.format(
							"pop!: type %s not supported",
							Types.getType(args.first())));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction push_BANG = 
			new VncFunction(
					"push!", 
					VncFunction
						.meta()
						.module("core")
						.arglists("(push! stack v)")		
						.doc("Pushes an item to a stack.")
						.examples("(let [s (stack)]\n   (push! s 4)\n   (push! s 3)\n   (pop! s)\n   s)")
						.build()
			) {		
				public VncVal apply(final VncList args) {
					assertArity("push!", args, 2);

					final VncVal val = args.first();
					if (val == Nil) {
						return Nil;
					}
		
					if (Types.isVncStack(val)) {
						return ((VncStack)val).push(args.second());
					}
					else {
						throw new VncException(String.format(
								"push!: type %s not supported",
								Types.getType(args.first())));
					}
				}
		
			    private static final long serialVersionUID = -1848883965231344442L;
			};

	public static VncFunction offer_BANG = 
			new VncFunction(
					"offer!", 
					VncFunction
						.meta()
						.module("core")
						.arglists("(offer! queue v)", "(offer! queue timeout v)")		
						.doc("Offers an item to a queue with an optional timeout in milliseconds.")
						.examples("(let [s (queue)]\n   (offer! s 4)\n   (offer! s 3)\n   (poll! s)\n   s)")
						.build()
			) {		
				public VncVal apply(final VncList args) {
					assertArity("offer!", args, 2, 3);

					final VncVal val = args.first();
					if (val == Nil) {
						return Nil;
					}
		
					if (Types.isVncQueue(val)) {
						if (args.size() == 2) {
							return ((VncQueue)val).offer(args.second());
						}
						else {
							return ((VncQueue)val).offer(args.third(), Coerce.toVncLong(args.second()).getValue());
						}
					}
					else {
						throw new VncException(String.format(
								"offer!: type %s not supported",
								Types.getType(args.first())));
					}
				}
		
			    private static final long serialVersionUID = -1848883965231344442L;
			};

	public static VncFunction poll_BANG = 
			new VncFunction(
					"poll!", 
					VncFunction
						.meta()
						.module("core")
						.arglists("(poll! queue)", "(poll! queue timeout)")		
						.doc("Polls an item from a queue with an optional timeout in milliseconds.")
						.examples("(let [s (queue)]\n   (offer! s 4)\n   (offer! s 3)\n   (poll! s)\n   s)")
						.build()
			) {		
				public VncVal apply(final VncList args) {
					assertArity("poll!", args, 1, 2);

					final VncVal val = args.first();
					if (val == Nil) {
						return Nil;
					}
		
					if (Types.isVncQueue(val)) {
						if (args.size() == 1) {
							return ((VncQueue)val).poll();
						}
						else {
							return ((VncQueue)val).poll(Coerce.toVncLong(args.second()).getValue());
						}
					}
					else {
						throw new VncException(String.format(
								"poll!: type %s not supported",
								Types.getType(args.first())));
					}
				}
		
			    private static final long serialVersionUID = -1848883965231344442L;
			};

	public static VncFunction peek = 
		new VncFunction(
				"peek", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(peek coll)")		
					.doc("For a list, same as first, for a vector, same as last, for a stack the top element")
					.examples(
						"(peek '(1 2 3 4))",
						"(peek [1 2 3 4])",
						"(let [s (stack)]\n   (push! s 4)\n  (peek s))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("peek", args, 1);
				
				final VncVal val = args.first();
				if (val == Nil) {
					return Nil;
				}
	
				if (Types.isVncVector(val)) {
					final VncVector vec = (VncVector)val;
					return vec.isEmpty() ? Nil : vec.nth(vec.size()-1);
				}
				else if (Types.isVncSequence(val)) {
					final VncSequence seq = (VncSequence)val;
					return seq.isEmpty() ? Nil : seq.first();
				}
				else if (Types.isVncStack(val)) {
					return ((VncStack)val).peek();
				}
				else {
					throw new VncException(String.format(
							"peek: type %s not supported",
							Types.getType(args.first())));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction take_while = 
		new VncFunction(
				"take-while", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(take-while predicate coll)")		
					.doc(
						"Returns a list of successive items from coll while " + 
						"(predicate item) returns logical true.")
					.examples("(take-while neg? [-2 -1 0 1 2 3])")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("take-while", args, 2);
				
				final VncFunction predicate = Coerce.toVncFunction(args.first());
				final VncSequence coll = Coerce.toVncSequence(args.second());
				
				for(int i=0; i<coll.size(); i++) {
					final VncVal take = predicate.apply(VncList.of(coll.nth(i)));
					if (take == False) {
						return coll.slice(0, i);
					}
				}
				return coll;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction take = 
		new VncFunction(
				"take", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(take n coll)")		
					.doc(
						"Returns a collection of the first n items in coll, or all items if " + 
						"there are fewer than n.")
					.examples(
						"(take 3 [1 2 3 4 5])", 
						"(take 10 [1 2 3 4 5])")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("take", args, 2);
				
				final VncLong n = Coerce.toVncLong(args.first());
				final VncSequence coll = Coerce.toVncSequence(args.second());
	
				return coll.slice(0, (int)Math.min(n.getValue(), coll.size()));
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction drop_while = 
		new VncFunction(
				"drop-while", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(drop-while predicate coll)")		
					.doc(
						"Returns a list of the items in coll starting from the " + 
						"first item for which (predicate item) returns logical false.")
					.examples("(drop-while neg? [-2 -1 0 1 2 3])")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("drop-while", args, 2);
				
				final VncFunction predicate = Coerce.toVncFunction(args.first());
				final VncSequence coll = Coerce.toVncSequence(args.second());
				
				for(int i=0; i<coll.size(); i++) {
					final VncVal take = predicate.apply(VncList.of(coll.nth(i)));
					if (take == False) {
						return coll.slice(i);
					}
				}
				return coll.empty();
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction drop = 
		new VncFunction(
				"drop", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(drop n coll)")		
					.doc("Returns a collection of all but the first n items in coll")
					.examples("(drop 3 [1 2 3 4 5])", "(drop 10 [1 2 3 4 5])")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("drop", args, 2);
				
				final VncLong n = Coerce.toVncLong(args.first());
				final VncSequence coll = Coerce.toVncSequence(args.second());
	
				return coll.slice((int)Math.min(n.getValue()+1, coll.size()));
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction flatten = 
		new VncFunction(
				"flatten", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(flatten coll)")		
					.doc(
						"Takes any nested combination of collections (lists, vectors, " + 
						"etc.) and returns their contents as a single, flat sequence. " + 
						"(flatten nil) returns an empty list.")
					.examples("(flatten [])", "(flatten [[1 2 3] [4 5 6] [7 8 9]])")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("flatten", args, 1);
				
				final VncCollection coll = Coerce.toVncCollection(args.first());
				
				final List<VncVal> result = new ArrayList<>();
				flatten(coll, result);			
				return Types.isVncVector(coll) ? new VncVector(result) : new VncList(result);
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction reverse = 
		new VncFunction(
				"reverse", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(reverse coll)")		
					.doc("Returns a collection of the items in coll in reverse order")
					.examples("(reverse [1 2 3 4 5 6])")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("reverse", args, 1);
				
				final VncSequence coll = Coerce.toVncSequence(args.first());
				
				final List<VncVal> reversed = new ArrayList<>();
				for(int ii=coll.size()-1; ii>=0; ii--) {
					reversed.add(coll.nth(ii));
				}	
				
				return coll.withValues(reversed);
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction sort = 
		new VncFunction(
				"sort", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(sort coll)", "(sort comparefn coll)")		
					.doc(
						"Returns a sorted list of the items in coll. If no compare function " + 
						"comparefn is supplied, uses the natural compare. The compare function " + 
						"takes two arguments and returns -1, 0, or 1")
					.examples(
						"(sort [3 2 5 4 1 6])", 
						"(sort compare [3 2 5 4 1 6])", 
						"; reversed\n" +
						"(sort (comp (partial * -1) compare) [3 2 5 4 1 6])", 
						"(sort {:c 3 :a 1 :b 2})")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("sort", args, 1, 2);
	
				if (args.size() == 1) {
					// no compare function -> sort by natural order
					return sort(
							"sort", 
							args, 
							args.first(), 
							(x,y) -> Coerce.toVncLong(compare.apply(VncList.of(x,y))).getIntValue());
				}
				else if (args.size() == 2) {
					final VncFunction compfn = Coerce.toVncFunction(args.first());
					
					return sort(
							"sort", 
							args, 
							args.second(), 
							(x,y) -> Coerce.toVncLong(compfn.apply(VncList.of(x,y))).getIntValue());
				}
				else {
					throw new VncException("sort: args not supported");
				}			
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction sort_by = 
		new VncFunction(
				"sort-by", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(sort-by keyfn coll)", "(sort-by keyfn compfn coll)")		
					.doc(
						"Returns a sorted sequence of the items in coll, where the sort " + 
						"order is determined by comparing (keyfn item).  If no comparator is " + 
						"supplied, uses compare.")
					.examples(
						"(sort-by count [\"aaa\" \"bb\" \"c\"])", 
						"; reversed\n" +
						"(sort-by count (comp (partial * -1) compare) [\"aaa\" \"bb\" \"c\"])", 
						"(sort-by first [[1 2] [3 4] [2 3]])",
						"; reversed\n" +
						"(sort-by first (comp (partial * -1) compare) [[1 2] [3 4] [2 3]])",
						"(sort-by (fn [x] (get x :rank)) [{:rank 2} {:rank 3} {:rank 1}])",
						"; reversed\n" +
						"(sort-by (fn [x] (get x :rank)) (comp (partial * -1) compare) [{:rank 2} {:rank 3} {:rank 1}])")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("sort-by", args, 2, 3);
	
				if (args.size() == 2) {
					final VncFunction keyfn = Coerce.toVncFunction(args.first());
	
					return sort(
							"sort-by", 
							args, 
							args.second(), 
							(x,y) -> Coerce.toVncLong(
										compare.apply(
												VncList.of(
													keyfn.apply(VncList.of(x)),
													keyfn.apply(VncList.of(y))))
									 ).getIntValue());
				}
				else if (args.size() == 3) {
					final VncFunction keyfn = Coerce.toVncFunction(args.first());
					final VncFunction compfn = Coerce.toVncFunction(args.second());
	
					return sort(
							"sort-by", 
							args, 
							args.nth(2), 
							(x,y) -> Coerce.toVncLong(
										compfn.apply(
												VncList.of(
													keyfn.apply(VncList.of(x)),
													keyfn.apply(VncList.of(y)))
												)
									 ).getIntValue());
				}
				else {
					throw new VncException("sort-by: args not supported");
				}			
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction group_by = 
		new VncFunction(
				"group-by", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(group-by f coll)")		
					.doc(
						"Returns a map of the elements of coll keyed by the result of " + 
						"f on each element. The value at each key will be a vector of the " + 
						"corresponding elements, in the order they appeared in coll.")
					.examples(
						"(group-by count [\"a\" \"as\" \"asd\" \"aa\" \"asdf\" \"qwer\"])",
						"(group-by odd? (range 10))")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("group-by", args, 2);
	
				final VncFunction fn = Coerce.toVncFunction(args.first());
				final VncSequence coll = Coerce.toVncSequence(args.second());
	
				VncMap map = new VncOrderedMap();
				
				for(VncVal v : coll.getList()) {
					final VncVal key = fn.apply(VncList.of(v));
					final VncSequence val = Coerce.toVncSequence(map.getMap().get(key));
					if (val == null) {
						map = map.assoc(key, VncVector.of(v));
					}
					else {
						map = map.assoc(key, val.addAtEnd(v));
					}
				}
				
				return map;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	// General sequence functions
	public static VncFunction apply = 
		new VncFunction(
				"apply", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(apply f args* coll)")		
					.doc("Applies f to all arguments composed of args and coll")
					.examples(
						"(apply + [1 2 3])",
						"(apply + 1 2 [3 4 5])",
						"(apply str [1 2 3 4 5])")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				final VncFunction fn = Coerce.toVncFunction(args.first());
				final VncList fn_args = args.slice(1,args.size()-1);
				
				final VncVal coll = args.last();
				if (coll == Nil) {
					return fn.apply(fn_args.addAtEnd(Nil));
				}
				else {
					final VncSequence tailArgs = Coerce.toVncSequence(args.last());
					return fn.apply(fn_args.addAllAtEnd(tailArgs));				
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction comp = 
		new VncFunction(
				"comp", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(comp f*)")		
					.doc(
						"Takes a set of functions and returns a fn that is the composition " + 
						"of those fns. The returned fn takes a variable number of args, " + 
						"applies the rightmost of fns to the args, the next " + 
						"fn (right-to-left) to the result, etc. ")
					.examples(
						"((comp str +) 8 8 8)", 
						"(map (comp - (partial + 3) (partial * 2)) [1 2 3 4])", 
						"((reduce comp [(partial + 1) (partial * 2) (partial + 3)]) 100)",
						"(filter (comp not zero?) [0 1 0 2 0 3 0 4])", 
						"(do \n" +
						"   (def fifth (comp first rest rest rest rest)) \n" +
						"   (fifth [1 2 3 4 5]))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertMinArity("comp", args, 1);
				
				final List<VncFunction> fns = 
						args.getList()
							.stream()
							.map(v -> Coerce.toVncFunction(v))
							.collect(Collectors.toList());
				
				return new VncFunction() {
					public VncVal apply(final VncList args) {
						VncList args_ = args;
						VncVal result = Nil;
						for(int ii=fns.size()-1; ii>=0; ii--) {
							final VncFunction fn = fns.get(ii);
							result = fn.apply(args_);
							args_ = VncList.of(result);
						}
						return result;
					}
	
				    private static final long serialVersionUID = -1L;
				};
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction compare = 
		new VncFunction(
				"compare", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(compare x y)")		
					.doc(
						"Comparator. Returns -1, 0, or 1 when x is logically 'less than', " +
						"'equal to', or 'greater than' y. For list and vectors the longer " + 
						"sequence is always 'greater' regardless of its contents. " +
						"For sets and maps only the size of the collection is compared.")
					.examples(
						"(compare nil 0)", 
						"(compare 0 nil)", 
						"(compare 1 0)", 
						"(compare 1 1)", 
						"(compare 1M 2M)", 
						"(compare 1 nil)", 
						"(compare nil 1)", 
						"(compare \"aaa\" \"bbb\")", 
						"(compare [0 1 2] [0 1 2])", 
						"(compare [0 1 2] [0 9 2])", 
						"(compare [0 9 2] [0 1 2])", 
						"(compare [1 2 3] [0 1 2 3])", 
						"(compare [0 1 2] [3 4])")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("compare", args, 2);
	
				return new VncLong(args.first().compareTo(args.second()));				
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncFunction partial = 
		new VncFunction(
				"partial", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(partial f args*)")		
					.doc(
						"Takes a function f and fewer than the normal arguments to f, and " + 
						"returns a fn that takes a variable number of additional args. When " + 
						"called, the returned function calls f with args + additional args.")
					.examples(
						"((partial * 2) 3)", 
						"(map (partial * 2) [1 2 3 4])", 
						"(do \n" +
						"   (def hundred-times (partial * 100)) \n" +
						"   (hundred-times 5))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertMinArity("partial", args, 2);
				
				final VncFunction fn = Coerce.toVncFunction(args.first());
				final VncList fnArgs = args.rest();
				
				return new VncFunction() {
					public VncVal apply(final VncList args) {
						return fn.apply(fnArgs.addAllAtEnd(args));
					}
	
				    private static final long serialVersionUID = -1L;
				};
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction map = 
		new VncFunction(
				"map", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(map f coll colls*)")		
					.doc(
						"Applys f to the set of first items of each coll, followed by applying " + 
						"f to the set of second items in each coll, until any one of the colls " + 
						"is exhausted. Any remaining items in other colls are ignored. ")
					.examples(
						"(map inc [1 2 3 4])",
						"(map + [1 2 3 4] [10 20 30 40])")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				if (args.size() == 0) {
					return Nil;
				}
				else if (args.size() == 1) {
					final VncFunction fn = Coerce.toVncFunction(args.first());

					// return a transducer
					return new VncFunction() {
						public VncVal apply(final VncList args) {
							assertArity("map:transducer", args, 1);
							final VncFunction rf = Coerce.toVncFunction(args.first());

							return new VncFunction() {
								public VncVal apply(final VncList args) {
									assertArity("map:transducer", args, 1, 2, 3);
									if (args.size() == 0) {
										return rf.apply(new VncList());
									}
									else if (args.size() == 1) {
										final VncVal result = args.first();
										return rf.apply(VncList.of(result));
									}
									else {
										final VncVal result = args.first();
										final VncList inputs = args.slice(1);
										
										return rf.apply(VncList.of(result, fn.apply(inputs)));
									}
								}
				
							    private static final long serialVersionUID = -1L;
							};
						}
		
					    private static final long serialVersionUID = -1L;
					};
				}
				else {
					final VncFunction fn = Coerce.toVncFunction(args.first());
					final VncList lists = removeNilValues((VncList)args.rest());
					final List<VncVal> result = new ArrayList<>();
								
					if (lists.isEmpty()) {
						return Nil;
					}
					
					int index = 0;
					boolean hasMore = true;
					while(hasMore) {
						final List<VncVal> fnArgs = new ArrayList<>();
						
						for(int ii=0; ii<lists.size(); ii++) {
							final VncSequence nthList = Coerce.toVncSequence(lists.nth(ii));
							if (nthList.size() > index) {
								fnArgs.add(nthList.nth(index));
							}
							else {
								hasMore = false;
								break;
							}
						}
		
						if (hasMore) {
							final VncVal val = fn.apply(new VncList(fnArgs));
							result.add(val);			
							index += 1;
						}
					}
			
					return new VncList(result);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction mapv = 
		new VncFunction(
				"mapv", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(mapv f coll colls*)")		
					.doc(
						"Returns a vector consisting of the result of applying f " +
						"to the set of first items of each coll, followed by applying " + 
						"f to the set of second items in each coll, until any one of the colls " + 
						"is exhausted. Any remaining items in other colls are ignored. ")
					.examples(
						"(mapv inc [1 2 3 4])",
						"(mapv + [1 2 3 4] [10 20 30 40])")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				final VncFunction fn = Coerce.toVncFunction(args.first());
				final VncList lists = removeNilValues((VncList)args.rest());
				final List<VncVal> result = new ArrayList<>();
	
				if (lists.isEmpty()) {
					return Nil;
				}
				
				int index = 0;
				boolean hasMore = true;
				while(hasMore) {
					final List<VncVal> fnArgs = new ArrayList<>();
					
					for(int ii=0; ii<lists.size(); ii++) {
						final VncSequence nthList = Coerce.toVncSequence(lists.nth(ii));
						if (nthList.size() > index) {
							fnArgs.add(nthList.nth(index));
						}
						else {
							hasMore = false;
							break;
						}
					}
	
					if (hasMore) {
						result.add(fn.apply(new VncList(fnArgs)));			
						index += 1;
					}
				}
		
				return new VncVector(result);
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction keep = 
		new VncFunction(
				"keep", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(keep f coll)")		
					.doc(
						"Returns a sequence of the non-nil results of (f item). Note, " + 
						"this means false return values will be included. f must be " + 
						"free of side-effects.")
					.examples(
						"(keep even? (range 1 4))",
						"(keep (fn [x] (if (odd? x) x)) (range 4))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("keep", args, 2);
				
				// use 'map' to apply the mapping function
				final VncVal result = map.apply(args);
	
				return result == Nil ? Nil : removeNilValues(Coerce.toVncList(result));
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction docoll = 
		new VncFunction(
				"docoll", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(docoll f coll)")		
					.doc(
						"Applies f to the items of the collection presumably for side effects. " +
						"Returns nil. ")
					.examples(
						"(docoll #(println %) [1 2 3 4])",
						"(docoll \n" +
						"    (fn [[k v]] (println (pr-str k v)))  \n" +
						"    {:a 1 :b 2 :c 3 :d 4})")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("docoll", args, 2);
	
				final VncFunction fn = Coerce.toVncFunction(args.first());
				final VncVal coll = args.second();
				
				if (coll == Nil) {
					// ok do nothing
				}
				else if (Types.isVncSequence(coll)) {
					((VncSequence)coll).forEach(v -> fn.apply(VncList.of(v)));
				}
				else if (Types.isVncMap(coll)) {
					((VncMap)coll).entries().forEach(v -> fn.apply(VncList.of(VncVector.of(v.getKey(), v.getValue()))));
				}
				else {
					throw new VncException(String.format(
							"docoll: collection type %s not supported",
							Types.getType(coll)));
				}
					
				return Nil;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction mapcat = 
		new VncFunction(
				"mapcat", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(mapcat fn & colls)")		
					.doc(
						"Returns the result of applying concat to the result of applying map " + 
						"to fn and colls. Thus function fn should return a collection.")
					.examples(
						"(mapcat reverse [[3 2 1 0] [6 5 4] [9 8 7]])")
					.build()
		) {		
			public VncVal apply(final VncList args) {			
				return concat.apply(Coerce.toVncList(map.apply(args)));
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction map_invert = 
		new VncFunction(
				"map-invert", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(map-invert m)")		
					.doc(
						"Returns the map with the vals mapped to the keys.")
					.examples(
						"(map-invert {:a 1 :b 2 :c 3})")
					.build()
		) {		
			public VncVal apply(final VncList args) {			
				assertArity("map-invert", args, 1);

				final VncMap m = Coerce.toVncMap(args.first());
				
				final Map<VncVal,VncVal> inverted = new HashMap<>();
				for(VncMapEntry e : m.entries()) {
					inverted.put(e.getValue(), e.getKey());
				}
				return m.withValues(inverted, m.getMeta());
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction filter = 
		new VncFunction(
				"filter", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(filter predicate coll)")		
					.doc(
						"Returns a collection of the items in coll for which " + 
						"(predicate item) returns logical true. ")
					.examples(
						"(filter even? [1 2 3 4 5 6 7])")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("filter", args, 1, 2);
				
				final VncFunction predicate = Coerce.toVncFunction(args.first());
				
				if (args.size() == 1) {
					// return a transducer
					return new VncFunction() {
						public VncVal apply(final VncList args) {
							assertArity("filter:transducer", args, 1);
							final VncFunction rf = Coerce.toVncFunction(args.first());

							return new VncFunction() {
								public VncVal apply(final VncList args) {
									assertArity("filter:transducer", args, 1, 2, 3);
									if (args.size() == 0) {
										return rf.apply(new VncList());
									}
									else if (args.size() == 1) {
										final VncVal result = args.first();
										return rf.apply(VncList.of(result));
									}
									else {
										final VncVal result = args.first();
										final VncVal input = args.second();
										
										final VncVal cond = predicate.apply(VncList.of(input));
										return (cond != False && cond != Nil)
													? rf.apply(VncList.of(result, input))
													: result;
									}
								}
				
							    private static final long serialVersionUID = -1L;
							};
						}
		
					    private static final long serialVersionUID = -1L;
					};
				}
				else {
					final VncSequence coll = Coerce.toVncSequence(args.second());
		
					final List<VncVal> items = new ArrayList<>();
					
					for(int i=0; i<coll.size(); i++) {
						final VncVal val = coll.nth(i);
						final VncVal keep = predicate.apply(VncList.of(val));
						if (keep != False && keep != Nil) {
							items.add(val);
						}
					}
					
					return coll.withValues(items);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction remove = 
		new VncFunction(
				"remove", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(remove predicate coll)")		
					.doc(
						"Returns a collection of the items in coll for which " + 
						"(predicate item) returns logical false. ")
					.examples(
						"(remove even? [1 2 3 4 5 6 7])")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("remove", args, 2);
				
				final VncFunction predicate = Coerce.toVncFunction(args.first());
				final VncSequence coll = Coerce.toVncSequence(args.second());
	
				final List<VncVal> items = new ArrayList<>();
				for(int i=0; i<coll.size(); i++) {
					final VncVal val = coll.nth(i);
					final VncVal keep = predicate.apply(VncList.of(val));
					if (keep == False) {
						items.add(val);
					}				
				}
				
				return coll.withValues(items);
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction reduce = 
		new VncFunction(
				"reduce", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(reduce f coll)", "(reduce f val coll)")		
					.doc(
						"f should be a function of 2 arguments. If val is not supplied, " + 
						"returns the result of applying f to the first 2 items in coll, then " + 
						"applying f to that result and the 3rd item, etc. If coll contains no " + 
						"items, f must accept no arguments as well, and reduce returns the " + 
						"result of calling f with no arguments.  If coll has only 1 item, it " + 
						"is returned and f is not called.  If val is supplied, returns the " + 
						"result of applying f to val and the first item in coll, then " + 
						"applying f to that result and the 2nd item, etc. If coll contains no " + 
						"items, returns val and f is not called.")
					.examples(
						"(reduce (fn [x y] (+ x y)) [1 2 3 4 5 6 7])",
						"(reduce (fn [x y] (+ x y)) 10 [1 2 3 4 5 6 7])",
						"((reduce comp [(partial + 1) (partial * 2) (partial + 3)]) 100)",
						"(reduce (fn [m [k v]] (assoc m v k)) {} {:b 2 :a 1 :c 3})",
						"(reduce (fn [m c] (assoc m (first c) c)) {} [[:a 1] [:b 2] [:c 3]])")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("reduce", args, 2, 3);
				
				final boolean twoArguments = args.size() < 3;
				final VncFunction reduceFn = Coerce.toVncFunction(args.first());
	
				if (twoArguments) {
					List<VncVal> coll;
					
					if (Types.isVncSequence(args.second())) {
						coll = Coerce.toVncSequence(args.second()).getList();
					}
					else if (Types.isVncMap(args.second())) {
						coll = Coerce.toVncMap(args.second()).toVncList().getList();
					}
					else {
						throw new VncException(String.format(
								"Function 'reduce' does not allow %s as coll parameter", 
								Types.getType(args.second())));
					}
					
					if (coll.isEmpty()) {
						return reduceFn.apply(new VncList());
					}
					else {
						VncVal value = coll.get(0);
						for(int ii=1; ii<coll.size(); ii++) {
							value = reduceFn.apply(VncList.of(value, coll.get(ii)));
						}
						return value;
					}
				}
				else {
					List<VncVal> coll;
					
					if (Types.isVncSequence(args.nth(2))) {
						coll = Coerce.toVncSequence(args.nth(2)).getList();
					}
					else if (Types.isVncMap(args.nth(2))) {
						coll = Coerce.toVncMap(args.nth(2)).toVncList().getList();
					}
					else {
						throw new VncException(String.format(
								"Function 'reduce' does not allow %s as coll parameter", 
								Types.getType(args.nth(2))));
					}
					
					if (coll.isEmpty()) {
						return args.second();
					}
					else if (coll.size() == 1) {
						return reduceFn.apply(VncList.of(args.second(), coll.get(0)));
					}
					else {
						VncVal value = args.second();
						for(int ii=0; ii<coll.size(); ii++) {
							value = reduceFn.apply(VncList.of(value, coll.get(ii)));
						}
						return value;
					}
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction reduce_kv = 
		new VncFunction(
				"reduce-kv", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(reduce-kv f init coll))")		
					.doc(
						"Reduces an associative collection. f should be a function of 3 " + 
						"arguments. Returns the result of applying f to init, the first key " + 
						"and the first value in coll, then applying f to that result and the " + 
						"2nd key and value, etc. If coll contains no entries, returns init " + 
						"and f is not called. Note that reduce-kv is supported on vectors, " + 
						"where the keys will be the ordinals.")
					.examples(
						"(reduce-kv (fn [x y z] (assoc x z y)) {} {:a 1 :b 2 :c 3})")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("reduce-kv", args, 3);
				
				final VncFunction reduceFn = Coerce.toVncFunction(args.first());		
				final List<VncMapEntry> values = Coerce.toVncHashMap(args.nth(2)).entries();
				
				VncMap value = (VncMap)args.second();
				
				if (values.isEmpty()) {
					return value;
				}
				else {
					for(VncMapEntry entry : values) {
						final VncVal key = entry.getKey();
						final VncVal val = entry.getValue();
						
						value = Coerce.toVncMap(reduceFn.apply(VncList.of(value, key, val)));
					}
					
					return value;
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction merge = 
		new VncFunction(
				"merge", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(merge & maps)")		
					.doc(
						"Returns a map that consists of the rest of the maps conj-ed onto " +
						"the first.  If a key occurs in more than one map, the mapping from " +
						"the latter (left-to-right) will be the mapping in the result.")
					.examples(
						"(merge {:a 1 :b 2 :c 3} {:b 9 :d 4})",
						"(merge {:a 1} nil)",
						"(merge nil {:a 1})",
						"(merge nil nil)")
					.build()
		) {
			public VncVal apply(final VncList args) {	
				assertMinArity("merge", args, 1);
				
				// remove Nil		
				final List<VncVal> maps = args.stream()
											  .filter(v -> v != Nil)
											  .collect(Collectors.toList());
				
				if (maps.isEmpty()) {
					return Nil;
				}
				else {
					final Map<VncVal,VncVal> map = new HashMap<>();
					maps.stream().forEach(v -> map.putAll(Coerce.toVncMap(v).getMap()));
					return new VncHashMap(map);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction conj = 
		new VncFunction(
				"conj", 
				VncFunction
					.meta()
					.module("core")
					.arglists(
						"(conj)", 
						"(conj x)", 
						"(conj coll x)", 
						"(conj coll x & xs)")		
					.doc(
						"Returns a new collection with the x, xs " + 
						"'added'. (conj nil item) returns (item).  The 'addition' may " + 
						"happen at different 'places' depending on the concrete type.")
					.examples(
						"(conj [1 2 3] 4)",
						"(conj '(1 2 3) 4)",
						"(conj (set 1 2 3) 4)",
						"(conj )",
						"(conj 4)")
					.build()
		) {
			public VncVal apply(final VncList args) {
				if (args.isEmpty()) {
					return new VncVector();
				}
				else if (args.size() == 1) {
					return args.first();
				}
				else {
					VncVal coll = args.first();
					if (coll == Nil) {
						coll = new VncList();
					}
	
					if (Types.isVncVector(coll)) {
						return ((VncVector)coll).addAllAtEnd(args.rest());
					} 
					else if (Types.isVncList(coll)) {
						return ((VncList)coll).addAllAtStart(args.rest());
					}
					else if (Types.isVncSet(coll)) {
						return ((VncSet)coll).addAll(args.rest());
					}
					else if (Types.isVncMap(coll)) {
						final VncMap map = (VncMap)coll;			
						final VncVal second = args.second();
						if (Types.isVncVector(second) && ((VncVector)second).size() == 2) {
							return map.assoc(
										VncList.of(
											((VncVector)second).first(),
											((VncVector)second).second()));
						}
						else if (Types.isVncMap(second)) {
							return map.putAll((VncMap)second);
						}
						else {
							throw new VncException(String.format(
									"Invalid x %s while calling function 'conj'",
									Types.getType(args.second())));
						}
					}
					else {
						throw new VncException(String.format(
								"Invalid coll %s while calling function 'conj'",
								Types.getType(coll)));
					}
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction disj = 
		new VncFunction(
				"disj", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(disj coll x)", "(disj coll x & xs)")		
					.doc( "Returns a new set with the x, xs removed.")
					.examples("(disj (set 1 2 3) 3)")
					.build()
		) {
			public VncVal apply(final VncList args) {			
				assertMinArity("disj", args, 2);
				
				if (args.first() instanceof VncHashSet) {
					return ((VncHashSet)args.first()).removeAll(args.rest());
				}
				else {
					throw new VncException(String.format(
							"Invalid coll %s while calling function 'disj'",
							Types.getType(args.first())));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction seq = 
		new VncFunction(
				"seq", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(seq coll)")		
					.doc(
						"Returns a seq on the collection. If the collection is " + 
						"empty, returns nil. (seq nil) returns nil. seq also works on " + 
						"Strings.")
					.examples(
						"(seq nil)",
						"(seq [1 2 3])",
						"(seq '(1 2 3))",
						"(seq {:a 1 :b 2})",
						"(seq \"abcd\")")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("seq", args, 1);
	
				final VncVal val = args.first();
				if (Types.isVncMap(val)) {
					if (((VncMap)val).isEmpty()) { 
						return Nil; 
					}
					return new VncList(
							((VncMap)val)
								.entries()
								.stream()
								.map(e -> VncVector.of(e.getKey(), e.getValue()))
								.collect(Collectors.toList()));
				} 
				if (Types.isVncVector(val)) {
					return ((VncVector)val).isEmpty() ? Nil : ((VncVector)val).toVncList();
				} 
				else if (Types.isVncList(val)) {
					return ((VncList)val).isEmpty() ? Nil :  val;
				} 
				else if (Types.isVncString(val)) {
					final String s = ((VncString)val).getValue();
					if (s.length() == 0) { 
						return Nil; 
					}
					
					final List<VncVal> lst = new ArrayList<VncVal>();
					for (char c : s.toCharArray()) {
						lst.add(new VncString(String.valueOf(c)));
					}
					return new VncList(lst);
				} 
				else if (val == Nil) {
					return Nil;
				} 
				else {
					throw new VncException("seq: called on non-sequence");
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction repeat = 
		new VncFunction(
				"repeat", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(repeat n x)")		
					.doc("Returns a collection with the value x repeated n times")
					.examples("(repeat 5 [1 2])")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("repeat", args, 2);
					
				final long repeat = Coerce.toVncLong(args.first()).getValue();
				if (repeat < 0) {
					throw new VncException("repeat: a count n must be grater or equal to 0");	
				}
	
				final VncVal val = args.second();
				final List<VncVal> values = new ArrayList<>();
				for(int ii=0; ii<repeat; ii++) {
					values.add(val);
				}			
				return new VncList(values);
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction repeatedly = 
		new VncFunction(
				"repeatedly", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(repeatedly n fn)")		
					.doc(
						"Takes a function of no args, presumably with side effects, and " + 
						"returns a collection of n calls to it")
					.examples(
						"(repeatedly 5 #(rand-long 11))",
						";; compare with repeat, which only calls the 'rand-long'\n" +
						";; function once, repeating the value five times. \n" +
						"(repeat 5 (rand-long 11))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("repeatedly", args, 2);
	
				
				final long repeat = Coerce.toVncLong(args.first()).getValue();
				final VncFunction fn = Coerce.toVncFunction(args.second());
				
				if (repeat < 0) {
					throw new VncException("repeatedly: a count n must be grater or equal to 0");	
				}
	
				final List<VncVal> values = new ArrayList<>();
				for(int ii=0; ii<repeat; ii++) {
					values.add(fn.apply(new VncList()));
				}			
				return new VncList(values);
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	

	
	///////////////////////////////////////////////////////////////////////////
	// Meta functions
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction meta = 
		new VncFunction(
				"meta", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(meta obj)")		
					.doc("Returns the metadata of obj, returns nil if there is no metadata.")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("meta", args, 1);
	
				return args.first().getMeta();
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction with_meta = 
		new VncFunction(
				"with-meta", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(with-meta obj m)")		
					.doc("Returns a copy of the object obj, with a map m as its metadata.")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("meta", args, 2);
	
				return args.first().withMeta(Coerce.toVncMap(args.second()));
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction vary_meta = 
		new VncFunction(
				"vary-meta", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(vary-meta obj f & args)")		
					.doc("Returns a copy of the object obj, with (apply f (meta obj) args) as its metadata.")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertMinArity("vary-meta", args, 2);
	
				final VncVal obj = args.first();
				final VncVal meta = obj.getMeta();
				final VncFunction fn = Coerce.toVncFunction(args.second());
				final VncList fnArgs = args.slice(2).addAtStart(meta == Nil ? new VncHashMap() : meta);
				
				return obj.withMeta(fn.apply(fnArgs));
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};


	

	///////////////////////////////////////////////////////////////////////////
	// Utilities
	///////////////////////////////////////////////////////////////////////////


	public static VncFunction gensym = 
		new VncFunction(
				"gensym", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(gensym)", "(gensym prefix)")		
					.doc("Generates a symbol.")
					.examples("(gensym )", "(gensym \"prefix_\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("gensym", args, 0, 1);
				
				final String prefix = args.isEmpty() 
										? "G__" 
										: Types.isVncSymbol(args.first())
											? Coerce.toVncSymbol(args.first()).getName()
											: Coerce.toVncString(args.first()).getValue();
				
				return new VncSymbol(prefix + String.valueOf(gensymValue.incrementAndGet()));
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction name = 
		new VncFunction(
				"name", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(name x)")		
					.doc("Returns the name String of a string, symbol, keyword, or function/macro.")
					.examples(
						"(name :x)",
						"(name 'x)",
						"(name \"x\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("name", args, 1);
				
				final VncVal arg = args.first();
				
				if (arg == Nil) {
					return Nil;
				}
				else if (Types.isVncKeyword(arg)) {
					return new VncString(((VncKeyword)arg).getValue());
				}
				else if (Types.isVncSymbol(arg)) {
					return new VncString(((VncSymbol)arg).getName());
				}
				else if (Types.isVncString(arg)) {
					return arg;
				}
				else if (Types.isVncFunction(arg)) {
					return new VncString(((VncFunction)arg).getName());
				}
				else {
					throw new VncException(String.format(
							"Function 'name' does not allow %s as parameter", 
							Types.getType(arg)));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncFunction module = 
		new VncFunction(
				"module", 
				VncFunction
					.meta()
					.module("module")
					.arglists("(module fn)")		
					.doc("Returns the module a function/macro has been defined in.")
					.examples("(module +)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("module", args, 1);
				
				final VncVal arg = args.first();
				
				if (Types.isVncFunction(arg)) {
					return new VncString(((VncFunction)arg).getModule());
				}
				else {
					throw new VncException(String.format(
							"Function 'module' does not allow %s as parameter", 
							Types.getType(arg)));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction type = 
		new VncFunction(
				"type", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(type x)")		
					.doc("Returns the type of x.")
					.examples(
						"(type 5)",
						"(type [1 2])",
						"(type (. :java.math.BigInteger :valueOf 100))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("type", args, 1);
				return Types.getType(args.first());
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction transduce = 
		new VncFunction(
				"transduce", 
				VncFunction
					.meta()
					.module("core")
					.arglists(
						"(transduce xform f coll)", 
						"(transduce xform f init coll)")		
					.doc("todo")
					.examples()
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("transduce", args, 3, 4);
				
				// --------------------------------------------------------------
				//              W O R K   I N   P R O G R E S S
				// --------------------------------------------------------------
				
				// (def xf (map #(+ % 1)))
				// (transduce xf + [1 2 3 4])  ;; => 14
				// (transduce xf conj [1 2 3 4])  ;; => [2 3 4 5]
	
				// Returns the result of applying (the transformed) xf to init and 
				// the first item in coll, then applying xf to that result and the 
				// 2nd item, etc. If coll contains no items, returns init and f is 
				// not called. Note that certain transforms may inject or skip items.
				 
				final VncFunction xform = Coerce.toVncFunction(args.first());
				final VncFunction f = Coerce.toVncFunction(args.second());
				final VncVal init = args.size() == 4
										? args.third()
										: f.apply(new VncList());
				final VncSequence coll = args.size() == 4
										? (VncSequence)args.fourth()
										: (VncSequence)args.third();
				
				if (coll.isEmpty()) {
					return init;
				}
				else {
					final VncFunction tf = (VncFunction)xform.apply(VncList.of(f));
	
					VncVal ret = init;

					for(VncVal v : coll.getList()) {
						ret = tf.apply(VncList.of(ret, v));
						if (Types.isVncJavaObject(ret, Reduced.class)) {
							ret = Coerce.toVncJavaObject(ret, Reduced.class).deref();
							break;
						}
					}
					
					// cleanup
					return tf.apply(VncList.of(ret));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction java_obj_Q = 
		new VncFunction(
				"java-obj?", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(java-obj? obj)")		
					.doc("Returns true if obj is a Java object")
					.examples("(java-obj? (. :java.math.BigInteger :new \"0\"))")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("java-obj?", args, 1);
				
				return Types.isVncJavaObject(args.first()) ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncFunction java_enumeration_to_list = 
		new VncFunction(
				"java-enumeration-to-list", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(java-enumeration-to-list e)")		
					.doc("Converts a Java enumeration to a list")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("java-enumeration-to-list", args, 1);
				
				if (Types.isVncJavaObject(args.first(), Enumeration.class)) {
					final Enumeration<?> e = (Enumeration<?>)Coerce.toVncJavaObject(args.first()).getDelegate();
					final List<VncVal> list = StreamUtil
													 .stream(e)
													 .map(v -> JavaInteropUtil.convertToVncVal(v))
													 .collect(Collectors.toList());
					return new VncList(list); 
				}
				else {
					throw new VncException(String.format(
							"Function 'java-enumeration-to-list' does not allow %s as parameter", 
							Types.getType(args.first())));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction java_iterator_to_list = 
		new VncFunction(
				"java-iterator-to-list", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(java-iterator-to-list e)")		
					.doc("Converts a Java iterator to a list")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("java-iterator-to-list", args, 1);
				
				if (Types.isVncJavaObject(args.first(), Iterator.class)) {
					final Iterator<?> i = (Iterator<?>)Coerce.toVncJavaObject(args.first()).getDelegate();
					final List<VncVal> list = StreamUtil
													.stream(i)
													.map(v -> JavaInteropUtil.convertToVncVal(v))
													.collect(Collectors.toList());
					return new VncList(list); 
				}
				else {
					throw new VncException(String.format(
							"Function 'java-iterator-to-list' does not allow %s as parameter", 
							Types.getType(args.first())));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction java_wrap = 
		new VncFunction(
				"java-wrap", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(java-wrap val)")		
					.doc("Wraps a venice value")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("java-wrap", args, 1);
				
				final VncVal arg = args.first();
				
				return arg instanceof VncTunnelAsJavaObject 
						? arg 
						: new VncTunnelAsJavaObject(arg);
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction java_unwrap = 
		new VncFunction(
				"java-unwrap", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(java-unwrap val)")		
					.doc("Unwraps a venice value")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("java-unwrap", args, 1);
				
				final VncVal arg = args.first();
				
				return arg instanceof VncTunnelAsJavaObject
						? ((VncTunnelAsJavaObject)arg).getDelegate()
						: arg;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
		
		
	private static void flatten(final VncVal value, final List<VncVal> result) {
		if (Types.isVncSequence(value)) {
			Coerce.toVncSequence(value).forEach(v -> flatten(v, result));
		}
		else if (Types.isVncMap(value)) {
			((VncMap)value).entries().forEach(e -> {
				result.add(e.getKey());
				flatten(e.getValue(), result);
			});
		}
		else {
			result.add(value);
		}
	}
	
	private static VncVal sort(
			final String fnName, 
			final VncVal fnArgs, 
			final VncVal coll, 
			final Comparator<VncVal> c
	) {
		if (Types.isVncVector(coll)) {
			return new VncVector(
					((VncVector)coll)
						.getList()
						.stream()
						.sorted(c)
						.collect(Collectors.toList()));
		}
		else if (Types.isVncSequence(coll)) {
			return new VncList(
					((VncSequence)coll)
						.getList()
						.stream()
						.sorted(c)
						.collect(Collectors.toList()));
		}
		else if (Types.isVncSet(coll)) {
			return new VncList(
					((VncSet)coll)
						.getList()
						.stream()
						.sorted(c)
						.collect(Collectors.toList()));
		}
		else if (Types.isVncMap(coll)) {
			return new VncList(
					 ((VncMap)coll)
					 	.toVncList()
						.getList()
						.stream()
						.sorted(c)
						.collect(Collectors.toList()));
		}
		else {
			throw new VncException(String.format(
					"%s: collection type %s not supported",
					fnName, Types.getType(coll)));
		}
	}

	private static String encoding(final VncVal enc) {
		return enc == Nil 
				? "UTF-8" 
				: Types.isVncKeyword(enc)
					? Coerce.toVncKeyword(enc).getValue()
					: Coerce.toVncString(enc).getValue();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap.Builder()
				.put("throw",				throw_ex)
				
				.put("nil?",				nil_Q)
				.put("some?",				some_Q)
				.put("true?",				true_Q)
				.put("false?",				false_Q)
				.put("boolean?",			boolean_Q)
				.put("int?",				int_Q)
				.put("long?",				long_Q)
				.put("double?",				double_Q)
				.put("decimal?",			decimal_Q)
				.put("number?",				number_Q)
				.put("bytebuf?",			bytebuf_Q)
				.put("string?",				string_Q)
				.put("symbol",				symbol)
				.put("symbol?",				symbol_Q)
				.put("keyword",				keyword)
				.put("keyword?",			keyword_Q)
				.put("fn?",					fn_Q)
				.put("macro?",				macro_Q)
		
				.put("pr-str",				pr_str)
				.put("str",					str)
				.put("readline",			readline)
				.put("read-string",			read_string)
				
				.put("==",					equal_Q)
				.put("!=",					not_equal_Q)
				.put("<",					lt)
				.put("<=",					lte)
				.put(">",					gt)
				.put(">=",					gte)

				.put("match?",				match_Q)
				.put("not-match?",			not_match_Q)

				// deprecated (just for compatibility)
				.put("match",				match_Q)
				.put("match-not",			not_match_Q)
				
				.put("boolean",				boolean_cast)
				.put("int",					int_cast)
				.put("long",				long_cast)
				.put("double",				double_cast)
				.put("decimal",				decimal_cast)
				.put("bytebuf",				bytebuf_cast)
				.put("bytebuf-to-string",	bytebuf_to_string)
				.put("bytebuf-from-string",	bytebuf_from_string)
				
				.put("list",				new_list)
				.put("list*",				new_list_ASTERISK)
				.put("list?",				list_Q)
				.put("vector",				new_vector)
				.put("vector?",				vector_Q)
				.put("map?",				map_Q)
				.put("hash-map?",			hash_map_Q)
				.put("ordered-map?",		ordered_map_Q)
				.put("sorted-map?",			sorted_map_Q)
				.put("mutable-map?",		mutable_map_Q)
				.put("stack?",				stack_Q)
				.put("queue?",				queue_Q)
				.put("hash-map",			new_hash_map)
				.put("ordered-map",			new_ordered_map)
				.put("sorted-map",			new_sorted_map)
				.put("mutable-map",			new_mutable_map)
				.put("stack",				new_stack)
				.put("queue",				new_queue)
				.put("assoc",				assoc)
				.put("assoc!",				assoc_BANG)
				.put("assoc-in",			assoc_in)
				.put("dissoc",				dissoc)
				.put("dissoc!",				dissoc_BANG)
				.put("contains?", 			contains_Q)
				.put("find",				find)
				.put("get",					get)
				.put("get-in",				get_in)
				.put("key",					key)
				.put("keys",				keys)
				.put("val",					val)
				.put("vals",				vals)
				.put("update",				update)
				.put("update!",				update_BANG)
				.put("subvec",				subvec)
				.put("bytebuf-sub",			bytebuf_sub)
				.put("empty",				empty)

				.put("set?",				set_Q)
				.put("sorted-set?",			sorted_set_Q)
				.put("set",					new_set)
				.put("sorted-set",			new_sorted_set)
				.put("difference",			difference)
				.put("union",				union)
				.put("intersection",		intersection)

				.put("split-at",			split_at)
				.put("split-with",			split_with)
				.put("into",				into)
				.put("sequential?",			sequential_Q)
				.put("coll?",				coll_Q)
				.put("cons",				cons)
				.put("co",					cons)
				.put("concat",				concat)
				.put("interpose",			interpose)
				.put("interleave",			interleave)
				.put("mapcat",				mapcat)
				.put("map-invert",			map_invert)
				.put("keep",				keep)
				.put("docoll",				docoll)
				.put("nth",					nth)
				.put("first",				first)
				.put("second",				second)
				.put("third",				third)
				.put("last",				last)
				.put("rest",				rest)
				.put("butlast",				butlast)
				.put("nfirst",				nfirst)
				.put("nlast",				nlast)
				.put("empty-to-nil",		emptyToNil)
				.put("pop",					pop)
				.put("pop!",				pop_BANG)
				.put("push!",				push_BANG)
				.put("poll!",				poll_BANG)
				.put("offer!",				offer_BANG)
				.put("peek",				peek)
				.put("empty?",				empty_Q)
				.put("not-empty?",			not_empty_Q)
				.put("every?",				every_Q)
				.put("not-every?",			not_every_Q)
				.put("any?",				any_Q)
				.put("not-any?",			not_any_Q)
				.put("count",				count)
				.put("compare",				compare)
				.put("apply",				apply)
				.put("comp",				comp)
				.put("partial",				partial)
				.put("map",					map)
				.put("mapv",				mapv)
				.put("filter",				filter)
				.put("distinct",			distinct)
				.put("dedupe",				dedupe)
				.put("partition",			partition)
				.put("remove",				remove)
				.put("reduce",				reduce)
				.put("reduce-kv",			reduce_kv)
				.put("take",				take)
				.put("take-while",			take_while)
				.put("drop",				drop)
				.put("drop-while",			drop_while)
				.put("flatten",				flatten)
				.put("reverse",				reverse)
				.put("replace",				replace)
				.put("group-by",			group_by)
				.put("sort",				sort)
				.put("sort-by",				sort_by)
				.put("some",				some)
		
				.put("merge",				merge)
				.put("conj",				conj)
				.put("disj",				disj)
				.put("seq",					seq)
				.put("repeat",				repeat)
				.put("repeatedly",			repeatedly)
				
				.put("transduce",			transduce)
		
				.put("meta",				meta)
				.put("with-meta",			with_meta)
				.put("vary-meta",			vary_meta)
				
				.put("coalesce",			coalesce)
				
				.put("gensym",				gensym)
				.put("name",				name)
				.put("module",				module)
				.put("type",				type)
				.put("instance?",			instance_Q)	

				.put("java-obj?",					java_obj_Q)	
				.put("java-iterator-to-list",		java_iterator_to_list)	
				.put("java-enumeration-to-list",	java_enumeration_to_list)	
				.put("java-wrap",					java_wrap)	
				.put("java-unwrap",					java_unwrap)	
					
				.toMap();

	
	private static final AtomicLong gensymValue = new AtomicLong(0);
}
