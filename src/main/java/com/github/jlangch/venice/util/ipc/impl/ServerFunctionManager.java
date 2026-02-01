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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.github.jlangch.venice.util.ipc.Authenticator;
import com.github.jlangch.venice.util.ipc.IMessage;
import com.github.jlangch.venice.util.ipc.IpcException;
import com.github.jlangch.venice.util.ipc.ServerConfig;
import com.github.jlangch.venice.util.ipc.impl.dest.function.Func;
import com.github.jlangch.venice.util.ipc.impl.dest.function.IpcFunction;
import com.github.jlangch.venice.util.ipc.impl.util.ServerLogger;


public class ServerFunctionManager {

    public ServerFunctionManager(
            final ServerConfig config,
            final ServerLogger logger
    ) {
        this.authenticator = config.getAuthenticator();
        this.logger = logger;
        this.maxFunctions = config.getMaxTopics();
    }


    public IpcFunction getFunction(final String functionName) {
        Objects.requireNonNull(functionName);

        return functions.get(functionName);
    }

    public void createFunction(
            final String functionName,
            final Function<IMessage,IMessage> func
    ) {
        Objects.requireNonNull(functionName);
        Objects.requireNonNull(func);

        FunctionValidator.validateFunctionName(functionName);

        if (functions.size() >= maxFunctions) {
            throw new IpcException(String.format(
                    "Cannot create function! Reached the limit of %d functions.",
                    maxFunctions));
        }

        functions.computeIfAbsent(
                functionName,
                n -> {
                    final IpcFunction t = new Func(functionName, func);

                    t.updateAcls(authenticator.getFunctionAclsMappedByPrincipal(functionName));

                    logger.info(
                        "server", "function",
                        String.format("Created function %s.",functionName));

                    return t;
                });
    }

    public void removeFunction(final String functionName) {
        Objects.requireNonNull(functionName);

        functions.remove(functionName);
    }

    public boolean existsFunction(final String functionName) {
        Objects.requireNonNull(functionName);

        return functions.containsKey(functionName);
    }


    private final Authenticator authenticator;
    private final ServerLogger logger;
    private final int maxFunctions;
    private final Map<String, IpcFunction> functions = new ConcurrentHashMap<>();
}
