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
package com.github.jlangch.venice.impl.functions;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.javainterop.JavaInterop;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.concurrent.ThreadLocalMap;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.concurrent.ManagedScheduledThreadPoolExecutor;
import com.github.jlangch.venice.javainterop.IInterceptor;


public class ScheduleFunctions {
	
	public static VncFunction schedule_delay = 
		new VncFunction(
				"schedule-delay", 
				VncFunction
					.meta()
					.arglists("(schedule-delay fn delay time-unit)")		
					.doc(
						"Creates and executes a one-shot action that becomes enabled " + 
						"after the given delay. \n" + 
						"Returns a future. (deref f), (future? f), (future-cancel f), " +
						"and (future-done? f) will work on the returned future. \n" + 
						"Time unit is one of :milliseconds, :seconds, :minutes, :hours, or :days. ")
					.examples(
						"(schedule-delay (fn[] (println \"test\")) 1 :seconds)",
						"(deref (schedule-delay (fn [] 100) 2 :seconds))")
					.seeAlso("schedule-at-fixed-rate")
					.build()
		) {		
			public VncVal apply(final VncList args) {	
				ArityExceptions.assertArity(this, args, 3);
	
				sandboxFunctionCallValidation();

				final VncFunction fn = Coerce.toVncFunction(args.first());
				final VncLong delay = Coerce.toVncLong(args.second());
				final VncKeyword unit = Coerce.toVncKeyword(args.third());
	
				final IInterceptor parentInterceptor = JavaInterop.getInterceptor();
				
				// thread local values from the parent thread
				final AtomicReference<Map<VncKeyword,VncVal>> parentThreadLocals = 
						new AtomicReference<>(ThreadLocalMap.getValues());
				
				final Callable<VncVal> taskWrapper = () -> {
					try {
						// inherit thread local values to the child thread
						ThreadLocalMap.setValues(parentThreadLocals.get());
						ThreadLocalMap.clearCallStack();
						JavaInterop.register(parentInterceptor);	
						
						return fn.applyOf();
					}
					finally {
						// clean up
						JavaInterop.unregister();
						ThreadLocalMap.remove();
					}
				};
				
				final ScheduledFuture<VncVal> future = mngdExecutor
														.getExecutor()
														.schedule(
															taskWrapper, 
															delay.getValue(),
															toTimeUnit(unit));
					
				return new VncJavaObject(future);
			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction schedule_at_fixed_rate = 
		new VncFunction(
				"schedule-at-fixed-rate", 
				VncFunction
					.meta()
					.arglists("(schedule-at-fixed-rate fn initial-delay period time-unit)")		
					.doc(
						"Creates and executes a periodic action that becomes enabled first " + 
						"after the given initial delay, and subsequently with the given " + 
						"period. \n" + 
						"Returns a future. (future? f), (future-cancel f), and (future-done? f) " +
						"will work on the returned future. \n" + 
						"Time unit is one of :milliseconds, :seconds, :minutes, :hours, or :days. ")
					.examples(
						"(schedule-at-fixed-rate #(println \"test\") 1 2 :seconds)",
						
						"(let [s (schedule-at-fixed-rate #(println \"test\") 1 2 :seconds)] \n" +
						"   (sleep 16 :seconds) \n" +
						"   (future-cancel s))")
					.seeAlso("schedule-delay")
					.build()
		) {		
			public VncVal apply(final VncList args) {	
				ArityExceptions.assertArity(this, args, 4);

				sandboxFunctionCallValidation();

				final VncFunction fn = Coerce.toVncFunction(args.first());
				final VncLong delay = Coerce.toVncLong(args.second());
				final VncLong period = Coerce.toVncLong(args.third());
				final VncKeyword unit = Coerce.toVncKeyword(args.nth(3));
		
				final IInterceptor parentInterceptor = JavaInterop.getInterceptor();
				
				// thread local values from the parent thread
				final AtomicReference<Map<VncKeyword,VncVal>> parentThreadLocals = 
						new AtomicReference<>(ThreadLocalMap.getValues());
				
				final Runnable taskWrapper = () -> {
					try {
						// inherit thread local values to the child thread
						ThreadLocalMap.setValues(parentThreadLocals.get());
						ThreadLocalMap.clearCallStack();
						JavaInterop.register(parentInterceptor);	
						
						fn.applyOf();
					}
					finally {
						// clean up
						JavaInterop.unregister();
						ThreadLocalMap.remove();
					}
				};
				
				final ScheduledFuture<?> future = mngdExecutor
													.getExecutor()
													.scheduleAtFixedRate(
														taskWrapper, 
														delay.getValue(),
														period.getValue(),
														toTimeUnit(unit));
				
				return new VncJavaObject(future);
			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};

	
	
	private static TimeUnit toTimeUnit(final VncKeyword unit) {
		switch(unit.getValue()) {
			case "milliseconds": return TimeUnit.MILLISECONDS;
			case "seconds": return TimeUnit.SECONDS;
			case "minutes":  return TimeUnit.MINUTES;
			case "hours": return TimeUnit.HOURS;
			case "days": return TimeUnit.DAYS;
			default: throw new VncException("Invalid scheduler time-unit " + unit.getValue());
		}
	}

	///////////////////////////////////////////////////////////////////////////
	// Utils
	///////////////////////////////////////////////////////////////////////////

	public static void shutdown() {
		mngdExecutor.shutdown();
	}


	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap
					.Builder()
					.add(schedule_delay)
					.add(schedule_at_fixed_rate)
					.toMap();	
	
	
	private static ManagedScheduledThreadPoolExecutor mngdExecutor =
		new ManagedScheduledThreadPoolExecutor("venice-scheduler-pool", 4);
}
