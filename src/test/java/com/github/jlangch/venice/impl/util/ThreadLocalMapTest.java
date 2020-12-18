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
package com.github.jlangch.venice.impl.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.concurrent.ThreadLocalMap;


public class ThreadLocalMapTest {

	@Test
	public void test() {
		ThreadLocalMap.set(new VncKeyword("alpha"), new VncLong(100));
		ThreadLocalMap.set(new VncKeyword("beta"), new VncLong(200));
		ThreadLocalMap.set(new VncKeyword("gamma"), new VncLong(300));
		
		assertEquals(100L, ((VncLong)ThreadLocalMap.get(new VncKeyword("alpha"))).getValue().longValue());
		assertEquals(200L, ((VncLong)ThreadLocalMap.get(new VncKeyword("beta"))).getValue().longValue());
		assertEquals(300L, ((VncLong)ThreadLocalMap.get(new VncKeyword("gamma"))).getValue().longValue());
	}
	
}
