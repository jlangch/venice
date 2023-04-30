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
package com.github.jlangch.venice;


/**
 * A service registry for service discovery and registration
 *
 * @author juerg
 */
public interface IServiceRegistry {

    /**
     * Register a named service.
     *
     * <p>Services can be replaced by registering them anew with another service object.
     *
     * @param name The service's name. A service name must be a non blank string.
     * @param service The service, any non <code>null</code> Java object
     */
    void register(String name, Object service);

    /**
     * Unregister a service.
     *
     * <p>Unregistering an unknown service is silently skipped.
     *
     * @param name The service's name
     */
    void unregister(String name);

    /**
     * Unregister all services.
     */
    void unregisterAll();

    /**
     * Looking up a service
     *
     * @param name The service's name
     * @return the service or <code>null</code> if not registered
     */
    Object lookup(String name);

    /**
     * Returns true if the service exists otherwise false
     *
     * @param name The service's name
     * @return <code>null</code> if the service exists otherwise <code>false</code>
     */
    boolean exists(String name);

}
