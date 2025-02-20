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
package com.github.jlangch.venice.impl.util.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

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
    public void test_2a() {
        final List<List<String>> records = new CSVReader(',', '"').parse("");

        assertEquals(0, records.size());
    }

    @Test
    public void test_2b() {
        final List<List<String>> records = new CSVReader(',', '"').parse("1");

        assertEquals(1, records.size());
        assertEquals(1, records.get(0).size());
        assertEquals("1", records.get(0).get(0));
    }

    @Test
    public void test_2c() {
        final List<List<String>> records = new CSVReader(',', '"').parse("1,2,3");

        assertEquals(1, records.size());
        assertEquals(3, records.get(0).size());
        assertEquals("1", records.get(0).get(0));
        assertEquals("2", records.get(0).get(1));
        assertEquals("3", records.get(0).get(2));
    }

    @Test
    public void test_3a() {
        final List<List<String>> records = new CSVReader(',', '"').parse("\n");

        assertEquals(0, records.size());
    }

    @Test
    public void test_3b() {
        final List<List<String>> records = new CSVReader(',', '"').parse("1\n2");

        assertEquals(2, records.size());

        assertEquals(1, records.get(0).size());
        assertEquals("1", records.get(0).get(0));

        assertEquals(1, records.get(1).size());
        assertEquals("2", records.get(1).get(0));
    }

    @Test
    public void test_3c() {
        final List<List<String>> records = new CSVReader(',', '"').parse("1,2,3\n4,5,6");

        assertEquals(2, records.size());

        assertEquals(3, records.get(0).size());
        assertEquals("1", records.get(0).get(0));
        assertEquals("2", records.get(0).get(1));
        assertEquals("3", records.get(0).get(2));

        assertEquals(3, records.get(1).size());
        assertEquals("4", records.get(1).get(0));
        assertEquals("5", records.get(1).get(1));
        assertEquals("6", records.get(1).get(2));
    }

    @Test
    public void test_4a() {
        final List<List<String>> records = new CSVReader(',', '"').parse("\n\n");

        assertEquals(0, records.size());
    }

    @Test
    public void test_4b() {
        final List<List<String>> records = new CSVReader(',', '"').parse("1\n2\n");

        assertEquals(2, records.size());

        assertEquals(1, records.get(0).size());
        assertEquals("1", records.get(0).get(0));

        assertEquals(1, records.get(1).size());
        assertEquals("2", records.get(1).get(0));
    }

    @Test
    public void test_4c() {
        final List<List<String>> records = new CSVReader(',', '"').parse("1,2,3\n4,5,6\n");

        assertEquals(2, records.size());

        assertEquals(3, records.get(0).size());
        assertEquals("1", records.get(0).get(0));
        assertEquals("2", records.get(0).get(1));
        assertEquals("3", records.get(0).get(2));

        assertEquals(3, records.get(1).size());
        assertEquals("4", records.get(1).get(0));
        assertEquals("5", records.get(1).get(1));
        assertEquals("6", records.get(1).get(2));
    }

    @Test
    public void test_5a() {
        final List<List<String>> records = new CSVReader(',', '"').parse(",");

        assertEquals(1, records.size());
        assertEquals(2, records.get(0).size());
        assertEquals(null, records.get(0).get(0));
        assertEquals(null, records.get(0).get(1));
    }

    @Test
    public void test_5a_lf1() {
        final List<List<String>> records = new CSVReader(',', '"').parse(",\n");

        assertEquals(1, records.size());
        assertEquals(2, records.get(0).size());
        assertEquals(null, records.get(0).get(0));
        assertEquals(null, records.get(0).get(1));
    }

    @Test
    public void test_5a_lf2() {
        final List<List<String>> records = new CSVReader(',', '"').parse(",\n,");

        assertEquals(2, records.size());

        assertEquals(2, records.get(0).size());
        assertEquals(null, records.get(0).get(0));
        assertEquals(null, records.get(0).get(1));

        assertEquals(2, records.get(1).size());
        assertEquals(null, records.get(1).get(0));
        assertEquals(null, records.get(1).get(1));
    }

    @Test
    public void test_5b() {
        final List<List<String>> records = new CSVReader(',', '"').parse(",,");

        assertEquals(1, records.size());
        assertEquals(3, records.get(0).size());
        assertEquals(null, records.get(0).get(0));
        assertEquals(null, records.get(0).get(1));
        assertEquals(null, records.get(0).get(2));
    }

    @Test
    public void test_5b_lf1() {
        final List<List<String>> records = new CSVReader(',', '"').parse(",,\n");

        assertEquals(1, records.size());
        assertEquals(3, records.get(0).size());
        assertEquals(null, records.get(0).get(0));
        assertEquals(null, records.get(0).get(1));
        assertEquals(null, records.get(0).get(2));
    }

    @Test
    public void test_5b_lf2() {
        final List<List<String>> records = new CSVReader(',', '"').parse(",,\n,,");

        assertEquals(2, records.size());

        assertEquals(3, records.get(0).size());
        assertEquals(null, records.get(0).get(0));
        assertEquals(null, records.get(0).get(1));
        assertEquals(null, records.get(0).get(2));

        assertEquals(3, records.get(1).size());
        assertEquals(null, records.get(1).get(0));
        assertEquals(null, records.get(1).get(1));
        assertEquals(null, records.get(1).get(2));
    }

    @Test
    public void test_5c() {
        final List<List<String>> records = new CSVReader(',', '"').parse(",,,");

        assertEquals(1, records.size());
        assertEquals(4, records.get(0).size());
        assertEquals(null, records.get(0).get(0));
        assertEquals(null, records.get(0).get(1));
        assertEquals(null, records.get(0).get(2));
        assertEquals(null, records.get(0).get(3));
    }

    @Test
    public void test_5c_lf1() {
        final List<List<String>> records = new CSVReader(',', '"').parse(",,,\n");

        assertEquals(1, records.size());
        assertEquals(4, records.get(0).size());
        assertEquals(null, records.get(0).get(0));
        assertEquals(null, records.get(0).get(1));
        assertEquals(null, records.get(0).get(2));
        assertEquals(null, records.get(0).get(3));
    }

    @Test
    public void test_5c_lf2() {
        final List<List<String>> records = new CSVReader(',', '"').parse(",,,\n,,,");

        assertEquals(2, records.size());

        assertEquals(4, records.get(0).size());
        assertEquals(null, records.get(0).get(0));
        assertEquals(null, records.get(0).get(1));
        assertEquals(null, records.get(0).get(2));
        assertEquals(null, records.get(0).get(3));

        assertEquals(4, records.get(01).size());
        assertEquals(null, records.get(1).get(0));
        assertEquals(null, records.get(1).get(1));
        assertEquals(null, records.get(1).get(2));
        assertEquals(null, records.get(1).get(3));
    }

    @Test
    public void test_6a() {
        final List<List<String>> records = new CSVReader(',', '"').parse("1,");

        assertEquals(1, records.size());
        assertEquals(2, records.get(0).size());
        assertEquals("1", records.get(0).get(0));
        assertEquals(null, records.get(0).get(1));
    }

    @Test
    public void test_6b() {
        final List<List<String>> records = new CSVReader(',', '"').parse(",1");

        assertEquals(1, records.size());
        assertEquals(2, records.get(0).size());
        assertEquals(null, records.get(0).get(0));
        assertEquals("1", records.get(0).get(1));
    }

    @Test
    public void test_7a() {
        final List<List<String>> records = new CSVReader(',', '"').parse("1,,");

        assertEquals(1, records.size());
        assertEquals(3, records.get(0).size());
        assertEquals("1", records.get(0).get(0));
        assertEquals(null, records.get(0).get(1));
        assertEquals(null, records.get(0).get(2));
    }

    @Test
    public void test_7b() {
        final List<List<String>> records = new CSVReader(',', '"').parse(",1,");

        assertEquals(1, records.size());
        assertEquals(3, records.get(0).size());
        assertEquals(null, records.get(0).get(0));
        assertEquals("1", records.get(0).get(1));
        assertEquals(null, records.get(0).get(2));
    }

    @Test
    public void test_7c() {
        final List<List<String>> records = new CSVReader(',', '"').parse(",,1");

        assertEquals(1, records.size());
        assertEquals(3, records.get(0).size());
        assertEquals(null, records.get(0).get(0));
        assertEquals(null, records.get(0).get(1));
        assertEquals("1", records.get(0).get(2));
    }

    @Test
    public void test_7d() {
        final List<List<String>> records = new CSVReader(',', '"').parse("1,1,");

        assertEquals(1, records.size());
        assertEquals(3, records.get(0).size());
        assertEquals("1", records.get(0).get(0));
        assertEquals("1", records.get(0).get(1));
        assertEquals(null, records.get(0).get(2));
    }

    @Test
    public void test_7e() {
        final List<List<String>> records = new CSVReader(',', '"').parse(",1,1");

        assertEquals(1, records.size());
        assertEquals(3, records.get(0).size());
        assertEquals(null, records.get(0).get(0));
        assertEquals("1", records.get(0).get(1));
        assertEquals("1", records.get(0).get(2));
    }

    @Test
    public void test_7f() {
        final List<List<String>> records = new CSVReader(',', '"').parse("1,,1");

        assertEquals(1, records.size());
        assertEquals(3, records.get(0).size());
        assertEquals("1", records.get(0).get(0));
        assertEquals(null, records.get(0).get(1));
        assertEquals("1", records.get(0).get(2));
    }

    @Test
    public void test_10a() {
        final List<List<String>> records = new CSVReader(',', '\'').parse("1,'''zh'");

        assertEquals(1, records.size());

        final List<String> record = records.get(0);
        assertEquals(2, record.size());
        assertEquals("1",record.get(0));
        assertEquals("'zh", record.get(1));
    }

    @Test
    public void test_10b() {
        final List<List<String>> records = new CSVReader(',', '\'').parse("1,'zh'''");

        assertEquals(1, records.size());

        final List<String> record = records.get(0);
        assertEquals(2, record.size());
        assertEquals("1",record.get(0));
        assertEquals("zh'", record.get(1));
    }

    @Test
    public void test_10c() {
        final List<List<String>> records = new CSVReader(',', '\'').parse("1,'''zh'''");

        assertEquals(1, records.size());

        final List<String> record = records.get(0);
        assertEquals(2, record.size());
        assertEquals("1",record.get(0));
        assertEquals("'zh'", record.get(1));
    }

    @Test
    public void test_10d() {
        final List<List<String>> records = new CSVReader(',', '\'').parse("1,'''''''zh'''''''");

        assertEquals(1, records.size());

        final List<String> record = records.get(0);
        assertEquals(2, record.size());
        assertEquals("1",record.get(0));
        assertEquals("'''zh'''", record.get(1));
    }

    @Test
    public void test_11a_error() {
        try {
            new CSVReader(',', '\'').parse("1,'''zh' ");
            fail("Expected RuntimeException");
        }
        catch (RuntimeException ex) {
            assertEquals("Unexpected char ' ' after quoted field at line 1, col 9.", ex.getMessage());
        }
    }

    @Test
    public void test_11b_error() {
        try {
            new CSVReader(',', '\"').parse("1,2\"3");
            fail("Expected RuntimeException");
        }
        catch (RuntimeException ex) {
            assertEquals("The quote char '\"' must not appear in a non quoted field at line 1, col 4.", ex.getMessage());
        }
    }

    @Test
    public void test_12a() {
        final List<List<String>> records = new CSVReader(',', '\'').parse("1,'z''h',");

        assertEquals(1, records.size());

        final List<String> record = records.get(0);
        assertEquals(3, record.size());
        assertEquals("1",record.get(0));
        assertEquals("z'h", record.get(1));
        assertEquals(null, record.get(2));
    }

    @Test
    public void test_12b() {
        final List<List<String>> records = new CSVReader(',', '\'').parse("1,,'z''h'");

        assertEquals(1, records.size());

        final List<String> record = records.get(0);
        assertEquals(3, record.size());
        assertEquals("1",record.get(0));
        assertEquals(null, record.get(1));
        assertEquals("z'h", record.get(2));
    }

    @Test
    public void test_13a() {
        final List<List<String>> records =
                new CSVReader(',', '\'').parse("1,'Zurich','Wipkingen, X-''1''',ZH");

        assertEquals(1, records.size());

        final List<String> record = records.get(0);
        assertEquals(4, record.size());
        assertEquals("1",record.get(0));
        assertEquals("Zurich", record.get(1));
        assertEquals("Wipkingen, X-'1'", record.get(2));
        assertEquals("ZH", record.get(3));
    }

    @Test
    public void test_13b() {
        final List<List<String>> records =
                new CSVReader(',', '\'').parse("1,'Zurich','Wipkingen, X-''1''',ZH\n1,'Zurich','Hoengg, X-''2''',ZH");

        assertEquals(2, records.size());

        List<String> record = records.get(0);
        assertEquals(4, record.size());
        assertEquals("1",record.get(0));
        assertEquals("Zurich", record.get(1));
        assertEquals("Wipkingen, X-'1'", record.get(2));
        assertEquals("ZH", record.get(3));

        record = records.get(1);
        assertEquals(4, record.size());
        assertEquals("1",record.get(0));
        assertEquals("Zurich", record.get(1));
        assertEquals("Hoengg, X-'2'", record.get(2));
        assertEquals("ZH", record.get(3));
    }

}
