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

import java.util.List;

import com.github.jlangch.venice.impl.debug.Break;
import com.github.jlangch.venice.impl.debug.IDebugAgent;
import com.github.jlangch.venice.impl.util.CallStack;
import com.github.jlangch.venice.impl.util.StringUtil;


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
				handleBreakpointCmd(params);
				break;
			case "run":
				run();
				break;
			case "callstack":
				callstack();
				break;
			case "fn-args":
				break;
			case "locals":
				break;
			case "local":
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
		agent.leaveBreak();
		printer.println("debug", "Leaving break");
	}
	
	private void callstack() {
		final CallStack cs = agent.getBreak().getCallStack();
		printer.println("debug", "Callstack:\n" + cs);
	}
	
	private void handleBreakpointCmd(final List<String> params) {
		if (params.size() < 1)  {
			printer.println("error", "Invalid 'dbg breakpoint {cmd}' command");
		}
		else {
			switch(StringUtil.trimToEmpty(params.get(1))) {
				case "add":
					params.subList(2, params.size())
						  .forEach(s -> agent.addBreakpoint(s));
					break;
					
				case "remove":
					params.subList(2, params.size())
					  .forEach(s -> agent.removeBreakpoint(s));
					break;
					
				case "clear":
					agent.removeAllBreakpoints();
					break;
					
				case "list":
					agent.listBreakpoints()
						 .stream()
						 .sorted()
						 .forEach(s -> printer.println("stdout", "   " + s));
					break;
			}
		}
	}

	private void breakpointListener(final Break b) {
		printer.println("debug", "Stopped in function " + b.getFn().getQualifiedName());		
	}
   
    
	private final TerminalPrinter printer;
	private final IDebugAgent agent;
}
