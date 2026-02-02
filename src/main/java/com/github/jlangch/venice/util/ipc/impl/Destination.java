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
import java.util.concurrent.ConcurrentHashMap;

import com.github.jlangch.venice.util.ipc.AccessMode;


public abstract class Destination implements IDestination {

    public Destination(final String name) {
        this.name = name;
    }


    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean canRead(final String principal) {
        return principal == null
                ? defaultAcl.canRead()
                : acls.getOrDefault(principal, defaultAcl).canRead();
    }

    @Override
    public boolean canWrite(final String principal) {
        return principal == null
                ? defaultAcl.canWrite()
                : acls.getOrDefault(principal, defaultAcl).canWrite();
    }

    @Override
    public boolean canExecute(final String principal) {
        return principal == null
                ? defaultAcl.canExecute()
                : acls.getOrDefault(principal, defaultAcl).canExecute();
    }

    @Override
    public void updateAcls(final Map<String,Acl>  acls, final Acl defaultAcl) {
        this.acls.clear();
        if (acls != null) {
            this.acls.putAll(acls);
        }
        this.defaultAcl = defaultAcl;
    }

    @Override
    public void clearAcls() {
        acls.clear();
    }


    private final String name;

    // ACL mapped by principal -> ACL
    private volatile Acl defaultAcl = new Acl("*", "*", AccessMode.DENY);
    private final ConcurrentHashMap<String, Acl> acls = new ConcurrentHashMap<>();
}
