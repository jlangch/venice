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
package com.github.jlangch.venice.impl.util.markdown.block;

import com.github.jlangch.venice.impl.util.markdown.chunk.Chunk;
import com.github.jlangch.venice.impl.util.markdown.chunk.ChunkParser;
import com.github.jlangch.venice.impl.util.markdown.chunk.Chunks;


public class TextBlock implements Block {

	public TextBlock() {
	}


	public void add(final Chunks chunks) {
		this.chunks.add(chunks);
	}
	
	public void add(final Chunk chunk) {
		this.chunks.add(chunk);
	}
	
	public Chunks getChunks() {
		return chunks;
	}

	@Override
	public boolean isEmpty() {
		return chunks.isEmpty();
	}
	
	@Override
	public void parseChunks() {
		chunks = new ChunkParser(chunks).parse();
	}
	
	
	private Chunks chunks = new Chunks();
}
