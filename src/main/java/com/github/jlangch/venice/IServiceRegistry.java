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
     * Register a service
     *
     * @param name The service's name
     * @param service The service
     */
    void register(String name, Object service);

    /**
     * Unregister a service
     *
     * @param name The service's name
     */
    void unregister(String name);

    /**
     * Looking up a service
     *
     * @param name
     * @return the service or <code>null</code> if not found
     */
    Object lookup(String name);

    /**
     * Returns true if the service with the name exists otherwise false
     *
     * @param name
     * @return <code>null</code> if the service exists otherwise <code>false</code>
     */
    boolean exists(String name);

}
