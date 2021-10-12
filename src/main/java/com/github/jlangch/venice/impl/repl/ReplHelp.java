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

import com.github.jlangch.venice.Venice;


public class ReplHelp {
	
	public final static String COMMANDS =
			"Venice REPL: V" + Venice.getVersion() + "\n\n" +
			"Commands:\n" +
			"  !reload      reload Venice environment\n" +
			"  !restart     restart the REPL.\n" +
			"               note: the REPL launcher script must support REPL\n" +
			"                     restarting.\n" +
			"  !, !?, !help help\n" +
			"  !darkmode    switch to Venice's dark color theme\n" +
			"  !lightmode   switch to Venice's light color theme\n" +
			"  !info        show REPL setup context data\n" +
			"  !config      show a sample REPL config\n" +
			"  !classpath   show the REPL classpath\n" +
			"  !loadpath    show the REPL loadpath\n" +
			"  !highlight   turn highlighting dynamically on or off\n" +
			"                 !highlight {on/off}\n" +
			"  !macroexpand enable macro expansion while loading files and\n" +
			"               modules. \n" +
			"               This can speed-up script execution by a factor 3\n" +
			"               or 5 and even more with complex code!\n" +
			"  !env         print env symbols:\n" +
			"                 !env print {symbol-name}\n" +
			"                 !env global\n" +
			"                 !env global io/*\n" +
			"                 !env global *file*\n" +
			"  !sandbox     sandbox\n" +	
			"                 !sandbox status\n" +
			"                 !sandbox config\n" +
			"                 !sandbox accept-all\n" +
			"                 !sandbox reject-all\n" +
			"                 !sandbox customized\n" +
			"                 !sandbox add-rule rule\n" +
			"  !java-ex     print Java exception\n" +
			"                 !java-ex\n" +
			"                 !java-ex {on/off}\n" +
			"  !hist clear  clear the history\n" +
			"  !quit, !q    quit the REPL\n\n" +
			"Drag&Drop: \n" +
			"  Scripts can be dragged to the REPL. Upon pressing [return]\n" +
			"  the  REPL loads the script through the dropped absolute or\n" +
			"  relative filename. If the script has less than 20 lines it's\n" +
			"  source is displayed.\n\n" +
			"History: \n" +
			"  A history of the last three result values is kept by\n" +
			"  the REPL, accessible through the symbols `*1`, `*2`, `*3`,\n" +
			"  and `**`. E.g. (printl *1)\n\n" +
			"Shortcuts:\n" +
			"  ctrl-A   move the cursor to the start\n" +
			"  ctrl-C   stop the running command, cancel a multi-line edit,\n" +
			"           or break out of the REPL\n" +
			"  ctrl-E   move the cursor to the end\n" +
			"  ctrl-K   remove the text after the cursor and store it in a\n" +
			"           cut-buffer\n" +
			"  ctrl-L   clear the screen\n" +
			"  ctrl-Y   yank the text from the cut-buffer\n" +
			"  ctrl-_   undo\n";

	public final static String ENV =
			"Please choose from:\n" +
			"   !env print {symbol-name}\n" +
			"   !env global\n" +
			"   !env global io/*\n" +
			"   !env global *file*\n";

	public final static String APP =
			"Please pass an app name:\n" +
			"   !app {app-name}\n";

	public final static String SANDBOX =
			"Please choose from:\n" +
			"   !sandbox status\n" +
			"   !sandbox config\n" +
			"   !sandbox accept-all\n" +
			"   !sandbox reject-all\n" +
			"   !sandbox customized\n" +
			"   !sandbox add-rule class:java.lang.Math:*\n" +
			"   !sandbox add-rule system.property:os.name\n" +
			"   !sandbox add-rule blacklist:venice:func:io/exists-dir?\n" +
			"   !sandbox add-rule blacklist:venice:func:*io*\n" +
			"   !sandbox add-rule venice:module:shell\n";
}
