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
package org.venice.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.venice.impl.types.VncLong;
import org.venice.impl.types.VncString;
import org.venice.impl.types.VncSymbol;
import org.venice.impl.types.VncVal;


public class ErrorMessageTest {

	@Test
	public void test_err_location() {
		final VncVal val = new VncLong(0);
		
		val.setMetaVal(new VncSymbol(":file"), new VncString("core.vnc"));
		val.setMetaVal(new VncSymbol(":line"), new VncLong(10));
		val.setMetaVal(new VncSymbol(":column"), new VncLong(42));

		assertEquals("core.vnc", ((VncString)val.getMetaVal(ErrorMessage.FILE)).getValue());
		assertEquals(Long.valueOf(10L), ((VncLong)val.getMetaVal(ErrorMessage.LINE)).getValue());
		assertEquals(Long.valueOf(42L), ((VncLong)val.getMetaVal(ErrorMessage.COLUMN)).getValue());
	}
	
}
