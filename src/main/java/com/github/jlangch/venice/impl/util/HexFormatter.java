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
package com.github.jlangch.venice.impl.util;

import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * A hex formatter
 */
public class HexFormatter {
	
	private HexFormatter() {
	}
	
	/**
	 * Formats to hex.
	 * 
	 * <pre>
	 *   byte[] arr = { 0x45, 0xE5, 0x12 };
	 * 
	 *   toHex(arr, null) =&gt; "45E512"
	 *   toHex(arr, ", ") =&gt; "45, E5, 12"
	 * </pre>
	 * 
	 * @param	binary An optional binary array
	 * @param	delimiter An optional delimiter
	 * 
	 * @return	The hex formatted string or an empty string if the 
	 * 			<code>binary</code> is <code>null</code> or empty
	 */
	public static String toHex(final byte[] binary, final String delimiter) {
		return toHex(binary, delimiter, false);
	}

	/**
	 * Formats to hex.
	 * 
	 * <pre>
	 *   byte[] arr = { 0x45, 0xE5, 0x12 };
	 * 
	 *   toHex(arr, null, false) =&gt; "45E512"
	 *   toHex(arr, ", ", true) =&gt; "0x45, 0xE5, 0x12"
	 * </pre>
	 * 
	 * @param	binary An optional binary array
	 * @param	delimiter An optional delimiter
	 * @param	prefixWith0x prefix every byte with '0x'
	 * 
	 * @return	The hex formatted string or an empty string if the 
	 * 			<code>binary</code> is <code>null</code> or empty
	 */
	public static String toHex(
			final byte[] binary, 
			final String delimiter, 
			final boolean prefixWith0x
	) {
		return binary == null
				? ""
				: IntStream.range(0, binary.length)
						   .mapToObj(idx -> toHex(binary[idx], prefixWith0x))
						   .collect(Collectors.joining(delimiter == null ? "" : delimiter));
	}

	
	public static String toHex(final byte aByte) {
		return String.format("%02X", aByte);
	}
	
	public static String toHex(final byte aByte, final boolean prefixWith0x) {
		return prefixWith0x ? "0x" + toHex(aByte) : toHex(aByte);
	}

}
