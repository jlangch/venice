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

import java.util.ArrayList;
import java.util.List;

import com.github.jlangch.venice.impl.reader.LineReader;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.markdown.chunk.LineBreakChunk;
import com.github.jlangch.venice.impl.util.markdown.chunk.RawChunk;


public class TextBlockParser {

	public TextBlockParser(final LineReader reader) {
		this.reader = reader;
	}
	
	public TextBlock parse() {		
		final TextBlock block = new TextBlock();

		if (reader.eof()) {
			return block;
		}

		List<String> lines = new ArrayList<>();
		
		while(!reader.eof()) {
			String line = reader.peek();
			reader.consume();
			
			if (StringUtil.isBlank(line)) {
				break;
			}
			else {
				lines.add(line);
			}
		}
		
		addLine(block, String.join(" ", lines));

		block.parseChunks();
		
		return block;
	}
	
	
	public static boolean isTextBlockStart(final String line) {
		return true;
	}

	
	private void addLine(final TextBlock block, final String line) {
		if (line.contains("¶")) {
			final String[] chunks = line.split("¶");
			for(int ii=0; ii<chunks.length; ii++) {
				if (ii>0) {
					block.add(new LineBreakChunk());
				}
				block.add(new RawChunk(chunks[ii]));
			}
			
			if (line.endsWith("¶")) {
				block.add(new LineBreakChunk());
			}
		}
		else {
			block.add(new RawChunk(line));
		}
	}
	
	
	private final LineReader reader;
}
