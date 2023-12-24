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
package com.github.jlangch.venice.impl.util.ssl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;


public class CustomHostnameVerifier implements HostnameVerifier {

    public CustomHostnameVerifier(final String hostname) {
    	if (hostname != null) {
    		hostnames.add(hostname);
    	}
    }

    public CustomHostnameVerifier(final List<String> hostnames) {
    	if (hostnames != null) {
    		hostnames.stream()
    				.filter(h -> h != null)
    				.filter(h -> !h.isEmpty())
    				.forEach(h -> hostnames.add(h));
    	}
    }


    @Override
    public boolean verify(
            final String hostname,
            final SSLSession sslSession
    ) {
        return hostnames.contains(hostname);
    }


    final Set<String> hostnames = new HashSet<>();
}
