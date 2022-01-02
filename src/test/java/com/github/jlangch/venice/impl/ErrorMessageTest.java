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

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.util.MetaUtil;


public class ErrorMessageTest {

	@Test
	public void test_err_location() {
		VncVal meta = Constants.Nil;
		meta = MetaUtil.addMetaVal(meta, MetaUtil.FILE, new VncString("core.venice"));
		meta = MetaUtil.addMetaVal(meta, MetaUtil.LINE, new VncLong(10));
		meta = MetaUtil.addMetaVal(meta, MetaUtil.COLUMN, new VncLong(42));

		final VncVal val = new VncLong(0L, meta);
		
		assertEquals("core.venice", ((VncString)val.getMetaVal(MetaUtil.FILE)).getValue());
		assertEquals(10L, ((VncLong)val.getMetaVal(MetaUtil.LINE)).getValue());
		assertEquals(42L, ((VncLong)val.getMetaVal(MetaUtil.COLUMN)).getValue());
		
		assertEquals("core.venice", MetaUtil.getFile(val.getMeta()));
		assertEquals(10, MetaUtil.getLine(val.getMeta()));
		assertEquals(42, MetaUtil.getCol(val.getMeta()));
	}
	
}
