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
package com.github.jlangch.venice;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;


public class Recursion_TailPos_Check_Test {

    @Test
    public void test_check_do() {
        final Venice venice = new Venice();

        assertNull(venice.eval("(do (tail-pos))"));
        assertNull(venice.eval("(do 1 (tail-pos))"));
        assertThrows(NotInTailPositionException.class, () -> venice.eval("(do (tail-pos) 1)"));
        assertThrows(NotInTailPositionException.class, () -> venice.eval("(do 1 (tail-pos) 1)"));
    }

    @Test
    public void test_check_if() {
        final Venice venice = new Venice();

        assertNull(venice.eval("(if true (tail-pos) 2)"));
        assertNull(venice.eval("(if false 2 (tail-pos))"));
        assertThrows(NotInTailPositionException.class, () -> venice.eval("(if true (+ 1 (tail-pos)) 2)"));
    }

    @Test
    public void test_check_if_not() {
        final Venice venice = new Venice();

        assertNull(venice.eval("(if-not false (tail-pos) 2)"));
        assertNull(venice.eval("(if-not true 2 (tail-pos))"));
        assertThrows(NotInTailPositionException.class, () -> venice.eval("(if-not false (+ 1 (tail-pos)) 2)"));
    }

    @Test
    public void test_check_if_let() {
        final Venice venice = new Venice();

        assertNull(venice.eval("(if-let [a true] (tail-pos))"));
        assertNull(venice.eval("(if-let [a false] 2 (tail-pos))"));
        assertThrows(NotInTailPositionException.class, () -> venice.eval("(if-let [a true] (+ 1 (tail-pos)) 2)"));
    }

    @Test
    public void test_check_when() {
        final Venice venice = new Venice();

        assertNull(venice.eval("(when true (tail-pos))"));
        assertNull(venice.eval("(when true 2 (tail-pos))"));
        assertThrows(NotInTailPositionException.class, () -> venice.eval("(when true (tail-pos) 2)"));
    }

    @Test
    public void test_check_when_not() {
        final Venice venice = new Venice();

        assertNull(venice.eval("(when-not false (tail-pos))"));
        assertNull(venice.eval("(when-not false 2 (tail-pos))"));
        assertThrows(NotInTailPositionException.class, () -> venice.eval("(when-not false (tail-pos) 2)"));
    }

    @Test
    public void test_check_when_let() {
        final Venice venice = new Venice();

        assertNull(venice.eval("(when-let [a true] (tail-pos))"));
        assertNull(venice.eval("(when-let [a true] 2 (tail-pos))"));
        assertThrows(NotInTailPositionException.class, () -> venice.eval("(when-let [a true] (+ 1 (tail-pos)) 2)"));
    }

    @Test
    public void test_check_let() {
        final Venice venice = new Venice();

        assertNull(venice.eval("(let [a 1] (tail-pos))"));
        assertNull(venice.eval("(let [a 1] 2 (tail-pos))"));
        assertThrows(NotInTailPositionException.class, () -> venice.eval("(let [a 1] (tail-pos) 2)"));
    }

    @Test
    public void test_check_lockig() {
        final Venice venice = new Venice();

        assertNull(venice.eval("(locking 1 (tail-pos))"));
        assertNull(venice.eval("(locking 1 2 (tail-pos))"));
        assertThrows(NotInTailPositionException.class, () -> venice.eval("(locking 1 (tail-pos) 2)"));
    }

    @Test
    public void test_check_try_catch() {
        final Venice venice = new Venice();

        assertNull(venice.eval("(try (tail-pos))"));
        assertNull( venice.eval("(try 1 (tail-pos))"));
        assertThrows(NotInTailPositionException.class, () -> venice.eval("(try (tail-pos) 1)"));
        assertThrows(NotInTailPositionException.class, () -> venice.eval("(try (throw 1) (catch :ValueException ex (tail-pos)))"));
        assertThrows(NotInTailPositionException.class, () -> venice.eval("(try (throw 1) (catch :ValueException ex 1 (tail-pos)))"));
        assertThrows(NotInTailPositionException.class, () -> venice.eval("(try (throw 1) (catch :ValueException ex (tail-pos) 1))"));
        assertThrows(NotInTailPositionException.class, () -> venice.eval("(try 1 (finally (tail-pos)))"));
        assertThrows(NotInTailPositionException.class, () -> venice.eval("(try 1 (finally 1 (tail-pos)))"));
        assertThrows(NotInTailPositionException.class, () -> venice.eval("(try 1 (finally (tail-pos) 1))"));
    }

    @Test
    public void test_check_case() {
        final Venice venice = new Venice();

        assertNull(venice.eval("(case :a :a (tail-pos) :b (tail-pos) (tail-pos))"));
        assertNull(venice.eval("(case :b :a (tail-pos) :b (tail-pos) (tail-pos))"));
        assertNull(venice.eval("(case :c :a (tail-pos) :b (tail-pos) (tail-pos))"));
    }

    @Test
    public void test_check_cond() {
        final Venice venice = new Venice();

        assertNull(venice.eval("(cond true (tail-pos) false (tail-pos) :else (tail-pos))"));
        assertNull(venice.eval("(cond false (tail-pos) true (tail-pos) :else (tail-pos))"));
        assertNull(venice.eval("(cond false (tail-pos) false (tail-pos) :else (tail-pos))"));
    }

    @Test
    public void test_check_or() {
        final Venice venice = new Venice();

        assertNull(venice.eval("(or false (tail-pos))"));
        assertNull(venice.eval("(or false false (tail-pos))"));
    }

    @Test
    public void test_check_and() {
        final Venice venice = new Venice();

        assertNull(venice.eval("(and true (tail-pos))"));
        assertNull(venice.eval("(and true true (tail-pos))"));
    }

    @Test
    public void test_check_defn() {
        final Venice venice = new Venice();

        assertNull(venice.eval("(do (defn foo [] (tail-pos)) (foo))"));
        assertNull(venice.eval("(do (defn foo [] 2 (tail-pos)) (foo))"));
        assertThrows(NotInTailPositionException.class, () -> venice.eval("(do (defn foo [] (tail-pos) 2) (foo))"));
        assertThrows(NotInTailPositionException.class, () -> venice.eval("(do (defn foo [] 2 (tail-pos) 2) (foo))"));
    }

}
