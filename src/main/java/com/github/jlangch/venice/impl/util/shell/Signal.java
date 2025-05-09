/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2025 Venice
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
package com.github.jlangch.venice.impl.util.shell;


public enum Signal {

    SIGHUP(1),    // Terminal line hangup

    SIGINT(2),    // Interrupt program

    SIGQUIT(3),   // Quit program

    SIGKILL(9),   // Kill program

    SIGTERM(15);  // Software termination signal


    private Signal(final int signal) {
        this.signal = signal;
    }

    public int signal() {
        return signal;
    }

    private final int signal;
}
