/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2026 Venice
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
package com.github.jlangch.venice.util.ipc.impl;

import com.github.jlangch.venice.impl.util.StringUtil;


public class FunctionValidator {

    public static void validateFunctionName(final String functionName) {
        if (StringUtil.isBlank(functionName)) {
            throw new IllegalArgumentException("A function name must not be empty or blank!");
        }

        if (functionName.length() > FUNCTION_MAX_LEN) {
            throw new IllegalArgumentException(
                    "A function name is limited to " + FUNCTION_MAX_LEN + " characters!");
        }

        if (functionName.matches("wal")) {
            throw new IllegalArgumentException(
                    "The function name 'wal' is a preserved name!");
        }

        if (!functionName.matches("[a-zA-Z0-9_\\-/]+")) {
            throw new IllegalArgumentException(
                    "The function name \"" + functionName + "\" must only contain the characters: "
                    + "'a-z', 'A-Z', '0-9', '_', '-', or '/'!");
        }
    }


    public static final long FUNCTION_MAX_LEN = 100;
}
