/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2025 Venice
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
package com.github.jlangch.venice.util.ipc;


public enum ResponseStatus {

    OK(0),

    SERVER_ERROR(1),
    HANDLER_ERROR(2),
    BAD_REQUEST(3),

    QUEUE_NOT_FOUND(10),
    QUEUE_EMPTY(11),
    QUEUE_FULL(12),

    DIFFIE_HELLMAN_KEY(20),
    DIFFIE_HELLMAN_ERROR(21),

    NULL(99);



    public static ResponseStatus fromCode(int code) {
        for (ResponseStatus s : ResponseStatus.values()) {
            if (s.value == code) {
                return s;
            }
        }
        return null;
    }

    private final int value;

    private ResponseStatus(final int val) {
        value = val;
    }


    public int getValue() { return value; }
}
