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
package com.github.jlangch.venice.impl.functions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class IOFnBlacklisted {

	public static Set<String> getAllIoFunctions() {
		return new HashSet<>(Arrays.asList(
								// load (macros)
								"load-file",
								"load-classpath-file",

								// system
								"gc",
								"sleep",
								"sh",
								"system-prop",
								
								// concurrency
								"deliver",
								"future",
								"future?",
								"future-cancel",
								"future-cancelled?",
								"future-done?",
								"promise",
								"promise?",
								"agent",
								"send",
								"send-off",
								"restart-agent",
								"set-error-handler!",
								"agent-error",
								"agent-error-mode",
								"await",
								"await-for",
								"shutdown-agents",
								"shutdown-agents?",
								"await-termination-agents",
								"await-termination-agents?",

								// I/O
								"io/copy-file",
								"io/delete-file",
								"io/delete-file-on-exit",
								"io/exists-dir?",
								"io/exists-file?",
								"io/file-size",
								"io/list-files",
								"io/move-file",
								"io/mkdir",
								"io/mkdirs",
								"io/slurp",
								"io/slurp-lines",
								"io/spit",
								"io/temp-file",
								"io/tmp-dir",
								"io/user-dir",
								"io/load-classpath-resource"));
	}

}
