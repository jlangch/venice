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
package com.github.jlangch.venice.util.ipc.impl.wal;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.util.StringUtil;


public class WalQueueManager {

    public WalQueueManager(final File walDir) {
        this.walDir = walDir;
    }

    public List<File> listLogFiles() {
        return Arrays
                .stream(walDir.listFiles())
                .filter(f -> f.getName().endsWith(".wal"))
                .collect(Collectors.toList());
    }

    public List<String> listQueueNames() {
        return listLogFiles()
                .stream()
                .map(f -> toQueueName(f))
                .collect(Collectors.toList());
    }


    public static String toFileName(final String queueName) {
        return queueName.replace('/', '$') + ".wal";
    }

    public static String toQueueName(final File file) {
        return StringUtil.removeEnd(file.getName().replace('$', '/'), ".wal");
    }


    private final File walDir;
}
