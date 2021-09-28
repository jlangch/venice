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
package com.github.jlangch.venice.impl.types;

import java.util.concurrent.ConcurrentHashMap;

import com.github.jlangch.venice.ArityException;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.MetaUtil;


public class VncProtocolFunction extends VncFunction {

	public VncProtocolFunction(
			final String name, 
			final VncSymbol protocol,
			final VncMultiArityFunction defaultFn,
			final VncVal meta
	) {
		super(name, null, false, null, meta);
		this.protocol = protocol;
		this.defaultFn = defaultFn;
	}

	public VncSymbol getProtocolName() {
		return protocol;
	}
	
	public void register(final VncKeyword type, final VncFunction fn) {
		typeFunctions.put(type, fn);
	}
	
	public void unregister(final VncKeyword type) {
		typeFunctions.remove(type);
	}
	
	@Override
	public VncProtocolFunction withMeta(final VncVal meta) {
		super.withMeta(meta);
		return this;
	}
	
	@Override
	public VncKeyword getType() {
		return new VncKeyword(
					TYPE, 
					MetaUtil.typeMeta(
						new VncKeyword(VncFunction.TYPE_FUNCTION),
						new VncKeyword(VncVal.TYPE)));
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
		// lookup protocol function based on the type of the first argument
		final VncKeyword type = Types.getType(args.first());
		final VncFunction fn = typeFunctions.get(type);
		
		if (fn == null) {
			return defaultFn.getFunctionForArgs(args);  // fallback
		}
		else if (fn instanceof VncMultiArityFunction) {
			try {
				return ((VncMultiArityFunction)fn).getFunctionForArgs(args);
			}
			catch(ArityException ex) {
				return defaultFn.getFunctionForArgs(args);  // fallback
			}
		}
		else {
			final int arity = args.size();

			if (fn.hasVariadicArgs()) {
				if (arity >= fn.getFixedArgsCount()) {
					return fn;
				}
				else {
					return defaultFn.getFunctionForArgs(args);  // fallback
				}
			}
			else {
				if (fn.getFixedArgsCount() == arity) {
					return fn;
				}
				else {
					return defaultFn.getFunctionForArgs(args);  // fallback
				}
			}
		}
	}

	@Override
	public VncFunction getFunctionForArity(final int arity) {
		throw new VncException("Not supported VncProtocolFunction::getFunctionForArity(..)!");
	}
	
	@Override public TypeRank typeRank() {
		return TypeRank.MULTI_PROTOCOL_FUNCTION;
	}

	
    public static final String TYPE = ":core/protocol-function";
	
    private static final long serialVersionUID = -1848883965231344442L;
    
	private final VncSymbol protocol;
	private final VncMultiArityFunction defaultFn;
	private final ConcurrentHashMap<VncKeyword,VncFunction> typeFunctions = new ConcurrentHashMap<>();
}