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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.debug.agent.DebugAgent;
import com.github.jlangch.venice.impl.util.CollectionUtil;
import com.github.jlangch.venice.impl.util.StringUtil;


public class ThreadBridge {

	private ThreadBridge(
			final String name,
			final ThreadContextSnapshot snapshot,
			final Collection<Options> options
	) {
		this.name = name;
		this.snapshot = snapshot;
		this.options.addAll(options);
	}
	
	public static ThreadBridge create(
			final String name,
			final Options... options
	) {
		final Set<Options> opts = new HashSet<>(CollectionUtil.toList(options));
		
		validateName(name);
		validateOptions(name, opts);
		
		return new ThreadBridge(
						name,
						ThreadContext.snapshot(),
						opts);
	}
	
	public <T> Callable<T> bridgeCallable(final Callable<T> callable) {
		final Callable<T> wrapper = () -> {
			if (snapshot.isSameAsCurrentThread()) {
				validateRunInSameThread();
				
				return callable.call();
			}
			else {
				try {
					// inherit thread local values to the child thread
					ThreadContext.inheritFrom(snapshot);

					if (options.contains(Options.DEACTIVATE_DEBUG_AGENT)) {
						DebugAgent.unregister();
					}

					return callable.call();
				}
				finally {
					// clean up
					ThreadContext.remove();
				}
			}};
		
		return wrapper;
	}
	
	public Runnable bridgeRunnable(final Runnable runnable) {
		final Runnable wrapper = () -> {
			if (snapshot.isSameAsCurrentThread()) {
				validateRunInSameThread();
				
				runnable.run();
			}
			else {
				try {
					// inherit thread local values to the child thread
					ThreadContext.inheritFrom(snapshot);

					if (options.contains(Options.DEACTIVATE_DEBUG_AGENT)) {
						DebugAgent.unregister();
					}
					
					runnable.run();
				}
				finally {
					// clean up
					ThreadContext.remove();
				}
			}};
		
		return wrapper;
	}
	
	
	private void validateRunInSameThread() {
		if (!options.contains(Options.ALLOW_SAME_THREAD)) {
			throw new VncException(String.format(
					"The ThreadBridge '%s' is not allowed to run in the "
					+ "same thread!", 
					name));
		}
		if (options.contains(Options.DEACTIVATE_DEBUG_AGENT)) {
			throw new VncException(String.format(
					"The ThreadBridge '%s' can not deactivate the "
					+ "debugger if run in the same thread to prevent "
					+ "unexpected behaviour!", 
					name));
		}
	}
	
	private static void validateName(
			final String name
	) {
		if (StringUtil.isBlank(name)) {
			throw new VncException("A ThreadBridge name must not be blank!");
		}
	}
	
	private static void validateOptions(
			final String name,
			final Set<Options> options
	) {
		if (options.contains(Options.ALLOW_SAME_THREAD)
				&& options.contains(Options.DEACTIVATE_DEBUG_AGENT)
		) {
			throw new VncException(String.format(
					"The ThreadBridge '%s' can not deactivate the "
					+ "debugger if run in the same thread to prevent "
					+ "unexpected behaviour!", 
					name));
		}
	}
	
	
	public static enum Options { 
		ALLOW_SAME_THREAD,
		DEACTIVATE_DEBUG_AGENT
	};
	
	
	private final String name;
	private final ThreadContextSnapshot snapshot;
	private final Set<Options> options = new HashSet<>();
}
