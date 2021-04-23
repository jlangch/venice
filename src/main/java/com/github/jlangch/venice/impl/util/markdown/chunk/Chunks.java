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
package com.github.jlangch.venice.impl.util.markdown.chunk;

import java.util.ArrayList;
import java.util.List;


public class Chunks {

	public Chunks() {
	}
	
	public Chunks add(final Chunk chunk) {
		if (chunk != null && !chunk.isEmpty()) {
			chunks.add(chunk);
		}
		return this;
	}

	public Chunks add(final Chunks chunks) {
		if (chunks != null ) {
			for(Chunk c : chunks.getChunks()) {
				if (!c.isEmpty()) this.chunks.add(c);
			}
		}
		
		return this;
	}
	
	public boolean isEmpty() {
		return chunks.isEmpty();
	}
	
	public int size() {
		return chunks.size();
	}
	
	public List<Chunk> getChunks() {
		return chunks;
	}


	private final List<Chunk> chunks = new ArrayList<>();
}
