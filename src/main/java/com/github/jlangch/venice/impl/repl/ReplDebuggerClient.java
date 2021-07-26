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

import static com.github.jlangch.venice.impl.debug.BreakpointType.FunctionEntry;
import static com.github.jlangch.venice.impl.debug.BreakpointType.FunctionException;
import static com.github.jlangch.venice.impl.debug.BreakpointType.FunctionExit;
import static com.github.jlangch.venice.impl.util.CollectionUtil.drop;
import static com.github.jlangch.venice.impl.util.CollectionUtil.first;
import static com.github.jlangch.venice.impl.util.CollectionUtil.second;
import static com.github.jlangch.venice.impl.util.CollectionUtil.toSet;
import static com.github.jlangch.venice.impl.util.StringUtil.trimToEmpty;
import static com.github.jlangch.venice.impl.util.StringUtil.trimToNull;
import static com.github.jlangch.venice.impl.util.StringUtil.truncate;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.Destructuring;
import com.github.jlangch.venice.impl.debug.Break;
import com.github.jlangch.venice.impl.debug.BreakpointType;
import com.github.jlangch.venice.impl.debug.IDebugAgent;
import com.github.jlangch.venice.impl.debug.StopNextType;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.util.CallFrame;
import com.github.jlangch.venice.impl.util.CallStack;


/**
 * REPL debugger client
 * 
 * <p>A typical debug session looks like:
 * <pre>
 *   venice> (defn sum [x y] (+ x y))
 *   venice> !dbg attach
 *   venice> !dbg start
 *   venice> !dbg breakpoint add (!) user/sum
 *   venice> (sum 6 7)
 *   Stopped in function user/sum at FunctionEntry
 *   venice> !dbg params
 *   Parameters:
 *   [x y]
 *
 *   Arguments:
 *   (6 7)
 *   venice> !dbg next
 *   Stopped in function user/sum at FunctionExit
 *   venice> !dbg retval
 *   return: 13
 *   venice> !dbg next
 *   Resuming from function user/sum
 * </pre>
 */
public class ReplDebuggerClient {

	public ReplDebuggerClient(
			final IDebugAgent agent,
			final TerminalPrinter printer
	) {
		this.agent = agent;
		this.printer = printer;
	}

	public void handleDebuggerCommand(final List<String> params) {
		switch(trimToEmpty(first(params))) {
			case "start":  // !dbg start
				start();
				break;
				
			case "stop":  // !dbg stop
				stop();
				break;
				
			case "breakpoint":   // !dbg breakpoint add (|) user/sum +
			case "bp":
				handleBreakpointCmd(drop(params, 1));
				break;
				
			case "next":  // !dbg next
			case "n":
				run();
				break;
				
			case "next+":  // !dbg next+ ()
			case "n+":
				runToNextFunction(
					parseBreakpointTypes(
						second(params), 
						toSet(FunctionEntry)));
				break;
				
			case "next-":  // !dbg next- ()
			case "n-":
				runToNextNonSystemFunction(
					parseBreakpointTypes(
						second(params), 
						toSet(FunctionEntry)));
				break;
				
			case "callstack":  // !dbg callstack
			case "cs":
				callstack();
				break;
				
			case "params":  // !dbg params
			case "p":
				params(drop(params, 1));
				break;
				
			case "locals":  // !dbg locals {level}
				locals(second(params));
				break;
				
			case "local":  // !dbg local x
				local(second(params));
				break;
				
			case "global":  // !dbg global filter
				global(second(params));
				break;
				
			case "retval":  // !dbg retval
			case "ret":
				retval();
				break;
				
			case "ex":  // !dbg ex
				ex();
				break;
				
			default:
				printer.println("error", "Invalid debug command.");
				break;
		}
	}

	private void start() {
		agent.start();
		agent.addBreakListener(this::breakpointListener);
		printer.println("stdout", "Debugger: started");
	}
	
	private void stop() {
		agent.stop();
		printer.println("stdout", "Debugger: stopped");
	}
	
	private void run() {
		// final String fnName = agent.getBreak().getFn().getQualifiedName();
		// printer.println("debug", "Returning from function " + fnName);
		agent.leaveBreak(StopNextType.MatchingFnName, null);
	}
	
	private void runToNextFunction(final Set<BreakpointType> flags) {
		// final String fnName = agent.getBreak().getFn().getQualifiedName();
		// printer.println("debug", "Returning from function " + fnName + ". Stop on next function...");
		agent.leaveBreak(StopNextType.AnyFunction, flags);
	}
	
	private void runToNextNonSystemFunction(final Set<BreakpointType> flags) {
		// final String fnName = agent.getBreak().getFn().getQualifiedName();
		// printer.println("debug", "Returning from function " + fnName + ". Stop on next function...");
		agent.leaveBreak(StopNextType.AnyNonSystemFunction, flags);
	}
	
	private void callstack() {
		if (!agent.hasBreak()) {
			printer.println("debug", "Not in a debug break!");
			return;
		}
		
		final CallStack cs = agent.getBreak().getCallStack();
		printer.println("debug", cs.toString());
	}
	
	private void params(final List<String> params) {
		if (!agent.hasBreak()) {
			printer.println("debug", "Not in a debug break!");
			return;
		}
		
		final VncFunction fn = agent.getBreak().getFn();
		final VncVector spec = fn.getParams();	
		final VncList args = agent.getBreak().getArgs();

		if (fn.isNative()) {
			printer.println("debug", renderNativeFnParams(args));
		}
		else {
			final boolean plainSymbolParams = Destructuring.isFnParamsWithoutDestructuring(spec);
			if (plainSymbolParams) {
				printer.println("debug", renderFnNoDestructuring(spec, args));
			}
			else {
				printer.println("debug", renderFnDestructuring(spec, args));
			}
		}
	}
	
	private void locals(final String sLevel) {
		if (!agent.hasBreak()) {
			printer.println("debug", "Not in a debug break!");
			return;
		}

		Env env = agent.getBreak().getEnv();
		if (env == null) {
			printer.println("debug", "No information on local vars available");
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
	
			printer.println("debug", String.format(
										"[%d/%d] Local vars:\n%s",
										level,
										maxLevel,
										info));
		}
	}
	
	private void local(final String name) {
		if (!agent.hasBreak()) {
			printer.println("debug", "Not in a debug break!");
			return;
		}

		final Env env = agent.getBreak().getEnv();
		if (env == null) {
			printer.println("debug", "No information on local vars available");
		}
		else {
			final VncSymbol sym = new VncSymbol(name);
			final Var v = env.findLocalVar(sym);
			if (v == null) {
				printer.println("debug", String.format("%s -> <not found>", name));
			}
			else {
				printer.println("debug", formatVar(sym, v.getVal()));
			}
		}
	}
	
	private void global(final String name) {
		if (!agent.hasBreak()) {
			printer.println("debug", "Not in a debug break!");
			return;
		}

		final Env env = agent.getBreak().getEnv();
		if (env == null) {
			printer.println("debug", "No information on global vars available");
		}
		else {
			final VncSymbol sym = new VncSymbol(name);
			final Var v = env.getGlobalVarOrNull(sym);
			if (v == null) {
				printer.println(
						"debug", 
						String.format("%s: <not found>", name));
			}
			else {
				final String sval = truncate(v.getVal().toString(true), 100, "...");
				printer.println("debug", String.format("%s: %s", name, sval));
			}
		}
	}
	
	private void retval() {
		if (!agent.hasBreak()) {
			printer.println("debug", "Not in a debug break!");
			return;
		}

		final VncVal v = agent.getBreak().getRetVal();
		if (v == null) {
			printer.println("debug", "Return value: <not available>");
		}
		else {
			final String sval = truncate(v.toString(true), 100, "...");
			printer.println("debug", String.format("Return value: %s", sval));
		}
	}
	
	private void ex() {
		if (!agent.hasBreak()) {
			printer.println("debug", "Not in a debug break!");
			return;
		}

		final Exception e = agent.getBreak().getException();
		if (e == null) {
			printer.println("debug", "exception: <not available>");
		}
		else {
			printer.printex("debug", e);
		}
	}
	
	private void handleBreakpointCmd(final List<String> params) {
		if (params.size() < 1)  {
			printer.println("error", "Invalid 'dbg breakpoint {cmd}' command");
		}
		else {
			// build regex: "^[(!)]+$"
			final String regex = "^[" + getBreakpointTypeSymbolList() + "]+$";
			
			switch(trimToEmpty(params.get(0))) {
				case "add":
					String types = trimToEmpty(params.get(1));
					if (types.matches(regex)) {
						drop(params, 2)
							.stream()
							.filter(s -> !s.matches(regex))
							.forEach(s -> agent.addBreakpoint(
			  					 			s, 
			  					 			parseBreakpointTypes(types)));
					}
					else {
						drop(params, 1)
							.stream()
							.filter(s -> !s.matches(regex))
							.forEach(s -> agent.addBreakpoint(
								  			s,
								  			toSet(FunctionEntry)));
					}
					break;
					
				case "remove":
					drop(params, 1).forEach(s -> agent.removeBreakpoint(s));
					break;
					
				case "clear":
					agent.removeAllBreakpoints();
					break;
					
				case "list":
					agent.getBreakpoints()
						 .entrySet()
						 .stream()
						 .sorted(Comparator.comparing(e -> e.getKey()))
						 .forEach(e -> printer.println(
								 		"stdout", 
								 		String.format(
								 			"  %s %s", 
								 			e.getKey(),
								 			format(e.getValue()))));
					break;
			}
		}
	}
	
	private String format(final Set<BreakpointType> types) {
		// predefined order of breakpoint types
		return Arrays.asList(FunctionEntry, FunctionException, FunctionExit)
					 .stream()
					 .filter(t -> types.contains(t))
					 .map(t -> t.symbol())
					 .collect(Collectors.joining());
	}

	private Set<BreakpointType> parseBreakpointTypes(final String types) {
		return parseBreakpointTypes(types, new HashSet<>());
	}

	private Set<BreakpointType> parseBreakpointTypes(
			final String types,
			final Set<BreakpointType> defaultTypes
	) {
		if (trimToNull(types) == null) {
			return defaultTypes;
		}
		else {
			Set<BreakpointType> tset = Arrays.asList(BreakpointType.values())
											 .stream()
											 .filter(t -> types.contains(t.symbol()))
											 .collect(Collectors.toSet());
			
			return tset.isEmpty() ? defaultTypes : tset;
		}
	}
	
	private String getBreakpointTypeSymbolList() {
		// return "(!)"
		return BreakpointType
					.all()
					.stream()
					.map(t -> t.symbol())
					.collect(Collectors.joining());
	}
	
	private void breakpointListener(final Break b) {
		final VncFunction fn = b.getFn();
		
		final String srcInfo = fn.isNative() 
								? "" 
								: " at " + new CallFrame(fn).getSourcePosInfo();
		
		printer.println(
				"debug", 
				String.format(
						"Stopped in function %s (%s)%s",
						fn.getQualifiedName(),
						b.getBreakpointType(),
						srcInfo));
	}
   
	private String renderNativeFnParams(final VncList args) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("Arguments passed to native function:");
		
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
		
		return sb.toString();
	}
	   
	private String renderFnNoDestructuring(final VncVector spec, final VncList args) {
		StringBuilder sb = new StringBuilder();
	
		VncVector spec_ = spec;
		VncList args_ = args;

		sb.append("Arguments passed to function:");
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
		
		return sb.toString();
	}
	   
	private String renderFnDestructuring(final VncVector spec, final VncList args) {
		StringBuilder sb = new StringBuilder();

		sb.append("Arguments passed to function (destructured):");
		final List<Var> vars = Destructuring.destructure(spec, args);
		vars.forEach(v -> sb.append(formatVar(v)));
		
		return sb.toString();
	}
	
	private String formatVar(final Var v) {
		return formatVar(v.getName(), v.getVal());
	}
	
	private String formatVar(final VncVal name, VncVal value) {
		final String sval = truncate(value.toString(true), 100, "...");
		final String sname =  name.toString(true);
		return String.format("%s -> %s", sname, sval);
	}
	
	private String formatVar(final int index, VncVal value) {
		final String sval = truncate(value.toString(true), 100, "...");
		return String.format("[%d] -> %s", index, sval);
	}
	
    
	private final TerminalPrinter printer;
	private final IDebugAgent agent;
}
