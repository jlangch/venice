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

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.util.io.zip.ZipFileSystemUtil;
import com.github.jlangch.venice.javainterop.ILoadPaths;


public class LoadPaths implements ILoadPaths {

    private LoadPaths(
            final List<File> paths,
            final boolean unlimitedAccess
    ) {
        if (paths != null) {
            this.paths.addAll(paths);
        }
        this.unlimitedAccess = unlimitedAccess;
    }

    public static LoadPaths of(
            final List<File> paths,
            final boolean unlimitedAccess
    ) {
        if (paths == null || paths.isEmpty()) {
            return new LoadPaths(null, unlimitedAccess);
        }
        else {
            final List<File> savePaths = new ArrayList<>();

            for(File p : paths) {
                if (p != null) {
                    if (p.isFile() || p.isDirectory()) {
                        savePaths.add(canonical(p.getAbsoluteFile()));
                    }
                    else {
                        // skip silently
                    }
                }
            }

            return new LoadPaths(savePaths, unlimitedAccess);
        }
    }

    @Override
    public String loadVeniceFile(final File file) {
        if (file == null) {
            return null;
        }
        else {
            final String path = file.getPath();

            final String vncFile = path.endsWith(".venice") ? path : path + ".venice";

            final ByteBuffer data = load(new File(vncFile));

            return data == null
                    ? null
                    : new String(data.array(), getCharset("UTF-8"));
        }
    }

    @Override
    public ByteBuffer loadBinaryResource(final File file) {
        return load(file);
    }

    @Override
    public String loadTextResource(final File file, final String encoding) {
        final ByteBuffer data = load(file);

        return data == null
                ? null
                : new String(data.array(), getCharset(encoding));
    }

    @Override
    public List<File> getPaths() {
        return Collections.unmodifiableList(paths);
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
            final File f = canonical(file);
            final File dir = f.getParentFile();

            // check load paths
            for(File p : paths) {
                if (p.isDirectory()) {
                    if (dir.equals(p)) return true;
                }
                else if (p.isFile()) {
                    if (f.equals(p)) return true;
                }
            }

            return false;
        }
    }

    @Override
    public boolean isUnlimitedAccess() {
        return unlimitedAccess;
    }


    private ByteBuffer load(final File file) {
        final ByteBuffer dataFromLoadPath = paths.stream()
                                                 .map(p -> loadFromLoadPath(p, file))
                                                 .filter(d -> d != null)
                                                 .findFirst()
                                                 .orElse(null);

        if (dataFromLoadPath != null) {
            return dataFromLoadPath;
        }
        else if (unlimitedAccess && file.isFile()) {
            return loadFile(file);
        }
        else {
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
                    return null;
                }
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
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
                        ? loadFile(file)
                        : null;
            }
            else {
                final File f = new File(loadPath, file.getPath());
                return f.isFile()
                        ? loadFile(new File(loadPath, file.getPath()))
                        : null;
            }
        }
        catch (Exception ex) {
            throw new VncException(
                        String.format("Failed to load file '%s'", file.getPath()),
                        ex);
        }
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

    private ByteBuffer loadFile(final File file) {
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
            final File fl = new File(dir_, file.getPath());
            if (fl.isFile()) {
                if (fl.getCanonicalPath().startsWith(dir_.getCanonicalPath())) {
                    // Prevent accessing files outside the load-path.
                    // E.g.: ../../coffee
                    return true;
                }
            }
        }

        return false;
    }

    private Charset getCharset(final String encoding) {
        return encoding == null || encoding.isEmpty()
                ? Charset.defaultCharset()
                : Charset.forName(encoding);
    }


    // a list of existing canonical paths
    private final List<File> paths = new ArrayList<>();
    private final boolean unlimitedAccess;
}
