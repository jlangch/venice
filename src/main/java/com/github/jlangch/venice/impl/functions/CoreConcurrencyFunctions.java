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

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.github.jlangch.venice.SecurityException;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.IDeref;
import com.github.jlangch.venice.impl.types.VncAtom;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncByteBuffer;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.VncVolatile;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.concurrent.Delay;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.MetaUtil;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;
import com.github.jlangch.venice.util.CapturingPrintStream;


public class CoreConcurrencyFunctions {


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
                    else if (delegate instanceof StringWriter) {
                        return new VncString(((StringWriter)delegate).getBuffer().toString());
                    }
                    else if (delegate instanceof CapturingPrintStream) {
                        return new VncString(((CapturingPrintStream)delegate).getOutput());
                    }
                    else if (delegate instanceof ByteArrayOutputStream) {
                        return new VncByteBuffer(((ByteArrayOutputStream)delegate).toByteArray());
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




    ///////////////////////////////////////////////////////////////////////////
    // Delay
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction delay_ASTERISK =
        new VncFunction(
                "delay*",
                VncFunction
                    .meta()
                    .arglists("(delay* fn)")
                    .doc("Creates a new delay object for a function")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                final VncFunction fn = Coerce.toVncFunction(args.first());
                return new VncJavaObject(new Delay(fn));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

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
            new SymbolMapBuilder()
	            .add(new_atom)
	            .add(atom_Q)
	            .add(reset_BANG)
	            .add(swap_BANG)
	            .add(swap_vals_BANG)
	            .add(compare_and_set_BANG)

	            .add(delay_ASTERISK)
	            .add(delay_Q)
	            .add(force)

	            .add(new_volatile)
	            .add(volatile_Q)

	            .add(deref)
	            .add(deref_Q)

	            .toMap();
}
