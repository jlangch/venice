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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncDouble;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonWriter;


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
						"Options: :pretty true/false (defaults to false) ")
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
					final Object javaVal = val.convertToJavaObject();
					
					final VncHashMap options = VncHashMap.ofAll(args.slice(1));				
					final VncVal pretty = options.get(new VncKeyword("pretty")); 

					return pretty == Constants.True
							? new VncString(JsonWriter.indent(INDENT).string().value(javaVal).done())
							: new VncString(JsonWriter.string(javaVal));
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
						"out maybe a Java OutputStream or a Java PrintStream. \n" +
						"Options: :pretty true/false (defaults to false) ")
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
					final Object javaVal = val.convertToJavaObject();
					
					final VncHashMap options = VncHashMap.ofAll(args.slice(2));						
					final VncVal pretty = options.get(new VncKeyword("pretty")); 

					if (out instanceof OutputStream) {
						if (pretty == Constants.True) {
							JsonWriter.indent(INDENT).on((OutputStream)out).value(javaVal).done();
						}
						else {
							JsonWriter.on((OutputStream)out).value(javaVal).done();
						}
					}
					else if (out instanceof PrintStream) {
						if (pretty == Constants.True) {
							JsonWriter.indent(INDENT).on((PrintStream)out).value(javaVal).done();
						}
						else {
							JsonWriter.on((PrintStream)out).value(javaVal).done();
						}
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
					.arglists("(json/read-str s)")		
					.doc("Reads a JSON string and returns it as a venice datatype.")
					.examples("(json/read-str (json/write-str [{:a 100 :b 100}]))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("json/parse", args, 1);
	
				final VncVal val = args.first();
				
				if (val == Nil) {
					return Nil;
				}
				else {
					try {
						final VncString s = Coerce.toVncString(val);
						return convertToVncVal(JsonParser.any().from(s.getValue()));
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
					.arglists("(json/slurp in)")		
					.doc(
						"Slurps a JSON string from the input and returns it as a venice datatype.\n" +
						"in maybe a Java InputStream or a Java Reader.")
					.examples(
						"(let [json (json/write-str {:a 100 :b 100})             \n" +
						"      data (bytebuf-from-string json :utf-8)            \n" +
						"      in (. :java.io.ByteArrayInputStream :new data)]   \n" +
						"  (str (json/slurp in)))                                  ")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("json/slurp", args, 1);
	
				final VncVal val = args.first();
				
				if (val == Nil) {
					return Nil;
				}
				else {
					try {
						final Object in = Coerce.toVncJavaObject(args.first()).getDelegate();

						if (in instanceof InputStream) {
							return convertToVncVal(JsonParser.any().from((InputStream)in));
						}
						else if (in instanceof Reader) {
							return convertToVncVal(JsonParser.any().from((Reader)in));
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


	@SuppressWarnings("unchecked")
	private static VncVal convertToVncVal(final Object value) {
		if (value == null) {
			return Constants.Nil;
		}
		else if (value instanceof String) {
			return new VncString((String)value);
		}
		else if (value instanceof Number) {
			if (value instanceof Integer) {
				return new VncLong((Integer)value);
			}
			else if (value instanceof Long) {
				return new VncLong((Long)value);
			}
			else if (value instanceof Float) {
				return new VncDouble((Float)value);
			}
			else if (value instanceof Double) {
				return new VncDouble((Double)value);
			}
		}
		else if (value instanceof Boolean) {
			return ((Boolean)value).booleanValue() ? Constants.True : Constants.False;
		}
		else if (value instanceof List) {
			final List<VncVal> list = new ArrayList<>();
			for(Object o : (List<?>)value) {
				list.add(convertToVncVal(o));
			}
			return new VncList(list);
		}
		else if (value instanceof Map) {
			final HashMap<VncVal,VncVal> map = new HashMap<>();
			for(Map.Entry<Object, Object> o : ((Map<Object,Object>)value).entrySet()) {
				map.put(convertToVncVal(o.getKey()),convertToVncVal(o.getValue()));
			}
			return new VncHashMap(map);
		}
			
		throw new VncException("Failed to parse JSON. Unsupported Java type: " + value.getClass());
	}

	
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
