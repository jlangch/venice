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
package com.github.jlangch.venice.impl.types.collections;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.javainterop.JavaInteropUtil;
import com.github.jlangch.venice.impl.types.IVncJavaObject;
import com.github.jlangch.venice.impl.types.VncVal;


public class VncJavaSet extends VncCollection implements IVncJavaObject {
	
	public VncJavaSet(final Set<Object> value) {
		this.value = value;
	}
	
	
	@Override
	public Object getDelegate() {
		return value;
	}

	public VncSet empty() {
		return new VncSet();
	}
	
	public void add(final VncVal val) {
		value.add(JavaInteropUtil.convertToVncVal(val));
	}
	
	public boolean contains(final VncVal val) {
		return value.contains(JavaInteropUtil.convertToVncVal(val));
	}
	
	public VncSet copy() {
		return new VncSet(toList());
	}
	
	public VncList toList() {
		return new VncList(getVncValueList());
	}

	public VncSet toVncSet() {
		return new VncSet(toList());
	}

	public VncList toVncList() {
		return new VncList(toList());
	}

	public VncVector toVncVector() {
		return new VncVector(toList());
	}

	public int size() {
		return value.size();
	}
	
	public boolean isEmpty() {
		return value.isEmpty();
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
		VncJavaSet other = (VncJavaSet) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override 
	public String toString() {
		return toString(true);
	}
	
	@Override
	public String toString(final boolean print_readably) {
		return "#{" + Printer.join(getVncValueList(), " ", print_readably) + "}";
	}

	private List<VncVal> getVncValueList() {
		return value
				.stream()
				.map(v -> JavaInteropUtil.convertToVncVal(v))
				.collect(Collectors.toList());
	}
	
	
    private static final long serialVersionUID = -1848883965231344442L;

	private final Set<Object> value;	
}