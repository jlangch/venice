/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2026 Venice
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
package com.github.jlangch.venice.util.ipc.impl.util;

import java.util.Objects;

import com.github.jlangch.venice.impl.util.io.zip.GZipper;


public class Compressor {


    public Compressor(final long cutoffSize) {
        this.cutoffSize = cutoffSize < 0 ? -1 : cutoffSize;
    }


    public static Compressor off() {
        return new Compressor(-1);
    }


    public byte[] compress(final byte[] data) {
        Objects.requireNonNull(data);
        return needsCompression(data) ? compress(data, true) : data;
    }

    public byte[] compress(final byte[] data, final boolean compress) {
        Objects.requireNonNull(data);
        return compress ? GZipper.gzip(data) : data;
    }

    public byte[] decompress(final byte[] data, final boolean decompress) {
        Objects.requireNonNull(data);
        return decompress ? GZipper.ungzip(data) : data;
    }

    public long cutoffSize() {
        return cutoffSize;
    }

    public boolean needsCompression(final byte[] data) {
        return cutoffSize >= 0 && data.length >= cutoffSize;
    }

    public boolean isActive() {
        return cutoffSize >= 0;
    }


    private final long cutoffSize;
}
