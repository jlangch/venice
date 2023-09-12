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

import java.util.List;

import com.github.jlangch.venice.impl.debug.agent.DebugAgent;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Types;


public class RecursionPoint {

    public RecursionPoint(
            final List<VncVal> loopBindingNames,
            final VncList loopExpressions,
            final Env loopEnv,
            final VncVal meta,
            final DebugAgent debugAgent
    ) {
        this.loopBindingNames = loopBindingNames;
        this.loopBindingNamesCount = loopBindingNames.size();
        this.loopExpressions = loopExpressions;
        this.loopEnv = loopEnv;
        this.meta = meta;
        this.debugAgent = debugAgent;
    }


    public int getLoopBindingNamesCount() {
        return loopBindingNamesCount;
    }

    public VncVal getLoopBindingName(final int idx) {
        return loopBindingNames.get(idx);
    }

    public List<VncVal> getLoopBindingNames() {
        return loopBindingNames;
    }

    public VncList getLoopExpressions() {
        return loopExpressions;
    }

    public Env getLoopEnv() {
        return loopEnv;
    }

    public VncVal getMeta() {
        return meta;
    }

    public DebugAgent getDebugAgent() {
        return debugAgent;
    }

    public boolean isDebuggingActive() {
        return debugAgent != null;
    }

    public static void addToLocalEnv(final VncVal symVal, final VncVal bindVal, final Env env) {
        if (Types.isVncSymbol(symVal)) {
            env.setLocal(new Var((VncSymbol)symVal, bindVal, Var.Scope.Local));
        }
        else {
            // destructuring
            env.addLocalVars(Destructuring.destructure(symVal, bindVal));
        }
    }


    private final List<VncVal> loopBindingNames;
    private final int loopBindingNamesCount;
    private final VncList loopExpressions;
    private final Env loopEnv;
    private final VncVal meta;
    private final DebugAgent debugAgent;
}
