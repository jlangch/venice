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
import com.github.jlangch.venice.impl.ModuleLoader;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Types;


public class ModuleLoadFunctions {

	///////////////////////////////////////////////////////////////////////////
	// Module load functions
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction loadCoreModule = 
			new VncFunction(
					"load-core-module", 
					VncFunction
						.meta()
						.module("core")
						.arglists("(load-core-module name)")		
						.doc("Loads a Venice extension module.")
						.build()
			) {	
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
		
			    private static final long serialVersionUID = -1848883965231344442L;
			};

	
	public static VncFunction loadClasspathVenice = 
		new VncFunction(
				"load-classpath-venice",
				VncFunction
					.meta()
					.module("core")
					.build()
		) {
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
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

		
		
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap.Builder()
					.put("load-core-module",		loadCoreModule)
					.put("load-classpath-venice",	loadClasspathVenice)
					.toMap();
}
