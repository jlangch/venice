/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2019 Venice
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

import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.util.StopWatch;
import com.github.jlangch.venice.support.AuditEvent;
import com.github.jlangch.venice.support.AuditEventType;


public class VeniceElapsedTest {

	@BeforeAll
	public static void test() {
		System.out.println("Performance tests (VeniceElapsedTest):");
	}

	@Test
	public void evalWithObject() {
		final AuditEvent event = new AuditEvent(
										"jd",
										2000L,
										AuditEventType.ALERT,
										"john.doe",
										"login",
										"text");

		final Venice venice = new Venice();
		
		final String script1 = 
				"(or (match (get event :eventName) \"webapp[.](started|stopped)\") " +
				"    (== (get event :eventKey) \"superuser\") " +
				"    (== (get event :eventType) \"ALERT\") " +
				")";       

		final String script2 = 
				"(or (match eventName \"webapp[.](started|stopped)\") " +
				"    (== eventKey \"superuser\") " +
				"    (== eventType \"ALERT\") " +
				")";       

		final PreCompiled compiled1 = venice.precompile("script1", script1);
		final PreCompiled compiled2 = venice.precompile("script2", script2);
		
		// --------------------------------------------------------
		// Java
		// --------------------------------------------------------
		// JIT warm.up
		@SuppressWarnings("unused")
		int result = 0;
		for(int ii=0; ii<12000; ii++) {
			boolean res = event.getEventName().matches("webapp[.](started|stopped)")
							|| event.getEventKey().equals("superuser")
							|| event.getEventType() == AuditEventType.ALERT;
			result += res ? 0 : 1;
		}	
		final StopWatch	sw = StopWatch.millis();
		for(int ii=0; ii<1000; ii++) {
			boolean res = event.getEventName().matches("webapp[.](started|stopped)")
							|| event.getEventKey().equals("superuser")
							|| event.getEventType() == AuditEventType.ALERT;
			result += res ? 0 : 1;
		}	
		System.out.println("Elapsed (Java reference, 1000 calls): " + sw.stop().toString()); 

		
		// --------------------------------------------------------
		// not compiled, implicit symbol conversion
		// --------------------------------------------------------
		// JIT warm.up
		for(int ii=0; ii<12000; ii++) {
			venice.eval(script1, Parameters.of("event", event));
		}	
		sw.start();	
		for(int ii=0; ii<1000; ii++) {
			venice.eval(script1, Parameters.of("event", event));
		}	
		System.out.println("Elapsed (1000 calls): " + sw.stop().toString()); 
	  
		
				
		// --------------------------------------------------------
		// precompiled, implicit symbol conversion
		// --------------------------------------------------------
		// JIT warm.up
		for(int ii=0; ii<12000; ii++) {
	        venice.eval(compiled1, Parameters.of("event", event));
		}	
		sw.start();	
		for(int ii=0; ii<1000; ii++) {
			// implicitly convert AuditEvent symbol (JavaInteropUtil with reflection)
	        venice.eval(compiled1, Parameters.of("event", event));
		}	
		System.out.println("Elapsed (precompiled, implicit params, 1000 calls): " + sw.stop().toString()); 

		
		// --------------------------------------------------------
		// precompiled, explicit symbol conversion
		// --------------------------------------------------------
		// JIT warm.up
		for(int ii=0; ii<12000; ii++) {
	        venice.eval(compiled1, Parameters.of("event", toMap(event)));
		}	
		sw.start();	
		for(int ii=0; ii<1000; ii++) {
			// explicitly convert AuditEvent symbol
	        venice.eval(compiled1, Parameters.of("event", toMap(event)));
		}	
		System.out.println("Elapsed (precompiled, explicit params, 1000 calls): " + sw.stop().toString()); 

		
		// --------------------------------------------------------
		// precompiled, explicit symbol conversion
		// --------------------------------------------------------
		// JIT warm.up
		for(int ii=0; ii<12000; ii++) {
	        venice.eval(compiled2, 
	        		Parameters.of(
	        				"eventName", event.getEventName(),
	        				"eventType", event.getEventType(),
	        				"eventKey", event.getEventKey()));
		}	
		sw.start();	
		for(int ii=0; ii<1000; ii++) {
			// explicitly convert AuditEvent symbol
	        venice.eval(compiled2, 
	        		Parameters.of(
	        				"eventName", event.getEventName(),
	        				"eventType", event.getEventType(),
	        				"eventKey", event.getEventKey()));
		}	
		System.out.println("Elapsed (precompiled, simple params, 1000 calls): " + sw.stop().toString()); 
	}
	
	
	private Map<String,Object> toMap(final AuditEvent event) {
		return Parameters.of(
				"principal", 		 event.getPrincipal(),
				"elapsedTimeMillis", event.getElapsedTimeMillis(),
				"eventType", 		 event.getEventType(),
				"eventKey",  		 event.getEventKey(),
				"eventName", 		 event.getEventName(),
				"eventMessage", 	 event.getEventMessage());
	}
	
}
