/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2020 Venice
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

	public static Set<String> getIoFunctions() {
		return new HashSet<>(
				Arrays.asList(
					// print
					"print",
					"printf",
					"println",
					"newline",

					// load (macros)
					"load-file",
					"load-classpath-file",
					"*load-file",
					"*load-classpath-file",

					// system
					"gc",
					"shutdown-hook",
					"sh",
					"system-prop",
					
					// concurrency
					"deliver",
					"future",
					"future?",
					"future-cancel",
					"future-cancelled?",
					"future-done?",
					"futures-fork",
					"futures-wait",
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
					
					// scheduler
					"schedule-delay",
					"schedule-at-fixed-rate",

					// I/O
					"io/copy-file",
					"io/copy-stream",
					"io/delete-file",
					"io/delete-file-on-exit",
					"io/delete-file-tree",
					"io/download",
					"io/exists-dir?",
					"io/exists-file?",
					"io/file-size",
					"io/list-file-tree",
					"io/list-files",
					"io/list-files-glob",
					"io/load-classpath-resource",
					"io/move-file",
					"io/mkdir",
					"io/mkdirs",
					"io/slurp",
					"io/slurp-lines",
					"io/slurp-stream",
					"io/spit",
					"io/spit-stream",
					"io/temp-dir",
					"io/temp-file",
					"io/tmp-dir",
					"io/user-dir",
					"io/wait-for",
					
					// I/O zip
					"io/zip",
					"io/zip-append",
					"io/zip-remove",
					"io/zip-file",
					"io/zip-list",
					"io/unzip",
					"io/unzip-first",
					"io/unzip-nth",
					"io/unzip-all",
					"io/unzip-to-dir",
					"io/zip-size",
					"io/gzip",
					"io/gzip-to-stream",
					"io/ungzip",
					"io/ungzip-to-stream"));
	}

}
