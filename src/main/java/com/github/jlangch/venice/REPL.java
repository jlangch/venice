/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2019 Venice
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
package com.github.jlangch.venice;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.Env;
import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.Readline;
import com.github.jlangch.venice.impl.ValueException;
import com.github.jlangch.venice.impl.Var;
import com.github.jlangch.venice.impl.VeniceInterpreter;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.util.FileUtil;
import com.github.jlangch.venice.impl.util.ThreadLocalMap;
import com.github.jlangch.venice.util.CommandLineArgs;


public class REPL {
	
	public static void main(final String[] args) {
		final CommandLineArgs cli = new CommandLineArgs(args);
		if (cli.switchPresent("-file") || cli.switchPresent("-script")) {
			exec(cli);
		}
		else {
			repl(args);
		}
	}

	private static void repl(final String[] args) {
		final VeniceInterpreter venice = new VeniceInterpreter();
		
		final VncList argv = toList(args);
		
		final Env env = venice.createEnv();
		env.setGlobal(new Var(new VncSymbol("*ARGV*"), argv));
		
		// REPL loop
		while (true) {
			String line;
			try {
				line = Readline.readline(PROMPT);
				if (line == null) { 
					continue; 
				}
			} 
			catch (EofException e) {
				break;
			} 
			catch (VncException e) {
				e.printVeniceStackTrace();
				break;
			}
			catch (Exception e) {
				e.printStackTrace();
				break;
			}
			
			try {
				ThreadLocalMap.clearCallStack();
				System.out.println("=> " + venice.PRINT(venice.RE(line, "repl", env)));
			} 
			catch (ContinueException e) {
				continue;
			} 
			catch (ValueException e) {
				System.out.println(e.getMessage());
				System.out.println("Value: " + Printer._pr_str(e.getValue(), false));
				continue;
			} 
			catch (VncException e) {
				e.printVeniceStackTrace();
				continue;
			}
			catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
	}

	private static void exec(final CommandLineArgs cli) {
		final VncList argv = toList(cli.args());

		final VeniceInterpreter venice = new VeniceInterpreter();
		final Env env = venice.createEnv();
		env.setGlobal(new Var(new VncSymbol("*ARGV*"), argv));

		if (cli.switchPresent("-file")) {
			final String file = cli.switchValue("-file");
			final String script = new String(FileUtil.load(new File(file)));
			
			System.out.println(venice.PRINT(venice.RE(script, new File(file).getName(), env)));
		}
		else if (cli.switchPresent("-script")) {
			final String script = cli.switchValue("-script");
			
			System.out.println(venice.PRINT(venice.RE(script, "script", env)));
		}
	}
	
	private static VncList toList(final String[] args) {
		return new VncList(Arrays
							.asList(args)
							.stream()
							.map(s -> new VncString(s))
							.collect(Collectors.toList()));
	}
	
	
	private static final String PROMPT = "venice> ";
}
