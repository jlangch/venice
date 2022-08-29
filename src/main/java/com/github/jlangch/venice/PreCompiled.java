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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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

    /**
     * Serializes this pre-compiled Venice script
     *
     * @return the serialized byte array
     */
    public byte[] serialize() {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (GZIPOutputStream gzos = new GZIPOutputStream(baos, true)) {
            new ObjectOutputStream(gzos).writeObject(this);
            gzos.flush();
            return baos.toByteArray();
        }
        catch (Exception ex) {
            throw new RuntimeException("Failed to serialize pre-compiled Venice script", ex);
        }
    }

    /**
     * Deserializes a pre-compiled Venice script
     *
     * @param precompiled a serialized, pre-compiled Venice script
     * @return the deserialized pre-compiled script
     * @throws IncompatibleVersionException
     *              if the Venice version of the deserialized script
     *              does not match this Venice version
     */
    public static PreCompiled deserialize(final byte[] precompiled) {
        final PreCompiled preCompiled = deserialize_(precompiled);
        if (!preCompiled.version.equals(Version.VERSION)) {
            throw new IncompatibleVersionException(String.format(
                    "Failed to deserialize pre-compiled Venice script. "
                        + "The pre-compiled version %s does not match this Venice version %s",
                    preCompiled.version,
                    Version.VERSION));
        }
        return preCompiled;
    }

    private static PreCompiled deserialize_(final byte[] precompiled) {
        final ByteArrayInputStream bais = new ByteArrayInputStream(precompiled);

        try (GZIPInputStream gzis = new GZIPInputStream(bais)) {
            return (PreCompiled)new ObjectInputStream(gzis).readObject();
        }
        catch (Exception ex) {
            throw new RuntimeException("Failed to deserialize pre-compiled Venice script", ex);
        }
    }


    private static final long serialVersionUID = -3044466744877602703L;

    private final String name;
    private final Object precompiled;
    private final String version;
    private final boolean macroexpand;
    private final NamespaceRegistry nsRegistry;
    private final SymbolTable symbols;
}
