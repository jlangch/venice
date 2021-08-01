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
package com.github.jlangch.venice.impl.debug;

import static com.github.jlangch.venice.impl.util.StringUtil.isBlank;


public class BreakpointLine {

	public BreakpointLine(
			final String file,
			final int lineNr			
	) {
		if (isBlank(file)) {
			throw new IllegalArgumentException("A file must not be blank");
		}
		if (lineNr <= 0) {
			throw new IllegalArgumentException("A lineNr must not be lower than 1");
		}
		
		this.file = file;
		this.lineNr = lineNr;
	}
	

	public String getFile() {
		return file;
	}
	
	public int getLineNr() {
		return lineNr;
	}
	
	@Override
	public String toString() {
		return String.format("%s %d", file, lineNr);
	}


	private final String file;
	private final int lineNr;			
}
