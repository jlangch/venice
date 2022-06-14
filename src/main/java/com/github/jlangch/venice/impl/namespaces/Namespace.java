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
package com.github.jlangch.venice.impl.namespaces;

import java.util.concurrent.ConcurrentHashMap;

import com.github.jlangch.venice.impl.javainterop.JavaImports;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;


public class Namespace {

    public Namespace(final VncSymbol ns) {
        this.ns = ns == null ? Namespaces.NS_USER : ns;
    }

    public VncSymbol getNS() {
        return ns;
    }

    public void addAlias(final String alias, final String ns) {
        aliases.put(alias, ns);
    }

    public void removeAlias(final String alias) {
        aliases.remove(alias);
    }

    public String lookupByAlias(final String alias) {
        return aliases.get(alias);
    }

    public VncMap listAliases() {
        return aliases
                .entrySet()
                .stream()
                .map(e -> VncHashMap.of(new VncSymbol(e.getKey()),
                                        new VncSymbol(e.getValue())))
                .reduce(new VncHashMap(), (x,y) -> x.putAll(y));
    }

    public JavaImports getJavaImports() {
        return javaImports;
    }

    public VncList getJavaImportsAsVncList() {
        return javaImports.list();
    }

    @Override
    public String toString() {
        return ns.getName();
    }


    private final VncSymbol ns;
    private final JavaImports javaImports = new JavaImports();
    private final ConcurrentHashMap<String,String> aliases = new ConcurrentHashMap<>();
}
