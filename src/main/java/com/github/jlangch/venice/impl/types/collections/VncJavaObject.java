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

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.javainterop.Invoker;
import com.github.jlangch.venice.impl.javainterop.JavaInterop;
import com.github.jlangch.venice.impl.javainterop.JavaInteropUtil;
import com.github.jlangch.venice.impl.types.IVncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.util.reflect.ReflectionAccessor;
import com.github.jlangch.venice.javainterop.IInvoker;
import com.github.jlangch.venice.javainterop.JavaInterceptor;


public class VncJavaObject extends VncMap implements IVncJavaObject {

	public VncJavaObject(final Object obj) {
		this.delegate = obj;
	}
	
	@Override
	public Object getDelegate() {
		return delegate;
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
		JavaInteropUtil.convertToVncVal(
			JavaInterop
				.getInterceptor()
				.onSetBeanProperty(
						new Invoker(), 
						delegate, 
						name.getValue(), 
						JavaInteropUtil.convertToJavaObject(value)));
	}
	
	@Override
	public VncMap empty() {
		throw new VncException("not supported");
	}
	
	@Override
	public Map<VncVal,VncVal> getMap() {
		return convertBean().getMap();
	}

	@Override
	public VncVal get(final VncVal key) {
		return getProperty((VncString)key);
	}

	@Override
	public VncMap copy() {
		throw new VncException("not supported");
	}

	@Override
	public Set<Entry<VncVal, VncVal>> entries() {
		return new HashSet<>();
	}

	@Override
	public VncMap assoc(final VncVal... mvs) {
		throw new VncException("not supported");
	}

	@Override
	public VncMap assoc(final VncList lst) {
		throw new VncException("not supported");
	}

	@Override
	public VncMap dissoc(final VncList lst) {
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
		
		final JavaInterceptor interceptor = JavaInterop.getInterceptor();
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
	
		
	private final Object delegate;
}
