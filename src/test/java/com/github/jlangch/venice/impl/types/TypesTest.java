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
package com.github.jlangch.venice.impl.types;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.types.util.Types;


public class TypesTest {

	@Test
	public void test_isVncJavaObject() {
		final VncJavaObject javaObj = new VncJavaObject(Long.valueOf(100));
		
		assertTrue(Types.isVncJavaObject(javaObj));
		assertTrue(Types.isVncJavaObject(javaObj, Long.class));
		assertTrue(Types.isVncJavaObject(javaObj, Number.class));
		assertTrue(Types.isVncJavaObject(javaObj, Object.class));
		assertFalse(Types.isVncJavaObject(javaObj, Integer.class));
	}
}
