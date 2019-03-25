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

import java.util.concurrent.ConcurrentHashMap;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncVector;


public class VncMultiFunction extends VncFunction {

	public VncMultiFunction(final String name, final VncFunction discriminatorFn) {
		super(name);
		
		if (discriminatorFn == null) {
			throw new VncException("A discriminator function must not be null");
		}
		
		this.discriminatorFn = discriminatorFn;
	}

	@Override
	public VncMultiFunction withMeta(final VncVal meta) {
		super.withMeta(meta);
		return this;
	}
	
	public void addFn(final VncVal dispatchVal, final VncFunction fn) {
		if (dispatchVal == null) {
			throw new VncException("A dispatch value must not be null");
		}
		if (fn == null) {
			throw new VncException("A multifunction method must not be null");
		}
				
		functions.put(dispatchVal, fn); // replace is allowed
	}

	public void removeFn(final VncVal dispatchVal) {
		if (dispatchVal == null) {
			throw new VncException("A dispatch value must not be null");
		}
		
		functions.remove(dispatchVal);
	}

	@Override
	public VncVector getParams() { 
		return discriminatorFn.getParams();
	}

	@Override
	public VncVal apply(final VncList params) {
		final VncVal dispatchVal = discriminatorFn.apply(params);
				
		return findMethod(dispatchVal).apply(params);
	}

	@Override public int typeRank() {
		return 102;
	}
	
	@Override 
	public String toString() {
		return "multi-fn " + getName();
	}
		
	private VncFunction findMethod(final VncVal dispatchVal) {
		final VncFunction fn = functions.get(dispatchVal);
		if (fn != null) {
			return fn;
		}

		final VncFunction defaultFn = functions.get(DEFAULT_METHOD);
		if (defaultFn != null) {
			return defaultFn;
		}
		
		throw new VncException(String.format(
					"No matching '%s' multifunction method defined for dispatch value %s", 
					getName(),
					Printer.pr_str(dispatchVal, true)));
	}
	
	
	private static final long serialVersionUID = -1848883965231344442L;
	
	private static final VncKeyword DEFAULT_METHOD = new VncKeyword(":default");

	private final VncFunction discriminatorFn;
	private final ConcurrentHashMap<VncVal,VncFunction> functions = new ConcurrentHashMap<>();
}