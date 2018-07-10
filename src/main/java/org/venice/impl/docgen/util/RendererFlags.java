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
package org.venice.impl.docgen.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.venice.impl.util.StringUtil;


public class RendererFlags {

	private RendererFlags(final Set<String> flags) {
		this.flags.addAll(flags == null ? new HashSet<>() : flags);
	}
	
	/**
	 * Parses a colon delimited format flag string. 
	 * 
	 * <p>"flag1:flag2:flag3" => { 'flag1', 'flag2', 'flag3'}
	 * 
	 * @param	format
	 * 				A format
	 * 
	 * @return	The parsed flags as a set of strings 
	 */
	public static RendererFlags parse(final String format) {
		return new RendererFlags(
				Arrays.stream(StringUtil.trimToEmpty(format).split("[:]"))
				      .map(f -> StringUtil.trimToNull(f))
				      .filter(f -> f != null)
				      .collect(Collectors.toSet()));
	}

	public boolean contains(final String flag) {
		return flags.contains(StringUtil.trimToEmpty(flag));
	}

	public boolean containsAll(final String ... flags) {
		return flags == null 
					? false 
					: this.flags.containsAll(new HashSet<>(Arrays.asList(flags)));
	}

	@Override
	public String toString() {
		return flags.toString();
	}

	
	private final Set<String> flags = new HashSet<>();
}
