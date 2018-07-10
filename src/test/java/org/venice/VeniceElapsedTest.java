/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2018 Venice
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
package org.venice;

import java.util.Map;

import org.junit.Test;
import org.venice.impl.util.StopWatch;
import org.venice.support.AuditEvent;
import org.venice.support.AuditEventType;


public class VeniceElapsedTest {
		
	@Test
	public void evalWithObject() {
		final AuditEvent event = new AuditEvent(
										"jd",
										2000L,
										AuditEventType.INFO,
										"john.doe",
										"login",
										"text");

		final Venice venice = new Venice();
		
		String script = 
				"(or (match (get event :eventName) \"webapp[.](started|stopped)\") " +
				"    (== (get event :eventKey) \"superuser\") " +
				"    (== (get event :eventType) \"ALERT\") " +
				")";       
	       
		// warm up
		for(int ii=0; ii<100; ii++) {
			venice.eval(script, Parameters.of("event", event));
			venice.eval(venice.precompile(script), Parameters.of("event", event));
		}
		
		
		// --------------------------------------------------------
		// not compiled, implicit symbol conversion
		// --------------------------------------------------------

		final StopWatch	sw = StopWatch.millis();	
		for(int ii=0; ii<1000; ii++) {
			venice.eval(script, Parameters.of("event", event));
		}	
		System.out.println("Elapsed (1000 calls): " + sw.stop().toString()); 
	  
		
		
		final PreCompiled compiled = venice.precompile(script);
		
		// --------------------------------------------------------
		// precompiled, implicit symbol conversion
		// --------------------------------------------------------
		sw.start();	
		for(int ii=0; ii<1000; ii++) {
			// implicitly convert AuditEvent symbol (JavaInteropUtil with reflection)
	        venice.eval(compiled, Parameters.of("event", event));
		}	
		System.out.println("Elapsed (precompiled, implicit symbols, 1000 calls): " + sw.stop().toString()); 

		
		// --------------------------------------------------------
		// precompiled, explicit symbol conversion
		// --------------------------------------------------------
		sw.start();	
		for(int ii=0; ii<1000; ii++) {
			// explicitly convert AuditEvent symbol
	        venice.eval(compiled, Parameters.of("event", toMap(event)));
		}	
		System.out.println("Elapsed (precompiled, explicit symbols, 1000 calls): " + sw.stop().toString()); 
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
