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
import java.util.Set;

import com.github.jlangch.venice.impl.types.VncVal;


public abstract class VncSet extends VncCollection {

	abstract VncSet add(final VncVal val);
	
	abstract VncSet addAll(final VncSet val);
	
	abstract VncSet addAll(final VncSequence val);

	abstract VncSet remove(final VncVal val);

	abstract VncSet removeAll(final VncSet val);

	abstract VncSet removeAll(final VncSequence val);
	
	abstract boolean contains(final VncVal val);
	
	abstract Set<VncVal> getSet();
	
	abstract List<VncVal> getList();
		
	abstract VncVector toVncVector();


	private static final long serialVersionUID = -5846849359948270462L;
}