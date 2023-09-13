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
package com.github.jlangch.venice.impl;

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncMapEntry;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.callstack.CallFrame;
import com.github.jlangch.venice.impl.util.callstack.WithCallStack;


public class Destructuring {

    // scalar binding
    // x 10                                     -> x: 10

    // sequential destructuring on vector
    // [x y] [10 20]                            -> x: 10, y: 20
    // [x _ y] [10 20 30]                       -> x: 10, y: 30
    // [x y & z] [10 20 30 40 50]               -> x: 10, y: 20, z: [30 40 50]
    // [x y :as all] [10 20 30 40 50]           -> x: 10, y: 20, all: [10 20 30 40 50]
    // [x y & z :as all] [10 20 30 40 50]       -> x: 10, y: 20, z: [30 40 50] all: [10 20 30 40 50]
    // [[v x & y] z] [[10 20 30 40] 50]         -> v: 10, x: 20, y: [30 40], z: 50

    // associative destructuring on map
    // {a :a b :b} {:a 1 :b 2 :c 3}               -> a: 1, :b 2
    // {:keys [a b]} {:a 1 :b 2 :c 3}             -> a: 1, :b 2
    // {:syms [a b]} {'a 1 'b 2 'c 3}             -> a: 1, :b 2
    // {:strs [a b]} {"a" 1 "b" 2 "c" 3}          -> a: 1, :b 2
    // {:keys [a b] :as all} {:a 1 :b 2 :c 3}     -> a: 1, :b 2, all: {:a 1 :b 2 :c 3}
    // {:syms [a b] :as all} {'a 1 'b 2 'c 3}     -> a: 1, :b 2, all: {'a 1 'b 2 'c 3}
    // {:strs [a b] :as all} {"a" 1 "b" 2 "c" 3}  -> a: 1, :b 2, all: {"a" 1 "b" 2 "c" 3}
    // {:keys [a b] :or {:b 2}} {:a 1 :c 3}       -> a: 1, :b 2
    // {:syms [a b] :or {'b 2}} {'a 1 'c 3}       -> a: 1, :b 2
    // {:strs [a b] :or {"b" 2}} {"a" 1 "c" 3}    -> a: 1, :b 2

    // associative destructuring on map nested
    // {a :a, {x :x, y :y} :c} {:a 1, :b 2, :c {:x 10, :y 11}}   -> a: 1, :b 2, x: 10, y: 11

    // associative destructuring on vector
    // [x {:keys [a b]}] [10 {:a 1 :b 2 :c 3}]  -> a: 1, :b 2

    public static List<Var> destructure(
            final VncVal symVal,
            final VncVal bindVal
    ) {
        final List<Var> bindings = new ArrayList<>();

        if (Types.isVncSymbol(symVal)) {
            // scalar value binding [n 10]
            bindings.add(new Var((VncSymbol)symVal, bindVal, Var.Scope.Local));
        }
        else if (Types.isVncSequence(symVal)) {
            // sequential destructuring

            if (Types.isVncSequence(bindVal)) {
                sequential_list_destructure(
                        (VncSequence)symVal,
                        (VncSequence)bindVal,
                        bindVal,
                        bindings);
            }
            else if (Types.isVncString(bindVal)) {
                // turn into a list destructuring
                sequential_list_destructure(
                        (VncSequence)symVal,
                        ((VncString)bindVal).toVncList(),
                        (VncString)bindVal,
                        bindings);
            }
            else if (Types.isVncMapEntry(bindVal)) {
                sequential_list_destructure(
                        (VncSequence)symVal,
                        ((VncMapEntry)bindVal).toVector(),
                        bindVal,
                        bindings);
            }
            else if (bindVal == Nil) {
                sequential_list_destructure(
                        (VncSequence)symVal,
                        VncList.empty(),
                        bindVal,
                        bindings);
            }
            else {
                try (WithCallStack cs = new WithCallStack(callframe(bindVal))) {
                    throw new VncException(
                            String.format(
                                    "Invalid sequential destructuring bind value type %s. "
                                    + "Expected list, vector, or string.",
                                    Types.getType(bindVal)));
                }
            }
        }
        else if (Types.isVncMap(symVal)) {
            // associative destructuring

            if (Types.isVncMap(bindVal)) {
                associative_map_destructure((VncMap)symVal, (VncMap)bindVal, bindings);
            }
            else if (Types.isVncVector(bindVal)) {
                try (WithCallStack cs = new WithCallStack(callframe(bindVal))) {
                    throw new VncException(
                            String.format(
                                    "Associative destructuring on vector is not yet implemented."));
                }
            }
            else if (bindVal == Nil) {
                associative_map_destructure((VncMap)symVal, new VncHashMap(), bindings);
            }
            else {
                try (WithCallStack cs = new WithCallStack(callframe(bindVal))) {
                    throw new VncException(
                            String.format(
                                    "Invalid associative destructuring bind value type %s. Expected map.",
                                    Types.getType(bindVal)));
                }
            }
        }
        else {
            try (WithCallStack cs = new WithCallStack(callframe(symVal))) {
                throw new VncException(
                        String.format(
                                "Invalid destructuring sym value type %s. Expected symbol.",
                                Types.getType(symVal)));
            }
        }

        return bindings;
    }

    public static boolean isFnParamsWithoutDestructuring(final VncVector params) {
        for(int ii=0; ii<params.size(); ii++) {
            final VncVal param = params.nth(ii);
            if (param instanceof VncSymbol) {
                final String sym = ((VncSymbol)param).getName();
                if (sym.equals("_") || sym.equals("&")) return false;
            }
            else {
                return false;
            }
        }

        return true;
    }


    private static void sequential_list_destructure(
            final VncSequence symVals,
            final VncSequence bindVals,
            final VncVal originalBindVal,
            final List<Var> bindings
    ) {
        // [[x y] [10 20]]
        // [[x _ y _ z] [10 20 30 40 50]]
        // [[x y & z] [10 20 30 40 50]]
        // [[x y & z] [10 20]]   => x=10, y=20, z=nil
        // [[x y & z] [10]]      => x=10, y=nil, z=nil
        // [[x y :as all] [10 20 30 40 50]]
        // [[x y & z :as all] [10 20 30 40 50]]
        // [[x [y z]] [10 [20 30]]]

        VncSequence symValsRest = symVals;
        VncSequence bindValsRest = bindVals;

        while(!symValsRest.isEmpty()) {
            final VncVal symVal = symValsRest.first();
            symValsRest = symValsRest.rest();

            if (Types.isVncSymbol(symVal)) {
                final String symName = ((VncSymbol)symVal).getName();

                if (symName.equals("_")) {
                    // skip
                    bindValsRest = bindValsRest.rest();
                }
                else if (symName.equals("&")) {
                    final VncSymbol sym = (VncSymbol)symValsRest.first();
                    symValsRest = symValsRest.rest();
                    final VncVal bindVal = bindValsRest.isEmpty() ? Nil : bindValsRest;
                    bindValsRest = VncList.empty();
                    bindings.add(new Var(sym, bindVal, Var.Scope.Local));
                }
                else {
                    final VncSymbol sym = (VncSymbol)symVal;
                    final VncVal bindVal = bindValsRest.first();
                    bindValsRest = bindValsRest.rest();
                    FunctionArgsTypeHints.validate(sym, bindVal);
                    bindings.add(new Var(sym, bindVal, Var.Scope.Local));
                }
            }
            else if (isAsKeyword(symVal)) {
                // [[:as all] [10 20 30]]
                final VncSymbol sym = (VncSymbol)symValsRest.first();
                symValsRest = symValsRest.rest();
                bindings.add(new Var(sym, originalBindVal, Var.Scope.Local)); // bind the original values
            }
            else if (Types.isVncSequence(symVal)) {
                // nested destructuring
                final VncVal bindVal = bindValsRest.isEmpty() ? VncList.empty() : bindValsRest.first();
                bindValsRest = bindValsRest.rest();
                bindings.addAll(destructure(symVal, bindVal));
            }
            else if (Types.isVncMap(symVal)) {
                // associative destructuring
                final VncVal bindVal = bindValsRest.isEmpty() ? VncHashMap.empty() : bindValsRest.first();
                bindValsRest = bindValsRest.rest();
                associative_map_destructure((VncMap)symVal, (VncMap)bindVal, bindings);
            }
        }
    }

    private static void associative_map_destructure(
            final VncMap symVals,
            final VncMap bindVals,
            final List<Var> bindings
    ) {
        // {:keys [a b]} {:a 1 :b 2 :c 3}           -> a: 1, :b 2
        // {:syms [a b]} {'a 1 'b 2 'c 3}           -> a: 1, :b 2
        // {:strs [a b]} {"a" 1 "b" 2 "c" 3}        -> a: 1, :b 2

        // {a :a b :b} {:a 1 :b 2 :c 3}             -> a: 1, :b 2

        final List<Var> local_bindings = new ArrayList<>();


        final List<VncVal> symbols = sortAssociativeNames(symVals.keys());

        for(int ii = 0; ii<symbols.size(); ii++) {
            final VncVal symValName = symbols.get(ii);

            if (symValName.equals(KEYWORD_KEYS)) {
                final VncVal symbol = symVals.get(KEYWORD_KEYS);
                if (Types.isVncVector(symbol)) {
                    for(VncVal sym : ((VncVector)symbol)) {
                        final VncSymbol s = (VncSymbol)sym;
                        final VncVal v = bindVals.get(new VncKeyword(s.getName()));
                        FunctionArgsTypeHints.validate(s, v);
                        local_bindings.add(new Var(s, v, Var.Scope.Local));
                    }
                }
                else {
                    try (WithCallStack cs = new WithCallStack(callframe(symbol))) {
                        throw new VncException(
                                    String.format(
                                            "Invalid associative destructuring with :keys symbol "
                                            + "type %s. Expected vector.",
                                            Types.getType(symbol)));
                    }
                }
            }
            else if (symValName.equals(KEYWORD_SYMS)) {
                final VncVal symbol = symVals.get(KEYWORD_SYMS);
                if (Types.isVncVector(symbol)) {
                    for(VncVal sym : ((VncVector)symbol)) {
                        final VncSymbol s = (VncSymbol)sym;
                        final VncVal v = bindVals.get(s);
                        FunctionArgsTypeHints.validate(s, v);
                        local_bindings.add(new Var(s, v, Var.Scope.Local));
                    }
                }
                else {
                    try (WithCallStack cs = new WithCallStack(callframe(symbol))) {
                        throw new VncException(
                                    String.format(
                                            "Invalid associative destructuring with :syms "
                                            + "symbol type %s. Expected vector.",
                                            Types.getType(symbol)));
                    }
                }
            }
            else if (symValName.equals(KEYWORD_STRS)) {
                final VncVal symbol = symVals.get(KEYWORD_STRS);
                if (Types.isVncVector(symbol)) {
                    for(VncVal sym : ((VncVector)symbol)) {
                        final VncSymbol s = (VncSymbol)sym;
                        final VncVal v = bindVals.get(new VncString(s.getName()));
                        FunctionArgsTypeHints.validate(s, v);
                        local_bindings.add(new Var(s, v, Var.Scope.Local));
                    }
                }
                else {
                    try (WithCallStack cs = new WithCallStack(callframe(symbol))) {
                        throw new VncException(
                                String.format(
                                        "Invalid associative destructuring with "
                                        + ":strs symbol type %s. Expected vector.",
                                        Types.getType(symbol)));
                    }
                }
            }
            else if (symValName.equals(KEYWORD_OR)) {
                final VncVal symbol = symVals.get(KEYWORD_OR);
                if (symbol != Nil && Types.isVncMap(symbol)) {
                    for(Map.Entry<VncVal,VncVal> e : ((VncMap)symbol).getJavaMap().entrySet()) {
                        final int bIdx = Var.getVarIndex((VncSymbol)e.getKey(), local_bindings);
                        if (bIdx == -1) {
                            local_bindings.add(new Var((VncSymbol)e.getKey(), e.getValue(), Var.Scope.Local));

                        }
                        else {
                            final Var b = local_bindings.get(bIdx);
                            if (b.getVal() == Nil) {
                                local_bindings.set(bIdx, new Var((VncSymbol)e.getKey(), e.getValue(), Var.Scope.Local));
                            }
                        }
                    }
                }
            }
            else if (symValName.equals(KEYWORD_AS)) {
                final VncVal symbol = symVals.get(KEYWORD_AS);
                if (symbol != Nil && Types.isVncSymbol(symbol)) {
                    local_bindings.add(new Var((VncSymbol)symbol, bindVals, Var.Scope.Local));
                }
            }
            else if (Types.isVncMap(symValName)) {
                // nested associative destructuring
                final VncVal v = bindVals.get(symVals.get(symValName));
                associative_map_destructure(
                        (VncMap)symValName,
                        v == Nil ? new VncHashMap() : (VncMap)v,
                        local_bindings);
            }
            else if (Types.isVncVector(symValName)) {
                // nested sequential destructuring
                final VncSequence nestedBindVals = (VncSequence)bindVals.get(symVals.get(symValName));
                sequential_list_destructure(
                        (VncVector)symValName,
                        nestedBindVals,
                        nestedBindVals,
                        local_bindings);
            }
            else if (Types.isVncList(symValName)) {
                // nested sequential destructuring
                final VncSequence nestedBindVals = (VncSequence)bindVals.get(symVals.get(symValName));
                sequential_list_destructure(
                        (VncList)symValName,
                        nestedBindVals,
                        nestedBindVals,
                        local_bindings);
            }
            else if (Types.isVncSymbol(symValName)) {
                final VncVal s = symVals.get(symValName);
                final VncVal v = bindVals.get(s);
                local_bindings.add(new Var((VncSymbol)symValName, v, Var.Scope.Local));
            }
            else {
                try (WithCallStack cs = new WithCallStack(callframe(symValName))) {
                    throw new VncException(
                            String.format(
                                    "Invalid associative destructuring name type %s.",
                                    Types.getType(symValName)));
                }
            }
        }

        bindings.addAll(local_bindings);
    }

    private static boolean isAsKeyword(final VncVal val) {
        return Types.isVncKeyword(val) && ((VncKeyword)val).equals(KEYWORD_AS);
    }

    private static List<VncVal> sortAssociativeNames(final VncList names) {
        final List<VncVal> sorted = new ArrayList<>();

        for(VncVal n : names) {
            if (is_KEYS_SYMS_STRS(n)) {
                sorted.add(n);
            }
        }

        for(VncVal n : names) {
            if (n != Nil && !(is_KEYS_SYMS_STRS(n) || is_AS_OR(n))) {
                sorted.add(n);
            }
        }

        for(VncVal n : names) {
            if (is_AS_OR(n)) {
                sorted.add(n);
            }
        }

        return sorted;
    }

    private static boolean is_KEYS_SYMS_STRS(final VncVal n) {
        return n.equals(KEYWORD_KEYS) || n.equals(KEYWORD_SYMS) || n.equals(KEYWORD_STRS);
    }

    private static boolean is_AS_OR(final VncVal n) {
        return n.equals(KEYWORD_AS) || n.equals(KEYWORD_OR);
    }

    private static CallFrame callframe(final VncVal val) {
        return new CallFrame("destructuring", val.getMeta());
    }


    private static final VncKeyword KEYWORD_AS = new VncKeyword(":as");
    private static final VncKeyword KEYWORD_OR = new VncKeyword(":or");
    private static final VncKeyword KEYWORD_KEYS = new VncKeyword(":keys");
    private static final VncKeyword KEYWORD_SYMS = new VncKeyword(":syms");
    private static final VncKeyword KEYWORD_STRS = new VncKeyword(":strs");
}
