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

		String line = reader.peek();
		reader.consume();

		if (StringUtil.isBlank(line)) {
			return block;
		}
		
		addLine(block, line);
		
		while(!reader.eof()) {
			line = reader.peek();
			reader.consume();
			
			if (StringUtil.isBlank(line)) {
				break;
			}
			else {
				addLine(block, line);
			}
		}
		
		block.parseChunks();
		
		return block;
	}
	
	
	public static boolean isTextBlockStart(final String line) {
		return true;
	}

	
	private void addLine(final TextBlock block, final String line) {
		if (lineEndsLineBreak(line)) {
			block.add(new RawChunk(line.substring(0, line.length()-1).trim()));
			block.add(new LineBreakChunk());
		}
		else {
			block.add(new RawChunk(line));
		}
	}
	
	private boolean lineEndsLineBreak(final String line) {
		return line.matches("^.*[¬¶]$");
	}
	
	
	private final LineReader reader;
}
