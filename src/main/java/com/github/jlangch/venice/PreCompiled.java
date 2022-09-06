/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2022 Venice
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
package com.github.jlangch.venice;

import java.io.Serializable;

import com.github.jlangch.venice.impl.env.SymbolTable;
import com.github.jlangch.venice.impl.namespaces.NamespaceRegistry;


/**
 * Holds a pre-compiled Venice script
 */
public class PreCompiled implements Serializable {

    public PreCompiled(
            final String name,
            final Object precompiled,
            final boolean macroexpand,
            final NamespaceRegistry nsRegistry,
            final SymbolTable symbols
    ) {
        this.name = name;
        this.precompiled = precompiled;
        this.macroexpand = macroexpand;
        this.nsRegistry = nsRegistry;
        this.symbols = symbols;
        this.version = Version.VERSION;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public boolean isMacroexpand() {
        return macroexpand;
    }

    public Object getPrecompiled() {
        return precompiled;
    }

    public Object getNamespaceRegistry() {
        return nsRegistry;
    }

    public Object getSymbols() {
        return symbols;
    }


    private static final long serialVersionUID = -3044466744877602703L;

    private final String name;
    private final Object precompiled;
    private final String version;
    private final boolean macroexpand;
    private final NamespaceRegistry nsRegistry;
    private final SymbolTable symbols;
}
