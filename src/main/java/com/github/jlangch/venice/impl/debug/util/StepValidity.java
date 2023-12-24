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
package com.github.jlangch.venice.impl.debug.util;


public class StepValidity {

    private StepValidity(
            final boolean valid,
            final String errMsg
    ) {
        this.valid = valid;
        this.errMsg = errMsg;
    }

    public static StepValidity valid() {
        return new StepValidity(true, null);
    }

    public static StepValidity invalid(final String errMsg) {
        return new StepValidity(false, errMsg);
    }

    public boolean isValid() {
        return valid;
    }

    public String getErrMsg() {
        return errMsg;
    }


    private final boolean valid;
    private final String errMsg;
}
