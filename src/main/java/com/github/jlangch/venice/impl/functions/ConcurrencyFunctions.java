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
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.github.jlangch.venice.SecurityException;
import com.github.jlangch.venice.ValueException;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.thread.ThreadBridge;
import com.github.jlangch.venice.impl.types.IDeref;
import com.github.jlangch.venice.impl.types.IVncFunction;
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
import com.github.jlangch.venice.impl.types.collections.VncLazySeq;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.concurrent.Agent;
import com.github.jlangch.venice.impl.types.concurrent.Delay;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.CallFrame;
import com.github.jlangch.venice.impl.util.MetaUtil;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;
import com.github.jlangch.venice.impl.util.concurrent.ManagedCachedThreadPoolExecutor;


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
                        "timeout can be used for futures and will return `timeout-val` " +
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
            @Override
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
                                final VncVal v = future.get();
                                return v == null ? Nil : v;
                            }
                            else {
                                final long timeout = Coerce.toVncLong(args.second()).getValue();
                                final VncVal v = future.get(timeout, TimeUnit.MILLISECONDS);
                                return v == null ? Nil : v;
                            }
                        }
                        catch(TimeoutException ex) {
                            return args.size() == 3 ? args.third() : Nil;
                        }
                        catch(ExecutionException | CompletionException ex) {
                            if (ex.getCause() != null) {
                                // just unwrap SecurityException and VncException
                                if (ex.getCause() instanceof SecurityException) {
                                    throw (SecurityException)ex.getCause();
                                }
                                else if (ex.getCause() instanceof TimeoutException) {
                                    if (args.size() == 3) {
                                        return args.third();
                                    }
                                    else {
                                        throw new com.github.jlangch.venice.TimeoutException(ex.getCause());
                                    }
                                }
                                else if (ex.getCause() instanceof VncException) {
                                    throw (VncException)ex.getCause();
                                }
                            }

                            throw new VncException("Future execution failure", ex);
                        }
                        catch(CancellationException ex) {
                            throw new VncException("Future has been cancelled", ex);
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
            @Override
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
            @Override
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
            @Override
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
            @Override
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
                        "Options: ¶\n" +
                        "&ensp; :meta metadata-map ¶\n" +
                        "&ensp; :validator validate-fn \n\n" +
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
            @Override
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
            @Override
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
            @Override
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
                        "`(apply f current-value-of-box args)`. Note that f may be called " +
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
            @Override
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
                        "`(apply f current-value-of-atom args)`. Note that f may be called " +
                        "multiple times, and thus should be free of side effects. " +
                        "Returns [old new], the value of the atom before and after the swap.")
                    .examples(
                        "(do                                \n" +
                        "   (def queue (atom '(1 2 3)))     \n" +
                        "   (swap-vals! queue pop))           ")
                .seeAlso("swap!", "reset!", "compare-and-set!", "atom", "volatile")
                .build()
        ) {
            @Override
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
            @Override
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
            @Override
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
            @Override
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
                        "Options: ¶\n" +
                        "&ensp; :error-handler handler-fn ¶\n" +
                        "&ensp; :error-mode mode-keyword ¶\n" +
                        "&ensp; :validator validate-fn \n\n" +
                        "The `handler-fn` is called if an action throws an exception. It's a " +
                        "function taking two args the agent and the exception. The " +
                        "mode-keyword may be either :continue (the default) or :fail " +
                        "The `validate-fn` must be nil or a side-effect-free fn of one " +
                        "argument, which will be passed the intended new state on any state " +
                        "change. If the new state is unacceptable, the `validate-fn` should " +
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
            @Override
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
                        "Dispatch an action to an agent. Returns the agent immediately.\n\n" +
                        "The state of the agent will be set to the value of:¶\n" +
                        "&ensp; `(apply action-fn state-of-agent args)`")
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
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 2);

                sandboxFunctionCallValidation();

                if (Types.isVncJavaObject(args.first(), Agent.class)) {
                    final Agent agent = (Agent)Coerce.toVncJavaObject(args.first()).getDelegate();
                    final VncFunction fn = Coerce.toVncFunction(args.second());
                    final VncList fnArgs = args.slice(2);

                    agent.send(new CallFrame(this, args), fn, fnArgs);
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
                        "the agent immediately.\n\n" +
                        "The state of the agent will be set to the value of:¶\n" +
                        "&ensp; `(apply action-fn state-of-agent args)`")
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
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 2);

                sandboxFunctionCallValidation();

                if (Types.isVncJavaObject(args.first(), Agent.class)) {
                    final Agent agent = (Agent)Coerce.toVncJavaObject(args.first()).getDelegate();
                    final VncFunction fn = Coerce.toVncFunction(args.second());
                    final VncList fnArgs = args.slice(2);

                    agent.send_off(new CallFrame(this, args), fn, fnArgs);
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
            @Override
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
                        "Sets the error-handler of an agent to `handler-fn`. If an action " +
                        "being run by the agent throws an exception `handler-fn` will be " +
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
            @Override
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
                        "agent if the agent is failed. Returns `nil` if the agent is not " +
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
            @Override
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
            @Override
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
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                sandboxFunctionCallValidation();

                final List<Agent> agents = args.stream()
                                               .map(a -> (Agent)Coerce.toVncJavaObject(a).getDelegate())
                                               .collect(Collectors.toList());

                return agents.isEmpty()
                        ? True
                        : VncBoolean.of(
                                Agent.await(
                                        new CallFrame(this, args),
                                        agents,
                                        -1));
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
            @Override
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
                        : VncBoolean.of(
                                Agent.await(
                                        new CallFrame(this, args),
                                        agents,
                                        timeoutMillis));
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
            @Override
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
            @Override
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
            @Override
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
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                return VncBoolean.of(Agent.isShutdown());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction agent_send_thread_pool_info =
        new VncFunction(
                "agent-send-thread-pool-info",
                VncFunction
                    .meta()
                    .arglists("(agent-send-thread-pool-info)")
                    .doc(
                        "Returns the thread pool info of the ThreadPoolExecutor serving " +
                        "agent send.\n\n" +
                        "| *core-pool-size*       |  the number of threads to keep in the pool, " +
                        "                            even if they are idle |\n" +
                        "| *maximum-pool-size*    |  the maximum allowed number of threads |\n" +
                        "| *current-pool-size*    |  the current number of threads in the pool |\n" +
                        "| *largest-pool-size*    |  the largest number of threads that have " +
                        "                            ever simultaneously been in the pool |\n" +
                        "| *active-thread-count*  |  the approximate number of threads that are " +
                        "                            actively executing tasks |\n" +
                        "| *scheduled-task-count* |  the approximate total number of tasks that " +
                        "                            have ever been scheduled for execution |\n" +
                        "| *completed-task-count* |  the approximate total number of tasks " +
                        "                            that have completed execution |")
                    .examples(
                        "(agent-send-thread-pool-info)")
                    .seeAlso("agent", "send")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                return Agent.sendExecutorInfo();
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction agent_send_off_thread_pool_info =
        new VncFunction(
                "agent-send-off-thread-pool-info",
                VncFunction
                    .meta()
                    .arglists("(agent-send-off-thread-pool-info)")
                    .doc(
                        "Returns the thread pool info of the ThreadPoolExecutor serving " +
                        "agent send-off.\n\n" +
                        "| *core-pool-size*       |  the number of threads to keep in the pool, " +
                        "                            even if they are idle |\n" +
                        "| *maximum-pool-size*    |  the maximum allowed number of threads |\n" +
                        "| *current-pool-size*    |  the current number of threads in the pool |\n" +
                        "| *largest-pool-size*    |  the largest number of threads that have " +
                        "                            ever simultaneously been in the pool |\n" +
                        "| *active-thread-count*  |  the approximate number of threads that are " +
                        "                            actively executing tasks |\n" +
                        "| *scheduled-task-count* |  the approximate total number of tasks that " +
                        "                            have ever been scheduled for execution |\n" +
                        "| *completed-task-count* |  the approximate total number of tasks " +
                        "                            that have completed execution |")
                    .examples(
                        "(agent-send-off-thread-pool-info)")
                    .seeAlso("agent", "send-off")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                return Agent.sendOffExecutorInfo();
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
                        "(do                            \n" +
                        "   (def p (promise))           \n" +
                        "   (deliver p 10)              \n" +
                        "   (deliver p 20) ; no effect  \n" +
                        "   @p)                           ")
                    .seeAlso("deliver-ex", "promise", "realized?")
                    .build()
        ) {
            @Override
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

    public static VncFunction deliver_ex =
        new VncFunction(
                "deliver-ex",
                VncFunction
                    .meta()
                    .arglists("(deliver-ex ref ex)")
                    .doc(
                        "Delivers the supplied exception to the promise, releasing any pending " +
                        "derefs. A subsequent call to deliver on a promise will have no effect.")
                    .examples(
                        "(do                                             \n" +
                        "   (def p (promise))                            \n" +
                        "   (deliver-ex p (ex :VncException \"error\"))  \n" +
                        "   (deliver p 20)  ; no effect                  \n" +
                        "   (try                                         \n" +
                        "     @p                                         \n" +
                        "     (catch :VncException e (ex-message e))))   ")
                    .seeAlso("deliver", "promise", "realized?")
                    .build()
        ) {
            @Override
            @SuppressWarnings("unchecked")
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                sandboxFunctionCallValidation();

                final Object promise = Coerce.toVncJavaObject(args.first()).getDelegate();
                final VncVal value = args.second();

                if (promise instanceof CompletableFuture) {
                    CompletableFuture<VncVal> cf = (CompletableFuture<VncVal>)promise;

                    if (value instanceof VncJavaObject) {
                        final Object delegate = ((VncJavaObject)value).getDelegate();
                        if (delegate instanceof VncException) {
                            cf.completeExceptionally((VncException)delegate);
                        }
                        else if (delegate instanceof Exception) {
                            cf.completeExceptionally((Exception)delegate);
                        }
                        else {
                            cf.completeExceptionally(new ValueException(value));
                        }
                    }
                    else {
                        cf.completeExceptionally(new ValueException(value));
                    }
                    return Nil;
                }
                else {
                    throw new VncException(String.format(
                            "Function 'deliver-ex' does not allow type %s as parameter",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    // see also: https://github.com/funcool/promesa   (promise chaining)
    //           https://dzone.com/articles/20-examples-of-using-javas-completablefuture
    //
    // Note: Java 9 has added orTimeout and completeOnTimeout methods on CompletableFuture
    public static VncFunction promise =
        new VncFunction(
                "promise",
                VncFunction
                    .meta()
                    .arglists(
                        "(promise)",
                        "(promise fn)")
                    .doc(
                        "Returns a promise object that can be read with deref, and set, " +
                        "once only, with deliver. Calls to deref prior to delivery will " +
                        "block, unless the variant of deref with timeout is used. All " +
                        "subsequent derefs will return the same delivered value without " +
                        "blocking.\n\n" +
                        "Promises are implemented on top of Java's `CompletableFuture`.")
                    .examples(
                        "(do                              \n" +
                        "   (def p (promise))             \n" +
                        "   (deliver p 10)                \n" +
                        "   (deliver p 20) ; no effect    \n" +
                        "   @p)                             ",

                        ";; deliver the promise from a future           \n" +
                        "(do                                            \n" +
                        "   (def p (promise))                           \n" +
                        "   (defn task1 [] (sleep 500) (deliver p 10))  \n" +
                        "   (defn task2 [] (sleep 800) (deliver p 20))  \n" +
                        "   (future task1)                              \n" +
                        "   (future task2)                              \n" +
                        "   @p)                                           ",

                        ";; deliver the promise from a task's return value    \n" +
                        "(do                                                  \n" +
                        "   (defn task [] (sleep 300) 10)                     \n" +
                        "   (def p (promise task))                            \n" +
                        "   @p)                                                ",

                        "(let [p (promise #(do (sleep 300) 10))]              \n" +
                        "   @p)                                                 ")
                    .seeAlso(
                        "deliver", "promise?", "realized?", "deref",
                        "done?", "cancel", "cancelled?",
                        "all-of", "any-of",
                        "then-accept", "then-accept-both", "then-apply", "then-combine",
                        "then-compose", "when-complete", "accept-either", "apply-to-either",
                        "or-timeout", "complete-on-timeout", "timeout-after")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0, 1);

                sandboxFunctionCallValidation();

                if (args.isEmpty()) {
                    return new VncJavaObject(new CompletableFuture<VncVal>());
                }
                else {
                    final VncVal arg = args.first();
                    if (arg instanceof VncFunction) {
                        final VncFunction fn = Coerce.toVncFunction(args.first());

                        final CallFrame[] cf = new CallFrame[] {
                                                    new CallFrame(this, args),
                                                    new CallFrame(fn) };

                        // Create a wrapper that inherits the Venice thread context
                        // from the parent thread to the executer thread!
                        final ThreadBridge threadBridge = ThreadBridge.create("promise", cf);
                        final Supplier<VncVal> taskWrapper = threadBridge.bridgeSupplier(() -> fn.applyOf());

                        return new VncJavaObject(
                                        CompletableFuture.supplyAsync(
                                                taskWrapper,
                                                mngdExecutor.getExecutor()));
                    }
                    else {
                        throw new VncException(String.format(
                                "Function 'promise' does not allow type %s argument",
                                Types.getType(args.first())));
                    }
                }
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
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(Types.isVncJavaObject(args.first(), CompletableFuture.class));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction all_of =
        new VncFunction(
                "all-of",
                VncFunction
                    .meta()
                    .arglists("(all-of p & ps)")
                    .doc(
                        "Returns a new promise that is completed when all of the " +
                        "given promises complete. If any of the given promises " +
                        "complete exceptionally, then the returned promise also does " +
                        "so. Otherwise, the results, if any, of the given promises are " +
                        "not reflected in the returned promise, but may be obtained " +
                        "by inspecting them individually.")
                    .examples(
                        "(-> (all-of (promise (fn [] (sleep 100) 1))  \n" +
                        "            (promise (fn [] (sleep 100) 2))  \n" +
                        "            (promise (fn [] (sleep 500) 3))) \n" +
                        "    (deref))")
                    .seeAlso(
                        "promise", "any-of")
                    .build()
        ) {
            @Override
            @SuppressWarnings("unchecked")
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                final List<CompletableFuture<VncVal>> cfs = new ArrayList<>();

                for(VncVal v : args) {
                    cfs.add(Coerce.toVncJavaObject(
                                                        v,
                                                        CompletableFuture.class));
                }

                final CompletableFuture<Void> cf2 = CompletableFuture.allOf(cfs.toArray(new CompletableFuture[0]));

                return new VncJavaObject(cf2);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction any_of =
        new VncFunction(
                "any-of",
                VncFunction
                    .meta()
                    .arglists("(any-of p & ps)")
                    .doc(
                        "Returns a new promise that is completed when any of the " +
                        "given promises complete, with the same result. Otherwise, " +
                        "if it completed exceptionally, the returned promise also " +
                        "does so.")
                    .examples(
                        "(-> (any-of (promise (fn [] (sleep 300) 1))  \n" +
                        "            (promise (fn [] (sleep 100) 2))  \n" +
                        "            (promise (fn [] (sleep 500) 3))) \n" +
                        "    (deref))")
                    .seeAlso(
                        "promise", "all-of")
                    .build()
        ) {
            @Override
            @SuppressWarnings("unchecked")
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                final List<CompletableFuture<VncVal>> cfs = new ArrayList<>();

                for(VncVal v : args) {
                    cfs.add(Coerce.toVncJavaObject(
                                                        v,
                                                        CompletableFuture.class));
                }

                final CompletableFuture<Object> cf2 = CompletableFuture.anyOf(cfs.toArray(new CompletableFuture[0]));

                return new VncJavaObject(cf2);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction then_accept =
        new VncFunction(
                "then-accept",
                VncFunction
                    .meta()
                    .arglists("(then-accept p f)")
                    .doc(
                        "Returns a new promise that, when this promise completes normally, " +
                        "is executing the function f with this stage's result as the argument.")
                    .examples(
                        "(-> (promise (fn [] \"the quick brown fox\"))    \n" +
                        "    (then-accept (fn [v] (println (pr-str v))))  \n" +
                        "    (deref))")
                    .seeAlso(
                        "promise", "then-accept-both", "then-apply", "then-combine",
                        "then-compose", "when-complete", "accept-either", "apply-to-either",
                        "or-timeout", "complete-on-timeout")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                @SuppressWarnings("unchecked")
                final CompletableFuture<VncVal> cf = Coerce.toVncJavaObject(
                                                                                    args.first(),
                                                                                    CompletableFuture.class);
                final VncFunction fn = Coerce.toVncFunction(args.second());

                final ThreadBridge threadBridge = ThreadBridge.create(
                                                    "then-accept",
                                                    new CallFrame[] {
                                                        new CallFrame(this, args),
                                                        new CallFrame(fn)});
                final Consumer<VncVal> taskWrapper = threadBridge.bridgeConsumer((VncVal v) -> fn.applyOf(v));

                final CompletableFuture<Void> cf2 = cf.thenAcceptAsync(
                                                            taskWrapper,
                                                            mngdExecutor.getExecutor());

                return new VncJavaObject(cf2);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction then_apply =
        new VncFunction(
                "then-apply",
                VncFunction
                    .meta()
                    .arglists("(then-apply p f)")
                    .doc(
                        "Applies a function f on the result of the previous stage of the promise p.")
                    .examples(
                        "(-> (promise (fn [] \"the quick brown fox\"))           \n" +
                        "    (then-apply str/upper-case)                         \n" +
                        "    (then-apply #(str % \" jumps over the lazy dog\"))  \n" +
                        "    (deref))")
                    .seeAlso(
                        "promise", "then-accept", "then-accept-both", "then-combine",
                        "then-compose", "when-complete", "accept-either", "apply-to-either",
                        "or-timeout", "complete-on-timeout")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                @SuppressWarnings("unchecked")
                final CompletableFuture<VncVal> cf = Coerce.toVncJavaObject(
                                                                                    args.first(),
                                                                                    CompletableFuture.class);
                final VncFunction fn = Coerce.toVncFunction(args.second());

                final ThreadBridge threadBridge = ThreadBridge.create(
                                                    "then-apply",
                                                    new CallFrame[] {
                                                        new CallFrame(this, args),
                                                        new CallFrame(fn)});
                final Function<VncVal,VncVal> taskWrapper = threadBridge.bridgeFunction((VncVal v) -> fn.applyOf(v));

                final CompletableFuture<VncVal> cf2 = cf.thenApplyAsync(
                                                            taskWrapper,
                                                            mngdExecutor.getExecutor());

                return new VncJavaObject(cf2);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction then_combine =
        new VncFunction(
                "then-combine",
                VncFunction
                    .meta()
                    .arglists("(then-combine p p-other f)")
                    .doc(
                        "Applies a function f to the result of the previous stage of promise p " +
                        "and the result of another promise p-other")
                    .examples(
                        "(-> (promise (fn [] \"The Quick Brown Fox\"))                           \n" +
                        "    (then-apply str/upper-case)                                         \n" +
                        "    (then-combine (-> (promise (fn [] \"Jumps Over The Lazy Dog\"))     \n" +
                        "                      (then-apply str/lower-case))                      \n" +
                        "                  #(str %1 \" \" %2))                                   \n" +
                        "    (deref))")
                    .seeAlso(
                        "promise", "then-accept", "then-accept-both", "then-apply",
                        "then-compose", "when-complete", "accept-either", "apply-to-either",
                        "or-timeout", "complete-on-timeout")
                    .build()
        ) {
            @Override
            @SuppressWarnings("unchecked")
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 3);

                final CompletableFuture<VncVal> cf = Coerce.toVncJavaObject(
                                                                                    args.first(),
                                                                                    CompletableFuture.class);

                final CompletableFuture<VncVal> cf2 = Coerce.toVncJavaObject(
                                                                                    args.second(),
                                                                                    CompletableFuture.class);

                final VncFunction fn = Coerce.toVncFunction(args.third());

                final ThreadBridge threadBridge = ThreadBridge.create(
                                                    "then-combine",
                                                    new CallFrame[] {
                                                        new CallFrame(this, args),
                                                        new CallFrame(fn)});

                final BiFunction<VncVal,VncVal,VncVal> taskWrapper = threadBridge.bridgeBiFunction(
                                                                        (VncVal v1, VncVal v2) -> fn.applyOf(v1, v2));

                final CompletableFuture<VncVal> cf3 = cf.thenCombineAsync(
                                                            cf2,
                                                            taskWrapper,
                                                            mngdExecutor.getExecutor());

                return new VncJavaObject(cf3);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction then_compose =
        new VncFunction(
                "then-compose",
                VncFunction
                    .meta()
                    .arglists("(then-compose p f)")
                    .doc(
                        "Composes the result of two promises. f receives the result of the first promise p " +
                        "and returns a new promise that composes that value with this promise. ")
                    .examples(
                        "(-> (promise (fn [] \"The Quick Brown Fox\"))                                \n" +
                        "    (then-apply str/upper-case)                                              \n" +
                        "    (then-compose (fn [x] (-> (promise (fn [] \"Jumps Over The Lazy Dog\"))  \n" +
                        "                              (then-apply str/lower-case)                    \n" +
                        "                              (then-apply #(str x \" \" %1)))))              \n" +
                        "    (deref))")
                    .seeAlso(
                        "promise", "then-accept", "then-accept-both", "then-apply", "then-combine",
                        "when-complete", "accept-either", "apply-to-either",
                        "or-timeout", "complete-on-timeout")
                    .build()
        ) {
            @Override
            @SuppressWarnings("unchecked")
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final CompletableFuture<VncVal> cf = Coerce.toVncJavaObject(
                                                                                    args.first(),
                                                                                    CompletableFuture.class);

                final VncFunction fn = Coerce.toVncFunction(args.second());

                final ThreadBridge threadBridge = ThreadBridge.create(
                                                    "then-compose",
                                                    new CallFrame[] {
                                                        new CallFrame(this, args),
                                                        new CallFrame(fn)});

                final Function<VncVal,CompletableFuture<VncVal>> taskWrapper = threadBridge.bridgeFunction(
                                                                        (VncVal v1) -> (CompletableFuture<VncVal>)((VncJavaObject)fn.applyOf(v1)).getDelegate());

                final CompletableFuture<VncVal> cf2 = cf.thenComposeAsync(
                                                            taskWrapper,
                                                            mngdExecutor.getExecutor());

                return new VncJavaObject(cf2);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction when_complete =
        new VncFunction(
                "when-complete",
                VncFunction
                    .meta()
                    .arglists("(when-complete p f)")
                    .doc(
                        "Returns the promise p with the same result or exception at this stage, " +
                        "that executes the action f. Passes the current stage's result value as " +
                        "first and a possible exception as second argument to the function. The " +
                        "asynchronous function f is called presumably for handling side effects.")
                    .examples(
                        "(-> (promise (fn [] \"The Quick Brown Fox\"))                       \n" +
                        "    (then-apply str/upper-case)                                     \n" +
                        "    (when-complete (fn [v,e] (println (pr-str {:value v :ex e}))))  \n" +
                        "    (then-apply str/lower-case)                                     \n" +
                        "    (deref))")
                    .seeAlso(
                        "promise", "then-accept", "then-accept-both", "then-apply", "then-combine",
                        "then-compose", "accept-either", "apply-to-either",
                        "or-timeout", "complete-on-timeout")
                    .build()
        ) {
            @Override
            @SuppressWarnings("unchecked")
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final CompletableFuture<VncVal> cf = Coerce.toVncJavaObject(
                                                                                    args.first(),
                                                                                    CompletableFuture.class);

                final VncFunction fn = Coerce.toVncFunction(args.second());

                final ThreadBridge threadBridge = ThreadBridge.create(
                                                    "when-complete",
                                                    new CallFrame[] {
                                                        new CallFrame(this, args),
                                                        new CallFrame(fn)});

                final BiConsumer<VncVal,Throwable> taskWrapper = threadBridge.bridgeBiConsumer(
                                                                        (VncVal v, Throwable th) -> fn.applyOf(
                                                                                                        v,
                                                                                                        th == null ? Nil : new VncJavaObject(th)));

                final CompletableFuture<VncVal> cf2 = cf.whenCompleteAsync(
                                                            taskWrapper,
                                                            mngdExecutor.getExecutor());

                return new VncJavaObject(cf2);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction accept_either =
        new VncFunction(
                "accept-either",
                VncFunction
                    .meta()
                    .arglists("(accept-either p p-other f)")
                    .doc(
                        "Returns a new promise that, when either this or the other " +
                        "given promise completess normally, is executed with the " +
                        "corresponding result as argument to the supplied function f.")
                    .examples(
                        "(-> (promise (fn [] (sleep 200) \"The quick brown fox\"))                      \n" +
                        "    (accept-either (promise (fn [] (sleep 100) \"jumps over the lazy dog\"))   \n" +
                        "                   (fn [v] (println (pr-str v))))                              \n" +
                        "    (deref))")
                    .seeAlso(
                        "promise", "then-accept", "then-accept-both", "then-apply", "then-combine",
                        "then-compose", "when-complete", "apply-to-either",
                        "or-timeout", "complete-on-timeout")
                    .build()
        ) {
            @Override
            @SuppressWarnings("unchecked")
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 3);

                final CompletableFuture<VncVal> cf = Coerce.toVncJavaObject(
                                                                                    args.first(),
                                                                                    CompletableFuture.class);

                final CompletableFuture<VncVal> cf2 = Coerce.toVncJavaObject(
                                                                                    args.second(),
                                                                                    CompletableFuture.class);

                final VncFunction fn = Coerce.toVncFunction(args.third());

                final ThreadBridge threadBridge = ThreadBridge.create(
                                                    "accept-either",
                                                    new CallFrame[] {
                                                        new CallFrame(this, args),
                                                        new CallFrame(fn)});

                final Consumer<VncVal> taskWrapper = threadBridge.bridgeConsumer((v) -> fn.applyOf(v));

                final CompletableFuture<Void> cf3 = cf.acceptEitherAsync(
                                                            cf2,
                                                            taskWrapper,
                                                            mngdExecutor.getExecutor());

                return new VncJavaObject(cf3);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction apply_to_either =
        new VncFunction(
                "apply-to-either",
                VncFunction
                    .meta()
                    .arglists("(apply-to-either p p-other f)")
                    .doc(
                        "Returns a new promise that, when either this or the other " +
                        "given promise completes normally, is executed with the " +
                        "corresponding result as argument to the supplied function f.")
                    .examples(
                        "(-> (promise (fn [] (sleep 200) \"The quick brown fox\"))                      \n" +
                        "    (apply-to-either (promise (fn [] (sleep 100) \"jumps over the lazy dog\")) \n" +
                        "                     (fn [v] (str/upper-case v)))                              \n" +
                        "    (deref))")
                    .seeAlso(
                        "promise", "then-accept", "then-accept-both", "then-apply", "then-combine",
                        "then-compose", "when-complete", "accept-either",
                        "or-timeout", "complete-on-timeout")
                    .build()
        ) {
            @Override
            @SuppressWarnings("unchecked")
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 3);

                final CompletableFuture<VncVal> cf = Coerce.toVncJavaObject(
                                                                                    args.first(),
                                                                                    CompletableFuture.class);

                final CompletableFuture<VncVal> cf2 = Coerce.toVncJavaObject(
                                                                                    args.second(),
                                                                                    CompletableFuture.class);

                final VncFunction fn = Coerce.toVncFunction(args.third());

                final ThreadBridge threadBridge = ThreadBridge.create(
                                                    "apply-to-either",
                                                    new CallFrame[] {
                                                        new CallFrame(this, args),
                                                        new CallFrame(fn)});

                final Function<VncVal,VncVal> taskWrapper = threadBridge.bridgeFunction((v) -> fn.applyOf(v));

                final CompletableFuture<VncVal> cf3 = cf.applyToEitherAsync(
                                                            cf2,
                                                            taskWrapper,
                                                            mngdExecutor.getExecutor());

                return new VncJavaObject(cf3);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction then_accept_both =
        new VncFunction(
                "then-accept-both",
                VncFunction
                    .meta()
                    .arglists("(then-accept-both p p-other f)")
                    .doc(
                        "Returns a new promise that, when either this or the other " +
                        "given promise completes normally, is executing the function f " +
                        "with the two results as arguments.")
                    .examples(
                        "(-> (promise (fn [] (sleep 200) \"The quick brown fox\"))                       \n" +
                        "    (then-accept-both (promise (fn [] (sleep 100) \"jumps over the lazy dog\")) \n" +
                        "                      (fn [u v] (println (pr-str (str u \" \" v)))))            \n" +
                        "    (deref))")
                    .seeAlso(
                            "promise", "then-accept", "then-apply", "apply-to-either", "then-combine",
                            "then-compose", "when-complete", "accept-either",
                            "or-timeout", "complete-on-timeout")
                    .build()
        ) {
            @Override
            @SuppressWarnings("unchecked")
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 3);

                final CompletableFuture<VncVal> cf = Coerce.toVncJavaObject(
                                                                                    args.first(),
                                                                                    CompletableFuture.class);

                final CompletableFuture<VncVal> cf2 = Coerce.toVncJavaObject(
                                                                                    args.second(),
                                                                                    CompletableFuture.class);

                final VncFunction fn = Coerce.toVncFunction(args.third());

                final ThreadBridge threadBridge = ThreadBridge.create(
                                                    "then-accept-both",
                                                    new CallFrame[] {
                                                        new CallFrame(this, args),
                                                        new CallFrame(fn)});

                final BiConsumer<VncVal,VncVal> taskWrapper = threadBridge.bridgeBiConsumer((u,v) -> fn.applyOf(u, v));

                final CompletableFuture<Void> cf3 = cf.thenAcceptBothAsync(
                                                            cf2,
                                                            taskWrapper,
                                                            mngdExecutor.getExecutor());

                return new VncJavaObject(cf3);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction timeout_after =
        new VncFunction(
                "timeout-after",
                VncFunction
                    .meta()
                    .arglists("(timeout-after p time time-unit)")
                    .doc(
                        "Returns a promise that timouts afer the specified time. The promise " +
                        "throws a TimeoutException.")
                    .examples(
                        "(-> (promise (fn [] (sleep 100) \"The quick brown fox\"))   \n" +
                        "    (accept-either (timeout-after 500 :milliseconds)        \n" +
                        "                   (fn [v] (println (pr-str v))))           \n" +
                        "    (deref))",
                        "(-> (promise (fn [] (sleep 1000) \"The quick brown fox\"))  \n" +
                        "    (accept-either (timeout-after 500 :milliseconds)        \n" +
                        "                   (fn [v] (println (pr-str v))))           \n" +
                        "    (deref))",
                        "(-> (promise (fn [] (sleep 1000) \"The quick brown fox\"))  \n" +
                        "    (accept-either (timeout-after 500 :milliseconds)        \n" +
                        "                   (fn [v] (println (pr-str v))))           \n" +
                        "    (deref 2000 :timeout))",
                        "(-> (promise (fn [] (sleep 200) \"The quick brown fox\"))   \n" +
                        "    (apply-to-either (timeout-after 100 :milliseconds)      \n" +
                        "                     identity)                              \n" +
                        "    (deref))")
                    .seeAlso(
                        "promise",
                        "then-accept", "then-accept-both", "then-apply", "then-combine",
                        "then-compose", "when-complete", "accept-either", "apply-to-either",
                        "or-timeout", "complete-on-timeout")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final VncLong time = Coerce.toVncLong(args.first());
                final VncKeyword unit = Coerce.toVncKeyword(args.second());

                return  new VncJavaObject(
                                timeoutAfter(
                                    time.getValue(),
                                    ScheduleFunctions.toTimeUnit(unit)));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction or_timeout =
        new VncFunction(
                "or-timeout",
                VncFunction
                    .meta()
                    .arglists("(or-timeout p time time-unit)")
                    .doc(
                        "Exceptionally completes the promise with a TimeoutException " +
                        "if not otherwise completed before the given timeout.")
                    .examples(
                        "(-> (promise (fn [] (sleep 100) \"The quick brown fox\"))   \n" +
                        "    (or-timeout 500 :milliseconds)                          \n" +
                        "    (then-apply str/upper-case)                             \n" +
                        "    (deref))",
                        "(-> (promise (fn [] (sleep 300) \"The quick brown fox\"))   \n" +
                        "    (or-timeout 200 :milliseconds)                          \n" +
                        "    (then-apply str/upper-case)                             \n" +
                        "    (deref))",
                        "(-> (promise (fn [] (sleep 300) \"The quick brown fox\"))   \n" +
                        "    (then-apply str/upper-case)                             \n" +
                        "    (or-timeout 200 :milliseconds)                          \n" +
                        "    (deref))")
                    .seeAlso(
                        "promise",
                        "then-accept", "then-accept-both", "then-apply", "then-combine",
                        "then-compose", "when-complete", "accept-either", "apply-to-either",
                        "complete-on-timeout")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 3);

                @SuppressWarnings("unchecked")
                final CompletableFuture<VncVal> cf = Coerce.toVncJavaObject(
                                                        args.first(),
                                                        CompletableFuture.class);

                final VncLong time = Coerce.toVncLong(args.second());
                final VncKeyword unit = Coerce.toVncKeyword(args.third());

                final CompletableFuture<VncVal> cf2 = timeoutAfter(
                                                            time.getValue(),
                                                            ScheduleFunctions.toTimeUnit(unit));

                final VncFunction fn = CoreFunctions.identity;

                final CompletableFuture<VncVal> cf3 = cf.applyToEitherAsync(
                                                            cf2,
                                                            (x) -> fn.applyOf(x),
                                                            mngdExecutor.getExecutor());

                return new VncJavaObject(cf3);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction complete_on_timeout =
        new VncFunction(
                "complete-on-timeout",
                VncFunction
                    .meta()
                    .arglists("(complete-on-timeout p value time time-unit)")
                    .doc(
                        "Completes the promise with the given value if not otherwise " +
                        "completed before the given timeout.")
                    .examples(
                        "(-> (promise (fn [] (sleep 100) \"The quick brown fox\"))             \n" +
                        "    (complete-on-timeout \"The fox did not jump\" 500 :milliseconds)  \n" +
                        "    (deref))",
                        "(-> (promise (fn [] (sleep 500) \"The quick brown fox\"))             \n" +
                        "    (complete-on-timeout \"The fox did not jump\" 100 :milliseconds)  \n" +
                        "    (deref))",
                        "(-> (promise (fn [] (sleep 500) \"The quick brown fox\"))             \n" +
                        "    (complete-on-timeout \"The fox did not jump\" 100 :milliseconds)  \n" +
                        "    (then-apply str/upper-case)                                       \n" +
                        "    (deref))",
                        "(-> (promise (fn [] (sleep 50) 100))             \n" +
                        "    (complete-on-timeout 888 100 :milliseconds)  \n" +
                        "    (then-apply #(do (sleep 200) (* % 3)))       \n" +
                        "    (complete-on-timeout 999 220 :milliseconds)  \n" +
                        "    (deref))")
                    .seeAlso(
                        "promise",
                        "then-accept", "then-accept-both", "then-apply", "then-combine",
                        "then-compose", "when-complete", "accept-either", "apply-to-either",
                        "or-timeout")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 4);

                @SuppressWarnings("unchecked")
                final CompletableFuture<VncVal> cf = Coerce.toVncJavaObject(
                                                        args.first(),
                                                        CompletableFuture.class);

                final VncVal timeoutVal = args.second();
                final VncLong time = Coerce.toVncLong(args.third());
                final VncKeyword unit = Coerce.toVncKeyword(args.fourth());

                final CompletableFuture<VncVal> cf2 = timeoutAfter(
                                                            timeoutVal,
                                                            time.getValue(),
                                                            ScheduleFunctions.toTimeUnit(unit));

                final VncFunction fn = CoreFunctions.identity;

                final CompletableFuture<VncVal> cf3 = cf.applyToEitherAsync(
                                                            cf2,
                                                            (x) -> fn.applyOf(x),
                                                            mngdExecutor.getExecutor());

                return new VncJavaObject(cf3);
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
                        "deref with timeout is used.\n\n" +
                        "Thread local vars will be inherited by the future child thread. Changes " +
                        "of the child's thread local vars will not be seen on the parent.")
                    .examples(
                        "(do                                       \n" +
                        "   (defn wait [] (sleep 300) 100)         \n" +
                        "   (let [f (future wait)]                 \n" +
                        "      (deref f)))                           ",

                        "(let [f (future #(do (sleep 300) 100))]   \n" +
                        "   (deref f))                               ",

                        "(do                                       \n" +
                        "   (defn wait [x] (sleep 300) (+ x 100))  \n" +
                        "   (let [f (future (partial wait 10))]    \n" +
                        "      (deref f)))                           ",

                        "(do                                       \n" +
                        "   (defn sum [x y] (+ x y))               \n" +
                        "   (let [f (future (partial sum 3 4))]    \n" +
                        "      (deref f)))                           ",

                        ";; demonstrates the use of thread locals with futures         \n" +
                        "(do                                                           \n" +
                        "   ;; parent thread locals                                    \n" +
                        "   (binding [a 10 b 20]                                       \n" +
                        "      ;; future with child thread locals                      \n" +
                        "      (let [f (future (fn [] (binding [b 90] {:a a :b b})))]  \n" +
                        "         {:child @f :parent {:a a :b b}})))                     ")
                    .seeAlso(
                        "deref", "realized?", "done?", "cancel", "cancelled?",
                        "future-task", "promise",
                        "futures-fork", "futures-wait")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                sandboxFunctionCallValidation();

                final VncFunction fn = Coerce.toVncFunction(args.first());

                final CallFrame[] cf = new CallFrame[] {
                                            new CallFrame(this, args),
                                            new CallFrame(fn) };

                // Create a wrapper that inherits the Venice thread context
                // from the parent thread to the executer thread!
                final ThreadBridge threadBridge = ThreadBridge.create("future", cf);
                final Callable<VncVal> taskWrapper = threadBridge.bridgeCallable(() -> fn.applyOf());

                // Note: Do NOT use a CompletableFuture
                //       Canceling a CompletableFuture does not interrupt the
                //       task wrapper!!!
                final Future<VncVal> future = mngdExecutor
                                                .getExecutor()
                                                .submit(taskWrapper);

                return new VncJavaObject(future);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction future_task =
        new VncFunction(
                "future-task",
                VncFunction
                    .meta()
                    .arglists(
                        "(future-task f completed-fn)",
                        "(future-task f sucess-fn failure-fn)")
                    .doc(
                        "Takes a function f without arguments and yields a future object that will " +
                        "invoke the function in another thread. " +
                        "\n\n" +
                        "If a single completed function is passed it will be called with the future " +
                        "as its argument as soon as the future has completed. If a success and a failure " +
                        "function are passed either the success or failure function will be called as " +
                        "soon as the future has completed. Upon success the success function will be " +
                        "called with the future's result as its argument, upon failure the failure " +
                        "function will be called with the exception as its argument." +
                        "\n\n" +
                        "In combination with a queue a completion service can be built. The tasks " +
                        "appear in the queue in the order they have completed." +
                        "\n\n" +
                        "Thread local vars will be inherited by the future child thread. Changes " +
                        "of the child's thread local vars will not be seen on the parent.")
                    .examples(
                        ";; building a completion service                                                  \n" +
                        ";; CompletionService = incoming worker queue + worker threads + output data queue \n" +
                        "(do                                                                               \n" +
                        "   (def q (queue 10))                                                             \n" +
                        "   (defn process [s v] (sleep s) v)                                               \n" +
                        "   (defn failure [s m] (sleep s) (throw (ex :VncException m)))                    \n" +
                        "   (future-task (partial process 200 2) #(offer! q %) #(offer! q %))              \n" +
                        "   (future-task (partial process 400 4) #(offer! q %) #(offer! q %))              \n" +
                        "   (future-task (partial process 100 1) #(offer! q %) #(offer! q %))              \n" +
                        "   (future-task (partial failure 300 \"Failed 3\") #(offer! q %) #(offer! q %))   \n" +
                        "   (println (poll! q 1000))                                                       \n" +
                        "   (println (poll! q 1000))                                                       \n" +
                        "   (println (poll! q 1000))                                                       \n" +
                        "   (println (poll! q 1000)))                                                        ",
                        ";; building a completion service (future-task API variant)                        \n" +
                        "(do                                                                               \n" +
                        "   (def q (queue 10))                                                             \n" +
                        "   (defn process [s v] (sleep s) v)                                               \n" +
                        "   (defn failure [s m] (sleep s) (throw (ex :VncException m)))                    \n" +
                        "   (defn print_result [f] (try (println @f) (catch :Exception e (println e))))    \n" +
                        "   (future-task (partial process 200 2) #(offer! q %))                            \n" +
                        "   (future-task (partial process 400 4) #(offer! q %))                            \n" +
                        "   (future-task (partial process 100 1) #(offer! q %))                            \n" +
                        "   (future-task (partial failure 300 \"Failed 3\") #(offer! q %))                 \n" +
                        "   (print_result (poll! q 1000))                                                  \n" +
                        "   (print_result (poll! q 1000))                                                  \n" +
                        "   (print_result (poll! q 1000))                                                  \n" +
                        "   (print_result (poll! q 1000)))                                                   ")
                    .seeAlso("future")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2, 3);

                sandboxFunctionCallValidation();

                final VncFunction taskFn = Coerce.toVncFunction(args.first());

                // Create a wrapper that inherits the Venice thread context from the parent thread to the executer thread!
                final ThreadBridge bridgedTask = bridge(taskFn, args);

                final Callable<VncVal> taskWrapper = bridgedTask.bridgeCallable(() -> taskFn.applyOf());


                if (args.size() == 2) {
                    final VncFunction onCompleteFn = Coerce.toVncFunction(args.second());

                    // Create a wrapper that inherits the Venice thread context from the parent thread to the executer thread!
                    final ThreadBridge bridgedOnComplete = bridge(onCompleteFn, args);

                    final Consumer<Future<VncVal>> onComplete = (Future<VncVal> f) -> onCompleteFn.applyOf(new VncJavaObject(f));

                    final Consumer<Future<VncVal>> onCompleteWrapper = bridgedOnComplete.bridgeConsumer(onComplete);

                    return exec(new VncFutureTask(taskWrapper, onCompleteWrapper));
                }
                else {
                    final VncFunction onSuccessFn = Coerce.toVncFunction(args.second());
                    final VncFunction onFailureFn = Coerce.toVncFunction(args.third());

                    // Create a wrapper that inherits the Venice thread context from the parent thread to the executer thread!
                    final ThreadBridge bridgedOnSuccess = bridge(onSuccessFn, args);
                    final ThreadBridge bridgedOnFailure = bridge(onFailureFn, args);

                    final Consumer<VncVal> onSuccess = (VncVal f) -> onSuccessFn.applyOf(new VncJavaObject(f));
                    final Consumer<VncException> onFailure = (VncException f) -> onFailureFn.applyOf(new VncJavaObject(f));

                    final Consumer<VncVal> onSuccessWrapper = bridgedOnSuccess.bridgeConsumer(onSuccess);
                    final Consumer<VncException> onFailureWrapper = bridgedOnFailure.bridgeConsumer(onFailure);

                    return exec(new VncFutureTask(taskWrapper, onSuccessWrapper, onFailureWrapper));
                }
            }

            private ThreadBridge bridge(final VncFunction fn, final VncList args) {
                return ThreadBridge.create(
                            "future-task",
                            new CallFrame[] {
                                new CallFrame(this, args),
                                new CallFrame(fn)});
            }

            private VncJavaObject exec(final VncFutureTask futureTask) {
                // Note: Do NOT use a CompletableFuture
                //       Canceling a CompletableFuture does not interrupt the
                //       task wrapper!!!
                @SuppressWarnings("unchecked")
                final Future<VncVal> future = (Future<VncVal>)mngdExecutor
                                                                .getExecutor()
                                                                .submit(futureTask);

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
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(Types.isVncJavaObject(args.first(), Future.class));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction cancel =
        new VncFunction(
                "cancel",
                VncFunction
                    .meta()
                    .arglists("(cancel f)")
                    .doc("Cancels a future or a promise")
                    .examples(
                        "(do                                                                     \n" +
                        "   (def wait (fn [] (sleep 400) 100))                                   \n" +
                        "   (let [f (future wait)]                                               \n" +
                        "      (sleep 50)                                                        \n" +
                        "      (printf \"After 50ms: cancelled=%b\\n\" (cancelled? f))           \n" +
                        "      (cancel f)                                                        \n" +
                        "      (sleep 100)                                                       \n" +
                        "      (printf \"After 150ms: cancelled=%b\\n\" (cancelled? f))))          ")
                    .seeAlso("future", "promise", "done?", "cancelled?")
                    .build()
        ) {
            @Override
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
                        throw new VncException("Failed to cancel future/promise", ex);
                    }
                }

                throw new VncException(String.format(
                        "Function 'cancel' does not allow type %s as parameter.",
                        Types.getType(args.first())));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction cancelled_Q =
        new VncFunction(
                "cancelled?",
                VncFunction
                    .meta()
                    .arglists("(cancelled? f)")
                    .doc("Returns true if the future or promise is cancelled otherwise false")
                    .examples("(cancelled? (future (fn [] 100)))")
                    .seeAlso("future", "promise", "done?", "cancel")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                if (Types.isVncJavaObject(args.first(), Future.class)) {
                    try {
                        @SuppressWarnings("unchecked")
                        final Future<VncVal> future = Coerce.toVncJavaObject(args.first(), Future.class);
                        return VncBoolean.of(future.isCancelled());
                    }
                    catch(Exception ex) {
                        throw new VncException("Failed to check if future/promise is cancelled", ex);
                    }
                }

                throw new VncException(String.format(
                        "Function 'cancelled?' does not allow type %s as parameter",
                        Types.getType(args.first())));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction done_Q =
        new VncFunction(
                "done?",
                VncFunction
                    .meta()
                    .arglists("(done? f)")
                    .doc( "Returns true if the future or promise is done otherwise false")
                    .examples(
                        "(do                                                            \n" +
                        "   (def wait (fn [] (sleep 200) 100))                          \n" +
                        "   (let [f (future wait)]                                      \n" +
                        "      (sleep 50)                                               \n" +
                        "      (printf \"After 50ms: done=%b\\n\" (done? f))            \n" +
                        "      (sleep 300)                                              \n" +
                        "      (printf \"After 300ms: done=%b\\n\" (done? f))))           ")
                    .seeAlso("future", "promise", "realized?", "cancel", "cancelled?")
                    .build()
        ) {
            @Override
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
            @Override
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

    public static VncFunction futures_thread_pool_info =
        new VncFunction(
                "futures-thread-pool-info",
                VncFunction
                    .meta()
                    .arglists("(futures-thread-pool-info)")
                    .doc(
                        "Returns the thread pool info of the ThreadPoolExecutor serving " +
                        "the futures.\n\n" +
                        "| *core-pool-size*       |  the number of threads to keep in the pool, " +
                        "                            even if they are idle |\n" +
                        "| *maximum-pool-size*    |  the maximum allowed number of threads |\n" +
                        "| *current-pool-size*    |  the current number of threads in the pool |\n" +
                        "| *largest-pool-size*    |  the largest number of threads that have " +
                        "                            ever simultaneously been in the pool |\n" +
                        "| *active-thread-count*  |  the approximate number of threads that are " +
                        "                            actively executing tasks |\n" +
                        "| *scheduled-task-count* |  the approximate total number of tasks that " +
                        "                            have ever been scheduled for execution |\n" +
                        "| *completed-task-count* |  the approximate total number of tasks " +
                        "                            that have completed execution |")
                    .examples(
                        "(futures-thread-pool-info)")
                    .seeAlso("future")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                return mngdExecutor.info();
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
            @Override
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
                    .seeAlso("delay", "deref", "realized?")
                    .build()
        ) {
            @Override
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
                    .doc("If x is a delay, returns its value, else returns x")
                    .examples(
                        "(do                                              \n" +
                        "   (def x (delay (println \"working...\") 100))  \n" +
                        "   (force x))",
                        "(force (+ 1 2))")
                    .seeAlso("delay", "deref", "realized?")
                    .build()
        ) {
            @Override
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
                        "(do \n" +
                        "  (assoc! (thread-local) :a 1) \n" +
                        "  (get (thread-local) :a))",
                        "(do \n" +
                        "  (assoc! (thread-local) :a 1) \n" +
                        "  (get (thread-local) :b 999))",
                        "(do \n" +
                        "  (thread-local :a 1 :b 2) \n" +
                        "  (get (thread-local) :a))",
                        "(do \n" +
                        "  (thread-local { :a 1 :b 2 }) \n" +
                        "  (get (thread-local) :a))",
                        "(do \n" +
                        "  (thread-local-clear) \n" +
                        "  (assoc! (thread-local) :a 1 :b 2) \n" +
                        "  (dissoc! (thread-local) :a) \n" +
                        "  (get (thread-local) :a 999))")
                    .seeAlso(
                        "thread-local-clear",
                        "thread-local-map",
                        "assoc!",
                        "dissoc!",
                        "get")
                    .build()
        ) {
            @Override
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
            @Override
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
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                new VncThreadLocal().clear(true); // preserve system values!
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
                    .doc(
                        "Returns a snaphost of the thread local vars as a map.\n\n" +
                        "Note:¶" +
                        "The returned map is a copy of the current thread local vars. Thus \n" +
                        "modifying this map is not modifying the thread local vars! \n" +
                        "Use `assoc!` and `dissoc!` for that purpose!")
                    .examples(
                        "(do \n" +
                        "  (thread-local-clear) \n" +
                        "  (thread-local :a 1 :b 2) \n" +
                        "  (thread-local-map))")
                    .seeAlso("thread-local", "get", "assoc!", "dissoc!")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                return VncThreadLocal.toMap();
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };



    ///////////////////////////////////////////////////////////////////////////
    // Thread
    ///////////////////////////////////////////////////////////////////////////


    public static VncFunction thread =
        new VncFunction(
                "thread",
                VncFunction
                    .meta()
                    .arglists(
                        "(thread f)", "(thread f name)")
                    .doc(
                        "Executes f in another thread, returning immediately to the calling " +
                        "thread. Returns a `promise` which will receive the result of " +
                        "calling f when completed. An optional name can be assigned to the thread.\n\n" +
                        "*Note:* Each call to `thread` creates a new expensive system thread. " +
                        "Consider to use futures or promises that use an *ExecutorService* to deal " +
                        "efficiently with threads.")
                    .examples(
                         "@(thread #(do (sleep 100) 1))",
                         "@(thread #(do (sleep 100) 1) \"job\")")
                    .seeAlso(
                         "future", "promise")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                final VncFunction fn = Coerce.toVncFunction(args.first());

                final String name = args.size() == 2
                                        ? Coerce.toVncString(args.second()).getValue()
                                        : "VeniceThread-" + nextThreadNum.getAndIncrement();

                final CallFrame[] cf = new CallFrame[] {
                                            new CallFrame(this, args),
                                            new CallFrame(fn) };

                final CompletableFuture<VncVal> result = new CompletableFuture<>();

                // Create a wrapper that inherits the Venice thread context
                // from the parent thread to the executer thread!
                final ThreadBridge threadBridge = ThreadBridge.create("thread", cf);

                final Runnable taskWrapper = threadBridge.bridgeRunnable(
                        () -> { try {
                                    result.complete(fn.apply(VncList.empty()));
                                }
                                catch(Exception ex) {
                                    result.completeExceptionally(ex);
                                }
                              });

                final Thread th = new Thread(taskWrapper, name);
                th.setDaemon(true);
                th.start();

                return new VncJavaObject(result);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction thread_id =
        new VncFunction(
                "thread-id",
                VncFunction
                    .meta()
                    .arglists("(thread-id)")
                    .doc(
                        "Returns the identifier of this Thread. The thread ID is a " +
                        "positive number generated when this thread was created. " +
                        "The thread ID is unique and remains unchanged during its " +
                        "lifetime. When a thread is terminated, this thread ID may " +
                        "be reused.")
                    .examples("(thread-id)")
                    .seeAlso("thread-name")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                return new VncLong(Thread.currentThread().getId());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction thread_daemon_Q =
        new VncFunction(
                "thread-daemon?",
                VncFunction
                    .meta()
                    .arglists("(thread-daemon?)")
                    .doc(
                        "Returns true if this Thread is a daemon thread else false.")
                    .examples("(thread-daemon?)")
                    .seeAlso("thread-name")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                return VncBoolean.of(Thread.currentThread().isDaemon());
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
            @Override
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
            @Override
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
                        "before the second call had examined it).\n\n" +
                        "Returns true if the current thread has been interrupted " +
                        "else false.")
                    .examples("(thread-interrupted)")
                    .seeAlso("thread-interrupted?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                return VncBoolean.of(Thread.interrupted());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };



    ///////////////////////////////////////////////////////////////////////////
    // Parallel processing
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction pmap =
        new VncFunction(
                "pmap",
                VncFunction
                    .meta()
                    .arglists("(pmap f coll)", "(pmap f coll & colls)")
                    .doc(
                        "Like `map`, except *f* is applied in parallel. Only useful for " +
                        "computationally intensive functions where the time of *f* " +
                        "dominates the coordination overhead.\n\n" +
                        "The result collection is sorted in the same way as for `map`, " +
                        "i.e. it preserves the items' order in the *coll* (or *colls*) " +
                        "parameter(s) of `pmap`. In other words: calculation is done " +
                        "parallel, but the result is delivered in the order the input " +
                        "came (in *coll*/*colls*).\n" +
                        "In contrast, side effects of *f* (if any) are coming in random " +
                        "order!\n\n" +
                        "`pmap` is implemented using Venice futures and processes " +
                        "`(+ 2 (cpus))` items in parallel.")
                    .examples(
                        ";; With `pmap`, the total elapsed time is just over 2 seconds:\n" +
                        "(do                                          \n" +
                        "  (defn long-running-job [n]                 \n" +
                        "    (sleep 2000)  ; wait for 2 seconds       \n" +
                        "    (+ n 10))                                \n" +
                        "  (time (pmap long-running-job (range 4))))   ",
                        ";; With `map`, the total elapsed time is roughly 4 * 2 seconds:\n" +
                        "(do                                          \n" +
                        "  (defn long-running-job [n]                 \n" +
                        "    (sleep 2000)  ; wait for 2 seconds       \n" +
                        "    (+ n 10))                                \n" +
                        "  (time (map long-running-job (range 4))))  ")
                    .seeAlso("pcalls", "map", "cpus")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 2);

                if (args.size() == 2 && args.second() == Nil) {
                    return VncList.empty();
                }

                final IVncFunction fn = Coerce.toIVncFunction(args.first());

                VncLazySeq lazySeq;

                if (args.size() == 2) {
                    // single collection
                    final VncSequence seq = VncSequence.coerceToSequence(args.second());

                    lazySeq = VncLazySeq
                                .ofAll(seq, Nil)
                                .map(v -> future.applyOf(
                                            VncFunction.of(() -> fn.applyOf(v))));
                }
                else {
                    // multiple collections
                    final VncList listsOfSeqs = args.rest().filter(v -> v != Nil);

                    final VncSequence[] seqs = new VncSequence[listsOfSeqs.size()];
                    for(int ii=0; ii<listsOfSeqs.size(); ii++) {
                        seqs[ii] = VncSequence.coerceToSequence(listsOfSeqs.nth(ii));
                    }

                    VncList mappingFnArgs = VncList.empty();

                    while(!isOneSeqEmpty(seqs)) {
                        VncList fnArgs = VncList.empty();
                        for(int ii=0; ii<seqs.length; ii++) {
                            fnArgs = fnArgs.addAtEnd(seqs[ii].first());
                            seqs[ii] = seqs[ii].rest();
                        }
                        mappingFnArgs = mappingFnArgs.addAtEnd(fnArgs);
                    }

                    lazySeq = VncLazySeq
                                .ofAll(mappingFnArgs, Nil)
                                .map(v -> future.applyOf(
                                            VncFunction.of(() -> fn.apply((VncList)v))));
                }


                // parallelization
                final int n = 2 + Runtime.getRuntime().availableProcessors();

                VncList result = VncList.empty();
                while(true) {
                    final VncLazySeq part = lazySeq.take(n);
                    if (part.isEmpty()) {
                        break;
                    }

                    lazySeq = lazySeq.drop(n);

                    final VncList partialRes = part.realize()
                                                   .map(v -> deref.applyOf(v));

                    result = result.addAllAtEnd(partialRes);
                }

                return result;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction pcalls =
        new VncFunction(
                "pcalls",
                VncFunction
                    .meta()
                    .arglists("(pcalls & fns)")
                    .doc(
                        "Executes the no-arg *fns* in parallel, returning a sequence " +
                        "of their values in the same order the functions are passed.\n" +
                        "In contrast, side effects of *fns* (if any) are coming in random " +
                        "order!\n\n" +
                        "`pcalls` is implemented using Venice futures and processes " +
                        "`(+ 2 (cpus))` functions in parallel.")
                    .examples(
                        "(pcalls #(+ 1 2) #(+ 2 3) #(+ 3 4))")
                    .seeAlso("pmap", "cpus")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                // (pmap #(%) fns)
                return pmap.applyOf(
                        VncFunction.of((VncList x) -> ((VncFunction)x.first()).apply(VncList.empty())),
                        args.map(f -> Coerce.toVncFunction(f)));
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

    public static class VncFutureTask extends FutureTask<VncVal> {
        public VncFutureTask(
                final Callable<VncVal> taskFn,
                final Consumer<Future<VncVal>> doneFn
        ) {
            super(taskFn);
            this.doneFn = doneFn;
            this.successFn = null;
            this.failureFn = null;
        }

        public VncFutureTask(
                final Callable<VncVal> taskFn,
                final Consumer<VncVal> successFn,
                final Consumer<VncException> failureFn
        ) {
            super(taskFn);
            this.doneFn = null;
            this.successFn = successFn;
            this.failureFn = failureFn;
        }

        @Override
        protected void done() {
            if (doneFn != null) {
                doneFn.accept(this);
            }
            else {
                try {
                    successFn.accept(get());
                }
                catch(VncException ex) {
                    failureFn.accept(ex);
                }
                catch(ExecutionException | CompletionException ex) {
                    if (ex.getCause() == null) {
                        failureFn.accept(new VncException(ex.getMessage(), ex));
                    }
                    else {
                        if (ex.getCause() instanceof VncException) {
                            failureFn.accept((VncException)ex.getCause());
                        }
                        else {
                            failureFn.accept(new VncException(ex.getCause().getMessage(), ex.getCause()));
                        }
                    }
                }
                catch(Exception ex) {
                    failureFn.accept(new VncException(ex.getMessage(), ex));
                }
            }
        }


        private final Consumer<Future<VncVal>> doneFn;
        private final Consumer<VncVal> successFn;
        private final Consumer<VncException> failureFn;
    };

    public static <T> CompletableFuture<T> timeoutAfter(final long timeout, final TimeUnit unit) {
        final ScheduledExecutorService delayer = ScheduleFunctions.getScheduledExecutorService();

        final CompletableFuture<T> result = new CompletableFuture<T>();

        delayer.schedule(() -> result.completeExceptionally(new TimeoutException()), timeout, unit);

        return result;
    }

    public static <T> CompletableFuture<T> timeoutAfter(final T timeoutVal, final long timeout, final TimeUnit unit) {
        final ScheduledExecutorService delayer = ScheduleFunctions.getScheduledExecutorService();

        final CompletableFuture<T> result = new CompletableFuture<T>();

        delayer.schedule(() -> result.complete(timeoutVal), timeout, unit);

        return result;
    }

    private static boolean isOneSeqEmpty(final VncSequence[] seqs) {
        for(int ii=0; ii<seqs.length; ii++) {
            if (seqs[ii].isEmpty()) {
                return true;
            }
        }
        return false;
    }


    ///////////////////////////////////////////////////////////////////////////
    // types_ns is namespace of type functions
    ///////////////////////////////////////////////////////////////////////////

    public static Map<VncVal, VncVal> ns =
            new SymbolMapBuilder()
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
                    .add(agent_send_thread_pool_info)
                    .add(agent_send_off_thread_pool_info)

                    .add(promise)
                    .add(promise_Q)
                    .add(deliver)
                    .add(deliver_ex)
                    .add(then_accept)
                    .add(then_apply)
                    .add(then_combine)
                    .add(then_compose)
                    .add(when_complete)
                    .add(accept_either)
                    .add(apply_to_either)
                    .add(then_accept_both)
                    .add(timeout_after)
                    .add(or_timeout)
                    .add(complete_on_timeout)
                    .add(all_of)
                    .add(any_of)

                    .add(future)
                    .add(future_task)
                    .add(future_Q)
                    .add(futures_fork)
                    .add(futures_wait)
                    .add(futures_thread_pool_info)

                    .add(delay_Q)
                    .add(force)

                    .add(done_Q)
                    .add(cancel)
                    .add(cancelled_Q)

                    .add(thread)
                    .add(thread_id)
                    .add(thread_daemon_Q)
                    .add(thread_name)
                    .add(thread_interrupted_Q)
                    .add(thread_interrupted)

                    .add(new_thread_local)
                    .add(thread_local_Q)
                    .add(thread_local_clear)
                    .add(thread_local_map)

                    .add(pmap)
                    .add(pcalls)

                    .toMap();


    private static AtomicLong nextThreadNum = new AtomicLong(1L);

    private static ManagedCachedThreadPoolExecutor mngdExecutor =
            new ManagedCachedThreadPoolExecutor("venice-future-pool", 200);
}
