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

import com.github.jlangch.venice.impl.Destructuring;
import com.github.jlangch.venice.impl.debug.agent.Break;
import com.github.jlangch.venice.impl.debug.agent.IDebugAgent;
import com.github.jlangch.venice.impl.debug.agent.StepMode;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncVector;
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
 *   debug> !step-return
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

		switch(trimToEmpty(first(params))) {
			case "info":
			case "?":
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
				
			case "step-next":
			case "sn":
				if (!agent.isStepPossible(StepMode.StepToNextFunction)) {
					printer.println(
							"error", 
							"Stepping into next function is not possible in the "
							+ "current debug context");
					return;
				}
				agent.step(StepMode.StepToNextFunction);
				break;
				
			case "step-next-":
			case "sn-":
				if (!agent.isStepPossible(StepMode.StepToNextNonSystemFunction))  {
					return;
				}
				agent.step(StepMode.StepToNextNonSystemFunction);
				break;
				
			case "step-entry":
			case "se":
				if (!agent.isStepPossible(StepMode.StepToFunctionEntry))  {
					return;
				}
				agent.step(StepMode.StepToFunctionEntry);
				break;
				
			case "step-return":
			case "sr":
				if (!agent.isStepPossible(StepMode.StepToFunctionReturn)) {
					return;
				}
				printer.println(
						"debug", 
						String.format(
							"Stepping into return of function %s ...",
							agent.getBreak().getFn().getQualifiedName()));
				agent.step(StepMode.StepToFunctionReturn);
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

			default:
				println(HELP);
				break;
		}
	}
	
	private void handleBreakpointCmd(final List<String> params) {
		if (params.isEmpty())  {
			printBreakpoints();
		}
		else {
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
					println("Skip breakpoints: " + agent.isSkipBreakpoints());
					break;
					
				case "list":
					printBreakpoints();
					break;
					
				default:
					printlnErr(String.format("Invalid breakpoint command '%s'.", cmd));
					break;
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
	
			println(String.format(
						"Local vars at level %d/%d:\n%s",
						level,
						maxLevel,
						info));
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
				println(String.format("%s -> <not found>", name));
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
				println(String.format("%s: <not found>", name));
			}
			else {
				final String sval = truncate(v.getVal().toString(true), 100, "...");
				println(String.format("%s: %s", name, sval));
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
			final String sval = truncate(v.toString(true), 100, "...");
			println(String.format("Return value: %s", sval));
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
					: "function",
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
					: "function",
				fn.getQualifiedName()));

		final List<Var> vars = Destructuring.destructure(spec, args);
		vars.forEach(v -> {
			sb.append('\n');
			sb.append(formatVar(v)); 
		});

		
		if (br.getRetVal() != null) {
			sb.append('\n');
			sb.append(formatReturnVal(br.getRetVal()));
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
		return String.format(
				"Break in %s %s at %s level.",
				br.isBreakInSpecialForm()
					? "special form"
					: "function",
				br.getFn().getQualifiedName(),
				br.getBreakpointScope().description());
	}
	
	private String formatStop(final Break br) {	
		return String.format(
				"Stopped in %s %s%s at %s level.",
				br.isBreakInSpecialForm()
					? "special form"
					: "function",
				br.getFn().getQualifiedName(),
				br.getFn().isNative() 
					? "" 
					: " (" + new CallFrame(br.getFn()).getSourcePosInfo() + ")",
				br.getBreakpointScope().description());
	}
	
	private void println(final String s) {
		printer.println("debug", s);
	}
	
	private void printlnErr(final String s) {
		printer.println("error", s);
	}
	
	
	private final static String HELP =
		  //+------------------------------------------------------------------------------+
			"Venice debugger\n" +
			"\n" +
			"The debugger can stop within functions at 4 levels: \n" +
			"  call:        before the passed arguments are evaluated\n" +
			"  entry:       after the passed arguments have been evaluated\n" +
			"  exception:   on catching an exception with the function's body\n" +
			"  exit:        before returning from the function\n" +
			"\n" +
			"Commands: \n" +
			"  !attach      Attach the debugger to the REPL\n" +
			"  !detach      Detach the debugger from the REPL\n" +
			"  !terminate   Terminate a running debug session\n" +
			"               Sends an interrupt to the script under debugging.\n" +
			"  !info        Print info on the current debug session\n" +
			"  !breakpoint  Manage breakpoints\n" +
			"               o Add one or multiple breakpoints\n" +
			"                  !breakpoint add n, n*\n" +
			"                  E.g.: !breakpoint add user/gauss\n" +
			"                        !breakpoint add user/gauss +\n" +
			"               o Remove one or multiple breakpoints\n" +
			"                  !breakpoint remove n, n*\n" +
			"                  E.g.: !breakpoint remove user/gauss + \n" +
			"               o Temporarily skip/unskip all breakpoints\n" +
			"                  !breakpoint skip\n" +
			"                  !breakpoint unksip\n" +
			"                  !breakpoint skip?\n" +
			"               o List breakpoints\n" +
			"                  !breakpoint list\n" +
			"                  E.g.: !breakpoint list\n" +
			"               Short form: !b ...\n" +
			"  !resume      Resume from breakpoint\n" +
			"               Short form: !r\n" +
			"  !step-next   Step to next function\n" +
			"               Short form: !sn\n" +
			"  !step-entry  Step into function entry after args evaluation\n" +
			"               Short form: !se\n" +
			"  !step-return Step to the return of the function\n" +
			"               Short form: !sr\n" +
			"  !break?      Prints info on whether the debugger is in a break or not\n" +
			"               Short form: !b?\n" +
			"  !params      Print the functions parameters\n" +
			"               Short form: !p\n" +
			"  !locals x    Print the local vars from the level x. The level is optional\n" +
			"               and defaults to the top level.\n" +
			"               Short form: !l\n" +
			"  !local v     Print a local var with the name v\n" +
			"  !global v    Print a global var with the name v\n" +
			"  !callstack   Print the current callstack\n" +
			"               Short form: !cs\n" +
			"  !retval      Print the functions return value\n" +
			"               Short form: !ret\n" +
			"  !ex          Print the function's exception\n";

	private static final Set<String> DEBUG_COMMANDS = new HashSet<>(
			Arrays.asList(
					// command     short
					"attach",
					"detach",
					"terminate",
					"info",        "?",
					"breakpoint",  "b",
					"resume",      "r",
					"step-next",   "sn",
					"step-next-",  "sn-",
					"step-entry",  "se",
					"step-return", "sr",
					"break?",      "b?",
					"callstack",   "cs",
					"params",      "p",
					"locals",      "l",
					"local", 
					"global",
					"retval",      "ret",
					"ex"
			));
	
   
	private final TerminalPrinter printer;
	private final IDebugAgent agent;
	private final Thread replThread;
}
