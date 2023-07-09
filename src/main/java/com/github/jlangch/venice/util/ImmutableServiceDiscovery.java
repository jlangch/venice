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
package com.github.jlangch.venice.util;

import com.github.jlangch.venice.AssertionException;
import com.github.jlangch.venice.IServiceDiscovery;
import com.github.jlangch.venice.IServiceRegistry;


public class ImmutableServiceDiscovery implements IServiceDiscovery {

    public ImmutableServiceDiscovery(final IServiceRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Object lookup(final String name) {
        if (name == null) {
            throw new AssertionException(
                    "A service name for looking up a service in the service registry must not be null!");
        }

        return registry.lookup(name);
    }

    @Override
    public boolean exists(final String name) {
        if (name == null) {
            throw new AssertionException(
                    "A service name for checking service availability in the service registry must not be null!");
        }

        return registry.exists(name);
    }


    private final IServiceRegistry registry;
 }
