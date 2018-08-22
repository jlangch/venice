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
package com.github.jlangch.venice.impl.util;

import java.util.HashMap;
import java.util.Map;

import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;


public class ThreadLocalMap {
	
	public ThreadLocalMap() {
	}
	
	public static VncVal get(final VncKeyword name) {
		return name == null ? Constants.Nil : get().values.get(name);
	}
	
	public static void set(final VncKeyword name, final VncVal val) {
		if (name != null) {
			get().values.put(name, val == null ? Constants.Nil : val);
		}
	}

	private static ThreadLocalMap get() {
		return ThreadLocalMap.context.get();
	}

	public static void clear() {
		get().values.clear();
	}
	
    public static void remove() {
    	ThreadLocalMap.context.set(null);
    	ThreadLocalMap.context.remove();
    }

	
	private final Map<VncKeyword,VncVal> values = new HashMap<>();
	
	private static ThreadLocal<ThreadLocalMap> context = 
			InheritableThreadLocal.withInitial(() -> new ThreadLocalMap()); 
}
