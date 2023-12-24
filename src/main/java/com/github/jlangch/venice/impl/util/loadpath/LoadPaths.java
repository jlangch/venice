/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.util.CollectionUtil;
import com.github.jlangch.venice.impl.util.io.CharsetUtil;
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
            return toString(load(veniceFile), CharsetUtil.DEFAULT_CHARSET);
        }
    }

    @Override
    public ByteBuffer loadBinaryResource(final File file) {
        return file == null ? null : load(file);
    }

    @Override
    public String loadTextResource(final File file, final Charset charset) {
        return file == null ? null : toString(load(file), CharsetUtil.charset(charset));
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
    public boolean isOnLoadPath(final File file, final Access mode) {
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
                    if (p.isOnPath(file, mode)) {
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

    @Override
    public boolean isRegularFileOnLoadPath(final File file, final Access mode) {
        if (file == null) {
            throw new IllegalArgumentException("A file must not be null");
        }

        try {
            for(LoadPath p : paths) {
                final boolean exists = p.isRegularFileOnLoadPath(file, mode);
                if (exists) {
                    return true;
                }
            }

            if (unlimitedAccess) {
                return file.isFile();
            }

            return false;
        }
        catch (VncException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new VncException(
                        String.format(
                                "Failed to check if the regular file '%s' exists the load path",
                                file.getPath()),
                        ex);
        }
   }

    @Override
    public boolean isDirectoryOnLoadPath(final File file, final Access mode) {
        if (file == null) {
            throw new IllegalArgumentException("A file must not be null");
        }

        try {
            for(LoadPath p : paths) {
                final boolean exists = p.isDirectoryOnLoadPath(file, mode);
                if (exists) {
                    return true;
                }
            }

            if (unlimitedAccess) {
                return file.isDirectory();
            }

            return false;
        }
        catch (VncException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new VncException(
                        String.format(
                                "Failed to check if the directory '%s' exists the load path",
                                file.getPath()),
                        ex);
        }
    }

    @Override
    public File normalize(final File file) {
        if (file == null) {
            throw new IllegalArgumentException("A file must not be null");
        }

        try {
            if (paths.isEmpty()) {
                return file;  // principle of least surprise
            }
            else {
                // try to normalize regardless of unlimited mode
                for(LoadPath p : paths) {
                    File normalized = p.normalize(file);
                    if (normalized != null) {
                        return normalized.getCanonicalFile();
                    }
                }

                if (unlimitedAccess) {
                    return file;  // principle of least surprise
                }
            }
        }
        catch(IOException ex) {
            throw new VncException("Failed to get canonical file", ex);
        }

        throw new VncException(
                String.format("Failed to normalize the file '%s'", file.getPath()));
    }


    @Override
    public InputStream getInputStream(final File file) {
        if (file == null) {
            throw new IllegalArgumentException("A file must not be null");
        }

        try {
            for(LoadPath p : paths) {
                final InputStream is = p.getInputStream(file);
                if (is != null) {
                    return is;
                }
            }

            if (unlimitedAccess) {
                if (!file.isAbsolute()) {
                    for(LoadPath p : paths) {
                        if (p instanceof DirectoryLoadPath) {
                            final File f = new File(p.path(), file.getPath());
                            if (f.exists()) {
                                return Files.newInputStream(f.toPath());
                            }
                        }
                    }
                }

                if (file.exists()) {
                    return Files.newInputStream(file.toPath());
                }
            }

            throw new VncException("No such file: '" + file.getPath() + "'");
        }
        catch (VncException ex) {
            throw ex;
        }
        catch (FileNotFoundException | NoSuchFileException ex) {
            throw new VncException(
                        String.format(
                                "No such file: '%s'",
                                file.getPath()),
                        ex);
        }
        catch (Exception ex) {
            throw new VncException(
                        String.format(
                                "Failed to return an InputStream for the file '%s' on the load path",
                                file.getPath()),
                        ex);
        }
    }

    @Override
    public BufferedReader getBufferedReader(final File file, final Charset charset) {
        if (file == null) {
            throw new IllegalArgumentException("A file must not be null");
        }

        try {
            for(LoadPath p : paths) {
                final BufferedReader br = p.getBufferedReader(file, charset);
                if (br != null) {
                    return br;
                }
            }

            if (unlimitedAccess) {
                if (!file.isAbsolute()) {
                    for(LoadPath p : paths) {
                        if (p instanceof DirectoryLoadPath) {
                            final File f = new File(p.path(), file.getPath());
                            if (f.exists()) {
                                return Files.newBufferedReader(f.toPath(), charset);
                            }
                        }
                    }
                }

                if (file.exists()) {
                    return Files.newBufferedReader(file.toPath(), charset);
                }
            }

            throw new VncException("No such file: '" + file.getPath() + "'");
        }
        catch (VncException ex) {
            throw ex;
        }
        catch (FileNotFoundException | NoSuchFileException ex) {
            throw new VncException(
                        String.format("No such file: '%s'",file.getPath()),
                        ex);
        }
        catch (Exception ex) {
            throw new VncException(
                        String.format(
                                "Failed to return a BufferedReader for the file '%s' on the load path",
                                file.getPath()),
                        ex);
        }
    }

    @Override
    public OutputStream getOutputStream(final File file, final OpenOption... options) {
        if (file == null) {
            throw new IllegalArgumentException("A file must not be null");
        }

        try {
            for(LoadPath p : paths) {
                final OutputStream os = p.getOutputStream(file, options);
                if (os != null) {
                    return os;
                }
            }

            if (unlimitedAccess) {
                if (!file.isAbsolute()) {
                    for(LoadPath p : paths) {
                        if (p instanceof DirectoryLoadPath) {
                            final File f = new File(p.path(), file.getPath());
                            return Files.newOutputStream(f.toPath(), options);
                        }
                    }
                }

                return Files.newOutputStream(file.toPath(), options);
            }

            return null;
        }
        catch (VncException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new VncException(
                        String.format(
                                "Failed to return an OutputStream for the file '%s' on the load path",
                                file.getPath()),
                        ex);
        }
    }

    @Override
    public boolean active() {
        return !getPaths().isEmpty();
    }


    private ByteBuffer load(final File file) {
        try {
            // try to load the file from one of the load paths
            final ByteBuffer dataFromLoadPath = loadBinary(file);

            if (dataFromLoadPath != null) {
                // prefer loading the file from the load paths
                return dataFromLoadPath;
            }
            else if (unlimitedAccess && file.isFile()) {
                // if the file has not been found on the load paths and 'unlimited'
                // file access is enabled load the file
                return ByteBuffer.wrap(Files.readAllBytes(file.toPath()));
            }
            else {
                // file is not available
                return null;
            }
        }
        catch (VncException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new VncException(
                        String.format("Failed to load the file '%s'", file.getPath()),
                        ex);
        }
    }

    private ByteBuffer loadBinary(final File file) throws IOException {
        for(LoadPath p : paths) {
            final ByteBuffer buf = p.load(file);
            if (buf != null) return buf;
        }
        return null;
    }

    private String toString(final ByteBuffer data, final Charset charset) {
        return data == null
                ? null
                : new String(data.array(), CharsetUtil.charset(charset));
    }


    private final List<LoadPath> paths = new ArrayList<>();
    private final boolean unlimitedAccess;
}
