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
package com.github.jlangch.venice.impl.functions;

import static com.github.jlangch.venice.impl.functions.FunctionsUtil.assertArity;
import static com.github.jlangch.venice.impl.functions.FunctionsUtil.assertMinArity;
import static com.github.jlangch.venice.impl.types.Constants.False;
import static com.github.jlangch.venice.impl.types.Constants.Nil;
import static com.github.jlangch.venice.impl.types.Constants.True;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.javainterop.DynamicInvocationHandler;
import com.github.jlangch.venice.impl.javainterop.JavaInterop;
import com.github.jlangch.venice.impl.javainterop.JavaInteropUtil;
import com.github.jlangch.venice.impl.types.Coerce;
import com.github.jlangch.venice.impl.types.Types;
import com.github.jlangch.venice.impl.types.VncAtom;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncThreadLocal;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncJavaObject;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.util.ThreadLocalMap;
import com.github.jlangch.venice.impl.util.ThreadPoolUtil;
import com.github.jlangch.venice.javainterop.IInterceptor;


public class ConcurrencyFunctions {

	///////////////////////////////////////////////////////////////////////////
	// DEREF
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction deref = new VncFunction("deref") {
		{
			setArgLists("(deref ref)", "(deref ref timeout-ms timeout-val)");
			
			setDoc("Dereferences an atom or a Future object. When applied to an " + 
					"atom, returns its current state. When applied to a future, " +
					"will block if computation not complete. The variant taking a " +
					"timeout can be used for futures and will return timeout-val " + 
					"if the timeout (in milliseconds) is reached before a value " + 
					"is available. \n" +
					"Also reader macro: @atom/@future/@promise.");
			
			
			setExamples(
					"(do                             \n" +
					"   (def counter (atom 0))       \n" +
					"   (deref counter))               ",

					"(do                             \n" +
					"   (def counter (atom 0))       \n" +
					"   @counter)                      ",

					"(do                             \n" +
					"   (def task (fn [] 100))       \n" +
					"   (let [f (future task)]       \n" +
					"        (deref f)))               ",

					"(do                             \n" +
					"   (def task (fn [] 100))       \n" +
					"   (let [f (future task)]       \n" +
					"        @f))                      ",

					"(do                             \n" +
					"   (def task (fn [] 100))       \n" +
					"   (let [f (future task)]       \n" +
					"        (deref f 300 :timeout)))  ",

					"(do                                            \n" +
					"   (def my-delay                               \n" +
					"        (delay (println \"working...\") 100))  \n" +
					"   @my-delay)                                    ");
		}

		public VncVal apply(final VncList args) {
			assertArity("deref", args, 1, 3);
			
			if (Types.isVncAtom(args.first())) {
				final VncAtom atm = (VncAtom)args.first();
				return atm.deref();
			}
			else if (Types.isVncJavaObject(args.first())) {
				final Object delegate = ((VncJavaObject)args.first()).getDelegate();
				if (delegate instanceof Future) {
					try {
						@SuppressWarnings("unchecked")
						final Future<VncVal> future = (Future<VncVal>)((VncJavaObject)args.first()).getDelegate();
						if (args.size() == 1) {
							return JavaInteropUtil.convertToVncVal(future.get());
						}
						else {
							final long timeout = Coerce.toVncLong(args.nth(1)).getValue();
							try {
								return JavaInteropUtil.convertToVncVal(future.get(timeout, TimeUnit.MILLISECONDS));
							}
							catch(TimeoutException ex) {
								return args.nth(2);
							}
						}
					}
					catch(ExecutionException ex) {
						if (ex.getCause() != null && (ex.getCause() instanceof SecurityException)) {
							throw (SecurityException)ex.getCause();
						}
					}
					catch(Exception ex) {
						throw new VncException("Failed to deref future", ex);
					}
				}
				else if (delegate instanceof Delay) {
					return ((Delay)delegate).deref();
				}
			}

			throw new VncException(String.format(
					"Function 'deref' does not allow type %s as parameter.",
					Types.getClassName(args.first())));
		}
	
	    private static final long serialVersionUID = -1848883965231344442L;
	};

	
	
	///////////////////////////////////////////////////////////////////////////
	// Atom
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction new_atom = new VncFunction("atom") {
		{
			setArgLists("(atom x)");
			
			setDoc("Creates an atom with the initial value x");
			
			setExamples("(do\n   (def counter (atom 0))\n   (deref counter))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("atom", args, 1);
			
			return new VncAtom(args.nth(0));
		}
		
	    private static final long serialVersionUID = -1848883965231344442L;
	};

	public static VncFunction atom_Q = new VncFunction("atom?") {
		{
			setArgLists("(atom? x)");
			
			setDoc("Returns true if x is an atom, otherwise false");
			
			setExamples("(do\n   (def counter (atom 0))\n   (atom? counter))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("atom?", args, 1);
			
			return Types.isVncAtom(args.nth(0)) ? True : False;
		}
		
	    private static final long serialVersionUID = -1848883965231344442L;
	};

	
	public static VncFunction reset_BANG = new VncFunction("reset!") {
		{
			setArgLists("(reset! atom newval)");
			
			setDoc( "Sets the value of atom to newval without regard for the " + 
					"current value. Returns newval.");
			
			setExamples("(do\n   (def counter (atom 0))\n   (reset! counter 99)\n   (deref counter))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("reset!", args, 2);
			
			final VncAtom atm = Coerce.toVncAtom(args.nth(0));
			return atm.reset(args.nth(1));
		}
		
	    private static final long serialVersionUID = -1848883965231344442L;
	};

	public static VncFunction swap_BANG = new VncFunction("swap!") {
		{
			setArgLists("(swap! atom f & args)");
			
			setDoc( "Atomically swaps the value of atom to be: " + 
					"(apply f current-value-of-atom args). Note that f may be called " + 
					"multiple times, and thus should be free of side effects.  Returns " + 
					"the value that was swapped in.");
			
			setExamples("(do\n   (def counter (atom 0))\n   (swap! counter inc)\n   (deref counter))");
		}
		
		public VncVal apply(final VncList args) {
			assertMinArity("swap!", args, 2);
			
			final VncAtom atm = Coerce.toVncAtom(args.nth(0));		
			final VncFunction fn = Coerce.toVncFunction(args.nth(1));
			final VncList swapArgs = args.slice(2);
			
			return atm.swap(fn, swapArgs);
		}
		
	    private static final long serialVersionUID = -1848883965231344442L;
	};

	public static VncFunction compare_and_set_BANG = new VncFunction("compare-and-set!") {
		{
			setArgLists("(compare-and-set! atom oldval newval)");
			
			setDoc( "Atomically sets the value of atom to newval if and only if the " + 
					"current value of the atom is identical to oldval. Returns true if " + 
					"set happened, else false");
			
			setExamples("(do\n   (def counter (atom 2))\n   (compare-and-set! counter 2 4)\n   (deref counter))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("compare-and-set!", args, 3);
			
			final VncAtom atm = Coerce.toVncAtom(args.nth(0));		
			
			return atm.compare_and_set(args.nth(1), args.nth(2));
		}
		
	    private static final long serialVersionUID = -1848883965231344442L;
	};


	///////////////////////////////////////////////////////////////////////////
	// Promises
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction deliver = new VncFunction("deliver") {
		{
			setArgLists("(deliver ref value)");
			
			setDoc("Delivers the supplied value to the promise, releasing any pending " + 
				   "derefs. A subsequent call to deliver on a promise will have no effect.");
			
			
			setExamples(
					"(do                   \n" +
					"   (def p (promise))  \n" +
					"   (deliver p 123))");
		}

		@SuppressWarnings("unchecked")
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().validateBlackListedVeniceFunction("deliver");

			assertArity("deliver", args, 2);
			
			final Object promise = Coerce.toVncJavaObject(args.first()).getDelegate();
			final VncVal value = args.second();
			
			if (promise instanceof CompletableFuture) {
				((CompletableFuture<VncVal>)promise).complete(value);
				return Nil;
			}
			else {
				throw new VncException(String.format(
						"Function 'deliver' does not allow type %s as parameter",
						Types.getClassName(args.first())));
			}
		}
		
	    private static final long serialVersionUID = -1848883965231344442L;
	};

	public static VncFunction promise = new VncFunction("promise") {
		{
			setArgLists("(promise)");
			
			setDoc( "Returns a promise object that can be read with deref, and set, " + 
					"once only, with deliver. Calls to deref prior to delivery will " + 
					"block, unless the variant of deref with timeout is used. All " + 
					"subsequent derefs will return the same delivered value without " + 
					"blocking.");
			
			setExamples(
					"(do                                        \n" +
					"   (def p (promise))                       \n" +
					"   (def task (fn []                        \n" +
					"                 (do                       \n" +
					"                    (sleep 500)            \n" +
					"                    (deliver p 123))))     \n" +
					"                                           \n" +
					"   (future task)                           \n" +
					"   (deref p))");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().validateBlackListedVeniceFunction("promise");

			assertArity("promise", args, 0);

			return new VncJavaObject(new CompletableFuture<VncVal>());
		}
		
	    private static final long serialVersionUID = -1848883965231344442L;
	};

	public static VncFunction promise_Q = new VncFunction("promise?") {
		{
			setArgLists("(promise? p)");
			
			setDoc("Returns true if f is a Promise otherwise false");
			
			setExamples("(promise? (promise)))");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().validateBlackListedVeniceFunction("promise?");

			assertArity("promise?", args, 1);

			return Types.isVncJavaObject(args.first(), CompletableFuture.class) ? True : False;
		}
		
	    private static final long serialVersionUID = -1848883965231344442L;
	};

	

	///////////////////////////////////////////////////////////////////////////
	// Futures
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction future = new VncFunction("future") {
		{
			setArgLists("(future fn)");
			
			setDoc( "Takes a function and yields a future object that will " + 
					"invoke the function in another thread, and will cache the result and " + 
					"return it on all subsequent calls to deref. If the computation has " + 
					"not yet finished, calls to deref will block, unless the variant of " + 
					"deref with timeout is used.");
			
			setExamples(
					"(do                                         \n" + 
					"   (def wait (fn [] (do (sleep 500) 100)))  \n" + 
					"                                            \n" + 
					"   (let [f (future wait)]                   \n" + 
					"        (deref f))                          \n" + 
					")");
		}
		
		@SuppressWarnings("unchecked")
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().validateBlackListedVeniceFunction("future");

			assertArity("future", args, 1);
			
			final VncFunction fn = Coerce.toVncFunction(args.first());

			// wrap the passed function so that its return value can be
			// wrapped with a VncJavaObject. So that there are no 
			// VncVal -> Java Object conversions. Thus
			// the function's return value is not touched (just 
			// wrapped/unwrapped with a VncJavaObject)!			
			final VncFunction wrapped = new VncFunction() {
				public VncVal apply(final VncList args) {
					return new VncJavaObject(fn.apply(args));
				}
				
			    private static final long serialVersionUID = -1L;
			};

			final Callable<VncVal> task = (Callable<VncVal>)DynamicInvocationHandler.proxify(
												Callable.class, 
												new VncHashMap(new VncKeyword("call"), wrapped));

			final IInterceptor parentInterceptor = JavaInterop.getInterceptor();
			
			final Callable<Object> taskWrapper = () -> {
				try {
					ThreadLocalMap.clearCallStack();
					JavaInterop.register(parentInterceptor);	
					
					return task.call();
				}
				finally {
					// clean up
					ThreadLocalMap.remove();
					JavaInterop.unregister();
				}
			};
			
			final Future<Object> future = executor.submit(taskWrapper);
			
			return new VncJavaObject(future);
		}
		
	    private static final long serialVersionUID = -1848883965231344442L;
	};

	public static VncFunction future_Q = new VncFunction("future?") {
		{
			setArgLists("(future? f)");
			
			setDoc( "Returns true if f is a Future otherwise false");
			
			setExamples("(future? (future (fn [] 100)))");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().validateBlackListedVeniceFunction("future?");

			assertArity("future?", args, 1);

			return Types.isVncJavaObject(args.first(), Future.class) ? True : False;
		}
		
	    private static final long serialVersionUID = -1848883965231344442L;
	};

	public static VncFunction future_done_Q = new VncFunction("future-done?") {
		{
			setArgLists("(future-done? f)");
			
			setDoc( "Returns true if f is a Future is done otherwise false");
			
			setExamples("(future-done? (future (fn [] 100)))");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().validateBlackListedVeniceFunction("future-done?");

			assertArity("future-done?", args, 1);

			if (Types.isVncJavaObject(args.first(), Future.class)) {
				try {
					@SuppressWarnings("unchecked")
					final Future<VncVal> future = (Future<VncVal>)((VncJavaObject)args.first()).getDelegate();
					return future.isDone() ? True : False;
				}
				catch(Exception ex) {
					throw new VncException("Failed to check if future is done", ex);
				}
			}

			throw new VncException(String.format(
					"Function 'future-done?' does not allow type %s as parameter",
					Types.getClassName(args.first())));
		}
		
	    private static final long serialVersionUID = -1848883965231344442L;
	};

	public static VncFunction future_cancel = new VncFunction("future-cancel") {
		{
			setArgLists("(future-cancel f)");
			
			setDoc("Cancels the future");
			
			setExamples("(future-cancel (future (fn [] 100)))");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().validateBlackListedVeniceFunction("future-cancel");

			assertArity("future-cancel", args, 1);


			if (Types.isVncJavaObject(args.first(), Future.class)) {
				try {
					@SuppressWarnings("unchecked")
					final Future<VncVal> future = (Future<VncVal>)((VncJavaObject)args.first()).getDelegate();
					future.cancel(true);
					return args.first();
				}
				catch(Exception ex) {
					throw new VncException("Failed to cancel future", ex);
				}
			}

			throw new VncException(String.format(
					"Function 'future-cancel' does not allow type %s as parameter.",
					Types.getClassName(args.first())));
		}
		
	    private static final long serialVersionUID = -1848883965231344442L;
	};

	public static VncFunction future_cancelled_Q = new VncFunction("future-cancelled?") {
		{
			setArgLists("(future-cancelled? f)");
			
			setDoc( "Returns true if f is a Future is cancelled otherwise false");
			
			setExamples(
					"(future-cancelled? (future (fn [] 100)))");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().validateBlackListedVeniceFunction("future-cancelled?");

			assertArity("future-cancelled?", args, 1);

			if (Types.isVncJavaObject(args.first(), Future.class)) {
				try {
					@SuppressWarnings("unchecked")
					final Future<VncVal> future = (Future<VncVal>)((VncJavaObject)args.first()).getDelegate();
					return future.isCancelled() ? True : False;
				}
				catch(Exception ex) {
					throw new VncException("Failed to check if future is cancelled", ex);
				}
			}

			throw new VncException(String.format(
					"Function 'future-cancelled?' does not allow type %s as parameter",
					Types.getClassName(args.first())));
		}
		
	    private static final long serialVersionUID = -1848883965231344442L;
	};

	

	///////////////////////////////////////////////////////////////////////////
	// Delay
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction delay_Q = new VncFunction("delay?") {
		{
			setArgLists("(delay? x)");
			
			setDoc( "Returns true if x is a Delay created with delay");
			
			setExamples(
					"(do                                              \n" +
					"   (def x (delay (println \"working...\") 100))  \n" +
					"   (delay? x))                                     ");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("delay?", args, 1);

			return Types.isVncJavaObject(args.first(), Delay.class) ? True : False;
		}
		
	    private static final long serialVersionUID = -1848883965231344442L;
	};

	public static VncFunction force = new VncFunction("force") {
		{
			setArgLists("(force x)");
			
			setDoc( "If x is a Delay, returns its value, else returns x");
			
			setExamples(
					"(do                                              \n" +
					"   (def x (delay (println \"working...\") 100))  \n" +
					"   (force x))                                      ");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("force", args, 1);

			if (Types.isVncJavaObject(args.first(), Delay.class)) {
				final Delay delay = (Delay)((VncJavaObject)args.first()).getDelegate();
				return delay.deref();
			}
			else {
				return args.first();
			}
		}
		
	    private static final long serialVersionUID = -1848883965231344442L;
	};


	
	///////////////////////////////////////////////////////////////////////////
	// Thread local
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction new_thread_local = new VncFunction("thread-local") {
		{
			setArgLists("(thread-local)");
			
			setDoc("Creates a new thread-local accessor");
			
			setExamples(
					"(thread-local :a 1 :b 2)", 
					"(thread-local { :a 1 :b 2 })",
					"(do \n" +
					"   (thread-local-clear) \n" +
					"   (assoc (thread-local) :a 1 :b 2) \n" +
					"   (dissoc (thread-local) :a) \n" +
					"   (get (thread-local) :b 100) \n" +
					")");
		}
		
		public VncVal apply(final VncList args) {
			if (args.size() == 1 && Types.isVncMap(args.nth(0))) {
				return new VncThreadLocal(((VncMap)args.nth(0)).getMap());
			}
			else {
				return new VncThreadLocal(args);
			}
		}
		
	    private static final long serialVersionUID = -1848883965231344442L;
	};

	public static VncFunction thread_local_Q = new VncFunction("thread-local?") {
		{
			setArgLists("(thread-local? x)");
			
			setDoc("Returns true if x is a thread-local, otherwise false");
			
			setExamples("(do\n   (def x (thread-local))\n   (thread-local? x))");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("thread-local?", args, 1);
			
			return Types.isVncThreadLocal(args.nth(0)) ? True : False;
		}
		
	    private static final long serialVersionUID = -1848883965231344442L;
	};

	public static VncFunction thread_local_clear = new VncFunction("thread-local-clear") {
		{
			setArgLists("(thread-local-clear)");
			
			setDoc("Removes all thread local vars");
			
			setExamples("(thread-local-clear)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("thread-local-clear", args, 0);
			new VncThreadLocal().clear();
			return this;
		}
		
	    private static final long serialVersionUID = -1848883965231344442L;
	};



	///////////////////////////////////////////////////////////////////////////
	// Thread utils
	///////////////////////////////////////////////////////////////////////////


	public static VncFunction thread_id = new VncFunction("thread-id") {
		{
			setArgLists("(thread-id)");
			
			setDoc( "Returns the identifier of this Thread. The thread ID is a " +
					"positive number generated when this thread was created. " +
					"The thread ID  is unique and remains unchanged during its " +
					"lifetime. When a thread is terminated, this thread ID may " +
					"be reused.");
			
			setExamples("(thread-id)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("thread-id", args, 0);
			return new VncLong(Thread.currentThread().getId());
		}
		
	    private static final long serialVersionUID = -1848883965231344442L;
	};

	public static VncFunction thread_name = new VncFunction("thread-name") {
		{
			setArgLists("(thread-name)");
			
			setDoc("Returns this thread's name.");
			
			setExamples("(thread-name)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("thread-name", args, 0);
			return new VncString(Thread.currentThread().getName());
		}
		
	    private static final long serialVersionUID = -1848883965231344442L;
	};

	
	public static void shutdown() {
		executor.shutdown();
	}

	public static void shutdownNow() {
		executor.shutdownNow();
	}

	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap.Builder()		
					.put("deref",	 			deref)

					.put("atom",				new_atom)
					.put("atom?",				atom_Q)
					.put("reset!",				reset_BANG)
					.put("swap!",				swap_BANG)
					.put("compare-and-set!", 	compare_and_set_BANG)

					.put("promise",				promise)
					.put("promise?",			promise_Q)
					.put("deliver",				deliver)
					
					.put("future",				future)
					.put("future?",				future_Q)
					.put("future-done?",		future_done_Q)
					.put("future-cancel",		future_cancel)
					.put("future-cancelled?",	future_cancelled_Q)

					.put("delay?",				delay_Q)
					.put("force",				force)
					
					.put("thread-id",			thread_id)
					.put("thread-name",			thread_name)

					.put("thread-local",		new_thread_local)
					.put("thread-local?",		thread_local_Q)
					.put("thread-local-clear",	thread_local_clear)
					.toMap();	
	

	private final static AtomicLong futureThreadPoolCounter = new AtomicLong(0);

	private final static ExecutorService executor = 
			Executors.newCachedThreadPool(
					ThreadPoolUtil.createThreadFactory(
							"venice-future-pool-%d", 
							futureThreadPoolCounter,
							true /* daemon threads */));
}
