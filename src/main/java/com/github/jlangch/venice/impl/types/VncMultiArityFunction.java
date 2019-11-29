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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.collections.VncList;


public class VncMultiArityFunction extends VncFunction {

	public VncMultiArityFunction(final String name, final List<VncFunction> functions) {
		super(name);
		
		if (functions == null || functions.isEmpty()) {
			throw new VncException("A multi-arity function must have at least one function");
		}
		
		for(VncFunction fn : functions) {
			if (fn.hasVariadicArgs()) {
				variadiArgFunctions.add(fn);
			}
			else {
				fixedArgFunctions.put(fn.getFixedArgsCount(), fn);
			}
		}
	}


	@Override
	public VncMultiArityFunction withMeta(final VncVal meta) {
		super.withMeta(meta);
		return this;
	}

	@Override
	public VncVal apply(final VncList params) {
		final VncFunction fn = findFunction(params.size());
		
		if (fn == null) {
			throw new VncException("No matching multi-arity function");
		}
		
		return fn.apply(params);
	}
	
	@Override public int typeRank() {
		return 101;
	}
	
	public VncList getFunctions() {
		final List<VncFunction> list = new ArrayList<>();
		
		list.addAll(fixedArgFunctions.values());
		list.addAll(variadiArgFunctions);
		
		return new VncList(list);
	}
	
	private VncFunction findFunction(final int arity) {
		VncFunction fn = fixedArgFunctions.get(arity);
		if (fn == null) {
			int fixedArgs = -1;
			for(VncFunction f : variadiArgFunctions) {
				if (arity >= f.getFixedArgsCount()) {
					if (f.getFixedArgsCount() > fixedArgs) {
						fixedArgs = f.getFixedArgsCount();
						fn = f;
					}
				}
			}
		}		
		return fn;
	}

	
    private static final long serialVersionUID = -1848883965231344442L;
    
    private final List<VncFunction> variadiArgFunctions = new ArrayList<>();
    private final Map<Integer, VncFunction> fixedArgFunctions = new HashMap<>();
}