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

import java.util.List;

import com.github.jlangch.venice.impl.debug.breakpoint.BreakpointFn;
import com.github.jlangch.venice.impl.debug.breakpoint.BreakpointFnRef;
import com.github.jlangch.venice.impl.debug.util.StepValidity;


public interface IDebugAgent {

	/**
	 * Detach the debugger from Venice
	 */
	void detach();

	
	
	// -------------------------------------------------------------------------
	// Breakpoint management
	// -------------------------------------------------------------------------

	/**
	 * @return all the registered breakpoints
	 */
	List<BreakpointFn> getBreakpoints();

	/**
	 * Add a new breakpoint
	 * 
	 * @param breakpoint A breakpoint
	 */
	void addBreakpoint(BreakpointFn breakpoint);

	/**
	 * Add a new breakpoints
	 * 
	 * @param breakpoints A list of breakpoints
	 */
	void addBreakpoints(List<BreakpointFn> breakpoints);

	/**
	 * Removes a breakpoint
	 * 
	 * @param breakpoint The breakpoint to be removed
	 */
	void removeBreakpoint(BreakpointFn breakpoint);

	/**
	 * Removes breakpoints
	 * 
	 * @param breakpoints The breakpoints to be removed
	 */
	void removeBreakpoints(List<BreakpointFn> breakpoints);

	/**
	 * Remove all breakpoints
	 */
	void removeAllBreakpoints();
	
	/**
	 * Temporarily skip/unskip all breakpoints
	 * 
	 * @param skip if <code>true</code> skip else unskip
	 */
	void skipBreakpoints(boolean skip);
	
	/**
	 * @return <code>true</code> if the breakpoints are temporarily skipped
	 * 			else <code>false</code>
	 */
	boolean isSkipBreakpoints();

	
	void storeBreakpoints();
	
	void restoreBreakpoints();
	


	// -------------------------------------------------------------------------
	// Breaks
	// -------------------------------------------------------------------------

	/**
	 * Checks if there is a breakpoint matching the qualified name
	 *  
	 * @param bpRef The a breakpoint reference
	 * @return Returns <code>true</code> if there is a breakpoint matching the
	 *         qualified name, otherwise <code>false</code>.
	 */
	boolean hasBreakpointFor(BreakpointFnRef bpRef);

	/**
	 * Add a breakpoint listener that is call from the debug agent whenever
	 * a breakpoint is reached.
	 * 
	 * <p>Note: As of now not more than one listener can be attached to the
	 * debug agent.
	 * 
	 * @param listener A listener. Passing <code>null</code> will deactivate 
	 * 					listening.
	 */
	void addBreakListener(IBreakListener listener);

	/**
	 * @return <code>true</code> if the debugger is currently in a break.
	 */
	boolean hasActiveBreak();
	
	/**
	 * @return Returns the break if the debugger is currently in a break
	 *         otherwise <code>null</code>.
	 */
	Break getActiveBreak();

	/**
	 * switches the active break
	 * 
	 * @param index the index of the break in the range [1..n]
	 * @return the active break or <code>null</code> if the break could not
	 * 		   be switched.
	 */
	Break switchActiveBreak(int index);

	/**
	 * @return all breaks
	 */
	List<Break> getAllBreaks();

	/**
	 * Clears the current break and resumes processing for the next breakpoint.
	 */
	void clearBreaks();

	/**
	 * Resumes processing for the next breakpoint.
	 */
	void resume();

	/**
	 * Enters a step mode.
	 * 
	 * @param mode A step mode
	 * @return The validity if the step mode could be entered or the reason when
	 *         not
	 */
	StepValidity step(StepMode mode);

	/**
	 * Checks a step mode is possible in the current debugger context.
	 * 
	 * @param mode A step mode
	 * @return The validity if the step mode can be entered or the reason when
	 *         not
	 */
	StepValidity isStepPossible(StepMode mode);

}
