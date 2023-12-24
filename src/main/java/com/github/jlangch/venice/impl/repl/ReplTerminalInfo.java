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
package com.github.jlangch.venice.impl.repl;

import org.jline.terminal.Terminal;

import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncOrderedMap;


public class ReplTerminalInfo {

    public ReplTerminalInfo(final Terminal terminal) {
        this.terminal = terminal;
    }

    public VncMap info() {
        return VncOrderedMap.of(
                new VncKeyword("term-name"),  new VncString(terminal.getName()),
                new VncKeyword("term-type"),  new VncString(terminal.getType()),
                new VncKeyword("term-cols"),  new VncLong(terminal.getSize().getColumns()),
                new VncKeyword("term-rows"),  new VncLong(terminal.getSize().getRows()),
                new VncKeyword("term-class"), new VncKeyword(terminal.getClass().getSimpleName()));
    }

    private final Terminal terminal;
}
