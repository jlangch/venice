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

import com.github.jlangch.venice.util.ipc.MessageType;
import com.github.jlangch.venice.util.ipc.ResponseStatus;


public abstract class Messages {

    public static Message testMessage(final byte[] payload, final boolean oneway) {
        return new Message(
                null,
                MessageType.TEST,
                ResponseStatus.NULL,
                oneway,
                false,
                false,
                Messages.EXPIRES_NEVER,
                SUBJECT_TEST,
                "application/octet-stream",
                null,
                payload);
    }


    private static int KB = 1024;
    private static int MB = KB * KB;

    // Message limit
    public static final long MESSAGE_LIMIT_MIN     =   2 * KB;   //   2 KB
    public static final long MESSAGE_LIMIT_MAX     = 250 * MB;   // 250 MB
    public static final long MESSAGE_LIMIT_DEFAULT =  20 * MB;   //  20 MB

    public static final long SUBJECT_MAX_LEN = 100;

    // Message timeout
    public static final long EXPIRES_NEVER   = -1L;
    public static final long NO_TIMEOUT      = -1L;
    public static final long DEFAULT_TIMEOUT = 300L;  // 300ms
    public static final long ZERO_TIMEOUT    = 0L;    //   0ms

    // Queues
    public static final long QUEUENAME_MAX_LEN = 100;
    public static final long MIMETYPE_MAX_LEN = 100;
    public static final long CHARSET_MAX_LEN = 50;

    // Server requests
    public static final String SUBJECT_SERVER_PREFIX = "ipc-server/";
    public static final String SUBJECT_SERVER_STATUS = "ipc-server/status";
    public static final String SUBJECT_SERVER_THREAD_POOL_STATS = "ipc-server/thread-pool-statistics";
    public static final String SUBJECT_SERVER_ERROR = "ipc-server/error";
    public static final String SUBJECT_SERVER_CLIENT_CONFIG = "ipc-server/client-config";

    public static final String SUBJECT_DIFFIE_HELLMANN = "dh";
    public static final String SUBJECT_AUTHENTICATION  = "authentication";
    public static final String SUBJECT_HEARTBEAT       = "heartbeat";
    public static final String SUBJECT_TEST            = "T";

    // Client requests
    public static final String SUBJECT_CLIENT_THREAD_POOL_STATS = "ipc-client/thread-pool-statistics";

}
