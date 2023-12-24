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
package com.github.jlangch.venice;


/**
 * Defines a custom dynamic service lookup mechanism.
 *
 * @author juerg
 */
public interface IServiceDiscovery {

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
     * @return <code>true</code> if the service exists otherwise <code>false</code>
     */
    default boolean exists(String name) {
    	return lookup(name) != null;
    }

}
