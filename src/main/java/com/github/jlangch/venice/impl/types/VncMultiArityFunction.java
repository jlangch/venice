/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2020 Venice
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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.collections.VncList;


public class VncMultiArityFunction extends VncFunction {

	public VncMultiArityFunction(final String name, final List<VncFunction> functions, final boolean macro) {
		super(name, macro);
		
		if (functions == null || functions.isEmpty()) {
			throw new VncException("A multi-arity function must have at least one function");
		}
		

		int maxFixedArgs = -1;
		for(VncFunction fn : functions) {
			if (!fn.hasVariadicArgs()) {
				maxFixedArgs = Math.max(maxFixedArgs, fn.getFixedArgsCount());
			}
		}

		fixedArgFunctions = new VncFunction[maxFixedArgs+1]; 

		for(VncFunction fn : functions) {
			if (fn.hasVariadicArgs()) {
				variadicArgFunctions.add(fn);
			}
			else {
				fixedArgFunctions[fn.getFixedArgsCount()] = fn;
			}
		}
	}


	@Override
	public VncMultiArityFunction withMeta(final VncVal meta) {
		super.withMeta(meta);
		return this;
	}
	
	@Override
	public VncKeyword getType() {
		return isMacro() ? VncFunction.TYPE_MACRO : VncFunction.TYPE_FUNCTION;
	}
	
	@Override
	public VncKeyword getSupertype() {
		return VncVal.TYPE;
	}

	@Override
	public List<VncKeyword> getAllSupertypes() {
		return Arrays.asList(VncVal.TYPE);
	}

	@Override
	public VncVal apply(final VncList args) {
		return getFunctionForArgs(args).apply(args);
	}

	@Override
	public boolean isNative() { 
		return false;
	}

	@Override
	public VncFunction getFunctionForArgs(final VncList args) {
		final int arity = args.size();

		VncFunction fn = arityFunctionCache.get(arity);
		if (fn != null) {
			return fn;
		}
		
		if (arity < fixedArgFunctions.length) {
			fn = fixedArgFunctions[arity];
		}
		
		if (fn == null) {
			// with multi-arity functions choose the matching function with
			// highest number of fixed args
			int fixedArgs = -1;
			for(VncFunction candidateFn : variadicArgFunctions) {
				final int candidateFnFixedArgs = candidateFn.getFixedArgsCount();
				if (arity >= candidateFnFixedArgs) {
					if (candidateFnFixedArgs > fixedArgs) {
						fixedArgs = candidateFnFixedArgs;
						fn = candidateFn;
					}
				}
			}
		}	
		
		if (fn == null) {
			throw new VncException(String.format(
					"No matching '%s' multi-arity function for arity %d", 
					getQualifiedName(),
					arity));
		}

		arityFunctionCache.put(arity, fn);
		
		return fn;
	}
	
	@Override public TypeRank typeRank() {
		return TypeRank.MULTI_ARITY_FUNCTION;
	}
	
	public VncList getFunctions() {
		final List<VncFunction> list = new ArrayList<>();
		
		for(VncFunction f : fixedArgFunctions) {
			list.add(f);
		}
		list.addAll(variadicArgFunctions);
		
		return VncList.ofList(list);
	}

	
    private static final long serialVersionUID = -1848883965231344442L;
    
    private final List<VncFunction> variadicArgFunctions = new ArrayList<>();
    private final VncFunction[] fixedArgFunctions;
    private final ConcurrentHashMap<Integer,VncFunction> arityFunctionCache = new ConcurrentHashMap<>();
}