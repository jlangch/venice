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
package com.github.jlangch.venice.util.openai;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;


public class RegistryFunctionDispatcher implements IFunctionDispatcher{

    public RegistryFunctionDispatcher() {
    }

    public void register(final String name, final Function<String,String> fn) {
        registry.put(name, fn);
    }

    public void unregister(final String name) {
        registry.remove(name);
    }

    @Override
    public String call(String fnName, String fnArgsJson) {
        final Function<String,String> fn = registry.get(fnName);
        if (fn == null) {
            throw new RuntimeException("No OpenAI function available for name '" + fnName + "'");
        }

        return fn.apply(fnArgsJson);
    }


    private final Map<String, Function<String,String>> registry = new ConcurrentHashMap<>();
}
