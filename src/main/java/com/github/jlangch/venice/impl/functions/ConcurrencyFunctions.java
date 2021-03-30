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

import static com.github.jlangch.venice.impl.types.Constants.Nil;
import static com.github.jlangch.venice.impl.types.VncBoolean.False;
import static com.github.jlangch.venice.impl.types.VncBoolean.True;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.MetaUtil;
import com.github.jlangch.venice.impl.javainterop.JavaInterop;
import com.github.jlangch.venice.impl.types.IDeref;
import com.github.jlangch.venice.impl.types.VncAtom;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncThreadLocal;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.VncVolatile;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.concurrent.Agent;
import com.github.jlangch.venice.impl.types.concurrent.Delay;
import com.github.jlangch.venice.impl.types.concurrent.ThreadLocalMap;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.concurrent.ManagedCachedThreadPoolExecutor;
import com.github.jlangch.venice.javainterop.IInterceptor;


public class ConcurrencyFunctions {

	///////////////////////////////////////////////////////////////////////////
	// DEREF
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction deref = 
		new VncFunction(
				"deref", 
				VncFunction
					.meta()
					.arglists("(deref x)", "(deref x timeout-ms timeout-val)")		
					.doc(
						"Dereferences an atom, a future or a promise object. When applied to an " + 
						"atom, returns its current state. When applied to a future, " +
						"will block if computation is not complete. The variant taking a " +
						"timeout can be used for futures and will return timeout-val " + 
						"if the timeout (in milliseconds) is reached before a value " + 
						"is available. If a future is deref'd and the waiting thread is " + 
						"interrupted the futures are cancelled.")
					.examples(
						"(do                             \n" +
						"   (def counter (atom 10))      \n" +
						"   (deref counter))               ",
	
						"(do                             \n" +
						"   (def counter (atom 10))      \n" +
						"   @counter)                      ",
	
						"(do                             \n" +
						"   (defn task [] 100)           \n" +
						"   (let [f (future task)]       \n" +
						"      (deref f)))                 ",
	
						"(do                             \n" +
						"   (defn task [] 100)           \n" +
						"   (let [f (future task)]       \n" +
						"      @f))                        ",
	
						"(do                             \n" +
						"   (defn task [] 100)           \n" +
						"   (let [f (future task)]       \n" +
						"      (deref f 300 :timeout)))    ",
	
						"(do                                              \n" +
						"   (def x (delay (println \"working...\") 100))  \n" +
						"   @x)                                             ",
						
						"(do                             \n" +
						"   (def p (promise))            \n" +
						"   (deliver p 10)               \n" +
						"   @p)                            ",

						"(do                             \n" +
						"   (def x (agent 100))          \n" +
						"   @x)                            ",

						"(do                             \n" +
						"   (def counter (volatile 10))  \n" +
						"   @counter)                      ")

					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1, 3);

				final VncVal first = args.first();

				if (Types.isIDeref(first)) {
					final IDeref d = Coerce.toIDeref(args.first());
					return d.deref();
				}
				else if (Types.isVncJavaObject(first)) {
					final Object delegate = ((VncJavaObject)first).getDelegate();
					if (delegate instanceof Future) {
						@SuppressWarnings("unchecked")
						final Future<VncVal> future = (Future<VncVal>)delegate;
						try {
							if (args.size() == 1) {
								return future.get();
							}
							else {
								final long timeout = Coerce.toVncLong(args.second()).getValue();
								return future.get(timeout, TimeUnit.MILLISECONDS);
							}
						}
						catch(TimeoutException ex) {
							return args.size() == 3 ? args.third() : Nil;
						}
						catch(ExecutionException ex) {
							if (ex.getCause() != null) {
								if (ex.getCause() instanceof SecurityException) {
									throw (SecurityException)ex.getCause();
								}
								else if (ex.getCause() instanceof VncException) {
									throw (VncException)ex.getCause();
								}
							}
							
							throw new VncException("Failed to deref future", ex);
						}
						catch(CancellationException ex) {
							throw new VncException("Failed to deref future. Future has been cancelled", ex);
						}
						catch(InterruptedException ex) {
							// cancel future
							safelyCancelFuture(future);
							throw new com.github.jlangch.venice.InterruptedException(
									"Interrupted while waiting for future to return result (deref future).");
						}
						catch(Exception ex) {
							throw new VncException("Failed to deref future", ex);
						}
					}
					else if (Types.isIDeref(delegate)) {
						return ((IDeref)delegate).deref();
					}
				}
	
				throw new VncException(String.format(
						"Function 'deref' does not allow type %s as parameter.",
						Types.getType(first)));
		
			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction deref_Q = 
		new VncFunction(
				"deref?", 
				VncFunction
					.meta()
					.arglists("(deref? x)")		
					.doc("Returns true if x is dereferencable.")
					.examples(
						"(deref? (atom 10))",
						"(deref? (delay 100))",
						"(deref? (promise))",
						"(deref? (future (fn [] 10)))",
						"(deref? (volatile 100))",
						"(deref? (agent 100))",
						"(deref? (just 100))")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);

				final VncVal first = args.first();

				if (Types.isIDeref(first)) {
					return True;
				}
				else if (Types.isVncJavaObject(first)) {
					final Object delegate = ((VncJavaObject)first).getDelegate();
					if (delegate instanceof Future || Types.isIDeref(delegate)) {
						return True;
					}
				}
	
				return False;
			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction realized_Q = 
		new VncFunction(
				"realized?", 
				VncFunction
					.meta()
					.arglists("(realized? x)")		
					.doc("Returns true if a value has been produced for a promise, delay, or future.")
					.examples(
						"(do                                \n" +
						"   (def task (fn [] 100))          \n" +
						"   (let [f (future task)]          \n" +
						"        (println (realized? f))    \n" +
						"        (println @f)               \n" +
						"        (println (realized? f))))    ",
	
						"(do                                \n" +
						"   (def p (promise))               \n" +
						"   (println (realized? p))         \n" +
						"   (deliver p 123)                 \n" +
						"   (println @p)                    \n" +
						"   (println (realized? p)))          ",
	
						"(do                                \n" +
						"   (def x (delay 100))             \n" +
						"   (println (realized? x))         \n" +
						"   (println @x)                    \n" +
						"   (println (realized? x)))          ")
					.seeAlso("future", "delay", "promise")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);
				
				if (Types.isVncJavaObject(args.first())) {
					final Object delegate = ((VncJavaObject)args.first()).getDelegate();
					if (delegate instanceof Future) {
						return VncBoolean.of(((Future<?>)delegate).isDone());
					}
					else if (delegate instanceof CompletableFuture) {
						return VncBoolean.of(((CompletableFuture<?>)delegate).isDone());
					}
					else if (delegate instanceof Delay) {
						return VncBoolean.of(((Delay)delegate).isRealized());
					}
				}
				
				return True;
			}
		
			private static final long serialVersionUID = -1848883965231344442L;
		};



	///////////////////////////////////////////////////////////////////////////
	// Watches
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction add_watch = 
		new VncFunction(
				"add-watch", 
				VncFunction
					.meta()
					.arglists("(add-watch ref key fn)")		
					.doc(
						"Adds a watch function to an agent/atom reference. The watch fn must " + 
						"be a fn of 4 args: a key, the reference, its old-state, its " + 
						"new-state.")
					.examples(
						"(do                                      \n" +
						"   (def x (agent 10))                    \n" +
						"   (defn watcher [key ref old new]       \n" +
						"         (println \"watcher: \" key))    \n" +
						"   (add-watch x :test watcher))            ")
					.seeAlso("agent")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 3);
				
				final VncVal ref = args.first();
				final VncKeyword key = Coerce.toVncKeyword(args.second());
				final VncFunction fn = Coerce.toVncFunction(args.nth(2));
						
				if (Types.isVncJavaObject(ref)) {
					final Object delegate = ((VncJavaObject)args.first()).getDelegate();
					if (delegate instanceof Agent) {
						((Agent)delegate).addWatch(key, fn);
						return Nil;
					}
				}
				else if (Types.isVncAtom(ref)) {
					((VncAtom)ref).addWatch(key, fn);
					return Nil;
				}
	
				throw new VncException(String.format(
						"Function 'add-watch' does not allow type %s as ref.",
						Types.getType(ref)));
			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction remove_watch = 
		new VncFunction(
				"remove-watch", 
				VncFunction
					.meta()
					.arglists("(remove-watch ref key)")		
					.doc( "Removes a watch function from an agent/atom reference.")
					.examples(
						"(do                                      \n" +
						"   (def x (agent 10))                    \n" +
						"   (defn watcher [key ref old new]       \n" +
						"         (println \"watcher: \" key))    \n" +
						"   (add-watch x :test watcher)           \n" +
						"   (remove-watch x :test))                 ")
					.seeAlso("agent")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 2);
				
				final VncVal ref = args.first();
				final VncKeyword key = Coerce.toVncKeyword(args.second());
						
				if (Types.isVncJavaObject(ref)) {
					final Object delegate = ((VncJavaObject)args.first()).getDelegate();
					if (delegate instanceof Agent) {
						((Agent)delegate).removeWatch(key);
						return Nil;
					}
				}
				else if (Types.isVncAtom(ref)) {
					((VncAtom)ref).removeWatch(key);
					return Nil;
				}
	
				throw new VncException(String.format(
						"Function 'remove-watch' does not allow type %s as ref.",
						Types.getType(ref)));
			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};

	
	///////////////////////////////////////////////////////////////////////////
	// Atom
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction new_atom = 
		new VncFunction(
				"atom", 
				VncFunction
					.meta()
					.arglists(
						"(atom x)",
						"(atom x & options)")		
					.doc(
						"Creates an atom with the initial value x. \n\n" +
						"Options: \n" +
						"  :meta metadata-map \n" +
						"  :validator validate-fn \n" +
						"If metadata-map is supplied, it will become the metadata on the " + 
						"atom. validate-fn must be nil or a side-effect-free fn of one " + 
						"argument, which will be passed the intended new state on any state " + 
						"change. If the new state is unacceptable, the validate-fn should " + 
						"return false or throw an exception.")
					.examples(
						"(do                       \n" +
						"  (def counter (atom 0))  \n" +
						"  (swap! counter inc)     \n" +
						"  (deref counter))          ",
						"(do                       \n" +
						"  (def counter (atom 0))  \n" +
						"  (reset! counter 9)      \n" +
						"  @counter)                 ")
					.seeAlso("deref", "reset!", "swap!", "compare-and-set!", "add-watch", "remove-watch")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertMinArity(this, args, 1);
				
				final VncHashMap options = VncHashMap.ofAll(args.rest());
				final VncVal meta = options.get(new VncKeyword("meta"));
				final VncVal validator = options.get(new VncKeyword("validator"));
				
				return new VncAtom(
						args.first(), 
						validator == Nil ? null : Coerce.toVncFunction(validator),
						MetaUtil.mergeMeta(args.getMeta(), meta));
			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncFunction atom_Q = 
		new VncFunction(
				"atom?", 
				VncFunction
					.meta()
					.arglists("(atom? x)")		
					.doc("Returns true if x is an atom, otherwise false")
					.examples(
						"(do                        \n" +
						"   (def counter (atom 0))  \n" +
						"   (atom? counter))          ")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);
				
				return VncBoolean.of(Types.isVncAtom(args.first()));
			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction reset_BANG = 
		new VncFunction(
				"reset!", 
				VncFunction
					.meta()
					.arglists("(reset! box newval)")		
					.doc(
						"Sets the value of an atom or a volatile to newval without " + 
						"regard for the current value. Returns newval.")
					.examples(
						"(do                           \n" +
						"  (def counter (atom 0))      \n" +
						"  (reset! counter 99)         \n" +
						"  @counter)                     ",
						"(do                           \n" +
						"  (def counter (atom 0))      \n" +
						"  (reset! counter 99))          ",
						"(do                           \n" +
						"  (def counter (volatile 0))  \n" +
						"  (reset! counter 99)         \n" +
						"  @counter)                     ")
					.seeAlso("atom", "volatile")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 2);
				
				final VncVal val = args.first();
				
				if (Types.isVncAtom(val)) {
					return ((VncAtom)val).reset(args.second());					
				}
				else if (Types.isVncVolatile(val)) {
					return ((VncVolatile)val).reset(args.second());					
				}
				else {
					throw new VncException(String.format(
							"Function 'reset!' does not allow type %s as argument.",
							Types.getType(val)));
				}
			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction swap_BANG = 
		new VncFunction(
				"swap!", 
				VncFunction
					.meta()
					.arglists("(swap! box f & args)")		
					.doc(
						"Atomically swaps the value of an atom or a volatile to be: " + 
						"(apply f current-value-of-box args). Note that f may be called " + 
						"multiple times, and thus should be free of side effects. " + 
						"Returns the value that was swapped in.")
					.examples(
						"(do                                  \n" +
						"   (def counter (atom 0))            \n" +
						"   (swap! counter inc))                ",
						"(do                                  \n" +
						"   (def counter (atom 0))            \n" +
						"   (swap! counter inc)               \n" +
						"   (swap! counter + 1)               \n" +
						"   (swap! counter #(inc %))          \n" +
						"   (swap! counter (fn [x] (inc x)))  \n" +
						"   @counter)                           ",
						"(do                                  \n" +
						"   (def fruits (atom ()))            \n" +
						"   (swap! fruits conj :apple)        \n" +
						"   (swap! fruits conj :mango)        \n" +
						"   @fruits)                            ",
						"(do                                  \n" +
						"   (def counter (volatile 0))        \n" +
						"   (swap! counter (partial + 6))     \n" +
						"   @counter)                           ")
				.seeAlso("swap-vals!", "reset!", "compare-and-set!", "atom", "volatile")
				.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertMinArity(this, args, 2);
				
				final VncVal box = args.first();
				
				if (Types.isVncAtom(box)) {
					final VncFunction fn = Coerce.toVncFunction(args.second());
					final VncList swapArgs = args.slice(2);					
					return ((VncAtom)box).swap(fn, swapArgs);
				}
				else if (Types.isVncVolatile(box)) {
					final VncFunction fn = Coerce.toVncFunction(args.second());
					final VncList swapArgs = args.slice(2);					
					return ((VncVolatile)box).swap(fn, swapArgs);
				}
				else {
					throw new VncException(String.format(
							"Function 'swap!' does not allow type %s as argument.",
							Types.getType(box)));
				}

			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction swap_vals_BANG = 
		new VncFunction(
				"swap-vals!", 
				VncFunction
					.meta()
					.arglists("(swap-vals! atom f & args)")		
					.doc(
						"Atomically swaps the value of an atom to be: " + 
						"(apply f current-value-of-atom args). Note that f may be called " + 
						"multiple times, and thus should be free of side effects. " + 
						"Returns [old new], the value of the atom before and after the swap.")
					.examples(
						"(do                                \n" +
						"   (def queue (atom '(1 2 3)))     \n" +
						"   (swap-vals! queue pop))           ")
				.seeAlso("swap!", "reset!", "compare-and-set!", "atom", "volatile")
				.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertMinArity(this, args, 2);
				
				final VncVal box = args.first();
				
				if (Types.isVncAtom(box)) {
					final VncFunction fn = Coerce.toVncFunction(args.second());
					final VncList swapArgs = args.slice(2);					
					return ((VncAtom)box).swap_vals(fn, swapArgs);
				}
				else {
					throw new VncException(String.format(
							"Function 'swap-vals!' does not allow type %s as argument.",
							Types.getType(box)));
				}

			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction compare_and_set_BANG = 
		new VncFunction(
				"compare-and-set!", 
				VncFunction
					.meta()
					.arglists("(compare-and-set! atom oldval newval)")		
					.doc(
						"Atomically sets the value of atom to newval if and only if the " + 
						"current value of the atom is identical to oldval. Returns true if " + 
						"set happened, else false.")
					.examples(
						"(do                               \n" +
						"   (def counter (atom 2))         \n" +
						"   (compare-and-set! counter 2 4) \n" +
						"   @counter)                        ")
					.seeAlso("atom")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 3);
				
				final VncAtom atm = Coerce.toVncAtom(args.first());		
				
				return atm.compareAndSet(args.second(), args.nth(2));
			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};
		
		
		
	///////////////////////////////////////////////////////////////////////////
	// Volatile
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction new_volatile = 
		new VncFunction(
				"volatile", 
				VncFunction
					.meta()
					.arglists("(volatile x)")		
					.doc("Creates a volatile with the initial value x")
					.examples(
						"(do                           \n" +
						"  (def counter (volatile 0))  \n" +
						"  (swap! counter inc)         \n" +
						"  (deref counter))              ",
						"(do                           \n" +
						"  (def counter (volatile 0))  \n" +
						"  (reset! counter 9)          \n" +
						"  @counter)                     ")
					.seeAlso("deref", "reset!", "swap!")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);
				
				return new VncVolatile(args.first(), args.getMeta());
			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncFunction volatile_Q = 
		new VncFunction(
				"volatile?", 
				VncFunction
					.meta()
					.arglists("(volatile? x)")		
					.doc("Returns true if x is a volatile, otherwise false")
					.examples(
						"(do                            \n" +
						"   (def counter (volatile 0))  \n" +
						"   (volatile? counter))          ")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);
				
				return VncBoolean.of(Types.isVncVolatile(args.first()));
			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};

	

	///////////////////////////////////////////////////////////////////////////
	// Agents
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction agent = 
		new VncFunction(
				"agent", 
				VncFunction
					.meta()
					.arglists("(agent state & options)")		
					.doc(
						"Creates and returns an agent with an initial value of state and " +
						"zero or more options. \n\n" +
						"Options: \n" +
						"  :error-handler handler-fn \n" +
						"  :error-mode mode-keyword \n" +
						"  :validator validate-fn \n" +
						"The handler-fn is called if an action throws an exception. It's a " +
						"function taking two args the agent and the exception. The " +
						"mode-keyword may be either :continue (the default) or :fail " +
						"The validate-fn must be nil or a side-effect-free fn of one " + 
						"argument, which will be passed the intended new state on any state " + 
						"change. If the new state is unacceptable, the validate-fn should " + 
						"return false or throw an exception.")
					.examples(
						"(do                         \n" +
						"   (def x (agent 100))      \n" +
						"   (send x + 5)             \n" +
						"   (sleep 100)              \n" +
						"   (deref x))                 ")
					.seeAlso("send", "send-off", "await", "await-for", "deref", "set-error-handler!", "agent-error")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertMinArity(this, args, 1);

				sandboxFunctionCallValidation();

				final VncMap opts = VncHashMap.ofAll(args.rest());

				return new VncJavaObject(new Agent(args.first(), opts));
			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};
			  
	public static VncFunction send = 
		new VncFunction(
				"send", 
				VncFunction
					.meta()
					.arglists("(send agent action-fn args)")		
					.doc(
						"Dispatch an action to an agent. Returns the agent immediately." +
						"The state of the agent will be set to the value of:\n" + 
						" (apply action-fn state-of-agent args)")
					.examples(
						"(do                           \n" +
						"   (def x (agent 100))        \n" +
						"   (send x + 5)               \n" +
						"   (send x (partial + 7))     \n" +
						"   (sleep 100)                \n" +
						"   (deref x))                   ")
					.seeAlso("agent", "send-off")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				ArityExceptions.assertMinArity(this, args, 2);

				sandboxFunctionCallValidation();

				if (Types.isVncJavaObject(args.first(), Agent.class)) {
					final Agent agent = (Agent)Coerce.toVncJavaObject(args.first()).getDelegate();
					final VncFunction fn = Coerce.toVncFunction(args.second());		
					final VncList fnArgs = args.slice(2);		
					
					agent.send(fn, fnArgs);
					return args.first();
				}
				else {
					throw new VncException(String.format(
							"Function 'send' does not allow type %s as agent parameter",
							Types.getType(args.first())));
				}
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction send_off = 
		new VncFunction(
				"send-off", 
				VncFunction
					.meta()
					.arglists("(send-off agent fn args)")		
					.doc(
						"Dispatch a potentially blocking action to an agent. Returns " +
						"the agent immediately. The state of the agent will be set to " +
						"the value of:\n" + 
						" (apply action-fn state-of-agent args)")
					.examples(
						"(do                           \n" +
						"   (def x (agent 100))        \n" +
						"   (send-off x + 5)           \n" +
						"   (send-off x (partial + 7)) \n" +
						"   (sleep 100)                \n" +
						"   (deref x))                   ")
					.seeAlso("agent", "send")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertMinArity(this, args, 2);

				sandboxFunctionCallValidation();

				if (Types.isVncJavaObject(args.first(), Agent.class)) {
					final Agent agent = (Agent)Coerce.toVncJavaObject(args.first()).getDelegate();
					final VncFunction fn = Coerce.toVncFunction(args.second());		
					final VncList fnArgs = args.slice(2);		
					
					agent.send_off(fn, fnArgs);
					return args.first();
				}
				else {
					throw new VncException(String.format(
							"Function 'send-off' does not allow type %s as agent parameter",
							Types.getType(args.first())));
				}
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction restart_agent = 
		new VncFunction(
				"restart-agent", 
				VncFunction
					.meta()
					.arglists("(restart-agent agent state)")		
					.doc(
						"When an agent is failed, changes the agent state to new-state and " + 
						"then un-fails the agent so that sends are allowed again.")
					.examples(
						"(do                          \n" +
						"   (def x (agent 100))       \n" +
						"   (restart-agent x 200)     \n" +
						"   (deref x))                  ")
					.seeAlso("agent")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 2);

				sandboxFunctionCallValidation();

				if (Types.isVncJavaObject(args.first(), Agent.class)) {
					final Agent agent = (Agent)Coerce.toVncJavaObject(args.first()).getDelegate();
					final VncVal state = args.second();		
					
					agent.restart(state);
					return args.first();
				}
				else {
					throw new VncException(String.format(
							"Function 'restart-agent' does not allow type %s as agent parameter",
							Types.getType(args.first())));
				}
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction set_error_handler = 
		new VncFunction(
				"set-error-handler!", 
				VncFunction
					.meta()
					.arglists("(set-error-handler! agent handler-fn)")		
					.doc(
						"Sets the error-handler of an agent to handler-fn. If an action " + 
						"being run by the agent throws an exception handler-fn will be " +
						"called with two arguments: the agent and the exception.")
					.examples(
						"(do                                          \n" +
						"   (def x (agent 100))                       \n" +
						"   (defn err-handler-fn [ag ex]              \n" +
						"      (println \"error occured: \"           \n" +
						"               (:message ex)                 \n" +
						"               \" and we still have value\"  \n" +
						"               @ag))                         \n" +
						"   (set-error-handler! x err-handler-fn)     \n" +
						"   (send x (fn [n] (/ n 0))))                  ")
					.seeAlso("agent", "agent-error-mode", "agent-error")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 2);

				sandboxFunctionCallValidation();

				if (Types.isVncJavaObject(args.first(), Agent.class)) {
					final Agent agent = (Agent)Coerce.toVncJavaObject(args.first()).getDelegate();
					agent.setErrorHandler(Coerce.toVncFunction(args.second()));
					return args.first();
				}
				else {
					throw new VncException(String.format(
							"Function 'set-error-handler!' does not allow type %s as agent parameter",
							Types.getType(args.first())));
				}
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction agent_error = 
		new VncFunction(
				"agent-error", 
				VncFunction
					.meta()
					.arglists("(agent-error agent)")		
					.doc(
						"Returns the exception thrown during an asynchronous action of the " + 
						"agent if the agent is failed. Returns nil if the agent is not " + 
						"failed.")
					.examples(
						"(do                                              \n" +
						"   (def x (agent 100 :error-mode :fail))         \n" +
						"   (send x (fn [n] (/ n 0)))                     \n" +
						"   (sleep 500)                                   \n" +
						"   (agent-error x))                                ")
					.seeAlso("agent", "set-error-handler!", "agent-error-mode")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);

				sandboxFunctionCallValidation();

				if (Types.isVncJavaObject(args.first(), Agent.class)) {
					final Agent agent = (Agent)Coerce.toVncJavaObject(args.first()).getDelegate();
					final RuntimeException ex = agent.getError();
					return ex == null ? Nil : new VncJavaObject(ex);
				}
				else {
					throw new VncException(String.format(
							"Function 'agent-error' does not allow type %s as agent parameter",
							Types.getType(args.first())));
				}
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};


	public static VncFunction agent_error_mode = 
		new VncFunction(
				"agent-error-mode", 
				VncFunction
					.meta()
					.arglists("(agent-error-mode agent)")		
					.doc( "Returns the agent's error mode")
					.examples(
						"(do                                              \n" +
						"   (def x (agent 100 :error-mode :fail))         \n" +
						"   (agent-mode x))                                 ")
					.seeAlso("agent", "set-error-handler!", "agent-error")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);
				
				if (Types.isVncJavaObject(args.first(), Agent.class)) {
					final Agent agent = (Agent)Coerce.toVncJavaObject(args.first()).getDelegate();
					return agent.getErrorMode();
				}
				else {
					throw new VncException(String.format(
							"Function 'agent-error-mode' does not allow type %s as agent parameter",
							Types.getType(args.first())));
				}
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction await = 
		new VncFunction(
				"await", 
				VncFunction
					.meta()
					.arglists("(await agents)")		
					.doc(
						"Blocks the current thread (indefinitely) until all actions dispatched " + 
						"thus far (from this thread or agent) to the agents have occurred. ")
					.examples(
						"(do                                              \n" +
						"   (def x1 (agent 100))                          \n" +
						"   (def x2 (agent {}))                           \n" +
						"   (send-off x1 + 5)                             \n" +
						"   (send-off x2 (fn [state]                      \n" +
					    "                  (sleep 100)                    \n" +
					    "                  (assoc state :done true)))     \n" +
					    "   ;; blocks till the agent actions are finished \n" +
						"   (await x1 x2))                                 ")
					.seeAlso("agent", "await-for")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertMinArity(this, args, 1);

				sandboxFunctionCallValidation();

				final List<Agent> agents = args.stream()
											   .map(a -> (Agent)Coerce.toVncJavaObject(a).getDelegate())
											   .collect(Collectors.toList());
				
				return agents.isEmpty() 
						? True
						: VncBoolean.of(Agent.await(agents, -1));
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction await_for = 
		new VncFunction(
				"await-for", 
				VncFunction
					.meta()
					.arglists("(await-for timeout-ms agents)")		
					.doc(
						"Blocks the current thread until all actions dispatched thus " + 
						"far (from this thread or agent) to the agents have occurred, or the " + 
						"timeout (in milliseconds) has elapsed. Returns logical false if " + 
						"returning due to timeout, logical true otherwise.")
					.examples(
						"(do                                              \n" +
						"   (def x1 (agent 100))                          \n" +
						"   (def x2 (agent {}))                           \n" +
						"   (send-off x1 + 5)                             \n" +
						"   (send-off x2 (fn [state]                      \n" +
					    "                  (sleep 100)                    \n" +
					    "                  (assoc state :done true)))     \n" +
					    "   ;; blocks till the agent actions are finished \n" +
						"   (await-for 500 x1 x2))                          ")
					.seeAlso("agent", "await")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertMinArity(this, args, 2);

				sandboxFunctionCallValidation();

				final long timeoutMillis = Coerce.toVncLong(args.first()).getValue();
				final List<Agent> agents = args.rest()
											   .stream()
											   .map(a -> (Agent)Coerce.toVncJavaObject(a).getDelegate())
											   .collect(Collectors.toList());
				
				return agents.isEmpty() 
						? True
						: VncBoolean.of(Agent.await(agents, timeoutMillis));
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction shutdown_agents = 
		new VncFunction(
				"shutdown-agents", 
				VncFunction
					.meta()
					.arglists("(shutdown-agents)")		
					.doc(
						"Initiates a shutdown of the thread pools that back the agent " + 
						"system. Running actions will complete, but no new actions will been " + 
						"accepted")
					.examples(
						"(do                           \n" +
						"   (def x1 (agent 100))       \n" +
						"   (def x2 (agent 100))       \n" +
						"   (shutdown-agents))          ")
					.seeAlso("agent")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 0);

				sandboxFunctionCallValidation();

				Agent.shutdown();
				
				return Nil;
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction shutdown_agents_Q = 
		new VncFunction(
				"shutdown-agents?", 
				VncFunction
					.meta()
					.arglists("(shutdown-agents?)")		
					.doc("Returns true if the thread-pool that backs the agents is shut down")
					.examples(
						"(do                           \n" +
						"   (def x1 (agent 100))       \n" +
						"   (def x2 (agent 100))       \n" +
						"   (shutdown-agents)          \n" +
						"   (sleep 300)                \n" +
						"   (shutdown-agents?))          ")
					.seeAlso("agent")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 0);
		
				return VncBoolean.of(Agent.isShutdown());
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction await_termination_agents = 
		new VncFunction(
				"await-termination-agents", 
				VncFunction
					.meta()
					.arglists("(shutdown-agents)")		
					.doc(
						"Blocks until all actions have completed execution after a shutdown " +
						"request, or the timeout occurs, or the current thread is " +
						"interrupted, whichever happens first.")
					.examples(
						"(do                                   \n" +
						"   (def x1 (agent 100))               \n" +
						"   (def x2 (agent 100))               \n" +
						"   (shutdown-agents)                  \n" +
						"   (await-termination-agents 1000))     ")
					.seeAlso("agent")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);

				sandboxFunctionCallValidation();

				final long timeoutMillis = Coerce.toVncLong(args.first()).getValue();
	
				Agent.awaitTermination(timeoutMillis);
				
				return Nil;
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction await_termination_agents_Q = 
		new VncFunction(
				"await-termination-agents?", 
				VncFunction
					.meta()
					.arglists("(await-termination-agents?)")		
					.doc( "Returns true if all tasks have been completed following agent shut down")
					.examples(
						"(do                                  \n" +
						"   (def x1 (agent 100))              \n" +
						"   (def x2 (agent 100))              \n" +
						"   (shutdown-agents)                 \n" +
						"   (await-termination-agents 1000)   \n" +
						"   (sleep 300)                       \n" +
						"   (await-termination-agents?))       ")
					.seeAlso("agent")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 0);
		
				return VncBoolean.of(Agent.isShutdown());
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};

	

	///////////////////////////////////////////////////////////////////////////
	// Promises
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction deliver = 
		new VncFunction(
				"deliver", 
				VncFunction
					.meta()
					.arglists("(deliver ref value)")		
					.doc(
						"Delivers the supplied value to the promise, releasing any pending " + 
						"derefs. A subsequent call to deliver on a promise will have no effect.")
					.examples(
						"(do                   \n" +
						"   (def p (promise))  \n" +
						"   (deliver p 10)     \n" +
						"   (deliver p 20)     \n" +
						"   @p)                  ")
					.seeAlso("promise", "realized?")
					.build()
		) {
			@SuppressWarnings("unchecked")
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 2);

				sandboxFunctionCallValidation();

				final Object promise = Coerce.toVncJavaObject(args.first()).getDelegate();
				final VncVal value = args.second();
				
				if (promise instanceof CompletableFuture) {
					((CompletableFuture<VncVal>)promise).complete(value);
					return Nil;
				}
				else {
					throw new VncException(String.format(
							"Function 'deliver' does not allow type %s as parameter",
							Types.getType(args.first())));
				}
			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction promise = 
		new VncFunction(
				"promise", 
				VncFunction
					.meta()
					.arglists("(promise)")		
					.doc(
						"Returns a promise object that can be read with deref, and set, " + 
						"once only, with deliver. Calls to deref prior to delivery will " + 
						"block, unless the variant of deref with timeout is used. All " + 
						"subsequent derefs will return the same delivered value without " + 
						"blocking.")
					.examples(
						"(do                   \n" +
						"   (def p (promise))  \n" +
						"   (deliver p 10)     \n" +
						"   (deliver p 20)     \n" +
						"   @p)                  ",
					
						"(do                                            \n" +
						"   (def p (promise))                           \n" +
						"   (defn task1 [] (sleep 500) (deliver p 10))  \n" +
						"   (defn task2 [] (sleep 800) (deliver p 20))  \n" +
						"   (future task1)                              \n" +
						"   (future task2)                              \n" +
						"   @p)                                           ")
					.seeAlso("deliver", "promise?", "realized?", "deref")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 0);

				sandboxFunctionCallValidation();

				return new VncJavaObject(new CompletableFuture<VncVal>());
			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction promise_Q = 
		new VncFunction(
				"promise?", 
				VncFunction
					.meta()
					.arglists("(promise? p)")		
					.doc("Returns true if f is a Promise otherwise false")
					.examples("(promise? (promise)))")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);
	
				return VncBoolean.of(Types.isVncJavaObject(args.first(), CompletableFuture.class));
			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};

	

	///////////////////////////////////////////////////////////////////////////
	// Futures
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction future = 
		new VncFunction(
				"future", 
				VncFunction
					.meta()
					.arglists("(future fn)")		
					.doc(
						"Takes a function without arguments and yields a future object that will " + 
						"invoke the function in another thread, and will cache the result and " + 
						"return it on all subsequent calls to deref. If the computation has " + 
						"not yet finished, calls to deref will block, unless the variant of " + 
						"deref with timeout is used. \n" +
						"Thread local vars will be inherited by the future child thread. Changes " +
						"of the child's thread local vars will not be seen on the parent.")
					.examples(
						"(do                                          \n" + 
						"   (def wait (fn [] (do (sleep 300) 100)))   \n" + 
						"   (let [f (future wait)]                    \n" + 
						"        (deref f)))                            ",

						"(do                                                 \n" + 
						"   (def wait (fn [x] (do (sleep 300) (+ x 100))))   \n" + 
						"   (let [f (future (partial wait 10))]              \n" + 
						"        (deref f)))                                   ",
					

						";; demonstrates the use of thread locals with futures              \n" +
						"(do                                                                \n" +
						"   ;; parent thread locals                                         \n" +
						"   (binding [a 10 b 20]                                            \n" +
						"      ;; future with child thread locals                           \n" +
						"      (let [f (future (fn [] (binding [b 90] {:a a :b b})))]       \n" +
						"         {:child @f :parent {:a a :b b}})))                          ")
					.seeAlso("deref", "realized?", "future-done?", "future-cancel", "future-cancelled?", "futures-fork", "futures-wait")
					.build()
		) {		
			public VncVal apply(final VncList args) {	
				ArityExceptions.assertArity(this, args, 1);

				sandboxFunctionCallValidation();

				final VncFunction fn = Coerce.toVncFunction(args.first());
		
				final IInterceptor parentInterceptor = JavaInterop.getInterceptor();
				
				// thread local values from the parent thread
				final AtomicReference<Map<VncKeyword,VncVal>> parentThreadLocals = 
						new AtomicReference<>(ThreadLocalMap.getValues());
				
				final Callable<VncVal> taskWrapper = () -> {
					// The future function is called from the JavaVM. Rig a
					// Venice context with the thread local vars and the sandbox
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
				
				final Future<VncVal> future = mngdExecutor
												.getExecutor()
												.submit(taskWrapper);
				
				return new VncJavaObject(future);
				
//				// Canceling a CompletableFuture does not interrupt the task wrapper!!!
//				
//				final CompletableFuture<VncVal> completableFuture = new CompletableFuture<>();
//				
//				final Runnable taskWrapper = () -> {
//					// The future function is called from the JavaVM. Rig a
//					// Venice context with the thread local vars and the sandbox
//					try {
//						// inherit thread local values to the child thread
//						ThreadLocalMap.setValues(parentThreadLocals.get());
//						ThreadLocalMap.clearCallStack();
//						JavaInterop.register(parentInterceptor);	
//						
//						try {
//							completableFuture.complete(fn.applyOf());
//						}
//						catch(Exception ex) {
//							completableFuture.completeExceptionally(ex);
//						}
//					}
//					finally {
//						// clean up
//						JavaInterop.unregister();
//						ThreadLocalMap.remove();
//					}
//				};
//				
//				mngdExecutor.getExecutor().submit(taskWrapper);
//				
//				return new VncJavaObject(completableFuture);
			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction future_Q = 
		new VncFunction(
				"future?", 
				VncFunction
					.meta()
					.arglists("(future? f)")		
					.doc( "Returns true if f is a Future otherwise false")
					.examples("(future? (future (fn [] 100)))")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);
	
				return VncBoolean.of(Types.isVncJavaObject(args.first(), Future.class));
			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction future_done_Q = 
		new VncFunction(
				"future-done?", 
				VncFunction
					.meta()
					.arglists("(future-done? f)")		
					.doc( "Returns true if f is a Future is done otherwise false")
					.examples(
						"(do                                                            \n" +
						"   (def wait (fn [] (do (sleep 200) 100)))                     \n" +
						"   (let [f (future wait)]                                      \n" +
						"      (sleep 50)                                               \n" +
						"      (printf \"After 50ms: done=%b\\n\" (future-done? f))     \n" +
						"      (sleep 300)                                              \n" +
						"      (printf \"After 300ms: done=%b\\n\" (future-done? f))))    ")
					.seeAlso("future", "realized?", "future-cancel", "future-cancelled?")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);
	
				if (Types.isVncJavaObject(args.first(), Future.class)) {
					try {
						@SuppressWarnings("unchecked")
						final Future<VncVal> future = Coerce.toVncJavaObject(args.first(), Future.class);
						return VncBoolean.of(future.isDone());
					}
					catch(Exception ex) {
						throw new VncException("Failed to check if future is done", ex);
					}
				}
	
				throw new VncException(String.format(
						"Function 'future-done?' does not allow type %s as parameter",
						Types.getType(args.first())));
			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction future_cancel = 
		new VncFunction(
				"future-cancel", 
				VncFunction
					.meta()
					.arglists("(future-cancel f)")		
					.doc("Cancels the future")
					.examples(
						"(do                                                                     \n" +
						"   (def wait (fn [] (do (sleep 400) 100)))                              \n" +
						"   (let [f (future wait)]                                               \n" +
						"      (sleep 50)                                                        \n" +
						"      (printf \"After 50ms: cancelled=%b\\n\" (future-cancelled? f))    \n" +
						"      (future-cancel f)                                                 \n" +
						"      (sleep 100)                                                       \n" +
						"      (printf \"After 150ms: cancelled=%b\\n\" (future-cancelled? f))))   ")
					.seeAlso("future", "future-done?", "future-cancelled?")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);

				sandboxFunctionCallValidation();
	
				if (Types.isVncJavaObject(args.first(), Future.class)) {
					try {
						@SuppressWarnings("unchecked")
						final Future<VncVal> future = Coerce.toVncJavaObject(args.first(), Future.class);
						future.cancel(true);
						return args.first();
					}
					catch(Exception ex) {
						throw new VncException("Failed to cancel future", ex);
					}
				}
	
				throw new VncException(String.format(
						"Function 'future-cancel' does not allow type %s as parameter.",
						Types.getType(args.first())));
			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction future_cancelled_Q = 
		new VncFunction(
				"future-cancelled?", 
				VncFunction
					.meta()
					.arglists("(future-cancelled? f)")		
					.doc("Returns true if f is a Future is cancelled otherwise false")
					.examples("(future-cancelled? (future (fn [] 100)))")
					.seeAlso("future", "future-done?", "future-cancel")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);
	
				if (Types.isVncJavaObject(args.first(), Future.class)) {
					try {
						@SuppressWarnings("unchecked")
						final Future<VncVal> future = Coerce.toVncJavaObject(args.first(), Future.class);
						return VncBoolean.of(future.isCancelled());
					}
					catch(Exception ex) {
						throw new VncException("Failed to check if future is cancelled", ex);
					}
				}
	
				throw new VncException(String.format(
						"Function 'future-cancelled?' does not allow type %s as parameter",
						Types.getType(args.first())));
			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction futures_fork = 
		new VncFunction(
				"futures-fork", 
				VncFunction
					.meta()
					.arglists(
						"(futures-fork count worker-factory-fn)")		
					.doc(
						"Creates a list of count futures. The worker factory is single argument " +
						"function that gets the worker index (0..count-1) as argument and returns " +
						"a worker function. Returns a list with the created futures.")
					.examples(
						"(do                                                \n" +
						"  (def mutex 0)                                    \n" +
						"  (defn log [& xs]                                 \n" +
						"    (locking mutex (println (apply str xs))))      \n" +
						"  (defn factory [n]                                \n" +
						"    (fn [] (log \"Worker\" n)))                    \n" +
						"  (apply futures-wait (futures-fork 3 factory)))     ")
					.seeAlso("future", "futures-wait")
				.build()
		) {	
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 2);

				sandboxFunctionCallValidation();
	
				final VncLong count = Coerce.toVncLong(args.first());
				final VncFunction workerFactoryFn = Coerce.toVncFunction(args.second());
				
				final List<VncVal> futures = new ArrayList<>();
				
				for(int ii=0; ii<count.getValue(); ii++) {
					final VncFunction worker = (VncFunction)workerFactoryFn.apply(VncList.of(new VncLong(ii)));
					futures.add(future.apply(VncList.of(worker)));
				}
				
				return VncList.ofList(futures);
			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction futures_thread_pool_size = 
		new VncFunction(
				"futures-thread-pool-size", 
				VncFunction
					.meta()
					.arglists(
						"(futures-thread-pool-size)")		
					.doc(
						"Returns the maximum pool size of the ThreadPoolExecutor that serving " +
						"the futures.")
					.examples(
						"(futures-thread-pool-size)")
					.seeAlso("future")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 0);

				return new VncLong(mngdExecutor.getMaximumThreadPoolSize());
			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction futures_wait = 
		new VncFunction(
				"futures-wait", 
				VncFunction
					.meta()
					.arglists(
						"(futures-wait & futures)")		
					.doc(
						"Waits for all futures to get terminated. If the waiting " +
						"thread is interrupted the futures are cancelled. ")
					.examples(
						"(do                                                \n" +
						"  (def mutex 0)                                    \n" +
						"  (defn log [& xs]                                 \n" +
						"    (locking mutex (println (apply str xs))))      \n" +
						"  (defn factory [n]                                \n" +
						"    (fn [] (log \"Worker\" n)))                    \n" +
						"  (apply futures-wait (futures-fork 3 factory)))     ")
					.seeAlso("future", "futures-fork")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				ArityExceptions.assertMinArity(this, args, 0);

				sandboxFunctionCallValidation();
	
				for(VncVal v : args) {
					final Future<?> future = Coerce.toVncJavaObject(v, Future.class);
					
					try {
						future.get();
					}
					catch(ExecutionException | CancellationException ex) {
						// ok, continue with next future. We just wait for termination and
						// do not care about the future's result!
					}
					catch(InterruptedException ex) {
						// cancel all futures
						args.forEach(f -> safelyCancelFuture(Coerce.toVncJavaObject(f, Future.class)));
						
						throw new com.github.jlangch.venice.InterruptedException(
								"Interrupted while waiting for futures to terminate (futures-wait & futures).");
					}
					catch(Exception ex) {
						throw new VncException("Failed to wait for future", ex);
					}
				}
				
				return Nil;
			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};

		
	///////////////////////////////////////////////////////////////////////////
	// Delay
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction delay_Q = 
		new VncFunction(
				"delay?", 
				VncFunction
					.meta()
					.arglists("(delay? x)")		
					.doc("Returns true if x is a Delay created with delay")
					.examples(
						"(do                                              \n" +
						"   (def x (delay (println \"working...\") 100))  \n" +
						"   (delay? x))                                     ")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);
	
				return VncBoolean.of(Types.isVncJavaObject(args.first(), Delay.class));
			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction force = 
		new VncFunction(
				"force", 
				VncFunction
					.meta()
					.arglists("(force x)")		
					.doc("If x is a Delay, returns its value, else returns x")
					.examples(
						"(do                                              \n" +
						"   (def x (delay (println \"working...\") 100))  \n" +
						"   (force x))",
						"(force (+ 1 2))")
					.seeAlso("delay", "deref", "realized?")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);

				sandboxFunctionCallValidation();
	
				if (Types.isVncJavaObject(args.first(), Delay.class)) {
					final Delay delay = Coerce.toVncJavaObject(args.first(), Delay.class);
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

	public static VncFunction new_thread_local = 
		new VncFunction(
				"thread-local", 
				VncFunction
					.meta()
					.arglists("(thread-local)")		
					.doc("Creates a new thread-local accessor")
					.examples(
						"(assoc! (thread-local) :a 1)",
						"(get (thread-local) :a 100)",
						"(thread-local :a 1 :b 2)", 
						"(thread-local { :a 1 :b 2 })",
						"(do \n" +
						"   (thread-local-clear) \n" +
						"   (assoc! (thread-local) :a 1 :b 2) \n" +
						"   (dissoc! (thread-local) :a) \n" +
						"   (get (thread-local) :b 100))")
					.seeAlso(
						"thread-local-clear", 
						"thread-local-map", 
						"assoc!", 
						"dissoc!",
						"get")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				if (args.size() == 1 && Types.isVncMap(args.first())) {
					return new VncThreadLocal(((VncMap)args.first()).getJavaMap());
				}
				else {
					return new VncThreadLocal(args);
				}
			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction thread_local_Q = 
		new VncFunction(
				"thread-local?", 
				VncFunction
					.meta()
					.arglists("(thread-local? x)")		
					.doc("Returns true if x is a thread-local, otherwise false")
					.examples(
						"(do\n" +
						"  (def x (thread-local))\n" +
						"  (thread-local? x))")
					.seeAlso("thread-local")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);
				
				return VncBoolean.of(Types.isVncThreadLocal(args.first()));
			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction thread_local_clear = 
		new VncFunction(
				"thread-local-clear", 
				VncFunction
					.meta()
					.arglists("(thread-local-clear)")		
					.doc("Removes all thread local vars")
					.examples("(thread-local-clear)")
					.seeAlso("thread-local", "dissoc!")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 0);
				new VncThreadLocal().clear();
				return this;
			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction thread_local_map = 
		new VncFunction(
				"thread-local-map", 
				VncFunction
					.meta()
					.arglists("(thread-local-map)")		
					.doc("Returns a snaphost of the thread local vars as a map")
					.examples(
						"(do \n" +
						"  (thread-local :a 1 :b 2) \n" +
						"  (thread-local-map))")
					.seeAlso("thread-local", "get")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 0);
				return VncThreadLocal.toMap();
			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};



	///////////////////////////////////////////////////////////////////////////
	// Thread
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction thread_id = 
		new VncFunction(
				"thread-id", 
				VncFunction
					.meta()
					.arglists("(thread-id)")		
					.doc(
						"Returns the identifier of this Thread. The thread ID is a " +
						"positive number generated when this thread was created. " +
						"The thread ID  is unique and remains unchanged during its " +
						"lifetime. When a thread is terminated, this thread ID may " +
						"be reused.")
					.examples("(thread-id)")
					.seeAlso("thread-name")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 0);
				return new VncLong(Thread.currentThread().getId());
			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction thread_name = 
		new VncFunction(
				"thread-name", 
				VncFunction
					.meta()
					.arglists("(thread-name)")		
					.doc("Returns this thread's name.")
					.examples("(thread-name)")
					.seeAlso("thread-id")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 0);
				return new VncString(Thread.currentThread().getName());
			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction thread_interrupted_Q = 
		new VncFunction(
				"thread-interrupted?", 
				VncFunction
					.meta()
					.arglists("(thread-interrupted?)")		
					.doc(
						"Tests whether this thread has been interrupted. The " +
						"interrupted status of the thread is unaffected by this " +
						"method. \n" +
						"Returns true if the current thread has been interrupted " +
						"else false.")
					.examples("(thread-interrupted?)")
					.seeAlso("thread-interrupted")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 0);
				return VncBoolean.of(Thread.currentThread().isInterrupted());
			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction thread_interrupted = 
		new VncFunction(
				"thread-interrupted", 
				VncFunction
					.meta()
					.arglists("(thread-interrupted)")		
					.doc(
						"Tests whether the current thread has been interrupted. " +
						"The interrupted status of the thread is cleared by this " +
						"method. In other words, if this method were to be called " +
						"twice in succession, the second call would return false " +
						"(unless the current thread were interrupted again, after " +
						"the first call had cleared its interrupted status and " +
						"before the second call had examined it).\n" + 
						"Returns true if the current thread has been interrupted " +
						"else false.")
					.examples("(thread-interrupted)")
					.seeAlso("thread-interrupted?")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 0);
				return VncBoolean.of(Thread.interrupted());
			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};



	///////////////////////////////////////////////////////////////////////////
	// Utils
	///////////////////////////////////////////////////////////////////////////

	public static void shutdown() {
		mngdExecutor.shutdown();
	}

	public static void setMaximumFutureThreadPoolSize(final int maximumPoolSize) {
		mngdExecutor.setMaximumThreadPoolSize(maximumPoolSize);
	}
	

	private static void safelyCancelFuture(final Future<?> future) {
		try { 
			future.cancel(true);
		}
		catch(Exception e) { 
		}
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap
					.Builder()		
					.add(deref)
					.add(deref_Q)
					.add(realized_Q)
					
					.add(add_watch)
					.add(remove_watch)

					.add(new_atom)
					.add(atom_Q)
					.add(reset_BANG)
					.add(swap_BANG)
					.add(swap_vals_BANG)
					.add(compare_and_set_BANG)

					.add(new_volatile)
					.add(volatile_Q)

					.add(agent)
					.add(send)
					.add(send_off)
					.add(restart_agent)
					.add(set_error_handler)
					.add(agent_error)
					.add(agent_error_mode)
					.add(await)
					.add(await_for)
					.add(shutdown_agents)
					.add(shutdown_agents_Q)
					.add(await_termination_agents)
					.add(await_termination_agents_Q)
										
					.add(promise)
					.add(promise_Q)
					.add(deliver)
					
					.add(future)
					.add(future_Q)
					.add(future_done_Q)
					.add(future_cancel)
					.add(future_cancelled_Q)
					.add(futures_fork)
					.add(futures_wait)
					.add(futures_thread_pool_size)

					.add(delay_Q)
					.add(force)
					
					.add(thread_id)
					.add(thread_name)
					.add(thread_interrupted_Q)
					.add(thread_interrupted)

					.add(new_thread_local)
					.add(thread_local_Q)
					.add(thread_local_clear)
					.add(thread_local_map)
					.toMap();	
	
	
	private static ManagedCachedThreadPoolExecutor mngdExecutor = 
			new ManagedCachedThreadPoolExecutor("venice-future-pool", 100);
}
