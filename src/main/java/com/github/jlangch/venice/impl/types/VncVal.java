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
package com.github.jlangch.venice.impl.types;

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.io.Serializable;

import com.github.jlangch.venice.impl.MetaUtil;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;


abstract public class VncVal implements Comparable<VncVal>, Serializable {

	public VncVal() {
		this(Nil);
	}

	public VncVal(final VncVal meta) {	
		this.meta = meta;
		this._private = MetaUtil.isPrivate(meta);
	}
	
	abstract public VncVal withMeta(final VncVal meta);
		
	abstract public int typeRank();

	public boolean isCollection() {
		return false;
	}

	abstract public Object convertToJavaObject();

	public VncVal getMeta() {
		// getMeta() can be redefined. Functions do that to manage meta data themselves.
		return meta == null ? Nil : meta; 
	}
	
	public VncVal getMetaVal(final VncString key) {
		final VncVal meta_ = getMeta();
		if (meta_ == Nil) {
			return Nil;
		}
		else if (meta_ instanceof VncHashMap) {
			return ((VncHashMap)meta_).get(key);
		}
		else {
			return Nil; // not a map
		}
	}

	public VncVal getMetaVal(final VncString key, final VncVal defaultValue) {
		final VncVal val = getMetaVal(key);
		return val == Nil ? defaultValue : val;
	}

	public boolean isPrivate() {
		return _private;
	}

	@Override
	public int compareTo(final VncVal o) {
		final int c = Integer.valueOf(typeRank()).compareTo(Integer.valueOf(o.typeRank()));
		return c != 0 ? c : -1;
	}

	public String toString(final boolean print_readably) {
		return toString();
	}
	

	@Override
	public int hashCode() {
		return 31;
	}

	@Override
	public boolean equals(Object obj) {
		return true;
	}
	
    
	
    private static final long serialVersionUID = -1848883965231344442L;

	private final VncVal meta;
	private final boolean _private;
}