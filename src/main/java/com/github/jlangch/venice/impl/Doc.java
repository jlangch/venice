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
package com.github.jlangch.venice.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;


public class Doc {
	
	public static String getDoc(final String name) {
		
		// function?
		final VncFunction func = functions.get(name);
		if (func != null) {
			final StringBuilder sb =  new StringBuilder();
			sb.append(func.getArgLists().getList().stream().map(s -> toString(s)).collect(Collectors.joining(", ")));
			sb.append("\n");
			sb.append(toString(func.getDoc()));
			return sb.toString();			
		}
		
		// macro?
		final MacroDef macro = macros.get(name);
		if (macro != null) {
			final StringBuilder sb =  new StringBuilder();
			sb.append(macro.getSignatures().stream().collect(Collectors.joining(", ")));
			sb.append("\n");
			sb.append(macro.getDescription());
			return sb.toString();			
		}
		
		return "<no documentation available>";			
	}
	
	private static String toString(final VncVal val) {
		return val == Constants.Nil ? "" : ((VncString)val).getValue();
	}
	
	
	private static final Map<String,VncFunction> functions = new HashMap<>();
	private static final Map<String,MacroDef> macros = new HashMap<>();
	
	static {
		CoreFunctions.ns.values().forEach(f -> functions.put(((VncFunction)f).getName(), (VncFunction)f));
		CoreMacroDefs.getMacros().forEach(m -> macros.put(m.getName(), m));
	}
}
