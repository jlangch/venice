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
package com.github.jlangch.venice.impl.util;

import com.github.jlangch.venice.ArityException;
import com.github.jlangch.venice.impl.types.collections.VncList;


public class ArityExceptions {

	public static void assertArity(
			final String fnName, 
			final VncList args, 
			final int... expectedArities
	) {
		final int arity = args.size();
		for (int ii=0; ii<expectedArities.length; ii++) {
			if (expectedArities[ii] == arity) return;
		}
		throwArityEx(arity, fnName);
	}
	
	public static void assertMinArity(
			final String fnName, 
			final VncList args, 
			final int minArity
	) {
		final int arity = args.size();
		if (arity < minArity) {
			throwArityEx(arity, fnName);
		}
	}

	public static void throwArityEx(
			final int arity,
			final String fnName
	) {
		throw new ArityException(String.format(
					"Wrong number of args (%d) passed to function %s", 
					arity, 
					fnName));
	}

	public static void throwArityEx(
			final int arity, 
			final int expectedArgs, 
			final String fnName
	) {
		throw new ArityException(String.format(
					"Wrong number of args (%d) passed to function %s. Expected %d args!", 
					arity, 
					fnName, 
					expectedArgs));
	}


	public static void throwVariadicArityEx(
			final int arity,
			final String fnName,
			final int fixedArgsCount
	) {
		throw new ArityException(String.format(
					"Wrong number of args (%d) passed to the variadic function %s that "
						+ "requires at least %d args.", 
					arity, 
					fnName,
					fixedArgsCount));
	}

}
