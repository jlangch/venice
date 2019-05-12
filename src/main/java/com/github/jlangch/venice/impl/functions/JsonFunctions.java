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
import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.function.Function;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
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
					.module("json")
					.arglists(
						"(json/write-str val & options)")		
					.doc(
						"Writes the val to a JSON string.\n" +
						"Options are : \n" +
						"  :pretty true/false  \n" + 
						"      Enables/disables pretty printing. Defaults to false.")
					.examples(
						"(json/write-str {:a 100 :b 100})",
						"(json/write-str {:a 100 :b 100} :pretty true)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertMinArity("json/write", args, 1);
	
				final VncVal val = args.first();
				
				if (val == Nil) {
					return Nil;
				}
				else {					
					final VncHashMap options = VncHashMap.ofAll(args.slice(1));
					final VncVal pretty = options.get(new VncKeyword("pretty")); 

					final StringBuilder sb = new StringBuilder();
					
					final JsonAppendableWriter writer = pretty == Constants.True
															? JsonWriter.indent(INDENT).on(sb)
															: JsonWriter.on(sb);
								
					new VncJsonWriter(writer).write(val).done();
					
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
					.module("json")
					.arglists(
						"(json/spit out val & options)")		
					.doc(
						"Spits the JSON converted val to the output.\n" +
						"out maybe a Java OutputStream or a Java Writer. \n" +
						"Options are : \n" +
						"  :pretty true/false  \n" + 
						"      Enables/disables pretty printing. Defaults to false.")
					.examples(
						"(let [out (. :java.io.ByteArrayOutputStream :new)]           \n" +
						"  (json/spit out {:a 100 :b 100 :c [10 20 30]})              \n" +
						"  (. out :flush)                                             \n" +
						"  (. :java.lang.String :new (. out :toByteArray) \"utf-8\"))   ")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertMinArity("json/spit", args, 2);
	
				final Object out = Coerce.toVncJavaObject(args.first()).getDelegate();
				final VncVal val = args.second();
				
				if (val == Nil) {
					return Nil;
				}
				else {
					final VncHashMap options = VncHashMap.ofAll(args.slice(2));
					final VncVal pretty = options.get(new VncKeyword("pretty")); 

					if (out instanceof PrintStream) {
						final JsonAppendableWriter writer = pretty == Constants.True
																? JsonWriter.indent(INDENT).on((PrintStream)out)
																: JsonWriter.on((PrintStream)out);
																
						new VncJsonWriter(writer).write(val).done();
					}
					else if (out instanceof OutputStream) {
						final JsonAppendableWriter writer = pretty == Constants.True
																? JsonWriter.indent(INDENT).on((OutputStream)out)
																: JsonWriter.on((OutputStream)out);
																
						new VncJsonWriter(writer).write(val).done();
					}
					else if (out instanceof Writer) {
						final JsonAppendableWriter writer = pretty == Constants.True
																? JsonWriter.indent(INDENT).on((Writer)out)
																: JsonWriter.on((OutputStream)out);
																
						new VncJsonWriter(writer).write(val).done();
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
					.module("json")
					.arglists("(json/read-str s & options)")		
					.doc(
						"Reads a JSON string and returns it as a venice datatype.\n" + 
						"Options are : \n" +
						"  :key-fn fn  \n" + 
						"      Single-argument function called on JSON property names; \n" +
						"      return value will replace the property names in the output. \n" +
						"      Default is 'identity', use 'keyword' to get keyword \n" +
						"      properties.")
					.examples("(json/read-str (json/write-str [{:a 100 :b 100}]))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertMinArity("json/parse", args, 1);
	
				final VncVal val = args.first();
				
				if (val == Nil) {
					return Nil;
				}
				else {
					try {
						final VncString s = Coerce.toVncString(val);
						
						final VncHashMap options = VncHashMap.ofAll(args.slice(1));
						final VncVal key_fn = options.get(new VncKeyword("key-fn")); 

						final Function<VncVal,VncVal> keyFN = 
								key_fn == Nil 
									? null
									: (key) -> ((VncFunction)key_fn).apply(VncList.of(key));
						
						return new VncJsonReader(JsonReader.from(s.getValue()), keyFN).read();
					}
					catch(Exception ex) {
						throw new VncException("Failed to read JSON string", ex);
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
					.module("json")
					.arglists("(json/slurp in & options)")		
					.doc(
						"Slurps a JSON string from the input and returns it as a venice datatype.\n" +
						"in maybe a Java InputStream or a Java Reader. \n" +
						"Options are : \n" +
						"  :key-fn fn  \n" + 
						"      Single-argument function called on JSON property names; \n" +
						"      return value will replace the property names in the output. \n" +
						"      Default is 'identity', use 'keyword' to get keyword \n" +
						"      properties.")
					.examples(
						"(let [json (json/write-str {:a 100 :b 100})             \n" +
						"      data (bytebuf-from-string json :utf-8)            \n" +
						"      in (. :java.io.ByteArrayInputStream :new data)]   \n" +
						"  (str (json/slurp in)))                                  ")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertMinArity("json/slurp", args, 1);
	
				final VncVal val = args.first();
				
				if (val == Nil) {
					return Nil;
				}
				else {
					try {
						final Object in = Coerce.toVncJavaObject(args.first()).getDelegate();

						final VncHashMap options = VncHashMap.ofAll(args.slice(1));
						final VncVal key_fn = options.get(new VncKeyword("key-fn")); 

						final Function<VncVal,VncVal> keyFN = 
								key_fn == Nil 
									? null
									: (key) -> ((VncFunction)key_fn).apply(VncList.of(key));

						if (in instanceof InputStream) {
							return new VncJsonReader(JsonReader.from((InputStream)in), keyFN).read();
						}
						else if (in instanceof Reader) {
							return new VncJsonReader(JsonReader.from((Reader)in), keyFN).read();
						}
						else {
							throw new VncException(String.format(
									"Function 'json/slurp' does not allow %s as in",
									Types.getType(args.first())));
						}
					}
					catch(Exception ex) {
						throw new VncException("Failed to parse JSON", ex);
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
					.module("json")
					.arglists("(json/pretty-print s)")		
					.doc("Pretty prints a JSON string")
					.examples("(json/pretty-print (json/write-str {:a 100 :b 100}))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("pretty-print", args, 1);
	
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
						throw new VncException("Failed to pretty print JSON", ex);
					}
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};


	private static final String INDENT = "  ";
	
	
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap.Builder()
					.put("json/write-str",		write_str)
					.put("json/spit",			spit)
					.put("json/read-str",		read_str)
					.put("json/slurp",			slurp)
					.put("json/pretty-print",	pretty_print)
					.toMap();	
}
