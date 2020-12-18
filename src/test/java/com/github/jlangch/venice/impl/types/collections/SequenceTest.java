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
package com.github.jlangch.venice.impl.types.collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.functions.CoreFunctions;
import com.github.jlangch.venice.impl.types.VncLong;


public class SequenceTest {

	@Test
	public void test_addAllAtStart_VncTinyVector() {
		VncSequence add = VncList.of(new VncLong(1L), new VncLong(2L));

		// not reversed
		VncSequence seq = tiny_vector;
		seq = seq.addAllAtStart(add, false);
		
		assertEquals(add.size() + tiny_vector.size(), seq.size());
		assertEquals(1L, seq.nth(0).convertToJavaObject());
		assertEquals(2L, seq.nth(1).convertToJavaObject());
		assertEquals(4L, seq.nth(2).convertToJavaObject());

		// reversed
		seq = tiny_vector;
		seq = seq.addAllAtStart(add, true);
		
		assertEquals(add.size() + tiny_vector.size(), seq.size());
		assertEquals(2L, seq.nth(0).convertToJavaObject());
		assertEquals(1L, seq.nth(1).convertToJavaObject());
		assertEquals(4L, seq.nth(2).convertToJavaObject());
	}

	@Test
	public void test_addAllAtStart_VncTinyList() {
		VncSequence add = VncList.of(new VncLong(1L), new VncLong(2L));

		// not reversed
		VncSequence seq = tiny_list;
		seq = seq.addAllAtStart(add, false);
		
		assertEquals(add.size() + tiny_list.size(), seq.size());
		assertEquals(1L, seq.nth(0).convertToJavaObject());
		assertEquals(2L, seq.nth(1).convertToJavaObject());
		assertEquals(4L, seq.nth(2).convertToJavaObject());

		// reversed
		seq = tiny_list;
		seq = seq.addAllAtStart(add, true);
		
		assertEquals(add.size() + tiny_list.size(), seq.size());
		assertEquals(2L, seq.nth(0).convertToJavaObject());
		assertEquals(1L, seq.nth(1).convertToJavaObject());
		assertEquals(4L, seq.nth(2).convertToJavaObject());
	}

	@Test
	public void test_addAllAtStart_VncVector() {
		VncSequence add = VncList.of(new VncLong(1L), new VncLong(2L));

		// not reversed
		VncSequence seq = tall_vector;
		seq = seq.addAllAtStart(add, false);
		
		assertEquals(add.size() + tall_vector.size(), seq.size());
		assertEquals(1L, seq.nth(0).convertToJavaObject());
		assertEquals(2L, seq.nth(1).convertToJavaObject());
		assertEquals(4L, seq.nth(2).convertToJavaObject());

		// reversed
		seq = tall_vector;
		seq = seq.addAllAtStart(add, true);
		
		assertEquals(add.size() + tall_vector.size(), seq.size());
		assertEquals(2L, seq.nth(0).convertToJavaObject());
		assertEquals(1L, seq.nth(1).convertToJavaObject());
		assertEquals(4L, seq.nth(2).convertToJavaObject());
	}

	@Test
	public void test_addAllAtStart_VncList() {
		VncSequence add = VncList.of(new VncLong(1L), new VncLong(2L));

		// not reversed
		VncSequence seq = tall_list;
		seq = seq.addAllAtStart(add, false);
		
		assertEquals(add.size() + tall_list.size(), seq.size());
		assertEquals(1L, seq.nth(0).convertToJavaObject());
		assertEquals(2L, seq.nth(1).convertToJavaObject());
		assertEquals(4L, seq.nth(2).convertToJavaObject());

		// reversed
		seq = tall_list;
		seq = seq.addAllAtStart(add, true);
		
		assertEquals(add.size() + tall_list.size(), seq.size());
		assertEquals(2L, seq.nth(0).convertToJavaObject());
		assertEquals(1L, seq.nth(1).convertToJavaObject());
		assertEquals(4L, seq.nth(2).convertToJavaObject());
	}

	@Test
	public void test_addAllAtStart_VncMutableVector() {
		VncSequence add = VncList.of(new VncLong(1L), new VncLong(2L));

		// not reversed
		VncSequence seq = VncMutableVector.of(new VncLong(4L));
		seq = seq.addAllAtStart(add, false);
		
		assertEquals(3, seq.size());
		assertEquals(1L, seq.nth(0).convertToJavaObject());
		assertEquals(2L, seq.nth(1).convertToJavaObject());
		assertEquals(4L, seq.nth(2).convertToJavaObject());

		// reversed
		seq = VncMutableVector.of(new VncLong(4L));
		seq = seq.addAllAtStart(add, true);
		
		assertEquals(3, seq.size());
		assertEquals(2L, seq.nth(0).convertToJavaObject());
		assertEquals(1L, seq.nth(1).convertToJavaObject());
		assertEquals(4L, seq.nth(2).convertToJavaObject());
	}

	@Test
	public void test_addAllAtStart_VncMutableList() {
		VncSequence add = VncList.of(new VncLong(1L), new VncLong(2L));

		// not reversed
		VncSequence seq = VncMutableList.of(new VncLong(4L));
		seq = seq.addAllAtStart(add, false);
		
		assertEquals(3, seq.size());
		assertEquals(1L, seq.nth(0).convertToJavaObject());
		assertEquals(2L, seq.nth(1).convertToJavaObject());
		assertEquals(4L, seq.nth(2).convertToJavaObject());

		// reversed
		seq = VncMutableList.of(new VncLong(4L));
		seq = seq.addAllAtStart(add, true);
		
		assertEquals(3, seq.size());
		assertEquals(2L, seq.nth(0).convertToJavaObject());
		assertEquals(1L, seq.nth(1).convertToJavaObject());
		assertEquals(4L, seq.nth(2).convertToJavaObject());
	}

	@Test
	public void test_addAllAtStart_VncJavaList() {
		VncSequence add = VncList.of(new VncLong(1L), new VncLong(2L));

		// not reversed
		VncSequence seq = VncJavaList.of(4L);
		seq = seq.addAllAtStart(add, false);
		
		assertEquals(3, seq.size());
		assertEquals(1L, seq.nth(0).convertToJavaObject());
		assertEquals(2L, seq.nth(1).convertToJavaObject());
		assertEquals(4L, seq.nth(2).convertToJavaObject());

		// reversed
		seq = VncJavaList.of(4L);
		seq = seq.addAllAtStart(add, true);
		
		assertEquals(3, seq.size());
		assertEquals(2L, seq.nth(0).convertToJavaObject());
		assertEquals(1L, seq.nth(1).convertToJavaObject());
		assertEquals(4L, seq.nth(2).convertToJavaObject());
	}
	
	
	private static final VncSequence tiny_list   = VncList.of(new VncLong(4L));
	private static final VncSequence tiny_vector = VncVector.of(new VncLong(4L));
	private static final VncSequence tall_list   = (VncList)CoreFunctions.repeat.applyOf(new VncLong(10L), new VncLong(4L));
	private static final VncSequence tall_vector = ((VncList)CoreFunctions.repeat.applyOf(new VncLong(10L), new VncLong(4L))).toVncVector();
}
