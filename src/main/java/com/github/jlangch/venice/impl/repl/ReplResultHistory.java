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
package com.github.jlangch.venice.impl.repl;

import java.util.LinkedList;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;


public class ReplResultHistory {

    public ReplResultHistory(final int max) {
        this.max = max;
        IntStream.range(0, max).forEach(ii -> results.add(Constants.Nil));
    }

    public synchronized void add(final VncVal val) {
        results.addFirst(val == null ? Constants.Nil : val);
        results.removeLast();
    }

    public int max() {
        return max;
    }

    public synchronized void mergeToEnv(final Env env) {
        IntStream.rangeClosed(1, results.size())
                 .forEach(ii -> addToEnv(env, "*" + ii, results.get(ii-1)));

        addToEnv(env, "**", VncList.ofList(results));
    }

    public void addToEnv(final Env env, final String name, final VncVal val) {
        env.setGlobal(new Var(new VncSymbol(name), val, Var.Scope.Global));
    }

    public boolean isResultHistorySymbol(final String symbol) {
        final String l = symbol.trim();

        //  check **, *1, *2, *3, ...
        return Stream.concat(
                        Stream.of("**"),
                        IntStream.rangeClosed(1, max()).mapToObj(ii -> "*" + ii))
                     .anyMatch(s -> l.equals(s));
    }


    private final int max;
    private final LinkedList<VncVal> results = new LinkedList<>();
}
