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
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.Tuple2;


public class EnvUtils {

    public static String envGlobalsToString(final Env env, final String regexFilter) {
        return new StringBuilder()
                    .append(String.join("\n", globalVarsFormatted(env, regexFilter)))
                    .toString();
    }

    public static List<String> globalVarsFormatted(final Env env, final String regexFilter) {
        return globalVarsFormatted(
                env.getAllGlobalSymbols(),
                regexFilter == null ? null : Pattern.compile(regexFilter));
    }

    public static List<Tuple2<VncSymbol,VncKeyword>> globalVars(final Env env, final String regexFilter) {
        return globalVars(
                env.getAllGlobalSymbols(),
                regexFilter == null ? null : Pattern.compile(regexFilter));
    }

    public static List<Tuple2<VncSymbol,VncKeyword>> globalVars(final Env env, final Pattern regexPattern) {
        return globalVars(
                env.getAllGlobalSymbols(),
                regexPattern);
    }


    private static List<Tuple2<VncSymbol,VncKeyword>> globalVars(
            final Map<VncSymbol,Var> vars,
            final Pattern regexPattern
    ) {
        final Predicate<String> p = regexPattern == null ? null : regexPattern.asPredicate();

        return vars.values()
                   .stream()
                   .sorted((a,b) -> a.getName().getName().compareTo(b.getName().getName()))
                   .filter(v -> regexPattern == null
                                       ? true
                                       : p.test(v.getName().getName()))
                   .map(v -> new Tuple2<VncSymbol,VncKeyword>(
                                    v.getName(),
                                    formatGlobalVarType(v)))
                   .collect(Collectors.toList());
    }

    private static List<String> globalVarsFormatted(
            final Map<VncSymbol,Var> vars,
            final Pattern regexPattern
    ) {
        return globalVars(vars, regexPattern)
                   .stream()
                   .map(v -> String.format("%s (%s)", v._1, v._2))
                   .collect(Collectors.toList());
    }


    private static VncKeyword formatGlobalVarType(final Var var_) {
        final VncVal val = var_.getVal();
        if (Types.isVncJavaObject(val)) {
            final VncJavaObject vJava = (VncJavaObject)val;
            if (vJava.getDelegateFormalType() != null) {
                return new VncKeyword(vJava.getDelegateFormalType().getName());
            }
            else {
                return new VncKeyword(vJava.getDelegate().getClass().getName());
            }
        }
        else {
            return Types.getType(val);
        }
    }

}
