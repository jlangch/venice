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
package com.github.jlangch.venice.impl.repl;

import org.jline.reader.EOFError;
import org.jline.reader.ParsedLine;
import org.jline.reader.SyntaxError;
import org.jline.reader.impl.DefaultParser;

import com.github.jlangch.venice.EofException;
import com.github.jlangch.venice.impl.Env;
import com.github.jlangch.venice.impl.VeniceInterpreter;


public class ReplParser extends DefaultParser {
	
	public ReplParser(final VeniceInterpreter venice, final Env env) {
		this.venice = venice;
		this.env = env;
	}

	public ParsedLine parse(
			final String line, 
			final int cursor, 
			final ParseContext context
	) throws SyntaxError {
		try {
			venice.RE(line, "repl", env);
			return super.parse(line, cursor, context);
		}
		catch(EofException ex) {
			// proceed with multi-line editing
			throw new EOFError(1, 1, ex.getMessage());
		}
	}

	
	private final VeniceInterpreter venice;
	private final Env env;
}
