/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2023 Venice
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
package com.github.jlangch.venice.impl.util;

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

import com.github.jlangch.venice.FileException;
import com.github.jlangch.venice.impl.types.IVncFunction;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncVal;


public class VncFileIterator implements Iterator<VncVal>, Iterable<VncVal> {

    public VncFileIterator(
            final File root,
            final IVncFunction filter
    ) {
        if (root == null) {
            throw new FileException("A file iterator root dir must not be null!");
        }
        if (!root.isDirectory()) {
            throw new FileException(
                    "The file iterator root dir '" + root + "' does not exist!");
        }

        this.filter = filter;

        addChildren(root);
    }


    @Override
    public boolean hasNext() {
        return hasDirs() || hasFiles();
    }

    @Override
    public VncVal next() {
        while(hasDirs() || hasFiles()) {
        	final VncVal item;

            if (hasFiles()) {
                final File file = nextFile();
                item = new VncJavaObject(file);
            }
            else {
                final File dir = nextDir();
                addChildren(dir);
                item = new VncJavaObject(dir);
            }

            if (filter == null) {
                return item;
            }
            else {
                if (VncBoolean.isTrue(filter.applyOf(item))) return item;
            }
        }

        return Nil;
    }

    @Override
    public Iterator<VncVal> iterator() {
        return this;
    }

    public void stop() {
        dirsLeft.clear();
        filesLeft.clear();
    }


    private void addChildren(final File parent) {
        Arrays
            .stream(parent.listFiles())
            .sorted(Comparator.comparing(File::getName))
            .forEach(f -> {
                if (f.isDirectory()) {
                    dirsLeft.addLast(f);
                }
                else if (f.isFile()) {
                    filesLeft.addLast(f);
                }
             });
    }

    private boolean hasDirs() {
        return !dirsLeft.isEmpty();
    }

    private boolean hasFiles() {
        return !filesLeft.isEmpty();
    }

    private File nextDir() {
        return hasDirs() ? dirsLeft.removeFirst() : null;
    }

    private File nextFile() {
        return hasFiles() ? filesLeft.removeFirst() : null;
    }


    private final IVncFunction filter;

    private final LinkedList<File> dirsLeft = new LinkedList<>();
    private final LinkedList<File> filesLeft = new LinkedList<>();
}
