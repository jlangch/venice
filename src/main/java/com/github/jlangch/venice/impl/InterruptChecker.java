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
package com.github.jlangch.venice.impl;

import com.github.jlangch.venice.impl.types.VncFunction;


public class InterruptChecker {

    public static void checkInterrupted(final Thread thread, final VncFunction fn) {
        if (thread.isInterrupted()) {
            throw new com.github.jlangch.venice.InterruptedException(
                        "Interrupted while processing function " + fn.getQualifiedName());
        }
    }

    public static void checkInterrupted(final Thread thread, final String fnName) {
        if (thread.isInterrupted()) {
            throw new com.github.jlangch.venice.InterruptedException(
                        "Interrupted while processing function " + fnName);
        }
    }

}
