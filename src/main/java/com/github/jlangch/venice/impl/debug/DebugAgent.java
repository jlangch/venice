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
import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.Namespaces;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncVector;
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
		reset();
	}

	@Override
	public void stop() {
		activated = false;
		reset();
	}

	@Override
	public boolean active() {
		return activated;
	}



	// -------------------------------------------------------------------------
	// Breakpoint management
	// -------------------------------------------------------------------------
	
	@Override
	public Map<String, Set<BreakpointType>> getBreakpoints() {
		return new HashMap<>(breakpoints);
	}

	@Override
	public void addBreakpoint(
			final String qualifiedName, 
			final Set<BreakpointType> types
	) {
		final Set<BreakpointType> copyTypes = new HashSet<>(types);
		
		if (copyTypes.isEmpty()) {
			copyTypes.add(FunctionEntry);
		}
		
		breakpoints.put(qualifiedName, copyTypes);
	}

	@Override
	public void removeBreakpoint(final String qualifiedName) {
		breakpoints.remove(qualifiedName);
	}

	@Override
	public void removeAllBreakpoints() {
		breakpoints.clear();
		stopNextType = StopNextType.MatchingFnName;
		stopNextTypeFlags = null;
	}



	// -------------------------------------------------------------------------
	// Breaks
	// -------------------------------------------------------------------------

	@Override
	public boolean hasBreak(final String qualifiedName) {
		if (activated) {
			switch (stopNextType) {
				case MatchingFnName: return breakpoints.containsKey(qualifiedName);
				case AnyFunction: return true;
				case AnyNonSystemFunction: return !hasSystemNS(qualifiedName);
				default: return false;
			}
		}
		else {
			return false;
		}
	}

	@Override
	public void addBreakListener(final IBreakListener listener) {
		breakListener = listener;
	}

	public void onBreakLoop(
			final List<VncSymbol> loopBindingNames,
			final VncVal loopMeta,
			final Env env
	) {
		if (isStopOnFunction("loop", FunctionEntry)) {			
			final Break br = new Break(
									new SpecialFormVirtualFunction(
											"loop", 
											VncVector.ofColl(loopBindingNames), 
											loopMeta), 
									VncList.ofColl(
											loopBindingNames
											  .stream()
											  .map(s -> env.findLocalVar(s))
											  .map(v -> v == null ? Nil : v.getVal())
											  .collect(Collectors.toList())), 
									null, 
									null, 
									env, 
									ThreadLocalMap.getCallStack(), 
									FunctionEntry);
			notifOnBreak(br);
			waitOnBreak(br);
		}
	}
	
	public void onBreakFnEnter(
			final String fnName,
			final VncFunction fn,
			final VncList args,
			final Env env
	) {
		if (isStopOnFunction(fnName, FunctionEntry)) {
			final Break br = new Break(
									fn, 
									args, 
									null, 
									null, 
									env, 
									ThreadLocalMap.getCallStack(), 
									FunctionEntry);
			
			notifOnBreak(br);
			waitOnBreak(br);
		}
	}
	
	public void onBreakFnExit(
			final String fnName,
			final VncFunction fn,
			final VncList args,
			final VncVal retVal,
			final Env env
	) {
		if (isStopOnFunction(fnName, FunctionExit)) {
			final Break br = new Break(
									fn, 
									args, 
									retVal, 
									null, 
									env, 
									ThreadLocalMap.getCallStack(), 
									FunctionExit);
			
			notifOnBreak(br);
			waitOnBreak(br);
		}
	}
	
	public void onBreakFnException(
			final String fnName,
			final VncFunction fn,
			final VncList args,
			final Exception ex,
			final Env env
	) {
		if (isStopOnFunction(fnName, FunctionException)) {
			final Break br = new Break(
									fn, 
									args, 
									null, 
									ex, 
									env, 
									ThreadLocalMap.getCallStack(), 
									FunctionException);
			
			notifOnBreak(br);
			waitOnBreak(br);
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
	public void leaveBreak(
			final StopNextType type,
			final Set<BreakpointType> flags
	) {
		activeBreak = null;
		stopNextType = type == null ? StopNextType.MatchingFnName : type;
		stopNextTypeFlags = flags == null ? null : new HashSet<>(flags);
	}

	private void reset() {
		activeBreak = null;
		stopNextType = StopNextType.MatchingFnName;
		stopNextTypeFlags = null;
		breakListener = null;
	}
	
	private void notifOnBreak(final Break br) {
		activeBreak = br;
		
		if (breakListener != null) {
			breakListener.onBreak(activeBreak);
		}
	}
	
	private boolean hasSystemNS(final String qualifiedName) {
		final int pos = qualifiedName.indexOf('/');
		return pos < 1 
				? true 
				: Namespaces.isSystemNS(qualifiedName.substring(0, pos));
	}
	
	private boolean isStopOnFunction(
			final String fnName, 
			final BreakpointType breakpointType
	) {
		switch(stopNextType) {
			case MatchingFnName:
				final Set<BreakpointType> types = breakpoints.get(fnName);
				return types != null && types.contains(breakpointType);
				
			case AnyFunction:
				return stopNextTypeFlags == null 
						|| stopNextTypeFlags.contains(breakpointType);
				
			case AnyNonSystemFunction: 
				return !hasSystemNS(fnName)
							&& (stopNextTypeFlags == null 
									|| stopNextTypeFlags.contains(breakpointType));
				
			default:
				return false;
		}
	}

	private void waitOnBreak(final Break br) {
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

	
	private volatile boolean activated = false;
	private volatile StopNextType stopNextType = StopNextType.MatchingFnName;
	private volatile Set<BreakpointType> stopNextTypeFlags = null;
	private volatile Break activeBreak = null;
	private volatile IBreakListener breakListener = null;
	private final ConcurrentHashMap<String,Set<BreakpointType>> breakpoints = new ConcurrentHashMap<>();
}
