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

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncMapEntry;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.Zipper;


public class MakeFunctions {

	public static VncFunction make_app = 
		new VncFunction(
				"make-app", 
				VncFunction
					.meta()
					.arglists("(make-app name main-file file-map dest-file)")		
					.doc("Creates a Venice application archive.")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("make-app", args, 4);

				try {
					final String name = Coerce.toVncString(args.first()).getValue();

					final String mainFileName = Coerce.toVncString(args.second()).getValue();

					final VncMap files = Coerce.toVncMap(args.third());
					
					final File destFile = IOFunctions.convertToFile(
											args.fourth(),
											"Function 'make-app' dest-file is not a file");
					
					final Map<String, Object> zipEntries = new LinkedHashMap<>();
					
					final String manifest = String.format(
												"{ \n" +
												"   \"version\" : \"%s\" \n" +
												"   \"app-name\" : \"%s\" \n" +
												"   \"main-file\" : \"%s\" \n" +
												"}",
												"1.0",
												name,
												mainFileName);
											
					zipEntries.put("MANIFEST.MF", manifest.getBytes("utf-8"));
									
					for(VncMapEntry e : files.entries()) {
						final String path = Coerce.toVncString(e.getKey()).getValue();
						final File file = IOFunctions.convertToFile(
												e.getValue(),
												String.format(
														"Function 'make-app' not a file %s -> %s",
														path,
														Types.getType(e.getValue())));
						
						zipEntries.put(path, Files.readAllBytes(file.toPath()));
					}
					
					final OpenOption[] openOptions = new OpenOption[] {
															StandardOpenOption.CREATE,
															StandardOpenOption.WRITE,
															StandardOpenOption.TRUNCATE_EXISTING};

					final byte[] data = Zipper.zip(zipEntries);
					
					Files.write(destFile.toPath(), data, openOptions);
					
					return Nil;
				} 
				catch (Exception ex) {
					throw new VncException("Failed to build Venice app", ex);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
		
	
		
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap
					.Builder()
					.add(make_app)
					.toMap();
}
