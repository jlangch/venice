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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
		
		// parse into lines
		final List<String> lines = new ArrayList<>();	
		while (!reader.eof() && !StringUtil.isBlank(reader.peek())) {
			String line = reader.peek();
			reader.consume();
			lines.add(line);
		}

		// parse into items (an item may have 1..N lines)
		final List<List<String>> items = new ArrayList<>();
		List<String> itemLines = new ArrayList<>();	
		for(String line : lines) {
			if (isEmptyItem(line)) {
				continue; // skip empty items
			}
			if (isItemStart(line)) {
				if (!itemLines.isEmpty()) {
					items.add(itemLines);
					itemLines = new ArrayList<>();
				}
			}			
			itemLines.add(line);
		}
		if (!itemLines.isEmpty()) {
			items.add(itemLines);
		}
		

		final ListBlock block = new ListBlock();

		// list type
		block.setOrdered(isOrderedItemStart(items.get(0).get(0)));
		
		for(List<String> itemLines_ : items) {
			String first = itemLines_.get(0);
			
			if (isUnorderedItemStart(first)) {
				// strip list bullet
				first = StringUtil.trimLeft(first);
				first = first.substring(1);
				first = StringUtil.trimLeft(first);
				
				String text = first;
				
				if (itemLines_.size() > 1) {
					final List<String> rest = itemLines_
												.subList(1, itemLines_.size())
												.stream()
												.map(l -> StringUtil.trimLeft(l))
												.collect(Collectors.toList());
					text = text + " " + String.join(" ", rest);
				}

				TextBlock item = new TextBlock();
				addLine(item, StringUtil.trimRight(text));
				
				block.addItem(item);
			}		
			else if (isOrderedItemStart(first)) {
				// strip list item number
				final int pos = first.indexOf('.');
				first = first.substring(pos+1);
				first = StringUtil.trimLeft(first);
				
				String text = first;

				if (itemLines_.size() > 1) {
					final List<String> rest = itemLines_
												.subList(1, itemLines_.size())
												.stream()
												.map(l -> StringUtil.trimLeft(l))
												.collect(Collectors.toList());
					text = text + " " + String.join(" ", rest);
				}

				TextBlock item = new TextBlock();
				addLine(item, StringUtil.trimRight(text));
				
				if (!item.isEmpty()) {
					block.addItem(item);
				}
			}			
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

	public static boolean isEmptyItem(final String line) {
		return line.matches(" *[*] *") || line.matches(" *[0-9]+[.] *");
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
