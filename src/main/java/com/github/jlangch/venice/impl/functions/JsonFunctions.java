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
import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncDouble;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonWriter;


public class JsonFunctions {

	///////////////////////////////////////////////////////////////////////////
	// JSON
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction to_json = 
		new VncFunction(
				"json/to-json", 
				VncFunction
					.meta()
					.module("json")
					.arglists("(json/to-json val)")		
					.doc("Converts the val to JSON")
					.examples("(json/to-json {:a 100 :b 100})")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("json/to-json", args, 1);
	
				final VncVal val = args.first();
				
				if (val == Nil) {
					return Nil;
				}
				else {
					final String json = JsonWriter.string(val.convertToJavaObject());
					return new VncString(json);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction to_pretty_json = 
		new VncFunction(
				"json/to-pretty-json", 
				VncFunction
					.meta()
					.module("json")
					.arglists("(json/to-pretty-json val)")		
					.doc("Converts the val to pretty printed JSON")
					.examples("(json/to-pretty-json {:a 100 :b 100})")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("json/to-pretty-json", args, 1);
	
				final VncVal val = args.first();
				
				if (val == Nil) {
					return Nil;
				}
				else {
					final String json = JsonWriter.indent("  ")
												  .string()
												  .value(val.convertToJavaObject())
												  .done();
					return new VncString(json);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction parse = 
		new VncFunction(
				"json/parse", 
				VncFunction
					.meta()
					.module("json")
					.arglists("(json/parse s)")		
					.doc("Parses a JSON string")
					.examples("(json/parse (json/to-json [{:a 100 :b 100}]))")
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
					.examples("(json/pretty-print (json/to-json {:a 100 :b 100}))")
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

	
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap.Builder()
					.put("json/to-json",		to_json)
					.put("json/to-pretty-json",	to_pretty_json)
					.put("json/parse",			parse)
					.put("json/pretty-print",	pretty_print)
					.toMap();	
}
