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

import static com.github.jlangch.venice.impl.types.Constants.False;
import static com.github.jlangch.venice.impl.types.Constants.Nil;
import static com.github.jlangch.venice.impl.types.Constants.True;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.github.jlangch.venice.ArityException;
import com.github.jlangch.venice.ContinueException;
import com.github.jlangch.venice.EofException;
import com.github.jlangch.venice.ValueException;
import com.github.jlangch.venice.Version;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.javainterop.JavaInterop;
import com.github.jlangch.venice.impl.javainterop.JavaInteropUtil;
import com.github.jlangch.venice.impl.types.Coerce;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.Types;
import com.github.jlangch.venice.impl.types.VncAtom;
import com.github.jlangch.venice.impl.types.VncBigDecimal;
import com.github.jlangch.venice.impl.types.VncByteBuffer;
import com.github.jlangch.venice.impl.types.VncDouble;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
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
import com.github.jlangch.venice.impl.util.StreamUtil;
import com.github.jlangch.venice.impl.util.StringUtil;


public class CoreFunctions {

	///////////////////////////////////////////////////////////////////////////
	// Documentation
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction doc = new VncFunction("doc") {
		{
			setArgLists("(doc name)");
			
			setDoc("Returns the documentation for the function/macro with the given name");
		}
		public VncVal apply(final VncList args) {
			assertArity("doc", args, 1);
			
			return new VncString(Doc.getDoc(Coerce.toVncString(args.first()).getValue()));
		}
	};



	///////////////////////////////////////////////////////////////////////////
	// Errors/Exceptions
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction throw_ex = new VncFunction("throw") {
		{
			setArgLists("(throw)", "(throw x)");
			
			setDoc("Throws exception with passed value x");
			
			setExamples(
					"(try (throw 100) (catch (do (+ 1 2) -1)))",
					"(try (throw 100) (catch (do (+ 1 2) -1)) (finally -2))");
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


	public static VncFunction slurp = new VncFunction("slurp") {
		{
			setArgLists("(slurp file & options)");
			
			setDoc( "Returns the file's content as text (string) or binary (bytebuf). " +
					"Defaults to binary=false and encoding=UTF-8. " +
					"Options: :encoding \"UTF-8\" :binary true/false. ");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().checkBlackListedVeniceFunction("slurp", args);

			assertMinArity("slurp", args, 1);

			try {	
				File file;
				
				if (Types.isVncString(args.nth(0)) ) {
					file = new File(((VncString)args.nth(0)).getValue());
				}
				else if (isJavaIoFile(args.nth(0)) ) {
					file = (File)(Coerce.toVncJavaObject(args.nth(0)).getDelegate());
				}
				else {
					throw new VncException(String.format(
							"Function 'slurp' does not allow %s as f. %s",
							Types.getClassName(args.nth(0)),
							ErrorMessage.buildErrLocation(args)));
				}

				
				final VncHashMap options = new VncHashMap(args.slice(1));

				final VncVal binary = options.get(new VncKeyword("binary")); 
				
				if (binary == True) {
					final byte[] data = Files.readAllBytes(file.toPath());
					
					return new VncByteBuffer(ByteBuffer.wrap(data));
				}
				else {
					final VncVal encVal = options.get(new VncKeyword("encoding")); 
					
					final String encoding = encVal == Nil ? "UTF-8" : Coerce.toVncString(encVal).getValue();
									
					final byte[] data = Files.readAllBytes(file.toPath());
					
					return new VncString(new String(data, encoding));
				}
			} 
			catch (Exception ex) {
				throw new VncException(ex.getMessage(), ex);
			}
		}
	};

	public static VncFunction spit = new VncFunction("spit") {
		{
			setArgLists("(spit f content & options)");
			
			setDoc( "Opens f, writes content, and then closes f. Defaults to append=true " +
					"and encoding=UTF-8. " +
					"Options: :append true/false, :encoding \"UTF-8\"");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().checkBlackListedVeniceFunction("spit", args);


			assertMinArity("spit", args, 2);

			try {
				// Currently just string content is supported!
				
				File file;
				
				if (Types.isVncString(args.nth(0)) ) {
					file = new File(((VncString)args.nth(0)).getValue());
				}
				else if (isJavaIoFile(args.nth(0)) ) {
					file = (File)(Coerce.toVncJavaObject(args.nth(0)).getDelegate());
				}
				else {
					throw new VncException(String.format(
							"Function 'spit' does not allow %s as f. %s",
							Types.getClassName(args.nth(0)),
							ErrorMessage.buildErrLocation(args)));
				}

		
				final VncVal content = args.nth(1);

				final VncHashMap options = new VncHashMap(args.slice(2));

				final VncVal append = options.get(new VncKeyword("append")); 
				
				final VncVal encVal = options.get(new VncKeyword("encoding")); 
					
				final String encoding = encVal == Nil ? "UTF-8" : ((VncString)encVal).getValue();

				byte[] data;
				
				if (Types.isVncString(content)) {
					data = ((VncString)content).getValue().getBytes(encoding);
				}
				else if (Types.isVncByteBuffer(content)) {
					data = ((VncByteBuffer)content).getValue().array();
				}
				else {
					throw new VncException(String.format(
							"Function 'spit' does not allow %s as content. %s",
							Types.getClassName(content),
							ErrorMessage.buildErrLocation(args)));
				}

				final List<OpenOption> openOptions = new ArrayList<>();
				openOptions.add(StandardOpenOption.CREATE);
				openOptions.add(StandardOpenOption.WRITE);
				
				if (append != False) {
					openOptions.add(StandardOpenOption.TRUNCATE_EXISTING);
				}
				
				Files.write(
						file.toPath(), 
						data, 
						openOptions.toArray(new OpenOption[0]));
				
				return Nil;
			} 
			catch (Exception ex) {
				throw new VncException(ex.getMessage(), ex);
			}
		}
	};

	public static VncFunction loadCoreModule = new VncFunction("load-core-module") {
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
	
	///////////////////////////////////////////////////////////////////////////
	// Number functions
	///////////////////////////////////////////////////////////////////////////
	
	public static VncFunction decimalScale = new VncFunction("dec/scale") {
		{
			setArgLists("(dec/scale x scale rounding-mode)");
			
			setDoc( "Scales a decimal. rounding-mode is one of (:CEILING, :DOWN, " +
					":FLOOR, :HALF_DOWN, :HALF_EVEN, :HALF_UP, :UNNECESSARY, :UP)");
			
			setExamples(
					"(dec/scale 2.44697M 0 :HALF_UP)",
					"(dec/scale 2.44697M 1 :HALF_UP)",
					"(dec/scale 2.44697M 2 :HALF_UP)",
					"(dec/scale 2.44697M 3 :HALF_UP)",
					"(dec/scale 2.44697M 10 :HALF_UP)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("dec/scale", args, 3);

			final VncVal arg = args.nth(0);
			final VncLong scale = Coerce.toVncLong(args.nth(1));
			final RoundingMode roundingMode = VncBigDecimal.toRoundingMode((VncString)args.nth(2));
						
			if (Types.isVncBigDecimal(arg)) {
				final BigDecimal val = ((VncBigDecimal)arg).getValue();
				return new VncBigDecimal(val.setScale(scale.getValue().intValue(), roundingMode));
			}
			else {
				throw new VncException(String.format(
										"Function 'dec/scale' does not allow %s as operand 1. %s",
										Types.getClassName(arg),
										ErrorMessage.buildErrLocation(args)));
			}
		}
	};
	
	public static VncFunction decimalAdd = new VncFunction("dec/add") {
		{
			setArgLists("(dec/add x y scale rounding-mode)");
			
			setDoc( "Adds two decimals and scales the result. rounding-mode is one of (:CEILING, :DOWN, " +
					":FLOOR, :HALF_DOWN, :HALF_EVEN, :HALF_UP, :UNNECESSARY, :UP)");
			
			setExamples("(dec/add 2.44697M 1.79882M 3 :HALF_UP)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("dec/add", args, 4);

			final VncBigDecimal op1 = Coerce.toVncBigDecimal(args.nth(0));
			final VncBigDecimal op2 = Coerce.toVncBigDecimal(args.nth(1));
			final VncLong scale = Coerce.toVncLong(args.nth(2));
			final RoundingMode roundingMode = VncBigDecimal.toRoundingMode(Coerce.toVncString(args.nth(3)));
				
			return new VncBigDecimal(op1.getValue()
							.add(op2.getValue())
							.setScale(scale.getValue().intValue(), roundingMode));
		}
	};
	
	public static VncFunction decimalSubtract = new VncFunction("dec/sub") {
		{
			setArgLists("(dec/sub x y scale rounding-mode)");
			
			setDoc( "Subtract y from x and scales the result. rounding-mode is one of (:CEILING, :DOWN, " +
					":FLOOR, :HALF_DOWN, :HALF_EVEN, :HALF_UP, :UNNECESSARY, :UP)");
			
			setExamples("(dec/sub 2.44697M 1.79882M 3 :HALF_UP)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("dec/sub", args, 4);

			final VncBigDecimal op1 = Coerce.toVncBigDecimal(args.nth(0));
			final VncBigDecimal op2 = Coerce.toVncBigDecimal(args.nth(1));
			final VncLong scale = Coerce.toVncLong(args.nth(2));
			final RoundingMode roundingMode = VncBigDecimal.toRoundingMode(Coerce.toVncString(args.nth(3)));
				
			return new VncBigDecimal(op1.getValue().subtract(op2.getValue()).setScale(scale.getValue().intValue(), roundingMode));
		}
	};
	
	public static VncFunction decimalMultiply = new VncFunction("dec/mul") {
		{
			setArgLists("(dec/mul x y scale rounding-mode)");
			
			setDoc( "Multiplies two decimals and scales the result. rounding-mode is one of (:CEILING, :DOWN, " +
					":FLOOR, :HALF_DOWN, :HALF_EVEN, :HALF_UP, :UNNECESSARY, :UP)");
			
			setExamples("(dec/mul 2.44697M 1.79882M 5 :HALF_UP)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("dec/mul", args, 4);

			final VncBigDecimal op1 = Coerce.toVncBigDecimal(args.nth(0));
			final VncBigDecimal op2 = Coerce.toVncBigDecimal(args.nth(1));
			final VncLong scale = Coerce.toVncLong(args.nth(2));
			final RoundingMode roundingMode = VncBigDecimal.toRoundingMode(Coerce.toVncString(args.nth(3)));
				
			return new VncBigDecimal(op1.getValue().multiply(op2.getValue()).setScale(scale.getValue().intValue(), roundingMode));
		}
	};
	
	public static VncFunction decimalDivide = new VncFunction("dec/div") {
		{
			setArgLists("(dec/div x y scale rounding-mode)");
			
			setDoc( "Divides x by y and scales the result. rounding-mode is one of (:CEILING, :DOWN, " +
					":FLOOR, :HALF_DOWN, :HALF_EVEN, :HALF_UP, :UNNECESSARY, :UP)");
			
			setExamples("(dec/div 2.44697M 1.79882M 5 :HALF_UP)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("dec/div", args, 4);

			final VncBigDecimal op1 = Coerce.toVncBigDecimal(args.nth(0));
			final VncBigDecimal op2 = Coerce.toVncBigDecimal(args.nth(1));
			final VncLong scale = Coerce.toVncLong(args.nth(2));
			final RoundingMode roundingMode = VncBigDecimal.toRoundingMode(Coerce.toVncString(args.nth(3)));
				
			return new VncBigDecimal(op1.getValue().divide(op2.getValue(), scale.getValue().intValue(), roundingMode));
		}
	};
	
	public static VncFunction add = new VncFunction("+") {
		{
			setArgLists("(+)", "(+ x)", "(+ x y)", "(+ x y & more)");
			
			setDoc("Returns the sum of the numbers. (+) returns 0.");
			
			setExamples("(+)", "(+ 1)", "(+ 1 2)", "(+ 1 2 3 4)");
		}
		
		public VncVal apply(final VncList args) {
			if (args.isEmpty()) {
				return new VncLong(0);
			}
			else if (args.size() == 1) {
				return args.nth(0);
			}

			VncVal val = args.first();
			for(VncVal v : args.slice(1).getList()) { val = Calc.add(val, v); }
			return val;
		}
	};
	
	public static VncFunction subtract = new VncFunction("-") {
		{
			setArgLists("(- x)", "(- x y)", "(- x y & more)");
			
			setDoc( "If one number is supplied, returns the negation, else subtracts " +
					"the numbers from x and returns the result.");
			
			setExamples("(- 4)", "(- 8 3 -2 -1)", "(- 8 2.5)", "(- 8 1.5M)");
		}
		
		public VncVal apply(final VncList args) {
			if (args.isEmpty()) {
				throw new ArityException(args, 0, "-");
			}
			else if (args.size() == 1) {
				final VncVal first = args.nth(0);
				if (Types.isVncLong(first)) {
					return Calc.mul(first, new VncLong(-1L));
				}
				else if (Types.isVncDouble(first)) {
					return Calc.mul(first, new VncDouble(-1D));
				}
				else if (Types.isVncBigDecimal(first)) {
					return Calc.mul(first, new VncBigDecimal(new BigDecimal("-1.0")));
				}
				else {
					return first;
				}
			}

			VncVal val = args.first();
			for(VncVal v : args.slice(1).getList()) { val = Calc.sub(val, v); }
			return val;
		}
	};
	
	public static VncFunction multiply = new VncFunction("*") {
		{
			setArgLists("(*)", "(* x)", "(* x y)", "(* x y & more)");
			
			setDoc("Returns the product of numbers. (*) returns 1");
			
			setExamples("(*)", "(* 4)", "(* 4 3)", "(* 4 3 2)", "(* 6.0 2)", "(* 6 1.5M)");
		}
		
		public VncVal apply(final VncList args) {
			if (args.isEmpty()) {
				return new VncLong(1);
			}
			else if (args.size() == 1) {
				return args.nth(0);
			}

			VncVal val = args.first();
			for(VncVal v : args.slice(1).getList()) { val = Calc.mul(val, v); }
			return val;
		}
	};
	
	public static VncFunction divide = new VncFunction("/") {
		{
			setArgLists("(/ x)", "(/ x y)", "(/ x y & more)");
			
			setDoc( "If no denominators are supplied, returns 1/numerator, " + 
					"else returns numerator divided by all of the denominators.");
			
			setExamples("(/ 2.0)", "(/ 12 2 3)", "(/ 12 3)", "(/ 6.0 2)", "(/ 6 1.5M)");
		}
		
		public VncVal apply(final VncList args) {
			if (args.isEmpty()) {
				throw new ArityException(args, 0, "/");
			}
			else if (args.size() == 1) {
				final VncVal first = args.nth(0);
				if (Types.isVncLong(first)) {
					return Calc.div(new VncLong(1L), first);
				}
				else if (Types.isVncDouble(first)) {
					return Calc.div(new VncDouble(1D), first);
				}
				else if (Types.isVncBigDecimal(first)) {
					return Calc.div(new VncBigDecimal(BigDecimal.ONE), first);
				}
				else {
					return first;
				}
			}

			VncVal val = args.first();
			for(VncVal v : args.slice(1).getList()) { val = Calc.div(val, v); }
			return val;
		}
	};
	
	public static VncFunction modulo = new VncFunction("mod") {
		{
			setArgLists("(mod n d)");
			
			setDoc("Modulus of n and d.");
			
			setExamples("(mod 10 4)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("abs", args, 2);

			if (!Types.isVncLong(args.nth(0))) {
				throw new VncException(String.format(
						"Function 'mod' does not allow %s as numerator. %s", 
						Types.getClassName(args.nth(0)),
						ErrorMessage.buildErrLocation(args)));
			}
			if (!Types.isVncLong(args.nth(1))) {
				throw new VncException(String.format(
						"Function 'mod' does not allow %s as denominator. %s", 
						Types.getClassName(args.nth(1)),
						ErrorMessage.buildErrLocation(args)));
			}
			
			return new VncLong(
						((VncLong)args.nth(0)).getValue().longValue() 
						% 
						((VncLong)args.nth(1)).getValue().longValue());
		}
	};
	
	public static VncFunction inc = new VncFunction("inc") {
		{
			setArgLists("(inc x)");
			
			setDoc("Increments the number x");
			
			setExamples("(inc 10)", "(inc 10.1)", "(inc 10.12M)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("inc", args, 1);

			final VncVal arg = args.nth(0);
			if (Types.isVncLong(arg)) {
				return ((VncLong)arg).inc();
			}
			else if (Types.isVncDouble(arg)) {
				return ((VncDouble)arg).inc();
			}
			else if (Types.isVncBigDecimal(arg)) {
				return ((VncBigDecimal)arg).inc();
			}
			else {
				throw new VncException(String.format(
						"Invalid argument type %s while calling function 'inc'. %s",
						Types.getClassName(arg),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};
	
	public static VncFunction dec = new VncFunction("dec") {
		{
			setArgLists("(dec x)");
			
			setDoc("Decrements the number x");
			
			setExamples("(dec 10)", "(dec 10.1)", "(dec 10.12M)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("dec", args, 1);

			final VncVal arg = args.nth(0);
			if (Types.isVncLong(arg)) {
				return ((VncLong)arg).dec();
			}
			else if (Types.isVncDouble(arg)) {
				return ((VncDouble)arg).dec();
			}
			else if (Types.isVncBigDecimal(arg)) {
				return ((VncBigDecimal)arg).dec();
			}
			else {
				throw new VncException(String.format(
						"Invalid argument type %s while calling function 'dec'. %s",
						Types.getClassName(arg),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};
	
	public static VncFunction max = new VncFunction("max") {
		{
			setArgLists("(max x)", "(max x y)", "(max x y & more)");
			
			setDoc("Returns the greatest of the values");
			
			setExamples(
					"(max 1)", "(max 1 2)", "(max 4 3 2 1)",
					"(max 1.0)", "(max 1.0 2.0)", "(max 4.0 3.0 2.0 1.0)",
					"(max 1.0M)", "(max 1.0M 2.0M)", "(max 4.0M 3.0M 2.0M 1.0M)",
					"(max 1.0M 2)");
		}
		
		public VncVal apply(final VncList args) {
			if (args.isEmpty()) {
				throw new ArityException(args, 0, "max");
			}

			final VncVal op1 = args.nth(0);
			
			VncVal max = op1;
			for(VncVal op : args.rest().getList()) {
				if (Types.isVncLong(max)) {
					max = ((VncLong)max).gte(op) == True ? max : op;
				}
				else if (Types.isVncDouble(max)) {
					max = ((VncDouble)max).gte(op) == True ? max : op;
				}
				else if (Types.isVncBigDecimal(max)) {
					max = ((VncBigDecimal)max).gte(op) == True ? max : op;
				}
				else {
					throw new VncException(String.format(
											"Function 'max' does not allow %s as operand 1. %s", 
											Types.getClassName(max),
											ErrorMessage.buildErrLocation(args)));
				}
			}
			
			return max;			
		}
	};
	
	public static VncFunction min = new VncFunction("min") {
		{
			setArgLists("(min x)", "(min x y)", "(min x y & more)");
			
			setDoc("Returns the smallest of the values");
			
			setExamples(
					"(min 1)", "(min 1 2)", "(min 4 3 2 1)",
					"(min 1.0)", "(min 1.0 2.0)", "(min 4.0 3.0 2.0 1.0)",
					"(min 1.0M)", "(min 1.0M 2.0M)", "(min 4.0M 3.0M 2.0M 1.0M)",
					"(min 1.0M 2)");
		}
		
		public VncVal apply(final VncList args) {
			if (args.isEmpty()) {
				throw new ArityException(args, 0, "min");
			}
			
			final VncVal op1 = args.nth(0);
			
			VncVal min = op1;
			for(VncVal op : args.rest().getList()) {
				if (Types.isVncLong(min)) {
					min = ((VncLong)min).lte(op) == True ? min : op;
				}
				else if (Types.isVncDouble(min)) {
					min = ((VncDouble)min).lte(op) == True ? min : op;
				}
				else if (Types.isVncBigDecimal(min)) {
					min = ((VncBigDecimal)min).lte(op) == True ? min : op;
				}
				else {
					throw new VncException(String.format(
											"Function 'min' does not allow %s as operand 1. %s", 
											Types.getClassName(min),
											ErrorMessage.buildErrLocation(args)));
				}
			}
			
			return min;			
		}
	};
	
	public static VncFunction abs = new VncFunction("abs") {
		{
			setArgLists("(abs x)");
			
			setDoc("Returns the absolute value of the number");
			
			setExamples("(abs 10)", "(abs -10)", "(abs -10.1)", "(abs -10.12M)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("abs", args, 1);
			
			final VncVal arg = args.nth(0);
			
			if (Types.isVncLong(arg)) {
				return new VncLong(Math.abs(((VncLong)arg).getValue().longValue()));
			}
			else if (Types.isVncDouble(arg)) {
				return new VncDouble(Math.abs(((VncDouble)arg).getValue().doubleValue()));
			}
			else if (Types.isVncBigDecimal(arg)) {
				return new VncBigDecimal(((VncBigDecimal)arg).getValue().abs());
			}
			else {
				throw new VncException(String.format(
						"Invalid argument type %s while calling function 'abs'. %s",
						Types.getClassName(arg),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};
	
	public static VncFunction rand_long = new VncFunction("rand-long") {
		{
			setArgLists("(rand-long)", "(rand-long max)");
			
			setDoc( "Without argument returns a random long between 0 and MAX_LONG. " +
					"Without argument max returns a random long between 0 and max exclusive.");
			
			setExamples("(rand-long)", "(rand-long 100)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("rand-long", args, 0, 1);
			
			if (args.isEmpty()) {
				return new VncLong(Math.abs(random.nextLong()));
			}
			else {
				final long max = Coerce.toVncLong(args.first()).getValue();
				if (max < 2) {
					throw new VncException("Function 'rand-long' does not allow negative max values");

				}
				return new VncLong(Math.abs(random.nextLong()) % max);
			}
		}
	};
	public static VncFunction rand_double = new VncFunction("rand-double") {
		{
			setArgLists("(rand-double)", "(rand-double max)");
			
			setDoc( "Without argument returns a double between 0.0 and 1.0. " +
					"Without argument max returns a random double between 0.0 and max.");
			
			setExamples("(rand-double)", "(rand-double 100.0)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("rand-double", args, 0, 1);
			
			if (args.isEmpty()) {
				return new VncDouble(random.nextDouble());
			}
			else {
				final double max = Coerce.toVncDouble(args.first()).getValue();
				if (max < 0.0) {
					throw new VncException(String.format(
							"Function 'rand-double' does not allow negative max values. %s",
							ErrorMessage.buildErrLocation(args)));

				}
				return new VncDouble(random.nextDouble() * max);
			}
		}
	};

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
			if (Types.isVncLong(op1)) {
				return ((VncLong)op1).lt(op2);
			}
			else if (Types.isVncDouble(op1)) {
				return ((VncDouble)op1).lt(op2);
			}
			else if (Types.isVncBigDecimal(op1)) {
				return ((VncBigDecimal)op1).lt(op2);
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
			if (Types.isVncLong(op1)) {
				return ((VncLong)op1).lte(op2);
			}
			else if (Types.isVncDouble(op1)) {
				return ((VncDouble)op1).lte(op2);
			}
			else if (Types.isVncBigDecimal(op1)) {
				return ((VncBigDecimal)op1).lte(op2);
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
			if (Types.isVncLong(op1)) {
				return ((VncLong)op1).gt(op2);
			}
			else if (Types.isVncDouble(op1)) {
				return ((VncDouble)op1).gt(op2);
			}
			else if (Types.isVncBigDecimal(op1)) {
				return ((VncBigDecimal)op1).gt(op2);
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
			if (Types.isVncLong(op1)) {
				return ((VncLong)op1).gte(op2);
			}
			else if (Types.isVncDouble(op1)) {
				return ((VncDouble)op1).gte(op2);
			}
			else if (Types.isVncBigDecimal(op1)) {
				return ((VncBigDecimal)op1).gte(op2);
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
	
	public static VncFunction zero_Q = new VncFunction("zero?") {
		{
			setArgLists("(zero? x)");
			
			setDoc("Returns true if x zero else false");
			
			setExamples("(zero? 0)", "(zero? 2)", "(zero? 0.0)", "(zero? 0.0M)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("zero?", args, 1);
			
			final VncVal op1 = args.nth(0);
			if (Types.isVncLong(op1)) {
				return ((VncLong)op1).getValue() == 0 ? True : False;
			}
			else if (Types.isVncDouble(op1)) {
				return ((VncDouble)op1).getValue() == 0.0 ? True : False;
			}
			else if (Types.isVncBigDecimal(op1)) {
				return ((VncBigDecimal)op1).getValue().compareTo(BigDecimal.ZERO) == 0 ? True : False;
			}
			else {
				throw new VncException(String.format(
										"Function 'zero' does not allow %s as operand 1. %s", 
										Types.getClassName(op1),
										ErrorMessage.buildErrLocation(args)));
			}
		}
	};
	
	public static VncFunction pos_Q = new VncFunction("pos?") {
		{
			setArgLists("(pos? x)");
			
			setDoc("Returns true if x greater than zero else false");
			
			setExamples("(pos? 3)", "(pos? -3)", "(pos? 3.2)", "(pos? 3.2M)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("pos?", args, 1);
			
			final VncVal op1 = args.nth(0);
			if (Types.isVncLong(op1)) {
				return ((VncLong)op1).getValue() > 0 ? True : False;
			}
			else if (Types.isVncDouble(op1)) {
				return ((VncDouble)op1).getValue() > 0 ? True : False;
			}
			else if (Types.isVncBigDecimal(op1)) {
				return ((VncBigDecimal)op1).getValue().compareTo(BigDecimal.ZERO) > 0 ? True : False;
			}
			else {
				throw new VncException(String.format(
										"Function 'pos' does not allow %s as operand 1. %s", 
										Types.getClassName(op1),
										ErrorMessage.buildErrLocation(args)));
			}
		}
	};
	
	public static VncFunction neg_Q = new VncFunction("neg?") {
		{
			setArgLists("(neg? x)");
			
			setDoc("Returns true if x smaller than zero else false");
			
			setExamples("(neg? -3)", "(neg? 3)", "(neg? -3.2)", "(neg? -3.2M)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("neg?", args, 1);
			
			final VncVal op1 = args.nth(0);
			if (Types.isVncLong(op1)) {
				return ((VncLong)op1).getValue() < 0 ? True : False;
			}
			else if (Types.isVncDouble(op1)) {
				return ((VncDouble)op1).getValue() < 0 ? True : False;
			}
			else if (Types.isVncBigDecimal(op1)) {
				return ((VncBigDecimal)op1).getValue().compareTo(BigDecimal.ZERO) < 0 ? True : False;
			}
			else {
				throw new VncException(String.format(
										"Function 'plus' does not allow %s as operand 1. %s", 
										Types.getClassName(op1),
										ErrorMessage.buildErrLocation(args)));
			}
		}
	};
	
	public static VncFunction even_Q = new VncFunction("even?") {
		{
			setArgLists("(even? n)");
			
			setDoc("Returns true if n is even, throws an exception if n is not an integer");
			
			setExamples("(odd? 4)", "(odd? 3)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("even?", args, 1);
			
			final VncVal op1 = args.nth(0);
			if (Types.isVncLong(op1)) {
				return ((VncLong)op1).getValue() % 2 == 0 ? True : False;
			}
			else {
				throw new VncException(String.format(
										"Function 'even' does not allow %s as operand. %s", 
										Types.getClassName(op1),
										ErrorMessage.buildErrLocation(args)));
			}
		}
	};
	
	public static VncFunction odd_Q = new VncFunction("odd?") {
		{
			setArgLists("(odd? n)");
			
			setDoc("Returns true if n is odd, throws an exception if n is not an integer");
			
			setExamples("(odd? 3)", "(odd? 4)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("odd?", args, 1);
			
			final VncVal op1 = args.nth(0);
			if (Types.isVncLong(op1)) {
				return ((VncLong)op1).getValue() % 2 == 1 ? True : False;
			}
			else {
				throw new VncException(String.format(
										"Function 'odd' does not allow %s as operand. %s", 
										Types.getClassName(op1),
										ErrorMessage.buildErrLocation(args)));
			}
		}
	};

	public static VncFunction time_ms = new VncFunction("time-ms") {
		{
			setArgLists("(time-ms)");
			
			setDoc("Returns the current time in milliseconds");
			
			setExamples("(time-ms)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("time_ms", args, 0);
			
			return new VncLong(System.currentTimeMillis());
		}
	};

	public static VncFunction time_ns = new VncFunction("time-ns") {
		{
			setArgLists("(time-ns)");
			
			setDoc( "Returns the current value of the running Java Virtual Machine's " +
					"high-resolution time source, in nanoseconds.");
			
			setExamples("(time-ns)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("time_ns", args, 0);
			
			return new VncLong(System.nanoTime());
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
				else if (Types.isVncLong(arg)) {
					final BigDecimal dec = new BigDecimal(((VncLong)arg).getValue());
					return new VncBigDecimal(args.size() < 3 ? dec : dec.setScale(scale.getValue().intValue(), roundingMode));
				}
				else if (Types.isVncDouble(arg)) {
					final BigDecimal dec = VncBigDecimal.toDecimal((VncDouble)arg).getValue();
					return new VncBigDecimal(args.size() < 3 ? dec : dec.setScale(scale.getValue().intValue(), roundingMode));
				}
				else if (Types.isVncBigDecimal(arg)) {
					final BigDecimal dec = ((VncBigDecimal)arg).getValue();
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

	public static VncFunction subbytebuf = new VncFunction("subbytebuf") {
		{
			setArgLists("(subbytebuf x start) (subbytebuf x start end)");
			
			setDoc( "Returns a byte buffer of the items in buffer from start (inclusive) "+
					"to end (exclusive). If end is not supplied, defaults to " + 
					"(count bytebuffer)");
			
			setExamples(
					"(subbytebuf (bytebuf [1 2 3 4 5 6]) 2)", 
					"(subbytebuf (bytebuf [1 2 3 4 5 6]) 4)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("subbytebuf", args, 2, 3);

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
			else {
				final VncMap mhm = Coerce.toVncMap(args.nth(0));
				final VncVal key = args.nth(1);
				final VncVal key_not_found = (args.size() == 3) ? args.nth(2) : Nil;
				
				final VncVal value = mhm.get(key);
				return value != Nil ? value : key_not_found;
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
				final VncSequence coll = Coerce.toVncSequence(args.second());

				if (coll.isEmpty()) {
					return False;
				}
				
				return coll.getList()
						   .stream()
						   .allMatch(v -> pred.apply(new VncList(v)) == True) ? True : False;
			}
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
				final VncSequence coll = Coerce.toVncSequence(args.second());
				
				if (coll.isEmpty()) {
					return False;
				}
								
				return coll.getList()
						   .stream()
						   .anyMatch(v -> pred.apply(new VncList(v)) == True) ? True : False;
			}
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

			final VncVal exp = args.nth(0);
			if (exp == Nil) {
				return new VncList();
			}
			else if (Types.isVncList(exp)) {
				return ((VncList)exp).rest();
			}
			else {
				return ((VncVector)exp).rest();
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
					"(coalesce [])",
					"(coalesce [1 2])",
					"(coalesce [nil])",
					"(coalesce [nil 1 2])");
		}
		
		public VncVal apply(final VncList args) {
			return args.getList().stream().filter(v -> v != Nil).findFirst().orElse(Nil);
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
			
			setExamples(
					"(. :java.lang.Long :class)");
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
			setArgLists("(sort coll)", "(sort compfn coll)");
			
			setDoc( "Returns a sorted list of the items in coll. If no compare function " + 
					"compfn is supplied, uses the natural compare. The compare function " + 
					"takes two arguments and returns -1, 0, or 1");
			
			setExamples(
					"(sort [3 2 5 4 1 6])", 
					"(sort {:c 3 :a 1 :b 2})");
		}

		public VncVal apply(final VncList args) {
			assertArity("sort", args, 1, 2);

			if (args.size() == 1) {
				final VncVal coll = args.nth(0);
				
				if (Types.isVncVector(coll)) {
					return new VncVector(
							((VncVector)coll)
								.getList()
								.stream()
								.sorted()
								.collect(Collectors.toList()));
				}
				else if (Types.isVncList(coll)) {
					return new VncList(
							((VncList)coll)
								.getList()
								.stream()
								.sorted()
								.collect(Collectors.toList()));
				}
				else if (Types.isVncSet(coll)) {
					return new VncList(
							((VncSet)coll)
								.getList()
								.stream()
								.sorted()
								.collect(Collectors.toList()));
				}
				else if (Types.isVncMap(coll)) {
					return new VncList(
							 ((VncMap)coll).toVncList()
								.getList()
								.stream()
								.sorted()
								.collect(Collectors.toList()));
				}
				else {
					throw new VncException(String.format(
							"sort: collection type not supported. %s",
							ErrorMessage.buildErrLocation(args)));
				}
			}
			else if (args.size() == 2) {
				final VncFunction compfn = Coerce.toVncFunction(args.nth(0));
				final VncVal coll = args.nth(1);
				
				if (Types.isVncVector(coll)) {
					return new VncVector(
							((VncVector)coll)
								.getList()
								.stream()
								.sorted((x,y) -> ((VncLong)compfn.apply(new VncList(x,y))).getValue().intValue())
								.collect(Collectors.toList()));
				}
				else if (Types.isVncList(coll)) {
					return new VncList(
							((VncList)coll)
								.getList()
								.stream()
								.sorted((x,y) -> ((VncLong)compfn.apply(new VncList(x,y))).getValue().intValue())
								.collect(Collectors.toList()));
				}
				else if (Types.isVncSet(coll)) {
					return new VncList(
							((VncSet)coll)
								.getList()
								.stream()
								.sorted((x,y) -> ((VncLong)compfn.apply(new VncList(x,y))).getValue().intValue())
								.collect(Collectors.toList()));
				}
				else if (Types.isVncMap(coll)) {
					return new VncList(
							 ((VncMap)coll).toVncList()
								.getList()
								.stream()
								.sorted((x,y) -> ((VncLong)compfn.apply(new VncList(x,y))).getValue().intValue())
								.collect(Collectors.toList()));
				}
				else {
					throw new VncException(String.format(
							"sort: collection type not supported. %s",
							ErrorMessage.buildErrLocation(args)));
				}
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
					"(sort-by first [[1 2] [3 4] [2 3]])",
					"(sort-by (fn [x] (get x :rank)) [{:rank 2} {:rank 3} {:rank 1}])");
		}

		public VncVal apply(final VncList args) {
			assertArity("sort-by", args, 2, 3);

			if (args.size() == 2) {
				final VncFunction keyfn = Coerce.toVncFunction(args.nth(0));
				final VncVal coll = args.nth(1);
				
				if (Types.isVncVector(coll)) {
					return new VncVector(
							((VncVector)coll)
								.getList()
								.stream()
								.sorted((x,y) -> keyfn.apply(new VncList(x)).compareTo(keyfn.apply(new VncList(y))))
								.collect(Collectors.toList()));
				}
				else if (Types.isVncList(coll)) {
					return new VncList(
							((VncList)coll)
								.getList()
								.stream()
								.sorted((x,y) -> keyfn.apply(new VncList(x)).compareTo(keyfn.apply(new VncList(y))))
								.collect(Collectors.toList()));
				}
				else if (Types.isVncMap(coll)) {
					return new VncList(
							 ((VncMap)coll).toVncList()
								.getList()
								.stream()
								.sorted((x,y) -> keyfn.apply(new VncList(x)).compareTo(keyfn.apply(new VncList(y))))
								.collect(Collectors.toList()));
				}
				else {
					throw new VncException(String.format(
							"sort-by: collection type not supported. %s",
							ErrorMessage.buildErrLocation(args)));
				}
			}
			else if (args.size() == 3) {
				final VncFunction keyfn = Coerce.toVncFunction(args.nth(0));
				final VncFunction compfn = Coerce.toVncFunction(args.nth(1));
				final VncVal coll = args.nth(2);
				
				if (Types.isVncVector(coll)) {
					return new VncVector(
							((VncVector)coll)
								.getList()
								.stream()
								.sorted((x,y) -> Coerce.toVncLong(compfn.apply(
														new VncList(
															keyfn.apply(new VncList(x)),
															keyfn.apply(new VncList(y)))
														)
													).getValue().intValue())
								.collect(Collectors.toList()));
				}
				else if (Types.isVncList(coll)) {
					return new VncList(
							((VncList)coll)
								.getList()
								.stream()
								.sorted((x,y) -> Coerce.toVncLong(compfn.apply(
														new VncList(
															keyfn.apply(new VncList(x)),
															keyfn.apply(new VncList(y)))
														)
													).getValue().intValue())
								.collect(Collectors.toList()));
				}
				else if (Types.isVncMap(coll)) {
					return new VncList(
							 ((VncMap)coll).toVncList()
								.getList()
								.stream()
								.sorted((x,y) -> Coerce.toVncLong(compfn.apply(
														new VncList(
															keyfn.apply(new VncList(x)),
															keyfn.apply(new VncList(y)))
														)
													).getValue().intValue())
								.collect(Collectors.toList()));
				}
				else {
					throw new VncException(String.format(
							"sort-by: collection type not supported. %s",
							ErrorMessage.buildErrLocation(args)));
				}
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
	
	public static VncFunction partial = new VncFunction("partial") {
		{
			setArgLists("(partial f args*)");
			
			setDoc( "Takes a function f and fewer than the normal arguments to f, and " + 
					"returns a fn that takes a variable number of additional args. When " + 
					"called, the returned function calls f with args + additional args.");
			
			setExamples(
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
					return fn.apply(fnArgs.addAtEnd(args));
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
					result.getList().add(fn.apply(fnArgs));			
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
					"    (fn [[k v]] (println (pr-str k v)))" +
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
					"(reduce (fn [x y] (+ x y)) 10 [1 2 3 4 5 6 7])");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("reduce", args, 2, 3);
			
			final boolean twoArguments = args.size() < 3;
			final VncFunction reduceFn = Coerce.toVncFunction(args.nth(0));

			if (twoArguments) {
				final List<VncVal> coll = Coerce.toVncList(args.nth(1)).getList();
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
				final List<VncVal> coll = Coerce.toVncList(args.nth(2)).getList();
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

	public static VncFunction range = new VncFunction("range") {
		{
			setArgLists("(range end)", "(range start end)", "(range start end step)");
			
			setDoc( "Returns a collection of numbers from start (inclusive) to end " + 
					"(exclusive), by step, where start defaults to 0 and step defaults to 1. " +
					"When start is equal to end, returns empty list.");
			
			setExamples(
					"(range 10)",
					"(range 10 20)",
					"(range 10 20 3)",
					"(range 10 15 0.5)",
					"(range 1.1M 2.2M 0.1M)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("range", args, 1, 2, 3);

			VncVal start = new VncLong(0);
			VncVal end = new VncLong(0);
			VncVal step = new VncLong(1);

			switch(args.size()) {
				case 1:
					end = args.nth(0);
					break;
				case 2:
					start = args.nth(0);
					end = args.nth(1);
					break;
				case 3:
					start = args.nth(0);
					end = args.nth(1);
					step = args.nth(2);
					break;
			}
						
			if (!Types.isVncNumber(start)) {
				throw new VncException(String.format(
						"range: start value must be a number. %s",
						ErrorMessage.buildErrLocation(args)));
			}
			if (!Types.isVncNumber(end)) {
				throw new VncException(String.format(
						"range: end value must be a number. %s",
						ErrorMessage.buildErrLocation(args)));	
			}
			if (!Types.isVncNumber(step)) {
				throw new VncException(String.format(
						"range: step value must be a number. %s",
						ErrorMessage.buildErrLocation(args)));	
			}

			final List<VncVal> values = new ArrayList<>();

			if (zero_Q.apply(new VncList(step)) == True) {
				throw new VncException(String.format(
						"range: a step value must not be 0. %s",
						ErrorMessage.buildErrLocation(args)));	
			}
			
			if (pos_Q.apply(new VncList(step)) == True) {
				if (lt.apply(new VncList(end, start)) == True) {
					throw new VncException(String.format(
							"range positive step: end must not be lower than start. %s",
							ErrorMessage.buildErrLocation(args)));	
				}
				
				VncVal val = start;
				while(lt.apply(new VncList(val, end)) == True) {
					values.add(val);
					val = add.apply(new VncList(val, step));
				}
			}
			else {
				if (gt.apply(new VncList(end, start)) == True) {
					throw new VncException(String.format(
							"range negative step: end must not be greater than start. %s",
							ErrorMessage.buildErrLocation(args)));	
				}
				
				VncVal val = start;
				while(gt.apply(new VncList(val, end)) == True) {
					values.add(val);
					val = add.apply(new VncList(val, step));
				}
			}
			
			return new VncList(values);
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
	// Atom functions
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction new_atom = new VncFunction("atom") {
		{
			setArgLists("(atom x)");
			
			setDoc("Creates an atom with the initial value x");
			
			setExamples("(do\n   (def counter (atom 0))\n   (deref counter))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("atom", args, 1);
			
			return new VncAtom(args.nth(0));
		}
	};

	public static VncFunction atom_Q = new VncFunction("atom?") {
		{
			setArgLists("(atom? x)");
			
			setDoc("Returns true if x is an atom, otherwise false");
			
			setExamples("(do\n   (def counter (atom 0))\n   (atom? counter))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("atom?", args, 1);
			
			return Types.isVncAtom(args.nth(0)) ? True : False;
		}
	};

	public static VncFunction deref = new VncFunction("deref") {
		{
			setArgLists("(deref atom)");
			
			setDoc("Dereferences an atom, returns its value");
			
			setExamples("(do\n   (def counter (atom 0))\n   (deref counter))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("deref", args, 1);
			
			final VncAtom atm = Coerce.toVncAtom(args.nth(0));
			return atm.deref();
		}
	};

	public static VncFunction reset_BANG = new VncFunction("reset!") {
		{
			setArgLists("(reset! atom newval)");
			
			setDoc( "Sets the value of atom to newval without regard for the " + 
					"current value. Returns newval.");
			
			setExamples("(do\n   (def counter (atom 0))\n   (reset! counter 99)\n   (deref counter))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("reset!", args, 2);
			
			final VncAtom atm = Coerce.toVncAtom(args.nth(0));
			return atm.reset(args.nth(1));
		}
	};

	public static VncFunction swap_BANG = new VncFunction("swap!") {
		{
			setArgLists("(swap! atom f & args)");
			
			setDoc( "Atomically swaps the value of atom to be: " + 
					"(apply f current-value-of-atom args). Note that f may be called " + 
					"multiple times, and thus should be free of side effects.  Returns " + 
					"the value that was swapped in.");
			
			setExamples("(do\n   (def counter (atom 0))\n   (swap! counter inc)\n   (deref counter))");
		}
		
		public VncVal apply(final VncList args) {
			assertMinArity("swap!", args, 2);
			
			final VncAtom atm = Coerce.toVncAtom(args.nth(0));		
			final VncFunction fn = Coerce.toVncFunction(args.nth(1));
			final VncList swapArgs = args.slice(2);
			
			return atm.swap(fn, swapArgs);
		}
	};

	public static VncFunction compare_and_set_BANG = new VncFunction("compare-and-set!") {
		{
			setArgLists("(compare-and-set! atom oldval newval)");
			
			setDoc( "Atomically sets the value of atom to newval if and only if the " + 
					"current value of the atom is identical to oldval. Returns true if " + 
					"set happened, else false");
			
			setExamples("(do\n   (def counter (atom 2))\n   (compare-and-set! counter 2 4)\n   (deref counter))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("compare-and-set!", args, 3);
			
			final VncAtom atm = Coerce.toVncAtom(args.nth(0));		
			
			return atm.compare_and_set(args.nth(1), args.nth(2));
		}
	};

	
	///////////////////////////////////////////////////////////////////////////
	// IO functions
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction io_file = new VncFunction("io/file") {
		{
			setArgLists("(io/file path) (io/file parent child)");
			
			setDoc( "Returns a java.io.File. path, parent, and child can be a string " +
					"or java.io.File");
			
			setExamples(
					"(io/file \"/temp/test.txt\")",
					"(io/file \"/temp\" \"test.txt\")",
					"(io/file (io/file \"/temp\") \"test.txt\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("io/file", args, 1, 2);
						
			if (args.size() == 1) {
				final VncVal path = args.nth(0);
				if (Types.isVncString(path)) {
					return new VncJavaObject(new File(((VncString)path).getValue()));					
				}
				else if (isJavaIoFile(path) ) {
					return path;
				}
				else {
					throw new VncException(String.format(
							"Function 'io/file' does not allow %s as path. %s",
							Types.getClassName(path),
							ErrorMessage.buildErrLocation(args)));
				}
			}
			else {
				final VncVal parent = args.nth(0);
				final VncVal child = args.nth(1);
				
				File parentFile;

				if (Types.isVncString(parent)) {
					parentFile = new File(((VncString)parent).getValue());					
				}
				else if (isJavaIoFile(parent) ) {
					parentFile = (File)((VncJavaObject)parent).getDelegate();
				}
				else {
					throw new VncException(String.format(
							"Function 'io/file' does not allow %s as parent. %s",
							Types.getClassName(parent),
							ErrorMessage.buildErrLocation(args)));
				}

				if (Types.isVncString(child)) {
					 return new VncJavaObject(new File(parentFile, ((VncString)child).getValue()));					
				}
				else {
					throw new VncException(String.format(
							"Function 'io/file' does not allow %s as child. %s",
							Types.getClassName(child),
							ErrorMessage.buildErrLocation(args)));
				}
			}		
		}
	};

	public static VncFunction io_file_Q = new VncFunction("io/file?") {
		{
			setArgLists("(io/file? x)");
			
			setDoc("Returns true if x is a java.io.File.");
			
			setExamples(
					"(io/file? (io/file \"/temp/test.txt\"))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("io/file?", args, 1);
						
			final VncVal path = args.nth(0);
			return isJavaIoFile(path) ? True : False;
		}
	};

	public static VncFunction io_exists_file_Q = new VncFunction("io/exists-file?") {
		{
			setArgLists("(io/exists-file? x)");
			
			setDoc("Returns true if the file x exists. x must be a java.io.File.");
			
			setExamples(
					"(io/exists-file? (io/file \"/temp/test.txt\"))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("io/exists-file?", args, 1);
									
			if (!isJavaIoFile(args.nth(0)) ) {
				throw new VncException(String.format(
						"Function 'io/exists-file?' does not allow %s as x. %s",
						Types.getClassName(args.nth(0)),
						ErrorMessage.buildErrLocation(args)));
			}

			final File file = (File)((VncJavaObject)args.nth(0)).getDelegate();
			return file.isFile() ? True : False;
		}
	};

	public static VncFunction io_exists_dir_Q = new VncFunction("io/exists-dir?") {
		{
			setArgLists("(io/exists-dir? x)");
			
			setDoc("Returns true if the file x exists and is a directory. x must be a java.io.File.");
			
			setExamples(
					"(io/exists-dir? (io/file \"/temp\"))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("io/exists-dir?", args, 1);
									
			if (!isJavaIoFile(args.nth(0)) ) {
				throw new VncException(String.format(
						"Function 'io/exists-dir?' does not allow %s as x. %s",
						Types.getClassName(args.nth(0)),
						ErrorMessage.buildErrLocation(args)));
			}

			final File file = (File)((VncJavaObject)args.nth(0)).getDelegate();
			return file.isDirectory() ? True : False;
		}
	};

	public static VncFunction io_delete_file = new VncFunction("io/delete-file") {
		{
			setArgLists("(io/delete-file x)");
			
			setDoc("Deletes a file. x must be a java.io.File.");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().checkBlackListedVeniceFunction("io/delete-file", args);

			assertArity("io/delete-file", args, 1);

			if (!isJavaIoFile(args.nth(0)) ) {
				throw new VncException(String.format(
						"Function 'io/delete-file' does not allow %s as x. %s",
						Types.getClassName(args.nth(0)),
						ErrorMessage.buildErrLocation(args)));
			}

			final File file = (File)((VncJavaObject)args.nth(0)).getDelegate();
			try {
				Files.deleteIfExists(file.toPath());	
			}
			catch(Exception ex) {
				throw new VncException(
						String.format("Failed to delete file %s", file.getPath()),
						ex);
			}
			
			return Nil;
		}
	};

	public static VncFunction io_delete_file_on_exit = new VncFunction("io/delete-file-on-exit") {
		{
			setArgLists("(io/delete-file-on-exit x)");
			
			setDoc("Deletes a file on JVM exit. x must be a string or java.io.File.");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().checkBlackListedVeniceFunction("io/delete-file-on-exit", args);

			assertArity("io/delete-file-on-exit", args, 1);

			File file;
			if (Types.isVncString(args.nth(0)) ) {
				file = new File(((VncString)args.nth(0)).getValue());
			}
			else if (isJavaIoFile(args.nth(0)) ) {
				file = (File)((VncJavaObject)args.nth(0)).getDelegate();
			}
			else {
				throw new VncException(String.format(
						"Function 'io/delete-file-on-exit' does not allow %s as x. %s",
						Types.getClassName(args.nth(0)),
						ErrorMessage.buildErrLocation(args)));
			}

			try {
				file.deleteOnExit();;	
			}
			catch(Exception ex) {
				throw new VncException(
						String.format("Failed to marke file %s to delete on exit", file.getPath()),
						ex);
			}
			
			return Nil;
		}
	};

	public static VncFunction io_list_files = new VncFunction("io/list-files") {
		{
			setArgLists("(io/list-files dir filterFn?)");
			
			setDoc( "Lists files in a directory. dir must be a java.io.File. filterFn " +
					"is an optional filter that filters the files found");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().checkBlackListedVeniceFunction("io/list-files", args);

			assertArity("io/list-files", args, 1, 2);

			if (!isJavaIoFile(args.nth(0)) ) {
				throw new VncException(String.format(
						"Function 'io/list-files' does not allow %s as x. %s",
						Types.getClassName(args.nth(0)),
						ErrorMessage.buildErrLocation(args)));
			}
			
			final File file = (File)((VncJavaObject)args.nth(0)).getDelegate();
			try {
				final VncFunction filterFn = (args.size() == 2) ? Coerce.toVncFunction(args.nth(1)) : null;

				final VncList files = new VncList();

				for(File f : file.listFiles()) {
					final VncVal result = (filterFn == null) 
											? True 
											: filterFn.apply(new VncList(new VncJavaObject(f)));						
					if (result == True) {
						files.addAtEnd(new VncJavaObject(f));
					}
				}
				
				return files;
			}
			catch(Exception ex) {
				throw new VncException(
						String.format(
								"Failed to list files %s. %s", 
								file.getPath(),
								ErrorMessage.buildErrLocation(args)), 
						ex);
			}
		}
	};

	public static VncFunction io_copy_file = new VncFunction("io/copy-file") {
		{
			setArgLists("(io/copy input output)");
			
			setDoc( "Copies input to output. Returns nil or throws IOException. " + 
					"Input and output must be a java.io.File.");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().checkBlackListedVeniceFunction("io/copy-file", args);

			assertArity("io/copy-file", args, 2);

			if (!isJavaIoFile(args.nth(0)) ) {
				throw new VncException(String.format(
						"Function 'io/delete-file' does not allow %s as input. %s",
						Types.getClassName(args.nth(0)),
						ErrorMessage.buildErrLocation(args)));
			}
			if (!isJavaIoFile(args.nth(1)) ) {
				throw new VncException(String.format(
						"Function 'io/delete-file' does not allow %s as output. %s",
						Types.getClassName(args.nth(1)),
						ErrorMessage.buildErrLocation(args)));
			}


			final File from = (File)((VncJavaObject)args.nth(0)).getDelegate();
			final File to = (File)((VncJavaObject)args.nth(1)).getDelegate();
			
			try {
				Files.copy(from.toPath(), to.toPath());
			}
			catch(Exception ex) {
				throw new VncException(
						String.format(
								"Failed to copy file %s to %s. %s", 
								from.getPath(), 
								to.getPath(),
								ErrorMessage.buildErrLocation(args)),
						ex);
			}
			
			return Nil;
		}
	};

	public static VncFunction io_temp_file = new VncFunction("io/temp-file") {
		{
			setArgLists("(io/temp-file prefix suffix)");
			
			setDoc("Creates an empty temp file with prefix and suffix");
			
			setExamples(
				"(do \n" +
				"   (let [file (io/temp-file \"test-\", \".txt\")] \n" +
				"        (spit file \"123456789\" :append true) \n" +
				"        (io/slurp-temp-file file :binary false :remove true)) \n" +
				")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("io/temp-file", args, 2);

			final String prefix = Coerce.toVncString(args.first()).getValue();
			final String suffix = Coerce.toVncString(args.second()).getValue();
			try {
				final String path = File.createTempFile(prefix, suffix).getPath();
				tempFiles.add(path);
				return new VncString(path);
			}
			catch (Exception ex) {
				throw new VncException(ex.getMessage(), ex);
			}
		}
	};

	public static VncFunction io_tmp_dir = new VncFunction("io/tmp-dir") {
		{
			setArgLists("(io/tmp-dir)");
			
			setDoc("Returns the tmp dir as a java.io.File.");
			
			setExamples("(io/tmp-dir )");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().checkBlackListedVeniceFunction("io/tmp-dir", args);

			assertArity("io/tmp-dir", args, 0);

			return new VncJavaObject(new File(System.getProperty("java.io.tmpdir")));
		}
	};

	public static VncFunction io_user_dir = new VncFunction("io/user-dir") {
		{
			setArgLists("(io/user-dir)");
			
			setDoc("Returns the user dir (current working dir) as a java.io.File.");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().checkBlackListedVeniceFunction("io/user-dir", args);

			assertArity("io/user-dir", args, 0);

			return new VncJavaObject(new File(System.getProperty("user.dir")));
		}
	};

	public static VncFunction io_slurp_temp_file = new VncFunction("io/slurp-temp-file") {
		{
			setArgLists("(io/slurp-temp-file file & options)");
			
			setDoc( "Slurps binary or string data from a previously created temp file. " +
					"Supports the option :binary to either slurp binary or string data. " +
					"For string data an optional encoding can be specified. " +
					"Options: :encoding \"UTF-8\" :binary true/false. ");
			
			setExamples(
				"(do \n" +
				"   (let [file (io/temp-file \"test-\", \".txt\")] \n" +
				"        (spit file \"123456789\" :append true) \n" +
				"        (io/slurp-temp-file file :binary false :remove true)) \n" +
				")");
		}
		
		public VncVal apply(final VncList args) {
			assertMinArity("io/slurp-temp-file", args, 1);

			try {	
				File file;
				
				if (Types.isVncString(args.nth(0)) ) {
					file = new File(((VncString)args.nth(0)).getValue());
				}
				else if (isJavaIoFile(args.nth(0)) ) {
					file = (File)(Coerce.toVncJavaObject(args.nth(0)).getDelegate());
				}
				else {
					throw new VncException(String.format(
							"Function 'io/slurp-temp-file' does not allow %s as f. %s",
							Types.getClassName(args.nth(0)),
							ErrorMessage.buildErrLocation(args)));
				}

				
				if (!tempFiles.contains(file.getPath())) {
					throw new VncException(String.format(
							"Function 'io/slurp-temp-file' tries to access the unknown temp file '%s'. %s",
							file.getPath(),
							ErrorMessage.buildErrLocation(args)));
				}
				
				final VncHashMap options = new VncHashMap(args.slice(1));

				final VncVal binary = options.get(new VncKeyword("binary")); 
	
				final VncVal remove = options.get(new VncKeyword("remove")); 

				if (binary == True) {
					final byte[] data = Files.readAllBytes(file.toPath());
					
					if (remove == True) {
						file.delete();
						tempFiles.remove(file.getPath());
					}
					
					return new VncByteBuffer(ByteBuffer.wrap(data));
				}
				else {
					final VncVal encVal = options.get(new VncKeyword("encoding")); 
					
					final String encoding = encVal == Nil ? "UTF-8" : Coerce.toVncString(encVal).getValue();
									
					final byte[] data = Files.readAllBytes(file.toPath());

					if (remove == True) {
						file.delete();
						tempFiles.remove(file.getPath());
					}

					return new VncString(new String(data, encoding));
				}
			} 
			catch (VncException ex) {
				throw ex;
			}
			catch (Exception ex) {
				throw new VncException(ex.getMessage(), ex);
			}
		}
	};

	public static VncFunction io_slurp_stream = new VncFunction("io/slurp-stream") {
		{
			setArgLists("(io/slurp-stream is & options)");
			
			setDoc( "Slurps binary or string data from an input stream. " +
					"Supports the option :binary to either slurp binary or string data. " +
					"For string data an optional encoding can be specified. " +
					"Options: :encoding \"UTF-8\" :binary true/false. ");
			
			setExamples(
				"(do \n" +
				"   (import :java.io.FileInputStream) \n" +
				"   (let [file (io/temp-file \"test-\", \".txt\")] \n" +
				"        (spit file \"123456789\" :append true) \n" +
				"        (try-with [is (. :FileInputStream :new file)] \n" +
				"           (io/slurp-stream is :binary false))) \n" +
				")");
		}
		
		public VncVal apply(final VncList args) {
			assertMinArity("io/slurp-stream", args, 1);

			try {	
				final InputStream is = (InputStream)(Coerce.toVncJavaObject(args.nth(0)).getDelegate());
								
				final VncHashMap options = new VncHashMap(args.slice(1));

				final VncVal binary = options.get(new VncKeyword("binary")); 

				if (binary == True) {
					final byte[] data = StreamUtil.toByteArray(is);
					return data == null ? Nil : new VncByteBuffer(ByteBuffer.wrap(data));
				}
				else {
					final VncVal encVal = options.get(new VncKeyword("encoding")); 
					
					final String encoding = encVal == Nil ? "UTF-8" : Coerce.toVncString(encVal).getValue();

					return new VncString(StreamUtil.toString(is, encoding));
				}
			} 
			catch (VncException ex) {
				throw ex;
			}
			catch (Exception ex) {
				throw new VncException(ex.getMessage(), ex);
			}
		}
	};

	public static VncFunction io_spit_stream = new VncFunction("spit-stream") {
		{
			setArgLists("(io/spit-stream os content & options)");
			
			setDoc( "Writes content (string or bytebuf) to the output stream os. " +
					"If content is of type string an optional encoding (defaults to " +
					"UTF-8) is supported. The stream can optionally be flushed after " +
					"the operation. " +
					"Options: :flush true/false :encoding \"UTF-8\"");
			
			setExamples(
				"(do \n" +
				"   (import :java.io.FileOutputStream) \n" +
				"   (let [file (io/temp-file \"test-\", \".txt\")] \n" +
				"        (try-with [is (. :FileOutputStream :new file)] \n" +
				"           (io/spit-stream is \"123456789\" :flush true))) \n" +
				")");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().checkBlackListedVeniceFunction("spit-stream", args);


			assertMinArity("io/spit-stream", args, 2);

			try {
				final OutputStream os = (OutputStream)(Coerce.toVncJavaObject(args.nth(0)).getDelegate());
		
				final VncVal content = args.nth(1);

				final VncHashMap options = new VncHashMap(args.slice(2));

				final VncVal encVal = options.get(new VncKeyword("encoding")); 					
				final String encoding = encVal == Nil ? "UTF-8" : ((VncString)encVal).getValue();

				final VncVal flushVal = options.get(new VncKeyword("flush")); 
				final boolean flush = flushVal == True ? true : false;

				byte[] data;
				
				if (Types.isVncString(content)) {
					data = ((VncString)content).getValue().getBytes(encoding);
				}
				else if (Types.isVncByteBuffer(content)) {
					data = ((VncByteBuffer)content).getValue().array();
				}
				else {
					throw new VncException(String.format(
							"Function 'spit-stream' does not allow %s as content. %s",
							Types.getClassName(content),
							ErrorMessage.buildErrLocation(args)));
				}
				
				os.write(data);
				
				if (flush) {
					os.flush();
				}
				
				return Nil;
			} 
			catch (Exception ex) {
				throw new VncException(ex.getMessage(), ex);
			}
		}
	};

	
	
	///////////////////////////////////////////////////////////////////////////
	// String functions
	///////////////////////////////////////////////////////////////////////////
	
	public static VncFunction str_blank = new VncFunction("str/blank?") {
		{
			setArgLists("(str/blank? s)");
			
			setDoc("True if s is blank.");
			
			setExamples(
					"(str/blank? nil)", 
					"(str/blank? \"\")", 
					"(str/blank? \"  \")", 
					"(str/blank? \"abc\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/blank?", args, 1);

			if (args.nth(0) == Nil) {
				return True;
			}
			
			final String s = Coerce.toVncString(args.nth(0)).getValue();		

			return StringUtil.isBlank(s) ? True : False;
		}
	};
	
	public static VncFunction str_starts_with = new VncFunction("str/starts-with?") {
		{
			setArgLists("(str/starts-with? s substr)");
			
			setDoc("True if s starts with substr.");
			
			setExamples(
					"(str/starts-with? \"abc\"  \"ab\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/starts-with?", args, 2);

			if (args.nth(0) == Nil || args.nth(1) == Nil) {
				return False;
			}
			
			final VncString string = Coerce.toVncString(args.nth(0));		
			final VncString prefix = Coerce.toVncString(args.nth(1));		
			
			return string.getValue().startsWith(prefix.getValue()) ? True : False;
		}
	};
	
	public static VncFunction str_ends_with = new VncFunction("str/ends-with?") {
		{
			setArgLists("(str/ends-with? s substr)");
			
			setDoc("True if s ends with substr.");
			
			setExamples(
					"(str/starts-with? \"abc\"  \"bc\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/ends-with?", args, 2);

			if (args.nth(0) == Nil || args.nth(1) == Nil) {
				return False;
			}

			final VncString string = Coerce.toVncString(args.nth(0));		
			final VncString suffix = Coerce.toVncString(args.nth(1));		
			
			return string.getValue().endsWith(suffix.getValue()) ? True : False;
		}
	};
	
	public static VncFunction str_contains = new VncFunction("str/contains?") {
		{
			setArgLists("(str/contains? s substr)");
			
			setDoc("True if s contains with substr.");
			
			setExamples(
					"(str/contains? \"abc\"  \"ab\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/contains?", args, 2);

			if (args.nth(0) == Nil || args.nth(1) == Nil) {
				return False;
			}

			final VncString string = Coerce.toVncString(args.nth(0));		
			final VncString text = Coerce.toVncString(args.nth(1));		
			
			return string.getValue().contains(text.getValue()) ? True : False;
		}
	};
	
	public static VncFunction str_trim = new VncFunction("str/trim") {
		{
			setArgLists("(str/trim s substr)");
			
			setDoc("Trims leading and trailing spaces from s.");
			
			setExamples("(str/trim \" abc  \")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/trim", args, 1);

			if (args.nth(0) == Nil) {
				return Nil;
			}

			return new VncString(Coerce.toVncString(args.nth(0)).getValue().trim());
		}
	};
	
	public static VncFunction str_trim_to_nil = new VncFunction("str/trim-to-nil") {
		{
			setArgLists("(str/trim-to-nil s substr)");
			
			setDoc( "Trims leading and trailing spaces from s. " +
					"Returns nil if the rewsulting string is empry");
			
			setExamples(
					"(str/trim \"\")",
					"(str/trim \"    \")",
					"(str/trim nil)",
					"(str/trim \" abc   \")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/trim-to-nil", args, 1);

			if (args.nth(0) == Nil) {
				return Nil;
			}

			final String str = Coerce.toVncString(args.nth(0)).getValue().trim();
			return str.isEmpty() ? Nil : new VncString(str);
		}
	};
	
	public static VncFunction str_index_of = new VncFunction("str/index-of") {
		{
			setArgLists("(str/index-of s value)", "(str/index-of s value from-index)");
			
			setDoc( "Return index of value (string or char) in s, optionally searching " + 
					"forward from from-index. Return nil if value not found.");
			
			setExamples(
					"(str/index-of \"abcdefabc\" \"ab\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/index-of", args, 2, 3);

			final String text = Coerce.toVncString(args.nth(0)).getValue();	
			final String searchString = Coerce.toVncString(args.nth(1)).getValue();		
			
			if (args.size() == 3) {
				final int startPos = Coerce.toVncLong(args.nth(2)).getValue().intValue();
				final int pos = text.indexOf(searchString, startPos);
				return pos < 0 ? Nil : new VncLong(pos);
			}
			else {
				final int pos = text.indexOf(searchString);
				return pos < 0 ? Nil : new VncLong(pos);
			}
		}
	};
	
	public static VncFunction str_last_index_of = new VncFunction("str/last-index-of") {
		{
			setArgLists("(str/last-index-of s value)", "(str/last-index-of s value from-index)");
			
			setDoc( "Return last index of value (string or char) in s, optionally\n" + 
					"searching backward from from-index. Return nil if value not found.");
			
			setExamples(
					"(str/last-index-of \"abcdefabc\" \"ab\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/last-index-of", args, 2, 3);

			if (args.nth(0) == Nil) {
				return Nil;
			}

			final String text = Coerce.toVncString(args.nth(0)).getValue();	
			final String searchString = Coerce.toVncString(args.nth(1)).getValue();		
			
			if (args.size() > 2) {
				final int startPos = Coerce.toVncLong(args.nth(2)).getValue().intValue();
				final int pos = text.lastIndexOf(searchString, startPos);
				return pos < 0 ? Nil : new VncLong(pos);
			}
			else {
				final int pos = text.lastIndexOf(searchString);
				return pos < 0 ? Nil : new VncLong(pos);
			}
		}
	};
	
	public static VncFunction str_replace_first = new VncFunction("str/replace-first") {
		{
			setArgLists("(str/replace-first s search replacement)");
			
			setDoc("Replaces the first occurrance of search in s");
			
			setExamples(
					"(str/replace-first \"abcdefabc\" \"ab\" \"XYZ\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/replace-first", args, 3);

			if (args.nth(0) == Nil) {
				return Nil;
			}

			final String text = Coerce.toVncString(args.nth(0)).getValue();	
			final String searchString = Coerce.toVncString(args.nth(1)).getValue();		
			final String replacement = Coerce.toVncString(args.nth(2)).getValue();		

			if (StringUtil.isEmpty(text) || StringUtil.isEmpty(searchString) || replacement == null) {
				return args.nth(0);
			}

			int pos = text.indexOf(searchString);
			return pos >= 0
				? new VncString(
						text.substring(0, pos) + 
						replacement + 
						text.substring(pos + replacement.length()))
			 	: args.nth(0);
		}
	};
	
	public static VncFunction str_replace_last = new VncFunction("str/replace-last") {
		{
			setArgLists("(str/replace-last s search replacement)");
			
			setDoc("Replaces the last occurrance of search in s");
			
			setExamples(
					"(str/replace-last \"abcdefabc\" \"ab\" \"XYZ\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/replace-last", args, 3);

			if (args.nth(0) == Nil) {
				return Nil;
			}

			final String text = Coerce.toVncString(args.nth(0)).getValue();	
			final String searchString = Coerce.toVncString(args.nth(1)).getValue();		
			final String replacement = Coerce.toVncString(args.nth(2)).getValue();		

			if (StringUtil.isEmpty(text) || StringUtil.isEmpty(searchString) || replacement == null) {
				return args.nth(0);
			}

			int pos = text.lastIndexOf(searchString);
			return pos >= 0
				? new VncString(
						text.substring(0, pos) + 
						replacement + 
						text.substring(pos + replacement.length()))
			 	: args.nth(0);
		}
	};
	
	public static VncFunction str_replace_all = new VncFunction("str/replace-all") {
		{
			setArgLists("(str/replace-all s search replacement)");
			
			setDoc("Replaces the all occurrances of search in s");
			
			setExamples(
					"(str/replace-all \"abcdefabc\" \"ab\" \"XYZ\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/replace-all", args, 3);

			if (args.nth(0) == Nil) {
				return Nil;
			}

			final String text = Coerce.toVncString(args.nth(0)).getValue();	
			final String searchString = Coerce.toVncString(args.nth(1)).getValue();		
			final String replacement = Coerce.toVncString(args.nth(2)).getValue();		
			
			if (StringUtil.isEmpty(text) || StringUtil.isEmpty(searchString) || replacement == null) {
				return args.nth(0);
			}

			String searchText = text;
			int start = 0;
			int end = searchText.indexOf(searchString, start);
			if (end == -1) {
				return args.nth(0);
			}
			final int replLength = searchString.length();
			final StringBuilder buf = new StringBuilder();
			while (end != -1) {
				buf.append(text, start, end).append(replacement);
				start = end + replLength;
				end = searchText.indexOf(searchString, start);
			}
			buf.append(text, start, text.length());
			return new VncString(buf.toString());
		}
	};
	
	public static VncFunction str_lower_case = new VncFunction("str/lower-case") {
		{
			setArgLists("(str/lower-case s)");
			
			setDoc("Converts s to lowercase");
			
			setExamples(
					"(str/lower-case \"aBcDeF\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/lower-case", args, 1);

			if (args.nth(0) == Nil) {
				return Nil;
			}

			final VncString string = Coerce.toVncString(args.nth(0));		
			
			return new VncString(string.getValue().toLowerCase());
		}
	};
	
	public static VncFunction str_upper_case = new VncFunction("str/upper-case") {
		{
			setArgLists("(str/upper-case s)");
			
			setDoc("Converts s to uppercase");
			
			setExamples(
					"(str/upper-case \"aBcDeF\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/upper-case", args, 1);

			if (args.nth(0) == Nil) {
				return Nil;
			}

			final VncString string = Coerce.toVncString(args.nth(0));		
			
			return new VncString(string.getValue().toUpperCase());
		}
	};
	
	public static VncFunction str_join = new VncFunction("str/join") {
		{
			setArgLists("(str/join coll)", "(str/join separator coll)");
			
			setDoc("Joins all elements in coll separated by an optional separator.");
			
			setExamples(
					"(str/join [1 2 3])",
					"(str/join \"-\" [1 2 3])");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/join", args, 1, 2);

			final VncList coll = Coerce.toVncList(args.last());		
			final VncString delim = args.size() == 2 ? Coerce.toVncString(args.nth(0)) : new VncString("");
			
			return new VncString(
						coll.size() > 0
							? coll
								.getList()
								.stream()
								.map(v -> Types.isVncString(v) ? ((VncString)v).getValue() : v.toString())
								.collect(Collectors.joining(delim.getValue()))
							: "");
		}
	};
	
	public static VncFunction str_subs = new VncFunction("str/subs") {
		{
			setArgLists("(str/subs s start)", "(str/subs s start end)");
			
			setDoc( "Returns the substring of s beginning at start inclusive, and ending " + 
					"at end (defaults to length of string), exclusive.");
			
			setExamples(
					"(str/subs \"abcdef\" 2)",
					"(str/subs \"abcdef\" 2 5)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/subs", args, 2, 3);

			final VncString string = Coerce.toVncString(args.nth(0));		
			final VncLong from = Coerce.toVncLong(args.nth(1));
			final VncLong to = args.size() > 2 ? (VncLong)args.nth(2) : null;
			
			return new VncString(
							to == null
								? string.getValue().substring(from.getValue().intValue())
								: string.getValue().substring(from.getValue().intValue(), to.getValue().intValue()));
		}
	};
	
	public static VncFunction str_split = new VncFunction("str/split") {
		{
			setArgLists("(str/split s regex)");
			
			setDoc("Splits string on a regular expression.");
			
			setExamples("(str/split \"abc , def , ghi\" \"[ *],[ *]\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/split", args, 2);

			final VncString string = Coerce.toVncString(args.nth(0));		
			final VncString regex = Coerce.toVncString(args.nth(1));
			
			return new VncList(
					Arrays
						.asList(string.getValue().split(regex.getValue()))
						.stream()
						.map(s -> new VncString(s))
						.collect(Collectors.toList()));			
		}
	};
	
	public static VncFunction str_split_lines = new VncFunction("str/split-lines") {
		{
			setArgLists("(str/split-lines s)");
			
			setDoc("Splits s into lines.");
			
			setExamples("(str/split-lines \"line1\nline2\nline3\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/split-lines", args, 1);

			return args.nth(0) == Nil
					? new VncList()
					: new VncList(
							StringUtil
								.splitIntoLines(Coerce.toVncString(args.nth(0)).getValue())
								.stream()
								.map(s -> new VncString(s))
								.collect(Collectors.toList()));
		}
	};
	
	public static VncFunction str_format = new VncFunction("str/format") {
		{
			setArgLists("(str/format format args*)");
			
			setDoc("Returns a formatted string using the specified format string and arguments.");
			
			setExamples("(str/format \"%s: %d\" \"abc\" 100)");
		}
		
		public VncVal apply(final VncList args) {
			final VncString fmt = (VncString)args.nth(0);		
			final List<Object> fmtArgs = args
										.slice(1)
										.getList()
										.stream()
										.map(v -> JavaInteropUtil.convertToJavaObject(v))
										.collect(Collectors.toList());
			
			return new VncString(String.format(fmt.getValue(), fmtArgs.toArray()));		
		}
	};
	
	public static VncFunction str_quote = new VncFunction("str/quote") {
		{
			setArgLists("(str/quote str q)", "(str/quote str start end)");
			
			setDoc("Quotes a string.");
			
			setExamples(
					"(str/quote \"abc\" \"-\")",
					"(str/quote \"abc\" \"<\" \">\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/quote", args, 2, 3);

			final String s = Coerce.toVncString(args.nth(0)).getValue();
			final String start = Coerce.toVncString(args.nth(1)).getValue();
			final String end = (args.size() == 2) 
									? start 
									: Coerce.toVncString(args.nth(2)).getValue();

			return new VncString(start + s + end);
		}
	};
	
	public static VncFunction str_truncate = new VncFunction("str/truncate") {
		{
			setArgLists("(str/truncate s maxlen marker)");
			
			setDoc( "Truncates a string to the max lenght maxlen and adds the " +
					"marker to the end if the string needs to be truncated");
			
			setExamples(
					"(str/truncate \"abcdefghij\" 20 \"...\")",
					"(str/truncate \"abcdefghij\" 9 \"...\")",
					"(str/truncate \"abcdefghij\" 4 \"...\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/truncate", args, 3);
			
			if (args.nth(0) == Nil) {
				return Nil;
			}
			
			return new VncString(
						StringUtil.truncate(
							Coerce.toVncString(args.nth(0)).getValue(), 
							Coerce.toVncLong(args.nth(1)).getValue().intValue(), 					
							Coerce.toVncString(args.nth(2)).getValue()));		
		}
	};

	public static VncFunction str_strip_start = new VncFunction("str/strip-start") {
		{
			setArgLists("(str/strip-start s substr)");
			
			setDoc("Removes a substr only if it is at the beginning of a s, otherwise returns s.");
			
			setExamples(
					"(str/strip-start \"abcdef\" \"abc\")",
					"(str/strip-start \"abcdef\" \"def\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/strip-start", args, 2);
			
			if (args.nth(0) == Nil) {
				return Nil;
			}
			
			final String s = Coerce.toVncString(args.nth(0)).getValue();
			final String substr = Coerce.toVncString(args.nth(1)).getValue();
			
			return new VncString(s.startsWith(substr) ? s.substring(substr.length()) : s);		
		}
	};

	public static VncFunction str_strip_end = new VncFunction("str/strip-end") {
		{
			setArgLists("(str/strip-end s substr)");
			
			setDoc("Removes a substr only if it is at the end of a s, otherwise returns s.");
			
			setExamples(
					"(str/strip-end \"abcdef\" \"def\")",
					"(str/strip-end \"abcdef\" \"abc\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/strip-end", args, 2);
			
			if (args.nth(0) == Nil) {
				return Nil;
			}
			
			final String s = Coerce.toVncString(args.nth(0)).getValue();
			final String substr = Coerce.toVncString(args.nth(1)).getValue();
			
			return new VncString(s.endsWith(substr) ? s.substring(0, s.length() - substr.length()) : s);		
		}
	};

	public static VncFunction str_strip_indent = new VncFunction("str/strip-indent") {
		{
			setArgLists("(str/strip-indent s)");
			
			setDoc("Strip the indent of a multi-line string. The first line's leading whitespaces define the indent.");
			
			setExamples(
					"(str/strip-indent \"  line1\n    line2\n    line3\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/strip-indent", args, 1);
			
			if (args.nth(0) == Nil) {
				return Nil;
			}
			
			return new VncString(StringUtil.stripIndent(Coerce.toVncString(args.first()).getValue()));
		}
	};

	public static VncFunction str_strip_margin = new VncFunction("str/strip-margin") {
		{
			setArgLists("(str/strip-margin s)");
			
			setDoc("Strips leading whitespaces upto and including the margin '|' " +
					"from each line in a multi-line string.");
			
			setExamples(
					"(str/strip-margin \"line1\n  |  line2\n  |  line3\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/strip-margin", args, 1);
			
			if (args.nth(0) == Nil) {
				return Nil;
			}
			
			return new VncString(StringUtil.stripMargin(Coerce.toVncString(args.first()).getValue(), '|'));
		}
	};

	public static VncFunction str_repeat = new VncFunction("str/repeat") {
		{
			setArgLists("(str/repeat s n)", "(str/repeat s n sep)");
			
			setDoc("Repeats s n times with an optional separator.");
			
			setExamples(
					"(str/repeat \"abc\" 0)",
					"(str/repeat \"abc\" 3)",
					"(str/repeat \"abc\" 3 \"-\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/repeat", args, 2, 3);
			
			if (args.nth(0) == Nil) {
				return Nil;
			}
			
			final String s = Coerce.toVncString(args.nth(0)).getValue();
			final int times = Coerce.toVncLong(args.nth(1)).getValue().intValue();
			final String sep = args.size() == 3 ? Coerce.toVncString(args.nth(2)).getValue() : "";
			
			final StringBuilder sb = new StringBuilder();
			for(int ii=0; ii<times; ii++) {
				if (ii>0)sb.append(sep);
				sb.append(s);
			}			
			return new VncString(sb.toString());		
		}
	};


	///////////////////////////////////////////////////////////////////////////
	// Utilities
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction version = new VncFunction("version") {
		{
			setArgLists("(version)");
			
			setDoc("Returns the version.");
			
			setExamples("(version )");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("version", args, 0);
			
			return new VncString(Version.VERSION);
		}
	};

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

	public static VncFunction uuid = new VncFunction("uuid") {
		{
			setArgLists("(uuid)");
			
			setDoc("Generates a UUID.");
			
			setExamples("(uuid )");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("uuid", args, 0);
			return new VncString(UUID.randomUUID().toString());
		}
	};

	public static VncFunction type = new VncFunction("type") {
		{
			setArgLists("(type x)");
			
			setDoc("Retruns the type of x.");
			
			setExamples(
					"(type 5)",
					"(type (. :java.time.ZonedDateTime :now))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("type", args, 1);
			return Types.getClassName(args.first());
		}
	};

	public static VncFunction sleep = new VncFunction("sleep") {
		{
			setArgLists("(sleep n)");
			
			setDoc("Sleep for n milliseconds.");
			
			setExamples("(sleep 30)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("sleep", args, 1);
			
			try { 
				Thread.sleep(Coerce.toVncLong(args.first()).getValue());
			} catch(Exception ex) {
			}
			
			return Nil;
		}
	};
	
	public static Set<String> getAllIoFunctions() {
		return new HashSet<>(Arrays.asList(
								"slurp",
								"spit",
								"load-file",
								"io/exists-file?",
								"io/exists-dir?",
								"io/list-files",
								"io/delete-file",
								"io/copy-file",
								"io/tmp-dir",
								"io/user-dir"));
	}
	
	
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
	
	public static void assertArity(
			final String fnName, 
			final VncList args, 
			final int...expectedArities
	) {
		final int arity = args.size();
		for (int a : expectedArities) {
			if (a == arity) return;
		}		
		throw new ArityException(args, arity, fnName);
	}
	
	private static void assertMinArity(
			final String fnName, 
			final VncList args, 
			final int minArity
	) {
		final int arity = args.size();
		if (arity < minArity) {		
			throw new ArityException(args, arity, fnName);
		}
	}

	private static boolean isJavaIoFile(final VncVal val) {
		return (Types.isVncJavaObject(val) && ((VncJavaObject)val).getDelegate() instanceof File);
	}

	private static VncList removeNilValues(final VncList list) {		
		return new VncList(removeNilValues(list.getList()));
	}

	private static List<VncVal> removeNilValues(final List<VncVal> items) {		
		return items.stream()
				    .filter(v -> v != Nil)
				    .collect(Collectors.toList());
	}

	
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap.Builder()
				.put("doc",					doc)
				
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
				.put("slurp",				slurp)
				.put("spit",				spit)
				
				.put("==",					equal_Q)
				.put("!=",					not_equal_Q)			
				.put("<",					lt)
				.put("<=",					lte)
				.put(">",					gt)
				.put(">=",					gte)

				.put("match",				match_Q)
				.put("match-not",			match_not_Q)
				
				.put("dec/scale",			decimalScale)
				.put("dec/add",				decimalAdd)
				.put("dec/sub",				decimalSubtract)
				.put("dec/mul",				decimalMultiply)
				.put("dec/div",				decimalDivide)
				.put("+",					add)
				.put("-",					subtract)
				.put("*",					multiply)
				.put("/",					divide)
				.put("mod",					modulo)
				.put("inc",					inc)
				.put("dec",					dec)
				.put("abs",					abs)
				.put("min",					min)
				.put("max",					max)
				.put("boolean",				boolean_cast)
				.put("long",				long_cast)
				.put("double",				double_cast)
				.put("decimal",				decimal_cast)
				.put("bytebuf",				bytebuf_cast)
				.put("bytebuf-to-string",	bytebuf_to_string)
				.put("bytebuf-from-string",	bytebuf_from_string)			
				.put("zero?",				zero_Q)
				.put("pos?",				pos_Q)
				.put("neg?",				neg_Q)
				.put("even?",				even_Q)
				.put("odd?",				odd_Q)
				.put("time-ms",				time_ms)
				.put("time-ns",				time_ns)
				.put("rand-long",			rand_long)
				.put("rand-double",			rand_double)
		
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
				.put("subbytebuf", 			subbytebuf)
				.put("empty", 				empty)

				.put("set?",				set_Q)
				.put("set",					new_set)
				.put("difference", 			difference)
				.put("union", 				union)
				.put("intersection", 		intersection)

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
				.put("nfirst",				nfirst)
				.put("nlast",				nlast)
				.put("empty-to-nil",		emptyToNil)
				.put("pop",					pop)
				.put("peek",				peek)
				.put("empty?",				empty_Q)
				.put("not-empty?",			not_empty_Q)
				.put("every?",				every_Q)
				.put("any?",				any_Q)
				.put("count",				count)
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
				.put("range",				range)
				.put("repeat",				repeat)
		
				.put("meta",				meta)
				.put("with-meta",			with_meta)
				.put("vary-meta",			vary_meta)
				
				.put("atom",				new_atom)
				.put("atom?",				atom_Q)
				.put("deref",	 			deref)
				.put("reset!",				reset_BANG)
				.put("swap!",				swap_BANG)
				.put("compare-and-set!", 	compare_and_set_BANG)
				
				.put("coalesce", 			coalesce)
				
				.put("gensym",				gensym)
				.put("uuid",				uuid)
				.put("sleep",				sleep)
				.put("version",				version)
				.put("type",				type)
				
				.put("io/file",				io_file)
				.put("io/file?",			io_file_Q)
				.put("io/exists-file?",		io_exists_file_Q)
				.put("io/exists-dir?",		io_exists_dir_Q)
				.put("io/list-files",		io_list_files)
				.put("io/delete-file",		io_delete_file)
				.put("io/delete-file-on-exit", io_delete_file_on_exit)
				
				.put("io/copy-file",		io_copy_file)
				.put("io/temp-file",		io_temp_file)
				.put("io/tmp-dir",			io_tmp_dir)
				.put("io/user-dir",			io_user_dir)
				.put("io/slurp-temp-file",	io_slurp_temp_file)
				.put("io/slurp-stream",	    io_slurp_stream)
				.put("io/spit-stream",	    io_spit_stream)
				
				.put("str/blank?",			str_blank)
				.put("str/starts-with?",	str_starts_with)
				.put("str/ends-with?",		str_ends_with)
				.put("str/contains?",		str_contains)
				.put("str/trim",			str_trim)
				.put("str/trim-to-nil",		str_trim_to_nil)
				.put("str/index-of",		str_index_of)
				.put("str/last-index-of",	str_last_index_of)
				.put("str/replace-first",	str_replace_first)
				.put("str/replace-last",	str_replace_last)
				.put("str/replace-all",		str_replace_all)
				.put("str/lower-case",		str_lower_case)
				.put("str/upper-case",		str_upper_case)
				.put("str/join",			str_join)
				.put("str/subs",			str_subs)
				.put("str/split",			str_split)
				.put("str/split-lines",		str_split_lines)
				.put("str/format",			str_format)
				.put("str/quote",			str_quote)
				.put("str/truncate",		str_truncate)
				.put("str/strip-start",		str_strip_start)
				.put("str/strip-end",		str_strip_end)
				.put("str/strip-indent",	str_strip_indent)
				.put("str/strip-margin",	str_strip_margin)
				.put("str/repeat",		    str_repeat)

				.put("class",				className)	
				.put("load-core-module",	loadCoreModule)
				
				.toMap();

	
	private static final AtomicLong gensymValue = new AtomicLong(0);
	private static final Random random = new Random();
	private static final HashSet<String> tempFiles = new HashSet<>();
}
