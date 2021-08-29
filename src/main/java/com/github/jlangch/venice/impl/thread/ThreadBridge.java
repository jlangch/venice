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
package com.github.jlangch.venice.impl.thread;

import static com.github.jlangch.venice.impl.thread.ThreadBridge.Options.DEACTIVATE_DEBUG_AGENT;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.debug.agent.DebugAgent;
import com.github.jlangch.venice.impl.util.CollectionUtil;
import com.github.jlangch.venice.impl.util.StringUtil;


/**
 * The <code>ThreadBridge</code> properly runs functions in clients threads
 * inheriting the correct environment from the calling parent function.
 * 
 * <p>Functions that are run in futures, agents, or schedulers are managed
 * by this bridge.
 */
public class ThreadBridge {

	private ThreadBridge(
			final String name,
			final ThreadContextSnapshot parentThreadSnapshot,
			final boolean deactivateDebugAgent
	) {
		this.parentThreadSnapshot = parentThreadSnapshot;
		this.deactivateDebugAgent = deactivateDebugAgent;
	}
	
	public static ThreadBridge create(
			final String name,
			final Options... options
	) {
		final Set<Options> opts = new HashSet<>(CollectionUtil.toList(options));

		final boolean deactivateDebugAgent = opts.contains(DEACTIVATE_DEBUG_AGENT);

		validateName(name);

		return new ThreadBridge(
						name,
						ThreadContext.snapshot(),
						deactivateDebugAgent);
	}
	
	public <T> Callable<T> bridgeCallable(final Callable<T> callable) {
		final Callable<T> wrapper = () -> {
			try {
				// inherit thread local values to the child thread
				ThreadContext.inheritFrom(parentThreadSnapshot);

				if (deactivateDebugAgent) {
					DebugAgent.unregister();
				}

				return callable.call();
			}
			finally {
				// clean up
				ThreadContext.remove();
			}};
		
		return wrapper;
	}
	
	public Runnable bridgeRunnable(final Runnable runnable) {
		final Runnable wrapper = () -> {
			try {
				// inherit thread local values to the child thread
				ThreadContext.inheritFrom(parentThreadSnapshot);

				if (deactivateDebugAgent) {
					DebugAgent.unregister();
				}
				
				runnable.run();
			}
			finally {
				// clean up
				ThreadContext.remove();
			}};
		
		return wrapper;
	}
	
	public boolean isSameAsCurrentThread() {
		return parentThreadSnapshot.isSameAsCurrentThread();
	}
	
	
	private static void validateName(final String name) {
		if (StringUtil.isBlank(name)) {
			throw new VncException("A ThreadBridge name must not be blank!");
		}
	}
	
	
	public static enum Options { 
		DEACTIVATE_DEBUG_AGENT
	};
	
	
	private final ThreadContextSnapshot parentThreadSnapshot;
	private final boolean deactivateDebugAgent;
}
