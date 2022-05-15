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


public class TitleBlockParser {

	public TitleBlockParser(final LineReader reader) {
		this.reader = reader;
	}
	
	public TitleBlock parse() {
		if (reader.eof()) {
			return new TitleBlock();
		}

		String line = StringUtil.trimToEmpty(reader.peek());
		
		if (TitleBlockParser.isBlockStart(line)) {
			int level = 0;
			while (!line.isEmpty() && line.startsWith("#")) {
				level++;
				line = line.substring(1);
			}

			final String text = StringUtil.trimToEmpty(line);
			if (!text.isEmpty()) {
				reader.consume();
				return new TitleBlock(text, level);
			}
		}
		
		return new TitleBlock();
	}

	public static boolean isBlockStart(final String line) {
		return line.startsWith("#") && !line.matches("[#]+");
	}

	
	private final LineReader reader;
}
