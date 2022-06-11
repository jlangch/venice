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
package com.github.jlangch.venice.impl.specialforms.util;

import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;


public class CatchBlock {

    public CatchBlock(
            final VncSymbol exSym,
            final VncList body,
            final VncVal meta
    ) {
        this.exSym = exSym;
        this.body = body;
        this.meta = meta;
    }


    public VncSymbol getExSym() {
        return exSym;
    }

    public VncList getBody() {
        return body;
    }

    public VncVal getMeta() {
        return meta;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("SYM: ");
        sb.append(exSym);
        sb.append("\n");
        sb.append(body);

        return sb.toString();
    }


    private final VncSymbol exSym;
    private final VncList body;
    private final VncVal meta;
}
