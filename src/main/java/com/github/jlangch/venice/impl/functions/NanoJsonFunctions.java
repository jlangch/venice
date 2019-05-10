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

import java.util.Map;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.javainterop.JavaInteropUtil;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonWriter;


public class NanoJsonFunctions {

	///////////////////////////////////////////////////////////////////////////
	// JSON
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction to_json = 
		new VncFunction(
				"njson/to-json", 
				VncFunction
					.meta()
					.module("njson")
					.arglists("(njson/to-json val)")		
					.doc("Converts the val to JSON")
					.examples("(njson/to-json {:a 100 :b 100})")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("njson/to-json", args, 1);
	
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
				"njson/to-pretty-json", 
				VncFunction
					.meta()
					.module("njson")
					.arglists("(njson/to-pretty-json val)")		
					.doc("Converts the val to pretty printed JSON")
					.examples("(njson/to-pretty-json {:a 100 :b 100})")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("njson/to-pretty-json", args, 1);
	
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
				"njson/parse", 
				VncFunction
					.meta()
					.module("njson")
					.arglists("(njson/parse s)")		
					.doc("Parses a JSON string")
					.examples("(njson/parse (njson/to-json [{:a 100 :b 100}]))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("njson/parse", args, 1);
	
				final VncVal val = args.first();
				
				if (val == Nil) {
					return Nil;
				}
				else {
					try {
						final VncString s = Coerce.toVncString(val);
						return JavaInteropUtil.convertToVncVal(JsonParser.any().from(s.getValue()));
					}
					catch(Exception ex) {
						throw new VncException("Failed to parse JSON", ex);
					}
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

		
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap.Builder()
					.put("njson/to-json",			to_json)
					.put("njson/to-pretty-json",	to_pretty_json)
					.put("njson/parse",				parse)
					.toMap();	
}
