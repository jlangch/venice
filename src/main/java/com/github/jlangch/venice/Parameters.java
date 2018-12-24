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
package com.github.jlangch.venice;

import java.util.HashMap;
import java.util.Map;


/**
 * A script parameter builder
 */
public class Parameters {
	
	public Parameters() {
	}
	
	public static Map<String,Object> of(final String key, final Object val) {
		return new Parameters().put(key, val).toMap();
	}

	public static Map<String,Object> of(
			final String key1, final Object val1,
			final String key2, final Object val2
	) {
		return new Parameters()
					.put(key1, val1).put(key2, val2)
					.toMap();
	}

	public static Map<String,Object> of(
			final String key1, final Object val1,
			final String key2, final Object val2,
			final String key3, final Object val3
	) {
		return new Parameters()
					.put(key1, val1).put(key2, val2)
					.put(key3, val3)
					.toMap();
	}

	public static Map<String,Object> of(
			final String key1, final Object val1,
			final String key2, final Object val2,
			final String key3, final Object val3,
			final String key4, final Object val4
	) {
		return new Parameters()
					.put(key1, val1).put(key2, val2)
					.put(key3, val3).put(key4, val4)
					.toMap();
	}

	public static Map<String,Object> of(
			final String key1, final Object val1,
			final String key2, final Object val2,
			final String key3, final Object val3,
			final String key4, final Object val4,
			final String key5, final Object val5
	) {
		return new Parameters()
					.put(key1, val1).put(key2, val2)
					.put(key3, val3).put(key4, val4)
					.put(key5, val5)
					.toMap();
	}

	public static Map<String,Object> of(
			final String key1, final Object val1,
			final String key2, final Object val2,
			final String key3, final Object val3,
			final String key4, final Object val4,
			final String key5, final Object val5,
			final String key6, final Object val6
	) {
		return new Parameters()
					.put(key1, val1).put(key2, val2)
					.put(key3, val3).put(key4, val4)
					.put(key5, val5).put(key6, val6)
					.toMap();
	}

	public Parameters put(final String key, final Object val) {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("A parameter key must not be null or empty");
		}
		
		symbols.put(key, val);
		return this;
	}
	
	public Map<String,Object> toMap() {
		return symbols;
	}
	
	
	private final Map<String,Object> symbols = new HashMap<>();
}
