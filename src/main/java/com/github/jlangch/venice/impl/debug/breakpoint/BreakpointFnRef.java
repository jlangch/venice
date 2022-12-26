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
package com.github.jlangch.venice.impl.debug.breakpoint;


/**
 * Defines a reference for function breakpoint
 */
public class BreakpointFnRef {

    public BreakpointFnRef(final String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }


    public String getQualifiedName() {
        return qualifiedName;
    }


    @Override
    public String toString() {
        return qualifiedName;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((qualifiedName == null) ? 0 : qualifiedName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BreakpointFnRef other = (BreakpointFnRef) obj;
        if (qualifiedName == null) {
            if (other.qualifiedName != null)
                return false;
        } else if (!qualifiedName.equals(other.qualifiedName))
            return false;
        return true;
    }


    public static final BreakpointFnRef IF = new BreakpointFnRef("if");
    public static final BreakpointFnRef LET = new BreakpointFnRef("let");
    public static final BreakpointFnRef BINDINGS = new BreakpointFnRef("bindings");
    public static final BreakpointFnRef LOOP = new BreakpointFnRef("loop");

    private final String qualifiedName;
}
