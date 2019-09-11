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
package com.github.jlangch.venice;

import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class Version {

	public static String getVersionFromManifest() {
		try {
			final Class<?> clazz = Version.class;
			final String className = clazz.getSimpleName() + ".class";
			final String classPath = clazz.getResource(className).toString();
			if (!classPath.startsWith("jar")) {	  
				return null; // Class not from JAR
			}
			
			final String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) 
											+ "/META-INF/MANIFEST.MF";
			final Manifest manifest = new Manifest(new URL(manifestPath).openStream());
			final Attributes attr = manifest.getMainAttributes();
			return attr.getValue("Implementation-Version");		
		}
		catch(Exception ex) {
			return null;
		}
	}
	
	
	public static final String VERSION = "1.7.2";
}
