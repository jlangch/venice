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

import java.util.List;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.util.Tuple3;


public class VncMultiArityFunction extends VncFunction {

	public VncMultiArityFunction(final List<VncFunction> functions) {
		this(null, functions);
	}
	
	public VncMultiArityFunction(final String name, final List<VncFunction> functions) {
		super(name);
		
		if (functions == null || functions.isEmpty()) {
			throw new VncException("A multi-arity function must have at least one function");
		}
		
		this.functions = functions
							.stream()
							.map(fn -> new Tuple3<VncFunction, Integer, Boolean>(
												fn, 
												countFixedArgs(fn.getParams()),
												hasRemaingsArgs(fn.getParams())))
							.collect(Collectors.toList());
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
	@Override 
	public String toString() {
		return getName();
	}
	
	private VncFunction findFunction(final int arity) {
		int fixedArgs = -1;
		VncFunction fn = null;
		
		for(Tuple3<VncFunction, Integer, Boolean> f : functions) {
			if (f._3 == false) {
				// only fixed args
				if (f._2.equals(arity)) return f._1;  // exact fixed args match
			}
			else {
				// with remaing args
				if (arity >= f._2) {
					if (f._2 > fixedArgs) {
						fixedArgs = f._2;
						fn = f._1;
					}
				}
			}
		}
		
		return fn;
	}
	
	private static int countFixedArgs(final VncVector params) {
		int fixedArgs = 0;
		
		for(VncVal p : params.getList()) {
			if (isElisionSymbol(p)) break;
			fixedArgs++;
		}
		
		return fixedArgs;
	}

	private static boolean hasRemaingsArgs(final VncVector params) {
		for(VncVal p : params.getList()) {
			if (isElisionSymbol(p)) return true;
		}
		return false;
	}

	private static boolean isElisionSymbol(final VncVal val) {
		return Types.isVncSymbol(val) && ((VncSymbol)val).getName().equals("&");
	}

	
    private static final long serialVersionUID = -1848883965231344442L;
    
    private final List<Tuple3<VncFunction, Integer, Boolean>> functions;
}