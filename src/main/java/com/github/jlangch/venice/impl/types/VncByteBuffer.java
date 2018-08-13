/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2018 Venice
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
package com.github.jlangch.venice.impl.types;

import java.nio.ByteBuffer;

import com.github.jlangch.venice.impl.types.collections.VncList;


public class VncByteBuffer extends VncVal {

	public VncByteBuffer(final ByteBuffer v) { 
		value = v; 
	}

	public VncByteBuffer copy() { 
		final VncByteBuffer v = new VncByteBuffer(value);
		v.setMeta(getMeta());
		return v;
	}

	public ByteBuffer getValue() { 
		return value; 
	}

	public int size() { 
		return value.capacity(); 
	}
	
	public VncList toVncList() { 
		final VncList list = new VncList();
		final byte[] buf = value.array();
		for(int ii=0; ii<buf.length; ii++) {
			list.addAtEnd(new VncLong(buf[ii] & 0x0FF));
		}
		return list; 
	}
	
	@Override 
	public int compareTo(final VncVal o) {
		return Types.isVncDouble(o) ? getValue().compareTo(((VncByteBuffer)o).getValue()) : 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		VncByteBuffer other = (VncByteBuffer) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override 
	public String toString() {
		final byte[] arr = value.array();
		
		final StringBuilder sb = new StringBuilder();
		sb.append("[");
		
		for(int ii=0; ii<100 && ii<arr.length; ii++) {
			if (ii>0) sb.append(" ");
			sb.append((long)arr[ii] & 0x0FF);
		}
		
		if (arr.length > 100) {
			 sb.append(" ...");
		}
		
		sb.append("]");
		return sb.toString();
	}


	private final ByteBuffer value;
}