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
package com.github.jlangch.venice.impl;

import com.github.jlangch.venice.AssertionException;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.MetaUtil;
import com.github.jlangch.venice.impl.util.callstack.CallFrame;
import com.github.jlangch.venice.impl.util.callstack.WithCallStack;


public class FunctionArgsTypeHints {

    public static void validate(
            final VncSymbol sym,
            final VncVal val
    ) {
    	validate(sym, val, getParamType(sym));
    }

    public static void validate(
            final VncSymbol sym,
            final VncVal val,
            final VncKeyword typeMeta
    ) {
        if (typeMeta != null) {
            // check 'val' type against 'typeMeta'
            if (!Types.isInstanceOf(typeMeta, val)) {
                try (WithCallStack cs = new WithCallStack(callframe(sym))) {
                    throw new AssertionException(String.format(
                            "function argument type not compatible: arg-name=%s, arg-type=%s, expected-type=%s ",
                            sym.getSimpleName(),
                            Types.getType(val).toString(true),
                            typeMeta.toString(true)));
                }
            }
        }
    }

    public static VncKeyword[] getParamTypes(final VncVal[] paramArr) {
        final VncKeyword[] types = new VncKeyword[paramArr.length];

        for(int ii=0; ii<paramArr.length; ii++) {
            final VncVal p = paramArr[ii];
            types[ii] = Types.isVncSymbol(p) ? getParamType((VncSymbol)p) : null;
       }

        return types;
    }

    public static VncKeyword getParamType(final VncSymbol param) {
        final VncVal t = param.getMetaVal(MetaUtil.TYPE);
        if (Types.isVncKeyword(t)) {
            final VncKeyword tkw = (VncKeyword)t;
            if (tkw.hasNamespace()) {
                return tkw;
            }
            else if (Types.isCoreType(tkw.getSimpleName())) {
                // if it's a core type qualify it with core namespace
                return new VncKeyword("core/" + tkw.getSimpleName());
            }
            else {
            	return null;  // not quiet sure what to do in this case!
            }
        }
        else {
            return null;
        }
    }

    private static CallFrame callframe(final VncVal val) {
        return new CallFrame("invalid-argument-type", val.getMeta());
    }

}
