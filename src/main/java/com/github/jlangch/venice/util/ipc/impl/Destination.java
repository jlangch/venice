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
        if (principal == null) {
            return true;
        }
        else {
            final Acl acl = acls.get(principal);
            return acl != null ? acl.canRead() : noAcls;
        }
    }

    @Override
    public boolean canWrite(final String principal) {
        if (principal == null) {
            return true;
        }
        else {
            final Acl acl = acls.get(principal);
            return acl != null ? acl.canWrite() : noAcls;
        }
    }

    @Override
    public boolean canExecute(final String principal) {
        if (principal == null) {
            return true;
        }
        else {
            final Acl acl = acls.get(principal);
            return acl != null ? acl.canExecute() : noAcls;
        }
    }

    @Override
    public void updateAcls(final Map<String,Acl>  acls) {
        this.acls.clear();
        if (acls != null) {
            this.acls.putAll(acls);
        }
        noAcls = this.acls.isEmpty();
    }

    @Override
    public void clearAcls() {
        acls.clear();
    }


    private final String name;

    // ACL mapped by principal -> ACL
    private volatile boolean noAcls = true;
    private final ConcurrentHashMap<String, Acl> acls = new ConcurrentHashMap<>();
}
