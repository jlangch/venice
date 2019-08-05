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

import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.concurrent.ThreadLocalMap;


public class Namespace {

	public static String getNamespace(final String name) {
		final int pos = name.indexOf("/");
		return pos < 1 ? null : name.substring(0, pos);
	}

	public static boolean isQualified(final String name) {
		return name.indexOf("/") >= 1;
	}

	public static boolean isQualified(final VncSymbol sym) {
		return isQualified(sym.getName());
	}

	public static boolean on() {
		return false;
	}

	public static VncSymbol getCurrentNS() {
		return ThreadLocalMap.getCurrNS();
	}
	
	public static void setCurrentNS(final VncSymbol ns) {
		ThreadLocalMap.setCurrNS(ns);
	}
	
	public static VncSymbol getCurrentSymbolLookupNS() {
		return ThreadLocalMap.getCurrFnSymLookupNS();
	}
	
	public static void setCurrentSymbolLookupNS(final VncSymbol ns) {
		ThreadLocalMap.setCurrFnSymLookupNS(ns);
	}

	
	public static final VncSymbol NS_SYMBOL_CURRENT = new VncSymbol("*ns*");
	
	public static final VncSymbol NS_USER = new VncSymbol("user");
	public static final VncSymbol NS_CORE = new VncSymbol("core");
	public static final VncSymbol NS_ANONYMOUS = new VncSymbol("anonymous");
}
