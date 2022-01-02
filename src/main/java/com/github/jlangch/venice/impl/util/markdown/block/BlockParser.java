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
package com.github.jlangch.venice.impl.util.markdown.block;

import com.github.jlangch.venice.impl.reader.LineReader;
import com.github.jlangch.venice.impl.util.StringUtil;


public class BlockParser {

	public BlockParser(final String text) {
		reader = new LineReader(text);
	}
	
	
	public Blocks parse() {
		while(!reader.eof()) {
			String line = reader.peek();
			
			if (StringUtil.isBlank(line)) {
				reader.consume(); // skip blank lines
			}
			else if (CodeBlockParser.isBlockStart(line)) {
				final CodeBlockParser p = new CodeBlockParser(reader);
				final CodeBlock block = p.parse();				
				if (!block.isEmpty()) {
					blocks.add(block);
				}
			}
			else if (ListBlockParser.isBlockStart(line)) {
				final ListBlockParser p = new ListBlockParser(reader);
				final ListBlock block = p.parse();				
				if (!block.isEmpty()) {
					blocks.add(block);
				}
			}
			else if (TableBlockParser.isBlockStart(line)) {
				final TableBlockParser p = new TableBlockParser(reader);
				final TableBlock block = p.parse();				
				if (!block.isEmpty()) {
					blocks.add(block);
				}
			}
			else {
				final TextBlockParser p = new TextBlockParser(reader);
				final TextBlock block = p.parse();				
				if (!block.isEmpty()) {
					blocks.add(block);
				}
			}
		}
		
		return blocks;
	}

	
	private final LineReader reader;
	private final Blocks blocks = new Blocks();
}
