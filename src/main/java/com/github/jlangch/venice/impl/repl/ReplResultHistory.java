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
package com.github.jlangch.venice.impl.repl;

import java.util.LinkedList;

import com.github.jlangch.venice.impl.Env;
import com.github.jlangch.venice.impl.Var;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;

public class ReplResultHistory {
	
	public ReplResultHistory(final int max) {
		this.max = max;
	}

	public void add(final VncVal val) {
		results.addFirst(val == null ? Constants.Nil : val);
		if (results.size() > max) results.removeLast();
	}
	
	public void mergeToEnv(final Env env) {
		for(int ii=0; ii<max; ii++) {
			env.setGlobal(new Var(
					new VncSymbol("*" + (ii + 1)),
					ii < results.size() ? results.get(ii) : Constants.Nil));
		}
	}

	
	private final int max;
	private final LinkedList<VncVal> results = new LinkedList<>();
}
