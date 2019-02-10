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
package com.github.jlangch.venice.util;

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import com.github.jlangch.venice.impl.MetaUtil;
import com.github.jlangch.venice.impl.types.Coerce;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;


public class CallFrame {

	public CallFrame(final String fnName, final VncVal meta) {
		this.fnName = fnName;
		this.meta = meta;
	}
	
	public String getFnName() {
		return fnName;
	}
	
	public String getFile() {
		final VncVal vFile = meta == Nil ? Nil : getMetaVal(MetaUtil.FILE);
		final String file = vFile == Nil ? null : Coerce.toVncString(vFile).getValue();
		return file == null || file.isEmpty() ? "unknown" : file;
	}
	
	public int getLine() {
		final VncVal vLine = meta == Nil ? Nil : getMetaVal(MetaUtil.LINE);
		return vLine == Nil ? -1 : Coerce.toVncLong(vLine).getValue().intValue();		
	}
	
	public int getCol() {
		final VncVal vCol = meta == Nil ? Nil : getMetaVal(MetaUtil.COLUMN);
		return vCol == Nil ? -1 : Coerce.toVncLong(vCol).getValue().intValue();		
	}

	@Override
	public String toString() {
		return fnName == null
				? String.format("%s: line %d, col %d", getFile(), getLine(), getCol())
				: String.format("%s (%s: line %d, col %d)", fnName, getFile(), getLine(), getCol());
	}

	private VncVal getMetaVal(final VncString key) {
		if (meta == Constants.Nil) {
			return Constants.Nil;
		}
		else if (meta instanceof VncHashMap) {
			return ((VncHashMap)meta).get(key);
		}
		else {
			return Constants.Nil; // not a map
		}
	}

	
	private final String fnName;
	private final VncVal meta; 
}
