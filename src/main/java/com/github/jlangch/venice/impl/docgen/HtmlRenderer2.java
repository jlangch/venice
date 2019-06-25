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
package com.github.jlangch.venice.impl.docgen;

import static com.github.jlangch.venice.impl.VeniceClasspath.getVeniceBasePath;

import java.util.Map;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.util.ClassPathResource;


public class HtmlRenderer2 {
	
	public static String renderCheatSheet(final Map<String,Object> data) {
		try {
			final String template = loadCheatSheetTemplate();
			
			final String script = "(do                                           \n" +
								  "   (load-module :kira)                        \n" +
								  "   (kira/eval template [\"${\" \"}$\"] data))   ";
			
			final Venice venice = new Venice();
			
			return (String)venice.eval(
							script,
							Parameters.of("template", template, "data", data));
		}
		catch(VncException ex) {
			throw new RuntimeException(
						"Failed to render cheat sheet HTML. \n" + 
						"Venice Callstack: \n" + ex.getCallStackAsString("   "),
						ex);
		}
		catch(Exception ex) {
			throw new RuntimeException("Failed to render cheat sheet HTML", ex);
		}
	}
	
	private static String loadCheatSheetTemplate() {
		return new ClassPathResource(getVeniceBasePath() + "docgen/cheatsheet2.html")
						.getResourceAsString("UTF-8");
	}
}
