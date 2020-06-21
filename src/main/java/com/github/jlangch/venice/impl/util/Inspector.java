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

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.util.ArrayList;
import java.util.List;

import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncMultiArityFunction;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncOrderedMap;
import com.github.jlangch.venice.impl.types.util.Types;


public class Inspector {
	
	public static VncVal inspect(final VncVal val) {
		if (val == Nil) {
			return VncOrderedMap.of(
					new VncKeyword("type"), Types.getType(val),
					new VncKeyword("meta"), val.getMeta());
			
		}
		else if (Types.isVncMultiArityFunction(val)) {
			final List<VncVal> arityFunctions = new ArrayList<>();
			
			((VncMultiArityFunction)val).getFunctions().forEach(f -> {
				arityFunctions.add(
					VncOrderedMap.of(
						new VncKeyword("arity"), new VncLong(((VncFunction)f).getFixedArgsCount()),
						new VncKeyword("variadic?"), VncBoolean.of(((VncFunction)f).hasVariadicArgs()),
						new VncKeyword("fn"), inspect((VncFunction)f)));
			});
			
			return VncOrderedMap.of(
					new VncKeyword("name"), new VncString(((VncFunction)val).getQualifiedName()),
					new VncKeyword("type"), Types.getType(val),
					new VncKeyword("meta"), toNil(val.getMeta()),
					new VncKeyword("arity-fn"), VncList.ofList(arityFunctions));
		}
		else if (Types.isVncMultiFunction(val)) {
			return VncOrderedMap.of(
					new VncKeyword("name"), new VncString(((VncFunction)val).getQualifiedName()),
					new VncKeyword("type"), Types.getType(val),
					new VncKeyword("meta"), toNil(val.getMeta()),
					new VncKeyword("body"), toNil(((VncFunction)val).getBody()),
					new VncKeyword("params"), toNil(((VncFunction)val).getParams()));
		}
		else if (Types.isVncMacro(val)) {
			return VncOrderedMap.of(
					new VncKeyword("name"), new VncString(((VncFunction)val).getQualifiedName()),
					new VncKeyword("type"), Types.getType(val),
					new VncKeyword("meta"), toNil(val.getMeta()),
					new VncKeyword("body"), toNil(((VncFunction)val).getBody()),
					new VncKeyword("params"), toNil(((VncFunction)val).getParams()));
		}
		else if (Types.isVncFunction(val)) {
			return VncOrderedMap.of(
					new VncKeyword("name"), new VncString(((VncFunction)val).getQualifiedName()),
					new VncKeyword("type"), Types.getType(val),
					new VncKeyword("meta"), toNil(val.getMeta()),
					new VncKeyword("body"), toNil(((VncFunction)val).getBody()),
					new VncKeyword("params"), toNil(((VncFunction)val).getParams()));
		}
		else {
			return VncOrderedMap.of(
					new VncKeyword("type"), Types.getType(val),
					new VncKeyword("meta"), val.getMeta());
		}
	}

	
	private static VncVal toNil(final VncVal val) {
		return val == null ? Nil : val;
	}

}
