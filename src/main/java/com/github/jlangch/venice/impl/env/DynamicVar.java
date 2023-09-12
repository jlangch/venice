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

import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;


public class DynamicVar extends Var {

    public DynamicVar(final VncSymbol name, final VncVal val, final Scope scope) {
        super(name, val, scope);
        th_keyword = new VncKeyword(name.getQualifiedName());
    }

    @Override
    public VncVal getVal() {
        return peekVal();
    }

    public void setVal(final VncVal val) {
        ThreadContext.setValue(th_keyword, val == null ? Nil : val);
    }

    public void pushVal(final VncVal val) {
        ThreadContext.pushValue(th_keyword, val == null ? Nil : val);
    }

    public VncVal peekVal() {
        final VncVal thVal = ThreadContext.peekValue(th_keyword);
        return thVal == Nil ? super.getVal() : thVal;
    }

    public VncVal popVal() {
        final VncVal thVal = ThreadContext.popValue(th_keyword);
        return thVal == Nil ? super.getVal() : thVal;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public String toString(final boolean print_readably) {
        return super.toString(print_readably);
    }


    private static final long serialVersionUID = 1598432086227773369L;

    private final VncKeyword th_keyword;
}
