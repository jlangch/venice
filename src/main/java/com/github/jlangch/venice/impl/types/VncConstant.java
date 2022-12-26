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
package com.github.jlangch.venice.impl.types;

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import com.github.jlangch.venice.impl.util.MetaUtil;


public class VncConstant extends VncScalar {

    public VncConstant(final String name) {
        super(Nil);
        value = name;
    }


    @Override
    public VncConstant withMeta(final VncVal meta) {
        return this;
    }

    @Override
    public VncKeyword getType() {
        if (this == Constants.Nil) {
            return new VncKeyword(
                    ":core/nil",
                    MetaUtil.typeMeta(new VncKeyword(VncVal.TYPE)));
        }
        else {
            return null;
        }
    }

    public String getValue() {
        return value;
    }

    @Override
    public TypeRank typeRank() {
        return TypeRank.CONSTANT;
    }

    @Override
    public Object convertToJavaObject() {
        if (this == Constants.Nil) {
            return null;
        }
        else {
            return null;
        }
    }

    @Override
    public int compareTo(final VncVal o) {
        if (this == Nil) {
            return o == Nil ? 0 : -1;
        }
        else if (o == Nil) {
            return 1;
        }

        return super.compareTo(o);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        VncConstant other = (VncConstant) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return value;
    }


    private static final long serialVersionUID = -1848883965231344442L;

    private final String value;
}
