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
package com.github.jlangch.venice.impl.util.loadpath;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.util.CollectionUtil;
import com.github.jlangch.venice.javainterop.ILoadPaths;


public class LoadPaths implements ILoadPaths {

    private LoadPaths(
            final List<LoadPath> paths,
            final boolean unlimitedAccess
    ) {
        this.paths.addAll(paths);
        this.unlimitedAccess = unlimitedAccess;
    }

    public static LoadPaths of(
            final List<File> paths,
            final boolean unlimitedAccess
    ) {
        return new LoadPaths(
                        CollectionUtil
                            .toEmpty(paths)
                            .stream()
                            .filter(p -> p != null)
                            .map(p -> LoadPath.of(p))
                            .collect(Collectors.toList()),
                        unlimitedAccess);
    }

    @Override
    public String loadVeniceFile(final File file) {
        if (file == null) {
            return null;
        }
        else {
            final String path = file.getPath();
            final File veniceFile = path.endsWith(".venice") ? file : new File(path + ".venice");
            return toString(load(veniceFile), "UTF-8");
        }
    }

    @Override
    public ByteBuffer loadBinaryResource(final File file) {
        return file == null ? null : load(file);
    }

    @Override
    public String loadTextResource(final File file, final String encoding) {
        return file == null ? null : toString(load(file), encoding);
    }

    @Override
    public List<File> getPaths() {
        return paths.stream()
                    .map(p -> p.path())
                    .collect(Collectors.toList());
    }

    @Override
    public boolean isUnlimitedAccess() {
        return unlimitedAccess;
    }

    @Override
    public boolean isOnLoadPath(final File file) {
        if (file == null) {
            throw new IllegalArgumentException("A file must not be null");
        }
        else if (unlimitedAccess) {
            return true;
        }
        else {
            try {
                // Check load paths. We do not care if the file exists its just
                // important to be within one of the load paths
                for(LoadPath p : paths) {
                    if (p.isOnPath(file)) {
                       return true;
                    }
                }
            }
            catch (VncException ex) {
                throw ex;
            }
            catch (Exception ex) {
                throw new VncException(
                            String.format(
                                    "Failed to check if the file '%s' is on the load path",
                                    file.getPath()),
                            ex);
            }

            return false;
        }
    }

    private ByteBuffer load(final File file) {
        // try to load the file from one of the load paths
        final ByteBuffer dataFromLoadPath = paths.stream()
                                                 .map(p -> p.load(file))
                                                 .filter(d -> d != null)
                                                 .findFirst()
                                                 .orElse(null);

        if (dataFromLoadPath != null) {
            // prefer loading the file from the load paths
            return dataFromLoadPath;
        }
        else if (unlimitedAccess && file.isFile()) {
            // if the file has not been found on the load paths and 'unlimited'
            // file access is enabled load the file
            try {
                return ByteBuffer.wrap(Files.readAllBytes(file.toPath()));
            }
            catch(IOException ex) {
                return null;
            }
        }
        else {
            // file is not available
            return null;
        }
    }

    private String toString(final ByteBuffer data, final String encoding) {
        return data == null
        		? null
        		: new String(data.array(), charset(encoding));
    }

    private Charset charset(final String encoding) {
    	return encoding == null || encoding.isEmpty()
                ? Charset.defaultCharset()
                : Charset.forName(encoding);
    }


    private final List<LoadPath> paths = new ArrayList<>();
    private final boolean unlimitedAccess;
}
