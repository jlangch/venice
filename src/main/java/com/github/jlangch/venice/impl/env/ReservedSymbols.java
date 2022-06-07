/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2022 Venice
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
package com.github.jlangch.venice.impl.env;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.specialforms.SpecialForms;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.util.CallFrame;
import com.github.jlangch.venice.impl.util.WithCallStack;


public class ReservedSymbols {

	public static boolean isReserved(final VncSymbol symbol) {
		return symbol != null && reserved.contains(symbol.getName());
	}

	public static boolean isSpecialForm(final VncSymbol symbol) {
		return symbol != null && SpecialForms.isSpecialForm(symbol.getName());
	}

	public static void validateNotReservedSymbol(final VncSymbol symbol) {
		if (symbol != null) {
			if (isSpecialForm(symbol)) {
				try (WithCallStack cs = new WithCallStack(new CallFrame(symbol.getQualifiedName(), symbol.getMeta()))) {
					throw new VncException(
							String.format(
									"The special form name '%s' can not be used a symbol.", 
									symbol.getName()));
				}
			}
			if (isReserved(symbol)) {
				try (WithCallStack cs = new WithCallStack(new CallFrame(symbol.getQualifiedName(), symbol.getMeta()))) {
					throw new VncException(
							String.format(
									"Reserved name '%s' can not be used a symbol.", 
									symbol.getName()));
				}
			}
		}
	}

	private static<T> HashSet<T> merge(final Set<T> s1, final Collection<T> s2) {
		final HashSet<T> s = new HashSet<>(s1);
		s.addAll(s2);
		return s;
	}
		
	private static final Set<String> reserved = 
			merge(
				SpecialForms.FORMS, 
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
					"*run-mode*",
					"*macroexpand-on-load*",
					"*ansi-term*",
					"*app-name*",
					"*app-archive*",
					"*ARGV*",
					"*REPL*",
					"*repl-color-theme*"));
}
