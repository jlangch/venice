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

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.ModuleLoader;
import com.github.jlangch.venice.impl.javainterop.JavaInterop;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncByteBuffer;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ZipFileSystemUtil;
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
				assertArity(args, 1);

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
				assertArity(args, 1);

				JavaInterop.getInterceptor().validateVeniceFunction("*load-classpath-file");

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
		
	public static VncFunction loadResource = 
		new VncFunction(
				"*load-resource",
				VncFunction
					.meta()
					.arglists("(*load-resource file load-paths & options)")		
					.doc(
						"Loads a resource from the given load-paths. Returns a string, a bytebuffer " +
						"or nil if the file does not exist. \n\n" +
						"Options: \n" +
						"  :binary true/false - e.g :binary true, defaults to true \n" +
						"  :encoding enc - e.g :encoding :utf-8, defaults to :utf-8")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertMinArity(args, 2);
				
				try {
					final VncHashMap options = VncHashMap.ofAll(args.rest().rest());
					final boolean binary = VncBoolean.isTrue(options.get(new VncKeyword("binary")));
					final String encoding = encoding(options.get(new VncKeyword("encoding")));
					
					final File file = new File(name(args.first()));					
					final VncList loadPaths = Coerce.toVncList(args.second());

					if (loadPaths.isEmpty()) {
						final VncVal data = load(file.toPath());
						return data == Nil || binary ? data : convertToString(data, encoding);
					}
					else {
						final VncVal data = loadPaths.getList()
											         .stream()
											         .map(p -> name(p))
											         .map(p -> loadFile(p, file))
											         .filter(d -> d != Nil)
											         .findFirst()
											         .orElse(Nil);
						if (data == Nil) {
							throw new VncException("Failed to load Venice file: " + file);
						}
						else {
							return binary ? data : convertToString(data, encoding);
						}
					}
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

	private static VncVal loadFile(
			final String loadPath, 
			final File file
	) {
		return loadPath.endsWith(".zip")
					? loadFileFromZip(new File(loadPath), file)
					: loadFileFromDir(new File(loadPath), file);
	}

	private static VncVal loadFileFromZip(
			final File zip, 
			final File file
	) {
		if (zip.exists()) {
			try {
				return ZipFileSystemUtil.loadBinaryFileFromZip(zip, file);
			}
			catch(Exception ex) {
				return Nil;
			}
		}
		
		return Nil;
	}
	
	private static VncVal loadFileFromDir(final File path, final File file) {
		try {
			if (isFileWithinDirectory(path, file)) {
				final File dir = path.getAbsoluteFile();
				return load(new File(dir, file.getPath()).toPath());
			}
			else {
				return Nil;
			}
		}
		catch (Exception ex) {
			throw new VncException(String.format("Failed to load file '%s'", file.getPath()), ex);
		}
	}

	private static boolean isFileWithinDirectory(final File path, final File file)  throws IOException {
		final File dir = path.getAbsoluteFile();
		if (dir.isDirectory()) {
			final File fl = new File(dir, file.getPath());
			if (fl.isFile()) {
				if (fl.getCanonicalPath().startsWith(dir.getCanonicalPath())) {
					// Prevent accessing files outside the load-path.
					// E.g.: ../../coffee
					return true;
				}
			}
		}
		
		return false;
	}
	
	private static VncVal load(final Path path) {
		try {
			return new VncByteBuffer(Files.readAllBytes(path));
		}
		catch (Exception ex) {
			return Nil;
		}
	}

	private static VncVal convertToString(final VncVal binary, final String encoding) {
		try {
			return binary == Nil
					? Nil
					: new VncString(new String(((VncByteBuffer)binary).getBytes(), encoding));
		}
		catch (Exception ex) {
			return Nil;
		}
	}

	private static String encoding(final VncVal enc) {
		return enc == Nil
				? "utf-8"
				: Types.isVncKeyword(enc)
					? Coerce.toVncKeyword(enc).getValue()
					: Coerce.toVncString(enc).getValue();
	}
		
	
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap
					.Builder()
					.add(loadModule)
					.add(loadResource)
					.add(loadClasspathFile)
					.toMap();
}
