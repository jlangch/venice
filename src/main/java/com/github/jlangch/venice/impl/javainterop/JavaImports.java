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
package com.github.jlangch.venice.impl.javainterop;

import java.io.Serializable;
import java.util.HashMap;

import com.github.jlangch.venice.impl.ValueException;


public class JavaImports implements Serializable {

	public JavaImports() {
		add(ValueException.class.getName());
	}
	
	
	/**
	 * Looks up a class name
	 * 
	 * @param simpleClassName A simple class name like 'Math'
	 * @return the class name e.g.: 'java.lang.Math' or <tt>null</tt> if not found
	 */
	public String lookupClassName(final String simpleClassName) {
		return imports.get(simpleClassName);
	}

	/**
	 * Resolves a class name.
	 * 
	 * @param className A simple class name like 'Math' or a class
	 *                  'java.lang.Math'
	 * @return the mapped class 'Math' -&gt; 'java.lang.Math' or the passed 
	 *         value if a mapping does nor exist 
	 */
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
	
	
	private static final long serialVersionUID = 1784667662341909868L;

	private final HashMap<String,String> imports = new HashMap<>();
}
