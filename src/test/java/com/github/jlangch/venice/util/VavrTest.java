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
package com.github.jlangch.venice.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.vavr.Tuple2;
import io.vavr.collection.Vector;


public class VavrTest {

    @Test
    public void testZip() {
        Vector<String> vec = Vector.of("a", "b", "c");

        Vector<Tuple2<String, Integer>> zipped = vec.zip(Vector.of(0, 1, 2));

        assertEquals(3, zipped.size());
    }

    @Test @Disabled
    public void testPerformance() {
        Vector<Long> vec = Vector.of(1L,1L,1L,1L,1L,1L,1L,1L,1L,1L,1L,1L,1L,1L,1L,1L,1L,1L);

        // WARMUP ------------------------------------------------------------
        int count = 0;
        for(int ii=0; ii<20000; ii++) {
            final List<Long> result = new ArrayList<>();
            vec.forEach(s -> result.add(s));
            count += result.size();
        }

        for(int ii=0; ii<20000; ii++) {
            final List<Long> result = new ArrayList<>();
            for(Long i : vec.asJava()) {
                result.add(i + 1L);
            }
            count += result.size();
        }

        for(int ii=0; ii<20000; ii++) {
            final List<Long> result = new ArrayList<>();
            for(Long i : vec.toJavaList()) {
                result.add(i + 1L);
            }
            count += result.size();
        }

        for(int ii=0; ii<20000; ii++) {
            final List<Long> result = new ArrayList<>();
            for(Long i : vec.asJavaMutable()) {
                result.add(i + 1L);
            }
            count += result.size();
        }


        // TEST ------------------------------------------------------------

        count = 0;

        System.gc();
        long nanos = System.nanoTime();
        for(int ii=0; ii<1000; ii++) {
            final List<Long> result = new ArrayList<>();
            vec.forEach(s -> result.add(s + 1L));
            count += result.size();
        }
        long elapsed = System.nanoTime() - nanos;
        System.out.println("Vavr forEach(): " + elapsed / 1000);

        System.gc();
        nanos = System.nanoTime();
        for(int ii=0; ii<1000; ii++) {
            final List<Long> result = new ArrayList<>();
            for(Long i : vec.asJava()) {
                result.add(i + 1L);
            }
            count += result.size();
        }
        elapsed = System.nanoTime() - nanos;
        System.out.println("Vavr vec.asJava(): " + elapsed / 1000);

        System.gc();
        nanos = System.nanoTime();
        for(int ii=0; ii<1000; ii++) {
            final List<Long> result = new ArrayList<>();
            for(Long i : vec.toJavaList()) {
                result.add(i + 1L);
            }
            count += result.size();
        }
        elapsed = System.nanoTime() - nanos;
        System.out.println("Vavr vec.toJavaList(): " + elapsed / 1000);

        System.gc();
        nanos = System.nanoTime();
        for(int ii=0; ii<1000; ii++) {
            final List<Long> result = new ArrayList<>();
            for(Long i : vec.asJavaMutable()) {
                result.add(i + 1L);
            }
            count += result.size();
        }
        elapsed = System.nanoTime() - nanos;
        System.out.println("Vavr vec.asJavaMutable(): " + elapsed / 1000);

        assertEquals(72000, count);
    }
}
