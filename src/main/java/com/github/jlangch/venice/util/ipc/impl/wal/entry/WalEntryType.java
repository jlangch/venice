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
package com.github.jlangch.venice.util.ipc.impl.wal.entry;


public enum WalEntryType {

    ACK(0),

    CONFIG(1),

    DATA(2);


    public static WalEntryType fromCode(int code) {
        for (WalEntryType s : WalEntryType.values()) {
            if (s.value == code) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown WAL entry type code: " + code);
    }

    private final int value;

    private WalEntryType(final int val) {
        value = val;
    }


    public int getValue() { return value; }
}
