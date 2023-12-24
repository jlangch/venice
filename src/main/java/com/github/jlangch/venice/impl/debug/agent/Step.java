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
package com.github.jlangch.venice.impl.debug.agent;

import static com.github.jlangch.venice.impl.debug.agent.StepMode.SteppingDisabled;

import com.github.jlangch.venice.impl.util.CollectionUtil;


/**
 * Defines the step context for the debugger
 */
public class Step {

    public Step() {
        this(SteppingDisabled, null);
    }

    public Step(final StepMode mode) {
        this(mode, null);
    }

    public Step(
            final StepMode mode,
            final String boundToFnName
    ) {
        this.mode = mode;
        this.boundToFnName = boundToFnName;
    }


    public Step clear() {
        return new Step();
    }

    public StepMode mode() {
        return mode;
    }

    public boolean isInMode(final StepMode... modes) {
        return CollectionUtil.toList(modes).contains(mode);
    }

    public String boundToFnName() {
        return boundToFnName;
    }

    public boolean isBoundToFnName(final String fnName) {
        return boundToFnName != null && boundToFnName.equals(fnName);
    }

    public boolean isBoundToFnNameOrNull(final String fnName) {
        return boundToFnName == null || boundToFnName.equals(fnName);
    }


    private final StepMode mode;
    private final String boundToFnName;
}
