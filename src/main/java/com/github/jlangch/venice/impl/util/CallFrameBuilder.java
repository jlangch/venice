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
package com.github.jlangch.venice.impl.util;

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import com.github.jlangch.venice.impl.MetaUtil;
import com.github.jlangch.venice.impl.types.Coerce;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.Types;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.util.CallFrame;


public class CallFrameBuilder {
	
	public static CallFrame fromVal(final String fnName, final VncVal val) {
		return build(
				fnName,
				val.getMetaVal(MetaUtil.FILE),
				val.getMetaVal(MetaUtil.LINE),
				val.getMetaVal(MetaUtil.COLUMN));
	}
	
	public static CallFrame fromVal(final VncVal val) {
		return build(
				null,
				val.getMetaVal(MetaUtil.FILE),
				val.getMetaVal(MetaUtil.LINE),
				val.getMetaVal(MetaUtil.COLUMN));
	}

	public static CallFrame fromFunction(final VncFunction fn, final VncVal fnSym) {
		if (Types.isVncSymbol(fnSym)) {
			return build(
					fn.getName(), 		
					fnSym.getMetaVal(MetaUtil.FILE),
					fnSym.getMetaVal(MetaUtil.LINE),
					fnSym.getMetaVal(MetaUtil.COLUMN));
		}
		else {
			return CallFrameBuilder.build(fn.getName(), Nil, Nil, Nil);
		}
	}
	
	public static CallFrame build(
			final String fnName,
			final VncVal file, 
			final VncVal line, 
			final VncVal column
	) {
		return new CallFrame(
				fnName,
				file == Constants.Nil ? null : Coerce.toVncString(file).getValue(),
				line == Constants.Nil ? null : Coerce.toVncLong(line).getValue().intValue(),
				column == Constants.Nil ? null : Coerce.toVncLong(column).getValue().intValue());
	}

}
