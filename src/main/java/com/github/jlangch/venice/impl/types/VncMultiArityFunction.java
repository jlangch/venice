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
package com.github.jlangch.venice.impl.types;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.github.jlangch.venice.ArityException;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.util.MetaUtil;


public class VncMultiArityFunction extends VncFunction {

    public VncMultiArityFunction(
            final String name,
            final List<VncFunction> functions,
            final boolean macro,
            final VncVal meta
    ) {
        super(name, null, macro, null, meta);

        if (functions == null || functions.isEmpty()) {
            throw new VncException("A multi-arity function must have at least one function");
        }


        int maxFixedArgs = -1;
        for(VncFunction fn : functions) {
            if (!fn.hasVariadicArgs()) {
                maxFixedArgs = Math.max(maxFixedArgs, fn.getFixedArgsCount());
            }
        }

        fixedArgFunctions = new VncFunction[maxFixedArgs+1];

        for(VncFunction fn : functions) {
            if (fn.hasVariadicArgs()) {
                variadicArgFunctions.add(fn);
            }
            else {
                fixedArgFunctions[fn.getFixedArgsCount()] = fn;
            }
        }
    }


    @Override
    public VncMultiArityFunction withMeta(final VncVal meta) {
        super.withMeta(meta);
        return this;
    }

    @Override
    public VncKeyword getType() {
        return new VncKeyword(
                    isMacro() ? TYPE_MACRO : TYPE_FUNCTION,
                    MetaUtil.typeMeta(
                        new VncKeyword(VncVal.TYPE)));
    }

    @Override
    public VncVal apply(final VncList args) {
        return getFunctionForArgs(args).apply(args);
    }

    @Override
    public boolean isNative() {
        return false;
    }

    @Override
    public VncFunction getFunctionForArgs(final VncList args) {
        return getFunctionForArity(args.size());
    }

    @Override
    public VncFunction getFunctionForArity(final int arity) {
        VncFunction fn = arityFunctionCache.get(arity);
        if (fn != null) {
            return fn;
        }

        if (arity < fixedArgFunctions.length) {
            fn = fixedArgFunctions[arity];
        }

        if (fn == null) {
            // with multi-arity functions choose the matching function with
            // highest number of fixed args
            int fixedArgs = -1;
            for(VncFunction candidateFn : variadicArgFunctions) {
                final int candidateFnFixedArgs = candidateFn.getFixedArgsCount();
                if (arity >= candidateFnFixedArgs) {
                    if (candidateFnFixedArgs > fixedArgs) {
                        fixedArgs = candidateFnFixedArgs;
                        fn = candidateFn;
                    }
                }
            }
        }

        if (fn == null) {
            throw new ArityException(String.format(
                    "No matching '%s' multi-arity function for arity %d",
                    getQualifiedName(),
                    arity));
        }

        if (arity < 100) {
            // only cache arities up to 100 otherwise we run into memory problems
            // with with large artites mapped to a '&' param
            arityFunctionCache.put(arity, fn);
        }

        return fn;
    }

    @Override public TypeRank typeRank() {
        return TypeRank.MULTI_ARITY_FUNCTION;
    }

    public VncList getFunctions() {
        final List<VncFunction> list = new ArrayList<>();

        for(VncFunction f : fixedArgFunctions) {
            if (f != null) {
                list.add(f);
            }
        }
        list.addAll(variadicArgFunctions);

        return VncList.ofList(list);
    }


    private static final long serialVersionUID = -1848883965231344442L;

    private final List<VncFunction> variadicArgFunctions = new ArrayList<>();
    private final VncFunction[] fixedArgFunctions;
    private final ConcurrentHashMap<Integer,VncFunction> arityFunctionCache = new ConcurrentHashMap<>();
}
