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

import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncMap;


public class MetaUtil {

	public static VncVal addDefMeta(final VncVal val, final VncMap meta) {
		final VncVal argslist = meta.get(ARGLIST);
		if (argslist != Constants.Nil) {
			val.setMetaVal(ARGLIST, argslist);
		}
		final VncVal doc = meta.get(DOC);
		if (doc != Constants.Nil) {
			val.setMetaVal(DOC, doc);
		}
		final VncVal examples = meta.get(EXAMPLES);
		if (examples != Constants.Nil) {
			val.setMetaVal(EXAMPLES, examples);
		}
		return val;
	}
	
	public static VncVal withTokenPos(final VncVal val, final Token token) {
		val.setMetaVal(FILE, new VncString(token.getFile()));
		val.setMetaVal(LINE, new VncLong(token.getLine()));
		val.setMetaVal(COLUMN, new VncLong(token.getColumn()));
		return val;
	}

	public static void copyTokenPos(final VncVal from, final VncVal to) {
		to.setMetaVal(FILE, from.getMetaVal(FILE));
		to.setMetaVal(LINE, from.getMetaVal(LINE));
		to.setMetaVal(COLUMN, from.getMetaVal(COLUMN));
	}
	
	public static VncVal toMeta(final Token token) {
		return VncHashMap.ofAll(
					FILE, new VncString(token.getFile()),
					LINE, new VncLong(token.getLine()),
					COLUMN, new VncLong(token.getColumn()));
	}


	
	// Var definition
	public static final VncKeyword ARGLIST = new VncKeyword(":arglists"); 
	public static final VncKeyword DOC = new VncKeyword(":doc"); 
	public static final VncKeyword EXAMPLES = new VncKeyword(":examples"); 
	
	// File error location
	public static final VncKeyword FILE = new VncKeyword(":file"); 
	public static final VncKeyword LINE = new VncKeyword(":line"); 
	public static final VncKeyword COLUMN = new VncKeyword(":column"); 
}
