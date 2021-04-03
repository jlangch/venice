/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2021 Venice
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

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.json.VncJsonReader;
import com.github.jlangch.venice.impl.util.json.VncJsonWriter;
import com.github.jlangch.venice.nanojson.JsonAppendableWriter;
import com.github.jlangch.venice.nanojson.JsonParser;
import com.github.jlangch.venice.nanojson.JsonReader;
import com.github.jlangch.venice.nanojson.JsonWriter;


public class JsonFunctions {

	///////////////////////////////////////////////////////////////////////////
	// JSON
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction write_str = 
		new VncFunction(
				"json/write-str", 
				VncFunction
					.meta()
					.arglists(
						"(json/write-str val & options)")		
					.doc(
						"Writes the val to a JSON string.\n\n" +
						"Options are: \n" +
						"  :pretty boolean \n" + 
						"      Enables/disables pretty printing. \n" +
						"      Defaults to false. \n" +
						"  :decimal-as-double boolean \n" + 
						"      If true emit a decimal as double else as string. \n" +
						"      Defaults to false.")
					.examples(
						"(json/write-str {:a 100 :b 100})",
						"(json/write-str {:a 100 :b 100} :pretty true)")
					.seeAlso("json/read-str", "json/spit", "json/slurp", "json/pretty-print")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertMinArity(this, args, 1);
	
				final VncVal val = args.first();
				
				if (val == Nil) {
					return Nil;
				}
				else {					
					final VncHashMap options = VncHashMap.ofAll(args.slice(1));
					final boolean prettyPrint = isTrueOption(options, "pretty"); 
					final boolean decimalAsDouble = isTrueOption(options, "decimal-as-double"); 

					final StringBuilder sb = new StringBuilder();
					
					final JsonAppendableWriter writer = prettyPrint
															? JsonWriter.indent(INDENT).on(sb)
															: JsonWriter.on(sb);
								
					new VncJsonWriter(writer, decimalAsDouble).write(val).done();
					
					return new VncString(sb.toString());
				}
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction spit = 
		new VncFunction(
				"json/spit", 
				VncFunction
					.meta()
					.arglists(
						"(json/spit out val & options)")		
					.doc(
						"Spits the JSON converted val to the output.\n" +
						"out maybe a file, a Java OutputStream, or a Java Writer. \n\n" +
						"Options are: \n" +
						"  :pretty boolean \n" + 
						"      Enables/disables pretty printing. \n" +
						"      Defaults to false. \n" +
						"  :decimal-as-double boolean \n" + 
						"      If true emit a decimal as double else as string. \n" +
						"      Defaults to false. \n" +
						"  :encoding enc - e.g :encoding :utf-8, defaults to :utf-8")
					.examples(
						"(let [out (. :java.io.ByteArrayOutputStream :new)]           \n" +
						"  (json/spit out {:a 100 :b 100 :c [10 20 30]})              \n" +
						"  (. out :flush)                                             \n" +
						"  (. :java.lang.String :new (. out :toByteArray) \"utf-8\"))   ")
					.seeAlso("json/write-str", "json/read-str", "json/slurp", "json/pretty-print")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertMinArity(this, args, 2);

				sandboxFunctionCallValidation();

				final Object out = Coerce.toVncJavaObject(args.first()).getDelegate();
				final VncVal val = args.second();
				
				if (val == Nil) {
					return Nil;
				}
				else {
					final VncHashMap options = VncHashMap.ofAll(args.slice(2));
					final boolean prettyPrint = isTrueOption(options, "pretty"); 
					final boolean decimalAsDouble = isTrueOption(options, "decimal-as-double"); 
					final String encoding = encoding(options.get(new VncKeyword("encoding")));

					if (out instanceof File) {
						try (BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream((File)out), encoding))) {
							final JsonAppendableWriter writer = prettyPrint
									? JsonWriter.indent(INDENT).on(wr)
									: JsonWriter.on(wr);
									
							new VncJsonWriter(writer, decimalAsDouble).write(val).done();
						}
						catch(Exception ex) {
							throw new VncException("Function 'json/spit'. Failed to spit JSON to File", ex);
						}				
					}
					else if (out instanceof PrintStream) {
						final JsonAppendableWriter writer = prettyPrint
																? JsonWriter.indent(INDENT).on((PrintStream)out)
																: JsonWriter.on((PrintStream)out);
																
						new VncJsonWriter(writer, decimalAsDouble).write(val).done();
					}
					else if (out instanceof OutputStream) {
						try (BufferedWriter wr = new BufferedWriter(new OutputStreamWriter((OutputStream)out, encoding))) {
							final JsonAppendableWriter writer = prettyPrint
																	? JsonWriter.indent(INDENT).on(wr)
																	: JsonWriter.on(wr);
																	
							new VncJsonWriter(writer, decimalAsDouble).write(val).done();
						}
						catch(Exception ex) {
							throw new VncException("Function 'json/spit'. Failed to spit JSON to File", ex);
						}				
					}
					else if (out instanceof Writer) {
						final JsonAppendableWriter writer = prettyPrint
																? JsonWriter.indent(INDENT).on((Writer)out)
																: JsonWriter.on((Writer)out);
																
						new VncJsonWriter(writer, decimalAsDouble).write(val).done();
					}
					else {
						throw new VncException(String.format(
								"Function 'json/spit' does not allow %s as out",
								Types.getType(args.first())));
					}
					
					return Nil;
				}
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction read_str = 
		new VncFunction(
				"json/read-str", 
				VncFunction
					.meta()
					.arglists("(json/read-str s & options)")		
					.doc(
						"Reads a JSON string and returns it as a Venice datatype.\n\n" + 
						"Options are: \n" +
						"  :key-fn fn \n" + 
						"      Single-argument function called on JSON property names; \n" +
						"      return value will replace the property names in the output. \n" +
						"      Default is 'identity', use 'keyword' to get keyword \n" +
						"      properties. \n" +
						"  :value-fn fn \n" + 
						"      Function to transform values in JSON objects in\n" + 
						"      the output. For each JSON property, value-fn is called with\n" + 
						"      two arguments: the property name (transformed by key-fn) and\n" + 
						"      the value. The return value of value-fn will replace the value\n" + 
						"      in the output. The default value-fn returns the value unchanged.\n" + 
						"  :decimal boolean \n" + 
						"      If true use BigDecimal for decimal numbers instead of Double.\n" + 
						"      Default is false.")
					.examples(
						"(json/read-str (json/write-str {:a 100 :b 100}))",
						"(json/read-str (json/write-str {:a 100 :b 100}) :key-fn keyword)",
						"(json/read-str (json/write-str {:a 100 :b 100}) \n" +
						"                   :value-fn (fn [k v] (if (== \"a\" k) (inc v) v)))")
					.seeAlso("json/write-str", "json/spit", "json/slurp", "json/pretty-print")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertMinArity(this, args, 1);
	
				final VncVal val = args.first();
				
				if (val == Nil) {
					return Nil;
				}
				else {
					try {
						final VncString s = Coerce.toVncString(val);
						
						final VncHashMap options = VncHashMap.ofAll(args.slice(1));
						final VncFunction key_fn = getFunctionOption(options, "key-fn"); 
						final VncFunction value_fn = getFunctionOption(options, "value-fn"); 
						final boolean toDecimal = isTrueOption(options, "decimal"); 

						final Function<VncVal,VncVal> keyFN = 
								key_fn == null ? null : (key) -> key_fn.apply(VncList.of(key));

						final BiFunction<VncVal,VncVal,VncVal> valueFN = 
								value_fn == null ? null : (k, v) -> value_fn.apply(VncList.of(k, v));

						return new VncJsonReader(
									JsonReader.from(s.getValue()), keyFN, valueFN, toDecimal).read();
					}
					catch(Exception ex) {
						throw new VncException("Function 'json/read-str'. Failed to read JSON string", ex);
					}
				}
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction slurp = 
		new VncFunction(
				"json/slurp", 
				VncFunction
					.meta()
					.arglists("(json/slurp in & options)")		
					.doc(
						"Slurps a JSON string from the input and returns it as a Venice datatype.\n" +
						"in maybe a file, a Java InputStream, or a Java Reader. \n\n" +
						"Options are: \n" +
						"  :key-fn fn  \n" + 
						"      Single-argument function called on JSON property names; \n" +
						"      return value will replace the property names in the output. \n" +
						"      Default is 'identity', use 'keyword' to get keyword \n" +
						"      properties. \n" +
						"  :value-fn fn \n" + 
						"      Function to transform values in JSON objects in\n" + 
						"      the output. For each JSON property, value-fn is called with\n" + 
						"      two arguments: the property name (transformed by key-fn) and\n" + 
						"      the value. The return value of value-fn will replace the value\n" + 
						"      in the output. The default value-fn returns the value unchanged.\n" + 
						"  :decimal boolean \n" + 
						"      If true use BigDecimal for decimal numbers instead of Double.\n" + 
						"      Default is false.\n" +
						"  :encoding enc - e.g :encoding :utf-8, defaults to :utf-8")
					.examples(
						"(let [json (json/write-str {:a 100 :b 100})             \n" +
						"      data (bytebuf-from-string json :utf-8)            \n" +
						"      in (. :java.io.ByteArrayInputStream :new data)]   \n" +
						"  (str (json/slurp in)))                                  ")
					.seeAlso("json/write-str", "json/read-str", "json/spit", "json/pretty-print")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertMinArity(this, args, 1);

				sandboxFunctionCallValidation();

				final VncVal val = args.first();
				
				if (val == Nil) {
					return Nil;
				}
				else {
					try {
						final Object in = Coerce.toVncJavaObject(args.first()).getDelegate();

						final VncHashMap options = VncHashMap.ofAll(args.slice(1));
						final VncFunction key_fn = getFunctionOption(options, "key-fn"); 
						final VncFunction value_fn = getFunctionOption(options, "value-fn"); 
						final boolean toDecimal = isTrueOption(options, "decimal"); 
						final String encoding = encoding(options.get(new VncKeyword("encoding")));

						final Function<VncVal,VncVal> keyFN = 
								key_fn == null ? null : (key) -> key_fn.apply(VncList.of(key));

						final BiFunction<VncVal,VncVal,VncVal> valueFN = 
								value_fn == null ? null : (k, v) -> value_fn.apply(VncList.of(k, v));

						if (in instanceof File) {
							try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream((File)in), encoding))) {
								return new VncJsonReader(JsonReader.from(br), keyFN, valueFN, toDecimal).read();
							}
						}
						else if (in instanceof InputStream) {
							try (BufferedReader br = new BufferedReader(new InputStreamReader((InputStream)in, encoding))) {
								return new VncJsonReader(JsonReader.from(br), keyFN, valueFN, toDecimal).read();
							}
						}
						else if (in instanceof Reader) {
							return new VncJsonReader(JsonReader.from((Reader)in), keyFN, valueFN, toDecimal).read();
						}
						else {
							throw new VncException(String.format(
									"Function 'json/slurp' does not allow %s as in",
									Types.getType(args.first())));
						}
					}
					catch(Exception ex) {
						throw new VncException("Function 'json/slurp'. Failed to parse JSON", ex);
					}
				}
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction pretty_print = 
		new VncFunction(
				"json/pretty-print", 
				VncFunction
					.meta()
					.arglists("(json/pretty-print s)")		
					.doc("Pretty prints a JSON string")
					.examples("(json/pretty-print (json/write-str {:a 100 :b 100}))")
					.seeAlso("json/write-str", "json/read-str", "json/spit", "json/slurp")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);
	
				final VncVal val = args.first();
				
				if (val == Nil) {
					return Nil;
				}
				else {
					try {
						final VncString s = Coerce.toVncString(val);
						final Object o = JsonParser.any().from(s.getValue());
						return new VncString(JsonWriter.indent("  ").string() .value(o).done());
					}
					catch(Exception ex) {
						throw new VncException("Function 'json/pretty-print'. Failed to pretty print JSON", ex);
					}
				}
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};

	private static boolean isTrueOption(final VncHashMap options, final String optionName) {
		return VncBoolean.isTrue(options.get(new VncKeyword(optionName),VncBoolean.False)); 
	}
	
	private static VncFunction getFunctionOption(final VncHashMap options, final String optionName) {
		final VncVal val = options.get(new VncKeyword(optionName)); 
		return val == Constants.Nil ? null : Coerce.toVncFunction(val);
	}
	
	private static String encoding(final VncVal enc) {
		return enc == Nil
				? "UTF-8"
				: Types.isVncKeyword(enc)
					? Coerce.toVncKeyword(enc).getValue()
					: Coerce.toVncString(enc).getValue();
	}

	
	private static final String INDENT = "  ";
	
	
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap
					.Builder()
					.add(write_str)
					.add(spit)
					.add(read_str)
					.add(slurp)
					.add(pretty_print)
					.toMap();	
}
