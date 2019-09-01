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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.concurrent.ThreadLocalMap;


public class Namespaces {

	public static String getNamespace(final String name) {
		final int pos = name.indexOf("/");
		return pos < 1 ? null : name.substring(0, pos);
	}

	public static String getName(final String name) {
		final int pos = name.indexOf("/");
		return pos < 0 ? name : name.substring(pos+1);
	}

	public static boolean isQualified(final String name) {
		return name.indexOf("/") >= 1;
	}

	public static boolean isQualified(final VncSymbol sym) {
		return isQualified(sym.getName());
	}


	public static boolean isCoreNS(final VncSymbol nsSym) {
		return Namespaces.NS_CORE.equals(nsSym);
	}

	public static boolean isCoreNS(final String nsName) {
		return "core".equals(nsName);
	}

	public static VncSymbol getCurrentNS() {
		return ThreadLocalMap.getCurrNS().getNS();
	}

	public static Namespace getCurrentNamespace() {
		return ThreadLocalMap.getCurrNS();
	}

	public static void setCurrentNamespace(final Namespace ns) {
		ThreadLocalMap.setCurrNS(ns);
	}

	
	public static final String NS_CURRENT_NAME = "*ns*";
	public static final VncSymbol NS_CURRENT_SYMBOL = new VncSymbol("*ns*");
	
	public static final VncSymbol NS_USER = new VncSymbol("user");
	public static final VncSymbol NS_CORE = new VncSymbol("core");
	public static final VncSymbol NS_IO = new VncSymbol("io");
	public static final VncSymbol NS_STR = new VncSymbol("str");
	public static final VncSymbol NS_REGEX = new VncSymbol("regex");
	public static final VncSymbol NS_TIME = new VncSymbol("time");
	
	public static final Set<String> RESERVED_NAMESPACES = 
			Collections.unmodifiableSet(
				new HashSet<>(
					Arrays.asList(
							"core", "io", "str", "regex", "time",
							"crypt", "json", "pdf", "xml", "bench",
							"test",
							"xchart", "kira", 
							"tc", "ring", 
							"http", "jackson",
							"math", "webdav", "maven")));

}
