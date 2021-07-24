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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.concurrent.ThreadLocalMap;
import com.github.jlangch.venice.impl.util.CallStack;


public class DebugAgent implements IDebugAgent {

	public DebugAgent() {
	}

	// -------------------------------------------------------------------------
	// Debugger turn on/off
	// -------------------------------------------------------------------------
	
	@Override
	public void activate(final boolean activate) {
		activated = activate;
		activeBreak = null;
		breakListener = null;
	}

	@Override
	public boolean activated() {
		return activated;
	}



	// -------------------------------------------------------------------------
	// Breakpoint management
	// -------------------------------------------------------------------------

	@Override
	public boolean hasBreakpoint(final String qualifiedName) {
		return breakpoints.containsKey(qualifiedName);
	}
	
	@Override
	public List<String> listBreakpoints() {
		return new ArrayList<>(breakpoints.keySet());
	}

	@Override
	public void addBreakpoint(final String qualifiedName) {
		breakpoints.put(qualifiedName, "");
	}

	@Override
	public void removeBreakpoint(final String qualifiedName) {
		breakpoints.remove(qualifiedName);
	}

	@Override
	public void removeAllBreakpoints() {
		breakpoints.clear();
	}



	// -------------------------------------------------------------------------
	// Breaks
	// -------------------------------------------------------------------------

	@Override
	public void addBreakListener(final IBreakListener listener) {
		breakListener = listener;
	}
	
	public void onBreak(
			final VncFunction fn,
			final VncList args,
			final Env env
	) {
		final CallStack callstack = ThreadLocalMap.getCallStack();
		
		onBreakEntered(fn, args, env, callstack);
		
		try {
			while(hasBreak()) {
				Thread.sleep(500);
			}
		}
		catch(InterruptedException ex) {
			throw new com.github.jlangch.venice.InterruptedException(
					String.format(
							"Interrupted while waiting for leaving breakpoint in function '%s'.",
							fn.getQualifiedName()));
		}
		finally {
			activeBreak = null;
		}
	}

	@Override
	public Break getBreak() {
		return activeBreak;
	}

	@Override
	public boolean hasBreak() {
		return activeBreak != null;
	}

	@Override
	public void leaveBreak() {
		activeBreak = null;
	}


	private void onBreakEntered(
			final VncFunction fn,
			final VncList args,
			final Env env,
			final CallStack callstack
	) {
		activeBreak = new Break(fn, args, env, callstack);
		
		if (breakListener != null) {
			breakListener.onBreak(activeBreak);
		}
	}


	private volatile boolean activated = false;
	private volatile Break activeBreak = null;
	private volatile IBreakListener breakListener = null;
	private final ConcurrentHashMap<String,String> breakpoints = new ConcurrentHashMap<>();
}
