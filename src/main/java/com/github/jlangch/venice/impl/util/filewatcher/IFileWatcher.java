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
package com.github.jlangch.venice.impl.util.filewatcher;

import java.io.Closeable;
import java.nio.file.Path;
import java.util.List;

import com.github.jlangch.venice.impl.util.callstack.CallFrame;


public interface IFileWatcher extends Closeable {

    public void start(final CallFrame[] callFrame) ;

    public Path getMainDir();

    public void register(final Path dir);

    public List<Path> getRegisteredPaths();

    public boolean isRunning() ;

    @Override
    public void close();

}
