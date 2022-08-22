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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
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
     * Returns an {@link InputStream} for a file from the load paths
     *
     * @param file A file to return an {@code InputStream}.

     * @return The {@link InputStream} or {@code null} if not found
     */
    InputStream getInputStream(File file);

    /**
     * Returns a {@link BufferedReader} for a file from the load paths
     *
     * @param file A file to return an {@code BufferedReader}.
     * @param charset an optional text encoding like 'UTF-8'.
     *                'UTF-8' on passing {@code null}
     * @return The {@link BufferedReader} or {@code null} if not found
     */
    BufferedReader getBufferedReader(File file, Charset charset);

    /**
     * Returns an {@link OutputStream} for a file from the load paths
     *
     * <p>If no options are
     * present then this method works as if the {@link StandardOpenOption#CREATE
     * CREATE}, {@link StandardOpenOption#TRUNCATE_EXISTING TRUNCATE_EXISTING},
     * and {@link StandardOpenOption#WRITE WRITE} options are present. In other
     * words, it opens the file for writing, creating the file if it doesn't
     * exist, or initially truncating an existing regular-file to a size of
     * {@code 0} if it exists.
     *
     *
     * @param file A file to return an {@link BufferedReader}.
     * @param options options specifying how the file is opened
     * @return The {@link OutputStream} or {@code null} if not found
     */
    OutputStream getOutputStream(File file, OpenOption... options);

    /**
     * Returns {@code true} if the regular file exists on the load path
     *
     * @param file a file
     * @return {@code true} if the file exists on the load path
     */
    boolean isRegularFileOnLoadPath(File file);

    /**
     * Returns {@code true} if the directory exists on the load path
     *
     * @param file a file
     * @return {@code true} if the file exists on the load path
     */
    boolean isDirectoryOnLoadPath(File file);

    /**
     * @return the file paths associated with this {@code ILoadPaths} object
     */
    List<File> getPaths();

    /**
     * Checks if the passed file is within the load paths. The file must not
     * exist though.
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
