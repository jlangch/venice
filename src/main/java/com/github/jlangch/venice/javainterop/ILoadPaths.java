/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2022 Venice
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
package com.github.jlangch.venice.javainterop;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;


/**
 * Defines load paths for Venice.
 *
 * <p>The Venice functions 'load-file' and 'load-resource' can be
 * bound to load files and resources from restricted paths only.
 * Load paths are part of Venice's {@code Sandbox} to control where
 * files can be loaded from.
 *
 * @see LoadPathsFactory
 * @author juerg
 */
public interface ILoadPaths {

    /**
     * Load a Venice script file from the load paths
     *
     * @param file A Venice script file to load. Adds a '.venice' file
     *         extension implicitly if missing.
     * @return The script or {@code null} if not found
     */
    String loadVeniceFile(File file);

    /**
     * Loads a binary resources file from the load paths
     *
     * @param file A file to load.
     * @return The binary or {@code null} if not found
     */
    ByteBuffer loadBinaryResource(File file);

    /**
     * Loads a text resources file from the load paths
     *
     * @param file A file to load.
     * @param charset an optional text encoding like 'UTF-8'.
     *                'UTF-8' on passing {@code null}
     * @return The text resource or {@code null} if not found
     */
    String loadTextResource(File file, Charset charset);

    /**
     * @return the file paths associated with this {@code ILoadPaths} object
     */
    List<File> getPaths();

    /**
     * Checks if the passed file is within the load paths
     *
     * @param file a file to check
     * @return true if the file is within the load paths
     */
    boolean isOnLoadPath(File file);

    /**
     * @return {@code true} if the access to files is unlimited or
     *         {@code false} if the access is limited to the load paths.
     */
    boolean isUnlimitedAccess();
}
