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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncVector;


public class VncMultiFunction extends VncFunction {

	public VncMultiFunction(final String name, final IVncFunction discriminatorFn) {
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
	
	@Override
	public VncKeyword getType() {
		return TYPE;
	}

	@Override
	public List<VncKeyword> getAllSupertypes() {
		return Arrays.asList(VncFunction.TYPE_FUNCTION, VncVal.TYPE);
	}
	
	public VncMultiFunction addFn(final VncVal dispatchVal, final VncFunction fn) {
		if (dispatchVal == null) {
			throw new VncException("A dispatch value must not be null");
		}
		if (fn == null) {
			throw new VncException("A multifunction method must not be null");
		}
				
		functions.put(dispatchVal, fn); // replace is allowed
		
		return this;
	}

	public VncMultiFunction removeFn(final VncVal dispatchVal) {
		if (dispatchVal == null) {
			throw new VncException("A dispatch value must not be null");
		}
		
		functions.remove(dispatchVal);
		
		return this;
	}

	@Override
	public VncVector getParams() { 
		return discriminatorFn instanceof VncFunction
					? ((VncFunction)discriminatorFn).getParams()
					: keywordDiscriminatorFnParams;
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
		final VncVal dispatchVal = discriminatorFn.apply(args);
		
		final VncFunction fn = functions.get(dispatchVal);
		if (fn != null) {
			return fn;
		}

		final VncFunction defaultFn = functions.get(DEFAULT_METHOD);
		if (defaultFn != null) {
			return defaultFn;
		}
		
		throw new VncException(String.format(
					"No matching '%s' multi-function method defined for dispatch value %s", 
					getQualifiedName(),
					Printer.pr_str(dispatchVal, true)));
	}
	
	@Override
	public VncFunction getFunctionForArity(final int arity) {
		throw new VncException(String.format(
				"No supported for multi-function methods (%s)", 
				getQualifiedName()));
	}

	@Override public TypeRank typeRank() {
		return TypeRank.MULTI_FUNCTION;
	}
	
	@Override 
	public String toString() {
		return "multi-fn " + getQualifiedName();
	}
	

    public static final VncKeyword TYPE = new VncKeyword(":core/multi-function");

	private static final long serialVersionUID = -1848883965231344442L;
	
	private static final VncKeyword DEFAULT_METHOD = new VncKeyword(":default");
	
	private static final VncVector keywordDiscriminatorFnParams = VncVector.of(new VncSymbol("x"));

	private final IVncFunction discriminatorFn;
	private final ConcurrentHashMap<VncVal,VncFunction> functions = new ConcurrentHashMap<>();
}