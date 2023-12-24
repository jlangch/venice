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

import static com.github.jlangch.venice.impl.util.StringUtil.padRight;

import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Enhances the {@link Break} class with a debugger 'waitable' flag.
 */
public class WaitableBreak {

    public WaitableBreak(final Break br, final boolean waiting) {
        if (br == null) {
            throw new IllegalArgumentException("A break must not be null");
        }

        this.br = br;
        this.waiting.set(waiting);
    }


    public Break getBreak() {
        return br;
    }

    public boolean isWaitingOnBreak() {
        return waiting.get();
    }

    public void stopWaitingOnBreak() {
        waiting.set(false);
    }

    @Override
    public String toString() {
        return String.format(
                    "%s\n%s %b",
                    br.toString(),
                    padRight("Waiting:", Break.FORMAT_PAD_LEN),
                    waiting.get());
    }

    @Override
    public int hashCode() {
        return br.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof WaitableBreak && br.equals(((WaitableBreak)obj).br);
    }


    private final Break br;
    private final AtomicBoolean waiting = new AtomicBoolean(false);
}
