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

import static com.github.jlangch.venice.util.ipc.AccessMode.PRINCIPAL_READ;
import static com.github.jlangch.venice.util.ipc.AccessMode.PRINCIPAL_READ_WRITE;
import static com.github.jlangch.venice.util.ipc.AccessMode.PRINCIPAL_WRITE;
import static com.github.jlangch.venice.util.ipc.AccessMode.UNRESTRICTED_READ;
import static com.github.jlangch.venice.util.ipc.AccessMode.UNRESTRICTED_READ_WRITE;
import static com.github.jlangch.venice.util.ipc.AccessMode.UNRESTRICTED_WRITE;

import java.util.Objects;

import com.github.jlangch.venice.util.ipc.AccessMode;


public class Acl {

    public Acl(
            final String subject,
            final String principal,
            final AccessMode mode
    ) {
        Objects.requireNonNull(subject);
        Objects.requireNonNull(mode);

        this.subject = subject;
        this.principal = principal == null ? "*" : principal;
        this.mode = mode;
    }


    public String getSubject() {
        return subject;
    }

    public String getPrincipal() {
        return principal;
    }

    public AccessMode getMode() {
        return mode;
    }


    public boolean canRead(final String principal) {
        if (mode == UNRESTRICTED_READ || mode == UNRESTRICTED_READ_WRITE) {
            return true;
        }
        else if (principal != null && principal.equals(this.principal)){
            return mode == PRINCIPAL_READ || mode == PRINCIPAL_READ_WRITE;
        }
        else {
            return false;
        }
    }

    public boolean canWrite(final String principal) {
        if (mode == UNRESTRICTED_WRITE || mode == UNRESTRICTED_READ_WRITE) {
            return true;
        }
        else if (principal != null && principal.equals(this.principal)){
            return mode == PRINCIPAL_WRITE || mode == PRINCIPAL_READ_WRITE;
        }
        else {
            return false;
        }
    }


    private final String subject;
    private final String principal;
    private final AccessMode mode;
}
