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

import com.github.jlangch.venice.impl.types.VncSymbol;


public class ReservedSymbols {

	public static boolean isReserved(final VncSymbol symbol) {
		return symbol != null && reserved.contains(symbol.getName());
	}

	public static void validate(final VncSymbol symbol) {
		if (symbol != null && reserved.contains(symbol.getName())) {
			throw new SecurityException(
					String.format(
							"Reserved symbol '%s'. Redefinition is not allowed.", 
							symbol.getName()));
		}
	}

	
	
	private static final Set<String> reserved = 
			new HashSet<>(Arrays.asList(
					"def",
					"defonce",
					"def-dynamic",
					"doc",
					"eval",
					"let",
					"binding",
					"loop",
					"recur",
					"quote",
					"quasiquote",
					"defmacro",
					"macroexpand",
					"try",
					"try-with",
					"import",
					"do",
					"if",
					"fn",
					"prof",
					"perf",
					"resolve",
					"defmulti",
					"defmethod",
					"deftype",
					"defrecord",
					
					".",
					"proxify",
					"*version*",
					"*newline*"));
}
