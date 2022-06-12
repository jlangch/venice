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
package com.github.jlangch.venice.impl.util.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;


public class CSVReaderTest {

    @Test
    public void test_1() {
        final List<List<String>> records = new CSVReader().parse("1,2,3");

        assertEquals(1, records.size());
        assertEquals(3, records.get(0).size());
        assertEquals("1", records.get(0).get(0));
        assertEquals("2", records.get(0).get(1));
        assertEquals("3", records.get(0).get(2));
    }

    @Test
    public void test_2() {
        final List<List<String>> records = new CSVReader(',', '"').parse("1,2,3");

        assertEquals(1, records.size());
        assertEquals(3, records.get(0).size());
        assertEquals("1", records.get(0).get(0));
        assertEquals("2", records.get(0).get(1));
        assertEquals("3", records.get(0).get(2));
    }

    @Test
    public void test_3() {
        final List<List<String>> records = new CSVReader(',', '"').parse("1,,,4");

        assertEquals(1, records.size());
        assertEquals(4, records.get(0).size());
        assertEquals("1", records.get(0).get(0));
        assertEquals(null, records.get(0).get(1));
        assertEquals(null, records.get(0).get(2));
        assertEquals("4", records.get(0).get(3));
    }

    @Test
    public void test_4() {
        final List<List<String>> records = new CSVReader(',', '"').parse(",,,");

        assertEquals(1, records.size());
        assertEquals(3, records.get(0).size());
        assertEquals(null, records.get(0).get(0));
        assertEquals(null, records.get(0).get(1));
        assertEquals(null, records.get(0).get(2));
    }

    @Test
    public void test_5() {
        final List<List<String>> records =
                new CSVReader(',', '\'').parse("1,'Zurich','Wipkingen, X-''1''',ZH");

        assertEquals(1, records.size());
        assertEquals(4, records.get(0).size());
        assertEquals("1", records.get(0).get(0));
        assertEquals("Zurich", records.get(0).get(1));
        assertEquals("Wipkingen, X-'1'", records.get(0).get(2));
        assertEquals("ZH", records.get(0).get(3));
    }

}
