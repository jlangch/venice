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

import java.util.concurrent.ConcurrentHashMap;

import com.github.jlangch.venice.util.ipc.IpcException;
import com.github.jlangch.venice.util.password.PBKDF2PasswordEncoder;


public class Authenticator {

    public Authenticator(final boolean active) {
        this.active = active;
    }


    public boolean isActive() {
        return active;
    }

    public void addCredentials(
            final String userName,
            final String password
    ) {
        if (userName == null || password == null) {
            throw new IpcException("Invalid user authorization credentials!");
        }
        authorizations.put(userName, pwEncoder.encode(password));
    }

    public void removeCredentials(
            final String userName
    ) {
        authorizations.remove(userName);
    }

    public void clearCredentials(
            final String userName
    ) {
        authorizations.clear();
    }

    public boolean isAuthenticated(
            final String userName,
            final String password
    ) {
        if (userName == null || password == null) {
            return false;
        }

        final String pwHash = authorizations.get(userName);
        return pwHash != null && pwEncoder.verify(password, pwHash);
    }


    private final boolean active;
    private final PBKDF2PasswordEncoder pwEncoder = new PBKDF2PasswordEncoder();
    private final ConcurrentHashMap<String, String> authorizations = new ConcurrentHashMap<>();
}
