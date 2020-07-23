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
			final InetAddress startAddress,
			final InetAddress endAddress
	) {
		this.cidr = cidr;
		this.startAddress = startAddress;
		this.endAddress = endAddress;
		
		this.startAddressBigInt = new BigInteger(1, startAddress.getAddress());
		this.endAddressBigInt = new BigInteger(1, endAddress.getAddress());

	}

	public static CIDR parse(final String cidr) {
		/* split CIDR to address and prefix part */
		if (cidr.contains("/")) {
			try {
				final int index = cidr.indexOf("/");
				final String addressPart = cidr.substring(0, index);
				final String networkPart = cidr.substring(index + 1);

				final InetAddress inetAddress = InetAddress.getByName(addressPart);
				final int prefixLength = Integer.parseInt(networkPart);

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

				final BigInteger mask = (new BigInteger(1, maskBuffer.array())).not().shiftRight(prefixLength);

				final ByteBuffer buffer = ByteBuffer.wrap(inetAddress.getAddress());
				final BigInteger ipVal = new BigInteger(1, buffer.array());

				final BigInteger startIp = ipVal.and(mask);
				final BigInteger endIp = startIp.add(mask.not());

				final byte[] startIpArr = toBytes(startIp.toByteArray(), targetSize);
				final byte[] endIpArr = toBytes(endIp.toByteArray(), targetSize);
			
				return new CIDR(cidr, 
								InetAddress.getByAddress(startIpArr), 
								InetAddress.getByAddress(endIpArr));
			}
			catch(UnknownHostException ex) {
				throw new VncException("Invalid CIDR IP block '" + cidr + "'", ex);
			}
		} 
		else {
			throw new VncException("Invalid CIDR IP block '" + cidr + "'");
		}
	}

	
	private static byte[] toBytes(final byte[] array, final int targetSize) {
		final int arr_len = array.length > targetSize ? targetSize : array.length;
		final int arr_off = array.length > targetSize ? array.length - targetSize : 0;
		
		final ByteBuffer buf = ByteBuffer.allocate(targetSize);
		buf.position(targetSize - arr_len);
		buf.put(array, arr_off, arr_len);
		return buf.array();
	}

	public String getCidr() {
		return cidr;
	}
	
	public InetAddress getStartInetAddress() {
		return startAddress;
	}

	public InetAddress getEndInetAddress() {
		return endAddress;
	}
	
	public String getStartHostAddress() {
		return startAddress.getHostAddress();
	}

	public String getEndHostAddress() {
		return endAddress.getHostAddress();
	}

	public boolean isInRange(final InetAddress ipAddress) {
		final BigInteger target = new BigInteger(1, ipAddress.getAddress());

		final int st = startAddressBigInt.compareTo(target);
		final int te = target.compareTo(endAddressBigInt);

		return (st == -1 || st == 0) && (te == -1 || te == 0);
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
		return String.format("%s: [%s .. %s]", cidr, startAddress, endAddress);
	}
	
	
	private final String cidr;

	private final InetAddress startAddress;
	private final InetAddress endAddress;

	private final BigInteger startAddressBigInt;
	private final BigInteger endAddressBigInt;
}