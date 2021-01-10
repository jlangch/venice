package com.github.jlangch.venice.impl.util.excel;
/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2021 Venice
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

import java.util.HashMap;
import java.util.Map;


public class EntityRecord {
	public EntityRecord() {
	}
	
	public static EntityRecord of(final Map<String,Object> fields) {
		final EntityRecord ge = new EntityRecord();
		fields.forEach((k,v) -> ge.put(k, v));
		return ge;
	}
	
	public void put(final String fieldName, final Object fieldValue) {
		fields.put(fieldName, fieldValue);
	}

	public Object get(final String fieldName) {
		return fields.get(fieldName);
	}

	final private Map<String,Object> fields = new HashMap<>();
}
