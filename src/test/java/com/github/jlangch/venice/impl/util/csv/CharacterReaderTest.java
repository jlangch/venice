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
package com.github.jlangch.venice.impl.util.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;


public class CharacterReaderTest {

    @Test
    public void test() {
        final CharacterReader rd = new CharacterReader("12\n3");

        assertEquals(false, rd.isEof());
        assertEquals('1', rd.peek());
        assertEquals(1, rd.getLineNr());
        assertEquals(1, rd.getColNr());

        rd.consume();

        assertEquals(false, rd.isEof());
        assertEquals('2', rd.peek());
        assertEquals(1, rd.getLineNr());
        assertEquals(2, rd.getColNr());

        rd.consume();

        assertEquals(false, rd.isEof());
        assertEquals('\n', rd.peek());
        assertEquals(1, rd.getLineNr());
        assertEquals(3, rd.getColNr());

        rd.consume();

        assertEquals(false, rd.isEof());
        assertEquals('3', rd.peek());
        assertEquals(2, rd.getLineNr());
        assertEquals(1, rd.getColNr());

        rd.consume();

        assertEquals(true, rd.isEof());
        assertEquals(-1, rd.peek());
        assertEquals(2, rd.getLineNr());
        assertEquals(2, rd.getColNr());
    }

}
