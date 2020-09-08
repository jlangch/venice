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
package com.github.jlangch.venice.impl;

import static com.github.jlangch.venice.impl.VeniceClasspath.getVeniceBasePath;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.util.ClassPathResource;


public class ModuleLoader {

	public static String loadModule(final String module) {
		if (!isValidModule(module)) {
			throw new VncException(String.format(
					"The Venice module '%s' does not exist",
					module));
		}
		
		final String name = module + ".venice";

		try {
			return modules.computeIfAbsent(
					name, 
					k -> new ClassPathResource(getVeniceBasePath() + k)
								.getResourceAsString("UTF-8"));
		}
		catch(Exception ex) {
			throw new VncException(String.format(
					"Failed to load Venice module '%s'", name), 
					ex);
		}
	}

	public static String loadClasspathFile(final String file) {
		// For security reasons just allow to load venice scripts!
		if (!file.endsWith(".venice")) {
			throw new VncException(String.format(
					"Must not load other than Venice (*.venice) resources from "
						+ "classpath. Resource: '%s'"));
		}
		
		try {
			return classpathFiles.computeIfAbsent(
					file, 
					k -> new ClassPathResource(file).getResourceAsString("UTF-8"));
		}
		catch(Exception ex) {
			throw new VncException(String.format(
					"Failed to load Venice classpath file '%s'", file), 
					ex);
		}
	}
		
	public static boolean isValidModule(final String module) {
		return VALID_MODULES.contains(module);
	}
	
	public static boolean isValidModule(final VncKeyword module) {
		return isValidModule(module.getValue());
	}
		
	
	
	private static final Map<String,String> modules = new ConcurrentHashMap<>();
	private static final Map<String,String> classpathFiles = new ConcurrentHashMap<>();
	
	public static final Set<String> VALID_MODULES = 
			Collections.unmodifiableSet(
				new HashSet<>(
					Arrays.asList(
							"ansi", 
							"app", 
							"benchmark", 
							"core", 
							"crypt", 
							"fam", 
							"geoip", 
							"gradle", 
							"http", 
							"jackson", 
							"java", 
							"kira", 
							"math", 
							"maven", 
							"mercator", 
							"parsatron",
							"repl-setup", 
							"ring", 
							"semver", 
							"shell", 
							"test",
							"tomcat", 
							"tomcat-util", 
							"tput",
							"webdav", 
							"xchart", 
							"xml")));
	
	public static final Set<VncKeyword> PRELOADED_MODULES = 
			Collections.unmodifiableSet(
				new HashSet<>(
					Arrays.asList(
							new VncKeyword("str"), 
							new VncKeyword("regex"), 
							new VncKeyword("time"), 
							new VncKeyword("io"), 
							new VncKeyword("json"), 
							new VncKeyword("pdf"), 
							new VncKeyword("cidr"), 
							new VncKeyword("csv"))));
}
