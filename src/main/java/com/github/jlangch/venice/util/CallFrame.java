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
package com.github.jlangch.venice.util;

import com.github.jlangch.venice.impl.MetaUtil;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;

public class CallFrame {

	public CallFrame(
			final String fnName, 
			final String file, 
			final int line, 
			final int col
	) {
		this.fnName = fnName;
		this.file = file == null || file.isEmpty() ? "unknown" : file;
		this.line = line;
		this.col = col;
	}
	
	public static CallFrame fromVal(final String fnName, final VncVal val) {
		final VncVal file = val.getMetaVal(MetaUtil.FILE);
		final VncVal line = val.getMetaVal(MetaUtil.LINE);
		final VncVal column = val.getMetaVal(MetaUtil.COLUMN);
		return new CallFrame(
				fnName,
				file == Constants.Nil ? "unknown" : ((VncString)file).getValue(),
				line == Constants.Nil ? 1 : ((VncLong)line).getValue().intValue(),
				column == Constants.Nil ? 1 : ((VncLong)column).getValue().intValue());
	}
	
	public static CallFrame fromVal(final VncVal val) {
		final VncVal file = val.getMetaVal(MetaUtil.FILE);
		final VncVal line = val.getMetaVal(MetaUtil.LINE);
		final VncVal column = val.getMetaVal(MetaUtil.COLUMN);
		return new CallFrame(
				null,
				file == Constants.Nil ? "unknown" : ((VncString)file).getValue(),
				line == Constants.Nil ? 1 : ((VncLong)line).getValue().intValue(),
				column == Constants.Nil ? 1 : ((VncLong)column).getValue().intValue());
	}
	
	public String getFnName() {
		return fnName;
	}
	
	public String getFile() {
		return file;
	}
	
	public int getLine() {
		return line;
	}
	
	public int getCol() {
		return col;
	}


	@Override
	public String toString() {
		return fnName == null
				? String.format("%s: line %d, col %d", file, line, col)
				: String.format("%s (%s: line %d, col %d)", fnName, file, line, col);
	}

	
	private final String fnName;
	private final String file; 
	private final int line; 
	private final int col;
}
