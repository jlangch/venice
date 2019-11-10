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

import java.util.HashMap;
import java.util.Map;

import com.github.jlangch.venice.impl.types.VncSymbol;


public class AutoGenSym {
	
	public AutoGenSym() {
	}

	public void enterSyntaxQuote() {
		insideSyntaxQuote = true;
		autogenSymMap.clear();
	}
	
	public void leaveSyntaxQuote() {
		insideSyntaxQuote = false;
		autogenSymMap.clear();
	}
	
	public boolean isWithinSyntaxQuote() {
		return insideSyntaxQuote;
	}

	public boolean isAutoGenSymbol(final VncSymbol sym) {
		final String name = sym.getName();
		return name.length() > 1 && name.endsWith("#");
	}
	
	public VncSymbol lookup(final VncSymbol sym) {
		return isAutoGenSymbol(sym)
				? autogenSymMap.computeIfAbsent(sym, s -> genAutoSym(s))
				: sym;
	}

	private VncSymbol genAutoSym(final VncSymbol sym) {
		return GenSym.generateAutoSym(stripTrailingHash(sym.getName()));
	}
	
	private String stripTrailingHash(final String s) {
		return s.substring(0, s.length()-1);
	}
	
	
	private boolean insideSyntaxQuote;
	private final Map<VncSymbol,VncSymbol> autogenSymMap = new HashMap<>();
}
