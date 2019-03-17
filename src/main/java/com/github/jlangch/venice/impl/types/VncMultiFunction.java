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
import com.github.jlangch.venice.impl.util.ErrorMessage;


public class VncMultiFunction extends VncFunction {

	public VncMultiFunction(final String name, final VncFunction dicriminatorFn, final VncVal ast) {
		super(name, ast, null, null);
		
		if (dicriminatorFn == null) {
			throw new VncException("A dicriminator function must not be null");
		}
		
		this.dicriminatorFn = dicriminatorFn;
	}

	@Override
	public VncMultiFunction withMeta(final VncVal meta) {
		super.withMeta(meta);
		return this;
	}
	
	public void addFn(final VncVal dispatchVal, final VncFunction fn) {
		functions.put(dispatchVal, fn);
	}
	
	@Override
	public VncVector getParams() { 
		return dicriminatorFn.getParams();
	}

	@Override
	public VncVal apply(final VncList params) {
		final VncVal dispatchVal = dicriminatorFn.apply(params);
		
		final VncFunction fn = functions.get(dispatchVal);
		
		if (fn == null) {
			throw new VncException(String.format(
					"No matching '%s' multi function defined for dispatch value %s. %s", 
					getName(),
					Printer._pr_str(dispatchVal, true),
					ErrorMessage.buildErrLocation(getAst())));
		}
		
		return fn.apply(params);
	}

	
	@Override public int typeRank() {
		return 102;
	}
	@Override 
	public String toString() {
		return "multi-fn " + getName();
	}
		
	
	private static final long serialVersionUID = -1848883965231344442L;
    
	private final VncFunction dicriminatorFn;
    private final ConcurrentHashMap<VncVal,VncFunction> functions = new ConcurrentHashMap<>();
}