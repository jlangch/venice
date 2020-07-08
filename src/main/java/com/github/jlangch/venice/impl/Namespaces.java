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

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.concurrent.ThreadLocalMap;
import com.github.jlangch.venice.impl.types.util.Types;


public class Namespaces {
	
	public static VncSymbol lookupNS(final VncVal ns, final Env env) {
		if (ns == null || ns == Nil) {
			return Namespaces.NS_CORE;
		}
		else if (Types.isVncSymbol(ns)) {
			final VncSymbol ns_ = (VncSymbol)ns;
		
			if (Namespaces.isCurrentNSSymbol(ns_)) {
				final VncVal val = env.getGlobalOrNil(ns_);
				return val == Nil ? Namespaces.NS_CORE : (VncSymbol)val;
			}
			else {
				return ns_;
			}
		}
		else {
			throw new VncException(String.format(
					"Cannot map type '%s' to VncSymbol to lookup namespace",
					Types.getType(ns)));
		}
	}

	public static boolean isCoreNS(final VncSymbol nsSym) {
		return Namespaces.NS_CORE.equals(nsSym);
	}

	public static boolean isCoreNS(final String nsName) {
		return "core".equals(nsName);
	}

	public static boolean isCurrentNSSymbol(final VncSymbol nsSym) {
		return NS_CURRENT_NAME.equals(nsSym.getName());
	}

	public static boolean isSystemNS(final String nsName) {
		return SYSTEM_NAMESPACES.contains(nsName);
	}

	public static boolean isCurrentNSSymbol(final String nsName) {
		return NS_CURRENT_NAME.equals(nsName);
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

	public static final Set<String> SYSTEM_NAMESPACES = 
			Collections.unmodifiableSet(
					new HashSet<>(
						Arrays.asList(
								"core", "cidr", "csv", "io", "str", "regex", "time")));

	public static final Set<String> RESERVED_NAMESPACES = 
			Collections.unmodifiableSet(
				new HashSet<>(
					Arrays.asList(
							"local",
							"core", 
							"cidr", "csv", "io", "str", "regex", "time",
							"crypt", "json", "pdf", "xml", "bench",
							"test",
							"xchart", "kira", "parsatron",
							"tc", "ring", 
							"http", "jackson",
							"math", "webdav", "maven")));

}
