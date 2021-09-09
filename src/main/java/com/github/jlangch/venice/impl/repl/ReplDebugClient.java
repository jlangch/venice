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
import java.util.concurrent.atomic.AtomicLong;
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
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.CallFrame;
import com.github.jlangch.venice.impl.util.CallStack;


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

	public Env getEnv() {
		return currCallFrame == null
				? agent.getActiveBreak().getEnv()
				: currCallFrame.getEnv();
	}
	
	public void handleCommand(final String cmdLine) {
		final List<String> params = Arrays.asList(cmdLine.split(" +"));

		StepValidity stepValidity = null;
		
		final String cmd = trimToEmpty(first(params));
		switch(cmd) {
			case "info":
			case "i":
				printer.println("stdout", agent.toString()); 
				printer.println("stdout", "Current CallFrame:  " 
											+ (currCallFrame == null 
													? "-" : currCallFrame));
				break;

			case "breakpoint":
			case "b":
				handleBreakpointCmd(drop(params, 1));
				break;

			case "resume":
			case "r":
				clearCurrCallFrame();
				agent.resume();
				break;

			case "resume-all":
			case "ra":
				clearCurrCallFrame();
				agent.resumeAll();
				break;
				
			case "step":
			case "s":
			case "step-any":
			case "sa":
				clearCurrCallFrame();
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
				clearCurrCallFrame();
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
				clearCurrCallFrame();
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
				clearCurrCallFrame();
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
				clearCurrCallFrame();
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
				clearCurrCallFrame();
				stepValidity = agent.isStepPossible(StepMode.StepToFunctionEntry);
				if (stepValidity.isValid()) {
					println("Stepping to entry of function %s ...",
							agent.getActiveBreak().getFn().getQualifiedName());
					stepValidity = agent.step(StepMode.StepToFunctionEntry);
				}
				if (!stepValidity.isValid()) {
					printer.println("stdout", agent.toString());
					printlnErr(stepValidity);
				}
				break;
				
			case "step-exit":
			case "sx":
				clearCurrCallFrame();
				stepValidity = agent.isStepPossible(StepMode.StepToFunctionExit);
				if (stepValidity.isValid()) {
					println("Stepping to exit of function %s ...",
							agent.getActiveBreak().getFn().getQualifiedName());
					stepValidity = agent.step(StepMode.StepToFunctionExit);
				}
				if (!stepValidity.isValid()) {
					printer.println("stdout", agent.toString());
					printlnErr(stepValidity);
				}
				break;
				
			case "breaks": 
				printBreakList();
				break;
				
			case "break":
				switchBreak(first(drop(params, 1)));
				break;
				
			case "break?": 
				isBreak();
				break;
				
			case "callstack": 
			case "cs":
				handleCallstackCmd(drop(params, 1));
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
				printlnErr(
						"Invalid debug command '%s'. Use '!help' for help.\n\n"
						+ "To run a REPL non debug command, detach the debugger "
						+ "first using '!detach'.", 
						cmd);
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
					case "a":
						agent.addBreakpoints(parseBreakpoints(drop(params, 1)));
						break;
						
					case "remove":
					case "rem":
					case "r":
						agent.removeBreakpoints(parseBreakpoints(drop(params, 1)));
						break;
						
					case "clear":
					case "c":
						agent.removeAllBreakpoints();
						break;
						
					case "skip":
					case "s":
						agent.skipBreakpoints(true);
						break;
						
					case "unskip":
					case "u":
						agent.skipBreakpoints(false);
						break;
						
					case "skip?":
						println("Skip breakpoints: %s", agent.isSkipBreakpoints());
						break;
						
					case "list":
					case "l":
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
	
	private void handleCallstackCmd(final List<String> params) {
		if (!agent.hasActiveBreak()) {
			println("Not in a debug break!");
			return;
		}
		
		if (params.isEmpty())  {
			printCallstack();
		}
		else {
			try {
				final String cmd = trimToEmpty(params.get(0));
				switch(cmd) {
					case "list":
						printCallstack();
						break;

					case "select":
					case "s": {
							final List<CallFrame> frames = getCallFrames(agent.getActiveBreak());
							final int level = parseCallStackLevel(params.get(1), frames.size());
							currCallFrameLevel = level;
							currCallFrame = frames.get(level-1);
							println("Selected call frame -> [%d/%d]: %s", 
									level,
									frames.size(),
									currCallFrame);
						}
						break;

					case "up": {
							final List<CallFrame> frames = getCallFrames(agent.getActiveBreak());
							currCallFrameLevel = limit(currCallFrameLevel + 1, 1, frames.size());
							currCallFrame = frames.get(currCallFrameLevel-1);
							println("Selected call frame -> [%d/%d]: %s", 
									currCallFrameLevel,
									frames.size(),
									currCallFrame);
						}
						break;

					case "down": {
							final List<CallFrame> frames = getCallFrames(agent.getActiveBreak());
							currCallFrameLevel = limit(currCallFrameLevel - 1, 1, frames.size());
							currCallFrame = frames.get(currCallFrameLevel-1); 
							println("Selected call frame -> [%d/%d]: %s", 
									currCallFrameLevel,
									frames.size(),
									currCallFrame);
						}
						break;

					case "dselect":
					case "d":
						println("Cleared call frame operations") ;
						currCallFrame = null;
						break;


					default:
						printlnErr(
							"Invalid callstack command '%s'. Use one of "
								+ "'list', 'select', 'up', 'down', or 'deselect'.", 
							cmd);
						break;
				}
			}
			catch(ParseError ex) {
				printer.println("error", ex.getMessage());
			}
			catch(RuntimeException ex) {
				printer.println("error", ex.getMessage());
			}
		}
	}

	
	private void switchBreak(final String sIndex) {
		final int breakCount = agent.getAllBreaks().size();

		if (breakCount == 0) {
			printlnErr("No breaks available!");
		}
		else {
			final int index = parseBreakIndex(sIndex);
			if (index < 1 || index > breakCount) {
				printlnErr(
					"Invalid break index %d. Must be in the range [1..%d].",
					index,
					breakCount);
			}
			else {
				final Break br = agent.switchActiveBreak(index);
				if (br != null) {
					println("Active break -> %s", br.getBreakFnInfo(false));
				}
				else {
					printlnErr("Failed switching active break!");
				}
			}
		}
	}

	private void isBreak() {
		println(agent.hasActiveBreak()
					? formatStop(agent.getActiveBreak())
					: "Not in a debug break!");
	}
	
	private void printCallstack() {
		final Break br = agent.getActiveBreak();

		println(formatBreakOverview(br));
		println();
		println("Callstack:");
		println(formatCallstack(br.getCallStack(), true));
	}
	
	private void printBreakList() {
		final List<Break> breaks = agent.getAllBreaks();
		
		final AtomicLong idx = new AtomicLong(1L);
		if (breaks.isEmpty()) {
			println("No breaks available!");
		}
		else {
			println("Breaks");
			breaks.forEach(b -> println("  [%d]: %s", 
									    idx.getAndIncrement(), 
						 			    b.getBreakFnInfo(false)));
		}
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
		if (!agent.hasActiveBreak()) {
			println("Not in a debug break!");
			return;
		}

		final Break br = agent.getActiveBreak();

		if (currCallFrame == null) {
			println(formatBreakOverview(br));
	
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
		else {
			println(renderCallFrameParams(currCallFrame));
		}
	}
	
	private void locals(final String sLevel) {
		if (!agent.hasActiveBreak()) {
			println("Not in a debug break!");
			return;
		}
		
		final Break br = agent.getActiveBreak();

		println(formatBreakOverview(br));

		if (currCallFrame == null) {
			final Env env = agent.getActiveBreak().getEnv();

			final int maxLevel = env.level() + 1;
			final int level = parseEnvLevel(sLevel, 1, maxLevel);

			println(
				"Local vars at breakpoint env level %d/%d:\n%s", 
				level, maxLevel, renderLocalCars(env, level));
		}
		else {
			final Env env = currCallFrame.getEnv();

			final int maxLevel = env.level() + 1;
			final int level = parseEnvLevel(sLevel, 1, maxLevel);

			println(
				"Local vars at env level %d/%d of call frame (%d) of %s:\n%s", 
				level, maxLevel, currCallFrameLevel, currCallFrame, 
				renderLocalCars(env, level));
		}
	}
			
	private void retval() {
		if (!agent.hasActiveBreak()) {
			println("Not in a debug break!");
			return;
		}
		
		final Break br = agent.getActiveBreak();

		println(formatBreakOverview(br));

		final VncVal v = br.getRetVal();
		if (v == null) {
			println("Return value: <not available>");
		}
		else {
			println("Return value: %s", renderValue(v,100));
		}
	}
	
	private void ex() {
		if (!agent.hasActiveBreak()) {
			println("Not in a debug break!");
			return;
		}
	
		final Break br = agent.getActiveBreak();

		println(formatBreakOverview(br));

		final Exception e = br.getException();
		if (e == null) {
			println("exception: <not available>");
		}
		else {
			printer.printex("debug", e);
		}
	}
	
	private void breakpointListener(final Break b) {
		clearCurrCallFrame();
		
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

	private String renderCallFrameParams(final CallFrame frame) {
		final VncList args = frame.getArgs();

		final StringBuilder sb = new StringBuilder();
		
		sb.append(String.format(
					"Arguments passed to the call frame %s:",
					frame));
		
		sb.append(renderIndexedParams(args));
		
		return sb.toString();
	}

	private String renderNativeFnParams(final Break br) {
		final VncFunction fn = br.getFn();
		final VncList args = br.getArgs();

		final StringBuilder sb = new StringBuilder();
		
		sb.append(String.format(
					"Arguments passed to native function %s:",
					fn.getQualifiedName()));
		
		sb.append(renderIndexedParams(args));
		
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

	private String renderIndexedParams(final VncList args) {
		final StringBuilder sb = new StringBuilder();

		VncList args_ = args.take(10);
		
		int ii = 0;
		while(!args_.isEmpty()) {
			sb.append("\n" + formatVar(ii++, args_.first()));
			args_ = args_.rest();
		}
		
		args_ = args.drop(10);
		if (!args_.isEmpty()) {
			sb.append(String.format(
						"\n... %d more arguments not displayed", 
						args_.size()));
		}
		
		return sb.toString();
	}
	
	private String renderLocalCars(final Env env, final int level) {
		final List<Var> vars = env.getLocalVars(level-1);
		return vars == null || vars.isEmpty()
				? String.format(
					"   <no local vars at env level %d>",
					level)	
				: vars.stream()
					  .map(v -> formatVar(v))
					  .collect(Collectors.joining("\n"));
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

	private String formatBreakOverview(final Break br) {
		final VncFunction fn = br.getFn();

		return String.format(
				"Break in %s %s at %s level.",
				br.isBreakInSpecialForm()
					? "special form"
					: fn.isMacro() ? "macro" : "function",
				fn.getQualifiedName(),
				br.getBreakpointScope().description());
	}

	private String formatCallstack(final CallStack cs, final boolean showLevel) {
		if (showLevel) {
			final int digits = cs.isEmpty() 
								? 1 
								: ((int)Math.floor(Math.log10(cs.size()))) + 1;
			
			final String format = "%s%" + digits + "d: %s";
			
			final boolean printMarker = currCallFrame != null;
			
			final AtomicLong idx = new AtomicLong(1);
			return cs.toList()
					 .stream()
					 .map(f -> String.format(
							 	format, 
							 	printMarker
							 		? idx.get() == currCallFrameLevel ? "* " : "  "
							 		: "",
							 	idx.getAndIncrement(), 
							 	f.toString()))
					 .collect(Collectors.joining("\n"));
		}
		else {
			return cs.toList()
					 .stream()
					 .collect(Collectors.joining("\n"));
		}
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
		
		if (val instanceof VncCollection) {
			final int size = ((VncCollection)val).size();
			return String.format("%s [%d]: %s", type, size, sVal);
		}
		else {
			return sVal;
		}
	}
	
	private int parseCallStackLevel(final String level, final int max) {
		final int lvl = parseCallStackLevel(level);		
		if (lvl < 1 || lvl > max) {
			throw new RuntimeException(String.format(
					"Invalid callstack level '%d'. Must be a in the range [1..%d].", 
					lvl,
					max));
		}
		return lvl;
	}
	
	private int parseCallStackLevel(final String level) {
		try {
			return Integer.parseInt(level);
		}
		catch(Exception ex) {
			throw new RuntimeException(String.format(
						"Invalid callstack level '%s'. Must be a number.", 
						level));
		}
	}
	
	private int parseEnvLevel(
			final String sLevel, 
			final int min, 
			final int max
	) {
		try {
			final int level = sLevel == null ? 1 : Integer.parseInt(sLevel);
			return limit(level, min, max);
		}
		catch(Exception ex) {
			throw new RuntimeException(String.format(
						"Invalid env level '%s'. Must be a number.", 
						sLevel));
		}
	}
	
	private int parseBreakIndex(final String sIndex) {
		try {
			return Integer.parseInt(sIndex);
		}
		catch(Exception ex) {
			throw new RuntimeException(String.format(
						"Invalid env level '%s'. Must be a number.", 
						sIndex));
		}
	}
	
	private void println() {
		printer.println("debug", "");
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
	
	private void clearCurrCallFrame() {
		currCallFrame = null;
		currCallFrameLevel = 0;
	}
	
	private List<CallFrame> getCallFrames(final Break br) {
		return br.getCallStack().callstack();
	}
	
	private int limit(final int val, final int min, final int max) {
		return Math.max(Math.min(val, max), min);
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
					"breaks",
					"switch-break",   "sb",
					"break?",         "b?",
					"callstack",      "cs",
					"params",         "p",
					"locals",         "l",
					"retval",         "ret",
					"ex" ));
	

	// if the 'currCallFrame' is not null the debug commands !params and
	// !list operate on the args/env of the current call frame instead of the
	// args/env function in the break
	private CallFrame currCallFrame;
	private int currCallFrameLevel = 0;
	
	private final TerminalPrinter printer;
	private final IDebugAgent agent;
	private final Thread replThread;
}
