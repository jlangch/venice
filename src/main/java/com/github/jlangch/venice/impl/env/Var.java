/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2023 Venice
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

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.beans.Transient;
import java.io.Serializable;
import java.util.List;

import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;


public class Var implements Serializable {

    public Var(final VncSymbol name, final VncVal val, final Scope scope) {
        this(name, val, true, scope);
    }

    public Var(final VncSymbol name, final VncVal val, final boolean overwritable, final Scope scope) {
        this.name = name;
        this.val = val == null ? Nil : val;
        this.overwritable = overwritable;
        this.scope = scope;
    }

    public VncVal getVal() {
        return val;
    }

    public VncSymbol getName() {
        return name;
    }

    public boolean isOverwritable() {
        return overwritable;
    }

    public Scope getScope() {
        return scope;
    }

    @Transient
    public boolean isGlobal() {
        return scope == Scope.Global;
    }

    @Transient
    public boolean isLocal() {
        return scope == Scope.Local;
    }

    @Transient
    public boolean isPrivate() {
        return name.isPrivate();
    }

    @Override
    public String toString() {
        return String.format(
                "{%s %s %s :overwritable %b}",
                this.getClass().getSimpleName(),
                getName().toString(),
                getVal().toString(),
                isOverwritable());
    }

    public String toString(final boolean print_readably) {
        return String.format(
                "{%s %s %s :overwritable %b}",
                this.getClass().getSimpleName(),
                getName().toString(print_readably),
                getVal().toString(print_readably),
                isOverwritable());
    }



    public static Var findVar(final VncSymbol sym, final List<Var> bindings) {
        final int idx = getVarIndex(sym, bindings);
        return idx < 0 ? null : bindings.get(idx);
    }

    public static int getVarIndex(final VncSymbol sym, final List<Var> bindings) {
        for(int ii=0; ii<bindings.size(); ii++) {
            final Var b = bindings.get(ii);
            if (b.getName().equals(sym)) {
                return ii;
            }
        }
        return -1;
    }


    public static enum Scope { Global, Local };


    private static final long serialVersionUID = 1598432086227773369L;

    private final VncSymbol name;
    private final VncVal val;
    private final boolean overwritable;
    private final Scope scope;
}
