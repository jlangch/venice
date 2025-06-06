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
package com.github.jlangch.venice.impl.namespaces;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.github.jlangch.venice.impl.types.VncSymbol;


public class NamespaceRegistry implements Serializable {

    public NamespaceRegistry() {
    }

    public NamespaceRegistry add(final Namespace ns) {
        Objects.requireNonNull(ns);
        namespaces.put(ns.getNS(), ns);
        return this;
    }

    public NamespaceRegistry add(final NamespaceRegistry nsRegistry) {
        for(Namespace ns : nsRegistry.namespaces.values()) {
            add(ns);
        }
        return this;
    }

    public NamespaceRegistry remove(final VncSymbol sym) {
        Objects.requireNonNull(sym);
        namespaces.remove(sym);
        return this;
    }

    public Namespace get(final VncSymbol sym) {
        Objects.requireNonNull(sym);
        return namespaces.get(sym);
    }

    public Namespace computeIfAbsent(final VncSymbol sym) {
        Objects.requireNonNull(sym);
        return namespaces.computeIfAbsent(sym, (s) -> new Namespace(s));
    }

    public void clear() {
        namespaces.clear();
    }

    public boolean isEmpty() {
        return namespaces.isEmpty();
    }

    public NamespaceRegistry copy() {
        final NamespaceRegistry nsRegistry = new NamespaceRegistry();

        for(Map.Entry<VncSymbol, Namespace> entry : namespaces.entrySet()) {
            nsRegistry.namespaces.put(entry.getKey(), entry.getValue().copy());
        }

        return nsRegistry;
    }


    private static final long serialVersionUID = 672571759583276084L;

    private final Map<VncSymbol, Namespace> namespaces = new ConcurrentHashMap<>();
}
