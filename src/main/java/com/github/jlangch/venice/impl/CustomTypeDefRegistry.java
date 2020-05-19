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
import com.github.jlangch.venice.impl.types.custom.VncChoiceTypeDef;
import com.github.jlangch.venice.impl.types.custom.VncCustomTypeDef;
import com.github.jlangch.venice.impl.types.custom.VncWrappingTypeDef;


public class CustomTypeDefRegistry {
	
	public CustomTypeDefRegistry() {
	}

	public void addCustomType(final VncCustomTypeDef typeDef) {
		Objects.requireNonNull(typeDef);
		customTypes.put(typeDef.getType(), typeDef);
	}

	public void addWrappedType(final VncWrappingTypeDef typeDef) {
		Objects.requireNonNull(typeDef);
		wrappedTypes.put(typeDef.getType(), typeDef);
	}

	public void addChoiceType(final VncChoiceTypeDef typeDef) {
		Objects.requireNonNull(typeDef);
		choiceTypes.put(typeDef.getType(), typeDef);
	}

	public VncCustomTypeDef getCustomType(final VncKeyword type) {
		Objects.requireNonNull(type);
		return customTypes.get(type);
	}

	public VncWrappingTypeDef getWrappedType(final VncKeyword type) {
		Objects.requireNonNull(type);
		return wrappedTypes.get(type);
	}

	public VncChoiceTypeDef getChoiceType(final VncKeyword type) {
		Objects.requireNonNull(type);
		return choiceTypes.get(type);
	}

	public boolean existsType(final VncKeyword type) {
		Objects.requireNonNull(type);
		return existsCustomType(type) 
					|| existsWrappedType(type) 
					|| existsChoiceType(type);
	}

	public boolean existsCustomType(final VncKeyword type) {
		Objects.requireNonNull(type);
		return customTypes.get(type) != null;
	}
	
	public boolean existsWrappedType(final VncKeyword type) {
		Objects.requireNonNull(type);
		return wrappedTypes.get(type) != null;
	}
	
	public boolean existsChoiceType(final VncKeyword type) {
		Objects.requireNonNull(type);
		return choiceTypes.get(type) != null;
	}
	
	public VncCustomTypeDef removeCustomType(final VncKeyword type) {
		Objects.requireNonNull(type);
		return customTypes.remove(type);
	}
	
	public VncWrappingTypeDef removeWrappedType(final VncKeyword type) {
		Objects.requireNonNull(type);
		return wrappedTypes.remove(type);
	}
	
	public VncChoiceTypeDef removeChoiceType(final VncKeyword type) {
		Objects.requireNonNull(type);
		return choiceTypes.remove(type);
	}

	public void clear() {
		customTypes.clear();
		wrappedTypes.clear();
		choiceTypes.clear();
	}
	

	private final Map<VncKeyword, VncCustomTypeDef> customTypes = new ConcurrentHashMap<>();
	private final Map<VncKeyword, VncWrappingTypeDef> wrappedTypes = new ConcurrentHashMap<>();
	private final Map<VncKeyword, VncChoiceTypeDef> choiceTypes = new ConcurrentHashMap<>();
	
	
}
