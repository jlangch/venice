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

import java.util.List;
import java.util.Map;

import com.github.jlangch.venice.impl.types.VncVal;


public abstract class VncMap extends VncCollection {

	@Override
	public abstract VncMap copy();

	public abstract Map<VncVal,VncVal> getMap();
	
	public abstract VncVal get(VncVal key);

	public abstract VncVal containsKey(VncVal key);
	
	public abstract VncList keys();
	
	public abstract List<VncMapEntry> entries();

	public abstract VncMap putAll(VncMap map);

	public abstract VncMap assoc(VncVal... mvs);

	public abstract VncMap assoc(VncList mvs);

	public abstract VncMap dissoc(VncVal... keys);

	public abstract VncMap dissoc(VncList keys);


    private static final long serialVersionUID = -1848883965231344442L;
}