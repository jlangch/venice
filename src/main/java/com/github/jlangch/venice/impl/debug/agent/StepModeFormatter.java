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


/**
 * Defines the stepping modes for the debugger
 */
public class StepModeFormatter {

	public static String format(final StepMode mode) {
		if (mode == null) {
			return "undefined";
		}
		
		switch(mode) {
			case StepToAny:
				return "step to any";
				
			case StepToNextFunction:
				return "step to next function";
	
			case StepToNextNonSystemFunction:
				return "step to next non system function";
	
			case StepOverFunction:
				return "step over function";
				
			case StepOverFunction_NextCall:
				return "step over function";
			
			case StepToNextFunctionCall:
				return "step to next function call level";
	
			case StepToFunctionEntry:
				return "step to entry level in current function";
	
			case StepToFunctionExit:
				return "step to exit level in current function";
				
			case SteppingDisabled:
				return "disabled";
				
			default:
				throw new RuntimeException("Unhandled mode " + mode);
		
		}
	}

}
