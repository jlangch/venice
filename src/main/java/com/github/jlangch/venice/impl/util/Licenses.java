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
package com.github.jlangch.venice.impl.util;

import static com.github.jlangch.venice.impl.VeniceClasspath.getVeniceBasePath;

import java.util.LinkedHashMap;
import java.util.Map;

import com.github.jlangch.venice.impl.util.io.ClassPathResource;


public class Licenses {

	public static Map<String,String> lics() {
		final Map<String,String> lics = new LinkedHashMap<>();
			
		lics.put("Venice", loadLicense("LICENSE-Vavr.txt"));
		lics.put("3rd Party: VAVR", loadLicense("LICENSE-Vavr.txt"));
		lics.put("3rd Party: JLine3", loadLicense("LICENSE-JLine3.txt"));
		lics.put("3rd Party: nanojson", loadLicense("LICENSE-nanojson.txt"));
		lics.put("3rd Party: Open Font", loadLicense("LICENSE-OFL.txt"));
		
		return lics;
	}

	
	private static String loadLicense(final String name) {
		final String path = getVeniceBasePath() + "licenses/" + name;		
		return new ClassPathResource(path).getResourceAsString("UTF-8");
	}
}
