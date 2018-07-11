/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2018 Venice
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
package org.venice;

import org.venice.impl.Env;
import org.venice.impl.Printer;
import org.venice.impl.Readline;
import org.venice.impl.VeniceInterpreter;
import org.venice.impl.types.VncString;
import org.venice.impl.types.VncSymbol;
import org.venice.impl.types.collections.VncList;


public class REPL {
	
	public static void main(final String[] args) {
		final VeniceInterpreter venice = new VeniceInterpreter();
		
		final Env env = venice.createEnv();

		final VncList argv = new VncList();
		for (int ii=1; ii<args.length; ii++) {
			argv.addAtEnd(new VncString(args[ii]));
		}
		env.set(new VncSymbol("*ARGV*"), argv);
		

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
			catch (Exception e) {
				System.out.println("Exception: " + e.getMessage());
				break;
			}
			
			try {
				System.out.println("=> " + venice.PRINT(venice.RE(line, "repl", env)));
			} 
			catch (ContinueException e) {
				continue;
			} 
			catch (ValueException e) {
				System.out.println("Error: " + Printer._pr_str(e.getValue(), false));
				continue;
			} 
			catch (Exception t) {
				System.out.println("Uncaught " + t + ": " + t.getMessage());
				continue;
			}
		}
	}
	
	
	private static final String PROMPT = "venice> ";
}
