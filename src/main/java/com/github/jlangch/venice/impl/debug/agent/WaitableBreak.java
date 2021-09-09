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
package com.github.jlangch.venice.impl.debug.agent;

import static com.github.jlangch.venice.impl.util.StringUtil.padRight;

import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Enhances the {@link Break} class with a debugger 'waitable' flag.
 */
public class WaitableBreak {

	public WaitableBreak(final Break br) {
		this.br = br;
	}


	public Break getBreak() {
		return br;
	}
	
	public boolean isWaitingOnBreak() {
		return waiting.get();
	}

	public void startWaitingOnBreak() {
		waiting.set(true);
	}

	public void stopWaitingOnBreak() {
		waiting.set(false);
	}

	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(br.toString());
		sb.append("\n");
		sb.append(String.format(
					"%s %b", 
					padRight("Waiting:", Break.FORMAT_PAD_LEN),
					waiting.get()));
		return sb.toString();
	}

	@Override
	public int hashCode() {
		return br.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WaitableBreak other = (WaitableBreak) obj;
		if (br == null) {
			if (other.br != null)
				return false;
		} else if (!br.equals(other.br))
			return false;
		return true;
	}



	private final Break br;
	private final AtomicBoolean waiting = new AtomicBoolean(false);
}
