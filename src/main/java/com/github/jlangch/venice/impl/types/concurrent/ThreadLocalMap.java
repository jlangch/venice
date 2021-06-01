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
package com.github.jlangch.venice.impl.types.concurrent;

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.io.PrintStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Namespace;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncStack;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.util.CallStack;

public class ThreadLocalMap {
	
	public ThreadLocalMap() {
		initNS();
	}


	public Namespace getCurrentNS() {
		return nsCurr;
	}

	public void setCurrentNS(final Namespace ns) {
		nsCurr = ns;
	}

	
	public static VncVal get(final VncKeyword key) {
		return get(key, Nil);
	}
	
	public static VncVal get(final VncKeyword key, final VncVal defaultValue) {
		if (key == null) {
			return Nil;
		}
		else {
			final VncVal v = get().values.get(key);
			if (v instanceof VncStack) {
				final VncVal thVal = ((VncStack)v).peek();
				return thVal == Nil ? defaultValue : thVal;
			}
			else {
				return v == null ? defaultValue : v;
			}
		}
	}
	
	public static void set(final VncKeyword key, final VncVal val) {
		if (key != null) {
			final ThreadLocalMap tmap = get();
			final VncVal v = tmap.values.get(key);
			if (v == null) {
				tmap.values.put(key, val == null ? Nil : val);
			}
			else if (v instanceof VncStack) {
				((VncStack) v).clear();
				((VncStack)v).push(val == null ? Nil : val);
			}
			else {
				tmap.values.put(key, val);
			}
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

	public static void push(final VncKeyword key, final VncVal val) {
		if (key != null) {
			final ThreadLocalMap tmap = get();
			if (tmap.values.containsKey(key)) {
				final VncVal v = tmap.values.get(key);
				if (v instanceof VncStack) {
					((VncStack)v).push(val == null ? Nil : val);
				}
				else {
					throw new VncException(String.format(
							"The var %s is not defined as dynamic on the thread-local map",
							key.getValue()));
				}
			}
			else {
				final VncStack stack = new VncStack();
				stack.push(val == null ? Nil : val);
				tmap.values.put(key, stack);
			}
		}
	}

	public static VncVal pop(final VncKeyword key) {
		if (key != null) {
			final ThreadLocalMap tmap = get();
			if (tmap.values.containsKey(key)) {
				final VncVal v = tmap.values.get(key);
				if (v instanceof VncStack) {
					return ((VncStack)v).pop();
				}
				else {
					throw new VncException(String.format(
							"The var %s is not defined as dynamic on the thread-local map",
							key.getValue()));
				}
			}
		}

		return Nil;
	}

	public static VncVal peek(final VncKeyword key) {
		if (key != null) {
			final ThreadLocalMap tmap = get();
			if (tmap.values.containsKey(key)) {
				final VncVal v = tmap.values.get(key);
				if (v instanceof VncStack) {
					return ((VncStack)v).peek();
				}
				else {
					throw new VncException(String.format(
							"The var %s is not defined as dynamic on the thread-local map",
							key.getValue()));
				}
			}
		}

		return Nil;
	}

	public static void clearCallStack() {
		get().callStack.clear();
	}

	public static CallStack getCallStack() {
		return  get().callStack;
	}

	public static Map<VncKeyword,VncVal> getValues() {
		final Map<VncKeyword,VncVal> copy = new HashMap<>();
		
		copyValues(get().values, copy);
		
		return copy;  // return a copy of the values
	}

	public static void setValues(final Map<VncKeyword,VncVal> newValues) {
		copyValues(newValues, get().values);
	}


	public static Namespace getCurrNS() {
		return get().nsCurr;
	}

	public static void setCurrNS(final Namespace ns) {
		get().nsCurr = ns;
	}

	public static void clearValues(final boolean preserveSystemValues) {
		try {
			if (preserveSystemValues) {
				final Map<VncKeyword, VncVal> values = get().values;
				
				// clear all values except the system values
				final Set<VncKeyword> keys = new HashSet<>(values.keySet());
				keys.remove(new VncKeyword("*in*"));
				keys.remove(new VncKeyword("*out*"));
				keys.remove(new VncKeyword("*err*"));
				
				keys.forEach(k -> values.remove(k));
			}
			else {
				get().values.clear();
			}
		}
		catch(Exception ex) {
			// do not care
		}
	}

	public static void clear() {
		try {
			get().values.clear();
			get().callStack.clear();
			get().initNS();
		}
		catch(Exception ex) {
			// do not care
		}
	}
	
	public static void remove() {
		try {
			clear();
			
			ThreadLocalMap.context.set(null);
			ThreadLocalMap.context.remove();
		}
		catch(Exception ex) {
			// do not care
		}
	}
	
	public static ThreadLocalMap get() {
		return ThreadLocalMap.context.get();
	}

	public static PrintStream getStdOut() {
		return Coerce.toVncJavaObject(peek(new VncKeyword(":*out*")), PrintStream.class);
	}

	public static PrintStream getStdErr() {
		return Coerce.toVncJavaObject(peek(new VncKeyword(":*err*")), PrintStream.class);
	}

	public static Reader getStdIn() {
		return Coerce.toVncJavaObject(peek(new VncKeyword(":*in*")), Reader.class);
	}

	
	private static void copyValues(final Map<VncKeyword,VncVal> from, final Map<VncKeyword,VncVal> to) {
		to.clear();
		
		for(Map.Entry<VncKeyword,VncVal> e : from.entrySet()) {
			final VncVal val = e.getValue();
			if (val instanceof VncStack) {
				final VncStack copyStack = new VncStack();
				if (!((VncStack)val).isEmpty()) {
					copyStack.push(((VncStack)val).peek());
				}
				to.put(e.getKey(), copyStack);
			}
			else {
				to.put(e.getKey(), val);
			}
		}
	}
	
	private void initNS() {
		nsCurr = new Namespace(new VncSymbol("user"));
	}

	
	private final Map<VncKeyword,VncVal> values = new HashMap<>();
	private final CallStack callStack = new CallStack();
	private Namespace nsCurr;
	
	
	// Note: Do NOT use InheritableThreadLocal with ExecutorServices. It's not guaranteed
	//       to work in all cases!
	private static ThreadLocal<ThreadLocalMap> context = 
			ThreadLocal.withInitial(() -> new ThreadLocalMap()); 
}
