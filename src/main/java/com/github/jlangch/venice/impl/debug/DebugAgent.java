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

import static com.github.jlangch.venice.impl.debug.BreakpointScope.FunctionCall;
import static com.github.jlangch.venice.impl.debug.BreakpointScope.FunctionEntry;
import static com.github.jlangch.venice.impl.debug.BreakpointScope.FunctionException;
import static com.github.jlangch.venice.impl.debug.BreakpointScope.FunctionExit;
import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.Namespaces;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.concurrent.ThreadLocalMap;
import static com.github.jlangch.venice.impl.util.StringUtil.*;


public class DebugAgent implements IDebugAgent {

	public DebugAgent() {
	}
	

	// -------------------------------------------------------------------------
	// Register Agent
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
	

	
	// -------------------------------------------------------------------------
	// Lifecycle
	// -------------------------------------------------------------------------
	
	@Override
	public void detach() {
		clearAll();
	}

	
	
	// -------------------------------------------------------------------------
	// Breakpoint management
	// -------------------------------------------------------------------------
	
	@Override
	public List<IBreakpoint> getBreakpoints() {
		final ArrayList<IBreakpoint> list = new ArrayList<>();

		list.addAll(
				breakpoints
					.keySet()
					.stream()
					.filter(b -> b instanceof BreakpointFn)
					.map(b -> (BreakpointFn)b)
					.sorted(Comparator
								.comparing(BreakpointFn::getQualifiedFnName))
					.collect(Collectors.toList()));

		list.addAll(
				breakpoints
					.keySet()
					.stream()
					.filter(b -> b instanceof BreakpointLine)
					.map(b -> (BreakpointLine)b)
					.sorted(Comparator
								.comparing(BreakpointLine::getFile)
								.thenComparing(BreakpointLine::getLineNr))
					.collect(Collectors.toList()));

		return list;
	}

	@Override
	public void addBreakpoint(final IBreakpoint breakpoint) {
		if (breakpoint == null) {
			throw new IllegalArgumentException("A breakpoint must not be null");
		}
		
		breakpoints.put(breakpoint, breakpoint);
	}

	@Override
	public void removeBreakpoint(IBreakpoint breakpoint) {
		if (breakpoint != null) {
			breakpoints.remove(breakpoint);
		}
	}

	@Override
	public void removeAllBreakpoints() {
		clearAll();
	}
	
	@Override
	public void skipBreakpoints(final boolean skip) {
		skipBreakpoints = skip;
	}

	@Override
	public boolean isSkipBreakpoints() {
		return skipBreakpoints;	
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
	public boolean hasBreakpointFor(final String qualifiedFnName) {
		final boolean inBreak = activeBreak != null;
		
		switch (stepMode) {
			case Disabled: 
				return !skipBreakpoints && breakpoints.containsKey(
											new BreakpointFn(qualifiedFnName));
				
			case StepToNextFunction: 
				return inBreak;
				
			case StepToNextNonSystemFunction: 
				return inBreak && !hasSystemNS(qualifiedFnName);
				
			case StepToFunctionReturn: 
				return inBreak && qualifiedFnName.equals(stepBoundToFnName);
				
			case StepIntoFunction: 
				return inBreak && qualifiedFnName.equals(stepBoundToFnName);
				
			case StepToNextLine:
				return false;
				
			default: 
				return false;
		}
	}
	
	@Override
	public boolean hasBreakpointFor(final BreakpointLine bp) {
		return isStopOnLineNr(bp);
	}

	@Override
	public void addBreakListener(final IBreakListener listener) {
		breakListener = listener;
	}

	public void onBreakLineNr(
			final BreakpointLine bp,
			final VncFunction fn,
			final VncList args,
			final Env env
	) {
		if (isStopOnLineNr(bp)) {
			final Break br = new Break(
									bp,
									fn,
									args,
									env,
									ThreadLocalMap.getCallStack(),
									FunctionCall);
			
			notifyOnBreak(br);
			waitOnBreak(br);
		}
	}

	public void onBreakLoop(
			final List<VncSymbol> loopBindingNames,
			final VncVal meta,
			final Env env
	) {
		if (isStopOnFunction("loop", FunctionEntry)) {
			final Break br = new Break(
									new BreakpointFn("loop"),
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
									env,
									ThreadLocalMap.getCallStack(),
									FunctionEntry);
			notifyOnBreak(br);
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
									new BreakpointFn("let"),
									new SpecialFormVirtualFunction("let", vars, meta), 
									VncList.ofColl(
										vars.stream()
											.map(v -> v.getVal())
											.collect(Collectors.toList())),
									env, 
									ThreadLocalMap.getCallStack(),
									FunctionEntry);
			notifyOnBreak(br);
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
									new BreakpointFn(fnName),
									fn,
									args,
									env,
									ThreadLocalMap.getCallStack(),
									FunctionEntry);
			
			notifyOnBreak(br);
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
									new BreakpointFn(fnName),
									fn,
									args,
									retVal,
									null,
									env,
									ThreadLocalMap.getCallStack(),
									FunctionExit);
			
			notifyOnBreak(br);
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
									new BreakpointFn(fnName),
									fn,
									args,
									null,
									ex,
									env,
									ThreadLocalMap.getCallStack(),
									FunctionException);

			notifyOnBreak(br);
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
	}

	@Override
	public boolean step(final StepMode mode) {
		if (!isStepPossible(mode)) {
			return false;
		}
		
		final Break br = activeBreak;


		switch(mode) {
			case StepToNextFunction:
				stepMode = StepMode.StepToNextFunction;
				stepBoundToFnName = null;
				stepFrom = null;
				break;
	
			case StepToNextNonSystemFunction:
				stepMode = StepMode.StepToNextNonSystemFunction;
				stepBoundToFnName = null;
				stepFrom = null;
				break;
				
			case StepIntoFunction:
				if (br.getBreakpointScope() == FunctionCall) {
					stepMode = StepMode.StepIntoFunction;
					stepBoundToFnName = br.getFn().getQualifiedName();
					// keep 'stepFrom'
				}
				else {
					stepMode = StepMode.Disabled;
					stepBoundToFnName = null;
					stepFrom = null;
				}
				break;
				
			case StepToFunctionReturn:
				if (br.getBreakpointScope() == FunctionCall) {
					stepMode = StepMode.StepToFunctionReturn;
					stepBoundToFnName = br.getFn().getQualifiedName();
					// keep 'stepFrom'
				}
				else if (br.getBreakpointScope() == FunctionEntry) {
					stepMode = StepMode.StepToFunctionReturn;
					stepBoundToFnName = br.getFn().getQualifiedName();
					// keep 'stepFrom'
				}
				else {
					stepMode = StepMode.Disabled;
					stepBoundToFnName = null;
					stepFrom = null;
				}
				break;
				
			case StepToNextLine:
				if (br.isBreakInLineNr()) {
					stepMode = StepMode.StepToNextLine;
					stepBoundToFnName = null;
					stepFrom = br;
				}
				else if (stepFrom != null && stepFrom.isBreakInLineNr()) {
					stepMode = StepMode.StepToNextLine;
					stepBoundToFnName = null;
					stepFrom = null;
				}
				else {
					stepMode = StepMode.Disabled;
					stepBoundToFnName = null;
					stepFrom = null;
				}
				break;
				
			case Disabled:
			default:
				stepMode = StepMode.Disabled;
				stepBoundToFnName = null;
				stepFrom = null;
				break;
		}

		activeBreak = null;

		return true;
	}

	@Override
	public boolean isStepPossible(final StepMode mode) {
		final Break br = activeBreak;
		
		if (mode == null || br == null) {
			return false;
		}
		
		switch(mode) {
			case StepToNextFunction:
				return true;
	
			case StepToNextNonSystemFunction:
				return true;
				
			case StepIntoFunction:
				return br.getBreakpointScope() == FunctionCall;
				
			case StepToFunctionReturn:
				return !br.isBreakInSpecialForm() 
							&& (br.getBreakpointScope() == FunctionCall
									|| br.getBreakpointScope() == FunctionEntry);
				
			case StepToNextLine:
				return br.isBreakInLineNr() 
							|| (stepFrom != null && stepFrom.isBreakInLineNr());
				
			case Disabled:
				return true;
					
			default:
				return false;
		}		
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		
		sb.append(String.format(
					"Active break:          %s\n", 
					activeBreak == null
						?  "no" 
						: "Break\n" + indent(activeBreak.toString(), 25)));
		
		sb.append(String.format(
					"Step mode:             %s\n", 
					stepMode));
		
		sb.append(String.format(
					"Step bound to Fn name: %s\n", 
					stepBoundToFnName == null ? "-" : stepBoundToFnName));
		
		sb.append(String.format(
					"Step from break:       %s\n", 
					stepFrom == null 
						? "-" 
						: "Break\n" + indent(stepFrom.toString(), 25)));
		
		sb.append(String.format(
					"Skip breakpoints:      %s", 
					skipBreakpoints ? "yes" : "no"));
		
		return sb.toString();
	}

	private void notifyOnBreak(final Break br) {
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
	
	private boolean isStopOnLineNr(final BreakpointLine bp) {
		if (bp == null) {
			return false;
		}
		
		if (stepFrom == null) {
			return !skipBreakpoints && breakpoints.containsKey(bp);
		}
		else if (stepFrom.isBreakInLineNr()) {
			// handles step line in same file on another line
			final BreakpointLine b = (BreakpointLine)stepFrom.getBreakpoint();
			return bp.getFile().equals(b.getFile()) && bp.getLineNr() != b.getLineNr();
		}
		else {
			return false;
		}
	}
	
	private boolean isStopOnFunction(
			final String fnName, 
			final BreakpointScope bt
	) {
		switch(stepMode) {
			case Disabled:
				if (skipBreakpoints) {
					return false;
				}
				else {
					final IBreakpoint bp = breakpoints.get(new BreakpointFn(fnName));
					return bp != null 
							&& bp instanceof BreakpointFn
							&& ((BreakpointFn)bp).hasScope(bt);
				}

			case StepToNextFunction:
				return bt == FunctionEntry;

			case StepToNextNonSystemFunction: 
				return bt == FunctionEntry && !hasSystemNS(fnName);

			case StepToFunctionReturn: 
				return bt == FunctionExit && fnName.equals(stepBoundToFnName);

			case StepIntoFunction: 
				return bt == FunctionEntry && fnName.equals(stepBoundToFnName);

			case StepToNextLine:
				return false;

			default:
				return false;
		}
	}

	private void waitOnBreak(final Break br) {
		try {
			while(hasBreak()) {
				Thread.sleep(BREAK_LOOP_SLEEP_MILLIS);
			}
		}
		catch(InterruptedException iex) {
			throw new com.github.jlangch.venice.InterruptedException(
					String.format(
							"Interrupted while waiting for leaving breakpoint "
								+ "in function '%s' (%s).",
							br.getFn().getQualifiedName(),
							br.getBreakpointScope()));
		}
		finally {
			activeBreak = null;
		}
	}
	
	private void clearBreak() {
		activeBreak = null;
		stepMode = StepMode.Disabled;
		stepBoundToFnName = null;
		stepFrom = null;
	}

	private void clearAll() {
		activeBreak = null;
		stepMode = StepMode.Disabled;
		stepBoundToFnName = null;
		stepFrom = null;
		skipBreakpoints = false;
		breakpoints.clear();
	}



	private static final long BREAK_LOOP_SLEEP_MILLIS = 500L;

	// simple breakpoint memorization
	private static final ConcurrentHashMap<IBreakpoint,IBreakpoint> memorized =
			new ConcurrentHashMap<>();

	private volatile Break activeBreak = null;
	private volatile StepMode stepMode = StepMode.Disabled;
	private volatile String stepBoundToFnName = null;
	private volatile Break stepFrom = null;
	private volatile boolean skipBreakpoints = false;

	private volatile IBreakListener breakListener = null;
	private final ConcurrentHashMap<IBreakpoint,IBreakpoint> breakpoints =
			new ConcurrentHashMap<>();
}
