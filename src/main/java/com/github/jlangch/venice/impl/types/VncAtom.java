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
package com.github.jlangch.venice.impl.types;

import static com.github.jlangch.venice.impl.types.Constants.False;
import static com.github.jlangch.venice.impl.types.Constants.True;

import java.util.concurrent.atomic.AtomicReference;

import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.types.collections.VncList;


public class VncAtom extends VncVal {

	public VncAtom(final VncVal value) { 
		state.set(value); 
	}
	
	public VncAtom copy() { 
		final VncAtom v = new VncAtom(state.get()); 
		v.setMeta(getMeta());
		return v;
	}
	
	public VncVal reset(final VncVal newVal) {
		state.set(newVal); 
		return newVal;
	}
	
	public VncVal deref() {
		return state.get();
	}
	
	public VncVal swap(final VncFunction fn, final VncList args) {
		for(;;) {
			final VncVal oldValue = deref();
			
			final VncList new_args = new VncList();
			new_args.getList().addAll(args.getList());
			new_args.getList().add(0, oldValue);
			final VncVal newValue = fn.apply(new_args);
			
			if (state.compareAndSet(oldValue, newValue)) {
				return state.get();
			}
		}
	}
	
	public VncVal compare_and_set(final VncVal expectValue, final VncVal newValue) {
		final VncVal oldValue = deref();
		if (oldValue.equals(expectValue)) {			
			return state.compareAndSet(oldValue, newValue) ? True : False;
		}
		else {
			return False;
		}
	}
	
	
	@Override 
	public String toString() {
		return "(atom " + Printer._pr_str(state.get(), true) + ")";
	}

	public String toString(final boolean print_readably) {
		return "(atom " + Printer._pr_str(state.get(), print_readably) + ")";
	}
	
	
	private final AtomicReference<VncVal> state = new AtomicReference<>();
}