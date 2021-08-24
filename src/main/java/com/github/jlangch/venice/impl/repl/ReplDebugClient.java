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

import static com.github.jlangch.venice.impl.debug.breakpoint.BreakpointParser.parseBreakpoints;
import static com.github.jlangch.venice.impl.util.CollectionUtil.drop;
import static com.github.jlangch.venice.impl.util.CollectionUtil.first;
import static com.github.jlangch.venice.impl.util.CollectionUtil.second;
import static com.github.jlangch.venice.impl.util.StringUtil.trimToEmpty;
import static com.github.jlangch.venice.impl.util.StringUtil.truncate;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.jlangch.venice.ParseError;
import com.github.jlangch.venice.impl.Destructuring;
import com.github.jlangch.venice.impl.debug.agent.Break;
import com.github.jlangch.venice.impl.debug.agent.IDebugAgent;
import com.github.jlangch.venice.impl.debug.agent.StepMode;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncCollection;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.collections.VncSet;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.CallFrame;


/**
 * REPL debugger client
 * 
 * <p>A typical debug session looks like:
 * <pre>
 *   venice> (defn sum [x y] (+ x y))
 *   venice> !attach
 *   debug> !breakpoint add user/sum
 *   debug> (sum 6 7)
 *   Stopped in function user/sum (user: line 1, col 7) at entry
 *   debug> !params
 *   Break in function user/sum at entry.
 *   Arguments passed to function user/sum:
 *   x -> 6
 *   y -> 7
 *   debug> !step-exit
 *   Stopped in function user/sum (user: line 1, col 7) at exit
 *   debug> !params
 *   Break in function user/sum at exit.
 *   Arguments passed to function user/sum:
 *   x -> 6
 *   y -> 7
 *   [return] -> 13
 *   debug> !resume
 * </pre>
 */
public class ReplDebugClient {

	public ReplDebugClient(
			final IDebugAgent agent,
			final TerminalPrinter printer,
			final Thread replThread
	) {
		this.agent = agent;
		this.printer = printer;
		this.replThread = replThread;

		agent.addBreakListener(this::breakpointListener);
	}

	
	public static void pringHelp(final TerminalPrinter printer) {
		printer.println("stdout", HELP);
	}
	
	public static boolean isDebugCommand(final String cmd) {
		return DEBUG_COMMANDS.contains(cmd);
	}

	public void handleCommand(final String cmdLine) {
		final List<String> params = Arrays.asList(cmdLine.split(" +"));

		final String cmd = trimToEmpty(first(params));
		switch(cmd) {
			case "info":
			case "i":
				printer.println("stdout", agent.toString());
				break;

			case "breakpoint":
			case "b":
				handleBreakpointCmd(drop(params, 1));
				break;

			case "resume":
			case "r":
				agent.resume();
				break;
				
			case "step":
			case "s":
				if (!agent.isStepPossible(StepMode.StepToAny)) {
					printErrorSteppingNotPossible("to any");
					return;
				}
				agent.step(StepMode.StepToAny);
				break;
				
			case "step-next":
			case "sn":
				if (!agent.isStepPossible(StepMode.StepToNextFunction)) {
					printErrorSteppingNotPossible("into next");
					return;
				}
				agent.step(StepMode.StepToNextFunction);
				break;
				
			case "step-next-call":
			case "snc":
				if (!agent.isStepPossible(StepMode.StepToNextFunctionCall)) {
					printErrorSteppingNotPossible("into next");
					return;
				}
				agent.step(StepMode.StepToNextFunctionCall);
				break;
				
			case "step-next-":
			case "sn-":
				if (!agent.isStepPossible(StepMode.StepToNextNonSystemFunction))  {
					printErrorSteppingNotPossible("into next");
					return;
				}
				agent.step(StepMode.StepToNextNonSystemFunction);
				break;
				
			case "step-over":
			case "so":
				if (!agent.isStepPossible(StepMode.StepOverFunction)) {
					printErrorSteppingNotPossible("over");
					return;
				}
				agent.step(StepMode.StepOverFunction);
				break;
				
			case "step-entry":
			case "se":
				if (!agent.isStepPossible(StepMode.StepToFunctionEntry))  {
					printErrorSteppingNotPossible("to entry of");
					return;
				}
				println("Stepping to entry of function %s ...",
						agent.getBreak().getFn().getQualifiedName());
				agent.step(StepMode.StepToFunctionEntry);
				break;
				
			case "step-exit":
			case "sx":
				if (!agent.isStepPossible(StepMode.StepToFunctionExit)) {
					printErrorSteppingNotPossible("to exit of");
					return;
				}
				println("Stepping to exit of function %s ...",
						agent.getBreak().getFn().getQualifiedName());
				agent.step(StepMode.StepToFunctionExit);
				break;
				
			case "break?": 
			case "b?":
				isBreak();
				break;
				
			case "callstack": 
			case "cs":
				callstack();
				break;
				
			case "params": 
			case "p":
				params(drop(params, 1));
				break;
				
			case "locals":
			case "l":
				locals(second(params));
				break;
				
			case "local": 
				local(second(params));
				break;
				
			case "global":
				global(second(params));
				break;
				
			case "retval":
			case "ret":
				retval();
				break;
				
			case "ex":
				ex();
				break;

			case "help":
			case "?":
				println(HELP);
				break;
				
			default:
				printlnErr("Invalid command '%s'. Use '!help' for help.", cmd);
				break;
		}
	}
	
	private void handleBreakpointCmd(final List<String> params) {
		if (params.isEmpty())  {
			printBreakpoints();
		}
		else {
			try {
				final String cmd = trimToEmpty(params.get(0));
				switch(cmd) {
					case "add":
						agent.addBreakpoints(parseBreakpoints(drop(params, 1)));
						break;
						
					case "remove":
						agent.removeBreakpoints(parseBreakpoints(drop(params, 1)));
						break;
						
					case "clear":
						agent.removeAllBreakpoints();
						break;
						
					case "skip":
						agent.skipBreakpoints(true);
						break;
						
					case "unskip":
						agent.skipBreakpoints(false);
						break;
						
					case "skip?":
						println("Skip breakpoints: %s", agent.isSkipBreakpoints());
						break;
						
					case "list":
						printBreakpoints();
						break;
						
					default:
						printlnErr(
							"Invalid breakpoint command '%s'. Use one of "
								+ "'add', 'remove', 'clear', 'skip', 'unskip', "
								+ "or 'list'.", 
							cmd);
						break;
				}
			}
			catch(ParseError ex) {
				printer.println("error", ex.getMessage());
			}
		}
	}
	
	private void isBreak() {
		if (!agent.hasBreak()) {
			println("Not in a debug break!");
		}
		else {
			println(formatStop(agent.getBreak()));
		}
	}
	
	private void callstack() {
		if (!agent.hasBreak()) {
			println("Not in a debug break!");
			return;
		}
		
		final Break br = agent.getBreak();

		println(formatBreak(br));
		println(br.getCallStack().toString());
	}
	
	private void printBreakpoints() {
		if (agent.getBreakpoints().isEmpty()) {
			 printer.println("stdout", "No breakpoints defined!");
		}
		else {
			final boolean skip = agent.isSkipBreakpoints();
			agent.getBreakpoints()
				 .stream()
				 .forEach(b ->
					 b.getSelectors().forEach(s ->
						 printer.println(
							"stdout",
							String.format(
								"  %s%s", 
								skip ? "[-] " : "", 
										s.formatForBaseFn(
											b.getQualifiedFnName(),
											true)))));
		}
	}
	
	private void params(final List<String> params) {
		if (!agent.hasBreak()) {
			println("Not in a debug break!");
			return;
		}

		final Break br = agent.getBreak();

		println(formatBreak(br));

		if (br.isBreakInNativeFn()) {
			println(renderNativeFnParams(br));
		}
		else {
			final VncVector spec = br.getFn().getParams();	
			final boolean plainSymbolParams = Destructuring.isFnParamsWithoutDestructuring(spec);
			if (plainSymbolParams) {
				println(renderFnNoDestructuring(br));
			}
			else {
				println(renderFnDestructuring(br));
			}
		}
	}
	
	private void locals(final String sLevel) {
		if (!agent.hasBreak()) {
			println("Not in a debug break!");
			return;
		}
		
		final Break br = agent.getBreak();

		println(formatBreak(br));

		Env env = agent.getBreak().getEnv();
		if (env == null) {
			println("No information on local vars available");
		}
		else {
			int maxLevel = env.level() + 1;
			int level = sLevel == null ? 1 : Integer.parseInt(sLevel);
			level = Math.max(Math.min(maxLevel, level), 1);
			
			final List<Var> vars = env.getLocalVars(level-1);
			final String info = vars.isEmpty()
									? String.format(
										"   <no local vars at level %d>",
										level)	
									: vars.stream()
										  .map(v -> formatVar(v))
										  .collect(Collectors.joining("\n"));
	
			println("Local vars at level %d/%d:\n%s", level, maxLevel, info);
		}
	}
	
	private void local(final String name) {
		if (!agent.hasBreak()) {
			println("Not in a debug break!");
			return;
		}

		final Env env = agent.getBreak().getEnv();
		if (env == null) {
			println("No information on local vars available");
		}
		else {
			final VncSymbol sym = new VncSymbol(name);
			final Var v = env.findLocalVar(sym);
			if (v == null) {
				println("%s -> <not found>", name);
			}
			else {
				println(formatVar(sym, v.getVal()));
			}
		}
	}
	
	private void global(final String name) {
		if (!agent.hasBreak()) {
			println("Not in a debug break!");
			return;
		}

		final Env env = agent.getBreak().getEnv();
		if (env == null) {
			println("No information on global vars available");
		}
		else {
			final VncSymbol sym = new VncSymbol(name);
			final Var v = env.getGlobalVarOrNull(sym);
			if (v == null) {
				println("%s: <not found>", name);
			}
			else {
				final String sval = truncate(v.getVal().toString(true), 100, "...");
				println("%s: %s", name, sval);
			}
		}
	}
	
	private void retval() {
		if (!agent.hasBreak()) {
			println("Not in a debug break!");
			return;
		}
		
		final Break br = agent.getBreak();

		println(formatBreak(br));

		final VncVal v = br.getRetVal();
		if (v == null) {
			println("Return value: <not available>");
		}
		else {
			println("Return value: %s", renderValue(v,100));
		}
	}
	
	private void ex() {
		if (!agent.hasBreak()) {
			println("Not in a debug break!");
			return;
		}
	
		final Break br = agent.getBreak();

		println(formatBreak(br));

		final Exception e = br.getException();
		if (e == null) {
			println("exception: <not available>");
		}
		else {
			printer.printex("debug", e);
		}
	}
	
	private void breakpointListener(final Break b) {
		printer.println("debug", formatStop(b));

		// Interrupt the LineReader to display a new prompt
		replThread.interrupt();
	}

	private String renderNativeFnParams(final Break br) {
		final VncFunction fn = br.getFn();
		final VncList args = br.getArgs();

		final StringBuilder sb = new StringBuilder();
		
		sb.append(String.format(
					"Arguments passed to native function %s:",
					fn.getQualifiedName()));
		
		VncList args_ = args;
		
		for(int ii=0; ii<10; ii++) {
			sb.append("\n");
			sb.append(formatVar(ii, args_.first()));
			
			args_ = args_.rest();
			if (args_.isEmpty()) break;
		}
		
		if (args_.size() > 0) {
			sb.append(String.format(
						"\n... %d more arguments not displayed", 
						args_.size()));
		}
		
		if (br.getRetVal() != null) {
			sb.append('\n');
			sb.append(formatReturnVal(br.getRetVal()));
		}
		
		return sb.toString();
	}
	   
	private String renderFnNoDestructuring(final Break br) {
		final VncFunction fn = br.getFn();
		final VncVector spec = fn.getParams();	
		final VncList args = br.getArgs();

		final StringBuilder sb = new StringBuilder();
	
		VncVector spec_ = spec;
		VncList args_ = args;

		sb.append(String.format(
					"Arguments passed to %s %s:",
					br.isBreakInSpecialForm() 
						? "special form"
						: fn.isMacro() ? "macro" : "function",
					fn.getQualifiedName()));

		while(true) {
			sb.append("\n");
			sb.append(formatVar(spec_.first(), args_.first()));
			
			spec_ = spec_.rest();
			args_ = args_.rest();
			if (spec_.isEmpty()) {
				if (!args_.isEmpty()) {
					sb.append(String.format(
								"\n... %d more arguments not matching a parameter", 
								args_.size()));
				}
				break;
			}
		}
		
		if (br.getRetVal() != null) {
			sb.append('\n');
			sb.append(formatReturnVal(br.getRetVal()));
		}
		
		return sb.toString();
	}
	   
	private String renderFnDestructuring(final Break br) {
		final VncFunction fn = br.getFn();
		final VncVector spec = fn.getParams();	
		final VncList args = br.getArgs();

		final StringBuilder sb = new StringBuilder();

		sb.append(String.format(
					"Arguments passed to %s %s (destructured):",
					br.isBreakInSpecialForm() 
						? "special form"
						: fn.isMacro() ? "macro" : "function",
					fn.getQualifiedName()));

		final List<Var> vars = Destructuring.destructure(spec, args);
		vars.forEach(v -> sb.append("\n" + formatVar(v)));

		
		if (br.getRetVal() != null) {
			sb.append("\n" + formatReturnVal(br.getRetVal()));
		}

		return sb.toString();
	}
	
	private String formatVar(final Var v) {
		return formatVar(v.getName(), v.getVal());
	}
	
	private String formatVar(final VncVal name, final VncVal value) {
		final String sval = truncate(value.toString(true), 100, "...");
		final String sname =  name.toString(true);
		return String.format("%s -> %s", sname, sval);
	}
	
	private String formatVar(final int index, final VncVal value) {
		final String sval = truncate(value.toString(true), 100, "...");
		return String.format("[%d] -> %s", index, sval);
	}

	private String formatReturnVal(final VncVal retval) {
		final String sval = truncate(retval.toString(true), 100, "...");
		return String.format("[return] -> %s", sval);
	}

	private String formatBreak(final Break br) {
		final VncFunction fn = br.getFn();

		return String.format(
				"Break in %s %s at %s level.",
				br.isBreakInSpecialForm()
					? "special form"
					: fn.isMacro() ? "macro" : "function",
				fn.getQualifiedName(),
				br.getBreakpointScope().description());
	}
	
	private String formatStop(final Break br) {	
		final VncFunction fn = br.getFn();

		return String.format(
				"Stopped in %s %s%s at %s level.",
				br.isBreakInSpecialForm()
					? "special form"
					: fn.isMacro() ? "macro" : "function",
				fn.getQualifiedName(),
				fn.isNative() 
					? "" 
					: " (" + new CallFrame(fn).getSourcePosInfo() + ")",
				br.getBreakpointScope().description());
	}
	
	private String renderValue(final VncVal val, final int maxLen) {
		final String sVal = truncate(val.toString(true), maxLen, "...");
		final String type = Types.getType(val).toString();
		
		if (val instanceof VncSequence) {
			final int size = ((VncSequence)val).size();
			return String.format("%s [%d]: %s", type, size, sVal);
		}
		else if (val instanceof VncMap) {
			final int size = ((VncMap)val).size();
			return String.format("%s [%d]: %s", type, size, sVal);
		}
		else if (val instanceof VncSet) {
			final int size = ((VncSet)val).size();
			return String.format("%s [%d]: %s", type, size, sVal);
		}
		else if (val instanceof VncCollection) {
			final int size = ((VncCollection)val).size();
			return String.format("%s [%d]: %s", type, size, sVal);
		}
		else {
			return sVal;
		}
	}
	
	private void printErrorSteppingNotPossible(final String context) {
		printlnErr(
			"Stepping %s function is not possible in the current debug context",
			context);
	}
	
	private void println(String format, Object... args) {
		printer.println("debug", String.format(format, args));
	}
	
	private void printlnErr(String format, Object... args) {
		printer.println("error", String.format(format, args));
	}
	
	
	private final static String HELP =
		  //+------------------------------------------------------------------------------+
			"Venice debugger\n" +
			"\n" +
			"The debugger can break functions at 4 levels: \n" +
			"  call:        breaks before the passed parameters are evaluated. The \n" +
			"               unevaluated function parameters are available for inspection.\n" +
			"  entry:       breaks right after the passed parameters have been evaluated.\n" +
			"               The evaluated function parameters are available for inspection.\n" +
			"  exception:   breaks on catching an exception within the function's body. The\n" +
			"               exeption and the evaluated functions parameters are available\n" +
			"               for inspection.\n" +
			"  exit:        breaks before returning from the function. The return value and\n" +
			"               the evaluated function parameters are available for inspection.\n" +
			"\n" +
			"Commands: \n" +
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
			"                  E.g.: !breakpoint remove foo/gauss \n" +
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
			"  !step-next   Step to next function entry\n" +
			"               Short form: !sn\n" +
			"  !step-over   Step over the current function to next function entry.\n" +
			"               Implicitely steps over functions involved with function\n" +
			"               parameter evaluation.\n" +
			"               Short form: !so\n" +
			"  !step-entry  Step to the entry of the current function\n" +
			"               Short form: !se\n" +
			"  !step-exit   Step to the exit of the current function\n" +
			"               Short form: !sx\n" +
			"  !break?      Checks if the debugger is in a break or not\n" +
			"               Short form: !b?\n" +
			"  !params      Print the function's parameters\n" +
			"               Short form: !cs\n" +
			"  !retval      Print the function's return value\n" +
			"               Short form: !ret\n" +
			"  !ex          Print the function's exception\n" +
			"  !locals x    Print the local vars from the level x. The level is optional\n" +
			"               and defaults to the top level.\n" +
			"               Short form: !l\n" +
			"  !local v     Print a local var with the name v\n" +
			"  !global v    Print a global var with the name v\n" +
			"  !callstack   Print the current callstack\n" +
			"  form         Runs a Venice form in the current break context. Useful to\n" +
			"               inspect parameters, return values, or global/local vars.\n" +
			"               E.g.:  (first param1)";

	private static final Set<String> DEBUG_COMMANDS = new HashSet<>(
			Arrays.asList(
					// command     short
					"attach",
					"detach",
					"terminate",
					"info",         "i",
					"breakpoint",   "b",
					"resume",       "r",
					"step-any",     "sa",
					"step-next",    "sn",
					"step-next-",   "sn-",
					"step-over",    "so",
					"step-entry",   "se",
					"step-exit",    "sx",
					"break?",       "b?",
					"callstack",    "cs",
					"params",       "p",
					"locals",       "l",
					"local", 
					"global",
					"retval",       "ret",
					"ex"
			));
	
   
	private final TerminalPrinter printer;
	private final IDebugAgent agent;
	private final Thread replThread;
}
