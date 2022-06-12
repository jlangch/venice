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
package com.github.jlangch.venice.impl.util.cidr;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import com.github.jlangch.venice.VncException;


/**
 * A class that gets an IP range from CIDR specification. It supports
 * both IPv4 and IPv6.
 *
 * <p>CIDR (Classless Inter-Domain Routing) block functions. Parses CIDR
 * IP notations to IP address ranges. It supports both IPv4 and IPv6.
 *
 * <p>See: https://en.wikipedia.org/wiki/Classless_Inter-Domain_Routing
 *
 * <p>CIDR tool: // https://www.ipaddressguide.com/cidr
 *
 * <p>Example:
 * <pre>
 *    CIDR cidr = new CIDR("222.192.0.0/11");
 *    System.out.println(cidr.toString());
 * </pre>
 */
public class CIDR implements Comparable<CIDR> {

    private CIDR(
            final String cidrNotation,
            final int cidrRange,
            final InetAddress startAddress,
            final InetAddress endAddress,
            final boolean ip4
    ) {
        this.cidrNotation = cidrNotation;
        this.cidrRange = cidrRange;
        this.ip4 = ip4;

        this.lowInet = startAddress;
        this.highInet = endAddress;

        this.lowBigInt = new BigInteger(1, startAddress.getAddress());
        this.highBigInt = new BigInteger(1, endAddress.getAddress());
    }

    /**
     * Parses a CIDR string, e.g. "192.168.0.1/16".
     *
     * <p>Note: "192.168.0.1" accepted as if it was /32 ("192.168.0.1/32"),
     *
     * @param cidr a CIDR string
     * @return the parsed CIDR
     */
    public static CIDR parse(final String cidr) {
        try {
            final int index = cidr.indexOf("/");
            final String addressPart = index < 0 ? cidr : cidr.substring(0, index);
            final InetAddress inetAddress = InetAddress.getByName(addressPart);

            ByteBuffer maskBuffer;
            int targetSize;

            if (inetAddress.getAddress().length == 4) {
                // 4 bytes
                maskBuffer = ByteBuffer.allocate(4)
                                       .putInt(-1);
                targetSize = 4;
            }
            else {
                // 16 bytes
                maskBuffer = ByteBuffer.allocate(16)
                                       .putLong(-1L)
                                       .putLong(-1L);
                targetSize = 16;
            }

            final String cidrRangePart = index < 0 ? (targetSize == 4 ? "32" : "128") : cidr.substring(index + 1);
            final int cidrRange = Integer.parseInt(cidrRangePart); // number of leading 1-bits in the mask

            final BigInteger mask = new BigInteger(1, maskBuffer.array()).not().shiftRight(cidrRange);

            final ByteBuffer buffer = ByteBuffer.wrap(inetAddress.getAddress());
            final BigInteger ipVal = new BigInteger(1, buffer.array());

            final BigInteger startIp = ipVal.and(mask);
            final BigInteger endIp = startIp.add(mask.not());

            final byte[] startIpArr = toBytes(startIp.toByteArray(), targetSize);
            final byte[] endIpArr = toBytes(endIp.toByteArray(), targetSize);

            return new CIDR(cidr,
                            cidrRange,
                            InetAddress.getByAddress(startIpArr),
                            InetAddress.getByAddress(endIpArr),
                            targetSize == 4);
        }
        catch(UnknownHostException ex) {
            throw new VncException("Invalid CIDR IP block '" + cidr + "'", ex);
        }
    }


    public String getNotation() {
        return cidrNotation;
    }

    public int getRange() {
        return cidrRange;
    }

    public InetAddress getLowInetAddress() {
        return lowInet;
    }

    public InetAddress getHighInetAddress() {
        return highInet;
    }

    public String getLowHostAddress() {
        return lowInet.getHostAddress();
    }

    public String getHighHostAddress() {
        return highInet.getHostAddress();
    }

    public boolean isIP4() {
        return ip4;
    }

    public boolean isIP6() {
        return !ip4;
    }

    public boolean getLowAddressBit(final int n) {
        return lowBigInt.testBit(n);
    }

    public boolean getHighAddressBit(final int n) {
        return highBigInt.testBit(n);
    }

    public boolean isInRange(final InetAddress ipAddress) {
        final boolean matchedIpType = (ip4 && ipAddress instanceof Inet4Address)
                                        || (!ip4 && ipAddress instanceof Inet6Address);

        if (matchedIpType) {
            final BigInteger target = new BigInteger(1, ipAddress.getAddress());

            final int st = lowBigInt.compareTo(target);
            if (st <= 0) {
                final int te = target.compareTo(highBigInt);
                return te <= 0;
            }
            else {
                return false;
            }
        }
        else {
            return false;
        }
    }

    public boolean isInRange(final String ipAddress) {
        try {
            return isInRange(InetAddress.getByName(ipAddress));
        }
        catch(Exception ex) {
            throw new VncException("Invalid IP address '" + ipAddress + "'", ex);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((highBigInt == null) ? 0 : highBigInt.hashCode());
        result = prime * result + ((lowBigInt == null) ? 0 : lowBigInt.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CIDR other = (CIDR) obj;
        if (highBigInt == null) {
            if (other.highBigInt != null)
                return false;
        } else if (!highBigInt.equals(other.highBigInt))
            return false;
        if (lowBigInt == null) {
            if (other.lowBigInt != null)
                return false;
        } else if (!lowBigInt.equals(other.lowBigInt))
            return false;
        return true;
    }

    @Override
    public final int compareTo(final CIDR other) {
        final int lowDiff = this.lowBigInt.compareTo(other.lowBigInt);
        if (lowDiff != 0) {
            return lowDiff;
        }
        final int highDiff = this.highBigInt.compareTo(other.highBigInt);
        if (highDiff != 0) {
            return -highDiff; // negative = widest first
        }
        return 0;
    }

    @Override
    public String toString() {
        return String.format("%s: [%s .. %s]", cidrNotation, lowInet, highInet);
    }

    public static String toBinaryString(final InetAddress inet, final boolean octetSpacing) {
        final byte[] bytes = inet.getAddress();

        final StringBuilder sb = new StringBuilder();
        for(int ii=0; ii<bytes.length; ii++) {
            if (octetSpacing && ii>0) sb.append(" ");
            sb.append(toBinary(bytes[ii]));
        }
        return sb.toString();
    }

    private static byte[] toBytes(final byte[] array, final int targetSize) {
        final int arr_len = array.length > targetSize ? targetSize : array.length;
        final int arr_off = array.length > targetSize ? array.length - targetSize : 0;

        final ByteBuffer buf = ByteBuffer.allocate(targetSize);
        buf.position(targetSize - arr_len);
        buf.put(array, arr_off, arr_len);
        return buf.array();
    }

    private static String toBinary(final byte b) {
        final StringBuilder sb = new StringBuilder();

        final String s = Integer.toString(b & 0xff, 2);
        for(int jj=s.length(); jj<8; jj++) sb.append('0'); // prefix with '0's
        sb.append(s);

        return sb.toString();
    }


    private final String cidrNotation;
    private final int cidrRange;
    private final boolean ip4;

    private final InetAddress lowInet;
    private final InetAddress highInet;

    private final BigInteger lowBigInt;
    private final BigInteger highBigInt;
}
