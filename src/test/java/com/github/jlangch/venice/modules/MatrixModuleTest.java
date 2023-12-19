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
package com.github.jlangch.venice.modules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.AssertionException;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;


public class MatrixModuleTest {

	// ------------------------------------------------------------------------
	// validate
	// ------------------------------------------------------------------------

    @Test
    public void test_validate_ok_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                       \n" +
                "   (load-module :matrix)  \n" +
                "   (matrix/validate []))  ";

        venice.eval(script);
    }

    @Test
    public void test_validate_ok_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                         \n" +
                "   (load-module :matrix)    \n" +
                "   (matrix/validate [[]]))  ";

        venice.eval(script);
    }

    @Test
    public void test_validate_ok_3() {
        final Venice venice = new Venice();

        final String script =
                "(do                           \n" +
                "   (load-module :matrix)       \n" +
                "   (matrix/validate [[] []]))  ";

        venice.eval(script);
    }

    @Test
    public void test_validate_ok_4() {
        final Venice venice = new Venice();

        final String script =
                "(do                          \n" +
                "   (load-module :matrix)     \n" +
                "   (matrix/validate [[1]]))  ";

        venice.eval(script);
    }

    @Test
    public void test_validate_ok_5() {
        final Venice venice = new Venice();

        final String script =
                "(do                            \n" +
                "   (load-module :matrix)       \n" +
                "   (matrix/validate [[1 2]]))  ";

        venice.eval(script);
    }

    @Test
    public void test_validate_ok_6() {
        final Venice venice = new Venice();

        final String script =
                "(do                              \n" +
                "   (load-module :matrix)         \n" +
                "   (matrix/validate [[1] [1]]))  ";

        venice.eval(script);
    }

    @Test
    public void test_validate_ok_7() {
        final Venice venice = new Venice();

        final String script =
                "(do                                 \n" +
                "   (load-module :matrix)            \n" +
                "   (matrix/validate [[1 2] [1 2]])) ";

        venice.eval(script);
    }

    @Test
    public void test_validate_fail_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                       \n" +
                "   (load-module :matrix)  \n" +
                "   (matrix/validate nil))  ";

        assertThrows(VncException.class, () -> venice.eval(script));
    }

    @Test
    public void test_validate_fail_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                       \n" +
                "   (load-module :matrix)  \n" +
                "   (matrix/validate 2))  ";

        assertThrows(VncException.class, () -> venice.eval(script));
    }

    @Test
    public void test_validate_fail_3() {
        final Venice venice = new Venice();

        final String script =
                "(do                       \n" +
                "   (load-module :matrix)  \n" +
                "   (matrix/validate [5]))  ";

        assertThrows(VncException.class, () -> venice.eval(script));
    }

    @Test
    public void test_validate_fail_4() {
        final Venice venice = new Venice();

        final String script =
                "(do                       \n" +
                "   (load-module :matrix)  \n" +
                "   (matrix/validate [[] 4))  ";

        assertThrows(VncException.class, () -> venice.eval(script));
    }

    @Test
    public void test_validate_fail_5() {
        final Venice venice = new Venice();

        final String script =
                "(do                       \n" +
                "   (load-module :matrix)  \n" +
                "   (matrix/validate [[5] 4))  ";

        assertThrows(VncException.class, () -> venice.eval(script));
    }

    @Test
    public void test_validate_fail_6() {
        final Venice venice = new Venice();

        final String script =
                "(do                       \n" +
                "   (load-module :matrix)  \n" +
                "   (matrix/validate [[5] [4 5]))  ";

        assertThrows(VncException.class, () -> venice.eval(script));
    }



	// ------------------------------------------------------------------------
	// empty?
	// ------------------------------------------------------------------------

    @Test
    public void test_empty_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                       \n" +
                "   (load-module :matrix)  \n" +
                "   (matrix/empty? []))  ";

        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void test_empty_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                       \n" +
                "   (load-module :matrix)  \n" +
                "   (matrix/empty? [[]]))  ";

        assertFalse((Boolean)venice.eval(script));
    }

    @Test
    public void test_empty_3() {
        final Venice venice = new Venice();

        final String script =
                "(do                       \n" +
                "   (load-module :matrix)  \n" +
                "   (matrix/empty? [[] []]))  ";

        assertFalse((Boolean)venice.eval(script));
    }

    @Test
    public void test_empty_4() {
        final Venice venice = new Venice();

        final String script =
                "(do                       \n" +
                "   (load-module :matrix)  \n" +
                "   (matrix/empty? [[4]]))  ";

        assertFalse((Boolean)venice.eval(script));
    }

    @Test
    public void test_empty_5() {
        final Venice venice = new Venice();

        final String script =
                "(do                       \n" +
                "   (load-module :matrix)  \n" +
                "   (matrix/empty? [[4] [5]]))  ";

        assertFalse((Boolean)venice.eval(script));
    }



	// ------------------------------------------------------------------------
	// rows
	// ------------------------------------------------------------------------

    @Test
    public void test_rows_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                       \n" +
                "   (load-module :matrix)  \n" +
                "   (matrix/rows []))      ";

        assertEquals(0L, venice.eval(script));
    }

    @Test
    public void test_rows_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                       \n" +
                "   (load-module :matrix)  \n" +
                "   (matrix/rows [[1]]))      ";

        assertEquals(1L, venice.eval(script));
    }

    @Test
    public void test_rows_3() {
        final Venice venice = new Venice();

        final String script =
                "(do                       \n" +
                "   (load-module :matrix)  \n" +
                "   (matrix/rows [[1 2]]))      ";

        assertEquals(1L, venice.eval(script));
    }

    @Test
    public void test_rows_4() {
        final Venice venice = new Venice();

        final String script =
                "(do                       \n" +
                "   (load-module :matrix)  \n" +
                "   (matrix/rows [[1] [2]]))      ";

        assertEquals(2L, venice.eval(script));
    }

    @Test
    public void test_rows_5() {
        final Venice venice = new Venice();

        final String script =
                "(do                       \n" +
                "   (load-module :matrix)  \n" +
                "   (matrix/rows [[1 2] [1 2]]))      ";

        assertEquals(2L, venice.eval(script));
    }



	// ------------------------------------------------------------------------
	// columns
	// ------------------------------------------------------------------------

    @Test
    public void test_columns_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                       \n" +
                "   (load-module :matrix)  \n" +
                "   (matrix/columns []))   ";

        assertEquals(0L, venice.eval(script));
    }

    @Test
    public void test_columns_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                       \n" +
                "   (load-module :matrix)  \n" +
                "   (matrix/columns [[1]])) ";

        assertEquals(1L, venice.eval(script));
    }

    @Test
    public void test_columns_3() {
        final Venice venice = new Venice();

        final String script =
                "(do                           \n" +
                "   (load-module :matrix)      \n" +
                "   (matrix/columns [[1 2]]))  ";

        assertEquals(2L, venice.eval(script));
    }

    @Test
    public void test_columns_4() {
        final Venice venice = new Venice();

        final String script =
                "(do                            \n" +
                "   (load-module :matrix)       \n" +
                "   (matrix/columns [[1] [2]])) ";

        assertEquals(1L, venice.eval(script));
    }

    @Test
    public void test_columns_5() {
        final Venice venice = new Venice();

        final String script =
                "(do                                 \n" +
                "   (load-module :matrix)            \n" +
                "   (matrix/columns [[1 2] [1 2]]))  ";

        assertEquals(2L, venice.eval(script));
    }



	// ------------------------------------------------------------------------
	// element
	// ------------------------------------------------------------------------

    @Test
    public void test_element_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                           \n" +
                "   (load-module :matrix)      \n" +
                "   (matrix/element [] 0 0))   ";

        assertThrows(VncException.class, () -> venice.eval(script));
    }

    @Test
    public void test_element_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                     \n" +
                "   (load-module :matrix)                \n" +
                "   (matrix/element [[1 2][3 4]] 4 4))   ";

        assertThrows(VncException.class, () -> venice.eval(script));
    }

    @Test
    public void test_element_3() {
        final Venice venice = new Venice();

        final String script =
                "(do                                     \n" +
                "   (load-module :matrix)                \n" +
                "   (matrix/element [[1 2 3 4]] 0 0))   ";

        assertEquals(1L, venice.eval(script));
    }

    @Test
    public void test_element_4() {
        final Venice venice = new Venice();

        final String script =
                "(do                                     \n" +
                "   (load-module :matrix)                \n" +
                "   (matrix/element [[1][2][3][4]] 3 0))   ";

        assertEquals(4L, venice.eval(script));
    }

    @Test
    public void test_element_5() {
        final Venice venice = new Venice();

        final String script =
                "(do                                     \n" +
                "   (load-module :matrix)                \n" +
                "   (matrix/element [[1 2][3 4]] 1 0))   ";

        assertEquals(3L, venice.eval(script));
    }



	// ------------------------------------------------------------------------
	// assoc-element
	// ------------------------------------------------------------------------

    @Test
    public void test_assoc_element_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                      \n" +
                "   (load-module :matrix)                                 \n" +
                "   (pr-str (matrix/assoc-element [[1 2][3 4]] 0 0 9)))   ";

        assertEquals("[[9 2] [3 4]]", venice.eval(script));
    }

    @Test
    public void test_assoc_element_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                      \n" +
                "   (load-module :matrix)                                 \n" +
                "   (pr-str (matrix/assoc-element [[1 2][3 4]] 1 3 9)))   ";

        assertThrows(AssertionException.class, () -> venice.eval(script));
    }

    @Test
    public void test_assoc_element_3() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                      \n" +
                "   (load-module :matrix)                                 \n" +
                "   (pr-str (matrix/assoc-element [[1 2][3 4]] 6 3 9)))   ";

        assertThrows(AssertionException.class, () -> venice.eval(script));
    }

    @Test
    public void test_assoc_element_4() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                      \n" +
                "   (load-module :matrix)                                 \n" +
                "   (pr-str (matrix/assoc-element [[1 2][3 4]] 1 6 9)))   ";

        assertThrows(AssertionException.class, () -> venice.eval(script));
    }



	// ------------------------------------------------------------------------
	// row
	// ------------------------------------------------------------------------

    @Test
    public void test_row_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                        \n" +
                "   (load-module :matrix)                   \n" +
                "   (pr-str (matrix/row [[1 2][3 4]] 0)))   ";

        assertEquals("[1 2]", venice.eval(script));
    }

    @Test
    public void test_row_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                        \n" +
                "   (load-module :matrix)                   \n" +
                "   (pr-str (matrix/row [[1 2][3 4]] 1)))   ";

        assertEquals("[3 4]", venice.eval(script));
    }

    @Test
    public void test_row_3() {
        final Venice venice = new Venice();

        final String script =
                "(do                                        \n" +
                "   (load-module :matrix)                   \n" +
                "   (pr-str (matrix/row [[1 2][3 4]] 2)))   ";

        assertThrows(AssertionException.class, () -> venice.eval(script));
    }



	// ------------------------------------------------------------------------
	// column
	// ------------------------------------------------------------------------

    @Test
    public void test_column_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                \n" +
                "   (load-module :matrix)                           \n" +
                "   (pr-str (matrix/column [[1 2][3 4][5 6]] 0)))   ";

        assertEquals("[1 3 5]", venice.eval(script));
    }

    @Test
    public void test_column_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                \n" +
                "   (load-module :matrix)                           \n" +
                "   (pr-str (matrix/column [[1 2][3 4][5 6]] 1)))   ";

        assertEquals("[2 4 6]", venice.eval(script));
    }

    @Test
    public void test_column_3() {
        final Venice venice = new Venice();

        final String script =
                "(do                                        \n" +
                "   (load-module :matrix)                   \n" +
                "   (pr-str (matrix/row [[1 2][3 4]] 2)))   ";

        assertThrows(AssertionException.class, () -> venice.eval(script));
    }

}
