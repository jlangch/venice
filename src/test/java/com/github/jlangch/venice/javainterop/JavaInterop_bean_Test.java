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
package com.github.jlangch.venice.javainterop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.javainterop.JavaInteropUtil;
import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.support.AuditEvent;
import com.github.jlangch.venice.support.AuditEventType;


public class JavaInterop_bean_Test {


	@Test
	public void test_convert_bean_to_map() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                                      " +
				"   (import :com.github.jlangch.venice.support.User )                     " +
				"   (import :java.time.LocalDate)                                         " +
				"                                                                         " + 
				"   (def john (. :User :new \"john\" 24 (. :LocalDate :of 2018 7 21)))    " +
				"   (hash-map john)                                                       " + 
				")";
		
		@SuppressWarnings("unchecked")
		final Map<Object,Object> map = (Map<Object,Object>)venice.eval(script);
		
		assertEquals("john", map.get("firstname"));
		assertEquals(Integer.valueOf(24), map.get("age"));
		assertEquals(LocalDate.of(2018, 07, 21), map.get("birthday"));
		assertEquals("com.github.jlangch.venice.support.User", map.get("class"));
	}

	@Test
	public void test_bean_map_accessor() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                                      " +
				"   (import :com.github.jlangch.venice.support.User )                     " +
				"   (import :java.time.LocalDate)                                         " +
				"                                                                         " + 
				"   (def john (. :User :new \"john\" 24 (. :LocalDate :of 2018 7 21)))    " +
				"   (:age john)                                                           " + 
				")";
		
		assertEquals(24, venice.eval(script));
	}

	@Test
	public void test_bean_like_access() {
		final AuditEvent event = new AuditEvent(
										"su",
										2000L,
										AuditEventType.ALERT,
										"superuser",
										"webapp.started",
										"text");
		
		try {
			ThreadContext.setInterceptor(new AcceptAllInterceptor());

			final VncVal val = JavaInteropUtil.convertToVncVal(event);
			assertTrue(Types.isVncJavaObject(val));
			
			final VncJavaObject javaObj = (VncJavaObject)val;
			assertEquals("su", ((VncString)javaObj.get(new VncKeyword("principal"))).getValue());
			assertEquals(2000L, ((VncLong)javaObj.get(new VncKeyword("elapsedTimeMillis"))).getValue().longValue());
			assertEquals("ALERT", ((VncString)javaObj.get(new VncKeyword("eventType"))).getValue());
			assertEquals("superuser", ((VncString)javaObj.get(new VncKeyword("eventKey"))).getValue());
			assertEquals("webapp.started", ((VncString)javaObj.get(new VncKeyword("eventName"))).getValue());
			assertEquals("text", ((VncString)javaObj.get(new VncKeyword("eventMessage"))).getValue());
			assertEquals(AuditEvent.class.getName(), ((VncString)javaObj.get(new VncKeyword("class"))).getValue());
			
			final Object obj = val.convertToJavaObject();
			assertTrue(obj instanceof AuditEvent);
		}
		finally {
			ThreadContext.remove();
		}
	}

}