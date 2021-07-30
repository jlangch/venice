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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Namespaces;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.concurrent.ThreadLocalMap;


public class DebugAgent implements IDebugAgent {

	public DebugAgent() {
	}
	

	// -------------------------------------------------------------------------
	// Register
	// -------------------------------------------------------------------------

	public static void register(final DebugAgent agent) {
		ThreadLocalMap.setDebugAgent(agent);
	}
	
	public static void unregister() {
		ThreadLocalMap.setDebugAgent(null);
	}
	
	public static DebugAgent current() {
		return ThreadLocalMap.getDebugAgent();
	}
	
	public static boolean isAttached() {
		return ThreadLocalMap.getDebugAgent() != null;
	}
	
	
	@Override
	public void detach() {
		activeBreak = null;
		breakpoints.clear();
		stopNext = StopNext.MatchingFnName;
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
		stopNext = StopNext.MatchingFnName;
	}

	@Override
	public void storeBreakpoints() {
		memorized.putAll(breakpoints);
	}
	
	@Override
	public void restoreBreakpoints() {
		breakpoints.putAll(memorized);
	}



	// -------------------------------------------------------------------------
	// Breaks
	// -------------------------------------------------------------------------

	@Override
	public boolean hasBreak(final String qualifiedName) {
		switch (stopNext) {
			case MatchingFnName: return breakpoints.containsKey(qualifiedName);
			case AnyFunction: return true;
			case AnyNonSystemFunction: return !hasSystemNS(qualifiedName);
			case FunctionReturn: return qualifiedName.equals(stopNextReturnFnName);
			default: return false;
		}
	}

	@Override
	public void addBreakListener(final IBreakListener listener) {
		breakListener = listener;
	}

	public void onBreakLoop(
			final List<VncSymbol> loopBindingNames,
			final VncVal meta,
			final Env env
	) {
		if (isStopOnFunction("loop", FunctionEntry)) {			
			final Break br = new Break(
									new SpecialFormVirtualFunction(
											"loop", 
											VncVector.ofColl(loopBindingNames), 
											meta), 
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

	public void onBreakLet(
			final List<Var> vars,
			final VncVal meta,
			final Env env
	) {
		if (isStopOnFunction("let", FunctionEntry)) {
			Collections.sort(vars, Comparator.comparing(v -> v.getName()));
			final Break br = new Break(
									new SpecialFormVirtualFunction(
											"let", 
											VncVector.ofColl(
												vars.stream()
													.map(v -> v.getName())
													.collect(Collectors.toList())), 
											meta), 
									VncList.ofColl(
										vars.stream()
											.map(v -> v.getVal())
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
	public void resume() {
		clearBreak();
		stopNext = StopNext.MatchingFnName;
	}

	@Override
	public void stepToNextFn() {
		clearBreak();
		stopNext = StopNext.AnyFunction;
	}

	@Override
	public void stepToNextNonSystemFn() {
		clearBreak();
		stopNext = StopNext.AnyNonSystemFunction;
	}
	
	@Override
	public void stepToReturn() {
		if (activeBreak == null) {
			throw new VncException(
					"Cannot step into a function's return if there is no break!");
		}
		else if (activeBreak.isSpecialForm()) {
			throw new VncException(
					"Cannot step into a special form's return!");
		}
		else {
			stopNext = StopNext.FunctionReturn;
			stopNextReturnFnName = activeBreak.getFn().getQualifiedName();
			activeBreak = null;
		}
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
			final BreakpointType bt
	) {
		switch(stopNext) {
			case MatchingFnName:
				return breakpoints.getOrDefault(fnName, EMPTY_BP)
								  .contains(bt);
				
			case AnyFunction:
				return bt == FunctionEntry;
				
			case AnyNonSystemFunction: 
				return bt == FunctionEntry && !hasSystemNS(fnName);
				
			case FunctionReturn: 
				return bt == FunctionExit && fnName.equals(stopNextReturnFnName);
				
			default:
				return false;
		}
	}

	private void waitOnBreak(final Break br) {
		try {
			while(hasBreak()) {
				Thread.sleep(BREAK_SLEEP);
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
	
	void clearBreak() {
		activeBreak = null;
		stopNext = StopNext.MatchingFnName;
		stopNextReturnFnName = null;
	}

	
	private static enum StopNext {
		MatchingFnName,			// stop on function with specified name entry
		AnyFunction,			// stop on next function entry
		AnyNonSystemFunction,	// stop on next non system function entry
		FunctionReturn;			// stop on function return
	}
	
	
	
	private static final long BREAK_SLEEP = 500L;
	private static final Set<BreakpointType> EMPTY_BP = new HashSet<>();

	// simple breakpoint memorization
	private static final ConcurrentHashMap<String,Set<BreakpointType>> memorized =
			new ConcurrentHashMap<>();

	private volatile StopNext stopNext = StopNext.MatchingFnName;
	private volatile String stopNextReturnFnName = null;
	private volatile Break activeBreak = null;
	private volatile IBreakListener breakListener = null;
	private final ConcurrentHashMap<String,Set<BreakpointType>> breakpoints = 
			new ConcurrentHashMap<>();
}
