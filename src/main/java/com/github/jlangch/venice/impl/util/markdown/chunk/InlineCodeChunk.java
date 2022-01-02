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
package com.github.jlangch.venice.impl.util.markdown.chunk;

import com.github.jlangch.venice.impl.util.StringUtil;

public class InlineCodeChunk implements Chunk {

	public InlineCodeChunk() {
		this("");
	}

	public InlineCodeChunk(final String text) {
		this.text = StringUtil.trimToEmpty(text);
	}


	@Override
	public boolean isEmpty() {
		return text.isEmpty();
	}
	
	
	public String getText() {
		return text;
	}

	
	private final String text;
}
