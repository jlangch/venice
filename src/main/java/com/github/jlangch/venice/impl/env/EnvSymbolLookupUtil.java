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
package com.github.jlangch.venice.impl.env;

import java.util.List;
import java.util.stream.Collectors;

import org.jline.utils.Levenshtein;

import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.util.Tuple2;


public class EnvSymbolLookupUtil {

    public static List<VncSymbol> getGlobalSymbolCandidates(
            final String name,
            final Env env,
            final int limit
    ) {
        final List<VncSymbol> globalSymbols = env.getAllGlobalFunctionSymbols();

        // exact match on simple name
        List<VncSymbol> candidates = EnvSymbolLookupUtil.getGlobalSymbolCandidates(
                                            name,
                                            globalSymbols,
                                            5,
                                            0);

        if (candidates.isEmpty()) {
            // levenshtein match on simple name with distance 1
            candidates = EnvSymbolLookupUtil.getGlobalSymbolCandidates(
                            name,
                            globalSymbols,
                            5,
                            1);
        }

        return candidates;
    }

    public static List<VncSymbol> getGlobalSymbolCandidates(
            final String name,
            final List<VncSymbol> globalFunctionSymbols,
            final int limit,
            final int levenshteinDistance
    ) {
        if (levenshteinDistance == 0) {
            return globalFunctionSymbols
                       .stream()
                       .filter(s -> s.getSimpleName().equals(name))
                       .sorted()
                       .limit(limit)
                       .collect(Collectors.toList());
        }
        else {
            return globalFunctionSymbols
                       .stream()
                       .map(s -> new Tuple2<Integer,VncSymbol>(
                                       Levenshtein.distance(name, s.getSimpleName()),
                                       s))
                       .filter(t -> t.getFirst() <= levenshteinDistance)
                       .sorted()
                       .map(t -> t.getSecond())
                       .limit(limit)
                       .collect(Collectors.toList());
        }
    }

    public static String getSymbolNotFoundMsg(
            final VncSymbol sym,
            final List<VncSymbol> candidates
    ) {
        final List<String> indented = candidates
                                       .stream()
                                       .map(s -> "   " + s.getQualifiedName())
                                       .collect(Collectors.toList());

        return String.format(
                "Symbol '%s' not found!\n\nDid you mean?\n%s\n",
                sym.getQualifiedName(),
                String.join("\n", indented));
    }

}
