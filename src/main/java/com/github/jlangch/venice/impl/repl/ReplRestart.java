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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;


public abstract class ReplRestart {

	public static void writeRestartCommands(final boolean macroEpxandOnLoad) {
		try(FileWriter fw = new FileWriter(RESTART_FILE, false);
			PrintWriter pw = new PrintWriter(fw)
		) {
			if (macroEpxandOnLoad) {
				pw.println("-macropexand");
			}
		}
		catch(Exception ex) {
			// skipped (best effort)
		}
	}

	public static boolean hasRestartCommands() {
		try {
			return new File(RESTART_FILE).exists();
		}
		catch(Exception ex) {
			return false;
		}
	}

	public static boolean isRestartWithMacroExpand() {
		try (FileReader rd = new FileReader(new File(RESTART_FILE));
			 BufferedReader br = new BufferedReader(rd)
		) {
			return br.lines().anyMatch(s -> s.trim().equals("-macropexand"));
		}
		catch(Exception ex) {
			return false;
		}
	}

	public static void removeRestartCommands() {
		try {
			new File(RESTART_FILE).delete();
		}
		catch(Exception ex) {
			// skipped (best effort)
		}
	}

	
	private final static String RESTART_FILE = ".repl.restart";
}
