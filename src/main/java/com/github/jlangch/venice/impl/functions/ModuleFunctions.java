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

import java.io.File;
import java.nio.file.Files;
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
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;


public class ModuleFunctions {

	///////////////////////////////////////////////////////////////////////////
	// Module load functions
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction loadModule = 
		new VncFunction(
				"*load-module", 
				VncFunction
					.meta()
					.arglists("(*load-module name)")		
					.doc("Loads a Venice extension module.")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				try {	
					assertArity("*load-module", args, 1);
					
					final String name = Coerce.toVncString(CoreFunctions.name.apply(args)).getValue();
					return new VncString(ModuleLoader.loadModule(name));
				} 
				catch (Exception ex) {
					throw new VncException(ex.getMessage(), ex);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction loadClasspathFile = 
		new VncFunction(
				"*load-classpath-file",
				VncFunction
					.meta()
					.arglists("(*load-classpath-file name)")		
					.doc("Loads a Venice file from the classpath.")
					.build()
		) {
			public VncVal apply(final VncList args) {
				try {	
					assertArity("*load-classpath-file", args, 1);
					
					final String file = suffixWithVeniceFileExt(name(args.first()));
					
					if (file != null) {
						final String res = ModuleLoader.loadClasspathFile(file);
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
		
	public static VncFunction loadFile = 
		new VncFunction(
				"*load-file",
				VncFunction
					.meta()
					.arglists("(*load-file file)")		
					.doc("Loads a Venice file.")
					.build()
		) {
			public VncVal apply(final VncList args) {
				try {	
					assertArity("*load-file", args, 1);
					
					final String file = suffixWithVeniceFileExt(name(args.first()));
					
					if (file != null) {
						try {
							final byte[] data = Files.readAllBytes(new File(file).toPath());
	
							return new VncString(new String(data, "utf-8"));
						}
						catch (Exception ex) {
							throw new VncException("Failed to load Venice file '" + file + "'", ex);
						}
					}
					else {
						return Nil;
					}
				} 
				catch (VncException ex) {
					throw ex;
				}
				catch (Exception ex) {
					throw new VncException(ex);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};


	private static String name(final VncVal val) {
		if (Types.isVncString(val)) {
			return ((VncString)val).getValue();
		}
		else if (Types.isVncKeyword(val)) {
			return ((VncKeyword)val).getValue();
		}
		else if (Types.isVncSymbol(val)) {
			return ((VncSymbol)val).getName();
		}
		else {
			return null;
		}
	}
	
	private static String suffixWithVeniceFileExt(final String s) {
		return s.endsWith(".venice") ? s : s + ".venice";
	}
		
	
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap
					.Builder()
					.add(loadModule)
					.add(loadFile)
					.add(loadClasspathFile)
					.toMap();
}
