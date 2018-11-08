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
import com.github.jlangch.venice.util.CallStack;


public class ThreadLocalMap {
	
	public ThreadLocalMap() {
	}
	
	public static VncVal get(final VncKeyword key) {
		return get(key, Constants.Nil);
	}
	
	public static VncVal get(final VncKeyword key, final VncVal defaultValue) {
		if (key == null) {
			return Constants.Nil;
		}
		else {
			final VncVal v = get().values.get(key);
			return v == null ? defaultValue : v;
		}
	}
	
	public static void set(final VncKeyword key, final VncVal val) {
		if (key != null) {
			get().values.put(key, val == null ? Constants.Nil : val);
		}
	}
	
	public static void remove(final VncKeyword key) {
		if (key != null) {
			get().values.remove(key);
		}
	}
	
	public static boolean containsKey(final VncKeyword key) {
		return key == null ? false : get().values.containsKey(key);
	}

	public static void clearCallStack() {
		get().callStack.clear();
	}

	public static CallStack getCallStack() {
		return  get().callStack;
	}
	
	private static ThreadLocalMap get() {
		return ThreadLocalMap.context.get();
	}

	public static void clear() {
		try {
			get().values.clear();
			get().callStack.clear();
		}
		catch(Exception ex) {
			// do not care
		}
	}
	
	public static void remove() {
		try {
			get().values.clear();
			
			ThreadLocalMap.context.set(null);
			ThreadLocalMap.context.remove();
		}
		catch(Exception ex) {
			// do not care
		}
	}

	
	private final Map<VncKeyword,VncVal> values = new HashMap<>();
	private final CallStack callStack = new CallStack();
	
	private static ThreadLocal<ThreadLocalMap> context = 
			InheritableThreadLocal.withInitial(() -> new ThreadLocalMap()); 
}
