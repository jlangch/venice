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
package com.github.jlangch.venice.impl.functions;

import static com.github.jlangch.venice.impl.functions.FunctionsUtil.assertArity;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.javainterop.DynamicInvocationHandler;
import com.github.jlangch.venice.impl.javainterop.JavaInterop;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncTunnelAsJavaObject;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.concurrent.ThreadLocalMap;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.util.ThreadPoolUtil;
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
					.build()
		) {		
			@SuppressWarnings("unchecked")
			public VncVal apply(final VncList args) {	
				assertArity("schedule-delay", args, 3);
	
				JavaInterop.getInterceptor().validateVeniceFunction("schedule-delay");

				final VncFunction fn = Coerce.toVncFunction(args.first());
				final VncLong delay = Coerce.toVncLong(args.second());
				final VncKeyword unit = Coerce.toVncKeyword(args.third());
	
				// wrap the passed function so that its return value can be
				// wrapped with a VncTunnelAsJavaObject. So that there are no 
				// VncVal -> Java Object conversions. Thus
				// the function's return value is not touched (just 
				// wrapped/unwrapped with a VncTunnelAsJavaObject)!			
				final VncFunction wrapped = new VncFunction(fn.getQualifiedName(), fn.getMeta()) {
					public VncVal apply(final VncList args) {
						return new VncTunnelAsJavaObject(fn.apply(args));
					}
					
					private static final long serialVersionUID = -1L;
				};
				
				final Callable<VncVal> task = (Callable<VncVal>)DynamicInvocationHandler.proxify(
													ThreadLocalMap.getCallStack().peek(),
													Callable.class, 
													VncHashMap.of(new VncKeyword("call"), wrapped));
	
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
						
						return task.call();
					}
					finally {
						// clean up
						JavaInterop.unregister();
						ThreadLocalMap.remove();
					}
				};
				
				final ScheduledFuture<VncVal> future = getExecutor().schedule(
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
						"(schedule-at-fixed-rate (fn[] (println \"test\")) 1 2 :seconds)",
						
						"(let [s (schedule-at-fixed-rate (fn[] (println \"test\")) 1 2 :seconds)] \n" +
						"   (sleep 16 :seconds) \n" +
						"   (future-cancel s))")
					.build()
		) {		
			public VncVal apply(final VncList args) {	
				assertArity("schedule-at-fixed-rate", args, 4);

				JavaInterop.getInterceptor().validateVeniceFunction("schedule-at-fixed-rate");

				final VncFunction fn = Coerce.toVncFunction(args.first());
				final VncLong delay = Coerce.toVncLong(args.second());
				final VncLong period = Coerce.toVncLong(args.third());
				final VncKeyword unit = Coerce.toVncKeyword(args.nth(3));
	
				// wrap the passed function so that its return value can be
				// wrapped with a VncTunnelAsJavaObject. So that there are no 
				// VncVal -> Java Object conversions. Thus
				// the function's return value is not touched (just 
				// wrapped/unwrapped with a VncTunnelAsJavaObject)!			
				final VncFunction wrapped = new VncFunction(fn.getQualifiedName(), fn.getMeta()) {
					public VncVal apply(final VncList args) {
						return new VncTunnelAsJavaObject(fn.apply(args));
					}
					
					private static final long serialVersionUID = -1L;
				};
				
				final Runnable task = (Runnable)DynamicInvocationHandler.proxify(
													ThreadLocalMap.getCallStack().peek(),
													Runnable.class, 
													VncHashMap.of(new VncKeyword("run"), wrapped));
	
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
						
						task.run();
					}
					finally {
						// clean up
						JavaInterop.unregister();
						ThreadLocalMap.remove();
					}
				};
				
				final ScheduledFuture<?> future = getExecutor().scheduleAtFixedRate(
														taskWrapper, 
														delay.getValue(),
														period.getValue(),
														toTimeUnit(unit));
				
				return new VncJavaObject(future);
			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};


	public static void shutdown() {
		synchronized(threadPoolCounter) {
			if (executor != null) {
				executor.shutdown();
			}
		}
	}

	public static void shutdownNow() {
		synchronized(threadPoolCounter) {
			if (executor != null) {
				executor.shutdownNow();
			}
		}
	}
	
	
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


	private static ScheduledExecutorService getExecutor() {
		synchronized(threadPoolCounter) {
			if (executor == null) {
				executor = createExecutor();				
			}
			return executor;
		}
	}

	
	private static ScheduledExecutorService createExecutor() {
		return Executors.newScheduledThreadPool(
						4,
						ThreadPoolUtil.createThreadFactory(
								"venice-scheduler-pool-%d", 
								threadPoolCounter,
								true /* daemon threads */));
		
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
	
	
	private final static AtomicLong threadPoolCounter = new AtomicLong(0);

	private static ScheduledExecutorService executor = null;
}
