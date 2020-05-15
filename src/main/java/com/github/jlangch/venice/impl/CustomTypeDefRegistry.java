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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.custom.VncCustomTypeDef;


public class CustomTypeDefRegistry {
	
	public CustomTypeDefRegistry() {
	}

	public void add(final VncCustomTypeDef typeDef) {
		Objects.requireNonNull(typeDef);
		types.put(typeDef.getType(), typeDef);
	}
	
	public VncCustomTypeDef get(final VncKeyword type) {
		Objects.requireNonNull(type);
		return types.get(type);
	}
	
	public boolean exists(final VncKeyword type) {
		Objects.requireNonNull(type);
		return types.get(type) != null;
	}
	
	public VncCustomTypeDef computeIfAbsent(final VncCustomTypeDef typeDef) {
		Objects.requireNonNull(typeDef);
		return types.computeIfAbsent(typeDef.getType(), t -> typeDef);
	}
	
	public VncCustomTypeDef remove(final VncKeyword type) {
		Objects.requireNonNull(type);
		return types.remove(type);
	}

	public void clear() {
		types.clear();
	}
	

	private final Map<VncKeyword, VncCustomTypeDef> types = new ConcurrentHashMap<>();
}
