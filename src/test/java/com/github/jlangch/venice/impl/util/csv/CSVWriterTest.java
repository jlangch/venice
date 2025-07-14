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

import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;


public class CSVWriterTest {

    @Test
    public void test_1() {
        final StringWriter wr = new StringWriter();

        final List<List<String>> records = Arrays.asList(Arrays.asList("1", "2", "3"));

        new CSVWriter().write(wr, records);

        assertEquals("1,2,3", wr.toString());
    }

    @Test
    public void test_2a() {
        final StringWriter wr = new StringWriter();

        final List<List<String>> records = Arrays.asList(Arrays.asList("1", "2", "3"));

        new CSVWriter(',', '"', "\n").write(wr, records);

        assertEquals("1,2,3", wr.toString());
    }

    @Test
    public void test_2b() {
        final StringWriter wr = new StringWriter();

        final List<List<String>> records = Arrays.asList(Arrays.asList("1", "2", "3"),
                                                         Arrays.asList("4", "5", "6"));

        new CSVWriter(',', '"', "\n").write(wr, records);

        assertEquals("1,2,3\n4,5,6", wr.toString());
    }

    @Test
    public void test_3() {
        final StringWriter wr = new StringWriter();

        final List<List<String>> records = Arrays.asList(Arrays.asList("1", "2", "3"));

        new CSVWriter(';', '"', "\n").write(wr, records);

        assertEquals("1;2;3", wr.toString());
    }

    @Test
    public void test_4() {
        final StringWriter wr = new StringWriter();

        final List<List<String>> records = Arrays.asList(Arrays.asList("1", null, "3"));

        new CSVWriter(',', '"', "\n").write(wr, records);

        assertEquals("1,,3", wr.toString());
    }

    @Test
    public void test_5() {
        final StringWriter wr = new StringWriter();

        final List<List<String>> records = Arrays.asList(Arrays.asList(null, null, null));

        new CSVWriter(',', '"', "\n").write(wr, records);

        assertEquals(",,", wr.toString());
    }

    @Test
    public void test_6() {
        final StringWriter wr = new StringWriter();

        final List<List<String>> records = Arrays.asList(Arrays.asList("1", "Zurich", "Wipkingen, X-'1'", "ZH"));

        new CSVWriter(',', '\'', "\n").write(wr, records);

        assertEquals("1,Zurich,'Wipkingen, X-''1''',ZH", wr.toString());
    }

    @Test
    public void test_7() {
        final StringWriter wr = new StringWriter();

        final List<List<String>> records = Arrays.asList(Arrays.asList("1", "Zurich", "Wipkingen,\n X-'1'", "ZH"));

        new CSVWriter(',', '\'', "\n").write(wr, records);

        assertEquals("1,Zurich,'Wipkingen,\n X-''1''',ZH", wr.toString());
    }

    @Test
    public void test_quote_due_to_delimiter() {
        final StringWriter wr = new StringWriter();

        final List<List<String>> records = Arrays.asList(Arrays.asList("1", "2,3"));

        new CSVWriter(',', '\'', "\n").write(wr, records);

        assertEquals("1,'2,3'", wr.toString());
    }

    @Test
    public void test_quote_due_to_lineseparator() {
        final StringWriter wr = new StringWriter();

        final List<List<String>> records = Arrays.asList(Arrays.asList("1", "2\n3"));

        new CSVWriter(',', '\'', "\n").write(wr, records);

        assertEquals("1,'2\n3'", wr.toString());
    }

    @Test
    public void test_quote_due_to_space() {
        final StringWriter wr = new StringWriter();

        final List<List<String>> records = Arrays.asList(Arrays.asList("1", "2 3"));

        new CSVWriter(',', '\'', "\n").write(wr, records);

        assertEquals("1,'2 3'", wr.toString());
    }

    @Test
    public void test_quote_due_to_quote_1a() {
        final StringWriter wr = new StringWriter();

        final List<List<String>> records = Arrays.asList(Arrays.asList("1", "'23"));

        new CSVWriter(',', '\'', "\n").write(wr, records);

        assertEquals("1,'''23'", wr.toString());
    }

    @Test
    public void test_quote_due_to_quote_1b() {
        final StringWriter wr = new StringWriter();

        final List<List<String>> records = Arrays.asList(Arrays.asList("1", "'''23"));

        new CSVWriter(',', '\'', "\n").write(wr, records);

        assertEquals("1,'''''''23'", wr.toString());
    }

    @Test
    public void test_quote_due_to_quote_2a() {
        final StringWriter wr = new StringWriter();

        final List<List<String>> records = Arrays.asList(Arrays.asList("1", "2'3"));

        new CSVWriter(',', '\'', "\n").write(wr, records);

        assertEquals("1,'2''3'", wr.toString());
    }

    @Test
    public void test_quote_due_to_quote_2b() {
        final StringWriter wr = new StringWriter();

        final List<List<String>> records = Arrays.asList(Arrays.asList("1", "2'''3"));

        new CSVWriter(',', '\'', "\n").write(wr, records);

        assertEquals("1,'2''''''3'", wr.toString());
    }

    @Test
    public void test_quote_due_to_quote_3a() {
        final StringWriter wr = new StringWriter();

        final List<List<String>> records = Arrays.asList(Arrays.asList("1", "23'"));

        new CSVWriter(',', '\'', "\n").write(wr, records);

        assertEquals("1,'23'''", wr.toString());
    }

    @Test
    public void test_quote_due_to_quote_3b() {
        final StringWriter wr = new StringWriter();

        final List<List<String>> records = Arrays.asList(Arrays.asList("1", "23'''"));

        new CSVWriter(',', '\'', "\n").write(wr, records);

        assertEquals("1,'23'''''''", wr.toString());
    }

}
