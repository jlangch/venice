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
package com.github.jlangch.venice.impl;

import java.util.List;

import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.types.VncVal;


public interface IVeniceInterpreter {

	void initNS();
	
	void sealSystemNS();
	
	void setMacroExpandOnLoad(boolean macroExpandOnLoad, Env env);
	
	boolean isMacroExpandOnLoad();

	VncVal READ(String script, String filename);

	VncVal EVAL(VncVal ast, Env env);

	VncVal MACROEXPAND(VncVal ast, Env env);

	VncVal RE(String script, String name, Env env);

	String PRINT(VncVal exp);

	Env createEnv(
			boolean macroexpandOnLoad, 
			boolean ansiTerminal, 
			RunMode runMode);

	Env createEnv(
			List<String> preloadedExtensionModules,
			boolean macroExpandOnLoad, 
			boolean ansiTerminal, 
			RunMode runMode);

	List<String> getAvailableModules();

}
