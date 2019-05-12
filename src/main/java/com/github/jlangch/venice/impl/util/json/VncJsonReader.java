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
package com.github.jlangch.venice.impl.util.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncDouble;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.nanojson.JsonLazyNumber;
import com.github.jlangch.venice.nanojson.JsonParserException;
import com.github.jlangch.venice.nanojson.JsonReader;

public class VncJsonReader {
	
	public VncJsonReader(final JsonReader reader) {
		this.reader = reader;
	}

	public VncVal read() {
		try {
			return readAny();
		}
		catch(JsonParserException ex) {
			throw new VncException(
					String.format(
						"Json deserialization error at line %d column %d. %s",
						ex.getLinePosition(),
						ex.getCharPosition(),
						ex.getMessage()),
					ex);
		}
	}
	
	private VncVal readAny() throws JsonParserException {
		switch(reader.current()) {
			case OBJECT:
				return readObject();
			case ARRAY:
				return readArray();
			case STRING:
				return readString();
			case NUMBER:
				return readNumber();
			case BOOLEAN:
				return readBoolean();
			case NULL:
				return readNull();
			default:
				throw new VncException("Json deserialization error");
		}
	}
	
	private VncVal readObject() throws JsonParserException {
		reader.object();
		
		final Map<VncVal,VncVal> map = new HashMap<>();
		while(reader.next()) {
			final String key = reader.key();
			final VncVal val = readAny();
			map.put(new VncString(key), val);
		}
		
		return new VncHashMap(map);
	}

	private VncVal readArray() throws JsonParserException {
		reader.array();
		
		final List<VncVal> list = new ArrayList<>();
		while(reader.next()) {
			final VncVal val = readAny();
			list.add(val);
		}
		
		return new VncList(list);
	}

	private VncVal readString() throws JsonParserException {
		return new VncString(reader.string());
	}

	private VncVal readNumber() throws JsonParserException {
		final Number number = reader.number();
		if (number instanceof JsonLazyNumber) {
			JsonLazyNumber n = (JsonLazyNumber)number;
			return n.isDouble() 
						? new VncDouble(n.doubleValue()) 
						: new VncLong(n.longValue());
		}
		else {
			throw new VncException(
					"Json deserialization error. Unexpected number type " 
						+ number.getClass().getName());
		}
	}

	private VncVal readBoolean() throws JsonParserException {
		return reader.bool() ? Constants.True : Constants.False;
	}

	private VncVal readNull() throws JsonParserException {
		reader.nul();
		return Constants.Nil;
	}

	
	private final JsonReader reader;
}
