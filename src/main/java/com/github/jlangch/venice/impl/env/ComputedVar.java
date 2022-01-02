/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2022 Venice
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
package com.github.jlangch.venice.impl.env;

import java.util.function.Supplier;

import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;


public class ComputedVar extends Var {

	public ComputedVar(
			final VncSymbol name, 
			final Supplier<VncVal> supplier
	) {
		super(name, Constants.Nil);
		
		this.supplier = supplier;
	}

	public ComputedVar(
			final VncSymbol name, 
			final Supplier<VncVal> supplier,
			final boolean overwritable
	) {
		super(name, Constants.Nil, overwritable);
		
		this.supplier = supplier;
	}

	@Override 
	public VncVal getVal() {
		return supplier.get();
	}

	@Override 
	public String toString() {
		return super.toString();
	}
	
	@Override 
	public String toString(final boolean print_readably) {
		return super.toString(print_readably);
	}

	
	private static final long serialVersionUID = 1598432086227773369L;
	
	final Supplier<VncVal> supplier;
}
