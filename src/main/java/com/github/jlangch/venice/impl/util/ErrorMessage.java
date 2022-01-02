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

import com.github.jlangch.venice.impl.reader.Token;
import com.github.jlangch.venice.impl.types.VncVal;


public class ErrorMessage {

	public static String buildErrLocation(
			final String file,
			final int line,
			final int column
	) {
		return String.format(
				"File <%s> (%d,%d)",
				file == null ? "unknown" : file,
				line,
				column);
	}

	public static String buildErrLocation(final Token token) {
		return token != null
				? buildErrLocation(
						token.getFile(),
						token.getLine(),
						token.getColumn())
				: "File <unknown> (1,1)";
	}
	
	public static String buildErrLocation(final VncVal val) {
		final VncVal meta = val.getMeta();
		
		final String file = MetaUtil.getFile(meta);
		final int line =  MetaUtil.getLine(meta);
		final int column =  MetaUtil.getCol(meta);
		
		return String.format(
				"File <%s> (%d,%d)",
				file == null || file.isEmpty()? "unknown" : file,
				line == -1 ? 1 : line,
				column == -1 ? 1 : column);
	}
}
