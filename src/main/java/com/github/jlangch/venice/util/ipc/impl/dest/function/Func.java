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
package com.github.jlangch.venice.util.ipc.impl.dest.function;

import java.util.Objects;
import java.util.function.Function;

import com.github.jlangch.venice.util.ipc.IMessage;
import com.github.jlangch.venice.util.ipc.impl.Destination;


public class Func extends Destination implements IpcFunction {

    public Func(final String name, Function<IMessage,IMessage> func) {
        super(name);

        Objects.requireNonNull(func);

        this.func = func;
    }

    @Override
	public IMessage apply(final IMessage m) {
        return func.apply(m);
    }

    private final Function<IMessage,IMessage> func;
}
