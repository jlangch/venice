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
package org.venice.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.venice.impl.types.Types;
import org.venice.impl.types.VncVal;
import org.venice.impl.types.collections.VncList;

public class Printer {

	public static String join(
			final List<VncVal> value, 
			final String delim, 
			final boolean print_readably
	) {
		return value.stream()
					.map(v -> v.toString(print_readably))
					.collect(Collectors.joining(delim));
	}

	public static String join(
			final Map<String,VncVal> value, 
			final String delim, 
			final boolean print_readably
	) {
		return value
				.entrySet()
				.stream()
				.map(e -> {
					final String v = e.getValue().toString(print_readably);
					if (Types.isVncKeyword(e.getKey())) {
						return ":" + e.getKey().substring(1) + delim + v;
					} 
					else if (print_readably) {
						return "\"" + e.getKey().toString() + "\"" + delim + v;
					} 
					else {
						return e.getKey().toString() + delim + v;
					}
				 })
				.collect(Collectors.joining(delim));
	}

	public static String _pr_str(final VncVal mv, final boolean print_readably) {
		return mv.toString(print_readably);
	}

	public static String _pr_str_args(
			final VncList args,
			final String sep, 
			final boolean print_readably
	) {
		return join(args.getList(), sep, print_readably);
	}
}
