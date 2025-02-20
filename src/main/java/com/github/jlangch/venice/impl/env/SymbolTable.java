/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2025 Venice
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
package com.github.jlangch.venice.impl.env;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.github.jlangch.venice.impl.types.VncSymbol;


public class SymbolTable implements Serializable {

    public SymbolTable() {
        this.symbols = new HashMap<>();
    }

    public SymbolTable(final Map<VncSymbol,Var> symbols) {
        this.symbols = symbols;
    }

    public Map<VncSymbol,Var> getSymbolMap() {
        return symbols;
    }

    public void put(final Var value) {
        symbols.put(value.getName(), value);
    }


    private static final long serialVersionUID = -6061770310338511676L;

    private final Map<VncSymbol,Var> symbols;
}
