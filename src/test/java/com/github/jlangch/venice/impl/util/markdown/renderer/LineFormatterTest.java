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
package com.github.jlangch.venice.impl.util.markdown.renderer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;


public class LineFormatterTest {

	@Test
	public void test_leftAlign() {
		assertEquals("", LineFormatter.leftAlign((String)null, 0, ' '));
		assertEquals("", LineFormatter.leftAlign("", 0, ' '));
		assertEquals("", LineFormatter.leftAlign("1", 0, ' '));
		assertEquals("", LineFormatter.leftAlign("12", 0, ' '));
		assertEquals("", LineFormatter.leftAlign("123", 0, ' '));
		
		assertEquals(" ", LineFormatter.leftAlign((String)null, 1, ' '));
		assertEquals(" ", LineFormatter.leftAlign("", 1, ' '));
		assertEquals("1", LineFormatter.leftAlign("1", 1, ' '));
		assertEquals("1", LineFormatter.leftAlign("12", 1, ' '));
		assertEquals("1", LineFormatter.leftAlign("123", 1, ' '));
		
		assertEquals("  ", LineFormatter.leftAlign((String)null, 2, ' '));
		assertEquals("  ", LineFormatter.leftAlign("", 2, ' '));
		assertEquals("1 ", LineFormatter.leftAlign("1", 2, ' '));
		assertEquals("12", LineFormatter.leftAlign("12", 2, ' '));
		assertEquals("12", LineFormatter.leftAlign("123", 2, ' '));
		
		assertEquals("   ", LineFormatter.leftAlign((String)null, 3, ' '));
		assertEquals("   ", LineFormatter.leftAlign("", 3, ' '));
		assertEquals("1  ", LineFormatter.leftAlign("1", 3, ' '));
		assertEquals("12 ", LineFormatter.leftAlign("12", 3, ' '));
		assertEquals("123", LineFormatter.leftAlign("123", 3, ' '));
		
		assertEquals("    ", LineFormatter.leftAlign((String)null, 4, ' '));
		assertEquals("    ", LineFormatter.leftAlign("", 4, ' '));
		assertEquals("1   ", LineFormatter.leftAlign("1", 4, ' '));
		assertEquals("12  ", LineFormatter.leftAlign("12", 4, ' '));
		assertEquals("123 ", LineFormatter.leftAlign("123", 4, ' '));
		
		assertEquals("     ", LineFormatter.leftAlign((String)null, 5, ' '));
 		assertEquals("     ", LineFormatter.leftAlign("", 5, ' '));
		assertEquals("1    ", LineFormatter.leftAlign("1", 5, ' '));
		assertEquals("12   ", LineFormatter.leftAlign("12", 5, ' '));
		assertEquals("123  ", LineFormatter.leftAlign("123", 5, ' '));
	}

	@Test
	public void test_rightAlign() {
		assertEquals("", LineFormatter.rightAlign((String)null, 0, ' '));
		assertEquals("", LineFormatter.rightAlign("", 0, ' '));
		assertEquals("", LineFormatter.rightAlign("1", 0, ' '));
		assertEquals("", LineFormatter.rightAlign("12", 0, ' '));
		assertEquals("", LineFormatter.rightAlign("123", 0, ' '));
		
		assertEquals(" ", LineFormatter.rightAlign((String)null, 1, ' '));
		assertEquals(" ", LineFormatter.rightAlign("", 1, ' '));
		assertEquals("1", LineFormatter.rightAlign("1", 1, ' '));
		assertEquals("1", LineFormatter.rightAlign("12", 1, ' '));
		assertEquals("1", LineFormatter.rightAlign("123", 1, ' '));
		
		assertEquals("  ", LineFormatter.rightAlign((String)null, 2, ' '));
		assertEquals("  ", LineFormatter.rightAlign("", 2, ' '));
		assertEquals(" 1", LineFormatter.rightAlign("1", 2, ' '));
		assertEquals("12", LineFormatter.rightAlign("12", 2, ' '));
		assertEquals("12", LineFormatter.rightAlign("123", 2, ' '));
		
		assertEquals("   ", LineFormatter.rightAlign((String)null, 3, ' '));
		assertEquals("   ", LineFormatter.rightAlign("", 3, ' '));
		assertEquals("  1", LineFormatter.rightAlign("1", 3, ' '));
		assertEquals(" 12", LineFormatter.rightAlign("12", 3, ' '));
		assertEquals("123", LineFormatter.rightAlign("123", 3, ' '));
		
		assertEquals("    ", LineFormatter.rightAlign((String)null, 4, ' '));
		assertEquals("    ", LineFormatter.rightAlign("", 4, ' '));
		assertEquals("   1", LineFormatter.rightAlign("1", 4, ' '));
		assertEquals("  12", LineFormatter.rightAlign("12", 4, ' '));
		assertEquals(" 123", LineFormatter.rightAlign("123", 4, ' '));
		
		assertEquals("     ", LineFormatter.rightAlign((String)null, 5, ' '));
		assertEquals("     ", LineFormatter.rightAlign("", 5, ' '));
		assertEquals("    1", LineFormatter.rightAlign("1", 5, ' '));
		assertEquals("   12", LineFormatter.rightAlign("12", 5, ' '));
		assertEquals("  123", LineFormatter.rightAlign("123", 5, ' '));
	}

	@Test
	public void test_centerAlign() {
		assertEquals("", LineFormatter.centerAlign((String)null, 0, ' '));
		assertEquals("", LineFormatter.centerAlign("", 0, ' '));
		assertEquals("", LineFormatter.centerAlign("1", 0, ' '));
		assertEquals("", LineFormatter.centerAlign("12", 0, ' '));
		assertEquals("", LineFormatter.centerAlign("123", 0, ' '));
		
		assertEquals(" ", LineFormatter.centerAlign((String)null, 1, ' '));
		assertEquals(" ", LineFormatter.centerAlign("", 1, ' '));
		assertEquals("1", LineFormatter.centerAlign("1", 1, ' '));
		assertEquals("1", LineFormatter.centerAlign("12", 1, ' '));
		assertEquals("1", LineFormatter.centerAlign("123", 1, ' '));
		
		assertEquals("  ", LineFormatter.centerAlign((String)null, 2, ' '));
		assertEquals("  ", LineFormatter.centerAlign("", 2, ' '));
		assertEquals("1 ", LineFormatter.centerAlign("1", 2, ' '));
		assertEquals("12", LineFormatter.centerAlign("12", 2, ' '));
		assertEquals("12", LineFormatter.centerAlign("123", 2, ' '));
		
		assertEquals("   ", LineFormatter.centerAlign((String)null, 3, ' '));
		assertEquals("   ", LineFormatter.centerAlign("", 3, ' '));
		assertEquals(" 1 ", LineFormatter.centerAlign("1", 3, ' '));
		assertEquals("12 ", LineFormatter.centerAlign("12", 3, ' '));
		assertEquals("123", LineFormatter.centerAlign("123", 3, ' '));
		
		assertEquals("    ", LineFormatter.centerAlign((String)null, 4, ' '));
		assertEquals("    ", LineFormatter.centerAlign("", 4, ' '));
		assertEquals(" 1  ", LineFormatter.centerAlign("1", 4, ' '));
		assertEquals(" 12 ", LineFormatter.centerAlign("12", 4, ' '));
		assertEquals("123 ", LineFormatter.centerAlign("123", 4, ' '));
		
		assertEquals("     ", LineFormatter.centerAlign((String)null, 5, ' '));
		assertEquals("     ", LineFormatter.centerAlign("", 5, ' '));
		assertEquals("  1  ", LineFormatter.centerAlign("1", 5, ' '));
		assertEquals(" 12  ", LineFormatter.centerAlign("12", 5, ' '));
		assertEquals(" 123 ", LineFormatter.centerAlign("123", 5, ' '));
		
		assertEquals("      ", LineFormatter.centerAlign((String)null, 6, ' '));
		assertEquals("      ", LineFormatter.centerAlign("", 6, ' '));
		assertEquals("  1   ", LineFormatter.centerAlign("1", 6, ' '));
		assertEquals("  12  ", LineFormatter.centerAlign("12", 6, ' '));
		assertEquals(" 123  ", LineFormatter.centerAlign("123", 6, ' '));
		
		assertEquals("       ", LineFormatter.centerAlign((String)null, 7, ' '));
		assertEquals("       ", LineFormatter.centerAlign("", 7, ' '));
		assertEquals("   1   ", LineFormatter.centerAlign("1", 7, ' '));
		assertEquals("  12   ", LineFormatter.centerAlign("12", 7, ' '));
		assertEquals("  123  ", LineFormatter.centerAlign("123", 7, ' '));
	}

}
