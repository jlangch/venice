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
package com.github.jlangch.venice.impl.debug.agent;

import static com.github.jlangch.venice.impl.debug.agent.StepMode.StepIntoFunction;
import static com.github.jlangch.venice.impl.debug.agent.StepMode.StepToFunctionReturn;
import static com.github.jlangch.venice.impl.debug.agent.StepMode.StepToNextFunction;
import static com.github.jlangch.venice.impl.debug.agent.StepMode.StepToNextNonSystemFunction;
import static com.github.jlangch.venice.impl.debug.breakpoint.FunctionScope.FunctionCall;
import static com.github.jlangch.venice.impl.debug.breakpoint.FunctionScope.FunctionEntry;
import static com.github.jlangch.venice.impl.debug.breakpoint.FunctionScope.FunctionException;
import static com.github.jlangch.venice.impl.debug.breakpoint.FunctionScope.FunctionExit;
import static com.github.jlangch.venice.impl.types.Constants.Nil;
import static com.github.jlangch.venice.impl.util.StringUtil.indent;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.Namespaces;
import com.github.jlangch.venice.impl.debug.breakpoint.AncestorSelector;
import com.github.jlangch.venice.impl.debug.breakpoint.AncestorType;
import com.github.jlangch.venice.impl.debug.breakpoint.BreakpointFn;
import com.github.jlangch.venice.impl.debug.breakpoint.BreakpointFnRef;
import com.github.jlangch.venice.impl.debug.breakpoint.FunctionScope;
import com.github.jlangch.venice.impl.debug.breakpoint.Selector;
import com.github.jlangch.venice.impl.debug.util.SpecialFormVirtualFunction;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.concurrent.ThreadLocalMap;
import com.github.jlangch.venice.impl.util.CallStack;


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
	public List<BreakpointFn> getBreakpoints() {
		return breakpoints
					.values()
					.stream()
					.sorted()
					.collect(Collectors.toList());
	}

	@Override
	public void addBreakpoint(final BreakpointFn breakpoint) {
		if (breakpoint == null) {
			throw new IllegalArgumentException("A breakpoint must not be null");
		}
		
		final BreakpointFnRef ref = breakpoint.getBreakpointRef();
		breakpoints.remove(ref);
		breakpoints.put(ref, breakpoint);
	}
	
	@Override
	public void addBreakpoints(final List<BreakpointFn> breakpoints) {
		if (breakpoints != null) {
			breakpoints.forEach(b -> addBreakpoint(b));
		}
	}

	@Override
	public void removeBreakpoint(final BreakpointFn breakpoint) {
		if (breakpoint != null) {
			breakpoints.remove(breakpoint.getBreakpointRef());
		}
	}
	
	@Override
	public void removeBreakpoints(final List<BreakpointFn> breakpoints) {
		if (breakpoints != null) {
			breakpoints.forEach(b -> removeBreakpoint(b));
		}
	}

	@Override
	public void removeAllBreakpoints() {
		breakpoints.clear();
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
	public boolean hasBreakpointFor(final BreakpointFnRef bpRef) {
		if (bpRef instanceof BreakpointFnRef) {
			switch (step.mode()) {
				case SteppingDisabled: 
					return !skipBreakpoints && breakpoints.containsKey(bpRef);
					
				case StepToNextFunction: 
				case StepToNextNonSystemFunction: 
				case StepToFunctionReturn: 
				case StepIntoFunction: 
					return true;
					
				default: 
					return false;
			}
		}
		else {
			switch(step.mode()) {
				case SteppingDisabled:
					return !skipBreakpoints && breakpoints.containsKey(bpRef);
		
				case StepToNextFunction:
				case StepToNextNonSystemFunction: 
				case StepToFunctionReturn: 
				case StepIntoFunction: 
					// stop on line nr is suspended while stepping
					return false;
		
				default:
					return false;
			}
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
									new BreakpointFnRef("loop"),
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
									new BreakpointFnRef("let"),
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
									new BreakpointFnRef(fnName),
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
									new BreakpointFnRef(fnName),
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
									new BreakpointFnRef(fnName),
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
				step = new Step(StepToNextFunction);
				break;
	
			case StepToNextNonSystemFunction:
				step = new Step(StepToNextNonSystemFunction);
				break;
				
			case StepIntoFunction:
				if (br.isInScope(FunctionCall)) {
					step = new Step(
								StepIntoFunction,
								br.getFn().getQualifiedName(),
								step.fromBreak());
				}
				else {
					step = step.clear();
				}
				break;
				
			case StepToFunctionReturn:
				if (br.isInScope(FunctionCall, FunctionEntry)) {
					step = new Step(
								StepToFunctionReturn,
								br.getFn().getQualifiedName(),
								step.fromBreak());
				}
				else {
					step = step.clear();
				}
				break;
				
			case SteppingDisabled:
			default:
				step = step.clear();
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
				return br.isInScope(FunctionCall);
				
			case StepToFunctionReturn:
				return !br.isBreakInSpecialForm() 
							&& br.isInScope(FunctionCall, FunctionEntry);

			case SteppingDisabled:
				return true;
					
			default:
				return false;
		}		
	}
	
	@Override
	public void clearBreak() {
		step = step.clear();
		activeBreak = null;
	}

	@Override
	public String toString() {
		final Step stepTmp = step;

		final StringBuilder sb = new StringBuilder();
		
		sb.append(String.format(
					"Active break:          %s\n", 
					activeBreak == null
						?  "no" 
						: "Break\n" + indent(activeBreak.toString(), 25)));
		
		sb.append(String.format(
					"Step mode:             %s\n", 
					stepTmp.mode()));
		
		sb.append(String.format(
					"Step bound to Fn name: %s\n", 
					stepTmp.boundToFnName() == null ? "-" : stepTmp.boundToFnName()));
		
		sb.append(String.format(
					"Step from break:       %s\n", 
					stepTmp.fromBreak() == null 
						? "-" 
						: "Break\n" + indent(stepTmp.fromBreak().toString(), 25)));
		
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
	
	private boolean isStopOnFunction(
			final String fnName, 
			final FunctionScope scope
	) {
		final Step stepTmp = step;  // be immune to changing step var

		switch(stepTmp.mode()) {
			case SteppingDisabled:
				if (skipBreakpoints) {
					return false;
				}
				else {
					final BreakpointFn bp = breakpoints.get(new BreakpointFnRef(fnName));
					return matchesWithBreakpoint(fnName, scope, bp);
				}

			case StepToNextFunction:
				return scope == FunctionEntry;

			case StepToNextNonSystemFunction: 
				return scope == FunctionEntry && !hasSystemNS(fnName);

			case StepToFunctionReturn: 
				return scope == FunctionExit && stepTmp.isBoundToFnName(fnName);

			case StepIntoFunction: 
				return scope == FunctionEntry && stepTmp.isBoundToFnName(fnName);

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

	private void clearAll() {
		step = step.clear();
		skipBreakpoints = false;
		breakpoints.clear();
		activeBreak = null;
	}

	private boolean matchesWithBreakpoint(
			final String fnName, 
			final FunctionScope scope,
			final BreakpointFn bp
	) {
		if (bp != null) {
			for(Selector s : bp.getSelectors()) {
				// match scope
				if (s.hasScope(scope)) {
					// The scope matches!
					AncestorSelector as = s.getAncestorSelector();
					if (as == null) {
						return true;
					}
					else {
						// match ancestor with callstack
						final CallStack callStack = ThreadLocalMap.get().getCallStack_();
						if (as.getType() == AncestorType.Nearest) {
							if (callStack.hasNearestAncestor(fnName)) {
								return true;
							}
						}
						else {
							if (callStack.hasAnyAncestor(fnName)) {
								return true;
							}
						}
					}
				}
			}
		}
		
		return false;
	}
	
	

	private static final long BREAK_LOOP_SLEEP_MILLIS = 500L;

	// simple breakpoint memorization
	private static final ConcurrentHashMap<BreakpointFnRef,BreakpointFn> memorized =
			new ConcurrentHashMap<>();

	private volatile Break activeBreak = null;
	private volatile Step step = new Step();
	private volatile boolean skipBreakpoints = false;

	private volatile IBreakListener breakListener = null;
	private final ConcurrentHashMap<BreakpointFnRef,BreakpointFn> breakpoints =
			new ConcurrentHashMap<>();
}
