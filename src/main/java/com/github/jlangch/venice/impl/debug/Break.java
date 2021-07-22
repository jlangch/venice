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

import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.util.CallStack;


public class Break {

	public Break(
			final VncFunction fn,
			final VncList args,
			final Env env,
			final CallStack callStack
	) {
		this.fn = fn;
		this.args = args;
		this.env = env;
		this.callStack = callStack;
	}
	

	public VncFunction getFn() {
		return fn;
	}

	public VncList getArgs() {
		return args;
	}

	public Env getEnv() {
		return env;
	}

	public CallStack getCallStack() {
		return callStack;
	}


	private final VncFunction fn;
	private final VncList args;
	private final Env env;
	private final CallStack callStack;
}
