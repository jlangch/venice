/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2018 Venice
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
package com.github.jlangch.venice.impl;

import java.util.ArrayList;
import java.util.List;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.Types;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;


public class Destructuring {
	
	// [x 10]                              -> x: 10
	// [[x y] [10 20]]                     -> x: 10, y: 20
	// [[x _ y] [10 20 30]]                -> x: 10, y: 30
	// [[x y & z] [10 20 30 40 50]]        -> x: 10, y: 20, z: [30 40 50]
	// [[[v x & y] z] [[10 20 30 40] 50]]  -> v: 10, x: 20, y: [30 40], z: 50

	public static List<Binding> destructure(
			final VncVal symVal, 
			final VncVal bindVal
	) {
		final List<Binding> bindings = new ArrayList<>();
		
		if (Types.isVncSymbol(symVal)) {
			// [n 10]
			bindings.add(new Binding((VncSymbol)symVal, bindVal));
		}
		else if (Types.isVncList(symVal)) {
			if (Types.isVncList(bindVal)) {
				// sequential destructuring
				// [[x y] [10 20]]
				// [[x y & z] [10 20 30 40 50]]
				final List<VncVal> symbols = ((VncList)symVal).getList();
				final List<VncVal> values = ((VncList)bindVal).getList();
				for(int ii=0; ii<symbols.size(); ii++) {
					if (isIgnoreBindingSymbol(symbols.get(ii))) {
						continue;
					}
					else if (isElisionSymbol(symbols.get(ii))) {
						final VncSymbol sym = (VncSymbol)symbols.get(ii+1);
						final VncVal val = ii <= values.size() ? ((VncList)bindVal).slice(ii) : Constants.Nil;
						bindings.add(new Binding(sym, val));
						break;
					}
					else if (Types.isVncSymbol(symbols.get(ii))) {
						final VncSymbol sym = (VncSymbol)symbols.get(ii);
						final VncVal val = ii < values.size() ? values.get(ii) : Constants.Nil;
						bindings.add(new Binding(sym, val));
					}
					else if (Types.isVncList(symbols.get(ii))) {
						final VncVal syms = symbols.get(ii);
						final VncVal val = ii < values.size() ? values.get(ii) : Constants.Nil;						
						bindings.addAll(destructure(syms, val));
					}
				}
			}
			else if (Types.isVncString(bindVal)) {
				// sequential destructuring
				// [[x y] [10 20]]
				// [[x y & z] [10 20 30 40 50]]
				final List<VncVal> symbols = ((VncList)symVal).getList();
				final List<VncVal> values = ((VncString)bindVal).toVncList().getList();
				for(int ii=0; ii<symbols.size(); ii++) {
					if (isIgnoreBindingSymbol(symbols.get(ii))) {
						continue;
					}
					else if (isElisionSymbol(symbols.get(ii))) {
						final VncSymbol sym = (VncSymbol)symbols.get(ii+1);
						final VncVal val = (((VncString)bindVal).toVncList()).slice(ii);
						bindings.add(new Binding(sym, val));
						break;
					}
					else {
						final VncSymbol sym = (VncSymbol)symbols.get(ii);
						final VncVal val = ii < values.size() ? values.get(ii) : Constants.Nil;
						bindings.add(new Binding(sym, val));
					}
				}
			}
			else {
				throw new VncException(
						String.format(
								"Invalid destructuring bind value type %s. Expected list, vector, or string.",
								Types.getClassName(bindVal)));
			}
		}
		else {
			throw new VncException(
					String.format(
							"Invalid destructuring sym value type %s. Expected symbol.",
							Types.getClassName(symVal)));
		}
				
		return bindings;
	}
	
	
	private static boolean isElisionSymbol(final VncVal val) {
		return Types.isVncSymbol(val) && ((VncSymbol)val).getName().equals("&");
	}
	
	private static boolean isIgnoreBindingSymbol(final VncVal val) {
		return Types.isVncSymbol(val) && ((VncSymbol)val).getName().equals("_");
	}
}
