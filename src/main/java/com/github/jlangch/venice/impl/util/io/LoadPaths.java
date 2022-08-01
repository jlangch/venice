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
package com.github.jlangch.venice.impl.util.io;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.util.CollectionUtil;
import com.github.jlangch.venice.impl.util.io.zip.ZipFileSystemUtil;
import com.github.jlangch.venice.javainterop.ILoadPaths;


public class LoadPaths implements ILoadPaths {

    private LoadPaths(
            final List<File> paths,
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
                            .filter(p -> p.isFile() || p.isDirectory())
                            .map(p -> canonical(p.getAbsoluteFile()))
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
        return Collections.unmodifiableList(paths);
    }

    @Override
    public boolean isUnlimitedAccess() {
        return unlimitedAccess;
    }


    private ByteBuffer load(final File file) {
    	// try to load the file from one of the load paths
        final ByteBuffer dataFromLoadPath = paths.stream()
                                                 .map(p -> loadFromLoadPath(p, file))
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
            return loadFileData(file);
        }
        else {
            // file is not available
            return null;
        }
    }

    private ByteBuffer loadFromLoadPath(
            final File loadPath,
            final File file
    ) {
        if (loadPath.getName().endsWith(".zip")) {
            return loadFileFromZip(loadPath, file);
        }
        else if (loadPath.isDirectory()) {
            return loadFileFromDir(loadPath, file);
        }
        else if (loadPath.isFile()) {
            final File f = canonical(file);
            if (loadPath.equals(f)) {
                try {
                    return ByteBuffer.wrap(Files.readAllBytes(f.toPath()));
                }
                catch(IOException ex) {
                    return null; // just proceed and try with next load path
                }
            }
        }

        // not found on this load path, proceed and try with next load path
        return null;
    }

    private ByteBuffer loadFileFromZip(
            final File zip,
            final File file
    ) {
        if (zip.exists()) {
            try {
                return ZipFileSystemUtil
                            .loadBinaryFileFromZip(zip, file)
                            .getValue();
            }
            catch(Exception ex) {
                return null;
            }
        }
        else {
            return null;
        }
    }

    private ByteBuffer loadFileFromDir(final File loadPath, final File file) {
        try {
            if (file.isAbsolute()) {
                return isFileWithinDirectory(loadPath, file)
                        ? loadFileData(file)
                        : null;
            }
            else {
                final File f = new File(loadPath, file.getPath());
                return f.isFile()
                        ? loadFileData(new File(loadPath, file.getPath()))
                        : null;
            }
        }
        catch (Exception ex) {
            throw new VncException(
                        String.format("Failed to load file '%s'", file.getPath()),
                        ex);
        }
    }

    private ByteBuffer loadFileData(final File file) {
        try {
            return ByteBuffer.wrap(Files.readAllBytes(file.toPath()));
        }
        catch(IOException ex) {
            return null;
        }
    }

    private boolean isFileWithinDirectory(
            final File dir,
            final File file
    ) throws IOException {
        final File dir_ = dir.getAbsoluteFile();
        if (dir_.isDirectory()) {
            final File file_ = file.getAbsoluteFile();
            if (file_.getCanonicalFile().toPath().startsWith(dir_.getCanonicalFile().toPath())) {
                // Prevent accessing files outside the load-path
            	//
            	// Load path:  [/Users/pit/scripts]
                // E.g.: foo.venice             =>  /Users/pit/scripts/foo.venice        (ok)
            	//       ../../foo.venice       =>  /Users/pit/scripts/../../foo.venice  (!!!)
                //       /Users/pit/foo.venice  =>  /Users/pit/foo.venice                (!!!)
                return true;
            }
        }

        return false;
    }

    private static File canonical(final File file) {
        try {
            return file.getCanonicalFile();
        }
        catch(IOException ex) {
            throw new VncException(
                    String.format(
                            "The file '%s' can not be converted to a canonical path!",
                            file.getPath()),
                    ex);
        }
    }

    private String toString(final ByteBuffer data, final String encoding) {
        if (data == null) {
            return null;
        }
        else {
            final Charset charset = encoding == null || encoding.isEmpty()
                                        ? Charset.defaultCharset()
                                        : Charset.forName(encoding);
            return new String(data.array(), charset);
        }
    }


    // a list of existing canonical paths
    private final List<File> paths = new ArrayList<>();
    private final boolean unlimitedAccess;
}
