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
package com.github.jlangch.venice.util.ipc.impl.queue;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;


public class CircularBufferTest {

    @Test
    public void testBasic() {
        final CircularBuffer<Integer> buffer = new CircularBuffer<>("test", 5);

        buffer.offer(0);
        assertEquals(1, buffer.size());

        assertEquals(0, (int)buffer.poll());
        assertEquals(0, buffer.size());

        buffer.offer(0);
        buffer.offer(1);
        buffer.offer(2);
        buffer.offer(3);
        buffer.offer(4);

        assertEquals(5, buffer.size());

        assertEquals(0, (int)buffer.poll());
        assertEquals(1, (int)buffer.poll());
        assertEquals(2, (int)buffer.poll());
        assertEquals(3, (int)buffer.poll());
        assertEquals(4, (int)buffer.poll());

        assertEquals(0, buffer.size());
    }

    @Test
    public void testCircle() {
        final CircularBuffer<Integer> buffer = new CircularBuffer<>("test", 5);

        buffer.offer(0);
        buffer.offer(1);
        buffer.offer(2);
        buffer.offer(3);
        buffer.offer(4);

        assertEquals(5, buffer.size());

        buffer.offer(5);

        assertEquals(5, buffer.size());

        buffer.offer(6);

        assertEquals(5, buffer.size());

        assertEquals(2, (int)buffer.poll());
        assertEquals(3, (int)buffer.poll());
        assertEquals(4, (int)buffer.poll());
        assertEquals(5, (int)buffer.poll());
        assertEquals(6, (int)buffer.poll());
    }

    @Test
    public void testLoad_1() {
        final CircularBuffer<Integer> buffer = new CircularBuffer<>("test", 5);

        for(int ii=0; ii<100; ii++) {
            buffer.offer(ii);
            assertEquals(ii, (int)buffer.poll());
        }
    }

    @Test
    public void testLoad_2() {
        final CircularBuffer<Integer> buffer = new CircularBuffer<>("test", 5);

        buffer.offer(0);
        buffer.offer(1);
        buffer.offer(2);
        buffer.offer(3);
        buffer.offer(4);

        for(int ii=0; ii<100; ii++) {
            buffer.offer(ii);
        }

        assertEquals(5, buffer.size());

        assertEquals(95, (int)buffer.poll());
        assertEquals(96, (int)buffer.poll());
        assertEquals(97, (int)buffer.poll());
        assertEquals(98, (int)buffer.poll());
        assertEquals(99, (int)buffer.poll());

        assertEquals(0, buffer.size());
    }

}
