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
package com.github.jlangch.venice.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.github.jlangch.venice.VncException;


public class Modules {

	public static boolean isReserved(final String moduleName) {
		return moduleName != null && reserved.contains(moduleName);
	}
	
	public static void validateModuleName(final String moduleName) {
//		if (isReserved(moduleName)) {
//			throw new VncException(String.format("Reserved module name '%s'", moduleName));
//		}
	}
	
	public static void validateFileName(final String fileName) {
		validateModuleName(fileNameToModule(fileName));
	}

	public static String fileNameToModule(final String fileName) {
		return fileName != null && fileName.endsWith(".venice") 
					? fileName.substring(0, fileName.length() - 7) 
					: fileName;
	}

	private static final Set<String> reserved = 
			new HashSet<>(Arrays.asList("core", "time", "str", "io", "shell", "system", "venice"));
}
