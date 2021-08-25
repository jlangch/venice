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
package com.github.jlangch.venice.impl.repl;


public class ReplDebugHelp {

	public final static String HELP =
		  //+------------------------------------------------------------------------------+
			"Venice debugger\n" +
			"\n" +
			"The debugger can break functions at 4 levels:\n" +
			"  call:        breaks before the passed parameters are evaluated. The\n" +
			"               unevaluated function parameters are available for inspection.\n" +
			"  entry:       breaks right after the passed parameters have been evaluated.\n" +
			"               The evaluated function parameters are available for inspection.\n" +
			"  exception:   breaks on catching an exception within the function's body. The\n" +
			"               exception and the evaluated functions parameters are available\n" +
			"               for inspection.\n" +
			"  exit:        breaks before returning from the function. The return value and\n" +
			"               the evaluated function parameters are available for inspection.\n" +
			"\n" +
			"Commands:\n" +
			"  !attach      Attach the debugger to the REPL\n" +
			"  !detach      Detach the debugger from the REPL\n" +
			"  !terminate   Terminate a running debug session. Sends an interrupt to the\n" +
			"               script under debugging.\n" +
			"  !info        Print detail info on the current debug session\n" +
			"  !breakpoint  Manage breakpoints\n" +
			"               o Add one or multiple breakpoints\n" +
			"                  !breakpoint add n, n*\n" +
			"                  E.g.: !breakpoint add foo/gauss\n" +
			"                        !breakpoint add foo/gauss count\n" +
			"                        Ancestor selectors:\n" +
			"                          direct ancestor: !breakpoint add foo/gauss > filter\n" +
			"                          any ancestor:    !breakpoint add foo/gauss + filter\n" +
			"               o Remove one or multiple breakpoints\n" +
			"                  !breakpoint remove n, n*\n" +
			"                  E.g.: !breakpoint remove foo/gauss\n" +
			"               o Temporarily skip/unskip all breakpoints\n" +
			"                  !breakpoint skip\n" +
			"                  !breakpoint unksip\n" +
			"                  !breakpoint skip?\n" +
			"               o List breakpoints\n" +
			"                  !breakpoint list\n" +
			"                  E.g.: !breakpoint list\n" +
			"               Short form: !b ...\n" +
			"  !resume      Resume from current break\n" +
			"               Short form: !r\n" +
			"  !step-any    Step to the next available break at one of the four break\n" +
			"               levels within the current or the next function whatever is first.\n" +
			"               Short form: !sa\n" +
			"  !step-next   Step to next function at entry level\n" +
			"               Short form: !sn\n" +
			"  !step-over   Step over the current function to next function entry.\n" +
			"               Implicitely steps over functions involved with function\n" +
			"               parameter evaluation.\n" +
			"               Short form: !so\n" +
			"  !step-call   Step to next function at call level\n" +
			"               Short form: !sc\n" +
			"  !step-entry  Step to the entry level of the current function\n" +
			"               Short form: !se\n" +
			"  !step-exit   Step to the exit level of the current function\n" +
			"               Short form: !sx\n" +
			"  !break?      Checks if the debugger is in a break or not\n" +
			"  !params      Print the function's parameters\n" +
			"               Short form: !p\n" +
			"  !retval      Print the function's return value\n" +
			"               Short form: !ret\n" +
			"  !ex          Print the function's exception\n" +
			"  !locals x    Print the local vars from the level x. The level is optional\n" +
			"               and defaults to the top level.\n" +
			"               Short form: !l\n" +
			"  !callstack   Print the current callstack\n" +
			"  form         Runs a Venice form in the current break context. Useful to\n" +
			"               inspect parameters, return values, or global/local vars.\n" +
			"               Note: Debugging is suspended for the evaluating the form!\n" +
			"               E.g.:  (first param1)";

}
