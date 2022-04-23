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
package com.github.jlangch.venice.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMapEntry;
import com.github.jlangch.venice.impl.types.collections.VncVector;


public class DestructuringTest {

	@Test
	public void test_sequential_single() {
		final VncVal symVal = new VncSymbol("n");
		final VncVal bindVal = new VncLong(10);
		
		final List<Var> bindings = Destructuring.destructure(symVal, bindVal);

		assertEquals(1, bindings.size());
		
		assertEquals("n", bindings.get(0).getName().getName());
		assertEquals(Long.valueOf(10L), ((VncLong)bindings.get(0).getVal()).getValue());
	}

	@Test
	public void test_sequential_multiple() {
		// [[x y] [10 20]]

		final VncVal symVal = VncList.of(new VncSymbol("x"), new VncSymbol("y"));
		final VncVal bindVal = VncList.of(new VncLong(10), new VncLong(20));
		
		final List<Var> bindings = Destructuring.destructure(symVal, bindVal);

		assertEquals(2, bindings.size());
		
		assertEquals("x", bindings.get(0).getName().getName());
		assertEquals(Long.valueOf(10L), ((VncLong)bindings.get(0).getVal()).getValue());
		
		assertEquals("y", bindings.get(1).getName().getName());
		assertEquals(Long.valueOf(20L), ((VncLong)bindings.get(1).getVal()).getValue());
	}

	@Test
	public void test_sequential_map_entry() {
		// [[x y] VncMapEntry(10 20)]

		final VncVal symVal = VncList.of(new VncSymbol("x"), new VncSymbol("y"));
		final VncVal bindVal = new VncMapEntry(new VncLong(10), new VncLong(20));
		
		final List<Var> bindings = Destructuring.destructure(symVal, bindVal);

		assertEquals(2, bindings.size());
		
		assertEquals("x", bindings.get(0).getName().getName());
		assertEquals(Long.valueOf(10L), ((VncLong)bindings.get(0).getVal()).getValue());
		
		assertEquals("y", bindings.get(1).getName().getName());
		assertEquals(Long.valueOf(20L), ((VncLong)bindings.get(1).getVal()).getValue());
	}

	@Test
	public void test_sequential_multiple_empty_1() {
		// [[x y] []]

		final VncVal symVal = VncList.of(new VncSymbol("x"), new VncSymbol("y"));
		final VncVal bindVal = VncList.empty();
		
		final List<Var> bindings = Destructuring.destructure(symVal, bindVal);

		assertEquals(2, bindings.size());
		
		assertEquals("x", bindings.get(0).getName().getName());
		assertEquals(Constants.Nil, bindings.get(0).getVal());
		
		assertEquals("y", bindings.get(1).getName().getName());
		assertEquals(Constants.Nil, bindings.get(1).getVal());
	}

	@Test
	public void test_sequential_multiple_empty_2() {
		// [[x & y] []]

		final VncVal symVal = VncList.of(new VncSymbol("x"), new VncSymbol("&"), new VncSymbol("y"));
		final VncVal bindVal =VncList.empty();
		
		final List<Var> bindings = Destructuring.destructure(symVal, bindVal);

		assertEquals(2, bindings.size());
		
		assertEquals("x", bindings.get(0).getName().getName());
		assertEquals(Constants.Nil, bindings.get(0).getVal());
		
		assertEquals("y", bindings.get(1).getName().getName());
		assertEquals(Constants.Nil, bindings.get(1).getVal());
	}

	@Test
	public void test_sequential_multiple_empty_3() {
		// [[x & y] [10]]

		final VncVal symVal = VncList.of(new VncSymbol("x"), new VncSymbol("&"), new VncSymbol("y"));
		final VncVal bindVal = VncList.of(new VncLong(10));
		
		final List<Var> bindings = Destructuring.destructure(symVal, bindVal);

		assertEquals(2, bindings.size());
		
		assertEquals("x", bindings.get(0).getName().getName());
		assertEquals(Long.valueOf(10L), ((VncLong)bindings.get(0).getVal()).getValue());
		
		assertEquals("y", bindings.get(1).getName().getName());
		assertEquals(Constants.Nil, bindings.get(1).getVal());
	}
	
	@Test
	public void test_sequential_multiple_empty_all() {
		// [[] []]

		final VncVal symVal = VncList.empty();
		final VncVal bindVal = VncList.empty();
		
		final List<Var> bindings = Destructuring.destructure(symVal, bindVal);

		assertEquals(0, bindings.size());
	}

	@Test
	public void test_sequential_multiple_elision() {
		// [[x _ y _ z] [10 20 30 40 50]]

		final VncVal symVal = VncList.of(
									new VncSymbol("x"), 
									new VncSymbol("_"), 
									new VncSymbol("y"), 
									new VncSymbol("_"), 
									new VncSymbol("z"));
		final VncVal bindVal = VncList.of(
									new VncLong(10), 
									new VncLong(20), 
									new VncLong(30), 
									new VncLong(40), 
									new VncLong(50));
		
		final List<Var> bindings = Destructuring.destructure(symVal, bindVal);

		assertEquals(3, bindings.size());
		
		assertEquals("x", bindings.get(0).getName().getName());
		assertEquals(Long.valueOf(10L), ((VncLong)bindings.get(0).getVal()).getValue());
		
		assertEquals("y", bindings.get(1).getName().getName());
		assertEquals(Long.valueOf(30L), ((VncLong)bindings.get(1).getVal()).getValue());
		
		assertEquals("z", bindings.get(2).getName().getName());
		assertEquals(Long.valueOf(50L), ((VncLong)bindings.get(2).getVal()).getValue());
	}

	@Test
	public void test_sequential_multiple_fill_up_1() {
		// [[x y & z] [10 20 30 40 50]]

		final VncVal symVal = VncList.of(
									new VncSymbol("x"), 
									new VncSymbol("y"), 
									new VncSymbol("&"), 
									new VncSymbol("z"));
		final VncVal bindVal = VncList.of(
									new VncLong(10), 
									new VncLong(20), 
									new VncLong(30), 
									new VncLong(40), 
									new VncLong(50));
		
		final List<Var> bindings = Destructuring.destructure(symVal, bindVal);

		assertEquals(3, bindings.size());
		
		assertEquals("x", bindings.get(0).getName().getName());
		assertEquals(Long.valueOf(10L), ((VncLong)bindings.get(0).getVal()).getValue());
		
		assertEquals("y", bindings.get(1).getName().getName());
		assertEquals(Long.valueOf(20L), ((VncLong)bindings.get(1).getVal()).getValue());
		
		assertEquals("z", bindings.get(2).getName().getName());
		assertEquals(3, ((VncList)bindings.get(2).getVal()).size());
		assertEquals(Long.valueOf(30L), ((VncLong)((VncList)bindings.get(2).getVal()).nth(0)).getValue());
		assertEquals(Long.valueOf(40L), ((VncLong)((VncList)bindings.get(2).getVal()).nth(1)).getValue());
		assertEquals(Long.valueOf(50L), ((VncLong)((VncList)bindings.get(2).getVal()).nth(2)).getValue());
	}

	@Test
	public void test_sequential_multiple_fill_up_1_as() {
		// [[x y & z :as all] [10 20 30 40 50]]

		final VncVal symVal = VncList.of(
									new VncSymbol("x"), 
									new VncSymbol("y"), 
									new VncSymbol("&"), 
									new VncSymbol("z"), 
									new VncKeyword(":as"), 
									new VncSymbol("all"));
		final VncVal bindVal = VncList.of(
									new VncLong(10), 
									new VncLong(20), 
									new VncLong(30), 
									new VncLong(40), 
									new VncLong(50));
		
		final List<Var> bindings = Destructuring.destructure(symVal, bindVal);

		assertEquals(4, bindings.size());
		
		assertEquals("x", bindings.get(0).getName().getName());
		assertEquals(Long.valueOf(10L), ((VncLong)bindings.get(0).getVal()).getValue());
		
		assertEquals("y", bindings.get(1).getName().getName());
		assertEquals(Long.valueOf(20L), ((VncLong)bindings.get(1).getVal()).getValue());
		
		assertEquals("z", bindings.get(2).getName().getName());
		assertEquals(3, ((VncList)bindings.get(2).getVal()).size());
		assertEquals(Long.valueOf(30L), ((VncLong)((VncList)bindings.get(2).getVal()).nth(0)).getValue());
		assertEquals(Long.valueOf(40L), ((VncLong)((VncList)bindings.get(2).getVal()).nth(1)).getValue());
		assertEquals(Long.valueOf(50L), ((VncLong)((VncList)bindings.get(2).getVal()).nth(2)).getValue());
		
		assertEquals("all", bindings.get(3).getName().getName());
		assertEquals(5, ((VncList)bindings.get(3).getVal()).size());
		assertEquals(Long.valueOf(10L), ((VncLong)((VncList)bindings.get(3).getVal()).nth(0)).getValue());
		assertEquals(Long.valueOf(20L), ((VncLong)((VncList)bindings.get(3).getVal()).nth(1)).getValue());
		assertEquals(Long.valueOf(30L), ((VncLong)((VncList)bindings.get(3).getVal()).nth(2)).getValue());
		assertEquals(Long.valueOf(40L), ((VncLong)((VncList)bindings.get(3).getVal()).nth(3)).getValue());
		assertEquals(Long.valueOf(50L), ((VncLong)((VncList)bindings.get(3).getVal()).nth(4)).getValue());
	}

	@Test
	public void test_sequential_multiple_fill_up_2() {
		// [[x y & z] [10 20]]

		final VncVal symVal = VncList.of(
									new VncSymbol("x"), 
									new VncSymbol("y"), 
									new VncSymbol("&"), 
									new VncSymbol("z"));
		final VncVal bindVal = VncList.of(
									new VncLong(10), 
									new VncLong(20));
		
		final List<Var> bindings = Destructuring.destructure(symVal, bindVal);

		assertEquals(3, bindings.size());
		
		assertEquals("x", bindings.get(0).getName().getName());
		assertEquals(Long.valueOf(10L), ((VncLong)bindings.get(0).getVal()).getValue());
		
		assertEquals("y", bindings.get(1).getName().getName());
		assertEquals(Long.valueOf(20L), ((VncLong)bindings.get(1).getVal()).getValue());
		
		assertEquals("z", bindings.get(2).getName().getName());
		assertEquals(Constants.Nil, bindings.get(2).getVal());
	}
	
	@Test
	public void test_sequential_nested() {
		// [[[v x & y] z] [[10 20 30 40] 50]]

		final VncVal symNestedVal = VncList.of(
											new VncSymbol("v"), 
											new VncSymbol("x"), 
											new VncSymbol("&"), 
											new VncSymbol("y"));
		final VncVal symVal = VncList.of(
									symNestedVal, 
									new VncSymbol("z"));
		
		final VncVal bindNestedVal = VncList.of(
											new VncLong(10), 
											new VncLong(20), 
											new VncLong(30), 
											new VncLong(40));
		final VncVal bindVal = VncList.of(bindNestedVal, new VncLong(50));
		
		final List<Var> bindings = Destructuring.destructure(symVal, bindVal);

		assertEquals(4, bindings.size());
		
		assertEquals("v", bindings.get(0).getName().getName());
		assertEquals(Long.valueOf(10L), ((VncLong)bindings.get(0).getVal()).getValue());
		
		assertEquals("x", bindings.get(1).getName().getName());
		assertEquals(Long.valueOf(20L), ((VncLong)bindings.get(1).getVal()).getValue());
		
		assertEquals("y", bindings.get(2).getName().getName());
		assertEquals(2, ((VncList)bindings.get(2).getVal()).size());
		assertEquals(Long.valueOf(30L), ((VncLong)((VncList)bindings.get(2).getVal()).nth(0)).getValue());
		assertEquals(Long.valueOf(40L), ((VncLong)((VncList)bindings.get(2).getVal()).nth(1)).getValue());
		
		assertEquals("z", bindings.get(3).getName().getName());
		assertEquals(Long.valueOf(50L), ((VncLong)bindings.get(3).getVal()).getValue());
	}

	@Test
	public void test_sequential_string() {
		// [[x y z] "abcdef"]

		final VncVal symVal = VncList.of(
									new VncSymbol("x"), 
									new VncSymbol("y"), 
									new VncSymbol("z"));
		final VncVal bindVal = new VncString("abcdef");
		
		final List<Var> bindings = Destructuring.destructure(symVal, bindVal);

		assertEquals(3, bindings.size());
		
		assertEquals("x", bindings.get(0).getName().getName());
		assertEquals("a", ((VncString)bindings.get(0).getVal()).getValue());
		
		assertEquals("y", bindings.get(1).getName().getName());
		assertEquals("b", ((VncString)bindings.get(1).getVal()).getValue());
		
		assertEquals("z", bindings.get(2).getName().getName());
		assertEquals("c", ((VncString)bindings.get(2).getVal()).getValue());
	}

	@Test
	public void test_sequential_string_multiple_elsision() {
		// [[x _ y _ z] "abcdef"]

		final VncVal symVal = VncList.of(
									new VncSymbol("x"), 
									new VncSymbol("_"), 
									new VncSymbol("y"), 
									new VncSymbol("_"), 
									new VncSymbol("z"));
		final VncVal bindVal = new VncString("abcdef");
		
		final List<Var> bindings = Destructuring.destructure(symVal, bindVal);

		assertEquals(3, bindings.size());
		
		assertEquals("x", bindings.get(0).getName().getName());
		assertEquals("a", ((VncString)bindings.get(0).getVal()).getValue());
		
		assertEquals("y", bindings.get(1).getName().getName());
		assertEquals("c", ((VncString)bindings.get(1).getVal()).getValue());
		
		assertEquals("z", bindings.get(2).getName().getName());
		assertEquals("e", ((VncString)bindings.get(2).getVal()).getValue());
	}

	@Test
	public void test_sequential_string_fill_up() {
		// [[x y & z] "abcdef"]

		final VncVal symVal = VncList.of(
									new VncSymbol("x"), 
									new VncSymbol("y"), 
									new VncSymbol("&"), 
									new VncSymbol("z"));
		
		final VncVal bindVal = new VncString("abcdef");
		
		final List<Var> bindings = Destructuring.destructure(symVal, bindVal);

		assertEquals(3, bindings.size());
		
		assertEquals("x", bindings.get(0).getName().getName());
		assertEquals("a", ((VncString)bindings.get(0).getVal()).getValue());
		
		assertEquals("y", bindings.get(1).getName().getName());
		assertEquals("b", ((VncString)bindings.get(1).getVal()).getValue());
		
		assertEquals("z", bindings.get(2).getName().getName());
		assertEquals(4, ((VncList)bindings.get(2).getVal()).size());
		assertEquals("c", ((VncString)((VncList)bindings.get(2).getVal()).nth(0)).getValue());
		assertEquals("d", ((VncString)((VncList)bindings.get(2).getVal()).nth(1)).getValue());
		assertEquals("e", ((VncString)((VncList)bindings.get(2).getVal()).nth(2)).getValue());
		assertEquals("f", ((VncString)((VncList)bindings.get(2).getVal()).nth(3)).getValue());
	}

	@Test
	public void test_sequential_string_as() {
		// [[x y z :as all] "abcdef"]

		final VncVal symVal = VncList.of(
									new VncSymbol("x"), 
									new VncSymbol("y"), 
									new VncSymbol("z"), 
									new VncKeyword(":as"), 
									new VncSymbol("all"));
		
		final VncVal bindVal = new VncString("abcdef");
		
		final List<Var> bindings = Destructuring.destructure(symVal, bindVal);

		assertEquals(4, bindings.size());
		
		assertEquals("x", bindings.get(0).getName().getName());
		assertEquals("a", ((VncString)bindings.get(0).getVal()).getValue());
		
		assertEquals("y", bindings.get(1).getName().getName());
		assertEquals("b", ((VncString)bindings.get(1).getVal()).getValue());
		
		assertEquals("z", bindings.get(2).getName().getName());
		assertEquals("c", ((VncString)bindings.get(2).getVal()).getValue());
		
		assertEquals("all", bindings.get(3).getName().getName());
		assertEquals("abcdef", ((VncString)bindings.get(3).getVal()).getValue());
	}

	@Test
	public void test_associative_simple() {
		// [{a :a, b :b, c :c} {:a 1 :b 2 :d 4}]  ->  a: 1, b: 2, c: nil

		final VncVal symVal = VncHashMap.of(
									new VncSymbol("a"), new VncKeyword(":a"),
									new VncSymbol("b"), new VncKeyword(":b"),
									new VncSymbol("c"), new VncKeyword(":c"));
		
		final VncVal bindVal = VncHashMap.of(
										new VncKeyword(":a"), new VncLong(1),
										new VncKeyword(":b"), new VncLong(2),
										new VncKeyword(":d"), new VncLong(4));
		
		final List<Var> bindings = Destructuring.destructure(symVal, bindVal);
		assertEquals(3, bindings.size());

		assertEquals(Long.valueOf(1L), ((VncLong)Var.findVar(new VncSymbol("a"), bindings).getVal()).getValue());		
		assertEquals(Long.valueOf(2L), ((VncLong)Var.findVar(new VncSymbol("b"), bindings).getVal()).getValue());
		assertEquals(Constants.Nil,    Var.findVar(new VncSymbol("c"), bindings).getVal());
	}

	@Test
	public void test_associative_keys() {
		// [{:keys [a b c]} {:a 1 :b 2 :d 4}]  ->  a: 1, b: 2, c: nil

		final VncVal symVal = VncHashMap.of(
									new VncKeyword(":keys"), 
									VncVector.of(new VncSymbol("a"), new VncSymbol("b"), new VncSymbol("c")));
		
		final VncVal bindVal = VncHashMap.of(
										new VncKeyword(":a"), new VncLong(1),
										new VncKeyword(":b"), new VncLong(2),
										new VncKeyword(":d"), new VncLong(4));
		
		final List<Var> bindings = Destructuring.destructure(symVal, bindVal);
		assertEquals(3, bindings.size());

		assertEquals(Long.valueOf(1L), ((VncLong)Var.findVar(new VncSymbol("a"), bindings).getVal()).getValue());		
		assertEquals(Long.valueOf(2L), ((VncLong)Var.findVar(new VncSymbol("b"), bindings).getVal()).getValue());
		assertEquals(Constants.Nil,    Var.findVar(new VncSymbol("c"), bindings).getVal());
	}

	@Test
	public void test_associative_keys_with_or() {
		// [{:keys [a b c] :or {c 3}} {:a 1 :b 2 :d 4}]  ->  a: 1, b: 2, c: 3

		final VncVal symVal = VncHashMap.of(
									new VncKeyword(":keys"), 
									VncVector.of(new VncSymbol("a"), new VncSymbol("b"), new VncSymbol("c")),
									new VncKeyword(":or"),
									VncHashMap.of(new VncSymbol("c"), new VncLong(3)));
		
		final VncVal bindVal = VncHashMap.of(
										new VncKeyword(":a"), new VncLong(1),
										new VncKeyword(":b"), new VncLong(2),
										new VncKeyword(":d"), new VncLong(4));
		
		final List<Var> bindings = Destructuring.destructure(symVal, bindVal);
		assertEquals(3, bindings.size());
		
		assertEquals(Long.valueOf(1L), ((VncLong)Var.findVar(new VncSymbol("a"), bindings).getVal()).getValue());		
		assertEquals(Long.valueOf(2L), ((VncLong)Var.findVar(new VncSymbol("b"), bindings).getVal()).getValue());
		assertEquals(Long.valueOf(3L), ((VncLong)Var.findVar(new VncSymbol("c"), bindings).getVal()).getValue());
	}

	@Test
	public void test_associative_syms() {
		// [{:syms [a b]} {'a 1 'b 2 'd 4}]  ->  'a 1, 'b 2, 'c nil

		final VncVal symVal = VncHashMap.of(
									new VncKeyword(":syms"), 
									VncVector.of(new VncSymbol("a"), new VncSymbol("b"), new VncSymbol("c")));
		
		final VncVal bindVal = VncHashMap.of(
										new VncSymbol("a"), new VncLong(1),
										new VncSymbol("b"), new VncLong(2),
										new VncSymbol("d"), new VncLong(4));
		
		final List<Var> bindings = Destructuring.destructure(symVal, bindVal);
		assertEquals(3, bindings.size());

		assertEquals(Long.valueOf(1L), ((VncLong)Var.findVar(new VncSymbol("a"), bindings).getVal()).getValue());		
		assertEquals(Long.valueOf(2L), ((VncLong)Var.findVar(new VncSymbol("b"), bindings).getVal()).getValue());
		assertEquals(Constants.Nil,    Var.findVar(new VncSymbol("c"), bindings).getVal());
	}

	@Test
	public void test_associative_syms_with_or() {
		// [{:syms [a b] :or {c 3}} {'a 1 'b 2 'd 4}]  ->  'a 1, 'b 2, 'c 3

		final VncVal symVal = VncHashMap.of(
									new VncKeyword(":syms"), 
									VncVector.of(new VncSymbol("a"), new VncSymbol("b"), new VncSymbol("c")),
									new VncKeyword(":or"),
									VncHashMap.of(new VncSymbol("c"), new VncLong(3)));
		
		final VncVal bindVal = VncHashMap.of(
										new VncSymbol("a"), new VncLong(1),
										new VncSymbol("b"), new VncLong(2),
										new VncSymbol("d"), new VncLong(4));
		
		final List<Var> bindings = Destructuring.destructure(symVal, bindVal);
		assertEquals(3, bindings.size());
		
		assertEquals(Long.valueOf(1L), ((VncLong)Var.findVar(new VncSymbol("a"), bindings).getVal()).getValue());		
		assertEquals(Long.valueOf(2L), ((VncLong)Var.findVar(new VncSymbol("b"), bindings).getVal()).getValue());
		assertEquals(Long.valueOf(3L), ((VncLong)Var.findVar(new VncSymbol("c"), bindings).getVal()).getValue());
	}
	
	@Test
	public void test_associative_strs() {
		// [{:strs [a b c]} {"a" 1 "b" 2 "d" 4}]  ->  "a" 1, "b" 2, "c" nil

		final VncVal symVal = VncHashMap.of(
									new VncKeyword(":strs"), 
									VncVector.of(new VncSymbol("a"), new VncSymbol("b"), new VncSymbol("c")));
		
		final VncVal bindVal = VncHashMap.of(
										new VncString("a"), new VncLong(1),
										new VncString("b"), new VncLong(2),
										new VncString("d"), new VncLong(4));
		
		final List<Var> bindings = Destructuring.destructure(symVal, bindVal);
		assertEquals(3, bindings.size());

		assertEquals(Long.valueOf(1L), ((VncLong)Var.findVar(new VncSymbol("a"), bindings).getVal()).getValue());		
		assertEquals(Long.valueOf(2L), ((VncLong)Var.findVar(new VncSymbol("b"), bindings).getVal()).getValue());
		assertEquals(Constants.Nil,    Var.findVar(new VncSymbol("c"), bindings).getVal());
	}
	
	@Test
	public void test_associative_strs_with_or() {
		// [{:strs [a b c] :or {c 3}} {"a" 1 "b" 2 "d" 4}]  ->  "a" 1, "b" 2, "c" 3

		final VncVal symVal = VncHashMap.of(
									new VncKeyword(":strs"), 
									VncVector.of(new VncSymbol("a"), new VncSymbol("b"), new VncSymbol("c")),
									new VncKeyword(":or"),
									VncHashMap.of(new VncSymbol("c"), new VncLong(3)));
		
		final VncVal bindVal = VncHashMap.of(
										new VncString("a"), new VncLong(1),
										new VncString("b"), new VncLong(2),
										new VncString("d"), new VncLong(4));
		
		final List<Var> bindings = Destructuring.destructure(symVal, bindVal);
		assertEquals(3, bindings.size());
		
		assertEquals(Long.valueOf(1L), ((VncLong)Var.findVar(new VncSymbol("a"), bindings).getVal()).getValue());		
		assertEquals(Long.valueOf(2L), ((VncLong)Var.findVar(new VncSymbol("b"), bindings).getVal()).getValue());
		assertEquals(Long.valueOf(3L), ((VncLong)Var.findVar(new VncSymbol("c"), bindings).getVal()).getValue());
	}

	@Test
	public void test_associative_mixed() {
		// [{:keys [a b c], e :e, f :f, g :g, h :h, :or {c 3, f 6}} {:a 1 :b 2 :d 4 :e 5 :g 7}]  
		// ->  a: 1, b: 2, c: 3, e: 4, f: 5, g: 6, h: nil

		final VncVal symVal = VncHashMap.of(
									new VncKeyword(":keys"), 
									VncVector.of(new VncSymbol("a"), new VncSymbol("b"), new VncSymbol("c")),
									new VncSymbol("e"), new VncKeyword(":e"),
									new VncSymbol("f"), new VncKeyword(":f"),
									new VncSymbol("g"), new VncKeyword(":g"),
									new VncSymbol("h"), new VncKeyword(":h"),
									new VncKeyword(":or"),
									VncHashMap.of(
											new VncSymbol("c"), new VncLong(3),
											new VncSymbol("f"), new VncLong(6)));
		
		final VncVal bindVal = VncHashMap.of(
										new VncKeyword(":a"), new VncLong(1),
										new VncKeyword(":b"), new VncLong(2),
										new VncKeyword(":d"), new VncLong(4),
										new VncKeyword(":e"), new VncLong(5),
										new VncKeyword(":g"), new VncLong(7));

		final List<Var> bindings = Destructuring.destructure(symVal, bindVal);
		assertEquals(7, bindings.size());
		
		assertEquals(Long.valueOf(1L), ((VncLong)Var.findVar(new VncSymbol("a"), bindings).getVal()).getValue());		
		assertEquals(Long.valueOf(2L), ((VncLong)Var.findVar(new VncSymbol("b"), bindings).getVal()).getValue());
		assertEquals(Long.valueOf(3L), ((VncLong)Var.findVar(new VncSymbol("c"), bindings).getVal()).getValue());
		assertEquals(Long.valueOf(5L), ((VncLong)Var.findVar(new VncSymbol("e"), bindings).getVal()).getValue());
		assertEquals(Long.valueOf(6L), ((VncLong)Var.findVar(new VncSymbol("f"), bindings).getVal()).getValue());
		assertEquals(Long.valueOf(7L), ((VncLong)Var.findVar(new VncSymbol("g"), bindings).getVal()).getValue());
		assertEquals(Constants.Nil,    Var.findVar(new VncSymbol("h"), bindings).getVal());
	}

	@Test
	public void test_associative_nested_associated() {
		// {a :a, {x :x, y :y} :c} {:a 1, :b 2, :c {:x 10, :y 11}}   -> a: 1, b: 2, x: 10, y: 11

		final VncVal symVal = VncHashMap.of(
									new VncSymbol("a"), 
									new VncKeyword(":a"),
									VncHashMap.of(
											new VncSymbol("x"), new VncKeyword(":x"),
											new VncSymbol("y"), new VncKeyword(":y")), 
									new VncKeyword(":c"));
		
		final VncVal bindVal = VncHashMap.of(
										new VncKeyword(":a"), new VncLong(1),
										new VncKeyword(":b"), new VncLong(2),
										new VncKeyword(":c"), 
										VncHashMap.of(
												new VncKeyword(":x"), new VncLong(10),
												new VncKeyword(":y"), new VncLong(11)));
		
		final List<Var> bindings = Destructuring.destructure(symVal, bindVal);
		assertEquals(3, bindings.size());

		assertEquals(Long.valueOf(1L),  ((VncLong)Var.findVar(new VncSymbol("a"), bindings).getVal()).getValue());		
		assertEquals(Long.valueOf(10L), ((VncLong)Var.findVar(new VncSymbol("x"), bindings).getVal()).getValue());		
		assertEquals(Long.valueOf(11L), ((VncLong)Var.findVar(new VncSymbol("y"), bindings).getVal()).getValue());
	}

	@Test
	public void test_associative_nested_sequential() {
		// {a :a, [x y] :c} {:a 1, :b 2, :c [10 11]}   -> a: 1, b: 2, x: 10, y: 11

		final VncVal symVal = VncHashMap.of(
									new VncSymbol("a"), 
									new VncKeyword(":a"),
									VncList.of(new VncSymbol("x"), new VncSymbol("y")), 
									new VncKeyword(":c"));
		
		final VncVal bindVal = VncHashMap.of(
										new VncKeyword(":a"), new VncLong(1),
										new VncKeyword(":b"), new VncLong(2),
										new VncKeyword(":c"), 
										VncList.of(new VncLong(10), new VncLong(11)));
		
		final List<Var> bindings = Destructuring.destructure(symVal, bindVal);
		assertEquals(3, bindings.size());

		assertEquals(Long.valueOf(1L),  ((VncLong)Var.findVar(new VncSymbol("a"), bindings).getVal()).getValue());		
		assertEquals(Long.valueOf(10L), ((VncLong)Var.findVar(new VncSymbol("x"), bindings).getVal()).getValue());		
		assertEquals(Long.valueOf(11L), ((VncLong)Var.findVar(new VncSymbol("y"), bindings).getVal()).getValue());
	}

	@Test
	public void test_let() {
		final Venice venice = new Venice();
		
		assertEquals("1", venice.eval("(str (let [a 1]  a))"));
		
		assertEquals("3", venice.eval("(str (let [a (+ 1 2)] a))"));
		
		assertEquals("6", venice.eval("(str (let [[a b c] '(1 2 3)] (+ a b c)))"));

		assertEquals("3", venice.eval("(str (let [[x y] [1 2 3 4 5 6]] (+ x y)))"));
		assertEquals("126", venice.eval("(str (let [[x y :as coords] [1 2 3 4 5 6]] (str x y (count coords))))"));

		assertEquals("[u v w]", venice.eval("(str (let [[a b c] \"uvwxyz\"] [a b c]))"));
		assertEquals("[u v (w x y z)]", venice.eval("(str (let [[a b & c] \"uvwxyz\"] [a b c]))"));
		
		assertEquals("[1 2 3]", venice.eval("(str (let [[a b c] '(1 2 3)] [a b c]))"));
		assertEquals("[1 2 3]", venice.eval("(str (let [[a b c] [1 2 3]] [a b c]))"));
		assertEquals("[1 2 3]", venice.eval("(str (let [[a [b c]] '(1 (2 3))] [a b c]))"));
		assertEquals("[1 2 3]", venice.eval("(str (let [[a [b c]] [1 [2 3]]] [a b c]))"));
		
		assertEquals("[3 1]", venice.eval("(str (let [[a _ c] '(1 2 3)] [c a]))"));
	
		assertEquals("3", venice.eval("(str (let [[ _ _ c ] '(1 2 3)] c)))"));

		assertEquals("[1 2 (3 4 5)]", venice.eval("(str (let [[a b & c] '(1 2 3 4 5)] [a b c]))"));
		assertEquals("[1 2 nil]", venice.eval("(str (let [[a b & c] '(1 2)] [a b c]))"));
		assertEquals("(1 2 3 4 5)", venice.eval("(str (let [[& a] '(1 2 3 4 5)] a))"));
	}
	
	@Test
	public void test_function_args_sequential_destructuring_1() {
		final Venice venice = new Venice();

		assertEquals("(1 2)", venice.eval(
								"(do \n" +
								"  (defn foo [[x y]] (list x y))\n" +
								"  (pr-str (foo [1 2])))"));

		assertEquals("(1 2)", venice.eval(
								"(do \n" +
								"  (defn foo [[x y]] (list x y))\n" +
								"  (pr-str (foo [1 2 3])))"));

		assertEquals("(1 nil)", venice.eval(
								"(do \n" +
								"  (defn foo [[x y]] (list x y))\n" +
								"  (pr-str (foo [1])))"));
	}
	
	@Test
	public void test_function_args_sequential_destructuring_2() {
		final Venice venice = new Venice();

		assertEquals("(1 2)", venice.eval("(str (map (fn [[k v]] v) [[:a 1] [:b 2]]))"));
		assertEquals("(:a :b)", venice.eval("(str (map (fn [[k v]] k) [[:a 1] [:b 2]]))"));
		assertEquals("([1 :a] [2 :b])", venice.eval("(str (map (fn [[k v]] [v k]) [[:a 1] [:b 2]]))"));
	}
	
	@Test
	public void test_function_args_associative_destructuring() {
		final Venice venice = new Venice();

		assertEquals("(1 2)", venice.eval(
								"(do \n" +
								"  (defn foo [{:keys [x y]}] (list x y))\n" +
								"  (pr-str (foo {:x 1 :y 2})))"));

		assertEquals("(1 2)", venice.eval(
								"(do \n" +
								"  (defn foo [{:keys [x y] :or {y 10}}] (list x y))\n" +
								"  (pr-str (foo {:x 1 :y 2})))"));

		assertEquals("(1 9)", venice.eval(
								"(do \n" +
								"  (defn foo [{:keys [x y] :or {y 9}}] (list x y))\n" +
								"  (pr-str (foo {:x 1})))"));
	}
}
