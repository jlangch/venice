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

import java.util.Map;

import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncVal;


public class VncMapEntry extends VncVal {

	public VncMapEntry(final VncVal key, final VncVal val) {
		super(Constants.Nil);
		this.key = key;
		this.val = val;
	}
	
	public VncMapEntry(final Map.Entry<VncVal, VncVal> entry) {
		super(Constants.Nil);
		this.key = entry.getKey();
		this.val = entry.getValue();
	}
	
	public VncVal getKey() {
		return key;
	}

	public VncVal getValue() {
		return val;
	}

	@Override
	public VncMapEntry withMeta(final VncVal meta) {
		return this;
	}
	
	@Override public int typeRank() {
		return 207;
	}

	
	private static final long serialVersionUID = 7943559441888855596L;
	
	private final VncVal key;
	private final VncVal val;
}
