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
package org.venice.impl.types;

import org.venice.impl.types.collections.VncHashMap;


abstract public class VncVal implements Comparable<VncVal> {

	abstract public VncVal copy();
	
	public VncVal getMeta() { 
		return meta; 
	}
	
	public void setMeta(final VncVal m) { 
		meta = m == null ? Constants.Nil : m; 
	}

	public VncVal getMetaVal(final VncSymbol key) {
		if (meta == Constants.Nil) {
			return Constants.Nil;
		}
		else {
			return ((VncHashMap)meta).get(key);
		}
	}

	public void setMetaVal(final VncSymbol key, final VncVal val) {
		if (meta == Constants.Nil) {
			meta = new VncHashMap();
		}

		((VncHashMap)meta).getMap().put(key, val);	
	}
	
	public boolean isList() { 
		return false; 
	}

	@Override
	public int compareTo(final VncVal o) {
		return 0;
	}

	public String toString(final boolean print_readably) {
		return toString();
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((meta == null) ? 0 : meta.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VncVal other = (VncVal) obj;
		if (meta == null) {
			if (other.meta != null)
				return false;
		} else if (!meta.equals(other.meta))
			return false;
		return true;
	}


	private VncVal meta = Constants.Nil;
}