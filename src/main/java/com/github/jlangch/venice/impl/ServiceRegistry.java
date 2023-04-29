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
package com.github.jlangch.venice.impl;

import com.github.jlangch.venice.AssertionException;
import com.github.jlangch.venice.IServiceRegistry;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncMutableMap;
import com.github.jlangch.venice.impl.util.StringUtil;


public class ServiceRegistry extends VncMutableMap implements IServiceRegistry {

	public ServiceRegistry() {

	}

	@Override
    public void register(final String name, final Object service) {
        if (StringUtil.isBlank(name)) {
            throw new AssertionException(
                    "A service name for the service registry must not be blank!");
        }
        if (service == null) {
            throw new AssertionException(
                    "A service for the service registry must not be null!");
        }

        assoc(new VncKeyword(name), new VncJavaObject(service));
    }

    @Override
    public void unregister(final String name) {
        if (name == null) {
            throw new AssertionException(
                    "A service name for unregistering a service in the service registry must not be null!");
        }

        dissoc(new VncKeyword(name));
    }

    @Override
    public Object lookup(final String name) {
        if (name == null) {
            throw new AssertionException(
                    "A service name for looking up a service in the service registry must not be null!");
        }

        final VncVal service = get(new VncKeyword(name));
        return service == Constants.Nil ? null : ((VncJavaObject)service).getDelegate();
    }

    @Override
    public boolean exists(final String name) {
        if (name == null) {
            throw new AssertionException(
                    "A service name for testing existence in the service registry must not be null!");
        }

        final VncVal service = get(new VncKeyword(name));
        return service != Constants.Nil && service instanceof VncJavaObject;
    }



    private static final long serialVersionUID = 1L;
}
