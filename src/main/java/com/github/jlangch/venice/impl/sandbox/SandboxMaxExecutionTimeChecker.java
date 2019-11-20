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
package com.github.jlangch.venice.impl.sandbox;

import java.io.Serializable;

import com.github.jlangch.venice.impl.javainterop.JavaInterop;

public class SandboxMaxExecutionTimeChecker implements Serializable {

	public SandboxMaxExecutionTimeChecker() {
		this.sandboxDeadlineTime = getSandboxDeadlineTime();
	}
	
	
	public void check() {
		if (sandboxDeadlineTime > 0 && System.currentTimeMillis() > sandboxDeadlineTime) {
			throw new SecurityException(
					"Venice Sandbox: The sandbox exceeded the max execution time");
		}
	}

	private static long getSandboxDeadlineTime() {
		final Integer maxExecTimeSeconds = JavaInterop.getInterceptor().getMaxExecutionTimeSeconds();
		return maxExecTimeSeconds == null 
					? -1
					: System.currentTimeMillis() + 1000 * maxExecTimeSeconds.longValue();
	}

	
	private static final long serialVersionUID = -2470884288885597614L;

	private final long sandboxDeadlineTime;
}
