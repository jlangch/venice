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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.github.jlangch.venice.ContinueException;
import com.github.jlangch.venice.EofException;
import com.github.jlangch.venice.ValueException;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.ModuleLoader;
import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.Reader;
import com.github.jlangch.venice.impl.Readline;
import com.github.jlangch.venice.impl.types.Coerce;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.Types;
import com.github.jlangch.venice.impl.types.VncBigDecimal;
import com.github.jlangch.venice.impl.types.VncByteBuffer;
import com.github.jlangch.venice.impl.types.VncDouble;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncThreadLocal;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncCollection;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncJavaList;
import com.github.jlangch.venice.impl.types.collections.VncJavaMap;
import com.github.jlangch.venice.impl.types.collections.VncJavaObject;
import com.github.jlangch.venice.impl.types.collections.VncJavaSet;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncOrderedMap;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.collections.VncSet;
import com.github.jlangch.venice.impl.types.collections.VncSortedMap;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.util.ErrorMessage;


public class CoreFunctions {

	///////////////////////////////////////////////////////////////////////////
	// Errors/Exceptions
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction throw_ex = new VncFunction("throw") {
		{
			setArgLists("(throw)", "(throw x)");
			
			setDoc("Throws exception with passed value x");
			
			setExamples(
					"(do                                                     \n" +
					"   (import :com.github.jlangch.venice.ValueException)   \n" +
					"   (try                                                 \n" +
					"     (throw 100)                                        \n" +
					"     (catch :ValueException ex (:value ex))))             ",
					
					"(do                                                     \n" +
					"   (import :java.lang.Exception)                        \n" +
					"   (try                                                 \n" +
					"      (throw [100 {:a 3}])                              \n" +
					"      (catch :Exception ex (:value ex))                 \n" +
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
					"      (catch :RuntimeException ex (:message (:cause ex)))))          "
					);
		}
		
		public VncVal apply(final VncList args) {
			if (args.isEmpty()) {
				throw new ValueException(Constants.Nil);
			}
			else if (Types.isVncJavaObject(args.nth(0))) {
				final Object obj = ((VncJavaObject)args.nth(0)).getDelegate();
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
				throw new ValueException(args.nth(0));
			}
		}
	};


	///////////////////////////////////////////////////////////////////////////
	// Scalar functions
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction nil_Q = new VncFunction("nil?") {
		{
			setArgLists("(nil? x)");
			
			setDoc("Returns true if x is nil, false otherwise");
			
			setExamples(
					"(nil? nil)",
					"(nil? 0)",
					"(nil? false)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("nil?", args, 1);
			
			return args.nth(0) == Nil ? True : False;
		}
	};

	public static VncFunction some_Q = new VncFunction("some?") {
		{
			setArgLists("(some? x)");
			
			setDoc("Returns true if x is not nil, false otherwise");
			
			setExamples(
					"(some? nil)",
					"(some? 0)",
					"(some? 4.0)",
					"(some? false)",
					"(some? [])",
					"(some? {})");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("some?", args, 1);
			
			return args.nth(0) == Nil ? False : True;
		}
	};

	public static VncFunction true_Q = new VncFunction("true?") {
		{
			setArgLists("(true? x)");
			
			setDoc("Returns true if x is true, false otherwise");
			
			setExamples(
					"(true? true)",
					"(true? false)",
					"(true? nil)",
					"(true? 0)",
					"(true? (== 1 1))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("true?", args, 1);
			
			return args.nth(0) == True ? True : False;
		}
	};

	public static VncFunction false_Q = new VncFunction("false?") {
		{
			setArgLists("(false? x)");
			
			setDoc("Returns true if x is false, false otherwise");
			
			setExamples(
					"(false? true)",
					"(false? false)",
					"(false? nil)",
					"(false? 0)",
					"(false? (== 1 2))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("false?", args, 1);
			
			return args.nth(0) == False ? True : False;
		}
	};
	
	public static VncFunction boolean_Q = new VncFunction("boolean?") {
		{
			setArgLists("(boolean? n)");
			
			setDoc("Returns true if n is a boolean");
			
			setExamples(
					"(boolean? true)",
					"(boolean? false)",
					"(boolean? nil)",
					"(boolean? 0)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("boolean?", args, 1);
			
			return args.nth(0) == True || args.nth(0) == False ? True : False;
		}
	};
	
	public static VncFunction long_Q = new VncFunction("long?") {
		{
			setArgLists("(long? n)");
			
			setDoc("Returns true if n is a long");
			
			setExamples(
					"(long? 4)",
					"(long? 3.1)",
					"(long? true)",
					"(long? nil)",
					"(long? {})");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("long?", args, 1);

			return Types.isVncLong(args.nth(0)) ? True : False;
		}
	};
	
	public static VncFunction double_Q = new VncFunction("double?") {
		{
			setArgLists("(double? n)");
			
			setDoc("Returns true if n is a double");
			
			setExamples(
					"(double? 4.0)",
					"(double? 3)",
					"(double? true)",
					"(double? nil)",
					"(double? {})");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("double?", args, 1);
			
			return Types.isVncDouble(args.nth(0)) ? True : False;
		}
	};
	
	public static VncFunction decimal_Q = new VncFunction("decimal?") {
		{
			setArgLists("(decimal? n)");
			
			setDoc("Returns true if n is a decimal");
			
			setExamples(
					"(decimal? 4.0M)",
					"(decimal? 4.0)",
					"(decimal? 3)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("decimal?", args, 1);
			
			return Types.isVncBigDecimal(args.nth(0)) ? True : False;
		}
	};
	
	public static VncFunction number_Q = new VncFunction("number?") {
		{
			setArgLists("(number? n)");
			
			setDoc("Returns true if n is a number (long, double, or decimal)");
			
			setExamples(
					"(number? 4.0M)",
					"(number? 4.0)",
					"(number? 3)",
					"(number? true)",
					"(number? \"a\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("number?", args, 1);
			
			return Types.isVncLong(args.nth(0)) 
					|| Types.isVncDouble(args.nth(0))
					|| Types.isVncBigDecimal(args.nth(0))? True : False;
		}
	};

	public static VncFunction string_Q = new VncFunction("string?") {
		{
			setArgLists("(string? x)");
			
			setDoc("Returns true if x is a string");
			
			setExamples(
					"(bytebuf? (bytebuf [1 2]))",
					"(bytebuf? [1 2])",
					"(bytebuf? nil)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("string?", args, 1);
			
			if (args.nth(0) instanceof VncKeyword) { 
				return False; 
			}
			if (args.nth(0) instanceof VncString) { 
				return True; 
			}
			else {
				return False;
			}
		}
	};

	public static VncFunction symbol = new VncFunction("symbol") {
		{
			setArgLists("(symbol name)");
			
			setDoc("Returns a symbol from the given name");
			
			setExamples(
					"(symbol \"a\")",
					"(symbol 'a)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("symbol", args, 1);

			if (Types.isVncSymbol(args.nth(0))) {
				return args.nth(0);
			} 
			else if (Types.isVncString(args.nth(0))) {
				return new VncSymbol((VncString)args.nth(0));
			} 
			else {
				throw new VncException(String.format(
						"Function 'symbol' does not allow %s name. %s",
						Types.getClassName(args.nth(0)),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};
	
	public static VncFunction symbol_Q = new VncFunction("symbol?") {
		{
			setArgLists("(symbol? x)");
			
			setDoc("Returns true if x is a symbol");
			
			setExamples(
					"(symbol? (symbol \"a\"))",
					"(symbol? 'a)",
					"(symbol? nil)",
					"(symbol? :a)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("symbol?", args, 1);
			
			return Types.isVncSymbol(args.nth(0)) ? True : False;
		}
	};

	public static VncFunction keyword = new VncFunction("keyword") {
		{
			setArgLists("(keyword name)");
			
			setDoc("Returns a keyword from the given name");
			
			setExamples(
					"(keyword \"a\")",
					"(keyword :a)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("keyword", args, 1);
			
			if (Types.isVncKeyword(args.nth(0))) {
				return args.nth(0);
			} 
			else if (Types.isVncString(args.nth(0))) {
				return new VncKeyword(((VncString)args.nth(0)).getValue());
			} 
			else {
				throw new VncException(String.format(
						"Function 'keyword' does not allow %s name. %s",
						Types.getClassName(args.nth(0)),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};
	
	public static VncFunction keyword_Q = new VncFunction("keyword?") {
		{
			setArgLists("(keyword? x)");
			
			setDoc("Returns true if x is a keyword");
			
			setExamples(
					"(keyword? (keyword \"a\"))",
					"(keyword? :a)",
					"(keyword? nil)",
					"(keyword? 'a)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("keyword?", args, 1);
			
			return Types.isVncKeyword(args.nth(0)) ? True : False;
		}
	};
	
	public static VncFunction fn_Q = new VncFunction("fn?") {
		{
			setArgLists("(fn? x)");
			
			setDoc("Returns true if x is a function");
			
			setExamples(
					"(do \n   (def sum (fn [x] (+ 1 x)))\n   (fn? sum))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("fn?", args, 1);
			
			if (!Types.isVncFunction(args.nth(0))) { 
				return False; 
			}
			return ((VncFunction)args.nth(0)).isMacro() ? False : True;
		}
	};
	
	public static VncFunction macro_Q = new VncFunction("macro?") {
		{
			setArgLists("(macro? x)");
			
			setDoc("Returns true if x is a macro");
			
			setExamples("(macro? and)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("macro?", args, 1);
			
			if (!Types.isVncFunction(args.nth(0))) { 
				return False; 
			}
			return ((VncFunction)args.nth(0)).isMacro() ? True : False;
		}
	};


	///////////////////////////////////////////////////////////////////////////
	// String functions
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction pr_str = new VncFunction("pr-str") {
		{
			setArgLists("(pr-str & xs)");
			
			setDoc( "With no args, returns the empty string. With one arg x, returns " + 
					"x.toString(). With more than one arg, returns the concatenation " +
					"of the str values of the args with delimiter ' '.");
			
			setExamples("(pr-str )", "(pr-str 1 2 3)");
		}
		
		public VncVal apply(final VncList args) {
			return args.isEmpty()
					? new VncString("")
					: new VncString(
							args.getList()
								.stream()
								.map(v -> Printer._pr_str(v, true))
								.collect(Collectors.joining(" ")));
		}
	};

	public static VncFunction str = new VncFunction("str") {
		{
			setArgLists("(str & xs)");
			
			setDoc( "With no args, returns the empty string. With one arg x, returns " + 
					"x.toString(). (str nil) returns the empty string. With more than " + 
					"one arg, returns the concatenation of the str values of the args.");
			
			setExamples("(str )", "(str 1 2 3)");
		}
		
		public VncVal apply(final VncList args) {
			return args.isEmpty()
					? new VncString("")
					: new VncString(
							args.getList()
								.stream()
								.filter(v -> v != Nil)
								.map(v -> Printer._pr_str(v, false))
								.collect(Collectors.joining("")));
		}
	};

	public static VncFunction readline = new VncFunction("readline") {
		{
			setArgLists("(readline prompt)");
			
			setDoc("Reads the next line from stdin. The function is sandboxed");
		}
	
		public VncVal apply(final VncList args) {			
			final String prompt = Coerce.toVncString(args.nth(0)).getValue();
			try {
				return new VncString(Readline.readline(prompt));
			} 
			catch (IOException ex) {
				throw new ValueException(new VncString(ex.getMessage()), ex);
			} 
			catch (EofException e) {
				return Nil;
			}
		}
	};

	public static VncFunction read_string = new VncFunction("read-string") {
		{
			setArgLists("(read-string x)");
			
			setDoc("Reads from x");
		}
		
		public VncVal apply(final VncList args) {
			try {
				assertArity("read-string", args, 1);

				return Reader.read_str(Coerce.toVncString(args.nth(0)).getValue(), null);
			} 
			catch (ContinueException c) {
				return Nil;
			}
		}
	};

	public static VncFunction loadCoreModule = new VncFunction("load-core-module") {
		{
			setArgLists("(load-core-module name)");
			
			setDoc("Loads a Venice extension module.");
		}
		
		public VncVal apply(final VncList args) {
			try {	
				assertArity("load-core-module", args, 1);
				
				final VncVal name = args.first();
				
				if (Types.isVncString(name)) {
					final String module = ModuleLoader.load(((VncString)args.first()).getValue());
					return new VncString(module);
				}
				else if (Types.isVncSymbol(name)) {
					final String module = ModuleLoader.load(((VncSymbol)args.first()).getName());
					return new VncString(module);
				}
				else {
					return Nil;
				}
			} 
			catch (Exception ex) {
				throw new VncException(ex.getMessage(), ex);
			}
		}
	};

	public static VncFunction loadClasspathVenice = new VncFunction("load-classpath-venice") {
		public VncVal apply(final VncList args) {
			try {	
				assertArity("load-classpath-venice", args, 1);
				
				final VncVal name = args.first();
				
				if (Types.isVncString(name)) {
					final String res = ModuleLoader.loadVeniceResource(((VncString)args.first()).getValue());
					return res == null ? Nil : new VncString(res);
				}
				else if (Types.isVncKeyword(name)) {
					final String res = ModuleLoader.loadVeniceResource(((VncKeyword)args.first()).getValue());
					return res == null ? Nil : new VncString(res);
				}
				else if (Types.isVncSymbol(name)) {
					final String res = ModuleLoader.loadVeniceResource(((VncSymbol)args.first()).getName());
					return res == null ? Nil : new VncString(res);
				}
				else {
					return Nil;
				}
			} 
			catch (Exception ex) {
				throw new VncException(ex.getMessage(), ex);
			}
		}
	};

	
	///////////////////////////////////////////////////////////////////////////
	// Number functions
	///////////////////////////////////////////////////////////////////////////	

	public static VncFunction equal_Q = new VncFunction("==") {
		{
			setArgLists("(== x y)");
			
			setDoc("Returns true if both operands have the equivalent type");
			
			setExamples("(== 0 0)", "(== 0 1)", "(== 0 0.0)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("==", args, 2);
			
			return Types._equal_Q(args.nth(0), args.nth(1)) ? True : False;
		}
	};

	public static VncFunction not_equal_Q = new VncFunction("!=") {
		{
			setArgLists("(!= x y)");
			
			setDoc("Returns true if both operands do not have the equivalent type");
			
			setExamples("(!= 0 1)", "(!= 0 0)", "(!= 0 0.0)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("!=", args, 2);
			
			return Types._equal_Q(args.nth(0), args.nth(1)) ? False : True;
		}
	};

	public static VncFunction match_Q = new VncFunction("match") {
		{
			setArgLists("(match s regex)");
			
			setDoc("Returns true if the string s matches the regular expression regex");
			
			setExamples("(match \"1234\" \"[0-9]+\")", "(match \"1234ss\" \"[0-9]+\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("match", args, 2);
			
			if (!Types.isVncString(args.nth(0))) {
				throw new VncException(String.format(
						"Invalid first argument type %s while calling function 'match'. %s",
						Types.getClassName(args.nth(0)),
						ErrorMessage.buildErrLocation(args)));
			}
			if (!Types.isVncString(args.nth(1))) {
				throw new VncException(String.format(
						"Invalid second argument type %s while calling function 'match'. %s",
						Types.getClassName(args.nth(1)),
						ErrorMessage.buildErrLocation(args)));
			}

			return Types._match_Q(args.nth(0), args.nth(1)) ? True : False;
		}
	};

	public static VncFunction match_not_Q = new VncFunction("match-not") {
		{
			setArgLists("(match-not s regex)");
			
			setDoc("Returns true if the string s does not match the regular expression regex");
			
			setExamples("(match-not \"1234\" \"[0-9]+\")", "(match-not \"1234ss\" \"[0-9]+\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("match-not", args, 2);
			
			if (!Types.isVncString(args.nth(0))) {
				throw new VncException(String.format(
						"Invalid first argument type %s while calling function 'match-not'. %s",
						Types.getClassName(args.nth(0)),
						ErrorMessage.buildErrLocation(args)));
			}
			if (!Types.isVncString(args.nth(1))) {
				throw new VncException(String.format(
						"Invalid second argument type %s while calling function 'match-not'. %s",
						Types.getClassName(args.nth(1)),
						ErrorMessage.buildErrLocation(args)));
			}
			
			return Types._match_Q(args.nth(0), args.nth(1)) ? False : True;
		}
	};

	public static VncFunction lt = new VncFunction("<") {
		{
			setArgLists("(< x y)");
			
			setDoc("Returns true if x is smaller than y");
			
			setExamples("(< 2 3)", "(< 2 3.0)", "(< 2 3.0M)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("<", args, 2);

			final VncVal op1 = args.nth(0);
			final VncVal op2 = args.nth(1);
			
			if (Types.isVncLong(op1) || Types.isVncDouble(op1) || Types.isVncBigDecimal(op1)) {
				return op1.compareTo(op2) < 0 ? True : False;
			}
			else if (Types.isVncString(op1)) {
				if (!Types.isVncString(op2)) {
					throw new VncException(String.format(
							"Function '<' with operand 1 of type %s does not allow %s as operand 2. %s", 
							Types.getClassName(op1),
							Types.getClassName(op2),
							ErrorMessage.buildErrLocation(args)));
				}

				final String s1 = ((VncString)op1).getValue();
				final String s2 = ((VncString)op2).getValue();
				return s1.compareTo(s2) < 0 ? True : False;
			}
			else {
				throw new VncException(String.format(
										"Function '<' does not allow %s as operand 1. %s", 
										Types.getClassName(op1),
										ErrorMessage.buildErrLocation(args)));
			}
		}
	};
	
	public static VncFunction lte = new VncFunction("<=") {
		{
			setArgLists("(<= x y)");
			
			setDoc("Returns true if x is smaller or equal to y");
			
			setExamples("(<= 2 3)", "(<= 3 3)", "(<= 2 3.0)", "(<= 2 3.0M)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("<=", args, 2);

			final VncVal op1 = args.nth(0);
			final VncVal op2 = args.nth(1);
			
			if (Types.isVncLong(op1) || Types.isVncDouble(op1) || Types.isVncBigDecimal(op1)) {
				return op1.compareTo(op2) <= 0 ? True : False;
			}
			else if (Types.isVncString(op1)) {
				if (!Types.isVncString(op2)) {
					throw new VncException(String.format(
							"Function '<=' with operand 1 of type %s does not allow %s as operand 2. %s", 
							Types.getClassName(op1),
							Types.getClassName(op2),
							ErrorMessage.buildErrLocation(args)));
				}

				final String s1 = ((VncString)op1).getValue();
				final String s2 = ((VncString)op2).getValue();
				return s1.compareTo(s2) <= 0 ? True : False;
			}
			else {
				throw new VncException(String.format(
										"Function '<=' does not allow %s as operand 1. %s", 
										Types.getClassName(op1),
										ErrorMessage.buildErrLocation(args)));
			}
		}
	};
	
	public static VncFunction gt = new VncFunction(">") {
		{
			setArgLists("(> x y)");
			
			setDoc("Returns true if x is greater than y");
			
			setExamples("(> 3 2)", "(> 3 3)", "(> 3.0 2)", "(> 3.0M 2)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity(">", args, 2);

			final VncVal op1 = args.nth(0);
			final VncVal op2 = args.nth(1);
			
			if (Types.isVncLong(op1) || Types.isVncDouble(op1) || Types.isVncBigDecimal(op1)) {
				return op1.compareTo(op2) > 0 ? True : False;
			}
			else if (Types.isVncString(op1)) {
				if (!Types.isVncString(op2)) {
					throw new VncException(String.format(
							"Function '>' with operand 1 of type %s does not allow %s as operand 2. %s", 
							Types.getClassName(op1),
							Types.getClassName(op2),
							ErrorMessage.buildErrLocation(args)));
				}

				final String s1 = ((VncString)op1).getValue();
				final String s2 = ((VncString)op2).getValue();
				return s1.compareTo(s2) > 0 ? True : False;
			}
			else {
				throw new VncException(String.format(
										"Function '>' does not allow %s as operand 1. %s", 
										Types.getClassName(op1),
										ErrorMessage.buildErrLocation(args)));
			}
		}
	};
	
	public static VncFunction gte = new VncFunction(">=") {
		{
			setArgLists("(>= x y)");
			
			setDoc("Returns true if x is greater or equal to y");
			
			setExamples("(>= 3 2)", "(>= 3 3)", "(>= 3.0 2)", "(>= 3.0M 2)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity(">=", args, 2);

			final VncVal op1 = args.nth(0);
			final VncVal op2 = args.nth(1);
			
			if (Types.isVncLong(op1) || Types.isVncDouble(op1) || Types.isVncBigDecimal(op1)) {
				return op1.compareTo(op2) >= 0 ? True : False;
			}
			else if (Types.isVncString(op1)) {
				if (!Types.isVncString(op2)) {
					throw new VncException(String.format(
							"Function '>=' with operand 1 of type %s does not allow %s as operand 2. %s", 
							Types.getClassName(op1),
							Types.getClassName(op2),
							ErrorMessage.buildErrLocation(args)));
				}

				final String s1 = ((VncString)op1).getValue();
				final String s2 = ((VncString)op2).getValue();
				return s1.compareTo(s2) >= 0 ? True : False;
			}
			else {
				throw new VncException(String.format(
										"Function '>=' does not allow %s as operand 1. %s", 
										Types.getClassName(op1),
										ErrorMessage.buildErrLocation(args)));
			}
		}
	};
	


	
	///////////////////////////////////////////////////////////////////////////
	// Casts
	///////////////////////////////////////////////////////////////////////////
 	
	public static VncFunction boolean_cast = new VncFunction("boolean") {
		{
			setArgLists("(boolean x)");
			
			setDoc("Converts to boolean. Everything except 'false' and 'nil' is true in boolean context.");
			
			setExamples(
					"(boolean false)",
					"(boolean true)",
					"(boolean nil)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("boolean", args, 1);

			final VncVal arg = args.nth(0);
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
	};
 	
	public static VncFunction long_cast = new VncFunction("long") {
		{
			setArgLists("(long x)");
			
			setDoc("Converts to long");
			
			setExamples(
					"(long 1)",
					"(long nil)",
					"(long false)",
					"(long true)",
					"(long 1.2)",
					"(long 1.2M)",
					"(long \"1.2\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("long", args, 1);

			final VncVal op1 = args.nth(0);
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
			else if (Types.isVncDouble(op1)) {
				return new VncLong(((VncDouble)op1).getValue().longValue());
			}
			else if (Types.isVncBigDecimal(op1)) {
				return new VncLong(((VncBigDecimal)op1).getValue().longValue());
			}
			else if (Types.isVncString(op1)) {
				final String s = ((VncString)op1).getValue();
				try {
					return new VncLong(Long.parseLong(s));
				}
				catch(Exception ex) {
					throw new VncException(String.format(
							"Function 'long': the string %s can not be converted to a long. %s", 
							s,
							ErrorMessage.buildErrLocation(args)));
				}
			}
			else {
				throw new VncException(String.format(
										"Function 'long' does not allow %s as operand 1. %s", 
										Types.getClassName(op1),
										ErrorMessage.buildErrLocation(args)));
			}
		}
	};
	
	public static VncFunction double_cast = new VncFunction("double") {
		{
			setArgLists("(double x)");
			
			setDoc("Converts to double");
			
			setExamples(
					"(double 1)",
					"(double nil)",
					"(double false)",
					"(double true)",
					"(double 1.2)",
					"(double 1.2M)",
					"(double \"1.2\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("double", args, 1);

			final VncVal op1 = args.nth(0);
			if (op1 == Nil) {
				return new VncDouble(0.0);
			}
			else if (op1 == False) {
				return new VncDouble(0.0);
			}
			else if (op1 == True) {
				return new VncDouble(1.0);
			}
			else if (Types.isVncLong(op1)) {
				return new VncDouble(((VncLong)op1).getValue().doubleValue());
			}
			else if (Types.isVncDouble(op1)) {
				return op1;
			}
			else if (Types.isVncBigDecimal(op1)) {
				return new VncDouble(((VncBigDecimal)op1).getValue().doubleValue());
			}
			else if (Types.isVncString(op1)) {
				final String s = ((VncString)op1).getValue();
				try {
					return new VncDouble(Double.parseDouble(s));
				}
				catch(Exception ex) {
					throw new VncException(String.format(
							"Function 'double': the string %s can not be converted to a double. %s", 
							s,
							ErrorMessage.buildErrLocation(args)));
				}
			}
			else {
				throw new VncException(String.format(
							"Function 'double' does not allow %s as operand 1. %s", 
							Types.getClassName(op1),
							ErrorMessage.buildErrLocation(args)));
			}
		}
	};
	
	public static VncFunction decimal_cast = new VncFunction("decimal") {
		{
			setArgLists("(decimal x) (decimal x scale rounding-mode)");
			
			setDoc( "Converts to decimal. rounding-mode is one of (:CEILING, :DOWN, " +
					":FLOOR, :HALF_DOWN, :HALF_EVEN, :HALF_UP, :UNNECESSARY, :UP)");
			
			setExamples(
					"(decimal 2)", 
					"(decimal 2 3 :HALF_UP)", 
					"(decimal 2.5787 3 :HALF_UP)",
					"(decimal \"2.5787\" 3 :HALF_UP)",
					"(decimal nil)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("decimal", args, 1, 3);

			if (args.isEmpty()) {
				return new VncBigDecimal(BigDecimal.ZERO);
			}
			else {				
				final VncVal arg = args.nth(0);
				final VncLong scale = args.size() < 3 ? null : Coerce.toVncLong(args.nth(1));
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
							"Function 'decimal' does not allow %s as operand 1. %s", 
							Types.getClassName(arg),
							ErrorMessage.buildErrLocation(args)));
				}
			}
		}
	};

	
	///////////////////////////////////////////////////////////////////////////
	// List functions
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction new_list = new VncFunction("list") {
		{
			setArgLists("(list & items)");
			
			setDoc("Creates a new list containing the items.");
			
			setExamples("(list )", "(list 1 2 3)", "(list 1 2 3 [:a :b])");
		}
		
		public VncVal apply(final VncList args) {
			return new VncList(args.getList());
		}
	};

	static public boolean list_Q(VncVal mv) {
		return mv.getClass().equals(VncList.class);
	}
	
	public static VncFunction list_Q = new VncFunction("list?") {
		{
			setArgLists("(list? obj)");
			
			setDoc("Returns true if obj is a list");
			
			setExamples("(list? (list 1 2))", "(list? '(1 2))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("list?", args, 1);
			
			return list_Q(args.nth(0)) ? True : False;
		}
	};

	
	///////////////////////////////////////////////////////////////////////////
	// Vector functions
	///////////////////////////////////////////////////////////////////////////
	public static VncFunction new_vector = new VncFunction("vector") {
		{
			setArgLists("(vector & items)");
			
			setDoc("Creates a new vector containing the items.");
			
			setExamples("(vector )", "(vector 1 2 3)", "(vector 1 2 3 [:a :b])");
		}
		
		public VncVal apply(final VncList args) {
			return new VncVector(args.getList());
		}
	};

	static public boolean vector_Q(VncVal mv) {
		return mv.getClass().equals(VncVector.class);
	}
	
	public static VncFunction vector_Q = new VncFunction("vector?") {
		{
			setArgLists("(vector? obj)");
			
			setDoc("Returns true if obj is a vector");
			
			setExamples("(vector? (vector 1 2))", "(vector? [1 2])");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("vector?", args, 1);
			
			return vector_Q(args.nth(0)) ? True : False;
		}
	};
	
	public static VncFunction subvec = new VncFunction("subvec") {
		{
			setArgLists("(subvec v start) (subvec v start end)");
			
			setDoc( "Returns a vector of the items in vector from start (inclusive) "+
					"to end (exclusive). If end is not supplied, defaults to " + 
					"(count vector)");
			
			setExamples("(subvec [1 2 3 4 5 6] 2)", "(subvec [1 2 3 4 5 6] 4)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("subvec", args, 2, 3);

			final VncVector vec = Coerce.toVncVector(args.nth(0));		
			final VncLong from = Coerce.toVncLong(args.nth(1));
			final VncLong to = args.size() > 2 ? Coerce.toVncLong(args.nth(2)) : null;
			
			return new VncVector(
							to == null
								? vec.getList().subList(from.getValue().intValue(), vec.size())
								: vec.getList().subList(from.getValue().intValue(), to.getValue().intValue()));
		}
	};

	
	///////////////////////////////////////////////////////////////////////////
	// ByteBuf functions
	///////////////////////////////////////////////////////////////////////////
	
	public static VncFunction bytebuf_Q = new VncFunction("bytebuf?") {
		{
			setArgLists("(bytebuf? x)");
			
			setDoc("Returns true if x is a bytebuf");
			
			setExamples(
					"(bytebuf? (bytebuf [1 2]))",
					"(bytebuf? [1 2])",
					"(bytebuf? nil)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("bytebuf?", args, 1);
			
			return Types.isVncByteBuffer(args.nth(0)) ? True : False;
		}
	};

	public static VncFunction bytebuf_cast = new VncFunction("bytebuf") {
		{
			setArgLists("(bytebuf x)");
			
			setDoc( "Converts to bytebuf. x can be a bytebuf, a list/vector of longs, or a string");
			
			setExamples("(bytebuf [0 1 2])", "(bytebuf '(0 1 2))", "(bytebuf \"abc\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("bytebuf", args, 0, 1);

			if (args.isEmpty()) {
				return new VncByteBuffer(ByteBuffer.wrap(new byte[0]));
			}
			
			final VncVal arg = args.nth(0);

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
				return ((VncByteBuffer)arg).copy();
			}
			else if (Types.isVncList(arg)) {
				if (!((VncList)arg).getList().stream().allMatch(v -> Types.isVncLong(v))) {
					throw new VncException(String.format(
							"Function 'bytebuf' a list as argument must contains long values. %s",
							ErrorMessage.buildErrLocation(args)));
				}
				
				final List<VncVal> list = ((VncList)arg).getList();
				
				final byte[] buf = new byte[list.size()];
				for(int ii=0; ii<list.size(); ii++) {
					buf[ii] = (byte)((VncLong)list.get(ii)).getValue().longValue();
				}
				
				return new VncByteBuffer(ByteBuffer.wrap(buf));
			}

			throw new VncException(String.format(
						"Function 'bytebuf' does not allow %s as argument. %s", 
						Types.getClassName(arg),
						ErrorMessage.buildErrLocation(args)));
		}
	};

	public static VncFunction bytebuf_from_string = new VncFunction("bytebuf-from-string") {
		{
			setArgLists("(bytebuf-from-string s encoding)");
			
			setDoc( "Converts a string to a bytebuf using an optional encoding. The encoding defaults to UTF-8");
			
			setExamples("(bytebuf-from-string \"abcdef\" :UTF-8)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("bytebuf-from-string", args, 1, 2);

			final String s = Coerce.toVncString(args.first()).getValue();

			final VncVal encVal = args.size() == 2 ? args.second() : Nil;
			final String encoding = encVal == Nil 
										? "UTF-8" 
										: Types.isVncKeyword(encVal)
											? Coerce.toVncKeyword(encVal).getValue()
											: Coerce.toVncString(encVal).getValue();
			
			try {
				return new VncByteBuffer(ByteBuffer.wrap(s.getBytes(encoding)));				
			}
			catch(Exception ex) {
				throw new VncException(String.format(
						"Failed to convert string to bytebuffer. %s",
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};

	public static VncFunction bytebuf_to_string = new VncFunction("bytebuf-to-string") {
		{
			setArgLists("(bytebuf-to-string buf encoding)");
			
			setDoc( "Converts a bytebuf to a string using an optional encoding. The encoding defaults to UTF-8");
			
			setExamples("(bytebuf-to-string (bytebuf [97 98 99]) :UTF-8)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("bytebuf-to-string", args, 1, 2);

			final ByteBuffer buf = Coerce.toVncByteBuffer(args.first()).getValue();

			final VncVal encVal = args.size() == 2 ? args.second() : Nil;
			final String encoding = encVal == Nil 
										? "UTF-8" 
										: Types.isVncKeyword(encVal)
											? Coerce.toVncKeyword(encVal).getValue()
											: Coerce.toVncString(encVal).getValue();
			
			try {
				return new VncString(new String(buf.array(), encoding));				
			}
			catch(Exception ex) {
				throw new VncException(String.format(
						"Failed to convert bytebuffer to string. %s",
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};

	public static VncFunction bytebuf_sub = new VncFunction("bytebuf-sub") {
		{
			setArgLists("(bytebuf-sub x start) (bytebuf-sub x start end)");
			
			setDoc( "Returns a byte buffer of the items in buffer from start (inclusive) "+
					"to end (exclusive). If end is not supplied, defaults to " + 
					"(count bytebuffer)");
			
			setExamples(
					"(bytebuf-sub (bytebuf [1 2 3 4 5 6]) 2)", 
					"(bytebuf-sub (bytebuf [1 2 3 4 5 6]) 4)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("bytebuf-sub", args, 2, 3);

			final byte[] buf = Coerce.toVncByteBuffer(args.nth(0)).getValue().array();		
			final VncLong from = Coerce.toVncLong(args.nth(1));
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
	};

	
	///////////////////////////////////////////////////////////////////////////
	// Set functions
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction new_set = new VncFunction("set") {
		{
			setArgLists("(set & items)");
			
			setDoc("Creates a new set containing the items.");
			
			setExamples("(set )", "(set nil)", "(set 1)", "(set 1 2 3)", "(set [1 2] 3)");
		}
		
		public VncVal apply(final VncList args) {
			return new VncSet(args);
		}
	};

	public static VncFunction set_Q = new VncFunction("set?") {
		{
			setArgLists("(set? obj)");
			
			setDoc("Returns true if obj is a set");
			
			setExamples("(set? (set 1))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("set?", args, 1);
			
			return Types.isVncSet(args.nth(0)) ? True : False;
		}
	};

	public static VncFunction difference = new VncFunction("difference") {
		{
			setArgLists("(difference s1)", "(difference s1 s2)", "(difference s1 s2 & sets)");
			
			setDoc("Return a set that is the first set without elements of the remaining sets");
			
			setExamples(
					"(difference (set 1 2 3))",
					"(difference (set 1 2) (set 2 3))",
					"(difference (set 1 2) (set 1) (set 1 4) (set 3))");
		}
		
		public VncVal apply(final VncList args) {
			assertMinArity("difference", args, 1);
			
			final Set<VncVal> set = Coerce.toVncSet(args.first()).getSet();
			
			for(int ii=1; ii<args.size(); ii++) {
				set.removeAll(Coerce.toVncSet(args.nth(ii)).getSet());
			}
			
			return new VncSet(set);
		}
	};

	public static VncFunction union = new VncFunction("union") {
		{
			setArgLists("(union s1)", "(union s1 s2)", "(union s1 s2 & sets)");
			
			setDoc("Return a set that is the union of the input sets");
			
			setExamples(
					"(union (set 1 2 3))",
					"(union (set 1 2) (set 2 3))",
					"(union (set 1 2 3) (set 1 2) (set 1 4) (set 3))");
		}
		
		public VncVal apply(final VncList args) {
			assertMinArity("union", args, 1);
			
			final Set<VncVal> set = Coerce.toVncSet(args.first()).getSet();
			
			for(int ii=1; ii<args.size(); ii++) {
				set.addAll(Coerce.toVncSet(args.nth(ii)).getSet());
			}
			
			return new VncSet(set);
		}
	};

	public static VncFunction intersection = new VncFunction("intersection") {
		{
			setArgLists("(intersection s1)", "(intersection s1 s2)", "(intersection s1 s2 & sets)");
			
			setDoc("Return a set that is the intersection of the input sets");
			
			setExamples(
					"(intersection (set 1))",
					"(intersection (set 1 2) (set 2 3))",
					"(intersection (set 1 2) (set 3 4))");
		}
		
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
			
			return new VncSet(intersection);
		}
	};

	
	///////////////////////////////////////////////////////////////////////////
	// HashMap functions
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction new_hash_map = new VncFunction("hash-map") {
		{
			setArgLists("(hash-map & keyvals)", "(hash-map map)");
			
			setDoc("Creates a new hash map containing the items.");
			
			setExamples(
					"(hash-map :a 1 :b 2)", 
					"(hash-map (sorted-map :a 1 :b 2))");
		}
		
		public VncVal apply(final VncList args) {
			if (args.size() == 1 && Types.isVncMap(args.nth(0))) {
				return new VncHashMap(((VncMap)args.nth(0)).getMap());
			}
			else if (args.size() == 1 && Types.isVncJavaObject(args.nth(0))) {
				return ((VncJavaObject)args.nth(0)).toVncMap();
			}
			else {
				return new VncHashMap(args);
			}
		}
	};

	public static VncFunction new_ordered_map = new VncFunction("ordered-map") {
		{
			setArgLists("(ordered-map & keyvals)", "(ordered-map map)");
			
			setDoc("Creates a new ordered map containing the items.");
			
			setExamples(
					"(ordered-map :a 1 :b 2)", 
					"(ordered-map (hash-map :a 1 :b 2))");
		}
		
		public VncVal apply(final VncList args) {
			if (args.size() == 1 && Types.isVncMap(args.nth(0))) {
				return new VncOrderedMap(((VncMap)args.nth(0)).getMap());
			}
			else {
				return new VncOrderedMap(args);
			}
		}
	};

	public static VncFunction new_sorted_map = new VncFunction("sorted-map") {
		{
			setArgLists("(sorted-map & keyvals)", "(sorted-map map)");
			
			setDoc("Creates a new sorted map containing the items.");
			
			setExamples(
					"(sorted-map :a 1 :b 2)", 
					"(sorted-map (hash-map :a 1 :b 2))");
		}
		
		public VncVal apply(final VncList args) {
			if (args.size() == 1 && Types.isVncMap(args.nth(0))) {
				return new VncSortedMap(((VncMap)args.nth(0)).getMap());
			}
			else {
				return new VncSortedMap(args);
			}
		}
	};

	public static VncFunction map_Q = new VncFunction("map?") {
		{
			setArgLists("(map? obj)");
			
			setDoc("Returns true if obj is a map");
			
			setExamples("(map? {:a 1 :b 2})");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("map?", args, 1);
			
			return Types.isVncMap(args.nth(0)) ? True : False;
		}
	};

	public static VncFunction hash_map_Q = new VncFunction("hash-map?") {
		{
			setArgLists("(hash-map? obj)");
			
			setDoc("Returns true if obj is a hash map");
			
			setExamples("(hash-map? (hash-map :a 1 :b 2))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("hash-map?", args, 1);
			
			return Types.isVncHashMap(args.nth(0)) ? True : False;
		}
	};

	public static VncFunction ordered_map_Q = new VncFunction("ordered-map?") {
		{
			setArgLists("(ordered-map? obj)");
			
			setDoc("Returns true if obj is an ordered map");
			
			setExamples("(ordered-map? (ordered-map :a 1 :b 2))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("ordered-map?", args, 1);
			
			return Types.isVncOrderedMap(args.nth(0)) ? True : False;
		}
	};

	public static VncFunction sorted_map_Q = new VncFunction("sorted-map?") {
		{
			setArgLists("(sorted-map? obj)");
			
			setDoc("Returns true if obj is a sorted map");
			
			setExamples("(sorted-map? (sorted-map :a 1 :b 2))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("sorted-map?", args, 1);
			
			return Types.isVncSortedMap(args.nth(0)) ? True : False;
		}
	};

	public static VncFunction contains_Q = new VncFunction("contains?") {
		{
			setArgLists("(contains? coll key)");
			
			setDoc( "Returns true if key is present in the given collection, otherwise " + 
					"returns false.");
			
			setExamples(
					"(contains? {:a 1 :b 2} :a)",
					"(contains? [10 11 12] 1)",
					"(contains? [10 11 12] 5)",
					"(contains? \"abc\" 1)",
					"(contains? \"abc\" 5)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("contains?", args, 2);
			
			final VncVal coll = args.nth(0);
			final VncVal key = args.nth(1);
			
			if (Types.isVncMap(coll)) {
				final VncMap mhm = (VncMap)coll;
				final Map<VncVal,VncVal> hm = mhm.getMap();
				return hm.containsKey(key) ? True : False;
			}
			else if (Types.isVncVector(coll)) {
				final VncVector v = (VncVector)coll;
				final VncLong k = (VncLong)key;
				return v.size() > k.getValue().intValue() ? True : False;
			}
			else if (Types.isVncSet(coll)) {
				final VncSet s = (VncSet)coll;
				return s.getSet().contains(key) ? True : False;
			}
			else if (Types.isVncString(coll)) {
				final VncString s = (VncString)coll;
				final VncLong k = (VncLong)key;
				return s.getValue().length() > k.getValue().intValue() ? True : False;			
			}
			else {
				throw new VncException(String.format(
						"Function 'contains?' does not allow %s as coll. %s", 
						Types.getClassName(coll),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};

	public static VncFunction assoc = new VncFunction("assoc") {
		{
			setArgLists("(assoc coll key val)", "(assoc coll key val & kvs)");
			
			setDoc( "When applied to a map, returns a new map of the " + 
					"same type, that contains the mapping of key(s) to " + 
					"val(s). When applied to a vector, returns a new vector that " + 
					"contains val at index. Note - index must be <= (count vector).");
			
			setExamples(
					"(assoc {} :a 1 :b 2)",
					"(assoc nil :a 1 :b 2)",
					"(assoc [1 2 3] 0 10)",
					"(assoc [1 2 3] 3 10)");
		}
		
		public VncVal apply(final VncList args) {
			if (args.nth(0) == Nil) {
				final VncMap new_hm = new VncHashMap();
				new_hm.assoc((VncList)args.slice(1));
				return new_hm;
			}
			else if (Types.isVncMap(args.nth(0))) {
				final VncMap hm = (VncMap)args.nth(0);
				
				final VncMap new_hm = hm.copy();
				new_hm.assoc((VncList)args.slice(1));
				return new_hm;
			}
			else if (Types.isVncVector(args.nth(0))) {
				final VncVector vec = ((VncVector)args.nth(0)).copy();
				final VncList keyvals = args.slice(1);
				for(int ii=0; ii<keyvals.size(); ii+=2) {
					final VncLong key = Coerce.toVncLong(keyvals.nth(ii));
					final VncVal val = keyvals.nth(ii+1);
					if (vec.size() > key.getValue().intValue()) {
						vec.getList().set(key.getValue().intValue(), val);
					}
					else {
						vec.addAtEnd(val);
					}
				}
				return vec;
			}
			else if (Types.isVncString(args.nth(0))) {
				String s = ((VncString)args.nth(0)).getValue();
				final VncList keyvals = (VncList)args.slice(1);
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
			else if (Types.isVncThreadLocal(args.nth(0))) {
				final VncThreadLocal th = (VncThreadLocal)args.nth(0);
				
				th.assoc((VncList)args.slice(1));
				return th;
			}
			else {
				throw new VncException(String.format(
						"Function 'assoc' does not allow %s as collection. %s", 
						Types.getClassName(args.nth(0)),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};

	public static VncFunction assoc_in = new VncFunction("assoc-in") {
		{
			setArgLists("(assoc-in m ks v)");
			
			setDoc( "Associates a value in a nested associative structure, where ks is a " + 
					"sequence of keys and v is the new value and returns a new nested structure. " + 
					"If any levels do not exist, hash-maps or vectors will be created.");
			
			setExamples(
					"(do\n   (def users [{:name \"James\" :age 26}  {:name \"John\" :age 43}])\n   (assoc-in users [1 :age] 44))",
					"(do\n   (def users [{:name \"James\" :age 26}  {:name \"John\" :age 43}])\n   (assoc-in users [2] {:name \"Jack\" :age 19}) )");			
		}
		
		public VncVal apply(final VncList args) {
			assertArity("assoc-in", args, 3);
						
			final VncCollection coll_copy = Coerce.toVncCollection(args.nth(0)).copy();
			VncList keys = Coerce.toVncList(args.nth(1));
			final VncVal new_val = args.nth(2);
			
			VncCollection coll = coll_copy;
			while(!keys.isEmpty()) {
				final VncVal key = keys.first();
				keys = keys.rest();
					
				if (Types.isVncMap(coll)) {
					final VncVal val = ((VncMap)coll).get(key);
					if (val == Nil) {
						if (keys.isEmpty()) {
							((VncMap)coll).assoc(key, new_val);
						}
						else {
							final VncMap newMap = new VncHashMap();
							((VncMap)coll).assoc(key, newMap);
							coll = newMap;
						}
					}
					else if (keys.isEmpty()) {
						((VncMap)coll).assoc(key, new_val);
					}
					else if (Types.isVncCollection(val)) {
						coll = (VncCollection)val;
					}
					else {
						break;
					}
				}
				else {
					if (Types.isVncLong(key)) {
						final int idx = ((VncLong)key).getValue().intValue();
						final int len = ((VncList)coll).size();
											
						if (idx < 0 || idx > len) {
							throw new VncException(String.format(
									"Function 'assoc-in' index %d out of bounds. %s",
									idx,
									ErrorMessage.buildErrLocation(args)));
						}
						else if (idx < len) {
							if (keys.isEmpty()) {
								((VncList)coll).getList().set(idx, new_val);
								break;
							}
							else {
								final VncVal val = ((VncList)coll).nth(idx);
								if (Types.isVncCollection(val)) {
									coll = ((VncCollection)val);
								}
								else {
									break;
								}								
							}
						}
						else {
							((VncList)coll).addAtEnd(new_val);
							break;
						}
					}
					else {
						break;
					}
				}
			}
			
			return coll_copy;
		}
	};
	
	public static VncFunction dissoc = new VncFunction("dissoc") {
		{
			setArgLists("(dissoc coll key)", "(dissoc coll key & ks)");
			
			setDoc( "Returns a new coll of the same type, " + 
					"that does not contain a mapping for key(s)");
			
			setExamples(
					"(dissoc {:a 1 :b 2 :c 3} :b)",
					"(dissoc {:a 1 :b 2 :c 3} :c :b)");
		}
		
		public VncVal apply(final VncList args) {
			if (Types.isVncMap(args.nth(0))) {
				final VncMap hm = (VncMap)args.nth(0);
				
				final VncMap new_hm = (VncMap)hm.copy();
				new_hm.dissoc((VncList)args.slice(1));
				return new_hm;
			}
			else if (Types.isVncVector(args.nth(0))) {
				final VncVector vec = ((VncVector)args.nth(0)).copy();
				final VncList keyvals = (VncList)args.slice(1);
				for(int ii=0; ii<keyvals.size(); ii++) {
					final VncLong key = Coerce.toVncLong(keyvals.nth(ii));
					if (vec.size() > key.getValue().intValue()) {
						vec.getList().remove(key.getValue().intValue());
					}
				}
				return vec;
			}
			else if (Types.isVncString(args.nth(0))) {
				String s = ((VncString)args.nth(0)).getValue();
				final VncList keyvals = (VncList)args.slice(1);
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
			else if (Types.isVncThreadLocal(args.nth(0))) {
				final VncThreadLocal th = (VncThreadLocal)args.nth(0);
				
				th.dissoc((VncList)args.slice(1));
				return th;
			}
			else {
				throw new VncException(String.format(
						"Function 'dissoc' does not allow %s as coll. %s", 
						Types.getClassName(args.nth(0)),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};

	public static VncFunction get = new VncFunction("get") {
		{
			setArgLists("(get map key)", "(get map key not-found)");
			
			setDoc("Returns the value mapped to key, not-found or nil if key not present.");
			
			setExamples(
					"(get {:a 1 :b 2} :b)",
					";; keywords act like functions on maps \n(:b {:a 1 :b 2})");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("get", args, 2, 3);
			
			if (args.nth(0) == Nil) {
				return Nil;
			} 
			else if (Types.isVncMap(args.nth(0))) {
				final VncMap mhm = Coerce.toVncMap(args.nth(0));
				final VncVal key = args.nth(1);
				final VncVal key_not_found = (args.size() == 3) ? args.nth(2) : Nil;
				
				final VncVal value = mhm.get(key);
				return value != Nil ? value : key_not_found;
			}
			else if (Types.isVncThreadLocal(args.nth(0))) {
				final VncThreadLocal th = Coerce.toVncThreadLocal(args.nth(0));
				
				final VncKeyword key = Coerce.toVncKeyword(args.nth(1));
				final VncVal key_not_found = (args.size() == 3) ? args.nth(2) : Nil;
				
				final VncVal value = th.get(key);
				return value != Nil ? value : key_not_found;
			}
			else {
				throw new VncException(String.format(
						"Function 'get' does not allow %s as collection. %s", 
						Types.getClassName(args.nth(0)),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};

	public static VncFunction get_in = new VncFunction("get-in") {
		{
			setArgLists("(get-in m ks)", "(get-in m ks not-found)");
			
			setDoc( "Returns the value in a nested associative structure, " + 
					"where ks is a sequence of keys. Returns nil if the key " + 
					"is not present, or the not-found value if supplied.");
			
			setExamples(
					"(get-in {:a 1 :b {:c 2 :d 3}} [:b :c])",
					"(get-in [:a :b :c] [0])",
					"(get-in [:a :b [:c :d :e]] [2 1])",
					"(get-in {:a 1 :b {:c [4 5 6]}} [:b :c 1])");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("get-in", args, 2, 3);
			
			VncCollection coll = Coerce.toVncCollection(args.nth(0));
			VncList keys = Coerce.toVncList(args.nth(1));
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
						final VncVal val = ((VncList)coll).nthOrDefault(index, Nil);
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
	};

	public static VncFunction find = new VncFunction("find") {
		{
			setArgLists("(find map key)");
			
			setDoc("Returns the map entry for key, or nil if key not present.");
					
			setExamples("(find {:a 1 :b 2} :b)", "(find {:a 1 :b 2} :z)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("find", args, 2);
			
			if (args.nth(0) == Nil) {
				return Nil;
			} 
			else {
				final VncMap mhm = Coerce.toVncMap(args.nth(0));
				final VncVal key = args.nth(1);
				
				final VncVal value = mhm.get(key);
				return value == Nil ? Nil : new VncVector(key, value);
			}
		}
	};

	public static VncFunction key = new VncFunction("key") {
		{
			setArgLists("(key e)");
			
			setDoc("Returns the key of the map entry.");
			
			setExamples("(key (find {:a 1 :b 2} :b))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("key", args, 1);
			
			final VncList entry = Coerce.toVncList(args.nth(0));
			return entry.first();
		}
	};

	public static VncFunction keys = new VncFunction("keys") {
		{
			setArgLists("(keys map)");
			
			setDoc("Returns a collection of the map's keys.");
			
			setExamples("(keys {:a 1 :b 2 :c 3})");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("keys", args, 1);
			
			final VncMap mhm = Coerce.toVncMap(args.nth(0));
			final Map<VncVal,VncVal> hm = mhm.getMap();
			final VncList key_lst = new VncList();
			for (VncVal key : hm.keySet()) {
				key_lst.addAtEnd(key);
			}
			return key_lst;
		}
	};

	public static VncFunction val = new VncFunction("val") {
		{
			setArgLists("(val e)");
			
			setDoc("Returns the val of the map entry.");
			
			setExamples("(val (find {:a 1 :b 2} :b))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("val", args, 1);
			
			final VncList entry = Coerce.toVncList(args.nth(0));
			return entry.second();
		}
	};

	public static VncFunction vals = new VncFunction("vals") {
		{
			setArgLists("(vals map)");
			
			setDoc("Returns a collection of the map's values.");
			
			setExamples("(vals {:a 1 :b 2 :c 3})");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("vals", args, 1);
			
			final VncMap mhm = Coerce.toVncMap(args.nth(0));
			final Map<VncVal,VncVal> hm = mhm.getMap();
			VncList val_lst = new VncList();
			for (VncVal val : hm.values()) {
				val_lst.addAtEnd(val);
			}
			return val_lst;
		}
	};

	public static VncFunction update = new VncFunction("update") {
		{
			setArgLists("(update m k f)");
			
			setDoc("Updates a value in an associative structure, where k is a " + 
					"key and f is a function that will take the old value " + 
					"return the new value. Returns a new structure.");
			
			setExamples(
					"(update [] 0 (fn [x] 5))",
					"(update [0 1 2] 0 (fn [x] 5))",
					"(update [0 1 2] 0 (fn [x] (+ x 1)))",
					"(update {} :a (fn [x] 5))",
					"(update {:a 0} :b (fn [x] 5))",
					"(update {:a 0 :b 1} :a (fn [x] 5))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("update", args, 3);
			
			if (Types.isVncList(args.first())) {
				final VncList list = ((VncList)args.first()).copy();
				final int idx = Coerce.toVncLong(args.second()).getValue().intValue();
				final VncFunction fn = Coerce.toVncFunction(args.nth(2));
						
				if (idx < 0 || idx > list.size()) {
					throw new VncException(String.format(
							"Function 'update' index %d out of bounds. %s",
							idx,
							ErrorMessage.buildErrLocation(args)));
				}
				else if (idx < list.size()) {
					list.getList().set(idx, fn.apply(new VncList(list.nth(idx))));
				}
				else {
					list.addAtEnd(fn.apply(new VncList(Nil)));
				}			
				return list;
			}
			else if (Types.isVncMap(args.first())) {
				final VncMap map = ((VncMap)args.first()).copy();
				final VncVal key = args.second();
				final VncFunction fn = Coerce.toVncFunction(args.nth(2));
				map.assoc(key, fn.apply(new VncList(map.get(key))));
				return map;
			}
			else {
				throw new VncException(String.format(
						"'update' does not allow %s as associative structure. %s", 
						Types.getClassName(args.nth(0)),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};

	public static VncFunction update_BANG = new VncFunction("update!") {
		{
			setArgLists("(update! m k f)");
			
			setDoc("Updates a value in an associative structure, where k is a " + 
					"key and f is a function that will take the old value " + 
					"return the new value.");
			
			setExamples(
					"(update! [] 0 (fn [x] 5))",
					"(update! [0 1 2] 0 (fn [x] 5))",
					"(update! [0 1 2] 0 (fn [x] (+ x 1)))",
					"(update! {} :a (fn [x] 5))",
					"(update! {:a 0} :b (fn [x] 5))",
					"(update! {:a 0 :b 1} :a (fn [x] 5))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("update!", args, 3);
			
			if (Types.isVncList(args.first())) {
				final VncList list = (VncList)args.first();
				final int idx = Coerce.toVncLong(args.second()).getValue().intValue();
				final VncFunction fn = Coerce.toVncFunction(args.nth(2));
						
				if (idx < 0 || idx > list.size()) {
					throw new VncException(String.format(
							"Function 'update' index %d out of bounds. %s",
							idx,
							ErrorMessage.buildErrLocation(args)));
				}
				else if (idx < list.size()) {
					list.getList().set(idx, fn.apply(new VncList(list.nth(idx))));
				}
				else {
					list.addAtEnd(fn.apply(new VncList(Nil)));
				}			
				return list;
			}
			else if (Types.isVncMap(args.first())) {
				final VncMap map = (VncMap)args.first();
				final VncVal key = args.second();
				final VncFunction fn = Coerce.toVncFunction(args.nth(2));
				map.assoc(key, fn.apply(new VncList(map.get(key))));
				return map;
			}
			else {
				throw new VncException(String.format(
						"'update' does not allow %s as associative structure. %s", 
						Types.getClassName(args.nth(0)),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};

	
	///////////////////////////////////////////////////////////////////////////
	// Sequence functions
	///////////////////////////////////////////////////////////////////////////


	public static VncFunction split_with = new VncFunction("split-with") {
		{
			setArgLists("(split-with pred coll)");
			
			setDoc( "Splits the collection at the first false/nil predicate result in a vector with two lists");
			
			setExamples(
					"(split-with odd? [1 3 5 6 7 9])",
					"(split-with odd? [1 3 5])",
					"(split-with odd? [2 4 6])");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("split-with", args, 2);
			
			if (args.second() == Nil) {
				return new VncVector(new VncList(), new VncList());
			}
			
			final VncFunction pred = Coerce.toVncFunction(args.first());
			final VncSequence coll = Coerce.toVncSequence(args.second());
			
			final List<VncVal> items = coll.getList();
			int splitPos = items.size();
			
			// find splitPos
			for(int ii=0; ii<items.size(); ii++) {
				final VncVal val = coll.nth(ii);
				final VncVal match = pred.apply(new VncList(val));
				if (match == False || match == Nil) {
					splitPos = ii;
					break;
				}				
			}
			
			if (splitPos == 0) {
				return new VncVector(new VncList(), new VncList(items));
			}
			else if (splitPos < items.size()) {
				return new VncVector(
							new VncList(items.subList(0, splitPos)), 
							new VncList(items.subList(splitPos, items.size())));
			}
			else {
				return new VncVector(new VncList(items), new VncList());
			}
		}
	};

	public static VncFunction into = new VncFunction("into") {
		{
			setArgLists("(into to-coll from-coll)");
			
			setDoc( "Returns a new coll consisting of to-coll with all of the items of" + 
					"from-coll conjoined.");
			
			setExamples(
					"(into (sorted-map) [ [:a 1] [:c 3] [:b 2] ] )",
					"(into (sorted-map) [ {:a 1} {:c 3} {:b 2} ] )",
					"(into [] {1 2, 3 4})",
					"(into '() '(1 2 3))",
					"(into [1 2 3] '(4 5 6))",
					"(into '() (bytebuf [0 1 2]))",
					"(into [] (bytebuf [0 1 2]))",
					"(into '() \"abc\")",
					"(into [] \"abc\")",
					"(into (sorted-map) {:b 2 :c 3 :a 1})");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("into", args, 2);
			
			if (args.second() == Nil) {
				return args.first();
			}
	
			final VncCollection to = Coerce.toVncCollection(args.first()).copy();

			if (Types.isVncByteBuffer(args.second())) {
				final VncList byteList = ((VncByteBuffer)args.second()).toVncList();
			
				if (Types.isVncVector(to)) {
					byteList.forEach(v -> ((VncVector)to).addAtEnd(v));
				}
				else if (Types.isVncList(to)) {
					byteList.forEach(v -> ((VncList)to).addAtEnd(v));
				}
				else {
					throw new VncException(String.format(
							"Function 'into' does only allow list and vector as to-coll if from-coll " +
							"is a bytebuf. %s", 
							ErrorMessage.buildErrLocation(args)));
				}
				return to;
			}
			else if (Types.isVncString(args.second())) {
				final VncList charList = ((VncString)args.second()).toVncList();
				
				
				if (Types.isVncVector(to)) {
					charList.forEach(v -> ((VncVector)to).addAtEnd(v));
				}
				else if (Types.isVncList(to)) {
					charList.forEach(v -> ((VncList)to).addAtEnd(v));
				}
				else if (Types.isVncSet(to)) {
					charList.forEach(v -> ((VncSet)to).add(v));
				}
				else {
					throw new VncException(String.format(
							"Function 'into' does only allow list, vector, and set as to-coll if from-coll " +
							"is a string. %s", 
							ErrorMessage.buildErrLocation(args)));
				}
				
				return to;
			}



			final VncCollection from = Coerce.toVncCollection(args.second());
			
			if (Types.isVncVector(to)) {
				from.toVncList().getList().forEach(v -> ((VncVector)to).addAtEnd(v));
			}
			else if (Types.isVncList(to)) {
				from.toVncList().getList().forEach(v -> ((VncList)to).addAtStart(v));
			}
			else if (Types.isVncSet(to)) {
				from.toVncList().getList().forEach(v -> ((VncSet)to).add(v));
			}
			else if (Types.isVncMap(to)) {
				if (Types.isVncSequence(from)) {
					((VncList)from).getList().forEach(it -> {
						if (Types.isVncSequence(it)) {
							((VncMap)to).assoc(((VncSequence)it).toVncList());
						}
						else if (Types.isVncMap(it)) {
							((VncMap)to).getMap().putAll(((VncMap)it).getMap());
						}
					});
				}
				else if (Types.isVncMap(from)) {
					 ((VncMap)to).getMap().putAll(((VncMap)from).getMap());
				}				
			}
			else {
				throw new VncException(String.format(
						"Function 'into' does not allow %s as to-coll. %s", 
						Types.getClassName(args.first()),
						ErrorMessage.buildErrLocation(args)));
			}
			
			return to;
		}
	};

	public static VncFunction sequential_Q = new VncFunction("sequential?") {
		{
			setArgLists("(sequential? obj)");
			
			setDoc("Returns true if obj is a sequential collection");
			
			setExamples("(sequential? '(1))", 
						"(sequential? [1])", 
						"(sequential? {:a 1})", 
						"(sequential? nil)", 
						"(sequential? \"abc\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("sequential?", args, 1);
			
			return Types.isVncSequence(args.first()) ? True : False;
		}
	};

	public static VncFunction coll_Q = new VncFunction("coll?") {
		{
			setArgLists("(coll? obj)");
			
			setDoc("Returns true if obj is a collection");
			
			setExamples("(coll? {:a 1})", "(coll? [1 2])");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("coll?", args, 1);
			
			return Types.isVncCollection(args.first()) ? True : False;
		}
	};
	
	public static VncFunction every_Q = new VncFunction("every?") {
		{
			setArgLists("(every? pred coll)");
			
			setDoc( "Returns true if the predicate is true for all collection items, " +
					"false otherwise");
			
			setExamples(
					"(every? (fn [x] (number? x)) nil)",
					"(every? (fn [x] (number? x)) [])",
					"(every? (fn [x] (number? x)) [1 2 3 4])",
					"(every? (fn [x] (number? x)) [1 2 3 :a])",
					"(every? (fn [x] (>= x 10)) [10 11 12])");
		}
		
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
						   .allMatch(v -> pred.apply(new VncList(v)) == True) ? True : False;
			}
		}
	};
	
	public static VncFunction not_every_Q = new VncFunction("not-every?") {
		{
			setArgLists("(not-every? pred coll)");
			
			setDoc( "Returns false if the predicate is true for all collection items, " +
					"true otherwise");
			
			setExamples(
					"(not-every? (fn [x] (number? x)) nil)",
					"(not-every? (fn [x] (number? x)) [])",
					"(not-every? (fn [x] (number? x)) [1 2 3 4])",
					"(not-every? (fn [x] (number? x)) [1 2 3 :a])",
					"(not-every? (fn [x] (>= x 10)) [10 11 12])");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("not-every?", args, 2);
				
			return every_Q.apply(args) == True ? False : True;
		}
	};
	
	public static VncFunction any_Q = new VncFunction("any?") {
		{
			setArgLists("(any? pred coll)");
			
			setDoc( "Returns true if the predicate is true for at least one collection item, " +
					"false otherwise");
			
			setExamples(
					"(any? (fn [x] (number? x)) nil)",
					"(any? (fn [x] (number? x)) [])",
					"(any? (fn [x] (number? x)) [1 :a :b])",
					"(any? (fn [x] (number? x)) [1 2 3])",
					"(any? (fn [x] (>= x 10)) [1 5 10])");
		}
		
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
						   .anyMatch(v -> pred.apply(new VncList(v)) == True) ? True : False;
			}
		}
	};
	
	public static VncFunction not_any_Q = new VncFunction("not-any?") {
		{
			setArgLists("(not-any? pred coll)");
			
			setDoc( "Returns false if the predicate is true for at least one collection item, " +
					"true otherwise");
			
			setExamples(
					"(not-any? (fn [x] (number? x)) nil)",
					"(not-any? (fn [x] (number? x)) [])",
					"(not-any? (fn [x] (number? x)) [1 :a :b])",
					"(not-any? (fn [x] (number? x)) [1 2 3])",
					"(not-any? (fn [x] (>= x 10)) [1 5 10])");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("not-any?", args, 2);
			
			return any_Q.apply(args) == True ? False : True;
		}
	};


	public static VncFunction count = new VncFunction("count") {
		{
			setArgLists("(count coll)");
			
			setDoc( "Returns the number of items in the collection. (count nil) returns " + 
					"0. Also works on strings, and Java Collections");
			
			setExamples("(count {:a 1 :b 2})", "(count [1 2])", "(count \"abc\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("count", args, 1);
			
			final VncVal arg = args.nth(0);
			if (arg == Nil) {
				return new VncLong(0L);
			} 
			else if (Types.isVncString(arg)) {
				return new VncLong(((VncString)arg).getValue().length());
			}
			else if (Types.isVncByteBuffer(arg)) {
				return new VncLong(((VncByteBuffer)arg).size());
			}
			else if (Types.isVncList(arg)) {
				return new VncLong(((VncList)arg).size());
			}
			else if (Types.isVncSet(arg)) {
				return new VncLong(((VncSet)arg).size());
			}
			else if (Types.isVncMap(arg)) {
				return new VncLong(((VncMap)arg).size());
			}
			else if (Types.isVncJavaList(arg)) {
				return new VncLong(((VncJavaList)arg).size());
			}
			else if (Types.isVncJavaSet(arg)) {
				return new VncLong(((VncJavaSet)arg).size());
			}
			else if (Types.isVncJavaMap(arg)) {
				return new VncLong(((VncJavaMap)arg).size());
			}
			else {
				throw new VncException(String.format(
						"Invalid argument type %s while calling function 'count'. %s",
						Types.getClassName(arg),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};

	public static VncFunction empty = new VncFunction("empty") {
		{
			setArgLists("(empty coll)");
			
			setDoc("Returns an empty collection of the same category as coll, or nil");
			
			setExamples("(empty {:a 1})", "(empty [1 2])", "(empty '(1 2))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("empty", args, 1);

			final VncVal coll = args.first();
			if (coll == Nil) {
				return Nil;
			} 
			else if (coll instanceof VncVector) {
				return ((VncVector)coll).empty();
			} 
			else if (coll instanceof VncList) {
				return ((VncList)coll).empty();
			} 
			else if (coll instanceof VncMap) {
				return ((VncMap)coll).empty();
			} 
			else {
				throw new VncException(String.format(
						"Invalid argument type %s while calling function 'empty'. %s",
						Types.getClassName(coll),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};

	public static VncFunction empty_Q = new VncFunction("empty?") {
		{
			setArgLists("(empty? x)");
			
			setDoc("Returns true if x is empty");
			
			setExamples("(empty? {})", "(empty? [])", "(empty? '())");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("empty?", args, 1);

			final VncVal exp = args.nth(0);
			if (exp == Nil) {
				return True;
			} 
			else if (exp instanceof VncString && ((VncString)exp).getValue().isEmpty()) {
				return True;
			} 
			else if (exp instanceof VncCollection && ((VncCollection)exp).isEmpty()) {
				return True;
			} 
			else if (exp instanceof VncByteBuffer && ((VncByteBuffer)exp).size() == 0) {
				return True;
			} 
			else {
				return False;
			}
		}
	};

	public static VncFunction not_empty_Q = new VncFunction("not-empty?") {
		{
			setArgLists("(not-empty? x)");
			
			setDoc("Returns true if x is not empty");
			
			setExamples("(empty? {:a 1})", "(empty? [1 2])", "(empty? '(1 2))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("not-empty?", args, 1);

			final VncVal exp = args.nth(0);
			if (exp == Nil) {
				return False;
			} 
			else if (exp instanceof VncString && ((VncString)exp).getValue().isEmpty()) {
				return False;
			} 
			else if (exp instanceof VncCollection && ((VncCollection)exp).isEmpty()) {
				return False;
			}
			else if (exp instanceof VncByteBuffer && ((VncByteBuffer)exp).size() != 0) {
				return True;
			} 
			else {
				return True;
			}
		}
	};

	public static VncFunction cons = new VncFunction("cons") {
		{
			setArgLists("(cons x coll)");
			
			setDoc( "Returns a new collection where x is the first element and coll is\n" + 
					"the rest");
			
			setExamples(
					"(cons 1 '(2 3 4 5 6))",
					"(cons [1 2] [4 5 6])");
		}

		public VncVal apply(final VncList args) {
			assertArity("cons", args, 2);

			if (Types.isVncList(args.nth(1))) {
				final VncList list = new VncList();
				list.addAtStart(args.nth(0));
				list.addAtEnd((VncList)args.nth(1));
				return list;
			}
			else if (Types.isVncMap(args.nth(1)) && Types.isVncMap(args.nth(0))) {
				final VncMap map = ((VncMap)args.nth(1)).copy();
				map.getMap().putAll(((VncMap)args.nth(0)).getMap());
				return map;
			}
			else {
				throw new VncException(String.format(
						"Invalid argument type %s while calling function 'cons'. %s",
						Types.getClassName(args.nth(1)),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};

	public static VncFunction concat = new VncFunction("concat") {
		{
			setArgLists("(concat coll)", "(concat coll & colls)");
			
			setDoc( "Returns a collection of the concatenation of the elements " +
					"in the supplied colls.");
			
			setExamples(
					"(concat [1 2])",
					"(concat [1 2] [4 5 6])",
					"(concat '(1 2))",
					"(concat '(1 2) [4 5 6])",
					"(concat {:a 1})",
					"(concat {:a 1} {:b 2 c: 3})",
					"(concat \"abc\")",
					"(concat \"abc\" \"def\")");
		}
		
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
				else if (Types.isVncList(val)) {
					result.addAll(((VncList)val).getList());
				}
				else if (Types.isVncSet(val)) {
					result.addAll(((VncSet)val).getList());
				}
				else if (Types.isVncMap(val)) {
					result.addAll(((VncMap)val).toVncList().getList());
				}
				else if (Types.isVncJavaList(val)) {
					result.addAll(((VncJavaList)val).toVncList().getList());
				}
				else if (Types.isVncJavaSet(val)) {
					result.addAll(((VncJavaSet)val).toVncList().getList());
				}
				else if (Types.isVncJavaMap(val)) {
					result.addAll(((VncJavaMap)val).toVncList().getList());
				}
				else {
					throw new VncException(String.format(
							"Invalid argument type %s while calling function 'concat'. %s",
							Types.getClassName(val),
							ErrorMessage.buildErrLocation(args)));
				}
			});
			
			return new VncList(result);
		}
	};

	public static VncFunction interleave = new VncFunction("interleave") {
		{
			setArgLists("(interleave c1 c2)", "(interleave c1 c2 & colls)");
			
			setDoc("Returns a collection of the first item in each coll, then the second etc.");
			
			setExamples("(interleave [:a :b :c] [1 2])");	
		}
		
		public VncVal apply(final VncList args) {
			assertMinArity("interleave", args, 2);

			int len = Coerce.toVncList(args.first()).size();
			final List<VncList> lists = new ArrayList<>();
			for(int ii=0; ii<args.size(); ii++) {
				final VncList l = Coerce.toVncList(args.nth(ii));
				lists.add(l);
				len = Math.min(len, l.size());				
			}

			final VncList result = new VncList();
			
			for(int nn=0; nn<len; nn++) {
				final VncList item = new VncList();
				for(int ii=0; ii<lists.size(); ii++) {
					item.addAtEnd(lists.get(ii).nth(nn));
				}
				result.addAtEnd(item);
			}
					
			return result;
		}
	};

	public static VncFunction interpose = new VncFunction("interpose") {
		{
			setArgLists("(interpose sep coll)");
			
			setDoc("Returns a collection of the elements of coll separated by sep.");
						
			setExamples("(interpose \", \" [1 2 3])", "(apply str (interpose \", \" [1 2 3]))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("interpose", args, 2);

			final VncVal sep = args.first();
			final VncList coll = Coerce.toVncList(args.second());
			
			final VncList result = new VncList();
	
			if (!coll.isEmpty()) {
				result.addAtEnd(coll.first());
				coll.rest().forEach(v -> {
					result.addAtEnd(sep);
					result.addAtEnd(v);
				});
			}
						
			return result;
		}
	};

	public static VncFunction first = new VncFunction("first") {
		{
			setArgLists("(first coll)");
			
			setDoc("Returns the first element of coll.");
			
			setExamples(
					"(first nil)",
					"(first [])",
					"(first [1 2 3])",
					"(first '())",
					"(first '(1 2 3))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("first", args, 1);

			final VncVal val = args.nth(0);
			if (val == Nil) {
				return Nil;
			}
			
			if (Types.isVncList(val)) {
				return ((VncList)val).first();
			}
			else if (Types.isVncString(val)) {
				return ((VncString)val).first();
			}
			else {
				throw new VncException(String.format(
						"Invalid argument type %s while calling function 'first'. %s",
						Types.getClassName(val),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};

	public static VncFunction second = new VncFunction("second") {
		{
			setArgLists("(second coll)");
			
			setDoc("Returns the second element of coll.");
			
			setExamples(
					"(second nil)",
					"(second [])",
					"(second [1 2 3])",
					"(second '())",
					"(second '(1 2 3))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("second", args, 1);


			final VncVal val = args.nth(0);
			if (val == Nil) {
				return Nil;
			}
			
			if (Types.isVncList(val)) {
				return ((VncList)val).second();
			}
			else if (Types.isVncString(val)) {
				return ((VncString)val).second();
			}
			else {
				throw new VncException(String.format(
						"Invalid argument type %s while calling function 'second'. %s",
						Types.getClassName(val),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};

	public static VncFunction nth = new VncFunction("nth") {
		{
			setArgLists("(nth coll idx)");
			
			setDoc("Returns the nth element of coll.");
			
			setExamples(
					"(nth nil 1)",
					"(nth [1 2 3] 1)",
					"(nth '(1 2 3) 1)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("nth", args, 2);

			final int idx = Coerce.toVncLong(args.nth(1)).getValue().intValue();

			final VncVal val = args.nth(0);
			if (val == Nil) {
				return Nil;
			}
			
			if (Types.isVncList(val)) {
				return ((VncList)val).nth(idx);
			}
			else if (Types.isVncString(val)) {
				return ((VncString)val).nth(idx);
			}
			else {
				throw new VncException(String.format(
						"Invalid argument type %s while calling function 'nth'. %s",
						Types.getClassName(val),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};

	public static VncFunction last = new VncFunction("last") {
		{
			setArgLists("(last coll)");
			
			setDoc("Returns the last element of coll.");
			
			setExamples(
					"(last nil)",
					"(last [])",
					"(last [1 2 3])",
					"(last '())",
					"(last '(1 2 3))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("last", args, 1);

			final VncVal val = args.nth(0);
			if (val == Nil) {
				return Nil;
			}
			
			if (Types.isVncList(val)) {
				return ((VncList)val).last();
			}
			else if (Types.isVncString(val)) {
				return ((VncString)val).last();
			}
			else {
				throw new VncException(String.format(
						"Invalid argument type %s while calling function 'last'. %s",
						Types.getClassName(val),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};

	public static VncFunction rest = new VncFunction("rest") {
		{
			setArgLists("(rest coll)");
			
			setDoc("Returns a collection with second to list element");
			
			setExamples(
					"(rest nil)",
					"(rest [])",
					"(rest [1])",
					"(rest [1 2 3])",
					"(rest '())",
					"(rest '(1))",
					"(rest '(1 2 3))");
		}
		
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
			else {
				throw new VncException(String.format(
						"Invalid argument type %s while calling function 'rest'. %s",
						Types.getClassName(args.first()),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};

	public static VncFunction butlast = new VncFunction("butlast") {
		{
			setArgLists("(butlast coll)");
			
			setDoc("Returns a collection with all but the last list element");
			
			setExamples(
					"(butlast nil)",
					"(butlast [])",
					"(butlast [1])",
					"(butlast [1 2 3])",
					"(butlast '())",
					"(butlast '(1))",
					"(butlast '(1 2 3))");
		}
		
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
			else {
				throw new VncException(String.format(
						"Invalid argument type %s while calling function 'butlast'. %s",
						Types.getClassName(args.first()),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};

	public static VncFunction nfirst = new VncFunction("nfirst") {
		{
			setArgLists("(nfirst coll n)");
			
			setDoc("Returns a collection of the first n items");
			
			setExamples(
					"(nfirst nil 2)",
					"(nfirst [] 2)",
					"(nfirst [1] 2)",
					"(nfirst [1 2 3] 2)",
					"(nfirst '() 2)",
					"(nfirst '(1) 2)",
					"(nfirst '(1 2 3) 2)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("nfirst", args, 2);

			if (args.nth(0) == Nil) {
				return new VncList();
			}
			else if (Types.isVncVector(args.nth(0))) {
				final VncVector vec = Coerce.toVncVector(args.nth(0));		
				final int n = Math.max(0, Math.min(vec.size(), Coerce.toVncLong(args.nth(1)).getValue().intValue()));				
				return vec.isEmpty() 
						? new VncVector() 
						: new VncVector(vec.getList().subList(0, n));
			}
			else if (Types.isVncList(args.nth(0))) {
				final VncList list = Coerce.toVncList(args.nth(0));		
				final int n = Math.max(0, Math.min(list.size(), Coerce.toVncLong(args.nth(1)).getValue().intValue()));				
				return list.isEmpty() 
						? new VncList() 
						: new VncList(list.getList().subList(0, n));
			}
			else {
				throw new VncException(String.format(
						"nfirst: type %s not supported. %s",
						Types.getClassName(args.nth(0)),
						ErrorMessage.buildErrLocation(args.nth(0))));
			}
		}
	};

	public static VncFunction nlast = new VncFunction("nlast") {
		{
			setArgLists("(nlast coll n)");
			
			setDoc("Returns a collection of the last n items");
			
			setExamples(
					"(nlast nil 2)",
					"(nlast [] 2)",
					"(nlast [1] 2)",
					"(nlast [1 2 3] 2)",
					"(nlast '() 2)",
					"(nlast '(1) 2)",
					"(nlast '(1 2 3) 2)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("nlast", args, 2);

			if (args.nth(0) == Nil) {
				return new VncList();
			}
			else if (Types.isVncVector(args.nth(0))) {
				final VncVector vec = Coerce.toVncVector(args.nth(0));		
				final int n = Math.max(0, Math.min(vec.size(), Coerce.toVncLong(args.nth(1)).getValue().intValue()));				
				return vec.isEmpty() 
						? new VncVector() 
						: new VncVector(vec.getList().subList(vec.size()-n, vec.size()));
			}
			else if (Types.isVncList(args.nth(0))) {
				final VncList list = Coerce.toVncList(args.nth(0));		
				final int n = Math.max(0, Math.min(list.size(), Coerce.toVncLong(args.nth(1)).getValue().intValue()));				
				return list.isEmpty() 
						? new VncList() 
						: new VncList(list.getList().subList(list.size()-n, list.size()));
			}
			else {
				throw new VncException(String.format(
						"nlast: type %s not supported. %s",
						Types.getClassName(args.nth(0)),
						ErrorMessage.buildErrLocation(args.nth(0))));
			}
		}
	};

	public static VncFunction distinct = new VncFunction("distinct") {
		{
			setArgLists("(distinct coll)");
			
			setDoc("Returns a collection with all duplicates removed");
			
			setExamples(
					"(distinct [1 2 3 4 2 3 4])",
					"(distinct '(1 2 3 4 2 3 4))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("distinct", args, 1);

			if (args.nth(0) == Nil) {
				return new VncList();
			}
			
			final VncList result = ((VncList)args.nth(0)).empty();
			
			result.getList().addAll(
					Coerce
						.toVncList(args.nth(0))
						.getList()
						.stream()
						.distinct()
						.collect(Collectors.toList()));
			
			return result;
		}
	};

	public static VncFunction dedupe = new VncFunction("dedupe") {
		{
			setArgLists("(dedupe coll)");
			
			setDoc("Returns a collection with all consecutive duplicates removed");
			
			setExamples(
					"(dedupe [1 2 2 2 3 4 4 2 3])",
					"(dedupe '(1 2 2 2 3 4 4 2 3))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("dedupe", args, 1);

			if (args.nth(0) == Nil) {
				return new VncList();
			}
			
			final VncList result = ((VncList)args.nth(0)).empty();
			
			VncVal seen = null;
			
			for(VncVal val : Coerce.toVncList(args.nth(0)).getList()) {
				if (seen == null || !val.equals(seen)) {
					result.addAtEnd(val);
					seen = val;
				}
			}
			
			return result;
		}
	};


	public static VncFunction partition = new VncFunction("partition") {
		{
			setArgLists("(partition n coll)", "(partition n step coll)", "(partition n step padcoll coll)");
			
			setDoc( "Returns a collection of lists of n items each, at offsets step " + 
					"apart. If step is not supplied, defaults to n, i.e. the partitions " + 
					"do not overlap. If a padcoll collection is supplied, use its elements as " + 
					"necessary to complete last partition upto n items. In case there are " + 
					"not enough padding elements, return a partition with less than n items.");
			
			setExamples(
					"(partition 4 (range 20))",
					"(partition 4 6 (range 20))",
					"(partition 3 6 [\"a\"] (range 20))",
					"(partition 4 6 [\"a\" \"b\" \"c\" \"d\"] (range 20))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("partition", args, 2, 3 ,4);

			final int n = Coerce.toVncLong(args.nth(0)).getValue().intValue();
			final int step = args.size() > 2 ? Coerce.toVncLong(args.nth(1)).getValue().intValue() : n;
			final List<VncVal> padcoll = args.size() > 3 ? Coerce.toVncList(args.nth(2)).getList() : new ArrayList<>();
			final List<VncVal> coll = Coerce.toVncList(args.nth(args.size()-1)).getList();
			
			if (n <= 0) {
				throw new VncException(String.format(
						"partition: n must be a positive number. %s",
						ErrorMessage.buildErrLocation(args.nth(0))));
			}
			if (step <= 0) {
				throw new VncException(String.format(
						"partition: step must be a positive number. %s",
						ErrorMessage.buildErrLocation(args.nth(0))));
			}
			
			// split at 'step'
			final List<List<VncVal>> splits = new ArrayList<>();
			for (int ii=0; ii<coll.size(); ii += step) {			
				splits.add(coll.subList(ii, Math.min(ii + step, coll.size())));
			}
			
			final VncList result = new VncList();
			for(List<VncVal> split : splits) {
				if (n == split.size()) {
					result.addList(new VncList(split));
				}
				else if (n < split.size()) {
					result.addList(new VncList(split.subList(0, n)));
				}
				else {
					final List<VncVal> split_ = new ArrayList<>(split);
					for(int ii=0; ii<(n-split.size()) && ii<padcoll.size(); ii++) {
						split_.add(padcoll.get(ii));
					}
					result.addList(new VncList(split_));
				}
			}
			return result;
		}
	};

	public static VncFunction coalesce = new VncFunction("coalesce") {
		{
			setArgLists("(coalesce args*)");
			
			setDoc("Returns the first non nil arg");
			
			setExamples(
					"(coalesce )",
					"(coalesce 1 2)",
					"(coalesce nil)",
					"(coalesce nil 1 2)");
		}
		
		public VncVal apply(final VncList args) {
			return args.stream().filter(v -> v != Nil).findFirst().orElse(Nil);
		}
	};

	public static VncFunction emptyToNil = new VncFunction("empty-to-nil") {
		{
			setArgLists("(empty-to-nil x)");
			
			setDoc("Returns nil if x is empty");
			
			setExamples(
					"(empty-to-nil \"\")",
					"(empty-to-nil [])",
					"(empty-to-nil '())",
					"(empty-to-nil {})");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("empty-to-nil", args, 1);
			
			final VncVal arg = args.nth(0);
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
	};

	public static VncFunction className = new VncFunction("class") {
		{
			setArgLists("(class x)");
			
			setDoc("Returns the class of x");
			
			setExamples("(class 5)", "(class [1 2])", "(class (. :java.time.ZonedDateTime :now))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("class", args, 1);
			
			return Types.getClassName(args.nth(0));
		}
	};

	public static VncFunction pop = new VncFunction("pop") {
		{
			setArgLists("(pop coll)");
			
			setDoc( "For a list, returns a new list without the first item, " + 
					"for a vector, returns a new vector without the last item.");
			
			setExamples(
					"(pop '(1 2 3 4))",
					"(pop [1 2 3 4])");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("pop", args, 1);
			
			final VncVal exp = args.nth(0);
			if (exp == Nil) {
				return new VncList();
			}
			final VncList ml = Coerce.toVncList(exp);

			if (Types.isVncVector(ml)) {
				return ml.size() < 2 ? new VncVector() : ((VncVector)ml).slice(0, ml.size()-1);
			}
			else {
				return ml.isEmpty() ? new VncList() : ml.slice(1);
			}
		}
	};

	public static VncFunction peek = new VncFunction("peek") {
		{
			setArgLists("(peek coll)");
			
			setDoc("For a list, same as first, for a vector, same as last");
			
			setExamples(
					"(peek '(1 2 3 4))",
					"(peek [1 2 3 4])");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("peek", args, 1);
			
			final VncVal exp = args.nth(0);
			if (exp == Nil) {
				return Nil;
			}
			final VncList ml = Coerce.toVncList(exp);

			if (Types.isVncVector(ml)) {
				return ml.isEmpty() ? Nil : ((VncVector)ml).nth(ml.size()-1);
			}
			else {
				return ml.isEmpty() ? Nil : ml.nth(0);
			}
		}
	};
	
	public static VncFunction take_while = new VncFunction("take-while") {
		{
			setArgLists("(take-while predicate coll)");
			
			setDoc( "Returns a list of successive items from coll while " + 
					"(predicate item) returns logical true.");
			
			setExamples("(take-while neg? [-2 -1 0 1 2 3])");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("take-while", args, 2);
			
			final VncFunction predicate = Coerce.toVncFunction(args.nth(0));
			final VncList coll = Coerce.toVncList(args.nth(1));
			
			for(int i=0; i<coll.size(); i++) {
				final VncVal take = predicate.apply(new VncList(coll.nth(i)));
				if (take == False) {
					return coll.slice(0, i);
				}
			}
			return coll;
		}
	};
	
	public static VncFunction take = new VncFunction("take") {
		{
			setArgLists("(take n coll)");
			
			setDoc( "Returns a collection of the first n items in coll, or all items if " + 
					"there are fewer than n.");
			
			setExamples("(take 3 [1 2 3 4 5])", "(take 10 [1 2 3 4 5])");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("take", args, 2);
			
			final VncLong n = Coerce.toVncLong(args.nth(0));
			final VncList coll = Coerce.toVncList(args.nth(1));

			return coll.slice(0, (int)Math.min(n.getValue(), coll.size()));
		}
	};
	
	public static VncFunction drop_while = new VncFunction("drop-while") {
		{
			setArgLists("(drop-while predicate coll)");
			
			setDoc( "Returns a list of the items in coll starting from the " + 
					"first item for which (predicate item) returns logical false.");
			
			setExamples("(drop-while neg? [-2 -1 0 1 2 3])");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("drop-while", args, 2);
			
			final VncFunction predicate = Coerce.toVncFunction(args.nth(0));
			final VncList coll = Coerce.toVncList(args.nth(1));
			
			for(int i=0; i<coll.size(); i++) {
				final VncVal take = predicate.apply(new VncList(coll.nth(i)));
				if (take == False) {
					return coll.slice(i);
				}
			}
			return coll.empty();
		}
	};
	
	public static VncFunction drop = new VncFunction("drop") {
		{
			setArgLists("(drop n coll)");
			
			setDoc("Returns a collection of all but the first n items in coll");
			
			setExamples("(drop 3 [1 2 3 4 5])", "(drop 10 [1 2 3 4 5])");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("drop", args, 2);
			
			final VncLong n = Coerce.toVncLong(args.nth(0));
			final VncList coll = Coerce.toVncList(args.nth(1));

			return coll.slice((int)Math.min(n.getValue()+1, coll.size()));
		}
	};
	
	public static VncFunction flatten = new VncFunction("flatten") {
		{
			setArgLists("(flatten coll)");
			
			setDoc( "Takes any nested combination of collections (lists, vectors, " + 
					"etc.) and returns their contents as a single, flat sequence. " + 
					"(flatten nil) returns an empty list.");
			
			setExamples("(flatten [])", "(flatten [[1 2 3] [4 5 6] [7 8 9]])");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("flatten", args, 1);
			
			final VncList coll = Coerce.toVncList(args.nth(0));
			
			final List<VncVal> result = new ArrayList<>();
			flatten(coll, result);			
			return Types.isVncVector(coll) ? new VncVector(result) : new VncList(result);
		}
	};
	
	public static VncFunction reverse = new VncFunction("reverse") {
		{
			setArgLists("(reverse coll)");
			
			setDoc("Returns a collection of the items in coll in reverse order");
			
			setExamples("(reverse [1 2 3 4 5 6])");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("reverse", args, 1);
			
			final VncList coll = Coerce.toVncList(args.nth(0));
			
			final VncList result = coll.empty();
			for(int ii=coll.size()-1; ii>=0; ii--) {
				result.addAtEnd(coll.nth(ii));
			}	
			return result;
		}
	};
	
	public static VncFunction sort = new VncFunction("sort") {
		{
			setArgLists("(sort coll)", "(sort comparefn coll)");
			
			setDoc( "Returns a sorted list of the items in coll. If no compare function " + 
					"comparefn is supplied, uses the natural compare. The compare function " + 
					"takes two arguments and returns -1, 0, or 1");
			
			setExamples(
					"(sort [3 2 5 4 1 6])", 
					"(sort compare [3 2 5 4 1 6])", 
					"; reversed\n" +
					"(sort (comp (partial * -1) compare) [3 2 5 4 1 6])", 
					"(sort {:c 3 :a 1 :b 2})");
		}

		public VncVal apply(final VncList args) {
			assertArity("sort", args, 1, 2);

			if (args.size() == 1) {
				// no compare function -> sort by natural order
				return sort(
						"sort", 
						args, 
						args.nth(0), 
						(x,y) -> Coerce.toVncLong(compare.apply(new VncList(x,y))).getIntValue());
			}
			else if (args.size() == 2) {
				final VncFunction compfn = Coerce.toVncFunction(args.nth(0));
				
				return sort(
						"sort", 
						args, 
						args.nth(1), 
						(x,y) -> Coerce.toVncLong(compfn.apply(new VncList(x,y))).getIntValue());
			}
			else {
				throw new VncException(String.format(
						"sort: args not supported. %s",
						ErrorMessage.buildErrLocation(args)));
			}			
		}
	};

	public static VncFunction sort_by = new VncFunction("sort-by") {
		{
			setArgLists("(sort-by keyfn coll)", "(sort-by keyfn compfn coll)");
			
			setDoc( "Returns a sorted sequence of the items in coll, where the sort " + 
					"order is determined by comparing (keyfn item).  If no comparator is " + 
					"supplied, uses compare.");
			
			setExamples(
					"(sort-by count [\"aaa\" \"bb\" \"c\"])", 
					"; reversed\n" +
					"(sort-by count (comp (partial * -1) compare) [\"aaa\" \"bb\" \"c\"])", 
					"(sort-by first [[1 2] [3 4] [2 3]])",
					"; reversed\n" +
					"(sort-by first (comp (partial * -1) compare) [[1 2] [3 4] [2 3]])",
					"(sort-by (fn [x] (get x :rank)) [{:rank 2} {:rank 3} {:rank 1}])",
					"; reversed\n" +
					"(sort-by (fn [x] (get x :rank)) (comp (partial * -1) compare) [{:rank 2} {:rank 3} {:rank 1}])");
		}

		public VncVal apply(final VncList args) {
			assertArity("sort-by", args, 2, 3);

			if (args.size() == 2) {
				final VncFunction keyfn = Coerce.toVncFunction(args.nth(0));

				return sort(
						"sort-by", 
						args, 
						args.nth(1), 
						(x,y) -> Coerce.toVncLong(
									compare.apply(
										new VncList(
												keyfn.apply(new VncList(x)),
												keyfn.apply(new VncList(y))))
								 ).getIntValue());
			}
			else if (args.size() == 3) {
				final VncFunction keyfn = Coerce.toVncFunction(args.nth(0));
				final VncFunction compfn = Coerce.toVncFunction(args.nth(1));

				return sort(
						"sort-by", 
						args, 
						args.nth(2), 
						(x,y) -> Coerce.toVncLong(
									compfn.apply(
										new VncList(
												keyfn.apply(new VncList(x)),
												keyfn.apply(new VncList(y)))
											)
								 ).getIntValue());
			}
			else {
				throw new VncException(String.format(
						"sort-by: args not supported. %s",
						ErrorMessage.buildErrLocation(args)));
			}			
		}
	};

	public static VncFunction group_by = new VncFunction("group-by") {
		{
			setArgLists("(group-by f coll)");
			
			setDoc( "Returns a map of the elements of coll keyed by the result of " + 
					"f on each element. The value at each key will be a vector of the " + 
					"corresponding elements, in the order they appeared in coll.");
			
			setExamples(
					"(group-by count [\"a\" \"as\" \"asd\" \"aa\" \"asdf\" \"qwer\"])",
					"(group-by odd? (range 10))");
		}

		public VncVal apply(final VncList args) {
			assertArity("group-by", args, 2);

			final VncFunction fn = Coerce.toVncFunction(args.nth(0));
			final VncList coll = Coerce.toVncList(args.nth(1));

			final VncMap map = new VncOrderedMap();
			
			coll.getList().stream().forEach(v -> {
				final VncVal key = fn.apply(new VncList(v));
				final VncList val = Coerce.toVncList(map.getMap().get(key));
				if (val == null) {
					map.getMap().put(key, new VncVector(v));
				}
				else {
					map.getMap().put(key, val.addAtEnd(v));
				}
			});
			
			return map;
		}
	};

	// General sequence functions
	public static VncFunction apply = new VncFunction("apply") {
		{
			setArgLists("(apply f args* coll)");
			
			setDoc("Applies f to all arguments composed of args and coll");
			
			setExamples(
					"(apply str [1 2 3 4 5])");
		}
		
		public VncVal apply(final VncList args) {
			final VncFunction fn = Coerce.toVncFunction(args.nth(0));
			final VncList fn_args = args.slice(1,args.size()-1);
			
			final VncVal coll = args.last();
			if (coll == Nil) {
				fn_args.getList().add(Nil);
			}
			else {
				final List<VncVal> tailArgs = Coerce.toVncList(args.last()).getList();
				fn_args.getList().addAll(tailArgs);				
			}
			return fn.apply(fn_args);
		}
	};
	
	public static VncFunction comp = new VncFunction("comp") {
		{
			setArgLists("(comp f*)");
			
			setDoc( "Takes a set of functions and returns a fn that is the composition " + 
					"of those fns. The returned fn takes a variable number of args, " + 
					"applies the rightmost of fns to the args, the next " + 
					"fn (right-to-left) to the result, etc. ");

			setExamples(
					"((comp str +) 8 8 8)", 
					"(map (comp - (partial + 3) (partial * 2)) [1 2 3 4])", 
					"((reduce comp [(partial + 1) (partial * 2) (partial + 3)]) 100)",
					"(filter (comp not zero?) [0 1 0 2 0 3 0 4])", 
					"(do \n" +
					"   (def fifth (comp first rest rest rest rest)) \n" +
					"   (fifth [1 2 3 4 5]))");
		}
		
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
						args_ = new VncList(result);
					}
					return result;
				}
			};
		}
	};
	
	public static VncFunction compare = new VncFunction("compare") {
		{
			setArgLists("(compare x y)");
			
			setDoc( "Comparator. Returns -1, 0, or 1 when x is logically 'less than', " +
					"'equal to', or 'greater than' y.");

			setExamples(
					"(compare nil 0)", 
					"(compare 0 nil)", 
					"(compare 1 0)", 
					"(compare 1 1)", 
					"(compare 1 2)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("compare", args, 2);

			return new VncLong(args.first().compareTo(args.second()));				
		}
	};
		
	public static VncFunction partial = new VncFunction("partial") {
		{
			setArgLists("(partial f args*)");
			
			setDoc( "Takes a function f and fewer than the normal arguments to f, and " + 
					"returns a fn that takes a variable number of additional args. When " + 
					"called, the returned function calls f with args + additional args.");
			
			setExamples(
					"((partial * 2) 3)", 
					"(map (partial * 2) [1 2 3 4])", 
					"(do \n" +
					"   (def hundred-times (partial * 100)) \n" +
					"   (hundred-times 5))");
		}
		
		public VncVal apply(final VncList args) {
			assertMinArity("partial", args, 2);
			
			final VncFunction fn = Coerce.toVncFunction(args.first());
			final VncList fnArgs = args.slice(1);
			
			return new VncFunction() {
				public VncVal apply(final VncList args) {
					return fn.apply(fnArgs.copy().addAtEnd(args));
				}
			};
		}
	};

	public static VncFunction map = new VncFunction("map") {
		{
			setArgLists("(map f coll colls*)");
			
			setDoc( "Applys f to the set of first items of each coll, followed by applying " + 
					"f to the set of second items in each coll, until any one of the colls " + 
					"is exhausted.  Any remaining items in other colls are ignored. ");
			
			setExamples(
					"(map inc [1 2 3 4])");
		}
		
		public VncVal apply(final VncList args) {
			if (args.size() < 2) {
				return Nil;
			}
			
			final VncFunction fn = Coerce.toVncFunction(args.nth(0));
			final VncList lists = removeNilValues((VncList)args.slice(1));
			final VncList result = new VncList();
						
			if (lists.isEmpty()) {
				return Nil;
			}
			
			int index = 0;
			boolean hasMore = true;
			while(hasMore) {
				final VncList fnArgs = new VncList();
				
				for(int ii=0; ii<lists.size(); ii++) {
					final VncList nthList = Coerce.toVncList(lists.nth(ii));
					if (nthList.size() > index) {
						fnArgs.addAtEnd(nthList.nth(index));
					}
					else {
						hasMore = false;
						break;
					}
				}

				if (hasMore) {
					final VncVal val = fn.apply(fnArgs);
					result.getList().add(val);			
					index += 1;
				}
			}
	
			return result;
		}
	};

	public static VncFunction mapv = new VncFunction("mapv") {
		{
			setArgLists("(mapv f coll colls*)");
			
			setDoc( "Returns a vector consisting of the result of applying f " +
					"to the set of first items of each coll, followed by applying " + 
					"f to the set of second items in each coll, until any one of the colls " + 
					"is exhausted.  Any remaining items in other colls are ignored. ");

			setExamples(
					"(mapv inc [1 2 3 4])");
		}
		
		public VncVal apply(final VncList args) {
			final VncFunction fn = Coerce.toVncFunction(args.nth(0));
			final VncList lists = removeNilValues((VncList)args.slice(1));
			final VncVector result = new VncVector();

			if (lists.isEmpty()) {
				return Nil;
			}
			
			int index = 0;
			boolean hasMore = true;
			while(hasMore) {
				final VncList fnArgs = new VncList();
				
				for(int ii=0; ii<lists.size(); ii++) {
					final VncList nthList = Coerce.toVncList(lists.nth(ii));
					if (nthList.size() > index) {
						fnArgs.addAtEnd(nthList.nth(index));
					}
					else {
						hasMore = false;
						break;
					}
				}

				if (hasMore) {
					result.getList().add(fn.apply(fnArgs));			
					index += 1;
				}
			}
	
			return result;
		}
	};

	public static VncFunction keep = new VncFunction("keep") {
		{
			setArgLists("(keep f coll)");
			
			setDoc( "Returns a sequence of the non-nil results of (f item). Note, " + 
					"this means false return values will be included. f must be free of " + 
					"side-effects.");
			
			setExamples(
					"(keep even? (range 1 4))",
					"(keep (fn [x] (if (odd? x) x)) (range 4))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("keep", args, 2);
			
			final VncVal result = map.apply(args);

			return result == Nil
					? Nil
					: removeNilValues(Coerce.toVncList(result));
		}
	};

	public static VncFunction docoll = new VncFunction("docoll") {
		{
			setArgLists("(docoll f coll)");
			
			setDoc( "Applies f to the items of the collection presumably for side effects. " +
					"Returns nil. ");
			
			setExamples(
					"(docoll \n" +
					"   (fn [x] (println x)) \n" +
					"   [1 2 3 4])",
					"(docoll \n" +
					"    (fn [[k v]] (println (pr-str k v)))  \n" +
					"    {:a 1 :b 2 :c 3 :d 4})");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("docoll", args, 2);

			final VncFunction fn = Coerce.toVncFunction(args.first());
			final VncVal coll = args.second();
			
			if (coll == Nil) {
				// ok do nothing
			}
			else if (Types.isVncList(coll)) {
				((VncList)coll).forEach(v -> fn.apply(new VncList(v)));
			}
			else if (Types.isVncJavaList(coll)) {
				((VncJavaList)coll).forEach(v -> fn.apply(new VncList(v)));
			}
			else if (Types.isVncMap(coll)) {
				((VncMap)coll).entries().forEach(v -> fn.apply(new VncList(new VncVector(v.getKey(), v.getValue()))));
			}
			else {
				throw new VncException(String.format(
						"docoll: collection type %s not supported. %s",
						Types.getClassName(coll),
						ErrorMessage.buildErrLocation(args)));
			}
				
			return Nil;
		}
	};

	public static VncFunction mapcat = new VncFunction("mapcat") {
		{
			setArgLists("(mapcat fn & colls)");
			
			setDoc( "Returns the result of applying concat to the result of applying map " + 
					"to fn and colls. Thus function fn should return a collection.");

			setExamples(
					"(mapcat reverse [[3 2 1 0] [6 5 4] [9 8 7]])");
		}
		
		public VncVal apply(final VncList args) {			
			return concat.apply(Coerce.toVncList(map.apply(args)));
		}
	};

	public static VncFunction filter = new VncFunction("filter") {
		{
			setArgLists("(filter predicate coll)");
			
			setDoc( "Returns a collection of the items in coll for which " + 
					"(predicate item) returns logical true. ");

			setExamples(
					"(filter even? [1 2 3 4 5 6 7])");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("filter", args, 2);
			
			final VncFunction predicate = Coerce.toVncFunction(args.nth(0));
			final VncList coll = Coerce.toVncList(args.nth(1));
			final VncList result = coll.empty();
			for(int i=0; i<coll.size(); i++) {
				final VncVal val = coll.nth(i);
				final VncVal keep = predicate.apply(new VncList(val));
				if (!(keep == False || keep == Nil)) {
					result.getList().add(val);
				}				
			}
			return result;
		}
	};

	public static VncFunction remove = new VncFunction("remove") {
		{
			setArgLists("(remove predicate coll)");
			
			setDoc( "Returns a collection of the items in coll for which " + 
					"(predicate item) returns logical false. ");

			setExamples(
					"(filter even? [1 2 3 4 5 6 7])");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("remove", args, 2);
			
			final VncFunction predicate = Coerce.toVncFunction(args.nth(0));
			final VncList coll = Coerce.toVncList(args.nth(1));
			final VncList result = coll.empty();
			for(int i=0; i<coll.size(); i++) {
				final VncVal val = coll.nth(i);
				final VncVal keep = predicate.apply(new VncList(val));
				if (keep == False) {
					result.getList().add(val);
				}				
			}
			return result;
		}
	};
	
	public static VncFunction reduce = new VncFunction("reduce") {
		{
			setArgLists("(reduce f coll)", "(reduce f val coll)");
			
			setDoc( "f should be a function of 2 arguments. If val is not supplied, " + 
					"returns the result of applying f to the first 2 items in coll, then " + 
					"applying f to that result and the 3rd item, etc. If coll contains no " + 
					"items, f must accept no arguments as well, and reduce returns the " + 
					"result of calling f with no arguments.  If coll has only 1 item, it " + 
					"is returned and f is not called.  If val is supplied, returns the " + 
					"result of applying f to val and the first item in coll, then " + 
					"applying f to that result and the 2nd item, etc. If coll contains no " + 
					"items, returns val and f is not called.");

			setExamples(
					"(reduce (fn [x y] (+ x y)) [1 2 3 4 5 6 7])",
					"(reduce (fn [x y] (+ x y)) 10 [1 2 3 4 5 6 7])",
					"((reduce comp [(partial + 1) (partial * 2) (partial + 3)]) 100)",
					"(reduce (fn [m [k v]] (assoc m v k)) {} {:b 2 :a 1 :c 3})");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("reduce", args, 2, 3);
			
			final boolean twoArguments = args.size() < 3;
			final VncFunction reduceFn = Coerce.toVncFunction(args.nth(0));

			if (twoArguments) {
				List<VncVal> coll;
				
				if (Types.isVncList(args.nth(1))) {
					coll = Coerce.toVncList(args.nth(1)).getList();
				}
				else if (Types.isVncMap(args.nth(1))) {
					coll = Coerce.toVncMap(args.nth(1)).toVncList().getList();
				}
				else {
					throw new VncException(String.format(
							"Function 'reduce' does not allow %s as coll parameter. %s", 
							Types.getClassName(args.nth(1)),
							ErrorMessage.buildErrLocation(args)));
				}
				
				if (coll.isEmpty()) {
					return reduceFn.apply(new VncList());
				}
				else {
					VncVal value = coll.get(0);
					for(int ii=1; ii<coll.size(); ii++) {
						value = reduceFn.apply(new VncList(value, coll.get(ii)));
					}
					return value;
				}
			}
			else {
				List<VncVal> coll;
				
				if (Types.isVncList(args.nth(2))) {
					coll = Coerce.toVncList(args.nth(2)).getList();
				}
				else if (Types.isVncMap(args.nth(2))) {
					coll = Coerce.toVncMap(args.nth(2)).toVncList().getList();
				}
				else {
					throw new VncException(String.format(
							"Function 'reduce' does not allow %s as coll parameter. %s", 
							Types.getClassName(args.nth(2)),
							ErrorMessage.buildErrLocation(args)));
				}
				
				if (coll.isEmpty()) {
					return args.nth(1);
				}
				else if (coll.size() == 1) {
					return reduceFn.apply(new VncList(args.nth(1), coll.get(0)));
				}
				else {
					VncVal value = args.nth(1);
					for(int ii=0; ii<coll.size(); ii++) {
						value = reduceFn.apply(new VncList(value, coll.get(ii)));
					}
					return value;
				}
			}
		}
	};
	
	public static VncFunction reduce_kv = new VncFunction("reduce-kv") {
		{
			setArgLists("(reduce-kv f init coll))");
			
			setDoc( "Reduces an associative collection. f should be a function of 3 " + 
					"arguments. Returns the result of applying f to init, the first key " + 
					"and the first value in coll, then applying f to that result and the " + 
					"2nd key and value, etc. If coll contains no entries, returns init " + 
					"and f is not called. Note that reduce-kv is supported on vectors, " + 
					"where the keys will be the ordinals.");

			setExamples(
					"(reduce-kv (fn [x y z] (assoc x z y)) {} {:a 1 :b 2 :c 3})");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("reduce-kv", args, 3);
			
			final VncFunction reduceFn = Coerce.toVncFunction(args.nth(0));		
			final Set<Map.Entry<VncVal,VncVal>> values = Coerce.toVncHashMap(args.nth(2)).entries();
			
			VncMap value = (VncMap)args.nth(1);
			
			if (values.isEmpty()) {
				return value;
			}
			else {
				for(Map.Entry<VncVal,VncVal> entry : values) {
					final VncVal key = entry.getKey();
					final VncVal val = entry.getValue();
					
					value = Coerce.toVncMap(reduceFn.apply(new VncList(value, key, val)));
				}
				
				return value;
			}
		}
	};

	public static VncFunction merge = new VncFunction("merge") {
		{
			setArgLists("(merge & maps)");
			
			setDoc( "Returns a map that consists of the rest of the maps conj-ed onto " +
					"the first.  If a key occurs in more than one map, the mapping from " +
					"the latter (left-to-right) will be the mapping in the result.");
			
			setExamples(
					"(merge {:a 1 :b 2 :c 3} {:b 9 :d 4})",
					"(merge {:a 1} nil)",
					"(merge nil {:a 1})",
					"(merge nil nil)");
		}

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
	};

	public static VncFunction conj = new VncFunction("conj") {
		{
			setArgLists("(conj coll x)", "(conj coll x & xs)");
			
			setDoc( "Returns a new collection with the x, xs " + 
					"'added'. (conj nil item) returns (item).  The 'addition' may " + 
					"happen at different 'places' depending on the concrete type.");
			
			setExamples(
					"(conj [1 2 3] 4)",
					"(conj '(1 2 3) 4)");
		}

		public VncVal apply(final VncList args) {			
			if (args.nth(0) instanceof VncVector) {
				final VncList new_seq = new VncVector();
				final VncList src_seq = (VncList)args.nth(0);
				new_seq.getList().addAll(src_seq.getList());
				for(int i=1; i<args.size(); i++) {
					new_seq.addAtEnd(args.nth(i));
				}
				return (VncVal)new_seq;
			} 
			else if (args.nth(0) instanceof VncList) {
				final VncList new_seq = new VncList();
				final VncList src_seq = (VncList)args.nth(0);
				new_seq.getList().addAll(src_seq.getList());
				for(int i=1; i<args.size(); i++) {
					new_seq.addAtStart(args.nth(i));
				}
				return (VncVal)new_seq;
			}
			else if (args.nth(0) instanceof VncMap) {
				final VncMap src_map = (VncMap)args.nth(0);
				final VncMap new_map = src_map.copy();
			
				if (Types.isVncVector(args.nth(1)) && ((VncVector)args.nth(1)).size() == 2) {
					return new_map.assoc(
								new VncList(
									((VncVector)args.nth(1)).nth(0),
									((VncVector)args.nth(1)).nth(1)));
				}
				else if (Types.isVncMap(args.nth(1))) {
					new_map.getMap().putAll(((VncMap)args.nth(1)).getMap());
					return new_map;
				}
				else {
					throw new VncException(String.format(
							"Invalid x %s while calling function 'conj'. %s",
							Types.getClassName(args.nth(1)),
							ErrorMessage.buildErrLocation(args)));
				}
			}
			else {
				throw new VncException(String.format(
						"Invalid coll %s while calling function 'conj'. %s",
						Types.getClassName(args.nth(0)),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};

	public static VncFunction seq = new VncFunction("seq") {
		{
			setArgLists("(seq coll)");
			
			setDoc( "Returns a seq on the collection. If the collection is " + 
					"empty, returns nil.  (seq nil) returns nil. seq also works on " + 
					"Strings.");
			
			setExamples(
					"(seq nil)",
					"(seq [1 2 3])",
					"(seq '(1 2 3))",
					"(seq {:a 1 :b 2})",
					"(seq \"abcd\")");
		}

		public VncVal apply(final VncList args) {
			assertArity("seq", args, 1);

			final VncVal val = args.nth(0);
			if (Types.isVncMap(val)) {
				if (((VncMap)val).isEmpty()) { 
					return Nil; 
				}
				return new VncList(
						((VncMap)val)
							.entries()
							.stream()
							.map(e -> new VncVector(e.getKey(), e.getValue()))
							.collect(Collectors.toList()));
			} 
			if (Types.isVncVector(val)) {
				if (((VncVector)val).isEmpty()) { 
					return Nil; 
				}
				return new VncList(((VncVector)val).getList());
			} 
			else if (Types.isVncList(val)) {
				if (((VncList)val).isEmpty()) { 
					return Nil; 
				}
				return val;
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
				throw new VncException(String.format(
						"seq: called on non-sequence. %s",
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};

	public static VncFunction repeat = new VncFunction("repeat") {
		{
			setArgLists("(repeat n x)");
			
			setDoc("Returns a collection with the value x repeated n times");
			
			setExamples(
					"(repeat 5 [1 2])");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("repeat", args, 2);

			if (!Types.isVncLong(args.nth(0))) {
				throw new VncException("repeat: the count must be a long");	
			}
			
			final long repeat = ((VncLong)args.nth(0)).getValue();
			if (repeat < 0) {
				throw new VncException(String.format(
						"repeat: a count n must be grater or equal to 0. %s",
						ErrorMessage.buildErrLocation(args)));	
			}

			final VncVal val = args.nth(1);
			final List<VncVal> values = new ArrayList<>();
			for(int ii=0; ii<repeat; ii++) {
				values.add(val.copy());
			}			
			return new VncList(values);
		}
	};

	public static VncFunction repeatedly = new VncFunction("repeatedly") {
		{
			setArgLists("(repeatedly n fn)");
			
			setDoc("Takes a function of no args, presumably with side effects, and " + 
				   "returns a collection of n calls to it");
			
			setExamples(
					"(repeatedly 5 (fn [] (rand-long 11)))",
					";; compare with repeat, which only calls the 'rand-long'\n" +
					";; function once, repeating the value five times. \n" +
					"(repeat 5 (rand-long 11))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("repeatedly", args, 2);

			
			final long repeat = Coerce.toVncLong(args.first()).getValue();
			final VncFunction fn = Coerce.toVncFunction(args.second());
			
			if (repeat < 0) {
				throw new VncException(String.format(
						"repeatedly: a count n must be grater or equal to 0. %s",
						ErrorMessage.buildErrLocation(args)));	
			}

			final List<VncVal> values = new ArrayList<>();
			for(int ii=0; ii<repeat; ii++) {
				values.add(fn.apply(new VncList()));
			}			
			return new VncList(values);
		}
	};


	
	///////////////////////////////////////////////////////////////////////////
	// Meta functions
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction meta = new VncFunction("meta") {
		{
			setArgLists("(meta obj)");
			
			setDoc("Returns the metadata of obj, returns nil if there is no metadata.");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("meta", args, 1);

			return args.nth(0).getMeta();
		}
	};

	public static VncFunction with_meta = new VncFunction("with-meta") {
		{
			setArgLists("(with-meta obj m)");
			
			setDoc("Returns a copy of the object obj, with a map m as its metadata.");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("meta", args, 2);

			if (!Types.isVncMap(args.nth(1))) {
				throw new VncException(String.format(
						"with-meta: the meta data for the object must be a map. %s",
						ErrorMessage.buildErrLocation(args)));	
			}

			final VncVal new_obj = args.nth(0).copy();
			new_obj.setMeta(args.nth(1));
			return new_obj;
		}
	};

	public static VncFunction vary_meta = new VncFunction("vary-meta") {
		{
			setArgLists("(vary-meta obj f & args)");
			
			setDoc("Returns a copy of the object obj, with (apply f (meta obj) args) as its metadata.");
		}
		
		public VncVal apply(final VncList args) {
			assertMinArity("vary-meta", args, 2);

			if (!Types.isVncFunction(args.nth(1))) {
				throw new VncException(String.format(
						"var-meta requires a function as second argument. %s",
						ErrorMessage.buildErrLocation(args)));
			}

			final VncVal meta = args.nth(0).getMeta();
			final VncFunction fn = (VncFunction)args.nth(1);
			final VncList fnArgs = args.slice(2);
			fnArgs.addAtStart(meta == Nil ? new VncHashMap() : meta);
			
			final VncVal new_obj = args.nth(0).copy();
			new_obj.setMeta(fn.apply(fnArgs));
			return new_obj;
		}
	};


	


	///////////////////////////////////////////////////////////////////////////
	// Utilities
	///////////////////////////////////////////////////////////////////////////


	public static VncFunction gensym = new VncFunction("gensym") {
		{
			setArgLists("(gensym)", "(gensym prefix)");
			
			setDoc("Generates a symbol.");
			
			setExamples("(gensym )", "(gensym \"prefix_\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("gensym", args, 0, 1);
			
			final String prefix = args.isEmpty() 
									? "G__" 
									: Types.isVncSymbol(args.nth(0))
										? Coerce.toVncSymbol(args.nth(0)).getName()
										: Coerce.toVncString(args.nth(0)).getValue();
			
			return new VncSymbol(prefix + String.valueOf(gensymValue.incrementAndGet()));
		}
	};

	public static VncFunction name = new VncFunction("name") {
		{
			setArgLists("(name x)");
			
			setDoc("Returns the name String of a string, symbol or keyword.");
			
			setExamples(
					"(name :x)",
					"(name 'x)",
					"(name \"x\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("name", args, 1);
			
			final VncVal arg = args.first();
			
			if (Types.isVncKeyword(arg)) {
				return new VncString(((VncKeyword)arg).getValue());
			}
			else if (Types.isVncSymbol(arg)) {
				return new VncString(((VncSymbol)arg).getName());
			}
			else if (Types.isVncString(arg)) {
				return arg;
			}
			else {
				throw new VncException(String.format(
						"Function 'name' does not allow %s as parameter. %s", 
						Types.getClassName(arg),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};

	public static VncFunction type = new VncFunction("type") {
		{
			setArgLists("(type x)");
			
			setDoc("Returns the type of x.");
			
			setExamples(
					"(type 5)",
					"(type (. :java.time.ZonedDateTime :now))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("type", args, 1);
			return Types.getClassName(args.first());
		}
	};
	
	private static void flatten(final VncVal value, final List<VncVal> result) {
		if (Types.isVncList(value)) {
			((VncList)value).getList().forEach(v -> flatten(v, result));
		}
		else if (Types.isVncHashMap(value)) {
			((VncHashMap)value).entries().forEach(e -> {
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
		else if (Types.isVncList(coll)) {
			return new VncList(
					((VncList)coll)
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
					 ((VncMap)coll).toVncList()
						.getList()
						.stream()
						.sorted(c)
						.collect(Collectors.toList()));
		}
		else {
			throw new VncException(String.format(
					"%s: collection type not supported. %s",
					fnName,
					ErrorMessage.buildErrLocation(fnArgs)));
		}
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

				.put("match",				match_Q)
				.put("match-not",			match_not_Q)
				
				.put("boolean",				boolean_cast)
				.put("long",				long_cast)
				.put("double",				double_cast)
				.put("decimal",				decimal_cast)
				.put("bytebuf",				bytebuf_cast)
				.put("bytebuf-to-string",	bytebuf_to_string)
				.put("bytebuf-from-string",	bytebuf_from_string)			
				
				.put("list",				new_list)
				.put("list?",				list_Q)
				.put("vector",				new_vector)
				.put("vector?",				vector_Q)
				.put("map?",				map_Q)
				.put("hash-map?",			hash_map_Q)
				.put("ordered-map?",		ordered_map_Q)
				.put("sorted-map?",			sorted_map_Q)
				.put("hash-map",			new_hash_map)
				.put("ordered-map",			new_ordered_map)
				.put("sorted-map",			new_sorted_map)
				.put("assoc",				assoc)
				.put("assoc-in",			assoc_in)				
				.put("dissoc",				dissoc)
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
				.put("subvec", 				subvec)
				.put("bytebuf-sub", 		bytebuf_sub)
				.put("empty", 				empty)

				.put("set?",				set_Q)
				.put("set",					new_set)
				.put("difference", 			difference)
				.put("union", 				union)
				.put("intersection", 		intersection)

				.put("split-with",			split_with)
				.put("into",				into)
				.put("sequential?",	    	sequential_Q)
				.put("coll?",	    		coll_Q)
				.put("cons",				cons)
				.put("co",					cons)
				.put("concat",				concat)
				.put("interpose",			interpose)
				.put("interleave",			interleave)
				.put("mapcat",				mapcat)
				.put("keep",				keep)
				.put("docoll",				docoll)
				.put("nth",					nth)
				.put("first",				first)
				.put("second",				second)
				.put("last",				last)
				.put("rest",				rest)
				.put("butlast",				butlast)
				.put("nfirst",				nfirst)
				.put("nlast",				nlast)
				.put("empty-to-nil",		emptyToNil)
				.put("pop",					pop)
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
				.put("reduce-kv", 			reduce_kv)
				.put("take", 				take)
				.put("take-while", 			take_while)
				.put("drop", 				drop)
				.put("drop-while", 			drop_while)
				.put("flatten", 			flatten)
				.put("reverse", 			reverse)
				.put("group-by", 			group_by)
				.put("sort", 				sort)
				.put("sort-by", 			sort_by)
		
				.put("merge",				merge)
				.put("conj",				conj)
				.put("seq",					seq)
				.put("repeat",				repeat)
				.put("repeatedly",			repeatedly)
		
				.put("meta",				meta)
				.put("with-meta",			with_meta)
				.put("vary-meta",			vary_meta)
				
				.put("coalesce", 			coalesce)
				
				.put("gensym",				gensym)
				.put("name",				name)
				.put("type",				type)
				
				.put("class",				className)	
				.put("load-core-module",	loadCoreModule)
				.put("load-classpath-venice", loadClasspathVenice)
							
				.toMap();

	
	private static final AtomicLong gensymValue = new AtomicLong(0);
}
