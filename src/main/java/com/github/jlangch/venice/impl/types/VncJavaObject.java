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
package com.github.jlangch.venice.impl.types;

import static com.github.jlangch.venice.impl.types.Constants.False;
import static com.github.jlangch.venice.impl.types.Constants.True;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.javainterop.Invoker;
import com.github.jlangch.venice.impl.javainterop.JavaInterop;
import com.github.jlangch.venice.impl.javainterop.JavaInteropUtil;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncMapEntry;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.util.reflect.ReflectionAccessor;
import com.github.jlangch.venice.javainterop.IInterceptor;
import com.github.jlangch.venice.javainterop.IInvoker;


public class VncJavaObject extends VncMap implements IVncJavaObject {

	public VncJavaObject(final Object obj) {
		this(obj, Constants.Nil);
	}
	
	public VncJavaObject(final Object obj, final VncVal meta) {
		super(meta);
		this.delegate = obj;
	}
	
	
	@Override
	public Object getDelegate() {
		return delegate;
	}
	
	@Override
	public VncMap empty() {
		throw new VncException("not supported");
	}
	@Override
	public VncHashMap withValues(final Map<VncVal,VncVal> replaceVals) {
		throw new VncException("not supported");
	}
	
	@Override
	public VncHashMap withValues(
			final Map<VncVal,VncVal> replaceVals, 
			final VncVal meta
	) {
		throw new VncException("not supported");
	}

	@Override
	public VncMap copy() {
		throw new VncException("not supported");
	}
	
	@Override
	public VncMap withMeta(final VncVal meta) {
		return this;
	}

	public VncVal getProperty(final VncString name) {
		return JavaInteropUtil.convertToVncVal(
				JavaInterop
					.getInterceptor()
					.onGetBeanProperty(
							new Invoker(), 
							delegate, 
							name.getValue()));
	}

	public void setProperty(final VncString name, final VncVal value) {
		JavaInterop
			.getInterceptor()
			.onSetBeanProperty(
					new Invoker(), 
					delegate, 
					name.getValue(), 
					JavaInteropUtil.convertToJavaObject(value));
	}
	
	@Override
	public Map<VncVal,VncVal> getMap() {
		return convertBean().getMap();
	}

	@Override
	public VncVal containsKey(final VncVal key) {
		return getMap().containsKey(key) ? True : False;
	}

	@Override
	public VncVal get(final VncVal key) {
		return getProperty((VncString)key);
	}

	@Override
	public VncList keys() {
		return new VncList(new ArrayList<>(getMap().keySet()));
	}

	@Override
	public List<VncMapEntry> entries() {
		return Collections.unmodifiableList(
					getMap()
						.entrySet()
						.stream().map(e -> new VncMapEntry(e.getKey(), e.getValue()))
						.collect(Collectors.toList()));
	}

	@Override
	public VncMap putAll(final VncMap map) {
		throw new VncException("not supported");
	}

	@Override
	public VncMap assoc(final VncVal... mvs) {
		throw new VncException("not supported");
	}

	@Override
	public VncMap assoc(final VncSequence mvs) {
		throw new VncException("not supported");
	}

	@Override
	public VncMap dissoc(final VncVal... keys) {
		throw new VncException("not supported");
	}

	@Override
	public VncMap dissoc(final VncSequence keys) {
		throw new VncException("not supported");
	}

	@Override
	public VncList toVncList() {
		return new VncList();
	}

	@Override
	public VncVector toVncVector() {
		return new VncVector();
	}

	public VncMap toVncMap() {
		return new VncHashMap(getMap());
	}

	@Override
	public int size() {
		return ReflectionAccessor.getBeanGetterProperties(delegate).size();
	}

	@Override
	public boolean isEmpty() {
		return false;
	}
	
	@Override public int typeRank() {
		return 9;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((delegate == null) ? 0 : delegate.hashCode());
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
		VncJavaObject other = (VncJavaObject) obj;
		if (delegate == null) {
			if (other.delegate != null)
				return false;
		} else if (!delegate.equals(other.delegate))
			return false;
		return true;
	}

	@Override
	public String toString(final boolean print_readably) {
		return delegate.toString();
	}
	

	private VncHashMap convertBean() {
		final VncHashMap.Builder builder = new VncHashMap.Builder();
		
		final IInterceptor interceptor = JavaInterop.getInterceptor();
		final IInvoker invoker = new Invoker();
		
		ReflectionAccessor
			.getBeanGetterProperties(delegate)
			.forEach(property -> {
				try {
					builder.put(
							new VncKeyword(property), 
							JavaInteropUtil.convertToVncVal(
									interceptor.onGetBeanProperty(
											invoker, delegate, property)));
				}
				catch(Exception ex) {
					throw new RuntimeException(ex);
				}
			});
		
		return builder.build();
	}
	
		
    private static final long serialVersionUID = -1848883965231344442L;

	private final Object delegate;
}
