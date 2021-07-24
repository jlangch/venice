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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.debug.Break;
import com.github.jlangch.venice.impl.debug.BreakpointType;
import com.github.jlangch.venice.impl.debug.IDebugAgent;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.util.CallStack;
import com.github.jlangch.venice.impl.util.CollectionUtil;
import com.github.jlangch.venice.impl.util.StringUtil;


/**
 * REPL debugger client
 * 
 * <p>A typical debug session looks like:
 * <pre>
 *   venice> (defn sum [x y] (println "running sum") (+ x y))
 *   venice> !dbg attach
 *   venice> !dbg activate
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

	public void handleDebuggerCommand(
			final List<String> params
	) {
		switch(StringUtil.trimToEmpty(params.get(0))) {
			case "activate":
				activate();
				break;
			case "deactivate":
				deactivate();
				break;
			case "breakpoint":
				handleBreakpointCmd(CollectionUtil.drop(params, 1));
				break;
			case "next":
				run();
				break;
			case "next-fn":
				stopOnNextFunction();
				break;			
			case "callstack":
				callstack();
				break;
			case "params":
				fn_args(CollectionUtil.drop(params, 1));
				break;
			case "locals":
				locals(CollectionUtil.drop(params, 1));
				break;
			case "local":
				local(params.get(1));
				break;
			case "retval":
				retval();
				break;
			case "ex":
				ex();
				break;
			default:
				printer.println("error", "Invalid dbg command.");
				break;
		}
	}

	private void activate() {
		agent.activate(true);
		agent.addBreakListener(this::breakpointListener);
		printer.println("stdout", "Debugger: activated");
	}
	
	private void deactivate() {
		agent.activate(false);
		printer.println("stdout", "Debugger: deactivated");
	}
	
	private void run() {
		final String fnName = agent.getBreak().getFn().getQualifiedName();
		agent.leaveBreak();
		printer.println("debug", "Returning from function " + fnName);
	}
	
	private void stopOnNextFunction() {
		final String fnName = agent.getBreak().getFn().getQualifiedName();
		agent.leaveBreakForNextFunction();
		printer.println("debug", "Returning from function " + fnName + ". Stop on next function...");
	}
	
	private void callstack() {
		final CallStack cs = agent.getBreak().getCallStack();
		printer.println("debug", "Callstack:\n" + cs);
	}
	
	private void fn_args(final List<String> params) {
		final VncFunction fn = agent.getBreak().getFn();
		final VncVector spec = fn.getParams();	
		final VncList args = agent.getBreak().getArgs();
		
		printer.println("debug", 
						"Parameters:\n" + spec.toString(true) +
						"\n\nArguments:\n" + args.toString(true));
	}
	
	private void locals(final List<String> params) {
		Env env = agent.getBreak().getEnv();
		int maxLevel = env.level() + 1;
		int level = Integer.parseInt(params.get(0));
		level = Math.max(Math.min(maxLevel, level), 1);
		
		printer.println(
			"debug",
			String.format(
				"[%d/%d] Local vars:\n%s",
				level,
				maxLevel,
				env.getLocalVars(level-1)
				   .stream()
				   .map(v -> v.getName().getSimpleName())
				   .map(s -> "   " + s)
				   .collect(Collectors.joining("\n"))));
	}
	
	private void local(final String name) {
		final VncSymbol sym = new VncSymbol(name);
		final Var v = agent.getBreak().getEnv().findLocalVar(sym);
		if (v == null) {
			printer.println(
					"debug", 
					String.format("%s: <not found>", name));
		}
		else {
			printer.println(
					"debug", 
					String.format(
							"%s: %s", 
							name, 
							v.getVal().toString(true)));
		}
	}
	
	private void retval() {
		final VncVal v = agent.getBreak().getRetVal();
		if (v == null) {
			printer.println("debug", "return: <not available>");
		}
		else {
			printer.println(
					"debug", 
					String.format("return: %s", v.toString(true)));
		}
	}
	
	private void ex() {
		final Exception e = agent.getBreak().getException();
		if (e == null) {
			printer.println("debug", "exception: <not available>");
		}
		else {
			printer.println(
					"debug", 
					String.format("exception: %s", e.getClass().getName()));
		}
	}
	
	private void handleBreakpointCmd(final List<String> params) {
		if (params.size() < 1)  {
			printer.println("error", "Invalid 'dbg breakpoint {cmd}' command");
		}
		else {
			switch(StringUtil.trimToEmpty(params.get(0))) {
				case "add":
					String types = StringUtil.trimToEmpty(params.get(1));
					if (types.matches("^[(!)]+$")) {
						CollectionUtil.drop(params, 2)
									  .stream()
									  .filter(s -> !s.matches("^[(!)]+$"))
						  			  .forEach(s -> agent.addBreakpoint(
						  					 			s, 
						  					 			parseBreakpointTypes(types)));
					}
					else {
						CollectionUtil.drop(params, 1)
									  .stream()
									  .filter(s -> !s.matches("^[(!)]+$"))
									  .forEach(s -> agent.addBreakpoint(
											  			s,
											  			parseBreakpointTypes("(")));
					}
					break;
					
				case "remove":
					CollectionUtil.drop(params, 1)
								  .forEach(s -> agent.removeBreakpoint(s));
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
	
	private String format(final BreakpointType type) {
		switch(type) {
			case FunctionEntry: return "(";
			case FunctionException: return "!";
			case FunctionExit: return ")";
			default: return "";
		}
	}

	private String format(final Set<BreakpointType> types) {
		return Arrays.asList(BreakpointType.values())
					 .stream()
					 .filter(t -> types.contains(t))
					 .map(t -> format(t))
					 .collect(Collectors.joining());
	}

	private Set<BreakpointType> parseBreakpointTypes(final String types) {
		return Arrays.asList(BreakpointType.values())
					 .stream()
					 .filter(t -> types.contains(format(t)))
					 .collect(Collectors.toSet());
	}
	
	private void breakpointListener(final Break b) {
		printer.println(
				"debug", 
				String.format(
						"Stopped in function %s at %s",
						b.getFn().getQualifiedName(),
						b.getBreakpointType()));		
	}
   
    
	private final TerminalPrinter printer;
	private final IDebugAgent agent;
}
