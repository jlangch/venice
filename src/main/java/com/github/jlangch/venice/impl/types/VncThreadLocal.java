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

import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.concurrent.ThreadLocalMap;
import com.github.jlangch.venice.impl.types.util.Coerce;


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
	public VncKeyword getSupertype() {
		return VncVal.TYPE;
	}

	@Override
	public List<VncKeyword> getAllSupertypes() {
		return Arrays.asList(VncVal.TYPE);
	}

	public VncVal get(final VncKeyword key) {
		return ThreadLocalMap.get(key);
	}

	public VncVal get(final VncKeyword key, final VncVal defaultValue) {
		return ThreadLocalMap.get(key, defaultValue);
	}

	public VncVal get(final String key) {
		return get(new VncKeyword(key));
	}

	public VncVal get(final String key, final VncVal defaultValue) {
		return get(new VncKeyword(key), defaultValue);
	}
	
	public void set(final VncKeyword key, final VncVal val) {
		ThreadLocalMap.set(key, val);
	}
	
	public void remove(final VncKeyword key) {
		ThreadLocalMap.remove(key);
	}

	public VncVal containsKey(final VncKeyword key) {
		return VncBoolean.of(key != null && ThreadLocalMap.containsKey(key));
	}

	public VncThreadLocal assoc(final VncVal... kvs) {
		for (int ii=0; ii<kvs.length; ii+=2) {
			set(Coerce.toVncKeyword(kvs[ii]), kvs[ii+1]);
		}
		return this;
	}

	public VncThreadLocal assoc(final VncList mvs) {
		VncList kv = mvs;
		while(!kv.isEmpty()) {
			set(Coerce.toVncKeyword(kv.first()), kv.second());
			kv = kv.drop(2);
		}
		return this;
	}

	public VncThreadLocal dissoc(final VncList lst) {
		for (VncVal v : lst) {
			remove(Coerce.toVncKeyword(v));
		}
		return this;
	}

	public VncThreadLocal dissoc(final VncKeyword... ks) {
		for (int ii=0; ii<ks.length; ii++) {
			remove(ks[ii]);
		}
		return this;
	}

	public VncThreadLocal clear() {
		ThreadLocalMap.clearValues();
		return this;
	}

	public static VncMap toMap() {
		return new VncHashMap(ThreadLocalMap.getValues());
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
	

	public static final VncKeyword TYPE = new VncKeyword(":core/thread-local");

    private static final long serialVersionUID = -1848883965231344442L;
}