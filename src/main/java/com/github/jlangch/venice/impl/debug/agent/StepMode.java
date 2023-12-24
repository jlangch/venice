/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
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
public enum StepMode {

    // Step to any next function or next level
    StepToAny,

    // Step to next function entry
    StepToNextFunction,

    // Step to next non system function entry
    StepToNextNonSystemFunction,

    // Step over the current function
    StepOverFunction,
    StepOverFunction_NextCall,

    // Step in the next function at call level
    StepToNextFunctionCall,

    // Step in the current function to the entry: call -> entry
    // Steps over all functions called for evaluating the functions args
    StepToFunctionEntry,

    // Step in the current function to the exit: (call,entry) -> exit
    // Steps over all functions called for evaluating the functions args or body
    StepToFunctionExit,

    // Disable stepping, just stop on function breakpoints
    SteppingDisabled;

}
