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
package com.github.jlangch.venice.impl.debug.breakpoint;

import java.util.Arrays;
import java.util.List;


/**
 * Defines the breakpoint function scopes.
 *
 * <p>Breakpoint function scopes are related to function execution phases and
 * define at which execution phase a break point may be set.
 *
 * <p><b>Defined scopes:</b>
 * <ol>
 * <li> CALL: inspection of unevaluated function args
 * <li> ENTRY: inspection of evaluated function args
 * <li> EXCEPTION: inspection of exception caught in the function's body
 * and evaluated function args
 * <li> EXIT: inspection of evaluated function args and return value
 * </ol>
 *
 * <p><b>Function processing model:</b>
 *
 * <pre>
 *                                                  Function Scope
 *                                                        |
 *                                                        v
 * +---------------------------------------------------------------+
 * |Function                                                       |
 * +-------------------------------------------------- Call Level -+
 * |                                                               |
 * | 1) Evaluating and destructuring args                          |
 * |                                                               |
 * +------------------------------------------------- Entry Level -+
 * |                                                               |
 * | 2) Evaluating body expressions               Exception Level -|
 * |                                                               |
 * +-------------------------------------------------- Exit Level -+
 * |                                                               |
 * | 3) Return the value of the last expression                    |
 * |                                                               |
 * +---------------------------------------------------------------+
 * </pre>
 */
public enum FunctionScope {

    FunctionCall(">", "call"),              // Stop at fn call

    FunctionEntry("(", "entry"),            // Stop at fn entry

    FunctionExit(")", "exit"),              // Stop at fn exit

    FunctionException("!", "exception");    // Stop on exception in fn body


    private FunctionScope(String symbol, String description) {
        this.symbol = symbol;
        this.description = description;
    }

    public String symbol() {
        return symbol;
    }

    public String description() {
        return description;
    }

    public static List<FunctionScope> all() {
        return Arrays.asList(values());
    }


    private final String symbol;
    private final String description;
}
