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

import static com.github.jlangch.venice.util.ipc.AccessMode.EXECUTE;
import static com.github.jlangch.venice.util.ipc.AccessMode.READ;
import static com.github.jlangch.venice.util.ipc.AccessMode.READ_WRITE;
import static com.github.jlangch.venice.util.ipc.AccessMode.WRITE;

import java.util.Objects;

import com.github.jlangch.venice.util.ipc.AccessMode;


public class Acl {

    public Acl(
            final String subject,   // queue/topic name
            final String principal,
            final AccessMode mode
    ) {
        Objects.requireNonNull(subject);
        Objects.requireNonNull(mode);

        this.subject = subject;
        this.principal = principal == null ? "*" : principal;
        this.mode = mode;
        this.read = mode == READ || mode == READ_WRITE;
        this.write = mode == WRITE || mode == READ_WRITE;
        this.exec = mode == EXECUTE;
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


    public boolean canRead() {
        return read;
    }

    public boolean canWrite() {
        return write;
    }

    public boolean canExecute() {
        return exec;
    }


    @Override
    public String toString() {
        return String.format("principal=%s, subject=%s, mode=%s", principal,subject,  mode.name());
    }



    private final String subject;
    private final String principal;
    private final AccessMode mode;
    private final boolean read;
    private final boolean write;
    private final boolean exec;
}
