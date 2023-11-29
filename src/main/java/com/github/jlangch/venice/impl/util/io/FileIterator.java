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
package com.github.jlangch.venice.impl.util.io;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.github.jlangch.venice.FileException;


public class FileIterator implements Iterator<File> {

    public FileIterator(final File root) {
        this(root, (f) -> true);
    }

    public FileIterator(
            final File root,
            final Predicate<File> filter
    ) {
        if (root == null) {
            throw new IllegalArgumentException(
                    "A root dir must not be null!");
        }
        if (!root.isDirectory()) {
            throw new FileException(
                    "The root dir '" + root + "' does not exist!");
        }

        this.root = root;
        this.filter = filter;

        addChildren(root);
    }

    public Stream<File> stream() {
        return StreamSupport.stream(
                    Spliterators.spliteratorUnknownSize(
                            new FileIterator(root, filter),
                            Spliterator.ORDERED),
                    false);
    }

    @Override
    public boolean hasNext() {
        return hasDirs() || hasFiles();
    }

    @Override
    public File next() {
        while(hasDirs() || hasFiles()) {
            if (hasFiles()) {
                final File file = nextFile();
                if (filter.test(file)) return file;
            }
            else {
                final File dir = nextDir();
                addChildren(dir);
                if (filter.test(dir)) return dir;
            }
        }

        return null;
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


    private final File root;
    private final Predicate<File> filter;

    private final LinkedList<File> dirsLeft = new LinkedList<>();
    private final LinkedList<File> filesLeft = new LinkedList<>();
}
