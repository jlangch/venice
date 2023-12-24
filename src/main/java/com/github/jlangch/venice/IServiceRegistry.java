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

import java.util.Map;


/**
 * A service registry for service discovery and registration.
 *
 * <p><b>Note:</b> Registrations can only be managed through {@link IServiceRegistry}.
 * Venice scripts have access to {@link IServiceRegistry#lookup(String)} only.
 *
 * <p>Example:
 *
 * <pre>
 * Venice venice = new Venice();
 *
 * IServiceRegistry registry = venice.getServiceRegistry();
 * registry.register("Calculator", new Calculator());
 *
 * long r = (Long)venice.eval("(service :Calculator :multiply 10 20)");
 * </pre>
 *
 * While `Calculator` is defined as:
 *
 * <pre>
 * public class Calculator {
 *   public long multiply(long v1, long v2) {
 *     return v1 * v2;
 *   }
 * }
 * </pre>
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
     * @return this registry to allow builder style configuration
     */
	IServiceRegistry register(String name, Object service);

    /**
     * Register named services.
     *
     * @param services A map of named services.
     *                 The <code>key</code> holds the service's name (a non blank string).
     *                 The <code>value</code> holds the service, any non <code>null</code> Java object
    * @return this registry to allow builder style configuration
     */
	IServiceRegistry registerAll(Map<String,Object> services);

    /**
     * Register a dynamic service lookup.
     *
     * <p>Note: Services registered through {@link #register(String,Object)} have
     * precedence over services available provided by the custom service discovery via
     * {@link IServiceDiscovery}
     *
     * @param serviceDiscovery The custom service discovery mechanism
    * @return this registry to allow builder style configuration
     */
	IServiceRegistry registerServiceDiscovery(IServiceDiscovery serviceDiscovery);

    /**
     * Unregister a service.
     *
     * <p>Unregistering an unknown service is silently skipped.
     *
     * @param name The service's name
     * @return this registry to allow builder style configuration
     */
	IServiceRegistry unregister(String name);

    /**
     * Unregister all services and the service discovery
     *
     * @return this registry to allow builder style configuration
     */
	IServiceRegistry unregisterAll();

    /**
     * Unregister the dynamic service lookup.
     *
     * @return this registry to allow builder style configuration
     */
	IServiceRegistry unregisterServiceDiscovery();

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
    boolean exists(String name);

}
