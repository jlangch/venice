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
package com.github.jlangch.venice.impl.debug.util;

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.util.List;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncVector;


/**
 * Defines a special form as a <i>virtual</i> function to be used as
 * function for a breakpoint.
 */
public class SpecialFormVirtualFunction extends VncFunction {
	
	public SpecialFormVirtualFunction(
			final String name, 
			final VncVector params, 
			final VncVal meta
	) {
		super(name, params, false, null, meta);
	}
	
	public SpecialFormVirtualFunction(
			final String name, 
			final List<Var> args, 
			final VncVal meta
	) {
		this(name, toParams(args), meta);
	}
	
	
	@Override
	public VncVal apply(final VncList args) {
		return Nil;
	}

	@Override
	public boolean isNative() { 
		return false;
	}

	
	private static VncVector toParams(final List<Var> args) {
		return VncVector.ofColl(
					args.stream()
						.map(v -> v.getName())
						.collect(Collectors.toList()));
	}
	
	
	private static final long serialVersionUID = -1;
}
