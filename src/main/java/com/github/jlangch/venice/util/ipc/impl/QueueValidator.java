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
package com.github.jlangch.venice.util.ipc.impl;

import com.github.jlangch.venice.impl.util.StringUtil;


public class QueueValidator {

    public static void validate(final String queueName) {
        if (StringUtil.isBlank(queueName)) {
            throw new IllegalArgumentException("A queue name must not be empty or blank!");
        }

        if (queueName.length() > QUEUE_NAME_MAX_LEN) {
            throw new IllegalArgumentException(
                    "A queue name is limited to " + QUEUE_NAME_MAX_LEN + " characters!");
        }

        if (queueName.matches("wal")) {
            throw new IllegalArgumentException(
                    "The queue name 'wal' is a preserved name!");
        }

        if (!queueName.matches("[a-zA-Z0-9_\\-/]+")) {
            throw new IllegalArgumentException(
                    "The queue name \"" + queueName + "\" must only contain the characters: "
                    + "'a-z', 'A-Z', '0-9', '_', '-', or '/'!");
        }
    }


    public static final long QUEUE_NAME_MAX_LEN = 80;
}
