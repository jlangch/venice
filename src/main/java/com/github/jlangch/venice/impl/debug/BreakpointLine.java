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

import java.util.Comparator;

import com.github.jlangch.venice.impl.MetaUtil;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncMap;


/**
 * Defines a breakpoint given by a file and a line number
 */
public class BreakpointLine implements IBreakpoint {

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
	
	
	public static BreakpointLine fromMeta(final VncVal meta) {
		if (meta instanceof VncMap) {
			final String file = MetaUtil.getFile(meta);
			final int lineNr = MetaUtil.getLine(meta);
			return file != null && lineNr > 0
					? new BreakpointLine(file, lineNr)
					: null;
		}
		else {
			return null;
		}
	}

	
	public String getFile() {
		return file;
	}
	
	public int getLineNr() {
		return lineNr;
	}
	
	public boolean isSameFile(final BreakpointLine bp) {
		return file.equals(bp.getFile());
	}
	
	public boolean isSameLineNr(final BreakpointLine bp) {
		return lineNr == bp.getLineNr();
	}
	
	@Override
	public String format() {
		return String.format("%s at line %d", file, lineNr);
	}
	
	@Override
	public String formatEx() {
		return String.format("%s at line %d", file, lineNr);
	}
	
	@Override
	public String toString() {
		return String.format("Line breakpoint: %s", format());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		result = prime * result + lineNr;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BreakpointLine other = (BreakpointLine) obj;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		if (lineNr != other.lineNr)
			return false;
		return true;
	}

	@Override
	public int compareTo(final IBreakpoint o) {
		if (o instanceof BreakpointLine) {
			return comp.compare(this, (BreakpointLine)o);
		}
		else {
			return 1;
		}
	}


	private static Comparator<BreakpointLine> comp = 
			Comparator.comparing(BreakpointLine::getFile)
					  .thenComparing(BreakpointLine::getLineNr);
	
	private final String file;
	private final int lineNr;			
}
