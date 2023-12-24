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

import java.net.InetAddress;
import java.util.Map;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;
import com.github.jlangch.venice.impl.util.cidr.CIDR;
import com.github.jlangch.venice.impl.util.cidr.CidrTrie;


public class CidrFunctions {

    ///////////////////////////////////////////////////////////////////////////
    // CIDR
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction parse =
        new VncFunction(
                "cidr/parse",
                VncFunction
                    .meta()
                    .arglists("(cidr/parse cidr)")
                    .doc("Parses CIDR IP blocks to an IP address range. Supports both IPv4 and IPv6.")
                    .examples(
                        "(cidr/parse \"222.192.0.0/11\")",
                        "(cidr/parse \"2001:0db8:85a3:08d3:1319:8a2e:0370:7347/64\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal cidr = args.first();
                if (Types.isVncJavaObject(cidr, CIDR.class)) {
                    return cidr;
                }
                else {
                    return new VncJavaObject(CIDR.parse(Coerce.toVncString(cidr).getValue()));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction in_range_Q =
        new VncFunction(
                "cidr/in-range?",
                VncFunction
                    .meta()
                    .arglists("(cidr/in-range? ip cidr)")
                    .doc(
                        "Returns true if the ip adress is within the ip range of the cidr else false. " +
                        "ip may be a string or a :java.net.InetAddress, cidr may be a string " +
                        "or a CIDR Java object obtained from 'cidr/parse'.")
                    .examples(
                        "(cidr/in-range? \"222.220.0.0\" \"222.220.0.0/11\")",
                        "(cidr/in-range? (inet/inet-addr \"222.220.0.0\") \"222.220.0.0/11\")",
                        "(cidr/in-range? \"222.220.0.0\" (cidr/parse \"222.220.0.0/11\"))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final VncVal ip = args.first();
                final VncVal cidr_ = args.second();


                if (Types.isVncString(ip)) {
                    if (Types.isVncString(cidr_)) {
                        final CIDR cidr = CIDR.parse(((VncString)cidr_).getValue());
                        return VncBoolean.of(cidr.isInRange(((VncString)ip).getValue()));
                    }
                    else if (Types.isVncJavaObject(cidr_, CIDR.class)) {
                        final CIDR cidr = (CIDR)((VncJavaObject)cidr_).getDelegate();
                        return VncBoolean.of(cidr.isInRange(((VncString)ip).getValue()));
                    }
                    else {
                        throw new VncException(String.format(
                                "Invalid argument type %s while calling function 'cidr/in-range?'",
                                Types.getType(cidr_)));
                    }
                }
                else if (Types.isVncJavaObject(ip, InetAddress.class)) {
                    if (Types.isVncString(cidr_)) {
                        final InetAddress inet = (InetAddress)((VncJavaObject)ip).getDelegate();
                        final CIDR cidr = CIDR.parse(((VncString)cidr_).getValue());
                        return VncBoolean.of(cidr.isInRange(inet));
                    }
                    else if (Types.isVncJavaObject(cidr_, CIDR.class)) {
                        final InetAddress inet = (InetAddress)((VncJavaObject)ip).getDelegate();
                        final CIDR cidr = (CIDR)((VncJavaObject)cidr_).getDelegate();
                        return VncBoolean.of(cidr.isInRange(inet));
                    }
                    else {
                        throw new VncException(String.format(
                                "Invalid argument type %s while calling function 'cidr/in-range?'",
                                Types.getType(cidr_)));
                    }
                }
                else {
                    throw new VncException(String.format(
                            "Invalid argument type %s while calling function 'cidr/in-range?'",
                            Types.getType(ip)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction start_inet_addr =
        new VncFunction(
                "cidr/start-inet-addr",
                VncFunction
                    .meta()
                    .arglists("(cidr/start-inet-addr cidr)")
                    .doc("Returns the start inet address of a CIDR IP block.")
                    .examples(
                        "(cidr/start-inet-addr \"222.192.0.0/11\")",
                        "(cidr/start-inet-addr \"2001:0db8:85a3:08d3:1319:8a2e:0370:7347/64\")",
                        "(cidr/start-inet-addr (cidr/parse \"222.192.0.0/11\"))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal arg = args.first();
                if (Types.isVncJavaObject(arg, CIDR.class)) {
                    final CIDR cidr = (CIDR)((VncJavaObject)arg).getDelegate();
                    return new VncJavaObject(cidr.getLowInetAddress());
                }
                else if (Types.isVncString(arg)) {
                    final CIDR cidr = CIDR.parse(((VncString)arg).getValue());
                    return new VncJavaObject(cidr.getLowInetAddress());
                }
                else {
                    throw new VncException(String.format(
                            "Invalid argument type %s while calling function 'cidr/start-inet-addr'",
                            Types.getType(arg)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction end_inet_addr =
            new VncFunction(
                    "cidr/end-inet-addr",
                    VncFunction
                        .meta()
                        .arglists("(cidr/end-inet-addr cidr)")
                        .doc("Returns the end inet address of a CIDR IP block.")
                        .examples(
                            "(cidr/end-inet-addr \"222.192.0.0/11\")",
                            "(cidr/end-inet-addr \"2001:0db8:85a3:08d3:1319:8a2e:0370:7347/64\")",
                            "(cidr/end-inet-addr (cidr/parse \"222.192.0.0/11\"))")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertArity(this, args, 1);

                    final VncVal arg = args.first();
                    if (Types.isVncJavaObject(arg, CIDR.class)) {
                        final CIDR cidr = (CIDR)((VncJavaObject)arg).getDelegate();
                        return new VncJavaObject(cidr.getHighInetAddress());
                    }
                    else if (Types.isVncString(arg)) {
                        final CIDR cidr = CIDR.parse(((VncString)arg).getValue());
                        return new VncJavaObject(cidr.getHighInetAddress());
                    }
                    else {
                        throw new VncException(String.format(
                                "Invalid argument type %s while calling function 'cidr/end-inet-addr'",
                                Types.getType(arg)));
                    }
                }

                private static final long serialVersionUID = -1848883965231344442L;
            };



    ///////////////////////////////////////////////////////////////////////////
    // Trie
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction trie =
        new VncFunction(
                "cidr/trie",
                VncFunction
                    .meta()
                    .arglists("(cidr/trie)")
                    .doc("Create a new mutable concurrent CIDR trie.")
                    .examples(
                        "(do                                                \n" +
                        "  (let [trie (cidr/trie)]                          \n" +
                        "    (cidr/insert trie                              \n" +
                        "                 (cidr/parse \"192.16.10.0/24\")   \n" +
                        "                 \"Germany\")                      \n" +
                        "    (cidr/lookup trie \"192.16.10.15\")))            ")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                return new VncJavaObject(new CidrTrie<VncVal>());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction size =
            new VncFunction(
                    "cidr/size",
                    VncFunction
                        .meta()
                        .arglists("(cidr/size trie)")
                        .doc("Returns the size of the trie.")
                        .examples(
                            "(do                                                \n" +
                            "  (let [trie (cidr/trie)]                          \n" +
                            "    (cidr/insert trie                              \n" +
                            "                 (cidr/parse \"192.16.10.0/24\")   \n" +
                            "                 \"Germany\")                      \n" +
                            "    (cidr/size trie)))                               ")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertArity(this, args, 1);

                    @SuppressWarnings("unchecked")
                    final CidrTrie<VncVal> trie = Coerce.toVncJavaObject(args.first(), CidrTrie.class);

                    return new VncLong(trie.size());
                }

                private static final long serialVersionUID = -1848883965231344442L;
            };

    public static VncFunction insert =
        new VncFunction(
                "cidr/insert",
                VncFunction
                    .meta()
                    .arglists("(cidr/insert trie cidr value)")
                    .doc(
                        "Insert a new CIDR / value relation into trie. Works with " +
                        "IPv4 and IPv6. Please keep IPv4 and IPv6 CIDRs in " +
                        "different tries.")
                    .examples(
                        "(do                                                \n" +
                        "  (let [trie (cidr/trie)]                          \n" +
                        "    (cidr/insert trie                              \n" +
                        "                 (cidr/parse \"192.16.10.0/24\")   \n" +
                        "                 \"Germany\")                      \n" +
                        "    (cidr/lookup trie \"192.16.10.15\")))            ")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 3);

                @SuppressWarnings("unchecked")
                final CidrTrie<VncVal> trie = Coerce.toVncJavaObject(args.first(), CidrTrie.class);

                final CIDR cidr = Coerce.toVncJavaObject(args.second(), CIDR.class);

                trie.insert(cidr, args.third());

                return Constants.Nil;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction lookup =
        new VncFunction(
                "cidr/lookup",
                VncFunction
                    .meta()
                    .arglists("(cidr/lookup trie ip)")
                    .doc(
                        "Lookup the associated value of a CIDR in the trie. " +
                        "A cidr \"192.16.10.0/24\" or an inet address " +
                        "\"192.16.10.15\" can be passed as ip.")
                    .examples(
                        "(do                                                \n" +
                        "  (let [trie (cidr/trie)]                          \n" +
                        "    (cidr/insert trie                              \n" +
                        "                 (cidr/parse \"192.16.10.0/24\")   \n" +
                        "                 \"Germany\")                      \n" +
                        "    (cidr/lookup trie \"192.16.10.15\")))            ")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                @SuppressWarnings("unchecked")
                final CidrTrie<VncVal> trie = Coerce.toVncJavaObject(args.first(), CidrTrie.class);

                if (Types.isVncString(args.second())) {
                    final String ip = ((VncString)args.second()).getValue();
                    final VncVal val = trie.getValue(ip);
                    return val == null ? Constants.Nil : val;
                }
                else if (Types.isVncJavaObject(args.second())) {
                    final CIDR cidr = Coerce.toVncJavaObject(args.second(), CIDR.class);
                    final VncVal val = trie.getValue(cidr);
                    return val == null ? Constants.Nil : val;
                }
                else {
                    throw new VncException(String.format(
                            "Invalid second argument type %s while calling function 'cidr/lookup'",
                            Types.getType(args.second())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction lookup_mixed =
        new VncFunction(
                "cidr/lookup-mixed",
                VncFunction
                    .meta()
                    .arglists("(cidr/lookup-mixed trie-ip4 trie-ip6 ip)")
                    .doc(
                        "Lookup the associated value of a CIDR in the IPv4 or IPv6 trie. " +
                        "A cidr \"192.16.10.0/24\" or an inet address \"192.16.10.15\" " +
                        "(IPv4 or IPv6) can be passed as ip. The ip will then be routed " +
                        "to the corresponding IPv4 or IPv6 trie.")
                    .examples(
                        "(do                                               \n" +
                        "  (let [trie-ip4 (cidr/trie)                      \n" +
                        "        trie-ip6 (cidr/trie)]                     \n" +
                        "    (cidr/insert trie-ip4                         \n" +
                        "                 (cidr/parse \"192.16.10.0/24\")  \n" +
                        "                 \"Germany\")                     \n" +
                        "    (cidr/lookup-mixed trie-ip4                   \n" +
                        "                       trie-ip6                   \n" +
                        "                       \"192.16.10.15\")))          ")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 3);

                if (Types.isVncString(args.third())) {
                    final String ip = ((VncString)args.third()).getValue();

                    @SuppressWarnings("unchecked")
                    final CidrTrie<VncVal> trie = Coerce.toVncJavaObject(
                                                        ip.contains(".") ? args.first() : args.second(),
                                                        CidrTrie.class);
                    final VncVal val = trie.getValue(ip);
                    return val == null ? Constants.Nil : val;
                }
                else if (Types.isVncJavaObject(args.third())) {
                    final CIDR cidr = Coerce.toVncJavaObject(args.third(), CIDR.class);

                    @SuppressWarnings("unchecked")
                    final CidrTrie<VncVal> trie = Coerce.toVncJavaObject(
                                                        cidr.isIP4() ? args.first() : args.second(),
                                                        CidrTrie.class);
                    final VncVal val = trie.getValue(cidr);
                    return val == null ? Constants.Nil : val;
                }
                else {
                    throw new VncException(String.format(
                            "Invalid second argument type %s while calling function 'cidr/lookup-mixed'",
                            Types.getType(args.third())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction lookup_reverse =
            new VncFunction(
                    "cidr/lookup-reverse",
                    VncFunction
                        .meta()
                        .arglists("(cidr/lookup-reverse trie ip)")
                        .doc("Reverse lookup a CIDR in the trie given an IP address")
                        .examples(
                            "(do                                                \n" +
                            "  (let [trie (cidr/trie)]                          \n" +
                            "    (cidr/insert trie                              \n" +
                            "                 (cidr/parse \"192.16.10.0/24\")   \n" +
                            "                 \"Germany\")                      \n" +
                            "    (cidr/lookup-reverse trie \"192.16.10.15\")))    ")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertArity(this, args, 2);

                    @SuppressWarnings("unchecked")
                    final CidrTrie<VncVal> trie = Coerce.toVncJavaObject(args.first(), CidrTrie.class);

                    if (Types.isVncString(args.second())) {
                        final String ip = ((VncString)args.second()).getValue();
                        final CIDR cidr = trie.getCIDR(ip);
                        return cidr == null ? Constants.Nil : new VncJavaObject(cidr);
                    }
                    else {
                        throw new VncException(String.format(
                                "Invalid second argument type %s while calling function 'cidr/lookup-reverse'",
                                Types.getType(args.second())));
                    }
                }

                private static final long serialVersionUID = -1848883965231344442L;
            };



    ///////////////////////////////////////////////////////////////////////////
    // types_ns is namespace of type functions
    ///////////////////////////////////////////////////////////////////////////

    public static final Map<VncVal, VncVal> ns =
            new SymbolMapBuilder()
                    .add(parse)
                    .add(in_range_Q)
                    .add(trie)
                    .add(size)
                    .add(insert)
                    .add(lookup)
                    .add(lookup_mixed)
                    .add(lookup_reverse)
                    .add(start_inet_addr)
                    .add(end_inet_addr)
                    .toMap();
}
