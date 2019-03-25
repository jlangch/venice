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

import java.util.concurrent.atomic.AtomicReference;

import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.util.Watchable;


public class VncAtom extends VncVal {

	public VncAtom(final VncVal value, final VncVal meta) {
		super(meta);
		state.set(value); 
	}

	
	@Override
	public VncAtom withMeta(final VncVal meta) {
		return new VncAtom(state.get(), meta);
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
			final VncVal oldVal = deref();
			
			final VncList new_args = VncList.of(oldVal).addAllAtEnd(args);
			final VncVal newVal = fn.apply(new_args);
			
			if (state.compareAndSet(oldVal, newVal)) {
				watchable.notifyWatches(this, oldVal, newVal);
				return state.get();
			}
		}
	}
	
	public VncVal compare_and_set(final VncVal expectValue, final VncVal newVal) {
		final VncVal oldVal = deref();
		if (oldVal.equals(expectValue)) {			
			final boolean successful = state.compareAndSet(oldVal, newVal);			
			if (successful) {
				watchable.notifyWatches(this, oldVal, newVal);
			}			
			return successful ? True : False;
		}
		else {
			return False;
		}
	}
	
	public void addWatch(final VncKeyword name, final VncFunction fn) {
		watchable.addWatch(name, fn);
	}
	
	public void removeWatch(final VncKeyword name) {
		watchable.removeWatch(name);
	}
	
	@Override public int typeRank() {
		return 10;
	}
	
	@Override 
	public String toString() {
		return "(atom " + Printer.pr_str(state.get(), true) + ")";
	}

	public String toString(final boolean print_readably) {
		return "(atom " + Printer.pr_str(state.get(), print_readably) + ")";
	}
	
	
	
    private static final long serialVersionUID = -1848883965231344442L;
	
	private final AtomicReference<VncVal> state = new AtomicReference<>();
	private final Watchable watchable = new Watchable();
}