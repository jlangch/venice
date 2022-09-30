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
package com.github.jlangch.venice.macros;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.AssertionException;
import com.github.jlangch.venice.Venice;


public class AssertMacroTest {

    @Test
    public void test_assert_OK() {
        final Venice venice = new Venice();

        try {
            venice.eval("(assert true)");
        }
        catch(AssertionException ex) {
             fail("Unexpected AssertionException");
        }

        try {
            venice.eval("(assert true \"error\")");
        }
        catch(AssertionException ex) {
             fail("Unexpected AssertionException");
        }
    }

    @Test
    public void test_assert_FAILED_expr_false() {
        final Venice venice = new Venice();

        try {
            venice.eval("(assert false)");
            fail("Expected AssertionException");
        }
        catch(AssertionException ex) {
            assertEquals(
                    "Assert failed.\n"
                    + "Expression:\n"
                    + "false",
                    ex.getMessage());
        }

        try {
            venice.eval("(assert false \"error\")");
            fail("Expected AssertionException");
        }
        catch(AssertionException ex) {
            assertEquals(
                    "Assert failed.\n"
                    + "error\n"
                    + "Expression:\n"
                    + "false",
                    ex.getMessage());
        }
    }

    @Test
    public void test_assert_FAILED_expr_nil() {
        final Venice venice = new Venice();

        try {
            venice.eval("(assert nil)");
            fail("Expected AssertionException");
        }
        catch(AssertionException ex) {
            assertEquals(
                    "Assert failed.\n"
                    + "Expression:\n"
                    + "nil",
                    ex.getMessage());
        }

        try {
            venice.eval("(assert nil \"error\")");
            fail("Expected AssertionException");
        }
        catch(AssertionException ex) {
            assertEquals(
                    "Assert failed.\n"
                    + "error\n"
                    + "Expression:\n"
                    + "nil",
                    ex.getMessage());
        }
    }

    @Test
    public void test_assert_FAILED_exception() {
        final Venice venice = new Venice();

        try {
            venice.eval("(assert (/ 1 0))");
            fail("Expected AssertionException");
        }
        catch(AssertionException ex) {
            assertEquals(
                    "Assert failed.\n"
                    + "Unexpected exception: :com.github.jlangch.venice.VncException\n"
                    + "Expression:\n"
                    + "(/ 1 0)",
                    ex.getMessage());
        }

        try {
            venice.eval("(assert (/ 1 0) \"error\")");
            fail("Expected AssertionException");
        }
        catch(AssertionException ex) {
            assertEquals(
                    "Assert failed.\n"
                    + "error\n"
                    + "Unexpected exception: :com.github.jlangch.venice.VncException\n"
                    + "Expression:\n"
                    + "(/ 1 0)",
                    ex.getMessage());
        }
    }

    @Test
    public void test_assert_eq_OK() {
        final Venice venice = new Venice();

        try {
            venice.eval("(assert-eq 1 1)");
        }
        catch(AssertionException ex) {
             fail("Unexpected AssertionException");
        }

        try {
            venice.eval("(assert-eq 1 1\"error\")");
        }
        catch(AssertionException ex) {
             fail("Unexpected AssertionException");
        }
    }

    @Test
    public void test_assert_eq_FAILED() {
        final Venice venice = new Venice();

        try {
            venice.eval("(assert-eq 1 2)");
            fail("Expected AssertionException");
        }
        catch(AssertionException ex) {
            assertEquals(
                "Assert failed.\n"
                + "Expected: 1\n"
                + "Actual:   2\n"
                + "Expression:\n"
                + "2",
                ex.getMessage());
        }

        try {
            venice.eval("(assert-eq 1 2 \"error\")");
            fail("Expected AssertionException");
        }
        catch(AssertionException ex) {
            assertEquals(
                    "Assert failed.\n"
                    + "error\n"
                    + "Expected: 1\n"
                    + "Actual:   2\n"
                    + "Expression:\n"
                    + "2",
                    ex.getMessage());
        }
    }

    @Test
    public void test_assert_eq_FAILED_exception() {
        final Venice venice = new Venice();

        try {
            venice.eval("(assert-eq 1 (/ 1 0))");
            fail("Expected AssertionException");
        }
        catch(AssertionException ex) {
            assertEquals(
                "Assert failed.\n"
                + "Unexpected exception: :com.github.jlangch.venice.VncException\n"
                + "Expression:\n"
                + "(/ 1 0)",
                ex.getMessage());
        }

        try {
            venice.eval("(assert-eq 1 (/ 1 0) \"error\")");
            fail("Expected AssertionException");
        }
        catch(AssertionException ex) {
            assertEquals(
                    "Assert failed.\n"
                    + "error\n"
                    + "Unexpected exception: :com.github.jlangch.venice.VncException\n"
                    + "Expression:\n"
                    + "(/ 1 0)",
                    ex.getMessage());
        }
    }

    @Test
    public void test_assert_throws_OK() {
        final Venice venice = new Venice();

        try {
            venice.eval(
                    "(do                                                               \n" +
                    "   (import :com.github.jlangch.venice.util.TestException)         \n" +
                    "                                                                  \n" +
                    "   ;; Venice converts RuntimeException -> VncException            \n" +
                    "   (assert-throws :VncException (ex :TestException)))             ");
        }
        catch(Exception ex) {
            fail("Unexpected Exception " + ex.getClass().getName());
        }

        try {
            venice.eval(
                    "(do                                                               \n" +
                    "   (import :com.github.jlangch.venice.util.TestException)         \n" +
                    "                                                                  \n" +
                    "   ;; Venice converts RuntimeException -> VncException            \n" +
                    "   (assert-throws :VncException (ex :TestException) \"error\"))   ");
        }
        catch(Exception ex) {
            fail("Unexpected Exception " + ex.getClass().getName());
        }


        try {
            venice.eval(
                    "(do                                                               \n" +
                    "   (import :com.github.jlangch.venice.AssertionException)         \n" +
                    "                                                                  \n" +
                    "   (assert-throws :AssertionException (assert false)))            ");
        }
        catch(Exception ex) {
            fail("Unexpected Exception " + ex.getClass().getName());
        }

        try {
            venice.eval(
                    "(do                                                               \n" +
                    "   (import :com.github.jlangch.venice.AssertionException)         \n" +
                    "                                                                  \n" +
                    "   (assert-throws :AssertionException (assert false) \"error\"))  ");
        }
        catch(Exception ex) {
            fail("Unexpected Exception " + ex.getClass().getName());
        }


        try {
            venice.eval("(assert-throws :ValueException (throw 1))");
        }
        catch(Exception ex) {
            fail("Unexpected Exception " + ex.getClass().getName());
        }

        try {
            venice.eval("(assert-throws :ValueException (throw 1) \"error\")");
        }
        catch(Exception ex) {
            fail("Unexpected Exception " + ex.getClass().getName());
        }
    }

    @Test
    public void test_assert_throws_FAILED() {
        final Venice venice = new Venice();


        try {
            venice.eval("(assert-throws :VncException (/ 2 1))");
            fail("Expected AssertionException");
        }
        catch(AssertionException ex) {
            assertEquals(
                    "Assert failed.\n"
                    + "Expected: :VncException\n"
                    + "But no exception has been thrown!\n"
                    + "Expression:\n"
                    + "(/ 2 1)",
                    ex.getMessage());
        }
        catch(Exception ex) {
            fail("Unexpected Exception " + ex.getClass().getName());
        }

        try {
            venice.eval("(assert-throws :VncException (/ 2 1) \"error\")");
            fail("Expected AssertionException");
        }
        catch(AssertionException ex) {
            assertEquals(
                    "Assert failed.\n"
                    + "error\n"
                    + "No exception thrown\n"
                    + "Expression:\n"
                    + "(/ 2 1)",
                    ex.getMessage());
        }
        catch(Exception ex) {
            fail("Unexpected Exception " + ex.getClass().getName());
        }
    }

    @Test
    public void test_assert_throws_OK_exception() {
        final Venice venice = new Venice();


        try {
            venice.eval("(assert-throws :VncException (/ 2 0) \"error\")");
        }
        catch(Exception ex) {
            fail("Unexpected Exception " + ex.getClass().getName());
        }
    }
}
