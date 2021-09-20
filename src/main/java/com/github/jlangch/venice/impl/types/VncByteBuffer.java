/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2021 Venice
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.custom.VncWrappingTypeDef;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.MetaUtil;


public class VncByteBuffer extends VncVal {

	public VncByteBuffer(final byte[] v) { 
		this(ByteBuffer.wrap(v), null, Constants.Nil); 
	}
	
	public VncByteBuffer(final ByteBuffer v) { 
		this(v, null, Constants.Nil); 
	}
	
	public VncByteBuffer(final ByteBuffer v, final VncVal meta) {
		this(v, null, meta);
	}
	
	public VncByteBuffer(
			final ByteBuffer v, 
			final VncWrappingTypeDef wrappingTypeDef, 
			final VncVal meta
	) {
		super(wrappingTypeDef, meta);
		value = v; 
	}

	
	@Override
	public VncByteBuffer withMeta(final VncVal meta) {
		return new VncByteBuffer(value, getWrappingTypeDef(), meta);
	}
	
	@Override
	public VncByteBuffer wrap(final VncWrappingTypeDef wrappingTypeDef, final VncVal meta) {
		return new VncByteBuffer(value, wrappingTypeDef, meta); 
	}
	
	@Override
	public VncKeyword getType() {
		return isWrapped() ? getWrappingTypeDef().getType() : TYPE;
	}
		
	@Override
	public List<VncKeyword> getSupertypes() {
		return isWrapped() 
				? Arrays.asList(TYPE, VncVal.TYPE)
				: Arrays.asList(VncVal.TYPE);
	}

	public ByteBuffer getValue() { 
		return value; 
	}

	public byte[] getBytes() { 
		return value.array(); 
	}

	public int size() { 
		return value.capacity(); 
	}
	
	public VncList toVncList() { 
		final List<VncVal> list = new ArrayList<>();
		final byte[] buf = value.array();
		for(int ii=0; ii<buf.length; ii++) {
			list.add(new VncLong(buf[ii] & 0x0FF));
		}
		return VncList.ofList(list);
	}
	
	@Override 
	public TypeRank typeRank() {
		return TypeRank.BYTEBUFFER;
	}

	@Override
	public Object convertToJavaObject() {
		return value;
	}
	
	@Override 
	public int compareTo(final VncVal o) {
		if (o == Constants.Nil) {
			return 1;
		}
		else if(Types.isVncByteBuffer(o)) {
			return getValue().compareTo(((VncByteBuffer)o).getValue());
		}

		return super.compareTo(o);
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


    public static final VncKeyword TYPE = new VncKeyword(":core/bytebuf", MetaUtil.typeMeta());

	private static final long serialVersionUID = -1848883965231344442L;

	private final ByteBuffer value;
}