/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2020 Venice
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
package com.github.jlangch.venice.impl.util.cidr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;

import org.junit.jupiter.api.Test;


public class CIDRTest {

	@Test
	public void testIP4() {
		final CIDR cidr = CIDR.parse("192.16.10.0/24");
						
		assertTrue(cidr.isIP4());
		assertEquals("192.16.10.0", cidr.getLowHostAddress());
		assertEquals("192.16.10.255", cidr.getHighHostAddress());
		
		assertFalse(cidr.isInRange("100.16.10.0"));
		assertFalse(cidr.isInRange("192.16.9.254"));
		assertFalse(cidr.isInRange("192.16.9.255"));
		assertTrue(cidr.isInRange("192.16.10.0"));
		assertTrue(cidr.isInRange("192.16.10.10"));
		assertTrue(cidr.isInRange("192.16.10.255"));
		assertFalse(cidr.isInRange("192.16.11.0"));
		assertFalse(cidr.isInRange("192.16.11.1"));
		assertFalse(cidr.isInRange("200.16.10.0"));
	}

	@Test
	public void testToBinaryString() throws Exception{
		assertEquals(
				"00000000 00000000 00000000 00000000", 
				CIDR.toBinaryString(InetAddress.getByName("0.0.0.0"), true));

		assertEquals(
				"00000000 00000000 00000000 00000001", 
				CIDR.toBinaryString(InetAddress.getByName("0.0.0.1"), true));

		assertEquals(
				"11000000 00010000 00001010 00000000", 
				CIDR.toBinaryString(InetAddress.getByName("192.16.10.0"), true));
		
		assertEquals(
				"11111111 11111111 11111111 11111110", 
				CIDR.toBinaryString(InetAddress.getByName("255.255.255.254"), true));
		
		assertEquals(
				"11111111 11111111 11111111 11111111", 
				CIDR.toBinaryString(InetAddress.getByName("255.255.255.255"), true));

	
		assertEquals(
				"00000000000000000000000000000000", 
				CIDR.toBinaryString(InetAddress.getByName("0.0.0.0"), false));

		assertEquals(
				"00000000000000000000000000000001", 
				CIDR.toBinaryString(InetAddress.getByName("0.0.0.1"), false));

		assertEquals(
				"11000000000100000000101000000000", 
				CIDR.toBinaryString(InetAddress.getByName("192.16.10.0"), false));
		
		assertEquals(
				"11111111111111111111111111111110", 
				CIDR.toBinaryString(InetAddress.getByName("255.255.255.254"), false));
		
		assertEquals(
				"11111111111111111111111111111111", 
				CIDR.toBinaryString(InetAddress.getByName("255.255.255.255"), false));
	}

	@Test
	public void testGetLowAddressBit() {
		final CIDR cidr = CIDR.parse("192.16.10.0/24");
			
		assertEquals(24, cidr.getCidrRange());

		final int ipBits = cidr.isIP4() ? 32 : 128;

		final StringBuilder sb = new StringBuilder();
		for(int bit=ipBits-1; bit>=0; bit--) {
			sb.append(cidr.getLowAddressBit(bit) ? "1" : "0");
			if (bit>0 && bit % 8 == 0) sb.append(" ");
		}
		
		assertEquals("11000000 00010000 00001010 00000000", sb.toString());
	}
}
