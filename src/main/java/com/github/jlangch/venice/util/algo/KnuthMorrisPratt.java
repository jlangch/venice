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
package com.github.jlangch.venice.util.algo;


/**
 * Knuth-Morris-Pratt (KMP) pattern matching algorithm.
 *
 * The KMP algorithm is an efficient method for finding the occurrence
 * of a substring (a pattern) within a larger string (or in this case,
 * a sequence of bytes)
 *
 * The <code>computeFailure</code> function pre-processes the pattern to
 * facilitate a faster substring search by tracking the longest prefix
 * that's also a suffix at each point in the pattern. This preprocessing
 * step is crucial for the efficiency of the KMP algorithm.
 */
public abstract class KnuthMorrisPratt {

    /**
     * Searches for a byte pattern in a larger data buffer.
     *
     * @param data      The data buffer
     * @param pattern   The pattern to search for
     * @param indexFrom The start index to begin search with
     *
     * @return The index of the first occurrence of the pattern
     *         in the buffer or -1 of the pattern is not found.
     */
    public static int indexOf(
            final byte[] data,
            final byte[] pattern,
            final int indexFrom
    ) {
        if (data == null) {
            throw new IllegalArgumentException("A data byte array must not be null!");
        }
        if (pattern == null) {
            throw new IllegalArgumentException("A pattern byte array must not be null!");
        }
        if (indexFrom < 0) {
            throw new IllegalArgumentException("An indexFrom must not be negative!");
        }

        if (indexFrom > data.length - pattern.length) {
            return -1;
        }
        if (data.length == 0 || pattern.length == 0) {
            return -1;
        }
        if (data.length < pattern.length) {
            return -1;
        }


        final int[] failure = computeFailure(pattern);

        int j = 0;

        for (int i = indexFrom; i < data.length; i++) {
            while (j > 0 && pattern[j] != data[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == data[i]) {
                j++;
            }
            if (j == pattern.length) {
                return i - pattern.length + 1;
            }
        }
        return -1;
    }

    private static int[] computeFailure(final byte[] pattern) {
        final int[] failure = new int[pattern.length];

        int j = 0;
        for (int i = 1; i < pattern.length; i++) {
            while (j>0 && pattern[j] != pattern[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == pattern[i]) {
                j++;
            }
            failure[i] = j;
        }

        return failure;
    }

}
