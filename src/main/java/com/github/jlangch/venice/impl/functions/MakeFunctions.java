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
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;

import com.github.jlangch.venice.Version;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncMapEntry;
import com.github.jlangch.venice.impl.types.collections.VncOrderedMap;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.Zipper;


public class MakeFunctions {

	public static VncFunction make_app = 
		new VncFunction(
				"make-app", 
				VncFunction
					.meta()
					.arglists("(make-app name main-file file-map dest-dir)")		
					.doc("Creates a Venice application archive.")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("make-app", args, 4);

				try {
					final VncString name = Coerce.toVncString(args.first());

					final VncString mainFileName = Coerce.toVncString(args.second());

					final VncMap files = Coerce.toVncMap(args.third());
					
					final File destDir = IOFunctions.convertToFile(
											args.fourth(),
											"Function 'make-app' dest-dir is not a file");

					validateDestDirExists(destDir);
					
					final Map<String, Object> zipEntries = new LinkedHashMap<>();
					
					final VncString manifest = buildManifest(name, mainFileName);
											
					zipEntries.put("MANIFEST.MF", manifest.getValue().getBytes("utf-8"));
									
					for(VncMapEntry e : files.entries()) {
						final String path = Coerce.toVncString(e.getKey()).getValue();
						final File file = IOFunctions.convertToFile(
												e.getValue(),
												String.format(
														"Function 'make-app' not a file %s -> %s",
														path,
														Types.getType(e.getValue())));

						validateSourceFileExists(file);

						zipEntries.put(
							makeVeniceFileName(path), 
							Files.readAllBytes(file.toPath()));
					}
					
					final OpenOption[] openOptions = new OpenOption[] {
															StandardOpenOption.CREATE,
															StandardOpenOption.WRITE,
															StandardOpenOption.TRUNCATE_EXISTING};

					final byte[] data = Zipper.zip(zipEntries);
					
					Files.write(new File(destDir, name.getValue() + ".zip").getAbsoluteFile().toPath(), data, openOptions);
					
					return Nil;
				} 
				catch (VncException ex) {
					throw ex;
				}
				catch (Exception ex) {
					throw new VncException("Failed to build Venice app", ex);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
		
		
	private static VncString buildManifest(
		final VncString name,
		final VncString mainFileName
	) {
		return Coerce.toVncString(
				JsonFunctions.write_str.apply(
				  VncList.of(
					VncOrderedMap.of(
						new VncString("manifest-version"), new VncString("1.0"),
						new VncString("app-name"), name,
						new VncString("main-file"), mainFileName),
						new VncString("created-by"), new VncString("Venice " + Version.VERSION))));
	}
	
	private static String makeVeniceFileName(final String path) {
		if (path.endsWith(".venice")) {
			return path;
		}
		else {
			return path + ".venice";
		}
	}
	
	private static void validateDestDirExists(final File dir) {
		if (!dir.isDirectory()) {
			throw new VncException(String.format(
					"Function 'make-app' dest-dir '%s' does not exist.",
					dir.getPath()));
		}
	}
	
	private static void validateSourceFileExists(final File file) {
		if (!file.isFile()) {
			throw new VncException(String.format(
					"Function 'make-app' file '%s' does not exist.",
					file.getPath()));
		}
	}
	
		
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap
					.Builder()
					.add(make_app)
					.toMap();
}
