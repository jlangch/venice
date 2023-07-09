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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import com.github.jlangch.venice.AssertionException;
import com.github.jlangch.venice.IServiceDiscovery;
import com.github.jlangch.venice.IServiceRegistry;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.util.StringUtil;


public class ServiceRegistry implements IServiceRegistry {

	public ServiceRegistry() {
		clear();
	}

	@Override
    public ServiceRegistry register(final String name, final Object service) {
        if (StringUtil.isBlank(name)) {
            throw new AssertionException(
                    "A service name for the service registry must not be blank!");
        }
        if (service == null) {
            throw new AssertionException(
                    "A service for the service registry must not be null!");
        }

        staticRegistry.put(name, service);

        return this;
    }

	@Override
    public ServiceRegistry registerAll(final Map<String,Object> services) {
		if (services != null) {
			services.forEach((k,v) -> register(k,v));
		}

        return this;
	}

	@Override
    public ServiceRegistry registerServiceDiscovery(final IServiceDiscovery serviceDiscovery) {
		if (serviceDiscovery == null) {
            throw new AssertionException(
                    "A service discovery for the service registry must not be null!");
		}

 		replaceDynamicRegistry(serviceDiscovery);

        return this;
	}

    @Override
    public ServiceRegistry unregister(final String name) {
        if (name == null) {
            throw new AssertionException(
                    "A service name for unregistering a service in the service registry must not be null!");
        }

        staticRegistry.remove(name);

        return this;
    }

    @Override
    public ServiceRegistry unregisterAll() {
    	clear();

        return this;
   }

    @Override
    public ServiceRegistry unregisterServiceDiscovery() {
    	replaceDynamicRegistry(null);

        return this;
    }

    @Override
    public Object lookup(final String name) {
        if (name == null) {
            throw new AssertionException(
                    "A service name for looking up a service in the service registry must not be null!");
        }

        // primary lookup on static registry
        final Object service = staticRegistry.get(name);
        if (service != null) {
             return service;
        }

        // secondary lookup on dynamic registry
        final IServiceDiscovery sd = getDynamicRegistry();
        if (sd != null) {
            return sd.lookup(name);
        }

        throw new VncException(
                "No registered service available under the name '" + name + "'!");
    }

    @Override
    public boolean exists(final String name) {
        if (name == null) {
            throw new AssertionException(
                    "A service name for testing existence in the service registry must not be null!");
        }

        if (staticRegistry.containsKey(name)) {
        	return true;
        }
        else {
            final IServiceDiscovery sd = getDynamicRegistry();
            return sd != null && sd.lookup(name) != null;
        }
     }


    private void clear() {
    	staticRegistry.clear();
    	replaceDynamicRegistry(null);
    }

    private IServiceDiscovery getDynamicRegistry() {
     	return serviceDiscovery.get();
    }

    private void replaceDynamicRegistry(final IServiceDiscovery sd) {
    	serviceDiscovery.set(sd);
    }


    private Map<String,Object> staticRegistry = new ConcurrentHashMap<>();
    private AtomicReference<IServiceDiscovery> serviceDiscovery = new AtomicReference<>();
 }
