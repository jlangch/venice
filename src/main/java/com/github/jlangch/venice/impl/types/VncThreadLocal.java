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

import java.util.Map;

import com.github.jlangch.venice.impl.types.collections.VncList;
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
		return key != null && ThreadLocalMap.containsKey(key) ? True : False;
	}

	public VncThreadLocal assoc(final VncVal... kvs) {
		for (int ii=0; ii<kvs.length; ii+=2) {
			set(Coerce.toVncKeyword(kvs[ii]), kvs[ii+1]);
		}
		return this;
	}

	public VncThreadLocal assoc(final VncList lst) {
		for (int i=0; i<lst.getList().size(); i+=2) {
			set(Coerce.toVncKeyword(lst.nth(i)), lst.nth(i+1));
		}
		return this;
	}

	public VncThreadLocal dissoc(final VncList lst) {
		for (int i=0; i<lst.getList().size(); i++) {
			remove(Coerce.toVncKeyword(lst.nth(i)));
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
		ThreadLocalMap.clear();
		return this;
	}
	
	@Override public int typeRank() {
		return 11;
	}

	
	@Override 
	public String toString() {
		return "ThreadLocal";
	}
	
	
    private static final long serialVersionUID = -1848883965231344442L;
}