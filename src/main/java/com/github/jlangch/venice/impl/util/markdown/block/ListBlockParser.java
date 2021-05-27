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
		
		Boolean ordered = null;

		while (!reader.eof() && !StringUtil.isBlank(reader.peek())) {
			String line = reader.peek();
			reader.consume();

			if (isUnorderedItemStart(line)) {
				if (ordered == null) {
					ordered = false;
					block.setOrdered(false);
				}			
				if (!item.isEmpty()) {
					block.addItem(item);
				}
				item = new TextBlock();

				// strip list bullet
				line = StringUtil.trimLeft(line);
				line = line.substring(1);
				line = StringUtil.trimLeft(line);
				
				addLine(item, line);
			}
			else if (isOrderedItemStart(line)) {
				if (ordered == null) {
					ordered = true;
					block.setOrdered(true);
				}
				if (!item.isEmpty()) {
					block.addItem(item);
				}
				item = new TextBlock();

				// strip list item number
				final int pos = line.indexOf('.');
				line = line.substring(pos+1);
				line = StringUtil.trimLeft(line);
				
				addLine(item, line);
			}
			else {
				line = StringUtil.trimLeft(line);
				item.add(new RawChunk(line));
			}
		}
		
		if (!item.isEmpty()) {
			block.addItem(item);
		}
		
		block.parseChunks();
		return block;
	}
	
	public static boolean isBlockStart(final String line) {
		return isItemStart(line);
	}

	public static boolean isItemStart(final String line) {
		return isUnorderedItemStart(line) || isOrderedItemStart(line);
	}

	private static boolean isUnorderedItemStart(final String line) {
		return line.matches(" *[*] +[^ ].*");
	}

	private static boolean isOrderedItemStart(final String line) {
		return line.matches(" *[0-9]+[.] +[^ ].*");
	}

	private void addLine(final TextBlock block, final String line) {
		if (line.contains("¶")) {
			final String[] chunks = line.split("¶");
			for(int ii=0; ii<chunks.length; ii++) {
				if (ii>0) {
					block.add(new LineBreakChunk());
				}
				block.add(new RawChunk(chunks[ii].trim()));
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
