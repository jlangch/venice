/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncByteBuffer;
import com.github.jlangch.venice.impl.types.VncDouble;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncInteger;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncLazySeq;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;
import com.github.jlangch.venice.impl.util.io.CharsetUtil;


public class BytebufFunctions {


    ///////////////////////////////////////////////////////////////////////////
    // ByteBuf functions
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction bytebuf_Q =
        new VncFunction(
                "bytebuf?",
                VncFunction
                    .meta()
                    .arglists("(bytebuf? x)")
                    .doc("Returns true if x is a bytebuf")
                    .examples(
                        "(bytebuf? (bytebuf [1 2]))",
                        "(bytebuf? [1 2])",
                        "(bytebuf? nil)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(Types.isVncByteBuffer(args.first()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction bytebuf_cast =
        new VncFunction(
                "bytebuf",
                VncFunction
                    .meta()
                    .arglists("(bytebuf x)")
                    .doc(
                        "Converts x to bytebuf. x can be a bytebuf, a list/vector of longs, " +
                        "a string")
                    .examples(
                        "(bytebuf [0 1 2])",
                        "(bytebuf '(0 1 2))",
                        "(bytebuf \"abc\")")
                    .seeAlso(
                        "io/bytebuf-out-stream")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0, 1);

                if (args.isEmpty()) {
                    return new VncByteBuffer(ByteBuffer.wrap(new byte[0]));
                }

                final VncVal arg = args.first();

                if (Types.isVncString(arg)) {
                    try {
                        return new VncByteBuffer(
                                        ByteBuffer.wrap(
                                            ((VncString)arg).getValue().getBytes(CharsetUtil.DEFAULT_CHARSET)));
                    }
                    catch(Exception ex) {
                        throw new VncException(
                                "Failed to coerce string to bytebuf", ex);
                    }
                }
                else if (Types.isVncJavaObject(arg)) {
                    final Object delegate = ((VncJavaObject)arg).getDelegate();
                    if (delegate.getClass() == byte[].class) {
                        return new VncByteBuffer(ByteBuffer.wrap((byte[])delegate));
                    }
                    else if (delegate instanceof ByteBuffer) {
                        return new VncByteBuffer((ByteBuffer)delegate);
                    }
                }
                else if (Types.isVncByteBuffer(arg)) {
                    return arg;
                }
                else if (Types.isVncSequence(arg)) {
                    if (!((VncSequence)arg).stream().allMatch(v -> Types.isVncLong(v))) {
                        throw new VncException(String.format(
                                "Function 'bytebuf' a list as argument must contains long values"));
                    }

                    final VncSequence seq = (VncSequence)arg;

                    final byte[] buf = new byte[seq.size()];
                    int ii = 0;
                    for(VncVal v : seq) {
                        buf[ii++] = (byte)((VncLong)v).getValue().longValue();
                    }

                    return new VncByteBuffer(ByteBuffer.wrap(buf));
                }

                throw new VncException(String.format(
                            "Function 'bytebuf' does not allow %s as argument",
                            Types.getType(arg)));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction bytebuf_allocate =
        new VncFunction(
                "bytebuf-allocate",
                VncFunction
                    .meta()
                    .arglists(
                    	"(bytebuf-allocate length)",
                    	"(bytebuf-allocate length init-val)")
                    .doc(
                    	"Allocates a new bytebuf. The values will be all zero or preset with " +
                    	"init-val id init-val is supplied.")
                    .examples(
                    	"(bytebuf-allocate 20)",
                    	"(bytebuf-allocate 20 0x55)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                final int length = Coerce.toVncLong(args.first()).getValue().intValue();

                if (args.size() == 1) {
                	return new VncByteBuffer(ByteBuffer.allocate(length));
                }
                else {
                	final byte val = (byte)(Coerce.toVncLong(args.first()).getValue().longValue() & 0x0FF);
                	final byte[] data = new byte[length];
                	Arrays.fill(data, val);
                	return new VncByteBuffer(ByteBuffer.wrap(data));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction bytebuf_allocate_random =
        new VncFunction(
                "bytebuf-allocate-random",
                VncFunction
                    .meta()
                    .arglists(
                    	"(bytebuf-allocate-random length)")
                    .doc(
                    	"Allocates a new bytebuf. The values will be all preset with random" +
                    	"bytes")
                    .examples(
                    	"(bytebuf-allocate-random 20)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final int length = Coerce.toVncLong(args.first()).getValue().intValue();
            	final byte[] data = new byte[length];
            	new SecureRandom().nextBytes(data);
            	return new VncByteBuffer(ByteBuffer.wrap(data));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction bytebuf_capacity =
        new VncFunction(
                "bytebuf-capacity",
                VncFunction
                    .meta()
                    .arglists("(bytebuf-capacity buf)")
                    .doc( "Returns the capacity of a bytebuf.")
                    .examples("(bytebuf-capacity (bytebuf-allocate 100))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final ByteBuffer buf = Coerce.toVncByteBuffer(args.first()).getValue();

                return new VncLong(buf.capacity());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction bytebuf_limit =
        new VncFunction(
                "bytebuf-limit",
                VncFunction
                    .meta()
                    .arglists("(bytebuf-limit buf)")
                    .doc( "Returns the limit of a bytebuf.")
                    .examples("(bytebuf-limit (bytebuf-allocate 100))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final ByteBuffer buf = Coerce.toVncByteBuffer(args.first()).getValue();

                return new VncLong(buf.limit());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction bytebuf_byte_order_BANG =
        new VncFunction(
                "bytebuf-byte-order!",
                VncFunction
                    .meta()
                    .arglists("(bytebuf-byte-order! buf endian)")
                    .doc( "Sets the bytebuf's byte order.")
                    .examples(
                    	"(-> (bytebuf-allocate 10)              \n" +
                        "    (bytebuf-byte-order! :big-endian)  \n" +
                        "    (bytebuf-byte-order))              ",
                    	"(-> (bytebuf-allocate 10)                 \n" +
                        "    (bytebuf-byte-order! :little-endian)  \n" +
                        "    (bytebuf-byte-order))                 ")
                    .seeAlso("bytebuf-byte-order")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final ByteBuffer buf = Coerce.toVncByteBuffer(args.first()).getValue();
                final String order = Coerce.toVncKeyword(args.second()).getSimpleName();

                switch(order) {
	                case "big-endian":
	                	buf.order(ByteOrder.BIG_ENDIAN);
	                	break;
	                case "little-endian":
	                	buf.order(ByteOrder.LITTLE_ENDIAN);
	                	break;
	                default:
	                    throw new VncException(String.format(
	                            "Invalid bytebuf byte order '" + order + "'"));
                }

                return args.first();
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction bytebuf_byte_order =
        new VncFunction(
                "bytebuf-byte-order",
                VncFunction
                    .meta()
                    .arglists("(bytebuf-byte-order buf endian)")
                    .doc( "Returns the bytebuf's byte order.")
                    .examples(
                    	"(bytebuf-byte-order (bytebuf-allocate 10))")
                    .seeAlso("bytebuf-byte-order!")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final ByteBuffer buf = Coerce.toVncByteBuffer(args.first()).getValue();

                final String order = buf.order().toString();
                switch(order) {
	                case "BIG_ENDIAN": return new VncKeyword(":big-endian");
	                case "LITTLE_ENDIAN": return new VncKeyword(":little-endian");
	                default: return new VncKeyword(":little-endian");
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction bytebuf_from_string =
        new VncFunction(
                "bytebuf-from-string",
                VncFunction
                    .meta()
                    .arglists(
                        "(bytebuf-from-string s)",
                    	"(bytebuf-from-string s encoding)",
                    	"(bytebuf-from-string s encoding buf-length fillbyte)")
                    .doc(
                    	"Converts a string to a bytebuf using an optional encoding. " +
                    	"The encoding defaults to :UTF-8")
                    .examples(
                    	"(bytebuf-from-string \"abcdef\")",
                    	"(bytebuf-from-string \"abcdef\" :UTF-8)",
                    	"(bytebuf-from-string \"abcdef\" :UTF-8 16 0x00)")
                    .seeAlso(
                    	"bytebuf-to-string")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2, 4);

                try {
	                final String s = Coerce.toVncString(args.first()).getValue();

	                if (args.size() == 1) {
	                	final Charset charset = Charset.forName("UTF-8");
	                	return new VncByteBuffer(ByteBuffer.wrap(s.getBytes(charset)));
	                }
	                else if (args.size() == 2) {
	                	final VncVal encVal = args.second();
	                	final Charset charset = CharsetUtil.charset(encVal);
	                	return new VncByteBuffer(ByteBuffer.wrap(s.getBytes(charset)));
	                }
	                else if (args.size() == 4) {
	                	final VncVal encVal = args.second();
	                	final Charset charset = CharsetUtil.charset(encVal);

	                	final long buflen = Coerce.toVncLong(args.third()).getValue();

	                	final byte[] bytes = s.getBytes(charset);
	                	if (bytes.length == buflen) {
		                	return new VncByteBuffer(ByteBuffer.wrap(bytes));
	                	}
	                	else if (bytes.length < buflen) {
		                	final byte filler = (byte)(Coerce.toVncLong(args.fourth()).getValue() & 0xFF);
		                	final byte[] buf = Arrays.copyOf(bytes, (int)buflen);
	                		Arrays.fill(buf, bytes.length, buf.length, filler);
		                	return new VncByteBuffer(ByteBuffer.wrap(buf));
	                	}
	                	else {
	                		return new VncByteBuffer(ByteBuffer.wrap(Arrays.copyOf(bytes, (int)buflen)));
	                	}
	                }
	                else {
	                    throw new VncException(String.format(
	                            "bytebuf-from-string illegal number of arguments"));
	                }
                }
                catch(Exception ex) {
                    throw new VncException(String.format(
                            "Failed to convert string to bytebuffer"));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction bytebuf_to_string =
        new VncFunction(
                "bytebuf-to-string",
                VncFunction
                    .meta()
                    .arglists("(bytebuf-to-string buf encoding)")
                    .doc( "Converts a bytebuf to a string using an optional encoding. The encoding defaults to :UTF-8")
                    .examples("(bytebuf-to-string (bytebuf [97 98 99]) :UTF-8)")
                    .seeAlso("bytebuf-from-string")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                final ByteBuffer buf = Coerce.toVncByteBuffer(args.first()).getValue();

                final VncVal encVal = args.size() == 2 ? args.second() : Nil;
                final Charset charset = CharsetUtil.charset(encVal);

                try {
                    return new VncString(new String(buf.array(), charset));
                }
                catch(Exception ex) {
                    throw new VncException(String.format(
                            "Failed to convert bytebuf to string"));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction bytebuf_to_list =
        new VncFunction(
                "bytebuf-to-list",
                VncFunction
                    .meta()
                    .arglists("(bytebuf-to-list buf)")
                    .doc( "Returns the bytebuf as lazy list of integers")
                    .examples("(doall (bytebuf-to-list (bytebuf [97 98 99])))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                final ByteBuffer buf = Coerce.toVncByteBuffer(args.first()).getValue();
                final AtomicLong idx = new AtomicLong(0L);

                final VncFunction iter = new VncFunction("iter") {
                    @Override
                    public VncVal apply(final VncList args) {
                        final int ii = (int)idx.getAndIncrement();
                        if (ii >= buf.limit()) {
                            return Nil;
                        }
                        else {
                            final int val = buf.get(ii) & 0xFF;
                            return new VncInteger(val);
                        }
                    }
                    private static final long serialVersionUID = 1L;
                };

                return VncLazySeq.iterate(iter, Nil);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction bytebuf_sub =
        new VncFunction(
                "bytebuf-sub",
                VncFunction
                    .meta()
                    .arglists("(bytebuf-sub x start) (bytebuf-sub x start end)")
                    .doc(
                        "Returns a byte buffer of the items in buffer from start (inclusive) "+
                        "to end (exclusive). If end is not supplied, defaults to " +
                        "(count bytebuffer)")
                    .examples(
                        "(bytebuf-sub (bytebuf [1 2 3 4 5 6]) 2)",
                        "(bytebuf-sub (bytebuf [1 2 3 4 5 6]) 4)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2, 3);

                final byte[] buf = Coerce.toVncByteBuffer(args.first()).getBytes();
                final VncLong from = Coerce.toVncLong(args.second());
                final VncLong to = args.size() > 2 ? Coerce.toVncLong(args.nth(2)) : null;


                return new VncByteBuffer(
                                to == null
                                    ? ByteBuffer.wrap(
                                            Arrays.copyOfRange(
                                                    buf,
                                                    from.getValue().intValue(),
                                                    buf.length))
                                    :  ByteBuffer.wrap(
                                            Arrays.copyOfRange(
                                                    buf,
                                                    from.getValue().intValue(),
                                                    to.getValue().intValue())));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction bytebuf_put_buf_BANG =
        new VncFunction(
                "bytebuf-put-buf!",
                VncFunction
                    .meta()
                    .arglists("(bytebuf-put-buf! dst src src-offset length)")
                    .doc("This method transfers bytes from the src to the dst buffer at " +
                         "the current position, and then increments the position by length.")
                    .examples(
                        "(-> (bytebuf-allocate 10)   \n" +
                        "    (bytebuf-pos! 4)        \n" +
                        "    (bytebuf-put-buf! (bytebuf [1 2 3]) 0 2))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 4);

                final ByteBuffer dst = Coerce.toVncByteBuffer(args.first()).getValue();
                final ByteBuffer src = Coerce.toVncByteBuffer(args.second()).getValue();
                final VncLong src_offset = Coerce.toVncLong(args.third());
                final VncLong length = Coerce.toVncLong(args.fourth());

                dst.put(
                    src.array(),
                    src_offset.getValue().intValue(),
                    length.getValue().intValue());

                return args.nth(0);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction bytebuf_get_byte =
        new VncFunction(
                "bytebuf-get-byte",
                VncFunction
                    .meta()
                    .arglists(
                        "(bytebuf-get-byte buf)",
                        "(bytebuf-get-byte buf pos)")
                    .doc(
                        "Reads a byte from the buffer. Without a pos reads from the " +
                        "current position and increments the position by one. With a " +
                        "position reads the byte from that position.")
                    .examples(
                        "(-> (bytebuf-allocate 4)   \n" +
                        "    (bytebuf-put-byte! 1)  \n" +
                        "    (bytebuf-put-byte! 2)  \n" +
                        "    (bytebuf-get-byte 0))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                final ByteBuffer buf = Coerce.toVncByteBuffer(args.nth(0)).getValue();

                if (args.size() == 1) {
                    final int v = buf.get() & 0xFF;
                    return new VncInteger(v);
                }
                else {
                    final VncLong pos = Coerce.toVncLong(args.nth(1));
                    final int v = buf.get(pos.getIntValue()) & 0xFF;
                    return new VncInteger(v);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction bytebuf_get_int =
        new VncFunction(
                "bytebuf-get-int",
                VncFunction
                    .meta()
                    .arglists(
                        "(bytebuf-get-int buf)",
                        "(bytebuf-get-int buf pos)")
                    .doc(
                        "Reads an integer from the buffer. Without a pos reads from the " +
                        "current position and increments the position by four. With a " +
                        "position reads the integer from that position.")
                    .examples(
                        "(-> (bytebuf-allocate 8)   \n" +
                        "    (bytebuf-put-int! 1I)  \n" +
                        "    (bytebuf-put-int! 2I)  \n" +
                        "    (bytebuf-get-int 0))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                final ByteBuffer buf = Coerce.toVncByteBuffer(args.nth(0)).getValue();

                if (args.size() == 1) {
                    final int v = buf.getInt();
                    return new VncInteger(v);
                }
                else {
                    final VncLong pos = Coerce.toVncLong(args.nth(1));
                    final int v = buf.getInt(pos.getIntValue());
                    return new VncInteger(v);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction bytebuf_get_long =
        new VncFunction(
                "bytebuf-get-long",
                VncFunction
                    .meta()
                    .arglists(
                        "(bytebuf-get-long buf)",
                        "(bytebuf-get-long buf pos)")
                    .doc(
                        "Reads a long from the buffer. Without a pos reads from the " +
                        "current position and increments the position by eight. With a " +
                        "position reads the long from that position.")
                    .examples(
                        "(-> (bytebuf-allocate 16)   \n" +
                        "    (bytebuf-put-long! 20)  \n" +
                        "    (bytebuf-put-long! 40)  \n" +
                        "    (bytebuf-get-long 0))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                final ByteBuffer buf = Coerce.toVncByteBuffer(args.nth(0)).getValue();

                if (args.size() == 1) {
                    final long v = buf.getLong();
                    return new VncLong(v);
                }
                else {
                    final VncLong pos = Coerce.toVncLong(args.nth(1));
                    final long v = buf.getLong(pos.getValue().intValue());
                    return new VncLong(v);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction bytebuf_get_float =
        new VncFunction(
                "bytebuf-get-float",
                VncFunction
                    .meta()
                    .arglists(
                        "(bytebuf-get-float buf)",
                        "(bytebuf-get-float buf pos)")
                    .doc(
                        "Reads a float from the buffer. Without a pos reads from the " +
                        "current position and increments the position by four. With a " +
                        "position reads the float from that position.")
                    .examples(
                        "(-> (bytebuf-allocate 16)   \n" +
                        "    (bytebuf-put-float! 20.0)  \n" +
                        "    (bytebuf-put-float! 40.0)  \n" +
                        "    (bytebuf-get-float 0))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                final ByteBuffer buf = Coerce.toVncByteBuffer(args.nth(0)).getValue();

                if (args.size() == 1) {
                    final float v = buf.getFloat();
                    return new VncDouble(v);
                }
                else {
                    final VncLong pos = Coerce.toVncLong(args.nth(1));
                    final float v = buf.getFloat(pos.getValue().intValue());
                    return new VncDouble(v);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction bytebuf_get_double =
        new VncFunction(
                "bytebuf-get-double",
                VncFunction
                    .meta()
                    .arglists(
                        "(bytebuf-get-double buf)",
                        "(bytebuf-get-double buf pos)")
                    .doc(
                        "Reads a double from the buffer. Without a pos reads from the " +
                        "current position and increments the position by eight. With a " +
                        "position reads the double from that position.")
                    .examples(
                        "(-> (bytebuf-allocate 16)   \n" +
                        "    (bytebuf-put-double! 20.0)  \n" +
                        "    (bytebuf-put-double! 40.0)  \n" +
                        "    (bytebuf-get-double 0))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                final ByteBuffer buf = Coerce.toVncByteBuffer(args.nth(0)).getValue();

                if (args.size() == 1) {
                    final double v = buf.getDouble();
                    return new VncDouble(v);
                }
                else {
                    final VncLong pos = Coerce.toVncLong(args.nth(1));
                    final double v = buf.getDouble(pos.getValue().intValue());
                    return new VncDouble(v);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction bytebuf_put_byte_BANG =
        new VncFunction(
                "bytebuf-put-byte!",
                VncFunction
                    .meta()
                    .arglists("(bytebuf-put-byte! buf b)")
                    .doc("Writes a byte to the buffer at the current position, and then " +
                         "increments the position by one.")
                    .examples(
                        "(-> (bytebuf-allocate 4)   \n" +
                        "    (bytebuf-put-byte! 1)  \n" +
                        "    (bytebuf-put-byte! 2I))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final ByteBuffer buf = Coerce.toVncByteBuffer(args.nth(0)).getValue();

                final VncVal b = args.nth(1);
                if (b instanceof VncInteger) {
                    final VncInteger val = Coerce.toVncInteger(args.nth(1));
                    buf.put(val.getValue().byteValue());
                }
                else if (b instanceof VncLong) {
                    final VncLong val = Coerce.toVncLong(args.nth(1));
                    buf.put(val.getValue().byteValue());
                }
                else {
                    throw new VncException(
                            "Function 'bytebuf-put-byte!' expects an integer or a long value "
                                + "as second arg");
                }

                return args.nth(0);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction bytebuf_put_long_BANG =
        new VncFunction(
                "bytebuf-put-long!",
                VncFunction
                    .meta()
                    .arglists("(bytebuf-put-long! buf l)")
                    .doc("Writes a long (8 bytes) to buffer at the current position, and then " +
                         "increments the position by eight.")
                    .examples(
                        "(-> (bytebuf-allocate 16)   \n" +
                        "    (bytebuf-put-long! 4)   \n" +
                        "    (bytebuf-put-long! 8))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final ByteBuffer buf = Coerce.toVncByteBuffer(args.nth(0)).getValue();
                final VncLong val = Coerce.toVncLong(args.nth(1));

                buf.putLong(val.getValue());

                return args.nth(0);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction bytebuf_put_int_BANG =
        new VncFunction(
                "bytebuf-put-int!",
                VncFunction
                    .meta()
                    .arglists("(bytebuf-put-int! buf i)")
                    .doc("Writes an integer (4 bytes) to buffer at the current position, and then " +
                         "increments the position by four.")
                    .examples(
                        "(-> (bytebuf-allocate 8)   \n" +
                        "    (bytebuf-put-int! 4I)  \n" +
                        "    (bytebuf-put-int! 8I))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final ByteBuffer buf = Coerce.toVncByteBuffer(args.nth(0)).getValue();
                final VncInteger val = Coerce.toVncInteger(args.nth(1));

                buf.putInt(val.getValue());

                return args.nth(0);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction bytebuf_put_double_BANG =
        new VncFunction(
                "bytebuf-put-double!",
                VncFunction
                    .meta()
                    .arglists("(bytebuf-put-double! buf d)")
                    .doc("Writes a double (8 bytes) to buffer at the current position, and then " +
                         "increments the position by eight.")
                    .examples(
                        "(-> (bytebuf-allocate 16)     \n" +
                        "    (bytebuf-put-double! 64.0)  \n" +
                        "    (bytebuf-put-double! 200.0))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final ByteBuffer buf = Coerce.toVncByteBuffer(args.nth(0)).getValue();
                final VncDouble val = Coerce.toVncDouble(args.nth(1));

                buf.putDouble(val.getValue());

                return args.nth(0);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction bytebuf_put_float_BANG =
        new VncFunction(
                "bytebuf-put-float!",
                VncFunction
                    .meta()
                    .arglists("(bytebuf-put-float! buf d)")
                    .doc("Writes a float (4 bytes) to buffer at the current position, and then " +
                         "increments the position by four.")
                    .examples(
                        "(-> (bytebuf-allocate 8)       \n" +
                        "    (bytebuf-put-float! 64.0)  \n" +
                        "    (bytebuf-put-float! 200.0))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final ByteBuffer buf = Coerce.toVncByteBuffer(args.nth(0)).getValue();
                final VncDouble val = Coerce.toVncDouble(args.nth(1));

                buf.putFloat(val.getValue().floatValue());

                return args.nth(0);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction bytebuf_pos =
        new VncFunction(
                "bytebuf-pos",
                VncFunction
                    .meta()
                    .arglists("(bytebuf-pos buf)")
                    .doc("Returns the buffer's current position.")
                    .examples(
                        "(bytebuf-pos (bytebuf-allocate 10))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final ByteBuffer buf = Coerce.toVncByteBuffer(args.nth(0)).getValue();

                return new VncLong(buf.position());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction bytebuf_pos_BANG =
        new VncFunction(
                "bytebuf-pos!",
                VncFunction
                    .meta()
                    .arglists("(bytebuf-pos! buf pos)")
                    .doc("Sets the buffer's position.")
                    .examples(
                        "(-> (bytebuf-allocate 10)    \n" +
                        "    (bytebuf-pos! 4)         \n" +
                        "    (bytebuf-put-byte! 1)    \n" +
                        "    (bytebuf-pos! 8)         \n" +
                        "    (bytebuf-put-byte! 2))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final ByteBuffer buf = Coerce.toVncByteBuffer(args.nth(0)).getValue();
                final VncLong pos = Coerce.toVncLong(args.nth(1));

                try {
                    buf.position(pos.getValue().intValue());
                }
                catch(RuntimeException ex) {
                    throw new VncException("Failed to set bytebuf position!", ex);
                }

                return args.nth(0);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };



    ///////////////////////////////////////////////////////////////////////////
    // types_ns is namespace of type functions
    ///////////////////////////////////////////////////////////////////////////

    public static final Map<VncVal, VncVal> ns =
            new SymbolMapBuilder()
                .add(bytebuf_Q)
                .add(bytebuf_cast)
                .add(bytebuf_allocate)
                .add(bytebuf_allocate_random)
                .add(bytebuf_capacity)
                .add(bytebuf_limit)
                .add(bytebuf_byte_order_BANG)
                .add(bytebuf_byte_order)
                .add(bytebuf_to_string)
                .add(bytebuf_to_list)
                .add(bytebuf_from_string)
                .add(bytebuf_sub)
                .add(bytebuf_get_byte)
                .add(bytebuf_get_int)
                .add(bytebuf_get_long)
                .add(bytebuf_get_float)
                .add(bytebuf_get_double)
                .add(bytebuf_put_buf_BANG)
                .add(bytebuf_put_byte_BANG)
                .add(bytebuf_put_int_BANG)
                .add(bytebuf_put_long_BANG)
                .add(bytebuf_put_float_BANG)
                .add(bytebuf_put_double_BANG)
                .add(bytebuf_pos)
                .add(bytebuf_pos_BANG)
                .toMap();
}
