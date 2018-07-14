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
package org.venice.impl;

import static org.venice.impl.types.Constants.False;
import static org.venice.impl.types.Constants.Nil;
import static org.venice.impl.types.Constants.True;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.venice.ArityException;
import org.venice.ContinueException;
import org.venice.EofException;
import org.venice.ValueException;
import org.venice.VncException;
import org.venice.impl.javainterop.JavaInterop;
import org.venice.impl.javainterop.JavaInteropUtil;
import org.venice.impl.types.Constants;
import org.venice.impl.types.Types;
import org.venice.impl.types.VncAtom;
import org.venice.impl.types.VncBigDecimal;
import org.venice.impl.types.VncByteBuffer;
import org.venice.impl.types.VncDouble;
import org.venice.impl.types.VncFunction;
import org.venice.impl.types.VncLong;
import org.venice.impl.types.VncString;
import org.venice.impl.types.VncSymbol;
import org.venice.impl.types.VncVal;
import org.venice.impl.types.collections.VncCollection;
import org.venice.impl.types.collections.VncHashMap;
import org.venice.impl.types.collections.VncJavaList;
import org.venice.impl.types.collections.VncJavaMap;
import org.venice.impl.types.collections.VncJavaObject;
import org.venice.impl.types.collections.VncJavaSet;
import org.venice.impl.types.collections.VncList;
import org.venice.impl.types.collections.VncMap;
import org.venice.impl.types.collections.VncOrderedMap;
import org.venice.impl.types.collections.VncSet;
import org.venice.impl.types.collections.VncSortedMap;
import org.venice.impl.types.collections.VncVector;
import org.venice.impl.util.StringUtil;


public class CoreFunctions {

	///////////////////////////////////////////////////////////////////////////
	// Documentation
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction doc = new VncFunction("doc") {
		{
			setArgLists("(doc name)");
			
			setDescription(
				"Returns the documentation for the function/macro with the given name");
		}
		public VncVal apply(final VncList args) {
			assertArity("doc", args, 1);
			
			return new VncString(Doc.getDoc(((VncString)args.first()).getValue()));
		}
	};



	///////////////////////////////////////////////////////////////////////////
	// Errors/Exceptions
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction throw_ex = new VncFunction("throw") {
		{
			setArgLists("(throw)", "(throw x)");
			
			setDescription("Throws exception with passed value x");
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
			
			setDescription(
					"Returns true if x is nil, false otherwise");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("nil?", args, 1);
			
			return args.nth(0) == Nil ? True : False;
		}
	};

	public static VncFunction some_Q = new VncFunction("some?") {
		{
			setArgLists("(some? x)");
			
			setDescription(
					"Returns true if x is not nil, false otherwise");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("nil?", args, 1);
			
			return args.nth(0) == Nil ? False : True;
		}
	};

	public static VncFunction true_Q = new VncFunction("true?") {
		{
			setArgLists("(true? x)");
			
			setDescription(
					"Returns true if x is true, false otherwise");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("true?", args, 1);
			
			return args.nth(0) == True ? True : False;
		}
	};

	public static VncFunction false_Q = new VncFunction("false?") {
		{
			setArgLists("(false? x)");
			
			setDescription(
					"Returns true if x is false, false otherwise");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("false?", args, 1);
			
			return args.nth(0) == False ? True : False;
		}
	};
	
	public static VncFunction boolean_Q = new VncFunction("boolean?") {
		{
			setArgLists("(boolean? n)");
			
			setDescription(
					"Returns true if n is a boolean");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("boolean?", args, 1);
			
			return args.nth(0) == True || args.nth(0) == False ? True : False;
		}
	};
	
	public static VncFunction long_Q = new VncFunction("long?") {
		{
			setArgLists("(long? n)");
			
			setDescription(
					"Returns true if n is a long");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("long?", args, 1);

			return Types.isVncLong(args.nth(0)) ? True : False;
		}
	};
	
	public static VncFunction double_Q = new VncFunction("double?") {
		{
			setArgLists("(double? n)");
			
			setDescription(
					"Returns true if n is a double");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("double?", args, 1);
			
			return Types.isVncDouble(args.nth(0)) ? True : False;
		}
	};
	
	public static VncFunction decimal_Q = new VncFunction("decimal?") {
		{
			setArgLists("(decimal? n)");
			
			setDescription(
					"Returns true if n is a decimal");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("decimal?", args, 1);
			
			return Types.isVncDecimal(args.nth(0)) ? True : False;
		}
	};
	
	public static VncFunction number_Q = new VncFunction("number?") {
		{
			setArgLists("(number? n)");
			
			setDescription(
					"Returns true if n is a number (long, double, or decimal)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("number?", args, 1);
			
			return Types.isVncLong(args.nth(0)) 
					|| Types.isVncDouble(args.nth(0))
					|| Types.isVncDecimal(args.nth(0))? True : False;
		}
	};

	
	public static VncFunction bytebuf_Q = new VncFunction("bytebuf?") {
		{
			setArgLists("(bytebuf? x)");
			
			setDescription(
					"Returns true if x is a bytebuf");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("bytebuf?", args, 1);
			
			return Types.isVncByteBuffer(args.nth(0)) ? True : False;
		}
	};

	public static VncFunction string_Q = new VncFunction("string?") {
		{
			setArgLists("(string? x)");
			
			setDescription(
					"Returns true if x is a string");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("string?", args, 1);
			
			if (!(args.nth(0) instanceof VncString)) { return False; }
			return ((VncString)args.nth(0)).isKeyword() ? False : True;
		}
	};

	public static VncFunction symbol = new VncFunction("symbol") {
		{
			setArgLists("(symbol name)");
			
			setDescription("Returns a symbol from the given name");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("symbol", args, 1);
			
			return new VncSymbol((VncString)args.nth(0));
		}
	};
	
	public static VncFunction symbol_Q = new VncFunction("symbol?") {
		{
			setArgLists("(symbol? x)");
			
			setDescription(
					"Returns true if x is a symbol");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("symbol?", args, 1);
			
			return Types.isVncSymbol(args.nth(0)) ? True : False;
		}
	};

	public static VncFunction keyword = new VncFunction("keyword") {
		{
			setArgLists("(keyword name)");
			
			setDescription("Returns a keyword from the given name");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("keyword", args, 1);
			
			if (Types.isVncString(args.nth(0)) && ((VncString)args.nth(0)).isKeyword()) {
				return args.nth(0);
			} 
			else {
				return VncString.keyword(((VncString)args.nth(0)).getValue());
			}
		}
	};
	
	public static VncFunction keyword_Q = new VncFunction("keyword?") {
		{
			setArgLists("(keyword? x)");
			
			setDescription(
					"Returns true if x is a keyword");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("keyword?", args, 1);
			
			if (!Types.isVncString(args.nth(0))) { return False; }
			final String s = ((VncString)args.nth(0)).getValue();
			return Types.isVncKeyword(s) ? True : False;
		}
	};
	
	public static VncFunction fn_Q = new VncFunction("fn?") {
		{
			setArgLists("(fn? x)");
			
			setDescription("Returns true if x is a function");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("fn?", args, 1);
			
			if (!Types.isVncFunction(args.nth(0))) { return False; }
			return ((VncFunction)args.nth(0)).isMacro() ? False : True;
		}
	};
	
	public static VncFunction macro_Q = new VncFunction("macro?") {
		{
			setArgLists("(macro? x)");
			
			setDescription("Returns true if x is a macro");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("macro?", args, 1);
			
			if (!Types.isVncFunction(args.nth(0))) { return False; }
			return ((VncFunction)args.nth(0)).isMacro() ? True : False;
		}
	};


	///////////////////////////////////////////////////////////////////////////
	// String functions
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction pr_str = new VncFunction("pr-str") {
		{
			setArgLists("(pr_str & xs)");
			
			setDescription(
					"With no args, returns the empty string. With one arg x, returns " + 
					"x.toString(). With more than one arg, returns the concatenation " +
					"of the str values of the args with delimiter ' '.");
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
			
			setDescription(
					"With no args, returns the empty string. With one arg x, returns " + 
					"x.toString(). (str nil) returns the empty string. With more than " + 
					"one arg, returns the concatenation of the str values of the args.");
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

	public static VncFunction prn = new VncFunction("prn") {
		{
			setArgLists("(prn & xs)");
			
			setDescription(
					"Prints to stdout, with no args, prints the empty string. With one arg x, " + 
					"prints x.toString(). With more than one arg, prints the concatenation " +
					"of the str values of the args with delimiter ' '." +
					"The function is sandboxed.");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().checkBlackListedVeniceFunction("prn", args);
			
			System.out.print(
					args.isEmpty()
						? new VncString("")
						: new VncString(
								args.getList()
									.stream()
									.map(v -> Printer._pr_str(v, true))
									.collect(Collectors.joining(" "))));
			
			return Nil;
		}
	};

	public static VncFunction println = new VncFunction("println") {
		{
			setArgLists("(println & xs)");
			
			setDescription(
					"Prints to stdout with a tailing linefeed, with no args, prints the " + 
					"empty string. With one arg x, prints x.toString(). With more than " +
					"one arg, prints the concatenation of the str values of the args with " +
					"delimiter ' '." +
					"The function is sandboxed.");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().checkBlackListedVeniceFunction("println", args);
			
			System.out.println(
					args.isEmpty()
						? new VncString("")
						: new VncString(
								args.getList()
									.stream()
									.map(v -> Printer._pr_str(v, true))
									.collect(Collectors.joining(" "))));
			
			return Nil;
		}
	};

	public static VncFunction readline = new VncFunction("readline") {
		{
			setArgLists("(readline prompt)");
			
			setDescription("Reads the next line from stdin. The function is sandboxed");
		}
	
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().checkBlackListedVeniceFunction("readline", args);
			
			final String prompt = ((VncString)args.nth(0)).getValue();
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
			
			setDescription("Reads from x");
		}
		
		public VncVal apply(final VncList args) {
			try {
				assertArity("read-string ", args, 1);

				return Reader.read_str(((VncString)args.nth(0)).getValue(), null);
			} 
			catch (ContinueException c) {
				return Nil;
			}
		}
	};

	public static VncFunction slurp = new VncFunction("slurp") {
		{
			setArgLists("(slurp file & options)");
			
			setDescription(
					"Returns the file's content as text. The encoding defaults to UTF-8. " +
					"Options: :encoding \"UTF-8\"");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().checkBlackListedVeniceFunction("slurp", args);
			
			try {	
				File file;
				
				if (Types.isVncString(args.nth(0)) ) {
					file = new File(((VncString)args.nth(0)).getValue());
				}
				else if (isJavaIoFile(args.nth(0)) ) {
					file = (File)((VncJavaObject)args.nth(0)).getDelegate();
				}
				else {
					throw new VncException(String.format(
							"Function 'spit' does not allow %s as f",
							Types.getClassName(args.nth(0))));
				}

				
				final VncHashMap options = new VncHashMap(args.slice(1));
				
				final VncVal encVal = options.get(VncString.keyword("encoding")); 
					
				final String encoding = encVal == Nil ? "UTF-8" : ((VncString)encVal).getValue();
								
				final byte[] data = Files.readAllBytes(file.toPath());
				
				return new VncString(new String(data, encoding));
			} 
			catch (Exception ex) {
				throw new VncException(ex.getMessage(), ex);
			}
		}
	};

	public static VncFunction spit = new VncFunction("spit") {
		{
			setArgLists("(spit f content & options)");
			
			setDescription(
					"Opens f, writes content, then closes f. " +
					"Options: :append true/false, :encoding \"UTF-8\"");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().checkBlackListedVeniceFunction("spit", args);
			
			try {
				// Currently just string content is supported!
				
				File file;
				
				if (Types.isVncString(args.nth(0)) ) {
					file = new File(((VncString)args.nth(0)).getValue());
				}
				else if (isJavaIoFile(args.nth(0)) ) {
					file = (File)((VncJavaObject)args.nth(0)).getDelegate();
				}
				else {
					throw new VncException(String.format(
							"Function 'spit' does not allow %s as f",
							Types.getClassName(args.nth(0))));
				}

		
				final VncVal content = args.nth(1);

				final VncHashMap options = new VncHashMap(args.slice(2));

				final VncVal append = options.get(VncString.keyword("append")); 
				
				final VncVal encVal = options.get(VncString.keyword("encoding")); 
					
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
							"Function 'spit' does not allow %s as content",
							Types.getClassName(content)));
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

	
	///////////////////////////////////////////////////////////////////////////
	// Number functions
	///////////////////////////////////////////////////////////////////////////
	
	public static VncFunction decimalScale = new VncFunction("dec/scale") {
		{
			setArgLists("(dec/scale x scale rounding-mode)");
			
			setDescription(
					"Scales a decimal. rounding-mode is one of (:CEILING, :DOWN, " +
					":FLOOR, :HALF_DOWN, :HALF_EVEN, :HALF_UP, :UNNECESSARY, :UP)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("dec/scale", args, 3);

			final VncVal arg = args.nth(0);
			final VncLong scale = (VncLong)args.nth(1);
			final RoundingMode roundingMode = VncBigDecimal.toRoundingMode((VncString)args.nth(2));
						
			if (Types.isVncDecimal(arg)) {
				final BigDecimal val = ((VncBigDecimal)arg).getValue();
				return new VncBigDecimal(val.setScale(scale.getValue().intValue(), roundingMode));
			}
			else {
				throw new VncException(String.format(
										"Function 'dec/scale' does not allow %s as operand 1",
										Types.getClassName(arg)));
			}
		}
	};
	
	public static VncFunction decimalAdd = new VncFunction("dec/add") {
		{
			setArgLists("(dec/add x y scale rounding-mode)");
			
			setDescription(
					"Adds two decimals and scales the result. rounding-mode is one of (:CEILING, :DOWN, " +
					":FLOOR, :HALF_DOWN, :HALF_EVEN, :HALF_UP, :UNNECESSARY, :UP)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("dec/add", args, 4);

			final VncBigDecimal op1 = (VncBigDecimal)args.nth(0);
			final VncBigDecimal op2 = (VncBigDecimal)args.nth(1);
			final VncLong scale = (VncLong)args.nth(2);
			final RoundingMode roundingMode = VncBigDecimal.toRoundingMode((VncString)args.nth(3));
				
			return new VncBigDecimal(op1.getValue().add(op2.getValue()).setScale(scale.getValue().intValue(), roundingMode));
		}
	};
	
	public static VncFunction decimalSubtract = new VncFunction("dec/sub") {
		{
			setArgLists("(dec/sub x y scale rounding-mode)");
			
			setDescription(
					"Subtract y from x and scales the result. rounding-mode is one of (:CEILING, :DOWN, " +
					":FLOOR, :HALF_DOWN, :HALF_EVEN, :HALF_UP, :UNNECESSARY, :UP)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("dec/sub", args, 4);

			final VncBigDecimal op1 = (VncBigDecimal)args.nth(0);
			final VncBigDecimal op2 = (VncBigDecimal)args.nth(1);
			final VncLong scale = (VncLong)args.nth(2);
			final RoundingMode roundingMode = VncBigDecimal.toRoundingMode((VncString)args.nth(3));
				
			return new VncBigDecimal(op1.getValue().subtract(op2.getValue()).setScale(scale.getValue().intValue(), roundingMode));
		}
	};
	
	public static VncFunction decimalMultiply = new VncFunction("dec/mul") {
		{
			setArgLists("(dec/mul x y scale rounding-mode)");
			
			setDescription(
					"Multiplies two decimals and scales the result. rounding-mode is one of (:CEILING, :DOWN, " +
					":FLOOR, :HALF_DOWN, :HALF_EVEN, :HALF_UP, :UNNECESSARY, :UP)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("dec/mul", args, 4);

			final VncBigDecimal op1 = (VncBigDecimal)args.nth(0);
			final VncBigDecimal op2 = (VncBigDecimal)args.nth(1);
			final VncLong scale = (VncLong)args.nth(2);
			final RoundingMode roundingMode = VncBigDecimal.toRoundingMode((VncString)args.nth(3));
				
			return new VncBigDecimal(op1.getValue().multiply(op2.getValue()).setScale(scale.getValue().intValue(), roundingMode));
		}
	};
	
	public static VncFunction decimalDivide = new VncFunction("dec/div") {
		{
			setArgLists("(dec/div x y scale rounding-mode)");
			
			setDescription(
					"Divides x by y and scales the result. rounding-mode is one of (:CEILING, :DOWN, " +
					":FLOOR, :HALF_DOWN, :HALF_EVEN, :HALF_UP, :UNNECESSARY, :UP)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("dec/div", args, 4);

			final VncBigDecimal op1 = (VncBigDecimal)args.nth(0);
			final VncBigDecimal op2 = (VncBigDecimal)args.nth(1);
			final VncLong scale = (VncLong)args.nth(2);
			final RoundingMode roundingMode = VncBigDecimal.toRoundingMode((VncString)args.nth(3));
				
			return new VncBigDecimal(op1.getValue().divide(op2.getValue(), scale.getValue().intValue(), roundingMode));
		}
	};
	
	public static VncFunction add = new VncFunction("+") {
		{
			setArgLists("(+)", "(+ x)", "(+ x y)", "(+ x y & more)");
			
			setDescription("Returns the sum of the numbers. (+) returns 0.");
			
			setExamples("(+)", "(+ 1)", "(+ 1 2)", "(+ 1 2 3 4)");
		}
		
		public VncVal apply(final VncList args) {
			if (args.isEmpty()) {
				return new VncLong(0);
			}
			else if (args.size() == 1) {
				return args.nth(0);
			}

			final VncVal first = args.nth(0);
			final VncList rest = args.slice(1);
			if (Types.isVncLong(first)) {
				VncLong val = (VncLong)first;
				for(VncVal v : rest.getList()) { val = val.add(v); }
				return val;
			}
			else if (Types.isVncDouble(first)) {
				VncDouble val = (VncDouble)first;
				for(VncVal v : rest.getList()) { val = val.add(v); }
				return val;
			}
			else if (Types.isVncDecimal(first)) {
				VncBigDecimal val = (VncBigDecimal)first;
				for(VncVal v : rest.getList()) { val = val.add(v); }
				return val;
			}
			else {
				throw new VncException(String.format(
						"Invalid argument type %s while calling function '+'",
						Types.getClassName(first)));
			}
		}
	};
	
	public static VncFunction subtract = new VncFunction("-") {
		{
			setArgLists("(- x)", "(- x y)", "(- x y & more)");
			
			setDescription(
					"If one number is supplied, returns the negation, else subtracts " +
					"the numbers from x and returns the result.");
		}
		
		public VncVal apply(final VncList args) {
			if (args.isEmpty()) {
				throw new ArityException(0, "-");
			}
			else if (args.size() == 1) {
				final VncVal first = args.nth(0);
				if (Types.isVncLong(first)) {
					return  ((VncLong)first).multiply(new VncLong(-1L));
				}
				else if (Types.isVncDouble(first)) {
					return ((VncDouble)first).multiply(new VncDouble(-1D));
				}
				else if (Types.isVncDecimal(first)) {
					return ((VncBigDecimal)first).multiply(new VncBigDecimal(new BigDecimal("-1.0")));
				}
				else {
					return first;
				}
			}

			final VncVal first = args.nth(0);
			final VncList rest = args.slice(1);
			if (Types.isVncLong(first)) {
				VncLong val = (VncLong)first;
				for(VncVal v : rest.getList()) { val = val.subtract(v); }
				return val;
			}
			else if (Types.isVncDouble(first)) {
				VncDouble val = (VncDouble)first;
				for(VncVal v : rest.getList()) { val = val.subtract(v); }
				return val;
			}
			else if (Types.isVncDecimal(first)) {
				VncBigDecimal val = (VncBigDecimal)first;
				for(VncVal v : rest.getList()) { val = val.subtract(v); }
				return val;
			}
			else {
				throw new VncException(String.format(
						"Invalid argument type %s while calling function '-'",
						Types.getClassName(first)));
			}
		}
	};
	
	public static VncFunction multiply = new VncFunction("*") {
		{
			setArgLists("(*)", "(* x)", "(* x y)", "(* x y & more)");
			
			setDescription("Returns the product of numbers. (*) returns 1");
		}
		
		public VncVal apply(final VncList args) {
			if (args.isEmpty()) {
				return new VncLong(1);
			}
			else if (args.size() == 1) {
				return args.nth(0);
			}

			final VncVal first = args.nth(0);
			final VncList rest = args.slice(1);
			if (Types.isVncLong(first)) {
				VncLong val = (VncLong)first;
				for(VncVal v : rest.getList()) { val = val.multiply(v); }
				return val;
			}
			else if (Types.isVncDouble(first)) {
				VncDouble val = (VncDouble)first;
				for(VncVal v : rest.getList()) { val = val.multiply(v); }
				return val;
			}
			else if (Types.isVncDecimal(first)) {
				VncBigDecimal val = (VncBigDecimal)first;
				for(VncVal v : rest.getList()) { val = val.multiply(v); }
				return val;
			}
			else {
				throw new VncException(String.format(
						"Invalid argument type %s while calling function '*'",
						Types.getClassName(first)));
			}
		}
	};
	
	public static VncFunction divide = new VncFunction("/") {
		{
			setArgLists("(/ x)", "(/ x y)", "(/ x y & more)");
			
			setDescription(
					"If no denominators are supplied, returns 1/numerator, " + 
					"else returns numerator divided by all of the denominators.");
		}
		
		public VncVal apply(final VncList args) {
			if (args.isEmpty()) {
				throw new ArityException(0, "/");
			}
			else if (args.size() == 1) {
				final VncVal first = args.nth(0);
				if (Types.isVncLong(first)) {
					return new VncLong(1L).divide((VncLong)first);
				}
				else if (Types.isVncDouble(first)) {
					return new VncDouble(1D).divide((VncDouble)first);
				}
				else if (Types.isVncDecimal(first)) {
					return new VncBigDecimal(BigDecimal.ONE).divide((VncBigDecimal)first);
				}
				else {
					return first;
				}
			}

			final VncVal first = args.nth(0);
			final VncList rest = args.slice(1);
			if (Types.isVncLong(first)) {
				VncLong val = (VncLong)first;
				for(VncVal v : rest.getList()) { val = val.divide(v); }
				return val;
			}
			else if (Types.isVncDouble(first)) {
				VncDouble val = (VncDouble)first;
				for(VncVal v : rest.getList()) { val = val.divide(v); }
				return val;
			}
			else if (Types.isVncDecimal(first)) {
				VncBigDecimal val = (VncBigDecimal)first;
				for(VncVal v : rest.getList()) { val = val.divide(v); }
				return val;
			}
			else {
				throw new VncException(String.format(
						"Invalid argument type %s while calling function '/'",
						Types.getClassName(first)));
			}
		}
	};
	
	public static VncFunction modulo = new VncFunction("mod") {
		{
			setArgLists("(mod n d)");
			
			setDescription("Modulus of n and d.");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("abs", args, 2);

			if (!Types.isVncLong(args.nth(0))) {
				throw new VncException(String.format(
						"Function 'mod' does not allow %s as numerator", 
						Types.getClassName(args.nth(0))));
			}
			if (!Types.isVncLong(args.nth(1))) {
				throw new VncException(String.format(
						"Function 'mod' does not allow %s as denominator", 
						Types.getClassName(args.nth(1))));
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
			
			setDescription("Increments the number x");
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
			else if (Types.isVncDecimal(arg)) {
				return ((VncBigDecimal)arg).inc();
			}
			else {
				throw new VncException(String.format(
						"Invalid argument type %s while calling function 'inc'",
						Types.getClassName(arg)));
			}
		}
	};
	
	public static VncFunction dec = new VncFunction("dec") {
		{
			setArgLists("(dec x)");
			
			setDescription("Decrements the number x");
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
			else if (Types.isVncDecimal(arg)) {
				return ((VncBigDecimal)arg).dec();
			}
			else {
				throw new VncException(String.format(
						"Invalid argument type %s while calling function 'dec'",
						Types.getClassName(arg)));
			}
		}
	};
	
	public static VncFunction max = new VncFunction("max") {
		{
			setArgLists("(max x)", "(max x y)", "(max x y & more)");
			
			setDescription(
					"Returns the greatest of the values");
		}
		
		public VncVal apply(final VncList args) {
			if (args.isEmpty()) {
				throw new ArityException(0, "max");
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
				else if (Types.isVncDecimal(max)) {
					max = ((VncBigDecimal)max).gte(op) == True ? max : op;
				}
				else {
					throw new VncException(String.format(
											"Function 'max' does not allow %s as operand 1", 
											Types.getClassName(max)));
				}
			}
			
			return max;			
		}
	};
	
	public static VncFunction min = new VncFunction("min") {
		{
			setArgLists("(min x)", "(min x y)", "(min x y & more)");
			
			setDescription(
					"Returns the smallest of the values");
		}
		
		public VncVal apply(final VncList args) {
			if (args.isEmpty()) {
				throw new ArityException(0, "min");
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
				else if (Types.isVncDecimal(min)) {
					min = ((VncBigDecimal)min).lte(op) == True ? min : op;
				}
				else {
					throw new VncException(String.format(
											"Function 'min' does not allow %s as operand 1", 
											Types.getClassName(min)));
				}
			}
			
			return min;			
		}
	};
	
	public static VncFunction abs = new VncFunction("abs") {
		{
			setArgLists("(abs x)");
			
			setDescription(
					"Returns the absolute value of the number");
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
			else if (Types.isVncDecimal(arg)) {
				return new VncBigDecimal(((VncBigDecimal)arg).getValue().abs());
			}
			else {
				throw new VncException(String.format(
						"Invalid argument type %s while calling function 'abs'",
						Types.getClassName(arg)));
			}
		}
	};
	
	public static VncFunction rand_long = new VncFunction("rand-long") {
		{
			setArgLists("(rand-long)", "(rand-long max)");
			
			setDescription(
					"Without argument returns a random long between 0 and MAX_LONG. " +
					"Without argument max returns a random long between 0 and max exclusive.");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("rand-long", args, 0, 1);
			
			if (args.isEmpty()) {
				return new VncLong(Math.abs(random.nextLong()));
			}
			else {
				final long max = ((VncLong)args.first()).getValue();
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
			
			setDescription(
					"Without argument returns a double long between 0.0 and 1.0. " +
					"Without argument max returns a random long between 0.0 and max.");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("rand-double", args, 0, 1);
			
			if (args.isEmpty()) {
				return new VncDouble(random.nextDouble());
			}
			else {
				final double max = ((VncDouble)args.first()).getValue();
				if (max < 0.0) {
					throw new VncException("Function 'rand-double' does not allow negative max values");

				}
				return new VncDouble(random.nextDouble() * max);
			}
		}
	};

	public static VncFunction equal_Q = new VncFunction("==") {
		{
			setArgLists("(== x y)");
			
			setDescription(
					"Returns true of both operands have the equivalent type");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("==", args, 2);
			
			return Types._equal_Q(args.nth(0), args.nth(1)) ? True : False;
		}
	};

	public static VncFunction not_equal_Q = new VncFunction("!=") {
		{
			setArgLists("(!= x y)");
			
			setDescription(
					"Returns true of both operands do not have the equivalent type");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("!=", args, 2);
			
			return Types._equal_Q(args.nth(0), args.nth(1)) ? False : True;
		}
	};

	public static VncFunction match_Q = new VncFunction("match") {
		{
			setArgLists("(match s regex)");
			
			setDescription("Returns true if the string s matches the regular expression regex");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("match", args, 2);
			
			if (!Types.isVncString(args.nth(0))) {
				throw new VncException(String.format(
						"Invalid first argument type %s while calling function 'match'",
						Types.getClassName(args.nth(0))));
			}
			if (!Types.isVncString(args.nth(1))) {
				throw new VncException(String.format(
						"Invalid second argument type %s while calling function 'match'",
						Types.getClassName(args.nth(1))));
			}

			return Types._match_Q(args.nth(0), args.nth(1)) ? True : False;
		}
	};

	public static VncFunction match_not_Q = new VncFunction("match-not") {
		{
			setArgLists("(match-not s regex)");
			
			setDescription("Returns true if the string s does not match the regular expression regex");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("match-not", args, 2);
			
			if (!Types.isVncString(args.nth(0))) {
				throw new VncException(String.format(
						"Invalid first argument type %s while calling function 'match-not'",
						Types.getClassName(args.nth(0))));
			}
			if (!Types.isVncString(args.nth(1))) {
				throw new VncException(String.format(
						"Invalid second argument type %s while calling function 'match-not'",
						Types.getClassName(args.nth(1))));
			}
			
			return Types._match_Q(args.nth(0), args.nth(1)) ? False : True;
		}
	};

	public static VncFunction lt = new VncFunction("<") {
		{
			setArgLists("(< x y)");
			
			setDescription(
					"Returns true if x is smaller than y");
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
			else if (Types.isVncDecimal(op1)) {
				return ((VncBigDecimal)op1).lt(op2);
			}
			else if (Types.isVncString(op1)) {
				if (!Types.isVncString(op2)) {
					throw new VncException(String.format(
							"Function '<' with operand 1 of type %s does not allow %s as operand 2", 
							Types.getClassName(op1),
							Types.getClassName(op2)));
				}

				final String s1 = ((VncString)op1).getValue();
				final String s2 = ((VncString)op2).getValue();
				return s1.compareTo(s2) < 0 ? True : False;
			}
			else {
				throw new VncException(String.format(
										"Function '<' does not allow %s as operand 1", 
										Types.getClassName(op1)));
			}
		}
	};
	
	public static VncFunction lte = new VncFunction("<=") {
		{
			setArgLists("(<= x y)");
			
			setDescription(
					"Returns true if x is smaller or equal to y");
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
			else if (Types.isVncDecimal(op1)) {
				return ((VncBigDecimal)op1).lte(op2);
			}
			else if (Types.isVncString(op1)) {
				if (!Types.isVncString(op2)) {
					throw new VncException(String.format(
							"Function '<=' with operand 1 of type %s does not allow %s as operand 2", 
							Types.getClassName(op1),
							Types.getClassName(op2)));
				}

				final String s1 = ((VncString)op1).getValue();
				final String s2 = ((VncString)op2).getValue();
				return s1.compareTo(s2) <= 0 ? True : False;
			}
			else {
				throw new VncException(String.format(
										"Function '<=' does not allow %s as operand 1", 
										Types.getClassName(op1)));
			}
		}
	};
	
	public static VncFunction gt = new VncFunction(">") {
		{
			setArgLists("(> x y)");
			
			setDescription(
					"Returns true if x is greater than y");
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
			else if (Types.isVncDecimal(op1)) {
				return ((VncBigDecimal)op1).gt(op2);
			}
			else if (Types.isVncString(op1)) {
				if (!Types.isVncString(op2)) {
					throw new VncException(String.format(
							"Function '>' with operand 1 of type %s does not allow %s as operand 2", 
							Types.getClassName(op1),
							Types.getClassName(op2)));
				}

				final String s1 = ((VncString)op1).getValue();
				final String s2 = ((VncString)op2).getValue();
				return s1.compareTo(s2) > 0 ? True : False;
			}
			else {
				throw new VncException(String.format(
										"Function '>' does not allow %s as operand 1", 
										Types.getClassName(op1)));
			}
		}
	};
	
	public static VncFunction gte = new VncFunction(">=") {
		{
			setArgLists("(>= x y)");
			
			setDescription(
					"Returns true if x is greater or equal to y");
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
			else if (Types.isVncDecimal(op1)) {
				return ((VncBigDecimal)op1).gte(op2);
			}
			else if (Types.isVncString(op1)) {
				if (!Types.isVncString(op2)) {
					throw new VncException(String.format(
							"Function '>=' with operand 1 of type %s does not allow %s as operand 2", 
							Types.getClassName(op1),
							Types.getClassName(op2)));
				}

				final String s1 = ((VncString)op1).getValue();
				final String s2 = ((VncString)op2).getValue();
				return s1.compareTo(s2) >= 0 ? True : False;
			}
			else {
				throw new VncException(String.format(
										"Function '>=' does not allow %s as operand 1", 
										Types.getClassName(op1)));
			}
		}
	};
	
	public static VncFunction zero_Q = new VncFunction("zero?") {
		{
			setArgLists("(zero? x)");
			
			setDescription(
					"Returns true if x zero else false");
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
			else if (Types.isVncDecimal(op1)) {
				return ((VncBigDecimal)op1).getValue().compareTo(BigDecimal.ZERO) == 0 ? True : False;
			}
			else {
				throw new VncException(String.format(
										"Function 'zero' does not allow %s as operand 1", 
										Types.getClassName(op1)));
			}
		}
	};
	
	public static VncFunction pos_Q = new VncFunction("pos?") {
		{
			setArgLists("(pos? x)");
			
			setDescription(
					"Returns true if x greater than zero else false");
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
			else if (Types.isVncDecimal(op1)) {
				return ((VncBigDecimal)op1).getValue().compareTo(BigDecimal.ZERO) > 0 ? True : False;
			}
			else {
				throw new VncException(String.format(
										"Function 'pos' does not allow %s as operand 1", 
										Types.getClassName(op1)));
			}
		}
	};
	
	public static VncFunction neg_Q = new VncFunction("neg?") {
		{
			setArgLists("(neg? x)");
			
			setDescription(
					"Returns true if x smaller than zero else false");
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
			else if (Types.isVncDecimal(op1)) {
				return ((VncBigDecimal)op1).getValue().compareTo(BigDecimal.ZERO) < 0 ? True : False;
			}
			else {
				throw new VncException(String.format(
										"Function 'plus' does not allow %s as operand 1", 
										Types.getClassName(op1)));
			}
		}
	};
	
	public static VncFunction even_Q = new VncFunction("even?") {
		{
			setArgLists("(even? n)");
			
			setDescription(
					"Returns true if n is even, throws an exception if n is not an integer");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("even?", args, 1);
			
			final VncVal op1 = args.nth(0);
			if (Types.isVncLong(op1)) {
				return ((VncLong)op1).getValue() % 2 == 0 ? True : False;
			}
			else {
				throw new VncException(String.format(
										"Function 'even' does not allow %s as operand", 
										Types.getClassName(op1)));
			}
		}
	};
	
	public static VncFunction odd_Q = new VncFunction("odd?") {
		{
			setArgLists("(odd? n)");
			
			setDescription(
					"Returns true if n is odd, throws an exception if n is not an integer");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("odd?", args, 1);
			
			final VncVal op1 = args.nth(0);
			if (Types.isVncLong(op1)) {
				return ((VncLong)op1).getValue() % 2 == 1 ? True : False;
			}
			else {
				throw new VncException(String.format(
										"Function 'odd' does not allow %s as operand", 
										Types.getClassName(op1)));
			}
		}
	};

	public static VncFunction time_ms = new VncFunction("time-ms") {
		{
			setArgLists("(time-ms)");
			
			setDescription("Returns the current time in milliseconds");
			
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
			
			setDescription(
					"Returns the current value of the running Java Virtual Machine's " +
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
			
			setDescription("Converts to boolean. Everything except 'false' and 'nil' is true in boolean context.");
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
			
			setDescription("Converts to long");
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
			else if (Types.isVncDecimal(op1)) {
				return new VncLong(((VncBigDecimal)op1).getValue().longValue());
			}
			else if (Types.isVncString(op1)) {
				final String s = ((VncString)op1).unkeyword().getValue();
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
										Types.getClassName(op1)));
			}
		}
	};
	
	public static VncFunction double_cast = new VncFunction("double") {
		{
			setArgLists("(double x)");
			
			setDescription("Converts to double");
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
			else if (Types.isVncDecimal(op1)) {
				return new VncDouble(((VncBigDecimal)op1).getValue().doubleValue());
			}
			else if (Types.isVncString(op1)) {
				final String s = ((VncString)op1).unkeyword().getValue();
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
							Types.getClassName(op1)));
			}
		}
	};
	
	public static VncFunction decimal_cast = new VncFunction("decimal") {
		{
			setArgLists("(decimal x) (decimal x scale rounding-mode)");
			
			setDescription(
					"Converts to decimal. rounding-mode is one of (:CEILING, :DOWN, " +
					":FLOOR, :HALF_DOWN, :HALF_EVEN, :HALF_UP, :UNNECESSARY, :UP)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("decimal", args, 1, 3);

			if (args.isEmpty()) {
				return new VncBigDecimal(BigDecimal.ZERO);
			}
			else {				
				final VncVal arg = args.nth(0);
				final VncLong scale = args.size() < 3 ? null : (VncLong)args.nth(1);
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
					final BigDecimal dec = new BigDecimal(((VncString)arg).unkeyword().getValue());
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
				else if (Types.isVncDecimal(arg)) {
					final BigDecimal dec = ((VncBigDecimal)arg).getValue();
					return new VncBigDecimal(args.size() < 3 ? dec : dec.setScale(scale.getValue().intValue(), roundingMode));
				}
				else {
					throw new VncException(String.format(
							"Function 'decimal' does not allow %s as operand 1", 
							Types.getClassName(arg)));
				}
			}
		}
	};

	
	public static VncFunction bytebuf_cast = new VncFunction("bytebuf") {
		{
			setArgLists("(bytebuf x)");
			
			setDescription(
					"Converts to bytebuf. x can be a bytebuf, a list/vector of longs, or a string");
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
			else if (Types.isVncByteBuffer(arg)) {
				return ((VncByteBuffer)arg).copy();
			}
			else if (Types.isVncList(arg)) {
				if (!((VncList)arg).getList().stream().allMatch(v -> Types.isVncLong(v))) {
					throw new VncException(
							"Function 'bytebuf' a list as argument must contains long values");
				}
				
				List<VncVal> list = ((VncList)arg).getList();
				
				final byte[] buf = new byte[list.size()];
				for(int ii=0; ii<list.size(); ii++) {
					buf[ii] = (byte)((VncLong)list.get(ii)).getValue().longValue();
				}
				
				return new VncByteBuffer(ByteBuffer.wrap(buf));
			}
			else {
				throw new VncException(String.format(
						"Function 'bytebuf' does not allow %s as argument", 
						Types.getClassName(arg)));
			}
		}
	};

	
	///////////////////////////////////////////////////////////////////////////
	// List functions
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction new_list = new VncFunction("list") {
		{
			setArgLists("(list & items)");
			
			setDescription("Creates a new list containing the items.");
		}
		
		public VncVal apply(final VncList args) {
			if (args.size() == 1 && Types.isVncJavaList(args.nth(0))) {
				return ((VncJavaList)args.nth(0)).toVncList();
			}
			else if (args.size() == 1 && Types.isVncJavaSet(args.nth(0))) {
				return ((VncJavaSet)args.nth(0)).toVncList();
			}
			else if (args.size() == 1 && Types.isVncSet(args.nth(0))) {
				return ((VncSet)args.nth(0)).toVncList();
			}
			else if (args.size() == 1 && Types.isVncString(args.nth(0))) {
				return ((VncString)args.nth(0)).toVncList();
			}
			else if (args.size() == 1 && Types.isVncByteBuffer(args.nth(0))) {
				return ((VncByteBuffer)args.nth(0)).toVncList();
			}
			else {
				return new VncList(args.getList());
			}
		}
	};

	static public boolean list_Q(VncVal mv) {
		return mv.getClass().equals(VncList.class);
	}
	
	public static VncFunction list_Q = new VncFunction("list?") {
		{
			setArgLists("(list? obj)");
			
			setDescription("Returns true if obj is a list");
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
			
			setDescription("Creates a new vector containing the items.");
		}
		
		public VncVal apply(final VncList args) {
			if (args.size() == 1 && Types.isVncJavaList(args.nth(0))) {
				return ((VncJavaList)args.nth(0)).toVncVector();
			}
			else if (args.size() == 1 && Types.isVncJavaSet(args.nth(0))) {
				return ((VncJavaSet)args.nth(0)).toVncVector();
			}
			else if (args.size() == 1 && Types.isVncSet(args.nth(0))) {
				return ((VncSet)args.nth(0)).toVncVector();
			}
			else {
				return new VncVector(args.getList());
			}
		}
	};

	static public boolean vector_Q(VncVal mv) {
		return mv.getClass().equals(VncVector.class);
	}
	
	public static VncFunction vector_Q = new VncFunction("vector?") {
		{
			setArgLists("(vector? obj)");
			
			setDescription("Returns true if obj is a vector");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("vector?", args, 1);
			
			return vector_Q(args.nth(0)) ? True : False;
		}
	};
	
	public static VncFunction subvec = new VncFunction("subvec") {
		{
			setArgLists("(subvec v start) (subvec v start end)");
			
			setDescription(
					"Returns a vector of the items in vector from start (inclusive) "+
					"to end (exclusive). If end is not supplied, defaults to " + 
					"(count vector)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("subvec", args, 2, 3);

			final VncVector vec = (VncVector)args.nth(0);		
			final VncLong from = (VncLong)args.nth(1);
			final VncLong to = args.size() > 2 ? (VncLong)args.nth(2) : null;
			
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
			
			setDescription(
					"Returns a byte buffer of the items in buffer from start (inclusive) "+
					"to end (exclusive). If end is not supplied, defaults to " + 
					"(count bytebuffer)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("subbytebuf", args, 2, 3);

			final byte[] buf = ((VncByteBuffer)args.nth(0)).getValue().array();		
			final VncLong from = (VncLong)args.nth(1);
			final VncLong to = args.size() > 2 ? (VncLong)args.nth(2) : null;
			
			
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
			
			setDescription("Creates a new set containing the items.");
		}
		
		public VncVal apply(final VncList args) {
			if (args.size() == 1 && Types.isVncJavaSet(args.nth(0))) {
				return ((VncJavaSet)args.nth(0)).toVncSet();
			}
			else if (args.size() == 1 && Types.isVncJavaList(args.nth(0))) {
				return ((VncJavaList)args.nth(0)).toVncSet();
			}
			else if (args.size() == 1 && Types.isVncList(args.nth(0))) {
				return ((VncList)args.nth(0)).toVncSet();
			}
			else {
				return new VncSet(args);
			}
		}
	};

	public static VncFunction set_Q = new VncFunction("set?") {
		{
			setArgLists("(set? obj)");
			
			setDescription("Returns true if obj is a set");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("set?", args, 1);
			
			return Types.isVncSet(args.nth(0)) ? True : False;
		}
	};

	
	///////////////////////////////////////////////////////////////////////////
	// HashMap functions
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction new_hash_map = new VncFunction("hash-map") {
		{
			setArgLists("(hash-map & keyvals)");
			
			setDescription("Creates a new hash map containing the items.");
		}
		
		public VncVal apply(final VncList args) {
			if (args.size() == 1 && Types.isVncJavaMap(args.nth(0))) {
				return ((VncJavaMap)args.nth(0)).toVncHashMap();
			}
			if (args.size() == 1 && Types.isVncJavaObject(args.nth(0))) {
				return ((VncJavaObject)args.nth(0)).toVncMap();
			}
			else {
				return new VncHashMap(args);
			}
		}
	};

	public static VncFunction new_ordered_map = new VncFunction("ordered-map") {
		{
			setArgLists("(ordered-map & keyvals)");
			
			setDescription("Creates a new ordered map containing the items.");
		}
		
		public VncVal apply(final VncList args) {
			if (args.size() == 1 && Types.isVncJavaMap(args.nth(0))) {
				return ((VncJavaMap)args.nth(0)).toVncOrderedMap();
			}
			else {
				return new VncOrderedMap(args);
			}
		}
	};

	public static VncFunction new_sorted_map = new VncFunction("sorted-map") {
		{
			setArgLists("(sorted-map & keyvals)");
			
			setDescription("Creates a new sorted map containing the items.");
		}
		
		public VncVal apply(final VncList args) {
			if (args.size() == 1 && Types.isVncJavaMap(args.nth(0))) {
				return ((VncJavaMap)args.nth(0)).toVncSortedMap();
			}
			else {
				return new VncSortedMap(args);
			}
		}
	};

	public static VncFunction map_Q = new VncFunction("map?") {
		{
			setArgLists("(map? obj)");
			
			setDescription("Returns true if obj is a map");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("map?", args, 1);
			
			return Types.isVncMap(args.nth(0)) ? True : False;
		}
	};

	public static VncFunction hash_map_Q = new VncFunction("hash-map?") {
		{
			setArgLists("(hash-map? obj)");
			
			setDescription("Returns true if obj is a hash map");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("hash-map?", args, 1);
			
			return Types.isVncHashMap(args.nth(0)) ? True : False;
		}
	};

	public static VncFunction ordered_map_Q = new VncFunction("ordered-map?") {
		{
			setArgLists("(ordered-map? obj)");
			
			setDescription("Returns true if obj is an ordered map");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("ordered-map?", args, 1);
			
			return Types.isVncOrderedMap(args.nth(0)) ? True : False;
		}
	};

	public static VncFunction sorted_map_Q = new VncFunction("sorted-map?") {
		{
			setArgLists("(sorted-map? obj)");
			
			setDescription(
					"Returns true if obj is a sorted map");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("sorted-map?", args, 1);
			
			return Types.isVncSortedMap(args.nth(0)) ? True : False;
		}
	};

	public static VncFunction contains_Q = new VncFunction("contains?") {
		{
			setArgLists("(contains? coll key)");
			
			setDescription(
					"Returns true if key is present in the given collection, otherwise " + 
					"returns false.");
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
						"Function 'contains?' does not allow %s as coll", 
						Types.getClassName(coll)));
			}
		}
	};

	public static VncFunction assoc = new VncFunction("assoc") {
		{
			setArgLists("(assoc coll key val)", "(assoc coll key val & kvs)");
			
			setDescription(
					"When applied to a map, returns a new map of the " + 
					"same type, that contains the mapping of key(s) to " + 
					"val(s). When applied to a vector, returns a new vector that " + 
					"contains val at index. Note - index must be <= (count vector).");
		}
		
		public VncVal apply(final VncList args) {
			if (Types.isVncMap(args.nth(0))) {
				final VncMap hm = (VncMap)args.nth(0);
				
				final VncMap new_hm = (VncMap)hm.copy();
				new_hm.assoc((VncList)args.slice(1));
				return new_hm;
			}
			else if (Types.isVncVector(args.nth(0))) {
				final VncVector vec = ((VncVector)args.nth(0)).copy();
				final VncList keyvals = (VncList)args.slice(1);
				for(int ii=0; ii<keyvals.size(); ii+=2) {
					final VncLong key = (VncLong)keyvals.nth(ii);
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
					final VncLong key = (VncLong)keyvals.nth(ii);
					final VncString val = (VncString)keyvals.nth(ii+1);
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
						"Function 'assoc' does not allow %s as coll", 
						Types.getClassName(args.nth(0))));
			}
		}
	};
	
	public static VncFunction dissoc = new VncFunction("dissoc") {
		{
			setArgLists("(dissoc coll key)", "(dissoc coll key & ks)");
			
			setDescription(
					"Returns a new coll of the same type, " + 
					"that does not contain a mapping for key(s)");
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
					final VncLong key = (VncLong)keyvals.nth(ii);
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
					final VncLong key = (VncLong)keyvals.nth(ii);
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
						"Function 'dissoc' does not allow %s as coll", 
						Types.getClassName(args.nth(0))));
			}
		}
	};

	public static VncFunction get = new VncFunction("get") {
		{
			setArgLists("(get map key)", "(get map key not-found)");
			
			setDescription("Returns the value mapped to key, not-found or nil if key not present.");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("get", args, 2, 3);
			
			if (args.nth(0) == Nil) {
				return Nil;
			} 
			else {
				final VncMap mhm = (VncMap)args.nth(0);
				final VncVal key = args.nth(1);
				final VncVal key_not_found = (args.size() == 3) ? args.nth(2) : Nil;
				
				final VncVal value = mhm.get(key);
				return value != Nil ? value : key_not_found;
			}
		}
	};

	public static VncFunction find = new VncFunction("find") {
		{
			setArgLists("(find map key)");
			
			setDescription("Returns the map entry for key, or nil if key not present.");
					
			setExamples("(find {:a 1 :b 2} :b)", "(find {:a 1 :b 2} :z)");

		}
		
		public VncVal apply(final VncList args) {
			assertArity("find", args, 2);
			
			if (args.nth(0) == Nil) {
				return Nil;
			} 
			else {
				final VncMap mhm = (VncMap)args.nth(0);
				final VncVal key = args.nth(1);
				
				final VncVal value = mhm.get(key);
				return value == Nil ? Nil : new VncVector(key, value);
			}
		}
	};

	public static VncFunction key = new VncFunction("key") {
		{
			setArgLists("(key e)");
			
			setDescription("Returns the key of the map entry.");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("key", args, 1);
			
			final VncList entry = (VncList)args.nth(0);
			return entry.first();
		}
	};

	public static VncFunction keys = new VncFunction("keys") {
		{
			setArgLists("(keys map)");
			
			setDescription("Returns a collection of the map's keys.");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("keys", args, 1);
			
			final VncMap mhm = (VncMap)args.nth(0);
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
			
			setDescription("Returns the val of the map entry.");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("val", args, 1);
			
			final VncList entry = (VncList)args.nth(0);
			return entry.second();
		}
	};

	public static VncFunction vals = new VncFunction("vals") {
		{
			setArgLists("(vals map)");
			
			setDescription("Returns a collection of the map's values.");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("vals", args, 1);
			
			final VncMap mhm = (VncMap)args.nth(0);
			final Map<VncVal,VncVal> hm = mhm.getMap();
			VncList val_lst = new VncList();
			for (VncVal val : hm.values()) {
				val_lst.addAtEnd(val);
			}
			return val_lst;
		}
	};

	
	///////////////////////////////////////////////////////////////////////////
	// Sequence functions
	///////////////////////////////////////////////////////////////////////////


	public static VncFunction into = new VncFunction("into") {
		{
			setArgLists("(into to-coll from-coll)");
			
			setDescription(
					"Returns a new coll consisting of to-coll with all of the items of" + 
					"from-coll conjoined.");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("into", args, 2);
			
			final VncVal to = (VncVal)args.nth(0);
			final VncVal from = (VncVal)args.nth(1);
			
			if (Types.isVncVector(to)) {
				if (Types.isVncSet(from)) {
					((VncSet)from).getList().forEach(v -> ((VncVector)to).addAtEnd(v));
				}
				else if (Types.isVncList(from)) {
					((VncList)from).getList().forEach(v -> ((VncVector)to).addAtEnd(v));
				}
				else if (Types.isVncMap(from)) {
					((VncMap)from).toVncList().getList().forEach(v -> ((VncVector)to).addAtEnd(v));
				}				
			}
			else if (Types.isVncList(to)) {
				if (Types.isVncSet(from)) {
					((VncSet)from).getList().forEach(v -> ((VncList)to).addAtStart(v));
				}
				else if (Types.isVncList(from)) {
					((VncList)from).getList().forEach(v -> ((VncList)to).addAtStart(v));
				}
				else if (Types.isVncMap(from)) {
					((VncMap)from).toVncList().getList().forEach(v -> ((VncVector)to).addAtStart(v));
				}				
			}
			else if (Types.isVncMap(to)) {
				if (Types.isVncList(from)) {
					((VncList)from).getList().forEach(it -> {
						if (Types.isVncList(it)) {
							((VncMap)to).assoc((VncList)it);
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
			
			return to;
		}
	};

	public static VncFunction seq_Q = new VncFunction("seq?") {
		{
			setArgLists("(seq? obj)");
			
			setDescription("Returns true if obj is a sequential collection");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("seq?", args, 1);
			
			return Types.isVncList(args.nth(0)) ? True : False;
		}
	};

	public static VncFunction coll_Q = new VncFunction("coll?") {
		{
			setArgLists("(coll? obj)");
			
			setDescription("Returns true if obj is a collection");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("seq?", args, 1);
			
			return Types.isVncCollection(args.nth(0)) ? True : False;
		}
	};

	public static VncFunction count = new VncFunction("count") {
		{
			setArgLists("(count coll)");
			
			setDescription(
					"Returns the number of items in the collection. (count nil) returns " + 
					"0. Also works on strings, and Java Collections");
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
						"Invalid argument type %s while calling function 'count'",
						Types.getClassName(arg)));
			}
		}
	};

	public static VncFunction empty_Q = new VncFunction("empty?") {
		{
			setArgLists("(empty? x)");
			
			setDescription("Returns true if x is empty");
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
			
			setDescription("Returns true if x is not empty");
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
			
			setDescription(
					"Returns a new collection where x is the first element and coll is\n" + 
					"the rest");
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
						"Invalid argument type %s while calling function 'cons'",
						Types.getClassName(args.nth(1))));
			}
		}
	};

	public static VncFunction concat = new VncFunction("concat") {
		{
			setArgLists("(concat coll)", "(concat coll & colls)");
			
			setDescription(
					"Returns a collection of the concatenation of the elements " +
					"in the supplied colls.");
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
							"Invalid argument type %s while calling function 'concat'",
							Types.getClassName(val)));
				}
			});
			
			return new VncList(result);
		}
	};

	public static VncFunction first = new VncFunction("first") {
		{
			setArgLists("(first coll)");
			
			setDescription("Returns the first element of coll.");
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
						"Invalid argument type %s while calling function 'first'",
						Types.getClassName(val)));
			}
		}
	};

	public static VncFunction second = new VncFunction("second") {
		{
			setArgLists("(second coll)");
			
			setDescription("Returns the second element of coll.");
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
						"Invalid argument type %s while calling function 'second'",
						Types.getClassName(val)));
			}
		}
	};

	public static VncFunction nth = new VncFunction("nth") {
		{
			setArgLists("(nth coll idx)");
			
			setDescription("Returns the nth element of coll.");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("nth", args, 2);

			final int idx = ((VncLong)args.nth(1)).getValue().intValue();

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
						"Invalid argument type %s while calling function 'nth'",
						Types.getClassName(val)));
			}
		}
	};

	public static VncFunction last = new VncFunction("last") {
		{
			setArgLists("(last coll)");
			
			setDescription("Returns the last element of coll.");
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
						"Invalid argument type %s while calling function 'last'",
						Types.getClassName(val)));
			}
		}
	};

	public static VncFunction rest = new VncFunction("rest") {
		{
			setArgLists("(rest coll)");
			
			setDescription("Returns a collection with second to list element");
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

	public static VncFunction coalesce = new VncFunction("coalesce") {
		{
			setArgLists("(coalesce args*)");
			
			setDescription("Returns the first non nil arg");
		}
		
		public VncVal apply(final VncList args) {
			return args.getList().stream().filter(v -> v != Nil).findFirst().orElse(Nil);
		}
	};

	public static VncFunction emptyToNil = new VncFunction("empty-to-nil") {
		{
			setArgLists("(empty-to-nil x)");
			
			setDescription("Returns nil if x is empty");
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
			
			setDescription("Returns the class of x");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("class", args, 1);
			
			return Types.getClassName(args.nth(0));
		}
	};

	public static VncFunction pop = new VncFunction("pop") {
		{
			setArgLists("(pop coll)");
			
			setDescription(
					"For a list, returns a new list without the first item, " + 
					"for a vector, returns a new vector without the last item.");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("pop", args, 1);
			
			final VncVal exp = args.nth(0);
			if (exp == Nil) {
				return new VncList();
			}
			final VncList ml = ((VncList)exp);

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
			
			setDescription("For a list, same as first, for a vector, same as last");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("peek", args, 1);
			
			final VncVal exp = args.nth(0);
			if (exp == Nil) {
				return Nil;
			}
			final VncList ml = ((VncList)exp);

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
			
			setDescription(
					"Returns a list of successive items from coll while " + 
					"(predicate item) returns logical true.");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("take-while", args, 2);
			
			final VncFunction predicate = (VncFunction)args.nth(0);
			final VncList coll = (VncList)args.nth(1);
			
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
			
			setDescription(
					"Returns a collection of the first n items in coll, or all items if " + 
					"there are fewer than n.");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("take", args, 2);
			
			final VncLong n = (VncLong)args.nth(0);
			final VncList coll = (VncList)args.nth(1);

			return coll.slice(0, (int)Math.min(n.getValue(), coll.size()));
		}
	};
	
	public static VncFunction drop_while = new VncFunction("drop-while") {
		{
			setArgLists("(drop-while predicate coll)");
			
			setDescription(
					"Returns a list of the items in coll starting from the " + 
					"first item for which (predicate item) returns logical false.");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("drop-while", args, 2);
			
			final VncFunction predicate = (VncFunction)args.nth(0);
			final VncList coll = (VncList)args.nth(1);
			
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
			
			setDescription(
					"Returns a collection of all but the first n items in coll");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("drop", args, 2);
			
			final VncLong n = (VncLong)args.nth(0);
			final VncList coll = (VncList)args.nth(1);

			return coll.slice((int)Math.min(n.getValue()+1, coll.size()));
		}
	};
	
	public static VncFunction flatten = new VncFunction("flatten") {
		{
			setArgLists("(flatten coll)");
			
			setDescription(
					"Takes any nested combination of collections (lists, vectors, " + 
					"etc.) and returns their contents as a single, flat sequence. " + 
					"(flatten nil) returns an empty list.");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("flatten", args, 1);
			
			final VncList coll = (VncList)args.nth(0);
			
			final List<VncVal> result = new ArrayList<>();
			flatten(coll, result);			
			return Types.isVncVector(coll) ? new VncVector(result) : new VncList(result);
		}
	};
	
	public static VncFunction reverse = new VncFunction("reverse") {
		{
			setArgLists("(reverse coll)");
			
			setDescription(
					" Returns a collection of the items in coll in reverse order");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("reverse", args, 1);
			
			final VncList coll = (VncList)args.nth(0);
			
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
			
			setDescription(
					"Returns a sorted list of the items in coll. If no compare function " + 
					"compfn is supplied, uses the natural compare. The compare function " + 
					"takes two arguments and returns -1, 0, or 1");
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
				else if (Types.isVncMap(coll)) {
					return new VncList(
							 ((VncMap)coll).toVncList()
								.getList()
								.stream()
								.sorted()
								.collect(Collectors.toList()));
				}
				else {
					throw new VncException("sort: collection type not supported");
				}
			}
			else if (args.size() == 2) {
				final VncFunction compfn = (VncFunction)args.nth(0);
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
				else if (Types.isVncMap(coll)) {
					return new VncList(
							 ((VncMap)coll).toVncList()
								.getList()
								.stream()
								.sorted((x,y) -> ((VncLong)compfn.apply(new VncList(x,y))).getValue().intValue())
								.collect(Collectors.toList()));
				}
				else {
					throw new VncException("sort: collection type not supported");
				}
			}
			else {
				throw new VncException("sort: args not supported");
			}			
		}
	};
	
	public static VncFunction sort_by = new VncFunction("sort-by") {
		{
			setArgLists("(sort-by keyfn coll)", "(sort-by keyfn compfn coll)");
			
			setDescription(
					"Returns a sorted sequence of the items in coll, where the sort " + 
					"order is determined by comparing (keyfn item).  If no comparator is " + 
					"supplied, uses compare.");
		}

		public VncVal apply(final VncList args) {
			assertArity("sort-by", args, 2, 3);

			if (args.size() == 2) {
				final VncFunction keyfn = (VncFunction)args.nth(0);
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
					throw new VncException("sort: collection type not supported");
				}
			}
			else if (args.size() == 3) {
				final VncFunction keyfn = (VncFunction)args.nth(0);
				final VncFunction compfn = (VncFunction)args.nth(1);
				final VncVal coll = args.nth(2);
				
				if (Types.isVncVector(coll)) {
					return new VncVector(
							((VncVector)coll)
								.getList()
								.stream()
								.sorted((x,y) -> ((VncLong)compfn.apply(
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
								.sorted((x,y) -> ((VncLong)compfn.apply(
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
								.sorted((x,y) -> ((VncLong)compfn.apply(
														new VncList(
															keyfn.apply(new VncList(x)),
															keyfn.apply(new VncList(y)))
														)
													).getValue().intValue())
								.collect(Collectors.toList()));
				}
				else {
					throw new VncException("sort: collection type not supported");
				}
			}
			else {
				throw new VncException("sort: args not supported");
			}			
		}
	};
	
	public static VncFunction group_by = new VncFunction("group-by") {
		{
			setArgLists("(group-by f coll)");
			
			setDescription(
					"Returns a map of the elements of coll keyed by the result of " + 
					"f on each element. The value at each key will be a vector of the " + 
					"corresponding elements, in the order they appeared in coll.");
		}

		public VncVal apply(final VncList args) {
			assertArity("group-by", args, 2);

			final VncFunction fn = (VncFunction)args.nth(0);
			final VncList coll = (VncList)args.nth(1);

			final VncMap map = new VncOrderedMap();
			
			coll.getList().stream().forEach(v -> {
				final VncVal key = fn.apply(new VncList(v));
				final VncList val = (VncList)map.getMap().get(key);
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
			
			setDescription("Applies f to all arguments composed of args and coll");
		}
		
		public VncVal apply(final VncList args) {
			final VncFunction fn = (VncFunction)args.nth(0);
			final VncList fn_args = args.slice(1,args.size()-1);	
			final List<VncVal> tailArgs = ((VncList)args.last()).getList();
			fn_args.getList().addAll(tailArgs);
			return fn.apply(fn_args);
		}
	};

	public static VncFunction map = new VncFunction("map") {
		{
			setArgLists("(map f coll colls*)");
			
			setDescription(
					"Applys f to the set of first items of each coll, followed by applying " + 
					"f to the set of second items in each coll, until any one of the colls " + 
					"is exhausted.  Any remaining items in other colls are ignored. ");
		}
		
		public VncVal apply(final VncList args) {
			final VncFunction fn = (VncFunction)args.nth(0);
			final VncList lists = (VncList)args.slice(1);
			final VncList result = new VncList();
	
			int index = 0;
			boolean hasMore = true;
			while(hasMore) {
				final VncList fnArgs = new VncList();
				
				for(int ii=0; ii<lists.size(); ii++) {
					final VncList nthList = (VncList)lists.nth(ii);
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

	public static VncFunction mapcat = new VncFunction("mapcat") {
		{
			setArgLists("(mapcat fn & colls)");
			
			setDescription(
					"Returns the result of applying concat to the result of applying map " + 
					"to fn and colls. Thus function fn should return a collection.");
		}
		
		public VncVal apply(final VncList args) {			
			return concat.apply((VncList)map.apply(args));
		}
	};

	public static VncFunction filter = new VncFunction("filter") {
		{
			setArgLists("(filter predicate coll)");
			
			setDescription(
					"Returns a collection of the items in coll for which " + 
					"(predicate item) returns logical true. ");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("filter", args, 2);
			
			final VncFunction predicate = (VncFunction)args.nth(0);
			final VncList coll = (VncList)args.nth(1);
			final VncList result = coll.empty();
			for(int i=0; i<coll.size(); i++) {
				final VncVal val = coll.nth(i);
				final VncVal keep = predicate.apply(new VncList(val));
				if (keep == True) {
					result.getList().add(val);
				}				
			}
			return result;
		}
	};

	public static VncFunction remove = new VncFunction("remove") {
		{
			setArgLists("(remove predicate coll)");
			
			setDescription(
					"Returns a collection of the items in coll for which " + 
					"(predicate item) returns logical false. ");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("remove", args, 2);
			
			final VncFunction predicate = (VncFunction)args.nth(0);
			final VncList coll = (VncList)args.nth(1);
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
			
			setDescription(
					"f should be a function of 2 arguments. If val is not supplied, " + 
					"returns the result of applying f to the first 2 items in coll, then " + 
					"applying f to that result and the 3rd item, etc. If coll contains no " + 
					"items, f must accept no arguments as well, and reduce returns the " + 
					"result of calling f with no arguments.  If coll has only 1 item, it " + 
					"is returned and f is not called.  If val is supplied, returns the " + 
					"result of applying f to val and the first item in coll, then " + 
					"applying f to that result and the 2nd item, etc. If coll contains no " + 
					"items, returns val and f is not called.");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("reduce", args, 2, 3);
			
			final boolean twoArguments = args.size() < 3;
			final VncFunction reduceFn = (VncFunction)args.nth(0);

			if (twoArguments) {
				final List<VncVal> coll = ((VncList)args.nth(1)).getList();
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
				final List<VncVal> coll = ((VncList)args.nth(2)).getList();
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
			
			setDescription(
					"Reduces an associative collection. f should be a function of 3 " + 
					"arguments. Returns the result of applying f to init, the first key " + 
					"and the first value in coll, then applying f to that result and the " + 
					"2nd key and value, etc. If coll contains no entries, returns init " + 
					"and f is not called. Note that reduce-kv is supported on vectors, " + 
					"where the keys will be the ordinals.");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("reduce-kv", args, 3);
			
			final VncFunction reduceFn = (VncFunction)args.nth(0);		
			final Set<Map.Entry<VncVal,VncVal>> values = ((VncHashMap)args.nth(2)).entries();
			
			VncMap value = (VncMap)args.nth(1);
			
			if (values.isEmpty()) {
				return value;
			}
			else {
				for(Map.Entry<VncVal,VncVal> entry : values) {
					final VncVal key = entry.getKey();
					final VncVal val = entry.getValue();
					
					value = (VncMap)reduceFn.apply(new VncList(value, key, val));
				}
				
				return value;
			}
		}
	};

	public static VncFunction conj = new VncFunction("conj") {
		{
			setArgLists("(conj coll x)", "(conj coll x & xs)");
			
			setDescription(
					"Returns a new collection with the x, xs " + 
					"'added'. (conj nil item) returns (item).  The 'addition' may " + 
					"happen at different 'places' depending on the concrete type.");
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
							"Invalid x %s while calling function 'conj'.",
							Types.getClassName(args.nth(1))));
				}
			}
			else {
				throw new VncException(String.format(
						"Invalid coll %s while calling function 'conj'.",
						Types.getClassName(args.nth(0))));
			}
		}
	};

	public static VncFunction seq = new VncFunction("seq") {
		{
			setArgLists("(seq coll)");
			
			setDescription(
					"Returns a seq on the collection. If the collection is " + 
					"empty, returns nil.  (seq nil) returns nil. seq also works on " + 
					"Strings.");
		}

		public VncVal apply(final VncList args) {
			assertArity("seq", args, 1);

			final VncVal val = (VncVal)args.nth(0);
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
				throw new VncException("seq: called on non-sequence");
			}
		}
	};

	public static VncFunction range = new VncFunction("range") {
		{
			setArgLists("(range end)", "(range start end)", "(range start end step)");
			
			setDescription(
					"Returns a collection of numbers from start (inclusive) to end " + 
					"(exclusive), by step, where start defaults to 0 and step defaults to 1. " +
					"When start is equal to end, returns empty list.");
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
				throw new VncException("range: start value must be a number");	
			}
			if (!Types.isVncNumber(end)) {
				throw new VncException("range: end value must be a number");	
			}
			if (!Types.isVncNumber(step)) {
				throw new VncException("range: step value must be a number");	
			}

			final List<VncVal> values = new ArrayList<>();

			if (zero_Q.apply(new VncList(step)) == True) {
				throw new VncException("range: a step value must not be 0");	
			}
			
			if (pos_Q.apply(new VncList(step)) == True) {
				if (lt.apply(new VncList(end, start)) == True) {
					throw new VncException("range positive step: end must not be lower than start");	
				}
				
				VncVal val = start;
				while(lt.apply(new VncList(val, end)) == True) {
					values.add(val);
					val = add.apply(new VncList(val, step));
				}
			}
			else {
				if (gt.apply(new VncList(end, start)) == True) {
					throw new VncException("range negative step: end must not be greater than start");	
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
			
			setDescription("Returns a collection with the value x repeated n times");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("repeat", args, 2);

			if (!Types.isVncLong(args.nth(0))) {
				throw new VncException("repeat: the count must be a long");	
			}
			
			final long repeat = ((VncLong)args.nth(0)).getValue();
			if (repeat < 0) {
				throw new VncException("repeat: a count n must be grater or equal to 0");	
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
			
			setDescription(
					"Returns the metadata of obj, returns nil if there is no metadata.");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("meta", args, 1);

			return args.nth(0).getMeta();
		}
	};

	public static VncFunction with_meta = new VncFunction("with-meta") {
		{
			setArgLists("(with-meta obj m)");
			
			setDescription(
					"Returns a copy of the object obj, with a map m as its metadata.");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("meta", args, 2);

			if (!Types.isVncMap(args.nth(1))) {
				throw new VncException("with-meta: the meta data for the object must be a map");	
			}

			final VncVal new_obj = ((VncVal)args.nth(0)).copy();
			new_obj.setMeta(args.nth(1));
			return new_obj;
		}
	};

	public static VncFunction vary_meta = new VncFunction("vary-meta") {
		{
			setArgLists("(vary-meta obj f & args)");
			
			setDescription(
					"Returns a copy of the object obj, with (apply f (meta obj) args) as its metadata.");
		}
		
		public VncVal apply(final VncList args) {
			if (args.size() < 2) {
				throw new VncException("vary-meta requires at leat two arguments");	
			}

			if (!Types.isVncFunction(args.nth(1))) {
				throw new VncException("var-meta requires a function as second argument");	
			}

			final VncVal meta = args.nth(0).getMeta();
			final VncFunction fn = (VncFunction)args.nth(1);
			final VncList fnArgs = args.slice(2);
			fnArgs.addAtStart(meta == Nil ? new VncHashMap() : meta);
			
			final VncVal new_obj = ((VncVal)args.nth(0)).copy();
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
			
			setDescription("Creates an atom with the initial value x");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("atom", args, 1);
			
			return new VncAtom(args.nth(0));
		}
	};

	public static VncFunction atom_Q = new VncFunction("atom?") {
		{
			setArgLists("(atom? x)");
			
			setDescription("Returns true if x is an atom, otherwise false");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("atom?", args, 1);
			
			return Types.isVncAtom(args.nth(0)) ? True : False;
		}
	};

	public static VncFunction deref = new VncFunction("deref") {
		{
			setArgLists("(deref atom)");
			
			setDescription("Dereferences an atom, returns its value");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("deref", args, 1);
			
			final VncAtom atm = (VncAtom)args.nth(0);
			return atm.deref();
		}
	};

	public static VncFunction reset_BANG = new VncFunction("reset!") {
		{
			setArgLists("(reset! atom newval)");
			
			setDescription(
					"Sets the value of atom to newval without regard for the " + 
					"current value. Returns newval.");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("reset!", args, 2);
			
			final VncAtom atm = (VncAtom)args.nth(0);
			return atm.reset(args.nth(1));
		}
	};

	public static VncFunction swap_BANG = new VncFunction("swap!") {
		{
			setArgLists("(swap! atom f & args)");
			
			setDescription(
					"Atomically swaps the value of atom to be: " + 
					"(apply f current-value-of-atom args). Note that f may be called " + 
					"multiple times, and thus should be free of side effects.  Returns " + 
					"the value that was swapped in.");
		}
		
		public VncVal apply(final VncList args) {
			if (args.size() < 2) {
				throw new VncException("swap! requires at leat two arguments");	
			}
			
			final VncAtom atm = (VncAtom)args.nth(0);		
			final VncFunction fn = (VncFunction)args.nth(1);
			final VncList swapArgs = ((VncList)args.slice(2));
			
			return atm.swap(fn, swapArgs);
		}
	};

	public static VncFunction compare_and_set_BANG = new VncFunction("compare-and-set!") {
		{
			setArgLists("(compare-and-set! atom oldval newval)");
			
			setDescription(
					"Atomically sets the value of atom to newval if and only if the " + 
					"current value of the atom is identical to oldval. Returns true if " + 
					"set happened, else false");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("compare-and-set!", args, 3);
			
			final VncAtom atm = (VncAtom)args.nth(0);		
			
			return atm.compare_and_set(args.nth(1), args.nth(2));
		}
	};

	
	///////////////////////////////////////////////////////////////////////////
	// IO functions
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction io_file = new VncFunction("io/file") {
		{
			setArgLists("(io/file path) (io/file parent child)");
			
			setDescription(
					"Returns a java.io.File. path, parent, and child can be a string " +
					"or java.io.File");
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
							"Function 'io/file' does not allow %s as path",
							Types.getClassName(path)));
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
							"Function 'io/file' does not allow %s as parent",
							Types.getClassName(parent)));
				}

				if (Types.isVncString(child)) {
					 return new VncJavaObject(new File(parentFile, ((VncString)child).getValue()));					
				}
				else {
					throw new VncException(String.format(
							"Function 'io/file' does not allow %s as child",
							Types.getClassName(child)));
				}
			}		
		}
	};

	public static VncFunction io_file_Q = new VncFunction("io/file?") {
		{
			setArgLists("(io/file? x)");
			
			setDescription("Returns true if x is a java.io.File.");
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
			
			setDescription("Returns true if the file x exists. x must be a java.io.File.");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("io/exists-file?", args, 1);
									
			if (!isJavaIoFile(args.nth(0)) ) {
				throw new VncException(String.format(
						"Function 'io/exists-file?' does not allow %s as x",
						Types.getClassName(args.nth(0))));
			}

			final File file = (File)((VncJavaObject)args.nth(0)).getDelegate();
			return file.exists() ? True : False;
		}
	};

	public static VncFunction io_delete_file = new VncFunction("io/delete-file") {
		{
			setArgLists("(io/delete-file x)");
			
			setDescription("Deletes a file. x must be a java.io.File.");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().checkBlackListedVeniceFunction("io/delete-file", args);

			assertArity("io/delete-file", args, 1);

			if (!isJavaIoFile(args.nth(0)) ) {
				throw new VncException(String.format(
						"Function 'io/delete-file' does not allow %s as x",
						Types.getClassName(args.nth(0))));
			}

			final File file = (File)((VncJavaObject)args.nth(0)).getDelegate();
			try {
				Files.deleteIfExists(file.toPath());	
			}
			catch(Exception ex) {
				throw new VncException(String.format(
						"Failed to delete file %s", file.getPath()));
			}
			
			return Nil;
		}
	};

	public static VncFunction io_copy_file = new VncFunction("io/copy-file") {
		{
			setArgLists("(io/copy input output)");
			
			setDescription(
					"Copies input to output. Returns nil or throws IOException. " + 
					"Input and output must be a java.io.File.");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().checkBlackListedVeniceFunction("io/copy-file", args);

			assertArity("io/copy-file", args, 2);

			if (!isJavaIoFile(args.nth(0)) ) {
				throw new VncException(String.format(
						"Function 'io/delete-file' does not allow %s as input",
						Types.getClassName(args.nth(0))));
			}
			if (!isJavaIoFile(args.nth(1)) ) {
				throw new VncException(String.format(
						"Function 'io/delete-file' does not allow %s as output",
						Types.getClassName(args.nth(1))));
			}


			final File from = (File)((VncJavaObject)args.nth(0)).getDelegate();
			final File to = (File)((VncJavaObject)args.nth(1)).getDelegate();
			
			try {
				Files.copy(from.toPath(), to.toPath());
			}
			catch(Exception ex) {
				throw new VncException(String.format(
						"Failed to copy file %s to %s", from.getPath(), to.getPath()));
			}
			
			return Nil;
		}
	};

	public static VncFunction io_tmp_dir = new VncFunction("io/tmp-dir") {
		{
			setArgLists("(io/tmp-dir)");
			
			setDescription("Returns the tmp dir as a java.io.File.");
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
			
			setDescription("Returns the user dir (current working dir) as a java.io.File.");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().checkBlackListedVeniceFunction("io/user-dir", args);

			assertArity("io/user-dir", args, 0);

			return new VncJavaObject(new File(System.getProperty("user.dir")));
		}
	};

	
	///////////////////////////////////////////////////////////////////////////
	// String functions
	///////////////////////////////////////////////////////////////////////////
	
	public static VncFunction str_starts_with = new VncFunction("str/starts-with?") {
		{
			setArgLists("(str/starts-with? s substr)");
			
			setDescription("True if s starts with substr.");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/starts-with?", args, 2);

			if (args.nth(0) == Nil || args.nth(1) == Nil) {
				return False;
			}
			
			final VncString string = (VncString)args.nth(0);		
			final VncString prefix = (VncString)args.nth(1);		
			
			return string.getValue().startsWith(prefix.getValue()) ? True : False;
		}
	};
	
	public static VncFunction str_ends_with = new VncFunction("str/ends-with?") {
		{
			setArgLists("(str/ends-with? s substr)");
			
			setDescription("True if s ends with substr.");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/ends-with?", args, 2);

			if (args.nth(0) == Nil || args.nth(1) == Nil) {
				return False;
			}

			final VncString string = (VncString)args.nth(0);		
			final VncString suffix = (VncString)args.nth(1);		
			
			return string.getValue().endsWith(suffix.getValue()) ? True : False;
		}
	};
	
	public static VncFunction str_contains = new VncFunction("str/contains?") {
		{
			setArgLists("(str/contains? s substr)");
			
			setDescription("True if s contains with substr.");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/contains?", args, 2);

			if (args.nth(0) == Nil || args.nth(1) == Nil) {
				return False;
			}

			final VncString string = (VncString)args.nth(0);		
			final VncString text = (VncString)args.nth(1);		
			
			return string.getValue().contains(text.getValue()) ? True : False;
		}
	};
	
	public static VncFunction str_trim = new VncFunction("str/trim") {
		{
			setArgLists("(str/trim s substr)");
			
			setDescription("Trims leading and trailing spaces from s.");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/trim", args, 1);

			if (args.nth(0) == Nil) {
				return Nil;
			}

			return new VncString(((VncString)args.nth(0)).getValue().trim());
		}
	};
	
	public static VncFunction str_trim_to_nil = new VncFunction("str/trim-to-nil") {
		{
			setArgLists("(str/trim-to-nil s substr)");
			
			setDescription(
					"Trims leading and trailing spaces from s. " +
					"Returns nil if the rewsulting string is empry");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/trim-to-nil", args, 1);

			if (args.nth(0) == Nil) {
				return Nil;
			}

			final String str = ((VncString)args.nth(0)).getValue().trim();
			return str.isEmpty() ? Nil : new VncString(str);
		}
	};
	
	public static VncFunction str_index_of = new VncFunction("str/index-of") {
		{
			setArgLists("(str/index-of s value)", "(str/index-of s value from-index)");
			
			setDescription(
					"Return index of value (string or char) in s, optionally searching " + 
					"forward from from-index. Return nil if value not found.");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/index-of", args, 2, 3);

			final String text = ((VncString)args.nth(0)).getValue();	
			final String searchString = ((VncString)args.nth(1)).getValue();		
			
			if (args.size() == 3) {
				final int startPos = ((VncLong)args.nth(2)).getValue().intValue();
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
			
			setDescription(
					"Return last index of value (string or char) in s, optionally\n" + 
					"searching backward from from-index. Return nil if value not found.");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/last-index-of", args, 2, 3);

			if (args.nth(0) == Nil) {
				return Nil;
			}

			final String text = ((VncString)args.nth(0)).getValue();	
			final String searchString = ((VncString)args.nth(1)).getValue();		
			
			if (args.size() > 2) {
				final int startPos = ((VncLong)args.nth(2)).getValue().intValue();
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
			
			setDescription(
					"Replaces the first occurrance of search in s");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/replace-first", args, 3);

			if (args.nth(0) == Nil) {
				return Nil;
			}

			final String text = ((VncString)args.nth(0)).getValue();	
			final String searchString = ((VncString)args.nth(1)).getValue();		
			final String replacement = ((VncString)args.nth(2)).getValue();		

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
			
			setDescription(
					"Replaces the last occurrance of search in s");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/replace-last", args, 3);

			if (args.nth(0) == Nil) {
				return Nil;
			}

			final String text = ((VncString)args.nth(0)).getValue();	
			final String searchString = ((VncString)args.nth(1)).getValue();		
			final String replacement = ((VncString)args.nth(2)).getValue();		

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
			
			setDescription(
					"Replaces the all occurrances of search in s");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/replace-all", args, 3);

			if (args.nth(0) == Nil) {
				return Nil;
			}

			final String text = ((VncString)args.nth(0)).getValue();	
			final String searchString = ((VncString)args.nth(1)).getValue();		
			final String replacement = ((VncString)args.nth(2)).getValue();		
			
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
			
			setDescription("Converts s to lowercase");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/lower-case", args, 1);

			if (args.nth(0) == Nil) {
				return Nil;
			}

			final VncString string = (VncString)args.nth(0);		
			
			return new VncString(string.getValue().toLowerCase());
		}
	};
	
	public static VncFunction str_upper_case = new VncFunction("str/upper-case") {
		{
			setArgLists("(str/upper-case s)");
			
			setDescription("Converts s to uppercase");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/upper-case", args, 1);

			if (args.nth(0) == Nil) {
				return Nil;
			}

			final VncString string = (VncString)args.nth(0);		
			
			return new VncString(string.getValue().toUpperCase());
		}
	};
	
	public static VncFunction str_join = new VncFunction("str/join") {
		{
			setArgLists("(str/join coll)", "(str/join separator coll)");
			
			setDescription("Joins all elements in coll separated by an optional separator.");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/join", args, 1, 2);

			final VncList strings = (VncList)args.nth(0);		
			final VncString delim = (VncString)args.nth(1);
			
			return new VncString(
						strings.size() > 0
							? strings
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
			
			setDescription(
					"Returns the substring of s beginning at start inclusive, and ending " + 
					"at end (defaults to length of string), exclusive.");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/subs", args, 2, 3);

			final VncString string = (VncString)args.nth(0);		
			final VncLong from = (VncLong)args.nth(1);
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
			
			setDescription(
					"Splits string on a regular expression.");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/split", args, 2);

			final VncString string = (VncString)args.nth(0);		
			final VncString regex = (VncString)args.nth(1);
			
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
			
			setDescription("Splits s into lines.");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("str/split-lines", args, 1);

			return args.nth(0) == Nil
					? new VncList()
					: new VncList(
							StringUtil
								.splitIntoLines(((VncString)args.nth(0)).getValue())
								.stream()
								.map(s -> new VncString(s))
								.collect(Collectors.toList()));
		}
	};
	
	public static VncFunction str_format = new VncFunction("str/format") {
		{
			setArgLists("(str/format s format args*)");
			
			setDescription("Returns a formatted string using the specified format string and arguments.");
		}
		
		public VncVal apply(final VncList args) {
			final VncString fmt = (VncString)args.nth(0);		
			final List<Object> fmtArgs = args
										.slice(1)
										.getList()
										.stream()
										.map(v -> JavaInteropUtil.convertToJavaObject(v))
										.collect(Collectors.toList());
			
			return new VncString(String.format(fmt.unkeyword().getValue(), fmtArgs.toArray()));		
		}
	};
	
	public static VncFunction str_truncate = new VncFunction("str/truncate") {
		{
			setArgLists("(str/truncate s maxlen marker)");
			
			setDescription(
					"Truncates a string to the max lenght maxlen and adds the " +
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
			
			return new VncString(StringUtil.truncate(
					((VncString)args.nth(0)).getValue(), 
					((VncLong)args.nth(1)).getValue().intValue(), 					
					((VncString)args.nth(2)).getValue()));		
		}
	};


	///////////////////////////////////////////////////////////////////////////
	// Utilities
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction gensym = new VncFunction("gensym") {
		{
			setArgLists("(gensym)", "(gensym prefix)");
			
			setDescription("Generates a symbol.");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("gensym", args, 0, 1);
			
			final String prefix = args.isEmpty() 
									? "G__" 
									: Types.isVncSymbol(args.nth(0))
										? ((VncSymbol)args.nth(0)).getName()
										: ((VncString)args.nth(0)).getValue();
			return new VncSymbol(prefix + String.valueOf(gensymValue.incrementAndGet()));
		}
	};

	public static VncFunction uuid = new VncFunction("uuid") {
		{
			setArgLists("(uuid)");
			
			setDescription("Generates a UUID.");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("uuid", args, 0);
			return new VncString(UUID.randomUUID().toString());
		}
	};
	
	public static Set<String> getAllIoFunctions() {
		return new HashSet<>(Arrays.asList(
								"prn",
								"println",
								"readline",
								"slurp",
								"spit",
								"load-file",
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
	
	private static void assertArity(
			final String fnName, 
			final VncList args, 
			final int...expectedArities
	) {
		final int arity = args.size();
		for (int a : expectedArities) {
			if (a == arity) return;
		}		
		throw new ArityException(arity, fnName);
	}

	private static boolean isJavaIoFile(final VncVal val) {
		return (Types.isVncJavaObject(val) && ((VncJavaObject)val).getDelegate() instanceof File);
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
				.put("prn",					prn)
				.put("println",				println)
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

				.put("class",				className)
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
				.put("set?",				set_Q)
				.put("map?",				map_Q)
				.put("hash-map?",			hash_map_Q)
				.put("ordered-map?",		ordered_map_Q)
				.put("sorted-map?",			sorted_map_Q)
				.put("set",					new_set)
				.put("hash-map",			new_hash_map)
				.put("ordered-map",			new_ordered_map)
				.put("sorted-map",			new_sorted_map)
				.put("assoc",				assoc)
				.put("dissoc",				dissoc)
				.put("contains?", 			contains_Q)
				.put("find",				find)
				.put("get",					get)
				.put("key",					key)
				.put("keys",				keys)
				.put("val",					val)
				.put("vals",				vals)
				.put("subvec", 				subvec)
				.put("subbytebuf", 			subbytebuf)
		
				.put("into",				into)
				.put("seq?",	    		seq_Q)
				.put("coll?",	    		coll_Q)
				.put("cons",				cons)
				.put("co",					cons)
				.put("concat",				concat)
				.put("mapcat",				mapcat)
				.put("nth",					nth)
				.put("first",				first)
				.put("second",				second)
				.put("last",				last)
				.put("rest",				rest)
				.put("empty-to-nil",		emptyToNil)
				.put("pop",					pop)
				.put("peek",				peek)
				.put("empty?",				empty_Q)
				.put("not-empty?",			not_empty_Q)
				.put("count",				count)
				.put("apply",				apply)
				.put("map",					map)
				.put("filter",				filter)
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
				
				.put("io/file",				io_file)
				.put("io/file?",			io_file_Q)
				.put("io/exists-file?",		io_exists_file_Q)
				.put("io/delete-file",		io_delete_file)
				.put("io/copy-file",		io_copy_file)
				.put("io/tmp-dir",			io_tmp_dir)
				.put("io/user-dir",			io_user_dir)
				
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
				.put("str/truncate",		str_truncate)

				.toMap();

	
	private static final AtomicLong gensymValue = new AtomicLong(0);
	private static final Random random = new Random();
}
