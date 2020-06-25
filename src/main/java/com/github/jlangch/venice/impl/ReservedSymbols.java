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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.github.jlangch.venice.impl.types.VncSymbol;


public class ReservedSymbols {

	public static boolean isReserved(final VncSymbol symbol) {
		return symbol != null && reserved.contains(symbol.getName());
	}

	public static boolean isSpecialForm(final VncSymbol symbol) {
		return symbol != null && sepcial_forms.contains(symbol.getName());
	}

	public static boolean isSpecialForm(final String name) {
		return name != null && sepcial_forms.contains(name);
	}


	public static void validateNotReservedSymbol(final VncSymbol symbol) {
		if (symbol != null && isReserved(symbol)) {
			throw new SecurityException(
					String.format(
							"Reserved symbol '%s'. Redefinition is not allowed.", 
							symbol.getName()));
		}
	}

	private static<T> HashSet<T> merge(final Set<T> s1, final Collection<T> s2) {
		final HashSet<T> s = new HashSet<>(s1);
		s.addAll(s2);
		return s;
	}
	
	private static final Set<String> sepcial_forms = 
			new HashSet<>(Arrays.asList(
					"def",
					"defonce",
					"def-dynamic",
					"defmacro",
					"doc",
					"ns",
					"eval",
					"let",
					"binding",
					"loop",
					"recur",
					"quote",
					"quasiquote",
					"macroexpand",
					"try",
					"try-with",
					"import",
					"imports",
					"do",
					"dorun",
					"dobench",
					"if",
					"fn",
					"resolve",
					"set!",
					"prof",
					"var-get",
					"defmulti",
					"defmethod",
					
					"deftype",
					"defprotocol",
					"defrecord"));
	
	private static final Set<String> reserved = 
			merge(
				sepcial_forms, 
				Arrays.asList(
					".",
					"proxify",
					"*in*",
					"*out*",
					"*err*",
					"*version*",
					"*newline*",
					"*ns*",
					"*loaded-modules*",
					"*loaded-files*",
					"*load-path*",
					"*run-mode*",
					"*macroexpand-on-load*",
					"*ansi-term*",
					"*app-name*",
					"*app-archive*",
					"*ARGV*"));
}
