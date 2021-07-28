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

import java.util.Arrays;
import java.util.List;


public enum BreakpointType {

	FunctionEntry("(", "entry"),			// Stop at function entry

	FunctionExit(")", "exit"),				// Stop at function exit

	FunctionException("!", "exception");	// Stop if exception is caught in function


	private BreakpointType(String symbol, String description) {
		this.symbol = symbol;
		this.description = description;
	}

	public String symbol() { return symbol; }
	public String description() { return description; }

	public static List<BreakpointType> all() {
		return Arrays.asList(values());
	}

	private final String symbol;
	private final String description;
}
