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

import java.util.List;


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
	List<IBreakpoint> getBreakpoints();

	/**
	 * Add a new breakpoint
	 * 
	 * @param breakpoint A breakpoint
	 */
	void addBreakpoint(IBreakpoint breakpoint);

	/**
	 * Removes a breakpoint
	 * 
	 * @param breakpoint The breakpoint to be removed
	 */
	void removeBreakpoint(IBreakpoint breakpoint);

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
	 * @param qualifiedFnName The qualified name of the function or special form
	 * @return Returns <code>true</code> if there is a breakpoint matching the
	 *         qualified name, otherwise <code>false</code>.
	 */
	boolean hasBreak(String qualifiedFnName);
	
	boolean hasBreak(BreakpointLine bp);

	void addBreakListener(IBreakListener listener);

	boolean hasBreak();
	
	Break getBreak();

	void resume();

	void step(StepMode mode);

	boolean isStepPossible(StepMode mode);

}
