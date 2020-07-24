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
package com.github.jlangch.venice.impl.util;

import java.math.BigInteger;
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
public class CIDR {
	
	private CIDR(
			final String cidr,
			final int cidrRange,
			final InetAddress startAddress,
			final InetAddress endAddress,
			final boolean ip4
	) {
		this.cidr = cidr;
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
			final String cidrRangePart = index < 0 ? "32" : cidr.substring(index + 1);

			final InetAddress inetAddress = InetAddress.getByName(addressPart);
			final int cidrRange = Integer.parseInt(cidrRangePart); // number of leading 1-bits in the mask

			ByteBuffer maskBuffer;
			int targetSize;
			
			if (inetAddress.getAddress().length == 4) {
				maskBuffer = ByteBuffer.allocate(4)
									   .putInt(-1);
				targetSize = 4;
			} 
			else {
				maskBuffer = ByteBuffer.allocate(16)
									   .putLong(-1L)
									   .putLong(-1L);
				targetSize = 16;
			}

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


	public String getCidr() {
		return cidr;
	}

	public int getCidrRange() {
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
	
	public int ipBits() {
		return ip4 ? 32 : 128;
	}

    public boolean getLowAddressBit(final int n) {
        return lowBigInt.testBit(n);
    }

    public boolean getHighAddressBit(final int n) {
        return highBigInt.testBit(n);
    }

	public boolean isInRange(final InetAddress ipAddress) {
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
	
	public boolean isInRange(final String ipAddress) {
		try {
			return isInRange(InetAddress.getByName(ipAddress));
		}
		catch(Exception ex) {
			throw new VncException("Invalid IP address '" + ipAddress + "'", ex);
		}
	}
		
	@Override
	public String toString() {
		return String.format("%s: [%s .. %s]", cidr, lowInet, highInet);
	}
	
	public static String toBinaryString(final BigInteger bigint, final int nBits) {
		final StringBuilder sb = new StringBuilder();
		for(int ii=nBits-1; ii>=0; ii--) {
			sb.append(bigint.testBit(ii) ? "1" : "0");
			if (ii>0 && ii % 8 == 0) sb.append(" ");
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
	
	
	
	private final String cidr;
	private final int cidrRange;
	private final boolean ip4;
	
	private final InetAddress lowInet;
	private final InetAddress highInet;

	private final BigInteger lowBigInt;
	private final BigInteger highBigInt;
}