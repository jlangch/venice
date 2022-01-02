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
package com.github.jlangch.venice.impl.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;


public class ThreadLocalMapTest {

	@Test
	public void test() {
		ThreadContext.setValue(new VncKeyword("alpha"), new VncLong(100));
		ThreadContext.setValue(new VncKeyword("beta"), new VncLong(200));
		ThreadContext.setValue(new VncKeyword("gamma"), new VncLong(300));
		
		assertEquals(100L, ((VncLong)ThreadContext.getValue(new VncKeyword("alpha"))).getValue().longValue());
		assertEquals(200L, ((VncLong)ThreadContext.getValue(new VncKeyword("beta"))).getValue().longValue());
		assertEquals(300L, ((VncLong)ThreadContext.getValue(new VncKeyword("gamma"))).getValue().longValue());
	}
	
}
