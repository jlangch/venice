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
package com.github.jlangch.venice.impl.types.custom;

import java.util.concurrent.atomic.AtomicReference;

import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.TypeRank;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.util.MetaUtil;


public abstract class VncCustomBaseTypeDef extends VncVal {

    public VncCustomBaseTypeDef(final VncKeyword type) {
        super(Constants.Nil);

        this.type = type.withMeta(MetaUtil.typeMeta());
    }

    public abstract VncMap toMap();

    @Override
    public VncVal withMeta(final VncVal meta) {
        return this; // not supported
    }

    @Override
    public VncKeyword getType() {
        return type.withMeta(
                MetaUtil.typeMeta(new VncKeyword(VncVal.TYPE)));
    }

    @Override
    public TypeRank typeRank() {
        return TypeRank.CUSTOM_TYPE_DEF;
    }

    @Override
    public Object convertToJavaObject() {
        return null; // not supported
    }

    @Override
    public int compareTo(final VncVal o) {
        if (o == Constants.Nil) {
            return 1;
        }
        else if (o instanceof VncCustomBaseTypeDef) {
            return type.getValue().compareTo(((VncCustomBaseTypeDef)o).type.getValue());
        }

        return super.compareTo(o);
    }

    @Override
    public String toString() {
        return ":" + type.getValue();
    }

    @Override
    public String toString(final boolean print_machine_readably) {
        return toString();
    }

    public void setCustomToStringFn(final VncFunction fn) {
        customToStringFn.set(fn);
    }

    public VncFunction getCustomToStringFn() {
        return customToStringFn.get();
    }

    public void setCustomCompareToFn(final VncFunction fn) {
        customCompareToFn.set(fn);
    }

    public VncFunction getCustomCompareToFn() {
        return customCompareToFn.get();
    }


    private static final long serialVersionUID = -1639883423759533879L;

    private final VncKeyword type;
    private final AtomicReference<VncFunction> customToStringFn = new AtomicReference<>();
    private final AtomicReference<VncFunction> customCompareToFn = new AtomicReference<>();
}
