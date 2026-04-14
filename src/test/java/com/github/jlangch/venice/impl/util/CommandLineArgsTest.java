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
package com.github.jlangch.venice.impl.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


public class CommandLineArgsTest {

    @Test
    public void test_1() {
        final CommandLineArgs cli = CommandLineArgs.of("-apple", "-banana");

        assertTrue(cli.switchPresent("-apple"));
        assertTrue(cli.switchPresent("-banana"));
        assertFalse(cli.switchPresent("-cherry"));
    }

    @Test
    public void test_2() {
        final CommandLineArgs cli = CommandLineArgs.of("-file", "a.txt", "-color", "blue");

        assertTrue(cli.switchPresent("-file"));
        assertEquals("a.txt", cli.switchValue("-file"));

        assertTrue(cli.switchPresent("-color"));
        assertEquals("blue", cli.switchValue("-color"));

        assertFalse(cli.switchPresent("-color-unknown"));
        assertEquals(null, cli.switchValue("-color-unknown"));
        assertEquals("green", cli.switchValue("-color-unknown", "green"));
    }

    @Test
    public void test_3() {
        final CommandLineArgs cli = CommandLineArgs.of("-file", "a.txt", "-long", "300");

        assertTrue(cli.switchPresent("-file"));
        assertEquals("a.txt", cli.switchValue("-file"));

        assertTrue(cli.switchPresent("-long"));
        assertEquals(300L, cli.switchLongValue("-long"));

        assertFalse(cli.switchPresent("-long-unknown"));
        assertEquals(null, cli.switchLongValue("-long-unknown"));
        assertEquals(400L, cli.switchLongValue("-long-unknown", 400L));
    }

    @Test
    public void test_4() {
        final CommandLineArgs cli = CommandLineArgs.of("-file", "a.txt", "-double", "1.5");

        assertTrue(cli.switchPresent("-file"));
        assertEquals("a.txt", cli.switchValue("-file"));

        assertTrue(cli.switchPresent("-double"));
        assertEquals(1.5D, cli.switchDoubleValue("-double"));

        assertFalse(cli.switchPresent("-double-unknown"));
        assertEquals(null, cli.switchDoubleValue("-double-unknown"));
        assertEquals(2.5D, cli.switchDoubleValue("-double-unknown", 2.5D));
    }

    @Test
    public void test_targets_1() {
        final CommandLineArgs cli = CommandLineArgs.of("-file", "a.txt", "1");

        assertTrue(cli.switchPresent("-file"));
        assertEquals("a.txt", cli.switchValue("-file"));

        assertArrayEquals(new String[] {"-file", "a.txt", "1"}, cli.args());
        assertArrayEquals(new String[] {"1"}, cli.targets());
    }

    @Test
    public void test_targets_2() {
        final CommandLineArgs cli = CommandLineArgs.of("-file", "a.txt", "1", "2");

        assertTrue(cli.switchPresent("-file"));
        assertEquals("a.txt", cli.switchValue("-file"));

        assertArrayEquals(new String[] {"-file", "a.txt", "1", "2"}, cli.args());
        assertArrayEquals(new String[] {"1", "2"}, cli.targets());
    }

    @Test
    public void test_targets_3() {
        final CommandLineArgs cli = CommandLineArgs.of("-file", "a.txt", "-c", "1", "2");

        assertTrue(cli.switchPresent("-file"));
        assertEquals("a.txt", cli.switchValue("-file"));

        assertTrue(cli.switchPresent("-c"));

        assertArrayEquals(new String[] {"-file", "a.txt", "-c", "1", "2"}, cli.args());
        assertArrayEquals(new String[] {"2"}, cli.targets());
    }

    @Test
    public void test_targets_4() {
        final CommandLineArgs cli = CommandLineArgs.of("-file", "a.txt", "-c", "-d", "1", "2");

        assertEquals("a.txt", cli.switchValue("-file"));
        assertTrue(cli.switchPresent("-c"));
        assertTrue(cli.switchPresent("-d"));
        assertArrayEquals(new String[] {"-file", "a.txt", "-c", "-d", "1", "2"}, cli.args());
        assertArrayEquals(new String[] {"2"}, cli.targets());
    }

    @Test
    public void test_targets_5() {
        final CommandLineArgs cli = CommandLineArgs.of("1", "2", "3");

        assertArrayEquals(new String[] {"1", "2", "3"}, cli.targets());
    }

    @Test
    public void test_removeSwitch_1() {
        final CommandLineArgs cli = CommandLineArgs.of("-file", "a.txt", "-c", "1", "2")
                                                   .removeSwitch("-file");

        assertFalse(cli.switchPresent("-file"));

        assertTrue(cli.switchPresent("-c"));

        assertArrayEquals(new String[] {"-c", "1", "2"}, cli.args());
        assertArrayEquals(new String[] {"2"}, cli.targets());
    }

    @Test
    public void test_removeSwitch_2() {
        final CommandLineArgs cli = CommandLineArgs.of("-file", "a.txt", "-c", "1", "2")
                                                   .removeSwitch("-file")
                                                   .removeSwitch("-c");

        assertFalse(cli.switchPresent("-file"));

        assertFalse(cli.switchPresent("-c"));

        assertArrayEquals(new String[] {"2"}, cli.args());
        assertArrayEquals(new String[] {"2"}, cli.targets());
    }

    @Test
    public void test_removeSwitch_3() {
        final CommandLineArgs cli = CommandLineArgs.of("-repl",
                                                       "-loadpath", ".",
                                                       "-restartable",
                                                       "-colors");

        assertTrue(cli.switchPresent("-repl"));
        assertTrue(cli.switchPresent("-loadpath"));
        assertEquals(".", cli.switchValue("-loadpath"));
        assertTrue(cli.switchPresent("-restartable"));
        assertTrue(cli.switchPresent("-colors"));
        assertArrayEquals(new String[] {}, cli.targets());
        assertArrayEquals(new String[] {"-repl", "-loadpath", ".", "-restartable", "-colors"}, cli.args());


        final CommandLineArgs cli2 = cli.removeSwitch("-repl");

        assertFalse(cli2.switchPresent("-repl"));
        assertTrue(cli2.switchPresent("-loadpath"));
        assertEquals(".", cli2.switchValue("-loadpath"));
        assertTrue(cli2.switchPresent("-restartable"));
        assertTrue(cli2.switchPresent("-colors"));
        assertArrayEquals(new String[] {}, cli2.targets());
        assertArrayEquals(new String[] {"-loadpath", ".", "-restartable", "-colors"}, cli2.args());
   }

    @Test
    public void test_removeSwitch_4() {
        final CommandLineArgs cli = CommandLineArgs.of("-repl",
                                                       "-loadpath", ".",
                                                       "-restartable",
                                                       "-macroexpand", "true",
                                                       "-colors")
                                                   .removeAllSwitches(CollectionUtil.toSet("-loadpath", "-macroexpand"))
                                                   .removeSwitch("-repl");

        assertFalse(cli.switchPresent("-repl"));
        assertFalse(cli.switchPresent("-loadpath"));
        assertFalse(cli.switchPresent("-macroexpand"));
        assertTrue(cli.switchPresent("-restartable"));
        assertTrue(cli.switchPresent("-colors"));
        assertArrayEquals(new String[] {}, cli.targets());
        assertArrayEquals(new String[] {"-restartable", "-colors"}, cli.args());
    }

    @Test
    public void test_removeSwitch_5() {
        final CommandLineArgs cli = CommandLineArgs.of("-script", "(+ 1 2)", "1", "2")
                                                   .removeSwitch("-script");

        assertFalse(cli.switchPresent("-script"));
        assertArrayEquals(new String[] {"1", "2"}, cli.args());
        assertArrayEquals(new String[] {"1", "2"}, cli.targets());
    }

}
