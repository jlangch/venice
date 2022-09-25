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

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Map;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncInteger;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;


public class InetFunctions {

    ///////////////////////////////////////////////////////////////////////////
    // InetAddress
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction ip4_Q =
        new VncFunction(
                "inet/ip4?",
                VncFunction
                    .meta()
                    .arglists("(inet/ip4? addr)")
                    .doc("Returns true if addr is an IPv4 address.")
                    .examples(
                        "(inet/ip4? \"222.192.0.0\")",
                        "(inet/ip4? (inet/inet-addr \"222.192.0.0\"))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal addr = args.first();

                if (Types.isVncString(addr)) {
                    return VncBoolean.of(((VncString)addr).getValue().contains("."));
                }
                else if (Types.isVncJavaObject(addr)) {
                    final Object inet = ((VncJavaObject)addr).getDelegate();
                    return VncBoolean.of(inet instanceof Inet4Address);
                }
                else {
                    return VncBoolean.False;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction ip6_Q =
        new VncFunction(
                "inet/ip6?",
                VncFunction
                    .meta()
                    .arglists("(inet/ip6? addr)")
                    .doc("Returns true if addr is an IPv6 address.")
                    .examples(
                        "(inet/ip6? \"2001:0db8:85a3:08d3:1319:8a2e:0370:7347\")",
                        "(inet/ip6? (inet/inet-addr \"2001:0db8:85a3:08d3:1319:8a2e:0370:7347\"))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal addr = args.first();

                if (Types.isVncString(addr)) {
                    return VncBoolean.of(((VncString)addr).getValue().contains(":"));
                }
                else if (Types.isVncJavaObject(addr)) {
                    final Object inet = ((VncJavaObject)addr).getDelegate();
                    return VncBoolean.of(inet instanceof Inet6Address);
                }
                else {
                    return VncBoolean.False;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction multicast_addr_Q =
        new VncFunction(
                "inet/multicast-addr?",
                VncFunction
                    .meta()
                    .arglists("(inet/multicast-addr? addr)")
                    .doc("Returns true if addr is a multicast address.")
                    .examples(
                        "(inet/multicast-addr? \"224.0.0.1\")",
                        "(inet/multicast-addr? (inet/inet-addr \"224.0.0.1\"))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal addr = args.first();

                if (Types.isVncString(addr)) {
                    final String ipAddr = ((VncString)addr).getValue();
                    try {
                        return VncBoolean.of(
                                InetAddress.getByName(ipAddr)
                                           .isMulticastAddress());
                    }
                    catch(Exception ex) {
                        throw new VncException("Not an IP address: '" + ipAddr + "'");
                    }
                }
                else if (Types.isVncJavaObject(addr, InetAddress.class)) {
                    final InetAddress inet = Coerce.toVncJavaObject(addr, InetAddress.class);
                    return VncBoolean.of(inet.isMulticastAddress());
                }
                else {
                    throw new VncException("Not an IP address: '" + addr + "'");
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction linklocal_addr_Q =
        new VncFunction(
                "inet/linklocal-addr?",
                VncFunction
                    .meta()
                    .arglists("(inet/linklocal-addr? addr)")
                    .doc("Returns true if addr is a link local address.")
                    .examples(
                        "(inet/linklocal-addr? \"169.254.0.0\")",
                        "(inet/linklocal-addr? (inet/inet-addr \"169.254.0.0\"))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal addr = args.first();

                if (Types.isVncString(addr)) {
                    final String ipAddr = ((VncString)addr).getValue();
                    try {
                        return VncBoolean.of(
                                InetAddress.getByName(ipAddr)
                                           .isLinkLocalAddress());
                    }
                    catch(Exception ex) {
                        throw new VncException("Not an IP address: '" + ipAddr + "'");
                    }
                }
                else if (Types.isVncJavaObject(addr, InetAddress.class)) {
                    final InetAddress inet = Coerce.toVncJavaObject(addr, InetAddress.class);
                    return VncBoolean.of(inet.isLinkLocalAddress());
                }
                else {
                    throw new VncException("Not an IP address: '" + addr + "'");
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction sitelocal_addr_Q =
        new VncFunction(
                "inet/sitelocal-addr?",
                VncFunction
                    .meta()
                    .arglists("(inet/sitelocal-addr? addr)")
                    .doc("Returns true if addr is a site local address.")
                    .examples(
                        "(inet/sitelocal-addr? \"192.168.0.0\")",
                        "(inet/sitelocal-addr? (inet/inet-addr \"192.168.0.0\"))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal addr = args.first();

                if (Types.isVncString(addr)) {
                    final String ipAddr = ((VncString)addr).getValue();
                    try {
                        return VncBoolean.of(
                                InetAddress.getByName(ipAddr)
                                           .isSiteLocalAddress());
                    }
                    catch(Exception ex) {
                        throw new VncException("Not an IP address: '" + ipAddr + "'");
                    }
                }
                else if (Types.isVncJavaObject(addr, InetAddress.class)) {
                    final InetAddress inet = Coerce.toVncJavaObject(addr, InetAddress.class);
                    return VncBoolean.of(inet.isSiteLocalAddress());
                }
                else {
                    throw new VncException("Not an IP address: '" + addr + "'");
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction inet_addr =
        new VncFunction(
                "inet/inet-addr",
                VncFunction
                    .meta()
                    .arglists("(inet/inet-addr addr)")
                    .doc("Converts a stringified IPv4 or IPv6 to a Java InetAddress.")
                    .examples(
                        "(inet/inet-addr \"222.192.0.0\")",
                        "(inet/inet-addr \"2001:0db8:85a3:08d3:1319:8a2e:0370:7347\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final String ip = Coerce.toVncString(args.first()).getValue();

                try {
                    return new VncJavaObject(InetAddress.getByName(ip));
                }
                catch(Exception ex) {
                    throw new VncException("Not an IP address: '" + ip + "'");
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction inet_addr_to_bytes =
        new VncFunction(
                "inet/inet-addr-to-bytes",
                VncFunction
                    .meta()
                    .arglists("(inet/inet-addr-to-bytes addr)")
                    .doc(
                        "Converts a stringified IPv4/IPv6 address or a Java InetAddress " +
                        "to an InetAddress byte vector.")
                    .examples(
                        "(inet/inet-addr-to-bytes \"222.192.12.0\")",
                        "(inet/inet-addr-to-bytes \"2001:0db8:85a3:08d3:1319:8a2e:0370:7347\")",
                        "(inet/inet-addr-to-bytes (inet/inet-addr \"222.192.0.0\"))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal ip = args.first();

                if (Types.isVncString(ip)) {
                    final String ip_ = ((VncString)ip).getValue();
                    try {
                        final byte[] bytes = InetAddress.getByName(ip_).getAddress();
                        final VncInteger[] ints = new VncInteger[bytes.length];
                        for(int ii=0; ii<bytes.length; ii++) {
                            ints[ii] = new VncInteger(Byte.toUnsignedInt(bytes[ii]));
                        }
                        return VncVector.of(ints);
                    }
                    catch(Exception ex) {
                        throw new VncException("Not an IP address: '" + ip_ + "'");
                    }
                }
                else if (Types.isVncJavaObject(ip, InetAddress.class)) {
                    final InetAddress ip_ = (InetAddress)((VncJavaObject)ip).getDelegate();
                    final byte[] bytes = ip_.getAddress();
                    final VncInteger[] ints = new VncInteger[bytes.length];
                    for(int ii=0; ii<bytes.length; ii++) {
                        ints[ii] = new VncInteger(Byte.toUnsignedInt(bytes[ii]));
                    }
                    return VncVector.of(ints);
                }
                else {
                    throw new VncException(String.format(
                            "Invalid argument type %s while calling function 'inet/in-range?'",
                            Types.getType(ip)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction inet_addr_from_bytes =
        new VncFunction(
                "inet/inet-addr-from-bytes",
                VncFunction
                    .meta()
                    .arglists("(inet/inet-addr-bytes addr)")
                    .doc(
                        "Converts a IPv4 or IPv6 byte address (a vector of unsigned " +
                        "integers) to a Java InetAddress.")
                    .examples(
                        "(inet/inet-addr-from-bytes [222I 192I 12I 0I])",
                        "(inet/inet-addr-from-bytes [32I 1I 13I 184I 133I 163I 8I 211I 19I 25I 138I 46I 3I 112I 115I 71I])")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncList ints = Coerce.toVncList(args.first());

                try {
                    final byte[] addr = new byte[ints.size()];
                    for(int ii=0; ii<ints.size(); ii++) {
                        addr[ii] = Coerce.toVncInteger(ints.nth(ii)).getValue().byteValue();
                    }
                    return new VncJavaObject(InetAddress.getByAddress(addr));
                }
                catch(Exception ex) {
                    throw new VncException("Not an IP address: '" + args.first() + "'");
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };



    ///////////////////////////////////////////////////////////////////////////
    // types_ns is namespace of type functions
    ///////////////////////////////////////////////////////////////////////////

    public static final Map<VncVal, VncVal> ns =
            new SymbolMapBuilder()
                    .add(inet_addr)
                    .add(ip4_Q)
                    .add(ip6_Q)
                    .add(multicast_addr_Q)
                    .add(linklocal_addr_Q)
                    .add(sitelocal_addr_Q)
                    .add(inet_addr_to_bytes)
                    .add(inet_addr_from_bytes)
                    .toMap();
}
