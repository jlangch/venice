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
import com.github.jlangch.venice.impl.debug.util.StepValidity;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.types.VncFunction;
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
		printer.println("stdout", ReplDebugHelp.HELP);
	}
	
	public static boolean isDebugCommand(final String cmd) {
		return DEBUG_COMMANDS.contains(cmd);
	}

	public void handleCommand(final String cmdLine) {
		final List<String> params = Arrays.asList(cmdLine.split(" +"));

		StepValidity stepValidity = null;
		
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
				stepValidity = agent.isStepPossible(StepMode.StepToAny);
				if (stepValidity.isValid()) {
					stepValidity = agent.step(StepMode.StepToAny);
				}
				if (!stepValidity.isValid()) {
					printer.println("stdout", agent.toString());
					printlnErr(stepValidity);
				}
				break;
				
			case "step-next":
			case "sn":
				stepValidity = agent.isStepPossible(StepMode.StepToNextFunction);
				if (stepValidity.isValid()) {
					stepValidity = agent.step(StepMode.StepToNextFunction);
				}
				if (!stepValidity.isValid()) {
					printer.println("stdout", agent.toString());
					printlnErr(stepValidity);
				}
				break;
				
			case "step-next-":
			case "sn-":
				stepValidity = agent.isStepPossible(StepMode.StepToNextNonSystemFunction);
				if (stepValidity.isValid()) {
					stepValidity = agent.step(StepMode.StepToNextNonSystemFunction);
				}
				if (!stepValidity.isValid()) {
					printer.println("stdout", agent.toString());
					printlnErr(stepValidity);
				}
				break;
				
			case "step-call":
			case "sc":
				stepValidity = agent.isStepPossible(StepMode.StepToNextFunctionCall);
				if (stepValidity.isValid()) {
					stepValidity = agent.step(StepMode.StepToNextFunctionCall);
				}
				if (!stepValidity.isValid()) {
					printer.println("stdout", agent.toString());
					printlnErr(stepValidity);
				}
				break;
				
			case "step-over":
			case "so":
				stepValidity = agent.isStepPossible(StepMode.StepOverFunction);
				if (stepValidity.isValid()) {
					stepValidity = agent.step(StepMode.StepOverFunction);
				}
				if (!stepValidity.isValid()) {
					printer.println("stdout", agent.toString());
					printlnErr(stepValidity);
				}
				break;
				
			case "step-entry":
			case "se":
				stepValidity = agent.isStepPossible(StepMode.StepToFunctionEntry);
				if (stepValidity.isValid()) {
					println("Stepping to entry of function %s ...",
							agent.getBreak().getFn().getQualifiedName());
					stepValidity = agent.step(StepMode.StepToFunctionEntry);
				}
				if (!stepValidity.isValid()) {
					printer.println("stdout", agent.toString());
					printlnErr(stepValidity);
				}
				break;
				
			case "step-exit":
			case "sx":
				stepValidity = agent.isStepPossible(StepMode.StepToFunctionExit);
				if (stepValidity.isValid()) {
					println("Stepping to exit of function %s ...",
							agent.getBreak().getFn().getQualifiedName());
					stepValidity = agent.step(StepMode.StepToFunctionExit);
				}
				if (!stepValidity.isValid()) {
					printer.println("stdout", agent.toString());
					printlnErr(stepValidity);
				}
				break;
				
			case "break?": 
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
				
			case "retval":
			case "ret":
				retval();
				break;
				
			case "ex":
				ex();
				break;

			case "help":
			case "?":
				println(ReplDebugHelp.HELP);
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

		if (br.getBreakpoint().getQualifiedName().equals("if")) {
			println(renderIfSpecialFormParams(br));
		}
		else if (br.isBreakInNativeFn()) {
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

	private String renderIfSpecialFormParams(final Break br) {
		final StringBuilder sb = new StringBuilder();
		
		sb.append("Arguments passed to if:");
		
		sb.append("\n");
		sb.append(formatVar(0, br.getArgs().first()));
		
		if (br.getRetVal() != null) {
			sb.append('\n');
			sb.append(formatReturnVal(br.getRetVal()));
		}
		
		return sb.toString();
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
	
	private void println(final String format, final Object... args) {
		printer.println("debug", String.format(format, args));
	}
	
	private void printlnErr(final String format, final Object... args) {
		printer.println("error", String.format(format, args));
	}
	
	private void printlnErr(final StepValidity validity) {
		if (!validity.isValid()) {
			printer.println("error", validity.getErrMsg());
		}
	}
	
	
	
	private static final Set<String> DEBUG_COMMANDS = new HashSet<>(
			Arrays.asList(
				   // command         alias
					"attach",
					"detach",
					"terminate",
					"info",           "i",
					"breakpoint",     "b",
					"resume",         "r",
					"step-any",       "sa",
					"step-next",      "sn",
					"step-next-",     "sn-",
					"step-call",      "sc",
					"step-over",      "so",
					"step-entry",     "se",
					"step-exit",      "sx",
					"break?",         "b?",
					"callstack",      "cs",
					"params",         "p",
					"locals",         "l",
					"retval",         "ret",
					"ex" ));
	
   
	private final TerminalPrinter printer;
	private final IDebugAgent agent;
	private final Thread replThread;
}
