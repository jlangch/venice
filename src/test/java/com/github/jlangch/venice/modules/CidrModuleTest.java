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
package com.github.jlangch.venice.modules;

import static org.junit.jupiter.api.Assertions.*;

import java.net.InetAddress;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class CidrModuleTest {

	@Test
	public void test_IP4() {
		final Venice venice = new Venice();

		final String script =
				"(do                                  " +
				"   (load-module :cidr)               " +
				"                                     " +
				"   (cidr/parse \"222.192.0.0/11\"))  ";

		@SuppressWarnings("unchecked")
		final Map<Object,Object> map = (Map<Object,Object>)venice.eval(script);
		
		assertEquals(
				"222.192.0.0/11",  
				(String)map.get("cidr"));
		
		assertEquals(
				"222.192.0.0", 
				((InetAddress)map.get("start-ip")).getHostAddress());
		
		assertEquals(
				"222.223.255.255", 
				((InetAddress)map.get("end-ip")).getHostAddress());
	}

	@Test
	public void test_IP6() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                              " +
				"   (load-module :cidr)                                           " +
				"                                                                 " +
				"   (cidr/parse \"2001:0db8:85a3:08d3:1319:8a2e:0370:7347/64\"))  ";

		@SuppressWarnings("unchecked")
		final Map<Object,Object> map = (Map<Object,Object>)venice.eval(script);
		
		assertEquals(
				"2001:0db8:85a3:08d3:1319:8a2e:0370:7347/64",
				(String)map.get("cidr"));
		
		assertEquals(
				"2001:db8:85a3:8d3:0:0:0:0",
				((InetAddress)map.get("start-ip")).getHostAddress());
		
		assertEquals(
				"2001:db8:85a3:8d3:ffff:ffff:ffff:ffff", 
				((InetAddress)map.get("end-ip")).getHostAddress());
	}

	@Test
	public void test_IP4_in_range() {
		final Venice venice = new Venice();

		final String script_tpl =
				"(do                         " +
				"   (load-module :cidr)      " +
				"                            " +
				"   (cidr/in-range? %s %s))  ";

		assertFalse((Boolean)venice.eval(String.format(script_tpl, "\"100.0.0.0\"",       "(cidr/parse \"222.192.0.0/11\")")));
		assertTrue ((Boolean)venice.eval(String.format(script_tpl, "\"222.192.0.0\"",     "(cidr/parse \"222.192.0.0/11\")")));
		assertTrue ((Boolean)venice.eval(String.format(script_tpl, "\"222.200.0.0\"",     "(cidr/parse \"222.192.0.0/11\")")));
		assertTrue ((Boolean)venice.eval(String.format(script_tpl, "\"222.223.255.255\"", "(cidr/parse \"222.192.0.0/11\")")));
		assertFalse((Boolean)venice.eval(String.format(script_tpl, "\"240.0.0.0\"",       "(cidr/parse \"222.192.0.0/11\")")));

		assertFalse((Boolean)venice.eval(String.format(script_tpl, "\"100.0.0.0\"",       "\"222.192.0.0/11\"")));
		assertTrue ((Boolean)venice.eval(String.format(script_tpl, "\"222.192.0.0\"",     "\"222.192.0.0/11\"")));
		assertTrue ((Boolean)venice.eval(String.format(script_tpl, "\"222.200.0.0\"",     "\"222.192.0.0/11\"")));
		assertTrue ((Boolean)venice.eval(String.format(script_tpl, "\"222.223.255.255\"", "\"222.192.0.0/11\"")));
		assertFalse((Boolean)venice.eval(String.format(script_tpl, "\"240.0.0.0\"",       "\"222.192.0.0/11\"")));
	}

}
