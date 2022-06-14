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

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.namespaces.Namespaces;
import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.util.CallFrame;
import com.github.jlangch.venice.impl.util.WithCallStack;


public class SpecialFormsUtil {

    public static void specialFormCallValidation(final String name) {
        ThreadContext.getInterceptor().validateVeniceFunction(name);
    }

    public static VncSymbol evaluateSymbolMetaData(
            final VncVal symVal,
            final Env env,
            final SpecialFormsContext ctx
    ) {
        final VncSymbol sym = Coerce.toVncSymbol(symVal);
        validateNotReservedSymbol(sym);
        return sym.withMeta(ctx.getEvaluator().evaluate(sym.getMeta(), env, false));
    }

    public static VncSymbol validateSymbolWithCurrNS(
            final VncSymbol sym,
            final String specialFormName
    ) {
        if (sym != null) {
            // do not allow to hijack another namespace
            final String ns = sym.getNamespace();
            if (ns != null && !ns.equals(Namespaces.getCurrentNS().getName())) {
                final CallFrame cf = new CallFrame(specialFormName, sym.getMeta());
                try (WithCallStack cs = new WithCallStack(cf)) {
                    throw new VncException(String.format(
                            "Special form '%s': Invalid use of namespace. "
                                + "The symbol '%s' can only be defined for the "
                                + "current namespace '%s'.",
                            specialFormName,
                            sym.getSimpleName(),
                            Namespaces.getCurrentNS().toString()));
                }
            }
        }

        return sym;
    }

    public static VncVal evaluateBody(
            final VncList body,
            final SpecialFormsContext ctx,
            final Env env,
            final boolean withTailPosition
    ) {
        ctx.getValuesEvaluator()
           .evaluate_values(body.butlast(), env);

        return ctx.getEvaluator()
                  .evaluate(body.last(), env, withTailPosition);
    }

    /**
     * Resolves a class name.
     *
     * @param className A simple class name like 'Math' or a class name
     *                  'java.lang.Math'
     * @return the mapped class 'Math' -&gt; 'java.lang.Math' or the passed
     *         value if a mapping does nor exist
     */
    public static String resolveClassName(final String className) {
        return Namespaces
                    .getCurrentNamespace()
                    .getJavaImports()
                    .resolveClassName(className);
    }


    private static void validateNotReservedSymbol(final VncSymbol symbol) {
        if (symbol != null) {
            if (symbol.isSpecialFormName()) {
                try (WithCallStack cs = new WithCallStack(new CallFrame(symbol.getQualifiedName(), symbol.getMeta()))) {
                    throw new VncException(
                            String.format(
                                    "The special form name '%s' can not be used a symbol.",
                                    symbol.getName()));
                }
            }
            if (symbol.isReservedName()) {
                try (WithCallStack cs = new WithCallStack(new CallFrame(symbol.getQualifiedName(), symbol.getMeta()))) {
                    throw new VncException(
                            String.format(
                                    "Reserved name '%s' can not be used a symbol.",
                                    symbol.getName()));
                }
            }
        }
    }

}
