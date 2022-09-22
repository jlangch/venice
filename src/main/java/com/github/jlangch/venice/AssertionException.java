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
package com.github.jlangch.venice;

import com.github.jlangch.venice.util.StackFrame;

/**
 * Thrown if an assertion validation fails.
 *
 * <p>It's raised in two cases:
 * <ul>
 *   <li> if the assert macro expression validation fails</li>
 *   <li> if the pre-conditions validation within a function fails</li>
 * </ul>
 */
public class AssertionException extends VncException {

    public AssertionException(final String message) {
        super(message);
    }

    public AssertionException(
            final String message,
            final String fnName,
            final String file,
            final int lineNr,
            final int colNr
    ) {
        super(message, new StackFrame(fnName, file, lineNr, colNr));
    }

    public AssertionException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public AssertionException(final Throwable cause) {
        super(cause);
    }


    private static final long serialVersionUID = 1349237272157335345L;
}
