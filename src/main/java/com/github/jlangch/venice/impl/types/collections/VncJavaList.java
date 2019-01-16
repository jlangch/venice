/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2019 Venice
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.javainterop.JavaInteropUtil;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.IVncJavaObject;
import com.github.jlangch.venice.impl.types.Types;
import com.github.jlangch.venice.impl.types.VncVal;


public class VncJavaList extends VncSequence implements IVncJavaObject {

	public VncJavaList() {
		this(null, null);
	}

	public VncJavaList(final VncVal meta) {
		this(null, meta);
	}

	public VncJavaList(final Collection<Object> val) {
		this(val, null);
	}

	public VncJavaList(final Collection<Object> val, final VncVal meta) {
		super(meta == null ? Constants.Nil : meta);
		addAll(val);
	}
	
	
	@Override
	public Object getDelegate() {
		return value;
	}
	
	@Override
	public VncList empty() {
		return new VncList(getMeta());
	}

	@Override
	public VncList copy() {
		return new VncList(getList(), getMeta());
	}

	@Override
	public VncList withMeta(final VncVal meta) {
		return new VncList(getList(), meta);
	}

	@Override
	public void forEach(Consumer<? super VncVal> action) {
		value.forEach(v -> action.accept(JavaInteropUtil.convertToVncVal(v)));
	}

	@Override
	public List<VncVal> getList() { 
		return value
				.stream()
				.map(v -> JavaInteropUtil.convertToVncVal(v))
				.collect(Collectors.toList());
	}
	
	@Override
	public boolean isList() { 
		return true; 
	}

	@Override
	public int size() {
		return value.size();
	}
	
	@Override
	public boolean isEmpty() {
		return value.isEmpty();
	}

	@Override
	public VncVal nth(final int idx) {
		if (idx < 0 || idx >= value.size()) {
			throw new VncException("nth: index out of range");
		}

		return JavaInteropUtil.convertToVncVal(value.get((int)idx));
	}

	@Override
	public VncVal nthOrDefault(final int idx, final VncVal defaultVal) {
		return idx >= 0 && idx < value.size() ? nth(idx) : defaultVal;
	}

	@Override
	public VncVal first() {
		return isEmpty() ? Constants.Nil : nth(0);
	}

	@Override
	public VncVal second() {
		return size() < 2 ? Constants.Nil : nth(1);
	}

	@Override
	public VncVal third() {
		return size() < 3 ? Constants.Nil : nth(2);
	}

	@Override
	public VncVal last() {
		return isEmpty() ? Constants.Nil : nth(value.size()-1);
	}

	@Override
	public VncList rest() {
		if (isEmpty()) {
			return new VncList();
		} 
		else {
			return new VncList(
							value
								.subList(1, value.size())
								.stream()
								.map(v -> JavaInteropUtil.convertToVncVal(v))
								.collect(Collectors.toList()));
		}
	}

	@Override
	public VncList slice(final int start, final int end) {
		return new VncList(
					value
						.subList(start, end)
						.stream()
						.map(v -> JavaInteropUtil.convertToVncVal(v))
						.collect(Collectors.toList()));
	}
	
	@Override
	public VncList slice(final int start) {
		return slice(start, value.size());
	}

	@Override
	public VncJavaList setAt(final int idx, final VncVal val) {
		value.set(idx, JavaInteropUtil.convertToJavaObject(val));
		return this;
	}

	@Override
	public VncJavaList removeAt(final int idx) {
		value.remove(idx);
		return this;
	}
	
	@Override
	public VncList toVncList() {
		return new VncList(getList(), getMeta());
	}
	
	@Override
	public VncVector toVncVector() {
		return new VncVector(getList(), getMeta());
	}
	
	@Override
	public VncJavaList addAtStart(final VncVal val) {
		value.add(0, JavaInteropUtil.convertToJavaObject(val));
		return this;
	}

	@Override
	public VncJavaList addAllAtStart(final VncSequence list) {
		final List<VncVal> items = list.getList();
		for(int ii=0; ii<items.size(); ii++) {
			value.add(0, JavaInteropUtil.convertToJavaObject(items.get(ii)));
		}
		return this;
	}
	
	@Override
	public VncJavaList addAtEnd(final VncVal val) {
		value.add(JavaInteropUtil.convertToJavaObject(val));
		return this;
	}

	@Override
	public VncJavaList addAllAtEnd(final VncSequence list) {
		final List<VncVal> items = list.getList();
		for(int ii=0; ii<items.size(); ii++) {
			value.add(JavaInteropUtil.convertToJavaObject(items.get(ii)));
		}
		return this;
	}

	@Override
	public int compareTo(final VncVal o) {
		if (o == Constants.Nil) {
			return 1;
		}
		else if (Types.isVncList(o)) {
			for(int ii=0; ii<Math.min(size(), ((VncJavaList)o).size()); ii++) {
				int c = nth(ii).compareTo(((VncJavaList)o).nth(ii));
				if (c != 0) {
					return c;
				}
			}
		}
		
		return 0;
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
		VncJavaList other = (VncJavaList) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override 
	public String toString() {
		return "(" + Printer.join(getList(), " ", true) + ")";
	}
	
	public String toString(final boolean print_readably) {
		return "(" + Printer.join(getList(), " ", print_readably) + ")";
	}

	private void addAll(final Collection<Object> val) {
		if (val != null) {
			val.forEach(v -> {
				value.add(v instanceof VncVal
							? JavaInteropUtil.convertToJavaObject((VncVal)val)
							: v);
			});
		}
	}


    private static final long serialVersionUID = -1848883965231344442L;

	private final List<Object> value = new ArrayList<>();
}