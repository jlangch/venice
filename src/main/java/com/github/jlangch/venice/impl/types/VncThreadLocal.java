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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.util.MetaUtil;


public class VncThreadLocal extends VncVal {

	public VncThreadLocal() { 
		super(Constants.Nil);
	}
	
	public VncThreadLocal(final Map<VncVal,VncVal> val) {
		super(Constants.Nil);
		val.entrySet().forEach(e -> set(Coerce.toVncKeyword(e.getKey()), e.getValue()));
	}
	
	public VncThreadLocal(final VncList lst) {
		super(Constants.Nil);
		assoc(lst);
	}

		
	@Override
	public VncThreadLocal withMeta(final VncVal meta) {
		return this;
	}
	
	@Override
	public VncKeyword getType() {
		return TYPE;
	}

	@Override
	public List<VncKeyword> getSupertypes() {
		return Arrays.asList(VncVal.TYPE);
	}

	public VncVal get(final VncKeyword key) {
		return ThreadContext.getValue(key);
	}

	public VncVal get(final VncKeyword key, final VncVal defaultValue) {
		return ThreadContext.getValue(key, defaultValue);
	}

	public VncVal get(final String key) {
		return get(new VncKeyword(key));
	}

	public VncVal get(final String key, final VncVal defaultValue) {
		return get(new VncKeyword(key), defaultValue);
	}
	
	public void set(final VncKeyword key, final VncVal val) {
		ThreadContext.setValue(key, val);
	}
	
	public void remove(final VncKeyword key) {
		ThreadContext.removeValue(key);
	}

	public VncVal containsKey(final VncKeyword key) {
		return VncBoolean.of(key != null && ThreadContext.containsKey(key));
	}

	public VncThreadLocal assoc(final VncVal... kvs) {
		for (int ii=0; ii<kvs.length-1; ii+=2) {
			final VncKeyword key = Coerce.toVncKeyword(kvs[ii]);
			if (isSystemKey(key)) {
				throw new VncException(String.format(
						"The %s value must be added/modifed on the thread local vars!",
						key));
			}
			else {
				set(key, kvs[ii+1]);
			}
		}
		return this;
	}

	public VncThreadLocal assoc(final VncList mvs) {
		VncList kv = mvs;
		while(!kv.isEmpty()) {
			final VncKeyword key = Coerce.toVncKeyword(kv.first());
			if (isSystemKey(key)) {
				throw new VncException(String.format(
						"The %s value must be added/modifed on the thread local vars!",
						key));
			}
			else {
				set(key, kv.second());
			}
			kv = kv.drop(2);
		}
		return this;
	}

	public VncThreadLocal dissoc(final VncList lst) {
		for (VncVal v : lst) {
			final VncKeyword key = Coerce.toVncKeyword(v);
			if (isSystemKey(key)) {
				throw new VncException(String.format(
						"The %s value must be removed from the thread local vars!",
						key));
			}
			else {
				remove(key);
			}
		}
		return this;
	}

	public VncThreadLocal dissoc(final VncKeyword... ks) {
		for (int ii=0; ii<ks.length; ii++) {
			if (isSystemKey(ks[ii])) {
				throw new VncException(String.format(
						"The %s value must be removed from the thread local vars!",
						ks[ii]));
			}
			else {
				remove(ks[ii]);
			}
		}
		return this;
	}

	public VncThreadLocal clear(final boolean preserveSystemValues) {
		ThreadContext.clearValues(preserveSystemValues);
		return this;
	}

	public static VncMap toMap() {
		// do not disclose *in*, *out*, *err*
		return new VncHashMap(ThreadContext.getValues())
						.dissoc(new VncKeyword("*in*"))
						.dissoc(new VncKeyword("*out*"))
						.dissoc(new VncKeyword("*err*"));
	}
	
	@Override 
	public TypeRank typeRank() {
		return TypeRank.THREADLOCAL;
	}

	@Override
	public Object convertToJavaObject() {
		return null;
	}
	
	@Override 
	public String toString() {
		return "ThreadLocal";
	}
	
	private static boolean isSystemKey(final VncKeyword key) {
		return "*in*".equals(key.getSimpleName())
			   || "*out*".equals(key.getSimpleName())
			   || "*err*".equals(key.getSimpleName());
	}

	public static final VncKeyword TYPE = new VncKeyword(":core/thread-local", MetaUtil.typeMeta());

    private static final long serialVersionUID = -1848883965231344442L;
}