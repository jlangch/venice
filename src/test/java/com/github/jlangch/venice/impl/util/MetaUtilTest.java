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
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncString;


public class MetaUtilTest {

	@Test
	public void testGetFileNullMeta() {
		assertNull(MetaUtil.getFile(null));
	}
	
	@Test
	public void testGetLineNullMeta() {
		assertEquals(-1, MetaUtil.getLine(null));
	}
	
	@Test
	public void testGetColNullMeta() {
		assertEquals(-1, MetaUtil.getCol(null));
	}
	
	@Test
	public void testGetMetaValNullMeta() {
		assertEquals(Constants.Nil, MetaUtil.getMetaVal(null, new VncString("key")));
	}
	
	@Test
	public void testGetNamespaceNullMeta() {
		assertNull(MetaUtil.getNamespace(null));
	}

	
	
	@Test
	public void testGetFileNilMeta() {
		assertNull(MetaUtil.getFile(Constants.Nil));
	}
	
	@Test
	public void testGetLineNillMeta() {
		assertEquals(-1, MetaUtil.getLine(Constants.Nil));
	}
	
	@Test
	public void testGetColNillMeta() {
		assertEquals(-1, MetaUtil.getCol(Constants.Nil));
	}
	
	@Test
	public void testGetMetaValNillMeta() {
		assertEquals(Constants.Nil, MetaUtil.getMetaVal(Constants.Nil, new VncString("key")));
	}
	
	@Test
	public void testGetNamespaceNillMeta() {
		assertNull(MetaUtil.getNamespace(Constants.Nil));
	}

}