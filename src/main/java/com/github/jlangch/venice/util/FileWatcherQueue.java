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
package com.github.jlangch.venice.util;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class FileWatcherQueue {

    public FileWatcherQueue(final int maxSize) {
        this.maxSize = Math.max(10, maxSize);  // minimum 10
    }

    public int size() {
        synchronized(queue) {
            return queue.size();
        }
    }

    public boolean isEmpty() {
        synchronized(queue) {
            return queue.isEmpty();
        }
    }

    public void clear() {
        synchronized(queue) {
            queue.clear();
        }
    }

    public void remove(final File file) {
        if (file != null) {
            synchronized(queue) {
                queue.removeIf(it -> it.equals(file));
            }
        }
    }

    public void push(final File file) {
        if (file != null) {
            synchronized(queue) {
                queue.removeIf(it -> it.equals(file));

                // limit the size
                while(queue.size() >= maxSize) {
                    queue.removeFirst();
                }

                queue.add(file);
            }
        }
    }

    public File pop() {
        synchronized(queue) {
            return queue.isEmpty() ? null : queue.removeFirst();
        }
    }

    public List<File> pop(final int n) {
        synchronized(queue) {
            final List<File> files = new ArrayList<>(n);
            for(int ii=0; ii<n  && !queue.isEmpty(); ii++) {
                files.add(queue.removeFirst());
            }
            return files;
        }
    }


    private final int maxSize;
    private final LinkedList<File> queue = new LinkedList<>();
}
