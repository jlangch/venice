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
package com.github.jlangch.venice.impl.util;

import com.github.jlangch.venice.impl.types.VncVal;


public class CallFrameFnData {

    public CallFrameFnData(final String fnName, final VncVal fnMeta) {
        this.fnName = fnName;
        this.fnMeta = fnMeta;
    }


    public String getFnName() {
        return fnName;
    }

    public VncVal getFnMeta() {
        return fnMeta;
    }

    public boolean matchesFnName(final String fnName) {
        return this.fnName.equals(fnName);
    }


    private final String fnName;
    private final VncVal fnMeta;
}
