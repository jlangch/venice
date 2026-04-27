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
package com.github.jlangch.venice.impl.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class CsvFunctionsTest {

    @Test
    public void test_singleline_comma() {
        final Venice venice = new Venice();

        final String script = "(csv/read \"1,2,3,4,5\")";

        @SuppressWarnings("unchecked")
        final List<List<String>> items = (List<List<String>>)venice.eval(script);

        assertEquals(1, items.size());

        final List<String> item = items.get(0);
        assertEquals(5, item.size());

        assertEquals("1", item.get(0));
        assertEquals("2", item.get(1));
        assertEquals("3", item.get(2));
        assertEquals("4", item.get(3));
        assertEquals("5", item.get(4));
     }

    @Test
    public void test_singleline_semi_colon() {
        final Venice venice = new Venice();

        final String script = "(csv/read \"1;2;3;4;5\" :separator \";\")";

        @SuppressWarnings("unchecked")
        final List<List<String>> items = (List<List<String>>)venice.eval(script);

        assertEquals(1, items.size());

        final List<String> item = items.get(0);
        assertEquals(5, item.size());

        assertEquals("1", item.get(0));
        assertEquals("2", item.get(1));
        assertEquals("3", item.get(2));
        assertEquals("4", item.get(3));
        assertEquals("5", item.get(4));
     }

    @Test
    public void test_multline_lf_comma() {
        final Venice venice = new Venice();

        final String script = "(csv/read \"11,12,13,14,15\n21,22,23,24,25\")";

        @SuppressWarnings("unchecked")
        final List<List<String>> items = (List<List<String>>)venice.eval(script);

        assertEquals(2, items.size());

        List<String> item = items.get(0);
        assertEquals(5, item.size());

        assertEquals("11", item.get(0));
        assertEquals("12", item.get(1));
        assertEquals("13", item.get(2));
        assertEquals("14", item.get(3));
        assertEquals("15", item.get(4));

        item = items.get(1);
        assertEquals(5, item.size());

        assertEquals("21", item.get(0));
        assertEquals("22", item.get(1));
        assertEquals("23", item.get(2));
        assertEquals("24", item.get(3));
        assertEquals("25", item.get(4));
      }

    @Test
    public void test_multline_lf_semi_colon() {
        final Venice venice = new Venice();

        final String script = "(csv/read \"11;12;13;14;15\n21;22;23;24;25\" :separator \";\")";

        @SuppressWarnings("unchecked")
        final List<List<String>> items = (List<List<String>>)venice.eval(script);

        assertEquals(2, items.size());

        List<String> item = items.get(0);
        assertEquals(5, item.size());

        assertEquals("11", item.get(0));
        assertEquals("12", item.get(1));
        assertEquals("13", item.get(2));
        assertEquals("14", item.get(3));
        assertEquals("15", item.get(4));

        item = items.get(1);
        assertEquals(5, item.size());

        assertEquals("21", item.get(0));
        assertEquals("22", item.get(1));
        assertEquals("23", item.get(2));
        assertEquals("24", item.get(3));
        assertEquals("25", item.get(4));
     }

    @Test
    public void test_multline_crlf_comma() {
        final Venice venice = new Venice();

        final String script = "(csv/read \"11,12,13,14,15\r\n21,22,23,24,25\")";

        @SuppressWarnings("unchecked")
        final List<List<String>> items = (List<List<String>>)venice.eval(script);

        assertEquals(2, items.size());

        List<String> item = items.get(0);
        assertEquals(5, item.size());

        assertEquals("11", item.get(0));
        assertEquals("12", item.get(1));
        assertEquals("13", item.get(2));
        assertEquals("14", item.get(3));
        assertEquals("15", item.get(4));

        item = items.get(1);
        assertEquals(5, item.size());

        assertEquals("21", item.get(0));
        assertEquals("22", item.get(1));
        assertEquals("23", item.get(2));
        assertEquals("24", item.get(3));
        assertEquals("25", item.get(4));
      }

    @Test
    public void test_multline_crlf_semi_colon() {
        final Venice venice = new Venice();

        final String script = "(csv/read \"11;12;13;14;15\r\n21;22;23;24;25\" :separator \";\")";

        @SuppressWarnings("unchecked")
        final List<List<String>> items = (List<List<String>>)venice.eval(script);

        assertEquals(2, items.size());

        List<String> item = items.get(0);
        assertEquals(5, item.size());

        assertEquals("11", item.get(0));
        assertEquals("12", item.get(1));
        assertEquals("13", item.get(2));
        assertEquals("14", item.get(3));
        assertEquals("15", item.get(4));

        item = items.get(1);
        assertEquals(5, item.size());

        assertEquals("21", item.get(0));
        assertEquals("22", item.get(1));
        assertEquals("23", item.get(2));
        assertEquals("24", item.get(3));
        assertEquals("25", item.get(4));
     }


    @Test
    public void test_multline_crlf_semi_colon_mapping() {
        final Venice venice = new Venice();

        // BUG: Cannot coerce value of type :core/list to function.
        final String script = "(do                                              \n" +
                              "  (-<> \"11;12;13;14;15\r\n21;22;23;24;25\"      \n" +
                              "       (csv/read <> :separator \";\")            \n" +
                              "       (map (fn [x] x) <>)))                     ";

        @SuppressWarnings("unchecked")
        final List<List<String>> items = (List<List<String>>)venice.eval(script);

        assertEquals(2, items.size());

        List<String> item = items.get(0);
        assertEquals(5, item.size());

        assertEquals("11", item.get(0));
        assertEquals("12", item.get(1));
        assertEquals("13", item.get(2));
        assertEquals("14", item.get(3));
        assertEquals("15", item.get(4));

        item = items.get(1);
        assertEquals(5, item.size());

        assertEquals("21", item.get(0));
        assertEquals("22", item.get(1));
        assertEquals("23", item.get(2));
        assertEquals("24", item.get(3));
        assertEquals("25", item.get(4));
     }

}
