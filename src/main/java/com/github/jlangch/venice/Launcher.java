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
import java.io.PrintStream;
import java.util.List;

import com.github.jlangch.venice.impl.Env;
import com.github.jlangch.venice.impl.LoadPath;
import com.github.jlangch.venice.impl.Var;
import com.github.jlangch.venice.impl.VeniceInterpreter;
import com.github.jlangch.venice.impl.javainterop.JavaInterop;
import com.github.jlangch.venice.impl.repl.REPL;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.util.CommandLineArgs;
import com.github.jlangch.venice.impl.util.FileUtil;
import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;
import com.github.jlangch.venice.javainterop.IInterceptor;


public class Launcher {
	
	public static void main(final String[] args) {
		final CommandLineArgs cli = new CommandLineArgs(args);
		final IInterceptor interceptor = new AcceptAllInterceptor();
		JavaInterop.register(interceptor);
		
		final List<String> loadPaths = LoadPath.parseFromString(cli.switchValue("--load-path"));

		try {
			if (cli.switchPresent("-file")) {
				final VeniceInterpreter venice = new VeniceInterpreter(interceptor, loadPaths);
				final Env env = createEnv(venice, cli);
	
				final String file = cli.switchValue("-file");
				final String script = new String(FileUtil.load(new File(file)));
				
				System.out.println(venice.PRINT(venice.RE(script, new File(file).getName(), env)));
			}
			else if (cli.switchPresent("-script")) {
				final VeniceInterpreter venice = new VeniceInterpreter(interceptor, loadPaths);
				final Env env = createEnv(venice, cli);
	
				final String script = cli.switchValue("-script");
				
				System.out.println(venice.PRINT(venice.RE(script, "script", env)));
			}
			else if (cli.switchPresent("-repl")) {
				new REPL(interceptor, loadPaths).run(args);
			}
			else {
				new REPL(interceptor, loadPaths).run(args);
			}
			
			System.exit(0);
		}
		catch (VncException ex) {
			ex.printVeniceStackTrace();
			System.exit(1);
		}	
		catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}	
	}
	
	private static Env createEnv(final VeniceInterpreter venice, final CommandLineArgs cli) {
		return venice.createEnv()
					 .setGlobal(new Var(new VncSymbol("*ARGV*"), cli.argsAsList()))
					 .setStdoutPrintStream(new PrintStream(System.out, true));
	}
	
}
