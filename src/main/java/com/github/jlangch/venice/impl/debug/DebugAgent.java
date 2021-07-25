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

import static com.github.jlangch.venice.impl.debug.BreakpointType.FunctionEntry;
import static com.github.jlangch.venice.impl.debug.BreakpointType.FunctionException;
import static com.github.jlangch.venice.impl.debug.BreakpointType.FunctionExit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.concurrent.ThreadLocalMap;


public class DebugAgent implements IDebugAgent {

	public DebugAgent() {
	}
	

	public static void register(final DebugAgent agent) {
		ThreadLocalMap.setDebugAgent(agent);
	}
	
	public static void unregister() {
		ThreadLocalMap.setDebugAgent(null);
	}
	
	public static DebugAgent current() {
		return ThreadLocalMap.getDebugAgent();
	}
	
	
	// -------------------------------------------------------------------------
	// Debugger turn on/off
	// -------------------------------------------------------------------------
	
	@Override
	public void start() {
		activated = true;
		activeBreak = null;
		stopOnNextFunction = false;
		breakListener = null;
	}

	@Override
	public void stop() {
		activated = false;
		activeBreak = null;
		stopOnNextFunction = false;
		breakListener = null;
	}

	@Override
	public boolean active() {
		return activated;
	}



	// -------------------------------------------------------------------------
	// Breakpoint management
	// -------------------------------------------------------------------------

	@Override
	public boolean hasBreakpoint(final String qualifiedName) {		 
		return stopOnNextFunction || breakpoints.containsKey(qualifiedName);
	}
	
	@Override
	public Map<String, Set<BreakpointType>> getBreakpoints() {
		return new HashMap<>(breakpoints);
	}

	@Override
	public void addBreakpoint(
			final String qualifiedName, 
			final Set<BreakpointType> types
	) {
		final Set<BreakpointType> copy = new HashSet<>(types);
		
		if (copy.isEmpty()) {
			copy.add(FunctionEntry);
		}
		
		breakpoints.put(qualifiedName, copy);
	}

	@Override
	public void removeBreakpoint(final String qualifiedName) {
		breakpoints.remove(qualifiedName);
	}

	@Override
	public void removeAllBreakpoints() {
		breakpoints.clear();
		stopOnNextFunction = false;
	}



	// -------------------------------------------------------------------------
	// Breaks
	// -------------------------------------------------------------------------

	@Override
	public void addBreakListener(final IBreakListener listener) {
		breakListener = listener;
	}
	
	public void onBreakFnEnter(
			final String fnName,
			final VncFunction fn,
			final VncList args,
			final Env env
	) {
		final Set<BreakpointType> types = breakpoints.get(fnName);
		if (stopOnNextFunction || (types != null && types.contains(FunctionEntry))) {
			onBreakFn(
				new Break(
					fn, 
					args, 
					null, 
					null, 
					env, 
					ThreadLocalMap.getCallStack(), 
					FunctionEntry));
		}
	}
	
	public void onBreakFnExit(
			final String fnName,
			final VncFunction fn,
			final VncList args,
			final VncVal retVal,
			final Env env
	) {
		final Set<BreakpointType> types = breakpoints.get(fnName);
		if (types != null && types.contains(FunctionExit)) {
			onBreakFn(
				new Break(
					fn, 
					args, 
					retVal, 
					null, 
					env, 
					ThreadLocalMap.getCallStack(), 
					FunctionExit));
		}
	}
	
	public void onBreakFnException(
			final String fnName,
			final VncFunction fn,
			final VncList args,
			final Exception ex,
			final Env env
	) {
		final Set<BreakpointType> types = breakpoints.get(fnName);
		if (types != null && types.contains(FunctionException)) {
			onBreakFn(
				new Break(
					fn, 
					args, 
					null, 
					ex, 
					env, 
					ThreadLocalMap.getCallStack(), 
					FunctionException));
		}
	}
	
	public void onBreakFn(final Break br) {
		onBreakEntered(br);
		
		try {
			while(hasBreak()) {
				Thread.sleep(500);
			}
		}
		catch(InterruptedException iex) {
			throw new com.github.jlangch.venice.InterruptedException(
					String.format(
							"Interrupted while waiting for leaving breakpoint "
								+ "in function '%s' (%s).",
							br.getFn().getQualifiedName(),
							br.getBreakpointType()));
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
		stopOnNextFunction = false;
	}

	@Override
	public void leaveBreakForNextFunction() {
		activeBreak = null;
		stopOnNextFunction = true;
	}


	private void onBreakEntered(final Break br) {
		activeBreak = br;
		
		if (breakListener != null) {
			breakListener.onBreak(activeBreak);
		}
	}


	private volatile boolean activated = false;
	private volatile boolean stopOnNextFunction = false;
	private volatile Break activeBreak = null;
	private volatile IBreakListener breakListener = null;
	private final ConcurrentHashMap<String,Set<BreakpointType>> breakpoints = new ConcurrentHashMap<>();
}
