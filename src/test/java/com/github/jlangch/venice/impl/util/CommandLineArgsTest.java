/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2021 Venice
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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
		
		assertEquals("a.txt", cli.switchValue("-file"));
		assertEquals("blue", cli.switchValue("-color"));
		assertNull(cli.switchValue("-time"));
	}

	@Test
	public void test_3() {
		final CommandLineArgs cli = CommandLineArgs.of("-file", "a.txt", "-long", "300");
		
		assertEquals("a.txt", cli.switchValue("-file"));
		assertEquals(300L, cli.switchLongValue("-long"));
		assertEquals(null, cli.switchLongValue("-long-unknown"));
		assertEquals(400L, cli.switchLongValue("-long-unknown", 400L));
	}

	@Test
	public void test_4() {
		final CommandLineArgs cli = CommandLineArgs.of("-file", "a.txt", "-double", "1.5");
		
		assertEquals("a.txt", cli.switchValue("-file"));
		assertEquals(1.5D, cli.switchDoubleValue("-double"));
		assertEquals(null, cli.switchDoubleValue("-double-unknown"));
		assertEquals(2.5D, cli.switchDoubleValue("-double-unknown", 2.5D));
	}
	
}
