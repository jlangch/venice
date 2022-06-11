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
package com.github.jlangch.venice.impl.types;

import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.util.MetaUtil;


public class VncVolatile extends VncVal implements IDeref {

    public VncVolatile(final VncVal value, final VncVal meta) {
        super(meta);
        state = value;
    }


    @Override
    public VncVolatile withMeta(final VncVal meta) {
        return new VncVolatile(state, meta);
    }

    @Override
    public VncKeyword getType() {
        return new VncKeyword(
                        TYPE,
                        MetaUtil.typeMeta(
                                new VncKeyword(VncVal.TYPE)));
    }

    public VncVal reset(final VncVal newVal) {
        state = newVal;
        return newVal;
    }

    @Override
    public VncVal deref() {
        return state;
    }

    public VncVal swap(final VncFunction fn, final VncList args) {
        final VncList new_args = VncList.of(state).addAllAtEnd(args);
        state = fn.apply(new_args);
        return state;
    }

    @Override
    public TypeRank typeRank() {
        return TypeRank.VOLATILE;
    }

    @Override
    public Object convertToJavaObject() {
        return null;
    }

    @Override
    public String toString() {
        return "(volatile " + Printer.pr_str(state, true) + ")";
    }

    @Override
    public String toString(final boolean print_machine_readably) {
        return "(volatile " + Printer.pr_str(state, print_machine_readably) + ")";
    }


    public static final String TYPE = ":core/volatile";

    private static final long serialVersionUID = -1848883965231344442L;

    private volatile VncVal state = Constants.Nil;
}
