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

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;


public class DestructuringTest {

	@Test
	public void test_single() {
		final VncVal symVal = new VncSymbol("n");
		final VncVal bindVal = new VncLong(10);
		
		final List<Binding> bindings = Destructuring.destructure(symVal, bindVal);

		assertEquals(1, bindings.size());
		
		assertEquals("n", bindings.get(0).sym.getName());
		assertEquals(Long.valueOf(10L), ((VncLong)bindings.get(0).val).getValue());
	}

	@Test
	public void test_multiple() {
		// [[x y] [10 20]]

		final VncVal symVal = new VncList(new VncSymbol("x"), new VncSymbol("y"));
		final VncVal bindVal = new VncList(new VncLong(10), new VncLong(20));
		
		final List<Binding> bindings = Destructuring.destructure(symVal, bindVal);

		assertEquals(2, bindings.size());
		
		assertEquals("x", bindings.get(0).sym.getName());
		assertEquals(Long.valueOf(10L), ((VncLong)bindings.get(0).val).getValue());
		
		assertEquals("y", bindings.get(1).sym.getName());
		assertEquals(Long.valueOf(20L), ((VncLong)bindings.get(1).val).getValue());
	}

	@Test
	public void test_multiple_empty_1() {
		// [[x y] []]

		final VncVal symVal = new VncList(new VncSymbol("x"), new VncSymbol("y"));
		final VncVal bindVal = new VncList();
		
		final List<Binding> bindings = Destructuring.destructure(symVal, bindVal);

		assertEquals(2, bindings.size());
		
		assertEquals("x", bindings.get(0).sym.getName());
		assertEquals(Constants.Nil, bindings.get(0).val);
		
		assertEquals("y", bindings.get(1).sym.getName());
		assertEquals(Constants.Nil, bindings.get(1).val);
	}

	@Test
	public void test_multiple_empty_2() {
		// [[x & y] []]

		final VncVal symVal = new VncList(new VncSymbol("x"), new VncSymbol("&"), new VncSymbol("y"));
		final VncVal bindVal = new VncList();
		
		final List<Binding> bindings = Destructuring.destructure(symVal, bindVal);

		assertEquals(2, bindings.size());
		
		assertEquals("x", bindings.get(0).sym.getName());
		assertEquals(Constants.Nil, bindings.get(0).val);
		
		assertEquals("y", bindings.get(1).sym.getName());
		assertEquals(Constants.Nil, bindings.get(1).val);
	}
	
	@Test
	public void test_multiple_empty_all() {
		// [[] []]

		final VncVal symVal = new VncList();
		final VncVal bindVal = new VncList();
		
		final List<Binding> bindings = Destructuring.destructure(symVal, bindVal);

		assertEquals(0, bindings.size());
	}

	@Test
	public void test_multiple_fill_up_1() {
		// [[x y & z] [10 20 30 40 50]]

		final VncVal symVal = new VncList(new VncSymbol("x"), new VncSymbol("y"), new VncSymbol("&"), new VncSymbol("z"));
		final VncVal bindVal = new VncList(new VncLong(10), new VncLong(20), new VncLong(30), new VncLong(40), new VncLong(50));
		
		final List<Binding> bindings = Destructuring.destructure(symVal, bindVal);

		assertEquals(3, bindings.size());
		
		assertEquals("x", bindings.get(0).sym.getName());
		assertEquals(Long.valueOf(10L), ((VncLong)bindings.get(0).val).getValue());
		
		assertEquals("y", bindings.get(1).sym.getName());
		assertEquals(Long.valueOf(20L), ((VncLong)bindings.get(1).val).getValue());
		
		assertEquals("z", bindings.get(2).sym.getName());
		assertEquals(3, ((VncList)bindings.get(2).val).size());
		assertEquals(Long.valueOf(30L), ((VncLong)((VncList)bindings.get(2).val).nth(0)).getValue());
		assertEquals(Long.valueOf(40L), ((VncLong)((VncList)bindings.get(2).val).nth(1)).getValue());
		assertEquals(Long.valueOf(50L), ((VncLong)((VncList)bindings.get(2).val).nth(2)).getValue());
	}

	@Test
	public void test_multiple_fill_up_2() {
		// [[x y & z] [10 20]]

		final VncVal symVal = new VncList(new VncSymbol("x"), new VncSymbol("y"), new VncSymbol("&"), new VncSymbol("z"));
		final VncVal bindVal = new VncList(new VncLong(10), new VncLong(20));
		
		final List<Binding> bindings = Destructuring.destructure(symVal, bindVal);

		assertEquals(3, bindings.size());
		
		assertEquals("x", bindings.get(0).sym.getName());
		assertEquals(Long.valueOf(10L), ((VncLong)bindings.get(0).val).getValue());
		
		assertEquals("y", bindings.get(1).sym.getName());
		assertEquals(Long.valueOf(20L), ((VncLong)bindings.get(1).val).getValue());
		
		assertEquals("z", bindings.get(2).sym.getName());
		assertEquals(0, ((VncList)bindings.get(2).val).size());
	}
	
	@Test
	public void test_nested() {
		// [[[v x & y] z] [[10 20 30 40] 50]]

		final VncVal symNestedVal = new VncList(new VncSymbol("v"), new VncSymbol("x"), new VncSymbol("&"), new VncSymbol("y"));
		final VncVal symVal = new VncList(symNestedVal, new VncSymbol("z"));
		
		final VncVal bindNestedVal = new VncList(new VncLong(10), new VncLong(20), new VncLong(30), new VncLong(40));
		final VncVal bindVal = new VncList(bindNestedVal, new VncLong(50));
		
		final List<Binding> bindings = Destructuring.destructure(symVal, bindVal);

		assertEquals(4, bindings.size());
		
		assertEquals("v", bindings.get(0).sym.getName());
		assertEquals(Long.valueOf(10L), ((VncLong)bindings.get(0).val).getValue());
		
		assertEquals("x", bindings.get(1).sym.getName());
		assertEquals(Long.valueOf(20L), ((VncLong)bindings.get(1).val).getValue());
		
		assertEquals("y", bindings.get(2).sym.getName());
		assertEquals(2, ((VncList)bindings.get(2).val).size());
		assertEquals(Long.valueOf(30L), ((VncLong)((VncList)bindings.get(2).val).nth(0)).getValue());
		assertEquals(Long.valueOf(40L), ((VncLong)((VncList)bindings.get(2).val).nth(1)).getValue());
		
		assertEquals("z", bindings.get(3).sym.getName());
		assertEquals(Long.valueOf(50L), ((VncLong)bindings.get(3).val).getValue());
	}


	@Test
	public void test_let() {
		final Venice venice = new Venice();
		
		assertEquals("1", venice.eval("(str (let [a 1]  a))"));
		
		assertEquals("3", venice.eval("(str (let [a (+ 1 2)] a))"));
		
		assertEquals("6", venice.eval("(str (let [[a b c] '(1 2 3)] (+ a b c)))"));
		
		assertEquals("[u v w]", venice.eval("(str (let [[a b c] \"uvwxyz\"] [a b c]))"));
		assertEquals("[u v (w x y z)]", venice.eval("(str (let [[a b & c] \"uvwxyz\"] [a b c]))"));
		
		assertEquals("[1 2 3]", venice.eval("(str (let [[a b c] '(1 2 3)] [a b c]))"));
		assertEquals("[1 2 3]", venice.eval("(str (let [[a b c] '(1 2 3)] [a b c]))"));
		
		assertEquals("[3 1]", venice.eval("(str (let [[a _ c] '(1 2 3)] [c a]))"));
	
		assertEquals("3", venice.eval("(str (let [[ _ _ c ] '(1 2 3)] c)))"));

		assertEquals("[1 2 (3 4 5)]", venice.eval("(str (let [[a b & c] '(1 2 3 4 5)] [a b c]))"));
		assertEquals("[1 2 ()]", venice.eval("(str (let [[a b & c] '(1 2)] [a b c]))"));
		assertEquals("(1 2 3 4 5)", venice.eval("(str (let [[& a] '(1 2 3 4 5)] a))"));
	}
}
