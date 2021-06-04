/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2021 Venice
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.jlangch.venice.ValueException;
import com.github.jlangch.venice.VncException;


public class JavaImports implements Serializable {

	public JavaImports() {
		// from java.lang
		add(Throwable.class.getName());
		add(Exception.class.getName());
		add(RuntimeException.class.getName());
		add(NullPointerException.class.getName());
		add(IllegalArgumentException.class.getName());
		
		// from com.github.jlangch.venice
		add(VncException.class.getName());
		add(ValueException.class.getName());
	}
	
	
	/**
	 * Looks up a class name
	 * 
	 * @param simpleClassName A simple class name like 'Math'
	 * @return the class name e.g.: 'java.lang.Math' or <code>null</code> if not found
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
		validateNoDuplicate(clazz);
		imports.put(getSimpleClassname(clazz), clazz);
	}
	
	public void clear() {
		imports.clear();
	}
	
	public List<String> list() {
		final ArrayList<String> items = new ArrayList<>(imports.values());
		Collections.sort(items);	
		return items;
	}

	
	private String getSimpleClassname(final String clazz) {
		final int pos = clazz.lastIndexOf('.');
		return pos < 0 ? clazz : clazz.substring(pos+1);
	}

	private void validateNoDuplicate(final String clazz) {
		final String cn = getSimpleClassname(clazz);
		final String c = imports.get(cn);
		
		if (c != null && !c.equals(clazz)) {
			throw new VncException(String.format(
					"Failed to import class '%s' as '%s'. There is a '%s' already imported as '%s'.",
					clazz, cn, c, cn));
		}
	}
	
	
	private static final long serialVersionUID = 1784667662341909868L;

	private final Map<String,String> imports = new ConcurrentHashMap<>();
}
