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
package com.github.jlangch.venice.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.github.jlangch.venice.EofException;

public class Readline {
	  
	// Just java readline (no history, or line editing)
	public static String readline(String prompt) throws EofException, IOException {
		System.out.print(prompt);
		
		final BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
		final String line=buffer.readLine();
		if (line == null) {
			throw new EofException();
		}
		return line;
	}
}
