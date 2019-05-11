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
								
					write(writer, val);
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
						final JsonAppendableWriter writer = pretty == Constants.True
																? JsonWriter.indent(INDENT).on((OutputStream)out)
																: JsonWriter.on((OutputStream)out);
						writer.value(javaVal).done();
					}
					else if (out instanceof PrintStream) {
						final JsonAppendableWriter writer = pretty == Constants.True
																? JsonWriter.indent(INDENT).on((PrintStream)out)
																: JsonWriter.on((PrintStream)out);
						writer.value(javaVal).done();
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
	
	
	private static void write(final JsonAppendableWriter writer, final VncVal val) {
		if (Types.isVncConstant(val)) {
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
		else if (Types.isVncString(val)) {
			writer.value(((VncString)val).getValue());
		}
		else if (Types.isVncInteger(val)) {
			writer.value(((VncInteger)val).getValue().intValue());
		}
		else if (Types.isVncLong(val)) {
			writer.value(((VncLong)val).getValue().longValue());
		}
		else if (Types.isVncDouble(val)) {
			writer.value(((VncDouble)val).getValue().doubleValue());
		}
		else if (Types.isVncBigDecimal(val)) {
			writer.value(((VncBigDecimal)val).getValue().toString());
		}
		else if (Types.isVncKeyword(val)) {
			writer.value(((VncKeyword)val).getValue());
		}
		else if (Types.isVncSymbol(val)) {
			writer.value(((VncSymbol)val).getName());
		}
		else if (Types.isVncList(val)) {
			writer.array();
			((VncList)val).forEach(v -> write(writer, v));
			writer.end();
		}
		else if (Types.isVncVector(val)) {
			writer.array();
			((VncVector)val).forEach(v -> write(writer, v));
			writer.end();
		}
		else if (Types.isVncJavaList(val)) {
			writer.array(((List<?>)((VncJavaList)val).getDelegate()));
		}
		else if (Types.isVncJavaObject(val)) {
			final Object delegate = ((VncJavaObject)val).getDelegate();
			if (delegate instanceof LocalDate) {
				writer.value(((LocalDate)delegate).format(FMT_LOCAL_DATE));
			}
			else if (delegate instanceof LocalDateTime) {
				writer.value(((LocalDateTime)delegate).format(FMT_LOCAL_DATE_TIME));
			}
			else if (delegate instanceof ZonedDateTime) {
				writer.value(((ZonedDateTime)delegate).format(FMT_DATE_TIME));
			}
			else {
				throw new VncException(String.format(
						"Json serialization error: the type %s can not be serialized",
						Types.getType(val)));
			}
		}
		else if (Types.isVncJavaMap(val)) {
			writer.object(((Map<?,?>)((VncJavaMap)val).getDelegate()));
		}
		else if (Types.isVncMap(val)) {
			writer.object();
			final Map<VncVal,VncVal> map = ((VncMap)val).getMap();
			for(Entry<VncVal,VncVal> e : map.entrySet()) {
				final VncVal k = e.getKey();
				final VncVal v = e.getValue();
				if (Types.isVncString(k)) {
					write(writer, ((VncString)k).getValue(), v);
				}
				else if (Types.isVncKeyword(k)) {
					write(writer, ((VncKeyword)k).getValue(), v);
				}
				else if (Types.isVncLong(k)) {
					write(writer, ((VncLong)k).getValue().toString(), v);
				}
				else {
					throw new VncException(String.format(
							"Json serialization error: the map key type %s can not be serialized",
							Types.getType(val)));
				}
			}
			writer.end();
		}
		else if (Types.isVncSet(val)) {
			writer.array();
			((VncSet)val).getSet().forEach(v -> write(writer, v));
			writer.end();
		}
		else if (Types.isVncJavaSet(val)) {
			writer.array(((Set<?>)((VncJavaSet)val).getDelegate()));
		}
		else if (Types.isVncSet(val)) {
			writer.array();
			((VncSet)val).getSet().forEach(v -> write(writer, v));
			writer.end();
		}
		else if (Types.isVncByteBuffer(val)) {
			final byte[] buf = ((VncByteBuffer)val).getValue().array();
			writer.value(Base64.getEncoder().encodeToString(buf));
		}
		else {
			throw new VncException(String.format(
					"Json serialization error: the type %s can not be serialized",
					Types.getType(val)));
		}
	}

	private static void write(final JsonAppendableWriter writer, final String key, final VncVal val) {
		if (Types.isVncConstant(val)) {
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
		else if (Types.isVncString(val)) {
			writer.value(key, ((VncString)val).getValue());
		}
		else if (Types.isVncInteger(val)) {
			writer.value(key, ((VncInteger)val).getValue().intValue());
		}
		else if (Types.isVncLong(val)) {
			writer.value(key, ((VncLong)val).getValue().longValue());
		}
		else if (Types.isVncDouble(val)) {
			writer.value(key, ((VncDouble)val).getValue().doubleValue());
		}
		else if (Types.isVncBigDecimal(val)) {
			writer.value(key, ((VncBigDecimal)val).getValue().toString());
		}
		else if (Types.isVncKeyword(val)) {
			writer.value(key, ((VncKeyword)val).getValue());
		}
		else if (Types.isVncSymbol(val)) {
			writer.value(key, ((VncSymbol)val).getName());
		}
		else if (Types.isVncList(val)) {
			writer.array(key);
			((VncList)val).forEach(v -> write(writer, v));
			writer.end();
		}
		else if (Types.isVncVector(val)) {
			writer.array(key);
			((VncVector)val).forEach(v -> write(writer, v));
			writer.end();
		}
		else if (Types.isVncJavaList(val)) {
			writer.array(key, ((List<?>)((VncJavaList)val).getDelegate()));
		}
		else if (Types.isVncJavaObject(val)) {
			final Object delegate = ((VncJavaObject)val).getDelegate();
			if (delegate instanceof LocalDate) {
				writer.value(key, ((LocalDate)delegate).format(FMT_LOCAL_DATE));
			}
			else if (delegate instanceof LocalDateTime) {
				writer.value(key, ((LocalDateTime)delegate).format(FMT_LOCAL_DATE_TIME));
			}
			else if (delegate instanceof ZonedDateTime) {
				writer.value(key, ((ZonedDateTime)delegate).format(FMT_DATE_TIME));
			}
			else {
				throw new VncException(String.format(
						"Json serialization error: the type %s can not be serialized",
						Types.getType(val)));
			}
		}
		else if (Types.isVncJavaMap(val)) {
			writer.object(key, ((Map<?,?>)((VncJavaMap)val).getDelegate()));
		}
		else if (Types.isVncMap(val)) {
			writer.object(key);
			final Map<VncVal,VncVal> map = ((VncMap)val).getMap();
			for(Entry<VncVal,VncVal> e : map.entrySet()) {
				final VncVal k = e.getKey();
				final VncVal v = e.getValue();
				if (Types.isVncString(k)) {
					write(writer, ((VncString)k).getValue(), v);
				}
				else if (Types.isVncKeyword(k)) {
					write(writer, ((VncKeyword)k).getValue(), v);
				}
				else if (Types.isVncLong(k)) {
					write(writer, ((VncLong)k).getValue().toString(), v);
				}
				else {
					throw new VncException(String.format(
							"Json serialization error: the map key type %s can not be serialized",
							Types.getType(val)));
				}
			}
			writer.end();
		}
		else if (Types.isVncSet(val)) {
			writer.array(key);
			((VncSet)val).getSet().forEach(v -> write(writer, v));
			writer.end();
		}
		else if (Types.isVncJavaSet(val)) {
			writer.array(key, ((Set<?>)((VncJavaSet)val).getDelegate()));
		}
		else if (Types.isVncSet(val)) {
			writer.array(key);
			((VncSet)val).getSet().forEach(v -> write(writer, v));
			writer.end();
		}
		else if (Types.isVncByteBuffer(val)) {
			final byte[] buf = ((VncByteBuffer)val).getValue().array();
			writer.value(key, Base64.getEncoder().encodeToString(buf));
		}
		else {
			throw new VncException(String.format(
					"Json serialization error: the type %s can not be serialized",
					Types.getType(val)));
		}
	}

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
