/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2018 Venice
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
package org.venice.impl.javainterop;

import java.util.HashMap;


public class JavaImports {

	public JavaImports() {		
	}
	
	
	public String lookupClassName(final String className) {
		return imports.get(className);
	}

	public String resolveClassName(final String className) {
		final String cn = imports.get(className);
		return cn == null ? className : cn;
	}

	public void add(final String clazz) {
		final int pos = clazz.lastIndexOf('.');
		if (pos < 0) {
			imports.put(clazz, clazz);
		}
		else {
			imports.put(clazz.substring(pos+1), clazz);
		}
	}
	
	public void clear() {
		imports.clear();
	}
	
	
	private final HashMap<String,String> imports = new HashMap<>();
}
