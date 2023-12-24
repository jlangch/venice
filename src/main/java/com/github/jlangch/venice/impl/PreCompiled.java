/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
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

import com.github.jlangch.venice.IPreCompiled;
import com.github.jlangch.venice.Version;
import com.github.jlangch.venice.impl.env.SymbolTable;
import com.github.jlangch.venice.impl.namespaces.NamespaceRegistry;
import com.github.jlangch.venice.impl.types.VncVal;


/**
 * Holds a pre-compiled Venice script
 */
public class PreCompiled implements IPreCompiled {

    public PreCompiled(
            final String scriptName,
            final String script,
            final VncVal precompiled,
            final boolean macroexpand,
            final NamespaceRegistry nsRegistry,
            final SymbolTable symbols
    ) {
        this.scriptName = scriptName;
        this.script = script;
        this.precompiled = precompiled;
        this.macroexpand = macroexpand;
        this.nsRegistry = nsRegistry;
        this.symbols = symbols;
        this.version = Version.VERSION;
    }


    @Override
	public String scriptName() {
    	return scriptName;
    }

    @Override
	public String script() {
    	return script;
    }

    public String getName() {
        return scriptName;
    }

    public String getVersion() {
        return version;
    }

    public boolean isMacroexpand() {
        return macroexpand;
    }

    public VncVal getPrecompiled() {
        return precompiled;
    }

    public NamespaceRegistry getNamespaceRegistry() {
        return nsRegistry;
    }

    public SymbolTable getSymbols() {
        return symbols;
    }



    private final String scriptName;
    private final String script;
    private final VncVal precompiled;
    private final String version;
    private final boolean macroexpand;
    private final NamespaceRegistry nsRegistry;
    private final SymbolTable symbols;
}
