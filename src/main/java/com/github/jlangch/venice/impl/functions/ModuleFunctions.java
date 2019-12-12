/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2020 Venice
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
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.ModuleLoader;
import com.github.jlangch.venice.impl.javainterop.JavaInterop;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.javainterop.IInterceptor;


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
				assertArity("*load-module", args, 1);

				try {
					final String name = Coerce.toVncString(CoreFunctions.name.apply(args)).getValue();
					
					// sandbox: validate module load
					final IInterceptor interceptor = JavaInterop.getInterceptor();
					interceptor.validateLoadModule(name);
					
					return new VncString(ModuleLoader.loadModule(name));
				} 
				catch (Exception ex) {
					throw new VncException("Failed to load Venice module", ex);
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
				assertArity("*load-classpath-file", args, 1);

				try {		
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
					throw new VncException("Failed to load Venice classpath file", ex);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncFunction loadFile = 
		new VncFunction(
				"*load-file",
				VncFunction
					.meta()
					.arglists("(*load-file file load-paths)")		
					.doc("Loads a Venice file.")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("*load-file", args, 1, 2);
				
				try {	
					final String f = suffixWithVeniceFileExt(name(args.first()));
					if (f != null) {
						final File file = new File(f);
						
						final VncList loadPaths = args.size() == 2 && Types.isVncList(args.second())
														? (VncList)args.second() 
														: new VncList();

						if (file != null) {
							if (loadPaths.isEmpty()) {
								final VncVal code = load(file.toPath());
								if (code != Nil) {
									return code;
								}
							}
							else if (file.isAbsolute()) {
								throw new VncException(
											"Failed to load Venice file '" + file + "'. " +
											"Absolute files cannot be used with a load-path!");
							}
							else {
								for(VncVal p : loadPaths.getList()) {
									if (p != Nil) {
										final String loadPath = name(p);
										if (loadPath.endsWith(".zip")) {
											// load "file" from zip
											try (FileSystem zipfs = mountZIP(new File(loadPath))) {
												final VncVal code = load(zipfs.getPath(file.getPath()));
												if (code != Nil) {
													return code;
												}
											}
										}
										else {
											final File dir = new File(loadPath).getAbsoluteFile();
											final File fl = new File(dir, file.getPath());
											if (fl.isFile()) {
												if (fl.getCanonicalPath().startsWith(dir.getCanonicalPath())) {
													// Prevent accessing files outside the load-path.
													// E.g.: ../../coffee
													final VncVal code = load(new File(dir, file.getPath()).toPath());
													if (code != Nil) {
														return code;
													}
												}
											}
										}
									}
								}
							}
	
							throw new VncException("Failed to load Venice file '" + file + "'. File not found!");
						}
					}
					
					return null;
				} 
				catch (VncException ex) {
					throw ex;
				}
				catch (Exception ex) {
					throw new VncException("Failed to load Venice file", ex);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};


	private static VncVal load(final Path path) {
		try {
			final byte[] data = Files.readAllBytes(path);

			return new VncString(new String(data, "utf-8"));
		}
		catch (Exception ex) {
			return Nil;
		}
	}
	
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
		return s == null ? null : (s.endsWith(".venice") ? s : s + ".venice");
	}
	
	private static FileSystem mountZIP(final File zip) throws IOException {
		return FileSystems.newFileSystem(
				zip.toPath(),
				ModuleFunctions.class.getClassLoader());
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
