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
package com.github.jlangch.venice.impl.functions;

import static com.github.jlangch.venice.impl.functions.FunctionsUtil.assertArity;
import static com.github.jlangch.venice.impl.functions.FunctionsUtil.assertMinArity;
import static com.github.jlangch.venice.impl.types.Constants.False;
import static com.github.jlangch.venice.impl.types.Constants.Nil;
import static com.github.jlangch.venice.impl.types.Constants.True;

import java.util.List;
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
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.javainterop.DynamicInvocationHandler;
import com.github.jlangch.venice.impl.javainterop.JavaInterop;
import com.github.jlangch.venice.impl.javainterop.JavaInteropUtil;
import com.github.jlangch.venice.impl.types.Coerce;
import com.github.jlangch.venice.impl.types.Types;
import com.github.jlangch.venice.impl.types.VncAtom;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncThreadLocal;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.util.Agent;
import com.github.jlangch.venice.impl.util.Delay;
import com.github.jlangch.venice.impl.util.ThreadLocalMap;
import com.github.jlangch.venice.impl.util.ThreadPoolUtil;
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
						"Dereferences an atom or a Future object. When applied to an " + 
						"atom, returns its current state. When applied to a future, " +
						"will block if computation not complete. The variant taking a " +
						"timeout can be used for futures and will return timeout-val " + 
						"if the timeout (in milliseconds) is reached before a value " + 
						"is available. \n" +
						"Also reader macro: @atom/@future/@promise.")
					.examples(
						"(do                             \n" +
						"   (def counter (atom 10))      \n" +
						"   (deref counter))               ",
	
						"(do                             \n" +
						"   (def counter (atom 10))      \n" +
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
	
						"(do                                              \n" +
						"   (def x (delay (println \"working...\") 100))  \n" +
						"   @x)                                             ")
					.build()
		) {
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
					else if (delegate instanceof Agent) {
						return ((Agent)delegate).deref();
					}
				}
	
				throw new VncException(String.format(
						"Function 'deref' does not allow type %s as parameter.",
						Types.getClassName(args.first())));
		
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
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("realized?", args, 1);
				
				if (Types.isVncJavaObject(args.first())) {
					final Object delegate = ((VncJavaObject)args.first()).getDelegate();
					if (delegate instanceof Future) {
						return ((Future<?>)delegate).isDone() ? True : False;
					}
					else if (delegate instanceof CompletableFuture) {
						return ((CompletableFuture<?>)delegate).isDone() ? True : False;
					}
					else if (delegate instanceof Delay) {
						return ((Delay)delegate).isRealized() ? True : False;
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
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("add-watch", args, 3);
				
				final VncVal ref = args.first();
				final VncKeyword key = Coerce.toVncKeyword(args.nth(1));
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
						Types.getClassName(ref)));
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
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("remove-watch", args, 2);
				
				final VncVal ref = args.first();
				final VncKeyword key = Coerce.toVncKeyword(args.nth(1));
						
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
						Types.getClassName(ref)));
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
					.arglists("(atom x)")		
					.doc("Creates an atom with the initial value x")
					.examples("(do\n   (def counter (atom 0))\n   (deref counter))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("atom", args, 1);
				
				return new VncAtom(args.first(), args.getMeta());
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
					.examples("(do\n   (def counter (atom 0))\n   (atom? counter))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("atom?", args, 1);
				
				return Types.isVncAtom(args.first()) ? True : False;
			}
			
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction reset_BANG = 
		new VncFunction(
				"reset!", 
				VncFunction
					.meta()
					.arglists("(reset! atom newval)")		
					.doc(
						"Sets the value of atom to newval without regard for the " + 
						"current value. Returns newval.")
					.examples("(do\n   (def counter (atom 0))\n   (reset! counter 99)\n   (deref counter))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("reset!", args, 2);
				
				final VncAtom atm = Coerce.toVncAtom(args.first());
				return atm.reset(args.nth(1));
			}
			
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction swap_BANG = 
		new VncFunction(
				"swap!", 
				VncFunction
					.meta()
					.arglists("(swap! atom f & args)")		
					.doc(
						"Atomically swaps the value of atom to be: " + 
						"(apply f current-value-of-atom args). Note that f may be called " + 
						"multiple times, and thus should be free of side effects.  Returns " + 
						"the value that was swapped in.")
					.examples("(do\n   (def counter (atom 0))\n   (swap! counter inc)\n   (deref counter))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertMinArity("swap!", args, 2);
				
				final VncAtom atm = Coerce.toVncAtom(args.first());		
				final VncFunction fn = Coerce.toVncFunction(args.nth(1));
				final VncList swapArgs = args.slice(2);
				
				return atm.swap(fn, swapArgs);
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
						"set happened, else false")
					.examples("(do\n   (def counter (atom 2))\n   (compare-and-set! counter 2 4)\n   (deref counter))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("compare-and-set!", args, 3);
				
				final VncAtom atm = Coerce.toVncAtom(args.first());		
				
				return atm.compare_and_set(args.nth(1), args.nth(2));
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
					.arglists("(agent state options)")		
					.doc(
						"Creates and returns an agent with an initial value of state and " +
						"zero or more options. \n" +
						"  :error-handler handler-fn \n" +
						"  :error-mode mode-keyword \n" +
						"The handler-fn is called if an action throws an exception. It's a" +
						"function taking two args the agent and the exception. The " +
						"mode-keyword may be either :continue (the default) or :fail")
					.examples(
						"(do                         \n" +
						"   (def x (agent 100))      \n" +
						"   (send x + 5)             \n" +
						"   (sleep 100)              \n" +
						"   (deref x))                 ")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				JavaInterop.getInterceptor().validateBlackListedVeniceFunction("agent");
	
				assertMinArity("agent", args, 1);
					
				return new VncJavaObject(new Agent(args.first(), args.rest()));
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
						"(do                         \n" +
						"   (def x (agent 100))      \n" +
						"   (send x + 5)             \n" +
						"   (sleep 100)              \n" +
						"   (deref x))                 ")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				JavaInterop.getInterceptor().validateBlackListedVeniceFunction("send");
	
				assertMinArity("send", args, 2);
				
				if (Types.isVncJavaObject(args.first(), Agent.class)) {
					final Agent agent = (Agent)Coerce.toVncJavaObject(args.first()).getDelegate();
					final VncFunction fn = Coerce.toVncFunction(args.nth(1));		
					final VncList fnArgs = args.slice(2);		
					
					agent.send(fn, fnArgs);
					return args.first();
				}
				else {
					throw new VncException(String.format(
							"Function 'send' does not allow type %s as agent parameter",
							Types.getClassName(args.first())));
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
						"(do                         \n" +
						"   (def x (agent 100))      \n" +
						"   (send-off x + 5)         \n" +
						"   (sleep 100)              \n" +
						"   (deref x))                 ")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				JavaInterop.getInterceptor().validateBlackListedVeniceFunction("send-off");
	
				assertArity("send-off", args, 3);
				
				if (Types.isVncJavaObject(args.first(), Agent.class)) {
					final Agent agent = (Agent)Coerce.toVncJavaObject(args.first()).getDelegate();
					final VncFunction fn = Coerce.toVncFunction(args.nth(1));		
					final VncList fnArgs = args.slice(2);		
					
					agent.send_off(fn, fnArgs);
					return args.first();
				}
				else {
					throw new VncException(String.format(
							"Function 'send-off' does not allow type %s as agent parameter",
							Types.getClassName(args.first())));
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
					.build()
		) {		
			public VncVal apply(final VncList args) {
				JavaInterop.getInterceptor().validateBlackListedVeniceFunction("restart-agent");
	
				assertArity("restart-agent", args, 2);
				
				if (Types.isVncJavaObject(args.first(), Agent.class)) {
					final Agent agent = (Agent)Coerce.toVncJavaObject(args.first()).getDelegate();
					final VncVal state = args.nth(1);		
					
					agent.restart(state);
					return args.first();
				}
				else {
					throw new VncException(String.format(
							"Function 'restart-agent' does not allow type %s as agent parameter",
							Types.getClassName(args.first())));
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
					.build()
		) {	
			public VncVal apply(final VncList args) {
				JavaInterop.getInterceptor().validateBlackListedVeniceFunction("set-error-handler!");
	
				assertArity("set-error-handler!", args, 2);
				
				if (Types.isVncJavaObject(args.first(), Agent.class)) {
					final Agent agent = (Agent)Coerce.toVncJavaObject(args.first()).getDelegate();
					agent.setErrorHandler(Coerce.toVncFunction(args.nth(1)));
					return args.first();
				}
				else {
					throw new VncException(String.format(
							"Function 'set-error-handler!' does not allow type %s as agent parameter",
							Types.getClassName(args.first())));
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
					.build()
		) {	
			public VncVal apply(final VncList args) {
				JavaInterop.getInterceptor().validateBlackListedVeniceFunction("agent-error");
	
				assertArity("agent-error", args, 1);
				
				if (Types.isVncJavaObject(args.first(), Agent.class)) {
					final Agent agent = (Agent)Coerce.toVncJavaObject(args.first()).getDelegate();
					final RuntimeException ex = agent.getError();
					return ex == null ? Nil : new VncJavaObject(ex);
				}
				else {
					throw new VncException(String.format(
							"Function 'agent-error' does not allow type %s as agent parameter",
							Types.getClassName(args.first())));
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
					.build()
		) {	
			public VncVal apply(final VncList args) {
				JavaInterop.getInterceptor().validateBlackListedVeniceFunction("agent-error-mode");
	
				assertArity("agent-error-mode", args, 1);
				
				if (Types.isVncJavaObject(args.first(), Agent.class)) {
					final Agent agent = (Agent)Coerce.toVncJavaObject(args.first()).getDelegate();
					return agent.getErrorMode();
				}
				else {
					throw new VncException(String.format(
							"Function 'agent-error-mode' does not allow type %s as agent parameter",
							Types.getClassName(args.first())));
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
						"(do                           \n" +
						"   (def x1 (agent 100))       \n" +
						"   (def x2 (agent 100))       \n" +
						"   (await x1 x2))               ")
					.build()
		) {
			public VncVal apply(final VncList args) {
				JavaInterop.getInterceptor().validateBlackListedVeniceFunction("await");
	
				assertMinArity("await", args, 1);
		
				final List<Agent> agents = args.getList()
											   .stream()
											   .map(a -> (Agent)Coerce.toVncJavaObject(a).getDelegate())
											   .collect(Collectors.toList());
				
				return agents.isEmpty() 
						? True
						: Agent.await(agents, -1) ? True : False;
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
						"(do                           \n" +
						"   (def x1 (agent 100))       \n" +
						"   (def x2 (agent 100))       \n" +
						"   (await-for 500 x1 x2))       ")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				JavaInterop.getInterceptor().validateBlackListedVeniceFunction("await-for");
	
				assertMinArity("await-for", args, 2);
		
				final long timeoutMillis = Coerce.toVncLong(args.first()).getValue();
				final List<Agent> agents = args.rest()
											   .getList()
											   .stream()
											   .map(a -> (Agent)Coerce.toVncJavaObject(a).getDelegate())
											   .collect(Collectors.toList());
				
				return agents.isEmpty() 
						? True
						: Agent.await(agents, timeoutMillis) ? True : False;
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction shutdown_agents = 
		new VncFunction(
				"shutdown-agents", 
				VncFunction
					.meta()
					.arglists("(shutdown-agents )")		
					.doc(
						"Initiates a shutdown of the thread pools that back the agent " + 
						"system. Running actions will complete, but no new actions will been " + 
						"accepted")
					.examples(
						"(do                           \n" +
						"   (def x1 (agent 100))       \n" +
						"   (def x2 (agent 100))       \n" +
						"   (shutdown-agents ))          ")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				JavaInterop.getInterceptor().validateBlackListedVeniceFunction("shutdown-agents");
	
				assertArity("shutdown-agents", args, 0);
		
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
						"   (shutdown-agents )         \n" +
						"   (sleep 300)                \n" +
						"   (shutdown-agents? ))         ")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				JavaInterop.getInterceptor().validateBlackListedVeniceFunction("shutdown-agents?");
	
				assertArity("shutdown-agents?", args, 0);
		
				return Agent.isShutdown() ? True : False;
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction await_termination_agents = 
		new VncFunction(
				"await-termination-agents", 
				VncFunction
					.meta()
					.arglists("(shutdown-agents )")		
					.doc(
						"Blocks until all actions have completed execution after a shutdown " +
						"request, or the timeout occurs, or the current thread is " +
						"interrupted, whichever happens first.")
					.examples(
						"(do                                   \n" +
						"   (def x1 (agent 100))               \n" +
						"   (def x2 (agent 100))               \n" +
						"   (shutdown-agents )                 \n" +
						"   (await-termination-agents 1000))     ")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				JavaInterop.getInterceptor().validateBlackListedVeniceFunction("await-termination-agents");
	
				assertArity("await-termination-agents", args, 1);
	
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
						"   (shutdown-agents )                \n" +
						"   (await-termination-agents 1000))  \n" +
						"   (sleep 300)                       \n" +
						"   (await-termination-agents? ))      ")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				JavaInterop.getInterceptor().validateBlackListedVeniceFunction("await-termination-agents?");
	
				assertArity("await-termination-agents?", args, 0);
		
				return Agent.isShutdown() ? True : False;
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
						"   (deliver p 123))")
					.build()
		) {
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
						"(do                                        \n" +
						"   (def p (promise))                       \n" +
						"   (def task (fn []                        \n" +
						"                 (do                       \n" +
						"                    (sleep 500)            \n" +
						"                    (deliver p 123))))     \n" +
						"                                           \n" +
						"   (future task)                           \n" +
						"   (deref p))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				JavaInterop.getInterceptor().validateBlackListedVeniceFunction("promise");
	
				assertArity("promise", args, 0);
	
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
				JavaInterop.getInterceptor().validateBlackListedVeniceFunction("promise?");
	
				assertArity("promise?", args, 1);
	
				return Types.isVncJavaObject(args.first(), CompletableFuture.class) ? True : False;
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
						"Takes a function and yields a future object that will " + 
						"invoke the function in another thread, and will cache the result and " + 
						"return it on all subsequent calls to deref. If the computation has " + 
						"not yet finished, calls to deref will block, unless the variant of " + 
						"deref with timeout is used.")
					.examples(
						"(do                                         \n" + 
						"   (def wait (fn [] (do (sleep 500) 100)))  \n" + 
						"                                            \n" + 
						"   (let [f (future wait)]                   \n" + 
						"        (deref f))                          \n" + 
						")")
					.build()
		) {		
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
													VncHashMap.of(new VncKeyword("call"), wrapped));
	
				final IInterceptor parentInterceptor = JavaInterop.getInterceptor();
				
				final Callable<Object> taskWrapper = () -> {
					try {
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
				
				final Future<Object> future = executor.submit(taskWrapper);
				
				return new VncJavaObject(future);
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
				JavaInterop.getInterceptor().validateBlackListedVeniceFunction("future?");
	
				assertArity("future?", args, 1);
	
				return Types.isVncJavaObject(args.first(), Future.class) ? True : False;
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
					.examples("(future-done? (future (fn [] 100)))")
					.build()
		) {		
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

	public static VncFunction future_cancel = 
		new VncFunction(
				"future-cancel", 
				VncFunction
					.meta()
					.arglists("(future-cancel f)")		
					.doc("Cancels the future")
					.examples("(future-cancel (future (fn [] 100)))")
					.build()
		) {	
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

	public static VncFunction future_cancelled_Q = 
		new VncFunction(
				"future-cancelled?", 
				VncFunction
					.meta()
					.arglists("(future-cancelled? f)")		
					.doc("Returns true if f is a Future is cancelled otherwise false")
					.examples("(future-cancelled? (future (fn [] 100)))")
					.build()
		) {	
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
				assertArity("delay?", args, 1);
	
				return Types.isVncJavaObject(args.first(), Delay.class) ? True : False;
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
						"   (force x))                                      ")
					.build()
		) {	
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

	public static VncFunction new_thread_local = 
		new VncFunction(
				"thread-local", 
				VncFunction
					.meta()
					.arglists("(thread-local)")		
					.doc("Creates a new thread-local accessor")
					.examples(
						"(thread-local :a 1 :b 2)", 
						"(thread-local { :a 1 :b 2 })",
						"(do \n" +
						"   (thread-local-clear) \n" +
						"   (assoc (thread-local) :a 1 :b 2) \n" +
						"   (dissoc (thread-local) :a) \n" +
						"   (get (thread-local) :b 100) \n" +
						")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				if (args.size() == 1 && Types.isVncMap(args.first())) {
					return new VncThreadLocal(((VncMap)args.first()).getMap());
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
					.examples("(do\n   (def x (thread-local))\n   (thread-local? x))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("thread-local?", args, 1);
				
				return Types.isVncThreadLocal(args.first()) ? True : False;
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
					.build()
		) {		
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
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("thread-id", args, 0);
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
					.build()
		) {		
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
					.put("realized?",	 		realized_Q)
					
					.put("add-watch",	 		add_watch)
					.put("remove-watch",	 	remove_watch)

					.put("atom",				new_atom)
					.put("atom?",				atom_Q)
					.put("reset!",				reset_BANG)
					.put("swap!",				swap_BANG)
					.put("compare-and-set!", 	compare_and_set_BANG)
					
					.put("agent", 				agent)
					.put("send",				send)
					.put("send-off",			send_off)
					.put("restart-agent",		restart_agent)
					.put("set-error-handler!",	set_error_handler)
					.put("agent-error",			agent_error)
					.put("agent-error-mode",	agent_error_mode)
					.put("await",				await)
					.put("await-for",			await_for)
					.put("shutdown-agents",		shutdown_agents)
					.put("shutdown-agents?",		shutdown_agents_Q)
					.put("await-termination-agents",	await_termination_agents)
					.put("await-termination-agents?",	await_termination_agents_Q)
										
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
