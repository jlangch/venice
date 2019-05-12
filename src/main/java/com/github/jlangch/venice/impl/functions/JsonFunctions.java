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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncAtom;
import com.github.jlangch.venice.impl.types.VncBigDecimal;
import com.github.jlangch.venice.impl.types.VncByteBuffer;
import com.github.jlangch.venice.impl.types.VncConstant;
import com.github.jlangch.venice.impl.types.VncDouble;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncInteger;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncJavaList;
import com.github.jlangch.venice.impl.types.collections.VncJavaMap;
import com.github.jlangch.venice.impl.types.collections.VncJavaSet;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncSet;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.grack.nanojson.JsonAppendableWriter;
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
					
					final VncHashMap options = VncHashMap.ofAll(args.slice(1));
					final VncVal pretty = options.get(new VncKeyword("pretty")); 

					final StringBuilder sb = new StringBuilder();
					
					final JsonAppendableWriter writer = pretty == Constants.True
															? JsonWriter.indent(INDENT).on(sb)
															: JsonWriter.on(sb);
								
					new VncJsonWriter(writer).write(val);
					writer.done();
					
					// final Object javaVal = val.convertToJavaObject();
					// writer.value(javaVal).done();
					
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
					final VncHashMap options = VncHashMap.ofAll(args.slice(2));
					final VncVal pretty = options.get(new VncKeyword("pretty")); 

					if (out instanceof PrintStream) {
						final JsonAppendableWriter writer = pretty == Constants.True
																? JsonWriter.indent(INDENT).on((PrintStream)out)
																: JsonWriter.on((PrintStream)out);
																
						new VncJsonWriter(writer).write(val);
						writer.done();

						// final Object javaVal = val.convertToJavaObject();
						// writer.value(javaVal).done();
					}
					else if (out instanceof OutputStream) {
						final JsonAppendableWriter writer = pretty == Constants.True
																? JsonWriter.indent(INDENT).on((OutputStream)out)
																: JsonWriter.on((OutputStream)out);
																
						new VncJsonWriter(writer).write(val);
						writer.done();

						// final Object javaVal = val.convertToJavaObject();
						// writer.value(javaVal).done();
					}
					else if (out instanceof Writer) {
						final JsonAppendableWriter writer = pretty == Constants.True
																? JsonWriter.indent(INDENT).on((Writer)out)
																: JsonWriter.on((OutputStream)out);
																
						new VncJsonWriter(writer).write(val);
						writer.done();

						// final Object javaVal = val.convertToJavaObject();
						// writer.value(javaVal).done();
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

		
	private static class VncJsonWriter {
		public VncJsonWriter(final JsonAppendableWriter writer) {
			this.writer = writer;
		}

		public void write(final VncVal val) {
			write(null, val);
		}

		private void write(final String key, final VncVal val) {
			if (val == null) {
				write_null(key);
			}
			else if (Types.isVncConstant(val)) {
				write_VncConstant(key, (VncConstant)val);
			}
			else if (Types.isVncString(val)) {
				write_VncString(key, (VncString)val);
			}
			else if (Types.isVncInteger(val)) {
				write_VncInteger(key, (VncInteger)val);
			}
			else if (Types.isVncLong(val)) {
				write_VncLong(key, (VncLong)val);
			}
			else if (Types.isVncDouble(val)) {
				write_VncDouble(key, (VncDouble)val);
			}
			else if (Types.isVncBigDecimal(val)) {
				write_VncBigDecimal(key, (VncBigDecimal)val);
			}
			else if (Types.isVncKeyword(val)) {
				write_VncKeyword(key, (VncKeyword)val);
			}
			else if (Types.isVncSymbol(val)) {
				write_VncSymbol(key, (VncSymbol)val);
			}
			else if (Types.isVncList(val)) {
				write_VncList(key, (VncList)val);
			}
			else if (Types.isVncVector(val)) {
				write_VncVector(key, (VncVector)val);
			}
			else if (Types.isVncJavaList(val)) {
				write_VncJavaList(key, (VncJavaList)val);
			}
			else if (Types.isVncJavaObject(val)) {
				write_VncJavaObject(key, (VncJavaObject)val);
			}
			else if (Types.isVncJavaMap(val)) {
				write_VncJavaMap(key, (VncJavaMap)val);
			}
			else if (Types.isVncMap(val)) {
				write_VncMap(key, (VncMap)val);
			}
			else if (Types.isVncSet(val)) {
				write_VncSet(key, (VncSet)val);
			}
			else if (Types.isVncJavaSet(val)) {
				write_VncJavaSet(key, (VncJavaSet)val);
			}
			else if (Types.isVncByteBuffer(val)) {
				write_VncByteBuffer(key, (VncByteBuffer)val);
			}
			else if (Types.isVncAtom(val)) {
				write(key, ((VncAtom)val).deref()); // delegate to deref value
			}
			else {
				throw new VncException(String.format(
						"Json serialization error: the type %s can not be serialized",
						Types.getType(val)));
			}
		}

		private void write_null(final String key) {
			if (key == null) {
				writer.nul();
			}
			else {
				writer.nul(key);
			}
		}

		private void write_VncConstant(final String key, final VncConstant val) {
			if (key == null) {
				if (val == Constants.Nil) {
					writer.nul();
				}
				else if (val == Constants.True) {
					writer.value(true);
				}
				else if (val == Constants.False) {
					writer.value(false);
				}
			}
			else {
				if (val == Constants.Nil) {
					writer.nul(key);
				}
				else if (val == Constants.True) {
					writer.value(key, true);
				}
				else if (val == Constants.False) {
					writer.value(key, false);
				}
			}
		}

		private void write_VncString(final String key, final VncString val) {
			final String v = val.getValue();
			if (key == null) {
				writer.value(v);
			}
			else {
				writer.value(key, v);
			}
		}

		private void write_VncInteger(final String key, final VncInteger val) {
			final int v = val.getValue().intValue();
			if (key == null) {
				writer.value(v);
			}
			else {
				writer.value(key, v);
			}
		}

		private void write_VncLong(final String key, final VncLong val) {
			final long v = val.getValue().longValue();
			if (key == null) {
				writer.value(v);
			}
			else {
				writer.value(key, v);
			}
		}

		private void write_VncDouble(final String key, final VncDouble val) {
			final double v = val.getValue().doubleValue();
			if (key == null) {
				writer.value(v);
			}
			else {
				writer.value(key, v);
			}
		}

		private void write_VncBigDecimal(final String key, final VncBigDecimal val) {
			final String v = val.getValue().toString();
			if (key == null) {
				writer.value(v);
			}
			else {
				writer.value(key, v);
			}
		}

		private void write_VncKeyword(final String key, final VncKeyword val) {
			final String v = val.getValue();
			if (key == null) {
				writer.value(v);
			}
			else {
				writer.value(key, v);
			}
		}

		private void write_VncSymbol(final String key, final VncSymbol val) {
			final String v = val.getName();
			if (key == null) {
				writer.value(v);
			}
			else {
				writer.value(key, v);
			}
		}

		private void write_VncList(final String key, final VncList val) {
			array(key);
			val.forEach(v -> write(null, v));
			end();
		}

		private void write_VncVector(final String key, final VncVector val) {
			array(key);
			val.forEach(v -> write(null, v));
			end();
		}

		private void write_VncJavaList(final String key, final VncJavaList val) {
			writer.array(key, ((List<?>)val.getDelegate()));
		}

		private void write_VncSet(final String key, final VncSet val) {
			array(key);
			val.getSet().forEach(v -> write(null, v));
			end();
		}

		private void write_VncJavaSet(final String key, final VncJavaSet val) {
			writer.array(key, ((Set<?>)val.getDelegate()));
		}

		private void write_VncJavaMap(final String key, final VncJavaMap val) {
			if (key == null) {
				writer.object(((Map<?,?>)val.getDelegate()));
			}
			else {
				writer.object(key, ((Map<?,?>)val.getDelegate()));
			}
		}

		private void write_VncMap(final String key, final VncMap val) {
			object(key);

			final Map<VncVal,VncVal> map = val.getMap();
			for(Entry<VncVal,VncVal> e : map.entrySet()) {
				final VncVal k = e.getKey();
				final VncVal v = e.getValue();
				if (Types.isVncString(k)) {
					write(((VncString)k).getValue(), v);
				}
				else if (Types.isVncKeyword(k)) {
					write(((VncKeyword)k).getValue(), v);
				}
				else if (Types.isVncLong(k)) {
					write(((VncLong)k).getValue().toString(), v);
				}
				else {
					throw new VncException(String.format(
							"Json serialization error: the map key type %s can not be serialized",
							Types.getType(val)));
				}
			}
			
			end();
		}

		private void write_VncJavaObject(final String key, final VncJavaObject val) {
			final Object delegate = val.getDelegate();
			if (delegate instanceof LocalDate) {
				final String formatted = ((LocalDate)delegate).format(FMT_LOCAL_DATE);
				if (key == null) {
					writer.value(formatted);
				}
				else {
					writer.value(key, formatted);
				}
			}
			else if (delegate instanceof LocalDateTime) {
				final String formatted = ((LocalDateTime)delegate).format(FMT_LOCAL_DATE_TIME);
				if (key == null) {
					writer.value(formatted);
				}
				else {
					writer.value(key, formatted);
				}
			}
			else if (delegate instanceof ZonedDateTime) {
				final String formatted = ((ZonedDateTime)delegate).format(FMT_DATE_TIME);
				if (key == null) {
					writer.value(formatted);
				}
				else {
					writer.value(key, formatted);
				}
			}
			else {
				throw new VncException(String.format(
						"Json serialization error: the type %s can not be serialized",
						Types.getType(val)));
			}
		}

		private void write_VncByteBuffer(final String key, final VncByteBuffer val) {
			final String encoded = Base64.getEncoder().encodeToString(val.getValue().array());

			if (key == null) {
				writer.value(encoded);
			}
			else {
				writer.value(key, encoded);
			}
		}

		private void array(final String key) {
			if (key == null) {
				writer.array();
			}
			else {
				writer.array(key);
			}
		}

		private void object(final String key) {
			if (key == null) {
				writer.object();
			}
			else {
				writer.object(key);
			}
		}

		private void end() {
			writer.end();
		}


		private final JsonAppendableWriter writer;
	}

	private static final String INDENT = "  ";
	
	private static final DateTimeFormatter FMT_LOCAL_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
	private static final DateTimeFormatter FMT_LOCAL_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
	private static final DateTimeFormatter FMT_DATE_TIME = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
	
	
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
