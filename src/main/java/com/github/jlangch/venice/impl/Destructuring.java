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
package com.github.jlangch.venice.impl;

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.Types;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.util.ErrorMessage;


public class Destructuring {
	
	// x 10                                     -> x: 10
	
	// sequential destructuring on vector
	// [x y] [10 20]                            -> x: 10, y: 20
	// [x _ y] [10 20 30]                       -> x: 10, y: 30
	// [x y & z] [10 20 30 40 50]               -> x: 10, y: 20, z: [30 40 50]
	// [x y :as all] [10 20 30 40 50]           -> x: 10, y: 20, all: [10 20 30 40 50]
	// [x y & z :as all] [10 20 30 40 50]       -> x: 10, y: 20, z: [30 40 50] all: [10 20 30 40 50]
	// [[v x & y] z] [[10 20 30 40] 50]         -> v: 10, x: 20, y: [30 40], z: 50

	// associative destructuring on map
	// {a :a b :b} {:a 1 :b 2 :c 3}               -> a: 1, b: 2
	// {:keys [a b]} {:a 1 :b 2 :c 3}             -> a: 1, b: 2
	// {:syms [a b]} {'a 1 'b 2 'c 3}             -> a: 1, b: 2
	// {:strs [a b]} {"a" 1 "b" 2 "c" 3}          -> a: 1, b: 2
	// {:keys [a b] :as all} {:a 1 :b 2 :c 3}     -> a: 1, b: 2, all: {:a 1 :b 2 :c 3}
	// {:syms [a b] :as all} {'a 1 'b 2 'c 3}     -> a: 1, b: 2, all: {'a 1 'b 2 'c 3}
	// {:strs [a b] :as all} {"a" 1 "b" 2 "c" 3}  -> a: 1, b: 2, all: {"a" 1 "b" 2 "c" 3}
	// {:keys [a b] :or {:b 2}} {:a 1 :c 3}       -> a: 1, b: 2
	// {:syms [a b] :or {'b 2}} {'a 1 'c 3}       -> a: 1, b: 2
	// {:strs [a b] :or {"b" 2}} {"a" 1 "c" 3}    -> a: 1, b: 2
	
	// associative destructuring on vector
	// [x {:keys [a b]}] [10 {:a 1 :b 2 :c 3}]  -> a: 1, b: 2

	public static List<Binding> destructure(
			final VncVal symVal, 
			final VncVal bindVal
	) {
		final List<Binding> bindings = new ArrayList<>();
		
		if (Types.isVncSymbol(symVal)) {
			// scalar value binding [n 10]
			
			bindings.add(new Binding((VncSymbol)symVal, bindVal));
		}
		else if (Types.isVncList(symVal)) {
			// sequential destructuring	
			
			if (bindVal == Nil) {
				sequential_list_destructure((VncList)symVal, new VncList(), bindings);
			}
			if (Types.isVncList(bindVal)) {
				sequential_list_destructure((VncList)symVal, bindVal, bindings);
			}
			else if (Types.isVncString(bindVal)) {
				sequential_string_destructure((VncList)symVal, bindVal, bindings);
			}
			else {
				throw new VncException(
						String.format(
								"Invalid sequential destructuring bind value type %s. Expected list, vector, or string. %s",
								Types.getClassName(bindVal),
								ErrorMessage.buildErrLocation(bindVal)));
			}
		}
		else if (Types.isVncMap(symVal)) {			
			// associative destructuring
			
			if (bindVal == Nil) {
				associative_map_destructure((VncMap)symVal, new VncHashMap(), bindings);
			}
			else if (Types.isVncMap(bindVal)) {
				associative_map_destructure((VncMap)symVal, bindVal, bindings);
			}
			else if (Types.isVncVector(bindVal)) {
				throw new VncException(
						String.format(
								"Associative destructuring on vector is not yet implemented",
								Types.getClassName(bindVal),
								ErrorMessage.buildErrLocation(bindVal)));
			}
			else {
				throw new VncException(
						String.format(
								"Invalid associative destructuring bind value type %s. Expected map. %s",
								Types.getClassName(bindVal),
								ErrorMessage.buildErrLocation(bindVal)));
			}
		}
		else {
			throw new VncException(
					String.format(
							"Invalid destructuring sym value type %s. Expected symbol. %s",
							Types.getClassName(symVal),
							ErrorMessage.buildErrLocation(symVal)));
		}
				
		return bindings;
	}
	
	private static void sequential_list_destructure(
			final VncList symVal, 
			final VncVal bindVal,
			final List<Binding> bindings
	) {
		// [[x y] [10 20]]
		// [[x _ y _ z] [10 20 30 40 50]]
		// [[x y & z] [10 20 30 40 50]]
		// [[x y :as all] [10 20 30 40 50]]
		// [[x y & z :as all] [10 20 30 40 50]]
		
		final List<VncVal> symbols = symVal.getList();
		final List<VncVal> values = ((VncList)bindVal).getList();
		int symIdx = 0;
		int valIdx = 0;
		
		while(symIdx<symbols.size()) {
			if (isIgnoreBindingSymbol(symbols.get(symIdx))) {
				symIdx++; 
				valIdx++;
			}
			else if (isElisionSymbol(symbols.get(symIdx))) {
				final VncSymbol sym = (VncSymbol)symbols.get(symIdx+1);
				final VncVal val = valIdx < values.size() ? ((VncList)bindVal).slice(valIdx) : new VncList();
				bindings.add(new Binding(sym, val));
				symIdx += 2; 
				valIdx = values.size(); // all values read
			}
			else if (isAsKeyword(symbols.get(symIdx))) {
				final VncSymbol sym = (VncSymbol)symbols.get(symIdx+1);
				bindings.add(new Binding(sym, bindVal));
				symIdx += 2; 
			}
			else if (Types.isVncSymbol(symbols.get(symIdx))) {
				final VncSymbol sym = (VncSymbol)symbols.get(symIdx);
				final VncVal val = valIdx < values.size() ? values.get(valIdx) : Nil;
				bindings.add(new Binding(sym, val));
				symIdx++; 
				valIdx++;
			}
			else if (Types.isVncList(symbols.get(symIdx))) {
				final VncVal syms = symbols.get(symIdx);
				final VncVal val = valIdx < values.size() ? values.get(valIdx) : Nil;						
				bindings.addAll(destructure(syms, val));
				symIdx++; 
				valIdx++;
			}
			else if (Types.isVncMap(symbols.get(symIdx))) {
				final VncMap syms = (VncMap)symbols.get(symIdx);
				final VncVal val = valIdx < values.size() ? values.get(valIdx) : Nil;						
				associative_map_destructure(syms, val == Nil ? new VncHashMap() : val, bindings);
				symIdx++; 
				valIdx++;
			}
		}
	}

	private static void sequential_string_destructure(
			final VncList symVal, 
			final VncVal bindVal,
			final List<Binding> bindings
	) {
		// [[x y] "abcde"]
		// [[x _ y _ z] "abcde"]
		// [[x y & z] "abcde"]
		// [[x y :as all] "abcde"]
		// [[x y & z :as all] "abcde"]
		final List<VncVal> symbols = symVal.getList();
		final List<VncVal> values = bindVal == Nil ? new ArrayList<>() : ((VncString)bindVal).toVncList().getList();
		int symIdx = 0;
		int valIdx = 0;

		while(symIdx<symbols.size()) {
			if (isIgnoreBindingSymbol(symbols.get(symIdx))) {
				symIdx++; 
				valIdx++;
			}
			else if (isElisionSymbol(symbols.get(symIdx))) {
				final VncSymbol sym = (VncSymbol)symbols.get(symIdx+1);
				final VncVal val = valIdx < values.size() ? (((VncString)bindVal).toVncList()).slice(valIdx) : new VncList();
				bindings.add(new Binding(sym, val));
				symIdx += 2; 
				valIdx = values.size(); // all values read
			}
			else if (isAsKeyword(symbols.get(symIdx))) {
				final VncSymbol sym = (VncSymbol)symbols.get(symIdx+1);
				bindings.add(new Binding(sym, bindVal));
				symIdx += 2; 
			}
			else {
				final VncSymbol sym = (VncSymbol)symbols.get(symIdx);
				final VncVal val = valIdx < values.size() ? values.get(valIdx) : Nil;
				bindings.add(new Binding(sym, val));
				symIdx++; 
				valIdx++;
			}
		}
	}
	
	private static void associative_map_destructure(
			final VncMap symVal, 
			final VncVal bindVal,
			final List<Binding> bindings
	) {
		// {:keys [a b]} {:a 1 :b 2 :c 3}           -> a: 1, b: 2
		// {:syms [a b]} {'a 1 'b 2 'c 3}           -> a: 1, b: 2
		// {:strs [a b]} {"a" 1 "b" 2 "c" 3}        -> a: 1, b: 2

		// {a :a b :b} {:a 1 :b 2 :c 3}             -> a: 1, b: 2

		final List<Binding> local_bindings = new ArrayList<>();


		final List<VncVal> symbols = sortAssociativeNames(symVal.keys().getList());

		for(int ii = 0; ii<symbols.size(); ii++) {
			final VncVal symValName = symbols.get(ii);
			
			if (symValName.equals(KW_KEYS)) {
				final VncVal symbol = symVal.get(KW_KEYS);
				if (Types.isVncVector(symbol)) {
					((VncVector)symbol).forEach(
							sym -> {
								final VncSymbol s = (VncSymbol)sym;
								final VncVal v = bindVal == Nil ? Nil : ((VncMap)bindVal).get(new VncKeyword(s.getName()));
								local_bindings.add(new Binding(s, v));								
							});
				}
				else {
					throw new VncException(
							String.format(
									"Invalid associative destructuring with :keys symbol type %s. Expected vector. %s",
									Types.getClassName(symbol),
									ErrorMessage.buildErrLocation(symbol)));
				}					
			}
			else if (symValName.equals(KW_SYMS)) {
				final VncVal symbol = symVal.get(KW_SYMS);
				if (Types.isVncVector(symbol)) {
					((VncVector)symbol).forEach(
							sym -> {
								final VncSymbol s = (VncSymbol)sym;
								final VncVal v = bindVal == Nil ? Nil : ((VncMap)bindVal).get(s);
								local_bindings.add(new Binding(s, v));								
							});
				}
				else {
					throw new VncException(
							String.format(
									"Invalid associative destructuring with :syms symbol type %s. Expected vector. %s",
									Types.getClassName(symbol),
									ErrorMessage.buildErrLocation(symbol)));
				}					
			}
			else if (symValName.equals(KW_STRS)) {
				final VncVal symbol = symVal.get(KW_STRS);
				if (Types.isVncVector(symbol)) {
					((VncVector)symbol).forEach(
							sym -> {
								final VncSymbol s = (VncSymbol)sym;
								final VncVal v = bindVal == Nil ? Nil : ((VncMap)bindVal).get(new VncString(s.getName()));
								local_bindings.add(new Binding(s, v));								
							});
				}
				else {
					throw new VncException(
							String.format(
									"Invalid associative destructuring with :strs symbol type %s. Expected vector. %s",
									Types.getClassName(symbol),
									ErrorMessage.buildErrLocation(symbol)));
				}					
			}
			else if (symValName.equals(KW_OR)) {
				final VncVal symbol = symVal.get(KW_OR);
				if (symbol != Nil && Types.isVncMap(symbol)) {
					((VncMap)symbol).entries().forEach(e -> {
						final int bIdx = Binding.getBindingIndex((VncSymbol)e.getKey(), local_bindings);
						if (bIdx == -1) {
							local_bindings.add(new Binding((VncSymbol)e.getKey(), e.getValue()));
							
						}
						else {
							final Binding b = local_bindings.get(bIdx);
							if (b.val == Nil) {
								local_bindings.set(bIdx, new Binding((VncSymbol)e.getKey(), e.getValue()));
							}
						}
						
					});
				}
				
			}
			else if (symValName.equals(KW_AS)) {
				final VncVal symbol = symVal.get(KW_AS);
				if (symbol != Nil && Types.isVncSymbol(symbol)) {
					local_bindings.add(new Binding((VncSymbol)symbol, bindVal));
				}
			}
			else if (Types.isVncSymbol(symValName)) {
				final VncVal s = symVal.get(symValName);
				final VncVal v = bindVal == Nil ? Nil : ((VncMap)bindVal).get(s);
				local_bindings.add(new Binding((VncSymbol)symValName, v));								
			}
		}

	
		bindings.addAll(local_bindings);
	}

	private static boolean isAsKeyword(final VncVal val) {
		return Types.isVncKeyword(val) && ((VncKeyword)val).equals(KW_AS);
	}

	private static boolean isElisionSymbol(final VncVal val) {
		return Types.isVncSymbol(val) && ((VncSymbol)val).getName().equals("&");
	}
	
	private static boolean isIgnoreBindingSymbol(final VncVal val) {
		return Types.isVncSymbol(val) && ((VncSymbol)val).getName().equals("_");
	}
	

	private static List<VncVal> sortAssociativeNames(final List<VncVal> names) {
		final List<VncVal> sorted = new ArrayList<>();
		
		sorted.addAll(
				names
					.stream()
					.filter(n -> n.equals(KW_KEYS) || n.equals(KW_SYMS) || n.equals(KW_STRS))
					.collect(Collectors.toList()));
		
		sorted.addAll(
				names
					.stream()
					.filter(n -> n != Nil)
					.filter(n -> !(n.equals(KW_KEYS) || n.equals(KW_SYMS) || n.equals(KW_STRS) 
									|| n.equals(KW_AS) || n.equals(KW_OR)))
					.collect(Collectors.toList()));

		sorted.addAll(
				names
					.stream()
					.filter(n -> n.equals(KW_AS) || n.equals(KW_OR))
					.collect(Collectors.toList()));

		return sorted;
	}
	
	
	private static final VncKeyword KW_AS = new VncKeyword(":as");
	private static final VncKeyword KW_OR = new VncKeyword(":or");
	private static final VncKeyword KW_KEYS = new VncKeyword(":keys");
	private static final VncKeyword KW_SYMS = new VncKeyword(":syms");
	private static final VncKeyword KW_STRS = new VncKeyword(":strs");
}
