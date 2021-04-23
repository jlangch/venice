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


public class ListBlockParser {

	public ListBlockParser(final LineReader reader) {
		this.reader = reader;
	}
	
	public ListBlock parse() {
		if (reader.eof()) {
			return new ListBlock();
		}

		if (!ListBlockParser.isBlockStart(reader.peek())) {
			return new ListBlock();
		}

		final ListBlock block = new ListBlock();

		TextBlock item = new TextBlock();

		while (!reader.eof() && !StringUtil.isBlank(reader.peek())) {
			String line = reader.peek();
			reader.consume();

			if (ListBlockParser.isBlockStart(line)) {
				if (!item.isEmpty()) {
					block.addItem(item);
				}
				item = new TextBlock();

				// strip list bullet
				line = StringUtil.trimLeft(line);
				line = line.substring(1);
				line = StringUtil.trimLeft(line);
				
				item.add(new RawChunk(line));
			}
			else {
				line = StringUtil.trimLeft(line);
				item.add(new RawChunk(line));
			}
		}
		
		return block;
	}
	
	public static boolean isBlockStart(final String line) {
		return isItemStart(line);
	}

	public static boolean isItemStart(final String line) {
		return line.matches(" *[*] +[^ ].*");
	}

	
	private final LineReader reader;
}
