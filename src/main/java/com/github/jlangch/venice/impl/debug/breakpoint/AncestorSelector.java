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
package com.github.jlangch.venice.impl.debug.breakpoint;

import com.github.jlangch.venice.impl.types.util.QualifiedName;


public class AncestorSelector {

    public AncestorSelector(
            final QualifiedName ancestorQN,
            final AncestorType type
    ) {
        if (ancestorQN == null) {
            throw new RuntimeException("A ancestorQN must not be null");
        }
        if (type == null) {
            throw new RuntimeException("A type must not be null");
        }

        this.ancestorQN = ancestorQN;
        this.type = type;
    }


    public QualifiedName getAncestor() {
        return ancestorQN;
    }

    public AncestorType getType() {
        return type;
    }

    public String formatForBaseFn(final String fnName) {
        return String.format(
                "%s %s %s",
                ancestorQN.getQualifiedName(),
                type.symbol(),
                fnName);
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ancestorQN == null) ? 0 : ancestorQN.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        AncestorSelector other = (AncestorSelector) obj;
        if (ancestorQN == null) {
            if (other.ancestorQN != null)
                return false;
        } else if (!ancestorQN.equals(other.ancestorQN))
            return false;
        if (type != other.type)
            return false;
        return true;
    }


    private final QualifiedName ancestorQN;
    private final AncestorType type;
}
