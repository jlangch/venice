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
import com.github.jlangch.venice.impl.util.markdown.chunk.RawChunk;
import com.github.jlangch.venice.impl.util.markdown.chunk.TextChunk;


public class TextBlockParser {

	public TextBlockParser(final LineReader reader) {
		this.reader = reader;
	}
	
	public TextBlock parse() {		
		if (reader.eof()) {
			return new TextBlock();
		}

		String line = reader.peek();
		reader.consume();

		if (StringUtil.isBlank(line)) {
			return new TextBlock();
		}

		final TextBlock block = new TextBlock();
		
		block.add(new RawChunk(line));
		
		while(!reader.eof()) {
			line = reader.peek();
			reader.consume();
			
			if (StringUtil.isBlank(line)) {
				break;
			}
			else {
				block.add(new TextChunk(line));
			}
		}
		
		return block;
	}
	
	
	public static boolean isTextBlockStart(final String line) {
		return true;
	}

	
	private final LineReader reader;
}
