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
package com.github.jlangch.venice;

import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.util.Types;


public class ValueException extends VncException {
		
	public ValueException(final String fnName, final Object value) {
		super(String.format("%s value thrown from %s", type(value), fnName));
		this.value = value;
	}

	public ValueException(final Object value) {
		this.value = value;
	}

	public ValueException(final Object value, final Throwable cause) {
		super(cause);
		this.value = value;
	}
	
	public Object getValue() { 
		return value; 
	}
	
	private static String type(final Object value) {
		if (value == null) {
			return "nil";
		}
		else if (value instanceof VncVal) {
			return Types.getType((VncVal)value).toString();
		}
		else {
			return ":" + value.getClass().getName();
		}
	}
	
	
	private static final long serialVersionUID = -7070216020647646364L;

	private final Object value;
}